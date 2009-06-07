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

package org.apache.ws.commons.tcpmon.core.engine;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

public class GZIPCompressedEntity extends HttpEntityWrapper {
    public GZIPCompressedEntity(HttpEntity entity) {
        super(entity);
    }

    @Override
    public Header getContentEncoding() {
        return new BasicHeader(HTTP.CONTENT_ENCODING, "gzip");
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public boolean isChunked() {
        return true;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        OutputStream compressed = new GZIPOutputStream(out);
        IOUtils.copy(wrappedEntity.getContent(), compressed);
        compressed.close();
    }
}
