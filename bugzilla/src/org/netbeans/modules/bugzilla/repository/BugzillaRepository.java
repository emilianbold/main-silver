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
 * single choiceget of license, a recipient has the option to distribute
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

package org.netbeans.modules.bugzilla.repository;

import java.awt.EventQueue;
import org.netbeans.modules.bugzilla.*;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.modules.bugtracking.kenai.spi.OwnerInfo;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugtracking.kenai.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.cache.IssueCache;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.bugzilla.commands.BugzillaExecutor;
import org.netbeans.modules.bugzilla.query.QueryController;
import org.netbeans.modules.bugzilla.query.QueryParameter;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.mylyn.util.GetRepositoryTasksCommand;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.MylynUtils;
import org.netbeans.modules.mylyn.util.SimpleQueryCommand;
import org.netbeans.modules.mylyn.util.SynchronizeTasksCommand;
import org.openide.nodes.Node;
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
public class BugzillaRepository {

    private static final String ICON_PATH = "org/netbeans/modules/bugtracking/ui/resources/repository.png"; // NOI18N

    private RepositoryInfo info;
    private TaskRepository taskRepository;
    private BugzillaRepositoryController controller;
    private Set<BugzillaQuery> queries = null;
    private IssueCache<BugzillaIssue> cache;
    private BugzillaExecutor executor;
    private Image icon;
    private BugzillaConfiguration bc;
    private RequestProcessor refreshProcessor;

    private final Set<ITask> issuesToRefresh = new HashSet<ITask>(5);
    private final Set<BugzillaQuery> queriesToRefresh = new HashSet<BugzillaQuery>(3);
    private Task refreshIssuesTask;
    private Task refreshQueryTask;

    private PropertyChangeSupport support;
    
    private Lookup lookup;
    
    private final Object RC_LOCK = new Object();

    public BugzillaRepository() {
        icon = ImageUtilities.loadImage(ICON_PATH, true);
        support = new PropertyChangeSupport(this);
    }

    public BugzillaRepository(RepositoryInfo info) {
        this();
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
        boolean shortLoginEnabled = Boolean.parseBoolean(info.getValue(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN));
        taskRepository = setupTaskRepository(name, null, url, user, password, httpUser, httpPassword, shortLoginEnabled);
    }

    public RepositoryInfo getInfo() {
        return info;
    }

    public String getID() {
        return info.getId();
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public BugzillaQuery createQuery() {
        BugzillaConfiguration conf = getConfiguration();
        if(conf == null || !conf.isValid()) {
            // invalid connection data?
            return null;
        }
        BugzillaQuery q = new BugzillaQuery(this);        
        return q;
    }

    public BugzillaIssue createIssue() {
        BugzillaConfiguration conf = getConfiguration();
        if(conf == null || !conf.isValid()) {
            // invalid connection data?
            return null;
        }
        
        String product = null;
        String component = null;
        for (String productCandidate : conf.getProducts()) {
            // iterates because a product without a component throws NPE inside mylyn
            List<String> components = conf.getComponents(product);
            if (!components.isEmpty()) {
                product = productCandidate;
                component = components.get(0);
                break;
            }
        }
        
        ITask task;
        try {
            task = MylynSupport.getInstance().getMylynFactory().createTask(taskRepository, new TaskMapping(product, component));
            return new BugzillaIssue(task, this);
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.WARNING, null, ex);
            return null;
        }
    }

