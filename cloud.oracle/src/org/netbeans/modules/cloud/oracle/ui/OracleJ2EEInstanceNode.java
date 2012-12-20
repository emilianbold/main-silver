/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cloud.oracle.ui;

import java.awt.Image;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.Action;
import org.netbeans.libs.oracle.cloud.sdkwrapper.model.Application;
import org.netbeans.libs.oracle.cloud.sdkwrapper.model.ApplicationType;
import org.netbeans.modules.cloud.oracle.OracleInstance;
import org.netbeans.modules.cloud.oracle.serverplugin.OracleJ2EEInstance;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 */
public class OracleJ2EEInstanceNode extends AbstractNode {
    
    public static final String WEBLOGIC_ICON = "org/netbeans/modules/cloud/oracle/ui/resources/weblogic.png"; // NOI18N
    
    private OracleJ2EEInstance aij;
    
    private boolean basicNode;
    
    private final OracleJ2EEInstanceChildren children;
    
    public OracleJ2EEInstanceNode(OracleJ2EEInstance aij, boolean basicNode) {
        this(aij, basicNode, new InstanceContent(), new OracleJ2EEInstanceChildren(aij, basicNode));
        
    }
    
    private OracleJ2EEInstanceNode(OracleJ2EEInstance aij, boolean basicNode, InstanceContent ic, OracleJ2EEInstanceChildren children) {
        super(children, new AbstractLookup(ic));
        this.basicNode = basicNode;
        this.aij = aij;
        this.children = children;
        ic.add(aij);
        ic.add(aij.getOracleInstance());
        ic.add(this);
        setName(""); // NOI18N
        setDisplayName(aij.getDisplayName());
        setIconBaseWithExtension(WEBLOGIC_ICON);
        
    }
    
    void refresh() {
        children.readKeys();
    }
    
    @Override
    public Image getIcon(int type) {
        return badgeIcon(super.getIcon(type));
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return badgeIcon(super.getOpenedIcon(type));
    }   
    
    private static final String RUNNING_ICON 
            = "org/netbeans/modules/cloud/oracle/ui/resources/running.png"; // NOI18N
    private static final String WAITING_ICON
            = "org/netbeans/modules/cloud/oracle/ui/resources/waiting.png"; // NOI18N
    private static final String TERMINATED_ICON
            = "org/netbeans/modules/cloud/oracle/ui/resources/terminated.png"; // NOI18N
    private static final String FAILED_ICON
            = "org/netbeans/modules/cloud/oracle/ui/resources/failed.png"; // NOI18N
    
    private Image badgeIcon(Image origImg) {
        if (basicNode) {
            return origImg;
        }
        Image badge = null;        
        switch (aij.getState()) {
            case UPDATING:
            case LAUNCHING:
            case TERMINATING:
                badge = ImageUtilities.loadImage(WAITING_ICON);
                break;
            case READY:
                badge = ImageUtilities.loadImage(RUNNING_ICON);
                break;
            case TERMINATED:
                badge = ImageUtilities.loadImage(TERMINATED_ICON);
                break;
        }
        return badge != null ? ImageUtilities.mergeImages(origImg, badge, 15, 8) : origImg;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(RefreshOracleJ2EEInstanceNodeAction.class),
            SystemAction.get(RemoteServerPropertiesAction.class)
        };
    }
    
    static class OracleJ2EEInstanceChildren extends Children.Keys<Application> {

        private OracleJ2EEInstance aij;
        private boolean basicNode;

        public OracleJ2EEInstanceChildren(OracleJ2EEInstance aij, boolean basicNode) {
            this.aij = aij;
            this.basicNode = basicNode;
            setKeys(Collections.<Application>emptySet());
        }
        
        @Override
        protected void addNotify() {
            if (!basicNode) {
                readKeys();
            }
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.<Application>emptySet());
        }
        
        @Override
        protected Node[] createNodes(Application key) {
            return new Node[]{new ApplicationNode(aij, key)};
        }
        
        private void readKeys() {
            OracleInstance.runAsynchronously(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    List<Application> apps = aij.getOracleInstance().getApplications();
                    Collections.sort(apps, new Comp());
                    OracleJ2EEInstanceChildren.this.setKeys(apps);
                    return null;
                }
            });
        }
    }
    
    private static final class Comp implements Comparator<Application>, Serializable {
        @Override
        public int compare(Application o1, Application o2) {
            String ap1 = o1.getApplicationName();
            String ap2 = o2.getApplicationName();
            if (ap1 == null) {
                ap1 = "ZZZ"; // NOI18N
            }
            if (ap2 == null) {
                ap2 = "ZZZ"; // NOI18N
            }
            return ap1.compareToIgnoreCase(ap2);
        }
    }

    public static class ApplicationNode extends AbstractNode {

        private Application app;
        
        public ApplicationNode(OracleJ2EEInstance aij, Application app) {
            this(aij, app, new InstanceContent());
        }
        
        private ApplicationNode(OracleJ2EEInstance aij, Application app, InstanceContent ic) {
            super(Children.LEAF, new AbstractLookup(ic));
            ic.add(aij);
            ic.add(this);
            this.app = app;
            setName(""); // NOI18N
            setDisplayName(app.getApplicationName());
        }

        public Application getApp() {
            return app;
        }

        void setApp(Application app) {
            this.app = app;
            fireIconChange();
        }
        
        void refreshChildren() {
            ((OracleJ2EEInstanceNode)getParentNode()).refresh();
        }
        
        @Override
        public Image getIcon(int type) {
            return badgeIcon(UISupport.getIcon(app.getType() == ApplicationType.WAR  || app.getType() == null ?
                    UISupport.ServerIcon.WAR_ARCHIVE : UISupport.ServerIcon.EAR_ARCHIVE));
        }

        @Override
        public Image getOpenedIcon(int type) {
            return badgeIcon(UISupport.getIcon(app.getType() == ApplicationType.WAR || app.getType() == null ?
                    UISupport.ServerIcon.WAR_ARCHIVE : UISupport.ServerIcon.EAR_ARCHIVE));
        }

        private Image badgeIcon(Image origImg) {
            Image badge = null;        
            switch (app.getState()) {
                    
                case STATE_UPDATE_PENDING:
                    badge = ImageUtilities.loadImage(WAITING_ICON);
                    break;
                case STATE_NEW:
                case STATE_RETIRED:
                case STATE_PREPARED:
                    badge = ImageUtilities.loadImage(TERMINATED_ICON);
                    break;
                case STATE_FAILED:
                    badge = ImageUtilities.loadImage(FAILED_ICON);
                    break;
                case STATE_ADMIN:
                case STATE_ACTIVE:
                    // no badge; app is running
                    break;
            }
            return badge != null ? ImageUtilities.mergeImages(origImg, badge, 15, 8) : origImg;
        }
        
        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {
                SystemAction.get(ViewApplicationAction.class),
                SystemAction.get(StartApplicationAction.class),
                SystemAction.get(StopApplicationAction.class),
                SystemAction.get(UndeployApplicationAction.class),
            };
        }

    }
    
}
