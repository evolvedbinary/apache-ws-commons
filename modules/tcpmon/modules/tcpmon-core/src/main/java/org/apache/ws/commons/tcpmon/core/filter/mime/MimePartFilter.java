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

package org.apache.ws.commons.tcpmon.core.filter.mime;

import org.apache.ws.commons.tcpmon.core.filter.HeaderParser;
import org.apache.ws.commons.tcpmon.core.filter.Stream;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;

/**
 * Filter that processes a MIME part.
 */
public class MimePartFilter implements StreamFilter {
    private static final int HEADERS = 0;
    private static final int CONTENT = 1;

    private final ContentFilterFactory contentFilterFactory;
    private int state = HEADERS;
    private StreamFilter[] contentFilterChain;
    
    public MimePartFilter(ContentFilterFactory contentFilterFactory) {
        this.contentFilterFactory = contentFilterFactory;
    }

    public void invoke(Stream stream) {
        while (stream.available() > 0) {
            if (state == HEADERS) {
                HeaderParser headers = new HeaderParser(stream);
                while (headers.available()) {
                    if (headers.getHeaderName().equalsIgnoreCase("Content-Type")) {
                        contentFilterChain = contentFilterFactory.getContentFilterChain(headers.getHeaderValue());
                    }
                    headers.skip();
                }
                if (headers.noMoreHeaders()) {
                    headers.skip();
                    state = CONTENT;
                    if (contentFilterChain != null) {
                        for (int i=contentFilterChain.length-1; i>=0; i--) {
                            stream.pushFilter(contentFilterChain[i]);
                        }
                    }
                } else {
                    return;
                }
            }
            stream.skipAll();
        }
    }
}
