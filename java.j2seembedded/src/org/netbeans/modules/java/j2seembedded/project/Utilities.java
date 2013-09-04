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
package org.netbeans.modules.java.j2seembedded.project;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.java.j2seembedded.platform.RemotePlatform;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.cookies.CloseCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class Utilities {

    private static final String PLATFORM_RUNTIME = "platform.runtime"; //NOI18N
    private static final String TARGET_RUN = "$target.run";             //NOI18N
    private static final String TARGET_DEBUG = "$target.debug";         //NOI18N
    private static final String COS_DISABLE = "compile.on.save.unsupported.remote.platform"; //NOI18N
    private static final String DEBUG_TRANSPORT = "debug-transport";        //NOI18N
    private static final String EXTENSION_NAME = "remote-platform-1";       //NOI18N
    private static final String BUILD_SCRIPT_PATH = "nbproject/remote-platform-impl.xml";   //NOI18N
    private static final String BUILD_SCRIPT_BACK_UP = "remote-platform-impl_backup";   //NOI18N
    private static final String BUILD_SCRIPT_PROTOTYPE = "/org/netbeans/modules/java/j2seembedded/resources/remote-platform-impl.xml";  //NOI18N
    private static final Map<String,String> CONFIG_PROPERTIES;
    static {
        Map<String,String> m = new HashMap<>();
        m.put(TARGET_RUN,"run-remote");     //NOI18N
        m.put(TARGET_DEBUG,"debug-remote");     //NOI18N
        m.put(COS_DISABLE, Boolean.TRUE.toString());
        m.put(DEBUG_TRANSPORT,"dt_socket"); //NOI18N
        CONFIG_PROPERTIES = Collections.unmodifiableMap(m);
    }

    private static final Logger LOG = Logger.getLogger(RemotePlatformProjectSaver.class.getName());

    private Utilities() {
        throw new IllegalStateException();
    }

    static final class UpdateConfigResult {

        private final Collection<String> updated;
        private final Collection<String> upToDate;


        private UpdateConfigResult(
            @NonNull final Collection<String> updated,
            @NonNull final Collection<String> upToDate) {
            Parameters.notNull("updated", updated); //NOI18N
            Parameters.notNull("upToDate", upToDate); //NOI18N
            this.updated = Collections.unmodifiableCollection(updated);
            this.upToDate = Collections.unmodifiableCollection(upToDate);
        }

        @NonNull
        Collection<String> getUpdatedConfigs() {
            return updated;
        }

        @NonNull
        Collection<String> getUpToDateConfigs() {
            return upToDate;
        }

        boolean hasRemotePlatform() {
            return !updated.isEmpty() || !upToDate.isEmpty();
        }
    };


    static boolean hasRemotePlatform(@NonNull final Project prj) {
        final PropertyEvaluator eval = prj.getLookup().lookup(J2SEPropertyEvaluator.class).evaluator();
        final String rpid = eval.getProperty(PLATFORM_RUNTIME);
        if (rpid != null) {
            final JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(
                null,
                new Specification(RemotePlatform.SPEC_NAME, null));
            for (JavaPlatform platform : platforms) {
                final String antPlatformName = platform.getProperties().get(RemotePlatform.PLAT_PROP_ANT_NAME);
                if (rpid.equals(antPlatformName)) {
                    return true;
                }
            }
        }
        return false;
    }

    static UpdateConfigResult updateRemotePlatformConfigurations(@NonNull final Project prj) throws IOException {
        try {
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<UpdateConfigResult>() {
                @Override
                public UpdateConfigResult run() throws Exception {
                    final Set<String> updated = new HashSet<>();
                    final Set<String> upToDate = new HashSet<>();
                    final FileObject prjDir = prj.getProjectDirectory();
                    if (prjDir != null) {
                        final FileObject cfgFolder = prjDir.getFileObject("nbproject/configs"); //NOI18N
                        if (cfgFolder != null) {
                            for (FileObject cfgFile : cfgFolder.getChildren()) {
                                if (!cfgFile.hasExt("properties")) {    //NOI18N
                                    continue;
                                }
                                final String relPath = FileUtil.getRelativePath(prjDir, cfgFile);
                                if (relPath != null) {
                                    final EditableProperties ep = new EditableProperties(true);
                                    try (final InputStream in = cfgFile.getInputStream()){
                                        ep.load(in);
                                    }
                                    final String runtimePlatform = ep.getProperty(PLATFORM_RUNTIME);
                                    if (runtimePlatform != null && !runtimePlatform.isEmpty()) {
                                        if (configAlreadyUpdated(ep)) {
                                            upToDate.add(relPath);
                                        } else {
                                            updateConfig(ep);
                                            final FileLock lock = cfgFile.lock();
                                            try (final OutputStream out = cfgFile.getOutputStream(lock)) {
                                                ep.store(out);
                                            } finally {
                                                lock.releaseLock();
                                            }
                                            updated.add(relPath);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return new UpdateConfigResult(updated, upToDate);
                }
            });
        } catch (MutexException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new IOException(e);
            }
        }
    }

    static boolean  hasRemoteExtension(@NonNull final Project project) {
        final AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        if (extender == null) {
            return false;
        }
        return extender.getExtension(EXTENSION_NAME) != null;
    }

    static void addRemoteExtension(@NonNull final Project project) throws IOException {
        final AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        if (extender == null) {
            LOG.log(
                Level.WARNING,
                "The project {0} ({1}) does not support AntBuildExtender.",     //NOI18N
                new Object[] {
                    ProjectUtils.getInformation(project).getDisplayName(),
                    FileUtil.getFileDisplayName(project.getProjectDirectory())
                });
            return;
        }
        final FileObject rpBuildScript = copyBuildScript(project);
        extender.addExtension(EXTENSION_NAME, rpBuildScript);
    }

    @NonNull
    static FileObject copyBuildScript (@NonNull final Project project) throws IOException {
        final FileObject projDir = project.getProjectDirectory();
        FileObject rpBuildScript = projDir.getFileObject(BUILD_SCRIPT_PATH);
        if (rpBuildScript != null && !isBuildScriptUpToDate(project)) {
            // try to close the file just in case the file is already opened in editor
            DataObject dobj = DataObject.find(rpBuildScript);
            CloseCookie closeCookie = dobj.getLookup().lookup(CloseCookie.class);
            if (closeCookie != null) {
                closeCookie.close();
            }
            final FileObject nbproject = projDir.getFileObject("nbproject");                    //NOI18N
            final FileObject backupFile = nbproject.getFileObject(BUILD_SCRIPT_BACK_UP, "xml"); //NOI18N
            if (backupFile != null) {
                backupFile.delete();
            }
            FileUtil.moveFile(rpBuildScript, nbproject, BUILD_SCRIPT_BACK_UP);
            rpBuildScript = null;
        }
        if (rpBuildScript == null) {
            rpBuildScript = FileUtil.createData(project.getProjectDirectory(), BUILD_SCRIPT_PATH);
            try(
                final InputStream in = new BufferedInputStream(RemotePlatformProjectSaver.class.getResourceAsStream(BUILD_SCRIPT_PROTOTYPE));
                final OutputStream out = new BufferedOutputStream(rpBuildScript.getOutputStream())) {
                FileUtil.copy(in, out);
            }
        }
        return rpBuildScript;
    }

    static boolean isBuildScriptUpToDate(@NonNull final Project project) {
        return false; //TODO
    }


    private static boolean configAlreadyUpdated(@NonNull final EditableProperties props) {
        for (Map.Entry<String,String> e : CONFIG_PROPERTIES.entrySet()) {
            if (!e.getValue().equals(props.get(e.getKey()))) {
                return false;
            }
        }
        return true;
    }

    private static void updateConfig(@NonNull final EditableProperties props) {
        for (Map.Entry<String,String> e : CONFIG_PROPERTIES.entrySet()) {
            props.setProperty(e.getKey(), e.getValue());
        }
    }

}
