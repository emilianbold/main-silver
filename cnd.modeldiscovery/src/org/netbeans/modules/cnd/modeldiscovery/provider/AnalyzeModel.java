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

package org.netbeans.modules.cnd.modeldiscovery.provider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.discovery.api.ApplicableImpl;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PkgConfig;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectImpl;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.discovery.api.DiscoveryProvider.class)
public class AnalyzeModel implements DiscoveryProvider {
    private Map<String,ProviderProperty> myProperties = new HashMap<String,ProviderProperty>();
    public static final String MODEL_FOLDER_KEY = "folder"; // NOI18N
    public static final String PREFER_LOCAL_FILES = "prefer-local"; // NOI18N
    protected boolean isStoped = false;
    
    
    public AnalyzeModel() {
        clean();
    }
    
    @Override
    public final void clean() {
        myProperties.clear();
        myProperties.put(MODEL_FOLDER_KEY, new ProviderProperty(){
            private String myPath;
            @Override
            public String getName() {
                return i18n("Model_Files_Name"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("Model_Files_Description"); // NOI18N
            }
            @Override
            public Object getValue() {
                return myPath;
            }
            @Override
            public void setValue(Object value) {
                if (value instanceof String){
                    myPath = (String)value;
                }
            }
            @Override
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.Folder;
            }
        });
        myProperties.put(PREFER_LOCAL_FILES, new ProviderProperty(){
            private Boolean myValue = Boolean.FALSE;
            @Override
            public String getName() {
                return i18n("Prefer_Local_Files"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("Prefer_Local_Files_Description"); // NOI18N
            }
            @Override
            public Object getValue() {
                return myValue;
            }
            @Override
            public void setValue(Object value) {
                if (value instanceof Boolean){
                    myValue = (Boolean)value;
                }
            }
            @Override
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.Boolean;
            }
        });
    }
    
    @Override
    public String getID() {
        return "model-folder"; // NOI18N
    }
    
    @Override
    public String getName() {
        return i18n("Model_Provider_Name"); // NOI18N
    }
    
    @Override
    public String getDescription() {
        return i18n("Model_Provider_Description"); // NOI18N
    }
    
    @Override
    public List<String> getPropertyKeys() {
        return new ArrayList<String>(myProperties.keySet());
    }
    
    @Override
    public ProviderProperty getProperty(String key) {
        return myProperties.get(key);
    }
    
    @Override
    public void stop() {
        isStoped = true;
    }
    
    @Override
    public List<Configuration> analyze(ProjectProxy project, Progress progress) {
        isStoped = false;
        MyConfiguration conf = new MyConfiguration(project, progress);
        List<Configuration> confs = new ArrayList<Configuration>();
        confs.add(conf);
        return confs;
    }
    
    
    private Map<String,List<String>> search(String root){
        HashSet<String> set = new HashSet<String>();
        ArrayList<String> list = new ArrayList<String>();
        list.add(root);
        for (Iterator<String> it = list.iterator(); it.hasNext();){
            if (isStoped) {
                break;
            }
            File f = new File(it.next());
            gatherSubFolders(f, set);
        }
        HashMap<String,List<String>> map = new HashMap<String,List<String>>();
        for (Iterator<String> it = set.iterator(); it.hasNext();){
            if (isStoped) {
                break;
            }
            File d = new File(it.next());
            if (d.exists() && d.isDirectory() && d.canRead()){
                File[] ff = d.listFiles();
                if (ff != null) {
                    for (int i = 0; i < ff.length; i++) {
                        if (ff[i].isFile()) {
                            List<String> l = map.get(ff[i].getName());
                            if (l==null){
                                l = new ArrayList<String>();
                                map.put(ff[i].getName(),l);
                            }
                            String path = ff[i].getAbsolutePath();
                            if (Utilities.isWindows()) {
                                path = path.replace('\\', '/');
                            }
                            l.add(path);
                        }
                    }
                }
            }
        }
        return map;
    }
    
    private void gatherSubFolders(File d, HashSet<String> set){
        if (isStoped) {
            return;
        }
        if (d.exists() && d.isDirectory() && d.canRead()){
            if (DiscoveryUtils.ignoreFolder(d)){
                return;
            }
            String path = d.getAbsolutePath();
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
            if (!set.contains(path)){
                set.add(path);
                File[] ff = d.listFiles();
                if (ff != null) {
                    for (int i = 0; i < ff.length; i++) {
                        if (ff[i].isDirectory()) {
                            try {
                                String canPath = ff[i].getCanonicalPath();
                                String absPath = ff[i].getAbsolutePath();
                                if (!absPath.equals(canPath) && absPath.startsWith(canPath)) {
                                    continue;
                                }
                            } catch (IOException ex) {
                                //Exceptions.printStackTrace(ex);
                            }
                            gatherSubFolders(ff[i], set);
                        }
                    }
                }
            }
        }
    }
    
