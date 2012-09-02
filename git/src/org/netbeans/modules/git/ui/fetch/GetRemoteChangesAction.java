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
package org.netbeans.modules.git.ui.fetch;

import java.util.Map;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.libs.git.GitTransportUpdate.Type;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import static org.netbeans.modules.git.ui.fetch.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author ondra
 */
abstract class GetRemoteChangesAction extends SingleRepositoryAction {
    protected void log (Map<String, GitTransportUpdate> updates, OutputLogger logger) {
        if (updates.isEmpty()) {
            logger.output(NbBundle.getMessage(GetRemoteChangesAction.class, "MSG_GetRemoteChangesAction.updates.noChange")); //NOI18N
        } else {
            for (Map.Entry<String, GitTransportUpdate> e : updates.entrySet()) {
                GitTransportUpdate update = e.getValue();
                if (update.getType() == Type.BRANCH) {
                    logger.output(NbBundle.getMessage(GetRemoteChangesAction.class, "MSG_GetRemoteChangesAction.updates.updateBranch", new Object[] { //NOI18N
                        update.getLocalName(), 
                        update.getOldObjectId(),
                        update.getNewObjectId(),
                        update.getResult(),
                    }));
                } else {
                    logger.output(NbBundle.getMessage(GetRemoteChangesAction.class, "MSG_GetRemoteChangesAction.updates.updateTag", new Object[] { //NOI18N
                        update.getLocalName(), 
                        update.getResult(),
                    }));
                }
            }
        }
    }
        
    private static String parseRemote (String branchName) {
        int pos = branchName.lastIndexOf('/');
        String remoteName = null;
        if (pos > 0) {
            remoteName = branchName.substring(0, pos);
        }
        return remoteName;
    }

    @Messages({"# {0} - branch name", "MSG_Err.noTrackedBranch=No tracked remote branch specified for local {0}",
        "# {0} - branch name", "MSG_Err.trackedBranchLocal=Tracked branch {0} is not a remote branch"})
    protected GitBranch getTrackedBranch (RepositoryInfo info, String errorLabel) {
        GitBranch activeBranch = info.getActiveBranch();
        if (activeBranch == null) {
            return null;
        }
        GitBranch trackedBranch = activeBranch.getTrackedBranch();
        if (trackedBranch == null) {
            notifyError(errorLabel,
                    MSG_Err_noTrackedBranch(activeBranch.getName())); //NOI18N
            return null;
        }
        if (!trackedBranch.isRemote()) {
            notifyError(errorLabel, MSG_Err_trackedBranchLocal(trackedBranch.getName())); //NOI18N
            return null;
        }
        return trackedBranch;
    }

    @Messages({"# {0} - branch name", "MSG_Err.noRemote=No remote found for branch {0}",
        "# {0} - branch name", "MSG_Err.noUri=No URI specified for remote {0}",
        "# {0} - branch name", "MSG_Err.noSpecs=No fetch ref specs specified for remote {0}"})
    protected static GitRemoteConfig getRemoteConfigForActiveBranch (GitBranch trackedBranch, RepositoryInfo info, String errorLabel) {
        Map<String, GitRemoteConfig> remotes = info.getRemotes();
        String remoteName = parseRemote(trackedBranch.getName());
        GitRemoteConfig cfg = remoteName == null ? null : remotes.get(remoteName);
        if (cfg == null) {
            notifyError(errorLabel, MSG_Err_noRemote(trackedBranch.getName()));
            return null;
        }
        if (cfg.getUris().isEmpty()) {
            notifyError(errorLabel, MSG_Err_noUri(cfg.getRemoteName()));
            return null;
        }
        if (cfg.getFetchRefSpecs().isEmpty()) {
            notifyError(errorLabel, MSG_Err_noSpecs(cfg.getRemoteName()));
            return null;
        }
        return cfg;
    }

    private static void notifyError (String errorLabel, String errorMessage) {
        NotifyDescriptor nd = new NotifyDescriptor(
            errorMessage,
            errorLabel,
            NotifyDescriptor.DEFAULT_OPTION,
            NotifyDescriptor.ERROR_MESSAGE,
            new Object[]{NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION},
            NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(nd);
    }
}
