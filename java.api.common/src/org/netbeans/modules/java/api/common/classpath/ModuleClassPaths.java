/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.api.common.classpath;

import com.sun.source.tree.ModuleTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.lang.model.element.ModuleElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import static org.netbeans.spi.java.classpath.ClassPathImplementation.PROP_RESOURCES;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
final class ModuleClassPaths {
    private static final Logger LOG = Logger.getLogger(ModuleClassPaths.class.getName());

    private ModuleClassPaths() {
        throw new IllegalArgumentException("No instance allowed."); //NOI18N
    }

    @NonNull
    static ClassPathImplementation createModuleInfoBasedPath(
            @NonNull final ClassPath systemModules,
            @NonNull final SourceRoots sourceRoots) {
        return new ModuleInfoClassPathImplementation(
                systemModules,
                sourceRoots,
                null,
                null);
    }

    @NonNull
    static ClassPathImplementation createModuleInfoBasedPath(
            @NonNull final ClassPath modulePath,
            @NonNull final SourceRoots sourceRoots,
            @NonNull final ClassPath systemModules,
            @NonNull final ClassPath legacyClassPath) {
        Parameters.notNull("systemModules", systemModules); //NOI18N
        Parameters.notNull("legacyClassPath", legacyClassPath); //NOI18N
        return new ModuleInfoClassPathImplementation(
                modulePath,
                sourceRoots,
                systemModules,
                legacyClassPath);
    }

    @NonNull
    static ClassPathImplementation createPlatformModulePath(
            @NonNull final PropertyEvaluator eval,
            @NonNull final String platformType) {
        return new PlatformModulePath(eval, platformType);
    }

    @NonNull
    static ClassPathImplementation createPropertyBasedModulePath(
            @NonNull final File projectDir,
            @NonNull final PropertyEvaluator eval,
            @NonNull final String... props) {
        return new PropertyModulePath(projectDir, eval, props);
    }

    private static final class PlatformModulePath extends BaseClassPathImplementation implements PropertyChangeListener {
        private static final String PLATFORM_ANT_NAME = "platform.ant.name";    //NOI18N
        private static final String PROTOCOL_NBJRT = "nbjrt";   //NOI18N

        private final PropertyEvaluator eval;
        private final String platformType;

        PlatformModulePath(
                @NonNull final PropertyEvaluator eval,
                @NonNull final String platformType) {
            Parameters.notNull("evel", eval);   //NOI18N
            Parameters.notNull("platformType", platformType);   //NOI18N
            this.eval = eval;
            this.platformType = platformType;
            this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
            final JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            jpm.addPropertyChangeListener(WeakListeners.propertyChange(this, jpm));
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            List<PathResourceImplementation> res = getCache();
            if (res != null) {
                return res;
            }
            res = createResources();
            synchronized (this) {
                assert res != null;
                if (getCache() == null) {
                    setCache(res);
                } else {
                    res = getCache();
                }
            }
            return res;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null ||
                ProjectProperties.PLATFORM_ACTIVE.equals(propName) ||
                (JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(propName) && isActivePlatformChange())) {
                resetCache(true);
            }
        }

        private boolean isActivePlatformChange() {
            List<PathResourceImplementation> current = getCache();
            if (current == null) {
                return false;
            }
            final Stream<JavaPlatform> platforms = getPlatforms();
            return platforms.findFirst().isPresent() ?
                current.isEmpty() :
                !current.isEmpty();
        }

        private List<PathResourceImplementation> createResources() {
            final List<PathResourceImplementation> res = new ArrayList<>();
            getPlatforms()
                .flatMap((plat)->plat.getBootstrapLibraries().entries().stream())
                .map((entry) -> entry.getURL())
                .filter((root) -> (PROTOCOL_NBJRT.equals(root.getProtocol())))
                .forEach((root)->{res.add(org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(root));});
            return res;
        }

