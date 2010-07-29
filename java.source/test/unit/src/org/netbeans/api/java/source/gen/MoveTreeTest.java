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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests method type parameters changes.
 * 
 * @author Pavel Flaska
 */
public class MoveTreeTest extends GeneratorTest {

    static {
        System.setProperty("org.netbeans.api.java.source.WorkingCopy.keep-old-trees", "true");
    }
    
    /** Creates a new instance of MethodParametersTest */
    public MoveTreeTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(MoveTreeTest.class);
        return suite;
    }
    
    public void testMoveExpression1() throws Exception {
        performMoveExpressionTest(
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 = 1+    2    *3;\n" +
            "        int i2 = 0;\n" +
            "    }\n" +
            "}\n",
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 = 1+    2    *3;\n" +
            "        int i2 = 1+    2    *3;\n" +
            "    }\n" +
            "}\n");
    }

    public void testMoveExpression2() throws Exception {
        performMoveExpressionTest(
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 = 1+    \n" +
            "                 2    *3;\n" +
            "        int foo = 0;\n" +
            "    }\n" +
            "}\n",
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 = 1+    \n" +
            "                 2    *3;\n" +
            "        int foo = 1+    \n" +
            "                  2    *3;\n" +
            "    }\n" +
            "}\n");
    }

    public void testMoveExpression3() throws Exception {
        performMoveExpressionTest(
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 =\n" +
            "            1+    \n" +
            "            2    *3;\n" +
            "        int foo = 0;\n" +
            "    }\n" +
            "}\n",
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 =\n" +
            "            1+    \n" +
            "            2    *3;\n" +
            "        int foo = 1+    \n" +
            "                  2    *3;\n" +
            "    }\n" +
            "}\n");
    }

    public void testMoveExpression4() throws Exception {
        performMoveExpressionTest(
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 =\n" +
            "                        1+    \n" +
            "                         2    *3;\n" +
            "        int foo = 0;\n" +
            "    }\n" +
            "}\n",
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        int i1 =\n" +
            "                        1+    \n" +
            "                         2    *3;\n" +
            "        int foo = 1+    \n" +
            "                   2    *3;\n" +
            "    }\n" +
            "}\n");
    }

    private void performMoveExpressionTest(String code, String golden) throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, code);

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var1 = (VariableTree) method.getBody().getStatements().get(0);
                VariableTree var2 = (VariableTree) method.getBody().getStatements().get(1);

                workingCopy.rewrite(var2.getInitializer(), var1.getInitializer());
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testMoveExpressionToStatement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public int taragui(String s1, String s2) {\n" +
            "        int i1 = taragui(\"foo\",\n" +
            "                         \"bar\");\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public int taragui(String s1, String s2) {\n" +
            "        taragui(\"foo\",\n" +
            "                \"bar\");\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);

                workingCopy.rewrite(var, workingCopy.getTreeMaker().ExpressionStatement(var.getInitializer()));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMoveMethod() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    private static class A {\n" +
            "        public void taragui() {\n" +
            "            int i1 = 1+    2    *3;\n" +
            "            int i2 = 1+    2    *3;\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    private static class A {\n" +
            "    }\n" +
            "\n" +
            "    public void taragui() {\n" +
            "        int i1 = 1+    2    *3;\n" +
            "        int i2 = 1+    2    *3;\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree clazzInner = (ClassTree) clazz.getMembers().get(1);
                MethodTree method = (MethodTree) clazzInner.getMembers().get(1);

                workingCopy.rewrite(clazz, make.addClassMember(clazz, method));
                workingCopy.rewrite(clazzInner, make.removeClassMember(clazzInner, method));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMoveStatements() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        System.err.println(1);\n" +
            "        System.err.println(2);\n" +
            "\n" +
            "\n" +
            "        System.err.println(3);System.err.println(3.5);\n" +
            "        System.     err.\n" +
            "                        println(4);\n" +
            "        System.err.println(5);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        System.err.println(1);\n" +
            "        {\n" +
            "            System.err.println(2);\n" +
            "\n" +
            "\n" +
            "            System.err.println(3);System.err.println(3.5);\n" +
            "            System.     err.\n" +
            "                            println(4);\n" +
            "        }\n" +
            "        System.err.println(5);\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree body = method.getBody();
                BlockTree inner = make.Block(body.getStatements().subList(1, 5), false);
                BlockTree nue = make.Block(Arrays.asList(body.getStatements().get(0), inner, body.getStatements().get(5)), false);

                workingCopy.rewrite(body, nue);
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
