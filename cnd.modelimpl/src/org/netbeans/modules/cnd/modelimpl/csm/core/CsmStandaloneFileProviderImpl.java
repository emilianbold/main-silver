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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmStandaloneFileProvider;
import org.netbeans.modules.cnd.api.project.DefaultSystemSettings;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeExitStatus;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.trace.NativeProjectProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Leonid Mesnik
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmStandaloneFileProvider.class)
public class CsmStandaloneFileProviderImpl extends CsmStandaloneFileProvider {

    private static final boolean TRACE = Boolean.getBoolean("cnd.standalone.trace"); //NOI18N
    private static final class Lock{}
    private static final Lock lock = new Lock();
    private static final Set<String> toBeRmoved = new HashSet<String>();

    private final CsmModelListener listener = new CsmModelListener() {

        @Override
        public void projectOpened(CsmProject project) {
        }

        @Override
        public void projectClosed(CsmProject project) {
        }

        @Override
        public void modelChanged(CsmChangeEvent e) {
            for (CsmFile file : e.getNewFiles()) {
                clean(file);
            }
            for (CsmFile file : e.getRemovedFiles()) {
                onFileRemove(file);
            }
        }
    };

    private final CsmProgressListener progressListener = new CsmProgressAdapter() {

        @Override
        public void projectLoaded(CsmProject project) {
            clean((ProjectBase) project);
        }

        @Override
        public void projectParsingFinished(CsmProject project) {
        }
    };

    public CsmStandaloneFileProviderImpl() {
        CsmListeners.getDefault().addModelListener(listener);
        CsmListeners.getDefault().addProgressListener(progressListener);
    }

    static CsmStandaloneFileProviderImpl getDefaultImpl() {
        return (CsmStandaloneFileProviderImpl) CsmStandaloneFileProvider.getDefault();
    }

    @Override
    public CsmFile getCsmFile(FileObject file) {
        CsmModelState modelState = CsmModelAccessor.getModelState();
        if (modelState != CsmModelState.ON) {
            if (TRACE) {
                trace("model is %s, no extra work for %s", modelState, file.getPath());  //NOI18N
            }
            return null;
        }
        String name = file.getPath();
        ProjectBase project = null;
        synchronized (this) {
            // findFile is expensive - don't call it twice!
            CsmFile csmFile = ModelImpl.instance().findFile(name, false);
            if (csmFile != null) {
                if (TRACE) {trace("returns file %s", csmFile);} //NOI18N
                return csmFile;
            }
            synchronized (lock) {
                if (toBeRmoved.contains(name)){
                    return null;
                }
                NativeProject platformProject = NativeProjectImpl.getNativeProjectImpl(file);
                if (platformProject != null) {
                    project = ModelImpl.instance().addProject(platformProject, name, true);
                    if (TRACE) {trace("added project %s", project.toString());} //NOI18N
                    project.ensureFilesCreated();
                }
            }
        }
        if (project != null && project.isValid()) {
            try {
                CsmFile out = project.getFile(file, false);
                if (TRACE) {trace("RETURNS STANALONE FILE %s", out);} //NOI18N
                return out;
            } catch (BufferUnderflowException ex) {
                // FIXUP: IZ#148840
                DiagnosticExceptoins.register(ex);
            } catch (IllegalStateException ex) {
                // project can be closed
                DiagnosticExceptoins.register(ex);
            }
        }
        return null;
    }

    private void clean(ProjectBase projectOpened) {
        if (projectOpened.getPlatformProject() instanceof NativeProjectImpl) {
            return;
        }
        if (TRACE) {trace("checking project %s", projectOpened.toString());} //NOI18N
        for (CsmProject dummy : ModelImpl.instance().projects()) {
            if (dummy.getPlatformProject() instanceof NativeProjectImpl) {
                for (CsmFile file : dummy.getAllFiles()) {
                    if (TRACE) {trace("\nchecking file %s", file.getAbsolutePath());} //NOI18N
                    if (projectOpened.getFile(((FileImpl) file).getFile(), false) != null) {
                        scheduleProjectRemoval(dummy);
                        continue;
                    }
                }
            }
        }
    }

