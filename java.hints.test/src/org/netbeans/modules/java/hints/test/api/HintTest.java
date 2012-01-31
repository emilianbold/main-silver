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
package org.netbeans.modules.java.hints.test.api;

import com.sun.source.util.TreePath;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.tools.Diagnostic;
import junit.framework.Assert;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.hints.providers.code.CodeHintProviderImpl;
import org.netbeans.modules.java.hints.providers.code.FSWrapper;
import org.netbeans.modules.java.hints.providers.code.FSWrapper.ClassWrapper;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.java.hints.test.SourceUtilsTestUtil;
import org.netbeans.modules.java.hints.test.TestLookup;
import org.netbeans.modules.java.hints.test.TestUtilities;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.MimeTypes;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**A support class for writing a test for a Java Hint. A test verifying that correct
 * warnings are produced should look like:
 * <pre>
 * HintTest.create()
 *         .input("&lt;input Java source code>")
 *         .run(&lt;class containg the hint>)
 *         .assertWarnings("&lt;required warning(s)>");
 * </pre>
 *
 * Note: when verifying that no warnings are produced in a particular situation,
 * do not pass any warnings to the {@code assertWarnings} method.
 *
 * A test verifying that a hint's transformation is correct:
 * <pre>
 * HintTest.create()
 *         .input("&lt;input Java source code>")
 *         .run(&lt;class containg the hint>)
 *         .findWarning("&lt;a warning produce by the hint>")
 *         .applyFix() //fill apply the only fix in the given ErrorDescription
 *         .assertCompilable()
 *         .assertOutput("&lt;output Java source code>");
 * </pre>
 *
 * All the tests run under the {@code test} branding, which allows to specify test values
 * for bundle keys for warning and fix in {@code Bundle_test.properties}, to isolate the
 * test from changes in the production {@code Bundle.properties}.
 *
 * @author lahvac
 */
public class HintTest {

    private final File workDir;
    private final FileObject sourceRoot;
    private final FileObject buildRoot;
    private final FileObject cache;
    private final Preferences testPreferences;
    private final List<FileObject> checkCompilable = new ArrayList<FileObject>();
    private String sourceLevel = "1.5";
    private Character caretMarker;
    private FileObject testFile;
    private FileObject[] extraClassPath = new FileObject[0];

