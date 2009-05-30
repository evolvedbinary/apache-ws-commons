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

package org.apache.ws.commons.tcpmon.core.engine;

import java.io.OutputStream;

public class Dump implements InterceptorListener, RequestResponseListener {
    private final OutputStream out;
    
    public Dump(OutputStream out) {
        this.out = out;
    }

    public RequestResponseListener createRequestResponseListener(String fromHost) {
        return this;
    }

    public void onServerSocketError(Throwable ex) {
    }

    public void onServerSocketStart() {
    }

    public OutputStream getRequestOutputStream() {
        return out;
    }

    public OutputStream getResponseOutputStream() {
        return out;
    }

    public void onError(Throwable ex) {
    }

    public void setElapsed(long elapsed) {
    }

    public void setState(int state) {
    }

    public void setTarget(String targetHost, int targetPort) {
    }
}