    /** Is called when a file is added to model */
    private void clean(CsmFile file) {
        if (!(file.getProject().getPlatformProject() instanceof NativeProjectImpl)) {
            notifyClosed(file);
        }
    }

    void onFileRemove(CsmFile file) {
        FileObject fo = CsmUtilities.getFileObject(file);
        if (fo != null && isOpen(fo)) {
            this.getCsmFile(fo);
        }
    }

    private boolean isOpen(FileObject fo) {
        try {
            DataObject dao = DataObject.find(fo);
            if (dao != null) {
                EditorCookie editorCookie = dao.getCookie(EditorCookie.class);
                if (editorCookie != null) {
                    return CsmUtilities.findRecentEditorPaneInEQ(editorCookie) != null;
                }
            }
        } catch (DataObjectNotFoundException ex) {
            // we don't need to report this exception;
            // probably the file is just removed by user
        }
        return false;
    }

    @Override
    synchronized public void notifyClosed(CsmFile csmFile) {
        //if (TRACE) {trace("checking file %s", csmFile.toString());} //NOI18N
        String closedFilePath = csmFile.getAbsolutePath().toString();
        for (CsmProject csmProject : ModelImpl.instance().projects()) {
            Object platformProject = csmProject.getPlatformProject();
            if (platformProject instanceof NativeProjectImpl) {
                NativeProjectImpl nativeProject = (NativeProjectImpl) platformProject;
                if (nativeProject.getProjectRoot().equals(closedFilePath)) {
                    for (CsmFile csmf : csmProject.getAllFiles()) {
                        File f = ((FileImpl) csmf).getFile();
                        DataObject dao = NativeProjectProvider.getDataObject(f);
                        if (dao != null) {
                            NativeFileItemSet set = dao.getLookup().lookup(NativeFileItemSet.class);
                            if (set != null) {
                                set.remove(nativeProject.findFileItem(f));
                            }
                        }
                    }
                    scheduleProjectRemoval(csmProject);
                }
            }
        }
    }

    @Override
    public boolean isStandalone(CsmFile file) {
        if (file instanceof FileImpl) {
            NativeFileItem nfi = ((FileImpl) file).getNativeFileItem();
            if (nfi instanceof CsmStandaloneFileProviderImpl.NativeFileItemImpl) {
                return true;
            }
        }
        return false;
    }

    private void scheduleProjectRemoval(final CsmProject project) {
        final String root = ((NativeProject)project.getPlatformProject()).getProjectRoot();
        if (TRACE) {trace("schedulling removal %s", project.toString());} //NOI18N
        synchronized (lock) {
            toBeRmoved.add(root);
        }
        ModelImpl.instance().enqueueModelTask(new Runnable() {
            @Override
            public void run() {
                if (project.isValid()) {
                    if (TRACE) {trace("removing %s", project.toString());} //NOI18N
                    ProjectBase projectBase = (ProjectBase) project;
                    ModelImpl.instance().closeProjectBase(projectBase, false);
                    synchronized (lock) {
                        toBeRmoved.remove(root);
                    }
                    if (TRACE) {trace("removed %s", project.toString());} //NOI18N
                }
            }
        }, "Standalone project removal."); //NOI18N
    }

    private static void trace(String pattern, Object... args) {
        assert TRACE : "Should not be called if TRACE is off!"; //NOI18N
        System.err.printf("### Standalone provider:  %s\n", String.format(pattern, args)); //NOI18N
    }

    private static final class NativeProjectImpl implements NativeProject {

        private final List<String> sysIncludes;
        private final List<String> usrIncludes;
        private final List<String> sysMacros;
        private final List<String> usrMacros;
        private final List<NativeFileItemImpl> files = new ArrayList<NativeFileItemImpl>();
        private final String projectRoot;
        private boolean pathsRelCurFile;
        private List<NativeProjectItemsListener> listeners = new ArrayList<NativeProjectItemsListener>();
        private static final class Lock {}
        private final Object listenersLock = new Lock();

