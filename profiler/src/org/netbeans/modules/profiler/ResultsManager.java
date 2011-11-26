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

package org.netbeans.modules.profiler;

import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.ProfilerEngineSettings;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.TargetAppRunner;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.CommonUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.results.ProfilingResultsDispatcher;
import org.netbeans.lib.profiler.results.ResultsSnapshot;
import org.netbeans.lib.profiler.results.RuntimeCCTNode;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.AllocMemoryResultsDiff;
import org.netbeans.lib.profiler.results.memory.AllocMemoryResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsDiff;
import org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import java.awt.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.lib.profiler.utils.StringUtils;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.project.ProjectStorage;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;


/** An manager for management/notifications about obtainer profiling results.
 *
 * @author Tomas Hurka
 * @author Ian Formanek
 */
public final class ResultsManager {
    final private static Logger LOGGER = Logger.getLogger(ResultsManager.class.getName());
    
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    //  /**
    //   * Resets collected results (either memory or CPU, depending on current mode).
    //   */
    //  public void resetCollectors() {
    //    reset();
    //    int instr = Profiler.getDefault().getTargetAppRunner().getProfilerClient().getCurrentInstrType();
    //    ProfilingResultsDispatcher.getDefault().reset();
    //    if (instr == CommonConstants.INSTR_RECURSIVE_FULL || instr == CommonConstants.INSTR_RECURSIVE_SAMPLED) {
    //      // TODO reset cpu profiling results collectors
    ////      CPUCallGraphBuilder.resetCollectors();
    //    } else if (instr == CommonConstants.INSTR_OBJECT_ALLOCATIONS || instr == CommonConstants.INSTR_OBJECT_LIVENESS) {
    //      MemoryCallGraphBuilder mcgb = Profiler.getDefault().getTargetAppRunner().getProfilerClient().getMemoryCallGraphBuilder();
    //      mcgb.resetCollectors();
    //    }
    //    fireResultsReset();
    //  }
    static class SelectedFile {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        FileObject folder;
        String fileExt;
        String fileName;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        SelectedFile(FileObject folder, String fileName, String fileExt) {
            this.folder = folder;
            this.fileName = fileName;
            this.fileExt = fileExt;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String PROFILED_APP_TERMINATED_MSG = NbBundle.getMessage(ResultsManager.class,
                                                                                  "ResultsManager_ProfiledAppTerminatedMsg"); // NOI18N
    private static final String DATA_NOT_AVAILABLE_MSG = NbBundle.getMessage(ResultsManager.class,
                                                                             "ResultsManager_DataNotAvailableMsg"); // NOI18N
    private static final String OUT_OF_MEMORY_MSG = NbBundle.getMessage(ResultsManager.class, "ResultsManager_OutOfMemoryMsg"); // NOI18N
    private static final String SNAPSHOT_DELETE_FAILED_MSG = NbBundle.getMessage(ResultsManager.class,
                                                                                 "ResultsManager_SnapshotDeleteFailedMsg"); // NOI18N
    private static final String SNAPSHOT_SAVE_FAILED_MSG = NbBundle.getMessage(ResultsManager.class,
                                                                               "ResultsManager_SnapshotSaveFailedMsg"); // NOI18N
    private static final String CANT_FIND_SNAPSHOT_LOCATION_MSG = NbBundle.getMessage(ResultsManager.class,
                                                                                      "ResultsManager_CantFindSnapshotLocationMsg"); // NOI18N
    private static final String SNAPSHOT_CREATE_IN_PROJECT_FAILED_MSG = NbBundle.getMessage(ResultsManager.class,
                                                                                            "ResultsManager_SnapshotCreateInProjectFailedMsg"); // NOI18N
    private static final String SNAPSHOT_LOAD_FAILED_MSG = NbBundle.getMessage(ResultsManager.class,
                                                                               "ResultsManager_SnapshotLoadFailedMsg"); // NOI18N
    private static final String SNAPSHOTS_LOAD_FAILED_MSG = NbBundle.getMessage(ResultsManager.class,
                                                                                "ResultsManager_SnapshotsLoadFailedMsg"); // NOI18N
    private static final String OBTAIN_SAVED_SNAPSHOTS_FAILED_MSG = NbBundle.getMessage(ResultsManager.class,
                                                                                        "ResultsManager_ObtainSavedSnapshotsFailedMsg"); // NOI18N
    private static final String SELECT_DIR_DIALOG_CAPTION = NbBundle.getMessage(ResultsManager.class,
                                                                                "ResultsManager_SelectDirDialogCaption"); // NOI18N
    private static final String SAVE_BUTTON_NAME = NbBundle.getMessage(ResultsManager.class, "ResultsManager_SaveButtonName"); // NOI18N
    private static final String OVERWRITE_FILE_DIALOG_CAPTION = NbBundle.getMessage(ResultsManager.class,
                                                                                    "ResultsManager_OverwriteFileDialogCaption"); // NOI18N
    private static final String OVERWRITE_FILE_DIALOG_MSG = NbBundle.getMessage(ResultsManager.class,
                                                                                "ResultsManager_OverwriteFileDialogMsg"); // NOI18N
    private static final String FILE_DELETE_FAILED_MSG = NbBundle.getMessage(ResultsManager.class,
                                                                             "ResultsManager_FileDeleteFailedMsg"); // NOI18N
    private static final String SNAPSHOT_EXPORT_FAILED_MSG = NbBundle.getMessage(ResultsManager.class,
                                                                                 "ResultsManager_SnapshotExportFailedMsg"); // NOI18N
    private static final String SAVE_SNAPSHOTS_DIALOG_CAPTION = NbBundle.getMessage(ResultsManager.class,
                                                                                    "ResultsManager_SaveSnapshotsDialogCaption"); // NOI18N
    private static final String SAVE_SNAPSHOTS_DIALOG_MSG = NbBundle.getMessage(ResultsManager.class,
                                                                                "ResultsManager_SaveSnapshotsDialogMsg"); // NOI18N
    private static final String SELECT_FILE_OR_DIR_DIALOG_CAPTION = NbBundle.getMessage(ResultsManager.class,
                                                                                        "ResultsManager_SelectFileOrDirDialogCaption"); // NOI18N
    private static final String PROFILER_SNAPSHOT_FILE_FILTER = NbBundle.getMessage(ResultsManager.class,
                                                                                    "ResultsManager_ProfilerSnapshotFileFilter"); // NOI18N
    private static final String PROFILER_HEAPDUMP_FILE_FILTER = NbBundle.getMessage(ResultsManager.class,
                                                                                    "ResultsManager_ProfilerHeapdumpFileFilter"); // NOI18N
    private static final String SNAPSHOT_CREATE_FAILED_MSG = NbBundle.getMessage(ResultsManager.class,
                                                                                 "ResultsManager_SnapshotCreateFailedMsg"); // NOI18N
    private static final String OUT_OF_MEMORY_SAVING = NbBundle.getMessage(LoadedSnapshot.class,
                                                                           "ResultsManager_OutOfMemorySavingMsg"); // NOI18N
    private static final String CANNOT_COMPARE_SNAPSHOTS_MSG = NbBundle.getMessage(LoadedSnapshot.class,
                                                                                   "ResultsManager_CannotCompareSnapshotsMsg"); // NOI18N
    private static final String DIRECTORY_DOESNT_EXIST_CAPTION = NbBundle.getMessage(LoadedSnapshot.class,
                                                                                     "ResultsManager_DirectoryDoesntExistCaption"); // NOI18N
    private static final String DIRECTORY_DOESNT_EXIST_MSG = NbBundle.getMessage(LoadedSnapshot.class,
                                                                                 "ResultsManager_DirectoryDoesntExistMsg"); // NOI18N
    private static final String CANNOT_OPEN_SNAPSHOT_MSG = NbBundle.getMessage(LoadedSnapshot.class,
                                                                                 "ResultsManager_CannotOpenSnapshotMsg"); // NOI18N
    private static final String CPU_SNAPSHOT_DISPLAYNAME = NbBundle.getMessage(ResultsManager.class,
                                                                                "ResultsManager_CpuSnapshotDisplayName"); // NOI18N
    private static final String MEMORY_SNAPSHOT_DISPLAYNAME = NbBundle.getMessage(ResultsManager.class,
                                                                                "ResultsManager_MemorySnapshotDisplayName"); // NOI18N
    private static final String HEAP_SNAPSHOT_DISPLAYNAME = NbBundle.getMessage(ResultsManager.class,
                                                                                "ResultsManager_HeapSnapshotDisplayName"); // NOI18N
                                                                                                                            // -----
    public static final String SNAPSHOT_EXTENSION = "nps"; // NOI18N
    public static final String HEAPDUMP_EXTENSION = "hprof"; // NOI18N
    /* see  org.netbeans.core.ui.sampler.SampleOutputStream.FILE_EXT */
    public static final String STACKTRACES_SNAPSHOT_EXTENSION = "npss"; // NOI18N 

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private ArrayList<LoadedSnapshot> loadedSnapshots = new ArrayList<LoadedSnapshot>();
    private File exportDir;
    private HashMap<FileObject, ProfilingSettings> settingsCache = new HashMap<FileObject, ProfilingSettings>();
    private HashMap<FileObject, Integer> typeCache = new HashMap<FileObject, Integer>();
    private Window mainWindow;
    private boolean resultsAvailable = false;
    
    private Lookup.Result<SnapshotsListener> snapshotListeners;
    private Lookup.Result<ResultsListener> resultsListeners;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    private ResultsManager() {
        Lookup l = Lookup.getDefault();
        snapshotListeners = l.lookupResult(SnapshotsListener.class);
        resultsListeners = l.lookupResult(ResultsListener.class);
    }

    private static class Singleton {
        final private static ResultsManager INSTANCE = new ResultsManager();
    }
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static ResultsManager getDefault() {
        return Singleton.INSTANCE;
    }

    public String getDefaultSnapshotFileName(LoadedSnapshot ls) {
        return "snapshot-" + ls.getSnapshot().getTimeTaken(); // NOI18N
    }
        
    public String getSnapshotDisplayName(String fileName, int snapshotType) {
        String displayName;
        if (fileName.startsWith("snapshot-")) { // NOI18N
            String time = fileName.substring("snapshot-".length(), fileName.length()); // NOI18N
            try {
                long timeStamp = Long.parseLong(time);
                displayName = StringUtils.formatUserDate(new Date(timeStamp));
            } catch (NumberFormatException e) {
                // file name is probably customized
                displayName = fileName;
            }
        } else {
            displayName = fileName;
        }
        switch (snapshotType) {
            case LoadedSnapshot.SNAPSHOT_TYPE_CPU:
                return MessageFormat.format(CPU_SNAPSHOT_DISPLAYNAME, new Object[] { displayName });
            case LoadedSnapshot.SNAPSHOT_TYPE_MEMORY_ALLOCATIONS:
            case LoadedSnapshot.SNAPSHOT_TYPE_MEMORY_LIVENESS:
                return MessageFormat.format(MEMORY_SNAPSHOT_DISPLAYNAME, new Object[] { displayName });
            default:
                return displayName;
        }
    }
    
    public String getHeapDumpDisplayName(String fileName) {
        String displayName;
        if (fileName.startsWith("heapdump-")) { // NOI18N
            String time = fileName.substring("heapdump-".length(), fileName.length()); // NOI18N
            try {
                long timeStamp = Long.parseLong(time);
                displayName = StringUtils.formatUserDate(new Date(timeStamp));
            } catch (NumberFormatException e) {
                // file name is probably customized
                displayName = fileName;
            }
        } else {
            displayName = fileName;
        }
        return MessageFormat.format(HEAP_SNAPSHOT_DISPLAYNAME, new Object[] { displayName });
    }

    public LoadedSnapshot[] getLoadedSnapshots() {
        return loadedSnapshots.toArray(new LoadedSnapshot[0]);
    }

    public LoadedSnapshot getSnapshotFromFileObject(FileObject fo) {
        LoadedSnapshot ls = findAlreadyLoadedSnapshot(fo);

        if (ls != null) {
            return ls;
        }

        try {
            return loadSnapshotFromFileObject(fo);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);

            return null;
        }
    }

