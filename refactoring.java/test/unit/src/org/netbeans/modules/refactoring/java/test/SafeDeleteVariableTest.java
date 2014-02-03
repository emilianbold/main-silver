/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import static org.netbeans.modules.refactoring.java.test.RefactoringTestBase.addAllProblems;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class SafeDeleteVariableTest extends RefactoringTestBase {

    public SafeDeleteVariableTest(String name) {
        super(name, "1.8");
    }
    
    public void testPackage() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        for(int i = 0; i< 10; i++) {\n"
                + "            A a = new A();\n"
                + "        }\n"
                + "    }\n"
                + "}\n"));
        performSafeDelete(src.getFileObject("t"), -1, false);
        verifyContent(src);
    }
    
    public void testForVariable() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        for(int i = 0; i< 10; i++) {\n"
                + "            A a = new A();\n"
                + "        }\n"
                + "    }\n"
                + "}\n"));
        performSafeDelete(src.getFileObject("t/A.java"), source.indexOf(" i") + 1, false, new Problem(true, "ERR_VarNotInBlockOrMethod"));
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        for(int i = 0; i< 10; i++) {\n"
                + "            A a = new A();\n"
                + "        }\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testMainArgs() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        A a = new A();\n"
                + "    }\n"
                + "}\n"));
        performSafeDelete(src.getFileObject("t/A.java"), source.indexOf("args") + 1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void main() {\n"
                + "        A a = new A();\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testMainArgsUsed() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        for (int i = 0; i < args.length; i++) {\n"
                + "            String string = args[i];\n"
                + "        }\n"
                + "    }\n"
                + "}\n"));
        performSafeDelete(src.getFileObject("t/A.java"), source.indexOf("args") + 1, false, new Problem(false, "WRN_ImplementsFound"));
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void main() {\n"
                + "        for (int i = 0; i < args.length; i++) {\n"
                + "            String string = args[i];\n"
                + "        }\n"
                + "    }\n"
                + "}\n"));
    }
    
    private void performSafeDelete(FileObject source, final int position, final boolean checkInComments, Problem... expectedProblems) throws Exception {
        final SafeDeleteRefactoring[] r = new SafeDeleteRefactoring[1];
        
        if(source.isFolder()) {
            r[0] = new SafeDeleteRefactoring(Lookups.fixed(source));
            r[0].setCheckInComments(checkInComments);
        } else {

            JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController javac) throws Exception {
                    javac.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cut = javac.getCompilationUnit();

                    TreePath tp = javac.getTreeUtilities().pathFor(position);

                    r[0] = new SafeDeleteRefactoring(Lookups.fixed(TreePathHandle.create(tp, javac)));
                    r[0].setCheckInComments(checkInComments);
                }
            }, true);
        }

        RefactoringSession rs = RefactoringSession.create("Introduce Parameter");
        List<Problem> problems = new LinkedList<>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);
    }
    
}
