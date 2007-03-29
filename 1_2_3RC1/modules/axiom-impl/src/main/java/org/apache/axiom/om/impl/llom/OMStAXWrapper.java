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

package org.apache.axiom.om.impl.llom;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.EmptyOMLocation;
import org.apache.axiom.om.impl.exception.OMStreamingException;
import org.apache.axiom.om.impl.llom.util.NamespaceContextImpl;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Note  - This class also implements the streaming constants interface to get access to the StAX
 * constants
 */
public class OMStAXWrapper implements XMLStreamReader, XMLStreamConstants {
    /** Field navigator */
    private OMNavigator navigator;

    /** Field builder */
    private OMXMLParserWrapper builder;

    /** Field parser */
    private XMLStreamReader parser;

    /** Field rootNode */
    private OMNode rootNode;

    /** Field isFirst */
    private boolean isFirst = true;

    // Navigable means the output should be taken from the navigator.
    // As soon as the navigator returns a null navigable will be reset
    // to false and the subsequent events will be taken from the builder
    // or the parser directly.

    /** Field NAVIGABLE */
    private static final short NAVIGABLE = 0;
    private static final short SWITCH_AT_NEXT = 1;
    private static final short COMPLETED = 2;
    private static final short SWITCHED = 3;
    private static final short DOCUMENT_COMPLETE = 4;


    /** Field state */
    private short state;

    /** Field currentEvent Default set to START_DOCUMENT */
    private int currentEvent = START_DOCUMENT;

    // SwitchingAllowed is set to false by default.
    // This means that unless the user explicitly states
    // that he wants things not to be cached, everything will
    // be cached.

    /** Field switchingAllowed */
    boolean switchingAllowed = false;

    /** Field elementStack */
    private Stack elementStack = null;

    // keeps the next event. The parser actually keeps one step ahead to
    // detect the end of navigation. (at the end of the stream the navigator
    // returns a null

    /** Field nextNode */
    private OMNode nextNode = null;

    // holder for the current node. Needs this to generate events from the current node

    /** Field currentNode */
    private OMNode currentNode = null;

    // needs this to refer to the last known node

    /** Field lastNode */
    private OMNode lastNode = null;

    /** Track depth to ensure we stop generating events when we are done with the root node. */
    int depth = 0;

    private boolean needToThrowEndDocument = false;

    /**
     * Method setAllowSwitching.
     *
     * @param b
     */
    public void setAllowSwitching(boolean b) {
        this.switchingAllowed = b;
    }

    /**
     * Method isAllowSwitching.
     *
     * @return Returns boolean.
     */
    public boolean isAllowSwitching() {
        return switchingAllowed;
    }

    /**
     * When constructing the OMStaxWrapper, the creator must produce the builder (an instance of the
     * OMXMLparserWrapper of the input) and the Element Node to start parsing. The wrapper will
     * parse(proceed) until the end of the given element. Hence care should be taken to pass the
     * root element if the entire document is needed.
     *
     * @param builder
     * @param startNode
     */
    public OMStAXWrapper(OMXMLParserWrapper builder, OMElement startNode) {
        this(builder, startNode, false);
    }

    /**
     * Constructor OMStAXWrapper.
     *
     * @param builder
     * @param startNode
     * @param cache
     */
    public OMStAXWrapper(OMXMLParserWrapper builder, OMElement startNode,
                         boolean cache) {

        // create a navigator
        this.navigator = new OMNavigator(startNode);
        this.builder = builder;
        this.rootNode = startNode;
        if (rootNode != null && rootNode.getParent() != null &&
                rootNode.getParent() instanceof OMDocument) {
            needToThrowEndDocument = true;
        }

        // initaite the next and current nodes
        // Note -  navigator is written in such a way that it first
        // returns the starting node at the first call to it
        currentNode = navigator.next();
        updateNextNode();
        switchingAllowed = !cache;
    }

    /**
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getPrefix()
     */
    public String getPrefix() {
        String returnStr = null;
        if (parser != null) {
            returnStr = parser.getPrefix();
        } else {
            if ((currentEvent == START_ELEMENT)
                    || (currentEvent == END_ELEMENT)) {
                OMNamespace ns = ((OMElement) lastNode).getNamespace();
                returnStr = (ns == null)
                        ? null
                        : ns.getPrefix();
            }
        }
        return returnStr;
    }

