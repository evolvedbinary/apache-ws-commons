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

package org.apache.ws.commons.tcpmon.core;

import java.io.OutputStream;

/**
 * Listener receiving information about a given request-response exchange.
 */
public interface IRequestResponse {
    int STATE_ACTIVE = 0;
    int STATE_REQ = 1;
    int STATE_RESP = 2;
    int STATE_DONE = 3;
    int STATE_ERROR = 4;
    
    void setTarget(String targetHost, int targetPort);
    void setState(int state);
    void setElapsed(long elapsed);
    
    /**
     * Get the output stream to which a copy of the request will be written.
     * 
     * @return an output stream or <code>null</code> if the implementation doesn't
     *         want to receive a copy of the request
     */
    OutputStream getRequestOutputStream();
    
    /**
     * Get the output stream to which a copy of the response will be written
     * 
     * @return an output stream or <code>null</code> if the implementation doesn't
     *         want to receive a copy of the response
     */
    OutputStream getResponseOutputStream();
    
    void onError(Throwable ex);
}
