/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.api.common.queries;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.Tree;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Implementation of the {@link CompilerOptionsQueryImplementation} for unit tests.
 * @author Tomas Zezula
 */
final class UnitTestsCompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {
    private static final Logger LOG = Logger.getLogger(UnitTestsCompilerOptionsQueryImpl.class.getName());
    private static final String MODULE_INFO_JAVA = "module-info.java";  //NOI18N
    private static final SpecificationVersion JDK9 = new SpecificationVersion("9"); //NOI18N

    private final PropertyEvaluator eval;
    private final SourceRoots srcRoots;
    private final SourceRoots testRoots;
    private final AtomicReference<ResultImpl> result;

    UnitTestsCompilerOptionsQueryImpl(
            @NonNull final PropertyEvaluator eval,
            @NonNull final SourceRoots srcRoots,
            @NonNull final SourceRoots testRoots) {
        Parameters.notNull("eval", eval);   //NOI18N
        Parameters.notNull("srcRoots", srcRoots);   //NOI18N
        Parameters.notNull("testRoots", testRoots); //NOI18N
        this.eval = eval;
        this.srcRoots = srcRoots;
        this.testRoots = testRoots;
        this.result = new AtomicReference<>();
    }

    @CheckForNull
    @Override
    public Result getOptions(@NonNull final FileObject file) {
        for (FileObject root : testRoots.getRoots()) {
            if (isArtifact(root, file)) {
                ResultImpl res = result.get();
                if (res == null) {
                    res = new ResultImpl(eval, srcRoots, testRoots);
                    if (!result.compareAndSet(null, res)) {
                        res = result.get();
                    }
                    assert res != null;
                }
                return res;
            }
        }
        return null;
    }

    private static boolean isArtifact(
            @NonNull final FileObject root,
            @NonNull final FileObject file) {
        return root.equals(file) || FileUtil.isParentOf(root, file);
    }

