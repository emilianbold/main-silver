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
package org.netbeans.modules.javascript2.nodejs.editor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript2.editor.JsCodeCompletionBase;
import static org.netbeans.modules.javascript2.editor.JsTestBase.JS_SOURCE_ID;
import org.netbeans.modules.javascript2.nodejs.TestProjectSupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Pisl
 */
public class NodeJSCodeCompletionTest extends JsCodeCompletionBase {

    public NodeJSCodeCompletionTest(String testName) throws IOException {
        super(testName);
    }

    static private boolean isSetup = false;

    @Override
    protected void setUp() throws Exception {
//        super.setUp(); //To change body of generated methods, choose Tools | Templates.
        if (!isSetup) {
            // only for the first run index all sources
            super.setUp();
            isSetup = true;
        }
        FileObject folder = getTestFile("TestNavigation");
            Project testProject = new TestProjectSupport.TestProject(folder, null);
            List lookupAll = new ArrayList();
            lookupAll.addAll(MockLookup.getDefault().lookupAll(Object.class));
            lookupAll.add(new TestProjectSupport.FileOwnerQueryImpl(testProject));
            MockLookup.setInstances(lookupAll.toArray());

    }

    public void testBasicExport01() throws Exception {
        checkCompletion("TestNavigation/public_html/js/foo2.js", "+ circle.^area(4));", false);
    }

    public void testBasicExport02() throws Exception {
        checkCompletion("TestNavigation/public_html/js/testAddress.js", "as.^print();", false);
    }
    
    public void testExport01() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/testcc01.js", "var mess = require('./simpleModule').m^essage;", false);
    }
    
    public void testExport02() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/testcc01.js", "simple.m^essage.setCode(23);", false);
    }
    
    public void testExport03() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/testcc01.js", "simple.message.se^tCode(23);", false);
    }
    
    public void testExport04() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/testcc01.js", "mess.set^Code(25);", false);
    }
    
    public void testExport05() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/testcc01.js", "mOut.o^utput = 'bug';", false);
    }
    
    public void testIssue249436_01() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/issue249436.js", "t^ //cc here", false);
    }
    
    public void testIssue249632() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/issue249632.js", "rnewe.rgetAttempt().a^a;", false);
    }
    
    public void testIssue249628() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/issue249629.js", "instRef.rprops.^b;", false);
    }
    
    public void testIssue249500_01() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/issue249500.js", "a^;", false);
    }
    
    public void testIssue249500_02() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/issue249500.js", "p^;", false);
    }
    
    public void testIssue249626_01() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/issue249626.js", "require(\"./complexModule\").^literalRef.propX.iprop;", false);
    }
    
    public void testIssue249626_02() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/issue249626.js", "require(\"./complexModule\").literalRef.^propX.iprop;", false);
    }
    
    public void testIssue249626_03() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/issue249626.js", "require(\"./complexModule\").literalRef.propX.^iprop;", false);
    }
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new LinkedList<FileObject>();

        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/TestNavigation/public_html/")));
        return Collections.singletonMap(
                JS_SOURCE_ID,
                ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[cpRoots.size()]))
        );
    }

    @Override
    protected boolean classPathContainsBinaries() {
        return true;
    }

    @Override
    protected boolean cleanCacheDir() {
        return false;
    }
}
