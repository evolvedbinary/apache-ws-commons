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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.activation.MimeType;

import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;

public class ChainedContentFilterFactory implements ContentFilterFactory {
    private final List<ContentFilterFactory> factories = new ArrayList<ContentFilterFactory>(5);
    
    public void add(ContentFilterFactory factory) {
        factories.add(factory);
    }

    public StreamFilter[] getContentFilterChain(MimeType contentType) {
        if (factories.isEmpty()) {
            return null;
        } else if (factories.size() == 1) {
            return factories.get(0).getContentFilterChain(contentType);
        } else {
            List<StreamFilter> filters = new ArrayList<StreamFilter>(5);
            for (ContentFilterFactory factory : factories) {
                StreamFilter[] f = factory.getContentFilterChain(contentType);
                if (f != null) {
                    filters.addAll(Arrays.asList(f));
                }
            }
            return filters.isEmpty() ? null : filters.toArray(new StreamFilter[filters.size()]);
        }
    }
}
