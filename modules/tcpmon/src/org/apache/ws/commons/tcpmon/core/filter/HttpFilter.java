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

package org.apache.ws.commons.tcpmon.core.filter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Base class for {@link HttpRequestFilter} and {@link HttpResponseFilter}.
 */
public abstract class HttpFilter implements StreamFilter {
    private static final int STATE_FIRST_LINE = 0;
    private static final int STATE_HEADER = 1;
    private static final int STATE_CONTENT = 2;
    private static final int STATE_COMPLETE = 2;

    private final HeaderProcessor headerProcessor = new HeaderProcessor();
    private final boolean decodeTransferEncoding;
    protected final List handlers = new LinkedList();
    private int state = STATE_FIRST_LINE;
    private ContentFilterFactory contentFilterFactory;
    private EntityProcessor transferDecoder;
    private StreamFilter contentFilter;
    
    public HttpFilter(boolean decodeTransferEncoding) {
        this.decodeTransferEncoding = decodeTransferEncoding;
        headerProcessor.addHandler(new HeaderHandler() {
            public String handleHeader(String name, String value) {
                return processHeader(name, value);
            }
        });
    }
    
    public void setContentFilterFactory(ContentFilterFactory contentFilterFactory) {
        if (!decodeTransferEncoding) {
            throw new UnsupportedOperationException();
        }
        this.contentFilterFactory = contentFilterFactory;
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
                    if (headerProcessor.process(stream)) {
                        state = STATE_CONTENT;
                        if (contentFilter != null) {
                            stream.pushFilter(contentFilter);
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
                            if (contentFilter != null) {
                                stream.popFilter();
                            }
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

    private String processHeader(String name, String value) {
        if (name.equalsIgnoreCase("Content-Length")) {
            transferDecoder = new IdentityDecoder(Integer.parseInt(value));
        } else if (name.equalsIgnoreCase("Transfer-Encoding")) {
            if (value.equals("chunked")) {
                transferDecoder = new ChunkedDecoder();
            }
        } else if (name.equalsIgnoreCase("Content-Type")) {
            if (contentFilterFactory != null) {
                contentFilter = contentFilterFactory.getContentFilter(value);
            }
        }
        for (Iterator it = handlers.iterator(); it.hasNext(); ) {
            value = ((HeaderHandler)it.next()).handleHeader(name, value);
            if (value == null) {
                break;
            }
        }
        return value;
    }
}
