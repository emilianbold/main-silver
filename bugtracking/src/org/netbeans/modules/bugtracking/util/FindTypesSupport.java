/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
//import org.netbeans.api.jumpto.type.TypeBrowser;
import org.netbeans.modules.bugtracking.BugtrackingManager;
//import org.netbeans.modules.search.FindInFilesAction;

/**
 *
 * @author Tomas Stupka
 */
final class FindTypesSupport implements MouseMotionListener, MouseListener {
    
    private static Pattern JAVA_CLASS_NAME_PATTERN = Pattern.compile("([a-zA-Z_\\$][a-zA-Z\\d_\\$\\.]*)*([A-Z_\\$][a-zA-Z\\d_\\$]*)"); 
    private String HIGHLIGHTS_PROPERTY = "highlights.property";
    private String PREV_HIGHLIGHT_PROPERTY = "prev.highlights.property";
            
    private static FindTypesSupport instance;
    private Style defStyle;
    private final PopupMenu popupMenu;

    private FindTypesSupport() {
        defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        popupMenu = new PopupMenu();
    }
    
    public static FindTypesSupport getInstance() {
        if (instance == null) {
            instance = new FindTypesSupport();            
        }
        return instance;
    }

    private Highlight getHighlight(JTextPane pane, int offset) {
        List<Highlight> highlights = (List<Highlight>) pane.getClientProperty(HIGHLIGHTS_PROPERTY);
        Highlight h = null;
        for (int i = 0; i < highlights.size(); i++) {
            h = highlights.get(i);
            if(h.startOffset <= offset && h.endOffset >= offset) {
                break;
            } 
            h = null;
        }
        return h;
    }
    
    private class Highlight {
        int startOffset;
        int endOffset;
        public Highlight(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Highlight other = (Highlight) obj;
            if (this.startOffset != other.startOffset) {
                return false;
            }
            if (this.endOffset != other.endOffset) {
                return false;
            }
            return true;
        }
        @Override
        public int hashCode() {
            int hash = 3;
            hash = 71 * hash + this.startOffset;
            hash = 71 * hash + this.endOffset;
            return hash;
        }
    }
    
    public void register(JTextPane pane) {
        StyledDocument doc = pane.getStyledDocument();
        Style hlStyle = doc.addStyle("regularBlue-findtype", defStyle); 
        hlStyle.addAttribute(HyperlinkSupport.TYPE_ATTRIBUTE, new TypeLink());
        StyleConstants.setForeground(hlStyle, Color.BLUE);
        StyleConstants.setUnderline(hlStyle, true);     
        
        List<Integer> l = getTypeName(pane.getText());
        List<Highlight> highlights = new ArrayList<Highlight>(l.size());
        for (int i = 0; i < l.size(); i++) {
            highlights.add(new Highlight(l.get(i), l.get(++i)));
        }
        pane.putClientProperty(HIGHLIGHTS_PROPERTY, highlights);
        
        pane.addMouseMotionListener(this);
        pane.addMouseListener(this);
    }

    @Override
    public void mouseDragged(MouseEvent e) { }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        
        JTextPane pane = (JTextPane)e.getSource();
        StyledDocument doc = pane.getStyledDocument();
        
        int offset = pane.viewToModel(e.getPoint());
        Element elem = doc.getCharacterElement(offset);
        
        AttributeSet as = elem.getAttributes();
        if (StyleConstants.isUnderline(as)) {
            // do not underline whats already underlined
            return;
        }
        Highlight h = getHighlight(pane, offset);
        Highlight prevHighlight = (Highlight) pane.getClientProperty(PREV_HIGHLIGHT_PROPERTY);
//        if(h != null && h.equals(prevHighlight)) {
//            return; // nothing to do
//        } else 
        if(prevHighlight != null) {
            doc.setCharacterAttributes(prevHighlight.startOffset, prevHighlight.endOffset - prevHighlight.startOffset, defStyle, true);
            pane.putClientProperty(PREV_HIGHLIGHT_PROPERTY, null);
        }
        
