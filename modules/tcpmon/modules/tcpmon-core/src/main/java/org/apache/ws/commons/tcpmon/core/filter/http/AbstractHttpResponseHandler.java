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

package org.apache.ws.commons.tcpmon.core.filter.http;

/**
 * Abstract implementation of {@link HttpResponseHandler} with default behavior.
 */
public class AbstractHttpResponseHandler implements HttpResponseHandler {
    public String processResponseLine(String responseLine) {
        return responseLine;
    }

    public String handleHeader(String name, String value) {
        return value;
    }

    public void responseCompleted() {
    }
}
