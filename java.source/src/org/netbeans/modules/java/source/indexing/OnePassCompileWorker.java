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

package org.netbeans.modules.java.source.indexing;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeScanner;
import org.netbeans.lib.nbjavac.services.CancelAbort;
import org.netbeans.lib.nbjavac.services.CancelService;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.FatalError;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.MissingPlatformError;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import javax.annotation.processing.Processor;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
import org.netbeans.modules.java.source.usages.ClassNamesForFileOraculumImpl;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.ExecutableFilesIndex;
import org.netbeans.modules.java.source.usages.Pair;
import org.netbeans.modules.parsing.lucene.support.LowMemoryWatcher;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.SuspendStatus;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
final class OnePassCompileWorker extends CompileWorker {

    @Override
    ParsingOutput compile(
            final ParsingOutput previous,
            final Context context,
            final JavaParsingContext javaContext,
            final Collection<? extends CompileTuple> files) {
        final JavaFileManager fileManager = ClasspathInfoAccessor.getINSTANCE().getFileManager(javaContext.getClasspathInfo());
        final Map<JavaFileObject, List<String>> file2FQNs = previous != null ? previous.file2FQNs : new HashMap<JavaFileObject, List<String>>();
        final Set<ElementHandle<TypeElement>> addedTypes = previous != null ? previous.addedTypes : new HashSet<ElementHandle<TypeElement>>();
        final Set<File> createdFiles = previous != null ? previous.createdFiles : new HashSet<File>();
        final Set<Indexable> finished = previous != null ? previous.finishedFiles : new HashSet<Indexable>();
        final Set<ElementHandle<TypeElement>> modifiedTypes = previous != null ? previous.modifiedTypes : new HashSet<ElementHandle<TypeElement>>();
        final Set<CompileTuple> aptGenerated = previous != null ? previous.aptGenerated : new HashSet<CompileTuple>();
        final ClassNamesForFileOraculumImpl cnffOraculum = new ClassNamesForFileOraculumImpl(file2FQNs);

        final LowMemoryWatcher mem = LowMemoryWatcher.getInstance();
        final DiagnosticListenerImpl dc = new DiagnosticListenerImpl();
        final HashMap<JavaFileObject, Pair<CompilationUnitTree, CompileTuple>> jfo2units = new HashMap<JavaFileObject, Pair<CompilationUnitTree, CompileTuple>>();
        LinkedList<Pair<CompilationUnitTree, CompileTuple>> units = new LinkedList<Pair<CompilationUnitTree, CompileTuple>>();
        JavacTaskImpl jt = null;

        boolean nop = true;
        final SuspendStatus suspendStatus = context.getSuspendStatus();
        final SourcePrefetcher sourcePrefetcher = SourcePrefetcher.create(files, suspendStatus);
        while (sourcePrefetcher.hasNext())  {
            final CompileTuple tuple = sourcePrefetcher.next();
            try {
                if (tuple != null) {
                    nop = false;
                    if (context.isCancelled()) {
                        return null;
                    }
                    try {
                        if (mem.isLowMemory()) {
                            jt = null;
                            units = null;
                            dc.cleanDiagnostics();
                            System.gc();
                        }
                        if (jt == null) {
                            jt = JavacParser.createJavacTask(javaContext.getClasspathInfo(), dc, javaContext.getSourceLevel(), cnffOraculum, javaContext.getFQNs(), new CancelService() {
                                public @Override boolean isCanceled() {
                                    return context.isCancelled();
                                }
                            }, APTUtils.get(context.getRoot()));
                        }
                        for (CompilationUnitTree cut : jt.parse(tuple.jfo)) { //TODO: should be exactly one
                            if (units != null) {
                                Pair<CompilationUnitTree, CompileTuple> unit = Pair.<CompilationUnitTree, CompileTuple>of(cut, tuple);
                                units.add(unit);
                                jfo2units.put(tuple.jfo, unit);
                            }
                            computeFQNs(file2FQNs, cut, tuple);
                        }
                        Log.instance(jt.getContext()).nerrors = 0;
                    } catch (CancelAbort ca) {
                        if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                            JavaIndex.LOG.log(Level.FINEST, "OnePassCompileWorker was canceled in root: " + FileUtil.getFileDisplayName(context.getRoot()), ca);  //NOI18N
                        }
                    } catch (Throwable t) {
                        if (JavaIndex.LOG.isLoggable(Level.WARNING)) {
                            final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                            final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                            final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                            final String message = String.format("OnePassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                        tuple.indexable.getURL().toString(),
                                        FileUtil.getFileDisplayName(context.getRoot()),
                                        bootPath == null   ? null : bootPath.toString(),
                                        classPath == null  ? null : classPath.toString(),
                                        sourcePath == null ? null : sourcePath.toString()
                                        );
                            JavaIndex.LOG.log(Level.WARNING, message, t);  //NOI18N
                        }
                        if (t instanceof ThreadDeath) {
                            throw (ThreadDeath) t;
                        }
                        else {
                            jt = null;
                            units = null;
                            dc.cleanDiagnostics();
                            System.gc();
                        }
                    }
                }
            } finally {
                sourcePrefetcher.remove();
            }
        }

