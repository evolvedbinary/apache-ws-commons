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

package org.apache.ws.commons.tcpmon.core.filter.mime;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import junit.framework.TestCase;

import org.apache.ws.commons.tcpmon.core.filter.ReplaceFilter;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;
import org.apache.ws.commons.tcpmon.core.filter.TestUtil;

public class MultipartFilterTest extends TestCase {
    public void test() throws Exception {
        MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));
        MimeMultipart mp = new MimeMultipart();
        MimeBodyPart bp1 = new MimeBodyPart();
        bp1.setContent("test", "text/plain");
        mp.addBodyPart(bp1);
        MimeBodyPart bp2 = new MimeBodyPart();
        bp2.setContent("test", "text/plain");
        mp.addBodyPart(bp2);
        msg.setContent(mp);
        msg.saveChanges();
        String contentType = msg.getContentType();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mp.writeTo(baos);
        
        ContentFilterFactory cff = new ContentFilterFactory() {
            public StreamFilter[] getContentFilterChain(String contentType) {
                try {
                    return new StreamFilter[] { new ReplaceFilter("test", "TEST", "ascii") };
                } catch (UnsupportedEncodingException ex) {
                    throw new Error(ex);
                }
            }
        };
        
        byte[] filtered = TestUtil.filter(new MultipartFilter(cff, contentType), baos.toByteArray());
        MimeMultipart mp2 = new MimeMultipart(new ByteArrayDataSource(filtered, contentType));
        assertEquals(2, mp2.getCount());
        assertEquals("TEST", mp2.getBodyPart(0).getContent());
        assertEquals("TEST", mp2.getBodyPart(1).getContent());
    }
}
