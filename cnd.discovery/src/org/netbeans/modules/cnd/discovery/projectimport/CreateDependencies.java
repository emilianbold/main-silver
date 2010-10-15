/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.discovery.projectimport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface.Applicable;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryExtension;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem.ProjectItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * 
 * @author Alexander Simon
 */
public class CreateDependencies implements PropertyChangeListener {
    private static final boolean TRACE = true;
    private static final RequestProcessor RP = new RequestProcessor(ImportExecutable.class.getName(), 1);
    private final Project mainProject;
    private final List<String> dependencies;
    private List<String> paths;
    private final Map<Project, String> createdProjects = new HashMap<Project,String>();
    private MakeConfigurationDescriptor mainConfigurationDescriptor;

    public CreateDependencies(Project mainProject, List<String> dependencies, List<String> paths) {
        this.mainProject = mainProject;
        this.dependencies = dependencies;
        this.paths = paths;
    }

    public void create() {
        ConfigurationDescriptorProvider pdp = mainProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (!pdp.gotDescriptor()) {
            return;
        }
        mainConfigurationDescriptor = pdp.getConfigurationDescriptor();
        if (paths == null) {
            if (dependencies == null || dependencies.isEmpty()) {
                return;
            }
            Map<String,String> dllPaths = new HashMap<String, String>();
            String root = ImportExecutable.findFolderPath(ImportExecutable.getRoot(mainConfigurationDescriptor));
            if (root != null) {
                MakeConfiguration activeConfiguration = mainConfigurationDescriptor.getActiveConfiguration();
                String ldLibPath = ImportExecutable.getLdLibraryPath(activeConfiguration);
                boolean search = false;
                for(String dll : dependencies) {
                    String p = ImportExecutable.findLocation(dll, ldLibPath);
                    if (p != null) {
                        dllPaths.put(dll, p);
                    } else {
                        search = true;
                        dllPaths.put(dll, null);
                    }
                }
                if (search) {
                    ImportExecutable.gatherSubFolders(new File(root), new HashSet<String>(), dllPaths);
                }
            }
            paths = new ArrayList<String>();
            for(Map.Entry<String, String> entry : dllPaths.entrySet()) {
                if (entry.getValue() != null) {
                    if (ImportExecutable.isMyDll(entry.getValue(), root)) {
                        paths.add(entry.getValue());
                    }
                }
            }
        }
        for(String  entry : paths) {
            try {
                Project createProject = createProject(entry, "", "", ""); // NOI18N
                createdProjects.put(createProject, entry);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (!createdProjects.isEmpty()) {
            OpenProjects.getDefault().addPropertyChangeListener(this);
            Project[] toOpen = new Project[createdProjects.size()];
            int i = 0;
            for(Project p : createdProjects.keySet()) {
                toOpen[i] = p;
                i++;
            }
            OpenProjects.getDefault().open(toOpen, false);
        }
    }

    private static void updateRunProfile(String baseDir, RunProfile runProfile, String arguments, String dir, String envText) {
        // Arguments
        runProfile.setArgs(arguments);
        // Working dir
        String wd = dir;
        wd = CndPathUtilitities.toRelativePath(baseDir, wd);
        wd = CndPathUtilitities.normalize(wd);
        runProfile.setRunDirectory(wd);
        // Environment
        Env env = runProfile.getEnvironment();
	env.removeAll();
        env.decode(envText);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
            if (evt.getNewValue() instanceof Project[]) {
                Project[] projects = (Project[])evt.getNewValue();
                if (projects.length == 0) {
                    return;
                }
                for(Project aProject : projects) {
                    if (createdProjects.containsKey(aProject)){
                        addReqProject(aProject);
                    }
                }
                ImportExecutable.saveMakeConfigurationDescriptor(mainProject);
                for(Project aProject : projects) {
                    String executable = null;
                    if (createdProjects.containsKey(aProject)){
                        executable = createdProjects.get(aProject);
                        createdProjects.remove(aProject);
                    } else {
                        continue;
                    }
                    IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
                    if (extension != null) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("DW:buildResult", executable); // NOI18N
                        map.put("DW:consolidationLevel", "file"); // NOI18N
                        map.put("DW:rootFolder", aProject.getProjectDirectory().getPath()); // NOI18N
                        process((DiscoveryExtension)extension, aProject, map);
                    }
                }
                if (createdProjects.isEmpty()) {
                    OpenProjects.getDefault().removePropertyChangeListener(this);
                }
            }
        }
    }

    private void addReqProject(Project lastSelectedProject) {
        ConfigurationDescriptorProvider provider = lastSelectedProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor configurationDescriptor = provider.getConfigurationDescriptor(true);
        mainConfigurationDescriptor.getActiveConfiguration().getRequiredProjectsConfiguration().add(
                new ProjectItem(new MakeArtifact(configurationDescriptor, configurationDescriptor.getActiveConfiguration())));
    }

    public void process(final DiscoveryExtension extension, final Project lastSelectedProject, final Map<String, Object> map){
        ImportExecutable.switchModel(false, lastSelectedProject);
        Task post = RP.post(new Runnable() {

            @Override
            public void run() {
                ProgressHandle progress = ProgressHandleFactory.createHandle(NbBundle.getBundle(ImportExecutable.class).getString("ImportExecutable.Progress")); // NOI18N
                progress.start();
                try {
                    ConfigurationDescriptorProvider provider = lastSelectedProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
                    MakeConfigurationDescriptor configurationDescriptor = provider.getConfigurationDescriptor(true);
                    Applicable applicable = extension.isApplicable(map, lastSelectedProject);
                    if (applicable.isApplicable()) {
                        ImportExecutable.resetCompilerSet(configurationDescriptor.getActiveConfiguration(), applicable);
                        if (extension.canApply(map, lastSelectedProject)) {
                            try {
                                extension.apply(map, lastSelectedProject);
                                ImportExecutable.saveMakeConfigurationDescriptor(lastSelectedProject);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    ImportExecutable.switchModel(true, lastSelectedProject);
                } catch (Throwable ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    progress.finish();
                }
            }
        });
    }
    
    private static Project createProject(String executablePath, String arguments, String dir, String envText) throws IOException {
        Project project;
        String projectParentFolder = ProjectGenerator.getDefaultProjectFolder();
        String projectName = ProjectGenerator.getValidProjectName(projectParentFolder, new File(executablePath).getName());
        String baseDir = projectParentFolder + File.separator + projectName;
        MakeConfiguration conf = new MakeConfiguration(baseDir, "Default", MakeConfiguration.TYPE_MAKEFILE); // NOI18N
        // Working dir
        String wd = new File(executablePath).getParentFile().getPath();
        wd = CndPathUtilitities.toRelativePath(baseDir, wd);
        wd = CndPathUtilitities.normalize(wd);
        conf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(wd);
        // Executable
        String exe = executablePath;
        exe = CndPathUtilitities.toRelativePath(baseDir, exe);
        exe = CndPathUtilitities.normalize(exe);
        conf.getMakefileConfiguration().getOutput().setValue(exe);
        updateRunProfile(baseDir, conf.getProfile(), arguments, dir, envText);
        ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(projectName, projectParentFolder);
        prjParams.setOpenFlag(false).setConfiguration(conf).setImportantFiles(Collections.<String>singletonList(exe).iterator());
        project = ProjectGenerator.createBlankProject(prjParams);
        return project;
    }
}
