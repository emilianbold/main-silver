/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.kenai.spi;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiSupport.BugtrackingType;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
abstract class KenaiRepositories {

    private static KenaiRepositories instance;

    private final Map<String, Object> kenaiLocks = new HashMap<String, Object>(1);

    /**
     * Holds already created kenai repositories
     */
    private Map<String, RepositoryProvider> repositoriesMap = Collections.synchronizedMap(new HashMap<String, RepositoryProvider>());

    protected KenaiRepositories() { }

    public synchronized static KenaiRepositories getInstance() {
        if(instance == null) {
            instance = Lookup.getDefault().lookup(KenaiRepositories.class);
            if (instance == null) {
                instance = new DefaultImpl();
            }
        }
        return instance;
    }

    //--------------------------------------------------------------------------


    /**
     * Returns a {@link RepositoryProvider} representing the given {@link KenaiProject}
     *
     * @param kp
     * @return
     */
    public RepositoryProvider getRepository(KenaiProject kp) {
        return getRepository(kp, true);
    }

    /**
     * Returns a {@link RepositoryProvider} representing the given {@link KenaiProject}.
     *
     * @param kp KenaiProject
     * @param forceCreate determines if a RepositoryProvider instance should be created if it doesn't already exist
     * @return
     */
    public RepositoryProvider getRepository(KenaiProject kp, boolean forceCreate) {

        String repositoryKey = kp.getWebLocation().toString();
        BugtrackingManager.LOG.log(Level.FINER, "requesting repository for {0}", repositoryKey);  // NOI18N

        KenaiSupport support = getSupport(kp);
        if(support == null) {
            BugtrackingManager.LOG.log(Level.FINER, "no repository available for {0}", repositoryKey);  // NOI18N
            return null;
        }

        Object lock = getKenaiLock(kp, support);
        synchronized(lock) { // synchronize for a kenai instance and bugtracking type
            RepositoryProvider repository = repositoriesMap.get(repositoryKey);
            if(repository == null && forceCreate) {
                repository = support.createRepository(kp);
                if(repository != null) {
                    // XXX what if more repos?!
                    repositoriesMap.put(repositoryKey, repository);
                }
            }
            BugtrackingManager.LOG.log(
                    Level.FINER,
                    "returning repository {0}:{1} for {2}", // NOI18N
                    new Object[]{repository != null ? repository.getInfo().getDisplayName() : "null", repository != null ? repository.getInfo().getUrl() : "", repositoryKey});  // NOI18N
            return repository;
        }
    }

    private KenaiSupport getSupport(KenaiProject kp) {
        // find the support corresponding with the given project
        KenaiSupport support = null;
        BugtrackingConnector[] connectors = BugtrackingUtil.getBugtrackingConnectors();
        for (BugtrackingConnector c : connectors) {
            support = c.getLookup().lookup(KenaiSupport.class);
            if (support != null && support.getType() == kp.getType()) {
                BugtrackingManager.LOG.log(Level.FINER, "found suport for {0}", kp.getWebLocation().toString()); // NOI18N
                break;
            }
            support = null;
        }
        return support;
    }

    private Object getKenaiLock(KenaiProject kp, KenaiSupport support) {
        synchronized(kenaiLocks) {
            BugtrackingType type = support.getType();
            final String key = kp.getWebLocation().getHost() + ":" + type;  // NOI18N
            BugtrackingManager.LOG.log(Level.FINER, "requesting lock for {0}", key); // NOI18N
            Object lock = kenaiLocks.get(key);
            if(lock == null) {
                lock = new Object();
                kenaiLocks.put(key, lock);
            }
            BugtrackingManager.LOG.log(Level.FINER, "returning lock {0} for {1}", new Object[]{lock, key}); // NOI18N
            return lock;
        }
    }   

    /**
     * Returns bugtracking repositories of all Kenai projects.
     *
     * @param  allOpenProjects  if {@code false}, search only Kenai projects
     *                          that are currently open in the Kenai dashboard;
     *                          if {@code true}, search also all Kenai projects
     *                          currently opened in the IDE
     * @return  array of repositories collected from the projects
     *          (never {@code null})
     */
    public abstract RepositoryProvider[] getRepositories(boolean allOpenProjects);

    //--------------------------------------------------------------------------

    /**
     * The default implementation of {@code KenaiRepositories}.
     * This implementation is used if no other implementation is found
     * in the default lookup.
     */
    private static class DefaultImpl extends KenaiRepositories {

