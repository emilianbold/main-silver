/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.kenai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.netbeans.modules.bugzilla.query.*;
import java.text.MessageFormat;
import java.util.logging.Level;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.LogHandler;
import org.netbeans.modules.bugzilla.TestConstants;
import org.netbeans.modules.bugzilla.TestUtil;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;

/**
 *
 * @author tomas
 */
public class KenaiQueryRefreshTest extends NbTestCase implements TestConstants, QueryConstants {

    public KenaiQueryRefreshTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }   
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());

        System.setProperty("kenai.com.url","https://testkenai.com");
        BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
        String username = br.readLine();
        String password = br.readLine();
        br.close();
        Kenai.getDefault().login(username, password.toCharArray());

        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.setProperty("netbeans.t9y.bugzilla.force.refresh.delay", "please!");
    }

    @Override
    protected void tearDown() throws Exception {        
    }

    public void testKenaiQueryNoAutoRefresh() throws Throwable {
        final String summary = "summary" + System.currentTimeMillis();
        BugzillaConfig.getInstance().setQueryRefreshInterval(0); // would mean refresh imediately
        BugzillaConfig.getInstance().setQueryAutoRefresh(QUERY_NAME, false);

        LogHandler schedulingHandler = new LogHandler("scheduling query", LogHandler.Compare.STARTS_WITH, 120);

        // create query
        BugzillaRepository repo = getRepository();

        String p =  MessageFormat.format(PARAMETERS_FORMAT, summary);
        final KenaiQuery jq = new KenaiQuery(QUERY_NAME, repo, p, TEST_PROJECT, true, false);

        // query was created yet it wasn't refreshed
        assertFalse(schedulingHandler.isDone());

    }

    public void testKenaiQueryAutoRefresh() throws Throwable {
        final String summary = "summary" + System.currentTimeMillis();
        BugzillaConfig.getInstance().setQueryRefreshInterval(1); // 1 minute
        BugzillaConfig.getInstance().setQueryAutoRefresh(QUERY_NAME, true);

        LogHandler refreshHandler = new LogHandler("refresh finish -", LogHandler.Compare.STARTS_WITH, 12000);
        LogHandler schedulingHandler = new LogHandler("scheduling query", LogHandler.Compare.STARTS_WITH, 12000);

        // create issue
        KenaiRepository repo = getKenaiRepository();
        String id = TestUtil.createIssue(repo, summary);
        assertNotNull(id);

        // create query
        LogHandler h = new LogHandler("Finnished populate", LogHandler.Compare.STARTS_WITH);
        String p =  MessageFormat.format(PARAMETERS_FORMAT, summary);
        final BugzillaQuery q = new KenaiQuery(QUERY_NAME, repo, p, TEST_PROJECT, true, false);
        h.waitUntilDone();
        QueryTestUtil.selectTestProject(q);
        refreshHandler.reset();

        // kenai queries are auto refreshed no matter if they are open or not, so
        // we don't have to do anything with the query - just wait until it gets refreshed.

        schedulingHandler.waitUntilDone();
        refreshHandler.waitUntilDone();

        assertTrue(schedulingHandler.isDone());
        assertTrue(refreshHandler.isDone());

        Issue[] issues = q.getIssues(IssueCache.ISSUE_STATUS_ALL);
        assertEquals(1, issues.length);
    }

    private BugzillaRepository getRepository() {
        return TestUtil.getRepository(REPO_NAME, REPO_URL, REPO_USER, REPO_PASSWD);
    }

    private KenaiRepository getKenaiRepository() throws KenaiException {
        // using kenai project 'koliba' - even if the actually used repository is different, it should have no effect on the result
        return new KenaiRepository(Kenai.getDefault().getProject("koliba"), REPO_NAME, REPO_URL, REPO_HOST, REPO_USER, REPO_PASSWD, "product=" + TEST_PROJECT, TEST_PROJECT);
    }


    
}
