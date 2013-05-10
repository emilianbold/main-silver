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
package org.netbeans.modules.mylyn.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.RepositoryModel;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityManager;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.data.ITaskDataManagerListener;
import org.eclipse.mylyn.internal.tasks.core.data.SynchronizationManger;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataDiff;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataState;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataStore;
import org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizer;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryListener;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.ITaskAttributeDiff;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.openide.modules.Places;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Ondrej Vrabec
 */
public class MylynSupport {

    private static MylynSupport instance;
    private static final String BACKUP_SUFFIX = ".backup"; //NOI18N
    private final TaskRepositoryManager taskRepositoryManager;
    private final TaskRepository localTaskRepository;
    private final TaskList taskList;
    private final RepositoryModel repositoryModel;
    private final SynchronizationManger synchronizationManager;
    private final TaskDataManager taskDataManager;
    private final TaskActivityManager taskActivityManager;
    private final TaskListExternalizer taskListWriter;
    private final File taskListStorageFile;
    private boolean taskListInitialized;
    private MylynFactory factory;
    private static final Logger LOG = Logger.getLogger(MylynSupport.class.getName());
    private ITaskListChangeListener taskListListener;
    private static final String PROP_REPOSITORY_CREATION_TIME = "repository.creation.time_"; //NOI18N
    private IRepositoryListener taskRepositoryManagerListener;
    private static final String ATTR_TASK_INCOMING_NEW = "NetBeans.task.unseen"; //NOI18N
    private static final RequestProcessor RP = new RequestProcessor("MylynSupport", 1, true); //NOI18N
    private final Task saveTask;
    private boolean dirty;

    public static synchronized MylynSupport getInstance () {
        if (instance == null) {
            instance = new MylynSupport();
        }
        return instance;
    }

