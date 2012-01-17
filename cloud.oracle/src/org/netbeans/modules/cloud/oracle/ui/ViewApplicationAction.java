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

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.libs.oracle.cloud.sdkwrapper.exception.UnknownResourceException;
import org.netbeans.libs.oracle.cloud.sdkwrapper.model.Application;
import org.netbeans.libs.oracle.cloud.sdkwrapper.model.ApplicationState;
import org.netbeans.modules.cloud.oracle.serverplugin.OracleJ2EEInstance;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 */
public class ViewApplicationAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        final OracleJ2EEInstance inst = activatedNodes[0].getLookup().lookup(OracleJ2EEInstance.class);
        final OracleJ2EEInstanceNode.ApplicationNode appNode = activatedNodes[0].getLookup().lookup(OracleJ2EEInstanceNode.ApplicationNode.class);
        Application app = appNode.getApp();
        
        // check latest status of app
        try {
            app = inst.getOracleInstance().refreshApplication(app);
        } catch (UnknownResourceException ex) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.MSG_WasRemoved()));
            appNode.refreshChildren();
            return;
        }
        if (!isAppInRightState(app)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.MSG_WrongState()));
            appNode.setApp(app);
            return;
        }
        String url = app.getApplicationUrls().get(0);
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(url));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        if (activatedNodes[0].getLookup().lookup(OracleJ2EEInstance.class) == null) {
            return false;
        }
        OracleJ2EEInstanceNode.ApplicationNode appNode = activatedNodes[0].getLookup().lookup(OracleJ2EEInstanceNode.ApplicationNode.class);
        if (appNode == null) {
            return false;
        }
        return isAppInRightState(appNode.getApp());
    }

    protected boolean isAppInRightState(Application app) {
        return ApplicationState.STATE_ACTIVE == app.getState() && app.getApplicationUrls() != null && app.getApplicationUrls().size() > 0;
    }
    
    @Override
    public String getName() {
        return "View";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
    
}
