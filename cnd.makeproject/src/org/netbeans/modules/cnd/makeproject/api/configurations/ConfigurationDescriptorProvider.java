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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationXMLReader;
import org.netbeans.modules.cnd.makeproject.platform.Platforms;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.ui.UIGesturesSupport;
import org.netbeans.modules.dlight.util.usagetracking.SunStudioUserCounter;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

public class ConfigurationDescriptorProvider {
    public static final boolean VCS_WRITE = true; // Boolean.getBoolean("cnd.make.vcs.write");//org.netbeans.modules.cnd.makeproject.configurations.CommonConfigurationXMLCodec.VCS_WRITE;
    
    public static final String USG_PROJECT_CONFIG_CND = "USG_PROJECT_CONFIG_CND"; // NOI18N
    public static final String USG_PROJECT_OPEN_CND = "USG_PROJECT_OPEN_CND"; // NOI18N
    public static final String USG_PROJECT_CREATE_CND = "USG_PROJECT_CREATE_CND"; // NOI18N
    private static final String USG_CND_PROJECT_ACTION = "USG_CND_PROJECT_ACTION"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    private final static RequestProcessor RP = new RequestProcessor("Configuration Updater", 1); // NOI18N
    private final FileObject projectDirectory;
    private final Project project;
    private volatile MakeConfigurationDescriptor projectDescriptor = null;
    private volatile boolean hasTried = false;
    private String relativeOffset = null;
    private List<FileObject> trackedFiles;
    private volatile boolean needReload;

    // for unit tests only
    public ConfigurationDescriptorProvider(FileObject projectDirectory) {
        this(null, projectDirectory);
    }

    public ConfigurationDescriptorProvider(Project project, FileObject projectDirectory) {
        this.project = project;
        this.projectDirectory = projectDirectory;
    }

    public void setRelativeOffset(String relativeOffset) {
        this.relativeOffset = relativeOffset;
    }
    private final Object readLock = new Object();

    public MakeConfigurationDescriptor getConfigurationDescriptor() {
        return getConfigurationDescriptor(true);
    }

    private boolean shouldBeLoaded() {
        return ((projectDescriptor == null || needReload) && !hasTried);

    }

