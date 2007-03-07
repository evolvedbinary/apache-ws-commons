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

package org.apache.axiom.soap;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public abstract class SOAPTestCase extends AbstractTestCase {
    protected SOAPFactory soap11Factory;
    protected SOAPFactory soap12Factory;
    protected OMFactory omFactory;

    protected SOAPEnvelope soap11Envelope;
    protected SOAPEnvelope soap12Envelope;

    protected SOAPEnvelope soap11EnvelopeWithParser;
    protected SOAPEnvelope soap12EnvelopeWithParser;

    protected static final String SOAP11_FILE_NAME = "soap/soap11/soap11message.xml";
    protected static final String SOAP12_FILE_NAME = "soap/soap12message.xml";
    private Log log = LogFactory.getLog(getClass());

    /** @param testName  */
    public SOAPTestCase(String testName) {
        super(testName);
        soap11Factory = OMAbstractFactory.getSOAP11Factory();
        soap12Factory = OMAbstractFactory.getSOAP12Factory();
        omFactory = OMAbstractFactory.getOMFactory();
    }

    protected void setUp() throws Exception {
        super.setUp();

        soap11Envelope = soap11Factory.createSOAPEnvelope();
        soap12Envelope = soap12Factory.createSOAPEnvelope();

        soap11EnvelopeWithParser =
                (SOAPEnvelope) this.getSOAPBuilder(SOAP11_FILE_NAME)
                        .getDocumentElement();
        soap12EnvelopeWithParser =
                (SOAPEnvelope) this.getSOAPBuilder(SOAP12_FILE_NAME)
                        .getDocumentElement();
    }

    protected StAXSOAPModelBuilder getSOAPBuilder(String fileName) {
        XMLStreamReader parser = null;
        try {
            parser =
                    XMLInputFactory.newInstance().createXMLStreamReader(
                            new FileReader(getTestResourceFile(fileName)));
        } catch (XMLStreamException e) {
            log.info(e.getMessage());
        } catch (FileNotFoundException e) {
            log.info(e.getMessage());
        }
        return new StAXSOAPModelBuilder(parser, null);
    }

}
