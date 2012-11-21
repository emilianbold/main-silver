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
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.ElementFactory;
import org.netbeans.modules.css.model.api.Media;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelUtils;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.Selector;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.elements.TreePath;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Create CSS Rule panel.
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "selector.type.class=Class",
    "selector.type.id=Id",
    "selector.type.element=Element",
    "selector.type.compound=Compound",
    "selector.rule.postfix= Rule",
    "none.item=<html><font color=\"777777\">&lt;none&gt;</font></html>",
    "class.selector.descr=Applies to all elements with this style class assigned.",
    "id.selector.descr=Applies just to one single element with this id set.",
    "element.selector.descr=Applies to html elements with the selector name.",
    "compound.selector.descr=The Compound selector is used to create styles for a combination of tags or tags that are nested in other tags."
})
public class CreateRulePanel extends javax.swing.JPanel {

    private static final Logger LOGGER = Logger.getLogger(CreateRulePanel.class.getSimpleName());
    
    //TODO take the values from editor settings!
    private static Color tagColor = new Color(0, 0, 230);
    private static Color attrNameColor = new Color(0, 153, 0);
    private static Color attrValueColor = new Color(206, 123, 0);
    
    /**
     * Default value of the compound selector - derived from selected html source element.
     */
    private String compoundSelectorDefaultValue;
    
    /**
     * Active html source element.
     */
    private HtmlSourceElementHandle activeElement;
    private AtomicReference<String> activeElementClass, activeElementId;
    
    /**
     * SelectorItem for each of the supported selector type. 
     * null if not modified/defined by user.
     */
    private SelectorItem selectedClazz, selectedId, selectedElement, selectedCompound;
    
    /**
     * SelectorItem-s for "no class".
     */
    private final SelectorItem NO_CLASS = SelectorItem.createClass(null, null);
    /**
     * SelectorItem-s for "no id".
     */
    private final SelectorItem NO_ID = SelectorItem.createId(null, null);
    
    /**
     * Textual descriptions of the selector types.
     */
    private String[] SELECTOR_TYPE_DESCRIPTIONS = new String[]{
        Bundle.class_selector_descr(),
        Bundle.id_selector_descr(),
        Bundle.element_selector_descr(),
        Bundle.compound_selector_descr()
    };
    
    /**
     * ComboBoxModel for the selector combobox.
     */
    private ExtDefaultComboBoxModel SELECTORS_MODEL;
    
    /**
     * ComboBoxModel for the selectors combobox.
     */
    private ExtDefaultComboBoxModel STYLESHEETS_MODEL;
    
    /**
     * ComboBoxModel for the at-rules combobox.
     */
    private ExtDefaultComboBoxModel AT_RULES_MODEL;
    
    /**
     * ListModel for the selector type listbox.
     */
    private ListModel SELECTORS_LIST_MODEL;
    
    /**
     * Context of the create rule panel.
     */
    private FileObject context;
    
    /**
     * Cached all html source element names.
     */
    private Collection<String> ELEMENT_SELECTOR_ITEMS;

    /**
     * Renderer for selector's combobox dropdown content.
     */
    private SelectorItemRenderer SELECTOR_MODEL_ITEM_RENDERER;
    
    /**
     * Creates a new instance of the panel.
     * 
     * @param context file context, must not be null.
     * @param handle html source element handle, can be null.
     */
    public CreateRulePanel(FileObject context, HtmlSourceElementHandle handle) {
        assert context != null;
        this.context = context;
        this.activeElement = handle;

        STYLESHEETS_MODEL = new ExtDefaultComboBoxModel();
        AT_RULES_MODEL = new ExtDefaultComboBoxModel();
        SELECTORS_MODEL = new ExtDefaultComboBoxModel();

        createStyleSheetsModel();

        SELECTORS_LIST_MODEL = new AbstractListModel() {
            @Override
            public int getSize() {
                return 4;
            }

            @Override
            public Object getElementAt(int i) {
                switch (i) {
                    case 0:
                        return Bundle.selector_type_class();
                    case 1:
                        return Bundle.selector_type_id();
                    case 2:
                        return Bundle.selector_type_element();
                    case 3:
                        return Bundle.selector_type_compound();
                    default:
                        throw new IllegalStateException();
                }
            }
        };

        initComponents();

        //listens on changes of the selector type list
        selectorTypeList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return; //ignore adjusting events
                }

                //enable the selectors CB once user chooses one of the selector types
                if (!selectorCB.isEnabled()) {
                    selectorCB.setEnabled(true);
                }

                //update the description
                int index = selectorTypeList.getSelectedIndex();
                descriptionPane.setText(SELECTOR_TYPE_DESCRIPTIONS[index]);


                //update the separator's title
                selectorTypeLabel.setText(selectorTypeList.getSelectedValue().toString() + Bundle.selector_rule_postfix());

                updateSelectorsModel(); //will also select active element

