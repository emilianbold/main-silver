/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.zend.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.TextAction;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.zend.ZendPhpFrameworkProvider;
import org.netbeans.modules.php.zend.util.ZendUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @see BaseAction
 * @author Tomas Mysik
 */
public final class GoToActionOrViewAction extends TextAction implements ContextAwareAction {
    private static final long serialVersionUID = -448767564313335474L;

    static final RequestProcessor RP = new RequestProcessor(GoToActionOrViewAction.class.getName());

    private static final GoToActionOrViewAction INSTANCE = new GoToActionOrViewAction();
    private static final int DEFAULT_OFFSET = 0;

    private GoToActionOrViewAction() {
        super(getFullName());
        // copied from BaseAction
        putValue("noIconInMenu", true); // NOI18N
        putValue(NAME, getFullName());
        putValue("menuText", getPureName()); // NOI18N
    }

    public static GoToActionOrViewAction getInstance() {
        return INSTANCE;
    }

    private static String getFullName() {
        return NbBundle.getMessage(GoToActionOrViewAction.class, "LBL_ZendAction", getPureName());
    }

    private static String getPureName() {
        return NbBundle.getMessage(GoToActionOrViewAction.class, "LBL_GoToActionOrView");
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        FileObject fo = FileUtils.getFileObject(actionContext);
        return getGoToAction(fo, getOffset(actionContext));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FileObject fo = NbEditorUtilities.getFileObject(getTextComponent(e).getDocument());
        Action action = getGoToAction(fo, getTextComponent(e).getCaretPosition());
        if (action != null) {
            action.actionPerformed(e);
        }
    }

    private Action getGoToAction(FileObject fo, int offset) {
        if (!isValid(fo)) {
            return null;
        }

        if (ZendUtils.isViewWithAction(fo)) {
            return new GoToActionAction(fo);
        } else if (ZendUtils.isAction(fo)) {
            return new GoToViewAction(fo, offset);
        }
        return null;
    }

    private boolean isValid(FileObject fo) {
        if (fo == null) {
            return false;
        }
        PhpModule phpModule = PhpModule.forFileObject(fo);
        if (phpModule == null) {
            return false;
        }
        return ZendPhpFrameworkProvider.getInstance().isInPhpModule(phpModule);
    }

    private int getOffset(Lookup context) {
        EditorCookie editorCookie = context.lookup(EditorCookie.class);
        if (editorCookie != null) {
            return getOffset(editorCookie);
        }
        FileObject fo = FileUtils.getFileObject(context);
        if (fo == null) {
            return DEFAULT_OFFSET;
        }
        try {
            editorCookie = DataObject.find(fo).getLookup().lookup(EditorCookie.class);
            return getOffset(editorCookie);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return DEFAULT_OFFSET;
    }

    private int getOffset(EditorCookie editorCookie) {
        if (editorCookie == null) {
            return DEFAULT_OFFSET;
        }
        JEditorPane[] openedPanes = editorCookie.getOpenedPanes();
        if (openedPanes == null || openedPanes.length == 0) {
            return DEFAULT_OFFSET;
        }
        return openedPanes[0].getCaretPosition();
    }

    static final class GoToActionAction extends AbstractAction {
        private static final long serialVersionUID = -95284897533404L;

        private final FileObject fo;

        public GoToActionAction(FileObject fo) {
            super(NbBundle.getMessage(GoToActionOrViewAction.class, "LBL_GoToAction"));
            assert ZendUtils.isViewWithAction(fo);
            this.fo = fo;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RP.execute(new Runnable() {
                @Override
                public void run() {
                    FileObject action = ZendUtils.getAction(fo);
                    if (action != null) {
                        UiUtils.open(action, getActionMethodOffset(action));
                    }
                }
            });
        }

        private int getActionMethodOffset(FileObject action) {
            String actionMethodName = ZendUtils.getActionName(fo);
            EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
            for (PhpClass phpClass : editorSupport.getClasses(action)) {
                if (actionMethodName != null) {
                    for (PhpClass.Method method : phpClass.getMethods()) {
                        if (actionMethodName.equals(method.getName())) {
                            return method.getOffset();
                        }
                    }
                }
                return phpClass.getOffset();
            }
            return DEFAULT_OFFSET;
        }
    }

    private static final class GoToViewAction extends AbstractAction {
        private static final long serialVersionUID = -95313458753404L;

        private final FileObject fo;
        private final int offset;

        public GoToViewAction(FileObject fo, int offset) {
            super(NbBundle.getMessage(GoToActionOrViewAction.class, "LBL_GoToView"));
            assert ZendUtils.isAction(fo);
            this.fo = fo;
            this.offset = offset;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RP.execute(new Runnable() {
                @Override
                public void run() {
                    EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
                    PhpBaseElement phpElement = editorSupport.getElement(fo, offset);
                    if (phpElement == null) {
                        return;
                    }
                    FileObject view = ZendUtils.getView(fo, phpElement);
                    if (view != null) {
                        UiUtils.open(view, DEFAULT_OFFSET);
                    }
                }
            });
        }
    }
}
