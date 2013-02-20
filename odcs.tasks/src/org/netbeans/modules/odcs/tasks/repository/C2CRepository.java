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
package org.netbeans.modules.odcs.tasks.repository;

import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.PasswordAuthentication;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiAccessor;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiProject;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.util.TextUtils;
import org.netbeans.modules.mylyn.util.MylynUtils;
import org.netbeans.modules.odcs.tasks.C2C;
import org.netbeans.modules.odcs.tasks.C2CConnector;
import org.netbeans.modules.odcs.tasks.C2CExecutor;
import org.netbeans.modules.odcs.tasks.issue.C2CIssue;
import org.netbeans.modules.odcs.tasks.query.C2CQuery;
import org.netbeans.modules.odcs.tasks.spi.C2CExtender;
import org.netbeans.modules.odcs.tasks.util.C2CUtil;
import org.netbeans.modules.mylyn.util.PerformQueryCommand;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka
 */
public class C2CRepository implements PropertyChangeListener {

    private final Object INFO_LOCK = new Object();
    private final Object QUERIES_LOCK = new Object();
    private RepositoryInfo info;
    private C2CRepositoryController controller;
    private TaskRepository taskRepository;
    private Lookup lookup;
    private Cache cache;
    private C2CExecutor executor;
    private Map<PredefinedTaskQuery, C2CQuery> predefinedQueries;
    private Collection<C2CQuery> remoteSavedQueries;
    private static final String ICON_PATH = "org/netbeans/modules/odcs/tasks/resources/repository.png"; //NOI18N
    private final Image icon;
    
    private PropertyChangeSupport support;
    
    private KenaiProject kenaiProject;
    
    public C2CRepository (KenaiProject kenaiProject) {
        this(createInfo(kenaiProject.getDisplayName(), kenaiProject.getFeatureLocation())); // use name as id - can't be changed anyway
        assert kenaiProject != null;
        this.kenaiProject = kenaiProject;
        KenaiUtil.getKenaiAccessor(kenaiProject.getFeatureLocation()).addPropertyChangeListener(this, kenaiProject.getWebLocation().toString());
    }
    
    public C2CRepository() {
        this.icon = ImageUtilities.loadImage(ICON_PATH, true);
        this.support = new PropertyChangeSupport(this);
    }
    
    public C2CRepository(RepositoryInfo info) {
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
        
        taskRepository = createTaskRepository(name, url, user, password, httpUser, httpPassword);
    }

    @NbBundle.Messages({"# {0} - repository name", "# {1} - url", "LBL_RepositoryTooltipNoUser={0} : {1}"})
    private static RepositoryInfo createInfo (String repoName, String url) {
        String id = getRepositoryId(repoName, url);
        String tooltip = Bundle.LBL_RepositoryTooltipNoUser(repoName, url);
        return new RepositoryInfo(id, C2CConnector.ID, url, repoName, tooltip);
    }
    
    private static String getRepositoryId (String name, String url) {
        return TextUtils.encodeURL(url) + ":" + name; //NOI18N
    }
    
