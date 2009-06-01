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

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.mortbay.http.HttpContext;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;

public class TestUtil {
    private TestUtil() {}
    
    public static Server createServer(int port) {
        Server server = new Server();
        SocketListener listener = new SocketListener();
        listener.setPort(port);
        server.addListener(listener);
        HttpContext context = new HttpContext(server, "/*");
        context.addHandler(new TestHttpHandler());
        return server;
    }
    
    public static HttpClient createClient(InterceptorConfiguration config) {
        HttpClient client = new DefaultHttpClient();
        if (config.isProxy()) {
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost("localhost", config.getListenPort(), "http"));
        }
        // We don't handle 100 continue yet
        client.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        return client;
    }
    
    public static String getBaseUri(InterceptorConfiguration config, Server server) {
        int serverPort = server.getListeners()[0].getPort();
        if (config.isProxy()) {
            return "http://localhost:" + serverPort;
        } else {
            if (config.getTargetPort() != serverPort) {
                throw new IllegalArgumentException();
            }
            return "http://localhost:" + config.getListenPort();
        }
    }

    public static String getResponseAsString(HttpResponse response) throws Exception {
        InputStream in = response.getEntity().getContent();
        try {
            return IOUtils.toString(in, "UTF-8");
        } finally {
            in.close();
        }
    }
}
