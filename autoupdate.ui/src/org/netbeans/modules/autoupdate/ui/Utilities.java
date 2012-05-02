/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.autoupdate.ui.actions.Installer;
import org.netbeans.modules.autoupdate.ui.actions.ShowNotifications;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.util.*;

/**
 *
 * @author Jiri Rechtacek
 */
public class Utilities {
    private static final Logger logger = Logger.getLogger(Utilities.class.getName());
    private static Boolean isModulesOnly;
    private static String PLUGIN_MANAGER_MODULES_ONLY = "plugin_manager_modules_only";
    private static String PLUGIN_MANAGER_SHARED_INSTALLATION = "plugin_manager_shared_installation";
    
    public static String PLUGIN_MANAGER_CHECK_INTERVAL = "plugin.manager.check.interval";
    public static String PLUGIN_MANAGER_DONT_CARE_WRITE_PERMISSION = "plugin_manager_dont_care_write_permission";
    
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat ("yyyy/MM/dd"); // NOI18N
    public static final String TIME_OF_MODEL_INITIALIZATION = "time_of_model_initialization"; // NOI18N
    public static final String TIME_OF_REFRESH_UPDATE_CENTERS = "time_of_refresh_update_centers"; // NOI18N
    
    static final String UNSORTED_CATEGORY = NbBundle.getMessage (Utilities.class, "Utilities_Unsorted_Category");
    static final String LIBRARIES_CATEGORY = NbBundle.getMessage (Utilities.class, "Utilities_Libraries_Category");
    static final String BRIDGES_CATEGORY = NbBundle.getMessage (Utilities.class, "Utilities_Bridges_Category");
    
    private static final String FIRST_CLASS_MODULES = "org.netbeans.modules.autoupdate.services, org.netbeans.modules.autoupdate.ui"; // NOI18N
    private static final String PLUGIN_MANAGER_FIRST_CLASS_MODULES = "plugin.manager.first.class.modules"; // NOI18N
    
    private static final String ALLOW_SHOWING_BALLOON = "plugin.manager.allow.showing.balloon"; // NOI18N
    private static final String SHOWING_BALLOON_TIMEOUT = "plugin.manager.showing.balloon.timeout"; // NOI18N

    private static final RequestProcessor WORKER_THREADS_PROCESSOR = new RequestProcessor("autoupdate-ui-worker", 10, false);
    
    private static Collection<String> first_class_modules = null;
    
    private static Set<String> acceptedLicenseIDs;
    public static final String PLUGIN_MANAGER_ACCEPTED_LICENSE_IDS = "plugin_manager_accepted_license_ids"; // NOI18N
    
    @SuppressWarnings ("deprecation")
    public static List<UnitCategory> makeInstalledCategories (List<UpdateUnit> units) {
        //units = filterUneditable(units);
        List<UnitCategory> res = new ArrayList<UnitCategory> ();
        List<String> names = new ArrayList<String> ();
        for (UpdateUnit u : units) {
            UpdateElement el = u.getInstalled();
            if (el != null || u.isPending ()) {
                String catName = el == null && u.isPending () ? u.getAvailableUpdates ().get (0).getCategory () : el.getCategory ();
                Unit.Installed i = new Unit.Installed (u, catName);
                if (names.contains(catName)) {
                    UnitCategory cat = res.get(names.indexOf(catName));
                    cat.addUnit (i);
                } else {
                    UnitCategory cat = new UnitCategory(catName);
                    cat.addUnit (i);
                    res.add(cat);
                    names.add(catName);
                }
                String licenseId = el.getLicenseId();
                if (licenseId != null && getAcceptedLicenseIds().add(licenseId)) {
                    logger.fine("License ID - Yet another license " + licenseId + " was accepted during installation.");
                }
            }
        }
        logger.log(Level.FINER, "makeInstalledCategories (" + units.size() + ") returns " + res.size());
        return res;
    }
    
    private static Set<String> getAcceptedLicenseIds() {
        if (acceptedLicenseIDs == null) {
            loadAcceptedLicenseIDs(0);
        }
        return acceptedLicenseIDs;
    }

    public static boolean isLicenseIdApproved(String licenseId) {
        if (licenseId == null) {
            return false;
        }
        logger.finest("License ID - Was " + licenseId + " accepted? " + getAcceptedLicenseIds().contains(licenseId));
        return getAcceptedLicenseIds().contains(licenseId);
    }
    
