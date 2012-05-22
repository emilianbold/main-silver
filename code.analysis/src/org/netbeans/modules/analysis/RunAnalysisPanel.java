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
package org.netbeans.modules.analysis;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.analysis.spi.Analyzer.AnalyzerFactory;
import org.netbeans.modules.analysis.spi.Analyzer.Context;
import org.netbeans.modules.analysis.spi.Analyzer.MissingPlugin;
import org.netbeans.modules.analysis.spi.Analyzer.WarningDescription;
import org.netbeans.modules.analysis.ui.AdjustConfigurationPanel;
import org.netbeans.modules.analysis.ui.ConfigurationsComboModel;
import org.netbeans.modules.refactoring.api.Scope;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 *
 * @author lahvac
 */
public final class RunAnalysisPanel extends javax.swing.JPanel implements LookupListener {

    private static final String COMBO_PROTOTYPE = "999999999999999999999999999999999999999999999999999999999999";
    private final JPanel progress;
    private final RequiredPluginsPanel requiredPlugins;
    private       Collection<? extends AnalyzerFactory> analyzers;
    private final Lookup.Result<AnalyzerFactory> analyzersResult;
    private final Map<String, AnalyzerAndWarning> warningId2Description = new HashMap<String, AnalyzerAndWarning>();
    private final JButton runAnalysis;

    public RunAnalysisPanel(ProgressHandle handle, Lookup context, JButton runAnalysis) {
        this(handle, context, runAnalysis, null);
    }
    
