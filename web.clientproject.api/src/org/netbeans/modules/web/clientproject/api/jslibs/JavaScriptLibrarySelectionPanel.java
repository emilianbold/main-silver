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
package org.netbeans.modules.web.clientproject.api.jslibs;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.AbstractListModel;
import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.web.clientproject.api.WebClientLibraryManager;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.common.api.Version;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * UI for selecting JS libraries.
 * @since 1.20
 */
public final class JavaScriptLibrarySelectionPanel extends JPanel {

    private static final long serialVersionUID = -468734354571212312L;

    static final Logger LOGGER = Logger.getLogger(JavaScriptLibrarySelectionPanel.class.getName());

    private static final Pattern LIBRARIES_FOLDER_PATTERN = Pattern.compile("^[\\w-.]+$", Pattern.CASE_INSENSITIVE); // NOI18N

    static final RequestProcessor RP = new RequestProcessor(JavaScriptLibrarySelectionPanel.class);

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final JavaScriptLibrariesValidator librariesValidator;

    final List<SelectedLibrary> selectedLibraries = Collections.synchronizedList(new ArrayList<SelectedLibrary>());
    final Set<SelectedLibrary> failedLibraries = Collections.synchronizedSet(new HashSet<SelectedLibrary>());
    // @GuardedBy("EDT")
    final Set<SelectedLibrary> invalidLibraries = new HashSet<SelectedLibrary>();
    // @GuardedBy("EDT")
    final LibrariesTableModel librariesTableModel = LibrariesTableModel.create();
    // @GuardedBy("EDT")
    final TableRowSorter<LibrariesTableModel> librariesTableSorter = new TableRowSorter<LibrariesTableModel>(librariesTableModel);
    // @GuardedBy("EDT")
    final LibrariesListModel selectedLibrariesListModel = new LibrariesListModel(selectedLibraries);

    private volatile File defaultWorkDir = null;
    // folder path is accessed outside of EDT thread
    private volatile String librariesFolder = null;
    private volatile boolean librariesFolderSet = false;
    private volatile boolean panelEnabled = true;


    /**
     * Create new instance of the panel.
     * <p>
     * This method must be run in the UI thread.
     * @param librariesValidator validator for selected JS libraries
     */
    public JavaScriptLibrarySelectionPanel(@NonNull JavaScriptLibrariesValidator librariesValidator) {
        Parameters.notNull("librariesValidator", librariesValidator); // NOI18N
        checkUiThread();

        this.librariesValidator = librariesValidator;

        initComponents();

        initInfos();
        initLibraries();
        initLibrariesFolder();
    }

    /**
     * Set additional information message that is displayed in the top of this panel.
     * <p>
     * This method must be run in the UI thread.
     * @param additionalInfo additional information message to be shown, can be {@code null} for no message
     */
    public void setAdditionalInfo(@NullAllowed String additionalInfo) {
        checkUiThread();
        additionalInfoLabel.setText(additionalInfo);
        additionalInfoLabel.setVisible(additionalInfo != null);
        ((GroupLayout) getLayout()).setHonorsVisibility(additionalInfoLabel, additionalInfo != null);
    }

    void setBrowseButtonVisible(@NullAllowed File defaultWorkDir) {
        checkUiThread();
        this.defaultWorkDir = defaultWorkDir;
        librariesFolderBrowseButton.setVisible(defaultWorkDir != null);
    }

    /**
     * Get the list of selected JS libraries.
     * @return list of selected JS libraries
     */
    public List<SelectedLibrary> getSelectedLibraries() {
        return new ArrayList<SelectedLibrary>(selectedLibraries);
    }

    /**
     * Get path of the libraries folder. The folder does not need to exist.
     * @return path of the libraries folder
     */
    public String getLibrariesFolder() {
        return librariesFolder;
    }

    void setLibrariesFolder(String folder) {
        checkUiThread();
        librariesFolderSet = true;
        librariesFolderTextField.setText(folder);
    }