    private static String i18n(String id) {
        return NbBundle.getMessage(AnalyzeModel.class,id);
    }
    
    @Override
    public boolean isApplicable(ProjectProxy project) {
        if (project.getProject() != null){
            Project makeProject = project.getProject();
            ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (pdp.gotDescriptor()) {
                CsmProject langProject = CsmModelAccessor.getModel().getProject(makeProject);
                if (langProject != null/* && langProject.isStable(null)*/){
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public DiscoveryExtensionInterface.Applicable canAnalyze(ProjectProxy project) {
        return new ApplicableImpl(true, null, null, 40, false, null, null, null, null);
    }
    
    private class MyConfiguration implements Configuration{
        private List<SourceFileProperties> myFileProperties;
        private List<String> myIncludedFiles;
        private MakeConfigurationDescriptor makeConfigurationDescriptor;
        private CsmProject langProject;
        private Progress progress;
        
        private MyConfiguration(ProjectProxy project, Progress progress){
            Project makeProject = project.getProject();
            this.progress = progress;
            langProject = CsmModelAccessor.getModel().getProject(makeProject);
            ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
            makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        }
        
        @Override
        public List<ProjectProperties> getProjectConfiguration() {
            return ProjectImpl.divideByLanguage(getSourcesConfiguration());
        }
       
        @Override
        public List<String> getDependencies() {
            return null;
        }
        
        public boolean isExcluded(Item item){
            MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
            ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
            if (itemConfiguration == null) {
                return true;
            }
            BooleanConfiguration excl =itemConfiguration.getExcluded();
            return excl.getValue();
        }
        
        private List<SourceFileProperties> getSourceFileProperties(String root){
            List<SourceFileProperties> res = new ArrayList<SourceFileProperties>();
            if (root != null) {
                Map<String,List<String>> searchBase = search(root);
                PkgConfig pkgConfig = PkgConfigManager.getDefault().getPkgConfig(makeConfigurationDescriptor.getActiveConfiguration());
                boolean preferLocal = ((Boolean)getProperty(PREFER_LOCAL_FILES).getValue()).booleanValue();
                Item[] items = makeConfigurationDescriptor.getProjectItems();
                Map<String,Item> projectSearchBase = new HashMap<String,Item>();
                for (int i = 0; i < items.length; i++){
                    if (isStoped) {
                        break;
                    }
                    Item item = items[i];
                    String path = item.getNormalizedPath();
                    projectSearchBase.put(path, item);
                }
                for (int i = 0; i < items.length; i++){
                    if (isStoped) {
                        break;
                    }
                    Item item = items[i];
                    if (!isExcluded(item)) {
                        Language lang = item.getLanguage();
                        if (lang == Language.C || lang == Language.CPP){
                            CsmFile langFile = langProject.findFile(item, false);
                            SourceFileProperties source = new ModelSource(item, langFile, searchBase, projectSearchBase, pkgConfig, preferLocal);
                            res.add(source);
                        }
                    }
                }
            }
            return res;
        }
        
        @Override
        public List<SourceFileProperties> getSourcesConfiguration() {
            if (myFileProperties == null){
                myFileProperties = getSourceFileProperties((String)getProperty(MODEL_FOLDER_KEY).getValue());
            }
            return myFileProperties;
        }
        
        @Override
        public List<String> getIncludedFiles(){
            if (myIncludedFiles == null) {
                HashSet<String> unique = new HashSet<String>();
                Item[] items = makeConfigurationDescriptor.getProjectItems();
                if (progress != null){
                    progress.start(items.length);
                }
                try {
                    for (int i = 0; i < items.length; i++){
                        if (isStoped) {
                            break;
                        }
                        Item item = items[i];
                        if (isExcluded(item)) {
                            continue;
                        }
                        String path = item.getAbsPath();
                        File file = new File(path);
                        if (CndFileUtils.exists(file)) {
                            unique.add(CndFileUtils.normalizeAbsolutePath(file.getAbsolutePath()));
                        }
                    }
                    HashSet<String> unUnique = new HashSet<String>();
                    for(SourceFileProperties source : getSourcesConfiguration()){
                        if (source instanceof ModelSource){
                            unUnique.addAll( ((ModelSource)source).getIncludedFiles() );
                        }
                        if (progress != null){
                            progress.increment(null);
                        }
                    }
                    for(String path : unUnique){
                        File file = new File(path);
                        if (CndFileUtils.exists(file)) {
                            unique.add(CndFileUtils.normalizeAbsolutePath(file.getAbsolutePath()));
                        }
                    }
                    myIncludedFiles = new ArrayList<String>(unique);
                } finally {
                    if (progress != null){
                        progress.done();
                    }
                }
            }
            return myIncludedFiles;
        }
    }
}
