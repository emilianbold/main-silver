/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.kenai.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.ui.dashboard.DashboardImpl;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.QueryAccessor;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

public class KenaiPopupMenu extends CookieAction {

    private static HashMap<VersioningSystem, JComponent[]> versioningItemMap = new HashMap<VersioningSystem, JComponent[]>();
    private static Map<Project, String> repoForProjCache = new WeakHashMap<Project, String>();

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new KenaiPopupMenuPresenter(actionContext);
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[]{Project.class, DataFolder.class};
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(KenaiPopupMenu.class, "KENAI_POPUP"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private final class KenaiPopupMenuPresenter extends AbstractAction implements Presenter.Popup {

        private final Project proj;
        private final int MAX_VERSIONING_ITEMS = 2;

        private KenaiPopupMenuPresenter(Lookup actionContext) {
            proj = actionContext.lookup(Project.class);
        }

        private JMenu constructKenaiMenu() {
            // show for Kenai projects
            final JMenu kenaiPopup = new JMenu(NbBundle.getMessage(KenaiPopupMenu.class, "KENAI_POPUP")); //NOI18N
            kenaiPopup.setVisible(true);
            KenaiFeature[] issueTrackers = null;
            try {
                /* Show actions related to versioning - commit/update */
                VersioningSystem owner = VersioningSupport.getOwner(FileUtil.toFile(proj.getProjectDirectory()));
                JComponent[] items = createVersioningSystemItems(owner, getActivatedNodes());
                for (int i = 0; i < items.length; i++) {
                    JComponent item = items[i];
                    if (item != null) {
                        kenaiPopup.add(item);
                    }
                }
                kenaiPopup.addSeparator();
                /* Add action to navigate to Kenai project - based on repository URL (not on Kenai dashboard at the moment) */
                // if isKenaiProject==true, there must be cached result + it is different from ""
                String projRepo = repoForProjCache.get(proj);
                String kpName = KenaiProject.getNameForRepository(projRepo);
                if (kpName != null) {
                    if (inDashboard(kpName)) {
                        issueTrackers = Kenai.getDefault().getProject(kpName).getFeatures(Type.ISSUES);
                        if (issueTrackers != null && issueTrackers.length > 0) {
                            kenaiPopup.add(new LazyFindIssuesAction(proj, kpName));
                            kenaiPopup.add(new LazyNewIssuesAction(proj, kpName));
                        }
                    } else {
                        JMenuItem issuesNAItem = new JMenuItem(NbBundle.getMessage(KenaiPopupMenu.class, "LBL_NA_SERVICES"));
                        issuesNAItem.setEnabled(false);
                        issuesNAItem.setVisible(true);
                        kenaiPopup.add(issuesNAItem);
                    }
                    kenaiPopup.addSeparator();
                    kenaiPopup.add(new LazyOpenKenaiProjectAction(kpName));
                }
            } catch (KenaiException ex) {
                Exceptions.printStackTrace(ex);
            }
            return kenaiPopup;
        }

        private boolean isDesiredAction(Action action) {
            final String[] DESIRED_ACTIONS = {"commit", "update"}; //NOI18N
            String correctedName = ((String)action.getValue(Action.NAME)).replaceAll("[&.]", "").toLowerCase(); //NOI18N
            return Arrays.asList(DESIRED_ACTIONS).contains(correctedName);
        }

        private JComponent[] createVersioningSystemItems(VersioningSystem owner, Node[] nodes) {
            JComponent[] items = versioningItemMap.get(owner);
            if (items != null) {
                return items;
            }
            VCSAnnotator an = owner.getVCSAnnotator();
            if (an == null) {
                return null;
            }
            VCSContext ctx = VCSContext.forNodes(nodes);
            Action[] actions = an.getActions(ctx, VCSAnnotator.ActionDestination.MainMenu);
            items = new JComponent[MAX_VERSIONING_ITEMS];
            int i = 0;
            for (Action action : actions) {
                if (action != null) {
                    if (!isDesiredAction(action)) {
                        continue;
                    }
                    JMenuItem item = createmenuItem(action);
                    items[i++] = item;
                    if (i == MAX_VERSIONING_ITEMS) {
                        break;
                    }
                }
            }
            /* XXX #176013
            versioningItemMap.put(owner, items);
            */
            return items;
        }

        private JMenuItem createmenuItem(Action action) {
            JMenuItem item;
            if (action instanceof SystemAction) {
                final SystemAction sa = (SystemAction) action;
                item = new JMenuItem(new AbstractAction(sa.getName()) {

                    public void actionPerformed(ActionEvent e) {
                        sa.actionPerformed(e);
                    }
                });
            } else {
                item = new JMenuItem(action);
            }
            Mnemonics.setLocalizedText(item, (String) action.getValue(Action.NAME));
            return item;
        }

        public JMenuItem getPopupPresenter() {
            JMenu kenaiPopup = new JMenu(); //NOI18N
            kenaiPopup.setVisible(false);
            if (proj == null || !isKenaiProject(proj) || getActivatedNodes().length > 1) { // hide for non-Kenai projects
                if (repoForProjCache.get(proj) == null && getActivatedNodes().length == 1) {
                    final JMenu dummy = new JMenu(NbBundle.getMessage(KenaiPopupMenu.class, "LBL_CHECKING")); //NOI18N
                    dummy.setVisible(true);
                    dummy.setEnabled(false);
                    RequestProcessor.getDefault().post(new Runnable() { // cache the results, update the popup menu

                        public void run() {
                            String s = (String) proj.getProjectDirectory().getAttribute("ProvidedExtensions.RemoteLocation"); //NOI18N
                            if (s == null) {
                                repoForProjCache.put(proj, ""); // null cannot be used - project with no repo is null, "" is to indicate I already checked this one...
                                dummy.setVisible(false);
                            } else {
                                repoForProjCache.put(proj, s);
                                final JMenu tmp = constructKenaiMenu();
                                final Component[] c = tmp.getMenuComponents();
                                SwingUtilities.invokeLater(new Runnable() {

                                    public void run() {
                                        tmp.revalidate();
                                        dummy.setText(NbBundle.getMessage(KenaiPopupMenu.class, "KENAI_POPUP"));
                                        dummy.setEnabled(true);
                                        for (int i = 0; i < c.length; i++) {
                                            Component item = c[i];
                                            dummy.add(item);
                                        }
                                    }
                                });
                            }
                        }
                    });
                    return dummy;
                }
            } else { // show for Kenai projects
                kenaiPopup = constructKenaiMenu();
            }
            return kenaiPopup;
        }

        boolean isKenaiProject(final Project proj) {
            assert proj != null;
            String projRepo = repoForProjCache.get(proj);
            if (projRepo == null) { // repo is not cached - has to be cached on the background before
                return false;
            }
            if (!projRepo.equals("")) {
                return KenaiProject.getNameForRepository(projRepo) !=null;
            }
            return false;
        }

        public void actionPerformed(ActionEvent e) {
        }

        private boolean inDashboard(String projID) {
            ProjectHandle[] prj = Dashboard.getDefault().getOpenProjects();
            for (int i = 0; i < prj.length; i++) {
                ProjectHandle ph = prj[i];
                if (ph.getId().equals(projID)) {
                    return true;
                }
            }
            return false;
        }

    }

    class LazyFindIssuesAction extends JMenuItem {

        public LazyFindIssuesAction(final Project proj, final String kenaiProjectUniqueName) {
            super(NbBundle.getMessage(KenaiPopupMenu.class, "FIND_ISSUE")); //NOI18N
            this.addActionListener(new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    new RequestProcessor("__ISSUETRACKER", 1).post(new Runnable() { //NOI18N

                        public void run() {
                            ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupMenu.class, "CONTACTING_ISSUE_TRACKER"));  //NOI18N
                            handle.start();
                            try {
                                final KenaiProject kp = Kenai.getDefault().getProject(kenaiProjectUniqueName);
                                if (kp != null) {
                                    final ProjectHandleImpl pHandle = new ProjectHandleImpl(kp);
                                    DashboardImpl.getInstance().addProject(pHandle, false, true);
                                    QueryAccessor.getDefault().getFindIssueAction(pHandle).actionPerformed(e);
                                    return;
                                }
                            } catch (KenaiException e) {
                                e.printStackTrace();
                            } finally {
                                handle.finish();
                            }
                        }
                    });
                }
            });
        }
    }

    class LazyNewIssuesAction extends JMenuItem {

        public LazyNewIssuesAction(final Project proj, final String kenaiProjectUniqueName) {
            super(NbBundle.getMessage(KenaiPopupMenu.class, "NEW_ISSUE")); //NOI18N
            this.addActionListener(new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    new RequestProcessor("__ISSUETRACKER", 1).post(new Runnable() {  //NOI18N

                        public void run() {
                            ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupMenu.class, "CONTACTING_ISSUE_TRACKER")); //NOI18N
                            handle.start();
                            try {
                                final KenaiProject kp = Kenai.getDefault().getProject(kenaiProjectUniqueName);
                                if (kp != null) {
                                    final ProjectHandleImpl pHandle = new ProjectHandleImpl(kp);
                                    DashboardImpl.getInstance().addProject(pHandle, false, true);
                                    QueryAccessor.getDefault().getCreateIssueAction(pHandle).actionPerformed(e);
                                }
                            } catch (KenaiException e) {
                                e.printStackTrace();
                            } finally {
                                handle.finish();
                            }
                        }
                    });
                }
            });
        }
    }

    class LazyOpenKenaiProjectAction extends JMenuItem {

        public LazyOpenKenaiProjectAction(final String kenaiProjectUniqueName) {
            super(NbBundle.getMessage(KenaiPopupMenu.class, "OPEN_CORRESPONDING_KENAI_PROJ")); //NOI18N
            this.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            KenaiTopComponent.findInstance().open();
                            KenaiTopComponent.findInstance().requestActive();
                        }
                    });
                    RequestProcessor.getDefault().post(new Runnable() {

                        public void run() {
                            ProgressHandle handle = null;
                            try {
                                handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupMenu.class, "CTL_OpenKenaiProjectAction")); //NOI18N
                                handle.start();
                                final KenaiProject kp = Kenai.getDefault().getProject(kenaiProjectUniqueName);
                                if (kp != null) {
                                    final ProjectHandleImpl pHandle = new ProjectHandleImpl(kp);
                                    DashboardImpl.getInstance().addProject(pHandle, false, true);
                                }
                            } catch (KenaiException ex) {
                                Exceptions.printStackTrace(ex);
                            } finally {
                                if (handle != null) {
                                    handle.finish();
                                    return;
                                }
                            }
                        }
                    });
                }
            });
        }
    }

}
