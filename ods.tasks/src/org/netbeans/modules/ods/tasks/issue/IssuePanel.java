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
package org.netbeans.modules.ods.tasks.issue;

import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.UIUtils;
import org.netbeans.modules.mylyn.util.WikiPanel;
import org.netbeans.modules.mylyn.util.WikiUtils;
import org.netbeans.modules.ods.tasks.C2C;
import org.netbeans.modules.ods.tasks.DummyUtils;
import org.netbeans.modules.ods.tasks.issue.C2CIssue.Attachment;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.netbeans.modules.ods.tasks.util.C2CUtil;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author tomas
 */
public class IssuePanel extends javax.swing.JPanel implements Scrollable {
    
    private static final String RESOLUTION_RESOLVED = "RESOLVED";               // NOI18N    
    private static final String STATUS_FIXED = "FIXED";                         // NOI18N
    private static final String STATUS_UNCONFIRMED = "UNCONFIRMED";             // NOI18N
    private static final String RESOLUTION_DUPLICATE = "DUPLICATE";             // NOI18N

    private C2CIssue issue;
    
    private boolean reloading;
    private boolean skipReload;
    
    private CommentsPanel commentsPanel;
    private AttachmentsPanel attachmentsPanel;
    
    private Map<String,String> initialValues = new HashMap<String,String>();
    private List<String> tags = new LinkedList<String>();
    
    // message panel 
    private boolean cyclicDependency = false;
    private boolean noSummary = false;
    private boolean invalidTag = false;
    private boolean noComponent = false;
    private boolean noVersion = false;
    private boolean noTargetMilestione = false;
    private boolean noDuplicateId = false;
    private List<String> fieldErrors = new LinkedList<String>();
    private List<String> fieldWarnings = new LinkedList<String>();
    private int resolvedIndex;
    private WikiPanel descriptionPanel;
    private WikiPanel addCommentPanel;
    private static String wikiLanguage = "Confluence";
    
    
    /**
     * Creates new form IssuePanel
     */
    public IssuePanel() {
        initComponents();
        
        separatorLabel.setVisible(false);
        separatorLabel3.setVisible(false);
        
        updateReadOnlyField(reportedField);
        updateReadOnlyField(modifiedField);
        updateReadOnlyField(headerField);
        
        messagePanel.setBackground(getBackground());
        
//        customFieldsPanelLeft.setBackground(getBackground());
//        customFieldsPanelRight.setBackground(getBackground());
        Font font = reportedLabel.getFont();
        headerField.setFont(font.deriveFont((float)(font.getSize()*1.7)));
        duplicateField.setVisible(false);
        duplicateButton.setVisible(false);
        attachDocumentListeners();

        GroupLayout layout = (GroupLayout)getLayout();
        addCommentPanel = WikiUtils.getWikiPanel(wikiLanguage, true, true);
        layout.replace(dummyAddCommentPanel, addCommentPanel);

        // Comments panel
        commentsPanel = new CommentsPanel();
        commentsPanel.setNewCommentHandler(new CommentsPanel.NewCommentHandler() {
            @Override
            public void append(String text) {
                addCommentPanel.appendCodeText(text);
                scrollRectToVisible(addCommentPanel.getBounds());
            }
        });
        attachmentsPanel = new AttachmentsPanel();
        layout.replace(dummyCommentsPanel, commentsPanel);
        layout.replace(dummyAttachmentsPanel, attachmentsPanel);
        attachmentsLabel.setLabelFor(attachmentsPanel);
        initSpellChecker();
        initDefaultButton();

        descriptionPanel = WikiUtils.getWikiPanel(wikiLanguage, false, true);
        layout.replace(dummyDescriptionPanel, descriptionPanel);
    }

    private void initSpellChecker () {
        Spellchecker.register(summaryField);
    }

    private void initDefaultButton() {
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit"); // NOI18N
        ActionMap actionMap = getActionMap();
        Action submitAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (submitButton.isEnabled()) {
                    submitButtonActionPerformed(null);
                }
            }
        };
        actionMap.put("submit", submitAction); // NOI18N
    }

    C2CIssue getIssue() {
        return issue;
    }

    public void setIssue(C2CIssue issue) {
        assert SwingUtilities.isEventDispatchThread() : "Accessing Swing components. Do not call outside event-dispatch thread!"; // NOI18N
        headerField.setText(issue.getDisplayName());
        if (this.issue == null) {
            
            IssueCache<C2CIssue, TaskData> cache = issue.getRepository().getIssueCache();
            cache.removePropertyChangeListener(issue, cacheListener);
            cache.addPropertyChangeListener(issue, cacheListener);

            summaryField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateNoSummary();
                }
            });
            tagsField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateInvalidTag();
                }
            });
        }
        this.issue = issue;
        initCombos();
//        initCustomFields(); XXX
        
        Collection<Keyword> kws = DummyUtils.getClientData(issue.getRepository().getTaskRepository()).getKeywords();
        tags.clear();
        for (Keyword keyword : kws) {
            tags.add(keyword.getName());
        }
                
        reloadForm(true);

        if (issue.isNew()) {
            selectProduct();
        }

        // Hack to "link" the width of both columns