    public void remove() {
        Collection<BugzillaQuery> qs = getQueries();
        BugzillaQuery[] toRemove = qs.toArray(new BugzillaQuery[qs.size()]);
        for (BugzillaQuery q : toRemove) {
            removeQuery(q);
        }
        resetRepository(true);
        if (getTaskRepository() != null) {
            // Maybe it's not needed to remove in mylyn?
        }
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

    synchronized void resetRepository(boolean keepConfiguration) {
        if(!keepConfiguration) {
            bc = null;
        }
    }

    public String getDisplayName() {
        return info.getDisplayName();
    }

    private String getTooltip(String repoName, String user, String url) {
        return NbBundle.getMessage(BugzillaRepository.class, "LBL_RepositoryTooltip", new Object[] {repoName, user, url}); // NOI18N
    }

    public Image getIcon() {
        return icon;
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

    public BugzillaIssue[] getIssues(final String... ids) {
        final List<BugzillaIssue> ret = new LinkedList<BugzillaIssue>();
        try {
            MylynSupport supp = MylynSupport.getInstance();
            Set<String> unknownTasks = new HashSet<String>(ids.length);
            for (String id : ids) {
                BugzillaIssue issue = findIssueForTask(supp.getTask(getTaskRepository().getUrl(), id));
                if (issue == null) {
                    // must go online
                    unknownTasks.add(id);
                } else {
                    ret.add(issue);
                }
            }
            if (!unknownTasks.isEmpty()) {
                GetRepositoryTasksCommand cmd = supp.getMylynFactory()
                        .createGetRepositoryTasksCommand(taskRepository, unknownTasks);
                getExecutor().execute(cmd, true);
                for (ITask task : cmd.getTasks()) {
                    BugzillaIssue issue = findIssueForTask(task);
                    if (issue != null) {
                        ret.add(issue);
                    }
                }
            }
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.INFO, null, ex);
        }
        return ret.toArray(new BugzillaIssue[ret.size()]);
    }
    
    public BugzillaIssue getIssue(final String id) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        BugzillaIssue issue = findIssueForTask(BugzillaUtil.getTask(this, id, true));
        return issue;
    }

    // XXX create repo wih product if kenai project and use in queries
    public Collection<BugzillaIssue> simpleSearch(final String criteria) {
        assert taskRepository != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        String[] keywords = criteria.split(" ");                                // NOI18N

        final List<BugzillaIssue> issues = new ArrayList<BugzillaIssue>();

        if(keywords.length == 1 && isInteger(keywords[0])) {
            BugzillaIssue issue = findIssueForTask(BugzillaUtil.getTask(this, keywords[0], false));
            if (issue != null) {
                issues.add(issue);
            }
        }

        StringBuilder url = new StringBuilder();
        url.append(BugzillaConstants.URL_ADVANCED_BUG_LIST + "&short_desc_type=allwordssubstr&short_desc="); // NOI18N
        for (int i = 0; i < keywords.length; i++) {
            String val = keywords[i].trim();
            if(val.equals("")) {
                continue;
            }                                        // NOI18N
            try {
                val = URLEncoder.encode(val, getTaskRepository().getCharacterEncoding());
            } catch (UnsupportedEncodingException ueex) {
                Bugzilla.LOG.log(Level.INFO, null, ueex);
                try {
                    val = URLEncoder.encode(val, "UTF-8"); // NOI18N
                } catch (UnsupportedEncodingException ex) {
                    // should not happen
                }
            }
            url.append(val);
            if(i < keywords.length - 1) {
                url.append("+");                                                // NOI18N
            }
        }
        QueryParameter[] additionalParams = getSimpleSearchParameters();
        for (QueryParameter qp : additionalParams) {
            url.append(qp.get(true));
        }
        
        try {
            IRepositoryQuery iquery = MylynSupport.getInstance().getMylynFactory().createNewQuery(taskRepository, "bugzilla simple search query"); //NOI18N
            iquery.setUrl(url.toString());
            SimpleQueryCommand cmd = MylynSupport.getInstance().getMylynFactory()
                    .createSimpleQueryCommand(taskRepository, iquery);
            getExecutor().execute(cmd, false);
            for (ITask task : cmd.getTasks()) {
                BugzillaIssue issue = findIssueForTask(task);
                if (issue != null) {
                    issues.add(issue);
                }
            }
        } catch (CoreException ex) {
            // should not happen
            Bugzilla.LOG.log(Level.WARNING, null, ex);
        }
        return issues;
    }

    public RepositoryController getController() {
        if(controller == null) {
            controller = new BugzillaRepositoryController(this);
        }
        return controller;
    }

