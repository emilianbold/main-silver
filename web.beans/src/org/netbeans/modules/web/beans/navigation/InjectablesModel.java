/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package org.netbeans.modules.web.beans.navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.beans.api.model.Result;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.openide.filesystems.FileObject;

/**
 * @author ads
 */
public final class InjectablesModel extends DefaultTreeModel {
    
    private static final long serialVersionUID = -6845959436250662000L;

    private static final Logger LOG = Logger.getLogger(
            InjectablesModel.class.getName());
    
    static Element[] EMPTY_ELEMENTS_ARRAY = new Element[0];

    public InjectablesModel(Result result, 
            CompilationController controller ,MetadataModel<WebBeansModel> model ) 
    {
        super(null);
        
        myModel = model;
        if ( result.getKind() == Result.ResultKind.DEFINITION_ERROR || 
                !( result instanceof Result.ApplicableResult))
        {
            myTypeHandles= Collections.emptyList();
            myProductionHandles = Collections.emptyMap();
            return;
        }
        
        if ( result instanceof Result.ResolutionResult ){
            myResult = (Result.ResolutionResult)result;
        }
        
        Result.ApplicableResult applicableResult = (Result.ApplicableResult) result;
        Set<TypeElement> typeElements = applicableResult.getTypeElements();
        
        myProductionHandles = new HashMap<ElementHandle<?>, 
            List<TypeMirrorHandle<DeclaredType>>>();
        
        myDisabledBeans = new HashSet<ElementHandle<?>>();
        Set<Element> disabled = new HashSet<Element>();

        myTypeHandles = new ArrayList<ElementHandle<TypeElement>>(typeElements.size());
        for (TypeElement el : typeElements) {
            ElementHandle<TypeElement> handle = ElementHandle.create(el);
            myTypeHandles.add(handle);
            if ( applicableResult.isDisabled(el)){
                myDisabledBeans.add( handle );
                disabled.add( el );
            }
        }
        
        Map<Element, List<DeclaredType>> allProductions = 
            applicableResult.getAllProductions();
        for (Entry<Element, List<DeclaredType>> entry : allProductions.entrySet()) {
            ElementHandle<Element> handleKey = ElementHandle.create( entry.getKey());
            List<TypeMirrorHandle<DeclaredType>> list = 
                new ArrayList<TypeMirrorHandle<DeclaredType>>( entry.getValue().size());
            for (DeclaredType type : entry.getValue()) {
                TypeMirrorHandle<DeclaredType> typeHandle = 
                    TypeMirrorHandle.create( type );
                list.add( typeHandle );
            }
            myProductionHandles.put(handleKey,  list );
            if ( applicableResult.isDisabled(entry.getKey())){
                myDisabledBeans.add( handleKey );
                disabled.add( entry.getKey() );
            }
        }

        update( typeElements, allProductions, disabled , controller);
    }
    
    void update() {
        update( myTypeHandles , myProductionHandles );
    }

    void fireTreeNodesChanged() {
        super.fireTreeNodesChanged(this, getPathToRoot((TreeNode)getRoot()), 
                null, null);
    }

