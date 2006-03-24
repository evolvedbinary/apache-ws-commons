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

import org.apache.axiom.soap.SOAPFactory;


/**
 * Class FactoryFinder
 */
class FactoryFinder {
    private static final String DEFAULT_OM_FACTORY_CLASS_NAME =
            "org.apache.axiom.om.impl.llom.factory.OMLinkedListImplFactory";
    private static final String DEFAULT_SOAP11_FACTORY_CLASS_NAME =
            "org.apache.ws.commons.soap.impl.llom.soap11.SOAP11Factory";
    private static final String DEFAULT_SOAP12_FACTORY_CLASS_NAME =
            "org.apache.ws.commons.soap.impl.llom.soap12.SOAP12Factory";

    /**
     * @param loader
     * @return
     * @throws OMFactoryException
     */


    private static Object findFactory(ClassLoader loader,
                                      String factoryClass,
                                      String systemPropertyName)
            throws OMFactoryException {

        String factoryClassName = factoryClass;

        //first look for a java system property
        if (systemPropertyName != null &&
            System.getProperty(systemPropertyName) != null) {
            factoryClassName = System.getProperty(systemPropertyName);
        }

        Object factory;
        try {
            if (loader == null) {
                factory = Class.forName(factoryClassName).newInstance();
            } else {
                factory = loader.loadClass(factoryClassName).newInstance();
            }
        } catch (Exception e) {
            throw new OMFactoryException(e);
        }
        return factory;
    }

    /**
     * The searching for the factory class happens in the following order
     * 1. look for a system property called <b>soap11.factory</b>. this can be set by
     * passing the -Dsoap11.factory="classname"
     * 2. Pick the default factory class.
     * it is the class hardcoded at the constant DEFAULT_SOAP11_FACTORY_CLASS_NAME
     *
     * @param loader
     * @return
     * @throws OMFactoryException
     */
    public static SOAPFactory findSOAP11Factory(ClassLoader loader)
            throws OMFactoryException {
        return (SOAPFactory) findFactory(loader,
                                         DEFAULT_SOAP11_FACTORY_CLASS_NAME,
                                         OMAbstractFactory.SOAP11_FACTORY_NAME_PROPERTY);
    }

    /**
     * The searching for the factory class happens in the following order
     * 1. look for a system property called <b>soap12.factory</b>. this can be set by
     * passing the -Dsoap12.factory="classname"
     * 2. Pick the default factory class.
     * it is the class hardcoded at the constant DEFAULT_SOAP12_FACTORY_CLASS_NAME
     *
     * @param loader
     * @return
     * @throws OMFactoryException
     */
    public static SOAPFactory findSOAP12Factory(ClassLoader loader)
            throws OMFactoryException {
        return (SOAPFactory) findFactory(loader,
                                         DEFAULT_SOAP12_FACTORY_CLASS_NAME,
                                         OMAbstractFactory.SOAP12_FACTORY_NAME_PROPERTY);
    }

    /**
     * The searching for the factory class happens in the following order
     * 1. look for a system property called <b>om.factory</b>. this can be set by
     * passing the -Dom.factory="classname"
     * 2. Pick the default factory class.
     * it is the class hardcoded at the constant DEFAULT_OM_FACTORY_CLASS_NAME
     *
     * @param loader
     * @return
     * @throws OMFactoryException
     */
    public static OMFactory findOMFactory(ClassLoader loader)
            throws OMFactoryException {
        return (OMFactory) findFactory(loader,
                                       DEFAULT_OM_FACTORY_CLASS_NAME,
                                       OMAbstractFactory.OM_FACTORY_NAME_PROPERTY);
    }

    /**
     *
     * @param classLoader
     * @param soapFactory
     * @return The SOAPFactory
     */
    public static SOAPFactory findSOAPFactory(final ClassLoader classLoader,
                                              final String soapFactory) {
        return (SOAPFactory) findFactory(classLoader,
                                         soapFactory,
                                         null);
    }
}
