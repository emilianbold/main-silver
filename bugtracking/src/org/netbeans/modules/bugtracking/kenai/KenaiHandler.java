/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.kenai;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiSupport;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.bugtracking.ui.query.QueryTopComponent;
import org.netbeans.modules.bugtracking.util.KenaiUtil;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.QueryHandle;
import org.netbeans.modules.kenai.ui.spi.QueryResultHandle;
import org.openide.util.NbBundle;

/**
 *
 * @author tomas
 */
public class KenaiHandler implements PropertyChangeListener {


    private final QueryAccessorImpl qaImpl;
    private final Kenai kenai;

    final private Map<String, ProjectHandleListener> projectListeners = new HashMap<String, ProjectHandleListener>();
    final private Map<String, KenaiRepositoryListener> kenaiRepoListeners = new HashMap<String, KenaiRepositoryListener>();
    final private Map<String, Map<String, QueryHandle>> queryHandles = new HashMap<String, Map<String, QueryHandle>>();

    private String lastLoggedUser = null;

    public KenaiHandler(QueryAccessorImpl qaImpl, Kenai kenai) {
        this.qaImpl = qaImpl;
        this.kenai = kenai;
        this.kenai.addPropertyChangeListener(this);
        Dashboard.getDefault().addPropertyChangeListener(this);
        lastLoggedUser = getKenaiUser();
    }

    List<QueryHandle> getQueryHandles(ProjectHandle project, Query... queries) {
        List<QueryHandle> ret = new ArrayList<QueryHandle>();
        synchronized (queryHandles) {
            Map<String, QueryHandle> m = queryHandles.get(project.getId());
            if (m == null) {
                m = new HashMap<String, QueryHandle>();
                queryHandles.put(project.getId(), m);
            } else {
                List<String> l = new ArrayList<String>();
                for (Query q : queries) {
                    if (q != null) {
                        String qName = q.getDisplayName();
                        l.add(qName);
                    }
                }
                m.keySet().retainAll(l);
            }
            for (Query q : queries) {
                String qName = q.getDisplayName();
                QueryHandle qh = m.get(qName);
                if (qh == null) {
                    Issue[] issues = q.getIssues();
                    // XXX HACK - totaly new queries should be refreshed.
                    //            unfortunatelly, an already refreshed query with
                    //            will be unnecessarilly refreshed one more time
                    //            as needed.
                    if(issues != null && issues.length > 0) {
                        qh = createQueryHandle(q, false);
                    } else {
                        qh = createQueryHandle(q, true); // true -> needs refresh
                    }
                    m.put(qName, qh);
                }
                ret.add(qh);
            }
        }
        sortQueries(ret);
        return ret;
    }

    private QueryHandleImpl createQueryHandle(Query q, boolean needsRefresh) {
        Repository repo = q.getRepository();
        KenaiSupport support = repo.getLookup().lookup(KenaiSupport.class);
        boolean predefined = false;
        if(support != null) {
            boolean needsLogin = support.needsLogin(q);
            predefined = support.getAllIssuesQuery(repo) == q || support.getMyIssuesQuery(repo) == q;
            if(needsLogin) {
                return new LoginAwareQueryHandle(q, needsRefresh, predefined);
            }
        }
        return new QueryHandleImpl(q, needsRefresh, predefined);
    }

    List<QueryHandle> getQueryHandles(Repository repo, ProjectHandle projectHandle) {
        Query[] queries = repo.getQueries();
        if(queries == null) {
            // XXX is this possible - at least preset queries
            return Collections.emptyList();
        }
        return getQueryHandles(projectHandle, queries);
    }

    private void sortQueries(List<QueryHandle> queryHandles) {
        Collections.sort(queryHandles, new Comparator<QueryHandle>() {
            public int compare(QueryHandle qh1, QueryHandle qh2) {
                if(qh1 == null && qh1 == null) {
                    return 0;
                }
                if(qh2 == null) {
                    return 1;
                }
                if(qh1 == null) {
                    return -1;
                }
                boolean predefined1 = false;
                boolean predefined2 = false;
                if(qh1 instanceof QueryDescriptor && ((QueryDescriptor) qh1).isPredefined()) predefined1 = true;
                if(qh2 instanceof QueryDescriptor && ((QueryDescriptor) qh2).isPredefined()) predefined2 = true;
                if(predefined1 && !predefined2) {
                    return -1;
                } else if(predefined1 && !predefined2) {
                    return 1;
                }
                return qh1.getDisplayName().compareTo(qh2.getDisplayName());
            }
        });
    }

    void registerProject(ProjectHandle project, List<QueryHandle> queries) {
        ProjectHandleListener pl;
        String url = project.getKenaiProject().getKenai().getUrl().toString();
        synchronized (projectListeners) {
            pl = projectListeners.get(project.getId());
        }
        if (pl != null) {
            project.removePropertyChangeListener(pl);
        }
        pl = new ProjectHandleListener(project, queries);
        project.addPropertyChangeListener(pl);
        synchronized (projectListeners) {
            projectListeners.put(project.getId(), pl);
        }
    }

    void registerRepository(Repository repo, ProjectHandle project) {
        KenaiRepositoryListener krl = null;
        synchronized (kenaiRepoListeners) {
            String url = project.getKenaiProject().getKenai().getUrl().toString();
            krl = kenaiRepoListeners.get(repo.getID());
            if (krl == null) {
                krl = new KenaiRepositoryListener(repo, project);
                repo.addPropertyChangeListener(krl);
                kenaiRepoListeners.put(repo.getID(), krl);
            }
        }
    }

