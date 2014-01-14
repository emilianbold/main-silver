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
package org.netbeans.modules.cnd.qnavigator.navigator;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.model.tasks.CndParserResult;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Alexander Simon
 */
public class NavigatorNodeFactoryTask extends IndexingAwareParserResultTask<CndParserResult> {
    private AtomicBoolean canceled = new AtomicBoolean(false);
    
    public NavigatorNodeFactoryTask() {
        super(TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    @Override
    public void run(CndParserResult result, SchedulerEvent event) {
        synchronized (this) {
            canceled.set(true);
            canceled = new AtomicBoolean(false);
        }
        final NavigatorComponent navigator = NavigatorComponent.getInstance();
        if (navigator == null) {
            return;
        }
        final NavigatorPanelUI panelUI = navigator.getPanelUI();
        final NavigatorContent content = panelUI.getContent();
        final DataObject cdo = content.getDataObject();
        if (cdo == null) {
            return;
        }
        FileObject fo = result.getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return;
        }
        String mimeType = result.getSnapshot().getMimePath().getPath();
        CsmFile csmFile = result.getCsmFile();
        if (csmFile != null) {
            NavigatorModel oldModel = content.getModel();
            if (oldModel != null) {
                DataObject oldCdo = oldModel.getDataObject();
                CsmFile oldCsmFile = oldModel.getCsmFile();
                if (oldCsmFile != null && oldCsmFile.isValid()) {
                    if (cdo.equals(oldCdo) && csmFile.equals(oldCsmFile) && csmFile.isValid()) {
                        oldModel.update(canceled, false);
                        return;
                    }
                }
            }
            final NavigatorModel model = new NavigatorModel(cdo, fo, panelUI, navigator, mimeType, csmFile);
            if (!canceled.get()) {
                content.setModel(model);
                model.update(canceled, true);
            }
        } else {
            final NavigatorModel model = new NavigatorModel(cdo, fo, panelUI, navigator, mimeType, csmFile);
            content.setModel(model);
            model.update(canceled, true);
        }
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.SELECTED_NODES_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public final synchronized void cancel() {
        canceled.set(true);
    }
    
    @MimeRegistrations({
        @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.FORTRAN_MIME_TYPE, service = TaskFactory.class)
    })
    public static class NavigatorSourceFactory extends TaskFactory {
        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singletonList(new NavigatorNodeFactoryTask());
        }
    }
}
