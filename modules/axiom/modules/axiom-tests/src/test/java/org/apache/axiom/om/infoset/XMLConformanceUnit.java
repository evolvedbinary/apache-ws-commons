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

package org.apache.axiom.om.infoset;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class XMLConformanceUnit extends XMLTestCase implements EntityResolver {

    private String filePath;
    private File directory;

    public XMLConformanceUnit(String filePath, String testName) {
        super(testName);
        this.filePath = filePath;
        this.directory = new File(filePath).getParentFile();
    }

    public void testSingleFileConformance()
            throws Exception {
        OMElement rootElement;

        System.out.println("XML File:" + filePath);
        XMLInputFactory factory = XMLInputFactory.newInstance();
//        factory.setProperty("report-cdata-event", Boolean.TRUE);
        StAXOMBuilder staxOMBuilder = OMXMLBuilderFactory.
                createStAXOMBuilder(OMAbstractFactory.getOMFactory(),
                                    factory.createXMLStreamReader(
                                            new FileInputStream(filePath)));
        rootElement = staxOMBuilder.getDocumentElement();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ((OMDocument) rootElement.getParent()).serialize(baos);

        InputSource resultXML = new InputSource(new InputStreamReader(
                new ByteArrayInputStream(baos.toByteArray())));

        Document dom1 = newDocument(new InputSource(new FileReader(filePath)));
        Document dom2 = newDocument(resultXML);

        Diff diff = compareXML(dom1, dom2);
        assertXMLEqual(diff, true);
    }

    /**
     * Method newDocument
     *
     * @param in
     * @throws javax.xml.parsers.ParserConfigurationException
     *
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public Document newDocument(InputSource in)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setEntityResolver(this);
        return db.parse(in);
    }

    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        File f = new File(directory, systemId.substring(systemId.lastIndexOf('/')));
        return new InputSource(new FileInputStream(f));
    }

    public String getName() {
        String name = filePath;
        if (name.lastIndexOf("xmlconf") != -1)
            name = name.substring(name.lastIndexOf("xmlconf"));
        return name.replace('\\', '/');
    }
}
