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
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.awt.UndoRedo;
import org.netbeans.modules.versioning.util.DelegatingUndoRedo;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.util.Utils;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.history.LinkButton;
import org.netbeans.modules.versioning.ui.options.HistoryOptions;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.swing.etable.QuickFilter;
import org.openide.cookies.SaveCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 * Top component which displays something.
 * 
 * @author Tomas Stupka
 */
@MultiViewElement.Registration(
        displayName="#CTL_SourceTabCaption",                                    // NOI18N
        // no icon
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID=HistoryComponent.PREFERRED_ID, 
        mimeType="",
        position=1000000 // lets leave some space in case somebody really wants to be the last
)
final public class HistoryComponent extends JPanel implements MultiViewElement, HelpCtx.Provider, PropertyChangeListener {

    private HistoryFileView masterView;
    static final String PREFERRED_ID = "text.history";                          // NOI18N
    private final DelegatingUndoRedo delegatingUndoRedo = new DelegatingUndoRedo(); 
    private Toolbar toolBar;
    private EmptyToolbar emptyToolbar;
    
    private HistoryDiffView diffView;
    
    private VCSFileProxy[] files;
    private InstanceContent activatedNodesContent;
    private ProxyLookup lookup;
    private Lookup context;
    private VersioningSystem versioningSystem;
    private MultiViewElementCallback callback;
    private JSplitPane splitPane;
    private NoContentPanel noContentPanel;
        
