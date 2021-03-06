/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.test.ide;

import java.util.logging.Level;
import junit.framework.Test;
import junit.framework.TestResult;
import org.netbeans.junit.NbModuleSuite;

public class MemoryValidationTest extends IDEValidation {
    /** Need to be defined because of JUnit */
    public MemoryValidationTest(String name) {
        super(name);
    }

//    @Override
//    public void run(TestResult result) {
//        if (!getName().startsWith("testGC")) {
//            result = new TestResult();
//        }
//        super.run(result);
//    }

    protected @Override int timeOut() {
        return 300000;
    }
    
    public static Test suite() {
        // XXX: supresses warning about jpda debugger using parsing API from AWT thread
        System.setProperty("org.netbeans.modules.parsing.impl.TaskProcessor.level", "OFF");

        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
            MemoryValidationTest.class
        ).clusters(".*").enableModules(".*").
        honorAutoloadEager(true).
        failOnException(Level.OFF)
        .failOnMessage(Level.OFF);

        conf = conf.addTest("testInitGC");
        conf = conf.addTest("testMainMenu");
//        conf = conf.addTest("testHelp");
        conf = conf.addTest("testOptions");
        conf = conf.addTest("testNewProject");
        // sample project must exist before testShortcuts
        conf = conf.addTest("testShortcuts");
        conf = conf.addTest("testNewFile");
//        conf = conf.addTest("testProjectsView");
//        conf = conf.addTest("testFilesView");

        conf = conf.addTest("testEditor");
        conf = conf.addTest("testBuildAndRun");

//        conf = conf.addTest("testDebugging");
//        conf = conf.addTest("testJUnit");
//        conf = conf.addTest("testXML");
//        conf = conf.addTest("testDb");
//        conf = conf.addTest("testWindowSystem");

        conf = conf.addTest("testGCDocuments");
        conf = conf.addTest("testGCProjects");
        // not in commit suite because it needs net connectivity
        // suite.addTest(new IDEValidation("testPlugins"));
        return NbModuleSuite.create(conf);
    }
}
