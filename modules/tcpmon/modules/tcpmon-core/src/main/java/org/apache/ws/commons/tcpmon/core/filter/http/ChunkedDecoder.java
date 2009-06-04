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
    private static final int STATE_START_CHUNK = 1;
    private static final int STATE_CHUNK = 2;
    private static final int STATE_TRAILER = 3;
    
    private int state = STATE_START_CHUNK;
    private int remaining; // bytes remaining in the current chunk

    public boolean process(Stream stream) {
        while (stream.available() > 0) {
            switch (state) {
                case STATE_CHUNK:
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
                            state = STATE_START_CHUNK;
                        } else {
                            throw new StreamException("Invalid chunked encoding");
                        }
                    }
                    break;
                case STATE_START_CHUNK: {
                    int eolIndex = StreamUtil.searchEndOfLine(stream);
                    if (eolIndex == -1) {
                        return false;
                    }
                    remaining = Integer.parseInt(StreamUtil.getAsciiString(stream, 0, eolIndex), 16);
                    stream.discard(eolIndex+2);
                    state = remaining == 0 ? STATE_TRAILER : STATE_CHUNK;
                    break;
                }
                case STATE_TRAILER: {
                    if (stream.available() < 2) {
                        return false;
                    }
                    if (stream.get(0) == '\r' && stream.get(1) == '\n') {
                        stream.discard(2);
                        return true;
                    } else {
                        throw new StreamException("Entity headers in trailer not supported");
                    }
                }
            }
        }
        return false;
    }
}
