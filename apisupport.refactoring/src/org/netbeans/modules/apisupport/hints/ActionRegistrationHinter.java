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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.hints;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

/**
 * #191236: {@code ActionRegistration} conversion.
 */
@ServiceProvider(service=Hinter.class)
public class ActionRegistrationHinter implements Hinter {

    public @Override void process(final Context ctx) throws Exception {
        final Object instanceCreate = ctx.instanceFile().getAttribute("literal:instanceCreate");
        if ("method:org.openide.awt.Actions.alwaysEnabled".equals(instanceCreate)) {
            ctx.addStandardHint(new Callable<Void>() {
                public @Override Void call() throws Exception {
                    if (!annotationsAvailable(ctx)) {
                        return null;
                    }
                    ctx.findAndModifyDeclaration(ctx.instanceFile().getAttribute("literal:delegate"), new RegisterAction(ctx));
                    return null;
                }
            });
        } else if ("method:org.openide.awt.Actions.callback".equals(instanceCreate) || "method:org.openide.awt.Actions.context".equals(instanceCreate)) {
            ctx.addHint(Severity.WARNING, ctx.standardDescription()/* XXX no fixes yet */);
        } else if (ctx.instanceFile().getPath().startsWith("Actions/")) {
            // Old-style eager action of some variety.
            ctx.addStandardHint(new Callable<Void>() {
                public @Override Void call() throws Exception {
                    if (!annotationsAvailable(ctx)) {
                        return null;
                    }
                    Object action;
                    if (instanceCreate != null) {
                        action = instanceCreate;
                    } else {
                        Object clazz = ctx.instanceFile().getAttribute("instanceClass");
                        if (clazz != null) {
                            action = "new:" + clazz;
                        } else {
                            action = "new:" + ctx.instanceFile().getName().replace('-', '.');
                        }
                    }
                    ctx.findAndModifyDeclaration(action, new RegisterAction(ctx) {
                        public @Override void run(WorkingCopy wc, Element declaration, ModifiersTree modifiers) throws Exception {
                            TypeMirror type;
                            if (declaration.getKind() == ElementKind.CLASS) {
                                type = ((TypeElement) declaration).asType();
                            } else {
                                type = ((ExecutableElement) declaration).getReturnType();
                            }
                            boolean ok = false;
                            for (Class<?> xface : new Class<?>[] {Presenter.Menu.class, Presenter.Toolbar.class, Presenter.Popup.class, ContextAwareAction.class}) {
                                TypeElement xfaceEl = wc.getElements().getTypeElement(xface.getCanonicalName());
                                if (xfaceEl != null && wc.getTypes().isAssignable(type, xfaceEl.asType())) {
                                    ok = true;
                                    break;
                                }
                            }
                            if (!ok) {
                                DialogDisplayer.getDefault().notify(new Message(NbBundle.getMessage(ActionRegistrationHinter.class, "ActionRegistrationHinter.not_presenter", type), NotifyDescriptor.WARNING_MESSAGE));
                                return;
                            }
                            super.run(wc, declaration, modifiers);
                        }
                    });
                    return null;
                }
            });
        }
    }

    private boolean annotationsAvailable(Context ctx) {
        if (ctx.canAccess("org.openide.awt.ActionReferences")) {
            return true;
        } else {
            DialogDisplayer.getDefault().notify(new Message(NbBundle.getMessage(ActionRegistrationHinter.class, "ActionRegistrationHinter.missing_org.openide.awt"), NotifyDescriptor.WARNING_MESSAGE));
            return false;
        }
    }

    private static class RegisterAction implements Context.ModifyDeclarationTask {

        private final Context ctx;

        RegisterAction(Context ctx) {
            this.ctx = ctx;
        }

        public @Override void run(WorkingCopy wc, Element declaration, ModifiersTree modifiers) throws Exception {
            Map<String,Object> params = new HashMap<String,Object>();
            params.put("category", ctx.instanceFile().getParent().getPath().substring("Actions/".length()));
            params.put("id", ctx.instanceFile().getName().replace('-', '.'));
            ModifiersTree nue = ctx.addAnnotation(wc, modifiers, "org.openide.awt.ActionID", params);
            params.clear();
            String displayName = ctx.bundlevalue(ctx.instanceFile().getAttribute("literal:displayName"), declaration);
            if (displayName == null) {
                // @ActionRegistration requires this attr, even though it is unused for eager actions.
                displayName = "#TODO";
            }
            params.put("displayName", displayName);
            params.put("iconBase", ctx.instanceFile().getAttribute("iconBase"));
            Boolean noIconInMenu = (Boolean) ctx.instanceFile().getAttribute("noIconInMenu");
            if (noIconInMenu != null) {
                params.put("iconInMenu", !noIconInMenu);
            }
            params.put("asynchronous", ctx.instanceFile().getAttribute("asynchronous"));
            nue = ctx.addAnnotation(wc, nue, "org.openide.awt.ActionRegistration", params);
            ctx.delete(ctx.instanceFile());
            TreeMaker make = wc.getTreeMaker();
            List<AnnotationTree> anns = new ArrayList<AnnotationTree>();
            for (FileObject shadow : NbCollections.iterable(ctx.instanceFile().getFileSystem().getRoot().getData(true))) {
                if (!shadow.hasExt("shadow")) {
                    continue;
                }
                if (ctx.instanceFile().getPath().equals(shadow.getAttribute("originalFile"))) {
                    List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
                    Integer pos = (Integer) shadow.getAttribute("position");
                    if (pos != null) {
                        arguments.add(make.Assignment(make.Identifier("position"), make.Literal(pos)));
                    }
                    String name = shadow.getName();
                    if (!name.equals(ctx.instanceFile().getName())) {
                        arguments.add(make.Assignment(make.Identifier("name"), make.Literal(name)));
                    }
                    arguments.add(make.Assignment(make.Identifier("path"), make.Literal(shadow.getParent().getPath())));
                    // XXX maybe look for nearby separators?
                    TypeElement ann = wc.getElements().getTypeElement("org.openide.awt.ActionReference");
                    if (ann == null) {
                        throw new IllegalArgumentException("Could not find ActionReference in classpath");
                    }
                    anns.add(make.Annotation(make.QualIdent(ann), arguments));
                    ctx.delete(shadow);
                }
            }
            if (anns.size() == 1) {
                nue = make.addModifiersAnnotation(nue, anns.get(0));
            } else if (!anns.isEmpty()) {
                TypeElement ann = wc.getElements().getTypeElement("org.openide.awt.ActionReferences");
                if (ann == null) {
                    throw new IllegalArgumentException("Could not find ActionReferences in classpath");
                }
                nue = make.addModifiersAnnotation(nue, make.Annotation(make.QualIdent(ann), Collections.singletonList(make.Assignment(make.Identifier("value"), make.NewArray(null, Collections.<ExpressionTree>emptyList(), anns)))));
            }
            wc.rewrite(modifiers, GeneratorUtilities.get(wc).importFQNs(nue));
        }

    }

}
