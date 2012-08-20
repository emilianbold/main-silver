/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.navigation.hierarchy;

import com.sun.source.util.TreePath;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.java.navigation.JavadocTopComponent;
import org.netbeans.modules.java.navigation.NoBorderToolBar;
import org.netbeans.modules.java.navigation.base.Pair;
import org.netbeans.modules.java.navigation.base.TapPanel;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd = "-//org.netbeans.modules.java.navigation.hierarchy//Hierarchy//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "HierarchyTopComponent",
iconBase="org/netbeans/modules/java/navigation/resources/supertypehierarchy.gif", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "properties", openAtStartup = false)
@Messages({
    "CTL_HierarchyTopComponent=Hierarchy",
    "HINT_HierarchyTopComponent=This is a Hierarchy window"
})
public final class HierarchyTopComponent extends TopComponent implements ExplorerManager.Provider, ActionListener, PropertyChangeListener {

    private static final int NOW = 0;
    private static final int JDOC_TIME = 500;
    private static final Logger LOG = Logger.getLogger(HierarchyTopComponent.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(HierarchyTopComponent.class);
    @StaticResource
    private static final String REFRESH_ICON = "org/netbeans/modules/java/navigation/resources/hierarchy_refresh.png";  //NOI18N
    @StaticResource
    private static final String JDOC_ICON = "org/netbeans/modules/java/navigation/resources/javadoc_open.png";          //NOI18N
    private static final String NON_ACTIVE_CONTENT = "non-active-content";  //NOI18N
    private static final String ACTIVE_CONTENT = "active-content";  //NOI18N
    private static final String PROP_LOWER_TOOLBAR_EXPANDED = "filtersPanelTap.expanded"; //NOI18N
    private static final int MIN_HISTORY_WIDTH = 50;
    
    private static HierarchyTopComponent instance;

    private final JDocFinder jdocFinder;
    private final RequestProcessor.Task jdocTask;
    private final ExplorerManager explorerManager;
    private final InstanceContent selectedNodes;
    private final Lookup lookup;
    private final Container contentView;
    private final BeanTreeView btw;
    private final TapPanel lowerToolBar;
    private final JComboBox viewTypeCombo;
    private final JComboBox historyCombo;
    private final JButton refreshButton;
    private final JButton jdocButton;
    private final HierarchyFilters filters;
    private final RootChildren rootChildren;

    @NbBundle.Messages({
        "TXT_RefreshContent=Refresh",
        "TXT_OpenJDoc=Open Javadoc Window",
        "TXT_NonActiveContent=<No View Available - Refresh Manually>"
    })
    public HierarchyTopComponent() {
        jdocFinder = new JDocFinder();
        jdocTask = RP.create(jdocFinder);
        explorerManager = new ExplorerManager();
        rootChildren = new RootChildren();
        filters = new HierarchyFilters();
        explorerManager.setRootContext(Nodes.rootNode(rootChildren, filters));
        selectedNodes  = new InstanceContent();
        lookup = new AbstractLookup(selectedNodes);
        explorerManager.addPropertyChangeListener(this);
        initComponents();
        setName(Bundle.CTL_HierarchyTopComponent());
        setToolTipText(Bundle.HINT_HierarchyTopComponent());        
        viewTypeCombo = new JComboBox(new DefaultComboBoxModel(ViewType.values()));
        viewTypeCombo.addActionListener(this);
        historyCombo = new JComboBox(HierarchyHistoryUI.createModel()){
            @Override
            public Dimension getMinimumSize() {
                Dimension res = super.getMinimumSize();
                if (res.width > MIN_HISTORY_WIDTH) {
                    res = new Dimension(MIN_HISTORY_WIDTH, res.height);
                }
                return res;
            }
        };
        historyCombo.setRenderer(HierarchyHistoryUI.createRenderer());
        historyCombo.addActionListener(this);
        refreshButton = new JButton(ImageUtilities.loadImageIcon(REFRESH_ICON, true));
        refreshButton.addActionListener(this);
        refreshButton.setFocusable(false);
        refreshButton.setToolTipText(Bundle.TXT_RefreshContent());
        jdocButton = new JButton(ImageUtilities.loadImageIcon(JDOC_ICON, true));
        jdocButton.addActionListener(this);
        jdocButton.setFocusable(false);
        jdocButton.setToolTipText(Bundle.TXT_OpenJDoc());
        final Box upperToolBar = new MainToolBar(viewTypeCombo, historyCombo, refreshButton, jdocButton);        
        add(decorateAsUpperPanel(upperToolBar), BorderLayout.NORTH);
        contentView = new JPanel();
        contentView.setLayout(new CardLayout());
        JPanel nonActiveContent = updateBackground(new JPanel());
        nonActiveContent.setLayout(new BorderLayout());
        final JLabel info = new JLabel(Bundle.TXT_NonActiveContent());
        info.setEnabled(false);
        info.setHorizontalAlignment(SwingConstants.CENTER);
        nonActiveContent.add(info, BorderLayout.CENTER);
        btw = createBeanTreeView();
        contentView.add(nonActiveContent, NON_ACTIVE_CONTENT);
        contentView.add(btw, ACTIVE_CONTENT);
        add(contentView,BorderLayout.CENTER);
        lowerToolBar = new TapPanel();
        lowerToolBar.setOrientation(TapPanel.DOWN);
        final JComponent lowerButtons = filters.getComponent();
        lowerButtons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        lowerToolBar.add(lowerButtons);
        final boolean expanded = NbPreferences.forModule(HierarchyTopComponent.class).
                getBoolean(PROP_LOWER_TOOLBAR_EXPANDED, true); //NOI18N
        lowerToolBar.setExpanded(expanded);
        lowerToolBar.addPropertyChangeListener(this);
        add(updateBackground(lowerToolBar), BorderLayout.SOUTH);

    }

    public void setContext(
            @NonNull final JavaSource js,
            @NonNull final JTextComponent tc) {
        final Collection<FileObject> fos = js.getFileObjects();
        assert fos.size() == 1;
        final FileObject fo = fos.iterator().next();
        final Callable<Pair<URI,ElementHandle<TypeElement>>> resolver = new EditorResolver(
                js,
                fo,
                tc.getCaret().getDot());
        schedule(resolver);
    }

    public void setContext (@NonNull final JavaSource js) {
        final Collection<FileObject> fos = js.getFileObjects();
        assert fos.size() == 1;
        final FileObject fo = fos.iterator().next();
        final Callable<Pair<URI,ElementHandle<TypeElement>>> resolver = new FileResolver(js, fo);
        schedule(resolver);

    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        if (refreshButton == e.getSource()) {
            final JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
            final JavaSource js = JavaSource.forDocument(Utilities.getDocument(lastFocusedComponent));
            if (js != null) {
                setContext(js, lastFocusedComponent);
            }
        } else if (jdocButton == e.getSource()) {
            final TopComponent win = JavadocTopComponent.findInstance();
            if (win != null && !win.isShowing()) {
                win.open();
                jdocTask.schedule(NOW);
            }
        } else if (historyCombo == e.getSource()) {
            refresh();
        } else if (viewTypeCombo == e.getSource()) {
            refresh();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            final Node[] oldNodes = (Node[])evt.getOldValue();
            final Node[] newNodes = (Node[])evt.getNewValue();
            for (Node n: oldNodes) {
                selectedNodes.remove(n);
                selectedNodes.remove(n, NodeToFileObjectConvertor.INSTANCE);
            }            
            for (Node n : newNodes) {
                selectedNodes.add(n);
                selectedNodes.add(n, NodeToFileObjectConvertor.INSTANCE);
            }
            if (newNodes.length > 0 && JavadocTopComponent.shouldUpdate()) {
                jdocFinder.cancel();
                jdocTask.schedule(JDOC_TIME);
            }
        } else if (TapPanel.EXPANDED_PROPERTY.equals(evt.getPropertyName())) {
            NbPreferences.forModule(HierarchyTopComponent.class).putBoolean(
                    PROP_LOWER_TOOLBAR_EXPANDED,
                    lowerToolBar.isExpanded());
        }
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        if (JavadocTopComponent.shouldUpdate() && getLookup().lookup(Node.class) != null) {
            jdocFinder.cancel();
            jdocTask.schedule(NOW);
        }
    }



    @NonNull
    private static BeanTreeView createBeanTreeView() {
        final BeanTreeView btw = new BeanTreeView();
        btw.setRootVisible(false);
        return btw;
    }

    @NonNull
    private static <T extends JComponent> T updateBackground(@NonNull final T comp) {
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            comp.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
        return comp;
    }

    @NonNull
    private static JPanel decorateAsUpperPanel(@NonNull final JComponent comp) {
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        wrapper.add(comp,c);
        wrapper.setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0,
                    UIManager.getColor("NbSplitPane.background")));//NOI18N
        return updateBackground(wrapper);
    }

    private void showBusy() {
        assert SwingUtilities.isEventDispatchThread();
        ((CardLayout)contentView.getLayout()).show(contentView, ACTIVE_CONTENT);
        rootChildren.set(Nodes.waitNode());
    }

    private void schedule(@NonNull final Callable<Pair<URI,ElementHandle<TypeElement>>> resolver) {
        showBusy();
        assert resolver != null;
        final RunnableFuture<Pair<URI,ElementHandle<TypeElement>>> becomesType = new FutureTask<Pair<URI,ElementHandle<TypeElement>>>(resolver);
        jdocTask.cancel();
        jdocFinder.cancel();
        RP.execute(becomesType);
        Object selItem = viewTypeCombo.getSelectedItem();
        if (!(selItem instanceof ViewType)) {
            selItem = ViewType.SUPER_TYPE;
        }
        final Runnable refreshTask = new RefreshTask(becomesType,(ViewType)selItem);
        jdocTask.cancel();
        jdocFinder.cancel();
        RP.execute(refreshTask);
    }

    private void refresh() {
        final Object selItem = historyCombo.getSelectedItem();
        if (selItem instanceof Pair) {
            final Pair<URI,ElementHandle<TypeElement>> pair = (Pair<URI,ElementHandle<TypeElement>>)selItem;
            schedule(new Callable<Pair<URI, ElementHandle<TypeElement>>>() {
                @Override
                public Pair<URI, ElementHandle<TypeElement>> call() throws Exception {
                    return pair;
                }
            });
        }
    }

    public static synchronized HierarchyTopComponent findDefault() {

        HierarchyTopComponent component = instance;

        if (component == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent("HierarchyTopComponent"); //NOI18N
            if (tc instanceof HierarchyTopComponent) {
                component = instance = (HierarchyTopComponent) tc;
            } else {
                component = instance = new HierarchyTopComponent();
            }
        }
        return component;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @NbBundle.Messages({
        "LBL_SuperTypeView=Supertype View",
        "LBL_SubTypeView=Subtype View"})
    private static enum ViewType {
                       
        SUPER_TYPE(Bundle.LBL_SuperTypeView()),
        SUB_TYPE(Bundle.LBL_SubTypeView());

        private final String displayName;

        private ViewType(@NonNull final String displayName) {
            assert displayName != null;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static final class FileResolver implements Callable<Pair<URI,ElementHandle<TypeElement>>>{

        private final JavaSource js;
        private final FileObject fo;

        public FileResolver(
                @NonNull final JavaSource js,
                @NonNull final FileObject fo) {
            assert js != null;
            assert fo != null;
            this.js = js;
            this.fo = fo;
        }

        @Override
        public Pair<URI,ElementHandle<TypeElement>> call() throws Exception {
            final List<ElementHandle<TypeElement>> ret = new ArrayList<ElementHandle<TypeElement>>(1);
            ret.add(null);
            js.runUserActionTask(
                    new Task<CompilationController>(){
                        @Override
                        public void run(CompilationController cc) throws Exception {
                            cc.toPhase (Phase.ELEMENTS_RESOLVED);
                            ret.set(0,findMainElement(cc,fo.getName()));
                        }
                    },
                    true);
            final ElementHandle<TypeElement> handle = ret.get(0);
            if (handle == null) {
                return null;
            }
            final FileObject file = SourceUtils.getFile(handle, js.getClasspathInfo());
            if (file == null) {
                return null;
            }
            return Pair.<URI,ElementHandle<TypeElement>>of(file.toURI(),handle);
        }

        @CheckForNull
        static ElementHandle<TypeElement> findMainElement(
                @NonNull final CompilationController cc,
                @NonNull final String fileName) {
            final List<? extends TypeElement> topLevels = cc.getTopLevelElements();
            if (topLevels.isEmpty()) {
                return null;
            }
            TypeElement candidate = topLevels.get(0);
            for (int i = 1; i< topLevels.size(); i++) {
                if (fileName.contentEquals(topLevels.get(i).getSimpleName())) {
                    candidate = topLevels.get(i);
                    break;
                }
            }
            return ElementHandle.create(candidate);
        }
    }

    private static final class EditorResolver implements Callable<Pair<URI,ElementHandle<TypeElement>>> {

        private final JavaSource js;
        private final FileObject fo;
        private final int dot;

        public EditorResolver(
                @NonNull final JavaSource js,
                @NonNull final FileObject fo,
                final int dot) {
            assert js != null;
            assert fo != null;
            this.js = js;
            this.fo = fo;
            this.dot = dot;
        }

        @Override
        public Pair<URI,ElementHandle<TypeElement>> call() throws Exception {
            final List<ElementHandle<TypeElement>> ret = new ArrayList<ElementHandle<TypeElement>>();
            ret.add(null);
            js.runUserActionTask(
                    new Task<CompilationController>(){
                        @Override
                        public void run(CompilationController cc) throws Exception {
                            cc.toPhase (Phase.RESOLVED);
                            Document document = cc.getDocument ();
                            if (document != null) {
                                // Find the TreePath for the caret position
                                final TreePath tp = cc.getTreeUtilities ().pathFor(dot);
                                // Get Element
                                Element element = cc.getTrees().getElement(tp);
                                if (element instanceof TypeElement) {
                                    ret.set(0, ElementHandle.create((TypeElement) element));
                                } else if (element instanceof VariableElement) {
                                    TypeMirror typeMirror = ((VariableElement) element).asType();
                                    if (typeMirror.getKind() == TypeKind.DECLARED) {
                                        element = ((DeclaredType) typeMirror).asElement();
                                        if (element != null) {
                                            ret.set(0, ElementHandle.create((TypeElement) element));
                                        }
                                    }
                                } else if (element instanceof ExecutableElement) {
                                    if (element.getKind() == ElementKind.METHOD) {
                                        TypeMirror typeMirror = ((ExecutableElement) element).getReturnType();
                                        if (typeMirror.getKind() == TypeKind.DECLARED) {
                                            element = ((DeclaredType) typeMirror).asElement();
                                            if (element != null) {
                                                ret.set(0, ElementHandle.create((TypeElement) element));
                                            }
                                        }
                                    } else if (element.getKind() == ElementKind.CONSTRUCTOR) {
                                        element = element.getEnclosingElement();
                                        if (element != null) {
                                            ret.set(0, ElementHandle.create((TypeElement) element));
                                        }
                                    }
                                } else {
                                    ret.set(0,FileResolver.findMainElement(cc, fo.getName()));
                                }
                            }
                        }
                    },
                    true);
            final ElementHandle<TypeElement> handle = ret.get(0);
            if (handle == null) {
                return null;
            }
            final FileObject file = SourceUtils.getFile(handle, js.getClasspathInfo());
            if (file == null) {
                return null;
            }
            return Pair.<URI,ElementHandle<TypeElement>>of(file.toURI(),handle);
        }
    }


    private final class RefreshTask implements Runnable {

        private final Future<Pair<URI,ElementHandle<TypeElement>>> toShow;
        private final ViewType viewType;

        RefreshTask(
            @NonNull final Future<Pair<URI,ElementHandle<TypeElement>>> toShow,
            @NonNull final ViewType viewType) {
            assert toShow != null;
            assert viewType != null;
            this.toShow = toShow;
            this.viewType = viewType;
        }

        @Override
        @NbBundle.Messages({
        "ERR_Cannot_Resolve_File=Cannot resolve type: {0}.",
        "ERR_Not_Declared_Type=Not a declared type."})
        public void run() {
            try {
                final Pair<URI,ElementHandle<TypeElement>> pair = toShow.get();
                if (pair != null) {
                    final FileObject file = URLMapper.findFileObject(pair.first.toURL());
                    JavaSource js;
                    if (file != null && (js=JavaSource.forFileObject(file)) != null) {
                        LOG.log(Level.FINE, "Showing hierarchy for: {0}", pair.second.getQualifiedName());  //NOI18N
                        HierarchyHistory.getInstance().addToHistory(pair);
                        js.runUserActionTask(new Task<CompilationController>() {
                            @Override
                            public void run(CompilationController cc) throws Exception {
                                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                                final TypeElement te = pair.second.resolve(cc);
                                if (te != null) {
                                    final Node root;
                                    if (viewType == ViewType.SUPER_TYPE) {
                                     root = Nodes.superTypeHierarchy(
                                            (DeclaredType)te.asType(),
                                            cc.getClasspathInfo(),
                                            filters);
                                    } else {
                                        Node subTypes = Nodes.subTypeHierarchy(te, cc, filters, new AtomicBoolean());
                                        
                                        root = subTypes != null ? subTypes : /*XXX:*/new AbstractNode(Children.LEAF);
                                    }
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            historyCombo.getModel().setSelectedItem(pair);
                                            rootChildren.set(root);
                                            btw.expandAll();
                                        }
                                    });
                                }
                            }
                        }, true);
                    } else {
                        rootChildren.set(null);
                        StatusDisplayer.getDefault().setStatusText(Bundle.ERR_Cannot_Resolve_File(pair.second.getQualifiedName()));
                    }
                } else {
                    rootChildren.set(null);
                    StatusDisplayer.getDefault().setStatusText(Bundle.ERR_Not_Declared_Type());
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private class JDocFinder implements Runnable, Callable<Boolean>, Task<CompilationController> {

        //@NotThreadSafe
        private ElementHandle<?> handle;
        //@NotThreadSafe
        private ElementJavadoc doc;
        private volatile boolean cancelled;

        @Override
        public void run() {
            cancelled = false;
            if (JavadocTopComponent.shouldUpdate()) {
                final ElementJavadoc documentation = getJavaDoc();
                if (documentation != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final JavadocTopComponent tc = JavadocTopComponent.findInstance();
                            if (tc != null) {
                                tc.open();
                                tc.setJavadoc(documentation);
                            }
                        }
                    });
                }
            }
        }

        @Override
        public void run(CompilationController cc) throws Exception {
            if (cancelled) {
                return;
            }
            cc.toPhase( JavaSource.Phase.UP_TO_DATE );
            if (cancelled) {
                return;
            }
            final Element e = handle.resolve(cc);
            if (e != null && !cancelled) {
                doc = ElementJavadoc.create(cc, e, this);
            }
        }

        @Override
        @NonNull
        public Boolean call() throws Exception {
            return cancelled;
        }

        void cancel() {
            cancelled = true;
        }

        @CheckForNull
        private ElementJavadoc getJavaDoc() {
            final Node node = getLookup().lookup(Node.class);
            if (node == null) {
                return null;
            }
            final TreePathHandle tph = node.getLookup().lookup(TreePathHandle.class);
            if (tph == null) {
                return null;
            }
            final FileObject fo = node.getLookup().lookup(FileObject.class);
            if (fo == null) {
                return null;
            }
            final JavaSource js = JavaSource.forFileObject(fo);
            if (js == null) {
                return null;
            }
            handle = tph.getElementHandle();
            try {
                js.runUserActionTask(this, true);
            } catch( IOException ioE ) {
                Exceptions.printStackTrace(ioE);
                return null;
            }
            return doc;
        }
    };

    private static final class NodeToFileObjectConvertor implements InstanceContent.Convertor<Node,FileObject> {

        public static final NodeToFileObjectConvertor INSTANCE =
                new NodeToFileObjectConvertor();

        private NodeToFileObjectConvertor() {}

        @Override
        public FileObject convert(Node obj) {
            return obj.getLookup().lookup(FileObject.class);
        }

        @Override
        public Class<? extends FileObject> type(Node obj) {
            return FileObject.class;
        }

        @Override
        public String id(Node obj) {
            return obj.toString();
        }

        @Override
        public String displayName(Node obj) {
            return obj.getDisplayName();
        }

    }
    
    private static final class MainToolBar extends Box {
        MainToolBar(@NonNull final JComponent... components) {
            super(BoxLayout.X_AXIS);
            setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 5));
            final JToolBar toolbar = new NoBorderToolBar(JToolBar.HORIZONTAL);
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setBorderPainted(false);
            toolbar.setBorder(BorderFactory.createEmptyBorder());
            toolbar.setOpaque(false);
            toolbar.setFocusable(false);
            for (JComponent component : components) {
                toolbar.add(component);
            }
            add (toolbar);
        }
    }

    private static final class RootChildren extends Children.Array {
        
        void set (Node node) {
            remove(getNodes(true));
            if (node != null) {
                add(new Node[] {node});
            }
        }
    }
}
