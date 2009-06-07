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

import java.io.OutputStream;

import junit.framework.TestCase;

public class PipelineTest extends TestCase {
    private static class TestFilter implements StreamFilter {
        private boolean eos;
        
        public boolean isReadOnly() {
            return true;
        }
        
        public void invoke(Stream stream) {
            stream.skipAll();
            eos = stream.isEndOfStream();
        }

        public boolean isEos() {
            return eos;
        }
    }
    
    public void testEndOfStream() throws Exception {
        Pipeline pipeline = new Pipeline();
        TestFilter filter1 = new TestFilter();
        pipeline.addFilter(filter1);
        TestFilter filter2 = new TestFilter();
        pipeline.addFilter(filter2);
        OutputStream out = pipeline.getOutputStream();
        out.write(new byte[10]);
        out.close();
        assertTrue(filter1.isEos());
        assertTrue(filter2.isEos());
    }
}