        @Override
        public RepositoryProvider[] getRepositories(boolean allOpenProjects) {
            if("true".equals(System.getProperty("netbeans.bugtracking.noOpenProjects", "false"))) {
                allOpenProjects = false; 
            }
            KenaiProject[] kenaiProjects = allOpenProjects
                                           ? union(getDashboardProjects(),
                                                   getProjectsViewProjects())
                                           : getDashboardProjects();

            RepositoryProvider[] result = new RepositoryProvider[kenaiProjects.length];

            EnumSet<BugtrackingType> reluctantSupports = EnumSet.noneOf(BugtrackingType.class);
            int count = 0;
            for (KenaiProject p : kenaiProjects) {
                if(!reluctantSupports.contains(p.getType())) {
                    RepositoryProvider repo = getRepository(p);
                    if (repo != null) {
                        result[count++] = repo;
                    } else {
                        KenaiSupport support = super.getSupport(p);
                        if(support != null) {
                            BugtrackingManager.LOG.log(
                                    Level.WARNING,
                                    "could not get repository for project {0} with {1} bugtracking type ",
                                    new Object[]{p.getWebLocation(), p.getType()});
                            // there is a support available for the projects bugtracking type, yet
                            // we weren't able to create a repository for the project.
                            // lets assume there is something with the bugracker or that the user canceled
                            // the authorisation (see also issue #182946) and skip all other projects with the same
                            // support in this one call.
                            reluctantSupports.add(support.getType());
                        }
                    }
                } else {
                    BugtrackingManager.LOG.log(
                                    Level.WARNING,
                                    "skipping getRepository for project {0} with {1} bugtracking type ",
                                    new Object[]{p.getWebLocation(), p.getType()});
                }
            }
            return stripTrailingNulls(result);
        }

        private KenaiProject[] getDashboardProjects() {
            return KenaiUtil.getDashboardProjects();
        }

        private KenaiProject[] getProjectsViewProjects() {
            Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
            if (openProjects.length == 0) {
                return new KenaiProject[0];
            }

            int count = 0;
            KenaiProject[] kenaiProjects = new KenaiProject[openProjects.length];
            for (Project p : openProjects) {
                KenaiProject kenaiProject = getKenaiProject(p);
                if (kenaiProject != null) {
                    kenaiProjects[count++] = kenaiProject;
                }
            }

            return stripTrailingNulls(kenaiProjects);
        }

        private static KenaiProject getKenaiProject(Project p) {
            FileObject rootDir = p.getProjectDirectory();
            Object attValue = rootDir.getAttribute(
                                       "ProvidedExtensions.RemoteLocation");//NOI18N
            if (!(attValue instanceof String)) {
                return null;
            }

            String url = (String) attValue;
            KenaiProject kenaiProject = null;
            try {
                if(BugtrackingUtil.isNbRepository(url)) {
                    KenaiAccessor kenaiAccessor = KenaiUtil.getKenaiAccessor();
                    if(kenaiAccessor != null) {
                        OwnerInfo owner = kenaiAccessor.getOwnerInfo(FileUtil.toFile(p.getProjectDirectory()));
                        if(owner != null) {
                            kenaiProject = KenaiUtil.getKenaiProject(url, owner.getOwner());
                        }
                    } else {
                        // might be deactivated
                        BugtrackingManager.LOG.fine("kenai accessor not available");
                    }
                } else {
                    kenaiProject = KenaiUtil.getKenaiProjectForRepository(url);
                }

            } catch (IOException ex) {
                kenaiProject = null;
                BugtrackingManager.LOG.log(Level.WARNING,
                        "No Kenai project is available for bugtracking repository " //NOI18N
                        + " [" + url + "]"); //NOI18N
                BugtrackingManager.LOG.log(Level.FINE, null, ex);
            }
            return kenaiProject;
        }

        private static KenaiProject[] union(KenaiProject[]... projectArrays) {
            Map<String, KenaiProject> union = new HashMap<String, KenaiProject>();
            for (KenaiProject[] projectArray : projectArrays) {
                for (KenaiProject p : projectArray) {
                    String name = p.getName();
                    if (!union.keySet().contains(name)) {
                        union.put(name, p);
                    }
                }
            }
            return union.values().toArray(new KenaiProject[union.values().size()]);
        }

        private static <T> T[] stripTrailingNulls(T[] array) {

            /* count trailing nulls -> compute size of the resulting array */
            int resultSize = array.length;
            while ((resultSize > 0) && (array[resultSize - 1] == null)) {
                resultSize--;
            }

            if (resultSize == array.length) {       //no trailing nulls
                return array;
            }

            T[] result = (T[]) java.lang.reflect.Array.newInstance(
                                                array.getClass().getComponentType(),
                                                resultSize);
            if (resultSize != 0) {
                System.arraycopy(array, 0, result, 0, resultSize);
            }
            return result;
        }

    }

}
