/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axiom.util.stax.dialect;

import java.util.Properties;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;

import junit.framework.TestSuite;

public class DialectTest extends TestSuite {
    private final ClassLoader classLoader;
    private final Properties props;
    private StAXDialect dialect;
    
    public DialectTest(ClassLoader classLoader, String name, Properties props) {
        super(name);
        this.classLoader = classLoader;
        this.props = props;
        addDialectTest(new CreateXMLEventWriterWithNullEncodingTestCase());
        addDialectTest(new CreateXMLStreamReaderThreadSafetyTestCase());
        addDialectTest(new CreateXMLStreamWriterThreadSafetyTestCase());
        addDialectTest(new CreateXMLStreamWriterWithNullEncodingTestCase());
        addDialectTest(new DisallowDoctypeDeclWithDenialOfServiceTestCase());
        addDialectTest(new DisallowDoctypeDeclWithExternalSubsetTestCase());
        addDialectTest(new DisallowDoctypeDeclWithInternalSubsetTestCase());
        addDialectTest(new EnableCDataReportingTestCase());
        addDialectTest(new GetCharacterEncodingSchemeTestCase());
        addDialectTest(new GetEncodingExternalTestCase());
        addDialectTest(new GetEncodingFromDetectionTestCase("UTF-8", "UTF-8"));
        // The case of UTF-16 with a byte order marker is not well defined:
        // * One may argue that the result should be UTF-16BE or UTF-16LE because
        //   otherwise the information about the byte order is lost.
        // * On the other hand, one may argue that the result should be UTF-16
        //   because UTF-16BE or UTF-16LE may be interpreted as an indication that
        //   there should be no BOM.
        // Therefore we accept both results.
        addDialectTest(new GetEncodingFromDetectionTestCase("UnicodeBig", new String[] { "UTF-16", "UTF-16BE" } ));
        addDialectTest(new GetEncodingFromDetectionTestCase("UnicodeLittle", new String[] { "UTF-16", "UTF-16LE" }));
        // Here there is no doubt; if the encoding is UTF-16 without BOM, then the
        // parser should report the detected byte order.
        addDialectTest(new GetEncodingFromDetectionTestCase("UnicodeBigUnmarked", "UTF-16BE"));
        addDialectTest(new GetEncodingFromDetectionTestCase("UnicodeLittleUnmarked", "UTF-16LE"));
        addDialectTest(new GetEncodingTestCase());
        addDialectTest(new GetEncodingWithCharacterStreamTestCase());
        addDialectTest(new GetLocalNameIllegalStateExceptionTestCase(XMLStreamConstants.START_ELEMENT, false));
        addDialectTest(new GetLocalNameIllegalStateExceptionTestCase(XMLStreamConstants.END_ELEMENT, false));
        addDialectTest(new GetLocalNameIllegalStateExceptionTestCase(XMLStreamConstants.PROCESSING_INSTRUCTION, true));
        addDialectTest(new GetLocalNameIllegalStateExceptionTestCase(XMLStreamConstants.CHARACTERS, true));
        addDialectTest(new GetLocalNameIllegalStateExceptionTestCase(XMLStreamConstants.COMMENT, true));
        addDialectTest(new GetLocalNameIllegalStateExceptionTestCase(XMLStreamConstants.SPACE, true));
        addDialectTest(new GetLocalNameIllegalStateExceptionTestCase(XMLStreamConstants.START_DOCUMENT, true));
        addDialectTest(new GetLocalNameIllegalStateExceptionTestCase(XMLStreamConstants.END_DOCUMENT, true));
        addDialectTest(new GetLocalNameIllegalStateExceptionTestCase(XMLStreamConstants.ENTITY_REFERENCE, false));
        addDialectTest(new GetLocalNameIllegalStateExceptionTestCase(XMLStreamConstants.DTD, true));
        addDialectTest(new GetLocalNameIllegalStateExceptionTestCase(XMLStreamConstants.CDATA, true));
        addDialectTest(new GetNameIllegalStateExceptionTestCase(XMLStreamConstants.START_ELEMENT, false));
        addDialectTest(new GetNameIllegalStateExceptionTestCase(XMLStreamConstants.END_ELEMENT, false));
        addDialectTest(new GetNameIllegalStateExceptionTestCase(XMLStreamConstants.PROCESSING_INSTRUCTION, true));
        addDialectTest(new GetNameIllegalStateExceptionTestCase(XMLStreamConstants.CHARACTERS, true));
        addDialectTest(new GetNameIllegalStateExceptionTestCase(XMLStreamConstants.COMMENT, true));
        addDialectTest(new GetNameIllegalStateExceptionTestCase(XMLStreamConstants.SPACE, true));
        addDialectTest(new GetNameIllegalStateExceptionTestCase(XMLStreamConstants.START_DOCUMENT, true));
        addDialectTest(new GetNameIllegalStateExceptionTestCase(XMLStreamConstants.END_DOCUMENT, true));
        addDialectTest(new GetNameIllegalStateExceptionTestCase(XMLStreamConstants.ENTITY_REFERENCE, true));
        addDialectTest(new GetNameIllegalStateExceptionTestCase(XMLStreamConstants.DTD, true));
        addDialectTest(new GetNameIllegalStateExceptionTestCase(XMLStreamConstants.CDATA, true));
        addDialectTest(new GetNamespaceContextImplicitNamespacesTestCase());
        addDialectTest(new GetNamespaceURIIllegalStateExceptionTestCase(XMLStreamConstants.START_ELEMENT, false));
        addDialectTest(new GetNamespaceURIIllegalStateExceptionTestCase(XMLStreamConstants.END_ELEMENT, false));
        addDialectTest(new GetNamespaceURIIllegalStateExceptionTestCase(XMLStreamConstants.PROCESSING_INSTRUCTION, true));
        addDialectTest(new GetNamespaceURIIllegalStateExceptionTestCase(XMLStreamConstants.CHARACTERS, true));
        addDialectTest(new GetNamespaceURIIllegalStateExceptionTestCase(XMLStreamConstants.COMMENT, true));
        addDialectTest(new GetNamespaceURIIllegalStateExceptionTestCase(XMLStreamConstants.SPACE, true));
        addDialectTest(new GetNamespaceURIIllegalStateExceptionTestCase(XMLStreamConstants.START_DOCUMENT, true));
        addDialectTest(new GetNamespaceURIIllegalStateExceptionTestCase(XMLStreamConstants.END_DOCUMENT, true));
        addDialectTest(new GetNamespaceURIIllegalStateExceptionTestCase(XMLStreamConstants.ENTITY_REFERENCE, true));
        addDialectTest(new GetNamespaceURIIllegalStateExceptionTestCase(XMLStreamConstants.DTD, true));
        addDialectTest(new GetNamespaceURIIllegalStateExceptionTestCase(XMLStreamConstants.CDATA, true));
        addDialectTest(new GetPrefixIllegalStateExceptionTestCase(XMLStreamConstants.START_ELEMENT, false));
        addDialectTest(new GetPrefixIllegalStateExceptionTestCase(XMLStreamConstants.END_ELEMENT, false));
        addDialectTest(new GetPrefixIllegalStateExceptionTestCase(XMLStreamConstants.PROCESSING_INSTRUCTION, true));
        addDialectTest(new GetPrefixIllegalStateExceptionTestCase(XMLStreamConstants.CHARACTERS, true));
        addDialectTest(new GetPrefixIllegalStateExceptionTestCase(XMLStreamConstants.COMMENT, true));
        addDialectTest(new GetPrefixIllegalStateExceptionTestCase(XMLStreamConstants.SPACE, true));
        addDialectTest(new GetPrefixIllegalStateExceptionTestCase(XMLStreamConstants.START_DOCUMENT, true));
        addDialectTest(new GetPrefixIllegalStateExceptionTestCase(XMLStreamConstants.END_DOCUMENT, true));
        addDialectTest(new GetPrefixIllegalStateExceptionTestCase(XMLStreamConstants.ENTITY_REFERENCE, true));
        addDialectTest(new GetPrefixIllegalStateExceptionTestCase(XMLStreamConstants.DTD, true));
        addDialectTest(new GetPrefixIllegalStateExceptionTestCase(XMLStreamConstants.CDATA, true));
        addDialectTest(new GetPrefixWithNoPrefixTestCase());
        addDialectTest(new GetTextInPrologTestCase());
        addDialectTest(new GetVersionTestCase());
        addDialectTest(new HasNameTestCase(XMLStreamConstants.START_ELEMENT, true));
        addDialectTest(new HasNameTestCase(XMLStreamConstants.END_ELEMENT, true));
        addDialectTest(new HasNameTestCase(XMLStreamConstants.PROCESSING_INSTRUCTION, false));
        addDialectTest(new HasNameTestCase(XMLStreamConstants.CHARACTERS, false));
        addDialectTest(new HasNameTestCase(XMLStreamConstants.COMMENT, false));
        addDialectTest(new HasNameTestCase(XMLStreamConstants.SPACE, false));
        addDialectTest(new HasNameTestCase(XMLStreamConstants.START_DOCUMENT, false));
        addDialectTest(new HasNameTestCase(XMLStreamConstants.END_DOCUMENT, false));
        addDialectTest(new HasNameTestCase(XMLStreamConstants.ENTITY_REFERENCE, false));
        addDialectTest(new HasNameTestCase(XMLStreamConstants.DTD, false));
        addDialectTest(new HasNameTestCase(XMLStreamConstants.CDATA, false));
        addDialectTest(new HasTextTestCase(XMLStreamConstants.START_ELEMENT, false));
        addDialectTest(new HasTextTestCase(XMLStreamConstants.END_ELEMENT, false));
        addDialectTest(new HasTextTestCase(XMLStreamConstants.PROCESSING_INSTRUCTION, false));
        addDialectTest(new HasTextTestCase(XMLStreamConstants.CHARACTERS, true));
        addDialectTest(new HasTextTestCase(XMLStreamConstants.COMMENT, true));
        addDialectTest(new HasTextTestCase(XMLStreamConstants.SPACE, true));
        addDialectTest(new HasTextTestCase(XMLStreamConstants.START_DOCUMENT, false));
        addDialectTest(new HasTextTestCase(XMLStreamConstants.END_DOCUMENT, false));
        addDialectTest(new HasTextTestCase(XMLStreamConstants.ENTITY_REFERENCE, true));
        addDialectTest(new HasTextTestCase(XMLStreamConstants.DTD, true));
        // Note: CDATA events are actually not mentioned in the Javadoc of XMLStreamReader#hasText().
        //       This is because reporting CDATA sections as CDATA events is an implementation
        //       specific feature. Nevertheless, for obvious reasons, we expect hasText to
        //       return true in this case.
        addDialectTest(new HasTextTestCase(XMLStreamConstants.CDATA, true));
        addDialectTest(new IsCharactersOnCDATASectionTestCase());
        addDialectTest(new IsCharactersTestCase(XMLStreamConstants.START_ELEMENT, false));
        addDialectTest(new IsCharactersTestCase(XMLStreamConstants.END_ELEMENT, false));
        addDialectTest(new IsCharactersTestCase(XMLStreamConstants.PROCESSING_INSTRUCTION, false));
        addDialectTest(new IsCharactersTestCase(XMLStreamConstants.CHARACTERS, true));
        addDialectTest(new IsCharactersTestCase(XMLStreamConstants.COMMENT, false));
        addDialectTest(new IsCharactersTestCase(XMLStreamConstants.SPACE, false));
        addDialectTest(new IsCharactersTestCase(XMLStreamConstants.START_DOCUMENT, false));
        addDialectTest(new IsCharactersTestCase(XMLStreamConstants.END_DOCUMENT, false));
        addDialectTest(new IsCharactersTestCase(XMLStreamConstants.ENTITY_REFERENCE, false));
        addDialectTest(new IsCharactersTestCase(XMLStreamConstants.DTD, false));
        addDialectTest(new IsCharactersTestCase(XMLStreamConstants.CDATA, false));
        addDialectTest(new IsStandaloneTestCase());
        addDialectTest(new MaskedNamespaceTestCase());
        addDialectTest(new NextAfterEndDocumentTestCase());
        addDialectTest(new SetPrefixScopeTestCase());
        addDialectTest(new StandaloneSetTestCase());
        addDialectTest(new WriteStartDocumentWithNullEncodingTestCase());
    }
    
