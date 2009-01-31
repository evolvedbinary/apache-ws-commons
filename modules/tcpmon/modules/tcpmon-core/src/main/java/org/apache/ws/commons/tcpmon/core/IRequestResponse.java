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

import java.io.Writer;

/**
 * Listener receiving information about a given request-response exchange.
 */
public interface IRequestResponse {
    void setOutHost(String outHost);
    void setState(String state);
    void setRequest(String request);
    void setElapsed(String elapsed);
    Writer getRequestWriter();
    Writer getResponseWriter();
    String getRequestAsString();
    String getResponseAsString();
}