    public KenaiProject getKenaiProject () {
        return kenaiProject;
    }
    
    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(KenaiAccessor.PROP_LOGIN)) {

            String user;
            char[] psswd;
            PasswordAuthentication pa =
                    KenaiUtil.getPasswordAuthentication(kenaiProject.getWebLocation().toString(), false); // do not force login
            if (pa != null) {
                user = pa.getUserName();
                psswd = pa.getPassword();
            } else {
                user = ""; //NOI18N
                psswd = new char[0];
            }

            setCredentials(user, psswd, null, null);
        }
    }
    

    public boolean authenticate (String errroMsg) {
        PasswordAuthentication pa = KenaiUtil.getPasswordAuthentication(kenaiProject.getWebLocation().toString(), true);
        if(pa == null) {
            return false;
        }
        
        String user = pa.getUserName();
        char[] password = pa.getPassword();

        setCredentials(user, password, null, null);

        return true;
    }
    
    static TaskRepository createTaskRepository(String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {
        AbstractRepositoryConnector rc = C2C.getInstance().getRepositoryConnector();
        return MylynUtils.createTaskRepository(rc.getConnectorKind(), name, url, user, password, httpUser, httpPassword);
    }
    
    public void ensureCredentials() {
        authenticate(null);
    }
    
    public synchronized void setCredentials(String user, char[] password, String httpUser, char[] httpPassword) {
        String oldUser = taskRepository == null ? null : taskRepository.getUserName();
        if (oldUser == null) {
            oldUser = ""; //NOI18N
        }
        MylynUtils.setCredentials(taskRepository, user, password, httpUser, httpPassword);
        if (!oldUser.equals(user)) {
            resetRepository(user.isEmpty());
        }
    }

    synchronized void setInfoValues(String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {
        setTaskRepository(name, url, user, password, httpUser, httpPassword);
        String id = info != null ? info.getId() : name + System.currentTimeMillis();
        info = new RepositoryInfo(id, C2CConnector.ID, url, name, getTooltip(name, user, url), user, httpUser, password, httpPassword);
    }
    
    private void setTaskRepository(String name, String url, String user, char[] password, String httpUser, char[] httpPassword) {
        taskRepository = createTaskRepository(name, url, user, password, httpUser, httpPassword);
        resetRepository(false); 
    }    
    
    synchronized void resetRepository (boolean logout) {
        synchronized (QUERIES_LOCK) {
            if (logout) {
                remoteSavedQueries.clear();
            } else {
                remoteSavedQueries = null;
            }
        }
        if(getTaskRepository() != null) {
            C2CExtender.repositoryRemoved(
                C2C.getInstance().getRepositoryConnector(),
                getTaskRepository()
            );
        }
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }
            
    public RepositoryInfo getInfo() {
        synchronized(INFO_LOCK) {
            return info;
        }
    }

    public String getDisplayName() {
        return info.getDisplayName();
    }
    
    public String getUrl() {
        return info.getUrl();
    }
    
    public Image getIcon() {
        return icon;
    }

    public void remove() {
        resetRepository(true);
    }

    public C2CIssue getIssue(final String id) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        TaskData taskData = C2CUtil.getTaskData(this, id);
        if(taskData == null) {
            return null;
        }
        try {
            C2CIssue issue = getIssueCache().setIssueData(id, taskData);
            // XXX ensureConfigurationUptodate(issue);
            return issue;
        } catch (IOException ex) {
            C2C.LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    
    public RepositoryController getControler() {
        if(controller == null) {
            controller = new C2CRepositoryController(this);
        }
        return controller;
    }

    public Collection<C2CIssue> simpleSearch(String criteria) {
        assert taskRepository != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        String[] keywords = criteria.split(" ");                                // NOI18N

        final List<C2CIssue> issues = new ArrayList<C2CIssue>();
        TaskDataCollector collector = new TaskDataCollector() {
            @Override
            public void accept(TaskData taskData) {
                C2CIssue issue = new C2CIssue(taskData, C2CRepository.this);
                issues.add(issue); // we don't cache this issues
                                   // - the retured taskdata are partial
                                   // - and we need an as fast return as possible at this place

            }
        };

        if(keywords.length == 1 && isInteger(keywords[0])) {
            // only one search criteria -> might be we are looking for the bug with id=keywords[0]
            TaskData taskData = C2CUtil.getTaskData(this, keywords[0], false);
            if(taskData != null) {
                C2CIssue issue = new C2CIssue(taskData, C2CRepository.this);
                issues.add(issue); // we don't cache this issues
                                   // - the retured taskdata are partial
                                   // - and we need an as fast return as possible at this place
            }
        }

        try {
            criteria = URLEncoder.encode(criteria, getTaskRepository().getCharacterEncoding());
        } catch (UnsupportedEncodingException ueex) {
            C2C.LOG.log(Level.INFO, null, ueex);
            try {
                criteria = URLEncoder.encode(criteria, "UTF-8"); // NOI18N
            } catch (UnsupportedEncodingException ex) {
                // should not happen
            }
        }

        // XXX shouldn't be only a perfect match 
        IRepositoryQuery iquery = new RepositoryQuery(taskRepository.getConnectorKind(), "ODCS simple task search");            // NOI18N
        iquery.setAttribute(TaskAttribute.SUMMARY, criteria);
        
        PerformQueryCommand queryCmd = 
            new PerformQueryCommand(
                C2C.getInstance().getRepositoryConnector(),
                getTaskRepository(), 
                collector,
                iquery);
        getExecutor().execute(queryCmd);
        if(queryCmd.hasFailed()) {
            return Collections.emptyList();
        }
        return issues;
        
        
    }
    
    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }    

    public Lookup getLookup() {
        if(lookup == null) {
            lookup = Lookups.fixed(new Object[] { getIssueCache(), kenaiProject });
        }
        return lookup;
    }

    public Collection<C2CQuery> getQueries() {
        List<C2CQuery> ret = new ArrayList<C2CQuery>();
        synchronized (QUERIES_LOCK) {
            initializePredefinedQueries();
            ret.addAll(predefinedQueries.values());
            if (remoteSavedQueries == null) {
                C2C.getInstance().getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        requestRemoteSavedQueries();
                    }
                });
            } else {
                ret.addAll(remoteSavedQueries);
            }
        }
        return ret;
    }
    
    protected void requestRemoteSavedQueries () {
        List<C2CQuery> queries = new ArrayList<C2CQuery>();
        ensureCredentials();
        RepositoryConfiguration conf = C2C.getInstance().getClientData(this).getRepositoryConfiguration();
        if (conf != null) {
            List<SavedTaskQuery> savedQueries = conf.getSavedTaskQueries();
            for (SavedTaskQuery sq : savedQueries) {
                C2CQuery q = C2CQuery.createSaved(this, sq);
                queries.add(q);
                
                C2C.LOG.log(Level.FINER, "added remote query {0} to repository {1}", new Object[]{sq.getName(), getDisplayName()});
            }
        }
        synchronized (QUERIES_LOCK) {
            remoteSavedQueries = new HashSet<C2CQuery>();
            remoteSavedQueries.addAll(queries);
        }
        
        if(!queries.isEmpty()) {
            fireQueryListChanged();
        }
    }

    private void initializePredefinedQueries () {
        if (predefinedQueries == null) {
            Map<PredefinedTaskQuery, IRepositoryQuery> queries = new EnumMap<PredefinedTaskQuery, IRepositoryQuery>(PredefinedTaskQuery.class);
            for (PredefinedTaskQuery ptq : PredefinedTaskQuery.values()) {
                queries.put(ptq, C2CExtender.getQuery(C2C.getInstance().getRepositoryConnector(), ptq, ptq.getLabel(), getTaskRepository().getConnectorKind()));
            }
            synchronized(QUERIES_LOCK) {
                predefinedQueries = new EnumMap<PredefinedTaskQuery, C2CQuery>(PredefinedTaskQuery.class);
                for (Map.Entry<PredefinedTaskQuery, IRepositoryQuery> e : queries.entrySet()) {
                    predefinedQueries.put(e.getKey(), C2CQuery.createPredefined(C2CRepository.this, e.getValue().getSummary(), e.getValue()));
                    
                    C2C.LOG.log(Level.FINER, "added predefined query {0} to repository {1}", new Object[]{e.getKey().name(), getDisplayName()});
                }
            }
        }
    }

    public final C2CQuery getPredefinedQuery (PredefinedTaskQuery ptq) {
        getQueries();
        synchronized (QUERIES_LOCK) {
            initializePredefinedQueries();
            return predefinedQueries.get(ptq);
        }
    }

    public C2CIssue createIssue() {
        TaskData data = C2CUtil.createTaskData(getTaskRepository());
        return new C2CIssue(data, this);
    }

    public C2CQuery createQuery() {
        return C2CQuery.createNew(this);
    }

    public C2CIssue[] getIssues(String[] ids) {
        if (ids.length == 0) {
            return new C2CIssue[0];
        } else {
            //TODO is there a bulk command?
            List<C2CIssue> issues = new ArrayList<C2CIssue>(ids.length);
            for (String id : ids) {
                C2CIssue i = getIssue(id);
                if (i != null) {
                    issues.add(i);
                }
            }
            return issues.toArray(new C2CIssue[issues.size()]);
        }
    }
    
    private String getTooltip(String repoName, String user, String url) {
        return NbBundle.getMessage(C2CRepository.class, "LBL_RepositoryTooltip", new Object[] {repoName, user, url}); // NOI18N
    }

    public void refreshConfiguration() {
        C2C.getInstance().refreshClientData(this);
    }

    public String getID() {
        return info.getId();
    }

    public IssueCache<C2CIssue, TaskData> getIssueCache() {
        if(cache == null) {
            cache = new Cache();
        }
        return cache;
    }

    public C2CExecutor getExecutor() {
        if(executor == null) {
            executor = new C2CExecutor(this);
        }
        return executor;
    }

    public void removeQuery(C2CQuery query) {
        getIssueCache().removeQuery(query.getDisplayName()); // XXX do we have to do this?
        synchronized (QUERIES_LOCK) {
            remoteSavedQueries.remove(query);
        }
        fireQueryListChanged();
    }

    public void saveQuery(C2CQuery query) {
        synchronized (QUERIES_LOCK) {
            remoteSavedQueries.add(query);
        }
        fireQueryListChanged();
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private void fireQueryListChanged() {
        support.firePropertyChange(RepositoryProvider.EVENT_QUERY_LIST_CHANGED, null, null);
    }
    
    private class Cache extends IssueCache<C2CIssue, TaskData> {
        Cache() {
            super(
                C2CRepository.this.getUrl(), 
                new IssueAccessorImpl(), 
                C2C.getInstance().getIssueProvider(), 
                C2CUtil.getRepository(C2CRepository.this));
        }
    }

    private class IssueAccessorImpl implements IssueCache.IssueAccessor<C2CIssue, TaskData> {
        @Override
        public C2CIssue createIssue(TaskData taskData) {
            C2CIssue issue = new C2CIssue(taskData, C2CRepository.this);
            return issue;
        }
        @Override
        public void setIssueData(C2CIssue issue, TaskData taskData) {
            assert issue != null && taskData != null;
            issue.setTaskData(taskData);
        }
        @Override
        public String getRecentChanges(C2CIssue issue) {
            assert issue != null;
            return issue.getRecentChanges();
        }
        @Override
        public long getLastModified(C2CIssue issue) {
            assert issue != null;
            return issue.getLastModify();
        }
        @Override
        public long getCreated(C2CIssue issue) {
            assert issue != null;
            return issue.getCreated();
        }
        @Override
        public String getID(TaskData issueData) {
            assert issueData != null;
            return C2CIssue.getID(issueData);
        }
        @Override
        public Map<String, String> getAttributes(C2CIssue issue) {
            assert issue != null;
            return issue.getAttributes();
        }
    }    
}
