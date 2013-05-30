/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.atoum.run;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.spi.testing.run.TestCase;

public class TapParserTest extends NbTestCase {

    public TapParserTest(String name) {
        super(name);
    }

    public void testParse() throws Exception {
        List<TestSuiteVo> suites = new TapParser()
                .parse(getFileContent("atoum-tap.log"), 1000L);
        assertEquals(3, suites.size());

        TestSuiteVo suite1 = suites.get(0);
        assertEquals("tests\\unit\\StdClass", suite1.getName());
        assertEquals("/home/gapon/NetBeansProjects/atoum-sample/tests/unit/StdClass.php", suite1.getFile());

        List<TestCaseVo> testCases1 = suite1.getTestCases();
        assertEquals(6, testCases1.size());

        TestCaseVo testCase1 = testCases1.get(0);
        assertEquals("testVoid", testCase1.getName());
        assertEquals(TestCase.Status.PENDING, testCase1.getStatus());
        // XXX should be null
        assertEquals("/home/gapon/NetBeansProjects/atoum-sample/tests/unit/StdClass.php", testCase1.getMessage());
        // XXX should be the line above
        assertNull(testCase1.getFile());
        // XXX should be some number != -1
        assertEquals(-1, testCase1.getLine());
        assertEquals(100L, testCase1.getTime());

        TestCaseVo testCase2 = testCases1.get(1);
        assertEquals("testFail", testCase2.getName());
        assertEquals(TestCase.Status.FAILED, testCase2.getStatus());
        assertEquals("object(stdClass) is not null; second line of the message", testCase2.getMessage());
        assertEquals("/home/gapon/NetBeansProjects/atoum-sample/tests/unit/StdClass.php", testCase2.getFile());
        assertEquals(19, testCase2.getLine());
        assertEquals(100L, testCase2.getTime());

        TestCaseVo testCase3 = testCases1.get(2);
        assertEquals("testPass", testCase3.getName());
        assertEquals(TestCase.Status.PASSED, testCase3.getStatus());
        assertNull(testCase3.getMessage());
        assertNull(testCase3.getFile());
        assertEquals(-1, testCase3.getLine());
        assertEquals(100L, testCase3.getTime());

        TestCaseVo testCase4 = testCases1.get(3);
        assertEquals("testSkipped", testCase4.getName());
        assertEquals(TestCase.Status.SKIPPED, testCase4.getStatus());
        assertEquals("This test was skipped", testCase4.getMessage());
        assertEquals(Arrays.asList("/home/gapon/NetBeansProjects/atoum-sample/tests/unit/StdClass.php:29"), testCase4.getStackTrace());
        assertEquals("/home/gapon/NetBeansProjects/atoum-sample/tests/unit/StdClass.php", testCase4.getFile());
        assertEquals(29, testCase4.getLine());
        assertEquals(100L, testCase4.getTime());

        TestCaseVo testCase5 = testCases1.get(4);
        assertEquals("testException", testCase5.getName());
        assertEquals(TestCase.Status.ERROR, testCase5.getStatus());
        assertEquals("exception 'RuntimeException' with message 'This test triggered a \\RuntimeException' in /home/gapon/NetBeansProjects/atoum-sample/tests/unit/StdClass.php:41", testCase5.getMessage());
        assertEquals(Arrays.asList(
                "/home/gapon/NetBeansProjects/atoum-sample/vendor/atoum/atoum/classes/test.php(838): tests\\unit\\StdClass->testException()",
                "-(1): mageekguy\\atoum\\test->runTestMethod('testException')",
                "{main}",
                "/home/gapon/NetBeansProjects/atoum-sample/vendor/atoum/atoum/classes/test.php:838"), testCase5.getStackTrace());
        assertEquals("/home/gapon/NetBeansProjects/atoum-sample/vendor/atoum/atoum/classes/test.php", testCase5.getFile());
        assertEquals(838, testCase5.getLine());
        assertEquals(100L, testCase5.getTime());

        TestCaseVo testCase6 = testCases1.get(5);
        assertEquals("testError", testCase6.getName());
        // XXX should be ERROR
        assertEquals(TestCase.Status.FAILED, testCase6.getStatus());
        assertEquals("This test triggered an error", testCase6.getMessage());
        assertEquals("/home/gapon/NetBeansProjects/atoum-sample/tests/unit/StdClass.php", testCase6.getFile());
        assertEquals(36, testCase6.getLine());
        assertEquals(100L, testCase6.getTime());

        TestSuiteVo suite2 = suites.get(1);
        assertEquals("tests\\unit\\atoum\\sample\\Foobar", suite2.getName());
        assertNull(suite2.getFile());

        List<TestCaseVo> testCases2 = suite2.getTestCases();
        assertEquals(3, testCases2.size());

        TestCaseVo testCase7 = testCases2.get(0);
        assertEquals("test__construct", testCase7.getName());
        assertEquals(TestCase.Status.PASSED, testCase7.getStatus());
        assertNull(testCase7.getFile());
        assertEquals(-1, testCase7.getLine());
        assertEquals(100L, testCase7.getTime());

        TestCaseVo testCase8 = testCases2.get(1);
        assertEquals("testGetFoo", testCase8.getName());
        assertEquals(TestCase.Status.PASSED, testCase8.getStatus());
        assertNull(testCase8.getFile());
        assertEquals(-1, testCase8.getLine());
        assertEquals(100L, testCase8.getTime());

        TestCaseVo testCase9 = testCases2.get(2);
        assertEquals("testIncomplete", testCase9.getName());
        assertEquals(TestCase.Status.ABORTED, testCase9.getStatus());
        assertEquals("I died", testCase9.getMessage());
        assertNull(testCase9.getFile());
        assertEquals(-1, testCase9.getLine());
        assertEquals(100L, testCase9.getTime());

        TestSuiteVo suite3 = suites.get(2);
        assertEquals("my\\project\\tests\\units\\helloWorld", suite3.getName());
        assertEquals("/home/gapon/NetBeansProjects/atoum/test/helloWorld.php", suite3.getFile());

        List<TestCaseVo> testCases3 = suite3.getTestCases();
        assertEquals(1, testCases3.size());

        TestCaseVo testCase10 = testCases3.get(0);
        assertEquals("testBye", testCase10.getName());
        assertEquals(TestCase.Status.FAILED, testCase10.getStatus());
        assertEquals("strings are not equals", testCase10.getMessage());
        assertEquals("string(10) \"Bye World!\"", testCase10.getDiff().getExpected());
        assertEquals("string(6) \"Hello!\"", testCase10.getDiff().getActual());
        assertEquals("/home/gapon/NetBeansProjects/atoum/test/helloWorld.php", testCase10.getFile());
        assertEquals(31, testCase10.getLine());
        assertEquals(100L, testCase10.getTime());
    }

    private String getFileContent(String filePath) throws IOException {
        File file = new File(getDataDir(), filePath);
        assertTrue(file.getAbsolutePath(), file.isFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

}
