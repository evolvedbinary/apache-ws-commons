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

package org.apache.ws.commons.tcpmon.eclipse.ui;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.swt.widgets.Text;

/**
 * A simple writer that appends the character data to a {@link Text} widget.
 */
public class TextWidgetWriter extends Writer {
    private final Text textArea;

    public TextWidgetWriter(Text textArea) {
        this.textArea = textArea;
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        final String s = new String(cbuf, off, len);
        MainView.display.syncExec(new Runnable() {
            public void run() {
                textArea.append(s);
            }
        });
    }

    public void close() throws IOException {
    }

    public void flush() throws IOException {
    }
}
