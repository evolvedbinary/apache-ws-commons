/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ws.commons.tcpmon.eclipse.ui;

import org.apache.ws.commons.tcpmon.SlowLinkSimulator;
import org.apache.ws.commons.tcpmon.TCPMonBundle;
import org.apache.ws.commons.tcpmon.core.Configuration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

/**
 * This is similar to the main swing listener but includes SWT components instead of Swing ones
 *
 */
class Listener {
    private Composite leftPanel = null;
    private Composite rightPanel = null;
    private Composite textComposite = null;
    private Text portField = null;
    private Text hostField = null;
    private Text tPortField = null;
    private Button isProxyBox = null;
    private Button stopButton = null;
    public Button removeButton = null;
    public Button removeAllButton = null;
    private Button xmlFormatBox = null;
    public Button saveButton = null;
    public Button resendButton = null;
    public Table connectionTable = null;
    public TableEnhancer tableEnhancer = null;

    private TabFolder tabFolder;
    private TabItem portTabItem;

    private SocketWaiter sw = null;
    private SlowLinkSimulator slowLink;

    public final Vector connections = new Vector();

    public String HTTPProxyHost = null;
    public int HTTPProxyPort = 80;

    public Listener(TabFolder tabFolder, String name, int listenPort,
                    String host, int targetPort, boolean isProxy,
                    SlowLinkSimulator slowLink) {
        if (name == null) {
            name = TCPMonBundle.getMessage("port01", "Port") + " " + listenPort;
        }
        // set the slow link to the passed down link
        if (slowLink != null) {
            this.slowLink = slowLink;
        } else {
            // or make up a no-op one.
            this.slowLink = new SlowLinkSimulator(0, 0);
        }

        this.tabFolder = tabFolder;
        createPortTab(isProxy, listenPort, host, targetPort);

    }

    public void createPortTab(boolean isProxy, int listenPort, String host, int targetPort) {
        portTabItem = new TabItem(tabFolder, SWT.NONE);

        final Composite composite = new Composite(tabFolder, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.numColumns = 8;
        composite.setLayout(gl);
        portTabItem.setControl(composite);

        stopButton = new Button(composite, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.widthHint = 71;
        stopButton.setLayoutData(gd);
        final String start = TCPMonBundle.getMessage("start00", "Start");
        stopButton.setText(start);

        final Label listenPortLabel = new Label(composite, SWT.NONE);
        gd = new GridData();
        gd.horizontalIndent = 5;
        listenPortLabel.setLayoutData(gd);
        listenPortLabel.setText(TCPMonBundle.getMessage("listenPort01", "Listen Port:"));

        portField = new Text(composite, SWT.BORDER);
        portField.setText("" + listenPort);
        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.widthHint = 40;
        portField.setLayoutData(gd);

        (new Label(composite, SWT.NONE)).setText(TCPMonBundle.getMessage("host00", "Host:"));

        hostField = new Text(composite, SWT.BORDER);
        hostField.setText(host);
        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.widthHint = 202;
        hostField.setLayoutData(gd);

        (new Label(composite, SWT.NONE)).setText(TCPMonBundle.getMessage("port02", "Port:"));

        tPortField = new Text(composite, SWT.BORDER);
        tPortField.setText("" + targetPort);
        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.widthHint = 40;
        tPortField.setLayoutData(gd);

        isProxyBox = new Button(composite, SWT.LEFT | SWT.CHECK);
        isProxyBox.setAlignment(SWT.LEFT);
        gd = new GridData(64, SWT.DEFAULT);
        gd.verticalIndent = 2;
        gd.horizontalIndent = 10;
        isProxyBox.setLayoutData(gd);
        isProxyBox.setText(TCPMonBundle.getMessage("proxy00", "Proxy"));
        isProxyBox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean state = ((Button) e.getSource()).getSelection();
                tPortField.setEnabled(!state);
                hostField.setEnabled(!state);
            }
        });
        isProxyBox.setSelection(isProxy);
        portField.setEditable(false);
        hostField.setEditable(false);
        tPortField.setEditable(false);
        stopButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (stopButton.getText().equals(TCPMonBundle.getMessage("stop00", "Stop"))) {
                    stop();
                } else {
                    start();
                }
            }
        });

        connectionTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        connectionTable.setHeaderVisible(true);
        gd = new GridData(SWT.LEFT, SWT.FILL, true, false, 8, 1);
        gd.heightHint = 102;
        connectionTable.setLayoutData(gd);

        final TableColumn stateColumn = new TableColumn(connectionTable, SWT.CENTER);
        stateColumn.setWidth(47);
        stateColumn.setText(TCPMonBundle.getMessage("state00", "State"));

        final TableColumn timeColumn = new TableColumn(connectionTable, SWT.CENTER);
        timeColumn.setWidth(100);
        timeColumn.setText(TCPMonBundle.getMessage("time00", "Time"));

        final TableColumn reqHostColumn = new TableColumn(connectionTable, SWT.CENTER);
        reqHostColumn.setWidth(100);
        reqHostColumn.setText(TCPMonBundle.getMessage("requestHost00", "Request Host"));

        final TableColumn targetHostColumn = new TableColumn(connectionTable, SWT.CENTER);
        targetHostColumn.setWidth(100);
        targetHostColumn.setText(TCPMonBundle.getMessage("targetHost", "Target Host"));

        final TableColumn requestColumn = new TableColumn(connectionTable, SWT.CENTER);
        requestColumn.setWidth(100);
        requestColumn.setText(TCPMonBundle.getMessage("request00", "Request..."));

        final TableColumn elapsedTimeColumn = new TableColumn(connectionTable, SWT.CENTER);
        elapsedTimeColumn.setWidth(140);
        elapsedTimeColumn.setText(TCPMonBundle.getMessage("elapsed00", "Elapsed Time"));

        final TableItem headerItem = new TableItem(connectionTable, SWT.BORDER);
        headerItem.setText(new String[]{"--", "Most Recent", "--", "--", "--", "--"});
        tableEnhancer = new TableEnhancer(connectionTable);

