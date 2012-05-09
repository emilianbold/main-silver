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
package org.netbeans.modules.tasks.ui.dashboard;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.tasks.ui.actions.Actions;
import org.netbeans.modules.tasks.ui.actions.OpenTaskAction;
import org.netbeans.modules.tasks.ui.model.Category;
import org.netbeans.modules.tasks.ui.treelist.TreeLabel;
import org.netbeans.modules.tasks.ui.treelist.TreeListNode;
import org.netbeans.modules.tasks.ui.utils.Utils;

/**
 *
 * @author jpeska
 */
public class TaskNode extends TreeListNode implements Comparable<TaskNode> {

    private Issue task;
    private JPanel panel;
    private TreeLabel lblName;
    private Category category;

    public TaskNode(Issue task, TreeListNode parent) {
        // TODO subtasks, it is not in bugtracking API
        //super(task.hasSubtasks(), parent);
        super(false, parent);
        this.task = task;
    }

    @Override
    protected List<TreeListNode> createChildren() {
//        if(!task.hasSubtasks()){
//            return Collections.emptyList();
//        }
//        List<TaskNode> children = new ArrayList<TaskNode>();
//        List<Issue> tasks = task.getSubtasks();
//        AppliedFilters filters = DashboardViewer.getInstance().getAppliedFilters();
//        for (Issue t : tasks) {
//            if (filters.isInFilter(t)) {
//                children.add(new TaskNode(t, this));
//            }
//        }
//        Collections.sort(children);
//        return new ArrayList<TreeListNode>(children);
        return Collections.emptyList();
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(false);
            lblName = new TreeLabel();
            panel.add(lblName, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 3), 0, 0));
        }
        lblName.setText(Utils.getTaskDisplayString(task, lblName, rowWidth, DashboardViewer.getInstance().isTaskNodeActive(this), hasFocus));
        lblName.setToolTipText(task.getTooltip());
        lblName.setForeground(foreground);
        fireContentChanged();
        return panel;
    }

    @Override
    protected Action getDefaultAction() {
        return new OpenTaskAction(task);
    }

    @Override
    public Action[] getPopupActions() {
        List<Action> actions = Actions.getTaskPopupActions(this);
        return actions.toArray(new Action[actions.size()]);
    }

    public Issue getTask() {
        return task;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isCategorized() {
        return category != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaskNode other = (TaskNode) obj;
        if (isCategorized() && other.isCategorized()) {
            return taskEquals(other.getTask());
        } else {
            return getParent().equals(other.getParent()) && taskEquals(other.getTask());
        }
    }

    private boolean taskEquals(Issue other) {
        // TODO complete task equals method
        if (task.getStatus() != other.getStatus()) {
            return false;
        }
        if (!task.getRepository().getId().equalsIgnoreCase(other.getRepository().getId())) {
            return false;
        }
        if (!task.getID().equalsIgnoreCase(other.getID())) {
            return false;
        }
        if (!task.getDisplayName().equalsIgnoreCase(other.getDisplayName())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.task != null ? this.task.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(TaskNode toCompare) {
        int statusCompare = task.getStatus().compareTo(toCompare.task.getStatus());
        if (statusCompare != 0) {
            return statusCompare;
        } else {
            int id = Integer.parseInt(task.getID());
            int idOther = Integer.parseInt(toCompare.task.getID());
            if (id < idOther) {
                return 1;
            } else  if (id > idOther){
                return -1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public String toString() {
        return task.getDisplayName();
    }
}