    private MylynSupport () {
        taskRepositoryManager = new TaskRepositoryManager();
        taskRepositoryManager.addRepositoryConnector(new LocalRepositoryConnector());
        localTaskRepository = new TaskRepository(LocalRepositoryConnector.CONNECTOR_KIND,
                LocalRepositoryConnector.REPOSITORY_URL);
        taskRepositoryManager.addRepository(localTaskRepository);
        taskList = new TaskList();

        repositoryModel = new RepositoryModel(taskList, taskRepositoryManager);
        synchronizationManager = new SynchronizationManger(repositoryModel) {
            @Override
            public synchronized boolean hasParticipants (String connectorKind) {
                return false;
            }
        };
        taskActivityManager = new TaskActivityManager(taskRepositoryManager, taskList);
        TaskDataStore taskDataStore = new TaskDataStore(taskRepositoryManager);
        taskDataManager = new TaskDataManager(taskDataStore, taskRepositoryManager, taskList,
                taskActivityManager, synchronizationManager);

        String storagePath = Places.getUserDirectory().getAbsolutePath()
                + "/var/tasks/mylyn".replace("/", File.separator); //NOI18N
        taskListStorageFile = new File(storagePath, ITasksCoreConstants.DEFAULT_TASK_LIST_FILE);
        taskDataManager.setDataPath(storagePath);
        taskListWriter = new TaskListExternalizer(repositoryModel, taskRepositoryManager);
        saveTask = RP.create(new Runnable() {
            @Override
            public void run () {
                try {
                    persist(false);
                } catch (CoreException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
            }
        });
        attachListeners();
    }

    /**
     * Returns all known tasks from the given repository.
     *
     * @param taskRepository repository tasks are stored in
     * @return tasks from the requested repository
     * @throws IOException when the tasklist is inaccessible.
     */
    public Collection<ITask> getTasks (TaskRepository taskRepository) throws CoreException {
        ensureTaskListLoaded();
        return taskList.getTasks(taskRepository.getUrl());
    }

    public Collection<ITask> getTasks (IRepositoryQuery query) throws CoreException {
        assert query instanceof RepositoryQuery;
        if (query instanceof RepositoryQuery) {
            ensureTaskListLoaded();
            return ((RepositoryQuery) query).getChildren();
        } else {
            return Collections.<ITask>emptyList();
        }
    }

    public ITask getTask (String repositoryUrl, String taskId) throws CoreException {
        ensureTaskListLoaded();
        return taskList.getTask(repositoryUrl, taskId);
    }

    public Collection<ITask> getUnsubmittedTasks (TaskRepository taskRepository) throws CoreException {
        ensureTaskListLoaded();
        return taskList.getUnsubmittedContainer(taskRepository.getUrl()).getChildren();
    }

    public TaskRepository getLocalTaskRepository () {
        return localTaskRepository;
    }

    public void save () throws CoreException {
        persist(false);
    }

    /**
     * Returns task data model for the editor page.
     *
     * @param task task to get data for
     * @return task data model the editor page should access - read and edit - 
     * or null when no data for the task found
     */
    public NetBeansTaskDataModel getTaskDataModel (ITask task)  {
        assert taskListInitialized;
        task.setAttribute(ATTR_TASK_INCOMING_NEW, null);
        TaskRepository taskRepository = getTaskRepositoryFor(task);
        try {
            ITaskDataWorkingCopy workingCopy = taskDataManager.getWorkingCopy(task);
            return new NetBeansTaskDataModel(taskRepository, task, workingCopy);
        } catch (CoreException ex) {
            LOG.log(Level.FINE, null, ex);
            return null;
        }
    }

    /**
     * Adds a listener notified when task is updated. Such listeners should be
     * notifications, task editors or task wrappers.
     *
     * @param listener
     */
    public void addTaskDataListener (ITaskDataManagerListener listener) {
        taskDataManager.addListener(listener);
    }

    public void removeTaskDataListener (ITaskDataManagerListener listener) {
        taskDataManager.removeListener(listener);
    }

    public void addRepositoryListener (IRepositoryListener listener) {
        taskRepositoryManager.addListener(listener);
    }

    /**
     * Adds listener to the tasklist. The listener will be notified on a change
     * in the tasklist, such as add or remove of items (queries, tasks) or
     * change in a task's content (summary, description, etc.).
     *
     * @param listener
     */
    public void addTaskListListener (ITaskListChangeListener listener) {
        taskList.addChangeListener(listener);
    }

    public void removeTaskListListener (ITaskListChangeListener listener) {
        taskList.removeChangeListener(listener);
    }

    // for tests only
    static synchronized void reset () {
        instance = null;
    }

    public TaskDataState getTaskDataState (ITask task) throws CoreException {
        return taskDataManager.getTaskDataState(task);
    }

    public Set<TaskAttribute> countDiff (TaskData newTaskData, TaskData oldTaskData) {
        Set<TaskAttribute> attributes = new LinkedHashSet<TaskAttribute>();
        TaskDataDiff diff = new TaskDataDiff(repositoryModel, newTaskData, oldTaskData);
        for (ITaskAttributeDiff diffAttr : diff.getChangedAttributes()) {
            attributes.add(newTaskData.getRoot().getAttribute(diffAttr.getAttributeId()));
        }
        return attributes;
    }

    public Set<IRepositoryQuery> getRepositoryQueries (TaskRepository taskRepository) throws CoreException {
        ensureTaskListLoaded();
        return new HashSet<IRepositoryQuery>(taskList.getRepositoryQueries(taskRepository.getUrl()));
    }

    public void addQuery (TaskRepository taskRepository, IRepositoryQuery query) throws CoreException {
        if (!(query instanceof RepositoryQuery)) {
            throw new IllegalArgumentException("Query must be instance of RepositoryQuery: " + query);
        }
        ensureTaskListLoaded();
        taskList.addQuery((RepositoryQuery) query);
    }

    public void discardLocalEdits (ITask task) throws CoreException {
        taskDataManager.discardEdits(task);
    }

    public IRepositoryQuery getRepositoryQuery (TaskRepository taskRepository, String queryName) throws CoreException {
        for (IRepositoryQuery q : getRepositoryQueries(taskRepository)) {
            if (queryName.equals(q.getSummary())) {
                return q;
            }
        }
        return null;
    }

    public void deleteTask (ITask task) throws CoreException {
        assert taskListInitialized;
        taskList.deleteTask(task);
    }

    public void deleteQuery (IRepositoryQuery query) {
        assert taskListInitialized;
        if (query instanceof RepositoryQuery) {
            taskList.deleteQuery((RepositoryQuery) query);
        }
    }
    
    public MylynFactory getMylynFactory () throws CoreException {
        if (factory == null) {
            ensureTaskListLoaded();
            factory = new MylynFactory(taskList, taskDataManager, taskRepositoryManager, repositoryModel);
        }
        return factory;
    }

    public void markTaskSeen (ITask task, boolean seen) {
        taskDataManager.setTaskRead(task, seen);
        task.setAttribute(ATTR_TASK_INCOMING_NEW, null);
    }

    /**
     * Returns a repository for the given connector and URL.
     * If such a repository does not yet exist, creates one and registers it in the mylyn infrastructure.
     *
     * @param repositoryConnector connector handling the given repository
     * @param repositoryUrl  task repository URL
     */
    public TaskRepository getTaskRepository (AbstractRepositoryConnector repositoryConnector, String repositoryUrl) {
        TaskRepository repository = taskRepositoryManager.getRepository(repositoryConnector.getConnectorKind(), repositoryUrl);
        if (repository == null) {
            repository = new TaskRepository(repositoryConnector.getConnectorKind(), repositoryUrl);
            addTaskRepository(repositoryConnector, repository);
        }
        return repository;
    }

    public void setRepositoryUrl (TaskRepository repository, String url) throws CoreException {
        String oldUrl = repository.getRepositoryUrl();
        if (!url.equals(oldUrl)) {
            ensureTaskListLoaded();
            for (ITask task : taskList.getAllTasks()) {
                if (url.equals(task.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL))) {
                    taskDataManager.refactorRepositoryUrl(task, task.getRepositoryUrl(), url);
                } else if (oldUrl.equals(task.getRepositoryUrl())) {
                    taskDataManager.refactorRepositoryUrl(task, url, url);
                }
            }
            taskList.refactorRepositoryUrl(oldUrl, url);
            repository.setRepositoryUrl(url);
            taskRepositoryManager.notifyRepositoryUrlChanged(repository, oldUrl);
        }
    }

