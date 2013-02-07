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

package org.netbeans.modules.git.ui.commit;

import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitUser;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Mode;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.diff.MultiDiffPanelController;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.hooks.GitHook;
import org.netbeans.modules.versioning.hooks.GitHookContext;
import org.netbeans.modules.versioning.hooks.VCSHookContext;
import org.netbeans.modules.versioning.hooks.VCSHooks;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.common.VCSCommitDiffProvider;
import org.netbeans.modules.versioning.util.common.VCSCommitFilter;
import org.netbeans.modules.versioning.util.common.VCSCommitPanel;
import org.netbeans.modules.versioning.util.common.VCSCommitParameters.DefaultCommitParameters;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class GitCommitPanel extends VCSCommitPanel<GitFileNode> {

    static final GitCommitFilter FILTER_HEAD_VS_WORKING = new GitCommitFilter(
                "HEAD_VS_WORKING", 
                new ImageIcon(GitCommitPanel.class.getResource("/org/netbeans/modules/git/resources/icons/head_vs_working.png")),
                NbBundle.getMessage(GitCommitPanel.class, "ParametersPanel.tgbHeadVsWorking.toolTipText"),
                true); 
    static final GitCommitFilter FILTER_HEAD_VS_INDEX = new GitCommitFilter(
                "HEAD_VS_INDEX", 
                new ImageIcon(GitCommitPanel.class.getResource("/org/netbeans/modules/git/resources/icons/head_vs_index.png")),
                NbBundle.getMessage(GitCommitPanel.class, "ParametersPanel.tgbHeadVsIndex.toolTipText"),
                false);
    
    private final Collection<GitHook> hooks;
    private final File[] roots;
    private final File repository;
    private final boolean fromGitView;
    private final DiffProvider diffProvider;

    private GitCommitPanel(GitCommitTable table, final File[] roots, final File repository, DefaultCommitParameters parameters, Preferences preferences, Collection<GitHook> hooks, 
            VCSHookContext hooksContext, DiffProvider diffProvider, boolean fromGitView, List<VCSCommitFilter> filters) {
        super(table, parameters, preferences, hooks, hooksContext, filters, diffProvider);
        this.diffProvider = diffProvider;
        this.roots = roots;
        this.repository = repository;
        this.hooks = hooks;
        this.fromGitView = fromGitView;
    }

    public static GitCommitPanel create(final File[] roots, final File repository, GitUser user, boolean fromGitView) {
        Preferences preferences = GitModuleConfig.getDefault().getPreferences();
        String lastCanceledCommitMessage = GitModuleConfig.getDefault().getLastCanceledCommitMessage();

        GitCommitParameters parameters = new GitCommitParameters(preferences, lastCanceledCommitMessage, user);
        
        Collection<GitHook> hooks = VCSHooks.getInstance().getHooks(GitHook.class);
        GitHookContext hooksCtx = new GitHookContext(roots, null, new GitHookContext.LogEntry[] {});        
        
        DiffProvider diffProvider = new DiffProvider();
        final GitCommitTable gitCommitTable = new GitCommitTable();
        final CommitPanel panel = parameters.getPanel();
        panel.amendCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                gitCommitTable.setAmend(panel.amendCheckBox.isSelected());
            }
        });
        return new GitCommitPanel(gitCommitTable, roots, repository, parameters, preferences, hooks, hooksCtx, diffProvider, fromGitView, createFilters(fromGitView));
    }

    private static void disableFilters () {
        for (GitCommitFilter f : Arrays.asList(FILTER_HEAD_VS_INDEX, FILTER_HEAD_VS_WORKING)) {
            f.setSelected(false);
        }
    }
    
    private static List<VCSCommitFilter> createFilters (boolean gitViewStoredMode) {
        // synchronize access to this static field
        assert EventQueue.isDispatchThread();
        disableFilters();
        Mode mode = gitViewStoredMode ? GitModuleConfig.getDefault().getLastUsedModificationContext() : GitModuleConfig.getDefault().getLastUsedCommitViewMode();
        (Mode.HEAD_VS_INDEX.equals(mode) ? GitCommitPanel.FILTER_HEAD_VS_INDEX : GitCommitPanel.FILTER_HEAD_VS_WORKING).setSelected(true);
        return Arrays.<VCSCommitFilter>asList(FILTER_HEAD_VS_INDEX, FILTER_HEAD_VS_WORKING);
    }
    
    @Override
    public GitCommitParameters getParameters() {
        return (GitCommitParameters) super.getParameters();
    }

    public Collection<GitHook> getHooks() {
        return hooks;
    }

    @Override
    protected void computeNodes() {      
        computeNodesIntern();
    }

    @Override
    public boolean open (VCSContext context, HelpCtx helpCtx) {
        // synchronize access to this static field
        assert EventQueue.isDispatchThread();
        boolean ok = super.open(context, helpCtx);
        GitProgressSupport supp = support;
        if (supp != null) {
            supp.cancel();
        }
        if (ok && !fromGitView) {
            GitModuleConfig.getDefault().setLastUsedCommitViewMode(getSelectedFilter() == GitCommitPanel.FILTER_HEAD_VS_INDEX ? Mode.HEAD_VS_INDEX : Mode.HEAD_VS_WORKING_TREE);
        }
        for (Map.Entry<File, MultiDiffPanelController> e : diffProvider.controllers.entrySet()) {
            e.getValue().componentClosed();
        }
        return ok;
    }
    
    /** used by unit tests */
    GitProgressSupport support;
    RequestProcessor.Task computeNodesIntern() {      
        final boolean refreshFinnished[] = new boolean[] { false };
        RequestProcessor rp = Git.getInstance().getRequestProcessor(repository);

        GitProgressSupport supp = this.support;
        if (supp != null) {
            supp.cancel();
        }
        support = getProgressSupport(refreshFinnished);
        final String preparingMessage = NbBundle.getMessage(CommitAction.class, "Progress_Preparing_Commit"); //NOI18N
        setupProgress(preparingMessage, support.getProgressComponent());
        Task task = support.start(rp, repository, preparingMessage);
        
        // do not show progress in dialog if task finnished early        
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!(refreshFinnished[0])) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            showProgress();                            
                        }
                    });                     
                }
            }
        }, 1000);
        return task;
    }

    // merge-type commit dialog can hook into this method
    protected GitProgressSupport getProgressSupport (final boolean[] refreshFinished) {
        return new GitCommitDialogProgressSupport(refreshFinished);
    }

    private class GitCommitDialogProgressSupport extends GitProgressSupport {

        private final boolean[] refreshFinished;

        public GitCommitDialogProgressSupport(boolean[] refreshFinished) {
            this.refreshFinished = refreshFinished;
        }

        @Override
        public void perform() {
            try {
                loadFiles();
                loadHeadLogMessage();
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, true);
            } finally {
                refreshFinished[0] = true;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        stopProgress();
                    }
                });
            }
        }

        private void loadHeadLogMessage() throws IllegalArgumentException, GitException {
            GitRevisionInfo gitRevisionInfo = getClient().log(GitUtils.HEAD, getProgressMonitor());
            String headCommitMessage = gitRevisionInfo.getFullMessage();
            getParameters().getPanel().setHeadCommitMessage(headCommitMessage);
        }

        private boolean loadFiles() {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    getCommitTable().setNodes(new GitFileNode[0]);
                }
            });
            // Ensure that cache is uptodate
            FileStatusCache cache = Git.getInstance().getFileStatusCache();
            cache.refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repository, Arrays.asList(roots)), getProgressMonitor());
            // the realy time consuming part is over;
            // no need to show the progress component,
            // which only makes the dialog flicker
            refreshFinished[0] = true;
            File[][] split = Utils.splitFlatOthers(roots);
            List<File> fileList = new ArrayList<File>();
            for (int c = 0; c < split.length; c++) {
                File[] splitRoots = split[c];
                boolean recursive = c == 1;
                if (recursive) {
                    File[] files = cache.listFiles(splitRoots, getAcceptedStatus());
                    for (int i = 0; i < files.length; i++) {
                        for (int r = 0; r < splitRoots.length; r++) {
                            if (Utils.isAncestorOrEqual(splitRoots[r], files[i])) {
                                if (!fileList.contains(files[i])) {
                                    fileList.add(files[i]);
                                }
                            }
                        }
                    }
                } else {
                    File[] files = GitUtils.flatten(splitRoots, getAcceptedStatus());
                    for (int i = 0; i < files.length; i++) {
                        if (!fileList.contains(files[i])) {
                            fileList.add(files[i]);
                        }
                    }
                }
            }
            if (fileList.isEmpty()) {
                return true;
            }
            List<GitFileNode> nodesList = new ArrayList<GitFileNode>(fileList.size());
            Git git = Git.getInstance();
            for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
                File file = it.next();
                if (repository.equals(git.getRepositoryRoot(file))) {
                    GitFileNode node = new GitFileNode(repository, file);
                    nodesList.add(node);
                }
            }
            final GitFileNode[] nodes = nodesList.toArray(new GitFileNode[nodesList.size()]);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    getCommitTable().setNodes(nodes);
                }
            });
            return false;
        }
    }
    
    private EnumSet<Status> getAcceptedStatus() {
        VCSCommitFilter f = getSelectedFilter();
        if(f == FILTER_HEAD_VS_INDEX) {
            return FileInformation.STATUS_MODIFIED_HEAD_VS_INDEX;
        } else if(f == FILTER_HEAD_VS_WORKING) {
            return FileInformation.STATUS_MODIFIED_HEAD_VS_WORKING;                
        }         
        throw new IllegalStateException("wrong filter " + (f != null ? f.getID() : "NULL"));    // NOI18N        
    }

    private static class DiffProvider extends VCSCommitDiffProvider {

        private final Map<File, MultiDiffPanelController> controllers = new HashMap<File, MultiDiffPanelController>();

        @Override
        public Set<File> getModifiedFiles () {
            return getSaveCookiesPerFile().keySet();
        }

        private Map<File, SaveCookie> getSaveCookiesPerFile () {
            Map<File, SaveCookie> modifiedFiles = new HashMap<File, SaveCookie>();
            for (Map.Entry<File, MultiDiffPanelController> e : controllers.entrySet()) {
                SaveCookie[] cookies = e.getValue().getSaveCookies(false);
                if (cookies.length > 0) {
                    modifiedFiles.put(e.getKey(), cookies[0]);
                }
            }
            return modifiedFiles;
        }

        @Override
        public JComponent createDiffComponent (File file) {
            MultiDiffPanelController controller = new MultiDiffPanelController(file);
            controllers.put(file, controller);
            return controller.getPanel();
        }
        

        /**
         * Returns save cookies available for files in the commit table
         * @return
         */
        @Override
        protected SaveCookie[] getSaveCookies () {
            return getSaveCookiesPerFile().values().toArray(new SaveCookie[0]);
        }

        /**
         * Returns editor cookies available for modified and not open files in the commit table
         * @return
         */
        @Override
        protected EditorCookie[] getEditorCookies () {
            LinkedList<EditorCookie> allCookies = new LinkedList<EditorCookie>();
            for (Map.Entry<File, MultiDiffPanelController> e : controllers.entrySet()) {
                EditorCookie[] cookies = e.getValue().getEditorCookies(true);
                if (cookies.length > 0) {
                    allCookies.add(cookies[0]);
                }
            }
            return allCookies.toArray(new EditorCookie[allCookies.size()]);
        }        
    }    
    
    private static class GitCommitFilter extends VCSCommitFilter {
        private final Icon icon;
        private final String tooltip;
        private final String id;

        GitCommitFilter(String id, Icon icon, String tooltip, boolean selected) {
            super(selected);
            this.icon = icon;
            this.tooltip = tooltip;
            this.id = id;
        }
        
        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public String getTooltip() {
            return tooltip;
        }

        @Override
        public String getID() {
            return id;
        }

        @Override
        public void setSelected (boolean selected) {
            super.setSelected(selected);
        }
        
    }