        @NonNull
        private Stream<JavaPlatform> getPlatforms() {
            final String platformName = eval.getProperty(ProjectProperties.PLATFORM_ACTIVE);
            return platformName != null && !platformName.isEmpty() ?
                    Arrays.stream(JavaPlatformManager.getDefault().getInstalledPlatforms())
                        .filter((plat)->
                            platformName.equals(plat.getProperties().get(PLATFORM_ANT_NAME)) &&
                            platformType.equals(plat.getSpecification().getName())) :
                    Stream.empty();
        }
    }

    private static final class PropertyModulePath extends BaseClassPathImplementation implements PropertyChangeListener, FileChangeListener {
        private final File projectDir;
        private final PropertyEvaluator eval;
        private final Set<String> props;
        //@GuardedBy("this")
        private final Set<File> listensOn;

        PropertyModulePath(
            @NonNull final File projectDir,
            @NonNull final PropertyEvaluator eval,
            @NonNull final String... props) {
            Parameters.notNull("projectDir", projectDir);   //NOI18N
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("props", props); //NOI18N
            this.projectDir = projectDir;
            this.eval = eval;
            this.props = new LinkedHashSet<>();
            this.listensOn = new HashSet<>();
            Collections.addAll(this.props, props);
            this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
        }

        @Override
        @NonNull
        public List<? extends PathResourceImplementation> getResources() {
            List<PathResourceImplementation> res = getCache();
            if (res == null) {
                final List<PathResourceImplementation> collector = res = new ArrayList<>();
                final Collection<File> modulePathRoots = new HashSet<>();
                props.stream()
                    .map((prop)->eval.getProperty(prop))
                    .flatMap((path)-> {
                        return path == null ?
                            Collections.<String>emptyList().stream() :
                            Arrays.stream(PropertyUtils.tokenizePath(path));
                    })
                    .map((part)->PropertyUtils.resolveFile(projectDir, part))
                    .flatMap((modulePathEntry) -> {
                        if (isArchiveFile(modulePathEntry)) {
                            return Stream.of(modulePathEntry);
                        } else {
                            modulePathRoots.add(modulePathEntry);
                            return findModules(modulePathEntry);
                        }
                    })
                    .forEach((file)->{
                        URL url = FileUtil.urlForArchiveOrDir(file);
                        if (url != null) {
                            collector.add(org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(url));
                        }
                    });
                synchronized (this) {
                    List<PathResourceImplementation> cv = getCache();
                    if (cv == null) {
                        final Set<File> toAdd = new HashSet<>(modulePathRoots);
                        final Set<File> toRemove = new HashSet<>(listensOn);
                        toAdd.removeAll(listensOn);
                        toRemove.removeAll(modulePathRoots);
                        for (File f : toRemove) {
                            FileUtil.removeFileChangeListener(this, f);
                            listensOn.remove(f);
                        }
                        for (File f : toAdd) {
                            FileUtil.addFileChangeListener(this, f);
                            listensOn.add(f);
                        }
                        LOG.log(
                            Level.FINE,
                            "{0} setting results: {1}, listening on: {2}",    //NOI18N
                            new Object[]{
                                getClass().getSimpleName(),
                                res,
                                listensOn
                            });
                        setCache(res);
                    } else {
                        res = cv;
                    }
                }
            }
            return res;
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null || props.contains(propName)) {
                LOG.log(
                    Level.FINER,
                    "{0} propertyChange: {1}",    //NOI18N
                    new Object[]{
                        getClass().getSimpleName(),
                        propName
                    });
                resetCache(true);
            }
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            handleFileEvent(fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            handleFileEvent(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            handleFileEvent(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            handleFileEvent(fe);
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
        }

        private void handleFileEvent(@NonNull final FileEvent fe) {
            LOG.log(
                Level.FINER,
                "{0} file event: {1}",    //NOI18N
                new Object[]{
                    getClass().getSimpleName(),
                    fe.getFile()
                });
            resetCache(true);
        }

        private static boolean isArchiveFile(@NonNull final File file) {
            try {
                return FileUtil.isArchiveFile(BaseUtilities.toURI(file).toURL());
            } catch (MalformedURLException mue) {
                LOG.log(
                        Level.WARNING,
                        "Invalid URL for: {0}",
                        file);
                return false;
            }
        }

        @NonNull
        private static Stream<File> findModules(@NonNull final File modulesFolder) {
            //No project's dist folder do File.list
            File[] modules = modulesFolder.listFiles();
            return modules == null ?
               Collections.<File>emptyList().stream():
               Arrays.stream(modules);
        }
    }

    private static final class ModuleInfoClassPathImplementation  extends BaseClassPathImplementation implements PropertyChangeListener, FileChangeListener, ClassIndexListener {

        private static final String MODULE_INFO_JAVA = "module-info.java";   //NOI18N
        private static final String MOD_JAVA_BASE = "java.base";    //NOI18N
        private static final List<PathResourceImplementation> TOMBSTONE = Collections.unmodifiableList(new ArrayList<>());
        private final ClassPath base;
        private final SourceRoots sources;
        private final ClassPath systemModules;
        private final ClassPath legacyClassPath;
        private final ThreadLocal<Object[]> selfRes;

        //@GuardedBy("this")
        private ClasspathInfo activeProjectSourceRoots;
        //@GuardedBy("this")
        private volatile boolean rootsChanging;
        //@GuardedBy("this")
        private Collection<File> moduleInfos;

        ModuleInfoClassPathImplementation(
                @NonNull final ClassPath base,
                @NonNull final SourceRoots sources,
                @NullAllowed final ClassPath systemModules,
                @NullAllowed final ClassPath legacyClassPath) {
            super(null);
            Parameters.notNull("base", base);       //NOI18N
            Parameters.notNull("sources", sources); //NOI18N
            this.base = base;
            this.sources = sources;
            this.systemModules = systemModules;
            this.legacyClassPath = legacyClassPath;
            this.selfRes = new ThreadLocal<>();
            this.moduleInfos = Collections.emptyList();
            this.base.addPropertyChangeListener(WeakListeners.propertyChange(this, this.base));
            this.sources.addPropertyChangeListener(WeakListeners.propertyChange(this, this.sources));
            if (this.legacyClassPath != null) {
                this.legacyClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this, this.legacyClassPath));
            }
        }

        @Override
        @NonNull
        public List<? extends PathResourceImplementation> getResources() {
            List<PathResourceImplementation> res = getCache();
            boolean needToFire = false;
            if (res == TOMBSTONE) {
                needToFire = true;
                res = null;
            }
            if (res != null) {
                return res;
            }
            final Object[] bestSoFar = selfRes.get();
            if (bestSoFar != null) {
                bestSoFar[1] = Boolean.TRUE;
                return (List<? extends PathResourceImplementation>) bestSoFar[0];
            }
            final Collection<File> newModuleInfos = new ArrayDeque<>();
            final List<URL> newActiveProjectSourceRoots = new ArrayList<>();
            final Map<String,URL> modulesByName = getModulesByName(base, newActiveProjectSourceRoots);
            synchronized (this) {
                if (activeProjectSourceRoots != null) {
                    activeProjectSourceRoots.getClassIndex().removeClassIndexListener(this);
                    activeProjectSourceRoots = null;
                }
                if (!newActiveProjectSourceRoots.isEmpty()) {
                    activeProjectSourceRoots = ClasspathInfo.create(
                            ClassPath.EMPTY,
                            ClassPath.EMPTY,
                            org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(newActiveProjectSourceRoots.toArray(new URL[newActiveProjectSourceRoots.size()])));
                    activeProjectSourceRoots.getClassIndex().addClassIndexListener(this);
                    LOG.log(
                        Level.FINER,
                        "{0} for {1} listening on: {2}",    //NOI18N
                        new Object[]{
                            getClass().getSimpleName(),
                            base,
                            newActiveProjectSourceRoots
                        });
                }
            }
            res = new ArrayList<>(modulesByName.size());
            modulesByName.values().stream()
                    .map((url)->org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(url))
                    .forEach(res::add);
            final List<PathResourceImplementation> selfResResources;
            final ClassPath bootModules;
            final ClassPath userModules;
            final ClassPath bootCp;
            if (systemModules != null) {
                selfResResources = Collections.emptyList();
                bootModules = systemModules;
                userModules = base;
                bootCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(
                    findJavaBase(getModulesByName(systemModules, null)));
            } else {
                selfResResources = findJavaBase(modulesByName);
                bootModules = base;
                userModules = ClassPath.EMPTY;
                bootCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(selfResResources);
            }
            LOG.log(
                Level.FINER,
                "{0} for {1} self resources: {2}",    //NOI18N
                new Object[]{
                    ModuleInfoClassPathImplementation.class.getSimpleName(),
                    base,
                    selfResResources
                });
            LOG.log(
                Level.FINEST,
                "{0} for {1} bootModules: {2}, bootCp: {3},  modules: {4}",    //NOI18N
                new Object[]{
                    ModuleInfoClassPathImplementation.class.getSimpleName(),
                    base,
                    bootModules,
                    bootCp,
                    modulesByName
                });
            selfRes.set(new Object[]{
                selfResResources,
                needToFire});
            try {
                boolean found = false;
                for (URL root : sources.getRootURLs()) {
                    try {
                        final File moduleInfo = FileUtil.normalizeFile(new File(BaseUtilities.toFile(root.toURI()),MODULE_INFO_JAVA));
                        newModuleInfos.add(moduleInfo);
                        if (!found) {
                            final FileObject modules = FileUtil.toFileObject(moduleInfo);
                            if (modules != null) {
                                found = true;
                                final JavaSource src = JavaSource.create(
                                    new ClasspathInfo.Builder(bootCp)
                                            .setModuleBootPath(bootModules)
                                            .setModuleCompilePath(userModules)
                                            .build(),
                                    modules);
                                if (src != null) {
                                    try {
                                        final List<List<PathResourceImplementation>> resInOut = new ArrayList<>(1);
                                        resInOut.add(res);
                                        src.runUserActionTask((CompilationController cc) -> {
                                            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                            final Trees trees = cc.getTrees();
                                            final TreePathScanner<ModuleElement, Void> scanner =
                                                    new TreePathScanner<ModuleElement, Void>() {
                                                        @Override
                                                        public ModuleElement visitModule(ModuleTree node, Void p) {
                                                            return (ModuleElement) trees.getElement(getCurrentPath());
                                                        }
                                                };
                                            final ModuleElement rootModule = scanner.scan(new TreePath(cc.getCompilationUnit()), null);
                                            Set<URL> requires = collectRequiredModules(rootModule, true, modulesByName);
                                            resInOut.set(0, filterModules(resInOut.get(0), requires));
                                        }, true);
                                        res = resInOut.get(0);
                                    } catch (IOException ioe) {
                                        Exceptions.printStackTrace(ioe);
                                    }
                                }
                            }
                        }
                    } catch (URISyntaxException e) {
                        LOG.log(
                            Level.WARNING,
                            "Invalid URL: {0}, reason: {1}",    //NOI18N
                            new Object[]{
                                root,
                                e.getMessage()
                            });
                    }
                }
                if (!found && legacyClassPath != null) {
                    final List<ClassPath.Entry> legacyEntires = legacyClassPath.entries();
                    final List<PathResourceImplementation> tmp = new ArrayList<>(res.size() + legacyEntires.size());
                    legacyEntires.stream()
                            .map((e)->org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(e.getURL()))
                            .forEach(tmp::add);
                    tmp.addAll(res);
                    res = tmp;
                }
            } finally {
                needToFire = selfRes.get()[1] == Boolean.TRUE;
                selfRes.remove();
            }
            synchronized (this) {
                assert res != null;
                List<PathResourceImplementation> ccv = getCache();
                if (ccv == null || ccv == TOMBSTONE) {
                    LOG.log(
                        Level.FINE,
                        "{0} for {1} setting results: {2}, listening on: {3}",    //NOI18N
                        new Object[]{
                            getClass().getSimpleName(),
                            base,
                            res,
                            newModuleInfos
                        });
                    setCache(res);
                    final Collection<File> added = new ArrayList<>(newModuleInfos);
                    added.removeAll(moduleInfos);
                    final Collection<File> removed = new ArrayList<>(moduleInfos);
                    removed.removeAll(newModuleInfos);
                    removed.stream().forEach((f) -> FileUtil.removeFileChangeListener(this, f));
                    added.stream().forEach((f) -> FileUtil.addFileChangeListener(this, f));
                    moduleInfos = newModuleInfos;
                } else {
                    res = ccv;
                }
            }
            if (needToFire) {
                fire();
            }
            return res;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null || ClassPath.PROP_ENTRIES.equals(propName) || SourceRoots.PROP_ROOTS.equals(propName)) {
                final Runnable action = () -> resetCache(TOMBSTONE, true);
                if (ProjectManager.mutex().isWriteAccess()) {
                    ProjectManager.mutex().postReadRequest(action);
                } else {
                    action.run();
                }
            }
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            resetCache(TOMBSTONE, true);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            resetCache(TOMBSTONE, true);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            resetCache(TOMBSTONE, true);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            resetCache(TOMBSTONE, true);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        public void rootsAdded(final RootsEvent event) {
            ClasspathInfo info;
            synchronized (this) {
                info = activeProjectSourceRoots;
                if (info != null) {
                    if (rootsChanging) {
                        info = null;
                    } else {
                        rootsChanging = true;
                    }
                }
            }
            if (info != null) {
                try {
                    JavaSource.create(info).runWhenScanFinished((cc)->{
                            LOG.log(
                                Level.FINER,
                                "{0} for {1} got class index event: {2}",    //NOI18N
                                new Object[]{
                                    ModuleInfoClassPathImplementation.class.getSimpleName(),
                                    base,
                                    event
                                });
                            rootsChanging = false;
                            resetCache(TOMBSTONE, true);
                        },
                        true);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }

        @Override
        public void rootsRemoved(RootsEvent event) {
        }

        @Override
        public void typesAdded(TypesEvent event) {
        }

        @Override
        public void typesRemoved(TypesEvent event) {
        }

        @Override
        public void typesChanged(TypesEvent event) {
        }

        @NonNull
        private static Map<String,URL> getModulesByName(
                @NonNull final ClassPath cp,
                @NullAllowed final Collection<URL> projectSourceRoots) {
            final Map<String,URL> res = new HashMap<>();
            cp.entries().stream()
                    .map((entry)->entry.getURL())
                    .forEach((url)-> {
                        if (projectSourceRoots != null) {
                            final SourceForBinaryQuery.Result2 sfbqRes = SourceForBinaryQuery.findSourceRoots2(url);
                            if (sfbqRes.preferSources()) {
                                Arrays.stream(sfbqRes.getRoots())
                                        .map((fo)->fo.toURL())
                                        .forEach(projectSourceRoots::add);
                            }
                        }
                        final String moduleName = SourceUtils.getModuleName(url, true);
                        if (moduleName != null) {
                            res.put(moduleName, url);
                        }
                    });
            return res;
        }

        @NonNull
        private static List<PathResourceImplementation> findJavaBase(final Map<String,URL> modulesByName) {
            return Optional.ofNullable(modulesByName.get(MOD_JAVA_BASE))
                .map(org.netbeans.spi.java.classpath.support.ClassPathSupport::createResource)
                .map(Collections::singletonList)
                .orElseGet(Collections::emptyList);
        }

        @NonNull
        private static Set<URL> collectRequiredModules(
                @NonNull final ModuleElement module,
                final boolean transitive,
                @NonNull final Map<String,URL> modulesByName) {
            final Set<URL> res = new HashSet<>();
            final Set<ModuleElement> seen = new HashSet<>();
            collectRequiredModulesImpl(module, transitive, true, modulesByName, seen, res);
            return res;
        }

        private static boolean collectRequiredModulesImpl(
                @NullAllowed final ModuleElement module,
                final boolean transitive,
                final boolean topLevel,
                @NonNull final Map<String,URL> modulesByName,
                @NonNull final Collection<? super ModuleElement> seen,
                @NonNull final Collection<? super URL> c) {
            if (module != null && !module.isUnnamed() && seen.add(module)) {
                for (ModuleElement.Directive directive : module.getDirectives()) {
                    if (directive.getKind() == ModuleElement.DirectiveKind.REQUIRES) {
                        ModuleElement.RequiresDirective req = (ModuleElement.RequiresDirective) directive;
                        if (topLevel || req.isPublic()) {
                            final ModuleElement dependency = req.getDependency();
                            boolean add = true;
                            if (transitive) {
                                add = collectRequiredModulesImpl(dependency, transitive, false, modulesByName, seen, c);
                            }
                            if (add) {
                                final URL dependencyURL = modulesByName.get(dependency.getQualifiedName().toString());
                                if (dependencyURL != null) {
                                    c.add(dependencyURL);
                                }
                            }
                        }
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        @NonNull
        private static List<PathResourceImplementation> filterModules(
                @NonNull List<PathResourceImplementation> modules,
                @NonNull Set<URL> requires) {
            final List<PathResourceImplementation> res = new ArrayList<>(modules.size());
            for (PathResourceImplementation pr : modules) {
                for (URL url : pr.getRoots()) {
                    if (requires.contains(url)) {
                        res.add(pr);
                    }
                }
            }
            return res;
        }
    }

    private static abstract class BaseClassPathImplementation implements ClassPathImplementation {

        private final PropertyChangeSupport listeners;
        //@GuardedBy("this")
        private List<PathResourceImplementation> cache;

        BaseClassPathImplementation() {
            this(null);
        }

        BaseClassPathImplementation(final List<PathResourceImplementation> initialValue) {
            this.listeners = new PropertyChangeSupport(this);
            synchronized (this) {
                this.cache = initialValue;
            }
        }

        @Override
        public final void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            this.listeners.addPropertyChangeListener(listener);
        }

        @Override
        public final void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            this.listeners.removePropertyChangeListener(listener);
        }

        @CheckForNull
        final synchronized List<PathResourceImplementation> getCache() {
            return this.cache;
        }

        final synchronized void setCache(@NullAllowed final List<PathResourceImplementation> cache) {
            this.cache = cache;
        }

        final void resetCache(final boolean fire) {
            resetCache(null, fire);
        }

        final void resetCache(
                @NullAllowed final List<PathResourceImplementation> update,
                final boolean fire) {
            synchronized (this) {
                this.cache = update;
            }
            if (fire) {
                fire();
            }
        }

        final void fire() {
            this.listeners.firePropertyChange(PROP_RESOURCES, null, null);
        }
    }

}
