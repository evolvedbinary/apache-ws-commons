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

public class Configuration {
    private int listenPort;
    private String targetHost;
    private int targetPort;
    private boolean proxy;
    private boolean xmlFormat;
    private String httpProxyHost;
    private int httpProxyPort;
    private SlowLinkSimulator slowLink;
    
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
    
    public int getListenPort() {
        return listenPort;
    }
    
    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public String getTargetHost() {
        return targetHost;
    }
    
    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }
    
    public int getTargetPort() {
        return targetPort;
    }
    
    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public boolean isProxy() {
        return proxy;
    }

    public void setProxy(boolean proxy) {
        this.proxy = proxy;
    }

    public boolean isXmlFormat() {
        return xmlFormat;
    }

    public void setXmlFormat(boolean xmlFormat) {
        this.xmlFormat = xmlFormat;
    }

    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    public void setHttpProxyHost(String httpProxyHost) {
        this.httpProxyHost = httpProxyHost;
    }

    public int getHttpProxyPort() {
        return httpProxyPort;
    }

    public void setHttpProxyPort(int httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
    }

    public SlowLinkSimulator getSlowLink() {
        return slowLink;
    }

    public void setSlowLink(SlowLinkSimulator slowLink) {
        this.slowLink = slowLink;
    }
}
