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
package org.apache.axis2.transport.jms;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.ds.MapDataSource;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.jms.MapMessage;
import javax.jms.JMSException;
import javax.jms.MessageEOFException;
import javax.xml.namespace.QName;

/**
 * Input stream that reads data from a JMS {@link MapMessage}.
 */
public class MapMessageInputStream extends InputStream {

    private static final Log log = LogFactory.getLog(MapMessageInputStream.class);
    private MapMessage message;
    private String encoding;
    private ByteArrayInputStream byteStream = null;

    public MapMessageInputStream(MapMessage message, String encoding) {
        this.message = message;
        this.encoding = encoding;
    }

    private ByteArrayInputStream getByteStream() {
        if (byteStream != null) {
            return byteStream;
        } else {
            JMSUtils utils = new JMSUtils();
            Map payloadMap = utils.getMessageMapPayload(message);
            if (payloadMap != null) {
                QName wrapperQName = BaseConstants.DEFAULT_MAP_WRAPPER;
                OMFactory ombuilderFactory = OMAbstractFactory.getOMFactory();
                // It was assumed that the creation of a MapDataSource in here will
                // always be based on the default wrapper QName.
                MapDataSource mds = new MapDataSource(payloadMap, wrapperQName.getLocalPart(),
                    ombuilderFactory.createOMNamespace(wrapperQName.getNamespaceURI(), wrapperQName.getPrefix()));
                try {
                    byteStream = new ByteArrayInputStream(mds.getXMLBytes(encoding));
                } catch (UnsupportedEncodingException e) {
                    log.error("Unsupported Encoding");
                    byteStream = null;
                }
            }
            return byteStream;
        }
    }

    @Override
    public int read() {
        ByteArrayInputStream readStream = getByteStream();
        if (readStream == null) {
            return -1;
        }
        return readStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) {
        ByteArrayInputStream readStream = getByteStream();
        if (readStream == null) {
            return -1;
        }
        return readStream.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        ByteArrayInputStream readStream = getByteStream();
        if (readStream == null) {
            return -1;
        }
        return readStream.read(b);
    }
}
