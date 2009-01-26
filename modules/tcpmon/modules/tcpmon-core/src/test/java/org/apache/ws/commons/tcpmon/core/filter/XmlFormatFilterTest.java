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

public class XmlFormatFilterTest extends TestCase {
    private static void assertFormat(String expected, String in) {
        assertEquals(expected, TestUtil.filter(new XmlFormatFilter(2), in));
    }
    
    public void test1() {
        assertFormat("<root>\n  <a/>\n</root>", "<root><a/></root>");
    }

    public void test2() {
        assertFormat("<?xml version=\"1.0\"?>\n<root>\n  <a/>\n</root>",
                     "<?xml version=\"1.0\"?><root><a/></root>");
    }

    public void test3() {
        assertFormat("<root>\n  <a>test</a>\n</root>", "<root><a>test</a></root>");
    }
    
    public void test4() {
        assertFormat("<root>\n  <child>\n    <a/>\n  </child>\n</root>",
                     "<root><child><a/></child></root>");
    }

    public void test5() {
        assertFormat("<root>\n  <child>\n    <a>test</a>\n  </child>\n</root>",
                     "<root><child><a>test</a></child></root>");
    }
}