    public static void addAcceptedLicenseIDs(Collection licenseIds) {
        if (licenseIds != null && getAcceptedLicenseIds().addAll(licenseIds)) {
            logger.fine("License ID - License ID " + licenseIds + " was accepted.");
        }
    }
    
    public static void storeAcceptedLicenseIDs() {
        StringBuilder sb = new StringBuilder();
        for(String licenseId : acceptedLicenseIDs) {
            sb.append(licenseId).append(",");
        }
        getPreferences().put(PLUGIN_MANAGER_ACCEPTED_LICENSE_IDS, sb.substring(0, sb.length() - 1));
        logger.fine("License IDs - Stored: " + sb.substring(0, sb.length() - 1));
        acceptedLicenseIDs = null;
    }
    
    public static void loadAcceptedLicenseIDs(int capacity) {
        if (acceptedLicenseIDs == null) {
            acceptedLicenseIDs = new HashSet<String> (capacity);
        }
        String storedIds = getPreferences().get(PLUGIN_MANAGER_ACCEPTED_LICENSE_IDS, null);
        if (storedIds != null) {
            acceptedLicenseIDs.addAll(Arrays.asList(storedIds.split(",")));
        }
        logger.fine("License IDs - Loaded: " + acceptedLicenseIDs);
    }
            
    public static List<UnitCategory> makeUpdateCategories (final List<UpdateUnit> units, boolean isNbms) {
        long start = System.currentTimeMillis();
        if (! isNbms && ! units.isEmpty ()) {
            List<UnitCategory> fcCats = makeFirstClassUpdateCategories ();
            if (! fcCats.isEmpty ()) {
                return fcCats;
            } else if(hasPendingFirstClassModules()) {
                return new ArrayList <UnitCategory>();
            }
        }
        List<UnitCategory> res = new ArrayList<UnitCategory> ();
        if(units.isEmpty()) {
            return res;
        }

        List<String> names = new ArrayList<String> ();
        Set<UpdateUnit> coveredByVisible = new HashSet <UpdateUnit> ();
        Map<UpdateUnit, List<UpdateElement>> coveredByVisibleMap = new HashMap <UpdateUnit, List<UpdateElement>> ();

        for (UpdateUnit u : units) {
            UpdateElement el = u.getInstalled ();
            if (! u.isPending() && el != null && (el.isEnabled() || isNbms)) {
                List<UpdateElement> updates = u.getAvailableUpdates ();
                if (updates.isEmpty()) {
                    continue;
                }
                coveredByVisible.add(u);

                OperationContainer<InstallSupport> container = OperationContainer.createForUpdate();
                OperationInfo<InstallSupport> info = container.add(updates.get(0));
                Set <UpdateElement> required = info.getRequiredElements();
                for(UpdateElement ue : required){
                    if(!ue.getUpdateUnit().isPending()) {
                        coveredByVisible.add(ue.getUpdateUnit());
                    }
                }
                for(OperationInfo <InstallSupport> i : container.listAll()) {
                    if(!i.getUpdateUnit().isPending()) {
                        coveredByVisible.add((i.getUpdateUnit()));
                    }
                }
                //coveredByVisibleMap.put(u,list);

                String catName = el.getCategory();
                if (names.contains (catName)) {
                    UnitCategory cat = res.get (names.indexOf (catName));
                    cat.addUnit (new Unit.Update (u, isNbms, catName));
                } else {
                    UnitCategory cat = new UnitCategory (catName);
                    cat.addUnit (new Unit.Update (u, isNbms, catName));
                    res.add (cat);
                    names.add (catName);
                }
            }
        }

        // not covered by visible modules

        Collection<UpdateUnit> allUnits = UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.MODULE);
        Set <UpdateUnit> otherUnits = new HashSet <UpdateUnit> ();
        for(UpdateUnit u : allUnits) {
            if(!coveredByVisible.contains(u) && 
                    u.getAvailableUpdates().size() > 0 &&
                    u.getInstalled()!=null &&
                    !u.isPending() &&
                    (u.getInstalled().isEnabled() ||
                        OperationContainer.createForEnable().canBeAdded(u, u.getInstalled()))) { // just disabled, but can be enabled: i.e. not eager/autoload
                otherUnits.add(u);
            }
        }

        List<Unit.InternalUpdate> internals = new ArrayList <Unit.InternalUpdate>();
        createVisibleModulesDependecyMap(units, coveredByVisibleMap);

