/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.subversion.remote.ui.commit;

import java.util.List;
import java.util.ArrayList;
import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.*;

/**
 *
 * @author Petr Kuzel
 */
public final class ExcludeFromCommitAction extends ContextAction {

    public static final int UNDEFINED = -1;
    public static final int EXCLUDING = 1;
    public static final int INCLUDING = 2;

    @Override
    protected boolean enable(Node[] nodes) {
        Context cachedContext = getCachedContext(nodes);
        final FileSystem fileSystem = cachedContext.getFileSystem();
        if (fileSystem == null || !VCSFileProxySupport.isConnectedFileSystem(fileSystem)) {
            return false;
        }
        return isCacheReady() && getActionStatus(nodes) != UNDEFINED;
    }

    @Override
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_LOCAL_CHANGE;
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_LOCAL_CHANGE;
    }

    @Override
    protected String getBaseName(Node [] activatedNodes) {
        int actionStatus = getActionStatus(activatedNodes);
        switch (actionStatus) {
        case UNDEFINED:
        case EXCLUDING:
            return "popup_commit_exclude"; // NOI18N
        case INCLUDING:
            return "popup_commit_include"; // NOI18N
        default:
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }
    }
    
    public int getActionStatus(Node[] nodes) {
        VCSFileProxy [] files = getCachedContext(nodes).getFiles();
        int status = UNDEFINED;
        for (int i = 0; i < files.length; i++) {
            SvnModuleConfig config = SvnModuleConfig.getDefault(VCSFileProxySupport.getFileSystem(files[i]));
            if (config.isExcludedFromCommit(files[i].getPath())) {
                if (status == EXCLUDING) {
                    return UNDEFINED;
                }
                status = INCLUDING;
            } else {
                if (status == INCLUDING) {
                    return UNDEFINED;
                }
                status = EXCLUDING;
            }
        }
        return status;
    }

    @Override
    public void performContextAction(final Node[] nodes) {
        ProgressSupport support = new ContextAction.ProgressSupport(this, nodes, getCachedContext(nodes)) {
            @Override
            public void perform() {
                int status = getActionStatus(nodes);
                List<VCSFileProxy> files = new ArrayList<>();
                for (Node node : nodes) {
                    VCSFileProxy aFile = node.getLookup().lookup(VCSFileProxy.class);
                    FileObject fo = node.getLookup().lookup(FileObject.class);
                    if (aFile != null) {
                        files.add(aFile);
                    } else if (fo != null) {
                        VCSFileProxy f = VCSFileProxy.createFileProxy(fo);
                        if (f != null) {
                            files.add(f);
                        }
                    }
                }
                if (files.size() == 0) {
                    return;
                }
                SvnModuleConfig config = SvnModuleConfig.getDefault(VCSFileProxySupport.getFileSystem(files.get(0)));
                List<String> paths = new ArrayList<>(files.size());
                for (VCSFileProxy file : files) {
                    paths.add(file.getPath());
                }
                if (isCanceled()) {
                    return;
                }
                if (status == EXCLUDING) {
                    config.addExclusionPaths(paths);
                } else if (status == INCLUDING) {
                    config.removeExclusionPaths(paths);
                }
            }
        };
        support.start(createRequestProcessor(nodes));
    }

}
