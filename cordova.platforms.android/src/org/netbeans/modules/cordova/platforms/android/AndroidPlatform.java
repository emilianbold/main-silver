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

import org.netbeans.modules.cordova.platforms.MobilePlatform;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.SwingUtilities;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.cordova.platforms.Device;
import org.netbeans.modules.cordova.platforms.PlatformManager;
import org.netbeans.modules.cordova.platforms.ProcessUtils;
import org.netbeans.modules.cordova.platforms.ProvisioningProfile;
import org.netbeans.modules.cordova.platforms.SDK;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service=MobilePlatform.class)
public class AndroidPlatform implements MobilePlatform {
    
    private static String ANDROID_SDK_ROOT_PREF = "android.sdk.home"; //NOI18N
    
    public static int DEFAULT_TIMEOUT = 30000;

    private transient final java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    
    public AndroidPlatform() {
    }

//    public void createProject(File dir, String targetId, String projectName, String activityName, String packageName) throws IOException {
//        ProcessBuilder pb = ProcessBuilder.getLocal();
//        pb.setExecutable(getSdkLocation() + "/tools/android");
//        pb.setArguments(
//        Arrays.asList(
//                "create", "project", 
//                "--target", targetId,
//                "--name", projectName,
//                "--path", dir.getPath(),
//                "--activity", activityName,
//                "--package", packageName
//                ));
//        pb.setWorkingDirectory(dir.getParentFile().getAbsolutePath());
//        try {
//            Process call = pb.call();
//            call.waitFor();
//            InputStreamReader inputStreamReader = new InputStreamReader(new BufferedInputStream(call.getErrorStream()));
//            if (call.exitValue() != 0) {
//                StringBuilder error = new StringBuilder();
//                char[] ch = new char[1];
//                while (inputStreamReader.ready()) {
//                    inputStreamReader.read(ch);
//                    error.append(ch);
//                }
//                throw new IOException(error.toString());
//            }
//        } catch (InterruptedException ex) {
//            throw new IOException(ex);
//        }
//    }
    
    @Override
    public Collection<Device> getVirtualDevices() throws IOException {
        assert !SwingUtilities.isEventDispatchThread();
        String avdString = ProcessUtils.callProcess(getAndroidCommand(), true, AndroidPlatform.DEFAULT_TIMEOUT, "list", "avd"); //NOI18N
        return AVD.parse(avdString);
    }
    
    private String getAndroidCommand() {
        if (Utilities.isWindows()) {
            return getSdkLocation() + "\\tools\\android.bat";
        } else {
            return getSdkLocation() + "/tools/android";
        }
    }
    
    String getAdbCommand() {
        if (Utilities.isWindows()) {
            return getSdkLocation() + "\\platform-tools\\adb.exe";
        } else {
            return getSdkLocation() + "/platform-tools/adb";

        }
    }
    

    @Override
    public Collection<SDK> getSDKs() throws IOException {
        //assert !SwingUtilities.isEventDispatchThread();
        String avdString = ProcessUtils.callProcess(getAndroidCommand(), true, 30000, "list", "target");//NOI18N
        return Target.parse(avdString);
    }
    
    private final HashSet<String> targets = new HashSet<String>(Arrays.asList(new String[]{
            "android-7", //NOI18N
            "android-8", //NOI18N
            "android-9", //NOI18N
            "android-10", //NOI18N
            "android-11", //NOI18N
            "android-12", //NOI18N
            "android-13", //NOI18N
            "android-14", //NOI18N
            "android-15", //NOI18N
            "android-16"})); //NOI18N
    
    
    @Override
    public SDK getPrefferedTarget() {
        try {
            final Collection<SDK> targets1 = getSDKs();
            for (SDK t: targets1) {
                if (targets.contains(t.getName())) {
                    return t;
                }
            }
            return targets1.iterator().next();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    
    @Override
    public Collection<org.netbeans.modules.cordova.platforms.Device> getConnectedDevices() throws IOException {
        //assert !SwingUtilities.isEventDispatchThread();
        String avdString = ProcessUtils.callProcess(getAdbCommand(), true, AndroidPlatform.DEFAULT_TIMEOUT, "devices"); //NOI18N
        Collection<org.netbeans.modules.cordova.platforms.Device> devices = AndroidDevice.parse(avdString);
        if (devices.isEmpty()) {
            //maybe adb is just down. try to restart adb
            ProcessUtils.callProcess(getAdbCommand(), true, AndroidPlatform.DEFAULT_TIMEOUT, "kill-server"); //NOI18N
            ProcessUtils.callProcess(getAdbCommand(), true, AndroidPlatform.DEFAULT_TIMEOUT, "start-server"); //NOI18N
        }
        avdString = ProcessUtils.callProcess(getAdbCommand(), true, AndroidPlatform.DEFAULT_TIMEOUT, "devices"); //NOI18N
        devices = AndroidDevice.parse(avdString);
        return devices;
    }
    
    
    public ExecutorTask buildProject(File dir, String... targets) throws IOException {
        File build = new File(dir.getAbsolutePath() + File.separator + "build.xml"); //NOI18N
        FileObject buildFo = FileUtil.toFileObject(build);
        return ActionUtils.runTarget(buildFo, targets, null);
    }
    
    /**
     * Deletes dir and all subdirectories/files!
     * @param dir
     * @throws IOException 
     */
    public void cleanProject(File dir) throws IOException {
        FileUtil.toFileObject(dir).delete();
    }

    @Override
    public String getSdkLocation() {
        return NbPreferences.forModule(AndroidPlatform.class).get(ANDROID_SDK_ROOT_PREF, null);
    }

    @Override
    public void setSdkLocation(String sdkLocation) {
        NbPreferences.forModule(AndroidPlatform.class).put(ANDROID_SDK_ROOT_PREF, sdkLocation);
        propertyChangeSupport.firePropertyChange("SDK", null, sdkLocation);//NOI18N
    }
    
    @Override
    public boolean waitEmulatorReady(int timeout) {
        try {
            return RequestProcessor.getDefault().invokeAny(Collections.singleton(new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        return waitEmulatorReady();
                    }
                }), timeout, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
        } catch (TimeoutException ex) {
        }
        return false;
        
    }
    
    private boolean waitEmulatorReady() {
        try {
            String value;
            for(;;) {
                value = ProcessUtils.callProcess(
                        getAdbCommand(), 
                        true, 
                        -1, 
                        "-e", 
                        "wait-for-device", 
                        "shell", 
                        "getprop", 
                        "init.svc.bootanim"); //NOI18N
                if ("stopped".equals(value.trim())) { //NOI18N
                    return true;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } 
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
        
    }

    @Override
    public void manageDevices() {
        assert !SwingUtilities.isEventDispatchThread();
        try {
            ProcessUtils.callProcess(getAndroidCommand(), true, -1, "avd"); //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public boolean isReady() {
        return getSdkLocation() != null && !getSdkLocation().isEmpty();
    }
    
    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    @Override
    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener ) {
        propertyChangeSupport.addPropertyChangeListener( listener );
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener ) {
        propertyChangeSupport.removePropertyChangeListener( listener );
    }


    @Override
    public String getType() {
        return PlatformManager.ANDROID_TYPE;
    }

    @Override
    public String getSimulatorPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Device getDevice(String name, EditableProperties props) {
        return AndroidDevice.get(name, props);
    }

    @Override
    public String getCodeSignIdentity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getProvisioningProfilePath() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCodeSignIdentity(String identity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setProvisioningProfilePath(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<? extends ProvisioningProfile> getProvisioningProfiles() {
        return Collections.emptyList();
    }
}