    public RunAnalysisPanel(ProgressHandle handle, Lookup context, JButton runAnalysis, DialogState state) {
        this.runAnalysis = runAnalysis;
        this.analyzersResult = Lookup.getDefault().lookupResult(AnalyzerFactory.class);
        this.analyzersResult.addLookupListener(this);
        
        initComponents();

        List<ScopeDescription> scopes = new ArrayList<ScopeDescription>();
        Icon currentProjectIcon = null;
        NonRecursiveFolder pack = context.lookup(NonRecursiveFolder.class);
        FileObject currentFile = context.lookup(FileObject.class);

        if (currentFile != null && currentFile.isData()) {
            scopes.add(new FileScopeDescription(currentFile));
        }
        
        if (pack != null && currentFile == null) {
            currentFile = pack.getFolder();
        }
        
        if (currentFile != null) {
            Project p = FileOwnerQuery.getOwner(currentFile);

            if (p != null) {
                ProjectInformation pi = ProjectUtils.getInformation(p);

                scopes.add(0, new CurrentProjectScopeDescription(p));
                currentProjectIcon = pi.getIcon();

                if (pack == null) {
                    ClassPath bootCP = ClassPath.getClassPath(currentFile, ClassPath.BOOT);

                    if (bootCP != null) {
                        final FileObject packFO = currentFile.getParent();
                        pack = new NonRecursiveFolder() {
                            @Override public FileObject getFolder() {
                                return packFO;
                            }
                        };
                    }
                }
            }
        } else {
            Project selected = context.lookup(Project.class);

            if (selected == null) {
                SourceGroup sg = context.lookup(SourceGroup.class);

                if (sg != null) {
                    selected = FileOwnerQuery.getOwner(sg.getRootFolder());
                }
            }

            if (selected == null) {
                DataFolder df = context.lookup(DataFolder.class);

                if (df != null) {
                    selected = FileOwnerQuery.getOwner(df.getPrimaryFile());
                }
            }

            if (selected != null) {
                ProjectInformation pi = ProjectUtils.getInformation(selected);

                scopes.add(0, new CurrentProjectScopeDescription(selected));
                currentProjectIcon = pi.getIcon();
            }
        }

        if (pack != null) {
            ClassPath source = ClassPath.getClassPath(pack.getFolder(), ClassPath.SOURCE);

            if (source != null) {
                String packName = source.getResourceName(pack.getFolder());

                scopes.add(1, new PackageScopeDescription(pack, packName));
            }
        }

        scopes.add(0, new AllProjectsScopeDescription(currentProjectIcon));

        scopeCombo.setModel(new DefaultComboBoxModel(scopes.toArray(new ScopeDescription[0])));
        scopeCombo.setRenderer(new ScopeRenderer());
        scopeCombo.setSelectedIndex(scopes.size() - 1);

        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        progress = new JPanel(new CardLayout());
        progress.add(new JPanel(), "empty");
        progress.add(ProgressHandleFactory.createProgressComponent(handle), "progress");
        progress.add(requiredPlugins = new RequiredPluginsPanel(), "plugins");
        add(progress, gridBagConstraints);
        ((CardLayout) progress.getLayout()).show(progress, "empty");

        if (state == null) state = DialogState.load();
        updateConfigurations(state);
        updateEnableDisable();

        setBorder(new EmptyBorder(12, 12, 12, 12));
        
        ConfigurationsManager.getDefault().addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                resultChanged(null);
            }
        });
        
        configurationCombo.addActionListener(new ActionListener() {
            Object currentItem = configurationCombo.getSelectedItem();
            @Override
            public void actionPerformed(ActionEvent e) {
                Object tempItem = configurationCombo.getSelectedItem();
                if (tempItem instanceof String) {
                    configurationCombo.setSelectedItem(currentItem);
                } else {
                    currentItem = tempItem;
                }
            }
        });
        
        inspectionCombo.addActionListener(new ActionListener() {
            Object currentItem = inspectionCombo.getSelectedItem();
            @Override
            public void actionPerformed(ActionEvent e) {
                Object tempItem = inspectionCombo.getSelectedItem();
                if (!(tempItem instanceof AnalyzerAndWarning)) {
                    inspectionCombo.setSelectedItem(currentItem);
                } else {
                    currentItem = tempItem;
                }
            }
        });
    }

    void started() {
        ((CardLayout) progress.getLayout()).show(progress, "progress");
        progress.invalidate();

        //disable all elements in the dialog:
        List<JComponent> todo = new LinkedList<JComponent>();

        todo.add(this);

        while (!todo.isEmpty()) {
            JComponent c = todo.remove(0);

            if (c == progress) continue;

            c.setEnabled(false);

            for (Component child : c.getComponents()) {
                if (child instanceof JComponent) todo.add((JComponent) child);
            }
        }
    }

    @Messages({"LBL_Predefined=Predefined", "LBL_Custom=Custom"})
    public void updateConfigurations(DialogState state) {
        analyzers = analyzersResult.allInstances();
        
        Object selectedConfiguration = null;
        DefaultComboBoxModel configurationModel = new DefaultComboBoxModel();
        configurationModel.addElement(Bundle.LBL_Predefined());
        configurationModel.addElement(null);
        
        for (AnalyzerFactory analyzer : analyzers) {
            if (SPIAccessor.ACCESSOR.getAnalyzerId(analyzer).equals(state.selectedAnalyzer)) {
                selectedConfiguration = analyzer;
            }
            configurationModel.addElement(analyzer);
        }

        configurationModel.addElement(Bundle.LBL_Custom());

        for (Configuration c : ConfigurationsManager.getDefault().getConfigurations()) {
            if (c.id().equals(state.selectedConfiguration)) {
                selectedConfiguration = c;
            }
            configurationModel.addElement(c);
        }

        configurationCombo.setModel(configurationModel);
        configurationCombo.setSelectedItem(selectedConfiguration);

        configurationRadio.setSelected(state.configurationsSelected);
        configurationCombo.setRenderer(new ConfigurationRenderer(true));

        DefaultComboBoxModel inspectionModel = new DefaultComboBoxModel();
        AnalyzerAndWarning firstInspection = null;
        AnalyzerAndWarning preselectInspection = null;

        for (AnalyzerFactory a : analyzers) {
            inspectionModel.addElement(SPIAccessor.ACCESSOR.getAnalyzerDisplayName(a));

            Map<String, Collection<WarningDescription>> cat2Warnings = new TreeMap<String, Collection<WarningDescription>>();

            for (WarningDescription wd : a.getWarnings()) {
                String cat = SPIAccessor.ACCESSOR.getWarningCategoryDisplayName(wd); //TODO: should be based on the id rather than on the display name
                Collection<WarningDescription> warnings = cat2Warnings.get(cat);

                if (warnings == null) {
                    cat2Warnings.put(cat, warnings = new TreeSet<WarningDescription>(new Comparator<WarningDescription>() {
                        @Override public int compare(WarningDescription o1, WarningDescription o2) {
                            return SPIAccessor.ACCESSOR.getWarningDisplayName(o1).compareToIgnoreCase(SPIAccessor.ACCESSOR.getWarningDisplayName(o2));
                        }
                    }));
                }

                warnings.add(wd);
            }

            for (Entry<String, Collection<WarningDescription>> catE : cat2Warnings.entrySet()) {
                inspectionModel.addElement("  " + catE.getKey());

                for (WarningDescription wd : catE.getValue()) {
                    AnalyzerAndWarning aaw = new AnalyzerAndWarning(a, wd);
                    inspectionModel.addElement(aaw);
                    warningId2Description.put(SPIAccessor.ACCESSOR.getWarningId(wd), aaw);
                    
                    if (firstInspection == null) firstInspection = aaw;
                    if (SPIAccessor.ACCESSOR.getWarningId(wd).equals(state.selectedInspection)) {
                        preselectInspection = aaw;
                    }
                }
            }
        }

        inspectionCombo.setModel(inspectionModel);
        inspectionCombo.setRenderer(new InspectionRenderer());
        inspectionCombo.setSelectedItem(preselectInspection != null ? preselectInspection : firstInspection);
        singleInspectionRadio.setSelected(!state.configurationsSelected);

        updatePlugins();
    }

    private void updatePlugins() {
        Collection<? extends AnalyzerFactory> toRun;

        if (!(configurationCombo.getSelectedItem() instanceof AnalyzerFactory)) {
            toRun = analyzers;
        } else {
            toRun = Collections.singleton((AnalyzerFactory) configurationCombo.getSelectedItem());
        }

        Context ctx = SPIAccessor.ACCESSOR.createContext(null, null, null, null, -1, -1);
        Set<MissingPlugin> plugins = new HashSet<MissingPlugin>();
        boolean someOk = false;

        for (AnalyzerFactory a : toRun) {
            Collection<? extends MissingPlugin> req = a.requiredPlugins(ctx);
            plugins.addAll(req);
            someOk |= req.isEmpty();
        }

        if (plugins.isEmpty()) {
            ((CardLayout) progress.getLayout()).show(progress, "empty");
        } else {
            requiredPlugins.setRequiredPlugins(plugins, !someOk);
            ((CardLayout) progress.getLayout()).show(progress, "plugins");
        }
        
        runAnalysis.setEnabled(someOk);
    }

    public Scope getSelectedScope(AtomicBoolean cancel) {
        return ((ScopeDescription) scopeCombo.getSelectedItem()).getScope(cancel);
    }
    
    public AnalyzerFactory getSelectedAnalyzer() {
        if (!(configurationCombo.getSelectedItem() instanceof AnalyzerFactory)) return null;
        return (AnalyzerFactory) configurationCombo.getSelectedItem();
    }

    public Configuration getConfiguration() {
        if (inspectionCombo.isEnabled()) return ConfigurationsManager.getDefault().getTemporaryConfiguration();
        Object selected = configurationCombo.getSelectedItem();

        if (selected instanceof Configuration) return (Configuration) selected;
        else return null;
    }

    public Collection<? extends AnalyzerFactory> getAnalyzers() {
        return analyzers;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        radioButtons = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        scopeCombo = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        configurationCombo = new javax.swing.JComboBox();
        manage = new javax.swing.JButton();
        configurationRadio = new javax.swing.JRadioButton();
        singleInspectionRadio = new javax.swing.JRadioButton();
        inspectionCombo = new javax.swing.JComboBox();
        browse = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(scopeCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabel2, gridBagConstraints);

        configurationCombo.setPrototypeDisplayValue(COMBO_PROTOTYPE);
        configurationCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(configurationCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(manage, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.manage.text")); // NOI18N
        manage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(manage, gridBagConstraints);

        radioButtons.add(configurationRadio);
        org.openide.awt.Mnemonics.setLocalizedText(configurationRadio, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.configurationRadio.text")); // NOI18N
        configurationRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationRadioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(configurationRadio, gridBagConstraints);

        radioButtons.add(singleInspectionRadio);
        org.openide.awt.Mnemonics.setLocalizedText(singleInspectionRadio, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.singleInspectionRadio.text")); // NOI18N
        singleInspectionRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                singleInspectionRadioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(singleInspectionRadio, gridBagConstraints);

        inspectionCombo.setPrototypeDisplayValue(COMBO_PROTOTYPE);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(inspectionCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browse, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.browse.text")); // NOI18N
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(browse, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void configurationComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationComboActionPerformed
        updatePlugins();
    }//GEN-LAST:event_configurationComboActionPerformed

    @Messages("LBL_Configurations=Configurations")
    private void manageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageActionPerformed
        AdjustConfigurationPanel panel = new AdjustConfigurationPanel(analyzers, null, null);
        DialogDescriptor nd = new DialogDescriptor(panel, Bundle.LBL_Configurations(), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null);

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            panel.save();
        }
    }//GEN-LAST:event_manageActionPerformed

    private void configurationRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationRadioActionPerformed
        updateEnableDisable();
    }//GEN-LAST:event_configurationRadioActionPerformed

    private void singleInspectionRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singleInspectionRadioActionPerformed
        updateEnableDisable();
    }//GEN-LAST:event_singleInspectionRadioActionPerformed

    @Messages("LBL_Browse=Browse Inspections")
    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        Object selectedInspection = inspectionCombo.getSelectedItem();
        AnalyzerFactory analyzerToSelect;
        String warningToSelect;
        
        if (selectedInspection instanceof AnalyzerAndWarning) {
            analyzerToSelect = ((AnalyzerAndWarning) selectedInspection).analyzer;
            warningToSelect = SPIAccessor.ACCESSOR.getWarningId(((AnalyzerAndWarning) selectedInspection).wd);
        } else {
            analyzerToSelect = null;
            warningToSelect = "";
        }

        AdjustConfigurationPanel panel = new AdjustConfigurationPanel(analyzers, analyzerToSelect, warningToSelect);
        DialogDescriptor nd = new DialogDescriptor(panel, Bundle.LBL_Browse(), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null);

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            inspectionCombo.setSelectedItem(warningId2Description.get(panel.getIdToRun()));
        }
    }//GEN-LAST:event_browseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browse;
    private javax.swing.JComboBox configurationCombo;
    private javax.swing.JRadioButton configurationRadio;
    private javax.swing.JComboBox inspectionCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton manage;
    private javax.swing.ButtonGroup radioButtons;
    private javax.swing.JComboBox scopeCombo;
    private javax.swing.JRadioButton singleInspectionRadio;
    // End of variables declaration//GEN-END:variables

    private void updateEnableDisable() {
        boolean configuration = configurationRadio.isSelected();

        configurationCombo.setEnabled(configuration);
        manage.setEnabled(configuration);
        inspectionCombo.setEnabled(!configuration);
        browse.setEnabled(!configuration);
    }

    String getSingleWarningId() {
        return inspectionCombo.isEnabled() ? SPIAccessor.ACCESSOR.getWarningId(((AnalyzerAndWarning) inspectionCombo.getSelectedItem()).wd) : null;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                updateConfigurations(getDialogState());
            }
        });
    }
    
    public DialogState getDialogState() {
        Object selectedConfiguration = configurationCombo.getSelectedItem();
        Object selectedInspection = inspectionCombo.getSelectedItem();
        return new DialogState(configurationRadio.isSelected(),
                               selectedConfiguration instanceof AnalyzerFactory ? SPIAccessor.ACCESSOR.getAnalyzerId((AnalyzerFactory) selectedConfiguration) : null,
                               selectedConfiguration instanceof Configuration ? ((Configuration) selectedConfiguration).id() : null,
                               selectedInspection instanceof AnalyzerAndWarning ? SPIAccessor.ACCESSOR.getWarningId(((AnalyzerAndWarning) selectedInspection).wd) : null);
    }

    public static final class ConfigurationRenderer extends DefaultListCellRenderer {

        private final boolean indent;

        public ConfigurationRenderer(boolean indent) {
            this.indent = indent;
        }

        @Messages({"LBL_RunAllAnalyzers=All Analyzers", "# {0} - the analyzer that should be run", "LBL_RunAnalyzer={0}"})
        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                value = Bundle.LBL_RunAllAnalyzers();
            } else if (value instanceof AnalyzerFactory) {
                value = Bundle.LBL_RunAnalyzer(SPIAccessor.ACCESSOR.getAnalyzerDisplayName((AnalyzerFactory) value));
            } else if (value instanceof Configuration) {
                value = ((Configuration) value).getDisplayName();
            } else if (value instanceof String) {
                setFont(getFont().deriveFont(Font.ITALIC));
                setText((String) value);
                setEnabled(false);
                setBackground(list.getBackground());
                setForeground(UIManager.getColor("Label.disabledForeground"));

                return this;
            }

            if (index == list.getModel().getSize()-5 && list.getModel() instanceof ConfigurationsComboModel && ((ConfigurationsComboModel) list.getModel()).canModify()) {
                setBorder(new Separator(list.getForeground()));
            } else {
                setBorder(null);
            }

            return super.getListCellRendererComponent(list, (indent ? "  " : "") + value, index, isSelected, cellHasFocus);
        }
    }

    private static final class InspectionRenderer extends DefaultListCellRenderer {

        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof AnalyzerAndWarning) {
                value = "    " + SPIAccessor.ACCESSOR.getWarningDisplayName(((AnalyzerAndWarning) value).wd);
            } else if (value instanceof String) {
                setFont(getFont().deriveFont(Font.ITALIC));
                setText((String) value);
                setEnabled(false);
                setBackground(list.getBackground());
                setForeground(UIManager.getColor("Label.disabledForeground"));

                return this;
            }

            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private interface ScopeDescription {
        public String getDisplayName();
        public Icon   getIcon();
        public Scope  getScope(AtomicBoolean cancel);
    }

    private static final class AllProjectsScopeDescription implements ScopeDescription {
        private final Icon icon;
        public AllProjectsScopeDescription(Icon icon) {
            this.icon = icon;
        }
        @Override
        @Messages("DN_OpenProjects=Open Projects")
        public String getDisplayName() {
            return Bundle.DN_OpenProjects();
        }
        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public Scope getScope(AtomicBoolean cancel) {
            Map<Project, Map<FileObject, ClassPath>> projects2RegisteredContent = RunAnalysis.projects2RegisteredContent(cancel);

            if (cancel.get()) return null;

            Scope target = Scope.create(null, null, null);

            for (Project p : OpenProjects.getDefault().getOpenProjects()) {
                if (cancel.get()) return null;
                target = RunAnalysis.addProjectToScope(p, target, cancel, projects2RegisteredContent);
            }

            return target;
        }
    }

    private static final class CurrentProjectScopeDescription implements ScopeDescription {
        private final Project project;
        public CurrentProjectScopeDescription(Project project) {
            this.project = project;
        }
        @Override
        @Messages({"# {0} - project display name", "DN_CurrentProject=Current Project ({0})"})
        public String getDisplayName() {
            return Bundle.DN_CurrentProject(ProjectUtils.getInformation(project).getDisplayName());
        }
        @Override
        public Icon getIcon() {
            return ProjectUtils.getInformation(project).getIcon();
        }

        @Override
        public Scope getScope(AtomicBoolean cancel) {
            Map<Project, Map<FileObject, ClassPath>> projects2RegisteredContent = RunAnalysis.projects2RegisteredContent(cancel);

            return RunAnalysis.addProjectToScope(project, Scope.create(null, null, null), cancel, projects2RegisteredContent);
        }
    }

    private static final class PackageScopeDescription implements ScopeDescription {
        private final NonRecursiveFolder pack;
        private final String packName;
        public PackageScopeDescription(NonRecursiveFolder pack, String packName) {
            this.pack = pack;
            this.packName = packName;
        }
        @Override
        @Messages({"# {0} - package display name", "DN_CurrentPackage=Current Package ({0})"})
        public String getDisplayName() {
            return Bundle.DN_CurrentPackage(packName);
        }
        @Override
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/analysis/ui/resources/package.gif", false);
        }

        @Override
        public Scope getScope(AtomicBoolean cancel) {
            return Scope.create(null, Collections.singletonList(pack), null);
        }
    }

    private static final class FileScopeDescription implements ScopeDescription {
        private static final Logger LOG = Logger.getLogger(FileScopeDescription.class.getName());
        private final FileObject file;
        public FileScopeDescription(FileObject file) {
            this.file = file;
        }
        @Override
        @Messages({"# {0} - file display name", "DN_CurrentFile=Current File ({0})"})
        public String getDisplayName() {
            return Bundle.DN_CurrentFile(file.getNameExt());
        }
        @Override
        public Icon getIcon() {
            try {
                DataObject d = DataObject.find(file);
                Node n = d.getNodeDelegate();
                return ImageUtilities.image2Icon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            } catch (DataObjectNotFoundException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            }
        }

        @Override
        public Scope getScope(AtomicBoolean cancel) {
            return Scope.create(null, null, Collections.singletonList(file));
        }
    }

    private static final class ScopeRenderer extends DefaultListCellRenderer {

        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof ScopeDescription) {
                ScopeDescription sd = (ScopeDescription) value;

                try {
                    return super.getListCellRendererComponent(list, sd.getDisplayName(), index, isSelected, cellHasFocus);
                } finally {
                    setIcon(sd.getIcon());
                }
            }

            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private static class Separator implements Border {

        private Color fgColor;

        Separator(Color color) {
            fgColor = color;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics gr = g.create();
            if (gr != null) {
                try {
                    gr.translate(x, y);
                    gr.setColor(fgColor);
                    gr.drawLine(0, height - 1, width - 1, height - 1);
                } finally {
                    gr.dispose();
                }
            }
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 1, 0);
        }
    }

    private static final class AnalyzerAndWarning {
        private final AnalyzerFactory analyzer;
        private final WarningDescription wd;
        public AnalyzerAndWarning(AnalyzerFactory analyzer, WarningDescription wd) {
            this.analyzer = analyzer;
            this.wd = wd;
        }
    }
    
    public static final class DialogState {
        private final boolean configurationsSelected;
        private final String  selectedAnalyzer;
        private final String  selectedConfiguration;
        private final String  selectedInspection;

        private DialogState(boolean configurationsSelected, String selectedAnalyzer, String selectedConfiguration, String selectedInspection) {
            this.configurationsSelected = configurationsSelected;
            this.selectedAnalyzer = selectedAnalyzer;
            this.selectedConfiguration = selectedConfiguration;
            this.selectedInspection = selectedInspection;
        }
        
        public void save() {
            Preferences prefs = NbPreferences.forModule(RunAnalysisPanel.class).node("RunAnalysisPanel");
            
            prefs.putBoolean("configurationsSelected", configurationsSelected);
            if (selectedAnalyzer != null)
                prefs.put("selectedAnalyzer", selectedAnalyzer);
            else
                prefs.remove("selectedAnalyzer");
            if (selectedConfiguration != null)
                prefs.put("selectedConfiguration", selectedConfiguration);
            else
                prefs.remove("selectedConfiguration");
            if (selectedInspection != null)
                prefs.put("selectedInspection", selectedInspection);
            else
                prefs.remove("selectedInspection");
        }
        
        private static DialogState load() {
            Preferences prefs = NbPreferences.forModule(RunAnalysisPanel.class).node("RunAnalysisPanel");
            return new DialogState(prefs.getBoolean("configurationsSelected", true),
                                   prefs.get("selectedAnalyzer", null),
                                   prefs.get("selectedConfiguration", null),
                                   prefs.get("selectedInspection", null));
        }
    }
}
