/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.cnd.gizmo.options;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.gizmo.spi.GizmoOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.IntConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationManager;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;

public class GizmoOptionsImpl implements ConfigurationAuxObject, GizmoOptions {
    private static final String GIZMO_CATEGORY = "Gizmo"; // NOI18N
    public static final String PROFILE_ID = "gizmo_options"; // NOI18N
    private final PropertyChangeSupport pcs;
    private boolean needSave = false;
    private String baseDir;
    private BooleanConfiguration profileOnRun;
    public static final String PROFILE_ON_RUN_PROP = "profileOnRun"; // NOI18N
    public static final String DATA_PROVIDER_PROP = "dataProvider"; // NOI18N
    public static final String CONFIGURATION_PROP = "configuration"; // NOI18N
    private String[] configurationNames;
    private DLightConfiguration[] configurations;
    private IntConfiguration gizmoConfigurations = null;
    private String preferredConfigurationName = null;

    public GizmoOptionsImpl(String baseDir, PropertyChangeSupport pcs) {
        this.baseDir = baseDir;
        this.pcs = pcs;
        profileOnRun = new BooleanConfiguration(null, true, null, null);
    }

    public DLightConfiguration getDLightConfiguration() {
        if (configurations == null || gizmoConfigurations == null || gizmoConfigurations.getValue() < 0 || gizmoConfigurations.getValue() >= configurations.length) {
            return null;
        }
        return configurations[gizmoConfigurations.getValue()];
    }

    public void setPreferredDLightConfiguration(String name) {
        preferredConfigurationName = name;
    }
    

    public void init(Configuration conf) {
        MakeConfiguration makeConfiguration = (MakeConfiguration) conf;
        ExecutionEnvironment execEnv = makeConfiguration.getDevelopmentHost().getExecutionEnvironment();

        //if we have sun studio compiler along compiler collections presentedCompiler
        CompilerSetManager compilerSetManager = CompilerSetManager.getDefault(execEnv);
        List<CompilerSet> compilers = compilerSetManager.getCompilerSets();
        boolean hasSunStudio = false;
        for (CompilerSet cs : compilers) {
            if (cs.isSunCompiler()) {
                hasSunStudio = true;
                break;
            }
        }
        String platform = makeConfiguration.getDevelopmentHost().getBuildPlatformDisplayName();

        // check whether list has changed
        if (gizmoConfigurations != null) {
            List<DLightConfiguration> newConfList = new ArrayList<DLightConfiguration>();
            for (DLightConfiguration dlightConfiguration : DLightConfigurationManager.getInstance().getConfigurationsByCategoryName(GIZMO_CATEGORY)) {
                if (dlightConfiguration.getPlatforms() != null){
                    List<String> platforms = dlightConfiguration.getPlatforms();
                    for (String supportedPlatform : platforms){
                        if (platform.indexOf(supportedPlatform) != -1){
                            newConfList.add(dlightConfiguration);
                            break;
                        }
                    }
                }
            }
            int i = 0;
            for (DLightConfiguration dlc : newConfList) {
                if (!dlc.getConfigurationName().equals(configurations[i++].getConfigurationName())) {
                    // List has changed. Re-create but keep current selection.
                    preferredConfigurationName = configurations[gizmoConfigurations.getValue()].getConfigurationName();
                    gizmoConfigurations = null; // Will trigger new list to be created
                    break;
                }
            }
        }

        // Gizmo configurations
        if (gizmoConfigurations == null || configurationNames.length == 0) {
            List<String> names = new ArrayList<String>();
            List<DLightConfiguration> confs = new ArrayList<DLightConfiguration>();
            for (DLightConfiguration dlightConfiguration : DLightConfigurationManager.getInstance().getConfigurationsByCategoryName(GIZMO_CATEGORY)) {
                    if (dlightConfiguration.getPlatforms() != null){
                        List<String> platforms = dlightConfiguration.getPlatforms();
                        for (String supportedPlatform : platforms){
                            if (platform.indexOf(supportedPlatform) != -1){
                                names.add(dlightConfiguration.getDisplayedName());
                                confs.add(dlightConfiguration);
                                break;
                            }
                        }
                    }

            }
            configurationNames = names.toArray(new String[names.size()]);
            configurations = confs.toArray(new DLightConfiguration[confs.size()]);
            // Figure out default;
            int defConf = -1;
            if (preferredConfigurationName != null) {
                defConf = getConfigurationIndexByName(preferredConfigurationName);
                preferredConfigurationName = null;
            }
            if (defConf == -1) {
                if (platform.indexOf("Solaris") >= 0 || platform.indexOf("Linux") >= 0) { // NOI18N
                    if (hasSunStudio || platform.indexOf("Linux") >= 0) { // NOI18N
                        defConf = getConfigurationIndexByName("GizmoSunStudioStandard"); // NOI18N
                    } else {
                        defConf = getConfigurationIndexByName("GizmoDTraceStandard"); // NOI18N
                    }
                } else {
                    defConf = getConfigurationIndexByName("GizmoSimple"); // NOI18N
                }
            }
            if (defConf == -1) {
                defConf = 0;
            }
            gizmoConfigurations = new IntConfiguration(null, defConf, configurationNames, null);
        }
    }

