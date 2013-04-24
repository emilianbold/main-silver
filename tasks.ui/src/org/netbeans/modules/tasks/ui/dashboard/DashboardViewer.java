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
package org.netbeans.modules.tasks.ui.dashboard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.team.ui.util.treelist.LinkButton;
import org.netbeans.modules.tasks.ui.actions.Actions;
import org.netbeans.modules.tasks.ui.actions.Actions.CreateCategoryAction;
import org.netbeans.modules.tasks.ui.actions.Actions.CreateRepositoryAction;
import org.netbeans.modules.tasks.ui.cache.CategoryEntry;
import org.netbeans.modules.tasks.ui.cache.DashboardStorage;
import org.netbeans.modules.tasks.ui.cache.TaskEntry;
import org.netbeans.modules.tasks.ui.DashboardTransferHandler;
import org.netbeans.modules.tasks.ui.filter.AppliedFilters;
import org.netbeans.modules.tasks.ui.filter.DashboardFilter;
import org.netbeans.modules.tasks.ui.Category;
import org.netbeans.modules.tasks.ui.settings.DashboardSettings;
import org.netbeans.modules.tasks.ui.DashboardRefresher;
import org.netbeans.modules.tasks.ui.Utils;
import org.netbeans.modules.team.ui.util.treelist.ColorManager;
import org.netbeans.modules.team.ui.util.treelist.TreeList;
import org.netbeans.modules.team.ui.util.treelist.TreeListModel;
import org.netbeans.modules.team.ui.util.treelist.TreeListModelListener;
import org.netbeans.modules.team.ui.util.treelist.TreeListNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Singleton providing access to Tasks window.
 *
 * @author S. Aubrecht
 * @author J. Peska
 */
public final class DashboardViewer implements PropertyChangeListener {

    public static final String PREF_ALL_PROJECTS = "allProjects"; //NOI18N
    public static final String PREF_COUNT = "count"; //NOI18N
    public static final String PREF_ID = "id"; //NOI18N
    private final TreeListModel model = new TreeListModel();
    private static final ListModel EMPTY_MODEL = new AbstractListModel() {
        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public Object getElementAt(int index) {

            return null;
        }
    };
    private final RequestProcessor requestProcessor = new RequestProcessor("Dashboard"); // NOI18N
    private final TreeList treeList = new TreeList(model);
    public final JScrollPane dashboardComponent;
    private boolean opened = false;
    private final TitleNode titleCategoryNode;
    private final TitleNode titleRepositoryNode;
    private final ErrorNode errorRepositories;
    private final ErrorNode errorCategories;
    private final Object LOCK_CATEGORIES = new Object();
    private final Object LOCK_REPOSITORIES = new Object();
    private Map<Category, CategoryNode> mapCategoryToNode;
    private List<CategoryNode> categoryNodes;
    private List<RepositoryNode> repositoryNodes;
    private AppliedFilters<Issue> appliedTaskFilters;
    private AppliedFilters<CategoryNode> appliedCategoryFilters;
    private AppliedFilters<RepositoryNode> appliedRepositoryFilters;
    private int taskHits;
    private Set<TreeListNode> expandedNodes;
    private boolean persistExpanded = true;
    private TreeListNode activeTaskNode;
    static final Logger LOG = Logger.getLogger(DashboardViewer.class.getName());
    private ModelListener modelListener;

