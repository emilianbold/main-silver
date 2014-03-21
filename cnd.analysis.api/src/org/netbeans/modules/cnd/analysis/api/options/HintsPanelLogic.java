/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.analysis.api.options;

import org.netbeans.modules.cnd.analysis.api.AbstractCustomizerProvider;
import org.netbeans.modules.cnd.analysis.api.CodeAudit;
import org.netbeans.modules.cnd.analysis.api.CodeAuditProvider;
import org.netbeans.modules.cnd.analysis.api.options.HintsPanel.ExtendedModel;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.modules.cnd.utils.ui.NamedOption;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


/** Contains all important listeners and logic of the Hints Panel.
 *
 * @author Petr Hrebejk
 */
class HintsPanelLogic implements MouseListener, KeyListener, TreeSelectionListener, ChangeListener, ActionListener {

    private static final Map<Severity,Integer> severity2index;
    
    private static final String DESCRIPTION_HEADER = 
        "<html><head>" + // NOI18N
        //"<link rel=\"StyleSheet\" href=\"nbdocs://org.netbeans.modules.usersguide/org/netbeans/modules/usersguide/ide.css\" type=\"text/css\">" // NOI18N
        //"<link rel=\"StyleSheet\" href=\"nbresloc:/org/netbeans/modules/java/hints/resources/ide.css\" type=\"text/css\">" + // NOI18N
        "</head><body>"; // NOI18N

    private static final String DESCRIPTION_FOOTER = "</body></html>"; // NOI18N
    
    
    static {
        severity2index = new HashMap<Severity, Integer>();
        severity2index.put( Severity.ERROR, 0  );
        severity2index.put( Severity.WARNING, 1  );
    }
    
    private JTree errorTree;
    private JLabel severityLabel;
    private JComboBox severityComboBox;
    private JPanel customizerPanel;
    private JEditorPane descriptionTextArea;
    private Preferences preferences;
    private final DefaultComboBoxModel defModel = new DefaultComboBoxModel();
    private final String defLabel = NbBundle.getMessage(HintsPanel.class, "CTL_ShowAs_Label"); //NOI18N
    
    HintsPanelLogic() {
        defModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_AsError")); //NOI18N
        defModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_AsWarning")); //NOI18N
    }
    
    void connect( JTree errorTree, JLabel severityLabel, JComboBox severityComboBox,
                  JPanel customizerPanel, JEditorPane descriptionTextArea,
                  Preferences preferences) {
        this.preferences = preferences;
        this.errorTree = errorTree;
        this.severityLabel = severityLabel;
        this.severityComboBox = severityComboBox;
        this.customizerPanel = customizerPanel;
        this.descriptionTextArea = descriptionTextArea;        
        
        valueChanged( null );
        
        errorTree.addKeyListener(this);
        errorTree.addMouseListener(this);
        errorTree.getSelectionModel().addTreeSelectionListener(this);
            
        severityComboBox.addActionListener(this);
    }
    
    void disconnect() {
        
        errorTree.removeKeyListener(this);
        errorTree.removeMouseListener(this);
        errorTree.getSelectionModel().removeTreeSelectionListener(this);
            
        severityComboBox.removeActionListener(this);
                
        componentsSetEnabled( false, ROOT );
    }
    
    synchronized void applyChanges() {
    }
    
    /** Were there any changes in the settings
     */
    boolean isChanged() {
        return false;
    }
    
    static Object getUserObject( TreePath path ) {
        if( path == null ) {
            return null;
        }
        Object node = path.getLastPathComponent();
        if (node instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode tn = (DefaultMutableTreeNode)node;
            return tn.getUserObject();
        }
        return null;
    }
    
    static Object getUserObject( DefaultMutableTreeNode node ) {
        return node.getUserObject();
    }
    
