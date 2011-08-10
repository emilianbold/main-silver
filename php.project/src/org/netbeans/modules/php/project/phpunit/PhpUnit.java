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

package org.netbeans.modules.php.project.phpunit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.api.phpmodule.PhpProgram;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.api.PhpLanguageOptions.PhpVersion;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * PHP Unit 3.x support, the common part.
 * @author Tomas Mysik
 */
public abstract class PhpUnit extends PhpProgram {

    protected static final Logger LOGGER = Logger.getLogger(PhpUnit.class.getName());

    // for keeping log files to able to evaluate and fix issues
    public static final boolean KEEP_LOGS = Boolean.getBoolean("nb.php.phpunit.keeplogs"); // NOI18N
    // options
    public static final String OPTIONS_SUB_PATH = "PhpUnit"; // NOI18N
    // test files suffix
    public static final String TEST_CLASS_SUFFIX = "Test"; // NOI18N
    private static final String TEST_FILE_SUFFIX = TEST_CLASS_SUFFIX + ".php"; // NOI18N
    // suite files suffix
    private static final String SUITE_CLASS_SUFFIX = "Suite"; // NOI18N
    private static final String SUITE_FILE_SUFFIX = SUITE_CLASS_SUFFIX + ".php"; // NOI18N
    // create test
    private static final String REQUIRE_ONCE_TPL_START = "require_once '"; // NOI18N
    private static final String REQUIRE_ONCE_TPL_END = "%s';"; // NOI18N
    // cli options
    public static final String PARAM_VERSION = "--version"; // NOI18N
    public static final String PARAM_FILTER = "--filter"; // NOI18N
    public static final String PARAM_COVERAGE_LOG = "--coverage-clover"; // NOI18N
    public static final String PARAM_SKELETON = "--skeleton-test"; // NOI18N
    public static final String PARAM_LIST_GROUPS = "--list-groups"; // NOI18N
    public static final String PARAM_GROUP = "--group"; // NOI18N

    // for older PHP Unit versions
    public static final String PARAM_SKELETON_OLD = "--skeleton"; // NOI18N
    // bootstrap & config
    public static final String PARAM_BOOTSTRAP = "--bootstrap"; // NOI18N
    private static final String BOOTSTRAP_FILENAME = "bootstrap%s.php"; // NOI18N
    public static final String PARAM_CONFIGURATION = "--configuration"; // NOI18N
    private static final String CONFIGURATION_FILENAME = "configuration%s.xml"; // NOI18N

    // output files
    public static final File XML_LOG;
    public static final File COVERAGE_LOG;

    // suite file
    public static final File SUITE;
    public static final String SUITE_NAME = "NetBeansSuite"; // NOI18N
    public static final String SUITE_RUN = "run=%s"; // NOI18N
    private static final String SUITE_REL_PATH = "phpunit/" + SUITE_NAME + ".php"; // NOI18N

    // php props
    public static final char DIRECTORY_SEPARATOR = '/'; // NOI18N
    public static final String DIRNAME_FILE = ".dirname(__FILE__).'/"; // NOI18N
    public static final String REQUIRE_ONCE_REL_PART = "'" + DIRNAME_FILE; // NOI18N

    public static final Pattern LINE_PATTERN = Pattern.compile("(?:.+\\(\\) )?(.+):(\\d+)"); // NOI18N

    // unknown version
    static final int[] UNKNOWN_VERSION = new int[0];
    // minimum supported version
    static final int[] MINIMAL_VERSION = new int[] {3, 3, 0};
    static final int[] MINIMAL_VERSION_PHP53 = new int[] {3, 4, 0};

    /**
     * volatile is enough because:
     *  - never mind if the version is detected 2x
     *  - we don't change array values but only the array itself (local variable created and then assigned to 'version')
     */
    static volatile int[] version = null;