    private void addDialectTest(DialectTestCase testCase) {
        testCase.init(this);
        addTest(testCase);
    }
    
    XMLInputFactory newXMLInputFactory() {
        String className = props == null ? null : props.getProperty(XMLInputFactory.class.getName());
        if (className == null) {
            ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            try {
                XMLInputFactory factory = XMLInputFactory.newInstance();
                if (classLoader != ClassLoader.getSystemClassLoader()
                        && factory.getClass().getClassLoader() != classLoader) {
                    throw new FactoryConfigurationError("Wrong factory!");
                }
                return factory;
            } finally {
                Thread.currentThread().setContextClassLoader(savedClassLoader);
            }
        } else {
            try {
                return (XMLInputFactory)classLoader.loadClass(className).newInstance();
            } catch (Exception ex) {
                throw new FactoryConfigurationError(ex);
            }
        }
    }
    
    XMLInputFactory newNormalizedXMLInputFactory() {
        XMLInputFactory factory = newXMLInputFactory();
        if (dialect == null) {
            dialect = StAXDialectDetector.getDialect(factory.getClass());
        }
        return dialect.normalize(factory);
    }

    XMLOutputFactory newXMLOutputFactory() {
        String className = props == null ? null : props.getProperty(XMLOutputFactory.class.getName());
        if (className == null) {
            ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            try {
                XMLOutputFactory factory = XMLOutputFactory.newInstance();
                if (classLoader != ClassLoader.getSystemClassLoader()
                        && factory.getClass().getClassLoader() != classLoader) {
                    throw new FactoryConfigurationError("Wrong factory!");
                }
                return factory;
            } finally {
                Thread.currentThread().setContextClassLoader(savedClassLoader);
            }
        } else {
            try {
                return (XMLOutputFactory)classLoader.loadClass(className).newInstance();
            } catch (Exception ex) {
                throw new FactoryConfigurationError(ex);
            }
        }
    }
    
    XMLOutputFactory newNormalizedXMLOutputFactory() {
        XMLOutputFactory factory = newXMLOutputFactory();
        if (dialect == null) {
            dialect = StAXDialectDetector.getDialect(factory.getClass());
        }
        return dialect.normalize(factory);
    }
    
    StAXDialect getDialect() {
        return dialect;
    }
}
