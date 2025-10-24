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
package org.apache.ws.commons.util;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Default implementation of {@link javax.xml.namespace.NamespaceContext}.
 *
 * Stores each Prefix and Namespace only once in {@link #namespacePrefixes}
 * and {@link #namespaceUris} respectively.
 *
 * Each Prefix to Namespace mapping then has an entry in {@link #scopedPrefixUriMappings}.
 *
 * Mappings that are removed are initially marked as deleted, the underlying
 * storage will be resized after the number of deletions reaches a threshold.
 *
 * @author <a href="mailto:adam@evolvedbinary.com">Adam Retter</a>
 */
public class NamespaceContextImpl implements NamespaceContext {

    private static final Iterator EMPTY_ITERATOR = new EmptyIterator();
    private static final List EMPTY_LIST = Arrays.asList(new Object[0]);

    /**
     * Represents a deleted entry in {@link #scopedPrefixUriMappings}
     */
    private static final int MAPPING_TOMBSTONE = -1;

    /**
     * The default maximum number of compact attempts that are made before we invoke compaction.
     * See {@link #compactAttempts}.
     */
    private static final int DEFAULT_COMPACT_THRESHOLD = 32;

    /**
     * The current scope.
     * First and default scope is 0.
     * This is an index into {@link #scopedPrefixUriMappings}.
     */
    private int currentScope = 0;

    /**
     * Unique Namespace Prefixes.
     */
    private String[] namespacePrefixes;

    /**
     * Unique Namespace URIs
     */
    private String[] namespaceUris;

    /**
     * Namespace Prefix to URI Mappings per-scope.
     * A 2D-Array, whose first index is the scope, and the second index is the Mapping within that scope.
     * Each Mapping is a long. If the Mapping has the value -1 it has been removed (this will be cleaned up later).
     * The long value of each Mapping is actually 2 ints whereby:
     * 1. the first int (lower 32 bits) is the Prefix index into {@link #namespacePrefixes}
     * 2. the second int (upper 32 bits) is the Namespace index into {@link #namespaceUris}
     */
    private long[][] scopedPrefixUriMappings;

    /**
     * The number of attempts to compact the storage used by this class.
     * This is incremented each time a removal is made from {@link #scopedPrefixUriMappings}.
     * When this value reaches {@link #compactThreshold} the storage used by
     * this class will be compacted.
     */
    private int compactAttempts = 0;


    /**
     * The maximum number of compact attempts that are made before we invoke compaction.
     * See {@link #compactAttempts}.
     */
    private final int compactThreshold;


    public NamespaceContextImpl() {
        this(DEFAULT_COMPACT_THRESHOLD);
    }

    /**
     * @param compactThreshold The maximum number of compact attempts that are made before we invoke compaction.
     */
    public NamespaceContextImpl(final int compactThreshold) {
        this.compactThreshold = compactThreshold;
    }

    public String getNamespaceURI(final String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("The namespace prefix must not be null.");
        }

        if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
            return XMLConstants.XML_NS_URI;
        }

        if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }

        if (scopedPrefixUriMappings != null) {
            for (int scopeIdx = currentScope; scopeIdx != -1; scopeIdx--) {
                final long[] prefixUriMappings = scopedPrefixUriMappings[scopeIdx];
                if (prefixUriMappings != null) {
                    for (int mapIdx = prefixUriMappings.length - 1; mapIdx != -1; mapIdx--) {
                        final long prefixUriMapping = prefixUriMappings[mapIdx];
                        if (prefixUriMapping == MAPPING_TOMBSTONE) {
                            // skip deleted mapping
                            continue;
                        }

                        final int prefixIdx = (int) (prefixUriMapping & 0xFFFFFFFF);
                        final String nsPrefix = namespacePrefixes[prefixIdx];
                        if (prefix.equals(nsPrefix)) {
                            final int uriIdx = (int) (prefixUriMapping >> 32);
                            final String nsUri = namespaceUris[uriIdx];
                            return nsUri;
                        }
                    }
                }
            }
        }

        return XMLConstants.NULL_NS_URI;
    }

    public String getPrefix(final String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("The namespace URI must not be null.");
        }

        if (namespaceURI.equals(XMLConstants.XML_NS_URI)) {
            return XMLConstants.XML_NS_PREFIX;
        }

        if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }

        if (scopedPrefixUriMappings != null) {
            for (int scopeIdx = currentScope; scopeIdx != -1; scopeIdx--) {
                final long[] prefixUriMappings = scopedPrefixUriMappings[scopeIdx];
                if (prefixUriMappings != null) {
                    for (int mapIdx = prefixUriMappings.length - 1; mapIdx != -1; mapIdx--) {
                        final long prefixUriMapping = prefixUriMappings[mapIdx];
                        if (prefixUriMapping == MAPPING_TOMBSTONE) {
                            // skip deleted mapping
                            continue;
                        }

                        final int uriIdx = (int) (prefixUriMapping >> 32);
                        final String nsUri = namespaceUris[uriIdx];
                        if (namespaceURI.equals(nsUri)) {
                            final int prefixIdx = (int) (prefixUriMapping & 0xFFFFFFFF);
                            final String nsPrefix = namespacePrefixes[prefixIdx];
                            return nsPrefix;
                        }
                    }
                }
            }
        }

        return null;
    }

    public Iterator getPrefixes(final String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("The namespace URI must not be null.");
        }

        if (namespaceURI.equals(XMLConstants.XML_NS_URI)) {
            return new SingleValueIterator(XMLConstants.XML_NS_PREFIX);
        }

        if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            return new SingleValueIterator(XMLConstants.XMLNS_ATTRIBUTE);
        }

        if (scopedPrefixUriMappings != null) {
            for (int scopeIdx = currentScope; scopeIdx != -1; scopeIdx--) {
                final long[] prefixUriMappings = scopedPrefixUriMappings[scopeIdx];
                if (prefixUriMappings != null) {
                    for (int mapIdx = prefixUriMappings.length - 1; mapIdx != -1; mapIdx--) {
                        final long prefixUriMapping = prefixUriMappings[mapIdx];
                        if (prefixUriMapping == MAPPING_TOMBSTONE) {
                            // skip deleted mapping
                            continue;
                        }

                        final int uriIdx = (int) (prefixUriMapping >> 32);
                        final String nsUri = namespaceUris[uriIdx];
                        if (namespaceURI.equals(nsUri)) {
                            final int prefixIdx = (int) (prefixUriMapping & 0xFFFFFFFF);
                            final String nsPrefix = namespacePrefixes[prefixIdx];

                            return new PrefixesIterator(namespaceURI, scopeIdx, mapIdx, nsPrefix);
                        }
                    }
                }
            }
        }

        return EMPTY_ITERATOR;
    }

    /**
     * Returns a non-empty prefix currently mapped to the given
     * URL or null, if there is no such mapping.
     *
     * This method may be used to find a possible prefix for an attributes namespace
     * URI. This is similar to {@link #getPrefix(String)} but:
     * 1. Given {@link XMLConstants#NULL_NS_URI} it will always return {@link XMLConstants#DEFAULT_NS_PREFIX}.
     * 2. Given {@link XMLConstants#XML_NS_URI} it may return a different result to {@link XMLConstants#XML_NS_PREFIX}.
     * 3. Given {@link XMLConstants#XMLNS_ATTRIBUTE_NS_URI} it may return a different result to {@link XMLConstants#XMLNS_ATTRIBUTE}.
     * For element prefixes, or in general, you should use {@link #getPrefix(String)} instead.
     *
     * @param namespaceURI The namespace URI in question.
     *
     * @return the attribute prefix or null.
     *
     * @throws IllegalArgumentException The namespace URI is null.
     *
     * @deprecated Use {@link #getPrefix(String)} instead.
     */
    public String getAttributePrefix(final String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("The namespace URI must not be null.");
        }

        if (namespaceURI.equals(XMLConstants.NULL_NS_URI)) {
            return XMLConstants.DEFAULT_NS_PREFIX;
        }

        if (scopedPrefixUriMappings != null) {
            for (int scopeIdx = currentScope; scopeIdx != -1; scopeIdx--) {
                final long[] prefixUriMappings = scopedPrefixUriMappings[scopeIdx];
                if (prefixUriMappings != null) {
                    for (int mapIdx = prefixUriMappings.length - 1; mapIdx != -1; mapIdx--) {
                        final long prefixUriMapping = prefixUriMappings[mapIdx];
                        if (prefixUriMapping == MAPPING_TOMBSTONE) {
                            // skip deleted mapping
                            continue;
                        }

                        final int uriIdx = (int) (prefixUriMapping >> 32);
                        final String nsUri = namespaceUris[uriIdx];
                        if (namespaceURI.equals(nsUri)) {
                            final int prefixIdx = (int) (prefixUriMapping & 0xFFFFFFFF);
                            final String nsPrefix = namespacePrefixes[prefixIdx];
                            if (nsPrefix.length() > 0) {
                                return nsPrefix;
                            }
                        }
                    }
                }
            }
        }

        if (namespaceURI.equals(XMLConstants.XML_NS_URI)) {
            return XMLConstants.XML_NS_PREFIX;
        }

        if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }

        return null;
    }

    /**
     * Returns whether a given prefix is currently declared.
     *
     * @param prefix the namespace prefix.
     *
     * @return true if the prefix is declared, false otherwise.
     */
    public boolean isPrefixDeclared(final String prefix) {
        if (prefix == null) {
            return false;
        }

        if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
            return true;
        }

        if (scopedPrefixUriMappings != null) {
            for (int scopeIdx = currentScope; scopeIdx != -1; scopeIdx--) {
                final long[] prefixUriMappings = scopedPrefixUriMappings[scopeIdx];
                if (prefixUriMappings != null) {
                    for (int mapIdx = prefixUriMappings.length - 1; mapIdx != -1; mapIdx--) {
                        final long prefixUriMapping = prefixUriMappings[mapIdx];
                        if (prefixUriMapping == MAPPING_TOMBSTONE) {
                            // skip deleted mapping
                            continue;
                        }

                        final int prefixIdx = (int) (prefixUriMapping & 0xFFFFFFFF);
                        final String nsPrefix = namespacePrefixes[prefixIdx];
                        if (prefix.equals(nsPrefix)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns a list of all prefixes which are currently declared,
     * in the order of declaration.
     * Duplicates are possible, if a prefix has been assigned to more
     * than one URI, or repeatedly to the same URI.
     *
     * @return a list of all prefixes.
     */
    public List getPrefixes() {
        List result = null;
        if (scopedPrefixUriMappings != null) {
            for (int scopeIdx = currentScope; scopeIdx != -1; scopeIdx--) {
                final long[] prefixUriMappings = scopedPrefixUriMappings[scopeIdx];
                if (prefixUriMappings != null) {

                    for (int mapIdx = 0; mapIdx < prefixUriMappings.length; mapIdx++) {
                        final long prefixUriMapping = prefixUriMappings[mapIdx];
                        if (prefixUriMapping == MAPPING_TOMBSTONE) {
                            // skip deleted mapping
                            continue;
                        }

                        final int prefixIdx = (int) (prefixUriMapping & 0xFFFFFFFF);
                        final String nsPrefix = namespacePrefixes[prefixIdx];

                        if (result == null) {
                            result = new ArrayList(mapIdx + 1);
                        }
                        result.add(nsPrefix);
                    }
                }
            }
        }

        if (result == null) {
            result = EMPTY_LIST;
        }

        return result;
    }

    /**
     * Start a new Scope.
     */
    public void pushScope() {
        if (scopedPrefixUriMappings == null) {
            scopedPrefixUriMappings = new long[2][];
        } else {
            final long[][] newScopedPrefixUriMappings = new long[scopedPrefixUriMappings.length + 1][];
            System.arraycopy(scopedPrefixUriMappings, 0, newScopedPrefixUriMappings, 0, scopedPrefixUriMappings.length);
            scopedPrefixUriMappings = newScopedPrefixUriMappings;
        }
        currentScope++;
    }

    /**
     * End a Scope.
     *
     * @return The list of prefixes in the previous Scope.
     */
    public List popScope() {
        if (currentScope == 0) {
            throw new NoSuchElementException("There are no scopes to pop.");
        }

        List result = null;

        if (scopedPrefixUriMappings != null) {
            final long[] prefixUriMappings = scopedPrefixUriMappings[currentScope];
            if (prefixUriMappings != null) {
                for (int mapIdx = prefixUriMappings.length - 1; mapIdx != -1; mapIdx--) {
                    final long prefixUriMapping = prefixUriMappings[mapIdx];
                    if (prefixUriMapping == MAPPING_TOMBSTONE) {
                        // skip deleted mapping
                        continue;
                    }

                    final int prefixIdx = (int) (prefixUriMapping & 0xFFFFFFFF);
                    final String nsPrefix = namespacePrefixes[prefixIdx];

                    if (result == null) {
                        result = new ArrayList(mapIdx + 1);
                    }
                    result.add(nsPrefix);
                }

                final long[][] newScopedPrefixUriMappings = new long[scopedPrefixUriMappings.length - 1][];
                System.arraycopy(scopedPrefixUriMappings, 0, newScopedPrefixUriMappings, 0, newScopedPrefixUriMappings.length);
                scopedPrefixUriMappings = newScopedPrefixUriMappings;
                currentScope--;
            }
        }

        if (result == null) {
            result = EMPTY_LIST;
        }

        return result;
    }

    /**
     * Returns the current number of assigned prefixes.
     * Note, that a prefix may be assigned in several nested elements, in which case every assignment is counted.
     * This method is typically called before invoking the
     * method {@link org.xml.sax.ContentHandler#startElement(String, String, String, org.xml.sax.Attributes)}.
     * The return value is used as a savable state.
     * After invoking {@link org.xml.sax.ContentHandler#endElement(String, String, String)}, the state is restored
     * by calling {@link #checkContext(int)}.
     *
     * @return the current number of assigned prefixes.
     *
     * @deprecated Use {@link #pushScope()}.
     */
    public int getContext() {
        int prefixes = 0;
        if (scopedPrefixUriMappings != null) {
            for (int scopeIdx = 0; scopeIdx < scopedPrefixUriMappings.length; scopeIdx++) {
                final long[] prefixUriMappings = scopedPrefixUriMappings[scopeIdx];
                if (prefixUriMappings != null) {
                    for (int mapIdx = 0; mapIdx < prefixUriMappings.length; mapIdx++) {
                        final long prefixUriMapping = prefixUriMappings[mapIdx];
                        if (prefixUriMapping == MAPPING_TOMBSTONE) {
                            // skip deleted mapping
                            continue;
                        }
                        prefixes++;
                    }
                }
            }
        }
        return prefixes;
    }

    /**
     * This method is used to restore the namespace state after an element is created.
     * It takes as input a state, as returned by {@link #getContext()}.
     * For any prefix, which was since saving the state, the prefix is returned and deleted from the internal list.
     * In other words, a typical use looks like this:
     * <pre>
     *   NamespaceSupport nss;
     *   ContentHandler h;
     *   int context = nss.getContext();
     *   h.startElement("foo", "bar", "f:bar", new AttributesImpl());
     *   ...
     *   h.endElement("foo", "bar", "f:bar");
     *   for (;;) {
     *     String prefix = nss.checkContext(context);
     *     if (prefix == null) {
     *       break;
     *     }
     *     h.endPrefixMapping(prefix);
     *   }
     * </pre>
     *
     * @param i the input state.
     *
     * @return the prefix.
     *
     * @deprecated Use {@link #popScope()}.
     */
    public String checkContext(final int i) {
        if (scopedPrefixUriMappings == null || getContext() == i) {
            return null;
        }

        for (int scopeIdx = currentScope; scopeIdx != -1; scopeIdx--) {
            final long[] prefixUriMappings = scopedPrefixUriMappings[scopeIdx];
            if (prefixUriMappings != null) {
                for (int mapIdx = prefixUriMappings.length - 1; mapIdx != -1; mapIdx--) {
                    final long prefixUriMapping = prefixUriMappings[mapIdx];
                    if (prefixUriMapping != MAPPING_TOMBSTONE) {
                        // Set this mapping as deleted, will be compacted later
                        prefixUriMappings[mapIdx] = MAPPING_TOMBSTONE;
                        attemptCompact();

                        // return the prefix;
                        final int prefixIdx = (int) (prefixUriMapping & 0xFFFFFFFF);
                        return namespacePrefixes[prefixIdx];
                    }
                }
            }
        }

        return null;
    }

    /**
     * Declares a new prefix.
     * Typically called from within {@link org.xml.sax.ContentHandler#startPrefixMapping(String, String)}.
     *
     * @param prefix The namespace prefix.
     * @param namespaceURI The namespace URI.
     *
     * @throws IllegalArgumentException Prefix or URI are null.
     */
    public void startPrefixMapping(final String prefix, final String namespaceURI) {
        if (prefix == null) {
            throw new IllegalArgumentException("The namespace prefix must not be null.");
        }

        if (namespaceURI == null) {
            throw new IllegalArgumentException("The namespace prefix must not be null.");
        }

        // check for an existing Prefix
        int prefixIdx = -1;
        if (namespacePrefixes != null) {
            for (int pfxIdx = namespacePrefixes.length - 1; pfxIdx != -1; pfxIdx--) {
                if (prefix.equals(namespacePrefixes[pfxIdx])) {
                    prefixIdx = pfxIdx;
                    break;
                }
            }
        }
        if (prefixIdx == -1) {
            // no existing Prefix, add a new one
            if (namespacePrefixes == null) {
                namespacePrefixes = new String[] { prefix };
                prefixIdx = 0;
            } else {
                final String[] newNamespacePrefixes = new String[namespacePrefixes.length + 1];
                System.arraycopy(namespacePrefixes, 0,  newNamespacePrefixes, 0, namespacePrefixes.length);
                newNamespacePrefixes[namespacePrefixes.length] = prefix;
                prefixIdx = namespacePrefixes.length;
                namespacePrefixes = newNamespacePrefixes;
            }
        }

        // check for an existing URI
        int uriIdx = -1;
        if (namespaceUris != null) {
            for (int uIdx = namespaceUris.length - 1; uIdx != -1; uIdx--) {
                if (namespaceURI.equals(namespaceUris[uIdx])) {
                    uriIdx = uIdx;
                    break;
                }
            }
        }
        if (uriIdx == -1) {
            // no existing URI, add a new one
            if (namespaceUris == null) {
                namespaceUris = new String[] { namespaceURI };
                uriIdx = 0;
            } else {
                final String[] newNamespaceUris = new String[namespaceUris.length + 1];
                System.arraycopy(namespaceUris, 0,  newNamespaceUris, 0, namespaceUris.length);
                newNamespaceUris[namespaceUris.length] = namespaceURI;
                uriIdx = namespaceUris.length;
                namespaceUris = newNamespaceUris;
            }
        }

        // guard against initial scope use
        if (scopedPrefixUriMappings == null) {
            scopedPrefixUriMappings = new long[1][];
        }
        int mappingIdx = -1;
        long[] prefixUriMappings = scopedPrefixUriMappings[currentScope];
        if (prefixUriMappings == null) {
            prefixUriMappings = new long[1];
            scopedPrefixUriMappings[currentScope] = prefixUriMappings;
            mappingIdx = 0;
        } else {
            // first try and find a deleted mapping
            for (int i = prefixUriMappings.length - 1; i != -1; i--) {
                if (prefixUriMappings[i] == MAPPING_TOMBSTONE) {
                    // found a deleted mapping, so reuse it
                    mappingIdx = i;
                    break;
                }
            }

            if (mappingIdx == -1) {
                // no empty slot, so extend the array
                final long[] newPrefixUriMappings = new long[prefixUriMappings.length + 1];
                System.arraycopy(prefixUriMappings, 0, newPrefixUriMappings, 0, prefixUriMappings.length);
                mappingIdx = prefixUriMappings.length;
                scopedPrefixUriMappings[currentScope] = newPrefixUriMappings;
                prefixUriMappings = newPrefixUriMappings;
            }
        }

        // add the new Prefix -> URI mapping
        final long prefixUriMapping = ((long) uriIdx << 32) | prefixIdx;
        prefixUriMappings[mappingIdx] = prefixUriMapping;
    }

    /**
     * Removes the declaration of the prefix, which has been defined last.
     * Typically called from within {@link org.xml.sax.ContentHandler#endPrefixMapping(String)}.
     *
     * @param prefix The namespace prefix.
     *
     * @throws IllegalArgumentException The prefix is null.
     */
    public void endPrefixMapping(final String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("The namespace prefix must not be null.");
        }

        if (scopedPrefixUriMappings == null) {
            return;
        }

        for (int scopeIdx = currentScope; scopeIdx != -1; scopeIdx--) {
            final long[] prefixUriMappings = scopedPrefixUriMappings[scopeIdx];
            if (prefixUriMappings != null) {
                for (int mapIdx = prefixUriMappings.length - 1; mapIdx != -1; mapIdx--) {
                    final long prefixUriMapping = prefixUriMappings[mapIdx];
                    if (prefixUriMapping == MAPPING_TOMBSTONE) {
                        // skip deleted mapping
                        continue;
                    }

                    final int prefixIdx = (int) (prefixUriMapping & 0xFFFFFFFF);
                    final String nsPrefix = namespacePrefixes[prefixIdx];
                    if (prefix.equals(nsPrefix)) {
                        // Set this mapping as deleted, will be compacted later
                        prefixUriMappings[mapIdx] = MAPPING_TOMBSTONE;
                        attemptCompact();
                        return;
                    }
                }
            }
        }
    }

    /**
     * Resets the Namespace Context's state.
     * Allows reusing the object.
     */
    public void reset() {
        namespacePrefixes = null;
        namespaceUris = null;
        scopedPrefixUriMappings = null;
        currentScope = 0;
        compactAttempts = 0;
    }

    /**
     * Records a compaction attempt.
     * When the number of attempts exceeds the threshold, compaction will be run.
     */
    private void attemptCompact() {
        compactAttempts++;
        if (compactAttempts == compactThreshold) {
            doCompact();
            compactAttempts = 0;
        }
    }

    /**
     * When a mapping is removed we don't immediately remove
     * the entries from {@link #namespacePrefixes} and {@link #namespaceUris}
     * as these may be used by other pre-existing mappings. That means
     * without some sort of compaction, those two arrays could otherwise
     * accumulate prefixes and uris that are no longer used over time.
     *
     * This function compacts {@link #namespacePrefixes} and {@link #namespaceUris}
     * by removing any entries that do not have a corresponding entry in {@link #scopedPrefixUriMappings}.
     */
    private void doCompact() {
        // Try and compact the Prefixes and URIs, if they need compacting get the old to new index mappings for each
        final int[] oldToNewNamespacePrefixesIndicies = compactNamespacePrefixes();
        final int[] oldToNewNamespaceUrisIndicies = compactNamespaceUris();

        // Try and compact the Mappings
        if (scopedPrefixUriMappings != null) {
            for (int scopeIdx = 0; scopeIdx < scopedPrefixUriMappings.length; scopeIdx++) {
                long[] prefixUriMappings = scopedPrefixUriMappings[scopeIdx];

                if (prefixUriMappings != null) {
                    int newIdx = 0;
                    long[] newPrefixUriMappings = new long[prefixUriMappings.length];

                    for (int mappingIdx = 0; mappingIdx < prefixUriMappings.length; mappingIdx++) {
                        final long prefixUriMapping = prefixUriMappings[mappingIdx];
                        if (prefixUriMapping == MAPPING_TOMBSTONE) {
                            // skip deleted mapping
                            continue;
                        }

                        // get the new mapped Prefix index (maybe unchanged, and so the same as the existing one)
                        final int newMappedPrefixIdx;
                        final int existingMappedPrefixIdx = (int) (prefixUriMapping & 0xFFFFFFFF);
                        if (oldToNewNamespacePrefixesIndicies != null) {
                            newMappedPrefixIdx = oldToNewNamespacePrefixesIndicies[existingMappedPrefixIdx];
                        } else {
                            newMappedPrefixIdx = existingMappedPrefixIdx;
                        }

                        // get the new mapped Namespace index (maybe unchanged, and so the same as the existing one)
                        final int newMappedUriIdx;
                        final int existingMappedUriIdx = (int) (prefixUriMapping >> 32);
                        if (oldToNewNamespaceUrisIndicies != null) {
                            newMappedUriIdx = oldToNewNamespaceUrisIndicies[existingMappedUriIdx];
                        } else {
                            newMappedUriIdx = existingMappedUriIdx;
                        }

                        final long newPrefixUriMapping;
                        if (existingMappedPrefixIdx != newMappedPrefixIdx || existingMappedUriIdx != newMappedUriIdx) {
                            // Prefix or Namespace index has changed, so update the mapping
                            newPrefixUriMapping = ((long) newMappedUriIdx << 32) | newMappedPrefixIdx;
                        } else {
                            newPrefixUriMapping = prefixUriMappings[mappingIdx];
                        }
                        newPrefixUriMappings[newIdx++] = newPrefixUriMapping;

                    }

                    // set the updated mappings
                    prefixUriMappings = new long[newIdx];
                    System.arraycopy(newPrefixUriMappings, 0, prefixUriMappings, 0, newIdx);  // shrink mappings
                    scopedPrefixUriMappings[scopeIdx] = prefixUriMappings;
                }
            }
        }
    }

    private int[] compactNamespacePrefixes() {
        int newNamespacePrefixesLen = 0;
        /**
         * Index is the existing Prefix index.
         * Value is the new Prefix index.
         */
        int[] oldToNewNamespacePrefixesIndicies = new int[namespacePrefixes.length];
        for (int pfxIdx = 0; pfxIdx < namespacePrefixes.length; pfxIdx++) {

            int mappedInScope = -1;
            int mappedInMapping = -1;

            if (scopedPrefixUriMappings != null) {
                for (int scopeIdx = 0; scopeIdx < scopedPrefixUriMappings.length; scopeIdx++) {
                    final long[] prefixUriMappings = scopedPrefixUriMappings[scopeIdx];
                    if (prefixUriMappings != null) {
                        for (int mappingIdx = 0; mappingIdx < prefixUriMappings.length; mappingIdx++) {
                            final long prefixUriMapping = prefixUriMappings[mappingIdx];
                            final int mappedPrefixIdx = (int) (prefixUriMapping & 0xFFFFFFFF);

                            if (mappedPrefixIdx == pfxIdx) {
                                mappedInMapping = mappingIdx;
                                break;
                            }
                        }

                        if (mappedInMapping > -1) {
                            mappedInScope = scopeIdx;
                            break;
                        }
                    }
                }
            }

            if (mappedInScope > -1) {
                // Prefix is in use
                oldToNewNamespacePrefixesIndicies[pfxIdx] = newNamespacePrefixesLen++;
            } else {
                // NO mapping that uses this Prefix, so we need to remove this Prefix
                oldToNewNamespacePrefixesIndicies[pfxIdx] = -1;
            }
        }

        if (newNamespacePrefixesLen < namespacePrefixes.length) {
            // There are less Prefixes mapped than we have entries for, so we need to compact the namespacePrefixes

            // compact namespacePrefixes
            final String[] newNamespacesPrefixes = new String[newNamespacePrefixesLen];
            for (int oldNamespacesPrefixIdx = 0; oldNamespacesPrefixIdx < oldToNewNamespacePrefixesIndicies.length; oldNamespacesPrefixIdx++) {
                final int newNamespacesPrefixIdx = oldToNewNamespacePrefixesIndicies[oldNamespacesPrefixIdx];
                if (newNamespacesPrefixIdx != -1) {
                    newNamespacesPrefixes[newNamespacesPrefixIdx] = namespacePrefixes[oldNamespacesPrefixIdx];
                }
            }
            namespacePrefixes = newNamespacesPrefixes;

            return oldToNewNamespacePrefixesIndicies;

        } else {
            return null;
        }
    }

    private int[] compactNamespaceUris() {
        int newNamespaceUrisLen = 0;
        /**
         * Index is the existing Namespace index.
         * Value is the new Namespace index.
         */
        int[] oldToNewNamespaceUrisIndicies = new int[namespaceUris.length];
        for (int uriIdx = 0; uriIdx < namespaceUris.length; uriIdx++) {

            int mappedInScope = -1;
            int mappedInMapping = -1;

            if (scopedPrefixUriMappings != null) {
                for (int scopeIdx = 0; scopeIdx < scopedPrefixUriMappings.length; scopeIdx++) {
                    final long[] prefixUriMappings = scopedPrefixUriMappings[scopeIdx];
                    if (prefixUriMappings != null) {
                        for (int mappingIdx = 0; mappingIdx < prefixUriMappings.length; mappingIdx++) {
                            final long prefixUriMapping = prefixUriMappings[mappingIdx];
                            final int mappedUriIdx = (int) (prefixUriMapping >> 32);

                            if (mappedUriIdx == uriIdx) {
                                mappedInMapping = mappingIdx;
                                break;
                            }
                        }

                        if (mappedInMapping > -1) {
                            mappedInScope = scopeIdx;
                            break;
                        }
                    }
                }
            }

            if (mappedInScope > -1) {
                // Namespace is in use
                oldToNewNamespaceUrisIndicies[uriIdx] = newNamespaceUrisLen++;
            } else {
                // NO mapping that uses this Namespace, so we need to remove this Prefix
                oldToNewNamespaceUrisIndicies[uriIdx] = -1;
            }
        }

        if (newNamespaceUrisLen < namespaceUris.length) {
            // There are less Namespaces mapped than we have entries for, so we need to compact the namespaceUris

            // compact namespacePrefixes
            final String[] newNamespacesPrefixes = new String[newNamespaceUrisLen];
            for (int oldNamespacesPrefixIdx = 0; oldNamespacesPrefixIdx < oldToNewNamespaceUrisIndicies.length; oldNamespacesPrefixIdx++) {
                final int newNamespacesPrefixIdx = oldToNewNamespaceUrisIndicies[oldNamespacesPrefixIdx];
                if (newNamespacesPrefixIdx != -1) {
                    newNamespacesPrefixes[newNamespacesPrefixIdx] = namespaceUris[oldNamespacesPrefixIdx];
                }
            }
            namespaceUris = newNamespacesPrefixes;

            return oldToNewNamespaceUrisIndicies;

        } else {
            return null;
        }
    }

    class PrefixesIterator implements Iterator {
        private final String namespaceURI;

        private int scopeIdx;
        private int mapIdx;
        private String nextPrefix;

        PrefixesIterator(final String namespaceURI) {
            this(namespaceURI, currentScope, -1, null);
        }

        PrefixesIterator(final String namespaceURI, final int scopeIdx, final int mapIdx, final String nextPrefix) {
            this.namespaceURI = namespaceURI;
            this.scopeIdx = scopeIdx;
            this.mapIdx = mapIdx;
            this.nextPrefix = nextPrefix;
        }

        public boolean hasNext() {
            if (nextPrefix != null) {
                return true;
            }

            if (scopedPrefixUriMappings != null) {
                for (; scopeIdx != -1; scopeIdx--) {
                    final long[] prefixUriMappings = scopedPrefixUriMappings[scopeIdx];
                    if (prefixUriMappings != null) {

                        if (mapIdx == -1) {
                            mapIdx = prefixUriMappings.length - 1;
                        }

                        for (; mapIdx != -1; mapIdx--) {
                            final long prefixUriMapping = prefixUriMappings[mapIdx];

                            final int uriIdx = (int) (prefixUriMapping >> 32);
                            final String nsUri = namespaceUris[uriIdx];
                            if (namespaceURI.equals(nsUri)) {
                                final int prefixIdx = (int) (prefixUriMapping & 0xFFFFFFFF);
                                final String nsPrefix = namespacePrefixes[prefixIdx];
                                nextPrefix = nsPrefix;
                                return true;
                            }
                        }
                    }

                    mapIdx = -1;
                }
            }

            return false;
        }

        public Object next() {
            if (nextPrefix != null) {
                final String localCopy = nextPrefix;
                nextPrefix = null;
                mapIdx--;
                if (mapIdx == -1) {
                    scopeIdx--;
                }
                return localCopy;
            }

            if (scopedPrefixUriMappings != null) {
                for (; scopeIdx != -1; scopeIdx--) {
                    final long[] prefixUriMappings = scopedPrefixUriMappings[scopeIdx];
                    if (prefixUriMappings != null) {

                        if (mapIdx == -1) {
                            mapIdx = prefixUriMappings.length - 1;
                        }

                        for (; mapIdx != -1; mapIdx--) {
                            final long prefixUriMapping = prefixUriMappings[mapIdx];

                            final int uriIdx = (int) (prefixUriMapping >> 32);
                            final String nsUri = namespaceUris[uriIdx];
                            if (namespaceURI.equals(nsUri)) {
                                final int prefixIdx = (int) (prefixUriMapping & 0xFFFFFFFF);
                                final String nsPrefix = namespacePrefixes[prefixIdx];
                                return nsPrefix;
                            }
                        }
                    }

                    mapIdx = -1;
                }
            }

            throw new NoSuchElementException("No more elements");
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove is not supported on EmptyIterator");
        }
    }

    static class SingleValueIterator implements Iterator {
        private final String value;
        private boolean hasNext;

        public SingleValueIterator(final String value) {
            this.value = value;
            this.hasNext = true;
        }

        public boolean hasNext() {
            return hasNext;
        }

        public Object next() {
            if (hasNext) {
                hasNext = false;
                return value;
            }
            throw new NoSuchElementException("No more elements");
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove is not supported on EmptyIterator");
        }
    }

    static class EmptyIterator implements Iterator {
        public boolean hasNext() {
            return false;
        }

        public Object next() {
            throw new NoSuchElementException("EmptyIterator never has any elements");
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove is not supported on EmptyIterator");
        }
    }
}
