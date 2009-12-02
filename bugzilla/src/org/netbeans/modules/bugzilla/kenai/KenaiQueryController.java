/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.bugzilla.kenai;

import java.util.List;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.BugzillaConnector;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugzilla.query.QueryController;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.kenai.ui.api.NbModuleOwnerSupport.OwnerInfo;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

/**
 *
 * @author Tomas Stupka
 */
public class KenaiQueryController extends QueryController {
    private final String product;
    private final boolean predefinedQuery;

    public KenaiQueryController(BugzillaRepository repository, BugzillaQuery query, String urlParameters, String product, boolean predefinedQuery) {
        super(repository, query, urlParameters, false, false);
        this.product = product;
        this.predefinedQuery = predefinedQuery;
        postPopulate(urlParameters, false);
    }

    @Override
    public void populate(String urlParameters) {
        if(BugzillaUtil.isNbRepository(getRepository())) {
            if(urlParameters == null) {
                Node[] selection = query.getSelection();
                if(selection == null) {
                    // XXX not sure why we need this - i'm going to keep it for now,
                    // doesn't seem to harm
                    selection = WindowManager.getDefault().getRegistry().getActivatedNodes();
                }
                OwnerInfo ownerInfo = getRepository().getOwnerInfo(selection);
                if(ownerInfo != null) {
                    StringBuffer sb = new StringBuffer();
                    String owner = ownerInfo.getOwner();
                    if(owner == null || !owner.equals(product) ) {
                        // XXX is this even possible?
                    } else {
                        sb.append("product=");                                  // NOI18N
                        sb.append(owner);
                        List<String> data = ownerInfo.getExtraData();
                        if(data != null && data.size() > 0) {
                            sb.append("&component=");                           // NOI18N
                            sb.append(data.get(0));
                        }
                        urlParameters = sb.toString();
                    }
                }
                if(urlParameters == null) {
                    urlParameters = "product=" + product;                       // NOI18N
                }
            }

            super.populate(urlParameters);
        } else {
            super.populate(urlParameters);
            disableProduct();
        }
    }

    @Override
    protected void enableFields(boolean bl) {
        super.enableFields(bl);

        if(predefinedQuery) {
            // override - for predefined kenai queries are those always disabled
            panel.modifyButton.setEnabled(false);
            panel.removeButton.setEnabled(false);
        }
    }

    @Override
    public void closed() {
        super.closed();
        // override
        scheduleForRefresh();
    }

    protected void logAutoRefreshEvent(boolean autoRefresh) {
        BugtrackingUtil.logAutoRefreshEvent(
            BugzillaConnector.getConnectorName(),
            query.getDisplayName(),
            true,
            autoRefresh
        );
    }

}
