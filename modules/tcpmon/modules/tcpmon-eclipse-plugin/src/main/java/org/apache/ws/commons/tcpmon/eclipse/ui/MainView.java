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


import org.apache.ws.commons.tcpmon.TCPMonBundle;
import org.apache.ws.commons.tcpmon.core.engine.InterceptorConfiguration;
import org.apache.ws.commons.tcpmon.core.engine.InterceptorConfigurationBuilder;
import org.apache.ws.commons.tcpmon.core.filter.throttle.ThrottleConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 * The class <code>MainView</code> is the UI mounted into Eclipse as a View
 *
 */
public class MainView extends ViewPart{
    public static final int SWT_LABEL = 0;
    public static final int SWT_TEXT = 1;
    public static final int STATE_COLUMN = 0;
    public static final int OUTHOST_COLUMN = 3;
    public static final int REQ_COLUMN = 4;
    public static final int ELAPSED_COLUMN = 5;

    public static Display display;

    public Button addButton = null;
    public Button listenerButton = null;
    public Button proxyButton = null;
    public Button hTTPProxyBox = null;
    public Button delayBox = null;
    public Text port = null;
    public Text host = null;
    public Text tport = null;
    public Text hTTPProxyHost = null;
    public Text hTTPProxyPort = null;
    public Text delayBytes = null;
    public Text delayTime = null;

    public MainView() {
    }

    public void createPartControl(Composite parent) {
        display = parent.getDisplay();
        TabFolder tabFolder = new TabFolder(parent, SWT.TOP);
        createConfigurationTab(tabFolder);
        new Sender(tabFolder).createSenderTab();
    }

    private void createConfigurationTab(final TabFolder tabFolder) {
        TabItem configTab = new TabItem(tabFolder, SWT.NONE);
        configTab.setText("Configuration");

        Composite composite = new Composite(tabFolder, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));


        GridData gd = new GridData();
        gd.horizontalSpan = 3;
        gd.verticalIndent = 10;
        Label label = new Label(composite, SWT.NONE);
        label.setText(TCPMonBundle.getMessage("newTCP00", "Create a new TCPMon..."));
        label.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalAlignment = SWT.BEGINNING;
        gd.verticalIndent = 15;
        label = new Label(composite, SWT.NONE);
        label.setText(TCPMonBundle.getMessage("listenPort00", "Listen Port #"));
        label.setLayoutData(gd);

        gd = new GridData();
        gd.verticalIndent = 15;
        gd.widthHint = 60;
        port = new Text(composite, SWT.BORDER);
        port.setLayoutData(gd);

        addActAsOptions(composite);
        addOptions(composite);

