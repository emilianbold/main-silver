/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.ui.models;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Entlicher
 */
public class BreakpointGroup {

    static enum Group { NO, CUSTOM, LANGUAGE, TYPE, PROJECT, FILE, NESTED }

    static final String PROP_FROM_OPEN_PROJECTS = "fromOpenProjects";       // NOI18N
    static final String PROP_FROM_CURRENT_SESSION_PROJECTS = "fromCurrentSessionProjects";  // NOI18N

    private BreakpointGroup parent;
    private String name;
    private Object id;
    private Group group;
    private List<Breakpoint> breakpoints = new ArrayList<Breakpoint>();
    private List<BreakpointGroup> groups = new ArrayList<BreakpointGroup>();

    BreakpointGroup(Object id, String name, Group group) {
        this.id = id;
        this.name = name;
        this.group = group;
    }

    String getName() {
        return name;
    }

    Group getGroup() {
        return group;
    }

    List<BreakpointGroup> getSubGroups() {
        if (groups == null || groups.size() == 0) {
            return Collections.emptyList();
        }
        return groups;
    }

    List<Breakpoint> getBreakpoints() {
        return breakpoints;
    }

    private void addBreakpoint(Breakpoint b) {
        breakpoints.add(b);
    }

    private void addGroup(BreakpointGroup bg) {
        groups.add(bg);
        if (bg.parent != null) {
            throw new IllegalStateException("Group "+bg+" already has parent "+bg.parent);
        }
        bg.parent = this;
    }

    Object[] getGroupsAndBreakpoints() {
        List groupsAndBreakpoints = new ArrayList(groups.size() + breakpoints.size());
        groupsAndBreakpoints.addAll(groups);
        groupsAndBreakpoints.addAll(breakpoints);
        return groupsAndBreakpoints.toArray();
    }
    
    /*List<Breakpoint> getBreakpoints() {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager().getBreakpoints();
            ArrayList l = new ArrayList();
            int i, k = bs.length;
            for (i = 0; i < k; i++) {
                String gn = bs[i].getGroupName();
                if (gn.equals("")) {
                    l.add (bs [i]);
                } else {
                    if (!l.contains(gn)) {
                        l.add(gn);
                    }
                }
            }
    }*/

