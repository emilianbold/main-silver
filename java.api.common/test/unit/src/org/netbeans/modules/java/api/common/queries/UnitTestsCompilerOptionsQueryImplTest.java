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

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.test.MockChangeListener;
import org.openide.util.test.MockLookup;


/**
 *
 * @author Tomas Zezula
 */
public class UnitTestsCompilerOptionsQueryImplTest extends NbTestCase {

    private TestProject project;
    private SourceRoots srcRoots;
    private SourceRoots testRoots;

    public UnitTestsCompilerOptionsQueryImplTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(TestProject.createProjectType());
        MockMimeLookup.setInstances(MimePath.get("text/x-java"), new JavacParserFactory()); //NOI18N
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject src = FileUtil.createFolder(wd,"src"); //NOI18N
        final FileObject tst = FileUtil.createFolder(wd,"test");    //NOI18N
        final FileObject lib = FileUtil.createFolder(wd,"lib"); //NOI18N
        Project p = TestProject.createProject(wd, src, tst);
        project = p.getLookup().lookup(TestProject.class);
        assertNotNull(project);
        srcRoots = project.getSourceRoots();
        assertNotNull(srcRoots);
        assertEquals(srcRoots.getRoots().length, 1);
        assertEquals(srcRoots.getRoots()[0], src);
        assertNotNull(ClassPath.getClassPath(src, ClassPath.SOURCE));
        assertEquals(Collections.singletonList(src), Arrays.asList(ClassPath.getClassPath(src, ClassPath.SOURCE).getRoots()));
        testRoots = project.getTestRoots();
        assertNotNull(testRoots);
        assertEquals(testRoots.getRoots().length, 1);
        assertEquals(testRoots.getRoots()[0], tst);
        assertNotNull(ClassPath.getClassPath(tst, ClassPath.SOURCE));
        assertEquals(Collections.singletonList(tst), Arrays.asList(ClassPath.getClassPath(tst, ClassPath.SOURCE).getRoots()));
    }

    public void testJDK8() {
        setSourceLevel(project, "1.8"); //NOI18N
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        assertNull(impl.getOptions(srcRoots.getRoots()[0]));
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        final List<? extends String> args = r.getArguments();
        assertEquals(Collections.emptyList(), args);
    }

    public void testJDK9_UnnamedModule() {
        setSourceLevel(project, "9"); //NOI18N
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        assertNull(impl.getOptions(srcRoots.getRoots()[0]));
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        final List<? extends String> args = r.getArguments();
        assertEquals(Collections.emptyList(), args);
    }

    public void testJDK9_TestInlinedIntoSourceModule() throws IOException {
        setSourceLevel(project, "9"); //NOI18N
        final String srcModuleName = "org.nb.App";  //NOI18N
        createModuleInfo(srcRoots.getRoots()[0], srcModuleName);
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        assertNull(impl.getOptions(srcRoots.getRoots()[0]));
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        final List<? extends String> args = r.getArguments();
        assertEquals(
            Arrays.asList(
                String.format("-Xmodule:%s", srcModuleName),    //NOI18N
                String.format("--add-reads %s=ALL-UNNAMED", srcModuleName) //NOI18N
            ),
            args);
    }

    public void testJDK9_TestModule() throws IOException {
        setSourceLevel(project, "9"); //NOI18N
        final String srcModuleName = "org.nb.App";  //NOI18N
        final String testModuleName = "org.nb.AppTest";  //NOI18N
        createModuleInfo(srcRoots.getRoots()[0], srcModuleName);
        createModuleInfo(testRoots.getRoots()[0], testModuleName);
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        assertNull(impl.getOptions(srcRoots.getRoots()[0]));
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        final List<? extends String> args = r.getArguments();
        assertEquals(
            Arrays.asList(
                String.format("--add-reads %s=ALL-UNNAMED", testModuleName) //NOI18N
            ),
            args);
    }

    public void testExplicitArgs() {
        setSourceLevel(project, "9"); //NOI18N
        final List<String> options = Arrays.asList(
                "-Xlint:unchecked",     //NOI18N
                "-Xlint:deprecation"    //NOI18N
        );
        setTestJavacArgs(project,options.stream().collect(Collectors.joining(" ")));    //NOI18N
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        assertNull(impl.getOptions(srcRoots.getRoots()[0]));
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        final List<? extends String> args = r.getArguments();
        assertEquals(options, args);
    }

    public void testSourceLevelChanges() throws IOException {
        setSourceLevel(project, "1.8"); //NOI18N
        final String srcModuleName = "org.nb.App";  //NOI18N
        createModuleInfo(srcRoots.getRoots()[0], srcModuleName);
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        List<? extends String> args = r.getArguments();
        assertEquals(Collections.emptyList(), args);
        final MockChangeListener mcl = new MockChangeListener();
        r.addChangeListener(mcl);
        setSourceLevel(project, "9"); //NOI18N
        mcl.assertEventCount(1);
        args = r.getArguments();
        assertEquals(
            Arrays.asList(
                String.format("-Xmodule:%s", srcModuleName),    //NOI18N
                String.format("--add-reads %s=ALL-UNNAMED", srcModuleName) //NOI18N
            ),
            args);
    }

    public void testRootsChanges() throws IOException {
        setSourceLevel(project, "9"); //NOI18N
        final FileObject src2 = srcRoots.getRoots()[0].getParent().createFolder("src2");
        final String srcModuleName = "org.nb.App";  //NOI18N
        createModuleInfo(src2, srcModuleName);
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        List<? extends String> args = r.getArguments();
        assertEquals(Collections.emptyList(), args);
        final MockChangeListener mcl = new MockChangeListener();
        r.addChangeListener(mcl);
        srcRoots.putRoots(new URL[]{
            srcRoots.getRootURLs()[0],
            src2.toURL()
        }, new String[]{
            srcRoots.getRootNames()[0],
            src2.getName()
        });
        mcl.assertEvent();  //Actually 2 events may come (1 for src, 1 for tests)
        args = r.getArguments();
        assertEquals(
            Arrays.asList(
                String.format("-Xmodule:%s", srcModuleName),    //NOI18N
                String.format("--add-reads %s=ALL-UNNAMED", srcModuleName) //NOI18N
            ),
            args);
    }

    public void testModuleInfoCreation() throws IOException {
        setSourceLevel(project, "9"); //NOI18N
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        assertNull(impl.getOptions(srcRoots.getRoots()[0]));
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        List<? extends String> args = r.getArguments();
        assertEquals(Collections.emptyList(), args);
        final MockChangeListener mcl = new MockChangeListener();
        r.addChangeListener(mcl);
        final String srcModuleName = "org.nb.App";  //NOI18N
        createModuleInfo(srcRoots.getRoots()[0], srcModuleName);
        mcl.assertEventCount(1);
        args = r.getArguments();
        assertEquals(
            Arrays.asList(
                String.format("-Xmodule:%s", srcModuleName),    //NOI18N
                String.format("--add-reads %s=ALL-UNNAMED", srcModuleName) //NOI18N
            ),
            args);
    }

    public void testModuleInfoChanges() throws IOException {
        setSourceLevel(project, "9"); //NOI18N
        final String srcModuleName = "org.nb.App";  //NOI18N
        createModuleInfo(srcRoots.getRoots()[0], srcModuleName);
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        assertNull(impl.getOptions(srcRoots.getRoots()[0]));
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        List<? extends String> args = r.getArguments();
        assertEquals(
            Arrays.asList(
                String.format("-Xmodule:%s", srcModuleName),    //NOI18N
                String.format("--add-reads %s=ALL-UNNAMED", srcModuleName) //NOI18N
            ),
            args);
        final MockChangeListener mcl = new MockChangeListener();
        r.addChangeListener(mcl);
        final String newSrcModuleName = "org.nb.App2";  //NOI18N
        createModuleInfo(srcRoots.getRoots()[0], newSrcModuleName);
        mcl.assertEventCount(1);
        args = r.getArguments();
        assertEquals(
            Arrays.asList(
                String.format("-Xmodule:%s", newSrcModuleName),    //NOI18N
                String.format("--add-reads %s=ALL-UNNAMED", newSrcModuleName) //NOI18N
            ),
            args);
    }

    public void testJavacTestCompilerargsChanges() {
        setSourceLevel(project, "9"); //NOI18N
        final CompilerOptionsQueryImplementation impl = QuerySupport.createUnitTestsCompilerOptionsQuery(project.getEvaluator(), srcRoots, testRoots);
        assertNotNull(impl);
        final CompilerOptionsQueryImplementation.Result r = impl.getOptions(testRoots.getRoots()[0]);
        assertNotNull(r);
        List<? extends String> args = r.getArguments();
        assertEquals(Collections.emptyList(), args);
        final MockChangeListener mcl = new MockChangeListener();
        r.addChangeListener(mcl);
        final List<String> options = Arrays.asList(
                "-Xlint:unchecked",     //NOI18N
                "-Xlint:deprecation"    //NOI18N
        );
        setTestJavacArgs(project,options.stream().collect(Collectors.joining(" ")));    //NOI18N
        mcl.assertEventCount(1);
        args = r.getArguments();
        assertEquals(options, args);
    }


    private static void setSourceLevel(
        @NonNull final TestProject prj,
        @NonNull final String sourceLevel) {
        assertNotNull(prj);
        assertNotNull(sourceLevel);
        ProjectManager.mutex().writeAccess(()-> {
            try {
                final UpdateHelper helper = prj.getUpdateHelper();
                final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty(ProjectProperties.JAVAC_SOURCE, sourceLevel);
                ep.setProperty(ProjectProperties.JAVAC_TARGET, sourceLevel);
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(prj);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        });
        assertEquals(sourceLevel, SourceLevelQuery.getSourceLevel(prj.getProjectDirectory()));
    }

    private static void setTestJavacArgs(
            @NonNull final TestProject prj,
            @NullAllowed final String options) {
        ProjectManager.mutex().writeAccess(()->{
            try {
                final UpdateHelper h = prj.getUpdateHelper();
                final EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                if (options == null) {
                    ep.remove(ProjectProperties.JAVAC_TEST_COMPILERARGS);
                } else {
                    ep.setProperty(ProjectProperties.JAVAC_TEST_COMPILERARGS, options);
                }
                h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(prj);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        });
    }

    private static FileObject createModuleInfo(
        @NonNull final FileObject root,
        @NonNull final String moduleName) throws IOException {
        return TestFileUtils.writeFile(
                root,
                "module-info.java", //NOI18N
                String.format(
                    "module %s {}", //NOI18N
                    moduleName));
    }

}
