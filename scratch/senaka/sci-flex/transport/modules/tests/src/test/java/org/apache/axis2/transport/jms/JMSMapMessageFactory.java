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

package org.apache.axis2.transport.jms;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.axis2.transport.testkit.name.Name;

@Name("map")
public class JMSMapMessageFactory implements JMSMessageFactory<Map> {
    public static final JMSMapMessageFactory INSTANCE = new JMSMapMessageFactory();
    
    private JMSMapMessageFactory() {}

    public Message createMessage(Session session, Map data) throws JMSException {
        MapMessage message = session.createMapMessage();
        Iterator it = data.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            Object value = data.get(key);
            if (key != null && value != null && key instanceof String) {
                message.setObject((String)key, value);
            } else {
                System.out.println("Unable to add entry to Map");
            }
        }

        return message;
    }

    public Map parseMessage(Message message) throws JMSException {
        MapMessage mapMessage = (MapMessage)message;
        Map data = new TreeMap();
        for (Enumeration e = mapMessage.getMapNames() ; e.hasMoreElements() ;) {
            String key = (String) e.nextElement();
            Object value = mapMessage.getObject(key);
            if (value != null) {
                data.put(key, value);
            } else {
                System.out.println("Ignoring key " + key + " that did not return any value");
            }
        }
        return data;
    }
}
