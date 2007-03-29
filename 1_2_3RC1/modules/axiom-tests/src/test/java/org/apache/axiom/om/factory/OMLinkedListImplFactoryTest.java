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

package org.apache.axiom.om.factory;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMTestUtils;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** User: Eran Chinthaka (eran.chinthaka@gmail.com) Date: Feb 8, 2005 Time: 11:06:09 AM */
public class OMLinkedListImplFactoryTest extends AbstractTestCase {

    private Log log = LogFactory.getLog(getClass());

    public OMLinkedListImplFactoryTest(String testName) {
        super(testName);
    }

    SOAPFactory omFactory;
    OMNamespace namespace;
    String nsUri = "http://www.apache.org/~chinthaka";
    String nsPrefix = "myhome";

    protected void setUp() throws Exception {
        super.setUp();
        omFactory = OMAbstractFactory.getSOAP11Factory();
        namespace = omFactory.createOMNamespace(nsUri, nsPrefix);
    }

    public void testCreateOMElementWithNoBuilder() {
        OMElement omElement = omFactory.createOMElement("chinthaka",
                                                        namespace);
        assertTrue(
                "Programatically created OMElement should have done = true ",
                omElement.isComplete());

    }

    public void testCreateOMElement() {
        try {
            OMXMLParserWrapper omBuilder = OMTestUtils.getOMBuilder(
                    getTestResourceFile("soap/whitespacedMessage.xml"));
            OMElement documentElement = omBuilder.getDocumentElement();
            OMElement child = omFactory.createOMElement("child",
                                                        namespace,
                                                        documentElement,
                                                        omBuilder);
            assertTrue(
                    "OMElement with a builder should start with done = false ",
                    !child.isComplete());
            assertTrue("This OMElement must have a builder ",
                       child.getBuilder() instanceof OMXMLParserWrapper);

        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public void testCreateOMNamespace() {
        assertTrue("OMNamespace uri not correct",
                   nsUri.equals(
                           namespace.getNamespaceURI()));   // here equalsIgnoreCase should not be used as case does matter
        assertTrue("OMNamespace prefix not correct",
                   nsPrefix.equals(
                           namespace.getPrefix()));  // here equalsIgnoreCase should not be used as case does matter
    }

    public void testCreateText() {
        OMElement omElement = omFactory.createOMElement("chinthaka",
                                                        namespace);
        String text = "sampleText";
        OMText omText = omFactory.createOMText(omElement, text);
        assertTrue("Programatically created OMText should have done = true ",
                   omText.isComplete());
        assertTrue(
                "Programatically created OMText should have correct text value ",
                text.equals(omText.getText()));

    }

    public void testCreateSOAPBody() {
        try {
            OMXMLParserWrapper omBuilder = OMTestUtils.getOMBuilder(
                    getTestResourceFile("soap/minimalMessage.xml"));
            SOAPEnvelope soapEnvelope = (SOAPEnvelope) omBuilder.getDocumentElement();
            SOAPBody soapBodyOne = omFactory.createSOAPBody(soapEnvelope);
            assertTrue(
                    "Programatically created SOAPBody should have done = true ",
                    soapBodyOne.isComplete());
            soapBodyOne.detach();
            SOAPBody soapBodyTwo = omFactory.createSOAPBody(soapEnvelope,
                                                            omBuilder);
            assertTrue(
                    "SOAPBody with a builder should start with done = false ",
                    !soapBodyTwo.isComplete());
            assertTrue("This SOAPBody must have a builder ",
                       soapBodyTwo.getBuilder() instanceof OMXMLParserWrapper);


        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public void testCreateSOAPEnvelope() {
        try {
            omFactory.createOMNamespace(
                    SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                    SOAPConstants.SOAP_DEFAULT_NAMESPACE_PREFIX);
            SOAPEnvelope soapEnvelopeTwo = omFactory.createSOAPEnvelope();
            assertTrue(
                    "Programatically created SOAPEnvelope should have done = true ",
                    soapEnvelopeTwo.isComplete());
            SOAPEnvelope soapEnvelope = omFactory.createSOAPEnvelope(
                    OMTestUtils.getOMBuilder(
                            getTestResourceFile("soap/minimalMessage.xml")));
            assertTrue(
                    "SOAPEnvelope with a builder should start with done = false ",
                    !soapEnvelope.isComplete());
            assertTrue("This SOAPEnvelope must have a builder ",
                       soapEnvelope.getBuilder() instanceof OMXMLParserWrapper);


        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public void testCreateSOAPHeader() {
        try {
            OMXMLParserWrapper omBuilder = OMTestUtils.getOMBuilder(
                    getTestResourceFile("soap/minimalMessage.xml"));
            SOAPEnvelope soapEnvelope = (SOAPEnvelope) omBuilder.getDocumentElement();
            SOAPHeader soapHeader = omFactory.createSOAPHeader(soapEnvelope);
            assertTrue(
                    "Programatically created SOAPHeader should have done = true ",
                    soapHeader.isComplete());
            soapHeader.detach();
            SOAPHeader soapHeaderTwo = omFactory.createSOAPHeader(soapEnvelope,
                                                                  omBuilder);
            assertTrue(
                    "SOAPHeader with a builder should start with done = false ",
                    !soapHeaderTwo.isComplete());
            assertTrue("This SOAPHeader must have a builder ",
                       soapHeaderTwo.getBuilder() instanceof OMXMLParserWrapper);


        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public void testCreateSOAPHeaderBlock() {
        try {
            OMXMLParserWrapper omBuilder = OMTestUtils.getOMBuilder(
                    getTestResourceFile("soap/soapmessage.xml"));
            SOAPEnvelope soapEnvelope = (SOAPEnvelope) omBuilder.getDocumentElement();
            SOAPHeader soapHeader = soapEnvelope.getHeader();
            SOAPHeaderBlock soapHeaderBlock = omFactory.createSOAPHeaderBlock(
                    "soapHeaderBlockOne", namespace, soapHeader);
            assertTrue(
                    "Programatically created SOAPHeaderBlock should have done = true ",
                    soapHeaderBlock.isComplete());
            SOAPHeaderBlock soapHeaderBlockTwo = omFactory.createSOAPHeaderBlock(
                    "soapHeaderBlockOne", namespace, soapHeader, omBuilder);
            assertTrue(
                    "SOAPHeaderBlock with a builder should start with done = false ",
                    !soapHeaderBlockTwo.isComplete());
            assertTrue("This SOAPHeaderBlock must have a builder ",
                       soapHeaderBlockTwo.getBuilder() instanceof OMXMLParserWrapper);


        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public void testCreateSOAPFault() {
        try {
            OMXMLParserWrapper omBuilder = OMTestUtils.getOMBuilder(
                    getTestResourceFile("soap/soapmessage.xml"));
            SOAPEnvelope soapEnvelope = (SOAPEnvelope) omBuilder.getDocumentElement();
            SOAPBody soapBody = soapEnvelope.getBody();
            SOAPFault soapFault = omFactory.createSOAPFault(soapBody,
                                                            new Exception(" this is just a test "));
            assertTrue(
                    "Programatically created SOAPFault should have done = true ",
                    soapFault.isComplete());
            soapFault.detach();
            SOAPFault soapFaultTwo = omFactory.createSOAPFault(soapBody,
                                                               omBuilder);
            assertTrue(
                    "SOAPFault with a builder should start with done = false ",
                    !soapFaultTwo.isComplete());
            assertTrue("This SOAPFault must have a builder ",
                       soapFaultTwo.getBuilder() instanceof OMXMLParserWrapper);


        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }


}
