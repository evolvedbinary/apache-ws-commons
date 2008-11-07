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

package org.apache.axis2.format;

import java.io.InputStream;
import java.util.Map;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.ds.MapDataSource;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.base.BaseConstants;

/**
 * Adapter to add the {@link MapMessageBuilder} interface to an
 * existing {@link Builder}.
 * It implements the {@link MapMessageBuilder#processDocument(Map, String, MessageContext)}.
 */
public class MapMessageBuilderAdapter implements MapMessageBuilder {
    private final Builder builder;

    public MapMessageBuilderAdapter(Builder builder) {
        this.builder = builder;
    }

    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext) throws AxisFault {
        return builder.processDocument(inputStream, contentType, messageContext);
    }

    public OMElement processDocument(Map map, String contentType,
                                     MessageContext messageContext) throws AxisFault {
        String charset;
        try {
            ContentType ct = new ContentType(contentType);
            charset = ct.getParameter("charset");
        } catch (ParseException ex) {
            charset = null;
        }
        if (charset == null) {
            charset = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }
        messageContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charset);
        OMFactory omBuilderFactory = OMAbstractFactory.getOMFactory();
        QName wrapperQName = BaseConstants.DEFAULT_MAP_WRAPPER;
        return omBuilderFactory.createOMElement(new MapDataSource(map, wrapperQName.getLocalPart(),
            omBuilderFactory.createOMNamespace(wrapperQName.getNamespaceURI(), wrapperQName.getPrefix())), wrapperQName.getLocalPart(),
            omBuilderFactory.createOMNamespace(wrapperQName.getNamespaceURI(), wrapperQName.getPrefix()));
    }
}