    static Object[] createGroups(Properties props) {
        //props.addPropertyChangeListener(null);
        String[] groupNames = (String[]) props.getArray("Grouping", new String[] { Group.CUSTOM.name() });
        boolean openProjectsOnly = props.getBoolean(PROP_FROM_OPEN_PROJECTS, true);
        boolean sessionProjectsOnly = props.getBoolean(PROP_FROM_CURRENT_SESSION_PROJECTS, true);
        Breakpoint[] bs = DebuggerManager.getDebuggerManager().getBreakpoints();
        if (groupNames.length == 0 || groupNames[0].equals(Group.NO.name())) {
            return bs;
        }
        Group[] gs = new Group[groupNames.length];
        Map<Object, BreakpointGroup> gm = new HashMap<Object, BreakpointGroup>();
        //Map<Group, Set<BreakpointGroup>> groupSets = new HashMap<Group, Set<BreakpointGroup>>();
        for (int gi = 0; gi < groupNames.length; gi++) {
            //Group g = Group.valueOf(groupNames[gi]);
            gs[gi] = Group.valueOf(groupNames[gi]);
            //gm[gi] = new HashMap<Object, BreakpointGroup>();
            //groupSets.put(g, new HashSet<BreakpointGroup>());
        }
        //List groupsAndBreakpoints = new ArrayList();
        List<BreakpointGroup> groups = new ArrayList<BreakpointGroup>();
        List<Breakpoint> breakpoints = new ArrayList<Breakpoint>();
        List ids = new ArrayList();
        List<BreakpointGroup> parentGroups = new ArrayList<BreakpointGroup>();
        List<BreakpointGroup> rootGroups = new ArrayList<BreakpointGroup>();

        Set<Project> openProjects;
        if (openProjectsOnly) {
            openProjects = new HashSet<Project>(Arrays.asList(OpenProjects.getDefault().getOpenProjects()));
        } else {
            openProjects = null;
        }
        Set<Project> sessionProjects;
        if (sessionProjectsOnly) {
            // TODO: Perhaps, better, ask for the session breakpoints somehow directly
            sessionProjects = getCurrentSessionProjects();
        } else {
            sessionProjects = null;
        }
        
        for (int bi = 0; bi < bs.length; bi++) {
            Breakpoint b = bs[bi];
            //Breakpoint.GroupProperties bprops = b.getGroupProperties();
            GroupProperties bprops = GroupProperties.getFrom(b);
            if (bprops != null) {
                if (bprops.isHidden()) {
                    continue;
                }
                if (openProjects != null && !contains(openProjects, bprops.getProjects())) {
                    continue;
                }
                if (sessionProjects != null && !contains(sessionProjects, bprops.getProjects())) {
                    continue;
                }
            }
            parentGroups.clear();
            rootGroups.clear();
            ids.clear();
            for (int gi = 0; gi < gs.length; gi++) {
                Group g = gs[gi];
                //Set gSet = groupSets.get(g);
                String propertyName = null;
                Object id = null;
                String[] propertyNames = null;
                Object[] idz = null;
                switch (g) {
                    case CUSTOM:
                        propertyName = b.getGroupName();
                        if (propertyName != null && propertyName.length() == 0) {
                            propertyName = null;
                        }
                        id = propertyName;
                        break;
                    case FILE:
                        if (bprops != null) {
                            FileObject[] files = bprops.getFiles();
                            if (files != null && files.length > 0) {
                                if (files.length == 1) {
                                    propertyName = files[0].getPath();
                                    id = files[0];
                                } else {
                                    propertyNames = new String[files.length];
                                    idz = files;
                                    for (int i = 0; i < files.length; i++) {
                                        propertyNames[i] = files[i].getPath();
                                    }
                                }
                            }
                        }
                        break;
                    case LANGUAGE:
                        if (bprops != null) {
                            id = propertyName = bprops.getLanguage();
                        }
                        break;
                    case PROJECT:
                        if (bprops != null) {
                            Project[] prjs = bprops.getProjects();
                            if (prjs != null && prjs.length > 0) {
                                if (prjs.length == 1) {
                                    propertyName = ProjectUtils.getInformation(prjs[0]).getDisplayName();
                                    id = prjs[0];
                                } else {
                                    propertyNames = new String[prjs.length];
                                    idz = prjs;
                                    for (int i = 0; i < prjs.length; i++) {
                                        propertyNames[i] = ProjectUtils.getInformation(prjs[i]).getDisplayName();
                                    }
                                }
                            }
                        }
                        break;
                    case TYPE:
                        if (bprops != null) {
                            id = propertyName = bprops.getType();
                        }
                        break;
                }

                if (parentGroups.isEmpty()) {
                    if (id != null) {
                        BreakpointGroup bg = gm.get(id);
                        if (bg == null) {
                            bg = new BreakpointGroup(id, propertyName, g);
                            gm.put(id, bg);
                        }
                        parentGroups.add(bg);
                    } else if (idz != null) {
                        for (int i = 0; i < idz.length; i++) {
                            BreakpointGroup bg = gm.get(idz[i]);
                            if (bg == null) {
                                bg = new BreakpointGroup(idz[i], propertyNames[i], g);
                                gm.put(idz[i], bg);
                            }
                            parentGroups.add(bg);
                        }
                    }
                } else {    // Add them all under every parent group
                    if (id != null) {
                        for (int i = 0; i < parentGroups.size(); i++) {
                            BreakpointGroup pg = parentGroups.get(i);
                            Object key = new NestedGroupKey(pg, id);
                            BreakpointGroup bg = gm.get(key);
                            if (bg == null) {
                                bg = new BreakpointGroup(id, propertyName, g);
                                pg.addGroup(bg);
                                gm.put(key, bg);
                            }
                            parentGroups.set(i, bg);
                        }
                    } else if (idz != null) {
                        List<BreakpointGroup> newParentGroups = new ArrayList<BreakpointGroup>(parentGroups.size() * idz.length);
                        for (BreakpointGroup pg : parentGroups) {
                            for (int i = 0; i < idz.length; i++) {
                                Object key = new NestedGroupKey(pg, idz[i]);
                                BreakpointGroup bg = gm.get(key);
                                if (bg == null) {
                                    bg = new BreakpointGroup(idz[i], propertyNames[i], g);
                                    pg.addGroup(bg);
                                    gm.put(key, bg);
                                }
                                newParentGroups.add(bg);
                            }
                        }
                        parentGroups.clear();
                        parentGroups.addAll(newParentGroups);
                    }
                }

                /*
                BreakpointGroup bg = null;
                List<BreakpointGroup> bgs = null;
                if (id != null) {
                    ids.add(id);
                    Object key;
                    if (ids.size() > 1) {
                        key = new NestedGroupKey(ids.toArray());
                    } else {
                        key = id;
                    }
                    bg = gm.get(key);
                    if (bg == null) {
                        bg = new BreakpointGroup(id, propertyName, g);
                        gm.put(key, bg);
                    }
                } else if (idz != null) {
                    bgs = new ArrayList<BreakpointGroup>(idz.length);
                    
                }

                if (parentGroups.isEmpty()) {
                    if (bg != null) {
                        parentGroups.add(bg);
                    } else if (bgs != null) {
                        
                    }
                } else if (bg != null) {
                    parentGroup.addGroup(bg);
                    parentGroup = bg;
                }
                 */
                if (rootGroups.isEmpty()) {
                    rootGroups.addAll(parentGroups);
                }
            }
            if (parentGroups.isEmpty()) {
                breakpoints.add(b);
            } else {
                for (BreakpointGroup rg : rootGroups) {
                    if (!groups.contains(rg)) {
                        groups.add(rg);
                    }
                }
                for (BreakpointGroup pg : parentGroups) {
                    pg.addBreakpoint(b);
                }
            }
        }
        List groupsAndBreakpoints = new ArrayList(groups.size() + breakpoints.size());
        groupsAndBreakpoints.addAll(groups);
        groupsAndBreakpoints.addAll(breakpoints);
        return groupsAndBreakpoints.toArray();
    }