    public Collection<BugzillaQuery> getQueries() {
        return getQueriesIntern();
    }

    public IssueCache<BugzillaIssue> getIssueCache() {
        if(cache == null) {
            cache = new Cache();
        }
        return cache;
    }

    public void removeQuery(BugzillaQuery query) {        
        Bugzilla.LOG.log(Level.FINE, "removing query {0} for repository {1}", new Object[]{query.getDisplayName(), getDisplayName()}); // NOI18N
        BugzillaConfig.getInstance().removeQuery(this, query);
        getIssueCache().removeQuery(query.getStoredQueryName());
        getQueriesIntern().remove(query);
        stopRefreshing(query);
        fireQueryListChanged();
    }

    public void saveQuery(BugzillaQuery query) {
        assert info != null;
        Bugzilla.LOG.log(Level.FINE, "saving query {0} for repository {1}", new Object[]{query.getDisplayName(), getDisplayName()}); // NOI18N
        BugzillaConfig.getInstance().putQuery(this, query); 
        getQueriesIntern().add(query);
        fireQueryListChanged();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    private void fireQueryListChanged() {
        Bugzilla.LOG.log(Level.FINER, "firing query list changed for repository {0}", new Object[]{getDisplayName()}); // NOI18N
        support.firePropertyChange(RepositoryProvider.EVENT_QUERY_LIST_CHANGED, null, null);
    }
    
    private Set<BugzillaQuery> getQueriesIntern() {
        if(queries == null) {
            queries = new HashSet<BugzillaQuery>(10);
            String[] qs = BugzillaConfig.getInstance().getQueries(getID());
            for (String queryName : qs) {
                BugzillaQuery q = BugzillaConfig.getInstance().getQuery(this, queryName);
                if(q != null ) {
                    queries.add(q);
                } else {
                    Bugzilla.LOG.log(Level.WARNING, "Couldn''t find query with stored name {0}", queryName); // NOI18N
                }
            }
        }
        return queries;
    }

    public synchronized void setInfoValues(String user, char[] password) {
        setTaskRepository(info.getDisplayName(), info.getUrl(), user, password, null, null, Boolean.parseBoolean(info.getValue(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN)));
        info = new RepositoryInfo(
                        info.getId(), info.getConnectorId(), 
                        info.getUrl(), info.getDisplayName(), info.getTooltip(), 
                        user, null, password, null);
    }
    
    synchronized void setInfoValues(String name, String url, String user, char[] password, String httpUser, char[] httpPassword, boolean localUserEnabled) {
        setTaskRepository(name, url, user, password, httpUser, httpPassword, localUserEnabled);
        String id = info != null ? info.getId() : name + System.currentTimeMillis();
        info = new RepositoryInfo(id, BugzillaConnector.ID, url, name, getTooltip(name, user, url), user, httpUser, password, httpPassword);
    }
    
    public void ensureCredentials() {
        setCredentials(info.getUsername(), info.getPassword(), info.getHttpUsername(), info.getHttpPassword(), true);
    }
    
    public void setCredentials(String user, char[] password, String httpUser, char[] httpPassword) {
        setCredentials(user, password, httpUser, httpPassword, false);
    }
    
    private synchronized void setCredentials(String user, char[] password, String httpUser, char[] httpPassword, boolean keepConfiguration) {
        MylynUtils.setCredentials(taskRepository, user, password, httpUser, httpPassword);
        resetRepository(keepConfiguration);
    }

    protected synchronized void setTaskRepository(String user, char[] password) {
        setTaskRepository(info.getDisplayName(), info.getUrl(), user, password, null, null, Boolean.parseBoolean(info.getValue(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN)));
    }
    
    private void setTaskRepository(String name, String url, String user, char[] password, String httpUser, char[] httpPassword, boolean shortLoginEnabled) {

        String oldUrl = taskRepository != null ? taskRepository.getUrl() : "";
        AuthenticationCredentials c = taskRepository != null ? taskRepository.getCredentials(AuthenticationType.REPOSITORY) : null;
        String oldUser = c != null ? c.getUserName() : "";

        taskRepository = setupTaskRepository(name, oldUrl.equals(url) ? null : oldUrl,
                url, user, password, httpUser, httpPassword, shortLoginEnabled);
        resetRepository(oldUrl.equals(url) && oldUser.equals(user));
    }

    /**
     * If oldUrl is not null, gets the repository for the oldUrl and rewrites it
     * to the new url.
     */
    private static TaskRepository setupTaskRepository (String name, String oldUrl, String url, String user,
            char[] password, String httpUser, char[] httpPassword,
            boolean shortLoginEnabled) {
        TaskRepository repository;
        if (oldUrl == null) {
            repository = MylynSupport.getInstance().getTaskRepository(Bugzilla.getInstance().getRepositoryConnector(), url);
        } else {
            repository = MylynSupport.getInstance().getTaskRepository(Bugzilla.getInstance().getRepositoryConnector(), oldUrl);
            try {
                MylynSupport.getInstance().setRepositoryUrl(repository, url);
            } catch (CoreException ex) {
                Bugzilla.LOG.log(Level.WARNING, null, ex);
            }
        }
        setupProperties(repository, name, user, password, httpUser, httpPassword, shortLoginEnabled); 
        return repository;
    }

    static TaskRepository createTemporaryTaskRepository (String name, String url, String user,
            char[] password, String httpUser, char[] httpPassword,
            boolean localUserEnabled) {
        TaskRepository taskRepository = new TaskRepository(
                Bugzilla.getInstance().getRepositoryConnector().getConnectorKind(), url);
        setupProperties(taskRepository, name, user, password, httpUser, httpPassword, localUserEnabled);
        return taskRepository;
    }

    private static void setupProperties (TaskRepository repository, String displayName,
            String user, char[] password, String httpUser, char[] httpPassword,
            boolean shortLoginEnabled) {
        repository.setRepositoryLabel(displayName);
        MylynUtils.setCredentials(repository, user, password, httpUser, httpPassword);
        repository.setProperty(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN, shortLoginEnabled ? "true" : "false"); //NOI18N
    }

    public String getUrl() {
        return taskRepository != null ? taskRepository.getUrl() : null;
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    public BugzillaExecutor getExecutor() {
        if(executor == null) {
            executor = new BugzillaExecutor(this);
        }
        return executor;
    }

    public boolean authenticate(String errroMsg) {
        return BugtrackingUtil.editRepository(BugzillaUtil.getRepository(this), errroMsg);
    }

    /**
     *
     * @return true if the repository accepts usernames in a short form (without domain specification).
     */
    public boolean isShortUsernamesEnabled() {
        return taskRepository != null && "true".equals(taskRepository.getProperty(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN));
    }

    public Collection<RepositoryUser> getUsers() {
        return Collections.emptyList();
    }

    public OwnerInfo getOwnerInfo(Node[] nodes) {
        if(nodes == null || nodes.length == 0) {
            return null;
        }
        if(BugzillaUtil.isNbRepository(this)) {
            if(nodes != null && nodes.length > 0) {
                OwnerInfo ownerInfo = KenaiUtil.getOwnerInfo(nodes[0]);
                if(ownerInfo != null /*&& ownerInfo.getOwner().equals(product)*/ ) {
                    return ownerInfo;
                }
            }
        }
        return null;
    }

    /**
     * Returns the bugzilla configuration or null if not available
     * 
     * @return
     */
    public BugzillaConfiguration getConfiguration() {
        synchronized(RC_LOCK) {
            if(bc == null) {
                bc = createConfiguration(false);
            } else if(!bc.isValid()) {
                // there was already an attempt to get the configuration
                // yet it happend to be invalid, so try one more time as it 
                // might have been just a networking glitch  
                bc = createConfiguration(false);
            }
            return bc;
        }
    }

    public synchronized void refreshConfiguration() {
        synchronized(RC_LOCK) {
            BugzillaConfiguration conf = createConfiguration(true);
            if(conf.isValid()) {
                bc = conf;
            } else {
                // Hard to say at this point why the attempt to refresh the 
                // configuration failed - could be just a temporary networking issue.
                // This is called only from ensureConfigurationUptodate(), so even if
                // the metadata might not be uptodate anymore, they still may be 
                // sufficient for what the user plans to do. So let's cross the 
                // fingers and keep bc the way it is.
            }
        }
    }

    protected BugzillaConfiguration createConfiguration(boolean forceRefresh) {
        BugzillaConfiguration conf = new BugzillaConfiguration();
        conf.initialize(this, forceRefresh);
        return conf;
    }

    private void setupIssueRefreshTask() {
        if(refreshIssuesTask == null) {
            refreshIssuesTask = getRefreshProcessor().create(new Runnable() {
                @Override
                public void run() {
                    Set<ITask> tasks;
                    synchronized(issuesToRefresh) {
                        tasks = new HashSet<ITask>(issuesToRefresh);
                    }
                    if(tasks.isEmpty()) {
                        Bugzilla.LOG.log(Level.FINE, "no issues to refresh {0}", new Object[] {getDisplayName()}); // NOI18N
                        return;
                    }
                    Bugzilla.LOG.log(Level.FINER, "preparing to refresh issue {0} - {1}", new Object[] {getDisplayName(), tasks}); // NOI18N
                    try {
                        SynchronizeTasksCommand cmd = MylynSupport.getInstance().getMylynFactory()
                                .createSynchronizeTasksCommand(taskRepository, tasks);
                        getExecutor().execute(cmd, false);
                    } catch (CoreException ex) {
                        // should not happen
                        Bugzilla.LOG.log(Level.WARNING, null, ex);
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
                        Set<BugzillaQuery> queries;
                        synchronized(refreshQueryTask) {
                            queries = new HashSet<BugzillaQuery>(queriesToRefresh);
                        }
                        if(queries.isEmpty()) {
                            Bugzilla.LOG.log(Level.FINE, "no queries to refresh {0}", new Object[] {getDisplayName()}); // NOI18N
                            return;
                        }
                        for (BugzillaQuery q : queries) {
                            Bugzilla.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[] {q.getDisplayName(), getDisplayName()}); // NOI18N
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
        int delay = BugzillaConfig.getInstance().getIssueRefreshInterval();
        Bugzilla.LOG.log(Level.FINE, "scheduling issue refresh for repository {0} in {1} minute(s)", new Object[] {getDisplayName(), delay}); // NOI18N
        if(delay < 5 && System.getProperty("netbeans.t9y.bugzilla.force.refresh.delay") == null) {
            Bugzilla.LOG.log(Level.WARNING, " wrong issue refresh delay {0}. Falling back to default {0}", new Object[] {delay, BugzillaConfig.DEFAULT_ISSUE_REFRESH}); // NOI18N
            delay = BugzillaConfig.DEFAULT_ISSUE_REFRESH;
        }
        refreshIssuesTask.schedule(delay * 60 * 1000); // given in minutes
    }

    private void scheduleQueryRefresh() {
        String schedule = System.getProperty("netbeans.t9y.bugzilla.force.refresh.schedule", "");
        if(!schedule.isEmpty()) {
            int delay = Integer.parseInt(schedule);
            refreshQueryTask.schedule(delay); 
            return;
        }
        
        int delay = BugzillaConfig.getInstance().getQueryRefreshInterval();
        Bugzilla.LOG.log(Level.FINE, "scheduling query refresh for repository {0} in {1} minute(s)", new Object[] {getDisplayName(), delay}); // NOI18N
        if(delay < 5) {
            Bugzilla.LOG.log(Level.WARNING, " wrong query refresh delay {0}. Falling back to default {0}", new Object[] {delay, BugzillaConfig.DEFAULT_QUERY_REFRESH}); // NOI18N
            delay = BugzillaConfig.DEFAULT_QUERY_REFRESH;
        }
        refreshQueryTask.schedule(delay * 60 * 1000); // given in minutes
    }

    public void scheduleForRefresh (ITask task) {
        Bugzilla.LOG.log(Level.FINE, "scheduling issue {0} for refresh on repository {0}", new Object[] {task.getTaskId(), getDisplayName()}); // NOI18N
        synchronized(issuesToRefresh) {
            issuesToRefresh.add(task);
        }
        setupIssueRefreshTask();
    }

    public void stopRefreshing (ITask task) {
        Bugzilla.LOG.log(Level.FINE, "removing issue {0} from refresh on repository {1}", new Object[] {task.getTaskId(), getDisplayName()}); // NOI18N
        synchronized(issuesToRefresh) {
            issuesToRefresh.remove(task);
        }
    }

    public void scheduleForRefresh(BugzillaQuery query) {
        Bugzilla.LOG.log(Level.FINE, "scheduling query {0} for refresh on repository {1}", new Object[] {query.getDisplayName(), getDisplayName()}); // NOI18N
        synchronized(queriesToRefresh) {
            queriesToRefresh.add(query);
        }
        setupQueryRefreshTask();
    }

    public void stopRefreshing(BugzillaQuery query) {
        Bugzilla.LOG.log(Level.FINE, "removing query {0} from refresh on repository {1}", new Object[] {query.getDisplayName(), getDisplayName()}); // NOI18N
        synchronized(queriesToRefresh) {
            queriesToRefresh.remove(query);
        }
    }

    public void refreshAllQueries() {
        refreshAllQueries(false);
    }

    protected void refreshAllQueries(final boolean onlyOpened) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Collection<BugzillaQuery> qs = getQueries();
                for (BugzillaQuery q : qs) {
                    if(onlyOpened && !Bugzilla.getInstance().getBugtrackingFactory().isOpen(BugzillaUtil.getRepository(BugzillaRepository.this), q)) {
                        continue;
                    }
                    Bugzilla.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[] {q.getDisplayName(), getDisplayName()}); // NOI18N
                    QueryController qc = ((BugzillaQuery) q).getController();
                    qc.onRefresh();
                }
            }
        });
    }

    private RequestProcessor getRefreshProcessor() {
        if(refreshProcessor == null) {
            refreshProcessor = new RequestProcessor("Bugzilla refresh - " + getDisplayName()); // NOI18N
        }
        return refreshProcessor;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + getDisplayName() + ')';        //NOI18N
    }

    protected QueryParameter[] getSimpleSearchParameters () {
        return new QueryParameter[] {};
    }

    private BugzillaIssue findIssueForTask (ITask task) {
        BugzillaIssue issue = null;
        if (task != null) {
            try {
                IssueCache<BugzillaIssue> cache = getIssueCache();
                issue = cache.getIssue(task.getTaskId());
                if (issue != null) {
                    issue.setTask(task);
                }
                issue = cache.setIssueData(task.getTaskId(), issue != null ? issue : new BugzillaIssue(task, this));
            } catch (IOException ex) {
                Bugzilla.LOG.log(Level.INFO, null, ex);
            }
        }
        return issue;
    }

    private static class TaskMapping extends org.eclipse.mylyn.tasks.core.TaskMapping {
        private final String component;
        private final String product;

        public TaskMapping (String product, String component) {
            this.product = product;
            this.component = component;
        }

        @Override
        public String getProduct () {
            return product;
        }

        @Override
        public String getComponent () {
            return component;
        }
    }

    private class Cache extends IssueCache<BugzillaIssue> {
        Cache() {
            super(BugzillaRepository.this.getUrl(), new IssueAccessorImpl());
        }
    }

    private class IssueAccessorImpl implements IssueCache.IssueAccessor<BugzillaIssue> {
        @Override
        public long getLastModified(BugzillaIssue issue) {
            assert issue != null;
            return ((BugzillaIssue)issue).getLastModify();
        }
        @Override
        public long getCreated(BugzillaIssue issue) {
            assert issue != null;
            return ((BugzillaIssue)issue).getCreated();
        }
        @Override
        public Map<String, String> getAttributes(BugzillaIssue issue) {
            return Collections.<String, String>emptyMap();
        }
    }

}
