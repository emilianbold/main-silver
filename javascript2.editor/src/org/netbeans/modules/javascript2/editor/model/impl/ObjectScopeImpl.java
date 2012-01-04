/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.model.impl;

import com.oracle.nashorn.ir.ObjectNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.model.*;

/**
 *
 * @author Petr Pisl
 */
public class ObjectScopeImpl extends ScopeImpl implements ObjectScope {
    
    private boolean isLogical;
    public final List<Identifier> fullName;
    
    public ObjectScopeImpl(Scope inScope, ObjectNode node, List<Identifier> fqName) {
        super(inScope, JsElement.Kind.OBJECT, inScope.getFileObject(), 
                fqName.get(fqName.size() - 1).getName(), 
                new OffsetRange(node.getStart(), node.getFinish()),
                // TODO bug in parser. The end position is not returned correctly now
//                Token.descPosition(node.getLastToken()) + Token.descLength(node.getLastToken())), 
                EnumSet.of(Modifier.PUBLIC));
        ((ScopeImpl)inScope).addElement(this);
        this.isLogical = false;
        this.fullName = fqName;
    }
    
    public ObjectScopeImpl(Scope inScope, List<Identifier> fqName, OffsetRange range) {
        super(inScope, JsElement.Kind.OBJECT, inScope.getFileObject(), fqName.get(fqName.size() - 1).getName(), range,
                EnumSet.of(Modifier.PUBLIC));
        ((ScopeImpl)inScope).addElement(this);
        this.isLogical = true;
        this.fullName = fqName;
    }
    
    @Override
    public Collection<? extends ModelElement> getDeclaredProperties() {
        return null;
    }

    @Override
    public boolean isLogical() {
        return isLogical;
    }

    @Override
    public List<Identifier> getFQDeclarationName() {
        return fullName;
    }

    @Override
    public Collection<? extends FunctionScope> getMethods() {
        List<FunctionScope> result = new ArrayList<FunctionScope>();
        
        Collection<? extends FunctionScope> constructors = filter(getElements(), new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getJSKind().equals(JsElement.Kind.CONSTRUCTOR);
            }
        });
        if (constructors.size() > 0) {
            FunctionScope constructor = ModelUtils.getFirst(constructors);
            Collection<? extends FunctionScope> inConstructor = ModelUtils.getMethods(constructor);
            result.addAll(inConstructor);
        }
        
        result.addAll(ModelUtils.getMethods(this));
        return result;
    }
    
}
