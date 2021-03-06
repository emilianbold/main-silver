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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.mercurial.remote.ui.rebase;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.WorkingCopyInfo;
import org.netbeans.modules.mercurial.remote.commands.RebaseCommand;
import org.netbeans.modules.mercurial.remote.commands.RebaseCommand.Result.State;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.remotefs.versioning.hooks.HgHook;
import org.netbeans.modules.remotefs.versioning.hooks.HgHookContext;
import org.netbeans.modules.remotefs.versioning.hooks.VCSHooks;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * 
 * @author Ondrej Vrabec
 */
@ActionID(id = "org.netbeans.modules.mercurial.remote.ui.rebase.RebaseAction", category = "MercurialRemote")
@ActionRegistration(displayName = "#CTL_MenuItem_RebaseAction")
@Messages({
    "MSG_Rebase_Progress=Rebasing...",
    "MSG_Rebase_Started=Starting rebase",
    "CTL_MenuItem_RebaseAction=&Rebase...",
    "MSG_Rebase_Title_Sep=----------------",
    "MSG_Rebase_Title=Mercurial Rebase",
    "# Capitalized letters used intentionally to emphasize the words in an output window, should be translated",
    "MSG_Rebase_Finished=INFO: End of Rebase"
})
public class RebaseAction extends ContextAction {
    
