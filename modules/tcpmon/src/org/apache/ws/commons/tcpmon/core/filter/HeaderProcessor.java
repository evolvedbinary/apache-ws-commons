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

package org.apache.ws.commons.tcpmon.core.filter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Process a set of HTTP/MIME/RFC822 headers.
 * The headers are submitted to a set of handlers which may instruct
 * the processor to remove the header or substitute it by a new value.
 */
public class HeaderProcessor implements EntityProcessor {
    private final List handlers = new LinkedList();
    
    public void addHandler(HeaderHandler handler) {
        handlers.add(handler);
    }
    
    public boolean process(Stream stream) {
        while (true) {
            int eol = StreamUtil.searchEndOfLine(stream);
            if (eol == -1) {
                return false;
            }
            if (eol == 0) {
                stream.skip(2);
                return true;
            }
            int colon = -1;
            for (int i=0; i<eol; i++) {
                if (stream.get(i) == ':') {
                    colon = i;
                    break;
                }
            }
            String name = StreamUtil.getAsciiString(stream, 0, colon);
            int valueStart = colon+1;
            while (stream.get(valueStart) == ' ') {
                valueStart++;
            }
            String orgValue = StreamUtil.getAsciiString(stream, valueStart, eol);
            String value = orgValue;
            for (Iterator it = handlers.iterator(); it.hasNext(); ) {
                value = ((HeaderHandler)it.next()).handleHeader(name, value);
                if (value == null) {
                    break;
                }
            }
            if (value == null) {
                stream.discard(eol+2);
            } else if (value == orgValue) {
                stream.skip(eol+2);
            } else {
                stream.skip(valueStart);
                stream.discard(eol-valueStart);
                StreamUtil.insertAsciiString(stream, value);
                stream.skip(2);
            }
        }
    }
}
