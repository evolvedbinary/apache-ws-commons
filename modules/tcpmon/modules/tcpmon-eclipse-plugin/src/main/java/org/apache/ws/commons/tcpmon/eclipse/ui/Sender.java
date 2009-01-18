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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 *   This is similar to the main swing sender but includes SWT components instead of Swing ones
 */
class Sender {
    private TabFolder tabFolder = null;

    public Text endpointField;
    public Text actionField;
    public Text inputText;
    public Text outputText;
    public Button xmlFormatBox;
    public Button sendButton;
    public Button switchButton;

    public Sender(TabFolder tabFolder) {
        this.tabFolder = tabFolder;
    }

    public void createSenderTab() {
        final TabItem senderTabItem = new TabItem(tabFolder, SWT.NONE);
        senderTabItem.setText("Sender");

        final Composite tabComposite = new Composite(tabFolder, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        tabComposite.setLayout(gridLayout);
        senderTabItem.setControl(tabComposite);

        (new Label(tabComposite, SWT.NONE)).setText("Connection Endpoint");

        endpointField = new Text(tabComposite, SWT.BORDER);
        endpointField.setText("http://localhost:8080/axis2/services/XYZ");
        endpointField.setLayoutData(new GridData(312, SWT.DEFAULT));

        (new Label(tabComposite, SWT.NONE)).setText("SOAP Action");

        actionField = new Text(tabComposite, SWT.BORDER);
        actionField.setLayoutData(new GridData(60, SWT.DEFAULT));

        final Composite textComposite = new Composite(tabComposite, SWT.NONE);
        textComposite.setLayout(new FillLayout(SWT.VERTICAL));
        final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
        gd.heightHint = 384;
        gd.widthHint = 611;
        textComposite.setLayoutData(gd);

        inputText = new Text(textComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);

        outputText = new Text(textComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);

        xmlFormatBox = new Button(tabComposite, SWT.CHECK);
        xmlFormatBox.setText(TCPMonBundle.getMessage("xmlFormat00", "XML Format"));

        final Composite buttonComposite = new Composite(tabComposite, SWT.NONE);
        buttonComposite.setLayout(new RowLayout());
        final GridData buttonGd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        buttonGd.heightHint = 30;
        buttonGd.widthHint = 166;
        buttonComposite.setLayoutData(buttonGd);

        sendButton = new Button(buttonComposite, SWT.NONE);
        final RowData rowData = new RowData();
        rowData.width = 100;
        sendButton.setLayoutData(rowData);
        sendButton.setText("Send");
        sendButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                send();
            }

        });

        switchButton = new Button(buttonComposite, SWT.NONE);
        final RowData rd = new RowData();
        rd.width = 100;
        switchButton.setLayoutData(rd);
        switchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if (((FillLayout) textComposite.getLayout()).type == SWT.HORIZONTAL) {
                    ((FillLayout) textComposite.getLayout()).type = SWT.VERTICAL;
                } else {
                    ((FillLayout) textComposite.getLayout()).type = SWT.HORIZONTAL;
                }
                textComposite.layout();
            }
        });

        switchButton.setText(TCPMonBundle.getMessage("switch00", "Switch Layout"));
    }

    public void send() {
        try {
            URL u = new URL(endpointField.getText());
            URLConnection uc = u.openConnection();
            HttpURLConnection connection = (HttpURLConnection) uc;
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            String action = "\"" + (actionField.getText() == null ? "" : actionField.getText()) + "\"";
            connection.setRequestProperty("SOAPAction", action);
            connection.setRequestProperty("Content-Type", "text/xml");
            connection.setRequestProperty("User-Agent", "Axis/2.0");
            OutputStream out = connection.getOutputStream();
            Writer writer = new OutputStreamWriter(out);
            writer.write(inputText.getText());
            writer.flush();
            writer.close();
            String line;
            InputStream inputStream = null;
            try {
                inputStream = connection.getInputStream();
            } catch (IOException e) {
                inputStream = connection.getErrorStream();
            }
            outputText.setText("");
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = rd.readLine()) != null) {
                outputText.append(line);
            }
            if (xmlFormatBox.getSelection()) {
                outputText.setText(prettyXML(outputText.getText()));
            }
        } catch (Exception e) {
            StringWriter w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            outputText.setText(w.toString());
        }
    }

    public String prettyXML(String input) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            transformerFactory.setAttribute("indent-number", new Integer(2));
        } catch (Exception e) {
        }
        Transformer transformer = transformerFactory.newTransformer();
        try {
            transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
        } catch (Exception e) {
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(input)), new StreamResult(writer));
        return writer.toString();
    }

}
