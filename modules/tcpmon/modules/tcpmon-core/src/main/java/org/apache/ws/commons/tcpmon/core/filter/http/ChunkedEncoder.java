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

import org.apache.ws.commons.tcpmon.core.filter.Stream;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;
import org.apache.ws.commons.tcpmon.core.filter.StreamUtil;

public class ChunkedEncoder implements StreamFilter {
    private Headers headers;
    
    public ChunkedEncoder(Headers headers) {
        this.headers = headers;
    }

    public boolean isReadOnly() {
        return false;
    }

    public void invoke(Stream stream) {
        if (headers != null) {
            headers.writeTo(stream);
            headers = null;
        }
        int av = stream.available();
        if (av > 0 || stream.isEndOfStream()) {
            StreamUtil.insertAsciiString(stream, "\r\n");
            StreamUtil.insertAsciiString(stream, Integer.toString(av, 16));
            StreamUtil.insertAsciiString(stream, "\r\n");
            if (av == 0) {
                StreamUtil.insertAsciiString(stream, "\r\n");
            } else {
                stream.skipAll();
            }
        }
    }
}
