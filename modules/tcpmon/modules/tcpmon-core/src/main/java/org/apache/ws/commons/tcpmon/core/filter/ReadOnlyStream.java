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
import java.nio.ByteBuffer;

public class ReadOnlyStream implements Stream {
    private final Stream parent;

    public ReadOnlyStream(Stream parent) {
        this.parent = parent;
    }

    public int available() {
        return parent.available();
    }

    public byte discard() {
        // Ignore this and skip instead
        return parent.skip();
    }

    public void discard(int len) {
        // Ignore this and skip instead
        parent.skip(len);
    }

    public int get() {
        return parent.get();
    }

    public int get(int lookahead) {
        return parent.get(lookahead);
    }

    public void insert(byte b) {
        // Ignore this
    }

    public void insert(byte[] buffer, int offset, int length) {
        // Ignore this
    }

    public void insert(ByteBuffer buffer) {
        // Ignore this
    }

    public boolean isEndOfStream() {
        return parent.isEndOfStream();
    }

    public int read(byte[] buffer, int offset, int length) {
        return parent.read(buffer, offset, length);
    }

    public int read(ByteBuffer buffer) {
        return parent.read(buffer);
    }

    public void readAll(OutputStream out) throws IOException {
        parent.readAll(out);
    }

    public byte skip() {
        return parent.skip();
    }

    public void skip(int len) {
        parent.skip(len);
    }

    public void skipAll() {
        parent.skipAll();
    }

    public void pushFilter(StreamFilter filter) {
        parent.pushFilter(filter);
    }

    public void popFilter() {
        parent.popFilter();
    }

    public void error(String description) {
        parent.error(description);
    }
}
