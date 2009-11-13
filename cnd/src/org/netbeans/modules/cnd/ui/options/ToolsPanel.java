/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.cnd.ui.options;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
import org.openide.windows.WindowManager;

/** Display the "Tools Default" panel */
public final class ToolsPanel extends JPanel implements ActionListener,
        ListSelectionListener, ItemListener {

    private static enum ValidState {
        VALID,
        INVALID,
        UNKNOWN
    }

    // The following are constants so I can do == rather than "equals"
    public static final String PROP_VALID = "valid"; // NOI18N
    private boolean initialized = false;
    private boolean changed;
    private boolean changingCompilerSet;
    private boolean updating;
    private ValidState valid = ValidState.UNKNOWN;
    private ToolsPanelModel model = null;
    private boolean customizeDebugger;
    private ExecutionEnvironment execEnv;
    private static ToolsPanel instance = null;
    private CompilerSetManager csm;
    private CompilerSet currentCompilerSet;
    private static final Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N
    private static final ToolsCacheManager cacheManager = new ToolsCacheManager();

    /** Creates new form ToolsPanel */
    public ToolsPanel() {
        initComponents();
        setName("TAB_ToolsTab"); // NOI18N (used as a pattern...)
        changed = false;
        instance = this;
        currentCompilerSet = null;
        if (cacheManager.isRemoteAvailable()) {
            execEnv = cacheManager.getDefaultHostEnvironment();
            btEditDevHost.setEnabled(true);
            cbDevHost.setEnabled(true);
        } else {
            execEnv = ExecutionEnvironmentFactory.getLocal();
        }

        lstDirlist.setCellRenderer(new MyCellRenderer());

        if ("Windows".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
            setOpaque(false);
        }

        HelpCtx.setHelpIDString(this, "ResolveBuildTools"); // NOI18N
    }

    public ToolsPanel(ToolsPanelModel model) {
        this();
        this.model = model;
    }

    private void initializeLong() {
        csm = cacheManager.getCompilerSetManagerCopy(execEnv, true);
    }

    ToolsPanelModel getModel(){
        return model;
    }

    ExecutionEnvironment getExecutionEnvironment(){
        return execEnv;
    }

    private void initializeUI() {
        if (instance == null) {
            instance = this;
        }
        changingCompilerSet = true;
        if (model == null) {
            model = new GlobalToolsPanelModel();
        }
        cbDevHost.removeItemListener(this);

        ExecutionEnvironment selectedEnv = model.getSelectedDevelopmentHost();
        ServerRecord selectedRec = null;

        Collection<? extends ServerRecord> hostList = cacheManager.getHosts();
        if (hostList != null) {
            cbDevHost.removeAllItems();
            for (ServerRecord rec : hostList) {
                if (rec.getExecutionEnvironment().equals(selectedEnv)) {
                    selectedRec = rec;
                }
                cbDevHost.addItem(rec);
            }
        } else {
            cbDevHost.addItem(ServerList.get(ExecutionEnvironmentFactory.getLocal()));
        }

        if (selectedRec != null) {
            cbDevHost.setSelectedItem(selectedRec);
        } else {
            cbDevHost.setSelectedItem(cacheManager.getDefaultHostRecord());
        }

        cbDevHost.setRenderer(new MyDevHostListCellRenderer());
        cbDevHost.addItemListener(this);
        cbDevHost.setEnabled(model.getEnableDevelopmentHostChange());
        btEditDevHost.setEnabled(model.getEnableDevelopmentHostChange());
        execEnv = getSelectedRecord().getExecutionEnvironment();
        btVersions.setEnabled(false);
        initCustomizableDebugger();

        getToolCollectionPanel().initializeUI();
    }

    private ToolCollectionPanel getToolCollectionPanel(){
        return (ToolCollectionPanel) toolCollectionPanel;
    }

    private void addCompilerSet() {
        if (csm == null) {
            // Compiler set manager is not initialized yet
            // (initializeLong still running). Stop here to avoid NPEs.
            return;
        }

        AddCompilerSetPanel panel = new AddCompilerSetPanel(csm);
        String title = isRemoteHostSelected() ? getString("NEW_TOOL_SET_TITLE_REMOTE", ExecutionEnvironmentFactory.toUniqueID(csm.getExecutionEnvironment())) : getString("NEW_TOOL_SET_TITLE");
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, title);
        panel.setDialogDescriptor(dialogDescriptor);
        boolean oldHostValid = cacheManager.isDevHostValid(execEnv);
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (dialogDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
            boolean newHostValid = cacheManager.isDevHostValid(execEnv);
            if (oldHostValid != newHostValid) {
                // we didn't add the collection, but host changed its valid state
                dataValid();
            }
            return;
        }

        final CompilerSet cs = panel.getCompilerSet();
        updating = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        RequestProcessor.getDefault().post(new Runnable(){
            public void run() {
                csm.add(cs);
                changed = true;
                SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        update(false, cs);
                    }
                });
            }
        });
    }

    private void duplicateCompilerSet() {
        CompilerSet selectedCompilerSet = (CompilerSet) lstDirlist.getSelectedValue();
        DuplicateCompilerSetPanel panel = new DuplicateCompilerSetPanel(csm, selectedCompilerSet);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, getString("COPY_TOOL_SET_TITLE"));
        panel.setDialogDescriptor(dialogDescriptor);
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (dialogDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
            return;
        }
        String compilerSetName = panel.getCompilerSetName().trim();
        CompilerSet cs = selectedCompilerSet.createCopy();
        cs.setName(compilerSetName);
        cs.unsetDefault();
        cs.setAutoGenerated(false);
        csm.add(cs);
        changed = true;
        update(false, cs);
    }

    private void onCompilerSetChanged() {
        boolean cbRemoveEnabled;
        if (model.showRequiredTools()) {
            cbRemoveEnabled = lstDirlist.getSelectedIndex() >= 0;
        } else {
            cbRemoveEnabled = csm.getCompilerSets().size() > 1 && lstDirlist.getSelectedIndex() >= 0;
        }
        changeCompilerSet((CompilerSet) lstDirlist.getSelectedValue());
        btAdd.setEnabled(isHostValidForEditing());
        btRemove.setEnabled(cbRemoveEnabled && isHostValidForEditing());
        btDuplicate.setEnabled(lstDirlist.getSelectedIndex() >= 0 && isHostValidForEditing());
        btDefault.setEnabled(lstDirlist.getSelectedIndex() >= 0 && !((CompilerSet) lstDirlist.getSelectedValue()).isDefault());
    }

    private void onNewDevHostSelected() {
        if (!execEnv.equals(getSelectedRecord().getExecutionEnvironment())) {
            log.fine("TP.itemStateChanged: About to update");
            changed = true;
            if (!cacheManager.hasCache()) {
                List<ServerRecord> nulist = new ArrayList<ServerRecord>(cbDevHost.getItemCount());
                for (int i = 0; i < cbDevHost.getItemCount(); i++) {
                    nulist.add((ServerRecord) cbDevHost.getItemAt(i));
                }
                cacheManager.setHosts(nulist);
            }
            cacheManager.setDefaultRecord((ServerRecord) cbDevHost.getSelectedItem());
            execEnv = getSelectedRecord().getExecutionEnvironment();
            model.setSelectedDevelopmentHost(execEnv);
            update(true);
        } else {
            update(false);
        }
    }

    private void removeCompilerSet() {
        CompilerSet cs = (CompilerSet) lstDirlist.getSelectedValue();
        if (cs != null) {
            int index = csm.getCompilerSets().indexOf(cs);
            csm.remove(cs);
            if (cs.isDefault()) {
                if (csm.getCompilerSets().size() > 0) {
                    csm.setDefault(csm.getCompilerSet(0));
                }
            }
            if (index >= 0 && index < csm.getCompilerSets().size()) {
                update(false, csm.getCompilerSets().get(index));
            } else if (index > 0) {
                update(false, csm.getCompilerSets().get(index - 1));
            } else {
                getToolCollectionPanel().removeCompilerSet();
                update(false);
            }
            changed = true;
        }
    }

    private void setSelectedAsDefault() {
        CompilerSet cs = (CompilerSet) lstDirlist.getSelectedValue();
        csm.setDefault(cs);
        changed = true;
        update(false);

    }

    public static boolean supportedMake(String name) {
        name = IpeUtils.getBaseName(name);
        return !name.toLowerCase().equals("mingw32-make.exe"); // NOI18N
    }

    /** Update the display */
    public void update() {
        update(true, null);
    }

    private void update(boolean doInitialize) {
        update(doInitialize, null);
    }

    /** Update the display */
    public void update(final boolean doInitialize, final CompilerSet selectedCS) {
        updating = true;
        if (!initialized || doInitialize) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            RequestProcessor.getDefault().post(new Runnable(){
                public void run() {
                     initializeLong();
                     SwingUtilities.invokeLater(new Runnable(){
                        public void run() {
                            initializeUI();
                            updateUI(doInitialize, selectedCS);
                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }
                     });
                }
            });
        } else {
            updateUI(doInitialize, selectedCS);
        }
    }

    private void updateUI(boolean doInitialize, CompilerSet selectedCS){
        getToolCollectionPanel().updateUI(doInitialize, selectedCS);

        if (doInitialize) {
            // Set Default
            if (!csm.getCompilerSets().isEmpty()) {
                if (csm.getDefaultCompilerSet() == null) {
                    String name = model.getCompilerSetName(); // the default set
                    if (name.length() == 0 || csm.getCompilerSet(name) == null) {
                        csm.setDefault(csm.getCompilerSet(0));
                    } else {
                        csm.setDefault(csm.getCompilerSet(name));
                    }
                }
                String selectedName = model.getSelectedCompilerSetName(); // The selected set
                if (selectedName != null) {
                    selectedCS = csm.getCompilerSet(selectedName);

                }
                if (selectedCS == null) {
                    selectedCS = csm.getDefaultCompilerSet();
                }
            }
        }

        if (selectedCS == null) {
            selectedCS = (CompilerSet) lstDirlist.getSelectedValue();
        }
        lstDirlist.setListData(csm.getCompilerSets().toArray());
        if (selectedCS != null) {
            lstDirlist.setSelectedValue(selectedCS, true); // FIXUP: should use name
        }
        if (lstDirlist.getSelectedIndex() < 0) {
            lstDirlist.setSelectedIndex(0);
        }
        lstDirlist.invalidate();
        lstDirlist.repaint();
        onCompilerSetChanged();
        updating = false;
        dataValid();
        initialized = true;
    }

    boolean isRemoteHostSelected() {
        return ((ServerRecord) cbDevHost.getSelectedItem()).isRemote();
    }

    private ServerRecord getSelectedRecord() {
        return (ServerRecord) cbDevHost.getSelectedItem();
    }

    private boolean isHostValidForEditing() {
        return true; //serverList == null ? true : serverList.get((String)cbDevHost.getSelectedItem()).isOnline();
    }

    private void changeCompilerSet(CompilerSet cs) {
        getToolCollectionPanel().preChangeCompilerSet(cs);
        if (cs == null) {
            String errorMsg = "";
            if (!cacheManager.isDevHostValid(execEnv)) {
                errorMsg = NbBundle.getMessage(ToolsPanel.class, "TP_ErrorMessage_BadDevHost", execEnv.toString());
            }
            lblErrors.setText("<html>" + errorMsg + "</html>"); //NOI18N
            updateToolsControls(false, false, false, true);
            return;
        }
        if (currentCompilerSet != null && currentCompilerSet != cs) {
            getToolCollectionPanel().updateCompilerSet(currentCompilerSet, false);
        }

        changingCompilerSet = true;
        getToolCollectionPanel().changeCompilerSet(cs);
        changingCompilerSet = false;
        currentCompilerSet = cs;
        fireCompilerSetChange();
        dataValid();
    }

    public void applyChanges(boolean force) {
        changed = force;
        applyChanges();
    }

    /** Apply changes */
    public void applyChanges() {
        if (changed || isChangedInOtherPanels()) {

            CompilerSet cs = (CompilerSet) lstDirlist.getSelectedValue();
            changed = false;
            if (cs != null) {
                getToolCollectionPanel().updateCompilerSet(cs, true);
                model.setCompilerSetName(csm.getDefaultCompilerSet().getName());
                model.setSelectedCompilerSetName(cs.getName());
            }
            currentCompilerSet = cs;
            cacheManager.applyChanges((ServerRecord) cbDevHost.getSelectedItem());
        }
        getToolCollectionPanel().applyChanges();
        instance = null; // remove the global instance
    }

    /** What to do if user cancels the dialog (nothing) */
    public void cancel() {
        cacheManager.clear();
        changed = false;
        instance = null; // remove the global instance
    }

    //TODO: get rid of this...
    public static ToolsPanel getToolsPanel() {
        return instance;
    }

    public static ToolsCacheManager getToolsCacheManager() {
        return cacheManager;
    }

    public CompilerSet getCurrentCompilerSet() {
        return currentCompilerSet;
    }

    CompilerSetManager getCompilerSetManager(){
        return csm;
    }

    boolean isUpdatindOrChangingCompilerSet(){
        return updating || changingCompilerSet;
    }

    /**
     * Lets NB know if the data in the panel is valid and OK should be enabled
     *
     * @return Returns true if all data is valid
     */
    public boolean dataValid() {
        if (csm.getCompilerSets().size() == 0) {
            if (valid != ValidState.INVALID) {
                valid = ValidState.INVALID;
                firePropertyChange(PROP_VALID, true, false);
            }
            return false;
        }
        if (updating || changingCompilerSet) {
            return true;
        } else {
            boolean csmValid = csm.getCompilerSets().size() > 0;
            boolean isToolsValid = getToolCollectionPanel().isToolsValid();
            boolean devhostValid = cacheManager.isDevHostValid(execEnv);

            if (csmValid && isToolsValid && devhostValid) {
                if (valid != ValidState.VALID) {
                    valid = ValidState.VALID;
                    firePropertyChange(PROP_VALID, false, true);
                }
            } else {
                if (valid != ValidState.INVALID) {
                    valid = ValidState.INVALID;
                    firePropertyChange(PROP_VALID, true, false);
                }
            }

            // post errors in error text area
            lblErrors.setText("<html>"); // NOI18N
            if (valid == ValidState.INVALID) {
                ArrayList<String> errors = new ArrayList<String>();
                if (!devhostValid) {
                    errors.add(NbBundle.getMessage(ToolsPanel.class, "TP_ErrorMessage_BadDevHost", execEnv.toString()));
                }
                getToolCollectionPanel().getErrors(errors);
                StringBuilder errorString = new StringBuilder();
                for (int i = 0; i < errors.size(); i++) {
                    errorString.append(errors.get(i));
                    if (i < errors.size() - 1) {
                        errorString.append("<br>"); // NOI18N
                    } // NOI18N
                }
                lblErrors.setText("<html>" + errorString.toString() + "</html>"); //NOI18N

                validate();
                repaint();
            } else {
                lblErrors.setText("");
            }

            boolean baseDirValid = getToolCollectionPanel().isBaseDirValid();
            boolean enableText = baseDirValid || (isRemoteHostSelected() && isHostValidForEditing());
            boolean enableBrowse = baseDirValid && !isRemoteHostSelected();
            boolean enableVersions = (baseDirValid || isRemoteHostSelected()) && isHostValidForEditing();
            updateToolsControls(enableText, enableBrowse, enableVersions, false);

            return valid == ValidState.VALID;
        }
    }

    private void updateToolsControls(boolean enableText, boolean enableBrowse, boolean enableVersions, boolean cleanText) {
        btVersions.setEnabled(enableVersions);
        getToolCollectionPanel().updateToolsControls(enableText, enableBrowse, enableVersions, cleanText);
    }

    /**
     * Lets caller know if any data has been changed.
     *
     * @return True if anything has been changed
     */
    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    static Set<ChangeListener> listenerChanged = new HashSet<ChangeListener>();

    public static void addCompilerSetChangeListener(ChangeListener l) {
        listenerChanged.add(l);
    }

    public static void removeCompilerSetChangeListener(ChangeListener l) {
        listenerChanged.remove(l);
    }

    public void fireCompilerSetChange() {
        ChangeEvent ev = new ChangeEvent(currentCompilerSet);
        for (ChangeListener l : listenerChanged) {
            l.stateChanged(ev);
        }
    }
    private final static Set<ChangeListener> listenerModified = new WeakSet<ChangeListener>();

    public static void addCompilerSetModifiedListener(ChangeListener l) {
        synchronized (listenerModified) {
            listenerModified.add(l);
        }
    }

    public static void removeCompilerSetModifiedListener(ChangeListener l) {
        synchronized (listenerModified) {
            listenerModified.remove(l);
        }
    }

    public void fireCompilerSetModified() {
        ChangeEvent ev = new ChangeEvent(currentCompilerSet);
        synchronized (listenerModified) {
            for (ChangeListener l : listenerModified) {
                l.stateChanged(ev);
            }
        }
    }
    private static final Set<IsChangedListener> listenerIsChanged = new WeakSet<IsChangedListener>();

    public static void addIsChangedListener(IsChangedListener l) {
        synchronized (listenerIsChanged) {
            listenerIsChanged.add(l);
        }
    }

    public static void removeIsChangedListener(IsChangedListener l) {
        synchronized (listenerIsChanged) {
            listenerIsChanged.remove(l);
        }
    }

    private boolean isChangedInOtherPanels() {
        boolean isChanged = false;
        synchronized (listenerIsChanged) {
            for (IsChangedListener l : listenerIsChanged) {
                if (l.isChanged()) {
                    isChanged = true;
                    break;
                }
            }
        }
        return isChanged;
    }

    // implement ActionListener
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o instanceof JButton) {
            if (o == btAdd) {
                addCompilerSet();
            } else if (o == btRemove) {
                removeCompilerSet();
            } else if (o == btDuplicate) {
                duplicateCompilerSet();
            } else if (o == btEditDevHost) {
                editDevHosts();
            } else if (o == btDefault) {
                setSelectedAsDefault();
            }
        }
    }

    // implemet ItemListener
    public void itemStateChanged(ItemEvent ev) {
        Object o = ev.getSource();
        if (!updating) {
            if (o == cbDevHost && ev.getStateChange() == ItemEvent.SELECTED) {
                onNewDevHostSelected();
            }
        }
    }

    // Implement List SelectionListener
    public void valueChanged(ListSelectionEvent ev) {

        if (!ev.getValueIsAdjusting() && !updating) { // we don't want the event until its finished
            if (ev.getSource() == lstDirlist) {
                onCompilerSetChanged();
            }
        }
    }

    /**
     * Show the Development Host Manager. Note that we assume serverList is non-null as the Edit
     * button should <b>never</b> be enabled if its null.
     */
    private void editDevHosts() {
        // Show the Dev Host Manager dialog
        if (ServerListUIEx.showServerListDialog(cacheManager, null)) {
            changed = true;
            cbDevHost.removeItemListener(this);
            log.fine("TP.editDevHosts: Removing all items from cbDevHost");
            cbDevHost.removeAllItems();
            log.fine("TP.editDevHosts: Adding " + cacheManager.getHosts().size() + " items to cbDevHost");
            for (ServerRecord rec : cacheManager.getHosts()) {
                log.fine("    Adding " + rec);
                cbDevHost.addItem(rec);
            }
            log.fine("TP.editDevHosts: cbDevHost has " + cbDevHost.getItemCount() + " items");
            log.fine("TP.editDevHosts: getDefaultHostRecord returns " + cacheManager.getDefaultHostRecord());
            cbDevHost.setSelectedItem(cacheManager.getDefaultHostRecord());
            cacheManager.ensureHostSetup(getSelectedRecord().getExecutionEnvironment());
            cbDevHost.addItemListener(this);
            onNewDevHostSelected();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lbToolCollections = new javax.swing.JLabel();
        buttomPanel = new javax.swing.JPanel();
        lblErrors = new javax.swing.JLabel();
        btVersions = new javax.swing.JButton();
        btRestore = new javax.swing.JButton();
        ToolSetPanel = new javax.swing.JPanel();
        spDirlist = new JScrollPane(lstDirlist);
        lstDirlist = new javax.swing.JList();
        buttonPanel = new javax.swing.JPanel();
        btAdd = new javax.swing.JButton();
        btAdd.addActionListener(this);
        btRemove = new javax.swing.JButton();
        btRemove.addActionListener(this);
        btDuplicate = new javax.swing.JButton();
        btDuplicate.addActionListener(this);
        btDefault = new javax.swing.JButton();
        btDefault.addActionListener(this);
        lbDevHost = new javax.swing.JLabel();
        cbDevHost = new javax.swing.JComboBox();
        cbDevHost.addItemListener(this);
        btEditDevHost = new javax.swing.JButton();
        btEditDevHost.addActionListener(this);
        toolCollectionPanel = new ToolCollectionPanel(this);

        setMinimumSize(new java.awt.Dimension(600, 400));
        setLayout(new java.awt.GridBagLayout());

        lbToolCollections.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_DirlistLabel").charAt(0));
        lbToolCollections.setLabelFor(spDirlist);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle"); // NOI18N
        lbToolCollections.setText(bundle.getString("LBL_DirlistLabel")); // NOI18N
        lbToolCollections.setToolTipText(bundle.getString("HINT_DirListLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 4);
        add(lbToolCollections, gridBagConstraints);
        lbToolCollections.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_DirlistLabel")); // NOI18N
        lbToolCollections.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_DirlistLabel")); // NOI18N

        buttomPanel.setOpaque(false);
        buttomPanel.setLayout(new java.awt.GridBagLayout());

        lblErrors.setForeground(new java.awt.Color(255, 51, 51));
        lblErrors.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblErrors.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.lblErrors.text")); // NOI18N
        lblErrors.setEnabled(false);
        lblErrors.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        buttomPanel.add(lblErrors, gridBagConstraints);
        lblErrors.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.lblErrors.AccessibleContext.accessibleName")); // NOI18N

        btVersions.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_Versions").charAt(0));
        btVersions.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btVersions.text")); // NOI18N
        btVersions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btVersionsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weightx = 1.0;
        buttomPanel.add(btVersions, gridBagConstraints);
        btVersions.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btVersions.AccessibleContext.accessibleDescription")); // NOI18N

        btRestore.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_RestoreDefault_BT").charAt(0));
        btRestore.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btRestore.text")); // NOI18N
        btRestore.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        btRestore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRestoreActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        buttomPanel.add(btRestore, gridBagConstraints);
        btRestore.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btRestore.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(buttomPanel, gridBagConstraints);

        ToolSetPanel.setOpaque(false);
        ToolSetPanel.setLayout(new java.awt.GridBagLayout());

        spDirlist.setMinimumSize(new java.awt.Dimension(180, 20));
        spDirlist.setPreferredSize(new java.awt.Dimension(180, 20));

        lstDirlist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstDirlist.setAutoscrolls(false);
        lstDirlist.addListSelectionListener(this);
        spDirlist.setViewportView(lstDirlist);
        lstDirlist.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.lstDirlist.AccessibleContext.accessibleName")); // NOI18N
        lstDirlist.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.lstDirlist.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        ToolSetPanel.add(spDirlist, gridBagConstraints);

        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new java.awt.GridBagLayout());

        btAdd.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_AddButton").charAt(0));
        btAdd.setText(bundle.getString("LBL_AddButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        buttonPanel.add(btAdd, gridBagConstraints);
        btAdd.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_AddButton")); // NOI18N
        btAdd.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_AddButton")); // NOI18N

        btRemove.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_RemoveButton").charAt(0));
        btRemove.setText(bundle.getString("LBL_RemoveButton")); // NOI18N
        btRemove.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        buttonPanel.add(btRemove, gridBagConstraints);
        btRemove.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_RemoveButton")); // NOI18N
        btRemove.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_RemoveButton")); // NOI18N

        btDuplicate.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_UpButton").charAt(0));
        btDuplicate.setText(bundle.getString("LBL_UpButton")); // NOI18N
        btDuplicate.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        buttonPanel.add(btDuplicate, gridBagConstraints);
        btDuplicate.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_UpButton")); // NOI18N
        btDuplicate.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_UpButton")); // NOI18N

        btDefault.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_DownButton").charAt(0));
        btDefault.setText(bundle.getString("LBL_DownButton")); // NOI18N
        btDefault.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        buttonPanel.add(btDefault, gridBagConstraints);
        btDefault.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_DownButton")); // NOI18N
        btDefault.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_DownButton")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        ToolSetPanel.add(buttonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridheight = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(ToolSetPanel, gridBagConstraints);

        lbDevHost.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_DevelopmentHosts").charAt(0));
        lbDevHost.setLabelFor(cbDevHost);
        lbDevHost.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "LBL_DevelopmentHosts")); // NOI18N
        lbDevHost.setToolTipText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "HINT_DevelopmentHosts")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        add(lbDevHost, gridBagConstraints);

        cbDevHost.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(cbDevHost, gridBagConstraints);

        btEditDevHost.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_AddDevHost").charAt(0));
        btEditDevHost.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "Lbl_AddDevHost")); // NOI18N
        btEditDevHost.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btEditDevHost, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(toolCollectionPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void btVersionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btVersionsActionPerformed
    btVersions.setEnabled(false);
    final CompilerSet set = currentCompilerSet;
    if (set == null) {
        return;
    }

    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    RequestProcessor.getDefault().post(new Runnable() {

        public void run() {
            String versions = getToolCollectionPanel().getVersion(set);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    btVersions.setEnabled(true);
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            });
            NotifyDescriptor nd = new NotifyDescriptor.Message(versions);
            nd.setTitle(getString("LBL_VersionInfo_Title")); // NOI18N
            DialogDisplayer.getDefault().notify(nd);
        }
    });
}//GEN-LAST:event_btVersionsActionPerformed

    private void initCustomizableDebugger() {
        ToolsPanelGlobalCustomizer customizer = Lookup.getDefault().lookup(ToolsPanelGlobalCustomizer.class);
        customizeDebugger = customizer == null ? true : customizer.isDebuggerCustomizable();
    }

    boolean isCustomizableDebugger() {
        return customizeDebugger;
    }

    static class MyCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            CompilerSet cs = (CompilerSet) value;
            if (cs != null && cs.isDefault()) {
                comp.setFont(comp.getFont().deriveFont(Font.BOLD));
            }
            return comp;
        }
    }

    static class MyDevHostListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            ServerRecord rec = (ServerRecord) value;
            label.setText(rec.getDisplayName());
            if (value != null && value.equals(cacheManager.getDefaultHostRecord())) {
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            }
            return label;
        }
    }

