/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryRegistry {


    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     * a repository from this connector was created, removed or changed
     */
    public final static String EVENT_REPOSITORIES_CHANGED = "bugtracking.repositories.changed"; // NOI18N
    
    private static final String REPO_ID           = "bugracking.repository_";   // NOI18N
    private static final String DELIMITER         = "<=>";                      // NOI18N    
    
    private static final Object REPOSITORIES_LOCK = new Object();
    
    private static RepositoryRegistry instance;

    private RepositoriesMap repositories;
        
    private RepositoryRegistry() {}
    
    public static synchronized RepositoryRegistry getInstance() {
        if(instance == null) {
            instance = new RepositoryRegistry();
        }
        return instance;
    }
    
    public RepositoryProvider[] getRepositories() {
        synchronized(REPOSITORIES_LOCK) {
            List<RepositoryProvider> l = getStoredRepositories().getRepositories();
            return l.toArray(new RepositoryProvider[l.size()]);
        }
    }

    public RepositoryProvider[] getRepositories(String connectorID) {
        synchronized(REPOSITORIES_LOCK) {
            final Map<String, RepositoryProvider> m = getStoredRepositories().get(connectorID);
            if(m != null) {
                Collection<RepositoryProvider> c = m.values();
                return c.toArray(new RepositoryProvider[c.size()]);
            } else {
                return new RepositoryProvider[0];
            }
        }
    }

    private RepositoriesMap getStoredRepositories() {
        if (repositories == null) {
            repositories = new RepositoriesMap();
            String[] ids = getRepositoryIds();
            if (ids == null || ids.length == 0) {
                return repositories;
            }
            DelegatingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
            for (String id : ids) {
                String idArray[] = id.split(DELIMITER);
                String connectorId = idArray[1];
                for (DelegatingConnector c : connectors) {
                    if(c.getID().equals(connectorId)) {
                        RepositoryInfo info = SPIAccessor.IMPL.read(getPreferences(), id);
                        if(info != null) {
                            RepositoryProvider repo = c.createRepository(info);
                            if (repo != null) {
                                repositories.put(connectorId, repo);
                            }
                        }
                    }
                }
            }
        }
        return repositories;
    }
  
    public void addRepository(RepositoryProvider repository) {
        assert repository != null;
        if(KenaiUtil.isKenai(repository) && !BugtrackingUtil.isNbRepository(repository)) {
            // we don't store kenai repositories - XXX  shouldn't be even called
            return;        
        }
        Collection<RepositoryProvider> oldRepos;
        Collection<RepositoryProvider> newRepos;
        synchronized(REPOSITORIES_LOCK) {
            oldRepos = Collections.unmodifiableCollection(new LinkedList<RepositoryProvider>(getStoredRepositories().getRepositories()));
            String connectorID = repository.getInfo().getConnectorId();
            getStoredRepositories().put(repository.getInfo().getConnectorId(), repository); // cache
            putRepository(connectorID, repository); // persist
            newRepos = Collections.unmodifiableCollection(getStoredRepositories().getRepositories());

        }
        fireRepositoriesChanged(oldRepos, newRepos);
    }    

    public void removeRepository(RepositoryProvider repository) {
        Collection<RepositoryProvider> oldRepos;
        Collection<RepositoryProvider> newRepos;
        synchronized(REPOSITORIES_LOCK) {
            oldRepos = Collections.unmodifiableCollection(getStoredRepositories().getRepositories());
            String connectorID = repository.getInfo().getConnectorId();
            // persist remove
            getPreferences().remove(REPO_ID + DELIMITER + connectorID + DELIMITER + repository.getInfo().getId()); 
            // remove from cache
            getStoredRepositories().remove(connectorID, repository);
            
            newRepos = Collections.unmodifiableCollection(getStoredRepositories().getRepositories());
        }
        fireRepositoriesChanged(oldRepos, newRepos);
    }
    
    /**
     *
     * Returns all known repositories incl. the Kenai ones
     *
     * @param pingOpenProjects 
     * @return repositories
     */
    public RepositoryProvider[] getKnownRepositories(boolean pingOpenProjects) {
        RepositoryProvider[] kenaiRepos = KenaiUtil.getRepositories(pingOpenProjects);
        RepositoryProvider[] otherRepos = getRepositories();
        RepositoryProvider[] ret = new RepositoryProvider[kenaiRepos.length + otherRepos.length];
        System.arraycopy(kenaiRepos, 0, ret, 0, kenaiRepos.length);
        System.arraycopy(otherRepos, 0, ret, kenaiRepos.length, otherRepos.length);
        return ret;
    }
  
    private String[] getRepositoryIds() {
        return getKeysWithPrefix(REPO_ID);
    }
    
    private void putRepository(String connectorID, RepositoryProvider repository) {
        RepositoryInfo info = repository.getInfo();
        final String key = REPO_ID + DELIMITER + connectorID + DELIMITER + info.getId();
        SPIAccessor.IMPL.store(getPreferences(), info, key);

        char[] password =info.getPassword();
        char[] httpPassword = info.getHttpPassword();
        BugtrackingUtil.savePassword(password, null, info.getUsername(), info.getUrl());
        BugtrackingUtil.savePassword(httpPassword, "http", info.getHttpUsername(), info.getUrl()); // NOI18N
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(RepositoryRegistry.class);
    }   
    
    private String[] getKeysWithPrefix(String prefix) {
        String[] keys = null;
        try {
            keys = getPreferences().keys();
        } catch (BackingStoreException ex) {
            BugtrackingManager.LOG.log(Level.SEVERE, null, ex); // XXX
        }
        if (keys == null || keys.length == 0) {
            return new String[0];
        }
        List<String> ret = new ArrayList<String>();
        for (String key : keys) {
            if (key.startsWith(prefix)) {
                ret.add(key);
            }
        }
        return ret.toArray(new String[ret.size()]);
    }
    
    /**
     * remove a listener from this connector
     * @param listener
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Add a listener to this connector to listen on events
     * @param listener
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     *
     * @param oldRepositories - lists repositories which were available for the connector before the change
     * @param newRepositories - lists repositories which are available for the connector after the change
     */
    public void fireRepositoriesChanged(Collection<RepositoryProvider> oldRepositories, Collection<RepositoryProvider> newRepositories) {
        changeSupport.firePropertyChange(EVENT_REPOSITORIES_CHANGED, oldRepositories, newRepositories);
    }

    private class RepositoriesMap extends HashMap<String, Map<String, RepositoryProvider>> {
        public void remove(String connectorID, RepositoryProvider repository) {
            Map<String, RepositoryProvider> m = get(connectorID);
            if(m != null) {
                m.remove(repository.getInfo().getId());
            }
        }
        public void put(String connectorID, RepositoryProvider repository) {
            Map<String, RepositoryProvider> m = get(connectorID);
            if(m == null) {
                m = new HashMap<String, RepositoryProvider>();
                put(connectorID, m);
            }
            m.put(repository.getInfo().getId(), repository);
        }
        List<RepositoryProvider> getRepositories() {
            List<RepositoryProvider> ret = new LinkedList<RepositoryProvider>();
            for (Entry<String, Map<String, RepositoryProvider>> e : entrySet()) {
                ret.addAll(e.getValue().values());
            }
            return ret;
        }
        
    }
}