    /**
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getNamespaceURI()
     */
    public String getNamespaceURI() {
        String returnStr = null;
        if (parser != null) {
            returnStr = parser.getNamespaceURI();
        } else {
            if ((currentEvent == START_ELEMENT)
                    || (currentEvent == END_ELEMENT)
                    || (currentEvent == NAMESPACE)) {
                OMNamespace ns = ((OMElement) lastNode).getNamespace();
                returnStr = (ns == null)
                        ? null
                        : ns.getNamespaceURI();
            }
        }
        return returnStr;
    }

    /**
     * @return Returns boolean.
     * @see javax.xml.stream.XMLStreamReader#hasName()
     */
    public boolean hasName() {
        if (parser != null) {
            return parser.hasName();
        } else {
            return ((currentEvent == START_ELEMENT)
                    || (currentEvent == END_ELEMENT));
        }
    }

    /**
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getLocalName()
     */
    public String getLocalName() {
        String returnStr = null;
        if (parser != null) {
            returnStr = parser.getLocalName();
        } else {
            if ((currentEvent == START_ELEMENT)
                    || (currentEvent == END_ELEMENT)
                    || (currentEvent == ENTITY_REFERENCE)) {
                returnStr = ((OMElement) lastNode).getLocalName();
            }
        }
        return returnStr;
    }

    /**
     * @return Returns QName.
     * @see javax.xml.stream.XMLStreamReader#getName()
     */
    public QName getName() {
        QName returnName = null;
        if (parser != null) {
            returnName = parser.getName();
        } else {
            if ((currentEvent == START_ELEMENT)
                    || (currentEvent == END_ELEMENT)) {
                returnName = getQName((OMElement) lastNode);
            }
        }
        return returnName;
    }

    /**
     * @return Returns boolean.
     * @see javax.xml.stream.XMLStreamReader#hasText()
     */
    public boolean hasText() {
        return ((currentEvent == CHARACTERS) || (currentEvent == DTD)
                || (currentEvent == ENTITY_REFERENCE)
                || (currentEvent == COMMENT) || (currentEvent == SPACE));
    }

    /**
     * @return Returns int.
     * @see javax.xml.stream.XMLStreamReader#getTextLength()
     */
    public int getTextLength() {
        int returnLength = 0;
        if (parser != null) {
            returnLength = parser.getTextLength();
        } else {
            OMText textNode = (OMText) lastNode;
            returnLength = textNode.getText().length();
        }
        return returnLength;
    }

    /**
     * @return Returns int.
     * @see javax.xml.stream.XMLStreamReader#getTextStart()
     */
    public int getTextStart() {
        int returnLength = 0;
        if (parser != null) {
            returnLength = parser.getTextStart();
        }

        // Note - this has no relevant method in the OM
        return returnLength;
    }

    /**
     * @param i
     * @param chars
     * @param i1
     * @param i2
     * @return Returns int.
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamReader#getTextCharacters(int, char[], int, int)
     */
    public int getTextCharacters(int i, char[] chars, int i1, int i2)
            throws XMLStreamException {
        int returnLength = 0;
        if (parser != null) {
            try {
                returnLength = parser.getTextCharacters(i, chars, i1, i2);
            } catch (XMLStreamException e) {
                throw new OMStreamingException(e);
            }
        } else {
            if (hasText()) {
                OMText textNode = (OMText) lastNode;
                String str = textNode.getText();
                str.getChars(i, i + i2, chars, i1);
            }
        }
        return returnLength;
    }

    /**
     * @return Returns char[].
     * @see javax.xml.stream.XMLStreamReader#getTextCharacters()
     */
    public char[] getTextCharacters() {
        char[] returnArray = null;
        if (parser != null) {
            returnArray = parser.getTextCharacters();
        } else {
            if (hasText()) {
                OMText textNode = (OMText) lastNode;
                String str = textNode.getText();
                returnArray = str.toCharArray();
            }
        }
        return returnArray;
    }

