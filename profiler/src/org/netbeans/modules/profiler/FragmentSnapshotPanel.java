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

package org.netbeans.modules.profiler;

import org.netbeans.lib.profiler.results.ExportDataDumper;
import org.netbeans.lib.profiler.results.ResultsSnapshot;
import org.netbeans.lib.profiler.results.coderegion.CodeRegionResultsSnapshot;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.cpu.CodeRegionSnapshotPanel;
import org.openide.util.NbBundle;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.lib.profiler.ui.components.ProfilerToolbar;


/**
 * A display for snapshot of CPU profiling results
 *
 * @author Tomas Hurka
 * @author Ian Formanek
 */
@NbBundle.Messages({
    "FragmentSnapshotPanel_CallsTabName=Calls",
    "FragmentSnapshotPanel_InfoTabName=Info",
    "FragmentSnapshotPanel_CallsTabDescr=Code Fragment Calls - Execution data for code region",
    "FragmentSnapshotPanel_InfoTabDescr=Snapshot Information",
    "FragmentSnapshotPanel_PanelTitle=Code Fragment: {0}"
})
public class FragmentSnapshotPanel extends SnapshotPanel implements ChangeListener, ExportAction.ExportProvider {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private CodeRegionResultsSnapshot snapshot;
    private JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);
    private SaveSnapshotAction saveAction;
    private SnapshotInfoPanel infoPanel;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public FragmentSnapshotPanel(LoadedSnapshot ls) {
        this.snapshot = (CodeRegionResultsSnapshot) ls.getSnapshot();

        setLayout(new BorderLayout());

        infoPanel = new SnapshotInfoPanel(ls);

        CodeRegionSnapshotPanel crsPanel = new CodeRegionSnapshotPanel(snapshot);

        infoPanel.updateInfo();

        tabs.addTab(Bundle.FragmentSnapshotPanel_CallsTabName(), null, crsPanel, Bundle.FragmentSnapshotPanel_CallsTabDescr());
        tabs.addTab(Bundle.FragmentSnapshotPanel_InfoTabName(), null, infoPanel, Bundle.FragmentSnapshotPanel_InfoTabDescr());
        add(tabs, BorderLayout.CENTER);

        tabs.addChangeListener(this);

        ProfilerToolbar toolBar = ProfilerToolbar.create(false);
        toolBar.add(saveAction = new SaveSnapshotAction(ls));
        toolBar.add(new ExportAction(this,ls));

        add(toolBar.getComponent(), BorderLayout.NORTH);

        // support for traversing subtabs using Ctrl-Alt-PgDn/PgUp
        getActionMap().put("PreviousViewAction",
                           new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    moveToPreviousSubTab();
                }
            }); // NOI18N
        getActionMap().put("NextViewAction",
                           new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    moveToNextSubTab();
                }
            }); // NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public ResultsSnapshot getSnapshot() {
        return snapshot;
    }

    public void stateChanged(ChangeEvent e) {
        updateToolbar();
    }

    public void updateSavedState() {
        infoPanel.updateInfo();
        saveAction.updateState();
    }

    private void moveToNextSubTab() {
        tabs.setSelectedIndex(UIUtils.getNextSubTabIndex(tabs, tabs.getSelectedIndex()));
    }

    private void moveToPreviousSubTab() {
        tabs.setSelectedIndex(UIUtils.getPreviousSubTabIndex(tabs, tabs.getSelectedIndex()));
    }

    private void updateToolbar() {
        // update the toolbar if selected tab changed
    }

    public String getViewName() {
        return "snapshot-" + snapshot.getTimeTaken() + "-fragment"; // NOI18N
    }

    public boolean hasLoadedSnapshot() {
        return true;
    }

    public void exportData(int exportedFileType, ExportDataDumper eDD) {        
    }

    public boolean hasExportableView() {
        return false;
    }
}
