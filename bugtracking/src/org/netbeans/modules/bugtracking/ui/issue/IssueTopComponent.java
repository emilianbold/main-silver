/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.ui.issue;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.search.FindSupport;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.RepositoryComboRenderer;
import org.netbeans.modules.bugtracking.util.RepositoryComboSupport;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.TopComponent;

/**
 * Top component that displays information about one issue.
 *
 * @author Jan Stola, Tomas Stupka
 */
public final class IssueTopComponent extends TopComponent implements PropertyChangeListener {
    /** Set of opened {@code IssueTopComponent}s. */
    private static Set<IssueTopComponent> openIssues = new HashSet<IssueTopComponent>();
    /** Issue displayed by this top-component. */
    private Issue issue;
    private RequestProcessor rp = new RequestProcessor("Bugtracking issue", 1, true); // NOI18N
    private Task prepareTask;
    private RepositoryComboSupport rs;
    private Node[] context;

    /**
     * Creates new {@code IssueTopComponent}.
     */
    public IssueTopComponent() {
        initComponents();
        BugtrackingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        for (BugtrackingConnector c : connectors) {
            c.addPropertyChangeListener(this);
        }
        preparingLabel.setVisible(false);
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onNewClick();
            }
        });
        JComponent findBar = FindSupport.create(this).getFindBar();
        findBar.setVisible(false);
        issuePanel.add(findBar, BorderLayout.PAGE_END);
    }

    /**
     * Returns issue displayed by this top-component.
     *
     * @return issue displayed by this top-component.
     */
    public Issue getIssue() {
        return issue;
    }

    public void initNewIssue(Repository toSelect, Node[] context) {
        initNewIssue(toSelect, false, context);
    }

    public void initNewIssue(Repository defaultRepository, boolean suggestedSelectionOnly, Node[] context) {
        BugtrackingUtil.logBugtrackingUsage(defaultRepository, "ISSUE_EDIT"); // NOI18N
        this.context = context;

        Font f = new JLabel().getFont();
        int s = f.getSize();
        findIssuesLabel.setFont(repoLabel.getFont().deriveFont(s * 1.7f));

        if ((defaultRepository != null) && !suggestedSelectionOnly) {
            /* fixed selection that cannot be changed by user */
            DefaultComboBoxModel  repoModel = new DefaultComboBoxModel();
            repoModel.addElement(defaultRepository);
            repositoryComboBox.setModel(repoModel);
            repositoryComboBox.setRenderer(new RepositoryComboRenderer());
            repositoryComboBox.setSelectedItem(defaultRepository);
            repositoryComboBox.setEnabled(false);
            newButton.setEnabled(false);
            onRepoSelected();
        } else {
            if(defaultRepository == null) {
                rs = RepositoryComboSupport.setup(this, repositoryComboBox, false);
            } else {
                rs = RepositoryComboSupport.setup(this, repositoryComboBox, defaultRepository);
            }
        }
        repositoryComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    onRepoSelected();
                }
            }
        });
        setNameAndTooltip();
    }

    public void initNoIssue(final String issueId) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                preparingLabel.setVisible(true);
                repoPanel.setVisible(false);
                if(issueId != null) {
                    String desc = NbBundle.getMessage(IssueTopComponent.class, "LBL_OPENING_ISSUE", new Object[]{issueId});
                    preparingLabel.setText(desc);
                    setName(NbBundle.getMessage(IssueTopComponent.class, "LBL_LOADING_ISSUE", new Object[]{issueId}));
                    setToolTipText(desc);
                } else {
                    setNameAndTooltip();
                }
            }
        });
    }

    /**
     * Sets issue displayed by this top-component.
     *
     * @param issue displayed by this top-component.
     */
    public void setIssue(Issue issue) {
        assert (this.issue == null);
        BugtrackingUtil.logBugtrackingUsage(issue.getRepository(), "ISSUE_EDIT"); // NOI18N
        this.issue = issue;
        preparingLabel.setVisible(false);
        issuePanel.add(issue.getController().getComponent(), BorderLayout.CENTER);
        repoPanel.setVisible(false);
        setNameAndTooltip();
        issue.addPropertyChangeListener(this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        repoPanel = new javax.swing.JPanel();
        repositoryComboBox = new javax.swing.JComboBox();
        findIssuesLabel = new javax.swing.JLabel();
        repoLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        newButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        issuePanel = new javax.swing.JPanel();
        preparingLabel = new javax.swing.JLabel();

        repoPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        org.openide.awt.Mnemonics.setLocalizedText(findIssuesLabel, org.openide.util.NbBundle.getMessage(IssueTopComponent.class, "IssueTopComponent.findIssuesLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(repoLabel, org.openide.util.NbBundle.getMessage(IssueTopComponent.class, "IssueTopComponent.repoLabel.text")); // NOI18N
        repoLabel.setFocusCycleRoot(true);

        jPanel1.setOpaque(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 64, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 8, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(newButton, org.openide.util.NbBundle.getMessage(IssueTopComponent.class, "IssueTopComponent.newButton.text")); // NOI18N

        javax.swing.GroupLayout repoPanelLayout = new javax.swing.GroupLayout(repoPanel);
        repoPanel.setLayout(repoPanelLayout);
        repoPanelLayout.setHorizontalGroup(
            repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(repoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(repoPanelLayout.createSequentialGroup()
                        .addComponent(repoLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(repositoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(findIssuesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        repoPanelLayout.setVerticalGroup(
            repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(repoPanelLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(repoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(findIssuesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(repoLabel)
                    .addComponent(repositoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        issuePanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        issuePanel.setLayout(new java.awt.BorderLayout());

        preparingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(preparingLabel, org.openide.util.NbBundle.getMessage(IssueTopComponent.class, "IssueTopComponent.preparingLabel.text")); // NOI18N
        issuePanel.add(preparingLabel, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(repoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(issuePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(repoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(issuePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void onNewClick() {
        Repository repo = BugtrackingUtil.createRepository();
        if(repo != null) {
            repositoryComboBox.addItem(repo);
            repositoryComboBox.setSelectedItem(repo);
        }
    }

    private BugtrackingController controller;
    private void onRepoSelected() {
        if(prepareTask != null) {
            prepareTask.cancel();
        }
        Cancellable c = new Cancellable() {
            @Override
            public boolean cancel() {
                if(prepareTask != null) {
                    prepareTask.cancel();
                }
                return true;
            }
        };
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(IssueTopComponent.class, "CTL_PreparingIssue"), c); // NOI18N
        prepareTask = rp.post(new Runnable() {
            @Override
            public void run() {
                try {
                    handle.start();
                    preparingLabel.setVisible(true);
                    Repository repo = getRepository();
                    if (repo == null) {
                        return;
                    }
                    if(issue != null) {
                        if(controller != null) issuePanel.remove(controller.getComponent());
                        issue.removePropertyChangeListener(IssueTopComponent.this);
                    }
                    issue = repo.createIssue();
                    if (issue == null) {
                        return;
                    }

                    IssueAccessor.getInstance().setSelection(issue, context);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            controller = issue.getController();
                            issuePanel.add(controller.getComponent(), BorderLayout.CENTER);
                            issue.addPropertyChangeListener(IssueTopComponent.this);
                            revalidate();
                            repaint();

                            focusFirstEnabledComponent();
                        }
                    });
                } finally {
                    preparingLabel.setVisible(false);
                    handle.finish();
                    prepareTask = null;
                }
            }
        });
    }

    private Repository getRepository() {
        Object item = repositoryComboBox.getSelectedItem();
        if (item == null || !(item instanceof Repository)) {
            return null;
        }
        return (Repository) item;
    }

    private void focusFirstEnabledComponent() {
        repositoryComboBox.requestFocusInWindow();
        if(!repositoryComboBox.isEnabled()) {
            newButton.requestFocusInWindow();
            if(!newButton.isEnabled()) {
                newButton.transferFocus();
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel findIssuesLabel;
    private javax.swing.JPanel issuePanel;
    private javax.swing.JPanel jPanel1;
    private org.netbeans.modules.bugtracking.util.LinkButton newButton;
    private javax.swing.JLabel preparingLabel;
    private javax.swing.JLabel repoLabel;
    private javax.swing.JPanel repoPanel;
    private javax.swing.JComboBox repositoryComboBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        openIssues.add(this);
        if(issue != null) {
            issue.getController().opened();
        }
        BugtrackingManager.LOG.log(Level.FINE, "IssueTopComponent Opened {0}", (issue != null ? issue.getID() : "null")); // NOI18N
    }

    @Override
    public void componentClosed() {
        openIssues.remove(this);
        if(issue != null) {
            issue.removePropertyChangeListener(this);
            issue.getController().closed();
        }
        if(prepareTask != null) {
            prepareTask.cancel();
        }
        BugtrackingManager.LOG.log(Level.FINE, "IssueTopComponent Closed {0}", (issue != null ? issue.getID() : "null")); // NOI18N
    }

    /**
     * Returns top-component that should display the given issue.
     *
     * @param issue issue for which the top-component should be found.
     * @return top-component that should display the given issue.
     */
    public static synchronized IssueTopComponent find(Issue issue) {
        return find(issue, true);
    }

    /**
     * Returns top-component that should display the given issue. 
     *
     * @param issue issue for which the top-component should be found.
     * @param forceCreate determines wheter a TopComponent is created if none available yet
     *
     * @return top-component that should display the given issue.
     */
    public static synchronized IssueTopComponent find(Issue issue, boolean forceCreate) {
        for (IssueTopComponent tc : openIssues) {
            if (issue.equals(tc.getIssue())) {
                return tc;
            }
        }
        if(!forceCreate) {
            return null;
        }
        IssueTopComponent tc = new IssueTopComponent();
        tc.setIssue(issue);
        return tc;
    }

    /**
     * Returns top-component that should display the issue with the given issueId.
     *
     * @param issueId
     * @return
     */
    public static synchronized IssueTopComponent find(String issueId) {
        assert issueId != null;
        for (IssueTopComponent tc : openIssues) {
            Issue i = tc.getIssue();
            if(i == null) continue;
            if (issueId.equals(i.getID())) {
                return tc;
            }
        }
        IssueTopComponent tc = new IssueTopComponent();
        return tc;
    }

    private void setNameAndTooltip() throws MissingResourceException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if(issue != null) {
                    setName(issue.getShortenedDisplayName());
                    setToolTipText(issue.getTooltip());
                } else {
                    setName(NbBundle.getMessage(IssueTopComponent.class, "CTL_IssueTopComponent")); // NOI18N
                    setToolTipText(NbBundle.getMessage(IssueTopComponent.class, "CTL_IssueTopComponent")); // NOI18N
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Issue.EVENT_ISSUE_DATA_CHANGED)) {
            repoPanel.setVisible(false);
            setNameAndTooltip();
        } else if(evt.getPropertyName().equals(BugtrackingConnector.EVENT_REPOSITORIES_CHANGED)) {
            if(!repositoryComboBox.isEnabled()) {
                // well, looks like there shuold be only one repository available
                return;
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if(rs != null) {
                        rs.refreshRepositoryModel();
                    }
                }
            });
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        if (issue == null) {
            return repositoryComboBox.requestFocusInWindow();
        } else {
            return issue.getController().getComponent().requestFocusInWindow();
        }
    }

}
