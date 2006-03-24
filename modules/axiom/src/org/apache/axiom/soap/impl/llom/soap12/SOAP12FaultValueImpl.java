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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPProcessingException;
import org.apache.ws.commons.soap.impl.llom.SOAPFaultValueImpl;


public class SOAP12FaultValueImpl extends SOAPFaultValueImpl {
    
    public SOAP12FaultValueImpl(OMElement parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, factory);
    }

    public SOAP12FaultValueImpl(SOAPFactory factory)
            throws SOAPProcessingException {
        super(factory.getNamespace(), factory);
    }

    public SOAP12FaultValueImpl(OMElement parent, OMXMLParserWrapper builder,
            SOAPFactory factory) {
        super(parent, builder, factory);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!((parent instanceof SOAP12FaultSubCodeImpl) ||
                (parent instanceof SOAP12FaultCodeImpl))) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP FaultSubCode or SOAP FaultCode as the parent. But received some other implementation");
        }
    }
}
