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
package org.netbeans.modules.tasks.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.tasks.ui.DashboardTopComponent;
import org.netbeans.modules.tasks.ui.dashboard.CategoryNode;
import org.netbeans.modules.tasks.ui.dashboard.DashboardViewer;
import org.netbeans.modules.tasks.ui.dashboard.QueryNode;
import org.netbeans.modules.tasks.ui.dashboard.RepositoryNode;
import org.netbeans.modules.tasks.ui.dashboard.TaskNode;
import org.netbeans.modules.tasks.ui.model.Category;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jpeska
 */
public class Actions {

    public static List<Action> getTaskPopupActions(TaskNode taskNode) {
        Issue task = taskNode.getTask();
        List<Action> actions = new ArrayList<Action>();
        actions.add(new OpenTaskAction(task));
        actions.add(DashboardViewer.getInstance().isTaskNodeActive(taskNode) ? new DeactivateTaskAction() : new ActivateTaskAction(taskNode));
        if (taskNode.isCategorized()) {
            actions.add(new RemoveTaskAction(taskNode));
        }
        actions.add(new SetCategoryAction(taskNode));
        actions.add(new ScheduleTaskAction(task));
        actions.add(new NotificationTaskAction(task));
        actions.add(new RefreshTaskAction(task));
        return actions;
    }

    private static class RemoveTaskAction extends AbstractAction {

        private TaskNode taskNode;

        public RemoveTaskAction(TaskNode taskNode) {
            super(NbBundle.getMessage(Actions.class, "CTL_RemoveFromCat")); //NOI18N
            this.taskNode = taskNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().removeTask(taskNode);
        }
    }

    private static class ScheduleTaskAction extends AbstractAction {

        public ScheduleTaskAction(Issue task) {
            super("Schedule"); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }

    private static class RefreshTaskAction extends AbstractAction {

        private Issue task;

