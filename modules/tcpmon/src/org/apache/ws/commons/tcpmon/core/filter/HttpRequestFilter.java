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

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Filter that parses HTTP requests and invokes a set of {@link HTTPRequestHandler}
 * implementation.
 */
public class HttpRequestFilter implements StreamFilter {
    private static final int STATE_REQUEST = 0;
    private static final int STATE_HEADER = 1;
    private static final int STATE_DONE = 2;
    
    private List handlers = new LinkedList();
    private int state = STATE_REQUEST;

    public void addHandler(HttpRequestHandler handler) {
        handlers.add(handler);
    }

    public void invoke(Stream stream) {
        while (stream.available() > 0) {
            switch (state) {
                case STATE_REQUEST: {
                    int eol = StreamUtil.searchEndOfLine(stream);
                    if (eol == -1) {
                        // EOL not yet available; maybe next time...
                        return;
                    } else {
                        String orgRequest = StreamUtil.getAsciiString(stream, 0, eol);
                        String request = processRequest(orgRequest);
                        if (request == orgRequest) {
                            stream.skip(eol+2);
                        } else {
                            stream.discard(eol);
                            insert(stream, request);
                            stream.skip(2);
                        }
                        state = STATE_HEADER;
                    }
                    break;
                }
                case STATE_HEADER: {
                    int eol = StreamUtil.searchEndOfLine(stream);
                    if (eol == -1) {
                        return;
                    }
                    if (eol == 0) {
                        state = STATE_DONE;
                        break;
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
                    String value = processHeader(name, orgValue);
                    if (value == null) {
                        stream.discard(eol+2);
                    } else if (value == orgValue) {
                        stream.skip(eol+2);
                    } else {
                        stream.skip(valueStart);
                        stream.discard(eol-valueStart);
                        insert(stream, value);
                        stream.skip(2);
                    }
                    break;
                }
                default:
                    stream.skipAll();
            }
        }
    }
    
    private static void insert(Stream stream, String s) {
        byte[] b;
        try {
            b = s.getBytes("ascii");
        } catch (UnsupportedEncodingException ex) {
            // We should never get here
            throw new StreamException(ex);
        }
        stream.insert(b, 0, b.length);
    }
    
    private String processRequest(String request) {
        for (Iterator it = handlers.iterator(); it.hasNext(); ) {
            request = ((HttpRequestHandler)it.next()).processRequest(request);
        }
        return request;
    }
    
    private String processHeader(String name, String value) {
        for (Iterator it = handlers.iterator(); it.hasNext(); ) {
            value = ((HttpRequestHandler)it.next()).processHeader(name, value);
            if (value == null) {
                break;
            }
        }
        return value;
    }
}
