/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Oracle
 */
package org.netbeans.modules.netbinox;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.eclipse.osgi.baseadaptor.BaseAdaptor;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.HookConfigurator;
import org.eclipse.osgi.baseadaptor.HookRegistry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.baseadaptor.bundlefile.MRUBundleFileList;
import org.eclipse.osgi.baseadaptor.hooks.AdaptorHook;
import org.eclipse.osgi.baseadaptor.hooks.BundleFileFactoryHook;
import org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook;
import org.eclipse.osgi.baseadaptor.loader.BaseClassLoader;
import org.eclipse.osgi.baseadaptor.loader.ClasspathEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathManager;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.adaptor.BundleProtectionDomain;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.netbeans.core.netigso.spi.NetigsoArchive;
import org.openide.util.Lookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public final class NetbinoxHooks implements HookConfigurator, ClassLoadingHook,
BundleFileFactoryHook, FrameworkLog, AdaptorHook {
    private static transient Map<Bundle,ClassLoader> map;
    private static transient NetigsoArchive archive;
    static void clear() {
        map = null;
        archive = null;
    }

    static void registerMap(Map<Bundle, ClassLoader> bundleMap) {
        map = bundleMap;
    }

    static void registerArchive(NetigsoArchive netigsoArchive) {
        archive = netigsoArchive;
    }

    public void addHooks(HookRegistry hr) {
        hr.addClassLoadingHook(this);
        hr.addBundleFileFactoryHook(this);
        hr.addAdaptorHook(this);
        for (HookConfigurator hc : Lookup.getDefault().lookupAll(HookConfigurator.class)) {
            hc.addHooks(hr);
        }
    }

    public byte[] processClass(String string, byte[] bytes, ClasspathEntry ce, BundleEntry be, ClasspathManager cm) {
        return bytes;
    }

    public boolean addClassPathEntry(ArrayList al, String string, ClasspathManager cm, BaseData bd, ProtectionDomain pd) {
        return false;
    }

    public String findLibrary(BaseData bd, String string) {
        return null;
    }

    public ClassLoader getBundleClassLoaderParent() {
        return null;
    }

    public BaseClassLoader createClassLoader(ClassLoader parent, final ClassLoaderDelegate delegate, final BundleProtectionDomain bpd, BaseData bd, String[] classpath) {
        String loc = bd.getBundle().getLocation();
        //NetigsoModule.LOG.log(Level.FINER, "createClassLoader {0}", bd.getLocation());
        final String pref = "netigso://"; // NOI18N
        ClassLoader ml = null;
        if (loc != null && loc.startsWith(pref)) {
            String cnb = loc.substring(pref.length());
            ml = map.get(bd.getBundle());
        }
        if (ml == null) {
            return new NetbinoxLoader(parent, delegate, bpd, bd, classpath);
        }
        class Del extends ClassLoader implements BaseClassLoader {
            public Del(ClassLoader parent) {
                super(parent);
            }

            public ProtectionDomain getDomain() {
                return bpd;
            }

            public ClasspathEntry createClassPathEntry(BundleFile bf, ProtectionDomain pd) {
                return null;
            }

            public Class defineClass(String string, byte[] bytes, ClasspathEntry ce, BundleEntry be) {
                throw new UnsupportedOperationException();
            }

            public Class publicFindLoaded(String name) {
                return super.findLoadedClass(name);
            }

            public Object publicGetPackage(String name) {
                return super.getPackage(name);
            }

            public Object publicDefinePackage(String s1, String s2, String s3, String s4, String s5, String s6, String s7, URL url) {
                return super.definePackage(s1, s2, s3, s4, s5, s6, s7, url);
            }

            public ClasspathManager getClasspathManager() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void initialize() {
            }

            public URL findLocalResource(String name) {
                return null;
                /*
                ProxyClassLoader pcl = (ProxyClassLoader)getParent();
                return pcl.findResource(name);
                 *
                 */
            }

            public Enumeration<URL> findLocalResources(String name) {
                return null;
                /*
                ProxyClassLoader pcl = (ProxyClassLoader)getParent();
                try {
                    return pcl.findResources(name);
                } catch (IOException ex) {
                    return Enumerations.empty();
                }
                 */
            }

            @Override
            protected URL findResource(String name) {
                return findLocalResource(name);
            }

            @Override
            protected Enumeration<URL> findResources(String name) throws IOException {
                return findLocalResources(name);
            }

            public Class findLocalClass(String name) throws ClassNotFoundException {
                return getParent().loadClass(name);
            }

            public void close() {
            }

            public void attachFragment(BundleData bd, ProtectionDomain pd, String[] strings) {
            }

            public ClassLoaderDelegate getDelegate() {
                return delegate;
            }

            public Bundle getBundle() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
        return new Del(ml);
    }

    public void initializedClassLoader(BaseClassLoader bcl, BaseData bd) {
    }

    private final MRUBundleFileList mruList = new MRUBundleFileList();
    public BundleFile createBundleFile(Object file, final BaseData bd, boolean isBase) throws IOException {

        if (file instanceof File) {
            final File f = (File)file;
// running with fake manifest fails for some reason, disabling for now
//            final String loc = bd.getLocation();
//            if (loc != null && loc.startsWith("netigso://")) {
//                return new NetigsoBundleFile(f, bd);
//            }
            return new JarBundleFile(f, bd, archive, mruList, isBase);
        }
        return null;
    }

    public void log(FrameworkEvent fe) {
        Level l = Level.FINE;
        if ((fe.getType() & FrameworkEvent.ERROR) != 0) {
            l = Level.SEVERE;
        } else if ((fe.getType() & FrameworkEvent.WARNING) != 0) {
            l = Level.WARNING;
        } else if ((fe.getType() & FrameworkEvent.INFO) != 0) {
            l = Level.INFO;
        }
        LogRecord lr = new LogRecord(l, "framework event {0} type {1}");
        lr.setParameters(new Object[]{fe.getBundle().getSymbolicName(), fe.getType()});
        lr.setThrown(fe.getThrowable());
        lr.setLoggerName(NetbinoxFactory.LOG.getName());
        NetbinoxFactory.LOG.log(lr);
    }

    public void log(FrameworkLogEntry fle) {
        NetbinoxFactory.LOG.log(Level.FINE, "entry {0}", fle);
    }

    public void setWriter(Writer writer, boolean bln) {
    }

    public void setFile(File file, boolean bln) throws IOException {
    }

    public File getFile() {
        return null;
    }

    public void setConsoleLog(boolean bln) {
    }

    public void close() {
    }

    // adaptor hooks

    public void initialize(BaseAdaptor ba) {
    }

    public void frameworkStart(BundleContext bc) throws BundleException {
    }

    public void frameworkStop(BundleContext bc) throws BundleException {
    }

    public void frameworkStopping(BundleContext bc) {
    }

    public void addProperties(Properties prprts) {
    }

    public URLConnection mapLocationToURLConnection(String string) throws IOException {
        return null;
    }

    public void handleRuntimeError(Throwable thrwbl) {
        NetbinoxFactory.LOG.log(Level.WARNING, thrwbl.getMessage(), thrwbl);
    }

    public FrameworkLog createFrameworkLog() {
        return this;
    }
}
