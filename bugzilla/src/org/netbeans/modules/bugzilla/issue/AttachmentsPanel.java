/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.bugzilla.issue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.bugtracking.util.UIUtils;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue.Attachment;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Stola
 */
public class AttachmentsPanel extends JPanel {
    private static final Color BG_COLOR = new Color(220, 220, 220);
    private boolean hadNoAttachments = true;
    private List<AttachmentPanel> newAttachments;
    private JLabel noneLabel;
    private LinkButton createNewButton;
    private JLabel dummyLabel = new JLabel();
    private Method maxMethod;

    public AttachmentsPanel() {
        setBackground(UIManager.getColor("EditorPane.background")); // NOI18N
        ResourceBundle bundle = NbBundle.getBundle(AttachmentsPanel.class);
        noneLabel = new JLabel(bundle.getString("AttachmentsPanel.noneLabel.text")); // NOI18N
        createNewButton = new LinkButton(new CreateNewAction());
        createNewButton.getAccessibleContext().setAccessibleDescription(bundle.getString("AttachmentPanels.createNewButton.AccessibleContext.accessibleDescription")); // NOI18N
        try {
            maxMethod = GroupLayout.Group.class.getDeclaredMethod("calculateMaximumSize", int.class); // NOI18N
            maxMethod.setAccessible(true);
        } catch (NoSuchMethodException nsmex) {
            nsmex.printStackTrace();
        }
    }

