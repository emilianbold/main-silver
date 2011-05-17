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
package org.netbeans.modules.web.beans.analysis.analyzer.method;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.analysis.CdiEditorAnalysisFactory;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer.MethodAnalyzer;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class AnnotationsAnalyzer implements MethodAnalyzer {


    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer.MethodAnalyzer#analyze(javax.lang.model.element.ExecutableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, org.netbeans.api.java.source.CompilationInfo, java.util.List, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( ExecutableElement element, TypeMirror returnType,
            TypeElement parent, CompilationInfo compInfo,
            List<ErrorDescription> descriptions , AtomicBoolean cancel )
    {
        checkProductionObserverDisposerInject( element , compInfo ,descriptions,
                cancel );
    }

    private void checkProductionObserverDisposerInject(
            ExecutableElement element, CompilationInfo compInfo,
            List<ErrorDescription> descriptions , AtomicBoolean cancel )
    {
        boolean isProducer = AnnotationUtil.hasAnnotation(element, 
                AnnotationUtil.PRODUCES_FQN, compInfo);
        boolean isInitializer = AnnotationUtil.hasAnnotation(element, 
                AnnotationUtil.INJECT_FQN, compInfo);
        int observesCount = 0;
        int disposesCount = 0;
        List<? extends VariableElement> parameters = element.getParameters();
        for (VariableElement param : parameters) {
            if ( cancel.get() ){
                return;
            }
            if ( AnnotationUtil.hasAnnotation( param, AnnotationUtil.OBSERVES_FQN, 
                    compInfo))
            {
                observesCount++;
            }
            if ( AnnotationUtil.hasAnnotation( param, AnnotationUtil.DISPOSES_FQN, 
                    compInfo))
            {
                disposesCount++;
            }
        }
        String firstAnnotation = null;
        String secondAnnotation = null;
        if ( isProducer ){
            firstAnnotation = AnnotationUtil.PRODUCES;
            if ( isInitializer ){
                secondAnnotation = AnnotationUtil.INJECT;
            }
            else if ( observesCount >0 ){
                secondAnnotation = AnnotationUtil.OBSERVES;
            }
            else if ( disposesCount >0 ){
                secondAnnotation = AnnotationUtil.DISPOSES;
            }
        }
        else if ( isInitializer ){
            firstAnnotation = AnnotationUtil.INJECT;
            if ( observesCount >0 ){
                secondAnnotation = AnnotationUtil.OBSERVES;
            }
            else if ( disposesCount >0 ){
                secondAnnotation = AnnotationUtil.DISPOSES;
            }
        }
        else if ( observesCount >0 ){
            firstAnnotation = AnnotationUtil.OBSERVES;
            if ( disposesCount >0 ){
                secondAnnotation = AnnotationUtil.DISPOSES;
            }
        }
        if ( firstAnnotation != null && secondAnnotation != null  ){
            ErrorDescription description = CdiEditorAnalysisFactory.
                createError( element, compInfo, NbBundle.getMessage(
                    AnnotationsAnalyzer.class, "ERR_BothAnnotationsMethod", // NOI18N
                    firstAnnotation, secondAnnotation ));
            descriptions.add( description );
        }
        
        // Test quantity of observer parameters
        if ( observesCount > 1){
            ErrorDescription description = CdiEditorAnalysisFactory.
            createError( element, compInfo, NbBundle.getMessage(
                AnnotationsAnalyzer.class, "ERR_ManyObservesParameter" ));   // NOI18N
            descriptions.add( description );
        }
        // Test quantity of disposes parameters
        else if ( disposesCount >1 ){
            ErrorDescription description = CdiEditorAnalysisFactory.
            createError( element, compInfo, NbBundle.getMessage(
                AnnotationsAnalyzer.class, "ERR_ManyDisposesParameter"));    // NOI18N
            descriptions.add( description );
        }
        
        // A producer/disposer method must be a non-abstract method . 
        checkAbstractMethod(element, compInfo, descriptions, isProducer,
                disposesCount>0);
        
        if ( isInitializer ){
            checkInitializerMethod(element, compInfo, descriptions);
        }
    }

    private void checkInitializerMethod( ExecutableElement element,
            CompilationInfo compInfo, List<ErrorDescription> descriptions )
    {
        Set<Modifier> modifiers = element.getModifiers();
        boolean isAbstract = modifiers.contains( Modifier.ABSTRACT );
        boolean isStatic = modifiers.contains( Modifier.STATIC );
        if (  isAbstract || isStatic ){
            String key = isAbstract? "ERR_AbstractInitMethod":
                "ERR_StaticInitMethod";           // NOI18N
            ErrorDescription description = CdiEditorAnalysisFactory.
            createError( element, compInfo, NbBundle.getMessage(
                AnnotationsAnalyzer.class, key ));
            descriptions.add( description );
        }        
    }

    private void checkAbstractMethod( ExecutableElement element,
            CompilationInfo compInfo, List<ErrorDescription> descriptions,
            boolean isProducer, boolean isDisposer )
    {
        if ( isProducer || isDisposer ){
            String key = isProducer? "ERR_AbstractProducerMethod":
                "ERR_AbstractDisposerMethod";           // NOI18N
            Set<Modifier> modifiers = element.getModifiers();
            if ( modifiers.contains( Modifier.ABSTRACT )){
                ErrorDescription description = CdiEditorAnalysisFactory.
                createError( element, compInfo, NbBundle.getMessage(
                    AnnotationsAnalyzer.class, key ));
                descriptions.add( description );
            }
        }
    }

}