        if (nop) {
            return new ParsingOutput(true, file2FQNs, addedTypes, createdFiles, finished, modifiedTypes, aptGenerated);
        }

        if (units == null || JavaCustomIndexer.NO_ONE_PASS_COMPILE_WORKER) {
            return new ParsingOutput(false, file2FQNs, addedTypes, createdFiles, finished, modifiedTypes, aptGenerated);
        }

        CompileTuple active = null;
        Iterable<? extends Processor> processors = jt != null ? jt.getProcessors() : null;
        boolean aptEnabled = processors != null && processors.iterator().hasNext();
        try {
            while(!units.isEmpty()) {
                if (context.isCancelled()) {
                    return null;
                }
                Pair<CompilationUnitTree, CompileTuple> unit = units.removeFirst();
                active = unit.second;
                if (finished.contains(active.indexable)) {
                    continue;
                }
                if (mem.isLowMemory()) {
                    units = null;
                    System.gc();
                    return new ParsingOutput(false, file2FQNs, addedTypes, createdFiles, finished, modifiedTypes, aptGenerated);
                }
                Iterable<? extends TypeElement> types;
                types = jt.enterTrees(Collections.singletonList(unit.first));
                if (jfo2units.remove(active.jfo) != null) {
                    final Types ts = Types.instance(jt.getContext());
                    final Indexable activeIndexable = active.indexable;
                    class ScanNested extends TreeScanner {
                        Set<Pair<CompilationUnitTree, CompileTuple>> dependencies = new LinkedHashSet<Pair<CompilationUnitTree, CompileTuple>>();
                        @Override
                        public void visitClassDef(JCClassDecl node) {
                            if (node.sym != null) {
                                Type st = ts.supertype(node.sym.type);
                                if (st.tag == TypeTags.CLASS) {
                                    ClassSymbol c = st.tsym.outermostClass();
                                    Pair<CompilationUnitTree, CompileTuple> u = jfo2units.remove(c.sourcefile);
                                    if (u != null && !finished.contains(u.second.indexable) && !u.second.indexable.equals(activeIndexable)) {
                                        if (dependencies.add(u)) {
                                            scan((JCCompilationUnit)u.first);
                                        }
                                    }
                                }
                            }
                            super.visitClassDef(node);
                        }
                    }
                    ScanNested scanner = new ScanNested();
                    scanner.scan((JCCompilationUnit)unit.first);
                    if (!scanner.dependencies.isEmpty()) {
                        units.addFirst(unit);
                        for (Pair<CompilationUnitTree, CompileTuple> pair : scanner.dependencies) {
                            units.addFirst(pair);
                        }
                        continue;
                    }
                }
                if (mem.isLowMemory()) {
                    units = null;
                    System.gc();
                    return new ParsingOutput(false, file2FQNs, addedTypes, createdFiles, finished, modifiedTypes, aptGenerated);
                }
                jt.analyze(types);
                if (aptEnabled) {
                    JavaCustomIndexer.addAptGenerated(context, javaContext, active.indexable.getRelativePath(), aptGenerated);
                }
                if (mem.isLowMemory()) {
                    units = null;
                    System.gc();
                    return new ParsingOutput(false, file2FQNs, addedTypes, createdFiles, finished, modifiedTypes, aptGenerated);
                }
                javaContext.getFQNs().set(types, active.indexable.getURL());
                boolean[] main = new boolean[1];
                if (javaContext.getCheckSums().checkAndSet(active.indexable.getURL(), types, jt.getElements()) || context.isSupplementaryFilesIndexing()) {
                    javaContext.analyze(Collections.singleton(unit.first), jt, fileManager, unit.second, addedTypes, main);
                } else {
                    final Set<ElementHandle<TypeElement>> aTypes = new HashSet<ElementHandle<TypeElement>>();
                    javaContext.analyze(Collections.singleton(unit.first), jt, fileManager, unit.second, aTypes, main);
                    addedTypes.addAll(aTypes);
                    modifiedTypes.addAll(aTypes);
                }
                ExecutableFilesIndex.DEFAULT.setMainClass(context.getRoot().getURL(), active.indexable.getURL(), main[0]);
                for (JavaFileObject generated : jt.generate(types)) {
                    if (generated instanceof FileObjects.FileBase) {
                        createdFiles.add(((FileObjects.FileBase) generated).getFile());
                    } else {
                        // presumably should not happen
                    }
                }
                JavaCustomIndexer.setErrors(context, active, dc);
                Log.instance(jt.getContext()).nerrors = 0;
                finished.add(active.indexable);
            }
            return new ParsingOutput(true, file2FQNs, addedTypes, createdFiles, finished, modifiedTypes, aptGenerated);
        } catch (CouplingAbort ca) {
            //Coupling error
            TreeLoader.dumpCouplingAbort(ca, null);
        } catch (OutputFileManager.InvalidSourcePath isp) {
            //Deleted project - log & ignore
            if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                final String message = String.format("OnePassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                            active.jfo.toUri().toString(),
                            FileUtil.getFileDisplayName(context.getRoot()),
                            bootPath == null   ? null : bootPath.toString(),
                            classPath == null  ? null : classPath.toString(),
                            sourcePath == null ? null : sourcePath.toString()
                            );
                JavaIndex.LOG.log(Level.FINEST, message, isp);
            }
        } catch (MissingPlatformError mpe) {
            //No platform - log & mark files as errornous
            if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                final String message = String.format("OnePassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                            active.jfo.toUri().toString(),
                            FileUtil.getFileDisplayName(context.getRoot()),
                            bootPath == null   ? null : bootPath.toString(),
                            classPath == null  ? null : classPath.toString(),
                            sourcePath == null ? null : sourcePath.toString()
                            );
                JavaIndex.LOG.log(Level.FINEST, message, mpe);
            }
            JavaCustomIndexer.brokenPlatform(context, files, mpe.getDiagnostic());
        } catch (CancelAbort ca) {
            if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                JavaIndex.LOG.log(Level.FINEST, "OnePassCompileWorker was canceled in root: " + FileUtil.getFileDisplayName(context.getRoot()), ca);  //NOI18N
            }
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            } else {
                Level level = t instanceof FatalError ? Level.FINEST : Level.WARNING;
                if (JavaIndex.LOG.isLoggable(level)) {
                    final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                    final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                    final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                    final String message = String.format("OnePassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                active.jfo.toUri().toString(),
                                FileUtil.getFileDisplayName(context.getRoot()),
                                bootPath == null   ? null : bootPath.toString(),
                                classPath == null  ? null : classPath.toString(),
                                sourcePath == null ? null : sourcePath.toString()
                                );
                    JavaIndex.LOG.log(level, message, t);  //NOI18N
                }
            }
        }
        return new ParsingOutput(false, file2FQNs, addedTypes, createdFiles, finished, modifiedTypes, aptGenerated);
    }
}
