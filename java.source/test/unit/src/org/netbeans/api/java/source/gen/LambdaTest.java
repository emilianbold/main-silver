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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberReferenceTree.ReferenceMode;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests correct adding cast to statement.
 *
 * @author Pavel Flaska
 */
public class LambdaTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of AddCastTest */
    public LambdaTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(LambdaTest.class);
        return suite;
    }

    public void testPrintMemberReference() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = null;\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test::taragui;\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitLiteral(LiteralTree node, Void p) {
                        workingCopy.rewrite(node, make.MemberReference(ReferenceMode.INVOKE, make.Identifier("Test"), "taragui", Collections.<ExpressionTree>emptyList()));
                        return super.visitLiteral(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testBasicLambdaDiff() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (f) -> {};\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitVariable(VariableTree node, Void p) {
                        if (node.getName().contentEquals("e")) {
                            workingCopy.rewrite(node, make.setLabel(node, "f"));
                        }
                        return super.visitVariable(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstLambdaParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = () -> {};\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.addLambdaParameter(node, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "e", null, null)));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddSecondLambdaParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e, f) -> {};\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.addLambdaParameter(node, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "f", null, null)));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testPrependSecondLambdaParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (f, e) -> {};\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.insertLambdaParameter(node, 0, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "f", null, null)));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveFirstLambdaParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e, f) -> {};\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (f) -> {};\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.removeLambdaParameter(node, 0));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveSecondLambdaParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e, f) -> {};\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.removeLambdaParameter(node, 1));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testOnlyLambdaParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {};\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = () -> {};\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.removeLambdaParameter(node, 0));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testLambdaFullBody2Expression() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {return 1;};\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> 1;\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        ReturnTree t = (ReturnTree) ((BlockTree) node.getBody()).getStatements().get(0);
                        workingCopy.rewrite(node, make.setLambdaBody(node, t.getExpression()));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testLambdaExpression2FullBody() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> 1;\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        ChangeListener l = (e) -> {\n" +
            "            return 1;\n" +
            "        };\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.setLambdaBody(node, make.Block(Collections.singletonList(make.Return((ExpressionTree) node.getBody())), false)));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testLambdaExpression2FullBodyTreeMatch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.Collections;\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Collections.sort(list, (l, r) -> l.compareTo(r));\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.Collections;\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Collections.sort(list, (l, r) -> {\n" +
            "            return l.compareTo(r);\n" +
            "        });\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        workingCopy.rewrite(node, make.setLambdaBody(node, make.Block(Collections.singletonList(make.Return((ExpressionTree) node.getBody())), false)));
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testMethodReferenceDiff() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = hierbas.del.litoral.Test :: taragui;\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test :: taragui;\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitMemberReference(MemberReferenceTree node, Void p) {
                        workingCopy.rewrite(node, make.MemberReference(node.getMode(), make.Identifier("Test"), node.getName(), node.getTypeArguments()));
                        return super.visitMemberReference(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testMethodReferenceNameDiff() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test :: taragui;\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test :: taragui2;\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitMemberReference(MemberReferenceTree node, Void p) {
                        workingCopy.rewrite(node, make.setLabel(node, "taragui2"));
                        return super.visitMemberReference(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testMethodReferenceFirstTypeParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test::taragui;\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test::<String>taragui;\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitMemberReference(MemberReferenceTree node, Void p) {
                        workingCopy.rewrite(node, make.MemberReference(node.getMode(), node.getQualifierExpression(), node.getName(), Collections.singletonList(make.Identifier("String"))));
                        return super.visitMemberReference(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testMethodReferenceLastTypeParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test::<String>taragui;\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test::taragui;\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitMemberReference(MemberReferenceTree node, Void p) {
                        workingCopy.rewrite(node, make.MemberReference(node.getMode(), node.getQualifierExpression(), node.getName(), null));
                        return super.visitMemberReference(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testLambdaExpressionImplicit2ExplicitParamTypes() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.Collections;\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Collections.sort(list, (l, r) -> l.compareTo(r));\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.Collections;\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Collections.sort(list, (String l, String r) -> l.compareTo(r));\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        for (VariableTree par : node.getParameters()) {
                            workingCopy.rewrite(par.getType(), make.Identifier("String"));
                        }
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testLambdaExpressionExplicit2ImplicitParamTypes() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.Collections;\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Collections.sort(list, (String l, String r) -> l.compareTo(r));\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.Collections;\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Collections.sort(list, (l, r) -> l.compareTo(r));\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                        for (VariableTree par : node.getParameters()) {
                            workingCopy.rewrite(par, make.Variable(par.getModifiers(), par.getName(), null, par.getInitializer()));
                        }
                        return super.visitLambdaExpression(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }
    
    String getSourcePckg() {
        return "";
    }
    
    
}
