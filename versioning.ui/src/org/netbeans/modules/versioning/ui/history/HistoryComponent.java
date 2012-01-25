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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.awt.UndoRedo;
import org.netbeans.modules.versioning.util.DelegatingUndoRedo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.versioning.core.util.Utils;
import org.netbeans.modules.versioning.history.LinkButton;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.ui.history.RevisionNode.Filter;
import org.netbeans.modules.versioning.ui.options.HistoryOptions;
import org.openide.cookies.SaveCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Top component which displays something.
 * 
 * @author Tomas Stupka
 */
@MultiViewElement.Registration(
        displayName="#CTL_SourceTabCaption",
        // no icon
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID=HistoryComponent.PREFERRED_ID, 
        mimeType="",
        position=1000000 // lets leave some space in case somebody really wants to be the last
)
final public class HistoryComponent extends JPanel implements MultiViewElement, HelpCtx.Provider, PropertyChangeListener {

    private HistoryFileView masterView;
    static final String PREFERRED_ID = "text.history";
    private final DelegatingUndoRedo delegatingUndoRedo = new DelegatingUndoRedo(); 
    private Toolbar toolBar;
    private HistoryDiffView diffView;
    
    private File[] files;
    private InstanceContent activatedNodesContent;
    private ProxyLookup lookup;
    private VersioningSystem versioningSystem;
        