    private DashboardViewer() {
        expandedNodes = new HashSet<TreeListNode>();
        dashboardComponent = new JScrollPane() {
            @Override
            public void requestFocus() {
                Component view = getViewport().getView();
                if (view != null) {
                    view.requestFocus();
                } else {
                    super.requestFocus();
                }
            }

            @Override
            public boolean requestFocusInWindow() {
                Component view = getViewport().getView();
                return view != null ? view.requestFocusInWindow() : super.requestFocusInWindow();
            }
        };
        dashboardComponent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        dashboardComponent.setBorder(BorderFactory.createEmptyBorder());
        dashboardComponent.setBackground(ColorManager.getDefault().getDefaultBackground());
        dashboardComponent.getViewport().setBackground(ColorManager.getDefault().getDefaultBackground());
        mapCategoryToNode = new HashMap<Category, CategoryNode>();
        categoryNodes = new ArrayList<CategoryNode>();
        repositoryNodes = new ArrayList<RepositoryNode>();

        LinkButton btnAddCategory = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/add_category.png", true), new CreateCategoryAction()); //NOI18N
        btnAddCategory.setToolTipText(NbBundle.getMessage(DashboardViewer.class, "LBL_CreateCategory")); // NOI18N
        LinkButton btnClearCategories = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/clear.png", true), new Actions.ClearCategoriesAction());
        btnClearCategories.setToolTipText(NbBundle.getMessage(DashboardViewer.class, "LBL_ClearCategories")); // NOI18N
        titleCategoryNode = new TitleNode(NbBundle.getMessage(TitleNode.class, "LBL_Categories"), btnAddCategory, btnClearCategories); // NOI18N

        LinkButton btnAddRepo = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/add_repo.png", true), new CreateRepositoryAction()); //NOI18N
        btnAddRepo.setToolTipText(NbBundle.getMessage(DashboardViewer.class, "LBL_AddRepo")); // NOI18N
        titleRepositoryNode = new TitleNode(NbBundle.getMessage(TitleNode.class, "LBL_Repositories"), btnAddRepo); // NOI18N

        AbstractAction reloadAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData();
            }
        };
        errorRepositories = new ErrorNode(NbBundle.getMessage(TitleNode.class, "ERR_Repositories"), reloadAction); // NOI18N
        errorCategories = new ErrorNode(NbBundle.getMessage(TitleNode.class, "ERR_Categories"), reloadAction); // NOI18N

        modelListener = new ModelListener();
        model.addModelListener(modelListener);
        model.addRoot(-1, titleCategoryNode);
        model.addRoot(-1, titleRepositoryNode);

        AccessibleContext accessibleContext = treeList.getAccessibleContext();
        String a11y = NbBundle.getMessage(DashboardViewer.class, "A11Y_TeamProjects"); //NOI18N
        accessibleContext.setAccessibleName(a11y);
        accessibleContext.setAccessibleDescription(a11y);
        appliedTaskFilters = new AppliedFilters<Issue>();
        appliedCategoryFilters = new AppliedFilters<CategoryNode>();
        appliedRepositoryFilters = new AppliedFilters<RepositoryNode>();
        taskHits = 0;
        treeList.setTransferHandler(new DashboardTransferHandler());
        treeList.setDragEnabled(true);
        treeList.setDropMode(DropMode.ON_OR_INSERT);
        treeList.setModel(model);
        attachActions();
        dashboardComponent.setViewportView(treeList);
        dashboardComponent.invalidate();
        dashboardComponent.revalidate();
        dashboardComponent.repaint();
    }

    /**
     * currently visible dashboard instance
     *
     * @return
     */
    public static DashboardViewer getInstance() {
        return Holder.theInstance;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RepositoryManager.EVENT_REPOSITORIES_CHANGED)) {
            requestProcessor.post(new Runnable() {
                @Override
                public void run() {
                    titleRepositoryNode.setProgressVisible(true);
                    Collection<Repository> addedRepositories = (Collection<Repository>) evt.getNewValue();
                    Collection<Repository> removedRepositories = (Collection<Repository>) evt.getOldValue();
                    if (addedRepositories == null && removedRepositories == null) {
                        updateRepositories(RepositoryManager.getInstance().getRepositories());
                    } else {
                        updateRepositories(addedRepositories, removedRepositories);
                    }
                    titleRepositoryNode.setProgressVisible(false);
                }
            });
        } else if (evt.getPropertyName().equals(DashboardSettings.TASKS_LIMIT_SETTINGS_CHANGED)) {
            requestProcessor.post(new Runnable() {
                @Override
                public void run() {
                    updateContent();
                }
            });
        } else if (evt.getPropertyName().equals(DashboardSettings.AUTO_SYNC_SETTINGS_CHANGED)) {
            DashboardRefresher.getInstance().setupDashboardRefresh();
        }
    }

    void setSelection(Collection<TaskNode> toSelect) {
        for (TaskNode taskNode : toSelect) {
            TreeListNode parent = taskNode.getParent();
            if (!parent.isExpanded()) {
                parent.setExpanded(true);
            }
            treeList.setSelectedValue(taskNode, true);
        }
    }

    private static class Holder {

        private static final DashboardViewer theInstance = new DashboardViewer();
    }

    public void addDashboardSelectionListener(ListSelectionListener listener) {
        treeList.addListSelectionListener(listener);
    }

    public void removeDashboardSelectionListener(ListSelectionListener listener) {
        treeList.removeListSelectionListener(listener);
    }

    public void addModelListener(TreeListModelListener listener) {
        model.addModelListener(listener);
    }

    public void removeModelListener(TreeListModelListener listener) {
        model.removeModelListener(listener);
    }

    public void setActiveTaskNode(TreeListNode activeTaskNode) {
        this.activeTaskNode = activeTaskNode;
    }

    public boolean containsActiveTask(TreeListNode parent) {
        if (activeTaskNode == null) {
            return false;
        }
        TreeListNode activeParent = activeTaskNode.getParent();
        while (activeParent != null) {
            if (parent.equals(activeParent)) {
                return true;
            }
            activeParent = activeParent.getParent();
        }
        return false;
    }

    public boolean isTaskNodeActive(TaskNode taskNode) {
        return taskNode.equals(activeTaskNode);
    }

    boolean isOpened() {
        return opened;
    }

    public void close() {
        synchronized (LOCK_CATEGORIES) {
            treeList.setModel(EMPTY_MODEL);
            model.clear();
            opened = false;
        }
    }

    public JComponent getComponent() {
        opened = true;
        return dashboardComponent;
    }

    public void addTaskToCategory(Category category, TaskNode... taskNodes) {
        ArrayList<TaskNode> toSelect = new ArrayList<TaskNode>();
        CategoryNode destCategoryNode = mapCategoryToNode.get(category);
        for (TaskNode taskNode : taskNodes) {
            TaskNode categorizedTaskNode = getCategorizedTask(taskNode);
            //task is already categorized (task exists within categories)
            if (categorizedTaskNode != null) {
                //task is already in this category, do nothing
                if (category.equals(categorizedTaskNode.getCategory())) {
                    return;
                }
                //task is already in another category, dont add new taskNode but move existing one
                taskNode = categorizedTaskNode;
            }
            final boolean isCatInFilter = isCategoryInFilter(destCategoryNode);
            final boolean isTaskInFilter = appliedTaskFilters.isInFilter(taskNode.getTask());
            TaskNode toAdd = new TaskNode(taskNode.getTask(), destCategoryNode);
            if (destCategoryNode.addTaskNode(toAdd, isTaskInFilter)) {
                //remove from old category
                if (taskNode.isCategorized()) {
                    removeTask(taskNode);
                }
                //set new category
                toAdd.setCategory(category);
                if (DashboardViewer.getInstance().isTaskNodeActive(taskNode)) {
                    DashboardViewer.getInstance().setActiveTaskNode(toAdd);
                }
                toSelect.add(taskNode);
            }
            if (isTaskInFilter && !isCatInFilter) {
                addCategoryToModel(destCategoryNode);
            }
        }
        destCategoryNode.updateContentAndSelect(toSelect);
        storeCategory(category);
    }

    public void removeTask(TaskNode... taskNodes) {
        Map<Category, List<TaskNode>> map = new HashMap<Category, List<TaskNode>>();
        for (TaskNode taskNode : taskNodes) {
            List<TaskNode> tasks = map.get(taskNode.getCategory());
            if (tasks == null) {
                tasks = new ArrayList<TaskNode>();
            }
            tasks.add(taskNode);
            map.put(taskNode.getCategory(), tasks);
        }
        for (Entry<Category, List<TaskNode>> entry : map.entrySet()) {
            Category category = entry.getKey();
            CategoryNode categoryNode = mapCategoryToNode.get(category);
            final boolean isOldInFilter = isCategoryInFilter(categoryNode);

            List<TaskNode> tasks = entry.getValue();
            for (TaskNode taskNode : tasks) {
                taskNode.setCategory(null);
                categoryNode.removeTaskNode(taskNode);
            }
            model.contentChanged(categoryNode);
            if (!isCategoryInFilter(categoryNode) && isOldInFilter) {
                model.removeRoot(categoryNode);
            } else {
                //TODO only remove that child, dont updateContent all
                categoryNode.updateContent();
            }
            storeCategory(categoryNode.getCategory());
        }
    }

    public List<Category> getCategories(boolean openedOnly) {
        synchronized (LOCK_CATEGORIES) {
            List<Category> list = new ArrayList<Category>(categoryNodes.size());
            for (CategoryNode categoryNode : categoryNodes) {
                if (!(openedOnly && !categoryNode.isOpened())) {
                    list.add(categoryNode.getCategory());
                }
            }
            return list;
        }
    }

    public boolean isCategoryNameUnique(String categoryName) {
        synchronized (LOCK_CATEGORIES) {
            for (CategoryNode node : categoryNodes) {
                if (node.getCategory().getName().equalsIgnoreCase(categoryName)) {
                    return false;
                }
            }
            return true;
        }
    }

    public void renameCategory(Category category, final String newName) {
        CategoryNode node = mapCategoryToNode.get(category);
        final String oldName = category.getName();
        category.setName(newName);
        model.contentChanged(node);
        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                DashboardStorage.getInstance().renameCategory(oldName, newName);
            }
        });
    }

    public void addCategory(Category category) {
        synchronized (LOCK_CATEGORIES) {
            //add category to the model - sorted
            CategoryNode newCategoryNode = new CategoryNode(category, false);
            categoryNodes.add(newCategoryNode);
            mapCategoryToNode.put(category, newCategoryNode);
            addCategoryToModel(newCategoryNode);
            storeCategory(category);
        }
    }

    public void deleteCategory(final CategoryNode... toDelete) {
        String names;
        if (toDelete.length == 1) {
            names = toDelete[0].getCategory().getName();
        } else {
            names = toDelete.length + " " + NbBundle.getMessage(DashboardViewer.class, "LBL_Categories").toLowerCase();
        }
        String title = NbBundle.getMessage(DashboardViewer.class, "LBL_DeleteCatTitle");
        String message = NbBundle.getMessage(DashboardViewer.class, "LBL_DeleteQuestion", names);
        if (confirmDelete(title, message)) {
            synchronized (LOCK_CATEGORIES) {
                for (CategoryNode categoryNode : toDelete) {
                    model.removeRoot(categoryNode);
                    categoryNodes.remove(categoryNode);
                }
            }
            requestProcessor.post(new Runnable() {
                @Override
                public void run() {
                    String[] names = new String[toDelete.length];
                    for (int i = 0; i < names.length; i++) {
                        names[i] = toDelete[i].getCategory().getName();
                    }
                    DashboardStorage.getInstance().deleteCategories(names);
                }
            });
        }
    }

    public void setCategoryOpened(CategoryNode categoryNode, boolean opened) {
        synchronized (LOCK_CATEGORIES) {
            categoryNodes.remove(categoryNode);
            if (isCategoryInFilter(categoryNode)) {
                model.removeRoot(categoryNode);
            }
            Category category = categoryNode.getCategory();
            final CategoryNode newNode;
            if (opened) {
                newNode = new CategoryNode(category, true);
            } else {
                newNode = new ClosedCategoryNode(category);
            }
            categoryNodes.add(newNode);
            mapCategoryToNode.put(category, newNode);
            if (isCategoryInFilter(newNode)) {
                addCategoryToModel(newNode);
            }
            storeClosedCategories();
        }
    }

    private void addCategoryToModel(final CategoryNode categoryNode) {
        int index = model.getRootNodes().indexOf(titleCategoryNode) + 1;
        int size = model.getRootNodes().size();
        boolean added = false;
        for (; index < size; index++) {
            TreeListNode node = model.getRootNodes().get(index);
            if (node instanceof CategoryNode) {
                CategoryNode displNode = (CategoryNode) node;
                if (categoryNode.compareTo(displNode) < 0) {
                    addRootToModel(model.getRootNodes().indexOf(node), categoryNode);
                    added = true;
                    break;
                }
            } else {
                // the end of category list, add
                addRootToModel(model.getRootNodes().indexOf(node), categoryNode);
                added = true;
                break;
            }
        }
        if (!added) {
            addRootToModel(-1, categoryNode);
        }
    }

    private void storeCategory(final Category category) {
        final List<TaskEntry> taskEntries = new ArrayList<TaskEntry>(category.getTasks().size());
        for (Issue issue : category.getTasks()) {
            taskEntries.add(new TaskEntry(issue.getID(), issue.getRepository().getId()));
        }
        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                DashboardStorage.getInstance().storeCategory(category.getName(), taskEntries);
            }
        });
    }

    private void storeClosedCategories() {
        final DashboardStorage storage = DashboardStorage.getInstance();
        List<CategoryNode> closed = getClosedCategoryNodes();
        final List<String> names = new ArrayList<String>(closed.size());
        for (CategoryNode categoryNode : closed) {
            names.add(categoryNode.getCategory().getName());
        }
        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                storage.storeClosedCategories(names);
            }
        });
    }

    private List<CategoryNode> getClosedCategoryNodes() {
        synchronized (LOCK_CATEGORIES) {
            List<CategoryNode> closed = new ArrayList<CategoryNode>(categoryNodes.size());
            for (CategoryNode categoryNode : categoryNodes) {
                if (!categoryNode.isOpened()) {
                    closed.add(categoryNode);
                }
            }
            return closed;
        }
    }

    public void clearCategories() {
        NotifyDescriptor nd = new NotifyDescriptor(
                NbBundle.getMessage(DashboardViewer.class, "LBL_ClearCatQuestion"), //NOI18N
                NbBundle.getMessage(DashboardViewer.class, "LBL_ClearCatTitle"), //NOI18N
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                null,
                NotifyDescriptor.YES_OPTION);
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
            List<TaskNode> finished = new ArrayList<TaskNode>();
            for (CategoryNode categoryNode : categoryNodes) {
                if (!categoryNode.isOpened()) {
                    categoryNode = new CategoryNode(categoryNode.getCategory(), false);
                }
                for (TaskNode taskNode : categoryNode.getTaskNodes()) {
                    if (taskNode.getTask().isFinished()) {
                        finished.add(taskNode);
                    }
                }
            }
            removeTask(finished.toArray(new TaskNode[finished.size()]));
        }
    }

    public void addRepository(Repository repository) {
        synchronized (LOCK_REPOSITORIES) {
            //add repository to the model - sorted
            RepositoryNode repositoryNode = new RepositoryNode(repository);
            repositoryNodes.add(repositoryNode);
            addRepositoryToModel(repositoryNode);
        }
    }

    public void removeRepository(final RepositoryNode... toRemove) {
        String names;
        if (toRemove.length == 1) {
            names = toRemove[0].getRepository().getDisplayName();
        } else {
            names = toRemove.length + " " + NbBundle.getMessage(DashboardViewer.class, "LBL_Repositories").toLowerCase();
        }
        String title = NbBundle.getMessage(DashboardViewer.class, "LBL_RemoveRepoTitle");
        String message = NbBundle.getMessage(DashboardViewer.class, "LBL_RemoveQuestion", names);
        if (confirmDelete(title, message)) {
            for (RepositoryNode repositoryNode : toRemove) {
                synchronized (LOCK_REPOSITORIES) {
                    repositoryNodes.remove((RepositoryNode) repositoryNode);
                }
                model.removeRoot(repositoryNode);
            }
            requestProcessor.post(new Runnable() {
                @Override
                public void run() {
                    for (RepositoryNode repositoryNode : toRemove) {
                        repositoryNode.getRepository().remove();
                    }
                }
            });
        }
    }

    public void setRepositoryOpened(RepositoryNode repositoryNode, boolean opened) {
        synchronized (LOCK_REPOSITORIES) {
            repositoryNodes.remove(repositoryNode);
            if (isRepositoryInFilter(repositoryNode)) {
                model.removeRoot(repositoryNode);
            }
            Repository repository = repositoryNode.getRepository();
            final RepositoryNode newNode;
            if (opened) {
                newNode = new RepositoryNode(repository);
            } else {
                newNode = new ClosedRepositoryNode(repository);
            }
            repositoryNodes.add(newNode);
            if (isRepositoryInFilter(newNode)) {
                addRepositoryToModel(newNode);
            }
            storeClosedRepositories();
        }
    }

    public void deleteQuery(QueryNode... toDelete) {
        String names = "";
        for (int i = 0; i < toDelete.length; i++) {
            QueryNode queryNode = toDelete[i];
            names += queryNode.getQuery().getDisplayName();
            if (i != toDelete.length - 1) {
                names += ", ";
            }
        }
        String title = NbBundle.getMessage(DashboardViewer.class, "LBL_DeleteQueryTitle");
        String message = NbBundle.getMessage(DashboardViewer.class, "LBL_DeleteQuestion", names);
        if (confirmDelete(title, message)) {
            for (QueryNode queryNode : toDelete) {
                queryNode.getQuery().remove();
            }
        }
    }

    private void addRepositoryToModel(final RepositoryNode repositoryNode) {
        int index = model.getRootNodes().indexOf(titleRepositoryNode) + 1;
        int size = model.getRootNodes().size();
        boolean added = false;
        for (; index < size; index++) {
            TreeListNode node = model.getRootNodes().get(index);
            if (node instanceof RepositoryNode) {
                RepositoryNode displNode = (RepositoryNode) node;
                if (repositoryNode.compareTo(displNode) < 0) {
                    addRootToModel(model.getRootNodes().indexOf(node), repositoryNode);
                    added = true;
                    break;
                }
            } else {
                // the end of category list, add
                addRootToModel(model.getRootNodes().indexOf(node), repositoryNode);
                added = true;
                break;
            }
        }
        if (!added) {
            addRootToModel(-1, repositoryNode);
        }
    }

    private void storeClosedRepositories() {
        final DashboardStorage storage = DashboardStorage.getInstance();
        List<RepositoryNode> closed = getClosedRepositoryNodes();
        final List<String> ids = new ArrayList<String>(closed.size());
        for (RepositoryNode repositoryNode : closed) {
            ids.add(repositoryNode.getRepository().getId());
        }

        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                storage.storeClosedRepositories(ids);
            }
        });
    }

    private List<RepositoryNode> getClosedRepositoryNodes() {
        synchronized (LOCK_REPOSITORIES) {
            List<RepositoryNode> closed = new ArrayList<RepositoryNode>(repositoryNodes.size());
            for (RepositoryNode repositoryNode : repositoryNodes) {
                if (!repositoryNode.isOpened()) {
                    closed.add(repositoryNode);
                }
            }
            return closed;
        }
    }

    public AppliedFilters getAppliedTaskFilters() {
        return appliedTaskFilters;
    }

    public int updateTaskFilter(DashboardFilter<Issue> oldFilter, DashboardFilter<Issue> newFilter) {
        if (oldFilter != null) {
            appliedTaskFilters.removeFilter(oldFilter);
        }
        return applyTaskFilter(newFilter, true);
    }

    public int applyTaskFilter(DashboardFilter<Issue> taskFilter, boolean refresh) {
        appliedTaskFilters.addFilter(taskFilter);
        return manageApplyFilter(refresh);
    }

    public int removeTaskFilter(DashboardFilter<Issue> taskFilter, boolean refresh) {
        appliedTaskFilters.removeFilter(taskFilter);
        return manageRemoveFilter(refresh, !taskFilter.expandNodes());
    }

    public int applyCategoryFilter(DashboardFilter<CategoryNode> categoryFilter, boolean refresh) {
        appliedCategoryFilters.addFilter(categoryFilter);
        return manageApplyFilter(refresh);
    }

    public int removeCategoryFilter(DashboardFilter<CategoryNode> categoryFilter, boolean refresh) {
        appliedCategoryFilters.removeFilter(categoryFilter);
        return manageRemoveFilter(refresh, !categoryFilter.expandNodes());
    }

    public int applyRepositoryFilter(DashboardFilter<RepositoryNode> repositoryFilter, boolean refresh) {
        appliedRepositoryFilters.addFilter(repositoryFilter);
        return manageApplyFilter(refresh);
    }

    public int removeRepositoryFilter(DashboardFilter<RepositoryNode> repositoryFilter, boolean refresh) {
        appliedRepositoryFilters.removeFilter(repositoryFilter);
        return manageRemoveFilter(refresh, !repositoryFilter.expandNodes());
    }

    public void clearFilters() {
        appliedCategoryFilters.clear();
        appliedRepositoryFilters.clear();
        appliedTaskFilters.clear();
    }

    private int manageRemoveFilter(boolean refresh, boolean wasForceExpand) {
        if (refresh) {
            taskHits = 0;
            persistExpanded = !wasForceExpand;
            updateContent();
            persistExpanded = true;
            return taskHits;
        } else {
            return -1;
        }
    }

    private int manageApplyFilter(boolean refresh) {
        if (refresh) {
            taskHits = 0;
            updateContent();
            return taskHits;
        } else {
            return -1;
        }
    }

    public boolean expandNodes() {
        return appliedTaskFilters.expandNodes() || appliedCategoryFilters.expandNodes() || appliedRepositoryFilters.expandNodes();
    }

    public boolean showHitCount() {
        return appliedTaskFilters.showHitCount() || appliedCategoryFilters.showHitCount() || appliedRepositoryFilters.showHitCount();
    }

    public boolean isNodeExpanded(TreeListNode node) {
        if (expandNodes()) {
            return true;
        }
        return expandedNodes.contains(node);
    }

    public List<TreeListNode> getSelectedNodes() {
        List<TreeListNode> nodes = new ArrayList<TreeListNode>();
        Object[] selectedValues = treeList.getSelectedValues();
        for (Object object : selectedValues) {
            nodes.add((TreeListNode) object);
        }
        return nodes;
    }

    public void loadData() {
        removeErrorNodes();
        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                // w8 with loading to preject ot be opened
                try {
                    OpenProjects.getDefault().openProjects().get();
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                }
                titleRepositoryNode.setProgressVisible(true);
                titleCategoryNode.setProgressVisible(true);
                loadRepositories();
                titleRepositoryNode.setProgressVisible(false);
                loadCategories();
                titleCategoryNode.setProgressVisible(false);
                DashboardRefresher.getInstance().setupDashboardRefresh();
            }
        });
    }

    private void removeErrorNodes() {
        if (model.getRootNodes().contains(errorCategories)) {
            model.removeRoot(errorCategories);
        }
        if (model.getRootNodes().contains(errorRepositories)) {
            model.removeRoot(errorRepositories);
        }
    }

    public void loadCategory(Category category) {
        DashboardStorage storage = DashboardStorage.getInstance();
        List<TaskEntry> taskEntries = storage.readCategory(category.getName());
        category.setTasks(loadTasks(taskEntries));
    }

    private void loadCategories() {
        try {
            DashboardStorage storage = DashboardStorage.getInstance();
            List<CategoryEntry> categoryEntries = storage.readCategories();
            List<String> names = storage.readClosedCategories();

            final List<CategoryNode> catNodes = new ArrayList<CategoryNode>(categoryEntries.size());
            for (CategoryEntry categoryEntry : categoryEntries) {
                // was category opened
                boolean open = !names.contains(categoryEntry.getCategoryName());
                if (open) {
                    //List<Issue> tasks = loadTasks(categoryEntry.getTaskEntries());
                    catNodes.add(new CategoryNode(new Category(categoryEntry.getCategoryName()), true));
                } else {
                    catNodes.add(new ClosedCategoryNode(new Category(categoryEntry.getCategoryName())));
                }
            }
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setCategories(catNodes);
                    }
                });
            } else {
                setCategories(catNodes);
            }
        } catch (Throwable ex) {
            LOG.log(Level.WARNING, "Categories loading failed due to: {0}", ex);
            showCategoriesError();
        }
    }

    private List<Issue> loadTasks(List<TaskEntry> taskEntries) {
        List<Issue> tasks = new ArrayList<Issue>(taskEntries.size());
        Map<String, List<String>> m = new HashMap<String, List<String>>();
        for (TaskEntry taskEntry : taskEntries) {
            List<String> l = m.get(taskEntry.getRepositoryId());
            if (l == null) {
                l = new LinkedList<String>();
                m.put(taskEntry.getRepositoryId(), l);
            }
            l.add(taskEntry.getIssueId());
        }
        for (Entry<String, List<String>> e : m.entrySet()) {
            Repository repository = getRepository(e.getKey());
            if (repository != null) {
                List<String> l = e.getValue();
                Issue[] issues = repository.getIssues(l.toArray(new String[l.size()]));
                if (issues != null) {
                    tasks.addAll(Arrays.asList(issues));
                }
            }
        }
        return tasks;
    }

    private Repository getRepository(String repositoryId) {
        List<Repository> repositories = new ArrayList<Repository>(RepositoryManager.getInstance().getRepositories());
        for (Repository repository : repositories) {
            if (repository.getId().equals(repositoryId)) {
                return repository;
            }
        }
        return null;
    }

    private void updateRepositories(Collection<Repository> addedRepositories, Collection<Repository> removedRepositories) {
        synchronized (LOCK_REPOSITORIES) {
            List<RepositoryNode> toAdd = new ArrayList<RepositoryNode>();
            List<RepositoryNode> toRemove = new ArrayList<RepositoryNode>();

            if (removedRepositories != null) {
                for (RepositoryNode oldRepository : repositoryNodes) {
                    if (removedRepositories.contains(oldRepository.getRepository())) {
                        toRemove.add(oldRepository);
                    }
                }
            }
            if (addedRepositories != null) {
                List<Repository> oldValue = getRepositories(false);
                for (Repository addedRepository : addedRepositories) {
                    if (!oldValue.contains(addedRepository)) {
                        toAdd.add(createRepositoryNode(addedRepository));
                    }
                }
            }
            updateRepositories(toRemove, toAdd);
        }
    }

    private void updateRepositories(Collection<Repository> repositories) {
        synchronized (LOCK_REPOSITORIES) {
            List<RepositoryNode> toAdd = new ArrayList<RepositoryNode>();
            List<RepositoryNode> toRemove = new ArrayList<RepositoryNode>();

            for (RepositoryNode oldRepository : repositoryNodes) {
                if (!repositories.contains(oldRepository.getRepository())) {
                    toRemove.add(oldRepository);
                }
            }

            List<Repository> oldValue = getRepositories(false);
            for (Repository newRepository : repositories) {
                if (!oldValue.contains(newRepository)) {
                    toAdd.add(createRepositoryNode(newRepository));
                }
            }
            updateRepositories(toRemove, toAdd);
        }
    }

    private void updateRepositories(List<RepositoryNode> toRemove, List<RepositoryNode> toAdd) {
        synchronized (LOCK_REPOSITORIES) {
            //remove unavailable repositories from model
            repositoryNodes.removeAll(toRemove);
            for (RepositoryNode repositoryNode : toRemove) {
                model.removeRoot(repositoryNode);
            }
            //add new repositories to model
            for (RepositoryNode newRepository : toAdd) {
                repositoryNodes.add(newRepository);
                if (isRepositoryInFilter(newRepository)) {
                    addRepositoryToModel(newRepository);
                }
            }
        }
    }

    private RepositoryNode createRepositoryNode(Repository repository) {
        boolean open = Utils.isRepositoryOpened(repository.getId());
        if (open) {
            return new RepositoryNode(repository);
        } else {
            return new ClosedRepositoryNode(repository);
        }
    }

    public List<Repository> getRepositories(boolean openedOnly) {
        synchronized (LOCK_REPOSITORIES) {
            List<Repository> repositories = new ArrayList<Repository>();
            for (RepositoryNode repositoryNode : repositoryNodes) {
                if (!(openedOnly && !repositoryNode.isOpened())) {
                    repositories.add(repositoryNode.getRepository());
                }
            }
            return repositories;
        }
    }

    private void loadRepositories() {
        try {
            List<Repository> allRepositories = new ArrayList<Repository>(RepositoryManager.getInstance().getRepositories());
            final List<RepositoryNode> repoNodes = new ArrayList<RepositoryNode>(allRepositories.size());

            for (Repository repository : allRepositories) {
                boolean open = Utils.isRepositoryOpened(repository.getId());
                if (open) {
                    repoNodes.add(new RepositoryNode(repository));
                } else {
                    repoNodes.add(new ClosedRepositoryNode(repository));
                }
            }
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setRepositories(repoNodes);
                    }
                });
            } else {
                setRepositories(repoNodes);
            }
        } catch (Throwable ex) {
            LOG.log(Level.WARNING, "Repositories loading failed due to: {0}", ex);
            showRepositoriesError();
        }
    }

    private void showRepositoriesError() {
        int index = model.getRootNodes().indexOf(titleRepositoryNode) + 1;
        model.addRoot(index, errorRepositories);
    }

    private void showCategoriesError() {
        int index = model.getRootNodes().indexOf(titleCategoryNode) + 1;
        model.addRoot(index, errorCategories);
    }

    private TaskNode getCategorizedTask(TaskNode taskNode) {
        synchronized (LOCK_CATEGORIES) {
            for (CategoryNode categoryNode : categoryNodes) {
                int index = categoryNode.indexOf(taskNode.getTask());
                if (index != -1) {
                    return categoryNode.getTaskNodes().get(index);
                }
            }
            return null;
        }
    }

    private void addRootToModel(int index, TreeListNode node) {
        if (expandNodes() || expandedNodes.remove(node)) {
            node.setExpanded(true);
        }
        model.addRoot(index, node);
    }

    private void removeRootFromModel(TreeListNode node) {
        if (persistExpanded) {
            expandedNodes.remove(node);
            if (node.isExpanded() && !(node instanceof RepositoryNode)) {
                expandedNodes.add(node);
            }
        }
        model.removeRoot(node);
    }

    private void updateContent() {
        synchronized (LOCK_CATEGORIES) {
            for (CategoryNode categoryNode : categoryNodes) {
                categoryNode.initPaging();
                categoryNode.updateContent();
            }
        }
        synchronized (LOCK_REPOSITORIES) {
            for (RepositoryNode repositoryNode : repositoryNodes) {
                repositoryNode.updateContent();
            }
        }
        setRepositories(repositoryNodes);
        setCategories(categoryNodes);
    }

    private void setCategories(List<CategoryNode> catNodes) {
        synchronized (LOCK_CATEGORIES) {
            removeNodesFromModel(CategoryNode.class);
            categoryNodes = catNodes;
            mapCategoryToNode.clear();
            Collections.sort(categoryNodes);
            int index = model.getRootNodes().indexOf(titleCategoryNode) + 1;
            for (CategoryNode categoryNode : categoryNodes) {
                mapCategoryToNode.put(categoryNode.getCategory(), categoryNode);
                if (isCategoryInFilter(categoryNode)) {
                    taskHits += categoryNode.getFilteredTaskCount();
                    addRootToModel(index++, categoryNode);
                }
            }
        }
    }

    private void setRepositories(List<RepositoryNode> repoNodes) {
        synchronized (LOCK_REPOSITORIES) {
            removeNodesFromModel(RepositoryNode.class);
            repositoryNodes = repoNodes;
            Collections.sort(this.repositoryNodes);
            int index = model.getRootNodes().indexOf(titleRepositoryNode) + 1;
            for (RepositoryNode repositoryNode : repositoryNodes) {
                if (isRepositoryInFilter(repositoryNode)) {
                    taskHits += repositoryNode.getFilterHits();
                    addRootToModel(index++, repositoryNode);
                }
            }
        }
    }

    private boolean isCategoryInFilter(CategoryNode categoryNode) {
        return expandNodes() ? categoryNode.getFilteredTaskCount() > 0 && appliedCategoryFilters.isInFilter(categoryNode) : appliedCategoryFilters.isInFilter(categoryNode);
    }

    private boolean isRepositoryInFilter(RepositoryNode repositoryNode) {
        return expandNodes() ? repositoryNode.getFilteredQueryCount() > 0 && appliedRepositoryFilters.isInFilter(repositoryNode) : appliedRepositoryFilters.isInFilter(repositoryNode);
    }

    private void removeNodesFromModel(Class nodeClass) {
        ArrayList<TreeListNode> nodesToRemove = new ArrayList<TreeListNode>();
        for (TreeListNode root : model.getRootNodes()) {
            if (root != null && nodeClass.isAssignableFrom(root.getClass())) {
                nodesToRemove.add(root);
            }
        }
        for (TreeListNode node : nodesToRemove) {
            removeRootFromModel(node);
        }
    }

    private void attachActions() {
        treeList.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(Actions.REFRESH_KEY, "org.netbeans.modules.tasks.ui.action.Action.UniversalRefreshAction"); //NOI18N
        treeList.getActionMap().put("org.netbeans.modules.tasks.ui.action.Action.UniversalRefreshAction", new Actions.UniversalRefreshAction());//NOI18N

        treeList.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(Actions.DELETE_KEY, "org.netbeans.modules.tasks.ui.action.Action.UniversalDeleteAction"); //NOI18N
        treeList.getActionMap().put("org.netbeans.modules.tasks.ui.action.Action.UniversalDeleteAction", new Actions.UniversalDeleteAction());//NOI18N
    }

    private boolean confirmDelete(String title, String message) {
        NotifyDescriptor nd = new NotifyDescriptor(
                message,
                title,
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                null,
                NotifyDescriptor.YES_OPTION);
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
            return true;
        }
        return false;
    }

    private void handleSelection(TreeListNode node) {
        ListSelectionModel selectionModel = treeList.getSelectionModel();
        List<TreeListNode> children = node.getChildren();
        int childrenSize = children.size();
        removeChildrenSelection(children);
        if (!selectionModel.isSelectionEmpty()) {
            int indexOfNode = model.getAllNodes().indexOf(node);
            if (selectionModel.isSelectedIndex(indexOfNode) || selectionModel.isSelectedIndex(indexOfNode + childrenSize + 1)) {
                int minSelectionIndex = selectionModel.getMinSelectionIndex();
                int maxSelectionIndex = selectionModel.getMaxSelectionIndex();
                if (minSelectionIndex == maxSelectionIndex) {
                    selectionModel.setSelectionInterval(minSelectionIndex, maxSelectionIndex);
                } else {
                    List<Integer> selectedIndexes = new ArrayList<Integer>(maxSelectionIndex - minSelectionIndex + 1);
                    for (int i = minSelectionIndex; i <= maxSelectionIndex; i++) {
                        if (selectionModel.isSelectedIndex(i)) {
                            selectedIndexes.add(i);
                        }
                    }
                    selectionModel.clearSelection();
                    for (int index : selectedIndexes) {
                        selectionModel.addSelectionInterval(index, index);
                    }
                }
            }
        }
    }

    private void removeChildrenSelection(List<TreeListNode> children) {
        if (children.isEmpty()) {
            return;
        }
        final List<TreeListNode> allNodes = model.getAllNodes();
        int firstIndex = allNodes.indexOf(children.get(0));
        int lastIndex = allNodes.indexOf(children.get(children.size() - 1));
        treeList.getSelectionModel().removeSelectionInterval(firstIndex, lastIndex);
    }

    private class ModelListener implements TreeListModelListener {

        @Override
        public void nodeExpanded(TreeListNode node) {
            handleSelection(node);
        }
    }
}
