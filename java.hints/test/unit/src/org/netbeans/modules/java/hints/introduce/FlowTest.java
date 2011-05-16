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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import static org.junit.Assert.*;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.TestUtilities;
import org.netbeans.modules.java.hints.introduce.Flow.FlowResult;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**TODO: mostly tested indirectly through IntroduceHintTest, should be rather tested here
 *
 * @author lahvac
 */
public class FlowTest extends NbTestCase {

    public FlowTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }

    public void testSimple() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int i) {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        if (i == 0) ii = 3;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2",
                    "3");
    }

    public void testBinary1() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int i) {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = i == 0 && (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2",
                    "3");
    }

    public void testBinary2() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = true && (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "3");
    }

    public void testBinary3() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = false && (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2");
    }

    public void testBinary4() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int i) {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = i == 0 || (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2",
                    "3");
    }

    public void testBinary5() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = false || (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "3");
    }

    public void testBinary6() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = true || (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2");
    }

    public void test197666() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int i) {\n" +
                    "        int ii = 1;\n" +
                    "        boolean b = i == 1 && true;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "1");
    }

    public void test198233() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        boolean b = i == 1 && true;\n" +
                    "        System.err.println(i`i);\n" +
                    "        ===\n" +
                    "    }\n" +
                    "}\n",
                    true,
                    "1");
    }

    public void testIncorrectDeadBranch() throws Exception {
        performDeadBranchTest("package test;\n" +
                              "public class Test {\n" +
                              "    public void i() {\n" +
                              "        if (!i.getAndSet(true)) {\n" +
                              "            System.err.println(\"\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "    private final java.util.concurrent.atomic.AtomicBoolean i = new java.util.concurrent.atomic.AtomicBoolean();\n" +
                              "}\n");
    }

    private void prepareTest(String code, boolean allowErrors) throws Exception {
        clearWorkDir();

        FileObject workFO = FileUtil.toFileObject(getWorkDir());

        assertNotNull(workFO);

        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");

        FileObject data = FileUtil.createData(sourceRoot, "test/Test.java");

        org.netbeans.api.java.source.TestUtilities.copyStringToFile(FileUtil.toFile(data), code);

        data.refresh();

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);

        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);

        assertNotNull(ec);

        doc = ec.openDocument();

        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");

        JavaSource js = JavaSource.forFileObject(data);

        assertNotNull(js);

        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        assertNotNull(info);

        if (!allowErrors) {
            assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
        }
    }

    private CompilationInfo info;
    private Document doc;

    private void performTest(String code, String... assignments) throws Exception {
        performTest(code, false, assignments);
    }

    private void performTest(String code, boolean allowErrors, String... assignments) throws Exception {
        int[] span = new int[1];

        code = TestUtilities.detectOffsets(code, span, "`");

        prepareTest(code, allowErrors);

        FlowResult flow = Flow.assignmentsForUse(info, new AtomicBoolean());
        TreePath sel = info.getTreeUtilities().pathFor(span[0]);

        Set<String> actual = new HashSet<String>();

        for (TreePath tp : flow.getAssignmentsForUse().get(sel.getLeaf())) {
            actual.add(tp.getLeaf().toString());
        }

        assertEquals(new HashSet<String>(Arrays.asList(assignments)), actual);
    }

    private void performDeadBranchTest(String code) throws Exception {
        List<String> splitted = new LinkedList<String>(Arrays.asList(code.split(Pattern.quote("|"))));
        List<Integer> goldenSpans = new ArrayList<Integer>(splitted.size() - 1);
        StringBuilder realCode = new StringBuilder();

        realCode.append(splitted.remove(0));

        for (String s : splitted) {
            goldenSpans.add(realCode.length());
            realCode.append(s);
        }

        prepareTest(realCode.toString(), false);

        FlowResult flow = Flow.assignmentsForUse(info, new AtomicBoolean());

        List<Integer> actual = new ArrayList<Integer>(2 * flow.getDeadBranches().size());

        for (Tree dead : flow.getDeadBranches()) {
            actual.add((int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), dead));
            actual.add((int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), dead));
        }

        assertEquals(goldenSpans, actual);
    }

}
