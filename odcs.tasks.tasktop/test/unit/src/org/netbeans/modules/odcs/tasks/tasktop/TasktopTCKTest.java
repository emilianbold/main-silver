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
package org.netbeans.modules.odcs.tasks.tasktop;

import org.netbeans.modules.odcs.tasks.tasktop.TaskTopExtender;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.odcs.tasks.tck.C2CTestInfrastructure;
import org.netbeans.modules.odcs.tasks.tck.QueryTck;
import org.netbeans.modules.odcs.tasks.spi.C2CExtender;
import org.netbeans.modules.odcs.tasks.tck.C2CDataTck;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/** Runs all the tests from TCK defined in odcs.tasks module.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class TasktopTCKTest extends NbTestCase {
    
    private static String proxyHost = null;
    private static String proxyPort = null;
    
    static {
        initProxy();
    }

    private static void initProxy() {
        final File file = new File(System.getProperty("user.home"), ".test-team");
        if (!file.isFile()) {
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            br.readLine();
            br.readLine();

            proxyHost = br.readLine();
            proxyPort = br.readLine();

            br.close();

            System.setProperty("http.proxyPort", proxyPort);
            System.setProperty("http.proxyHost", proxyHost);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } 
    }

    public TasktopTCKTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return NbModuleSuite.emptyConfiguration()
            .honorAutoloadEager(true)
            .gui(false)
            .addTest(TasktopTCKTest.class) // initialize first
            .addTest(QueryTck.class)
            .addTest(C2CDataTck.class)
            .suite();
    }
    
    public void testVerifyRightTestInfrastructure() {
        C2CTestInfrastructure test = Lookup.getDefault().lookup(C2CTestInfrastructure.class);
        assertNotNull("Test infra found", test);
        assertEquals("Test infra is our", SpringTestInfrastructure.class, test.getClass());
    }

    public void testVerifyRightExtender() {
        C2CExtender ext = Lookup.getDefault().lookup(C2CExtender.class);
        assertNotNull("extender found", ext);
        assertEquals("extender is our", TaskTopExtender.class, ext.getClass());
    }
}