        static NativeProject getNativeProjectImpl(FileObject file) {
            DataObject dao = NativeProjectProvider.getDataObject(file);
            if (dao == null) {
                return null;
            }
            NativeFileItemSet set = dao.getLookup().lookup(NativeFileItemSet.class);
            if (set == null || !set.isEmpty()) {
                // it does not matter, what is there in the set! - see #185599, #185629
                return null;
            }
            CsmModel model = ModelImpl.instance();
            List<String> sysIncludes = new ArrayList<String>();
            List<String> usrIncludes = new ArrayList<String>();
            List<String> sysMacros = new ArrayList<String>();
            List<String> usrMacros = new ArrayList<String>();
            NativeFileItem.Language lang = NativeProjectProvider.getLanguage(file, dao);
            NativeProject prototype = null;
            for (CsmProject csmProject : model.projects()) {
                Object p = csmProject.getPlatformProject();
                if (p instanceof NativeProject) {
                    NativeProject project = (NativeProject)p;
                    if (file.getPath().startsWith(project.getProjectRoot())) {
                        prototype = project;
                        break;
                    }
                    for (String root : project.getSourceRoots()) {
                        if (file.getPath().startsWith(root)) {
                            prototype = project;
                            break;
                        }
                    }
                    if (prototype != null) {
                        break;
                    }
                }
            }

            if (prototype == null) {
                // Some default implementation should be provided.
                sysIncludes.addAll(DefaultSystemSettings.getDefault().getSystemIncludes(lang));
                sysMacros.addAll(DefaultSystemSettings.getDefault().getSystemMacros(lang));
            } else {
                if (ModelImpl.instance().isProjectDiabled(prototype)){
                    return null;
                }
                sysIncludes.addAll(prototype.getSystemIncludePaths());
                sysMacros.addAll(prototype.getSystemMacroDefinitions());
                usrIncludes.addAll(prototype.getUserIncludePaths());
                usrMacros.addAll(prototype.getUserMacroDefinitions());
            }
            NativeProjectImpl impl = new NativeProjectImpl(file, sysIncludes, usrIncludes, sysMacros, usrMacros);
            impl.addFile(file);
            set.add(impl.findFileItem(file));
            return impl;
        }

        private NativeProjectImpl(FileObject projectRoot,
                List<String> sysIncludes, List<String> usrIncludes,
                List<String> sysMacros, List<String> usrMacros) {

            this(projectRoot.getPath(), sysIncludes,
                    usrIncludes, sysMacros, usrMacros, false);
        }

        private NativeProjectImpl(String projectRoot,
                List<String> sysIncludes, List<String> usrIncludes,
                List<String> sysMacros, List<String> usrMacros,
                boolean pathsRelCurFile) {

            this.projectRoot = projectRoot;
            this.pathsRelCurFile = pathsRelCurFile;

            this.sysIncludes = createIncludes(sysIncludes);
            this.usrIncludes = createIncludes(usrIncludes);
            this.sysMacros = new ArrayList<String>(sysMacros);
            this.usrMacros = new ArrayList<String>(usrMacros);
        }

        private List<String> createIncludes(List<String> src) {
            if (pathsRelCurFile) {
                return new ArrayList<String>(src);
            } else {
                List<String> result = new ArrayList<String>(src.size());
                for (String path : src) {
                    File file = new File(path);
                    result.add(file.getAbsolutePath());
                }
                return result;
            }
        }

        private void addFile(FileObject file) {
            DataObject dobj = NativeProjectProvider.getDataObject(file);
            NativeFileItem.Language lang = NativeProjectProvider.getLanguage(file, dobj);
            NativeFileItemImpl item = new NativeFileItemImpl(file, this, lang);
            //TODO: put item in loockup of DataObject
            // registerItemInDataObject(dobj, item);
            this.files.add(item);
        }

        @Override
        public Object getProject() {
            return null;
        }

        @Override
        public List<String> getSourceRoots() {
            return Collections.<String>emptyList();
        }

        @Override
        public String getProjectRoot() {
            return this.projectRoot;
        }

        @Override
        public String getProjectDisplayName() {
            return getProjectRoot();
        }

        @Override
        public List<NativeFileItem> getAllFiles() {
            return Collections.<NativeFileItem>unmodifiableList(files);
        }

