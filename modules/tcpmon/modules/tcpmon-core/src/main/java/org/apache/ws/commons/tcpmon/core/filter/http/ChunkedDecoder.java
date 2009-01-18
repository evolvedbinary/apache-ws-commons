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

import org.apache.ws.commons.tcpmon.core.filter.EntityProcessor;
import org.apache.ws.commons.tcpmon.core.filter.Stream;
import org.apache.ws.commons.tcpmon.core.filter.StreamException;
import org.apache.ws.commons.tcpmon.core.filter.StreamUtil;

/**
 * Entity processor that processes HTTP chunked transfer encoding.
 */
public class ChunkedDecoder implements EntityProcessor {
    private int remaining = -1; // bytes remaining in the current chunk

    public boolean process(Stream stream) {
        while (stream.available() > 0) {
            if (remaining > 0) {
                int c = Math.min(stream.available(), remaining);
                stream.skip(c);
                remaining -= c;
            } else if (remaining == 0) {
                if (stream.available() < 2) {
                    return false;
                }
                if (stream.get(0) == '\r' && stream.get(1) == '\n') {
                    stream.discard(2);
                    remaining = -1;
                } else {
                    throw new StreamException("Invalid chunked encoding");
                }
            } else {
                int eolIndex = StreamUtil.searchEndOfLine(stream);
                if (eolIndex == -1) {
                    return false;
                }
                remaining = Integer.parseInt(StreamUtil.getAsciiString(stream, 0, eolIndex), 16);
                stream.discard(eolIndex+2);
                if (remaining == 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
