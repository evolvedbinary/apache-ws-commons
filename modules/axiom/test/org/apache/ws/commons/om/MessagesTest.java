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

import org.apache.ws.commons.soap.SOAPEnvelope;

public class MessagesTest extends OMTestCase {
    SOAPEnvelope soapEnvelope;

    public MessagesTest(String testName) {
        super(testName);
    }

    public void testMessageWithLotOfWhiteSpaces() throws OMException,
            Exception {
        soapEnvelope =
                (SOAPEnvelope) OMTestUtils.getOMBuilder(
                        getTestResourceFile(TestConstants.WHITESPACE_MESSAGE))
                .getDocumentElement();
        OMTestUtils.walkThrough(soapEnvelope);
    }

    public void testMinimalMessage() throws OMException, Exception {
        soapEnvelope =
                (SOAPEnvelope) OMTestUtils.getOMBuilder(
                        getTestResourceFile(TestConstants.MINIMAL_MESSAGE))
                .getDocumentElement();
        OMTestUtils.walkThrough(soapEnvelope);
    }

    public void testReallyBigMessage() throws OMException, Exception {
        soapEnvelope =
                (SOAPEnvelope) OMTestUtils.getOMBuilder(
                        getTestResourceFile(TestConstants.REALLY_BIG_MESSAGE))
                .getDocumentElement();
        OMTestUtils.walkThrough(soapEnvelope);
    }

    public void testEmptyBodiedMessage() throws OMException, Exception {
        soapEnvelope =
                (SOAPEnvelope) OMTestUtils.getOMBuilder(
                        getTestResourceFile(TestConstants.EMPTY_BODY_MESSAGE))
                .getDocumentElement();
        OMTestUtils.walkThrough(soapEnvelope);
    }


}
