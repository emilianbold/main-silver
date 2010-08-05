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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.actions.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.RerunType;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.php.api.phpmodule.PhpProgram;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpActionProvider;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO;
import org.netbeans.modules.php.project.ui.codecoverage.PhpCoverageProvider;
import org.netbeans.modules.php.project.ui.codecoverage.PhpUnitCoverageLogParser;
import org.netbeans.modules.php.project.ui.testrunner.UnitTestRunner;
import org.netbeans.modules.php.project.phpunit.PhpUnit;
import org.netbeans.modules.php.project.phpunit.PhpUnit.ConfigFiles;
import org.netbeans.modules.php.project.ui.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Action implementation for TEST configuration.
 * It means running and debugging tests.
 * @author Tomas Mysik
 */
class ConfigActionTest extends ConfigAction {
    static final ExecutionDescriptor.LineConvertorFactory PHPUNIT_LINE_CONVERTOR_FACTORY = new PhpUnitLineConvertorFactory();
    final PhpCoverageProvider coverageProvider;

    protected ConfigActionTest(PhpProject project) {
        super(project);
        coverageProvider = project.getLookup().lookup(PhpCoverageProvider.class);
        assert coverageProvider != null;
    }

    protected FileObject getTestDirectory(boolean showCustomizer) {
        return ProjectPropertiesSupport.getTestDirectory(project, showCustomizer);
    }

    protected boolean isCoverageEnabled() {
        return coverageProvider.isEnabled();
    }

    @Override
    public boolean isValid(boolean indexFileNeeded) {
        throw new IllegalStateException("Validation is not needed for tests");
    }

    @Override
    public boolean isDebugProjectEnabled() {
        throw new IllegalStateException("Debug project tests action is not supported");
    }

    @Override
    public boolean isRunFileEnabled(Lookup context) {
        FileObject file = CommandUtils.fileForContextOrSelectedNodes(context);
        return file != null && FileUtils.isPhpFile(file);
    }

    @Override
    public boolean isDebugFileEnabled(Lookup context) {
        if (XDebugStarterFactory.getInstance() == null) {
            return false;
        }
        return isRunFileEnabled(context);
    }

    @Override
    public void runProject() {
        // first, let user select test directory
        FileObject testDirectory = getTestDirectory(true);
        if (testDirectory == null) {
            return;
        }
        if (!isPhpUnitValid()) {
            return;
        }

        run(getPhpUnitInfo(null));
    }

    @Override
    public void debugProject() {
        throw new IllegalStateException("Debug project tests action is not supported");
    }

    @Override
    public void runFile(Lookup context) {
        if (!isPhpUnitValid()) {
            return;
        }
        run(getPhpUnitInfo(context));
    }

    @Override
    public void debugFile(Lookup context) {
        if (!isPhpUnitValid()) {
            return;
        }
        debug(getPhpUnitInfo(context));
    }

    private boolean isPhpUnitValid() {
        PhpUnit phpUnit = CommandUtils.getPhpUnit(false);
        return Utils.validatePhpUnitForProject(phpUnit, project);
    }

    void run(PhpUnitInfo info) {
        if (info == null) {
            return;
        }

        new RunScript(new RunScriptProvider(info)).run();
    }

    void debug(PhpUnitInfo info) {
        if (info == null) {
            return;
        }

        new DebugScript(new DebugScriptProvider(info)).run();
    }

    private PhpUnitInfo getPhpUnitInfo(Lookup context) {
        PhpUnit phpUnit = CommandUtils.getPhpUnit(true);
        if (phpUnit == null) {
            return null;
        }

        if (context == null) {
            // run project
            FileObject testDirectory = getTestDirectory(true);
            if (testDirectory == null) {
                return null;
            }
            return getProjectPhpUnitInfo(testDirectory);
        }

        return getFilePhpUnitInfo(context);
    }

    private PhpUnitInfo getProjectPhpUnitInfo(FileObject testDirectory) {
        return new PhpUnitInfo(testDirectory, testDirectory, null);
    }