    public HistoryComponent() {
        initComponents();
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            setBackground(UIManager.getColor("NbExplorerView.background"));     // NOI18N
        }
        Utils.addPropertyChangeListener(this);
        activatedNodesContent = new InstanceContent();
    }
    
    public HistoryComponent(File... files) {
        this();

        VCSFileProxy[] proxies = null;
        if(files != null && files.length > 0) {
            proxies = new VCSFileProxy[files.length];
            for (int i = 0; i < proxies.length; i++) {
                proxies[i] = VCSFileProxy.createFileProxy(files[i]);
            }
            this.files = proxies;
        }
        init(true);
    }
    
    public HistoryComponent(Lookup context) {
        this();
        
        this.context = context;
        DataObject dataObject = context.lookup(DataObject.class);
        List<VCSFileProxy> filesList = new LinkedList<VCSFileProxy>();
        if (dataObject instanceof DataShadow) {
            dataObject = ((DataShadow) dataObject).getOriginal();
        }
        if (dataObject != null) {
            Collection<VCSFileProxy> doFiles = toFileCollection(dataObject.files());
            filesList.addAll(doFiles);
        }
        this.files = filesList.toArray(new VCSFileProxy[filesList.size()]);
        init(false);
    }
    
    private Collection<VCSFileProxy> toFileCollection(Collection<? extends FileObject> fileObjects) {
        Set<VCSFileProxy> ret = new HashSet<VCSFileProxy>(fileObjects.size());
        for (FileObject fo : fileObjects) {
            ret.add(VCSFileProxy.createFileProxy(fo));
        }
        ret.remove(null);
        return ret;
    }        

    private void init(boolean refresh) {   
        if(hasFiles()) {
            this.versioningSystem = hasFiles() ? Utils.getOwner(files[0]) : null;
            History.LOG.log(Level.FINE, "owner of {0} is {1}", new Object[]{files[0], versioningSystem != null ? versioningSystem.getDisplayName() : null}); // NOI18N
            if(!hasHistory()) {
                noHistoryAvailable();
                return;
            }
        } else {
            noHistoryAvailable();
            return;
        }
        
        if(noContentPanel != null) {
            remove(noContentPanel);
        }
        if(splitPane == null) {
            splitPane = new JSplitPane();
            splitPane.setDividerLocation(150);
            splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            splitPane.setOneTouchExpandable(true);
            add(splitPane, BorderLayout.CENTER);
        }
        
        if(toolBar == null) {
            toolBar = new Toolbar(versioningSystem);
        } else {
            toolBar.setup(versioningSystem);
        }
        masterView = new HistoryFileView(files, versioningSystem, this);
        diffView = new HistoryDiffView(this); 
        
        masterView.getExplorerManager().addPropertyChangeListener(diffView); 
        masterView.getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
            private Node[] activatedNodes;
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {                            
                    Node[] newSelection = (Node[]) evt.getNewValue();
                    if(activatedNodes != null) {
                        for (Node n : activatedNodes) {
                            activatedNodesContent.remove(n);
                        }
                        activatedNodes = newSelection;
                        for (Node n : activatedNodes) {
                            activatedNodesContent.add(n);
                        }
                    }
                    if(newSelection != null) {
                        getToolbar().modeCombo.setEnabled(newSelection.length == 1);
                        getToolbar().modeLabel.setEnabled(newSelection.length == 1);
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
        if(Utils.EVENT_VERSIONED_ROOTS.equals(evt.getPropertyName()) && hasFiles()) {
            VersioningSystem vs = Utils.getOwner(files[0]);
            if(versioningSystem != vs ||
               hasLocalHistory != (History.getHistoryProvider(History.getInstance().getLocalHistory(files)) != null)) 
            {
                versioningSystem = vs;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        init(true);
                    }
                });
            }
        }
    }
    
    HistoryEntry getParentEntry(HistoryEntry entry) {
        return masterView.getParentEntry(entry);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
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
        if(hasHistory()) {
            return getToolbar();
        } else {
            if(emptyToolbar == null) {
                emptyToolbar = new EmptyToolbar();
            }
            return emptyToolbar;
        }
    }

    private Toolbar getToolbar() {
        return toolBar;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @NbBundle.Messages({
        "MSG_SaveModified=File {0} is modified. Save?"                          // NOI18N
    })
    @Override
    public CloseOperationState canCloseElement() {
        if(!hasFiles()) {
            return CloseOperationState.STATE_OK;
        }
        FileObject fo = files[0].toFileObject();
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
                return MultiViewFactory.createUnsafeCloseState("editor", save, null); // NOI18N
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
        Filter filter = (Filter) getToolbar().filterCombo.getSelectedItem();
        getToolbar().containsLabel.setVisible(filter instanceof ByUserFilter || filter instanceof ByMsgFilter);
        getToolbar().containsField.setVisible(filter instanceof ByUserFilter || filter instanceof ByMsgFilter);
        masterView.setFilter(filter);
        getToolbar().containsField.setText(""); // NOI18N
        getToolbar().containsField.requestFocus();
    }

    private void onModeChange() {
        diffView.modeChanged();
        if(masterView != null) {
            masterView.requestActive();
        }
    }
                
    @Override
    public Action[] getActions() {
        Action[] retValue;
        if (callback != null) {
            retValue = callback.createDefaultActions();
        } else {
            // fallback
            retValue = new Action[0];
        }
        return retValue;
    }

    @Override
    public Lookup getLookup() {
        if(lookup == null) {
            if(context != null) {
                lookup = new ProxyLookup(new Lookup[] {
                    context,
                    new AbstractLookup(activatedNodesContent)
                });
            } else {
                lookup = new ProxyLookup(new Lookup[] {
                    new AbstractLookup(activatedNodesContent)
                });
            }
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

    Node[] getSelectedNodes() {
        return masterView.getExplorerManager().getSelectedNodes();
    }

    private boolean hasFiles() {
        return files != null && files.length > 0;
    }

    private void noHistoryAvailable() throws MissingResourceException {
        if(noContentPanel == null) {
             noContentPanel = new NoContentPanel(NbBundle.getMessage(HistoryComponent.class, "MSG_NO_HISTORY"));
        }
        add(noContentPanel); 
    }

    private boolean hasLocalHistory;
    private boolean hasHistory() {
        if(hasFiles()) {
            hasLocalHistory = History.getHistoryProvider(History.getInstance().getLocalHistory(files)) != null;
            return hasFiles() && (hasLocalHistory || History.getHistoryProvider(versioningSystem) != null);
        } 
        return false;
    }

    private class EmptyToolbar extends JToolBar  {
        private EmptyToolbar() {
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setOpaque(false);
            setFloatable(false);
        }    
    }
    
    private class Toolbar extends JToolBar implements ActionListener {
        private JButton nextButton;
        private JButton prevButton;
        private JButton refreshButton;
        private JButton settingsButton;
        private JLabel modeLabel;
        private JLabel filterLabel;
        private JComboBox filterCombo;
        private JComboBox modeCombo;
        private JLabel containsLabel;
        private JTextField containsField;
        private ShowHistoryAction showHistoryAction;
        private final LinkButton searchHistoryButton;
        private final Separator separator1;
        private final Separator separator2;
        private final Separator separator3;
        
        private Toolbar(VersioningSystem vs) {
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
            
            modeLabel = new JLabel(NbBundle.getMessage(HistoryComponent.class, "LBL_CompareMode"));  // NOI18N
            modeCombo = new ValueSizedCombo();
            modeCombo.setModel(new DefaultComboBoxModel(new Object[] {CompareMode.TOCURRENT, CompareMode.TOPARENT}));
            modeCombo.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    onModeChange();
                }
            });
            
            filterLabel = new JLabel(NbBundle.getMessage(HistoryComponent.class, "LBL_Filter"));  // NOI18N
            filterCombo = new ValueSizedCombo();
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
            
            nextButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/ui/resources/icons/diff-next.png"))); // NOI18N
            prevButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/ui/resources/icons/diff-prev.png"))); // NOI18N
            nextButton.addActionListener(this);
            prevButton.addActionListener(this);
            nextButton.setEnabled(false);
            prevButton.setEnabled(false);
            refreshButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/ui/resources/icons/refresh.png"))); // NOI18N
            refreshButton.addActionListener(this);
            settingsButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/ui/resources/icons/options.png"))); // NOI18N
            settingsButton.addActionListener(this);
            showHistoryAction = new ShowHistoryAction();
            searchHistoryButton = new LinkButton(); // NOI18N
            searchHistoryButton.addActionListener(showHistoryAction);
            
            separator1 = new JToolBar.Separator();
            separator1.setOrientation(SwingConstants.VERTICAL);
            separator2 = new JToolBar.Separator();
            separator2.setOrientation(SwingConstants.VERTICAL);
            separator3 = new JToolBar.Separator();
            separator3.setOrientation(SwingConstants.VERTICAL);
            
            setup(vs);
            
            nextButton.setBorder(new EmptyBorder(0, 5, 0, 5));
            prevButton.setBorder(new EmptyBorder(0, 5, 0, 5));
            refreshButton.setBorder(new EmptyBorder(0, 0, 0, 0));
            filterLabel.setBorder(new EmptyBorder(0, 15, 0, 5));
            filterCombo.setBorder(new EmptyBorder(0, 5, 0, 5));
            modeLabel.setBorder(new EmptyBorder(0, 15, 0, 5));
            modeCombo.setBorder(new EmptyBorder(0, 5, 0, 5));
            containsLabel.setBorder(new EmptyBorder(0, 5, 0, 10));
            settingsButton.setBorder(new EmptyBorder(0, 5, 0, 10));

            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            add(nextButton); 
            add(prevButton); 
            
            add(separator1);
            
            add(refreshButton); 
            add(separator2);
            add(filterLabel); 
            add(filterCombo); 
            add(containsLabel); 
            add(containsField); 
            add(modeLabel); 
            add(modeCombo); 
            
            add(separator3);
            
            add(settingsButton);
            
            JPanel placeholder = new JPanel();
            placeholder.setOpaque(false);
            add(placeholder);   
            
            add(searchHistoryButton);             
        }

        void setup(VersioningSystem vs) {
            boolean visible = vs != null && vs.getVCSHistoryProvider() != null;
            if(visible) { 
                searchHistoryButton.setText(NbBundle.getMessage(this.getClass(), "LBL_ShowVersioningHistory", new Object[] {vs.getDisplayName()})); // NOI18N
                Filter[] filters = new Filter[] {
                    new AllFilter(), 
                    new VCSFilter(vs.getDisplayName()), 
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
                if(lastDifference) {
                    masterView.selectNextEntry();
                } else {
                    diffView.onNextButton();
                }
            } else if(e.getSource() == getToolbar().prevButton) {
                if(firstDifference) {
                    diffView.onSelectionLastDifference();
                    masterView.selectPrevEntry();
                } else {
                    diffView.onPrevButton();
                }
            } else if(e.getSource() == getToolbar().refreshButton) {
                masterView.refresh();
            } else if(e.getSource() == getToolbar().settingsButton) {
                OptionsDisplayer.getDefault().open(OptionsDisplayer.ADVANCED + "/Versioning/" + HistoryOptions.OPTIONS_SUBPATH); // NOI18N
            }
        }
        
        private class ValueSizedCombo extends JComboBox {
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
            private Object[] getValues() {
                Object[] ret = new Object[getModel().getSize()];
                for (int i = 0; i < ret.length; i++) {
                    ret[i] = getModel().getElementAt(i);
                }
                return ret;
            }
            private int getComboWidth() {
                Object[] values = getValues();
                FontMetrics fm = getFontMetrics(getFont());
                int ret = 130; // default min
                for (Object value : values) {
                    int width = fm.stringWidth(value.toString()) + 60;
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
    
    CompareMode getMode() {
        return (CompareMode) getToolbar().modeCombo.getSelectedItem();
    }

    void disableNavigationButtons() {
        getToolbar().prevButton.setEnabled(false);
        getToolbar().nextButton.setEnabled(false);
    }

    private boolean lastDifference = false;
    private boolean firstDifference = true;
    void refreshNavigationButtons(int currentDifference, int diffCount) {
        firstDifference = currentDifference <= 0;
        lastDifference = currentDifference == diffCount - 1;
        
        if(masterView.isSingleSelection()) {
            getToolbar().prevButton.setEnabled(!(firstDifference && masterView.isFirstRow()));
            getToolbar().nextButton.setEnabled(!(lastDifference && masterView.isLastRow()));
        } else {
            getToolbar().prevButton.setEnabled(currentDifference > 0);
            getToolbar().nextButton.setEnabled(currentDifference < diffCount - 1);
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.localhistory.ui.view.LHHistoryTab");   // NO18N
    }

    public static abstract class Filter implements QuickFilter {
        public boolean filtersProperty(Property property) {
            return false;
        }
        public abstract String getDisplayName();
        protected HistoryEntry getEntry(Object value) {
            if(value instanceof Node) {
                return getHistoryEntry((Node)value);
        }
            return null;
        }
 
        private HistoryEntry getHistoryEntry(Node node) {
            HistoryEntry entry = node.getLookup().lookup(HistoryEntry.class);
            if(entry != null) {
                return entry;
            } else {
                Node[] nodes = node.getChildren().getNodes();
                return nodes != null && nodes.length > 0 ? getHistoryEntry(nodes[0]) : null;
            }
        }
        
        public String getRendererValue(String value) {
            return HistoryUtils.escapeForHTMLLabel(value);
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
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
            if(HistoryRootNode.isLoadNext(value) || HistoryRootNode.isWait(value)) return true;
            
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
        public boolean filtersProperty(Property property) {
            return property instanceof RevisionNode.UserProperty;
        }
                
        @Override
        public boolean accept(Object value) {
            if(HistoryRootNode.isLoadNext(value) || HistoryRootNode.isWait(value)) return true;
            
            String byUser = getToolbar().containsField.getText();
            if(byUser == null || "".equals(byUser)) return true;                // NOI18N
            
            HistoryEntry e = getEntry(value);
            if(e != null) {
                String user = e.getUsernameShort();
                return user.toLowerCase().contains(byUser.toLowerCase());
            }
            return true;
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
        public boolean filtersProperty(Property property) {
            return property instanceof RevisionNode.MessageProperty;
        }
        
        @Override
        public boolean accept(Object value) {
            if(HistoryRootNode.isLoadNext(value) || HistoryRootNode.isWait(value)) return true;
            
            String byMsg = getToolbar().containsField.getText();
            if(byMsg == null || "".equals(byMsg)) return true;
            
            HistoryEntry e = getEntry(value);
            if(e != null) {
                String msg = e.getMessage();
                return msg.toLowerCase().contains(byMsg.toLowerCase());
            }
            return true;
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
        if(contains == null || "".equals(contains)) { // NOI18N
            return HistoryUtils.escapeForHTMLLabel(value);
        }            
        
        StringBuilder sb = new StringBuilder();
        int startIdx = 0;
        int endIdx = value.indexOf(contains);
        if(endIdx < 0) {
            return HistoryUtils.escapeForHTMLLabel(value);
        }
        while(endIdx > -1 ) {
            String t = value.substring(startIdx, endIdx);
            sb.append(HistoryUtils.escapeForHTMLLabel(t)); 
            sb.append("<b>"); // NOI18N 
            sb.append(HistoryUtils.escapeForHTMLLabel(contains)); 
            sb.append("</b>"); // NOI18N
            startIdx = endIdx + contains.length();
            endIdx = value.indexOf(contains, endIdx + 1);
        }
        if(startIdx < value.length()) {
            String t = value.substring(startIdx, value.length());
            sb.append(HistoryUtils.escapeForHTMLLabel(t)); 
        }
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
    
    public enum CompareMode {
        TOPARENT(NbBundle.getMessage(HistoryComponent.class, "LBL_DiffToParent")),   // NOI18N
        TOCURRENT(NbBundle.getMessage(HistoryComponent.class, "LBL_DiffToCurrent")); // NOI18N
        private final String displayName;
        private CompareMode(String displayName) {
            this.displayName = displayName;
        }
        @Override
        public String toString() {
            return displayName;
        }
    }
        
}
