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

package org.apache.ws.commons.tcpmon.core.filter.zip;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.ws.commons.tcpmon.core.filter.Stream;
import org.apache.ws.commons.tcpmon.core.filter.StreamException;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;
import org.apache.ws.commons.tcpmon.core.filter.StreamUtil;

public class GZIPDecoder implements StreamFilter {
    private static final int STATE_HEADER = 0;
    private static final int STATE_EXTRA = 1;
    private static final int STATE_NAME = 2;
    private static final int STATE_COMMENT = 3;
    private static final int STATE_CRC16 = 4;
    private static final int STATE_BLOCKS = 5;
    private static final int STATE_TRAILER = 6;
    private static final int STATE_FINISHED = 7;
    
    private static final int FHCRC = 2;
    private static final int FEXTRA = 4;
    private static final int FNAME = 8;
    private static final int FCOMMENT = 16;
    
    private final Inflater inflater;
    private final byte[] inBuffer = new byte[256];
    private final byte[] outBuffer = new byte[256];
    private int state = STATE_HEADER;
    private int flags;
    
    public GZIPDecoder() {
        inflater = new Inflater(true);
    }

    public void invoke(Stream stream) {
        switch (state) {
            case STATE_HEADER: {
                if (stream.available() < 10) {
                    return;
                }
                if (stream.get(0) != 0x1f || stream.get(1) != 0x8b) {
                    throw new StreamException("Not in GZIP format");
                }
                if (stream.get(2) != 8) {
                    throw new StreamException("Unsupported compression method");
                }
                flags = stream.get(3);
                stream.discard(10);
                state = STATE_EXTRA;
            }
            case STATE_EXTRA:
                if ((flags & FEXTRA) != 0) {
                    if (stream.available() < 2) {
                        return;
                    }
                    int xlen = StreamUtil.readUShort(stream);
                    if (stream.available() < xlen+2) {
                        return;
                    }
                    stream.discard(xlen+2);
                }
                state = STATE_NAME;
            case STATE_NAME:
                if ((flags & FNAME) != 0) {
                    while (true) {
                        if (stream.available() == 0) {
                            return;
                        }
                        if (stream.discard() == 0) {
                            break;
                        }
                    }
                }
                state = STATE_COMMENT;
            case STATE_COMMENT:
                if ((flags & FCOMMENT) != 0) {
                    while (true) {
                        if (stream.available() == 0) {
                            return;
                        }
                        if (stream.discard() == 0) {
                            break;
                        }
                    }
                }
                state = STATE_CRC16;
            case STATE_CRC16:
                if ((flags & FHCRC) != 0) {
                    if (stream.available() < 2) {
                        return;
                    }
                    stream.discard(2);
                }
                state = STATE_BLOCKS;
            case STATE_BLOCKS:
                while (stream.available() > 0 && !inflater.finished()) {
                    int in = Math.min(inBuffer.length, stream.available());
                    stream.read(inBuffer, 0, in);
                    inflater.setInput(inBuffer, 0, in);
                    while (true) {
                        int out;
                        try {
                            out = inflater.inflate(outBuffer);
                        } catch (DataFormatException ex) {
                            throw new StreamException(ex);
                        }
                        if (out > 0) {
                            stream.insert(outBuffer, 0, out);
                        } else {
                            break;
                        }
                    }
                    stream.discard(in-inflater.getRemaining());
                }
                if (!inflater.finished()) {
                    return;
                }
                state = STATE_TRAILER;
            case STATE_TRAILER:
                if (stream.available() < 8) {
                    return;
                }
                stream.discard(8); // TODO: we should check the CRC
                state = STATE_FINISHED;
            case STATE_FINISHED:
                if (stream.available() > 0) {
                    throw new StreamException("Extra content encountered");
                }
        }
    }
}
