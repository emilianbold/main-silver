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

package org.netbeans.modules.web.clientproject;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.clientproject.indirect.AntProjectHelper;
import org.netbeans.modules.web.clientproject.indirect.IndirectServices;
import org.netbeans.modules.web.clientproject.indirect.PropertyEvaluator;
import org.netbeans.modules.web.clientproject.indirect.SourcesHelper;
import org.openide.util.ChangeSupport;

/**
 *
 */
public class ClientSideProjectSources implements Sources, ChangeListener {
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final ClientSideProject project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;

    // @GuardedBy("this")
    private Sources delegate;


    public ClientSideProjectSources(ClientSideProject project, AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
    }
    
    @Override
    public synchronized SourceGroup[] getSourceGroups(String type) {
        assert Thread.holdsLock(this);
        if (delegate == null) {
            delegate = initSources();
            delegate.addChangeListener(this);
        }
        return delegate.getSourceGroups(type);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private Sources initSources() {
        SourcesHelper sourcesHelper = IndirectServices.getDefault().newSourcesHelper(project, helper, evaluator);
        sourcesHelper.sourceRoot("${" + ClientSideProjectConstants.PROJECT_SOURCE_FOLDER + "}") //NOI18N
                .displayName(org.openide.util.NbBundle.getMessage(ClientSideProjectSources.class, "SOURCES"))
                .add() // adding as principal root, continuing configuration
                .type(WebClientProjectConstants.SOURCES_TYPE_HTML5).add(); // adding as typed root
        sourcesHelper.sourceRoot("${" + ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER + "}") //NOI18N
                .displayName(org.openide.util.NbBundle.getMessage(ClientSideProjectSources.class, "SITE_ROOT"))
                .add() // adding as principal root, continuing configuration
                .type(WebClientProjectConstants.SOURCES_TYPE_HTML5_SITE_ROOT).add(); // adding as typed root
        sourcesHelper.sourceRoot("${" + ClientSideProjectConstants.PROJECT_TEST_FOLDER + "}") //NOI18N
                .displayName(org.openide.util.NbBundle.getMessage(ClientSideProjectSources.class, "UNIT_TESTS"))
                .add() // adding as principal root, continuing configuration
                .type(WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST).add(); // adding as typed root
        sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        return sourcesHelper.createSources();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

}
