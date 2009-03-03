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

package org.apache.axiom.soap;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.axiom.om.OMMetaFactory;

public class SOAPHeaderTestBase extends SOAPHeaderTestCase {
    private static final String ROLE_URI = "http://schemas.xmlsoap.org/soap/envelope/actor/next";

    public SOAPHeaderTestBase(OMMetaFactory omMetaFactory) {
        super(omMetaFactory);
    }

    //SOAP 1.1 Header Test (Programaticaly Created)--------------------------------------------------------------------------------
    public void testSOAP11AddHeaderBlock() {
        soap11Header.addHeaderBlock("echoOk1", namespace);
        soap11Header.addHeaderBlock("echoOk2", namespace);
        Iterator iterator = soap11Header.getChildren();
        SOAPHeaderBlock headerBlock1 = (SOAPHeaderBlock) iterator.next();
        assertFalse(
                "SOAP 1.1 Header Test : - After calling addHeaderBlock method twice, getChildren method returns empty iterator",
                headerBlock1 == null);
        assertTrue("SOAP 1.1 Header Test : - HeaderBlock1 local name mismatch",
                   headerBlock1.getLocalName().equals("echoOk1"));
        assertTrue(
                "SOAP 1.1 Header Test : - HeaderBlock1 namespace uri mismatch",
                headerBlock1.getNamespace().getNamespaceURI().equals(
                        "http://www.example.org"));

        SOAPHeaderBlock headerBlock2 = (SOAPHeaderBlock) iterator.next();
        assertFalse(
                "SOAP 1.1 Header Test : - After calling addHeaderBlock method twice, getChildren method returns an iterator with only one object",
                headerBlock2 == null);
        assertTrue("SOAP 1.1 Header Test : - HeaderBlock2 local name mismatch",
                   headerBlock2.getLocalName().equals("echoOk2"));
        assertTrue(
                "SOAP 1.1 Header Test : - HeaderBlock2 namespace uri mismatch",
                headerBlock2.getNamespace().getNamespaceURI().equals(
                        "http://www.example.org"));

        assertTrue(
                "SOAP 1.1 Header Test : - After calling addHeaderBlock method twice, getChildren method returns an iterator with more than two object",
                !iterator.hasNext());
    }

    public void testSOAP11ExamineHeaderBlocks() {
        System.out.println("Failing test...");
        soap11Header.addHeaderBlock("echoOk1", namespace).setRole(
                "http://schemas.xmlsoap.org/soap/envelope/actor/ultimateReceiver");
        soap11Header.addHeaderBlock("echoOk2", namespace).setRole(ROLE_URI);
        Iterator iterator = soap11Header.examineHeaderBlocks(ROLE_URI);
        iterator.hasNext();
        SOAPHeaderBlock headerBlockWithRole1 = (SOAPHeaderBlock) iterator.next();
        assertEquals(
                "SOAP 1.1 Header Test : - headerBlockWithRole local name mismatch",
                "echoOk2", headerBlockWithRole1.getLocalName());
        assertEquals(
                "SOAP 1.1 Header Test : - headerBlockWithRole role value mismatch",
                ROLE_URI, headerBlockWithRole1.getRole());


        assertFalse(
                "SOAP 1.1 Header Test : - header has three headerBlocks with the given role, but examineHeaderBlocks(String role) method returns an iterator with more than three objects",
                iterator.hasNext());
    }

//    public void testSOAP11ExtractHeaderBlocks() {

//    }


