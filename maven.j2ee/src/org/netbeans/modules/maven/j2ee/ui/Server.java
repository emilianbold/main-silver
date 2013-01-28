/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.ui;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import static org.netbeans.modules.maven.j2ee.ui.Bundle.*;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.openide.util.NbBundle.Messages;

/**
 * This is an UI server representation. It is mostly used in different kinds of
 * Maven UI (project wizards, customizer etc.) and represent single application
 * server.
 *
 * @author Martin Janicek
 */
public final class Server implements Comparable<Server> {

    private String serverInstanceId;
    private String sessionServerInstanceId;


    public Server(String serverInstanceId) {
        this.serverInstanceId = serverInstanceId;
    }

    public Server(String serverInstanceId, String sessionServerInstanceId) {
        this(serverInstanceId);
        assert ExecutionChecker.DEV_NULL.equals(serverInstanceId);
        this.sessionServerInstanceId = sessionServerInstanceId;
    }

    public String getServerInstanceID() {
        return serverInstanceId;
    }

    public String getServerID() {
        if (ExecutionChecker.DEV_NULL.equals(serverInstanceId)) {
            return ExecutionChecker.DEV_NULL;
        }
        return MavenProjectSupport.obtainServerID(serverInstanceId);
    }

    public String getSessionServerInstanceId() {
        return sessionServerInstanceId;
    }

    @Override
    public int compareTo(Server wrapper) {
        // <No Server> option should be always the last one
        if (ExecutionChecker.DEV_NULL.equals(this.serverInstanceId)) {
            return -1;
        }

        // If one server is an GF instance and the second one is not, always return GF
        if (this.serverInstanceId.contains("gf") && !wrapper.serverInstanceId.contains("gf")) { //NOI18N
            return 1;
        }
        if (!this.serverInstanceId.contains("gf") && wrapper.serverInstanceId.contains("gf")) { //NOI18N
            return -1;
        }

        // Otherwise compare just by String name
        return this.toString().compareTo(wrapper.toString());
    }

    @Messages({
        "MSG_No_Permanent_Server=<No Permanent Server, using \"{0}\" temporarily>",
        "MSG_Invalid_Server=<Invalid Server>",
        "MSG_No_Server=<No Server Selected>"
    })
    @Override
    public String toString() {
        if (ExecutionChecker.DEV_NULL.equals(serverInstanceId)) {
            if (sessionServerInstanceId != null) {
                ServerInstance si = Deployment.getDefault().getServerInstance(sessionServerInstanceId);
                try {
                    return MSG_No_Permanent_Server(si.getDisplayName());
                } catch (InstanceRemovedException ex) {
                    return MSG_Invalid_Server(); //NOI18N
                }
            } else {
                return MSG_No_Server(); //NOI18N
            }
        }
        ServerInstance si = Deployment.getDefault().getServerInstance(serverInstanceId);
        if (si != null) {
            try {
                return si.getDisplayName();
            } catch (InstanceRemovedException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.FINE, "", ex);
            }
        }
        return serverInstanceId;
    }
}