    ITask getOrCreateTask (TaskRepository taskRepository, String taskId, boolean addToTaskList) throws CoreException {
        ensureTaskListLoaded();
        ITask task = taskList.getTask(taskRepository.getUrl(), taskId);
        if (task == null) {
            task = repositoryModel.createTask(taskRepository, taskId);
            ((AbstractTask) task).setSynchronizationState(ITask.SynchronizationState.INCOMING_NEW);
            if (addToTaskList) {
                // ensure the task is in the tasklist
                taskList.addTask(task);
            }
        }
        return task;
    }

    TaskRepository getTaskRepositoryFor (ITask task) {
        if (task.getSynchronizationState() == ITask.SynchronizationState.OUTGOING_NEW) {
            return taskRepositoryManager.getRepository(
                    task.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_CONNECTOR_KIND),
                    task.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL));
        } else {
            return taskRepositoryManager.getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        }
    }

    void finish () throws CoreException {
        taskList.removeChangeListener(taskListListener);
        synchronized (taskList) {
            // make sure we save all changes
            dirty = true;
        }
        persist(true);
    }

    private void addTaskRepository (AbstractRepositoryConnector repositoryConnector, TaskRepository taskRepository) {
        if (!taskRepository.getConnectorKind().equals(repositoryConnector.getConnectorKind())) {
            throw new IllegalArgumentException("The given task repository is not managed by the given repository connector");
        }
        taskRepositoryManager.addRepositoryConnector(repositoryConnector);
        taskRepositoryManager.addRepository(taskRepository);
        // assert, noone should add two repository instances with the same URL
        assert taskRepository == taskRepositoryManager.getRepository(repositoryConnector.getConnectorKind(), taskRepository.getUrl());
    }
