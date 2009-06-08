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

import java.util.HashMap;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.ws.commons.tcpmon.core.filter.EntityProcessor;
import org.apache.ws.commons.tcpmon.core.filter.HeaderParser;
import org.apache.ws.commons.tcpmon.core.filter.ReadOnlyEntityProcessorWrapper;
import org.apache.ws.commons.tcpmon.core.filter.Stream;
import org.apache.ws.commons.tcpmon.core.filter.StreamException;
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

    private static final Map<String,TransferEncoding> transferEncodings;
    private static final Map<String,ContentEncoding> contentEncodings;
    
    static {
        transferEncodings = new HashMap<String,TransferEncoding>();
        transferEncodings.put("chunked", new ChunkedEncoding());
        contentEncodings = new HashMap<String,ContentEncoding>();
        contentEncodings.put("gzip", new GZIPEncoding());
    }
    
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
                        headerParser.discard();
                        processHeaders(stream);
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

    private void processHeaders(Stream stream) {
        processHeaders(headers);
        
        boolean hasEntity = false;
        TransferEncoding transferEncoding = null;
        ContentEncoding contentEncoding = null;
        for (Header header : headers) {
            String name = header.getName();
            String value = header.getValue();
            if (name.equalsIgnoreCase("Content-Length")) {
                hasEntity = true;
                transferEncoding = new IdentityEncoding();
            } else if (name.equalsIgnoreCase("Transfer-Encoding")) {
                hasEntity = true;
                transferEncoding = transferEncodings.get(value);
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
                contentEncoding = contentEncodings.get(value);
            }
        }
        
        if (transferEncoding == null || contentFilterChain == null) {
            headers.writeTo(stream);
        }
        
        if (transferEncoding != null) {
            transferDecoder = transferEncoding.createDecoder(headers);
        }
        
        if (hasEntity) {
            if (contentFilterChain != null) {
                if (!decode) {
                    if (transferEncoding != null) {
                        stream.pushFilter(transferEncoding.createEncoder(headers));
                    }
                    if (contentEncoding != null) {
                        stream.pushFilter(contentEncoding.createEncoder());
                    }
                }
                for (int i=contentFilterChain.length-1; i>=0; i--) {
                    stream.pushFilter(contentFilterChain[i]);
                }
                if (contentEncoding != null) {
                    stream.pushFilter(contentEncoding.createDecoder());
                }
            } else {
                if (decode) {
                    if (contentEncoding != null) {
                        stream.pushFilter(contentEncoding.createDecoder());
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
