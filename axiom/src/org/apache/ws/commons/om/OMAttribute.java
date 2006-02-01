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

package org.apache.ws.commons.om;

import javax.xml.namespace.QName;

/**
 * Interface OMAttribute
 */
public interface OMAttribute {
    /**
     * @return Returns localName.
     */
    public String getLocalName();

    /**
     * @param localName
     */
    public void setLocalName(String localName);

    /**
     * @return Returns String.
     */
    public String getAttributeValue();

    /**
     * @param value
     */
    public void setAttributeValue(String value);

    /**
     * @param omNamespace
     */
    public void setOMNamespace(OMNamespace omNamespace);

    /**
     * @return Returns OMNamespace.
     */
    public OMNamespace getNamespace();

    /**
     * @return Returns javax.xml.namespace.QName
     */
    public QName getQName();
}
