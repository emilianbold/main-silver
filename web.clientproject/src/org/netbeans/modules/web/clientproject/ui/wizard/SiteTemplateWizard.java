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
package org.netbeans.modules.web.clientproject.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.web.clientproject.sites.SiteZip;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class SiteTemplateWizard extends JPanel {

    private static final long serialVersionUID = 154768576465454L;

    static final Logger LOGGER = Logger.getLogger(SiteTemplateWizard.class.getName());
    private static final SiteTemplateImplementation NO_SITE_TEMPLATE = new DummySiteTemplateImplementation(null);

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    // @GuardedBy("EDT")
    private final SiteZip archiveSiteTemplate = new SiteZip();
    // @GuardedBy("EDT")
    private final SiteZip.Customizer archiveSiteCustomizer;
    // @GuardedBy("EDT")
    final DefaultListModel onlineTemplatesListModel = new DefaultListModel();
    final Object siteTemplateLock = new Object();

    // @GuardedBy("siteTemplateLock")
    SiteTemplateImplementation siteTemplate = NO_SITE_TEMPLATE;


    public SiteTemplateWizard() {
        assert EventQueue.isDispatchThread();

        archiveSiteCustomizer = archiveSiteTemplate.getCustomizer();
        assert archiveSiteCustomizer != null : "Archive template must have a customizer"; //NOI18N

        initComponents();
        // archive
        initArchiveTemplate();
        // other templates
        initOnlineTemplates();
        // listeners
        initListeners();
        // fire first change
        updateSiteTemplate();
    }

    private void initArchiveTemplate() {
        archiveTemplatePanel.add(archiveSiteCustomizer.getComponent(), BorderLayout.CENTER);
    }

    @NbBundle.Messages("SiteTemplateWizard.loading=Loading...")
    private void initOnlineTemplates() {
        assert EventQueue.isDispatchThread();
        // renderer
        onlineTemplateList.setCellRenderer(new TemplateListCellRenderer(onlineTemplateList.getCellRenderer()));
        // data
        onlineTemplateList.setModel(onlineTemplatesListModel);
        onlineTemplatesListModel.addElement(new DummySiteTemplateImplementation(Bundle.SiteTemplateWizard_loading()));
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                Collection<? extends SiteTemplateImplementation> templates = Lookup.getDefault().lookupAll(SiteTemplateImplementation.class);
                onlineTemplatesListModel.removeAllElements();
                for (SiteTemplateImplementation template : templates) {
                    onlineTemplatesListModel.addElement(template);
                }
            }
        });
    }

    private void initListeners() {
        // radios
        ItemListener defaultItemListener = new DefaultItemListener();
        noTemplateRadioButton.addItemListener(defaultItemListener);
        archiveTemplateRadioButton.addItemListener(defaultItemListener);
        onlineTemplateRadioButton.addItemListener(defaultItemListener);
        // online templates
        onlineTemplateList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                setSiteTemplate(getSelectedOnlineTemplate());
                fireChange();
                updateOnlineTemplateDescription();
            }
        });
    }

    final void updateSiteTemplate() {
        enableTemplates();
        fireChange();
    }

    private void enableTemplates() {
        if (noTemplateRadioButton.isSelected()) {
            setSiteTemplate(NO_SITE_TEMPLATE);
            setArchiveTemplateEnabled(false);
            setOnlineTemplateEnabled(false);
        } else if (archiveTemplateRadioButton.isSelected()) {
            setSiteTemplate(archiveSiteTemplate);
            setArchiveTemplateEnabled(true);
            setOnlineTemplateEnabled(false);
        } else if (onlineTemplateRadioButton.isSelected()) {
            setSiteTemplate(getSelectedOnlineTemplate());
            setArchiveTemplateEnabled(false);
            setOnlineTemplateEnabled(true);
        } else {
            throw new IllegalStateException("No template radio button selected?!"); // NOI18N
        }
    }

    void updateOnlineTemplateDescription() {
        String desc;
        synchronized (siteTemplateLock) {
            desc = siteTemplate != null ? siteTemplate.getDescription() : ""; // NOI18N
        }
        onlineTemplateDescriptionTextPane.setText(desc);
    }

    SiteTemplateImplementation getSelectedOnlineTemplate() {
        return (SiteTemplateImplementation) onlineTemplateList.getSelectedValue();
    }

    private void setArchiveTemplateEnabled(boolean enabled) {
        for (Component component : archiveSiteCustomizer.getComponent().getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void setOnlineTemplateEnabled(boolean enabled) {
        onlineTemplateDescriptionLabel.setEnabled(enabled);
        onlineTemplateList.setEnabled(enabled);
        onlineTemplateDescriptionTextPane.setEnabled(enabled);
    }

    public void addChangeListener(ChangeListener listener) {
        assert EventQueue.isDispatchThread();
        changeSupport.addChangeListener(listener);
        archiveSiteCustomizer.addChangeListener(listener);
    }

    public void removeChangeListener(final ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
        // #216283 - can be called form non-EDT thread
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                archiveSiteCustomizer.removeChangeListener(listener);
            }
        });
    }

    @NbBundle.Messages("SiteTemplateWizard.error.noTemplateSelected=No online template selected.")
    public String getErrorMessage() {
        boolean isArchiveSiteTemplate;
        synchronized (siteTemplateLock) {
            if (siteTemplate == null) {
                return Bundle.SiteTemplateWizard_error_noTemplateSelected();
            }
            isArchiveSiteTemplate = siteTemplate == archiveSiteTemplate;
        }
        if (isArchiveSiteTemplate) {
            // archive
            archiveSiteCustomizer.isValid();
            return archiveSiteCustomizer.getErrorMessage();
        }
        return null;
    }

    public String getWarningMessage() {
        boolean isArchiveSiteTemplate;
        synchronized (siteTemplateLock) {
            isArchiveSiteTemplate = siteTemplate == archiveSiteTemplate;
        }
        if (isArchiveSiteTemplate) {
            // archive
            archiveSiteCustomizer.isValid();
            return archiveSiteCustomizer.getWarningMessage();
        }
        return null;
    }

    public void lockPanel() {
        enablePanel(false);
        setArchiveTemplateEnabled(false);
        setOnlineTemplateEnabled(false);
    }

    public void unlockPanel() {
        enablePanel(true);
        enableTemplates();
    }

    private void enablePanel(boolean enable) {
        noTemplateRadioButton.setEnabled(enable);
        archiveTemplateRadioButton.setEnabled(enable);
        onlineTemplateRadioButton.setEnabled(enable);
    }

    @NbBundle.Messages({
        "# {0} - template name",
        "SiteTemplateWizard.template.preparing=Preparing template \"{0}\" for first usage...",
        "# {0} - template name",
        "SiteTemplateWizard.error.preparing=Cannot prepare template \"{0}\" (see IDE log for more details)."
    })
    public String prepareTemplate() {
        assert !EventQueue.isDispatchThread();
        final String templateName;
        synchronized (siteTemplateLock) {
            if (siteTemplate.isPrepared()) {
                // already prepared
                return null;
            }
            templateName = siteTemplate.getName();
        }
        // prepare
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(Bundle.SiteTemplateWizard_template_preparing(templateName));
        progressHandle.start();
        try {
            synchronized (siteTemplateLock) {
                siteTemplate.prepare();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return Bundle.SiteTemplateWizard_error_preparing(templateName);
        } finally {
            progressHandle.finish();
        }
        return null;
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    public SiteTemplateImplementation getSiteTemplate() {
        synchronized (siteTemplateLock) {
            return siteTemplate;
        }
    }

    void setSiteTemplate(SiteTemplateImplementation siteTemplate) {
        synchronized (siteTemplateLock) {
            this.siteTemplate = siteTemplate;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        templateButtonGroup = new javax.swing.ButtonGroup();
        infoLabel = new javax.swing.JLabel();
        noTemplateRadioButton = new javax.swing.JRadioButton();
        archiveTemplateRadioButton = new javax.swing.JRadioButton();
        archiveTemplatePanel = new javax.swing.JPanel();
        onlineTemplateRadioButton = new javax.swing.JRadioButton();
        onlineTemplateScrollPane = new javax.swing.JScrollPane();
        onlineTemplateList = new javax.swing.JList();
        onlineTemplateDescriptionLabel = new javax.swing.JLabel();
        onlineTemplateDescriptionScrollPane = new javax.swing.JScrollPane();
        onlineTemplateDescriptionTextPane = new javax.swing.JTextPane();

        org.openide.awt.Mnemonics.setLocalizedText(infoLabel, org.openide.util.NbBundle.getMessage(SiteTemplateWizard.class, "SiteTemplateWizard.infoLabel.text")); // NOI18N

        templateButtonGroup.add(noTemplateRadioButton);
        noTemplateRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(noTemplateRadioButton, org.openide.util.NbBundle.getMessage(SiteTemplateWizard.class, "SiteTemplateWizard.noTemplateRadioButton.text")); // NOI18N

        templateButtonGroup.add(archiveTemplateRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(archiveTemplateRadioButton, org.openide.util.NbBundle.getMessage(SiteTemplateWizard.class, "SiteTemplateWizard.archiveTemplateRadioButton.text")); // NOI18N

        archiveTemplatePanel.setLayout(new java.awt.BorderLayout());

        templateButtonGroup.add(onlineTemplateRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(onlineTemplateRadioButton, org.openide.util.NbBundle.getMessage(SiteTemplateWizard.class, "SiteTemplateWizard.onlineTemplateRadioButton.text")); // NOI18N

        onlineTemplateList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        onlineTemplateScrollPane.setViewportView(onlineTemplateList);

        onlineTemplateDescriptionLabel.setLabelFor(onlineTemplateDescriptionTextPane);
        org.openide.awt.Mnemonics.setLocalizedText(onlineTemplateDescriptionLabel, org.openide.util.NbBundle.getMessage(SiteTemplateWizard.class, "SiteTemplateWizard.onlineTemplateDescriptionLabel.text")); // NOI18N

        onlineTemplateDescriptionScrollPane.setViewportView(onlineTemplateDescriptionTextPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(infoLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(noTemplateRadioButton, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(archiveTemplateRadioButton, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(onlineTemplateRadioButton, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(onlineTemplateDescriptionLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(archiveTemplatePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(onlineTemplateScrollPane)
                            .addComponent(onlineTemplateDescriptionScrollPane))))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(infoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(noTemplateRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(archiveTemplateRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(archiveTemplatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(onlineTemplateRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(onlineTemplateScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(onlineTemplateDescriptionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(onlineTemplateDescriptionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel archiveTemplatePanel;
    private javax.swing.JRadioButton archiveTemplateRadioButton;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JRadioButton noTemplateRadioButton;
    private javax.swing.JLabel onlineTemplateDescriptionLabel;
    private javax.swing.JScrollPane onlineTemplateDescriptionScrollPane;
    private javax.swing.JTextPane onlineTemplateDescriptionTextPane;
    private javax.swing.JList onlineTemplateList;
    private javax.swing.JRadioButton onlineTemplateRadioButton;
    private javax.swing.JScrollPane onlineTemplateScrollPane;
    private javax.swing.ButtonGroup templateButtonGroup;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class DummySiteTemplateImplementation implements SiteTemplateImplementation {

        private final String name;


        public DummySiteTemplateImplementation(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException("Not supported."); //NOI18N
        }

        @Override
        public boolean isPrepared() {
            return true;
        }

        @Override
        public void prepare() {
            // noop
        }

        @Override
        public void apply(AntProjectHelper helper, ProgressHandle handle) {
            // noop
        }

        @Override
        public Collection<String> supportedLibraries() {
            return Collections.emptyList();
        }

    }

    private static final class TemplateListCellRenderer implements ListCellRenderer {

        private final ListCellRenderer cellRenderer;


        public TemplateListCellRenderer(ListCellRenderer cellRenderer) {
            this.cellRenderer = cellRenderer;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            SiteTemplateImplementation site = (SiteTemplateImplementation) value;
            return cellRenderer.getListCellRendererComponent(list, site.getName(), index, isSelected, cellHasFocus);
        }

    }

    private final class DefaultItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent event) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                updateSiteTemplate();
            }
        }

    }

}
