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

import java.io.Writer;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.ws.commons.tcpmon.TCPMonBundle;
import org.apache.ws.commons.tcpmon.core.ui.AbstractRequestResponse;

public class RequestResponse extends AbstractRequestResponse {
    private final Listener listener;
    
    /**
     * Field inputText
     */
    private JTextArea inputText;

    /**
     * Field inputScroll
     */
    JScrollPane inputScroll = null;

    /**
     * Field outputText
     */
    private JTextArea outputText;

    /**
     * Field outputScroll
     */
    JScrollPane outputScroll = null;
    
    public RequestResponse(Listener listener, String fromHost) {
        super(listener.getConfiguration());
        this.listener = listener;
        int count = listener.requestResponses.size();
        listener.tableModel.insertRow(count + 1,
                new Object[]{
                    TCPMonBundle.getMessage("active00","Active"),
                    getTime(),
                    fromHost,
                    "",
                    ""});
        listener.requestResponses.add(this);
        inputText = new JTextArea(null, null, 20, 80);
        inputScroll = new JScrollPane(inputText);
        outputText = new JTextArea(null, null, 20, 80);
        outputScroll = new JScrollPane(outputText);
        listener.handleSelection();
    }

    /**
     * Method remove
     */
    public void remove() {
        int index = -1;
        try {
            index = listener.requestResponses.indexOf(this);
            listener.tableModel.removeRow(index + 1);
            listener.requestResponses.remove(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setValue(int column, String value) {
        int index = listener.requestResponses.indexOf(this);
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
    
    public void setElapsed(long elapsed) {
        setValue(TCPMon.ELAPSED_COLUMN, String.valueOf(elapsed));
    }
    
    protected Writer getRequestWriter() {
        return new JTextAreaWriter(inputText);
    }

    protected Writer getResponseWriter() {
        return new JTextAreaWriter(outputText);
    }

    public String getRequestAsString() {
        return inputText.getText();
    }

    public String getResponseAsString() {
        return outputText.getText();
    }
}
