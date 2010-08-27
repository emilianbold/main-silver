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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.URLWait;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.config.WLDatasource;
import org.netbeans.modules.j2ee.weblogic9.config.gen.WeblogicWebApp;
import org.netbeans.modules.j2ee.weblogic9.ui.FailedAuthenticationSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;


/**
 *
 * @author Petr Hejl
 */
// FIXME refactor exceution to some common method
public final class CommandBasedDeployer {

    private static final Logger LOGGER = Logger.getLogger(CommandBasedDeployer.class.getName());

    private static RequestProcessor DEPLOYMENT_RP = new RequestProcessor("Weblogic Deployment", 1); // NOI18N

    private static final RequestProcessor URL_WAIT_RP = new RequestProcessor("Weblogic URL Wait", 10); // NOI18N

    private static final String WEBLOGIC_JAR_PATH = "server/lib/weblogic.jar"; // NOI18N

    private static final int TIMEOUT = 300000;

    private static final boolean SHOW_CONSOLE = Boolean.getBoolean(CommandBasedDeployer.class.getName() + ".showConsole");

    private final WLDeploymentManager deploymentManager;

    public CommandBasedDeployer(WLDeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    public ProgressObject directoryDeploy(final Target target, String name,
            File file, String host, String port, J2eeModule.Type type) {
        return deploy(createModuleId(target, file, host, port, name, type),
                file, "-nostage", "-name", name, "-source"); // NOI18N
    }

    public ProgressObject directoryRedeploy(final TargetModuleID moduleId) {
        return redeploy(new TargetModuleID[] {moduleId});
    }

    public ProgressObject deploy(Target[] target, final File file, final File plan, String host, String port) {
        // TODO is this correct only first server mentioned
        final TargetModuleID moduleId = createModuleId(target[0], file, host, port, file.getName(), null);
        return deploy(moduleId, file);
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file, File file2) {
        return redeploy(targetModuleID, "-source", file.getAbsolutePath()); // NOI18N
    }

    public ProgressObject undeploy(final TargetModuleID[] targetModuleID) {
        final WLProgressObject progress = new WLProgressObject(targetModuleID);

        progress.fireProgressEvent(null, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.RUNNING,
                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Started")));

        DEPLOYMENT_RP.submit(new Runnable() {

            @Override
            public void run() {
                boolean failed = false;
                LastLineProcessor lineProcessor = new LastLineProcessor();
                for (TargetModuleID module : targetModuleID) {
                    String name = module.getModuleID();
                    ExecutionService service = createService("-undeploy", lineProcessor, "-name", name);
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.RUNNING,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeploying", name)));

                    Future<Integer> result = service.run();
                    try {
                        Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                        if (value.intValue() != 0) {
                            failed = true;
                            progress.fireProgressEvent(null, new WLDeploymentStatus(
                                    ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                                    NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Failed",
                                        lineProcessor.getLastLine())));
                            FailedAuthenticationSupport.checkFailedAuthentication(deploymentManager, lineProcessor.getLastLine());
                            break;
                        } else {
                            continue;
                        }
                    } catch (InterruptedException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Failed_Interrupted")));
                        result.cancel(true);
                        Thread.currentThread().interrupt();
                        break;
                    } catch (TimeoutException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Failed_Timeout")));
                        result.cancel(true);
                        break;
                    } catch (ExecutionException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Failed_With_Message")));
                        break;
                    }
                }
                if (!failed) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Completed")));
                }
            }
        });

        return progress;
    }

    public ProgressObject start(final TargetModuleID[] targetModuleID) {
        final WLProgressObject progress = new WLProgressObject(targetModuleID);

        progress.fireProgressEvent(null, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Started")));

        DEPLOYMENT_RP.submit(new Runnable() {

            @Override
            public void run() {
                boolean failed = false;
                LastLineProcessor lineProcessor = new LastLineProcessor();
                for (TargetModuleID module : targetModuleID) {
                    String name = module.getModuleID();
                    ExecutionService service = createService("-start", lineProcessor, "-name", name);
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Starting", name)));

                    Future<Integer> result = service.run();
                    try {
                        Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                        if (value.intValue() != 0) {
                            failed = true;
                            progress.fireProgressEvent(null, new WLDeploymentStatus(
                                    ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                                    NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Failed",
                                        lineProcessor.getLastLine())));
                            FailedAuthenticationSupport.checkFailedAuthentication(deploymentManager, lineProcessor.getLastLine());
                            break;
                        } else {
                            waitForUrlReady(module, progress);
                            continue;
                        }
                    } catch (InterruptedException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Failed_Interrupted")));
                        result.cancel(true);
                        Thread.currentThread().interrupt();
                        break;
                    } catch (TimeoutException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Failed_Timeout")));
                        result.cancel(true);
                        break;
                    } catch (ExecutionException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Failed_With_Message")));
                        break;
                    }
                }
                if (!failed) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Completed")));
                }
            }
        });

