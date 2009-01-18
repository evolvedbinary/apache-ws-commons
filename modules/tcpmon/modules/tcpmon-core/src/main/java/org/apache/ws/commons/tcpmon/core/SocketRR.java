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

package org.apache.ws.commons.tcpmon.core;

import org.apache.ws.commons.tcpmon.core.filter.Pipeline;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * this class handles the pumping of data from the incoming socket to the
 * outgoing socket
 */
public class SocketRR extends Thread {
    private final AbstractConnection connection;

    /**
     * Field inSocket
     */
    Socket inSocket = null;

    /**
     * Field outSocket
     */
    Socket outSocket = null;

    /**
     * Field in
     */
    InputStream in = null;

    /**
     * Field out
     */
    OutputStream out = null;

    /**
     * Field done
     */
    volatile boolean done = false;

    /**
     * Field tmodel
     */
    volatile long elapsed = 0;
    
    private final Pipeline pipeline;
    
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
    public SocketRR(AbstractConnection connection, Socket inputSocket,
                    InputStream inputStream, Socket outputSocket,
                    OutputStream outputStream, Pipeline pipeline) {
        this.connection = connection;
        inSocket = inputSocket;
        in = inputStream;
        outSocket = outputSocket;
        out = outputStream;
        this.pipeline = pipeline;
    }

    /**
     * Method isDone
     *
     * @return boolean
     */
    public boolean isDone() {
        return done;
    }

    public String getElapsed() {
        return String.valueOf(elapsed);
    }
    
    /**
     * Method run
     */
    public void run() {
        try {
            long start = System.currentTimeMillis();
            int c;
            do {
                try {
                    c = pipeline.readFrom(in);
                } catch (IOException ex) {
                    // When reading from the socket, consider an I/O exception (such as connection
                    // reset) as the end of stream and silently discard the exception.
                    c = -1;
                }
                elapsed = System.currentTimeMillis() - start;
            } while (c != -1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            done = true;
            try {
                if (out != null) {
                    out.flush();
                    if (null != outSocket) {
                        outSocket.shutdownOutput();
                    } else {
                        out.close();
                    }
                    out = null;
                }
            } catch (Exception e) {
            }
            try {
                if (in != null) {
                    if (inSocket != null) {
                        inSocket.shutdownInput();
                    } else {
                        in.close();
                    }
                    in = null;
                }
            } catch (Exception e) {
            }
            connection.wakeUp();
        }
    }

    /**
     * Method halt
     */
    public void halt() {
        try {
            if (inSocket != null) {
                inSocket.close();
            }
            if (outSocket != null) {
                outSocket.close();
            }
            inSocket = null;
            outSocket = null;
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            in = null;
            out = null;
            done = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
