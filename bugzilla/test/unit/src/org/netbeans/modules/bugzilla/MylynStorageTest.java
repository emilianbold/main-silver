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
package org.netbeans.modules.bugzilla;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaOperation;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.core.data.ITaskDataManagerListener;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataState;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_PASSWD;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_URL;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_USER;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.mylyn.util.GetRepositoryTaskCommand;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.SimpleQueryCommand;
import org.netbeans.modules.mylyn.util.SubmitCommand;
import org.netbeans.modules.mylyn.util.SubmitTaskCommand;
import org.netbeans.modules.mylyn.util.SynchronizeQueriesCommand;
import org.netbeans.modules.mylyn.util.SynchronizeTasksCommand;
import org.openide.util.RequestProcessor;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Ondrej Vrabec
 */
public class MylynStorageTest extends NbTestCase {
    
    private static final String QUERY_NAME = "My new query";
    private BugzillaRepository br;
    private TaskRepository btr;
    private BugzillaRepositoryConnector brc;
    
    private static final String PRODUCT = "mylyn"; //NOI18N
    private static final String COMPONENT = "default"; //NOI18N
    
    public MylynStorageTest (String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", new File(getDataDir(), "userdir").getAbsolutePath());
        MockLookup.setLayersAndInstances();
        BugtrackingUtil.getBugtrackingConnectors(); // ensure conector
        
        br = TestUtil.getRepository("testbugzilla", REPO_URL, REPO_USER, REPO_PASSWD);
        btr = br.getTaskRepository();
        brc = Bugzilla.getInstance().getRepositoryConnector();
        // reset
        Method m = MylynSupport.class.getDeclaredMethod("reset", new Class[0]);
        m.setAccessible(true);
        m.invoke(MylynSupport.class);
        // register repository
        MylynSupport.getInstance().addTaskRepository(brc, btr);
    }

    @Override
    protected void tearDown () throws Exception {
        // persist for next round
        MylynSupport.getInstance().save();
        super.tearDown();
    }
    
    public static Test suite () {
        TestSuite suite = new NbTestSuite();
        // creates an offline temporary task
        suite.addTest(new MylynStorageTest("testCreateUnsubmittedTask"));
        // submit the temporary task to the server and turn it into a full remote task
        suite.addTest(new MylynStorageTest("testSubmitTemporaryTask"));
        // edit task
        suite.addTest(new MylynStorageTest("testEditTask"));
        // submit task
        suite.addTest(new MylynStorageTest("testSubmitTask"));
        // external changes
        suite.addTest(new MylynStorageTest("testIncomingChanges"));
        // external changes and refresh in editor page
        suite.addTest(new MylynStorageTest("testIncomingChangesInEditorPage"));
        // conflicts in incoming and outgoing
        suite.addTest(new MylynStorageTest("testConflicts"));
        // conflicts in incoming and outgoing in editor page
        suite.addTest(new MylynStorageTest("testConflictsInEditorPage"));
        // open task editor for unknown task
        suite.addTest(new MylynStorageTest("testOpenUnknownTask"));
        // open task editor for task with deleted/corrupted task data in storage
        suite.addTest(new MylynStorageTest("testOpenTaskWithDeletedData"));
        
        // create and init query
        suite.addTest(new MylynStorageTest("testCreateQuery"));
        // synchronize and get external changes
        suite.addTest(new MylynStorageTest("testSynchronizeQuery"));
        // modify query
        suite.addTest(new MylynStorageTest("testModifyQuery"));
        // remove from query internal - closing a task
        suite.addTest(new MylynStorageTest("testTaskRemovedFromQueryInt"));
        // remove from query externally - closing a task
        suite.addTest(new MylynStorageTest("testTaskRemovedFromQueryExt"));
        // test simple search - temporary query not added to the tasklist
        suite.addTest(new MylynStorageTest("testSimpleSearch"));
        return suite;
    }
    
    public void testCreateUnsubmittedTask () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        ITaskMapping mapping = new TaskMapping() {

            @Override
            public String getProduct () {
                return PRODUCT;
            }

            @Override
            public String getComponent () {
                return COMPONENT;
            }
            
        };
        ITask task = supp.getMylynFactory().createTask(btr, mapping);
        Collection<ITask> allLocalTasks = supp.getTasks(supp.getLocalTaskRepository());
        Collection<ITask> allUnsubmittedTasks = supp.getUnsubmittedTasks(btr);

