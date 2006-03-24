/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.ws.commons.om.util;

import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.om.OMNode;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Helper class to provide extra utility stuff against elements.
 * The code is designed to work with any element implementation.
 */

public class ElementHelper {

    private OMElement element;

    /**
     * Constructs and binds to an element.
     * @param element element to work with
     */
    public ElementHelper(OMElement element) {
        this.element = element;
    }

    /**
     * Turns a prefix:local qname string into a proper QName, evaluating it in the OMElement context.
     *
     * @param qname                    qname to resolve
     * @param defaultToParentNameSpace flag that controls behaviour when there is no namespace.
     * @return Returns null for any failure to extract a qname.
     */
    public QName resolveQName(String qname, boolean defaultToParentNameSpace) {
        int colon = qname.indexOf(':');
        if (colon < 0) {
            if (defaultToParentNameSpace) {
                //get the parent ns and use it for the child
                OMNamespace namespace = element.getNamespace();
                return new QName(namespace.getName(), qname, namespace.getPrefix());
            } else {
                //else things without no prefix are local.
                return new QName(qname);
            }
        }
        String prefix = qname.substring(0, colon);
        String local = qname.substring(colon + 1);
        if (local.length() == 0) {
            //empy local, exit accordingly
            return null;
        }

        OMNamespace namespace = element.findNamespaceURI(prefix);
        if (namespace == null) {
            return null;
        }
        return new QName(namespace.getName(), local, prefix);
    }

    /**
     * Turns a prefix:local qname string into a proper QName, evaluating it in the OMElement context.
     * Unprefixed qnames resolve to the local namespace.
     *
     * @param qname prefixed qname string to resolve
     * @return Returns null for any failure to extract a qname.
     */
    public QName resolveQName(String qname) {
        return resolveQName(qname, true);
    }

    public static void setNewElement(OMElement parent,
                                     OMElement myElement,
                                     OMElement newElement) {
        if (myElement != null) {
            myElement.discard();
        }
        parent.addChild(newElement);
        myElement = newElement;
    }

    public static OMElement getChildWithName(OMElement parent,
                                             String childName) {
        Iterator childrenIter = parent.getChildren();
        while (childrenIter.hasNext()) {
            OMNode node = (OMNode) childrenIter.next();
            if (node.getType() == OMNode.ELEMENT_NODE &&
                    childName.equals(((OMElement) node).getLocalName())) {
                return (OMElement) node;
            }
        }
        return null;
    }
}