    void setAttachments(List<Attachment> attachments) {
        hadNoAttachments = attachments.isEmpty();
        newAttachments = new LinkedList<AttachmentPanel>();
        removeAll();

        GroupLayout layout = new GroupLayout(this);
        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup(GroupLayout.LEADING);
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        ResourceBundle bundle = NbBundle.getBundle(AttachmentsPanel.class);
        GroupLayout.SequentialGroup newVerticalGroup = layout.createSequentialGroup();

        boolean noAttachments = hadNoAttachments;
        horizontalGroup.add(layout.createSequentialGroup()
            .add(noneLabel)
            .addPreferredGap(LayoutStyle.RELATED)
            .add(noAttachments ? createNewButton : dummyLabel));
        verticalGroup.add(layout.createParallelGroup(GroupLayout.BASELINE)
            .add(noneLabel)
            .add(noAttachments ? createNewButton : dummyLabel));
        dummyLabel.setVisible(false);
        noneLabel.setVisible(noAttachments);
        updateCreateNewButton(noAttachments);
        if (noAttachments) {
            // noneLabel + createNewButton
            verticalGroup.add(newVerticalGroup);
        } else {
            List<JPanel> panels = new ArrayList<JPanel>();
            JLabel descriptionLabel = new JLabel(bundle.getString("AttachmentsPanel.table.description")); // NOI18N
            JLabel filenameLabel = new JLabel(bundle.getString("AttachmentsPanel.table.filename")); // NOI18N
            JLabel dateLabel =  new JLabel(bundle.getString("AttachmentsPanel.table.date")); // NOI18N
            JLabel authorLabel = new JLabel(bundle.getString("AttachmentsPanel.table.author")); // NOI18N
            makeBold(descriptionLabel);
            makeBold(filenameLabel);
            makeBold(dateLabel);
            makeBold(authorLabel);
            GroupLayout.ParallelGroup descriptionGroup = layout.createParallelGroup();
            GroupLayout.ParallelGroup filenameGroup = layout.createParallelGroup();
            GroupLayout.ParallelGroup dateGroup = layout.createParallelGroup();
            GroupLayout.ParallelGroup authorGroup = layout.createParallelGroup();
            int descriptionWidth = Math.max(descriptionLabel.getPreferredSize().width, 150);
            descriptionGroup.add(descriptionLabel, descriptionWidth, descriptionWidth, descriptionWidth);
            filenameGroup.add(filenameLabel);
            dateGroup.add(dateLabel);
            authorGroup.add(authorLabel);
            JPanel panel = createHighlightPanel();
            panels.add(panel);
            horizontalGroup.add(layout.createSequentialGroup()
                    .add(descriptionGroup)
                    .addPreferredGap(descriptionLabel, filenameLabel, LayoutStyle.UNRELATED)
                    .add(filenameGroup)
                    .addPreferredGap(filenameLabel, dateLabel, LayoutStyle.UNRELATED)
                    .add(dateGroup)
                    .addPreferredGap(dateLabel, authorLabel, LayoutStyle.UNRELATED)
                    .add(authorGroup));
            verticalGroup.add(layout.createParallelGroup(GroupLayout.LEADING, false)
                .add(panel, 0, 0, Short.MAX_VALUE)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(descriptionLabel)
                    .add(filenameLabel)
                    .add(dateLabel)
                    .add(authorLabel)));
            for (BugzillaIssue.Attachment attachment : attachments) {
                boolean isPatch = "1".equals(attachment.getIsPatch()); // NOI18N
                String description = attachment.getDesc();
                String filename = attachment.getFilename();
                Date date = attachment.getDate();
                String author = attachment.getAuthor();
                descriptionLabel = new JLabel(description);
                LinkButton filenameButton = new LinkButton();
                LinkButton patchButton = null;
                JLabel lBrace = null;
                JLabel rBrace = null;
                GroupLayout.SequentialGroup hPatchGroup = null;
                if (isPatch) {
                    patchButton = new LinkButton();
                    lBrace = new JLabel("("); // NOI18N
                    rBrace = new JLabel(")"); // NOI18N
                    hPatchGroup = layout.createSequentialGroup()
                            .add(filenameButton)
                            .addPreferredGap(filenameButton, lBrace, LayoutStyle.RELATED)
                            .add(lBrace)
                            .add(patchButton)
                            .add(rBrace);
                }
                JPopupMenu menu = menuFor(attachment, patchButton);
                filenameButton.setAction(attachment.new DefaultAttachmentAction());
                filenameButton.setText(filename);
                filenameButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AttachmentsPanel.class, "AttachmentPanels.filenameButton.AccessibleContext.accessibleDescription")); // NOI18N
                dateLabel = new JLabel(date != null ? DateFormat.getDateInstance().format(date) : ""); // NOI18N
                authorLabel = new JLabel(author);
                descriptionLabel.setComponentPopupMenu(menu);
                filenameButton.setComponentPopupMenu(menu);
                dateLabel.setComponentPopupMenu(menu);
                authorLabel.setComponentPopupMenu(menu);
                descriptionGroup.add(descriptionLabel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                if (isPatch) {
                    lBrace.setComponentPopupMenu(menu);
                    patchButton.setComponentPopupMenu(menu);
                    rBrace.setComponentPopupMenu(menu);
                    filenameGroup.add(hPatchGroup);
                } else {
                    filenameGroup.add(filenameButton);
                }
                dateGroup.add(dateLabel);
                authorGroup.add(authorLabel);
                panel = createHighlightPanel();
                panel.addMouseListener(new MouseAdapter() {}); // Workaround for bug 6272233
                panel.setComponentPopupMenu(menu);
                panels.add(panel);
                GroupLayout.ParallelGroup pGroup = layout.createParallelGroup(GroupLayout.BASELINE);
                pGroup.add(descriptionLabel);
                pGroup.add(filenameButton);
                if (isPatch) {
                    pGroup.add(lBrace);
                    pGroup.add(patchButton);
                    pGroup.add(rBrace);
                }
                pGroup.add(dateLabel);
                pGroup.add(authorLabel);
                verticalGroup
                    .addPreferredGap(LayoutStyle.RELATED)
                    .add(layout.createParallelGroup(GroupLayout.LEADING, false)
                        .add(panel, 0, 0, Short.MAX_VALUE)
                        .add(pGroup));
            }
            verticalGroup.add(newVerticalGroup);
            int groupWidth = 0;
            if (maxMethod != null) {
                try {
                    groupWidth = (Integer)maxMethod.invoke(horizontalGroup, 1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            for (JPanel p : panels) {
                horizontalGroup.add(p, 0, 0, groupWidth);
            }
        }
        horizontalGroup.add(layout.createSequentialGroup()
                .add(noAttachments ? dummyLabel : createNewButton)
                .add(0, 0, Short.MAX_VALUE));
        verticalGroup.addPreferredGap(LayoutStyle.RELATED);
        verticalGroup.add(noAttachments ? dummyLabel : createNewButton);

        layout.setHorizontalGroup(horizontalGroup);
        layout.setVerticalGroup(verticalGroup);
        ((CreateNewAction)createNewButton.getAction()).setLayoutGroups(horizontalGroup, newVerticalGroup);
        setLayout(layout);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, super.getMinimumSize().height);
    }

