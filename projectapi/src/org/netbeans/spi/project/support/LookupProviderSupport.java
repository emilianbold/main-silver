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

package org.netbeans.spi.project.support;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.projectapi.MetaLookupMerger;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Factory for lookup capable of merging content from registered 
 * {@link org.netbeans.spi.project.LookupProvider} instances.
 * @author mkleint
 * @since org.netbeans.modules.projectapi 1.12
 */
public final class LookupProviderSupport {
    
    private LookupProviderSupport() {
    }
    
    private static final Logger LOG = Logger.getLogger(LookupProviderSupport.class.getName());

    /**
     * Creates a project lookup instance that combines the content from multiple sources. 
     * A convenience factory method for implementors of Project.
     * 
     * @param baseLookup initial, base content of the project lookup created by the project owner
     * @param folderPath the path in the System Filesystem that is used as root for lookup composition, as for {@link Lookups#forPath}.
     *        The content of the folder is assumed to be {@link LookupProvider} instances.
     * @return a lookup to be used in project
     */ 
    public static Lookup createCompositeLookup(Lookup baseLookup, String folderPath) {
        return new DelegatingLookupImpl(baseLookup, folderPath);
    }
    
    /**
     * Factory method for creating {@link org.netbeans.spi.project.LookupMerger} instance that merges
     * {@link org.netbeans.api.project.Sources} instances in the project lookup. 
     * Allows to compose the {@link org.netbeans.api.project.Sources}
     * content from multiple sources.
     * @return instance to include in project lookup
     */
    public static LookupMerger<Sources> createSourcesMerger() {
        return new SourcesMerger();
    }
    
    static class DelegatingLookupImpl extends ProxyLookup implements LookupListener, ChangeListener {
        private final Lookup baseLookup;
        static class UnmergedLookup extends ProxyLookup {
            void _setLookups(Lookup... lookups) {
                setLookups(lookups);
            }
        }
        private final UnmergedLookup unmergedLookup = new UnmergedLookup();
        private final Map<LookupMerger<?>,Object> mergerResults = new WeakHashMap<LookupMerger<?>,Object>();
        private final Lookup.Result<LookupProvider> providerResult;
        private final LookupListener providerListener;
        private List<LookupProvider> old = Collections.emptyList();
        private List<Lookup> currentLookups;
        private final ChangeListener metaMergerListener;

        @SuppressWarnings("rawtypes")
        private Lookup.Result<LookupMerger> mergers;
        private final Lookup.Result<MetaLookupMerger> metaMergers;
        private Reference<LookupListener> listenerRef;
        //#68623: the proxy lookup fires changes only if someone listens on a particular template:
        private final List<Lookup.Result<?>> results = new ArrayList<Lookup.Result<?>>();
        
        public DelegatingLookupImpl(Lookup base, String path) {
            this(base, Lookups.forPath(path), path);
        }
        
        @SuppressWarnings("LeakingThisInConstructor")
        public DelegatingLookupImpl(Lookup base, Lookup providerLookup, String path) {
            super();
            assert base != null;
            baseLookup = base;
            providerResult = providerLookup.lookup(new Lookup.Template<LookupProvider>(LookupProvider.class));
            metaMergers = providerLookup.lookupResult(MetaLookupMerger.class);
            metaMergerListener = WeakListeners.change(this, null);
            assert isAllJustLookupProviders(providerLookup) : 
                "Layer content at " + path + " contains other than LookupProvider instances! See messages.log file for more details."; //NOI18N
            doDelegate();
            providerListener = new LookupListener() {
                public @Override void resultChanged(LookupEvent ev) {
                    // XXX this may need to be run asynchronously; deadlock-prone
                    doDelegate();
                }
            };
            providerResult.addLookupListener(
                WeakListeners.create(LookupListener.class, providerListener, providerResult));
            metaMergers.addLookupListener(
                WeakListeners.create(LookupListener.class, providerListener, metaMergers));
        }
        
        //just for assertion evaluation.
        private boolean isAllJustLookupProviders(Lookup lkp) {
            for (Lookup.Item<?> item : lkp.lookupResult(Object.class).allItems()) {
                Class<?> clzz = item.getType();
                if (!LookupProvider.class.isAssignableFrom(clzz) && !MetaLookupMerger.class.isAssignableFrom(clzz)) {
                    LOG.log(Level.WARNING, "{0} from {1} is not a LookupProvider", new Object[] {clzz.getName(), item.getId()});
                }
            }
            return true; // always just print warnings
        }
        
        
        public @Override void resultChanged(LookupEvent ev) {
            doDelegate();
        }

        protected @Override void beforeLookup(Lookup.Template<?> template) {
            for (MetaLookupMerger metaMerger : metaMergers.allInstances()) {
                metaMerger.probing(template.getType()); // might fire ChangeEvent, see below
            }
        }
        
        public @Override void stateChanged(ChangeEvent e) {
            // A metamerger loaded its class and is now ready for service.
            doDelegate();
        }
        
