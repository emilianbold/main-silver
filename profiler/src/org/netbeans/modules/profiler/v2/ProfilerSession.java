/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.profiler.v2;

import java.util.Objects;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.event.ProfilingStateListener;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class ProfilerSession {
    
    // --- Static access -------------------------------------------------------
    
    private static ProfilerSession CURRENT_SESSION;
    private static final Object CURRENT_SESSION_LOCK = new Object();
    
    
    public static ProfilerSession forContext(Lookup context) {
        // Try to reuse the active session first
        synchronized(CURRENT_SESSION_LOCK) {
            if (CURRENT_SESSION != null && CURRENT_SESSION.isCompatibleContext(context)) {
                CURRENT_SESSION.setContext(context);
                return CURRENT_SESSION;
            }
        }
        
        // Create a new session, will eliminate another session when showing UI
        Provider provider = Lookup.getDefault().lookup(Provider.class);
        return provider == null ? null : provider.getSession(context);
    };
    
    public static ProfilerSession forProject(Lookup.Provider project) {
        // Try to reuse the active session first
        synchronized(CURRENT_SESSION_LOCK) {
            if (CURRENT_SESSION != null && Objects.equals(project, CURRENT_SESSION.getProject())) {
                return CURRENT_SESSION;
            }
        }
        
        // Create a new session, will eliminate another session when showing UI
        Provider provider = Lookup.getDefault().lookup(Provider.class);
        return provider == null ? null : provider.getSession(Lookups.fixed(project));
    };
    
    public static ProfilerSession currentSession() {
        synchronized(CURRENT_SESSION_LOCK) {
            return CURRENT_SESSION;
        }
    }
    
    
    public static void findAndConfigure(Lookup conf, Lookup.Provider project) {
        final ProfilerSession current = currentSession();
        if (current != null) current.configure(conf);
        else ProfilerSessions.createAndConfigure(conf, project);
    }
    
    // --- Constructor ---------------------------------------------------------
    
    protected ProfilerSession(NetBeansProfiler _profiler, Lookup context) {
        profiler = _profiler;
        setContext(context);
    }
    
    // --- Context -------------------------------------------------------------
    
    private Lookup context;
    
    
    protected synchronized final Lookup getContext() { return context; }
    
    private final void setContext(Lookup _context) {
        synchronized(this) { context = _context; }
        
        UIUtils.runInEventDispatchThread(new Runnable() {
            public void run() { if (window != null) window.updateSession(); }
        });
    }
    
    protected synchronized boolean isCompatibleContext(Lookup _context) {
        return Objects.equals(getProject(), _context.lookup(Lookup.Provider.class));
    }
    
    // --- SPI -----------------------------------------------------------------
    
    protected abstract void start();
    
    protected abstract void modify();
    
    protected abstract void terminate();
    
    // --- API -----------------------------------------------------------------
    
    private final NetBeansProfiler profiler;
    private ProfilerWindow window;
    
    private ProfilingSettings profilingSettings;
    private AttachSettings attachSettings;
    
    
    public final NetBeansProfiler getProfiler() { return profiler; }
    
    public abstract Lookup.Provider getProject();
    
    public abstract FileObject getFile();
    
    public final ProfilingSettings getProfilingSettings() { return profilingSettings; }
    
    public final AttachSettings getAttachSettings() { return attachSettings; }
    
    public final void setAttach(boolean attach) {
        if (!attach) clearAttachSettings();
        else initAttachSettings();
    }
    
    public final void requestActive() {
        UIUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                synchronized(CURRENT_SESSION_LOCK) {
                    if (CURRENT_SESSION != null && CURRENT_SESSION != ProfilerSession.this) {
                        ProfilerWindow w = CURRENT_SESSION.window;
                        if (w != null && !w.close()) return;
                    }
                }

                ProfilerWindow w = getWindow();
                w.open();
                w.requestActive();

                synchronized(CURRENT_SESSION_LOCK) {
                    CURRENT_SESSION = ProfilerSession.this;
                }
            }
        });
    };
    
    // --- Profiler API bridge -------------------------------------------------
    
    public final int getState() {
        return profiler.getProfilingState();
    }
    
    public final boolean inProgress() {
        return getState() != NetBeansProfiler.PROFILING_INACTIVE;
    }
    
    public final void addListener(ProfilingStateListener listener) {
        profiler.addProfilingStateListener(listener);
    }
    
    public final void removeListener(ProfilingStateListener listener) {
        profiler.removeProfilingStateListener(listener);
    }
    
    // --- Internal API --------------------------------------------------------
    
    private ProfilerFeatures features;
    
    
    final void doStart(ProfilingSettings pSettings, AttachSettings aSettings) {
        profilingSettings = pSettings;
        attachSettings = aSettings;
        start();
    }
    
    final void doModify(ProfilingSettings pSettings) {
        profilingSettings = pSettings;
        modify();
    }
    
    final void doTerminate() {
        terminate();
    }
    
    final ProfilerFeatures getFeatures() {
        assert !SwingUtilities.isEventDispatchThread();
        
        synchronized(this) {
            if (features == null) features = new ProfilerFeatures(this);
        }
        
        return features;
    }
    
    // --- Implementation ------------------------------------------------------
    
    private ProfilerWindow getWindow() {
        assert SwingUtilities.isEventDispatchThread();
        
        if (window == null) {
            window = new ProfilerWindow(ProfilerSession.this) {
                protected void componentClosed() {
                    super.componentClosed();
                    cleanup();
                }
            };
        }
        return window;
    }
    
    private void configure(final Lookup conf) {
        final ProfilerFeatures _features = getFeatures();
        final Set<ProfilerFeature> compatA = ProfilerFeatures.getCompatible(
                                             _features.getAvailable(), conf);
        if (compatA.isEmpty()) {
            // TODO: might offer creating a new profiling session if the current is not in progress
            ProfilerDialogs.displayInfo("Action not supported by the current profiling session.");
        } else {
            // Resolving selected features in only supported in EDT
            UIUtils.runInEventDispatchThread(new Runnable() {
                public void run() {
                    Set<ProfilerFeature> compatS = ProfilerFeatures.getCompatible(
                                                   _features.getSelected(), conf);

                    ProfilerFeature feature;
                    if (compatS.size() == 1) {
                        // Exactly one selected feature handles the action
                        feature = compatS.iterator().next();
                    } else if (!compatS.isEmpty()) {
                        // Multiple selected features handle the action
                        feature = ProfilerSessions.selectFeature(ProfilerSession.this, compatS);
                    } else if (compatA.size() == 1) {
                        // Exactly one available feature handles the action
                        feature = compatA.iterator().next();
                    } else {
                        // Multiple available features handle the action
                        feature = ProfilerSessions.selectFeature(ProfilerSession.this, compatA);
                    }

                    if (feature != null) {
                        _features.selectFeature(feature);
                        feature.configure(conf);
                        getWindow().selectFeature(feature);
                        requestActive();
                    }
                }
            });
        }
    }
    
    private void initAttachSettings() {
        if (attachSettings != null) return;
        
        attachSettings = new AttachSettings();
        // TODO: load attach settings
    }
    
    private void clearAttachSettings() {
        if (attachSettings == null) return;
        
        // TODO: save attach settings
        attachSettings = null;
    }
    
    private void cleanup() {
        synchronized(CURRENT_SESSION_LOCK) {
            if (CURRENT_SESSION == this) CURRENT_SESSION = null;
        }
        
        // TODO: unregister listeners (this.addListener) to prevent memory leaks
    }
    
    // --- Provider ------------------------------------------------------------
    
    public static abstract class Provider {
        
        public abstract ProfilerSession getSession(Lookup context);
        
    }
    
}
