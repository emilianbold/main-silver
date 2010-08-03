/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.form.layoutsupport.griddesigner.actions;

import java.awt.Component;
import java.util.Collections;
import javax.swing.JMenuItem;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.VisualReplicator;
import org.netbeans.modules.form.layoutsupport.griddesigner.DesignerContext;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridInfoProvider;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridManager;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridUtils;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteMenuView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;

/**
 * Action that allows to add a new component into the grid.
 *
 * @author Jan Stola
 */
public class AddAction extends AbstractGridAction {
    /**
     * Visual replicator responsible for the replication
     * of the container with the grid.
     */
    private VisualReplicator replicator;

    /**
     * Creates new {@code AddAction}.
     * 
     * @param replicator visual replicator responsible for
     * the replication of the container with the grid.
     */
    public AddAction(VisualReplicator replicator) {
        this.replicator = replicator;
    }

    @Override
    public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
        // This action provides just popup presenter. The actual
        // addition of the component is performed by AddComponentAction.
        return null;
    }
    
    @Override
    public JMenuItem getPopupPresenter(final GridActionPerformer performer) {
        PaletteMenuView menu = new PaletteMenuView(new NodeAcceptor() {
            @Override
            public boolean acceptNodes(Node[] nodes) {
                if (nodes.length != 1) {
                    return false;
                }
                PaletteItem paletteItem = nodes[0].getCookie(PaletteItem.class);
                if (paletteItem == null) {
                    return false;
                }
                if (PaletteItem.TYPE_CHOOSE_BEAN.equals(paletteItem.getExplicitComponentType())) {
                    NotifyDescriptor.InputLine desc = new NotifyDescriptor.InputLine(
                        FormUtils.getBundleString("MSG_Choose_Bean"), // NOI18N
                        FormUtils.getBundleString("TITLE_Choose_Bean")); // NOI18N
                    DialogDisplayer.getDefault().notify(desc);
                    if (NotifyDescriptor.OK_OPTION.equals(desc.getValue())) {
                        FormModel formModel = replicator.getTopMetaComponent().getFormModel();
                        String chooseBeanType = desc.getInputText();
                        paletteItem.setClassFromCurrentProject(chooseBeanType,
                            FormEditor.getFormDataObject(formModel).getPrimaryFile());
                    } else {
                        return false;
                    }
                }
                performer.performAction(new AddComponentAction(paletteItem));
                return true;
            }
        });
        menu.disableHack();
        return menu;
    }

    /**
     * Action that adds one component into the grid.
     */
    class AddComponentAction extends AbstractGridAction {
        /** Palette item that describes the component to add. */
        private PaletteItem pItem;

        /**
         * Creates new {@code AddComponentAction}.
         * 
         * @param pItem palette item that describes the component to add.
         */
        AddComponentAction(PaletteItem pItem) {
            this.pItem = pItem;
        }

        @Override
        public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
            GridInfoProvider info = gridManager.getGridInfo();
            int columns = info.getColumnCount();
            int rows = info.getRowCount();
            GridUtils.removePaddingComponents(gridManager);
            
            RADVisualContainer container = (RADVisualContainer)replicator.getTopMetaComponent();
            FormModel formModel = container.getFormModel();
            RADComponent metacomp = formModel.getComponentCreator().createComponent(
                    pItem.getComponentClassSource(), container, null);
            if (metacomp instanceof RADVisualComponent) {
                replicator.addComponent(metacomp);
                Component comp = (Component)replicator.getClonedComponent(metacomp);
                gridManager.setGridX(comp, context.getFocusedColumn());
                gridManager.setGridY(comp, context.getFocusedRow());
                context.setSelectedComponents(Collections.singleton(comp));
            }

            gridManager.updateLayout();
            GridUtils.revalidateGrid(gridManager);
            GridUtils.addPaddingComponents(gridManager, columns, rows);
            GridUtils.revalidateGrid(gridManager);
            return null;
        }

    }

}