    public void testSOAP11ExamineAllHeaderBlocks() {
        soap11Header.addHeaderBlock("echoOk1", namespace);
        soap11Header.addHeaderBlock("echoOk2", namespace);
        Iterator iterator = soap11Header.examineAllHeaderBlocks();
        iterator.hasNext();
        SOAPHeaderBlock headerBlock1 = (SOAPHeaderBlock) iterator.next();
        assertFalse(
                "SOAP 1.1 Header Test : - After calling addHeaderBlock method twice, examineAllHeaderBlocks method returns empty iterator",
                headerBlock1 == null);
        assertTrue("SOAP 1.1 Header Test : - HeaderBlock1 local name mismatch",
                   headerBlock1.getLocalName().equals("echoOk1"));
        assertTrue(
                "SOAP 1.1 Header Test : - HeaderBlock1 namespace uri mismatch",
                headerBlock1.getNamespace().getNamespaceURI().equals(
                        "http://www.example.org"));

        iterator.hasNext();
        SOAPHeaderBlock headerBlock2 = (SOAPHeaderBlock) iterator.next();
        assertFalse(
                "SOAP 1.1 Header Test : - After calling addHeaderBlock method twice, examineAllHeaderBlocks method returns an iterator with only one object",
                headerBlock2 == null);
        assertTrue("SOAP 1.1 Header Test : - HeaderBlock2 local name mismatch",
                   headerBlock2.getLocalName().equals("echoOk2"));
        assertTrue(
                "SOAP 1.1 Header Test : - HeaderBlock2 namespace uri mismatch",
                headerBlock2.getNamespace().getNamespaceURI().equals(
                        "http://www.example.org"));

        assertFalse(
                "SOAP 1.1 Header Test : - After calling addHeaderBlock method twice, examineAllHeaderBlocks method returns an iterator with more than two object",
                iterator.hasNext());

    }

//    public void testSOAP11ExtractAllHeaderBlocks() {
//
//    }


    public void testSOAP11getHeaderBlocksWithNSURI() {
        soap11Header.addHeaderBlock("echoOk1", namespace);
        soap11Header.addHeaderBlock("echoOk2",
                                    omFactory.createOMNamespace("http://www.test1.org", "test1"));
        ArrayList arrayList = soap11Header.getHeaderBlocksWithNSURI(
                "http://www.test1.org");
        assertTrue(
                "SOAP 1.1 Header Test : - getHeaderBlocksWithNSURI returns an arrayList of incorrect size",
                arrayList.size() == 1);
        assertTrue(
                "SOAP 1.1 Header Test : - headerBlock of given namespace uri mismatch",
                ((SOAPHeaderBlock) arrayList.get(0)).getNamespace().getNamespaceURI()
                        .equals("http://www.test1.org"));
    }

    //SOAP 1.2 Header Test (Programaticaly Created)----------------------------------------------------------------------------------
    public void testSOAP12AddHeadearBlock() {
        soap12Header.addHeaderBlock("echoOk1", namespace);
        soap12Header.addHeaderBlock("echoOk2", namespace);
        Iterator iterator = soap12Header.getChildren();
        SOAPHeaderBlock headerBlock1 = (SOAPHeaderBlock) iterator.next();
        assertFalse(
                "SOAP 1.2 Header Test : - After calling addHeaderBlock method, getChildren method returns empty iterator",
                headerBlock1 == null);
        assertTrue("SOAP 1.2 Header Test : - HeaderBlock1 local name mismatch",
                   headerBlock1.getLocalName().equals("echoOk1"));
        assertTrue(
                "SOAP 1.2 Header Test : - HeaderBlock1 namespace uri mismatch",
                headerBlock1.getNamespace().getNamespaceURI().equals(
                        "http://www.example.org"));

        SOAPHeaderBlock headerBlock2 = (SOAPHeaderBlock) iterator.next();
        assertFalse(
                "SOAP 1.2 Header Test : - After calling addHeaderBlock method, getChildren method returns an iterator with only one object",
                headerBlock2 == null);
        assertTrue("SOAP 1.2 Header Test : - HeaderBlock2 local name mismatch",
                   headerBlock2.getLocalName().equals("echoOk2"));
        assertTrue(
                "SOAP 1.2 Header Test : - HeaderBlock2 namespace uri mismatch",
                headerBlock2.getNamespace().getNamespaceURI().equals(
                        "http://www.example.org"));

        assertTrue(
                "SOAP 1.2 Header Test : - After calling addHeaderBlock method twice, getChildren method returns an iterator with more than two elements",
                !iterator.hasNext());
    }

    public void testSOAP12ExamineHeaderBlocks() {
        soap12Header.addHeaderBlock("echoOk1", namespace);
        soap12Header.addHeaderBlock("echoOk2", namespace).setRole(
                "http://schemas.xmlsoap.org/soap/envelope/actor/ultimateReceiver");
        Iterator iterator = soap12Header.examineHeaderBlocks(
                "http://schemas.xmlsoap.org/soap/envelope/actor/ultimateReceiver");
        iterator.hasNext();
        SOAPHeaderBlock headerBlockWithRole = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP 1.2 Header Test : - headerBlockWithRole local name mismatch",
                headerBlockWithRole.getLocalName().equals("echoOk2"));
        assertTrue(
                "SOAP 1.2 Header Test : - headerBlockWithRole role value mismatch",
                headerBlockWithRole.getRole().equals(
                        "http://schemas.xmlsoap.org/soap/envelope/actor/ultimateReceiver"));

