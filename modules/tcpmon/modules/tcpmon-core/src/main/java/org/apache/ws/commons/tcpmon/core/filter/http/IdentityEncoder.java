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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.ws.commons.tcpmon.core.filter.Stream;
import org.apache.ws.commons.tcpmon.core.filter.StreamException;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;

public class IdentityEncoder implements StreamFilter {
    private final Headers headers;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    
    public IdentityEncoder(Headers headers) {
        this.headers = headers;
    }

    public boolean isReadOnly() {
        return false;
    }

    public void invoke(Stream stream) {
        try {
            stream.readAll(buffer);
        } catch (IOException ex) {
            throw new StreamException(ex);
        }
        stream.discard(stream.available()); // TODO: we should have a discardAll method
        if (stream.isEndOfStream()) {
            byte[] data = buffer.toByteArray();
            headers.set("Content-Length", String.valueOf(data.length));
            headers.writeTo(stream);
            stream.insert(data, 0, data.length);
        }
    }
}