    public MakeConfigurationDescriptor getConfigurationDescriptor(boolean waitReading) {
        if (shouldBeLoaded()) {
            // attempt to read configuration descriptor
            // do this only once
            synchronized (readLock) {
                // check again that someone already havn't read
                if (shouldBeLoaded()) {
                    LOGGER.log(Level.FINE, "Start reading project descriptor for project {0} in ConfigurationDescriptorProvider@{1}", new Object[]{projectDirectory.getNameExt(), System.identityHashCode(this)}); // NOI18N
                    // It's important to set needReload=false before calling
                    // projectDescriptor.assign(), otherwise there will be
                    // infinite recursion.
                    needReload = false;

                    if (trackedFiles == null) {
                        FileChangeListener fcl = new ConfigurationXMLChangeListener();
                        List<FileObject> files = new ArrayList<FileObject>(2);
                        boolean first = true;
                        for (String path : new String[]{
                                    "nbproject/configurations.xml", //NOI18N
                                    "nbproject/private/configurations.xml"}) { //NOI18N
                            FileObject fo = projectDirectory.getFileObject(path);
                            if (fo != null) {
                                fo.addFileChangeListener(fcl);
                                // We have to store tracked files somewhere.
                                // Otherwise they will be GCed, and we won't get notifications.
                                files.add(fo);
                            } else {
                                if (first) {
                                    // prevent reading configurations before project cration
                                    CndUtils.threadsDump();
                                    new Exception("Attempt to read project before creation. Not found file " + projectDirectory.getPath() + "/" + path).printStackTrace(System.err); // NOI18N
                                    return null;
                                }
                            }
                            first = false;
                        }
                        trackedFiles = files;
                    }

                    ConfigurationXMLReader reader = new ConfigurationXMLReader(project, projectDirectory);

                    //                        if (waitReading && SwingUtilities.isEventDispatchThread()) {
                    //                            new Exception("Not allowed to use EDT for reading XML descriptor of project!" + projectDirectory).printStackTrace(System.err); // NOI18N
                    //                            // PLEASE DO NOT ADD HACKS like Task.waitFinished()
                    //                            // CHANGE YOUR LOGIC INSTEAD
                    //
                    //                            // FIXUP for IZ#146696: cannot open projects: Not allowed to use EDT...
                    //                            // return null;
                    //                        }
                    try {
                        SnapShot delta = startModifications();
                        MakeConfigurationDescriptor newDescriptor = reader.read(relativeOffset);
                        LOGGER.log(Level.FINE, "End of reading project descriptor for project {0} in ConfigurationDescriptorProvider@{1}", // NOI18N
                                new Object[]{projectDirectory.getNameExt(), System.identityHashCode(this)});
                        if (projectDescriptor == null) {
                            if (newDescriptor != null) {
                                projectDescriptor = newDescriptor;
                                LOGGER.log(Level.FINE, "Created project descriptor MakeConfigurationDescriptor@{0} for project {1} in ConfigurationDescriptorProvider@{2}", // NOI18N
                                        new Object[]{System.identityHashCode(projectDescriptor), projectDirectory.getNameExt(), System.identityHashCode(this)});
                            } else {
                                LOGGER.log(Level.FINE, "Cannot create project descriptor for project {0} in ConfigurationDescriptorProvider@{1}", // NOI18N
                                        new Object[]{projectDirectory.getNameExt(), System.identityHashCode(this)});
                            }
                        } else {
                            if (newDescriptor != null) {
                                newDescriptor.setProject(project);
                                newDescriptor.waitInitTask();
                                projectDescriptor.assign(newDescriptor);
                                endModifications(delta, true, LOGGER);
                                LOGGER.log(Level.FINE, "Reassigned project descriptor MakeConfigurationDescriptor@{0} for project {1} in ConfigurationDescriptorProvider@{2}", // NOI18N
                                        new Object[]{System.identityHashCode(projectDescriptor), projectDirectory.getNameExt(), System.identityHashCode(this)});
                            } else {
                                LOGGER.log(Level.FINE, "cannot reassign project descriptor MakeConfigurationDescriptor@{0} for project {1} in ConfigurationDescriptorProvider@{2}", // NOI18N
                                        new Object[]{System.identityHashCode(projectDescriptor), projectDirectory.getNameExt(), System.identityHashCode(this)});
                            }
                        }
                    } catch (java.io.IOException x) {
                        x.printStackTrace(System.err);
                        // most likely open failed
                    }

                    hasTried = true;
                }
            }
            }
        if (waitReading && projectDescriptor != null) {
            projectDescriptor.waitInitTask();
        }
        return projectDescriptor;
    }

    public SnapShot startModifications() {
        if (projectDescriptor != null) {
            return new Delta(projectDescriptor);
        }
        return null;
    }

    public void endModifications(SnapShot snapShot, boolean sendChangeEvent, Logger logger) {
        if (snapShot instanceof Delta) {
            Delta delta = (Delta) snapShot;
            if (sendChangeEvent && projectDescriptor != null) {
                delta.computeDelta(projectDescriptor);
                if (logger != null) {
                    delta.printStatistic(logger);
                }
                projectDescriptor.checkForChangedItems(delta);
            }
        }
    }
    
    public boolean gotDescriptor() {
        return projectDescriptor != null && projectDescriptor.getState() != State.READING;
    }

    public static ConfigurationAuxObjectProvider[] getAuxObjectProviders() {
        HashSet<ConfigurationAuxObjectProvider> auxObjectProviders = new HashSet<ConfigurationAuxObjectProvider>();
        Collection<? extends ConfigurationAuxObjectProvider> collection =
                Lookup.getDefault().lookupAll(ConfigurationAuxObjectProvider.class);
//      System.err.println("-------------------------------collection " + collection);
        Iterator<? extends ConfigurationAuxObjectProvider> iterator = collection.iterator();
        while (iterator.hasNext()) {
            auxObjectProviders.add(iterator.next());
        }
//      System.err.println("-------------------------------auxObjectProviders " + auxObjectProviders);
        return auxObjectProviders.toArray(new ConfigurationAuxObjectProvider[auxObjectProviders.size()]);
    }

