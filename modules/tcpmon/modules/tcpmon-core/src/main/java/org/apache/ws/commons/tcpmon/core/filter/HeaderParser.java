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

/**
 * Utility class to parse a set of HTTP/MIME/RFC822 headers from a stream.
 * 
 */
public class HeaderParser {
    private static final int STATE_UNKNOWN = 0;
    private static final int STATE_AVAILABLE = 1;
    private static final int STATE_NOT_AVAILABLE = 2;
    private static final int STATE_NO_MORE_HEADERS = 3;
    
    private final Stream stream;
    private int state = STATE_UNKNOWN;
    private int eol;
    private int colon;
    private int valueStart;

    public HeaderParser(Stream stream) {
        this.stream = stream;
    }
    
    private void updateState() {
        if (state != STATE_UNKNOWN) {
            return;
        }
        eol = StreamUtil.searchEndOfLine(stream);
        if (eol == -1) {
            state = STATE_NOT_AVAILABLE;
            return;
        }
        if (eol == 0) {
            state = STATE_NO_MORE_HEADERS;
            return;
        }
        for (int i=0; i<eol; i++) {
            if (stream.get(i) == ':') {
                colon = i;
                break;
            }
        }
        valueStart = colon+1;
        while (stream.get(valueStart) == ' ') {
            valueStart++;
        }
        state = STATE_AVAILABLE;
    }
    
    /**
     * Check whether a header is currently available from the underlying stream.
     * 
     * @return <code>true</code> if a header is available
     */
    public boolean available() {
        updateState();
        return state == STATE_AVAILABLE;
    }
    
    /**
     * Get the name of the current header.
     * 
     * @return the name of the header
     */
    public String getHeaderName() {
        if (state != STATE_AVAILABLE) {
            throw new IllegalStateException();
        }
        return StreamUtil.getAsciiString(stream, 0, colon);
    }
    
    /**
     * Get the value of the current header.
     * 
     * @return the value of the header
     */
    public String getHeaderValue() {
        if (state != STATE_AVAILABLE) {
            throw new IllegalStateException();
        }
        return StreamUtil.getAsciiString(stream, valueStart, eol);   
    }

    /**
     * Skip the current header. This will copy the current header
     * to the next filter.
     */
    public void skip() {
        if (state != STATE_AVAILABLE && state != STATE_NO_MORE_HEADERS) {
            throw new IllegalStateException();
        }
        stream.skip(eol+2);
        state = STATE_UNKNOWN;
    }
    
    /**
     * Discard the current header.
     */
    public void discard() {
        if (state != STATE_AVAILABLE && state != STATE_NO_MORE_HEADERS) {
            throw new IllegalStateException();
        }
        stream.discard(eol+2);
        state = STATE_UNKNOWN;
    }
    
    /**
     * Insert a new header in the stream.
     * 
     * @param name the name of the header
     * @param value the value of the header
     */
    public void insert(String name, String value) {
        StreamUtil.insertAsciiString(stream, name);
        StreamUtil.insertAsciiString(stream, ": ");
        StreamUtil.insertAsciiString(stream, value);
        StreamUtil.insertAsciiString(stream, "\r\n");
    }
    
    /**
     * Change the value of the current header. This will advance the position
     * to the next header.
     * 
     * @param newValue
     */
    public void change(String newValue) {
        if (state != STATE_AVAILABLE) {
            throw new IllegalStateException();
        }
        stream.skip(valueStart);
        stream.discard(eol-valueStart);
        StreamUtil.insertAsciiString(stream, newValue);
        stream.skip(2);
        state = STATE_UNKNOWN;
    }
    
    public boolean noMoreHeaders() {
        updateState();
        return state == STATE_NO_MORE_HEADERS;
    }
}
