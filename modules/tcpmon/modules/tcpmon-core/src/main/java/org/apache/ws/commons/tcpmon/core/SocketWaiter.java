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

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

import javax.net.ServerSocketFactory;

/**
 * wait for incoming connections, spawn a connection thread when
 * stuff comes in.
 */
public class SocketWaiter extends Thread {

    /**
     * Field sSocket
     */
    ServerSocket sSocket = null;

    /**
     * Field listener
     */
    AbstractListener listener;

    private final ServerSocketFactory serverSocketFactory;
    
    /**
     * Field port
     */
    int port;

    /**
     * Field pleaseStop
     */
    boolean pleaseStop = false;

    private final Vector connections = new Vector();

    /**
     * Constructor SocketWaiter
     *
     * @param l
     * @param p
     */
    public SocketWaiter(AbstractListener l, ServerSocketFactory serverSocketFactory, int p) {
        listener = l;
        this.serverSocketFactory = serverSocketFactory;
        port = p;
        start();
    }

    /**
     * Method run
     */
    public void run() {
        try {
            listener.onServerSocketStart();
            sSocket = serverSocketFactory.createServerSocket(port);
            for (; ;) {
                Socket inSocket = sSocket.accept();
                if (pleaseStop) {
                    break;
                }
                Connection connection = new Connection(listener, inSocket);
                // TODO: at some point we need to remove closed connections,
                //       otherwise this will be a memory leak.
                connections.add(connection);
                connection.start();
                inSocket = null;
            }
        } catch (Exception exp) {
            if (!"socket closed".equals(exp.getMessage())) {
                listener.onServerSocketError(exp);
            }
        }
    }

    /**
     * force a halt by connecting to self and then closing the server socket
     */
    public void halt() {
        try {
            pleaseStop = true;
            new Socket("127.0.0.1", port);
            if (sSocket != null) {
                sSocket.close();
            }
            for (Iterator it = connections.iterator(); it.hasNext(); ) {
                ((Connection)it.next()).halt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
