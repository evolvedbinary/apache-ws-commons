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

import java.io.UnsupportedEncodingException;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.ws.commons.tcpmon.core.filter.Stream;
import org.apache.ws.commons.tcpmon.core.filter.StreamException;
import org.apache.ws.commons.tcpmon.core.filter.StreamFilter;
import org.apache.ws.commons.tcpmon.core.filter.StreamUtil;

/**
 * Filter that processes multipart/* messages.
 */
public class MultipartFilter implements StreamFilter {
    private static final int STATE_START = 0;
    private static final int STATE_PREAMBLE = 1;
    private static final int STATE_IN_PART = 2;
    private static final int STATE_COMPLETE = 3;
    
    private final ContentFilterFactory contentFilterFactory;
    private final byte[] startBoundaryDelimiter;
    private final byte[] boundaryDelimiter;
    private final byte[] endBoundaryDelimiter;
    private int state = STATE_START;

    public MultipartFilter(ContentFilterFactory contentFilterFactory, String contentType) {
        this.contentFilterFactory = contentFilterFactory;
        MimeType mimeType;
        try {
            mimeType = new MimeType(contentType);
        } catch (MimeTypeParseException ex) {
            throw new StreamException(ex);
        }
        String boundary = mimeType.getParameter("boundary");
        try {
            startBoundaryDelimiter = ("--" + boundary + "\r\n").getBytes("ascii");
            boundaryDelimiter = ("\r\n--" + boundary + "\r\n").getBytes("ascii");
            endBoundaryDelimiter = ("\r\n--" + boundary + "--").getBytes("ascii");
        } catch (UnsupportedEncodingException ex) {
            throw new StreamException(ex);
        }
    }

    public void invoke(Stream stream) {
        if (state == STATE_START) {
            if (stream.available() < startBoundaryDelimiter.length) {
                return;
            }
            boolean isStartBoundaryDelimiter = true;
            for (int i=0; i<startBoundaryDelimiter.length; i++) {
                if (stream.get(i) != startBoundaryDelimiter[i]) {
                    isStartBoundaryDelimiter = false;
                    break;
                }
            }
            if (isStartBoundaryDelimiter) {
                stream.skip(startBoundaryDelimiter.length);
                stream.pushFilter(new MimePartFilter(contentFilterFactory));
                state = STATE_IN_PART;
            } else {
                state = STATE_PREAMBLE;
            }
        } else if (state == STATE_COMPLETE) {
            stream.skipAll();
            return;
        }
        byte[][] patterns = { boundaryDelimiter, endBoundaryDelimiter };
        while (true) {
            int match = StreamUtil.search(stream, patterns);
            if (match == -1) {
                break;
            }
            if (state == STATE_IN_PART) {
                stream.popFilter();
            }
            stream.skip(patterns[match].length);
            if (match == 0) {
                stream.pushFilter(new MimePartFilter(contentFilterFactory));
                state = STATE_IN_PART;
            } else {
                state = STATE_COMPLETE;
            }
        }
    }
}
