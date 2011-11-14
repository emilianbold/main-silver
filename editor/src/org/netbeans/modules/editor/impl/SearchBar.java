/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.editor.impl;

import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.logging.Level;
import javax.swing.event.PopupMenuEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.*;
import javax.swing.Action;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.lib2.search.EditorFindSupport;
import org.openide.awt.CloseButtonFactory;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * This is an implementation of a Firefox(TM) style Incremental Search Side Bar.
 * 
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public final class SearchBar extends JPanel {

    private static SearchBar searchbarInstance = null;
    private static final Logger LOG = Logger.getLogger(SearchBar.class.getName());
    private static final boolean CLOSE_ON_ENTER = Boolean.getBoolean("org.netbeans.modules.editor.search.closeOnEnter"); // NOI18N
    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);
    private static final Color NOT_FOUND = Color.RED.darker();
    private static final Color INVALID_REGEXP = Color.red;
    // Delay times for incremental search [ms]
    private static final int SEARCH_DELAY_TIME_LONG = 300; // < 3 chars
    private static final int SEARCH_DELAY_TIME_SHORT = 20; // >= 3 chars
    private static final int defaultIncremantalSearchComboWidth = 200;
    private static final int maxIncremantalSearchComboWidth = 350;
    private WeakReference<JTextComponent> actualTextComponent;
    private List<PropertyChangeListener> actualComponentListeners = new LinkedList<PropertyChangeListener>();
    private FocusAdapter focusAdapterForComponent;
    private PropertyChangeListener propertyChangeListenerForComponent;
    private final JButton expandButton;
    private final JPopupMenu expandPopup;
    private final JLabel findLabel;
    private final JComboBox incSearchComboBox;
    private final JTextField incSearchTextField;
    private final DocumentListener incSearchTextFieldListener;
    private boolean hadFocusOnIncSearchTextField = false;
    private final JButton findNextButton;
    private final JButton findPreviousButton;
    private final JCheckBox matchCaseCheckBox;
    private final JCheckBox wholeWordsCheckBox;
    private final JCheckBox regexpCheckBox;
    private final JCheckBox highlightCheckBox;
    private final JCheckBox wrapAroundCheckBox;
    private final JPanel padding;
    private final JButton closeButton;
    private Map<String, Object> findProps = EditorFindSupport.getInstance().getFindProperties();
    private boolean searched = false;
    /**
     * contains everything that is in Search bar and is possible to move to
     * expand popup
     */
    private final List<Component> inBar = new ArrayList<Component>();
    /**
     * components moved to popup
     */
    private final LinkedList<Component> inPopup = new LinkedList<Component>();
    /**
     * defines index of all components in Search bar
     */
    private final List<Component> barOrder = new ArrayList<Component>();
    private boolean isPopupGoingToShow = false;
    private boolean isPopupShown = false;

    public static SearchBar getInstance() {
        if (searchbarInstance == null) {
            searchbarInstance = new SearchBar();
        }
        return searchbarInstance;
    }

    /* 
     * default getInstance 
     */
    public static SearchBar getInstance(JTextComponent component) {
        SearchBar searchbarIns = getInstance();
        if (searchbarIns.getActualTextComponent() != component) {
            searchbarIns.setActualTextComponent(component);
        }
        return searchbarIns;
    }

    @SuppressWarnings("unchecked")
    private SearchBar() {
        addEscapeKeystrokeFocusBackTo(this);
        addKeystrokeFindActionTo(this);
        addKeystrokeReplaceActionTo(this);

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setFocusCycleRoot(true);
        Color bgColor = getBackground();
        setBackground(new Color(Math.max(0, bgColor.getRed() - 20),
                Math.max(0, bgColor.getGreen() - 20),
                Math.max(0, bgColor.getBlue() - 20)));
        setForeground(UIManager.getColor("textText")); //NOI18N

        add(Box.createHorizontalStrut(8)); //spacer in the beginnning of the toolbar

        incSearchComboBox = createIncSearchComboBox();
        incSearchTextField = createIncSearchTextField(incSearchComboBox);
        incSearchTextFieldListener = createIncSearchTextFieldListener(incSearchTextField);
        incSearchTextField.getDocument().addDocumentListener(incSearchTextFieldListener);
        addEnterKeystrokeFindNextTo(incSearchTextField);
        addShiftEnterKeystrokeFindPreviousTo(incSearchTextField);
        incSearchTextField.getActionMap().remove("toggle-componentOrientation"); // NOI18N
        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK), "none"); // NOI18N

        if (getCurrentKeyMapProfile().startsWith("Emacs")) { // NOI18N
            emacsProfileFix(incSearchTextField);
        }

        findLabel = new JLabel();
        Mnemonics.setLocalizedText(findLabel, NbBundle.getMessage(SearchBar.class, "CTL_Find")); // NOI18N
        findLabel.setLabelFor(incSearchComboBox);
        add(findLabel);
        add(incSearchComboBox);

        final JToolBar.Separator leftSeparator = new JToolBar.Separator();
        leftSeparator.setOrientation(SwingConstants.VERTICAL);
        add(leftSeparator);

        findPreviousButton = createFindButton("org/netbeans/modules/editor/resources/find_previous.png", "CTL_FindPrevious"); // NOI18N
        findPreviousButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                findPrevious();
            }
        });
        add(findPreviousButton);
        findNextButton = createFindButton("org/netbeans/modules/editor/resources/find_next.png", "CTL_FindNext"); // NOI18N
        findNextButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                findNext();
            }
        });
        add(findNextButton);

        final JToolBar.Separator rightSeparator = new JToolBar.Separator();
        rightSeparator.setOrientation(SwingConstants.VERTICAL);
        add(rightSeparator);

        matchCaseCheckBox = createCheckBox("CTL_MatchCase", EditorFindSupport.FIND_MATCH_CASE); // NOI18N
        add(matchCaseCheckBox);
        wholeWordsCheckBox = createCheckBox("CTL_WholeWords", EditorFindSupport.FIND_WHOLE_WORDS); // NOI18N
        add(wholeWordsCheckBox);
        regexpCheckBox = createRegExpCheckBox("CTL_Regexp", EditorFindSupport.FIND_REG_EXP); // NOI18N
        add(regexpCheckBox);
        highlightCheckBox = createCheckBox("CTL_Highlight", EditorFindSupport.FIND_HIGHLIGHT_SEARCH); // NOI18N
        add(highlightCheckBox);
        wrapAroundCheckBox = createCheckBox("CTL_WrapAround", EditorFindSupport.FIND_WRAP_SEARCH); // NOI18N
        add(wrapAroundCheckBox);
        selectCheckBoxes();

        expandButton = createExpandButton();
        expandPopup = createExpandPopup(expandButton);
        add(expandButton);
        expandButton.setVisible(true);

        // padding at the end of the toolbar
        padding = new JPanel();
        padding.setOpaque(false);
        add(padding);

        closeButton = createCloseButton();
        add(closeButton);

        makeBarExpandable();

        setVisible(false);
    }

    void updateIncSearchComboBoxHistory(String incrementalSearchText) {
        incSearchTextField.getDocument().removeDocumentListener(incSearchTextFieldListener);
        // Add the text to the top of the list
        for (int i = incSearchComboBox.getItemCount() - 1; i >= 0; i--) {
            String item = (String) incSearchComboBox.getItemAt(i);
            if (item.equals(incrementalSearchText)) {
                incSearchComboBox.removeItemAt(i);
            }
        }
        ((MutableComboBoxModel) incSearchComboBox.getModel()).insertElementAt(incrementalSearchText, 0);
        incSearchComboBox.setSelectedIndex(0);
        incSearchTextField.getDocument().addDocumentListener(incSearchTextFieldListener);
    }

    private FocusAdapter createFocusAdapterForComponent() {
        return new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if (e.getOppositeComponent() instanceof JRootPane) {
                    // Hack for linux where invoking Find from main menu caused focus gained on editor
                    // even when openning quick search
                    return;
                }
                hadFocusOnIncSearchTextField = false;
            }
        };
    }

    private PropertyChangeListener createPropertyChangeListenerForComponent() {
        PropertyChangeListener pcl = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt == null || !"keymap".equals(evt.getPropertyName())) { // NOI18N
                    return;
                }
                JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
                if (lastFocusedComponent == null) {
                    return;
                }
                Keymap keymap = lastFocusedComponent.getKeymap();

                if (keymap instanceof MultiKeymap) {
                    MultiKeymap multiKeymap = (MultiKeymap) keymap;

                    Action[] actions = lastFocusedComponent.getActions();
                    for (Action action : actions) { // Discover the keyStrokes for incremental-search-forward
                        String actionName = (String) action.getValue(Action.NAME);
                        if (actionName == null) {
                            LOG.log(Level.WARNING, "SearchBar: Null Action.NAME property of action: {0}\n", action);
                        } else if (actionName.equals(SearchAndReplaceBarHandler.INCREMENTAL_SEARCH_FORWARD) || actionName.equals(BaseKit.findNextAction)) {
                            keystrokeForSearchAction(multiKeymap, action,
                                    new AbstractAction() {

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            findNext();
                                        }
                                    });
                        } else if (actionName.equals(SearchAndReplaceBarHandler.INCREMENTAL_SEARCH_BACKWARD) || actionName.equals(BaseKit.findPreviousAction)) {
                            keystrokeForSearchAction(multiKeymap, action,
                                    new AbstractAction() {

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            findPrevious();
                                        }
                                    });
                        }
                    }
                }
            }

            private void keystrokeForSearchAction(MultiKeymap multiKeymap, Action searchAction, AbstractAction newSearchAction) {
                KeyStroke[] keyStrokes = multiKeymap.getKeyStrokesForAction(searchAction);
                if (keyStrokes != null) {
                    InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                    for (KeyStroke ks : keyStrokes) {
                        LOG.log(Level.FINE, "found {1} search action, {0}", new Object[]{ks, searchAction.getValue(Action.NAME)}); //NOI18N
                        inputMap.put(ks, (String) searchAction.getValue(Action.NAME));
                    }
                    getActionMap().put((String) searchAction.getValue(Action.NAME), newSearchAction);
                }
            }
        };
        pcl.propertyChange(new PropertyChangeEvent(this, "keymap", null, null));
        return pcl;
    }

    private void addShiftEnterKeystrokeFindPreviousTo(JTextField incSearchTextField) {
        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK, true),
                "incremental-find-previous"); // NOI18N
        incSearchTextField.getActionMap().put("incremental-find-previous", // NOI18N
                new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                findPrevious();
                if (CLOSE_ON_ENTER) {
                    looseFocus();
                }
            }
        });
    }

    private void addEnterKeystrokeFindNextTo(JTextField incSearchTextField) {
        incSearchTextField.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                "incremental-find-next"); // NOI18N
        incSearchTextField.getActionMap().put("incremental-find-next", // NOI18N
                new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                findNext();
                if (CLOSE_ON_ENTER) {
                    looseFocus();
                }
            }
        });
    }

    private JTextField createIncSearchTextField(final JComboBox incSearchComboBox) throws MissingResourceException {
        JTextField incrementalSearchTextField = (JTextField) incSearchComboBox.getEditor().getEditorComponent();
        incrementalSearchTextField.setToolTipText(NbBundle.getMessage(SearchBar.class, "TOOLTIP_IncrementalSearchText")); // NOI18N
        // flatten the action map for the text field to allow removal
        ActionMap origActionMap = incrementalSearchTextField.getActionMap();
        ActionMap newActionMap = new ActionMap();
        for (Object key : origActionMap.allKeys()) {
            newActionMap.put(key, origActionMap.get(key));
        }
        incrementalSearchTextField.setActionMap(newActionMap);
        incrementalSearchTextField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                hadFocusOnIncSearchTextField = true;
            }
        });
        return incrementalSearchTextField;
    }

    private DocumentListener createIncSearchTextFieldListener(final JTextField incSearchTextField) {
        final Timer searchDelayTimer = new Timer(SEARCH_DELAY_TIME_LONG, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                incrementalSearch();
            }
        });
        searchDelayTimer.setRepeats(false);

        // listen on text change
        return new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                searched = false;
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                searched = false;
                // text changed - attempt incremental search
                if (incSearchTextField.getText().length() > 3) {
                    searchDelayTimer.setInitialDelay(SEARCH_DELAY_TIME_SHORT);
                }
                searchDelayTimer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searched = false;
                // text changed - attempt incremental search
                if (incSearchTextField.getText().length() <= 3) {
                    searchDelayTimer.setInitialDelay(SEARCH_DELAY_TIME_LONG);
                }
                searchDelayTimer.restart();
            }
        };
    }

    private JButton createCloseButton() throws MissingResourceException {
        JButton button = CloseButtonFactory.createBigCloseButton();
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                looseFocus();
            }
        });
        button.setToolTipText(NbBundle.getMessage(SearchBar.class, "TOOLTIP_CloseIncrementalSearchSidebar")); // NOI18N
        return button;
    }

    private JCheckBox createRegExpCheckBox(String resName, final String findConstant) throws MissingResourceException {
        final JCheckBox regExpCheckBox = new JCheckBox();
        regExpCheckBox.setOpaque(false);
        Mnemonics.setLocalizedText(regExpCheckBox, NbBundle.getMessage(SearchBar.class, resName));
        regExpCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                switchFindSupportValue(findConstant);
                wholeWordsCheckBox.setEnabled(!regexpCheckBox.isSelected());
                incrementalSearch();
            }
        });
        regExpCheckBox.setMargin(BUTTON_INSETS);
        regExpCheckBox.setFocusable(false);
        return regExpCheckBox;
    }

    JCheckBox createCheckBox(String resName, final String findConstant) throws MissingResourceException {
        final JCheckBox checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        Mnemonics.setLocalizedText(checkBox, NbBundle.getMessage(SearchBar.class, resName));
        checkBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                switchFindSupportValue(findConstant);
                incrementalSearch();
            }
        });
        checkBox.setMargin(BUTTON_INSETS);
        checkBox.setFocusable(false);
        return checkBox;
    }

    private void selectCheckBoxes() {
        wholeWordsCheckBox.setSelected(getFindSupportValue(EditorFindSupport.FIND_WHOLE_WORDS));
        wholeWordsCheckBox.setEnabled(!getRegExp());
        matchCaseCheckBox.setSelected(getFindSupportValue(EditorFindSupport.FIND_MATCH_CASE));
        regexpCheckBox.setSelected(getRegExp());
        highlightCheckBox.setSelected(getFindSupportValue(EditorFindSupport.FIND_HIGHLIGHT_SEARCH));
        wrapAroundCheckBox.setSelected(getFindSupportValue(EditorFindSupport.FIND_WRAP_SEARCH));
    }

    private JPopupMenu createExpandPopup(final JButton expButton) {
        JPopupMenu expPopup = new JPopupMenu();
        expPopup.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // check if it was canceled by click on expand button
                if (expButton.getMousePosition() == null) {
                    expButton.setContentAreaFilled(false);
                    expButton.setBorderPainted(false);
                    isPopupShown = false;
                }
            }
        });
        return expPopup;
    }

    private JButton createFindButton(String imageIcon, String resName) {
        JButton button = new JButton(
                ImageUtilities.loadImageIcon(imageIcon, false));
        Mnemonics.setLocalizedText(button, NbBundle.getMessage(SearchBar.class, resName));
        button.setMargin(BUTTON_INSETS);
        return button;
    }

    private JButton createExpandButton() throws MissingResourceException {
        JButton expButton = new JButton(ImageUtilities.loadImageIcon("org/netbeans/modules/editor/resources/find_expand.png", false)); // NOI18N
        expButton.setMnemonic(NbBundle.getMessage(SearchBar.class, "CTL_ExpandButton_Mnemonic").charAt(0)); // NOI18N
        expButton.setToolTipText(NbBundle.getMessage(ReplaceBar.class, "TOOLTIP_ExpandButton")); // NOI18N
        expButton.setMargin(BUTTON_INSETS);
        expButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean state = !isPopupShown;
                isPopupShown = state;
                if (state) {
                    showExpandedMenu();
                } else {
                    hideExpandedMenu();
                }
            }
        });
        return expButton;
    }

    private JComboBox createIncSearchComboBox() {
        JComboBox incrementalSearchComboBox = new JComboBox() {

            public @Override
            Dimension getMinimumSize() {
                return getPreferredSize();
            }

            public @Override
            Dimension getMaximumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getPreferredSize() {
                int width;
                int editsize = this.getEditor().getEditorComponent().getPreferredSize().width + 10;
                if (editsize > defaultIncremantalSearchComboWidth && editsize < maxIncremantalSearchComboWidth) {
                    width = editsize;
                } else if (editsize >= maxIncremantalSearchComboWidth) {
                    width = maxIncremantalSearchComboWidth;
                } else {
                    width = defaultIncremantalSearchComboWidth;
                }
                return new Dimension(width,
                        super.getPreferredSize().height);
            }
        };

        incrementalSearchComboBox.setEditable(true);
        return incrementalSearchComboBox;
    }

    // Treat Emacs profile specially in order to fix #191895
    private void emacsProfileFix(final JTextField incSearchTextField) {
        class JumpOutOfSearchAction extends AbstractAction {

            private String actionName;

            public JumpOutOfSearchAction(String n) {
                actionName = n;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                looseFocus();
                if (getActualTextComponent() != null) {
                    ActionEvent ev = new ActionEvent(getActualTextComponent(), e.getID(), e.getActionCommand(), e.getModifiers());
                    Action action = getActualTextComponent().getActionMap().get(actionName);
                    action.actionPerformed(ev);
                }
            }
        }
        String actionName = "caret-begin-line"; // NOI18N
        Action a1 = new JumpOutOfSearchAction(actionName);
        incSearchTextField.getActionMap().put(actionName, a1);
        actionName = "caret-end-line"; // NOI18N
        Action a2 = new JumpOutOfSearchAction(actionName);
        incSearchTextField.getActionMap().put(actionName, a2);

        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_P, InputEvent.CTRL_MASK, false), "caret-up-alt"); // NOI18N
        actionName = "caret-up"; // NOI18N
        Action a3 = new JumpOutOfSearchAction(actionName);
        incSearchTextField.getActionMap().put("caret-up-alt", a3); // NOI18N

        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, InputEvent.CTRL_MASK, false), "caret-down-alt"); // NOI18N
        actionName = "caret-down"; // NOI18N
        Action a4 = new JumpOutOfSearchAction(actionName);
        incSearchTextField.getActionMap().put("caret-down-alt", a4); // NOI18N
    }
    // From org.netbeans.modules.editor.settings.storage.EditorSettingsImpl
    private static final String DEFAULT_PROFILE = "NetBeans"; //NOI18N
    private static final String FATTR_CURRENT_KEYMAP_PROFILE = "currentKeymap";      // NOI18N
    private static final String KEYMAPS_FOLDER = "Keymaps"; // NOI18N

    /* This method is verbatim copy from class
     * org.netbeans.modules.editor.settings.storage.EditorSettingsImpl
     * bacause we don't want to introduce the dependency between this module
     * and Editor Setting Storage module.
     */
    private String getCurrentKeyMapProfile() {
        String currentKeyMapProfile = null;
        FileObject fo = FileUtil.getConfigFile(KEYMAPS_FOLDER);
        if (fo != null) {
            Object o = fo.getAttribute(FATTR_CURRENT_KEYMAP_PROFILE);
            if (o instanceof String) {
                currentKeyMapProfile = (String) o;
            }
        }
        if (currentKeyMapProfile == null) {
            currentKeyMapProfile = DEFAULT_PROFILE;
        }
        return currentKeyMapProfile;
    }

    private void makeBarExpandable() {
        inBar.add(matchCaseCheckBox);
        inBar.add(wholeWordsCheckBox);
        inBar.add(regexpCheckBox);
        inBar.add(highlightCheckBox);
        inBar.add(wrapAroundCheckBox);
        barOrder.addAll(Arrays.asList(this.getComponents()));
        remove(expandButton);
        expandButton.setVisible(false);
    }

    @Override
    public Dimension getPreferredSize() {
        computeLayout();
        return super.getPreferredSize();
    }
    
    void computeLayout() {
        int parentWidth = this.getParent().getWidth();
        int totalWidth = 0;
        for (Component c : this.getComponents()) {
            if (c != padding) {
                totalWidth += c.getPreferredSize().width;
            }
        }

        boolean change = false;
        if (totalWidth <= parentWidth) { // enough space try to clear expand popup
            while (!inPopup.isEmpty()) {
                Component c = inPopup.getFirst();
                totalWidth += c.getPreferredSize().width;

                if (totalWidth > parentWidth) {
                    
                    break;
                }
                inPopup.removeFirst();
                inBar.add(c);
                expandPopup.remove(c);
                add(c, barOrder.indexOf(c));
                change = true;
            }
        } else { // lack of space
            while (totalWidth > parentWidth && !inBar.isEmpty()) {
                Component c = inBar.remove(inBar.size() - 1);
                inPopup.addFirst(c);
                remove(c);
                expandPopup.add(c, 0);
                totalWidth -= c.getPreferredSize().width;
                change = true;
            }
        }

        if (change) {
            if (inPopup.isEmpty()) {
                remove(expandButton);
                expandButton.setVisible(false);
            } else if (getComponentIndex(expandButton) < 0) {
                add(expandButton, getComponentIndex(padding));
                expandButton.setVisible(true);
            }
            this.revalidate();
            expandPopup.invalidate();
            expandPopup.validate();
        }
    }

    private int getComponentIndex(Component c) {
        Component[] comps = getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (c == comps[i]) {
                return i;
            }
        }
        return -1;
    }

    private void showExpandedMenu() {
        if (!inPopup.isEmpty() && !expandPopup.isVisible()) {
            isPopupGoingToShow = true;
            Insets ins = expandPopup.getInsets();
            // compute popup height since JPopupMenu.getHeight does not work
            expandPopup.show(expandButton, 0, -(matchCaseCheckBox.getHeight() * inPopup.size() + ins.top + ins.bottom));
        }
    }

    private void hideExpandedMenu() {
        if (expandPopup.isVisible()) {
            expandPopup.setVisible(false);
            incrementalSearch();
            incSearchTextField.requestFocusInWindow();
        }
    }

    @Override
    public String getName() {
        //Required for Aqua L&F toolbar UI
        return "editorSearchBar"; // NOI18N
    }

    void addEscapeKeystrokeFocusBackTo(JComponent component) {
        component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                "loose-focus"); // NOI18N
        component.getActionMap().put("loose-focus", // NOI18N
                new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!CLOSE_ON_ENTER && !searched) {
                        findNext();
                }
                looseFocus();
            }
        });
    }

    void addKeystrokeReplaceActionTo(JComponent component) {
        JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
        if (lastFocusedComponent == null) {
            return;
        }
        Keymap keymap = lastFocusedComponent.getKeymap();

        if (keymap instanceof MultiKeymap) {
            MultiKeymap multiKeymap = (MultiKeymap) keymap;

            KeyStroke[] keyStrokesForReplaceAction = multiKeymap.getKeyStrokesForAction(lastFocusedComponent.getActionMap().get(SearchAndReplaceBarHandler.REPLACE_ACTION));
            if (keyStrokesForReplaceAction == null) {
                return;
            }
            for (KeyStroke ks : keyStrokesForReplaceAction) {
                component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(ks.getKeyCode(), ks.getModifiers(), true), "replace-from-component"); // NOI18N
            }
            component.getActionMap().put("replace-from-component", new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
                    if (lastFocusedComponent == null) {
                        return;
                    }
                    BaseAction replaceAction = (BaseAction) lastFocusedComponent.getActionMap().get(SearchAndReplaceBarHandler.REPLACE_ACTION);
                    replaceAction.actionPerformed(e, lastFocusedComponent);
                }
            });
        }
    }

    void addKeystrokeFindActionTo(JComponent component) {
        JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
        if (lastFocusedComponent == null) {
            return;
        }
        Keymap keymap = lastFocusedComponent.getKeymap();

        if (keymap instanceof MultiKeymap) {
            MultiKeymap multiKeymap = (MultiKeymap) keymap;

            KeyStroke[] keyStrokesForReplaceAction = multiKeymap.getKeyStrokesForAction(lastFocusedComponent.getActionMap().get(SearchAndReplaceBarHandler.INCREMENTAL_SEARCH_FORWARD));
            if (keyStrokesForReplaceAction == null) {
                return;
            }
            for (KeyStroke ks : keyStrokesForReplaceAction) {
                component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(ks.getKeyCode(), ks.getModifiers(), true), "find-from-component"); // NOI18N
            }
            component.getActionMap().put("find-from-component", new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
                    if (lastFocusedComponent == null) {
                        return;
                    }
                    BaseAction findAction = (BaseAction) lastFocusedComponent.getActionMap().get(SearchAndReplaceBarHandler.INCREMENTAL_SEARCH_FORWARD);
                    findAction.actionPerformed(e, lastFocusedComponent);
                }
            });
        }
    }

    void gainFocus() {
        hadFocusOnIncSearchTextField = true;
        isPopupShown = false;
        setVisible(true);
        initBlockSearch();

        incSearchTextField.requestFocusInWindow();

        if (incSearchTextField.getText().length() > 0) {
            // preselect the text in incremental search text field
            incSearchTextField.selectAll();
            findPreviousButton.setEnabled(true);
            findNextButton.setEnabled(true);
        } else {
            findPreviousButton.setEnabled(false);
            findNextButton.setEnabled(false);
        }
        searched = false;
    }

    void looseFocus() {
        hadFocusOnIncSearchTextField = false;
        if (!isVisible()) {
            return;
        }
        if (isPopupGoingToShow) {
            isPopupGoingToShow = false;
            return;
        }

        EditorFindSupport.getInstance().setBlockSearchHighlight(0, 0);
        EditorFindSupport.getInstance().incSearchReset();
        if (getActualTextComponent() != null) {
            org.netbeans.editor.Utilities.setStatusText(getActualTextComponent(), "");
            getActualTextComponent().requestFocusInWindow();
        }
        setVisible(false);
    }

    private void incrementalSearch() {
        if (getActualTextComponent() == null) {
            return;
        }
        String incrementalSearchText = incSearchTextField.getText();
        boolean empty = incrementalSearchText.length() <= 0;

        // Enable/disable the pre/next buttons
        findPreviousButton.setEnabled(!empty);
        findNextButton.setEnabled(!empty);

        // configure find properties
        EditorFindSupport findSupport = EditorFindSupport.getInstance();
        findSupport.putFindProperties(getFindProps());

        // search starting at current caret position
        int caretPosition = getActualTextComponent().getCaretPosition();

        if (regexpCheckBox.isSelected()) {
            Pattern pattern;
            String patternErrorMsg = null;
            try {
                pattern = Pattern.compile(incrementalSearchText);
            } catch (PatternSyntaxException e) {
                pattern = null;
                patternErrorMsg = e.getDescription();
            }
            if (pattern != null) {
                // valid regexp
                incSearchTextField.setForeground(UIManager.getColor("textText")); //NOI18N
                org.netbeans.editor.Utilities.setStatusText(getActualTextComponent(), "", StatusDisplayer.IMPORTANCE_INCREMENTAL_FIND);
            } else {
                // invalid regexp
                incSearchTextField.setForeground(INVALID_REGEXP);
                org.netbeans.editor.Utilities.setStatusBoldText(getActualTextComponent(), NbBundle.getMessage(
                        SearchBar.class, "incremental-search-invalid-regexp", patternErrorMsg)); //NOI18N
            }
        } else {
            if (findSupport.incSearch(findProps, caretPosition) || empty) {
                // text found - reset incremental search text field's foreground
                incSearchTextField.setForeground(UIManager.getColor("textText")); //NOI18N
                org.netbeans.editor.Utilities.setStatusText(getActualTextComponent(), "", StatusDisplayer.IMPORTANCE_INCREMENTAL_FIND);
                searched = true;
            } else {
                // text not found - indicate error in incremental search
                // text field with red foreground
                incSearchTextField.setForeground(NOT_FOUND);
                org.netbeans.editor.Utilities.setStatusText(getActualTextComponent(), NbBundle.getMessage(
                        SearchBar.class, "incremental-search-not-found", incrementalSearchText),
                        StatusDisplayer.IMPORTANCE_INCREMENTAL_FIND); //NOI18N
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    void findNext() {
        find(true);
    }

    void findPrevious() {
        find(false);
    }

    private void find(boolean next) {
        String incrementalSearchText = incSearchTextField.getText();
        boolean empty = incrementalSearchText.length() <= 0;
        updateIncSearchComboBoxHistory(incrementalSearchText);

        // configure find properties
        EditorFindSupport findSupport = EditorFindSupport.getInstance();
        Map<String, Object> actualfindProps = getFindProps();
        findSupport.putFindProperties(actualfindProps);

        if (findSupport.find(actualfindProps, !next) || empty) {
            // text found - reset incremental search text field's foreground
            incSearchTextField.setForeground(UIManager.getColor("textText")); //NOI18N
            searched = true;
        } else {
            // text not found - indicate error in incremental search text field with red foreground
            incSearchTextField.setForeground(NOT_FOUND);
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @SuppressWarnings("unchecked")
    void initBlockSearch() {
        JTextComponent c = EditorRegistry.lastFocusedComponent();
        String selText;
        int startSelection;
        int endSelection;
        boolean blockSearchVisible = false;

        if (c != null) {
            startSelection = c.getSelectionStart();
            endSelection = c.getSelectionEnd();

            Document doc = c.getDocument();
            if (doc instanceof BaseDocument) {
                BaseDocument bdoc = (BaseDocument) doc;
                try {
                    int startLine = org.netbeans.editor.Utilities.getLineOffset(bdoc, startSelection);
                    int endLine = org.netbeans.editor.Utilities.getLineOffset(bdoc, endSelection);
                    if (endLine > startLine) {
                        blockSearchVisible = true;
                    }
                } catch (BadLocationException ble) {
                }
            }

            // caretPosition = bwdSearch.isSelected() ? c.getSelectionEnd() : c.getSelectionStart();

            if (!blockSearchVisible) {
                selText = c.getSelectedText();
                if (selText != null && selText.length() > 0) {
                    int n = selText.indexOf('\n');
                    if (n >= 0) {
                        selText = selText.substring(0, n);
                    }
                    incSearchTextField.setText(selText);
                    // findWhat.getEditor().setItem(selText);
                    // changeFindWhat(true);
                } else {
                    String findWhat = (String) EditorFindSupport.getInstance().getFindProperty(EditorFindSupport.FIND_WHAT);
                    if (findWhat != null && findWhat.length() > 0) {
                        incSearchTextField.getDocument().removeDocumentListener(incSearchTextFieldListener);
                        incSearchTextField.setText(findWhat);
                        incSearchTextField.getDocument().addDocumentListener(incSearchTextFieldListener);
                    }
                }
            }

            int blockSearchStartOffset = blockSearchVisible ? startSelection : 0;
            int blockSearchEndOffset = blockSearchVisible ? endSelection : 0;

            try {
                findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH, blockSearchVisible);
                findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH_START, doc.createPosition(blockSearchStartOffset));
                findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH_END, doc.createPosition(blockSearchEndOffset));
                EditorFindSupport.getInstance().setBlockSearchHighlight(blockSearchStartOffset, blockSearchEndOffset);
            } catch (BadLocationException ble) {
                findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH, Boolean.FALSE);
                findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH_START, null);
                findProps.put(EditorFindSupport.FIND_BLOCK_SEARCH_END, null);
            }

            EditorFindSupport.getInstance().putFindProperties(findProps);
        }
    }

    boolean getFindSupportValue(String findConstant) {
        Boolean b = (Boolean) findProps.get(findConstant);
        return b != null ? b.booleanValue() : false;
    }

    private void switchFindSupportValue(String findConstant) {
        findProps.put(findConstant, !getFindSupportValue(findConstant));
    }

    boolean getRegExp() {
        return getFindSupportValue(EditorFindSupport.FIND_REG_EXP);
    }

    boolean hadFocusOnTextField() {
        return hadFocusOnIncSearchTextField;
    }

    void lostFocusOnTextField() {
        hadFocusOnIncSearchTextField = false;
    }

    void setActualTextComponent(JTextComponent component) {
        if (getActualTextComponent() != null) {
            getActualTextComponent().removeFocusListener(focusAdapterForComponent);
            getActualTextComponent().removePropertyChangeListener(propertyChangeListenerForComponent);
        }
        if (focusAdapterForComponent == null) {
            focusAdapterForComponent = createFocusAdapterForComponent();
        }
        if (propertyChangeListenerForComponent == null) {
            propertyChangeListenerForComponent = createPropertyChangeListenerForComponent();
        }
        component.addFocusListener(focusAdapterForComponent);
        component.addPropertyChangeListener(propertyChangeListenerForComponent);
        for (PropertyChangeListener pcl : actualComponentListeners) {
            pcl.propertyChange(new PropertyChangeEvent(this, "actualTextComponent", getActualTextComponent(), component));
        }
        actualTextComponent = new WeakReference<JTextComponent>(component);
    }

    void addActualComponentListener(PropertyChangeListener propertyChangeListener) {
        actualComponentListeners.add(propertyChangeListener);
    }

    JTextComponent getActualTextComponent() {
        return actualTextComponent != null ? actualTextComponent.get() : null;
    }

    JTextField getIncSearchTextField() {
        return incSearchTextField;
    }

    JButton getCloseButton() {
        return closeButton;
    }

    JLabel getFindLabel() {
        return findLabel;
    }

    JButton getFindNextButton() {
        return findNextButton;
    }

    JButton getFindPreviousButton() {
        return findPreviousButton;
    }

    Map<String, Object> getFindProps() {
        findProps.put(EditorFindSupport.FIND_WHAT, incSearchTextField.getText());
        findProps.put(EditorFindSupport.FIND_MATCH_CASE, matchCaseCheckBox.isSelected());
        findProps.put(EditorFindSupport.FIND_WHOLE_WORDS, wholeWordsCheckBox.isSelected());
        findProps.put(EditorFindSupport.FIND_REG_EXP, regexpCheckBox.isSelected());
        findProps.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        findProps.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        findProps.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, !incSearchTextField.getText().isEmpty() && highlightCheckBox.isSelected());
        findProps.put(EditorFindSupport.FIND_WRAP_SEARCH, wrapAroundCheckBox.isSelected());
        return findProps;
    }

    JCheckBox getMatchCaseCheckBox() {
        return matchCaseCheckBox;
    }

    JCheckBox getRegexpCheckBox() {
        return regexpCheckBox;
    }

    boolean isSearched() {
        return searched;
    }

    void setSearched(boolean searched) {
        this.searched = searched;
    }

    JComponent getExpandButton() {
        return expandButton;
    }

    
}
