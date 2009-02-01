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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.ws.commons.tcpmon.core.filter.Pipeline;
import org.apache.ws.commons.tcpmon.core.filter.Stream;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;
import org.apache.ws.commons.tcpmon.core.filter.Tee;
import org.apache.ws.commons.tcpmon.core.filter.http.AbstractHttpResponseHandler;
import org.apache.ws.commons.tcpmon.core.filter.http.HttpProxyClientHandler;
import org.apache.ws.commons.tcpmon.core.filter.http.HttpRequestFilter;
import org.apache.ws.commons.tcpmon.core.filter.http.HttpResponseFilter;

/**
 * Sends a raw HTTP request and invokes {@link IRequestResponse} as necessary.
 */
public class RawSender implements Runnable {
    private final IRequestResponse requestResponse;
    private final Socket socket;
    private final OutputStream out;
    
    public RawSender(AbstractListener listener, String targetHost, int targetPort) throws IOException {
        Configuration config = listener.getConfiguration();
        requestResponse = listener.createRequestResponse("resend");
        requestResponse.setTarget(targetHost, targetPort);
        Pipeline pipeline = new Pipeline();
        pipeline.addFilter(new Tee(requestResponse.getRequestOutputStream()));
        if (config.getHttpProxyHost() != null) {
            HttpRequestFilter requestFilter = new HttpRequestFilter(false);
            pipeline.addFilter(requestFilter);
            requestFilter.addHandler(new HttpProxyClientHandler(targetHost, targetPort));
            socket = new Socket(config.getHttpProxyHost(), config.getHttpProxyPort());
        } else {
            socket = new Socket(targetHost, targetPort);
        }
        requestResponse.setState(IRequestResponse.STATE_ACTIVE);
        pipeline.addFilter(new StreamFilter() {
            public void invoke(Stream stream) {
                stream.skipAll();
                if (stream.isEndOfStream()) {
                    requestResponse.setState(IRequestResponse.STATE_REQ);
                }
            }
        });
        pipeline.addFilter(new Tee(socket.getOutputStream()));
        out = pipeline.getOutputStream();
    }

    public OutputStream getOutputStream() {
        return out;
    }

    public void run() {
        long start = System.currentTimeMillis();
        Pipeline pipeline = new Pipeline();
        pipeline.addFilter(new Tee(requestResponse.getResponseOutputStream()));
        HttpResponseFilter filter = new HttpResponseFilter(false);
        pipeline.addFilter(filter);
        filter.addHandler(new AbstractHttpResponseHandler() {
            public void responseCompleted() {
                try {
                    socket.shutdownInput();
                    socket.shutdownOutput();
                } catch (IOException ex) {
                    // Ignore
                }
            }
        });
        try {
            InputStream in = socket.getInputStream();
            while (pipeline.readFrom(in) != -1) {
                // Just loop
            }
            requestResponse.setState(IRequestResponse.STATE_DONE);
        } catch (IOException ex) {
            requestResponse.setState(IRequestResponse.STATE_ERROR);
            requestResponse.onError(ex);
        } finally {
            requestResponse.setElapsed(System.currentTimeMillis() - start);
            try {
                socket.close();
            } catch (IOException ex) {
                // Ignore
            }
        }
    }
}
