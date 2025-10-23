/*
 * Copyright 2003,2004  The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ws.commons.util.test;

import junit.framework.TestCase;
import org.apache.ws.commons.util.NamespaceContextImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class NamespaceContextIT extends TestCase {

    private static final String ORDERED_PREFIX_MAPPING_XML =
        "<Site>\n" +
        "    <config xmlns=\"urn:config\">123</config>\n" +
        "    <serverconfig xmlns=\"urn:config\">123</serverconfig>\n" +
        "</Site>";

    private static final String UNORDERED_PREFIX_MAPPING_XML =
        "<c:Site xmlns=\"urn:content\" xmlns:c=\"urn:content\">\n" +
        "    <config xmlns=\"urn:config\">123</config>\n" +
        "    <serverconfig xmlns=\"urn:config\">123</serverconfig>\n" +
        "</c:Site>";

    /**
     * Checks that the {@link NamespaceContextImpl} can handle starting
     * and ending prefix mappings which are not ordered.
     */
    public void testOrderedPrefixMapping() throws ParserConfigurationException, SAXException, IOException {
        final NamespaceContextHandler namespaceContextHandler = new NamespaceContextHandler();
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        final SAXParser saxParser = factory.newSAXParser();
        final Reader reader = new StringReader(ORDERED_PREFIX_MAPPING_XML);
        try {
            saxParser.parse(new InputSource(reader), namespaceContextHandler);
        } finally {
            reader.close();
        }
    }

    /**
     * Checks that the {@link NamespaceContextImpl} can handle starting
     * and ending prefix mappings which are not nested.
     * This is required and document by {@link org.xml.sax.ContentHandler#startPrefixMapping(String, String)}
     * and {@link org.xml.sax.ContentHandler#endPrefixMapping(String)}.
     */
    public void testUnorderedPrefixMapping() throws ParserConfigurationException, SAXException, IOException {
        final NamespaceContextHandler namespaceContextHandler = new NamespaceContextHandler();
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        final SAXParser saxParser = factory.newSAXParser();
        final Reader reader = new StringReader(UNORDERED_PREFIX_MAPPING_XML);
        try {
            saxParser.parse(new InputSource(reader), namespaceContextHandler);
        } finally {
            reader.close();
        }
    }

    class NamespaceContextHandler extends DefaultHandler {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
            namespaceContext.startPrefixMapping(prefix, uri);
            super.startPrefixMapping(prefix, uri);
        }

        public void endPrefixMapping(final String prefix) throws SAXException {
            namespaceContext.endPrefixMapping(prefix);
            super.endPrefixMapping(prefix);
        }
    }
}
