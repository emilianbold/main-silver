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

package org.netbeans.modules.php.project.deprecated;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.util.Utilities;

/**
 * Base class for all PHP based programs (scripts).
 * @author Tomas Mysik
 */
public abstract class PhpProgram {
    private static final ExecutionDescriptor DEFAULT_DESCRIPTOR = new ExecutionDescriptor()
            .controllable(true)
            .frontWindow(true)
            .frontWindowOnError(true)
            .inputVisible(true)
            .showProgress(true);

    /**
     * The {@link InputProcessorFactory input processor factory} that strips any
     * <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">ANSI escape sequences</a>.
     * <p>
     * <b>In fact, it is not needed anymore since the Output window understands ANSI escape sequences.</b>
     * @see InputProcessors#ansiStripping(InputProcessor)
     */
    public static final InputProcessorFactory ANSI_STRIPPING_FACTORY = new InputProcessorFactory() {
        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.ansiStripping(defaultProcessor);
        }
    };

    protected static final Logger LOGGER = Logger.getLogger(PhpProgram.class.getName());
    private static final String[] NO_PARAMETERS = new String[0];

    private final String program;
    private final String[] parameters;
    private final String fullCommand;

    /**
     * Parse command which can be just binary or binary with parameters.
     * As a parameter separator, "-" or "/" is used.
     * @param command command to parse, can be <code>null</code>.
     */
    public PhpProgram(String command) {
        if (command == null) {
            // avoid NPE
            command = ""; // NOI18N
        }

        // try to find program (search for " -" or " /" after space)
        String[] tokens = command.split(" * (?=\\-|/)", 2); // NOI18N
        switch (tokens.length) {
            case 1:
                LOGGER.fine("Only program given (no parameters)");

                program = tokens[0].trim();
                parameters = NO_PARAMETERS;
                fullCommand = program;
                break;

            default:
                assert tokens.length > 1;
                program = tokens[0].trim();
                parameters = Utilities.parseParameters(tokens[1].trim());
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format("Parameters parsed: %s => %s", tokens[1].trim(), Arrays.asList(parameters)));
                }
                fullCommand = command.trim();
                break;
        }
    }

    /**
     * Get PHP program, never <code>null</code>.
     * @return PHP program, never <code>null</code>.
     */
    public final String getProgram() {
        return program;
    }

    /**
     * Get parameters, can be an empty array but never <code>null</code>.
     * @return parameters, can be an empty array but never <code>null</code>.
     */
    public final String[] getParameters() {
        return parameters.clone();
    }

    /**
     * Get the full command, in the original form (just without leading and trailing whitespaces).
     * @return the full command, in the original form (just without leading and trailing whitespaces).
     */
    public final String getFullCommand() {
        return fullCommand;
    }

    /**
     * Return <code>true</code> if program is {@link #validate() valid), <code>false</code> otherwise.
     * @return <code>true</code> if program is {@link #validate() valid), <code>false</code> otherwise.
     * @see #validate()
     * @see InvalidPhpProgramException
     */
    public final boolean isValid() {
        return validate() == null;
    }

    /**
     * Get the error message if the PHP program is not valid or <code>null</code> if it's valid.
     * @return the error message if the PHP program is not valid or <code>null</code> if it's valid.
     * @see #isValid()
     * @see InvalidPhpProgramException
     */
    public abstract String validate();

    /**
     * Get {@link ExternalProcessBuilder process builder} with {@link #getProgram() program}
     * and {@link #getParameters() parameters}.
     * @return {@link ExternalProcessBuilder process builder} with {@link #getProgram() program}
     *         and {@link #getParameters() parameters}.
     */
    public ExternalProcessBuilder getProcessBuilder() {
        // XXX possibility to run via php interpreter
        // XXX possibility to set work dir
        ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(program);
        for (String param : parameters) {
            processBuilder = processBuilder.addArgument(param);
        }
        return processBuilder;
    }

    /**
     * Get the {@link ExecutionDescriptor execution descriptor}. This descriptor is:
     * <ul>
     *   <li>{@link ExecutionDescriptor#isControllable() controllable}</li>
     *   <li>{@link ExecutionDescriptor#isFrontWindow() displays the Output window}</li>
     *   <li>{@link ExecutionDescriptor#isFrontWindowOnError()  displays the Output window on error (since 1.62)}</li>
     *   <li>{@link ExecutionDescriptor#isInputVisible() has visible user input}</li>
     *   <li>{@link ExecutionDescriptor#showProgress() shows progress}</li>
     * </ul>
     * @return the default {@link ExecutionDescriptor execution descriptor}.
     */
    public static ExecutionDescriptor getExecutionDescriptor() {
        // XXX not static
        // XXX input not visible by default?
        return DEFAULT_DESCRIPTOR;
    }

    /**
     * Execute process, <b>non-blocking</b>. It is just a wrapper for {@link ExecutionService#run()}.
     * @param processBuilder {@link ExternalProcessBuilder process builder}
     * @param executionDescriptor {@link ExecutionDescriptor descriptor} describing the configuration of service
     * @param title display name of this service
     * @return task representing the actual run, value representing result
     *         of the {@link Future} is exit code of the process
     * @see #executeAndWait(ExternalProcessBuilder, ExecutionDescriptor, String)
     * @see #execute(ExternalProcessBuilder, ExecutionDescriptor, String, String)
     * @see ExecutionService#run()
     */
    public static Future<Integer> executeLater(ExternalProcessBuilder processBuilder, ExecutionDescriptor executionDescriptor, String title) {
        return ExecutionService.newService(processBuilder, executionDescriptor, title).run();
    }

    /**
     * Execute process, <b>blocking</b>. It is just a wrapper for {@link ExecutionService#run()} which waits for the return code.
     * @param processBuilder {@link ExternalProcessBuilder process builder}
     * @param executionDescriptor {@link ExecutionDescriptor descriptor} describing the configuration of service
     * @param title display name of this service
     * @return exit code of the process
     * @throws CancellationException if the process was cancelled
     * @throws ExecutionException if the process throws any exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @see #executeLater(ExternalProcessBuilder, ExecutionDescriptor, String)
     * @see #execute(ExternalProcessBuilder, ExecutionDescriptor, String, String)
     */
    public static int executeAndWait(ExternalProcessBuilder processBuilder, ExecutionDescriptor executionDescriptor, String title) throws ExecutionException, InterruptedException {
        return ExecutionService.newService(processBuilder, executionDescriptor, title).run().get();
    }

    /**
     * Execute process, <b>blocking but not blocking EDT</b>. It is just a wrapper for {@link ExecutionService#run()} which waits for the return code and displays
     * progress dialog if it is run in EDT.
     * <p>
     * {@link ExecutionException} is logged with INFO level, {@link InterruptedException} is simply propagated.
     * @param processBuilder {@link ExternalProcessBuilder process builder}
     * @param executionDescriptor {@link ExecutionDescriptor descriptor} describing the configuration of service
     * @param title display name of this service
     * @param progressMessage message displayed if the task is run in EDT
     * @return exit code of the process or {@code null} if any error occured
     * @see #executeLater(ExternalProcessBuilder, ExecutionDescriptor, String)
     * @see #executeAndWait(ExternalProcessBuilder, ExecutionDescriptor, String)
     */
    public static Integer execute(ExternalProcessBuilder processBuilder, ExecutionDescriptor executionDescriptor, String title, String progressMessage) {
        ExecutionService service = ExecutionService.newService(processBuilder, executionDescriptor, title);
        final Future<Integer> result = service.run();
        if (SwingUtilities.isEventDispatchThread()) {
            if (!result.isDone()) {
                try {
                    // let's wait in awt to avoid flashing dialogs
                    getResult(result, 99L);
                } catch (TimeoutException ex) {
                    ProgressUtils.showProgressDialogAndRun(new Runnable() {
                        @Override
                        public void run() {
                            getResult(result);
                        }
                    }, progressMessage);
                }
            }
        }
        return getResult(result);
    }

    static Integer getResult(Future<Integer> result) {
        try {
            return getResult(result, null);
        } catch (TimeoutException ex) {
            // in fact, cannot happen since we don't use timeout
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }

    static Integer getResult(Future<Integer> result, Long timeout) throws TimeoutException {
        try {
            if (timeout != null) {
                return result.get(timeout, TimeUnit.MILLISECONDS);
            }
            return result.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            // ignored
            LOGGER.log(Level.INFO, null, ex);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [program: "); // NOI18N
        sb.append(program);
        sb.append(", parameters: "); // NOI18N
        sb.append(Arrays.asList(parameters));
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    /**
     * Exception which can be used if a PHP program is not valid.
     * @see PhpProgram#isValid()
     * @see PhpProgram#validate()
     */
    public static final class InvalidPhpProgramException extends Exception {
        private static final long serialVersionUID = -831989756418354L;

        public InvalidPhpProgramException(String message) {
            super(message);
        }
    }
}
