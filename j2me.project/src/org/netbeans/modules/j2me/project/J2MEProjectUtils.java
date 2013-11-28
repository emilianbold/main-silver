/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.ListCellRenderer;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Profile;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2me.project.ui.PlatformsComboBoxModel;
import org.netbeans.modules.java.api.common.ui.PlatformFilter;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Roman Svitanic
 */
public class J2MEProjectUtils {

    private static ButtonGroup configurationsGroup;
    private static ButtonGroup profilesGroup;
    private static ArrayList<JCheckBox> optionalPackages;
    private static HashMap<String, J2MEPlatform.J2MEProfile> name2profileAll;

    public static ComboBoxModel createPlatformComboBoxModel() {
        return new DefaultComboBoxModel(readPlatforms());
    }

    public static ButtonGroup getConfigurationsButtonGroup() {
        return configurationsGroup;
    }

    public static ButtonGroup getProfilesButtonGroup() {
        return profilesGroup;
    }

    public static ArrayList<JCheckBox> getOptionalPackages() {
        return optionalPackages;
    }

    public static HashMap<String, J2MEPlatform.J2MEProfile> getNameToProfileMap() {
        if (name2profileAll == null || name2profileAll.isEmpty()) {
            readPlatforms();
        }
        return name2profileAll;
    }

    public static JavaPlatform[] readPlatforms() {
        configurationsGroup = new ButtonGroup();
        profilesGroup = new ButtonGroup();
        optionalPackages = new ArrayList<>();
        name2profileAll = new HashMap<>();
        // Read defined platforms and all configurations, profiles and optional packages
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(null, new Specification(J2MEPlatform.SPECIFICATION_NAME, new SpecificationVersion("8.0"))); //NOI18N
        Arrays.sort(platforms, new Comparator<JavaPlatform>() {
            @Override
            public int compare(final JavaPlatform o1, final JavaPlatform o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });
        HashMap<J2MEPlatform.J2MEProfile, J2MEPlatform.J2MEProfile> cfg = new HashMap<>(),
                prof = new HashMap<>(),
                opt = new HashMap<>();
        for (JavaPlatform platform1 : platforms) {
            if (platform1 instanceof J2MEPlatform) {
                J2MEPlatform platform = (J2MEPlatform) platform1;
                Profile profiles[] = platform.getSpecification().getProfiles();
                for (Profile profile : profiles) {
                    if (profile instanceof J2MEPlatform.J2MEProfile) {
                        J2MEPlatform.J2MEProfile p = (J2MEPlatform.J2MEProfile) profile;
                        name2profileAll.put(p.toString(), p);
                        switch (p.getType()) {
                            case J2MEPlatform.J2MEProfile.TYPE_CONFIGURATION:
                                p = takeBetter(p, cfg.remove(p));
                                cfg.put(p, p);
                                break;
                            case J2MEPlatform.J2MEProfile.TYPE_PROFILE:
                                p = takeBetter(p, prof.remove(p));
                                prof.put(p, p);
                                break;
                            case J2MEPlatform.J2MEProfile.TYPE_OPTIONAL:
                                p = takeBetter(p, opt.remove(p));
                                opt.put(p, p);
                                break;
                        }
                    }
                }
            }
        }
        J2MEPlatform.J2MEProfile arr[] = cfg.values().toArray(new J2MEPlatform.J2MEProfile[cfg.size()]);
        initAllConfigurations(arr);
        arr = prof.values().toArray(new J2MEPlatform.J2MEProfile[prof.size()]);
        initAllProfiles(arr);
        arr = opt.values().toArray(new J2MEPlatform.J2MEProfile[opt.size()]);
        initAllOptionalPackages(arr);
        return platforms;
    }

    private static J2MEPlatform.J2MEProfile takeBetter(final J2MEPlatform.J2MEProfile p1, final J2MEPlatform.J2MEProfile p2) {
        if (p1 == null) {
            return p2;
        }
        if (p2 == null) {
            return p1;
        }
        return p1.getDisplayNameWithVersion().length() > p2.getDisplayNameWithVersion().length() ? p1 : p2;
    }

    private static void initAllConfigurations(final J2MEPlatform.J2MEProfile cfgs[]) {
        Arrays.sort(cfgs);
        for (J2MEPlatform.J2MEProfile cfg : cfgs) {
            final JRadioButton btn = new JRadioButton(cfg.toString()); // TO DO some text formating
            btn.setToolTipText(cfg.getDisplayNameWithVersion());
            btn.setActionCommand(cfg.toString());
            configurationsGroup.add(btn);
        }
    }

    private static void initAllProfiles(final J2MEPlatform.J2MEProfile profs[]) {
        Arrays.sort(profs);
        for (J2MEPlatform.J2MEProfile prof : profs) {
            final JRadioButton btn = new JRadioButton(prof.toString()); // TO DO some text formating
            btn.setToolTipText(prof.getDisplayNameWithVersion());
            btn.setActionCommand(prof.toString());
            profilesGroup.add(btn);
        }
    }

    private static void initAllOptionalPackages(final J2MEPlatform.J2MEProfile opts[]) {
        Arrays.sort(opts, new Comparator<J2MEPlatform.J2MEProfile>() {
            @Override
            public int compare(final J2MEPlatform.J2MEProfile o1, final J2MEPlatform.J2MEProfile o2) {
                return o1.getDisplayNameWithVersion().compareTo(o2.getDisplayNameWithVersion());
            }
        });
        for (J2MEPlatform.J2MEProfile opt : opts) {
            final String dName = opt.isNameIsJarFileName() ? opt.getDisplayName() : opt.getDisplayNameWithVersion();
            final JCheckBox cb = new JCheckBox(dName);
            cb.setToolTipText(dName);
            cb.setActionCommand(opt.toString());
            optionalPackages.add(cb);
        }
    }

    public static ComboBoxModel createJDKPlatformComboBoxModel() {
        return new PlatformsComboBoxModel(PlatformUiSupport.createPlatformComboBoxModel(null,
                Arrays.<PlatformFilter>asList(new PlatformFilter() {
                    @Override
                    public boolean accept(JavaPlatform platform) {
                        return new SpecificationVersion("1.8").compareTo(platform.getSpecification().getVersion()) <= 0; //NOI18N
                    }
                })));
    }

    public static ListCellRenderer createJDKPlatformListCellRenderer() {
        return PlatformUiSupport.createPlatformListCellRenderer();
    }
}