/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jira.issue;

import com.atlassian.connector.eclipse.internal.jira.core.IJiraConstants;
import com.atlassian.connector.eclipse.internal.jira.core.JiraAttribute;
import com.atlassian.connector.eclipse.internal.jira.core.JiraRepositoryConnector;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.issuetable.TableSorter;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCacheUtils;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.bugtracking.util.RepositoryUserRenderer;
import org.netbeans.modules.bugtracking.util.UIUtils;
import org.netbeans.modules.bugtracking.util.UndoRedoSupport;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.kenai.KenaiRepository;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.util.JiraUtils;
import org.netbeans.modules.jira.util.PriorityRenderer;
import org.netbeans.modules.jira.util.ProjectRenderer;
import org.netbeans.modules.jira.util.ResolutionRenderer;
import org.netbeans.modules.jira.util.StatusRenderer;
import org.netbeans.modules.jira.util.TypeRenderer;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.openide.awt.HtmlBrowser;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Stola
 */
public class IssuePanel extends javax.swing.JPanel implements Scrollable {
    private static final RequestProcessor RP = new RequestProcessor("JIRA Issue Panel", 5, false); // NOI18N
    private static final Color ORIGINAL_ESTIMATE_COLOR = new Color(137, 175, 215);
    private static final Color REMAINING_ESTIMATE_COLOR = new Color(236, 142, 0);
    private static final Color TIME_SPENT_COLOR = new Color(81, 168, 37);
    private static final Color HIGHLIGHT_COLOR = new Color(217, 255, 217);
    private NbJiraIssue issue;
    private CommentsPanel commentsPanel;
    private AttachmentsPanel attachmentsPanel;
    private IssueLinksPanel issueLinksPanel;
    private boolean skipReload;
    private boolean reloading;
    private Map<NbJiraIssue.IssueField,Object> initialValues = new EnumMap<NbJiraIssue.IssueField,Object>(NbJiraIssue.IssueField.class);
    private PropertyChangeListener tasklistListener;
    private UndoRedoSupport undoRedoSupport;

