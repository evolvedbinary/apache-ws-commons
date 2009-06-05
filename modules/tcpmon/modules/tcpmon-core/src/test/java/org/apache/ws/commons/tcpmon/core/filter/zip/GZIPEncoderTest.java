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

package org.apache.ws.commons.tcpmon.core.filter.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.ws.commons.tcpmon.core.filter.Pipeline;
import org.apache.ws.commons.tcpmon.core.filter.Tee;

public class GZIPEncoderTest extends TestCase {
    public void test() throws Exception {
        byte[] content = new byte[10000];
        Random random = new Random();
        random.nextBytes(content);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Pipeline pipeline = new Pipeline();
        pipeline.addFilter(new GZIPEncoder());
        pipeline.addFilter(new Tee(baos));
        OutputStream out = pipeline.getOutputStream();
        out.write(content);
        out.close();
        byte[] content2 = IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(baos.toByteArray())));
        assertTrue(Arrays.equals(content, content2));
    }
}
