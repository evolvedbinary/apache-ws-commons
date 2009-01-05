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
 * Class containing utility methods to work with streams.
 */
public class StreamUtil {
    private StreamUtil() {}
    
    /**
     * Search the available data in the stream for the first occurrence of
     * the end of line sequence (\r\n).
     * 
     * @param stream the stream to search in
     * @return the offset relative to the current position in the stream
     *         where the first occurrence of the EOL sequence has been found,
     *         or -1 if there is no EOL sequence in the available part of the
     *         stream
     */
    public static int searchEndOfLine(Stream stream) {
        for (int i=0; i<stream.available()-1; i++) {
            if (stream.get(i) == '\r' && stream.get(i+1) == '\n') {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Convert a part of a stream to a string, using the ASCII charset encoding.
     */
    public static String getAsciiString(Stream stream, int begin, int end) {
        StringBuffer buffer = new StringBuffer(end-begin);
        for (int i=begin; i<end; i++) {
            buffer.append((char)stream.get(i));
        }
        return buffer.toString();
    }
}
