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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.accessibility.Accessible;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.SideBarFactory;
import org.netbeans.modules.html.editor.lib.api.elements.NodeUtils;
import org.netbeans.modules.html.editor.HtmlCaretAwareSourceTask.Source;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.TreePath;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * HTML tag navigation sidebar
 *
 * @author marekfukala
 */
public class NavigationSideBar extends JPanel implements Accessible {

    private JTextComponent component;
    private volatile AttributeSet attribs;
    private Lookup.Result<? extends FontColorSettings> fcsLookupResult;
    private final Document doc;
    private final LookupListener fcsTracker = new LookupListener() {

        @Override
        public void resultChanged(LookupEvent ev) {
            attribs = null;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    NavigationSideBar.this.repaint();
                }
            });
        }
    };
    private boolean enabled = true;
    List<Node> nesting = new ArrayList<Node>(5);

    public NavigationSideBar() {
        doc = null;
    }

    public NavigationSideBar(JTextComponent component) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 14, 0));
        setBorder(new EmptyBorder(2, 2, 2, 2));

        this.component = component;
        this.doc = component.getDocument();

        Source source = HtmlCaretAwareSourceTask.forDocument(doc);
        source.addChangeListener(new HtmlCaretAwareSourceTask.SourceListener() {

            @Override
            public void parsed(Result info, SchedulerEvent event) {
                NavigationSideBar.this.change(info, event);
            }
        });

        updatePreferredSize();

    }
    
    private void change(Result info, SchedulerEvent event) {
        if (event == null) {
            return ;
        }

        int caretPosition = ((CursorMovedSchedulerEvent) event).getCaretOffset();
        HtmlParserResult result = (HtmlParserResult) info;
        Collection<Node> allRoots = new LinkedList<Node>();
        allRoots.addAll(result.roots().values());
        allRoots.add(result.rootOfUndeclaredTagsParseTree());

        List<Node> nodesInPath = new ArrayList<Node>();
        int astOffset = info.getSnapshot().getEmbeddedOffset(caretPosition);
        for (Node root : allRoots) {
            Node leaf = NodeUtils.findNode(root, astOffset, false, false);
            if (leaf != null) {
                //add all nodes in the leaf's path to the root
                TreePath treePath = new TreePath(null, leaf);
                for (Node node : treePath.path()) { //really brilliant wording!!!!
                    if (node.type() == ElementType.OPEN_TAG && !NodeUtils.isVirtualNode(node)) {
                        nodesInPath.add(node);
                    }
                }
            }
        }
        //sort by start offsets
        Collections.sort(nodesInPath, new Comparator<Node>() {

            @Override
            public int compare(Node o1, Node o2) {
                return o1.from() - o2.from();
            }
        });

        updateNestingInfo(info, nodesInPath);

        if (testAccess != null) {
            testAccess.updated(nodesInPath);
        }

    }

    private void updateNestingInfo(final Result tsource, List<Node> sortedPath) {
        nesting = sortedPath;

        //update UI
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                updatePanelUI(tsource);
            }
        });

    }

    private void updatePanelUI(final Result tsource) {
        removeAll();

        for (final Node node : nesting) {
            final JLabel label = new javax.swing.JLabel();
            label.setForeground(Color.BLACK);
            label.setFont(new Font("Monospaced", Font.PLAIN, (int) (getColoring().getFont().getSize() * .9))); // NOI18N
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            label.setText(getDrawText(node.nodeId().toString()));
            label.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    label.setForeground(Color.BLUE);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    label.setForeground(Color.BLACK);
                }

                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int documentOffset = tsource.getSnapshot().getOriginalOffset(node.from());
                    component.getCaret().setDot(documentOffset);
                }
            });

            add(label);
        }

        revalidate();
        repaint();
    }

    private String getDrawText(String tagName) {
        return tagName;
    }

    private void updatePreferredSize() {
        if (enabled) {
            setPreferredSize(new Dimension(component.getWidth(), getColoring().getFont().getSize() + 4));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        } else {
            setPreferredSize(new Dimension(0, 0));
            setMaximumSize(new Dimension(0, 0));
        }
        revalidate();
    }

    private Coloring getColoring() {
        if (attribs == null) {
            if (fcsLookupResult == null) {
                fcsLookupResult = MimeLookup.getLookup(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(component)).lookupResult(FontColorSettings.class);
                fcsLookupResult.addLookupListener(WeakListeners.create(LookupListener.class, fcsTracker, fcsLookupResult));
            }

            FontColorSettings fcs = fcsLookupResult.allInstances().iterator().next();
//            AttributeSet attr = fcs.getFontColors(FontColorNames.CODE_FOLDING_BAR_COLORING);
//            if (attr == null) {
//                attr = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
//            } else {
            attribs = AttributesUtilities.createComposite(attribs, fcs.getFontColors(FontColorNames.DEFAULT_COLORING));
//            }
//            attribs = attr;
        }
        return Coloring.fromAttributeSet(attribs);
    }

    public static final class NavigationSideBarFactory implements SideBarFactory {

        @Override
        public JComponent createSideBar(JTextComponent target) {
            return new NavigationSideBar(target);
        }
    }

    //for unit tests only!!!
    void regicterTestAccess(TestAccess ta) {
        this.testAccess = ta;
    }

    static interface TestAccess {

        public void updated(List<Node> path);
    }
    private TestAccess testAccess;
}
