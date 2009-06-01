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

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.mortbay.http.HttpContext;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;

public abstract class InterceptorTestBase extends TestCase {
    protected static final int INTERCEPTOR_PORT = 9001;
    protected static final int SERVER_PORT = 9002;
    
    private Server server;
    private InterceptorConfiguration config;
    private Interceptor interceptor;
    private HttpClient client;
    private String baseUri;
    
    protected void setUp() throws Exception {
        // Set up server
        
        server = new Server();
        SocketListener listener = new SocketListener();
        listener.setPort(SERVER_PORT);
        server.addListener(listener);
        HttpContext context = new HttpContext(server, "/*");
        context.addHandler(new TestHttpHandler());
        server.start();
        
        // Set up interceptor
        
        config = buildInterceptorConfiguration();
        interceptor = new Interceptor(config, new Dump(System.out));
        // Wait for the interceptor to accept connections
        Thread.sleep(500); // TODO: there should be a better way
        
        // Set up client
        
        client = new DefaultHttpClient();
        if (config.isProxy()) {
            baseUri = "http://localhost:" + SERVER_PORT;
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost("localhost", INTERCEPTOR_PORT, "http"));
        } else {
            baseUri = "http://localhost:" + INTERCEPTOR_PORT;
        }
        // We don't handle 100 continue yet
        client.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
    }

    protected void tearDown() throws Exception {
        interceptor.halt();
        server.stop();
    }

    protected abstract InterceptorConfiguration buildInterceptorConfiguration();
    
    private static String getResponseAsString(HttpResponse response) throws Exception {
        InputStream in = response.getEntity().getContent();
        try {
            return IOUtils.toString(in, "UTF-8");
        } finally {
            in.close();
        }
    }
    
    public void testGet() throws Exception {
        HttpGet request = new HttpGet(baseUri + "/test");
        HttpResponse response = client.execute(request);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("test", getResponseAsString(response));
    }
    
    public void testPost() throws Exception {
        HttpPost request = new HttpPost(baseUri + "/echo");
        request.setEntity(new StringEntity("test"));
        HttpResponse response = client.execute(request);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("test", getResponseAsString(response));
    }
    
    public void testGetWithKeepAlive() throws Exception {
        HttpGet request = new HttpGet(baseUri + "/test");
        HttpResponse response = client.execute(request);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("test", getResponseAsString(response));
        response = client.execute(request);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("test", getResponseAsString(response));
    }
}
