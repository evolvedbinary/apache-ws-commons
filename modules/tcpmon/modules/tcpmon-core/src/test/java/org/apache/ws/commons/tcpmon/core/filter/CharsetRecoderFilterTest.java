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

import junit.framework.TestCase;

public class CharsetRecoderFilterTest extends TestCase {
    private static final String latin1TestString;
    
    static {
        StringBuffer buffer = new StringBuffer();
        for (int i=0; i<20; i++) {
            for (char c=32; c<256; c++) {
                buffer.append(c);
            }
        }
        latin1TestString = buffer.toString();
    }
    
    private void test(String s, String fromCharset, String toCharset) throws Exception {
        byte[] in = s.getBytes(fromCharset);
        byte[] out = TestUtil.filter(new CharsetRecoderFilter(fromCharset, toCharset), in);
        assertEquals(s, new String(out, toCharset));
    }
    
    public void testLatin1ToUTF8() throws Exception {
        test(latin1TestString, "iso-8859-1", "utf-8");
    }

    public void testUTF8ToLatin1() throws Exception {
        test(latin1TestString, "utf-8", "iso-8859-1");
    }
}
