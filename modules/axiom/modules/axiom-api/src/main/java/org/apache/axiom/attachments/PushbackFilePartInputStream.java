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

package org.apache.axiom.attachments;

import java.io.IOException;
import java.io.InputStream;

public class PushbackFilePartInputStream extends InputStream {

    MIMEBodyPartInputStream inStream;

    byte[] buffer;

    int count;

    /**
     * @param inStream
     * @param buffer
     */
    public PushbackFilePartInputStream(MIMEBodyPartInputStream inStream,
                                       byte[] buffer) {
        super();
        this.inStream = inStream;
        this.buffer = buffer;
        count = buffer.length;
    }

    public int read() throws IOException {
        int data;
        if (count > 0) {
            byte byteValue = buffer[buffer.length - count];
            // converting the byte to unsigned int value
            data = byteValue & 0xff;
            count--;
        } else {
            data = inStream.read();
        }
        return data;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (count > 0) {
            // Get the start of the internal buffer and the length to copy
            int start = buffer.length - count;
            int copyLen = Math.min(len, count); 
            
            // Copy the bytes to b
            System.arraycopy(buffer, start, b, off, copyLen);
            count = count - copyLen;
            
            // If more bytes are needed, read them from the inStream
            if (len > copyLen) {
                return inStream.read(b, off + copyLen, len - copyLen) + copyLen; 
            } else {
                return len;
            }
        } else {
            return inStream.read(b, off, len);
        }
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    
    public int available() throws IOException {
        return count + inStream.available();
    }
}