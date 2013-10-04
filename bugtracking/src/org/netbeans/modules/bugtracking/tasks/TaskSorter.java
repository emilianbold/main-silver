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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.bugtracking.settings.DashboardSettings;
import org.netbeans.modules.bugtracking.tasks.dashboard.DashboardViewer;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskNode;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class TaskSorter{

    private static TaskSorter instance;
    private List<TaskAttribute> attributes;
    private static final String PRIORITY_ID = "tasks.attribute.priority";
    private static final String STATUS_ID = "tasks.attribute.status";
    private static final String TASKID_ID = "tasks.attribute.taskid";
    private static final String SCHEDULED_ID = "tasks.attribute.scheduled";

    public static TaskSorter getInstance() {
        if (instance == null) {
            instance = new TaskSorter();
        }
        return instance;
    }

    private TaskSorter(){
        initAttributes();
        sortAttributes();
    }

    public Comparator<TaskNode> getComparator(){
        return new TaskComparator();
    }

    public List<TaskAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<TaskAttribute> attributes) {
        this.attributes = attributes;
        DashboardSettings.getInstance().setSortingAttributes(attributes);
    }

    private void sortAttributes() {
        DashboardSettings.getInstance().updateAttributesRank(attributes);
        Collections.sort(attributes);
    }

    private class TaskComparator implements Comparator<TaskNode> {

        @Override
        public int compare(TaskNode t1, TaskNode t2) {
            for (TaskAttribute taskAttribute : attributes) {
                int compare = taskAttribute.compare(t1, t2);
                if (compare != 0) {
                    return compare;
                }
            }
            return 0;
        }
    }

    private void initAttributes(){
        attributes = new ArrayList<TaskAttribute>();
        TaskAttribute priority = new TaskAttribute(PRIORITY_ID, NbBundle.getMessage(TaskSorter.class, "LBL_PriorityDisplayName"), new Comparator<TaskNode>() {
            @Override
            public int compare(TaskNode tn1, TaskNode tn2) {
                //TODO implement
                return 0;
            }
        });
        attributes.add(priority);

        TaskAttribute status = new TaskAttribute(STATUS_ID, NbBundle.getMessage(TaskSorter.class, "LBL_StatusDisplayName"), new Comparator<TaskNode>() {
            @Override
            public int compare(TaskNode tn1, TaskNode tn2) {
                return tn1.getTask().getStatus().compareTo(tn2.getTask().getStatus());
            }
        });
        attributes.add(status);

        TaskAttribute taskId = new TaskAttribute(TASKID_ID, NbBundle.getMessage(TaskSorter.class, "LBL_TaskidDisplayName"), new Comparator<TaskNode>() {
            @Override
            public int compare(TaskNode tn1, TaskNode tn2) {
                return DashboardUtils.compareTaskIds(tn1.getTask().getID(), tn2.getTask().getID());
            }
        });
        attributes.add(taskId);

        TaskAttribute scheduled = new TaskAttribute(SCHEDULED_ID, NbBundle.getMessage(TaskSorter.class, "LBL_ScheduledDisplayName"), new Comparator<TaskNode>() {
            @Override
            public int compare(TaskNode tn1, TaskNode tn2) {
                //TODO implement
                return 0;
            }
        });
        attributes.add(scheduled);
    }
}