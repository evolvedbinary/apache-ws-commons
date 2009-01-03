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

import java.io.IOException;

/**
 * Abstract filter that allows HTTP request rewriting.
 */
public abstract class HttpRequestFilter implements StreamFilter {
    private static final int STATE_REQUEST = 0;
    private static final int STATE_HEADER = 1;
    private static final int STATE_DONE = 2;
    
    private int state = STATE_REQUEST;
    
    public void invoke(Stream stream) throws IOException {
        while (stream.available() > 0) {
            switch (state) {
                case STATE_REQUEST: {
                    int eol = searchEndOfLine(stream);
                    if (eol == -1) {
                        // EOL not yet available; maybe next time...
                        return;
                    } else {
                        String orgRequest = getString(stream, 0, eol);
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
                    int eol = searchEndOfLine(stream);
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
                    String name = getString(stream, 0, colon);
                    int valueStart = colon+1;
                    while (stream.get(valueStart) == ' ') {
                        valueStart++;
                    }
                    String orgValue = getString(stream, valueStart, eol);
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
    
    private static int searchEndOfLine(Stream stream) {
        for (int i=0; i<stream.available()-1; i++) {
            if (stream.get(i) == '\r' && stream.get(i+1) == '\n') {
                return i;
            }
        }
        return -1;
    }
    
    private static String getString(Stream stream, int begin, int end) {
        StringBuffer buffer = new StringBuffer(end-begin);
        for (int i=begin; i<end; i++) {
            buffer.append((char)stream.get(i));
        }
        return buffer.toString();
    }
    
    private static void insert(Stream stream, String s) throws IOException {
        byte[] b = s.getBytes("ascii");
        stream.insert(b, 0, b.length);
    }
    
    protected String processRequest(String request) throws IOException {
        return request;
    }
    
    protected String processHeader(String name, String value) throws IOException {
        return value;
    }
}
