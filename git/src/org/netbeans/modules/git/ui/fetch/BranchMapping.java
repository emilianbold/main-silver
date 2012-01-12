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

import java.text.MessageFormat;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.ui.selectors.ItemSelector;
import org.netbeans.modules.git.ui.selectors.ItemSelector.Item;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class BranchMapping extends ItemSelector.Item {
    private final String label;
    private final String tooltip;
    private final String remoteBranch;
    private final GitRemoteConfig remote;
    private static final String BRANCH_MAPPING_LABEL = "{0} -> {1}/{0} [{2}]"; //NOI18N

    public BranchMapping (String remoteBranch, String localBranch, GitRemoteConfig remote, boolean preselected) {
        super(preselected);
        this.remoteBranch = remoteBranch;
        this.remote = remote;
        if(localBranch == null) {
            // added
            label = MessageFormat.format(BRANCH_MAPPING_LABEL, remoteBranch, remote.getRemoteName(), "<font color=\"#00b400\">A</font>");

            tooltip = NbBundle.getMessage(
                FetchBranchesStep.class, 
                "LBL_FetchBranchesPanel.BranchMapping.description", //NOI18N
                new Object[] { 
                    localBranch == null ? remote.getRemoteName() + "/" + remoteBranch : localBranch, //NOI18N
                    NbBundle.getMessage(FetchBranchesStep.class, "LBL_FetchBranchesPanel.BranchMapping.Mode.added.description") //NOI18N
                }); //NOI18N
        } else {
            // modified
            label = MessageFormat.format(BRANCH_MAPPING_LABEL, remoteBranch, remote.getRemoteName(), "<font color=\"#0000FF\">U</font>"); //NOI18N                 

            tooltip = NbBundle.getMessage(
                FetchBranchesStep.class, 
                "LBL_FetchBranchesPanel.BranchMapping.description", //NOI18N
                new Object[] { 
                    localBranch,
                    NbBundle.getMessage(FetchBranchesStep.class, "LBL_FetchBranchesPanel.BranchMapping.Mode.updated.description") //NOI18N
                }); 
        }
    }

    public String getRefSpec () {
        return GitUtils.getRefSpec(remoteBranch, remote.getRemoteName());
    }

    @Override
    public String getText () {
        return label;
    }

    @Override
    public String getTooltipText() {
        return tooltip;
    }

    public String getRemoteBranchName () {
        return remoteBranch;
    }
    
    public String getRemoteName () {
        return remote.getRemoteName();
    }

    @Override
    public int compareTo(Item t) {
        if(t == null) {
            return 1;
        }
        if(t instanceof BranchMapping) {
            return remoteBranch.compareTo(((BranchMapping)t).remoteBranch);
        }
        return 0;            
    }
}
