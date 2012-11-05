/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.options.keymap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.KeyStroke;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.core.options.keymap.api.KeyStrokeUtils;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.api.ShortcutsFinder;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Wrapper around the {@link KeymapModel}. This wrapper uses human-readable keystroke names,
 * and support local modifications. Once the modifications are {#link #apply applied}, they are
 * written to the shared storage. The underlying ShortcutsFinder (if any) is also
 * refreshed. The model is NOT thread-safe.
 * <p/>
 * The model should be cloned from the global ShortcutsFinder, then the caller may choose
 * to apply() the changes, or simply discard the entire data structure.
 * 
 * @author Svata Dedic
 */
class MutableShortcutsModel extends ShortcutsFinderImpl implements ShortcutsFinder.Writer {
    /**
     * Current profile
     */
    private String              currentProfile;

    /**
     * The underlying model
     */
    @NonNull
    private KeymapModel model;

    /**
     * Key: category name. Value = pair of List&lt;ShortcutAction>. The 1st List
     * holds all actions for the category AND subcategories, the 2nd List holds
     * list of actions in the category only. Initialized lazily by {@link #getItems}
     */
    private Map<String, List<Object>[]> categoryToActionsCache = 
            new HashMap<String, List<Object>[]> ();
    
    /**
     * Profiles, which has been modified. All keybindings are searched in this Map
     * first.
     */
    private Map<String, Map<ShortcutAction, Set<String>>> modifiedProfiles = 
            new HashMap<String, Map<ShortcutAction, Set<String>>> ();
    
    /**
     * Set of profiles to be deleted
     */
    private Set<String> deletedProfiles = new HashSet<String> ();
    
    /**
     * Global ShortcutsFinder to reset when the keymap is changed.
     */
    @NullAllowed
    private ShortcutsFinder master;
    
    public MutableShortcutsModel(@NonNull KeymapModel model, ShortcutsFinder master) {
        super(model);
        this.model = model;
        this.master = master == null ? Lookup.getDefault().lookup(ShortcutsFinder.class) : master;
    }
    
    
    List<String> getProfiles () {
        Set<String> result = new HashSet<String> (model.getProfiles ());
        result.addAll (modifiedProfiles.keySet ());
        List<String> r = new ArrayList<String> (result);
        Collections.sort (r);
        return r;
    }
    
    boolean isCustomProfile (String profile) {
        return modifiedProfiles.containsKey(profile) || model.isCustomProfile (profile);
    }
    
    boolean deleteOrRestoreProfile (String profile) {
        if (model.isCustomProfile (profile)) {
            deletedProfiles.add (profile);
            modifiedProfiles.remove (profile);
            return true;
        } else {
            Map<ShortcutAction, Set<String>> m = model.getKeymapDefaults (profile);
            m = convertFromEmacs (m);
            modifiedProfiles.put (profile, m);
            return false;
        }
    }
    
    protected String getCurrentProfile () {
        if (currentProfile == null) {
            return model.getCurrentProfile();
        } else {
            return currentProfile;
        }
    }
    
    void setCurrentProfile (String currentKeymap) {
        this.currentProfile = currentKeymap;
    }
    
    void cloneProfile (String newProfileName) {
        Map<ShortcutAction, Set<String>> result = new HashMap<ShortcutAction, Set<String>> ();
        cloneProfile ("", result);
        modifiedProfiles.put (newProfileName, result);
    }
    
    private void cloneProfile (
        String category,        // name of currently resolved category
        Map<ShortcutAction, Set<String>> result
    ) {
        Iterator it = getItems (category).iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            String[] shortcuts = getShortcuts ((ShortcutAction) o);
            result.put ((ShortcutAction)o, new HashSet<String> (Arrays.asList (shortcuts)));
        }
    }
    
    public ShortcutAction findActionForShortcut (String shortcut) {
        return findActionForShortcut (shortcut, "", false, null, "");
    }
    
    /**
     * Filters the actions and retains only those which come from the same KeymapManager
     * as the 'anchor' action. Actions from the same keymap manager are typically not allowed
     * to have the same key binding
     * 
     * @param actions actions to filter
     * @param anchor action that identifies the KeymapManager
     * @return filtered action list, as a new collection
     */
    Collection<ShortcutAction> filterSameScope(Set<ShortcutAction> actions, ShortcutAction anchor) {
        return KeymapModel.filterSameScope(actions, anchor);
    }

    /**
     * Finds action with conflicting shortcut (or a prefix, for a multi-keybinding)
     * for a shortcut
     * @param shortcut the shortcut to look for
     * @return action with same shortcut, or shortcutprefix. If the prefix is same
     * but the rest of multi-keybinding is different, returns <code>null</code> (no conflict).
     */
    Set<ShortcutAction> findActionForShortcutPrefix(String shortcut) {
        Set<ShortcutAction> set = new HashSet<ShortcutAction>();
        if (shortcut.length() == 0) {
            return set;
        }
        //has to work with multi-keybinding properly,
        //ie. not allow 'Ctrl+J' and 'Ctrl+J X' at the same time
        if (shortcut.contains(" ")) {
            findActionForShortcut(shortcut.substring(0, shortcut.lastIndexOf(' ')), "", true, set, shortcut);
        } else {
            findActionForShortcut(shortcut, "", true, set, shortcut);
        }
        return set;
    }

    private ShortcutAction findActionForShortcut (String shortcut, String category, boolean prefixSearch, Set<ShortcutAction> set, String completeMultikeySC) {
        //search in modified profiles first
        Map<ShortcutAction, Set<String>> map = modifiedProfiles.get(currentProfile);
        if (map != null) {
            for (Map.Entry<ShortcutAction, Set<String>> entry : map.entrySet()) {
                for (String sc : entry.getValue()) {
                    if (prefixSearch) {
                        if (sc.equals(shortcut) || (sc.startsWith(completeMultikeySC) && shortcut.equals(completeMultikeySC) && sc.contains(" "))) {
                            set.add(entry.getKey());
                        }
                    } else if (sc.equals(shortcut)) {
                        return entry.getKey();
                    }
                }
            }
        }

        Iterator it = getItems (category).iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            ShortcutAction action = (ShortcutAction) o;
            String[] shortcuts = getShortcuts (action);
            int i, k = shortcuts.length;
            for (i = 0; i < k; i++) {
                if (prefixSearch) {
                    if (shortcuts[i].equals(shortcut) || (shortcuts[i].startsWith(completeMultikeySC) && shortcut.equals(completeMultikeySC) && shortcuts[i].contains(" "))) {
                        set.add(action);
                    }
                } else if (shortcuts[i].equals(shortcut)) {
                    return action;
                }
            }

        }
        return null;
    }

    protected ShortcutAction findActionForId (String actionId, String category, boolean delegate) {
        // check whether the ID is not a duplicate one -> no action found:
        Iterator it = getItems (category).iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            String id;
            
            if (delegate) {
                // fallback for issue #197068 - try to find actions also by their classname:
                id = LayersBridge.getOrigActionClass((ShortcutAction)o);
            } else {
                id = ((ShortcutAction) o).getId ();
            }
            if (id != null && actionId.equals (id)) { 
                return (ShortcutAction) o;
            }
        }
        return null;
    }
    
    public String[] getShortcuts (ShortcutAction action) {
        if (modifiedProfiles.containsKey (currentProfile)) {
            // find it in modified shortcuts
            Map<ShortcutAction, Set<String>> actionToShortcuts = modifiedProfiles.
                get (currentProfile);
            if (actionToShortcuts.containsKey (action)) {
                Set<String> s = actionToShortcuts.get (action);
                return s.toArray (new String [s.size ()]);
            }
        }
        return super.getShortcuts(action);
    }

    /**
     * Set of all shortcuts used by current profile (including modifications)
     * In case there is a multikey keybinding used, its prefix is included
     * @return set of shortcuts
     */
    public Set<String> getAllCurrentlyUsedShortcuts() {
        Set<String> set = new LinkedHashSet<String>();
        //add modified shortcuts, if any
        Map<ShortcutAction, Set<String>> modMap = modifiedProfiles.get(currentProfile);
        if (modMap != null) {
            for (Map.Entry<ShortcutAction, Set<String>> entry : modMap.entrySet()) {
                for (String sc : entry.getValue()) {
                    set.add(sc);
                    if (sc.contains(" ")) { // NOI18N
                        set.add(sc.substring(0, sc.indexOf(' ')));
                    }
                }
            }
        }
        //add default shortcuts
        for (Map.Entry<ShortcutAction, Set<String>> entry : getProfileMap(currentProfile).entrySet()) {
            for (String sc : entry.getValue()) {
                    set.add(sc);
                    if (sc.contains(" ")) {
                        set.add(sc.substring(0, sc.indexOf(' ')));
                    }
                }
        }

        return set;
    }

    void addShortcut (ShortcutAction action, String shortcut) {
        // delete old shortcut
        ShortcutAction act = findActionForShortcut (shortcut);
        Set<String> s = new LinkedHashSet<String> ();
        s.addAll (Arrays.asList (getShortcuts (action)));
        s.add (shortcut);
        setShortcuts (action, s);
    }

    /**
     * Reverts shortcuts. If there is a conflict between the restored shortucts and other
     * actions, the method will do nothing unless 'force' is true, and returns collection of conflicting actions.
     * Return value of null indicates successful change.
     * 
     * @param action action to revert
     * @param force if true, does not check conflicts; used after user confirmation
     * @return {@code null} for success, or collection of conflicting actions 
     */
    Collection<ShortcutAction> revertShortcutsToDefault(ShortcutAction action, boolean force) {
        Map<ShortcutAction, Set<String>> m = model.getKeymapDefaults (currentProfile);
        m = convertFromEmacs(m);
        Set<String> shortcuts = m.get(action);
        if (shortcuts == null) {
            shortcuts = Collections.<String>emptySet(); //this action has no default shortcut
        }
        //lets search for conflicting SCs
        Set<ShortcutAction> conflictingActions = new HashSet<ShortcutAction>();
        for(String sc : shortcuts) {
            ShortcutAction ac = findActionForShortcut(sc);
            if (ac != null && !ac.equals(action)) {
                conflictingActions.add(ac);
            }
        }
        if (!conflictingActions.isEmpty() && !force) {
            return conflictingActions;
        }
        setShortcuts(action, shortcuts);
        return null;
    }

    public void setShortcuts (ShortcutAction action, Set<String> shortcuts) {
        Map<ShortcutAction, Set<String>> actionToShortcuts = modifiedProfiles.get (currentProfile);
        if (actionToShortcuts == null) {
            actionToShortcuts = new HashMap<ShortcutAction, Set<String>> ();
            modifiedProfiles.put (currentProfile, actionToShortcuts);
        }
        actionToShortcuts.put (action, shortcuts);
    }

    public void removeShortcut (ShortcutAction action, String shortcut) {
        Set<String> s = new LinkedHashSet<String> (Arrays.asList (getShortcuts (action)));
        s.remove (shortcut);
        setShortcuts(action, s);
    }
    
    /**
     * Simple guard against scheduling multiple tasks in advance. Also guards
     * against reentrancy.
     */
    private volatile boolean applyInProgress = false;
    
    public void apply () {
        if (applyInProgress) {
            return;
        }
        applyInProgress = true;
        RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                for (String profile: modifiedProfiles.keySet()) {
                    Map<ShortcutAction, Set<String>> actionToShortcuts = modifiedProfiles.get (profile);
                    actionToShortcuts = convertToEmacs (actionToShortcuts);
                    model.changeKeymap (
                        profile, 
                        actionToShortcuts
                    );
                }
                for (String profile: deletedProfiles) {
                    model.deleteProfile (profile);
                }
                model.setCurrentProfile (currentProfile);
                modifiedProfiles = new HashMap<String, Map<ShortcutAction, Set<String>>> ();
                deletedProfiles = new HashSet<String> ();
                model = new KeymapModel ();
                applyInProgress = false;
                clearCache();
                
                if (master != null) {
                    master.refreshActions();
                }
            }
        });
    }
    
    public boolean isChanged () {
        return (!modifiedProfiles.isEmpty ()) || !deletedProfiles.isEmpty ();
    }
    
    public void cancel () {
        modifiedProfiles = new HashMap<String, Map<ShortcutAction, Set<String>>> ();
        deletedProfiles = new HashSet<String> ();
        currentProfile = null;
    }

    Map<String, Map<ShortcutAction, Set<String>>> getModifiedProfiles() {
        return modifiedProfiles;
    }

    Set<String> getDeletedProfiles() {
        return deletedProfiles;
    }

    void setModifiedProfiles(Map<String, Map<ShortcutAction, Set<String>>> mp) {
        this.modifiedProfiles = mp;
    }

    void setDeletedProfiles(Set<String> dp) {
        this.deletedProfiles = dp;
    }
    
    /**
     * Converts Map (ShortcutAction > Set (String (shortcut Alt+Shift+P))) to 
     * Map (ShortcutAction > Set (String (shortcut AS-P))).
     */
    private static Map<ShortcutAction, Set<String>> convertToEmacs (Map<ShortcutAction, Set<String>> shortcuts) {
        Map<ShortcutAction, Set<String>> result = new HashMap<ShortcutAction, Set<String>> ();
        for (Map.Entry<ShortcutAction, Set<String>> entry: shortcuts.entrySet()) {
            ShortcutAction action = entry.getKey();
            Set<String> newSet = new HashSet<String> ();
            for (String s: entry.getValue()) {
                if (s.length () == 0) continue;
                KeyStroke[] ks = getKeyStrokes (s, " ");
                if (ks == null) 
                    continue; // unparsable shortcuts ignorred
                StringBuffer sb = new StringBuffer (
                    Utilities.keyToString (ks [0])
                );
                int i, k = ks.length;
                for (i = 1; i < k; i++)
                    sb.append (' ').append (Utilities.keyToString (ks [i]));
                newSet.add (sb.toString ());
            }
            result.put (action, newSet);
        }
        return result;
    }
    
    /** 
     * Returns multi keystroke for given text representation of shortcuts
     * (like Alt+A B). Returns null if text is not parsable, and empty array
     * for empty string.
     */
    private static KeyStroke[] getKeyStrokes (String keyStrokes, String delim) {
        if (keyStrokes.length () == 0) return new KeyStroke [0];
        StringTokenizer st = new StringTokenizer (keyStrokes, delim);
        List<KeyStroke> result = new ArrayList<KeyStroke> ();
        while (st.hasMoreTokens ()) {
            String ks = st.nextToken ().trim ();
            KeyStroke keyStroke = KeyStrokeUtils.getKeyStroke (ks);
            if (keyStroke == null) return null; // text is not parsable 
            result.add (keyStroke);
        }
        return result.toArray (new KeyStroke [result.size ()]);
    }
    
    public Set<String> getCategories() {
        return model.getActionCategories();
    }

    /**
     * Returns actions in the category and subcategories
     * @param category
     * @return 
     */
    public List<Object/*Union2<String,ShortcutAction>*/> getItems (String category) {
        return getItems(category, true);
    }
    
    /**
     * Returns list of actions in the given category, and optionally in the sub-categories.
     * 
     * @param category
     * @param prefix
     * @return 
     */
    public List<Object/*Union2<String,ShortcutAction>*/> getItems (String category, boolean prefix) {
        List<ShortcutAction>[] result = (List<ShortcutAction>[])(List[])categoryToActionsCache.get (category);
        if (result == null) {
            List<ShortcutAction> allActions = new ArrayList<ShortcutAction>();
            List<ShortcutAction> thisActions = Collections.emptyList();
            
            Set<String> filtered = new HashSet<String>(model.getActionCategories());
            for (Iterator<String> it = filtered.iterator(); it.hasNext(); ) {
                String cat = it.next();
                if (!cat.startsWith(category)) {
                    it.remove();
                } else if (category.length() > 0 && cat.length() > category.length() && cat.charAt(category.length()) != '/') {
                    it.remove();
                }
            }
            for (String c : filtered) {
                Collection<ShortcutAction> act = model.getActions(c);
                allActions.addAll(act);
                if (c.length() == category.length()) {
                    thisActions = new ArrayList<ShortcutAction>(act);
                }
            }
            Collections.<ShortcutAction>sort (allActions, new KeymapViewModel.ActionsComparator ());
            if (!thisActions.isEmpty()) {
                Collections.<ShortcutAction>sort (thisActions, new KeymapViewModel.ActionsComparator ());
            }
            result = new List[] { allActions , thisActions };
            ((Map)categoryToActionsCache).put (category, result);
        }
        return (List)(prefix ? result[0] : result[1]);
    }
    
}
