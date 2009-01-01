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

package org.apache.ws.commons.tcpmon;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import org.apache.ws.commons.tcpmon.core.AbstractConnection;
import org.apache.ws.commons.tcpmon.core.AbstractSocketRR;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * a connection listens to a single current connection
 */
class Connection extends AbstractConnection {
    /**
     * Field listener
     */
    Listener listener;

    /**
     * Field inputText
     */
    JTextArea inputText = null;

    /**
     * Field inputScroll
     */
    JScrollPane inputScroll = null;

    /**
     * Field outputText
     */
    JTextArea outputText = null;

    /**
     * Field outputScroll
     */
    JScrollPane outputScroll = null;

    /**
     * Constructor Connection
     *
     * @param l
     * @param s
     */
    public Connection(Listener l, Socket s) {
        super(l.getConfiguration(), s);
        listener = l;
        start();
    }

    /**
     * Constructor Connection
     *
     * @param l
     * @param in
     */
    public Connection(Listener l, InputStream in) {
        super(l.getConfiguration(), in);
        listener = l;
        start();
    }

    /**
     * Method remove
     */
    public void remove() {
        int index = -1;
        try {
            halt();
            index = listener.connections.indexOf(this);
            listener.tableModel.removeRow(index + 1);
            listener.connections.remove(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected void init(String time, String fromHost, String targetHost) {
        int count = listener.connections.size();
        listener.tableModel.insertRow(count + 1,
                new Object[]{
                    TCPMonBundle.getMessage("active00","Active"),
                    time,
                    fromHost,
                    targetHost,
                    ""});
        listener.connections.add(this);
        inputText = new JTextArea(null, null, 20, 80);
        inputScroll = new JScrollPane(inputText);
        outputText = new JTextArea(null, null, 20, 80);
        outputScroll = new JScrollPane(outputText);
        ListSelectionModel lsm = listener.connectionTable.getSelectionModel();
        if ((count == 0) || (lsm.getLeadSelectionIndex() == 0)) {
            listener.outPane.setVisible(false);
            int divLoc = listener.outPane.getDividerLocation();
            listener.setLeft(inputScroll);
            listener.setRight(outputScroll);
            listener.removeButton.setEnabled(false);
            listener.removeAllButton.setEnabled(true);
            listener.saveButton.setEnabled(true);
            listener.resendButton.setEnabled(true);
            listener.outPane.setDividerLocation(divLoc);
            listener.outPane.setVisible(true);
        }
        inputWriter = new JTextAreaWriter(inputText);
        outputWriter = new JTextAreaWriter(outputText);
    }
    
    protected AbstractSocketRR createInputSocketRR(Socket inSocket, InputStream inputStream,
            Socket outSocket, OutputStream outputStream, boolean format,
            SlowLinkSimulator slowLink) {
        return new SocketRR(this, inSocket, inputStream, outSocket, outputStream,
                format, listener.tableModel,
                listener.connections.indexOf(this) + 1, slowLink, inputWriter);
    }

    protected AbstractSocketRR createOutputSocketRR(Socket outSocket, InputStream inputStream,
            Socket inSocket, OutputStream outputStream, boolean format,
            SlowLinkSimulator slowLink) {
        return new SocketRR(this, outSocket, inputStream, inSocket, outputStream,
                format, null, 0, slowLink, outputWriter);
    }

    private void setValue(int column, String value) {
        int index = listener.connections.indexOf(this);
        if (index >= 0) {
            listener.tableModel.setValueAt(value, 1 + index, column);
        }
    }
    
    protected void setOutHost(String outHost) {
        setValue(TCPMon.OUTHOST_COLUMN, outHost);
    }
    
    protected void setState(String state) {
        setValue(TCPMon.STATE_COLUMN, state);
    }
    
    protected void setRequest(String request) {
        setValue(TCPMon.REQ_COLUMN, request);
    }
    
    protected void setElapsed(String elapsed) {
        setValue(TCPMon.ELAPSED_COLUMN, elapsed);
    }
}