    static {
        SUITE = InstalledFileLocator.getDefault().locate(SUITE_REL_PATH, "org.netbeans.modules.php.project", false);  // NOI18N
        if (SUITE == null || !SUITE.isFile()) {
            throw new IllegalStateException("Could not locate file " + SUITE_REL_PATH);
        }
        // output files, see #200775
        String logDirName = System.getProperty("java.io.tmpdir"); // NOI18N
        String userLogDirName = System.getProperty("nb.php.phpunit.logdir"); // NOI18N
        if (userLogDirName != null) {
            LOGGER.log(Level.INFO, "Custom directory for PhpUnit logs provided: {0}", userLogDirName);
            File userLogDir = new File(userLogDirName);
            if (userLogDir.isDirectory() && userLogDir.canWrite()) {
                logDirName = userLogDirName;
            } else {
                LOGGER.log(Level.WARNING, "Directory for PhpUnit logs {0} is not writable directory", userLogDirName);
            }
        }
        LOGGER.log(Level.FINE, "Directory for PhpUnit logs: {0}", logDirName);
        XML_LOG = new File(logDirName, "nb-phpunit-log.xml"); // NOI18N
        COVERAGE_LOG = new File(logDirName, "nb-phpunit-coverage.xml"); // NOI18N
     }

    PhpUnit(String command) {
        super(command);
    }

    public static PhpUnit getDefault() throws InvalidPhpProgramException {
        String command = PhpOptions.getInstance().getPhpUnit();
        String error = validate(command);
        if (error != null) {
            throw new InvalidPhpProgramException(error);
        }
        // a bit ugly :/
        if (hasValidVersion(new PhpUnitCustom(command))
                && version[0] >= MINIMAL_VERSION_PHP53[0]
                && version[1] >= MINIMAL_VERSION_PHP53[1]) {
            return new PhpUnitImpl(command);
        }
        return new PhpUnit33(command);
    }

    public static PhpUnit getCustom(String command) throws InvalidPhpProgramException {
        String error = validate(command);
        if (error != null) {
            throw new InvalidPhpProgramException(error);
        }
        return new PhpUnitCustom(command);
    }

    public abstract String getXmlLogParam();

    public static boolean isRequireOnceSourceFile(String line, String filename) {
        return line.startsWith(REQUIRE_ONCE_TPL_START)
                && line.endsWith(String.format(REQUIRE_ONCE_TPL_END, filename));
    }

    public static boolean isTestFile(String fileName) {
        return !fileName.equals(PhpUnit.TEST_FILE_SUFFIX) && fileName.endsWith(PhpUnit.TEST_FILE_SUFFIX);
    }

    public static boolean isTestClass(String className) {
        return !className.equals(PhpUnit.TEST_CLASS_SUFFIX) && className.endsWith(PhpUnit.TEST_CLASS_SUFFIX);
    }

    public static boolean isSuiteFile(String fileName) {
        return !fileName.equals(PhpUnit.SUITE_FILE_SUFFIX) && fileName.endsWith(PhpUnit.SUITE_FILE_SUFFIX);
    }

    public static boolean isSuiteClass(String className) {
        return !className.equals(PhpUnit.SUITE_CLASS_SUFFIX) && className.endsWith(PhpUnit.SUITE_CLASS_SUFFIX);
    }

    public static boolean isTestOrSuiteFile(String fileName) {
        return isTestFile(fileName) || isSuiteFile(fileName);
    }

    public static boolean isTestOrSuiteClass(String className) {
        return isTestClass(className) || isSuiteClass(className);
    }

    public static String getTestedClass(String testOrSuiteClass) {
        assert isTestOrSuiteClass(testOrSuiteClass) : "Not Test or Suite class: " + testOrSuiteClass;
        int lastIndexOf = -1;
        if (isTestClass(testOrSuiteClass)) {
            lastIndexOf = testOrSuiteClass.lastIndexOf(PhpUnit.TEST_CLASS_SUFFIX);
        } else if (isSuiteClass(testOrSuiteClass)) {
            lastIndexOf = testOrSuiteClass.lastIndexOf(PhpUnit.SUITE_CLASS_SUFFIX);
        }
        assert lastIndexOf != -1;
        return testOrSuiteClass.substring(0, lastIndexOf);
    }

    public static String makeTestFile(String testedFileName) {
        return testedFileName + PhpUnit.TEST_FILE_SUFFIX;
    }

    public static String makeTestClass(String testedClass) {
        return testedClass + PhpUnit.TEST_CLASS_SUFFIX;
    }

    public static String makeSuiteFile(String testedFileName) {
        return testedFileName + PhpUnit.SUITE_FILE_SUFFIX;
    }

