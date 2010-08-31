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

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.OverlayLayout;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormLAF;
import org.netbeans.modules.form.FormLoaderSettings;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.RADComponentNode;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.VisualReplicator;
import org.netbeans.modules.form.fakepeer.FakePeerContainer;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Grid designer.
 *
 * @author Jan Stola
 */
public class GridDesigner extends JPanel implements Customizer {
    /** Color of the selection. */
    public static final Color SELECTION_COLOR = FormLoaderSettings.getInstance().getSelectionBorderColor();
    /** Image of the resizing handle. */
    public static final Image RESIZE_HANDLE = ImageUtilities.loadImageIcon("org/netbeans/modules/form/resources/resize_handle.png", false).getImage(); // NOI18N
    /** The "main" panel of the designer. */
    private JPanel innerPane;
    /** Glass pane of the designer. */
    private GlassPane glassPane;
    /** Replicator used to clone components for the designer. */
    private VisualReplicator replicator;
    /** Property sheet. */
    private PropertySheet sheet;
    /** Grid customizer (part of the panel on the left side). */
    private GridCustomizer customizer;

    /**
     * Sets the designer container.
     * 
     * @param metaContainer designer container.
     */
    private void setDesignedContainer(RADVisualContainer metaContainer) {
        FormModel formModel = metaContainer.getFormModel();
        setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane();
        innerPane = new JPanel() {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        innerPane.setLayout(new OverlayLayout(innerPane));
        glassPane = new GlassPane(this);
        glassPane.setOpaque(false);
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        JToolBar toolBar = new JToolBar();
        UndoRedoSupport support = UndoRedoSupport.getSupport(formModel);
        support.reset(glassPane);
        toolBar.add(support.getRedoAction());
        toolBar.add(support.getUndoAction());
        JToggleButton padButton = initPaddingButton();
        toolBar.add(Box.createRigidArea(new Dimension(10,10)));
        toolBar.add(padButton);
        rightPanel.add(toolBar, BorderLayout.PAGE_START);
        // Estimate of the size of the header
        Dimension headerDim = new JLabel("99").getPreferredSize(); // NOI18N
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportView(innerPane);
        scrollPane.setPreferredSize(new Dimension(500,500));
        int unitIncrement = headerDim.height;
        scrollPane.getVerticalScrollBar().setUnitIncrement(unitIncrement);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(unitIncrement);
        rightPanel.add(scrollPane);
        splitPane.setRightComponent(rightPanel);
        add(splitPane);
        replicator = new VisualReplicator(true, FormUtils.getViewConverters(), FormEditor.getBindingSupport(formModel));
        replicator.setTopMetaComponent(metaContainer);
        final Object[] bean = new Object[1];
        // Create the cloned components in the correct look and feel setup
        FormLAF.executeWithLookAndFeel(formModel, new Runnable() {
            @Override
            public void run() {
                bean[0] = (Container)replicator.createClone();
            } 
        });        
        Container container = metaContainer.getContainerDelegate(bean[0]);
        innerPane.removeAll();
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        GroupLayout layout = new GroupLayout(mainPanel);
        layout.setHonorsVisibility(false);
        GroupLayout.Group hGroup = layout.createSequentialGroup()
                .addGap(3*GlassPane.HEADER_GAP+headerDim.width)
                .addComponent(container, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        layout.setHorizontalGroup(hGroup);
        GroupLayout.Group vGroup = layout.createSequentialGroup()
                .addGap(2*GlassPane.HEADER_GAP+headerDim.height)
                .addComponent(container, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        layout.setVerticalGroup(vGroup);
        mainPanel.setLayout(layout);
        glassPane.setPanes(innerPane, container);
        configureGridManager();
        splitPane.setLeftComponent(initLeftColumn());
        innerPane.add(glassPane);
        FakePeerContainer fakePeerContainer = new FakePeerContainer();
        fakePeerContainer.setLayout(new BorderLayout());
        fakePeerContainer.setBackground(mainPanel.getBackground());
        fakePeerContainer.setFont(FakePeerSupport.getDefaultAWTFont());
        fakePeerContainer.add(mainPanel);
        innerPane.add(fakePeerContainer);
    }

    /**
     * Configures the appropriate {@code GridManager}.
     */
    private void configureGridManager() {
        RADVisualContainer metacont = (RADVisualContainer)replicator.getTopMetaComponent();
        Object bean = replicator.getClonedComponent(metacont);
        Container container = metacont.getContainerDelegate(bean);
        LayoutManager layout = container.getLayout();
        GridManager gridManager = null;
        if (layout instanceof GridBagLayout) {
            gridManager = new GridBagManager(replicator);
        }
        glassPane.setGridManager(gridManager);
        customizer = gridManager.getCustomizer(glassPane);
    }

    /**
     * Creates and initialized the components on the left side.
     * 
     * @return component that represents the left side.
     */
    private JComponent initLeftColumn() {
        sheet = new PropertySheet();
        sheet.setPreferredSize(new Dimension(300, 500));
        JPanel leftPanel;
        if (customizer == null) {
            leftPanel = sheet;
        } else {
            leftPanel = new JPanel();
            leftPanel.setLayout(new BorderLayout());
            leftPanel.add(sheet);
            leftPanel.add(customizer.getComponent(), BorderLayout.PAGE_START);
        }
        return leftPanel;
    }

    /**
     * Creates and initializes "pad empty rows/columns" button.
     * 
     * @return "pad empty rows/columns" button.
     */
    private JToggleButton initPaddingButton() {
        JToggleButton button = new JToggleButton();
        ImageIcon image = ImageUtilities.loadImageIcon("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/pad_empty.png", false); // NOI18N
        button.setIcon(image);
        button.setToolTipText(NbBundle.getMessage(GridDesigner.class, "GridDesigner.padEmptyCells")); // NOI18N
        button.setSelected(FormLoaderSettings.getInstance().getPadEmptyCells());
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean padEmptyCells = ((JToggleButton)e.getSource()).isSelected();
                FormLoaderSettings.getInstance().setPadEmptyCells(padEmptyCells);
                glassPane.updateLayout();
            }
        });
        return button;
    }

