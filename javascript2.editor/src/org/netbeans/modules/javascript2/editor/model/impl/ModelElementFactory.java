/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.model.impl;

import com.oracle.nashorn.ir.FunctionNode;
import com.oracle.nashorn.ir.IdentNode;
import com.oracle.nashorn.ir.LiteralNode;
import com.oracle.nashorn.ir.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.model.Identifier;
import org.netbeans.modules.javascript2.editor.model.JsObject;

/**
 *
 * @author Petr Pisl
 */
class ModelElementFactory {

    static JsFunctionImpl create(FunctionNode functionNode, List<Identifier> fqName, ModelBuilder modelBuilder) {
        JsObjectImpl inObject = modelBuilder.getCurrentObject();
        JsObject parentObject = inObject;
        List<Identifier> parameters = new ArrayList(functionNode.getParameters().size());
        for(IdentNode node: functionNode.getParameters()) {
            parameters.add(create(node));
        }
        JsFunctionImpl result; 
        if (fqName.size() > 1) {
            JsObject globalObject = modelBuilder.getGlobal();
            List<Identifier> objectName = fqName.subList(0, fqName.size() - 1);
            parentObject = ModelUtils.getJsObject(globalObject, objectName);
            result = new JsFunctionImpl(parentObject, fqName.get(fqName.size() - 1), parameters, new OffsetRange(functionNode.getStart(), functionNode.getFinish()));
            if (!"prototype".equals(parentObject.getName())) {
                result.addModifier(Modifier.STATIC);
            } 
        } else {
            result = new JsFunctionImpl(inObject, fqName.get(fqName.size() - 1), parameters, new OffsetRange(functionNode.getStart(), functionNode.getFinish()));
        }
        parentObject.addProperty(result.getDeclarationName().getName(), result);
        return result;
    }
    
    static IdentifierImpl create(IdentNode node) {
        return new IdentifierImpl(node.getName(), new OffsetRange(node.getStart(), node.getFinish()));
    }
    
    static IdentifierImpl create(LiteralNode node) {
        return new IdentifierImpl(node.getString(), new OffsetRange(node.getStart(), node.getFinish()));
    }
    
    static JsObjectImpl create(ObjectNode objectNode, List<Identifier> fqName, ModelBuilder modelBuilder, boolean belongsToParent) {
        JsObjectImpl scope = modelBuilder.getCurrentObject();
        JsObject parent = scope;
        JsObject result = null;
        Identifier name = fqName.get(fqName.size() - 1);
        JsObjectImpl newObject;
        if (!belongsToParent) {
            JsObject globalObject = modelBuilder.getGlobal();
            List<Identifier> objectName = fqName.subList(0, fqName.size() - 1);
            parent = ModelUtils.getJsObject(globalObject, objectName);
        }
        result = parent.getPropery(name.getName());
        newObject = new JsObjectImpl(parent, name, new OffsetRange(objectNode.getStart(), objectNode.getFinish()));
        newObject.setDeclared(true);
        if (result != null) {
            // the object already exist due a definition of a property => needs to be copied
            for (String propertyName : result.getProperties().keySet()) {
                newObject.addProperty(propertyName, result.getPropery(propertyName));
            }
        }
        parent.addProperty(name.getName(), newObject);
        return (JsObjectImpl)newObject;
    }
    
}
