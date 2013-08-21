/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Utilities;

/**
 *
 * @author alsimon
 */
public class CompilerSettings {

    private ProjectBridge projectBridge;
    private final List<String> systemIncludePathsC;
    private final List<String> systemIncludePathsCpp;
    private final Map<String, String> systemMacroDefinitionsC;
    private final Map<String, String> systemMacroDefinitionsCpp;
    private Map<String, String> normalizedPaths = new ConcurrentHashMap<String, String>();
    private final CompilerFlavor compileFlavor;
    private final String cygwinDriveDirectory;
    private final boolean isWindows;
    private final ExecutionEnvironment developmentHostExecutionEnvironment;

    public CompilerSettings(ProjectProxy project) {
        projectBridge = DiscoveryUtils.getProjectBridge(project);
        systemIncludePathsCpp = DiscoveryUtils.getSystemIncludePaths(projectBridge, true);
        systemIncludePathsC = DiscoveryUtils.getSystemIncludePaths(projectBridge, false);
        systemMacroDefinitionsCpp = DiscoveryUtils.getSystemMacroDefinitions(projectBridge, true);
        systemMacroDefinitionsC = DiscoveryUtils.getSystemMacroDefinitions(projectBridge, false);
        compileFlavor = DiscoveryUtils.getCompilerFlavor(projectBridge);
        isWindows = Utilities.isWindows();
        if (isWindows) {
            cygwinDriveDirectory = DiscoveryUtils.getCygwinDrive(projectBridge);
        } else {
            cygwinDriveDirectory = null;
        }
        if (projectBridge != null) {
            developmentHostExecutionEnvironment = projectBridge.getDevelopmentHostExecutionEnvironment();
        } else {
            developmentHostExecutionEnvironment = null;
        }
    }

    public ProjectBridge getProjectBridge() {
        return projectBridge;
    }

    public boolean isRemoteDevelopmentHost() {
        if (developmentHostExecutionEnvironment == null) {
            return false;
        }
        return developmentHostExecutionEnvironment.isRemote();
    }
    
    public List<String> getSystemIncludePaths(ItemProperties.LanguageKind lang) {
        if (lang == ItemProperties.LanguageKind.CPP) {
            return systemIncludePathsCpp;
        } else if (lang == ItemProperties.LanguageKind.C) {
            return systemIncludePathsC;
        }
        return Collections.<String>emptyList();
    }

    public Map<String, String> getSystemMacroDefinitions(ItemProperties.LanguageKind lang) {
        if (lang == ItemProperties.LanguageKind.CPP) {
            return systemMacroDefinitionsCpp;
        } else if (lang == ItemProperties.LanguageKind.C) {
            return systemMacroDefinitionsC;
        }
        return Collections.<String, String>emptyMap();
    }

    public String getNormalizedPath(String path) {
        String res = normalizedPaths.get(path);
        if (res == null) {
            res = PathCache.getString(normalizePath(path));
            normalizedPaths.put(PathCache.getString(path), res);
        }
        return res;
    }

    protected String normalizePath(String path) {
        if (path.startsWith("/")) { // NOI18N
            if (isWindows() && isRemoteDevelopmentHost()) {
                path = PathUtilities.normalizeUnixPath(path);
            } else {
                path = DiscoveryUtils.normalizeAbsolutePath(path);
            }
        } else if (path.length()>2 && path.charAt(1)==':') {
            path = DiscoveryUtils.normalizeAbsolutePath(path);
        }
        if (Utilities.isWindows()) {
            path = path.replace('\\', '/');
        }
        return path;
    }

    public CompilerFlavor getCompileFlavor() {
        return compileFlavor;
    }

    public String getCygwinDrive() {
        return cygwinDriveDirectory;
    }

    public boolean isWindows() {
        return isWindows;
    }

    public void dispose() {
        systemIncludePathsC.clear();
        systemIncludePathsCpp.clear();
        systemMacroDefinitionsC.clear();
        systemMacroDefinitionsCpp.clear();
        normalizedPaths.clear();
        normalizedPaths = new ConcurrentHashMap<String, String>();
    }
}
