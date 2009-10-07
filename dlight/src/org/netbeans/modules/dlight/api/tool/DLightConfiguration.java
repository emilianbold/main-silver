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
package org.netbeans.modules.dlight.api.tool;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.tool.impl.DLightConfigurationAccessor;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 * Represents D-Light Configuration. Register your D-Light in D-Light filesystem.
 * Use the following example:
 * <pre>
 * &lt;filesystem&gt;
&lt;folder name="DLight"&gt;
&lt;folder name="Configurations"&gt;
&lt;folder name="MyFavoriteConfiguration"&gt;
&lt;folder name="KnownToolsConfigurationProviders"&gt;
&lt;file name="MyDLightToolConfigurationProvider.shadow"&gt;
&lt;attr name="originalFile" stringvalue="DLight/ToolConfigurationProviders/MyDLightToolConfigurationProvider.instance"/&gt;
&lt;/file&gt;
&lt;file name="MemoryToolConfigurationProvider.shadow"&gt;
&lt;attr name="originalFile" stringvalue="DLight/ToolConfigurationProviders/MemoryToolConfigurationProvider.instance"/&gt;
&lt;/file&gt;
&lt;file name="SyncToolConfigurationProvider.shadow"&gt;
&lt;attr name="originalFile" stringvalue="DLight/ToolConfigurationProviders/SyncToolConfigurationProvider.instance"/&gt;
&lt;/file&gt;
&lt;/folder&gt;
&lt;/folder&gt;
&lt;/folder&gt;
&lt;/folder&gt;
&lt;/filesystem&gt;
</pre>
 */
public final class DLightConfiguration {

    static{
        DLightConfigurationAccessor.setDefault(new DLightConfigurationAccessorImpl());
    }

    private static final String CONFIGURATION_OPTIONS = "ConfigurationOptions";//NOI18N
    private final FileObject rootFolder;
    private final ToolsConfiguration toolsConfiguration;
    private final DLightConfigurationOptions configurationOptions;
    private final String displayedName;
    private final String category;
    private final String[] platforms;
    private final String collectorProviders;
    private final String[] indicatorProviders;
    private final boolean isHidden;
    private final boolean isSystem;
    private final boolean isDefault;
    private final DLightConfigurationOptionsListener listener = new DLightConfigurationOptionsListenerImpl();

    static DLightConfiguration create(FileObject configurationRoot) {
        return new DLightConfiguration(configurationRoot);
    }

    static DLightConfiguration createDefault() {
        FileObject fsRoot = FileUtil.getConfigRoot();//epository.getDefault().getDefaultFileSystem();
        FileObject toolConfigurations = fsRoot.getFileObject("DLight/ToolConfigurationProviders");//NOI18N
        return new DLightConfiguration(fsRoot.getFileObject("DLight"), ToolsConfiguration.createDefault(toolConfigurations));//NOI18N
    }

    private DLightConfiguration(FileObject configurationRoot) {
        this(configurationRoot, ToolsConfiguration.create(configurationRoot));
    }

    private DLightConfiguration(FileObject configurationRoot, ToolsConfiguration toolsConfiguration) {
        this.toolsConfiguration = toolsConfiguration;
        this.rootFolder = configurationRoot;
        this.configurationOptions = getConfigurationOptions();
        this.displayedName = configurationRoot.getAttribute("displayedName") == null ? configurationRoot.getName() : (String)configurationRoot.getAttribute("displayedName");//NOI18N
        this.isHidden = configurationRoot.getAttribute("hidden") != null && (Boolean)configurationRoot.getAttribute("hidden"); // NOI18N
        this.isSystem = configurationRoot.getAttribute("system") != null && (Boolean)configurationRoot.getAttribute("system"); // NOI18N
        this.isDefault = configurationRoot.getAttribute("default") != null && (Boolean)configurationRoot.getAttribute("default"); // NOI18N
        this.category = configurationRoot.getAttribute("category") == null ? configurationRoot.getName() : (String)configurationRoot.getAttribute("category");//NOI18N
        this.platforms = configurationRoot.getAttribute("platforms") == null ? null : ((String)configurationRoot.getAttribute("platforms")).split(",");//NOI18N
        this.collectorProviders = configurationRoot.getAttribute("collector.providers") == null ? null : ((String)configurationRoot.getAttribute("collector.providers"));//NOI18N
        this.indicatorProviders = configurationRoot.getAttribute("indicator.providers") == null ? null : ((String)configurationRoot.getAttribute("indicator.providers")).split(",");//NOI18N
    }

    public final String getDisplayedName(){
        return displayedName;
    }

    public final String getCategoryName(){
        return category;
    }

    public final List<String> getPlatforms() {
        return Arrays.asList(platforms);
    }

    public final String getCollectorProviders() {
        return collectorProviders;
    }
    

    public final List<String> getIndicatorProviders() {
        return Arrays.asList(indicatorProviders);
    }

    boolean isSystem(){
        return isSystem;
    }

    boolean isDefault(){
        return isDefault;
    }


    final ToolsConfiguration getToolsConfiguration(){
        return toolsConfiguration;
    }

    /**
     * This method returns the list of tools for the current configuration
     * @return list of tools used in the current configuration
     */
    public List<DLightTool> getToolsSet() {
        synchronized (toolsConfiguration) {
            return toolsConfiguration.getToolsSet();
        }
    }

