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
 * <p>
 * Note that this filter only works if the stream is encoded in an
 * ASCII compatible charset encoding (ASCII, UTF-8, ISO-8859-x, etc.).
 */
public class XmlFormatFilter implements StreamFilter {
    private static final int STATE_WHITESPACE = 0;
    private static final int STATE_START_TAG = 1;
    private static final int STATE_END_TAG = 2;
    private static final int STATE_PI = 3;
    private static final int STATE_TEXT = 4;
    
    private final int tabWidth;
    private int state = STATE_TEXT;
    private boolean firstIndent = true;
    private boolean endTagRequiresIndent = false;
    private int level = -1; // The current element level (root = 0)
    
    public XmlFormatFilter(int tabWidth) {
        this.tabWidth = tabWidth;
    }

    private static boolean isSpace(int b) {
        return b == ' ' || b == '\r' || b == '\n' || b == '\t';
    }

    private static boolean isNameStartChar(int b) {
        // This covers the ASCII subset of the NameStartChar production, minus the colon
        return 'a' <= b && b <= 'z' || 'A' <= b && b <= 'Z' || b == '_';
    }

    private void indent(Stream stream, int amount) {
        if (firstIndent) {
            firstIndent = false;
        } else {
            stream.insert((byte)'\n');
        }
        for (int i = 0; i < tabWidth * amount; i++) {
            stream.insert((byte)' ');
        }
    }

    public void invoke(Stream stream) {
        try {
            while (stream.available() > 0) {
                switch (state) {
                    case STATE_WHITESPACE: {
                        int i = 0;
                        int c;
                        while (isSpace(c = stream.get(i))) {
                            i++;
                        }
                        if (c == '<') {
                            stream.discard(i);
                        } else {
                            stream.skip(i);
                        }
                        state = STATE_TEXT;
                        break;
                    }
                    case STATE_TEXT: {
                        if (stream.get(0) == '<') {
                            int c = stream.get(1);
                            if (c == '/') {
                                if (endTagRequiresIndent) {
                                    indent(stream, level);
                                }
                                stream.skip(2);
                                state = STATE_END_TAG;
                            } else if (c == '?') {
                                indent(stream, level+1);
                                stream.skip(2);
                                state = STATE_PI;
                            } else if (isNameStartChar(c)) {
                                indent(stream, ++level);
                                stream.skip(2);
                                state = STATE_START_TAG;
                            } else {
                                stream.skip(1);
                            }
                        } else {
                            stream.skip(1);
                        }
                        break;
                    }
                    case STATE_START_TAG: {
                        if (stream.get(0) == '/' && stream.get(1) == '>') {
                            stream.skip(2);
                            level--;
                            state = STATE_WHITESPACE;
                            endTagRequiresIndent = true;
                        } else if (stream.get(0) == '>') {
                            stream.skip(1);
                            state = STATE_WHITESPACE;
                            endTagRequiresIndent = false;
                        } else {
                            stream.skip(1);
                        }
                        break;
                    }
                    case STATE_END_TAG: {
                        if (stream.get(0) == '>') {
                            level--;
                            state = STATE_WHITESPACE;
                            endTagRequiresIndent = true;
                        }
                        stream.skip(1);
                        break;
                    }
                    case STATE_PI: {
                        if (stream.get(0) == '?' && stream.get(1) == '>') {
                            stream.skip(2);
                            state = STATE_WHITESPACE;
                            endTagRequiresIndent = true;
                        } else {
                            stream.skip(1);
                        }
                        break;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            return;
        }
    }
}
