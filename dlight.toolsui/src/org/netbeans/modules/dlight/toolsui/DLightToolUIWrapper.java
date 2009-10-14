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
package org.netbeans.modules.dlight.toolsui;

import org.netbeans.modules.dlight.api.tool.*;

/**
 *
 * @author thp
 */
public class DLightToolUIWrapper {

    private DLightTool dlightTool;
    private boolean enabled;
    private boolean modified = false;
    private boolean canEnable = true;

    public DLightToolUIWrapper(DLightTool dlightTool, boolean enabled, boolean canChange) {
        this.dlightTool = dlightTool;
        this.enabled = enabled;
        this.canEnable = canChange;
    }

    /**
     * @return the dLightTool
     */
    public DLightTool getDLightTool() {
        return dlightTool;
    }

    /**
     * @param dLightTool the dLightTool to set
     */
    public void setDLightTool(DLightTool dlightTool) {
        this.dlightTool = dlightTool;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled){
            modified = true;
        }
        this.enabled = enabled;
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

    public DLightToolUIWrapper copy() {
        DLightToolUIWrapper copy = new DLightToolUIWrapper(getDLightTool(), isEnabled(), canEnable());
        copy.setModified(false);
        return copy;
    }

    /**
     * @return the canChange
     */
    public boolean canEnable() {
        return canEnable;
    }

    /**
     * @param canChange the canChange to set
     */
    public void setCanEnable(boolean canChange) {
        this.canEnable = canChange;
    }
}
