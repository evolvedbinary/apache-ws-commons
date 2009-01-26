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

import org.apache.ws.commons.tcpmon.SlowLinkSimulator;
import org.apache.ws.commons.tcpmon.TCPMonBundle;
import org.apache.ws.commons.tcpmon.core.filter.CharsetDecoderFilter;
import org.apache.ws.commons.tcpmon.core.filter.Pipeline;
import org.apache.ws.commons.tcpmon.core.filter.RequestLineExtractor;
import org.apache.ws.commons.tcpmon.core.filter.StreamException;
import org.apache.ws.commons.tcpmon.core.filter.Tee;
import org.apache.ws.commons.tcpmon.core.filter.http.HttpHeaderRewriter;
import org.apache.ws.commons.tcpmon.core.filter.http.HttpProxyClientHandler;
import org.apache.ws.commons.tcpmon.core.filter.http.HttpProxyServerHandler;
import org.apache.ws.commons.tcpmon.core.filter.http.HttpRequestFilter;
import org.apache.ws.commons.tcpmon.core.filter.http.HttpResponseFilter;
import org.apache.ws.commons.tcpmon.core.filter.mime.DefaultContentFilterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * a connection listens to a single current connection
 */
public abstract class AbstractConnection extends Thread {
    private static final Charset UTF8 = Charset.forName("utf-8");
    
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

    /**
     * Field inputStream
     */
    private InputStream inputStream = null;

    protected Writer inputWriter;
    protected Writer outputWriter;

    /**
     * Constructor Connection
     *
     * @param config
     * @param s
     */
    public AbstractConnection(Configuration config, Socket s) {
        this.config = config;
        inSocket = s;
    }

    /**
     * Constructor Connection
     *
     * @param config
     * @param in
     */
    public AbstractConnection(Configuration config, InputStream in) {
        this.config = config;
        inputStream = in;
    }