        assertFalse(
                "SOAP 1.2 Header Test : - header has one headerBlock with role, but examineHeaderBlocks(String role) method returns an iterator with more than one object",
                iterator.hasNext());

    }


    public void testSOAP12ExamineMustUnderstandHeaderBlocks() {
        soap12Header.addHeaderBlock("echoOk1", namespace).setRole(
                "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver");

        SOAPHeaderBlock headerBlock1 = soap12Header.addHeaderBlock("echoOk2",
                                                                   namespace);
        headerBlock1.setRole(
                "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver");
        headerBlock1.setMustUnderstand(true);

        soap12Header.addHeaderBlock("echoOk3", namespace).setMustUnderstand(
                true);

        Iterator iterator = soap12Header.examineMustUnderstandHeaderBlocks(
                "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver");
        iterator.hasNext();
        SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) iterator.next();
        assertFalse(
                "SOAP 1.2 Header Test : - examineMustUnderstandHeaderBlocks method returns empty iterator",
                headerBlock == null);
        assertTrue("SOAP 1.2 Header Test : - HeaderBlock local name mismatch",
                   headerBlock.getLocalName().equals("echoOk2"));
        assertTrue("SOAP 1.2 Header Test : - HeaderBlock role value mismatch",
                   headerBlock.getRole().equals(
                           "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"));
        assertFalse(
                "SOAP 1.2 Header Test : - examineMustUnderstandHeaderBlocks method returns an iterator with more than one object",
                iterator.hasNext());
    }

    public void testSOAP12ExamineAllHeaderBlocks() {
        soap12Header.addHeaderBlock("echoOk1", namespace);
        soap12Header.addHeaderBlock("echoOk2", namespace);
        Iterator iterator = soap12Header.examineAllHeaderBlocks();
        iterator.hasNext();
        SOAPHeaderBlock headerBlock1 = (SOAPHeaderBlock) iterator.next();
        assertFalse(
                "SOAP 1.2 Header Test : - After calling addHeaderBlock method twice, examineAllHeaderBlocks method returns empty iterator",
                headerBlock1 == null);
        assertTrue("SOAP 1.2 Header Test : - HeaderBlock1 local name mismatch",
                   headerBlock1.getLocalName().equals("echoOk1"));
        assertTrue(
                "SOAP 1.2 Header Test : - HeaderBlock1 namespace uri mismatch",
                headerBlock1.getNamespace().getNamespaceURI().equals(
                        "http://www.example.org"));

        SOAPHeaderBlock headerBlock2 = (SOAPHeaderBlock) iterator.next();
        assertFalse(
                "SOAP 1.2 Header Test : - After calling addHeaderBlock method twice, examineAllHeaderBlocks method returns an iterator with only one object",
                headerBlock2 == null);
        assertTrue("SOAP 1.2 Header Test : - HeaderBlock2 local name mismatch",
                   headerBlock2.getLocalName().equals("echoOk2"));
        assertTrue(
                "SOAP 1.2 Header Test : - HeaderBlock2 namespace uri mismatch",
                headerBlock2.getNamespace().getNamespaceURI().equals(
                        "http://www.example.org"));

        assertFalse(
                "SOAP 1.2 Header Test : - After calling addHeaderBlock method twice, examineAllHeaderBlocks method returns an iterator with more than two object",
                iterator.hasNext());
    }
