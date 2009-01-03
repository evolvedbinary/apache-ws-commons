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
 * Filter that extracts the first line of a request, up to a given
 * maximum length.
 */
public abstract class RequestLineExtractor implements StreamFilter {
    private final byte[] buffer;
    private int length;
    private boolean done;
    
    public RequestLineExtractor(int maxLength) {
        this.buffer = new byte[maxLength];
    }
    
    public void invoke(Stream stream) throws IOException {
        if (done) {
            stream.skipAll();
        } else {
            while (!done && stream.available() > 0) {
                byte b = stream.skip();
                if (b == '\n') {
                    done = true;
                } else {
                    buffer[length++] = b;
                    if (length == buffer.length) {
                        done = true;
                    }
                }
            }
            if (done) {
                stream.skipAll();
                done(new String(buffer, 0, length, "ascii"));
            }
        }
    }
    
    protected abstract void done(String requestLine);
}