    public static void recordMetrics(String msg, MakeConfigurationDescriptor descr) {
        recordMetricsImpl(msg, null, descr, null);
    }

    public static void recordCreatedProjectMetrics(MakeConfiguration[] confs) {
        if (confs != null && confs.length > 0) {
            recordMetricsImpl(USG_PROJECT_CREATE_CND, confs[0], null, null);
        }
    }

    public static void recordActionMetrics(String action, MakeConfigurationDescriptor descr) {
        recordMetricsImpl(USG_CND_PROJECT_ACTION, null, descr, action);
    }

    private static void recordMetricsImpl(String msg,
            MakeConfiguration makeConfiguration,
            MakeConfigurationDescriptor descr,
            String action) {
        if (CndUtils.isUnitTestMode()) {
            // we don't want to count own tests
            return;
        }
        if (descr == null && makeConfiguration == null) {
            return;
        }
        Item[] projectItems = null;
        if (makeConfiguration == null) {
            if (descr.getConfs() == null || descr.getConfs().getActive() == null) {
                return;
            }
            if (makeConfiguration == null) {
                makeConfiguration = descr.getActiveConfiguration();
            }
            projectItems = (descr).getProjectItems();
            if (!USG_PROJECT_CREATE_CND.equals(msg) && (projectItems == null || projectItems.length == 0)) {
                // do not track empty applications
                return;
            }
        }
        String type;
        switch (makeConfiguration.getConfigurationType().getValue()) {
            case MakeConfiguration.TYPE_MAKEFILE:
                type = "MAKEFILE"; // NOI18N
                break;
            case MakeConfiguration.TYPE_APPLICATION:
                type = "APPLICATION"; // NOI18N
                break;
            case MakeConfiguration.TYPE_DB_APPLICATION:
                type = "DB_APPLICATION"; // NOI18N
                break;
            case MakeConfiguration.TYPE_DYNAMIC_LIB:
                type = "DYNAMIC_LIB"; // NOI18N
                break;
            case MakeConfiguration.TYPE_STATIC_LIB:
                type = "STATIC_LIB"; // NOI18N
                break;
            case MakeConfiguration.TYPE_QT_APPLICATION:
                type = "QT_APPLICATION"; // NOI18N
                break;
            case MakeConfiguration.TYPE_QT_DYNAMIC_LIB:
                type = "QT_DYNAMIC_LIB"; // NOI18N
                break;
            case MakeConfiguration.TYPE_QT_STATIC_LIB:
                type = "QT_STATIC_LIB"; // NOI18N
                break;
            default:
                type = "UNKNOWN"; // NOI18N
        }
        String host;
        CompilerSet compilerSet;
        if (makeConfiguration.getDevelopmentHost().isLocalhost()) {
            host = "LOCAL"; // NOI18N
            compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        } else {
            host = "REMOTE"; // NOI18N
            // do not force creation of compiler sets
            compilerSet = null;
        }
        String flavor;
        String[] families;
        if (compilerSet != null) {
            families = compilerSet.getCompilerFlavor().getToolchainDescriptor().getFamily();
            flavor = compilerSet.getCompilerFlavor().toString();
        } else {
            families = new String[0];
            if (makeConfiguration.getCompilerSet() != null) {
                families = new String[]{makeConfiguration.getCompilerSet().getName()};
            }
            flavor = makeConfiguration.getCompilerSet().getFlavor();
        }
        String family;
        if (families.length == 0) {
            family = flavor; // NOI18N
        } else {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < families.length; i++) {
                if (families[i] != null) {
                    buffer.append(families[i]);
                    if (i < families.length - 1) {
                        buffer.append(","); // NOI18N
                    }
                }
            }
            family = buffer.toString();
        }
        String platform;
        int platformID = makeConfiguration.getDevelopmentHost().getBuildPlatform();
        if (Platforms.getPlatform(platformID) != null) {
            platform = Platforms.getPlatform(platformID).getName();
        } else {
            platform = "UNKNOWN_PLATFORM"; // NOI18N
        }

