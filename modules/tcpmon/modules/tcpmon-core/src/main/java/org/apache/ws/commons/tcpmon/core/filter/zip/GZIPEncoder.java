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

import java.util.zip.CRC32;
import java.util.zip.Deflater;

import org.apache.ws.commons.tcpmon.core.filter.Stream;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;
import org.apache.ws.commons.tcpmon.core.filter.StreamUtil;

public class GZIPEncoder implements StreamFilter {
    private static final byte[] header = { 0x1f, (byte)0x8b, 8, 0, 0, 0, 0, 0, 0, -1 };
    
    private final Deflater deflater;
    private final CRC32 crc = new CRC32();
    private final byte[] inBuffer = new byte[256];
    private final byte[] outBuffer = new byte[256];
    private boolean isStart = true;
    
    public GZIPEncoder() {
        this.deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
    }

    public void invoke(Stream stream) {
        if (isStart) {
            stream.insert(header, 0, 10);
            isStart = false;
        }
        do {
            int in = Math.min(inBuffer.length, stream.available());
            stream.read(inBuffer, 0, in);
            deflater.setInput(inBuffer, 0, in);
            stream.discard(in);
            if (stream.isEndOfStream()) {
                deflater.finish();
            }
            crc.update(inBuffer, 0, in);
            while (true) {
                int out = deflater.deflate(outBuffer);
                if (out > 0) {
                    stream.insert(outBuffer, 0, out);
                } else {
                    break;
                }
            }
        } while (stream.available() > 0);
        if (stream.isEndOfStream()) {
            // Write trailer
            StreamUtil.insertInt(stream, (int)crc.getValue());
            StreamUtil.insertInt(stream, deflater.getTotalIn());
        }
    }
}
