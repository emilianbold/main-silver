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
package org.netbeans.modules.css.visual.editors;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.TreeSet;
import org.netbeans.modules.css.lib.api.properties.FixedTextGrammarElement;
import org.netbeans.modules.css.lib.api.properties.UnitGrammarElement;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author marekfukala
 */
public class PropertyValuesEditor extends PropertyEditorSupport implements ExPropertyEditor {
    
    private Collection<UnitGrammarElement> unitElements;
    private Collection<FixedTextGrammarElement> fixedElements;
    
    private String[] tags;

    public PropertyValuesEditor(Collection<FixedTextGrammarElement> fixedElements, Collection<UnitGrammarElement> unitElements) {
        this.fixedElements = fixedElements;
        this.unitElements = unitElements;
    }

    @Override
    public synchronized String[] getTags() {
        if(tags == null) {
            Collection<String> fixedElementNames = new TreeSet<String>();
            for(FixedTextGrammarElement element: fixedElements) {
                String value = element.getValue();
                if(Character.isLetter(value.charAt(0))) { //filter operators & similar
                    fixedElementNames.add(value);
                }
            }
            
            tags = fixedElementNames.toArray(new String[0]);
        }

        return tags;
    }

    @Override
    public void setAsText(String str) {
        if(str == null) {
            return ;
        }
        
        //first match fixed elements
        for(FixedTextGrammarElement element : fixedElements) {
            if(element.accepts(str)) {
                setValue(str);
                return ;
            }
        }
        //then units
        for(UnitGrammarElement element : unitElements) {
            if(element.accepts(str)) {
                setValue(str);
                return ;
            }
        }
        
        //report error
        throw new IllegalArgumentException(str);
    }

    @Override
    public String getAsText() {
        return getValue().toString();
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        //if there's at least one unit element, then the text field needs to be editable
        env.getFeatureDescriptor().setValue("canEditAsText", Boolean.TRUE);
    }

}