    private HintTest() throws Exception {
        List<URL> layers = new LinkedList<URL>();

        for (String layer : new String[] {"META-INF/generated-layer.xml"}) {
            boolean found = false;

            for (Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources(layer); en.hasMoreElements(); ) {
                found = true;
                layers.add(en.nextElement());
            }

            Assert.assertTrue(layer, found);
        }

        XMLFileSystem xmlFS = new XMLFileSystem();
        xmlFS.setXmlUrls(layers.toArray(new URL[0]));

        FileSystem system = new MultiFileSystem(new FileSystem[] {FileUtil.createMemoryFileSystem(), xmlFS});

        Repository repository = new Repository(system);

        assertEquals(Lookup.getDefault().getClass().getCanonicalName(), TestLookup.class, Lookup.getDefault().getClass());

        ((TestLookup) Lookup.getDefault()).setLookupsImpl(
            Lookups.fixed(repository,
                          new TestProxyClassPathProvider(),
                          new TestSourceForBinaryQuery(),
                          new TestSourceLevelQueryImplementation(),
                          JavaDataLoader.findObject(JavaDataLoader.class, true)),
            Lookups.metaInfServices(HintTest.class.getClassLoader()),
            Lookups.singleton(HintTest.class.getClassLoader())
        );

        Set<String> amt = MimeTypes.getAllMimeTypes();
        if (amt == null) {
            amt = new HashSet<String>();
        } else {
            amt = new HashSet<String>(amt);
        }
        amt.add("text/x-java");
        MimeTypes.setAllMimeTypes(amt);
        org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();

        TreeLoader.DISABLE_CONFINEMENT_TEST = true;
        testPreferences = new TempPreferences();
        HintsSettings.setPreferencesOverride(new Map<String, Preferences>() {
            @Override public int size() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override public boolean isEmpty() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override public boolean containsKey(Object key) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override public boolean containsValue(Object value) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override public Preferences get(Object key) {
                return testPreferences;
            }
            @Override public Preferences put(String key, Preferences value) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override public Preferences remove(Object key) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override public void putAll(Map<? extends String, ? extends Preferences> m) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override public void clear() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override public Set<String> keySet() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override public Collection<Preferences> values() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override public Set<Entry<String, Preferences>> entrySet() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        workDir = File.createTempFile("HinTest", "test");
        workDir.delete();
        workDir.mkdirs();

        FileUtil.refreshFor(workDir);

        FileObject wd = FileUtil.toFileObject(workDir);
        
        assertNotNull(wd);

        sourceRoot = FileUtil.createFolder(wd, "src");
        buildRoot = FileUtil.createFolder(wd, "build");
        cache = FileUtil.createFolder(wd, "cache");

        CacheFolder.setCacheFolder(cache);

        NbBundle.setBranding("test");
    }

    /**Bootstraps the test framework.
     *
     * @return the test framework - call more methods on it to set-up a test, then call {@code run} method and assert results.
     */
    public static HintTest create() throws Exception {
        return new HintTest();
    }

    /**A character to use as a marker of a caret in the input code. The caret position
     * during the run method will be set to the position of this character in the first input file.
     *
     * @param c a caret marker
     * @return itself
     */
    public HintTest setCaretMarker(char c) {
        this.caretMarker = c;
        return this;
    }

    /**Create a test file. Equivalent to calling {@code input("test/Test.java", code, true)}.
     *
     * @param code the content of the newly created test file
     * @return itself
     */
    public HintTest input(String code) throws Exception {
        return input("test/Test.java", code, true);
    }

    /**Create a test file. Equivalent to calling {@code input("test/Test.java", code, compilable)}.
     *
     * @param code the content of the newly created test file
     * @param compilable if true, it will be verified that the file does not contain
     *                   compilation errors before the hint is run on it
     * @return itself
     */
    public HintTest input(String code, boolean compilable) throws Exception {
        return input("test/Test.java", code, compilable);
    }

    /**Create a test file. Equivalent to calling {@code input(fileName, code, true)}.
     *
     * @param fileName a relative file name of the newly created file from a (automatically created) source root
     * @param code the content of the newly created test file
     * @return itself
     */
    public HintTest input(String fileName, String code) throws Exception {
        return input(fileName, code, true);
    }

    /**Create a test file. Any number of files can be created for one test, but the hint
     * will be run only on the first one.
     *
     * @param fileName a relative file name of the newly created file from a (automatically created) source root
     * @param code the content of the newly created test file
     * @param compilable if true, it will be verified that the file does not contain
     *                   compilation errors before the hint is run on it
     * @return itself
     */
    public HintTest input(String fileName, String code, boolean compilable) throws Exception {
        int caret = -1;

        if (caretMarker != null) {
            caret = code.indexOf(caretMarker);

            assertNotSame(-1, caret);

            code = code.substring(0, caret) + code.substring(caret + 1);
        }

        FileObject file = FileUtil.createData(sourceRoot, fileName);

        TestUtilities.copyStringToFile(file, code);

        if (compilable) {
            checkCompilable.add(file);
        }

        if (testFile == null) {
            testFile = file;
            Method m = CaretAwareJavaSourceTaskFactory.class.getDeclaredMethod("setLastPosition", FileObject.class, int.class);

            m.setAccessible(true);
            m.invoke(null, testFile, caret);
        }
        
        return this;
    }

    private void ensureCompilable(FileObject file) throws IOException, AssertionError, IllegalArgumentException {
        CompilationInfo info = parse(file);

        assertNotNull(info);

        for (Diagnostic d : info.getDiagnostics()) {
            if (d.getKind() == Diagnostic.Kind.ERROR)
                throw new AssertionError(d.getMessage(null));
        }
    }

    /**Sets a source level for all Java files used in this test.
     *
     * @param sourceLevel the source level to use while parsing Java files
     * @return itself
     */
    public HintTest sourceLevel(String sourceLevel) {
        this.sourceLevel = sourceLevel;
        return this;
    }

    /**Sets a preference that will be visible to the hint.
     *
     * @param preferencesKey a key for the preferences
     * @param value the value to set
     * @return itself
     */
    public HintTest preference(String preferencesKey, String value) {
        this.testPreferences.put(preferencesKey, value);
        return this;
    }

    /**Sets a preference that will be visible to the hint.
     *
     * @param preferencesKey a key for the preferences
     * @param value the value to set
     * @return itself
     */
    public HintTest preference(String preferencesKey, boolean value) {
        this.testPreferences.putBoolean(preferencesKey, value);
        return this;
    }

    /**Runs the given hint(s) on the first file written by a {@code input} method.
     *
     * @param hint all hints in this class will be run on the file
     * @return a wrapper over the hint output that allows verifying results of the hint
     */
    public HintOutput run(Class<?> hint) throws Exception {
        for (FileObject file : checkCompilable) {
            ensureCompilable(file);
        }
        
        Map<HintMetadata, Collection<HintDescription>> hints = new HashMap<HintMetadata, Collection<HintDescription>>();
        List<ClassWrapper> found = new ArrayList<ClassWrapper>();

        for (ClassWrapper w : FSWrapper.listClasses()) {
            if (hint.getCanonicalName().equals(w.getName())) {
                found.add(w);
            }
        }

        assertFalse(found.isEmpty());

        for (ClassWrapper w : found) {
            CodeHintProviderImpl.processClass(w, hints);
        }

        List<HintDescription> total = new LinkedList<HintDescription>();

        for (Collection<? extends HintDescription> l : hints.values()) {
            total.addAll(l);
        }
        
        CompilationInfo info = parse(testFile);

        assertNotNull(info);

        List<ErrorDescription> result = new ArrayList<ErrorDescription>();

        for (Entry<HintDescription, List<ErrorDescription>> e : computeErrors(info, total, new AtomicBoolean()).entrySet()) {
            result.addAll(e.getValue());
        }

        return new HintOutput(result);
    }

    private CompilationInfo parse(FileObject file) throws DataObjectNotFoundException, IllegalArgumentException, IOException {
        DataObject od = DataObject.find(file);
        EditorCookie ec = od.getLookup().lookup(EditorCookie.class);

        assertNotNull(ec);

        Document doc = ec.openDocument();

        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");

        JavaSource js = JavaSource.forFileObject(file);

        assertNotNull(js);

        final DeadlockTask bt = new DeadlockTask(Phase.RESOLVED);

        js.runUserActionTask(bt, true);
        
        return bt.info;
    }

    private static Map<HintDescription, List<ErrorDescription>> computeErrors(CompilationInfo info, Iterable<? extends HintDescription> hints, AtomicBoolean cancel) {
        return new HintsInvoker(info, cancel).computeHints(info, new TreePath(info.getCompilationUnit()), hints, new LinkedList<MessageImpl>());
    }

    private static class TempPreferences extends AbstractPreferences {

        /*private*/Properties properties;

        private TempPreferences() {
            super(null, "");
        }

        private  TempPreferences(TempPreferences parent, String name)  {
            super(parent, name);
            newNode = true;
        }

        protected final String getSpi(String key) {
            return properties().getProperty(key);
        }

        protected final String[] childrenNamesSpi() throws BackingStoreException {
            return new String[0];
        }

        protected final String[] keysSpi() throws BackingStoreException {
            return properties().keySet().toArray(new String[0]);
        }

        protected final void putSpi(String key, String value) {
            properties().put(key,value);
        }

        protected final void removeSpi(String key) {
            properties().remove(key);
        }

        protected final void removeNodeSpi() throws BackingStoreException {}
        protected  void flushSpi() throws BackingStoreException {}
        protected void syncSpi() throws BackingStoreException {
            properties().clear();
        }

        @Override
        public void put(String key, String value) {
            try {
                super.put(key, value);
            } catch (IllegalArgumentException iae) {
                if (iae.getMessage().contains("too long")) {
                    // Not for us!
                    putSpi(key, value);
                } else {
                    throw iae;
                }
            }
        }

        Properties properties()  {
            if (properties == null) {
                properties = new Properties();
            }
            return properties;
        }

        protected AbstractPreferences childSpi(String name) {
            return new TempPreferences(this, name);
        }
    }

    private class TestSourceForBinaryQuery implements SourceForBinaryQueryImplementation {

        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            FileObject f = URLMapper.findFileObject(binaryRoot);

            if (buildRoot.equals(f)) {
                return new SourceForBinaryQuery.Result() {
                    public FileObject[] getRoots() {
                        return new FileObject[] {
                            sourceRoot,
                        };
                    }

                    public void addChangeListener(ChangeListener l) {
                    }

                    public void removeChangeListener(ChangeListener l) {
                    }
                };
            }

            return null;
        }

    }

    private static List<URL> bootClassPath;

    private static Logger log = Logger.getLogger(SourceUtilsTestUtil.class.getName());

    private static synchronized List<URL> getBootClassPath() {
        if (bootClassPath == null) {
            try {
                String cp = System.getProperty("sun.boot.class.path");
                List<URL> urls = new ArrayList<URL>();
                String[] paths = cp.split(Pattern.quote(System.getProperty("path.separator")));

                for (String path : paths) {
                    File f = new File(path);

                    if (!f.canRead())
                        continue;

                    FileObject fo = FileUtil.toFileObject(f);

                    if (FileUtil.isArchiveFile(fo)) {
                        fo = FileUtil.getArchiveRoot(fo);
                    }

                    if (fo != null) {
                        urls.add(fo.getURL());
                    }
                }

                bootClassPath = urls;
            } catch (FileStateInvalidException e) {
                if (log.isLoggable(Level.SEVERE))
                    log.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        return bootClassPath;
    }
    
    private class TestProxyClassPathProvider implements ClassPathProvider {

        public ClassPath findClassPath(FileObject file, String type) {
            try {
            if (ClassPath.BOOT == type) {
                return ClassPathSupport.createClassPath(getBootClassPath().toArray(new URL[0]));
            }

            if (ClassPath.SOURCE == type) {
                return ClassPathSupport.createClassPath(new FileObject[] {
                    sourceRoot
                });
            }

            if (ClassPath.COMPILE == type) {
                return ClassPathSupport.createClassPath(extraClassPath);
            }

            if (ClassPath.EXECUTE == type) {
                return ClassPathSupport.createClassPath(new FileObject[] {
                    buildRoot
                });
            }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private class TestSourceLevelQueryImplementation implements SourceLevelQueryImplementation {

        public String getSourceLevel(FileObject javaFile) {
            return sourceLevel;
        }

    }


    private static class DeadlockTask implements Task<CompilationController> {

        private final Phase phase;
        private CompilationInfo info;

        public DeadlockTask(Phase phase) {
            assert phase != null;
            this.phase = phase;
        }

        public void run( CompilationController info ) {
            try {
                info.toPhase(this.phase);
                this.info = info;
            } catch (IOException ioe) {
                if (log.isLoggable(Level.SEVERE))
                    log.log(Level.SEVERE, ioe.getMessage(), ioe);
            }
        }

    }

    /**Encapsulated the output of the hint.
     */
    public final class HintOutput {
        
        private final List<ErrorDescription> result;

        private HintOutput(List<ErrorDescription> result) {
            this.result = result;

        }

        /**Assert that the hint(s) produced the given warnings. The provided strings
         * should match {@code toString()} results of {@link ErrorDescription}s produced
         * by the hint(s).
         *
         * @param warnings expected {@code toString()} results of {@link ErrorDescription}s produced
         *                 by the hint
         * @return itself
         * @throws AssertionError if the given warnings do not match the actual warnings
         */
        public HintOutput assertWarnings(String... warnings) {
            List<ErrorDescription> errors = new ArrayList<ErrorDescription>(result);
            Collections.sort(errors, ERRORS_COMPARATOR);
            List<String> errorsNames = new LinkedList<String>();

            errors = errors != null ? errors : Collections.<ErrorDescription>emptyList();

            for (ErrorDescription e : errors) {
                errorsNames.add(e.toString());
            }

            assertTrue("The warnings provided by the hint do not match expected warnings. Provided warnings: " + errorsNames.toString(), Arrays.equals(warnings, errorsNames.toArray(new String[0])));

            return this;
        }

        /**Find a specific warning.
         *
         * @param warning the warning to find - must be equivalent to {@code toString()}
         *                results of the {@link ErrorDescription}.
         * @return a wrapper about the given specific warnings
         * @throws AssertionError if the given warning cannot be found
         */
        public HintWarning findWarning(String warning) {
            List<ErrorDescription> errors = new ArrayList<ErrorDescription>(result);
            Collections.sort(errors, ERRORS_COMPARATOR);

            ErrorDescription toFix = null;

            for (ErrorDescription d : errors) {
                if (warning.equals(d.toString())) {
                    toFix = d;
                    break;
                }
            }

            assertNotNull("Warning: \"" + warning + "\" not found. All ErrorDescriptions: " + errors.toString(), toFix);

            return new HintWarning(toFix);
        }
    }

    /**A wrapper over a single warning.
     */
    public final class HintWarning {
        private final ErrorDescription warning;
        HintWarning(ErrorDescription warning) {
            this.warning = warning;
        }
        /**Applies the only fix of the current warning. Fails if the given warning
         * does not have exactly one fix.
         *
         * Note this is a destructive operation - the {@link #run(java.lang.Class)} or {@link #applyFix}
         * cannot be run in the future on any object that follows the chain from the same invocation of {@link #create()}.
         *
         * @return a wrapper over resulting source code
         * @throws AssertionError if there is not one fix for the given {@link ErrorDescription}
         */
        public AppliedFix applyFix() throws Exception {
            assertTrue("Must be computed", warning.getFixes().isComputed());

            List<Fix> fixes = warning.getFixes().getFixes();

            assertEquals(1, fixes.size());

            fixes.get(0).implement();
            LifecycleManager.getDefault().saveAll();
            
            return new AppliedFix();
        }
        /**Applies the specified fix of the current warning.
         *
         * Note this is a destructive operation - the {@link #run(java.lang.Class)} or {@link #applyFix}
         * cannot be run in the future on any object that follows the chain from the same invocation of {@link #create()}.
         *
         * @param fix {@link Fix#getText() } result of the required fix
         * @return a wrapper over resulting source code
         * @throws AssertionError if the fix cannot be found
         */
        public AppliedFix applyFix(String fix) throws Exception {
            assertTrue("Must be computed", warning.getFixes().isComputed());

            List<Fix> fixes = warning.getFixes().getFixes();
            List<String> fixNames = new LinkedList<String>();
            Fix toApply = null;

            for (Fix f : fixes) {
                if (fix.equals(f.getText())) {
                    toApply = f;
                }

                fixNames.add(f.getText());
            }

            assertNotNull("Cannot find fix to invoke: " + fixNames.toString(), toApply);

            toApply.implement();
            LifecycleManager.getDefault().saveAll();

            return new AppliedFix();
        }
    }

    /**A wrapper over result after applying a fix.
     */
    public final class AppliedFix {
        /**Require that the result is compilable. Equivalent to {@code assertCompilable("test/Test.java")}
         *
         * @return the wrapper itself
         * @throws AssertionError if the result is not compilable
         */
        public AppliedFix assertCompilable() throws Exception {
            return assertCompilable("test/Test.java");
        }
        /**Require that the given resulting file is compilable.
         *
         * @param fileName the name of the file that should be verified
         * @return the wrapper itself
         * @throws AssertionError if the result is not compilable
         */
        public AppliedFix assertCompilable(String fileName) throws Exception {
            FileObject toCheck = sourceRoot.getFileObject(fileName);

            assertNotNull(toCheck);

            ensureCompilable(toCheck);
            return this;
        }
        /**Verify the content of the resulting file. Equivalent to {@code assertOutput("test/Test.java")}.
         *
         * This method will "normalize" whitespaces in the file: generally, all
         * whitespaces are reduced to a single space both in the given code and
         * the code read from the file, before the comparison.
         *
         * @param code expected content of the resulting file.
         * @return the wrapper itself
         * @throws AssertionError if the file does not have the correct content
         */
        public AppliedFix assertOutput(String code) throws Exception {
            return assertOutput("test/Test.java", code);
        }
        /**Verify the content of the given resulting file.
         *
         * This method will "normalize" whitespaces in the file: generally, all
         * whitespaces are reduced to a single space both in the given code and
         * the code read from the file, before the comparison.
         *
         * @param fileName the name of the file that should be verified
         * @param code expected content of the resulting file.
         * @return the wrapper itself
         * @throws AssertionError if the file does not have the correct content
         */
        public AppliedFix assertOutput(String fileName, String code) throws Exception {
            FileObject toCheck = sourceRoot.getFileObject(fileName);

            assertNotNull(toCheck);

            DataObject toCheckDO = DataObject.find(toCheck);
            EditorCookie ec = toCheckDO.getLookup().lookup(EditorCookie.class);
            Document toCheckDocument = ec.openDocument();

            String realCode = toCheckDocument.getText(0, toCheckDocument.getLength());

            //ignore whitespaces:
            realCode = realCode.replaceAll("[ \t\n]+", " ");

            assertEquals("The output code does not match the expected code.", code.replaceAll("[ \t\n]+", " "), realCode);

            return this;
        }
        /**Verify the content of the resulting file. Equivalent to {@code assertVerbatimOutput("test/Test.java")}.
         *
         * This method will compare the content of the file exactly with the provided
         * code.
         *
         * @param fileName the name of the file that should be verified
         * @param code expected content of the resulting file.
         * @return the wrapper itself
         * @throws AssertionError if the result is not compilable
         */
        public AppliedFix assertVerbatimOutput(String code) throws Exception {
            return assertVerbatimOutput("test/Test.java", code);
        }
        /**Verify the content of the given resulting file.
         *
         * This method will compare the content of the file exactly with the provided
         * code.
         *
         * @param fileName the name of the file that should be verified
         * @param code expected content of the resulting file.
         * @return the wrapper itself
         * @throws AssertionError if the result is not compilable
         */
        public AppliedFix assertVerbatimOutput(String fileName, String code) throws Exception {
            FileObject toCheck = sourceRoot.getFileObject(fileName);

            assertNotNull(toCheck);

            DataObject toCheckDO = DataObject.find(toCheck);
            EditorCookie ec = toCheckDO.getLookup().lookup(EditorCookie.class);
            Document toCheckDocument = ec.openDocument();

            String realCode = toCheckDocument.getText(0, toCheckDocument.getLength());

            assertEquals("The output code does not match the expected code.", code, realCode);

            return this;
        }
    }

    private static final Comparator<ErrorDescription> ERRORS_COMPARATOR = new Comparator<ErrorDescription> () {

        public int compare (ErrorDescription e1, ErrorDescription e2) {
            return e1.getRange ().getBegin ().getOffset () - e2.getRange ().getBegin ().getOffset ();
        }
    };

    static {
        System.setProperty("org.openide.util.Lookup", TestLookup.class.getName());
        Assert.assertEquals(TestLookup.class, Lookup.getDefault().getClass());
    }
}