        /*************** TEST *******************/
        // is it really in the tasklist
        assertEquals(1, allLocalTasks.size());
        assertTrue(allLocalTasks.contains(task));
        assertEquals(1, allUnsubmittedTasks.size());
        assertTrue(allUnsubmittedTasks.contains(task));
        assertEquals(0, supp.getTasks(btr).size()); // not yet in the repository
        assertEquals("1", task.getTaskId());
    }
    
    public void testSubmitTemporaryTask () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        ITask task = supp.getUnsubmittedTasks(btr).iterator().next();
        // edit the task
        TaskDataModel model = supp.getTaskDataModel(task);
        
        // model.getTaskData returns our local data
        String defaultSummary = task.getSummary();
        TaskAttribute rta = model.getTaskData().getRoot();
        assertFalse(model.isDirty());
        // now edit summary, product and component
        String newSummary = "Task summary testSubmitTemporaryTask";
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(newSummary);
        model.attributeChanged(ta);
        
        // now we have unsaved changes, the task is dirty
        assertTrue(model.isDirty());
        // not yet saved
        assertEquals(defaultSummary, task.getSummary());
        // save
        model.save(new NullProgressMonitor());
        // all saved?
        assertFalse(model.isDirty());
        // well, not exactly, for new unsubmitted task we need to manually refresh task's attributes
        assertEquals(defaultSummary, task.getSummary());
        if (task.getSynchronizationState() == ITask.SynchronizationState.OUTGOING_NEW) {
            task.setSummary(newSummary);
        }
        assertEquals(newSummary, task.getSummary());
        
        // let's submit finally
        ITask submittedTask = submitTask(task, model);
        
        assertNotSame(task, submittedTask); // they difer, the new task is a persistent, not local one
        assertEquals(0, supp.getUnsubmittedTasks(btr).size());
        assertSame(submittedTask, supp.getTask(btr.getUrl(), submittedTask.getTaskId()));
        
        assertEquals(newSummary, task.getSummary());
        model = supp.getTaskDataModel(submittedTask);
        assertSame(btr, model.getTaskRepository());
        assertSame(submittedTask, model.getTask());
        assertFalse(model.isDirty());
        assertTrue(model.hasBeenRead());
        assertTrue(model.getChangedAttributes().isEmpty());
        assertTrue(model.getChangedOldAttributes().isEmpty());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
    }
    
    public void testEditTask () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Collection<ITask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        ITask task = tasks.iterator().next();
        assertNotNull(task);
        
        // the task should be clean, synchronized and without any modifications
        TaskDataModel model = supp.getTaskDataModel(task);
        assertFalse(model.isDirty());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        // edit
        TaskAttribute rta = model.getTaskData().getRoot();
        String oldSummary = task.getSummary();
        String newSummary = getName() + "_" + task.getTaskId();
        // change the task summary
        TaskAttribute summaryAttr = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        summaryAttr.setValue(newSummary);
        model.attributeChanged(summaryAttr);
        // task is dirty - not saved and has modifications
        assertTrue(model.isDirty());
        assertEquals(1, model.getChangedAttributes().size());
        for (TaskAttribute attr : model.getChangedAttributes()) {
            // unsaved modifications
            assertEquals(newSummary, attr.getValue());
            // no outgoing until save
            assertFalse(model.hasOutgoingChanges(attr));
        }
        
        // save
        model.save(new NullProgressMonitor());
        // task is clean (saved) - and has modifications
        assertFalse(model.isDirty());
        assertEquals(1, model.getChangedAttributes().size());
        for (TaskAttribute attr : model.getChangedAttributes()) {
            // unsaved modifications
            assertEquals(newSummary, attr.getValue());
            // no outgoing until save
            assertTrue(model.hasOutgoingChanges(attr));
        }
        assertEquals(ITask.SynchronizationState.OUTGOING, task.getSynchronizationState());
    }
    
    public void testSubmitTask () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Collection<ITask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        ITask task = tasks.iterator().next();
        assertNotNull(task);
        
        // outgoing unsubmitted changes
        assertEquals(ITask.SynchronizationState.OUTGOING, task.getSynchronizationState());
        TaskDataModel model = supp.getTaskDataModel(task);
        String oldSummary = task.getSummary();
        TaskAttribute summaryAttr = model.getTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY);
        String newSummary = summaryAttr.getValue();
        assertTrue(model.hasOutgoingChanges(summaryAttr));
        assertFalse(oldSummary.equals(newSummary));
        
        // submit
        task = submitTask(task, model);
        
        // test
        assertFalse(model.isDirty());
        assertTrue(model.getChangedAttributes().isEmpty());
        assertTrue(model.getChangedOldAttributes().isEmpty());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        assertEquals(newSummary, task.getSummary());
    }
    
    public void testIncomingChanges () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Collection<ITask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        ITask task = tasks.iterator().next();
        assertNotNull(task);
        
        DummyTaskWrapper wrapper = new DummyTaskWrapper(task);
        String oldSummary = task.getSummary();
        String newSummary = getName() + "_" + task.getTaskId();
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        makeExternalChange(task, newSummary);
        
        // still no change, need to do a sync job
        assertEquals(oldSummary, wrapper.getSummary());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        
        
        // sync with server
        SynchronizeTasksCommand cmd = supp.getMylynFactory().createSynchronizeTasksCommand(btr, Collections.<ITask>singleton(task));
        br.getExecutor().execute(cmd);
        assertEquals(ITask.SynchronizationState.INCOMING, task.getSynchronizationState());
        assertEquals(ITask.SynchronizationState.INCOMING, wrapper.getSynchronizationState());
        assertEquals(newSummary, wrapper.getSummary());
        assertEquals("Summary from " + oldSummary + " to " + newSummary, wrapper.getIncomingChangesText());
        wrapper.forget();
    }
    
    public void testIncomingChangesInEditorPage () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Collection<ITask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        ITask task = tasks.iterator().next();
        assertNotNull(task);
        
        String oldSummary = task.getSummary();
        String newSummary = getName() + "_" + task.getTaskId();
        DummyTaskWrapper wrapper = new DummyTaskWrapper(task);
        assertEquals(ITask.SynchronizationState.INCOMING, wrapper.getSynchronizationState());
        DummyEditorPage page = new DummyEditorPage(task);
        page.open();
        page.assertOpened();
        assertEquals(oldSummary, page.taskDataSummary);
        assertTrue(page.summaryChanged);
        page.clear();
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        makeExternalChange(task, newSummary);
        
        // still no change, need to do a sync job
        assertEquals(oldSummary, wrapper.getSummary());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        
        
        // sync with server
        SynchronizeTasksCommand cmd = supp.getMylynFactory().createSynchronizeTasksCommand(btr, Collections.<ITask>singleton(task));
        br.getExecutor().execute(cmd);
        // synchronized because it's refreshed in the editor page automatically
        assertTrue(page.summaryChanged);
        assertEquals(newSummary, page.taskDataSummary);
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        assertEquals(newSummary, wrapper.getSummary());
        assertEquals("", wrapper.getIncomingChangesText());
        
        page.close();
        wrapper.forget();
    }
    
    public void testConflicts () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Collection<ITask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        ITask task = tasks.iterator().next();
        assertNotNull(task);
        
        DummyTaskWrapper wrapper = new DummyTaskWrapper(task);
        String oldSummary = task.getSummary();
        String newSummary = getName() + "_" + task.getTaskId();
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        makeExternalChange(task, newSummary);
        
        // still no change, need to do a sync job
        assertEquals(oldSummary, wrapper.getSummary());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        
        // make local changes
        DummyEditorPage page = new DummyEditorPage(task);
        page.open();
        page.assertOpened();
        page.changeSummary(newSummary + "_local");
        // save
        page.close();
        
        // still no change, need to do a sync job
        assertEquals(ITask.SynchronizationState.OUTGOING, wrapper.getSynchronizationState());
        
        // sync with server
        SynchronizeTasksCommand cmd = supp.getMylynFactory().createSynchronizeTasksCommand(btr, Collections.<ITask>singleton(task));
        br.getExecutor().execute(cmd);
        assertEquals(ITask.SynchronizationState.CONFLICT, wrapper.getSynchronizationState());
        assertEquals(newSummary, wrapper.getSummary());
        assertEquals("Summary from " + oldSummary + " to " + newSummary, wrapper.getIncomingChangesText());
        
        // open editor and clear 
        page.open();
        assertEquals(ITask.SynchronizationState.OUTGOING, wrapper.getSynchronizationState());
        assertEquals("", wrapper.getIncomingChangesText());
        assertTrue(page.summaryChangedLocally);
        
        // revert to synchronized
        page.revert();
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        
        wrapper.forget();
    }
    
    public void testConflictsInEditorPage () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Collection<ITask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        ITask task = tasks.iterator().next();
        assertNotNull(task);
        
        DummyTaskWrapper wrapper = new DummyTaskWrapper(task);
        String oldSummary = task.getSummary();
        String newSummary = getName() + "_" + task.getTaskId();
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        makeExternalChange(task, newSummary);
        
        // still no change, need to do a sync job
        assertEquals(oldSummary, wrapper.getSummary());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        
        // make local changes
        DummyEditorPage page = new DummyEditorPage(task);
        page.open();
        page.assertOpened();
        page.changeSummary(newSummary + "_local");
        // save
        page.save();
        
        // still no change, need to do a sync job
        assertEquals(ITask.SynchronizationState.OUTGOING, wrapper.getSynchronizationState());
        
        // sync with server
        SynchronizeTasksCommand cmd = supp.getMylynFactory().createSynchronizeTasksCommand(btr, Collections.<ITask>singleton(task));
        br.getExecutor().execute(cmd);
        // not in conflict because it's refreshed in the editor page automatically
        assertTrue(page.summaryChangedLocally);
        assertEquals(newSummary + "_local", page.taskDataSummary);
        assertEquals(ITask.SynchronizationState.OUTGOING, wrapper.getSynchronizationState());
        assertEquals(newSummary, wrapper.getSummary());
        assertEquals("", wrapper.getIncomingChangesText());
        
        // revert to synchronized
        page.revert();
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        assertEquals(newSummary, page.taskDataSummary);
        assertEquals(newSummary, wrapper.getSummary());
        assertEquals("", wrapper.getIncomingChangesText());
        
        page.close();
        wrapper.forget();
    }
    
    public void testOpenUnknownTask () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        
        String taskId = "1";
        Collection<ITask> tasks = supp.getTasks(btr);
        for (ITask t : tasks) {
            assertFalse(taskId.equals(t.getTaskId()));
        }
        
        DummyEditorPage page = new DummyEditorPage(taskId);
        page.open();
        page.waitUntilOpened();
        page.assertOpened();
        
        page.close();
        
        assertTrue(supp.getTasks(btr).contains(page.task));
        supp.deleteTask(btr, page.task);
        assertFalse(supp.getTasks(btr).contains(page.task));
    }
    
    public void testOpenTaskWithDeletedData () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        
        Collection<ITask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        ITask task = tasks.iterator().next();
        assertNotNull(task);
        // delete task data
        assertNotNull(supp.getTaskDataState(task));
        deleteTaskData(task);
        assertNull(supp.getTaskDataState(task));
        
        DummyEditorPage page = new DummyEditorPage(task);
        page.open();
        page.waitUntilOpened();
        page.assertOpened();
        assertEquals(task, page.task);
        
        page.close();
    }
    
    public void testCreateQuery () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        
        Collection<ITask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        // task already known, will be up to date
        ITask task = tasks.iterator().next();
        assertNotNull(task);
                
        // query list is empty
        assertEquals(0, supp.getRepositoryQueries(btr).size());
        // create new query
        final IRepositoryQuery query = supp.getMylynFactory().createNewQuery(btr, QUERY_NAME);
        query.setUrl("/buglist.cgi?query_format=advanced&product=" + PRODUCT + "&component=" + COMPONENT);
        final boolean[] event = new boolean[1];
        ITaskListChangeListener tlList = new ITaskListChangeListener() {
            @Override
            public void containersChanged (Set<TaskContainerDelta> containers) {
                for (TaskContainerDelta d : containers) {
                    if (d.getKind() == TaskContainerDelta.Kind.ADDED && d.getElement() == query) {
                        event[0] = true;
                    }
                }
            }
        };
        // add the query
        supp.addTaskListListener(tlList);
        supp.addQuery(btr, query);
        supp.removeTaskListListener(tlList);
        // was it added?
        assertTrue(event[0]);
        assertSame(query, supp.getRepositoryQueries(btr).iterator().next());
        
        // it's still empty, need to sync first
        assertEquals(0, supp.getTasks(query).size());
        
        DummyQueryController controller = new DummyQueryController(query);
        // synchronize
        SynchronizeQueriesCommand cmd = supp.getMylynFactory().createSynchronizeQueriesCommand(btr, Collections.<IRepositoryQuery>singleton(query));
        br.getExecutor().execute(cmd);
        
        // all pages should be opened
        controller.closeAllPages();
        // get all tasks for the query
        tasks = controller.getTasks();
        assertEquals(new HashSet<ITask>(supp.getTasks(query)), new HashSet<ITask>(tasks));
        assertTrue(tasks.contains(task));
        
        // all tasks are NEW - except for the known old task
        for (ITask t : tasks) {
            if (t == task || controller.getOpenedTasks().contains(t)) {
                assertEquals(ITask.SynchronizationState.SYNCHRONIZED, t.getSynchronizationState());
            } else {
                assertEquals(ITask.SynchronizationState.INCOMING_NEW, t.getSynchronizationState());
                DummyEditorPage p = new DummyEditorPage(t);
                p.open();
                p.assertOpened();
                p.close();
                assertEquals(ITask.SynchronizationState.SYNCHRONIZED, t.getSynchronizationState());
            }
        }
        controller.forget();
    }
    
    public void testSynchronizeQuery () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        IRepositoryQuery q = supp.getRepositoryQuery(btr, QUERY_NAME);
        Collection<ITask> tasks = supp.getTasks(q);
        Set<DummyTaskWrapper> wrappers = new HashSet<DummyTaskWrapper>(tasks.size());
        
        // get tasks from the query
        assertFalse(tasks.isEmpty());
        
        // make external changes in summaries
        Map<ITask, String> oldSummaries = new HashMap<ITask, String>(tasks.size());
        Map<ITask, String> newSummaries = new HashMap<ITask, String>(tasks.size());
        int i = 0;
        for (ITask task : tasks) {
            // make at most 10 changes so this ends sometimes
            if (++i > 10) {
                break;
            }
            wrappers.add(new DummyTaskWrapper(task));
            assertEquals(ITask.SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
            String newSummary = getName() + "_" + task.getTaskId() + "_" + System.currentTimeMillis();
            oldSummaries.put(task, task.getSummary());
            newSummaries.put(task, newSummary);
            makeExternalChange(task, newSummary);
        }
        
        // no change yet
        for (DummyTaskWrapper wrapper : wrappers) {
            assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
            assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.task.getSynchronizationState());
        }
        SynchronizeQueriesCommand cmd = supp.getMylynFactory().createSynchronizeQueriesCommand(btr, Collections.<IRepositoryQuery>singleton(q));
        br.getExecutor().execute(cmd);
          
        // all tasks have incoming changes
        for (DummyTaskWrapper wrapper : wrappers) {
            String newSummary = newSummaries.get(wrapper.task);
            assertEquals(ITask.SynchronizationState.INCOMING, wrapper.getSynchronizationState());
            assertEquals(ITask.SynchronizationState.INCOMING, wrapper.task.getSynchronizationState());
            assertEquals(newSummary, wrapper.getSummary());
            assertEquals("Summary from " + oldSummaries.get(wrapper.task) + " to " + newSummary, wrapper.getIncomingChangesText());
            
            // open and see changes
            DummyEditorPage p = new DummyEditorPage(wrapper.task);
            p.open();
            assertTrue(p.summaryChanged);
            assertEquals(newSummary, p.taskDataSummary);
            assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
            assertEquals(ITask.SynchronizationState.SYNCHRONIZED, wrapper.task.getSynchronizationState());
            p.close();
        }
        for (DummyTaskWrapper wrapper : wrappers) {
            wrapper.forget();
        }
    }
    
    public void testModifyQuery () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        IRepositoryQuery query = supp.getRepositoryQuery(btr, QUERY_NAME);
        Collection<ITask> tasks = supp.getTasks(query);
        Collection<ITask> toRemove = new HashSet<ITask>();
        for (ITask task : tasks) {
            if (task.isCompleted()) {
                toRemove.add(task);
            }
        }
        assertFalse(toRemove.isEmpty());
        
        // make another new task so the query is not empty
        ITask createdTask = createNewTask("New task testModifyQuery");
        
        // modify query to make it more precise - will not list closed tasks
        assertFalse(tasks.isEmpty());
        DummyQueryController controller = new DummyQueryController(query);
        query.setUrl(query.getUrl() + "&bug_status=NEW" + "&bug_status=REOPENED"); //NOI18N
        // synchronize
        SynchronizeQueriesCommand cmd = supp.getMylynFactory().createSynchronizeQueriesCommand(btr, Collections.<IRepositoryQuery>singleton(query));
        br.getExecutor().execute(cmd);
        
        tasks = controller.tasks;
        assertFalse(tasks.isEmpty());
        assertEquals(new HashSet<ITask>(supp.getTasks(query)), new HashSet<ITask>(tasks));
        for (ITask removedTask : toRemove) {
            assertFalse(tasks.contains(removedTask));
        }
        assertTrue(tasks.contains(createdTask));
        controller.forget();
    }
    
    public void testTaskRemovedFromQueryInt () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        IRepositoryQuery q = supp.getRepositoryQuery(btr, QUERY_NAME);
        Collection<ITask> tasks = supp.getTasks(q);
        
        // get tasks from the query
        assertFalse(tasks.isEmpty());
        DummyQueryController controller = new DummyQueryController(q);
        // get a task to close
        ITask task = tasks.iterator().next();
        assertTrue(controller.tasks.contains(task));
        // close the task in editor
        DummyEditorPage page = new DummyEditorPage(task);
        page.open();
        page.closeTask("WONTFIX");
        page.save();
        
        // submit
        page.submit();
        assertTrue(task.isCompleted());
        
        // refresh query
        SynchronizeQueriesCommand cmd = supp.getMylynFactory().createSynchronizeQueriesCommand(btr, Collections.<IRepositoryQuery>singleton(q));
        br.getExecutor().execute(cmd);
        
        // task should be removed from the list
        assertFalse(controller.getTasks().contains(task));
        
        page.close();
        controller.forget();
    }
    
    public void testTaskRemovedFromQueryExt () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        IRepositoryQuery q = supp.getRepositoryQuery(btr, QUERY_NAME);
        Collection<ITask> tasks = supp.getTasks(q);
        
        // get tasks from the query
        assertFalse(tasks.isEmpty());
        DummyQueryController controller = new DummyQueryController(q);
        // get a task to close
        ITask task = tasks.iterator().next();
        assertTrue(controller.tasks.contains(task));
        
        // close the task externally
        assertFalse(task.isCompleted());
        TaskData external = supp.getTaskDataState(task).getRepositoryData();
        TaskAttribute opAttr = external.getRoot().getMappedAttribute(TaskAttribute.OPERATION);
        TaskOperation taskOperation = null;
        for (TaskOperation op : external.getAttributeMapper().getTaskOperations(opAttr)) {
            if (BugzillaOperation.resolve.getLabel().equals(op.getLabel())) {
                taskOperation = op;
                break;
            }
        }
        assertNotNull(taskOperation);
        external.getAttributeMapper().setTaskOperation(opAttr, taskOperation);
        TaskAttribute resolutionAttr = external.getRoot().getMappedAttribute(BugzillaOperation.resolve.getInputId());
        resolutionAttr.setValue("WONTFIX");
        SubmitCommand submitCmd = new SubmitCommand(Bugzilla.getInstance().getRepositoryConnector(), btr, external);
        br.getExecutor().execute(submitCmd);
        
        // refresh query
        SynchronizeQueriesCommand cmd = supp.getMylynFactory().createSynchronizeQueriesCommand(btr, Collections.<IRepositoryQuery>singleton(q));
        br.getExecutor().execute(cmd);
        
        // task should be removed from the list
        assertFalse(controller.getTasks().contains(task));
        
        controller.forget();
    }
    
    public void testSimpleSearch () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        String queryName = "Temporary query";
        
        IRepositoryQuery q = supp.getMylynFactory().createNewQuery(btr, queryName);
        q.setUrl("/buglist.cgi?query_format=advanced&bug_id=1%2C2%2C3"); // three tasks
        // query list is empty
        assertFalse(supp.getRepositoryQueries(btr).contains(q));
        // it's still empty, need to sync first
        
        // synchronize
        SimpleQueryCommand cmd = supp.getMylynFactory().createSimpleQueryCommand(btr, q);
        br.getExecutor().execute(cmd);
                
        // get all tasks for the query
        Collection<ITask> tasks = cmd.getTasks();
        assertEquals(3, tasks.size());
        
        Collection<ITask> tasklistTasks = supp.getTasks(btr);
        // all tasks are in the tasklist
        for (ITask t : tasks) {
            assertTrue(tasklistTasks.contains(t));
        }
        assertFalse(supp.getRepositoryQueries(btr).contains(q));
        
        // open tasks
        for (ITask t : tasks) {
            DummyEditorPage p = new DummyEditorPage(t);
            p.open();
            p.waitUntilOpened();
            p.assertOpened();
            p.close();
        }
    }

    /**
     * This should be done in the editor page upon click on Submit
     */
    private ITask submitTask (ITask task, TaskDataModel model) throws CoreException {
        SubmitTaskCommand cmd = MylynSupport.getInstance().getMylynFactory().createSubmitTaskCommand(task, model);
        br.getExecutor().execute(cmd);
        ITask newTask = cmd.getSubmittedTask();
        if (task == newTask) {
            // refresh model and whole editor page if opened
            model.refresh(null);
        }
        
        return newTask;
    }

    private void makeExternalChange (ITask task, String newSummary) throws CoreException {
        MylynSupport supp = MylynSupport.getInstance();
        TaskData taskData = supp.getTaskDataState(task).getRepositoryData();
        
        // edit the task externally
        TaskAttribute rta = taskData.getRoot();
        // now make an external change in summary
        TaskData external = new TaskData(taskData.getAttributeMapper(),
                taskData.getConnectorKind(),
                taskData.getRepositoryUrl(),
                taskData.getTaskId());
        external.setVersion(taskData.getVersion());
        for (TaskAttribute child : rta.getAttributes().values()) {
            external.getRoot().deepAddCopy(child);
        }
        external.getRoot().getMappedAttribute(TaskAttribute.SUMMARY).setValue(newSummary);
        SubmitCommand submitCmd = new SubmitCommand(Bugzilla.getInstance().getRepositoryConnector(), btr, external);
        br.getExecutor().execute(submitCmd);
    }

    private ITask createNewTask (String summary) throws CoreException {
        MylynSupport supp = MylynSupport.getInstance();
        ITaskMapping mapping = new TaskMapping() {

            @Override
            public String getProduct () {
                return PRODUCT;
            }

            @Override
            public String getComponent () {
                return COMPONENT;
            }
            
        };
        ITask task = supp.getMylynFactory().createTask(btr, mapping);
        TaskDataModel model = supp.getTaskDataModel(task);
        
        // model.getTaskData returns our local data
        TaskAttribute rta = model.getTaskData().getRoot();
        assertFalse(model.isDirty());
        // now edit summary, product and component
        String newSummary = summary;
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(newSummary);
        model.attributeChanged(ta);
        
        // save
        model.save(new NullProgressMonitor());
        if (task.getSynchronizationState() == ITask.SynchronizationState.OUTGOING_NEW) {
            task.setSummary(newSummary);
        }
        return submitTask(task, model);
    }

    private void deleteTaskData (ITask task) throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Field f = MylynSupport.class.getDeclaredField("taskDataManager");
        f.setAccessible(true);
        TaskDataManager mgr = (TaskDataManager) f.get(supp);
        mgr.deleteTaskData(task);
    }

    // something like BugzillaIssue
    private class DummyTaskWrapper implements ITaskListChangeListener {
        private final ITask task;
        private ITask.SynchronizationState syncState;
        private String summary;
        private String incomingChanges;

        public DummyTaskWrapper (ITask task) {
            this.task = task;
            syncState = task.getSynchronizationState();
            summary = task.getSummary();
            MylynSupport.getInstance().addTaskListListener(this);
        }

        // should be moved to a central place
        // maybe a BugzillaIssueManager
        @Override
        public void containersChanged (Set<TaskContainerDelta> containers) {
            for (TaskContainerDelta delta : containers) {
                if (delta.getElement() == task && delta.getKind() == TaskContainerDelta.Kind.CONTENT) {
                    syncState = task.getSynchronizationState();
                    summary = task.getSummary();
                    incomingChanges = "";
                    if (syncState == ITask.SynchronizationState.INCOMING
                            || syncState == ITask.SynchronizationState.CONFLICT) {
                        try {
                            TaskDataState taskDataState = MylynSupport.getInstance().getTaskDataState(task);
                            Set<TaskAttribute> changedAttributes = MylynSupport.getInstance().countDiff(
                                    taskDataState.getRepositoryData(),
                                    taskDataState.getLastReadData());
                            for (TaskAttribute changedAttr : changedAttributes) {
                                if (changedAttr.getId().equals(taskDataState.getRepositoryData().getRoot()
                                        .getMappedAttribute(TaskAttribute.SUMMARY).getId())) {
                                    incomingChanges = "Summary from "
                                            + taskDataState.getLastReadData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue()
                                            + " to "
                                            + taskDataState.getRepositoryData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue();
                                }
                            }
                        } catch (CoreException ex) {
                            log(ex.toString());
                        }
                    }
                }
            }
        }
        
        void forget () {
            MylynSupport.getInstance().removeTaskListListener(this);
        }

        private ITask.SynchronizationState getSynchronizationState () {
            return syncState;
        }

        private String getSummary () {
            return summary;
        }

        private String getIncomingChangesText () {
            return incomingChanges;
        }
    }

    private class DummyEditorPage implements ITaskDataManagerListener {
        private ITask task;
        private String taskId;
        private TaskDataModel model;
        private String taskDataSummary;
        private boolean summaryChanged;
        private boolean summaryChangedLocally;
        private volatile boolean waitingToOpen;
        private MylynSupport supp;

        public DummyEditorPage (ITask task) {
            this.task = task;
        }
        
        public DummyEditorPage (String taskId) {
            this.taskId = taskId;
        }
        
        void open () throws CoreException {
            supp = MylynSupport.getInstance();
            supp.addTaskDataListener(this);
            if (task == null || supp.getTaskDataState(task) == null) {
                waitingToOpen = true;
                RequestProcessor.getDefault().schedule(new Runnable() {
                    @Override
                    public void run () {
                        if (waitingToOpen) {
                            try {
                                GetRepositoryTaskCommand cmd = supp.getMylynFactory().createGetRepositoryTaskCommand(
                                        btr, task == null ? taskId : task.getTaskId());
                                br.getExecutor().execute(cmd);
                                task = cmd.getTask();
                                if (task != null) {
                                    finishOpen();
                                }
                            } catch (CoreException ex) {
                                log(ex.toString());
                            }
                        }
                    }
                }, 2, TimeUnit.SECONDS);
            } else {
                model = MylynSupport.getInstance().getTaskDataModel(task);
                if (model.getTaskData().isPartial()) {
                    waitingToOpen = true;
                } else {
                    finishOpen();
                }
            }
        }
        
        void close () throws CoreException {
            save();
            MylynSupport.getInstance().removeTaskDataListener(this);
        }

        @Override
        public void taskDataUpdated (TaskDataManagerEvent event) {
            if (event.getTask() == task) {
                if (!event.getTaskData().isPartial() && waitingToOpen) {
                    try {
                        finishOpen();
                    } catch (CoreException ex) {
                        log(ex.getMessage());
                    }
                }
                if (event.getTaskDataUpdated()) {
                    refresh();
                }
            }
        }

        @Override
        public void editsDiscarded (TaskDataManagerEvent event) {
            if (event.getTask() == task) {
                refresh();
            }
        }

        private void clear () {
            summaryChanged = summaryChangedLocally = false;
        }

        private void changeSummary (String newSummary) {
            TaskAttribute summaryAttr = model.getTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY);
            summaryAttr.setValue(newSummary);
            model.attributeChanged(summaryAttr);
            assertTrue(model.isDirty());
        }

        private void save () throws CoreException {
            if (model.isDirty()) {
                model.save(null);
            }
        }

        private void revert () throws CoreException {
            MylynSupport.getInstance().discardLocalEdits(task);
        }

        private void refresh () {
            if (model == null) {
                return;
            }
            // maybe show a warning before overwriting the state
            try {
                model.refresh(null);
                TaskAttribute ta = model.getTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY);
                summaryChangedLocally = model.hasOutgoingChanges(ta);
                summaryChanged = model.hasIncomingChanges(ta);
                taskDataSummary = ta.getValue();
            } catch (CoreException ex) {
                log(ex.toString());
            }
        }

        private void finishOpen () throws CoreException {
            if (model == null) {
                model = MylynSupport.getInstance().getTaskDataModel(task);
            }
            taskDataSummary = model.getTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue();
            TaskAttribute ta = model.getTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY);
            summaryChangedLocally = model.hasOutgoingChanges(ta);
            summaryChanged = model.hasIncomingChanges(ta);
            waitingToOpen = false;
        }
        
        private void assertOpened () {
            assertFalse(waitingToOpen);
        }

        private void closeTask (String resolution) {
            TaskOperation taskOperation = null;
            TaskAttribute opAttr = model.getTaskData().getRoot().getMappedAttribute(TaskAttribute.OPERATION);
            for (TaskOperation op : model.getTaskData().getAttributeMapper().getTaskOperations(opAttr)) {
                if (BugzillaOperation.resolve.getLabel().equals(op.getLabel())) {
                    taskOperation = op;
                    break;
                }
            }
            assertNotNull(taskOperation);
            assertFalse(task.isCompleted());
            model.getTaskData().getAttributeMapper().setTaskOperation(opAttr, taskOperation);
            model.attributeChanged(opAttr);
            TaskAttribute resolutionAttr = model.getTaskData().getRoot().getMappedAttribute(BugzillaOperation.resolve.getInputId());
            resolutionAttr.setValue(resolution);
            model.attributeChanged(resolutionAttr);
        }

        private void submit () throws CoreException {
            SubmitTaskCommand cmd = supp.getMylynFactory().createSubmitTaskCommand(task, model);
            br.getExecutor().execute(cmd);
        }

        private void waitUntilOpened () throws Exception {
            for (int i = 0; i < 50; ++i) {
                if (!waitingToOpen) {
                    break;
                }
                Thread.sleep(1000);
            }
            assertOpened();
        }
    }

    private class DummyQueryController implements ITaskListChangeListener {
        private final IRepositoryQuery query;
        private final MylynSupport supp;
        private final Set<ITask> tasks;
        private final List<DummyEditorPage> pages;
        private boolean flag = true;

        public DummyQueryController (IRepositoryQuery query) throws CoreException {
            this.query = query;
            this.supp = MylynSupport.getInstance();
            this.tasks = new HashSet<ITask>(supp.getTasks(query));
            this.pages = new ArrayList<DummyEditorPage>();
            supp.addTaskListListener(this);
        }

        @Override
        public void containersChanged (Set<TaskContainerDelta> containers) {
            for (TaskContainerDelta delta : containers) {
                if (!delta.isTransient() && delta.getParent()== query && delta.getElement() instanceof ITask) {
                    ITask task = (ITask) delta.getElement();
                    if (delta.getKind() == TaskContainerDelta.Kind.ADDED) {
                        // task added
                        tasks.add(task);
                        // open every other task to simulate fast clicking on task in query
                        // when the task is not yet ready
                        if (flag = !flag) {
                            // open page
                            DummyEditorPage page = new DummyEditorPage(task);
                            pages.add(page);
                            try {
                                page.open();
                            } catch (CoreException ex) {
                                log(ex.getMessage());
                            }
                        }
                    } else if (delta.getKind() == TaskContainerDelta.Kind.REMOVED
                            || delta.getKind() == TaskContainerDelta.Kind.DELETED) {
                        // task added
                        tasks.remove(task);
                    }
                }
            }
        }

        void closeAllPages () throws CoreException {
            for (DummyEditorPage page : pages) {
                page.assertOpened();
                page.close();
            }
        }
        
        private Collection<ITask> getTasks () {
            return tasks;
        }
        
        void forget () {
            supp.removeTaskListListener(this);
        }

        private Set<ITask> getOpenedTasks () {
            Set<ITask> retval = new HashSet<ITask>(pages.size());
            for (DummyEditorPage p : pages) {
                retval.add(p.task);
            }
            return retval;
        }
    }
    
}