    private PhpUnitInfo getFilePhpUnitInfo(Lookup context) {
        assert context != null;

        // #188770
        FileObject testDirectory = null;
        if (!ProjectPropertiesSupport.runAllTestFilesUsingPhpUnit(project)) {
            testDirectory = getTestDirectory(true);
            if (testDirectory == null) {
                return null;
            }
        }

        FileObject fileObj = null;
        if (testDirectory != null) {
            fileObj = CommandUtils.fileForContextOrSelectedNodes(context, testDirectory);
        } else {
            fileObj = CommandUtils.fileForContextOrSelectedNodes(context);
        }
        assert fileObj != null : "Fileobject not found for context: " + context + " and test directory: " + testDirectory;
        if (!fileObj.isValid()) {
            return null;
        }
        return new PhpUnitInfo(fileObj.getParent(), fileObj, fileObj.getName());
    }

    private static class PhpUnitInfo {
        public final FileObject workingDirectory;
        public final FileObject startFile;
        public final String testName;
        private final List<Testcase> customTests = Collections.synchronizedList(new ArrayList<Testcase>());

        public PhpUnitInfo(FileObject workingDirectory, FileObject startFile, String testName) {
            assert startFile != null;

            this.workingDirectory = workingDirectory;
            this.startFile = startFile;
            this.testName = testName;
        }

        public List<Testcase> getCustomTests() {
            return new ArrayList<Testcase>(customTests);
        }

        public void resetCustomTests() {
            customTests.clear();
        }

        public void setCustomTests(Collection<Testcase> tests) {
            resetCustomTests();
            customTests.addAll(tests);
        }
    }

    private class RunScriptProvider implements RunScript.Provider {
        protected final PhpUnitInfo info;
        protected final PhpUnit phpUnit;
        protected final UnitTestRunner testRunner;
        protected final RerunUnitTestHandler rerunUnitTestHandler;

        public RunScriptProvider(PhpUnitInfo info) {
            assert info != null;

            this.info = info;
            rerunUnitTestHandler = getRerunUnitTestHandler();
            testRunner = getTestRunner();
            phpUnit = CommandUtils.getPhpUnit(false);
            assert phpUnit != null;
        }

        protected boolean allTests(PhpUnitInfo info) {
            return info.testName == null;
        }

        @Override
        public ExecutionDescriptor getDescriptor() throws IOException {
            boolean phpUnitValid = PhpUnit.hasValidVersion(phpUnit);
            ExecutionDescriptor executionDescriptor = PhpProgram.getExecutionDescriptor()
                    .optionsPath(UiUtils.OPTIONS_PATH + "/" + PhpUnit.OPTIONS_SUB_PATH) // NOI18N
                    .frontWindow(!phpUnitValid)
                    .outConvertorFactory(PHPUNIT_LINE_CONVERTOR_FACTORY)
                    .inputVisible(false);
            if (phpUnitValid) {
                executionDescriptor = executionDescriptor
                        .preExecution(new Runnable() {
                            @Override
                            public void run() {
                                rerunUnitTestHandler.disable();
                                testRunner.start();
                            }
                        })
                        .postExecution(new Runnable() {
                            @Override
                            public void run() {
                                testRunner.showResults();
                                rerunUnitTestHandler.enable();
                                handleCodeCoverage();
                            }
                        });
            } else {
                executionDescriptor = executionDescriptor
                        .outProcessorFactory(new OutputProcessorFactory(phpUnit));
            }
            return executionDescriptor;
        }

