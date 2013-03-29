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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.classpath.DependencyProjectsProvider;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import static org.netbeans.modules.maven.apisupport.MavenNbModuleImpl.GROUPID_MOJO;
import static org.netbeans.modules.maven.apisupport.MavenNbModuleImpl.NBM_PLUGIN;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

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
        final List<DependencyProjectsProvider.Pair> nonModules = new ArrayList<DependencyProjectsProvider.Pair>();
        final Map<String, List<DependencyProjectsProvider.Pair>> extraCP = new HashMap<String, List<DependencyProjectsProvider.Pair>>();
        final Map<String, DependencyProjectsProvider.Pair> modules = new HashMap<String, DependencyProjectsProvider.Pair>();
        final Set<DependencyProjectsProvider.Pair> all = dpp.getDependencyProjects();
        for (DependencyProjectsProvider.Pair pair : all) {
            if (pair.isIncludedAtRuntime() && RunUtils.isCompileOnSaveEnabled(pair.getProject())) {
                NbModuleProvider nbm = pair.getProject().getLookup().lookup(NbModuleProvider.class);
                if (nbm != null) {
                    Artifact a = pair.getArtifact();
                    modules.put(a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getType() + ":" + a.getBaseVersion(), pair);
                } else {
                    nonModules.add(pair);
                }
            }
        }
        if (!nonModules.isEmpty()) {
            for (DependencyProjectsProvider.Pair pair : nonModules) {
                //TODO what to do for OSGI (netbeans.patches. doesn't work here) and for non module jars (find where they belong?)
                List<String> trail = new ArrayList(pair.getArtifact().getDependencyTrail());
                Collections.reverse(trail); //we go from the artifact itself up, first module project artifact is our boy..
                trail.remove(0); //first one is the actual artifact.
                for (String s : trail) {
                    if (modules.containsKey(s)) {
                        //we have found a module match
                        List<DependencyProjectsProvider.Pair> cp = extraCP.get(s);
                        if (cp == null) {
                            cp = new ArrayList<DependencyProjectsProvider.Pair>();
                            extraCP.put(s, cp);
                        }
                        cp.add(pair);
                    }
                }
            }
        }
        for (Map.Entry<String, DependencyProjectsProvider.Pair> pairEnt : modules.entrySet()) {
                NbModuleProvider nbm = pairEnt.getValue().getProject().getLookup().lookup(NbModuleProvider.class);
                if (nbm != null) {
                    String cnb = nbm.getCodeNameBase();
                    if (sb.length() > 0) {
                        sb.append(" ");
                    }
                    //TODO space in path, how to do?
                    sb.append("-J-Dnetbeans.patches.").append(cnb).append("=").append(projectToOutputDir(pairEnt.getValue().getProject()));
                    List<DependencyProjectsProvider.Pair> ex = extraCP.get(pairEnt.getKey());
                    boolean useOsgiDeps = MavenWhiteListQueryImpl.isUseOSGiDependencies(pairEnt.getValue().getProject());
                    if (ex != null && !ex.isEmpty()) {
                        for (DependencyProjectsProvider.Pair exPair : ex) {
                            NbMavenProject nbprj = exPair.getProject().getLookup().lookup(NbMavenProject.class);
                            if (nbprj != null && NbMavenProject.TYPE_OSGI.equals(nbprj.getPackagingType()) && useOsgiDeps) {
                                continue; //skip osgi if used as dependency, not classpath
                            }
                            //: or ; is there a constant for it?
                            sb.append(Utilities.isWindows() ? ";" : ":").append(projectToOutputDir(exPair.getProject()));
                            
                        }
                    }
                }            
            
        }
        if (sb.length() > 0) {
            config.setProperty("netbeans.run.params", sb.toString());
        }
        return true;
    }
    
    private String projectToOutputDir(Project p) {
        return new File(new File(FileUtil.toFile(p.getProjectDirectory()), "target"), "classes").getAbsolutePath();
    }

}
