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
package org.apache.axiom.om.impl.dom;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Implementation of <code>org.w3c.dom.Attr</code> and
 * <code>org.apache.axiom.om.OMAttribute</code>
 */
public class AttrImpl extends NodeImpl implements OMAttribute, Attr {

    /**
     * Name of the attribute
     */
    private String attrName;

    /**
     * Attribute value
     */
    private TextImpl attrValue;

    /**
     * Attribute namespace
     */
    private NamespaceImpl namespace;

    /**
     * Flag to indicate whether this attr is used or not
     */
    private boolean used;

    /**
     * Owner of this attribute
     */
    protected ParentNode parent;

    protected AttrImpl(DocumentImpl ownerDocument, OMFactory factory) {
        super(ownerDocument, factory);
    }

    public AttrImpl(DocumentImpl ownerDocument, String localName,
                    OMNamespace ns, String value, OMFactory factory) {
        super(ownerDocument, factory);
        this.attrName = localName;
        this.attrValue = new TextImpl(ownerDocument, value, factory);
        this.namespace = (NamespaceImpl) ns;
    }

    public AttrImpl(DocumentImpl ownerDocument, String name, String value,
                    OMFactory factory) {
        super(ownerDocument, factory);
        this.attrName = name;
        this.attrValue = new TextImpl(ownerDocument, value, factory);
    }

    public AttrImpl(DocumentImpl ownerDocument, String name, OMFactory factory) {
        super(ownerDocument, factory);
        this.attrName = name;
        //If this is a default namespace attr
        if(OMConstants.XMLNS_NS_PREFIX.equals(name)) {
            this.namespace = new NamespaceImpl(
                    OMConstants.XMLNS_NS_URI, OMConstants.XMLNS_NS_PREFIX);
        }
    }

    public AttrImpl(DocumentImpl ownerDocument, String localName,
                    OMNamespace namespace, OMFactory factory) {
        super(ownerDocument, factory);
        this.attrName = localName;
        this.namespace = (NamespaceImpl) namespace;
    }

    // /
    // /org.w3c.dom.Node methods
    // /
    /**
     * Returns the name of this attribute.
     */
    public String getNodeName() {
        return (this.namespace != null
                && !"".equals(this.namespace.getPrefix()) && !(OMConstants.XMLNS_NS_PREFIX.equals(this.attrName)))
                ? this.namespace.getPrefix()+ ":" + this.attrName
                : this.attrName;
    }

