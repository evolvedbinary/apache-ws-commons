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
import java.io.Writer;
import java.net.Socket;

import org.eclipse.swt.widgets.Table;
import org.apache.ws.commons.tcpmon.SlowLinkSimulator;
import org.apache.ws.commons.tcpmon.core.AbstractSocketRR;

/**
 * this class handles the pumping of data from the incoming socket to the
 * outgoing socket   Same as the swing one except for the use of SWT components
 */
class SocketRR extends AbstractSocketRR {
    /**
     * Field tmodel
     */
    Table tmodel = null;

    /**
     * Field tableIndex
     */
    int tableIndex = 0;

    /**
     * Constructor SocketRR
     *
     * @param c
     * @param inputSocket
     * @param inputStream
     * @param outputSocket
     * @param outputStream
     * @param _textArea
     * @param format
     * @param tModel
     * @param index
     * @param type
     * @param slowLink
     */
    public SocketRR(Connection c, Socket inputSocket,
                    InputStream inputStream, Socket outputSocket,
                    OutputStream outputStream,
                    boolean format, Table tModel, int index,
                    SlowLinkSimulator slowLink, Writer writer) {
        super(c, inputSocket, inputStream, outputSocket, outputStream, format, slowLink, writer);
        tmodel = tModel;
        tableIndex = index;
        start();
    }

    protected boolean isSaveFirstLine() {
        return tmodel != null;
    }
    
    protected String getSavedFirstLine() {
        final String[] result = new String[1];
        MainView.display.syncExec(new Runnable() {
            public void run() {
                result[0] = tmodel.getItem(tableIndex).getText(MainView.REQ_COLUMN);
            }
        });
        return result[0];
    }
    
    protected void setSavedFirstLine(final String value) {
        MainView.display.syncExec(new Runnable() {
            public void run() {
                tmodel.getItem(tableIndex).setText(MainView.REQ_COLUMN, value);
            }
        });
    }
}
