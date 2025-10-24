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

import javax.xml.XMLConstants;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class NamespaceContextTest extends TestCase {

    public void testGetNamespaceURINullPrefix() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        try {
            namespaceContext.getNamespaceURI(null);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            assertEquals("The namespace prefix must not be null.", e.getMessage());
        }
    }

    public void testGetNamespaceURIXmlNsPrefix() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        assertEquals(XMLConstants.XML_NS_URI, namespaceContext.getNamespaceURI(XMLConstants.XML_NS_PREFIX));
    }

    public void testGetNamespaceURIXmlAttributeNsPrefix() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        assertEquals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, namespaceContext.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE));
    }

    public void testGetNamespaceURIUnboundPrefix() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        final String ns1Prefix = "pfx1";
        assertEquals(XMLConstants.NULL_NS_URI, namespaceContext.getNamespaceURI(ns1Prefix));
    }

    public void testGetNamespaceURIBoundPrefix() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);
        assertEquals(ns1Uri, namespaceContext.getNamespaceURI(ns1Prefix));
    }

    public void testGetNamespaceURIBoundDefaultPrefix() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(XMLConstants.DEFAULT_NS_PREFIX, ns1Uri);
        assertEquals(ns1Uri, namespaceContext.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX));

        final String ns2Uri = "ns2";
        namespaceContext.startPrefixMapping(XMLConstants.DEFAULT_NS_PREFIX, ns2Uri);
        assertEquals(ns2Uri, namespaceContext.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX));
    }

    public void testGetPrefixNullNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        try {
            namespaceContext.getPrefix(null);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            assertEquals("The namespace URI must not be null.", e.getMessage());
        }
    }

    public void testGetPrefixXmlNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        assertEquals(XMLConstants.XML_NS_PREFIX, namespaceContext.getPrefix(XMLConstants.XML_NS_URI));
    }

    public void testGetPrefixXmlAttributeNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        assertEquals(XMLConstants.XMLNS_ATTRIBUTE, namespaceContext.getPrefix(XMLConstants.XMLNS_ATTRIBUTE_NS_URI));
    }

    public void testGetPrefixUnboundNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        final String ns1Uri = "ns1";
        assertNull(namespaceContext.getPrefix(ns1Uri));
    }

    public void testGetPrefixBoundNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);
        assertEquals(ns1Prefix, namespaceContext.getPrefix(ns1Uri));
    }

    public void testGetPrefixBoundDefaultNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(XMLConstants.DEFAULT_NS_PREFIX, ns1Uri);
        assertEquals(XMLConstants.DEFAULT_NS_PREFIX, namespaceContext.getPrefix(ns1Uri));

        final String ns2Uri = "ns2";
        namespaceContext.startPrefixMapping(XMLConstants.DEFAULT_NS_PREFIX, ns2Uri);
        assertEquals(XMLConstants.DEFAULT_NS_PREFIX, namespaceContext.getPrefix(ns2Uri));
    }

    public void testGetPrefixesIteratorNullNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        try {
            namespaceContext.getPrefixes(null);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            assertEquals("The namespace URI must not be null.", e.getMessage());
        }
    }

    public void testGetPrefixesIteratorXmlNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        final Iterator itPrefixes = namespaceContext.getPrefixes(XMLConstants.XML_NS_URI);
        assertTrue(itPrefixes.hasNext());
        assertEquals(XMLConstants.XML_NS_PREFIX, itPrefixes.next());
        assertFalse(itPrefixes.hasNext());
    }

    public void testGetPrefixesIteratorXmlAttributeNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        final Iterator itPrefixes = namespaceContext.getPrefixes(XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        assertTrue(itPrefixes.hasNext());
        assertEquals(XMLConstants.XMLNS_ATTRIBUTE, itPrefixes.next());
        assertFalse(itPrefixes.hasNext());
    }

    public void testGetPrefixesIteratorUnboundNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        final String ns1Uri = "ns1";
        assertFalse(namespaceContext.getPrefixes(ns1Uri).hasNext());
    }

    public void testGetPrefixesIteratorBoundNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);
        Iterator itPrefixes = namespaceContext.getPrefixes(ns1Uri);
        assertTrue(itPrefixes.hasNext());
        assertEquals(ns1Prefix, itPrefixes.next());
        assertFalse(itPrefixes.hasNext());

        final String ns2Prefix = "pfx2";
        final String ns2Uri = "ns2";
        namespaceContext.startPrefixMapping(ns2Prefix, ns2Uri);
        itPrefixes = namespaceContext.getPrefixes(ns2Uri);
        assertTrue(itPrefixes.hasNext());
        assertEquals(ns2Prefix, itPrefixes.next());
        assertFalse(itPrefixes.hasNext());

        final String ns3Prefix = "pfx3";
        namespaceContext.startPrefixMapping(ns3Prefix, ns2Uri);
        itPrefixes = namespaceContext.getPrefixes(ns2Uri);
        assertTrue(itPrefixes.hasNext());
        assertEquals(ns3Prefix, itPrefixes.next());
        assertTrue(itPrefixes.hasNext());
        assertEquals(ns2Prefix, itPrefixes.next());
        assertFalse(itPrefixes.hasNext());
    }

    public void testGetPrefixesIteratorBoundDefaultNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        final String ns2Prefix = "pfx2";
        namespaceContext.startPrefixMapping(ns2Prefix, XMLConstants.NULL_NS_URI);
        Iterator itPrefixes = namespaceContext.getPrefixes(XMLConstants.NULL_NS_URI);
        assertTrue(itPrefixes.hasNext());
        assertEquals(ns2Prefix, itPrefixes.next());
        assertFalse(itPrefixes.hasNext());

        final String ns3Prefix = "pfx3";
        namespaceContext.startPrefixMapping(ns3Prefix, XMLConstants.NULL_NS_URI);
        itPrefixes = namespaceContext.getPrefixes(XMLConstants.NULL_NS_URI);
        assertTrue(itPrefixes.hasNext());
        assertEquals(ns3Prefix, itPrefixes.next());
        assertTrue(itPrefixes.hasNext());
        assertEquals(ns2Prefix, itPrefixes.next());
        assertFalse(itPrefixes.hasNext());
    }

    public void testGetAttributePrefixNullNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        try {
            namespaceContext.getAttributePrefix(null);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            assertEquals("The namespace URI must not be null.", e.getMessage());
        }
    }

    public void testGetAttributePrefixXmlNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        assertEquals(XMLConstants.XML_NS_PREFIX, namespaceContext.getAttributePrefix(XMLConstants.XML_NS_URI));

        final String ns1Prefix = "pfx1";
        namespaceContext.startPrefixMapping(ns1Prefix, XMLConstants.XML_NS_URI);
        assertEquals(ns1Prefix, namespaceContext.getAttributePrefix(XMLConstants.XML_NS_URI));
    }

    public void testGetAttributePrefixXmlAttributeNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        assertEquals(XMLConstants.XMLNS_ATTRIBUTE, namespaceContext.getAttributePrefix(XMLConstants.XMLNS_ATTRIBUTE_NS_URI));

        final String ns1Prefix = "pfx1";
        namespaceContext.startPrefixMapping(ns1Prefix, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        assertEquals(ns1Prefix, namespaceContext.getAttributePrefix(XMLConstants.XMLNS_ATTRIBUTE_NS_URI));
    }

    public void testGetAttributePrefixUnboundNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        final String ns1Uri = "ns1";
        assertNull(namespaceContext.getAttributePrefix(ns1Uri));
    }

    public void testGetAttributePrefixBoundNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);
        assertEquals(ns1Prefix, namespaceContext.getAttributePrefix(ns1Uri));
    }

    public void testGetAttributePrefixBoundDefaultNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(XMLConstants.DEFAULT_NS_PREFIX, ns1Uri);
        assertNull(namespaceContext.getAttributePrefix(ns1Uri));

        final String ns2Uri = "ns2";
        namespaceContext.startPrefixMapping(XMLConstants.DEFAULT_NS_PREFIX, ns2Uri);
        assertNull(namespaceContext.getAttributePrefix(ns2Uri));
    }

    public void testIsPrefixDeclaredNull() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        assertFalse(namespaceContext.isPrefixDeclared(null));
    }

    public void testIsPrefixDeclaredXmlNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        assertTrue(namespaceContext.isPrefixDeclared(XMLConstants.XML_NS_PREFIX));
    }

    public void testIsPrefixDeclaredUnboundNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        final String ns1Prefix = "pfx1";
        assertFalse(namespaceContext.isPrefixDeclared(ns1Prefix));

        final String ns2Prefix = "pfx2";
        assertFalse(namespaceContext.isPrefixDeclared(ns2Prefix));
    }

    public void testIsPrefixDeclaredBoundNs() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);
        assertTrue(namespaceContext.isPrefixDeclared(ns1Prefix));

        final String ns2Prefix = "pfx2";
        final String ns2Uri = "ns2";
        namespaceContext.startPrefixMapping(ns2Prefix, ns2Uri);
        assertTrue(namespaceContext.isPrefixDeclared(ns2Prefix));
    }

    public void testGetPrefixesListEmpty() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        assertTrue(namespaceContext.getPrefixes().isEmpty());
    }

    public void testGetPrefixesListNonEmpty() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        assertTrue(namespaceContext.getPrefixes().isEmpty());

        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);

        final String ns2Prefix = "pfx2";
        final String ns2Uri = "ns2";
        namespaceContext.startPrefixMapping(ns2Prefix, ns2Uri);

        final String ns3Prefix = "pfx3";
        namespaceContext.startPrefixMapping(ns3Prefix, ns2Uri);

        namespaceContext.startPrefixMapping(ns2Prefix, ns2Uri);

        final List prefixes = namespaceContext.getPrefixes();
        assertEquals(4, prefixes.size());
        assertEquals(ns1Prefix, prefixes.get(0));
        assertEquals(ns2Prefix, prefixes.get(1));
        assertEquals(ns3Prefix, prefixes.get(2));
        assertEquals(ns2Prefix, prefixes.get(3));
    }

    public void testGetCheckContextEmpty() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        final int context = namespaceContext.getContext();
        assertEquals(0, context);

        final String prefix = namespaceContext.checkContext(context);
        assertNull(prefix);
    }

    public void testGetCheckContextNonEmpty1() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);

        final String ns2Prefix = "pfx2";
        final String ns2Uri = "ns2";
        namespaceContext.startPrefixMapping(ns2Prefix, ns2Uri);

        final int context = namespaceContext.getContext();
        assertFalse(0 == context);

        String prefix = namespaceContext.checkContext(context);
        assertNull(prefix);
    }

    public void testGetCheckContextNonEmpty2() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        final int context = namespaceContext.getContext();
        assertEquals(0, context);

        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);

        final String ns2Prefix = "pfx2";
        final String ns2Uri = "ns2";
        namespaceContext.startPrefixMapping(ns2Prefix, ns2Uri);

        String prefix = namespaceContext.checkContext(context);
        assertEquals(ns2Prefix, prefix);
        prefix = namespaceContext.checkContext(context);
        assertEquals(ns1Prefix, prefix);
        prefix = namespaceContext.checkContext(context);
        assertNull(prefix);
    }

    public void testGetCheckContextNonEmpty3() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        final int context1 = namespaceContext.getContext();
        assertEquals(0, context1);

        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);

        final String ns2Prefix = "pfx2";
        final String ns2Uri = "ns2";
        namespaceContext.startPrefixMapping(ns2Prefix, ns2Uri);

        final int context2 = namespaceContext.getContext();
        assertTrue(context2 != 0);

        final String ns3Prefix = "pfx3";
        final String ns3Uri = "ns3";
        namespaceContext.startPrefixMapping(ns3Prefix, ns3Uri);

        namespaceContext.startPrefixMapping(ns2Prefix, ns2Uri);

        String prefix = namespaceContext.checkContext(context2);
        assertEquals(ns2Prefix, prefix);
        prefix = namespaceContext.checkContext(context2);
        assertEquals(ns3Prefix, prefix);
        prefix = namespaceContext.checkContext(context2);
        assertNull(prefix);

        prefix = namespaceContext.checkContext(context1);
        assertEquals(ns2Prefix, prefix);
        prefix = namespaceContext.checkContext(context1);
        assertEquals(ns1Prefix, prefix);
        prefix = namespaceContext.checkContext(context1);
        assertNull(prefix);
    }

    public void testReset() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);
        assertEquals(ns1Uri, namespaceContext.getNamespaceURI(ns1Prefix));
        assertEquals(ns1Prefix, namespaceContext.getPrefix(ns1Uri));

        namespaceContext.reset();

        assertEquals(XMLConstants.NULL_NS_URI, namespaceContext.getNamespaceURI(ns1Prefix));
        assertNull(namespaceContext.getPrefix(ns1Uri));
    }

    public void testOrderedPrefixMapping() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        final String ns2Prefix = "pfx2";
        final String ns2Uri = "ns2";
        final String ns3Prefix = "pfx3";
        final String ns3Uri = "ns3";

        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);
        namespaceContext.startPrefixMapping(ns2Prefix, ns2Uri);
        namespaceContext.startPrefixMapping(XMLConstants.XML_NS_PREFIX, XMLConstants.NULL_NS_URI);
        namespaceContext.startPrefixMapping(ns3Prefix, ns3Uri);

        namespaceContext.endPrefixMapping(ns3Prefix);
        namespaceContext.endPrefixMapping(XMLConstants.XML_NS_PREFIX);
        namespaceContext.endPrefixMapping(ns2Prefix);
        namespaceContext.endPrefixMapping(ns1Prefix);
    }

    /**
     * NOTE(AR) This is an order that Apache Xerces can produce when parsing XML documents.
     */
    public void testUnorderedPrefixMapping() {
        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        final String ns2Prefix = "pfx2";
        final String ns2Uri = "ns2";
        final String ns3Prefix = "pfx3";
        final String ns3Uri = "ns3";

        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);
        namespaceContext.startPrefixMapping(ns2Prefix, ns2Uri);
        namespaceContext.startPrefixMapping(XMLConstants.XML_NS_PREFIX, XMLConstants.NULL_NS_URI);
        namespaceContext.startPrefixMapping(ns3Prefix, ns3Uri);

        namespaceContext.endPrefixMapping(ns1Prefix);
        namespaceContext.endPrefixMapping(ns2Prefix);
        namespaceContext.endPrefixMapping(XMLConstants.XML_NS_PREFIX);
        namespaceContext.endPrefixMapping(ns3Prefix);
    }

    public void testPopScopeEmpty() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();
        try {
            namespaceContext.popScope();
            fail("Expected NoSuchElementException");
        } catch (final NoSuchElementException e) {
            assertEquals("There are no scopes to pop.", e.getMessage());
        }
    }

    public void testPushPopScopeEmpty() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        namespaceContext.pushScope();

        final List prefixes = namespaceContext.popScope();
        assertTrue(prefixes.isEmpty());
    }

    public void testPushPopScopeNonEmpty1() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);

        final String ns2Prefix = "pfx2";
        final String ns2Uri = "ns2";
        namespaceContext.startPrefixMapping(ns2Prefix, ns2Uri);

        namespaceContext.pushScope();

        final List prefixes = namespaceContext.popScope();
        assertTrue(prefixes.isEmpty());
    }

    public void testPushPopScopeNonEmpty2() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        namespaceContext.pushScope();

        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);

        final String ns2Prefix = "pfx2";
        final String ns2Uri = "ns2";
        namespaceContext.startPrefixMapping(ns2Prefix, ns2Uri);

        List prefixes = namespaceContext.popScope();
        assertEquals(2, prefixes.size());
        assertEquals(ns2Prefix, prefixes.get(0));
        assertEquals(ns1Prefix, prefixes.get(1));

    }

    public void testPushPopScopeNonEmpty3() {
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl();

        namespaceContext.pushScope();

        final String ns1Prefix = "pfx1";
        final String ns1Uri = "ns1";
        namespaceContext.startPrefixMapping(ns1Prefix, ns1Uri);

        final String ns2Prefix = "pfx2";
        final String ns2Uri = "ns2";
        namespaceContext.startPrefixMapping(ns2Prefix, ns2Uri);

        namespaceContext.pushScope();

        final String ns3Prefix = "pfx3";
        final String ns3Uri = "ns3";
        namespaceContext.startPrefixMapping(ns3Prefix, ns3Uri);

        namespaceContext.startPrefixMapping(ns2Prefix, ns2Uri);

        List prefixes = namespaceContext.popScope();
        assertEquals(2, prefixes.size());
        assertEquals(ns2Prefix, prefixes.get(0));
        assertEquals(ns3Prefix, prefixes.get(1));

        prefixes = namespaceContext.popScope();
        assertEquals(2, prefixes.size());
        assertEquals(ns2Prefix, prefixes.get(0));
        assertEquals(ns1Prefix, prefixes.get(1));
    }

    public void testCompactLinearFirst() {
        final int compactThreshold = 10;
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl(compactThreshold);

        // Add 2x mappings
        for (int i = 0; i < compactThreshold * 2; i++) {
            final String nsPrefix = "pfx" + i;
            final String nsUri = "ns" + i;
            namespaceContext.startPrefixMapping(nsPrefix, nsUri);
        }

        // Remove first 1x mappings
        for (int i = 0; i < compactThreshold; i++) {
            final String nsPrefix = "pfx" + i;
            namespaceContext.endPrefixMapping(nsPrefix);
        }

        // Check new mappings
        for (int i = 0; i < compactThreshold * 2; i++) {
            final String nsPrefix = "pfx" + i;
            final String nsUri = "ns" + i;

            if (i < compactThreshold) {
                assertEquals(XMLConstants.NULL_NS_URI, namespaceContext.getNamespaceURI(nsPrefix));
                assertNull(namespaceContext.getPrefix(nsUri));
            } else {
                assertEquals(nsUri, namespaceContext.getNamespaceURI(nsPrefix));
                assertEquals(nsPrefix, namespaceContext.getPrefix(nsUri));
            }
        }
    }

    public void testCompactLinearLast() {
        final int compactThreshold = 10;
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl(compactThreshold);

        // Add 2x mappings
        for (int i = 0; i < compactThreshold * 2; i++) {
            final String nsPrefix = "pfx" + i;
            final String nsUri = "ns" + i;
            namespaceContext.startPrefixMapping(nsPrefix, nsUri);
        }

        // Remove last 1x mappings
        for (int i = compactThreshold; i < compactThreshold * 2; i++) {
            final String nsPrefix = "pfx" + i;
            namespaceContext.endPrefixMapping(nsPrefix);
        }

        // Check new mappings
        for (int i = 0; i < compactThreshold * 2; i++) {
            final String nsPrefix = "pfx" + i;
            final String nsUri = "ns" + i;

            if (i < compactThreshold) {
                assertEquals(nsUri, namespaceContext.getNamespaceURI(nsPrefix));
                assertEquals(nsPrefix, namespaceContext.getPrefix(nsUri));
            } else {
                assertEquals(XMLConstants.NULL_NS_URI, namespaceContext.getNamespaceURI(nsPrefix));
                assertNull(namespaceContext.getPrefix(nsUri));
            }
        }
    }

    public void testCompactLinearRandom() {
        final int compactThreshold = 10;
        final NamespaceContextImpl namespaceContext = new NamespaceContextImpl(compactThreshold);

        // Add 2x mappings
        for (int i = 0; i < compactThreshold * 2; i++) {
            final String nsPrefix = "pfx" + i;
            final String nsUri = "ns" + i;
            namespaceContext.startPrefixMapping(nsPrefix, nsUri);
        }

        // Remove 1x random mappings
        final BitSet removed = new BitSet(compactThreshold * 2);
        final Random random = new Random();
        int removedCount = 0;
        while (removedCount < 10) {
            final int idx = random.nextInt(compactThreshold * 2);
            if (!removed.get(idx)) {
                final String nsPrefix = "pfx" + idx;
                namespaceContext.endPrefixMapping(nsPrefix);
                removed.set(idx);
                removedCount++;
            }
        }

        // Check new mappings
        for (int i = 0; i < compactThreshold * 2; i++) {
            final String nsPrefix = "pfx" + i;
            final String nsUri = "ns" + i;

            if (removed.get(i)) {
                assertEquals(XMLConstants.NULL_NS_URI, namespaceContext.getNamespaceURI(nsPrefix));
                assertNull(namespaceContext.getPrefix(nsUri));
            } else {
                assertEquals(nsUri, namespaceContext.getNamespaceURI(nsPrefix));
                assertEquals(nsPrefix, namespaceContext.getPrefix(nsUri));
            }
        }
    }
}
