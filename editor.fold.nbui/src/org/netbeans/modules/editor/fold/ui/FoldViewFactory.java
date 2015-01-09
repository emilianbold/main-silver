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

package org.netbeans.modules.editor.fold.ui;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.text.View;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.view.EditorView;
import org.netbeans.modules.editor.lib2.view.EditorViewFactory;
import org.netbeans.modules.editor.lib2.view.EditorViewFactoryChange;
import org.netbeans.modules.editor.lib2.view.ViewUtils;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * View factory creating views for collapsed folds.
 *
 * @author Miloslav Metelka
 */

@SuppressWarnings("ClassWithMultipleLoggers")
public final class FoldViewFactory extends EditorViewFactory implements FoldHierarchyListener, LookupListener, PreferenceChangeListener {

    /**
     * Component's client property which can be set to view folds expanded for tooltip fold preview.
     */
    static final String VIEW_FOLDS_EXPANDED_PROPERTY = "view-folds-expanded"; // NOI18N

    // -J-Dorg.netbeans.editor.view.change.level=FINE
    static final Logger CHANGE_LOG = Logger.getLogger("org.netbeans.editor.view.change"); // NOI18N

    // -J-Dorg.netbeans.modules.editor.fold.FoldViewFactory.level=FINE
    private static final Logger LOG = Logger.getLogger(FoldViewFactory.class.getName());

    public static void register() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Fold view factory registered");
        }
        EditorViewFactory.registerFactory(new FoldFactory());
    }

    private FoldHierarchy foldHierarchy;

    private boolean foldHierarchyLocked;

    private Fold fold;

    private int foldStartOffset;

    private Iterator<Fold> collapsedFoldIterator;

    private boolean viewFoldsExpanded;
    
    /**
     * Composite Color settings from MIME lookup
     */
    private FontColorSettings   colorSettings;

    /**
     * Lookup results for color settings, being listened for changes.
     */
    private Lookup.Result       colorSource;
    
    private Preferences         prefs;
    
    private int viewFlags = 0;
    
    private final FoldHierarchyListener weakL;

    public FoldViewFactory(View documentView) {
        super(documentView);
        foldHierarchy = FoldHierarchy.get(textComponent());
        // the view factory may get eventually GCed, but the FoldHierarchy can survive, snce it is tied to the component.
        weakL = WeakListeners.create(FoldHierarchyListener.class, this, foldHierarchy);
        foldHierarchy.addFoldHierarchyListener(weakL);
        viewFoldsExpanded = Boolean.TRUE.equals(textComponent().getClientProperty(VIEW_FOLDS_EXPANDED_PROPERTY));
        
        String mime = DocumentUtilities.getMimeType(document());
        
        Lookup lkp = MimeLookup.getLookup(mime);
        colorSource = lkp.lookupResult(FontColorSettings.class);
        colorSource.addLookupListener(WeakListeners.create(LookupListener.class, this, colorSource));
        colorSettings = (FontColorSettings)colorSource.allInstances().iterator().next();
        prefs = lkp.lookup(Preferences.class);
        prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));
        
        initViewFlags();
    }
    
    private void initViewFlags() {
        viewFlags =
                (prefs.getBoolean(FoldUtilitiesImpl.PREF_CONTENT_PREVIEW, true) ? 1 : 0) |
                (prefs.getBoolean(FoldUtilitiesImpl.PREF_CONTENT_SUMMARY, true) ? 2 : 0);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        refreshColors();
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        String k = evt.getKey();
        if (FoldUtilitiesImpl.PREF_CONTENT_PREVIEW.equals(k) ||
            FoldUtilitiesImpl.PREF_CONTENT_SUMMARY.equals(k)) {
            initViewFlags();
            document().render(new Runnable() {
                @Override
                public void run() {
                    int end = document().getLength();
                    fireEvent(EditorViewFactoryChange.createList(0, end, EditorViewFactoryChange.Type.CHARACTER_CHANGE));
                }
            });
        }
    }

    private void refreshColors() {
        colorSettings = (FontColorSettings)colorSource.allInstances().iterator().next();
        document().render(new Runnable() {
            @Override
            public void run() {
                int end = document().getLength();
                fireEvent(EditorViewFactoryChange.createList(0, end, EditorViewFactoryChange.Type.CHARACTER_CHANGE));
            }
        });
    }

    @Override
    public void restart(int startOffset, int endOffset, boolean createViews) {
        foldHierarchy.lock(); // this.finish() always called in try-finally
        foldHierarchyLocked = true;
        @SuppressWarnings("unchecked")
        Iterator<Fold> it = FoldUtilities.collapsedFoldIterator(foldHierarchy, startOffset, Integer.MAX_VALUE);
        collapsedFoldIterator = it;
        foldStartOffset = -1; // Make a next call to updateFold() to fetch a fold
    }

    private void updateFold(int offset) {
        if (foldStartOffset < offset) {
            while (collapsedFoldIterator.hasNext()) {
                fold = collapsedFoldIterator.next();
                foldStartOffset = fold.getStartOffset();
                // avoid issue #229852; the length might be 0, if text was deleted and the fold collapsed to nothing &&
                // the fold hierarchy was not refreshed yet (the hierarchy locks itself in post update)
                int l = fold.getEndOffset() - foldStartOffset;
                if (foldStartOffset >= offset && l > 0) {
                    return;
                }
            }
            fold = null;
            foldStartOffset = Integer.MAX_VALUE;
        }
    }

    @Override
    public int nextViewStartOffset(int offset) {
        if (!viewFoldsExpanded) {
            updateFold(offset);
            return foldStartOffset;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public EditorView createView(int startOffset, int limitOffset, boolean forcedLimit,
    EditorView origView, int nextOrigViewOffset) {
        assert (startOffset == foldStartOffset) : "startOffset=" + startOffset + " != foldStartOffset=" + foldStartOffset; // NOI18N
        if (fold.getEndOffset() <= limitOffset || !forcedLimit) {
            return new FoldView(textComponent(), fold, colorSettings, viewFlags);
        } else {
            return null;
        }
    }
    
    @Override
    public int viewEndOffset(int startOffset, int limitOffset, boolean forcedLimit) {
        int foldEndOffset = fold.getEndOffset();
        if (foldEndOffset <= limitOffset) {
            return foldEndOffset;
        } else {
            return -1;
        }
    }

    @Override
    public void continueCreation(int startOffset, int endOffset) {
    }

    @Override
    public void finishCreation() {
        fold = null;
        collapsedFoldIterator = null;
        if (foldHierarchyLocked) {
            foldHierarchy.unlock();
        }
    }

    @Override
    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
        // For fold state changes use a higher priority
        int startOffset = evt.getAffectedStartOffset();
        int endOffset = evt.getAffectedEndOffset();
        if (CHANGE_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(CHANGE_LOG, "CHANGE in FoldViewFactory: <" + // NOI18N
                    startOffset + "," + endOffset + ">\n"); // NOI18N
        }
        fireEvent(EditorViewFactoryChange.createList(startOffset, endOffset,
                EditorViewFactoryChange.Type.PARAGRAPH_CHANGE));
    }

    public static final class FoldFactory implements EditorViewFactory.Factory {

        @Override
        public EditorViewFactory createEditorViewFactory(View documentView) {
            return new FoldViewFactory(documentView);
        }

        @Override
        public int weight() {
            return 100;
        }

    }

}
