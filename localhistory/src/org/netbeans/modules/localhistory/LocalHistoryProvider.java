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
package org.netbeans.modules.localhistory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.localhistory.ui.view.DeleteAction;
import org.netbeans.modules.localhistory.ui.view.RevertFileAction;
import org.netbeans.modules.localhistory.utils.FileUtils;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Tomas Stupka
 */
public class LocalHistoryProvider extends VCSHistoryProvider {

    @Override
    public void addChangeListener(ChangeListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HistoryEntry[] getHistory(File[] files, Date fromDate) {
        if(files == null || files.length == 0) {
            return new HistoryEntry[0];
        }
        List<HistoryEntry> storeEntries = new LinkedList<HistoryEntry>();
        for (File f : files) {
            StoreEntry[] ses = LocalHistory.getInstance().getLocalHistoryStore().getStoreEntries(f);
            for(StoreEntry se : ses) {
                HistoryEntry e = 
                    new HistoryEntry(
                        files, 
                        se.getDate(), 
                        se.getLabel(), 
                        "",                                                             // username         NOI18N
                        "",                                                             // username short   NOI18N 
                        NbBundle.getMessage(LocalHistoryProvider.class, "LBL_Local"),   // revision         NOI18N
                        NbBundle.getMessage(LocalHistoryProvider.class, "LBL_Local"),   // revision short   NOI18N
                        getActions(), 
                        new RevisionProviderImpl(se),
                        new MessageEditImpl(se));
                storeEntries.add(e);
            }
        }
        return storeEntries.toArray(new HistoryEntry[storeEntries.size()]);
    }

    @Override
    public Action createShowHistoryAction(File[] files) {
        return null;
    }
    
    private Action[] getActions() {
        return new Action[] {
            SystemAction.get(RevertFileAction.class),
            SystemAction.get(DeleteAction.class)    
        };
    }
    
    private class RevisionProviderImpl implements VCSHistoryProvider.RevisionProvider {
        private final StoreEntry se;

        public RevisionProviderImpl(StoreEntry se) {
            this.se = se;
        }
        
        @Override
        public void getRevisionFile(File originalFile, File revisionFile) {
            try {
                File file = se.getFile();
                FileUtils.copy(se.getStoreFileInputStream(), revisionFile);
                Utils.associateEncoding(file, revisionFile);
            } catch (IOException e) {
                LocalHistory.LOG.log(Level.WARNING, "Error while retrieving history for file {0} stored as {1}", new Object[]{se.getFile(), se.getStoreFile()}); // NOI18N
            }
        }
    }
    
    private class MessageEditImpl implements VCSHistoryProvider.MessageEditProvider {
        private final StoreEntry se;
        public MessageEditImpl(StoreEntry se) {
            this.se = se;
        }
        @Override
        public void setMessage(String message) throws IOException {
            LocalHistory.getInstance().getLocalHistoryStore().setLabel(se.getFile(), se.getTimestamp(), message);
        }
    }
}
