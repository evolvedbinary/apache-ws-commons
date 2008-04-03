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

package org.apache.axiom.om.impl.builder;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.OMContainerEx;
import org.apache.axiom.om.impl.OMNodeEx;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class SAXOMBuilder extends DefaultHandler {
    OMElement root = null;

    OMNode lastNode = null;

    OMElement nextElem = null;

    OMFactory factory = OMAbstractFactory.getOMFactory();

    List prefixMappings = new ArrayList();

    public void setDocumentLocator(Locator arg0) {
    }

    public void startDocument() throws SAXException {

    }

    public void endDocument() throws SAXException {
    }

    protected OMElement createNextElement(String localName) throws OMException {
        OMElement e;
        if (lastNode == null) {
            root = e = factory.createOMElement(localName, null, null, null);
        } else if (lastNode.isComplete()) {
            e = factory.createOMElement(localName, null, lastNode.getParent(),
                                        null);
            ((OMNodeEx) lastNode).setNextOMSibling(e);
            ((OMNodeEx) e).setPreviousOMSibling(lastNode);
        } else {
            OMContainerEx parent = (OMContainerEx) lastNode;
            e = factory.createOMElement(localName, null, (OMElement) lastNode,
                                        null);
            parent.setFirstChild(e);
        }
        return e;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        if (nextElem == null)
            nextElem = createNextElement(null);
        nextElem.declareNamespace(uri, prefix);
    }

    public void endPrefixMapping(String arg0) throws SAXException {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts) throws SAXException {
        if (localName == null || localName.trim().equals(""))
            localName = qName.substring(qName.indexOf(':') + 1);
        if (nextElem == null)
            nextElem = createNextElement(localName);
        else
            nextElem.setLocalName(localName);
        nextElem
                .setNamespace(nextElem.findNamespace(namespaceURI, null));
        int j = atts.getLength();
        for (int i = 0; i < j; i++)
            nextElem.addAttribute(atts.getLocalName(i), atts.getValue(i),
                                  nextElem.findNamespace(atts.getURI(i), null));
        lastNode = nextElem;
        nextElem = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String arg0, String arg1, String arg2)
            throws SAXException {
        if (lastNode.isComplete()) {
            OMContainer parent = lastNode.getParent();
            ((OMNodeEx) parent).setComplete(true);
            lastNode = (OMNode) parent;
        } else {
            OMElement e = (OMElement) lastNode;
            ((OMNodeEx) e).setComplete(true);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (lastNode == null) {
            throw new SAXException("");
        }
        OMNode node;
        if (lastNode.isComplete()) {
            node =
                    factory.createOMText(lastNode.getParent(),
                                         new String(ch,
                                                    start, length));
            ((OMNodeEx) lastNode).setNextOMSibling(node);
            ((OMNodeEx) node).setPreviousOMSibling(lastNode);
        } else {
            OMContainerEx e = (OMContainerEx) lastNode;
            node = factory.createOMText(e, new String(ch, start, length));
            e.setFirstChild(node);
        }
        lastNode = node;
    }

    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws SAXException {
    }

    public void processingInstruction(String arg0, String arg1)
            throws SAXException {
    }

    public void skippedEntity(String arg0) throws SAXException {
    }

    /** @return Returns the root. */
    public OMElement getRootElement() {
        return root;
    }
}
