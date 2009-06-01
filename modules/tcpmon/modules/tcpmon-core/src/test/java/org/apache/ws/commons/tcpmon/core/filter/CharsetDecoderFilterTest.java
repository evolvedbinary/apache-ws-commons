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

import java.io.StringWriter;

import junit.framework.TestCase;

public class CharsetDecoderFilterTest extends TestCase {
    public void testMalformed() {
        try {
            TestUtil.filter(new CharsetDecoderFilter(new StringWriter(), "UTF-8"),
                            new byte[] { (byte)255, 0 });
            fail("Expected exception");
        } catch (StreamException ex) {
            // OK
        }
    }
}
