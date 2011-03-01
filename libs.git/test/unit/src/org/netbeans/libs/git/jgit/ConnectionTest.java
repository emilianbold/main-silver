/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.libs.git.jgit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitClientCallback;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class ConnectionTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public ConnectionTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }
    
    public void testGitConnection () throws Exception {
        GitClient client = getClient(workDir);
        client.listRemoteBranches("git://bugtracking-test.cz.oracle.com/repo/", ProgressMonitor.NULL_PROGRESS_MONITOR);
    }
    
    // start damon as git daemon --base-path=/srv/git --verbose --export-all /srv/git &
    public void testHttpConnectionCredentialsInUri () throws Exception {
        // UN and PWD in uri
        GitClient client = getClient(workDir);
        client.listRemoteBranches("http://user:heslo@bugtracking-test.cz.oracle.com/git/repo/", ProgressMonitor.NULL_PROGRESS_MONITOR);
    }
    
    // start damon as git daemon --base-path=/srv/git --verbose --export-all /srv/git &
    public void testHttpConnection () throws Exception {
        // UN and PWD provided by a callback
        GitClient client = getClient(workDir);
        try {
            client.listRemoteBranches("http://bugtracking-test.cz.oracle.com/git/repo/", ProgressMonitor.NULL_PROGRESS_MONITOR);
            fail();
        } catch (GitException.AuthorizationException ex) {
            assertEquals("http://bugtracking-test.cz.oracle.com/git/repo/", ex.getRepositoryUrl());
        }
        GitClientCallback callback = new GitClientCallback() {
            @Override
            public String askQuestion (String uri, String prompt) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getUsername (String uri, String prompt) {
                return "user";
            }

            @Override
            public char[] getPassword (String uri, String prompt) {
                return "heslo".toCharArray();
            }

            @Override
            public char[] getPassphrase (String uri, String prompt) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            @Override
            public Boolean askYesNoQuestion (String uri, String prompt) {
                throw new UnsupportedOperationException();
            }
        };
        client.setCallback(callback);
        client.listRemoteBranches("http://bugtracking-test.cz.oracle.com/git/repo/", ProgressMonitor.NULL_PROGRESS_MONITOR);
    }
    
    // start damon as git daemon --base-path=/srv/git --verbose --export-all /srv/git &
    public void testHttpConnectionPublic () throws Exception {
        GitClient client = getClient(workDir);
        // no username or password
        client.setCallback(null);
        try {
            client.listRemoteBranches("http://bugtracking-test.cz.oracle.com/git/repo/", ProgressMonitor.NULL_PROGRESS_MONITOR);
            fail();
        } catch (GitException.AuthorizationException ex) {
            assertEquals("http://bugtracking-test.cz.oracle.com/git/repo/", ex.getRepositoryUrl());
        }
        client.listRemoteBranches("http://bugtracking-test.cz.oracle.com/git-public/repo/", ProgressMonitor.NULL_PROGRESS_MONITOR);
        // callback should not be called at all
        client.setCallback(new DefaultCallback());
        client.listRemoteBranches("http://bugtracking-test.cz.oracle.com/git-public/repo/", ProgressMonitor.NULL_PROGRESS_MONITOR);
    }
    
    // start damon as git daemon --base-path=/srv/git --verbose --export-all /srv/git &
    public void testHttpConnectionEmptyPassword () throws Exception {
        GitClient client = getClient(workDir);
        // UN and EMPTY password provided by a callback
        GitClientCallback callback = new GitClientCallback() {
            @Override
            public String askQuestion (String uri, String prompt) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getUsername (String uri, String prompt) {
                return "user2";
            }

            @Override
            public char[] getPassword (String uri, String prompt) {
                return "".toCharArray();
            }

            @Override
            public char[] getPassphrase (String uri, String prompt) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Boolean askYesNoQuestion (String uri, String prompt) {
                throw new UnsupportedOperationException();
            }
        };
        client.setCallback(callback);
        client.listRemoteBranches("http://bugtracking-test.cz.oracle.com/git/repo/", ProgressMonitor.NULL_PROGRESS_MONITOR);
    }

    // implement callback when jgit fully supports https and certs
    public void testHttpsConnection () throws Exception {
        GitClient client = getClient(workDir);
        try {
            client.listRemoteBranches("https://vcs-test.cz.oracle.com:440/svnsecure/", ProgressMonitor.NULL_PROGRESS_MONITOR);
            fail();
        } catch (GitException ex) {
            assertTrue(ex.getCause().getCause().getMessage().contains("unable to find valid certification path to requested target"));
        }
    }

    public void testSshConnectionCredentialsInUri () throws Exception {
        GitClient client = getClient(workDir);
        client.setCallback(new DefaultCallback());
        client.listRemoteBranches("ssh://tester:vsdsvsds@bugtracking-test.cz.oracle.com/srv/git/repo/", ProgressMonitor.NULL_PROGRESS_MONITOR);
    }

    public void testSshConnectionCredentialsFromCallback () throws Exception {
        GitClient client = getClient(workDir);
        GitClientCallback callback = new DefaultCallback() {
            @Override
            public String getUsername (String uri, String prompt) {
                return "tester";
            }

            @Override
            public char[] getPassword (String uri, String prompt) {
                return "vsdsvsds".toCharArray();
            }
        };
        client.setCallback(callback);
        client.listRemoteBranches("ssh://bugtracking-test.cz.oracle.com/srv/git/repo/", ProgressMonitor.NULL_PROGRESS_MONITOR);
    }

    public void testSshConnectionUserInUriPasswordFromCallback () throws Exception {
        GitClient client = getClient(workDir);
        GitClientCallback callback = new DefaultCallback() {
            @Override
            public char[] getPassword (String uri, String prompt) {
                return "vsdsvsds".toCharArray();
            }
        };
        client.setCallback(callback);
        client.listRemoteBranches("ssh://tester@bugtracking-test.cz.oracle.com/srv/git/repo/", ProgressMonitor.NULL_PROGRESS_MONITOR);
    }

    public void testSshFetchCredentialsFromCallback () throws Exception {
        GitClient client = getClient(workDir);
        client.setRemote(new GitRemoteConfig() {
            @Override
            public String getRemoteName () {
                return "origin";
            }

            @Override
            public List<String> getUris () {
                return Arrays.asList("ssh://bugtracking-test.cz.oracle.com/srv/git/repo/");
            }

            @Override
            public List<String> getPushUris () {
                return Collections.emptyList();
            }

            @Override
            public List<String> getFetchRefSpecs () {
                return Arrays.asList("+refs/heads/*:refs/remotes/origin/*");
            }

            @Override
            public List<String> getPushRefSpecs () {
                return Collections.emptyList();
            }
        }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        GitClientCallback callback = new DefaultCallback() {
            @Override
            public String getUsername (String uri, String prompt) {
                return "tester";
            }

            @Override
            public char[] getPassword (String uri, String prompt) {
                return "vsdsvsds".toCharArray();
            }
        };
        client.setCallback(callback);
        client.fetch("origin", ProgressMonitor.NULL_PROGRESS_MONITOR);
    }
    
    // someone tell me what's wrong here. correct passphrase is returned yet the private key cannot be unlocked and cert auth fails
//    public void testSshConnectionPassphrase () throws Exception {
//        GitClient client = getClient(workDir);
//        client.setCallback(new DefaultCallback() {
//            @Override
//            public String getUsername (String uri, String prompt) {
//                return "gittester";
//            }
//
//            @Override
//            public char[] getPassphrase (String uri, String prompt) {
//                return "qwerty".toCharArray();
//            }
//        });
//        client.listRemoteBranches("ssh://gittester@bugtracking-test.cz.oracle.com/srv/git/repo/", ProgressMonitor.NULL_PROGRESS_MONITOR);
//    }
    
    private static class DefaultCallback implements GitClientCallback {
        @Override
        public String askQuestion (String uri, String prompt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getUsername (String uri, String prompt) {
            return null;
        }

        @Override
        public char[] getPassword (String uri, String prompt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Boolean askYesNoQuestion (String uri, String prompt) {
            if (prompt.contains("RSA key fingerprint")) {
                return true;
            }
            return null;
        }

        @Override
        public char[] getPassphrase (String uri, String prompt) {
            return "".toCharArray();
        }
        
    }
}
