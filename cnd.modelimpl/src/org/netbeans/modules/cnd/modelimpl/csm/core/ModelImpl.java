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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.netbeans.modules.cnd.api.model.*;

import java.util.*;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.services.CsmStandaloneFileProvider;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.api.project.NativeProjectSettings;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheManager;
import org.netbeans.modules.cnd.apt.support.APTSystemStorage;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.netbeans.modules.cnd.utils.cache.TextCache;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.memory.LowMemoryEvent;
import org.netbeans.modules.cnd.modelimpl.memory.LowMemoryListener;
import org.netbeans.modules.cnd.modelimpl.memory.LowMemoryNotifier;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.FileNameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.ProjectNameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.UniqueNameCache;
import org.netbeans.modules.cnd.modelimpl.repository.KeyManager;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDManager;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * CsmModel implementation
 * @author Vladimir Kvashin
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.api.model.CsmModel.class)
public class ModelImpl implements CsmModel, LowMemoryListener {

    public ModelImpl() {
        startup();
    }

//    private void initThreasholds() {
//	String value, propertyName;
//	propertyName = "cnd.model.memory.warning.threashold"; // NOI18N
//	value = System.getProperty(propertyName);
//	if( value != null ) {
//	    try {
//		warningThreshold = Double.parseDouble(value);
//	    }
//	    catch(NumberFormatException e) {
//		Utils.LOG.severe("Incorrect format for property " + propertyName + ": " + value); // NOI18N
//		DiagnosticExceptoins.register(e);
//	    }
//	}
////	propertyName = "cnd.model.memory.fatal.threashold";
////	value = System.getProperty(propertyName);
////	if( value != null ) {
////	    try {
////		fatalThreshold = Double.parseDouble(value);
////	    }
////	    catch(NumberFormatException e) {
////		Utils.LOG.severe("Incorrect format for property " + propertyName + ": " + value);
////	    }
////	}
//    }
    public CsmProject findProject(Object id) {
        ProjectBase prj = null;
        if (id != null) {
            synchronized (lock) {
                prj = obj2Project(id);
            }
        }
        return prj;
    }

    private ProjectBase obj2Project(Object obj) {
        CsmUID<CsmProject> prjUID = platf2csm.get(obj);
        ProjectBase prj = (ProjectBase) UIDCsmConverter.UIDtoProject(prjUID);
        assert prj != null || prjUID == null : "null object for UID " + prjUID;
        return prj;
    }

    @Override
    public CsmProject getProject(Object id) {
        if (id instanceof Project) {
            NativeProject prj = ((Project) id).getLookup().lookup(NativeProject.class);
            if (prj != null) {
                id = prj;
            }
        }
        return findProject(id);
    }

    /*package*/final CsmProject _getProject(Object id) {
        ProjectBase prj = null;
        if (id != null) {
            synchronized (lock) {
                prj = obj2Project(id);
                if (prj == null && id instanceof Project) {
                    // for compatibility 
                    if (TraceFlags.DEBUG) {
                        System.err.println("getProject called with Project... expected NativeProject");
                        new Throwable().printStackTrace(System.err);
                    }
                    id = ((Project) id).getLookup().lookup(NativeProject.class);
                    prj = id != null ? obj2Project(id) : null;
                }
                if (prj == null) {
                    if (disabledProjects.contains(id)) {
                        return null;
                    }
                    String name;
                    if (id instanceof NativeProject) {
                        name = ((NativeProject) id).getProjectDisplayName();
                    } else {
                        new IllegalStateException("CsmProject does not exist: " + id).printStackTrace(System.err); // NOI18N
                        name = "<unnamed>"; // NOI18N
                    }
                    prj = ProjectImpl.createInstance(this, (NativeProject) id, name);
                    putProject2Map(id, prj);
                }
            }
        }
        return prj;
    }

