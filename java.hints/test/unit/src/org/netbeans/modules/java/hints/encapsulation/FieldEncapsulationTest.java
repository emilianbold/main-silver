/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.encapsulation;


import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;

/**
 *
 * @author tom
 */
public class FieldEncapsulationTest extends TestBase {

    public FieldEncapsulationTest(final String name) {
        super (name, FieldEncapsulation.class);
    }

    public void testPublic() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public int a = 10;\n" +
                            "}",
                            "2:15-2:16:verifier:Public Field");
    }

    public void testProtected() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    protected int a = 10;\n" +
                            "}",
                            "2:18-2:19:verifier:Protected Field");
    }

    public void testPackage() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    int a = 10;\n" +
                            "}",
                            "2:8-2:9:verifier:Package Field");
    }

    public void testPrivate() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    private int a = 10;\n" +
                            "}");
    }

    public void testPublicStatic() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public static int a = 10;\n" +
                            "}",
                            "2:22-2:23:verifier:Public Field");
    }

    public void testProtectedStatic() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    protected static int a = 10;\n" +
                            "}",
                            "2:25-2:26:verifier:Protected Field");
    }

    public void testPackageStatic() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    static int a = 10;\n" +
                            "}",
                            "2:15-2:16:verifier:Package Field");
    }

    public void testPrivateStatic() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    private static int a = 10;\n" +
                            "}");
    }

    public void testPublicFinal() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public final int a = 10;\n" +
                            "}");
    }

    public void testProtectedFinal() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    protected final int a = 10;\n" +
                            "}");
    }

    public void testPackageFinal() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    final int a = 10;\n" +
                            "}");
    }

    public void testPrivateFinal() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    private final int a = 10;\n" +
                            "}");
    }

    public void testPublicStaticFinal() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public static final int a = 10;\n" +
                            "}");
    }

    public void testProtectedStaticFinal() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    protected static final int a = 10;\n" +
                            "}");
    }

    public void testPackageStaticFinal() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    static final int a = 10;\n" +
                            "}");
    }

    public void testPrivateStaticFinal() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    private static final int a = 10;\n" +
                            "}");
    }

    public void testLocalField() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public void foo(int a) {\n"+
                            "        int b;\n"+
                            "    }\n"+
                            "}");
    }

    public void testInterface() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public interface Test {\n" +
                            "    int a = 10;\n" +
                            "}");
    }

    public void testEnum() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public enum Test {\n" +
                            "    A;\n" +
                            "}");
    }

}
