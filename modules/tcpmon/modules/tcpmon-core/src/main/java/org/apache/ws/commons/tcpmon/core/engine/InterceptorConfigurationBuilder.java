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

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.ws.commons.tcpmon.core.filter.StreamFilterFactory;

/**
 * Creates {@link InterceptorConfiguration} instances.
 */
public class InterceptorConfigurationBuilder {
    private ServerSocketFactory serverSocketFactory;
    private int listenPort;
    private SocketFactory socketFactory;
    private String targetHost;
    private int targetPort;
    private boolean proxy;
    private String httpProxyHost;
    private int httpProxyPort;
    private final List/*<StreamFilterFactory>*/ requestFilters = new ArrayList();
    private final List/*<StreamFilterFactory>*/ responseFilters = new ArrayList();

    public InterceptorConfigurationBuilder() {
    }
    
    public InterceptorConfigurationBuilder(InterceptorConfiguration config) {
        serverSocketFactory = config.getServerSocketFactory();
        listenPort = config.getListenPort();
        socketFactory = config.getSocketFactory();
        targetHost = config.getTargetHost();
        targetPort = config.getTargetPort();
        proxy = config.isProxy();
        httpProxyHost = config.getHttpProxyHost();
        httpProxyPort = config.getHttpProxyPort();
        requestFilters.addAll(Arrays.asList(config.requestFilters));
        responseFilters.addAll(Arrays.asList(config.responseFilters));
    }
    
    public void setServerSocketFactory(ServerSocketFactory serverSocketFactory) {
        this.serverSocketFactory = serverSocketFactory;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public void setSocketFactory(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }
    
    /**
     * Configure the interceptor to use SSL for outgoing connections.
     * 
     * @param validateCerts whether server certificates should be validated
     * @throws GeneralSecurityException
     */
    public void configureSSLSocketFactory(boolean validateCerts) throws GeneralSecurityException {
        SSLContext ctx = SSLContext.getInstance("SSL");
        ctx.init(null, validateCerts ? null : new TrustManager[] { new NoValidateCertTrustManager() }, null);
        socketFactory = ctx.getSocketFactory();
    }

    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }
    
    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public void setProxy(boolean proxy) {
        this.proxy = proxy;
    }

    public void configProxyFromSystemProperties() {
        String host = System.getProperty("http.proxyHost");
        if (host != null && host.length() > 0) {
            httpProxyHost = host;
            String port = System.getProperty("http.proxyPort");
            if (port != null && port.length() > 0) {
                httpProxyPort = Integer.parseInt(port);
            } else {
                httpProxyPort = 80;
            }
        } else {
            httpProxyHost = null;
        }
    }
    
    public void setHttpProxyHost(String httpProxyHost) {
        this.httpProxyHost = httpProxyHost;
    }

    public void setHttpProxyPort(int httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
    }
    
    public void addRequestFilter(StreamFilterFactory filter) {
        requestFilters.add(filter);
    }
    
    public void addResponseFilter(StreamFilterFactory filter) {
        responseFilters.add(filter);
    }
    
    public InterceptorConfiguration build() {
        if (serverSocketFactory == null) {
            serverSocketFactory = ServerSocketFactory.getDefault();
        }
        if (socketFactory == null) {
            socketFactory = SocketFactory.getDefault();
        }
        if (proxy) {
            targetHost = null;
            targetPort = -1;
        }
        return new InterceptorConfiguration(serverSocketFactory, listenPort, socketFactory,
                targetHost, targetPort, proxy, httpProxyHost, httpProxyPort,
                (StreamFilterFactory[])requestFilters.toArray(new StreamFilterFactory[requestFilters.size()]),
                (StreamFilterFactory[])responseFilters.toArray(new StreamFilterFactory[responseFilters.size()]));
    }
}
