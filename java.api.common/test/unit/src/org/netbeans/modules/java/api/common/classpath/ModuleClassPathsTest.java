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
package org.netbeans.modules.java.api.common.classpath;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import javax.lang.model.element.ModuleElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestJavaPlatform;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.RequestProcessor;
import org.openide.util.test.MockLookup;

/**
 * Tests for ModuleClassPaths.
 * Todo: add -Xmodule test, add requires transitive test
 * @author Tomas Zezula
 */
public class ModuleClassPathsTest extends NbTestCase {
    
    private static final Comparator<Object> LEX_COMPARATOR =
            (a,b) -> a.toString().compareTo(b.toString());
    private static final Predicate<ModuleElement> NON_JAVA_PUB = (e) -> 
                           !e.getQualifiedName().toString().startsWith("java.") &&  //NOI18N
                            e.getDirectives().stream()
                                .filter((d) -> d.getKind() == ModuleElement.DirectiveKind.EXPORTS)
                                .map((d) -> (ModuleElement.ExportsDirective)d)
                                .anyMatch((ed) -> ed.getTargetModules() == null);
    
    private SourceRoots src;
    private ClassPath systemModules;
    private FileObject automaticModuleRoot;
    private FileObject jarFileRoot;
    
    public ModuleClassPathsTest(@NonNull final String name) {
        super(name);
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final FileObject workDir = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        MockLookup.setInstances(TestProject.createProjectType(), new MockCompilerOptions());
        final FileObject prjDir = FileUtil.createFolder(workDir, "TestProject");    //NOI18N
        assertNotNull(prjDir);
        final FileObject srcDir = FileUtil.createFolder(prjDir, "src");    //NOI18N
        assertNotNull(srcDir);
        final Project prj = TestProject.createProject(prjDir, srcDir, null);
        assertNotNull(prj);
        setSourceLevel(prj, "9");   //NOI18N
        src = Optional.ofNullable(prj.getLookup().lookup(TestProject.class))
                .map((p) -> p.getSourceRoots())
                .orElse(null);
        systemModules = Optional.ofNullable(TestUtilities.getJava9Home())
                .map((jh) -> TestJavaPlatform.createModularPlatform(jh))
                .map((jp) -> jp.getBootstrapLibraries())
                .orElse(null);
        final FileObject libs = FileUtil.createFolder(workDir, "libs"); //NOI18N
        automaticModuleRoot = createJar(libs,"automatic.jar");  //NOI18N
        assertNotNull(automaticModuleRoot);
        jarFileRoot = createJar(libs,"legacy.jar"); //NOI18N
        assertNotNull(jarFileRoot);
    }
    
   
    public void testModuleInfoBasedCp_SystemModules_in_UnnamedModule() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = reads(
                systemModules,
                NamePredicate.create("java.se").or(NON_JAVA_PUB));  //NOI18N
        assertEquals(expectedURLs, resURLs);
    }
    
    
    public void testModuleInfoBasedCp_SystemModules_in_NamedEmptyModule() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        createModuleInfo(src, "Modle"); //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = reads(systemModules, NamePredicate.create("java.base"));  //NOI18N
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBasedCp_SystemModules_in_NamedModule() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        createModuleInfo(src, "Modle", "java.compact1"); //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = reads(systemModules, NamePredicate.create("java.compact1"));  //NOI18N
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBasedCp_UserModules_in_UnnamedModule() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(automaticModuleRoot);
        final ClassPath legacyCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(jarFileRoot);
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                src,
                systemModules,
                legacyCp,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = legacyCp.entries().stream()
                .map((e) -> e.getURL())
                .collect(Collectors.toList());
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBasedCp_UserModules_in_UnnamedModule_AddMods() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(automaticModuleRoot);
        final ClassPath legacyCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(jarFileRoot);
        final MockCompilerOptions opts = MockCompilerOptions.getInstance();
        assertNotNull("No MockCompilerOptions in Lookup", opts);
        opts.forRoot(src.getRoots()[0])
                .apply("--add-modules")    //NOI18N
                .apply(SourceUtils.getModuleName(automaticModuleRoot.toURL()));
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                src,
                systemModules,
                legacyCp,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        //Modules from add-modules + cp
        final Collection<URL> expectedURLs = new ArrayList<>();
        expectedURLs.add(automaticModuleRoot.toURL());
        legacyCp.entries().stream()
                .map((e) -> e.getURL())
                .forEach(expectedURLs::add);
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBasedCp_UserModules_in_NamedModule() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(automaticModuleRoot);
        final ClassPath legacyCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(jarFileRoot);
        createModuleInfo(src, "Modle", "java.compact1",SourceUtils.getModuleName(automaticModuleRoot.toURL())); //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                src,
                systemModules,
                legacyCp,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        //Modules from declared dependencies
        final Collection<URL> expectedURLs = new ArrayList<>();
        expectedURLs.add(automaticModuleRoot.toURL());
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBasedCp_UserModules_in_NamedModule_AddMods() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(automaticModuleRoot);
        final ClassPath legacyCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(jarFileRoot);
        createModuleInfo(src, "Modle", "java.compact1"); //NOI18N
        final MockCompilerOptions opts = MockCompilerOptions.getInstance();
        assertNotNull("No MockCompilerOptions in Lookup", opts);
        opts.forRoot(src.getRoots()[0])
                .apply("--add-modules")    //NOI18N
                .apply(SourceUtils.getModuleName(automaticModuleRoot.toURL()));
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                src,
                systemModules,
                legacyCp,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        //Modules from declared dependencies - nothing
        final Collection<URL> expectedURLs = new ArrayList<>();
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBasedCp_UserModules_in_NamedModule_AddRead() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(automaticModuleRoot);
        final ClassPath legacyCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(jarFileRoot);
        createModuleInfo(src, "Modle", "java.compact1"); //NOI18N
        final MockCompilerOptions opts = MockCompilerOptions.getInstance();
        assertNotNull("No MockCompilerOptions in Lookup", opts);
        opts.forRoot(src.getRoots()[0])
                .apply("--add-reads")    //NOI18N
                .apply("Modle="+SourceUtils.getModuleName(automaticModuleRoot.toURL()));    //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                src,
                systemModules,
                legacyCp,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        //Modules from declared dependencies + add-reads
        final Collection<URL> expectedURLs = new ArrayList<>();
        expectedURLs.add(automaticModuleRoot.toURL());
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBasedCp_UserModules_in_NamedModule_AddReadAllUnnamed() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(automaticModuleRoot);
        final ClassPath legacyCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(jarFileRoot);
        createModuleInfo(src, "Modle", "java.compact1"); //NOI18N
        final MockCompilerOptions opts = MockCompilerOptions.getInstance();
        assertNotNull("No MockCompilerOptions in Lookup", opts);
        opts.forRoot(src.getRoots()[0])
                .apply("--add-reads")    //NOI18N
                .apply("Modle=ALL-UNNAMED");    //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                src,
                systemModules,
                legacyCp,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        //Modules from declared dependencies + add-reads
        final Collection<URL> expectedURLs = legacyCp.entries().stream()
                .map((e) -> e.getURL())
                .collect(Collectors.toList());
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testProjectMutexWriteDeadlock() throws Exception {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final FileObject moduleInfo = createModuleInfo(src, "Modle", "java.compact1"); //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                null));
        final RequestProcessor deadLockMaker = new RequestProcessor("DeadLock Maker", 1);
        final ClasspathInfo info = new ClasspathInfo.Builder(systemModules)
                .build();
        final CountDownLatch startThread = new CountDownLatch(1);
        final CountDownLatch startSelf = new CountDownLatch(1);
        final CountDownLatch endThread = new CountDownLatch(1);
        deadLockMaker.execute(() -> {
            try {
                final JavaSource js = JavaSource.create(info);
                js.runUserActionTask((cc)->{
                        startThread.await();
                        startSelf.countDown();
                        ProjectManager.mutex().readAccess(()->{
                            System.out.println("EXECUTED");
                        });
                    },
                        true);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            } finally {
                endThread.countDown();
            }
        });
        ProjectManager.mutex().writeAccess(()-> {
            try {
                startThread.countDown();
                startSelf.await();
                cp.entries();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });                
        endThread.await();
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = reads(systemModules, NamePredicate.create("java.compact1"));  //NOI18N
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testPatchModule() throws Exception {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        createModuleInfo(src, "Modle", "java.compact1"); //NOI18N
        final FileObject compact1Patch1 = createPatchFolder("java.compact1.patch1");    //NOI18N
        final FileObject compact1Patch2 = createPatchFolder("java.compact1.patch2");    //NOI18N
        final MockCompilerOptions opts = MockCompilerOptions.getInstance();
        assertNotNull("No MockCompilerOptions in Lookup", opts);
        opts.forRoot(src.getRoots()[0])
                .apply("--patch-module")    //NOI18N
                .apply(String.format(
                        "java.compact1=%s:%s",  //NOI18N
                        FileUtil.toFile(compact1Patch1).getAbsolutePath(),
                        FileUtil.toFile(compact1Patch2).getAbsolutePath()));        
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                null));
        final Collection<URL> resURLs = collectEntries(cp);     
        final Collection<URL> expectedURLs = reads(
                systemModules,
                NamePredicate.create("java.compact1"),   //NOI18N
                compact1Patch1,
                compact1Patch2);  
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testPatchModuleWithDuplicates() throws Exception {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        createModuleInfo(src, "Modle", "java.compact1"); //NOI18N
        final FileObject compact1Patch1 = createPatchFolder("java.compact1.patch1");    //NOI18N
        final FileObject compact1Patch2 = createPatchFolder("java.compact1.patch2");    //NOI18N
        final MockCompilerOptions opts = MockCompilerOptions.getInstance();
        assertNotNull("No MockCompilerOptions in Lookup", opts);
        opts.forRoot(src.getRoots()[0])
                .apply("--patch-module")    //NOI18N
                .apply(String.format(
                        "java.compact1=%s",//NOI18N
                        FileUtil.toFile(compact1Patch1).getAbsolutePath()))
                .apply("--patch-module")    //NOI18N
                .apply(String.format("java.compact1=%s",  //NOI18N
                        FileUtil.toFile(compact1Patch2).getAbsolutePath()));
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                null));
        final Collection<URL> resURLs = collectEntries(cp);     
        final Collection<URL> expectedURLs = reads(
                systemModules,
                NamePredicate.create("java.compact1"),   //NOI18N
                compact1Patch1);  
        assertEquals(expectedURLs, resURLs);
    }
    
    private static void setSourceLevel(
            @NonNull final Project prj,
            @NonNull final String sourceLevel) throws IOException {
        try {
            final TestProject tprj = prj.getLookup().lookup(TestProject.class);
            if (tprj == null) {
                throw new IllegalStateException("No TestProject instance: " + prj); //NOI18N
            }
            ProjectManager.mutex().writeAccess((Mutex.ExceptionAction<Void>)()->{
                final EditableProperties ep = tprj.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty(ProjectProperties.JAVAC_SOURCE, sourceLevel);
                ep.setProperty(ProjectProperties.JAVAC_TARGET, sourceLevel);
                tprj.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(prj);
                return null;
            });
        } catch (MutexException e) {
            throw e.getCause() instanceof IOException ?
                    (IOException) e.getCause() :
                    new IOException(e.getCause());
        }
    }
    
    @NonNull
    private static FileObject createModuleInfo(
            @NonNull final SourceRoots src,
            @NonNull final String moduleName,
            @NonNull final String... requiredModules) throws IOException {
        final FileObject[] roots = src.getRoots();
        if (roots.length == 0) {
            throw new IOException("No source roots");   //NOI18N
        }
        final FileObject[] res = new FileObject[1];
        FileUtil.runAtomicAction((FileSystem.AtomicAction)() -> {
                res[0] = FileUtil.createData(roots[0], "module-info.java");    //NOI18N
                final StringBuilder module = new StringBuilder("module ").append(moduleName).append(" {");    //NOI18N
                for (String mod : requiredModules) {
                    module.append("requires ").append(mod).append(";");
                }
                module.append("}"); //NOI18N
                final FileLock lck = res[0].lock();
                try (OutputStream out = res[0].getOutputStream(lck);
                        InputStream in = new ByteArrayInputStream(module.toString().getBytes(FileEncodingQuery.getEncoding(res[0])))) {
                    FileUtil.copy(in, out);
                } finally {
                    lck.releaseLock();
                }
        });
        return res[0];
    }
    
    private FileObject createPatchFolder(final String name) throws IOException {
        return FileUtil.createFolder(FileUtil.normalizeFile(
                new File(getWorkDir(),name)));
    }
    
    private Collection<URL> collectEntries(@NonNull final ClassPath cp) {
        final List<URL> res = new ArrayList<>();
        for (ClassPath.Entry e : cp.entries()) {
            res.add(e.getURL());
        }
        Collections.sort(res, LEX_COMPARATOR);
        return res;
    }
    
    private Collection<URL> reads(
            @NonNull final ClassPath base,
            @NonNull final Predicate<ModuleElement> predicate,
            @NonNull final FileObject... additionalRoots) throws IOException {
        final ClasspathInfo info = new ClasspathInfo.Builder(base)
                .setModuleBootPath(base)
                .build();
        final Set<String> moduleNames = new HashSet<>();
        JavaSource.create(info).runUserActionTask((cc)-> {
                final Set<ModuleElement> rootModules = base.entries().stream()
                        .map((e) -> SourceUtils.getModuleName(e.getURL()))
                        .filter((n) -> n != null)
                        .map(cc.getElements()::getModuleElement)
                        .filter((m) -> m != null)
                        .filter(predicate)
                        .collect(Collectors.toSet());
                for (ModuleElement rootModule : rootModules) {
                    collectDeps(rootModule, moduleNames);
                }
            },
            true);
        final List<URL> l = new ArrayList<>();
        base.entries().stream()
                .map((e) -> e.getURL())
                .filter((url) -> moduleNames.contains(SourceUtils.getModuleName(url)))
                .forEach(l::add);
        Arrays.stream(additionalRoots)
                .map((fo) -> fo.toURL())
                .forEach(l::add);
        Collections.sort(l, LEX_COMPARATOR);
        return l;
    }
    
    private static void collectDeps(
        @NullAllowed final ModuleElement me,
        @NonNull final Collection<? super String> modNames) {
        if (me != null) {
            final String mn = me.getQualifiedName().toString();
            if (!modNames.contains(mn)) {
                modNames.add(mn);
                for (ModuleElement.Directive d : me.getDirectives()) {
                    if (d.getKind() == ModuleElement.DirectiveKind.REQUIRES) {
                        final ModuleElement.RequiresDirective rd = (ModuleElement.RequiresDirective)d;
                        if (rd.isPublic() || isMandated(rd)) {
                            collectDeps(rd.getDependency(), modNames);
                        }
                    }
                }
            }
        }
    }
    
    private static boolean isMandated(ModuleElement.RequiresDirective rd) {
        return Optional.ofNullable(rd.getDependency())
                .map((me) -> me.getQualifiedName().toString())
                .map((mn) -> "java.base".equals(mn))
                .orElse(Boolean.FALSE);
    }

    private static FileObject createJar(
            @NonNull final FileObject folder,
            @NonNull final String name,
            @NonNull final FileObject... content) throws IOException {
           final File f = FileUtil.toFile(folder);
           if (f == null) {
               throw new IOException("The " + FileUtil.getFileDisplayName(folder) +" is not local");
           }
           final File res = new File(f,name);
           try(JarOutputStream out = new JarOutputStream(new FileOutputStream(res))) {
               for (FileObject c : content) {
                   pack(out, c, c);
               }
           }
           return FileUtil.getArchiveRoot(FileUtil.toFileObject(res));
    }

    private static void pack(
            JarOutputStream out,
            FileObject f,
            FileObject root) throws IOException {
        if (f.isFolder()) {
            for (FileObject c : f.getChildren()) {
                pack(out, c, root);
            }
        } else {
            String path = FileUtil.getRelativePath(root, f);
            if (path.isEmpty()) {
                path = f.getNameExt();
            }
            out.putNextEntry(new ZipEntry(path));
            FileUtil.copy(f.getInputStream(), out);
            out.closeEntry();
        }
    }

    private static boolean compileModule(
            @NonNull final File src,
            @NonNull final File dest,
            @NonNull final File output) throws IOException, InterruptedException {
        final File f = TestUtilities.getJava9Home();
        if (f == null) {
            return false;
        }
        final FileObject jdkHome = FileUtil.toFileObject(f);
        if (jdkHome == null) {
            throw new IOException("Cannot find: " + f.getAbsolutePath());
        }
        final FileObject javac = Util.findTool("javac", Collections.singleton(jdkHome));
        if (javac == null) {
            throw new IOException("No javac in: " + f.getAbsolutePath());
        }
        final List<String> toCompile = Files.walk(src.toPath())
                .filter((p) -> p.getFileName().toString().endsWith(".java"))
                .map((p) -> p.toAbsolutePath().toString())
                .collect(Collectors.toList());
        if (toCompile.isEmpty()) {
            return true;
        }
        final List<String> cmd = new ArrayList<>(toCompile.size() + 7);
        cmd.add(FileUtil.toFile(javac).getAbsolutePath());
        cmd.add("-s");  //NOI18N
        cmd.add(src.getAbsolutePath());
        cmd.add("-d");  //NOI18N
        cmd.add(dest.getAbsolutePath());
        cmd.add("-source"); //NOI18N
        cmd.add("9");   //NOI18N
        cmd.addAll(toCompile);
        final ProcessBuilder pb = new ProcessBuilder(cmd)
            .redirectErrorStream(true)
            .redirectOutput(output);
        final Process p = pb.start();
        return p.waitFor() == 0;
    }

    private static final class NamePredicate implements Predicate<ModuleElement> {
        private final String name;
                
        private NamePredicate(@NonNull final String name) {
            this.name = name;
        }

        @Override
        public boolean test(ModuleElement t) {
            return name.equals(t.getQualifiedName().toString());
        }       
        
        @NonNull
        static Predicate<ModuleElement> create(@NonNull final String name) {
            return new NamePredicate(name);
        }
    } 
    
    private static final class MockCompilerOptions implements CompilerOptionsQueryImplementation {
        private final Map<FileObject,List<String>> options = new HashMap<>();
        
        @NonNull
        <T extends Function<String, T>> Function<String,T> forRoot(@NonNull final FileObject root) {
            final List<String> args = options.computeIfAbsent(root, (k) -> new ArrayList<>());
            args.clear();
            return new Function<String,T> () {
                @Override
                public T apply(String t) {
                    args.add(t);
                    return (T) this;
                }
            };
        }

        @Override
        @NonNull
        public Result getOptions(FileObject file) {
            for (Map.Entry<FileObject,List<String>> option : options.entrySet()) {
                if (isArtifact(option.getKey(), file)) {
                    return new R(option.getValue());
                }
            }
            return null;
        }
        
        @CheckForNull
        static MockCompilerOptions getInstance() {
            return Lookup.getDefault().lookup(MockCompilerOptions.class);
        }
        
        private static boolean isArtifact(
                @NonNull final FileObject root,
                @NonNull final FileObject file) {
            return root.equals(file) || FileUtil.isParentOf(root, file);
        }
        
        private static final class R extends Result {
            private final List<String> args;
            
            R(@NonNull final List<String> args) {
                this.args = args;
            }

            @Override
            @NonNull
            public List<? extends String> getArguments() {
                return Collections.unmodifiableList(args);
            }

            @Override
            public void addChangeListener(ChangeListener listener) {
            }

            @Override
            public void removeChangeListener(ChangeListener listener) {
            }            
        }        
    }
}
