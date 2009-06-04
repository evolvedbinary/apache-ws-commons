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

import javax.activation.MimeType;

import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;

/**
 * {@link ContentFilterFactory} implementation that handles <tt>multipart/related</tt>.
 * It delegates to a different content filter factory for individual MIME parts.
 */
public class MultipartContentFilterFactory implements ContentFilterFactory {
    private final ContentFilterFactory parent;
    
    public MultipartContentFilterFactory(ContentFilterFactory parent) {
        this.parent = parent;
    }

    public StreamFilter[] getContentFilterChain(MimeType contentType) {
        if (contentType.getBaseType().equalsIgnoreCase("multipart/related")) {
            return new StreamFilter[] { new MultipartFilter(this, contentType) };
        } else {
            return parent.getContentFilterChain(contentType);
        }
    }
}
