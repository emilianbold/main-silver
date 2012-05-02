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
package org.netbeans.modules.javascript2.editor;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Pisl
 */
public class JsCodeCompletionTest extends JsTestBase {

    public JsCodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        List lookupAll = new ArrayList();
        lookupAll.addAll(MockLookup.getDefault().lookupAll(Object.class));
        lookupAll.add(new IFL());
        MockLookup.setInstances(lookupAll.toArray());
        OpenProjects.getDefault().getOpenProjects();    }
    
    
  
    public void testPrefix1() throws Exception {
        checkPrefix("testfiles/completion/cc-prefix1.js");
    }

    public void testPrefix2() throws Exception {
        checkPrefix("testfiles/completion/cc-prefix2.js");
    }

    public void testProperty01() throws Exception {
        checkCompletion("testfiles/model/jQueryFragment01.js", "^jQuery.event.customEvent.test();", false);
    }

    public void testProperty02() throws Exception {
        checkCompletion("testfiles/model/jQueryFragment01.js", "jQuery.^event.customEvent.test();", false);
    }

    public void testProperty03() throws Exception {
        checkCompletion("testfiles/model/jQueryFragment01.js", "jQuery.event.^customEvent.test();", false);
    }

    public void testProperty04() throws Exception {
        checkCompletion("testfiles/model/jQueryFragment01.js", "jQuery.event.customEvent.^test();", false);
    }

    public void testTypeInferenceNew01() throws Exception {
        checkCompletion("testfiles/completion/typeInferenceNew.js", "^formatter.println(\"Car:\");", false);
    }

    public void testTypeInferenceNew02() throws Exception {
        checkCompletion("testfiles/completion/typeInferenceNew.js", "formatter.println(\"color: \" + object.^getColor());", false);
    }

    public void testTypeInferenceNew03() throws Exception {
        checkCompletion("testfiles/completion/typeInferenceNew.js", "formatter.println(\"town: \" + object.^town);", false);
    }

    public void testMethodsOfUndefinedObjects() throws Exception {
        checkCompletion("testfiles/completion/typeInferenceNew.js", "formatter.^println(\"town: \" + object.town);", false);
    }

    public void test129036() throws Exception {
        // needs to be displayed also return types
        checkCompletion("testfiles/completion/test129036.js", "my^ //Foo", false);
    }

    public void testSimpleObject01() throws Exception {
        checkCompletion("testfiles/model/simpleObject.js", "this.called = this.^called + 1;", false);
    }

    public void testSimpleObject02() throws Exception {
        checkCompletion("testfiles/model/simpleObject.js", "this.called = this.cal^led + 1;", false);
    }

    public void testSimpleObject03() throws Exception {
        checkCompletion("testfiles/model/simpleObject.js", "if (this.^color === \"red\") {", false);
    }

    public void testSimpleObject04() throws Exception {
        checkCompletion("testfiles/model/simpleObject.js", "formatter.println(Carrot.isVegi^table());", false);
    }

    public void testGetterSettterInObjectLiteral() throws Exception {
        checkCompletion("testfiles/model/getterSettterInObjectLiteral.js", "formatter.println(\"The dos is old \" + Dog.^years + \" years.\");", false);
    }
}
