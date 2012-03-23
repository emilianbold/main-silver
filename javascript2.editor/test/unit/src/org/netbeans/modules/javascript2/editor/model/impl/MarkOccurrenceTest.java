/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.model.impl;

import java.io.IOException;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class MarkOccurrenceTest extends JsTestBase {

    public MarkOccurrenceTest(String testName) {
        super(testName);
    }
    
    public void testSimpleObject01() throws Exception {
        checkOccurrences("testfiles/model/simpleObject.js", "var Car^rot = {", true);
    }
    
    public void testSimpleObject02() throws Exception {
        checkOccurrences("testfiles/model/simpleObject.js", "col^or: \"red\",", true);
    }
    
    public void testSimpleObject03() throws Exception {
        checkOccurrences("testfiles/model/simpleObject.js", "this.call^ed = this.called + 1;", true);
    }
    
    public void testSimpleObject04() throws Exception {
        checkOccurrences("testfiles/model/simpleObject.js", "getCo^lor: function () {", true);
    }
    
    public void testAssignments01() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "var he^ad = \"head\";", true);
    }
    
    public void testAssignments02() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "head = bo^dy;", true);
    }
    
    public void testAssignments03() throws Exception {
        checkOccurrences("testfiles/model/returnTypes02.js", "zi^p = 15000;", true);
    }
    
    public void testFunctionParameters01() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "function Joke (name, autor, descri^ption) {", true);
    }
    
    public void testFunctionParameters02() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "this.name = na^me;", true);
    }
    
    public void testFunctionParameters03() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "formatter.println(\"Author: \" + au^tor);", true);
    }
    
    public void testFunctionParameters04() throws Exception {
        checkOccurrences("testfiles/model/returnTypes02.js", "zip = zi^pp;", true);
    }
    
    public void testMethod01() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "formatter.println(\"Name: \" + this.getNa^me());", true);
    }

    public void testUndefinedMethods01() throws Exception {
        checkOccurrences("testfiles/completion/undefinedMethods.js", "dvorek.getPrasatko().udelejChro(dvo^rek.dejDefault(), \"afdafa\");", true);
    }

    public void testUndefinedMethods02() throws Exception {
        checkOccurrences("testfiles/completion/undefinedMethods.js", "dvorek.getPra^satko().udelejChro(dvorek.dejDefault(), \"afdafa\");", true);
    }

    public void testUndefinedMethods03() throws Exception {
        checkOccurrences("testfiles/completion/undefinedMethods.js", "dvorek.getPrasatko().udelejC^hro(dvorek.dejDefault(), \"afdafa\");", true);
    }

    public void testUndefinedMethods04() throws Exception {
        checkOccurrences("testfiles/completion/undefinedMethods.js", "dvorek.getKo^cicku().udelejMau();", true);
    }

    public void testFunctionParameters05() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "jQuery(function($^){", true);
    }
    
    public void testProperty01() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    $.timepic^ker.regional[\"cs\"] = {", true);
    }

    public void testProperty02() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    $.timepicker.region^al[\"cs\"] = {", true);
    }
    
    public void testProperty03() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    $.timepicker.regional[\"c^s\"] = {", true);
    }

    public void testProperty04() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    te^st.anotherProperty = test.myProperty;", true);
    }
    
    public void testProperty05() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    test.anotherProperty = test.myPrope^rty;", true);
    }
    
    public void testGetterSetterInObjectLiteral01() throws Exception {
        checkOccurrences("testfiles/model/getterSettterInObjectLiteral.js", "set yea^rs(count){this.old = count + 1;},", true);
    }

    public void testGetterSetterInObjectLiteral02() throws Exception {
        checkOccurrences("testfiles/model/getterSettterInObjectLiteral.js", "Dog.yea^rs = 10;", true);
    }
    
    public void testFunctionInGlobalSpace01() throws Exception {
        checkOccurrences("testfiles/model/functionInGlobal.js", "this.printSometh^ing();", true);
    }

    public void testFunctionInGlobalSpace02() throws Exception {
        checkOccurrences("testfiles/model/functionInGlobal.js", "this.anotherFunct^ion();", true);
    }
     
    public void testIssue209717_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue209717_01.js", "foobar = (typeof foo == \"undefined\") ? bar : f^oo;", true);
    }

    public void testIssue209717_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue209717_01.js", "foobar = (typeof foo == \"undefined\") ? b^ar : foo;", true);
    }
    
    public void testIssue209717_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue209717_02.js", "foobar = (typeof foo^22 == \"undefined\") ? bar : foo;", true);
    }

    @Override
    protected void assertDescriptionMatches(FileObject fileObject,
            String description, boolean includeTestName, String ext, boolean goldenFileInTestFileDir) throws IOException {
        super.assertDescriptionMatches(fileObject, description, includeTestName, ext, true);
    }
}
