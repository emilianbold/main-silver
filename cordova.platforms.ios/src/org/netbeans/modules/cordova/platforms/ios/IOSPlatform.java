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
package org.netbeans.modules.cordova.platforms.ios;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import org.netbeans.modules.cordova.platforms.Device;
import org.netbeans.modules.cordova.platforms.MobileDebugTransport;
import org.netbeans.modules.cordova.platforms.MobilePlatform;
import org.netbeans.modules.cordova.platforms.PlatformManager;
import org.netbeans.modules.cordova.platforms.ProcessUtils;
import org.netbeans.modules.cordova.platforms.SDK;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Becicka
 * 
 */
@ServiceProvider(service=MobilePlatform.class)
public class IOSPlatform implements MobilePlatform {

    private transient final java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    private String sdkLocation;
    
    public String getType() {
        return PlatformManager.IOS_TYPE;
    }
    
    public IOSPlatform() {
    }
    
    @Override
    public Collection<SDK> getSDKs()  {
        try {
            String listSdks = ProcessUtils.callProcess("xcodebuild", true, "-showsdks"); //NOI18N
            return IOSSDK.parse(listSdks);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.emptyList();
    }
    
    public void openUrl(Device device, String url) {
        try {
            String sim = InstalledFileLocator.getDefault().locate("bin/ios-sim", "org.netbeans.modules.cordova.platforms.ios", false).getPath();
            String a = ProcessUtils.callProcess(sim, false, "launch", "/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator6.0.sdk/Applications/MobileSafari.app", "--args", "-u", url); //NOI18N
            System.out.println(a);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public boolean isReady() {
        File f = new File("/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform");
        return f.exists();
    }
    
    @Override
    public String getSimulatorPath() {
        return InstalledFileLocator.getDefault().locate("bin/ios-sim", "org.netbeans.modules.cordova.platforms.ios", false).getPath();
    }

    @Override
    public Collection<Device> getConnectedDevices() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SDK getPrefferedTarget() {
        return getSDKs().iterator().next();
    }

    @Override
    public String getSdkLocation() {
        return sdkLocation;
    }


    @Override
    public void manageDevices() {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public void setSdkLocation(String sdkLocation) {
    }

    @Override
    public boolean waitEmulatorReady(int timeout) {
        return true;
    }

    @Override
    public MobileDebugTransport getDebugTransport() {
        return new IOSDebugTransport();
    }

    @Override
    public Device getDevice(String name, EditableProperties props) {
        return IOSDevice.IPHONE;
    }

    @Override
    public Collection<? extends Device> getVirtualDevices() throws IOException {
        return EnumSet.allOf(IOSDevice.class);
    }
}

