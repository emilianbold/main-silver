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
package org.netbeans.modules.ods.tasks.query;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.util.LogUtils;
import org.netbeans.modules.mylyn.GetMultiTaskDataCommand;
import org.netbeans.modules.mylyn.PerformQueryCommand;
import org.netbeans.modules.ods.tasks.C2C;
import org.netbeans.modules.ods.tasks.C2CConnector;
import org.netbeans.modules.ods.tasks.issue.C2CIssue;
import org.netbeans.modules.ods.tasks.repository.C2CRepository;
import org.netbeans.modules.ods.tasks.util.C2CUtil;

/**
 *
 * @author Tomas Stupka
 */
public class C2CQuery {

    private final C2CRepository repository;
    private C2CQueryController controller;

    private final List<QueryNotifyListener> notifyListeners = new ArrayList<QueryNotifyListener>();
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);;
    private String name;
    private long lastRefresh;
    private boolean saved;
    private boolean firstRun;
    private ColumnDescriptor[] columnDescriptors;
        
    public C2CQuery(C2CRepository repository) {
        this.repository = repository;
    }
    
    public C2CRepository getRepository() {
        return repository;
    }
    
    public boolean isSaved() {
        return saved;
    }

    int getSize() {
        return issues.size();
    }

    boolean wasRun() {
        return !firstRun;
    }

    public String getDisplayName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    void setSaved(boolean saved) {
        if(saved) {
//            XXX info = null;
        }
        this.saved = saved;
        fireQuerySaved();
    }

    long getLastRefresh() {
        return lastRefresh;
    }

    public boolean contains(String id) {
        return issues.contains(id);
    }

    public void remove() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Collection<C2CIssue> getIssues() {
        if (issues == null) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<String>();
        synchronized (issues) {
            ids.addAll(issues);
        }

        IssueCache<C2CIssue, TaskData> cache = repository.getIssueCache();
        List<C2CIssue> ret = new ArrayList<C2CIssue>();
        for (String id : ids) {
            ret.add(cache.getIssue(id));
        }
        return ret;
    }

    public ColumnDescriptor[] getColumnDescriptors() {
        if(columnDescriptors == null) {
            columnDescriptors = C2CIssue.getColumnDescriptors(repository);
        }
        return columnDescriptors;
    }
    
    public String getParametersString() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void addNotifyListener(QueryNotifyListener l) {
        synchronized(notifyListeners) {
            notifyListeners.add(l);
        }
    }

    public void removeNotifyListener(QueryNotifyListener l) {
        synchronized(notifyListeners) {
            notifyListeners.remove(l);
        }
    }

    protected void fireNotifyData(C2CIssue issue) {
        QueryNotifyListener[] list;
        synchronized(notifyListeners) {
            list = notifyListeners.toArray(new QueryNotifyListener[notifyListeners.size()]);
        }
        for (QueryNotifyListener l : list) {
            l.notifyData(issue);
        }
    }

    protected void fireStarted() {
        QueryNotifyListener[] list;
        synchronized(notifyListeners) {
            list = notifyListeners.toArray(new QueryNotifyListener[notifyListeners.size()]);
        }        
        for (QueryNotifyListener l : list) {
            l.started();
        }
    }

    protected void fireFinished() {
        QueryNotifyListener[] list;
        synchronized(notifyListeners) {
            list = notifyListeners.toArray(new QueryNotifyListener[notifyListeners.size()]);
        }        
        for (QueryNotifyListener l : list) {
            l.finished();
        }
    }

    public String getTooltip() {
        return name + " - " + repository.getDisplayName(); // NOI18N
    }

    public QueryController getController() {
        if(controller == null) {
            controller = new C2CQueryController(repository, this);
        }
        return controller;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private void fireQuerySaved() {
        support.firePropertyChange(QueryProvider.EVENT_QUERY_SAVED, null, null);
    }

    private void fireQueryRemoved() {
        support.firePropertyChange(QueryProvider.EVENT_QUERY_REMOVED, null, null);
    }

    private void fireQueryIssuesChanged() {
        support.firePropertyChange(QueryProvider.EVENT_QUERY_ISSUES_CHANGED, null, null);
    }  

    void refresh(Map<String, QueryParameter> parameters, boolean autoRefresh) {
        assert parameters != null;
        this.parameters = parameters;
        refreshIntern(autoRefresh);
    }
    
    public void refresh() {
        refreshIntern(false);
    }
    
    private final Set<String> issues = new HashSet<String>();
    private Set<String> archivedIssues = new HashSet<String>();
    private Map<String, QueryParameter> parameters = null;
    public void refreshIntern(final boolean autoRefresh) {
        
        assert parameters != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
          
        executeQuery(new Runnable() {
            @Override
            public void run() {
                C2C.LOG.log(Level.FINE, "refresh start - {0} [{1}]", new String[] {name, parameters.toString()}); // NOI18N
                try {
                    
                    // keeps all issues we will retrieve from the server
                    // - those matching the query criteria
                    // - and the obsolete ones
                    Set<String> queryIssues = new HashSet<String>();

                    issues.clear();
                    archivedIssues.clear();
                    if(isSaved()) {
                        if(!wasRun() && !issues.isEmpty()) {
                            C2C.LOG.log(Level.WARNING, "query {0} supposed to be run for the first time yet already contains issues.", getDisplayName()); // NOI18N
                            assert false;
                        }
                        // read the stored state ...
                        queryIssues.addAll(repository.getIssueCache().readQueryIssues(getDisplayName()));
                        queryIssues.addAll(repository.getIssueCache().readArchivedQueryIssues(getDisplayName()));
                        // ... and they might be rendered obsolete if not returned by the query
                        archivedIssues.addAll(queryIssues);
                    }
                    firstRun = false;

                    // run query to know what matches the criteria
                    
                    // IssuesIdCollector will populate the issues set
                    IRepositoryQuery query = new RepositoryQuery(C2C.getInstance().getRepositoryConnector().getConnectorKind(), "ODS query -" + getDisplayName());
                    for (Entry<String, QueryParameter> e : parameters.entrySet()) {
                        String attribute = e.getKey();
                        QueryParameter p = e.getValue();
                        String values = p.getValues();
                        if(values != null && !"".equals(values.trim())) {
                            query.setAttribute(attribute, p.getValues());
                        }
                    }
                    
                    PerformQueryCommand queryCmd = 
                        new PerformQueryCommand(
                            C2C.getInstance().getRepositoryConnector(),
                            repository.getTaskRepository(), 
                            new IssuesIdCollector(),
                            query);
                    repository.getExecutor().execute(queryCmd, !autoRefresh);
                    if(queryCmd.hasFailed()) {
                        return;
                    }

                    // only issues not returned by the query are obsolete
                    archivedIssues.removeAll(issues);
                    if(isSaved()) {
                        // ... and store all issues you got
                        repository.getIssueCache().storeQueryIssues(getDisplayName(), issues.toArray(new String[issues.size()]));
                        repository.getIssueCache().storeArchivedQueryIssues(getDisplayName(), archivedIssues.toArray(new String[archivedIssues.size()]));
                    }

                    // now get the task data for
                    // - all issue returned by the query
                    // - and issues which were returned by some previous run and are archived now
                    queryIssues.addAll(issues);

//                  XXX  getController().switchToDeterminateProgress(queryIssues.size());

                    // XXX this is toooooo slow - we should be able to work with partial taskData and get it whole only on issue open !!!!
                    for (String id : queryIssues) {
                        C2CIssue issue;
                        try {
                            TaskData taskData = C2CUtil.getTaskData(repository, id);
                            IssueCache<C2CIssue, TaskData> cache = repository.getIssueCache();
                            issue = (C2CIssue) cache.setIssueData(id, taskData);
                        } catch (IOException ex) {
                            C2C.LOG.log(Level.SEVERE, null, ex);
                            return;
                        }
                        fireNotifyData(issue); // XXX - !!! triggers getIssues()
                    }
                    
                } finally {
                    logQueryEvent(issues.size(), autoRefresh);
                    if(C2C.LOG.isLoggable(Level.FINE)) {
                        C2C.LOG.log(Level.FINE, "refresh finish - {0} [{1}]", new String[] {name, parameters.toString()}); // NOI18N
                    }
                }
            }
        });
    }

    protected void logQueryEvent(int count, boolean autoRefresh) {
        LogUtils.logQueryEvent(
            C2CConnector.ID,
            name,
            count,
            false,
            autoRefresh);
    }
    
    private void executeQuery (Runnable r) {
        fireStarted();
        try {
            r.run();
        } finally {
            fireFinished();
            fireQueryIssuesChanged();
            lastRefresh = System.currentTimeMillis();
        }
    }

    private class IssuesIdCollector extends TaskDataCollector {
        public IssuesIdCollector() {}
        @Override
        public void accept(TaskData taskData) {
            String id = C2CIssue.getID(taskData);
            
            System.out.println(" issue " + id);
            
            issues.add(id);
        }
    };
    private class IssuesCollector extends TaskDataCollector {
        public IssuesCollector() {}
        @Override
        public void accept(TaskData taskData) {
            String id = C2CIssue.getID(taskData);
//            XXX getController().addProgressUnit(C2CIssue.getDisplayName(taskData));
            C2CIssue issue;
            try {
                IssueCache<C2CIssue, TaskData> cache = repository.getIssueCache();
                issue = (C2CIssue) cache.setIssueData(id, taskData);
            } catch (IOException ex) {
                C2C.LOG.log(Level.SEVERE, null, ex);
                return;
            }
            fireNotifyData(issue); // XXX - !!! triggers getIssues()
        }
    };    
}