    private int getConfigurationIndexByName(String name) {
        int index = 0;
        for (DLightConfiguration dlightConfiguration : configurations) {
            if (dlightConfiguration.getConfigurationName().equals(name)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public String getConfiurationNameValue() {
        return configurations[getGizmoConfigurations().getValue()].getConfigurationName();
    }

    public Collection<String> getNames() {
        List<DLightTool> tools = new ArrayList<DLightTool>(getDLightConfiguration().getToolsSet());
        Collection<String> result = new ArrayList<String>();
        for (DLightTool tool : tools){
            result.add(tool.getID());
        }
        return result;
    }

    public void initialize() {
        clearChanged();
    }

    public static GizmoOptionsImpl getOptions(Configuration conf) {
        GizmoOptionsImpl gizmoOptions = (GizmoOptionsImpl) conf.getAuxObject(GizmoOptionsImpl.PROFILE_ID);
        gizmoOptions.init(conf);
        return gizmoOptions;
    }

    public boolean isModified() {
        return getProfileOnRun().getModified();
    }

    public String getId() {
        return PROFILE_ID;
    }

    private void checkPropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        if (oldValue != newValue && pcs != null) {
            pcs.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    private void checkPropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (oldValue != newValue && pcs != null) {
            pcs.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Profile On Run
     */
    public BooleanConfiguration getProfileOnRun() {
        return profileOnRun;
    }

    public boolean getProfileOnRunValue() {
        return getProfileOnRun().getValue();
    }

    public void setProfileOnRun(BooleanConfiguration profileOnRun) {
        this.profileOnRun = profileOnRun;
    }

    public void setProfileOnRunValue(boolean profileOnRunValue) {
        boolean oldValue = getProfileOnRunValue();
        getProfileOnRun().setValue(profileOnRunValue);
        checkPropertyChange(PROFILE_ON_RUN_PROP, oldValue, getProfileOnRunValue());
    }


    /**
     * @return the gizmoConfigurations
     */
    public IntConfiguration getGizmoConfigurations() {
        return gizmoConfigurations;
    }

    /**
     * @param gizmoConfigurations the gizmoConfigurations to set
     */
    public void setGizmoConfigurations(IntConfiguration gizmoConfigurations) {
        this.gizmoConfigurations = gizmoConfigurations;
    }

    public boolean shared() {
        return false;
    }

    public XMLDecoder getXMLDecoder() {
        return new GizmoOptionsXMLCodec(this);
    }

    public XMLEncoder getXMLEncoder() {
        return new GizmoOptionsXMLCodec(this);
    }

    // interface ProfileAuxObject
    public boolean hasChanged() {
        return needSave;
    }

    // interface ProfileAuxObject
    public void clearChanged() {
        needSave = false;
    }

    public void assign(ConfigurationAuxObject auxObject) {
        boolean oldBoolValue;
        GizmoOptionsImpl gizmoOptions = (GizmoOptionsImpl) auxObject;

        oldBoolValue = getProfileOnRun().getValue();
        getProfileOnRun().assign(gizmoOptions.getProfileOnRun());
        checkPropertyChange(PROFILE_ON_RUN_PROP, oldBoolValue, getProfileOnRunValue());
        if (getGizmoConfigurations() != null) { // IZ173904, FIXUP: configuration dup will loose gizmo configuration setting
            getGizmoConfigurations().assign(gizmoOptions.getGizmoConfigurations());
        }
        else {
            preferredConfigurationName = gizmoOptions.getConfiurationNameValue();
        }
    }

    @Override
    public GizmoOptionsImpl clone(Configuration c) {
        init(c);
        GizmoOptionsImpl clone = new GizmoOptionsImpl(getBaseDir(), null);
        clone.init(c);
        clone.setProfileOnRun(getProfileOnRun().clone());
        clone.setGizmoConfigurations(getGizmoConfigurations().clone());
        return clone;
    }

    public String getBaseDir() {
        return baseDir;
    }
    /** Look up i18n strings here */
    private static ResourceBundle bundle;

    protected static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(GizmoOptionsImpl.class);
        }
        return bundle.getString(s);
    }
}
