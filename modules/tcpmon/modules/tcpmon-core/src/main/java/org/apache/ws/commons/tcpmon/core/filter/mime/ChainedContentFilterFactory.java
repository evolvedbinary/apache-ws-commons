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
import java.util.Iterator;
import java.util.List;

import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;

public class ChainedContentFilterFactory implements ContentFilterFactory {
    private final List/*<ContentFilterFactory>*/ factories = new ArrayList(5);
    
    public void add(ContentFilterFactory factory) {
        factories.add(factory);
    }

    public StreamFilter[] getContentFilterChain(String contentType) {
        if (factories.isEmpty()) {
            return null;
        } else if (factories.size() == 1) {
            return ((ContentFilterFactory)factories.get(0)).getContentFilterChain(contentType);
        } else {
            List filters = new ArrayList(5);
            for (Iterator it = factories.iterator(); it.hasNext(); ) {
                StreamFilter[] f = ((ContentFilterFactory)it.next()).getContentFilterChain(contentType);
                if (f != null) {
                    filters.addAll(Arrays.asList(f));
                }
            }
            return filters.isEmpty() ? null : (StreamFilter[])filters.toArray(new StreamFilter[filters.size()]);
        }
    }
}
