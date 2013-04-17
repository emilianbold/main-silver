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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.core.RepositoryModel;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizeQueriesJob;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizeTasksJob;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskMigrationEvent;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.core.sync.SubmitJob;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobListener;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Vrabec
 */
public class MylynFactory {
    private static final Logger LOG = Logger.getLogger(MylynFactory.class.getName());
    private final TaskList taskList;
    private final TaskDataManager taskDataManager;
    private final TaskRepositoryManager taskRepositoryManager;
    private final RepositoryModel repositoryModel;

    MylynFactory (TaskList taskList,
            TaskDataManager taskDataManager, TaskRepositoryManager taskRepositoryManager,
            RepositoryModel repositoryModel) {
        this.taskList = taskList;
        this.taskDataManager = taskDataManager;
        this.taskRepositoryManager = taskRepositoryManager;
        this.repositoryModel = repositoryModel;
    }

    /**
     * Creates an unsubmitted task that's to be populated and submitted later.
     * The task is local until submitted and kept in the tasklist under
     * "Unsubmitted" category.
     *
     * @param taskRepository repository the task will be submitted to later.
     * @param initializingData default data (such as product/component) to
     * preset in the new task's data
     * @return the newly created task.
     * @throws CoreException tasklist or task data storage is inaccessible
     */
    @NbBundle.Messages({
        "MSG_NewTaskSummary=New Unsubmitted Task"
    })
    public ITask createTask (TaskRepository taskRepository, ITaskMapping initializingData) throws CoreException {
        // create new local task bound to the repository
        AbstractTask task = new LocalTask(String.valueOf(taskList.getNextLocalTaskId()), Bundle.MSG_NewTaskSummary());
        task.setSynchronizationState(ITask.SynchronizationState.OUTGOING_NEW);
        // maybe set on client's side
        task.setAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_CONNECTOR_KIND, taskRepository.getConnectorKind());
        task.setAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL, taskRepository.getUrl());

        // initialize task from taskdata
        AbstractRepositoryConnector repositoryConnector = taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind());
        TaskAttributeMapper attributeMapper = repositoryConnector.getTaskDataHandler().getAttributeMapper(taskRepository);
        TaskData taskData = new TaskData(attributeMapper, repositoryConnector.getConnectorKind(), taskRepository.getRepositoryUrl(), "");
        // init taskdata
        repositoryConnector.getTaskDataHandler().initializeTaskData(taskRepository, taskData, initializingData, new NullProgressMonitor());
        ITaskMapping mapping = repositoryConnector.getTaskMapping(taskData);
        String taskKind = mapping.getTaskKind();
        if (taskKind != null && taskKind.length() > 0) {
            task.setTaskKind(taskKind);
        }
        ITaskDataWorkingCopy workingCopy = taskDataManager.createWorkingCopy(task, taskData);
        workingCopy.save(null, null);
        repositoryConnector.updateNewTaskFromTaskData(taskRepository, task, taskData);
        String summary = mapping.getSummary();
        if (summary != null && summary.length() > 0) {
            task.setSummary(summary);
        }

        // sort into tasklist into the "unsubmitted" category
        taskList.addTask(task, taskList.getUnsubmittedContainer(task.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL)));
        return task;
    }
    
    /**
     * Returns a bugtracking command submitting the given task to the remote
     * repository.
     *
     * @param task task to submit
     * @param model task data to submit
     * @return the command ready to be executed
     * @throws CoreException problem while submitting
     */
    public SubmitTaskCommand createSubmitTaskCommand (final ITask task, TaskDataModel model) throws CoreException {
        final AbstractRepositoryConnector repositoryConnector;
        TaskRepository taskRepository = MylynSupport.getInstance().getTaskRepositoryFor(task);
        if (task.getSynchronizationState() == ITask.SynchronizationState.OUTGOING_NEW) {
            repositoryConnector = taskRepositoryManager.getRepositoryConnector(
                    task.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_CONNECTOR_KIND));
        } else {
            repositoryConnector = taskRepositoryManager.getRepositoryConnector(task.getConnectorKind());
        }

        SubmitTaskCommand.MylynSubmitTaskJob job = new SubmitTaskCommand.MylynSubmitTaskJob(taskDataManager,
                repositoryConnector,
                taskRepository,
                task,
                model.getTaskData(), model.getChangedOldAttributes() /*??? no idea what's this good for*/);
        job.addSubmitJobListener(new SubmitJobListener() {
            @Override
            public void taskSubmitted (SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
            }

            @Override
            public void taskSynchronized (SubmitJobEvent event, IProgressMonitor monitor) throws CoreException {
            }

            @Override
            public void done (SubmitJobEvent event) {
                // turn into full task
                SubmitJob job = event.getJob();
                ITask newTask = job.getTask();
                if (newTask != null && newTask != task) {
                    // copy anything you want
                    taskList.deleteTask(task);
                    taskList.addTask(newTask);
                    repositoryConnector.migrateTask(new TaskMigrationEvent(task, newTask));
                    try {
                        taskDataManager.deleteTaskData(task);
                    } catch (CoreException ex) {
                        LOG.log(Level.INFO, null, ex);
                    }
                }
            }
        });

        SubmitTaskCommand command = new SubmitTaskCommand(job);
        return command;
    }

    public SynchronizeTasksCommand createSynchronizeTasksCommand (TaskRepository taskRepository, Set<ITask> tasks) {
        AbstractRepositoryConnector repositoryConnector = taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind());
        SynchronizeTasksJob job = new SynchronizeTasksJob(taskList,
                taskDataManager,
                repositoryModel,
                repositoryConnector,
                taskRepository,
                tasks);
        return new SynchronizeTasksCommand(job, taskRepository, tasks);
    }

    public SynchronizeQueriesCommand createSynchronizeQueriesCommand (TaskRepository taskRepository, Set<IRepositoryQuery> queries) {
        Set<RepositoryQuery> repositoryQueries = new HashSet<RepositoryQuery>(queries.size());
        for (IRepositoryQuery q : queries) {
            assert q instanceof RepositoryQuery;
            if (q instanceof RepositoryQuery) {
                repositoryQueries.add((RepositoryQuery) q);
            }
        }
        AbstractRepositoryConnector repositoryConnector = taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind());
        SynchronizeQueriesJob job = new SynchronizeQueriesJob(taskList,
                taskDataManager,
                repositoryModel,
                repositoryConnector,
                taskRepository,
                repositoryQueries);
        return new SynchronizeQueriesCommand(job, taskRepository, repositoryQueries);
    }

    public GetRepositoryTaskCommand createGetRepositoryTaskCommand (TaskRepository taskRepository, String taskId) throws CoreException {
        AbstractRepositoryConnector repositoryConnector = taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind());
        GetRepositoryTaskCommand cmd = new GetRepositoryTaskCommand(repositoryConnector,
                taskRepository, taskId, taskDataManager);
        return cmd;
    }

    public SimpleQueryCommand createSimpleQueryCommand (TaskRepository taskRepository, IRepositoryQuery query) throws CoreException {
        AbstractRepositoryConnector repositoryConnector = taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind());
        return new SimpleQueryCommand(repositoryConnector, taskRepository, taskDataManager, query);
    }

    public IRepositoryQuery createNewQuery (TaskRepository taskRepository, String queryName) {
        IRepositoryQuery query = repositoryModel.createRepositoryQuery(taskRepository);
        assert query instanceof RepositoryQuery;
        query.setSummary(queryName);
        return query;
    }
    
}
