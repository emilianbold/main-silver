/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import junit.framework.Test;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.api.Scope;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ralph Ruijs
 */
public class FindUsagesTest extends NbTestCase {
    private FileObject projectDir;
    private static final Logger LOG = Logger.getLogger(FindUsagesTest.class.getName());

    public FindUsagesTest(String name) {
        super(name);
    }

    /**
     * Set-up the services and project
     */
    @Override
    protected void setUp() throws IOException, InterruptedException {
        clearWorkDir();
        String work = getWorkDirPath();
        System.setProperty("netbeans.user", work);
        projectDir = Utilities.openProject("SimpleJ2SEApp", getDataDir());
    }
    
    public void testFindUsages() throws IOException, InterruptedException, ExecutionException {
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/Main.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("simplej2seapp.Main");
                Element field = klass.getEnclosedElements().get(4);
                TreePathHandle element = TreePathHandle.create(field, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(element));
            }
        }, false).get();
        setScope(wuq, true, true, false, false, false, false);
        
        doRefactoring("FindUsagesTest", wuq, 7);
    }
    
    public void testFindCurrentPackage() throws IOException, InterruptedException, ExecutionException {
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/Main.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("simplej2seapp.Main");
                Element field = klass.getEnclosedElements().get(4);
                TreePathHandle element = TreePathHandle.create(field, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(element));
            }
        }, false).get();
        setScope(wuq, true, true, false, false, false, false);
        final NonRecursiveFolder package1 = new NonRecursiveFolder() {
            public FileObject getFolder() {
                return projectDir.getFileObject("/src/package1");
            }
        };
        Collection<NonRecursiveFolder> folders = Arrays.asList(package1);
        Scope scope = Scope.create(null, folders, null);
        wuq[0].getContext().add(scope);

        doRefactoring("FindCurrentPackageTest", wuq, 1);
    }
    
    public void testFindSingleSourceRoot() throws IOException, InterruptedException, ExecutionException {
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/Main.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("simplej2seapp.Main");
                Element field = klass.getEnclosedElements().get(4);
                TreePathHandle element = TreePathHandle.create(field, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(element));
            }
        }, false).get();

        setScope(wuq, true, true, false, false, false, false);
        
        Scope customScope = Scope.create(Arrays.asList(projectDir.getFileObject("/test")), null, null);
        wuq[0].getContext().add(customScope);

        doRefactoring("FindSingleSourceRootTest", wuq, 1);
    }
    
    public void testFindComplexScope() throws IOException, InterruptedException, ExecutionException {
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/Main.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("simplej2seapp.Main");
                Element field = klass.getEnclosedElements().get(4);
                TreePathHandle element = TreePathHandle.create(field, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(element));
            }
        }, false).get();

        setScope(wuq, true, true, false, false, false, false);
        final NonRecursiveFolder package1 = new NonRecursiveFolder() {
            public FileObject getFolder() {
                return projectDir.getFileObject("/src/package1");
            }
        };
        Scope customScope = Scope.create(Arrays.asList(projectDir.getFileObject("/test")),
                                         Arrays.asList(package1),
                                         Arrays.asList(projectDir.getFileObject("/src/simplej2seapp/B.java")));
        wuq[0].getContext().add(customScope);

        doRefactoring("FindComplexScopeTest", wuq, 3);
    }
    
    public void testFindSubclasses() throws IOException, InterruptedException, ExecutionException {
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/Main.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        final CharSequence[] symbolName = new CharSequence[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("simplej2seapp.Main");
                symbolName[0] = klass.getSimpleName();
                wuq[0] = new WhereUsedQuery(Lookups.singleton(TreePathHandle.create(klass, controller)));
            }
        }, false).get();
        
        setScope(wuq, false, false, true, false, false, false);

        doRefactoring("FindSubClassesTest", wuq, 3);
    }
    
    public void test200230() throws IOException, InterruptedException, ExecutionException {
        Utilities.openProject("SimpleJ2SEAppChild", getDataDir());
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/Main.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("simplej2seapp.Main");
                Element field = klass.getEnclosedElements().get(4);
                TreePathHandle element = TreePathHandle.create(field, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(element));
            }
        }, false).get();
        setScope(wuq, true, true, false, false, false, false);

        doRefactoring("FindCurrentPackageTest", wuq, 9);
    }
    
    private void doRefactoring(final String name, final WhereUsedQuery[] wuq, final int amount) {
        RefactoringSession rs = RefactoringSession.create("Session");

        wuq[0].prepare(rs);
        rs.doRefactoring(true);
        
        Collection<RefactoringElement> elems = rs.getRefactoringElements();

        LOG.fine(name);
        for (RefactoringElement refactoringElement : elems) {
            LOG.fine(refactoringElement.getParentFile().getNameExt());
        }
        
        assertEquals("Number of usages", amount, elems.size());
    }
    
    private void setScope(final org.netbeans.modules.refactoring.api.WhereUsedQuery[] wuq,
            boolean references, boolean comments, boolean subclasses,
            boolean directSubclasses, boolean overriding, boolean fromBaseclass) {
        wuq[0].putValue(WhereUsedQuery.FIND_REFERENCES, references);
        wuq[0].putValue(WhereUsedQuery.SEARCH_IN_COMMENTS, comments);
        wuq[0].putValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES, directSubclasses);
        wuq[0].putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, overriding);
        wuq[0].putValue(WhereUsedQueryConstants.FIND_SUBCLASSES, subclasses);
        wuq[0].putValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS, fromBaseclass);
    }
    
    public static Test suite() throws InterruptedException {
        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration().addTest(FindUsagesTest.class, "test200230")
                .clusters(".*").enableModules(".*")
                .gui(false));
    }
}