        Set <UpdateUnit> invisibleElementsWithoutVisibleParent = new HashSet <UpdateUnit> ();

        if(otherUnits.size() > 0 && !isNbms) {
            for(UpdateUnit uu : otherUnits) {
                UpdateUnit u = getVisibleUnitForInvisibleModule(uu, coveredByVisibleMap);
                if (u != null) {
                    if (u.getInstalled() != null && !u.getInstalled().isEnabled()) {
                        continue;
                    }
                    boolean exist = false;
                    for(Unit.InternalUpdate internal : internals) {
                        if(internal.getVisibleUnit() == u) {
                            internal.getUpdateUnits().add(uu);
                            exist = true;
                        }
                    }
                    if(!exist) {
                        //all already determined "internal" visible updates does not contain just found one
                        String catName = u.getInstalled().getCategory();
                        Unit.InternalUpdate iu = new Unit.InternalUpdate(u, catName, false);
                        iu.getUpdateUnits().add(uu);
                        internals.add(iu);
                        UnitCategory cat = new UnitCategory(catName);
                        res.add(cat);
                        names.add(catName);
                        cat.addUnit(iu);
                    }
                } else {
                    // fallback, show module itself
                    invisibleElementsWithoutVisibleParent.add(uu);
                }
            }            
        }

        Map<UpdateUnit, List<UpdateElement>> coveredByInvisibleMap = new HashMap <UpdateUnit, List<UpdateElement>> ();
        createVisibleModulesDependecyMap(invisibleElementsWithoutVisibleParent, coveredByInvisibleMap);

        Set <UpdateUnit> coveredByInvisible = new HashSet <UpdateUnit>();
        for (UpdateUnit invisibleUnit : invisibleElementsWithoutVisibleParent) {
            boolean add = true;
            UpdateElement update = invisibleUnit.getAvailableUpdates().get(0);
            if (invisibleUnit.getInstalled() != null && !invisibleUnit.getInstalled().isEnabled()) {
                continue;
            }
            for(UpdateUnit key : coveredByInvisibleMap.keySet()) {
                if(key.equals(invisibleUnit)) {
                    continue;
                }
                if(coveredByInvisibleMap.get(key).contains(update) && !coveredByInvisible.contains(key)) {
                    add = false;
                    break;
                }
            }
            
            if (add) {
                String catName = update.getCategory();
                UnitCategory cat;

                if (names.contains(catName)) {
                    cat = res.get(names.indexOf(catName));
                } else {
                    cat = new UnitCategory(catName);
                    res.add(cat);
                    names.add(catName);
                }
                cat.addUnit(new Unit.Update(invisibleUnit, isNbms, cat.getCategoryName()));
            } else {
                coveredByInvisible.add(invisibleUnit);
            }
        }

        for(Unit.InternalUpdate iu : internals) {
            iu.initState();
        }
        logger.log(Level.FINE, "makeUpdateCategories (" + units.size () + ") returns " + res.size () + ", took " + (System.currentTimeMillis()-start) + " ms");