    public static String makeSuiteClass(String testedClass) {
        return testedClass + PhpUnit.SUITE_CLASS_SUFFIX;
    }

    @Override
    public ExternalProcessBuilder getProcessBuilder() {
        return super.getProcessBuilder()
                .workingDirectory(new File(getProgram()).getParentFile());
    }

    // #170120
    public File getWorkingDirectory(ConfigFiles configFiles, File defaultWorkingDirectory) {
        if (configFiles.configuration != null) {
            return configFiles.configuration.getParentFile();
        }
        return defaultWorkingDirectory;
    }

    // XXX see 2nd paragraph
    /**
     * The minimum version of PHPUnit is <b>3.3.0</b> because:
     * - of XML log format changes (used for parsing of test results)
     * - running project action Test (older versions don't support directory as a parameter to run)
     * <p>
     * Since issue #167519 is fixed, this is not necessary true any more:
     * - test listener could be used instead of XML file (this would be more reliable and XML file independent)
     * - all tests are run using suite file, so no need to support directory as a parameter
     * @return <code>null</code> if invalid or not the minimal version of PHPUnit found, an error message otherwise
     */
    public static String validateVersion(PhpUnit phpUnit) {
        if (phpUnit == null) {
            return NbBundle.getMessage(PhpUnit.class, "MSG_NoPhpUnit");
        }
        String error = phpUnit.validate();
        if (error == null) {
            phpUnit.getVersion();
            if (version == null
                    || version == UNKNOWN_VERSION
                    || (version[0] <= MINIMAL_VERSION[0] && version[1] < MINIMAL_VERSION[1])) {
                error = NbBundle.getMessage(PhpUnit.class, "MSG_OldPhpUnit", PhpUnit.getVersions(phpUnit));
            }
        }
        return error;
    }

    /**
     * Check whether the PHPUnit is valid for the given project
     * (currently, this is false for PHP 5.3 project and PHPUnit 3.3.x).
     */
    public static String validateVersion(PhpUnit phpUnit, PhpProject project) {
        String error = validateVersion(phpUnit);
        if (error != null) {
            return error;
        }
        PhpVersion phpVersion = ProjectPropertiesSupport.getPhpVersion(project);
        switch (phpVersion) {
            case PHP_53:
                if (version[0] <= MINIMAL_VERSION_PHP53[0]
                        && version[1] < MINIMAL_VERSION_PHP53[1]) {
                    // this instanceof PhpUnitImpl; - would not work with PhpUnit35 etc.
                    error = NbBundle.getMessage(PhpUnit.class, "MSG_OldPhpUnitPhp53", PhpUnit.getVersions(phpUnit, project));
                }
                break;
            case PHP_5:
                // noop
                break;
            default:
                throw new IllegalStateException("Unknown PHP version: " + phpVersion);
        }
        return error;
    }

    public static boolean hasValidVersion(PhpUnit phpUnit) {
        return validateVersion(phpUnit) == null;
    }

    public static boolean hasValidVersion(PhpUnit phpUnit, PhpProject project) {
        return validateVersion(phpUnit, project) == null;
    }

    public static void resetVersion() {
        version = null;
    }

    /**
     * Get the version of PHPUnit in the form of [major][minor][revision].
     * @return
     */
    private int[] getVersion() {
        if (!isValid()) {
            return UNKNOWN_VERSION;
        }
        if (version != null) {
            return version;
        }

        version = UNKNOWN_VERSION;
        ExternalProcessBuilder externalProcessBuilder = getProcessBuilder()
                .addArgument(PARAM_VERSION);
        ExecutionDescriptor executionDescriptor = new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL)
                .outProcessorFactory(new OutputProcessorFactory());
        String message = NbBundle.getMessage(PhpUnit.class, "LBL_ValidatingPhpUnit");
        execute(externalProcessBuilder, executionDescriptor, message, message);

