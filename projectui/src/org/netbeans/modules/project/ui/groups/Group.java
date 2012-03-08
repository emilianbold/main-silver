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

package org.netbeans.modules.project.ui.groups;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.project.indexingbridge.IndexingBridge;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.OpenProjectListSettings;
import org.netbeans.modules.project.ui.ProjectTab;
import org.netbeans.modules.project.ui.ProjectUtilities;
import static org.netbeans.modules.project.ui.groups.Bundle.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 * Represents a project group.
 * Static methods represent set of groups and group selection.
 * @author Jesse Glick
 */
public abstract class Group {

    private static final Logger LOG = Logger.getLogger(Group.class.getName());
    private static final Logger UILOG = Logger.getLogger("org.netbeans.ui.project.groups");

    protected static final Preferences NODE = NbPreferences.forModule(Group.class).node("groups");
    /** Preferences key for the active group ID. */
    private static final String KEY_ACTIVE = "active"; // NOI18N
    /** Preferences key for display name of group. */
    protected static final String KEY_NAME = "name"; // NOI18N
    /** Preferences key for kind of group (see constants in subclasses). */
    protected static final String KEY_KIND = "kind"; // NOI18N
    /** Preferences key for path (space-separated) of project URLs for AdHocGroup, or single project dir URL for SubprojectsGroup, or dir URL for DirectoryGroup. */
    protected static final String KEY_PATH = "path"; // NOI18N
    /** Preferences key for main project path URL for AdHocGroup or DirectoryGroup. */
    protected static final String KEY_MAIN = "main"; // NOI18N

    private static Group load(String id) {
        if (id == null) {
            return null;
        }
        String kind = NODE.node(id).get(KEY_KIND, null);
        if (AdHocGroup.KIND.equals(kind)) {
            return new AdHocGroup(id);
        } else if (SubprojectsGroup.KIND.equals(kind)) {
            return new SubprojectsGroup(id);
        } else if (DirectoryGroup.KIND.equals(kind)) {
            return new DirectoryGroup(id);
        } else {
            LOG.log(Level.WARNING, "Cannot find project group kind for id={0}", id);
            return null;
        }
    }

    /**
     * Find all groups.
     * Sorted by display name.
     */
    public static SortedSet<Group> allGroups() {
        SortedSet<Group> groups = new TreeSet<Group>(displayNameComparator());
        try {
            for (String groupId : NODE.childrenNames()) {
                LOG.log(Level.FINER, "Considering project group id={0}", groupId);
                Group g = load(groupId);
                if (g != null) {
                    groups.add(g);
                }
            }
        } catch (BackingStoreException x) {
            Exceptions.printStackTrace(x);
        }
        return groups;
    }

    /**
     * Find the currently active group (or null).
     */
    public static Group getActiveGroup() {
        return load(NODE.get(KEY_ACTIVE, null));
    }