    /**
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getText()
     */
    public String getText() {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getText();
        } else {
            if (hasText()) {
                if (lastNode instanceof OMText) {
                    returnString = ((OMText) lastNode).getText();
                } else if (lastNode instanceof OMComment) {
                    returnString = ((OMComment) lastNode).getValue();
                }
            }
        }
        return returnString;
    }

    /**
     * @return Returns int.
     * @see javax.xml.stream.XMLStreamReader#getEventType()
     */

    // todo this should be improved
    public int getEventType() {
        return currentEvent;
    }

    /**
     * @param i
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getNamespaceURI
     */
    public String getNamespaceURI(int i) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getNamespaceURI(i);

        } else {
            if (isStartElement() || isEndElement()
                    || (currentEvent == NAMESPACE)) {
                OMNamespace ns = (OMNamespace) getItemFromIterator(
                        ((OMElement) lastNode).getAllDeclaredNamespaces(), i);
                returnString = (ns == null)
                        ? null
                        : ns.getNamespaceURI();
            }
        }

        /*
          The following line is necessary to overcome an issue where the empty
          namespace URI returning null rather than the empty string. Our resolution
          is to return "" if the return is actually null

          Note that this is not the case for  getNamespaceURI(prefix) method
          where the contract clearly specifies that the return may be null

        */
        if (returnString == null) returnString = "";

        return returnString;
    }

    /**
     * @param i
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getNamespacePrefix
     */
    public String getNamespacePrefix(int i) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getNamespacePrefix(i);
        } else {
            if (isStartElement() || isEndElement()
                    || (currentEvent == NAMESPACE)) {
                OMNamespace ns = (OMNamespace) getItemFromIterator(
                        ((OMElement) lastNode).getAllDeclaredNamespaces(), i);
                returnString = (ns == null)
                        ? null
                        : ns.getPrefix();
            }
        }
        return returnString;
    }

    /**
     * @return Returns int.
     * @see javax.xml.stream.XMLStreamReader#getNamespaceCount()
     */
    public int getNamespaceCount() {
        int returnCount = 0;
        if (parser != null) {
            returnCount = parser.getNamespaceCount();
        } else {
            if (isStartElement() || isEndElement()
                    || (currentEvent == NAMESPACE)) {
                returnCount =
                        getCount(
                                ((OMElement) lastNode).getAllDeclaredNamespaces());
            }
        }
        return returnCount;
    }

    /**
     * @param i
     * @return Returns boolean.
     * @see javax.xml.stream.XMLStreamReader#isAttributeSpecified
     */
    public boolean isAttributeSpecified(int i) {
        boolean returnValue = false;
        if (parser != null) {
            returnValue = parser.isAttributeSpecified(i);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {

                // theres nothing to be returned here
            } else {
                throw new IllegalStateException(
                        "attribute type accessed in illegal event!");
            }
        }
        return returnValue;
    }

    /**
     * @param i
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getAttributeValue
     */
    public String getAttributeValue(int i) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getAttributeValue(i);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                OMAttribute attrib = getAttribute((OMElement) lastNode, i);
                if (attrib != null) {
                    returnString = attrib.getAttributeValue();
                }
            } else {
                throw new IllegalStateException(
                        "attribute type accessed in illegal event!");
            }
        }
        return returnString;
    }

    /**
     * @param i
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getAttributeType
     */
    public String getAttributeType(int i) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getAttributeType(i);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {

                // todo implement this
            } else {
                throw new IllegalStateException(
                        "attribute type accessed in illegal event!");
            }
        }
        return returnString;
    }

    /**
     * @param i
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getAttributePrefix
     */
    public String getAttributePrefix(int i) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getAttributePrefix(i);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                OMAttribute attrib = getAttribute((OMElement) lastNode, i);
                if (attrib != null) {
                    OMNamespace nameSpace = attrib.getNamespace();
                    if (nameSpace != null) {
                        returnString = nameSpace.getPrefix();
                    }
                }
            } else {
                throw new IllegalStateException(
                        "attribute prefix accessed in illegal event!");
            }
        }
        return returnString;
    }

    /**
     * @param i
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getAttributeLocalName
     */
    public String getAttributeLocalName(int i) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getAttributeLocalName(i);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                OMAttribute attrib = getAttribute((OMElement) lastNode, i);
                if (attrib != null) {
                    returnString = attrib.getLocalName();
                }
            } else {
                throw new IllegalStateException(
                        "attribute localName accessed in illegal event!");
            }
        }
        return returnString;
    }

    /**
     * @param i
     * @return Returns String.
     * @see javax.xml.stream.XMLStreamReader#getAttributeNamespace
     */
    public String getAttributeNamespace(int i) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getAttributeNamespace(i);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                OMAttribute attrib = getAttribute((OMElement) lastNode, i);
                if (attrib != null) {
                    OMNamespace nameSpace = attrib.getNamespace();
                    if (nameSpace != null) {
                        returnString = nameSpace.getNamespaceURI();
                    }
                }
            } else {
                throw new IllegalStateException(
                        "attribute nameSpace accessed in illegal event!");
            }
        }
        return returnString;
    }

    /**
     * @param i
     * @return Returns QName.
     * @see javax.xml.stream.XMLStreamReader#getAttributeName
     */
    public QName getAttributeName(int i) {
        QName returnQName = null;
        if (parser != null) {
            returnQName = parser.getAttributeName(i);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                returnQName = getAttribute((OMElement) lastNode, i).getQName();
            } else {
                throw new IllegalStateException(
                        "attribute count accessed in illegal event!");
            }
        }
        return returnQName;
    }

    /**
     * @return Returns int.
     * @see javax.xml.stream.XMLStreamReader#getAttributeCount
     */
    public int getAttributeCount() {
        int returnCount = 0;
        if (parser != null) {
            returnCount = parser.getAttributeCount();
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                OMElement elt = (OMElement) lastNode;
                returnCount = getCount(elt.getAllAttributes());
            } else {
                throw new IllegalStateException(
                        "attribute count accessed in illegal event (" +
                                currentEvent + ")!");
            }
        }
        return returnCount;
    }

    // todo

    /**
     * Method getAttributeValue.
     *
     * @param s
     * @param s1
     * @return Returns String.
     */
    public String getAttributeValue(String s, String s1) {
        String returnString = null;
        if (parser != null) {
            returnString = parser.getAttributeValue(s, s1);
        } else {
            if (isStartElement() || (currentEvent == ATTRIBUTE)) {
                QName qname = new QName(s, s1);
                OMAttribute attrib = ((OMElement) lastNode).getAttribute(qname);
                if (attrib != null) {
                    returnString = attrib.getAttributeValue();
                }
            } else {
                throw new IllegalStateException(
                        "attribute type accessed in illegal event!");
            }
        }
        return returnString;
    }

    /**
     * Method isWhiteSpace.
     *
     * @return Returns boolean.
     */
    public boolean isWhiteSpace() {
        boolean b;
        if (parser != null) {
            b = parser.isWhiteSpace();
        } else {
            b = (currentEvent == SPACE);
        }
        return b;
    }

    /**
     * Method isCharacters.
     *
     * @return Returns boolean.
     */
    public boolean isCharacters() {
        boolean b;
        if (parser != null) {
            b = parser.isCharacters();
        } else {
            b = (currentEvent == CHARACTERS);
        }
        return b;
    }

    /**
     * Method isEndElement.
     *
     * @return Returns boolean.
     */
    public boolean isEndElement() {
        boolean b;
        if (parser != null) {
            b = parser.isEndElement();
        } else {
            b = (currentEvent == END_ELEMENT);
        }
        return b;
    }

    /**
     * @param i
     * @param s
     * @param s1
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamReader#require(int, String, String)
     */
    public void require(int i, String s, String s1) throws XMLStreamException {
        throw new XMLStreamException();
    }

    /**
     * Method isStartElement.
     *
     * @return Returns boolean.
     */
    public boolean isStartElement() {
        boolean b;
        if (parser != null) {
            b = parser.isStartElement();
        } else {
            b = (currentEvent == START_ELEMENT);
        }
        return b;
    }

    /**
     * Method getNamespaceURI.
     *
     * @param prefix
     * @return Returns String.
     */
    public String getNamespaceURI(String prefix) {

        String returnString = null;
        if (parser != null) {
            returnString = parser.getNamespaceURI(prefix);
        } else {
            if (isStartElement() || isEndElement()
                    || (currentEvent == NAMESPACE)) {

                if (rootNode instanceof OMElement) {
                    OMNamespace namespaceURI =
                            ((OMElement) rootNode).findNamespaceURI(prefix);
                    return namespaceURI != null ? namespaceURI.getNamespaceURI() : null;
                }
            }
        }


        return returnString;
    }

    /**
     * Method close.
     *
     * @throws XMLStreamException
     */
    public void close() throws XMLStreamException {

        // this doesnot mean anything with respect to the OM
        if (parser != null) {
            parser.close();
        }
    }

    /**
     * Method hasNext.
     *
     * @return Returns boolean.
     * @throws XMLStreamException
     */
    public boolean hasNext() throws XMLStreamException {
        if (needToThrowEndDocument) {
            return !(state == DOCUMENT_COMPLETE);
        } else {
            return (state != COMPLETED && currentEvent != END_DOCUMENT);
        }
    }

    /**
     * Returns the next tag.
     *
     * @return Returns int.
     * @throws org.apache.axiom.om.impl.exception.OMStreamingException
     *
     * @throws XMLStreamException
     */
    public int nextTag() throws XMLStreamException {
        int eventType = next();
        while ((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) // skip whitespace
                || (eventType == XMLStreamConstants.CDATA && isWhiteSpace()) // skip whitespace
                || eventType == XMLStreamConstants.SPACE
                || eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                || eventType == XMLStreamConstants.COMMENT) {
            eventType = next();
        }
        if (eventType != XMLStreamConstants.START_ELEMENT &&
                eventType != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("expected start or end tag", getLocation());
        }
        return eventType;
    }

    /**
     * @return Returns String.
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamReader#getElementText()
     */
    public String getElementText() throws XMLStreamException {
        if (parser != null) {
            try {
                return parser.getElementText();
            } catch (XMLStreamException e) {
                throw new OMStreamingException(e);
            }
        } else {
            ///////////////////////////////////////////////////////
            //// Code block directly from the API documentation ///
            if (getEventType() != XMLStreamConstants.START_ELEMENT) {
                throw new XMLStreamException(
                        "parser must be on START_ELEMENT to read next text", getLocation());
            }
            int eventType = next();
            StringBuffer content = new StringBuffer();
            while (eventType != XMLStreamConstants.END_ELEMENT) {
                if (eventType == XMLStreamConstants.CHARACTERS
                        || eventType == XMLStreamConstants.CDATA
                        || eventType == XMLStreamConstants.SPACE
                        || eventType == XMLStreamConstants.ENTITY_REFERENCE) {
                    content.append(getText());
                } else if (eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                        || eventType == XMLStreamConstants.COMMENT) {
                    // skipping
                } else if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    throw new XMLStreamException(
                            "unexpected end of document when reading element text content");
                } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                    throw new XMLStreamException(
                            "element text content may not contain START_ELEMENT");
                } else {
                    throw new XMLStreamException(
                            "Unexpected event type " + eventType, getLocation());
                }
                eventType = next();
            }
            return content.toString();
            ///////////////////////////////////////////////////////////////
        }

    }

    /**
     * Method next.
     *
     * @return Returns int.
     * @throws XMLStreamException
     */
    public int next() throws XMLStreamException {
        switch (state) {
            case DOCUMENT_COMPLETE:
                throw new XMLStreamException("End of the document reached");
            case COMPLETED:
                state = DOCUMENT_COMPLETE;
                currentEvent = END_DOCUMENT;
                break;
            case SWITCH_AT_NEXT:
                state = SWITCHED;

                // load the parser
                try {
                    parser = (XMLStreamReader) builder.getParser();
                } catch (Exception e) {
                    throw new XMLStreamException("problem accessing the parser. " + e.getMessage(),
                                                 e);
                }

                // We should throw an END_DOCUMENT
                if ((currentEvent == START_DOCUMENT) &&
                        (currentEvent == parser.getEventType())) {
                    currentEvent = parser.next();
                } else {
                    currentEvent = parser.getEventType();
                }

                updateCompleteStatus();
                break;
            case NAVIGABLE:
                currentEvent = generateEvents(currentNode);
                updateCompleteStatus();
                updateLastNode();
                break;
            case SWITCHED:
                if (parser.hasNext()) {
                    currentEvent = parser.next();
                }
                updateCompleteStatus();
                break;
            default :
                throw new OMStreamingException("unsuppported state!");
        }
        return currentEvent;
    }

    /**
     * Method getProperty.
     *
     * @param s
     * @return Returns Object.
     * @throws IllegalArgumentException
     */
    public Object getProperty(String s) throws IllegalArgumentException {
        throw new IllegalArgumentException();
    }

    /**
     * This is a very important method. It keeps the navigator one step ahead and pushes it one
     * event ahead. If the nextNode is null then navigable is set to false. At the same time the
     * parser and builder are set up for the upcoming event generation.
     *
     * @throws XMLStreamException
     */
    private void updateLastNode() throws XMLStreamException {
        lastNode = currentNode;
        currentNode = nextNode;
        try {
            updateNextNode();
        } catch (Exception e) {
            throw new XMLStreamException(e);
        }
    }

    /** Method updateNextNode. */
    private void updateNextNode() {
        if (navigator.isNavigable()) {
            nextNode = navigator.next();
        } else {
            if (!switchingAllowed) {
                if (navigator.isCompleted()) {
                    nextNode = null;

                } else {
                    builder.next();
                    navigator.step();
                    nextNode = navigator.next();
                }
            } else {
                //at this point check whether the navigator is done
                //if the navigator is done then we are fine and can directly
                // jump to the complete state ?
                if (navigator.isCompleted()) {
                    nextNode = null;
                } else {
                    // reset caching (the default is ON so it was not needed in the
                    // earlier case!
                    builder.setCache(false);
                    state = SWITCH_AT_NEXT;
                }
            }
        }
    }

    /** Method updateCompleteStatus. */
    private void updateCompleteStatus() {
        if (state == NAVIGABLE) {
            if (rootNode == currentNode) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    state = COMPLETED;
                }
            }
        } else {
            if (state == SWITCHED) {
                if (currentEvent == START_ELEMENT) {
                    ++depth;
                } else if (currentEvent == END_ELEMENT) {
                    --depth;
                    if (depth < 0) {
                        state = COMPLETED;
                    }
                }
            }
            state = (currentEvent == END_DOCUMENT)
                    ? DOCUMENT_COMPLETE
                    : state;
        }
    }

    /**
     * Method getNamespaceContext.
     *
     * @return Returns NamespaceContext.
     */
    public NamespaceContext getNamespaceContext() {
        return new NamespaceContextImpl(getAllNamespaces(lastNode));
    }

    /**
     * Method getEncoding.
     *
     * @return Returns String.
     */
    public String getEncoding() {
        return null;
    }

    /**
     * Method getLocation.
     *
     * @return Returns Location.
     */
    public Location getLocation() {
        return new EmptyOMLocation();
    }

    /**
     * Method getVersion.
     *
     * @return Returns String.
     */
    public String getVersion() {
        return "1.0"; //todo put the constant
    }


    /**
     * Method isStandalone.
     *
     * @return Returns boolean.
     */
    public boolean isStandalone() {
        return true;
    }

    /**
     * Method standaloneSet.
     *
     * @return Returns boolean.
     */
    public boolean standaloneSet() {
        return false;
    }

    /**
     * Method getCharacterEncodingScheme.
     *
     * @return Returns String.
     */
    public String getCharacterEncodingScheme() {
        return "utf-8";
    }

    /**
     * Method getPITarget.
     *
     * @return Returns String.
     */
    public String getPITarget() {
        throw new UnsupportedOperationException();
    }

    /**
     * Method getPIData.
     *
     * @return Returns String.
     */
    public String getPIData() {
        throw new UnsupportedOperationException();
    }

    /*
     *
     * ################################################################
     * Generator methods for the OMNodes returned by the navigator
     * ################################################################
     *
     */

    /**
     * Method generateEvents.
     *
     * @param node
     * @return Returns int.
     */
    private int generateEvents(OMNode node) {
        int returnEvent = 0;
        int nodeType = node.getType();
        switch (nodeType) {
            case OMNode.ELEMENT_NODE:
                OMElement element = (OMElement) node;
                returnEvent = generateElementEvents(element);
                break;
            case OMNode.TEXT_NODE:
                returnEvent = generateTextEvents();
                break;
            case OMNode.COMMENT_NODE:
                returnEvent = generateCommentEvents();
                break;
            case OMNode.CDATA_SECTION_NODE:
                returnEvent = generateCdataEvents();
                break;
            default :
                break;    // just ignore any other nodes
        }
        return returnEvent;
    }

    /**
     * Method generateElementEvents.
     *
     * @param elt
     * @return Returns int.
     */
    private int generateElementEvents(OMElement elt) {
        if (elementStack == null) {
            elementStack = new Stack();
        }
        int returnValue = START_ELEMENT;
        if (!elementStack.isEmpty() && elementStack.peek().equals(elt)) {
            returnValue = END_ELEMENT;
            elementStack.pop();
        } else {
            elementStack.push(elt);
        }
        return returnValue;
    }

    /**
     * Method generateTextEvents.
     *
     * @return Returns int.
     */
    private int generateTextEvents() {
        return CHARACTERS;
    }

    /**
     * Method generateCommentEvents
     *
     * @return Returns int.
     */
    private int generateCommentEvents() {
        return COMMENT;
    }

    /**
     * Method generateCdataEvents
     *
     * @return Returns int.
     */
    private int generateCdataEvents() {
        return CDATA;
    }

    /*
     * ####################################################################
     * Other helper methods
     * ####################################################################
     */

    /**
     * helper method getCount.
     *
     * @param it
     * @return Returns int.
     */
    private int getCount(Iterator it) {
        int count = 0;
        if (it != null) {
            while (it.hasNext()) {
                it.next();
                count++;
            }
        }
        return count;
    }

    /**
     * Helper method getItemFromIterator.
     *
     * @param it
     * @param index
     * @return Returns Object.
     */
    private Object getItemFromIterator(Iterator it, int index) {
        int count = 0;
        Object returnObject = null;
        boolean found = false;
        if (it != null) {
            while (it.hasNext()) {
                returnObject = it.next();
                if (index == count++) {
                    found = true;
                    break;
                }
            }
        }
        if (found) {
            return returnObject;
        } else {
            return null;
        }
    }

    /**
     * Helper method getQName.
     *
     * @param element
     * @return Returns QName.
     */
    private QName getQName(OMElement element) {
        QName returnName;
        OMNamespace ns = element.getNamespace();
        String localPart = element.getLocalName();
        if (ns != null) {
            String prefix = ns.getPrefix();
            String uri = ns.getNamespaceURI();
            if ((prefix == null) || prefix.equals("")) {
                returnName = new QName(uri, localPart);
            } else {
                returnName = new QName(uri, localPart, prefix);
            }
        } else {
            returnName = new QName(localPart);
        }
        return returnName;
    }

    /**
     * @param elt
     * @param index
     * @return Returns OMAttribute.
     */
    private OMAttribute getAttribute(OMElement elt, int index) {
        OMAttribute returnAttrib = null;
        if (elt != null) {
            returnAttrib =
                    (OMAttribute) getItemFromIterator(elt.getAllAttributes(),
                                                      index);
        }
        return returnAttrib;
    }

    public void setParser(XMLStreamReader parser) {
        this.parser = parser;
    }

    private Map getAllNamespaces(Object contextNode) {
        if (!(contextNode instanceof OMContainer &&
                contextNode instanceof OMElement)) {
            return new HashMap();
        }
        Map nsMap = new LinkedHashMap();
        for (OMContainer context = (OMContainer) contextNode;
             context != null && !(context instanceof OMDocument);
             context = ((OMElement) context).getParent()) {
            OMElement element = (OMElement) context;
            Iterator i = element.getAllDeclaredNamespaces();
            while (i != null && i.hasNext()) {
                addNamespaceToMap((OMNamespace) i.next(), nsMap);
            }
            if (element.getNamespace() != null) {
                addNamespaceToMap(element.getNamespace(), nsMap);
            }
            for (Iterator iter = element.getAllAttributes();
                 iter != null && iter.hasNext();) {
                OMAttribute attr = (OMAttribute) iter.next();
                if (attr.getNamespace() != null) {
                    addNamespaceToMap(attr.getNamespace(), nsMap);
                }
            }
        }
        return nsMap;
    }

    private void addNamespaceToMap(OMNamespace ns, Map map) {
        if (map.get(ns.getPrefix()) == null) {
            map.put(ns.getPrefix(), ns.getNamespaceURI());
        }
    }

    public OMXMLParserWrapper getBuilder() {
        return builder;
    }
}