    /**
     * Method run
     */
    public void run() {
        try {
            active = true;
            String HTTPProxyHost = config.getHttpProxyHost();
            int HTTPProxyPort = config.getHttpProxyPort();
            if (HTTPProxyHost == null) {
                HTTPProxyHost = System.getProperty("http.proxyHost");
                if ((HTTPProxyHost != null) && HTTPProxyHost.equals("")) {
                    HTTPProxyHost = null;
                }
                if (HTTPProxyHost != null) {
                    String tmp = System.getProperty("http.proxyPort");
                    if ((tmp != null) && tmp.equals("")) {
                        tmp = null;
                    }
                    if (tmp == null) {
                        HTTPProxyPort = 80;
                    } else {
                        HTTPProxyPort = Integer.parseInt(tmp);
                    }
                }
            }
            String fromHost;
            if (inSocket != null) {
                fromHost = (inSocket.getInetAddress()).getHostName();
            } else {
                fromHost = "resend";
            }
            String dateformat = TCPMonBundle.getMessage("dateformat00", "yyyy-MM-dd HH:mm:ss");
            DateFormat df = new SimpleDateFormat(dateformat);
            String targetHost = config.getTargetHost();
            init(df.format(new Date()), fromHost, targetHost);
            int targetPort = config.getTargetPort();
            InputStream tmpIn1 = inputStream;
            OutputStream tmpOut1 = null;
            OutputStream tmpOut2 = null;
            if (tmpIn1 == null) {
                tmpIn1 = inSocket.getInputStream();
            }
            if (inSocket != null) {
                tmpOut1 = inSocket.getOutputStream();
            }
            
            Pipeline requestPipeline = new Pipeline();
            requestPipeline.addFilter(new RequestLineExtractor(50) {
                protected void done(String requestLine) {
                    setRequest(requestLine);
                }
            });
            HttpRequestFilter requestFilter = new HttpRequestFilter(false);
            requestPipeline.addFilter(requestFilter);
            if (config.isProxy()) {
                requestFilter.addHandler(new HttpProxyServerHandler() {
                    protected void handleConnection(String host, int port) {
                        try {
                            outSocket = new Socket(host, port);
                        } catch (IOException ex) {
                            throw new StreamException(ex);
                        }
                    }
                });
            } else if (HTTPProxyHost != null) {
                requestFilter.addHandler(new HttpProxyClientHandler(targetHost, targetPort));
                outSocket = new Socket(HTTPProxyHost, HTTPProxyPort);
            } else {
                requestFilter.addHandler(new HttpHeaderRewriter("Host", targetHost + ":" + targetPort));
                outSocket = new Socket(targetHost, targetPort);
            }
            requestPipeline.addFilter(config.getSlowLink());
            Tee requestTee = new Tee();
            requestPipeline.addFilter(requestTee);
            if (config.isXmlFormat()) {
                HttpRequestFilter filter = new HttpRequestFilter(true);
                filter.setContentFilterFactory(new DefaultContentFilterFactory());
                requestPipeline.addFilter(filter);
            }
            requestPipeline.addFilter(new CharsetDecoderFilter(inputWriter, UTF8));
            
            // If we act as a proxy, we first need to read the start of the request before
            // the outSocket is available.
            while (outSocket == null) {
                requestPipeline.readFrom(tmpIn1);
            }
            
            tmpOut2 = outSocket.getOutputStream();
            requestTee.setOutputStream(tmpOut2);
            
            Pipeline responsePipeline = new Pipeline();
            responsePipeline.addFilter(new SlowLinkSimulator(config.getSlowLink()));
            if (tmpOut1 != null) {
                responsePipeline.addFilter(new Tee(tmpOut1));
            }
            if (config.isXmlFormat()) {
                HttpResponseFilter filter = new HttpResponseFilter(true);
                filter.setContentFilterFactory(new DefaultContentFilterFactory());
                responsePipeline.addFilter(filter);
            }
            responsePipeline.addFilter(new CharsetDecoderFilter(outputWriter, UTF8));
            
            // this is the channel to the endpoint
            rr1 = new SocketRR(this, inSocket, tmpIn1, outSocket, tmpOut2, requestPipeline);

            // this is the channel from the endpoint
            rr2 = new SocketRR(this, outSocket, outSocket.getInputStream(), inSocket, tmpOut1, responsePipeline);
            
            rr1.start();
            rr2.start();
            
            while ((rr1 != null) || (rr2 != null)) {

                if (rr2 != null) {
                    setElapsed(rr2.getElapsed());
                }
                
                // Only loop as long as the connection to the target
                // machine is available - once that's gone we can stop.
                // The old way, loop until both are closed, left us
                // looping forever since no one closed the 1st one.
                
                if ((null != rr1) && rr1.isDone()) {
                    if (rr2 != null) {
                        setState(TCPMonBundle.getMessage("resp00", "Resp"));
                    }
                    rr1 = null;
                }

                if ((null != rr2) && rr2.isDone()) {
                    if (rr1 != null) {
                        setState(TCPMonBundle.getMessage("req00", "Req"));
                    }
                    rr2 = null;
                }

                synchronized (this) {
                    this.wait(100);    // Safety just incase we're not told to wake up.
                }
            }

            active = false;

            setState(TCPMonBundle.getMessage("done00", "Done"));

        } catch (Exception e) {
            StringWriter st = new StringWriter();
            PrintWriter wr = new PrintWriter(st);
            setState(TCPMonBundle.getMessage("error00", "Error"));
            e.printStackTrace(wr);
            wr.close();
            if (outputWriter != null) {
                try {
                    outputWriter.write(st.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
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

    protected abstract void init(String time, String fromHost, String targetHost);
    protected abstract void setOutHost(String outHost);
    protected abstract void setState(String state);
    protected abstract void setRequest(String request);
    protected abstract void setElapsed(String elapsed);
}
