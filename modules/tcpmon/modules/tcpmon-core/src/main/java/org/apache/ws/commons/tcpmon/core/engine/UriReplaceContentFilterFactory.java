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

package org.apache.ws.commons.tcpmon.core.engine;

import java.io.UnsupportedEncodingException;

import javax.activation.MimeType;

import org.apache.ws.commons.tcpmon.core.filter.ReplaceFilter;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;
import org.apache.ws.commons.tcpmon.core.filter.http.HostRewriter;
import org.apache.ws.commons.tcpmon.core.filter.mime.ContentFilterFactory;

public class UriReplaceContentFilterFactory implements ContentFilterFactory {
    public static final int REQUEST = 0;
    public static final int RESPONSE = 1;
    
    private final HostRewriter hostRewriter;
    private final int direction;
    
    public UriReplaceContentFilterFactory(HostRewriter hostRewriter, int direction) {
        this.hostRewriter = hostRewriter;
        this.direction = direction;
    }

    public StreamFilter[] getContentFilterChain(MimeType contentType) {
        if (contentType.getPrimaryType().equalsIgnoreCase("text")
                || contentType.getSubType().toLowerCase().indexOf("xml") != -1) {
            String orgBaseUri = hostRewriter.getOrgBaseUri();
            String targetBaseUri = hostRewriter.getTargetBaseUri();
            String charset = contentType.getParameter("charset");
            if (orgBaseUri != null && targetBaseUri != null && charset != null) {
                String fromBaseUri;
                String toBaseUri;
                if (direction == REQUEST) {
                    fromBaseUri = orgBaseUri;
                    toBaseUri = targetBaseUri;
                } else {
                    fromBaseUri = targetBaseUri;
                    toBaseUri = orgBaseUri;
                }
                try {
                    return new StreamFilter[] { new ReplaceFilter(fromBaseUri, toBaseUri, charset) };
                } catch (UnsupportedEncodingException ex) {
                    return null;
                }
            }
        }
        return null;
    }
}