                //update stylesheet and at-rule models according to the selectors model selected element.
                SelectorItem activeSelectorItem = getActiveSelectorItem();
                if (activeSelectorItem != null) {
                    FileObject file = activeSelectorItem.getFile();
                    if(file != null) { //may be the NO_CLASS or NO_ID item w/o a file
                        //select active stylesheet
                        styleSheetCB.setSelectedItem(file);

                        //update current stylesheet model && at rules model 
                        updateAtRulesModel(file);
                        
                        //select the active at rule
                        AtRuleItem createInAtRule = activeSelectorItem.getCreateInAtRule();
                        if (createInAtRule != null) {
                            atRuleCB.setSelectedItem(createInAtRule);
                        } else {
                            atRuleCB.setSelectedIndex(0); //select first
                        }
                    }
                }

            }
        });

        //listens on changes in the selectors combobox
        selectorCB.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Object item = e.getItem();
                    if (item instanceof SelectorItem) {
                        //user selected sg. from drop down
                        setSelectorItem((SelectorItem) e.getItem());
                    }
                }
            }
        });

        //listens on changes in the selectors combobox' editor content,
        //the events are also fired when the value from dropdown is selected,
        //but subsequently and ItemEvent to selectorCB is fired as well!
        final ComboBoxEditor editor = selectorCB.getEditor();
        if (editor.getEditorComponent() instanceof JTextComponent) {
            JTextComponent textEditor = (JTextComponent) editor.getEditorComponent();
            Document doc = textEditor.getDocument();
            doc.addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent de) {
                    change();
                }

                @Override
                public void removeUpdate(DocumentEvent de) {
                    change();
                }

                @Override
                public void changedUpdate(DocumentEvent de) {
                    change();
                }

                private void change() {
                    //user change the selector - either by typing into the 
                    //text area or by selecting one from the dropdown
                    String item = editor.getItem().toString();

                    SelectorItem selectorItem;
                    switch (selectorTypeList.getSelectedIndex()) {
                        case 0:
                            //class
                            if (item.isEmpty()) {
                                selectorItem = NO_CLASS;
                            } else {
                                if (item.charAt(0) == '.') {
                                    item = item.substring(1);
                                }
                                selectorItem = SelectorItem.createClass(item, null);
                            }
                            break;
                        case 1:
                            //id
                            if (item.isEmpty()) {
                                selectorItem = NO_ID;
                            } else {
                                if (item.charAt(0) == '#') {
                                    item = item.substring(1);
                                }
                                selectorItem = SelectorItem.createId(item, null);
                            }
                            break;
                        case 2:
                            //element
                            if (item.isEmpty()) {
                                return;
                            }
                            selectorItem = SelectorItem.createElement(item);

                            break;
                        case 3:
                            //compound
                            if (item.isEmpty()) {
                                return;
                            }
                            selectorItem = SelectorItem.createCompound(item);

                            break;
                        default:
                            throw new IllegalStateException();
                    }

                    FileObject selectedStylesheet = (FileObject) styleSheetCB.getSelectedItem();
                    selectorItem.setCreateInFile(selectedStylesheet);
                    AtRuleItem selectedAtRule = (AtRuleItem) atRuleCB.getSelectedItem();
                    selectorItem.setCreateInAtRule(selectedAtRule);
                    setSelectorItem(selectorItem);

                }
            });
        }

        //listens on changes to the selectors combobox
        styleSheetCB.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    //user changed stylesheet in the combobox
                    //update the active SelectorItem to the selected stylesheet
                    SelectorItem activeSelectorItem = getActiveSelectorItem();
                    if (activeSelectorItem != null) {
                        activeSelectorItem.setCreateInFile(getActiveStylesheet());
                    }
                }
            }
        });

        //listens on changes to the at-rules combobox
        atRuleCB.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    //user changed at-rule in the combobox
                    //update the active SelectorItem to the selected at-rule
                    SelectorItem activeSelectorItem = getActiveSelectorItem();
                    if (activeSelectorItem != null) {
                        activeSelectorItem.setCreateInAtRule(getActiveAtRule());
                    }
                }
            }
        });


        initializeActiveElement();

        selectorTypeList.setSelectedIndex(0); //class

    }

    private Attribute getSelectedElementClass() {
        if (activeElement != null) {
            return activeElement.getOpenTag().getAttribute("class"); //NOI18N
        }
        return null;
    }

    private Attribute getSelectedElementId() {
        if (activeElement != null) {
            return activeElement.getOpenTag().getAttribute("id"); //NOI18N
        }
        return null;
    }

    /**
     * Returns value of the class attribute of the selected html source element.
     */
    private String getSelectedElementClassName() {
        if (activeElementClass == null) {
            activeElementClass = new AtomicReference<String>();
            Attribute clz = getSelectedElementClass();
            if (clz != null) {
                CharSequence unquotedValue = clz.unquotedValue();
                if (unquotedValue != null) {
                    activeElementClass.set(unquotedValue.toString());
                }
            }
        }
        return activeElementClass.get();
    }

    /**
     * Returns value of the id attribute of the selected html source element.
     */
    private String getSelectedElementIdName() {
        if (activeElementId == null) {
            activeElementId = new AtomicReference<String>();
            Attribute id = getSelectedElementId();
            if (id != null) {
                CharSequence unquotedValue = id.unquotedValue();
                if (unquotedValue != null) {
                    activeElementId.set(unquotedValue.toString());
                }
            }
        }
        return activeElementId.get();
    }

    private void initializeActiveElement() {
        if(activeElement == null) {
            //if handle was not passed in the constructor, try to obtain
            //it from the html source task
            activeElement = HtmlEditorSourceTask.getElement();
        }
        if (activeElement == null) {
            //still nothing, out of luck...
            return;
        }

        //set element's path in the UI
        StringBuilder compoundDefaultValue = new StringBuilder();
        StringBuilder elementPathLabelText = new StringBuilder();
        elementPathLabelText.append("<html><body>");

        TreePath path = new TreePath(activeElement.getOpenTag());
        for (int i = path.path().size() - 2; i >= 0; i--) { //skip the last "root" element
            Element e = path.path().get(i);
            compoundDefaultValue.append(e.id());

            elementPathLabelText.append("<font color=\"");
            elementPathLabelText.append(WebUtils.toHexCode(tagColor));
            elementPathLabelText.append("\">");
            elementPathLabelText.append("&lt;");
            elementPathLabelText.append(e.id());
            elementPathLabelText.append("&gt;");
            elementPathLabelText.append("</font>");

            if (i > 0) {
                //not last element
                compoundDefaultValue.append(' '); //NOI18N
                elementPathLabelText.append(' '); //NOI18N
            }
        }
        elementPathLabelText.append("</body></html>");

        elementPathLabel.setText(elementPathLabelText.toString());

        //enable the "apply changes to element" combobox so user may decide whether to
        //apply the changes to the element or not. If no html source elemnt is set, 
        //the checkbox remains disabled.
        applyChangesCB.setEnabled(true);

        //update the default for compound rule
        compoundSelectorDefaultValue = compoundDefaultValue.toString();

        updateElementCodeSample();
    }

    /**
     * Updates the active html source element sample code.
     * The sample content mutates as user changes class/id names.
     */
    private void updateElementCodeSample() {
        if (activeElement == null) {
            return;
        }
        StringBuilder source = new StringBuilder();

        source.append("<html><body>");
        source.append("<font color=\"");
        source.append(WebUtils.toHexCode(tagColor));
        source.append("\">");
        source.append("&lt;");
        source.append(activeElement.getOpenTag().name());
        source.append("</font>");

        String selectedClazzName = selectedClazz != null ? selectedClazz.getItemName() : null;
        String selectedElementClass = getSelectedElementClassName();

        String clz = selectedClazzName != null ? selectedClazzName : selectedElementClass;
        boolean change = selectedClazzName != null && selectedElementClass != null && !selectedClazzName.equals(selectedElementClass)
                || selectedClazzName != null && selectedElementClass == null;

        if (clz != null && !clz.isEmpty()) { //isEmpty - removed
            if (change) {
                source.append("<b>");
            }
            source.append("<font color=\"");
            source.append(WebUtils.toHexCode(attrNameColor));
            source.append("\">");
            source.append(" class=");
            source.append("</font>");
            source.append("<font color=\"");
            source.append(WebUtils.toHexCode(attrValueColor));
            source.append("\">");
            source.append("\"");
            source.append(clz);
            source.append("\"");
            source.append("</font>");
            if (change) {
                source.append("</b>");
            }
        }

        String selectedIdName = selectedId != null ? selectedId.getItemName() : null;
        String selectedElementId = getSelectedElementIdName();

        String id = selectedIdName != null ? selectedIdName : selectedElementId;
        change = selectedIdName != null && selectedElementId != null && !selectedIdName.equals(selectedElementId)
                || selectedIdName != null && selectedElementId == null;

        if (id != null && !id.isEmpty()) {
            if (change) {
                source.append("<b>");
            }
            source.append("<font color=\"");
            source.append(WebUtils.toHexCode(attrNameColor));
            source.append("\">");
            source.append(" id=");
            source.append("</font>");
            source.append("<font color=\"");
            source.append(WebUtils.toHexCode(attrValueColor));
            source.append("\">");
            source.append("\"");
            source.append(id);
            source.append("\"");
            source.append("</font>");
            if (change) {
                source.append("</b>");
            }
        }

        source.append("<font color=\"");
        source.append(WebUtils.toHexCode(tagColor));
        source.append("\">");
        source.append("&gt;");
        source.append("</font>");
        source.append("</body></html>");

        elementCodeLabel.setText(source.toString());

    }

    /**
     * Gets selected stylesheet.
     */
    private FileObject getActiveStylesheet() {
        return (FileObject) styleSheetCB.getSelectedItem();
    }

    /**
     * Gets selected at-rule.
     */
    private AtRuleItem getActiveAtRule() {
        return (AtRuleItem) atRuleCB.getSelectedItem();
    }

    /**
     * Gets instance of "active" {@link SelectorItem}.
     * What is returned depends on what type of selector is choosen by user in the UI.
     */
    private SelectorItem getActiveSelectorItem() {
        switch (selectorTypeList.getSelectedIndex()) {
            case 0:
                //class
                return selectedClazz;
            case 1:
                //id
                return selectedId;
            case 2:
                //element
                return selectedElement;
            case 3:
                //compound
                return selectedCompound;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Sets an instance of SelectorItem. 
     * Each type (class,id,element and compound)
     * are remembered separately.
     */
    private void setSelectorItem(SelectorItem selector) {
        switch (selector.getType()) {
            case SelectorItem.CLASS_TYPE:
                selectedClazz = selector;
                break;
            case SelectorItem.ID_TYPE:
                selectedId = selector;
                break;
            case SelectorItem.ELEMENT_TYPE:
                selectedElement = selector;
                break;
            case SelectorItem.COMPOUND_TYPE:
                selectedCompound = selector;
                break;
        }

        updateElementCodeSample(); //refresh UI

        FileObject existsIn = selector != null ? selector.getExistsInFile() : null;
        boolean exists = existsIn != null;
        if (exists) {
            STYLESHEETS_MODEL.setSelectedItem(existsIn);
        }

        //enable/disable the stylesheet and at-rule comboboxes
        //existing elements => they are disabled and vice versa.
        styleSheetCB.setEnabled(!exists);
        atRuleCB.setEnabled(!exists);

//        addStylesheetButton.setEnabled(!exists);
    }

    private void createStyleSheetsModel() {
        try {
            List<FileObject> items = new ArrayList<FileObject>();
            Project project = FileOwnerQuery.getOwner(context);
            if (project == null) {
                return;
            }
            CssIndex index = CssIndex.create(project);
            for (FileObject file : index.getAllIndexedFiles()) {
                if ("text/css".equals(file.getMIMEType())) {
                    items.add(file);
                }
            }

            STYLESHEETS_MODEL.setItems(items);

            //set selected item to the model
            if (items.contains(context)) { //the context may be the html file itself!
                STYLESHEETS_MODEL.setSelectedItem(context);
            } else {
                if(STYLESHEETS_MODEL.getSize() > 0) {
                    STYLESHEETS_MODEL.setSelectedIndex(0);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    /**
     * Gets an instance of css {@link Model} for the given file
     * @param file file context
     * @return non null instance of the source model.
     * @throws ParseException 
     */
    private Model getCssSourceModel(FileObject file) throws ParseException {
        final AtomicReference<Model> model_ref = new AtomicReference<Model>();
        Source source = Source.create(file);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                resultIterator = WebUtils.getResultIterator(resultIterator, "text/css");
                if (resultIterator != null) {
                    CssParserResult result = (CssParserResult) resultIterator.getParserResult();
                    model_ref.set(Model.getModel(result));
                }
            }
        });
        return model_ref.get();
    }

    /**
     * Refreshes the at-rules combobox model according to the at-rules
     * defined in the given file.
     * 
     * Does not set selected element in the model!
     * @param file file context, never null
     */
    private void updateAtRulesModel(FileObject file) {
        try {
            final Collection<AtRuleItem> items = new ArrayList<AtRuleItem>();
            items.add(null);

            final Model cssSourceModel = getCssSourceModel(file);
            cssSourceModel.runReadTask(new Model.ModelTask() {
                @Override
                public void run(StyleSheet styleSheet) {
                    ModelVisitor visitor = new ModelVisitor.Adapter() {
                        @Override
                        public void visitMedia(Media media) {
                            String displayName = cssSourceModel.getElementSource(media.getMediaQueryList()).toString();
                            items.add(new AtRuleItem(displayName, media));
                        }
                    };
                    styleSheet.accept(visitor);
                }
            });

            AT_RULES_MODEL.setItems(items);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void dumpSummary() {
        if (selectedClazz != null) {
            LOGGER.log(Level.FINE, "selected class = {0}", selectedClazz.getInfo());
        }
        if (selectedId != null) {
            LOGGER.log(Level.FINE, "selected id = {0}", selectedId.getInfo());
        }
        if (selectedElement != null) {
            LOGGER.log(Level.FINE, "selected element = {0}", selectedElement.getInfo());
        }
        if (selectedCompound != null) {
            LOGGER.log(Level.FINE, "selected compound = {0}", selectedCompound.getInfo());
        }
    }

    /**
     * Applies changes set in the dialog.
     *
     * Note: call outside of AWT thread.
     */
    public void applyChanges() {
        assert !EventQueue.isDispatchThread();

        dumpSummary();

        //Rules:
        //------
        //When the dialog is closed just one of the selector type is selected. 
        //The question is what changes should be applied if were done to the other
        //selector types:
        //
        //1. If class or id or both are modified lets apply both changes even if the selected selector type is
        //   set to element or compound when closing the dialog.
        //
        //2. if neither class nor id is modified then apply changes only for the selected selector type
        //

        try {
            //CLASS
            if (selectedClazz != null) {
                if (selectedClazz.getExistsInFile() == null && selectedClazz.getCreateInFile() != null && selectedClazz.getItemFQName() != null) {
                    //a. we need to create new rule 
                    createNewRule(selectedClazz);
                }
            }

            //ID
            if (selectedId != null) {
                if (selectedId.getExistsInFile() == null && selectedId.getCreateInFile() != null && selectedId.getItemFQName() != null) {
                    //a. we need to create new rule 
                    createNewRule(selectedId);
                }
            }

            //b. and modify the html source element
            if(Settings.getCreateRule_ApplyChangesToSelectedSourceElement()) {
                if(activeElement != null) {
                    modifySourceElement();
                }
            }

        } catch (/* IOException | ParseException */Exception e) {
            Exceptions.printStackTrace(e);
        }

        //ELEMENT || COMPOUND
        try {
            switch (selectorTypeList.getSelectedIndex()) {
                case 2:
                    //element
                    if (selectedElement != null) {
                        //create element selector in the selected location (file/at-rule)
                        createNewRule(selectedElement);
                    }

                    break;

                case 3:
                    //compound
                    if (selectedCompound != null) {
                        //create compound selector rule in selected location (file/at-rule)
                        createNewRule(selectedCompound);

                    }
                    break;
            }
        } catch (/* IOException | ParseException */Exception e) {
            Exceptions.printStackTrace(e);
        }

    }

    /**
     * Creates a new css rule.
     * Doesn't check whether the rule already exists or not.
     * 
     * @param selectorItem instance of {@link SelectorItem}
     * @throws IOException
     * @throws ParseException 
     */
    private void createNewRule(final SelectorItem selectorItem) throws IOException, ParseException {
        final FileObject createInFile = selectorItem.getCreateInFile();
        final Model cssSourceModel = getCssSourceModel(createInFile);

        cssSourceModel.runWriteTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {

                ElementFactory factory = cssSourceModel.getElementFactory();
                Selector s = factory.createSelector(selectorItem.getItemFQName());
                SelectorsGroup sg = factory.createSelectorsGroup(s);
                Declarations ds = factory.createDeclarations();
                Rule rule = factory.createRule(sg, ds);

                AtRuleItem createInAtRule = selectorItem.getCreateInAtRule();
                if (createInAtRule == null) {
                    //add to the body
                    Body body = styleSheet.getBody();
                    if (body == null) {
                        //create body if empty file
                        body = factory.createBody();
                        styleSheet.setBody(body);
                    }
                    styleSheet.getBody().addRule(rule);
                } else {
                    //add to the at-rule

                    //XXX: is it too hacky? As the underlying source model cannot 
                    //normally change during the modal dialog is opened the 
                    //media item obtained when the combobox model was created 
                    //should be the same as the model created in this method.
                    //if this is not true - the media object from the older
                    //source model would have to be resolved to the new source model.
                    Media media = createInAtRule.getMedia();
                    assert media.getModel() == cssSourceModel;

                    media.addRule(rule);
                }

                try {
                    cssSourceModel.applyChanges();
                    LOGGER.log(Level.FINE, "Created new rule {0} in file {1} (at-rule: {2}).", new Object[]{selectorItem.getItemFQName(), createInFile.getNameExt(), createInAtRule});
                    selectTheRuleInEditorIfOpened(cssSourceModel, rule);
                } catch (Exception /*ParseException, IOException, BadLocationException*/ ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

    }

    /**
     * Sets caret to the given css rule and focused the corresponding editor document if opened.
     */
    private void selectTheRuleInEditorIfOpened(final Model omodel, final Rule orule) throws DataObjectNotFoundException, ParseException {
        final FileObject file = omodel.getLookup().lookup(FileObject.class);
        DataObject dobj = DataObject.find(file);
        final EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        //first get instance of the new model so we can resolve the element's positions
        final AtomicInteger ruleOffset = new AtomicInteger(-1);
        Source source = Source.create(file);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                resultIterator = WebUtils.getResultIterator(resultIterator, "text/css");
                if (resultIterator != null) {
                    CssParserResult result = (CssParserResult) resultIterator.getParserResult();
                    final Model model = Model.getModel(result);
                    model.runReadTask(new Model.ModelTask() {
                        @Override
                        public void run(StyleSheet styleSheet) {
                            ModelUtils utils = new ModelUtils(model);
                            Rule match = utils.findMatchingRule(omodel, orule);
                            if (match != null) {
                                ruleOffset.set(match.getStartOffset());
                            }
                        }
                    });
                }
            }
        });
        if (ruleOffset.get() == -1) {
            return;
        }
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                JEditorPane[] openedPanes = ec.getOpenedPanes();
                if (openedPanes != null && openedPanes.length > 0) {
                    JEditorPane pane = openedPanes[0];
                    pane.setCaretPosition(ruleOffset.get());
                    ec.open(); //give it a focus
                    LOGGER.log(Level.FINE, "Focused file {0} and set caret to offset {1}", new Object[]{file.getNameExt(), ruleOffset.get()});
                }
            }
        });

    }
    private int pos; //last change offset
    private int diff; //aggregated document modifications diff

    /**
     * Changes the class and id attributes of the active html source element.
     */
    private void modifySourceElement() {
        final BaseDocument doc = (BaseDocument) getDocument(activeElement.getFile());
        final AtomicBoolean success = new AtomicBoolean();

        pos = Integer.MAX_VALUE;
        diff = -1;
        doc.runAtomicAsUser(new Runnable() {
            @Override
            public void run() {
                try {
                    if (selectedClazz != null) {
                        updateAttribute(doc, getSelectedElementClass(), selectedClazz.getItemName(), "class");
                    }
                    if (selectedId != null) {
                        updateAttribute(doc, getSelectedElementId(), selectedId.getItemName(), "id");
                    }

                    success.set(true); //better not to do the save from within the atomic modification task
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        //possibly save the document if not opened in editor
        if (success.get()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        saveDocumentIfNotOpened(doc);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    }

    /**
     * Saves the given document to its underlying {@link FileObject} if the
     * document is not opened in the nb editor, more formally if
     * EditorCookie.getOpenedPanes() == null.
     *
     * @param document
     * @throws IOException
     */
    private static void saveDocumentIfNotOpened(Document document) throws IOException {

        Object o = document.getProperty(Document.StreamDescriptionProperty);
        if (o == null || !(o instanceof DataObject)) {
            return;
        }
        DataObject dobj = (DataObject) o;
        EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        if (ec != null && ec.getOpenedPanes() == null) {
            //file not open in any editor
            SaveCookie save = dobj.getLookup().lookup(SaveCookie.class);
            if (save != null) {
                save.save();
            }
        }
    }

    /**
     * Gets a {@link Document} instance for the given {@link FileObject}.
     *
     * Formally it does EditorCookie.openDocument().
     *
     * @param file
     * @return
     */
    private static Document getDocument(FileObject file) {
        try {
            DataObject d = DataObject.find(file);
            EditorCookie ec = (EditorCookie) d.getLookup().lookup(EditorCookie.class);

            if (ec == null) {
                return null;
            }
            return ec.openDocument();
        } catch (IOException e) {
            return null;
        }
    }

    private void updateAttribute(Document doc, Attribute a, String value, String name) throws BadLocationException {
        LOGGER.log(Level.FINE, "Source element {0}: updating {1} attribute from {2} to {3}.", 
                new Object[]{activeElement, name, (a != null ? a.unquotedValue() : null), value});
        
        OpenTag ot = activeElement.getOpenTag();
        Snapshot snap = activeElement.getSnapshot();
        if (a == null && value == null) {
            return; //no change
        }

        if (a == null && value != null) {
            //insert whole new attribute 
            int insertPos = snap.getOriginalOffset(ot.from() + 1 + ot.name().length());

            StringBuilder sb = new StringBuilder();
            sb.append(' ');
            sb.append(name);
            sb.append('=');
            sb.append('"');
            sb.append(value);
            sb.append('"');

            doc.insertString(insertPos, sb.toString(), null);

            pos = insertPos;
            diff = sb.length();
        } else if (a != null && value == null) {
            //remove
            int removeFrom = a.from() - 1; //include the WS before attribute name
            int removeTo = a.to();

            int rfdoc = snap.getOriginalOffset(removeFrom);
            int rtdoc = snap.getOriginalOffset(removeTo);

            if (rfdoc >= pos) {
                rfdoc += diff;
                rtdoc += diff;
            }

            doc.remove(rfdoc, rtdoc - rfdoc);

            pos = removeFrom;
            diff = rfdoc - rtdoc;

        } else {
            //change
            int removeFrom = a.from();
            int removeTo = a.to();

            int rfdoc = snap.getOriginalOffset(removeFrom);
            int rtdoc = snap.getOriginalOffset(removeTo);

            if (rfdoc >= pos) {
                rfdoc += diff;
                rtdoc += diff;
            }

            doc.remove(rfdoc, rtdoc - rfdoc);

            int insertPos = rfdoc;

            StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.append('=');
            sb.append('"');
            sb.append(value);
            sb.append('"');

            doc.insertString(insertPos, sb.toString(), null);

            pos = insertPos;
            diff = rfdoc - rtdoc + sb.length();
        }
    }

    /**
     * Refreshes the combobox model of the selectors combobox according to the choosen selector type.
     */
    private void updateSelectorsModel() {
        Collection<SelectorItem> items = new TreeSet<SelectorItem>();
        SelectorItem selection = null;

        //1.add classes && ids
        Project project = FileOwnerQuery.getOwner(context);
        if (project == null) {
            return;
        }

        try {
            CssIndex index = CssIndex.create(project);
            DependenciesGraph dependencies = index.getDependencies(context);
            Collection<FileObject> allReferedFiles = dependencies.getAllReferedFiles();

            switch (selectorTypeList.getSelectedIndex()) {
                case 0:
                    //class
                    items.add(NO_CLASS);
                    String selectedClassName = selectedClazz == null ? getSelectedElementClassName() : null;
                    Map<FileObject, Collection<String>> findAllClassDeclarations = index.findAllClassDeclarations();
                    for (FileObject file : findAllClassDeclarations.keySet()) {
                        if (allReferedFiles.contains(file)) { //only refered files
                            Collection<String> classes = findAllClassDeclarations.get(file);
                            for (String clz : classes) {
                                SelectorItem classSelectorItem = SelectorItem.createClass(clz, file);
                                items.add(classSelectorItem);
                                if (clz.equals(selectedClassName)) {
                                    //remember the element matching the selected html source element class
                                    //if not already modified (selectedClazz != null)
                                    selection = classSelectorItem;
                                }
                            }
                        }
                    }

                    if (selectedClassName != null && (selectedClazz == null || !selectedClazz.getItemName().equals(getSelectedElementClassName()))) {
                        //add special item for the class name preset in the html source element code but w/o
                        //a corresponding css rule
                        SelectorItem classSelectorItem = SelectorItem.createClass(selectedClassName, null);
                        classSelectorItem.setCreateInFile(getActiveStylesheet());
                        classSelectorItem.setCreateInAtRule(getActiveAtRule());

                        items.add(classSelectorItem);
                        selection = classSelectorItem;
                    }

                    if (selectedClazz != null) {
                        selection = selectedClazz;
                    }

                    break;
                case 1:
                    //id
                    items.add(NO_ID);
                    String selectedIdName = selectedId == null ? getSelectedElementIdName() : null;
                    Map<FileObject, Collection<String>> findAllIdDeclarations = index.findAllIdDeclarations();
                    for (FileObject file : findAllIdDeclarations.keySet()) {
                        if (allReferedFiles.contains(file)) { //only refered files
                            Collection<String> ids = findAllIdDeclarations.get(file);
                            for (String id : ids) {
                                SelectorItem idSelectorItem = SelectorItem.createId(id, file);
                                items.add(idSelectorItem);
                                if (id.equals(selectedIdName)) {
                                    selection = idSelectorItem;
                                }
                            }
                        }
                    }

                    if (selectedIdName != null && (selectedId == null || !selectedId.getItemName().equals(getSelectedElementIdName()))) {
                        //add special item for the class name preset in the html source element code but w/o
                        //a corresponding css rule
                        SelectorItem idSelectorItem = SelectorItem.createId(selectedIdName, null);
                        idSelectorItem.setCreateInFile(getActiveStylesheet());
                        idSelectorItem.setCreateInAtRule(getActiveAtRule());

                        items.add(idSelectorItem);
                        selection = idSelectorItem;
                    }

                    if (selectedId != null) {
                        selection = selectedId;
                    }
                    break;
                case 2:
                    //element
                    for (String elementName : getElementNames()) {
                        SelectorItem item = SelectorItem.createElement(elementName);
                        item.setCreateInFile(getActiveStylesheet());
                        item.setCreateInAtRule(getActiveAtRule());
                        items.add(item);
                    }
                    if (selectedElement == null) {
                        selection = items.iterator().next();
                    } else {
                        selection = selectedElement;
                    }
                    break;
                case 3:
                    //compound
                    if (compoundSelectorDefaultValue != null) {
                        items.add(SelectorItem.createCompound(compoundSelectorDefaultValue));
                    }
                    if (selectedCompound == null) {
                        SelectorItem compound = SelectorItem.createCompound(compoundSelectorDefaultValue);
                        compound.setCreateInFile(getActiveStylesheet());
                        compound.setCreateInAtRule(getActiveAtRule());
                        selection = compound;
                    } else {
                        selection = selectedCompound;
                    }

                    break;
            }

            SELECTORS_MODEL.setItems(items);
            if (selection != null) {
                selectorCB.setSelectedItem(selection);
            } else {
                if (SELECTORS_MODEL.getSize() > 0) {
                    selectorCB.setSelectedIndex(0); //select first
                } else {
                    //just clear out the combo area
                    selectorCB.setSelectedItem(null);
                }
            }

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    /**
     * Gets cached sorted list of all html element's names.
     */
    private Collection<String> getElementNames() {
        if (ELEMENT_SELECTOR_ITEMS == null) {
            ELEMENT_SELECTOR_ITEMS = new TreeSet<String>();
            HtmlModel model = HtmlModelFactory.getModel(HtmlVersion.HTML5);
            for (HtmlTag tag : model.getAllTags()) {
                ELEMENT_SELECTOR_ITEMS.add(tag.getName());
            }
        }
        return ELEMENT_SELECTOR_ITEMS;
    }
    
    /**
     * Gets a renderer for selector's combobox dropdown content.
     */    
    private SelectorItemRenderer getSelectorModelItemRenderer() {
        if (SELECTOR_MODEL_ITEM_RENDERER == null) {
            SELECTOR_MODEL_ITEM_RENDERER = new SelectorItemRenderer();
        }
        return SELECTOR_MODEL_ITEM_RENDERER;
    }

    /**
     * Creates a renderer for the selectors combobox dropdown.
     */
    private ListCellRenderer createSelectorsRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    SelectorItem selectorItem = (SelectorItem) value;
                    if (selectorItem == NO_CLASS || selectorItem == NO_ID) {
                        setText(Bundle.none_item());
                        return c;
                    } else {
                        SelectorItemRenderer selectorModelItemRenderer = getSelectorModelItemRenderer();
                        boolean originalElement = false;
                        switch (selectorItem.getType()) {
                            case SelectorItem.CLASS_TYPE:
                                originalElement = selectorItem.getItemName().equals(getSelectedElementClassName());
                                break;
                            case SelectorItem.ID_TYPE:
                                originalElement = selectorItem.getItemName().equals(getSelectedElementIdName());
                                break;
                        }
                        selectorModelItemRenderer.setItem(selectorItem, isSelected, originalElement);
                        return selectorModelItemRenderer;
                    }
                }
                return c;
            }
        };
    }

    /**
     * Creates a renderer for the at-rules combobox dropdown.
     */   
    private ListCellRenderer createAtRulesRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText(Bundle.none_item());
                } else {
                    setText(((AtRuleItem) value).getDisplayName());
                }
                return c;
            }
        };
    }

    /**
     * Creates a renderer for the stylesheets combobox dropdown.
     */    
    private ListCellRenderer createStylesheetsRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    //empty model
                    return c;
                }
                FileObject file = (FileObject) value;
                String fileNameExt = file.getNameExt();
                setText(fileNameExt);
                return c;
            }
        };
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        selectorTypeList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        descriptionPane = new javax.swing.JTextPane();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        styleSheetCB = new javax.swing.JComboBox();
        atRuleCB = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        selectorCB = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        selectorTypeLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        elementPathLabel = new javax.swing.JLabel();
        applyChangesCB = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        elementCodeLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel1.text")); // NOI18N

        jSplitPane1.setDividerLocation(140);
        jSplitPane1.setDividerSize(4);

        selectorTypeList.setModel(SELECTORS_LIST_MODEL);
        selectorTypeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(selectorTypeList);

        jSplitPane1.setLeftComponent(jScrollPane1);

        descriptionPane.setEditable(false);
        descriptionPane.setText(org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.descriptionPane.text")); // NOI18N
        descriptionPane.setEnabled(false);
        jScrollPane2.setViewportView(descriptionPane);

        jSplitPane1.setRightComponent(jScrollPane2);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel3.text")); // NOI18N

        styleSheetCB.setModel(STYLESHEETS_MODEL);
        styleSheetCB.setEnabled(false);
        styleSheetCB.setRenderer(createStylesheetsRenderer());
        styleSheetCB.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                styleSheetCBItemStateChanged(evt);
            }
        });

        atRuleCB.setModel(AT_RULES_MODEL);
        atRuleCB.setEnabled(false);
        atRuleCB.setRenderer(createAtRulesRenderer());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel4.text")); // NOI18N

        selectorCB.setEditable(true);
        selectorCB.setModel(SELECTORS_MODEL);
        selectorCB.setEnabled(false);
        selectorCB.setRenderer(createSelectorsRenderer());

        org.openide.awt.Mnemonics.setLocalizedText(selectorTypeLabel, null);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel6.text")); // NOI18N

        elementPathLabel.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(elementPathLabel, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.elementPathLabel.text")); // NOI18N

        applyChangesCB.setSelected(Settings.getCreateRule_ApplyChangesToSelectedSourceElement());
        org.openide.awt.Mnemonics.setLocalizedText(applyChangesCB, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.applyChangesCB.text")); // NOI18N
        applyChangesCB.setEnabled(false);
        applyChangesCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyChangesCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel8.text")); // NOI18N

        elementCodeLabel.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(elementCodeLabel, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.elementCodeLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addContainerGap(462, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(selectorTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator1)
                        .add(6, 6, 6))
                    .add(layout.createSequentialGroup()
                        .add(jLabel8)
                        .add(12, 12, 12)
                        .add(jSeparator2)
                        .add(6, 6, 6))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel4)
                            .add(jLabel2)
                            .add(jLabel3)
                            .add(jLabel5)
                            .add(jLabel6))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, elementPathLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, elementCodeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, atRuleCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, styleSheetCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, selectorCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(0, 0, Short.MAX_VALUE)
                                .add(applyChangesCB)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(selectorTypeLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jLabel4)
                    .add(selectorCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(styleSheetCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(atRuleCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(elementCodeLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(elementPathLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(applyChangesCB)
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void styleSheetCBItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_styleSheetCBItemStateChanged
        FileObject file = (FileObject) STYLESHEETS_MODEL.getSelectedItem();
        //update at rules model
        updateAtRulesModel(file);

        //disable the at-rules combobox if there isn't a single at-rule in the choosen stylesheet
        atRuleCB.setEnabled(AT_RULES_MODEL.getSize() > 1);
    }//GEN-LAST:event_styleSheetCBItemStateChanged

    private void applyChangesCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyChangesCBActionPerformed
        Settings.setCreateRule_ApplyChangesToSelectedSourceElement(applyChangesCB.isSelected());
    }//GEN-LAST:event_applyChangesCBActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox applyChangesCB;
    private javax.swing.JComboBox atRuleCB;
    private javax.swing.JTextPane descriptionPane;
    private javax.swing.JLabel elementCodeLabel;
    private javax.swing.JLabel elementPathLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JComboBox selectorCB;
    private javax.swing.JLabel selectorTypeLabel;
    private javax.swing.JList selectorTypeList;
    private javax.swing.JComboBox styleSheetCB;
    // End of variables declaration//GEN-END:variables

    /**
     * Member of the selectors combobox model.
     * 
     * One instance if kept for each selector type as user changes the default values.
     */
    private static class SelectorItem implements Comparable<SelectorItem> {

        public static final int CLASS_TYPE = 0;
        public static final int ID_TYPE = 1;
        public static final int ELEMENT_TYPE = 2;
        public static final int COMPOUND_TYPE = 3;
        private int type;
        private String clz, id, element;
        private FileObject existsIn;
        private FileObject createNewIn;
        private AtRuleItem createIn;

        private static SelectorItem createClass(String name, FileObject existsIn) {
            return new SelectorItem(CLASS_TYPE, name, null, null, existsIn);
        }

        private static SelectorItem createId(String name, FileObject existsIn) {
            return new SelectorItem(ID_TYPE, null, name, null, existsIn);
        }

        private static SelectorItem createElement(String name) {
            return new SelectorItem(ELEMENT_TYPE, null, null, name, null);
        }

        private static SelectorItem createCompound(String name) {
            return new SelectorItem(COMPOUND_TYPE, null, null, name, null);
        }

        public SelectorItem(int type, String clz, String id, String element, FileObject existsIn) {
            this.type = type;
            this.clz = clz;
            this.id = id;
            this.element = element;
            this.existsIn = existsIn;
        }

        /**
         * Gets the selectoritem type. 
         * Values:
         * {@link SelectorItem#CLASS_TYPE}
         * {@link SelectorItem#ID_TYPE}
         * {@link SelectorItem#ELEMENT_TYPE}
         * {@link SelectorItem#COMPOUND_TYPE}
         * 
         */
        public int getType() {
            return type;
        }

        /**
         * Sets the at-rule where the selected rule should be placed.
         */
        public void setCreateInAtRule(AtRuleItem mediaItem) {
            this.createIn = mediaItem;
        }

        /**
         * Gets the selected at-rule (location where the rule should be created).
         */
        public AtRuleItem getCreateInAtRule() {
            return createIn;
        }

        public String getCreateInAtRuleDisplayName() {
            return getCreateInAtRule() != null ? getCreateInAtRule().getDisplayName() : null;
        }

        /**
         * Sets target location for the element. 
         * The rule will be created there.
         */
        public void setCreateInFile(FileObject file) {
            this.createNewIn = file;
        }

        /**
         * Gets target location for the rule.
         */
        public FileObject getCreateInFile() {
            return createNewIn;
        }

        /**
         * Gets this element's file.
         * 
         * If the element already exist in a stylesheet,
         * its fileobject will be returned. If the element doesn't
         * exist yet, it will return the {@link #getCreateInFile() } return value.
         */
        public FileObject getFile() {
            return getCreateInFile() != null ? getCreateInFile() : getExistsInFile();
        }

        public String getCreateInFileDisplayName() {
            return getCreateInFile() != null ? getCreateInFile().getNameExt() : null;
        }

        /**
         * Gets the location where the element already exist or null if it doesn't exist.
         */
        public FileObject getExistsInFile() {
            return existsIn;
        }

        public String getFileDisplayName() {
            return existsIn != null ? existsIn.getNameExt() : null;
        }

        /**
         * Gets the element name. 
         * Class name without leading dot and id without the hash sign.
         */
        public String getItemName() {
            StringBuilder sb = new StringBuilder();
            if (clz != null) {
                sb.append(clz);
            } else if (id != null) {
                sb.append(id);
            } else if (element != null) {
                sb.append(element);
            }
            
            return sb.length() == 0 ? null : sb.toString();
        }

        /**
         * Gets the "fully qualified" element name. 
         * Class name WITH the leading dot and id WITH the hash sign.
         */
        public String getItemFQName() {
            StringBuilder sb = new StringBuilder();
            if (clz != null) {
                sb.append('.');
                sb.append(clz);
            } else if (id != null) {
                sb.append('#');
                sb.append(id);
            } else if (element != null) {
                sb.append(element);
            }
            return sb.length() == 0 ? null : sb.toString();
        }

        @Override
        public String toString() {
            String in = getItemName();
            return in != null ? in : "";
        }

        /**
         * Gets textual description of this element.
         */
        public String getInfo() {
            StringBuilder sb = new StringBuilder();
            sb.append("type=");
            sb.append(getType());
            sb.append(", name=");
            sb.append(getItemName());
            sb.append(", existsIn=");
            sb.append(getFileDisplayName());
            sb.append(", createInFile=");
            sb.append(getCreateInFileDisplayName());
            sb.append(", createInAt-Rule=");
            sb.append(getCreateInAtRuleDisplayName());
            return sb.toString();

        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 13 * hash + this.type;
            hash = 13 * hash + (this.clz != null ? this.clz.hashCode() : 0);
            hash = 13 * hash + (this.id != null ? this.id.hashCode() : 0);
            hash = 13 * hash + (this.element != null ? this.element.hashCode() : 0);
            hash = 13 * hash + (this.existsIn != null ? this.existsIn.hashCode() : 0);
            hash = 13 * hash + (this.createNewIn != null ? this.createNewIn.hashCode() : 0);
            hash = 13 * hash + (this.createIn != null ? this.createIn.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SelectorItem other = (SelectorItem) obj;
            if (this.type != other.type) {
                return false;
            }
            if ((this.clz == null) ? (other.clz != null) : !this.clz.equals(other.clz)) {
                return false;
            }
            if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
                return false;
            }
            if ((this.element == null) ? (other.element != null) : !this.element.equals(other.element)) {
                return false;
            }
            if (this.existsIn != other.existsIn && (this.existsIn == null || !this.existsIn.equals(other.existsIn))) {
                return false;
            }
            if (this.createNewIn != other.createNewIn && (this.createNewIn == null || !this.createNewIn.equals(other.createNewIn))) {
                return false;
            }
            if (this.createIn != other.createIn && (this.createIn == null || !this.createIn.equals(other.createIn))) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(SelectorItem o) {
            String myFQN = getItemFQName();
            String herFQN = o == null ? null : o.getItemFQName();
            
            if(herFQN == null && myFQN != null) {
                return +1;
            } else if(herFQN != null && myFQN == null) {
                return -1;
            } else if(herFQN == null && myFQN == null) {
                return 0;
            } else {
                return myFQN.compareTo(herFQN);
            }
        }
    }

    /**
     * Represents at-rule from a stylesheet.
     */
    private static class AtRuleItem {

        private Media media;
        private String displayName;

        public AtRuleItem(String displayName, Media media) {
            this.displayName = displayName;
            this.media = media;
        }

        public Media getMedia() {
            return media;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + (this.media != null ? this.media.hashCode() : 0);
            hash = 59 * hash + (this.displayName != null ? this.displayName.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AtRuleItem other = (AtRuleItem) obj;
            if (this.media != other.media && (this.media == null || !this.media.equals(other.media))) {
                return false;
            }
            if ((this.displayName == null) ? (other.displayName != null) : !this.displayName.equals(other.displayName)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
        
    }

    /**
     * JPanel used to render the content of the selector's combobox dropdown.
     */
    private static class SelectorItemRenderer extends JPanel {

        private JLabel west, east;
        private Color bg, bgSelected, fg, fgSelected, inFile;
        private Font plain, bold;
        private boolean original;

        public SelectorItemRenderer() {
            west = new JLabel();
            east = new JLabel();
            setLayout(new BorderLayout());
            add(west, BorderLayout.WEST);
            add(east, BorderLayout.EAST);

            fg = javax.swing.UIManager.getDefaults().getColor("ComboBox.foreground");
            bg = javax.swing.UIManager.getDefaults().getColor("ComboBox.background");
            fgSelected = javax.swing.UIManager.getDefaults().getColor("ComboBox.selectionForeground");
            bgSelected = javax.swing.UIManager.getDefaults().getColor("ComboBox.selectionBackground");
            inFile = Color.gray;

            plain = west.getFont();
            bold = plain.deriveFont(Font.BOLD);
        }

        public void setItem(SelectorItem item, boolean isSelected, boolean originalElement) {
            if (original != originalElement) {
                //font change
                west.setFont(originalElement ? bold : plain);
                original = originalElement;
            }

            west.setText(item.getItemName());
            east.setText(item.getFileDisplayName());

            if (isSelected) {
                west.setForeground(fgSelected);
                east.setForeground(fgSelected);
                setBackground(bgSelected);
            } else {
                west.setForeground(fg);
                east.setForeground(inFile);
                setBackground(bg);

            }
        }
    }

    /**
     * Copied and modified platform's DefaultComboBoxModel.
     */
    public class ExtDefaultComboBoxModel extends AbstractListModel implements MutableComboBoxModel, Serializable {

        private List objects;
        private Object selectedObject;

        /**
         * Constructs an empty DefaultComboBoxModel object.
         */
        public ExtDefaultComboBoxModel() {
            objects = new ArrayList();
        }

        public void setItems(Collection items) {
            int oldLen = objects.size();
            objects.clear();
            objects.addAll(items);
            fireIntervalRemoved(this, 0, oldLen);
            fireIntervalAdded(this, 0, objects.size());
        }

        public void setSelectedIndex(int index) {
            if(index >= objects.size()) {
                return ;
            }
            if(index < 0) {
                return ;
            }
            setSelectedItem(objects.get(index));
        }
        
        // implements javax.swing.ComboBoxModel
        /**
         * Set the value of the selected item. The selected item may be null.
         * <p>
         *
         * @param anObject The combo box value or null for no selection.
         */
        @Override
        public void setSelectedItem(Object anObject) {
            if ((selectedObject != null && !selectedObject.equals(anObject))
                    || selectedObject == null && anObject != null) {
                selectedObject = anObject;
                fireContentsChanged(this, -1, -1);
            }
        }

        // implements javax.swing.ComboBoxModel
        @Override
        public Object getSelectedItem() {
            return selectedObject;
        }

        // implements javax.swing.ListModel
        @Override
        public int getSize() {
            return objects.size();
        }

        // implements javax.swing.ListModel
        @Override
        public Object getElementAt(int index) {
            if (index >= 0 && index < objects.size()) {
                return objects.get(index);
            } else {
                return null;
            }
        }

        /**
         * Returns the index-position of the specified object in the list.
         *
         * @param anObject
         * @return an int representing the index position, where 0 is the first
         * position
         */
        public int getIndexOf(Object anObject) {
            return objects.indexOf(anObject);
        }

        // implements javax.swing.MutableComboBoxModel
        @Override
        public void addElement(Object anObject) {
            objects.add(anObject);
            fireIntervalAdded(this, objects.size() - 1, objects.size() - 1);
            if (objects.size() == 1 && selectedObject == null && anObject != null) {
                setSelectedItem(anObject);
            }
        }

        // implements javax.swing.MutableComboBoxModel
        @Override
        public void insertElementAt(Object anObject, int index) {
            objects.add(index, anObject);
            fireIntervalAdded(this, index, index);
        }

        // implements javax.swing.MutableComboBoxModel
        @Override
        public void removeElementAt(int index) {
            if (getElementAt(index) == selectedObject) {
                if (index == 0) {
                    setSelectedItem(getSize() == 1 ? null : getElementAt(index + 1));
                } else {
                    setSelectedItem(getElementAt(index - 1));
                }
            }

            objects.remove(index);

            fireIntervalRemoved(this, index, index);
        }

        // implements javax.swing.MutableComboBoxModel
        @Override
        public void removeElement(Object anObject) {
            int index = objects.indexOf(anObject);
            if (index != -1) {
                removeElementAt(index);
            }
        }
    }
}
