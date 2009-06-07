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

import java.io.UnsupportedEncodingException;

import javax.activation.MimeType;

import junit.framework.TestCase;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.ws.commons.tcpmon.core.filter.ReplaceFilter;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;
import org.apache.ws.commons.tcpmon.core.filter.mime.ContentFilterFactory;
import org.mortbay.jetty.Server;

public class InterceptorTest extends TestCase {
    private void testWithContentFilter(boolean chunked) throws Exception {
        Server server = TestUtil.createServer(5555);
        server.start();
        
        InterceptorConfigurationBuilder configBuilder = new InterceptorConfigurationBuilder();
        configBuilder.setTargetHost("localhost");
        configBuilder.setTargetPort(5555);
        configBuilder.setListenPort(8000);
        configBuilder.setRequestContentFilterFactory(new ContentFilterFactory() {
            public StreamFilter[] getContentFilterChain(MimeType contentType) {
                try {
                    return new StreamFilter[] { new ReplaceFilter("pattern", "replacement", "ascii") };
                } catch (UnsupportedEncodingException ex) {
                    return null;
                }
            }
        });
        
        InterceptorConfiguration config = configBuilder.build();
        
        Interceptor interceptor = new Interceptor(config, new Dump(System.out));
        
        HttpClient client = TestUtil.createClient(config);
        HttpPost request = new HttpPost(TestUtil.getBaseUri(config, server) + "/echo");
        request.setEntity(TestUtil.createStringEntity("test-pattern-test", "utf-8", chunked));
        HttpResponse response = client.execute(request);
        assertEquals("test-replacement-test", TestUtil.getResponseAsString(response));
        
        interceptor.halt();
        
        server.stop();
    }

    public void testWithContentFilterNotChunked() throws Exception {
        testWithContentFilter(false);
    }

    public void testWithContentFilterChunked() throws Exception {
        testWithContentFilter(true);
    }
}
