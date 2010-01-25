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

package org.netbeans.modules.java.hints;

import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;

/**
 *
 * @author David Strupl
 */
public class OverridableMethodCallInConstructorTest extends TestBase {

    public OverridableMethodCallInConstructorTest(String name) {
        super(name, OverridableMethodCallInConstructor.class);
    }

    public void testDoNotReportPrivateCall() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public Test() { foo(); }\n" +
                            "    private void foo() { } \n" +
                            "}"
                            );
    }
    public void testReportPackagePrivateCall() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public Test() { foo(); }\n" +
                            "    void foo() { } \n" +
                            "}",
                            "2:20-2:23:verifier:Overridable method call in constructor"
                            );
    }
    public void testReportPublicCall() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public Test() { foo(); }\n" +
                            "    public void foo() { } \n" +
                            "}",
                            "2:20-2:23:verifier:Overridable method call in constructor"
                            );
    }
    public void testDoNotReportFinalCall() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public Test() { foo(); }\n" +
                            "    public final void foo() { } \n" +
                            "}"                            );
    }
    public void testDoNotReportStaticCall() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public Test() { foo(); }\n" +
                            "    static void foo() { } \n" +
                            "}"
                            );
    }
    public void testDoNotReportOnFinalClass() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public final class Test {\n" +
                            "    public Test() { foo(); }\n" +
                            "    public void foo() { } \n" +
                            "}"
                            );
    }
    public void testDoNotReportForeignClassMethodInvocations() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public Test() { new Object().toString() }\n" +
                            "}"
                            );
    }
    @RandomlyFails // not randomly ;-)
    public void testDoNotReportForeignObjectMethodInvocations() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public Test() { new Test().foo() }\n" +
                            "    public void foo() { } \n" +
                            "}"
                            );
    }
    
}