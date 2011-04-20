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
package org.netbeans.modules.web.beans.analysis.analizer.method;

import java.util.List;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.analysis.CdiEditorAnalysisFactory;
import org.netbeans.modules.web.beans.analysis.analizer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analizer.MethodElementAnalyzer.MethodAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.field.DelegateFieldAnalizer;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class DelegateMethodAnalyzer implements MethodAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.MethodElementAnalyzer.MethodAnalyzer#analyze(javax.lang.model.element.ExecutableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, org.netbeans.api.java.source.CompilationInfo, java.util.List)
     */
    @Override
    public void analyze( ExecutableElement element, TypeMirror returnType,
            TypeElement parent, CompilationInfo compInfo,
            List<ErrorDescription> descriptions )
    {
        List<? extends VariableElement> parameters = element.getParameters();
        for (VariableElement param : parameters) {
            if( AnnotationUtil.hasAnnotation(param, AnnotationUtil.DELEGATE_FQN, 
                    compInfo))
            {
                checkMethodDefinition(element , param, compInfo , descriptions );
                checkClassDefinition( parent , element, param, compInfo , descriptions );
            }
        }
    }

    private void checkClassDefinition( TypeElement parent,
            ExecutableElement element, VariableElement param,
            CompilationInfo compInfo, List<ErrorDescription> descriptions )
    {
        if ( !AnnotationUtil.hasAnnotation(parent, AnnotationUtil.DECORATOR, 
                compInfo))
        {
            ErrorDescription description = CdiEditorAnalysisFactory.
            createError( param, compInfo, 
                NbBundle.getMessage(DelegateFieldAnalizer.class, 
                        "ERR_DelegateIsNotInDecorator")); // NOI18N
            descriptions.add( description );
        }        
    }

    private void checkMethodDefinition( ExecutableElement element, 
            VariableElement param, CompilationInfo compInfo, 
            List<ErrorDescription> descriptions )
    {
        if ( element.getKind() == ElementKind.CONSTRUCTOR ){
            return;
        }
        if ( !AnnotationUtil.hasAnnotation(element, AnnotationUtil.INJECT_FQN, 
                compInfo))
        {
            ErrorDescription description = CdiEditorAnalysisFactory.
            createError( param, compInfo, 
                NbBundle.getMessage(DelegateMethodAnalyzer.class, 
                        "ERR_WrongDelegateMethod"));                        // NOI18N
            descriptions.add( description );
        }
    }

}
