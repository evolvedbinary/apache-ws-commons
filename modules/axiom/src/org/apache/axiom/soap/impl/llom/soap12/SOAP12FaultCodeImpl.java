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
import org.apache.ws.commons.soap.SOAPFault;
import org.apache.ws.commons.soap.SOAPFaultSubCode;
import org.apache.ws.commons.soap.SOAPFaultValue;
import org.apache.ws.commons.soap.SOAPProcessingException;
import org.apache.ws.commons.soap.impl.llom.SOAPFaultCodeImpl;

public class SOAP12FaultCodeImpl extends SOAPFaultCodeImpl {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */

    public SOAP12FaultCodeImpl(SOAPFactory factory) {
        super(factory.getNamespace(), factory);
    }

    /**
     * Constructor OMElementImpl
     *
     * @param parent
     * @param builder
     */
    public SOAP12FaultCodeImpl(SOAPFault parent, OMXMLParserWrapper builder, SOAPFactory factory) {
        super(parent, builder, factory);
    }

    /**
     * @param parent
     */
    public SOAP12FaultCodeImpl(SOAPFault parent, SOAPFactory factory) throws SOAPProcessingException {
        super(parent, true, factory);
    }


    public void setSubCode(SOAPFaultSubCode subCode) throws SOAPProcessingException {
        if (!(subCode instanceof SOAP12FaultSubCodeImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Fault Sub Code. But received some other implementation");
        }
        super.setSubCode(subCode);
    }

    public void setValue(SOAPFaultValue value) throws SOAPProcessingException {
        if (!(value instanceof SOAP12FaultValueImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Fault Value. But received some other implementation");
        }
        super.setValue(value);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP12FaultImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Fault as the parent. But received some other implementation");
        }
    }
}
