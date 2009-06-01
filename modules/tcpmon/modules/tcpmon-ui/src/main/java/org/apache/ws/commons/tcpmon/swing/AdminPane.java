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

package org.apache.ws.commons.tcpmon.swing;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.apache.ws.commons.tcpmon.TCPMonBundle;
import org.apache.ws.commons.tcpmon.core.engine.InterceptorConfiguration;
import org.apache.ws.commons.tcpmon.core.engine.InterceptorConfigurationBuilder;
import org.apache.ws.commons.tcpmon.core.filter.throttle.ThrottleConfiguration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.GeneralSecurityException;

/**
 * this is the admin page
 */
public class AdminPane extends JPanel {
	
    /**
     * Field listenerButton, proxyButton
     */
    public JRadioButton listenerButton, proxyButton;

    /**
     * Field hostLabel, tportLabel
     */
    public JLabel hostLabel, tportLabel;

    /**
     * Field port
     */
    public NumberField port;

    /**
     * Field host
     */
    public HostnameField host;

    /**
     * Field tport
     */
    public NumberField tport;

    /**
     * Field noteb
     */
    public JTabbedPane noteb;

    /**
     * Field HTTPProxyBox
     */
    public JCheckBox HTTPProxyBox;

    /**
     * Field HTTPProxyHost
     */
    public HostnameField HTTPProxyHost;

    /**
     * Field HTTPProxyPort
     */
    public NumberField HTTPProxyPort;

    /**
     * Field HTTPProxyHostLabel, HTTPProxyPortLabel
     */
    public JLabel HTTPProxyHostLabel, HTTPProxyPortLabel;

    /**
     * Field delayTimeLabel, delayBytesLabel
     */
    public JLabel delayTimeLabel, delayBytesLabel;

    /**
     * Field delayTime, delayBytes
     */
    public NumberField delayTime, delayBytes;

    /**
     * Field delayBox
     */
    public JCheckBox delayBox;
    
//    private final JCheckBox incomingSSLBox;
    
    private final JCheckBox outgoingSSLBox;
    
    final JCheckBox replaceURIsInContentBox;

