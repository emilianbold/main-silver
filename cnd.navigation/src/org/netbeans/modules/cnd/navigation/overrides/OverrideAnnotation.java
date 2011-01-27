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

package org.netbeans.modules.cnd.navigation.overrides;

import java.util.Collection;
import java.util.MissingResourceException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Kvashin
 */
/*package*/ class OverrideAnnotation extends BaseAnnotation {

    public OverrideAnnotation(StyledDocument document, CsmFunction decl, 
            Collection<? extends CsmOffsetableDeclaration> baseDecls, 
            Collection<? extends CsmOffsetableDeclaration> descDecls,
            Collection<? extends CsmOffsetableDeclaration> baseTemplates, 
            Collection<? extends CsmOffsetableDeclaration> templateSpecializations) {
        super(document, decl, baseDecls, descDecls, baseTemplates, templateSpecializations);
    }

    @Override
    public String getShortDescription() {
        String out = "";
        if (baseUIDs.isEmpty() && !descUIDs.isEmpty()) {
            out = NbBundle.getMessage(getClass(), "LAB_IsOverriden");
        } else if (!baseUIDs.isEmpty() && descUIDs.isEmpty()) {
            CharSequence text = "..."; //NOI18N
            if (baseUIDs.size() == 1) {
                CsmOffsetableDeclaration obj = baseUIDs.iterator().next().getObject();
                if (obj != null) {
                    text = obj.getQualifiedName();
                }
            }
            out = NbBundle.getMessage(getClass(), "LAB_Overrides", text);
        } else if (!baseUIDs.isEmpty() && !descUIDs.isEmpty()) {
            out = NbBundle.getMessage(getClass(), "LAB_OverridesAndIsOverriden");
        } else if (baseTemplateUIDs.isEmpty() && specializationUIDs.isEmpty()) { //both are empty
            throw new IllegalArgumentException("Either overrides or overridden should be non empty"); //NOI18N
        }
        out = addTemplateAnnotation(out);
        return out;
    }

    @Override
    protected CharSequence debugTypeString() {
        switch (type) {
            case OVERRIDES:
                return "OVERRIDES"; // NOI18N
            case IS_OVERRIDDEN:
                return "OVERRIDDEN"; // NOI18N
            case SPECIALIZES:
                return "SPECIALIZES"; // NOI18N
            case IS_SPECIALIZED:
                return "IS_SPECIALIZED"; // NOI18N
            case OVERRIDEN_COMBINED:
                return "OVERRIDES_AND_OVERRIDDEN"; // NOI18N
            case TEMPLATE_COMBINED:
                return "SPECIALIZES_AND_SPECIALIZED_FUNCTION"; // NOI18N
            case COMBINED:
                return "OVERRIDE_TEMPLATE_COMBINED_FUNCTION"; // NOI18N
            default:
                return "???"; // NOI18N
        }
    }
}
