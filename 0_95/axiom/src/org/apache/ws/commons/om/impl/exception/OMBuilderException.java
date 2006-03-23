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

package org.apache.ws.commons.om.impl.exception;

import org.apache.ws.commons.om.OMException;

/**
 * Class OMBuilderException
 */
public class OMBuilderException extends OMException {
	
    private static final long serialVersionUID = -7447667411291193889L;

	/**
     * Constructor OMBuilderException
     *
     * @param s
     */
    public OMBuilderException(String s) {
        super(s);
    }

    public OMBuilderException(Throwable cause) {
        super(cause);
    }
}
