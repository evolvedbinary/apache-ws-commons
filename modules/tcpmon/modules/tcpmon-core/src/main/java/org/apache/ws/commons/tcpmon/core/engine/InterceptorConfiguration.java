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

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.apache.ws.commons.tcpmon.core.filter.Pipeline;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilterFactory;
import org.apache.ws.commons.tcpmon.core.filter.mime.ContentFilterFactory;

/**
 * Holds the configuration for an {@link Interceptor} instance.
 * To avoid any concurrency issue, this class is designed as immutable.
 * Instances are created using {@link InterceptorConfigurationBuilder}. 
 */
public class InterceptorConfiguration {
    private final ServerSocketFactory serverSocketFactory;
    private final int listenPort;
    private final SocketFactory socketFactory;
    private final String targetHost;
    private final int targetPort;
    private final boolean proxy;
    private final String httpProxyHost;
    private final int httpProxyPort;
    final StreamFilterFactory[] requestFilters;
    final StreamFilterFactory[] responseFilters;
    private final ContentFilterFactory requestContentFilterFactory;
    private final ContentFilterFactory responseContentFilterFactory;

    InterceptorConfiguration(ServerSocketFactory serverSocketFactory, int listenPort,
            SocketFactory socketFactory, String targetHost, int targetPort, boolean proxy,
            String httpProxyHost, int httpProxyPort, StreamFilterFactory[] requestFilters,
            StreamFilterFactory[] responseFilters, ContentFilterFactory
            requestContentFilterFactory, ContentFilterFactory responseContentFilterFactory) {
        this.serverSocketFactory = serverSocketFactory;
        this.listenPort = listenPort;
        this.socketFactory = socketFactory;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.proxy = proxy;
        this.httpProxyHost = httpProxyHost;
        this.httpProxyPort = httpProxyPort;
        this.requestFilters = requestFilters;
        this.responseFilters = responseFilters;
        this.requestContentFilterFactory = requestContentFilterFactory;
        this.responseContentFilterFactory = responseContentFilterFactory;
    }

    public ServerSocketFactory getServerSocketFactory() {
        return serverSocketFactory;
    }

    public int getListenPort() {
        return listenPort;
    }
    
    public SocketFactory getSocketFactory() {
        return socketFactory;
    }

    public boolean isSecureSocketFactory() {
        return socketFactory instanceof SSLSocketFactory;
    }

    public String getTargetHost() {
        return targetHost;
    }
    
    public int getTargetPort() {
        return targetPort;
    }
    
    public boolean isProxy() {
        return proxy;
    }

    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    public int getHttpProxyPort() {
        return httpProxyPort;
    }
    
    private static void applyFilters(Pipeline pipeline, StreamFilterFactory[] filters) {
        for (int i=0; i<filters.length; i++) {
            pipeline.addFilter(filters[i].newInstance());
        }
    }
    
    public void applyRequestFilters(Pipeline pipeline) {
        applyFilters(pipeline, requestFilters);
    }
    
    public void applyResponseFilters(Pipeline pipeline) {
        applyFilters(pipeline, responseFilters);
    }

    public ContentFilterFactory getRequestContentFilterFactory() {
        return requestContentFilterFactory;
    }

    public ContentFilterFactory getResponseContentFilterFactory() {
        return responseContentFilterFactory;
    }
}
