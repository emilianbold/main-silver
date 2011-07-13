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

package org.netbeans.modules.bugzilla.issue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueSettingsStorage;
import org.netbeans.modules.bugtracking.util.HyperlinkSupport;
import org.netbeans.modules.bugtracking.util.HyperlinkSupport.Link;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.bugtracking.util.TextUtils;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.kenai.KenaiRepository;
import org.netbeans.modules.bugzilla.repository.IssueField;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Stola
 */
public class CommentsPanel extends JPanel {
    static final RequestProcessor RP = new RequestProcessor("Bugzilla Comments Panel", 5, false); // NOI18N
    private static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // NOI18N
    private final static String REPLY_TO_PROPERTY = "replyTo"; // NOI18N
    private final static String QUOTE_PREFIX = "> "; // NOI18N
    private final static int MAX_COMMENT_HEIGHT = 10000;
    
    private final static Color BLUE_BACKGROUND = new Color(0xf3f6fd);
    private final static Color GREY_FOREGROUND = new Color(0x999999);
    
    private final JPopupMenu commentsPopup = new PopupMenu();
    private final BugzillaIssueFinder issueFinder;
    private BugzillaIssue issue;
    private List<BugzillaIssue.Attachment> attachments;
    private List<String> attachmentIds;
    private NewCommentHandler newCommentHandler;

    private Set<Long> collapsedComments = Collections.synchronizedSet(new HashSet<Long>());
    private final Link issueLink;
    
