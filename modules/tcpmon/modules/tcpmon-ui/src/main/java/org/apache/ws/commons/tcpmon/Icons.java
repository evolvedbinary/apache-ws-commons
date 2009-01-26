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

package org.apache.ws.commons.tcpmon;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Icons {
    public static final Icon START = createIcon("connect");
    public static final Icon RESEND = createIcon("arrow_redo");
    public static final Icon SAVE = createIcon("disk");
    public static final Icon LAYOUT_HORIZONTAL = createIcon("application_tile_horizontal");
    public static final Icon LAYOUT_VERTICAL = createIcon("application_split");
    public static final Icon XML_FORMAT = createIcon("tag");
    public static final Icon CLOSE = createIcon("cross");
    
    private static ImageIcon createIcon(String name) {
        return new ImageIcon(Icons.class.getResource("/com/famfamfam/silk/" + name + ".png"), "TEST");
    }
    
    private Icons() {}
}