    public HistoryComponent() {
        initComponents();
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            setBackground(UIManager.getColor("NbExplorerView.background"));     // NOI18N
        }
        Utils.addPropertyChangeListener(this);
        activatedNodesContent = new InstanceContent();
    }
    
    public HistoryComponent(Lookup context) {
        this();
        DataObject dataObject = context.lookup(DataObject.class);

        List<File> filesList = new LinkedList<File>();
        if (dataObject instanceof DataShadow) {
            dataObject = ((DataShadow) dataObject).getOriginal();
        }
        if (dataObject != null) {
            Collection<File> doFiles = toFileCollection(dataObject.files());
            filesList.addAll(doFiles);
        }
        files = filesList.toArray(new File[filesList.size()]);
        VersioningSystem vs = VersioningSupport.getOwner(files[0]);
        init(vs, files);    
    }
    
    private Collection<File> toFileCollection(Collection<? extends FileObject> fileObjects) {
        Set<File> ret = new HashSet<File>(fileObjects.size());
        for (FileObject fo : fileObjects) {
            ret.add(FileUtil.toFile(fo));
        }
        ret.remove(null);
        return ret;
    }        

    private void init(VersioningSystem vs, final File... files) {   
        init(vs, false, files);
    }
    
    public void init(VersioningSystem vs, boolean refresh, final File... files) {   
        this.versioningSystem = vs;
        if(toolBar == null) {
            toolBar = new Toolbar(vs, files);
        } else {
            toolBar.setup(vs);
        }
        masterView = new HistoryFileView(files, vs, this);
        diffView = new HistoryDiffView(this); 
        
        masterView.getExplorerManager().addPropertyChangeListener(diffView); 
        masterView.getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
            private Node[] activatedNodes;
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {                            
                    if(activatedNodes != null) {
                        for (Node n : activatedNodes) {
                            activatedNodesContent.remove(n);
                        }
                        activatedNodes = (Node[]) evt.getNewValue();
                        for (Node n : activatedNodes) {
                            activatedNodesContent.add(n);
                        }
                    }
                }   
            }
        });
        
        // XXX should be solved in a more general way - not ony for HistoryFileView 
        splitPane.setTopComponent(masterView.getPanel());   
        splitPane.setBottomComponent(diffView.getPanel());   
        
        if(refresh) {
            masterView.refresh();
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(Utils.EVENT_VERSIONED_ROOTS.equals(evt.getPropertyName())) {
            final VersioningSystem vs = VersioningSupport.getOwner(files[0]);
            if(versioningSystem != vs) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        init(vs, true, files);
                    }
                });
            }
        }
    }
    
    Filter getSelectedFilter() {
        return (Filter) getToolbar().filterCombo.getSelectedItem();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());

        splitPane.setDividerLocation(150);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JSplitPane splitPane = new javax.swing.JSplitPane();
    // End of variables declaration//GEN-END:variables

    @Override
    public UndoRedo getUndoRedo() {
        return delegatingUndoRedo;
    }
    
    void setDiffView(JComponent currentDiffView) {
        delegatingUndoRedo.setDiffView(currentDiffView);
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return getToolbar();
    }

    private Toolbar getToolbar() {
        return toolBar;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        
    }

    @NbBundle.Messages({
        "MSG_SaveModified=File {0} is modified. Save?"
    })
    @Override
    public CloseOperationState canCloseElement() {
        if(files.length == 0) {
            return CloseOperationState.STATE_OK;
        }
        FileObject fo = FileUtil.toFileObject(files[0]);
        if(fo != null) {
            final DataObject dataObject;
            try {
                dataObject = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                History.LOG.log(Level.WARNING, null, ex);
                return CloseOperationState.STATE_OK;
            }
            if(dataObject != null && dataObject.isModified()) {
                AbstractAction save = new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SaveCookie sc = dataObject.getLookup().lookup(SaveCookie.class);
                        if(sc != null) {
                            try {
                                sc.save();
                            } catch (IOException ex) {
                                History.LOG.log(Level.WARNING, null, ex);
                            }
                        }
                    }
                };
                save.putValue(Action.LONG_DESCRIPTION, Bundle.MSG_SaveModified(dataObject.getPrimaryFile().getNameExt()));
                return MultiViewFactory.createUnsafeCloseState("editor", save, null);
            }
        }    
        return CloseOperationState.STATE_OK;
    }

    String getActiveFilterValue() {
        return getToolbar().containsField.isVisible() ? getToolbar().containsField.getText() : null;
    }

    @Override
    public void componentClosed() {
        Utils.removePropertyChangeListener(this);
        if(masterView != null) {
            masterView.close();
        }
    }
    
    @Override
    public void componentActivated() {
        if(masterView != null) {
            masterView.requestActive();
        }
    }

    private void onFilterChange() {
        Filter filter = getSelectedFilter();
        getToolbar().containsLabel.setVisible(filter instanceof ByUserFilter || filter instanceof ByMsgFilter);
        getToolbar().containsField.setVisible(filter instanceof ByUserFilter || filter instanceof ByMsgFilter);
        masterView.setFilter(getSelectedFilter());
        getToolbar().containsField.requestFocus();
    }

    @Override
    public Action[] getActions() {
        return new Action[0]; // XXX
    }

    @Override
    public Lookup getLookup() {
        if(lookup == null) {
            lookup = new ProxyLookup(new Lookup[] {
                Lookups.fixed((Object[]) files),
                new AbstractLookup(activatedNodesContent)
            });
        }
        return lookup;
    }

    @Override
    public void componentOpened() {}

    @Override
    public void componentShowing() {}

    @Override
    public void componentHidden() {}

    @Override
    public void componentDeactivated() {}
        
    private class Toolbar extends JToolBar implements ActionListener {
        private JButton nextButton;
        private JButton prevButton;
        private JButton refreshButton;
        private JButton settingsButton;
        private JLabel filterLabel;
        private JComboBox filterCombo;
        private JLabel containsLabel;
        private JTextField containsField;
        private ShowHistoryAction showHistoryAction;
        private final LinkButton searchHistoryButton;
        private final Separator separator1;
        private final Separator separator2;
        
        private Toolbar(VersioningSystem vs, final File... files) {
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setOpaque(false);
            setFloatable(false);
            
            containsLabel = new JLabel(NbBundle.getMessage(HistoryComponent.class, "LBL_Contains"));  // NOI18N
            containsLabel.setVisible(false);
            containsField = new JTextField();  
            containsField.setPreferredSize(new Dimension(150, containsField.getPreferredSize().height));
            containsField.setMaximumSize(new Dimension(150, containsField.getPreferredSize().height));
            containsField.setMinimumSize(new Dimension(150, containsField.getPreferredSize().height));
            containsField.getDocument().addDocumentListener(new ContainsListener());
            containsField.setVisible(false);
            
            filterLabel = new JLabel(NbBundle.getMessage(HistoryComponent.class, "LBL_Filter"));  // NOI18N
            filterCombo = new FilterCombo();
            filterCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if(value instanceof Filter) {
                        return super.getListCellRendererComponent(list, ((Filter) value).getDisplayName(), index, isSelected, cellHasFocus);
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
            filterCombo.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    onFilterChange();
                }
            });
            
            nextButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/ui/resources/icons/diff-next.png"))); 
            prevButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/ui/resources/icons/diff-prev.png"))); 
            nextButton.addActionListener(this);
            prevButton.addActionListener(this);
            refreshButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/ui/resources/icons/refresh.png"))); 
            refreshButton.addActionListener(this);
            settingsButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/ui/resources/icons/options.png"))); 
            settingsButton.addActionListener(this);
            showHistoryAction = new ShowHistoryAction();
            searchHistoryButton = new LinkButton(); // NOI18N
            searchHistoryButton.addActionListener(showHistoryAction);
            
            separator1 = new JToolBar.Separator();
            separator1.setOrientation(SwingConstants.VERTICAL);
            separator2 = new JToolBar.Separator();
            separator2.setOrientation(SwingConstants.VERTICAL);
            
            setup(vs);
            
            nextButton.setBorder(new EmptyBorder(0, 5, 0, 5));
            prevButton.setBorder(new EmptyBorder(0, 5, 0, 5));
            refreshButton.setBorder(new EmptyBorder(0, 0, 0, 0));
            filterLabel.setBorder(new EmptyBorder(0, 15, 0, 5));
            filterCombo.setBorder(new EmptyBorder(0, 5, 0, 5));
            containsLabel.setBorder(new EmptyBorder(0, 5, 0, 10));
            settingsButton.setBorder(new EmptyBorder(0, 5, 0, 10));

            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            add(nextButton); 
            add(prevButton); 
            
            add(separator1);
            
            add(refreshButton); 
            add(filterLabel); 
            add(filterCombo); 
            add(containsLabel); 
            add(containsField); 
            
            add(separator2);
            
            add(settingsButton);
            
            JPanel placeholder = new JPanel();
            placeholder.setOpaque(false);
            add(placeholder);   
            
            add(searchHistoryButton);             
        }

        void setup(VersioningSystem vs) {
            boolean visible = vs != null && vs.getVCSHistoryProvider() != null;
            if(visible) { 
                searchHistoryButton.setText(NbBundle.getMessage(this.getClass(), "LBL_ShowVersioningHistory", new Object[] {vs.getProperty(VersioningSystem.PROP_DISPLAY_NAME)}));
                Filter[] filters = new Filter[] {
                    new AllFilter(), 
                    new VCSFilter((String) vs.getProperty(VersioningSystem.PROP_DISPLAY_NAME)), 
                    new LHFilter(),
                    new ByUserFilter(),
                    new ByMsgFilter()};
                filterCombo.setModel(new DefaultComboBoxModel(filters));   
                showHistoryAction.delegate = vs.getVCSHistoryProvider().createShowHistoryAction(files);
            }
            filterCombo.setVisible(visible);
            filterLabel.setVisible(visible);
            searchHistoryButton.setVisible(visible);
            refreshButton.setVisible(visible);
            settingsButton.setVisible(visible);
            separator1.setVisible(visible);
            separator2.setVisible(visible);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == getToolbar().nextButton) {
                diffView.onNextButton();
            } else if(e.getSource() == getToolbar().prevButton) {
                diffView.onPrevButton();
            } else if(e.getSource() == getToolbar().refreshButton) {
                masterView.refresh();
            } else if(e.getSource() == getToolbar().settingsButton) {
                OptionsDisplayer.getDefault().open(OptionsDisplayer.ADVANCED + "/Versioning/" + HistoryOptions.OPTIONS_SUBPATH); // NOI18N
            }
        }
        
        private class FilterCombo extends JComboBox {
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(getComboWidth(), super.getPreferredSize().height);
            }
            private Filter[] getFilters() {
                Filter[] ret = new Filter[getModel().getSize()];
                for (int i = 0; i < ret.length; i++) {
                    Object e = getModel().getElementAt(i);
                    if(e instanceof Filter) {
                        ret[i] = (Filter) e;
                    }
                }
                return ret;
            }
            private int getComboWidth() {
                Filter[] filters = getFilters();
                FontMetrics fm = getFontMetrics(getFont());
                int ret = 150; // default min
                for (Filter filter : filters) {
                    int width = fm.stringWidth(filter.getDisplayName()) + 50;
                    if(width > 350) {
                        return 350;
                    }
                    if(width > ret) {
                        ret = width;
                    }
                }
                return ret;
            }
        }
        
        private class ShowHistoryAction implements ActionListener {
            private Action delegate;
            @Override
            public void actionPerformed(final ActionEvent e) {
                History.getInstance().getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        delegate.actionPerformed(e);
                    }
                }); 
            }
        }
    }

    void disableNavigationButtons() {
        getToolbar().prevButton.setEnabled(false);
        getToolbar().nextButton.setEnabled(false);
    }

    void refreshNavigationButtons(int currentDifference, int diffCount) {
        getToolbar().prevButton.setEnabled(currentDifference > 0);
        getToolbar().nextButton.setEnabled(currentDifference < diffCount - 1);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.localhistory.ui.view.LHHistoryTab");   // NO18N
    }

    private class AllFilter extends Filter {
        @Override
        public boolean accept(Object value) {
            return true;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HistoryComponent.class, "LBL_AllRevisionsFilter"); // NO18N   
        }
    }    
    private class VCSFilter extends Filter {
        private final String vcsName;
        public VCSFilter(String vscName) {
            this.vcsName = vscName;
        }
        @Override
        public boolean accept(Object value) {
            HistoryEntry e = getEntry(value);
            if(e != null) {
                if(!e.isLocalHistory()) return true;
            }
            return false;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HistoryComponent.class, "LBL_VCSRevisionsFilter", new Object[] {vcsName}); // NO18N
        }
    }    
    private class LHFilter extends Filter {
        @Override
        public boolean accept(Object value) {
            HistoryEntry e = getEntry(value);
            if(e != null) {
                if(e.isLocalHistory()) return true;
            }
            return false;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HistoryComponent.class, "LBL_LHRevisionsFilter"); // NO18N
        }
    }       
    private class ByUserFilter extends Filter {
        @Override
        public boolean accept(Object value) {
            String byUser = getToolbar().containsField.getText();
            if(byUser == null || "".equals(byUser)) return true;                // NOI18N
            
            HistoryEntry e = getEntry(value);
            if(e != null) {
                String user = e.getUsernameShort();
                if(user.toLowerCase().contains(byUser.toLowerCase())) return true;
            }
            return false;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HistoryComponent.class, "LBL_ByUserFilter"); // NO18N
        }
        @Override
        public String getRendererValue(String value) {
            return getFilteredRendererValue(value);
        }        
    }           
    private class ByMsgFilter extends Filter {
        @Override
        public boolean accept(Object value) {
            String byMsg = getToolbar().containsField.getText();
            if(byMsg == null || "".equals(byMsg)) return true;
            
            HistoryEntry e = getEntry(value);
            if(e != null) {
                String msg = e.getMessage();
                if(msg.toLowerCase().contains(byMsg.toLowerCase())) return true;
            }
            return false;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HistoryComponent.class, "LBL_ByMsgFilter"); // NO18N
        }

        @Override
        public String getRendererValue(String value) {
            return getFilteredRendererValue(value);
        }

    }           
    private String getFilteredRendererValue(String value) {
        String contains = getToolbar().containsField.getText();
        if(contains == null || "".equals(contains)) {
            return value;
        }            
        StringBuilder sb = new StringBuilder();
        sb.append(value.replace(contains, "<b>" + contains + "</b>")); // NOI18N
        return sb.toString();
    }
    
    private class ContainsListener implements DocumentListener, ActionListener { 
        private final Timer t;
        public ContainsListener() {
            t = new Timer(300, this);
            t.setRepeats(false);
        }
        @Override
        public void insertUpdate(DocumentEvent e) {
            t.start();
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
            t.start();
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
            t.start();
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            masterView.fireFilterChanged();
        }
    };    
}
