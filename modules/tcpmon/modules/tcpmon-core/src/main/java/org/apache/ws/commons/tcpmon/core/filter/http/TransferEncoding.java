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
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;

/**
 * Represents a particular transfer encoding as specified by the <tt>Transfer-Encoding</tt>
 * header.
 */
public interface TransferEncoding {
    /**
     * Create a decoder for the transfer encoding.
     * 
     * @param headers The HTTP headers of the request or response. The implementation may
     *                use this object to extract additional information.
     * @return a stream filter able to decode the transfer encoding
     */
    EntityProcessor createDecoder(Headers headers);

    /**
     * Create an encoder for the transfer encoding.
     * 
     * @param headers The HTTP headers for the request or response to encode. It is the
     *                responsibility of the implementation to write these headers to the
     *                stream. This gives the implementation the opportunity to modify
     *                some headers.
     * @return a stream filter able to encode the transfer encoding
     */
    StreamFilter createEncoder(Headers headers);
}
