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
package org.netbeans.modules.git.ui.history;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.GitUser;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.diff.ExportCommitAction;
import org.netbeans.modules.git.ui.revert.RevertCommitAction;
import org.netbeans.modules.git.ui.tag.CreateTagAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.history.AbstractSummaryView;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor.KenaiUser;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

class SummaryView extends AbstractSummaryView {

    private final SearchHistoryPanel master;
    
    private static final Logger LOG = Logger.getLogger(SummaryView.class.getName());
    
    private static DateFormat defaultFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    private static final Color HIGHLIGHT_BRANCH_FG = Color.BLACK;
    private static final Color HIGHLIGHT_TAG_FG = Color.BLACK;
    private static final Color HIGHLIGHT_BRANCH_BG = Color.decode("0xaaffaa"); //NOI18N
    private static final Color HIGHLIGHT_TAG_BG = Color.decode("0xffffaa"); //NOI18N
    
    static class GitLogEntry extends AbstractSummaryView.LogEntry implements PropertyChangeListener {

        private RepositoryRevision revision;
        private List events = new ArrayList<GitLogEvent>(10);
        private SearchHistoryPanel master;
        private String complexRevision;
        private final PropertyChangeListener list;
        private Collection<AbstractSummaryView.LogEntry.RevisionHighlight> complexRevisionHighlights;
    
        public GitLogEntry (RepositoryRevision revision, SearchHistoryPanel master) {
            this.revision = revision;
            this.master = master;
            revision.addPropertyChangeListener(RepositoryRevision.PROP_EVENTS_CHANGED, list = WeakListeners.propertyChange(this, revision));
        }

        @Override
        public Collection<AbstractSummaryView.LogEntry.Event> getEvents () {
            return events;
        }

        @Override
        public String getAuthor () {
            GitUser author = revision.getLog().getAuthor();
            return author == null ? "" : author.toString(); //NOI18N
        }

        @Override
        public String getDate () {
            Date date = new Date(revision.getLog().getCommitTime());
            return date != null ? defaultFormat.format(date) : null;
        }

        @Override
        public String getRevision () {
            if (complexRevision == null) {
                complexRevisionHighlights = new ArrayList<AbstractSummaryView.LogEntry.RevisionHighlight>(revision.getBranches().length + revision.getTags().length + 1);
                StringBuilder sb = new StringBuilder();
                // add branch labels
                for (GitBranch branch : revision.getBranches()) {
                    if (branch.getName() != GitBranch.NO_BRANCH) {
                        complexRevisionHighlights.add(new AbstractSummaryView.LogEntry.RevisionHighlight(sb.length(), branch.getName().length(), HIGHLIGHT_BRANCH_FG, HIGHLIGHT_BRANCH_BG));
                        sb.append(branch.getName()).append(' ');
                    }
                    if (branch.isActive()) {
                        complexRevisionHighlights.add(new AbstractSummaryView.LogEntry.RevisionHighlight(sb.length(), GitUtils.HEAD.length(), HIGHLIGHT_BRANCH_FG, HIGHLIGHT_BRANCH_BG));
                        sb.append(GitUtils.HEAD).append(' ');
                    }
                }
                // add tag labels
                for (GitTag tag : revision.getTags()) {
                    complexRevisionHighlights.add(new AbstractSummaryView.LogEntry.RevisionHighlight(sb.length(), tag.getTagName().length(), HIGHLIGHT_TAG_FG, HIGHLIGHT_TAG_BG));
                    sb.append(tag.getTagName()).append(' ');
                }
                String rev = revision.getLog().getRevision();
                sb.append(rev.length() > 7 ? rev.substring(0, 7) : rev);
                complexRevision = sb.toString();
            }
            return complexRevision;
        }

        @Override
        protected Collection<AbstractSummaryView.LogEntry.RevisionHighlight> getRevisionHighlights () {
            getRevision();
            return complexRevisionHighlights;
        }

        @Override
        public String getMessage () {
            return revision.getLog().getFullMessage();
        }

