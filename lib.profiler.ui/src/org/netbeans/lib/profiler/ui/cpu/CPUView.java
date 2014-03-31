/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.lib.profiler.ui.cpu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.FlatProfileContainer;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.JExtendedSplitPane;
import org.netbeans.lib.profiler.utils.Wildcards;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class CPUView extends JPanel {
    
    private final ProfilerClient client;
    private final boolean showSourceSupported;
    
    private CPUTableView tableView;
    private CPUTreeTableView treeTableView;
    
    
    public CPUView(ProfilerClient client, Set<ClientUtils.SourceCodeSelection> selection,
                   boolean showSourceSupported) {
        this.client = client;
        this.showSourceSupported = showSourceSupported;
        
        initUI(selection);
    }
    
    
    public void setView(boolean callTree, boolean hotSpots) {
        treeTableView.setVisible(callTree);
        tableView.setVisible(hotSpots);
    }
    
    
    public void refreshData() throws ClientUtils.TargetAppOrVMTerminated {
        client.forceObtainedResultsDump(true);
        
        try {
            CPUResultsSnapshot snapshotData =
                    client.getStatus().getInstrMethodClasses() == null ?
                    null : client.getCPUProfilingResultsSnapshot(false);

            if (snapshotData != null) {
                FlatProfileContainer flatData = snapshotData.getFlatProfile(
                        -1, CPUResultsSnapshot.METHOD_LEVEL_VIEW);
                
                Map<Integer, ClientUtils.SourceCodeSelection> idMap = new HashMap();
                for (int i = 0; i < flatData.getNRows(); i++) // TODO: getNRows is filtered, may not work for tree data!
                    idMap.put(flatData.getMethodIdAtRow(i), flatData.getSourceCodeSelectionAtRow(i));

                treeTableView.setData(snapshotData, idMap);
                tableView.setData(flatData, idMap);

            }
        } catch (CPUResultsSnapshot.NoDataAvailableException e) {
        } catch (Throwable t) {
            if (t instanceof ClientUtils.TargetAppOrVMTerminated) {
                throw ((ClientUtils.TargetAppOrVMTerminated)t);
            } else {
                System.err.println(">>> " + t.getMessage());
                t.printStackTrace(System.err);
            }
        }
    }
    
    public void resetData() {
        treeTableView.resetData();
        tableView.resetData();
    }
    
    
    public void refreshSelection() {
        treeTableView.refreshSelection();
        tableView.refreshSelection();
    }
    
    
    public abstract void showSource(ClientUtils.SourceCodeSelection value);
    
    public abstract void profileSingle(ClientUtils.SourceCodeSelection value);
    
    public abstract void selectForProfiling(ClientUtils.SourceCodeSelection[] value);
    
    public void popupShowing() {};
    
    public void popupHidden() {};
    
    
    private void profileMethod(ClientUtils.SourceCodeSelection value) {
        profileSingle(value);
    }
    
    private void profileClass(ClientUtils.SourceCodeSelection value) {
        profileSingle(new ClientUtils.SourceCodeSelection(
                value.getClassName(), Wildcards.ALLWILDCARD, null));
    }
    
    private void selectMethod(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(new ClientUtils.SourceCodeSelection[] { value });
    }
    
    
    private void initUI(Set<ClientUtils.SourceCodeSelection> selection) {
        setLayout(new BorderLayout(0, 0));
        
        treeTableView = new CPUTreeTableView(client, selection) {
            protected void performDefaultAction(ClientUtils.SourceCodeSelection value) {
                if (showSourceSupported) showSource(value);
            }
            protected void populatePopup(JPopupMenu popup, ClientUtils.SourceCodeSelection value) {
                CPUView.this.populatePopup(popup, value);
            }
            protected void popupShowing() { CPUView.this.popupShowing(); }
            protected void popupHidden()  { CPUView.this.popupHidden(); }
        };
        
        tableView = new CPUTableView(client, selection) {
            protected void performDefaultAction(ClientUtils.SourceCodeSelection value) {
                if (showSourceSupported) showSource(value);
            }
            protected void populatePopup(JPopupMenu popup, ClientUtils.SourceCodeSelection value) {
                CPUView.this.populatePopup(popup, value);
            }
            protected void popupShowing() { CPUView.this.popupShowing(); }
            protected void popupHidden()  { CPUView.this.popupHidden(); }
        };
        
        JSplitPane split = new JExtendedSplitPane(JSplitPane.VERTICAL_SPLIT) {
            {
                setBorder(null);
                setDividerSize(5);

                if (getUI() instanceof BasicSplitPaneUI) {
                    BasicSplitPaneDivider divider = ((BasicSplitPaneUI)getUI()).getDivider();
                    if (divider != null) {
                        Color c = UIUtils.isNimbus() ? UIUtils.getDisabledLineColor() :
                                new JSeparator().getForeground();
                        divider.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, c));
                    }
                }
            }
        };
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setTopComponent(treeTableView);
        split.setBottomComponent(tableView);
        
        add(split, BorderLayout.CENTER);
        
//        // TODO: read last state?
//        setView(true, false);
    }
    
    private void populatePopup(JPopupMenu popup, final ClientUtils.SourceCodeSelection value) {
        if (showSourceSupported) {
            popup.add(new JMenuItem("Go to Source") {
                { setEnabled(value != null); setFont(getFont().deriveFont(Font.BOLD)); }
                protected void fireActionPerformed(ActionEvent e) { showSource(value); }
            });
            popup.addSeparator();
        }
        
        popup.add(new JMenuItem("Profile this Method") {
            { setEnabled(value != null); }
            protected void fireActionPerformed(ActionEvent e) { profileMethod(value); }
        });
        
        popup.add(new JMenuItem("Profile this Class") {
            { setEnabled(value != null); }
            protected void fireActionPerformed(ActionEvent e) { profileClass(value); }
        });
        
        popup.addSeparator();
        popup.add(new JMenuItem("Select for Profiling") {
            { setEnabled(value != null); }
            protected void fireActionPerformed(ActionEvent e) { selectMethod(value); }
        });
    }
    
}