        return progress;
    }

    public ProgressObject stop(final TargetModuleID[] targetModuleID) {
        final WLProgressObject progress = new WLProgressObject(targetModuleID);

        progress.fireProgressEvent(null, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Started")));

        DEPLOYMENT_RP.submit(new Runnable() {

            @Override
            public void run() {
                boolean failed = false;
                LastLineProcessor lineProcessor = new LastLineProcessor();
                for (TargetModuleID module : targetModuleID) {
                    String name = module.getModuleID();
                    ExecutionService service = createService("-stop", lineProcessor, "-name", name);
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stopping", name)));

                    Future<Integer> result = service.run();
                    try {
                        Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                        if (value.intValue() != 0) {
                            failed = true;
                            progress.fireProgressEvent(null, new WLDeploymentStatus(
                                    ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                                    NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Failed",
                                        lineProcessor.getLastLine())));
                            FailedAuthenticationSupport.checkFailedAuthentication(deploymentManager, lineProcessor.getLastLine());
                            break;
                        } else {
                            continue;
                        }
                    } catch (InterruptedException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Failed_Interrupted")));
                        result.cancel(true);
                        Thread.currentThread().interrupt();
                        break;
                    } catch (TimeoutException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Failed_Timeout")));
                        result.cancel(true);
                        break;
                    } catch (ExecutionException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Failed_With_Message")));
                        break;
                    }
                }
                if (!failed) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.STOP, StateType.COMPLETED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Completed")));
                }
            }
        });

        return progress;
    }

    public ProgressObject deployDatasource(final Collection<WLDatasource> datasources) {
        final WLProgressObject progress = new WLProgressObject(new TargetModuleID[0]);

        progress.fireProgressEvent(null, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Datasource_Started")));

        DEPLOYMENT_RP.submit(new Runnable() {

            @Override
            public void run() {
                boolean failed = false;
                LastLineProcessor lineProcessor = new LastLineProcessor();
                for (WLDatasource datasource: datasources) {
                    if (datasource.getOrigin() == null) {
                        LOGGER.log(Level.INFO, "Could not deploy {0}", datasource.getName());
                        continue;
                    }
                    ExecutionService service = createService("-deploy", lineProcessor, "-name",
                            datasource.getName(), "-upload", datasource.getOrigin().getAbsolutePath());
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Datasource_Deploying", datasource.getName())));

                    Future<Integer> result = service.run();
                    try {
                        Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                        if (value.intValue() != 0) {
                            failed = true;
                            progress.fireProgressEvent(null, new WLDeploymentStatus(
                                    ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                                    NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Datasource_Failed",
                                        lineProcessor.getLastLine())));
                            FailedAuthenticationSupport.checkFailedAuthentication(deploymentManager, lineProcessor.getLastLine());
                            break;
                        } else {
                            continue;
                        }
                    } catch (InterruptedException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Datasource_Failed_Interrupted")));
                        result.cancel(true);
                        Thread.currentThread().interrupt();
                        break;
                    } catch (TimeoutException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Datasource_Failed_Timeout")));
                        result.cancel(true);
                        break;
                    } catch (ExecutionException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Datasource_Failed_With_Message")));
                        break;
                    }
                }
                if (!failed) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Datasource_Completed")));
                }
            }
        });

        return progress;
    }

    public ProgressObject deployLibraries(final Set<File> libraries) {
        final WLProgressObject progress = new WLProgressObject(new TargetModuleID[0]);

        progress.fireProgressEvent(null, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Started")));

        DEPLOYMENT_RP.submit(new Runnable() {

            @Override
            public void run() {
                boolean failed = false;
                LastLineProcessor lineProcessor = new LastLineProcessor();
                for (File library : libraries) {
                    ExecutionService service = createService("-deploy", lineProcessor,
                            "-library" , library.getAbsolutePath()); // NOI18N
                    Future<Integer> result = service.run();
                    try {
                        Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                        if (value.intValue() != 0) {
                            failed = true;
                            progress.fireProgressEvent(null, new WLDeploymentStatus(
                                    ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                    NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Failed",
                                        lineProcessor.getLastLine())));
                            FailedAuthenticationSupport.checkFailedAuthentication(deploymentManager, lineProcessor.getLastLine());
                            break;
                        } else {
                            continue;
                        }
                    } catch (InterruptedException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Failed_Interrupted")));
                        result.cancel(true);
                        Thread.currentThread().interrupt();
                        break;
                    } catch (TimeoutException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Failed_Timeout")));
                        result.cancel(true);
                        break;
                    } catch (ExecutionException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Failed_With_Message")));
                        break;
                    }
                }
                if (!failed) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Completed")));
                }
            }
        });

        return progress;
    }

    private ProgressObject deploy(final TargetModuleID moduleId, final File file,
            final String... parameters) {
        final WLProgressObject progress = new WLProgressObject(moduleId);

        progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deploying", file.getAbsolutePath())));

        DEPLOYMENT_RP.submit(new Runnable() {

            @Override
            public void run() {
                String[] execParams = new String[parameters.length + 1];
                execParams[execParams.length - 1] = file.getAbsolutePath();
                if (parameters.length > 0) {
                    System.arraycopy(parameters, 0, execParams, 0, parameters.length);
                }

                LastLineProcessor lineProcessor = new LastLineProcessor();
                ExecutionService service = createService("-deploy", lineProcessor, execParams); // NOI18N
                Future<Integer> result = service.run();
                try {
                    Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                    if (value.intValue() != 0) {
                        progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Failed",
                                    lineProcessor.getLastLine())));
                        FailedAuthenticationSupport.checkFailedAuthentication(deploymentManager, lineProcessor.getLastLine());
                    } else {
                        //waitForUrlReady(factory, moduleId, progress);
                        progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Completed")));
                    }
                } catch (InterruptedException ex) {
                    progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Failed_Interrupted")));
                    result.cancel(true);
                    Thread.currentThread().interrupt();
                } catch (TimeoutException ex) {
                    progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Failed_Timeout")));
                    result.cancel(true);
                } catch (ExecutionException ex) {
                    progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Failed_With_Message")));
                }
            }
        });

        return progress;
    }

    // FIXME we should check the source of module if it differs this should do undeploy/deploy
    private ProgressObject redeploy(final TargetModuleID[] targetModuleID, final String... parameters) {
        final WLProgressObject progress = new WLProgressObject(targetModuleID);

        progress.fireProgressEvent(null, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Started")));

        DEPLOYMENT_RP.submit(new Runnable() {

            @Override
            public void run() {
                boolean failed = false;
                LastLineProcessor lineProcessor = new LastLineProcessor();
                for (TargetModuleID module : targetModuleID) {
                    String name = module.getModuleID();
                    String[] execParams = new String[parameters.length + 2];
                    execParams[0] = "-name"; // NOI18N
                    execParams[1] = name;
                    if (parameters.length > 0) {
                        System.arraycopy(parameters, 0, execParams, 2, parameters.length);
                    }
                    ExecutionService service = createService("-redeploy", lineProcessor, execParams); // NOI18N
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeploying", name)));

                    Future<Integer> result = service.run();
                    try {
                        Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                        if (value.intValue() != 0) {
                            failed = true;
                            progress.fireProgressEvent(module, new WLDeploymentStatus(
                                    ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                    NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Failed",
                                        lineProcessor.getLastLine())));
                            FailedAuthenticationSupport.checkFailedAuthentication(deploymentManager, lineProcessor.getLastLine());
                            break;
                        } else {
                            //waitForUrlReady(factory, moduleId, progress);
                            continue;
                        }
                    } catch (InterruptedException ex) {
                        failed = true;
                        progress.fireProgressEvent(module, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Failed_Interrupted")));
                        result.cancel(true);
                        Thread.currentThread().interrupt();
                        break;
                    } catch (TimeoutException ex) {
                        failed = true;
                        progress.fireProgressEvent(module, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Failed_Timeout")));
                        result.cancel(true);
                        break;
                    } catch (ExecutionException ex) {
                        failed = true;
                        progress.fireProgressEvent(module, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Failed_With_Message")));
                        break;
                    }
                }
                if (!failed) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Completed")));
                }
            }
        });

        return progress;
    }

    private ExecutionService createService(final String command,
            final LineProcessor processor, String... parameters) {

        InstanceProperties ip = deploymentManager.getInstanceProperties();
        String username = ip.getProperty(InstanceProperties.USERNAME_ATTR);
        String password = ip.getProperty(InstanceProperties.PASSWORD_ATTR);

        String uri = ip.getProperty(InstanceProperties.URL_ATTR);
        // it is guaranteed it is WL
        String[] parts = uri.substring(WLDeploymentFactory.URI_PREFIX.length()).split(":");

        String host = parts[0];
        String port = parts.length > 1 ? parts[1] : "";

        ExternalProcessBuilder builder = new ExternalProcessBuilder(getJavaBinary())
                .redirectErrorStream(true)
                .addArgument("-cp") // NOI18N
                .addArgument(getClassPath())
                .addArgument("weblogic.Deployer") // NOI18N
                .addArgument("-adminurl") // NOI18N
                .addArgument("t3://" + host + ":" + port) // NOI18N
                .addArgument("-username") // NOI18N
                .addArgument(username)
                .addArgument("-password") // NOI18N
                .addArgument(password)
                .addArgument(command);

        for (String param : parameters) {
            builder = builder.addArgument(param);
        }

        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputVisible(true).outLineBased(true);
        if (processor != null) {
            descriptor = descriptor.outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {

                public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                    return InputProcessors.proxy(defaultProcessor, InputProcessors.bridge(processor));
                }
            });
        }
        if (!SHOW_CONSOLE) {
            descriptor = descriptor.inputOutput(InputOutput.NULL);
        }
        return ExecutionService.newService(builder, descriptor, "weblogic.Deployer " + command);
    }

    private String getClassPath() {
        InstanceProperties ip = deploymentManager.getInstanceProperties();
        String serverRoot = ip.getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
        if (serverRoot != null) {
            File file = new File(serverRoot, WEBLOGIC_JAR_PATH);
            if (file.exists() && file.isFile()) {
                return file.getAbsolutePath();
            }
        }
        return "";
    }

    private String getJavaBinary() {
        // TODO configurable ? or use the jdk server is running on ?
        JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
        Collection<FileObject> folders = platform.getInstallFolders();
        String javaBinary = "java"; // NOI18N
        if (folders.size() > 0) {
            FileObject folder = folders.iterator().next();
            File file = FileUtil.toFile(folder);
            if (file != null) {
                javaBinary = file.getAbsolutePath() + File.separator
                        + "bin" + File.separator + "java"; // NOI18N
            }
        }
        return javaBinary;
    }

    private static void waitForUrlReady(TargetModuleID moduleID,
            WLProgressObject progressObject) throws InterruptedException, TimeoutException {

        String webUrl = moduleID.getWebURL();
        if (webUrl == null) {
            TargetModuleID[] ch = moduleID.getChildTargetModuleID();
            if (ch != null) {
                for (int i = 0; i < ch.length; i++) {
                    webUrl = ch[i].getWebURL();
                    if (webUrl != null) {
                        break;
                    }
                }
            }

        }
        waitForUrlReady(webUrl, progressObject);
    }

    private static void waitForUrlReady(String webUrl,
            WLProgressObject progressObject) throws InterruptedException, TimeoutException {

        if (webUrl != null) {
            try {
                URL url = new URL(webUrl);
                String waitingMsg = NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Waiting_For_Url", url);

                progressObject.fireProgressEvent(null,
                        new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, waitingMsg));
                //delay to prevent hitting the old content before reload
                for (int i = 0; i < 3; i++) {
                    Thread.sleep(1000);
                }
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < TIMEOUT) {
                    if (URLWait.waitForUrlReady(URL_WAIT_RP, url, 1000)) {
                        break;
                    }
                }
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.INFO, "Malformed URL {0}", webUrl);
            }
        }
    }

    private static WLTargetModuleID createModuleId(Target target, File file,
            String host, String port, String name, J2eeModule.Type type) {

        WLTargetModuleID moduleId = new WLTargetModuleID(target, name);

        try {
            String serverUrl = "http://" + host + ":" + port;

            // TODO in fact we should look to deployment plan for overrides
            // for now it is as good as previous solution
            if (J2eeModule.Type.WAR.equals(type) || (type == null && file.getName().endsWith(".war"))) { // NOI18N
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
                if (fo != null) {
                    configureWarModuleId(moduleId, fo, serverUrl);
                }
            } else if (J2eeModule.Type.EAR.equals(type) || (type == null && file.getName().endsWith(".ear"))) { // NOI18N
                configureEarModuleId(moduleId, file, serverUrl);
            }

        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
        }

        return moduleId;
    }

    private static void configureEarModuleId(WLTargetModuleID moduleId, File file, String serverUrl) {
        try {
            FileObject root = null;
            if (file.isDirectory()) {
                root = FileUtil.toFileObject(FileUtil.normalizeFile(file));
            } else {
                JarFileSystem jfs = new JarFileSystem();
                jfs.setJarFile(file);
                root = jfs.getRoot();
            }
            if (root == null) {
                return;
            }
            
            FileObject appXml = root.getFileObject("META-INF/application.xml"); // NOI18N
            if (appXml != null) {
                Application ear = DDProvider.getDefault().getDDRoot(appXml);
                Module[] modules = ear.getModule();
                for (int i = 0; i < modules.length; i++) {
                    WLTargetModuleID childModuleId = new WLTargetModuleID(moduleId.getTarget());
                    if (modules[i].getWeb() != null) {
                        childModuleId.setContextURL(serverUrl + modules[i].getWeb().getContextRoot());
                    }
                    moduleId.addChild(childModuleId);
                }
            } else {
                // Java EE 5
                for (FileObject child : root.getChildren()) {
                    // this should work for exploded directory as well
                    if (child.hasExt("war") || child.hasExt("jar")) { // NOI18N
                        WLTargetModuleID childModuleId = new WLTargetModuleID(moduleId.getTarget());

                        if (child.hasExt("war")) { // NOI18N
                            configureWarModuleId(childModuleId, child, serverUrl);
                        }
                        moduleId.addChild(childModuleId);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (PropertyVetoException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    private static void configureWarModuleId(WLTargetModuleID moduleId, FileObject file, String serverUrl) {
        if (file.isFolder()) {
            FileObject weblogicXml = file.getFileObject("WEB-INF/weblogic.xml"); // NOI18N
            if (weblogicXml != null && weblogicXml.isData()) {
                try {
                    InputStream is = new BufferedInputStream(weblogicXml.getInputStream());
                    try {
                        String[] ctx = WeblogicWebApp.createGraph(is).getContextRoot();
                        if (ctx != null && ctx.length > 0) {
                            moduleId.setContextURL(serverUrl + ctx[0]);
                            return;
                        }
                    } finally {
                        is.close();
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
            moduleId.setContextURL(serverUrl + "/" + file.getNameExt()); // NOI18N
        } else {
            try {
                String contextRoot = "/" + file.getName(); // NOI18N
                ZipInputStream zis = new ZipInputStream(file.getInputStream());
                try {
                    ZipEntry entry = null;
                    while ((entry = zis.getNextEntry()) != null) {
                        if ("WEB-INF/weblogic.xml".equals(entry.getName())) { // NOI18N
                            String[] ddContextRoots =
                                    WeblogicWebApp.createGraph(new ZipEntryInputStream(zis)).getContextRoot();
                            if (ddContextRoots != null && ddContextRoots.length > 0) {
                                contextRoot = ddContextRoots[0];
                            }
                            break;
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, "Error reading context-root", ex); // NOI18N
                } finally {
                    zis.close();
                }

                moduleId.setContextURL(serverUrl + contextRoot);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }            
        }
    }

    private static class LastLineProcessor implements LineProcessor {

        private static final Pattern STACK_TRACE_PATTERN = Pattern.compile("^\\s+at.*$"); // NOI18N

        private String last = "";

        @Override
        public synchronized void processLine(String line) {
            if (line.length() != 0 && !STACK_TRACE_PATTERN.matcher(line).matches()) {
                last = line;
            }
        }

        public synchronized String getLastLine() {
            return last;
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {
        }
    }

    private static class ZipEntryInputStream extends InputStream {

        private final ZipInputStream zis;

        public ZipEntryInputStream(ZipInputStream zis) {
            this.zis = zis;
        }

        @Override
        public int available() throws IOException {
            return zis.available();
        }

        @Override
        public void close() throws IOException {
            zis.closeEntry();
        }

        @Override
        public int read() throws IOException {
            if (available() > 0) {
                return zis.read();
            }
            return -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return zis.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return zis.skip(n);
        }
    }

}
