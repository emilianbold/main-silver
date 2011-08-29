/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira;

import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.JiraRepositoryConnector;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiSupport;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.jira.kenai.KenaiRepository;
import org.netbeans.modules.jira.issue.JiraIssueProvider;
import org.netbeans.modules.jira.kenai.KenaiSupportImpl;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.jira.repository.JiraStorageManager;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class Jira {

    private Set<JiraRepository> repositories;

    private static final Object REPOSITORIES_LOCK = new Object();

    private JiraRepositoryConnector jrc;
    private static Jira instance;
    private JiraStorageManager storageManager;

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.jira.Jira");
    private RequestProcessor rp;

    private KenaiSupport kenaiSupport;
    private JiraConnector connector;
    
    private Jira() {
        ModuleLifecycleManager.instantiated = true;
    }

    public KenaiSupport getKenaiSupport() {
        if(kenaiSupport == null) {
            kenaiSupport = new KenaiSupportImpl();
        }
        return kenaiSupport;
    }

    public static synchronized Jira getInstance() {
        if(instance == null) {
            instance = new Jira();
            // lazy ping tasklist issue provider to load issues ...
            instance.getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    JiraIssueProvider.getInstance();
                }
            });
        }
        return instance;
    }

    static void init() {
        getInstance();
    }

    /**
     * Returns the request processor for common tasks in Jira.
     * Do not use this when accesing a remote repository.
     *
     * @return
     */
    public RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("Jira", 1, true); // NOI18N
        }
        return rp;
    }

    public void addRepository(JiraRepository repository) {
        Collection<Repository> oldRepos;
        Collection<Repository> newRepos;
        synchronized(REPOSITORIES_LOCK) {
            Set<JiraRepository> repos = getStoredRepositories();
            oldRepos = Collections.unmodifiableCollection(new LinkedList<Repository>(repos));
            if(!(repository instanceof KenaiRepository)) {
                // we don't store kenai repositories - XXX  shouldn't be even called
                repos.add(repository);
                JiraConfig.getInstance().putRepository(repository.getID(), repository);
            }
            newRepos = Collections.unmodifiableCollection(new LinkedList<Repository>(repos));
        }
        getConnector().fireRepositoriesChanged(oldRepos, newRepos);
    }

    public void removeRepository(JiraRepository repository) {
        Collection<Repository> oldRepos;
        Collection<Repository> newRepos;
        synchronized(REPOSITORIES_LOCK) {
            Set<JiraRepository> repos = getStoredRepositories();
            oldRepos = Collections.unmodifiableCollection(new LinkedList<Repository>(repos));
            repos.remove(repository);
            newRepos = Collections.unmodifiableCollection(new LinkedList<Repository>(repos));
            JiraConfig.getInstance().removeRepository(repository.getID());
        }
        getConnector().fireRepositoriesChanged(oldRepos, newRepos);
        JiraIssueProvider.getInstance().removeAllFor(repository);
    }

    public JiraConnector getConnector() {
        if (connector == null) {
            connector = Lookup.getDefault().lookup(JiraConnector.class);
        }
        return connector;
    }

    public JiraRepository[] getRepositories() {
        synchronized(REPOSITORIES_LOCK) {
            Set<JiraRepository> s = getStoredRepositories();
            return s.toArray(new JiraRepository[s.size()]);
        }
    }

    private Set<JiraRepository> getStoredRepositories() {
        if (repositories == null) {
            repositories = new HashSet<JiraRepository>();
            String[] names = JiraConfig.getInstance().getRepositories();
            if (names == null || names.length == 0) {
                return repositories;
            }
            for (String name : names) {
                JiraRepository repo = JiraConfig.getInstance().getRepository(name);
                if (repo != null) {
                    repositories.add(repo);
                }
            }
        }
        return repositories;
    }

    public JiraRepository getRepository(String name) {
        for(JiraRepository repo : getRepositories()) {
            if(repo.getDisplayName().equals(name)) {
                return repo;
            }
        }
        return null;
    }

    public JiraRepositoryConnector getRepositoryConnector() {
        if(jrc == null) {
            jrc = new JiraRepositoryConnector();
        }
        return jrc;
    }

    public JiraClient getClient(TaskRepository repo) throws JiraException {
        // XXX init repo connenction?
        return JiraClientFactory.getDefault().getJiraClient(repo);
    }

    public void removeClient(TaskRepository taskRepository) {
        JiraClientFactory.getDefault().repositoryRemoved(taskRepository);
    }

    public JiraStorageManager getStorageManager () {
        if (storageManager == null) {
            storageManager = JiraStorageManager.getInstance();
        }
        return storageManager;
    }
    
    void shutdown () {
        getStorageManager().shutdown();
    }    
}
