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
 * A filter acting on a stream.
 */
public interface StreamFilter {
    /**
     * Invoke the filter. This method is called by {@link Pipeline}
     * when data is available for processing. The implementation can
     * modify the stream by discarding bytes from the stream and
     * inserting new data. If it doesn't wish to modify the stream,
     * it should skip the relevant parts, so that it will be processed
     * by the next filter in the pipeline.
     * <p>
     * An implementation is not required to process (skip or discard)
     * all the data available on each invocation. If after the invocation
     * of this method {@link Stream#available()} is non zero, the remaining
     * (unprocessed) data will be available again during the next invocation
     * of the filter. 
     * 
     * @param stream the stream to process
     * @throws IOException
     */
    void invoke(Stream stream) throws IOException;
}
