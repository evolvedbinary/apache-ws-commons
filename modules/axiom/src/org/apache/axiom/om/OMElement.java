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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Iterator;

/**
 * A particular kind of node that represents an element infoset information item.
 * <p/>
 * <p>An element has a collection of children, attributes, and namespaces.</p>
 * <p>In contrast with DOM, this interface exposes namespaces separately from the
 * attributes.</p>
 */
public interface OMElement extends OMNode, OMContainer {

    /**
     * Returns a filtered list of children - just the elements.
     *
     * @return Returns an iterator over the child elements.
     * @see #getChildren()
     * @see #getChildrenWithName(javax.xml.namespace.QName)
     */
    public Iterator getChildElements();

    /**
     * Creates a namespace in the current element scope.
     *
     * @param uri    The namespace to declare in the current scope.  The
     *               caller is expected to ensure that the URI is a valid namespace name.
     * @param prefix The prefix to associate with the given namespace.
     *               The caller is expected to ensure that this is a valid XML prefix.
     *               If null is given, first this will check for an existing namespace
     *               with the same uri. If not found, a prefix will be auto-generated.
     * @return Returns the created namespace information item.
     * @see #declareNamespace(OMNamespace)
     * @see #findNamespace(String, String)
     * @see #getAllDeclaredNamespaces()
     */
    public OMNamespace declareNamespace(String uri, String prefix);


    /**
     * This will declare a default namespace for this element explicitly
     * @param uri
     * @return
     */
    public OMNamespace declareDefaultNamespace(String uri);

    /**
     * This will retrieve the default namespace of this element, if available. null returned if none
     * is found.
     * @return
     */
    public OMNamespace getDefaultNamespace();

    /**
     * Declares a namespace with the element as its scope.
     *
     * @param namespace The namespace to declare.
     * @return Returns the namespace parameter passed.
     * @see #declareNamespace(String, String)
     * @see #findNamespace(String, String)
     * @see #getAllDeclaredNamespaces()
     */
    public OMNamespace declareNamespace(OMNamespace namespace);

    /**
     * Finds a namespace with the given uri and prefix, in the scope of the hierarchy.
     * <p/>
     * <p>Searches from the current element and goes up the hiararchy until a match is found.
     * If no match is found, returns <tt>null</tt>.</p>
     * <p/>
     * <p>Either <tt>prefix</tt> or <tt>uri</tt> should be null.  Results are undefined
     * if both are specified.</p>
     *
     * @param uri    The namespace to look for.  If this is specified, <tt>prefix</tt> should be null.
     * @param prefix The prefix to look for.  If this is specified, <tt>uri</tt> should be null.
     * @return Returns the matching namespace declaration, or <tt>null</tt> if none was found.
     * @see #declareNamespace(String, String)
     * @see #declareNamespace(OMNamespace)
     * @see #getAllDeclaredNamespaces()
     */
    public OMNamespace findNamespace(String uri, String prefix);

    /**
     * Checks for a namespace in the context of this element with the given prefix and
     * returns the relevant namespace object, if available. If not available, returns null.
     *
     * @param prefix
     */
    public OMNamespace findNamespaceURI(String prefix);

    /**
     * Returns an iterator for all of the namespaces declared on this element.
     * <p/>
     * <p>If you're interested in all namespaces in scope, you need to call this function
     * for all parent elements as well.  Note that the iterator may be invalidated by
     * any call to either <tt>declareNamespace</tt> function.
     * </p>
     *
     * @return Returns an iterator over the {@link OMNamespace} items declared on the current element.
     * @see #findNamespace(String, String)
     * @see #declareNamespace(String, String)
     * @see #declareNamespace(OMNamespace)
     */
    public Iterator getAllDeclaredNamespaces() throws OMException;

    /**
     * Returns a list of OMAttributes.
     * 
     * <p>Note that the iterator returned by this function will be invalidated by
     * any <tt>addAttribute</tt> call.
     * </p>
     *
     * @return Returns an {@link Iterator} of {@link OMAttribute} items associated with the element.
     * @see #getAttribute
     * @see #addAttribute(OMAttribute)
     * @see #addAttribute(String, String, OMNamespace)
     */
    public Iterator getAllAttributes();

