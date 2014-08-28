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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.autoupdate;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.bugtracking.commons.AutoupdateSupport;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider;
import org.netbeans.modules.jira.client.spi.JiraConnectorSupport;
import org.netbeans.modules.jira.client.spi.JiraVersion;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;
import org.netbeans.modules.team.ide.spi.SettingsServices;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;

/**
 *
 * @author Tomas Stupka
 */
public class JiraAutoupdate {

    public static final JiraVersion SUPPORTED_JIRA_VERSION;
    private static JiraAutoupdate instance;
    static {
        String version = System.getProperty("netbeans.t9y.jira.supported.version"); // NOI18N
        SUPPORTED_JIRA_VERSION = JiraConnectorSupport.getInstance().getConnector().createJiraVersion(version != null ? version : "6.0"); // NOI18N
    }
    static final String JIRA_MODULE_CODE_NAME = "org.netbeans.modules.jira"; // NOI18N
    private static final Pattern VERSION_PATTERN = Pattern.compile("^.*version ((\\d+?\\.\\d+?\\.\\d+?)|(\\d+?\\.\\d+?)).*$"); // NOI18N
    
    private final Set<JiraRepository> repos = new WeakSet<>();
    
    private final AutoupdateSupport support = new AutoupdateSupport(new AutoupdateCallback(), JIRA_MODULE_CODE_NAME, NbBundle.getMessage(Jira.class, "LBL_ConnectorName"));
    private boolean connectorNotified;

    private JiraAutoupdate() { }

    public static JiraAutoupdate getInstance() {
        if(instance == null) {
            instance = new JiraAutoupdate();
        }
        return instance;
    }
    
    /**
     * Checks if the remote JIRA has a version higher then actually supported and if
     * an update is available on the UC.
     *
     * @param repository the repository to check the version for
     */
    public void checkAndNotify(JiraRepository repository) {
        repos.add(repository);
        support.checkAndNotify(repository.getUrl());

        if(JiraConnectorSupport.getActiveConnector() != JiraConnectorProvider.Type.XMLRPC) {
            return;
        }
        JiraVersion serverVersion = getSupportedServerVersion(repository);
        if(serverVersion == null) {
            return;
        }
        JiraVersion version50 = JiraConnectorSupport.getInstance().getConnector().createJiraVersion("5.0.0");
        if(serverVersion.compareTo(version50) >= 0) {
            askToChangeConnector();
        }         
    }
    
    public JiraVersion getSupportedServerVersion(final JiraRepository repository) {
        final String[] v = new String[1];
        BugtrackingCommand cmd = new BugtrackingCommand() {
            @Override
            public void execute() throws CoreException, IOException, MalformedURLException {
                JiraConfiguration conf = repository.getConfiguration();
                v[0] = conf.getServerVersion();
            }
        };
        repository.getExecutor().execute(cmd, false, false, false);
        if(cmd.hasFailed()) {
            return null; // be optimistic at this point
        }
        return JiraConnectorSupport.getInstance().getConnector().createJiraVersion(v[0]);
    }

    public AutoupdateSupport getAutoupdateSupport() {
        return support;
    }
    
    public boolean isSupportedVersion(JiraVersion version) {
        return version.compareTo(SUPPORTED_JIRA_VERSION) <= 0;
    }

    public JiraVersion getVersion(String desc) {
        Matcher m = VERSION_PATTERN.matcher(desc);
        if(m.matches()) {
            return JiraConnectorSupport.getInstance().getConnector().createJiraVersion(m.group(1)) ;
        }
        return null;
    }
    
    class AutoupdateCallback implements AutoupdateSupport.Callback {
        @Override
        public String getServerVersion(String url) {
            JiraRepository repository = null;
            for (JiraRepository r : repos) {
                if(r.getUrl().equals(url)) {
                    repository = r;
                }
            }
            assert repository != null;
            JiraVersion version = JiraAutoupdate.this.getSupportedServerVersion(repository);
            return version != null ? version.toString() : null;
        }

        @Override
        public boolean checkIfShouldDownload(String desc) {
            JiraVersion version = getVersion(desc);
            return version != null && SUPPORTED_JIRA_VERSION.compareTo(version) < 0;
        }

        @Override
        public boolean isSupportedVersion(String version) {
            return JiraAutoupdate.this.isSupportedVersion(JiraConnectorSupport.getInstance().getConnector().createJiraVersion(version));
        }
    };    
    
    @NbBundle.Messages({"CTL_Restart=Change Connector",
                        "CTL_RestartClickHere=You are accessing a JIRA server with a version higher than 5.0. "
                                + "Click here to change your connector settings."})
    private void askToChangeConnector() {
        if( connectorNotified ) {
            return;
        }
        connectorNotified = true;
        
        final SettingsServices settings = Lookup.getDefault().lookup(SettingsServices.class);
        
        AbstractAction a = null;
        if(settings != null && settings.providesOpenSection(SettingsServices.Section.TASKS)) {
            a = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    settings.openSection(SettingsServices.Section.TASKS);
                }
            };
        }
        NotificationDisplayer.getDefault().notify(
            Bundle.CTL_Restart(),
            ImageUtilities.loadImageIcon( "org/netbeans/modules/jira/resources/warning.gif", true ), //NOI18N
            Bundle.CTL_RestartClickHere(), 
            a,
            NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.INFO);
    }  
}
