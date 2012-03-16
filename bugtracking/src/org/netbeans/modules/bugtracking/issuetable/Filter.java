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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.bugtracking.issuetable;

import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCacheUtils;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public abstract class Filter {

    private static Map<QueryImpl, Map<Class, Filter>> queryToFilter = new WeakHashMap<QueryImpl, Map<Class, Filter>>();

    public abstract String getDisplayName();
    public abstract boolean accept(IssueNode issue);

    public static Filter getAllFilter(QueryImpl query) {
        return getFilter(query, AllFilter.class);
    }
    public static Filter getNotSeenFilter(QueryImpl query) {
        return getFilter(query, NotSeenFilter.class);
    }
    public static Filter getNewFilter(QueryImpl query) {
        return getFilter(query, NewFilter.class);
    }
    public static Filter getObsoleteDateFilter(QueryImpl query) {
        return getFilter(query, ObsoleteDateFilter.class);
    }
    public static Filter getAllButObsoleteDateFilter(QueryImpl query) {
        return getFilter(query, AllButObsoleteDateFilter.class);
    }

    private static <T extends Filter> Filter getFilter(QueryImpl query, Class<T> clazz) {
        Map<Class, Filter> filters = queryToFilter.get(query);
        if(filters == null) {
            filters = new HashMap<Class, Filter>(5);
            queryToFilter.put(query, filters);
        }
        Filter filter = filters.get(clazz);
        if(filter == null) {
            try {
                Constructor<T> c;
                if(query == null) {
                    c = clazz.getDeclaredConstructor();
                    filter = c.newInstance();
                } else {
                    c = clazz.getDeclaredConstructor(QueryImpl.class);
                    filter = c.newInstance(query);
                }
            } catch (Exception ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            }            
            filters.put(clazz, filter);
        }
        return filter;
    }

    private static class AllFilter extends Filter {
        private final QueryImpl query;
        AllFilter(QueryImpl query) {
            this.query = query;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Filter.class, "LBL_AllIssuesFilter");     // NOI18N
        }
        @Override
        public boolean accept(IssueNode node) {
            return contains(query, node.getIssue().getID());
        }
    }
    private static class NotSeenFilter extends Filter {
        private final QueryImpl query;
        NotSeenFilter(QueryImpl query) {
            this.query = query;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Filter.class, "LBL_UnseenIssuesFilter");  // NOI18N
        }
        @Override
        public boolean accept(IssueNode node) {
            return !IssueCacheUtils.wasSeen(node.getIssue()) && contains(query, node.getIssue().getID());
        }
    }
    private static class NewFilter extends Filter {
        private final QueryImpl query;
        NewFilter(QueryImpl query) {
            this.query = query;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Filter.class, "LBL_NewIssuesFilter");     // NOI18N
        }
        @Override
        public boolean accept(IssueNode node) {
            return IssueCacheUtils.getStatus(node.getIssue()) == IssueCache.ISSUE_STATUS_NEW;
        }
    }
    private static class ObsoleteDateFilter extends Filter {
        private final QueryImpl query;
        ObsoleteDateFilter(QueryImpl query) {
            this.query = query;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Filter.class, "LBL_ObsoleteIssuesFilter");// NOI18N
        }
        @Override
        public boolean accept(IssueNode node) {
            return !contains(query, node.getIssue().getID());
        }
    }
    private static class AllButObsoleteDateFilter extends Filter {
        private final QueryImpl query;
        AllButObsoleteDateFilter(QueryImpl query) {
            this.query = query;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Filter.class, "LBL_AllButObsoleteIssuesFilter");  // NOI18N
        }
        @Override
        public boolean accept(IssueNode node) {
            return contains(query, node.getIssue().getID());
        }
    }
    
    private static boolean contains(QueryImpl query, String id) {
        return query.contains(id);
    }
    
}