        @Override
        public void addProjectItemsListener(NativeProjectItemsListener listener) {
            synchronized (listenersLock) {
                listeners.add(listener);
            }
        }

        @Override
        public void removeProjectItemsListener(NativeProjectItemsListener listener) {
            synchronized (listenersLock) {
                listeners.remove(listener);
            }
        }

        @Override
        public NativeFileItemImpl findFileItem(FileObject fileObject) {
            String path = fileObject.getPath();
            for (NativeFileItemImpl item : files) {
                if (item.getFileObject().getPath().equals(path)) {
                    return item;
                }
            }
            return null;
        }

        @Override
        public NativeFileItem findFileItem(File file) {
            for (NativeFileItem item : files) {
                if (item.getFile().equals(file)) {
                    return item;
                }
            }
            return null;
        }

        @Override
        public NativeFileSearch getNativeFileSearch() {
            return new NativeFileSearch() {
                @Override
                public Collection<CharSequence> searchFile(NativeProject project, String fileName) {
                    return Collections.<CharSequence>emptyList();
                }
            };
        }

        @Override
        public List<String> getSystemIncludePaths() {
            return this.sysIncludes;
        }

        @Override
        public List<String> getUserIncludePaths() {
            return this.usrIncludes;
        }

        @Override
        public List<String> getSystemMacroDefinitions() {
            return this.sysMacros;
        }

        @Override
        public List<String> getUserMacroDefinitions() {
            return this.usrMacros;
        }

        @Override
        public List<NativeProject> getDependences() {
            return Collections.<NativeProject>emptyList();
        }

        @Override
        public void runOnProjectReadiness(NamedRunnable task) {
            task.run();
        }

        @Override
	public NativeExitStatus execute(String executable, String[] env, String... args) {
	    return null;
        }
        
        @Override
        public String getPlatformName() {
            return null;
        }

        @Override
        public final String toString() {
            return "SA " + projectRoot + ' ' + getClass().getName() + " @" + hashCode() + ":" + System.identityHashCode(this); // NOI18N
        }
    }

    private static final class NativeFileItemImpl implements NativeFileItem {

        private final FileObject file;
        private final NativeProjectImpl project;
        private final NativeFileItem.Language lang;

        public NativeFileItemImpl(FileObject file, NativeProjectImpl project, NativeFileItem.Language language) {

            this.project = project;
            this.file = file;
            this.lang = language;
        }

        @Override
        public NativeProject getNativeProject() {
            return project;
        }

        @Override
        public File getFile() {
            return FileUtil.toFile(file);
        }

        @Override
        public FileObject getFileObject() {
            return file;
        }

        @Override
        public List<String> getSystemIncludePaths() {
            List<String> result = project.getSystemIncludePaths();
            return project.pathsRelCurFile ? toAbsolute(result) : result;
        }

        @Override
        public List<String> getUserIncludePaths() {
            List<String> result = project.getUserIncludePaths();
            return project.pathsRelCurFile ? toAbsolute(result) : result;
        }

        private List<String> toAbsolute(List<String> orig) {
            FileObject base = file.getParent();
            List<String> result = new ArrayList<String>(orig.size());
            for (String path : orig) {
                if (CndPathUtilitities.isPathAbsolute(path)) {
                    result.add(path);
                } else {
                    String absPath = CndPathUtilitities.toAbsolutePath(base, path);
                    result.add(absPath);
                }
            }
            return result;
        }

        @Override
        public List<String> getSystemMacroDefinitions() {
            return project.getSystemMacroDefinitions();
        }

        @Override
        public List<String> getUserMacroDefinitions() {
            return project.getUserMacroDefinitions();
        }

        @Override
        public NativeFileItem.Language getLanguage() {
            return lang;
        }

        @Override
        public NativeFileItem.LanguageFlavor getLanguageFlavor() {
            return NativeFileItem.LanguageFlavor.GENERIC;
        }

        @Override
        public boolean isExcluded() {
            return false;
        }

        @Override
        public String toString() {
            return "SA " + file + " " + System.identityHashCode(this) + " " + lang + " from project:" + project; // NOI18N
        }
    }
}