// <editor-fold defaultstate="collapsed" desc="dialog for merged repository">
    static class GitCommitPanelMerged extends GitCommitPanel {

        private final File repository;

        static GitCommitPanel create(File[] roots, File repository, GitUser user, String mergeCommitMessage) {
            Preferences preferences = GitModuleConfig.getDefault().getPreferences();
            String lastCanceledCommitMessage = GitModuleConfig.getDefault().getLastCanceledCommitMessage();

            DefaultCommitParameters parameters = new GitCommitParameters(preferences, 
                    mergeCommitMessage == null ? lastCanceledCommitMessage : mergeCommitMessage,
                    mergeCommitMessage != null, user);

            Collection<GitHook> hooks = VCSHooks.getInstance().getHooks(GitHook.class);
            GitHookContext hooksCtx = new GitHookContext(roots, null, new GitHookContext.LogEntry[]{});

            DiffProvider diffProvider = new DiffProvider();

            return new GitCommitPanelMerged(new GitCommitTable(false), roots, repository, parameters, preferences, hooks, hooksCtx, diffProvider);
        }

        private GitCommitPanelMerged(GitCommitTable gitCommitTable, File[] roots, File repository, DefaultCommitParameters parameters,
                Preferences preferences, Collection<GitHook> hooks, GitHookContext hooksCtx, DiffProvider diffProvider) {
            super(gitCommitTable, roots, repository, parameters, preferences, hooks, hooksCtx, diffProvider, true, createFilters());
            this.repository = repository;
        }

        private static List<VCSCommitFilter> createFilters() {
            disableFilters();
            FILTER_HEAD_VS_INDEX.setSelected(true);
            return Collections.<VCSCommitFilter>singletonList(FILTER_HEAD_VS_INDEX);
        }

        @Override
        protected GitProgressSupport getProgressSupport(boolean[] refreshFinnished) {
            return new MergedCommitDialogProgressSupport(refreshFinnished);
        }

        private class MergedCommitDialogProgressSupport extends GitProgressSupport {

            private final boolean refreshFinished[];

            MergedCommitDialogProgressSupport(boolean[] refreshFinished) {
                this.refreshFinished = refreshFinished;
            }

            @Override
            public void perform() {
                try {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            getCommitTable().setNodes(new GitFileNode[0]);
                        }
                    });
                    // get list of modifications
                    File[] files;
                    try {
                        files = getClient().listModifiedIndexEntries(new File[] { repository }, getProgressMonitor());
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                        return;
                    }
                    FileStatusCache cache = Git.getInstance().getFileStatusCache();
                    if (isCanceled()) {
                        return;
                    }
                    cache.refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repository, Arrays.asList(files)), getProgressMonitor());
                    if (isCanceled()) {
                        return;
                    }

                    // the realy time consuming part is over;
                    // no need to show the progress component,
                    // which only makes the dialog flicker
                    refreshFinished[0] = true;
                    files = cache.listFiles(new File[]{repository}, FileInformation.STATUS_MODIFIED_HEAD_VS_INDEX);
                    if (files.length == 0) {
                        return;
                    }

                    ArrayList<GitFileNode> nodesList = new ArrayList<GitFileNode>(files.length);

                    for (File file : files) {
                        GitFileNode node = new GitFileNode(repository, file);
                        nodesList.add(node);
                    }
                    final GitFileNode[] nodes = nodesList.toArray(new GitFileNode[files.length]);
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            getCommitTable().setNodes(nodes);
                        }
                    });
                } finally {
                    refreshFinished[0] = true;
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            stopProgress();
                        }
                    });
                }
            }
        }

        @Override
        public void setErrorLabel (String htmlErrorLabel) {
            if (htmlErrorLabel == null || htmlErrorLabel.isEmpty()) {
                htmlErrorLabel = NbBundle.getMessage(GitCommitPanel.class, "MSG_CommitPanel.afterMerge"); //NOI18N
            }
            super.setErrorLabel(htmlErrorLabel);
        }

    }// </editor-fold>
}
