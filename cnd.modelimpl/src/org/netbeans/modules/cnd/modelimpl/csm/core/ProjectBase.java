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

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.NameAcceptor;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTFileSearch;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTIncludePathStorage;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler.State;
import org.netbeans.modules.cnd.apt.support.APTSystemStorage;
import org.netbeans.modules.cnd.apt.support.APTWalker;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.apt.support.PostIncludeData;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.modelimpl.cache.impl.WeakContainer;
import org.netbeans.modules.cnd.modelimpl.content.project.ClassifierContainer;
import org.netbeans.modules.cnd.modelimpl.content.project.DeclarationContainerProject;
import org.netbeans.modules.cnd.modelimpl.content.project.FileContainer;
import org.netbeans.modules.cnd.modelimpl.content.project.FileContainer.FileEntry;
import org.netbeans.modules.cnd.modelimpl.content.project.GraphContainer;
import org.netbeans.modules.cnd.modelimpl.content.project.IncludedFileContainer;
import org.netbeans.modules.cnd.modelimpl.content.project.IncludedFileContainer.Storage;
import org.netbeans.modules.cnd.modelimpl.content.project.ProjectComponent;
import org.netbeans.modules.cnd.modelimpl.csm.ClassEnumBase;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImplEx;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.Terminator;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.impl.services.FileInfoQueryImpl;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTRestorePreprocStateWalker;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.repository.ClassifierContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.FileContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.GraphContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.ProjectDeclarationContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.ProjectNameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.trace.TraceUtils;
import org.netbeans.modules.cnd.modelimpl.uid.LazyCsmCollection;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDManager;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Cancellable;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Base class for CsmProject implementation
 * @author Dmitry Ivanov
 * @author Vladimir Kvashin
 */