    boolean isSelected( DefaultMutableTreeNode node ) {
        for( int i = 0; i < node.getChildCount(); i++ ) {
            DefaultMutableTreeNode ch = (DefaultMutableTreeNode) node.getChildAt(i);
            Object o = ch.getUserObject();
            if ( o instanceof CodeAudit ) {
                CodeAudit hint = (CodeAudit)o;
                if ( hint.isEnabled()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // MouseListener implementation --------------------------------------------
    
    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        TreePath path = errorTree.getPathForLocation(e.getPoint().x, e.getPoint().y);
        if ( path != null ) {
            Rectangle r = errorTree.getPathBounds(path);
            if (r != null) {
                if ( r.contains(p)) {
                    int shift = p.x - r.x;
                    if (shift < r.height) {
                        toggle( path );
                    }
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}
    
    // KeyListener implementation ----------------------------------------------

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER ) {

            if ( e.getSource() instanceof JTree ) {
                JTree tree = (JTree) e.getSource();
                TreePath path = tree.getSelectionPath();

                if ( toggle( path )) {
                    e.consume();
                }
            }
        }
    }
    
    // TreeSelectionListener implementation ------------------------------------
    
    @Override
    public void valueChanged(TreeSelectionEvent ex) {            
        Object o = getUserObject(errorTree.getSelectionPath());
        
        if ( o instanceof CodeAudit ) {
            if (defModel != severityComboBox.getModel()) {
                severityComboBox.setModel(defModel);
                Mnemonics.setLocalizedText(severityLabel, defLabel);
            }

            CodeAudit hint = (CodeAudit) o;
            
            // Enable components
            componentsSetEnabled(true, AUDIT);
            
            // Set proper values to the componetnts

            if ("error".equals(hint.minimalSeverity())) { // NOI18N
                severityComboBox.setSelectedIndex(severity2index.get(Severity.ERROR));
            } else {
                severityComboBox.setSelectedIndex(severity2index.get(Severity.WARNING));
            }

            String description = hint.getDescription();
            descriptionTextArea.setText( description == null ? "" : wrapDescription(description)); // NOI18N
                                    
            // Optionally show the customizer
            customizerPanel.removeAll();
            JComponent c = null; //hint.getCustomizer()
            if ( c != null ) {               
                customizerPanel.add(c, BorderLayout.CENTER);
            }            
            customizerPanel.getParent().invalidate();
            ((JComponent)customizerPanel.getParent()).revalidate();
            customizerPanel.getParent().repaint();
        } else if ( o instanceof CodeAuditProvider ) {
            CodeAuditProvider hint = (CodeAuditProvider) o;
            String description = hint.getDescription();
            componentsSetEnabled(true, PROVIDER);
            descriptionTextArea.setText( description == null ? "" : wrapDescription(description)); // NOI18N
            // Optionally show the customizer
            customizerPanel.removeAll();
            JComponent c = null;
            if (o instanceof AbstractCustomizerProvider) {
               c = ((AbstractCustomizerProvider)o).createComponent(preferences);
            }
            if ( c != null ) {               
                customizerPanel.add(c, BorderLayout.CENTER);
            }            
            customizerPanel.getParent().invalidate();
            ((JComponent)customizerPanel.getParent()).revalidate();
            customizerPanel.getParent().repaint();
       } else if (o instanceof NamedOption) {
            NamedOption option = (NamedOption)o;
            if (defModel != severityComboBox.getModel()) {
                severityComboBox.setModel(defModel);
                Mnemonics.setLocalizedText(severityLabel, defLabel);
            }
            componentsSetEnabled(true, OPTION);
            String description = option.getDescription();
            descriptionTextArea.setText( description == null ? "" : wrapDescription(description)); // NOI18N
            // Optionally show the customizer
            customizerPanel.removeAll();
            JComponent c = null;
            if (o instanceof AbstractCustomizerProvider) {
               c = ((AbstractCustomizerProvider)o).createComponent(preferences);
            }
            if ( c != null ) {               
                customizerPanel.add(c, BorderLayout.CENTER);
            }            
            customizerPanel.getParent().invalidate();
            ((JComponent)customizerPanel.getParent()).revalidate();
            customizerPanel.getParent().repaint();
        } else {
            if (defModel != severityComboBox.getModel()) {
                severityComboBox.setModel(defModel);
                Mnemonics.setLocalizedText(severityLabel, defLabel);
            }
            componentsSetEnabled(false, ROOT);
        }
    }
    
    // ActionListener implementation -------------------------------------------
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if( errorTree.getSelectionPath() == null){
            return;
        }
        if (severityComboBox.equals(e.getSource())) {
            Object o = getUserObject(errorTree.getSelectionPath());
            if ( o instanceof CodeAudit ) {
                CodeAudit hint = (CodeAudit) o;
                if (index2severity(severityComboBox.getSelectedIndex()) == Severity.ERROR) {
                    hint.getPreferences().put(hint.getID(), "severity", "error"); //NOI18N
                } else {
                    hint.getPreferences().put(hint.getID(), "severity", "warning"); //NOI18N
                }
            }
        }
    }

   
    // ChangeListener implementation -------------------------------------------
    
    @Override
    public void stateChanged(ChangeEvent e) {
        // System.out.println("Task list box changed ");
    }
   
    // Private methods ---------------------------------------------------------

    private String wrapDescription( String description ) {
        return new StringBuffer( DESCRIPTION_HEADER ).append(description).append(DESCRIPTION_FOOTER).toString();        
    }
    
    private Severity index2severity( int index ) {
        for( Map.Entry<Severity,Integer> e : severity2index.entrySet()) {
            if ( e.getValue() == index ) {
                return e.getKey();
            }
        }
        throw new IllegalStateException( "Unknown severity"); //NOI18N
    }
       

    private boolean toggle(TreePath treePath) {
        if (treePath == null) {
            return false;
        }
        Object o = getUserObject(treePath);
        ExtendedModel model = (ExtendedModel) errorTree.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        if (o instanceof CodeAudit) {
            CodeAudit hint = (CodeAudit) o;
            if (hint.isEnabled()) {
                hint.getPreferences().put(hint.getID(), "enabled", "false"); //NOI18N
            } else {
                hint.getPreferences().put(hint.getID(), "enabled", "true"); //NOI18N
            }
            model.nodeChanged(node);
            errorTree.repaint();
        } else if (o instanceof CodeAuditProvider) {
            CodeAuditProvider provider = (CodeAuditProvider) o;
            boolean hasEnabled = false;
            boolean hasDisabled = false;
            for(int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) node.getChildAt(i);
                CodeAudit audit = (CodeAudit) childAt.getUserObject();
                if (audit.isEnabled()) {
                    hasEnabled = true;
                } else {
                    hasDisabled = true;
                }
            }
            if (hasEnabled) {
                if (hasDisabled) {
                    for(int i = 0; i < node.getChildCount(); i++) {
                        DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) node.getChildAt(i);
                        CodeAudit audit = (CodeAudit) childAt.getUserObject();
                        audit.getPreferences().put(audit.getID(), "enabled", "true"); //NOI18N
                    }
                } else {
                    for(int i = 0; i < node.getChildCount(); i++) {
                        DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) node.getChildAt(i);
                        CodeAudit audit = (CodeAudit) childAt.getUserObject();
                        audit.getPreferences().put(audit.getID(), "enabled", "false"); //NOI18N
                    }
                }
            } else {
                for(int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) node.getChildAt(i);
                    CodeAudit audit = (CodeAudit) childAt.getUserObject();
                    audit.getPreferences().put(audit.getID(), "enabled", "true"); //NOI18N
                }
            }
            model.nodeChanged(node);
            errorTree.repaint();
       } else if (o instanceof NamedOption) {
            NamedOption option = (NamedOption)o;
            if (NamedOption.getAccessor().getBoolean(option.getName())) {
                NamedOption.getAccessor().setBoolean(option.getName(), false);
            } else {
                NamedOption.getAccessor().setBoolean(option.getName(), true);
            }
            model.nodeChanged(node);
            errorTree.repaint();
       }
            
        return false;
    }
    
    private static final int ROOT = 0;
    private static final int PROVIDER = 1;
    private static final int AUDIT = 2;
    private static final int OPTION = 3;
    
    
    private void componentsSetEnabled( boolean enabled, int component ) {
        if ( !enabled ) {
            customizerPanel.removeAll();
            customizerPanel.getParent().invalidate();
            ((JComponent)customizerPanel.getParent()).revalidate();
            customizerPanel.getParent().repaint();
            severityComboBox.setSelectedIndex(0);
            descriptionTextArea.setText(""); // NOI18N
        }
        if (component == ROOT) {
            severityComboBox.setEnabled(false);
        } else if (component == AUDIT) {
            severityComboBox.setEnabled(enabled);
        } else  if (component == PROVIDER) {
            severityComboBox.setEnabled(false);
        } else  if (component == OPTION) {
            severityComboBox.setEnabled(false);
        }
        descriptionTextArea.setEnabled(enabled);
    }
    
    private static final class Confidence {
        private final int value;
        private final String displayName;
        private Confidence(int value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public int getValue() {
           return value; 
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }

}
