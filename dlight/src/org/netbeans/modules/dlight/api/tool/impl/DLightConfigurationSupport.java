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
package org.netbeans.modules.dlight.api.tool.impl;

import java.util.List;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationManager;
import org.netbeans.modules.dlight.api.tool.DLightTool;

/**
 * This class for internal use only:
 * Use it to
 */
public final class DLightConfigurationSupport {

    private final static DLightConfigurationSupport instance = new DLightConfigurationSupport();

    private DLightConfigurationSupport() {
    }

    public static synchronized DLightConfigurationSupport getInstance() {
        return instance;
    }

    /**
     * Sets default tool set for the
     * @param configurationName
     * @param tools
     */
    public final void setDefaultToolSet(String configurationName, List<DLightTool> tools) {
        //create XML layer
    }

    public boolean registerTool(String configurationName, DLightTool dlightTool) {
        DLightConfiguration configuration = DLightConfigurationManagerAccessor.getDefault().getDefaultConfiguration(DLightConfigurationManager.getInstance());
        return DLightConfigurationManagerAccessor.getDefault().registerTool(DLightConfigurationManager.getInstance(), configurationName, dlightTool);
    }

    public boolean registerTool(String configurationName, String toolID, boolean isOnByDefault) {
        return DLightConfigurationManagerAccessor.getDefault().registerTool(configurationName, toolID, isOnByDefault);
    }


    public boolean deleteTool(String configurationName, DLightTool dlightTool) {
        DLightConfiguration configuration = DLightConfigurationManagerAccessor.getDefault().getDefaultConfiguration(DLightConfigurationManager.getInstance());
        return DLightConfigurationManagerAccessor.getDefault().deleteTool(DLightConfigurationManager.getInstance(), configurationName, dlightTool);

    }

    public DLightConfiguration registerConfiguration(String configurationName, String displayedName, String category, List<String> platforms, String collector, List<String> indicators){
        return DLightConfigurationManagerAccessor.getDefault().registerConfiguration(DLightConfigurationManager.getInstance(), configurationName, displayedName, category, platforms, collector, indicators);
    }

    public boolean removeConfiguration(String configurationName){
        return DLightConfigurationManagerAccessor.getDefault().removeConfiguration(configurationName);
    }

    public boolean canRemoveConfiguration(String configurationName){
        return DLightConfigurationManagerAccessor.getDefault().canRemoveConfiguration(configurationName);
    }
}
