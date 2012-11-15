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
package org.netbeans.modules.kenai.ui.dashboard;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.collab.chat.MessagingAccessorImpl;
import org.netbeans.modules.kenai.ui.MemberAccessorImpl;
import org.netbeans.modules.kenai.ui.OpenNetBeansIDEProjects;
import org.netbeans.modules.kenai.ui.ProjectAccessorImpl;
import org.netbeans.modules.kenai.ui.SourceAccessorImpl;
import org.netbeans.modules.kenai.ui.api.KenaiServer;
import org.netbeans.modules.team.ui.common.ProjectNode;
import org.netbeans.modules.team.ui.common.SourceListNode;
import org.netbeans.modules.team.ui.spi.BuildAccessor;
import org.netbeans.modules.team.ui.spi.BuildHandle;
import org.netbeans.modules.team.ui.spi.DashboardProvider;
import org.netbeans.modules.team.ui.spi.MemberAccessor;
import org.netbeans.modules.team.ui.spi.MemberHandle;
import org.netbeans.modules.team.ui.spi.MessagingAccessor;
import org.netbeans.modules.team.ui.spi.ProjectAccessor;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.QueryAccessor;
import org.netbeans.modules.team.ui.spi.SourceAccessor;
import org.netbeans.modules.team.ui.spi.SourceHandle;
import org.netbeans.modules.team.ui.spi.TeamUIUtils;
import org.netbeans.modules.team.ui.treelist.LeafNode;
import org.netbeans.modules.team.ui.treelist.TreeListNode;
import org.openide.util.RequestProcessor;

/**
 *
 * @author tomas
 */
public class DashboardProviderImpl extends DashboardProvider<KenaiServer, KenaiProject> {
    
    private final KenaiServer server;
    private static final RequestProcessor RP = new RequestProcessor("Kenai logout", 5); //NOI18N

    public DashboardProviderImpl(KenaiServer server) {
        this.server = server;
    }
    
    @Override
    public Action createLogoutAction() {
        return new AbstractAction() {  
            @Override
            public void actionPerformed(ActionEvent e) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        server.logout();
                    }
                });
            }
        };
    }

    @Override
    public Action createLoginAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TeamUIUtils.showLogin(server, true);
            }
        };
    }

    @Override
    public LeafNode createMemberNode(MemberHandle user, TreeListNode parent) {
        return new MemberNode(user, parent);
    }

    @Override
    public TreeListNode createProjectLinksNode(ProjectNode pn, ProjectHandle<KenaiProject> project) {
        return new ProjectLinksNode(pn, project);
    }

    @Override
    public TreeListNode createMyProjectNode(ProjectHandle p) {
        return new MyProjectNode(p);
    }

    @Override
    public TreeListNode createSourceNode(SourceHandle s, SourceListNode sln) {
        return new SourceNode(s, sln);
    }

    @Override
    public MessagingAccessor<KenaiProject> getMessagingAccessor() {
        return MessagingAccessorImpl.getDefault();
    }

    @Override
    public MemberAccessor getMemberAccessor() {
        return MemberAccessorImpl.getDefault();
    }

    @Override
    public SourceAccessor getSourceAccessor() {
        return SourceAccessorImpl.getDefault();
    }

    @Override
    public TreeListNode createSourceListNode(ProjectNode pn, ProjectHandle<KenaiProject> project) {
        if (server.getUrl().toString().equals("https://netbeans.org")) { //NOI18N
            return new SourceListNode(pn, this, new OpenNetBeansIDEProjects(server.getKenai(), pn));
        } else {
            return new SourceListNode(pn, this, (LeafNode[]) null);
        }
    }

    @Override
    public QueryAccessor<KenaiProject> getQueryAccessor() {
        return getQueryAccessor(KenaiProject.class);
    }            

    @Override
    public ProjectAccessor<KenaiServer, KenaiProject> getProjectAccessor() {
        return ProjectAccessorImpl.getDefault();
    }

    @Override
    public BuildAccessor<KenaiProject> getBuildAccessor() {
        return new BuildAccessor() {
            public boolean isEnabled(ProjectHandle project) {
                return false;
            }
            public List<BuildHandle> getBuilds(ProjectHandle project) {
                return Collections.emptyList();
            }
            public Action getNewBuildAction(ProjectHandle project) {
                return null;
            };

            @Override
            public Class type() {
                return Object.class;
            }
        };
    }

    @Override
    public Collection<ProjectHandle<KenaiProject>> getMyProjects() {
        return server.getMyProjects();
    }

    @Override
    public KenaiServer forProject(ProjectHandle<KenaiProject> project) {
        return KenaiServer.forKenai(project.getTeamProject().getKenai());
    }
    
}
