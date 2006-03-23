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

package org.apache.ws.commons.om.impl.builder;

import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMConstants;
import org.apache.ws.commons.om.OMDocument;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMException;
import org.apache.ws.commons.om.OMFactory;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.om.OMNode;
import org.apache.ws.commons.om.OMXMLParserWrapper;
import org.apache.ws.commons.om.impl.OMNodeEx;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

/**
 * OM should be able to be built from any data source. And the model it builds 
 * may be a SOAP specific one or just an XML model. This class will give 
 * some common functionality of OM Building from StAX.
 */
public abstract class StAXBuilder implements OMXMLParserWrapper {

    /**
     * Field parser
     */
    protected XMLStreamReader parser;

    /**
     * Field omfactory
     */
    protected OMFactory omfactory;

    /**
     * Field lastNode
     */
    protected OMNode lastNode;

    // returns the state of completion

    /**
     * Field done
     */
    protected boolean done = false;

    // keeps the state of the cache

    /**
     * Field cache
     */
    protected boolean cache = true;

    // keeps the state of the parser access. if the parser is
    // accessed atleast once,this flag will be set

    /**
     * Field parserAccessed
     */
    protected boolean parserAccessed = false;
    protected OMDocument document;



    /**
     * Constructor StAXBuilder.
     *
     * @param ombuilderFactory
     * @param parser
     */
    protected StAXBuilder(OMFactory ombuilderFactory, XMLStreamReader parser) {
        this.parser = parser;
        omfactory = ombuilderFactory;
    }

    /**
     * Constructor StAXBuilder.
     *
     * @param parser
     */
    protected StAXBuilder(XMLStreamReader parser) {
        this(OMAbstractFactory.getOMFactory(), parser);
    }

    /**
     * Method setOmbuilderFactory.
     *
     * @param ombuilderFactory
     */
    public void setOmbuilderFactory(OMFactory ombuilderFactory) {
        this.omfactory = ombuilderFactory;
    }

    /**
     * Method processNamespaceData.
     *
     * @param node
     * @param isSOAPElement
     */
    protected abstract void processNamespaceData(OMElement node,
                                                 boolean isSOAPElement);

    // since the behaviors are different when it comes to namespaces
    // this must be implemented differently

    /**
     * Method processAttributes.
     *
     * @param node
     */
    protected void processAttributes(OMElement node) {
        int attribCount = parser.getAttributeCount();
        for (int i = 0; i < attribCount; i++) {
            OMNamespace ns = null;
            String uri = parser.getAttributeNamespace(i);
            String prefix = parser.getAttributePrefix(i);

            if (uri != null && uri.hashCode() != 0) {
                ns = node.findNamespace(uri, prefix);
                if (ns== null) {
                    ns = node.declareNamespace(uri, prefix);
                }
            }


            // todo if the attributes are supposed to namespace qualified all the time
            // todo then this should throw an exception here
           
            node.addAttribute(parser.getAttributeLocalName(i),
                    parser.getAttributeValue(i), ns);
        }
    }

    /**
     * Method createOMText.
     *
     * @return Returns OMNode.
     * @throws OMException
     */
    protected OMNode createOMText(int textType) throws OMException {
        OMNode node = null;
        if (lastNode == null) {
            return null;
        } else if (!lastNode.isComplete()) {
            node = omfactory.createText((OMElement) lastNode, parser.getText(), textType);
        } else if (!(lastNode.getParent() instanceof OMDocument)) {
            node = omfactory.createText((OMElement)lastNode.getParent(), parser.getText(), textType);
        }
        return node;
    }

    /**
     * Method reset.
     *
     * @param node
     * @throws OMException
     */
    public void reset(OMNode node) throws OMException {
        lastNode = null;
    }

