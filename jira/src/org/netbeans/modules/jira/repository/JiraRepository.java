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

package org.netbeans.modules.jira.repository;

import org.netbeans.modules.bugtracking.kenai.spi.RepositoryUser;
import com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.User;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ContentFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ProjectFilter;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import java.util.Map;
import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.JiraConnector;
import org.netbeans.modules.jira.commands.JiraCommand;
import org.netbeans.modules.jira.commands.JiraExecutor;
import org.netbeans.modules.jira.commands.NamedFiltersCommand;
import org.netbeans.modules.jira.commands.PerformQueryCommand;
import org.netbeans.modules.jira.issue.JiraTaskListProvider;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.query.JiraQuery;
import org.netbeans.modules.jira.query.QueryController;
import org.netbeans.modules.jira.util.JiraUtils;
import org.netbeans.modules.jira.util.MylynUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class JiraRepository {

    private static final String ICON_PATH = "org/netbeans/modules/bugtracking/ui/resources/repository.png"; // NOI18N

    private TaskRepository taskRepository;
    private JiraRepositoryController controller;
    private Set<JiraQuery> queries = null;
    private Set<JiraQuery> remoteFilters = null;
    private IssueCache<NbJiraIssue, TaskData> cache;
    private Image icon;

    private final Set<String> issuesToRefresh = new HashSet<String>(5);
    private final Set<JiraQuery> queriesToRefresh = new HashSet<JiraQuery>(3);
    private Task refreshIssuesTask;
    private Task refreshQueryTask;
    private RequestProcessor refreshProcessor;
    private JiraExecutor executor;
    private JiraConfiguration configuration;

    private final Object REPOSITORY_LOCK = new Object();
    private final Object CONFIGURATION_LOCK = new Object();
    private final Object QUERIES_LOCK = new Object();

    private Lookup lookup;
    private RepositoryInfo info;

    public JiraRepository() {
        icon = ImageUtilities.loadImage(ICON_PATH, true);
    }

    public JiraRepository(RepositoryInfo info) {
        this.info = info;
        String name = info.getDisplayName();
        String user = info.getUsername();
        if(user == null) {
            user = ""; // NOI18N
        }
        char[] password = info.getPassword();
        if(password == null) {
            password = new char[0]; 
        }
        String httpUser = info.getHttpUsername();
        if(httpUser == null) {
            httpUser = ""; // NOI18N
        }
        char[] httpPassword = info.getHttpPassword();
        if(httpPassword == null) {
            httpPassword = new char[0]; 
        }
        String url = info.getUrl();

        taskRepository = createTaskRepository(name, url, user, password, httpUser, httpPassword);
        JiraTaskListProvider.getInstance().notifyRepositoryCreated(this);
    }
    
    public RepositoryInfo getInfo() {
        return info;
    }
    
    public String getID() {
        return info.getId();
    }

    public JiraQuery createQuery() {
        if(getConfiguration() == null) {
            // invalid connection data?
            return null;
        }
        return new JiraQuery(this);
    }

    public NbJiraIssue createIssue() {
        if(getConfiguration() == null) {
            // invalid connection data?
            return null;
        }
        TaskAttributeMapper attributeMapper =
                Jira.getInstance()
                    .getRepositoryConnector()
                    .getTaskDataHandler()
                    .getAttributeMapper(taskRepository);
        TaskData data =
                new TaskData(
                    attributeMapper,
                    taskRepository.getConnectorKind(),
                    taskRepository.getRepositoryUrl(),
                    ""); // NOI18N
        return new NbJiraIssue(data, this);
    }

    synchronized void setInfoValues(String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {
        setTaskRepository(name, url, user, password, httpUser, httpPassword);
        String id = info != null ? info.getId() : name + System.currentTimeMillis();
        info = new RepositoryInfo(id, JiraConnector.ID, url, name, getTooltip(name, user, url), user, httpUser, password, httpPassword);
    }
        
    public String getDisplayName() {
        return info.getDisplayName();
    }

    private String getTooltip(String repoName, String user, String url) {
        return NbBundle.getMessage(JiraRepository.class, "LBL_RepositoryTooltip", new Object[] {repoName, user, url}); // NOI18N
    }

    public Image getIcon() {
        return icon;
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public NbJiraIssue getIssue(String key) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        TaskData taskData = JiraUtils.getTaskDataByKey(JiraRepository.this, key);
        if(taskData == null) {
            return null;
        }
        try {
            return getIssueCache().setIssueData(key, taskData);
        } catch (IOException ex) {
            Jira.LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public RepositoryController getController() {
        if(controller == null) {
            controller = new JiraRepositoryController(this);
        }
        return controller;
    }

    public Lookup getLookup() {
        if(lookup == null) {
            lookup = Lookups.fixed(getLookupObjects());
        }
        return lookup;
    }

    protected Object[] getLookupObjects() {
        return new Object[] { getIssueCache() };
    }

    public String getUrl() {
        return taskRepository != null ? taskRepository.getUrl() : null;
    }

    public void remove() {
        synchronized(QUERIES_LOCK) {
            Set<JiraQuery> qs = getQueriesIntern();
            for (JiraQuery q : qs) {
                removeQuery(q);
            }
        }
        resetRepository(true);
        JiraTaskListProvider.getInstance().notifyRepositoryRemoved(this);
    }

    public void removeQuery(JiraQuery query) {
        Jira.getInstance().getStorageManager().removeQuery(this, query);
        getIssueCache().removeQuery(query.getStoredQueryName());
        synchronized(QUERIES_LOCK) {
            getQueriesIntern().remove(query);
        }
        stopRefreshing(query);
    }

    public void saveQuery(JiraQuery query) {
        assert info != null;
        Jira.getInstance().getStorageManager().putQuery(this, query);
        synchronized (QUERIES_LOCK) {
            getQueriesIntern().add(query);
        }
    }

    private Set<JiraQuery> getQueriesIntern() {
        synchronized (QUERIES_LOCK) {
            if(queries == null) {
                JiraStorageManager manager = Jira.getInstance().getStorageManager();
                queries = manager.getQueries(this);
            }
            return queries;
        }
    }

    public Collection<JiraQuery> getQueries() {
        List<JiraQuery> ret = new ArrayList<JiraQuery>();
        synchronized (QUERIES_LOCK) {
            Set<JiraQuery> l = getQueriesIntern();
            ret.addAll(l);
            if(remoteFilters == null) {
                Jira.getInstance().getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        getRemoteFilters();
                    }
                });
            } else {
                ret.addAll(remoteFilters);
            }
        }
        return ret;
    }

    protected void getRemoteFilters() {
        List<JiraQuery> ret = new ArrayList<JiraQuery>();
        NamedFiltersCommand cmd = new NamedFiltersCommand(taskRepository);
        getExecutor().execute(cmd);
        if(!cmd.hasFailed()) {
            NamedFilter[] filters = cmd.getNamedFilters();
            if(filters != null) {
                for (NamedFilter nf : filters) {
                    JiraQuery q = new JiraQuery(nf.getName(), this, nf);
                    ret.add(q);
                    q.fireQuerySaved();
                }
            }
        }
        synchronized (QUERIES_LOCK) {
            remoteFilters = new HashSet<JiraQuery>();
            remoteFilters.addAll(ret);
        }
    }

    public Collection<NbJiraIssue> simpleSearch(String criteria) {
        assert taskRepository != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        String[] keywords = criteria.split(" ");                                // NOI18N

        final List<NbJiraIssue> issues = new ArrayList<NbJiraIssue>();
        TaskDataCollector collector = new TaskDataCollector() {
            @Override
            public void accept(TaskData taskData) {
                NbJiraIssue issue = new NbJiraIssue(taskData, JiraRepository.this);
                issues.add(issue); // we don't cache this issues
                                   // - the retured taskdata are partial
                                   // - and we need an as fast return as possible at this place

            }
        };

        if(keywords.length == 1) {
            // only one search criteria -> might be we are looking for the bug with id=keywords[0]
            keywords[0] = repairKeyIfNeeded(keywords[0]);
            if (keywords[0] != null) {
                TaskData taskData = JiraUtils.getTaskDataByKey(this, keywords[0], false);
                if (taskData != null) {
                    NbJiraIssue issue = new NbJiraIssue(taskData, JiraRepository.this);
                    issues.add(issue); // we don't cache this issues
                    // - the retured taskdata are partial
                    // - and we need an as fast return as possible at this place
                }
            }
        }

        // XXX escape special characters
        // + - && || ! ( ) { } [ ] ^ " ~ * ? \

        FilterDefinition fd = new FilterDefinition();
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(criteria, " \t");  // NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            sb.append(token);
            sb.append(' ');                                         // NOI18N
            sb.append(token);
            sb.append('*');                                         // NOI18N
            sb.append(' ');                                         // NOI18N
        }

        final ContentFilter cf = new ContentFilter(sb.toString(), true, false, false, false);
        fd.setContentFilter(cf);
        fd.setProjectFilter(getProjectFilter());
        PerformQueryCommand queryCmd = new PerformQueryCommand(this, fd, collector);
        getExecutor().execute(queryCmd);
        return issues;
    }

    public IssueCache<NbJiraIssue, TaskData> getIssueCache() {
        if(cache == null) {
            cache = new Cache();
        }
        return cache;
    }

    private void setTaskRepository(String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {
        String oldUrl = taskRepository != null ? taskRepository.getUrl() : "";
        AuthenticationCredentials c = taskRepository != null ? taskRepository.getCredentials(AuthenticationType.REPOSITORY) : null;
        String oldUser = c != null ? c.getUserName() : "";
        String oldPassword = c != null ? c.getPassword() : "";

        taskRepository = createTaskRepository(name, url, user, password, httpUser, httpPassword);
        
        resetRepository(oldUrl.equals(url) && oldUser.equals(user) && oldPassword.equals(password)); // XXX reset the configuration only if the host changed
    }

    public void setCredentials(String user, char[] password, String httpUser, char[] httpPassword) {
        MylynUtils.setCredentials(taskRepository, user, password, httpUser, httpPassword);
    }

    static TaskRepository createTaskRepository(String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {
        TaskRepository repository =
                new TaskRepository(
                    Jira.getInstance().getRepositoryConnector().getConnectorKind(),
                    url);
        MylynUtils.setCredentials(repository, user, password, httpUser, httpPassword);
        return repository;
    }

    public String getUsername() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c != null ? c.getUserName() : ""; // NOI18N
    }

    public char[] getPassword() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c != null ? c.getPassword().toCharArray() : new char[0]; 
    }

    public String getHttpUsername() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.HTTP);
        return c != null ? c.getUserName() : ""; // NOI18N
    }

    public char[] getHttpPassword() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.HTTP);
        return c != null ? c.getPassword().toCharArray() : new char[0]; 
    }

    void resetRepository(boolean keepConfiguration) {
        // XXX synchronization
        if(!keepConfiguration) {
            configuration = null;
        }
        synchronized (REPOSITORY_LOCK) {
            TaskRepository taskRepo = getTaskRepository();
            if (taskRepo != null) {
                Jira.getInstance().removeClient(taskRepo);
            }
        }
    }

    /**
     * Returns the jira configuration or null if not available
     *
     * @return
     */
    public JiraConfiguration getConfiguration() {
        synchronized (CONFIGURATION_LOCK) {
            if (configuration == null) {
                configuration = getConfigurationIntern(false);
            }
            return configuration;
        }
    }

    public void refreshConfiguration() {
        JiraConfiguration c = getConfigurationIntern(true);
        synchronized (CONFIGURATION_LOCK) {
            configuration = c;
        }
    }

    protected JiraConfiguration createConfiguration(JiraClient client) throws CoreException {
        return new JiraConfiguration(client, JiraRepository.this);
    }

    private JiraConfiguration getConfigurationIntern(final boolean forceRefresh) {
        // XXX need logging incl. consumed time

        class ConfigurationCommand extends JiraCommand {
            JiraConfiguration configuration;
            @Override
            public void execute() throws JiraException, CoreException, IOException, MalformedURLException {
                final JiraClient client = Jira.getInstance().getClient(getTaskRepository());

                boolean needRefresh = !client.getCache().hasDetails();
                Jira.LOG.log(Level.FINE, "configuration refresh {0} : needRefresh = {1} forceRefresh={2}", new Object[]{getUrl(), needRefresh, forceRefresh});
                if(forceRefresh || needRefresh) {
                    Jira.getInstance().getRepositoryConnector().updateRepositoryConfiguration(getTaskRepository(), new NullProgressMonitor());
                }
                configuration = createConfiguration(client);
            }
        }
        ConfigurationCommand cmd = new ConfigurationCommand();

        getExecutor().execute(cmd, true, false, false);
        if(!cmd.hasFailed()) {
            return cmd.configuration;
        }
        return null;
    }

    private void setupIssueRefreshTask() {
        if(refreshIssuesTask == null) {
            refreshIssuesTask = getRefreshProcessor().create(new Runnable() {
                @Override
                public void run() {
                    Set<String> ids;
                    synchronized(issuesToRefresh) {
                        ids = new HashSet<String>(issuesToRefresh);
                    }
                    if(ids.isEmpty()) {
                        Jira.LOG.log(Level.FINE, "no issues to refresh {0}", new Object[] {getDisplayName()}); // NOI18N
                        return;
                    }
                    Jira.LOG.log(Level.FINER, "preparing to refresh {0} - {1}", new Object[] {getDisplayName(), ids}); // NOI18N
                    for (String id : ids) {
                        try {
                            TaskData data = JiraUtils.getTaskDataById(JiraRepository.this, id, false);
                            if(data == null) {
                                Jira.LOG.log(Level.WARNING, "No task data available for issue with id {0}", id); // NOI18N
                            } else {
                                getIssueCache().setIssueData(id, data);
                            }
                        } catch (IOException ex) {
                            Jira.LOG.log(Level.SEVERE, null, ex); // NOI18N
                        }
                    }
                    scheduleIssueRefresh();
                }
            });
            scheduleIssueRefresh();
        }
    }

    private void setupQueryRefreshTask() {
        if(refreshQueryTask == null) {
            refreshQueryTask = getRefreshProcessor().create(new Runnable() {
                @Override
                public void run() {
                    try {
                        Set<JiraQuery> queries;
                        synchronized(refreshQueryTask) {
                            queries = new HashSet<JiraQuery>(queriesToRefresh);
                        }
                        if(queries.isEmpty()) {
                            Jira.LOG.log(Level.FINE, "no queries to refresh {0}", new Object[] {getDisplayName()}); // NOI18N
                            return;
                        }
                        for (JiraQuery q : queries) {
                            Jira.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[] {q.getDisplayName(), getDisplayName()}); // NOI18N
                            QueryController qc = q.getController();
                            qc.autoRefresh();
                        }
                    } finally {
                        scheduleQueryRefresh();
                    }
                }
            });
            scheduleQueryRefresh();
        }
    }

    private void scheduleIssueRefresh() {
        int delay = JiraConfig.getInstance().getIssueRefreshInterval();
        Jira.LOG.log(Level.FINE, "scheduling issue refresh for repository {0} in {1} minute(s)", new Object[] {getDisplayName(), delay}); // NOI18N
        refreshIssuesTask.schedule(delay * 60 * 1000); // given in minutes
    }

    private void scheduleQueryRefresh() {
        int delay = JiraConfig.getInstance().getQueryRefreshInterval();
        Jira.LOG.log(Level.FINE, "scheduling query refresh for repository {0} in {1} minute(s)", new Object[] {getDisplayName(), delay}); // NOI18N
        refreshQueryTask.schedule(delay * 60 * 1000); // given in minutes
    }

    public void scheduleForRefresh(String id) {
        Jira.LOG.log(Level.FINE, "scheduling issue {0} for refresh on repository {0}", new Object[] {id, getDisplayName()}); // NOI18N
        synchronized(issuesToRefresh) {
            issuesToRefresh.add(id);
        }
        setupIssueRefreshTask();
    }

    public void stopRefreshing(String id) {
        Jira.LOG.log(Level.FINE, "removing issue {0} from refresh on repository {1}", new Object[] {id, getDisplayName()}); // NOI18N
        synchronized(issuesToRefresh) {
            issuesToRefresh.remove(id);
        }
    }

    public void scheduleForRefresh(JiraQuery query) {
        Jira.LOG.log(Level.FINE, "scheduling query {0} for refresh on repository {1}", new Object[] {query.getDisplayName(), getDisplayName()}); // NOI18N
        synchronized(queriesToRefresh) {
            queriesToRefresh.add(query);
        }
        setupQueryRefreshTask();
    }

    public void stopRefreshing(JiraQuery query) {
        Jira.LOG.log(Level.FINE, "removing query {0} from refresh on repository {1}", new Object[] {query.getDisplayName(), getDisplayName()}); // NOI18N
        synchronized(queriesToRefresh) {
            queriesToRefresh.remove(query);
        }
    }

    public void refreshAllQueries() {
        Collection<JiraQuery> qs = getQueries();
        for (JiraQuery q : qs) {
            Jira.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[] {q.getDisplayName(), getDisplayName()}); // NOI18N
            QueryController qc = ((JiraQuery) q).getController();
            qc.onRefresh();
        }
    }

    public boolean authenticate(String errroMsg) {
        return BugtrackingUtil.editRepository(JiraUtils.getRepository(this), errroMsg);
    }

    private RequestProcessor getRefreshProcessor() {
        if(refreshProcessor == null) {
            refreshProcessor = new RequestProcessor("Jira refresh - " + getDisplayName()); // NOI18N
        }
        return refreshProcessor;
    }

    public Collection<RepositoryUser> getUsers() {
        Collection<User> users = getConfiguration().getUsers();
        List<RepositoryUser> members = new ArrayList<RepositoryUser>();
        for (User user : users) {
            members.add(new RepositoryUser(user.getName(), user.getFullName()));
        }
        return members;
    }

    public JiraExecutor getExecutor() {
        if(executor == null) {
            executor = new JiraExecutor(this);
        }
        return executor;
    }

    /**
     * Returns null if key is not a valid Jira issue key
     * @param key
     * @return
     */
    protected String repairKeyIfNeeded (String key) {
        String retval = null;
        try {
            Long.parseLong(key);
            // problem
            // mylyn will interpret this key as an ID
        } catch (NumberFormatException ex) {
            // this is good, no InsufficientRightsException will be thrown in mylyn
            retval = key;
        }
        return retval;
    }

    /**
     * Returns <code>null</code> for a general repository.
     * Override this to provide a valid project filter for a repository which is limited to a subset of all projects (e.g. kenai).
     * @return a project filter - <code>null</code> for this implementation.
     */
    protected ProjectFilter getProjectFilter () {
        return null;
    }

    private class Cache extends IssueCache<NbJiraIssue, TaskData> {
        Cache() {
            super(
                JiraRepository.this.getUrl(), 
                new IssueAccessorImpl(),
                Jira.getInstance().getIssueProvider(),
                JiraUtils.getRepository(JiraRepository.this));
        }
    }
    private class IssueAccessorImpl implements IssueCache.IssueAccessor<NbJiraIssue, TaskData> {
        @Override
        public NbJiraIssue createIssue(TaskData taskData) {
            NbJiraIssue issue = new NbJiraIssue(taskData, JiraRepository.this);
            org.netbeans.modules.jira.issue.JiraTaskListProvider.getInstance().notifyIssueCreated(issue);
            return issue;
        }
        @Override
        public void setIssueData(NbJiraIssue issue, TaskData taskData) {
            assert issue != null && taskData != null;
            ((NbJiraIssue)issue).setTaskData(taskData);
        }
        @Override
        public String getRecentChanges(NbJiraIssue issue) {
            assert issue != null;
            return ((NbJiraIssue)issue).getRecentChanges();
        }
        @Override
        public long getLastModified(NbJiraIssue issue) {
            assert issue != null;
            return ((NbJiraIssue)issue).getLastModify();
        }
        @Override
        public long getCreated(NbJiraIssue issue) {
            assert issue != null;
            return ((NbJiraIssue)issue).getCreated();
        }
        @Override
        public String getID(TaskData issueData) {
            assert issueData != null;
            return NbJiraIssue.getID(issueData);
        }
        @Override
        public Map<String, String> getAttributes(NbJiraIssue issue) {
            assert issue != null;
            return ((NbJiraIssue)issue).getAttributes();
        }
    }
}
