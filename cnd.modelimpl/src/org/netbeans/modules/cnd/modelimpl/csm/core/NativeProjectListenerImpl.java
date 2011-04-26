/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;

/**
 * Implementation of the NativeProjectItemsListener interface
 * @author Vladimir Kvashin
 */
// package-local
class NativeProjectListenerImpl implements NativeProjectItemsListener {
    private static final boolean TRACE = false;

    private final NativeProject nativeProject;
    private final ProjectBase projectBase;
    private volatile boolean enabledEventsHandling = true;

    public NativeProjectListenerImpl(ModelImpl model, NativeProject nativeProject, ProjectBase project) {
        this.nativeProject = nativeProject;
        this.projectBase = project;
    }
    
    @Override
    public void fileAdded(NativeFileItem fileItem) {
        if (TRACE) {
            new Exception().printStackTrace(System.err);
            System.err.println("Native event fileAdded:"); // NOI18N
            System.err.println("\t"+fileItem.getAbsolutePath()); // NOI18N
        }
	onProjectItemsAdded(Collections.singletonList(fileItem));
    }

    @Override
    public void filesAdded(List<NativeFileItem> fileItems) {
        if (TRACE) {
            new Exception().printStackTrace(System.err);
            System.err.println("Native event filesAdded:"); // NOI18N
            for(NativeFileItem fileItem: fileItems){
                System.err.println("\t"+fileItem.getAbsolutePath()); // NOI18N
            }
        }
        onProjectItemsAdded(fileItems);
    }

    @Override
    public void fileRemoved(NativeFileItem fileItem) {
        if (TRACE) {
            new Exception().printStackTrace(System.err);
            System.err.println("Native event fileRemoved:"); // NOI18N
            System.err.println("\t"+fileItem.getAbsolutePath()); // NOI18N
        }
	onProjectItemRemoved(Collections.singletonList(fileItem));
    }

    @Override
    public void filesRemoved(List<NativeFileItem> fileItems) {
        if (TRACE) {
            new Exception().printStackTrace(System.err);
            System.err.println("Native event filesRemoved:"); // NOI18N
            for(NativeFileItem fileItem: fileItems){
                System.err.println("\t"+fileItem.getAbsolutePath()); // NOI18N
            }
        }
        onProjectItemRemoved(fileItems);
    }

    @Override
    public void fileRenamed(String oldPath, NativeFileItem newFileIetm){
        if (TRACE) {
            new Exception().printStackTrace(System.err);
            System.err.println("Native event fileRenamed:"); // NOI18N
            System.err.println("\tOld Name:"+oldPath); // NOI18N
            System.err.println("\tNew Name:"+newFileIetm.getAbsolutePath()); // NOI18N
        }
	onProjectItemRenamed(oldPath, newFileIetm);
    }

    @Override
    public void filePropertiesChanged(NativeFileItem fileItem) {
        if (TRACE) {
            new Exception().printStackTrace(System.err);
            System.err.println("Native event filePropertiesChanged:"); // NOI18N
            System.err.println("\t"+fileItem.getAbsolutePath()); // NOI18N
        }
	filesPropertiesChanged(Collections.singletonList(fileItem));
    }

    @Override
    public void filesPropertiesChanged(final List<NativeFileItem> fileItems) {
        if (TRACE) {
            new Exception().printStackTrace(System.err);
            System.err.println("Native event filesPropertiesChanged:"); // NOI18N
            for(NativeFileItem fileItem: fileItems){
                System.err.println("\t"+fileItem.getAbsolutePath()); // NOI18N
            }
        }
        if (enabledEventsHandling) {
            // FIXUP for #109425
            ModelImpl.instance().enqueueModelTask(new Runnable() {
                @Override
                public void run() {
                    filesPropertiesChangedImpl(fileItems);
                }
            }, "Applying property changes"); // NOI18N
        } else {
            if (TraceFlags.TIMING) {
                System.err.printf("\nskipped filesPropertiesChanged(list) %s...\n",
                        nativeProject.getProjectDisplayName());
            }
        }
    }

    /*package*/final void enableListening(boolean enable) {
        if (TraceFlags.TIMING) {
            System.err.printf("\n%s ProjectListeners %s...\n", enable?"enable":"disable",
                    nativeProject.getProjectDisplayName());
        }
        enabledEventsHandling = enable;
    }

    @Override
    public void filesPropertiesChanged() {
        if (TRACE) {
            new Exception().printStackTrace(System.err);
            System.err.println("Native event projectPropertiesChanged:"); // NOI18N
            for(NativeFileItem fileItem : nativeProject.getAllFiles()){
                System.err.println("\t"+fileItem.getAbsolutePath()); // NOI18N
            }
        }
        if (enabledEventsHandling) {
            // FIXUP for #109425
            ModelImpl.instance().enqueueModelTask(new Runnable() {
                @Override
                public void run() {
                    ArrayList<NativeFileItem> list = new ArrayList<NativeFileItem>();
                    for(NativeFileItem item : nativeProject.getAllFiles()){
                        if (!item.isExcluded()) {
                            switch(item.getLanguage()){
                                case C:
                                case CPP:
                                case FORTRAN:
                                    list.add(item);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    filesPropertiesChangedImpl(list);
                }
            }, "Applying property changes"); // NOI18N
        } else {
            if (TraceFlags.TIMING) {
                System.err.printf("\nskipped filesPropertiesChanged %s...\n", nativeProject.getProjectDisplayName());
            }
        }
    }

    @Override
    public void projectDeleted(NativeProject nativeProject) {
	RepositoryUtils.onProjectDeleted(nativeProject);
    }

    private void onProjectItemsAdded(final List<NativeFileItem> items) {
        if (items.size()>0){
            try {
                projectBase.onFileAdded(items);
            } catch( Exception e ) {
                e.printStackTrace(System.err);
            }
        }
    }
    
    private void onProjectItemRemoved(final List<NativeFileItem> items) {
        if (!items.isEmpty()) {
            try {
                projectBase.onFileRemoved(items);
            } catch( Exception e ) {
                e.printStackTrace(System.err);
            }
        }
    }

    private void onProjectItemRenamed(String oldPath, NativeFileItem newFileIetm) {
        try {
            projectBase.onFileRemoved(oldPath);
            projectBase.onFileAdded(newFileIetm);
        } catch( Exception e ) {
            //TODO: FIX (most likely in Makeproject: path == null in this situation,
            //this cause NPE
            e.printStackTrace(System.err);
        }
    }
    
    private void filesPropertiesChangedImpl(final List<NativeFileItem> items) {
        if (items.size()>0){
            try {
                if (projectBase.isValid()) {
                    if (projectBase instanceof ProjectImpl) {
                        LibraryManager.getInstance().onProjectPropertyChanged(projectBase.getUID());
                    }
                    projectBase.onFilePropertyChanged(items);
                }
            } catch( Exception e ) {
                e.printStackTrace(System.err);
            }
        }
    }
}