    /**
     * Set the currently active group (or null).
     */
    @Messages({
        "# {0} - internal group info", "Group.UI.setActiveGroup=Selecting project group: {0}",
        "#NOI18N", "Group.UI.setActiveGroup_ICON_BASE=org/netbeans/modules/project/ui/resources/openProject.png"
    })
    public static void setActiveGroup(Group nue) {
        LOG.log(Level.FINE, "set active group: {0}", nue);
        Group old = getActiveGroup();
        if (nue != null) {
            NODE.put(KEY_ACTIVE, nue.id);
        } else {
            if (old == null) {
                //#141403
                return;
            }
            NODE.remove(KEY_ACTIVE);
        }
        if (projectsLoaded) {
        // OK if g == old; still want to fix open projects.
            switchingGroup.set(true);
            try {
        open(nue);
            } finally {
                switchingGroup.set(false);
            }
        } else {
            OpenProjectListSettings settings = OpenProjectListSettings.getInstance();
            settings.setOpenProjectsURLsAsStrings(nue != null ? nue.projectPaths() : Collections.<String>emptyList());
            settings.setMainProjectURL(nue != null ? nue.prefs().get(KEY_MAIN, null) : null);
            // XXX with #168578 could adjust open files too
        }
        if (UILOG.isLoggable(Level.FINER)) {
            LogRecord rec = new LogRecord(Level.FINER, "Group.UI.setActiveGroup");
            rec.setParameters(new Object[] {nue != null ? nue.toString(true) : null});
            rec.setResourceBundle(NbBundle.getBundle(Group.class));
            rec.setLoggerName(UILOG.getName());
            UILOG.log(rec);
        }
    }
    private static boolean projectsLoaded;
    /**
     * Until this is called, property changes are just from startup.
     * @see org.netbeans.modules.project.ui.OpenProjectList.LoadOpenProjects#updateGlobalState
     */
    public static void projectsLoaded() {
        projectsLoaded = true;
    }
    private static final ThreadLocal<Boolean> switchingGroup = new ThreadLocal<Boolean>() {
        @Override protected Boolean initialValue() {
            return false;
        }
    };
    static {
        LOG.fine("initializing open projects listener");
        OpenProjects.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                if (!projectsLoaded || switchingGroup.get()) {
                    return;
                }
                String propertyName = evt.getPropertyName();
                if (propertyName != null) {
                    Group g = getActiveGroup();
                    if (g != null) {
                        LOG.log(Level.FINE, "received {0} on {1}", new Object[] {propertyName, g.id});
                        g.openProjectsEvent(propertyName);
                    }
                }
            }
        });
    }

    protected static String sanitizeNameAndUniquifyForId(String name) {
        String sanitizedId = name.replaceAll("[^a-zA-Z0-9_.-]+", "_");
        Set<String> existing;
        try {
            existing = new HashSet<String>(Arrays.asList(NODE.childrenNames()));
        } catch (BackingStoreException x) {
            Exceptions.printStackTrace(x);
            return sanitizedId;
        }
        if (existing.contains(sanitizedId)) {
            for (int i = 2; ; i++) {
                String candidate = sanitizedId + "_" + i;
                if (!existing.contains(candidate)) {
                    return candidate;
                }
            }
        } else {
            return sanitizedId;
        }
    }

    protected final String id;

    protected Group(String id) {
        this.id = id;
        assert id.indexOf('/') == -1;
    }

    protected Preferences prefs() {
        return NODE.node(id);
    }

    /**
     * The name of a group; may be used for display purposes.
     */
    public String getName() {
        String n = getNameOrNull();
        if (n == null) {
            n = id;
        }
        return n;
    }

    protected String getNameOrNull() {
        return prefs().get(KEY_NAME, null);
    }

    /**
     * Change the current display name.
     */
    public void setName(String n) {
        prefs().put(KEY_NAME, n);
        if (this.equals(getActiveGroup())) {
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    ProjectTab.findDefault(ProjectTab.ID_LOGICAL).setGroup(Group.this);
                }
            });
        }
    }

    protected static Project projectForPath(String path) {
        if (path != null) {
            try {
                FileObject fo = URLMapper.findFileObject(new URL(path));
                if (fo != null && fo.isFolder()) {
                    return ProjectManager.getDefault().findProject(fo);
                }
            } catch (IOException x) {
                Exceptions.printStackTrace(x);
            }
        }
        return null;
    }

    /**
     * The projects (currently) contained in the group.
     */
    public Set<Project> getProjects() {
        return getProjects(null, 0, 0);
    }
    private Set<Project> getProjects(ProgressHandle h, int start, int end) {
        if (h != null) {
            h.progress("", start);
        }
        Set<Project> projects = new HashSet<Project>();
        findProjects(projects, h, start, end);
        if (h != null) {
            h.progress("", end);
        }
        assert !projects.contains(null) : "Found null in " + projects + " from " + this;
        return projects;
    }

    protected abstract void findProjects(Set<Project> projects, ProgressHandle h, int start, int end);

    /**
     * Gets a list of paths (URLs) to projects in the group.
     * @return a list based on {@link #getProjects()} in the default implementation
     */
    protected List<String> projectPaths() {
        List<String> urls = new ArrayList<String>();
        for (Project p : getProjects()) {
            urls.add(p.getProjectDirectory().toURL().toString());
        }
        return urls;
    }

    @Messages({"# {0} - project display name", "Group.progress_project=Loading project \"{0}\""})
    protected static String progressMessage(Project p) {
        return Group_progress_project(ProjectUtils.getInformation(p).getDisplayName());
    }

    /**
     * The main project for this group (if any).
     */
    public Project getMainProject() {
        return projectForPath(prefs().get(KEY_MAIN, null));
    }

    /**
     * Change the main project in the group.
     * @throws IllegalArgumentException unless the main project is among {@link #getProjects}
     */
    public void setMainProject(Project mainProject) throws IllegalArgumentException {
        LOG.log(Level.FINE, "updating main project for {0} to {1}", new Object[] {id, mainProject});
        URL f = null;
        if (mainProject != null && getProjects().contains(mainProject)) {
            f = mainProject.getProjectDirectory().toURL();
        }
        if (f != null) {
            prefs().put(KEY_MAIN, f.toExternalForm());
        } else {
            if (mainProject != null) {
                LOG.log(Level.WARNING, "...but not an open project or disk path not found");
            }
            prefs().remove(KEY_MAIN);
        }
    }

    /**
     * Open a group, replacing any open projects with this group's project set.
     */
    @Messages({
        "# {0} - group display name", "Group.open_handle=Opening group \"{0}\"",
        "Group.close_handle=Closing group",
        "# {0} - count", "Group.progress_closing=Closing {0} old projects",
        "# {0} - count", "Group.progress_opening=Opening {0} new projects"
    })
    private static void open(final Group g) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                ProjectTab.findDefault(ProjectTab.ID_LOGICAL).setGroup(g);
            }
        });
        String handleLabel;
        if (g != null) {
            handleLabel = Group_open_handle(g.getName());
        } else {
            handleLabel = Group_close_handle();
        }
        final ProgressHandle h = ProgressHandleFactory.createHandle(handleLabel);
        try {
        h.start(200);
        ProjectUtilities.WaitCursor.show();
        final OpenProjectList opl = OpenProjectList.getDefault();
        Set<Project> oldOpen = new HashSet<Project>(Arrays.asList(opl.getOpenProjects()));
        Set<Project> newOpen = g != null ? g.getProjects(h, 10, 100) : Collections.<Project>emptySet();
        final Set<Project> toClose = new HashSet<Project>(oldOpen);
        toClose.removeAll(newOpen);
        final Set<Project> toOpen = new HashSet<Project>(newOpen);
        toOpen.removeAll(oldOpen);
        assert !toClose.contains(null) : toClose;
        assert !toOpen.contains(null) : toOpen;
        IndexingBridge.getDefault().runProtected(new Runnable() {
            @Override public void run() {
        h.progress(Group_progress_closing(toClose.size()), 110);
        opl.close(toClose.toArray(new Project[toClose.size()]), false);
        h.switchToIndeterminate();
        h.progress(Group_progress_opening(toOpen.size()));
        opl.open(toOpen.toArray(new Project[toOpen.size()]), false, h, null);
        if (g != null) {
            opl.setMainProject(g.getMainProject());
        }
            }
        });
        } finally {
            ProjectUtilities.WaitCursor.hide();
            h.finish();
        }
        
    }

    protected void openProjectsEvent(String propertyName) {
        if (propertyName.equals(OpenProjects.PROPERTY_MAIN_PROJECT)) {
        setMainProject(OpenProjects.getDefault().getMainProject());
    }
    }

    /**
     * Delete this group.
     */
    public void destroy() {
        LOG.log(Level.FINE, "destroying: {0}", id);
        if (equals(getActiveGroup())) {
            setActiveGroup(null);
        }
        try {
            Preferences p = prefs();
            p.removeNode();
            assert !p.nodeExists("") : "failed to destroy " + id;
        } catch (BackingStoreException x) {
            Exceptions.printStackTrace(x);
        }
    }

    public abstract GroupEditPanel createPropertiesPanel();

    /**
     * Compares groups according to display name.
     */
    public static Comparator<Group> displayNameComparator() {
        return new Comparator<Group>() {
            Collator COLLATOR = Collator.getInstance();
            @Override public int compare(Group g1, Group g2) {
                return COLLATOR.compare(g1.getName(), g2.getName());
            }
        };
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Group && id.equals(((Group) obj).id);
    }

    @Override
    public String toString() {
        return toString(false);
    }
    protected String toString(boolean scrubPersonalInfo) {
        return getClass().getName().replaceFirst("^.+\\.", "") + "[id=" + (scrubPersonalInfo ? "#" + id.hashCode() : id) + ",|projects|=" + getProjects().size() + "]";
    }

    /**
     * True if the projects specified by this group are exactly those open at the moment.
     * More precisely, true if closing and reopening this group would leave you with the same
     * set of projects (incl. main project) as you currently have.
     */
    public boolean isPristine() {
        return getProjects().equals(new HashSet<Project>(Arrays.asList(OpenProjects.getDefault().getOpenProjects())));
    }

}