        return res;
    };

    public static HashMap<UpdateUnit, List<UpdateElement>> getVisibleModulesDependecyMap(Collection<UpdateUnit> allUnits) {
        HashMap<UpdateUnit, List<UpdateElement>> result = new HashMap <UpdateUnit, List<UpdateElement>>();
        createVisibleModulesDependecyMap(allUnits, result);
        return result;
    }

    private static void createVisibleModulesDependecyMap(Collection<UpdateUnit> allUnits, Map <UpdateUnit, List<UpdateElement>> result) {
        for (UpdateUnit u : allUnits) {
            if (u.getInstalled() != null && !u.isPending() && !result.containsKey(u)) {

                OperationContainer<InstallSupport> container = u.getAvailableUpdates().isEmpty() ? 
                    OperationContainer.createForInternalUpdate() :
                    OperationContainer.createForUpdate();
                OperationInfo<InstallSupport> info = container.add(u, 
                        u.getAvailableUpdates().isEmpty() ? u.getInstalled() : u.getAvailableUpdates().get(0));

                List<UpdateElement> list = new ArrayList<UpdateElement>();

                for (UpdateElement ur : info.getRequiredElements()) {
                    if(!ur.getUpdateUnit().isPending()) {
                        list.add(ur);
                    }
                }
                for (OperationInfo<InstallSupport> in : container.listAll()) {
                    UpdateUnit unit = in.getUpdateUnit();
                    if (unit != u) {
                        List<UpdateElement> updates = unit.getAvailableUpdates();
                        if (updates.size() > 0 && !list.contains(updates.get(0))) {
                            list.add(updates.get(0));
                        }
                    }
                }
                if(!list.isEmpty()) {
                    result.put(u, list);
                }
            }
        }        
    }

    public static UpdateUnit getVisibleUnitForInvisibleModule(UpdateUnit invisible, Map<UpdateUnit, List<UpdateElement>> map) {
        List <UpdateUnit> candidates = new ArrayList<UpdateUnit>();

        for(UpdateUnit unit : map.keySet()) {
            for (UpdateElement ue : map.get(unit)) {
                if (ue.getUpdateUnit().equals(invisible)) {
                    logger.log(Level.FINE,
                            "... found candidate visible module " + unit.getCodeName() + " for invisible " + invisible.getCodeName());
                    candidates.add(unit);
                }
            }
        }

        UpdateUnit result = null;
        if(candidates.isEmpty()) {
            logger.log(Level.FINE,
                    "Have not found visible module for invisible " + invisible.getCodeName());
        } else {
            
            int overlap = 0;
            UpdateUnit takeMe = null;
            for(UpdateUnit tryMe : candidates) {
                int o = getNameOverlapping(tryMe.getCodeName(), invisible.getCodeName());
                if(o > overlap) {
                    takeMe = tryMe;
                    overlap = o;
                }
            }
            if (takeMe != null) {
                result = takeMe;
            } else {
                result = candidates.get(0);
                for (UpdateUnit u : candidates) {
                    if (u.getCodeName().endsWith(".kit")) {
                        result = u;
                        break;
                    }
                }
            }
            logger.log(Level.FINE,
                    "Found visible module " + candidates.get(0).getCodeName() + " for invisible " + invisible.getCodeName());
        }

        return result;
    }
    private static int getNameOverlapping(String cn1, String cn2) {
        String[] cn1sp = cn1.split("\\.");
        String[] cn2sp = cn2.split("\\.");

        int length1 = cn1sp.length;
        int length2 = cn2sp.length;
        int min = Math.min(length1, length2);
        int i;
        for(i=0;i<min;i++) {
            if(!cn1sp[i].equals(cn2sp[i])) {
                break;
            }
        }
        return (2 * i > min) ? i : 0;
    }
   
    public static long getTimeOfInitialization () {
        return getPreferences ().getLong (TIME_OF_MODEL_INITIALIZATION, 0);
    }
    
    public static void putTimeOfInitialization (long time) {
        getPreferences ().putLong (TIME_OF_MODEL_INITIALIZATION, time);
    }
    
    public static long getTimeOfRefreshUpdateCenters () {
        return getPreferences ().getLong (TIME_OF_REFRESH_UPDATE_CENTERS, 0);
    }

    public static void putTimeOfRefreshUpdateCenters (long time) {
        getPreferences ().putLong (TIME_OF_REFRESH_UPDATE_CENTERS, time);
    }

    private static List<UnitCategory> makeFirstClassUpdateCategories () {
        Collection<UpdateUnit> units = UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.MODULE);
        List<UnitCategory> res = new ArrayList<UnitCategory> ();
        List<String> names = new ArrayList<String> ();
        final Collection <String> firstClass = getFirstClassModules();
        for (UpdateUnit u : units) {
            UpdateElement el = u.getInstalled ();
            if (! u.isPending() && el != null) {
                List<UpdateElement> updates = u.getAvailableUpdates ();
                if (updates.isEmpty()) {
                    continue;
                }
                if (firstClass.contains (el.getCodeName ())) {
                    String catName = el.getCategory();
                    if (names.contains (catName)) {
                        UnitCategory cat = res.get (names.indexOf (catName));
                        cat.addUnit (new Unit.Update (u, false, catName));
                    } else {
                        UnitCategory cat = new UnitCategory (catName);
                        cat.addUnit (new Unit.Update (u, false, catName));
                        res.add (cat);
                        names.add (catName);
                    }
                }
            }
        }
        logger.log(Level.FINER, "makeFirstClassUpdateCategories (" + units.size () + ") returns " + res.size ());
        return res;
    }
    
    private static boolean hasPendingFirstClassModules () {
        Collection<UpdateUnit> units = UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.MODULE);
        final Collection <String> firstClass = getFirstClassModules ();
        for (UpdateUnit u : units) {
            UpdateElement el = u.getInstalled ();
            if (u.isPending() && el != null) {
                List<UpdateElement> updates = u.getAvailableUpdates ();
                if (updates.isEmpty()) {
                    continue;
                }
                if (firstClass.contains (el.getCodeName ())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static List<UnitCategory> makeAvailableCategories (final List<UpdateUnit> units, boolean isNbms) {
        List<UnitCategory> res = new ArrayList<UnitCategory> ();
        List<String> names = new ArrayList<String> ();
        for (UpdateUnit u : units) {
            UpdateElement el = u.getInstalled ();
            if (! u.isPending() && el == null) {
                List<UpdateElement> updates = u.getAvailableUpdates ();
                if (updates == null || updates.isEmpty()) {
                    continue;
                }
                UpdateElement upEl = updates.get (0);
                String catName = upEl.getCategory();
                if (names.contains (catName)) {
                    UnitCategory cat = res.get (names.indexOf (catName));
                    cat.addUnit (new Unit.Available (u, isNbms, catName));
                } else {
                    UnitCategory cat = new UnitCategory (catName);
                    cat.addUnit (new Unit.Available (u, isNbms, catName));
                    res.add (cat);
                    names.add (catName);
                }
            }
        }
        logger.log(Level.FINER, "makeAvailableCategories (" + units.size () + ") returns " + res.size ());

        return res;
    };

    public static void showURL (URL href) {
        HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
        assert displayer != null : "HtmlBrowser.URLDisplayer found.";
        if (displayer != null) {
            displayer.showURL (href);
        } else {
            logger.log (Level.INFO, "No URLDisplayer found.");
        }
    }
    
    public static String getDownloadSizeAsString (int size) {
        int gbSize = size / (1024 * 1024 * 1024);
        if (gbSize > 0) {
            return gbSize + getBundle ("Utilities_DownloadSize_GB");
        }
        int mbSize = size / (1024 * 1024);
        if (mbSize > 0) {
            return mbSize + getBundle ("Utilities_DownloadSize_MB");
        }
        int kbSize = size / 1024;
        if (kbSize > 0) {
            return kbSize + getBundle ("Utilities_DownloadSize_kB");
        }
        return size + getBundle ("Utilities_DownloadSize_B");
    }
    
    private static String getBundle (String key, Object... params) {
        return NbBundle.getMessage (Utilities.class, key, params);
    }
    
    public static void presentRefreshProvider (UpdateUnitProvider provider, PluginManagerUI manager, boolean force) {
        assert ! SwingUtilities.isEventDispatchThread () : "Don't presentRefreshProvider() call in EQ!";
        doRefreshProviders (Collections.singleton (provider), manager, force);
    }
    
    // Call PluginManagerUI.updateUnitsChanged() after refresh to reflect change in model
    public static void presentRefreshProviders (Collection<UpdateUnitProvider> providers, PluginManagerUI manager, boolean force) {
        assert ! SwingUtilities.isEventDispatchThread () : "Don't presentRefreshProvider() call in EQ!";
        doRefreshProviders (providers, manager, force);
    }
    
    // Call PluginManagerUI.updateUnitsChanged() after refresh to reflect change in model
    public static void presentRefreshProviders (PluginManagerUI manager, boolean force) {
        assert ! SwingUtilities.isEventDispatchThread () : "Don't presentRefreshProviders() call in EQ!";
        doRefreshProviders (null, manager, force);
    }
    
    private static void doRefreshProviders (Collection<UpdateUnitProvider> providers, PluginManagerUI manager, boolean force) {
        boolean finish = false;
        while (! finish) {
            finish = tryRefreshProviders (providers, manager, force);
        }
    }

    public static void showProviderNotification(UpdateUnitProvider p) {
        ShowNotifications.checkNotification(p);
    }
    
    private static boolean tryRefreshProviders (Collection<UpdateUnitProvider> providers, PluginManagerUI manager, boolean force) {
        ProgressHandle handle = ProgressHandleFactory.createHandle (NbBundle.getMessage(SettingsTableModel.class,  ("Utilities_CheckingForUpdates")));
        JComponent progressComp = ProgressHandleFactory.createProgressComponent (handle);
        JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
        detailLabel.setHorizontalAlignment (SwingConstants.LEFT);
        try {
            manager.setProgressComponent (detailLabel, progressComp);
            handle.setInitialDelay (0);
            handle.start ();
            if (providers == null) {
                providers = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (true);
            }
            for (UpdateUnitProvider p : providers) {
                try {
                    p.refresh (handle, force);
                    showProviderNotification(p);
                } catch (IOException ioe) {
                    logger.log (Level.INFO, ioe.getMessage (), ioe);
                    JButton cancel = new JButton ();
                    Mnemonics.setLocalizedText (cancel, getBundle ("Utilities_NetworkProblem_Cancel")); // NOI18N
                    JButton skip = new JButton ();
                    Mnemonics.setLocalizedText (skip, getBundle ("Utilities_NetworkProblem_Skip")); // NOI18N
                    skip.setEnabled (providers.size() > 1);
                    JButton tryAgain = new JButton ();
                    Mnemonics.setLocalizedText (tryAgain, getBundle ("Utilities_NetworkProblem_Continue")); // NOI18N
                    ProblemPanel problem = new ProblemPanel (
                            getBundle ("Utilities_NetworkProblem_Text", p.getDisplayName (), ioe.getLocalizedMessage ()), // NOI18N
                            new JButton [] { tryAgain, skip, cancel });
                    Object ret = problem.showNetworkProblemDialog ();
                    if (skip.equals (ret)) {
                        // skip UpdateUnitProvider and try next one
                        continue;
                    } else if (tryAgain.equals (ret)) {
                        // try again
                        return false;
                    }
                    return true;
                }
            }
        } finally {
            if (handle != null) {
                handle.finish ();
            }
            // XXX: Avoid NPE when called refresh providers on selected units
            // #101836: OperationContainer.contains() sometimes fails
            Containers.initNotify ();
            manager.unsetProgressComponent (detailLabel, progressComp);
        }
        return true;
    }

    public static void startAsWorkerThread(final PluginManagerUI manager, final Runnable runnableCode, final String progressDisplayName) {
        startAsWorkerThread (manager, runnableCode, progressDisplayName, 0);
    }
    
    public static void startAsWorkerThread (final PluginManagerUI manager,
            final Runnable runnableCode,
            final String progressDisplayName,
            final long estimatedTime) {
        startAsWorkerThread(new Runnable() {
            @Override
            public void run() {
                final ProgressHandle handle = ProgressHandleFactory.createHandle(progressDisplayName); // NOI18N                
                JComponent progressComp = ProgressHandleFactory.createProgressComponent(handle);
                JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent(handle);
                
                try {                    
                    detailLabel.setHorizontalAlignment(SwingConstants.LEFT);
                    manager.setProgressComponent(detailLabel, progressComp);
                    handle.setInitialDelay(0);
                    if (estimatedTime == 0) {
                        handle.start ();                    
                        handle.progress (progressDisplayName);
                        runnableCode.run ();
                    } else {
                        assert estimatedTime > 0 : "Estimated time " + estimatedTime;
                        final long friendlyEstimatedTime = estimatedTime + 2/*friendly constant*/;
                        handle.start ((int) friendlyEstimatedTime * 10, friendlyEstimatedTime); 
                        handle.progress (progressDisplayName, 0);
                        final RequestProcessor.Task runnableTask = Installer.RP.post (runnableCode);
                        RequestProcessor.Task post = Installer.RP.post (new Runnable () {
                            @Override
                            @SuppressWarnings("SleepWhileInLoop")
                             public void run () {
                                 int i = 0;
                                 while (! runnableTask.isFinished ()) {
                                     try {
                                         if (friendlyEstimatedTime * 10 > i++) {
                                             handle.progress (progressDisplayName, i);
                                         } else {
                                             handle.switchToIndeterminate ();
                                             handle.progress (progressDisplayName);
                                             return ;
                                         }
                                         Thread.sleep (100);
                                     } catch (InterruptedException ex) {
                                         // no worries
                                     }
                                 }
                             }
                         });
                        runnableTask.addTaskListener (new TaskListener () {
                            @Override
                            public void taskFinished (Task task) {
                                task.removeTaskListener (this);
                                handle.finish ();
                            }
                        });
                        runnableTask.waitFinished ();
                    }
                } finally {
                    if (handle != null) {
                        handle.finish();
                    }                    
                    manager.unsetProgressComponent (detailLabel, progressComp);
                }
            }
        });
    }
    
    public static RequestProcessor.Task startAsWorkerThread(final Runnable runnableCode) {
        return startAsWorkerThread(runnableCode, 0);    
    }   
    
    public static RequestProcessor.Task startAsWorkerThread(final Runnable runnableCode, final int delay) {
        RequestProcessor.Task retval = WORKER_THREADS_PROCESSOR.create(runnableCode);
        if (SwingUtilities.isEventDispatchThread ()) {
            retval.schedule(delay);
        } else {
            if (delay > 0) {
                try {
                    java.lang.Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            retval.run();
        }
        return retval;
    }    

    public static UpdateManager.TYPE [] getUnitTypes () {
        if (modulesOnly ()) {
            return new UpdateManager.TYPE [] { UpdateManager.TYPE.MODULE };
        } else {
            return new UpdateManager.TYPE [] { UpdateManager.TYPE.KIT_MODULE, UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT };
        }
    }
        
    public static Boolean isGlobalInstallation() {
        String s = getPreferences().get(PLUGIN_MANAGER_SHARED_INSTALLATION, System.getProperty("plugin.manager.install.global")); // NOI18N
        
        if (Boolean.parseBoolean(s)) {
            return Boolean.TRUE;
        } else if (Boolean.FALSE.toString().equalsIgnoreCase(s)) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }

    public static void setGlobalInstallation(boolean isGlobal) {
        getPreferences ().putBoolean (PLUGIN_MANAGER_SHARED_INSTALLATION, isGlobal);
    }
    
    public static boolean modulesOnly () {
        return isModulesOnly == null ? modulesOnlyDefault () : isModulesOnly;
    }
    
    public static boolean showExtendedDescription () {
        return Boolean.valueOf (System.getProperty ("plugin.manager.extended.description"));
    }
    
    public static String getCustomCheckIntervalInMinutes () {
        return System.getProperty (PLUGIN_MANAGER_CHECK_INTERVAL);
    }
    
    private static String getCustomFirstClassModules () {
        return System.getProperty (PLUGIN_MANAGER_FIRST_CLASS_MODULES);
    }
    
    public static Collection<String> getFirstClassModules () {
        if (first_class_modules != null) {
            return first_class_modules;
        }
        String names = getCustomFirstClassModules ();
        if (names == null || names.length () == 0) {
            names = FIRST_CLASS_MODULES;
        }
        first_class_modules = new HashSet<String> ();
        StringTokenizer en = new StringTokenizer (names, ","); // NOI18N
        while (en.hasMoreTokens ()) {
            first_class_modules.add (en.nextToken ().trim ());
        }
        return first_class_modules;
    }
    
    /** Allow show Windows-like balloon in the status line.
     * 
     * @return <code>true</code> if showing is allowed, <code>false</code> if don't, or <code>null</code> was not specified in <code>plugin.manager.allow.showing.balloon</code>
     */
    public static Boolean allowShowingBalloon () {
        String allowShowing = System.getProperty (ALLOW_SHOWING_BALLOON);
        return allowShowing == null ? null : Boolean.valueOf (allowShowing);
    }

    /** Gets defalut timeout for showing Windows-like balloon in the status line.
     * The timeout can be specified in <code>plugin.manager.showing.balloon.timeout</code>. The dafault value is 30*1000.
     * The value 0 means unlimited timeout.
     * 
     * @return the amout of time to show the ballon in miliseconds.
     */
    public static int getShowingBalloonTimeout () {
        String timeoutS = System.getProperty (SHOWING_BALLOON_TIMEOUT);
        int timeout = 30 * 1000;
        try {
            if (timeoutS != null) {
                timeout = Integer.parseInt (timeoutS);
            }
        } catch (NumberFormatException nfe) {
            logger.log (Level.INFO, nfe + " while parsing " + timeoutS + " for " + SHOWING_BALLOON_TIMEOUT);
        }
        return timeout;
    }

    /** Do auto-check for available new plugins a while after startup.
     * 
     * @return false as default
     */
    public static boolean shouldCheckAvailableNewPlugins () {
        String shouldCheck = System.getProperty ("plugin.manager.check.new.plugins");
        return shouldCheck == null ? false : Boolean.valueOf (shouldCheck);
    }

    /** Do auto-check for available updates a while after startup.
     * 
     * @return true as default
     */
    public static boolean shouldCheckAvailableUpdates() {
        String shouldCheck = System.getProperty ("plugin.manager.check.updates");
        return shouldCheck == null ? true : Boolean.valueOf (shouldCheck);
    }

    public static void setModulesOnly (boolean modulesOnly) {
        isModulesOnly = modulesOnly ? Boolean.TRUE : Boolean.FALSE;
        getPreferences ().putBoolean (PLUGIN_MANAGER_MODULES_ONLY, isModulesOnly);
    }
    
    private static boolean modulesOnlyDefault () {
        return getPreferences ().getBoolean (PLUGIN_MANAGER_MODULES_ONLY, Boolean.valueOf (System.getProperty ("plugin.manager.modules.only")));
    }

    public static Comparator<String> getCategoryComparator () {
        return new Comparator<String> () {
            @Override
            public int compare (String o1, String o2) {
                /*
                // Libraries always put in the last place.
                if (LIBRARIES_CATEGORY.equals (o1)) {
                    if (LIBRARIES_CATEGORY.equals (o2)) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    if (LIBRARIES_CATEGORY.equals (o2)) {
                        return -1;
                    }
                    // Eager modules come between categories and libraries.
                    if (BRIDGES_CATEGORY.equals (o1)) {
                        if (BRIDGES_CATEGORY.equals (o2)) {
                            return 0;
                        } else {
                            return 1;
                        }
                    } else {
                        if (BRIDGES_CATEGORY.equals (o2)) {
                            return -1;
                        }
                        // Eager modules come between categories and libraries.
                        if (UNSORTED_CATEGORY.equals (o1)) {
                            if (UNSORTED_CATEGORY.equals (o2)) {
                                return 0;
                            } else {
                                return 1;
                            }
                        } else {
                            if (UNSORTED_CATEGORY.equals (o2)) {
                                return -1;
                            }
                        }

                        return Collator.getInstance ().compare (o1, o2);
                    }
                }
                 *
                 */
                return Collator.getInstance ().compare (o1, o2);
            }
        };
    }
    
    public static List<File> sharedDirs () {
        List<File> files = new ArrayList<File> ();
        
        String dirs = System.getProperty ("netbeans.dirs"); // NOI18N
        if (dirs != null) {
            Enumeration en = new StringTokenizer (dirs, File.pathSeparator);
            while (en.hasMoreElements ()) {
                File f = new File ((String) en.nextElement ());
                files.add (f);
            }
        }
        
        
        File id = getPlatformDir ();
        if (id != null) {
            files.add(id);
        }
        
        return Collections.unmodifiableList (files);
    }
    
    public static boolean canWriteInCluster (File cluster) {
        assert cluster != null : "dir cannot be null";
        assert cluster.exists () : cluster + " must exists";
        assert cluster.isDirectory () : cluster + " is directory";
        if (cluster == null || ! cluster.exists () || ! cluster.isDirectory ()) {
            logger.log (Level.INFO, "Invalid cluster " + cluster);
            return false;
        }
        // workaround the bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4420020
        if (cluster.canWrite () && cluster.canRead () && org.openide.util.Utilities.isWindows ()) {
            File trackings = new File (cluster, "update_tracking"); // NOI18N
            if (trackings.exists () && trackings.isDirectory ()) {
                for (File f : trackings.listFiles ()) {
                    if (f.exists () && f.isFile ()) {
                        FileWriter fw = null;
                        try {
                            fw = new FileWriter (f, true);
                        } catch (IOException ioe) {
                            // just check of write permission
                            logger.log (Level.FINE, f + " has no write permission", ioe);
                            return false;
                        } finally {
                            try {
                                if (fw != null) {
                                    fw.close ();
                                }
                            } catch (IOException ex) {
                                logger.log (Level.INFO, ex.getLocalizedMessage (), ex);
                            }
                        }
                        logger.log (Level.FINE, f + " has write permission");
                        return true;
                    }
                }
            }
        }
        logger.log (Level.FINE, "Can write into " + cluster + "? " + cluster.canWrite ());
        return cluster.canWrite ();
    }
    
    private static File getPlatformDir () {
        String platform = System.getProperty ("netbeans.home"); // NOI18N
        return platform == null ? null : new File (platform);
    }
    
    private static Preferences getPreferences () {
        return NbPreferences.forModule (Utilities.class);
    }
    
}
