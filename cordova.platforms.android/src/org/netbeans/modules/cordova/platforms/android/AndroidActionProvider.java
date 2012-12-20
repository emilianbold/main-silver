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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.BuildPerformer;
import org.netbeans.modules.cordova.platforms.Device;
import org.netbeans.modules.cordova.platforms.PlatformManager;
import org.netbeans.modules.cordova.platforms.PropertyProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
@NbBundle.Messages({
    "ERR_Title=Error",
    "LBL_CheckingDevice=Checking android device..."
})
public class AndroidActionProvider implements ActionProvider {

    private final Project p;

    public AndroidActionProvider(Project p) {
        this.p = p;
    }
    
    @Override
    public String[] getSupportedActions() {
        return new String[]{
                    COMMAND_BUILD,
                    COMMAND_CLEAN,
                    COMMAND_RUN
                };
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        final BuildPerformer build = Lookup.getDefault().lookup(BuildPerformer.class);
        String checkAndroid = checkAndroid();
        if (checkAndroid!=null) {
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

            if (COMMAND_BUILD.equals(command)) {
            build.perform(build.BUILD_ANDROID,p);
        } else if (COMMAND_CLEAN.equals(command)) {
            build.perform(build.CLEAN_ANDROID, p);
        } else if (COMMAND_RUN.equals(command)) {
            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                @Override
                public void run() {
                    String checkDevices = checkDevices(p);
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
                            checkDevices = checkDevices(p);
                        }
                    }
                    if (build.isPhoneGapBuild(p)) {
                        build.perform(BuildPerformer.RUN_ANDROID, p);
                    } else {
                        PropertyProvider config = (PropertyProvider) p.getLookup().lookup(ProjectConfigurationProvider.class).getActiveConfiguration();
                        final Device device = config.getDevice();
                        device.openUrl(build.getUrl(p));
                        if (Browser.CHROME.getName().equals(config.getProperty(Device.BROWSER_PROP))) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            build.startDebugging(device, p);
                        }
                    }

                }
            }, Bundle.LBL_CheckingDevice(), new AtomicBoolean(), false);
        }
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return true;
    }

    @NbBundle.Messages({
            "ERR_ConnectAndroidDevice=Please connect Android device and enable USB debugging.",
            "ERR_RunAndroidEmulator=Please run Android Emulator.",
            "ERR_Unknown=Unknown Error."})
    private String checkDevices(Project p) {
        PropertyProvider config = (PropertyProvider) p.getLookup().lookup(ProjectConfigurationProvider.class).getActiveConfiguration();
        try {
            if (Device.DEVICE.equals(config.getProperty(Device.DEVICE_PROP))) { //NOI18N
                for (Device dev : PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE).getConnectedDevices()) {
                    if (!dev.isEmulator()) {
                        return null;
                    }
                }
                return Bundle.ERR_ConnectAndroidDevice();
            } else {
                for (Device dev : PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE).getConnectedDevices()) {
                    if (dev.isEmulator()) {
                        return null;
                    }
                }
                return Bundle.ERR_RunAndroidEmulator();
            }
        } catch (IOException iOException) {
            Exceptions.printStackTrace(iOException);
        }
        return Bundle.ERR_Unknown();
    }
    
    @NbBundle.Messages("ERR_AndroidNotConfigured=Android Platform is not configured.\nConfigure?")
    private String checkAndroid() {
        if (!PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE).isReady()) {
            return Bundle.ERR_AndroidNotConfigured();
        }
        return null;
    }
    
}
