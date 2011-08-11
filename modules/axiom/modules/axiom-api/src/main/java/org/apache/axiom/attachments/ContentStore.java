/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axiom.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * Stores the content of a MIME part.
 */
abstract class ContentStore {
    abstract InputStream getInputStream() throws IOException;

    /**
     * Get a {@link DataSource} implementation specific for this buffering strategy.
     * @param contentType TODO
     * 
     * @return the {@link DataSource} implementation or <code>null</code> if a default
     *         {@link DataSource} implementation should be used
     */
    abstract DataSource getDataSource(String contentType);
    
    abstract void writeTo(OutputStream out) throws IOException;

    abstract long getSize();

    abstract void destroy() throws IOException;
}
