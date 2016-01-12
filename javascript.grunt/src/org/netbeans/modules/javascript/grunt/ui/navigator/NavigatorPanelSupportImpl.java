/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.grunt.ui.navigator;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.grunt.GruntBuildTool;
import org.netbeans.modules.javascript.grunt.GruntBuildToolSupport;
import org.netbeans.modules.web.clientproject.api.build.BuildTools;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

@NbBundle.Messages({
    "NavigatorPanelSupportImpl.name=Grunt Tasks",
    "NavigatorPanelSupportImpl.hint=Displays tasks in the current Gruntfile.js script.",
})
public class NavigatorPanelSupportImpl implements BuildTools.NavigatorPanelSupport, ChangeListener {

    private static final RequestProcessor RP = new RequestProcessor(NavigatorPanelSupportImpl.class);

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final RequestProcessor.Task task;


    public NavigatorPanelSupportImpl() {
        task = RP.create(new Runnable() {
            @Override
            public void run() {
                fireChange();
            }
        });
    }

    @Override
    public String getDisplayName() {
        return Bundle.NavigatorPanelSupportImpl_name();
    }

    @Override
    public String getDisplayHint() {
        return Bundle.NavigatorPanelSupportImpl_hint();
    }

    @Override
    public BuildTools.BuildToolSupport getBuildToolSupport(FileObject buildFile) {
        Project project = FileOwnerQuery.getOwner(buildFile);
        if (project == null) {
            return null;
        }
        if (GruntBuildTool.inProject(project) == null) {
            return null;
        }
        GruntBuildToolSupport support = new GruntBuildToolSupport(project, buildFile);
        support.addChangeListener(WeakListeners.change(this, support));
        return support;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        task.schedule(2000);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    //~ Factories

    @NavigatorPanel.Registration(mimeType = "text/grunt+javascript", displayName = "#NavigatorPanelSupportImpl.name", position = 100)
    public static NavigatorPanel createNavigatorPanel() {
        return BuildTools.getDefault().createNavigatorPanel(new NavigatorPanelSupportImpl());
    }

}
