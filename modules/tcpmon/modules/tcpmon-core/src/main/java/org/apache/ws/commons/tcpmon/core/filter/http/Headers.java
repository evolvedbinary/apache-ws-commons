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

package org.apache.ws.commons.tcpmon.core.filter.http;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.ws.commons.tcpmon.core.filter.HeaderParser;
import org.apache.ws.commons.tcpmon.core.filter.Stream;
import org.apache.ws.commons.tcpmon.core.filter.StreamUtil;

public class Headers implements Iterable<Header> {
    private final List<Header> headers = new LinkedList<Header>();
    
    public void add(String name, String value) {
        headers.add(new Header(name, value));
    }
    
    public Header getFirst(String name) {
        for (Header header : headers) {
            if (header.getName().equalsIgnoreCase(name)) {
                return header;
            }
        }
        return null;
    }
    
    public void set(String name, String value) {
        boolean replaced = false;
        Header newHeader = new Header(name, value);
        for (ListIterator<Header> it = headers.listIterator(); it.hasNext(); ) {
            Header header = it.next();
            if (header.getName().equalsIgnoreCase(name)) {
                if (replaced) {
                    it.remove();
                } else {
                    it.set(newHeader);
                    replaced = true;
                }
            }
        }
        if (!replaced) {
            headers.add(newHeader);
        }
    }
    
    public Iterator<Header> iterator() {
        return headers.iterator();
    }
    
    /**
     * Write the headers to a given stream.
     * 
     * @param stream the stream to write to
     */
    public void writeTo(Stream stream) {
        HeaderParser p = new HeaderParser(stream);
        for (Header header : headers) {
            p.insert(header.getName(), header.getValue());
        }
        StreamUtil.insertAsciiString(stream, "\r\n");
    }
}