    public ProfilingSettings getSnapshotSettings(FileObject fo) {
        ProfilingSettings settings = settingsCache.get(fo);

        if ((settings == null) && !settingsCache.containsKey(fo)) {
            settings = readSettingsFromFile(fo);
            settingsCache.put(fo, settings);
        }

        return settings;
    }

    public int getSnapshotType(FileObject fo) {
        Integer type = typeCache.get(fo);

        if (type == null) {
            type = Integer.valueOf(readTypeFromFile(fo));
            typeCache.put(fo, type);
        }

        return type.intValue();
    }

    public void cctEstablished(RuntimeCCTNode runtimeCCTNode) {
        if (NetBeansProfiler.getDefaultNB().getProfilingState() == Profiler.PROFILING_INACTIVE) return; // Calibration, ignore
        resultsAvailable = true;
        fireResultsAvailable();
    }

    public void cctReset() {
        resultsAvailable = false;
        fireResultsReset();
    }

    public void closeSnapshot(LoadedSnapshot ls) {
        if (ls != null) {
            loadedSnapshots.remove(ls);
            fireSnapshotRemoved(ls);
        }
    }

    public void compareSnapshots(FileObject snapshot1FO, FileObject snapshot2FO) {
        LoadedSnapshot s1 = null;
        LoadedSnapshot s2 = null;

        try {
            s1 = findAlreadyLoadedSnapshot(snapshot1FO);

            if (s1 == null) {
                s1 = loadSnapshotFromFileObject(snapshot1FO);
            }

            s2 = findAlreadyLoadedSnapshot(snapshot2FO);

            if (s2 == null) {
                s2 = loadSnapshotFromFileObject(snapshot2FO);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, MessageFormat.format(SNAPSHOT_LOAD_FAILED_MSG, new Object[] { e.getMessage() }), e);

            return;
        }

        if ((s1 != null) && (s2 != null)) {
            compareSnapshots(s1, s2);
        } else {
            ProfilerDialogs.displayError(SNAPSHOTS_LOAD_FAILED_MSG);
        }
    }