    /**
     * Constructor AdminPage
     *
     * @param notebook
     * @param name
     */
    public AdminPane(JTabbedPane notebook, String name) {
        JPanel mainPane = null;
        JButton addButton = null;
        this.setLayout(new BorderLayout());
        noteb = notebook;
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        mainPane = new JPanel(layout);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        mainPane.add(new JLabel(TCPMonBundle.getMessage("newTCP00",
                "Create a new TCPMon...")
                + " "), c);

        // Add some blank space
        mainPane.add(Box.createRigidArea(new Dimension(1, 5)), c);

        // The listener info
        // /////////////////////////////////////////////////////////////////
        JPanel tmpPanel = new JPanel(new GridBagLayout());
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        tmpPanel.add(new JLabel(TCPMonBundle.getMessage("listenPort00",
                "Listen Port #")
                + " "), c);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        tmpPanel.add(port = new NumberField(4), c);
        mainPane.add(tmpPanel, c);
        mainPane.add(Box.createRigidArea(new Dimension(1, 5)), c);

        // Group for the radio buttons
        ButtonGroup btns = new ButtonGroup();
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        mainPane.add(new JLabel(TCPMonBundle.getMessage("actAs00", "Act as a...")), c);

        // Target Host/Port section
        // /////////////////////////////////////////////////////////////////
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        final String listener = TCPMonBundle.getMessage("listener00", "Listener");
        mainPane.add(listenerButton = new JRadioButton(listener), c);
        btns.add(listenerButton);
        listenerButton.setSelected(true);
        listenerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (listener.equals(event.getActionCommand())) {
                    boolean state = listenerButton.isSelected();
                    tport.setEnabled(state);
                    host.setEnabled(state);
                    hostLabel.setForeground(state
                            ? Color.black
                            : Color.gray);
                    tportLabel.setForeground(state
                            ? Color.black
                            : Color.gray);
                    replaceURIsInContentBox.setEnabled(state);
                }
            }
        });
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        mainPane.add(Box.createRigidArea(new Dimension(25, 0)));
        mainPane.add(hostLabel =
                new JLabel(TCPMonBundle.getMessage("targetHostname00",
                        "Target Hostname")
                + " "), c);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        host = new HostnameField(30);
        mainPane.add(host, c);
        host.setText(TCPMon.DEFAULT_HOST);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        mainPane.add(Box.createRigidArea(new Dimension(25, 0)));
        mainPane.add(tportLabel =
                new JLabel(TCPMonBundle.getMessage("targetPort00", "Target Port #")
                + " "), c);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        tport = new NumberField(4);
        mainPane.add(tport, c);
        tport.setValue(TCPMon.DEFAULT_PORT);

        // Act as proxy section
        // /////////////////////////////////////////////////////////////////
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        final String proxy = TCPMonBundle.getMessage("proxy00", "Proxy");
        mainPane.add(proxyButton = new JRadioButton(proxy), c);
        btns.add(proxyButton);
        proxyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (proxy.equals(event.getActionCommand())) {
                    // TODO: duplicate code here!!
                    boolean state = proxyButton.isSelected();
                    tport.setEnabled(!state);
                    host.setEnabled(!state);
                    hostLabel.setForeground(state
                            ? Color.gray
                            : Color.black);
                    tportLabel.setForeground(state
                            ? Color.gray
                            : Color.black);
                    replaceURIsInContentBox.setEnabled(!state);
                }
            }
        });

        // Spacer
        // ///////////////////////////////////////////////////////////////
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        mainPane.add(Box.createRigidArea(new Dimension(1, 10)), c);

        // Options section
        // /////////////////////////////////////////////////////////////////
        JPanel opts = new JPanel(new GridBagLayout());
        opts.setBorder(new TitledBorder(TCPMonBundle.getMessage("options00",
                "Options")));
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        mainPane.add(opts, c);

        // HTTP Proxy Support section
        // /////////////////////////////////////////////////////////////////
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        final String proxySupport = TCPMonBundle.getMessage("proxySupport00",
                "HTTP Proxy Support");
        opts.add(HTTPProxyBox = new JCheckBox(proxySupport), c);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        opts.add(HTTPProxyHostLabel =
                new JLabel(TCPMonBundle.getMessage("hostname00", "Hostname") + " "),
                c);
        HTTPProxyHostLabel.setForeground(Color.gray);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        opts.add(HTTPProxyHost = new HostnameField(30), c);
        HTTPProxyHost.setEnabled(false);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        opts.add(HTTPProxyPortLabel =
                new JLabel(TCPMonBundle.getMessage("port00", "Port #") + " "), c);
        HTTPProxyPortLabel.setForeground(Color.gray);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        opts.add(HTTPProxyPort = new NumberField(4), c);
        HTTPProxyPort.setEnabled(false);
        HTTPProxyBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (proxySupport.equals(event.getActionCommand())) {
                    boolean b = HTTPProxyBox.isSelected();
                    Color color = b
                            ? Color.black
                            : Color.gray;
                    HTTPProxyHost.setEnabled(b);
                    HTTPProxyPort.setEnabled(b);
                    HTTPProxyHostLabel.setForeground(color);
                    HTTPProxyPortLabel.setForeground(color);
                }
            }
        });

        // Set default proxy values...
        String tmp = System.getProperty("http.proxyHost");
        if ((tmp != null) && tmp.equals("")) {
            tmp = null;
        }
        HTTPProxyBox.setSelected(tmp != null);
        HTTPProxyHost.setEnabled(tmp != null);
        HTTPProxyPort.setEnabled(tmp != null);
        HTTPProxyHostLabel.setForeground((tmp != null)
                ? Color.black
                : Color.gray);
        HTTPProxyPortLabel.setForeground((tmp != null)
                ? Color.black
                : Color.gray);
        if (tmp != null) {
            HTTPProxyBox.setSelected(true);
            HTTPProxyHost.setText(tmp);
            tmp = System.getProperty("http.proxyPort");
            if ((tmp != null) && tmp.equals("")) {
                tmp = null;
            }
            if (tmp == null) {
                tmp = "80";
            }
            HTTPProxyPort.setText(tmp);
        }

        // add byte delay fields
        opts.add(Box.createRigidArea(new Dimension(1, 10)), c);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        final String delaySupport = TCPMonBundle.getMessage("delay00",
                "Simulate Slow Connection");
        opts.add(delayBox = new JCheckBox(delaySupport), c);

        // bytes per pause
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        delayBytesLabel = new JLabel(TCPMonBundle.getMessage("delay01",
                "Bytes per Pause"));
        opts.add(delayBytesLabel, c);
        delayBytesLabel.setForeground(Color.gray);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        opts.add(delayBytes = new NumberField(6), c);
        delayBytes.setEnabled(false);

        // delay interval
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        delayTimeLabel = new JLabel(TCPMonBundle.getMessage("delay02",
                "Delay in Milliseconds"));
        opts.add(delayTimeLabel, c);
        delayTimeLabel.setForeground(Color.gray);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        opts.add(delayTime = new NumberField(6), c);
        delayTime.setEnabled(false);

        // enabler callback
        delayBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (delaySupport.equals(event.getActionCommand())) {
                    boolean b = delayBox.isSelected();
                    Color color = b
                            ? Color.black
                            : Color.gray;
                    delayBytes.setEnabled(b);
                    delayTime.setEnabled(b);
                    delayBytesLabel.setForeground(color);
                    delayTimeLabel.setForeground(color);
                }
            }
        });
        
        // SSL options
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
//        opts.add(incomingSSLBox = new JCheckBox("Use SSL for incoming connections"), c); // TODO: i18n
        opts.add(outgoingSSLBox = new JCheckBox("Use SSL for outgoing connections"), c); // TODO: i18n
        
        // Replace URIs options
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        opts.add(replaceURIsInContentBox = new JCheckBox("Replace URIs in request/response content"), c); // TODO: i18n
        
        // Spacer
        // ////////////////////////////////////////////////////////////////
        mainPane.add(Box.createRigidArea(new Dimension(1, 10)), c);

        // ADD Button
        // /////////////////////////////////////////////////////////////////
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        final String add = TCPMonBundle.getMessage("add00", "Add");
        mainPane.add(addButton = new JButton(add), c);
        this.add(new JScrollPane(mainPane), BorderLayout.CENTER);

        // addButton.setEnabled( false );
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (add.equals(event.getActionCommand())) {
                    InterceptorConfiguration config = getConfiguration();
                    if (config == null) {

                        // no port, button does nothing
                        return;
                    }
                    try {
                        new Listener(noteb, null, config);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // reset the port
                    port.setText(null);
                }
            }
        });
        notebook.addTab(name, this);
        
        new Sender(noteb);
        notebook.repaint();
        notebook.setSelectedIndex(notebook.getTabCount() - 1);
    }
    
    private InterceptorConfiguration getConfiguration() {
        InterceptorConfigurationBuilder configBuilder = new InterceptorConfigurationBuilder();
        
        int lPort = port.getValue(0);
        if (lPort == 0) {
            // no port, no configuration
            return null;
        }
        configBuilder.setListenPort(lPort);
        configBuilder.setProxy(proxyButton.isSelected());
        configBuilder.setTargetHost(host.getText());
        configBuilder.setTargetPort(tport.getValue(0));
        if (delayBox.isSelected()) {
            ThrottleConfiguration throttleConfig = new ThrottleConfiguration(delayBytes.getValue(0), delayTime.getValue(0));
            configBuilder.addRequestFilter(throttleConfig);
            configBuilder.addResponseFilter(throttleConfig);
        }

        // Pick-up the HTTP Proxy settings
        // /////////////////////////////////////////////////
        String text = HTTPProxyHost.getText();
        if (text.length() > 0) {
            configBuilder.setHttpProxyHost(text);
        }
        text = HTTPProxyPort.getText();
        int proxyPort = HTTPProxyPort.getValue(-1);
        if (proxyPort != -1) {
            configBuilder.setHttpProxyPort(Integer.parseInt(text));
        }
        
        if (outgoingSSLBox.isSelected()) {
            try {
                configBuilder.configureSSLSocketFactory(false);
            } catch (GeneralSecurityException ex) {
                throw new Error(ex);
            }
        }
        
        configBuilder.setReplaceURIsInContent(replaceURIsInContentBox.isSelected());
        
        return configBuilder.build();
    }
    
    /**
     * a text field with a restricted set of characters
     */
    static class RestrictedTextField extends JTextField {
        /**
         * Field validText
         */
        protected String validText;

        /**
         * Constructor RestrictedTextField
         *
         * @param validText
         */
        public RestrictedTextField(String validText) {
            setValidText(validText);
        }

        /**
         * Constructor RestrictedTextField
         *
         * @param columns
         * @param validText
         */
        public RestrictedTextField(int columns, String validText) {
            super(columns);
            setValidText(validText);
        }

        /**
         * Constructor RestrictedTextField
         *
         * @param text
         * @param validText
         */
        public RestrictedTextField(String text, String validText) {
            super(text);
            setValidText(validText);
        }

        /**
         * Constructor RestrictedTextField
         *
         * @param text
         * @param columns
         * @param validText
         */
        public RestrictedTextField(String text, int columns, String validText) {
            super(text, columns);
            setValidText(validText);
        }

        /**
         * Method setValidText
         *
         * @param validText
         */
        private void setValidText(String validText) {
            this.validText = validText;
        }

        /**
         * fascinatingly, this method is called in the super() constructor,
         * meaning before we are fully initialized. C++ doesnt actually permit
         * such a situation, but java clearly does...
         *
         * @return a new document
         */
        public Document createDefaultModel() {
            return new RestrictedDocument();
        }

        /**
         * this class strips out invaid chars
         */
        class RestrictedDocument extends PlainDocument {
            /**
             * Constructs a plain text document.  A default model using
             * <code>GapContent</code> is constructed and set.
             */
            public RestrictedDocument() {
            }

            /**
             * add a string; only those chars in the valid text list are allowed
             *
             * @param offset
             * @param string
             * @param attributes
             * @throws BadLocationException
             */
            public void insertString(int offset,
                                     String string,
                                     AttributeSet attributes)
                    throws BadLocationException {
                if (string == null) {
                    return;
                }
                int len = string.length();
                StringBuffer buffer = new StringBuffer(string.length());
                for (int i = 0; i < len; i++) {
                    char ch = string.charAt(i);
                    if (validText.indexOf(ch) >= 0) {
                        buffer.append(ch);
                    }
                }
                super.insertString(offset, new String(buffer), attributes);
            }
        }    // end class NumericDocument
    }

    /**
     * because we cant use Java1.4's JFormattedTextField, here is
     * a class that accepts numbers only
     */
    static class NumberField extends RestrictedTextField {
        /**
         * Field VALID_TEXT
         */
        private static final String VALID_TEXT = "0123456789";

        /**
         * Constructs a new <code>TextField</code>.  A default model is created,
         * the initial string is <code>null</code>,
         * and the number of columns is set to 0.
         */
        public NumberField() {
            super(VALID_TEXT);
        }

        /**
         * Constructs a new empty <code>TextField</code> with the specified
         * number of columns.
         * A default model is created and the initial string is set to
         * <code>null</code>.
         *
         * @param columns the number of columns to use to calculate
         *                the preferred width; if columns is set to zero, the
         *                preferred width will be whatever naturally results from
         *                the component implementation
         */
        public NumberField(int columns) {
            super(columns, VALID_TEXT);
        }

        /**
         * get the int value of a field, any invalid (non int) field returns
         * the default
         *
         * @param def default value
         * @return the field contents
         */
        public int getValue(int def) {
            int result = def;
            String text = getText();
            if ((text != null) && (text.length() != 0)) {
                try {
                    result = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                }
            }
            return result;
        }

        /**
         * set the text to a numeric value
         *
         * @param value number to assign
         */
        public void setValue(int value) {
            setText(Integer.toString(value));
        }
    }    // end class NumericTextField

    /**
     * hostname fields
     */
    static class HostnameField extends RestrictedTextField {

        // list of valid chars in a hostname

        /**
         * Field VALID_TEXT
         */
        private static final String VALID_TEXT =
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWZYZ-.";

        /**
         * Constructor HostnameField
         *
         * @param columns
         */
        public HostnameField(int columns) {
            super(columns, VALID_TEXT);
        }

        /**
         * Constructor HostnameField
         */
        public HostnameField() {
            super(VALID_TEXT);
        }
    }    
}