//		SelectionListener part goes here - I THINK IT'S DONE

        connectionTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleSelection();
            }
        });


        final Composite buttonComposite = new Composite(composite, SWT.NONE);
        buttonComposite.setLayout(new RowLayout());
        gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
        gd.heightHint = 32;
        gd.widthHint = 59;
        buttonComposite.setLayoutData(gd);

        removeButton = new Button(buttonComposite, SWT.NONE);
        RowData rd = new RowData();
        rd.width = 100;
        removeButton.setLayoutData(rd);
        final String removeSelected = TCPMonBundle.getMessage("removeSelected00", "Remove Selected");
        removeButton.setText(removeSelected);
        removeButton.setEnabled(false);
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (removeSelected.equals(((Button) e.getSource()).getText())) {
                    remove();
                }
            }
        });

        removeAllButton = new Button(buttonComposite, SWT.NONE);
        rd = new RowData();
        rd.width = 100;
        removeAllButton.setLayoutData(rd);
        final String removeAll = TCPMonBundle.getMessage("removeAll00", "Remove All");
        removeAllButton.setText(removeAll);
        removeAllButton.setEnabled(false);
        removeAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (removeAll.equals(((Button) e.getSource()).getText())) {
                    removeAll();
                }
            }
        });

        textComposite = new Composite(composite, SWT.NONE);
        textComposite.setLayout(new FillLayout(SWT.VERTICAL));
        gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 8, 1);
        gd.widthHint = 589;
        gd.heightHint = 230;
        textComposite.setLayoutData(gd);

        leftPanel = new Composite(textComposite, SWT.BORDER);
        leftPanel.setLayout(new GridLayout(8, true));

        rightPanel = new Composite(textComposite, SWT.BORDER);
        rightPanel.setLayout(new GridLayout(8, true));

        xmlFormatBox = new Button(composite, SWT.CHECK);
        xmlFormatBox.setText(TCPMonBundle.getMessage("xmlFormat00", "XML Format"));

        final Composite buttonComposite2 = new Composite(composite, SWT.NONE);
        buttonComposite2.setLayout(new RowLayout());
        gd = new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1);
        gd.heightHint = 27;
        gd.widthHint = 27;
        buttonComposite2.setLayoutData(gd);

        saveButton = new Button(buttonComposite2, SWT.NONE);
        rd = new RowData();
        rd.width = 100;
        saveButton.setLayoutData(rd);
        final String save = TCPMonBundle.getMessage("save00", "Save");
        saveButton.setText(save);
        saveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (save.equals(((Button) e.getSource()).getText())) {
                    save();
                }
            }
        });

        resendButton = new Button(buttonComposite2, SWT.NONE);
        rd = new RowData();
        rd.width = 100;
        resendButton.setLayoutData(rd);
        final String resend = TCPMonBundle.getMessage("resend00", "Resend");
        resendButton.setText(resend);
        resendButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (resend.equals(((Button) e.getSource()).getText())) {
                    resend();
                }
            }
        });

        Button switchButton = new Button(buttonComposite2, SWT.NONE);
        rd = new RowData();
        rd.width = 100;
        switchButton.setLayoutData(rd);
        final String switchStr = TCPMonBundle.getMessage("switch00", "Switch Layout");
        switchButton.setText(switchStr);
        switchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (switchStr.equals(((Button) e.getSource()).getText())) {
                    if (((FillLayout) textComposite.getLayout()).type == SWT.HORIZONTAL) {
                        ((FillLayout) textComposite.getLayout()).type = SWT.VERTICAL;
                    } else {
                        ((FillLayout) textComposite.getLayout()).type = SWT.HORIZONTAL;
                    }
                    leftPanel.layout();
                    rightPanel.layout();
                    textComposite.layout();
                }
            }
        });

        final Composite buttonComposite3 = new Composite(composite, SWT.NONE);
        buttonComposite3.setLayout(new RowLayout());
        gd = new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1);
        gd.horizontalIndent = 95;
        gd.heightHint = 27;
        gd.widthHint = 27;
        buttonComposite3.setLayoutData(gd);

        Button closeButton = new Button(buttonComposite3, SWT.None);
        rd = new RowData();
        rd.width = 60;
        closeButton.setLayoutData(rd);
        final String close = TCPMonBundle.getMessage("close00", "Close");
        closeButton.setText(close);
        closeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (close.equals(((Button) e.getSource()).getText())) {
                    close();
                }
            }
        });
        tableEnhancer.setSelectionInterval(0, 0);
        start();
    }

    public void handleSelection() {
        if (tableEnhancer.isSelectionEmpty()) {
            setLeft(MainView.SWT_LABEL, " " + TCPMonBundle.getMessage("wait00",
                    "Waiting for Connection..."));
            setRight(MainView.SWT_LABEL, "");
            removeButton.setEnabled(false);
            removeAllButton.setEnabled(false);
            saveButton.setEnabled(false);
            resendButton.setEnabled(false);
            leftPanel.layout();
            rightPanel.layout();
            textComposite.layout();
        } else {
            int row = tableEnhancer.getLeadSelectionIndex();
            if (row != tableEnhancer.getMaxSelectionIndex()) {
                row = tableEnhancer.getMaxSelectionIndex();
            }
            if (row == 0) {
                if (connections.size() == 0) {
                    setLeft(MainView.SWT_LABEL, " " + TCPMonBundle.getMessage("wait00",
                            "Waiting for connection..."));
                    setRight(MainView.SWT_LABEL, "");
                    removeButton.setEnabled(false);
                    removeAllButton.setEnabled(false);
                    saveButton.setEnabled(false);
                    resendButton.setEnabled(false);
                    leftPanel.layout();
                    rightPanel.layout();
                    textComposite.layout();
                } else {
                    Connection conn = (Connection) connections.lastElement();
                    removeChildren(leftPanel.getChildren());
                    removeChildren(rightPanel.getChildren());
                    ((GridData) conn.inputText.getLayoutData()).exclude = false;
                    conn.inputText.setVisible(true);
                    ((GridData) conn.outputText.getLayoutData()).exclude = false;
                    conn.outputText.setVisible(true);
                    removeButton.setEnabled(false);
                    removeAllButton.setEnabled(true);
                    saveButton.setEnabled(true);
                    resendButton.setEnabled(true);
                    leftPanel.layout();
                    rightPanel.layout();
                    textComposite.layout();
                }
            } else {
                Connection conn = (Connection) connections.get(row - 1);
                removeChildren(leftPanel.getChildren());
                removeChildren(rightPanel.getChildren());
                ((GridData) conn.inputText.getLayoutData()).exclude = false;
                conn.inputText.setVisible(true);
                ((GridData) conn.outputText.getLayoutData()).exclude = false;
                conn.outputText.setVisible(true);
                removeButton.setEnabled(true);
                removeAllButton.setEnabled(true);
                saveButton.setEnabled(true);
                resendButton.setEnabled(true);
                leftPanel.layout();
                rightPanel.layout();
                textComposite.layout();
            }
        }
    }

    public void stop() {
        try {
            for (int i = 0; i < connections.size(); i++) {
                Connection conn = (Connection) connections.get(i);
                conn.halt();
            }
            sw.halt();
            stopButton.setText(TCPMonBundle.getMessage("start00", "Start"));
            portField.setEditable(true);
            hostField.setEditable(true);
            tPortField.setEditable(true);
            isProxyBox.setEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        int port = Integer.parseInt(portField.getText());
        portField.setText("" + port);
        portTabItem.setText(TCPMonBundle.getMessage("port01", "Port") + " " + port);
        int tmp = Integer.parseInt(tPortField.getText());
        tPortField.setText("" + tmp);
        sw = new SocketWaiter(this, port);
        stopButton.setText(TCPMonBundle.getMessage("stop00", "Stop"));
        portField.setEditable(false);
        hostField.setEditable(false);
        tPortField.setEditable(false);
        isProxyBox.setEnabled(false);
    }


    public void remove() {
        int index;
        Connection con;
        int[] selectionIndices = tableEnhancer.getSelectionIndicesWithoutZero();
        for (int i = 0; i < selectionIndices.length; i++) {
            index = selectionIndices[i];
            con = (Connection) connections.get(index - 1 - i);
            if (con.isActive()) {
                MessageBox mb = new MessageBox(MainView.display.getActiveShell(), SWT.ICON_INFORMATION | SWT.OK);
                mb.setMessage(TCPMonBundle.getMessage("inform00", "Connection can be removed only when its status indicates Done"));
                mb.setText("Connection Active");
                mb.open();
                continue;
            }
            con.halt();
            con.inputText.dispose();
            con.outputText.dispose();
            connections.remove(con);
            tableEnhancer.remove(index - i);
            tableEnhancer.setSelectionInterval(0, 0);
        }
    }


    public void removeAll() {
        tableEnhancer.selectAll();
        remove();
    }


    public void close() {
        MessageBox mb = new MessageBox(MainView.display.getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        mb.setMessage(TCPMonBundle.getMessage("quit00", "Do you want to remove monitoring") + " " + portTabItem.getText());
        mb.setText("Remove Monitor");
        int response = mb.open();
        if (response == SWT.YES) {
            if (stopButton.getText().equals(TCPMonBundle.getMessage("stop00", "Stop"))) {
                stop();
            }
            portTabItem.dispose();
        }
    }


    public void save() {
        FileDialog fd = new FileDialog(MainView.display.getActiveShell(), SWT.SAVE);
        fd.setText("Save");
        fd.setFilterPath(".");
        String path = fd.open();
        if (path == null) {
            return;
        }
        try {
            File file = new File(path);
            FileOutputStream out = new FileOutputStream(file);
            int rc = tableEnhancer.getLeadSelectionIndex();
            int n = 0;
            for (Iterator i = connections.iterator(); i.hasNext();
                 n++) {
                Connection conn = (Connection) i.next();
                if (tableEnhancer.isSelectedIndex(n + 1)
                        || (!(i.hasNext())
                        && (tableEnhancer.getLeadSelectionIndex() == 0))) {
                    rc = Integer.parseInt(portField.getText());
                    out.write("\n==============\n".getBytes());
                    out.write(((TCPMonBundle.getMessage("listenPort01",
                            "Listen Port:")
                            + " " + rc + "\n")).getBytes());
                    out.write((TCPMonBundle.getMessage("targetHost01",
                            "Target Host:")
                            + " " + hostField.getText()
                            + "\n").getBytes());
                    rc = Integer.parseInt(tPortField.getText());
                    out.write(((TCPMonBundle.getMessage("targetPort01",
                            "Target Port:")
                            + " " + rc + "\n")).getBytes());
                    out.write((("==== "
                            + TCPMonBundle.getMessage("request01", "Request")
                            + " ====\n")).getBytes());
                    out.write(conn.inputText.getText().getBytes());
                    out.write((("==== "
                            + TCPMonBundle.getMessage("response00", "Response")
                            + " ====\n")).getBytes());
                    out.write(conn.outputText.getText().getBytes());
                    out.write("\n==============\n".getBytes());
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resend() {
        try {
            int rc;
            Connection conn;
            rc = tableEnhancer.getMaxSelectionIndex();
            if (rc == 0) {
                conn = (Connection) connections.lastElement();
            } else {
                conn = (Connection) connections.get(rc - 1);
            }

            if (rc > 0) {
                tableEnhancer.clearSelection();
                tableEnhancer.setSelectionInterval(0, 0);
            }
            InputStream in = null;
            String text = conn.inputText.getText();

            // Fix Content-Length HTTP headers
            if (text.startsWith("POST ") || text.startsWith("GET ")) {

                int pos1, pos2, pos3;
                String headers;
                pos3 = text.indexOf("\n\n");
                if (pos3 == -1) {
                    pos3 = text.indexOf("\r\n\r\n");
                    if (pos3 != -1) {
                        pos3 = pos3 + 4;
                    }
                } else {
                    pos3 += 2;
                }
                headers = text.substring(0, pos3);
                pos1 = headers.indexOf("Content-Length:");

                if (pos1 != -1) {
                    int newLen = text.length() - pos3;
                    pos2 = headers.indexOf("\n", pos1);
                    System.err.println("CL: " + newLen);
                    System.err.println("Hdrs: '" + headers + "'");
                    System.err.println("subTEXT: '"
                            + text.substring(pos3, pos3 + newLen)
                            + "'");
                    text = headers.substring(0, pos1) + "Content-Length: "
                            + newLen + "\n" + headers.substring(pos2 + 1)
                            + text.substring(pos3);
                    System.err.println("\nTEXT: '" + text + "'");
                }
            }
            in = new ByteArrayInputStream(text.getBytes());
            new Connection(this, in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Object setLeft(int type, String text) {
        Control[] children = leftPanel.getChildren();
        removeChildren(children);
        switch (type) {
            case 0:
                Label label = new Label(leftPanel, SWT.NONE);
                GridData gd = new GridData(GridData.FILL_BOTH);
                gd.grabExcessHorizontalSpace = true;
                gd.horizontalSpan = 8;
                gd.exclude = false;
                label.setLayoutData(gd);
                label.setText(text);
                leftPanel.layout();
                textComposite.layout();
                return label;
            case 1:
                Text leftText = new Text(leftPanel, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
                GridData gd1 = new GridData(GridData.FILL_BOTH);
                gd1.grabExcessHorizontalSpace = true;
                gd1.horizontalSpan = 8;
                gd1.exclude = false;
                leftText.setLayoutData(gd1);
                leftText.setText(text);
                leftPanel.layout();
                textComposite.layout();
                return leftText;
        }
        return null;
    }

    public void layout() {
        leftPanel.layout();
        rightPanel.layout();
    }


    public Object setRight(int type, String text) {
        Control[] children = rightPanel.getChildren();
        removeChildren(children);
        switch (type) {
            case 0:
                Label label = new Label(rightPanel, SWT.NONE);
                GridData gd = new GridData(GridData.FILL_BOTH);
                gd.grabExcessHorizontalSpace = true;
                gd.horizontalSpan = 8;
                gd.exclude = false;
                label.setLayoutData(gd);
                label.setText(text);
                rightPanel.layout();
                textComposite.layout();
                return label;
            case 1:
                Text rightText = new Text(rightPanel, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
                GridData gd1 = new GridData(GridData.FILL_BOTH);
                gd1.grabExcessHorizontalSpace = true;
                gd1.horizontalSpan = 8;
                rightText.setLayoutData(gd1);
                rightText.setText(text);
                rightPanel.layout();
                textComposite.layout();
                return rightText;
        }
        return null;
    }

    private void removeChildren(Control[] children) {
        for (int i = 0; i < children.length; i++) {
            ((GridData) children[i].getLayoutData()).exclude = true;
            children[i].setVisible(false);
            children[i].getShell().layout(true);
        }
    }

    public Configuration getConfiguration() {
        final Configuration config = new Configuration();
        MainView.display.syncExec(new Runnable() {
            public void run() {
                config.setListenPort(Integer.parseInt(portField.getText()));
                config.setTargetHost(hostField.getText());
                config.setTargetPort(Integer.parseInt(tPortField.getText()));
                config.setProxy(isProxyBox.getSelection());
                config.setXmlFormat(xmlFormatBox.getSelection());
            }
        });
        config.setHttpProxyHost(HTTPProxyHost);
        config.setHttpProxyPort(HTTPProxyPort);
        config.setSlowLink(slowLink);
        return config;
    }
}
