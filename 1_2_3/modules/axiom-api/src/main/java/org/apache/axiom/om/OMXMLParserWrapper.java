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

package org.apache.axiom.om;

/** Interface OMXMLParserWrapper */
public interface OMXMLParserWrapper {
    /**
     * Proceed the parser one step and return the event value.
     *
     * @return Returns int.
     * @throws org.apache.axiom.om.OMException
     *
     * @throws OMException
     */
    int next() throws OMException;

    /**
     * Discards the current element. This will remove the given element and its decendants.
     *
     * @param el
     * @throws OMException
     *
     * @throws OMException
     */
    void discard(OMElement el) throws OMException;

    /**
     * @param b
     * @throws org.apache.axiom.om.OMException
     *
     * @throws OMException
     */
    void setCache(boolean b) throws OMException;

    /**
     * Allows to access the underlying parser. Since the parser depends on the underlying
     * implementation, an Object is returned. However the implementations may have restrictions in
     * letting access to the parser.
     *
     * @return Returns Object.
     */
    Object getParser();

    /** @return Returns the complete status. */
    boolean isCompleted();

    /** @return Returns the document element. */
    OMElement getDocumentElement();

    /**
     * Returns the type of the builder. Can be either PUSH_TYPE_BUILDER or PULL_TYPE_BUILDER.
     *
     * @return Returns short.
     */
    short getBuilderType();

    /**
     * Registers an external content handler. Especially useful for push type builders. Throws an
     * unsupportedOperationException if such handler registration is not supported.
     *
     * @param obj
     */
    void registerExternalContentHandler(Object obj);

    /**
     * get the registered external content handler
     *
     * @return Returns Object.
     */
    Object getRegisteredContentHandler();
}