    private String getKenaiUser() {
        PasswordAuthentication pa = KenaiUtil.getPasswordAuthentication(kenai, false);
        return pa != null ? pa.getUserName() : null;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Dashboard.PROP_REFRESH_REQUEST)) {
            if(kenai.equals(((Dashboard)evt.getSource())/* XXX .getKenai()*/)) {
                clear();
            }
        } else if(evt.getPropertyName().equals(Kenai.PROP_LOGIN)) {
            if(evt.getNewValue() == null) { // means logged out                
                ProjectHandleListener[] pls;
                synchronized(projectListeners) {
                    pls = projectListeners.values().toArray(new ProjectHandleListener[projectListeners.values().size()]);
                }
                for (ProjectHandleListener pl : pls) {
                    pl.closeQueries();
                }
            } else {
                // logged in
                String user = getKenaiUser();
                if(!user.equals(lastLoggedUser)) {
                    for(Map<String, QueryHandle> m : queryHandles.values()) {
                        for(QueryHandle qh : m.values()) {
                            if(qh instanceof LoginAwareQueryHandle) {
                                ((LoginAwareQueryHandle)qh).needsRefresh();
                            }
                        }
                    }
                }
                user = lastLoggedUser;
            }
            refreshKenaiQueries();
        }
    }

    private void refreshKenaiQueries() {
        Set<QueryTopComponent> tcs = QueryTopComponent.getOpenQueries(); // XXX updates also non kenai TC
        for (QueryTopComponent tc : tcs) {
            tc.updateSavedQueries();
        }
    }

    void clear() {
        synchronized(projectListeners) {
            projectListeners.clear();
        }
        synchronized(kenaiRepoListeners) {
            kenaiRepoListeners.clear();
        }
        synchronized(queryHandles) {
            queryHandles.clear();
        }
    }

    Action getFindIssuesAction(final Repository repo) {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if(!KenaiUtil.isLoggedIn(kenai) &&
                    KenaiSupport.BugtrackingType.JIRA == getBugtrackingType(repo) &&
                   !KenaiUtil.showLogin())
                {
                    return;
                }
                BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() { // XXX add post method to BM
                    public void run() {
                        QueryAction.openQuery(null, repo, true);
                    }
                });
            }
        };
    }

    Action getCreateIssueAction(final Repository repo) {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if(!KenaiUtil.isLoggedIn(kenai) &&
                    KenaiSupport.BugtrackingType.JIRA == getBugtrackingType(repo) &&
                   !KenaiUtil.showLogin())
                {
                    return;
                }
                BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() { // XXX add post method to BM
                    public void run() {
                        IssueAction.openIssue(repo);
                    }
                });
            }
        };
    }

    private KenaiSupport.BugtrackingType getBugtrackingType(Repository repo) {
        KenaiSupport support = repo.getLookup().lookup(KenaiSupport.class);
        if(support != null) {
            return support.getType();
        } else {
            assert false : "no KenaiSupport available for repository [" + repo.getDisplayName() + "]";  // NOI18N
        }
        return null;
    }

    private class ProjectHandleListener implements PropertyChangeListener {
        private List<QueryHandle> queries;
        private ProjectHandle ph;
        public ProjectHandleListener(ProjectHandle ph, List<QueryHandle> queries) {
            this.queries = queries;
            this.ph = ph;
        }
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(ProjectHandle.PROP_CLOSE)) {
                closeQueries();
            }
        }
        public void closeQueries() {
            for (QueryHandle qh : queries) {
                if(qh instanceof QueryHandleImpl) {
                    QueryAction.closeQuery(((QueryHandleImpl) qh).getQuery());
                }
            }
            synchronized (projectListeners) {
                ph.removePropertyChangeListener(this);
                projectListeners.remove(ph.getId());
            }
        }
    }

    private class KenaiRepositoryListener implements PropertyChangeListener {
        private final ProjectHandle ph;
        private Repository repo;

        public KenaiRepositoryListener(Repository repo, ProjectHandle ph) {
            this.ph = ph;
            this.repo = repo;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(Repository.EVENT_QUERY_LIST_CHANGED)) {
                qaImpl.fireQueriesChanged(ph, getQueryHandles(repo, ph));
            }
        }
    }

    private class LoginAwareQueryHandle extends QueryHandleImpl {
        private String notLoggedIn = NbBundle.getMessage(QueryAccessorImpl.class, "LBL_NotLoggedIn"); // NOI18N
        public LoginAwareQueryHandle(Query query, boolean needsRefresh, boolean predefined) {
            super(query, needsRefresh, predefined);
        }
        @Override
        public String getDisplayName() {
            return super.getDisplayName() + (KenaiUtil.isLoggedIn(kenai) ? "" : " " + notLoggedIn);        // NOI18N
        }
        @Override
        List<QueryResultHandle> getQueryResults() {
            return KenaiUtil.isLoggedIn(kenai) ? super.getQueryResults() : Collections.EMPTY_LIST;
        }
        @Override
        void refreshIfNeeded() {
            if(!KenaiUtil.isLoggedIn(kenai)) {
                return;
            }
            super.refreshIfNeeded();
        }
        void needsRefresh() {
            super.needsRefresh = true;
        }
    }
}
