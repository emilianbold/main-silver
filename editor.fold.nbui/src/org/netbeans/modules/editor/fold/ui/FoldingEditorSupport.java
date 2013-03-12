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
package org.netbeans.modules.editor.fold.ui;

import java.util.concurrent.Callable;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseCaret;
import org.netbeans.spi.editor.fold.FoldHierarchyMonitor;

/**
 * Provides adjustments to functions of editor component
 * based on folding operations.
 * This code was originally part of editor.lib, in BaseCaret class.
 * 
 * @author sdedic
 */
public class FoldingEditorSupport implements FoldHierarchyListener {
    private static final Logger LOG = Logger.getLogger(FoldingEditorSupport.class.getName());
    
    /**
     * Component where the folding takes place
     */
    private final JTextComponent component;
    
    /**
     * Fold hierarchy
     */
    private final FoldHierarchy foldHierarchy;
    
    FoldingEditorSupport(FoldHierarchy h, JTextComponent component) {
        this.component = component;
        this.foldHierarchy = h;
        component.putClientProperty("org.netbeans.api.fold.expander", new C());
        foldHierarchy.addFoldHierarchyListener(this);
    }
    
    private class C implements Runnable, Callable<Boolean> {
        private boolean res;
        private boolean sharp;
        
        public void run() {
            foldHierarchy.lock();
            try {
                int offset = component.getCaret().getDot();
                res = false;
                Fold f = FoldUtilities.findCollapsedFold(foldHierarchy, offset, offset);
                if (f != null) {
                    if (sharp) {
                        res = f.getStartOffset() < offset && f.getEndOffset() > offset;
                    } else {
                        res = f.getStartOffset() <= offset && f.getEndOffset() >= offset;
                    }
                    if (res) {
                        foldHierarchy.expand(f);
                    }
                }
            } finally {
                foldHierarchy.unlock();
            }
        }
        
        public boolean equals(Object whatever) {
            if (!(whatever instanceof Caret)) {
                return super.equals(whatever);
            }
            sharp = false;
            final Document doc = component.getDocument();
            doc.render(this);
            return res;
        }
        
        public Boolean call() {
            sharp = true;
            final Document doc = component.getDocument();
            doc.render(this);
            return res;
        }
    }
    
    public @Override void foldHierarchyChanged(FoldHierarchyEvent evt) {
        if (!(component.getCaret() instanceof BaseCaret)) {
            return;
        }
        int caretOffset = component.getCaret().getDot();
        final int addedFoldCnt = evt.getAddedFoldCount();
        final boolean scrollToView;
        LOG.finest("Received fold hierarchy change"); // NOI18N
        if (addedFoldCnt > 0) {
            FoldHierarchy hierarchy = (FoldHierarchy) evt.getSource();
            Fold collapsed = null;
            boolean wasExpanded = false;
            while ((collapsed = FoldUtilities.findCollapsedFold(hierarchy, caretOffset, caretOffset)) != null && collapsed.getStartOffset() < caretOffset &&
                    collapsed.getEndOffset() > caretOffset) {
                        hierarchy.expand(collapsed);
                        wasExpanded = true;
            }
            // prevent unneeded scrolling; the user may have scrolled out using mouse already
            // so scroll only if the added fold may affect Y axis. Actually it's unclear why
            // we should reveal the current position on fold events except when caret is positioned in now-collapsed fold
            scrollToView = wasExpanded;
        } else {
            int startOffset = Integer.MAX_VALUE;
            // Set the caret's offset to the end of just collapsed fold if necessary
            if (evt.getAffectedStartOffset() <= caretOffset && evt.getAffectedEndOffset() >= caretOffset) {
                for (int i = 0; i < evt.getFoldStateChangeCount(); i++) {
                    FoldStateChange change = evt.getFoldStateChange(i);
                    if (change.isCollapsedChanged()) {
                        Fold fold = change.getFold();
                        if (fold.isCollapsed() && fold.getStartOffset() <= caretOffset && fold.getEndOffset() >= caretOffset) {
                            if (fold.getStartOffset() < startOffset) {
                                startOffset = fold.getStartOffset();
                            }
                        }
                    }
                }
                if (startOffset != Integer.MAX_VALUE) {
                    ((BaseCaret)component.getCaret()).setDot(startOffset, false);
                }
            }
            scrollToView = false;
        }
        // Update caret's visual position
        // Post the caret update asynchronously since the fold hierarchy is updated before
        // the view hierarchy and the views so the dispatchUpdate() could be picking obsolete
        // view information.
        if (addedFoldCnt > 1 || scrollToView) {
            SwingUtilities.invokeLater(new Runnable() {
                public @Override void run() {
                    LOG.finest("Updating after fold hierarchy change"); // NOI18N
                    if (component == null) {
                        return;
                    }
                    ((BaseCaret)component.getCaret()).refresh(addedFoldCnt > 1 && !scrollToView);
                }
            });
        }
    }

    @MimeRegistration(mimeType = "", service = FoldHierarchyMonitor.class)
    public static class F implements FoldHierarchyMonitor {
        @Override
        public void foldsAttached(FoldHierarchy h) {
            FoldingEditorSupport supp = new FoldingEditorSupport(h, h.getComponent());
            // stick as client property to prevent GC:
            h.getComponent().putClientProperty(F.class, supp);
        }
        
        static {
            FoldViewFactory.register();
        }
    }
}
