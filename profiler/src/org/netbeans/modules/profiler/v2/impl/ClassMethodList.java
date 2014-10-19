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

package org.netbeans.modules.profiler.v2.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.utils.formatting.MethodNameFormatter;
import org.netbeans.lib.profiler.utils.Wildcards;
import org.netbeans.lib.profiler.utils.formatting.DefaultMethodNameFormatter;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourceMethodInfo;
import org.netbeans.modules.profiler.v2.ProfilerSession;
import org.netbeans.modules.profiler.v2.ui.SmallButton;

/**
 *
 * @author Jiri Sedlacek
 */
public final class ClassMethodList {
    
    public static void showClasses(ProfilerSession session, Set<ClientUtils.SourceCodeSelection> selection, Component invoker) {
        UI ui = UI.forClasses(session, selection, invoker);
        ui.show(invoker, -8, invoker.getHeight() + 1);
    }
    
    public static void showMethods(ProfilerSession session, Set<ClientUtils.SourceCodeSelection> selection, Component invoker) {
        UI ui = UI.forMethods(session, selection, invoker);
        ui.show(invoker, -8, invoker.getHeight() + 1);
    }
    
    
    private ClassMethodList() {}
    
    private static class UI extends JPopupMenu {
        
        private boolean addingEntry = false;
        
        static UI forClasses(ProfilerSession session, Set<ClientUtils.SourceCodeSelection> selection, Component invoker) {
            return new UI(session, selection, false);
        }
        
        static UI forMethods(ProfilerSession session, Set<ClientUtils.SourceCodeSelection> selection, Component invoker) {
            return new UI(session, selection, true);
        }
        
        
        private UI(final ProfilerSession session, final Set<ClientUtils.SourceCodeSelection> selection, final boolean methods) {
            
            JPanel content = new JPanel(new BorderLayout(8, 8));
            content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            
            class XListModel extends AbstractListModel<ClientUtils.SourceCodeSelection> {
                public int getSize() {
                    return selection.size();
                }
                public ClientUtils.SourceCodeSelection getElementAt(int index) {
                    return (ClientUtils.SourceCodeSelection)new ArrayList(selection).get(index);
                }
                public void refresh() {
                    super.fireContentsChanged(this, 0, getSize());
                }
            }
            
            final XListModel xmodel = new XListModel();
            final JList list = new JList(xmodel) {
                public Dimension getPreferredScrollableViewportSize() {
                    Dimension dim = super.getPreferredScrollableViewportSize();
                    dim.width = 350;
                    return dim;
                }
            };
            int format = methods ? DefaultMethodNameFormatter.VERBOSITY_CLASSMETHOD :
                                   DefaultMethodNameFormatter.VERBOSITY_CLASS;
            final MethodNameFormatter formatter = new DefaultMethodNameFormatter(format);
            list.setCellRenderer(new DefaultListCellRenderer() {
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    String name = formatter.formatMethodName((ClientUtils.SourceCodeSelection)value).toFormatted();
                    return super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
                }
            });
            
            final JButton addB = new SmallButton("+") {
                protected void fireActionPerformed(ActionEvent e) {
                    final Component invoker = getInvoker();
                    addingEntry = true;
                    ClientUtils.SourceCodeSelection sel = null;
                    
                    if (methods) {
                        SourceMethodInfo mtd = ClassMethodSelector.selectMethod(session);
                        
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                addingEntry = false;
                                UI.this.setVisible(false);
                                invoker.getParent().repaint(); // XXX
                                if (!invoker.isShowing()) System.err.println(">>> INVALID INVOKER: " + invoker.getClass().getName() + ", " + invoker);
                                UI.this.show(invoker, -8, invoker.getHeight() + 1);
                            }
                        });
                        
                        if (mtd != null)
                            sel = new ClientUtils.SourceCodeSelection(mtd.getClassName(),
                                                                      mtd.getName(), mtd.getSignature());
                    } else {
                        SourceClassInfo cls = ClassMethodSelector.selectClass(session);
                        
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                addingEntry = false;
                                UI.this.setVisible(false);
                                invoker.getParent().repaint(); // XXX
                                if (!invoker.isShowing()) System.err.println(">>> INVALID INVOKER: " + invoker.getClass().getName() + ", " + invoker);
                                UI.this.show(invoker, -8, invoker.getHeight() + 1);
                            }
                        });
                        
                        if (cls != null)
                            sel = new ClientUtils.SourceCodeSelection(cls.getQualifiedName(),
                                                                      Wildcards.ALLWILDCARD, null);
                    }
                    
                    if (sel != null) {
                        selection.add(sel);
                        xmodel.refresh();
                    }
                }   
            };
            
            final JButton removeB = new SmallButton("-") {
                protected void fireActionPerformed(ActionEvent e) {
                    final Component invoker = getInvoker();
                    
                    List<ClientUtils.SourceCodeSelection> sel = list.getSelectedValuesList();
                    for (ClientUtils.SourceCodeSelection s : sel) selection.remove(s);
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            invoker.getParent().repaint(); // XXX
                        }
                    });
                    
                    xmodel.refresh();
                    list.clearSelection();
                    setEnabled(false);
                }
            };
            removeB.setEnabled(false);
            
            
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            list.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) return;
                    removeB.setEnabled(list.getSelectedValue() != null);
                }
            });
            
            content.add(new JScrollPane(list), BorderLayout.CENTER);
            
            JPanel buttons = new JPanel(new GridLayout(5, 1));
            buttons.add(addB);
            buttons.add(removeB);
            content.add(buttons, BorderLayout.EAST);
            
            add(content);
            
        }
        
        public void setVisible(boolean b) {
            if (!addingEntry) super.setVisible(b);
        }
        
    }
    
}
