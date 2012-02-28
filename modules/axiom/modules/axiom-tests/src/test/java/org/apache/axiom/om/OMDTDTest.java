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

package org.apache.axiom.om;

import javax.xml.stream.XMLStreamException;

import java.io.ByteArrayOutputStream;

public class OMDTDTest extends AbstractTestCase {

    private OMDocument document;

    protected void setUp() throws Exception {
        try {
            OMXMLParserWrapper stAXOMBuilder = OMXMLBuilderFactory.createOMBuilder(getTestResource("dtd.xml"));
            document = stAXOMBuilder.getDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void tearDown() throws Exception {
        document.close(false);
    }

    public void testDTDSerialization() {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.serialize(baos);
            String serializedString = new String(baos.toByteArray());

            assertTrue(serializedString.indexOf("<!ENTITY foo \"bar\">") > -1);
            assertTrue(serializedString.indexOf("<!ENTITY bar \"foo\">") > -1);
            assertTrue(
                    serializedString.indexOf("<feed xmlns=\"http://www.w3.org/2005/Atom\">") > -1);
        } catch (XMLStreamException e) {
            fail("Bug in serializing OMDocuments which have DTDs, text and a document element");
        }
    }
}
