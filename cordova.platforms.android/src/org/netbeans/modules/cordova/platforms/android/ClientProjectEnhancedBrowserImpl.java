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

package org.netbeans.modules.cordova.platforms.android;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.BuildPerformer;
import org.netbeans.modules.cordova.platforms.Device;
import org.netbeans.modules.cordova.platforms.PropertyProvider;
import static org.netbeans.modules.cordova.platforms.android.AndroidBrowser.Kind.DEVICE_CHROME;
import static org.netbeans.modules.cordova.platforms.android.AndroidBrowser.Kind.DEVICE_DEFAULT;
import static org.netbeans.modules.cordova.platforms.android.AndroidBrowser.Kind.EMULATOR_DEFAULT;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.modules.web.clientproject.spi.platform.RefreshOnSaveListener;
import org.netbeans.spi.project.ActionProvider;
import static org.netbeans.spi.project.ActionProvider.COMMAND_BUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_CLEAN;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RUN;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RUN_SINGLE;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 * @author Jan Becicka
 */
public class ClientProjectEnhancedBrowserImpl implements ClientProjectEnhancedBrowserImplementation {

    final private Project project;
    private final WebBrowser browser;
    private static final Logger LOGGER = Logger.getLogger(ClientProjectEnhancedBrowserImpl.class.getName());

    ClientProjectEnhancedBrowserImpl(Project project, WebBrowser browser) {
        this.project = project;
        this.browser = browser;
    }

    @Override
    public void save() {
        // this should save changes in UI for particular configuration
    }

    @Override
    public RefreshOnSaveListener getRefreshOnSaveListener() {
        return new RefreshOnSaveListenerImpl();
    }

    @Override
    public ActionProvider getActionProvider() {
        return new ActionProviderImpl();
    }

    @Override
    public ProjectConfigurationCustomizer getProjectConfigurationCustomizer() {
        return null;
    }

    @Override
    public void deactivate() {
    }

    @Override
    public boolean isHighlightSelectionEnabled() {
        return true;
    }

    @Override
    public ProjectConfigurationProvider getProjectConfigurationProvider() {
        return null;
    }

    private class ActionProviderImpl implements ActionProvider {

        public ActionProviderImpl() {
        }

        @Override
        public String[] getSupportedActions() {
            return new String[]{
                COMMAND_RUN,
                COMMAND_RUN_SINGLE
            };
        }

        @Override
        public void invokeAction(String command, final Lookup context) throws IllegalArgumentException {
            final BuildPerformer build = Lookup.getDefault().lookup(BuildPerformer.class);
            String checkAndroid = AndroidActionProvider.checkAndroid();
            if (checkAndroid != null) {
                NotifyDescriptor not = new NotifyDescriptor(
                        checkAndroid,
                        Bundle.ERR_Title(),
                        NotifyDescriptor.OK_CANCEL_OPTION,
                        NotifyDescriptor.ERROR_MESSAGE,
                        null,
                        null);
                Object value = DialogDisplayer.getDefault().notify(not);
                if (NotifyDescriptor.CANCEL_OPTION != value) {
                    OptionsDisplayer.getDefault().open("Advanced/MobilePlatforms");
                }
                return;
            }

            if (COMMAND_RUN.equals(command) || COMMAND_RUN_SINGLE.equals(command)) {
                ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    @Override
                    public void run() {
                        String checkDevices = checkDevices(project);
                        while (checkDevices != null) {
                            NotifyDescriptor not = new NotifyDescriptor(
                                    checkDevices,
                                    Bundle.ERR_Title(),
                                    NotifyDescriptor.DEFAULT_OPTION,
                                    NotifyDescriptor.ERROR_MESSAGE,
                                    null,
                                    null);
                            Object value = DialogDisplayer.getDefault().notify(not);
                            if (NotifyDescriptor.CANCEL_OPTION == value) {
                                return;
                            } else {
                                checkDevices = checkDevices(project);
                            }
                        }
                        Browser b;
                        boolean emulator;
                        if (Bundle.LBL_DeviceDefault().equals(browser.getName())) {
                            b=Browser.DEFAULT;
                            emulator = false;
                        } else if (Bundle.LBL_DeviceChrome().equals(browser.getName())) {
                            b=Browser.CHROME;
                            emulator = false;
                        } else {
                            b=Browser.DEFAULT;
                            emulator = true;
                        }
                        Device device = new AndroidDevice("android", b, emulator);
                        
                        device.openUrl(build.getUrl(project, context));
                        if (Browser.CHROME.getName().equals(b)) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            try {
                                build.startDebugging(device, project);
                            } catch (IllegalStateException ex) {
                                LOGGER.log(Level.INFO, ex.getMessage(), ex);
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), Bundle.ERR_WebDebug());
                                    }
                                });
                            }
                        }

                    }

                    private String checkDevices(Project project) {
                        return null;
                    }
                }, Bundle.LBL_CheckingDevice(), new AtomicBoolean(), false);
            }
        }

        @Override
        public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
            return true;
        }
    }

}
