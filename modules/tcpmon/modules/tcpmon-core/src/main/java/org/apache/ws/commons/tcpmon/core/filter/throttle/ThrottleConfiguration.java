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

package org.apache.ws.commons.tcpmon.core.filter.throttle;

public class ThrottleConfiguration {
    private int delayBytes;
    private int delayTime;
    
    /**
     * construct
     *
     * @param delayBytes bytes per delay; set to 0 for no delay
     * @param delayTime  delay time per delay in milliseconds
     */
    public ThrottleConfiguration(int delayBytes, int delayTime) {
        this.delayBytes = delayBytes;
        this.delayTime = delayTime;
    }

    public int getDelayBytes() {
        return delayBytes;
    }

    public int getDelayTime() {
        return delayTime;
    }
}