    public IssuePanel() {
        initComponents();
        updateReadOnlyField(createdField);
        updateReadOnlyField(updatedField);
        updateReadOnlyField(originalEstimateField);
        updateReadOnlyField(remainingEstimateField);
        updateReadOnlyField(timeSpentField);
        updateReadOnlyField(resolutionField);
        updateReadOnlyField(projectField);
        updateReadOnlyField(statusField);
        customFieldPanelLeft.setBackground(getBackground());
        customFieldPanelRight.setBackground(getBackground());
        parentHeaderPanel.setBackground(getBackground());
        UIUtils.fixFocusTraversalKeys(environmentArea);
        UIUtils.fixFocusTraversalKeys(addCommentArea);
        UIUtils.issue163946Hack(componentScrollPane);
        UIUtils.issue163946Hack(affectsVersionScrollPane);
        UIUtils.issue163946Hack(fixVersionScrollPane);
        UIUtils.issue163946Hack(environmentScrollPane);
        UIUtils.issue163946Hack(addCommentScrollPane);
        summaryField.setPreferredSize(summaryField.getMinimumSize());
        initAttachmentsPanel();
        initIssueLinksPanel();
        initSpellChecker();
        initDefaultButton();
        attachFieldStatusListeners();
        attachHideStatusListener();
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

    private void updateReadOnlyField(JTextField field) {
        if ("GTK".equals(UIManager.getLookAndFeel().getID())) { // NOI18N
            field.setUI(new BasicTextFieldUI());
        }
        field.setBackground(getBackground());
    }

    NbJiraIssue getIssue() {
        return issue;
    }

    void setIssue(NbJiraIssue issue) {
        if (this.issue == null) {
            attachIssueListener(issue);
        }
        this.issue = issue;
        try {
            reloading = true;
            initRenderers();
            initProjectCombo();
            initPriorityCombo();
            initResolutionCombo();
            initHeaderLabel();
            initCommentsPanel();
            initAssigneeCombo();
        } finally {
            reloading = false;
        }

        reloadForm(true);
    }

    private void initIssueLinksPanel() {
        issueLinksPanel = new IssueLinksPanel();
        GroupLayout layout = (GroupLayout)getLayout();
        layout.replace(dummyIssueLinksPanel, issueLinksPanel);
        issueLinksLabel.setLabelFor(issueLinksPanel);
    }

    private void initAttachmentsPanel() {
        attachmentsPanel = new AttachmentsPanel();
        GroupLayout layout = (GroupLayout)getLayout();
        layout.replace(dummyAttachmentPanel, attachmentsPanel);
        attachmentLabel.setLabelFor(attachmentsPanel);
    }

    private void initRenderers() {
        // Project combo
        projectCombo.setRenderer(new ProjectRenderer());

        // Issue type combo
        issueTypeCombo.setRenderer(new TypeRenderer());

        // Priority combo
        priorityCombo.setRenderer(new PriorityRenderer());

        // Component list
        componentList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof com.atlassian.connector.eclipse.internal.jira.core.model.Component) {
                    value = ((com.atlassian.connector.eclipse.internal.jira.core.model.Component)value).getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        // Status combo
        statusCombo.setRenderer(new StatusRenderer());

        // Resolution combo
        resolutionCombo.setRenderer(new ResolutionRenderer());
    }

    private void initProjectCombo() {
        Project[] projects = issue.getRepository().getConfiguration().getProjects();
        DefaultComboBoxModel model = new DefaultComboBoxModel(projects);
        model.setSelectedItem(null); // Make sure nothing is pre-selected
        projectCombo.setModel(model);
    }

    private void initPriorityCombo() {
        Priority[] priority = issue.getRepository().getConfiguration().getPriorities();
        DefaultComboBoxModel model = new DefaultComboBoxModel(priority);
        priorityCombo.setModel(model);
    }

    private void initStatusCombo(JiraStatus status) {
        List<JiraStatus> statusList = allowedStatusTransitions(status);
        JiraStatus[] statuses = statusList.toArray(new JiraStatus[statusList.size()]);
        DefaultComboBoxModel model = new DefaultComboBoxModel(statuses);
        statusCombo.setModel(model);
    }

    private void initResolutionCombo() {
        Resolution[] resolution = issue.getRepository().getConfiguration().getResolutions();
        DefaultComboBoxModel model = new DefaultComboBoxModel(resolution);
        resolutionCombo.setModel(model);
    }

    private void initAssigneeCombo() {
        assigneeCombo.setRenderer(new RepositoryUserRenderer());
        RP.post(new Runnable() {
            @Override
            public void run() {
                final Collection<RepositoryUser> users = issue.getRepository().getUsers();
                final DefaultComboBoxModel assignedModel = new DefaultComboBoxModel();
                for (RepositoryUser user: users) {
                    assignedModel.addElement(user);
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        reloading = true;
                        try {
                            Object assignee = (assigneeField.getParent() == null) ? assigneeCombo.getSelectedItem() : assigneeField.getText();
                            if (assignee == null) {
                                assignee = ""; //NOI18N
                            }
                            assigneeCombo.setModel(assignedModel);
                            GroupLayout layout = (GroupLayout)getLayout();
                            if ((assigneeCombo.getParent()==null) != users.isEmpty()) {
                                layout.replace(users.isEmpty() ? assigneeCombo : assigneeField, users.isEmpty() ? assigneeField : assigneeCombo);
                                assigneeLabel.setLabelFor(users.isEmpty() ? assigneeField : assigneeCombo);
                            }
                            if (assigneeField.getParent() == null) {
                                assigneeCombo.setSelectedItem(assignee);
                            } else {
                                assigneeField.setText(assignee.toString());
                            }
                        } finally {
                            reloading = false;
                        }
                    }
                });
            }
        });
    }

    private void initHeaderLabel() {
        Font font = headerLabel.getFont();
        headerLabel.setFont(font.deriveFont((float)(font.getSize()*1.7)));
    }

    private void initCommentsPanel() {
        commentsPanel = new CommentsPanel();
        commentsPanel.setNewCommentHandler(new CommentsPanel.NewCommentHandler() {
            @Override
            public void append(String text) {
                addCommentArea.append(text);
                addCommentArea.requestFocus();
                scrollRectToVisible(addCommentScrollPane.getBounds());
            }
        });
        GroupLayout layout = (GroupLayout)getLayout();
        layout.replace(dummyCommentPanel, commentsPanel);
    }

    private void initSpellChecker () {
        Spellchecker.register(summaryField);
        Spellchecker.register(addCommentArea);
    }

    private Map<String,JLabel> customFieldLabels = new HashMap<String,JLabel>();
    private Map<String,JComponent> customFieldComponents = new HashMap<String,JComponent>();
    private void initCustomFields() {
        customFieldPanelLeft.removeAll();
        customFieldPanelRight.removeAll();
        List<NbJiraIssue.CustomField> supportedFields = getSupportedCustomFields();
        customFieldPanelLeft.setVisible(!supportedFields.isEmpty());
        customFieldPanelRight.setVisible(!supportedFields.isEmpty());
        if (!supportedFields.isEmpty()) {
            GroupLayout labelLayout = new GroupLayout(customFieldPanelLeft);
            customFieldPanelLeft.setLayout(labelLayout);
            GroupLayout fieldLayout = new GroupLayout(customFieldPanelRight);
            customFieldPanelRight.setLayout(fieldLayout);
            GroupLayout.ParallelGroup labelHorizontalGroup = labelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
            GroupLayout.SequentialGroup labelVerticalGroup = labelLayout.createSequentialGroup();
            GroupLayout.ParallelGroup fieldHorizontalGroup = fieldLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
            GroupLayout.SequentialGroup fieldVerticalGroup = fieldLayout.createSequentialGroup();
            boolean first = true;
            for (NbJiraIssue.CustomField cField : supportedFields) {
                JLabel label = new JLabel(cField.getLabel());
                JTextField field = new JTextField();
                customFieldLabels.put(cField.getId(), label);
                customFieldComponents.put(cField.getId(), field);
                label.setLabelFor(field);
                label.setPreferredSize(new Dimension(label.getPreferredSize().width, field.getPreferredSize().height));
                if (!first) {
                    labelVerticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
                    fieldVerticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
                }
                labelHorizontalGroup.addComponent(label);
                labelVerticalGroup.addComponent(label, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                fieldHorizontalGroup.addComponent(field);
                fieldVerticalGroup.addComponent(field);
                first = false;
            }
            labelLayout.setHorizontalGroup(labelHorizontalGroup);
            labelLayout.setVerticalGroup(labelVerticalGroup);
            fieldLayout.setHorizontalGroup(fieldHorizontalGroup);
            fieldLayout.setVerticalGroup(fieldVerticalGroup);
        }
    }

    private List<NbJiraIssue.CustomField> getSupportedCustomFields() {
        NbJiraIssue.CustomField[] cFields = issue.getCustomFields();
        List<NbJiraIssue.CustomField> supportedFields = new LinkedList<NbJiraIssue.CustomField>();
        for (NbJiraIssue.CustomField cField : cFields) {
            if (isSupportedCustomField(cField)) {
                supportedFields.add(cField);
            }
        }
        return supportedFields;
    }

    private void attachFieldStatusListeners() {
        issueTypeCombo.addActionListener(new CancelHighlightListener(issueTypeLabel));
        statusCombo.addActionListener(new CancelHighlightListener(statusLabel));
        statusCombo.addActionListener(new CancelHighlightListener(resolutionLabel));
        priorityCombo.addActionListener(new CancelHighlightListener(priorityLabel));
        assigneeCombo.addActionListener(new CancelHighlightListener(assigneeLabel));
        dueField.getDocument().addDocumentListener(new CancelHighlightListener(dueLabel));
        assigneeField.getDocument().addDocumentListener(new CancelHighlightListener(assigneeLabel));
        summaryField.getDocument().addDocumentListener(new CancelHighlightListener(summaryLabel));
        environmentArea.getDocument().addDocumentListener(new CancelHighlightListener(environmentLabel));
        componentList.addListSelectionListener(new CancelHighlightListener(componentLabel));
        affectsVersionList.addListSelectionListener(new CancelHighlightListener(affectsVersionLabel));
        fixVersionList.addListSelectionListener(new CancelHighlightListener(fixVersionLabel));
        addCommentArea.getDocument().addDocumentListener(new RevalidatingListener());
        addCommentArea.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                makeCaretVisible(addCommentArea);
            }
        });
    }

    private void attachHideStatusListener() {
        assigneeField.getDocument().addDocumentListener(new DocumentListener() {
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
                if (!reloading) {
                    assigneeStatusLabel.setVisible(false);
                }
            }
        });
    }

    void reloadForm(boolean force) {
        if (skipReload) {
            return;
        }
        reloading = true;

        boolean isNew = issue.getTaskData().isNew();
        headerLabel.setVisible(!isNew || issue.isSubtask());
        createdLabel.setVisible(!isNew);
        createdField.setVisible(!isNew);
        updatedLabel.setVisible(!isNew);
        updatedField.setVisible(!isNew);
        separator.setVisible(!isNew);
        commentsPanel.setVisible(!isNew);
        attachmentLabel.setVisible(!isNew);
        attachmentsPanel.setVisible(!isNew);
        issueLinksLabel.setVisible(!isNew);
        issueLinksPanel.setVisible(!isNew);
        resolutionCombo.setVisible(!isNew);
        resolutionField.setVisible(!isNew);
        originalEstimateLabel.setVisible(!isNew);
        originalEstimateField.setVisible(!isNew);
        originalEstimatePanel.setVisible(!isNew);
        remainingEstimateLabel.setVisible(!isNew);
        remainingEstimateField.setVisible(!isNew);
        remainingEstimatePanel.setVisible(!isNew);
        timeSpentLabel.setVisible(!isNew);
        timeSpentField.setVisible(!isNew);
        timeSpentPanel.setVisible(!isNew);
        originalEstimateLabelNew.setVisible(isNew);
        originalEstimateFieldNew.setVisible(isNew);
        originalEstimateHint.setVisible(isNew);
        logWorkButton2.setVisible(!isNew);
        subtaskLabel.setVisible(!isNew);

        createSubtaskButton.setVisible(false);
        convertToSubtaskButton.setVisible(false);
        dummySubtaskPanel.setVisible(false);

        if (force) {
            initCustomFields();
        }

        // Operations
        boolean startProgressAvailable = false;
        boolean stopProgressAvailable = false;
        boolean resolveIssueAvailable = false;
        boolean closeIssueAvailable = false;
        boolean reopenIssueAvailable = false;
        for (TaskOperation operation : issue.getAvailableOperations().values()) {
            String label = operation.getLabel();
            if (JiraUtils.isStartProgressOperation(label)) {
                startProgressAvailable = true;
            } else if (JiraUtils.isStopProgressOperation(label)) {
                stopProgressAvailable = true;
            } else if (JiraUtils.isResolveOperation(label)) {
                resolveIssueAvailable = true;
            } else if (JiraUtils.isCloseOperation(label)) {
                closeIssueAvailable = true;
            } else if (JiraUtils.isReopenOperation(label)) {
                reopenIssueAvailable = true;
            }
        }
        startProgressButton.setVisible(startProgressAvailable);
        stopProgressButton.setVisible(stopProgressAvailable);
        resolveIssueButton.setVisible(resolveIssueAvailable);
        closeIssueButton.setVisible(closeIssueAvailable);
        reopenIssueButton.setVisible(reopenIssueAvailable);

        org.openide.awt.Mnemonics.setLocalizedText(addCommentLabel, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.description" : "IssuePanel.addCommentLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(submitButton, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.submitButton.text.new" : "IssuePanel.submitButton.text")); // NOI18N
        boolean showProjectCombo = isNew && !issue.isSubtask();
        if (showProjectCombo != (projectCombo.getParent() != null)) {
            GroupLayout layout = (GroupLayout)getLayout();
            layout.replace(showProjectCombo ? projectField : projectCombo, showProjectCombo ? projectCombo : projectField);
        }
        if (isNew != (statusField.getParent() != null)) {
            GroupLayout layout = (GroupLayout)getLayout();
            layout.replace(isNew ? statusCombo : statusField, isNew ? statusField : statusCombo);
        }
        if (isNew != (actionPanel.getParent() == null)) {
            GroupLayout layout = (GroupLayout)getLayout();
            layout.replace(isNew ? actionPanel : dummyActionPanel, isNew ? dummyActionPanel : actionPanel);
        }
        if (isNew != (cancelButton.getParent() == null)) {
            GroupLayout layout = (GroupLayout)getLayout();
            layout.replace(isNew ? cancelButton : dummyCancelButton, isNew ? dummyCancelButton : cancelButton);
        }

        reloadCustomFields();

        final String parentKey = issue.getParentKey();
        boolean hasParent = (parentKey != null) && (parentKey.trim().length() > 0);
        if  (hasParent) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    Issue parentIssue = issue.getRepository().getIssueCache().getIssue(parentKey);
                    if (parentIssue == null) {
                        parentIssue = issue.getRepository().getIssue(parentKey);
                    }
                    final Issue parent = parentIssue;
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            parentHeaderPanel.setVisible(true);
                            parentHeaderPanel.removeAll();
                            headerLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/subtask.png", true)); // NOI18N
                            GroupLayout layout = new GroupLayout(parentHeaderPanel);
                            JLabel parentLabel = new JLabel();
                            parentLabel.setText(parent.getSummary());
                            LinkButton parentButton = new LinkButton(new AbstractAction() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    parent.open();
                                }
                            });
                            parentButton.setText(parentKey+':');
                            layout.setHorizontalGroup(
                                layout.createSequentialGroup()
                                    .addComponent(parentButton)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(parentLabel)
                            );
                            layout.setVerticalGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(parentButton)
                                    .addComponent(parentLabel)
                            );
                            parentHeaderPanel.setLayout(layout);
                        }
                    });
                }
            });
        } else {
            parentHeaderPanel.setVisible(false);
            headerLabel.setIcon(null);
        }

        JiraConfiguration config = issue.getRepository().getConfiguration();
        if (isNew) {
            String projectId = issue.getFieldValue(NbJiraIssue.IssueField.PROJECT);
            if ((projectId != null) && !projectId.equals("")) { // NOI18N
                Project project = config.getProjectById(projectId);
                reloadField(projectField, project.getName(), NbJiraIssue.IssueField.PROJECT);
                if (!project.equals(projectCombo.getSelectedItem())) {
                    projectCombo.setSelectedItem(project);
                }
            } else {
                projectCombo.setSelectedIndex(0); // Preselect the project
            }
            reloadField(issueTypeCombo, config.getIssueTypeById(issue.getFieldValue(NbJiraIssue.IssueField.TYPE)), NbJiraIssue.IssueField.TYPE);
            reloadField(priorityCombo, config.getPriorityById(issue.getFieldValue(NbJiraIssue.IssueField.PRIORITY)), NbJiraIssue.IssueField.PRIORITY);
            statusField.setText(STATUS_OPEN);
            fixPrefSize(statusField);
            if (issue.isSubtask()) {
                headerLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.headerLabel.newSubtask")); // NOI18N
            }
        } else {
            ResourceBundle bundle = NbBundle.getBundle(IssuePanel.class);
            // Header label
            String headerFormat = bundle.getString("IssuePanel.headerLabel.format"); // NOI18N
            String headerTxt = MessageFormat.format(headerFormat, issue.getKey(), issue.getSummary());
            headerLabel.setText(headerTxt);
            Dimension dim = headerLabel.getPreferredSize();
            headerLabel.setMinimumSize(new Dimension(0, dim.height));
            headerLabel.setPreferredSize(new Dimension(0, dim.height));
            // Created field
            String createdFormat = bundle.getString("IssuePanel.createdField.format"); // NOI18N
            String reporter = config.getUser(issue.getFieldValue(NbJiraIssue.IssueField.REPORTER)).getFullName();
            String creation = JiraUtils.dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.CREATION), true);
            String createdTxt = MessageFormat.format(createdFormat, creation, reporter);
            createdField.setText(createdTxt);
            fixPrefSize(createdField);
            boolean isKenaiRepository = (issue.getRepository() instanceof KenaiRepository);
            if ((reporterStatusLabel.getIcon() == null) && isKenaiRepository) {
                String host = ((KenaiRepository) issue.getRepository()).getHost();
                JLabel label = KenaiUtil.createUserWidget(reporter, host, KenaiUtil.getChatLink(issue));
                label.setText(null);
                ((GroupLayout)getLayout()).replace(reporterStatusLabel, label);
                reporterStatusLabel = label;
            }
            String projectId = issue.getFieldValue(NbJiraIssue.IssueField.PROJECT);
            Project project = config.getProjectById(projectId);
            reloadField(projectCombo, project, NbJiraIssue.IssueField.PROJECT);
            reloadField(projectField, project.getName(), NbJiraIssue.IssueField.PROJECT);
            reloadField(issueTypeCombo, config.getIssueTypeById(issue.getFieldValue(NbJiraIssue.IssueField.TYPE)), NbJiraIssue.IssueField.TYPE);
            initStatusCombo(config.getStatusById(issue.getFieldValue(NbJiraIssue.IssueField.STATUS)));
            reloadField(summaryField, issue.getFieldValue(NbJiraIssue.IssueField.SUMMARY), NbJiraIssue.IssueField.SUMMARY);
            reloadField(priorityCombo, config.getPriorityById(issue.getFieldValue(NbJiraIssue.IssueField.PRIORITY)), NbJiraIssue.IssueField.PRIORITY);
            List<String> componentIds = issue.getFieldValues(NbJiraIssue.IssueField.COMPONENT);
            reloadField(componentList, componentsByIds(projectId, componentIds), NbJiraIssue.IssueField.COMPONENT);
            List<String> affectsVersionIds = issue.getFieldValues(NbJiraIssue.IssueField.AFFECTSVERSIONS);
            reloadField(affectsVersionList, versionsByIds(projectId, affectsVersionIds),NbJiraIssue.IssueField.AFFECTSVERSIONS);
            List<String> fixVersionIds = issue.getFieldValues(NbJiraIssue.IssueField.FIXVERSIONS);
            reloadField(fixVersionList, versionsByIds(projectId, fixVersionIds), NbJiraIssue.IssueField.FIXVERSIONS);
            Resolution resolution = config.getResolutionById(issue.getFieldValue(NbJiraIssue.IssueField.RESOLUTION));
            reloadField(resolutionField, (resolution==null) ? "" : resolution.getName(), NbJiraIssue.IssueField.RESOLUTION); // NOI18N
            fixPrefSize(resolutionField);
            reloadField(statusCombo, config.getStatusById(issue.getFieldValue(NbJiraIssue.IssueField.STATUS)), NbJiraIssue.IssueField.STATUS);
            String assignee = issue.getFieldValue(NbJiraIssue.IssueField.ASSIGNEE);
            String selectedAssignee = (assigneeField.getParent() == null) ? assigneeCombo.getSelectedItem().toString() : assigneeField.getText();
            if (isKenaiRepository && (assignee.trim().length() > 0) && (force || !selectedAssignee.equals(assignee))) {
                String host = ((KenaiRepository) issue.getRepository()).getHost();
                JLabel label = KenaiUtil.createUserWidget(assignee, host, KenaiUtil.getChatLink(issue));
                label.setText(null);
                ((GroupLayout)getLayout()).replace(assigneeStatusLabel, label);
                assigneeStatusLabel = label;
            }
            if (force) {
                assigneeStatusLabel.setVisible(assignee.trim().length() > 0);
            }
            reloadField(assigneeField, assignee, NbJiraIssue.IssueField.ASSIGNEE);
            reloadField(assigneeCombo, assignee, NbJiraIssue.IssueField.ASSIGNEE);
            reloadField(environmentArea, issue.getFieldValue(NbJiraIssue.IssueField.ENVIRONMENT), NbJiraIssue.IssueField.ENVIRONMENT);
            reloadField(updatedField, JiraUtils.dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.MODIFICATION), true), NbJiraIssue.IssueField.MODIFICATION);
            fixPrefSize(updatedField);
            reloadField(dueField, JiraUtils.dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.DUE)), NbJiraIssue.IssueField.DUE);

            // Work-log
            String originalEstimateTxt = issue.getFieldValue(NbJiraIssue.IssueField.INITIAL_ESTIMATE);
            int originalEstimate = toInt(originalEstimateTxt);
            int remainintEstimate = toInt(issue.getFieldValue(NbJiraIssue.IssueField.ESTIMATE));
            int timeSpent = toInt(issue.getFieldValue(NbJiraIssue.IssueField.ACTUAL));
            if ((originalEstimateTxt.length() == 0) && (remainintEstimate + timeSpent > 0)) {
                // originalEstimate is sometimes empty incorrectly
                originalEstimate = remainintEstimate + timeSpent;
            }
            int daysPerWeek = issue.getRepository().getConfiguration().getWorkDaysPerWeek();
            int hoursPerDay = issue.getRepository().getConfiguration().getWorkHoursPerDay();
            reloadField(originalEstimateField, JiraUtils.getWorkLogText(originalEstimate, daysPerWeek, hoursPerDay, false), NbJiraIssue.IssueField.INITIAL_ESTIMATE);
            reloadField(remainingEstimateField, JiraUtils.getWorkLogText(remainintEstimate, daysPerWeek, hoursPerDay, true), NbJiraIssue.IssueField.ESTIMATE);
            reloadField(timeSpentField, JiraUtils.getWorkLogText(timeSpent, daysPerWeek, hoursPerDay, false), NbJiraIssue.IssueField.ACTUAL);
            fixPrefSize(originalEstimateField);
            fixPrefSize(remainingEstimateField);
            fixPrefSize(timeSpentField);
            int scale = Math.max(originalEstimate, timeSpent+remainintEstimate);
            Color bgColor = UIManager.getDefaults().getColor("EditorPane.background"); // NOI18N
            setupWorkLogPanel(originalEstimatePanel, ORIGINAL_ESTIMATE_COLOR, Color.lightGray, Color.lightGray, originalEstimate, scale-originalEstimate, 0);
            setupWorkLogPanel(remainingEstimatePanel, Color.lightGray, REMAINING_ESTIMATE_COLOR, bgColor, timeSpent, remainintEstimate, scale-timeSpent-remainintEstimate);
            setupWorkLogPanel(timeSpentPanel, TIME_SPENT_COLOR, Color.lightGray, bgColor, timeSpent, remainintEstimate, scale-timeSpent-remainintEstimate);

            // Comments
            commentsPanel.setIssue(issue);
            UIUtils.keepFocusedComponentVisible(commentsPanel);
            if (force) {
                addCommentArea.setText(""); // NOI18N
            }

            // Attachments
            attachmentsPanel.setIssue(issue);
            UIUtils.keepFocusedComponentVisible(attachmentsPanel);

            // Issue-links
            boolean anyLink = (issue.getLinkedIssues().length != 0);
            issueLinksLabel.setVisible(anyLink);
            issueLinksPanel.setVisible(anyLink);
            issueLinksPanel.setIssue(issue);

            // Sub-tasks
            boolean hasSubtasks = issue.hasSubtasks();
            subtaskLabel.setVisible(hasSubtasks);
            if (subTaskScrollPane != null) {
                subTaskScrollPane.setVisible(hasSubtasks);
            }
            if (hasSubtasks) {
                if (subTaskTable == null) {
                    subTaskTable = new JTable();
                    subTaskTable.setDefaultRenderer(JiraStatus.class, new StatusRenderer());
                    subTaskTable.setDefaultRenderer(Priority.class, new PriorityRenderer());
                    subTaskTable.setDefaultRenderer(IssueType.class, new TypeRenderer());
                    subTaskTable.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 2) {
                                Point p = e.getPoint();
                                int row = subTaskTable.rowAtPoint(p);
                                TableModel model = subTaskTable.getModel();
                                final String issueKey = (String)model.getValueAt(row,0);
                                Issue.open(issue.getRepository(), issueKey);
                            }
                        }
                    });
                    subTaskScrollPane = new JScrollPane(subTaskTable);
                }
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        final SubtaskTableModel tableModel = new SubtaskTableModel(issue);
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                TableSorter sorter = new TableSorter(tableModel);
                                subTaskTable.setModel(sorter);
                                sorter.setTableHeader(subTaskTable.getTableHeader());

                                // Table height tweaks
                                int height = 0;
                                for(int row=0; row<tableModel.getRowCount(); row++) {
                                    height += subTaskTable.getRowHeight(row);
                                }
                                subTaskTable.setPreferredScrollableViewportSize(new Dimension(
                                        subTaskTable.getPreferredScrollableViewportSize().width,
                                        height
                                ));

                                if (subTaskScrollPane.getParent() == null) {
                                    ((GroupLayout)getLayout()).replace(dummySubtaskPanel, subTaskScrollPane);
                                }
                                revalidate();
                            }
                        });
                    }
                });
            }
        }
        updateTasklistButton();
        updateFieldStatuses();
        reloading = false;
    }
    private JComponent dummyCancelButton = new JLabel();
    private JComponent dummyActionPanel = new JLabel();
    private JTable subTaskTable;
    private JScrollPane subTaskScrollPane;

    private void reloadCustomFields() {
        for (NbJiraIssue.CustomField cField : getSupportedCustomFields()) {
            String type = cField.getType();
            if ("com.atlassian.jira.plugin.labels:labels".equals(type)) { // NOI18N
                JTextField field = (JTextField)customFieldComponents.get(cField.getId());
                if (field != null) {
                    field.setText(cField.getValues().get(0));
                }
            }
        }
    }

    private boolean isSupportedCustomField(NbJiraIssue.CustomField field) {
        return "com.atlassian.jira.plugin.labels:labels".equals(field.getType()); // NOI18N
    }

    private void reloadField(JComponent fieldComponent, Object fieldValue, NbJiraIssue.IssueField field) {
        if (fieldComponent instanceof JComboBox) {
            ((JComboBox)fieldComponent).setSelectedItem(fieldValue);
        } else if (fieldComponent instanceof JFormattedTextField) {
            ((JFormattedTextField)fieldComponent).setValue(fieldValue);
        } else if (fieldComponent instanceof JTextComponent) {
            ((JTextComponent)fieldComponent).setText(fieldValue.toString());
        } else if (fieldComponent instanceof JList) {
            JList list = (JList)fieldComponent;
            list.clearSelection();
            ListModel model = list.getModel();
            for (Object value : (List)fieldValue) {
                for (int i=0; i<model.getSize(); i++) {
                    if (model.getElementAt(i).equals(value)) {
                        list.getSelectionModel().addSelectionInterval(i, i);
                    }
                }
            }
        }
        initialValues.put(field, fieldValue);
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, JComboBox combo) {
        Object value = combo.getSelectedItem();
        if (value != null) {
            String key;
            switch (field) {
                case PROJECT: key = ((Project)value).getId(); break;
                case TYPE: key = ((IssueType)value).getId(); break;
                case STATUS: key = ((JiraStatus)value).getId(); break;
                case RESOLUTION: key = ((Resolution)value).getId(); break;
                case PRIORITY: key = ((Priority)value).getId(); break;
                case ASSIGNEE: key = value.toString(); break;
                default: throw new UnsupportedOperationException();
            }
            storeFieldValue(field, key);
        }
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, JList combo) {
        Object[] values = combo.getSelectedValues();
        List<String> keys = new ArrayList<String>(values.length);
        for (int i=0; i<values.length; i++) {
            switch (field) {
                case COMPONENT: keys.add(((com.atlassian.connector.eclipse.internal.jira.core.model.Component)values[i]).getId()); break;
                case AFFECTSVERSIONS: keys.add(((Version)values[i]).getId()); break;
                case FIXVERSIONS: keys.add(((Version)values[i]).getId()); break;
                default: throw new UnsupportedOperationException();
            }
        }
        storeFieldValue(field, keys);
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, JFormattedTextField formattedField) {
        Object value = formattedField.getValue();
        storeFieldValue(field, (value == null) ? "" : ((Date)value).getTime()+""); // NOI18N
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, JTextComponent textComponent) {
        storeFieldValue(field, textComponent.getText());
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, String value) {
        if (issue.getTaskData().isNew() || !value.equals(initialValues.get(field))) {
            issue.setFieldValue(field, value);
        }
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, List<String> values) {
        Object initValue = initialValues.get(field);
        boolean identical = false;
        if (initValue instanceof List) {
            List<?> initValues = (List<?>)initValue;
            identical = values.containsAll(initValues) && initValues.containsAll(values);
        }
        if (issue.getTaskData().isNew() || !identical) {
            issue.setFieldValues(field, values);
        }
    }

    private void storeCustomFieldValue(NbJiraIssue.CustomField cField) {
        String type = cField.getType();
        if ("com.atlassian.jira.plugin.labels:labels".equals(type)) { // NOI18N
            JTextField field = (JTextField)customFieldComponents.get(cField.getId());
            if (field != null) {
                List<String> values = Collections.singletonList(field.getText());
                cField.setValues(values);
                issue.setCustomField(cField);
            }
        }
    }

    private void storeStatusAndResolution() {
        Object statusValue = initialValues.get(NbJiraIssue.IssueField.STATUS);
        Object selectedValue = statusCombo.getSelectedItem();
        if (!(statusValue instanceof JiraStatus) || (!(selectedValue instanceof JiraStatus))) {
            return; // should not happen
        }
        JiraStatus initialStatus = (JiraStatus)statusValue;
        JiraStatus selectedStatus = (JiraStatus)selectedValue;
        if (initialStatus.equals(selectedStatus)) {
            return; // no change
        }
        String statusName = selectedStatus.getName();
        if (statusName.equals(STATUS_OPEN)) {
            issue.stopProgress();
        } else if (statusName.equals(STATUS_IN_PROGRESS)) {
            issue.startProgress();
        } else if (statusName.equals(STATUS_REOPENED)) {
            issue.reopen(null);
        } else if (statusName.equals(STATUS_RESOLVED)) {
            Resolution resolution = (Resolution)resolutionCombo.getSelectedItem();
            issue.resolve(resolution, null);
        } else if (statusName.equals(STATUS_CLOSED)) {
            Resolution resolution = (Resolution)resolutionCombo.getSelectedItem();
            issue.close(resolution, null);
        }
    }

    private void setupWorkLogPanel(JPanel panel, Color color1,  Color color2, Color color3, int val1, int val2, int val3) {
        panel.setLayout(new GridBagLayout());

        JLabel label1 = new JLabel();
        label1.setOpaque(true);
        label1.setBackground(color1);
        label1.setPreferredSize(new Dimension(0,10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = val1;
        panel.add(label1, c);

        JLabel label2 = new JLabel();
        label2.setOpaque(true);
        label2.setBackground(color2);
        label2.setPreferredSize(new Dimension(0,10));
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = val2;
        panel.add(label2, c);

        JLabel label3 = new JLabel();
        label3.setOpaque(true);
        label3.setBackground(color3);
        label3.setPreferredSize(new Dimension(0,10));
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = val3;
        panel.add(label3, c);
    }

    private List<com.atlassian.connector.eclipse.internal.jira.core.model.Component> componentsByIds(String projectId, List<String> componentIds) {
        JiraConfiguration config = issue.getRepository().getConfiguration();
        List<com.atlassian.connector.eclipse.internal.jira.core.model.Component> components = new ArrayList<com.atlassian.connector.eclipse.internal.jira.core.model.Component>(componentIds.size());
        for (String id : componentIds) {
            com.atlassian.connector.eclipse.internal.jira.core.model.Component component = config.getComponentById(projectId, id);
            if(component != null) {
                components.add(component);
            }
        }
        return components;
    }

    private List<Version> versionsByIds(String projectId, List<String> versionIds) {
        JiraConfiguration config = issue.getRepository().getConfiguration();
        List<Version> versions = new ArrayList<Version>(versionIds.size());
        for (String id : versionIds) {
            Version version = config.getVersionById(projectId, id);
            if(version != null) {
                versions.add(version);
            }
        }
        return versions;
    }

    private int toInt(String text) {
        if (text.trim().length() > 0) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException nfex) {
                Jira.LOG.log(Level.INFO, nfex.getMessage(), nfex);
            }
        }
        return 0;
    }

    void reloadFormInAWT(final boolean force) {
        if (EventQueue.isDispatchThread()) {
            reloadForm(force);
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    reloadForm(force);
                }
            });
        }
    }

    PropertyChangeListener cacheListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() != issue) {
                return;
            }
            if (IssueCache.EVENT_ISSUE_SEEN_CHANGED.equals(evt.getPropertyName())) {
                updateFieldStatuses();
            }
        }
    };

    private void attachIssueListener(final NbJiraIssue issue) {
        IssueCacheUtils.removeCacheListener(issue, cacheListener);
        IssueCacheUtils.addCacheListener(issue, cacheListener);
    }

    private static void fixPrefSize(JTextField textField) {
        // The preferred size of JTextField on (Classic) Windows look and feel
        // is one pixel shorter. The following code is a workaround.
        textField.setPreferredSize(null);
        Dimension dim = textField.getPreferredSize();
        Dimension fixedDim = new Dimension(dim.width+1, dim.height);
        textField.setPreferredSize(fixedDim);
    }

    private void updateFieldStatuses() {
        updateFieldStatus(NbJiraIssue.IssueField.PROJECT, projectLabel);
        updateFieldStatus(NbJiraIssue.IssueField.TYPE, issueTypeLabel);
        updateFieldStatus(NbJiraIssue.IssueField.STATUS, statusLabel);
        updateFieldStatus(NbJiraIssue.IssueField.RESOLUTION, resolutionLabel);
        updateFieldStatus(NbJiraIssue.IssueField.PRIORITY, priorityLabel);
        updateFieldStatus(NbJiraIssue.IssueField.DUE, dueLabel);
        updateFieldStatus(NbJiraIssue.IssueField.ASSIGNEE, assigneeLabel);
        updateFieldStatus(NbJiraIssue.IssueField.COMPONENT, componentLabel);
        updateFieldStatus(NbJiraIssue.IssueField.AFFECTSVERSIONS, affectsVersionLabel);
        updateFieldStatus(NbJiraIssue.IssueField.FIXVERSIONS, fixVersionLabel);
        updateFieldStatus(NbJiraIssue.IssueField.INITIAL_ESTIMATE, originalEstimateLabel);
        updateFieldStatus(NbJiraIssue.IssueField.ESTIMATE, remainingEstimateLabel);
        updateFieldStatus(NbJiraIssue.IssueField.ACTUAL, timeSpentLabel);
        updateFieldStatus(NbJiraIssue.IssueField.SUMMARY, summaryLabel);
        updateFieldStatus(NbJiraIssue.IssueField.ENVIRONMENT, environmentLabel);
    }

    private void updateFieldStatus(NbJiraIssue.IssueField field, JLabel label) {
        boolean highlight = false;
        if (!issue.getTaskData().isNew()) {
            int status = issue.getFieldStatus(field);
            highlight = (status == NbJiraIssue.FIELD_STATUS_NEW) || (status == NbJiraIssue.FIELD_STATUS_MODIFIED);
        }
        label.setOpaque(highlight);
        if (highlight) {
            label.setBackground(HIGHLIGHT_COLOR);
        }
        label.repaint();
    }

    private static final String STATUS_OPEN = "Open"; // NOI18N
    private static final String STATUS_IN_PROGRESS = "In Progress"; // NOI18N
    private static final String STATUS_REOPENED = "Reopened"; // NOI18N
    private static final String STATUS_RESOLVED = "Resolved"; // NOI18N
    private static final String STATUS_CLOSED = "Closed"; // NOI18N
    private List<JiraStatus> allowedStatusTransitions(JiraStatus status) {
        // Available operations
        boolean startProgressAvailable = false;
        boolean stopProgressAvailable = false;
        boolean resolveIssueAvailable = false;
        boolean closeIssueAvailable = false;
        boolean reopenIssueAvailable = false;
        for (TaskOperation operation : issue.getAvailableOperations().values()) {
            String label = operation.getLabel();
            if (JiraUtils.isStartProgressOperation(label)) {
                startProgressAvailable = true;
            } else if (JiraUtils.isStopProgressOperation(label)) {
                stopProgressAvailable = true;
            } else if (JiraUtils.isResolveOperation(label)) {
                resolveIssueAvailable = true;
            } else if (JiraUtils.isCloseOperation(label)) {
                closeIssueAvailable = true;
            } else if (JiraUtils.isReopenOperation(label)) {
                reopenIssueAvailable = true;
            }
        }

        String statusName = status.getName();
        List<String> allowedNames = new ArrayList<String>(3);
        allowedNames.add(statusName);
        if (stopProgressAvailable) {
            allowedNames.add(STATUS_OPEN);
        }
        if (startProgressAvailable) {
            allowedNames.add(STATUS_IN_PROGRESS);
        }
        if (reopenIssueAvailable) {
            allowedNames.add(STATUS_REOPENED);
        }
        if (resolveIssueAvailable) {
            allowedNames.add(STATUS_RESOLVED);
        }
        if (closeIssueAvailable) {
            allowedNames.add(STATUS_CLOSED);
        }

        List<JiraStatus> allowedStatuses = new ArrayList<JiraStatus>(allowedNames.size());
        for (JiraStatus s : issue.getRepository().getConfiguration().getStatuses()) {
            if (allowedNames.contains(s.getName())) {
                allowedStatuses.add(s);
            }
        }
        return allowedStatuses;
    }

    private void updateTasklistButton() {
        tasklistButton.setEnabled(false);
        RP.post(new Runnable() {
            @Override
            public void run() {
                JiraIssueProvider provider = JiraIssueProvider.getInstance();
                if (provider == null || issue.isNew()) { // do not enable button for new issues
                    return;
                }
                final boolean isInTasklist = provider.isAdded(issue);
                if (isInTasklist) {
                    attachTasklistListener(provider);
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String tasklistMessage = NbBundle.getMessage(IssuePanel.class,
                                isInTasklist ? "IssuePanel.tasklistButton.remove" : "IssuePanel.tasklistButton.add"); // NOI18N
                        tasklistButton.setText(tasklistMessage);
                        tasklistButton.setEnabled(true);
                    }
                });
            }
        });
    }

    private void attachTasklistListener (JiraIssueProvider provider) {
        if (tasklistListener == null) { // is not attached yet
            // listens on events comming from the tasklist, like when an issue is removed, etc.
            // needed to correctly update tasklistButton label and status
            tasklistListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (JiraIssueProvider.PROPERTY_ISSUE_REMOVED.equals(evt.getPropertyName()) && issue.equals(evt.getOldValue())) {
                        Runnable inAWT = new Runnable() {
                            @Override
                            public void run() {
                                updateTasklistButton();
                            }
                        };
                        if (EventQueue.isDispatchThread()) {
                            inAWT.run();
                        } else {
                            EventQueue.invokeLater(inAWT);
                        }
                    }
                }
            };
            provider.addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(tasklistListener, provider));
        }
    }

    private void submitChange(final Runnable change, String progressMessage)  {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(progressMessage);
        handle.start();
        handle.switchToIndeterminate();
        skipReload = true;
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    change.run();
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            skipReload = false;
                        }
                    });
                    handle.finish();
                    reloadFormInAWT(true);
                }
            }
        });
    }

    private void cancelHighlight(JLabel label) {
        if (!reloading) {
            label.setOpaque(false);
            label.getParent().repaint();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        resolutionField = new javax.swing.JTextField();
        projectField = new javax.swing.JTextField();
        statusField = new javax.swing.JTextField();
        assigneeCombo = new javax.swing.JComboBox();
        dummyIssueLinksPanel = new javax.swing.JPanel();
        parentHeaderPanel = new javax.swing.JPanel();
        customFieldPanelLeft = new javax.swing.JPanel();
        customFieldPanelRight = new javax.swing.JPanel();
        headerLabel = new javax.swing.JLabel();
        projectLabel = new javax.swing.JLabel();
        projectCombo = new javax.swing.JComboBox();
        issueTypeLabel = new javax.swing.JLabel();
        issueTypeCombo = new javax.swing.JComboBox();
        statusLabel = new javax.swing.JLabel();
        statusCombo = new javax.swing.JComboBox();
        resolutionLabel = new javax.swing.JLabel();
        resolutionCombo = new javax.swing.JComboBox();
        priorityLabel = new javax.swing.JLabel();
        priorityCombo = new javax.swing.JComboBox();
        createdLabel = new javax.swing.JLabel();
        createdField = new javax.swing.JTextField();
        updatedLabel = new javax.swing.JLabel();
        updatedField = new javax.swing.JTextField();
        dueLabel = new javax.swing.JLabel();
        dueField = new javax.swing.JFormattedTextField();
        assigneeLabel = new javax.swing.JLabel();
        assigneeField = new javax.swing.JTextField();
        componentLabel = new javax.swing.JLabel();
        componentScrollPane = new javax.swing.JScrollPane();
        componentList = new javax.swing.JList();
        affectsVersionLabel = new javax.swing.JLabel();
        affectsVersionScrollPane = new javax.swing.JScrollPane();
        affectsVersionList = new javax.swing.JList();
        fixVersionLabel = new javax.swing.JLabel();
        fixVersionScrollPane = new javax.swing.JScrollPane();
        fixVersionList = new javax.swing.JList();
        originalEstimateLabel = new javax.swing.JLabel();
        originalEstimateField = new javax.swing.JTextField();
        remainingEstimateLabel = new javax.swing.JLabel();
        remainingEstimateField = new javax.swing.JTextField();
        timeSpentLabel = new javax.swing.JLabel();
        timeSpentField = new javax.swing.JTextField();
        attachmentLabel = new javax.swing.JLabel();
        subtaskLabel = new javax.swing.JLabel();
        summaryLabel = new javax.swing.JLabel();
        summaryField = new javax.swing.JTextField();
        environmentLabel = new javax.swing.JLabel();
        environmentScrollPane = new javax.swing.JScrollPane();
        environmentArea = new javax.swing.JTextArea();
        addCommentLabel = new javax.swing.JLabel();
        addCommentScrollPane = new javax.swing.JScrollPane();
        addCommentArea = new javax.swing.JTextArea() {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                Dimension dim = super.getPreferredScrollableViewportSize();
                JScrollPane scrollPane = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
                int delta = 0;
                if (scrollPane != null) {
                    Component comp = scrollPane.getHorizontalScrollBar();
                    delta = comp.isVisible() ? comp.getHeight() : 0;
                }
                Insets insets = getInsets();
                int prefHeight = 5 * getRowHeight() + insets.top + insets.bottom;
                dim = new Dimension(dim.width, delta + ((dim.height < prefHeight) ? prefHeight : dim.height));
                return dim;
            }
        };
        separator = new javax.swing.JSeparator();
        submitButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        dummyCommentPanel = new javax.swing.JPanel();
        dummySubtaskPanel = new javax.swing.JPanel();
        dummyAttachmentPanel = new javax.swing.JPanel();
        actionPanel = new javax.swing.JPanel();
        actionLabel = new javax.swing.JLabel();
        startProgressButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        stopProgressButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        resolveIssueButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        closeIssueButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        createSubtaskButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        convertToSubtaskButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        logWorkButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        refreshButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        reopenIssueButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        tasklistButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        showInBrowserButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        originalEstimatePanel = new javax.swing.JPanel();
        remainingEstimatePanel = new javax.swing.JPanel();
        timeSpentPanel = new javax.swing.JPanel();
        originalEstimateFieldNew = new javax.swing.JTextField();
        originalEstimateLabelNew = new javax.swing.JLabel();
        originalEstimateHint = new javax.swing.JLabel();
        logWorkButton2 = new org.netbeans.modules.bugtracking.util.LinkButton();
        reporterStatusLabel = new javax.swing.JLabel();
        assigneeStatusLabel = new javax.swing.JLabel();
        issueLinksLabel = new javax.swing.JLabel();

        resolutionField.setEditable(false);
        resolutionField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        projectField.setEditable(false);
        projectField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        statusField.setEditable(false);
        statusField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        assigneeCombo.setEditable(true);
        assigneeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assigneeComboActionPerformed(evt);
            }
        });

        setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        projectLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.projectLabel.text")); // NOI18N

        projectCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectComboActionPerformed(evt);
            }
        });

        issueTypeLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.issueTypeLabel.text")); // NOI18N

        statusLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusLabel.text")); // NOI18N

        statusCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusComboActionPerformed(evt);
            }
        });

        resolutionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionLabel.text")); // NOI18N

        priorityLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.priorityLabel.text")); // NOI18N

        createdLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.createdLabel.text")); // NOI18N

        createdField.setEditable(false);
        createdField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        updatedLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.updatedLabel.text")); // NOI18N

        updatedField.setEditable(false);
        updatedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        dueLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dueLabel.text")); // NOI18N

        dueField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter() {
            public Object stringToValue(String text) throws java.text.ParseException {
                if (text == null || text.trim().length() == 0) {
                    return null;
                }
                return super.stringToValue(text);
            }
        }));

        assigneeLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.assigneeLabel.text")); // NOI18N

        assigneeField.setColumns(15);

        componentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.componentLabel.text")); // NOI18N

        componentList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Dummy" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        componentScrollPane.setViewportView(componentList);

        affectsVersionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.affectsVersionLabel.text")); // NOI18N

        affectsVersionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Dummy" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        affectsVersionScrollPane.setViewportView(affectsVersionList);

        fixVersionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.fixVersionLabel.text")); // NOI18N

        fixVersionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Dummy" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        fixVersionScrollPane.setViewportView(fixVersionList);

        originalEstimateLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.originalEstimateLabel.text")); // NOI18N

        originalEstimateField.setEditable(false);
        originalEstimateField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        remainingEstimateLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.remainingEstimateLabel.text")); // NOI18N

        remainingEstimateField.setEditable(false);
        remainingEstimateField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        timeSpentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.timeSpentLabel.text")); // NOI18N

        timeSpentField.setEditable(false);
        timeSpentField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        attachmentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachmentLabel.text")); // NOI18N

        subtaskLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.subtaskLabel.text")); // NOI18N

        summaryLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.summaryLabel.text")); // NOI18N

        environmentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.environmentLabel.text")); // NOI18N

        environmentArea.setLineWrap(true);
        environmentArea.setRows(5);
        environmentScrollPane.setViewportView(environmentArea);

        addCommentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.addCommentLabel.text")); // NOI18N

        addCommentScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        addCommentArea.setLineWrap(true);
        addCommentScrollPane.setViewportView(addCommentArea);

        submitButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitButton.text")); // NOI18N
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        dummySubtaskPanel.setOpaque(false);

        dummyAttachmentPanel.setOpaque(false);

        actionPanel.setBackground(new java.awt.Color(233, 236, 245));

        actionLabel.setFont(actionLabel.getFont().deriveFont(actionLabel.getFont().getStyle() | java.awt.Font.BOLD));
        actionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.actionLabel.text")); // NOI18N

        startProgressButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.startProgressButton.text")); // NOI18N
        startProgressButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startProgressButtonActionPerformed(evt);
            }
        });

        stopProgressButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.stopProgressButton.text")); // NOI18N
        stopProgressButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopProgressButtonActionPerformed(evt);
            }
        });

        resolveIssueButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolveIssueButton.text")); // NOI18N
        resolveIssueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolveIssueButtonActionPerformed(evt);
            }
        });

        closeIssueButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.closeIssueButton.text")); // NOI18N
        closeIssueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeIssueButtonActionPerformed(evt);
            }
        });

        createSubtaskButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.createSubtaskButton.text")); // NOI18N
        createSubtaskButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createSubtaskButtonActionPerformed(evt);
            }
        });

        convertToSubtaskButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.convertToSubtaskButton.text")); // NOI18N

        logWorkButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.logWorkButton.text")); // NOI18N
        logWorkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logWorkButtonActionPerformed(evt);
            }
        });

        refreshButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        reopenIssueButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reopenIssueButton.text")); // NOI18N
        reopenIssueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reopenIssueButtonActionPerformed(evt);
            }
        });

        tasklistButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.tasklistButton.add")); // NOI18N
        tasklistButton.setEnabled(false);
        tasklistButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tasklistButtonActionPerformed(evt);
            }
        });

        showInBrowserButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.showInBrowserButton.text")); // NOI18N
        showInBrowserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showInBrowserButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout actionPanelLayout = new javax.swing.GroupLayout(actionPanel);
        actionPanel.setLayout(actionPanelLayout);
        actionPanelLayout.setHorizontalGroup(
            actionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(actionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(actionLabel)
                    .addComponent(startProgressButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resolveIssueButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(createSubtaskButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(convertToSubtaskButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logWorkButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stopProgressButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(closeIssueButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reopenIssueButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tasklistButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showInBrowserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        actionPanelLayout.setVerticalGroup(
            actionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(actionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(startProgressButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stopProgressButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reopenIssueButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resolveIssueButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeIssueButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createSubtaskButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(convertToSubtaskButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logWorkButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tasklistButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showInBrowserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        originalEstimatePanel.setOpaque(false);

        remainingEstimatePanel.setOpaque(false);

        timeSpentPanel.setOpaque(false);

        originalEstimateFieldNew.setColumns(15);

        originalEstimateLabelNew.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.originalEstimateLabelNew.text")); // NOI18N

        originalEstimateHint.setFont(originalEstimateHint.getFont().deriveFont(originalEstimateHint.getFont().getSize()-2f));
        originalEstimateHint.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.originalEstimateHint.text")); // NOI18N

        logWorkButton2.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.logWorkButton2.text")); // NOI18N
        logWorkButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logWorkButtonActionPerformed(evt);
            }
        });

        issueLinksLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.issueLinksLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(separator, javax.swing.GroupLayout.DEFAULT_SIZE, 852, Short.MAX_VALUE)
            .addComponent(dummyCommentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 852, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(675, Short.MAX_VALUE)
                .addComponent(logWorkButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(remainingEstimateLabel)
                            .addComponent(timeSpentLabel)
                            .addComponent(originalEstimateLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(timeSpentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeSpentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(remainingEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(remainingEstimatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(originalEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(originalEstimatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(customFieldPanelLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(summaryLabel)
                            .addComponent(environmentLabel)
                            .addComponent(addCommentLabel)
                            .addComponent(attachmentLabel)
                            .addComponent(subtaskLabel)
                            .addComponent(originalEstimateLabelNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(issueLinksLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(environmentScrollPane)
                            .addComponent(summaryField, javax.swing.GroupLayout.DEFAULT_SIZE, 687, Short.MAX_VALUE)
                            .addComponent(addCommentScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 687, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(submitButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton))
                            .addComponent(dummyAttachmentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 687, Short.MAX_VALUE)
                            .addComponent(dummySubtaskPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 687, Short.MAX_VALUE)
                            .addComponent(originalEstimateFieldNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(originalEstimateHint)
                            .addComponent(customFieldPanelRight, javax.swing.GroupLayout.DEFAULT_SIZE, 687, Short.MAX_VALUE)
                            .addComponent(dummyIssueLinksPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 687, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(projectLabel)
                                    .addComponent(issueTypeLabel)
                                    .addComponent(statusLabel)
                                    .addComponent(resolutionLabel)
                                    .addComponent(priorityLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(projectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(issueTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(resolutionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(createdLabel)
                                            .addComponent(updatedLabel)
                                            .addComponent(dueLabel)
                                            .addComponent(assigneeLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(assigneeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(assigneeStatusLabel))
                                            .addComponent(dueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(updatedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(createdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(reporterStatusLabel))))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(componentLabel)
                                    .addComponent(componentScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(affectsVersionLabel)
                                    .addComponent(affectsVersionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(fixVersionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(fixVersionLabel)))
                            .addComponent(headerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE)
                            .addComponent(parentHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(actionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {issueTypeCombo, priorityCombo, projectCombo, resolutionCombo, statusCombo});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {affectsVersionScrollPane, componentScrollPane, fixVersionScrollPane});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, submitButton});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {originalEstimateField, remainingEstimateField, timeSpentField});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {assigneeField, dueField});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(parentHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(headerLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(projectLabel)
                            .addComponent(projectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(createdLabel)
                            .addComponent(createdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(reporterStatusLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(issueTypeLabel)
                            .addComponent(issueTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(updatedLabel)
                            .addComponent(updatedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(statusLabel)
                            .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dueLabel)
                            .addComponent(dueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(resolutionLabel)
                            .addComponent(resolutionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(assigneeLabel)
                            .addComponent(assigneeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(assigneeStatusLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(priorityLabel)
                            .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(componentLabel)
                            .addComponent(affectsVersionLabel)
                            .addComponent(fixVersionLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(componentScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(affectsVersionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fixVersionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(actionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(originalEstimateLabel)
                        .addComponent(originalEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(originalEstimatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(remainingEstimateLabel)
                        .addComponent(remainingEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(remainingEstimatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(timeSpentLabel)
                        .addComponent(timeSpentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(timeSpentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logWorkButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dummyIssueLinksPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(issueLinksLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(attachmentLabel)
                    .addComponent(dummyAttachmentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(subtaskLabel)
                    .addComponent(dummySubtaskPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(summaryLabel)
                    .addComponent(summaryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(environmentLabel)
                    .addComponent(environmentScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(customFieldPanelRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customFieldPanelLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addCommentScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addCommentLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(originalEstimateFieldNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(originalEstimateLabelNew))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(originalEstimateHint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(submitButton)
                    .addComponent(cancelButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dummyCommentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {originalEstimateField, originalEstimatePanel});

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {timeSpentField, timeSpentPanel});

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {remainingEstimateField, remainingEstimatePanel});

    }// </editor-fold>//GEN-END:initComponents

    private void projectComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectComboActionPerformed
        Object value = projectCombo.getSelectedItem();
        if (!(value instanceof Project)) return;
        final Project cachedProject = (Project)value;
        
        String msgPattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.projectMetaData"); // NOI18N
        String msg = MessageFormat.format(msgPattern, cachedProject.getName());
        final boolean wasReloading = reloading;
        final ProgressHandle handle = ProgressHandleFactory.createHandle(msg);
        handle.start();
        handle.switchToIndeterminate();
        RP.post(new Runnable() {
            @Override
            public void run() {

                final Project project;
                // The project meta-data may not be initialized.
                // Their intialization must be performed outside event-dispatch thread
                try {
                    JiraConfiguration config = issue.getRepository().getConfiguration();
                    project = config.getProjectById(cachedProject.getId()); // lets make sure we hold the rigth instance
                    config.ensureProjectLoaded(project);
                    config.ensureIssueTypes(project);
                } finally {
                    handle.finish();
                }

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        boolean oldReloading = reloading;
                        reloading = wasReloading;

                        // --- Reload dependent combos
                        JiraConfiguration config =  issue.getRepository().getConfiguration();
                        boolean subtask = issue.isSubtask();
                        boolean anySubtaskType = false;
                        IssueType[] issueTypes = config.getIssueTypes(project);
                        List<IssueType> types = new ArrayList<IssueType>(issueTypes.length);
                        for (IssueType issueType : issueTypes) {
                            if (issueType.isSubTaskType() == subtask) {
                                types.add(issueType);
                            }
                            anySubtaskType |= issueType.isSubTaskType();
                        }
                        issueTypeCombo.setModel(new DefaultComboBoxModel(types.toArray(new IssueType[types.size()])));
                        reloadField(issueTypeCombo, config.getIssueTypeById(issue.getFieldValue(NbJiraIssue.IssueField.TYPE)), NbJiraIssue.IssueField.TYPE);
                        createSubtaskButton.setVisible(!subtask && anySubtaskType);

                        // Reload components
                        DefaultListModel componentModel = new DefaultListModel();
                        for (com.atlassian.connector.eclipse.internal.jira.core.model.Component component : config.getComponents(project)) {
                            componentModel.addElement(component);
                        }
                        componentList.setModel(componentModel);
                        List<String> componentIds = issue.getFieldValues(NbJiraIssue.IssueField.COMPONENT);
                        reloadField(componentList, componentsByIds(project.getId(), componentIds), NbJiraIssue.IssueField.COMPONENT);

                        // Reload versions
                        DefaultListModel versionModel = new DefaultListModel();
                        for (Version version : config.getVersions(project)) {
                            versionModel.addElement(version);
                        }
                        affectsVersionList.setModel(versionModel);
                        fixVersionList.setModel(versionModel);
                        List<String> affectsVersionIds = issue.getFieldValues(NbJiraIssue.IssueField.AFFECTSVERSIONS);
                        reloadField(affectsVersionList, versionsByIds(project.getId(), affectsVersionIds),NbJiraIssue.IssueField.AFFECTSVERSIONS);
                        List<String> fixVersionIds = issue.getFieldValues(NbJiraIssue.IssueField.FIXVERSIONS);
                        reloadField(fixVersionList, versionsByIds(project.getId(), fixVersionIds), NbJiraIssue.IssueField.FIXVERSIONS);

                        reloading = oldReloading;

                        TaskData data = issue.getTaskData();
                        if (data.isNew() && !issue.isSubtask()) {
                            issue.setFieldValue(NbJiraIssue.IssueField.PROJECT, project.getId());
                            JiraRepositoryConnector connector = Jira.getInstance().getRepositoryConnector();
                            try {
                                connector.getTaskDataHandler().initializeTaskData(issue.getRepository().getTaskRepository(), data, connector.getTaskMapping(data), new NullProgressMonitor());
                                reloadForm(false);
                            } catch (CoreException cex) {
                                Jira.LOG.log(Level.INFO, cex.getMessage(), cex);
                            }
                        }
                    }
                });
            }
        });
    }//GEN-LAST:event_projectComboActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        String refreshMessageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshMessage"); // NOI18N
        String refreshMessage = MessageFormat.format(refreshMessageFormat, issue.getKey());
        final ProgressHandle handle = ProgressHandleFactory.createHandle(refreshMessage);
        handle.start();
        handle.switchToIndeterminate();
        RP.post(new Runnable() {
            @Override
            public void run() {
                IssueCache cache = issue.getRepository().getIssueCache();
                String parentKey = issue.getParentKey();
                if ((parentKey != null) && (parentKey.trim().length()>0)) {
                    Issue parentIssue = cache.getIssue(parentKey);
                    if (parentIssue != null) {
                        parentIssue.refresh();
                    }
                }
                for (String subTaskKey : issue.getSubtaskKeys()) {
                    Issue subTask = cache.getIssue(subTaskKey);
                    if (subTask != null) {
                        subTask.refresh();
                    }
                }
                issue.refresh();
                handle.finish();
                reloadFormInAWT(true);
            }
        });
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        reloadForm(true);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        boolean isNew = issue.getTaskData().isNew();
        storeStatusAndResolution();
        storeFieldValue(NbJiraIssue.IssueField.PROJECT, projectCombo);
        storeFieldValue(NbJiraIssue.IssueField.TYPE, issueTypeCombo);
        storeFieldValue(NbJiraIssue.IssueField.PRIORITY, priorityCombo);
        storeFieldValue(NbJiraIssue.IssueField.COMPONENT, componentList);
        storeFieldValue(NbJiraIssue.IssueField.AFFECTSVERSIONS, affectsVersionList);
        storeFieldValue(NbJiraIssue.IssueField.FIXVERSIONS, fixVersionList);
        storeFieldValue(NbJiraIssue.IssueField.SUMMARY, summaryField);
        storeFieldValue(NbJiraIssue.IssueField.ENVIRONMENT, environmentArea);
        storeFieldValue(NbJiraIssue.IssueField.DUE, dueField);
        if (assigneeField.getParent() == null) {
            storeFieldValue(NbJiraIssue.IssueField.ASSIGNEE, assigneeCombo);
        } else {
            storeFieldValue(NbJiraIssue.IssueField.ASSIGNEE, assigneeField);
        }
        for (NbJiraIssue.CustomField cField : getSupportedCustomFields()) {
            storeCustomFieldValue(cField);
        }
        String submitMessage;
        if (isNew) {
            String estimateCode = originalEstimateFieldNew.getText();
            String estimateTxt = JiraUtils.getWorkLogSeconds(
                    estimateCode,
                    issue.getRepository().getConfiguration().getWorkDaysPerWeek(),
                    issue.getRepository().getConfiguration().getWorkHoursPerDay()) + ""; // NOI18N
            storeFieldValue(NbJiraIssue.IssueField.INITIAL_ESTIMATE, estimateTxt);
            storeFieldValue(NbJiraIssue.IssueField.ESTIMATE, estimateTxt);
            storeFieldValue(NbJiraIssue.IssueField.DESCRIPTION, addCommentArea);
            submitMessage = NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitNewMessage"); // NOI18N
        } else {
            String submitMessageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitMessage"); // NOI18N
            submitMessage = MessageFormat.format(submitMessageFormat, issue.getKey());
        }
        final ProgressHandle handle = ProgressHandleFactory.createHandle(submitMessage);
        handle.start();
        handle.switchToIndeterminate();
        skipReload = true;
        if (!isNew && !"".equals(addCommentArea.getText().trim())) { // NOI18N
            issue.addComment(addCommentArea.getText());
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                boolean wasNew = issue.isNew();
                try {
                    ret = issue.submitAndRefresh();
                    for (File attachment : attachmentsPanel.getNewAttachments()) {
                        if (attachment.exists() && attachment.isFile()) {
                            issue.addAttachment(attachment, null, null);
                        } else {
                            // PENDING notify user
                        }
                    }
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            skipReload = false;
                        }
                    });
                    handle.finish();
                    if(ret) {
                        reloadFormInAWT(true);
                        if (wasNew && (issue.getParentKey() != null) && (issue.getParentKey().trim().length() > 0)) {
                            Issue parent = issue.getRepository().getIssue(issue.getParentKey());
                            parent.refresh();
                        }
                    }
                }
            }
        });
    }//GEN-LAST:event_submitButtonActionPerformed

    private void statusComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusComboActionPerformed
        Object selection = statusCombo.getSelectedItem();
        if (!(selection instanceof JiraStatus)) {
            return;
        }
        String selectedStatus = ((JiraStatus)selection).getName();
        Object ir = initialValues.get(NbJiraIssue.IssueField.RESOLUTION);
        boolean initiallyWithResolution = (ir != null) && (ir.toString().trim().length() > 0);
        boolean nowWithResolution = STATUS_CLOSED.equals(selectedStatus) || STATUS_RESOLVED.equals(selectedStatus);
        boolean showCombo = !initiallyWithResolution && nowWithResolution;
        boolean showField = initiallyWithResolution && nowWithResolution;
        GroupLayout layout = (GroupLayout)getLayout();
        if (showCombo && (resolutionCombo.getParent() == null)) {
            layout.replace(resolutionField, resolutionCombo);
        }
        if (showField && (resolutionField.getParent() == null)) {
            layout.replace(resolutionCombo, resolutionField);
        }
        resolutionCombo.setVisible(nowWithResolution);
        resolutionField.setVisible(nowWithResolution);
    }//GEN-LAST:event_statusComboActionPerformed

    private void startProgressButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startProgressButtonActionPerformed
        String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.startProgressMessage"); // NOI18N
        String message = MessageFormat.format(pattern, issue.getKey());
        submitChange(new Runnable() {
            @Override
            public void run() {
                issue.startProgress();
                issue.submitAndRefresh();
            }
        }, message);
    }//GEN-LAST:event_startProgressButtonActionPerformed

    private void stopProgressButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopProgressButtonActionPerformed
        String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.stopProgressMessage"); // NOI18N
        String message = MessageFormat.format(pattern, issue.getKey());
        submitChange(new Runnable() {
            @Override
            public void run() {
                issue.stopProgress();
                issue.submitAndRefresh();
            }
        }, message);
    }//GEN-LAST:event_stopProgressButtonActionPerformed

    private void resolveIssueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolveIssueButtonActionPerformed
        ResolveIssuePanel panel = new ResolveIssuePanel(issue);
        String title = NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolveIssueButton.text"); // NOI18N
        if (BugtrackingUtil.show(panel, title, title)) {
            String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolveIssueMessage"); // NOI18N
            String message = MessageFormat.format(pattern, issue.getKey());
            final Resolution resolution = panel.getSelectedResolution();
            final String comment = panel.getComment();
            submitChange(new Runnable() {
                @Override
                public void run() {
                    issue.resolve(resolution, comment);
                    issue.submitAndRefresh();
                }
            }, message);
        }
    }//GEN-LAST:event_resolveIssueButtonActionPerformed

    private void closeIssueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeIssueButtonActionPerformed
        ResolveIssuePanel panel = new ResolveIssuePanel(issue);
        String resolution = issue.getFieldValue(NbJiraIssue.IssueField.RESOLUTION);
        final Resolution newResolution;
        if ((resolution == null) || resolution.trim().equals("")) { // NOI18N
            String title = NbBundle.getMessage(IssuePanel.class, "IssuePanel.closeIssueButton.text"); // NOI18N
            if (BugtrackingUtil.show(panel, title, title)) {
                newResolution = panel.getSelectedResolution();
            } else {
                newResolution = null;
            }
        } else {
            newResolution = issue.getRepository().getConfiguration().getResolutionById(resolution);
        }
        if (newResolution != null) {
            String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.closeIssueMessage"); // NOI18N
            String message = MessageFormat.format(pattern, issue.getKey());
            final String comment = panel.getComment();
            submitChange(new Runnable() {
                @Override
                public void run() {
                    issue.close(newResolution, comment);
                    issue.submitAndRefresh();
                }
            }, message);
        }

    }//GEN-LAST:event_closeIssueButtonActionPerformed

    private void reopenIssueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reopenIssueButtonActionPerformed
        String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.reopenIssueMessage"); // NOI18N
        String message = MessageFormat.format(pattern, issue.getKey());
        submitChange(new Runnable() {
            @Override
            public void run() {
                issue.reopen(null);
                issue.submitAndRefresh();
            }
        }, message);
    }//GEN-LAST:event_reopenIssueButtonActionPerformed

    private void logWorkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logWorkButtonActionPerformed
        final WorkLogPanel panel = new WorkLogPanel(issue);
        if (panel.showDialog()) {
            String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.logWorkMessage"); // NOI18N
            String message = MessageFormat.format(pattern, issue.getKey());
            submitChange(new Runnable() {
                @Override
                public void run() {
                    issue.addWorkLog(panel.getStartDate(), panel.getTimeSpent(), panel.getDescription());
                    int remainingEstimate = panel.getRemainingEstimate();
                    if (remainingEstimate != -1) { // -1 means auto-adjust
                        issue.setFieldValue(NbJiraIssue.IssueField.ESTIMATE, (remainingEstimate+panel.getTimeSpent())+""); // NOI18N
                    }
                    issue.submitAndRefresh();
                }
            }, message);
        }
    }//GEN-LAST:event_logWorkButtonActionPerformed

    private void tasklistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tasklistButtonActionPerformed
        tasklistButton.setEnabled(false);
        JiraIssueProvider provider = JiraIssueProvider.getInstance();
        if (provider.isAdded(issue)) {
            provider.remove(issue);
        } else {
            attachTasklistListener(provider);
            provider.add(issue, true);
        }
        updateTasklistButton();
    }//GEN-LAST:event_tasklistButtonActionPerformed

    private void createSubtaskButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createSubtaskButtonActionPerformed
        NbJiraIssue subTask = (NbJiraIssue)issue.getRepository().createIssue();
        TaskAttribute rta = subTask.getTaskData().getRoot();
        TaskAttribute ta = rta.getMappedAttribute(JiraAttribute.TYPE.id());
        if (ta == null) {
            ta = rta.createMappedAttribute(JiraAttribute.TYPE.id());
        }
        ta.getMetaData().putValue(IJiraConstants.META_SUB_TASK_TYPE, Boolean.toString(true));
        subTask.setFieldValue(NbJiraIssue.IssueField.PROJECT, issue.getFieldValue(NbJiraIssue.IssueField.PROJECT));
        subTask.setFieldValue(NbJiraIssue.IssueField.PARENT_KEY, issue.getKey());
        subTask.setFieldValue(NbJiraIssue.IssueField.PARENT_ID, issue.getTaskData().getTaskId());
        JiraRepositoryConnector connector = Jira.getInstance().getRepositoryConnector();
        try {
            connector.getTaskDataHandler().initializeSubTaskData(issue.getTaskRepository(), subTask.getTaskData(), issue.getTaskData(), new NullProgressMonitor());
        } catch (CoreException cex) {
            Jira.LOG.log(Level.INFO, cex.getMessage(), cex);
        }
        subTask.open();
    }//GEN-LAST:event_createSubtaskButtonActionPerformed

    private void assigneeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assigneeComboActionPerformed
        if (!reloading) {
            assigneeStatusLabel.setVisible(false);
        }
        Object value = assigneeCombo.getSelectedItem();
        if (value instanceof RepositoryUser) {
            String assignee = ((RepositoryUser)value).getUserName();
            assigneeCombo.setSelectedItem(assignee);
        }
    }//GEN-LAST:event_assigneeComboActionPerformed

    private void showInBrowserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showInBrowserButtonActionPerformed
        try {
            URL url = new URL(issue.getRepository().getUrl() + "/browse/" + issue.getKey()); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException muex) {
            Jira.LOG.log(Level.INFO, "Unable to show the issue in the browser.", muex); // NOI18N
        }
    }//GEN-LAST:event_showInBrowserButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel actionLabel;
    private javax.swing.JPanel actionPanel;
    private javax.swing.JTextArea addCommentArea;
    private javax.swing.JLabel addCommentLabel;
    private javax.swing.JScrollPane addCommentScrollPane;
    private javax.swing.JLabel affectsVersionLabel;
    private javax.swing.JList affectsVersionList;
    private javax.swing.JScrollPane affectsVersionScrollPane;
    private javax.swing.JComboBox assigneeCombo;
    private javax.swing.JTextField assigneeField;
    private javax.swing.JLabel assigneeLabel;
    private javax.swing.JLabel assigneeStatusLabel;
    private javax.swing.JLabel attachmentLabel;
    private javax.swing.JButton cancelButton;
    private org.netbeans.modules.bugtracking.util.LinkButton closeIssueButton;
    private javax.swing.JLabel componentLabel;
    private javax.swing.JList componentList;
    private javax.swing.JScrollPane componentScrollPane;
    private org.netbeans.modules.bugtracking.util.LinkButton convertToSubtaskButton;
    private org.netbeans.modules.bugtracking.util.LinkButton createSubtaskButton;
    private javax.swing.JTextField createdField;
    private javax.swing.JLabel createdLabel;
    private javax.swing.JPanel customFieldPanelLeft;
    private javax.swing.JPanel customFieldPanelRight;
    private javax.swing.JFormattedTextField dueField;
    private javax.swing.JLabel dueLabel;
    private javax.swing.JPanel dummyAttachmentPanel;
    private javax.swing.JPanel dummyCommentPanel;
    private javax.swing.JPanel dummyIssueLinksPanel;
    private javax.swing.JPanel dummySubtaskPanel;
    private javax.swing.JTextArea environmentArea;
    private javax.swing.JLabel environmentLabel;
    private javax.swing.JScrollPane environmentScrollPane;
    private javax.swing.JLabel fixVersionLabel;
    private javax.swing.JList fixVersionList;
    private javax.swing.JScrollPane fixVersionScrollPane;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JLabel issueLinksLabel;
    private javax.swing.JComboBox issueTypeCombo;
    private javax.swing.JLabel issueTypeLabel;
    private org.netbeans.modules.bugtracking.util.LinkButton logWorkButton;
    private org.netbeans.modules.bugtracking.util.LinkButton logWorkButton2;
    private javax.swing.JTextField originalEstimateField;
    private javax.swing.JTextField originalEstimateFieldNew;
    private javax.swing.JLabel originalEstimateHint;
    private javax.swing.JLabel originalEstimateLabel;
    private javax.swing.JLabel originalEstimateLabelNew;
    private javax.swing.JPanel originalEstimatePanel;
    private javax.swing.JPanel parentHeaderPanel;
    private javax.swing.JComboBox priorityCombo;
    private javax.swing.JLabel priorityLabel;
    private javax.swing.JComboBox projectCombo;
    private javax.swing.JTextField projectField;
    private javax.swing.JLabel projectLabel;
    private org.netbeans.modules.bugtracking.util.LinkButton refreshButton;
    private javax.swing.JTextField remainingEstimateField;
    private javax.swing.JLabel remainingEstimateLabel;
    private javax.swing.JPanel remainingEstimatePanel;
    private org.netbeans.modules.bugtracking.util.LinkButton reopenIssueButton;
    private javax.swing.JLabel reporterStatusLabel;
    private javax.swing.JComboBox resolutionCombo;
    private javax.swing.JTextField resolutionField;
    private javax.swing.JLabel resolutionLabel;
    private org.netbeans.modules.bugtracking.util.LinkButton resolveIssueButton;
    private javax.swing.JSeparator separator;
    private org.netbeans.modules.bugtracking.util.LinkButton showInBrowserButton;
    private org.netbeans.modules.bugtracking.util.LinkButton startProgressButton;
    private javax.swing.JComboBox statusCombo;
    private javax.swing.JTextField statusField;
    private javax.swing.JLabel statusLabel;
    private org.netbeans.modules.bugtracking.util.LinkButton stopProgressButton;
    private javax.swing.JButton submitButton;
    private javax.swing.JLabel subtaskLabel;
    private javax.swing.JTextField summaryField;
    private javax.swing.JLabel summaryLabel;
    private org.netbeans.modules.bugtracking.util.LinkButton tasklistButton;
    private javax.swing.JTextField timeSpentField;
    private javax.swing.JLabel timeSpentLabel;
    private javax.swing.JPanel timeSpentPanel;
    private javax.swing.JTextField updatedField;
    private javax.swing.JLabel updatedLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize(); // Issue 176085
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
             // Issue 176085
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

    void makeCaretVisible(JTextArea textArea) {
        int pos = textArea.getCaretPosition();
        try {
            Rectangle rec = textArea.getUI().modelToView(textArea, pos);
            if (rec != null) {
                Point p = SwingUtilities.convertPoint(textArea, rec.x, rec.y, this);
                scrollRectToVisible(new Rectangle(p.x, p.y, rec.width, rec.height));
            }
        } catch (BadLocationException blex) {
            Jira.LOG.log(Level.INFO, blex.getMessage(), blex);
        }
    }

    void opened() {
        undoRedoSupport = UndoRedoSupport.getSupport(issue); 
        undoRedoSupport.register(addCommentArea); 
        undoRedoSupport.register(environmentArea); 
        
        // Hack - reset any previous modifications when the issue window is reopened
        reloadForm(true);
    }
    
    void closed() {
        if(issue != null) {
            commentsPanel.storeSettings();
            if (undoRedoSupport != null) {
                undoRedoSupport.unregisterAll(issue);
                undoRedoSupport = null;
            }
        }
    }

    class CancelHighlightListener implements DocumentListener, ActionListener, ListSelectionListener {
        private JLabel label;

        CancelHighlightListener(JLabel label) {
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

        @Override
        public void actionPerformed(ActionEvent e) {
            cancelHighlight(label);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            cancelHighlight(label);
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

}
