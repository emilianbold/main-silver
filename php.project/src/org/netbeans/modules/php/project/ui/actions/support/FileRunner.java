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
package org.netbeans.modules.php.project.ui.actions.support;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpInterpreter;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.spi.executable.DebugStarter;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 * Run or debug a file.
 * <p>
 * This class is thread safe.
 */
public final class FileRunner {

    static final Logger LOGGER = Logger.getLogger(FileRunner.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(FileRunner.class);
    private static final ExecutionDescriptor.LineConvertorFactory PHP_LINE_CONVERTOR_FACTORY = new PhpLineConvertorFactory();
    // for debugger, let's treat all the files withour project under one dbg session
    private static final Project DUMMY_PROJECT = new DummyProject();

    final File file;

    // @GuardedBy("this")
    PhpProject project;
    volatile String command;
    volatile String phpArgs;
    volatile String fileArgs;
    volatile String workDir;
    volatile boolean debug = false;


    public FileRunner(File file) {
        this.file = file;
    }

    public synchronized FileRunner project(PhpProject project) {
        this.project = project;
        return this;
    }

    public FileRunner command(String command) {
        this.command = command;
        return this;
    }

    public FileRunner phpArgs(String phpArgs) {
        this.phpArgs = phpArgs;
        return this;
    }

    public FileRunner fileArgs(String fileArgs) {
        this.fileArgs = fileArgs;
        return this;
    }

    public FileRunner workDir(String workDir) {
        this.workDir = workDir;
        return this;
    }

    @NbBundle.Messages({
        "# {0} - project or file name",
        "FileRunner.run.displayName={0} (run)"
    })
    public void run() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    getRunCallable(Bundle.FileRunner_run_displayName(getDisplayName()), false).call();
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        });
    }

    public void debug() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    debugInternal();
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        });
    }

    Callable<Cancellable> getRunCallable(final String displayName, final boolean debug) {
        return new Callable<Cancellable>() {
            @Override
            public Cancellable call() {
                assert !EventQueue.isDispatchThread();

                PhpExecutable executable = new PhpExecutable(command);
                if (StringUtils.hasText(workDir)) {
                    executable.workDir(new File(workDir));
                } else {
                    executable.workDir(file.getParentFile());
                }
                // open in browser or editor?
                Runnable postExecution = null;
                if (getRedirectToFile()) {
                    File tmpFile = createTempFile();
                    if (tmpFile != null) {
                        executable.fileOutput(tmpFile, false);
                        postExecution = new PostExecution(tmpFile);
                    }
                }
                executable
                        .displayName(displayName)
                        .viaAutodetection(false)
                        .viaPhpInterpreter(false)
                        .additionalParameters(getParams());
                if (debug) {
                    executable
                            .environmentVariables(Collections.singletonMap("XDEBUG_CONFIG", "idekey=" + PhpOptions.getInstance().getDebuggerSessionId())); // NOI18N
                }
                // run!
                final Future<Integer> result = executable
                        .run(getDescriptor(postExecution, !debug));
                return new Cancellable() {
                    @Override
                    public boolean cancel() {
                        return result.cancel(true);
                    }
                };
            }
        };
    }

    // XXX use php api executable for debugging
    @NbBundle.Messages({
        "# {0} - project or file name",
        "FileRunner.debug.displayName={0} (debug)"
    })
    private void debugInternal() {
        DebugStarter dbgStarter =  DebugStarterFactory.getInstance();
        assert dbgStarter != null;
        if (dbgStarter.isAlreadyRunning()) {
            if (CommandUtils.warnNoMoreDebugSession()) {
                dbgStarter.stop();
                debugInternal();
            }
        } else {
            Callable<Cancellable> callable = getRunCallable(Bundle.FileRunner_debug_displayName(getDisplayName()), true);
            DebugStarter.Properties props = new DebugStarter.Properties.Builder()
                    .setStartFile(FileUtil.toFileObject(file))
                    .setCloseSession(true)
                    // #209682 - "run as script" always from project files
                    .setPathMapping(Collections.<Pair<String, String>>emptyList())
                    .setDebugProxy(null) // no debug proxy for files (valid only for server urls)
                    .setEncoding(getEncoding())
                    .build();
            dbgStarter.start(project != null ? project : DUMMY_PROJECT, callable, props);
        }
    }

    synchronized String getDisplayName() {
        return project != null ? project.getName() : file.getName();
    }

    private List<String> getParams() {
        List<String> params = new ArrayList<String>();
        if (StringUtils.hasText(phpArgs)) {
            params.addAll(Arrays.asList(Utilities.parseParameters(phpArgs)));
        }
        params.add(file.getAbsolutePath());
        if (StringUtils.hasText(fileArgs)) {
            params.addAll(Arrays.asList(Utilities.parseParameters(fileArgs)));
        }
        return params;
    }

    ExecutionDescriptor getDescriptor(Runnable postExecution, boolean controllable) {
        ExecutionDescriptor descriptor = PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .charset(Charset.forName(getEncoding()))
                .controllable(controllable)
                .optionsPath(UiUtils.OPTIONS_PATH)
                .outConvertorFactory(PHP_LINE_CONVERTOR_FACTORY);
        if (!getPhpOptions().isOpenResultInOutputWindow()) {
            descriptor = descriptor.inputOutput(InputOutput.NULL)
                    .frontWindow(false)
                    .frontWindowOnError(false);
        }
        if (postExecution != null) {
            descriptor = descriptor.postExecution(postExecution);
        }
        return descriptor;
    }

    private String getEncoding() {
        return project != null ? ProjectPropertiesSupport.getEncoding(project) : FileEncodingQuery.getDefaultEncoding().name();
    }

    boolean getRedirectToFile() {
        return getPhpOptions().isOpenResultInBrowser() || getPhpOptions().isOpenResultInEditor();
    }

    File createTempFile() {
        try {
            File tmpFile = File.createTempFile(file.getName(), ".html"); // NOI18N
            tmpFile.deleteOnExit();
            return tmpFile;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }

    private PhpOptions getPhpOptions() {
        return PhpOptions.getInstance();
    }

    //~ Inner classes

    private static final class PhpLineConvertorFactory implements ExecutionDescriptor.LineConvertorFactory {

        @Override
        public LineConvertor newLineConvertor() {
            LineConvertor[] lineConvertors = new LineConvertor[PhpInterpreter.LINE_PATTERNS.length];
            int i = 0;
            for (Pattern linePattern : PhpInterpreter.LINE_PATTERNS) {
                lineConvertors[i++] = LineConvertors.filePattern(null, linePattern, null, 1, 2);
            }
            return LineConvertors.proxy(lineConvertors);
        }
    }

    private static final class PostExecution implements Runnable {

        private final File tmpFile;


        public PostExecution(File tmpFile) {
            this.tmpFile = tmpFile;
        }

        @Override
        public void run() {
            PhpOptions options = PhpOptions.getInstance();
            try {
                if (options.isOpenResultInBrowser()) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(Utilities.toURI(tmpFile).toURL());
                }
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            if (options.isOpenResultInEditor()) {
                PhpProjectUtils.openFile(tmpFile);
            }
        }

    }

    // needed for php debugger, used as a key in session map
    private static final class DummyProject implements Project {

        @Override
        public FileObject getProjectDirectory() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }

}