// TODO is this required? The task may be left in the storage indefinitely.
//    /**
//     * Removes the repository, its queries and tasks permanently from the mylyn
//     * infrastructure. The action is irreversible, be careful.
//     *
//     * @param taskRepository repository to remove
//     */
//    public void deleteTaskRepository (TaskRepository taskRepository) {
//        // queries to delete
//        Set<RepositoryQuery> queries = taskList.getRepositoryQueries(taskRepository.getUrl());
//        // tasks to delete
//        Set<ITask> tasks = taskList.getTasks(taskRepository.getUrl());
//        // unsubmitted tasks to delete
//        tasks.addAll(taskList.getUnsubmittedContainer(taskRepository.getUrl()).getChildren());
//        for (RepositoryQuery query : queries) {
//            taskList.deleteQuery(query);
//        }
//        for (ITask task : tasks) {
//            taskList.deleteTask(task);
//        }
//        taskRepositoryManager.removeRepository(taskRepository);
//    }

    private synchronized void ensureTaskListLoaded () throws CoreException {
        if (!taskListInitialized) {
            taskListInitialized = true;
            if (taskListStorageFile.length() > 0) {
                try {
                    taskListWriter.readTaskList(taskList, taskListStorageFile);
                } catch (CoreException ex) {
                    LOG.log(Level.INFO, null, ex);
                    throw new CoreException(new Status(ex.getStatus().getSeverity(), ex.getStatus().getPlugin(), "Cannot deserialize tasklist"));
                }
            }
        }
    }

    synchronized void persist (final boolean removeUnseenOrphanedTasks) throws CoreException {
        if (taskListInitialized) {
            taskList.run(new ITaskListRunnable() {
                @Override
                public void execute (IProgressMonitor monitor) throws CoreException {
                    boolean save;
                    synchronized (taskList) {
                        save = dirty;
                        dirty = false;
                    }
                    if (!save) {
                        return;
                    }
                    try {
                        if (removeUnseenOrphanedTasks) {
                            Set<ITask> orphanedUnseenTasks = new HashSet<ITask>();
                            for (UnmatchedTaskContainer cont : taskList.getUnmatchedContainers()) {
                                for (ITask task : cont.getChildren()) {
                                    if (task.getSynchronizationState() == ITask.SynchronizationState.INCOMING_NEW
                                            || Boolean.TRUE.toString().equals(task.getAttribute(ATTR_TASK_INCOMING_NEW))) {
                                        orphanedUnseenTasks.add(task);
                                    }
                                }
                            }
                            for (ITask taskToDelete : orphanedUnseenTasks) {
                                deleteTask(taskToDelete);
                            }
                        }
                        taskListStorageFile.getParentFile().mkdirs();
                        backupTaskList(taskListStorageFile);
                        taskListWriter.writeTaskList(taskList, taskListStorageFile);
                    } catch (CoreException ex) {
                        LOG.log(Level.INFO, null, ex);
                        throw new CoreException(new Status(ex.getStatus().getSeverity(), ex.getStatus().getPlugin(), "Cannot persist tasklist"));
                    }
                }
            });
        }
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(MylynSupport.class);
    }

    private void attachListeners () {
        taskRepositoryManager.addListener(taskRepositoryManagerListener = new IRepositoryListener() {
            @Override
            public void repositoryAdded (TaskRepository repository) {
                getRepositoryCreationTime(repository.getRepositoryUrl());
            }

            @Override
            public void repositoryRemoved (TaskRepository repository) {
                setRepositoryCreationTime(repository.getRepositoryUrl(), -1);
            }

            @Override
            public void repositorySettingsChanged (TaskRepository repository) {
            }

            @Override
            public void repositoryUrlChanged (TaskRepository repository, String oldUrl) {
                setRepositoryCreationTime(repository.getRepositoryUrl(), getRepositoryCreationTime(oldUrl));
                setRepositoryCreationTime(oldUrl, -1);
            }
        });
        taskList.addChangeListener(taskListListener = new ITaskListChangeListener() {
            @Override
            public void containersChanged (Set<TaskContainerDelta> deltas) {
                for (TaskContainerDelta delta : deltas) {
                    if (!delta.isTransient()) {
                        synchronized (taskList) {
                            dirty = true;
                        }
                        scheduleSave();
                    }
                    if (delta.getElement() instanceof ITask) {
                        // task added to the tasklist
                        // new tasks (incoming_new) created long ago in the past
                        // should be marked as uptodate so when a repository is registener in the IDE
                        // it is not all green. Only fresh new tasks are relevant to the user
                        ITask task = (ITask) delta.getElement();
                        if (task.getSynchronizationState() == ITask.SynchronizationState.INCOMING_NEW
                                && task.getCreationDate() != null) {
                            TaskRepository repository = taskRepositoryManager.getRepository(task.getConnectorKind(), task.getRepositoryUrl());
                            if (repository != null) {
                                long time = getRepositoryCreationTime(repository.getRepositoryUrl());
                                if (task.getCreationDate().getTime() < time) {
                                    markTaskSeen(task, true);
                                    task.setAttribute(ATTR_TASK_INCOMING_NEW, Boolean.TRUE.toString());
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void setRepositoryCreationTime (String repositoryUrl, long time) {
        if (time == -1) {
            getPreferences().remove(PROP_REPOSITORY_CREATION_TIME + repositoryUrl);
        } else {
            getPreferences().putLong(PROP_REPOSITORY_CREATION_TIME + repositoryUrl, time);
        }
    }

    private long getRepositoryCreationTime (String repositoryUrl) {
        long time = getPreferences().getLong(PROP_REPOSITORY_CREATION_TIME + repositoryUrl, -1);
        if (time == -1) {
            time = System.currentTimeMillis();
            setRepositoryCreationTime(repositoryUrl, time);
        }
        return time;
    }

    private void scheduleSave () {
        saveTask.schedule(5000);
    }

    private void backupTaskList (File taskListStorageFile) {
        if (taskListStorageFile.canWrite()) {
            File backup = new File(taskListStorageFile.getParentFile(), taskListStorageFile.getName() + BACKUP_SUFFIX);
            backup.delete();
            taskListStorageFile.renameTo(backup);
        }
    }
}
