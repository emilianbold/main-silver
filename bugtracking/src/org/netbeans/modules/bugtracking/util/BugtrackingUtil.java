/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.util;

import org.netbeans.modules.bugtracking.kenai.spi.RecentIssue;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bugtracking.*;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.ui.issue.IssueTopComponent;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.ui.issue.PatchContextChooser;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.bugtracking.ui.query.QueryTopComponent;
import org.netbeans.modules.bugtracking.ui.search.QuickSearchComboBar;
import org.netbeans.modules.bugtracking.ui.selectors.RepositorySelector;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.windows.TopComponent;

/**
 *
 * @author Tomas Stupka, Jan Stola
 * @author Marian Petras
 */
public class BugtrackingUtil {

    public static boolean show(JPanel panel, String title, String okName) {
        JButton ok = new JButton(okName);
        ok.getAccessibleContext().setAccessibleDescription(ok.getText());
        JButton cancel = new JButton(NbBundle.getMessage(BugtrackingUtil.class, "LBL_Cancel")); // NOI18N
        cancel.getAccessibleContext().setAccessibleDescription(cancel.getText());
        final DialogDescriptor dd =
            new DialogDescriptor(
                    panel,
                    title,
                    true,
                    new Object[]{ok, cancel},
                    ok,
                    DialogDescriptor.DEFAULT_ALIGN,
                    new HelpCtx(panel.getClass()),
                    null);
        return DialogDisplayer.getDefault().notify(dd) == ok;
    }

    /**
     * Returns all curently openend issues which aren't new.
     * 
     * @return issues
     */
    public static Collection<IssueImpl> getOpenIssues() {
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        List<IssueImpl> issues = new ArrayList<IssueImpl>();
        for (TopComponent tc : tcs) {
            if(tc instanceof IssueTopComponent) {
                IssueImpl issue = ((IssueTopComponent)tc).getIssue();
                if(!issue.isNew()) {
                    issues.add(issue);
                }
            }
        }
        return issues;
    }

    /**
     * Determines if the gives issue is opened in the editor area
     * @param issue
     * @return true in case the given issue is opened in the editor are, otherwise false
     */
    public static boolean isOpened(IssueImpl issue) {
        IssueTopComponent tc = IssueTopComponent.find(issue, false);
        return tc != null ? tc.isOpened() : false;
    }

    /**
     * Determines if the gives issue is opened in the editor area and
     * showing on the screen
     * @param issue
     * @return true in case the given issue is opened in the editor area
     *         and showing on the screen, otherwise false
     */
    public static boolean isShowing(IssueImpl issue) {
        IssueTopComponent tc = IssueTopComponent.find(issue, false);
        return tc != null ? tc.isShowing() : false;
    }

    /**
     * Determines if the gives query is opened in the editor area
     * @param query
     * @return
     */
    public static boolean isOpened(QueryImpl query) {
        QueryTopComponent tc = QueryTopComponent.find(query);
        return tc != null ? tc.isOpened() : false;
    }

    /**
     * Determines if the gives query is opened in the editor area and
     * showing on the screen
     * @param query
     * @return
     */
    public static boolean isShowing(QueryImpl query) {
        QueryTopComponent tc = QueryTopComponent.find(query);
        return tc != null ? tc.isShowing() : false;
    }

    /**
     * Filters the given issue by the given criteria and returns
     * those which either case unsensitively contain the criteria
     * in their summary or those which id equals the criteria.
     *
     * @param issues
     * @param criteria
     * @return
     */
    public static Collection<IssueImpl> getByIdOrSummary(Collection<IssueImpl> issues, String criteria) {
        if(criteria == null) {
            return issues;
        }
        criteria = criteria.trim();
        if(criteria.equals("")) {                                               // NOI18N
            return issues;
        }
        criteria = criteria.toLowerCase();
        List<IssueImpl> ret = new ArrayList<IssueImpl>();
        for (IssueImpl issue : issues) {
            if(issue.isNew()) continue;
            String id = issue.getID();
            if(id == null) continue;
            String summary = issue.getSummary();
            if(id.toLowerCase().startsWith(criteria) ||
               (summary != null && summary.toLowerCase().indexOf(criteria) > -1))
            {
                ret.add(issue);
            }  
        }
        return ret;
    }

    public static RepositoryImpl createRepository() {
        return createRepository(true);
    }
    
    public static RepositoryImpl createRepository(boolean selectNode) {
        RepositorySelector rs = new RepositorySelector();
        RepositoryImpl repo = rs.create(selectNode);
        return repo;
    }

    public static boolean editRepository(Repository repository, String errorMessage) {
        RepositorySelector rs = new RepositorySelector();
        return rs.edit(APIAccessor.IMPL.getImpl(repository), errorMessage);
    }

    public static boolean editRepository(Repository repository) {
        return editRepository(repository, null);
    }

    public static Collection<RepositoryImpl> getKnownRepositories(boolean pingOpenProjects) {
        return RepositoryRegistry.getInstance().getKnownRepositories(pingOpenProjects);
    }

    public static Collection<RepositoryImpl> getRepositories(String id) {
        return RepositoryRegistry.getInstance().getRepositories(id);
    }    
    
