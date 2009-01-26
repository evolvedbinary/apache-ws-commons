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

import org.apache.ws.commons.tcpmon.core.filter.HeaderHandler;
import org.apache.ws.commons.tcpmon.core.filter.HeaderProcessor;
import org.apache.ws.commons.tcpmon.core.filter.Stream;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;

/**
 * Filter that processes a MIME part.
 */
public class MimePartFilter implements StreamFilter {
    private HeaderProcessor headerProcessor;
    private StreamFilter[] contentFilterChain;

    public MimePartFilter(final ContentFilterFactory contentFilterFactory) {
        headerProcessor = new HeaderProcessor();
        headerProcessor.addHandler(new HeaderHandler() {
            public String handleHeader(String name, String value) {
                if (name.equalsIgnoreCase("Content-Type")) {
                    contentFilterChain = contentFilterFactory.getContentFilterChain(value);
                }
                return value;
            }
        });
    }

    public void invoke(Stream stream) {
        while (stream.available() > 0) {
            if (headerProcessor == null) {
                stream.skipAll();
            } else {
                if (headerProcessor.process(stream)) {
                    headerProcessor = null;
                    if (contentFilterChain != null) {
                        for (int i=contentFilterChain.length-1; i>=0; i--) {
                            stream.pushFilter(contentFilterChain[i]);
                        }
                    }
                } else {
                    return;
                }
            }
        }
    }
}
