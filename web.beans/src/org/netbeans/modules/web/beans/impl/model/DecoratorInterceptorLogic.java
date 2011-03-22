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
package org.netbeans.modules.web.beans.impl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.modules.web.beans.impl.model.results.ResultImpl;


/**
 * @author ads
 *
 */
abstract class DecoratorInterceptorLogic extends EventInjectionPointLogic {

    DecoratorInterceptorLogic( WebBeansModelImplementation model ) {
        super(model);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getDecorators(javax.lang.model.element.TypeElement)
     */
    @Override
    public Collection<TypeElement> getDecorators( TypeElement element ) {
        Collection<DecoratorObject> decorators = getModel().
            getDecoratorsManager().getObjects();
        
        Collection<TypeElement> result = new ArrayList<TypeElement>( decorators.size());
        for (DecoratorObject decoratorObject : decorators) {
            TypeElement decorator = decoratorObject.getTypeElement();
            if ( isDecorator( decorator , element )){
                result.add( decorator );
            }
        }
        return result;
    }

    private boolean isDecorator( TypeElement decorator, TypeElement element ) {
        /*
         * Tripple is used to get delegate Element and its TypeMirror.
         * If @Delegate injection point should be declared in each Decorator
         * explicitly then TypeMirror could be gotten as ".asType()" from
         * element and tripple is not needed.
         * It is unclear from the spec about inheritance delegate injection point
         * ( plain injection point is inherited ). So it is possble when
         * injection point is defined in the superclass and child inherit
         * this injection point without overloading it.
         * In the latter case injection point should be considered from 
         * the child point of view. TipeMirror access in this case is not
         * so simple for delegate method parameter.   
         */
        Triple<VariableElement, TypeMirror, Void> data = 
            getDelegateInjectionPoint(decorator);
        if ( data == null ){
            return false;
        }
        VariableElement delegate = data.getFirst();
        TypeMirror delegateType = data.getSecond();
        Set<TypeElement> set = new HashSet<TypeElement>();
        set.add( element );
        /*
         * Check assignability of delegate injection point and decorated type
         */
        filterBindingsByType(delegate, delegateType, set);
        if ( set.isEmpty() ){
            return false;
        }
        /*
         * Now delegate type is matched to the decorated type and one need 
         * to check just matching delegate qualifiers
         */
        return checkQualifiers(element, delegate, delegateType, set);
    }

    private Triple<VariableElement, TypeMirror, Void> getDelegateInjectionPoint( 
            TypeElement decorator ) 
    {
        List<? extends Element> allMembers = getCompilationController().getElements().
            getAllMembers( decorator );
        List<VariableElement> fields = ElementFilter.fieldsIn(allMembers);
        for (VariableElement field : fields) {
            if ( AnnotationObjectProvider.hasAnnotation(field, 
                    DELEGATE_ANNOTATION, getModel().getHelper() ) && 
                    AnnotationObjectProvider.hasAnnotation(field, 
                            INJECT_ANNOTATION, getModel().getHelper() ))
            {
                TypeMirror delegateType = getCompilationController().getTypes().
                    asMemberOf((DeclaredType)decorator.asType(), field);
                return new Triple<VariableElement, TypeMirror, Void>(field, 
                        delegateType, null);
            }
        }
        
        Triple<VariableElement, TypeMirror, Void> result;
        List<ExecutableElement> methods = ElementFilter.methodsIn(allMembers);
        List<ExecutableElement> ctors = ElementFilter.constructorsIn(allMembers);
        Set<ExecutableElement> allMethods = new LinkedHashSet<ExecutableElement>();
        allMethods.addAll( ctors );
        allMethods.addAll( methods );
        for (ExecutableElement method : allMethods) {
            if ( !AnnotationObjectProvider.hasAnnotation(method, INJECT_ANNOTATION,
                    getModel().getHelper())){
                continue;
            }
            result = getDelegate(method, decorator);
            if ( result != null ){
                return result;
            }
        }
        
        return null;
    }
    
    private Triple<VariableElement, TypeMirror, Void> getDelegate(
            ExecutableElement method, TypeElement decorator )
    {
        List<? extends VariableElement> parameters = method.getParameters();
        int index =0;
        VariableElement delegate = null;
        for (VariableElement variableElement : parameters) {
            if ( AnnotationObjectProvider.hasAnnotation(variableElement, 
                    DELEGATE_ANNOTATION, getModel().getHelper()))
            {
                delegate = variableElement;
                break;
            }
            index ++;
        }
        if ( delegate == null ){
            return null;
        }
        ExecutableType methodType = (ExecutableType)getCompilationController().
            getTypes().asMemberOf((DeclaredType)decorator.asType(), method );
        List<? extends TypeMirror> parameterTypes = methodType.getParameterTypes();
        TypeMirror typeMirror = parameterTypes.get(index);
        return new Triple<VariableElement, TypeMirror, Void>(delegate, typeMirror, null);
    }

    private boolean checkQualifiers( TypeElement element, VariableElement delegate, 
            TypeMirror delegateType, Set<TypeElement> set )
    {
        List<AnnotationMirror> quilifierAnnotations = new LinkedList<AnnotationMirror>();
        boolean anyQualifier = false;
        try {
            anyQualifier = hasAnyQualifier(delegate, false, false, quilifierAnnotations);
        }
        catch(InjectionPointDefinitionError e ){
            return false;
        }
        
        boolean defaultQualifier = !anyQualifier && quilifierAnnotations.size() == 0;
        boolean newQualifier = false; 
        
        if ( quilifierAnnotations.size() == 1 ){
            newQualifier = getModel().getHelper().hasAnnotation(quilifierAnnotations,
                    NEW_QUALIFIER_ANNOTATION);
            defaultQualifier = getModel().getHelper().hasAnnotation(quilifierAnnotations,
                    DEFAULT_QUALIFIER_ANNOTATION);
            
        }
        else if ( quilifierAnnotations.size() == 0 && anyQualifier) {
            // Just @Any case
            return true;
        }
        if  ( defaultQualifier ) {
            // @Default qualifier
            if ( hasImplicitDefaultQualifier(element)){
                return true;
            }
            else {
                List<AnnotationMirror> qualifiers = getQualifiers(element, true);
                return getModel().getHelper().hasAnnotation(qualifiers,
                        DEFAULT_QUALIFIER_ANNOTATION);
            }
        }
        else if (newQualifier){
            ResultImpl lookupResult = handleNewQualifier(delegate, delegateType, 
                    quilifierAnnotations);
            Set<TypeElement> typeElements = lookupResult.getTypeElements();
            return typeElements.contains( element );
        }

        if ( !checkQualifiers(element, quilifierAnnotations) ){
            return false;
        }
        
        filterBindingsByMembers(quilifierAnnotations, set, TypeElement.class );
        return !set.isEmpty();
    }

    private boolean checkQualifiers( TypeElement element,
            List<AnnotationMirror> quilifierAnnotations )
    {
        Set<String> requiredAnnotationFqns = getAnnotationFqns(quilifierAnnotations);
        
        List<? extends AnnotationMirror> elementAnnotations = 
            getQualifiers(element, true);
        Set<String> elementAnnotationFqns = getAnnotationFqns(elementAnnotations);
        
        if ( requiredAnnotationFqns.contains(DEFAULT_QUALIFIER_ANNOTATION) &&
                !elementAnnotationFqns.contains(DEFAULT_QUALIFIER_ANNOTATION) && 
                !hasImplicitDefaultQualifier(element))
        {
            return false;
        }
        requiredAnnotationFqns.remove(DEFAULT_QUALIFIER_ANNOTATION);
        return elementAnnotationFqns.containsAll(requiredAnnotationFqns);
    }

}
