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

/**
 * Filter that replaces content in a stream.
 */
public class ReplaceFilter implements StreamFilter {
    private final byte[][] patterns;
    private final byte[][] replacements;
    
    public ReplaceFilter(byte[][] patterns, byte[][] replacements) {
        this.patterns = patterns;
        this.replacements = replacements;
    }
    
    public ReplaceFilter(byte[] pattern, byte[] replacement) {
        this(new byte[][] { pattern }, new byte[][] { replacement });
    }
    
    public ReplaceFilter(String pattern, String replacement, String charsetName) throws UnsupportedEncodingException {
        this(pattern.getBytes(charsetName), replacement.getBytes(charsetName));
    }

    public void invoke(Stream stream) {
        int p;
        while ((p = StreamUtil.search(stream, patterns)) != -1) {
            stream.discard(patterns[p].length);
            byte[] replacement = replacements[p];
            stream.insert(replacement, 0, replacement.length);
        }
    }
}
