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
package org.netbeans.modules.web.clientproject;

import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider;
import org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo;
import org.netbeans.modules.web.clientproject.api.platform.PlatformProvider;
import org.netbeans.modules.web.clientproject.grunt.GruntfileExecutor;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.modules.web.clientproject.util.FileUtilities;
import org.netbeans.modules.web.common.api.UsageLogger;
import org.netbeans.spi.project.ActionProvider;

import static org.netbeans.spi.project.ActionProvider.COMMAND_TEST;

import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class ClientSideProjectActionProvider implements ActionProvider {

    private final ClientSideProject project;
    private final UsageLogger jsTestRunUsageLogger = UsageLogger.jsTestRunUsageLogger(ClientSideProjectUtilities.USAGE_LOGGER_NAME);
    private static final RequestProcessor RP = new RequestProcessor("ClientSideProjectActionProvider"); //NOI18N
    private static final Logger LOGGER = Logger.getLogger(ClientSideProjectActionProvider.class.getName());

    public ClientSideProjectActionProvider(ClientSideProject project) {
        this.project = project;
    }

    @Override
    public String[] getSupportedActions() {
        return new String[]{
                    COMMAND_RUN_SINGLE,
                    COMMAND_BUILD,
                    COMMAND_REBUILD,
                    COMMAND_CLEAN,
                    COMMAND_RUN,
                    COMMAND_RENAME,
                    COMMAND_MOVE,
                    COMMAND_COPY,
                    COMMAND_DELETE,
                    COMMAND_TEST,
                };
    }

    private ActionProvider getActionProvider() {
        ClientProjectEnhancedBrowserImplementation cfg = project.getEnhancedBrowserImpl();
        if (cfg != null) {
            return cfg.getActionProvider();
        } else {
            return null;
        }
    }

    // XXX this method should _really_ be refactored (will be done once grunt is removed from this module)
    @NbBundle.Messages({
        "LBL_ConfigureGrunt=Action not supported for this configuration.\nDo you want to configure project actions to call Grunt tasks?"
    })
    @Override
    public void invokeAction(final String command, final Lookup context) throws IllegalArgumentException {
        LifecycleManager.getDefault().saveAll();
        for (PlatformProvider provider : project.getPlatformProviders()) {
            ActionProvider actionProvider = provider.getActionProvider(project);
            if (actionProvider != null
                    && isSupportedAction(command, actionProvider)) {
                actionProvider.invokeAction(command, context);
            }
        }
        if (COMMAND_RUN_SINGLE.equals(command)
                || COMMAND_RUN.equals(command)) {
            if (project.isJsLibrary()) {
                return;
            }
            if (COMMAND_RUN_SINGLE.equals(command)) {
                FileObject fo = context.lookup(FileObject.class);
                if (fo != null
                        && FileUtilities.isJavaScriptFile(fo)) {
                    return;
                }
            }
            project.logBrowserUsage();
        }
        // XXX sorry no idea how to do this correctly
        if (COMMAND_RENAME.equals(command)) {
            renameProject();
            return;
        }
        // XXX sorry no idea how to do this correctly
        if (COMMAND_MOVE.equals(command)) {
            moveProject();
            return;
        }
        // XXX sorry no idea how to do this correctly
        if (COMMAND_COPY.equals(command)) {
            copyProject();
            return;
        }
        // XXX sorry no idea how to do this correctly
        if (COMMAND_DELETE.equals(command)) {
            deleteProject();
            return;
        }
        final ActionProvider ap = getActionProvider();
        if (ap != null && isSupportedAction(command, ap)) {
            // #217362 and possibly others
            if (project.isBroken(true)) {
                return;
            }
            RP.post(new Runnable() {
                @Override
                public void run() {
                    tryGrunt(command, false, true);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            ap.invokeAction(command, context);
                        }
                    });
                }
            });
            return;
        }
        if (COMMAND_TEST.equals(command)) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    tryGrunt(command, false, true);
                    runTests();
                }
            });
            return;
        }

        if (tryGrunt(command, true, false)) {
            return;
        }

        NotifyDescriptor desc = new NotifyDescriptor("Action not supported for this configuration", //NOI18N
                "Action not supported", //NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.INFORMATION_MESSAGE,
                new Object[]{NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(desc);
    }

    private boolean tryGrunt(String command, boolean showCustomizer, boolean waitFinished) {
        FileObject gruntFile = project.getProjectDirectory().getFileObject("Gruntfile.js");
        if (gruntFile != null) {
            String gruntBuild = project.getEvaluator().getProperty("grunt.action." + command);
            if (gruntBuild != null) {
                try {
                    ExecutorTask execute = new GruntfileExecutor(gruntFile, gruntBuild.split(" ")).execute();
                    if (waitFinished) {
                        execute.result();
                    }
                    return true;
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (showCustomizer) {
                NotifyDescriptor desc = new NotifyDescriptor.Confirmation(Bundle.LBL_ConfigureGrunt(), NotifyDescriptor.YES_NO_OPTION);
                Object option = DialogDisplayer.getDefault().notify(desc);
                if (option == NotifyDescriptor.YES_OPTION) {
                    project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer("grunt");
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        for (PlatformProvider provider : project.getPlatformProviders()) {
            ActionProvider actionProvider = provider.getActionProvider(project);
            if (actionProvider != null
                    && isSupportedAction(command, actionProvider)
                    && actionProvider.isActionEnabled(command, context)) {
                return true;
            }
        }
        if (COMMAND_RUN_SINGLE.equals(command)
                || COMMAND_RUN.equals(command)) {
            if (project.isJsLibrary()) {
                return false;
            }
        }
        if (COMMAND_RUN_SINGLE.equals(command)) {
            FileObject fo = context.lookup(FileObject.class);
            if (fo != null
                    && FileUtilities.isJavaScriptFile(fo)) {
                return false;
            }
        }
        ActionProvider ap = getActionProvider();
        if (ap != null && isSupportedAction(command, ap)) {
            return ap.isActionEnabled(command, context);
        }
        if (COMMAND_TEST.equals(command)) {
            // provider itself will handle if anything is incorrect
            return true;
        }
        // not sure how to force js-test-driver to run single test; I tried everything according
        // to their documentation and it always runs all tests
//        if (COMMAND_TEST_SINGLE.equals(command)) {
//            FileObject fo = getFile(context);
//            return (fo != null && "js".equals(fo.getExt()) && project.getConfigFolder() != null &&
//                    project.getConfigFolder().getFileObject("jsTestDriver.conf") != null &&
//                    project.getTestsFolder() != null &&
//                    FileUtil.isParentOf(project.getTestsFolder(), fo));
//        }
//        Project prj = context.lookup(Project.class);
//        ClientSideConfigurationProvider provider = prj.getLookup().lookup(ClientSideConfigurationProvider.class);
//        if (provider.getActiveConfiguration().getBrowser() != null) {
//            return true;
//        }
//        return false;
        return true;
    }

    private boolean isSupportedAction(String command, ActionProvider ap) {
        for (String c : ap.getSupportedActions()) {
            if (command.equals(c)) {
                return true;
            }
        }
        return false;
    }

    private void renameProject() {
        DefaultProjectOperations.performDefaultRenameOperation(project, null);
    }

    private void moveProject() {
        DefaultProjectOperations.performDefaultMoveOperation(project);
    }

    private void copyProject() {
        DefaultProjectOperations.performDefaultCopyOperation(project);
    }

    private void deleteProject() {
        DefaultProjectOperations.performDefaultDeleteOperation(project);
    }

    void runTests() {
        JsTestingProvider testingProvider = project.getJsTestingProvider(true);
        if (testingProvider != null) {
            jsTestRunUsageLogger.log(ClientSideProjectType.TYPE, testingProvider.getIdentifier());
            TestRunInfo testRunInfo = new TestRunInfo.Builder()
                    .build();
            testingProvider.runTests(project, testRunInfo);
        }
    }

}
