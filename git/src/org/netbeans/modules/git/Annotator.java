/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git;

import java.beans.PropertyChangeEvent;
import org.netbeans.modules.git.options.AnnotationColorProvider;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Pattern;
import javax.swing.Action;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitRepositoryState;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.ui.actions.AddAction;
import org.netbeans.modules.git.ui.actions.ConnectAction;
import org.netbeans.modules.git.ui.actions.DisconnectAction;
import org.netbeans.modules.git.ui.branch.CreateBranchAction;
import org.netbeans.modules.git.ui.checkout.CheckoutPathsAction;
import org.netbeans.modules.git.ui.checkout.CheckoutRevisionAction;
import org.netbeans.modules.git.ui.checkout.RevertChangesAction;
import org.netbeans.modules.git.ui.commit.CommitAction;
import org.netbeans.modules.git.ui.conflicts.ResolveConflictsAction;
import org.netbeans.modules.git.ui.diff.DiffAction;
import org.netbeans.modules.git.ui.init.InitAction;
import org.netbeans.modules.git.ui.output.OpenOutputAction;
import org.netbeans.modules.git.ui.repository.RepositoryBrowserAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.reset.ResetAction;
import org.netbeans.modules.git.ui.status.StatusAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * TODO: handle annotations
 * @author ondra
 */
