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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.ws.commons.tcpmon.core.filter.CharsetRecoderFilter;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;
import org.apache.ws.commons.tcpmon.core.filter.XmlFormatFilter;

/**
 * Default {@link ContentFilterFactory} implementation.
 */
public class DefaultContentFilterFactory implements ContentFilterFactory {
    private static final Set xmlContentTypes = new HashSet(Arrays.asList(new String[] {
            "text/xml", "application/xml", "application/soap+xml", "application/xop+xml" }));
    private static final Charset UTF8 = Charset.forName("utf-8");
    
    public StreamFilter[] getContentFilterChain(String contentType) {
        MimeType ctype;
        try {
            ctype = new MimeType(contentType);
        } catch (MimeTypeParseException ex) {
            return null;
        }
        String baseType = ctype.getBaseType().toLowerCase();
        boolean isXml = xmlContentTypes.contains(baseType);
        List filters = new ArrayList(2);
        if (isXml || ctype.getPrimaryType().equalsIgnoreCase("text")) {
            String charsetName = ctype.getParameter("charset");
            if (charsetName != null) {
                Charset charset = Charset.forName(charsetName);
                if (!charset.equals(UTF8)) {
                    filters.add(new CharsetRecoderFilter(charset, UTF8));
                }
            }
        }
        if (isXml) {
            filters.add(new XmlFormatFilter(3));
        } else if (baseType.equals("multipart/related")) {
            filters.add(new MultipartFilter(this, contentType));
        }
        return filters.isEmpty() ? null : (StreamFilter[])filters.toArray(new StreamFilter[filters.size()]);
    }
}