        public RefreshTaskAction(Issue task) {
            super(NbBundle.getMessage(Actions.class, "CTL_Refresh")); //NOI18N
            this.task = task;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    task.refresh();
                }
            });
        }
    }

    private static class SetCategoryAction extends AbstractAction {

        private TaskNode taskNode;

        public SetCategoryAction(TaskNode taskNode) {
            super(taskNode.isCategorized() ? NbBundle.getMessage(Actions.class, "CTL_MoveTask") : NbBundle.getMessage(Actions.class, "CTL_AddToCat")); //NOI18N
            this.taskNode = taskNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().addTask(taskNode);
        }
    }

    private static class NotificationTaskAction extends AbstractAction {

        public NotificationTaskAction(Issue task) {
            super(NbBundle.getMessage(Actions.class, "CTL_Notification")); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }

    public static List<Action> getCategoryPopupActions(CategoryNode categoryNode) {
        Category category = categoryNode.getCategory();
        List<Action> actions = new ArrayList<Action>();
        
        DeleteCategoryAction deleteCategoryAction = new DeleteCategoryAction(category);
        deleteCategoryAction.setEnabled(categoryNode.isOpened());
        actions.add(deleteCategoryAction);

        RenameCategoryAction renameCategoryAction = new RenameCategoryAction(category);
        renameCategoryAction.setEnabled(categoryNode.isOpened());
        actions.add(renameCategoryAction);

        NotificationCategoryAction notificationCategoryAction = new NotificationCategoryAction(category);
        notificationCategoryAction.setEnabled(categoryNode.isOpened());
        actions.add(notificationCategoryAction);

        RefreshCategoryAction refreshCategoryAction = new RefreshCategoryAction(categoryNode);
        refreshCategoryAction.setEnabled(categoryNode.isOpened());
        actions.add(refreshCategoryAction);
        return actions;
    }

    private static class DeleteCategoryAction extends AbstractAction {

        private Category category;

        public DeleteCategoryAction(Category category) {
            super(NbBundle.getMessage(Actions.class, "CTL_Delete")); //NOI18N
            this.category = category;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().deleteCategory(category);
        }
    }

    private static class NotificationCategoryAction extends AbstractAction {

        public NotificationCategoryAction(Category category) {
            super(NbBundle.getMessage(Actions.class, "CTL_Notification")); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }

    public static class RefreshCategoryAction extends AbstractAction {

        private final CategoryNode categoryNode;

        public RefreshCategoryAction(CategoryNode categoryNode) {
            super(NbBundle.getMessage(Actions.class, "CTL_Refresh")); //NOI18N
            this.categoryNode = categoryNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            categoryNode.refreshContent();
        }
    }

    private static class RenameCategoryAction extends AbstractAction {

        private Category category;

        public RenameCategoryAction(Category category) {
            super(NbBundle.getMessage(Actions.class, "CTL_Rename")); //NOI18N
            this.category = category;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardTopComponent.findInstance().renameCategory(category);
        }
    }

    public static List<Action> getRepositoryPopupActions(RepositoryNode repositoryNode) {
        Repository repository = repositoryNode.getRepository();
        List<Action> actions = new ArrayList<Action>();
        RemoveRepositoryAction removeRepositoryAction = new RemoveRepositoryAction(repositoryNode);
        removeRepositoryAction.setEnabled(repositoryNode.isOpened());
        actions.add(removeRepositoryAction);

        RefreshRepositoryAction refreshRepositoryAction = new RefreshRepositoryAction(repositoryNode);
        refreshRepositoryAction.setEnabled(repositoryNode.isOpened());
        actions.add(refreshRepositoryAction);

        PropertiesRepositoryAction propertiesRepositoryAction = new PropertiesRepositoryAction(repository);
        propertiesRepositoryAction.setEnabled(repositoryNode.isOpened());
        actions.add(propertiesRepositoryAction);

        actions.add(null);
        CreateTaskAction createTaskAction = new CreateTaskAction(repository);
        createTaskAction.setEnabled(repositoryNode.isOpened());
        actions.add(createTaskAction);

        SearchRepositoryAction searchRepositoryAction = new SearchRepositoryAction(repository);
        searchRepositoryAction.setEnabled(repositoryNode.isOpened());
        actions.add(searchRepositoryAction);

        return actions;
    }

    private static class RemoveRepositoryAction extends AbstractAction {

        private final RepositoryNode repositoryNode;

        public RemoveRepositoryAction(RepositoryNode repositoryNode) {
            super(NbBundle.getMessage(Actions.class, "CTL_Remove")); //NOI18N
            this.repositoryNode = repositoryNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DashboardViewer.getInstance().removeRepository(repositoryNode);
        }
    }

    private static class RefreshRepositoryAction extends AbstractAction {

        public RefreshRepositoryAction(RepositoryNode repositoryNode) {
            super(NbBundle.getMessage(Actions.class, "CTL_Refresh")); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }

    private static class PropertiesRepositoryAction extends AbstractAction {

        private final Repository repository;

        public PropertiesRepositoryAction(Repository repository) {
            super(NbBundle.getMessage(Actions.class, "CTL_Properties")); //NOI18N
            this.repository = repository;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RepositoryManager.getInstance().editRepository(repository);
        }
    }

    public static List<Action> getQueryPopupActions(QueryNode queryNode) {
        Query query = queryNode.getQuery();
        List<Action> actions = new ArrayList<Action>();
        actions.add(new OpenQueryAction(query));
        //actions.add(new EditQueryAction(query));
        actions.add(new DeleteQueryAction(query));
        actions.add(new NotificationQueryAction(query));
        actions.add(new RefreshQueryAction(queryNode));
        return actions;
    }

    private static class DeleteQueryAction extends AbstractAction {

        private final Query query;

        public DeleteQueryAction(Query query) {
            super(NbBundle.getMessage(Actions.class, "CTL_Delete")); //NOI18N
            this.query = query;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    query.remove();
                }
            });
        }
    }

    private static class EditQueryAction extends AbstractAction {

        private final Query query;

        public EditQueryAction(Query query) {
            super(NbBundle.getMessage(Actions.class, "CTL_Edit")); //NOI18N
            this.query = query;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            query.open(Query.QueryMode.EDIT);
        }
    }

    private static class RefreshQueryAction extends AbstractAction {

        private QueryNode queryNode;

        public RefreshQueryAction(QueryNode queryNode) {
            super(NbBundle.getMessage(Actions.class, "CTL_Refresh")); //NOI18N
            this.queryNode = queryNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            queryNode.refreshContent();
        }
    }

    private static class NotificationQueryAction extends AbstractAction {

        public NotificationQueryAction(Query query) {
            super(NbBundle.getMessage(Actions.class, "CTL_Notification")); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new DummyAction().actionPerformed(e);
        }
    }
}
