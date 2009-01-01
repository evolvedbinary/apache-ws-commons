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

import javax.swing.table.TableModel;

import org.apache.ws.commons.tcpmon.core.AbstractSocketRR;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.Socket;

/**
 * this class handles the pumping of data from the incoming socket to the
 * outgoing socket
 */
class SocketRR extends AbstractSocketRR {
    /**
     * Field tmodel
     */
    TableModel tmodel = null;

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
                    boolean format, TableModel tModel, int index,
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
        return (String) tmodel.getValueAt(tableIndex, TCPMon.REQ_COLUMN);
    }
    
    protected void setSavedFirstLine(String value) {
        tmodel.setValueAt(value, tableIndex, TCPMon.REQ_COLUMN);
    }
}