    /**
     * Implementation of {@code Customizer} interface (sets the object
     * to customize).
     * 
     * @param bean bean to customize.
     */
    @Override
    public void setObject(Object bean) {
        setDesignedContainer((RADVisualContainer)bean);
    }

    /** Selected meta-components. */
    private Set<RADVisualComponent> metaSelection = new HashSet<RADVisualComponent>();
    
    /**
     * Sets selection.
     * 
     * @param selection new selection.
     */
    public void setSelection(Set<Component> selection) {
        metaSelection.clear();
        RADVisualContainer metacont = (RADVisualContainer)replicator.getTopMetaComponent();
        for (RADVisualComponent metacomp : metacont.getSubComponents()) {
            Component comp = (Component)replicator.getClonedComponent(metacomp);
            if (selection.contains(comp)) {
                metaSelection.add(metacomp);
            }
        }
        updatePropertySheet();
        updateCustomizer();
    }

    /** Listener for property changes on selected nodes. */
    private PropertyChangeListener selectedNodeListener;

    /**
     * Returns listener for property changes on selected nodes.
     * 
     * @return listener for property changes on selected nodes.
     */
    private PropertyChangeListener getSelectedNodeListener() {
        if (selectedNodeListener == null) {
            selectedNodeListener = createSelectedNodeListener();
        }
        return selectedNodeListener;
    }

    /** Determines whether update of glassPane has been scheduled. */
    boolean updateScheduled = false;
    
    /**
     * Creates {@code selectedNodeListner}.
     * 
     * @return {@code selectedNodeListner}.
     */
    private PropertyChangeListener createSelectedNodeListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!glassPane.isUserActionInProgress()) {
                    if (!updateScheduled) {
                        // This method is called several times when a change
                        // is done to some property (in property sheet)
                        // when several components are selected.
                        // Avoiding partial refresh - waiting till
                        // other invocations/property modifications
                        // are finished.
                        updateScheduled = true;
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                updateScheduled = false;
                                glassPane.updateLayout();
                                updateCustomizer();
                            }
                        });
                    }
                }
            }
        };
    }

    /** Nodes selected in the property sheet. */
    private List<Node> selectedNodes = new ArrayList<Node>();
    
    /** Updates the property sheet according to the current selection. */
    private void updatePropertySheet() {
        List<Node> nodes = new ArrayList<Node>(metaSelection.size());
        for (RADVisualComponent metacomp : metaSelection) {
            RADComponentNode node = metacomp.getNodeReference();
            if (node == null) {
                // "metacomp" was just added and the node reference is not initialized yet
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        List<Node> nodes = new ArrayList<Node>(metaSelection.size());
                        for (RADVisualComponent metacomp : metaSelection) {
                            nodes.add(new LayoutConstraintsNode(metacomp.getNodeReference()));
                        }
                        setSelectedNodes(nodes);
                        sheet.setNodes(nodes.toArray(new Node[nodes.size()]));
                    }
                });
                return;
            } else {
                nodes.add(new LayoutConstraintsNode(node));
            }
        }
        setSelectedNodes(nodes);
        sheet.setNodes(nodes.toArray(new Node[nodes.size()]));
    }

    /**
     * Sets the selected nodes in the property sheet.
     * 
     * @param nodes new selection in the property sheet.
     */
    void setSelectedNodes(List<Node> nodes) {
        for (Node node : selectedNodes) {
            node.removePropertyChangeListener(getSelectedNodeListener());
        }
        this.selectedNodes = nodes;
        for (Node node : selectedNodes) {
            node.addPropertyChangeListener(getSelectedNodeListener());
        }
    }

    /**
     * Updates the grid customizer (part of the left side of the designer)
     * according to the current selection.
     */
    void updateCustomizer() {
        if (customizer != null) {
            DesignerContext context = glassPane.currentContext();
            customizer.setContext(context);
        }
    }

    /**
     * Node that shows just layout constraints of the given {@code RADComponentNode}.
     */
    static class LayoutConstraintsNode extends FilterNode {

        /**
         * Creates a new {@code LayoutConstraintsNode} based on the given node.
         * 
         * @param original the original node this node should be based on.
         */
        LayoutConstraintsNode(Node original) {
            super(original);
        }

        @Override
        public Node.PropertySet[] getPropertySets() {
            for (Node.PropertySet pSet : super.getPropertySets()) {
                String name = pSet.getName();
                if ("layout".equals(name)) { // NOI18N
                    final Node.PropertySet set = pSet;
                    String displayName = NbBundle.getMessage(GridDesigner.class, "GridDesigner.layoutConstraints"); // NOI18N
                    return new Node.PropertySet[] {new PropertySet(set.getName(), displayName, set.getShortDescription()) {
                        @Override
                        public Property<?>[] getProperties() {
                            return set.getProperties();
                        }
                    }};
                }
            }
            return new Node.PropertySet[0];
        }
    }

}