    private static boolean contains(Set<Project> openProjects, Project[] projects) {
        if (projects != null && projects.length > 0) {
            boolean contains = false;
            for (Project p : projects) {
                if (openProjects.contains(p)) {
                    contains = true;
                    break;
                }
            }
            return contains;
        } else {
            return true;
        }
    }

    private static Set<Project> getCurrentSessionProjects() {
        Session currentSession = DebuggerManager.getDebuggerManager().getCurrentSession();
        if (currentSession == null) {
            return null;
        }
        List<? extends Project> sessionProjects = currentSession.lookup(null, Project.class);
        if (sessionProjects.size() == 0) {
            return null;
        }
        return new HashSet<Project>(sessionProjects);
    }

    private static final class NestedGroupKey {

        private Object[] ids;

        public NestedGroupKey(Object[] ids) {
            this.ids = ids;
        }

        private static Object[] createIDs(BreakpointGroup g, Object id) {
            List ids = new ArrayList();
            ids.add(id);
            while (g != null) {
                ids.add(0, g.id);
                g = g.parent;
            }
            return ids.toArray();
        }

        public NestedGroupKey(BreakpointGroup g, Object id) {
            this(createIDs(g, id));
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof NestedGroupKey)) {
                return false;
            }
            return Arrays.equals(((NestedGroupKey) obj).ids, ids);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(ids);
        }

    }


    static final class GroupProperties {

        static GroupProperties getFrom(Breakpoint b) {
            try {
                java.lang.reflect.Method m;
                try {
                    m = b.getClass().getMethod("getGroupProperties");
                } catch (NoSuchMethodException ex) {
                    m = b.getClass().getDeclaredMethod("getGroupProperties");
                }
                m.setAccessible(true);
                Object gp = m.invoke(b);
                if (gp == null) {
                    return null;
                }
                return new GroupProperties(gp);
            } catch (NoSuchMethodException ex) {
                return null;
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        private Object gp;

        public GroupProperties(Object gp) {
            this.gp = gp;
        }

        private Object getMethod(String methodName) {
            try {
                java.lang.reflect.Method m;
                m = gp.getClass().getMethod(methodName);
                m.setAccessible(true);
                return m.invoke(gp);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        /**
         * Get the language of the source file with the breakpoint.
         * @return The human-readable language of the breakpoint source file or <code>null</code>
         * when this does not apply.
         * @see <code>org.netbeans.spi.debugger.ui.BreakpointType.getCategoryDisplayName()</code>
         */
        public String getLanguage() {
            return (String) getMethod("getLanguage");
        }

        /**
         * Get the breakpoint type.
         * @return The human-readable type of the breakpoint or <code>null</code>
         * when this does not apply.
         * @see <code>org.netbeans.spi.debugger.ui.BreakpointType.getTypeDisplayName()</code>
         */
        public String getType() {
            return (String) getMethod("getType");
        }

        /**
         * Get the source files containing this breakpoint.
         * @return The source files where this breakpoint is submitted or <code>null</code>
         * when this does not apply.
         */
        public FileObject[] getFiles() {
            return (FileObject[]) getMethod("getFiles");
        }

        /**
         * Get the projects containing this breakpoint.
         * @return The projects in which this breakpoint is submitted or <code>null</code>
         * when this does not apply.
         */
        public Project[] getProjects() {
            return (Project[]) getMethod("getProjects");
        }

        /**
         * Test is this breakpoint is hidden (not visible to the user).
         * @return <code>true</code> when this breakpoint is hidden, <code>false</code> otherwise.
         */
        public boolean isHidden() {
            return (Boolean) getMethod("isHidden");
        }
    }

}
