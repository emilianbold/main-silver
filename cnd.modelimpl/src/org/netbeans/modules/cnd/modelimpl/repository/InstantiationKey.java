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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;

/**
 * A key for CsmInstantiation
 * 
 * @author Nikolay Krasilnikov (nnnnnk@netbeans.org)
 */
/*package*/ final class InstantiationKey extends OffsetableKey {
    
    InstantiationKey(CsmInstantiation inst) {
        super(inst.getTemplateDeclaration(), NameCache.getManager().getString(getName(inst))); // NOI18N
    }
    
    private static CharSequence getName(CsmInstantiation inst) {
        StringBuilder sb = new StringBuilder(inst.getTemplateDeclaration().getName());
        sb.append("<"); // NOI18N
        Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = inst.getMapping();
        boolean first = true;
        for (Map.Entry<CsmTemplateParameter, CsmSpecializationParameter> param : mapping.entrySet()) {
            CsmSpecializationParameter specParam = param.getValue();
            if(CsmKindUtilities.isTypeBasedSpecalizationParameter(specParam)) {
                if(!first) {
                    sb.append(","); // NOI18N
                }
                CsmType type = ((CsmTypeBasedSpecializationParameter)specParam).getType();
                sb.append(type.getCanonicalText());
                first = false;
            }
        }
        sb.append(">"); // NOI18N
        return sb;
    }

    /*package*/ InstantiationKey(RepositoryDataInput aStream) throws IOException {
        super(aStream);
    }

    InstantiationKey(KeyDataPresentation presentation) {
        super(presentation);
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return CsmObjectFactory.instance();
    }
    
    @Override
    char getKind() {
        return Utils.getCsmInstantiationKindKey();
    }

    @Override
    public short getHandler() {
        return KeyObjectFactory.KEY_INSTANTIATION_KEY;
    }

    @Override
    public String toString() {
        String retValue;

        retValue = "InstantiationKey: " + super.toString(); // NOI18N
        return retValue;
    }

    @Override
    public int getSecondaryDepth() {
        return super.getSecondaryDepth() + 1;
    }

    @Override
    public int getSecondaryAt(int level) {
        if (level == 0) {
            return getHandler();
        } else {
            return super.getSecondaryAt(level - 1);
        }
    }
}
