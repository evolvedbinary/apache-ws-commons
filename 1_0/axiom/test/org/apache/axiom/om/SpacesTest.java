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

package org.apache.axiom.om;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SpacesTest extends XMLTestCase {
    private String filePath = "test-resources/xml/spaces.xml";


    private OMElement rootElement;

    protected void setUp() throws Exception {
    }

    public void testCData() throws Exception {
        checkOMConformance(new FileInputStream(filePath));
    }

    private void checkOMConformance(InputStream iStream) throws Exception {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();

            StAXOMBuilder staxOMBuilder = OMXMLBuilderFactory.
                    createStAXOMBuilder(OMAbstractFactory.getOMFactory(),
                            factory.createXMLStreamReader(
                                    iStream));
            staxOMBuilder.setDoDebug(true);
            rootElement = staxOMBuilder.getDocumentElement();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ((OMDocument) rootElement.getParent()).serialize(baos);

            InputSource resultXML = new InputSource(new InputStreamReader(
                    new ByteArrayInputStream(baos.toByteArray())));

            Document dom1 = newDocument(new InputSource(new FileInputStream(filePath)));
            Document dom2 = newDocument(resultXML);

            Diff diff = compareXML(dom1, dom2);
            assertXMLEqual(diff, true);
        } catch (XMLStreamException e) {
            fail(e.getMessage());
            throw new Exception(e);
        } catch (ParserConfigurationException e) {
            fail(e.getMessage());
            throw new Exception(e);
        } catch (SAXException e) {
            fail(e.getMessage());
            throw new Exception(e);
        } catch (IOException e) {
            fail(e.getMessage());
            throw new Exception(e);
        }
    }

    public Document newDocument(InputSource in)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(in);
    }


}
