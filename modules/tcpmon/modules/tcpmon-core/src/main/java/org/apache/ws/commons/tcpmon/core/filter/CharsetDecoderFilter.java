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
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * Filter that decodes the stream to character data and sends it to a {@link Writer}.
 */
public class CharsetDecoderFilter implements StreamFilter {
    private final Writer writer;
    private final CharsetDecoder decoder;
    private final ByteBuffer inBuffer = ByteBuffer.allocate(64);
    private final CharBuffer outBuffer = CharBuffer.allocate(64);
    private int errorCount;
    private boolean reportErrors = true;

    public CharsetDecoderFilter(Writer writer, Charset charset) {
        this.writer = writer;
        decoder = charset.newDecoder();
    }
    
    public CharsetDecoderFilter(Writer writer, String charsetName) {
        this(writer, Charset.forName(charsetName));
    }

    public boolean isReadOnly() {
        return true;
    }

    public void invoke(Stream stream) {
        CoderResult coderResult;
        do {
            stream.skip(stream.read(inBuffer));
            inBuffer.flip();
            coderResult = decoder.decode(inBuffer, outBuffer, stream.isEndOfStream());
            if (coderResult.isError()) {
                errorCount++;
                if (reportErrors) {
                    if (errorCount > 5) {
                        stream.error("Too many input errors; stop reporting.");
                        reportErrors = false;
                    } else {
                        StringBuilder buffer = new StringBuilder();
                        buffer.append("Malformed input for charset ");
                        buffer.append(decoder.charset().name());
                        buffer.append(':');
                        for (int i=0; i<coderResult.length(); i++) {
                            buffer.append(' ');
                            buffer.append(Integer.toHexString(inBuffer.get() & 0xFF));
                        }
                        stream.error(buffer.toString());
                    }
                }
                if (!reportErrors) {
                    inBuffer.position(inBuffer.position() + coderResult.length());
                }
            }
            outBuffer.flip();
            try {
                writer.write(outBuffer.array(), outBuffer.position(), outBuffer.remaining());
            } catch (IOException ex) {
                throw new StreamException(ex);
            }
            outBuffer.clear();
            inBuffer.compact();
        } while (stream.available() > 0 || coderResult == CoderResult.OVERFLOW);
    }
}