private void btRestoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRestoreActionPerformed
    if (csm == null) {
        // restore is available after long initialization
        return;
    }
    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
            getString("RESTORE_TXT"),
            getString("RESTORE_TITLE"),
            NotifyDescriptor.OK_CANCEL_OPTION);
    Object ret = DialogDisplayer.getDefault().notify(nd);
    if (ret != NotifyDescriptor.OK_OPTION) {
        return;
    }
    final CompilerSet selectedCS[] = new CompilerSet[]{(CompilerSet) lstDirlist.getSelectedValue()};
    final AtomicBoolean cancelled = new AtomicBoolean(false);
    Runnable longTask = new Runnable() {

        public void run() {
            log.finest("Restoring defaults\n");
            ServerRecord record = ServerList.get(execEnv);
            if (record.isOffline()) {
                record.validate(true);
                if (record.isOffline()) {
                    cancelled.set(true);
                    return;
                }
            }
//            try {
//                ConnectionManager.getInstance().connectTo(execEnv);
//            } catch (IOException ex) {
//                //TODO: report it!
//                cancelled.set(true);
//                return;
//            } catch (CancellationException ex) {
//                cancelled.set(true);
//                return;
//            }
            CompilerSetManager newCsm = CompilerSetManager.create(execEnv);
            newCsm.initialize(false, true);
            while (newCsm.isPending()) {
                log.finest("\twaiting for compiler manager to initialize...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    cancelled.set(true);
                    return;
                }
            }

            cacheManager.addCompilerSetManager(newCsm);
            List<CompilerSet> list = csm.getCompilerSets();
            for (CompilerSet cs : list) {
                if (!cs.isAutoGenerated()) {
                    String name = cs.getName();
                    String newName = newCsm.getUniqueCompilerSetName(name);
                    if (!name.equals(newName)) {
                        // FIXUP: show a dialog with renamed custom sets. Can't do now because of UI freeze.
                        cs.setName(newName);
                    }
                    newCsm.add(cs);
                }
            }
            String defaultName = null;
            CompilerSet defaultCS = csm.getDefaultCompilerSet();
            if (defaultCS != null) {
                defaultName = defaultCS.getName();
            }
            String selectedName = null;
            if (selectedCS[0] != null) {
                selectedName = selectedCS[0].getName();
            }
            csm = newCsm;
            CompilerSet defaultCompilerSet = csm.getCompilerSet(defaultName);
            if (defaultCompilerSet != null) {
                csm.setDefault(defaultCompilerSet);
            }
            if (selectedName != null) {
                selectedCS[0] = csm.getCompilerSet(selectedName);
            }
            if (execEnv.isLocal()) {
                WindowsSupport.getInstance().init();
            }
            log.finest("Restored defaults\n");
        }
    };
    Runnable postWork = new Runnable() {

        public void run() {
            if (!cancelled.get()) {
                changed = true;
                if (selectedCS[0] != null) {
                    update(false, selectedCS[0]);
                } else {
                    update(false);
                }
            }
        }
    };
    final Frame mainWindow = WindowManager.getDefault().getMainWindow();
    String title = getString("TITLE_Configure");
    String msg = getString("MSG_Configure_Compiler_Sets", execEnv.toString());
    ModalMessageDlg.runLongTask(mainWindow, longTask, postWork, null, title, msg);
}//GEN-LAST:event_btRestoreActionPerformed

    static String getString(String key) {
        return NbBundle.getMessage(ToolsPanel.class, key);
    }

    static String getString(String key, Object param) {
        return NbBundle.getMessage(ToolsPanel.class, key, param);
    }

    static String getString(String key, Object param1, Object param2) {
        return NbBundle.getMessage(ToolsPanel.class, key, param1, param2);
    }

    static String getString(String key, Object param1, Object param2, Object param3) {
        return NbBundle.getMessage(ToolsPanel.class, key, param1, param2, param3);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ToolSetPanel;
    private javax.swing.JButton btAdd;
    private javax.swing.JButton btDefault;
    private javax.swing.JButton btDuplicate;
    private javax.swing.JButton btEditDevHost;
    private javax.swing.JButton btRemove;
    private javax.swing.JButton btRestore;
    private javax.swing.JButton btVersions;
    private javax.swing.JPanel buttomPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JComboBox cbDevHost;
    private javax.swing.JLabel lbDevHost;
    private javax.swing.JLabel lbToolCollections;
    private javax.swing.JLabel lblErrors;
    private javax.swing.JList lstDirlist;
    private javax.swing.JScrollPane spDirlist;
    private javax.swing.JPanel toolCollectionPanel;
    // End of variables declaration//GEN-END:variables
}