        gd = new GridData(SWT.FILL, SWT.FILL, false, false);
        gd.verticalIndent = 10;
        gd.heightHint = 30;
        addButton = new Button(composite, SWT.PUSH);
        final String add = TCPMonBundle.getMessage("add00", "Add");
        addButton.setText(add);
        addButton.setLayoutData(gd);
        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if (add.equals(((Button) e.getSource()).getText())) {
                    InterceptorConfiguration config = getConfiguration();
                    if (config == null) {
                        return;
                    }
                    try {
                        new Listener(tabFolder, null, config);
                    } catch (Exception exp) {
                        exp.printStackTrace();
                    }

                    port.setText("");
                }
            }
        });
        configTab.setControl(composite);
    }

    private InterceptorConfiguration getConfiguration() {
        InterceptorConfigurationBuilder configBuilder = new InterceptorConfigurationBuilder();
        
        int lPort = getValue(0, port.getText());
        if (lPort == 0) {
            return null;
        }
        configBuilder.setListenPort(lPort);
        configBuilder.setProxy(proxyButton.getSelection());
        configBuilder.setTargetHost(host.getText());
        configBuilder.setTargetPort(getValue(0, tport.getText()));
        if (delayBox.getSelection()) {
            ThrottleConfiguration throttleConfig = new ThrottleConfiguration(getValue(0, delayBytes.getText()), getValue(0, delayTime.getText()));
            configBuilder.addRequestFilter(throttleConfig);
            configBuilder.addResponseFilter(throttleConfig);
        }
        
        String text = hTTPProxyHost.getText();
        if (text.length() > 0) {
            configBuilder.setHttpProxyHost(text);
            text = hTTPProxyPort.getText();
            int proxyPort = getValue(-1, hTTPProxyPort.getText());
            if (proxyPort != -1) {
                configBuilder.setHttpProxyPort(Integer.parseInt(text));
            }
        } else {
            configBuilder.configProxyFromSystemProperties();
        }
        
        return configBuilder.build();
    }

    private void addActAsOptions(Composite composite) {
        GridData gd = new GridData();
        gd.horizontalSpan = 3;
        gd.verticalIndent = 5;
        Label label = new Label(composite, SWT.NONE);
        label.setText(TCPMonBundle.getMessage("actAs00", "Act as a..."));
        label.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalSpan = 3;
        gd.verticalIndent = 5;
        listenerButton = new Button(composite, SWT.RADIO);
        listenerButton.setText(TCPMonBundle.getMessage("listener00", "Listener"));
        listenerButton.setSelection(true);
        listenerButton.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.verticalIndent = 5;
        gd.horizontalIndent = 25;
        final Label hostLabel = new Label(composite, SWT.NONE);
        hostLabel.setText(TCPMonBundle.getMessage("targetHostname00", "Target Hostname"));
        hostLabel.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.NONE, true, false);
        gd.verticalIndent = 5;
        host = new Text(composite, SWT.BORDER);
        host.setText("127.0.0.1");
        host.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.verticalIndent = 2;
        gd.horizontalIndent = 25;
        final Label tportLabel = new Label(composite, SWT.NONE);
        tportLabel.setText(TCPMonBundle.getMessage("targetPort00", "Target Port #"));
        tportLabel.setLayoutData(gd);

        gd = new GridData(SWT.NONE, SWT.NONE, false, false);
        gd.verticalIndent = 2;
        gd.widthHint = 60;
        tport = new Text(composite, SWT.BORDER);
        tport.setText("8080");
        tport.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalSpan = 3;
        gd.verticalIndent = 5;
        proxyButton = new Button(composite, SWT.RADIO);
        proxyButton.setText(TCPMonBundle.getMessage("proxy00", "Proxy"));
        proxyButton.setLayoutData(gd);

        listenerButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean state = ((Button) e.getSource()).getSelection();
                hostLabel.setEnabled(state);
                host.setEnabled(state);
                tportLabel.setEnabled(state);
                tport.setEnabled(state);
            }
        });
    }

    private void addOptions(Composite composite) {
        GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
        gd.horizontalSpan = 3;
        gd.verticalIndent = 5;
        final Group optGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
        optGroup.setText(TCPMonBundle.getMessage("options00", "Options"));
        optGroup.setLayoutData(gd);

        hTTPProxyBox = new Button(optGroup, SWT.CHECK);
        hTTPProxyBox.setBounds(10, 20, 200, 20);
        hTTPProxyBox.setText(TCPMonBundle.getMessage("proxySupport00", "HTTP Proxy Support"));

        final Label hTTPProxyHostLabel = new Label(optGroup, SWT.NONE);
        hTTPProxyHostLabel.setEnabled(false);
        hTTPProxyHostLabel.setText(TCPMonBundle.getMessage("hostname00", "Hostname"));
        hTTPProxyHostLabel.setBounds(30, 50, 70, 25);

        hTTPProxyHost = new Text(optGroup, SWT.BORDER);
        hTTPProxyHost.setEnabled(false);
        hTTPProxyHost.setBounds(110, 50, 300, 25);

        final Label hTTPProxyPortLabel = new Label(optGroup, SWT.NONE);
        hTTPProxyPortLabel.setEnabled(false);
        hTTPProxyPortLabel.setText(TCPMonBundle.getMessage("port00", "Port #"));
        hTTPProxyPortLabel.setBounds(30, 85, 70, 25);

        hTTPProxyPort = new Text(optGroup, SWT.BORDER);
        hTTPProxyPort.setEnabled(false);
        hTTPProxyPort.setBounds(110, 85, 70, 25);

        // Set default proxy values...
        String tmp = System.getProperty("http.proxyHost");
        if ((tmp != null) && tmp.equals("")) {
            tmp = null;
        }

        hTTPProxyBox.setSelection(tmp != null);
        hTTPProxyHost.setEnabled(tmp != null);
        hTTPProxyPort.setEnabled(tmp != null);
        hTTPProxyHostLabel.setEnabled(tmp != null);
        hTTPProxyPortLabel.setEnabled(tmp != null);
        if (tmp != null) {
            hTTPProxyBox.setSelection(true);
            hTTPProxyHost.setText(tmp);
            tmp = System.getProperty("http.proxyPort");
            if ((tmp != null) && tmp.equals("")) {
                tmp = null;
            }
            if (tmp == null) {
                tmp = "80";
            }
            hTTPProxyPort.setText(tmp);
        }

        delayBox = new Button(optGroup, SWT.CHECK);
        delayBox.setBounds(10, 120, 200, 20);
        final String delaySupport = TCPMonBundle.getMessage("delay00", "Simulate Slow Connection");
        delayBox.setText(delaySupport);

        final Label delayBytesLabel = new Label(optGroup, SWT.NONE);
        delayBytesLabel.setEnabled(false);
        delayBytesLabel.setText(TCPMonBundle.getMessage("delay01", "Bytes per Pause"));
        delayBytesLabel.setBounds(30, 150, 130, 25);

        delayBytes = new Text(optGroup, SWT.BORDER);
        delayBytes.setEnabled(false);
        delayBytes.setBounds(170, 150, 70, 25);

        final Label delayTimeLabel = new Label(optGroup, SWT.NONE);
        delayTimeLabel.setEnabled(false);
        delayTimeLabel.setText(TCPMonBundle.getMessage("delay02", "Delay in Milliseconds"));
        delayTimeLabel.setBounds(30, 185, 130, 25);

        delayTime = new Text(optGroup, SWT.BORDER);
        delayTime.setEnabled(false);
        delayTime.setBounds(170, 185, 70, 25);


        hTTPProxyBox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean state = ((Button) e.getSource()).getSelection();
                hTTPProxyHostLabel.setEnabled(state);
                hTTPProxyPortLabel.setEnabled(state);
                hTTPProxyHost.setEnabled(state);
                hTTPProxyPort.setEnabled(state);
            }
        });

        delayBox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean state = ((Button) e.getSource()).getSelection();
                delayBytesLabel.setEnabled(state);
                delayTimeLabel.setEnabled(state);
                delayBytes.setEnabled(state);
                delayTime.setEnabled(state);
            }
        });

    }

    public void setFocus() {
    }

    public int getValue(int def, String text) {
        int result = def;
        if ((text != null) && (text.length() != 0)) {
            try {
                result = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                return (result = def);
            }
        }
        return result;
    }

}