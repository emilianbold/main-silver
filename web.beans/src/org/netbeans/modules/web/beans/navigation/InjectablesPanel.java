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
 *
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

package org.netbeans.modules.web.beans.navigation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Based on org.netbeans.modules.java.navigation.JavaHierarchyPanel
 *
 * @author ads
 *
 */
public class InjectablesPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = -1643692494954311020L;

    public static final Icon FQN_ICON = ImageUtilities.loadImageIcon(
            "org/netbeans/modules/java/navigation/resources/fqn.gif", false); // NOI18N

    public static final Icon EXPAND_ALL_ICON = ImageUtilities.loadImageIcon(
            "org/netbeans/modules/java/navigation/resources/expandall.gif", false); // NOI18N
    
    private static final String NON_BINDING_MEMBER_ANNOTATION =
                                            "javax.enterprise.inject.NonBinding";    // NOI18N

    private static TreeModel pleaseWaitTreeModel;
    static
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        root.add(new DefaultMutableTreeNode(NbBundle.getMessage(
                InjectablesPanel.class, "LBL_WaitNode"))); // NOI18N
        pleaseWaitTreeModel = new DefaultTreeModel(root);
    }
    
    public InjectablesPanel(final ElementHandle<? extends Element> var, 
            MetadataModel<WebBeansModel> metaModel, final WebBeansModel model,
            JavaHierarchyModel treeModel ) 
    {
        myJavaHierarchyModel = treeModel;
        initComponents();

        // disable filtering for now: list of injectables will be always short
        mySeparator.setVisible(false);
        myFilterLabel.setVisible(false);
        myFilterTextField.setVisible(false);
        myCaseSensitiveFilterCheckBox.setVisible(false);
        
        myModel = metaModel;
        myDocPane = new DocumentationScrollPane( true );
        mySplitPane.setRightComponent( myDocPane );
        mySplitPane.setDividerLocation(
                WebBeansNavigationOptions.getHierarchyDividerLocation());
        
        ToolTipManager.sharedInstance().registerComponent(myJavaHierarchyTree);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        myCaseSensitiveFilterCheckBox.setSelected(
                WebBeansNavigationOptions.isCaseSensitive());
        myShowFQNToggleButton.setSelected(
                WebBeansNavigationOptions.isShowFQN());

        if ( model == null ){
            try {
                metaModel.runReadAction( new MetadataModelAction<WebBeansModel, Void>() {
                    @Override
                    public Void run( WebBeansModel model ) throws Exception {
                        initCDIContext( var, model  );
                        return null;
                    }
                });
            }
            catch (MetadataModelException e) {
                Logger.getLogger( InjectablesPanel.class.getName()).
                    log( Level.WARNING, e.getMessage(), e);
            }
            catch (IOException e) {
                Logger.getLogger( InjectablesPanel.class.getName()).
                    log( Level.WARNING, e.getMessage(), e);
            }
        }
        else {
            initCDIContext( var, model );
        }
        

        myJavaHierarchyTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        myJavaHierarchyTree.setRootVisible(false);
        myJavaHierarchyTree.setShowsRootHandles(true);
        myJavaHierarchyTree.setCellRenderer(new JavaTreeCellRenderer());

        myJavaHierarchyTree.setModel(myJavaHierarchyModel);

        registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                close();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        initListeners();
    }

    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                reload();
                myFilterTextField.requestFocusInWindow();
            }           
        });
    }

    public void removeNotify() {
        WebBeansNavigationOptions.setHierarchyDividerLocation(
                mySplitPane.getDividerLocation());
        myDocPane.setData( null );
        super.removeNotify();
    }
    
    // Hack to allow showing of Help window when F1 or HELP key is pressed.
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, 
            boolean pressed) 
    {
        if (e.getKeyCode() == KeyEvent.VK_F1 || e.getKeyCode() == KeyEvent.VK_HELP)  {
            JComponent rootPane = SwingUtilities.getRootPane(this);
            if (rootPane != null) {
                rootPane.putClientProperty(ResizablePopup.HELP_COOKIE, Boolean.TRUE); 
            }
        }
        return super.processKeyBinding(ks, e, condition, pressed);
    }
    
    protected JLabel getTypeLabel(){
        return myTypeLbl;
    }
    
    protected JLabel getInjectionQualifiersLabel(){
        return myBindingLbl;
    }
    
    protected void setInjectableType( TypeMirror typeMirror,
            CompilationController controller )
    {
        if ( typeMirror.getKind().isPrimitive()){
            myShortTypeName.append( typeMirror.getKind().toString().toLowerCase());
            myFqnTypeName.append(  myShortTypeName);
            return;
        }
        if ( typeMirror.getKind() == TypeKind.ARRAY ){
            setInjectableArrayType( typeMirror , controller );
            myShortTypeName = myShortTypeName.append("[]");     // NOI18N
            myFqnTypeName = myFqnTypeName.append("[]");         // NOI18N
        }
        Element element = controller.getTypes().asElement( typeMirror );
        if ( element != null ){
            myFqnTypeName.append( (element instanceof TypeElement )?
                    ((TypeElement)element).getQualifiedName().toString() :
                        element.getSimpleName().toString());
            myShortTypeName.append(element.getSimpleName().toString());
        }
    }
    
    /*
     * Dialog shows element tree. Qualifiers and type is shown for selected 
     * node in this tree. This method is used to access an element which
     * contains qualifiers and type.
     * This method is required for derived classes which wants to reuse
     * functionality of this class. Such classes <code>context</code> element
     * could be without required annotations and type (F.e. observer method.
     * It is used as start point for finding its observer parameter ).    
     */
    protected Element getSelectedQualifiedElement( Element context , 
            WebBeansModel model )
    {
        return context;
    }
    
    /*
     * Normally the subject element is injection point.
     * In this case this method returns exactly injection point element
     * from its handle as context.
     * Subclasses could override this behavior to return some other 
     * element . This element will be used for showing type and qualifiers. 
     */
    protected Element getSubjectElement ( ElementHandle<? extends Element> context , 
            WebBeansModel model)
    {
        return context.resolve( model.getCompilationController() );
    }
    
    private void enterBusy() {
        myJavaHierarchyTree.setModel(pleaseWaitTreeModel);
        JRootPane rootPane = SwingUtilities.getRootPane(InjectablesPanel.this);
        if (rootPane != null) {
            rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            myLastFocusedComponent = window.getFocusOwner();
        }
        myFilterTextField.setEnabled(false);
        myCaseSensitiveFilterCheckBox.setEnabled(false);
        myShowFQNToggleButton.setEnabled(false);
        myExpandAllButton.setEnabled(false);
    }
    
    private void leaveBusy() {
        myJavaHierarchyTree.setModel(myJavaHierarchyModel);
        JRootPane rootPane = SwingUtilities.getRootPane(InjectablesPanel.this);
        if (rootPane != null) {
            rootPane.setCursor(Cursor.getDefaultCursor());
        }
        myFilterTextField.setEnabled(true);
        myCaseSensitiveFilterCheckBox.setEnabled(true);
        myShowFQNToggleButton.setEnabled(true);
        myExpandAllButton.setEnabled(true);
        if (myLastFocusedComponent != null) {
            if (myLastFocusedComponent.isDisplayable()) {
                myLastFocusedComponent.requestFocusInWindow();
            }
            myLastFocusedComponent = null;
        }
    }
    
    private void reload() {
        enterBusy();

        WebBeansNavigationOptions.setCaseSensitive(myCaseSensitiveFilterCheckBox.isSelected());
        WebBeansNavigationOptions.setShowFQN(myShowFQNToggleButton.isSelected());

        RequestProcessor.getDefault().post(
            new Runnable() {
                public void run() {
                    try {
                        myJavaHierarchyModel.update();
                    } finally {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                leaveBusy();
                                // expand the tree
                                for (int row = 0; 
                                    row < myJavaHierarchyTree.getRowCount(); row++) 
                                {
                                    myJavaHierarchyTree.expandRow(row);
                                }
                            }});
                    }
                }
            });
    }

    private void expandAll() {
        SwingUtilities.invokeLater(
                new Runnable() {
            public void run() {
                JRootPane rootPane = SwingUtilities.getRootPane(InjectablesPanel.this);
                if (rootPane != null) {
                    rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                }
            }
        }
        );

        SwingUtilities.invokeLater(
                new Runnable() {
            public void run() {
                try {
                    // expand the tree
                    for (int row = 0; row < myJavaHierarchyTree.getRowCount(); row++) {
                        myJavaHierarchyTree.expandRow(row);
                    }
                    selectMatchingRow();
                } finally {
                    JRootPane rootPane = SwingUtilities.getRootPane(InjectablesPanel.this);
                    if (rootPane != null) {
                        rootPane.setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        }
        );
    }

    private void selectMatchingRow() {
        myFilterTextField.setForeground(UIManager.getColor("TextField.foreground"));
        myJavaHierarchyTree.setSelectionRow(-1);
        // select first matching
        for (int row = 0; row < myJavaHierarchyTree.getRowCount(); row++) {
            Object o = myJavaHierarchyTree.getPathForRow(row).getLastPathComponent();
            if (o instanceof JavaElement) {
                String filterText = myFilterTextField.getText();
                if (Utils.patternMatch((JavaElement)o, filterText, 
                        filterText.toLowerCase())) 
                {
                    myJavaHierarchyTree.setSelectionRow(row);
                    myJavaHierarchyTree.scrollRowToVisible(row);
                    return;
                }
            }
        }
        myFilterTextField.setForeground(Color.RED);
    }

    private void gotoElement(JavaElement javaToolsJavaElement) {
        try {
            javaToolsJavaElement.gotoElement();
        } finally {
            close();
        }
    }

    private void showBindings() {
        myInjectableBindings.setToolTipText(null);
        TreePath treePath = myJavaHierarchyTree.getSelectionPath();
        if (treePath != null) {
            Object node = treePath.getLastPathComponent();
            if (node instanceof InjectableTreeNode<?>) {
                final ElementHandle<?> elementHandle = 
                    ((InjectableTreeNode<?>)node).getElementHandle();
                try {
                    getModel().runReadAction( new MetadataModelAction<WebBeansModel, Void>() {

                        public Void run( WebBeansModel model ) throws Exception {
                            Element element = elementHandle.resolve(
                                    model.getCompilationController());
                            if ( element == null ){
                                myInjectableBindings.setText("");
                            }
                            else {
                                element = getSelectedQualifiedElement( element, model);
                                List<AnnotationMirror> bindings = 
                                    model.getQualifiers(element);
                                StringBuilder builder = new StringBuilder();
                                for (AnnotationMirror annotationMirror : bindings) {
                                    appendBinding(annotationMirror, builder,  
                                            myShowFQNToggleButton.isSelected() );
                                }
                                String bindingsString = "";
                                if ( builder.length() >0 ){
                                    bindingsString = builder.substring(0 , 
                                            builder.length() -2 );
                                }
                                myInjectableBindings.setText( bindingsString);
                            }
                            return null;
                        }
                    });
                }
                catch (MetadataModelException e) {
                    Logger.getLogger( InjectablesPanel.class.getName() ).
                        log( Level.WARNING, e.getMessage(), e);
                }
                catch (IOException e) {
                    Logger.getLogger( InjectablesPanel.class.getName() ).
                    log( Level.WARNING, e.getMessage(), e);
                }
                myInjectableBindings.setCaretPosition(0);
                myInjectableBindings.setToolTipText(((JavaElement)node).getTooltip());
            }
        }
    }

    private void showJavaDoc() {
        TreePath treePath = myJavaHierarchyTree.getSelectionPath();
        if (treePath != null) {
            Object node = treePath.getLastPathComponent();
            if (node instanceof JavaElement) {
                myDocPane.setData( ((JavaElement)node).getJavaDoc() );
            }
        }
    }

    private void close() {
        Window window = SwingUtilities.getWindowAncestor(InjectablesPanel.this);
        if (window != null) {
            window.setVisible(false);
        }
    }
    
    private MetadataModel<WebBeansModel> getModel(){
        return myModel;
    }
    

    private void initCDIContext( ElementHandle<? extends Element> handle,
            WebBeansModel model )
    {
        Element context = getSubjectElement(handle, model);
        if ( context == null ){
            return;
        }
        
        TypeMirror typeMirror  = context.asType();
        myShortTypeName = new StringBuilder();
        myFqnTypeName = new StringBuilder();
        setInjectableType(typeMirror, model.getCompilationController());
        
        List<AnnotationMirror> qualifiers = model.getQualifiers( context );
        
        StringBuilder fqnBuilder = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        for (AnnotationMirror annotationMirror : qualifiers) {
            appendBinding(annotationMirror, fqnBuilder,  true );
            appendBinding(annotationMirror, builder,  false );
        }
        if ( fqnBuilder.length() >0 ){
            myFqnBindings  = fqnBuilder.substring(0 , fqnBuilder.length() -2 );
            myShortBindings = builder.substring(0 , builder.length() -2 );
        }
        else {
            // this should never happens actually.
            myFqnBindings = "";
            myShortBindings = "";
        }
        if ( myShowFQNToggleButton.isSelected() ) {
            myBindings.setText( myFqnBindings );
        }
        else {
            myBindings.setText( myShortBindings );
        }
        
        reloadInjectionPoint();
    }

    private void setInjectableArrayType( TypeMirror typeMirror,
            CompilationController controller )
    {
        TypeMirror componentType = ((ArrayType)typeMirror).getComponentType();
        setInjectableType(componentType, controller);
    }

    private void appendBinding( AnnotationMirror mirror , StringBuilder builder , 
            boolean isFqn )
    {
        DeclaredType type = mirror.getAnnotationType();
        Element annotation = type.asElement();
        
        builder.append("@");            // NOI18N
        String annotationName ;
        if ( isFqn ) {
            annotationName= ( annotation instanceof TypeElement )?
                ((TypeElement)annotation).getQualifiedName().toString() : 
                    annotation.getSimpleName().toString();
        }
        else { 
            annotationName = annotation.getSimpleName().toString();
        }
        
        builder.append( annotationName );
        
        appendBindingParamters( mirror , builder );
        
        builder.append(", ");           // NOI18N
    }

    private void appendBindingParamters( AnnotationMirror mirror,
            StringBuilder builder )
    {
        Map<? extends ExecutableElement, ? extends AnnotationValue> 
            elementValues = mirror.getElementValues();
        StringBuilder params = new StringBuilder();
        for ( Entry<? extends ExecutableElement, ? extends AnnotationValue> 
            entry :  elementValues.entrySet()) 
        {
            ExecutableElement key = entry.getKey();
            AnnotationValue value = entry.getValue();
            List<? extends AnnotationMirror> annotationMirrors = 
                key.getAnnotationMirrors();
            boolean nonBinding = false;
            for (AnnotationMirror annotationMirror : annotationMirrors) {
                DeclaredType annotationType = annotationMirror.getAnnotationType();
                Element element = annotationType.asElement();
                if ( ( element instanceof TypeElement ) && 
                        ((TypeElement)element).getQualifiedName().
                        contentEquals(NON_BINDING_MEMBER_ANNOTATION))
                {
                    nonBinding = true;
                    break;
                }
            }
            if ( !nonBinding ){
                params.append( key.getSimpleName().toString() );
                params.append( "=" );               // NOI18N
                if ( value.getValue() instanceof String ){
                    params.append('"');
                    params.append( value.getValue().toString());
                    params.append('"');
                }
                else {
                    params.append( value.getValue().toString());
                }
                params.append(", ");                // NOI18N
            }
        }
        if ( params.length() >0 ){
            builder.append( "(" );                   // NOI18N
            builder.append( params.substring(0 , params.length() -2 ));
            builder.append( ")" );                   // NOI18N
        }
    }

    private void reloadInjectionPoint(){
        if ( myShowFQNToggleButton.isSelected() ) {
            myBindings.setText( myFqnBindings );
            myType.setText(myFqnTypeName.toString());
        }
        else {
            myBindings.setText( myShortBindings );
            myType.setText(myShortTypeName.toString());
        } 
    }
    

    private void initListeners() {
        myFilterTextField.getDocument().addDocumentListener(
                new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                selectMatchingRow();
            }
            public void insertUpdate(DocumentEvent e) {
                selectMatchingRow();
            }
            public void removeUpdate(DocumentEvent e) {
                selectMatchingRow();
            }
        }
        );
        
        registerKeyboardActions();

        myCaseSensitiveFilterCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                WebBeansNavigationOptions.setCaseSensitive(
                        myCaseSensitiveFilterCheckBox.isSelected());
                if (myFilterTextField.getText().trim().length() > 0) {
                    // apply filters again only if there is some filter text
                    selectMatchingRow();
                }
            }
        });

        myJavaHierarchyTree.addMouseListener(
                new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                Point point = me.getPoint();
                TreePath treePath = myJavaHierarchyTree.
                    getPathForLocation(point.x, point.y);
                if (treePath != null) {
                    Object node = treePath.getLastPathComponent();
                    if (node instanceof JavaElement) {
                        if (me.getClickCount() == 2){
                            gotoElement((JavaElement) node);
                        }
                    }
                }
            }
        }
        );

        myJavaHierarchyTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                showBindings();
                showJavaDoc();
            }
        });

        myShowFQNToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                WebBeansNavigationOptions.setShowFQN(myShowFQNToggleButton.isSelected());
                myJavaHierarchyModel.fireTreeNodesChanged();
                reloadInjectionPoint();
                showBindings();
            }
        });

        myExpandAllButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        expandAll();
                    }
                });

        myCloseButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        close();
                    }
                });
    }

    private void registerKeyboardActions() {
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.firstRow(myJavaHierarchyTree);
            }
        };

        myFilterTextField.registerKeyboardAction( listener,
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, false),
                JComponent.WHEN_FOCUSED);
        
        myBindings.registerKeyboardAction(listener,
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, false),
                JComponent.WHEN_FOCUSED);

        listener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.previousRow(myJavaHierarchyTree);
            }
        };
        myFilterTextField.registerKeyboardAction(listener,
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
                JComponent.WHEN_FOCUSED);
        
        myBindings.registerKeyboardAction( listener ,
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
                JComponent.WHEN_FOCUSED);

        listener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.nextRow(myJavaHierarchyTree);
            }
        };
        myFilterTextField.registerKeyboardAction(listener,
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false),
                JComponent.WHEN_FOCUSED);
        
        myBindings.registerKeyboardAction(listener,
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false),
                JComponent.WHEN_FOCUSED);

        listener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.lastRow(myJavaHierarchyTree);
            }
        };
        myFilterTextField.registerKeyboardAction(listener,
                KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, false),
                JComponent.WHEN_FOCUSED);
        
        myBindings.registerKeyboardAction(listener,
                KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, false),
                JComponent.WHEN_FOCUSED);

        myBindings.putClientProperty(
            "HighlightsLayerExcludes", // NOI18N
            "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" // NOI18N
        );

        myFilterTextField.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath treePath = myJavaHierarchyTree.getSelectionPath();
                if (treePath != null) {
                    Object node = treePath.getLastPathComponent();
                    if (node instanceof JavaElement) {
                        gotoElement((JavaElement) node);
                    }
                }
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                JComponent.WHEN_FOCUSED);

        myFilterTextField.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Component view = myDocPane.getViewport().getView();
                        if (view instanceof JEditorPane) {
                            JEditorPane editorPane = (JEditorPane) view;
                            ActionListener actionForKeyStroke =
                                editorPane.getActionForKeyStroke(
                                        KeyStroke.getKeyStroke(
                                                KeyEvent.VK_PAGE_UP, 0, false));                            
                            actionForKeyStroke.actionPerformed(
                                    new ActionEvent(editorPane, 
                                            ActionEvent.ACTION_PERFORMED, ""));
                        }
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 
                        KeyEvent.SHIFT_MASK, false),
                JComponent.WHEN_FOCUSED);
        myFilterTextField.registerKeyboardAction(
                new ActionListener() {
                    private boolean firstTime = true;
                    public void actionPerformed(ActionEvent actionEvent) {
                        Component view = myDocPane.getViewport().getView();
                        if (view instanceof JEditorPane) {
                            JEditorPane editorPane = (JEditorPane) view;
                            ActionListener actionForKeyStroke =
                                editorPane.getActionForKeyStroke(
                                        KeyStroke.getKeyStroke(
                                                KeyEvent.VK_PAGE_DOWN, 0, false));
                            actionEvent = new ActionEvent(editorPane, 
                                    ActionEvent.ACTION_PERFORMED, "");
                            actionForKeyStroke.actionPerformed(actionEvent);
                            if (firstTime) {
                                actionForKeyStroke.actionPerformed(actionEvent);
                                firstTime = false;
                            }
                        }
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 
                        KeyEvent.SHIFT_MASK, false),
                JComponent.WHEN_FOCUSED);
        
        myJavaHierarchyTree.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath treePath = myJavaHierarchyTree.getLeadSelectionPath();
                if (treePath != null) {
                    Object node = treePath.getLastPathComponent();
                    if (node instanceof JavaElement) {
                        gotoElement((JavaElement) node);
                    }
                }
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                JComponent.WHEN_FOCUSED);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mySplitPane = new javax.swing.JSplitPane();
        myJavaHierarchyTreeScrollPane = new javax.swing.JScrollPane();
        myJavaHierarchyTree = new javax.swing.JTree();
        myFilterLabel = new javax.swing.JLabel();
        myFilterTextField = new javax.swing.JTextField();
        myCaseSensitiveFilterCheckBox = new javax.swing.JCheckBox();
        myFiltersLabel = new javax.swing.JLabel();
        myCloseButton = new javax.swing.JButton();
        myFiltersToolbar = new NoBorderToolBar();
        myShowFQNToggleButton = new javax.swing.JToggleButton();
        myExpandAllButton = new javax.swing.JButton();
        mySeparator = new javax.swing.JSeparator();
        myBindings = new javax.swing.JEditorPane();
        myBindingLbl = new javax.swing.JLabel();
        myType = new javax.swing.JEditorPane();
        myTypeLbl = new javax.swing.JLabel();
        myInjectableBindings = new javax.swing.JEditorPane();
        myInjectableBindingLbl = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        mySplitPane.setDividerLocation(300);

        myJavaHierarchyTreeScrollPane.setBorder(null);
        myJavaHierarchyTreeScrollPane.setViewportView(myJavaHierarchyTree);
        myJavaHierarchyTree.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSD_InjectableHierarchy")); // NOI18N

        mySplitPane.setLeftComponent(myJavaHierarchyTreeScrollPane);

        myFilterLabel.setLabelFor(myFilterTextField);
        org.openide.awt.Mnemonics.setLocalizedText(myFilterLabel, org.openide.util.NbBundle.getBundle(InjectablesPanel.class).getString("LABEL_filterLabel")); // NOI18N

        myFilterTextField.setToolTipText(org.openide.util.NbBundle.getBundle(InjectablesPanel.class).getString("TOOLTIP_filterTextField")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myCaseSensitiveFilterCheckBox, org.openide.util.NbBundle.getBundle(InjectablesPanel.class).getString("LABEL_caseSensitiveFilterCheckBox")); // NOI18N
        myCaseSensitiveFilterCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(myFiltersLabel, org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "LABEL_filtersLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myCloseButton, org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "LABEL_Close")); // NOI18N

        myFiltersToolbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        myFiltersToolbar.setFloatable(false);
        myFiltersToolbar.setBorderPainted(false);
        myFiltersToolbar.setOpaque(false);

        myShowFQNToggleButton.setIcon(FQN_ICON);
        myShowFQNToggleButton.setMnemonic('Q');
        myShowFQNToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(InjectablesPanel.class).getString("TOOLTIP_showFQNToggleButton")); // NOI18N
        myShowFQNToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        myFiltersToolbar.add(myShowFQNToggleButton);

        myExpandAllButton.setIcon(EXPAND_ALL_ICON);
        myExpandAllButton.setMnemonic('E');
        myExpandAllButton.setToolTipText(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "TOOLTIP_expandAll")); // NOI18N
        myExpandAllButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        myFiltersToolbar.add(myExpandAllButton);

        myBindings.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Nb.ScrollPane.Border.color")));
        myBindings.setContentType("text/x-java");
        myBindings.setEditable(false);

        myBindingLbl.setLabelFor(myBindings);
        org.openide.awt.Mnemonics.setLocalizedText(myBindingLbl, org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "LBL_Bindings")); // NOI18N

        myType.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Nb.ScrollPane.Border.color")));
        myType.setContentType("text/x-java");
        myType.setEditable(false);

        myTypeLbl.setLabelFor(myType);
        org.openide.awt.Mnemonics.setLocalizedText(myTypeLbl, org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "LBL_Type")); // NOI18N

        myInjectableBindings.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Nb.ScrollPane.Border.color")));
        myInjectableBindings.setContentType("text/x-java");
        myInjectableBindings.setEditable(false);

        myInjectableBindingLbl.setLabelFor(myInjectableBindings);
        org.openide.awt.Mnemonics.setLocalizedText(myInjectableBindingLbl, org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "LBL_CurrentElementBindings")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mySplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(myFilterLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myFilterTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myCaseSensitiveFilterCheckBox))
                    .add(mySeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(myBindingLbl)
                            .add(myTypeLbl))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(myType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
                            .add(myBindings, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(myFiltersLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myFiltersToolbar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myCloseButton))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(myInjectableBindingLbl)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myInjectableBindings, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myTypeLbl)
                    .add(myType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myBindings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myBindingLbl))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mySeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myFilterLabel)
                    .add(myFilterTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myCaseSensitiveFilterCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mySplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myInjectableBindingLbl)
                    .add(myInjectableBindings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(myFiltersLabel)
                        .add(myCloseButton))
                    .add(myFiltersToolbar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        myFilterLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSN_TextFilter")); // NOI18N
        myFilterLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSD_TextFilter")); // NOI18N
        myFilterTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSD_TextFieldFilter")); // NOI18N
        myCaseSensitiveFilterCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSN_CaseSensitive")); // NOI18N
        myCaseSensitiveFilterCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "caseSensitiveFilterCheckBox_ACSD")); // NOI18N
        myFiltersLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSN_Filters")); // NOI18N
        myFiltersLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSD_Filters")); // NOI18N
        myCloseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSN_Close")); // NOI18N
        myCloseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSD_Close")); // NOI18N
        myBindingLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSN_Bindings")); // NOI18N
        myBindingLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSD_Bindnigs")); // NOI18N
        myTypeLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSN_Type")); // NOI18N
        myTypeLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSD_Type")); // NOI18N
        myInjectableBindingLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSN_InjectableBindings")); // NOI18N
        myInjectableBindingLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InjectablesPanel.class, "ACSD_InjectableBindnigs")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel myBindingLbl;
    private javax.swing.JEditorPane myBindings;
    private javax.swing.JCheckBox myCaseSensitiveFilterCheckBox;
    private javax.swing.JButton myCloseButton;
    private javax.swing.JButton myExpandAllButton;
    private javax.swing.JLabel myFilterLabel;
    private javax.swing.JTextField myFilterTextField;
    private javax.swing.JLabel myFiltersLabel;
    private javax.swing.JToolBar myFiltersToolbar;
    private javax.swing.JLabel myInjectableBindingLbl;
    private javax.swing.JEditorPane myInjectableBindings;
    private javax.swing.JTree myJavaHierarchyTree;
    private javax.swing.JScrollPane myJavaHierarchyTreeScrollPane;
    private javax.swing.JSeparator mySeparator;
    private javax.swing.JToggleButton myShowFQNToggleButton;
    private javax.swing.JSplitPane mySplitPane;
    private javax.swing.JEditorPane myType;
    private javax.swing.JLabel myTypeLbl;
    // End of variables declaration//GEN-END:variables
    
    private StringBuilder myFqnTypeName;
    private StringBuilder myShortTypeName;
    
    private String myFqnBindings;
    private String myShortBindings;
    
    private JavaHierarchyModel myJavaHierarchyModel;
    
    private DocumentationScrollPane myDocPane;
    private MetadataModel<WebBeansModel> myModel;
    
    private Component myLastFocusedComponent;
}
