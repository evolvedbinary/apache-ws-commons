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

package org.apache.ws.commons.tcpmon.core.engine;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

/**
 * wait for incoming connections, spawn a connection thread when
 * stuff comes in.
 */
public class Interceptor extends Thread {

    /**
     * Field sSocket
     */
    ServerSocket sSocket = null;

    private final InterceptorConfiguration config;
    private final InterceptorListener listener;
    
    /**
     * Field pleaseStop
     */
    boolean pleaseStop = false;

    private final Vector connections = new Vector();

    /**
     * Constructor.
     * 
     * @param config the interceptor configuration
     * @param listener object listening for events from the interceptor; may be <code>null</code>
     */
    public Interceptor(InterceptorConfiguration config, InterceptorListener listener) {
        this.config = config;
        this.listener = listener;
        start();
    }

    /**
     * Method run
     */
    public void run() {
        try {
            if (listener != null) {
                listener.onServerSocketStart();
            }
            sSocket = config.getServerSocketFactory().createServerSocket(config.getListenPort());
            for (; ;) {
                Socket inSocket = sSocket.accept();
                if (pleaseStop) {
                    break;
                }
                Connection connection = new Connection(config, listener, inSocket);
                // TODO: at some point we need to remove closed connections,
                //       otherwise this will be a memory leak.
                connections.add(connection);
                connection.start();
                inSocket = null;
            }
        } catch (Exception exp) {
            if (listener != null && !"socket closed".equals(exp.getMessage())) {
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
            new Socket("127.0.0.1", config.getListenPort());
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
