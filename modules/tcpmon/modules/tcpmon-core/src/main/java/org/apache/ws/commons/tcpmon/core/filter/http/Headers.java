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

public class Headers {
    private final List/*<Header>*/ headers = new LinkedList();
    
    public void add(String name, String value) {
        headers.add(new Header(name, value));
    }
    
    public Header getFirst(String name) {
        for (Iterator it = headers.iterator(); it.hasNext(); ) {
            Header header = (Header)it.next();
            if (header.getName().equalsIgnoreCase(name)) {
                return header;
            }
        }
        return null;
    }
    
    public void set(String name, String value) {
        boolean replaced = false;
        Header newHeader = new Header(name, value);
        for (ListIterator it = headers.listIterator(); it.hasNext(); ) {
            Header header = (Header)it.next();
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
    
    public Iterator iterator() {
        return headers.iterator();
    }
}
