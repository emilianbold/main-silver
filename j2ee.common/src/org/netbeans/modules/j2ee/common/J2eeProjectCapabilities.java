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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.common;

import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.specs.support.api.JpaSupport;
import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 * Facade allowing queries for certain capabilities provided by Java EE runtime.
 *
 * @author Petr Hejl
 * @since 1.58
 */
public final class J2eeProjectCapabilities {

    private final J2eeModuleProvider provider;
    private final Profile ejbJarProfile;
    private final Profile webProfile;

    private J2eeProjectCapabilities(J2eeModuleProvider provider,
            Profile ejbJarProfile, Profile webProfile) {
        this.provider = provider;
        this.ejbJarProfile = ejbJarProfile;
        this.webProfile = webProfile;
    }

    @CheckForNull
    public static J2eeProjectCapabilities forProject(@NonNull Project project) {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (provider == null) {
            return null;
        }
        Profile ejbJarProfile = null;
        Profile webProfile = null;
        if (provider.getJ2eeModule().getType() == J2eeModule.Type.EJB ||
                provider.getJ2eeModule().getType() == J2eeModule.Type.WAR) {
            EjbJar[] ejbJars = EjbJar.getEjbJars(project);
            if (ejbJars.length > 0) {
                // just use first one to test profile:
                ejbJarProfile =  ejbJars[0].getJ2eeProfile();
            }
            if (provider.getJ2eeModule().getType() == J2eeModule.Type.WAR) {
                WebModule module = WebModule.getWebModule(project.getProjectDirectory());
                if (module != null) {
                    webProfile = module.getJ2eeProfile();
                }
            }
        }
        return new J2eeProjectCapabilities(provider, ejbJarProfile, webProfile);
    }

    /**
     * EJB 3.0 functionality is supported in EjbJar project which is targetting
     * JEE5 or JEE6 platform.
     */
    public boolean isEjb30Supported() {
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        boolean eeOk = ejbJarProfile != null && (ejbJarProfile.equals(Profile.JAVA_EE_5) ||
                ejbJarProfile.equals(Profile.JAVA_EE_6_FULL));
        return J2eeModule.Type.EJB.equals(moduleType) && eeOk;
    }

    /**
     * EJB 3.1 functionality is supported in EjbJar and Web project which is targetting
     * full JEE6 platform.
     */
    public boolean isEjb31Supported() {
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        boolean ee6 = ejbJarProfile != null && ejbJarProfile.equals(Profile.JAVA_EE_6_FULL);
        return ee6 && (J2eeModule.Type.EJB.equals(moduleType) ||
                J2eeModule.Type.WAR.equals(moduleType));
    }

    /**
     * EJB 3.1 Lite functionality is supported in Web project targetting JEE6
     * web profile and wherever full EJB 3.1 is supported.
     */
    public boolean isEjb31LiteSupported() {
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        boolean ee6Web = ejbJarProfile != null && ejbJarProfile.equals(Profile.JAVA_EE_6_WEB);
        return isEjb31Supported() || (J2eeModule.Type.WAR.equals(moduleType) && ee6Web);
    }
    
    /*
     * Returns <code>true</code> if the server used by project supports JSF 2.x.
     * 
     * @retunr <code>true</code> if the server used by project supports JSF 2.x
     * @since 1.61
     */
    public boolean isJsf2Included() {
        if (webProfile == null || !webProfile.equals(Profile.JAVA_EE_6_FULL)
                && !webProfile.equals(Profile.JAVA_EE_6_WEB)) {
            return false;
        }
        String projectServerInstanceID = provider.getServerInstanceID();
        try {
            ServerInstance inst = Deployment.getDefault().getServerInstance(projectServerInstanceID);
            Set<Profile> profiles = inst.getJ2eePlatform().getSupportedProfiles();
            return profiles.contains(Profile.JAVA_EE_6_FULL)
                    || (profiles.contains(Profile.JAVA_EE_6_WEB)
                        && !provider.getServerID().startsWith("Tomcat")); // NOI18N
        } catch (InstanceRemovedException ex) {
            return false;
        }
    }

    public boolean hasDefaultPersistenceProvider() {
        J2eePlatform platform  = getPlatform();
        if (platform == null) {
            // server probably not registered, can't resolve whether default provider is supported (see #79856)
            return false;
        }

        Set<Profile> profiles = platform.getSupportedProfiles(provider.getJ2eeModule().getType());
        if (!profiles.contains(Profile.JAVA_EE_5) && !profiles.contains(Profile.JAVA_EE_6_FULL)) {
            return false;
        }
        JpaSupport support = JpaSupport.getInstance(platform);
        return support != null && support.getDefaultProvider() != null;
    }

    private J2eePlatform getPlatform() {
        try {
            String instance = provider.getServerInstanceID();
            if (instance != null) {
                return Deployment.getDefault().getServerInstance(provider.getServerInstanceID()).getJ2eePlatform();
            }
        } catch (InstanceRemovedException ex) {
            // will return null
        }
        return null;
    }
}
