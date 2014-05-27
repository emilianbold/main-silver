/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.nb;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.indexing.IndexingManagerAccessor;
import org.netbeans.modules.parsing.implspi.EnvironmentFactory;
import org.netbeans.modules.parsing.implspi.SchedulerControl;
import org.netbeans.modules.parsing.implspi.SourceControl;
import org.netbeans.modules.parsing.implspi.SourceEnvironment;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.UserQuestionException;
import org.openide.util.lookup.ServiceProvider;

/**
 * The DataObject-based implementation of SourceEnvironment. Refactored from the
 * original implementation in Parsing API
 * 
 * @author sdedic
 */
// Note: 40k is a random position, but less than 50k used in parsing.api tests. Anyone who depends
// on parsing.api tests will receive the testing environment.
@ServiceProvider(service = EnvironmentFactory.class, position = 40000) 
public final class DataObjectEnvFactory extends SourceEnvironment implements EnvironmentFactory {
    
    static {
        IndexingManagerAccessor.setReleaseCompletion(new Runnable() {
            public void run() {
                EventSupport.releaseCompletionCondition();
            }
        });
    }

    @Override
    public Scheduler createScheduler(String schedulerName) {
        switch (schedulerName) {
            case "CURSOR_SENSITIVE_TASK_SCHEDULER": // NOI18N
                return new CursorSensitiveScheduler();
            
            case "EDITOR_SENSITIVE_TASK_SCHEDULER": // NOI18N
                return new CurrentDocumentScheduler();
                
            case "SELECTED_NODES_SENSITIVE_TASK_SCHEDULER": // NOI18N
                return new SelectedNodesScheduler();
                
            default:
                return null;
        }
    }
    
    
    static FileObject getFileObject(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof FileObject) {
            return (FileObject)sdp;
        }
        if (sdp instanceof DataObject) {
            return ((DataObject)sdp).getPrimaryFile();
        }
        return null;
    }

    @Override
    public FileObject findFileObject(Document doc) {
        return getFileObject(doc);
    }

    @Override
    public <T> T runPriorityIO (final Callable<T> r) throws Exception {
        assert r != null;
        return ProvidedExtensions.priorityIO(r);
    }

    @Override
    public SourceEnvironment createEnvironment(Source src, SourceControl control) {
        if (control != null) {
            return new EventSupport(control);
        } else {
            return this;
        }
    }

    @Override
    public Set<String> findSupportedMIMETypes() {
        return EditorSettings.getDefault().getAllMimeTypes();
    }

    @Override
    public Document readDocument(FileObject fileObject, boolean forceOpen) throws IOException {
        EditorCookie ec = null;

        try {
            DataObject dataObject = DataObject.find (fileObject);
            ec = dataObject.getLookup ().lookup (EditorCookie.class);
        } catch (DataObjectNotFoundException ex) {
            //DataobjectNotFoundException may happen in case of deleting opened file
            //handled by returning null
        }

        if (ec == null) return null;
        Document doc = ec.getDocument ();
        if (doc == null && forceOpen) {
            try {
                doc = ec.openDocument ();
            } catch (UserQuestionException uqe) {
                uqe.confirmed ();
                doc = ec.openDocument ();
            }
        }
        return doc;
    }
    
    public void attachScheduler(SchedulerControl s, boolean attach) {
    }

    @Override
    public void addDocListener(SourceEnvironment.DocListener listener) {
    }

    @Override
    public void activate() {
    }

    @Override
    public boolean isReparseBlocked() {
        return false;
    }

}