    private void update( final List<ElementHandle<TypeElement>> typeHandles ,
            final Map<ElementHandle<?>,List<TypeMirrorHandle<DeclaredType>>> 
            productions ) 
    {
        try {
            getModel().runReadAction(
                    new MetadataModelAction<WebBeansModel, Void>() {

                        public Void run( WebBeansModel model ) {
                            Set<Element> disabled = new HashSet<Element>();
                            List<TypeElement> typesList = fillTypes(typeHandles, 
                                    model, disabled);
                            
                            Map<Element, List<DeclaredType>> productionsMap = 
                                fillProductions( productions, model , disabled);

                            update(typesList, productionsMap , disabled , model
                                    .getCompilationController());
                            return null;
                        }
                    });

            return;
        }
        catch (MetadataModelException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
        catch (IOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    private Map<Element, List<DeclaredType>> fillProductions(
            Map<ElementHandle<?>, List<TypeMirrorHandle<DeclaredType>>> productions,
            WebBeansModel model, Set<Element> disabled )
    {
        Map<Element, List<DeclaredType>> result;
        if ( productions == null || productions.size() == 0){
            result = Collections.emptyMap();
        }
        else {
            result = new HashMap<Element, List<DeclaredType>>();
            for(Entry<ElementHandle<?>,List<TypeMirrorHandle<DeclaredType>>>
                entry : productions.entrySet() )
            {
                ElementHandle<?> handle = entry.getKey();
                Element element = handle.resolve(model.getCompilationController());
                if (element != null) {
                    if (myDisabledBeans.contains(handle)) {
                        disabled.add(element);
                    }
                    List<DeclaredType> list = new ArrayList<DeclaredType>( 
                            entry.getValue().size());
                    for( TypeMirrorHandle<DeclaredType> mirrorHandle : 
                        entry.getValue())
                    {
                        DeclaredType mirror = mirrorHandle.resolve( 
                                model.getCompilationController());
                        if ( mirror!= null){
                            list.add( mirror );
                        }
                        else {
                            LOG.warning(mirrorHandle.toString()
                                    + " cannot be resolved using: " // NOI18N
                                    + model.getCompilationController()
                                            .getClasspathInfo());
                        }
                    }
                    if ( list.size() >0 ){
                        result.put(element, list);
                    }
                }
                else {
                    LOG.warning(handle.toString()
                            + " cannot be resolved using: " // NOI18N
                            + model.getCompilationController()
                                    .getClasspathInfo());
                }
            }
        }
        return result;
    }

    private  List<TypeElement> fillTypes(final List<ElementHandle<TypeElement>> 
            typeHandles, WebBeansModel model, Set<Element> disabled )
    {
        List<TypeElement> typesList;
        if (typeHandles != null && typeHandles.size() != 0)
        {
            typesList = new ArrayList<TypeElement>(
                    typeHandles.size());

            for (ElementHandle<TypeElement> 
                typeHandle : typeHandles)
            {
                TypeElement element = typeHandle
                        .resolve(model
                                .getCompilationController());
                if (element != null) {
                    typesList.add(element);
                    if ( myDisabledBeans.contains( typeHandle)){
                        disabled.add( element);
                    }
                }
                else {
                    LOG.warning(typeHandle.toString()
                        + " cannot be resolved using: "     // NOI18N
                         + model.getCompilationController()
                                .getClasspathInfo());
                }
            }
        }
        else {
            typesList = Collections.emptyList();
        }
        return typesList;
    }

    private void update(final Collection<TypeElement> typeElements, 
            final Map<Element, List<DeclaredType>> productions, final 
            Set<Element> disabledBeans,
            CompilationController controller) 
    {
        if (typeElements.size()==0 && productions.size() == 0 ) {
            return;
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        Map<Element, InjectableTreeNode<? extends Element>> elementMap= 
            new LinkedHashMap<Element, InjectableTreeNode<? extends Element>>();

        for (TypeElement element : typeElements) {
            FileObject fileObject = SourceUtils.getFile(ElementHandle
                    .create(element), controller.getClasspathInfo());
            // Type declaration
            TypeTreeNode node = new TypeTreeNode(fileObject,
                    (TypeElement) element, controller);
            insertTreeNode(elementMap, (TypeElement) element, node, root, 
                    disabledBeans.contains(element), controller);
        }
        
        for (Entry<Element,List<DeclaredType>> entry : productions.entrySet()){
            Element element = entry.getKey();
            FileObject fileObject = SourceUtils.getFile(ElementHandle
                    .create(element), controller.getClasspathInfo());
            if ( element instanceof ExecutableElement ){
                // Method definition
                MethodTreeNode node = new MethodTreeNode(fileObject, 
                        (ExecutableElement)element, entry.getValue().get( 0 ),
                        disabledBeans.contains( element), controller);
                insertTreeNode( elementMap , (ExecutableElement)element , 
                        node , root ,  controller);
                if ( entry.getValue().size() >1 ){
                    /*
                     *  TODO : tree could be extended with children : 
                     *  executable element  may be considered as parent
                     *  for type mirrors that are this executable element
                     *  viewed as member of DeclaredType ( item in entry.getValue() ).
                     */
                }
            }
            else  {
                // Should be produces field.
                InjectableTreeNode<Element> node = 
                    new InjectableTreeNode<Element>(fileObject, element,  
                            entry.getValue().get( 0 ), 
                            disabledBeans.contains(element),controller);
                insertTreeNode( elementMap , node , root );
                if ( entry.getValue().size() >1 ){
                    /*
                     *  TODO : tree could be extended with children : 
                     *  field element  may be considered as parent
                     *  for type mirrors that are this filed 
                     *  viewed as member of DeclaredType ( item in entry.getValue() ).
                     */
                }
            }
        }

        setRoot(root);
    }
    
    private void insertTreeNode( Map<Element, 
            InjectableTreeNode<? extends Element>> elementMap,TypeElement element , 
            TypeTreeNode node, DefaultMutableTreeNode root , boolean isDisabled,
            CompilationController controller)
    {
        TypeTreeNode parent = null;
        
        for( Entry<Element, InjectableTreeNode<? extends Element>> entry : 
                elementMap.entrySet())
        {
            Element key = entry.getKey();
            if ( !( key instanceof TypeElement )){
                continue;
            }
            TypeTreeNode injectableNode = (TypeTreeNode)entry.getValue();
            TypeElement typeElement = (TypeElement)key;
            if (typeElement == null ){
                continue;
            }
            if ( controller.getTypes().isAssignable( element.asType(), 
                    typeElement.asType()))
            {
                if ( parent == null ){
                    parent = injectableNode;
                }
                else if ( parent.isAssignableFrom(typeElement, controller)){
                    parent = injectableNode;
                }
            }
        }
        
        DefaultMutableTreeNode parentNode = parent;
        
        if ( parentNode == null ){
            parentNode = root;
        }
        Enumeration<?> children = parentNode.children();
        List<TypeTreeNode> movedChildren = new LinkedList<TypeTreeNode>();
        while (children.hasMoreElements()) {
            TypeTreeNode childNode = (TypeTreeNode) children.nextElement();
            if (childNode.isAssignable(element, controller))
            {
                movedChildren.add(childNode);
            }
        }

        for (TypeTreeNode typeTreeNode : movedChildren) {
            parentNode.remove(typeTreeNode);
            node.add(typeTreeNode);
        }
        parentNode.add(node);
        elementMap.put(element, node);
    }
    
    private void insertTreeNode( Map<Element, 
            InjectableTreeNode<? extends Element>> elementMap,
            ExecutableElement element , MethodTreeNode node, 
            DefaultMutableTreeNode root , CompilationController controller)
    {
        MethodTreeNode parent = null;
        
        List<ExecutableElement> overriddenMethods = new ArrayList<ExecutableElement>();
        ExecutableElement overriddenMethod = element;
        while ( true ){
            overriddenMethod = 
                controller.getElementUtilities().getOverriddenMethod(overriddenMethod);
            if ( overriddenMethod == null ){
                break;
            }
            overriddenMethods.add( overriddenMethod );
        }
        if ( overriddenMethods.size() > 0  )
        {
            for (Entry<Element, InjectableTreeNode<? extends Element>> entry : 
                elementMap.entrySet())
            {
                Element key = entry.getKey();
                if (!(key instanceof ExecutableElement)) {
                    continue;
                }
                MethodTreeNode injectableNode = (MethodTreeNode) entry
                        .getValue();
                ExecutableElement method = (ExecutableElement) key;
                if (method == null) {
                    continue;
                }

                int index = overriddenMethods.indexOf( method);
                if ( index != -1 ) {
                    if (parent == null) {
                        parent = injectableNode;
                    }
                    else if (parent.isOverridden( index, overriddenMethods, 
                            controller)) 
                    {
                        parent = injectableNode;
                    }
                }
            }
        }
        
        DefaultMutableTreeNode parentNode = parent;
        
        if ( parentNode == null ){
            parentNode = root;
        }
        Enumeration<?> children = parentNode.children();
        List<MethodTreeNode> movedChildren = new LinkedList<MethodTreeNode>();
        while (children.hasMoreElements()) {
            MethodTreeNode childNode = (MethodTreeNode) children.nextElement();
            if (childNode.overridesMethod(element, controller))
            {
                movedChildren.add(childNode);
            }
        }

        for (MethodTreeNode methodNode : movedChildren) {
            parentNode.remove(methodNode);
            node.add(methodNode);
        }
        parentNode.add(node);
        elementMap.put(element, node);
    }
    
    private void insertTreeNode( Map<Element, 
            InjectableTreeNode<? extends Element>> elementMap,
            InjectableTreeNode<Element> node, DefaultMutableTreeNode root )
    {
        root.add( node );
    }

    private MetadataModel<WebBeansModel> getModel(){
        return myModel;
    }
    
    private List<ElementHandle<TypeElement>> myTypeHandles;
    private Map<ElementHandle<?>,List<TypeMirrorHandle<DeclaredType>>> myProductionHandles;
    private Set<ElementHandle<?>> myDisabledBeans;
    private MetadataModel<WebBeansModel> myModel;
    private Result.ResolutionResult myResult;
}
