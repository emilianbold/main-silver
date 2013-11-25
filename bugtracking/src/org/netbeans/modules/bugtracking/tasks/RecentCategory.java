/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskNode;
import org.netbeans.modules.bugtracking.team.spi.RecentIssue;
import org.openide.util.NbBundle;

public class RecentCategory extends Category {

    private final BugtrackingManager bugtrackingManager;
    private final RecentComparator recentComparator;
    private List<RecentIssue> recentIssues;
    private final Map<IssueImpl, RecentIssue> issue2recent = new HashMap<IssueImpl, RecentIssue>();

    public RecentCategory() {
        super(NbBundle.getMessage(RecentCategory.class, "LBL_Recent"), new ArrayList<IssueImpl>(), true);
        bugtrackingManager = BugtrackingManager.getInstance();
        recentComparator = new RecentComparator();
    }

    @Override
    public boolean persist() {
        return false;
    }

    @Override
    public List<IssueImpl> getTasks() {
        List<IssueImpl> result;
        synchronized (issue2recent) {
            recentIssues = new ArrayList<RecentIssue>();
            Collection<List<RecentIssue>> values = bugtrackingManager.getAllRecentIssues().values();
            for (List<RecentIssue> list : values) {
                recentIssues.addAll(list);
            }
            Collections.sort(recentIssues, recentComparator);

            result = new ArrayList<IssueImpl>(recentIssues.size());
            for (RecentIssue recentIssue : recentIssues) {
                IssueImpl impl = APIAccessor.IMPL.getImpl(recentIssue.getIssue());
                result.add(impl);
                issue2recent.put(impl, recentIssue);
            }
        }
        return result;
    }

    private RecentIssue getRecentIssue(IssueImpl impl) {
        return issue2recent.get(impl);
    }

    public Comparator<TaskNode> getTaskNodeComparator() {
        return new Comparator<TaskNode>() {

            @Override
            public int compare(TaskNode o1, TaskNode o2) {
                return recentComparator.compare(getRecentIssue(o1.getTask()), getRecentIssue(o2.getTask()));
            }
        };
    }

    private static class RecentComparator implements Comparator<RecentIssue> {

        @Override
        public int compare(RecentIssue i1, RecentIssue i2) {
            return -Long.compare(i1.getTimestamp(), i2.getTimestamp());
        }
    }
}