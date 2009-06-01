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
import javax.activation.MimeTypeParseException;

import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;

/**
 * Partial {@link ContentFilterFactory} implementation that handles <tt>multipart/related</tt>.
 */
public abstract class MultipartAwareContentFilterFactory implements ContentFilterFactory {
    public StreamFilter[] getContentFilterChain(String contentType) {
        MimeType ctype;
        try {
            ctype = new MimeType(contentType);
        } catch (MimeTypeParseException ex) {
            return null;
        }
        if (ctype.getBaseType().equalsIgnoreCase("multipart/related")) {
            return new StreamFilter[] { new MultipartFilter(this, contentType) };
        } else {
            return getContentFilterChainForMimePart(ctype);
        }
    }
    
    protected abstract StreamFilter[] getContentFilterChainForMimePart(MimeType contentType);
}
