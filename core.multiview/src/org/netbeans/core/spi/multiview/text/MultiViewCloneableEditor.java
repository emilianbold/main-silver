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

package org.netbeans.core.spi.multiview.text;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.text.Document;
import org.netbeans.api.actions.Savable;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * @author  mkleint
 */
class MultiViewCloneableEditor extends CloneableEditor  implements MultiViewElement {
    private static final long serialVersionUID =-3126744316644172415L;

    private transient MultiViewElementCallback multiViewObserver;
    private transient JToolBar bar;
    
    public MultiViewCloneableEditor() {
        super();
    }
    
    public MultiViewCloneableEditor(CloneableEditorSupport support) {
        super(support, true);
        initializeBySupport();
    }
    
    @Override
    public JComponent getToolbarRepresentation() {
        Document doc = getEditorPane().getDocument();
        if (doc instanceof NbDocument.CustomToolbar) {
            if (bar == null) {
                bar = ((NbDocument.CustomToolbar)doc).createToolbar(getEditorPane());
            }
        }
        if (bar == null) {
            bar = new JToolBar();
        }
        return bar;
    }
    
    @Override
    public javax.swing.JComponent getVisualRepresentation() {
        return this;
    }
    
    @Override
    public final void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewObserver = callback;
    }
    
    protected final MultiViewElementCallback getElementObserver() {
        return multiViewObserver;
    }
    
    @Override
    public void componentActivated() {
        super.componentActivated();
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
    }
    
    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }
    
    @Override
    public void componentHidden() {
        super.componentHidden();
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
    }
    
    @Override
    public void componentShowing() {
        if (multiViewObserver != null) {
            updateName();
        }
        super.componentShowing();
    }
    
    @Override
    public javax.swing.Action[] getActions() {
        return super.getActions();
    }
    
    @Override
    public org.openide.util.Lookup getLookup() {
        return super.getLookup();
    }
    
    @Override
    public String preferredID() {
        return super.preferredID();
    }
    
    
    @Override
    public void requestVisible() {
        if (multiViewObserver != null) {
            multiViewObserver.requestVisible();
        } else {
            super.requestVisible();
        }
    }
    
    @Override
    public void requestActive() {
        if (multiViewObserver != null) {
            multiViewObserver.requestActive();
        } else {
            super.requestActive();
        }
    }
    
    
    @Override
    public void updateName() {
        super.updateName();
        if (multiViewObserver != null) {
            TopComponent tc = multiViewObserver.getTopComponent();
            tc.setHtmlDisplayName(getHtmlDisplayName());
            tc.setDisplayName(getDisplayName());
            tc.setName(getName());
            tc.setToolTipText(getToolTipText());
        }
    }
    
    @Override
    public void open() {
        if (multiViewObserver != null) {
            multiViewObserver.requestVisible();
        } else {
            super.open();
        }
        
    }

    @Override
    protected boolean closeLast() {
        return super.closeLast(false);
    }

    @Messages({
        "MSG_SaveModified=File {0} is modified. Save?"
    })
    @Override
    public CloseOperationState canCloseElement() {
        final CloneableEditorSupport sup = getLookup().lookup(CloneableEditorSupport.class);
        Enumeration en = getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements()) {
                // at least two is OK
                return CloseOperationState.STATE_OK;
            }
        }
        
        Savable sav = getLookup().lookup(Savable.class);
        if (sav != null) {
            AbstractAction save = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        sup.saveDocument();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
            save.putValue(Action.LONG_DESCRIPTION, Bundle.MSG_SaveModified(sav));
            return MultiViewFactory.createUnsafeCloseState("editor", save, null);
        } 
        return CloseOperationState.STATE_OK;
    }
    
}
