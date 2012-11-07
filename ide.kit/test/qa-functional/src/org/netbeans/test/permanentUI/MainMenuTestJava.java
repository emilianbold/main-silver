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
package org.netbeans.test.permanentUI;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.permanentUI.utils.ProjectContext;

public class MainMenuTestJava extends MainMenuTest {

    private static boolean init = true;


    public MainMenuTestJava(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(MainMenuTestJava.class).clusters(".*").enableModules(".*");
        conf.addTest(ALL_TESTS);
        return conf.suite();
    }

    @Override
    protected void initSources() {
        try {
            openDataProjects("SampleProject"); // NOI18N
            EditorOperator.closeDiscardAll();
            ProjectsTabOperator pto = new ProjectsTabOperator();
            // find node in given tree
            Node sample1 = new Node(pto.tree(), "SampleProject|Source Packages|sample1"); // NOI18N
            Node node = new Node(sample1, "SampleClass1.java"); // NOI18N
            new OpenAction().performAPI(node);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isInit() {
        return init;
    }

    @Override
    public void setInit(boolean init) {
        MainMenuTestJava.init = init;
    }

    @Override
    public void testFileMenu() {
        oneMenuTest("File", getProjectContext());
    }

    @Override
    public void testRefactorMenu() {
        oneMenuTest("Refactor", getProjectContext());
    }

    @Override
    public void testDebugMenu() {
        oneMenuTest("Debug", getProjectContext());
    }

    @Override
    public void testRunMenu() {
        oneMenuTest("Run", getProjectContext());
    }

    @Override
    public void testToolsMenu() {
        oneMenuTest("Tools", getProjectContext());
    }

    @Override
    public void testProfileMenu() {
        oneMenuTest("Profile", getProjectContext());
    }

    @Override
    public void testSource_PreprocessorBlocksSubMenu() {
        oneSubMenuTest("Source|Preprocessor Blocks", false, getProjectContext());
    }

    @Override
    public void testView_CodeFoldsSubMenu() {
        //TODO fix empty submenu items names due to init
        //oneSubMenuTest("View|Code Folds", true, getProjectContext());
    }

    @Override
    ProjectContext getProjectContext() {
        return ProjectContext.JAVA;
    }
}