//    public void testSOAP12ExtractAllHeaderBlocks() {
//
//    }

    public void testSOAP12getHeaderBlocksWithNSURI() {
        soap12Header.addHeaderBlock("echoOk1", namespace);
        soap12Header.addHeaderBlock("echoOk2",
                                    omFactory.createOMNamespace("http://www.test1.org", "test1"));
        ArrayList arrayList = soap12Header.getHeaderBlocksWithNSURI(
                "http://www.test1.org");
        assertTrue(
                "SOAP 1.2 Header Test : - getHeaderBlocksWithNSURI returns an arrayList of incorrect size",
                arrayList.size() == 1);
        assertTrue(
                "SOAP 1.2 Header Test : - headerBlock of given namespace uri, mismatch",
                ((SOAPHeaderBlock) arrayList.get(0)).getNamespace().getNamespaceURI()
                        .equals("http://www.test1.org"));
    }

    //SOAP 1.1 Header Test (With Parser)---------------------------------------------------------------------------------------------
    public void testSOAP11ExamineHeaderBlocksWithParser() {
        Iterator iterator = soap11HeaderWithParser.examineHeaderBlocks(
                "http://schemas.xmlsoap.org/soap/actor/next");
        iterator.hasNext();
        SOAPHeaderBlock headerBlock1 = (SOAPHeaderBlock) iterator.next();
        assertEquals(
                "SOAP 1.1 Header Test With Parser : - headerBlock1 localname mismatch",
                headerBlock1.getLocalName(),
                "From");
        assertTrue(
                "SOAP 1.1 Header Test With Parser : - headerBlock1 role value mismatch",
                headerBlock1.getRole().equals(
                        "http://schemas.xmlsoap.org/soap/actor/next"));
        iterator.hasNext();
        SOAPHeaderBlock headerBlock2 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP 1.1 Header Test With Parser : - headerBlock2 localname mmismatch",
                headerBlock2.getLocalName().equals("MessageID"));
        assertTrue(
                "SOAP 1.1 Header Test With Parser : - headerBlock2 role value mmismatch",
                headerBlock2.getRole().equals(
                        "http://schemas.xmlsoap.org/soap/actor/next"));

        assertFalse(
                "SOAP 1.1 Header Test With Parser : - examineHeaderBlocks(String role) method returns an iterator with more than two objects",
                iterator.hasNext());
    }

    public void testSOAP11ExamineMustUnderstandHeaderBlocksWithParser() {
        Iterator iterator = soap11HeaderWithParser.examineMustUnderstandHeaderBlocks(
                "http://schemas.xmlsoap.org/soap/actor/next");
        iterator.hasNext();
        SOAPHeaderBlock headerBlock1 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP 1.1 Header Test With Parser : - headerBlock1 localname mmismatch",
                headerBlock1.getLocalName().equals("MessageID"));
        assertTrue(
                "SOAP 1.1 Header Test With Parser : - headerBlock1 role value mmismatch",
                headerBlock1.getRole().equals(
                        "http://schemas.xmlsoap.org/soap/actor/next"));

        assertFalse(
                "SOAP 1.1 Header Test With Parser : - examineMustUnderstandHeaderBlocks(String role) method returns an iterator with more than one objects",
                iterator.hasNext());
    }

    public void testSOAP11ExamineAllHeaderBlocksWithParser() {
        Iterator iterator = soap11HeaderWithParser.examineAllHeaderBlocks();
        iterator.hasNext();
        SOAPHeaderBlock headerBlock1 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP 1.1 Header Test With Parser : - headerBlock1 localname mmismatch",
                headerBlock1.getLocalName().equals("From"));
        iterator.hasNext();
        SOAPHeaderBlock headerBlock2 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP 1.1 Header Test With Parser : - headerBlock1 localname mmismatch",
                headerBlock2.getLocalName().equals("MessageID"));
        iterator.hasNext();
        SOAPHeaderBlock headerBlock3 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP 1.1 Header Test With Parser : - headerBlock1 localname mmismatch",
                headerBlock3.getLocalName().equals("To"));

        assertFalse(
                "SOAP 1.1 Header Test With Parser : - examineAllHeaderBlocks method returns an iterator with more than three objects",
                iterator.hasNext());
    }

    public void testSOAP11getHeaderBlocksWithNSURIWithParser() {
        ArrayList arrayList = soap11HeaderWithParser.getHeaderBlocksWithNSURI(
                "http://example.org/ts-tests");
        assertTrue(
                "SOAP 1.1 Header Test With Parser : - getHeaderBlocksWithNSURI returns an arrayList of incorrect size",
                arrayList.size() == 1);
        assertTrue(
                "SOAP 1.1 Header Test With Parser : - headerBlock of given namespace uri, local name mismatch",
                ((SOAPHeaderBlock) arrayList.get(0)).getLocalName().equals(
                        "MessageID"));
        assertTrue(
                "SOAP 1.1 Header Test With Parser : - headerBlock of given namespace uri, mismatch",
                ((SOAPHeaderBlock) arrayList.get(0)).getNamespace().getNamespaceURI()
                        .equals("http://example.org/ts-tests"));
    }

    //SOAP 1.2 Header Test (With Parser)-------------------------------------------------------------------------------------------
    public void testSOAP12ExamineHeaderBlocksWithParser() {
        Iterator iterator = soap12HeaderWithParser.examineHeaderBlocks(
                "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver");
        iterator.hasNext();
        SOAPHeaderBlock headerBlock1 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP 1.2 Header Test With Parser : - headerBlock1 localname mmismatch",
                headerBlock1.getLocalName().equals("echoOk"));
        assertTrue(
                "SOAP 1.2 Header Test With Parser : - headerBlock1 role value mmismatch",
                headerBlock1.getRole().equals(
                        "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"));
        iterator.hasNext();
        SOAPHeaderBlock headerBlock2 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP 1.2 Header Test With Parser : - headerBlock2 localname mmismatch",
                headerBlock2.getLocalName().equals("echoOk2"));
        assertTrue(
                "SOAP 1.2 Header Test With Parser : - headerBlock2 role value mmismatch",
                headerBlock2.getRole().equals(
                        "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"));

        assertFalse(
                "SOAP 1.2 Header Test With Parser : - examineHeaderBlocks(String role) method returns an iterator with more than two objects",
                iterator.hasNext());
    }

    public void testSOAP12ExamineMustUnderstandHeaderBlocksWithParser() {
        Iterator iterator = soap12HeaderWithParser.examineMustUnderstandHeaderBlocks(
                "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver");
        iterator.hasNext();
        SOAPHeaderBlock headerBlock1 = (SOAPHeaderBlock) iterator.next();
        assertEquals(
                "SOAP 1.2 Header Test With Parser : - headerBlock localname mmismatch",
                headerBlock1.getLocalName(),
                "echoOk");
        assertEquals(
                "SOAP 1.2 Header Test With Parser : - headerBlock role value mmismatch",
                headerBlock1.getRole(),
                "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver");

        iterator.hasNext();
        SOAPHeaderBlock headerBlock2 = (SOAPHeaderBlock) iterator.next();
        assertEquals(
                "SOAP 1.2 Header Test With Parser : - headerBlock localname mmismatch",
                headerBlock2.getLocalName(),
                "echoOk2");
        assertEquals(
                "SOAP 1.2 Header Test With Parser : - headerBlock role value mmismatch",
                headerBlock1.getRole(),
                "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver");

        assertFalse(
                "SOAP 1.2 Header Test With Parser : - examineMustUnderstandHeaderBlocks(String role) method returns an iterator with more than one objects",
                iterator.hasNext());
    }

    public void testSOAP12ExamineAllHeaderBlocksWithParser() {
        Iterator iterator = soap12HeaderWithParser.examineAllHeaderBlocks();
        assertTrue(iterator.hasNext());
        SOAPHeaderBlock headerBlock1 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP 1.2 Header Test With Parser : - headerBlock1 localname mmismatch",
                headerBlock1.getLocalName().equals("echoOk"));
        assertTrue(iterator.hasNext());
        SOAPHeaderBlock headerBlock2 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP 1.2 Header Test With Parser : - headerBlock1 localname mmismatch",
                headerBlock2.getLocalName().equals("echoOk1"));
        assertTrue(iterator.hasNext());
        SOAPHeaderBlock headerBlock3 = (SOAPHeaderBlock) iterator.next();
        assertTrue(
                "SOAP 1.2 Header Test With Parser : - headerBlock1 localname mmismatch",
                headerBlock3.getLocalName().equals("echoOk2"));

        assertFalse(
                "SOAP 1.2 Header Test With Parser : - examineAllHeaderBlocks method returns an iterator with more than three objects",
                iterator.hasNext());
    }

    public void testSOAP12getHeaderBlocksWithNSURIWithParser() {
        ArrayList arrayList = soap12HeaderWithParser.getHeaderBlocksWithNSURI(
                "http://example.org/ts-tests");
        assertTrue(
                "SOAP 1.2 Header Test With Parser : - getHeaderBlocksWithNSURI returns an arrayList of incorrect size",
                arrayList.size() == 1);
        assertTrue(
                "SOAP 1.2 Header Test With Parser : - headerBlock of given namespace uri, local name mismatch",
                ((SOAPHeaderBlock) arrayList.get(0)).getLocalName().equals(
                        "echoOk"));
        assertTrue(
                "SOAP 1.2 Header Test With Parser : - headerBlock of given namespace uri, mismatch",
                ((SOAPHeaderBlock) arrayList.get(0)).getNamespace().getNamespaceURI()
                        .equals("http://example.org/ts-tests"));
    }
}
