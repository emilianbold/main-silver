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

import java.util.List;
import org.netbeans.modules.javascript2.editor.model.DeclarationScope;
import org.netbeans.modules.javascript2.editor.model.Identifier;
import org.netbeans.modules.javascript2.editor.model.JsElement;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.Model;

/**
 *
 * @author Petr Pisl
 */
public class ModelUtils {
      
    public static JsObjectImpl getJsObject (ModelBuilder builder, List<Identifier> fqName) {
        JsObject result = builder.getCurrentObject();
        JsObject tmpObject = null;
        String firstName = fqName.get(0).getName();
        
        while (tmpObject == null && result.getParent() != null) {
            if (result.getProperty(firstName) != null) {
                tmpObject = result;
            }
            result = result.getParent();
        }
        if (tmpObject == null) {
            tmpObject = builder.getGlobal();
        }
        for (Identifier name : fqName) {
            result = tmpObject.getProperty(name.getName());
            if (result == null) {
                result = new JsObjectImpl(tmpObject, name, name.getOffsetRange());
                tmpObject.addProperty(name.getName(), result);
            }
            tmpObject = result;
        }
        return (JsObjectImpl)result;        
    }

    public static boolean isGlobal(JsObject object) {
        return object.getJSKind() == JsElement.Kind.FILE;
    }
    
    public static JsObject findJsObject(Model model, int offset) {
        JsObject result = null;
        JsObject global = model.getGlobalObject();
        result = findJsObject(global, offset);
        if (result == null) {
            result = global;
        }
        return result;
    }
    
    public static JsObject findJsObject(JsObject object, int offset) {
        JsObjectImpl jsObject = (JsObjectImpl)object;
        JsObject result = null;
        JsObject tmpObject = null;
        if (jsObject.getOffsetRange(null).containsInclusive(offset)) {
            result = jsObject;
            for (JsObject property : jsObject.getProperties().values()) {
                JsElement.Kind kind = property.getJSKind();
                if (kind == JsElement.Kind.OBJECT 
                        || kind == JsElement.Kind.FUNCTION || kind == JsElement.Kind.METHOD || kind == JsElement.Kind.CONSTRUCTOR)
                tmpObject = findJsObject(property, offset);
                if (tmpObject != null) {
                    result = tmpObject;
                    break;
                }
            }
        }
        return result;
    }
    
    public static DeclarationScope getDeclarationScope(Model model, int offset) {
        DeclarationScope result = null;
        JsObject global = model.getGlobalObject();
        result = getDeclarationScope((DeclarationScope)global, offset);
        if (result == null) {
            result = (DeclarationScope)global;
        }
        return result;
    }
    
    private static DeclarationScope getDeclarationScope(DeclarationScope scope, int offset) {
        DeclarationScopeImpl dScope = (DeclarationScopeImpl)scope;
        DeclarationScope result = null;
        DeclarationScope function = null;
        if (dScope.getOffsetRange(null).containsInclusive(offset)) {
            result = dScope;
            for (DeclarationScope innerScope : dScope.getDeclarationsScope()) {
                function = getDeclarationScope(innerScope, offset);
                if (function != null) {
                    result = function;
                    break;
                }
            }
        }
        return result;
    }
    
    public static String createFQN(JsObject object) {
        StringBuilder result = new StringBuilder();
        result.append(object.getName());
        JsObject parent = object;
        while((parent = parent.getParent()).getJSKind() != JsElement.Kind.FILE) {
            result.insert(0, ".");
            result.insert(0, parent.getName());
        }
        return result.toString();
    }
}
