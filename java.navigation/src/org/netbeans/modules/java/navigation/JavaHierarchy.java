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

package org.netbeans.modules.java.navigation;

import java.util.logging.Level;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.JDialog;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.*;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.awt.StatusDisplayer;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
public final class JavaHierarchy {
    
    private static final Logger LOG = Logger.getLogger(JavaHierarchy.class.getName());

    /**
     * Show the hierarchy of the types in the fileObject.
     * 
     * @param fileObject 
     */
    public static void show(final FileObject fileObject) {
        if (fileObject != null) {
            final JavaSource javaSource = JavaSource.forFileObject(fileObject);
            if (javaSource != null) {
                final AtomicReference<ElementHandle[]> handles = new AtomicReference<ElementHandle[]>();
                final AtomicBoolean cancel = new AtomicBoolean();
                ProgressUtils.runOffEventDispatchThread(
                        new Runnable() {
                            public void run() {
                                try {
                                    javaSource.runUserActionTask(new Task<CompilationController>() {
                                        @Override
                                        public void run(
                                            CompilationController compilationController)
                                            throws Exception {
                                            compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                                            final List<? extends TypeElement> topLevels = compilationController.getTopLevelElements();
                                            final Element[] elements = topLevels.toArray(new Element[topLevels.size()]);
                                            handles.set(getHandles(elements));
                                        }
                                    }, true);
                                } catch (IOException ioe) {
                                    Exceptions.printStackTrace(ioe);
                                }
                            }
                        },
                        NbBundle.getMessage(JavaHierarchy.class, "TITLE_Hierarchy", fileObject.getName()),
                        cancel,
                        false);
                if (!cancel.get() && handles.get() != null) {
                    show(fileObject, handles.get());
                }
            }
        }
    }

    public static void show(
            final FileObject fileObject,
            final ElementHandle[] elements) {
        LOG.log(Level.FINE, "Showing hierarchy for: {0}", fileObject == null ? null : FileUtil.getFileDisplayName(fileObject));   //NOI18N
        if (fileObject != null) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaHierarchy.class, "LBL_WaitNode"));
            JDialog dialog = ResizablePopup.getDialog(fileObject);
            final StringBuilder membersOf = new StringBuilder();
            if (elements != null && elements.length > 0) {
                final int start;
                if (elements[0].getKind() == ElementKind.PACKAGE && elements.length > 1) {
                    start = 1;
                } else {
                    start = 0;
                }
                boolean first = true;
                for (int i=start; i< elements.length; i++) {
                    if (!first) {
                        membersOf.append(", "); //NOI18N
                    } else {
                        first = false;
                    }
                    membersOf.append(SourceUtils.getJVMSignature(elements[i])[0]);
                }
            }
            String title = NbBundle.getMessage(JavaHierarchy.class, "TITLE_Hierarchy", membersOf);
            dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavaHierarchy.class, "ACSD_JavaHierarchyDialog", membersOf));
            dialog.setTitle(title); // NOI18N
            dialog.setContentPane(new JavaHierarchyPanel(fileObject, elements));
            dialog.setVisible(true);
            LOG.log(Level.FINE, "Opened hierarchy for: {0}", FileUtil.getFileDisplayName(fileObject));  //NOI18N
        }
    }



    @CheckForNull
    public static final ElementHandle[] getHandles(@NullAllowed final Element[] elements) {
        if (elements == null) {
            return null;
        }
        final ElementHandle[] res = new ElementHandle[elements.length];
        for (int i = 0; i< elements.length; i++) {
            res[i] = ElementHandle.create(elements[i]);
        }
        return res;
    }
}