    public ProjectBase addProject(NativeProject id, String name, boolean enableModel) {
        ProjectBase prj = null;
        assert (id != null) : "The platform project is null"; // NOI18N
        if (enableModel && !disabledProjects.contains(id)) {
            boolean fireOpened = false;
            synchronized (lock) {
                if (state != CsmModelState.ON) {
                    if (TraceFlags.TRACE_MODEL_STATE) {
                        System.err.println("project " + name + " wasn't added because model is " + state + "\n\t" + id);
                    }
                    return null;
                }
                prj = obj2Project(id);
                if (prj == null) {
                    prj = ProjectImpl.createInstance(this, id, name);
                    putProject2Map(id, prj);
                    fireOpened = true;
                } else {
                    String expectedUniqueName = ProjectBase.getUniqueName(id).toString();
                    String defactoUniqueName = prj.getUniqueName().toString();
                    if (!defactoUniqueName.equals(expectedUniqueName)) {
                        new IllegalStateException("Existing project unique name differ: " + defactoUniqueName + " - expected " + expectedUniqueName).printStackTrace(System.err); // NOI18N
                    }
                }
            }
            if (fireOpened) {
                ListenersImpl.getImpl().fireProjectOpened(prj);
            }
        } else {
            disabledProjects.add(id);
        }
        return prj;
    }

    // for testing purposes only
    public ProjectBase addProject(ProjectBase prj) {
        synchronized (lock) {
            Object id = prj.getPlatformProject();
            if (obj2Project(id) != null) {
                new IllegalStateException("CsmProject already exists: " + id).printStackTrace(System.err); // NOI18N
                return null;
            }
            putProject2Map(id, prj);
        }
        ListenersImpl.getImpl().fireProjectOpened(prj);
        return prj;
    }

    private void putProject2Map(final Object id, final ProjectBase prj) {
        CsmUID<CsmProject> uid = UIDCsmConverter.projectToUID(prj);
        if (TraceFlags.TRACE_CPU_CPP) {System.err.println("putting [" + platf2csm.size() + "] prj@" + System.identityHashCode(prj) + " with UID@" + System.identityHashCode(uid) + uid);}
        platf2csm.put(id, uid);
    }

    public void closeProject(Object platformProject) {
        _closeProject(null, platformProject, !TraceFlags.PERSISTENT_REPOSITORY);
    }

    public void closeProject(Object platformProject, boolean cleanRepository) {
        _closeProject(null, platformProject, cleanRepository);
    }

    public void closeProjectBase(ProjectBase prj) {
        _closeProject(prj, prj.getPlatformProject(), !TraceFlags.PERSISTENT_REPOSITORY);
    }

    public void closeProjectBase(ProjectBase prj, boolean cleanRepository) {
        _closeProject(prj, prj.getPlatformProject(), cleanRepository);
    }