        @Override
        public Action[] getActions () {
            List<Action> actions = new ArrayList<Action>();
            boolean hasParents = revision.getLog().getParents().length > 0;
            
            if (hasParents) {
                actions.add(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_DiffToPrevious_Short")) { //NOI18N
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        diffPrevious(revision, master);
                    }
                });
            }
            actions.add(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_TagCommit")) { //NOI18N
                @Override
                public void actionPerformed (ActionEvent e) {
                    CreateTagAction action = SystemAction.get(CreateTagAction.class);
                    action.createTag(master.getRepository(), revision.getLog().getRevision());
                }
            });
            if (revision.getLog().getParents().length < 2) {
                actions.add(new AbstractAction(NbBundle.getMessage(ExportCommitAction.class, "LBL_ExportCommitAction_PopupName")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        ExportCommitAction action = SystemAction.get(ExportCommitAction.class);
                        action.exportCommit(master.getRepository(), revision.getLog().getRevision());
                    }
                });
                actions.add(new AbstractAction(NbBundle.getMessage(RevertCommitAction.class, "LBL_RevertCommitAction_PopupName")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        RevertCommitAction action = SystemAction.get(RevertCommitAction.class);
                        action.revert(master.getRepository(), master.getRoots(), revision.getLog().getRevision());
                    }
                });
            }
            return actions.toArray(new Action[actions.size()]);
        }

        @Override
        public String toString () {
            return revision.toString();
        }

        @Override
        protected void expand () {
            revision.expandEvents();
        }

        @Override
        protected void cancelExpand () {
            revision.cancelExpand();
        }

        @Override
        protected boolean isEventsInitialized () {
            return revision.isEventsInitialized();
        }

        @Override
        public boolean isVisible () {
            boolean visible = true;
            // can apply filter criteria here
            return visible;
        }

        @Override
        protected boolean isLessInteresting () {
            return getRepositoryRevision().getLog().getParents().length > 1;
        }

        RepositoryRevision getRepositoryRevision () {
            return revision;
        }

        void refreshEvents () {
            ArrayList<GitLogEvent> evts = new ArrayList<GitLogEvent>(revision.getEvents().size());
            for (RepositoryRevision.Event event : revision.getEvents()) {
                evts.add(new GitLogEvent(master, event));
            }
            List<GitLogEvent> oldEvents = new ArrayList<GitLogEvent>(events);
            List<GitLogEvent> newEvents = new ArrayList<GitLogEvent>(evts);
            events = evts;
            eventsChanged(oldEvents, newEvents);
        }

        @Override
        public void propertyChange (PropertyChangeEvent evt) {
            if (RepositoryRevision.PROP_EVENTS_CHANGED.equals(evt.getPropertyName()) && revision == evt.getSource()) {
                refreshEvents();
            }
        }
    }
    
    static class GitLogEvent extends AbstractSummaryView.LogEntry.Event {

        private final RepositoryRevision.Event event;
        private final SearchHistoryPanel master;

        GitLogEvent (SearchHistoryPanel master, RepositoryRevision.Event event) {
            this.master = master;
            this.event = event;
        }

        @Override
        public String getPath () {
            return event.getPath();
        }

        @Override
        public String getOriginalPath () {
            return event.getOriginalPath();
        }

        @Override
        public File getFile () {
            return event.getFile();
        }

        @Override
        public String getAction () {
            return Character.toString(event.getAction());
        }
        
        public RepositoryRevision.Event getEvent() {
            return event;
        }

        @Override
        public Action[] getUserActions () {
            List<Action> actions = new ArrayList<Action>();
            boolean viewEnabled = event.getFile() != null && event.getAction() != 'D';
            boolean hasParents = event.getLogInfoHeader().getLog().getParents().length > 0;
            
            if (hasParents) {
                actions.add(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_DiffToPrevious")) { // NOI18N
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        diffPrevious(event, master);
                    }
                });
            }
            if (viewEnabled) {
                actions.add(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_View")) { // NOI18N
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new GitProgressSupport() {
                            @Override
                            protected void perform () {
                                openFile(event, false, getProgressMonitor());
                            }
                        }.start(Git.getInstance().getRequestProcessor(), master.getRepository(), NbBundle.getMessage(SummaryView.class, "MSG_SummaryView.openingFilesFromHistory")); //NOI18N
                    }
                });
                actions.add(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_ShowAnnotations")) { // NOI18N
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new GitProgressSupport() {
                            @Override
                            protected void perform () {
                                openFile(event, true, getProgressMonitor());
                            }
                        }.start(Git.getInstance().getRequestProcessor(), master.getRepository(), NbBundle.getMessage(SummaryView.class, "MSG_SummaryView.openingFilesFromHistory")); //NOI18N
                    }
                });
            }
            return actions.toArray(new Action[actions.size()]);
        }

        @Override
        public boolean isVisibleByDefault () {
            return master.isShowInfo() || event.isUnderRoots();
        }

        @Override
        public String toString () {
            return event.toString();
        }
    }
    
    public SummaryView (SearchHistoryPanel master, List<? extends LogEntry> results, Map<String, KenaiUser> kenaiUserMap) {
        super(createViewSummaryMaster(master), results, kenaiUserMap);
        this.master = master;
    }
    
    private static SummaryViewMaster createViewSummaryMaster (final SearchHistoryPanel master) {
        final Map<String, String> colors = new HashMap<String, String>();
        colors.put("A", "#008000"); //NOI18N
        colors.put("C", "#008000"); //NOI18N
        colors.put("R", "#008000"); //NOI18N
        colors.put("M", "#0000ff"); //NOI18N
        colors.put("D", "#999999"); //NOI18N
        colors.put("?", "#000000"); //NOI18N

        return new SummaryViewMaster() {

            @Override
            public JComponent getComponent () {
                return master;
            }

            @Override
            public File[] getRoots () {
                return master.getRoots();
            }

            @Override
            public String getMessage () {
                return master.getCriteria().getCommitMessage();
            }

            @Override
            public Map<String, String> getActionColors () {
                return colors;
            }

            @Override
            public void getMoreResults (PropertyChangeListener callback, int count) {
                master.getMoreRevisions(callback, count);
            }

            @Override
            public boolean hasMoreResults () {
                return master.hasMoreResults();
            }
        };
    }
    
    @Override
    protected void onPopup (JComponent invoker, Point p, final Object[] selection) {
        JPopupMenu menu = new JPopupMenu();
        
        final RepositoryRevision container;
        final RepositoryRevision.Event[] drev;

        Object revCon = selection[0];
        boolean revisionSelected;
        boolean missingFile = false;        
        
        if (revCon instanceof GitLogEntry && selection.length == 1) {
            revisionSelected = true;
            container = ((GitLogEntry) selection[0]).revision;
            drev = new RepositoryRevision.Event[0];
        } else {
            revisionSelected = false;
            drev = new RepositoryRevision.Event[selection.length];

            for(int i = 0; i < selection.length; i++) {
                if (!(selection[i] instanceof GitLogEvent)) {
                    return;
                }
                drev[i] = ((GitLogEvent) selection[i]).getEvent();
                
                if(!missingFile && drev[i].getFile() == null) {
                    missingFile = true;
                }
            }                
            container = drev[0].getLogInfoHeader();
        }
        boolean hasParents = container.getLog().getParents().length > 0;

        final boolean singleSelection = selection.length == 1;
        final boolean viewEnabled = singleSelection && !revisionSelected && drev[0].getFile() != null && drev[0].getAction() != 'D';
        
        if (hasParents) {
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_DiffToPrevious")) { // NOI18N
                {
                    setEnabled(singleSelection);
                }
                @Override
                public void actionPerformed(ActionEvent e) {
                    diffPrevious(selection[0], master);
                }
            }));
        }

        if (revisionSelected) {
            if (singleSelection) {
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(CreateTagAction.class, "LBL_CreateTagAction_PopupName.revision", container.getLog().getRevision().substring(0, 7))) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        CreateTagAction action = SystemAction.get(CreateTagAction.class);
                        action.createTag(master.getRepository(), container.getLog().getRevision());
                    }
                }));
                if (container.getLog().getParents().length < 2) {
                    menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(ExportCommitAction.class, "LBL_ExportCommitAction_PopupName")) { //NOI18N
                        @Override
                        public void actionPerformed (ActionEvent e) {
                            ExportCommitAction action = SystemAction.get(ExportCommitAction.class);
                            action.exportCommit(master.getRepository(), container.getLog().getRevision());
                        }
                    }));
                    menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(RevertCommitAction.class, "LBL_RevertCommitAction_PopupName")) { //NOI18N
                        @Override
                        public void actionPerformed (ActionEvent e) {
                            RevertCommitAction action = SystemAction.get(RevertCommitAction.class);
                            action.revert(master.getRepository(), master.getRoots(), container.getLog().getRevision());
                        }
                    }));
                }
            }
        } else {
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_View")) { // NOI18N
                {
                    setEnabled(viewEnabled);
                }
                @Override
                public void actionPerformed(ActionEvent e) {
                    new GitProgressSupport() {
                        @Override
                        protected void perform () {
                            for (RepositoryRevision.Event evt : drev) {
                                openFile(evt, false, getProgressMonitor());
                            }
                        }
                    }.start(Git.getInstance().getRequestProcessor(), master.getRepository(), NbBundle.getMessage(SummaryView.class, "MSG_SummaryView.openingFilesFromHistory")); //NOI18N
                }
            }));
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_ShowAnnotations")) { // NOI18N
                {
                    setEnabled(viewEnabled);
                }
                @Override
                public void actionPerformed(ActionEvent e) {
                    new GitProgressSupport() {
                        @Override
                        protected void perform () {
                            for (RepositoryRevision.Event evt : drev) {
                                openFile(evt, true, getProgressMonitor());
                            }
                        }
                    }.start(Git.getInstance().getRequestProcessor(), master.getRepository(), NbBundle.getMessage(SummaryView.class, "MSG_SummaryView.openingFilesFromHistory")); //NOI18N
                }
            }));
        }
        menu.show(invoker, p.x, p.y);
    }

    private static void openFile (RepositoryRevision.Event evt, boolean showAnnotations, ProgressMonitor pm) {
        try {
            File originalFile = evt.getFile();
            String revision = evt.getLogInfoHeader().getLog().getRevision();
            GitUtils.openInRevision(originalFile, -1, revision, showAnnotations, pm);
        } catch (IOException ex) {
            LOG.log(Level.FINE, null, ex);
        }
    }

    private static void diffPrevious (Object o, SearchHistoryPanel master) {
        if (o instanceof RepositoryRevision.Event) {
            RepositoryRevision.Event drev = (RepositoryRevision.Event) o;
            master.showDiff(drev);
        } else if (o instanceof RepositoryRevision) {
            RepositoryRevision container = (RepositoryRevision) o;
            master.showDiff(container);
        } else if (o instanceof GitLogEvent) {
            master.showDiff(((GitLogEvent) o).event);
        } else if (o instanceof GitLogEntry) {
            master.showDiff(((GitLogEntry) o).revision);
        }
    }
}
