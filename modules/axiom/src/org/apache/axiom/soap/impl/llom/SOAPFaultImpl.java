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

package org.apache.axiom.soap.impl.llom;

import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.OMNodeEx;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.impl.llom.OMSerializerUtil;
import org.apache.axiom.om.impl.serialize.StreamWriterToContentHandlerConverter;
import org.apache.axiom.soap.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

/**
 * Class SOAPFaultImpl
 */
public abstract class SOAPFaultImpl extends SOAPElement
        implements SOAPFault, OMConstants {

    protected Exception e;

    protected SOAPFaultImpl(OMNamespace ns, SOAPFactory factory) {
        super(SOAPConstants.SOAPFAULT_LOCAL_NAME, ns, factory);
    }

    /**
     * Constructor SOAPFaultImpl
     *
     * @param parent
     * @param e
     */
    public SOAPFaultImpl(SOAPBody parent, Exception e, SOAPFactory factory) throws SOAPProcessingException {
        super(parent, SOAPConstants.SOAPFAULT_LOCAL_NAME, true, factory);
        setException(e);
    }

    public void setException(Exception e) {
        this.e = e;
        putExceptionToSOAPFault(e);
    }

    public SOAPFaultImpl(SOAPBody parent, SOAPFactory factory) throws SOAPProcessingException {
        super(parent, SOAPConstants.SOAPFAULT_LOCAL_NAME, true, factory);
    }

    /**
     * Constructor SOAPFaultImpl
     *
     * @param parent
     * @param builder
     */
    public SOAPFaultImpl(SOAPBody parent, OMXMLParserWrapper builder,
            SOAPFactory factory) {
        super(parent, SOAPConstants.SOAPFAULT_LOCAL_NAME, builder, factory);
    }


    protected abstract SOAPFaultDetail getNewSOAPFaultDetail(SOAPFault fault) throws SOAPProcessingException;

    // --------------- Getters and Settors --------------------------- //

    public void setCode(SOAPFaultCode soapFaultCode) throws SOAPProcessingException {
        setNewElement(getCode(), soapFaultCode);
    }

    public SOAPFaultCode getCode() {
        return (SOAPFaultCode) this.getChildWithName(
                SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME);
    }

    public void setReason(SOAPFaultReason reason) throws SOAPProcessingException {
        setNewElement(getReason(), reason);
    }

    public SOAPFaultReason getReason() {
        return (SOAPFaultReason) this.getChildWithName(
                SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME);
    }

    public void setNode(SOAPFaultNode node) throws SOAPProcessingException {
        setNewElement(getNode(), node);
    }

    public SOAPFaultNode getNode() {
        return (SOAPFaultNode) this.getChildWithName(
                SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME);
    }

    public void setRole(SOAPFaultRole role) throws SOAPProcessingException {
        setNewElement(getRole(), role);
    }

    public SOAPFaultRole getRole() {
        return (SOAPFaultRoleImpl) this.getChildWithName(
                SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME);
    }

    public void setDetail(SOAPFaultDetail detail) throws SOAPProcessingException {
        setNewElement(getDetail(), detail);
    }

    public SOAPFaultDetail getDetail() {
        return (SOAPFaultDetail) this.getChildWithName(
                SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME);
    }

    /**
     * If exception detailElement is not there we will return null
     */
    public Exception getException() throws OMException {
        SOAPFaultDetail detail = getDetail();
        if (detail == null) {
            return null;
        }

        OMElement exceptionElement = getDetail().getFirstChildWithName(
                new QName(SOAPConstants.SOAP_FAULT_DETAIL_EXCEPTION_ENTRY));
        if (exceptionElement != null && exceptionElement.getText() != null) {
            return new Exception(exceptionElement.getText());
        }
        return null;
    }

    protected void putExceptionToSOAPFault(Exception e) throws SOAPProcessingException {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        sw.flush();
        SOAPFaultDetail detail = getDetail();
        if (getDetail() == null) {
            detail = getNewSOAPFaultDetail(this);
            setDetail(detail);
        }
        OMElement faultDetailEnty = new OMElementImpl(
                SOAPConstants.SOAP_FAULT_DETAIL_EXCEPTION_ENTRY, null, detail,
                factory);
        faultDetailEnty.setText(sw.getBuffer().toString());
    }

    protected void setNewElement(OMElement myElement, OMElement newElement) {
        if (myElement != null) {
            myElement.discard();
        }
        if (newElement != null && newElement.getParent() != null) {
            newElement.discard();
        }
        this.addChild(newElement);
        myElement = newElement;
    }

    protected OMElement getChildWithName(String childName) {
        Iterator childrenIter = getChildren();
        while (childrenIter.hasNext()) {
            OMNode node = (OMNode) childrenIter.next();
            if (node.getType() == OMNode.ELEMENT_NODE &&
                    childName.equals(((OMElement) node).getLocalName())) {
                return (OMElement) node;
            }
        }
        return null;
    }

    protected void serialize(org.apache.axiom.om.impl.OMOutputImpl omOutput, boolean cache) throws XMLStreamException {
        // select the builder
        short builderType = PULL_TYPE_BUILDER;    // default is pull type
        if (builder != null) {
            builderType = this.builder.getBuilderType();
        }
        if ((builderType == PUSH_TYPE_BUILDER)
                && (builder.getRegisteredContentHandler() == null)) {
            builder.registerExternalContentHandler(new StreamWriterToContentHandlerConverter(omOutput));
        }

        // this is a special case. This fault element may contain its children in any order. But spec mandates a specific order
        // the overriding of the method will facilitate that. Not sure this is the best method to do this :(
        build();

        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        OMSerializerUtil.serializeStartpart(this, writer);
        SOAPFaultCode faultCode = getCode();
        if (faultCode != null) {
            ((OMNodeEx) faultCode).serialize(omOutput);
        }
        SOAPFaultReason faultReason = getReason();
        if (faultReason != null) {
            ((OMNodeEx) faultReason).serialize(omOutput);
        }

        serializeFaultNode(omOutput);

        SOAPFaultRole faultRole = getRole();
        if (faultRole != null && faultRole.getText() != null && !"".equals(faultRole.getText())) {
            ((OMNodeEx) faultRole).serialize(omOutput);
        }

        SOAPFaultDetail faultDetail = getDetail();
        if (faultDetail != null) {
            ((OMNodeEx) faultDetail).serialize(omOutput);
        }

        OMSerializerUtil.serializeEndpart(writer);
    }

    protected abstract void serializeFaultNode(org.apache.axiom.om.impl.OMOutputImpl omOutput) throws XMLStreamException;


}
