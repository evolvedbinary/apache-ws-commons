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

import java.io.Serializable;


/** <p>A <code>QName</code> is a qualified name, as specified by
 * XML Schema Part2: Datatypes specification, Namespaces in XML, Namespaces in XML Errata.
 * A qualified name is made up of a namespace URI, a local part, and a prefix.
 * The prefix is not really a part of the <code>QName</code> and
 * remains only to provide lexical information. It is <em>not</em>
 * used in the {@link #equals(Object)} or {@link #hashCode()}
 * methods.</p>
 * <p>Namespace URI and prefix may be omitted, in which case the
 * default value "" (empty string) is used.</p>
 * <p>Instances of <code>QName</code> are immutable. You may safely
 * store references.</p>
 */
public class QName implements Serializable {
    private static final long serialVersionUID = 4418622981026545151L;
	private final String namespaceURI, localPart, prefix;
	
	/** <p>Creates a new <code>QName</code> with the given
	 * <code>pNamespaceURI</code> and <code>pLocalPart</code>. The
	 * prefix is set to "" (empty string).</p>
	 * @param pNamespaceURI The namespace URI; may be null, in which case
	 *   the default value "" (empty string) is used.
	 * @param pLocalPart The local part.
	 * @throws IllegalArgumentException The local part was null.
	 */
	public QName(String pNamespaceURI, String pLocalPart) {
		if (pLocalPart == null) {
			throw new IllegalArgumentException("The local part must not be null");
		}
		namespaceURI = pNamespaceURI == null ? "" : pNamespaceURI;
		localPart = pLocalPart;
		prefix = "";
	}
	
	/** <p>Creates a new <code>QName</code> with the given
	 * <code>pNamespaceURI</code>, <code>pLocalPart</code>, and
	 * <code>pPrefix</code>.</p>
	 * @param pNamespaceURI The namespace URI; may be null, in which case
	 *   the default value "" (empty string) is used.
	 * @param pLocalPart The local part.
	 * @param pPrefix The prefix. Must not be null. Use "" (empty string)
	 *   to indicate that no namespace URI is present or the namespace
	 *   URI is not relevant.
	 * @throws IllegalArgumentException The local part or the prefix was null.
	 */
	public QName(String pNamespaceURI, String pLocalPart, java.lang.String pPrefix) {
		if (pLocalPart == null) {
			throw new IllegalArgumentException("The local part must not be null");
		}
		if (pPrefix == null) {
			throw new IllegalArgumentException("The prefix must not be null");
		}
		namespaceURI = pNamespaceURI == null ? "" : pNamespaceURI;
		localPart = pLocalPart;
		prefix = pPrefix;
	}
	
	/** <p>Creates a new <code>QName</code> with the given
	 * <code>pLocalPart</code>, the namespace URI "" (empty string),
	 * and the prefix "" (empty string).</p>
	 * @param pLocalPart The local part.
	 * @throws IllegalArgumentException The local part or the prefix was null.
	 */
	public QName(String pLocalPart) {
		if (pLocalPart == null) {
			throw new IllegalArgumentException("The local part must not be null");
		}
		namespaceURI = "";
		localPart = pLocalPart;
		prefix = "";
	}
	
	/** <p>Returns the namespace URI.</p>
	 * @return Namespace URI or "" (empty string) to indicate the absence
	 *   of a namespace.
	 */
	public String getNamespaceURI() {
		return namespaceURI;
	}
	
	/** <p>Returns the local part of the <code>QName</code>.</p>
	 * @return The local part.
	 */
	public String getLocalPart() {
		return localPart;
	}
	
	/** <p>Returns the namespace prefix.</p>
	 * @return The namespace prefix or "" (empty string) to indicate the
	 *   default namespace
	 */
	public String getPrefix() {
		return prefix;
	}
	
	/** <p>Returns true, if
	 * <ul>
	 *   <li><code>pOther</code> instanceof QName</li>
	 *   <li>getNamespaceURI().equals(pOther.getNamespaceURI())</li>
	 *   <li>getLocalPart().equals(pOther.getLocalPart())</li>
	 * </ul>
	 * <em>Note</em>: The prefix is ignored.</p>
	 */
	public boolean equals(Object pOther) {
		if (!(pOther instanceof QName)) {
			return false;
		}
		QName other = (QName) pOther;
		return namespaceURI.equals(other.namespaceURI)  &&  localPart.equals(other.localPart);
	}
	
	/** <p>Returns the <code>QName</code>'s hash code.
	 * The prefix is ignored when calculating the hash code.</p>
	 */
	public int hashCode() {
		return namespaceURI.hashCode() + localPart.hashCode();
	}
	
	/** <p>Converts the QName into a string representation. The current
	 * implementation returns the local part, if the namespace URI is
	 * "" (empty string). Otherwise returns "{" + namespaceURI + "}" + localPart.
	 * The prefix is ignored.</p>
	 * <p>The representation is subject to changes, as there is currently no
	 * standard representation for a <code>QName</code>. You should use this
	 * method for debugging or logging purposes only.</p>
	 */
	public java.lang.String toString() {
		return namespaceURI.length() == 0 ?
				localPart : "{" + namespaceURI + "}" + localPart;
	}
	
	/** <p>Parses the given string representation of a <code>pQName</code>.
	 * The <code>QName</code> is expected to have the same representation
	 * than returned by {@link #toString()}.</p>
	 * <p>It is not possible to specify a prefix. The returned
	 * <code>QName</code> will always have the prefix "" (empty string).</p>
	 * @param pQName String representation of a QName, as generated by
	 *   {@link #toString()}.
	 * @return QName with the prefix "" (empty string)
	 * @throws IllegalArgumentException The given <code>pQName</code>
	 *   was null or empty.
	 */
	public static QName valueOf(String pQName) {
		if (pQName == null) {
			throw new IllegalArgumentException("The string representation of a QName must not be null.");
		}
		if (pQName.charAt(0) == '{') {
			int end = pQName.indexOf('}', 1);
			if (end == -1) {
				throw new IllegalArgumentException("Expected a terminator ('}') of the namespace URI.");
			}
			return new QName(pQName.substring(1, end), pQName.substring(end+1));
		} else {
			return new QName(pQName);
		}
	}
}
