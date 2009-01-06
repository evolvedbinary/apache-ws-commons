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

/**
 * Filter that reformats XML data so that it is properly indented.
 */
public class XmlFormatFilter implements StreamFilter {
    private final int tabWidth;
    private int nextIndent = -1;
    private int previousIndent = -1;
    
    public XmlFormatFilter(int tabWidth) {
        this.tabWidth = tabWidth;
    }

    public void invoke(Stream stream) {
        try {
            boolean inXML = false;
            while (stream.available() > 0) {
                int thisIndent = -1;
                if (stream.get(0) == '<' && stream.get(1) != '/') {
                    previousIndent = nextIndent++;
                    thisIndent = nextIndent;
                    inXML = true;
                } else if (stream.get(0) == '<' && stream.get(1) == '/') {
                    if (previousIndent > nextIndent) {
                        thisIndent = nextIndent;
                    }
                    previousIndent = nextIndent--;
                    inXML = true;
                } else if (stream.get(0) == '/' && stream.get(1) == '>') {
                    previousIndent = nextIndent--;
                    inXML = true;
                }
                if (thisIndent != -1) {
                    if (thisIndent > 0) {
                        stream.insert((byte) '\n');
                    }
                    for (int i = tabWidth * thisIndent; i > 0; i--) {
                        stream.insert((byte) ' ');
                    }
                }
                if (!inXML || (stream.get(0) != '\n' && stream.get(0) != '\r')) {
                    stream.skip();
                } else {
                    stream.discard();
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            return;
        }
    }
}
