/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.apisupport;

import java.io.File;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.classpath.DependencyProjectsProvider;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileUtil;

/**
 * adds -J-Dnetbeans.patches.%cnb%=%path% to netbeans.run.params execution parameter, links up the module Compile on Save outputs.
 * @author mkleint
 */
@ProjectServiceProvider(service=LateBoundPrerequisitesChecker.class, projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM_APPLICATION)
public class CoSApplicationLateBoundChecker implements LateBoundPrerequisitesChecker {

    @Override
    public boolean checkRunConfig(RunConfig config, ExecutionContext con) {
        if (config.getProject() == null) {
            return true;
        }
        DependencyProjectsProvider dpp = config.getProject().getLookup().lookup(DependencyProjectsProvider.class);
        String params = config.getProperties().get("netbeans.run.params");
        StringBuilder sb = new StringBuilder(params != null ? params : "");
        for (DependencyProjectsProvider.Pair pair : dpp.getDependencyProjects()) {
            if (pair.isIncludedAtRuntime() && RunUtils.isCompileOnSaveEnabled(pair.getProject())) {
                NbModuleProvider nbm = pair.getProject().getLookup().lookup(NbModuleProvider.class);
                if (nbm != null) {
                    String cnb = nbm.getCodeNameBase();
                    if (sb.length() > 0) {
                        sb.append(" ");
                    }
                    //TODO space in path, how to do?
                    sb.append("-J-Dnetbeans.patches.").append(cnb).append("=").append(new File(new File(FileUtil.toFile(pair.getProject().getProjectDirectory()), "target"), "classes").getAbsolutePath());
                } else {
                    //TODO what to do for OSGI (netbeans.patches. doesn't work here) and for non module jars (find where they belong?)
                }
            }
        }
        if (sb.length() > 0) {
            config.setProperty("netbeans.run.params", sb.toString());
        }
        return true;
    }

}
