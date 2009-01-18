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

import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;
import org.apache.ws.commons.tcpmon.core.filter.XmlFormatFilter;

/**
 * Default {@link ContentFilterFactory} implementation.
 */
public class DefaultContentFilterFactory implements ContentFilterFactory {
    public StreamFilter getContentFilter(String contentType) {
        int idx = contentType.indexOf(';');
        String baseContentType = (idx == -1 ? contentType : contentType.substring(0, idx)).trim().toLowerCase();
        if (baseContentType.equals("text/xml") || baseContentType.equals("application/xml")
                || baseContentType.equals("application/soap+xml") || baseContentType.equals("application/xop+xml")) {
            return new XmlFormatFilter(3);
        } else if (baseContentType.equals("multipart/related")) {
            return new MultipartFilter(this, contentType);
        } else {
            return null;
        }
    }
}