        String ideType = SunStudioUserCounter.getIDEType().getTag();
        if (USG_PROJECT_CREATE_CND.equals(msg)) {
            // stop here
            UIGesturesSupport.submit(msg, type, flavor, family, host, platform, "USER_PROJECT", ideType); //NOI18N
        } else if (USG_CND_PROJECT_ACTION.equals(msg)) {
            UIGesturesSupport.submit(msg, action, type, flavor, family, host, platform, ideType); //NOI18N
        } else if (projectItems != null) {
            makeConfiguration.reCountLanguages(descr);
            int size = 0;
            int allItems = projectItems.length;
            boolean cLang = false;
            boolean ccLang = false;
            boolean fLang = false;
            boolean aLang = false;
            for (Item item : projectItems) {
                ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration);
                if (itemConfiguration != null && !itemConfiguration.getExcluded().getValue()) {
                    size++;
                    switch (itemConfiguration.getTool()) {
                        case CCompiler:
                            cLang = true;
                            break;
                        case CCCompiler:
                            ccLang = true;
                            break;
                        case FortranCompiler:
                            fLang = true;
                            break;
                        case Assembler:
                            aLang = true;
                            break;
                    }
                }
            }
            String ccUsage = ccLang ? "USE_CPP" : "NO_CPP"; // NOI18N
            String cUsage = cLang ? "USE_C" : "NO_C"; // NOI18N
            String fUsage = fLang ? "USE_FORTRAN" : "NO_FORTRAN"; // NOI18N
            String aUsage = aLang ? "USE_ASM" : "NO_ASM"; // NOI18N
            UIGesturesSupport.submit(msg, type, flavor, family, host, platform, toSizeString(allItems), toSizeString(size), ccUsage, cUsage, fUsage, aUsage, ideType);
        }
    }

    private static String toSizeString(int size) {
        String strSize;
        if (size < 25) {
            strSize = "25"; // NOI18N
        } else if (size < 100) {
            strSize = "100"; // NOI18N
        } else if (size < 500) {
            strSize = "500"; // NOI18N
        } else if (size < 1000) {
            strSize = "1000"; // NOI18N
        } else if (size < 2000) {
            strSize = "2000"; // NOI18N
        } else if (size < 5000) {
            strSize = "5000"; // NOI18N
        } else if (size < 10000) {
            strSize = "10000"; // NOI18N
        } else if (size < 20000) {
            strSize = "20000"; // NOI18N
        } else if (size < 50000) {
            strSize = "50000"; // NOI18N
        } else {
            strSize = "99999"; // NOI18N
        }
        return strSize;
    }

    public void closed() {
        MakeConfigurationDescriptor descr = getConfigurationDescriptor();
        if (descr != null) {
            descr.closed();
        }
    }

    public void opened() {
        MakeConfigurationDescriptor descr = getConfigurationDescriptor(true);
        if (descr != null) {
            descr.opened();
        }
    }

    /**
     * This listener will be notified about updates of files
     * <code>nbproject/configurations.xml</code> and
     * <code>nbproject/private/configurations.xml</code>.
     * These files should be reloaded when changed externally.
     * See IZ#146701: can't update project through subversion, or any other
     */
    private class ConfigurationXMLChangeListener implements FileChangeListener {

        private void resetConfiguration() {
            if (projectDescriptor == null || !projectDescriptor.isModified()) {
                synchronized (readLock) {
                    if (projectDescriptor == null || !projectDescriptor.isModified()) {
                        // Don't reload if descriptor is modified in memory.
                        // This also prevents reloading when descriptor is being saved.
                        LOGGER.log(Level.FINE, "Mark to reload project descriptor MakeConfigurationDescriptor@{0} for project {1} in ConfigurationDescriptorProvider@{2}", new Object[]{System.identityHashCode(projectDescriptor), projectDirectory.getNameExt(), System.identityHashCode(this)}); // NOI18N
                        needReload = true;
                        hasTried = false;
                        RP.post(new Runnable() {

                            @Override
                            public void run() {
                                getConfigurationDescriptor();
                            }
                        });
                    }
                }
            }
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            resetConfiguration();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            resetConfiguration();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            resetConfiguration();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            resetConfiguration();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            resetConfiguration();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // Don't reset configuration on file attribute change.
        }
    }

    public interface SnapShot {
    }
    
    public static final class Delta implements SnapShot {

        private final Map<String, Pair> oldState = new HashMap<String, Pair>();
        private final List<Item> included = new ArrayList<Item>();
        private final List<Item> added = new ArrayList<Item>(); 
        private final List<Item> excluded = new ArrayList<Item>(); 
        private final List<Item> deleted = new ArrayList<Item>(); 
        private final List<Item> changed = new ArrayList<Item>(); 
        private final List<Item> replaced = new ArrayList<Item>(); 

        private Delta(MakeConfigurationDescriptor oldDescriptor) {
            if (oldDescriptor != null) {
                for(Item item : oldDescriptor.getProjectItems()) {
                    oldState.put(item.getAbsolutePath(), new Pair(item, item.getCRC(), item.isExcluded()));
                }
            }
        }
        
        private void computeDelta(MakeConfigurationDescriptor newDescriptor) {
            Set<Item> oldSet = new HashSet<Item>();
            for (Map.Entry<String, Delta.Pair> entry : oldState.entrySet()) {
                oldSet.add(entry.getValue().item);
            }
            Item[] newItems = newDescriptor.getProjectItems();
            for (Item item : newItems) {
                Delta.Pair pair = oldState.get(item.getAbsolutePath());
                if (pair == null) {
                    added.add(item);
                } else {
                    oldSet.remove(pair.item);
                    if (item.isExcluded() && pair.excluded) {
                        // no changes
                        replaced.add(item);
                    } else if (item.isExcluded() && !pair.excluded) {
                        excluded.add(item);
                    } else if (!item.isExcluded() && pair.excluded) {
                        included.add(item);
                    } else {
                        // compare item properties
                        if (item.getCRC() != pair.crc) {
                            changed.add(item);
                        } else {
                            if (pair.item != item) {
                                replaced.add(item);
                            }
                        }
                    }
                }
            }
            for (Item item : oldSet) {
                deleted.add(item);
            }
            oldState.clear();
        }
        
        public void printStatistic(Logger logger) {
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "Configuration updated:\n\t{0} deleted items\n\t{1} added items\n\t{2} changed items",
                        new Object[]{deleted.size()+excluded.size(), added.size()+included.size(), changed.size()});
            }
        }
        
        public boolean isEmpty() {
            return included.isEmpty() && added.isEmpty() && excluded.isEmpty() && deleted.isEmpty() && changed.isEmpty();
        }

        /**
         * marked as included items
         */
        public List<Item> getIncluded() {
            return Collections.unmodifiableList(included);
        }

        /**
         * added in project items
         */
        public List<Item> getAdded() {
            return Collections.unmodifiableList(added);
        }

        /**
         * marked as excluded items
         */
        public List<Item> getExcluded() {
            return Collections.unmodifiableList(excluded);
        }

        /**
         * deleted from project items
         */
        public List<Item> getDeleted() {
            return Collections.unmodifiableList(deleted);
        }

        /**
         * items with changed properties
         */
        public List<Item> getChanged() {
            return Collections.unmodifiableList(changed);
        }

        /**
         * Items which properties were not changed (from code model point of view) but instances were replaced
         */
        public List<Item> getReplaced() {
            return Collections.unmodifiableList(replaced);
        }
        
        private static final class Pair {
            final int crc;
            final boolean excluded;
            final Item item;
            private Pair(Item item, int crc, boolean excluded) {
                this.crc = crc;
                this.excluded = excluded;
                this.item = item;
            }
        }
    }
}
