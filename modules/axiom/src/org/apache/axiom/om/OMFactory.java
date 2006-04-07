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

/**
 * Class OMFactory
 */
public interface OMFactory {

    /**
     * Creates a new OMDocument.
     */
    public OMDocument createOMDocument();

    public OMDocument createOMDocument(OMXMLParserWrapper builder);


    /**
     * @param localName
     * @param ns
     */
    public OMElement createOMElement(String localName, OMNamespace ns);

    public OMElement createOMElement(String localName, OMNamespace ns, OMContainer parent) throws OMException;

    /**
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     */
    public OMElement createOMElement(String localName, OMNamespace ns,
                                     OMContainer parent,
                                     OMXMLParserWrapper builder);

    /**
     * This is almost the same as as createOMElement(localName,OMNamespace) method above.
     * But some people may, for some reason, need to use the conventional method of putting a namespace.
     * Or in other words people might not want to use the new OMNamespace.
     * Well, this is for those people.
     *
     * @param localName
     * @param namespaceURI
     * @param namespacePrefix
     * @return Returns the newly created OMElement.
     */
    public OMElement createOMElement(String localName,
                                     String namespaceURI,
                                     String namespacePrefix);

    /**
     * QName(localPart),
     * QName(namespaceURI, localPart) - a prefix will be assigned to this
     * QName(namespaceURI, localPart, prefix)
     *
     * @param qname
     * @param parent
     * @return Returns the new OMElement.
     * @throws OMException
     */
    public OMElement createOMElement(QName qname, OMContainer parent)
            throws OMException;

    /**
     * @param uri
     * @param prefix
     * @return Returns OMNameSpace.
     */
    public OMNamespace createOMNamespace(String uri, String prefix);

    /**
     * @param parent
     * @param text
     * @return Returns OMText.
     */
    public OMText createText(OMElement parent, String text);

    /**
     * @param parent
     * @param text   - This text itself can contain a namespace inside it.
     * @return
     */
    public OMText createText(OMElement parent, QName text);

    /**
     * @param parent
     * @param text
     * @param type   - this should be either of XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA,
     *               XMLStreamConstants.SPACE, XMLStreamConstants.ENTITY_REFERENCE
     * @return Returns OMText.
     */
    public OMText createText(OMElement parent, String text, int type);
    public OMText createText(OMElement parent, char[] charArary, int type);

    /**
     * @param parent
     * @param text   - This text itself can contain a namespace inside it.
     * @param type
     * @return
     */
    public OMText createText(OMElement parent, QName text, int type);

    /**
     * @param s
     * @return Returns OMText.
     */
    public OMText createText(String s);

    /**
     * @param s
     * @param type - OMText node can handle SPACE, CHARACTERS, CDATA and ENTITY REFERENCES. For Constants, use either
     *             XMLStreamConstants or constants found in OMNode.
     * @return Returns OMText.
     */
    public OMText createText(String s, int type);

    public OMText createText(String s, String mimeType, boolean optimize);

    public OMText createText(Object dataHandler, boolean optimize);

    public OMText createText(OMElement parent, String s, String mimeType,
                             boolean optimize);

    public OMText createText(String contentID, OMElement parent,
                             OMXMLParserWrapper builder);

    public OMAttribute createOMAttribute(String localName,
                                         OMNamespace ns,
                                         String value);

    /**
     * Creates DocType/DTD.
     *
     * @param parent
     * @param content
     * @return Returns doctype.
     */
    public OMDocType createOMDocType(OMContainer parent, String content);

    /**
     * Creates a PI.
     *
     * @param parent
     * @param piTarget
     * @param piData
     * @return Returns OMProcessingInstruction.
     */
    public OMProcessingInstruction createOMProcessingInstruction(OMContainer parent, String piTarget, String piData);

    /**
     * Creates a comment.
     *
     * @param parent
     * @param content
     * @return Returns OMComment.
     */
    public OMComment createOMComment(OMContainer parent, String content);
}
