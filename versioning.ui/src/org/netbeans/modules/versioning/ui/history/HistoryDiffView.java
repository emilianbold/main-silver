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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.versioning.ui.history;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.MissingResourceException;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.text.BadLocationException;

import org.netbeans.api.diff.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka
 */
public class HistoryDiffView implements PropertyChangeListener {
           
    private final HistoryComponent tc;
    private DiffPanel panel;
    private Component diffComponent;
    private DiffController diffView;                
    private Runnable prepareDiff = null;
    private Task prepareDiffTask = null;
    private boolean selected;
    private PreparingDiffHandler preparingDiffPanel;
        
    /** Creates a new instance of LocalHistoryView */
    public HistoryDiffView(HistoryComponent tc) {
        this.tc = tc;
        panel = new DiffPanel();                                                              
        showNoContent(NbBundle.getMessage(HistoryDiffView.class, "MSG_DiffPanel_NoVersion"));  // NOI18N                
    }    
        
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            tc.disableNavigationButtons();
            selectionChanged(evt);            
        } else if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
            tc.refreshNavigationButtons(diffView.getDifferenceIndex(), diffView.getDifferenceCount());
        }
    }
      
    JPanel getPanel() {
        return panel;
    }    
    
    private void selectionChanged(PropertyChangeEvent evt) {
        Node[] newSelection = ((Node[]) evt.getNewValue());
        if(newSelection != null) {
            if (newSelection.length == 1) {
                HistoryEntry entry = newSelection[0].getLookup().lookup(HistoryEntry.class);
                if (entry != null) {
                    File file = newSelection[0].getLookup().lookup(File.class);
                    if(file == null) {
                        file = entry.getFiles()[0];
                    }
                    selected = true;
                    refreshDiffPanel(entry, file);
                    return;
                }
            } else if (newSelection.length == 2) {
                HistoryEntry entry1 = newSelection[0].getLookup().lookup(HistoryEntry.class);
                File file1 = null;
                if (entry1 != null) {
                    file1 = newSelection[0].getLookup().lookup(File.class);
                    if(file1 == null) {
                        file1 = entry1.getFiles()[0];
                    }
                }
                
                File file2 = null;
                HistoryEntry entry2 = newSelection[1].getLookup().lookup(HistoryEntry.class);
                if (entry2 != null) {
                    file2 = newSelection[1].getLookup().lookup(File.class);
                    if(file2 == null) {
                        file2 = entry1.getFiles()[0];
                    }
                }
                
                if(entry1 != null && entry2 != null && file1 != null && file2 != null) {
                    refreshDiffPanel(entry1, entry2, file1, file2);
                    selected = true;
                    return;
                }
            }
        } 
        
        selected = false;
        String msgKey = (newSelection == null) || (newSelection.length == 0)
                        ? "MSG_DiffPanel_NoVersion"                     //NOI18N
                        : "MSG_DiffPanel_IllegalSelection";             //NOI18N
        showNoContent(NbBundle.getMessage(HistoryDiffView.class, msgKey));
    }           
    
    private void refreshDiffPanel(HistoryEntry entry1, HistoryEntry entry2, File file1, File file2) { 
        prepareDiff = new RevisionDiffPrepareTask(entry1, entry2, file1, file2);
        scheduleTask(prepareDiff);
    } 
    private void refreshDiffPanel(HistoryEntry entry, File file) {  
        prepareDiff = new CurrentDiffPrepareTask(entry, file);
        scheduleTask(prepareDiff);
    }        

    private void scheduleTask(Runnable runnable) {          
        if(prepareDiffTask != null) {
            prepareDiffTask.cancel();
            getPreparingDiffHandler().finish();
    }
        prepareDiffTask = History.getInstance().getRequestProcessor().create(runnable);
        prepareDiffTask.schedule(0);        
    }

    private PreparingDiffHandler getPreparingDiffHandler() {
        if(preparingDiffPanel == null) {
            preparingDiffPanel = new PreparingDiffHandler();
    }
        return preparingDiffPanel;
    }
    
    private class CurrentDiffPrepareTask implements Runnable {
        
        private final HistoryEntry entry;
        private final File file;

        public CurrentDiffPrepareTask(final HistoryEntry entry, File file) {
            this.entry = entry;
            this.file = file;
        }

        @Override
        public void run() {
            File tmpFile;
            getPreparingDiffHandler().start();
            try {
                File tempFolder = Utils.getTempFolder();
                tmpFile = new File(tempFolder, file.getName()); // XXX
                entry.getRevisionFile(file, tmpFile);
            } finally {
                getPreparingDiffHandler().finish();
            }
            String title2;
            if(file.exists()) {
                title2 = NbBundle.getMessage(HistoryDiffView.class, "LBL_Diff_CurrentFile"); // NOI18N
            } else {
                title2 = NbBundle.getMessage(HistoryDiffView.class, "LBL_Diff_FileDeleted"); // NOI18N
            }            
            prepareDiffView(tmpFile, file, file.getName() + " " + RevisionNode.getFormatedDate(entry), title2); // NOI18N
        }

    }        

    private class RevisionDiffPrepareTask implements Runnable {
        
        private final HistoryEntry entry1;
        private final HistoryEntry entry2;
        private final File file1;
        private final File file2;

        public RevisionDiffPrepareTask(final HistoryEntry entry1, HistoryEntry entry2, File file1, File file2) {
            this.entry1 = entry1;
            this.entry2 = entry2;
            this.file1 = file1;
            this.file2 = file2;
        }

        @Override
        public void run() {
            File revisionFile1;
            File revisionFile2;
            getPreparingDiffHandler().start();
            try {
                revisionFile1 = getRevisionFile(entry1, file1);
                revisionFile2 = getRevisionFile(entry2, file2);
            } finally {
                getPreparingDiffHandler().finish();
            }
            String title1 = getTitle(entry1, file1);
            String title2 = getTitle(entry2, file2);
            prepareDiffView(revisionFile1, revisionFile2, title1, title2);
        }

        private String getTitle(HistoryEntry entry, File file) throws MissingResourceException {
            String title1;
            if(file.exists()) {
                title1 = file.getName() + " " + RevisionNode.getFormatedDate(entry); // NOI18N
            } else {
                title1 = NbBundle.getMessage(HistoryDiffView.class, "LBL_Diff_FileDeleted"); // NOI18N
            }
            return title1;
        }

        private File getRevisionFile(HistoryEntry entry, File file) {
            File tempFolder = Utils.getTempFolder();
            File revFile = new File(tempFolder, file.getName()); // XXX
            entry.getRevisionFile(file, revFile);
            return revFile;
        }

    }  

    private void prepareDiffView(final File file1, final File file2, final String title1, final String title2) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {            
                try {   

                    StreamSource ss1 = new LHStreamSource(file1, title1, getMimeType(file2));

                    StreamSource ss2;                        
                    if(file2.exists()) {
                        ss2 = new LHStreamSource(file2, title2, getMimeType(file2));
                    } else {
                        ss2 = StreamSource.createSource("currentfile", title2, getMimeType(file2), new StringReader("")); // NOI18N
                    }

                    diffView = DiffController.createEnhanced(ss1, ss2);
                    diffView.addPropertyChangeListener(HistoryDiffView.this);

                    JComponent c = diffView.getJComponent();
                    setDiffComponent(c);
                    tc.setDiffView(c);
                    if(diffView.getDifferenceCount() > 0) {
                        setCurrentDifference(0);
                    } else {
                        tc.refreshNavigationButtons(diffView.getDifferenceIndex(), diffView.getDifferenceCount());
                    }
                    panel.revalidate();
                    panel.repaint();
                    if("true".equals(System.getProperty("vcshistory.bindDiffRowToEditor", "false"))) { // NOI18N
                        setBaseLocation();
                    }

                } catch (IOException ioe)  {
                    History.LOG.log(Level.SEVERE, null, ioe);
                }                            
            }

            private void setBaseLocation() throws DataObjectNotFoundException {
                FileObject fo = FileUtil.toFileObject(file2);
                DataObject dao = fo != null ? DataObject.find(fo) : null;
                EditorCookie cookie = dao != null ? dao.getLookup().lookup(EditorCookie.class) : null;
                if(cookie != null) {
                    // find an editor
                    JEditorPane[] panes = cookie.getOpenedPanes();
                    if(panes != null && panes.length > 0) {
                        int p = panes[0].getCaretPosition();
                        if(p > 0) {
                            try {
                                int row = Utilities.getLineOffset((BaseDocument)panes[0].getDocument(), p);
                                if(row > 0) {
                                    diffView.setLocation(DiffController.DiffPane.Base, DiffController.LocationType.LineNumber, row);
                                } 
                            } catch (BadLocationException ex) {
                                History.LOG.log(Level.WARNING, null, ex);
                            }
                        }
                    }
                }
            }
            
            private String getMimeType(File file) {
                FileObject fo = FileUtils.toFileObject(file);
                if(fo != null) {
                    return fo.getMIMEType();   
                } else {
                    return "content/unknown"; // NOI18N
                }                
            }        
        });
        
    }

    private void showNoContent(String s) {
        setDiffComponent(new NoContentPanel(s));
    }

    private void setDiffComponent(Component component) {
        if(diffComponent != null) {
            panel.diffPanel.remove(diffComponent);     
        }       
        panel.diffPanel.add(component, BorderLayout.CENTER);
        diffComponent = component;   
        panel.diffPanel.revalidate();
        panel.diffPanel.repaint();
    }       
    
    void onNextButton() {
        if(diffView == null) {
            return;
        }          
        int nextDiffernce = diffView.getDifferenceIndex() + 1;        
        if(nextDiffernce < diffView.getDifferenceCount()) {
            setCurrentDifference(nextDiffernce);    
        }                        
    }

    void onPrevButton() {
        if(diffView == null) {
            return;
        }
        int prevDiffernce = diffView.getDifferenceIndex() - 1;
        if(prevDiffernce > -1) {
            setCurrentDifference(prevDiffernce);                
        }            
    }    
    
    private void setCurrentDifference(int idx) {
        diffView.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, idx);    
        tc.refreshNavigationButtons(diffView.getDifferenceIndex(), diffView.getDifferenceCount());
    }
    
    private class LHStreamSource extends StreamSource {
        
        private final File file;
        private final String title;
        private final String mimeType;

        public LHStreamSource(File file, String title, String mimeType) {
            this.file = file;
            this.title = title;
            this.mimeType = mimeType;
        }

        @Override
        public boolean isEditable() {
            FileObject fo = FileUtils.toFileObject(file);
            return isPrimary(fo);
        }
        
        private boolean isPrimary(FileObject fo) {            
            if (fo != null) {
                try {
                    DataObject dao = DataObject.find(fo);
                    return fo.equals(dao.getPrimaryFile());
                } catch (DataObjectNotFoundException e) {
                    // no dataobject, never mind
                }
            }
            return true;
        }
    
        @Override
        public Lookup getLookup() {
            FileObject fo = FileUtils.toFileObject(file);
            if (fo != null && isPrimary(fo)) {
                return Lookups.fixed(fo);                 
            } else {
                return Lookups.fixed(); 
            }
        }

        @Override
        public String getName() {
            return title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getMIMEType() {
            return mimeType;
        }

        @Override
        public Reader createReader() throws IOException {
            return new FileReader(file);
        }

        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }

    private class PreparingDiffHandler extends JPanel implements ActionListener {

        private JLabel label = new JLabel();
        private Component progressComponent;
        private ProgressHandle handle;
        private final Timer timer;
        public PreparingDiffHandler() {
            label.setText(NbBundle.getMessage(HistoryDiffView.class, "LBL_PreparingDiff")); // NOI18N
            this.setBackground(UIManager.getColor("TextArea.background")); // NOI18N

            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            add(label, c);
            label.setEnabled(false);
            timer = new Timer(800, this);
}

        
        void start() {
            timer.start();
        }
        
        private synchronized void startProgress() throws MissingResourceException {
            handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(HistoryDiffView.class, "LBL_PreparingDiff")); // NOI18N
            setProgressComponent(ProgressHandleFactory.createProgressComponent(handle));
            handle.start();
            handle.switchToIndeterminate();                    
            setDiffComponent(PreparingDiffHandler.this);
        }
        
        synchronized void finish() {
            timer.stop();
            if(handle != null) {
                handle.finish();
                handle = null;
            }
        }
        
        void setProgressComponent(Component component) {
            if(progressComponent != null) remove(progressComponent);
            if(component != null) {
                this.progressComponent = component;
                GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
                add(component, gridBagConstraints);
            } 
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    startProgress();
                }
            });
        }        
    }
}
