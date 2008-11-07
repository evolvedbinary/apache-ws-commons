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

import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;

/**
 * Message builder able to build messages from a Map.
 * This interface can be optionally implemented by {@link Builder}
 * implementations that support building a message from a {@link Map}
 * <p>
 * This interface is currently used by the JMS transport to process
 * {@link javax.jms.MapMessage} instances.
 */
public interface MapMessageBuilder extends Builder {
    public OMElement processDocument(Map map, String contentType,
            MessageContext messageContext) throws AxisFault;
}