    private void _closeProject(final ProjectBase csmProject, final Object platformProjectKey, final boolean cleanRepository) {
        _closeProject2_pre(csmProject, platformProjectKey);
        if (SwingUtilities.isEventDispatchThread()) {
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    _closeProject2(csmProject, platformProjectKey, cleanRepository);
                }
            };
            this.enqueueModelTask(task, "Closing Project "); // NOI18N
        } else {
            _closeProject2(csmProject, platformProjectKey, cleanRepository);
        }
    }

    private void _closeProject2_pre(ProjectBase csmProject, Object platformProjectKey) {
        ProjectBase prj = (csmProject == null) ? (ProjectBase) getProject(platformProjectKey) : csmProject;
        if (prj != null) {
            prj.setDisposed();
        }
    }

    private void _closeProject2(ProjectBase csmProject, Object platformProjectKey, boolean cleanRepository) {
        ProjectBase prj = csmProject;
        boolean cleanModel = false;
        synchronized (lock) {
            CsmUID<CsmProject> uid = platf2csm.remove(platformProjectKey);
            if (uid != null) {
                prj = (prj == null) ? (ProjectBase) UIDCsmConverter.UIDtoProject(uid) : prj;
                assert prj != null : "null object for UID " + uid;
            }
            cleanModel = (platf2csm.isEmpty());
        }

        if (prj != null) {
            disposeProject(prj, cleanRepository);
            if (!prj.isArtificial()) {
                LibraryManager.getInstance().onProjectClose(prj.getUID());
            }
        }

        if (cleanModel) {
            // time to clean caches
            cleanCaches();
        }
    }

    /*package-local*/ void disposeProject(final ProjectBase prj) {
        disposeProject(prj, !TraceFlags.PERSISTENT_REPOSITORY);
    }

    /*package-local*/ void disposeProject(final ProjectBase prj, boolean cleanRepository) {
        assert prj != null;
        if (prj != null) {
            CharSequence name = prj.getName();
            if (TraceFlags.TRACE_CLOSE_PROJECT) {
                System.err.println("dispose project " + name);
            }
            prj.setDisposed();
            ListenersImpl.getImpl().fireProjectClosed(prj);
            ParserThreadManager.instance().waitEmptyProjectQueue(prj);
            prj.dispose(cleanRepository);
            if (TraceFlags.TRACE_CLOSE_PROJECT) {
                System.err.println("project closed " + name);
            }
        }
    }

    @Override
    public Collection<CsmProject> projects() {
        Collection<CsmUID<CsmProject>> vals;
        synchronized (lock) {
            vals = new ArrayList<CsmUID<CsmProject>>(platf2csm.values());
        }
        Collection<CsmProject> out = new ArrayList<CsmProject>(vals.size());
        for (CsmUID<CsmProject> uid : vals) {
            ProjectBase prj = (ProjectBase) UIDCsmConverter.UIDtoProject(uid);
            assert prj != null : "null project for UID " + uid;
            if (prj != null) {
                out.add(prj);
            }
        }
        return out;
    }
    // used only in a couple of functions below
    private final String clientTaskPrefix = "Code Model Client Request"; // NOI18N
    private static final String modelTaskPrefix = "Code Model Request Processor"; // NOI18N

    @Override
    public Cancellable enqueue(Runnable task, CharSequence name) {
        return enqueue(userTasksProcessor, task, clientTaskPrefix + " :" + name); // NOI18N
    }

    public static ModelImpl instance() {
        return (ModelImpl) CsmModelAccessor.getModel();
    }

    public Cancellable enqueueModelTask(Runnable task, String name) {
        return enqueue(modelProcessor, task, modelTaskPrefix + ": " + name); // NOI18N
    }

    private Cancellable enqueue(RequestProcessor processor, final Runnable task, final String taskName) {
        if (TraceFlags.TRACE_182342_BUG) {
            new Exception(taskName).printStackTrace(System.err);
        }
        return processor.post(new Runnable() {

            @Override
            public void run() {
                String oldName = Thread.currentThread().getName();
                Thread.currentThread().setName(taskName); // NOI18N
                try {
                    task.run();
                } catch (Throwable thr) {
                    DiagnosticExceptoins.register(thr);
                } finally {
                    Thread.currentThread().setName(oldName);
                }
            }
        });
    }

    @Override
    public CsmFile findFile(CharSequence absPath, boolean createIfPossible, boolean snapShot) {
        CndUtils.assertAbsolutePathInConsole(absPath.toString());
        Collection<CsmProject> projects = projects();
        CsmFile ret = null;
        for (CsmProject curPrj : projects) {
            if (curPrj instanceof ProjectBase) {
                ProjectBase ownerPrj = ((ProjectBase) curPrj).findFileProject(absPath, createIfPossible);
                if (ownerPrj != null) {
                    CsmFile csmFile = ownerPrj.findFile(absPath, createIfPossible, snapShot);
                    if (csmFile != null) {
                        ret = csmFile;
                        if (!CsmStandaloneFileProvider.getDefault().isStandalone(csmFile)) {
                            return ret;
                        }
                    }
                }
            }
        }
        // try the same with canonical path
        String canonical;
        try {
            canonical = new File(absPath.toString()).getCanonicalPath();
        } catch (IOException ex) {
            canonical = null;
        }
        if (canonical != null && !canonical.equals(absPath)) {
            CsmFile out = findFile(canonical, true, snapShot);
            if (out != null) {
                ret = out;
            }
        }
        return ret;
    }

    @Override
    public CsmModelState getState() {
        return state;
    }

    public final void startup() {

        if (TraceFlags.TRACE_MODEL_STATE) {
            System.err.println("ModelImpl.startup");
        }

        ModelSupport.instance().setModel(this);

        setState(CsmModelState.ON);

        if (TraceFlags.CHECK_MEMORY && warningThreshold > 0) {
            LowMemoryNotifier.instance().addListener(this);
            LowMemoryNotifier.instance().setThresholdPercentage(warningThreshold);
        }

        ParserThreadManager.instance().startup(CndUtils.isStandalone());
        RepositoryUtils.startup();
    //if( ! isStandalone() ) {
    //    for( NativeProject nativeProject : ModelSupport.instance().getNativeProjects() ) {
    //    	addProject(nativeProject, nativeProject.getProjectDisplayName());
    //    }
    //}
    }

    public void shutdown() {

        if (TraceFlags.TRACE_MODEL_STATE) {
            System.err.println("ModelImpl.shutdown");
        }
        setState(CsmModelState.CLOSING);

        // it's now done in ProjectBase.setDisposed()
        // unregisterProjectListeners();

        ParserThreadManager.instance().shutdown();

        if (TraceFlags.CHECK_MEMORY) {
            LowMemoryNotifier.instance().removeListener(this);
        }

        Collection<CsmProject> prjsColl;
        Collection<CsmProject> libs = new HashSet<CsmProject>();

        synchronized (lock) {
            prjsColl = projects();
            platf2csm.clear();
        }

        // clearFileExistenceCache all opened projects, UIDs will be removed in disposeProject
        for (Iterator<CsmProject> projIter = prjsColl.iterator(); projIter.hasNext();) {
            ProjectBase project = (ProjectBase) projIter.next();
            libs.addAll(project.getLibraries());
            disposeProject(project);
        }
        for (Iterator<CsmProject> projIter = libs.iterator(); projIter.hasNext();) {
            disposeProject((ProjectBase) projIter.next());
        }
        LibraryManager.getInstance().shutdown();

        cleanCaches();

        setState(CsmModelState.OFF);

        RepositoryUtils.shutdown();

        ModelSupport.instance().setModel(null);
    }

    @Override
    public void memoryLow(final LowMemoryEvent event) {

        double percentage = ((double) event.getUsedMemory() / (double) event.getMaxMemory());

        final boolean warning = percentage >= warningThreshold && projects().size() > 0;

//	final boolean fatal = percentage >= fatalThreshold && projects().size() > 0;
//	
//	if( fatal ) {
//	    LowMemoryNotifier.instance().removeListener(this);
//	}
//	else {
//	    LowMemoryNotifier.instance().setThresholdPercentage(fatalThreshold);
//	}

        Runnable runner = new Runnable() {

            @Override
            public void run() {
                Thread.currentThread().setName("Code model low memory handler"); // NOI18N
//		if( fatal ) {
//		    ParserThreadManager.instance().shutdown();
//		    ModelSupport.instance().onMemoryLow(event, true);
//		}
//		else {
                ModelSupport.instance().onMemoryLow(event, false);
//		}
            }
        };
        // I have to use Thread directly here (instead of Request processor)
        // for the following reasons:
        // 1) I have to return control very fast
        // 2) if I use RequestProcessor, I can't be sure the thread will be launched -
        // what if we already reached the limit for this RequestProcessor?
        new Thread(runner).start();

    }

    private void setState(CsmModelState newState) {
        if (TraceFlags.TRACE_MODEL_STATE) {
            System.err.println("  ModelImpl.setState " + state + " -> " + newState);
        }
        if (newState != state) {
            CsmModelState oldState = state;
            state = newState;
            ListenersImpl.getImpl().fireModelStateChanged(newState, oldState);
        }
    }

    public void suspend() {
        if (TraceFlags.TRACE_MODEL_STATE) {
            System.err.println("ModelImpl.suspend");
        }
        setState(CsmModelState.SUSPENDED);
        ParserQueue.instance().suspend();
    }

    public void resume() {
        if (TraceFlags.TRACE_MODEL_STATE) {
            System.err.println("ModelImpl.resume");
        }
        setState(CsmModelState.ON);
        ParserQueue.instance().resume();
    }

    public void disableProject(NativeProject nativeProject) {
        if (TraceFlags.TRACE_MODEL_STATE) {
            System.err.println("ModelImpl.disableProject " + nativeProject.getProjectDisplayName());
        }
        synchronized (lock) {
            disabledProjects.add(nativeProject);
        }
        ProjectBase csmProject = (ProjectBase) findProject(nativeProject);
        if (csmProject != null) {
            disableProject2(csmProject);
        }
    }

    public void disableProject(ProjectBase csmProject) {
        if (TraceFlags.TRACE_MODEL_STATE) {
            System.err.println("ModelImpl.disableProject " + csmProject);
        }
        if (csmProject != null) {
            synchronized (lock) {
                disabledProjects.add(csmProject.getPlatformProject());
            }
            disableProject2(csmProject);
        }
    }

    private void disableProject2(final ProjectBase csmProject) {
        csmProject.setDisposed();
        Lookup.Provider project = findProjectByNativeProject(ModelSupport.getNativeProject(csmProject.getPlatformProject()));
        setCodeAssistanceEnabled(project, false);
        // that's a caller's responsibility to launch disabling in a separate thread
        disableProject3(csmProject);
    }

    private void disableProject3(ProjectBase csmProject) {
        if (TraceFlags.TRACE_MODEL_STATE) {
            System.err.println("ModelImpl.disableProject3");
        }
        suspend();
        try {
            while (ParserQueue.instance().isParsing(csmProject)) {
                try {
                    if (TraceFlags.TRACE_MODEL_STATE) {
                        System.err.println("ModelImpl.disableProject3: waiting for current parse...");
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }

            closeProjectBase(csmProject);
            boolean cleanModel;
            synchronized (lock) {
                cleanModel = platf2csm.isEmpty();
            }
            // repository memory cache isn't cleared here,
            // so it's safe to do this outside of the lock
            if (cleanModel) {
                cleanCaches();
            }
        } finally {
            resume();
        }
    }

//    /**
//     * Checks whether there are only library projects.
//     * If yes, returns the set of remaining library projects.
//     * Otherwise returns null
//     */
//    private Set<LibProjectImpl> getLastLibs() {
//	Collection/*<CsmProjects>*/ projects = projects();
//	Set<LibProjectImpl> lastLibs = new HashSet<LibProjectImpl>(projects.size());
//	for (Iterator it = projects.iterator(); it.hasNext();) {
//	    Object e = it.next();
//	    if( e instanceof LibProjectImpl ) {
//		lastLibs.add((LibProjectImpl) e);
//	    }
//	    else {
//		return null;
//	    }
//	}
//	return lastLibs;
//    }
    /** Enables/disables code model for the particular project */
    public void enableProject(NativeProject nativeProject) {
        if (TraceFlags.TRACE_MODEL_STATE) {
            System.err.println("ModelImpl.enableProject " + nativeProject.getProjectDisplayName());
        }
        synchronized (lock) {
            disabledProjects.remove(nativeProject);
        }
        Lookup.Provider project = findProjectByNativeProject(nativeProject);
        setCodeAssistanceEnabled(project, true);
        addProject(nativeProject, nativeProject.getProjectDisplayName(), Boolean.TRUE);
    //ProjectBase csmProject = (ProjectBase) _getProject(nativeProject);
    //fireProjectOpened(csmProject);
    //new CodeAssistanceOptions(findProjectByNativeProject(nativeProject)).setCodeAssistanceEnabled(Boolean.TRUE);
    }

    private void setCodeAssistanceEnabled(Lookup.Provider project, boolean enable) {
        if (project != null) {
            NativeProjectSettings settings = project.getLookup().lookup(NativeProjectSettings.class);
            if (settings != null) {
                settings.setCodeAssistanceEnabled(enable);
            }
        }
    }
    
    public static Lookup.Provider findProjectByNativeProject(NativeProject nativeProjectToSearch) {
        for(NativeProject nativeProject : NativeProjectRegistry.getDefault().getOpenProjects()) {
            if (nativeProject == nativeProjectToSearch) {
                return nativeProject.getProject();
            }
        }
        return null;
    }

    public boolean isProjectEnabled(NativeProject nativeProject) {
        ProjectBase project = (ProjectBase) findProject(nativeProject);
        return (project != null) && (!project.isDisposing());
    }

    public boolean isProjectDiabled(NativeProject id) {
        return disabledProjects.contains(id);
    }

    private void cleanCaches() {
        TextCache.getManager().dispose();
        FilePathCache.getManager().dispose();
        QualifiedNameCache.getManager().dispose();
        NameCache.getManager().dispose();
        UniqueNameCache.getManager().dispose();
        FileNameCache.getManager().dispose();
        ProjectNameCache.getManager().dispose();
        APTDriver.close();
        APTFileCacheManager.close();
        UIDManager.instance().dispose();
        KeyManager.instance().dispose();
        CndFileUtils.clearFileExistenceCache();
        APTSystemStorage.dispose();
    }

    @Override
    public void scheduleReparse(Collection<CsmProject> projects) {
        CndFileUtils.clearFileExistenceCache();
        ParserQueue.instance().clearParseWatch();
        Collection<LibProjectImpl> libs = new HashSet<LibProjectImpl>();
        Collection<ProjectBase> toReparse = new HashSet<ProjectBase>();
        for (CsmProject csmProject : projects) {
            if (csmProject instanceof ProjectBase) {
                ProjectBase project = (ProjectBase) csmProject;
                toReparse.add(project);
                for (CsmProject csmLib : project.getLibraries()) {
                    if (csmLib instanceof LibProjectImpl) {
                        LibProjectImpl lib = (LibProjectImpl) csmLib;
                        if (!libs.contains(lib)) {
                            libs.add(lib);
                        }
                    }
                }
            }
        }
        for(CsmProject csmProject : projects()) {
            if (!projects.contains(csmProject)) {
                if (csmProject instanceof ProjectBase) {
                    ProjectBase project = (ProjectBase) csmProject;
                    for (CsmProject csmLib : project.getLibraries()) {
                        if (csmLib instanceof LibProjectImpl) {
                            libs.remove((LibProjectImpl)csmLib);
                        }
                    }
                }
            }
        }
        for (LibProjectImpl lib : libs) {
            lib.initFields();
        }
        Collection<Object> platformProjects = new ArrayList<Object>();
        for (ProjectBase projectBase : toReparse) {
            final Object platformProject = projectBase.getPlatformProject();
            platformProjects.add(platformProject);
            closeProject(platformProject, true);
        }
        for (LibProjectImpl lib : libs) {
            closeProject(lib.getPlatformProject(), true);
        }
        LibraryManager.getInstance().cleanLibrariesData(libs);
        for (Object platformProject : platformProjects) {
            ProjectBase newPrj = (ProjectBase) _getProject(platformProject);
            if (newPrj != null) { // VK: at least once I've got NPE here: might be already closed? 
                newPrj.scheduleReparse();
                ListenersImpl.getImpl().fireProjectOpened(newPrj);
            }
        }
    }

    private static final class Lock {}
    private final Object lock = new Lock();
    /** maps platform project to project */
    private final Map<Object, CsmUID<CsmProject>> platf2csm = new HashMap<Object, CsmUID<CsmProject>>();
    private CsmModelState state;
    private double warningThreshold = 0.98;
    //private double fatalThreshold = 0.99;
    private final Set<Object> disabledProjects = new HashSet<Object>();
    private final RequestProcessor modelProcessor = new RequestProcessor("Code model request processor", 1); // NOI18N
    private final RequestProcessor userTasksProcessor = new RequestProcessor("User model tasks processor", 4); // NOI18N
    
    /////////// 
    // tracing
    public void dumpInfo(PrintWriter printOut, boolean withContainers) {
        printOut.printf("ModelImpl: disabled=%d, projects=%d\n", disabledProjects.size(), platf2csm.size());// NOI18N
        int ind = 1;
        for (Object prj : disabledProjects) {
            printOut.printf("D[%d] %s\n", ind++, prj);// NOI18N
        }
        ind=1;
        for (Map.Entry<Object, CsmUID<CsmProject>> entry : platf2csm.entrySet()) {
            final Object key = entry.getKey();
            CsmUID<CsmProject> value = entry.getValue();
            printOut.printf("[%d] key=[%s] %s\n\tprj=(%d)%s\n", ind, key.getClass().getSimpleName(), key, System.identityHashCode(value), value);// NOI18N
            CsmProject prj = UIDCsmConverter.UIDtoProject(value);
            if (prj == null) {
                printOut.printf("Project was NOT restored from repository\n");// NOI18N
            } else if (prj instanceof ProjectBase) {
                printOut.printf("[%d] disposing?=%s\n", ind, ((ProjectBase)prj).isDisposing());// NOI18N
                Collection<CsmProject> libraries = prj.getLibraries();
                int libInd = 0;
                for (CsmProject lib : libraries) {
                    printOut.printf("\tlib[%d]=%s\n", ++libInd, lib);// NOI18N
                }
                Object platformProject = prj.getPlatformProject();
                printOut.printf("platformProjec=%s\n", platformProject);// NOI18N
                if (platformProject instanceof NativeProject) {
                    NativeProject np = (NativeProject) platformProject;
                    List<NativeProject> dependences = np.getDependences();
                    libInd = 0;
                    for (NativeProject nativeLib : dependences) {
                        printOut.printf("\tnativeLib[%d]=%s\n", ++libInd, nativeLib);// NOI18N
                    }
                }
                if (withContainers) {
                    ((ProjectBase) prj).traceFileContainer(printOut);
                }
            } else {
                printOut.printf("Project has unexpected class type %s\n", prj.getClass().getName());// NOI18N
            }
            ind++;
        }
    }
}
