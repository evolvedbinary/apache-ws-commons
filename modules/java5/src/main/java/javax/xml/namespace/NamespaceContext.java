/*
 * Copyright 2003, 2004  The Apache Software Foundation
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
package javax.xml.namespace;

/** <p>The <code>NamespaceContext</code> interface is a helper tool for
 * XML parsing applications which need to know the mappings between XML
 * namespace prefixes and namespace URI's. As such, it is closely related
 * to the events
 * {@link org.xml.sax.ContentHandler#startPrefixMapping(String,String)},
 * and {@link org.xml.sax.ContentHandler#endPrefixMapping(String)} in
 * {@link org.xml.sax.ContentHandler}.</p>
 * <p>In what follows, it is important to note, that a single prefix
 * can only be mapped to a single namespace URI at any time. However,
 * the converse is not true: Multiple prefixes can be mapped to the
 * same namespace URI's.</p>
 * <p>For example, in the case of an XML Schema parser, an instance
 * of <code>NamespaceContext</code> might be used to resolve the namespace
 * URI's of referenced data types, and element or attribute names, which
 * are typically given as qualified names, including a prefix and a local
 * name.</p>
 *
 * @author JSR-31
 * @since JAXB 1.0
 */
public interface NamespaceContext {
  /** <p>Given a prefix, returns the namespace URI associated with the prefix.
   * More precisely, the following rules apply:
   * <table border="1">
   *   <tr><th>Prefix (Input)</th><th>Namespace URI (Output)</th></tr>
   *   <tr><th>{@link javax.xml.XMLConstants#DEFAULT_NS_PREFIX} ("")</th>
   *     <td>The current default namespace URI or null, if there is no
   *       such default. (In which case the absence of a prefix indicates
   *       the absence of a namespace URI.)</td></tr>
   *   <tr><th>{@link javax.xml.XMLConstants#XML_NS_PREFIX} ("xml")</th>
   *     <td>{@link javax.xml.XMLConstants#XML_NS_URI} ("http://www.w3.org/XML/1998/namespace")</td></tr>
   *   <tr><th>{@link javax.xml.XMLConstants#XMLNS_ATTRIBUTE} ("xmlns")</th>
   *     <td>{@link javax.xml.XMLConstants#XMLNS_ATTRIBUTE_NS_URI} ("http://www.w3.org/2000/xmlns/")</td>
   *   </tr>
   *   <tr><th>Any other prefix</th><td>The namespace URI currently mapped to the
   *     prefix or null, if no such mapping is established.</td></tr>
   * </table></p>
   * @param pPrefix The prefix being looked up in the list of mappings.
   * @return The Namespace URI to which the input prefix is currently mapped
   *   or null, if there is no such mapping.
   * @throws IllegalArgumentException The input prefix is null.
   */
  public String getNamespaceURI(String pPrefix);


  /** <p>This method returns a prefix, which is currently mapped to the given
   * namespace URI. Note, that multiple prefixes may be mapped to the namespace
   * URI, in which case the returned prefix is undetermined. Do not make any
   * assumptions on the order in such cases. It is a better choice to use
   * {@link #getPrefixes(String)} instead, if you depend on some order
   * <table border="1">
   *   <tr><th>Namespace URI (Input)</th><th>Prefix (Output)</th></tr>
   *   <tr><th>Current default namespace URI</th>
   *     <td>{@link javax.xml.XMLConstants#DEFAULT_NS_PREFIX} ("")</td></tr>
   *   <tr><th>{@link javax.xml.XMLConstants#XML_NS_URI} ("http://www.w3.org/XML/1998/namespace")</th>
   *     <td>{@link javax.xml.XMLConstants#XML_NS_PREFIX} ("xml")</td></tr>
   *   <tr><th>{@link javax.xml.XMLConstants#XMLNS_ATTRIBUTE_NS_URI} ("http://www.w3.org/2000/xmlns/")</th>
   *     <td>{@link javax.xml.XMLConstants#XMLNS_ATTRIBUTE}</td></tr>
   * </table></p>
   *
   * @param pNamespaceURI The namespace URI being looked up in the list of mappings.
   * @return A prefix currently mapped to the given namespace URI or null, if there
   *   is no such mapping
   * @throws IllegalArgumentException The input URI is null
   */
  public java.lang.String getPrefix(java.lang.String pNamespaceURI);


  /** <p>This method returns a collection of prefixes, which are currently mapped
   * to the given namespace URI. Note, that the collection may contain more than
   * one prefix, in which case the order is undetermined. If you do not depend
   * on a certain order and any prefix will do, you may choose to use
   * {@link #getPrefix(String)} instead. The following table describes the
   * returned values in more details:
   * <table border="1">
   *   <tr><th>Namespace URI (Input)</th><th>Prefix collection (Output)</th></tr>
   *   <tr><th>{@link javax.xml.XMLConstants#XML_NS_URI} ("http://www.w3.org/XML/1998/namespace")</th>
   *     <td>Collection with a single element: {@link javax.xml.XMLConstants#XML_NS_PREFIX} ("xml")</td></tr>
   *   <tr><th>{@link javax.xml.XMLConstants#XMLNS_ATTRIBUTE_NS_URI} ("http://www.w3.org/2000/xmlns/")</th>
   *     <td>Collection with a single element: {@link javax.xml.XMLConstants#XMLNS_ATTRIBUTE}</td></tr>
   * </table></p>
   *
   * @param pNamespaceURI The namespace URI being looked up in the list of
   *   mappings or null, if there is no such mapping.
   * @return An unmodifiable {@link java.util.Iterator}: Using it's
   *   {@link java.util.Iterator#remove()} method throws an
   *   {@link UnsupportedOperationException}.
   * @throws IllegalStateException The input URI is null
   */
  public java.util.Iterator getPrefixes(java.lang.String pNamespaceURI);
}