//        Dimension dim = ccField.getPreferredSize();
//        int width1 = Math.max(osCombo.getPreferredSize().width, platformCombo.getPreferredSize().width);
//        int width2 = Math.max(priorityCombo.getPreferredSize().width, showIssueType ? issueTypeCombo.getPreferredSize().width : severityCombo.getPreferredSize().width);
//        int gap = LayoutStyle.getInstance().getPreferredGap(osCombo, platformCombo, LayoutStyle.ComponentPlacement.RELATED, SwingConstants.EAST, this);
//        ccField.setPreferredSize(new Dimension(2*Math.max(width1,width2)+gap,dim.height));
    }
    
    private void selectProduct() {
        // XXX
//        if (ownerInfo != null) {
//            String owner = findInModel(productCombo, ownerInfo.getOwner());
//            selectInCombo(productCombo, owner, true);
//            List<String> data = ownerInfo.getExtraData();
//            if (data != null && data.size() > 0) {
//                String component = findInModel(componentCombo, data.get(0));
//                selectInCombo(componentCombo, component, true);
//            }
//        } else {
//            BugzillaRepository repository = issue.getRepository();
//            if (repository instanceof KenaiRepository) {
//                String productName = ((KenaiRepository)repository).getProductName();
//                selectInCombo(productCombo, productName, true);
//            } else if (BugzillaUtil.isNbRepository(repository)) {
//                // IssueProvider 181224
//                String defaultProduct = "ide"; // NOI18N
//                String defaultComponent = "Code"; // NOI18N
//                productCombo.setSelectedItem(defaultProduct);
//                componentCombo.setSelectedItem(defaultComponent);
//            } else {
//                productCombo.setSelectedIndex(0);
//            }
//        }
        
        productCombo.setSelectedIndex(0);
    }    
    
    private void initCombos() {
        C2CData cd = DummyUtils.getClientData(issue.getRepository().getTaskRepository());
        // XXX check if valid
        
        productCombo.setModel(toComboModel(cd.getProducts()));
        productCombo.setRenderer(new ClientDataRenderer());
        
        // componentCombo, versionCombo, targetMilestoneCombo are filled
        // automatically when productCombo is set/changed
        // Do not support MOVED resolution (yet?)

        // List<String> resolutions = new LinkedList<String>(cd.getResolutions());
        // resolutions.remove("MOVED"); // NOI18N XXX
        resolutionCombo.setModel(toComboModel(cd.getResolutions()));
        resolutionCombo.setRenderer(new ClientDataRenderer());
        
        priorityCombo.setModel(toComboModel(cd.getPriorities()));
        priorityCombo.setRenderer(new ClientDataRenderer());
        
        severityCombo.setModel(toComboModel(cd.getSeverities()));
        severityCombo.setRenderer(new ClientDataRenderer());

        ownerCombo.setModel(toComboModel(cd.getUsers()));
        ownerCombo.setRenderer(new ClientDataRenderer());
        
        // statusCombo and resolution fields are filled in reloadForm
    }

    private ComboBoxModel toComboModel(List items) {
        return new DefaultComboBoxModel(items.toArray());
    }

    private int oldCommentCount;
    void reloadForm(boolean force) {
        if (skipReload) { // XXX when set?
            return;
        }
        int noWarnings = fieldWarnings.size();
        int noErrors = fieldErrors.size();
        if (force) {
            fieldWarnings.clear();
            fieldErrors.clear();
        }
        reloading = true;
        boolean isNew = issue.isNew();
        
        headerField.setVisible(!isNew);
        statusCombo.setEnabled(!isNew);
        org.openide.awt.Mnemonics.setLocalizedText(addCommentLabel, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.description" : "IssuePanel.addCommentLabel.text")); // NOI18N
        reportedLabel.setVisible(!isNew);
        reportedField.setVisible(!isNew);
        modifiedLabel.setVisible(!isNew);
        modifiedField.setVisible(!isNew);
        statusLabel.setVisible(!isNew);
        statusCombo.setVisible(!isNew);
        resolutionLabel.setVisible(!isNew);
        resolutionCombo.setVisible(!isNew);
        separator.setVisible(!isNew);
        commentsPanel.setVisible(!isNew);
        attachmentsLabel.setVisible(!isNew);
        attachmentsPanel.setVisible(!isNew);
        refreshButton.setVisible(!isNew);
        separatorLabel.setVisible(!isNew);
        cancelButton.setVisible(!isNew);
        separatorLabel3.setVisible(!isNew);
        showInBrowserButton.setVisible(!isNew);
        parentLabel.setVisible(!isNew);
        parentField.setVisible(!isNew);
        parentButton.setVisible(!isNew);
        subtaskLabel.setVisible(!isNew);
        subtaskField.setVisible(!isNew);
        subtaskButton.setVisible(!isNew);
        externalLabel.setVisible(!isNew);
        externalField.setVisible(!isNew);
        externalButton.setVisible(!isNew);
        org.openide.awt.Mnemonics.setLocalizedText(submitButton, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.submitButton.text.new" : "IssuePanel.submitButton.text")); // NOI18N
        if (isNew && force) {
            // Preselect the first product
            selectProduct();
            initStatusCombo(STATUS_UNCONFIRMED); // NOI18N
        } else {
            String format = NbBundle.getMessage(IssuePanel.class, "IssuePanel.headerLabel.format"); // NOI18N
            String headerTxt = MessageFormat.format(format, issue.getID(), issue.getSummary());
            headerField.setText(headerTxt);
            Dimension dim = headerField.getPreferredSize();
            headerField.setMinimumSize(new Dimension(0, dim.height));
            headerField.setPreferredSize(new Dimension(0, dim.height));
            reloadField(force, issueTypeCombo, IssueField.TASK_TYPE, issueTypeWarning, issueTypeLabel);
            reloadField(force, summaryField, IssueField.SUMMARY, summaryWarning, summaryLabel);
            reloadField(force, productCombo, IssueField.PRODUCT, productWarning, productLabel);
            reloadField(force, componentCombo, IssueField.COMPONENT, componentWarning, componentLabel);
            reloadField(force, releaseCombo, IssueField.MILESTONE, releaseWarning, releaseLabel);
            reloadField(force, resolutionCombo, IssueField.RESOLUTION, resolutionWarning, "resolution"); // NOI18N
            reloadField(force, descriptionPanel, IssueField.DESCRIPTION, descriptionPanel.getWarningLabel(), descriptionLabel); // NOI18N
            String status = reloadField(force, statusCombo, IssueField.STATUS, resolutionWarning, statusLabel);
            initStatusCombo(status);
            String initialResolution = initialValues.get(IssueField.RESOLUTION.getKey());
            if (RESOLUTION_DUPLICATE.equals(initialResolution)) { // NOI18N // XXX no string gvalues
                duplicateField.setEditable(false);
                duplicateField.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
                duplicateField.setBackground(getBackground());
            } else {
                JTextField field = new JTextField();
                duplicateField.setEditable(true);
                duplicateField.setBorder(field.getBorder());
                duplicateField.setBackground(field.getBackground());
            }
            reloadField(force, priorityCombo, IssueField.PRIORITY, priorityWarning, priorityLabel);
            reloadField(force, severityCombo, IssueField.SEVERITY, severityWarning, priorityLabel);
            reloadField(force, iterationCombo, IssueField.MILESTONE, iterationWarning, iterationLabel);
            reloadField(force, tagsField, IssueField.TAGS, tagsWarning, tagsLabel);

            if (!isNew) {
                // reported field
                format = NbBundle.getMessage(IssuePanel.class, "IssuePanel.reportedLabel.format"); // NOI18N
                Date creation = issue.getCreatedDate();
                String creationTxt = creation != null ? DateFormat.getDateInstance(DateFormat.DEFAULT).format(creation) : ""; // NOI18N
                
                String reporter = issue.getFieldValue(IssueField.REPORTER);
                String reporterName = issue.getPersonName(IssueField.REPORTER);
                String reporterTxt = ((reporterName == null) || (reporterName.trim().length() == 0)) ? reporter : reporterName;
                String reportedTxt = MessageFormat.format(format, creationTxt, reporterTxt);
                reportedField.setText(reportedTxt);
                
                fixPrefSize(reportedField);

                // modified field
                Date modification = issue.getCreatedDate();
                String modifiedTxt = modification != null ? DateFormat.getDateTimeInstance().format(modification) : ""; // NOI18N
                modifiedField.setText(modifiedTxt);
                fixPrefSize(modifiedField);
            }

            String assignee = issue.getFieldValue(IssueField.OWNER);
//          XXX  String selectedAssignee = (assignedField.getParent() == null) ? ownerCombo.getSelectedItem().toString() : assignedField.getText();
//            if (isKenaiRepository && (assignee.trim().length() > 0) && (force || !selectedAssignee.equals(assignee))) {
//                int index = assignee.indexOf('@');
//                String userName = (index == -1) ? assignee : assignee.substring(0,index);
//                String host = ((KenaiRepository) issue.getRepository()).getHost();
//                JLabel label = KenaiUtil.createUserWidget(userName, host, KenaiUtil.getChatLink(issue.getID()));
//                label.setText(null);
//                ((javax.swing.GroupLayout)getLayout()).replace(assignedToStatusLabel, label);
//                label.setVisible(assignedToStatusLabel.isVisible());
//                assignedToStatusLabel = label;
//            }
//            if (force) {
//                assignedToStatusLabel.setVisible(assignee.trim().length() > 0);
//            }
//            if (assignedField.getParent() == null) {
//                reloadField(force, ownerCombo, IssueField.ASSIGNED_TO, assignedToWarning, assignedLabel);
//            } else {
//                reloadField(force, assignedField, IssueField.ASSIGNED_TO, assignedToWarning, assignedLabel);
//            }
                
            reloadField(force, ownerCombo, IssueField.OWNER, ownerWarning, ownerLabel);
            
            reloadField(force, ccField, IssueField.CC, ccWarning, ccLabel);
            reloadField(force, subtaskField, IssueField.SUBTASK, parentWarning, subtaskLabel);
            reloadField(force, parentField, IssueField.PARENT, parentWarning, parentLabel);
//            reloadCustomFields(force); XXX
        }
        int newCommentCount = issue.getComments().length;
        if (!force && oldCommentCount != newCommentCount) {
            String message = NbBundle.getMessage(IssuePanel.class, "IssuePanel.commentAddedWarning"); // NOI18N
            if (!fieldWarnings.contains(message)) {
                fieldWarnings.add(0, message);
            }
        }
        oldCommentCount = newCommentCount;
        List<Attachment> attachments = issue.getAttachments();
        if (!isNew) {
            commentsPanel.setIssue(issue, attachments);
        }
        attachmentsPanel.setAttachments(attachments);
        UIUtils.keepFocusedComponentVisible(commentsPanel);
        UIUtils.keepFocusedComponentVisible(attachmentsPanel);
        if (force && !isNew) {
            addCommentPanel.clear();
        }
        updateFieldStatuses();
        updateNoSummary();
        if ((fieldWarnings.size() != noWarnings) || (fieldErrors.size() != noErrors)) {
            updateMessagePanel();
        }
        reloading = false;
    }
    
    private static void fixPrefSize(JTextField textField) {
        // The preferred size of JTextField on (Classic) Windows look and feel
        // is one pixel shorter. The following code is a workaround.
        textField.setPreferredSize(null);
        Dimension dim = textField.getPreferredSize();
        Dimension fixedDim = new Dimension(dim.width+1, dim.height);
        textField.setPreferredSize(fixedDim);
    }

    private String reloadField(boolean force, JComponent component, IssueField field, JLabel warningLabel, JLabel fieldLabel) {
        return reloadField(force, component, field, warningLabel, fieldName(fieldLabel));
    }
    
    private String reloadField(boolean force, JComponent component, IssueField field, JLabel warningLabel, String fieldName) {
        String currentValue = null;
        boolean isNew = issue.isNew();
        if (!force) {
            if (component instanceof JComboBox) {
                Object value = ((JComboBox)component).getSelectedItem();
                currentValue  = (value == null) ? "" : value.toString(); // NOI18N
            } else if (component instanceof JTextComponent) {
                currentValue = ((JTextComponent)component).getText();
            } else if (component instanceof JList) {
                JList list = (JList)component;
                StringBuilder sb = new StringBuilder();
                for (Object value : list.getSelectedValues()) {
                    if (sb.length()!=0) {
                        sb.append(',');
                    }
                    sb.append(value);
                }
                currentValue = sb.toString();
            } else if (component instanceof WikiPanel) {
                currentValue = ((WikiPanel)component).getWikiFormatText();
            }
        }
        String initialValue = initialValues.get(field.getKey());
        String newValue;
        if (component instanceof JList) {
            StringBuilder sb = new StringBuilder();
            for (String value : issue.getFieldValues(field)) {
                if (sb.length()!=0) {
                    sb.append(',');
                }
                sb.append(value);
            }
            newValue = sb.toString();
        } else {
            newValue = issue.getFieldValue(field);
        }
        boolean valueModifiedByUser = (currentValue != null) && (initialValue != null) && !currentValue.equals(initialValue);
        boolean valueModifiedByServer = (initialValue != null) && (newValue != null) && !initialValue.equals(newValue);
        if (force || !valueModifiedByUser) {
            if (component instanceof JComboBox) {
                JComboBox combo = (JComboBox)component;
                selectInCombo(combo, newValue, true);
            } else if (component instanceof JTextComponent) {
                ((JTextComponent)component).setText(newValue);
            } else if (component instanceof JList) {
                JList list = (JList)component;
                list.clearSelection();
                ListModel model = list.getModel();
                for (String value : issue.getFieldValues(field)) {
                    for (int i=0; i<model.getSize(); i++) {
                        if (value.equals(model.getElementAt(i))) {
                            list.addSelectionInterval(i, i);
                        }
                    }
                }
            } else if (component instanceof WikiPanel) {
                ((WikiPanel)component).setWikiFormatText(newValue);
            }
            if (force) {
                if (warningLabel != null) {
                    warningLabel.setIcon(null);
                }
            } else {
                if (!isNew && valueModifiedByServer && (warningLabel != null)) {
                    warningLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/warning.gif", true)); // NOI18N
                    String messageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.fieldModifiedWarning"); // NOI18N
                    String message = MessageFormat.format(messageFormat, fieldName, currentValue, newValue);
                    fieldWarnings.add(message);
                    warningLabel.setToolTipText(message);
                }
            }
            currentValue = newValue;
        } else {
            if (!isNew && valueModifiedByServer && (warningLabel != null)) {
                warningLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/error.gif", true)); // NOI18N
                String messageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.fieldModifiedError"); // NOI18N
                String message = MessageFormat.format(messageFormat, fieldName, newValue);
                fieldErrors.add(message);
                warningLabel.setToolTipText(message);
            }
        }
        if ((IssueField.SUMMARY == field) || (IssueField.PRIORITY == field) /* XXX || (field instanceof CustomIssueField)*/) {
            warningLabel.setVisible(warningLabel.getIcon() != null);
        }
        initialValues.put(field.getKey(), newValue);
        return currentValue;
    }
    
    private String fieldName(JComponent fieldLabel) {
        assert fieldLabel instanceof JLabel || fieldLabel instanceof JButton;
        String txt = "";
        if(fieldLabel instanceof JLabel) {
            txt = ((JLabel) fieldLabel).getText().trim();
            
        } else if(fieldLabel instanceof JButton) {
            txt = ((JButton) fieldLabel).getText().trim();
        } else {
            return null;
        }
        if (txt.endsWith(":")) { // NOI18N
            txt = txt.substring(0, txt.length()-1);
        }
        return txt;
    }
    
    private boolean selectInCombo(JComboBox combo, Object value, boolean forceInModel) {
        if (value == null) {
            return false;
        }
        
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            
            String itemValue = null;
            if(item instanceof Priority) {
                itemValue = ((Priority) item).getValue();
            } else if(item instanceof TaskSeverity) {
                itemValue = ((TaskSeverity) item).getValue();
            } else if(item instanceof TaskResolution) {
                itemValue = ((TaskResolution) item).getValue();
            } else if(item instanceof Product) {
                itemValue = ((Product) item).getName();
            } else if(item instanceof TaskUserProfile) {
                itemValue = ((TaskUserProfile) item).getRealname();
            } else if(value instanceof TaskStatus) {
                itemValue = ((TaskStatus) value).getValue();
            } else if(value instanceof com.tasktop.c2c.server.tasks.domain.Component) {
                itemValue = ((com.tasktop.c2c.server.tasks.domain.Component) value).getName();
            } else {
                assert value instanceof String : "Wrong value";                 // NOI18N
            }
            
            if(value.equals(itemValue)) {
                combo.setSelectedItem(item);
                break;
            }
        }
        
        if (forceInModel && !value.equals("") && !value.equals(getSelectedValue(combo))) { // NOI18N
            // Reload of server attributes is needed - workarounding it
            ComboBoxModel model = combo.getModel();
            if (model instanceof DefaultComboBoxModel) {
                ((DefaultComboBoxModel)model).insertElementAt(value, 0);
                combo.setSelectedIndex(0);
            }
        }
        return value.equals(getSelectedValue(combo));
    }    

    private String getSelectedValue(JComboBox combo) {
        Object item = combo.getSelectedItem();
        if(item == null) {
            return null;
        }
        if(item instanceof Priority) {
            return ((Priority) item).getValue();
        } else if(item instanceof TaskSeverity) {
            return ((TaskSeverity) item).getValue();
        } else if(item instanceof TaskResolution) {
            return ((TaskResolution) item).getValue();
        } else if(item instanceof Product) {
            return ((Product) item).getName();
        } else if(item instanceof TaskUserProfile) {
            return ((TaskUserProfile) item).getRealname();
        } else if(item instanceof TaskStatus) {
            return ((TaskStatus) item).getValue();
        } else if(item instanceof com.tasktop.c2c.server.tasks.domain.Component) {
            return ((com.tasktop.c2c.server.tasks.domain.Component) item).getName();
        } else {
            assert item instanceof String : "Wrong value";                 // NOI18N
        }
        return null;
    }
    
    private void initStatusCombo(String status) {
        // Init statusCombo - allowed transitions (heuristics):
        // Open -> Open-Unconfirmed-Reopened+Resolved
        // Resolved -> Reopened+Close
        // Close-Resolved -> Reopened+Resolved+(Close with higher index)
        C2CData cd = DummyUtils.getClientData(issue.getRepository().getTaskRepository());
        
        List<TaskStatus> statuses = cd.getStatuses();
        
        // XXX evaluate statuses for open and active
        for (int i = 0; i < statuses.size(); i++) {
            TaskStatus s = statuses.get(i);
            if(s.getValue().equals("RESOLVED")) {
                resolvedIndex = i;
                break;
            }
        }
        
        statusCombo.setModel(toComboModel(statuses));
        statusCombo.setRenderer(new ClientDataRenderer());
        selectInCombo(statusCombo, cd.getStatusByValue(status), false);
    }    
    
    private void updateReadOnlyField(JTextField field) {
        if ("GTK".equals(UIManager.getLookAndFeel().getID())) { // NOI18N
            field.setUI(new BasicTextFieldUI());
        }
        field.setBackground(getBackground());
        Caret caret = field.getCaret();
        if (caret instanceof DefaultCaret) {
            ((DefaultCaret)caret).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
    }

    private void updateNoSummary() {
        if (summaryField.getText().trim().length() == 0) {
            if (!noSummary) {
                noSummary = true;
                updateMessagePanel();
            }
        } else {
            if (noSummary) {
                noSummary = false;
                updateMessagePanel();
            }
        }
    }

    private void updateInvalidTag() {
        boolean invalidFound = false;
        StringTokenizer st = new StringTokenizer(tagsField.getText(), ", \t\n\r\f"); // NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (!tags.contains(token.toUpperCase())) {
                invalidFound = true;
                break;
            }
        }
        if (invalidFound != invalidTag) {
            invalidTag = invalidFound;
            updateMessagePanel();
        }
    }

    private void updateNoComponent() {
        boolean newNoComponent = (componentCombo.getSelectedItem() == null);
        if (noComponent != newNoComponent) {
            noComponent = newNoComponent;
            updateMessagePanel();
        }
    }

    private void updateNoRelease() {
        boolean newNoVersion = (releaseCombo.getSelectedItem() == null);
        if (noVersion != newNoVersion) {
            noVersion = newNoVersion;
            updateMessagePanel();
        }
    }

    private void updateNoIteration() {
        boolean newNoTargetMilestone = (iterationCombo.getSelectedItem() == null);
        if (noTargetMilestione != newNoTargetMilestone) {
            noTargetMilestione = newNoTargetMilestone;
            updateMessagePanel();
        }
    }
    
    private void attachDocumentListeners() {
        // XXX test, add missing fields
        ccField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(ccLabel));        
        parentField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(parentLabel));
        subtaskField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(parentLabel));
        CyclicDependencyDocumentListener cyclicDependencyListener = new CyclicDependencyDocumentListener();
        parentField.getDocument().addDocumentListener(cyclicDependencyListener);
        subtaskField.getDocument().addDocumentListener(cyclicDependencyListener);
        duplicateField.getDocument().addDocumentListener(new DuplicateListener());
    }

    private void updateFieldStatuses() {
        // XXX
//        updateFieldStatus(IssueField.SUMMARY, summaryLabel);
//        updateFieldStatus(IssueField.PRODUCT, productLabel);
//        updateFieldStatus(IssueField.COMPONENT, componentLabel);
//        updateFieldStatus(IssueField.VERSION, versionLabel);
//        updateFieldStatus(IssueField.PLATFORM, platformLabel);
//        updateFieldStatus(IssueField.OS, platformLabel);
//        updateFieldStatus(IssueField.STATUS, statusLabel);
//        updateFieldStatus(IssueField.RESOLUTION, resolutionLabel);
//        updateFieldStatus(IssueField.PRIORITY, priorityLabel);
//        updateFieldStatus(IssueField.SEVERITY, priorityLabel);
//        updateFieldStatus(IssueField.MILESTONE, targetMilestoneLabel);
//        updateFieldStatus(IssueField.URL, urlLabel);
//        updateFieldStatus(IssueField.WHITEBOARD, statusWhiteboardLabel);
//        updateFieldStatus(IssueField.KEYWORDS, keywordsLabel);
//        updateFieldStatus(IssueField.ASSIGNED_TO, assignedLabel);
//        updateFieldStatus(IssueField.QA_CONTACT, qaContactLabel);
//        updateFieldStatus(IssueField.CC, ccLabel);
//        updateFieldStatus(IssueField.DEPENDS_ON, dependsLabel);
//        updateFieldStatus(IssueField.BLOCKS, blocksLabel);
//        updateFieldStatus(IssueField.ESTIMATED_TIME, estimatedLabel);
//        updateFieldStatus(IssueField.REMAINING_TIME, remainingLabel);
//        updateFieldStatus(IssueField.WORK_TIME, timetrackingLabel);
//        updateFieldStatus(IssueField.DEADLINE, deadlineLabel);
//        if (BugzillaUtil.isNbRepository(issue.getRepository())) {
//            updateFieldStatus(IssueField.ISSUE_TYPE, issueTypeLabel);
//        }
//        for (CustomFieldInfo field : customFields) {
//            updateFieldStatus(field.field, field.label);
//        }
    }

    private void updateFieldStatus(IssueField field, JComponent label) {
//        assert label instanceof JButton || label instanceof JLabel;
//        boolean highlight = !issue.getTaskData().isNew() && (issue.getFieldStatus(field) != BugzillaIssue.FIELD_STATUS_UPTODATE);
//        label.setOpaque(highlight);
//        if (highlight) {
//            label.setBackground(HIGHLIGHT_COLOR);
//        }
    }

    PropertyChangeListener cacheListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() != IssuePanel.this.issue) {
                return;
            }
            if (IssueCache.EVENT_ISSUE_SEEN_CHANGED.equals(evt.getPropertyName())) {
                updateFieldStatuses();
            }
        }
    };

    private void findIssue(JTextField fld, String msg, String helpCtx, boolean append) {
        String newIssueID = BugtrackingUtil.selectIssue(
            NbBundle.getMessage(IssuePanel.class, msg), 
            C2CUtil.getRepository(issue.getRepository()),
            this,
            new HelpCtx(helpCtx));
        if (newIssueID != null) {
            if(append) {
                StringBuilder sb = new StringBuilder();
                if (!fld.getText().trim().equals("")) {                         // NOI18N
                    sb.append(fld.getText()).append(',').append(' ');           // NOI18N
                }
                sb.append(newIssueID);
                fld.setText(sb.toString());                
            } else {
                fld.setText(newIssueID);
            }
        }
    }

    private static class ClientDataRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(value instanceof Priority) {
                value = ((Priority) value).getValue();
            } else if(value instanceof TaskSeverity) {
                value = ((TaskSeverity) value).getValue();
            } else if(value instanceof TaskResolution) {
                value = ((TaskResolution) value).getValue();
            } else if(value instanceof Product) {
                value = ((Product) value).getName();
            } else if(value instanceof TaskUserProfile) {
                value = ((TaskUserProfile) value).getRealname();
            } else if(value instanceof TaskStatus) {
                value = ((TaskStatus) value).getValue();
            } else {
                assert value instanceof String : "Wrong value";                 // NOI18N
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    class CancelHighlightDocumentListener implements DocumentListener {
        private JComponent label;
        
        CancelHighlightDocumentListener(JComponent label) {
            this.label = label;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }
    }
    
    private void cancelHighlight(JComponent label) {
        if (!reloading) {
            label.setOpaque(false);
            label.getParent().repaint();
        }
    }    

    class CyclicDependencyDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            Set<Integer> bugs1 = bugs(parentField.getText());
            Set<Integer> bugs2 = bugs(subtaskField.getText());
            bugs1.retainAll(bugs2);
            if (bugs1.isEmpty()) {
                if (cyclicDependency) {
                    cyclicDependency = false;
                    updateMessagePanel();
                }
            } else {
                if (!cyclicDependency) {
                    cyclicDependency = true;
                    updateMessagePanel();
                }
            }
        }

        private Set<Integer> bugs(String values) {
            Set<Integer> bugs = new HashSet<Integer>();
            StringTokenizer st = new StringTokenizer(values, ", \t\n\r\f"); // NOI18N
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                try {
                    bugs.add(Integer.parseInt(token));
                } catch (NumberFormatException nfex) {}
            }
            return bugs;
        }
    }

    class RevalidatingListener implements DocumentListener, Runnable {
        private boolean ignoreUpdate;

        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (ignoreUpdate) return;
            ignoreUpdate = true;
            EventQueue.invokeLater(this);
        }

        @Override
        public void run() {
            revalidate();
            repaint();
            ignoreUpdate = false;
        }

    }

    private class DuplicateListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateNoDuplicateId();
        }
    }
    
    private void updateNoDuplicateId() {
        boolean newNoDuplicateId = "DUPLICATE".equals(resolutionCombo.getSelectedItem()) && "".equals(duplicateField.getText().trim());
        if(newNoDuplicateId != noDuplicateId) {
            noDuplicateId = newNoDuplicateId;
            updateMessagePanel();
        }
    }    
    
    private void updateMessagePanel() {
        messagePanel.removeAll();
        if (noComponent) {
            addMessage("IssuePanel.noComponent"); // NOI18N
        }
        if (noVersion) {
            addMessage("IssuePanel.noVersion"); // NOI18N
        }
        if (noTargetMilestione) {
            addMessage("IssuePanel.noTargetMilestone"); // NOI18N
        }
        if (noSummary) {
            JLabel noSummaryLabel = new JLabel();
            noSummaryLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.noSummary")); // NOI18N
            String icon = issue.isNew() ? "org/netbeans/modules/bugzilla/resources/info.png" : "org/netbeans/modules/bugzilla/resources/error.gif"; // NOI18N
            noSummaryLabel.setIcon(new ImageIcon(ImageUtilities.loadImage(icon)));
            messagePanel.add(noSummaryLabel);
        }
        if (cyclicDependency) {
            JLabel cyclicDependencyLabel = new JLabel();
            cyclicDependencyLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.cyclicDependency")); // NOI18N
            cyclicDependencyLabel.setIcon(new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugzilla/resources/error.gif"))); // NOI18N
            messagePanel.add(cyclicDependencyLabel);
        }
        if (invalidTag) {
            JLabel invalidKeywordLabel = new JLabel();
            invalidKeywordLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.invalidTag")); // NOI18N
            invalidKeywordLabel.setIcon(new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugzilla/resources/error.gif"))); // NOI18N
            messagePanel.add(invalidKeywordLabel);
        }
        if (noDuplicateId) {
            JLabel noDuplicateLabel = new JLabel();
            noDuplicateLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.noDuplicateId")); // NOI18N
            noDuplicateLabel.setIcon(new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugzilla/resources/error.gif"))); // NOI18N
            messagePanel.add(noDuplicateLabel);
        }
        if (noSummary || cyclicDependency || invalidTag || noComponent || noVersion || noTargetMilestione || noDuplicateId) {
            submitButton.setEnabled(false);
        } else {
            submitButton.setEnabled(true);
        }
        for (String fieldError : fieldErrors) {
            JLabel errorLabel = new JLabel(fieldError);
            errorLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/error.gif", true)); // NOI18N
            messagePanel.add(errorLabel);
        }
        for (String fieldWarning : fieldWarnings) {
            JLabel warningLabel = new JLabel(fieldWarning);
            warningLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/warning.gif", true)); // NOI18N
            messagePanel.add(warningLabel);
        }
        if (noSummary || cyclicDependency || invalidTag || noComponent || noVersion || noTargetMilestione || noDuplicateId || (fieldErrors.size() + fieldWarnings.size() > 0)) {
            messagePanel.setVisible(true);
            messagePanel.revalidate();
        } else {
            messagePanel.setVisible(false);
        }
    }    
    
    void addMessage(String messageKey) {
        JLabel messageLabel = new JLabel();
        messageLabel.setText(NbBundle.getMessage(IssuePanel.class, messageKey));
        String icon = issue.isNew() ? "org/netbeans/modules/bugzilla/resources/info.png" : "org/netbeans/modules/bugzilla/resources/error.gif"; // NOI18N
        messageLabel.setIcon(new ImageIcon(ImageUtilities.loadImage(icon)));
        messagePanel.add(messageLabel);
    }    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        issueTypeLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        statusWarning = new javax.swing.JLabel();
        resolutionLabel = new javax.swing.JLabel();
        resolutionCombo = new javax.swing.JComboBox();
        resolutionWarning = new javax.swing.JLabel();
        duplicateWarning = new javax.swing.JLabel();
        priorityLabel = new javax.swing.JLabel();
        priorityWarning = new javax.swing.JLabel();
        severityLabel = new javax.swing.JLabel();
        severityWarning = new javax.swing.JLabel();
        tagsLabel = new javax.swing.JLabel();
        tagsWarning = new javax.swing.JLabel();
        productLabel = new javax.swing.JLabel();
        productWarning = new javax.swing.JLabel();
        componentLabel = new javax.swing.JLabel();
        componentWarning = new javax.swing.JLabel();
        releaseLabel = new javax.swing.JLabel();
        releaseWarning = new javax.swing.JLabel();
        foundInLabel = new javax.swing.JLabel();
        foundInField = new javax.swing.JTextField();
        foundInWarning = new javax.swing.JLabel();
        iterationLabel = new javax.swing.JLabel();
        iterationWarning = new javax.swing.JLabel();
        estimateLabel = new javax.swing.JLabel();
        estimateField = new javax.swing.JTextField();
        estimateWarning = new javax.swing.JLabel();
        dueDateLabel = new javax.swing.JLabel();
        dueDateField = new javax.swing.JTextField();
        dueDateWarning = new javax.swing.JLabel();
        parentLabel = new javax.swing.JLabel();
        parentWarning = new javax.swing.JLabel();
        ownerLabel = new javax.swing.JLabel();
        ownerCombo = new javax.swing.JComboBox();
        ownerWarning = new javax.swing.JLabel();
        subtaskLabel = new javax.swing.JLabel();
        subtaskWarning = new javax.swing.JLabel();
        externalLabel = new javax.swing.JLabel();
        externalWarning = new javax.swing.JLabel();
        ccLabel = new javax.swing.JLabel();
        ccWarning = new javax.swing.JLabel();
        attachmentsLabel = new javax.swing.JLabel();
        dummyAttachmentsPanel = new javax.swing.JPanel();
        summaryLabel = new javax.swing.JLabel();
        summaryWarning = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        addCommentLabel = new javax.swing.JLabel();
        dummyCommentsPanel = new javax.swing.JPanel();
        separator = new javax.swing.JSeparator();
        messagePanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        separatorLabel = new javax.swing.JLabel();
        separatorLabel3 = new javax.swing.JLabel();
        dummyDescriptionPanel = new javax.swing.JPanel();
        dummyAddCommentPanel = new javax.swing.JPanel();

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(issueTypeLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.issueTypeLabel.text_1")); // NOI18N

        issueTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                issueTypeComboActionPerformed(evt);
            }
        });

        reportedLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(reportedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reportedLabel.text_1")); // NOI18N

        reportedField.setEditable(false);
        reportedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reportedField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reportedFieldActionPerformed(evt);
            }
        });

        modifiedLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(modifiedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.modifiedLabel.text_1")); // NOI18N

        modifiedField.setEditable(false);
        modifiedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modifiedField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifiedFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(statusLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusLabel.text_1")); // NOI18N

        statusCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(resolutionLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionLabel.text")); // NOI18N

        resolutionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolutionComboActionPerformed(evt);
            }
        });

        duplicateField.setColumns(15);
        duplicateField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(duplicateButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateButton.text_1")); // NOI18N
        duplicateButton.setFocusPainted(false);
        duplicateButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        duplicateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(priorityLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.priorityLabel.text_1")); // NOI18N

        priorityCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                priorityComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(severityLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.severityLabel.text")); // NOI18N

        severityCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                severityComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(tagsLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.tagsLabel.text")); // NOI18N

        tagsField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.tagsField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(tagsButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.tagsButton.text")); // NOI18N
        tagsButton.setFocusPainted(false);
        tagsButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tagsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tagsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(productLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.productLabel.text_1")); // NOI18N

        productCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(componentLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.componentLabel.text_1")); // NOI18N

        componentCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                componentComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(releaseLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.releaseLabel.text_1")); // NOI18N

        releaseCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                releaseComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(foundInLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.foundInLabel.text")); // NOI18N

        foundInField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.foundInField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(iterationLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.iterationLabel.text_1")); // NOI18N

        iterationCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iterationComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(estimateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.estimateLabel.text")); // NOI18N

        estimateField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.estimateField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dueDateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dueDateLabel.text")); // NOI18N

        dueDateField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dueDateField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(parentLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.parentLabel.text_1")); // NOI18N

        parentField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(parentButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.parentButton.text_1")); // NOI18N
        parentButton.setFocusPainted(false);
        parentButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        parentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parentButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(ownerLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.ownerLabel.text_1")); // NOI18N

        ownerCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ownerComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(subtaskLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.subtaskLabel.text")); // NOI18N

        subtaskField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(subtaskButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.subtaskButton.text")); // NOI18N
        subtaskButton.setFocusPainted(false);
        subtaskButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        subtaskButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subtaskButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(externalLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.externalLabel.text")); // NOI18N

        externalField.setColumns(15);
        externalField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                externalFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(externalButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.externalButton.text")); // NOI18N
        externalButton.setFocusPainted(false);
        externalButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        externalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                externalButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(ccLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.ccLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ccButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.ccButton.text")); // NOI18N
        ccButton.setFocusPainted(false);
        ccButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ccButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(attachmentsLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachmentsLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(summaryLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.summaryLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.descriptionLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addCommentLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.addCommentLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(submitButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitButton.text_1")); // NOI18N
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.cancelButton.text_1")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        messagePanel.setLayout(new javax.swing.BoxLayout(messagePanel, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel1.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        headerField.setEditable(false);
        headerField.setBackground(new java.awt.Color(0, 204, 102));
        headerField.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        org.openide.awt.Mnemonics.setLocalizedText(reloadButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reloadButton.text_1")); // NOI18N
        reloadButton.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reloadButton.toolTipText_1")); // NOI18N
        reloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadButtonActionPerformed(evt);
            }
        });

        separatorLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(showInBrowserButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.showInBrowserButton.text_1")); // NOI18N
        showInBrowserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showInBrowserButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.text_1")); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.toolTipText_1")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        separatorLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reloadButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showInBrowserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(headerField)
                    .addComponent(refreshButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(showInBrowserButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(reloadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(separatorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(separatorLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout dummyDescriptionPanelLayout = new javax.swing.GroupLayout(dummyDescriptionPanel);
        dummyDescriptionPanel.setLayout(dummyDescriptionPanelLayout);
        dummyDescriptionPanelLayout.setHorizontalGroup(
            dummyDescriptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        dummyDescriptionPanelLayout.setVerticalGroup(
            dummyDescriptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout dummyAddCommentPanelLayout = new javax.swing.GroupLayout(dummyAddCommentPanel);
        dummyAddCommentPanel.setLayout(dummyAddCommentPanelLayout);
        dummyAddCommentPanelLayout.setHorizontalGroup(
            dummyAddCommentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        dummyAddCommentPanelLayout.setVerticalGroup(
            dummyAddCommentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(descriptionLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(summaryLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(subtaskLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(parentLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(ownerLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(foundInLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(productLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(tagsLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(priorityLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(statusLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(issueTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(attachmentsLabel))
                    .addComponent(addCommentLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(foundInField)
                                    .addComponent(ownerCombo, 0, 167, Short.MAX_VALUE)
                                    .addComponent(productCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(ownerWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(estimateLabel))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(foundInWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(dueDateLabel))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(productWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(componentLabel))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(parentField, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                                    .addComponent(subtaskField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(parentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(parentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(subtaskButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(subtaskWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ccLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(externalLabel, javax.swing.GroupLayout.Alignment.TRAILING))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(estimateField, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(dueDateField, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(componentCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(estimateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(componentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(dueDateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(releaseLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(iterationLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(iterationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(iterationWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(releaseCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(releaseWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(externalField)
                                    .addComponent(ccField))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(externalButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(externalWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(ccButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ccWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(submitButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cancelButton))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(issueTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(issueTypeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(reportedLabel)
                                        .addGap(18, 18, 18)
                                        .addComponent(reportedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(modifiedLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(modifiedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(statusWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(priorityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(resolutionLabel)
                                            .addComponent(severityLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(resolutionCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(resolutionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(duplicateField, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(duplicateButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(duplicateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(severityCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(severityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(tagsField, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tagsButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tagsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(messagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(20, 20, 20))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(dummyAddCommentPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(dummyDescriptionPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(dummyAttachmentsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(summaryField, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGap(6, 6, 6)
                                .addComponent(summaryWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(separator)
                    .addComponent(dummyCommentsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {issueTypeCombo, iterationCombo, priorityCombo, releaseCombo, resolutionCombo, severityCombo, statusCombo});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(issueTypeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(priorityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(issueTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(issueTypeLabel)
                            .addComponent(reportedLabel)
                            .addComponent(reportedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(modifiedLabel)
                            .addComponent(modifiedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(statusLabel))
                            .addComponent(statusWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(duplicateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(resolutionLabel)
                                .addComponent(duplicateButton))
                            .addComponent(resolutionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(resolutionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(duplicateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(priorityLabel))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(severityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(severityLabel)))
                            .addComponent(severityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tagsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tagsButton)
                                    .addComponent(tagsLabel))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(releaseCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(releaseLabel))
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(componentCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(componentLabel))
                                            .addComponent(releaseWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(componentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(estimateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(iterationWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(iterationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(iterationLabel))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(1, 1, 1)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(productWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(productLabel)
                                                .addComponent(productCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(foundInWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(foundInField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(foundInLabel)
                                                .addComponent(dueDateLabel)
                                                .addComponent(dueDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(dueDateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(ownerWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(ownerLabel)
                                                .addComponent(ownerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(estimateLabel)
                                                .addComponent(estimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(2, 2, 2)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(parentLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(ccButton, javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(parentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                            .addComponent(parentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(parentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(subtaskWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(subtaskField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(subtaskLabel)
                                                        .addComponent(subtaskButton)
                                                        .addComponent(externalLabel)
                                                        .addComponent(externalButton)
                                                        .addComponent(externalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(24, 24, 24)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(dummyAttachmentsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(attachmentsLabel))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(summaryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(summaryLabel))
                                                    .addComponent(summaryWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(ccField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(ccLabel))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(ccWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(externalWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(descriptionLabel)
                                    .addComponent(dummyDescriptionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(tagsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addCommentLabel)
                    .addComponent(dummyAddCommentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(submitButton)
                    .addComponent(cancelButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dummyCommentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 11, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );
    }// </editor-fold>//GEN-END:initComponents

    
    
    private void showInBrowserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showInBrowserButtonActionPerformed
//        try {
//            URL url = new URL(issue.getRepository().getUrl() + BugzillaConstants.URL_SHOW_BUG + issue.getID());
//            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
//        } catch (MalformedURLException muex) {
//            Bugzilla.LOG.log(Level.INFO, "Unable to show the issue in the browser.", muex); // NOI18N
//        }
    }//GEN-LAST:event_showInBrowserButtonActionPerformed

    private void reloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadButtonActionPerformed
//        String reloadMessage = NbBundle.getMessage(IssuePanel.class, "IssuePanel.reloadMessage"); // NOI18N
//        final ProgressHandle handle = ProgressHandleFactory.createHandle(reloadMessage);
//        handle.start();
//        handle.switchToIndeterminate();
//        skipReload = true;
//        enableComponents(false);
//        RP.post(new Runnable() {
//            @Override
//            public void run() {
//                issue.getRepository().refreshConfiguration();
//                EventQueue.invokeLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            reloading = true;
//                            Object product = productCombo.getSelectedItem();
//                            Object platform = platformCombo.getSelectedItem();
//                            Object os = osCombo.getSelectedItem();
//                            Object priority = priorityCombo.getSelectedItem();
//                            Object severity = severityCombo.getSelectedItem();
//                            Object resolution = resolutionCombo.getSelectedItem();
//                            Object issueType = issueTypeCombo.getSelectedItem();
//                            initCombos();
//                            initCustomFields();
//                            selectInCombo(productCombo, product, false);
//                            selectInCombo(platformCombo, platform, false);
//                            selectInCombo(osCombo, os, false);
//                            selectInCombo(priorityCombo, priority, false);
//                            selectInCombo(severityCombo, severity, false);
//                            initStatusCombo(statusCombo.getSelectedItem().toString());
//                            selectInCombo(resolutionCombo, resolution, false);
//                            if (BugzillaUtil.isNbRepository(issue.getRepository())) {
//                                issueTypeCombo.setSelectedItem(issueType);
//                            }
//                            reloadCustomFields(true);
//                        } finally {
//                            reloading = false;
//                            enableComponents(true);
//                            skipReload = false;
//                        }
//                    }
//                });
//                handle.finish();
//            }
//        });
    }//GEN-LAST:event_reloadButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
//        String refreshMessageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshMessage"); // NOI18N
//        String refreshMessage = MessageFormat.format(refreshMessageFormat, issue.getID());
//        final ProgressHandle handle = ProgressHandleFactory.createHandle(refreshMessage);
//        handle.start();
//        handle.switchToIndeterminate();
//        skipReload = true;
//        enableComponents(false);
//        RP.post(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    issue.refresh();
//                } finally {
//                    EventQueue.invokeLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            enableComponents(true);
//                            skipReload = false;
//                        }
//                    });
//                    handle.finish();
//                    reloadFormInAWT(true);
//                }
//            }
//        });
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void issueTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_issueTypeComboActionPerformed
//        cancelHighlight(issueTypeLabel);
    }//GEN-LAST:event_issueTypeComboActionPerformed

    private void statusComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusComboActionPerformed
        cancelHighlight(statusLabel);
        resolutionCombo.setVisible(false);
        resolutionLabel.setVisible(false);
        // Hide/show resolution combo
        C2CData cd = DummyUtils.getClientData(issue.getRepository().getTaskRepository());
        String initialStatus = initialValues.get(IssueField.STATUS.getKey());
        boolean resolvedInitial = RESOLUTION_RESOLVED.equals(initialStatus); // NOI18N
        if (!resolvedInitial) {
            Object item = statusCombo.getSelectedItem();
            if(!(item instanceof TaskStatus)) {
                return;
            }
            TaskStatus status = (TaskStatus) statusCombo.getSelectedItem();
            TaskStatus resolvedStatus = cd.getStatusByValue(RESOLUTION_RESOLVED);
            if (resolvedStatus.equals(status)) { // NOI18N
                TaskResolution fixedResolution = C2CUtil.getResolutionByValue(cd, STATUS_FIXED);
                resolutionCombo.setSelectedItem(fixedResolution); 
                resolutionCombo.setVisible(true);
                resolutionLabel.setVisible(true);
            } else {
                resolutionCombo.setVisible(false);
                resolutionLabel.setVisible(false);
                duplicateField.setVisible(false);
                duplicateButton.setVisible(false);
            }
        }
        if (statusCombo.getSelectedIndex() >= resolvedIndex) {
            resolutionCombo.setVisible(true);
            resolutionLabel.setVisible(true);
        } else {
            resolutionCombo.setVisible(false);
            resolutionLabel.setVisible(false);
        }
        duplicateField.setVisible(false);
        duplicateButton.setVisible(false);
    }//GEN-LAST:event_statusComboActionPerformed

    private void duplicateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateButtonActionPerformed
        findIssue(duplicateField, "IssuePanel.duplicateButton.message", "org.netbeans.modules.c2c.duplicateChooser", false);
    }//GEN-LAST:event_duplicateButtonActionPerformed

    private void priorityComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_priorityComboActionPerformed
//        cancelHighlight(priorityLabel);
    }//GEN-LAST:event_priorityComboActionPerformed

    private void severityComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_severityComboActionPerformed
//        cancelHighlight(priorityLabel);
    }//GEN-LAST:event_severityComboActionPerformed

    private void productComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productComboActionPerformed
        cancelHighlight(productLabel);
        
        Object o = productCombo.getSelectedItem();
        if(o == null || !(o instanceof Product)) {
            return;
        }
        Product product = (Product) o;
        
        // Reload componentCombo, versionCombo and targetMilestoneCombo
        C2CData cd = DummyUtils.getClientData(issue.getRepository().getTaskRepository());
        
        Object component = componentCombo.getSelectedItem();
        Object version = releaseCombo.getSelectedItem();
        componentCombo.setModel(toComboModel(product.getComponents()));
        releaseCombo.setModel(toComboModel(product.getReleaseTags()));
        
        // Attempt to keep selection
        boolean isNew = issue.isNew();
        if (!isNew && !selectInCombo(componentCombo, component, false) && (componentCombo.getModel().getSize()>1)) {
            componentCombo.setSelectedItem(null);
        }
        if (!isNew && !selectInCombo(releaseCombo, version, false) && (releaseCombo.getModel().getSize() > 1)) {
            releaseCombo.setSelectedItem(null);
        }
        if (issue.isNew()) {
            issue.setFieldValue(IssueField.PRODUCT, product.getName());
            AbstractRepositoryConnector connector = C2C.getInstance().getRepositoryConnector();
            
            TaskData data = issue.getTaskData();
//            try {
                // throws NPE
//                connector.getTaskDataHandler().initializeTaskData(issue.getRepository().getTaskRepository(), data, connector.getTaskMapping(data), new NullProgressMonitor());
                initialValues.remove(IssueField.COMPONENT.getKey());
                initialValues.remove(IssueField.VERSION.getKey());
                initialValues.remove(IssueField.MILESTONE.getKey());
                reloadForm(false);
//            } catch (CoreException cex) {
//                C2C.LOG.log(Level.INFO, cex.getMessage(), cex);
//            }
        }
    }//GEN-LAST:event_productComboActionPerformed

    private void componentComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_componentComboActionPerformed
//        cancelHighlight(componentLabel);
//        updateNoComponent();
    }//GEN-LAST:event_componentComboActionPerformed

    private void releaseComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_releaseComboActionPerformed
        cancelHighlight(releaseLabel);
        updateNoRelease();
    }//GEN-LAST:event_releaseComboActionPerformed

    private void iterationComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iterationComboActionPerformed
        cancelHighlight(iterationLabel);
        updateNoIteration();
    }//GEN-LAST:event_iterationComboActionPerformed

    private void parentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parentButtonActionPerformed
        findIssue(parentField, "IssuePanel.parentButton.message", "org.netbeans.modules.c2c.parentChooser", true); // NOI18N
    }//GEN-LAST:event_parentButtonActionPerformed

    private void subtaskButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subtaskButtonActionPerformed
        findIssue(subtaskField, "IssuePanel.subtaskButton.message", "org.netbeans.modules.c2c.subtaskChooser", true); // NOI18N
    }//GEN-LAST:event_subtaskButtonActionPerformed

    private void externalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_externalButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_externalButtonActionPerformed

    private void externalFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_externalFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_externalFieldActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
//        final boolean isNew = issue.getTaskData().isNew();
//        if (isNew) {
//            storeFieldValue(IssueField.DESCRIPTION, addCommentArea);
//        }
//        storeFieldValue(IssueField.SUMMARY, summaryField);
//        storeFieldValue(IssueField.PRODUCT, productCombo);
//        storeFieldValue(IssueField.COMPONENT, componentCombo);
//        storeFieldValue(IssueField.VERSION, versionCombo);
//        storeFieldValue(IssueField.PLATFORM, platformCombo);
//        storeFieldValue(IssueField.OS, osCombo);
//        storeFieldValue(IssueField.STATUS, statusCombo);
//        if (resolutionCombo.isVisible()) {
//            storeFieldValue(IssueField.RESOLUTION, resolutionCombo);
//        } else if (!resolutionField.isVisible()) {
//            storeFieldValue(IssueField.RESOLUTION, ""); // NOI18N
//        }
//        if (duplicateField.isVisible() && duplicateField.isEditable()) {
//            issue.duplicate(duplicateField.getText());
//        }
//        storeFieldValue(IssueField.PRIORITY, priorityCombo);
//        if (BugzillaUtil.isNbRepository(issue.getRepository())) {
//            storeFieldValue(IssueField.ISSUE_TYPE, issueTypeCombo);
//        }
//        storeFieldValue(IssueField.SEVERITY, severityCombo);
//        if (usingTargetMilestones) {
//            storeFieldValue(IssueField.MILESTONE, targetMilestoneCombo);
//        }
//        storeAssignToDefault();
//        storeFieldValue(IssueField.URL, urlField);
//        storeFieldValue(IssueField.WHITEBOARD, statusWhiteboardField);
//        storeFieldValue(IssueField.KEYWORDS, keywordsField);
//        if (assignedField.getParent() == null) {
//            storeFieldValue(IssueField.ASSIGNED_TO, assignedCombo);
//        } else {
//            storeFieldValue(IssueField.ASSIGNED_TO, assignedField);
//        }
//        storeFieldValue(IssueField.QA_CONTACT, qaContactField);
//        storeCCValue();
//        storeFieldValue(IssueField.DEPENDS_ON, dependsField);
//        storeFieldValue(IssueField.BLOCKS, blocksField);
//        if (!isNew && !"".equals(addCommentArea.getText().trim())) { // NOI18N
//            issue.addComment(addCommentArea.getText());
//        }
//        if (!isNew && issue.hasTimeTracking()) {
//            storeFieldValue(IssueField.ESTIMATED_TIME, estimatedField);
//            storeFieldValue(IssueField.WORK_TIME, workedField);
//            storeFieldValue(IssueField.REMAINING_TIME, remainingField);
//            if(!deadlineField.getText().trim().equals(YYYY_MM_DD)) {
//                storeFieldValue(IssueField.DEADLINE, deadlineField);
//            }
//        }
//        // Store custom fields
//        for (CustomFieldInfo field : customFields) {
//            if (field.comp instanceof JTextComponent) {
//                storeFieldValue(field.field, (JTextComponent)field.comp);
//            } else if (field.comp instanceof JComboBox) {
//                storeFieldValue(field.field, (JComboBox)field.comp);
//            } else if (field.comp instanceof JList) {
//                storeFieldValue(field.field, (JList)field.comp);
//            } else {
//                Bugzilla.LOG.log(Level.INFO, "Custom field component {0} is not supported!", field.comp); // NOI18N
//            }
//        }
//        String submitMessage;
//        if (isNew) {
//            submitMessage = NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitNewMessage"); // NOI18N
//        } else {
//            String submitMessageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitMessage"); // NOI18N
//            submitMessage = MessageFormat.format(submitMessageFormat, issue.getID());
//        }
//        final ProgressHandle handle = ProgressHandleFactory.createHandle(submitMessage);
//        handle.start();
//        handle.switchToIndeterminate();
//        skipReload = true;
//        enableComponents(false);
//        RP.post(new Runnable() {
//            @Override
//            public void run() {
//                boolean ret = false;
//                try {
//                    ret = issue.submitAndRefresh();
//                    for (AttachmentsPanel.AttachmentInfo attachment : attachmentsPanel.getNewAttachments()) {
//                        if (attachment.file.exists() && attachment.file.isFile()) {
//                            if (attachment.description.trim().length() == 0) {
//                                attachment.description = NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachment.noDescription"); // NOI18N
//                            }
//                            issue.addAttachment(attachment.file, null, attachment.description, attachment.contentType, attachment.isPatch); // NOI18N
//                        } else {
//                            // PENDING notify user
//                        }
//                    }
//                    if(attachLogCheckBox.isVisible() && attachLogCheckBox.isSelected()) {
//                        File f = new File(Places.getUserDirectory(), NbBugzillaConstants.NB_LOG_FILE_PATH);
//                        if(f.exists()) {
//                            issue.addAttachment(f, "", NbBundle.getMessage(IssuePanel.class, "MSG_LOG_FILE_DESC"), NbBugzillaConstants.NB_LOG_FILE_ATT_CONT_TYPE, false); // NOI18N
//                        }
//                        BugzillaConfig.getInstance().putAttachLogFile(true);
//                    } else {
//                        BugzillaConfig.getInstance().putAttachLogFile(false);
//                    }
//                } finally {
//                    EventQueue.invokeLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            enableComponents(true);
//                            skipReload = false;
//                        }
//                    });
//                    handle.finish();
//                    if(ret) {
//                        if (isNew) {
//                            // Show all custom fields, not only the ones shown on bug creation
//                            EventQueue.invokeLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    initCustomFields();
//                                }
//                            });
//                        }
//                        reloadFormInAWT(true);
//                    }
//                }
//            }
//        });
//        if (isNew) {
//            BugzillaRepository repository = issue.getRepository();
//            if (repository != null) {
//                OwnerUtils.setLooseAssociation(BugzillaUtil.getRepository(repository), false);
//            }
//        }
    }//GEN-LAST:event_submitButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
//        reloadForm(true);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void tagsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tagsButtonActionPerformed
        String message = NbBundle.getMessage(IssuePanel.class, "IssuePanel.tagsButton.message"); // NOI18N
        String tags = C2CUtil.getTags(message, tagsField.getText(), issue.getRepository());
        tagsField.setText(tags);
    }//GEN-LAST:event_tagsButtonActionPerformed

    private void reportedFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reportedFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_reportedFieldActionPerformed

    private void modifiedFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifiedFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_modifiedFieldActionPerformed

    private void duplicateFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_duplicateFieldActionPerformed

    private void ownerComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ownerComboActionPerformed
        cancelHighlight(ownerLabel);
    }//GEN-LAST:event_ownerComboActionPerformed

    private void resolutionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolutionComboActionPerformed
//        cancelHighlight(resolutionCombo); XXX
//        if (resolutionCombo.getParent() == null) {
//            return;
//        }
        TaskResolution duplicate = C2CUtil.getResolutionByValue(DummyUtils.getClientData(issue.getRepository().getTaskRepository()), RESOLUTION_DUPLICATE);
        boolean shown = duplicate.equals(resolutionCombo.getSelectedItem()); // NOI18N
        duplicateField.setVisible(shown);
        duplicateButton.setVisible(shown && duplicateField.isEditable());
        updateNoDuplicateId();
    }//GEN-LAST:event_resolutionComboActionPerformed

    private void ccButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccButtonActionPerformed
        String message = NbBundle.getMessage(IssuePanel.class, "IssuePanel.ccButton.message"); // NOI18N
        String users = C2CUtil.getUsers(message, ccField.getText(), issue.getRepository());
        ccField.setText(users);
    }//GEN-LAST:event_ccButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addCommentLabel;
    private javax.swing.JLabel attachmentsLabel;
    final javax.swing.JButton cancelButton = new javax.swing.JButton();
    final javax.swing.JButton ccButton = new javax.swing.JButton();
    final javax.swing.JTextField ccField = new javax.swing.JTextField();
    private javax.swing.JLabel ccLabel;
    private javax.swing.JLabel ccWarning;
    final javax.swing.JComboBox componentCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel componentLabel;
    private javax.swing.JLabel componentWarning;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextField dueDateField;
    private javax.swing.JLabel dueDateLabel;
    private javax.swing.JLabel dueDateWarning;
    private javax.swing.JPanel dummyAddCommentPanel;
    private javax.swing.JPanel dummyAttachmentsPanel;
    private javax.swing.JPanel dummyCommentsPanel;
    private javax.swing.JPanel dummyDescriptionPanel;
    final javax.swing.JButton duplicateButton = new javax.swing.JButton();
    final javax.swing.JTextField duplicateField = new javax.swing.JTextField();
    private javax.swing.JLabel duplicateWarning;
    private javax.swing.JTextField estimateField;
    private javax.swing.JLabel estimateLabel;
    private javax.swing.JLabel estimateWarning;
    final javax.swing.JButton externalButton = new javax.swing.JButton();
    final javax.swing.JTextField externalField = new javax.swing.JTextField();
    private javax.swing.JLabel externalLabel;
    private javax.swing.JLabel externalWarning;
    private javax.swing.JTextField foundInField;
    private javax.swing.JLabel foundInLabel;
    private javax.swing.JLabel foundInWarning;
    final javax.swing.JTextField headerField = new javax.swing.JTextField();
    final javax.swing.JComboBox issueTypeCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel issueTypeLabel;
    final javax.swing.JLabel issueTypeWarning = new javax.swing.JLabel();
    final javax.swing.JComboBox iterationCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel iterationLabel;
    private javax.swing.JLabel iterationWarning;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel messagePanel;
    final javax.swing.JTextField modifiedField = new javax.swing.JTextField();
    final javax.swing.JLabel modifiedLabel = new javax.swing.JLabel();
    private javax.swing.JComboBox ownerCombo;
    private javax.swing.JLabel ownerLabel;
    private javax.swing.JLabel ownerWarning;
    final javax.swing.JButton parentButton = new javax.swing.JButton();
    final javax.swing.JTextField parentField = new javax.swing.JTextField();
    private javax.swing.JLabel parentLabel;
    private javax.swing.JLabel parentWarning;
    final javax.swing.JComboBox priorityCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel priorityLabel;
    private javax.swing.JLabel priorityWarning;
    final javax.swing.JComboBox productCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel productLabel;
    private javax.swing.JLabel productWarning;
    final org.netbeans.modules.bugtracking.util.LinkButton refreshButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JComboBox releaseCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel releaseLabel;
    private javax.swing.JLabel releaseWarning;
    final org.netbeans.modules.bugtracking.util.LinkButton reloadButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JTextField reportedField = new javax.swing.JTextField();
    final javax.swing.JLabel reportedLabel = new javax.swing.JLabel();
    private javax.swing.JComboBox resolutionCombo;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JLabel resolutionWarning;
    private javax.swing.JSeparator separator;
    private javax.swing.JLabel separatorLabel;
    private javax.swing.JLabel separatorLabel3;
    final javax.swing.JComboBox severityCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel severityLabel;
    private javax.swing.JLabel severityWarning;
    final org.netbeans.modules.bugtracking.util.LinkButton showInBrowserButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JComboBox statusCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusWarning;
    final javax.swing.JButton submitButton = new javax.swing.JButton();
    final javax.swing.JButton subtaskButton = new javax.swing.JButton();
    final javax.swing.JTextField subtaskField = new javax.swing.JTextField();
    private javax.swing.JLabel subtaskLabel;
    private javax.swing.JLabel subtaskWarning;
    final javax.swing.JTextField summaryField = new javax.swing.JTextField();
    private javax.swing.JLabel summaryLabel;
    private javax.swing.JLabel summaryWarning;
    final javax.swing.JButton tagsButton = new javax.swing.JButton();
    final javax.swing.JTextField tagsField = new javax.swing.JTextField();
    private javax.swing.JLabel tagsLabel;
    private javax.swing.JLabel tagsWarning;
    // End of variables declaration//GEN-END:variables

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize(); // IssueProvider 176085
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return getUnitIncrement();
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return (orientation==SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        JScrollPane scrollPane = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (scrollPane!=null) {
             // IssueProvider 176085
            int minWidth = getMinimumSize().width;
            int width = scrollPane.getSize().width;
            Insets insets = scrollPane.getInsets();
            width -= insets.left+insets.right;
            Border border = scrollPane.getViewportBorder();
            if (border != null) {
                insets = border.getBorderInsets(scrollPane);
                width -= insets.left+insets.right;
            }
            JComponent vsb = scrollPane.getVerticalScrollBar();
            if (vsb!=null && vsb.isVisible()) {
                width -= vsb.getSize().width;
            }
            if (minWidth>width) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    private int unitIncrement;
    private int getUnitIncrement() {
        if (unitIncrement == 0) {
            Font font = UIManager.getFont("Label.font"); // NOI18N
            if (font != null) {
                unitIncrement = (int)(font.getSize()*1.5);
            }
        }
        return unitIncrement;
    }

}
