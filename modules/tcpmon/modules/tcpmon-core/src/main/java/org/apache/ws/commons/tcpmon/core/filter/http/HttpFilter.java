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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.ws.commons.tcpmon.core.filter.EntityProcessor;
import org.apache.ws.commons.tcpmon.core.filter.HeaderParser;
import org.apache.ws.commons.tcpmon.core.filter.ReadOnlyStream;
import org.apache.ws.commons.tcpmon.core.filter.Stream;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;
import org.apache.ws.commons.tcpmon.core.filter.StreamUtil;
import org.apache.ws.commons.tcpmon.core.filter.mime.ContentFilterFactory;

/**
 * Base class for {@link HttpRequestFilter} and {@link HttpResponseFilter}.
 */
public abstract class HttpFilter implements StreamFilter {
    private static final int STATE_FIRST_LINE = 0;
    private static final int STATE_HEADER = 1;
    private static final int STATE_CONTENT = 2;
    private static final int STATE_COMPLETE = 3;

    private final boolean decodeTransferEncoding;
    protected final List handlers = new LinkedList();
    private int state = STATE_FIRST_LINE;
    private final Headers headers = new Headers();
    private ContentFilterFactory contentFilterFactory;
    private EntityProcessor transferDecoder;
    private StreamFilter[] contentFilterChain;
    
    public HttpFilter(boolean decodeTransferEncoding) {
        this.decodeTransferEncoding = decodeTransferEncoding;
    }
    
    public void setContentFilterFactory(ContentFilterFactory contentFilterFactory) {
        if (!decodeTransferEncoding) {
            throw new UnsupportedOperationException();
        }
        this.contentFilterFactory = contentFilterFactory;
    }
    
    public boolean isComplete() {
        return state == STATE_COMPLETE;
    }

    public void invoke(Stream stream) {
        while (stream.available() > 0) {
            switch (state) {
                case STATE_FIRST_LINE: {
                    int eol = StreamUtil.searchEndOfLine(stream);
                    if (eol == -1) {
                        // EOL not yet available; maybe next time...
                        return;
                    } else {
                        String orgRequest = StreamUtil.getAsciiString(stream, 0, eol);
                        String request = processFirstLine(orgRequest);
                        if (request == orgRequest) {
                            stream.skip(eol+2);
                        } else {
                            stream.discard(eol);
                            StreamUtil.insertAsciiString(stream, request);
                            stream.skip(2);
                        }
                        state = STATE_HEADER;
                    }
                    break;
                }
                case STATE_HEADER: {
                    HeaderParser headerParser = new HeaderParser(stream);
                    while (headerParser.available()) {
                        headers.add(headerParser.getHeaderName(), headerParser.getHeaderValue());
                        headerParser.discard();
                    }
                    if (headerParser.noMoreHeaders()) {
                        processHeaders();
                        for (Iterator it = headers.iterator(); it.hasNext(); ) {
                            Header header = (Header)it.next();
                            headerParser.insert(header.getName(), header.getValue());
                        }
                        headerParser.skip();
                        state = STATE_CONTENT;
                        if (contentFilterChain != null) {
                            for (int i=contentFilterChain.length-1; i>=0; i--) {
                                stream.pushFilter(contentFilterChain[i]);
                            }
                        }
                        break;
                    } else {
                        return;
                    }
                }
                case STATE_CONTENT: {
                    if (transferDecoder != null) {
                        Stream decoderStream =
                                decodeTransferEncoding ? stream : new ReadOnlyStream(stream);
                        if (transferDecoder.process(decoderStream)) {
                            state = STATE_COMPLETE;
                            if (contentFilterChain != null) {
                                for (int i=0; i<contentFilterChain.length; i++) {
                                    stream.popFilter();
                                }
                            }
                            completed();
                        }
                        break;
                    }
                    // Fall through
                }
                default:
                    stream.skipAll();
            }
        }
    }
    
    protected abstract String processFirstLine(String firstList);
    protected abstract void completed();

    private void processHeaders() {
        for (Iterator it = headers.iterator(); it.hasNext(); ) {
            Header header = (Header)it.next();
            String name = header.getName();
            String value = header.getValue();
            if (name.equalsIgnoreCase("Content-Length")) {
                transferDecoder = new IdentityDecoder(Integer.parseInt(value));
            } else if (name.equalsIgnoreCase("Transfer-Encoding")) {
                if (value.equals("chunked")) {
                    transferDecoder = new ChunkedDecoder();
                }
            } else if (name.equalsIgnoreCase("Content-Type")) {
                if (contentFilterFactory != null) {
                    contentFilterChain = contentFilterFactory.getContentFilterChain(value);
                }
            }
        }
        for (Iterator it = handlers.iterator(); it.hasNext(); ) {
            ((HeaderHandler)it.next()).handleHeaders(headers);
        }
    }
}