    /**
     * Method discard.
     *
     * @param el
     * @throws OMException
     */
    public void discard(OMElement el) throws OMException {
        OMElement element = null;

        if (element.isComplete() || !cache) {
            throw new OMException();
        }
        try {
            cache = false;
            do {
                while (parser.next() != XMLStreamConstants.END_ELEMENT) ;

                // TODO:
            } while (!parser.getName().equals(element.getQName()));
            lastNode = element.getPreviousOMSibling();
            if (lastNode != null) {
                ((OMNodeEx)lastNode).setNextOMSibling(null);
            } else {
                OMElement parent = (OMElement) element.getParent();
                if (parent == null) {
                    throw new OMException();
                }
                parent.setFirstChild(null);
                lastNode = parent;
            }
            cache = true;
        } catch (OMException e) {
            throw e;
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    /**
     * Method getText.
     *
     * @return Returns String.
     * @throws OMException
     */
    public String getText() throws OMException {
        return parser.getText();
    }

    /**
     * Method getNamespace. 
     *
     * @return Returns String.
     * @throws OMException
     */
    public String getNamespace() throws OMException {
        return parser.getNamespaceURI();
    }

    /**
     * Method getNamespaceCount.
     *
     * @return Returns int.
     * @throws OMException
     */
    public int getNamespaceCount() throws OMException {
        try {
            return parser.getNamespaceCount();
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    /**
     * Method getNamespacePrefix.
     *
     * @param index
     * @return Returns String.
     * @throws OMException
     */
    public String getNamespacePrefix(int index) throws OMException {
        try {
            return parser.getNamespacePrefix(index);
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    /**
     * Method getNamespaceUri.
     *
     * @param index
     * @return Returns String.
     * @throws OMException
     */
    public String getNamespaceUri(int index) throws OMException {
        try {
            return parser.getNamespaceURI(index);
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    /**
     * Method setCache.
     *
     * @param b
     */
    public void setCache(boolean b) {
        if (parserAccessed && b) {
            throw new UnsupportedOperationException(
                    "parser accessed. cannot set cache");
        }
        cache = b;
    }

    /**
     * Method getName.
     *
     * @return Returns String.
     * @throws OMException
     */
    public String getName() throws OMException {
        return parser.getLocalName();
    }

    /**
     * Method getPrefix.
     *
     * @return Returns String.
     * @throws OMException
     */
    public String getPrefix() throws OMException {
        return parser.getPrefix();
    }

    /**
     * Method getAttributeCount.
     *
     * @return Returns int.
     * @throws OMException
     */
    public int getAttributeCount() throws OMException {
        return parser.getAttributeCount();
    }

    /**
     * Method getAttributeNamespace.
     *
     * @param arg
     * @return Returns String.
     * @throws OMException
     */
    public String getAttributeNamespace(int arg) throws OMException {
        return parser.getAttributeNamespace(arg);
    }

    /**
     * Method getAttributeName.
     *
     * @param arg
     * @return Returns String.
     * @throws OMException
     */
    public String getAttributeName(int arg) throws OMException {
        return parser.getAttributeNamespace(arg);
    }

    /**
     * Method getAttributePrefix.
     *
     * @param arg
     * @return Returns String.
     * @throws OMException
     */
    public String getAttributePrefix(int arg) throws OMException {
        return parser.getAttributeNamespace(arg);
    }

    /**
     * Method getParser.
     *
     * @return Returns Object.
     */
    public Object getParser() {
        if (parserAccessed){
            throw new IllegalStateException(
                    "Parser already accessed!");
        }
        if (!cache) {
            parserAccessed = true;
            return parser;
        } else {
            throw new IllegalStateException(
                    "cache must be switched off to access the parser");
        }
    }

    /**
     * Method isCompleted.
     *
     * @return Returns boolean.
     */
    public boolean isCompleted() {
        return done;
    }

    /**
     * This method is called with the XMLStreamConstants.START_ELEMENT event.
     *
     * @return Returns OMNode.
     * @throws OMException
     */
    protected abstract OMNode createOMElement() throws OMException;

    /**
     * Forwards the parser one step further, if parser is not completed yet.
     * If this is called after parser is done, then throw an OMException.
     * If the cache is set to false, then returns the event, *without* building the OM tree.
     * If the cache is set to true, then handles all the events within this, and 
     * builds the object structure appropriately and returns the event.
     *
     * @return Returns int.
     * @throws OMException
     */
    public abstract int next() throws OMException;

    /**
     * @return Returns short.
     */
    public short getBuilderType() {
        return OMConstants.PULL_TYPE_BUILDER;
    }

    /**
     * Method registerExternalContentHandler.
     *
     * @param obj
     */
    public void registerExternalContentHandler(Object obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * Method getRegisteredContentHandler.
     * 
     * @return Returns Object.
     */
    public Object getRegisteredContentHandler() {
        throw new UnsupportedOperationException();
    }

    public OMDocument getDocument() {
        return document;
    }
}