public class Annotator extends VCSAnnotator implements PropertyChangeListener {
    private static final EnumSet<FileInformation.Status> STATUS_IS_IMPORTANT = EnumSet.noneOf(Status.class);
    private static final EnumSet<FileInformation.Status> STATUS_BADGEABLE = EnumSet.of(Status.UPTODATE, Status.NEW_INDEX_WORKING_TREE,
            Status.MODIFIED_HEAD_WORKING_TREE);
    static {
        STATUS_IS_IMPORTANT.addAll(FileInformation.STATUS_LOCAL_CHANGES);
        STATUS_IS_IMPORTANT.addAll(EnumSet.of(FileInformation.Status.UPTODATE, FileInformation.Status.NOTVERSIONED_EXCLUDED));
    }
    private static final Pattern lessThan = Pattern.compile("<");  // NOI18N
    private static final String badgeModified = "org/netbeans/modules/git/resources/icons/modified-badge.png";
    private static final String badgeConflicts = "org/netbeans/modules/git/resources/icons/conflicts-badge.png";
    private static final String toolTipModified = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeModified) + "\">&nbsp;"
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Modified");
    private static final String toolTipConflict = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeConflicts) + "\">&nbsp;"
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Conflicts");

    private final FileStatusCache cache;
    private MessageFormat format;
    private String emptyFormat;

    public Annotator() {
        cache = Git.getInstance().getFileStatusCache();
    }

    @Override
    public Action[] getActions (VCSContext context, ActionDestination destination) {
        Set<File> roots = GitUtils.getRepositoryRoots(context);
        boolean noneVersioned = (roots == null || roots.isEmpty());

        List<Action> actions = new LinkedList<Action>();
        ConnectAction ca = SystemAction.get(ConnectAction.class);
        if (destination.equals(ActionDestination.MainMenu)) {
            if (noneVersioned) {
                if (ca.isEnabled()) {
                    actions.add(ca);
                } else {
                    actions.add(SystemAction.get(InitAction.class));
                }
                actions.add(SystemAction.get(RepositoryBrowserAction.class));
            } else {            
                actions.add(SystemAction.get(StatusAction.class));
                actions.add(SystemAction.get(CheckoutPathsAction.class));
                actions.add(SystemAction.get(RevertChangesAction.class));
                actions.add(SystemAction.get(AddAction.class));
                actions.add(SystemAction.get(CommitAction.class));
                actions.add(SystemAction.get(DiffAction.class));
                actions.add(null);
                if (ca.isEnabled()) {
                    actions.add(SystemAction.get(ConnectAction.class));
                } else {
                    actions.add(SystemAction.get(DisconnectAction.class));
                }
                actions.add(SystemAction.get(RepositoryBrowserAction.class));
                actions.add(null);
                actions.add(SystemAction.get(CreateBranchAction.class));
                actions.add(SystemAction.get(CheckoutRevisionAction.class));
                actions.add(null);
                actions.add(SystemAction.get(OpenOutputAction.class));
                ResolveConflictsAction a = SystemAction.get(ResolveConflictsAction.class);
                if (a.isEnabled()) {
                    actions.add(null);
                    actions.add(a);
                }
                actions.add(SystemAction.get(ResetAction.class));
            }
        } else {
            Lookup lkp = context.getElements();
            if (noneVersioned) {                    
                if (ca.isEnabled()) {
                    actions.add(SystemActionBridge.createAction(ca, NbBundle.getMessage(ca.getClass(), "LBL_ConnectAction_PopupName"), lkp)); //NOI18N
                } else {
                    actions.add(SystemActionBridge.createAction(SystemAction.get(InitAction.class), NbBundle.getMessage(InitAction.class, "LBL_InitAction.popupName"), lkp));
                }
            } else {
                actions.add(SystemActionBridge.createAction(SystemAction.get(StatusAction.class), NbBundle.getMessage(StatusAction.class, "LBL_StatusAction.popupName"), lkp));
                actions.add(SystemActionBridge.createAction(SystemAction.get(AddAction.class), NbBundle.getMessage(AddAction.class, "LBL_AddAction.popupName"), lkp));
                actions.add(SystemActionBridge.createAction(SystemAction.get(CommitAction.class), NbBundle.getMessage(CommitAction.class, "LBL_CommitAction.popupName"), lkp));
                actions.add(SystemActionBridge.createAction(SystemAction.get(DiffAction.class), NbBundle.getMessage(DiffAction.class, "LBL_DiffAction_PopupName"), lkp));
                actions.add(SystemActionBridge.createAction(SystemAction.get(CheckoutPathsAction.class), NbBundle.getMessage(CheckoutPathsAction.class, "LBL_CheckoutPathsAction_PopupName"), lkp));
                actions.add(SystemActionBridge.createAction(SystemAction.get(RevertChangesAction.class), NbBundle.getMessage(RevertChangesAction.class, "LBL_RevertChangesAction_PopupName"), lkp));
                SystemActionBridge a = SystemActionBridge.createAction(SystemAction.get(ResolveConflictsAction.class), NbBundle.getMessage(ResolveConflictsAction.class, "LBL_ResolveConflictsAction_PopupName"), lkp);
                if (a.isEnabled()) {
                    actions.add(null);
                    actions.add(a);
                }
                actions.add(null);
                actions.add(SystemActionBridge.createAction(SystemAction.get(CreateBranchAction.class), NbBundle.getMessage(CreateBranchAction.class, "LBL_CreateBranchAction_PopupName"), lkp)); //NOI18N
                actions.add(SystemActionBridge.createAction(SystemAction.get(CheckoutRevisionAction.class), NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction_PopupName"), lkp)); //NOI18N
                DisconnectAction da = SystemAction.get(DisconnectAction.class);
                if (da.isEnabled()) {
                    actions.add(null);
                    actions.add(SystemActionBridge.createAction(da, NbBundle.getMessage(da.getClass(), "LBL_DisconnectAction_PopupName"), lkp)); //NOI18N
                }
                if (ca.isEnabled()) {
                    actions.add(null);
                    actions.add(SystemActionBridge.createAction(ca, NbBundle.getMessage(ca.getClass(), "LBL_ConnectAction_PopupName"), lkp)); //NOI18N
                }
            }
        }

        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public Image annotateIcon(Image icon, VCSContext context) {
        boolean folderAnnotation = false;
        for (File file : context.getRootFiles()) {
            if (file.isDirectory()) {
                folderAnnotation = true;
                Utils.addFolderToLog(file);
                break;
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !Utils.isFromMultiFileDataObject(context);
        }

        if (folderAnnotation == false) {
            return annotateFileIcon(context, icon);
        } else {
            return annotateFolderIcon(context, icon);
        }
    }

    @Override
    public String annotateName(String name, VCSContext context) {
        FileInformation mostImportantInfo = null;
        File mostImportantFile = null;
        boolean folderAnnotation = false;
        for (final File file : context.getRootFiles()) {
            FileInformation info = cache.getStatus(file);
            if (!info.containsStatus(STATUS_IS_IMPORTANT)) continue;
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
                mostImportantFile = file;
                folderAnnotation = info.isDirectory();
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !Utils.isFromMultiFileDataObject(context);
        }

        if (mostImportantInfo == null) return null;
        return folderAnnotation ?
            annotateFolderNameHtml(name, context, mostImportantInfo, mostImportantFile) :
            annotateNameHtml(name, mostImportantInfo, mostImportantFile);
    }

    private void refresh () {
        // TODO: implement, [status repository branch tags? etc.]
    }

    private static boolean isMoreImportant (FileInformation a, FileInformation b) {
        if (b == null) return true;
        if (a == null) return false;
        return a.getComparableStatus() < b.getComparableStatus();
    }


    private Image annotateFileIcon (VCSContext context, Image icon) throws IllegalArgumentException {
        FileInformation mostImportantInfo = null;
        File mostImportantFile = null;
        for (final File file : context.getRootFiles()) {
            FileInformation info = cache.getStatus(file);
            if (!info.containsStatus(STATUS_IS_IMPORTANT)) {
                continue;
            }
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
                mostImportantFile = file;
            }
        }
        if(mostImportantInfo == null) return null;
        String tooltip = null;
        String statusText = mostImportantInfo.getStatusText(FileInformation.Mode.HEAD_VS_INDEX.HEAD_VS_WORKING_TREE);
        if (mostImportantInfo.containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            // File is IGNORED
            tooltip = getAnnotationProvider().EXCLUDED_FILE_TOOLTIP.getFormat().format(new Object[] { statusText });
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.REMOVED_INDEX_WORKING_TREE))) {
            // ADDED to index but REMOVED in WT
            tooltip = getAnnotationProvider().UP_TO_DATE_FILE_TOOLTIP.getFormat().format(new Object[]{statusText});
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_INDEX_WORKING_TREE))) {
            // MODIFIED in index, MODIFIED in WT, but in WT same as HEAD
            tooltip = getAnnotationProvider().UP_TO_DATE_FILE_TOOLTIP.getFormat().format(new Object[]{statusText});
        } else if (mostImportantInfo.containsStatus(Status.REMOVED_HEAD_WORKING_TREE)) {
            // DELETED in WT
            tooltip = getAnnotationProvider().REMOVED_FILE_TOOLTIP.getFormat().format(new Object[] { statusText });
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.REMOVED_HEAD_INDEX))) {
            // recreated in WT
            tooltip = getAnnotationProvider().UP_TO_DATE_FILE_TOOLTIP.getFormat().format(new Object[]{statusText});
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.REMOVED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE))) {
            // recreated in WT and modified
            tooltip = getAnnotationProvider().MODIFIED_FILE_TOOLTIP.getFormat().format(new Object[] { statusText });
        } else if (mostImportantInfo.containsStatus(Status.NEW_INDEX_WORKING_TREE)) {
            // NEW in WT and unversioned
            tooltip = getAnnotationProvider().NEW_FILE_TOOLTIP.getFormat().format(new Object[] { statusText });
        } else if (mostImportantInfo.containsStatus(Status.NEW_HEAD_INDEX)) {
            // ADDED to index
            tooltip = getAnnotationProvider().ADDED_FILE_TOOLTIP.getFormat().format(new Object[] { statusText });
        } else if (mostImportantInfo.containsStatus(Status.MODIFIED_HEAD_WORKING_TREE)) {
            tooltip = getAnnotationProvider().MODIFIED_FILE_TOOLTIP.getFormat().format(new Object[] { statusText });
        } else if (mostImportantInfo.containsStatus(Status.UPTODATE)) {
            tooltip = null;
        } else if (mostImportantInfo.containsStatus(Status.IN_CONFLICT)) {
            tooltip = getAnnotationProvider().CONFLICT_FILE_TOOLTIP.getFormat().format(new Object[] { statusText });
        } else if (mostImportantInfo.containsStatus(Status.NOTVERSIONED_NOTMANAGED)) {
            tooltip = null;
        } else if (mostImportantInfo.containsStatus(Status.UNKNOWN)) {
            tooltip = null;
        } else {
            throw new IllegalStateException("Unknown status: " + mostImportantInfo.getStatus()); //NOI18N
        }
        return tooltip != null ? ImageUtilities.addToolTipToImage(icon, tooltip) : null;
    }

    private Image annotateFolderIcon(VCSContext context, Image icon) {
        boolean isVersioned = false;
        for (Iterator i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = (File) i.next();
            // There is an assumption here that annotateName was already
            // called and FileStatusCache.getStatus was scheduled if
            // FileStatusCache.getCachedStatus returned null.
            FileInformation info = cache.getStatus(file);
            if (info.containsStatus(STATUS_BADGEABLE)) {
                isVersioned = true;
                break;
            }
        }
        if (!isVersioned) {
            return null;
        }
        Image badge = null;
        if (cache.containsFiles(context, EnumSet.of(Status.IN_CONFLICT), true)) {
            badge = ImageUtilities.assignToolTipToImage(ImageUtilities.loadImage(badgeConflicts, true), toolTipConflict);
        } else if (cache.containsFiles(context, FileInformation.STATUS_LOCAL_CHANGES, true)) {
            badge = ImageUtilities.assignToolTipToImage(ImageUtilities.loadImage(badgeModified, true), toolTipModified);
        }
        if (badge != null) {
            return ImageUtilities.mergeImages(icon, badge, 16, 9);
        } else {
            return icon;
        }
    }

    private final Map<RepositoryInfo, Set<File>> filesWithRepositoryAnnotations = new WeakHashMap<RepositoryInfo, Set<File>>(3);
    
    private String annotateFolderNameHtml (String name, VCSContext context, FileInformation mostImportantInfo, File mostImportantFile) {
        String nameHtml = htmlEncode(name);
        if (mostImportantInfo.containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            return getAnnotationProvider().EXCLUDED_FILE.getFormat().format(new Object [] { nameHtml, ""}); // NOI18N
        }
        
        String folderAnnotation = ""; //NOI18N
        Set<File> roots = context.getRootFiles();
        File repository = Git.getInstance().getRepositoryRoot(mostImportantFile);
        if (roots.size() > 1 || mostImportantFile.equals(repository)) {
            // project node or repository root
            String branchLabel = ""; //NOI18N
            RepositoryInfo info = RepositoryInfo.getInstance(repository);
            addFileWithRepositoryAnnotation(info, mostImportantFile);
            GitBranch branch = info.getActiveBranch();
            if (branch != null) {
                branchLabel = branch.getName();
                if (branchLabel == GitBranch.NO_BRANCH) { // do not use equals
                    // not on a branch, show also commit id
                    branchLabel += " " + branch.getId(); // NOI18N
                }
            }
            GitRepositoryState repositoryState = info.getRepositoryState();
            if (repositoryState != GitRepositoryState.SAFE) {
                folderAnnotation = repositoryState.toString() + " - " + branchLabel; //NOI18N
            } else {
                folderAnnotation = branchLabel;
            }
        }

        MessageFormat uptodateFormat = getAnnotationProvider().UP_TO_DATE_FILE.getFormat();
        return uptodateFormat.format(new Object [] { nameHtml, !folderAnnotation.isEmpty() ? new StringBuilder(" [").append(folderAnnotation).append("]").toString() : "" }); // NOI18N
    }

    private void addFileWithRepositoryAnnotation (RepositoryInfo info, File file) {
        info.removePropertyChangeListener(this);
        synchronized (filesWithRepositoryAnnotations) {
            Set<File> files = filesWithRepositoryAnnotations.get(info);
            if (files == null) {
                filesWithRepositoryAnnotations.put(info, files = new HashSet<File>());
            }
            files.add(file);
        }
        info.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange (final PropertyChangeEvent evt) {
        if (evt.getPropertyName() == RepositoryInfo.PROPERTY_ACTIVE_BRANCH || evt.getPropertyName() == RepositoryInfo.PROPERTY_STATE) {
            Utils.post(new Runnable() {
                @Override
                public void run() {
                    RepositoryInfo info = (RepositoryInfo) evt.getSource();
                    Set<File> filesToRefresh;
                    synchronized (filesWithRepositoryAnnotations) {
                        filesToRefresh = filesWithRepositoryAnnotations.remove(info);
                    }
                    if (filesToRefresh != null && !filesToRefresh.isEmpty()) {
                        Git.getInstance().headChanged(filesToRefresh);
                    }
                }
            }, 400);
        }
    }

    public String annotateNameHtml(String name, FileInformation mostImportantInfo, File mostImportantFile) {
        name = htmlEncode(name);

        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);

        if (annotationsVisible && mostImportantFile != null && mostImportantInfo.containsStatus(STATUS_IS_IMPORTANT)) {
            if (format != null) {
                textAnnotation = formatAnnotation(mostImportantInfo, mostImportantFile);
            } else {
                String statusText = mostImportantInfo.getShortStatusText();
                if(!statusText.isEmpty()) {
                    textAnnotation = " [" + mostImportantInfo.getShortStatusText() + "]"; // NOI18N
                } else {
                    textAnnotation = ""; // NOI18N
                }
            }
        } else {
            textAnnotation = ""; // NOI18N
        }

        if (mostImportantInfo.containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            // IGNORED
            return getAnnotationProvider().EXCLUDED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.REMOVED_INDEX_WORKING_TREE))) {
            // ADDED to index but REMOVED in WT
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_INDEX_WORKING_TREE))) {
            // MODIFIED in index, MODIFIED in WT, but in WT same as HEAD
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.REMOVED_HEAD_WORKING_TREE)) {
            // DELETED in WT
            return getAnnotationProvider().REMOVED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.REMOVED_HEAD_INDEX))) {
            // recreated in WT
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.REMOVED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE))) {
            // recreated in WT and modified
            return getAnnotationProvider().MODIFIED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.NEW_INDEX_WORKING_TREE)) {
            // NEW in WT and unversioned
            return getAnnotationProvider().NEW_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.NEW_HEAD_INDEX)) {
            // ADDED to index
            return getAnnotationProvider().ADDED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.MODIFIED_HEAD_WORKING_TREE)) {
            return getAnnotationProvider().MODIFIED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.UPTODATE)) {
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.IN_CONFLICT)) {
            return getAnnotationProvider().CONFLICT_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.NOTVERSIONED_NOTMANAGED)) {
            return name;
        } else if (mostImportantInfo.containsStatus(Status.UNKNOWN)) {
            return name;
        } else {
            throw new IllegalStateException("Unknown status: " + mostImportantInfo.getStatus()); //NOI18N
        }
    }

    private static String htmlEncode(String name) {
        if (name.indexOf('<') == -1) return name;
        return lessThan.matcher(name).replaceAll("&lt;"); // NOI18N
    }

    private AnnotationColorProvider getAnnotationProvider() {
        return AnnotationColorProvider.getInstance();
    }

    private String formatAnnotation(FileInformation info, File file) {
        String statusString = "";  // NOI18N
        if (info.containsStatus(Status.UPTODATE)) {
            statusString = info.getShortStatusText();
        }

        //String stickyString = SvnUtils.getCopy(file);
        String stickyString = null;
        if (stickyString == null) {
            stickyString = ""; // NOI18N
        }

        Object[] arguments = new Object[] {
            statusString,
            stickyString,
        };

        String annotation = format.format(arguments, new StringBuffer(), null).toString().trim();
        if(annotation.equals(emptyFormat)) {
            return ""; // NOI18N
        } else {
            return " " + annotation; // NOI18N
        }
    }
}
