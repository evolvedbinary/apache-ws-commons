/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.tests.echo;

import java.util.Map;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.ds.MapDataSource;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.testkit.MessageTestData;
import org.apache.axis2.transport.testkit.channel.RequestResponseChannel;
import org.apache.axis2.transport.testkit.client.RequestResponseTestClient;
import org.apache.axis2.transport.testkit.endpoint.InOutEndpoint;
import org.apache.axis2.transport.testkit.message.XMLMessage;
import org.apache.axis2.transport.testkit.name.Name;

@Name("EchoMapXML")
public class XMLMapRequestResponseMessageTestCase extends XMLRequestResponseMessageTestCase {
    private final XMLMessage.Type xmlMessageType;
    private final Map data;
    
    public XMLMapRequestResponseMessageTestCase(RequestResponseChannel channel, RequestResponseTestClient<XMLMessage,XMLMessage> client, InOutEndpoint endpoint, XMLMessage.Type xmlMessageType, Map data, Object... resources) {
        super(channel, client, endpoint, xmlMessageType, new MessageTestData("UTF8", "", "UTF-8"), resources);
        this.xmlMessageType = xmlMessageType;
        this.data = data;
    }

    @Override
    protected XMLMessage prepareRequest() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        QName wrapperQName = BaseConstants.DEFAULT_MAP_WRAPPER;
        OMElement wrapper = factory.createOMElement(new MapDataSource(data, wrapperQName.getLocalPart(),
            factory.createOMNamespace(wrapperQName.getNamespaceURI(), wrapperQName.getPrefix())), wrapperQName.getLocalPart(),
            factory.createOMNamespace(wrapperQName.getNamespaceURI(), wrapperQName.getPrefix()));
        return new XMLMessage(wrapper, xmlMessageType);
    }

    @Override
    protected void checkResponse(XMLMessage request, XMLMessage response) throws Exception {
        OMElement orgElement = request.getPayload();
        OMElement element = response.getPayload();
        assertTrue(orgElement != null);
        assertTrue(element != null);
        assertTrue(orgElement instanceof OMSourcedElement);
        assertTrue(element instanceof OMSourcedElement);
        assertEquals(orgElement.getQName(), element.getQName());
        assertEquals(orgElement.toString(), element.toString());
    }
}
