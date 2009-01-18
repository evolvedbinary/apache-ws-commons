package org.apache.ws.commons.tcpmon.idea.plugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindowAnchor;

import org.apache.ws.commons.tcpmon.idea.ui.ComponentHandler;
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

public class IdeaPlugin implements ProjectComponent {

    private static final String TCPMON_NAME = "TCPMon";
    private Project project;


    public IdeaPlugin(Project project) {
        this.project = project;
    }

    public void projectOpened() {
        initToolWindow();
    }

    public void projectClosed() {
       unregisterToolWindow();
    }

    public String getComponentName() {
        return TCPMON_NAME;
    }

    public void initComponent() {
        //nothing to do here
    }

    public void disposeComponent() {
        //nothing to do here
    }

    private void initToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindowManager.registerToolWindow(
                TCPMON_NAME,
                ComponentHandler.getTCPMonTabbedPane(),
                ToolWindowAnchor.RIGHT);
//        URL resource = getClass().getClassLoader().getResource("images/tcpmonitor.gif");
//        toolWindow.setIcon(new ImageIcon(resource));
    }

    private void unregisterToolWindow() {
        //do nothing
    }
}
