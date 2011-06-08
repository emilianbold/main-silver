/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.jackpot.impl.batch;

import org.netbeans.modules.java.hints.introduce.CopyFinder;
import org.netbeans.modules.java.hints.jackpot.impl.MessageImpl;
import org.netbeans.modules.java.hints.jackpot.impl.Utilities;
import org.netbeans.modules.java.hints.jackpot.impl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.jackpot.impl.pm.BulkSearch;
import org.netbeans.modules.java.hints.jackpot.impl.pm.BulkSearch.BulkPattern;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext.MessageKind;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription.AdditionalQueryConstraints;
import org.netbeans.modules.java.hints.jackpot.spi.Trigger.PatternDescription;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class BatchSearch {

    private static final Logger LOG = Logger.getLogger(BatchSearch.class.getName());

    public static BatchResult findOccurrences(Iterable<? extends HintDescription> patterns, Scope scope) {
        return findOccurrences(patterns, scope, new ProgressHandleWrapper(null));
    }

    public static BatchResult findOccurrences(final Iterable<? extends HintDescription> patterns, final Scope scope, final ProgressHandleWrapper progress) {
        for (HintDescription pattern : patterns) {
            if (!(pattern.getTrigger() instanceof PatternDescription)) {
                throw new UnsupportedOperationException();
            }
        }

        return findOccurrencesLocal(patterns, scope.getIndexMapper(patterns), scope.getTodo(), progress);
    }

    private static BatchResult findOccurrencesLocal(final Iterable<? extends HintDescription> patterns, final MapIndices indexMapper, final Collection<? extends FileObject> todo, final ProgressHandleWrapper progress) {
        final BatchResult[] result = new BatchResult[1];

        try {
            JavaSource.create(Utilities.createUniversalCPInfo()).runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    result[0] = findOccurrencesLocalImpl(parameter, patterns, indexMapper, todo, progress);
                }
            }, true);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        return result[0];
    }
    
    private static BatchResult findOccurrencesLocalImpl(final CompilationInfo info, final Iterable<? extends HintDescription> patterns, MapIndices indexMapper, Collection<? extends FileObject> todo, ProgressHandleWrapper progress) {
        final Callable<BulkPattern> bulkPattern = new Callable<BulkPattern>() {
            private final AtomicReference<BulkPattern> pattern = new AtomicReference<BulkPattern>();
            public BulkPattern call() {
                if (pattern.get() == null) {
                    pattern.set(preparePattern(patterns, info));
                }

                return pattern.get();
            }
        };
        final Map<IndexEnquirer, Collection<? extends Resource>> result = new HashMap<IndexEnquirer, Collection<? extends Resource>>();
        final Collection<MessageImpl> problems = new LinkedList<MessageImpl>();
        ProgressHandleWrapper innerForAll = progress.startNextPartWithEmbedding(ProgressHandleWrapper.prepareParts(2 * todo.size()));
        
        for (final FileObject src : todo) {
            LOG.log(Level.FINE, "Processing: {0}", FileUtil.getFileDisplayName(src));
            
            IndexEnquirer indexEnquirer = indexMapper.findIndex(src, innerForAll);

            if (indexEnquirer == null) {
                indexEnquirer = new FileSystemBasedIndexEnquirer(src);
            }

            Collection<? extends Resource> occurrences = indexEnquirer.findResources(patterns, innerForAll, bulkPattern, problems);

            if (!occurrences.isEmpty()) {
                result.put(indexEnquirer, occurrences);
            }

            innerForAll.tick();
        }

        return new BatchResult(result, problems);
    }

    private static BulkPattern preparePattern(final Iterable<? extends HintDescription> patterns, CompilationInfo info) {
        Collection<String> code = new LinkedList<String>();
        Collection<Tree> trees = new LinkedList<Tree>();
        Collection<AdditionalQueryConstraints> additionalConstraints = new LinkedList<AdditionalQueryConstraints>();

        for (HintDescription pattern : patterns) {
            String textPattern = ((PatternDescription) pattern.getTrigger()).getPattern();

            code.add(textPattern);
            trees.add(Utilities.parseAndAttribute(info, textPattern, null));
            additionalConstraints.add(pattern.getAdditionalConstraints());
        }

        return BulkSearch.getDefault().create(code, trees, additionalConstraints);
    }

    public static void getVerifiedSpans(BatchResult candidates, @NonNull ProgressHandleWrapper progress, final VerifiedSpansCallBack callback, final Collection<? super MessageImpl> problems) {
        getVerifiedSpans(candidates, progress, callback, false, problems);
    }

    public static void getVerifiedSpans(BatchResult candidates, @NonNull ProgressHandleWrapper progress, final VerifiedSpansCallBack callback, boolean doNotRegisterClassPath, final Collection<? super MessageImpl> problems) {
        int[] parts = new int[candidates.projectId2Resources.size()];
        int   index = 0;

        for (Entry<? extends IndexEnquirer, ? extends Collection<? extends Resource>> e : candidates.projectId2Resources.entrySet()) {
            parts[index++] = e.getValue().size();
        }

        ProgressHandleWrapper inner = progress.startNextPartWithEmbedding(parts);

        for (Entry<? extends IndexEnquirer, ? extends Collection<? extends Resource>> e : candidates.projectId2Resources.entrySet()) {
            inner.startNextPart(e.getValue().size());

            e.getKey().validateResource(e.getValue(), progress, callback, doNotRegisterClassPath, problems);
        }
    }

    private static void getLocalVerifiedSpans(Collection<? extends Resource> resources, @NonNull final ProgressHandleWrapper progress, final VerifiedSpansCallBack callback, boolean doNotRegisterClassPath, final Collection<? super MessageImpl> problems) {
        Collection<FileObject> files = new LinkedList<FileObject>();
        final Map<FileObject, Resource> file2Resource = new HashMap<FileObject, Resource>();

        for (Resource r : resources) {
            FileObject file = r.getResolvedFile();

            if (file != null) {
                files.add(file);
                file2Resource.put(file, r);
            } else {
                callback.cannotVerifySpan(r);
                progress.tick();
            }
        }

        Map<ClasspathInfo, Collection<FileObject>> cp2Files = BatchUtilities.sortFiles(files);
        ClassPath[] toRegister = null;

        if (!doNotRegisterClassPath) {
            Set<ClassPath> toRegisterSet = new HashSet<ClassPath>();

            for (ClasspathInfo cpInfo : cp2Files.keySet()) {
                toRegisterSet.add(cpInfo.getClassPath(PathKind.SOURCE));
            }

            toRegister = !toRegisterSet.isEmpty() ? toRegisterSet.toArray(new ClassPath[0]) : null;

            if (toRegister != null) {
                GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, toRegister);
                try {
                    Utilities.waitScanFinished();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        try {
            for (Entry<ClasspathInfo, Collection<FileObject>> e : cp2Files.entrySet()) {
                try {
                    List<FileObject> toProcess = new ArrayList<FileObject>(e.getValue());
                    final AtomicInteger currentPointer = new AtomicInteger();
                    callback.groupStarted();

//                    for (FileObject f : toProcess) {
                    while (currentPointer.get() < toProcess.size()) {
                        final AtomicBoolean stop = new AtomicBoolean();
//                        JavaSource js = JavaSource.create(e.getKey(), f);
                        JavaSource js = JavaSource.create(e.getKey(), toProcess.subList(currentPointer.get(), toProcess.size()));

                        js.runUserActionTask(new Task<CompilationController>() {
                            public void run(CompilationController parameter) throws Exception {
                                if (stop.get()) return;

                                //workaround for #192481:
                                if (parameter.toPhase(Phase.PARSED).compareTo(Phase.PARSED) < 0)
                                    return ;

                                boolean cont = true;

                                try {
                                    Context ctx = JavaSourceAccessor.getINSTANCE().getJavacTask(parameter).getContext();
                                    ClassReader reader = ClassReader.instance(ctx);
                                    Field attributeReaders = ClassReader.class.getDeclaredField("attributeReaders");

                                    attributeReaders.setAccessible(true);
                                    ((Map) attributeReaders.get(reader)).remove(Names.instance(ctx)._org_netbeans_ParameterNames);
                                    //workaround for #192481 end

                                    if (parameter.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0)
                                        return ;

                                    progress.setMessage("processing: " + FileUtil.getFileDisplayName(parameter.getFileObject()));
                                    Resource r = file2Resource.get(parameter.getFileObject());

                                    List<ErrorDescription> hints = new HintsInvoker(parameter, new AtomicBoolean()).computeHints(parameter, r.hints, problems);

                                    cont = callback.spansVerified(parameter, r, hints);
                                } catch (ThreadDeath td) {
                                    throw td;
                                } catch (Throwable t) {
                                    LOG.log(Level.INFO, "Exception while performing batch processing in " + FileUtil.getFileDisplayName(parameter.getFileObject()), t);
                                    problems.add(new MessageImpl(MessageKind.WARNING, "An exception occurred while processing file: " + FileUtil.getFileDisplayName(parameter.getFileObject()) + " (" + t.getLocalizedMessage() + ")."));
                                }
                                
                                if (cont) {
                                    progress.tick();
                                    currentPointer.incrementAndGet();
                                } else {
                                    stop.set(true);
                                }
                            }
                        }, true);
                    }

                    callback.groupFinished();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } finally {
            if (toRegister != null) {
                GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, toRegister);
            }
            progress.finish();
        }
    }

    public interface VerifiedSpansCallBack {
        public void groupStarted();
        public boolean spansVerified(CompilationController wc, Resource r, Collection<? extends ErrorDescription> hints) throws Exception;
        public void groupFinished();
        public void cannotVerifySpan(Resource r);
    }

    public abstract static class Scope {

        public abstract String getDisplayName();
        public abstract Collection<? extends FileObject> getTodo();
        public abstract MapIndices getIndexMapper(Iterable<? extends HintDescription> hints);
        
    }
    
    public static final class BatchResult {
        
        private final Map<? extends IndexEnquirer, ? extends Collection<? extends Resource>> projectId2Resources;
        public final Collection<? extends MessageImpl> problems;
        
        public BatchResult(Map<? extends IndexEnquirer, ? extends Collection<? extends Resource>> projectId2Resources, Collection<? extends MessageImpl> problems) {
            this.projectId2Resources = projectId2Resources;
            this.problems = problems;
        }

        public Collection<? extends Collection<? extends Resource>> getResources() {
            return projectId2Resources.values();
        }

        public Map<FileObject, Collection<? extends Resource>> getResourcesWithRoots() {
            Map<FileObject, Collection<? extends Resource>> result = new HashMap<FileObject, Collection<? extends Resource>>();

            for (Entry<? extends IndexEnquirer, ? extends Collection<? extends Resource>> e : projectId2Resources.entrySet()) {
                result.put(e.getKey().src, e.getValue());
            }

            return result;
        }
    }

    public static final class Resource {
        private final IndexEnquirer indexEnquirer;
        private final String relativePath;
        final Iterable<? extends HintDescription> hints;
        private final BulkPattern pattern;

        public Resource(IndexEnquirer indexEnquirer, String relativePath, Iterable<? extends HintDescription> hints, BulkPattern pattern) {
            this.indexEnquirer = indexEnquirer;
            this.relativePath = relativePath;
            this.hints = hints;
            this.pattern = pattern;
        }

        public String getRelativePath() {
            return relativePath;
        }
        
        public Iterable<int[]> getCandidateSpans() {
            FileObject file = getResolvedFile();
            JavaSource js;

            if (file != null) {
                js = JavaSource.forFileObject(file);
            } else {
                CharSequence text = getSourceCode();

                if (text == null) {
                    return null;
                }

                Writer out = null;

                try {
                    file = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), relativePath);
                    out = new OutputStreamWriter(file.getOutputStream());

                    out.write(text.toString());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }

                js = JavaSource.create(Utilities.createUniversalCPInfo(), file);
            }

            final List<int[]> span = new LinkedList<int[]>();

            try {
                js.runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController cc) throws Exception {
                        cc.toPhase(Phase.PARSED);

                        span.addAll(doComputeSpans(cc));
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return span;
        }

        private Collection<int[]> doComputeSpans(CompilationInfo ci) {
            Collection<int[]> result = new LinkedList<int[]>();
            Map<String, Collection<TreePath>> found = BulkSearch.getDefault().match(ci, new TreePath(ci.getCompilationUnit()), pattern);
            
            for (Entry<String, Collection<TreePath>> e : found.entrySet()) {
                Tree treePattern = Utilities.parseAndAttribute(ci, e.getKey(), null);
                
                for (TreePath tp : e.getValue()) {
                    //XXX: this pass will not be performed on the web!!!
                    if (   BulkSearch.getDefault().requiresLightweightVerification()
                        && !CopyFinder.isDuplicate(ci, new TreePath(new TreePath(ci.getCompilationUnit()), treePattern), tp, false, new AtomicBoolean())) {
                        continue;
                    }
                    int[] span = new int[] {
                        (int) ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), tp.getLeaf()),
                        (int) ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), tp.getLeaf())
                    };

                    result.add(span);
                }
            }

            return result;
        }
        
        public FileObject getResolvedFile() {
            return indexEnquirer.src.getFileObject(relativePath);
        }

        public String getDisplayName() {
            FileObject file = getResolvedFile();

            if (file != null) {
                return FileUtil.getFileDisplayName(file);
            } else {
                return relativePath; //TODO:+container
            }
        }
        
        public CharSequence getSourceCode() {
            try {
                FileObject file = getResolvedFile();
                ByteBuffer bb = ByteBuffer.wrap(file.asBytes());

                return FileEncodingQuery.getEncoding(file).decode(bb);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        public FileObject getRoot() {
            return indexEnquirer.src;
        }
    }

    public static interface MapIndices {
        public IndexEnquirer findIndex(FileObject root, ProgressHandleWrapper progress);
    }

    public static abstract class IndexEnquirer {
        final FileObject src;
        public IndexEnquirer(FileObject src) {
            this.src = src;
        }
        public abstract Collection<? extends Resource> findResources(Iterable<? extends HintDescription> hints, ProgressHandleWrapper progress, Callable<BulkPattern> bulkPattern, Collection<? super MessageImpl> problems);
        public abstract void validateResource(Collection<? extends Resource> resources, ProgressHandleWrapper progress, VerifiedSpansCallBack callback, boolean doNotRegisterClassPath, Collection<? super MessageImpl> problems);
//        public int[] getEstimatedSpan(Resource r);
    }

    public static abstract class LocalIndexEnquirer extends IndexEnquirer {
        public LocalIndexEnquirer(FileObject src) {
            super(src);
        }
        public void validateResource(Collection<? extends Resource> resources, ProgressHandleWrapper progress, VerifiedSpansCallBack callback, boolean doNotRegisterClassPath, Collection<? super MessageImpl> problems) {
            getLocalVerifiedSpans(resources, progress, callback, false/*XXX*/, problems);
        }
    }

    public static final class FileSystemBasedIndexEnquirer extends LocalIndexEnquirer {
        public FileSystemBasedIndexEnquirer(FileObject src) {
            super(src);
        }
        public Collection<? extends Resource> findResources(final Iterable<? extends HintDescription> hints, ProgressHandleWrapper progress, final Callable<BulkPattern> bulkPattern, final Collection<? super MessageImpl> problems) {
            Collection<FileObject> files = new LinkedList<FileObject>();

            final ProgressHandleWrapper innerProgress = progress.startNextPartWithEmbedding(30, 70);

            BatchUtilities.recursive(src, src, files, innerProgress, 0, null, null);

            LOG.log(Level.FINE, "files: {0}", files);

            innerProgress.startNextPart(files.size());

            final Collection<Resource> result = new ArrayList<Resource>();

            if (!files.isEmpty()) {
                try {
                    long start = System.currentTimeMillis();

                    JavaSource.create(Utilities.createUniversalCPInfo(), files).runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController cc) throws Exception {
                            if (cc.toPhase(Phase.PARSED).compareTo(Phase.PARSED) <0) {
                                return ;
                            }

                            try {
                                boolean matches = BulkSearch.getDefault().matches(cc, new TreePath(cc.getCompilationUnit()), bulkPattern.call());

                                if (matches) {
                                    result.add(new Resource(FileSystemBasedIndexEnquirer.this, FileUtil.getRelativePath(src, cc.getFileObject()), hints, bulkPattern.call()));
                                }
                            } catch (ThreadDeath td) {
                                throw td;
                            } catch (Throwable t) {
                                LOG.log(Level.INFO, "Exception while performing batch search in " + FileUtil.getFileDisplayName(cc.getFileObject()), t);
                                problems.add(new MessageImpl(MessageKind.WARNING, "An exception occurred while testing file: " + FileUtil.getFileDisplayName(cc.getFileObject()) + " (" + t.getLocalizedMessage() + ")."));
                            }

                            innerProgress.tick();
                        }
                    }, true);

                    long end = System.currentTimeMillis();

                    LOG.log(Level.FINE, "took: {0}, per file: {1}", new Object[]{end - start, (end - start) / files.size()});
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            return result;
        }

    }

}