        @Override
        public ExternalProcessBuilder getProcessBuilder() {
            File startFile = FileUtil.toFile(info.startFile);
            ConfigFiles configFiles = PhpUnit.getConfigFiles(project, allTests(info));

            ExternalProcessBuilder externalProcessBuilder = phpUnit.getProcessBuilder()
                    .workingDirectory(phpUnit.getWorkingDirectory(configFiles, FileUtil.toFile(info.workingDirectory)))
                    .addArgument(phpUnit.getXmlLogParam())
                    .addArgument(PhpUnit.XML_LOG.getAbsolutePath());

            if (configFiles.bootstrap != null) {
                externalProcessBuilder = externalProcessBuilder
                        .addArgument(PhpUnit.PARAM_BOOTSTRAP)
                        .addArgument(configFiles.bootstrap.getAbsolutePath());
            }
            if (configFiles.configuration != null) {
                externalProcessBuilder = externalProcessBuilder
                        .addArgument(PhpUnit.PARAM_CONFIGURATION)
                        .addArgument(configFiles.configuration.getAbsolutePath());
            }
            if (configFiles.suite != null) {
                startFile = configFiles.suite;
            }

            if (isCoverageEnabled()) {
                externalProcessBuilder = externalProcessBuilder
                        .addArgument(PhpUnit.PARAM_COVERAGE_LOG)
                        .addArgument(PhpUnit.COVERAGE_LOG.getAbsolutePath());
            }

            List<Testcase> customTests = info.getCustomTests();
            if (!customTests.isEmpty()) {
                StringBuilder buffer = new StringBuilder(200);
                boolean first = true;
                for (Testcase test : customTests) {
                    if (!first) {
                        buffer.append("|"); // NOI18N
                    }
                    buffer.append(test.getName());
                    first = false;
                }
                externalProcessBuilder = externalProcessBuilder
                        .addArgument(PhpUnit.PARAM_FILTER)
                        .addArgument(buffer.toString());
                info.resetCustomTests();
            }

            externalProcessBuilder = externalProcessBuilder
                    .addArgument(PhpUnit.SUITE_NAME)
                    .addArgument(PhpUnit.SUITE.getAbsolutePath())
                    .addArgument(String.format(PhpUnit.SUITE_RUN, startFile.getAbsolutePath()));
            return externalProcessBuilder;
        }

        @Override
        public String getOutputTabTitle() {
            String title = null;
            if (allTests(info)) {
                File suite = PhpUnit.getCustomSuite(project);
                if (suite == null) {
                    title = NbBundle.getMessage(ConfigActionTest.class, "LBL_UnitTestsForTestSourcesSuffix");
                } else {
                    title = NbBundle.getMessage(ConfigActionTest.class, "LBL_UnitTestsForTestSourcesWithCustomSuiteSuffix", suite.getName());
                }
            } else {
                title = info.testName;
            }
            return String.format("%s - %s", phpUnit.getProgram(), title);
        }

        @Override
        public boolean isValid() {
            return phpUnit.isValid() && info.startFile != null;
        }

        protected RerunUnitTestHandler getRerunUnitTestHandler() {
            return new RerunUnitTestHandler(info);
        }

        protected UnitTestRunner getTestRunner() {
            return new UnitTestRunner(project, TestSession.SessionType.TEST, rerunUnitTestHandler, allTests(info));
        }

        void handleCodeCoverage() {
            if (!isCoverageEnabled()) {
                return;
            }

            CoverageVO coverage = new CoverageVO();
            try {
                PhpUnitCoverageLogParser.parse(new BufferedReader(new FileReader(PhpUnit.COVERAGE_LOG)), coverage);
            } catch (FileNotFoundException ex) {
                LOGGER.info(String.format("File %s not found. If there are no errors in PHPUnit output (verify in Output window), "
                        + "please report an issue (http://www.netbeans.org/issues/).", PhpUnit.COVERAGE_LOG));
                return;
            }
            if (!PhpUnit.KEEP_LOGS) {
                PhpUnit.COVERAGE_LOG.delete();
            }
            if (allTests(info)) {
                coverageProvider.setCoverage(coverage);
            } else {
                coverageProvider.updateCoverage(coverage);
            }
        }
    }

