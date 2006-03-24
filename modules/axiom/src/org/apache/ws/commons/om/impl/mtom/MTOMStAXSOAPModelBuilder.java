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

package org.apache.ws.commons.om.impl.mtom;

import org.apache.axiom.attachments.MIMEHelper;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMException;
import org.apache.ws.commons.om.OMNode;
import org.apache.ws.commons.om.OMText;
import org.apache.ws.commons.om.impl.MTOMConstants;
import org.apache.ws.commons.om.impl.OMNodeEx;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.impl.builder.StAXSOAPModelBuilder;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MTOMStAXSOAPModelBuilder extends StAXSOAPModelBuilder implements MTOMConstants {
	
    /**
     * <code>mimeHelper</code> handles deferred parsing of incoming MIME
     * Messages.
     */
    MIMEHelper mimeHelper;

    int partIndex = 0;

    public MTOMStAXSOAPModelBuilder(XMLStreamReader parser,
                                    SOAPFactory factory,
                                    MIMEHelper mimeHelper, String soapVersion) {
        super(parser, factory, soapVersion);
        this.mimeHelper = mimeHelper;
    }

    /**
     * @param reader
     * @param mimeHelper
     */
    public MTOMStAXSOAPModelBuilder(XMLStreamReader reader,
                                    MIMEHelper mimeHelper, String soapVersion) {
        super(reader, soapVersion);
        this.mimeHelper = mimeHelper;
    }

    protected OMNode createOMElement() throws OMException {

        elementLevel++;
        String elementName = parser.getLocalName();

        String namespaceURI = parser.getNamespaceURI();

        // create an OMBlob if the element is an <xop:Include>

        if (XOP_INCLUDE.equalsIgnoreCase(elementName)
                && XOP_NAMESPACE_URI
                .equalsIgnoreCase(namespaceURI)) {
            // do we need to check prfix as well. Meaning, should it be "XOP" ?


            OMText node;
            String contentID = null;
            String contentIDName = null;
            if (lastNode == null) {
                // Decide whether to ckeck the level >3 or not
                throw new OMException(
                        "XOP:Include element is not supported here");
            }
            if (parser.getAttributeCount() > 0) {
                contentID = parser.getAttributeValue(0);
                contentID = contentID.trim();
                contentIDName = parser.getAttributeLocalName(0);
                if (contentIDName.equalsIgnoreCase("href")
                        & contentID.substring(0, 3).equalsIgnoreCase("cid")) {
                    contentID = contentID.substring(4);
                    String charsetEncoding = getDocument().getCharsetEncoding();
                    String charEnc = charsetEncoding == null || "".equals(charsetEncoding) ? "UTF-8" : charsetEncoding;
                    try {
                        contentID = URLDecoder.decode(contentID, charEnc);
                    } catch (UnsupportedEncodingException e) {
                        throw new OMException("Unsupported Character Encoding Found", e);
                    }
                } else if (!(contentIDName.equalsIgnoreCase("href")
                        & (!contentID.equals("")))) {
                    throw new OMException(
                            "contentID not Found in XOP:Include element");
                }
            } else {
                throw new OMException(
                        "Href attribute not found in XOP:Include element");
            }

            // This cannot happen. XOP:Include is always the only child of an parent element
            // cause it is same as having some text
            try {
                OMElement e = (OMElement) lastNode;
                //node = new OMTextImpl(contentID, (OMElement) lastNode, this);
                node = this.omfactory.createText(contentID, (OMElement) lastNode, this);
                e.setFirstChild(node);
            } catch (ClassCastException e) {
                throw new OMException(
                        "Last Node & Parent of an OMText should be an Element" +
                                e);
            }

            return node;

        } else {
            OMElement node;
            if (lastNode == null) {
                node = constructNode(null, elementName, true);
                setSOAPEnvelope(node);
            } else if (lastNode.isComplete()) {
                node =
                        constructNode((OMElement) lastNode.getParent(),
                                elementName,
                                false);
                ((OMNodeEx)lastNode).setNextOMSibling(node);
                ((OMNodeEx)node).setPreviousOMSibling(lastNode);
            } else {
                OMElement e = (OMElement) lastNode;
                node = constructNode((OMElement) lastNode, elementName, false);
                e.setFirstChild(node);
            }

            // fill in the attributes
            processAttributes(node);
            //TODO Exception when trying to log . check this
            //			log.info("Build the OMElelment {" + node.getLocalName() + '}'
            //					+ node.getLocalName() + "By the StaxSOAPModelBuilder");
            return node;
        }
    }

    public DataHandler getDataHandler(String blobContentID) throws OMException {
        return mimeHelper.getDataHandler(blobContentID);
    }
}