        if(h != null && offset < elem.getEndOffset() - 1) {
            Style hlStyle = doc.getStyle("regularBlue-findtype");
            doc.setCharacterAttributes(h.startOffset, h.endOffset - h.startOffset, hlStyle, true);
            pane.putClientProperty(PREV_HIGHLIGHT_PROPERTY, h);
        } 
        
    }
    
    static List<Integer> getTypeName(String txt) {
        LinkedList<Integer> result = new LinkedList<Integer>();
        if ( txt == null) {
            return Collections.emptyList();
        }

        List<Integer> lineBreaks = new ArrayList<Integer>();
        int pos = -1;
        while( (pos = txt.indexOf("\n", pos + 1)) > -1) {
           lineBreaks.add(pos);
        }
        Matcher m  = JAVA_CLASS_NAME_PATTERN.matcher(txt);
        int last = -1;       
        int start = -1;
        while( m.find() ) {
           start = m.start(); 
           if(start != -1) {
               last = start + (m.group(1) != null ? m.group(1).length() : 0) + m.group(2).length(); 
               result.add(start);
               result.add(last);
           }
        }
        return result;
    }

    private Element element(MouseEvent e) {
        JTextPane pane = (JTextPane)e.getSource();
        StyledDocument doc = pane.getStyledDocument();
        return doc.getCharacterElement(pane.viewToModel(e.getPoint()));
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            if (SwingUtilities.isRightMouseButton(e)) {
                popupMenu.clickPoint.setLocation(e.getPoint());
                popupMenu.pane = (JTextPane)e.getSource();
                popupMenu.show((JTextPane)e.getSource(), e.getPoint().x, e.getPoint().y);
            }
            else if (SwingUtilities.isLeftMouseButton(e)) {
                Element elem = element(e);
                AttributeSet as = elem.getAttributes();
                TypeLink action = (TypeLink) as.getAttribute(HyperlinkSupport.TYPE_ATTRIBUTE);
                if (action != null) {
                    try {
                        String name = elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset());
                        int idx = name.lastIndexOf(".");
                        if(idx > -1 && name.length() > idx) {
                            name = name.substring(idx + 1);
                        }
                        action.jumpTo(name);
                    } catch(Exception ex) {
                        BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch(Exception ex) {
            BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }
    
    private class TypeLink {
        public void jumpTo(String resource) {
//            TypeBrowser.browse("Find type", resource, null);
        }
    }
    
    private class PopupMenu extends JPopupMenu {

        /*
         * Holds the location of where the user invoked the pop-up menu.
         * It must be remembered before calling super.show(...) because
         * the method show() may change the location of the pop-up menu,
         * so the original location might not be available.
         */
        private final Point clickPoint = new Point();
        private JTextPane pane;
        

        @Override
        public void show(Component invoker, int x, int y) {
            clickPoint.setLocation(x, y);
            super.show(invoker, x, y);
        }        
        
        @Override
        public void setVisible(boolean b) {
            if (b) {
                StyledDocument doc = pane.getStyledDocument();
                int offset = pane.viewToModel(clickPoint);
                Element elem = doc.getCharacterElement(offset);
                AttributeSet as = elem.getAttributes();
                final TypeLink link = (TypeLink) as.getAttribute(HyperlinkSupport.TYPE_ATTRIBUTE);
                if (link != null) {
                    try {
                        String name = elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset());
                        int idx = name.lastIndexOf(".");
                        final String shortname = idx > -1 && name.length() > idx ? name.substring(idx + 1) : name;
                        add(new JMenuItem(new AbstractAction("GoTo type '" + shortname + "'") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                link.jumpTo(shortname);
                            }
                        }));
                        if(name.length() > shortname.length()) {
                            final String path = name.replace(".", "/") + ".java";
                            add(new JMenuItem(new AbstractAction("Open '" + path + "'") { // XXX + ".java" ???
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    StackTraceSupport.open(path, -1);
                                }
                            }));
                        }
//                        add(new JMenuItem(new AbstractAction("Find in projects") {
//                            @Override
//                            public void actionPerformed(ActionEvent e) {
//                                FindInFilesAction a = (FindInFilesAction) FileUtil.getConfigObject("Actions/Edit/org-netbeans-modules-search-FindInFilesAction.instance", Action.class);
//                                if(a instanceof ContextAwareAction) {
////                                    a = ((ContextAwareAction)a).createContextAwareInstance(Lookups.singleton(ctx));
//                                }            
//                                a.performAction();
//                            }
//                        }));
                    } catch(Exception ex) {
                        BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
                    }
                    super.setVisible(true);
                } else {
                    super.setVisible(false);
                }
            } else {
                super.setVisible(false);
                removeAll();
            }
        }        
    }    

    
}