    private static final class ResultImpl extends Result implements ChangeListener,
            PropertyChangeListener, FileChangeListener {
        private final PropertyEvaluator eval;
        private final SourceRoots srcRoots;
        private final SourceRoots testRoots;
        private final ChangeSupport cs;
        private final ThreadLocal<boolean[]> reenter;
        private final Collection</*@GuardedBy("this")*/File> moduleInfoListeners ;
        //@GuardedBy("this")
        private List<String> cache;
        //@GuardedBy("this")
        private SourceLevelQuery.Result sourceLevel;
        //@GuardedBy("this")
        private boolean listensOnRoots;

        ResultImpl(
                @NonNull final PropertyEvaluator eval,
                @NonNull final SourceRoots srcRoots,
                @NonNull final SourceRoots testRoots) {
            this.eval = eval;
            this.srcRoots = srcRoots;
            this.testRoots = testRoots;
            this.cs = new ChangeSupport(this);
            this.reenter = new ThreadLocal<>();
            this.moduleInfoListeners = new HashSet<>();
        }

        @Override
        public List<? extends String> getArguments() {
            List<String> args;
            SourceLevelQuery.Result[] slq = new SourceLevelQuery.Result[1];
            synchronized (this) {
                args = cache;
                slq[0] = sourceLevel;
            }
            if (args == null) {
                boolean[] state;
                if ((state = reenter.get()) != null) {
                    args = Collections.emptyList();
                    state[0] = true;
                } else {
                    reenter.set(new boolean[1]);
                    try {
                        TestMode mode;
                        final Collection<File> allRoots = new HashSet<>();
                        final FileObject srcModuleInfo = findModuleInfo(srcRoots, allRoots, null);
                        final FileObject testModuleInfo = findModuleInfo(testRoots, allRoots, slq);
                        final boolean isLegacy = Optional.ofNullable(slq[0])
                            .map((r) -> r.getSourceLevel())
                            .map((sl) -> JDK9.compareTo(new SpecificationVersion(sl)) > 0)
                            .orElse(Boolean.TRUE);
                        mode = isLegacy ?
                            TestMode.LEGACY :
                            srcModuleInfo == null ?
                                TestMode.UNNAMED :
                                testModuleInfo == null ?
                                    TestMode.INLINED:
                                    TestMode.MODULE;
                        final String propVal = eval.getProperty(ProjectProperties.JAVAC_TEST_COMPILERARGS);
                        args = propVal != null && !propVal.isEmpty() ?
                                parseLine(propVal) :
                                mode.createArguments(
                                srcModuleInfo,
                                testModuleInfo);
                        synchronized (this) {
                            if (cache == null) {
                                cache = args;
                            } else {
                                args = cache;
                            }
                            if (sourceLevel == null && slq[0] != null) {
                                sourceLevel = slq[0];
                                if (sourceLevel.supportsChanges()) {
                                    sourceLevel.addChangeListener(WeakListeners.change(this, sourceLevel));
                                }
                            }
                            if (!listensOnRoots) {
                                listensOnRoots = true;
                                srcRoots.addPropertyChangeListener(WeakListeners.propertyChange(this, srcRoots));
                                testRoots.addPropertyChangeListener(WeakListeners.propertyChange(this, testRoots));
                                eval.addPropertyChangeListener(WeakListeners.propertyChange(this, eval));
                            }
                            final Set<File> toRemove = new HashSet<>(moduleInfoListeners);
                            toRemove.removeAll(allRoots);
                            allRoots.removeAll(moduleInfoListeners);
                            for (File f : toRemove) {
                                FileUtil.removeFileChangeListener(
                                        this,
                                        new File(f, MODULE_INFO_JAVA));
                                moduleInfoListeners.remove(f);
                            }
                            for (File f : allRoots) {
                                FileUtil.addFileChangeListener(
                                        this,
                                        new File(f, MODULE_INFO_JAVA));
                                moduleInfoListeners.add(f);
                            }
                        }
                    } finally {
                        final boolean fire = Optional.ofNullable(reenter.get())
                                .map((ba) -> ba[0])
                                .orElse(Boolean.FALSE);
                        reenter.remove();
                        if (fire) {
                            cs.fireChange();
                        }
                    }
                }
            }
            return args;
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

        @Override
        public void stateChanged(@NonNull final ChangeEvent e) {
            reset();
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            final String evtName = evt.getPropertyName();
            if (SourceRoots.PROP_ROOTS.equals(evtName) ||
                ProjectProperties.JAVAC_TEST_COMPILERARGS.equals(evtName) ||
                evt == null) {
                reset();
            }
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            reset();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            reset();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            reset();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            reset();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            //Not important
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            //Not important
        }

        private void reset() {
            synchronized (this) {
                cache = null;
            }
            cs.fireChange();
        }

        private static FileObject findModuleInfo(
                @NonNull final SourceRoots roots,
                @NonNull final Collection<? super File> rootCollector,
                @NullAllowed final SourceLevelQuery.Result[] holder) {
            FileObject result = null;
            for (FileObject root : roots.getRoots()) {
                if (holder != null) {
                    if (holder[0] == null) {
                        holder[0] = SourceLevelQuery.getSourceLevel2(root);
                    }
                }
                Optional.ofNullable(FileUtil.toFile(root))
                    .ifPresent(rootCollector::add);
                if (result == null) {
                    final FileObject moduleInfo = root.getFileObject(MODULE_INFO_JAVA);
                    if (moduleInfo != null) {
                        result = moduleInfo;
                    }
                }
            }
            return result;
        }

        @CheckForNull
        private static String getModuleName(@NonNull final FileObject moduleInfo) {
            try {
                final String[] res = new String[1];
                final JavaSource src = JavaSource.forFileObject(moduleInfo);
                if (src != null) {
                    src.runUserActionTask((cc) -> {
                        cc.toPhase(JavaSource.Phase.PARSED);
                        final CompilationUnitTree cu = cc.getCompilationUnit();
                        for (Tree decl : cu.getTypeDecls()) {
                            if (decl.getKind() == Tree.Kind.MODULE) {
                                res[0] = ((ModuleTree)decl).getName().toString();
                                break;
                            }
                        }
                    }, true);
                }
                return res[0];
            } catch (IOException ioe) {
                LOG.log(
                        Level.WARNING,
                        "Cannot read module declaration in: {0} due to: {1}",   //NOI18N
                        new Object[]{
                            FileUtil.getFileDisplayName(moduleInfo),
                            ioe.getMessage()
                        });
                return null;
            }
        }

        private static enum TestMode {
            /**
             * Tests for pre JDK9 sources.
             */
            LEGACY {
                @Override
                List<String> createArguments(
                        @NullAllowed final FileObject srcModuleInfo,
                        @NullAllowed final FileObject testModuleInfo) {
                    return Collections.emptyList();
                }
            },
            /**
             * Tests for an unnamed module.
             */
            UNNAMED {
                @Override
                List<String> createArguments(
                        @NullAllowed final FileObject srcModuleInfo,
                        @NullAllowed final FileObject testModuleInfo) {
                    return Collections.emptyList();
                }
            },
            /**
             * Tests inlined into names module in sources.
             */
            INLINED {
                @Override
                List<String> createArguments(
                        @NullAllowed final FileObject srcModuleInfo,
                        @NullAllowed final FileObject testModuleInfo) {
                    final String moduleName = getModuleName(srcModuleInfo);
                    if (moduleName == null) {
                        return Collections.emptyList();
                    }
                    final List<String> result = Arrays.asList(
                    String.format("-Xmodule:%s", moduleName),      //NOI18N
                    String.format("-XaddReads:%s=ALL-UNNAMED",     //NOI18N
                                    moduleName
                    ));
                    return Collections.unmodifiableList(result);
                }
            },
            /**
             * Tests have its own module.
             */
            MODULE {
                @Override
                List<String> createArguments(
                        @NullAllowed final FileObject srcModuleInfo,
                        @NullAllowed final FileObject testModuleInfo) {
                    final String moduleName = getModuleName(testModuleInfo);
                    return moduleName == null ?
                            Collections.emptyList() :
                            Collections.singletonList(
                                    String.format("-XaddReads:%s=ALL-UNNAMED",  //NOI18N
                                        moduleName
                                ));
                }
            };

            @NonNull
            abstract List<String> createArguments(
                    @NullAllowed final FileObject srcModuleInfo,
                    @NullAllowed final FileObject testModuleInfo);
        }
    }
}