        return version;
    }

    /**
     * Get an array with actual and minimal PHPUnit versions.
     * <p>
     * Return three times "?" if the actual version is not known or <code>null</code>.
     */
    public static String[] getVersions(PhpUnit phpUnit) {
        return getVersions(phpUnit, MINIMAL_VERSION);
    }

    /**
     * Get an array with actual and minimal PHPUnit versions for the given project.
     * <p>
     * Return three times "?" if the actual version is not known or <code>null</code>.
     */
    public static String[] getVersions(PhpUnit phpUnit, PhpProject project) {
        int[] minimalVersion = null;
        PhpVersion phpVersion = ProjectPropertiesSupport.getPhpVersion(project);
        switch (phpVersion) {
            case PHP_53:
                minimalVersion = MINIMAL_VERSION_PHP53;
                break;
            case PHP_5:
                minimalVersion = MINIMAL_VERSION;
                break;
            default:
                throw new IllegalStateException("Unknown PHP version: " + phpVersion);
        }
        return getVersions(phpUnit, minimalVersion);
    }

    private static String[] getVersions(PhpUnit phpUnit, int[] minimalVersion) {
        List<String> params = new ArrayList<String>(6);
        if (phpUnit == null || phpUnit.getVersion() == UNKNOWN_VERSION) {
            String questionMark = NbBundle.getMessage(PhpUnit.class, "LBL_QuestionMark");
            params.add(questionMark); params.add(questionMark); params.add(questionMark);
        } else {
            for (Integer i : phpUnit.getVersion()) {
                params.add(String.valueOf(i));
            }
        }
        for (Integer i : minimalVersion) {
            params.add(String.valueOf(i));
        }
        return params.toArray(new String[params.size()]);
    }

    public static ConfigFiles getConfigFiles(PhpProject project, boolean withSuite) {
        List<Pair<String, File>> missingFiles = new LinkedList<Pair<String, File>>();
        File bootstrap = ProjectPropertiesSupport.getPhpUnitBootstrap(project);
        if (bootstrap != null
                && !bootstrap.isFile()) {
            missingFiles.add(Pair.of(NbBundle.getMessage(PhpUnit.class, "LBL_Bootstrap"), bootstrap));
            bootstrap = null;
        }

        File configuration = ProjectPropertiesSupport.getPhpUnitConfiguration(project);
        if (configuration != null
                && !configuration.isFile()) {
            missingFiles.add(Pair.of(NbBundle.getMessage(PhpUnit.class, "LBL_XmlConfiguration"), configuration));
            configuration = null;
        }

        File suite = null;
        if (withSuite) {
            suite = ProjectPropertiesSupport.getPhpUnitSuite(project);
            if (suite != null
                    && !suite.isFile()) {
                missingFiles.add(Pair.of(NbBundle.getMessage(PhpUnit.class, "LBL_TestSuite"), suite));
                suite = null;
            }
        }
        warnAboutMissingFiles(missingFiles);
        return new ConfigFiles(bootstrap, ProjectPropertiesSupport.usePhpUnitBootstrapForCreateTests(project), configuration, suite);
    }

    public static File getCustomSuite(PhpProject project) {
        File suite = ProjectPropertiesSupport.getPhpUnitSuite(project);
        if (suite != null
                && suite.isFile()) {
            return suite;
        }
        return null;
    }

    public static File createBootstrapFile(final PhpProject project) {
        FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(project, false);
        assert testDirectory != null : "Test directory must already be set";

        final FileObject configFile = FileUtil.getConfigFile("Templates/PHPUnit/PHPUnitBootstrap"); // NOI18N
        final DataFolder dataFolder = DataFolder.findFolder(testDirectory);
        final File bootstrapFile = new File(getBootstrapFilepath(project));
        final File[] files = new File[1];
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                try {
                    DataObject dataTemplate = DataObject.find(configFile);
                    DataObject bootstrap = dataTemplate.createFromTemplate(dataFolder, bootstrapFile.getName() + "~"); // NOI18N
                    assert bootstrap != null;
                    moveAndAdjustBootstrap(project, FileUtil.toFile(bootstrap.getPrimaryFile()), bootstrapFile);
                    assert bootstrapFile.isFile();
                    files[0] = bootstrapFile;
                    informAboutGeneratedFile(bootstrapFile.getName());
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Cannot create PHPUnit bootstrap file", ex);
                }
            }
        });
        if (files[0] == null) {
            // no file generated
            warnAboutNotGeneratedFile(bootstrapFile.getName());
        }
        return files[0];
    }

    private static void moveAndAdjustBootstrap(PhpProject project, File tmpBootstrap, File finalBootstrap) {
        try {
            // input
            BufferedReader in = new BufferedReader(new FileReader(tmpBootstrap));
            try {
                // output
                BufferedWriter out = new BufferedWriter(new FileWriter(finalBootstrap));
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.contains("%INCLUDE_PATH%")) { // NOI18N
                            if (line.startsWith("//")) { // NOI18N
                                // comment about %INCLUDE_PATH%, let's skip it
                                continue;
                            }
                            line = processIncludePath(
                                    finalBootstrap,
                                    line,
                                    ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(PhpProjectProperties.INCLUDE_PATH),
                                    FileUtil.toFile(project.getProjectDirectory()));
                        }
                        out.write(line);
                        out.newLine();
                    }
                } finally {
                    out.flush();
                    out.close();
                }
            } finally {
                in.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }

        if (!tmpBootstrap.delete()) {
            LOGGER.log(Level.INFO, "Cannot delete temporary file {0}", tmpBootstrap);
            tmpBootstrap.deleteOnExit();
        }

        FileUtil.refreshFor(finalBootstrap.getParentFile());
    }

    static String processIncludePath(File bootstrap, String line, String includePath, File projectDir) {
        if (StringUtils.hasText(includePath)) {
            if (includePath.startsWith(":")) { // NOI18N
                includePath = includePath.substring(1);
            }
            StringBuilder buffer = new StringBuilder(200);
            for (String path : PropertyUtils.tokenizePath(includePath)) {
                File reference = PropertyUtils.resolveFile(projectDir, path);
                buffer.append(".PATH_SEPARATOR"); // NOI18N
                buffer.append(getDirnameFile(bootstrap, reference));
            }
            includePath = buffer.toString();
        } else {
            // comment out the line
            line = "//" + line; // NOI18N
        }
        line = line.replace("%INCLUDE_PATH%", includePath); // NOI18N
        return line;
    }

    public static File createConfigurationFile(PhpProject project) {
        FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(project, false);
        assert testDirectory != null : "Test directory must already be set";

        final FileObject configFile = FileUtil.getConfigFile("Templates/PHPUnit/PHPUnitConfiguration.xml"); // NOI18N
        final DataFolder dataFolder = DataFolder.findFolder(testDirectory);
        final File configurationFile = new File(getConfigurationFilepath(project));
        File file = null;
        try {
            DataObject dataTemplate = DataObject.find(configFile);
            DataObject configuration = dataTemplate.createFromTemplate(dataFolder, configurationFile.getName().replace(".xml", "")); // NOI18N
            assert configuration != null;
            file = configurationFile;
            informAboutGeneratedFile(configurationFile.getName());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Cannot create PHPUnit configuration file", ex);
        }
        if (file == null) {
            // no file generated
            warnAboutNotGeneratedFile(configurationFile.getName());
        }
        return file;
    }

    public static void informAboutGeneratedFile(String generatedFile) {
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(NbBundle.getMessage(PhpUnit.class, "MSG_FileGenerated", generatedFile)));
    }

    private static void warnAboutNotGeneratedFile(String file) {
        NotifyDescriptor warning = new NotifyDescriptor.Message(
                NbBundle.getMessage(PhpUnit.class, "MSG_NotGenerated", file),
                NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(warning);
    }

    private static void warnAboutMissingFiles(List<Pair<String, File>> missingFiles) {
        if (missingFiles.isEmpty()) {
            return;
        }
        StringBuilder buffer = new StringBuilder(100);
        for (Pair<String, File> pair : missingFiles) {
            buffer.append(NbBundle.getMessage(PhpUnit.class, "LBL_MissingFile", pair.first, pair.second.getAbsolutePath()));
            buffer.append("\n"); // NOI18N
        }
        NotifyDescriptor warning = new NotifyDescriptor.Message(
                NbBundle.getMessage(PhpUnit.class, "MSG_MissingFiles", buffer.toString()),
                NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(warning);
    }

    private static String getDirnameFile(File testFile, File sourceFile) {
        return getRelPath(testFile, sourceFile, ".'", DIRNAME_FILE, "'"); // NOI18N
    }

    public static String getRequireOnce(File testFile, File sourceFile) {
        return getRelPath(testFile, sourceFile, "", REQUIRE_ONCE_REL_PART, ""); // NOI18N
    }

    // XXX improve this and related method
    private static String getRelPath(File testFile, File sourceFile, String absolutePrefix, String relativePrefix, String suffix) {
        return getRelPath(testFile, sourceFile, absolutePrefix, relativePrefix, suffix, false);
    }

    // forceAbsolute only for unit tests
    static String getRelPath(File testFile, File sourceFile, String absolutePrefix, String relativePrefix, String suffix, boolean forceAbsolute) {
        File parentFile = testFile.getParentFile();
        String relPath = PropertyUtils.relativizeFile(parentFile, sourceFile);
        if (relPath == null || forceAbsolute) {
            // cannot be versioned...
            relPath = absolutePrefix + sourceFile.getAbsolutePath() + suffix;
        } else {
            relPath = relativePrefix + relPath + suffix;
        }
        return relPath.replace(File.separatorChar, DIRECTORY_SEPARATOR);
    }

    private static String getBootstrapFilepath(PhpProject project) {
        return getFilepath(project, BOOTSTRAP_FILENAME);
    }

    private static String getConfigurationFilepath(PhpProject project) {
        return getFilepath(project, CONFIGURATION_FILENAME);
    }

    private static String getFilepath(PhpProject project, String filename) {
        FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(project, false);
        assert testDirectory != null : "Test directory must already be set";

        File tests = FileUtil.toFile(testDirectory);
        File file = null;
        int i = 0;
        do {
            file = new File(tests, getFilename(filename, i++));
        } while (file.isFile());
        assert !file.isFile();
        return file.getAbsolutePath();
    }

    private static String getFilename(String filename, int i) {
        return String.format(filename, i == 0 ? "" : i); // NOI18N
    }

    @Override
    public String validate() {
        if (!StringUtils.hasText(getProgram())) {
            return NbBundle.getMessage(PhpUnit.class, "MSG_NoPhpUnit");
        }

        File file = new File(getProgram());
        if (!file.isAbsolute()) {
            return NbBundle.getMessage(PhpUnit.class, "MSG_PhpUnitNotAbsolutePath");
        }
        if (!file.isFile()) {
            return NbBundle.getMessage(PhpUnit.class, "MSG_PhpUnitNotFile");
        }
        if (!file.canRead()) {
            return NbBundle.getMessage(PhpUnit.class, "MSG_PhpUnitCannotRead");
        }
        return null;
    }

    public static String validate(String command) {
        return new PhpUnitCustom(command).validate();
    }

    public static final class ConfigFiles {
        public final File bootstrap;
        public final boolean useBootstrapForCreateTests;
        public final File configuration;
        public final File suite;

        public ConfigFiles(File bootstrap, boolean useBootstrapForCreateTests, File configuration, File suite) {
            this.bootstrap = bootstrap;
            this.useBootstrapForCreateTests = useBootstrapForCreateTests;
            this.configuration = configuration;
            this.suite = suite;
        }
    }

    static final class OutputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {
        //                                                              PHPUnit 3.3.1 by Sebastian Bergmann.
        private static final Pattern PHPUNIT_VERSION = Pattern.compile("PHPUnit\\s+(\\d+)\\.(\\d+)\\.(\\d+)\\w*\\s+"); // NOI18N

        @Override
        public InputProcessor newInputProcessor(final InputProcessor defaultProcessor) {
            return InputProcessors.bridge(new LineProcessor() {
                @Override
                public void processLine(String line) {
                    int[] match = match(line);
                    if (match != null) {
                        version = match;
                    }
                }
                @Override
                public void reset() {
                    try {
                        defaultProcessor.reset();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                @Override
                public void close() {
                    try {
                        defaultProcessor.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }

        static int[] match(String text) {
            assert text != null;
            if (StringUtils.hasText(text)) {
                Matcher matcher = PHPUNIT_VERSION.matcher(text);
                if (matcher.find()) {
                    int major = Integer.parseInt(matcher.group(1));
                    int minor = Integer.parseInt(matcher.group(2));
                    int release = Integer.parseInt(matcher.group(3));
                    return new int[] {major, minor, release};
                }
            }
            return null;
        }
    }
}
