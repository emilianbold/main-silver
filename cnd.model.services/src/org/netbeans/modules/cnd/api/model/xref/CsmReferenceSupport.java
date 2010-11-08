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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.api.model.xref;

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.xref.impl.ReferenceSupportImpl;
import org.openide.util.CharSequences;

/**
 * some help methods to support CsmReference objects
 * @author Vladimir Voskresensky
 */
public final class CsmReferenceSupport {
    
    private static final ReferenceSupportImpl impl = new ReferenceSupportImpl();
    private CsmReferenceSupport() {
        
    }
    
    public static CsmReference createObjectReference(CsmOffsetable obj) {
        return impl.createObjectReference(obj);
    }

    public static CsmReference createObjectReference(CsmObject target, CsmOffsetable owner) {
        return impl.createObjectReference(target, owner);
    }
    
    public static CharSequence getContextLineHtml(CsmReference ref, boolean refNameInBold) {
        CsmFile csmFile = ref.getContainingFile();
        int stToken = ref.getStartOffset();
        int endToken = ref.getEndOffset();
        CharSequence out = CsmDisplayUtilities.getContextLineHtml(csmFile, stToken, endToken, refNameInBold);
        if (out == null) {
            out = ref.getText();
        }
        return out;
    }

    public static CharSequence getContextLine(CsmReference ref) {
        CsmFile csmFile = ref.getContainingFile();
        int stToken = ref.getStartOffset();
        int endToken = ref.getEndOffset();
        CharSequence out = CsmDisplayUtilities.getContextLine(csmFile, stToken, endToken);
        if (out == null) {
            out = ref.getText();
        }
        return out;
    }
    
    public static boolean sameDeclaration(CsmObject checkDecl, CsmObject targetDecl) {
        if (checkDecl.equals(targetDecl)) {
            return true;
        } else if (CsmKindUtilities.isConstructor(checkDecl)) {
            return false;
        } else if (CsmKindUtilities.isQualified(checkDecl) && CsmKindUtilities.isQualified(targetDecl)) {
            CharSequence fqnCheck = ((CsmQualifiedNamedElement) checkDecl).getQualifiedName();
            CharSequence fqnTarget = ((CsmQualifiedNamedElement) targetDecl).getQualifiedName();
            if (fqnCheck.equals(fqnTarget)) {
                if (CsmKindUtilities.isFunction(checkDecl) && CsmKindUtilities.isFunction(targetDecl)) {
                    // we treat const and non-const functions as the same
                    fqnCheck = ((CsmFunction) checkDecl).getSignature().toString().replace("const", "").trim();
                    fqnTarget = ((CsmFunction) targetDecl).getSignature().toString().replace("const", "").trim();
                    if (fqnCheck.equals(fqnTarget)) {
                        return true;
                    }
                } else {
                    return true;
                }
            }            
        }
        return false;
    }
}
