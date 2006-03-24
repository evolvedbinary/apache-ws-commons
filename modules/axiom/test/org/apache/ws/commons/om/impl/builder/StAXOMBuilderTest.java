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

package org.apache.ws.commons.om.impl.builder;

import org.apache.ws.commons.om.AbstractTestCase;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMNode;
import org.apache.ws.commons.om.OMText;
import org.apache.ws.commons.om.impl.builder.StAXOMBuilder;
import org.apache.ws.commons.om.impl.llom.factory.OMXMLBuilderFactory;

import javax.xml.stream.XMLInputFactory;
import java.io.FileReader;
import java.util.Iterator;

public class StAXOMBuilderTest extends AbstractTestCase {
    StAXOMBuilder stAXOMBuilder;
    FileReader testFile;
    private OMElement rootElement;

    /**
     * Constructor.
     */
    public StAXOMBuilderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        testFile = new FileReader(getTestResourceFile("non_soap.xml"));
        stAXOMBuilder =
                OMXMLBuilderFactory.createStAXOMBuilder(
                        OMAbstractFactory.getSOAP11Factory(),
                        XMLInputFactory.newInstance().createXMLStreamReader(
                                testFile));
    }

    public void testGetRootElement() throws Exception {
        rootElement = stAXOMBuilder.getDocumentElement();
        assertTrue("Root element can not be null", rootElement != null);
        assertTrue(" Name of the root element is wrong",
                rootElement.getLocalName().equalsIgnoreCase("Root"));
        // get the first OMElement child
        OMNode omnode = rootElement.getFirstOMChild();
        while (omnode instanceof OMText) {
            omnode = omnode.getNextOMSibling();
        }
        Iterator children = ((OMElement) omnode).getChildren();
        int childrenCount = 0;
        while (children.hasNext()) {
            OMNode node = (OMNode) children.next();
            if (node instanceof OMElement)
                childrenCount++;
        }
        assertTrue(childrenCount == 5);
    }
}