    public void compareSnapshots(LoadedSnapshot s1, LoadedSnapshot s2) {
        ResultsSnapshot diff = null;

        if (s1.getSnapshot() instanceof AllocMemoryResultsSnapshot && s2.getSnapshot() instanceof AllocMemoryResultsSnapshot) {
            diff = new AllocMemoryResultsDiff((AllocMemoryResultsSnapshot) s1.getSnapshot(),
                                              (AllocMemoryResultsSnapshot) s2.getSnapshot());
        }
        else if (s1.getSnapshot() instanceof LivenessMemoryResultsSnapshot
                     && s2.getSnapshot() instanceof LivenessMemoryResultsSnapshot) {
            diff = new LivenessMemoryResultsDiff((LivenessMemoryResultsSnapshot) s1.getSnapshot(),
                                                 (LivenessMemoryResultsSnapshot) s2.getSnapshot());
        }

        if (diff != null) {
            SnapshotsDiffWindow sdw = SnapshotsDiffWindow.get(diff, s1, s2);
            sdw.open();
            sdw.requestActive();
        } else {
            ProfilerDialogs.displayError(MessageFormat.format(CANNOT_COMPARE_SNAPSHOTS_MSG,
                                                               new Object[] { s1.getFile().getName(), s2.getFile().getName() }));
        }
    }