    public static BugtrackingConnector[] getBugtrackingConnectors() {
        DelegatingConnector[] dcs = BugtrackingManager.getInstance().getConnectors();
        BugtrackingConnector[] cons = new BugtrackingConnector[dcs.length];
        for (int i = 0; i < cons.length; i++) {
            cons[i] = dcs[i].getDelegate();
        }
        return cons;
    }

    public static String scramble(String str) {
        return Scrambler.getInstance().scramble(str);
    }

    public static String descramble(String str) {
        return Scrambler.getInstance().descramble(str);
    }

    public static String selectIssue(String message, Repository repository, JPanel caller, HelpCtx helpCtx) {
        QuickSearchComboBar bar = new QuickSearchComboBar(caller);
        bar.setRepository(repository);
        bar.setAlignmentX(0f);
        bar.setMaximumSize(new Dimension(Short.MAX_VALUE, bar.getPreferredSize().height));
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
        panel.setLayout(layout);
        JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label, message);
        panel.add(label);
        label.setLabelFor(bar.getIssueComponent());
        LayoutStyle layoutStyle = LayoutStyle.getInstance();
        int gap = layoutStyle.getPreferredGap(label, bar, LayoutStyle.ComponentPlacement.RELATED, SwingConstants.SOUTH, panel);
        panel.add(Box.createVerticalStrut(gap));
        panel.add(bar);
        panel.add(Box.createVerticalStrut(gap));
        ResourceBundle bundle = NbBundle.getBundle(BugtrackingUtil.class);
        JLabel hintLabel = new JLabel(bundle.getString("MSG_SelectIssueHint")); // NOI18N
        hintLabel.setEnabled(false);
        panel.add(hintLabel);
        panel.add(Box.createVerticalStrut(80));
        panel.setBorder(BorderFactory.createEmptyBorder(
                layoutStyle.getContainerGap(panel, SwingConstants.NORTH, null),
                layoutStyle.getContainerGap(panel, SwingConstants.WEST, null),
                0,
                layoutStyle.getContainerGap(panel, SwingConstants.EAST, null)));
        panel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_IssueSelector"));
        Issue issue = null;
        JButton ok = new JButton(bundle.getString("LBL_Select")); // NOI18N
        ok.getAccessibleContext().setAccessibleDescription(ok.getText());
        JButton cancel = new JButton(bundle.getString("LBL_Cancel")); // NOI18N
        cancel.getAccessibleContext().setAccessibleDescription(cancel.getText());
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                bundle.getString("LBL_Issues"), // NOI18N
                true,
                NotifyDescriptor.OK_CANCEL_OPTION,
                ok,
                null);
        descriptor.setOptions(new Object [] {ok, cancel});
        descriptor.setHelpCtx(helpCtx);
        DialogDisplayer.getDefault().createDialog(descriptor).setVisible(true);
        if (descriptor.getValue() == ok) {
            issue = bar.getIssue();
        }
        return issue != null ? issue.getID() : null;
    }

    public static File selectPatchContext() {
        PatchContextChooser chooser = new PatchContextChooser();
        ResourceBundle bundle = NbBundle.getBundle(BugtrackingUtil.class);
        JButton ok = new JButton(bundle.getString("LBL_Apply")); // NOI18N
        JButton cancel = new JButton(bundle.getString("LBL_Cancel")); // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(
                chooser,
                bundle.getString("LBL_ApplyPatch"), // NOI18N
                true,
                NotifyDescriptor.OK_CANCEL_OPTION,
                ok,
                null);
        descriptor.setOptions(new Object [] {ok, cancel});
        descriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.bugtracking.patchContextChooser")); // NOI18N
        File context = null;
        DialogDisplayer.getDefault().createDialog(descriptor).setVisible(true);
        if (descriptor.getValue() == ok) {
            context = chooser.getSelectedFile();
        }
        return context;
    }

    /**
     * Recursively deletes all files and directories under a given file/directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }

    private static Pattern netbeansUrlPattern = Pattern.compile("(https|http)://(([a-z]|\\d)+\\.)*([a-z]|\\d)*netbeans([a-z]|\\d)*(([a-z]|\\d)*\\.)+org(.*)"); // NOI18N
    /**
     * Determines wheter the given {@link RepositoryProvider} is the
     * repository hosting netbeans or not
     *
     * @param repo
     * @return true if the given repository is the netbenas bugzilla, otherwise false
     */
    public static boolean isNbRepository(String url) {
        boolean ret = netbeansUrlPattern.matcher(url).matches();
        if(ret) {
            return true;
        }
        String nbUrl = System.getProperty("netbeans.bugzilla.url");  // NOI18N
        if(nbUrl == null || nbUrl.equals("")) {                      // NOI18N
            return false;
        }
        return url.startsWith(nbUrl);
    }

    /**
     *
     * @param password
     * @param prefix
     * @param user
     * @param url
     * @throws MissingResourceException
     */
    public static void savePassword(String password, String prefix, String user, String url) throws MissingResourceException {
        savePassword(password.toCharArray(), prefix, user, url);
    }
    
    public static void savePassword(char[] password, String prefix, String user, String url) throws MissingResourceException {
        if (password != null && password.length != 0) {                  
            Keyring.save(getPasswordKey(prefix, user, url), password, NbBundle.getMessage(BugtrackingUtil.class, "password_keyring_description", url)); // NOI18N
        } else {
            Keyring.delete(getPasswordKey(prefix, user, url));
        }
    }

    /**
     *
     * @param scrambledPassword
     * @param keyPrefix
     * @param url
     * @param user
     * @return
     */
    public static char[] readPassword(String scrambledPassword, String keyPrefix, String user, String url) {
        if (scrambledPassword != null && !scrambledPassword.equals("")) {                                    // NOI18N
            return BugtrackingUtil.descramble(scrambledPassword).toCharArray();
        } else {
            char[] password = Keyring.read(getPasswordKey(keyPrefix, user, url));
            return password != null ? password : new char[0];
        }
    }
    
    private static String getPasswordKey(String prefix, String user, String url) {
        return (prefix != null ? prefix + "-" : "") + user + "@" + url;         // NOI18N
    }


    /**
     * Determines if the jira plugin is instaled or not
     *
     * @return true if jira plugin is installed, otherwise false
     */
    public static boolean isJiraInstalled() {
        DelegatingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        for (DelegatingConnector c : connectors) {
            // XXX hack
            if(c.getDelegate().getClass().getName().startsWith("org.netbeans.modules.jira")) {    // NOI18N
                return true;
            }
        }
        return false;
    }

    public static void openQuery(final QueryImpl query, final RepositoryImpl repository, final boolean suggestedSelectionOnly) {
        QueryAction.openQuery(query, repository, suggestedSelectionOnly);
    }

    public static void openIssue(File file, String issueId) {
        IssueAction.openIssue(file, issueId);
    }

    public static Map<String, List<RecentIssue>> getAllRecentIssues() {
        return BugtrackingManager.getInstance().getAllRecentIssues();
    }

    public static Collection<IssueImpl> getRecentIssues(RepositoryImpl repo) {
        return BugtrackingManager.getInstance().getRecentIssues(repo);
    }

    public static String getPasswordLog(char[] psswd) {
        if(psswd == null) {
            return ""; // NOI18N
        }
        if("true".equals(System.getProperty("org.netbeans.modules.bugtracking.logPasswords", "false"))) { // NOI18N
            return new String(psswd); 
        }
        return "******"; // NOI18N
    }

    private static final String NB_BUGZILLA_PASSWORD = "nbbugzilla.password";                // NOI18N
    private static final String NB_BUGZILLA_USERNAME = "nbbugzilla.username";                // NOI18N
    
    /**
     * Returns the netbeans.org username
     * Shouldn't be called in awt
     *
     * @return username
     */
    public static String getNBUsername() {
        String user = BugtrackingConfig.getInstance().getPreferences().get(NB_BUGZILLA_USERNAME, ""); // NOI18N
        if("".equals(user)) {
            user = RepositoryRegistry.getBugzillaNBUsername();
        }
        return user.equals("") ? null : user;                         // NOI18N
    }

    /**
     * Returns the netbeans.org password
     * Shouldn't be called in awt
     *
     * @return password
     */
    public static char[] getNBPassword() {
        return Keyring.read(NB_BUGZILLA_PASSWORD);
    }

    /**
     * Save the given username as a netbeans.org username.
     * Shouldn't be called in awt
     */
    public static void saveNBUsername(String username) {
        if(username == null) {
            return;
        }
        BugtrackingConfig.getInstance().getPreferences().put(NB_BUGZILLA_USERNAME, username);
    }

    /**
     * Saves the given value as a netbeans.org password
     * Shouldn't be called in awt
     */
    public static void saveNBPassword(char[] password) {
        if(password == null) {
            Keyring.delete(NB_BUGZILLA_PASSWORD);
        } else {
            Keyring.save(
                NB_BUGZILLA_PASSWORD,
                password,
                NbBundle.getMessage(
                    BugtrackingUtil.class,
                    "NBRepositorySupport.password_keyring_description"));       // NOI18N

        }
    }
    
    public static File getFile(Node[] nodes) {
        if(nodes == null || nodes.length == 0) {
            return null;
        }
        final Lookup nodeLookup = nodes[0].getLookup();

        Project project = nodeLookup.lookup(Project.class);
        if (project != null) {
            return getFile(project);
        }

        DataObject dataObj = nodeLookup.lookup(DataObject.class);
        if (dataObj != null) {
            return getFile(dataObj);
        }
        return null;
    }
    
    private static File getFile(Project project) {
        FileObject fileObject = project.getProjectDirectory();
        return org.openide.filesystems.FileUtil.toFile(fileObject);
    }

    private static File getFile(DataObject dataObj) {
        FileObject fileObj = dataObj.getPrimaryFile();
        if (fileObj == null) {
            return null;
        }

        Project project = FileOwnerQuery.getOwner(fileObj);
        if (project != null) {
            return getFile(project);
        }
        return org.openide.filesystems.FileUtil.toFile(fileObj);
    }  
      
}
