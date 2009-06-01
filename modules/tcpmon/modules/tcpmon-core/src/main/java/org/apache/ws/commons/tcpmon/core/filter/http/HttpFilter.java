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

import org.apache.ws.commons.tcpmon.core.filter.HeaderParser;
import org.apache.ws.commons.tcpmon.core.filter.ReadOnlyFilterWrapper;
import org.apache.ws.commons.tcpmon.core.filter.Stream;
import org.apache.ws.commons.tcpmon.core.filter.StreamException;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;
import org.apache.ws.commons.tcpmon.core.filter.StreamUtil;
import org.apache.ws.commons.tcpmon.core.filter.mime.ContentFilterFactory;

/**
 * Base class for {@link HttpRequestFilter} and {@link HttpResponseFilter}.
 */
public abstract class HttpFilter implements StreamFilter, EntityCompletionListener {
    private static final int STATE_FIRST_LINE = 0;
    private static final int STATE_HEADER = 1;
    private static final int STATE_CONTENT = 2;
    private static final int STATE_COMPLETE = 3;

    private final boolean decodeTransferEncoding;
    private int state = STATE_FIRST_LINE;
    private final Headers headers = new Headers();
    private ContentFilterFactory contentFilterFactory;
    
    public HttpFilter(boolean decodeTransferEncoding) {
        this.decodeTransferEncoding = decodeTransferEncoding;
    }
    
    public void setContentFilterFactory(ContentFilterFactory contentFilterFactory) {
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
                        processHeaders(headerParser, stream);
                        state = STATE_CONTENT;
                    } else {
                        return;
                    }
                    break;
                }
                case STATE_CONTENT:
                    stream.skipAll();
                    break;
                case STATE_COMPLETE:
                    throw new StreamException("Received content after request or response was complete");
            }
        }
    }
    
    protected abstract String processFirstLine(String firstList);
    protected abstract void processHeaders(Headers headers);
    protected abstract void completed();

    private void processHeaders(HeaderParser headerParser, Stream stream) {
        boolean hasEntity = false;
        boolean discardHeaders = false;
        StreamFilter transferDecoder = null;
        StreamFilter transferEncoder = null;
        StreamFilter[] contentFilterChain = null;
        for (Iterator it = headers.iterator(); it.hasNext(); ) {
            Header header = (Header)it.next();
            String name = header.getName();
            String value = header.getValue();
            if (name.equalsIgnoreCase("Content-Length")) {
                hasEntity = true;
                transferDecoder = new IdentityDecoder(Integer.parseInt(value), this);
                transferEncoder = new IdentityEncoder(headers);
                discardHeaders = true;
            } else if (name.equalsIgnoreCase("Transfer-Encoding")) {
                hasEntity = true;
                if (value.equals("chunked")) {
                    transferDecoder = new ChunkedDecoder(this);
                    transferEncoder = new ChunkedEncoder();
                    discardHeaders = false;
                }
            } else if (name.equalsIgnoreCase("Content-Type")) {
                hasEntity = true;
                if (contentFilterFactory != null) {
                    contentFilterChain = contentFilterFactory.getContentFilterChain(value);
                }
            }
        }
        
        processHeaders(headers);
        
        if (discardHeaders && contentFilterChain != null) {
            headerParser.discard();
        } else {
            for (Iterator it = headers.iterator(); it.hasNext(); ) {
                Header header = (Header)it.next();
                headerParser.insert(header.getName(), header.getValue());
            }
            headerParser.skip();
        }
        
        if (hasEntity) {
            if (contentFilterChain != null) {
                if (transferEncoder != null && !decodeTransferEncoding) {
                    stream.pushFilter(transferEncoder);
                }
                for (int i=contentFilterChain.length-1; i>=0; i--) {
                    stream.pushFilter(contentFilterChain[i]);
                }
            }
            if (transferDecoder != null) {
                stream.pushFilter(decodeTransferEncoding || contentFilterChain != null
                        ? transferDecoder
                        : new ReadOnlyFilterWrapper(transferDecoder));
            }
        } else {
            onComplete();
        }
    }

    public void onComplete() {
        state = STATE_COMPLETE;
        completed();
    }
}
