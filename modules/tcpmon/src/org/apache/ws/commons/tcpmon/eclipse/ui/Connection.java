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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.apache.ws.commons.tcpmon.SlowLinkSimulator;
import org.apache.ws.commons.tcpmon.TCPMonBundle;
import org.apache.ws.commons.tcpmon.core.AbstractConnection;
import org.apache.ws.commons.tcpmon.core.AbstractSocketRR;

/**
 * a connection listens to a single current connection
 */
class Connection extends AbstractConnection {
    public TableItem item;

    /**
     * Field listener
     */
    Listener listener;

    /**
     * Field inputText
     */
    Text inputText = null;

    /**
     * Field outputText
     */
    Text outputText = null;

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

    protected void init(final String time, final String fromHost, final String targetHost) {
        final int count = listener.connections.size();

        MainView.display.syncExec(new Runnable() {
            public void run() {
                item = new TableItem(listener.connectionTable, SWT.BORDER, count + 1);
                item.setText(new String[]{TCPMonBundle.getMessage("active00", "Active"),
                        time,
                        fromHost,
                        targetHost,
                        "", ""});
                listener.tableEnhancer.setSelectionInterval(0, 0);
            }
        });


        listener.connections.add(this);
        TableEnhancer te = listener.tableEnhancer;
        if ((count == 0) || (te.getLeadSelectionIndex() == 0)) {

            MainView.display.syncExec(new Runnable() {
                public void run() {
                    inputText = (Text) listener.setLeft(MainView.SWT_TEXT, "");
                    outputText = (Text) listener.setRight(MainView.SWT_TEXT, "");
                    listener.removeButton.setEnabled(false);
                    listener.removeAllButton.setEnabled(true);
                    listener.saveButton.setEnabled(true);
                    listener.resendButton.setEnabled(true);
                }
            });

        }
    }

    protected AbstractSocketRR createInputSocketRR(Socket inSocket, InputStream inputStream,
            Socket outSocket, OutputStream outputStream, boolean format,
            SlowLinkSimulator slowLink) {
        return new SocketRR(this, inSocket, inputStream, outSocket, outputStream,
                inputText, format, listener.connectionTable,
                listener.connections.indexOf(this) + 1, "request:", slowLink);
    }

    protected AbstractSocketRR createOutputSocketRR(Socket outSocket, InputStream inputStream,
            Socket inSocket, OutputStream outputStream, boolean format,
            SlowLinkSimulator slowLink) {
        return new SocketRR(this, outSocket, inputStream, inSocket, outputStream,
                outputText, format, null, 0, "response:",
                slowLink);
    }

    protected void appendInputText(final String data) {
        MainView.display.syncExec(new Runnable() {
            public void run() {
                inputText.append(data);
            }
        });
    }
    
    protected void appendOutputText(final String data) {
        MainView.display.syncExec(new Runnable() {
            public void run() {
                if (outputText != null) {
                    outputText.append(data);
                } else {
                    // something went wrong before we had the output area
                    System.out.println(data);
                }
            }
        });
    }
    
    private void setValue(final int column, final String value) {
        final int index = listener.connections.indexOf(this);
        if (index >= 0) {
            MainView.display.syncExec(new Runnable() {
                public void run() {
                    listener.tableEnhancer.setValueAt(value, 1 + index, column);
                }
            });
        }
    }
    
    protected void setOutHost(String outHost) {
        setValue(MainView.OUTHOST_COLUMN, outHost);
    }
    
    protected void setState(String state) {
        setValue(MainView.STATE_COLUMN, state);
    }

    protected void setRequest(String request) {
        setValue(MainView.REQ_COLUMN, request);
    }
    
    protected void setElapsed(String elapsed) {
        setValue(MainView.ELAPSED_COLUMN, elapsed);
    }
}
