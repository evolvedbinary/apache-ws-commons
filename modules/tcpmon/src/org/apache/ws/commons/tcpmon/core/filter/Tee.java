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
import java.io.OutputStream;

/**
 * A filter that copies all the data in the stream to a given {@link OutputStream}.
 * The data is written to the output stream before being processed by filters further
 * down the pipeline. The target output stream can be specified using the
 * {@link #Tee(OutputStream)} constructor or later using the
 * {@link #setOutputStream(OutputStream)} method. If data is submitted to the filter before the
 * output stream has been set, it will be held back until
 * {@link #setOutputStream(OutputStream)} has been called and the filter is
 * invoked again.
 */
public class Tee implements StreamFilter {
    private OutputStream out;
    
    public Tee() {
    }
    
    public Tee(OutputStream out) {
        setOutputStream(out);
    }

    public void setOutputStream(OutputStream out) {
        if (this.out != null) {
            throw new IllegalStateException("The output stream has already been set");
        }
        this.out = out;
    }

    public void invoke(Stream stream) throws IOException {
        if (out != null) {
            stream.readAll(out);
            stream.skipAll();
        }
    }
}