    public void deleteSnapshot(FileObject snapshotFile) {
        LoadedSnapshot ls = findLoadedSnapshot(FileUtil.toFile(snapshotFile));

        try {
            snapshotFile.delete();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MessageFormat.format(SNAPSHOT_DELETE_FAILED_MSG, new Object[] { e.getMessage() }), e);

            return; // do not proceed with removing the snapshot from internal structures
        }

        if (ls != null) { // if the snapshot has been loaded, remove the internal structures as well
            loadedSnapshots.remove(ls);
            fireSnapshotRemoved(ls);
        }
    }

    public void exportSnapshots(final FileObject[] selectedSnapshots) {
        assert (selectedSnapshots != null);
        assert (selectedSnapshots.length > 0);

        if (selectedSnapshots.length == 1) {
            SelectedFile sf = selectSnapshotTargetFile(selectedSnapshots[0].getName(),
                                                       selectedSnapshots[0].getExt().equals(HEAPDUMP_EXTENSION));

            if ((sf != null) && checkFileExists(sf)) {
                exportSnapshot(selectedSnapshots[0], sf.folder, sf.fileName, sf.fileExt);
            }
        } else {
            JFileChooser chooser = new JFileChooser();

            if (exportDir != null) {
                chooser.setCurrentDirectory(exportDir);
            }

            chooser.setDialogTitle(SELECT_DIR_DIALOG_CAPTION);
            chooser.setApproveButtonText(SAVE_BUTTON_NAME);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setMultiSelectionEnabled(false);

            if (chooser.showSaveDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();

                if (!file.exists()) {
                    if (!ProfilerDialogs.displayConfirmation(DIRECTORY_DOESNT_EXIST_MSG, DIRECTORY_DOESNT_EXIST_CAPTION)) {
                        return; // cancelled by the user
                    }

                    file.mkdir();
                }

                exportDir = file;

                FileObject dir = FileUtil.toFileObject(FileUtil.normalizeFile(file));

                for (int i = 0; i < selectedSnapshots.length; i++) {
                    exportSnapshot(selectedSnapshots[i], dir, selectedSnapshots[i].getName(), selectedSnapshots[i].getExt());
                }
            }
        }
    }

    public LoadedSnapshot findLoadedSnapshot(ResultsSnapshot snapshot) {
        Iterator it = loadedSnapshots.iterator();

        while (it.hasNext()) {
            LoadedSnapshot ls = (LoadedSnapshot) it.next();

            if (ls.getSnapshot() == snapshot) {
                return ls;
            }
        }

        return null;
    }

    public LoadedSnapshot findLoadedSnapshot(File snapshotFile) {
        Iterator it = loadedSnapshots.iterator();

        while (it.hasNext()) {
            LoadedSnapshot ls = (LoadedSnapshot) it.next();

            if ((ls.getFile() != null) && ls.getFile().equals(snapshotFile)) {
                return ls;
            }
        }

        return null;
    }

    public boolean ideClosing() {
        Iterator it = loadedSnapshots.iterator();
        ArrayList /*<LoadedSnapshot*/ unsaved = new ArrayList();

        while (it.hasNext()) {
            LoadedSnapshot ls = (LoadedSnapshot) it.next();

            if (!ls.isSaved()) {
                unsaved.add(ls);
            }
        }

        if (unsaved.size() > 0) {
            Boolean ret = ProfilerDialogs.displayCancellableConfirmation(MessageFormat.format(
                    SAVE_SNAPSHOTS_DIALOG_MSG, new Object[] { "" + unsaved.size() }), // NOI18N
                    SAVE_SNAPSHOTS_DIALOG_CAPTION);

            if (Boolean.TRUE.equals(ret)) {
                Iterator unsIt = unsaved.iterator();

                while (unsIt.hasNext()) {
                    LoadedSnapshot ls = (LoadedSnapshot) unsIt.next();
                    saveSnapshot(ls);
                }

                return true; // exit the IDE
            } else if (Boolean.FALSE.equals(ret)) {
                return true; // exit the IDE
            } else {
                return false; // cancel
            }
        }

        return true;
    }
    
    private static final String HPROF_HEADER = "JAVA PROFILE 1.0"; // NOI18H
    private static final long MIN_HPROF_SIZE = 1024*1024L;
    public static boolean checkHprofFile(File file) {
        try {
            if (file.isFile() && file.canRead() && file.length()>MIN_HPROF_SIZE) { // heap dump must be 1M and bigger
                byte[] prefix = new byte[HPROF_HEADER.length()+4];
                RandomAccessFile raf = new RandomAccessFile(file,"r");  // NOI18H
                raf.readFully(prefix);
                raf.close();
                if (new String(prefix).startsWith(HPROF_HEADER)) {
                    return true;
                }
            }
        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
        return false;
    }

    public FileObject[] listSavedHeapdumps(Lookup.Provider project, File directory) {
        try {
            FileObject snapshotsFolder = null;
                    
            if (project == null && directory != null) {
                snapshotsFolder = FileUtil.toFileObject(directory);
            } else {
                snapshotsFolder = ProjectStorage.getSettingsFolder(project, false);
            }

            if (snapshotsFolder == null) {
                return new FileObject[0];
            }

            snapshotsFolder.refresh();

            FileObject[] children = snapshotsFolder.getChildren();

            ArrayList /*<FileObject>*/ files = new ArrayList /*<FileObject>*/();

            for (int i = 0; i < children.length; i++) {
                FileObject child = children[i];
                if (checkHprofFile(FileUtil.toFile(children[i])))
                    files.add(child);
            }

            Collections.sort(files,
                             new Comparator() {
                    public int compare(Object o1, Object o2) {
                        FileObject f1 = (FileObject) o1;
                        FileObject f2 = (FileObject) o2;

                        return f1.getName().compareTo(f2.getName());
                    }
                });

            FileObject[] ret = new FileObject[files.size()];
            files.toArray(ret);

            return ret;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MessageFormat.format(OBTAIN_SAVED_SNAPSHOTS_FAILED_MSG, new Object[] { e.getMessage() }), e);

            return new FileObject[0];
        }
    }