    /**
     * Returns the node type.
     * 
     * @see org.w3c.dom.Node#getNodeType()
     */
    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
    }

    /**
     * Returns the value of this attribute.
     * 
     * @see org.w3c.dom.Node#getNodeValue()
     */
    public String getNodeValue() throws DOMException {
        return (this.attrValue == null) ? "" : this.attrValue.getData();
    }

    /**
     * Returns the value of this attribute.
     * 
     * @see org.w3c.dom.Attr#getValue()
     */
    public String getValue() {
        return (this.attrValue == null) ? null : this.attrValue.getText();
    }

    // /
    // /org.w3c.dom.Attr methods
    // /
    public String getName() {
        return (this.namespace == null) ? this.attrName
                : OMConstants.XMLNS_NS_PREFIX + ":" + this.attrName;
    }

    /**
     * Returns the owner element.
     * 
     * @see org.w3c.dom.Attr#getOwnerElement()
     */
    public Element getOwnerElement() {
        // Owned is set to an element instance when the attribute is added to an
        // element
        return (Element) (isOwned() ? ownerNode : null);
    }

    public boolean getSpecified() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Not supported: Cannot detach attributes. Use the operations available in
     * the owner node.
     * 
     * @see org.apache.axiom.om.OMNode#detach()
     */
    public OMNode detach() throws OMException {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Not supported: Cannot discard attributes. Use the operations available in
     * the owner node.
     * 
     * @see org.apache.axiom.om.OMNode#discard()
     */
    public void discard() throws OMException {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Returns the type of this attribute node.
     * 
     * @see org.apache.axiom.om.OMNode#getType()
     */
    public int getType() {
        return Node.ATTRIBUTE_NODE;
    }

    /**
     * This is not supported since attributes serialization is handled by the
     * serialization of the owner nodes.
     */
    public void internalSerialize(XMLStreamWriter writer) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * This is not supported since attributes serialization is handled by the
     * serialization of the owner nodes.
     * 
     * @see org.apache.axiom.om.impl.OMNodeEx#internalSerializeAndConsume
     * (org.apache.axiom.om.impl.MTOMXMLStreamWriter)
     */
    public void internalSerializeAndConsume(XMLStreamWriter writer)
            throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Returns the namespace of the attribute as an <code>OMNamespace</code>.
     * 
     * @see org.apache.axiom.om.OMAttribute#getNamespace()
     */
    public OMNamespace getNamespace() {
        return this.namespace;
    }

    /**
     * Returns a qname representing the attribute.
     * 
     * @see org.apache.axiom.om.OMAttribute#getQName()
     */
    public QName getQName() {
        return (this.namespace == null) ? new QName(this.attrName) : new QName(
                this.namespace.getName(), this.attrName, this.namespace
                        .getPrefix());

    }

    /**
     * Returns the attribute value.
     * 
     * @see org.apache.axiom.om.OMAttribute#getAttributeValue()
     */
    public String getAttributeValue() {
        return this.attrValue.getText();
    }

    /**
     * Sets the name of attribute.
     * 
     * @see org.apache.axiom.om.OMAttribute#setLocalName(java.lang.String)
     */
    public void setLocalName(String localName) {
        this.attrName = localName;
    }

    /**
     * Sets the namespace of this attribute node.
     * 
     * @see org.apache.axiom.om.OMAttribute#setOMNamespace
     * (org.apache.axiom.om.OMNamespace)
     */
    public void setOMNamespace(OMNamespace omNamespace) {
        this.namespace = (NamespaceImpl) omNamespace;
    }

    /**
     * Sets the attribute value.
     * 
     * @see org.apache.axiom.om.OMAttribute#setAttributeValue(java.lang.String)
     */
    public void setAttributeValue(String value) {
        if (isReadonly()) {
            String msg = DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                    msg);
        }
        this.attrValue = (TextImpl) this.getOwnerDocument().createTextNode(
                value);
    }

    /**
     * Sets the parent element to the given OMContainer.
     * 
     * @see org.apache.axiom.om.impl.OMNodeEx#setParent
     * (org.apache.axiom.om.OMContainer)
     */
    public void setParent(OMContainer element) {
        this.parent = (ParentNode) element;
    }

    /**
     * Sets the type. NOT IMPLEMENTED: Unnecessary.
     * 
     * @see org.apache.axiom.om.impl.OMNodeEx#setType(int)
     */
    public void setType(int nodeType) throws OMException {
        // not necessary ???
    }

    /**
     * @return Returns boolean.
     */
    protected boolean isUsed() {
        return used;
    }

    /**
     * @param used
     *            The used to set.
     */
    protected void setUsed(boolean used) {
        this.used = used;
    }

    /**
     * Sets the value of the attribute.
     * 
     * @see org.w3c.dom.Attr#setValue(java.lang.String)
     */
    public void setValue(String value) throws DOMException {
        this.attrValue = (TextImpl) this.getOwnerDocument().createTextNode(
                value);
    }

    /**
     * Returns the parent node of this attribute.
     * 
     * @see org.apache.axiom.om.OMNode#getParent()
     */
    public OMContainer getParent() {
        return this.parent;
    }

    /**
     * Returns the attribute name.
     * 
     * @see org.w3c.dom.Node#getLocalName()
     */
    public String getLocalName() {
        return (this.namespace == null) ? this.attrName : DOMUtil
                .getLocalName(this.attrName);
    }

    /**
     * Returns the namespace URI of this attr node.
     * 
     * @see org.w3c.dom.Node#getNamespaceURI()
     */
    public String getNamespaceURI() {
        if (this.namespace != null) {
            return namespace.getName();
        }
        return null;
    }

    /**
     * Returns the namespace prefix of this attr node.
     * 
     * @see org.w3c.dom.Node#getPrefix()
     */
    public String getPrefix() {
        // TODO Error checking
        return (this.namespace == null) ? null : this.namespace.getPrefix();
    }

    public Node cloneNode(boolean deep) {

        AttrImpl clone = (AttrImpl) super.cloneNode(deep);

        if (clone.attrValue == null) {
            // Need to break the association w/ original kids
            clone.attrValue = new TextImpl(this.attrValue.toString(), factory);
            if (this.attrValue.nextSibling != null) {
                throw new UnsupportedOperationException(
                        "Attribute value can contain only a text " +
                        "node with out any siblings");
            }
        }
        clone.isSpecified(true);
        return clone;
    }

    /*
     * DOM-Level 3 methods
     */
    public TypeInfo getSchemaTypeInfo() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isId() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public String toString() {
        return (this.namespace == null) ? this.attrName : this.namespace
                .getPrefix()
                + ":" + this.attrName;
    }

}
