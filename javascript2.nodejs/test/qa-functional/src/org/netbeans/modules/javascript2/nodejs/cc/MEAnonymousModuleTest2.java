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
package org.netbeans.modules.javascript2.nodejs.cc;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.javascript2.nodejs.GeneralNodeJs;

/**
 *
 * @author vriha
 */
public class MEAnonymousModuleTest2 extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testAnonymousME31",
        "testAnonymousME35",
        "testAnonymousME36",
        "testAnonymousME311",
        "testAnonymousME312",
        "testAnonymousME313",
        "testAnonymousME41",
        "testAnonymousME45",
        "testAnonymousME46",
        "testAnonymousME411",
        "testAnonymousME412",
        "testAnonymousME413"
    };

    public MEAnonymousModuleTest2(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(MEAnonymousModuleTest2.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        MEAnonymousModuleTest2.currentFile = "cc15.js";
        openFile("modex|cc|cc15.js", "SimpleNode");
        endTest();
    }

    public void testAnonymousME35() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 85);
        endTest();
    }

    public void testAnonymousME36() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 87);
        endTest();
    }

    public void testAnonymousME311() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 97);
        endTest();
    }

    public void testAnonymousME312() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 99);
        endTest();
    }

    public void testAnonymousME313() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 101);
        endTest();
    }

    public void testAnonymousME45() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 85);
        endTest();
    }

    public void testAnonymousME46() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 87);
        endTest();
    }

    public void testAnonymousME411() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 97);
        endTest();
    }

    public void testAnonymousME412() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 99);
        endTest();
    }

    public void testAnonymousME413() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 101);
        endTest();
    }

    public void testAnonymousME31() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 77);
        endTest();
    }

    public void testAnonymousME41() throws Exception {
        startTest();
        openFile("modex|cc|cc16.js", "SimpleNode");
        MEAnonymousModuleTest2.currentFile = "cc16.js";
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 77);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator(MEAnonymousModuleTest2.currentFile);
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }
}
