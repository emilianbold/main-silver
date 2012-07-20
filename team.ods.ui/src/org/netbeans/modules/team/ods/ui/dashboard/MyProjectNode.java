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

package org.netbeans.modules.team.ods.ui.dashboard;

import com.tasktop.c2c.server.profile.domain.project.Project;
import org.netbeans.modules.team.ui.common.LinkButton;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.*;
import org.netbeans.modules.team.ods.api.ODSProject;
import org.netbeans.modules.team.ods.ui.api.CloudUiServer;
import org.netbeans.modules.team.ui.common.DefaultDashboard;
import org.netbeans.modules.team.ui.common.ProjectProvider;
import org.netbeans.modules.team.ui.treelist.TreeLabel;
import org.netbeans.modules.team.ui.spi.ProjectAccessor;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.QueryAccessor;
import org.netbeans.modules.team.ui.spi.QueryHandle;
import org.netbeans.modules.team.ui.spi.QueryResultHandle;
import org.netbeans.modules.team.ui.treelist.LeafNode;
import org.openide.awt.Notification;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * My Project's root node
 *
 * @author Jan Becicka
 */
public class MyProjectNode extends LeafNode implements ProjectProvider {

    private Notification bugNotification;
    private final ProjectHandle<ODSProject> project;
    private final ProjectAccessor accessor;
    private final QueryAccessor qaccessor;
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
    private LinkButton btnOpen = null;
    private LinkButton btnBugs = null;

    private boolean isMemberProject = false;

    private final Object LOCK = new Object();

    private final PropertyChangeListener projectListener;
    private TreeLabel rightPar;
    private TreeLabel leftPar;
    private RequestProcessor issuesRP = new RequestProcessor(MyProjectNode.class);
    private final DefaultDashboard<CloudUiServer, ODSProject> dashboard;

    public MyProjectNode( final ProjectHandle<ODSProject> project, final DefaultDashboard<CloudUiServer, ODSProject> dashboard) {
        super( null );
        if (project==null) {
            throw new IllegalArgumentException("project cannot be null"); // NOI18N
        }
        this.dashboard = dashboard;
        this.projectListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if( ProjectHandle.PROP_CONTENT.equals( evt.getPropertyName()) ) {
                    refreshChildren();
                    if( null != lbl ) {
                        lbl.setText(project.getDisplayName());
                    }
                } else if (QueryHandle.PROP_QUERY_RESULT.equals(evt.getPropertyName())) {
                    List<QueryResultHandle> queryResults = (List<QueryResultHandle>) evt.getNewValue();
                    for (QueryResultHandle queryResult : queryResults) {
                        if (queryResult.getResultType() == QueryResultHandle.ResultType.ALL_CHANGES_RESULT) {
                            dashboard.myProjectsProgressStarted();
                            setBugsLater(queryResult);
                            return;
                        }
                    }
                }
            }
        };
        this.project = project;
        this.accessor = dashboard.getDashboardProvider().getProjectAccessor();
        this.qaccessor = dashboard.getDashboardProvider().getQueryAccessor();
        this.project.addPropertyChangeListener( projectListener );
        project.getTeamProject().getServer().addPropertyChangeListener(projectListener);
        project.getTeamProject().addPropertyChangeListener(projectListener);
    }

    @Override
    public ProjectHandle getProject() {
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
                leftPar = new TreeLabel("("); // NOI18N
                rightPar = new TreeLabel(")"); // NOI18N
                component.add(leftPar, new GridBagConstraints(1, 0, 1, 1, 0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                component.add(rightPar, new GridBagConstraints(4, 0, 1, 1, 0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                setOnline(false);
                
                issuesRP.post(new Runnable() {
                    @Override
                    public void run() {
                        dashboard.myProjectsProgressStarted();
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
                        dashboard.myProjectsProgressFinished();
                        
                    }
                });


                component.add( new JLabel(), new GridBagConstraints(5,0,1,1,1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0) );
                btnOpen = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/team/ods/ui/resources/open.png", true), getOpenAction()); //NOI18N
                btnOpen.setText(null);
                btnOpen.setToolTipText(NbBundle.getMessage(MyProjectNode.class, "LBL_Open"));
                btnOpen.setRolloverEnabled(true);
                btnOpen.setRolloverIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/team/ods/ui/resources/open_over.png", true)); // NOI18N
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
            @Override
            public void run() {
                if (btnBugs == null || "0".equals(btnBugs.getText())) { // NOI18N
                    if (leftPar != null) {
                        leftPar.setVisible(b);
                    }
                    if (rightPar != null) {
                        rightPar.setVisible(b);
                    }
                }
                dashboard.dashboardComponent.repaint();
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
        if( isMemberProject == this.isMemberProject ) {
            return;
        }
        this.isMemberProject = isMemberProject;
        fireContentChanged();
        refreshChildren();
    }

    @Override
    protected void dispose() {
        super.dispose();
        
        project.removePropertyChangeListener( projectListener );
        project.getTeamProject().getServer().removePropertyChangeListener(projectListener);
        project.getTeamProject().removePropertyChangeListener(projectListener);
        
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
            @Override
            public void run() {
                if (btnBugs!=null) {
                    component.remove(btnBugs);
                }
                btnBugs = new LinkButton(bug.getText(), ImageUtilities.loadImageIcon("org/netbeans/modules/team/ods/ui/resources/bug.png", true), qaccessor.getOpenQueryResultAction(bug)); // NOI18N
                btnBugs.setHorizontalTextPosition(JLabel.LEFT);
                btnBugs.setToolTipText(bug.getToolTipText());
                component.add( btnBugs, new GridBagConstraints(3,0,1,1,0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
                boolean visible = !"0".equals(bug.getText()); // NOI18N
                leftPar.setVisible(visible);
                rightPar.setVisible(visible);
                btnBugs.setVisible(!"0".equals(bug.getText())); // NOI18N
                component.validate();
                dashboard.myProjectsProgressFinished();
                dashboard.dashboardComponent.repaint();
            }
        });
    }
}
