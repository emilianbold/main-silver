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
package org.netbeans.test.j2ee.wizard;

import junit.framework.Test;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author jungi, Jiri Skrivanek
 */
public class WizardsJavaEE7Test extends J2eeTestCase {

    /** Creates a new instance of WizardsJavaEE7Test */
    public WizardsJavaEE7Test(String testName) {
        super(testName);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        addServerTests(Server.GLASSFISH, conf);  // register server
        if (isRegistered(Server.GLASSFISH)) {
            conf = conf.addTest(Suite.class);
        }
        return conf.suite();
    }

    public static class Suite extends NbTestSuite {

        public Suite() {
            super();
            addTest(new NewFileWizardsTest("testLocalSessionBean", "7"));
            addTest(new NewFileWizardsTest("testRemoteSessionBean", "7"));
            addTest(new NewFileWizardsTest("testLocalRemoteSessionBean", "7"));
            addTest(new NewFileWizardsTest("testLocalStatefulSessionBean", "7"));
            addTest(new NewFileWizardsTest("testRemoteStatefulSessionBean", "7"));
            addTest(new NewFileWizardsTest("testLocalRemoteStatefulSessionBean", "7"));
            addTest(new NewFileWizardsTest("testPersistenceUnitInEjb", "7"));
            addTest(new NewFileWizardsTest("testEntityClassInEjb", "7"));
            addTest(new NewFileWizardsTest("testQueueMdbBean", "7"));
            addTest(new NewFileWizardsTest("testTopicMdbBean", "7"));
            addTest(new NewFileWizardsTest("testServiceLocatorInEjb", "7"));
            addTest(new NewFileWizardsTest("testCachingServiceLocatorInEjb", "7"));
            addTest(new NewFileWizardsTest("testBuildDefaultNewEJBMod", "7"));
            // web project
            addTest(new NewFileWizardsTest("testServiceLocatorInWeb", "7"));
            addTest(new NewFileWizardsTest("testCachingServiceLocatorInWeb", "7"));
            addTest(new NewFileWizardsTest("testPersistenceUnitInWeb", "7"));
            addTest(new NewFileWizardsTest("testEntityClassInWeb", "7"));
            addTest(new NewFileWizardsTest("testBuildDefaultNewWebMod", "7"));
            addTest(new NewProjectWizardsTest("closeProjects", "7"));
        }
    }
}