    public List<Indicator<?>> getIndicators() {
        List<Indicator<?>> result = new ArrayList<Indicator<?>>();
        List<DLightTool> tools = getToolsSet();
        for (DLightTool tool : tools) {
            result.addAll(tool.getIndicators());
        }
        return result;
    }

    private final List<Indicator<?>> getEnabledIndicators() {
        List<Indicator<?>> result = new ArrayList<Indicator<?>>();
        List<DLightTool> tools = getToolsSet();
        for (DLightTool tool : tools) {
            if (tool.isEnabled()){
                result.addAll(tool.getIndicators());
            }
        }
        return result;
    }

    public DLightTool getToolByID(String id) {
        if (id == null) {
            return null;
        }

        List<DLightTool> list;

        synchronized (toolsConfiguration) {
            list = new ArrayList<DLightTool>(toolsConfiguration.getToolsSet());
        }

        for (DLightTool tool : list) {
            if (id.equals(tool.getID())) {
                return tool;
            }
        }

        return null;
    }

    public DLightTool getToolByName(String name) {
        if (name == null) {
            return null;
        }

        List<DLightTool> list;

        synchronized (toolsConfiguration) {
            list = new ArrayList<DLightTool>(toolsConfiguration.getToolsSet());
        }

        for (DLightTool tool : list) {
            if (name.equals(tool.getName())) {
                return tool;
            }
        }

        return null;
    }

    public DLightConfigurationOptions getConfigurationOptions(boolean template) {
        if (template) {
            getConfigurationOptions();
        }   
        return configurationOptions;
    }

    private DLightConfigurationOptions getConfigurationOptions() {
        FileObject configurationsFolder = rootFolder.getFileObject(CONFIGURATION_OPTIONS);

        if (configurationsFolder == null) {
            return new DefaultConfigurationOption();
        }

        FileObject[] children = configurationsFolder.getChildren();

        if (children == null || children.length == 0) {
            return new DefaultConfigurationOption();
        }

        for (FileObject child : children) {

            DataObject dobj = null;
            try {
                dobj = DataObject.find(child);
            } catch (DataObjectNotFoundException ex) {
                Logger.getLogger(ToolsConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            }

            InstanceCookie ic = dobj.getCookie(InstanceCookie.class);
            if (ic == null) {
                String message = "D-Light options configuration " + child.getName() + " not found"; //NOI18N
                Logger.getLogger(ToolsConfiguration.class.getName()).log(Level.SEVERE, message, new Exception(message));
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Class<? extends DLightConfigurationOptions> clazz = (Class<? extends DLightConfigurationOptions>) ic.instanceClass();
                DLightConfigurationOptions configurationProvider = clazz.getConstructor().newInstance();
                configurationProvider.addListener(listener);
                return configurationProvider;
//                result.add(DLightToolAccessor.getDefault().newDLightTool(configurationProvider.create()));
//        Class<? extends DLightTool.Configuration> clazz = (Class<? extends DLightTool>) ic.instanceClass();
//        result.add(clazz.getConstructor().newInstance());
            } catch (InstantiationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    public String getConfigurationName() {
        return rootFolder.getName();
    }

    private class DefaultConfigurationOption implements DLightConfigurationOptions {

        private boolean turnState = true;

        public void turnCollectorsState(boolean turnState) {
            this.turnState = turnState;
        }

        public boolean areCollectorsTurnedOn() {
            return turnState;
        }

        public List<DLightTool> getToolsSet() {
            synchronized (toolsConfiguration) {
                return toolsConfiguration.getToolsSet();
            }
        }

        public List<DataCollector<?>> getCollectors(DLightTool tool) {
            return tool.getCollectors();
        }

        public List<IndicatorDataProvider<?>> getIndicatorDataProviders(DLightTool tool) {
            return tool.getIndicatorDataProviders();
        }

        public boolean validateToolsRequiredUserInteraction() {
            return true;
        }

        public boolean profileOnRun() {
            return false;
        }

        public Collection<String> getActiveToolNames() {
            Collection<String> toolIDs = new ArrayList<String>();
            for(DLightTool tool : getToolsSet()){
                if (tool.isEnabled()){
                    toolIDs.add(tool.getID());
                }
            }
            return toolIDs;
        }

        public void addListener(DLightConfigurationOptionsListener listener) {
       //     throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeListener(DLightConfigurationOptionsListener listener) {
       //     throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private final class DLightConfigurationOptionsListenerImpl implements DLightConfigurationOptionsListener{

        public boolean dlightToolEnabling(String toolName, boolean isEnabled) {
            if (getToolByName(toolName) == null){
                return false;
            }
            if (isEnabled){
                getToolByName(toolName).enable();
            }else{
                getToolByName(toolName).disable();
            }
            return true;
        }
        
    }

    private static final class DLightConfigurationAccessorImpl extends DLightConfigurationAccessor{

        @Override
        public List<Indicator<?>> getEnabledIndicators(DLightConfiguration configuration) {
            return configuration.getEnabledIndicators();
        }

        @Override
        public boolean isHidden(DLightConfiguration configuration) {
            return configuration.isHidden;
        }
        
    }
   
}
