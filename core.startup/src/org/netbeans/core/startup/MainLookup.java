/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core.startup;

import java.lang.reflect.Field;
import org.netbeans.JaveleonModule;
import org.netbeans.Module;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** The default lookup for the system.
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.util.Lookup.class)
public final class MainLookup extends ProxyLookup {
    private static boolean started = false;
    /** currently effective ClassLoader */
    private static ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    /** inner access to dynamic lookup service for this top mangager */
    private static InstanceContent instanceContent = new InstanceContent ();
    /** dynamic lookup service for this top mangager */
    private static Lookup instanceLookup = new AbstractLookup (instanceContent);

    /**
     * GuiRunLevel has started up.
     * That means that subsequent calls to lookup on ModuleInfo
     * need not try to get it again.
     */
    public static void started() {
        started = true;
    }
    
    /** Checks whether everything is started.
     */
    static boolean isStarted() {
        return started;
    } 

    public MainLookup () {
        super (new Lookup[] {
                   // #14722: pay attention also to META-INF/services/class.Name resources:
                   Lookups.metaInfServices(classLoader),
                   Lookups.singleton(classLoader),
                   Lookup.EMPTY, // will be moduleLookup
                   instanceLookup
               });
    }

    /** Called when a system classloader changes.
     */
    public static final void systemClassLoaderChanged (ClassLoader nue) {
        if (!(Lookup.getDefault() instanceof MainLookup)) {
            // May be called from MockServices.setServices even though we are not main lookup.
            return;
        }
        if (classLoader != nue) {
            classLoader = nue;
            MainLookup l = (MainLookup)Lookup.getDefault();
            Lookup[] delegates = l.getLookups();
            Lookup[] newDelegates = delegates.clone();
            
            // Javeleon hack for persisting all previously looked up
            // service instances from META-INF/services
            // All new lookups will ask the new system class loader
            if(JaveleonModule.isJaveleonPresent) {
                for(Lookup look : newDelegates) {
                    if(look.getClass().getName().contains("MetaInfServicesLookup")) {
                        try {
                            Field field = look.getClass().getDeclaredField("loader");
                            field.setAccessible(true);
                            field.set(look, classLoader);
                        } catch (Exception ex) {
                            // OK the field have been removed from the class
                            // just replace the MetaInfLookup instance then
                            // warn user about this though.
                            System.err.println("[JAVELOEN WARNING]: had to replace the META-INF/services lookup. Any attempt to lookup an already looked up class or interface will give you a completely fresh implementing instance!");
                        }
                    }
                }
            } else {
                 newDelegates[0] = Lookups.metaInfServices(classLoader);
            }
            // Replace classloader.
            newDelegates[1] = Lookups.singleton(classLoader);
            l.setLookups(newDelegates);
        } else {
            moduleClassLoadersUp();
        }
    }

    /** Called when modules are about to be turned on.
     */
    public static final void moduleClassLoadersUp() {
        MainLookup l = (MainLookup)Lookup.getDefault();
        Lookup[] newDelegates = null;
        Lookup[] delegates = l.getLookups();
        newDelegates = delegates.clone();
        newDelegates[0] = Lookups.metaInfServices(classLoader);
        l.setLookups(newDelegates);
    }

    /** Called when Lookup<ModuleInfo> is ready from the ModuleManager.
     * @see "#28465"
     */
    public static final void moduleLookupReady(Lookup moduleLookup) {
        if (Lookup.getDefault() instanceof MainLookup) {
            MainLookup l = (MainLookup)Lookup.getDefault();
            Lookup[] newDelegates = l.getLookups().clone();
            newDelegates[2] = moduleLookup;
            l.setLookups(newDelegates);
        }
    }

    /** When all module classes are accessible thru systemClassLoader, this
     * method is called to initialize the FolderLookup.
     */

    public static final void modulesClassPathInitialized () {
        //System.err.println("mCPI");
    //StartLog.logStart ("MainLookup: initialization of FolderLookup"); // NOI18N

        // replace the lookup by new one
        Lookup lookup = Lookup.getDefault ();
        StartLog.logProgress ("Got Lookup"); // NOI18N

        ((MainLookup)lookup).doInitializeLookup ();
    }

    //
    // 
    //
    
    /** Register new instance.
     */
    public static void register (Object obj) {
        instanceContent.add (obj);
    }
    
    /** Register new instance.
     * @param obj source
     * @param conv convertor which postponing an instantiation
     */
    public static <T,R> void register(T obj, InstanceContent.Convertor<T,R> conv) {
        instanceContent.add(obj, conv);
    }
    
    /** Unregisters the service.
     */
    public static void unregister (Object obj) {
        instanceContent.remove (obj);
    }
    /** Unregisters the service registered with a convertor.
     */
    public static <T,R> void unregister (T obj, InstanceContent.Convertor<T,R> conv) {
        instanceContent.remove (obj, conv);
    }
    
    
    
    

    private final void doInitializeLookup () {
        //System.err.println("doInitializeLookup");

        // extend the lookup
        Lookup[] arr = new Lookup[] {
            getLookups()[0], // metaInfServicesLookup
            getLookups()[1], // ClassLoader lookup
            getLookups()[2], // ModuleInfo lookup
            instanceLookup, 
            CoreBridge.getDefault().lookupCacheLoad(),
        };
        StartLog.logProgress ("prepared other Lookups"); // NOI18N

        setLookups (arr);
        StartLog.logProgress ("Lookups set"); // NOI18N
    //StartLog.logEnd ("MainLookup: initialization of FolderLookup"); // NOI18N
    }

    @Override
    protected void beforeLookup(Lookup.Template templ) {
        Class type = templ.getType();

        // Force module system to be initialize by looking up ModuleInfo or Modules.
        // Good for unit tests, etc.
        if (!started && (type == ModuleInfo.class || type == Module.class || type == Modules.class)) {
            Main.getModuleSystem ();
        }

        super.beforeLookup(templ);
    }
}
    