    private JPopupMenu menuFor(Attachment attachment, LinkButton patchButton) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(attachment.new DefaultAttachmentAction());
        menu.add(attachment.new SaveAttachmentAction());
        if ("1".equals(attachment.getIsPatch())) { // NOI18N
            AbstractAction action = attachment.new ApplyPatchAction();
            menu.add(action);
            patchButton.setAction(action);
            // Lower the first letter
            String label = patchButton.getText();
            patchButton.setText(label.substring(0,1).toLowerCase()+label.substring(1));
            patchButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AttachmentsPanel.class, "AttachmentPanels.patchButton.AccessibleContext.accessibleDescription")); // NOI18N
        }
        return menu;
    }

    private void updateCreateNewButton(boolean noAttachments) {
        String createNewButtonText = NbBundle.getMessage(AttachmentsPanel.class, "AttachmentsPanel.createNewButton.text"); // NOI18N
        createNewButton.setText(noAttachments ? ('('+createNewButtonText+')') : createNewButtonText);
    }

    private void makeBold(JLabel label) {
        Font font = label.getFont().deriveFont(Font.BOLD);
        label.setFont(font);
    }

    private JPanel createHighlightPanel() {
        JPanel panel = new JPanel();
        // PENDING what color (e.g. what key from UIDefaults) should I use?
        panel.setBackground(BG_COLOR);
        add(panel);
        return panel;
    }

    private PropertyChangeListener deletedListener;
    PropertyChangeListener getDeletedListener() {
        if (deletedListener == null) {
            deletedListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (AttachmentPanel.PROP_DELETED.equals(evt.getPropertyName())) {
                        for (AttachmentPanel panel : newAttachments) {
                            if (!panel.isDeleted()) {
                                return;
                            }
                        }
                        // The last attachment deleted
                        noneLabel.setVisible(true);
                        switchHelper();
                        updateCreateNewButton(true);
                    }
                }
            };
        }
        return deletedListener;
    }

    private void switchHelper() {
        JLabel temp = new JLabel();
        GroupLayout layout = (GroupLayout)getLayout();
        layout.replace(dummyLabel, temp);
        layout.replace(createNewButton, dummyLabel);
        layout.replace(temp, createNewButton);
    }

    List<AttachmentInfo> getNewAttachments() {
        List<AttachmentInfo> infos = new LinkedList<AttachmentInfo>();
        for (AttachmentPanel attachment : newAttachments) {
            if (!attachment.isDeleted()) {
                AttachmentInfo info = new AttachmentInfo();
                info.file = attachment.getFile();
                info.description = attachment.getDescription();
                info.contentType = attachment.getContentType();
                info.isPatch = attachment.isPatch();
                infos.add(info);
            }
        }
        return infos;
    }

    class AttachmentInfo {
        File file;
        String description;
        String contentType;
        boolean isPatch;
    }

    class CreateNewAction extends AbstractAction {
        private GroupLayout.ParallelGroup horizontalGroup;
        private GroupLayout.SequentialGroup verticalGroup;

        void setLayoutGroups(GroupLayout.ParallelGroup horizontalGroup,
                GroupLayout.SequentialGroup verticalGroup) {
            this.horizontalGroup = horizontalGroup;
            this.verticalGroup = verticalGroup;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AttachmentPanel attachment = new AttachmentPanel();
            attachment.setBackground(BG_COLOR);
            horizontalGroup.add(attachment, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
            verticalGroup.addPreferredGap(LayoutStyle.RELATED);
            verticalGroup.add(attachment, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
            if (noneLabel.isVisible()) {
                noneLabel.setVisible(false);
                switchHelper();
                updateCreateNewButton(false);
            }
            if (hadNoAttachments) {
                attachment.addPropertyChangeListener(getDeletedListener());
            }
            newAttachments.add(attachment);
            UIUtils.keepFocusedComponentVisible(attachment);
            revalidate();
        }

    }

}
