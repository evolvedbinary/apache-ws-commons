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
 * Interface implemented by classes that process an entity in a stream,
 * where an entity is a self-delimiting part of a stream.
 */
public interface EntityProcessor {
    /**
     * Process data from the stream.
     * The contract of this method is the same as for {@link StreamFilter#invoke(Stream)},
     * except for the return value.
     * 
     * @param stream the stream containing the entity to process
     * @return <code>true</code> if the end of the entity has been
     *         reached. In this case, the implementation must set
     *         the current position in the stream to the position
     *         just after the last byte being part of the entity.
     *         <p>
     *         <code>false</code> otherwise
     */
    boolean process(Stream stream);
}
