/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.support;

import org.netbeans.modules.jshell.parsing.ModelAccessor;
import org.netbeans.modules.jshell.parsing.LexerEmbeddingAdapter;
import org.netbeans.modules.jshell.model.Rng;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.model.ConsoleListener;
import org.netbeans.modules.jshell.model.ConsoleModel;
import org.netbeans.modules.jshell.model.ConsoleEvent;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.lib.nbjshell.RemoteJShellService;
import jdk.jshell.JShell;
import jdk.jshell.JShell.Subscription;
import org.netbeans.lib.nbjshell.JShellAccessor;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControl.ExecutionControlException;
import jdk.jshell.spi.ExecutionEnv;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.queries.SourceLevelQuery.Result;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.model.ConsoleContents;
import org.netbeans.modules.jshell.model.SnippetHandle;
import org.netbeans.modules.jshell.parsing.ShellAccessBridge;
import org.netbeans.modules.jshell.parsing.SnippetRegistry;
import static org.netbeans.modules.jshell.support.JShellLauncher.quote;
import org.netbeans.modules.jshell.support.ShellHistory.Item;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 * The root object for any JShell session. A shell session consists of:
 * <ul>
 * <li>a Document, viewed by an editor
 * <li>an output window, where the JShell prints the results
 * <li>the JShell instance itself
 * <li>a process handle to the running target VM
 * <li>transient FileSystem where the sources for parsing are created
 * <li>ConsoleModel which divides the Document and maps JShell snippets to
 * document parts.
 * </ul>
 * <p/>
 * <b>Threading model:</b> The JShell executes single-threaded, so <b>all accesses</b>
 * to JShell must be serialized using {@link #post(java.lang.Runnable)}. The getter
 * for JShell asserts when accessed from outside such Runnable - do not abuse.
 * <p/>
 *
 * @author sdedic
 */
public class ShellSession  {
    private static Logger LOG = Logger.getLogger(ShellSession.class.getName());
    
    public static final String PROP_ACTIVE = "active";

    /**
     * Work root, contains console file and snippet files
     */
    private final FileObject      workRoot;

    /**
     * The document being operated upon
     */
    private final Document  consoleDocument;
    
    /**
     * The java platform and projectInfo may possibly change.
     */
    private final JavaPlatform    platform;
    
    /**
     * ClasspathInfo as set up by the Project
     */
    private final ClasspathInfo   projectInfo;
    
    private ClasspathInfo cpInfo;
    
    /**
     * The executing JShell
     */
    private JShell shell;
    
    private ConsoleModel    model;
    
    /**
     * The shell output stream, possibly null.
     * Will be initialized during startup
     */
    private PrintStream shellControlOutput;
    
    private String  displayName;

    /**
     * True, if the operation of Shell is closed. Closed ShellSession
     * should not receive anything from the JShell. Detached ShellSession
     * MAY receive something, but should not reflect it in the document.
     */
    private volatile boolean closed;
    
    private FileObject consoleFile;
    private JShellEnvironment env;
    private final FileSystem  editorSnippetsFileSystem;
    private final FileObject editorWorkRoot;
    
    private volatile Set<Snippet>    initialSetupSnippets = Collections.emptySet();

    /**
     * True, if the Session was detached from the document. Another session
     * now 'owns' the document. The flag cannot be reset back to false.
     */
    private volatile boolean detached;
    
    private static final RequestProcessor FORCE_CLOSE_RP = new RequestProcessor("JShell socket closer");
    
    /**
     * Mapps snippets to the timestamps of their snippet files. Only valid snippets will
     */
    private final Map<Snippet, Long>    snippetTimeStamps = new WeakHashMap<>();

    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    
    private SnippetRegistry     snippetRegistry;
        
    public ShellSession(JShellEnvironment env) {
        this(env.getDisplayName(), 
             env.getConsoleDocument(), 
             env.getClasspathInfo(),
             env.getPlatform(),
             env.getWorkRoot(), 
             env.getConsoleFile());
        this.env = env;
    }
    
    public JShellEnvironment getEnv() {
        return env;
    }

    private ShellSession(String displayName, Document doc, ClasspathInfo cpInfo, 
            JavaPlatform platform, FileObject workRoot, FileObject consoleFile) {
        this.consoleDocument = doc;
        this.projectInfo = cpInfo;
        this.displayName = displayName;
        this.platform = platform;
        this.consoleFile = consoleFile;
        this.workRoot = workRoot;
        
        this.editorSnippetsFileSystem = FileUtil.createMemoryFileSystem();
        this.editorWorkRoot = editorSnippetsFileSystem.getRoot();
        this.shellControlOutput = new PrintStream(
            new WriterOutputStream(
                    // delegate to whatever Writer will be set
                    new Writer() {
                        @Override
                        public void write(char[] cbuf, int off, int len) throws IOException {
                            documentWriter.write(cbuf, off, len);
                        }

                        @Override
                        public void flush() throws IOException {
                            documentWriter.flush();
                        }

                        @Override
                        public void close() throws IOException {
                            documentWriter.close();
                        }

                    })
        );
    }
    
    public boolean isActive() {
        return !detached;
    }

    private Task detach() {
        Task t;
        synchronized (allSessions) {
            Reference<ShellSession> refS = allSessions.get(consoleDocument);
            if (refS != null && refS.get() == this) {
                allSessions.remove(consoleDocument);
                detached = true;
            } else {
                return Task.EMPTY;
            }
        }
//        closed = true;
        model.detach();
        closed();
        if (exec != null) {
            FORCE_CLOSE_RP.post(this::forceCloseStreams, 300);
        }
        // leave the model
        gsm.getGuardedSections().forEach((GuardedSection gs) -> gs.removeSection());
        return sendJShellClose();
    }
    
    private synchronized void forceCloseStreams() {
        if (exec != null) {
            exec.closeStreams();
        }
    }

    JShell getJShell() {
        assert evaluator.isRequestProcessorThread();
        if (shell == null) {
            initJShell();
        }
        return shell;
    }
    
    public FileObject getConsoleFile() {
        return consoleFile;
    }
    
    public boolean isValid() {
        return !closed && !detached;
    }
    
    /**
     * Writer implementation over the document. Informs the ConsoleModel,
     * that an event worth parsing is coming.
     */
    private class DocumentOutput extends Writer {
        private Throwable exception;
        
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (!isActive()) {
                // do not write from a closed JShell
                return;
            }
            AtomicLockDocument ald = LineDocumentUtils.asRequired(consoleDocument, AtomicLockDocument.class);
            try {
                ald.runAtomic(()-> {
                    try {
                        int offset = consoleDocument.getLength();
                        model.insertResponseString(offset,
                                String.copyValueOf(cbuf, off, len), null);
                    } catch (BadLocationException ex) {
                        exception = ex;
                    }
                });
                if (exception != null) {
                    throw new IOException(exception);
                }
            } finally {
                exception = null;
            }
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
            // FIXME: the JShell session should be terminated.
        }
    }
    
    /**
     * Creates a snippet file. If the `editedSnippetIndex' is not >= 0, it generates
     * a file NOT seen by repository updater; if the edited section contains multiple
     * snippets, then it is possible to give the snippet explicit name.
     * 
     * @param snippet
     * @param editedSnippetIndex
     * @return 
     */
    public FileObject   snippetFile(SnippetHandle snippet, int editedSnippetIndex) {
        if (launcher == null) {
            return null;
        }
        return snippetRegistry.snippetFile(snippet, editedSnippetIndex);
    }
    
    /**
     * Stream backed by a Writer. Uses UTF-8 to decode characters from the stream.
     */
    private static class WriterOutputStream extends OutputStream {
        private boolean writeImmediately = true;
        
        private final CharsetDecoder decoder;
        private final ByteBuffer decoderIn = ByteBuffer.allocate(128);
        private final CharBuffer decoderOut;
        private final Writer writer;
        
        public WriterOutputStream(Writer out) {
            this.writer = out;
            this.decoder = Charset.forName("UTF-8"). //NOI18N
                    newDecoder().
                    onMalformedInput(CodingErrorAction.REPLACE).
                    onUnmappableCharacter(CodingErrorAction.REPLACE).
                    replaceWith("?"); //NOI18N
            this.decoderOut = CharBuffer.allocate(2048);
        }
        
        @Override
        public void write(int b) throws IOException {
            decoderIn.put((byte)b);
            processInput(false);
            if (writeImmediately) {
                flushOutput();
            }
        }
        
        public void write(final byte[] b, int off, int len) throws IOException {
            while (len > 0) {
                final int c = Math.min(len, decoderIn.remaining());
                decoderIn.put(b, off, c);
                processInput(false);
                len -= c;
                off += c;
            }
            if (writeImmediately) {
                flushOutput();
            }
        }
        
        private void flushOutput() throws IOException {
            if (decoderOut.position() > 0) {
                writer.write(decoderOut.array(), 0, decoderOut.position());
                decoderOut.rewind();
            }
        }

        @Override
        public void close() throws IOException {
            processInput(true);
            flushOutput();
            writer.close();
        }

        @Override
        public void flush() throws IOException {
            flushOutput();
            writer.flush();
        }        
        
        private void processInput(final boolean endOfInput) throws IOException {
            // Prepare decoderIn for reading
            decoderIn.flip();
            CoderResult coderResult;
            while (true) {
                coderResult = decoder.decode(decoderIn, decoderOut, endOfInput);
                if (coderResult.isOverflow()) {
                    flushOutput();
                } else if (coderResult.isUnderflow()) {
                    break;
                } else {
                    // The decoder is configured to replace malformed input and unmappable characters,
                    // so we should not get here.
                    throw new IOException("Unexpected coder result"); //NOI18N
                }
            }
            // Discard the bytes that have been read
            decoderIn.compact();
        }
    }

    private volatile Launcher launcher;
    
    public Pair<ShellSession, Task> start() {
        ShellSession previous  = null;
        
        synchronized (allSessions) {
            Reference<ShellSession> sr = allSessions.get(env.getConsoleDocument());
            ShellSession s = null;
            
            if (sr != null) {
                previous = sr.get();
            }
        }
        if (previous != null) {
            previous.detach();
            AtomicLockDocument ald = LineDocumentUtils.asRequired(consoleDocument, AtomicLockDocument.class);
            ald.runAtomic(() -> {
                try {
                    consoleDocument.remove(0, consoleDocument.getLength());
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }
        init(previous);
        try {
            refreshGuardedSection();
        } catch (BadLocationException ex) {
        }
        synchronized (allSessions) {
            allSessions.put(consoleDocument, new WeakReference<>(this));
        }
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] { 
            env.getSnippetClassPath()
        });
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] { 
            env.getUserLibraryPath()
        });

        return Pair.of(previous, evaluator.post(() -> {

            ModelAccessor.INSTANCE.execute(model, false, () -> {
                try {
                    URL url = URLMapper.findURL(workRoot, URLMapper.INTERNAL);
                    IndexingManager.getDefault().refreshIndexAndWait(url, null, true);
                    getJShell();
                } catch (Exception ex) {
                    LOG.log(Level.FINE, "Thrown error: ", ex);
                    reportErrorMessage(ex);
                } finally {
                    ensureInputSectionAvailable();
                }
            }, this::getPromptAfterError);
        }));
    }
    
    /**
     * Writer to the console document.
     */
    private Writer documentWriter = new DocumentOutput();

    public Writer getDocumentWriter() {
        return documentWriter;
    }
    
    private RemoteJShellService exec;
    
    private String addRoots(String prev, ClassPath cp) {
        FileObject[] roots = cp.getRoots();
        StringBuilder sb = new StringBuilder(prev);
        
        for (FileObject r : roots) {
            FileObject ar = FileUtil.getArchiveFile(r);
            if (ar == null) {
                ar = r;
            }
            File f = FileUtil.toFile(ar);
            if (f != null) {
                if (sb.length() > 0) {
                    sb.append(File.pathSeparatorChar);
                }
                sb.append(f.getPath());
            }
        }
        return sb.toString();
    }
    
    private void setupJShellClasspath(JShell jshell) throws ExecutionControlException {
        ClassPath bcp = getClasspathInfo().getClassPath(PathKind.BOOT);
        ClassPath compile = getClasspathInfo().getClassPath(PathKind.COMPILE);
        ClassPath source = getClasspathInfo().getClassPath(PathKind.SOURCE);
        
        String cp = addRoots("", bcp);
        cp = addRoots(cp, compile);
        
        JShellAccessor.resetCompileClasspath(jshell, cp);
    }
    
    private boolean initializing;
    
    public  String getClasspath() {
        return createClasspathString();
    }
    
    private class GenProxy implements JShellGenerator {
        private final JShellGenerator del;

        public GenProxy(JShellGenerator del) {
            this.del = del;
        }

        @Override
        public ExecutionControl generate(ExecutionEnv ee) throws Throwable {
            ExecutionControl ctrl = del.generate(ee);
            exec = (RemoteJShellService)ctrl;
            return ctrl;
        }

        @Override
        public String getTargetSpec() {
            return del.getTargetSpec();
        } 
    }
    
    /**
     * Finds appropriate source (language) version for the environment.
     * @return 
     */
    private SpecificationVersion findSourceVersion() {
        Project p = env.getProject();
        if (p == null) {
            return findTargetVersion();
        }
        Result r = SourceLevelQuery.getSourceLevel2(p.getProjectDirectory());
        String s = r.getSourceLevel();
        if (s != null) {
            return new SpecificationVersion(s);
        } else {
            return findTargetVersion();
        }
    }
    
    private SpecificationVersion findTargetVersion() {
        return env.getPlatform().getSpecification().getVersion();
    }
    
    private class Launcher extends JShellLauncher implements Consumer<SnippetEvent> {
        Subscription subscription;
        
        public Launcher(ClasspathInfo cpInfo, PrintStream cmdout, PrintStream cmderr, InputStream userin, PrintStream userout, PrintStream usererr, JShellGenerator execEnv) {
            super(cpInfo, cmdout, cmderr, userin, userout, usererr, execEnv);
        }

        @Override
        protected JShell.Builder createJShell() {
            return customizeBuilder(super.createJShell());
        }

        @Override
        protected JShell createJShellInstance() {
            JShell shell = super.createJShellInstance();
            try {
                setupJShellClasspath(shell);
            } catch (ExecutionControlException ex) {
                Exceptions.printStackTrace(ex);
            }
            synchronized (ShellSession.this) {
                snippetRegistry = new SnippetRegistry(shell, bridgeImpl, workRoot, editorWorkRoot);
                ShellSession.this.shell = shell;
            }
            this.subscription = shell.onSnippetEvent(this);
            return shell;
        }

        @Override
        public void accept(SnippetEvent e) {
            SnippetHandle handle = snippetRegistry.installSnippet(
                e.snippet(), null, 0, true);
            // create an indexed file for the snippet.
            snippetRegistry.snippetFile(handle, 0);
        }

        @Override
        protected void classpathAdded(String arg) {
            super.classpathAdded(arg);
            File f = new File(arg);
            FileObject fob = FileUtil.toFileObject(f);
            if (fob != null) {
                env.appendClassPath(fob);
            }
        }

        @Override
        protected String resolveUserHome(String path) {
            String homeResolved = super.resolveUserHome(path);
            File f = new File(homeResolved);
            if (!f.isAbsolute()) {
                // prepend project's directory
                Project p = env.getProject();
                if (p != null) {
                    f = new File(FileUtil.toFile(p.getProjectDirectory()), homeResolved);
                    return f.getPath();
                }
            }
            return homeResolved;
        }
    }
    
    private SwitchingJavaFileManger fileman;
    
    private JShell.Builder customizeBuilder(JShell.Builder b) {
        SpecificationVersion v = findSourceVersion();
        if (v != null) {
            b.compilerOptions("-source", v.toString()); // NOI18N
        }
        v = findTargetVersion();
        if (v != null) {
            b.compilerOptions("-target", v.toString()); // NOI18N
        }
        b.remoteVMOptions("-classpath", quote(createClasspathString())); // NOI18N
        b.fileManager(fileman = new SwitchingJavaFileManger(getClasspathInfo()));
        return b;
    }
    
    private synchronized Launcher initShellLauncher() throws IOException {
        if (launcher != null) {
            return launcher;
        }
        launcher = new Launcher(
            env.getClasspathInfo(),
            shellControlOutput,
            shellControlOutput, 
            env.getInputStream(),
            env.getOutputStream(),
            env.getErrorStream(),
            new GenProxy(env.createExecutionEnv())
        );
        return launcher;
    }
    
    /**
     * Creates a private JShell instance
     * @return 
     */
    private JShell createJShellInstance() {
        JShell.Builder b = JShell.builder();
        return customizeBuilder(b).build();
    }
    
    private void initJShell() {
        if (shell != null) {
            return;
        }
        Launcher l = null;
        JShell shell = null;
        Subscription sub = null;
        try {
            initializing = true;
            l = initShellLauncher();
            shell = launcher.getJShell();
            // not necessary to launch  the shell, but WILL display the initial prompt
            launcher.start();
            initialSetupSnippets = new HashSet<>(shell.snippets().collect(Collectors.toList()));
        } catch (IOException | InternalError err) {
            Throwable t = err.getCause();
            if (t == null) {
                t = err;
            }
            reportErrorMessage(t);
            closed();
            env.notifyDisconnected(this);
            return;
        } finally {
            initializing = false;
            if (l != null & l.subscription != null && shell != null) {
                shell.unsubscribe(l.subscription);
            }
        }
        synchronized (this) {
            // it's possible that the shell's startup will terminate the session
            if (isValid()) {
                shell.onShutdown(sh -> closedDelayed());
            }
        }
    }
    
    private ShellAccessBridge bridgeImpl = new ShellAccessBridge() {
        @Override
        public <T> T execute(Callable<T> xcode) throws Exception {
            if (evaluator.isRequestProcessorThread()) {
                return xcode.call();
            } else {
                return fileman.withLocalManager(xcode);
            }
        }

        @Override
        public SourceCodeAnalysis.CompletionInfo analyzeInput(String input) {
            try {
                return execute(() -> getShell().sourceCodeAnalysis().analyzeCompletion(input));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public boolean isInitialized() {
            return shell != null;
        }
        
    };
    
    public SnippetRegistry getSnippetRegistry() {
        return snippetRegistry;
    }

    @NbBundle.Messages({
            "# {0} - the original exception reason",
            "ERR_CannotInitializeShell=Could not initialize JShell: {0}",
            "# {0} - original error reason",
            "ERR_CannotInitializeBecause=\n\tcaused by: {0}",
    })
    private String buildErrorMessage(Throwable t) {
        String locMessage = t.getLocalizedMessage();
        if (locMessage == null) {
            locMessage = t.getClass().getName();
        }
        if (t.getCause() == t) {
            return Bundle.ERR_CannotInitializeShell(locMessage);
        }
        StringBuilder sb = new StringBuilder(Bundle.ERR_CannotInitializeShell(locMessage));
        while (t.getCause() != t) {
            t = t.getCause();
            if (t == null) {
                break;
            }
            locMessage = t.getLocalizedMessage();
            if (locMessage == null) {
                locMessage = t.getClass().getName();
            }
            sb.append(Bundle.ERR_CannotInitializeBecause(t.getLocalizedMessage()));
        }
        return sb.toString();
    }

    private void ensureInputSectionAvailable() {
        ConsoleSection s = model.processInputSection(false);
        if (s != null) {
            return;
        }
        String promptText = "\n" + launcher.prompt(false); // NOI18N
        writeToShellDocument(promptText);
    }
    
    @NbBundle.Messages({
        "MSG_JShellClosed=The Java Shell VM has closed the connection. You may use shell slash-commands, or re-run the process.",
        "MSG_JShellCannotStart=The Java Shell VM is not reachable. You may only use shell commands, or re-run the target process.",
        "MSG_JShellDisconnected=The remote Java Shell has terminated. Restart the Java Shell to continue"
    })
    public void notifyClosed(JShellEnvironment env) {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }
        String s;
        if (initializing) {
            s = Bundle.MSG_JShellCannotStart();
        } else if (env.isClosed()) {
            s = Bundle.MSG_JShellClosed();
        } else {
            s = Bundle.MSG_JShellDisconnected();
        }
        reportShellMessage(s);
    }
    
    public void reportErrorMessage(Throwable t) {
        LOG.log(Level.INFO, "Error in JSHell", t);
        reportShellMessage(buildErrorMessage(t));
    }
    
    private void reportShellMessage(String msg) {
        if (!isActive()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (!scrollbackEndsWithNewline()) {
            sb.append("\n");
        }
        sb.append("|  ");
        if (msg.endsWith("\n")) {
            msg = msg.substring(0, msg.length() - 1);
        }
        sb.append(msg.replace("\n", "\n|  ")); // NOI18N
        sb.append("\n"); // NOI18N
        writeToShellDocument(sb.toString());
    }
    
    private boolean scrollbackEndsWithNewline() {
        boolean[] ret = new boolean[1];
        consoleDocument.render(() -> {
            int l = consoleDocument.getLength();
            if (l == 0) {
                ret[0] = true;
            } else {
                try {
                    ret[0] = consoleDocument.getText(consoleDocument.getLength() - 1, 1).charAt(0) == '\n';
                } catch (BadLocationException ex) {
                    ret[0] = false;
                }
            }
        });
        return ret[0];
    }
    
    private void writeToShellDocument(String text) {
        /*
        try {
            documentWriter.append(text);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Could not report console message: {0}", text);
        }
        */
        model.writeToShellDocument(text);
    }
    
    private Set<Snippet>    excludedSnippets = new HashSet<>();
    
    private String createClasspathString() {
        File remoteProbeJar = InstalledFileLocator.getDefault().locate(
                "modules/ext/nb-custom-jshell-probe.jar", "org.netbeans.libs.jshell", false);
        File replJar = InstalledFileLocator.getDefault().locate("modules/ext/nb-jshell.jar", "org.netbeans.libs.jshell", false);
        File toolsJar = null;

        for (FileObject jdkInstallDir : platform.getInstallFolders()) {
            FileObject toolsJarFO = jdkInstallDir.getFileObject("lib/tools.jar");

            if (toolsJarFO == null) {
                toolsJarFO = jdkInstallDir.getFileObject("../lib/tools.jar");
            }
            if (toolsJarFO != null) {
                toolsJar = FileUtil.toFile(toolsJarFO);
            }
        }
        ClassPath compilePath = getClasspathInfo().getClassPath(PathKind.COMPILE);
        
        FileObject[] roots = compilePath.getRoots();
        File[] urlFiles = new File[roots.length];
        int index = 0;
        for (FileObject fo : roots) {
            File f = FileUtil.toFile(fo);
            if (f != null) {
                urlFiles[index++] = f;
            }
        }
        String cp = addClassPath(
                toolsJar != null ? toClassPath(remoteProbeJar, replJar, toolsJar) : 
                                   toClassPath(remoteProbeJar, replJar),
                urlFiles) + System.getProperty("path.separator") + " "; // NOI18N avoid REPL bug
        
        return cp;
    }

    private static String addClassPath(String prefix, File... files) {
        String suffix = toClassPath(files);
        if (prefix != null && !prefix.isEmpty()) {
            return prefix + System.getProperty("path.separator") + suffix;
        }
        return suffix;
    }

    private static String toClassPath(File... files) {
        String sep = "";
        StringBuilder cp = new StringBuilder();

        for (File f : files) {
            if (f == null) continue;
            cp.append(sep);
            cp.append(f.getAbsolutePath());
            sep = System.getProperty("path.separator");
        }

        return cp.toString();
    }

    private void closed() {
        synchronized (this) {
            if (closed) {
                return;
            }
        }
        env.notifyDisconnected(this);
        propSupport.firePropertyChange(PROP_ACTIVE, true, false);
        
        // save the history
        ShellHistory h = env.getLookup().lookup(ShellHistory.class);
        if (h != null) {
            saveInputSections(h);
        }
    }
    
    private void saveInputSections(ShellHistory history) {
        history.pushItems(historyItems());
    }
    
    private void closedDelayed() {
        FORCE_CLOSE_RP.post(new Runnable() {
            public void run() {
                closed();
            }
        }, 300);
    }

    private synchronized void init(ShellSession prev) {
        ConsoleModel.initModel();
        evaluator = new RequestProcessor("Evaluator for " + displayName);
        initClasspath();
        model = ModelAccessor.INSTANCE.createModel((LineDocument)consoleDocument, evaluator, bridgeImpl);
        model.addConsoleListener(new LexerEmbeddingAdapter());
        model.addConsoleListener(new GuardedSectionUpdater());
        
        // missing API to create a GuardedSectionManager against a plain document:
        AbstractGuardedSectionsProvider hack = new AbstractGuardedSectionsProvider(
                new GuardedEditorSupport() {
            @Override
                    public StyledDocument getDocument() {
                        return (StyledDocument)consoleDocument;
                    }
                }
            ) {
            
            @Override
            public char[] writeSections(List<GuardedSection> sections, char[] content) {
                return null;
            }
            
            @Override
            public AbstractGuardedSectionsProvider.Result readSections(char[] content) {
                return null;
            }
        };
        // this crates and registers a GuardedSectionManager in the doc properties
        hack.createGuardedReader(new ByteArrayInputStream(new byte[0]), Charset.defaultCharset());
        gsm = GuardedSectionManager.getInstance((StyledDocument)consoleDocument);
        gsProvider = hack;
        
    }
    private AbstractGuardedSectionsProvider gsProvider;
    private GuardedSectionManager gsm;

    private void initClasspath() {
        ClassPath snippetSource = ClassPathSupport.createProxyClassPath(
                projectInfo.getClassPath(PathKind.SOURCE),
                ClassPathSupport.createClassPath(editorWorkRoot),
                ClassPathSupport.createClassPath(workRoot)
        );
        
        ClassPath compileClasspath = projectInfo.getClassPath(PathKind.COMPILE);

        this.cpInfo = ClasspathInfo.create(
                projectInfo.getClassPath(PathKind.BOOT),
                compileClasspath,
                snippetSource
        );
        
        this.consoleDocument.putProperty("java.classpathInfo", this.cpInfo);
    }

    public Document getConsoleDocument() {
        return consoleDocument;
    }

    public ClasspathInfo getClasspathInfo() {
        return cpInfo;
    }

    public JShell getShell() {
        initJShell();
        return shell;
    }

    public static ShellSession createSession(
            JShellEnvironment env) {
        return new ShellSession(env);
    }
    
    public static ShellSession get(FileObject f) {
        EditorCookie cake = f.getLookup().lookup(EditorCookie.class);
        if (cake == null) {
            return null;
        }
        Document d = cake.getDocument();
        if (d == null) {
            return null;
        }
        return get(d);
    }

    public static ShellSession get(Document d) {
        if (d == null) {
            return null;
        }
        synchronized (allSessions) {
            Reference<ShellSession> sr = allSessions.get(d);
            return sr == null ? null : sr.get();
        }
    }

    public static Collection<ShellSession> allSessions() {
        Collection<Reference<ShellSession>> ll;
        synchronized (allSessions) {
            ll = allSessions.values();
        }
        Collection<ShellSession> res = new ArrayList<>(ll.size());
        for (Iterator<Reference<ShellSession>> it = ll.iterator(); it.hasNext(); ) {
            Reference<ShellSession> sr = it.next();
            ShellSession s = sr.get();
            if (s != null) {
                res.add(s);
            }
        }
        return res;
    }
    
    private void addNewline(int offset) {
        AtomicLockDocument ald = LineDocumentUtils.asRequired(consoleDocument, AtomicLockDocument.class);
        ald.runAtomic(() -> {
            try {
                DocumentUtilities.setTypingModification(consoleDocument, false);
                consoleDocument.insertString(offset, "\n", null);
            } catch (BadLocationException ex) {
                
            }
        });
    }

    private void clearAndAddNewline(int offset) {
        AtomicLockDocument ald = LineDocumentUtils.asRequired(consoleDocument, AtomicLockDocument.class);
        ald.runAtomic(() -> {
            try {
                DocumentUtilities.setTypingModification(consoleDocument, false);
                consoleDocument.remove(offset, consoleDocument.getLength() - offset);
                consoleDocument.insertString(offset, "\n", null);
            } catch (BadLocationException ex) {
                
            }
        });
    }

    private boolean recordNoSave = false;
    
    private String executionLabel;

    public String getExecutionLabel() {
        return executionLabel;
    }
    
    public void evaluate(String command) throws IOException {
        evaluate(command, false, null);
    }
    
    public void clearInputAndEvaluateExternal(String command, String label) throws IOException {
        // evaluate even though the connection is not active; perhaps
        // we get access to the history.
        post(() -> {
            ConsoleSection s = model.processInputSection(true);
            if (s == null) {
                return;
            }
            clearAndAddNewline(s.getPartBegin());
            boolean saveSave = this.recordNoSave;
            try {
                recordNoSave = true;
                this.executionLabel = label;
                doExecuteCommands(command);
            } finally {
                this.executionLabel = null;
                this.recordNoSave = saveSave;
            }
        });
    }
    
    public void evaluate(String command, boolean excludeFromSave, String label) throws IOException {
        // evaluate even though the connection is not active; perhaps
        // we get access to the history.
        post(() -> {
            ConsoleSection s = model.processInputSection(true);
            if (s == null) {
                return;
            }
            String c = s.getContents(consoleDocument);
            if (!c.endsWith("\n")) { // NOI18N
                addNewline(s.getEnd());
            }
            boolean saveSave = this.recordNoSave;
            try {
                recordNoSave = excludeFromSave;
                this.executionLabel = label;
                doExecuteCommands(command);
            } finally {
                this.executionLabel = null;
                this.recordNoSave = saveSave;
            }
        });
    }

    /**
     * Executes commands in input buffer. Executes one by one, since some
     * snippets may be redundant and will not be reported at all by JShell, so
     * replacement of individual Snippets will be assisted by setting up a
     * start position of the to-be-executed snippet in the buffer.
     * <p/>
     * Since JShell stops executin after first error, a 'erroneous' flag is raised
     * by {@link #acceptSnippet} when it sees a REJECTED snippet (an error).
     */
    @NbBundle.Messages({
        "MSG_ErrorExecutingCommand=Note: You may need to restart the Java Shell to resume proper operation",
        "MSG_JShellCannotExecute=Java Shell cannot execute commands. Restart Java Shell or its host process."
    })
    private void doExecuteCommands(final String cmd) {
        ConsoleSection sec = model.processInputSection(true);
        if (sec == null) {
            return;
        }
        // rely on JShell's own parsing from the input section
        // just for case:
        ModelAccessor.INSTANCE.execute(model, cmd != null, () -> {
            Executor executor = new Executor(cmd, model.getExecutingSection(), shell);
            executor.execute();
        }, this::getPromptAfterError);
    }
    
    private class Executor implements Runnable, Consumer<SnippetEvent> {
        private final String          cmd;
        private final ConsoleSection  exec;
        private final JShell          shell;
        
        private List<String>    toExec = new ArrayList<>();
        private boolean         erroneous;
        private int             execOffset;

        public Executor(String cmd, ConsoleSection exec, JShell shell) {
            this.cmd = cmd;
            this.exec = exec;
            this.shell = shell;
        }
        
        private boolean isExternal() {
            return cmd != null;
        }
        
        @Override
        public void accept(SnippetEvent e) {
            switch (e.status()) {
                case REJECTED:
                    erroneous = true;
                case VALID:
                case RECOVERABLE_DEFINED:
                case RECOVERABLE_NOT_DEFINED:
                case NONEXISTENT:
                if (recordNoSave) {
                    excludedSnippets.add(e.snippet());
                }

                // register in the registry:
                SnippetHandle handle;
                
                if (isExternal()) {
                    handle = snippetRegistry.installSnippet(
                        e.snippet(), null, 0, true);
                } else {
                    handle = snippetRegistry.installSnippet(
                        e.snippet(), exec, execOffset, false);
                }
                // create an indexed file for the snippet.
                snippetRegistry.snippetFile(handle, 0);
            }
        }
        
        @Override
        public void run() {
            if (exec.getType() == ConsoleSection.Type.COMMAND) {
                toExec.add(exec.getContents(consoleDocument));
            } else {
                for (Rng r : exec.getAllSnippetBounds()) {
                    toExec.add(exec.getRangeContents(consoleDocument, r));
                }
            }
        }
        
        void execute() {
            try {
                if (cmd != null) {
                    toExec.add(cmd);
                } else {
                    // fill toExec
                    consoleDocument.render(this);
                }
                if (toExec.isEmpty()) {
                    return;
                }
                Rng[] ranges = cmd == null ? exec.getAllSnippetBounds() : null;
                int index = 0;
                execOffset = 0;
                Subscription sub = null;
                
                try {
                    for (String s : toExec) {
                            if (sub == null) {
                                String t = s.trim();
                                if (!t.isEmpty() && t.charAt(0) != '/') { // shell commands
                                    sub = shell.onSnippetEvent(this);
                                }
                            }
                            if (ranges != null) {
                                execOffset = exec.offsetToContents(ranges[index].start, true);
                            }
                            launcher.evaluate(s, index == toExec.size() - 1);
                            if (erroneous) {
                                break;
                            }
                        index++;
                    }
                } catch (IllegalStateException ex) {
                    reportShellMessage(Bundle.MSG_JShellCannotExecute());
                } catch (RuntimeException | IOException ex) {
                    reportErrorMessage(ex);
                    reportShellMessage(Bundle.MSG_ErrorExecutingCommand());
                } finally {
                    if (sub != null) {
                        shell.unsubscribe(sub);
                    }
                    ensureInputSectionAvailable();
                }
            } finally {
                
            }
        }
        
    }
    
    private String getPromptAfterError() {
        return launcher.prompt(false);
    }
    
    private synchronized Task sendJShellClose() {
        RemoteJShellService e;
        synchronized (this) {
            if (launcher == null) {
                return Task.EMPTY;
            }
            e = this.exec;
        }
        if (e != null) {
            e.requestShutdown();
        }
        // possibly delayed, if the evaluator is just processing some remote call.
        return evaluator.post(() -> {
            try {
                launcher.closeState();
            } catch (InternalError ex) {
                // ignore
            }
            forceCloseStreams();
        });
    }
    
    /**
     * Terminates the session and disconnects it from the Document.
     * @return Task where the Jshell termination runs.
     */
    public Task closeSession() {
        return detach();
    }
    
    interface Processor {
        public void handle(String data);
    }
    
    public ConsoleModel getModel() {
        return model;
    }
    
    private void refreshGuardedSection() throws BadLocationException {
        if (!isActive()) {
            return;
        }
        gsm.getGuardedSections().forEach((GuardedSection gs) -> gs.removeSection());
        ConsoleSection s = model.getInputSection();
        LineDocument ld = LineDocumentUtils.asRequired(consoleDocument, LineDocument.class);
        if (s == null) {
            // protected including the final newline, so an insertion at the end will
            // expand the guarded block automatically
            int l = consoleDocument.getLength() + 1;       
            gsm.protectSimpleRegion(ld.createPosition(0, Position.Bias.Forward),
                    ld.createPosition(l, Position.Bias.Forward),
                    "scrollback"); // NOI18N
        } else {
            int wr = s.getPartBegin() - 1;
            gsm.protectSimpleRegion(ld.createPosition(0, Position.Bias.Forward),
                    ld.createPosition(wr, Position.Bias.Backward),
                    "scrollback"); // NOI18N
        }
    }

    private static final Map<Document, Reference<ShellSession>> allSessions = new WeakHashMap<>();
    
    private class GuardedSectionUpdater implements ConsoleListener {
        @Override
        public void sectionCreated(ConsoleEvent e) {
            List<ConsoleSection> aff = e.getAffectedSections();
            for (ConsoleSection s : aff) {
                // if an input section has been created, the document BEFORE the section
                // should become guarded
                if (s == model.getLastInputSection()) {
                    // redefine the guarded block, if any, to span from the 
                    // start to the prompt end.
                    refresh();
                }
            }
        }
        
        private void refresh() {
            try {
                refreshGuardedSection();
            } catch (BadLocationException ex) {
                //
            }
        }
        
        @Override
        public void sectionUpdated(ConsoleEvent e) {
            for (ConsoleSection s : e.getAffectedSections()) {
                if (s == model.getLastInputSection()) {
                    refresh();
                    break;
                }
            }
        }

        @Override
        public void executing(ConsoleEvent e) {
        }

        @Override
        public void closed(ConsoleEvent e) {
        }
    }
    
    /**
     * All accesses to the shell must go through the request processor.
     */
    private RequestProcessor evaluator;
    
    public Task    post(Runnable r) {
        return evaluator.post(r);
    }
    
    /**
     * Returns the user-entered snippets. Does not return snippets, which are run
     * during the initial startup of JShell, just snippets executed afterwards.
     * 
     * @param onlyValid
     * @return 
     */
    public List<Snippet> getSnippets(boolean onlyUser, boolean onlyValid) {
        Set<Snippet> initial = this.initialSetupSnippets;
        JShell sh = shell;
        if (sh == null) {
            return Collections.emptyList();
        }
        
        List<Snippet> snips = new ArrayList<>(sh.snippets().collect(Collectors.toList()));
        if (onlyUser) {
            snips.removeAll(initial);
            snips.removeAll(excludedSnippets);
        }
        if (onlyValid) {
            for (Iterator<Snippet> it = snips.iterator(); it.hasNext(); ) {
                Snippet s = it.next();
                if (!validSnippet(s)) {
                    it.remove();
                }
            }
        }
        return snips;
    }

    private boolean validSnippet(Snippet s) {
        Snippet.Status status = shell.status(s);
        return !(status == Snippet.Status.DROPPED ||
            status == Snippet.Status.OVERWRITTEN ||
            status == Snippet.Status.REJECTED);
    }
    
    public void stopExecutingCode() {
        JShell shell = this.shell;
        if (shell == null || !model.isExecute()) {
            return;
        }
        shell.stop();
    }

    public List<ShellHistory.Item> historyItems() {
        final List<Item> historyLines = new ArrayList<>();
        try {
            ParserManager.parse(Collections.singleton(Source.create(getConsoleDocument())), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ConsoleContents console = ConsoleContents.get(resultIterator);
                    ConsoleSection input = console.getInputSection();
                    for (ConsoleSection s : console.getSectionModel().getSections()) {
                        if (!s.getType().input) {
                            continue;
                        }
                        if (s == input) {
                            // do not save current input
                            continue;
                        }
                        String contents = s.getContents(consoleDocument);
                        // ignore such lines, which contain just history command
                        if (contents.startsWith("/") && contents.length() > 2) {
                            if (contents.charAt(1) == '-' || Character.isDigit(contents.charAt(1))) {
                                continue;
                            }
                        }
                        List<SnippetHandle> handles = console.getHandles(s);
                        
                        Snippet.Kind sectionKind;
                        boolean command;
                        if (s.getType() == ConsoleSection.Type.COMMAND) {
                            command = true;
                            sectionKind = null;
                        } else {
                            command = false;
                            if (handles.isEmpty()) {
                                sectionKind = Snippet.Kind.ERRONEOUS;
                            } else {
                                sectionKind = handles.get(0).getKind();
                            }
                        }
                        contents = contents.trim();
                        if (contents.isEmpty()) {
                            continue;
                        }
                        historyLines.add(new ShellHistory.Item(sectionKind, command, contents));
                    }
                }
            });
        } catch (ParseException ex) {
            return Collections.emptyList();
        }
        return historyLines;
    }
}
