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
package org.netbeans.modules.cnd.refactoring.codegen;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.codegen.ui.ConstructorPanel;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.GeneratorUtils;
import org.netbeans.modules.cnd.utils.ui.UIGesturesSupport;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ConstructorGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CsmContext path = context.lookup(CsmContext.class);
            if (component == null || path == null) {
                return ret;
            }
            CsmClass typeElement = path.getEnclosingClass();
            if (typeElement == null) {
                return ret;
            }
            CsmObject objectUnderOffset = path.getObjectUnderOffset();
            final Set<CsmField> shouldBeInitializedFields = new LinkedHashSet<>();
            final Set<CsmField> mayBeIninitializedFields = new LinkedHashSet<>();
            final Set<CsmField> cannotBeInitializedFields = new LinkedHashSet<>();
            final List<CsmConstructor> constructors = new ArrayList<>();
            final Map<CsmClass,List<CsmConstructor>> inheritedConstructors = new HashMap<>();
            // check base class
            for (CsmInheritance csmInheritance : typeElement.getBaseClasses()) {
                CsmClass baseClass = CsmInheritanceUtilities.getCsmClass(csmInheritance);
                if (baseClass != null) {
                    List<CsmConstructor> list = new ArrayList<>();
                    for (CsmMember member : baseClass.getMembers()) {
                        if (CsmKindUtilities.isConstructor(member) && CsmInheritanceUtilities.matchVisibility(member, csmInheritance.getVisibility())) {
                            list.add((CsmConstructor)member);
                        }
                    }
                    if (!list.isEmpty()) {
                        inheritedConstructors.put(baseClass, list);
                    }
                }
            }
            GeneratorUtils.scanForFieldsAndConstructors(typeElement, shouldBeInitializedFields, mayBeIninitializedFields, cannotBeInitializedFields, constructors);
            ElementNode.Description constructorDescription = null;
            if (!inheritedConstructors.isEmpty()) {
                List<ElementNode.Description> baseClassesDescriptions = new ArrayList<>();
                for (Map.Entry<CsmClass,List<CsmConstructor>> entry : inheritedConstructors.entrySet()) {
                    List<ElementNode.Description> constructorDescriptions = new ArrayList<>();
                    for(CsmConstructor c : entry.getValue()) {
                        constructorDescriptions.add(ElementNode.Description.create(c, null, true, false));
                    }
                    baseClassesDescriptions.add(ElementNode.Description.create(entry.getKey(), constructorDescriptions, false, false));
                }
                constructorDescription = ElementNode.Description.create(typeElement, baseClassesDescriptions, false, false);
            }
            ElementNode.Description fieldsDescription = null;
            if (!mayBeIninitializedFields.isEmpty() || !shouldBeInitializedFields.isEmpty() || !cannotBeInitializedFields.isEmpty()) {
                List<ElementNode.Description> fieldDescriptions = new ArrayList<>();
                for (CsmField variableElement : mayBeIninitializedFields) {
                    fieldDescriptions.add(ElementNode.Description.create(variableElement, null, true, variableElement.equals(objectUnderOffset)));
                }
                for (CsmField variableElement : shouldBeInitializedFields) {
                    fieldDescriptions.add(ElementNode.Description.create(variableElement, null, true, true));
                }
                for (CsmField variableElement : cannotBeInitializedFields) {
                    fieldDescriptions.add(ElementNode.Description.create(variableElement, null, false, false));
                }
                fieldsDescription = ElementNode.Description.create(typeElement, Collections.singletonList(ElementNode.Description.create(typeElement, fieldDescriptions, false, false)), false, false);
            }
            if (constructorDescription != null || fieldsDescription != null) {
                ret.add(new ConstructorGenerator(component, path, typeElement, constructorDescription, fieldsDescription));
            }
            return ret;
        }
    }
    private final JTextComponent component;
    private final ElementNode.Description constructorDescription;
    private final ElementNode.Description fieldsDescription;
    private final CsmContext contextPath;
    private final CsmClass type;

    /** Creates a new instance of ConstructorGenerator */
    private ConstructorGenerator(JTextComponent component, CsmContext path, CsmClass type, ElementNode.Description constructorDescription, ElementNode.Description fieldsDescription) {
        this.component = component;
        this.constructorDescription = constructorDescription;
        this.fieldsDescription = fieldsDescription;
        this.contextPath = path;
        this.type = type;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ConstructorGenerator.class, "LBL_constructor"); //NOI18N
    }

    @Override
    public void invoke() {
        UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, CsmRefactoringUtils.GENERATE_TRACKING, "CONSTRUCTOR"); // NOI18N
        if (constructorDescription != null || fieldsDescription != null) {
            final ConstructorPanel panel = new ConstructorPanel(constructorDescription, fieldsDescription);
            DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_constructor")); //NOI18N
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
            try {
                dialog.setVisible(true);
            } catch (Throwable th) {
                if (!(th.getCause() instanceof InterruptedException)) {
                    throw new RuntimeException(th);
                }
                dialogDescriptor.setValue(DialogDescriptor.CANCEL_OPTION);
            } finally {
                dialog.dispose();
            }
            if (dialogDescriptor.getValue() != dialogDescriptor.getDefaultValue()) {
                return;
            }
            GeneratorUtils.generateConstructor(contextPath,  type, panel.getInheritedConstructors(), panel.getVariablesToInitialize());
        }
    }
}
