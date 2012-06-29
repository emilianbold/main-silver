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

package org.netbeans.modules.kenai.ui.dashboard;

import org.netbeans.modules.team.ui.common.LinkButton;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.*;
import org.netbeans.modules.kenai.api.*;
import org.netbeans.modules.team.ui.common.InterestingNode;
import org.netbeans.modules.team.ui.treelist.TreeLabel;
import org.netbeans.modules.kenai.ui.spi.MessagingAccessor;
import org.netbeans.modules.kenai.ui.spi.MessagingHandle;
import org.netbeans.modules.kenai.ui.spi.ProjectAccessor;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.QueryAccessor;
import org.netbeans.modules.kenai.ui.spi.QueryHandle;
import org.netbeans.modules.kenai.ui.spi.QueryResultHandle;
import org.netbeans.modules.team.ui.treelist.LeafNode;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * My Project's root node
 *
 * @author Jan Becicka
 */
public class MyProjectNode extends LeafNode implements InterestingNode {

    private Notification bugNotification;
    private final ProjectHandle project;
    private final ProjectAccessor accessor;
    private final QueryAccessor qaccessor;
    private final MessagingAccessor maccessor;
    private MessagingHandle mh;
    private QueryHandle allIssuesQuery;
    private PropertyChangeListener notificationListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (QueryHandle.PROP_QUERY_ACTIVATED.equals(evt.getPropertyName())) {
                if (bugNotification != null) {
                    bugNotification.clear();
                }
                allIssuesQuery.removePropertyChangeListener(notificationListener);
            }
        }
    };

    private Action openAction;

    private JPanel component = null;
    private JLabel lbl = null;