    public FileObject[] listSavedSnapshots(Lookup.Provider project, File directory) {
        try {
            FileObject snapshotsFolder = null;
                    
            if (project == null && directory != null) {
                snapshotsFolder = FileUtil.toFileObject(directory);
            } else {
                snapshotsFolder = ProjectStorage.getSettingsFolder(project, false);
            }

            if (snapshotsFolder == null) {
                return new FileObject[0];
            }

            snapshotsFolder.refresh();

            FileObject[] children = snapshotsFolder.getChildren();

            ArrayList /*<FileObject>*/ files = new ArrayList /*<FileObject>*/();

            for (int i = 0; i < children.length; i++) {
                FileObject child = children[i];

                if (child.getExt().equalsIgnoreCase(SNAPSHOT_EXTENSION)) {
                    files.add(child);
                }
            }

            Collections.sort(files,
                             new Comparator() {
                    public int compare(Object o1, Object o2) {
                        FileObject f1 = (FileObject) o1;
                        FileObject f2 = (FileObject) o2;

                        return f1.getName().compareTo(f2.getName());
                    }
                });

            FileObject[] ret = new FileObject[files.size()];
            files.toArray(ret);

            return ret;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MessageFormat.format(OBTAIN_SAVED_SNAPSHOTS_FAILED_MSG, new Object[] { e.getMessage() }), e);

            return new FileObject[0];
        }
    }

    public LoadedSnapshot loadSnapshot(FileObject selectedFile) {
        try {
            return loadSnapshotImpl(selectedFile);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MessageFormat.format(SNAPSHOT_LOAD_FAILED_MSG, new Object[] { e.getMessage() }), e);

            return null;
        }
    }

    public LoadedSnapshot[] loadSnapshots(FileObject[] selectedFiles) {
        LoadedSnapshot[] ret = new LoadedSnapshot[selectedFiles.length];

        for (int i = 0; i < selectedFiles.length; i++) {
            try {
                if (selectedFiles[i] != null) {
                    ret[i] = loadSnapshotImpl(selectedFiles[i]);
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, MessageFormat.format(SNAPSHOT_LOAD_FAILED_MSG, new Object[] { e.getMessage() }), e);
            }
        }

        return ret;
    }
    
    public void openSnapshot(File snapshot) {
        File sf = FileUtil.normalizeFile(snapshot);
        FileObject snapshotFo = FileUtil.toFileObject(sf);
        openSnapshot(snapshotFo);
    }
    
    public void openSnapshot(FileObject snapshotFo) {
        try {
            DataObject snapshotDo = DataObject.find(snapshotFo);
            OpenCookie open = snapshotDo.getCookie(OpenCookie.class);
            if (open != null) {
                open.open();
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void openSnapshot(LoadedSnapshot ls) {
        openSnapshot(ls, CommonConstants.SORTING_COLUMN_DEFAULT, false); // target component decides which column will be used for sorting
    }

    public void openSnapshot(final LoadedSnapshot ls, final int sortingColumn, final boolean sortingOrder) {
        if (ls == null) ProfilerDialogs.displayError(CANNOT_OPEN_SNAPSHOT_MSG);
        else CommonUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                SnapshotResultsWindow srw = SnapshotResultsWindow.get(ls, sortingColumn, sortingOrder);
                srw.open();
                srw.requestActive();
            }
        });
    }

    public void openSnapshots(LoadedSnapshot[] loaded) {
        LoadedSnapshot loadedSnapshot = null;

        try {
            SnapshotResultsWindow srw = null;

            for (int i = 0; i < loaded.length; i++) {
                loadedSnapshot = loaded[i];

                if (loaded[i] != null) {
                    srw = SnapshotResultsWindow.get(loadedSnapshot);
                    srw.open();
                }
            }

            if (srw != null) {
                srw.requestActive(); // activate the last one
            }
        } catch (Exception e) {
            ProfilerLogger.log(e);

            NotifyDescriptor.Message loadFailed = new NotifyDescriptor.Message(NbBundle.getMessage(ResultsManager.class,
                                                                                                   "ResultsManager_SnapshotLoadFailed",
                                                                                                   new Object[] {
                                                                                                       loadedSnapshot.getFile()
                                                                                                                     .getAbsolutePath()
                                                                                                   })); // NOI18N
            DialogDisplayer.getDefault().notify(loadFailed);
        }
    }

    public LoadedSnapshot prepareSnapshot() {
        return prepareSnapshot(true);
    }

    public LoadedSnapshot prepareSnapshot(boolean reqeustData) {
        ResultsSnapshot snapshot = null;

        if (!resultsAvailable()) {
            return null;
        }

        try {
            final TargetAppRunner runner = Profiler.getDefault().getTargetAppRunner();
            final ProfilerClient client = runner.getProfilerClient();
            final int currentInstrType = client.getCurrentInstrType();

            try {
                switch (currentInstrType) {
                    case ProfilerEngineSettings.INSTR_OBJECT_ALLOCATIONS:
                    case ProfilerEngineSettings.INSTR_OBJECT_LIVENESS:
                        snapshot = client.getMemoryProfilingResultsSnapshot(reqeustData);

                        break;
                    case ProfilerEngineSettings.INSTR_RECURSIVE_FULL:
                    case ProfilerEngineSettings.INSTR_RECURSIVE_SAMPLED:
                    case ProfilerEngineSettings.INSTR_NONE_SAMPLING:
                        snapshot = client.getCPUProfilingResultsSnapshot(reqeustData);

                        break;
                    case ProfilerEngineSettings.INSTR_CODE_REGION:
                        snapshot = client.getCodeRegionProfilingResultsSnapshot();

                        break;
                }
            } catch (ClientUtils.TargetAppOrVMTerminated e1) {
                LOGGER.log(Level.SEVERE, PROFILED_APP_TERMINATED_MSG, e1);
            } catch (CPUResultsSnapshot.NoDataAvailableException e2) {
                LOGGER.log(Level.SEVERE, DATA_NOT_AVAILABLE_MSG, e2);
            } catch (OutOfMemoryError e) {
                try {
                    reset(); // reset the client data
                    runner.resetTimers(); // reset the server data
                } catch (ClientUtils.TargetAppOrVMTerminated targetAppOrVMTerminated) {
                    // the target app has died; clean up all client data
                    runner.getProfilerClient().resetClientData();
                }

                LOGGER.log(Level.SEVERE, OUT_OF_MEMORY_MSG, e);
            }
        } finally {
            if (snapshot != null) {
                ProfilingSettings settings = new ProfilingSettings();
                Profiler.getDefault().getLastProfilingSettings().copySettingsInto(settings);
                settings.setSettingsName(Profiler.getDefault().getLastProfilingSettings().getSettingsName());

                Lookup.Provider profiledProject = NetBeansProfiler.getDefaultNB().getProfiledProject();

                return new LoadedSnapshot(snapshot, settings, null, profiledProject);
            }
        }

        return null;
    }

    /**
     * This should be called when the app is restarted or "Reset Collected Results" is invoked (because once this happened,
     * there are all sorts of data that's going to be deleted/changed, and an attempt to do something with old results displayed
     * here can cause big problems). It should also set the results panel invisible (or is it already happening?) etc.
     */
    public void reset() {
        ProfilingResultsDispatcher.getDefault().reset();
        resultsReset();
    }

    public boolean resultsAvailable() {
        return resultsAvailable;
    }

    public boolean saveSnapshot(LoadedSnapshot snapshot, FileObject profFile) {
        FileLock lock = null;
        DataOutputStream dos = null;

        try {
            lock = profFile.lock();

            OutputStream os = profFile.getOutputStream(lock);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            dos = new DataOutputStream(bos);
            //      System.out.println("Saving snapshot [" + snapshot.getSnapshot().getTimeTaken() + "]");
            snapshot.save(dos);
            dos.close();
            snapshot.setFile(FileUtil.toFile(profFile));
            snapshot.setSaved(true);
            fireSnapshotSaved(snapshot);
        } catch (IOException e) {
            try {
                if (dos != null) {
                    dos.close();
                }

                if (lock != null) {
                    profFile.delete(lock);
                }
            } catch (Exception e2) {
            }

            ProfilerDialogs.displayError(MessageFormat.format(SNAPSHOT_SAVE_FAILED_MSG, new Object[] { e.getMessage() }));

            return false; // failure => we wont continue with firing the event
        } catch (OutOfMemoryError e) {
            try {
                if (dos != null) {
                    dos.close();
                }

                if (lock != null) {
                    profFile.delete(lock);
                }
            } catch (Exception e2) {
            }

            ProfilerDialogs.displayError(OUT_OF_MEMORY_SAVING);

            return false; // failure => we wont continue with firing the event
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }

        return true;
    }

    public boolean saveSnapshot(LoadedSnapshot ls) {
        FileObject profFile = null;

        Lookup.Provider p = ls.getProject();
        FileObject saveDir = null;

        try {
            saveDir = ProjectStorage.getSettingsFolder(p, true);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MessageFormat.format(CANT_FIND_SNAPSHOT_LOCATION_MSG, new Object[] { e.getMessage() }), e);

            return false;
        }

        try {
            profFile = saveDir.createData(getDefaultSnapshotFileName(ls), SNAPSHOT_EXTENSION);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MessageFormat.format(SNAPSHOT_CREATE_IN_PROJECT_FAILED_MSG, new Object[] { e.getMessage() }), e);

            return false;
        }

        return saveSnapshot(ls, profFile);
    }

    public LoadedSnapshot takeSnapshot() {
        CommonUtils.runInEventDispatchThreadAndWait(new Runnable() {
                public void run() {
                    mainWindow = WindowManager.getDefault().getMainWindow();
                }
            });

        final Cursor cursor = mainWindow.getCursor();
        mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            LoadedSnapshot snapshot = prepareSnapshot();

            if (snapshot != null) {
                loadedSnapshots.add(snapshot);
                fireSnapshotTaken(snapshot);

                return snapshot;
            }
        } finally {
            mainWindow.setCursor(cursor);
        }

        return null;
    }

    protected void fireResultsAvailable() {
        if (resultsListeners.allClasses().isEmpty()) {
            return;
        }

        CommonUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                for(ResultsListener rl : resultsListeners.allInstances()) {
                    rl.resultsAvailable();
                }
            }
        });
    }

    protected void fireResultsReset() {
        if (resultsListeners.allClasses().isEmpty()) {
            return;
        }

        CommonUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                for(ResultsListener rl : resultsListeners.allInstances()) {
                    rl.resultsReset();
                }
            }
        });
    }

    protected void fireSnapshotLoaded(final LoadedSnapshot snapshot) {
        if (snapshotListeners.allClasses().isEmpty()) {
            return;
        }

        CommonUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                for(SnapshotsListener sl : snapshotListeners.allInstances()) {
                    sl.snapshotLoaded(snapshot);
                }
            }
        });
    }

    protected void fireSnapshotRemoved(final LoadedSnapshot snapshot) {
        if (snapshotListeners.allClasses().isEmpty()) {
            return;
        }

        CommonUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                for(SnapshotsListener sl : snapshotListeners.allInstances()) {
                    sl.snapshotRemoved(snapshot);
                }
            }
        });
    }

    protected void fireSnapshotSaved(final LoadedSnapshot snapshot) {
        if (snapshotListeners.allClasses().isEmpty()) {
            return;
        }

        CommonUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                for(SnapshotsListener sl : snapshotListeners.allInstances()) {
                    sl.snapshotSaved(snapshot);
                }
            }
        });
    }

    protected void fireSnapshotTaken(final LoadedSnapshot snapshot) {
        if (snapshotListeners.allClasses().isEmpty()) {
            return;
        }

        CommonUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                for(SnapshotsListener sl : snapshotListeners.allInstances()) {
                    sl.snapshotTaken(snapshot);
                }
            }
        });
    }

    void resultsBecameAvailable() {
        if (NetBeansProfiler.getDefaultNB().getProfilingState() == Profiler.PROFILING_INACTIVE) return; // Calibration, ignore
        resultsAvailable = true;
        fireResultsAvailable();
    }

    void resultsReset() {
        // NOTE: originally left as true for memory profiling due to Issue 60432, now seems to work correctly
        resultsAvailable = false;
        fireResultsReset();
    }

    // heapdump == true means selecting heapdump (*.hprof)
    SelectedFile selectSnapshotTargetFile(final String defaultName, final boolean heapdump) {
        String targetName;
        FileObject targetDir;

        // 1. let the user choose file or directory
        JFileChooser chooser = new JFileChooser();

        if (exportDir != null) {
            chooser.setCurrentDirectory(exportDir);
        }

        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle(SELECT_FILE_OR_DIR_DIALOG_CAPTION);
        chooser.setApproveButtonText(SAVE_BUTTON_NAME);
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith("." + (heapdump ? HEAPDUMP_EXTENSION : SNAPSHOT_EXTENSION)); //NOI18N
                }

                public String getDescription() {
                    return MessageFormat.format(PROFILER_SNAPSHOT_FILE_FILTER,
                                                new Object[] { heapdump ? HEAPDUMP_EXTENSION : SNAPSHOT_EXTENSION });
                }
            });

        if (chooser.showSaveDialog(WindowManager.getDefault().getMainWindow()) != JFileChooser.APPROVE_OPTION) {
            return null; // cancelled by the user
        }

        // 2. process both cases and extract file name and extension to use
        File file = chooser.getSelectedFile();
        String targetExt = heapdump ? HEAPDUMP_EXTENSION : SNAPSHOT_EXTENSION;

        if (file.isDirectory()) { // save to selected directory under default name
            exportDir = chooser.getCurrentDirectory();
            targetDir = FileUtil.toFileObject(FileUtil.normalizeFile(file));
            targetName = defaultName;
        } else { // save to selected file
            exportDir = chooser.getCurrentDirectory();

            targetDir = FileUtil.toFileObject(FileUtil.normalizeFile(exportDir));

            String fName = file.getName();

            // divide the file name into name and extension
            int idx = fName.lastIndexOf("."); // NOI18N

            if (idx == -1) { // no extension
                targetName = fName;

                // extension will be used from source file
            } else { // extension exists
                targetName = fName.substring(0, idx);
                targetExt = fName.substring(idx + 1);
            }
        }

        // 3. return a newly created FileObject
        return new SelectedFile(targetDir, targetName, targetExt);
    }

    private boolean checkFileExists(SelectedFile sf) {
        // check if the file already exists and if so prompt the user
        FileObject existingFile = sf.folder.getFileObject(sf.fileName, sf.fileExt);

        if (existingFile != null) {
            if (!ProfilerDialogs.displayConfirmation(MessageFormat.format(OVERWRITE_FILE_DIALOG_MSG,
                                                                                                  new Object[] {
                                                                                                      sf.fileName + "." //NOI18N
                                                                                                      + sf.fileExt
                                                                                                  }), OVERWRITE_FILE_DIALOG_CAPTION)) {
                return false; // cancelled by the user
            }

            try {
                existingFile.delete();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, MessageFormat.format(FILE_DELETE_FAILED_MSG, new Object[] { e.getMessage() }), e);

                return false;
            }
        }

        return true;
    }

    private void exportSnapshot(FileObject selectedSnapshot, FileObject targetFolder, String fileName, String fileExt) {
        if (checkFileExists(new SelectedFile(targetFolder, fileName, fileExt))) {
            try {
                FileUtil.copyFile(selectedSnapshot, targetFolder, fileName, fileExt);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, MessageFormat.format(SNAPSHOT_EXPORT_FAILED_MSG, new Object[] { e.getMessage() }), e);
            }
        }
    }

    private LoadedSnapshot findAlreadyLoadedSnapshot(FileObject selectedFile) {
        Iterator it = loadedSnapshots.iterator();
        File f = FileUtil.toFile(selectedFile);

        if (f == null) {
            return null;
        }

        while (it.hasNext()) {
            LoadedSnapshot ls = (LoadedSnapshot) it.next();

            if ((ls.getFile() != null) && (ls.getFile().equals(f))) {
                return ls;
            }
        }

        return null;
    }

    private Lookup.Provider findProjectForSnapshot(FileObject selectedFile) {
        return ProjectStorage.getProjectFromSettingsFolder(selectedFile.getParent());
    }

    private LoadedSnapshot loadSnapshotFromFileObject(FileObject selectedFile)
                                               throws IOException {
        DataInputStream dis = null;

        try {
            InputStream is = selectedFile.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            dis = new DataInputStream(bis);

            LoadedSnapshot ls = LoadedSnapshot.loadSnapshot(dis);

            if (ls != null) {
                ls.setFile(FileUtil.toFile(selectedFile));
                ls.setProject(findProjectForSnapshot(selectedFile));
            }

            return ls;
        } finally {
            if (dis != null) {
                dis.close();
            }
        }
    }

    private LoadedSnapshot loadSnapshotImpl(FileObject selectedFile)
                                     throws IOException {
        LoadedSnapshot ls = findAlreadyLoadedSnapshot(selectedFile);

        if (ls != null) {
            return ls;
        }

        ls = loadSnapshotFromFileObject(selectedFile);

        if (ls != null) {
            loadedSnapshots.add(ls);
            fireSnapshotLoaded(ls);
        }

        return ls;
    }

    private ProfilingSettings readSettingsFromFile(FileObject fo) {
        LoadedSnapshot ls = findAlreadyLoadedSnapshot(fo);

        if (ls != null) {
            return ls.getSettings();
        }

        DataInputStream dis = null;

        try {
            InputStream is = fo.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            dis = new DataInputStream(bis);

            // data before settings
            byte[] magicArray = new byte[LoadedSnapshot.PROFILER_FILE_MAGIC_STRING.length()];
            int len = dis.read(magicArray);

            if ((len != LoadedSnapshot.PROFILER_FILE_MAGIC_STRING.length())
                    || !LoadedSnapshot.PROFILER_FILE_MAGIC_STRING.equals(new String(magicArray))) {
                return null;
            }

            byte majorVersion = dis.readByte();
            byte minorVersion = dis.readByte();
            int type = dis.readInt();
            int compressedDataLen = dis.readInt();
            int uncompressedDataLen = dis.readInt();

            if (dis.skipBytes(compressedDataLen) != compressedDataLen) {
                return null;
            }

            // settings data
            int settingsLen = dis.readInt();
            byte[] settingsBytes = new byte[settingsLen];

            if (dis.read(settingsBytes) != settingsLen) {
                return null;
            }

            // create settings
            Properties props = new Properties();
            ProfilingSettings settings = new ProfilingSettings();
            ByteArrayInputStream bais2 = new ByteArrayInputStream(settingsBytes);
            BufferedInputStream bufBais2 = new BufferedInputStream(bais2);
            DataInputStream settingsDis = new DataInputStream(bufBais2);

            try {
                props.load(settingsDis);
            } catch (IOException e) {
                ProfilerLogger.log(e);

                return null;
            } finally {
                settingsDis.close();
            }

            settings.load(props);

            return settings;
        } catch (Exception e) {
            ProfilerLogger.log(e);

            return null;
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException e) {
                ProfilerLogger.log(e);
            }
        }
    }

    private int readTypeFromFile(FileObject fo) {
        LoadedSnapshot ls = findAlreadyLoadedSnapshot(fo);

        if (ls != null) {
            return ls.getType();
        }

        DataInputStream dis = null;

        try {
            InputStream is = fo.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            dis = new DataInputStream(bis);

            byte[] magicArray = new byte[LoadedSnapshot.PROFILER_FILE_MAGIC_STRING.length()];
            int len = dis.read(magicArray);

            if ((len != LoadedSnapshot.PROFILER_FILE_MAGIC_STRING.length())
                    || !LoadedSnapshot.PROFILER_FILE_MAGIC_STRING.equals(new String(magicArray))) {
                return LoadedSnapshot.SNAPSHOT_TYPE_UNKNOWN;
            }

            byte majorVersion = dis.readByte();
            byte minorVersion = dis.readByte();
            int type = dis.readInt();

            return type;
        } catch (Exception e) {
            return LoadedSnapshot.SNAPSHOT_TYPE_UNKNOWN;
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
