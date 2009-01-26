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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * Filter that changes the charset encoding of a stream.
 */
public class CharsetRecoderFilter implements StreamFilter {
    private final CharsetDecoder decoder;
    private final CharsetEncoder encoder;
    private final ByteBuffer inBuffer = ByteBuffer.allocate(64);
    private final CharBuffer charBuffer = CharBuffer.allocate(64);
    private final ByteBuffer outBuffer = ByteBuffer.allocate(64);
    
    public CharsetRecoderFilter(Charset fromCharset, Charset toCharset) {
        decoder = fromCharset.newDecoder();
        encoder = toCharset.newEncoder();
    }
    
    public CharsetRecoderFilter(String fromCharset, String toCharset) {
        this(Charset.forName(fromCharset), Charset.forName(toCharset));
    }
    
    public void invoke(Stream stream) {
        CoderResult inResult;
        do {
            stream.discard(stream.read(inBuffer));
            inBuffer.flip();
            inResult = decoder.decode(inBuffer, charBuffer, stream.isEndOfStream());
            charBuffer.flip();
            CoderResult outResult;
            do {
                outResult = encoder.encode(charBuffer, outBuffer, stream.isEndOfStream() && inResult == CoderResult.UNDERFLOW);
                outBuffer.flip();
                stream.insert(outBuffer);
                outBuffer.compact();
            } while (outResult == CoderResult.OVERFLOW);
            inBuffer.compact();
            charBuffer.compact();
        } while (stream.available() > 0 || inResult == CoderResult.OVERFLOW);
    }
}
