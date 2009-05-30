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

package org.apache.ws.commons.tcpmon.core.ui;

import java.io.OutputStream;

import org.apache.ws.commons.tcpmon.core.engine.InterceptorListener;
import org.apache.ws.commons.tcpmon.core.ui.AbstractRequestResponse;

public abstract class AbstractListener implements InterceptorListener {
    protected void resend(AbstractRequestResponse requestResponse) {
        try {
            String text = requestResponse.getRequestAsString();

            // Fix Content-Length HTTP headers
            if (text.startsWith("POST ") || text.startsWith("GET ")) {

                int pos1, pos2, pos3;
                String headers;
                pos3 = text.indexOf("\n\n");
                if (pos3 == -1) {
                    pos3 = text.indexOf("\r\n\r\n");
                    if (pos3 != -1) {
                        pos3 = pos3 + 4;
                    }
                } else {
                    pos3 += 2;
                }
                headers = text.substring(0, pos3);
                pos1 = headers.indexOf("Content-Length:");

                if (pos1 != -1) {
                    int newLen = text.length() - pos3;
                    pos2 = headers.indexOf("\n", pos1);
                    System.err.println("CL: " + newLen);
                    System.err.println("Hdrs: '" + headers + "'");
                    System.err.println("subTEXT: '"
                            + text.substring(pos3, pos3 + newLen)
                            + "'");
                    text = headers.substring(0, pos1) + "Content-Length: "
                            + newLen + "\n" + headers.substring(pos2 + 1)
                            + text.substring(pos3);
                    System.err.println("\nTEXT: '" + text + "'");
                }
            }
            RawSender sender = new RawSender(this, requestResponse.getTargetHost(),
                    requestResponse.getTargetPort());
            new Thread(sender).start();
            OutputStream out = sender.getOutputStream();
            out.write(text.getBytes());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public abstract Configuration getConfiguration();
}
