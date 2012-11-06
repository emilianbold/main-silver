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
package org.netbeans.modules.css.visual;

import java.awt.BorderLayout;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelUtils;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.visual.RuleEditorNode.DeclarationProperty;
import org.netbeans.modules.css.visual.actions.AddPropertyAction;
import org.netbeans.modules.css.visual.actions.CreateRuleAction;
import org.netbeans.modules.css.visual.actions.DeleteRuleAction;
import org.netbeans.modules.css.visual.actions.GoToSourceAction;
import org.netbeans.modules.css.visual.actions.RemovePropertyAction;
import org.netbeans.modules.css.visual.api.DeclarationInfo;
import org.netbeans.modules.css.visual.api.RuleEditorController;
import org.netbeans.modules.css.visual.api.SortMode;
import org.netbeans.modules.css.visual.filters.FilterSubmenuAction;
import org.netbeans.modules.css.visual.filters.FiltersManager;
import org.netbeans.modules.css.visual.filters.FiltersSettings;
import org.netbeans.modules.css.visual.filters.RuleEditorFilters;
import org.netbeans.modules.css.visual.filters.SortActionSupport;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

/**
 * Rule editor panel is a {@link JPanel} component which can be embedded in the
 * client's UI.
 *
 * It can be controlled and observed via {@link RuleEditorPanelController} and
 * {@link PropertyChangeListener}.
 *
 * Open questions/todo-s: -----------------------
 *
 * 2) (P4) related to #1 is how to listen on events happening over the sheet -
 * implementing the mouse hover based "disable" action (maybe not necessary
 * since doesn't make much sense for the rule editor).
 *
 * 3) (P2) add own (propagate the filters) popup menu to the sheet
 *
 * 4) (P4) (#EA) can property categories be programmatically collapsed/expanded?
 *
 * 5) (P3) in the unsorted mode, can be the categories disabled? They seem to
 * disappear only in the "sort by alpha" mode
 *
 * Enhancements: -------------- A) if categorized view enabled, the category
 * name containing a physical properties could be in bold font and the rest is
 * collapsed (possibly configurable by a toolbar toggle)
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "titleLabel.text={0} properties",
    "titleLabel.text.no.selected.rule=No Rule Selected",
    "titleLabel.tooltip.no.selected.rule=Select a css rule in editor or CSS Styles Window to activate the Rule Editor",
    "titleLabel.no.selected.rule=No Rule Selected",
    "label.rule.error.tooltip=The selected rule contains error(s), the listed properties are read only",
    "addPropertyCB.initial.text=Add Property ..."
})
public class RuleEditorPanel extends JPanel {

    private static final String RULE_EDITOR_LOGGER_NAME = "rule.editor"; //NOI18N
    public static final Logger LOG = Logger.getLogger(RULE_EDITOR_LOGGER_NAME);
    
    static RequestProcessor RP = new RequestProcessor(CssCaretAwareSourceTask.class);
    
    private static final Icon ERROR_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/css/visual/resources/error-glyph.gif")); //NOI18N
    private static final Icon APPLIED_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/css/visual/resources/database.gif")); //NOI18N
    private static final JLabel ERROR_LABEL = new JLabel(ERROR_ICON);
    private static final JLabel APPLIED_LABEL = new JLabel(APPLIED_ICON);

    static {
        ERROR_LABEL.setToolTipText(Bundle.label_rule_error_tooltip());
    }
//    private static final Color defaultPanelBackground = javax.swing.UIManager.getDefaults().getColor("Panel.background"); //NOI18N
    private REPropertySheet sheet;
    private Model model;
    private Rule rule;
    private Action addPropertyAction;
    private Action addRuleAction;
    private Action removeRuleAction;
    private Action[] actions;
    private JPopupMenu popupMenu;
    private RuleEditorFilters filters;
    private boolean showAllProperties, showCategories;
    private SortMode sortMode;
    public RuleEditorNode node;
    private PropertyChangeSupport CHANGE_SUPPORT = new PropertyChangeSupport(this);
    private boolean addPropertyMode;
   
    private Declaration createdDeclaration;
    private List<String> createdDeclarationsIdsList = new ArrayList<String>();
    
    private PropertyChangeListener MODEL_LISTENER = new PropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    if (Model.NO_CHANGES_APPLIED_TO_DOCUMENT.equals(evt.getPropertyName())) {
                        //Model.applyChanges() requested, but no changes were done,
                        //which means no new model will be created and hence no property sets refreshed
                        node.fireContextChanged(false);
                        
                    } else if (Model.CHANGES_APPLIED_TO_DOCUMENT.equals(evt.getPropertyName())) {
                        northWestPanel.add(APPLIED_LABEL);
                        northWestPanel.revalidate();
                        northWestPanel.repaint();

                        //re-set the css model as the CssCaretAwareSourceTask won't work 
                        //if the modified file is not opened in editor
                        Model model = getModel();
                        if (model != null) {
                            Document doc = model.getLookup().lookup(Document.class);
                            if (doc != null) {
                                try {
                                    Source source = Source.create(doc);
                                    ParserManager.parse(Collections.singleton(source), new UserTask() {
                                        @Override
                                        public void run(ResultIterator resultIterator) throws Exception {
                                            resultIterator = WebUtils.getResultIterator(resultIterator, "text/css");
                                            if (resultIterator != null) {
                                                CssParserResult result = (CssParserResult) resultIterator.getParserResult();
                                                final Model model = Model.getModel(result);
                                                LOG.log(Level.INFO, "Model.CHANGES_APPLIED_TO_DOCUMENT event handler - setting new model {0}", model);
                                                setModel(model);
                                            }
                                        }
                                    });
                                } catch (ParseException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    } else if (Model.MODEL_WRITE_TASK_FINISHED.equals(evt.getPropertyName())) {
                        if (createdDeclaration != null) {
                            //select & edit the property corresponding to the created declaration
                            node.fireContextChanged(false);
                            editCreatedDeclaration();
                        }
                    }
                }
            });
        }
    };
    
    public RuleEditorPanel() {
        this(false);
    }

    public RuleEditorPanel(boolean addPropertyMode) {

        this.addPropertyMode = addPropertyMode;
        FiltersSettings filtersSettings = addPropertyMode
                ? new FiltersSettings(false, false, true)
                : new FiltersSettings();

        node = new RuleEditorNode(this);

        sortMode = SortMode.ALPHABETICAL;

        filters = new RuleEditorFilters(this, filtersSettings);
        filters.getInstance().hookChangeListener(new FiltersManager.FilterChangeListener() {
            @Override
            public void filterStateChanged(ChangeEvent e) {
                updateFiltersPresenters();
            }
        });

        //initialize actions
        addPropertyAction = new AddPropertyAction(this);
        addRuleAction = new CreateRuleAction(this);
        removeRuleAction = new DeleteRuleAction(this);

        //keep actions status
        addRuleEditorListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(RuleEditorController.PropertyNames.MODEL_SET.name())) {
                    addRuleAction.setEnabled(evt.getNewValue() != null);
                } else if (evt.getPropertyName().equals(RuleEditorController.PropertyNames.RULE_SET.name())) {
                    addPropertyAction.setEnabled(evt.getNewValue() != null);
                    removeRuleAction.setEnabled(evt.getNewValue() != null);
                }
            }
        });

        actions = new Action[]{
            addPropertyAction,
            addRuleAction,
            removeRuleAction,
            null,
            new SortActionSupport.NaturalSortAction(filters),
            new SortActionSupport.AlphabeticalSortAction(filters),
            null,
            new FilterSubmenuAction(filters)
        };

        //custom popop for the whole panel
        //TODO possibly use some NB way, but I don't know it, no time for exploring now...
        JPopupMenu pm = new JPopupMenu();
        for (Action action : actions) {
            if (action != null) {
                if (action instanceof Presenter.Popup) {
                    pm.add(((Presenter.Popup) action).getPopupPresenter());
                } else {
                    pm.add(action);
                }
            } else {
                pm.addSeparator();
            }
        }

        setComponentPopupMenu(pm);

        //custom popup for the "menu icon"
        popupMenu = new JPopupMenu();
        popupMenu.add(addPropertyAction);
        popupMenu.add(addRuleAction);
        popupMenu.add(removeRuleAction);

        //init default components
        initComponents();

 
        if (!addPropertyMode) {
            northEastPanel.add(menuLabel, java.awt.BorderLayout.EAST);
            menuLabel.setComponentPopupMenu(popupMenu);
        }

        titleLabel.setText(null);

        //add the property sheet to the center
        sheet = new REPropertySheet(popupMenu);
        try {
            sheet.setSortingMode(PropertySheet.UNSORTED);
        } catch (PropertyVetoException ex) {
            //no-op
        }
        sheet.setPopupEnabled(true);
        sheet.setDescriptionAreaVisible(false);
        sheet.setNodes(new Node[]{node});

        add(sheet, BorderLayout.CENTER);

        if(addPropertyMode) {
            northWestPanel.remove(titleLabel);
            northWestPanel.add(filterTextField, BorderLayout.CENTER);
            cancelFilterLabel.setBorder(new EmptyBorder(0,4,0,8));
            northWestPanel.add(cancelFilterLabel, BorderLayout.WEST);
        }
        
        northEastPanel.add(filters.getComponent(), BorderLayout.WEST);

        updateFiltersPresenters();

        //add document listener to the filter text field 
        filterTextField.getDocument().addDocumentListener(new DocumentListener() {
 
            private void contentChanged() {
                node.setFilterText(filterTextField.getText());
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                contentChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                contentChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
       
    }
    
    //called fro the containing TC's componentDeactivated();
    public void componentDeactivated() {
        //Support for clearing the "created declarations list".
        //
        //When user adds new properties using the "Add Property" item 
        //at the end of the PS the items stays at the positions unsorted
        //At some point we need to resort the items according to their 
        //alphabetical order - lets do that when the TopComponent containing
        //rhe RuleEditor panel lost focus.
        createdDeclarationsIdsList.clear();
        node.fireContextChanged(true);
    }
    
    public FeatureDescriptor getSelected() {
        return sheet.getSelectedFeatureDescriptor();
    }
    
    void setCreatedDeclaration(Rule rule, Declaration declaration) {
        createdDeclaration = declaration;
        
        String declarationId = PropertyUtils.getDeclarationId(rule, declaration);
        createdDeclarationsIdsList.add(declarationId);
    }
    
    Declaration getCreatedDeclaration() {
        return this.createdDeclaration;
    }
    
    List<String> getCreatedDeclarationsIdsList() {
        return createdDeclarationsIdsList;
    }
    
    private void editCreatedDeclaration() {
        DeclarationProperty descriptor = node.getDeclarationProperty(createdDeclaration);
        assert descriptor != null;
        
        sheet.requestFocus();
//        sheet.select(descriptor, true);
        try {
            call_PropertySheet_select(sheet, descriptor, true);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        
        createdDeclaration = null;
    }
    
    private void call_PropertySheet_select(PropertySheet sheet, FeatureDescriptor descriptor, boolean edit) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        //private so far, will be public later
        Class clz = PropertySheet.class;
        Method select_method = clz.getDeclaredMethod("select", FeatureDescriptor.class, boolean.class); //NOI18N
        select_method.setAccessible(true);
        select_method.invoke(sheet, descriptor, edit);
    }

    public final void updateFiltersPresenters() {
        if (filters.getSettings().isShowCategoriesEnabled()) {
            setShowCategories(filters.getInstance().isSelected(RuleEditorFilters.SHOW_CATEGORIES));
        }
        if (filters.getSettings().isShowAllPropertiesEnabled()) {
            setShowAllProperties(filters.getInstance().isSelected(RuleEditorFilters.SHOW_ALL_PROPERTIES));
        }
    }

    public boolean isAddPropertyMode() {
        return addPropertyMode;
    }

    public SortMode getSortMode() {
        return sortMode;
    }

    public void setSortMode(SortMode mode) {
        if (this.sortMode == mode) {
            return; //no change
        }
        this.sortMode = mode;
        node.fireContextChanged(true);
    }

    public boolean isShowAllProperties() {
        return showAllProperties;
    }

    public void setShowAllProperties(boolean showAllProperties) {
        if (this.showAllProperties == showAllProperties) {
            return; //no change
        }
        this.showAllProperties = showAllProperties;
        node.fireContextChanged(true);
    }

    public boolean isShowCategories() {
        return showCategories;
    }

    public void setShowCategories(boolean showCategories) {
        if (this.showCategories == showCategories) {
            return; //no change
        }
        this.showCategories = showCategories;
        node.fireContextChanged(true);
    }

    public Model getModel() {
        return model;
    }
    
    public void releaseModel() {
        if(model == null) {
            return ;
        }
        setNoRuleState();
        model.removePropertyChangeListener(MODEL_LISTENER);
        this.model = null;
    }

    //runs in EDT
    public void setModel(final Model model) {
        assert SwingUtilities.isEventDispatchThread();
        if (model == null) {
            throw new NullPointerException();
        }

        if (this.model != null) {
            //new model for the same file, check if the model is not the same
            //as the current one
            if(model.getSerialNumber() == this.model.getSerialNumber()) {
                LOG.log(Level.FINE, "attempt to set the same model");
                return; //no change
            }
            
            //check if the set model is not even older than the curren one
            //if the model is for the same file
            FileObject old = this.model.getLookup().lookup(FileObject.class);
            FileObject neww = model.getLookup().lookup(FileObject.class);
            assert old != null; 
            assert neww != null;
            if(neww != null && neww.equals(old)) {
                if(model.getSerialNumber() < this.model.getSerialNumber()) { //or even older!
                    LOG.log(Level.WARNING, "attempt to set the older model {0} while the current is {1}!!!", new Object[]{model, this.model});
                    return; //no change
                }
            }
            
            this.model.removePropertyChangeListener(MODEL_LISTENER);
        }

        final Model oldModel = this.model;
        final Rule oldRule = this.rule;

        this.model = model;
        LOG.log(Level.FINE, "set new model ({0})", model);

        this.model.addPropertyChangeListener(MODEL_LISTENER);

        //remove the "applied changes mark"
        northWestPanel.remove(APPLIED_LABEL);
        northWestPanel.validate();
        northWestPanel.repaint();

        CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.MODEL_SET.name(), oldModel, this.model);

        if (this.rule != null) {
            //resolve the old rule from the previous model to corresponding rule in the new model
            final AtomicReference<Rule> rule_ref = new AtomicReference<Rule>();
            this.model.runReadTask(new Model.ModelTask() {
                @Override
                public void run(StyleSheet styleSheet) {
                    ModelUtils utils = new ModelUtils(model);
                    rule_ref.set(utils.findMatchingRule(oldModel, oldRule));
                }
            });
            
            Rule match = rule_ref.get();
            if (match == null) {
                setNoRuleState();
            } else {
                setRule(match);
            }
            
            //isn't this unnecessary as we already called setNoRuleState() or setRule(...)?!?!
            CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.RULE_SET.name(), oldRule, match);

        } else {
            LOG.log(Level.FINE, "no rule was set before");
            //no rule was set - fire event anyway
            CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.RULE_SET.name(), oldRule, rule);
        }

        //do not fire change event since it is required
        //to call setRule(...) subsequently which will 
        //fire the change even
    }

    public Rule getRule() {
        return rule;
    }
    
    public void setRule(final Rule rule) {
        assert SwingUtilities.isEventDispatchThread();
        if (rule == null) {
            throw new NullPointerException();
        }
        if (model == null) {
            throw new IllegalStateException("you must call setModel(Model model) beforehand!"); //NOI18N
        }

        Model ruleModel = rule.getModel();
        if(ruleModel != this.model) {
            LOG.log(Level.FINE, "attempt to set rule from different model {0}, while the current is {1}!", new Object[]{ruleModel, this.model});
            return; //no change
        }
        
        if (this.rule == rule) {
            LOG.log(Level.FINE, "attempt to set the same rule");
            return; //no change
        }
        Rule old = this.rule;
        this.rule = rule;
        LOG.log(Level.FINE, "set new rule ({0})", rule);
        
        CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.RULE_SET.name(), old, this.rule);

        northWestPanel.revalidate();

        //force property sets refresh if the rule is erroneous or 
        //the rule is valid, but the previous was erroneous.
        node.fireContextChanged(old == null || !(old.isValid() && rule.isValid())); 

        final AtomicReference<String> ruleNameRef = new AtomicReference<String>();
        model.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet stylesheet) {
                ruleNameRef.set(model.getElementSource(rule.getSelectorsGroup()).toString());
            }
        });
        titleLabel.setText(Bundle.titleLabel_text(ruleNameRef.get()));
        titleLabel.setToolTipText(null);
        titleLabel.setEnabled(true);

    }

    public void setNoRuleState() {
        LOG.log(Level.FINE, "setNoRuleState()");

        assert SwingUtilities.isEventDispatchThread();
        Rule old = this.rule;
        this.rule = null;
        CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.RULE_SET.name(), old, null);
 
        titleLabel.setText(Bundle.titleLabel_no_selected_rule());
        titleLabel.setToolTipText(Bundle.titleLabel_tooltip_no_selected_rule());
        titleLabel.setEnabled(false);
        
        node.fireContextChanged(false);
    }

    public void setDeclarationInfo(Declaration declaration, DeclarationInfo declarationInfo) {
        node.fireDeclarationInfoChanged(declaration, declarationInfo);
    }

    /**
     * Registers an instance of {@link PropertyChangeListener} to the component.
     *
     * @param listener
     */
    public final void addRuleEditorListener(PropertyChangeListener listener) {
        CHANGE_SUPPORT.addPropertyChangeListener(listener);
    }

    /**
     * Unregisters an instance of {@link PropertyChangeListener} from the
     * component.
     *
     * @param listener
     */
    public final void removeRuleEditorListener(PropertyChangeListener listener) {
        CHANGE_SUPPORT.removePropertyChangeListener(listener);
    }

    public Action[] getActions() {
        return actions;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menuLabel = new javax.swing.JLabel();
        cancelFilterLabel = new javax.swing.JLabel();
        filterTextField = new javax.swing.JTextField();
        northPanel = new javax.swing.JPanel();
        northEastPanel = new javax.swing.JPanel();
        northWestPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();

        menuLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/css/visual/resources/menu.png"))); // NOI18N
        menuLabel.setText(org.openide.util.NbBundle.getMessage(RuleEditorPanel.class, "RuleEditorPanel.menuLabel.text")); // NOI18N
        menuLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 0, 0));
        menuLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuLabelMouseClicked(evt);
            }
        });

        cancelFilterLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/css/visual/resources/cancel.png"))); // NOI18N
        cancelFilterLabel.setText(org.openide.util.NbBundle.getMessage(RuleEditorPanel.class, "RuleEditorPanel.cancelFilterLabel.text")); // NOI18N
        cancelFilterLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cancelFilterLabelMouseClicked(evt);
            }
        });

        filterTextField.setText(org.openide.util.NbBundle.getMessage(RuleEditorPanel.class, "RuleEditorPanel.filterTextField.text")); // NOI18N
        filterTextField.setMaximumSize(new java.awt.Dimension(32767, 32767));
        filterTextField.setMinimumSize(new java.awt.Dimension(60, 28));

        setPreferredSize(new java.awt.Dimension(400, 300));
        setLayout(new java.awt.BorderLayout());

        northPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        northPanel.setLayout(new java.awt.BorderLayout());

        northEastPanel.setLayout(new java.awt.BorderLayout());
        northPanel.add(northEastPanel, java.awt.BorderLayout.EAST);

        northWestPanel.setLayout(new javax.swing.BoxLayout(northWestPanel, javax.swing.BoxLayout.LINE_AXIS));

        titleLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        titleLabel.setMaximumSize(null);
        titleLabel.setPreferredSize(new java.awt.Dimension(100, 16));
        northWestPanel.add(titleLabel);

        northPanel.add(northWestPanel, java.awt.BorderLayout.CENTER);

        add(northPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void menuLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuLabelMouseClicked
        //just invoke popup as if right-clicked
        popupMenu.show(menuLabel, 0, 0);
    }//GEN-LAST:event_menuLabelMouseClicked

    private void cancelFilterLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelFilterLabelMouseClicked
        filterTextField.setText(null);
    }//GEN-LAST:event_cancelFilterLabelMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cancelFilterLabel;
    private javax.swing.JTextField filterTextField;
    private javax.swing.JLabel menuLabel;
    private javax.swing.JPanel northEastPanel;
    private javax.swing.JPanel northPanel;
    private javax.swing.JPanel northWestPanel;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables

    private class REPropertySheet extends PropertySheet {

        private final JPopupMenu genericPopupMenu;

        public REPropertySheet(JPopupMenu genericPopupMenu) {
            this.genericPopupMenu = genericPopupMenu;
        }
        
        public FeatureDescriptor getSelectedFeatureDescriptor() {
            return super.getSelection();
        }
        
        @Override
        protected JPopupMenu createPopupMenu() {
            FeatureDescriptor fd = getSelection();
            if(fd != null) {
                if(fd instanceof RuleEditorNode.DeclarationProperty) {
                    //property
                    //
                    //actions:
                    //remove
                    //hide
                    //????
                    //custom popop for the whole panel
                    JPopupMenu pm = new JPopupMenu();
                    
                    pm.add(new GoToSourceAction(RuleEditorPanel.this, (RuleEditorNode.DeclarationProperty)fd));
                    pm.addSeparator();
                    pm.add(new RemovePropertyAction(RuleEditorPanel.this, (RuleEditorNode.DeclarationProperty)fd));

                    return pm;
                    
                } else if(fd instanceof RuleEditorNode.PropertyCategoryPropertySet) {
                    //property category
                    //TODO possibly add "add property" action which would
                    //preselect the css category in the "add property dialog".
                }
            }            
            
            //no context popup - create the generic popup
            return genericPopupMenu;
        }
        
    }
}
