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
import org.apache.ws.commons.tcpmon.core.filter.StreamException;
import org.apache.ws.commons.tcpmon.core.filter.Tee;
import org.apache.ws.commons.tcpmon.core.filter.http.HttpHeaderRewriter;
import org.apache.ws.commons.tcpmon.core.filter.http.HttpProxyClientHandler;
import org.apache.ws.commons.tcpmon.core.filter.http.HttpProxyServerHandler;
import org.apache.ws.commons.tcpmon.core.filter.http.HttpRequestFilter;
import org.apache.ws.commons.tcpmon.core.filter.throttle.Throttle;
import org.apache.ws.commons.tcpmon.core.filter.throttle.ThrottleConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.net.SocketFactory;

/**
 * a connection listens to a single current connection
 */
public class Connection extends Thread {
    private final AbstractListener listener;
    private final Configuration config;

    /**
     * Field active
     */
    private boolean active;

    /**
     * Field inSocket
     */
    private Socket inSocket = null;

    /**
     * Field outSocket
     */
    private Socket outSocket = null;

    /**
     * Field rr1
     */
    private SocketRR rr1 = null;

    /**
     * Field rr2
     */
    private SocketRR rr2 = null;

    private IRequestResponse requestResponse;

    /**
     * Constructor Connection
     *
     * @param listener
     * @param s
     */
    public Connection(AbstractListener listener, Socket s) {
        this.listener = listener;
        config = listener.getConfiguration();
        inSocket = s;
    }

    /**
     * Method run
     */
    public void run() {
        try {
            active = true;
            String HTTPProxyHost = config.getHttpProxyHost();
            int HTTPProxyPort = config.getHttpProxyPort();
            ThrottleConfiguration throttleConfig = config.getThrottleConfiguration();
            final SocketFactory socketFactory = config.getSocketFactory();
            String targetHost = config.getTargetHost();
            requestResponse = listener.createRequestResponse(inSocket.getInetAddress().getHostName());
            int targetPort = config.getTargetPort();
            InputStream tmpIn1 = inSocket.getInputStream();
            OutputStream tmpOut1 = inSocket.getOutputStream();
            
            Pipeline requestPipeline = new Pipeline();
            HttpRequestFilter requestFilter = new HttpRequestFilter(false);
            requestPipeline.addFilter(requestFilter);
            if (config.isProxy()) {
                requestFilter.addHandler(new HttpProxyServerHandler() {
                    protected void handleConnection(String host, int port) {
                        requestResponse.setTarget(host, port);
                        try {
                            outSocket = socketFactory.createSocket(host, port);
                        } catch (IOException ex) {
                            throw new StreamException(ex);
                        }
                    }
                });
            } else {
                requestResponse.setTarget(targetHost, targetPort);
                requestFilter.addHandler(new HttpHeaderRewriter("Host", targetHost + ":" + targetPort));
                outSocket = socketFactory.createSocket(targetHost, targetPort);
            }
            // We log the request data at this stage. This means that the user will see the request
            // as if it had been sent directly from the client to the server (without TCPMon or a proxy
            // in between).
            OutputStream requestOutputStream = requestResponse.getRequestOutputStream();
            if (requestOutputStream != null) {
                requestPipeline.addFilter(new Tee(requestOutputStream));
            }
            if (HTTPProxyHost != null) {
                requestFilter.addHandler(new HttpProxyClientHandler(targetHost, targetPort));
                outSocket = socketFactory.createSocket(HTTPProxyHost, HTTPProxyPort);
            }
            if (throttleConfig != null) {
                requestPipeline.addFilter(new Throttle(throttleConfig));
            }
            Tee requestTee = new Tee();
            requestPipeline.addFilter(requestTee);
            
            requestResponse.setState(IRequestResponse.STATE_ACTIVE);
            
            // If we act as a proxy, we first need to read the start of the request before
            // the outSocket is available.
            while (outSocket == null) {
                requestPipeline.readFrom(tmpIn1);
            }
            
            OutputStream tmpOut2 = outSocket.getOutputStream();
            requestTee.setOutputStream(tmpOut2);
            
            Pipeline responsePipeline = new Pipeline();
            if (throttleConfig != null) {
                responsePipeline.addFilter(new Throttle(throttleConfig));
            }
            if (tmpOut1 != null) {
                responsePipeline.addFilter(new Tee(tmpOut1));
            }
            OutputStream responseOutputStream = requestResponse.getResponseOutputStream();
            if (responseOutputStream != null) {
                responsePipeline.addFilter(new Tee(responseOutputStream));
            }
            
            // this is the channel to the endpoint
            rr1 = new SocketRR(this, inSocket, tmpIn1, outSocket, tmpOut2, requestPipeline);

            // this is the channel from the endpoint
            rr2 = new SocketRR(this, outSocket, outSocket.getInputStream(), inSocket, tmpOut1, responsePipeline);
            
            rr1.start();
            rr2.start();
            
            while ((rr1 != null) || (rr2 != null)) {

                if (rr2 != null) {
                    requestResponse.setElapsed(rr2.getElapsed());
                }
                
                // Only loop as long as the connection to the target
                // machine is available - once that's gone we can stop.
                // The old way, loop until both are closed, left us
                // looping forever since no one closed the 1st one.
                
                if ((null != rr1) && rr1.isDone()) {
                    if (rr2 != null) {
                        requestResponse.setState(IRequestResponse.STATE_RESP);
                    }
                    rr1 = null;
                }

                if ((null != rr2) && rr2.isDone()) {
                    if (rr1 != null) {
                        requestResponse.setState(IRequestResponse.STATE_REQ);
                    }
                    rr2 = null;
                }

                synchronized (this) {
                    this.wait(100);    // Safety just incase we're not told to wake up.
                }
            }

            active = false;

            requestResponse.setState(IRequestResponse.STATE_DONE);

        } catch (Exception e) {
            if (requestResponse != null) {
                requestResponse.setState(IRequestResponse.STATE_ERROR);
                requestResponse.onError(e);
            }
            halt();
        }
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Method wakeUp
     */
    public synchronized void wakeUp() {
        this.notifyAll();
    }

    /**
     * Method halt
     */
    public void halt() {
        try {
            if (rr1 != null) {
                rr1.halt();
            }
            if (rr2 != null) {
                rr2.halt();
            }
            if (inSocket != null) {
                inSocket.close();
            }
            inSocket = null;
            if (outSocket != null) {
                outSocket.close();
            }
            outSocket = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
