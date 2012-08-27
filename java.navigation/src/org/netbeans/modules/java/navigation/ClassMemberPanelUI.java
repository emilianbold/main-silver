/*
 * ClassMemberPanelUi.java
 *
 * Created on November 8, 2006, 4:03 PM
 */

package org.netbeans.modules.java.navigation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.modules.java.navigation.ElementNode.Description;
import org.netbeans.modules.java.navigation.actions.FilterSubmenuAction;
import org.netbeans.modules.java.navigation.actions.SortActions;
import org.netbeans.modules.java.navigation.base.FiltersManager;
import org.netbeans.modules.java.navigation.base.HistorySupport;
import org.netbeans.modules.java.navigation.base.Pair;
import org.netbeans.modules.java.navigation.base.Resolvers;
import org.netbeans.modules.java.navigation.base.SelectJavadocTask;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.netbeans.modules.java.navigation.base.TapPanel;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 *
 * @author  phrebejk
 */
@SuppressWarnings("ClassWithMultipleLoggers")
public class ClassMemberPanelUI extends javax.swing.JPanel
        implements ExplorerManager.Provider, Lookup.Provider, FiltersManager.FilterChangeListener, PropertyChangeListener {

    private static final String JDOC_ICON = "org/netbeans/modules/java/navigation/resources/javadoc_open.png";          //NOI18N
    private static final String CMD_JDOC = "jdoc";  //NOI18N
    private static final String CMD_HISTORY = "history";    //NOI18N
    private static final int MIN_HISTORY_WIDTH = 50;

    private final ExplorerManager manager = new ExplorerManager();
    private final MyBeanTreeView elementView;
    private final TapPanel filtersPanel;
    private final InstanceContent selectedNodes = new InstanceContent();
    private final Lookup lookup = new AbstractLookup(selectedNodes);
    private final ClassMemberFilters filters;
    private final AtomicReference<State> state = new AtomicReference<State>();    
    private final Action[] actions; // General actions for the panel
    private final SelectJavadocTask jdocFinder;
    private final RequestProcessor.Task watcherTask = WATCHER_RP.create(new Runnable() {
        @Override
        public void run() {
            final State current = state.get();
            if (current != State.DONE) {
                LOG.log(
                    Level.WARNING,
                    "No scheduled navigator update in {0}ms, current state: {1}",   //NOI18N
                    new Object[]{
                        WATCHER_TIME,
                        state.get()
                    });
            }
        }
    });
    private final RequestProcessor.Task jdocTask;
    private final HistorySupport history;
    private long lastShowWaitNodeTime = -1;
    //@GuardedBy this
    private Toolbar toolbar;

    private static final int JDOC_TIME = 500;
    private static final Logger LOG = Logger.getLogger(ClassMemberPanelUI.class.getName()); //NOI18N
    private static final Logger PERF_LOG = Logger.getLogger(ClassMemberPanelUI.class.getName() + ".perf"); //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(ClassMemberPanelUI.class.getName(), 1);
    private static final RequestProcessor WATCHER_RP = new RequestProcessor(ClassMemberPanelUI.class.getName() + ".watcher", 1, false, false);  //NOI18N
    private static final int WATCHER_TIME = 30000; 
    
    
    /** Creates new form ClassMemberPanelUi */
    public ClassMemberPanelUI() {
        history = HistorySupport.getInstnace(this.getClass());
        jdocFinder = SelectJavadocTask.create(this);
        jdocTask = RP.create(jdocFinder);
        initComponents();
        manager.addPropertyChangeListener(this);
        
        // Tree view of the elements
        elementView = createBeanTreeView();        
        add(elementView, BorderLayout.CENTER);
               
        // filters
        filtersPanel = new TapPanel();
        filtersPanel.setOrientation(TapPanel.DOWN);
        // tooltip
        KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        String keyText = Utilities.keyToString(toggleKey);
        filtersPanel.setToolTipText(NbBundle.getMessage(ClassMemberPanelUI.class, "TIP_TapPanel", keyText)); //NOI18N
        
        filters = new ClassMemberFilters( this );
        filters.getFiltersManager().hookChangeListener(this);
        JComponent buttons = filters.getComponent();
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        filtersPanel.add(buttons);
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            filtersPanel.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        
        actions = new Action[] {            
            SortActions.createSortByNameAction(filters),
            SortActions.createSortBySourceAction(filters),
            null,
            new FilterSubmenuAction(filters.getFiltersManager())
        };
        
        add(filtersPanel, BorderLayout.SOUTH);        

        boolean expanded = NbPreferences.forModule(ClassMemberPanelUI.class).getBoolean("filtersPanelTap.expanded", true); //NOI18N
        filtersPanel.setExpanded(expanded);
        filtersPanel.addPropertyChangeListener(this);
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean result = super.requestFocusInWindow();
        elementView.requestFocusInWindow();
        return result;
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        elementView.requestFocus();
    }

    @Override
    public org.openide.util.Lookup getLookup() {
        // XXX Check for chenge of FileObject
        return lookup;
    }
    
    public org.netbeans.modules.java.navigation.ElementScanningTask getTask() {
        
        return new ElementScanningTask(this);
        
    }
    
    
    void showWaitNode() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
               elementView.setRootVisible(true);
               manager.setRootContext(ElementNode.getWaitNode());
               lastShowWaitNodeTime = System.currentTimeMillis();
               scheduled();
            } 
        });
    }
    
    void clearNodes() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
               elementView.setRootVisible(false);
               manager.setRootContext(new AbstractNode(Children.LEAF));
            } 
        });
    }
    
    private void scheduled() {
        state.set(State.SCHEDULED);
        boolean ae = false;
        assert ae = true;
        if (ae) {
            watcherTask.schedule(WATCHER_TIME);
        }
    }
    
    void start() {
        state.set(State.INVOKED);
    }
    
    private void done() {
        state.set(State.DONE);
        boolean ae = false;
        assert ae = true;
        if (ae) {
            watcherTask.cancel();
        }
    }
    
    public void selectElementNode( ElementHandle<Element> eh ) {
        ElementNode root = getRootNode();
        if ( root == null ) {
            return;
        }
        ElementNode node = root.getNodeForElement(eh);
        try {
            manager.setSelectedNodes(new Node[]{ node == null ? getRootNode() : node });
        } catch (PropertyVetoException propertyVetoException) {
            Exceptions.printStackTrace(propertyVetoException);
        }
    }
    
    public void setContext(
            @NonNull final JavaSource js,
            @NullAllowed JTextComponent target) {
        final Callable<Pair<URI,ElementHandle<TypeElement>>> resolver =
                target == null ?
                Resolvers.createFileResolver(js) :
                Resolvers.createEditorResolver(
                    js,
                    target.getCaret().getDot());
        schedule(resolver);
    }

    synchronized JComponent getToolbar() {
        if (toolbar == null) {
            toolbar = new Toolbar();
        }
        return toolbar;
    }

    void refresh( final Description description ) {
        
        final ElementNode rootNode = getRootNode();
        
        if ( rootNode != null && rootNode.getDescritption().fileObject.equals( description.fileObject) ) {
            // update
            //System.out.println("UPDATE ======" + description.fileObject.getName() );
            jdocTask.cancel();
            jdocFinder.cancel();
            RP.post(new Runnable() {
                public void run() {
                    rootNode.updateRecursively( description );
                    done();
                }
            } );            
        } else {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    elementView.setRootVisible(false);        
                    manager.setRootContext(new ElementNode( description ) );
                    done();
                    boolean scrollOnExpand = getScrollOnExpand();
                    setScrollOnExpand( false );
                    elementView.setAutoWaitCursor(false);
                    elementView.expandAll();
                    elementView.setAutoWaitCursor(true);
                    setScrollOnExpand( scrollOnExpand );

                    if (PERF_LOG.isLoggable(Level.FINE)) {
                        final long tm2 = System.currentTimeMillis();
                        final long tm1 = lastShowWaitNodeTime;
                        if (tm1 != -1) {
                            lastShowWaitNodeTime = -1;
                            PERF_LOG.log(Level.FINE,
                                String.format("ClassMemberPanelUI refresh took: %d ms", (tm2 - tm1)),
                                new Object[] { description.getFileObject().getName(), (tm2 - tm1) });
                        }
                    }
                }
            } );
            
        }
    }
    
    public void sort() {
        ElementNode root = getRootNode();
        if( null != root )
            root.refreshRecursively();
    }
    
    public ClassMemberFilters getFilters() {
        return filters;
    }
    
    public void expandNode( Node n ) {
        elementView.expandNode(n);
    }
    
    public Action[] getActions() {
        return actions;
    }
    
    public FileObject getFileObject() {
        final ElementNode root = getRootNode();
        if (root != null) {
            return root.getDescritption().fileObject;
        }
        else {
            return null;
        }        
    }
    
    // FilterChangeListener ----------------------------------------------------
    
    public void filterStateChanged(ChangeEvent e) {
        ElementNode root = getRootNode();
        
        if ( root != null ) {
            root.refreshRecursively();
        }
    }
    
    boolean getScrollOnExpand() {
        return null == elementView ? true : elementView.getScrollOnExpand();
    }
    
    void setScrollOnExpand( boolean scroll ) {
        if( null != elementView )
            elementView.setScrollOnExpand( scroll );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    // Private methods ---------------------------------------------------------
   
    private ElementNode getRootNode() {
        
        Node n = manager.getRootContext();
        if ( n instanceof ElementNode ) {
            return (ElementNode)n;
        }
        else {
            return null;
        }
    }
    
    private MyBeanTreeView createBeanTreeView() {
        return new MyBeanTreeView();
    }

    private void scheduleJavadocRefresh(final int time) {
        jdocFinder.cancel();
        jdocTask.schedule(time);
    }

    private void schedule(@NonNull final Callable<Pair<URI, ElementHandle<TypeElement>>> resolver) {
        showWaitNode();
        final Future<Pair<URI, ElementHandle<TypeElement>>> becomesHandle = RP.submit(resolver);
        final RefreshTask refresh = new RefreshTask(becomesHandle);
        RP.execute(refresh);
    }
    
    // ExplorerManager.Provider imlementation ----------------------------------
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    protected ElementJavadoc getJavaDocFor(
            @NonNull final ElementNode node,
            @NullAllowed final Callable<Boolean> cancel) {
        ElementNode root = getRootNode();
        if ( root == null ) {
            return null;
        }
        
        ElementHandle<? extends Element> eh = node.getDescritption().elementHandle;

        final JavaSource js = JavaSource.forFileObject( root.getDescritption().fileObject );
        if (js == null) {
            return null;
        }
        final JavaDocCalculator calculator = new JavaDocCalculator(eh, cancel);
        try {
            js.runUserActionTask( calculator, true );
        } catch( IOException ioE ) {
            Exceptions.printStackTrace( ioE );
            return null;
        }
        return calculator.doc;
        
    }
    
    private static class JavaDocCalculator implements Task<CompilationController> {

        private final ElementHandle<? extends Element> handle;
        private final Callable<Boolean> cancel;
        private ElementJavadoc doc;
        
        public JavaDocCalculator(
                @NonNull final ElementHandle<? extends Element> handle,
                @NullAllowed final Callable<Boolean> cancel) {
            this.handle = handle;
            this.cancel = cancel;
        }


        @Override
        public void run(CompilationController cc) throws Exception {
            cc.toPhase( JavaSource.Phase.UP_TO_DATE );
            
            Element e = handle.resolve( cc );
            doc = ElementJavadoc.create(cc, e, cancel );
        }
    };
        
    private class MyBeanTreeView extends BeanTreeView implements ToolTipManagerEx.ToolTipProvider {

        private final ToolTipManagerEx toolTipManager;
        
        public MyBeanTreeView() {
            toolTipManager = new ToolTipManagerEx( this );
        }
        
        public boolean getScrollOnExpand() {
            return tree.getScrollsOnExpand();
}
        
        public void setScrollOnExpand( boolean scroll ) {
            this.tree.setScrollsOnExpand( scroll );
        }
        
        public JComponent getComponent() {
            return tree;
        }

        public String getToolTipText(Point loc) {
            ElementJavadoc doc = getDocumentation(loc);
            return null == doc ? null : doc.getText();
        }
        
        private ElementJavadoc getDocumentation( Point loc ) {
            TreePath path = tree.getPathForLocation( loc.x, loc.y );
            if( null == path )
                return null;
            Node node = Visualizer.findNode( path.getLastPathComponent() );
            if( node instanceof ElementNode ) {
                return getJavaDocFor((ElementNode)node, toolTipManager);
            }
            return null;
        }

        public Rectangle getToolTipSourceBounds(Point loc) {
            ElementNode root = getRootNode();
            if ( root == null ) {
                return null;
            }
            TreePath path = tree.getPathForLocation( loc.x, loc.y );
            return null == path ? null : tree.getPathBounds( path );
        }
        
        public Point getToolTipLocation( Point mouseLocation, Dimension tipSize ) {
            Point screenLocation = getLocationOnScreen();
            Rectangle sBounds = getGraphicsConfiguration().getBounds();
            Dimension compSize = getSize();
            Point res = new Point();
            Rectangle tooltipSrcRect = getToolTipSourceBounds( mouseLocation );
            //May be null, prevent the NPE, nothing will be shown anyway.
            if (tooltipSrcRect == null) {
                tooltipSrcRect = new Rectangle();
            }

            Point viewPosition = getViewport().getViewPosition();
            screenLocation.x -= viewPosition.x;
            screenLocation.y -= viewPosition.y;
            
            //first try bottom right
            res.x = screenLocation.x + compSize.width;
            res.y = screenLocation.y + tooltipSrcRect.y+tooltipSrcRect.height;

            if( res.x + tipSize.width <= sBounds.x+sBounds.width
                    && res.y + tipSize.height <= sBounds.y+sBounds.height ) {
                return res;
            }

            //upper right
            res.x = screenLocation.x + compSize.width;
            res.y = screenLocation.y + tooltipSrcRect.y - tipSize.height;

            if( res.x + tipSize.width <= sBounds.x+sBounds.width
                    && res.y >= sBounds.y ) {
                return res;
            }

            //lower left
            res.x = screenLocation.x - tipSize.width;
            res.y = screenLocation.y + tooltipSrcRect.y;

            if( res.x >= sBounds.x
                    && res.y + tipSize.height <= sBounds.y+sBounds.height ) {
                return res;
            }

            //upper left
            res.x = screenLocation.x - tipSize.width;
            res.y = screenLocation.y + tooltipSrcRect.y + tooltipSrcRect.height - tipSize.height;

            if( res.x >= sBounds.x && res.y >= sBounds.y ) {
                return res;
            }

            //give up (who's got such a small display anyway?)
            res.x = screenLocation.x + tooltipSrcRect.x;
            if( sBounds.y + sBounds.height - (screenLocation.y + tooltipSrcRect.y + tooltipSrcRect.height) 
                > screenLocation.y + tooltipSrcRect.y - sBounds.y ) {
                res.y = screenLocation.y + tooltipSrcRect.y + tooltipSrcRect.height;
            } else {
                res.y = screenLocation.y + tooltipSrcRect.y - tipSize.height;
            }

            return res;
        }

        public void invokeUserAction(final MouseEvent me) {
            Mutex.EVENT.readAccess( new Runnable() {
                public void run() {
                    if( null != me ) {
                        ElementJavadoc doc = getDocumentation( me.getPoint() );
                        JavadocTopComponent tc = JavadocTopComponent.findInstance();
                        if( null != tc ) {
                            tc.open();
                            tc.setJavadoc( doc );
                            tc.requestActive();
                        }
                    }
                }
            });
        }

        //#123940 start
        private boolean inHierarchy;
        private boolean doExpandAll;
        
        @Override
        public void addNotify() {
            super.addNotify();
            
            inHierarchy = true;
            
            if (doExpandAll) {
                super.expandAll();
                doExpandAll = false;
            }
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            inHierarchy = false;
            this.toolTipManager.hideTipWindow();
        }

        @Override
        public void expandAll() {
            super.expandAll();
            
            if (!inHierarchy) {
                doExpandAll = true;
            }
        }
        //#123940 end
        
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            final Node[] oldNodes = (Node[]) evt.getOldValue();
            final Node[] newNodes = (Node[]) evt.getNewValue();
            for (Node n : oldNodes) {
                selectedNodes.remove(n);
            }
            for (Node n : newNodes) {
                selectedNodes.add(n);
            }
            if (newNodes.length > 0 && JavadocTopComponent.shouldUpdate()) {
                scheduleJavadocRefresh(JDOC_TIME);
            }
        } else if (TapPanel.EXPANDED_PROPERTY.equals(evt.getPropertyName())) {
            NbPreferences.forModule(ClassMemberPanelUI.class)
                    .putBoolean("filtersPanelTap.expanded", filtersPanel.isExpanded());
        }
    }
    
    private enum State {
        SCHEDULED,
        INVOKED,
        DONE
    }

    private class RefreshTask implements Runnable, Task<CompilationController> {

        private final Future<Pair<URI,ElementHandle<TypeElement>>> becomesHandle;

        RefreshTask(@NonNull final Future<Pair<URI,ElementHandle<TypeElement>>> becomesHandle) {
            assert becomesHandle != null;
            this.becomesHandle = becomesHandle;
        }

        @Override
        @NbBundle.Messages({
        "ERR_Cannot_Resolve_File=Cannot resolve type: {0}.",
        "ERR_Not_Declared_Type=Not a declared type."})
        public void run() {
            try {
                final Pair<URI,ElementHandle<TypeElement>> handlePair = becomesHandle.get();
                if (handlePair != null) {
                    final FileObject fo = URLMapper.findFileObject(handlePair.first.toURL());
                    if (fo != null) {
                        final ClasspathInfo cpInfo = ClasspathInfo.create(fo);
                        final FileObject target = SourceUtils.getFile(handlePair.second, cpInfo);
                        if (target != null) {
                            final JavaSource targetJs = JavaSource.forFileObject(target);
                            if (targetJs != null) {
                                history.addToHistory(handlePair);
                                targetJs.runUserActionTask(this, true);
                                ((Toolbar)getToolbar()).select(handlePair);
                            } else {
                                clearNodes();
                                StatusDisplayer.getDefault().setStatusText(Bundle.ERR_Cannot_Resolve_File(
                                    handlePair.second.getQualifiedName()));
                            }
                        } else {
                            clearNodes();
                            StatusDisplayer.getDefault().setStatusText(Bundle.ERR_Cannot_Resolve_File(
                                    handlePair.second.getQualifiedName()));
                        }
                    } else {
                        clearNodes();
                        StatusDisplayer.getDefault().setStatusText(Bundle.ERR_Cannot_Resolve_File(
                                    handlePair.second.getQualifiedName()));
                    }
                } else {
                    clearNodes();
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

        @Override
        public void run(@NonNull final CompilationController cc) throws Exception {
            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            getTask().run(cc);
        }

    }

    private class Toolbar extends JPanel implements ActionListener {

        private final JComboBox historyCombo;

        @NbBundle.Messages({
        "TXT_OpenJDoc=Open Javadoc Window",
        })
        Toolbar() {
            setLayout(new GridBagLayout());
            final Box box = new Box(BoxLayout.X_AXIS);
            box.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 5));
            final JToolBar toolbar = new NoBorderToolBar(JToolBar.HORIZONTAL);
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setBorderPainted(false);
            toolbar.setBorder(BorderFactory.createEmptyBorder());
            toolbar.setOpaque(false);
            toolbar.setFocusable(false);
            historyCombo = new JComboBox(HistorySupport.createModel(history)){
                @Override
                public Dimension getMinimumSize() {
                    Dimension res = super.getMinimumSize();
                    if (res.width > MIN_HISTORY_WIDTH) {
                        res = new Dimension(MIN_HISTORY_WIDTH, res.height);
                    }
                    return res;
                }
            };
            historyCombo.setRenderer(HistorySupport.createRenderer(history));
            historyCombo.setActionCommand(CMD_HISTORY);
            historyCombo.addActionListener(this);
            toolbar.add(historyCombo);
            final JButton jdocButton = new JButton(ImageUtilities.loadImageIcon(JDOC_ICON, true));
            jdocButton.setActionCommand(CMD_JDOC);
            jdocButton.addActionListener(this);
            jdocButton.setFocusable(false);
            jdocButton.setToolTipText(Bundle.TXT_OpenJDoc());
            toolbar.add(jdocButton);
            box.add (toolbar);
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
            add(box,c);
            if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (CMD_JDOC.equals(e.getActionCommand())) {
                final TopComponent win = JavadocTopComponent.findInstance();
                if (win != null && !win.isShowing()) {
                    win.open();
                    win.requestVisible();
                    scheduleJavadocRefresh(0);
                }
            } else if (CMD_HISTORY.equals(e.getActionCommand())) {
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
        }

        void select(@NonNull final Pair<URI,ElementHandle<TypeElement>> pair) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    historyCombo.getModel().setSelectedItem(pair);
                }
            });
        }
    }
}
