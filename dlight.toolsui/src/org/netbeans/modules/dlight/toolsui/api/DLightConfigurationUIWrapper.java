/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.toolsui.api;

import org.netbeans.modules.dlight.api.tool.*;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.dlight.api.tool.impl.DLightConfigurationManagerAccessor;

/**
 *
 * @author thp
 */
public class DLightConfigurationUIWrapper {
    private DLightConfiguration dlightConfiguration;
    private boolean custom;
    private String name;
    private String displayName;
    private List<DLightToolUIWrapper> tools;
    private boolean profileOnRun;
    private boolean modified;
    private DLightConfiguration copyOf;

    public DLightConfigurationUIWrapper(DLightConfiguration dlightConfiguration, List<DLightTool> allDLightTools) {
        this.dlightConfiguration = dlightConfiguration;
        this.name = dlightConfiguration.getConfigurationName();
        this.displayName = dlightConfiguration.getDisplayedName();
        this.profileOnRun = true;
        this.custom = false;
        copyOf = null;
        initWrapper(allDLightTools);
    }

    public DLightConfigurationUIWrapper(String name, String displayName, List<DLightTool> allDLightTools) {
        DLightConfigurationManagerAccessor accessor = DLightConfigurationManagerAccessor.getDefault();
        DLightConfigurationManager manager = DLightConfigurationManager.getInstance();
        this.dlightConfiguration = accessor.getDefaultConfiguration(manager);
        this.name = name;
        this.displayName = displayName;
        this.custom = true;
        initWrapper(allDLightTools);
    }

    private void initWrapper(List<DLightTool> allDLightTools) {
        tools = new ArrayList<DLightToolUIWrapper>();
        List<DLightTool> confDlightTools = dlightConfiguration.getToolsSet();
        int i = 0;
        for (DLightTool dlightTool : allDLightTools) {
            DLightTool toolToAdd = findTool(confDlightTools, dlightTool.getID());
            toolToAdd = toolToAdd == null ? dlightTool : toolToAdd;
            if (toolToAdd != null && toolToAdd.isVisible()){
                tools.add(new DLightToolUIWrapper(toolToAdd,  inList(dlightTool, confDlightTools)));
            }
        }
    }

    private static  DLightTool findTool(List<DLightTool> tools, String id){
        for (DLightTool tool : tools){
            if (tool.getID().equals(id)){
                return tool;
            }
        }
        return null;
    }
    
    private static boolean inList(DLightTool dlightTool, List<DLightTool> list) {
        for (DLightTool dt : list) {
            if (dt.getID().equals(dlightTool.getID())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the custom
     */
    public boolean isCustom() {
        return custom;
    }


    /**
     * @return the dLightConfiguration
     */
    public DLightConfiguration getDLightConfiguration() {
        return dlightConfiguration;
    }

    /**
     * @param dLightConfiguration the dLightConfiguration to set
     */
    public void setDLightConfiguration(DLightConfiguration dLightConfiguration) {
        this.dlightConfiguration = dLightConfiguration;
    }

    /**
     * @return the tools
     */
    public List<DLightToolUIWrapper> getTools() {
        return tools;
    }

    public DLightToolUIWrapper getToolUIWrapper(DLightTool dlightTool) {
        for (DLightToolUIWrapper dt : getTools()) {
            if (dt.getDLightTool().getID().equals(dlightTool.getID())) {
                return dt;
            }
        }
        return null;
    }

    /**
     * @param tools the tools to set
     */
    public void setTools(List<DLightToolUIWrapper> tools) {
        this.tools = tools;
    }

    public void setToolEnabled(DLightToolUIWrapper toolWrapper, boolean isEnabled){
        toolWrapper.setEnabled(isEnabled);
    }

    /**
     * @return the profileOnRun
     */
    public boolean isProfileOnRun() {
        return profileOnRun;
    }

    /**
     * @param profileOnRun the profileOnRun to set
     */
    public void setProfileOnRun(boolean profileOnRun) {
        if (this.profileOnRun != profileOnRun){
            modified = true;
        }
        this.profileOnRun = profileOnRun;
    }

    /**
     * @return the modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * @param modified the modified to set
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public DLightConfigurationUIWrapper copy() {
        List<DLightTool> toolsCopy = new ArrayList<DLightTool>(getTools().size());
        for (DLightToolUIWrapper dltuWrapper : getTools()) {
            toolsCopy.add(dltuWrapper.getDLightTool());
        }
        DLightConfigurationUIWrapper copy = new DLightConfigurationUIWrapper(getDLightConfiguration(), toolsCopy);
        copy.setProfileOnRun(isProfileOnRun());
        copy.setModified(false);
        return copy;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the copyOf
     */
    public DLightConfiguration getCopyOf() {
        return copyOf;
    }

    /**
     * @param copyOf the copyOf to set
     */
    public void setCopyOf(DLightConfiguration copyOf) {
        this.copyOf = copyOf;
    }
}
