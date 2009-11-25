/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.maven.classpath;

import org.codehaus.plexus.util.StringUtils;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.java.classpath.ClassPathImplementation;

import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;

/**
 * NO listening on changes here, let the BootClassPath deal with it..
 * @author  Milos Kleint
 */
public final class EndorsedClassPathImpl implements ClassPathImplementation {

    private List<? extends PathResourceImplementation> resourcesCache;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final NbMavenProjectImpl project;
    private BootClassPathImpl bcp;
    private String[] current;


    EndorsedClassPathImpl(NbMavenProjectImpl project) {
        this.project = project;
    }

    public List<? extends PathResourceImplementation> getResources() {
        assert bcp != null;
        synchronized (bcp.LOCK) {
            if (this.resourcesCache == null) {
                ArrayList<PathResourceImplementation> result = new ArrayList<PathResourceImplementation> ();
                String[] boot = getBootClasspath();
                if (boot != null) {
                    for (URL u :  stripDefaultJavaPlatform(boot)) {
                        result.add (ClassPathSupport.createResource(u));
                    }
                }
                current = boot;
                resourcesCache = Collections.unmodifiableList (result);
            }
            return this.resourcesCache;
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }

    private String[] getBootClasspath() {
        String carg = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "compilerArgument", "compile");
        if (carg != null) {
            //TODO
        }
        Properties cargs = PluginPropertyUtils.getPluginPropertyParameter(project, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "compilerArguments", "compile");
        if (cargs != null) {
            String carg2 = cargs.getProperty("bootclasspath");
            if (carg2 != null) {
                return StringUtils.split(carg2, File.pathSeparator);
            }
        }
        return null;
    }

    /**
     * Resets the cache and firesPropertyChange
     */
    boolean resetCache () {
        String[] newones = getBootClasspath();
        boolean fire = false;
        assert bcp != null;
        synchronized (bcp.LOCK) {
            if (!Arrays.equals(newones, current)) {
                resourcesCache = null;
                fire = true;
            }
        }
        if (fire) {
            support.firePropertyChange(PROP_RESOURCES, null, null);
        }
        return fire;
    }

    void setBCP(BootClassPathImpl aThis) {
        bcp = aThis;
    }

    private List<URL> stripDefaultJavaPlatform(String[] boot) {
        List<URL> toRet = new ArrayList<URL>();
        Set<URL> defs = getDefJavaPlatBCP();
        for (String s : boot) {
            File f = FileUtilities.convertStringToFile(s);
            URL entry = FileUtil.urlForArchiveOrDir(f);
            if (entry != null && !defs.contains(entry)) {
                toRet.add(entry);
            }
        }
        return toRet;
    }

    private final Set<URL> djpbcp = new HashSet<URL>();

    private Set<URL> getDefJavaPlatBCP() {
        synchronized (djpbcp) {
            if (djpbcp.size() == 0) {
                JavaPlatformManager mngr = JavaPlatformManager.getDefault();
                JavaPlatform jp = mngr.getDefaultPlatform();
                ClassPath cp = jp.getBootstrapLibraries();
                for (ClassPath.Entry ent : cp.entries()) {
                    djpbcp.add(ent.getURL());
                }
            }
            return Collections.unmodifiableSet(djpbcp);
        }
    }

}