    /**
     * Returns a named attribute if present.
     *
     * @param qname the qualified name to search for
     * @return Returns an OMAttribute with the given name if found, or null
     */
    public OMAttribute getAttribute(QName qname);

    /**
     * Returns a named attribute's value, if present.
     *
     * @param qname the qualified name to search for
     * @return Returns a String containing the attribute value, or null
     */
    public String getAttributeValue(QName qname);

    /**
     * Adds an attribute to this element.
     * <p/>
     * <p>There is no order implied by added attributes.</p>
     *
     * @param attr The attribute to add.
     * @return Returns the passed in attribute.
     */
    public OMAttribute addAttribute(OMAttribute attr);

    /**
     * Adds an attribute to the current element.
     * <p/>
     * <p>This function does not check to make sure that the given attribute value can be serialized directly
     * as an XML value.  The caller may, for example, pass a string with the character 0x01.
     *
     * @param attributeName The "local name" for the attribute.
     * @param value         The string value of the attribute.
     * @param ns            The namespace has to be one of the in scope namespace. i.e. the passed namespace
     *                      must be declared in the parent element of this attribute or ancestors of the parent element of the attribute.
     * @return Returns the added attribute.
     */
    public OMAttribute addAttribute(String attributeName, String value,
                                    OMNamespace ns);

    /**
     * Method removeAttribute
     *
     * @param attr
     */
    public void removeAttribute(OMAttribute attr);

    /**
     * Method setBuilder.
     *
     * @param wrapper
     */
    public void setBuilder(OMXMLParserWrapper wrapper);

    /**
     * Returns the builder object.
     *
     * @return Returns the builder object used to construct the underlying XML infoset on the fly.
     */
    public OMXMLParserWrapper getBuilder();

    /**
     * Sets the first child.
     *
     * @param node
     */
    public void setFirstChild(OMNode node);

    /**
     * Returns the first child element of the element.
     *
     * @return Returns the first child element of the element, or <tt>null</tt> if none was found.
     */

    public OMElement getFirstElement();


    /**
     * Returns the pull parser that will generate the pull
     * events relevant to THIS element.
     * <p/>
     * <p>Caching is on.</p>
     *
     * @return Returns an XMLStreamReader relative to this element.
     */
    public XMLStreamReader getXMLStreamReader();

    /**
     * Returns the pull parser that will generate the pull
     * events relevant to THIS element.
     * <p/>
     * <p>Caching is off.</p>
     *
     * @return Returns an XMLStreamReader relative to this element, with no caching.
     */
    public XMLStreamReader getXMLStreamReaderWithoutCaching();

    /**
     * @param text
     */
    public void setText(String text);
    public void setText(QName text);

    /**
     * Returns the non-empty text children as a String.
     *
     * @return Returns a String representing the concatenation of the child text nodes.
     */
    public String getText();
    public QName getTextAsQName();

    /**
     * Returns the local name of the element.
     *
     * @return Returns the local name of the element.
     */
    public String getLocalName();

    /**
     * Method setLocalName
     *
     * @param localName
     */
    public void setLocalName(String localName);

    /**
     * @return Returns the OMNamespace object associated with this element
     * @throws OMException
     */
    public OMNamespace getNamespace() throws OMException;

    /**
     * Sets the Namespace.
     *
     * @param namespace
     */
    public void setNamespace(OMNamespace namespace);

    /**
     * Gets the QName of this node.
     *
     * @return Returns the {@link QName} for the element.
     */
    public QName getQName();

    /**
     * This is a convenience method only. This will basically serialize the given OMElement
     * to a String but will build the OMTree in the memory
     */
    public String toString();

    /**
     * This is a convenience method only. This basically serializes the given OMElement
     * to a String but will NOT build the OMTree in the memory. So you are at your own risk of
     * losing information.
     */
    public String toStringWithConsume() throws XMLStreamException;


    /**
     * Turns a prefix:local qname string into a proper QName, evaluating it in the OMElement context.
     * Unprefixed qnames resolve to the local namespace.
     *
     * @param qname prefixed qname string to resolve
     * @return Returns null for any failure to extract a qname.
     */
    QName resolveQName(String qname);

    /**
     * Clones this element. Since both elements are build compleletely, you will
     * lose the differed building capability.
     * @return Returns OMElement.
     */
    public OMElement cloneOMElement();

    public void setLineNumber(int lineNumber);
    public int getLineNumber();
}
