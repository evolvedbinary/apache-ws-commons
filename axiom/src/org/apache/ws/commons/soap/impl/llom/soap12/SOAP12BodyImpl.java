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

package org.apache.ws.commons.soap.impl.llom.soap12;

import org.apache.ws.commons.om.OMException;
import org.apache.ws.commons.om.OMXMLParserWrapper;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFault;
import org.apache.ws.commons.soap.SOAPProcessingException;
import org.apache.ws.commons.soap.impl.llom.SOAPBodyImpl;

public class SOAP12BodyImpl extends SOAPBodyImpl {
    /**
     * @param envelope
     */
    public SOAP12BodyImpl(SOAPEnvelope envelope) throws SOAPProcessingException {
        super(envelope);
    }

    /**
     * Constructor SOAPBodyImpl
     *
     * @param envelope
     * @param builder
     */
    public SOAP12BodyImpl(SOAPEnvelope envelope, OMXMLParserWrapper builder) {
        super(envelope, builder);
    }

    public SOAPFault addFault(Exception e) throws OMException {
        SOAPFault soapFault = new SOAP12FaultImpl(this, e);
        return soapFault;
    }
}
