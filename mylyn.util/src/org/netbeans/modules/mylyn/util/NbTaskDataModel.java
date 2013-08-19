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

import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;

/**
 *
 * @author Ondrej Vrabec
 */
public final class NbTaskDataModel {

    private final ITaskDataWorkingCopy workingCopy;
    private final TaskDataModel delegateModel;
    private final List<NbTaskDataModelListener> listeners = new CopyOnWriteArrayList<NbTaskDataModelListener>();
    private final NbTask task;
    
    NbTaskDataModel (TaskRepository taskRepository, NbTask task, ITaskDataWorkingCopy workingCopy) {
        this.task = task;
        this.delegateModel = new TaskDataModel(taskRepository, task.getDelegate(), workingCopy);
        this.workingCopy = workingCopy;
        delegateModel.addModelListener(new TaskDataModelListener() {
            @Override
            public void attributeChanged (TaskDataModelEvent modelEvent) {
                NbTaskDataModelEvent event = new NbTaskDataModelEvent(NbTaskDataModel.this, modelEvent);
                for (NbTaskDataModelListener list : listeners.toArray(new NbTaskDataModelListener[0])) {
                    list.attributeChanged(event);
                }
            }
        });
    }

    public boolean hasIncomingChanges (TaskAttribute taskAttribute, boolean includeConflicts) {
        TaskData repositoryData = workingCopy.getRepositoryData();
        if (repositoryData == null) {
            return false;
        }
        taskAttribute = repositoryData.getRoot().getMappedAttribute(taskAttribute.getPath());
        if (taskAttribute == null) {
            return false;
        }
        boolean incoming = delegateModel.hasIncomingChanges(taskAttribute);
        if (includeConflicts && !incoming && delegateModel.hasOutgoingChanges(taskAttribute)) {
            TaskData lastReadData = workingCopy.getLastReadData();
            if (lastReadData == null) {
                return true;
            }
            TaskAttribute oldAttribute = lastReadData.getRoot().getMappedAttribute(taskAttribute.getPath());
            if (oldAttribute == null) {
                return true;
            }
            return !repositoryData.getAttributeMapper().equals(taskAttribute, oldAttribute);
        }
        return incoming;
    }

    public boolean hasOutgoingChanges (TaskAttribute ta) {
        return delegateModel.hasOutgoingChanges(ta);
    }

    public TaskData getLocalTaskData () {
        return delegateModel.getTaskData();
    }

    public TaskData getLastReadTaskData () {
        return workingCopy.getLastReadData();
    }

    public TaskData getRepositoryTaskData () {
        return workingCopy.getRepositoryData();
    }
    
    public void addNbTaskDataModelListener (NbTaskDataModelListener listener) {
        listeners.add(listener);
    }
    
    public void removeNbTaskDataModelListener (NbTaskDataModelListener listener) {
        listeners.remove(listener);
    }

    public boolean isDirty () {
        return delegateModel.isDirty();
    }

    public void attributeChanged (TaskAttribute a) {
        delegateModel.attributeChanged(a);
    }

    public Set<TaskAttribute> getChangedAttributes () {
        return delegateModel.getChangedAttributes();
    }

    public Set<TaskAttribute> getChangedOldAttributes () {
        return delegateModel.getChangedOldAttributes();
    }

    public void refresh () throws CoreException {
        delegateModel.refresh(null);
        Set<TaskAttribute> changedAttributes = delegateModel.getChangedAttributes();
        for (TaskAttribute ta : changedAttributes) {
            // there are still local unsaved changes, keep them in local taskdata
            TaskAttribute attribute = getLocalTaskData().getRoot().getAttribute(ta.getId());
            if (attribute == null) {
                getLocalTaskData().getRoot().deepAddCopy(ta);
            } else {
                attribute.setValues(ta.getValues());
            }
        }
    }

    public void save () throws CoreException {
        save(null);
    }

    public void save (IProgressMonitor monitor) throws CoreException {
        delegateModel.save(monitor);
    }

    public boolean hasBeenRead () {
        return delegateModel.hasBeenRead();
    }

    public TaskRepository getTaskRepository () {
        return delegateModel.getTaskRepository();
    }
    
    public NbTask getTask () {
        return task;
    }

    ITask getDelegateTask () {
        return delegateModel.getTask();
    }

    public boolean hasOutgoingChanged () {
        return isDirty() || !getChangedAttributes().isEmpty();
    }
    
    public static interface NbTaskDataModelListener extends EventListener {

        public void attributeChanged (NbTaskDataModelEvent event);
        
    }
    
    public static final class NbTaskDataModelEvent extends EventObject {
        private final TaskDataModelEvent modelEvent;
        private final NbTaskDataModel model;

        private NbTaskDataModelEvent (NbTaskDataModel source, TaskDataModelEvent modelEvent) {
            super(source);
            this.model = source;
            this.modelEvent = modelEvent;
        }

        public NbTaskDataModel getModel () {
            return model;
        }
        
        public TaskAttribute getTaskAttribute () {
            return modelEvent.getTaskAttribute();
        }
        
    }
}
