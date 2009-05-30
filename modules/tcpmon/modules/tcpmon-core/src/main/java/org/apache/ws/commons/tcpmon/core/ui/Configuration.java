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

import org.apache.ws.commons.tcpmon.core.engine.InterceptorConfiguration;

public class Configuration {
    private InterceptorConfiguration interceptorConfiguration;
    private boolean xmlFormat;
    
    public InterceptorConfiguration getInterceptorConfiguration() {
        return interceptorConfiguration;
    }

    public void setInterceptorConfiguration(InterceptorConfiguration interceptorConfiguration) {
        this.interceptorConfiguration = interceptorConfiguration;
    }

    public boolean isXmlFormat() {
        return xmlFormat;
    }

    public void setXmlFormat(boolean xmlFormat) {
        this.xmlFormat = xmlFormat;
    }
}
