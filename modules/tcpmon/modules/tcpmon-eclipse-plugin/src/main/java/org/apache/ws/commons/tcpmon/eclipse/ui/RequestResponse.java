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

import java.io.Writer;

import org.apache.ws.commons.tcpmon.TCPMonBundle;
import org.apache.ws.commons.tcpmon.core.ui.AbstractRequestResponse;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class RequestResponse extends AbstractRequestResponse {
    private final Listener listener;

    /**
     * Field inputText
     */
    Text inputText = null;

    /**
     * Field outputText
     */
    Text outputText = null;

    public RequestResponse(final Listener listener, final String fromHost) {
        super(listener.getConfiguration());
        this.listener = listener;
        final int count = listener.requestResponses.size();

        MainView.display.syncExec(new Runnable() {
            public void run() {
                TableItem item = new TableItem(listener.connectionTable, SWT.BORDER, count + 1);
                item.setText(new String[]{TCPMonBundle.getMessage("active00", "Active"),
                        getTime(),
                        fromHost,
                        "",
                        "", ""});
                listener.tableEnhancer.setSelectionInterval(0, 0);
            }
        });


        listener.requestResponses.add(this);
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

    private void setValue(final int column, final String value) {
        final int index = listener.requestResponses.indexOf(this);
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
    
    public void setElapsed(long elapsed) {
        setValue(MainView.ELAPSED_COLUMN, String.valueOf(elapsed));
    }

    protected Writer getRequestWriter() {
        return new TextWidgetWriter(inputText);
    }

    protected Writer getResponseWriter() {
        return new TextWidgetWriter(outputText);
    }

    public String getRequestAsString() {
        return inputText.getText();
    }

    public String getResponseAsString() {
        return outputText.getText();
    }
}
