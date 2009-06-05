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

package org.apache.ws.commons.tcpmon.core.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class TestUtil {
    private TestUtil() {}
    
    public static byte[] filter(StreamFilter filter, byte[] in, ErrorListener errorListener) {
        Pipeline pipeline = new Pipeline();
        if (errorListener != null) {
            pipeline.setErrorListener(errorListener);
        }
        pipeline.addFilter(filter);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.addFilter(new Tee(baos));
        ByteArrayInputStream bais = new ByteArrayInputStream(in);
        try {
            while (pipeline.readFrom(bais) != -1) {
                // Just loop
            }
        } catch (IOException ex) {
            throw new Error("Unexpected IOException when reading from ByteArrayInputStream", ex);
        }
        return baos.toByteArray();
    }
    
    public static byte[] filter(StreamFilter filter, byte[] in) {
        return filter(filter, in, null);
    }
    
    public static String filter(StreamFilter filter, String in, String charsetName) throws UnsupportedEncodingException {
        return new String(filter(filter, in.getBytes(charsetName)), charsetName);
    }
    
    public static String filter(StreamFilter filter, String in) {
        try {
            return filter(filter, in, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new Error(ex);
        }
    }
}
