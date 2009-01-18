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
    private boolean firstIndent = true;
    private int nextIndent = -1;
    private int previousIndent = -1;
    
    public XmlFormatFilter(int tabWidth) {
        this.tabWidth = tabWidth;
    }

    public void invoke(Stream stream) {
        try {
            while (stream.available() > 0) {
                boolean doIndent = false;
                if (stream.get(0) == '<' && stream.get(1) != '/') {
                    previousIndent = nextIndent++;
                    doIndent = true;
                } else if (stream.get(0) == '<' && stream.get(1) == '/') {
                    doIndent = previousIndent > nextIndent;
                    previousIndent = nextIndent--;
                } else if ((stream.get(0) == '/' || stream.get(0) == '?')
                        && stream.get(1) == '>') {
                    previousIndent = nextIndent--;
                }
                if (doIndent) {
                    if (firstIndent) {
                        firstIndent = false;
                    } else {
                        stream.insert((byte) '\n');
                    }
                    for (int i = tabWidth * nextIndent; i > 0; i--) {
                        stream.insert((byte) ' ');
                    }
                }
                if (stream.get(0) != '\n' && stream.get(0) != '\r') {
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
