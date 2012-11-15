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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem.ProjectItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.remote.sync.SharabilityFilter;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Misc project related utility functions
 * @author Vladimir Kvashin
 */
public class RemoteProjectSupport {

    private RemoteProjectSupport() {
    }

    public static ExecutionEnvironment getExecutionEnvironment(Project project) {
        MakeConfiguration mk = ConfigurationSupport.getProjectActiveConfiguration(project);
        if (mk != null) {
            return mk.getDevelopmentHost().getExecutionEnvironment();
        }
        return null;
    }

    public static boolean projectExists(Project project) {
        final FileObject projDirFO = project.getProjectDirectory();
        if(projDirFO != null && projDirFO.isValid()) {
            FileObject nbprojectFO = projDirFO.getFileObject("nbproject"); //NOI18N
            if (nbprojectFO != null && nbprojectFO.isValid()) {
                return true;
            }
        }
        return false;
    }

    public static FileObject getPrivateStorage(Project project) {
        FileObject baseDir = project.getProjectDirectory();
        if (baseDir == null) {
            return null;
        }
        try {
            return FileUtil.createFolder(baseDir, "nbproject/private"); //NOI18N
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.INFO, ex.getMessage(), ex); //NOI18N
            return null;
        }
    }

    public static FSPath[] getProjectSourceDirs(Project project, AtomicReference<String> runDir) {
        MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(project);
        FileSystem fs;
        try {
            fs = project.getProjectDirectory().getFileSystem();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return new FSPath[0];
        }
        if (conf == null) {
            return new FSPath[] { FSPath.toFSPath(project.getProjectDirectory()) };
        } else {
            return getProjectSourceDirs(project, conf, runDir);
        }
    }

    public static FSPath[] getProjectSourceDirs(Project project, MakeConfiguration conf, AtomicReference<String> runDir) {
        FileObject baseDir = project.getProjectDirectory();
        if (baseDir == null) {
            return new FSPath[0];
        }
        if (conf == null) {
            return new FSPath[] { FSPath.toFSPath(baseDir) };
        }
        if (runDir != null) {
            String d = conf.getMakefileConfiguration().getBuildCommandWorkingDirValue();
            FileSystem fs = conf.getBaseFSPath().getFileSystem();
            if (!CndPathUtilitities.isPathAbsolute(d)) {
                d = conf.getBaseFSPath().getPath() + FileSystemProvider.getFileSeparatorChar(fs) + d;
            }
            d = FileSystemProvider.normalizeAbsolutePath(d, fs);
            runDir.set(d);
        }
        // the project itself
        Set<FSPath> sourceFilesAndDirs = new HashSet<FSPath>();
        if (!conf.isMakefileConfiguration()) {
            sourceFilesAndDirs.add(FSPath.toFSPath(baseDir));
        }

        MakeConfigurationDescriptor mcs = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        if (mcs == null) {
            return new FSPath[0];
        }
        if (!mcs.getBaseDirFileObject().isValid()) {
            // no disk files
            return sourceFilesAndDirs.toArray(new FSPath[sourceFilesAndDirs.size()]);
        }
        for(String soorceRoot : mcs.getSourceRoots()) {
            // not sure whether they are absolute and normalized, so:
            FileObject fo = FileSystemProvider.getFileObject(baseDir, soorceRoot);
            if (fo != null) {
                sourceFilesAndDirs.add(FSPath.toFSPath(fo));
            }
        }
        addExtraFiles(mcs, sourceFilesAndDirs);
        List<Project> subProjects = new ArrayList<Project>(conf.getSubProjects());
        // required projects are different - see #194997
        for (ProjectItem requiredProject : conf.getRequiredProjectsConfiguration().getValue() ) {
            Project p = requiredProject.getProject(conf.getBaseFSPath());
            if (p != null) {
                subProjects.add(p);                
            }
        }        
        // Then go trough open subprojects and add their external source roots
        for (Project subProject : subProjects) {
            if (subProject.getProjectDirectory().isValid()) {
                sourceFilesAndDirs.add(FSPath.toFSPath(subProject.getProjectDirectory()));
                MakeConfigurationDescriptor subMcs =
                        MakeConfigurationDescriptor.getMakeConfigurationDescriptor(subProject);
                for(String soorceRoot : mcs.getSourceRoots()) {
                    FileObject fo = FileSystemProvider.getFileObject(subProject.getProjectDirectory(), soorceRoot);
                    if (fo != null) {
                        sourceFilesAndDirs.add(FSPath.toFSPath(fo));
                    }
                }
                addExtraFiles(subMcs, sourceFilesAndDirs);
            }
        }
        return sourceFilesAndDirs.toArray(new FSPath[sourceFilesAndDirs.size()]);
    }

    private static void addExtraFiles(MakeConfigurationDescriptor subMcs, Set<FSPath> filesToSync) {
        addExtraFiles(subMcs, filesToSync, subMcs.getProjectItems());
        addExtraFiles(subMcs, filesToSync, subMcs.getExternalFileItemsAsArray());
    }
    
    private static void addExtraFiles(MakeConfigurationDescriptor subMcs, Set<FSPath> filesToSync, Item[] items) {
        SharabilityFilter filter = new SharabilityFilter();
        for (Item item : items) {
            FileObject fo = item.getFileObject();
            if (fo == null || !fo.isValid()) {
                // it won't be added while recursin into directories
                filesToSync.add(item.getFSPath());
            } else if (!filter.accept(fo)) {
                // !normFO.isValid() is for inexistent file - we won't get it via directories recursion
                // !filter.accept(normFO) means that we'll filter it out, but it's explicitely in project
                filesToSync.add(item.getFSPath());
            } else if (!isContained(item.getNormalizedPath(), filesToSync)) {
                // directory containing file is not yet added => copy it
                filesToSync.add(item.getFSPath());
            }
        }
    }

    private static boolean isContained(String itemAbsPath, Set<FSPath> paths) {
        for (FSPath dir : paths) {
            String alreadyAddedPath = dir.getPath() + '/';
            if (itemAbsPath.startsWith(alreadyAddedPath)) {
                return true;
            }
        }
        return false;
    }
}