public abstract class ProjectBase implements CsmProject, Persistent, SelfPersistent, CsmIdentifiable, 
        CndFileSystemProvider.CndFileSystemProblemListener {
    
    /** Creates a new instance of CsmProjectImpl */
    protected ProjectBase(ModelImpl model, FileSystem fs, Object platformProject, String name) {
        namespaces = new ConcurrentHashMap<CharSequence, CsmUID<CsmNamespace>>();
        this.uniqueName = getUniqueName(fs, platformProject);
        RepositoryUtils.openUnit(createProjectKey(fs, platformProject));
        setStatus(Status.Initial);
        this.name = ProjectNameCache.getManager().getString(name);
        this.fileSystem = fs;
        init(model, platformProject);
        sysAPTData = APTSystemStorage.getInstance();
        userPathStorage = new APTIncludePathStorage();
        declarationsSorageKey = new ProjectDeclarationContainerKey(getUniqueName());
        weakDeclarationContainer = new WeakContainer<DeclarationContainerProject>(this, declarationsSorageKey);
        classifierStorageKey = new ClassifierContainerKey(getUniqueName());
        weakClassifierContainer = new WeakContainer<ClassifierContainer>(this, classifierStorageKey);
        fileContainerKey = new FileContainerKey(getUniqueName());
        weakFileContainer = new WeakContainer<FileContainer>(this, fileContainerKey);
        graphStorageKey = new GraphContainerKey(getUniqueName());
        weakGraphContainer = new WeakContainer<GraphContainer>(this, graphStorageKey);
        includedFileContainer = new IncludedFileContainer(this);
        initFields();
    }

    /*package*/final void initFields() {
        NamespaceImpl ns = NamespaceImpl.create(this, false);
        assert ns != null;
        this.globalNamespaceUID = UIDCsmConverter.namespaceToUID(ns);
        DeclarationContainerProject declarationContainer = new DeclarationContainerProject(this);
        CndUtils.assertTrue(declarationsSorageKey.equals(declarationContainer.getKey()));
        weakDeclarationContainer.clear();
        ClassifierContainer classifierContainer = new ClassifierContainer(this);
        CndUtils.assertTrue(classifierStorageKey.equals(classifierContainer.getKey()));
        weakClassifierContainer.clear();
        FileContainer fileContainer = new FileContainer(this);
        CndUtils.assertTrue(fileContainerKey.equals(fileContainer.getKey()));
        weakFileContainer.clear();
        GraphContainer graphContainer = new GraphContainer(this);
        CndUtils.assertTrue(graphStorageKey.equals(graphContainer.getKey()));
        weakGraphContainer.clear();
        FAKE_GLOBAL_NAMESPACE = NamespaceImpl.create(this, true);
    }

    private void init(ModelImpl model, Object platformProject) {
        this.model = model;
        this.platformProject = platformProject;
        // remember in repository
        RepositoryUtils.hang(this);
        // create global namespace

        if (TraceFlags.CLOSE_AFTER_PARSE) {
            Terminator.create(this);
        }
    }

    private boolean checkConsistency() {
        long time = TraceFlags.TIMING ? System.currentTimeMillis() : 0;
        if (getFileContainer() == FileContainer.empty()) {
            return false;
        }
        if (getDeclarationsSorage() == DeclarationContainerProject.empty()) {
            return false;
        }
        if (getGraph() == GraphContainer.empty()) {
            return false;
        }
        if (getGlobalNamespace() == FAKE_GLOBAL_NAMESPACE) {
            return false;
        }
        if (TraceFlags.TIMING) {
            System.err.printf("Consistency check took %d ms\n", System.currentTimeMillis() - time);
        }
        return true;
    }

    private void setStatus(Status newStatus) {
        //System.err.printf("CHANGING STATUS %s -> %s for %s (%s)\n", status, newStatus, name, getClass().getName());
        status = newStatus;
    }

    protected static void cleanRepository(FileSystem fs, Object platformProject, boolean articicial) {
        Key key = createProjectKey(fs, platformProject);
        RepositoryUtils.closeUnit(key, null, true);
    }

    private static Key createProjectKey(FileSystem fs, Object platfProj) {
        return KeyUtilities.createProjectKey(getUniqueName(fs, platfProj));
    }

    protected static ProjectBase readInstance(ModelImpl model, FileSystem fs, Object platformProject, String name) {

        long time = 0;
        if (TraceFlags.TIMING) {
            System.err.printf("Project %s: instantiating...\n", name);
            time = System.currentTimeMillis();
        }

        assert TraceFlags.PERSISTENT_REPOSITORY;
        Key key = createProjectKey(fs, platformProject);
        RepositoryUtils.openUnit(key);
        Persistent o = RepositoryUtils.get(key);
        if (o != null) {
            assert o instanceof ProjectBase;
            ProjectBase impl = (ProjectBase) o;
            CharSequence aName = ProjectNameCache.getManager().getString(name);
            if (!impl.name.equals(aName)) {
                impl.setName(aName);
            }
            impl.init(model, platformProject);
            if (TraceFlags.TIMING) {
                time = System.currentTimeMillis() - time;
                System.err.printf("Project %s: loaded. %d ms\n", name, time);
            }
            UIDManager.instance().clearProjectCache(key);
            if (impl.checkConsistency()) {
                return impl;
            }
        }
        return null;
    }

    @Override
    public final CsmNamespace getGlobalNamespace() {
        return _getGlobalNamespace();
    }

    @Override
    public final CharSequence getName() {
        return name;
    }

    protected final void setName(CharSequence name) {
        this.name = name;
    }

    /**
     * Returns a string that uniquely identifies this project.
     * One should never rely on this name structure,
     * just use it as in unique identifier
     */
    public final CharSequence getUniqueName() {
        return uniqueName;
    }

    public static CharSequence getUniqueName(NativeProject platformProject) {
        return getUniqueName(platformProject.getFileSystem(), platformProject);
    }

    @Override
    public String getDisplayName() {
        if (CndFileUtils.isLocalFileSystem(fileSystem)) {
            return name.toString();
        } else {
            return NbBundle.getMessage(getClass(), "ProjectDisplayName", name, fileSystem.getDisplayName());
        }
    }

    @Override
    public String getHtmlDisplayName() {
        if (CndFileUtils.isLocalFileSystem(fileSystem)) {
            return name.toString();
        } else {
            return NbBundle.getMessage(getClass(), "ProjectHtmlDisplayName", name, fileSystem.getDisplayName());
        }
    }

    
    public static CharSequence getUniqueName(FileSystem fs, Object platformProject) {
        Parameters.notNull("FileSystem", fs); //NOI18N
        String postfix = CndFileUtils.isLocalFileSystem(fs) ? "" : fs.getDisplayName();
        String result;
        if (platformProject instanceof NativeProject) {
            result = ((NativeProject) platformProject).getProjectRoot() + 'N' + postfix;
        } else if (platformProject instanceof CharSequence) {
            result = ((CharSequence)platformProject).toString() + 'L' + postfix;
        } else if (platformProject == null) {
            throw new IllegalArgumentException("Incorrect platform project: null"); // NOI18N
        } else {
            throw new IllegalArgumentException("Incorrect platform project class: " + platformProject.getClass()); // NOI18N
        }
        return ProjectNameCache.getManager().getString(result);
    }
    
    /** Gets an object, which represents correspondent IDE project */
    @Override
    public final Object getPlatformProject() {
        return platformProject;
    }

    /** Gets an object, which represents correspondent IDE project */
    protected final void setPlatformProject(Object platformProject) {
        CndUtils.assertTrue(this.platformProject == null);
        CndUtils.assertNotNull(platformProject, "Passing null project for ", this);
        this.platformProject = platformProject;
        CndUtils.assertTrue(this.uniqueName.equals(getUniqueName(fileSystem, platformProject)));
    }

    /** Finds namespace by its qualified name */
    public final CsmNamespace findNamespace(CharSequence qualifiedName, boolean findInLibraries) {
        CsmNamespace result = findNamespace(qualifiedName);
        if (result == null && findInLibraries) {
            for (Iterator<CsmProject> it = getLibraries().iterator(); it.hasNext();) {
                CsmProject lib = it.next();
                result = lib.findNamespace(qualifiedName);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    /** Finds namespace by its qualified name */
    @Override
    public final CsmNamespace findNamespace(CharSequence qualifiedName) {
        CsmNamespace nsp = _getNamespace(qualifiedName);
        return nsp;
    }

    private static String getNestedNamespaceQualifiedName(CharSequence name, NamespaceImpl parent, boolean createForEmptyNames) {
        StringBuilder sb = new StringBuilder(name);
        if (parent != null) {
            if (name.length() == 0 && createForEmptyNames) {
                sb.append(parent.getNameForUnnamedElement());
            }
            if (!parent.isGlobal()) {
                sb.insert(0, "::"); // NOI18N
                sb.insert(0, parent.getQualifiedName());
            }
        }
        return sb.toString();
    }

    public final NamespaceImpl findNamespaceCreateIfNeeded(NamespaceImpl parent, CharSequence name) {
        synchronized (namespaceLock) {
            String qualifiedName = ProjectBase.getNestedNamespaceQualifiedName(name, parent, true);
            NamespaceImpl nsp = _getNamespace(qualifiedName);
            if (nsp == null) {
                nsp = NamespaceImpl.create(this, parent, name.toString(), qualifiedName);
            }
            return nsp;
        }
    }

    public final void registerNamespace(NamespaceImpl namespace) {
        _registerNamespace(namespace);
    }

    public final void unregisterNamesace(NamespaceImpl namespace) {
        _unregisterNamespace(namespace);
    }

    public final CsmClassifier findClassifier(CharSequence qualifiedName, boolean findInLibraries) {
        CsmClassifier result = findClassifier(qualifiedName);
        if (result == null && findInLibraries) {
            for (Iterator<CsmProject> it = getLibraries().iterator(); it.hasNext();) {
                CsmProject lib = it.next();
                result = lib.findClassifier(qualifiedName);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public final CsmClassifier findClassifier(CharSequence qualifiedName) {
        CsmClassifier result = getClassifierSorage().getClassifier(qualifiedName);
        return result;
    }

    @Override
    public final Collection<CsmClassifier> findClassifiers(CharSequence qualifiedName) {
        CsmClassifier result = getClassifierSorage().getClassifier(qualifiedName);
        Collection<CsmClassifier> out = new ArrayList<CsmClassifier>();
        //Collection<CsmClassifier> out = new LazyCsmCollection<CsmClassifier, CsmClassifier>(new ArrayList<CsmUID<CsmClassifier>>(), TraceFlags.SAFE_UID_ACCESS);
        if (result != null) {
            if (CsmKindUtilities.isBuiltIn(result)) {
                return Collections.<CsmClassifier>singletonList(result);
            }
            CharSequence[] allClassifiersUniqueNames = Utils.getAllClassifiersUniqueNames(result.getUniqueName());
            Collection<CsmClassifier> fwds = new ArrayList<CsmClassifier>(1);
            for (CharSequence curUniqueName : allClassifiersUniqueNames) {
                Collection<? extends CsmDeclaration> decls = this.findDeclarations(curUniqueName);
                @SuppressWarnings("unchecked")
                Collection<CsmClassifier> classifiers = (Collection<CsmClassifier>) decls;
                for (CsmClassifier csmClassifier : classifiers) {
                    if (ForwardClass.isForwardClass(csmClassifier)) {
                        fwds.add(csmClassifier);
                    } else {
                        out.add(csmClassifier);
                    }
                }
            }
            // All forwards move at the end
            out.addAll(fwds);
        }
        return out;
    }

    @Override
    public final Collection<CsmInheritance> findInheritances(CharSequence name) {
        return getClassifierSorage().getInheritances(name);
    }

    @Override
    public final CsmDeclaration findDeclaration(CharSequence uniqueName) {
        return getDeclarationsSorage().getDeclaration(uniqueName);
    }

    @Override
    public final Collection<CsmOffsetableDeclaration> findDeclarations(CharSequence uniqueName) {
        return getDeclarationsSorage().findDeclarations(uniqueName);
    }

    public final Collection<CsmOffsetableDeclaration> findDeclarationsByPrefix(String uniquNamePrefix) {
        // To improve performance use char(255) instead real Character.MAX_VALUE
        char maxChar = 255; //Character.MAX_VALUE;
        return getDeclarationsSorage().getDeclarationsRange(uniquNamePrefix, uniquNamePrefix + maxChar); // NOI18N
    }

    public final Collection<CsmFriend> findFriendDeclarations(CsmOffsetableDeclaration decl) {
        return getDeclarationsSorage().findFriends(decl);
    }

    public final boolean registerDeclaration(CsmOffsetableDeclaration decl) {

        if (!Utils.canRegisterDeclaration(decl)) {
            if (TraceFlags.TRACE_REGISTRATION) {
                traceRegistration("not registered decl " + decl + " UID " + UIDs.get(decl)); //NOI18N
            }
            return false;
        }

        if (CsmKindUtilities.isClass(decl) || CsmKindUtilities.isEnum(decl)) {

            ClassEnumBase<?> cls = (ClassEnumBase<?>) decl;
            CharSequence qname = cls.getQualifiedName();

            synchronized (classifierReplaceLock) {
                CsmClassifier old = getClassifierSorage().getClassifier(qname);
                if (old != null) {
                    // don't register if the new one is weaker
                    if (cls.shouldBeReplaced(old)) {
                        if (TraceFlags.TRACE_REGISTRATION) {
                            traceRegistration("not registered decl " + decl + " UID " + UIDs.get(decl)); //NOI18N
                        }
                        return false;
                    }
                    // remove the old one if the new one is stronger
                    if ((old instanceof ClassEnumBase<?>) && ((ClassEnumBase<?>) old).shouldBeReplaced(cls)) {
                        if (TraceFlags.TRACE_REGISTRATION) {
                            System.err.println("disposing old decl " + old + " UID " + UIDs.get(decl)); //NOI18N
                        }
                        ((ClassEnumBase<?>) old).dispose();
                    }
                }
                getDeclarationsSorage().putDeclaration(decl);
                getClassifierSorage().putClassifier((CsmClassifier) decl);
            }

        } else if (CsmKindUtilities.isTypedef(decl)) { // isClassifier(decl) or isTypedef(decl) ??
            getDeclarationsSorage().putDeclaration(decl);
            getClassifierSorage().putClassifier((CsmClassifier) decl);
        } else {
            // only classes, enums and typedefs are registered as classifiers;
            // even if you implement CsmClassifier, this doesn't mean you atomatically get there ;)
            getDeclarationsSorage().putDeclaration(decl);
        }

        if (TraceFlags.TRACE_REGISTRATION) {
            System.err.println("registered " + decl + " UID " + UIDs.get(decl)); //NOI18N
        }
        return true;
    }

    public final void unregisterDeclaration(CsmOffsetableDeclaration decl) {
        if (TraceFlags.TRACE_REGISTRATION) {
            traceRegistration("unregistered " + decl + " UID " + UIDs.get(decl)); //NOI18N
        }
        if (decl instanceof CsmClassifier) {
            getClassifierSorage().removeClassifier(decl);
        }
        getDeclarationsSorage().removeDeclaration(decl);
    }

    private static void traceRegistration(String text) {
        assert TraceFlags.TRACE_REGISTRATION : "TraceFlags.TRACE_REGISTRATION should be checked *before* call !"; //NOI18N
        System.err.printf("registration: %s\n", text);
    }

    @Override
    public final void waitParse() {
        boolean insideParser = ParserThreadManager.instance().isParserThread();
        if (insideParser) {
            new Throwable("project.waitParse should NEVER be called in parser thread !!!").printStackTrace(System.err); // NOI18N
        }
        if (insideParser) {
            return;
        }
        ensureFilesCreated();
        ensureChangedFilesEnqueued();
        model.waitModelTasks();
        waitParseImpl();
    }

    private void waitParseImpl() {
        synchronized (waitParseLock) {
            while (ParserQueue.instance().hasPendingProjectRelatedWork(this, null)) {
                try {
                    //FIXUP - timeout is a workaround for #146436 hang on running unit tests
                    waitParseLock.wait(10000);
                } catch (InterruptedException ex) {
                    // do nothing
                }
            }
        }
    }

    protected void ensureChangedFilesEnqueued() {
    }

    /**
     * @param skipFile if null => check all files, otherwise skip checking
     * this file
     *
     */
    protected boolean hasChangedFiles(CsmFile skipFile) {
        return false;
    }

    protected boolean hasEditedFiles() {
        return false;
    }

    private Set<FileSystem> getIncludesFileSystems(NativeProject nativeProject) {
        Set<FileSystem> fileSystems = new HashSet<FileSystem>();
        for (FSPath fsPath : nativeProject.getSystemIncludePaths()) {
            fileSystems.add(fsPath.getFileSystem());
        }
        return fileSystems;
    }
    

    @Override
    public void problemOccurred(FSPath fsPath) {
        synchronized (fileSystemProblemsLock) {
            hasFileSystemProblems = true;
        }
    }

    @Override
    public void recovered(FileSystem fileSystem) {
        boolean prev;
        synchronized (fileSystemProblemsLock) {
            prev = hasFileSystemProblems;
            hasFileSystemProblems = false;
        }
        if (prev) {
            ModelImpl.instance().scheduleReparse(Collections.<CsmProject>singleton(this));
        }
    }    

    public final void enableProjectListeners(boolean enable) {
        synchronized (projectListenerLock) {
            if (projectListener != null) {
                projectListener.enableListening(enable);
            }
        }
    }

    protected final void registerProjectListeners() {
        synchronized (projectListenerLock) {
            if (platformProject instanceof NativeProject) {
                if (projectListener == null) {
                    projectListener = new NativeProjectListenerImpl(getModel(), (NativeProject) platformProject, this);
                }
                NativeProject nativeProject = (NativeProject) platformProject;
                nativeProject.addProjectItemsListener(projectListener);
                for (FileSystem fs : getIncludesFileSystems(nativeProject)) {
                    CndFileSystemProvider.addFileSystemProblemListener(this, fs);
                }
            }
        }
    }

    protected final void unregisterProjectListeners() {
        synchronized (projectListenerLock) {
            if (projectListener != null) {
                if (platformProject instanceof NativeProject) {
                    NativeProject nativeProject = (NativeProject) platformProject;
                    nativeProject.removeProjectItemsListener(projectListener);
                    for (FileSystem fs : getIncludesFileSystems(nativeProject)) {
                        CndFileSystemProvider.removeFileSystemProblemListener(this, fs);
                    }
                }
            }
        }
    }

    /*package*/ final void scheduleReparse() {
        ensureFilesCreated();
        DeepReparsingUtils.reparseOnEdit(this.getAllFileImpls(), this, true);
    }

    private final Object fileCreateLock = new Object();
    
    protected void ensureFilesCreated() {
        if (status == Status.Ready) {
            return;
        }
        boolean notify = false;
        try {
            synchronized (fileCreateLock) {
                if (status == Status.Initial || status == Status.Restored) {
                    try {
                        setStatus((status == Status.Initial) ? Status.AddingFiles : Status.Validating);
                        long time = 0;
                        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
                            System.err.println("suspend queue");
                            ParserQueue.instance().suspend();
                            if (TraceFlags.TIMING) {
                                time = System.currentTimeMillis();
                            }
                        }
                        ParserQueue.instance().onStartAddingProjectFiles(this);
                        notify = true;
                        registerProjectListeners();
                        NativeProject nativeProject = ModelSupport.getNativeProject(platformProject);
                        if (nativeProject != null) {
                            try {
                                ParserQueue.instance().suspend();
                                createProjectFilesIfNeed(nativeProject);
                            } finally {
                                ParserQueue.instance().resume();
                            }
                        }
                        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
                            if (TraceFlags.TIMING) {
                                time = System.currentTimeMillis() - time;
                                System.err.println("getting files from project system + put in queue took " + time + "ms");
                            }
                            System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec before resuming queue");
                            sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
                        }
                    } finally {
                        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
                            System.err.println("woke up after sleep");
                            ParserQueue.instance().resume();
                        }
                        setStatus(Status.Ready);
                    }
                }
            }
        } finally {
            if (notify) {
                ParserQueue.instance().onEndAddingProjectFiles(this);
            }
        }
    }
    
    private void sleep(int millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException ex) {
            // do nothing
        }
    }

    private void createProjectFilesIfNeed(NativeProject nativeProject) {

        if (TraceFlags.TIMING) {
            System.err.printf("\n\nGetting files from project system for %s...\n", getName());
        }
        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
            try {
                System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec before getting files from project");
                Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
                System.err.println("woke up after sleep");
            } catch (InterruptedException ex) {
                // do nothing
            }
        }
        long time = System.currentTimeMillis();
        final Set<NativeFileItem> removedFileItems = Collections.synchronizedSet(new HashSet<NativeFileItem>());
        final Set<NativeFileItem> readOnlyRemovedFilesSet = Collections.unmodifiableSet(removedFileItems);
        NativeProjectItemsListener projectItemListener = new NativeProjectItemsListener() {

            @Override
            public void fileAdded(NativeFileItem fileItem) {
            }

            @Override
            public void filesAdded(List<NativeFileItem> fileItems) {
            }

            @Override
            public void fileRemoved(NativeFileItem fileItem) {
                removedFileItems.add(fileItem);
            }

            @Override
            public void filesRemoved(List<NativeFileItem> fileItems) {
                removedFileItems.addAll(fileItems);
            }

            @Override
            public void fileRenamed(String oldPath, NativeFileItem newFileIetm) {
            }

            @Override
            public void filePropertiesChanged(NativeFileItem fileItem) {
            }

            @Override
            public void filesPropertiesChanged(List<NativeFileItem> fileItems) {
            }

            @Override
            public void filesPropertiesChanged() {
            }

            @Override
            public void projectDeleted(NativeProject nativeProject) {
            }
        };
        nativeProject.addProjectItemsListener(projectItemListener);
        List<NativeFileItem> sources = new ArrayList<NativeFileItem>();
        List<NativeFileItem> headers = new ArrayList<NativeFileItem>();
        List<NativeFileItem> excluded = new ArrayList<NativeFileItem>();
        for (NativeFileItem item : nativeProject.getAllFiles()) {
            if (!item.isExcluded()) {
                switch (item.getLanguage()) {
                    case C:
                    case CPP:
                    case FORTRAN:
                        sources.add(item);
                        break;
                    case C_HEADER:
                        headers.add(item);
                        break;
                    default:
                        break;
                }
            } else {
                switch (item.getLanguage()) {
                    case C:
                    case CPP:
                    case C_HEADER:
                    case FORTRAN:
                        excluded.add(item);
                        break;
                    default:
                        break;
                }
            }
        }

        if (TraceFlags.TIMING) {
            time = System.currentTimeMillis() - time;
            System.err.printf("Getting files from project system took  %d ms for %s\n", time, getName());
            System.err.printf("FILES COUNT for %s:\nSource files:\t%d\nHeader files:\t%d\nTotal files:\t%d\n",
                    getName(), sources.size(), headers.size(), sources.size() + headers.size());
            time = System.currentTimeMillis();
        }
        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
            try {
                System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec after getting files from project");
                Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
                System.err.println("woke up after sleep");
            } catch (InterruptedException ex) {
                // do nothing
            }
        }
        if (TraceFlags.DUMP_PROJECT_ON_OPEN) {
            ModelSupport.dumpNativeProject(nativeProject);
        }

        try {
            disposeLock.readLock().lock();

            if (TraceFlags.TIMING) {
                time = System.currentTimeMillis() - time;
                System.err.printf("Waited on disposeLock: %d ms for %s\n", time, getName());
                time = System.currentTimeMillis();
            }

            if (isDisposing()) {
                if (TraceFlags.TRACE_MODEL_STATE) {
                    System.err.printf("filling parser queue interrupted for %s\n", getName());
                }
                return;
            }

            ProjectSettingsValidator validator = null;
            if (status == Status.Validating) {
                validator = new ProjectSettingsValidator(this);
                validator.restoreSettings();
            }
            if (status == Status.Validating && RepositoryUtils.getRepositoryErrorCount(this) > 0){
                System.err.println("Clean index for project \""+getUniqueName()+"\" because index was corrupted (was "+RepositoryUtils.getRepositoryErrorCount(this)+" errors)."); // NOI18N
                validator = null;
                reopenUnit();
            }

            getProjectRoots().fixFolder(nativeProject.getProjectRoot());
            for (String root : nativeProject.getSourceRoots()) {
                getProjectRoots().fixFolder(root);
            }
            getProjectRoots().addSources(sources);
            getProjectRoots().addSources(headers);
            getProjectRoots().addSources(excluded);
            for(NativeFileItem nativeFileItem : excluded) {
                FileImpl file = getFile(nativeFileItem.getAbsolutePath(), true);
                if (file != null) {
                    removeFile(nativeFileItem.getAbsolutePath());
                }
            }
            CreateFilesWorker worker = new CreateFilesWorker(this);
            worker.createProjectFilesIfNeed(sources, true, readOnlyRemovedFilesSet, validator);
            if (status != Status.Validating  || RepositoryUtils.getRepositoryErrorCount(this) == 0){
                worker.createProjectFilesIfNeed(headers, false, readOnlyRemovedFilesSet, validator);
            }
            if (status == Status.Validating && RepositoryUtils.getRepositoryErrorCount(this) > 0){
                if (!TraceFlags.DEBUG_BROKEN_REPOSITORY) {
                    System.err.println("Clean index for project \""+getUniqueName()+"\" because index was corrupted (was "+RepositoryUtils.getRepositoryErrorCount(this)+" errors)."); // NOI18N
                }
                validator = null;
                reopenUnit();
                worker.createProjectFilesIfNeed(sources, true, readOnlyRemovedFilesSet, validator);
                worker.createProjectFilesIfNeed(headers, false, readOnlyRemovedFilesSet, validator);
            }
            if (validator != null && false) {
                // update all opened libraries using our storages associated with libs
                for (CsmProject lib : this.getLibraries()) {
                    ProjectBase libProject = (ProjectBase)lib;
                    libProject.mergeFileContainerFromStorage(this);
                }
            }
        } finally {
            disposeLock.readLock().unlock();
            if (TraceFlags.TIMING) {
                time = System.currentTimeMillis() - time;
                System.err.printf("FILLING PARSER QUEUE took %d ms for %s\n", time, getName());
            }
        }
        nativeProject.removeProjectItemsListener(projectItemListener);
    // in fact if visitor used for parsing => visitor will parse all included files
    // recursively starting from current source file
    // so, when we visit headers, they should not be reparsed if already were parsed
    }

    private void reopenUnit() {
        setStatus(Status.Initial);
        ParserQueue.instance().clean(this);
        RepositoryUtils.closeUnit(this.getUniqueName(), null, true);
        RepositoryUtils.openUnit(this);
        RepositoryUtils.hang(this);
        initFields();
    }

    /**
     * Creates FileImpl instance for the given file item if it hasn't yet been created.
     * Is called when initializing the project or new file is added to project.
     * Isn't intended to be used in #included file processing.
     */
    final protected void createIfNeed(NativeFileItem nativeFile, boolean isSourceFile) {
        FileAndHandler fileAndHandler = preCreateIfNeed(nativeFile, isSourceFile);
        if (fileAndHandler == null) {
            return;
        }
        // put directly into parser queue if needed
        ParserQueue.instance().add(fileAndHandler.fileImpl, fileAndHandler.preprocHandler.getState(), ParserQueue.Position.TAIL);
    }

    private FileAndHandler preCreateIfNeed(NativeFileItem nativeFile, boolean isSourceFile){
        // file object can be invalid for not existing file (#194357)
        assert (nativeFile != null && nativeFile.getFileObject() != null);
        if (!Utils.acceptNativeItem(nativeFile)) {
            return null;
        }
        FileImpl.FileType fileType = isSourceFile ? Utils.getFileType(nativeFile) : FileImpl.FileType.HEADER_FILE;

        FileAndHandler fileAndHandler = createOrFindFileImpl(ModelSupport.createFileBuffer(nativeFile.getFileObject()), nativeFile, fileType);

        if (fileAndHandler.preprocHandler == null) {
            fileAndHandler.preprocHandler = createPreprocHandler(nativeFile);
        }
        return fileAndHandler;
    }

    final FileImpl createIfNeed(NativeFileItem nativeFile, boolean isSourceFile, FileModel lwm,
            ProjectSettingsValidator validator, List<FileImpl> reparseOnEdit, List<NativeFileItem> reparseOnPropertyChanged) {

        FileAndHandler fileAndHandler = preCreateIfNeed(nativeFile, isSourceFile);
        if (fileAndHandler == null) {
            return null;
        }
        if (validator != null) {
            // fill up needed collections based on validation
            if (fileAndHandler.fileImpl.validate()) {
                if (fileAndHandler.fileImpl.isParsed()){
                    if (validator.arePropertiesChanged(nativeFile)) {
                        if (TraceFlags.TRACE_VALIDATION) {
                            System.err.printf("Validation: %s properties are changed \n", nativeFile.getAbsolutePath());
                        }
                        reparseOnPropertyChanged.add(nativeFile);
                    } else {
                        if (TraceFlags.TRACE_VALIDATION) {
                            System.err.printf("Validation: %s file is skipped as valid PARSED\n", nativeFile.getAbsolutePath());
                        }                        
                    }
                } else {
                    if (TraceFlags.TRACE_VALIDATION) {
                        System.err.printf("Validation: %s file to be parsed, because of state %s\n", nativeFile.getAbsolutePath(), fileAndHandler.fileImpl.getState());
                    }
                    if (validator.arePropertiesChanged(nativeFile)) {
                        if (fileAndHandler.fileImpl.getState() == FileImpl.State.INITIAL){
                            fileAndHandler.preprocHandler = createPreprocHandler(nativeFile);
                            ParserQueue.instance().add(fileAndHandler.fileImpl, fileAndHandler.preprocHandler.getState(), ParserQueue.Position.TAIL);
                        } else {
                            if (TraceFlags.TRACE_VALIDATION) {
                                System.err.printf("Validation: %s properties are changed \n", nativeFile.getAbsolutePath());
                            }
                            reparseOnPropertyChanged.add(nativeFile);
                        }
                    } else {
                        ParserQueue.instance().add(fileAndHandler.fileImpl, fileAndHandler.preprocHandler.getState(), ParserQueue.Position.TAIL);
                    }
                }
            } else {
                if (TraceFlags.TRACE_VALIDATION) {
                    System.err.printf("Validation: file %s is changed\n", nativeFile.getAbsolutePath());
                }
                if (validator.arePropertiesChanged(nativeFile)) {
                    reparseOnPropertyChanged.add(nativeFile);
                } else {
                    reparseOnEdit.add(fileAndHandler.fileImpl);
                }
            }
        } else {
            // put directly into parser queue if needed
            if (lwm == null || !lwm.fill(fileAndHandler.fileImpl)){
                ParserQueue.instance().add(fileAndHandler.fileImpl, fileAndHandler.preprocHandler.getState(), ParserQueue.Position.TAIL);
            }
        }
        return fileAndHandler.fileImpl;
    }

    /**
     * Is called after project is added to model
     * and all listeners are notified
     */
    public final void onAddedToModel() {
        final boolean isRestored = status == Status.Restored;
        //System.err.printf("onAddedToModel isRestored=%b status=%s for %s (%s) \n", isRestored, status, name, getClass().getName());
        if (status == Status.Initial || status == Status.Restored) {
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    onAddedToModelImpl(isRestored);
                    synchronized (initializationTaskLock) {
                        initializationTask = null;
                    }
                }
            };
            String text = (status == Status.Initial) ? "Filling parser queue for " : "Validating files for ";	// NOI18N
            synchronized (initializationTaskLock) {
                initializationTask = ModelImpl.instance().enqueueModelTask(r, text + getName());
            }
        }
    }

    protected final Status getStatus() {
        return status;
    }

    boolean isValidating(){
        return status == Status.Validating;
    }

    private void onAddedToModelImpl(boolean isRestored) {
        if (TraceFlags.TRACE_182342_BUG) {
            new Exception("Restored: " + isRestored + " disposing: " + isDisposing()).printStackTrace(System.err); // NOI18N
        }
        if (isDisposing()) {
            return;
        }

        try {
            disposeLock.readLock().lock();
            if (isDisposing()) {
                return;
            }

            ensureFilesCreated();
            if (isDisposing()) {
                return;
            }

            // changed files are enqueued by edit start/edit end handlers
//            ensureChangedFilesEnqueued();
//            if (isDisposing()) {
//                return;
//            }
            Notificator.instance().flush();
        } finally {
            disposeLock.readLock().unlock();
        }

        if (isRestored) {
            FileImpl.incParseCount();
            ProgressSupport.instance().fireProjectLoaded(ProjectBase.this);
        }

        try {
            disposeLock.readLock().lock();
            if (isRestored && !isDisposing()) {
                // FIXUP for #109105 fix the reason instead!
                try {
                    // TODO: refactor this - remove waiting here!
                    // It was introduced in version 1.2.2.27.2.94.4.41
                    // when validation was introduced
                    waitParseImpl();
                    checkForRemoved();
                } catch (Exception e) {
                    DiagnosticExceptoins.register(e);
                }
            }
            if (isDisposing()) {
                return;
            }
            Notificator.instance().flush();
        } finally {
            disposeLock.readLock().unlock();
        }
    }

    /**
     * For the project that is restored from persistence,
     * is called when 1-st time parsed.
     * Checks whether there are files in code model, that are removed from the project system
     */
    private void checkForRemoved() {

        NativeProject nativeProject = (platformProject instanceof NativeProject) ? (NativeProject) platformProject : null;

        // we might just ask NativeProject to find file,
        // but it's too ineffective; so we have to create a set of project files paths
        Set<String> projectFiles = null;
        if (nativeProject != null) {
            projectFiles = new HashSet<String>();
            for (NativeFileItem item : nativeProject.getAllFiles()) {
                if (!item.isExcluded()) {
                    switch (item.getLanguage()) {
                        case C:
                        case CPP:
                        case FORTRAN:
                        case C_HEADER:
                            projectFiles.add(item.getAbsolutePath());
                            //this would be a workaround for #116706 Code assistance do not recognize changes in file
                            //projectFiles.add(item.getFile().getCanonicalPath());
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        Set<FileImpl> candidates = new HashSet<FileImpl>();
        Set<FileImpl> removedPhysically = new HashSet<FileImpl>();
        for (FileImpl file : getAllFileImpls()) {
            FileObject fo = file.getFileObject();
            if (fo == null || !fo.isValid()) {
                removedPhysically.add(file);
            } else if (projectFiles != null) { // they might be null for library
                if (!projectFiles.contains(file.getAbsolutePath().toString())) {
                    candidates.add(file);
                }
            }
        }
        final ArrayList<FileImpl> removedFiles = new ArrayList<FileImpl>(removedPhysically);
        if (TraceFlags.TRACE_VALIDATION) {
            for (FileImpl file : removedPhysically) {
                System.err.printf("Validation: removing (physically deleted) %s\n", file.getAbsolutePath()); //NOI18N
            }
        }
        for (FileImpl file : candidates) {
            boolean remove = true;
            Set<CsmFile> parents = getParentFiles(file);
            for (CsmFile parent : parents) {
                if (!candidates.contains((FileImpl)parent)) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                if (TraceFlags.TRACE_VALIDATION) {
                    System.err.printf("Validation: removing (removed from project) %s\n", file.getAbsolutePath());//NOI18N
                }
                removedFiles.add(file);
            }
        }
        onFileImplRemoved(removedFiles);
    }

    protected final APTPreprocHandler createEmptyPreprocHandler(CharSequence absPath) {
        StartEntry startEntry = new StartEntry(getFileSystem(), FileContainer.getFileKey(absPath, true).toString(),
                RepositoryUtils.UIDtoKey(getUID()));
        return APTHandlersSupport.createEmptyPreprocHandler(startEntry);
    }

    protected final APTPreprocHandler createPreprocHandler(NativeFileItem nativeFile) {
        assert (nativeFile != null);
        APTMacroMap macroMap = getMacroMap(nativeFile);
        APTIncludeHandler inclHandler = getIncludeHandler(nativeFile);
        APTPreprocHandler preprocHandler = APTHandlersSupport.createPreprocHandler(macroMap, inclHandler, isSourceFile(nativeFile));
        return preprocHandler;
    }

    private APTIncludeHandler getIncludeHandler(NativeFileItem nativeFile) {
        if (!isSourceFile(nativeFile)) {
            nativeFile = DefaultFileItem.toDefault(nativeFile);
        }
        List<FSPath> origUserIncludePaths = nativeFile.getUserIncludePaths();
        if (TraceFlags.DUMP_NATIVE_FILE_ITEM_USER_INCLUDE_PATHS) {
            System.err.println("Item "+nativeFile.getAbsolutePath());
            for(FSPath path : origUserIncludePaths) {
                System.err.println("\tPath "+path.getPath());
            }
        }
        List<FSPath> origSysIncludePaths = nativeFile.getSystemIncludePaths();
        List<IncludeDirEntry> userIncludePaths = userPathStorage.get(origUserIncludePaths.toString(), origUserIncludePaths);
        List<IncludeDirEntry> sysIncludePaths = sysAPTData.getIncludes(origSysIncludePaths.toString(), origSysIncludePaths);
        String entryKey = FileContainer.getFileKey(nativeFile.getAbsolutePath(), true).toString();
        if (CndUtils.isDebugMode()) {
            FileSystem curPrjFS = getFileSystem();
            FileSystem nativeProjectFS = nativeFile.getNativeProject().getFileSystem();
            CndUtils.assertTrue(nativeProjectFS.equals(curPrjFS), "File systems differ: incoming=" + nativeProjectFS + ";cur=" + curPrjFS); //NOI18N
        }
        StartEntry startEntry = new StartEntry(getFileSystem(), entryKey, 
                RepositoryUtils.UIDtoKey(getUID()));
        APTFileSearch searcher = null;
        Object aPlatformProject = getPlatformProject();
        if (aPlatformProject != null){
            searcher = APTFileSearch.get(KeyUtilities.createProjectKey(ProjectBase.getUniqueName(fileSystem, aPlatformProject)));
        }
        return APTHandlersSupport.createIncludeHandler(startEntry, sysIncludePaths, userIncludePaths, searcher);
    }

    private APTMacroMap getMacroMap(NativeFileItem nativeFile) {
        if (!isSourceFile(nativeFile)) {
            nativeFile = DefaultFileItem.toDefault(nativeFile);
        }
        List<String> userMacros = nativeFile.getUserMacroDefinitions();
        List<String> sysMacros = nativeFile.getSystemMacroDefinitions();
        APTMacroMap map = APTHandlersSupport.createMacroMap(getSysMacroMap(sysMacros), userMacros);
        return map;
    }

    protected final boolean isSourceFile(NativeFileItem nativeFile) {
        FileImpl.FileType type = Utils.getFileType(nativeFile);
        return FileImpl.isSourceFileType(type);
    }

    private APTMacroMap getSysMacroMap(List<String> sysMacros) {
        //TODO: it's faster to use sysAPTData.getMacroMap(configID, sysMacros);
        // but we need this ID to get somehow... how?
        APTMacroMap map = sysAPTData.getMacroMap(sysMacros.toString(), sysMacros);
        return map;
    }

    /*package*/ final APTPreprocHandler getPreprocHandler(CharSequence absPath, PreprocessorStatePair statePair) {
        assert statePair != null;
        return createPreprocHandlerFromState(absPath, statePair.state);
    }

    /* package */ final APTPreprocHandler createPreprocHandlerFromState(CharSequence absPath, APTPreprocHandler.State state) {
        Collection<APTPreprocHandler> out = createPreprocHandlerFromStates(Collections.singleton(state), absPath);
        return out.iterator().next();
    }

    public final APTPreprocHandler.State getFirstValidPreprocState(CharSequence fileKey) {
        FileContainer.FileEntry entry = getFileContainer().getEntry(fileKey);
        if (entry == null) {
            return null;
        }
        Object lock = entry.getLock();
        synchronized (lock) {
            for (PreprocessorStatePair pair : entry.getStatePairs()) {
                StartEntry startEntry = APTHandlersSupport.extractStartEntry(pair.state);
                if (!ModelImpl.isClosedProject(startEntry.getStartFileProject())) {
                    return pair.state;
                }
            }
            Collection<FileEntry> includedFileEntries = getIncludedFileEntries(lock, fileKey);
            for (FileEntry fileEntry : includedFileEntries) {
                // return the first with non empty states collection
                for (State state : fileEntry.getPrerocStates()) {
                    return state;
                }
            }
        }
        return null;
    }

    /*package-local*/ final Collection<PreprocessorStatePair> getPreprocessorStatePairs(FileImpl fileImpl) {
        CharSequence fileKey = FileContainer.getFileKey(fileImpl.getAbsolutePath(), false);
        FileContainer.FileEntry entry = getFileContainer().getEntry(fileKey);
        if (entry == null) {
            return Collections.emptyList();
        }
        Object lock = entry.getLock();
        Collection<PreprocessorStatePair> out;
        synchronized (lock) {
            Collection<PreprocessorStatePair> containerStatePairs = entry.getStatePairs();
            if (hasClosedStartEntry(lock, containerStatePairs)) {
                // need to merge from dependent projects' storages
                Collection<FileEntry> includedFileEntries = getIncludedFileEntries(lock, fileKey);
                FileEntry mergeEntry = FileContainer.createFileEntryForMerge(fileKey);
                for (FileEntry fileEntry : includedFileEntries) {
                    for (PreprocessorStatePair pair : fileEntry.getStatePairs()) {
                        if (pair.pcState != FilePreprocessorConditionState.PARSING) {
                            updateFileEntryBasedOnIncludedStatePair(mergeEntry, pair, fileKey, fileImpl, null, null);
                        }
                    }
                }
                out = mergeEntry.getStatePairs();
            } else {
                out = containerStatePairs;
            }
        }
        return out;
    }

    public final Collection<APTPreprocHandler> getPreprocHandlers(FileImpl fileImpl) {
        CharSequence fileKey = FileContainer.getFileKey(fileImpl.getAbsolutePath(), false);
        FileContainer.FileEntry entry = getFileContainer().getEntry(fileKey);
        if (entry == null) {
            return Collections.emptyList();
        }

        Collection<APTPreprocHandler.State> states;
        Object lock = entry.getLock();
        synchronized (lock) {
            Collection<PreprocessorStatePair> containerStatePairs = entry.getStatePairs();
            if (hasClosedStartEntry(lock, containerStatePairs)) {
                // need to merge from dependent projects' storages
                Collection<FileEntry> includedFileEntries = getIncludedFileEntries(lock, fileKey);
                FileEntry mergeEntry = FileContainer.createFileEntryForMerge(fileKey);
                for (FileEntry fileEntry : includedFileEntries) {
                    for (PreprocessorStatePair pair : fileEntry.getStatePairs()) {
                        if (pair.pcState != FilePreprocessorConditionState.PARSING) {
                            updateFileEntryBasedOnIncludedStatePair(mergeEntry, pair, fileKey, fileImpl, null, null);
                        }
                    }
                }
                states = mergeEntry.getPrerocStates();
            } else {
                states = entry.getPrerocStates();
            }
        }
        Collection<APTPreprocHandler> result = createPreprocHandlerFromStates(states, fileKey);
        return result;
    }

    private boolean hasClosedStartEntry(Object lock, Collection<PreprocessorStatePair> containerStatePairs) {
        assert Thread.holdsLock(lock) : " must hold lock ";
        for (PreprocessorStatePair pair : containerStatePairs) {
            StartEntry startEntry = APTHandlersSupport.extractStartEntry(pair.state);
            if (ModelImpl.isClosedProject(startEntry.getStartFileProject())) {
                return true;
            }
        }
        return false;
    }

    /*package-local*/ final Collection<PreprocessorStatePair> getFileContainerStatePairsToDump(CharSequence absPath) {
        FileContainer.FileEntry entry = getFileContainer().getEntry(absPath);
        if (entry == null) {
            return Collections.emptyList();
        }
        synchronized (entry.getLock()) {
            return entry.getStatePairs();
        }
    }

    public final Collection<APTPreprocHandler> getFileContainerPreprocHandlersToDump(CharSequence absPath) {
        FileContainer.FileEntry entry = getFileContainer().getEntry(absPath);
        if (entry == null) {
            return Collections.emptyList();
        }

        Collection<APTPreprocHandler.State> states;
        synchronized (entry.getLock()) {
            states = entry.getPrerocStates();
        }
        return createPreprocHandlerFromStates(states, absPath);
    }

    private Collection<APTPreprocHandler> createPreprocHandlerFromStates(Collection<State> states, CharSequence absPath) {
        Collection<APTPreprocHandler> result = new ArrayList<APTPreprocHandler>(states.size());
        for (APTPreprocHandler.State state : states) {
            APTPreprocHandler preprocHandler = createEmptyPreprocHandler(absPath);
            if (state != null) {
                if (state.isCleaned()) {
                    preprocHandler = restorePreprocHandler(absPath, preprocHandler, state);
                } else {
                    if (TRACE_PP_STATE_OUT) {
                        System.err.println("copying state for " + absPath);
                    }
                    preprocHandler.setState(state);
                }
            }
            if (TRACE_PP_STATE_OUT) {
                System.err.printf("null state for %s, returning default one", absPath);
            }
            result.add(preprocHandler);
        }
        return result;
    }

    /**
     * This method for testing purpose only. Used from TraceModel
     */
    public final CsmFile testAPTParseFile(NativeFileItem item) {
        APTPreprocHandler preprocHandler = this.createPreprocHandler(item);
        return findFile(item.getAbsolutePath(), false, Utils.getFileType(item), preprocHandler, true, preprocHandler.getState(), item);
    }

    protected final APTPreprocHandler.State setChangedFileState(NativeFileItem nativeFile) {
        // TODO: do we need to change states in dependent projects' storages???
        APTPreprocHandler.State state;
        state = createPreprocHandler(nativeFile).getState();
        FileContainer fileContainer = getFileContainer();
        FileContainer.FileEntry entry = fileContainer.getEntry(nativeFile.getAbsolutePath());
        synchronized (entry.getLock()) {
            entry.invalidateStates();
            entry.setState(state, FilePreprocessorConditionState.PARSING);
        }
        fileContainer.put();
        return state;
    }

    protected final void invalidatePreprocState(CharSequence absPath) {
        FileContainer fileContainer = getFileContainer();
        Object stateLock = fileContainer.getLock(absPath);
        Collection<ProjectBase> dependentProjects = getDependentProjects();
        synchronized (stateLock) {
            fileContainer.invalidatePreprocState(absPath);
            this.invalidateIncludedPreprocState(stateLock, this, absPath);
            for (ProjectBase projectBase : dependentProjects) {
                projectBase.invalidateIncludedPreprocState(stateLock, this, absPath);
            }
        }
        fileContainer.put();
    }

    protected final void markAsParsingPreprocStates(CharSequence absPath) {
        // TODO: do we need to change states in dependent projects' storages???
        FileContainer fileContainer = getFileContainer();
        Object stateLock = fileContainer.getLock(absPath);
        synchronized (stateLock) {
            fileContainer.markAsParsingPreprocStates(absPath);
        }
//        fileContainer.put();
    }
    /**
     * The method is for tracing/testing/debugging purposes only
     */
    public final void debugInvalidateFiles() {
        // TODO: do we need to change states in dependent projects' storages???
        getFileContainer().debugClearState();
        for (Iterator<CsmProject> it = getLibraries().iterator(); it.hasNext();) {
            ProjectBase lib = (ProjectBase) it.next();
            lib.debugInvalidateFiles();
        }
    }

    private static final boolean TRACE_FILE = (TraceFlags.TRACE_FILE_NAME != null);
    /**
     * called to inform that file was #included from another file with specific preprocHandler
     *
     * @param file included file path
     * @param preprocHandler preprocHandler with which the file is including
     * @param mode of walker forced onFileIncluded for #include directive
     * @return true if it's first time of file including
     *          false if file was included before
     */
    public final FileImpl onFileIncluded(ProjectBase startProject, CharSequence file, APTPreprocHandler preprocHandler, PostIncludeData postIncludeState, int mode, boolean triggerParsingActivity) throws IOException {
        assert preprocHandler != null : "null preprocHandler for " + file;
        if (isDisposing() || startProject.isDisposing()) {
            return null;
        }
        final CsmModelState modelState = ModelImpl.instance().getState();
        if (modelState == CsmModelState.CLOSING || modelState == CsmModelState.OFF) {
            if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_MODEL_STATE) {
                System.err.printf("onFileIncluded: %s file [%s] is interrupted on closing model\n", file, this.getName());
            }
            return null;
        }
        FileImpl csmFile = findFile(file, true, FileImpl.FileType.HEADER_FILE, preprocHandler, false, null, null);

        if (csmFile == null || isDisposing() || startProject.isDisposing()) {
            return csmFile;
        }
        APTPreprocHandler.State newState = preprocHandler.getState();
        PreprocessorStatePair cachedOut = null;
        APTFileCacheEntry aptCacheEntry = null;
        FilePreprocessorConditionState pcState = null;
        boolean foundInCache = false;
        // check post include cache
        if (postIncludeState != null && postIncludeState.hasDeadBlocks()) {
            assert postIncludeState.hasPostIncludeMacroState() : "how could it be? " + file;
            pcState = FilePreprocessorConditionState.Builder.build(file, postIncludeState.getDeadBlocks());
            preprocHandler.getMacroMap().setState(postIncludeState.getPostIncludeMacroState());
            foundInCache = true;
        }
        // check visited file cache
        boolean isFileCacheApplicable = (mode == ProjectBase.GATHERING_TOKENS) && (APTHandlersSupport.getIncludeStackDepth(newState) != 0);
        if (!foundInCache && isFileCacheApplicable) {
            cachedOut = csmFile.getCachedVisitedState(newState);
            if (cachedOut != null) {
                preprocHandler.getMacroMap().setState(APTHandlersSupport.extractMacroMapState(cachedOut.state));
                pcState = cachedOut.pcState;
                foundInCache = true;
            }
        }
        // if not found in caches => visit include file
        if (!foundInCache) {
            APTFile aptLight = getAPTLight(csmFile);
            if (aptLight == null) {
                // in the case file was just removed
                Utils.LOG.log(Level.INFO, "Can not find or build APT for file {0}", file); //NOI18N
                return csmFile;
            }

            // gather macro map from all includes and fill preprocessor conditions state
            FilePreprocessorConditionState.Builder pcBuilder = new FilePreprocessorConditionState.Builder(csmFile.getAbsolutePath());
            // ask for exclusive entry if absent
            aptCacheEntry = csmFile.getAPTCacheEntry(preprocHandler, Boolean.TRUE);
            APTParseFileWalker walker = new APTParseFileWalker(startProject, aptLight, csmFile, preprocHandler, triggerParsingActivity, pcBuilder,aptCacheEntry);
            walker.visit();
            pcState = pcBuilder.build();
        }
        // updated caches
        // update post include cache
        if (postIncludeState != null && !postIncludeState.hasDeadBlocks()) {
            // cache info
            postIncludeState.setDeadBlocks(FilePreprocessorConditionState.Builder.getDeadBlocks(pcState));
        }
        // updated visited file cache
        if (cachedOut == null && isFileCacheApplicable) {
            csmFile.cacheVisitedState(newState, preprocHandler, pcState);
        }
        boolean thisProjectUpdateResult = false;
        boolean startProjectUpdateResult = false;
        try {
            if (isDisposing() || startProject.isDisposing()) {
                return csmFile;
            }
            if (triggerParsingActivity) {
                FileContainer.FileEntry
                entry = getFileContainer().getEntry(csmFile.getAbsolutePath());
                if (entry == null) {
                    entryNotFoundMessage(file);
                    return csmFile;
                }
                synchronized (entry.getLock()) {
                    PreprocessorStatePair newStatePair = new PreprocessorStatePair(newState, pcState);
                    // register included file and it's states in start project under current included file lock
                    startProjectUpdateResult = startProject.updateFileEntryForIncludedFile(entry, this, file, csmFile, newStatePair);
                    
                    // decide if parse is needed
                    List<APTPreprocHandler.State> statesToParse = new ArrayList<APTPreprocHandler.State>(4);
                    statesToParse.add(newState);
                    AtomicBoolean clean = new AtomicBoolean(false);
                    thisProjectUpdateResult = updateFileEntryBasedOnIncludedStatePair(entry, newStatePair, file, csmFile, clean, statesToParse);
                    if (thisProjectUpdateResult) {
                        // start project can be this project or another project, but
                        // we found the "best from the bests" for the current lib;
                        // it have to be considered as the best in start project lib storage as well
                        if (!startProjectUpdateResult) {
                            CndUtils.assertTrueInConsole(false, " this project " + this + " thinks that new state for " + file + " is the best but start project does not take it " + startProject);
                        }
                    }
                    if (thisProjectUpdateResult) {
                        // TODO: think over, what if we aready changed entry,
                        // but now deny parsing, because base, but not this project, is disposing?!
                        if (!isDisposing() && !startProject.isDisposing()) {
                            csmFile.setAPTCacheEntry(preprocHandler, aptCacheEntry, clean.get());
                            if (!TraceFlags.PARSE_HEADERS_WITH_SOURCES) {
                                ParserQueue.instance().add(csmFile, statesToParse, ParserQueue.Position.HEAD, clean.get(),
                                        clean.get() ? ParserQueue.FileAction.MARK_REPARSE : ParserQueue.FileAction.MARK_MORE_PARSE);
                            }
                        }
                    }
                }
            }
            return csmFile;
        } finally {
            if (thisProjectUpdateResult) {
                getFileContainer().put();
            }
            if (startProjectUpdateResult) {
                startProject.putIncludedFileStorage(this);
            }
        }
    }

    private boolean updateFileEntryForIncludedFile(FileEntry entryToLockOn, ProjectBase includedProject, CharSequence includedFileKey, FileImpl includedFile, PreprocessorStatePair newStatePair) {
        boolean startProjectUpdateResult;
        FileContainer.FileEntry includedFileEntryFromStartProject = includedFileContainer.getOrCreateEntryForIncludedFile(entryToLockOn, includedProject, includedFile);
        if (includedFileEntryFromStartProject != null) {
            startProjectUpdateResult = updateFileEntryBasedOnIncludedStatePair(includedFileEntryFromStartProject, newStatePair, includedFileKey, includedFile, null, null);
        } else {
            startProjectUpdateResult = false;
        }
        return startProjectUpdateResult;
    }

    private final IncludedFileContainer includedFileContainer;

    private void putIncludedFileStorage(ProjectBase includedProject) {
        includedFileContainer.putStorage(includedProject);
    }

    void invalidateLibraryStorage(CsmUID<CsmProject> libraryUID) {
        includedFileContainer.invalidateIncludeStorage(libraryUID);
    }

    IncludedFileContainer.Storage getIncludedLibraryStorage(ProjectBase includedProject) {
        return includedFileContainer.getStorageForProject(includedProject);
    }

    void prepareIncludeStorage(ProjectBase includedProject) {
        includedFileContainer.prepareIncludeStorage(includedProject);
    }

    Map<CsmUID<CsmProject> , Collection<PreprocessorStatePair>> getIncludedPreprocStatePairs(FileImpl fileToSearch) {
        return includedFileContainer.getPairsToDump(fileToSearch);
    }

    private void invalidateIncludedPreprocState(Object lock, ProjectBase includedFileOwner, CharSequence absPath) {
        includedFileContainer.invalidate(lock, includedFileOwner, absPath);
    }

    public Collection<State> getIncludedPreprocStates(FileImpl impl) {
        Collection<ProjectBase> dependentProjects = getDependentProjects();
        CharSequence fileKey = FileContainer.getFileKey(impl.getAbsolutePath(), false);
        Object stateLock = getFileContainer().getLock(fileKey);
        Collection<State> states = new ArrayList<State>(dependentProjects.size() + 1);
        synchronized (stateLock) {
            Collection<FileEntry> entries = this.getIncludedFileEntries(stateLock, fileKey);
            for (FileEntry fileEntry : entries) {
                states.addAll(fileEntry.getPrerocStates());
            }
        }
        return states;
    }

    private Collection<FileEntry> getIncludedFileEntries(Object stateLock, CharSequence fileKey) {
        assert Thread.holdsLock(stateLock) : " must hold state lock for " + fileKey;
        Collection<ProjectBase> dependentProjects = getDependentProjects();
        Collection<FileEntry> out = new ArrayList<FileEntry>(dependentProjects.size() + 1);
        FileEntry ownEntry = this.includedFileContainer.getIncludedFileEntry(stateLock, this, fileKey);
        if (ownEntry != null) {
            out.add(ownEntry);
        }
        for (ProjectBase dep : dependentProjects) {
            FileEntry depPrjEntry = dep.includedFileContainer.getIncludedFileEntry(stateLock, this, fileKey);
            if (depPrjEntry != null) {
                out.add(depPrjEntry);
            }
        }
        return out;
    }

    private void mergeFileContainerFromStorage(ProjectBase startPrj) {
        Storage storage = startPrj.getIncludedLibraryStorage(this);
        // we are library and were asked to update own file container
        // based on storage kept in dependent project (i.e. when project was opened)
        Map<CharSequence, FileEntry> internalMap = storage.getInternalMap();
        try {
            for (Map.Entry<CharSequence, FileEntry> storageEntry : internalMap.entrySet()) {
                CharSequence key = storageEntry.getKey();
                this.onFileIncluded(startPrj, key, null, null, ProjectBase.GATHERING_MACROS, true);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void mergeFromStorage(Storage storage) {

    }

    private boolean updateFileEntryBasedOnIncludedStatePair(
            FileContainer.FileEntry entry, PreprocessorStatePair newStatePair,
            CharSequence file, FileImpl csmFile,
            AtomicBoolean cleanOut, List<APTPreprocHandler.State> statesToParse) {
        String prefix = statesToParse == null ? "lib update:" : "parsing:"; // NOI18N
        APTPreprocHandler.State newState = newStatePair.state;
        FilePreprocessorConditionState pcState = newStatePair.pcState;
        List<PreprocessorStatePair> statesToKeep = new ArrayList<PreprocessorStatePair>(4);
        AtomicBoolean newStateFound = new AtomicBoolean();
        Collection<PreprocessorStatePair> entryStatePairs = entry.getStatePairs();
        // Phase 1: check preproc states of entry comparing to current state
        ComparisonResult comparisonResult = fillStatesToKeepBasedOnPPState(newState, entryStatePairs, statesToKeep, newStateFound);
        if (TRACE_FILE && FileImpl.traceFile(file)) {
            traceIncludeStates(prefix+"comparison 2 " + comparisonResult, csmFile, newState, pcState, newStateFound.get(), null, statesToKeep); // NOI18N
        }
        if (comparisonResult == ComparisonResult.DISCARD) {
            if (TRACE_FILE && FileImpl.traceFile(file)) {
                traceIncludeStates(prefix+"worse 2", csmFile, newState, pcState, false, null, statesToKeep); // NOI18N
            }
            return false;
        } else if (comparisonResult == ComparisonResult.KEEP_WITH_OTHERS) {
            if (newStateFound.get()) {
                // we are already in the list and not better than all, can stop
                if (TRACE_FILE && FileImpl.traceFile(file)) {
                    traceIncludeStates(prefix+"state is already here ", csmFile, newState, pcState, false, null, statesToKeep); // NOI18N
                }
                return false;
            }
        }
        // from that point we are NOT interested in what is in the entry:
        // it's locked; "good" states are are in statesToKeep, "bad" states don't matter

        assert comparisonResult != ComparisonResult.DISCARD;

        boolean clean;

        if (comparisonResult == ComparisonResult.REPLACE_OTHERS) {
            clean = true;
            CndUtils.assertTrueInConsole(statesToKeep.isEmpty(), "states to keep must be empty 2"); // NOI18N
            if (TRACE_FILE && FileImpl.traceFile(file)) {
                traceIncludeStates(prefix+"best state", csmFile, newState, pcState, clean, statesToParse, statesToKeep); // NOI18N
            }
        } else {  // comparisonResult == SAME
            clean = false;
            // Phase 2: check preproc conditional states of entry comparing to current conditional state
            comparisonResult = fillStatesToKeepBasedOnPCState(pcState, new ArrayList<PreprocessorStatePair>(statesToKeep), statesToKeep);
            if (TRACE_FILE && FileImpl.traceFile(file)) {
                traceIncludeStates(prefix+"pc state comparison " + comparisonResult, csmFile, newState, pcState, clean, statesToParse, statesToKeep); // NOI18N
            }
            switch (comparisonResult) {
                case REPLACE_OTHERS:
                    CndUtils.assertTrueInConsole(statesToKeep.isEmpty(), "states to keep must be empty 3"); // NOI18N
                    clean = true;
                    break;
                case KEEP_WITH_OTHERS:
                    break;
                case DISCARD:
                    return false;
                default:
                    assert false : prefix+"unexpected comparison result: " + comparisonResult; //NOI18N
                    return false;
            }
        }
        if (statesToParse != null && clean) {
            for (PreprocessorStatePair pair : statesToKeep) {
                // if pair has parsing in pair.pcState => it was not valid source file
                // skip it
                if (pair.pcState != FilePreprocessorConditionState.PARSING) {
                    statesToParse.add(pair.state);
                }
            }
        }
        entry.setStates(statesToKeep, newStatePair);
        if (statesToParse != null) {
            if (TRACE_FILE && FileImpl.traceFile(file)
                    && (TraceFlags.TRACE_PC_STATE || TraceFlags.TRACE_PC_STATE_COMPARISION)) {
                traceIncludeStates(prefix+"scheduling", csmFile, newState, pcState, clean, // NOI18N
                        statesToParse, statesToKeep);
            }
        }
        if (cleanOut != null) {
            cleanOut.set(clean);
        }
        return true;
    }

    private void entryNotFoundMessage(CharSequence file) {
        if (Utils.LOG.isLoggable(Level.INFO)) {
            // since file container can return empty container the entry can be null.
            StringBuilder buf = new StringBuilder("File container does not have file "); //NOI18N
            buf.append("[").append(file).append("]"); //NOI18N
            if (getFileContainer() == FileContainer.empty()) {
                buf.append(" because file container is EMPTY."); //NOI18N
            } else {
                buf.append("."); //NOI18N
            }
            if (isDisposing()) {
                buf.append("\n\tIt is very strange but project is disposing."); //NOI18N
            }
            if (!isValid()) {
                buf.append("\n\tIt is very strange but project is invalid."); //NOI18N
            }
            Status st = getStatus();
            if (st != null) {
                buf.append("\n\tProject ").append(toString()).append(" has status ").append(st).append("."); //NOI18N
            }
            Utils.LOG.info(buf.toString());
        }
    }

    private static void traceIncludeStates(CharSequence title,
            FileImpl file, APTPreprocHandler.State newState, FilePreprocessorConditionState pcState,
            boolean clean, Collection<APTPreprocHandler.State> statesToParse, Collection<PreprocessorStatePair> statesToKeep) {

        StringBuilder sb = new StringBuilder();
        for (PreprocessorStatePair pair : statesToKeep) {
            if (sb.length() > 0) {
                sb.append(", "); //NOI18N
            }
            sb.append(pair.pcState);
        }


        APTPreprocHandler preprocHandler = file.getProjectImpl(true).createEmptyPreprocHandler(file.getAbsolutePath());
        preprocHandler.setState(newState);

        System.err.printf("%s %s (1) %s\n\tfrom %s \n\t%s %s \n\t%s keeping [%s]\n", title, //NOI18N
                (clean ? "reparse" : "  parse"), file.getAbsolutePath(), //NOI18N
                APTHandlersSupport.extractStartEntry(newState).getStartFile(),
                TraceUtils.getPreprocStateString(preprocHandler.getState()),
                TraceUtils.getMacroString(preprocHandler, TraceFlags.logMacros),
                pcState, sb);

        if (statesToParse != null) {
            for (APTPreprocHandler.State state : statesToParse) {
                if (!newState.equals(state)) {
                    FilePreprocessorConditionState currPcState = null;
                    for (PreprocessorStatePair pair : statesToKeep) {
                        if (newState.equals(pair.state)) {
                            currPcState = pair.pcState;
                            break;
                        }
                    }
                    System.err.printf("%s %s (2) %s \n\tfrom %s\n\t valid %b context %b %s\n", title,//NOI18N
                            "  parse", file.getAbsolutePath(), //NOI18N
                            APTHandlersSupport.extractStartEntry(state).getStartFile(),
                            state.isValid(), state.isCompileContext(), currPcState);
                }
            }
        }
    }

    boolean setParsedPCState(FileImpl csmFile, State ppState, FilePreprocessorConditionState pcState) {
        FileContainer.FileEntry entry = getFileContainer().getEntry(csmFile.getAbsolutePath());
        if (entry == null) {
            entryNotFoundMessage(csmFile.getAbsolutePath());
            return false;
        }
        boolean entryFound;
        // IZ#179861: unstable test RepositoryValidation
        synchronized (entry.getLock()) {
            List<PreprocessorStatePair> statesToKeep = new ArrayList<PreprocessorStatePair>(4);
            Collection<PreprocessorStatePair> entryStatePairs = entry.getStatePairs();
            if (TraceFlags.TRACE_182342_BUG) {
                System.err.printf("setParsedPCState: original states for file: %s \n with new state: %s\n and pcState: %s\n", csmFile, ppState, pcState);
                if (entryStatePairs.isEmpty()) {
                    System.err.println("NO ORIGINAL STATES");
                } else {
                    int i = 0;
                    for (PreprocessorStatePair preprocessorStatePair : entryStatePairs) {
                        System.err.printf("setParsedPCState: State %d from original %s\n", i++, preprocessorStatePair);
                    }
                }
            }
            List<PreprocessorStatePair> copy = new ArrayList<PreprocessorStatePair>();
            entryFound = false;
            // put into copy array all except ourself
            for (PreprocessorStatePair pair : entryStatePairs) {
                assert pair != null : "can not be null element in " + entryStatePairs;
                assert pair.state != null: "state can not be null in pair " + pair + " for file " + csmFile;
                if ((pair.pcState == FilePreprocessorConditionState.PARSING) &&
                        // there coud be invalidated state which is in parsing phase now
                        APTHandlersSupport.equalsIgnoreInvalid(pair.state, ppState)) {
                    assert !entryFound;
                    entryFound = true;
                } else {
                    copy.add(pair);
                }
            }
            if (TraceFlags.TRACE_182342_BUG) {
                System.err.printf("setParsedPCState: %s found PARSING entry for file: %s \n", entryFound ? "" : "NOT", csmFile);
                if (copy.isEmpty()) {
                    System.err.println("NO KEPT STATES");
                } else {
                    int i = 0;
                    for (PreprocessorStatePair preprocessorStatePair : copy) {
                        System.err.printf("setParsedPCState: State %d from copy %s\n", i++, preprocessorStatePair);
                    }
                }
            }
            if (entryFound) {
                // Phase 2: check preproc conditional states of entry comparing to current conditional state
                ComparisonResult comparisonResult = fillStatesToKeepBasedOnPCState(pcState, copy, statesToKeep);
                switch (comparisonResult) {
                    case REPLACE_OTHERS:
                        CndUtils.assertTrueInConsole(statesToKeep.isEmpty(), "states to keep must be empty 3"); // NOI18N
                        entry.setStates(statesToKeep, new PreprocessorStatePair(ppState, pcState));
                        break;
                    case KEEP_WITH_OTHERS:
                        assert !statesToKeep.isEmpty();
                        entry.setStates(statesToKeep, new PreprocessorStatePair(ppState, pcState));
                        break;
                    case DISCARD:
                        assert !copy.isEmpty();
                        entry.setStates(copy, null);
                        break;
                    default:
                        assert false : "unexpected comparison result: " + comparisonResult; //NOI18N
                        break;
                }
            } else {
                // we already were removed, because our ppState was worse
                // or
                // header was parsed with correct context =>
                // no reason to check pcState and replace FilePreprocessorConditionState.PARSING
                // which is not present
            }
        }
        if (entryFound) {
            FileContainer fileContainer = getFileContainer();
            fileContainer.put();
        }
        return entryFound;
    }

    void notifyOnWaitParseLock() {
        // notify client waiting for end of fake registration
        synchronized (waitParseLock) {
            waitParseLock.notifyAll();
        }
    }

    public Iterator<CsmUID<CsmFile>> getFilteredFileUIDs(NameAcceptor nameFilter) {
        FileContainer fileContainer = getFileContainer();
        Collection<CsmUID<CsmFile>> filesUID = fileContainer.getFilesUID();
        Collection<CsmUID<CsmFile>> out = new ArrayList<CsmUID<CsmFile>>(filesUID.size());
        for (CsmUID<CsmFile> fileUID : filesUID) {
            CharSequence fileName = FileInfoQueryImpl.getFileName(fileUID);
            if (nameFilter.accept(fileName)) {
                out.add(fileUID);
            }
        }
        return out.iterator();
    }

    /**
     * @return the projectRoots
     */
    protected abstract SourceRootContainer getProjectRoots();

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    private static enum ComparisonResult {

        REPLACE_OTHERS,
        KEEP_WITH_OTHERS,
        DISCARD
    }

    /**
     * Checks old states and new one, decides
     * 1. which states to keep
     *    (returns collection of these states)
     * 2. is new state better, worse, or ~same than old ones
     *
     * NB: all OUT parameters are set in this function, so their initial values don't matter
     *
     * @param newState  IN:  new preprocessor state
     *
     * @param oldStates IN:  a collection of old states;
     *                       it might contain newState as well
     *
     * @param statesToKeep  OUT: array to fill with of old states
     *                      (except for new state! - it isn't copied here)
     *                      Unpredictable in the case function returns WORSE
     *
     * @param  newStateFound  OUT: set to true if new state is found among old ones
     *
     * @return  BETTER - new state is better than old ones
     *          SAME - new state is more or less  the same :) as old ones
     *          WORSE - new state is worse than old ones
     */
    private ComparisonResult fillStatesToKeepBasedOnPPState(
            APTPreprocHandler.State newState,
            Collection<PreprocessorStatePair> oldStates,
            Collection<PreprocessorStatePair> statesToKeep,
            AtomicBoolean newStateFound) {

        if (newState == null || !newState.isValid()) {
            return ComparisonResult.DISCARD;
        }

        statesToKeep.clear();
        newStateFound.set(false);
        ComparisonResult result = ComparisonResult.KEEP_WITH_OTHERS;

        for (PreprocessorStatePair pair : oldStates) {
            // newState might already be contained in oldStates
            // it should NOT be added to result
            if (newState.equals(pair.state)) {
                assert !newStateFound.get();
                newStateFound.set(true);
            } else {
                boolean keep = false;
                // check if pure invalid state, but do not consider as invalid
                // the invalidated entry in parsing mode
                if (pair.state != null && (pair.state.isValid())) {
                    if (pair.state.isCompileContext()) {
                        keep = true;
                        if (!newState.isCompileContext()) {
                            return ComparisonResult.DISCARD;
                        }
                    } else {
                        keep = !newState.isCompileContext();
                    }
                }
                if (keep) {
                    if (!pair.state.isCleaned()) {
                        pair = new PreprocessorStatePair(APTHandlersSupport.createCleanPreprocState(pair.state), pair.pcState);
                    }
                    statesToKeep.add(pair);
                    result = ComparisonResult.KEEP_WITH_OTHERS;
                } else {
                    CndUtils.assertTrueInConsole(statesToKeep.isEmpty() || !newState.isCompileContext(), "states to keep must be empty for new compile context entry"); // NOI18N
                    result = statesToKeep.isEmpty() ? ComparisonResult.REPLACE_OTHERS : ComparisonResult.KEEP_WITH_OTHERS;
                }
            }
        }
        if (result == ComparisonResult.REPLACE_OTHERS) {
            CndUtils.assertTrueInConsole(statesToKeep.isEmpty(), "states to keep must be empty "); // NOI18N
        }
        return result;
    }

    /**
     * If it returns WORSE, statesToKeep content is unpredictable!
     *
     * @param newState
     * @param pcState
     * @param oldStates
     * @param statesToKeep
     * @return
     */
    private ComparisonResult fillStatesToKeepBasedOnPCState(
            FilePreprocessorConditionState pcState,
            List<PreprocessorStatePair> oldStates,
            List<PreprocessorStatePair> statesToKeep) {

        boolean isSuperset = true; // true if this state is a superset of each old state

        // we assume that
        // 1. all oldStates are valid
        // 2. either them all are compileContext
        //    or this one and them all are NOT compileContext
        // so we do *not* check isValid & isCompileContext

        statesToKeep.clear();
        // in this place use direct for loop over list with known size
        // instead of "for (PreprocessorStatePair old : oldStates)"
        // due to performance problem of iterator.hasNext
        int size = oldStates.size();
        for (int i = 0; i < size; i++) {
            PreprocessorStatePair old = oldStates.get(i);
            if (old.pcState == FilePreprocessorConditionState.PARSING) {
                isSuperset = false;
                // not yet filled - file parsing is filling it right now => we don't know what it will be => keep it
                if (!old.state.isCleaned()) {
                    old = new PreprocessorStatePair(APTHandlersSupport.createCleanPreprocState(old.state), old.pcState);
                }
                statesToKeep.add(old);
            } else {
                if (old.pcState.isBetterOrEqual(pcState)) {
                    return ComparisonResult.DISCARD;
                } else if (pcState.isBetterOrEqual(old.pcState)) {
                    // still superset or current can replace old
                } else {
                    // states are not comparable => not superset
                    isSuperset = false;
                    if (!old.state.isCleaned()) {
                        old = new PreprocessorStatePair(APTHandlersSupport.createCleanPreprocState(old.state), old.pcState);
                    }
                    statesToKeep.add(old);
                }
            }
        }
        if (isSuperset) {
            assert statesToKeep.isEmpty() : "should be empty, but it is: " + Arrays.toString(statesToKeep.toArray());
            return ComparisonResult.REPLACE_OTHERS;
        } else {
            return ComparisonResult.KEEP_WITH_OTHERS;
        }
    }

//    private static final boolean isValid(APTPreprocHandler.State state) {
//        return state != null && state.isValid();
//    }
    public ProjectBase findFileProject(CharSequence absPath, boolean waitFilesCreated) {
        // check own files
        // Wait while files are created. Otherwise project file will be recognized as library file.
        if (waitFilesCreated) {
            ensureFilesCreated();
        }
        if (getFileUID(absPath, false) != null) {
            return this;
        } else {
            // else check in libs
            for (CsmProject prj : getLibraries()) {
                // Wait while files are created. Otherwise project file will be recognized as library file.
                if (waitFilesCreated) {
                    ((ProjectBase) prj).ensureFilesCreated();
                }
                if (((ProjectBase) prj).getFileUID(absPath, false) != null) {
                    return (ProjectBase) prj;
                }
            }
        }
        return null;
    }

    public ProjectBase findFileProject(FSPath fsPath, boolean waitFilesCreated) {
        // check own files
        // Wait while files are created. Otherwise project file will be recognized as library file.
        if (getFileSystem() == fsPath.getFileSystem()){
            if (waitFilesCreated) {
                ensureFilesCreated();
            }
            if (getFileUID(fsPath.getPath(), false) != null) {
                return this;
            }
        }
        // else check in libs
        for (CsmProject prj : getLibraries()) {
            if (((ProjectBase) prj).getFileSystem() == fsPath.getFileSystem()) {
                // Wait while files are created. Otherwise project file will be recognized as library file.
                if (waitFilesCreated) {
                    ((ProjectBase) prj).ensureFilesCreated();
                }
                if (((ProjectBase) prj).getFileUID(fsPath.getPath(), false) != null) {
                    return (ProjectBase) prj;
                }
            }
        }
        return null;        
    }
    
    public final boolean isMySource(String includePath) {
        return getProjectRoots().isMySource(includePath);
    }

    public abstract void onFileAdded(NativeFileItem nativeFile);

    public abstract void onFileAdded(List<NativeFileItem> items);
    //public abstract void onFileRemoved(NativeFileItem nativeFile);

    public abstract void onFileImplRemoved(Collection<FileImpl> files);

    public abstract void onFileRemoved(List<NativeFileItem> items);

    public abstract void onFilePropertyChanged(NativeFileItem nativeFile);

    public abstract void onFilePropertyChanged(List<NativeFileItem> items, boolean invalidateLibs);

    protected abstract ParserQueue.Position getIncludedFileParserQueuePosition();

    public abstract NativeFileItem getNativeFileItem(CsmUID<CsmFile> file);

    protected abstract void putNativeFileItem(CsmUID<CsmFile> file, NativeFileItem nativeFileItem);

    protected abstract NativeFileItem removeNativeFileItem(CsmUID<CsmFile> file);

    protected abstract void clearNativeFileContainer();

    public final void onFileRemoved(CharSequence absPath) {
        onFileImplRemoved(Collections.singletonList(getFile(absPath, false)));
    }

    public final void onFileExternalCreate(FileObject file) {
        CndFileUtils.clearFileExistenceCache();
        // #196664 - Code Model ignores the generated files"
        // when external file was created and assigned to this project => 
        // create csm file for it if possible
        NativeFileItem nativeFileItem = null;
        // Try to find native file
        if (getPlatformProject() instanceof NativeProject) {
            NativeProject prj = (NativeProject) getPlatformProject();
            if (prj != null) {
                nativeFileItem = prj.findFileItem(file);
            }
        }
        // schedule reparse either based on NFI 
        // or use FO as fallback, it can be helpful for header files not included into
        // project, but used in include directives which were broken so far
        if (nativeFileItem != null) {
            onFileAdded(nativeFileItem);
        } else {
            DeepReparsingUtils.reparseOnAdded(file, this);
        }
    }

    public final void onFileExternalChange(FileImpl file) {
        DeepReparsingUtils.tryPartialReparseOnChangedFile(this, file);
    }

    @Override
    public final CsmFile findFile(Object absolutePathOrNativeFileItem, boolean createIfPossible, boolean snapShot) {
        CsmFile res = null;
        if (absolutePathOrNativeFileItem instanceof FSPath) {
            FSPath fsPath = (FSPath)absolutePathOrNativeFileItem;
            if (this.getFileSystem() != fsPath.getFileSystem()) {
                return null;
            }
            absolutePathOrNativeFileItem = fsPath.getPath();
        }
        if (absolutePathOrNativeFileItem instanceof CharSequence) {
            res = findFileByPath((CharSequence) absolutePathOrNativeFileItem, createIfPossible);
        } else if (absolutePathOrNativeFileItem instanceof NativeFileItem) {
            res = findFileByItem((NativeFileItem) absolutePathOrNativeFileItem, createIfPossible);
        }
        if (snapShot && (res instanceof FileImpl)) {
            res = ((FileImpl)res).getSnapshot();
        }
        return res;
    }

    /*package*/final int getFileContainerSize() {
        return getFileContainer().getSize();
    }
    
    private CsmFile findFileByPath(CharSequence absolutePath, boolean createIfPossible) {
        absolutePath = CndFileUtils.normalizeAbsolutePath(fileSystem, absolutePath.toString());
        APTPreprocHandler preprocHandler = null;
        if (getFileContainer().getEntry(absolutePath) == null) {
            if (!createIfPossible) {
                return null;
            }
            NativeFileItem nativeFile = null;
            // Try to find native file
            if (getPlatformProject() instanceof NativeProject) {
                NativeProject prj = (NativeProject) getPlatformProject();
                if (prj != null) {
                    FileObject fo = CndFileUtils.toFileObject(prj.getFileSystem(), absolutePath.toString());
                    if (fo != null) {
                        nativeFile = prj.findFileItem(fo);
                    }
                    if (nativeFile == null) {
                        // if not belong to NB project => not our file
                        return null;
                    // nativeFile = new DefaultFileItem(prj, absolutePath);
                    }
                    if (!Utils.acceptNativeItem(nativeFile)) {
                        return null;
                    }
                    preprocHandler = createPreprocHandler(nativeFile);
                }
            }
            if (preprocHandler != null) {
                return findFile(absolutePath, false, FileImpl.FileType.UNDEFINED_FILE, preprocHandler, true, preprocHandler.getState(), nativeFile);
            }
        }
        // if getPreprocState(file) isn't null, the file alreasy exists, so we may not pass nativeFile
        return findFile(absolutePath, false, FileImpl.FileType.UNDEFINED_FILE, preprocHandler, true, null, null);
    }

    private CsmFile findFileByItem(NativeFileItem nativeFile, boolean createIfPossible) {
        CharSequence file = nativeFile.getAbsolutePath();
        APTPreprocHandler preprocHandler = null;
        if (getFileContainer().getEntry(file) == null) {
            if (!createIfPossible || !Utils.acceptNativeItem(nativeFile)) {
                return null;
            }
            // Try to find native file
            if (getPlatformProject() instanceof NativeProject) {
                NativeProject prj = nativeFile.getNativeProject();
                if (prj != null && nativeFile.getFileObject() != null && nativeFile.getFileObject().isValid()) {
                    preprocHandler = createPreprocHandler(nativeFile);
                }
            }
            if (preprocHandler != null) {
                return findFile(file, false, FileImpl.FileType.UNDEFINED_FILE, preprocHandler, true, preprocHandler.getState(), nativeFile);
            }
        }
        // if getPreprocState(file) isn't null, the file alreasy exists, so we may not pass nativeFile
        return findFile(file, false, FileImpl.FileType.UNDEFINED_FILE, preprocHandler, true, null, null);
    }

    protected final FileImpl findFile(CharSequence absPath, boolean treatSymlinkAsSeparateFile, FileImpl.FileType fileType, APTPreprocHandler preprocHandler,
            boolean scheduleParseIfNeed, APTPreprocHandler.State initial, NativeFileItem nativeFileItem) {
        FileImpl impl = getFile(absPath, treatSymlinkAsSeparateFile);
        if (impl == null){
            CndUtils.assertTrueInConsole(preprocHandler != null, "null preprocHandler");
            //FIXME:
            if (preprocHandler == null) {
                APTPreprocHandler.State state = getFirstValidPreprocState(absPath);
                preprocHandler = createPreprocHandlerFromState(absPath, state);
            }
            impl = findFileImpl(absPath, treatSymlinkAsSeparateFile, fileType, preprocHandler, scheduleParseIfNeed, initial, nativeFileItem);
        }
        return impl;
    }

    private FileImpl findFileImpl(CharSequence absPath, boolean treatSymlinkAsSeparateFile, FileImpl.FileType fileType, APTPreprocHandler preprocHandler,
            boolean scheduleParseIfNeed, APTPreprocHandler.State initial, NativeFileItem nativeFileItem) {
        FileImpl impl;
        boolean create = false;
        synchronized (fileContainerLock) {
            impl = getFile(absPath, treatSymlinkAsSeparateFile);
            if (impl == null) {
                create = true;
            }
        }
        if (create) {
            // it is expensive in Full Remote mode to create buffer, so do the work out of sync block
            assert preprocHandler != null : "null preprocHandler for " + absPath;
            FileObject fo = CndFileUtils.toFileObject(fileSystem, absPath);
            CndUtils.assertTrueInConsole(fo != null, "file object not found ", absPath); // + " in fs=" + fileSystem); // NOI18N
            if (fo == null) {
                return null;
            }
            FileBuffer fileBuffer = ModelSupport.createFileBuffer(fo);
            // and all other under lock again
            synchronized (fileContainerLock) {
                impl = getFile(absPath, treatSymlinkAsSeparateFile);
                if (impl == null) {
                    impl = new FileImpl(fileBuffer, this, fileType, nativeFileItem);
                    if (nativeFileItem != null) {
                        putNativeFileItem(impl.getUID(), nativeFileItem);
                    }
                    // initial can be null here and due to this we have warnings from ParserThread like:
                    // SEVERE [org.netbeans.modules.cnd.modelimpl]: Adding a file with an emty preprocessor state set
                    // TODO: do we need to set up initial value?
//                    if (initial == null) {
//                        initial = APTHandlersSupport.createCleanPreprocState(preprocHandler.getState());
//                    }
                    putFile(impl, initial);
                    // NB: parse only after putting into a map
                    if (scheduleParseIfNeed) {
                        APTPreprocHandler.State ppState = preprocHandler.getState();
                        ParserQueue.instance().add(impl, ppState, ParserQueue.Position.TAIL);
                    }
                }
            }
        }

        if (fileType == FileImpl.FileType.SOURCE_FILE && !impl.isSourceFile()) {
            impl.setSourceFile();
        } else if (fileType == FileImpl.FileType.HEADER_FILE && !impl.isHeaderFile()) {
            impl.setHeaderFile();
        }
        return impl;
    }
    
    protected final FileImpl createOrFindFileImpl(final FileBuffer buf, final NativeFileItem nativeFile) {
        return createOrFindFileImpl(buf, nativeFile, Utils.getFileType(nativeFile)).fileImpl;
    }

    private static class FileAndHandler {
        private final FileImpl fileImpl;
        private APTPreprocHandler preprocHandler;

        public FileAndHandler(FileImpl fileImpl, APTPreprocHandler preprocHandler) {
            this.fileImpl = fileImpl;
            this.preprocHandler = preprocHandler;
        }
    }

    private FileAndHandler createOrFindFileImpl(final FileBuffer buf, final NativeFileItem nativeFile, FileImpl.FileType fileType) {
        APTPreprocHandler preprocHandler = null;
        CharSequence absPath = buf.getAbsolutePath();
        FileImpl impl = getFile(absPath, true);
        CsmUID<CsmFile> aUid = null;
        if (impl == null) {
            preprocHandler = createPreprocHandler(nativeFile);
            synchronized (fileContainerLock) {
                impl = getFile(absPath, true);
                if (impl == null) {
                    assert preprocHandler != null;
                    impl = new FileImpl(buf, this, fileType, nativeFile);
                    putFile(impl, preprocHandler.getState());
                } else {
                    aUid = impl.getUID();
                }
            }
        } else {
            aUid = impl.getUID();
        }
        if (aUid != null) {
            putNativeFileItem(aUid, nativeFile);
        }
        return new FileAndHandler(impl, preprocHandler);
    }

    public final FileImpl getFile(CharSequence absPath, boolean treatSymlinkAsSeparateFile) {
        return getFileContainer().getFile(absPath, treatSymlinkAsSeparateFile);
    }

    public final CsmUID<CsmFile> getFileUID(CharSequence absPath, boolean treatSymlinkAsSeparateFile) {
        return getFileContainer().getFileUID(absPath, treatSymlinkAsSeparateFile);
    }

    protected final void removeFile(CharSequence file) {
        getFileContainer().removeFile(file);
    }

    protected final void putFile(FileImpl impl, APTPreprocHandler.State state) {
        if (state != null && !state.isCleaned()) {
            state = APTHandlersSupport.createCleanPreprocState(state);
        }
        getFileContainer().putFile(impl, state);
    }

    protected Collection<Key> getLibrariesKeys() {
        List<Key> res = new ArrayList<Key>();
        if (platformProject instanceof NativeProject) {
            for (NativeProject nativeLib : ((NativeProject) platformProject).getDependences()) {
                final Key key = createProjectKey(nativeLib.getFileSystem(), nativeLib);
                if (key != null) {
                    res.add(key);
                }
            }
        }
        // Last dependent project is common library.
        //final Key lib = KeyUtilities.createProjectKey("/usr/include"); // NOI18N
        //if (lib != null) {
        //    res.add(lib);
        //}
        if (!isArtificial()) {
            for (CsmUID<CsmProject> library : LibraryManager.getInstance().getLirariesKeys(getUID())) {
                res.add(RepositoryUtils.UIDtoKey(library));
            }
        }
        return res;
    }

    @Override
    public List<CsmProject> getLibraries() {
        List<CsmProject> res = new ArrayList<CsmProject>();
        if (platformProject instanceof NativeProject) {
            List<NativeProject> dependences = ((NativeProject) platformProject).getDependences();
            int size = dependences.size();
            for (int i = 0; i < size; i++) {
                NativeProject nativeLib = dependences.get(i);
                CsmProject prj = model.findProject(nativeLib);
                if (prj != null) {
                    res.add(prj);
                }
            }
        }
        // Last dependent project is common library.
        //ProjectBase lib = getModel().getLibrary("/usr/include"); // NOI18N
        //if (lib != null) {
        //    res.add(lib);
        //}
        if (!isArtificial()) {
            List<LibProjectImpl> libraries = LibraryManager.getInstance().getLibraries((ProjectImpl) this);
            int size = libraries.size();
            for (int i = 0; i < size; i++) {
                res.add(libraries.get(i));
            }
        }
        return res;
    }

    public Collection<ProjectBase> getDependentProjects() {
        List<ProjectBase> res = new ArrayList<ProjectBase>();
        for (CsmProject prj : model.projects()) {
            if (prj instanceof ProjectBase) {
                if (prj.getLibraries().contains(this)) {
                    res.add((ProjectBase) prj);
                }
            }
        }
        return res;
    }

    /**
     * Creates a dummy ClassImpl for unresolved name, stores in map
     * @param nameTokens name
     * @param file file that contains unresolved name (used for the purpose of statistics)
     * @param name offset that contains unresolved name (used for the purpose of statistics)
     */
    public final CsmClass getDummyForUnresolved(CharSequence[] nameTokens, CsmFile file, int offset) {
        if (Diagnostic.needStatistics()) {
            Diagnostic.onUnresolvedError(nameTokens, file, offset);
        }
        return getUnresolved().getDummyForUnresolved(nameTokens);
    }

    /**
     * Creates a dummy ClassImpl for unresolved name, stores in map.
     * Should be used only when restoring from persistence:
     * in contrary to getDummyForUnresolved(String[] nameTokens, CsmFile file, int offset),
     * it does not gather statistics!
     * @param nameTokens name
     */
    public final CsmClass getDummyForUnresolved(CharSequence name) {
        return getUnresolved().getDummyForUnresolved(name);
    }

    public final CsmNamespace getUnresolvedNamespace() {
        return getUnresolved().getUnresolvedNamespace();
    }

    public final CsmFile getUnresolvedFile() {
        return getUnresolved().getUnresolvedFile();
    }

    private Unresolved getUnresolved() {
        synchronized (unresolvedLock) {
            if (unresolved == null) {
                unresolved = new Unresolved(this);
            }
            return unresolved;
        }
    }

    @Override
    public final boolean isValid() {
        return platformProject != null && !isDisposing();
    }

    public void setDisposed() {
        disposing.set(true);
        synchronized (initializationTaskLock) {
            if (initializationTask != null) {
                initializationTask.cancel();
                initializationTask = null;
            }
        }
        unregisterProjectListeners();
        ParserQueue.instance().removeAll(this);
    }

    public final boolean isDisposing() {
        return disposing.get();
    }

    /**
     * called under disposeLock.writeLock() to clean up internals if needed
     */
    protected void onDispose() {
    }
    
    public final void dispose(final boolean cleanPersistent) {

        long time = 0;
        if (TraceFlags.TIMING) {
            System.err.printf("\n\nProject %s: disposing...\n", name);
            time = System.currentTimeMillis();
        }

        // just in case it wasn't called before (it's inexpensive)
        setDisposed();

        try {

            disposeLock.writeLock().lock();

            ProjectSettingsValidator validator = new ProjectSettingsValidator(this);
            validator.storeSettings();
            getUnresolved().dispose();
            RepositoryUtils.closeUnit(getUID(), getRequiredUnits(), cleanPersistent);
            onDispose();
            platformProject = null;
            unresolved = null;
            uid = null;
        } finally {
            disposeLock.writeLock().unlock();
        }

        if (TraceFlags.TIMING) {
            time = System.currentTimeMillis() - time;
            System.err.printf("Project %s: disposing took %d ms\n", name, time);
        }
    }

    protected final Set<CharSequence> getRequiredUnits() {
        Set<CharSequence> requiredUnits = new HashSet<CharSequence>();
        for (Key dependent : this.getLibrariesKeys()) {
            requiredUnits.add(dependent.getUnit());
        }
        return requiredUnits;
    }

//    private void disposeFiles() {
//        Collection<FileImpl> list = getFileContainer().getFileImpls();
//        getFileContainer().clear();
//        for (FileImpl file : list){
//            file.onProjectClose();
//            APTDriver.getInstance().invalidateAPT(file.getBuffer());
//        }
//        //clearNativeFileContainer();
//    }
    private int preventMultiplyDiagnosticExceptionsGlobalNamespace = 0;
    private NamespaceImpl _getGlobalNamespace() {
        NamespaceImpl ns = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(globalNamespaceUID);
        if (ns == null && preventMultiplyDiagnosticExceptionsGlobalNamespace < 5) {
            DiagnosticExceptoins.register(new IllegalStateException("Failed to get global namespace by key " + globalNamespaceUID)); // NOI18N
            preventMultiplyDiagnosticExceptionsGlobalNamespace++;
        }
        return ns != null ? ns : FAKE_GLOBAL_NAMESPACE;
    }

    private NamespaceImpl _getNamespace(CharSequence key) {
        key = CharSequences.create(key);
        CsmUID<CsmNamespace> nsUID = namespaces.get(key);
        NamespaceImpl ns = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(nsUID);
        return ns;
    }

    private void _registerNamespace(NamespaceImpl ns) {
        assert (ns != null);
        CharSequence key = ns.getQualifiedName();
        assert CharSequences.isCompact(key);
        CsmUID<CsmNamespace> nsUID = RepositoryUtils.<CsmNamespace>put(ns);
        assert nsUID != null;
        namespaces.put(key, nsUID);
    }

    private void _unregisterNamespace(NamespaceImpl ns) {
        assert (ns != null);
        assert !ns.isGlobal();
        CharSequence key = ns.getQualifiedName();
        assert CharSequences.isCompact(key);
        CsmUID<CsmNamespace> nsUID = namespaces.remove(key);
        assert nsUID != null;
        RepositoryUtils.remove(nsUID, ns);
    }

    protected final ModelImpl getModel() {
        return model;
    }

    public void onFileEditStart(FileBuffer buf, NativeFileItem nativeFile) {
    }

    public void onFileEditEnd(FileBuffer buf, NativeFileItem nativeFile, boolean undo) {
    }
    private CsmUID<CsmProject> uid = null;
    private final Object uidLock = new Object();
    
    @Override
    public final CsmUID<CsmProject> getUID() { // final because called from constructor
        CsmUID<CsmProject> out = uid;
        if (out == null) {
            synchronized (uidLock) {
                if (uid == null) {
                    uid = out = UIDUtilities.createProjectUID(this);
                    if (TraceFlags.TRACE_CPU_CPP) {System.err.println("getUID for project UID@"+System.identityHashCode(uid) + uid + "on prj@"+System.identityHashCode(this));}
                }
            }
        }
        return uid;
    }

    @Override
    public boolean isStable(CsmFile skipFile) {
        if (status == Status.Ready && !isDisposing()) {
            return !ParserQueue.instance().hasPendingProjectRelatedWork(this, (FileImpl) skipFile);
        }
        return false;
    }

    public final void onParseFinish() {
        onParseFinishImpl(false);
    }

    private void onParseFinishImpl(boolean libsAlreadyParsed) {
        FileImpl.incParseCount();
        synchronized (waitParseLock) {
            waitParseLock.notifyAll();
        }
        // it's ok to move the entire sycle into synchronized block,
        // because from inter-session persistence point of view,
        // if we don't fix fakes, we'll later consider that files are ok,
        // which is incorrect if there are some fakes
        try {
            disposeLock.readLock().lock();

            if (!isDisposing()) {
                if (!hasEditedFiles()) {
                    new FakeRegistrationWorker(this, disposing).fixFakeRegistration(libsAlreadyParsed);
                }
            }
        } catch (Exception e) {
            DiagnosticExceptoins.register(e);
        } finally {
            disposeLock.readLock().unlock();
            ProjectComponent.setStable(declarationsSorageKey);
            ProjectComponent.setStable(fileContainerKey);
            ProjectComponent.setStable(graphStorageKey);
            ProjectComponent.setStable(classifierStorageKey);
            checkStates(this, libsAlreadyParsed);

            if (!libsAlreadyParsed) {
                ParseFinishNotificator.onParseFinish(this);
            }
        }
        if (TraceFlags.PARSE_STATISTICS) {
            ParseStatistics.getInstance().printResults(this);
            ParseStatistics.getInstance().clear(this);
        }
    }

    private static void checkStates(ProjectBase prj, boolean libsAlreadyParsed){
        if (false) {
            System.err.println("Checking states for project "+prj.getName());
            for(Map.Entry<CharSequence, FileEntry> entry : prj.getFileContainer().getFileStorage().entrySet()){
                for(PreprocessorStatePair pair : entry.getValue().getStatePairs()){
                    if (!pair.state.isValid()){
                        System.err.println("Invalid state for file "+entry.getKey());
                    }
                }
            }
            if (libsAlreadyParsed) {
                for(CsmProject p : prj.getLibraries()){
                    if (p instanceof ProjectBase) {
                        checkStates((ProjectBase) p, false);
                    }
                }
            }
        }
    }

    /* collection to keep fake ASTs during parse phase */
    private final Map<CsmUID<CsmFile>, Map<CsmUID<FunctionImplEx<?>>, AST>> fakeASTs = new WeakHashMap<CsmUID<CsmFile>, Map<CsmUID<FunctionImplEx<?>>, AST>>();
    /*package*/final void trackFakeFunctionAST(CsmUID<CsmFile> fileUID, CsmUID<FunctionImplEx<?>> funUID, AST funAST) {
        synchronized (fakeASTs) {
            Map<CsmUID<FunctionImplEx<?>>, AST> fileASTs = fakeASTs.get(fileUID);
            if (fileASTs == null) {
                // create always
                fileASTs = new HashMap<CsmUID<FunctionImplEx<?>>, AST>();
                if (funAST != null) {
                    // remember new only if not null AST
                    fakeASTs.put(fileUID, fileASTs);
                }
            }
            if (funAST == null) {
                fileASTs.remove(funUID);
            } else {
                fileASTs.put(funUID, funAST);
            }
        }
    }

    /*package*/final void cleanAllFakeFunctionAST(CsmUID<CsmFile> fileUID) {
        synchronized (fakeASTs) {
            fakeASTs.remove(fileUID);
        }
    }

    /*package*/final void cleanAllFakeFunctionAST() {
        synchronized (fakeASTs) {
            fakeASTs.clear();
        }
    }

    /*package*/AST getFakeFunctionAST(CsmUID<CsmFile> fileUID, CsmUID<FunctionImplEx<?>> fakeUid) {
        synchronized (fakeASTs) {
            Map<CsmUID<FunctionImplEx<?>>, AST> fileASTs = fakeASTs.get(fileUID);
            return fileASTs == null ? null : fileASTs.get(fakeUid);
        }
    }

    /*package*/final void onLibParseFinish() {
        onParseFinishImpl(true);
    }

    /**
     * CsmProject implementation
     */
    @Override
    public final Collection<CsmFile> getAllFiles() {
        return getFileContainer().getFiles();
    }

    /**
     * CsmProject implementation
     */
    public final Collection<CsmUID<CsmFile>> getAllFilesUID() {
        return getFileContainer().getFilesUID();
    }

    public final Collection<CsmUID<CsmFile>> getHeaderFilesUID() {
        List<CsmUID<CsmFile>> uids = new ArrayList<CsmUID<CsmFile>>();
        for (FileImpl file : getAllFileImpls()) {
            if (!file.isSourceFile()) {
                uids.add(file.getUID());
            }
        }
        return uids;
    }
    /**
     * We'd better name this getFiles();
     * but unfortunately there already is such method,
     * and it is used intensively
     */
    public final Collection<FileImpl> getAllFileImpls() {
        return getFileContainer().getFileImpls();
    }

    @Override
    public final Collection<CsmFile> getSourceFiles() {
        List<CsmUID<CsmFile>> uids = new ArrayList<CsmUID<CsmFile>>();
        for (FileImpl file : getAllFileImpls()) {
            if (file.isSourceFile()) {
                uids.add(file.getUID());
            }
        }
        return new LazyCsmCollection<CsmFile, CsmFile>(uids, TraceFlags.SAFE_UID_ACCESS);
    }

    @Override
    public final Collection<CsmFile> getHeaderFiles() {
        return new LazyCsmCollection<CsmFile, CsmFile>(getHeaderFilesUID(), TraceFlags.SAFE_UID_ACCESS);
    }

    public final long getMemoryUsageEstimation() {
        //TODO: replace with some smart algorythm
        return getFileContainer().getSize();
    }

    @Override
    public final String toString() {
        return getName().toString() + ' ' + getClass().getName() + " @" + hashCode() + ":" + System.identityHashCode(this); // NOI18N
    }

    private volatile int hash = 0;

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = super.hashCode();
        }
        return hash;
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        return obj == this;
    }

    /**
     * Just a struct for the getStartEntryInfo return value:
     * if java allowed passing pointers by reference, we won't create this...
     */
    private static class /*struct*/ StartEntryInfo {

        private final APTPreprocHandler preprocHandler;
        private final ProjectBase startProject;
        private final FileImpl csmFile;

        public StartEntryInfo(APTPreprocHandler preprocHandler, ProjectBase startProject, FileImpl csmFile) {
            this.preprocHandler = preprocHandler;
            this.startProject = startProject;
            this.csmFile = csmFile;
        }
    }

    private StartEntryInfo getStartEntryInfo(APTPreprocHandler preprocHandler, APTPreprocHandler.State state) {
        StartEntry startEntry = APTHandlersSupport.extractStartEntry(state);
        ProjectBase startProject = Utils.getStartProject(startEntry);
        FileImpl csmFile = startProject == null ? null : startProject.getFile(startEntry.getStartFile(), false);
        if (csmFile != null) {
            NativeFileItem nativeFile = csmFile.getNativeFileItem();
            if (nativeFile != null && nativeFile.getFileObject() != null && nativeFile.getFileObject().isValid()) {
                preprocHandler = startProject.createPreprocHandler(nativeFile);
            }
        }
        return new StartEntryInfo(preprocHandler, startProject, csmFile);
    }

    private APTPreprocHandler restorePreprocHandler(CharSequence interestedFile, APTPreprocHandler preprocHandler, APTPreprocHandler.State state) {
        assert state != null;
        assert state.isCleaned();
        // walk through include stack to restore preproc information
        LinkedList<APTIncludeHandler.IncludeInfo> reverseInclStack = APTHandlersSupport.extractIncludeStack(state);
        assert (reverseInclStack != null);
        if (reverseInclStack.isEmpty()) {
            if (TRACE_PP_STATE_OUT) {
                System.err.println("stack is empty; return default for " + interestedFile);
            }
            return getStartEntryInfo(preprocHandler, state).preprocHandler;
        } else {
            if (TRACE_PP_STATE_OUT) {
                System.err.println("restoring for " + interestedFile);
            }
            return restorePreprocHandlerFromIncludeStack(reverseInclStack, interestedFile, preprocHandler, state);
        }
    }

    protected final APTPreprocHandler restorePreprocHandlerFromIncludeStack(LinkedList<APTIncludeHandler.IncludeInfo> reverseInclStack, 
            CharSequence interestedFile, APTPreprocHandler preprocHandler, APTPreprocHandler.State state) {
        // we need to reverse includes stack
        assert (!reverseInclStack.isEmpty()) : "state of stack is " + reverseInclStack;
        LinkedList<APTIncludeHandler.IncludeInfo> inclStack = Utils.reverse(reverseInclStack);
        StartEntryInfo sei = getStartEntryInfo(preprocHandler, state);
        FileImpl csmFile = sei.csmFile;
        ProjectBase startProject = sei.startProject;
        preprocHandler = sei.preprocHandler;

        APTFile aptLight = null;
        try {
            aptLight = csmFile == null ? null : getAPTLight(csmFile);
        } catch (IOException ex) {
            System.err.println("can't restore preprocessor state for " + interestedFile + //NOI18N
                    "\nreason: " + ex.getMessage());//NOI18N
            DiagnosticExceptoins.register(ex);
        }
        boolean ppStateRestored = false;
        if (aptLight != null) {
            // for testing remember restored file
            long time = REMEMBER_RESTORED ? System.currentTimeMillis() : 0;
            int stackSize = inclStack.size();
            // create concurrent entry if absent
            APTFileCacheEntry cacheEntry = csmFile.getAPTCacheEntry(preprocHandler, Boolean.FALSE);
            APTWalker walker = new APTRestorePreprocStateWalker(startProject, aptLight, csmFile, preprocHandler, inclStack, FileContainer.getFileKey(interestedFile, false).toString(), cacheEntry);
            walker.visit();
            // we do not remember cache entry because it is stopped before end of file
            // fileImpl.setAPTCacheEntry(handler, cacheEntry, false);

            if (preprocHandler.isValid()) {
                if (REMEMBER_RESTORED) {
                    if (testRestoredFiles == null) {
                        testRestoredFiles = new ArrayList<String>();
                    }
                    FileImpl interestedFileImpl = getFile(interestedFile, false);
                    assert interestedFileImpl != null;
                    String msg = interestedFile + " [" + (interestedFileImpl.isHeaderFile() ? "H" : interestedFileImpl.isSourceFile() ? "S" : "U") + "]"; // NOI18N
                    time = System.currentTimeMillis() - time;
                    msg = msg + " within " + time + "ms" + " stack " + stackSize + " elems"; // NOI18N
                    System.err.println("#" + testRestoredFiles.size() + " restored: " + msg); // NOI18N
                    testRestoredFiles.add(msg);
                }
                if (TRACE_PP_STATE_OUT) {
                    System.err.println("after restoring " + preprocHandler); // NOI18N
                }
                ppStateRestored = true;
            }
        }
        if (!ppStateRestored) {
            // need to recover from the problem, when start file is invalid or absent
            // try to find project who can create default handler with correct
            // compiler settings
            // preferences is start project
            if (startProject == null) {
                // otherwise use the project owner
                startProject = this;
            }
            preprocHandler = startProject.createDefaultPreprocHandler(interestedFile);
            // remember
            // TODO: file container should accept all without checks
            // otherwise state will not be replaced
//                synchronized (getFileContainer().getLock(interestedFile)) {
//                    if (state.equals(getPreprocState(interestedFile))) {
//                        APTPreprocHandler.State recoveredState = preprocHandler.getState();
//                        assert !recoveredState.isCompileContext();
//                        putPreprocState(interestedFile, recoveredState);
//                    }
//                }
        }
        return preprocHandler;        
    }
    
    private NativeProject findNativeProjectHolder(Set<ProjectBase> visited) {
        visited.add(this);
        NativeProject nativeProject = ModelSupport.getNativeProject(getPlatformProject());
        if (nativeProject == null) {
            // try to find dependent projects and ask them
            for (ProjectBase dependentPrj : getDependentProjects()) {
                if (!visited.contains(dependentPrj)) {
                    nativeProject = dependentPrj.findNativeProjectHolder(visited);
                    if (nativeProject != null) {
                        // found
                        break;
                    }
                }
            }
        }
        return nativeProject;
    }

    private APTPreprocHandler createDefaultPreprocHandler(CharSequence interestedFile) {
        NativeProject nativeProject = findNativeProjectHolder(new HashSet<ProjectBase>(10));
        APTPreprocHandler out;
        if (nativeProject != null) {
            // we have own native project to get settings from
            NativeFileItem item = new DefaultFileItem(nativeProject, interestedFile.toString());
            out = createPreprocHandler(item);
        } else {
            out = createEmptyPreprocHandler(interestedFile);
        }
        assert out != null : "failed creating default ppState for " + interestedFile;
        return out;
    }

    public final APTFile getAPTLight(CsmFile csmFile) throws IOException {
        FileImpl fileImpl = (FileImpl) csmFile;
        APTFile aptLight = fileImpl.getFileAPT(false);
        if (aptLight != null && APTUtils.LOG.isLoggable(Level.FINE)) {
            CharSequence guardMacro = aptLight.getGuardMacro();
            if (guardMacro.length() == 0 && !fileImpl.isSourceFile()) {
                APTUtils.LOG.log(Level.FINE, "FileImpl: file {0} does not have guard", new Object[]{fileImpl.getAbsolutePath()});// NOI18N
            }
        }

        return aptLight;
    }

    public final GraphContainer getGraph() {
        return getGraphStorage();
    }
    
    /**
     * gets all files that direct or indirect include the referenced file.
     */
    public final Set<CsmFile> getParentFiles(CsmFile referencedFile) {
        return getGraphStorage().getParentFiles(referencedFile);
    }
    
    private final static class DefaultFileItem implements NativeFileItem {

        private final NativeProject project;
        private final String normalizedAbsPath;

        public DefaultFileItem(NativeProject project, String absolutePath) {
            Parameters.notNull("project", project);
            Parameters.notNull("absolutePath", absolutePath);
            this.project = project;
            this.normalizedAbsPath = CndFileUtils.normalizeAbsolutePath(project.getFileSystem(), absolutePath);
        }

        public DefaultFileItem(NativeFileItem nativeFile) {
            Parameters.notNull("nativeFile", nativeFile);
            this.project = nativeFile.getNativeProject();
            this.normalizedAbsPath = nativeFile.getAbsolutePath(); // always normalized
            CndUtils.assertNormalized(project.getFileSystem(), normalizedAbsPath);
            Parameters.notNull("nativeFile.getAbsolutePath()", normalizedAbsPath);
        }

        public static NativeFileItem toDefault(NativeFileItem nativeFile) {
            // if not already fake
            if (!(nativeFile instanceof DefaultFileItem)) {
                nativeFile = new DefaultFileItem(nativeFile);
            }
            return nativeFile;
        }

        @Override
        public List<String> getUserMacroDefinitions() {
            if (project != null) {
                return project.getUserMacroDefinitions();
            }
            return Collections.<String>emptyList();
        }

        @Override
        public List<FSPath> getUserIncludePaths() {
            if (project != null) {
                return project.getUserIncludePaths();
            }
            return Collections.<FSPath>emptyList();
        }

        @Override
        public List<String> getSystemMacroDefinitions() {
            if (project != null) {
                return project.getSystemMacroDefinitions();
            }
            return Collections.<String>emptyList();
        }

        @Override
        public List<FSPath> getSystemIncludePaths() { 
            if (project != null) {
                return project.getSystemIncludePaths();
            }
            return Collections.<FSPath>emptyList();
        }

        @Override
        public NativeProject getNativeProject() {
            return project;
        }

        @Override
        public FileObject getFileObject() {
            return CndFileUtils.toFileObject(project.getFileSystem(), normalizedAbsPath);
        }

        @Override
        public String getAbsolutePath() {
            return normalizedAbsPath;
        }

        @Override
        public String getName() {
            return CndPathUtilitities.getBaseName(normalizedAbsPath);
        }
        
        @Override
        public Language getLanguage() {
            return NativeFileItem.Language.C_HEADER;
        }

        @Override
        public LanguageFlavor getLanguageFlavor() {
            return NativeFileItem.LanguageFlavor.UNKNOWN;
        }

        @Override
        public boolean isExcluded() {
            return false;
        }

        @Override
        public String toString() {
            return normalizedAbsPath + ' ' + project.getFileSystem().getDisplayName(); //NOI18N
        }
    }

    /**
     * Represent the project status.
     *
     * Concerns only initial stage of project life cycle:
     * allows to distinguish just newly-created project,
     * the phase when files are being added to project (and to parser queue)
     * and the phase when all files are already added.
     *
     * It isn't worth tracking further stages (stable/unstable)
     * since it's error prone (it's better to ask, say, parser queue
     * whether it contains files that belong to this project or not)
     */
    protected static enum Status {

        Initial,
        Restored,
        AddingFiles,
        Validating,
        Ready;
    }
    private volatile Status status;
    /** The task that is run in a request processor during project initialization */
    private Cancellable initializationTask;
    /** The lock under which the initializationTask is set */
    private static final class InitializationTaskLock {}
    private final Object initializationTaskLock = new InitializationTaskLock();
    private static final class WaitParseLock {}
    private final Object waitParseLock = new WaitParseLock();
    // to profile monitor usages
    private static final class ClassifierReplaceLock {}
    private final Object classifierReplaceLock = new ClassifierReplaceLock();
    private ModelImpl model;
    private Unresolved unresolved;
    private final Object unresolvedLock = new Object();
    
    private CharSequence name;
    private CsmUID<CsmNamespace> globalNamespaceUID;
    private NamespaceImpl FAKE_GLOBAL_NAMESPACE;
    private volatile Object platformProject;
    private final FileSystem fileSystem;

    private boolean hasFileSystemProblems;
    private final Object fileSystemProblemsLock = new Object();
    
    /**
     * Some notes concerning disposing and disposeLock fields.
     *
     * The purpose is not to perform some actions
     * (such as adding new files, continuing initialization, etc)
     * when the project is going to be disposed.
     *
     * The disposing field is changed only once,
     * from false to true (in setDispose() method)
     *
     * When it is changed to true, no lock is acquired, BUT:
     * it is guaranteed that events take place in the following order:
     * 1) disposing is set to true
     * 2) the disposeLock.writeLock() is locked after that
     * and remains locked during the entire project closure.
     *
     * Clients who need to check this, are obliged to
     * act in the following sequence:
     * 1) require disposeLock.readLock()
     * 2) check that the disposing field is still false
     * 3) keep disposeLock.readLock() locked
     * while performing critical actions
     * (the actions that should not be done
     * when the project is being disposed)
     *
     */
    private final AtomicBoolean disposing = new AtomicBoolean(false);
    private final ReadWriteLock disposeLock = new ReentrantReadWriteLock();
    private final CharSequence uniqueName;
    private final Map<CharSequence, CsmUID<CsmNamespace>> namespaces;
    private final Key classifierStorageKey;

    // collection of sharable system macros and system includes
    private final APTSystemStorage sysAPTData;
    private final APTIncludePathStorage userPathStorage;
    private static final class NamespaceLock {}
    private final Object namespaceLock = new NamespaceLock();
    private final Key declarationsSorageKey;
    private final Key fileContainerKey;
    private static final class FileContainerLock {}
    private final Object fileContainerLock = new FileContainerLock();
    private final Key graphStorageKey;
    private NativeProjectListenerImpl projectListener;
    private final Object projectListenerLock = new Object();
    
    // test variables.
    private static final boolean TRACE_PP_STATE_OUT = DebugUtils.getBoolean("cnd.dump.preproc.state", false); // NOI18N
    private static final boolean REMEMBER_RESTORED = TraceFlags.CLEAN_MACROS_AFTER_PARSE && (DebugUtils.getBoolean("cnd.remember.restored", false) || TRACE_PP_STATE_OUT);// NOI18N
    public static final int GATHERING_MACROS = 0;
    public static final int GATHERING_TOKENS = 1;

    ////////////////////////////////////////////////////////////////////////////
    /**
     * for tests only
     */
    public static List<String> testGetRestoredFiles() {
        return testRestoredFiles;
    }
    private static volatile List<String> testRestoredFiles = null;
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        assert aStream != null;
        PersistentUtils.writeFileSystem(fileSystem, aStream);
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        assert aFactory != null;
        assert this.name != null;
        PersistentUtils.writeUTF(name, aStream);
        //PersistentUtils.writeUTF(RepositoryUtils.getUnitName(getUID()), aStream);
        aFactory.writeUID(this.globalNamespaceUID, aStream);
        aFactory.writeStringToUIDMap(this.namespaces, aStream, false);

        ProjectComponent.writeKey(fileContainerKey, aStream);
        ProjectComponent.writeKey(declarationsSorageKey, aStream);
        ProjectComponent.writeKey(graphStorageKey, aStream);
        ProjectComponent.writeKey(classifierStorageKey, aStream);
        this.includedFileContainer.write(aStream);

        PersistentUtils.writeUTF(this.uniqueName, aStream);
        aStream.writeBoolean(hasFileSystemProblems);
    }

    protected ProjectBase(RepositoryDataInput aStream) throws IOException {

        fileSystem = PersistentUtils.readFileSystem(aStream);
        sysAPTData = APTSystemStorage.getInstance();
        userPathStorage = new APTIncludePathStorage();

        setStatus(Status.Restored);

        assert aStream != null;
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        assert aFactory != null : "default UID factory can not be bull";

        this.name = PersistentUtils.readUTF(aStream, ProjectNameCache.getManager());
        assert this.name != null : "project name can not be null";

        //CharSequence unitName = PersistentUtils.readUTF(aStream, DefaultCache.getManager());

        this.globalNamespaceUID = aFactory.readUID(aStream);
        assert globalNamespaceUID != null : "globalNamespaceUID can not be null";

        int collSize = aStream.readInt();
        if (collSize <= 0) {
            namespaces = new ConcurrentHashMap<CharSequence, CsmUID<CsmNamespace>>(0);
        } else {
            namespaces = new ConcurrentHashMap<CharSequence, CsmUID<CsmNamespace>>(collSize);
        }
        aFactory.readStringToUIDMap(this.namespaces, aStream, QualifiedNameCache.getManager(), collSize);

        fileContainerKey = ProjectComponent.readKey(aStream);
        assert fileContainerKey != null : "fileContainerKey can not be null";
        weakFileContainer = new WeakContainer<FileContainer>(this, fileContainerKey);

        declarationsSorageKey = ProjectComponent.readKey(aStream);
        assert declarationsSorageKey != null : "declarationsSorageKey can not be null";
        weakDeclarationContainer = new WeakContainer<DeclarationContainerProject>(this, declarationsSorageKey);

        graphStorageKey = ProjectComponent.readKey(aStream);
        assert graphStorageKey != null : "graphStorageKey can not be null";
        weakGraphContainer = new WeakContainer<GraphContainer>(this, graphStorageKey);

        classifierStorageKey = ProjectComponent.readKey(aStream);
        assert classifierStorageKey != null : "classifierStorageKey can not be null";
        weakClassifierContainer = new WeakContainer<ClassifierContainer>(this, classifierStorageKey);

        includedFileContainer = new IncludedFileContainer(this, aStream);
        
        uniqueName = PersistentUtils.readUTF(aStream, ProjectNameCache.getManager());
        assert uniqueName != null : "uniqueName can not be null";

        this.model = (ModelImpl) CsmModelAccessor.getModel();

        this.FAKE_GLOBAL_NAMESPACE = NamespaceImpl.create(this, true);
        this.hasFileSystemProblems = aStream.readBoolean();
    }

    private final WeakContainer<DeclarationContainerProject> weakDeclarationContainer;
    private DeclarationContainerProject getDeclarationsSorage() {
        DeclarationContainerProject dc = weakDeclarationContainer.getContainer();
        return dc != null ? dc : DeclarationContainerProject.empty();
    }

    private final WeakContainer<FileContainer> weakFileContainer;
    private FileContainer getFileContainer() {
        FileContainer fc = weakFileContainer.getContainer();
        return fc != null ? fc : FileContainer.empty();
    }

    private final WeakContainer<GraphContainer> weakGraphContainer;
    public final GraphContainer getGraphStorage() {
        GraphContainer gc = weakGraphContainer.getContainer();
        return gc != null ? gc : GraphContainer.empty();
    }

    private final WeakContainer<ClassifierContainer> weakClassifierContainer;
    private ClassifierContainer getClassifierSorage() {
        ClassifierContainer cc = weakClassifierContainer.getContainer();
        return cc != null ? cc : ClassifierContainer.empty();
    }

    public static void dumpProjectContainers(PrintStream printStream, CsmProject prj, boolean dumpFiles) {
        ProjectBase project = (ProjectBase) prj;
        dumpProjectClassifierContainer(project, printStream, !dumpFiles);
        dumpProjectDeclarationContainer(project, printStream);
        if (dumpFiles) {
            ProjectBase.dumpFileContainer(project, new PrintWriter(printStream));
            ProjectBase.dumpProjectGrapthContainer(project, printStream);
        }
    }

    /*package*/static void dumpProjectGrapthContainer(ProjectBase project, PrintStream printStream) {
        GraphContainer container = project.getGraphStorage();
        Map<CharSequence, CsmFile> map = new TreeMap<CharSequence, CsmFile>();
        for (CsmFile f : project.getAllFiles()) {
            map.put(f.getAbsolutePath(), f);
        }
        for (CsmFile file : map.values()) {
            printStream.println("\n========== Dumping links for file " + file.getAbsolutePath()); // NOI18N
            Map<CharSequence, CsmFile> set = new TreeMap<CharSequence, CsmFile>();
            for (CsmFile f : container.getInLinks(file)) {
                set.put(f.getAbsolutePath(), (FileImpl) f);
            }
            if (set.size() > 0) {
                printStream.println("\tInput"); // NOI18N
                for (CsmFile f : set.values()) {
                    printStream.println("\t\t" + f.getAbsolutePath()); // NOI18N
                }
                set.clear();
            }
            for (CsmFile f : container.getOutLinks(file)) {
                set.put(f.getAbsolutePath(), (FileImpl) f);
            }
            if (set.size() > 0) {
                printStream.println("\tOutput"); // NOI18N
                for (CsmFile f : set.values()) {
                    printStream.println("\t\t" + f.getAbsolutePath()); // NOI18N
                }
            }
        }
    }

    /*package*/static void dumpProjectClassifierContainer(ProjectBase project, PrintStream printStream, boolean offsetString) {
        ClassifierContainer container = project.getClassifierSorage();
        for (int phase = 0; phase < 3; phase++) {
            Map<CharSequence, CsmClassifier> map = null;
            switch (phase) {
                case 0:
                    printStream.printf("\n========== Dumping %s Project Classifiers\n", project.getName());//NOI18N
                    map = container.getTestClassifiers();
                    break;
                case 1:
                    printStream.printf("\n========== Dumping %s Project Short Classifiers\n", project.getName());//NOI18N
                    map = container.getTestShortClassifiers();
                    break;
                case 2:
                    printStream.printf("\n========== Dumping %s Project Typedefs\n", project.getName());//NOI18N
                    map = container.getTestTypedefs();
                    break;
            }
            for (Map.Entry<CharSequence, CsmClassifier> entry : map.entrySet()) {
                printStream.print("\t" + entry.getKey().toString() + " ");//NOI18N
                CsmClassifier value = entry.getValue();
                if (value == null) {
                    printStream.println("null");//NOI18N
                } else {
                    String pos = offsetString ? CsmTracer.getOffsetString(value, true) : "";//NOI18N
                    printStream.printf("%s %s\n", value.getUniqueName(), pos);//NOI18N
                }
            }
        }
    }

    /*package*/ static void dumpProjectDeclarationContainer(ProjectBase project, PrintStream printStream) {
        printStream.println("\n========== Dumping Project declarations");//NOI18N
        DeclarationContainerProject container = project.getDeclarationsSorage();
        for (Map.Entry<CharSequence, Object> entry : container.getTestDeclarations().entrySet()) {
            printStream.println("\t" + entry.getKey().toString());//NOI18N
            TreeMap<CharSequence, CsmDeclaration> set = new TreeMap<CharSequence, CsmDeclaration>();
            Object o = entry.getValue();
            if (o instanceof CsmUID<?>[]) {
                // we know the template type to be CsmDeclaration
                @SuppressWarnings("unchecked") // checked //NOI18N
                CsmUID<CsmDeclaration>[] uids = (CsmUID<CsmDeclaration>[]) o;
                for (CsmUID<CsmDeclaration> uidt : uids) {
                    final CsmDeclaration object = uidt.getObject();
                    if (object != null) {
                        set.put(((CsmOffsetableDeclaration) object).getContainingFile().getAbsolutePath(), object);
                    } else {
                        printStream.println("\tNO OBJECT FOR " + entry.getKey().toString() + "\n\t"+uidt);//NOI18N
                    }
                }
            } else if (o instanceof CsmUID<?>) {
                // we know the template type to be CsmDeclaration
                @SuppressWarnings("unchecked") // checked //NOI18N
                CsmUID<CsmDeclaration> uidt = (CsmUID<CsmDeclaration>) o;
                final CsmDeclaration object = uidt.getObject();
                if (object != null) {
                    set.put(((CsmOffsetableDeclaration) object).getContainingFile().getAbsolutePath(), object);
                } else {
                    printStream.println("\tNO OBJECT FOR " + entry.getKey().toString() + "\n\t" + uidt);//NOI18N
                }
            }
            for (Map.Entry<CharSequence, CsmDeclaration> f : set.entrySet()) {
                printStream.println("\t\t" + f.getValue() + " from " + f.getKey()); //NOI18N
            }
        }
        printStream.println("\n========== Dumping Project friends");//NOI18N
        for (Map.Entry<CharSequence, Set<CsmUID<CsmFriend>>> entry : container.getTestFriends().entrySet()) {
            printStream.println("\t" + entry.getKey().toString());//NOI18N
            TreeMap<CharSequence, CsmFriend> set = new TreeMap<CharSequence, CsmFriend>();
            for (CsmUID<? extends CsmFriend> uid : entry.getValue()) {
                CsmFriend f = uid.getObject();
                set.put(f.getQualifiedName(), f);
            }
            for (Map.Entry<CharSequence, CsmFriend> f : set.entrySet()) {
                printStream.println("\t\t" + f.getKey() + " " + f.getValue());//NOI18N
            }
        }
    }

    public static void dumpFileContainer(CsmProject project, PrintWriter printStream) {
        FileContainer fileContainer = ((ProjectBase) project).getFileContainer();
        printStream.println("\n========== Dumping File container"); // NOI18N
        Map<CharSequence, Object/*CharSequence or CharSequence[]*/> names = fileContainer.getCanonicalNames();
        //for unit test only
        Map<CharSequence, FileEntry> files = fileContainer.getFileStorage();
        for(Map.Entry<CharSequence, FileEntry> entry : files.entrySet()){
            CharSequence key = entry.getKey();
            printStream.println("\tFile "+key.toString()); // NOI18N
            Object name = names.get(key);
            if (name instanceof CharSequence[]) {
                for(CharSequence alt : (CharSequence[])name) {
                    printStream.println("\t\tAlias "+alt.toString()); // NOI18N
                }
            } else if (name instanceof CharSequence) {
                printStream.println("\t\tAlias "+name.toString()); // NOI18N
            }
            FileEntry file = entry.getValue();
            CsmFile csmFile = file.getTestFileUID().getObject();
            printStream.println("\t\tModel File "+csmFile.getAbsolutePath()); // NOI18N
            printStream.println("\t\tNumber of states "+file.getPrerocStates().size()); // NOI18N
            for (PreprocessorStatePair statePair : file.getStatePairs()) {
                StringTokenizer st = new StringTokenizer(FilePreprocessorConditionState.toStringBrief(statePair.pcState),"\n"); // NOI18N
                boolean first = true;
                while (st.hasMoreTokens()) {
                    if (first) {
                        printStream.println("\t\tState "+st.nextToken()); // NOI18N
                        first = false;
                    } else {
                        printStream.println("\t\t\t"+st.nextToken()); // NOI18N
                    }
                }
            }
        }
    }

}