    private final class DebugScriptProvider extends RunScriptProvider implements DebugScript.Provider {
        public DebugScriptProvider(PhpUnitInfo info) {
            super(info);
        }

        @Override
        public PhpProject getProject() {
            return project;
        }

        @Override
        public FileObject getStartFile() {
            return info.startFile;
        }

        @Override
        protected RerunUnitTestHandler getRerunUnitTestHandler() {
            return new RedebugUnitTestHandler(info);
        }

        @Override
        protected UnitTestRunner getTestRunner() {
            assert rerunUnitTestHandler instanceof RedebugUnitTestHandler;
            return new UnitTestRunner(project, TestSession.SessionType.DEBUG, rerunUnitTestHandler, allTests(info));
        }

        @Override
        public List<Pair<String, String>> getDebugPathMapping() {
            return Collections.emptyList();
        }

        @Override
        public Pair<String, Integer> getDebugProxy() {
            return null;
        }
    }

    private class RerunUnitTestHandler implements RerunHandler {
        protected final PhpUnitInfo info;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private volatile boolean enabled = false;

        public RerunUnitTestHandler(PhpUnitInfo info) {
            assert info != null;
            this.info = info;
        }

        @Override
        public final void rerun() {
            PhpActionProvider.submitTask(new Runnable() {
                @Override
                public void run() {
                    rerunInternal();
                }
            });
        }

        protected void rerunInternal() {
            ConfigActionTest.this.run(info);
        }

        @Override
        public void rerun(Set<Testcase> tests) {
            info.setCustomTests(tests);
            rerun();
        }

        @Override
        public boolean enabled(RerunType type) {
            boolean supportedType = false;
            switch (type) {
                case ALL:
                case CUSTOM:
                    supportedType = true;
                    break;
                default:
                    assert false : "Unknown RerunType: " + type;
                    break;
            }
            return supportedType && enabled;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        void enable() {
            if (!enabled) {
                enabled = true;
                changeSupport.fireChange();
            }
        }

        void disable() {
            if (enabled) {
                enabled = false;
                changeSupport.fireChange();
            }
        }
    }

    private class RedebugUnitTestHandler extends RerunUnitTestHandler {
        public RedebugUnitTestHandler(PhpUnitInfo info) {
            super(info);
        }

        @Override
        protected void rerunInternal() {
            ConfigActionTest.this.debug(info);
        }
    }

    static final class OutputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {
        private final PhpUnit phpUnit;

        public OutputProcessorFactory(PhpUnit phpUnit) {
            this.phpUnit = phpUnit;
        }

        @Override
        public InputProcessor newInputProcessor(final InputProcessor defaultProcessor) {
            return new InputProcessor() {
                @Override
                public void processInput(char[] chars) throws IOException {
                    defaultProcessor.processInput(chars);
                }
                @Override
                public void reset() throws IOException {
                    defaultProcessor.reset();
                }
                @Override
                public void close() throws IOException {
                    String msg = NbBundle.getMessage(ConfigActionTest.class, "MSG_OldPhpUnit", PhpUnit.getVersions(phpUnit));
                    char[] separator = new char[msg.length()];
                    Arrays.fill(separator, '='); // NOI18N
                    defaultProcessor.processInput("\n".toCharArray()); // NOI18N
                    defaultProcessor.processInput(separator);
                    defaultProcessor.processInput("\n".toCharArray()); // NOI18N
                    defaultProcessor.processInput(msg.toCharArray());
                    defaultProcessor.processInput("\n".toCharArray()); // NOI18N
                    defaultProcessor.processInput(separator);
                    defaultProcessor.processInput("\n".toCharArray()); // NOI18N
                    defaultProcessor.close();
                }
            };
        }
    }

    static final class PhpUnitLineConvertorFactory implements ExecutionDescriptor.LineConvertorFactory {
        @Override
        public LineConvertor newLineConvertor() {
            return LineConvertors.filePattern(null, PhpUnit.LINE_PATTERN, null, 1, 2);
        }

    }
}
