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
package org.netbeans.modules.css.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.properties.model.SemanticModel;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.PlainElement;
import org.netbeans.modules.css.model.impl.semantic.DeclarationsBoxEdgeSizeModel;
import org.netbeans.modules.css.model.impl.semantic.DeclarationsMarginModel;
import org.netbeans.modules.css.model.impl.semantic.DeclarationsPaddingModel;

/**
 *
 * @author marekfukala
 */
public class DeclarationsI extends ModelElement implements Declarations {

    private List<Declaration> declarations = new ArrayList<Declaration>();
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(Declaration declaration) {
            declarations.add(declaration);
        }
    };

    public DeclarationsI(Model model) {
        super(model);
    }

    public DeclarationsI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    public Collection<? extends SemanticModel> getSemanticModels() {
        if(isValid()) {
            Collection<SemanticModel> models = new ArrayList<SemanticModel>();
            models.add(new DeclarationsMarginModel(model, this));
            models.add(new DeclarationsPaddingModel(model, this));
            return models;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Declaration> getDeclarations() {
        return declarations;
    }

    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    public void addDeclaration(Declaration declaration) {
//        addTextElement("\n");
        addElement(declaration);
        addTextElement(";\n");
    }

    @Override
    public void removeDeclaration(Declaration declaration) {
        int index = getElementIndex(declaration);
        if (index == -1) {
            return;
        }
        removeElement(index); //remove the declaration

        //update the whitespaces in the preceding plain element
        PlainElement before = getElementAt(index - 1, PlainElement.class);
        if (before != null) {
            //remove all whitespace after last endline
            wipeWhitespaces(before, false);
        }

        //update the whitespaces after the declaration element
        PlainElement after = getElementAt(index, PlainElement.class);//the indexes shifted by the removal!
        if(after != null) {
            String afterTrimmed = after.getContent().toString().trim();
            if(";".equals(afterTrimmed)) {
                //semicolon - remove
                removeElement(index);
                PlainElement afterafter = getElementAt(index, PlainElement.class);//the indexes shifted by the removal!
                if(afterafter != null) {
                    //whitespace
                    //remove all whitespace after last endline
                    wipeWhitespaces(afterafter, true);
                }
            } else {
                //remove all whitespace after last endline
                wipeWhitespaces(after, true);
            }
        }
        
    }

    @Override
    protected Class getModelClass() {
        return Declarations.class;
    }
}
