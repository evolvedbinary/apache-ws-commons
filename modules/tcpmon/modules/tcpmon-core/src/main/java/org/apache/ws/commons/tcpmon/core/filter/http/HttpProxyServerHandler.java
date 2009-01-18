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

package org.apache.ws.commons.tcpmon.core.filter.http;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.ws.commons.tcpmon.core.filter.StreamException;

/**
 * Handler that rewrites an HTTP proxy request to a plain HTTP request.
 */
public abstract class HttpProxyServerHandler extends AbstractHttpRequestHandler {
    public String processRequestLine(String requestLine) {
        String[] parts = requestLine.split(" ");
        URL url;
        try {
            url = new URL(parts[1]);
        } catch (MalformedURLException ex) {
            throw new StreamException(ex);
        }
        int port = url.getPort();
        handleConnection(url.getHost(), port == -1 ? 80 : port);
        return parts[0] + " " + url.getFile() + " " + parts[2];
    }
    
    protected abstract void handleConnection(String host, int port);
}
