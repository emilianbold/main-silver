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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.tasks.ui.LinkButton;
import org.netbeans.modules.tasks.ui.actions.Actions;
import org.netbeans.modules.tasks.ui.actions.OpenQueryAction;
import org.netbeans.modules.tasks.ui.treelist.TreeLabel;
import org.netbeans.modules.tasks.ui.treelist.TreeListNode;

/**
 *
 * @author jpeska
 */
public class QueryNode extends TaskContainerNode implements Comparable<QueryNode> {

    private final Query query;
    private JPanel panel;
    private TreeLabel lblName;
    private LinkButton btnChanged;
    private LinkButton btnTotal;
    private QueryListener queryListener;
    final Object LOCK = new Object();
    private TreeLabel lblSeparator;

    public QueryNode(Query query, TreeListNode parent, boolean refresh) {
        super(refresh, true, parent, query.getDisplayName());
        this.query = query;
        updateNodes();
        queryListener = new QueryListener();
        query.addPropertyChangeListener(queryListener);
    }

    @Override
    protected List<Issue> load() {
        if (isRefresh()) {
            query.refresh();
            setRefresh(false);
        }
        return new ArrayList<Issue>(query.getIssues());
    }

    @Override
    protected void dispose() {
        super.dispose();
        query.removePropertyChangeListener(queryListener);
    }

    @Override
    protected List<TreeListNode> createChildren() {
        if (isRefresh()) {
            query.refresh();
            updateNodes();
            setRefresh(false);
        }
        List<TaskNode> filteredNodes = getFilteredTaskNodes();
        Collections.sort(filteredNodes);
        int taskCountToShow = getTaskCountToShow();
        List<TaskNode> taskNodesToShow;
        boolean addShowNext = false;
        if (filteredNodes.size() <= taskCountToShow) {
            taskNodesToShow = filteredNodes;
        } else {
            taskNodesToShow = new ArrayList<TaskNode>(filteredNodes.subList(0, taskCountToShow));
            addShowNext = true;
        }
        ArrayList<TreeListNode> children = new ArrayList<TreeListNode>(taskNodesToShow);
        if (addShowNext) {
            children.add(new ShowNextNode(this, Math.min(filteredNodes.size() - taskCountToShow, DEFAULT_TASKS_LIMIT)));
        }
        return children;
    }

    @Override
    void updateCounts() {
        if (panel != null) {
            btnTotal.setText(getTotalString());
            btnChanged.setText(getChangedString());
            boolean showChanged = getChangedTaskCount() > 0;
            lblSeparator.setVisible(showChanged);
            btnChanged.setVisible(showChanged);
        }
    }

    @Override
    List<Issue> getTasks() {
        return new ArrayList<Issue>(query.getIssues());
    }

    @Override
    void adjustTaskNode(TaskNode taskNode) {
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus) {
        super.configure(component, foreground, background, isSelected, hasFocus);
        if (panel != null) {
            if (DashboardViewer.getInstance().containsActiveTask(this)) {
                lblName.setFont(lblName.getFont().deriveFont(Font.BOLD));
            } else {
                lblName.setFont(lblName.getFont().deriveFont(Font.PLAIN));
            }
        }
    }

    @Override
    protected JComponent createComponent(List<Issue> data) {
        updateNodes();
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        synchronized (LOCK) {
            labels.clear();
            buttons.clear();

            lblName = new TreeLabel(query.getDisplayName());
            panel.add(lblName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
            labels.add(lblName);

            TreeLabel lbl = new TreeLabel("("); //NOI18N
            labels.add(lbl);
            panel.add(lbl, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0));

            btnTotal = new LinkButton(getTotalString(), new OpenQueryAction(this));
            panel.add(btnTotal, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            buttons.add(btnTotal);

            boolean showChanged = getChangedTaskCount() > 0;
            lblSeparator = new TreeLabel("|"); //NOI18N
            lblSeparator.setVisible(showChanged);
            panel.add(lblSeparator, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));
            labels.add(lblSeparator);

            btnChanged = new LinkButton(getChangedString(), new OpenQueryAction(Query.QueryMode.SHOW_NEW_OR_CHANGED, this)); //NOI18N
            btnChanged.setVisible(showChanged);
            panel.add(btnChanged, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            buttons.add(btnChanged);


            lbl = new TreeLabel(")"); //NOI18N
            labels.add(lbl);
            panel.add(lbl, new GridBagConstraints(6, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

            panel.add(new JLabel(), new GridBagConstraints(8, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        }
        return panel;
    }

    @Override
    protected Action getDefaultAction() {
        return new OpenQueryAction(this);
    }

    @Override
    public Action[] getPopupActions() {
        List<TreeListNode> selectedNodes = DashboardViewer.getInstance().getSelectedNodes();
        QueryNode[] queryNodes = new QueryNode[selectedNodes.size()];
        for (int i = 0; i < selectedNodes.size(); i++) {
            TreeListNode treeListNode = selectedNodes.get(i);
            if (treeListNode instanceof QueryNode) {
                queryNodes[i] = (QueryNode) treeListNode;
            } else {
                return null;
            }
        }
        List<Action> actions = Actions.getQueryPopupActions(queryNodes);
        return actions.toArray(new Action[actions.size()]);
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueryNode other = (QueryNode) obj;
        //TODO complete query equals method
        return query.getDisplayName().equalsIgnoreCase(other.query.getDisplayName());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.query != null ? this.query.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(QueryNode toCompare) {
        return query.getDisplayName().compareToIgnoreCase(toCompare.query.getDisplayName());
    }

    @Override
    public String toString() {
        return this.query.getDisplayName();
    }

    private class QueryListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(Query.EVENT_QUERY_ISSUES_CHANGED)) {
                updateContent();
            }
        }
    }
}
