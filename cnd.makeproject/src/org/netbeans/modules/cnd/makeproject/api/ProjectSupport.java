/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.makeproject.api;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CancellationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.makeproject.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;

public class ProjectSupport {
    private ProjectSupport() {
    }

    public static boolean saveAllProjects(String extraMessage) {
	boolean ok = true;
	Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
	for (int i = 0; i < openProjects.length; i++) {
	    MakeConfigurationDescriptor projectDescriptor = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(openProjects[i]);
	    if (projectDescriptor != null) {
                ok = ok && projectDescriptor.save(extraMessage);
            }
	}
	return ok;
    }

    public static Date lastModified(Project project) {
	FileObject projectFile = null;
	try {
	    projectFile = project.getProjectDirectory().getFileObject(MakeConfiguration.NBPROJECT_FOLDER + File.separator + MakeConfiguration.MAKEFILE_IMPL); // NOI18N
	}
	catch (Exception e) {
	    // happens if project is not a MakeProject
	}
	if (projectFile == null) {
            projectFile = project.getProjectDirectory();
        }
	return projectFile.lastModified();
    }

    public static void executeCustomAction(Project project, ProjectActionHandler customProjectActionHandler) {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class );
        if (pdp == null) {
            return;
        }
        MakeConfigurationDescriptor projectDescriptor = pdp.getConfigurationDescriptor();
        MakeConfiguration conf = projectDescriptor.getActiveConfiguration();
        if (conf == null) {
            return;
        }

        MakeActionProvider ap = project.getLookup().lookup(MakeActionProvider.class );
        if (ap == null) {
            return;
        }

        ap.invokeCustomAction(projectDescriptor, conf, customProjectActionHandler);
    }

    public static MakeProjectOptions.PathMode getPathMode(RemoteProject.Mode remoteMode) {
        return (remoteMode == RemoteProject.Mode.REMOTE_SOURCES) ? MakeProjectOptions.PathMode.ABS : MakeProjectOptions.getPathMode();
    }

    public static MakeProjectOptions.PathMode getPathMode(Project project) {
        if (project instanceof MakeProject) {
            RemoteProject remoteProject = project.getLookup().lookup(RemoteProject.class);
            if (remoteProject != null && remoteProject.getRemoteMode() == RemoteProject.Mode.REMOTE_SOURCES) {
                return MakeProjectOptions.PathMode.ABS;
            }
        }
        return MakeProjectOptions.getPathMode();
    }

    public static String toProperPath(FileObject base, FileObject path, Project project) {
        return toProperPath(base, path, getPathMode(project));
    }

    public static String toProperPath(FileObject base, String path, Project project) {
        return toProperPath(base, path, getPathMode(project));
    }

    public static String toProperPath(String base, String path, Project project) {
        return toProperPath(base, path, getPathMode(project));
    }

    public static String toProperPath(FileObject base, FileObject path, MakeProjectOptions.PathMode pathMode) {
        switch (pathMode) {
            case REL_OR_ABS:
                return CndPathUtilitities.toAbsoluteOrRelativePath(base, path);
            case REL:
                return CndPathUtilitities.toRelativePath(base, path);
            case ABS:
                return CndPathUtilitities.toAbsolutePath(base, path);
            default:
                throw new IllegalStateException("Unexpected path mode: " + pathMode); //NOI18N
        }
    }

    public static String toProperPath(FileObject base, String path, MakeProjectOptions.PathMode pathMode) {
        switch (pathMode) {
            case REL_OR_ABS:
                return CndPathUtilitities.toAbsoluteOrRelativePath(base, path);
            case REL:
                return CndPathUtilitities.toRelativePath(base, path);
            case ABS:
                return CndPathUtilitities.toAbsolutePath(base, path);
            default:
                throw new IllegalStateException("Unexpected path mode: " + pathMode); //NOI18N
        }
    }

    public static String toProperPath(String base, String path, MakeProjectOptions.PathMode pathMode) {
        switch (pathMode) {
            case REL_OR_ABS:
                return CndPathUtilitities.toAbsoluteOrRelativePath(base, path);
            case REL:
                return CndPathUtilitities.toRelativePath(base, path);
            case ABS:
                return CndPathUtilitities.toAbsolutePath(base, path);
            default:
                throw new IllegalStateException("Unexpected path mode: " + pathMode); //NOI18N
        }
    }

    public static String convertWorkingDirToRemoteIfNeeded(ProjectActionEvent pae, String localDir) {
        ExecutionEnvironment execEnv = pae.getConfiguration().getDevelopmentHost().getExecutionEnvironment();
        if (!checkConnection(execEnv)) {
            return null;
        }
        if (execEnv.isRemote()) {
            if (RemoteSyncSupport.getRemoteMode(pae.getProject()) == RemoteProject.Mode.LOCAL_SOURCES) {
                PathMap mapper = RemoteSyncSupport.getPathMap(pae.getProject());
                return mapper.getRemotePath(localDir, false);
            } else {
                CndUtils.assertAbsolutePathInConsole(localDir);
                if (CndPathUtilitities.isPathAbsolute(localDir)) {
                    return localDir;
                } else {
                    RemoteProject remoteProject = pae.getProject().getLookup().lookup(RemoteProject.class);
                    CndUtils.assertNotNullInConsole(pae, localDir);
                    if (remoteProject != null) {
                        localDir = remoteProject.getSourceBaseDir() + '/' + localDir;
                        localDir = FileSystemProvider.normalizeAbsolutePath(localDir, execEnv);
                        return localDir;
                    }
                }
            }
        }
        return localDir;
    }

    public static boolean checkConnection(ExecutionEnvironment execEnv) {
        if (execEnv.isRemote()) {
            try {
                ConnectionManager.getInstance().connectTo(execEnv);
                ServerRecord record = ServerList.get(execEnv);
                if (record.isOffline()) {
                    record.validate(true);
                }
                return record.isOnline();
            } catch (IOException ex) {
                return false;
            } catch (CancellationException ex) {
                return false;
            }
        } else {
            return true;
        }
    }

}