    private static final Logger LOG = Logger.getLogger(RebaseAction.class.getName());
    private static final String NB_REBASE_INFO_FILE = "netbeans-rebase.info"; //NOI18N
    
    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_RebaseAction"; // NOI18N
    }

    @Override
    @Messages({
        "MSG_Rebase.unfinishedMerge=Cannot rebase because of an unfinished merge.",
        "MSG_Rebase.noBranchHeads=No heads in the current branch \"{0}\".\n"
            + "Did you forget to commit to permanently create the branch?\n\n"
            + "Please switch to a fully operational branch before starting rebase.",
        "MSG_Rebase_Preparing_Progress=Preparing Rebase..."
    })
    protected void performContextAction(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final VCSFileProxy roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) {
            return;
        }
        final VCSFileProxy root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                HgLogMessage[] workingCopyParents = WorkingCopyInfo.getInstance(root).getWorkingCopyParents();
                if (HgUtils.isRebasing(root)) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            finishRebase(root);
                        }
                    });
                } else if (workingCopyParents.length > 1) {
                    // inside a merge
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                Bundle.MSG_Rebase_unfinishedMerge(),
                                NotifyDescriptor.ERROR_MESSAGE));
                } else {
                    try {
                        final HgLogMessage workingCopyParent = workingCopyParents[0];
                        final String currentBranch = HgCommand.getBranch(root);
                        HgLogMessage[] heads = HgCommand.getHeadRevisionsInfo(root, false, OutputLogger.getLogger(null));
                        final Collection<HgLogMessage> branchHeads = HgUtils.sortByBranch(heads).get(currentBranch);
                        if (isCanceled()) {
                            return;
                        }
                        if (branchHeads == null) {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                    Bundle.MSG_Rebase_noBranchHeads(currentBranch),
                                    NotifyDescriptor.ERROR_MESSAGE));
                            return;
                        }
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                doRebase(root, workingCopyParent, branchHeads);
                            }
                        });
                    } catch (HgException.HgCommandCanceledException ex) {
                        // canceled by user, do nothing
                    } catch (HgException ex) {
                        HgUtils.notifyException(ex);
                    }
                }
            }
        };
        support.start(rp, root, Bundle.MSG_Rebase_Preparing_Progress());
    }

    @Messages({
        "MSG_RebaseAction.progress.preparingChangesets=Preparing changesets to rebase",
        "MSG_RebaseAction.progress.rebasingChangesets=Rebasing changesets"
    })
    public static boolean doRebase (VCSFileProxy root, String base, String source, String dest,
            HgProgressSupport supp) throws HgException {
        OutputLogger logger = supp.getLogger();
        Collection<HgHook> hooks = VCSHooks.getInstance().getHooks(HgHook.class);
        String destRev = dest;
        String sourceRev = source;
        if (!hooks.isEmpty()) {
            try {
                if (destRev == null) {
                    HgLogMessage[] heads = HgCommand.getHeadRevisionsInfo(root, false, OutputLogger.getLogger(null));
                    final Collection<HgLogMessage> branchHeads = HgUtils.sortByBranch(heads).get(HgCommand.getBranch(root));
                    if (branchHeads != null && !branchHeads.isEmpty()) {
                        HgLogMessage tipmostHead = branchHeads.iterator().next();
                        for (HgLogMessage head : branchHeads) {
                            if (head.getRevisionAsLong() > tipmostHead.getRevisionAsLong()) {
                                tipmostHead = head;
                            }
                        }
                        destRev = tipmostHead.getCSetShortID();
                    }
                }
                if (supp.isCanceled()) {
                    return false;
                }
                if (sourceRev == null) {
                    String baseRev = base;
                    if (baseRev == null) {
                        baseRev = HgCommand.getParent(root, null, null).getChangesetId();
                    }
                    supp.setDisplayName(Bundle.MSG_RebaseAction_progress_preparingChangesets());
                    String revPattern = MessageFormat.format("last(limit(ancestor({0},{1})::{1}, 2), 1)", destRev, baseRev); //NOI18N
                    HgLogMessage[] revs = HgCommand.getRevisionInfo(root, Collections.<String>singletonList(revPattern), null);
                    if (revs.length == 0) {
                        LOG.log(Level.FINE, "doRebase: no revision returned for {0}", revPattern); //NOI18N
                    } else {
                        sourceRev = revs[0].getCSetShortID();
                    }
                }
            } catch (HgException.HgCommandCanceledException ex) {
            } catch (HgException ex) {
                // do nothing, just log, probably an unsupported hg revision language
                LOG.log(Level.INFO, null, ex);
            }
        }
        if (supp.isCanceled()) {
            return false;
        }
        
        supp.setDisplayName(Bundle.MSG_RebaseAction_progress_rebasingChangesets());
        RebaseCommand.Result rebaseResult = new RebaseCommand(root, RebaseCommand.Operation.START, logger)
                .setRevisionBase(base)
                .setRevisionSource(source)
                .setRevisionDest(dest)
                .call();
        handleRebaseResult(new RebaseHookContext(root, sourceRev, destRev, hooks), rebaseResult, supp);
        return rebaseResult.getState() == State.OK;
    }

    @Messages({
        "MSG_RebaseAction.progress.refreshingFiles=Refreshing files"
    })
    private void doRebase (final VCSFileProxy root, HgLogMessage workingCopyParent,
            Collection<HgLogMessage> branchHeads) {
        final Rebase rebase = new Rebase(root, workingCopyParent, branchHeads);
        if (rebase.showDialog()) {
            new HgProgressSupport() {
                @Override
                protected void perform () {
                    doRebase(rebase);
                }

                private void doRebase (final Rebase rebase) {
                    final HgProgressSupport supp = this;
                    OutputLogger logger = getLogger();
                    try {
                        logger.outputInRed(Bundle.MSG_Rebase_Title());
                        logger.outputInRed(Bundle.MSG_Rebase_Title_Sep());
                        logger.output(Bundle.MSG_Rebase_Started());
                        
                        HgUtils.runWithoutIndexing(new Callable<Void>() {
                            @Override
                            public Void call () throws Exception {
                                RebaseAction.doRebase(root, rebase.getRevisionBase(),
                                        rebase.getRevisionSource(),
                                        rebase.getRevisionDest(), supp);
                                supp.setDisplayName(Bundle.MSG_RebaseAction_progress_refreshingFiles());
                                HgUtils.forceStatusRefresh(root);
                                return null;
                            }
                        }, root);
                    } catch (HgException.HgCommandCanceledException ex) {
                        // canceled by user, do nothing
                    } catch (HgException ex) {
                        HgUtils.notifyException(ex);
                    }
                    logger.outputInRed(Bundle.MSG_Rebase_Finished());
                    logger.output(""); // NOI18N
                }
            }.start(Mercurial.getInstance().getRequestProcessor(root), root, Bundle.MSG_Rebase_Progress());
        }
    }

    @Messages({
        "MSG_Rebase_Abort=Aborting an interrupted rebase",
        "MSG_Rebase_Aborted=Rebase Aborted",
        "MSG_Rebase_Merging_Failed=Rebase interrupted because of a failed merge.\nResolve the conflicts and run the rebase again.",
        "MSG_Rebase_Continue=Continuing an interrupted rebase",
        "CTL_RebaseAction.continueButton.text=C&ontinue",
        "CTL_RebaseAction.continueButton.TTtext=Continue the interrupted rebase",
        "CTL_RebaseAction.abortButton.text=Abo&rt",
        "CTL_RebaseAction.abortButton.TTtext=Abort the interrupted rebase",
        "LBL_Rebase.rebasingState.title=Unfinished Rebase",
        "# {0} - repository name", "MSG_Rebase.rebasingState.text=Repository {0} is in the middle of an unfinished rebase.\n"
            + "Do you want to continue or abort the unfinished rebase?"
    })
    private void finishRebase (final VCSFileProxy root) {
        // abort or continue?
        JButton btnContinue = new JButton();
        Mnemonics.setLocalizedText(btnContinue, Bundle.CTL_RebaseAction_continueButton_text());
        btnContinue.setToolTipText(Bundle.CTL_RebaseAction_continueButton_TTtext());
        JButton btnAbort = new JButton();
        Mnemonics.setLocalizedText(btnAbort, Bundle.CTL_RebaseAction_abortButton_text());
        btnAbort.setToolTipText(Bundle.CTL_RebaseAction_abortButton_TTtext());
        Object value = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                Bundle.MSG_Rebase_rebasingState_text(root.getName()),
                Bundle.LBL_Rebase_rebasingState_title(),
                NotifyDescriptor.YES_NO_CANCEL_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                new Object[] { btnContinue, btnAbort, NotifyDescriptor.CANCEL_OPTION }, 
                btnContinue));
        if (value == btnAbort || value == btnContinue) {
            final boolean cont = btnContinue == value;
            new HgProgressSupport() {
                @Override
                protected void perform () {
                    finishRebase(cont);
                }
                
                private void finishRebase (final boolean cont) {
                    final OutputLogger logger = getLogger();
                    final HgProgressSupport supp = this;
                    try {
                        logger.outputInRed(Bundle.MSG_Rebase_Title());
                        logger.outputInRed(Bundle.MSG_Rebase_Title_Sep());
                        logger.output(cont
                                ? Bundle.MSG_Rebase_Continue()
                                : Bundle.MSG_Rebase_Abort());
                        HgUtils.runWithoutIndexing(new Callable<Void>() {
                            @Override
                            public Void call () throws Exception {
                                RebaseHookContext rebaseCtx = buildRebaseContext(root);
                                RebaseCommand.Result rebaseResult = new RebaseCommand(root, cont
                                        ? RebaseCommand.Operation.CONTINUE
                                        : RebaseCommand.Operation.ABORT, logger).call();
                                HgUtils.forceStatusRefresh(root);
                                handleRebaseResult(rebaseCtx, rebaseResult, supp);
                                return null;
                            }
                        }, root);
                    } catch (HgException.HgCommandCanceledException ex) {
                        // canceled by user, do nothing
                    } catch (HgException ex) {
                        HgUtils.notifyException(ex);
                    }
                    logger.outputInRed(Bundle.MSG_Rebase_Finished());
                    logger.output(""); // NOI18N
                }
            }.start(Mercurial.getInstance().getRequestProcessor(root), root, Bundle.MSG_Rebase_Progress());
        }
    }

    @Messages({
        "MSG_RebaseAction.progress.repairingPushHooks=Updating push hooks"
    })
    private static void handleRebaseResult (RebaseHookContext rebaseCtx, RebaseCommand.Result rebaseResult, HgProgressSupport supp) {
        OutputLogger logger = supp.getLogger();
        for (VCSFileProxy f : rebaseResult.getTouchedFiles()) {
            Mercurial.getInstance().notifyFileChanged(f);
        }
        logger.output(rebaseResult.getOutput());
        VCSFileProxy repository = rebaseCtx.repository;
        Mercurial.getInstance().historyChanged(repository);
        VCSFileProxySupport.delete(getNetBeansRebaseInfoFile(repository));
        
        if (rebaseResult.getState() == State.ABORTED) {
            logger.outputInRed(Bundle.MSG_Rebase_Aborted());
        } else if (rebaseResult.getState() == State.MERGING) {
            storeRebaseContext(rebaseCtx);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        Bundle.MSG_Rebase_Merging_Failed(),
                        NotifyDescriptor.ERROR_MESSAGE));
            logger.outputInRed(Bundle.MSG_Rebase_Merging_Failed());
        } else if (rebaseResult.getState() == State.OK) {
            if (!rebaseCtx.hooks.isEmpty() && rebaseCtx.source != null && rebaseCtx.dest != null) {
                VCSFileProxy bundleFile = rebaseResult.getBundleFile();
                if (bundleFile != null && bundleFile.exists()) {
                    supp.setDisplayName(Bundle.MSG_RebaseAction_progress_repairingPushHooks());
                    try {
                        HgHookContext.LogEntry[] originalEntries = findOriginalEntries(repository, bundleFile);
                        HgHookContext.LogEntry[] newEntries = findNewEntries(repository, rebaseCtx.dest);
                        Map<String, String> mapping = findChangesetMapping(originalEntries, newEntries);
                        for (HgHook hgHook : rebaseCtx.hooks) {
                            hgHook.afterCommitReplace(new HgHookContext(new VCSFileProxy[] { repository }, null, originalEntries),
                                    new HgHookContext(new VCSFileProxy[] { repository }, null, newEntries),
                                    mapping);
                        }
                    } catch (HgException.HgCommandCanceledException ex) {
                        // canceled by user, do nothing
                    } catch (HgException ex) {
                        // do nothing, just log
                        // probably an unsupported hg revision language
                        LOG.log(Level.INFO, null, ex);
                    }
                }
            }
        }
        logger.output("");
    }

    private static HgHookContext.LogEntry[] findOriginalEntries (VCSFileProxy repository, VCSFileProxy bundleFile) throws HgException {
        List<HgLogMessage> originalMessages = HgCommand.getBundleChangesets(repository, bundleFile, null);
        return convertToEntries(originalMessages.toArray(new HgLogMessage[originalMessages.size()]));
    }

    private static HgHookContext.LogEntry[] findNewEntries (VCSFileProxy repository, String destRevision) {
        HgLogMessage[] newMessages = HgCommand.getRevisionInfo(repository,
                Collections.<String>singletonList(MessageFormat.format(
                "descendants(last(children({0}), 1))", //NOI18N
                destRevision)), null);
        return convertToEntries(newMessages);
    }

    private static HgHookContext.LogEntry[] convertToEntries (HgLogMessage[] messages) {
        List<HgHookContext.LogEntry> entries = new ArrayList<>(messages.length);
        for (HgLogMessage msg : messages) {
            entries.add(new HgHookContext.LogEntry(
                    msg.getMessage(),
                    msg.getAuthor(),
                    msg.getCSetShortID(),
                    msg.getDate()));
        }
        return entries.toArray(new HgHookContext.LogEntry[entries.size()]);
    }
    
    private static Map<String, String> findChangesetMapping (HgHookContext.LogEntry[] originalEntries, HgHookContext.LogEntry[] newEntries) {
        Map<String, String> mapping = new HashMap<>(originalEntries.length);
        for (HgHookContext.LogEntry original : originalEntries) {
            boolean found = false;
            for (HgHookContext.LogEntry newEntry : newEntries) {
                if (original.getDate().equals(newEntry.getDate())
                        && original.getAuthor().equals(newEntry.getAuthor())
                        && original.getMessage().equals(newEntry.getMessage())) {
                    // is it really the same commit???
                    mapping.put(original.getChangeset(), newEntry.getChangeset());
                    found = true;
                    break;
                }
            }
            if (!found) {
                // delete ????
                mapping.put(original.getChangeset(), null);
            }
        }
        return mapping;
    }

    private static VCSFileProxy getNetBeansRebaseInfoFile (VCSFileProxy root) {
        return VCSFileProxy.createFileProxy(HgUtils.getHgFolderForRoot(root), NB_REBASE_INFO_FILE);
    }

    private static RebaseHookContext buildRebaseContext (VCSFileProxy root) {
        Collection<HgHook> hooks = VCSHooks.getInstance().getHooks(HgHook.class);
        VCSFileProxy info = getNetBeansRebaseInfoFile(root);
        String source = null;
        String dest = null;
        if (VCSFileProxySupport.canRead(info)) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(info.getInputStream(false), "UTF-8")); //NOI18N
                String line = br.readLine();
                if (line != null) {
                    source = line;
                }
                line = br.readLine();
                if (line != null) {
                    dest = line;
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, null, ex);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ex) {}
                }
            }
        }
        return new RebaseHookContext(root, source, dest, hooks);
    }

    private static void storeRebaseContext (RebaseHookContext context) {
        if (context.source == null || context.dest == null) {
            return;
        }
        VCSFileProxy info = getNetBeansRebaseInfoFile(context.repository);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(VCSFileProxySupport.getOutputStream(info), "UTF-8")); //NOI18N
            bw.write(context.source);
            bw.newLine();
            bw.write(context.dest);
            bw.newLine();
            bw.flush();
        } catch (IOException ex) {
            LOG.log(Level.INFO, null, ex);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {}
            }
        }
    }
    
    private static class RebaseHookContext {
        private final VCSFileProxy repository;
        private final String source;
        private final String dest;
        private final Collection<HgHook> hooks;

        public RebaseHookContext (VCSFileProxy repository, String sourceRev, String destRev, Collection<HgHook> hooks) {
            this.repository = repository;
            this.source = sourceRev;
            this.dest = destRev;
            this.hooks = hooks;
        }
    }
}