    public CommentsPanel() {
        setBackground(UIManager.getColor("EditorPane.background")); // NOI18N
        
        issueLink = new HyperlinkSupport.Link() {
            @Override
            public void onClick(String linkText) {
                final String issueKey = issueFinder.getIssueId(linkText);
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        Issue is = issue.getRepository().getIssue(issueKey);
                        if (is != null) {
                            is.open();
                        }
                    }
                });
            }
        };
        issueFinder = Lookup.getDefault().lookup(BugzillaIssueFinder.class);
        assert issueFinder != null;
    }

    void setIssue(BugzillaIssue issue,
                  List<BugzillaIssue.Attachment> attachments) {
        removeAll();
        this.issue = issue;
        initCollapsedComments();
        this.attachments = attachments;
        this.attachmentIds = getAttachmentIds(attachments);
        GroupLayout layout = new GroupLayout(this);
        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(horizontalGroup)
            .addContainerGap());
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addContainerGap();
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(verticalGroup));
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);
        String creationTxt = issue.getFieldValue(IssueField.CREATION);
        try {
            Date creation = dateTimeFormat.parse(creationTxt);
            creationTxt = format.format(creation);
        } catch (ParseException pex) {
            Bugzilla.LOG.log(Level.INFO, null, pex);
        }
        addSection(layout,
            new Long(0),    
            issue.getFieldValue(IssueField.DESCRIPTION),
            issue.getFieldValue(IssueField.REPORTER),
            issue.getFieldValue(IssueField.REPORTER_NAME),
            creationTxt, horizontalGroup, verticalGroup, true);
        for (BugzillaIssue.Comment comment : issue.getComments()) {
            String when = format.format(comment.getWhen());
            addSection(layout, comment.getNumber(), comment.getText(), comment.getAuthor(), comment.getAuthorName(), when, horizontalGroup, verticalGroup, false);
        }
        verticalGroup.addContainerGap();
        setLayout(layout);
    }

    private static List<String> getAttachmentIds(
                                   List<BugzillaIssue.Attachment> attachments) {
        if (attachments.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>(attachments.size());
        for (BugzillaIssue.Attachment attachment : attachments) {
            result.add(attachment.getId());
        }
        return result;
    }

    public void setNewCommentHandler(NewCommentHandler handler) {
        newCommentHandler = handler;
    }

    private void addSection(GroupLayout layout, final Long number, String text, final String author, String authorName, String dateTimeString,
            GroupLayout.ParallelGroup horizontalGroup, GroupLayout.SequentialGroup verticalGroup, boolean description) {
        
        JTextPane textPane = new JTextPane();
        setupTextPane(textPane, text);
        
        JPanel headerPanel = new JPanel();
        JPanel placeholder = createTextPanelPlaceholder();      
        JLabel commentLabel = new JLabel();
        JLabel rightLabel = new JLabel();
        JLabel iconLabel = new ExpandLabel(placeholder, textPane, headerPanel, commentLabel, number);
        JLabel leftLabel = new JLabel();
        
        // left label
        ResourceBundle bundle = NbBundle.getBundle(CommentsPanel.class);
        String leftTxt = "";
        String authorTxt = ((authorName != null) && (authorName.trim().length() > 0)) ? authorName : author;
        if (description) {
            String leftFormat = bundle.getString("CommentsPanel.leftLabel.format"); // NOI18N
            leftTxt = MessageFormat.format(leftFormat, authorTxt);
        } else {
            leftTxt = authorTxt;
        } 
        leftLabel.setText(leftTxt);
        leftLabel.setLabelFor(textPane);
        leftLabel.setForeground(GREY_FOREGROUND);
        leftLabel.setOpaque(false);
        
        // comment label
        commentLabel.setOpaque(false);
        
        // right label
        rightLabel.setText(dateTimeString);
        rightLabel.setForeground(GREY_FOREGROUND);
        rightLabel.setOpaque(false);
        
        // state label
        JLabel stateLabel = null;
        if (issue.getRepository() instanceof KenaiRepository) {
            int index = author.indexOf('@');
            String userName = (index == -1) ? author : author.substring(0,index);
            String host = ((KenaiRepository) issue.getRepository()).getHost();
            stateLabel = KenaiUtil.createUserWidget(userName, host, KenaiUtil.getChatLink(issue));
            stateLabel.setText(null);
        }
        
        // replay button
        LinkButton replyButton = new LinkButton(bundle.getString("Comments.replyButton.text")); // NOI18N
        replyButton.addActionListener(getReplyListener());
        replyButton.putClientProperty(REPLY_TO_PROPERTY, textPane);
        replyButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.replyButton.AccessibleContext.accessibleDescription")); // NOI18N
        replyButton.setOpaque(false);

        // Issue 172653 - JTextPane too big
        JComponent pane = textPane;
        if (textPane.getPreferredSize().height>Short.MAX_VALUE) {
            pane = new JScrollPane(textPane);
            Dimension dim = new Dimension(textPane.getPreferredSize());
            dim.height = MAX_COMMENT_HEIGHT;
            pane.setPreferredSize(dim); 
        }

        // Layout
        layoutHeaderPanel(headerPanel, iconLabel, leftLabel, commentLabel, rightLabel, replyButton, stateLabel);
        
        iconLabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        placeholder.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        leftLabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        commentLabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        rightLabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        
        horizontalGroup
            .addComponent(headerPanel)
            .addGroup(layout.createSequentialGroup()
                .addComponent(placeholder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pane));                
        
        if (!description) {
            verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        }
        verticalGroup
            .addComponent(headerPanel)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                .addComponent(placeholder)
                .addComponent(pane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        }

    private void setupTextPane(final JTextPane textPane, String comment) {
        textPane.setText(comment);
        HyperlinkSupport.getInstance().registerForStacktraces(textPane);
        HyperlinkSupport.getInstance().registerForURLs(textPane);
        HyperlinkSupport.getInstance().registerForIssueLinks(textPane, issueLink, issueFinder);
        
        Caret caret = textPane.getCaret();
        if (caret instanceof DefaultCaret) {
            ((DefaultCaret)caret).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }

        // attachments
        if (!attachmentIds.isEmpty()) {
            final int[] boundaries = AttachmentHyperlinkSupport
                                     .findBoundaries(comment, attachmentIds);
            if ((boundaries != null) && (boundaries.length != 0)) {
                HyperlinkSupport.Link attachmentLink = new HyperlinkSupport.Link() {
                    @Override
                    public void onClick(String linkText) {
                        CommentsPanel.this.openAttachmentHyperlink(textPane);
                    }
                };
                HyperlinkSupport.getInstance().registerLink(textPane, new int[] {boundaries[0], boundaries[1]}, attachmentLink);
            }
        }

        // pop-ups
        textPane.setComponentPopupMenu(commentsPopup);

        textPane.setBackground(BLUE_BACKGROUND);
        textPane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        textPane.setEditable(false);
        textPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.textPane.AccessibleContext.accessibleName")); // NOI18N
        textPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.textPane.AccessibleContext.accessibleDescription")); // NOI18N
    }

    private void layoutHeaderPanel(JPanel headerPanel, JLabel iconLabel, JLabel leftLabel, JLabel commentLabel, JLabel rightLabel, LinkButton replyButton, JLabel stateLabel) {
        GroupLayout layout = new GroupLayout(headerPanel);
        headerPanel.setLayout(layout);
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup()
            .addComponent(iconLabel)
            .addComponent(leftLabel);
        if (stateLabel != null) {
            hGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(stateLabel);
        }
        hGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(commentLabel,0, 0, Short.MAX_VALUE)
              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(rightLabel)
              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(replyButton);
        layout.setHorizontalGroup(hGroup);
        
        GroupLayout.ParallelGroup vGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(iconLabel)
            .addComponent(leftLabel);
        if (stateLabel != null) {
            vGroup.addComponent(stateLabel);
        }
        vGroup.addComponent(commentLabel)
              .addComponent(rightLabel)
              .addComponent(replyButton);
        layout.setVerticalGroup(vGroup);
    }

    private ActionListener replyListener;
    private ActionListener getReplyListener() {
        if (replyListener == null) {
            replyListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Object source = e.getSource();
                    if (source instanceof JComponent) {
                        JComponent comp = (JComponent)source;
                        Object value = comp.getClientProperty(REPLY_TO_PROPERTY);
                        if (value instanceof JTextPane) {
                            JTextPane pane = (JTextPane)value;
                            String text = pane.getText();
                            StringBuilder sb = new StringBuilder();
                            StringTokenizer tokenizer = new StringTokenizer(text, "\n"); // NOI18N
                            while (tokenizer.hasMoreElements()) {
                                String line = tokenizer.nextToken();
                                sb.append(QUOTE_PREFIX).append(line).append('\n');
                            }
                            newCommentHandler.append(sb.toString());
                        }
                    }
                }
            };
        }
        return replyListener;
    }

    private void openAttachmentHyperlink(JTextPane textPane) {
        String attachmentId = null;
        try {
            BugzillaIssue.Attachment attachment = getAttachment(textPane);
            if (attachment != null) {
                attachment.open();
            }
        } catch (Exception ex) {
            assert false;
            String errMsg = "Could not open attachment";                //NOI18N
            if (attachmentId != null) {
                errMsg += " #" + attachmentId;                          //NOI18N
            }
            ErrorManager.getDefault().log(ErrorManager.WARNING, errMsg);
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    private BugzillaIssue.Attachment getAttachment(JTextPane textPane) {
        String commentText = textPane.getText();
        String attachmentId = AttachmentHyperlinkSupport
                              .getAttachmentId(commentText);
        if (attachmentId != null) {
            int index = attachmentIds.indexOf(attachmentId);
            if (index != -1) {
                return attachments.get(index);
            }
        }
        return null;
    }

    private JPanel createTextPanelPlaceholder() {
        JPanel placeholder = new JPanel();
        placeholder.setBackground(BLUE_BACKGROUND);
        GroupLayout layout = new GroupLayout(placeholder);
        placeholder.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, ICON_WIDTH, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE));
        return placeholder;
    }

    class PopupMenu extends JPopupMenu {

        /*
         * Holds the location of where the user invoked the pop-up menu.
         * It must be remembered before calling super.show(...) because
         * the method show() may change the location of the pop-up menu,
         * so the original location might not be available.
         */
        private final Point clickPoint = new Point();

        @Override
        public void show(Component invoker, int x, int y) {
            clickPoint.setLocation(x, y);
            super.show(invoker, x, y);
        }

        @Override
        public void setVisible(boolean b) {
            if (b) {
                JTextPane pane = (JTextPane) getInvoker();
                BugzillaIssue.Attachment attachment = getAttachment(pane);
                if (attachment != null) {
                    add(new JMenuItem(attachment.new DefaultAttachmentAction()));
                    add(new JMenuItem(attachment.new SaveAttachmentAction()));
                    if ("1".equals(attachment.getIsPatch())) { // NOI18N
                        add(attachment.new ApplyPatchAction());
                    }
                    super.setVisible(true);
                }
            } else {
                super.setVisible(false);
                removeAll();
            }
        }

    }

    public interface NewCommentHandler {
        void append(String text);
    }

    private final JPopupMenu expandPopup = new ExpandPopupMenu();
    private Set<ExpandLabel> expandLabels = new HashSet<ExpandLabel>();
    
    private class ExpandPopupMenu extends JPopupMenu {
        public ExpandPopupMenu() {
            add(new JMenuItem(new AbstractAction(NbBundle.getMessage(CommentsPanel.class, "LBL_ExpandAll")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (ExpandLabel l : expandLabels) {
                        l.setState(false);
                    }
                }
            }));
            add(new JMenuItem(new AbstractAction(NbBundle.getMessage(CommentsPanel.class, "LBL_CollapseAll")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (ExpandLabel l : expandLabels) {
                        l.setState(true);
                    }
                }
            }));
        }
    }    

    private void commentCollapsed(Long number) {
        collapsedComments.add(number);
    }

    private Set<Long> touchedCommenst = Collections.synchronizedSet(new HashSet<Long>());
    private void commentExpanded(Long number) {
        if(collapsedComments.remove(number)) {
            touchedCommenst.add(number);
        }
    }

    private boolean isCollapsed(Long number) {
        return collapsedComments.contains(number);
    }
    
    private void initCollapsedComments() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                Collection<Long> s = IssueSettingsStorage.getInstance().loadCollapsedCommenst(issue.getRepository().getUrl(), issue.getID());
                for (Long l : s) {
                    if(!touchedCommenst.contains(l)) {
                        collapsedComments.add(l);
                    }
                }
            }
        });
    }
    
    void storeSettings() {
        if(issue != null) {
            IssueSettingsStorage.getInstance().storeCollapsedComments(collapsedComments, issue.getRepository().getUrl(), issue.getID());
        }
    }    
    
    private final static Icon ei;
    private final static Icon ci;
    private static final int ICON_WIDTH;
    static {
        JTree tv = new JTree();
        BasicTreeUI tvui = (BasicTreeUI) tv.getUI();
        ei = tvui.getExpandedIcon();
        ci = tvui.getCollapsedIcon();
        ICON_WIDTH = ei != null ? ei.getIconWidth() : 16;
    }
    private class ExpandLabel extends JLabel implements MouseListener {
        private final JTextPane textPane;
        private final JPanel headerPanel;
        private final JPanel placeholderPanel;
        private final JLabel commentLabel;
        private final Long number;
        
        public ExpandLabel(JPanel placeholderPanel, JTextPane textPane, JPanel headerPanel, JLabel commentLabel, Long number) {
            this.textPane = textPane;
            this.headerPanel = headerPanel;
            this.placeholderPanel = placeholderPanel;
            this.commentLabel = commentLabel;
            this.number = number;
            
            
            addMouseListener(this);
            setComponentPopupMenu(expandPopup);
            setState(isCollapsed(number));
            expandLabels.add(this);
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                setState(!isCollapsed(number)); 
            } 
        }

        @Override
        public void mousePressed(MouseEvent e) {}
        @Override
        public void mouseReleased(MouseEvent e) {}
        @Override
        public void mouseEntered(MouseEvent e) {}
        @Override
        public void mouseExited(MouseEvent e) {}
        
        private void setState(boolean collapsed) {
            if(collapsed) {
                textPane.setVisible(false);
                placeholderPanel.setVisible(false);
                commentLabel.setText(textPane.getText().replace("\n", " ").replace("\t", " "));
                setIcon(ci);
                headerPanel.setBackground(BLUE_BACKGROUND);
                commentCollapsed(number);
            } else {
                textPane.setVisible(true);
                placeholderPanel.setVisible(true);
                commentLabel.setText("");
                setIcon(ei);
                headerPanel.setBackground(Color.white);
                commentExpanded(number);
            }           
        }
    }
}
