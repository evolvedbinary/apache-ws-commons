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

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.ws.commons.tcpmon.core.filter.EntityProcessor;
import org.apache.ws.commons.tcpmon.core.filter.HeaderParser;
import org.apache.ws.commons.tcpmon.core.filter.ReadOnlyEntityProcessorWrapper;
import org.apache.ws.commons.tcpmon.core.filter.ReadOnlyStream;
import org.apache.ws.commons.tcpmon.core.filter.Stream;
import org.apache.ws.commons.tcpmon.core.filter.StreamException;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;
import org.apache.ws.commons.tcpmon.core.filter.StreamUtil;
import org.apache.ws.commons.tcpmon.core.filter.mime.ContentFilterFactory;
import org.apache.ws.commons.tcpmon.core.filter.zip.GZIPDecoder;
import org.apache.ws.commons.tcpmon.core.filter.zip.GZIPEncoder;

/**
 * Base class for {@link HttpRequestFilter} and {@link HttpResponseFilter}.
 */
public abstract class HttpFilter implements StreamFilter {
    private static final int STATE_FIRST_LINE = 0;
    private static final int STATE_HEADER = 1;
    private static final int STATE_CONTENT = 2;
    private static final int STATE_COMPLETE = 3;

    private final boolean decode;
    private int state = STATE_FIRST_LINE;
    private final Headers headers = new Headers();
    private ContentFilterFactory contentFilterFactory;
    private EntityProcessor transferDecoder;
    private StreamFilter[] contentFilterChain;
    
    public HttpFilter(boolean decode) {
        this.decode = decode;
    }
    
    public void setContentFilterFactory(ContentFilterFactory contentFilterFactory) {
        this.contentFilterFactory = contentFilterFactory;
    }
    
    public boolean isComplete() {
        return state == STATE_COMPLETE;
    }

    public boolean isReadOnly() {
        return false;
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
                    } else {
                        return;
                    }
                    break;
                }
                case STATE_CONTENT: {
                    if (transferDecoder != null) {
                        if (transferDecoder.process(stream)) {
                            state = STATE_COMPLETE;
                            if (contentFilterChain != null) {
                                for (int i=0; i<contentFilterChain.length; i++) {
                                    stream.popFilter();
                                }
                            }
                            onComplete();
                        }
                        break;
                    }
                    // Fall through
                }
                default:
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
        processHeaders(headers);
        
        boolean hasEntity = false;
        boolean discardHeaders = false;
        StreamFilter transferEncoder = null;
        StreamFilter contentDecoder = null;
        StreamFilter contentEncoder = null;
        for (Header header : headers) {
            String name = header.getName();
            String value = header.getValue();
            if (name.equalsIgnoreCase("Content-Length")) {
                hasEntity = true;
                transferDecoder = new IdentityDecoder(Integer.parseInt(value));
                transferEncoder = new IdentityEncoder(headers);
                discardHeaders = true;
            } else if (name.equalsIgnoreCase("Transfer-Encoding")) {
                hasEntity = true;
                if (value.equals("chunked")) {
                    transferDecoder = new ChunkedDecoder();
                    transferEncoder = new ChunkedEncoder();
                    discardHeaders = false;
                }
            } else if (name.equalsIgnoreCase("Content-Type")) {
                hasEntity = true;
                if (contentFilterFactory != null) {
                    try {
                        contentFilterChain = contentFilterFactory.getContentFilterChain(new MimeType(value));
                    } catch (MimeTypeParseException ex) {
                        // If the content type is unparseable, just continue
                    }
                }
            } else if (name.equalsIgnoreCase("Content-Encoding")) {
                if (value.equals("gzip")) {
                    contentDecoder = new GZIPDecoder();
                    contentEncoder = new GZIPEncoder();
                }
            }
        }
        
        if (discardHeaders && contentFilterChain != null) {
            headerParser.discard();
        } else {
            for (Header header : headers) {
                headerParser.insert(header.getName(), header.getValue());
            }
            headerParser.skip();
        }
        
        if (hasEntity) {
            if (contentFilterChain != null) {
                if (!decode) {
                    if (transferEncoder != null) {
                        stream.pushFilter(transferEncoder);
                    }
                    if (contentEncoder != null) {
                        stream.pushFilter(contentEncoder);
                    }
                }
                for (int i=contentFilterChain.length-1; i>=0; i--) {
                    stream.pushFilter(contentFilterChain[i]);
                }
                if (contentDecoder != null) {
                    stream.pushFilter(contentDecoder);
                }
            } else {
                if (decode) {
                    if (contentDecoder != null) {
                        stream.pushFilter(contentDecoder);
                    }
                } else {
                    if (transferDecoder != null) {
                        transferDecoder = new ReadOnlyEntityProcessorWrapper(transferDecoder);
                    }
                }
            }
            state = STATE_CONTENT;
        } else {
            onComplete();
        }
    }
    
    private void onComplete() {
        state = STATE_COMPLETE;
        transferDecoder = null;
        contentFilterChain = null;
        completed();
    }
}