        private void doDelegate() {
            synchronized (results) {
            for (Lookup.Result<?> r : results) {
                r.removeLookupListener(this);
            }
            results.clear();
            Collection<? extends LookupProvider> providers = providerResult.allInstances();
            List<Lookup> newLookups = new ArrayList<Lookup>();
            for (LookupProvider elem : providers) {
                if (old.contains(elem)) {
                    int index = old.indexOf(elem);
                    newLookups.add(currentLookups.get(index));
                } else {
                    Lookup newone = elem.createAdditionalLookup(baseLookup);
                    assert newone != null;
                    newLookups.add(newone);
                }
            }
            old = new ArrayList<LookupProvider>(providers);
            currentLookups = newLookups;
            newLookups.add(baseLookup);
            unmergedLookup._setLookups(newLookups.toArray(new Lookup[newLookups.size()]));
            List<Class<?>> filteredClasses = new ArrayList<Class<?>>();
            List<Object> mergedInstances = new ArrayList<Object>();
            LookupListener l = listenerRef != null ? listenerRef.get() : null;
            if (l != null) {
                mergers.removeLookupListener(l);
            }
            mergers = unmergedLookup.lookupResult(LookupMerger.class); // XXX maybe do this just in ctor
            l = WeakListeners.create(LookupListener.class, this, mergers);
            listenerRef = new WeakReference<LookupListener>(l);
            mergers.addLookupListener(l);
            @SuppressWarnings("rawtypes") Collection<LookupMerger> allMergers = new ArrayList<LookupMerger>(mergers.allInstances());
            for (MetaLookupMerger metaMerger : metaMergers.allInstances()) {
                LookupMerger<?> merger = metaMerger.merger();
                if (merger != null) {
                    allMergers.add(merger);
                }
                metaMerger.removeChangeListener(metaMergerListener);
                metaMerger.addChangeListener(metaMergerListener);
            }
            for (LookupMerger<?> lm : allMergers) {
                Class<?> c = lm.getMergeableClass();
                if (filteredClasses.contains(c)) {
                    LOG.log(Level.WARNING, "Two LookupMerger registered for {0}. Only first one will be used", c);
                    continue;
                }
                filteredClasses.add(c);
                Object merge = mergerResults.get(lm);
                if (merge == null) {
                    merge = lm.merge(unmergedLookup);
                    mergerResults.put(lm, merge);
                }
                mergedInstances.add(merge);
                Lookup.Result<?> result = unmergedLookup.lookupResult(c);
                result.addLookupListener(this);
                results.add(result);
            }
            Lookup filtered = Lookups.exclude(unmergedLookup, filteredClasses.toArray(new Class<?>[filteredClasses.size()]));
            Lookup fixed = Lookups.fixed(mergedInstances.toArray(new Object[mergedInstances.size()]));
            setLookups(fixed, filtered);
        }
        }
    }
    
    
    private static class SourcesMerger implements LookupMerger<Sources> {
        public @Override Class<Sources> getMergeableClass() {
            return Sources.class;
        }

        public @Override Sources merge(Lookup lookup) {
            return new SourcesImpl(lookup);
        }
    }
    
    private static class SourcesImpl implements Sources, ChangeListener, LookupListener {
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private Lookup.Result<Sources> delegates;
        private Collection<Sources> currentDelegates = new ArrayList<Sources>();
        
        public SourcesImpl(Lookup lookup) {
            if (currentDelegates.size() > 0) {
                for (Sources old : currentDelegates) {
                    old.removeChangeListener(this);
                }
                currentDelegates.clear();
            }
            if (delegates != null) {
                delegates.removeLookupListener(this);
            }
            Lookup.Result<Sources> srcs = lookup.lookupResult(Sources.class);
            for (Sources ns : srcs.allInstances()) {
                assert ns != this;
                ns.addChangeListener(this);
                currentDelegates.add(ns);
            }
            srcs.addLookupListener(this);
            delegates = srcs;
            changeSupport.fireChange();
        }

        public @Override SourceGroup[] getSourceGroups(String type) {
            assert delegates != null;
            Collection<SourceGroup> result = new ArrayList<SourceGroup>();
            for (Sources ns : delegates.allInstances()) {
                SourceGroup[] sourceGroups = ns.getSourceGroups(type);
                if (sourceGroups != null) {
                    for (SourceGroup sourceGroup : sourceGroups) {
                        if (sourceGroup == null) {
                            Exceptions.printStackTrace(new NullPointerException(ns + " returns null source group!"));
                        } else {
                            result.add(sourceGroup);
                        }
                    }
                }
            }
            return result.toArray(new SourceGroup[result.size()]);
        }

        public @Override void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        public @Override void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        public @Override void stateChanged(ChangeEvent e) {
            changeSupport.fireChange();
        }

        public @Override void resultChanged(LookupEvent ev) {
            if (currentDelegates.size() > 0) {
                for (Sources old : currentDelegates) {
                    old.removeChangeListener(this);
                }
                currentDelegates.clear();
            }
            for (Sources ns : delegates.allInstances()) {
                ns.addChangeListener(this);
                currentDelegates.add(ns);
            }
            changeSupport.fireChange();
        }
    }
    
}