    /**
     * Add listener to changes in the selected libraries or libraries folder.
     * @param listener listener to be added, can be {@code null}
     */
    public void addChangeListener(@NullAllowed ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    /**
     * Remove listener to changes in the selected libraries or libraries folder.
     * @param listener listener to be removed, can be {@code null}
     */
    public void removeChangeListener(@NullAllowed ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    /**
     * Validate and get an error message or {@code null} if there are no errors.
     * @return error message or {@code null} if there are no errors
     * @see #getWarningMessage()
     */
    public String getErrorMessage() {
        String error;
        error = validateLibrariesFolder();
        if (error != null) {
            return error;
        }
        error = validateLibraries();
        if (error != null) {
            return error;
        }
        return null;
    }

    /**
     * Validate and get a warning message or {@code null} if there are no warnings.
     * @return error message or {@code null} if there are no errors
     * @see #getErrorMessage()
     */
    public String getWarningMessage() {
        return null;
    }

    /**
     * Lock this panel, it means no user changes can be done.
     * <p>
     * This method must be run in the UI thread.
     * @see #unlockPanel()
     */
    public void lockPanel() {
        checkUiThread();
        enablePanel(false);
    }

    /**
     * Unlock this panel, it means no user changes can be done.
     * <p>
     * This method must be run in the UI thread.
     * @see #lockPanel()
     */
    public void unlockPanel() {
        checkUiThread();
        enablePanel(true);
    }

    /**
     * Set default JS libraries. These libraries are automatically selected
     * and cannot be removed (unselected) by user.
     * @param defaultLibs default JS libraries, typically relative file paths
     */
    public void updateDefaultLibraries(@NonNull Collection<String> defaultLibs) {
        Parameters.notNull("defaultLibs", defaultLibs); // NOI18N
        // remove default libraries
        Iterator<SelectedLibrary> iterator = selectedLibraries.iterator();
        while (iterator.hasNext()) {
            SelectedLibrary library = iterator.next();
            if (library.isDefault()) {
                iterator.remove();
            }
        }
        for (String lib : defaultLibs) {
            selectedLibraries.add(new SelectedLibrary(lib));
        }
        fireSelectedLibrariesChangeInEDT();
        adjustLibrariesFolder();
    }

    /**
     * Set failed JS libraries. Typically if the {@link #getSelectedLibraries() selected JS libraries}
     * cannot be downloaded.
     * @param failedLibs failed JS libraries
     */
    public void updateFailedLibraries(@NonNull List<SelectedLibrary> failedLibs) {
        Parameters.notNull("failedLibs", failedLibs); // NOI18N
        failedLibraries.clear();
        failedLibraries.addAll(failedLibs);
        fireSelectedLibrariesChangeInEDT();
    }

    private void checkUiThread() {
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Must be run in UI thread");
        }
    }

    private void adjustLibrariesFolder() {
        if (librariesFolderSet) {
            return;
        }
        Map<String, Integer> paths = new HashMap<>();
        for (SelectedLibrary selectedLibrary : selectedLibraries) {
            for (String filePath : selectedLibrary.getFilePaths()) {
                List<String> parts = new ArrayList<>(StringUtilities.explode(filePath, "/")); // NOI18N
                if (parts.size() < 2) {
                    continue;
                }
                // remove file name
                parts.remove(parts.size() - 1);
                String folderPath = StringUtilities.implode(parts, "/"); // NOI18N
                Integer count = paths.get(folderPath);
                if (count == null) {
                    count = 0;
                }
                paths.put(folderPath, ++count);
            }
        }
        Map.Entry<String, Integer> bestPath = null;
        for (Map.Entry<String, Integer> entry : paths.entrySet()) {
            if (bestPath == null) {
                bestPath = entry;
            } else if (bestPath.getValue() < entry.getValue()) {
                bestPath = entry;
            }
        }
        if (bestPath != null) {
            final String path = bestPath.getKey();
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    librariesFolderTextField.setText(path);
                }
            });
        }
    }

    private void initInfos() {
        setAdditionalInfo(null);
    }

    private void initLibraries() {
        setLibrariesUpdateLabel();
        initLibrariesTable();
        initLibrariesList();
        initLibrariesButtons();
    }

    @NbBundle.Messages({
        "JavaScriptLibrarySelectionPanel.update.tooltip=Click to update",
        "# {0} - date or n/a",
        "JavaScriptLibrarySelectionPanel.update.default=<html><a href=\"#\">Updated: {0}</a>",
        "# {0} - date with time or 'never' (see next message)",
        "JavaScriptLibrarySelectionPanel.update.default.tooltip=Updated: {0}, click to update",
        "# T13Y: If the lib has not been updated yet, this label is used instead of a date (see previous message)",
        "JavaScriptLibrarySelectionPanel.update.never=never",
        "JavaScriptLibrarySelectionPanel.update.running=Updating...",
    })
    private void setLibrariesUpdateLabel() {
        String text;
        String tooltip = null;
        if (isUpdateRunning()) {
            text = Bundle.JavaScriptLibrarySelectionPanel_update_running();
        } else {
            FileTime lastUpdateTime = WebClientLibraryManager.getDefault().getLibrariesLastUpdatedTime();
            String when;
            if (lastUpdateTime == null) {
                when = Bundle.JavaScriptLibrarySelectionPanel_update_never();
                tooltip = Bundle.JavaScriptLibrarySelectionPanel_update_tooltip();
            } else {
                Date updateDate = new Date(lastUpdateTime.toMillis());
                when = DateFormat.getDateInstance()
                        .format(updateDate);
                tooltip = Bundle.JavaScriptLibrarySelectionPanel_update_default_tooltip(DateFormat.getDateTimeInstance()
                        .format(updateDate));
            }
            text = Bundle.JavaScriptLibrarySelectionPanel_update_default(when);
        }
        updateLibrariesLabel.setText(text);
        updateLibrariesLabel.setToolTipText(tooltip);
        // fix ui
        updateLibrariesLabel.setMaximumSize(updateLibrariesLabel.getPreferredSize());
    }

    private void initLibrariesTable() {
        assert EventQueue.isDispatchThread();
        librariesTable.setModel(librariesTableModel);
        librariesTable.setRowSorter(librariesTableSorter);
        // tooltip
        librariesTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                assert EventQueue.isDispatchThread();
                if (librariesTableModel.isPopulating()) {
                    return;
                }
                Point point = e.getPoint();
                int row = librariesTable.convertRowIndexToModel(librariesTable.rowAtPoint(point));
                librariesTable.setToolTipText(getWrappedText(librariesTableModel.getItems().get(row).getDescription()));
            }

            /**
             * Wrap the given text after each 100 characters or so.
             */
            private String getWrappedText(String text) {
                if (text == null || text.isEmpty()) {
                    return null;
                }
                final int lineLength = 100;
                if (text.length() <= lineLength) {
                    return text;
                }
                StringBuilder sb = new StringBuilder(text.length() + 40);
                int currentLineLength = lineLength;
                for (String word : text.split(" ")) { // NOI18N
                    sb.append(word);
                    if (sb.length() > currentLineLength) {
                        sb.append("<br>"); // NOI18N
                        currentLineLength += lineLength + 4; // count <br> as well
                    } else {
                        sb.append(" "); // NOI18N
                    }
                }
                return "<html>" + sb.toString(); // NOI18N
            }
        });
        librariesFilterTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processChange();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                processChange();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                processChange();
            }
            private void processChange() {
                filterLibrariesTable();
            }
        });
    }

    private void initLibrariesList() {
        assert EventQueue.isDispatchThread();
        selectedLibrariesList.setModel(selectedLibrariesListModel);
        selectedLibrariesList.setCellRenderer(new SelectedLibraryRenderer(selectedLibrariesList.getCellRenderer(), failedLibraries, invalidLibraries));
    }

    private void initLibrariesButtons() {
        assert EventQueue.isDispatchThread();
        // listen on table and list
        ListSelectionListener selectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                enableLibraryButtons();
            }
        };
        librariesTable.getSelectionModel().addListSelectionListener(selectionListener);
        selectedLibrariesList.getSelectionModel().addListSelectionListener(selectionListener);
        librariesTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                enableLibraryButtons();
            }
        });
        selectedLibrariesListModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                dataChanged();
            }
            @Override
            public void intervalRemoved(ListDataEvent e) {
                dataChanged();
            }
            @Override
            public void contentsChanged(ListDataEvent e) {
                dataChanged();
            }
            private void dataChanged() {
                enableLibraryButtons();
                fireChangeEvent();
            }
        });
        // action listeners
        selectSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectSelectedLibraries();
            }
        });
        deselectSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deselectSelectedLibraries();
            }
        });
        // set correct state
        enableLibraryButtons();
    }

    private void initLibrariesFolder() {
        librariesFolderBrowseButton.setVisible(false);
        librariesFolderTextField.setText("js/libs"); // NOI18N
        librariesFolder = librariesFolderTextField.getText();
        librariesFolderTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processChange();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                processChange();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                processChange();
            }
            private void processChange() {
                librariesFolder = librariesFolderTextField.getText();
                // remove ending slashes
                while (librariesFolder.endsWith("/")) { // NOI18N
                    librariesFolder = librariesFolder.substring(0, librariesFolder.length() - 1);
                }
                fireChangeEvent();
            }
        });
    }

    void fireChangeEvent() {
        changeSupport.fireChange();
    }

    private boolean isUpdateRunning() {
        return !panelEnabled;
    }

    private void startUpdate() {
        lockPanel();
        setLibrariesUpdateLabel();
    }

    void finishUpdate() {
        unlockPanel();
        setLibrariesUpdateLabel();
    }

    private void enablePanel(boolean enabled) {
        assert EventQueue.isDispatchThread();
        panelEnabled = enabled;
        librariesFilterTextField.setEnabled(enabled);
        librariesTable.setEnabled(enabled);
        librariesTable.setRowSelectionAllowed(enabled);
        librariesTable.setColumnSelectionAllowed(enabled);
        selectSelectedButton.setEnabled(enabled);
        deselectSelectedButton.setEnabled(enabled);
        selectedLibrariesList.setEnabled(enabled);
        librariesFolderTextField.setEnabled(enabled);
    }

    void enableLibraryButtons() {
        assert EventQueue.isDispatchThread();
        // select
        selectSelectedButton.setEnabled(canSelectSelected());
        // deselect
        deselectSelectedButton.setEnabled(canDeselectSelected());
    }

    void filterLibrariesTable() {
        assert EventQueue.isDispatchThread();
        final String filter = librariesFilterTextField.getText().toLowerCase();
        if (filter.isEmpty()) {
            // no filter
            librariesTableSorter.setRowFilter(null);
            return;
        }
        // we have some filter
        librariesTableSorter.setRowFilter(new RowFilter<LibrariesTableModel, Integer>() {
            @Override
            public boolean include(RowFilter.Entry<? extends LibrariesTableModel, ? extends Integer> entry) {
                return entry.getStringValue(0).toLowerCase().contains(filter);
            }
        });
    }

    private boolean canSelectSelected() {
        if (librariesTableModel.isPopulating()) {
            return false;
        }
        return librariesTable.getSelectedRows().length > 0;
    }

    private boolean canDeselectSelected() {
        if (librariesTableModel.isPopulating()) {
            return false;
        }
        if (selectedLibraries.isEmpty()) {
            return false;
        }
        for (int index : selectedLibrariesList.getSelectedIndices()) {
            if (index >= selectedLibraries.size()) {
                // apparently happens when deselecting more libraries
                continue;
            }
            if (!selectedLibraries.get(index).isDefault()) {
                return true;
            }
        }
        return false;
    }

    void selectSelectedLibraries() {
        assert EventQueue.isDispatchThread();
        for (int i : librariesTable.getSelectedRows()) {
            selectLibrary(librariesTable.convertRowIndexToModel(i));
        }
        selectedLibrariesListModel.fireContentsChanged();
    }

    private void selectLibrary(int libraryIndex) {
        assert EventQueue.isDispatchThread();
        ModelItem modelItem = librariesTableModel.getItems().get(libraryIndex);
        // #232113 - do not allow more version of the same library
        String realName = getRealName(modelItem.getLibrary());
        Iterator<SelectedLibrary> iterator = selectedLibraries.iterator();
        while (iterator.hasNext()) {
            SelectedLibrary selectedLibrary = iterator.next();
            if (selectedLibrary.isDefault()) {
                // ignore it
                continue;
            }
            LibraryVersion libraryVersion = selectedLibrary.getLibraryVersion();
            assert libraryVersion != null : selectedLibrary;
            if (realName.equals(getRealName(libraryVersion.getLibrary()))) {
                // library already selected => remove it
                iterator.remove();
                break;
            }
        }
        // add new library
        LibraryVersion libraryVersion = modelItem.getSelectedVersion();
        selectedLibraries.add(new SelectedLibrary(libraryVersion));
    }

    void deselectSelectedLibraries() {
        assert EventQueue.isDispatchThread();
        // get selected items
        int[] selectedIndices = selectedLibrariesList.getSelectedIndices();
        List<SelectedLibrary> selected = new ArrayList<SelectedLibrary>(selectedIndices.length);
        for (int index : selectedIndices) {
            SelectedLibrary library = selectedLibraries.get(index);
            if (!library.isDefault()) {
                selected.add(library);
            }
        }
        // create set and remove selected items
        Set<SelectedLibrary> set = new HashSet<SelectedLibrary>(selectedLibraries);
        set.removeAll(selected);
        // copy them back to set
        selectedLibraries.clear();
        selectedLibraries.addAll(set);
        selectedLibrariesListModel.fireContentsChanged();
    }

    @NbBundle.Messages({
        "JavaScriptLibrarySelectionPanel.error.librariesFolder.invalid=Libraries folder can contain only alphanumeric characters, \"_\", \"-\", \".\" and \"/\"."
    })
    private String validateLibrariesFolder() {
        if (librariesFolder.isEmpty()) {
            return null;
        }
        for (String segment : librariesFolder.split("/")) { // NOI18N
            if (!LIBRARIES_FOLDER_PATTERN.matcher(segment).matches()) {
                return Bundle.JavaScriptLibrarySelectionPanel_error_librariesFolder_invalid();
            }
        }
        return null;
    }

    private String validateLibraries() {
        assert EventQueue.isDispatchThread();
        HashSet<SelectedLibrary> newLibraries = new HashSet<SelectedLibrary>();
        Iterator<SelectedLibrary> iterator = selectedLibraries.iterator();
        while (iterator.hasNext()) {
            SelectedLibrary library = iterator.next();
            if (library.isDefault()) {
                continue;
            }
            newLibraries.add(library);
        }
        Pair<Set<SelectedLibrary>, String> result = librariesValidator.validate(librariesFolder, newLibraries);
        // libraries
        invalidLibraries.clear();
        invalidLibraries.addAll(result.first());
        return result.second();
    }

    private void fireSelectedLibrariesChangeInEDT() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                selectedLibrariesListModel.fireContentsChanged();
            }
        });
    }

    static void errorOccured(String message) {
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }

    static String getRealDisplayName(SelectedLibrary selectedLibrary) {
        LibraryVersion libraryVersion = selectedLibrary.getLibraryVersion();
        assert libraryVersion != null : selectedLibrary;
        Library library = libraryVersion.getLibrary();
        assert library != null : libraryVersion;
        return getRealDisplayName(library);
    }

    static String getRealDisplayName(Library library) {
        String realName = library.getProperties().get(WebClientLibraryManager.PROPERTY_REAL_DISPLAY_NAME);
        assert realName != null : library;
        return realName;
    }

    static String getRealName(Library library) {
        String realName = library.getProperties().get(WebClientLibraryManager.PROPERTY_REAL_NAME);
        assert realName != null : library;
        return realName;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        generalInfoLabel = new javax.swing.JLabel();
        additionalInfoLabel = new javax.swing.JLabel();
        librariesLabel = new javax.swing.JLabel();
        librariesFilterTextField = new javax.swing.JTextField();
        librariesScrollPane = new javax.swing.JScrollPane();
        librariesTable = new LibrariesTable();
        updateLibrariesLabel = new javax.swing.JLabel();
        selectSelectedButton = new javax.swing.JButton();
        deselectSelectedButton = new javax.swing.JButton();
        selectedLabel = new javax.swing.JLabel();
        selectedLibrariesScrollPane = new javax.swing.JScrollPane();
        selectedLibrariesList = new javax.swing.JList();
        librariesFolderLabel = new javax.swing.JLabel();
        librariesFolderTextField = new javax.swing.JTextField();
        librariesFolderBrowseButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(generalInfoLabel, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelectionPanel.class, "JavaScriptLibrarySelectionPanel.generalInfoLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(additionalInfoLabel, "ADDITIONAL_INFO"); // NOI18N

        librariesLabel.setLabelFor(librariesTable);
        org.openide.awt.Mnemonics.setLocalizedText(librariesLabel, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelectionPanel.class, "JavaScriptLibrarySelectionPanel.librariesLabel.text")); // NOI18N

        librariesScrollPane.setViewportView(librariesTable);

        org.openide.awt.Mnemonics.setLocalizedText(updateLibrariesLabel, "UPDATE"); // NOI18N
        updateLibrariesLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                updateLibrariesLabelMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                updateLibrariesLabelMousePressed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(selectSelectedButton, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelectionPanel.class, "JavaScriptLibrarySelectionPanel.selectSelectedButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deselectSelectedButton, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelectionPanel.class, "JavaScriptLibrarySelectionPanel.deselectSelectedButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectedLabel, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelectionPanel.class, "JavaScriptLibrarySelectionPanel.selectedLabel.text")); // NOI18N

        selectedLibrariesScrollPane.setViewportView(selectedLibrariesList);

        librariesFolderLabel.setLabelFor(librariesFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(librariesFolderLabel, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelectionPanel.class, "JavaScriptLibrarySelectionPanel.librariesFolderLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(librariesFolderBrowseButton, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelectionPanel.class, "JavaScriptLibrarySelectionPanel.librariesFolderBrowseButton.text")); // NOI18N
        librariesFolderBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                librariesFolderBrowseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(librariesFolderLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(librariesFolderTextField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(librariesFolderBrowseButton))
            .addGroup(layout.createSequentialGroup()
                .addComponent(generalInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(additionalInfoLabel)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(updateLibrariesLabel))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(librariesLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(librariesFilterTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))
                    .addComponent(librariesScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectSelectedButton)
                    .addComponent(deselectSelectedButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectedLabel)
                    .addComponent(selectedLibrariesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {deselectSelectedButton, selectSelectedButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(generalInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(additionalInfoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(librariesLabel)
                    .addComponent(selectedLabel)
                    .addComponent(librariesFilterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectedLibrariesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(librariesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(selectSelectedButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deselectSelectedButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(updateLibrariesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(librariesFolderLabel)
                    .addComponent(librariesFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(librariesFolderBrowseButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void updateLibrariesLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_updateLibrariesLabelMouseEntered
        int cursor = isUpdateRunning() ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR;
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(cursor));
    }//GEN-LAST:event_updateLibrariesLabelMouseEntered

    @NbBundle.Messages("JavaScriptLibrarySelectionPanel.error.jsLibs.update=Available libraries not updated, see IDE log for more details.")
    private void updateLibrariesLabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_updateLibrariesLabelMousePressed
        if (isUpdateRunning()) {
            return;
        }
        startUpdate();
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    WebClientLibraryManager.getDefault().updateLibraries(true);
                } catch (InterruptedException ex) {
                    // cancelled
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    errorOccured(Bundle.JavaScriptLibrarySelectionPanel_error_jsLibs_update());
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            finishUpdate();
                        }
                    });
                }
            }
        });
    }//GEN-LAST:event_updateLibrariesLabelMousePressed

    @NbBundle.Messages("JavaScriptLibrarySelectionPanel.jsLibsFolder.browse=Select directory for JS libraries")
    private void librariesFolderBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_librariesFolderBrowseButtonActionPerformed
        assert defaultWorkDir != null;
        File dir = new FileChooserBuilder(JavaScriptLibrarySelectionPanel.class)
                .setDirectoriesOnly(true)
                .setTitle(Bundle.JavaScriptLibrarySelectionPanel_jsLibsFolder_browse())
                .setDefaultWorkingDirectory(defaultWorkDir)
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (dir != null) {
            String relativePath = PropertyUtils.relativizeFile(defaultWorkDir, dir);
            String path;
            if (relativePath == null) {
                path = dir.getAbsolutePath();
            } else if (".".equals(relativePath)) { // NOI18N
                path = "";
            } else {
                path = relativePath;
            }
            librariesFolderTextField.setText(path);
        }
    }//GEN-LAST:event_librariesFolderBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel additionalInfoLabel;
    private javax.swing.JButton deselectSelectedButton;
    private javax.swing.JLabel generalInfoLabel;
    private javax.swing.JTextField librariesFilterTextField;
    private javax.swing.JButton librariesFolderBrowseButton;
    private javax.swing.JLabel librariesFolderLabel;
    private javax.swing.JTextField librariesFolderTextField;
    private javax.swing.JLabel librariesLabel;
    private javax.swing.JScrollPane librariesScrollPane;
    private javax.swing.JTable librariesTable;
    private javax.swing.JButton selectSelectedButton;
    private javax.swing.JLabel selectedLabel;
    private javax.swing.JList selectedLibrariesList;
    private javax.swing.JScrollPane selectedLibrariesScrollPane;
    private javax.swing.JLabel updateLibrariesLabel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    /**
     * Class representing a specific JS library version, e.g. "jQuery 1.8 minified".
     */
    public static final class LibraryVersion {

        private final Library library;
        private final String type;


        /**
         * Create new JS library version.
         * @param library JS library, never {@code null}
         * @param type library type (e.g. "minified", "regular" etc.), never {@code null} or an empty string
         */
        public LibraryVersion(@NonNull Library library, @NonNull String type) {
            Parameters.notNull("library", library); // NOI18N
            Parameters.notNull("type", type); // NOI18N
            if (type.isEmpty()) {
                throw new IllegalArgumentException("Type cannot be empty string");
            }
            this.library = library;
            this.type = type;
        }

        /**
         * Get library.
         * @return library
         */
        public Library getLibrary() {
            return library;
        }

        /**
         * Get library type (e.g. "minified", "regular" etc.).
         * @return library type (e.g. "minified", "regular" etc.)
         */
        public String getType() {
            return type;
        }

        /**
         * Get library version, e.g. "1.8.2".
         * @return library version, e.g. "1.8.2"
         */
        public String getLibraryVersion() {
            return library.getProperties().get(WebClientLibraryManager.PROPERTY_VERSION);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + library.getName().hashCode();
            hash = 41 * hash + getLibraryVersion().hashCode();
            hash = 41 * hash + (this.type != null ? this.type.hashCode() : 0);
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
            final LibraryVersion other = (LibraryVersion) obj;
            if (!this.library.getName().equals(other.library.getName())) {
                return false;
            }
            if (this.getLibraryVersion() != other.getLibraryVersion() && (this.getLibraryVersion() == null || !this.getLibraryVersion().equals(other.getLibraryVersion()))) {
                return false;
            }
            if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "LibraryVersion{" + "library=" + library.getName() + ", type=" + type + '}'; //NOI18N
        }

    }

    /**
     * Class representing a selected JS library.
     */
    public static final class SelectedLibrary {

        private final String filename;
        private final LibraryVersion libraryVersion;


        /**
         * Create selected JS library for a local file. Such library is then {@link #isDefault() default}.
         * @param filename file name, typically with a relative file path
         */
        SelectedLibrary(@NonNull String filename) {
            this(filename, null);
            Parameters.notNull("filename", filename); // NOI18N
        }

        /**
         * Create selected JS library for a library version. Such library is not {@link #isDefault() default}.
         * @param libraryVersion library version
         */
        SelectedLibrary(@NonNull LibraryVersion libraryVersion) {
            this(null, libraryVersion);
            Parameters.notNull("libraryVersion", libraryVersion); // NOI18N
        }

        private SelectedLibrary(String filename, LibraryVersion libraryVersion) {
            this.filename = filename;
            this.libraryVersion = libraryVersion;
        }

        /**
         * Get paths of files in this JS library.
         * @return paths of files in this JS library
         */
        public List<String> getFilePaths() {
            if (filename != null) {
                return Collections.singletonList(filename);
            }
            return getLibraryFilePaths();
        }

        /**
         * Get library version of this JS library or {@code null} for {@link #isDefault() default} library.
         * @return library version of this JS library or {@code null} for {@link #isDefault() default} library
         * @see #isDefault()
         */
        @CheckForNull
        public LibraryVersion getLibraryVersion() {
            return libraryVersion;
        }

        /**
         * Return {@code true} if this JS library is {@link JavaScriptLibrarySelectionPanel#updateDefaultLibraries(Collection) default} JS library.
         * @return {@code true} if this JS library is {@link JavaScriptLibrarySelectionPanel#updateDefaultLibraries(Collection) default} JS library
         */
        public boolean isDefault() {
            return libraryVersion == null;
        }

        private List<String> getLibraryFilePaths() {
            return WebClientLibraryManager.getDefault().getLibraryFilePaths(libraryVersion.getLibrary(), libraryVersion.getType());
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 31 * hash + (this.filename != null ? this.filename.hashCode() : 0);
            hash = 31 * hash + (this.libraryVersion != null ? this.libraryVersion.hashCode() : 0);
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
            final SelectedLibrary other = (SelectedLibrary) obj;
            if ((this.filename == null) ? (other.filename != null) : !this.filename.equals(other.filename)) {
                return false;
            }
            if (this.libraryVersion != other.libraryVersion && (this.libraryVersion == null || !this.libraryVersion.equals(other.libraryVersion))) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "SelectedLibrary{" + "filename=" + filename + ", libraryVersion=" + libraryVersion + '}'; // NOI18N
        }

    }

    /**
     * Validator for selected JS libraries.
     */
    public interface JavaScriptLibrariesValidator {

        /**
         * Constant for a valid result of JS libraries validation.
         */
        Pair<Set<SelectedLibrary>, String> VALID_RESULT = Pair.of(Collections.<SelectedLibrary>emptySet(), null);

        /**
         * Validate given libraries.
         * @param librariesFolder folder for libraries
         * @param newLibraries new libraries to validate (without default libraries)
         * @return pair of invalid libraries together with the error message; empty set and {@code null}
         * if the libraries are valid
         * @see #VALID_RESULT
         */
        Pair<Set<SelectedLibrary>, String> validate(String librariesFolder, Set<SelectedLibrary> newLibraries);

    }

    private static final class LibrariesTable extends JTable {

        private static final long serialVersionUID = 1578314546784244L;


        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            row = convertRowIndexToModel(row);
            if (column != 1) {
                return super.getCellEditor(row, column);
            }
            LibrariesTableModel model = (LibrariesTableModel) getModel();
            JComboBox versionsComboBox = new JComboBox(model.getItems().get(row).getVersions());
            versionsComboBox.setRenderer(new VersionsRenderer(versionsComboBox.getRenderer()));
            return new DefaultCellEditor(versionsComboBox);
        }

    }

    private static final class VersionsRenderer implements ListCellRenderer {

        private final ListCellRenderer defaultRenderer;


        public VersionsRenderer(ListCellRenderer defaultRenderer) {
            this.defaultRenderer = defaultRenderer;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return defaultRenderer.getListCellRendererComponent(list, getLabel((LibraryVersion) value), index, isSelected, cellHasFocus);
        }

        @NbBundle.Messages({
            "# {0} - library version",
            "# {1} - library type",
            "VersionsRenderer.label={0} {1}",
            "VersionsRenderer.type.minified=minified",
            "VersionsRenderer.type.documented=documented"
        })
        public static String getLabel(LibraryVersion libraryVersion) {
            String version = libraryVersion.getLibraryVersion();
            String rawType = libraryVersion.getType();
            String type;
            if (WebClientLibraryManager.VOL_DOCUMENTED.equals(rawType)) {
                type = Bundle.VersionsRenderer_type_documented();
            } else if (WebClientLibraryManager.VOL_MINIFIED.equals(rawType)) {
                type = Bundle.VersionsRenderer_type_minified();
            } else if (WebClientLibraryManager.VOL_REGULAR.equals(rawType)) {
                type = ""; // NOI18N
            } else {
                assert false : "Unknown library type: " + libraryVersion; //NOI18N
                // fallback
                type = ""; // NOI18N
            }
            return Bundle.VersionsRenderer_label(version, type).trim();
        }

    }

    private static final class LibrariesTableModel extends AbstractTableModel implements PropertyChangeListener {

        private static final long serialVersionUID = -6832132354654L;

        private static final Comparator<Library> LIBRARY_COMPARATOR = new LibraryComparator();

        // @GuardedBy("EDT")
        private final List<ModelItem> items = new ArrayList<ModelItem>();

        private volatile boolean populating = false;


        private LibrariesTableModel() {
            populateModel();
        }

        static LibrariesTableModel create() {
            LibrariesTableModel model = new LibrariesTableModel();
            WebClientLibraryManager libraryManager = WebClientLibraryManager.getDefault();
            libraryManager.addPropertyChangeListener(WeakListeners.propertyChange(model, libraryManager));
            return model;
        }

        void populateModel() {
            assert EventQueue.isDispatchThread();
            items.clear();
            populating = true;
            fireTableDataChanged();
            JavaScriptLibrarySelectionPanel.RP.post(new Runnable() {
                @Override
                public void run() {
                    final Map<String, List<Library>> libraryMap = new ConcurrentHashMap<>();
                    for (Library lib : /*LibraryManager.getDefault().getLibraries()*/WebClientLibraryManager.getDefault().getLibraries()) {
                        if (WebClientLibraryManager.TYPE.equals(lib.getType())) {
                            String name = getRealName(lib);
                            List<Library> libs = libraryMap.get(name);
                            if (libs == null) {
                                libs = new ArrayList<>();
                                libraryMap.put(name, libs);
                            }
                            libs.add(lib);
                        }
                    }
                    // sort libs
                    for (List<Library> libs : libraryMap.values()) {
                        Collections.sort(libs, LIBRARY_COMPARATOR);
                    }
                    // refresh ui
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            assert EventQueue.isDispatchThread();
                            for (List<Library> libs : libraryMap.values()) {
                                items.add(new ModelItem(libs));
                            }
                            // sort libraries according their name:
                            Collections.sort(items, new Comparator<ModelItem>() {
                                @Override
                                public int compare(ModelItem o1, ModelItem o2) {
                                    return o1.getSimpleDisplayName().toLowerCase().compareTo(
                                            o2.getSimpleDisplayName().toLowerCase());
                                }
                            });
                            populating = false;
                            fireTableDataChanged();
                        }
                    });
                }
            });
        }

        @Override
        public int getRowCount() {
            assert EventQueue.isDispatchThread();
            if (populating) {
                return 1;
            }
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @NbBundle.Messages({
            "JavaScriptLibrarySelectionPanel.column.library=Library",
            "JavaScriptLibrarySelectionPanel.column.version=Version"
        })
        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return Bundle.JavaScriptLibrarySelectionPanel_column_library();
            }
            if (columnIndex == 1) {
                return Bundle.JavaScriptLibrarySelectionPanel_column_version();
            }
            assert false : "Unknown column index: " + columnIndex; // NOI18N
            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (populating) {
                return false;
            }
            return columnIndex == 1;
        }

        @NbBundle.Messages("JavaScriptLibrarySelectionPanel.data.loading=<html><i>Loading...</i></html>")
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            assert EventQueue.isDispatchThread();
            if (populating) {
                if (columnIndex == 0) {
                    return Bundle.JavaScriptLibrarySelectionPanel_data_loading();
                }
                if (columnIndex == 1) {
                    return null;
                }
                assert false : "Unknown column index: " + columnIndex; // NOI18N
                return null;
            }
            ModelItem modelItem = items.get(rowIndex);
            if (columnIndex == 0) {
                return modelItem.getSimpleDisplayName();
            }
            if (columnIndex == 1) {
                return VersionsRenderer.getLabel(modelItem.getSelectedVersion());
            }
            assert false : "Unknown column index: " + columnIndex; // NOI18N
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            assert EventQueue.isDispatchThread();
            assert !populating;
            ModelItem modelItem = items.get(rowIndex);
            if (columnIndex == 1) {
                modelItem.setSelectedVersion((LibraryVersion) aValue);
                return;
            }
            assert false : "Unknown column index: " + columnIndex; //NOI18N
        }

        boolean isPopulating() {
            return populating;
        }

        List<ModelItem> getItems() {
            assert EventQueue.isDispatchThread();
            return items;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (WebClientLibraryManager.PROPERTY_LIBRARIES.equals(evt.getPropertyName())) {
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        populateModel();
                    }
                });
            }
        }

    }

    private static final class LibrariesListModel extends AbstractListModel {

        private static final long serialVersionUID = -57683546574861110L;


        private static final Comparator<SelectedLibrary> SELECTED_LIBRARIES_COMPARATOR = new Comparator<SelectedLibrary>() {
            @Override
            public int compare(SelectedLibrary library1, SelectedLibrary library2) {
                if (library1.isDefault() && !library2.isDefault()) {
                    return 1;
                }
                if (!library1.isDefault() && library2.isDefault()) {
                    return -1;
                }
                return SelectedLibraryRenderer.getLabel(library1).compareToIgnoreCase(SelectedLibraryRenderer.getLabel(library2));
            }
        };

        private final List<SelectedLibrary> libraries;


        public LibrariesListModel(List<SelectedLibrary> libraries) {
            this.libraries = libraries;
        }

        @Override
        public int getSize() {
            return libraries.size();
        }

        @Override
        public SelectedLibrary getElementAt(int index) {
            return libraries.get(index);
        }

        public void fireContentsChanged() {
            sortLibraries();
            fireContentsChanged(this, 0, libraries.size() - 1);
        }

        /**
         * Make selected libraries unique and sort them.
         */
        private void sortLibraries() {
            Collections.sort(libraries, SELECTED_LIBRARIES_COMPARATOR);
        }

    }

    private static final class SelectedLibraryRenderer implements ListCellRenderer {

        private final ListCellRenderer defaultRenderer;
        private final Set<SelectedLibrary> failedLibraries;
        private final Set<SelectedLibrary> invalidLibraries;


        public SelectedLibraryRenderer(ListCellRenderer defaultRenderer, Set<SelectedLibrary> failedLibraries, Set<SelectedLibrary> invalidLibraries) {
            this.defaultRenderer = defaultRenderer;
            this.failedLibraries = failedLibraries;
            this.invalidLibraries = invalidLibraries;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            assert EventQueue.isDispatchThread();
            SelectedLibrary selectedLibrary = (SelectedLibrary) value;
            Component component = defaultRenderer.getListCellRendererComponent(list, getLabel(selectedLibrary), index, isSelected, cellHasFocus);
            if (selectedLibrary.isDefault()) {
                component.setEnabled(false);
            }
            if (failedLibraries.contains(selectedLibrary)
                    || invalidLibraries.contains(selectedLibrary)) {
                component.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
            }
            return component;
        }

        @NbBundle.Messages({
            "# {0} - library name or filename",
            "# {1} - library version or file path",
            "JavaScriptLibrarySelectionPanel.SelectedLibraryRenderer.label={0} ({1})",
        })
        static String getLabel(SelectedLibrary selectedLibrary) {
            List<String> filePaths = selectedLibrary.getFilePaths();
            assert !filePaths.isEmpty() : "No files for library: " + selectedLibrary;
            String label;
            if (selectedLibrary.isDefault()) {
                assert filePaths.size() == 1 : "Exactly one file expected but found " + filePaths.size() + " for default library " + selectedLibrary;
                String path = filePaths.get(0);
                int slashIndex = path.lastIndexOf('/'); // NOI18N
                if (slashIndex == -1) {
                    label = path;
                } else {
                    label = Bundle.JavaScriptLibrarySelectionPanel_SelectedLibraryRenderer_label(path.substring(slashIndex + 1), path.substring(0, slashIndex));
                }
            } else {
                label = Bundle.JavaScriptLibrarySelectionPanel_SelectedLibraryRenderer_label(getRealDisplayName(selectedLibrary), VersionsRenderer.getLabel(selectedLibrary.getLibraryVersion()));
            }
            return label;
        }

    }

    private static final class ModelItem {

        // @GuardedBy("EDT")
        private final Set<LibraryVersion> versions;

        // @GuardedBy("EDT")
        private LibraryVersion selectedVersion;


        public ModelItem(List<Library> libraries) {
            assert EventQueue.isDispatchThread();
            versions = createVersions(libraries);
            selectedVersion = versions.iterator().next();
        }

        public String getSimpleDisplayName() {
            return getRealDisplayName(getLibrary());
        }

        public String getDescription() {
            return getLibrary().getDescription();
        }

        public LibraryVersion[] getVersions() {
            assert EventQueue.isDispatchThread();
            return versions.toArray(new LibraryVersion[versions.size()]);
        }

        public LibraryVersion getSelectedVersion() {
            assert EventQueue.isDispatchThread();
            return selectedVersion;
        }

        public void setSelectedVersion(LibraryVersion selectedVersion) {
            assert EventQueue.isDispatchThread();
            assert selectedVersion != null;
            this.selectedVersion = selectedVersion;
        }

        private Set<LibraryVersion> createVersions(List<Library> libraries) {
            Set<LibraryVersion> libraryVersions = new LinkedHashSet<LibraryVersion>();
            for (Library library : libraries) {
                LibraryVersion libraryVersion = null;
                if (!library.getContent(WebClientLibraryManager.VOL_DOCUMENTED).isEmpty()) {
                    libraryVersion = new LibraryVersion(library, WebClientLibraryManager.VOL_DOCUMENTED);
                    libraryVersions.add(libraryVersion);
                }
                if (!library.getContent(WebClientLibraryManager.VOL_REGULAR).isEmpty()) {
                    libraryVersion = new LibraryVersion(library, WebClientLibraryManager.VOL_REGULAR);
                    libraryVersions.add(libraryVersion);
                }
                if (!library.getContent(WebClientLibraryManager.VOL_MINIFIED).isEmpty()) {
                    libraryVersion = new LibraryVersion(library, WebClientLibraryManager.VOL_MINIFIED);
                    libraryVersions.add(libraryVersion);
                }
                if (libraryVersion == null) {
                    assert false : "Unknown library version: " + library.getName(); //NOI18N
                }
            }
            return libraryVersions;
        }

        private Library getLibrary() {
            assert EventQueue.isDispatchThread();
            return versions.iterator().next().getLibrary();
        }

    }

    static final class LibraryComparator implements Comparator<Library> {

        // #230467
        private static final Pattern SANITIZE_VERSION_PATTERN = Pattern.compile("[^.0-9]"); // NOI18N

        private static final String[] POSTVERSIONS = new String[] {"patch"}; // NOI18N
        private static final String[] PREVERSIONS = new String[] {"rc", "pre", "beta"}; // NOI18N

        @Override
        public int compare(Library library1, Library library2) {
            String fullVersion1 = library1.getProperties().get(WebClientLibraryManager.PROPERTY_VERSION);
            String fullVersion2 = library2.getProperties().get(WebClientLibraryManager.PROPERTY_VERSION);
            String sanitizedVersion1 = sanitize(fullVersion1);
            String sanitizedVersion2 = sanitize(fullVersion2);
            Version ver1 = Version.fromDottedNotationWithFallback(sanitizedVersion1);
            Version ver2 = Version.fromDottedNotationWithFallback(sanitizedVersion2);
            if (ver1.equals(ver2)) {
                Integer result = compareSameVersions(fullVersion1, fullVersion2);
                if (result != null) {
                    return result;
                }
                if (!library1.getContent(WebClientLibraryManager.VOL_DOCUMENTED).isEmpty()) {
                    return -1;
                }
                if (!library2.getContent(WebClientLibraryManager.VOL_DOCUMENTED).isEmpty()) {
                    return 1;
                }
                if (!library1.getContent(WebClientLibraryManager.VOL_REGULAR).isEmpty()) {
                    return -1;
                }
                if (!library2.getContent(WebClientLibraryManager.VOL_REGULAR).isEmpty()) {
                    return 1;
                }
                return 0;
            }
            return ver1.isBelowOrEqual(ver2) ? 1 : -1;
        }

        @CheckForNull
        public Integer compareSameVersions(String fullVersion1, String fullVersion2) {
            // same version, check for rc, pre, beta
            String fullVer1 = fullVersion1.toLowerCase();
            String fullVer2 = fullVersion2.toLowerCase();
            Integer result = compareVersions(POSTVERSIONS, fullVer1, fullVer2, true);
            if (result != null) {
                return result;
            }
            return compareVersions(PREVERSIONS, fullVer1, fullVer2, false);
        }

        @CheckForNull
        private Integer compareVersions(String[] versions, String fullVersion1, String fullVersion2, boolean preferVersion) {
            boolean found1 = false;
            boolean found2 = false;
            for (int i = 0; i < versions.length; i++) {
                boolean is1 = fullVersion1.contains(versions[i]);
                boolean is2 = fullVersion2.contains(versions[i]);
                if (is1 && is2) {
                    return fullVersion2.compareTo(fullVersion1);
                }
                if (is1 || is2) {
                    if (is1) {
                        found1 = true;
                    }
                    if (is2) {
                        found2 = true;
                    }
                    String other = is1 ? fullVersion2 : fullVersion1;
                    for (int j = i; j < versions.length; j++) {
                        if (other.contains(versions[j])) {
                            return is1 ? -1 : 1;
                        }
                    }
                }
            }
            if (found1 || found2) {
                assert !found1 || !found2 : fullVersion1 + " ::" + fullVersion2 + "[" + found1 + " :: " + found2 + "]";
                if (preferVersion) {
                    return found1 ? -1 : 1;
                }
                return found1 ? 1 : -1;
            }
            return null;
        }

        public String sanitize(String version) {
            String[] parts = SANITIZE_VERSION_PATTERN.split(version);
            if (parts.length == 0) {
                return version;
            }
            return parts[0];
        }

    }

}
