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

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.beans.MetaModelSupport;
import org.netbeans.modules.web.beans.analysis.CdiEditorAnalysisFactory;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer.MethodAnalyzer;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult.ResultKind;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class AnnotationsAnalyzer implements MethodAnalyzer {
    
    private static final Logger LOG = Logger.getLogger( AnnotationsAnalyzer.class.getName() );


    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer.MethodAnalyzer#analyze(javax.lang.model.element.ExecutableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, org.netbeans.api.java.source.CompilationInfo, java.util.List, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( ExecutableElement element, TypeMirror returnType,
            TypeElement parent, CompilationInfo compInfo,
            List<ErrorDescription> descriptions , AtomicBoolean cancel )
    {
        checkProductionObserverDisposerInject( element , parent , 
                compInfo ,descriptions, cancel );
        if ( cancel.get()){
            return;
        }
        checkInjectionPoint( element , parent, compInfo , descriptions, cancel );
    }

    private void checkInjectionPoint( final ExecutableElement element, 
            TypeElement parent, final CompilationInfo compInfo, 
            final List<ErrorDescription> descriptions, final AtomicBoolean cancel )
    {
        Project project = FileOwnerQuery.getOwner( compInfo.getFileObject() );
        if ( project == null ){
            return ;
        }
        if ( cancel.get() ){
            return;
        }
        MetaModelSupport support = new MetaModelSupport(project);
        MetadataModel<WebBeansModel> metaModel = support.getMetaModel();
        final ElementHandle<ExecutableElement> handle = ElementHandle.create( element);
        final ElementHandle<TypeElement> parentHandle = ElementHandle.create( parent);
        try {
            metaModel.runReadAction( 
                    new MetadataModelAction<WebBeansModel, Void>() 
            {
                @Override
                public Void run( WebBeansModel model ) throws Exception {
                    ExecutableElement method = handle.resolve(
                            model.getCompilationController());
                    TypeElement parent = parentHandle.resolve(
                            model.getCompilationController());
                    if ( method == null || parent == null ){
                        return null;
                    }
                    for( VariableElement var : method.getParameters()){
                        if ( cancel.get() ){
                            return null;
                        }
                        if ( model.isInjectionPoint( var )){
                            checkName( method, var , 
                                    model.getCompilationController() , descriptions );
                            if( !model.isDynamicInjectionPoint(var)) {
                                DependencyInjectionResult result = model.
                                    lookupInjectables( var,  (DeclaredType)parent.asType());
                                checkResult(result, var , 
                                        model.getCompilationController() , 
                                        descriptions );
                            }
                        }
                    }
                    return null;
                }

            });
        }
        catch (MetadataModelException e) {
            if ( informInjectionPointDefError(e, element, compInfo, 
                    descriptions))
            {
                LOG.log( Level.INFO , null , e);
            }
        }
        catch (IOException e) {
            if ( informInjectionPointDefError(e, element, compInfo, 
                    descriptions))
            {
                LOG.log( Level.INFO , null , e);
            }
        }
    }
    
    private void checkName( ExecutableElement element, VariableElement var,
            CompilationInfo compInfo, List<ErrorDescription> descriptions )
    {
        AnnotationMirror annotation = AnnotationUtil.getAnnotationMirror( 
                var , AnnotationUtil.NAMED, compInfo);
        if ( annotation!= null && annotation.getElementValues().size() == 0 ){
            ErrorDescription description = CdiEditorAnalysisFactory.
                createError(var, compInfo, "ERR_ParameterNamedInjectionPoint");
            descriptions.add( description );
        }
    }

    private void checkResult( DependencyInjectionResult result ,
            Element element, CompilationInfo compInfo,
            List<ErrorDescription> descriptions )
    {
        if ( result instanceof DependencyInjectionResult.Error ){
            ResultKind kind = result.getKind();
            Severity severity = Severity.WARNING;
            if ( kind == DependencyInjectionResult.ResultKind.DEFINITION_ERROR){
                severity = Severity.ERROR;
            }
            String message = ((DependencyInjectionResult.Error)result).getMessage();
            ErrorDescription description = CdiEditorAnalysisFactory.
                createNotification(severity, element , compInfo, message);
            descriptions.add( description );
        }
    }

    private boolean informInjectionPointDefError(Exception exception , Element element, 
            CompilationInfo compInfo, List<ErrorDescription> descriptions)
    {
        Throwable cause = exception.getCause();
        if ( cause instanceof InjectionPointDefinitionError ){
            ErrorDescription description = CdiEditorAnalysisFactory.
                createError( element, compInfo, 
                    cause.getMessage());
            descriptions.add( description );
            return true;
        }
        return false;
    }

    private void checkProductionObserverDisposerInject(
            ExecutableElement element, TypeElement parent, CompilationInfo compInfo,
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
            checkInitializerMethod(element, parent , compInfo, descriptions);
        }
    }

    private void checkInitializerMethod( ExecutableElement element, 
            TypeElement parent, CompilationInfo compInfo, 
            List<ErrorDescription> descriptions )
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
        TypeMirror method = compInfo.getTypes().asMemberOf(
                (DeclaredType)parent.asType() , element);
        if ( method instanceof ExecutableType ){
            List<? extends TypeVariable> typeVariables = 
                ((ExecutableType)method).getTypeVariables();
            if ( typeVariables != null && typeVariables.size() > 0 ){
                ErrorDescription description = CdiEditorAnalysisFactory.
                    createError( element, compInfo, NbBundle.getMessage(
                            AnnotationsAnalyzer.class, "ERR_GenericInitMethod" ));
                descriptions.add( description );
            }
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