//    private LinkButton btnBookmark = null;
    private LinkButton btnOpen = null;
    private LinkButton btnMessages = null;
    private LinkButton btnBugs = null;

    private boolean isMemberProject = false;

    private final Object LOCK = new Object();

    private final PropertyChangeListener projectListener;
    private TreeLabel rightPar;
    private TreeLabel leftPar;
    private RequestProcessor issuesRP = new RequestProcessor(MyProjectNode.class);

    public MyProjectNode( final ProjectHandle project ) {
        super( null );
        if (project==null)
            throw new IllegalArgumentException("project cannot be null"); // NOI18N
        this.projectListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if( ProjectHandle.PROP_CONTENT.equals( evt.getPropertyName()) ) {
                    refreshChildren();
                    if( null != lbl )
                        lbl.setText(project.getDisplayName());
                } else if(MessagingHandle.PROP_MESSAGE_COUNT.equals(evt.getPropertyName())) {
                    if (btnMessages!=null) {
                        setOnline(mh.getMessageCount()>0);
                        btnMessages.setText(mh.getMessageCount()+"");
                    }
                } else if (KenaiProject.PROP_PROJECT_NOTIFICATION.equals(evt.getPropertyName())) {
                    KenaiNotification notification = (KenaiNotification) evt.getNewValue();
                    if (notification.getType() == KenaiService.Type.ISSUES && !notification.getAuthor().equals(project.getKenaiProject().getKenai().getPasswordAuthentication().getUserName())) {
                        showBugNotification(notification);
                    }

                } else if (Kenai.PROP_XMPP_LOGIN.equals(evt.getPropertyName())) {
                    if (evt.getOldValue()==null) {
                        setOnline(mh.getMessageCount()>0);
                    } else if (evt.getNewValue()==null) {
                        setOnline(false);
                        mh.removePropertyChangeListener(projectListener);
                        mh=maccessor.getMessaging(project);
                        mh.addPropertyChangeListener(projectListener);
                    }
                } else if (QueryHandle.PROP_QUERY_RESULT.equals(evt.getPropertyName())) {
                    List<QueryResultHandle> queryResults = (List<QueryResultHandle>) evt.getNewValue();
                    for (QueryResultHandle queryResult : queryResults) {
                        if (queryResult.getResultType() == QueryResultHandle.ResultType.ALL_CHANGES_RESULT) {
                            DashboardImpl.getInstance().myProjectsProgressStarted();
                            setBugsLater(queryResult);
                            return;
                        }
                    }
                }
            }
        };
        this.project = project;
        this.accessor = ProjectAccessor.getDefault();
        this.maccessor = MessagingAccessor.getDefault();
        this.qaccessor = QueryAccessor.getDefault();
        this.project.addPropertyChangeListener( projectListener );
        this.mh = maccessor.getMessaging(project);
        this.mh.addPropertyChangeListener(projectListener);
        project.getKenaiProject().getKenai().addPropertyChangeListener(projectListener);
        project.getKenaiProject().addPropertyChangeListener(projectListener);
    }

    ProjectHandle getProject() {
        return project;
    }

    ProjectAccessor getAccessor() {
        return accessor;
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus) {
        synchronized( LOCK ) {
            if( null == component ) {
                component = new JPanel( new GridBagLayout() );
                component.setOpaque(false);
                lbl = new TreeLabel(project.getDisplayName());
                component.add( lbl, new GridBagConstraints(0,0,1,1,0.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,16,0,3), 0,0) );

                int count = mh.getMessageCount();

                leftPar = new TreeLabel("("); // NOI18N
                rightPar = new TreeLabel(")"); // NOI18N
                component.add(leftPar, new GridBagConstraints(1, 0, 1, 1, 0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                btnMessages = new LinkButton(count + "", ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/collab/resources/newmessage.png", true), maccessor.getOpenMessagesAction(project)); // NOI18N
                btnMessages.setHorizontalTextPosition(JLabel.LEFT);
                component.add(btnMessages, new GridBagConstraints(2, 0, 1, 1, 0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                component.add(rightPar, new GridBagConstraints(4, 0, 1, 1, 0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                setOnline(mh.getOnlineCount() >= 0 && count >0);
                
                issuesRP.post(new Runnable() {

                    public void run() {
                        DashboardImpl.getInstance().myProjectsProgressStarted();
                        allIssuesQuery = qaccessor.getAllIssuesQuery(project);
                        if (allIssuesQuery != null) {
                            allIssuesQuery.addPropertyChangeListener(projectListener);
                            List<QueryResultHandle> queryResults = qaccessor.getQueryResults(allIssuesQuery);
                            for (QueryResultHandle queryResult:queryResults) {
                                if (queryResult.getResultType()==QueryResultHandle.ResultType.ALL_CHANGES_RESULT) {
                                    setBugsLater(queryResult);
                                    return;
                                }
                            }
                        }
                        DashboardImpl.getInstance().myProjectsProgressFinished();
                    }
                });


                component.add( new JLabel(), new GridBagConstraints(5,0,1,1,1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0) );
//                btnBookmark = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/bookmark.png", true), accessor.getBookmarkAction(project)); //NOI18N
//                btnBookmark.setToolTipText(NbBundle.getMessage(MyProjectNode.class, "LBL_LeaveProject"));
//                btnBookmark.setRolloverEnabled(true);
//                btnBookmark.setRolloverIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/bookmark_over.png", true));
//                component.add( btnBookmark, new GridBagConstraints(6,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
                btnOpen = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/open.png", true), getOpenAction()); //NOI18N
                btnOpen.setText(null);
                btnOpen.setToolTipText(NbBundle.getMessage(MyProjectNode.class, "LBL_Open"));
                btnOpen.setRolloverEnabled(true);
                btnOpen.setRolloverIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/open_over.png", true)); // NOI18N
                component.add( btnOpen, new GridBagConstraints(7,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
            }
            lbl.setForeground(foreground);
            return component;
        }
    }

    @Override
    public Action getDefaultAction() {
        return accessor.getDefaultAction(project, false);
    }

    private void setOnline(final boolean b) {
        Runnable run = new Runnable() {

            public void run() {
                if (btnBugs == null || "0".equals(btnBugs.getText())) { // NOI18N
                    if (leftPar != null) {
                        leftPar.setVisible(b);
                    }
                    if (rightPar != null) {
                        rightPar.setVisible(b);
                    }
                }
                if (btnMessages != null) {
                    btnMessages.setVisible(b);
                }
                DashboardImpl.getInstance().dashboardComponent.repaint();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }

    private Action getOpenAction() {
        if (openAction == null) {
            openAction = getDefaultAction();
        }
        return openAction;
    }

    @Override
    public Action[] getPopupActions() {
        return accessor.getPopupActions(project, false);
    }

    void setMemberProject(boolean isMemberProject) {
        if( isMemberProject == this.isMemberProject )
            return;
        this.isMemberProject = isMemberProject;
        fireContentChanged();
        refreshChildren();
    }

    @Override
    protected void dispose() {
        super.dispose();
        if( null != project ) {
            project.removePropertyChangeListener( projectListener );
            project.getKenaiProject().getKenai().removePropertyChangeListener(projectListener);
            project.getKenaiProject().removePropertyChangeListener(projectListener);
        }
        if (null != mh) {
            mh.removePropertyChangeListener(projectListener);
        }
        project.getKenaiProject().getKenai().removePropertyChangeListener(projectListener);
        if (allIssuesQuery != null) {
            allIssuesQuery.removePropertyChangeListener(projectListener);
            allIssuesQuery=null;
        }
        if (bugNotification != null) {
            bugNotification.clear();
        }
    }

    private void setBugsLater(final QueryResultHandle bug) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (btnBugs!=null) {
                    component.remove(btnBugs);
                }
                boolean hasMsgs = btnMessages != null && btnMessages.isVisible();
                btnBugs = new LinkButton(bug.getText(), ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/bug.png", true), qaccessor.getOpenQueryResultAction(bug)); // NOI18N
                btnBugs.setHorizontalTextPosition(JLabel.LEFT);
                btnBugs.setToolTipText(bug.getToolTipText());
                component.add( btnBugs, new GridBagConstraints(3,0,1,1,0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
                boolean visible = hasMsgs || !"0".equals(bug.getText()); // NOI18N
                leftPar.setVisible(visible);
                rightPar.setVisible(visible);
                btnBugs.setVisible(!"0".equals(bug.getText())); // NOI18N
                component.validate();
                DashboardImpl instance = DashboardImpl.getInstance();
                instance.myProjectsProgressFinished();
                instance.dashboardComponent.repaint();
            }
        });
    }

    private void showBugNotification(final KenaiNotification n) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (bugNotification != null) {
                    bugNotification.clear();
                }
                allIssuesQuery.removePropertyChangeListener(notificationListener);
                allIssuesQuery.addPropertyChangeListener(notificationListener);
                bugNotification = NotificationDisplayer.getDefault().notify(
                        NbBundle.getMessage(MyProjectNode.class, "LBL_NewOrChangedBugs", project.getDisplayName()),
                        ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/bug.png", true),
                        NbBundle.getMessage(MyProjectNode.class, "CTL_Show"),
                        btnBugs.getActionListeners()[0],
                        NotificationDisplayer.Priority.SILENT);
            }
        });

    }
}
