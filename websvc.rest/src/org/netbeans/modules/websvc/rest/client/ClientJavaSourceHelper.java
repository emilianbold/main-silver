/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.rest.client;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.rest.model.api.HttpMethod;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.SubResourceLocator;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.AbstractTask;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.model.WadlSaasResource;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Milan Kuchtiak
 */
public class ClientJavaSourceHelper {

    public static void generateJerseyClient(Node resourceNode, FileObject targetFo, String className) {

        // add REST and Jersey dependencies
        ClassPath cp = ClassPath.getClassPath(targetFo, ClassPath.COMPILE);
        List<Library> restLibs = new ArrayList<Library>();
        if (cp.findResource("javax/ws/rs/WebApplicationException.class") == null) { //NOI18N
            Library lib = LibraryManager.getDefault().getLibrary("restapi"); //NOI18N
            if (lib != null) {
                restLibs.add(lib);
            }
        }
        if (cp.findResource("com/sun/jersey/api/clientWebResource.class") == null) { //NOI18N
            Library lib = LibraryManager.getDefault().getLibrary("restlib"); //NOI18N
            if (lib != null) {
                restLibs.add(lib);
            }
        }
        if (restLibs.size() > 0) {
            try {
                ProjectClassPathModifier.addLibraries(
                        restLibs.toArray(new Library[restLibs.size()]),
                        targetFo,
                        ClassPath.COMPILE);
            } catch (java.io.IOException ex) {
                // the libraries are likely not available
                Logger.getLogger(ClientJavaSourceHelper.class.getName()).log(Level.INFO, "Cannot add Jersey libraries" , ex);
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                            NbBundle.getMessage(ClientJavaSourceHelper.class, "MSG_CannotAddJerseyLib"),
                            NotifyDescriptor.WARNING_MESSAGE));
                return;
            }
        }

        RestServiceDescription restServiceDesc = resourceNode.getLookup().lookup(RestServiceDescription.class);
        if (restServiceDesc != null) {
            String uriTemplate = restServiceDesc.getUriTemplate();
            if (uriTemplate != null) {

                PathFormat pf = null;
                if (uriTemplate.length() == 0) { // subresource locator
                    // find recursively the root resource
                    ResourcePath rootResourcePath = getResourcePath(resourceNode, restServiceDesc.getClassName(), "");
                    uriTemplate = rootResourcePath.getPath();
                    pf = rootResourcePath.getPathFormat();
                } else {
                    pf = getPathFormat(uriTemplate);
                }
                // compute baseURL
                Project prj = resourceNode.getLookup().lookup(Project.class);
                String baseURL =
                        (prj == null ? "" : getBaseURL(prj));
                if (baseURL.endsWith("/")) {
                    baseURL = baseURL.substring(0, baseURL.length() - 1);
                }

                // add inner Jersey Client class
                addJerseyClient(
                        JavaSource.forFileObject(targetFo),
                        className,
                        baseURL,
                        restServiceDesc,
                        null,
                        pf);
            }
        } else {
            WadlSaasResource saasResource = resourceNode.getLookup().lookup(WadlSaasResource.class);
            if (saasResource != null) {
                String baseUrl = saasResource.getSaas().getBaseURL();
                if (baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                }
                ResourcePath resourcePath = getResourcePath(saasResource);
                PathFormat pf = resourcePath.getPathFormat();
                String resourceUri = baseUrl;
                addJerseyClient(
                        JavaSource.forFileObject(targetFo),
                        className,
                        resourceUri,
                        null,
                        saasResource,
                        pf);
            }
        }
    }

    private static void addJerseyClient (
            JavaSource source,
            final String className,
            final String resourceUri,
            final RestServiceDescription restServiceDesc,
            final WadlSaasResource saasResource,
            final PathFormat pf) {
        try {
            ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

                @Override
                public void run(WorkingCopy copy) throws java.io.IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);

                    ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                    //String className = resourceName+"_JerseyClient"; //NOI18N
                    ClassTree modifiedTree = addJerseyClientClass(copy, tree, className, resourceUri, restServiceDesc, saasResource, pf);

                    copy.rewrite(tree, modifiedTree);
                }
            });

            result.commit();
        } catch (java.io.IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static ClassTree addJerseyClientClass (
            WorkingCopy copy,
            ClassTree tree,
            String className,
            String resourceURI,
            RestServiceDescription restServiceDesc,
            WadlSaasResource saasResource,
            PathFormat pf) {

        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree modifs = maker.Modifiers(Collections.<Modifier>singleton(Modifier.STATIC));
        ClassTree innerClass = maker.Class(
                modifs,
                className,
                Collections.<TypeParameterTree>emptyList(),
                null,
                Collections.<Tree>emptyList(),
                Collections.<Tree>emptyList());

        // add 3 fields
        ModifiersTree fieldModif =  maker.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE));
        Tree typeTree = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.api.client.WebResource"); //NOI18N
        VariableTree fieldTree = maker.Variable(fieldModif, "webResource", typeTree, null); //NOI18N
        ClassTree modifiedInnerClass = maker.addClassMember(innerClass, fieldTree);

        fieldModif =  maker.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE));
        typeTree = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.api.client.Client"); //NOI18N
        fieldTree = maker.Variable(fieldModif, "client", typeTree, null); //NOI18N
        modifiedInnerClass = maker.addClassMember(modifiedInnerClass, fieldTree);

        Set<Modifier> modifiersSet = new HashSet<Modifier>();
        modifiersSet.add(Modifier.PRIVATE);
        modifiersSet.add(Modifier.STATIC);
        modifiersSet.add(Modifier.FINAL);
        fieldModif =  maker.Modifiers(modifiersSet);
        typeTree = maker.Identifier("String"); //NOI18N
        fieldTree = maker.Variable(fieldModif, "BASE_URI", typeTree, maker.Literal(resourceURI)); //NOI18N
        modifiedInnerClass = maker.addClassMember(modifiedInnerClass, fieldTree);

        // add constructor
        ModifiersTree emtyModifier = maker.Modifiers(Collections.<Modifier>emptySet());
        TypeElement clientEl = copy.getElements().getTypeElement("com.sun.jersey.api.client.Client"); // NOI18N
        boolean isSubresource = (pf.getArguments().length>0);

        List<VariableTree> paramList = new ArrayList<VariableTree>();
        if (isSubresource) {
            for (String arg : pf.getArguments()) {
                Tree argTypeTree = maker.Identifier("String"); //NOI18N
                ModifiersTree fieldModifier = maker.Modifiers(Collections.<Modifier>emptySet());
                VariableTree argFieldTree = maker.Variable(fieldModifier, arg, argTypeTree, null); //NOI18N
                paramList.add(argFieldTree);
            }
        }

        String resURI = null; //NOI18N
        String subresourceExpr = ""; //NOI18N
        if (isSubresource) {
            subresourceExpr = "    String resourcePath = "+getPathExpression(pf)+";"; //NOI18N
            resURI = "resourcePath"; //NOI18N
        } else {
            resURI = getPathExpression(pf); //NOI18N
        }

        String body =
                "{"+ //NOI18N
                "   client = new "+(clientEl == null ? "com.sun.jersey.api.client.":"")+"Client();"+ //NOI18N
                subresourceExpr +
                "   webResource = client.resource(BASE_URI).path("+resURI+");"+ //NOI18N
                "}"; //NOI18N
        MethodTree constructorTree = maker.Constructor (
                emtyModifier,
                Collections.<TypeParameterTree>emptyList(),
                paramList,
                Collections.<ExpressionTree>emptyList(),
                body);
        modifiedInnerClass = maker.addClassMember(modifiedInnerClass, constructorTree);

        // add setResourcePath() method for SubresourceLocators
        if (isSubresource) {
            ModifiersTree methodModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC));
            body =
                "{"+ //NOI18N
                "   String resourcePath = "+getPathExpression(pf)+";"+ //NOI18N
                "   webResource = client.resource(BASE_URI).path(resourcePath);"+ //NOI18N
                "}"; //NOI18N
            MethodTree methodTree = maker.Method (
                    methodModifier,
                    "setResourcePath", //NOI18N
                    JavaSourceHelper.createTypeTree(copy, "void"), //NOI18N
                    Collections.<TypeParameterTree>emptyList(),
                    paramList,
                    Collections.<ExpressionTree>emptyList(),
                    body,
                    null); //NOI18N
            modifiedInnerClass = maker.addClassMember(modifiedInnerClass, methodTree);
        }

        // add wrappers for http methods (GET/POST/PUT/DELETE)
        if (restServiceDesc != null) {
            List<RestMethodDescription> annotatedMethods =  restServiceDesc.getMethods();
            for (RestMethodDescription methodDesc : annotatedMethods) {
                if (methodDesc instanceof HttpMethod) {
                    List<MethodTree> httpMethods = createHttpMethods(copy, (HttpMethod)methodDesc);
                    for (MethodTree httpMethod : httpMethods) {
                        modifiedInnerClass = maker.addClassMember(modifiedInnerClass, httpMethod);
                    }
                }
            }
        } else if (saasResource != null) {
            modifiedInnerClass = Wadl2JavaHelper.addHttpMethods(copy, modifiedInnerClass, saasResource);
        }

        // add close()
        ModifiersTree methodModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC));
        MethodTree methodTree = maker.Method (
                methodModifier,
                "close", //NOI18N
                JavaSourceHelper.createTypeTree(copy, "void"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                "{"+ //NOI18N
                "   client.destroy();"+ //NOI18N
                "}", //NOI18N
                null); //NOI18N
        modifiedInnerClass = maker.addClassMember(modifiedInnerClass, methodTree);

        ClassTree modifiedTree = tree;
        return maker.addClassMember(modifiedTree, modifiedInnerClass);
    }

    private static List<MethodTree> createHttpMethods(WorkingCopy copy, HttpMethod httpMethod) {
        List<MethodTree> httpMethods = new ArrayList<MethodTree>();
        String method = httpMethod.getType();
        if (RestConstants.GET_ANNOTATION.equals(method)) { //GET
            boolean found = false;
            String produces = httpMethod.getProduceMime();
            if (produces.length() > 0) {
                boolean multipleMimeTypes = produces.contains(","); //NOI18N
                for (HttpMimeType mimeType : HttpMimeType.values()) {
                    if (produces.contains(mimeType.getMimeType())) {
                        httpMethods.add(createHttpGETMethod(copy, httpMethod, mimeType, multipleMimeTypes));
                        found = true;
                    }
                }
            }
            if (!found) {
                httpMethods.add(createHttpGETMethod(copy, httpMethod, null, false));
            }
        } else if ( RestConstants.PUT_ANNOTATION.equals(method) ||
                    RestConstants.POST_ANNOTATION.equals(method) ||
                    RestConstants.DELETE_ANNOTATION.equals(method)
                  ) {
            boolean found = false;
            String consumes = httpMethod.getConsumeMime();
            if (consumes.length() > 0) { //NOI18N
                boolean multipleMimeTypes = consumes.contains(","); //NOI18N
                for (HttpMimeType mimeType : HttpMimeType.values()) {
                    if (consumes.contains(mimeType.getMimeType())) {
                        httpMethods.add(createHttpPOSTMethod(copy, httpMethod, mimeType, multipleMimeTypes));
                        found = true;
                    }
                }
            }
            if (!found) {
                httpMethods.add(createHttpPOSTMethod(copy, httpMethod, null, false));
            }
        }

        return httpMethods;
    }

    private static MethodTree createHttpGETMethod(WorkingCopy copy, HttpMethod httpMethod, HttpMimeType mimeType, boolean multipleMimeTypes) {
        String responseType = httpMethod.getReturnType();
        String path = httpMethod.getPath();
        String methodName = httpMethod.getName() + (multipleMimeTypes ? "_"+mimeType.name() : ""); //NOI18N

        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree methodModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC));
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());

        VariableTree classParam = null;
        ExpressionTree responseTree = null;
        String bodyParam = ""; //NOI18N
        List<TypeParameterTree> typeParams =  null;

        if ("java.lang.String".equals(responseType)) { //NOI18N
            responseTree = maker.Identifier("String"); //NOI18N
            bodyParam="String.class"; //NOI18N
            typeParams =  Collections.<TypeParameterTree>emptyList();
        } else {
            responseTree = maker.Identifier("T"); //NOI18N
            bodyParam="responseType"; //NOI18N
            classParam = maker.Variable(paramModifier, "responseType", maker.Identifier("Class<T>"), null); //NOI18N
            typeParams = Collections.<TypeParameterTree>singletonList(maker.TypeParameter("T", Collections.<ExpressionTree>emptyList())); //NOI18N
        }

        List<VariableTree> paramList = new ArrayList<VariableTree>();
        if (classParam != null) {
            paramList.add(classParam);
        }

        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.api.client.UniformInterfaceException"); //NOI18N

        if (path.length() == 0) {
            String body =
                "{"+ //NOI18N
                    (mimeType == null ?
                        "   return webResource.get("+bodyParam+");" :  //NOI18N
                        "   return webResource.accept("+mimeType.getMediaType()+").get("+bodyParam+");") +  //NOI18N
                "}"; //NOI18N
            return maker.Method (
                    methodModifier,
                    methodName,
                    responseTree,
                    typeParams,
                    paramList,
                    Collections.singletonList(throwsTree), //throws
                    body,
                    null);
        } else {
            PathFormat pf = getPathFormat(path);
            for (String arg : pf.getArguments()) {
                Tree typeTree = maker.Identifier("String"); //NOI18N
                ModifiersTree fieldModifier = maker.Modifiers(Collections.<Modifier>emptySet());
                VariableTree fieldTree = maker.Variable(fieldModifier, arg, typeTree, null); //NOI18N
                paramList.add(fieldTree);
            }
            String body =
                    "{"+ //NOI18N
                        (mimeType == null ?
                            "   return webResource.path("+getPathExpression(pf)+").get("+bodyParam+");" :  //NOI18N
                            "   return webResource.path("+getPathExpression(pf)+").accept("+mimeType.getMediaType()+").get("+bodyParam+");") +  //NOI18N
                    "}"; //NOI18N
            return maker.Method (
                    methodModifier,
                    methodName,
                    responseTree,
                    typeParams,
                    paramList,
                    Collections.<ExpressionTree>singletonList(throwsTree), //throws
                    body,
                    null); //NOI18N
        }
    }

    private static MethodTree createHttpPOSTMethod(WorkingCopy copy, HttpMethod httpMethod, HttpMimeType requestMimeType, boolean multipleMimeTypes) {
        String methodPrefix = httpMethod.getType().toLowerCase();
        String responseType = httpMethod.getReturnType();
        String path = httpMethod.getPath();
        String methodName = httpMethod.getName() + (multipleMimeTypes ? "_"+requestMimeType.name() : ""); //NOI18N

        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree methodModifier = maker.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC));
        ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());

        VariableTree classParam = null;
        ExpressionTree responseTree = null;
        String bodyParam1 = ""; //NOI18N
        String ret = ""; //NOI18N
        List<TypeParameterTree> typeParams =  Collections.<TypeParameterTree>emptyList();
        if ("javax.ws.rs.core.Response".equals(responseType)) { //NOI18N
            TypeElement clientResponseEl = copy.getElements().getTypeElement("com.sun.jersey.api.client.ClientResponse"); //NOI18N
            ret = "return "; //NOI18N
            responseTree = (clientResponseEl == null ?
                copy.getTreeMaker().Identifier("com.sun.jersey.api.client.ClientResponse") : // NOI18N
                copy.getTreeMaker().QualIdent(clientResponseEl));
            bodyParam1 = (clientResponseEl == null ?
                "com.sun.jersey.api.client.ClientResponse.class" : //NOI18N
                "ClientResponse.class"); //NOI18N
        } else if ("void".equals(responseType)) { //NOI18N
            responseTree = maker.Identifier("void"); //NOI18N
        } else if ("java.lang.String".equals(responseType)) { //NOI18N
            responseTree = maker.Identifier("String"); //NOI18N
            ret = "return "; //NOI18N
            bodyParam1="String.class"; //NOI18N
        } else {
            responseTree = maker.Identifier("T"); //NOI18N
            ret = "return "; //NOI18N
            bodyParam1="responseType"; //NOI18N
            classParam = maker.Variable(paramModifier, "responseType", maker.Identifier("Class<T>"), null); //NOI18N
            typeParams = Collections.<TypeParameterTree>singletonList(maker.TypeParameter("T", Collections.<ExpressionTree>emptyList()));
        }

        // create param list

        List<VariableTree> paramList = new ArrayList<VariableTree>();
        if (classParam != null) {
            paramList.add(classParam);
       
        }
        String bodyParam2 = "";
        if (requestMimeType != null) {
            if (requestMimeType == HttpMimeType.FORM) {
                // PENDING
            } else {
                VariableTree objectParam = maker.Variable(paramModifier, "requestEntity", maker.Identifier("Object"), null); //NOI18N
                paramList.add(objectParam);
                bodyParam2=(bodyParam1.length() > 0 ? ", " : "") + "requestEntity"; //NOI18N
            }
        }
        // throws
        ExpressionTree throwsTree = JavaSourceHelper.createTypeTree(copy, "com.sun.jersey.api.client.UniformInterfaceException"); //NOI18N

        if (path.length() == 0) {

            // body
            String body =
                "{"+ //NOI18N
                    (requestMimeType == null ?
                        "   "+ret+"webResource."+methodPrefix+"("+bodyParam1+bodyParam2+");" :  //NOI18N
                        "   "+ret+"webResource.type("+requestMimeType.getMediaType()+")."+methodPrefix+"("+bodyParam1+bodyParam2+");") +  //NOI18N
                "}"; //NOI18N
            return maker.Method (
                    methodModifier,
                    methodName,
                    responseTree,
                    typeParams,
                    paramList,
                    Collections.singletonList(throwsTree),
                    body,
                    null); //NOI18N
        } else {
            // add path params to param list
            PathFormat pf = getPathFormat(path);
            for (String arg : pf.getArguments()) {
                Tree typeTree = maker.Identifier("String"); //NOI18N
                ModifiersTree fieldModifier = maker.Modifiers(Collections.<Modifier>emptySet());
                VariableTree fieldTree = maker.Variable(fieldModifier, arg, typeTree, null); //NOI18N
                paramList.add(fieldTree);
            }
            // body
            String body =
                    "{"+ //NOI18N
                        (requestMimeType == null ?
                            "   "+ret+"webResource.path("+getPathExpression(pf)+")."+methodPrefix+"("+bodyParam1+bodyParam2+");" :  //NOI18N
                            "   "+ret+"webResource.path("+getPathExpression(pf)+").type("+requestMimeType.getMediaType()+")."+methodPrefix+"("+bodyParam1+bodyParam2+");") +  //NOI18N
                    "}"; //NOI18N
            return maker.Method (
                    methodModifier,
                    methodName,
                    responseTree,
                    typeParams,
                    paramList,
                    Collections.<ExpressionTree>singletonList(throwsTree),
                    body,
                    null); //NOI18N
        }
    }

    private static String getPathExpression(PathFormat pf) {
        String[] arguments = pf.getArguments();
        if (arguments.length == 0) {
            return "\""+pf.getPattern()+"\"";
        } else {
            return "java.text.MessageFormat.format(\""+pf.getPattern()+"\", new Object[] {"+getArgumentList(arguments)+"})"; //NOI18N
        }
    }

    private static String getArgumentList(String[] arguments) {
        if (arguments.length == 0) {
            return "";
        } else {
            StringBuffer buf = new StringBuffer(arguments[0]);
            for (int i=1 ; i<arguments.length ; i++) {
                buf.append(","+arguments[i]);
            }
            return buf.toString();
        }
    }

    private static PathFormat getPathFormat(String path) {
        String p = normalizePath(path); //NOI18N
        PathFormat pathFormat = new PathFormat();
        StringBuffer buf = new StringBuffer();
        List<String> arguments = new ArrayList<String>();
        for (int i=0 ; i<p.length() ; i++) {
            char ch = p.charAt(i);
            if (ch == '{') { //NOI18N
                int j=i+1;
                while (j<p.length() &&  p.charAt(j) != '}') { //NOI18N
                    j++;
                }
                String arg = p.substring(i+1,j);
                buf.append("{"+arguments.size()+"}"); //NOI18N
                arguments.add(arg);
                i = j;
            } else {
                buf.append(ch);
            }
        }

        pathFormat.setPattern(buf.toString());
        pathFormat.setArguments(arguments.toArray(new String[arguments.size()]));
        return pathFormat;
    }

    private static ResourcePath getResourcePath(WadlSaasResource saasResource) {
        String path = normalizePath(saasResource.getResource().getPath());
        WadlSaasResource parent = saasResource.getParent();
        while(parent != null) {
            String pathToken = normalizePath(parent.getResource().getPath());
            if (pathToken.length()>0) {
                path = pathToken+"/"+path; //NOI18N
            }
            parent = parent.getParent();
        }
        return new ResourcePath(getPathFormat(path), path);
    }

    private static String normalizePath(String path) {
        String s = path;
        while (s.startsWith("/")) { //NOI18N
            s = s.substring(1);
        }
        while (s.endsWith("/")) { //NOI18N
            s = s.substring(0, s.length()-1);
        }
        return s;
    }

    private static ResourcePath getResourcePath(Node resourceNode, String resourceClass, String uriTemplate) {
        String resourceUri = normalizePath(uriTemplate);
        Node projectNode = resourceNode.getParentNode();
        if (projectNode != null) {
            for (Node sibling : projectNode.getChildren().getNodes()) {
                if (resourceNode != sibling) {
                    RestServiceDescription desc = sibling.getLookup().lookup(RestServiceDescription.class);
                    if (desc != null) {
                        for (RestMethodDescription m : desc.getMethods()) {
                            if (m instanceof SubResourceLocator) {
                                SubResourceLocator resourceLocator = (SubResourceLocator)m;
                                if (resourceClass.equals(resourceLocator.getReturnType())) {
                                    // detect resource locator uri
                                    String resourceLocatorUri = normalizePath(resourceLocator.getUriTemplate());
                                    String parentResourceUri = desc.getUriTemplate();
                                    if (parentResourceUri.length() > 0) {
                                        // found root resource
                                        String subresourceUri = null;
                                        if (resourceLocatorUri.length() > 0) {
                                            if (resourceUri.length() > 0) {
                                                subresourceUri = resourceLocatorUri+"/"+resourceUri; //NOI18N
                                            } else {
                                                subresourceUri = resourceLocatorUri;
                                            }
                                        } else {
                                            subresourceUri = resourceUri;
                                        }
                                        PathFormat pf = getPathFormat(normalizePath(parentResourceUri)+"/"+subresourceUri); //NOI18N
                                        return new ResourcePath(pf, parentResourceUri);
                                    } else {
                                        // searching recursively further
                                        return getResourcePath(sibling, desc.getClassName(), resourceLocatorUri+"/"+uriTemplate); //NOI8N
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return new ResourcePath(getPathFormat(uriTemplate), uriTemplate);
    }

    public static String getBaseURL(Project project) {
        
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        String serverInstanceID = provider.getServerInstanceID();
        if (serverInstanceID == null) {
            Logger.getLogger(ClientJavaSourceHelper.class.getName()).log(Level.INFO, "Can not detect target J2EE server"); //NOI18N
            return "";
        }
        // getting port and host name
        ServerInstance serverInstance = Deployment.getDefault().getServerInstance(serverInstanceID);
        String portNumber = "8080"; //NOI18N
        String hostName = "localhost"; //NOI18N
        try {
            ServerInstance.Descriptor instanceDescriptor = serverInstance.getDescriptor();
            if (instanceDescriptor != null) {
                int port = instanceDescriptor.getHttpPort();
                portNumber = port == 0 ? "8080" : String.valueOf(port); //NOI18N
                String hstName = instanceDescriptor.getHostname();
                if (hstName != null) {
                    hostName = hstName;
                }
            }
        } catch (InstanceRemovedException ex) {
            Logger.getLogger(ClientJavaSourceHelper.class.getName()).log(Level.INFO, "Removed ServerInstance", ex); //NOI18N
        }

        String contextRoot = null;
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();

        if (J2eeModule.Type.WAR.equals(moduleType)) {
            J2eeModuleProvider.ConfigSupport configSupport = provider.getConfigSupport();
            try {
                contextRoot = configSupport.getWebContextRoot();
            } catch (ConfigurationException e) {
                // TODO the context root value could not be read, let the user know about it
            }
            if (contextRoot != null && contextRoot.startsWith("/")) { //NOI18N
                //NOI18N
                contextRoot = contextRoot.substring(1);
            }
        }
        String applicationPath = "resources"; //NOI18N
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        if (restSupport != null) {
            try {
                applicationPath = restSupport.getApplicationPath();
            } catch (IOException ex) {}
        }
        return "http://" + hostName + ":" + portNumber + "/" + //NOI18N
                (contextRoot != null && !contextRoot.equals("") ? contextRoot : "") + //NOI18N
                "/"+applicationPath; //NOI18N
    }

    static class PathFormat {
        private String pattern;
        private String[] arguments;

        public String[] getArguments() {
            return arguments;
        }

        public void setArguments(String[] arguments) {
            this.arguments = arguments;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }

    static class ResourcePath {
        private PathFormat pathFormat;
        private String path;

        public ResourcePath() {
        }

        public ResourcePath(PathFormat pathFormat, String path) {
            this.pathFormat = pathFormat;
            this.path = path;
        }

        public PathFormat getPathFormat() {
            return pathFormat;
        }

        public void setPathFormat(PathFormat pathFormat) {
            this.pathFormat = pathFormat;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    static enum HttpMimeType {
        XML("application/xml", "javax.ws.rs.core.MediaType.APPLICATION_XML"), //NOI18N
        JSON("application/json", "javax.ws.rs.core.MediaType.APPLICATION_JSON"), //NOI18N
        TEXT("text/plain", "javax.ws.rs.core.MediaType.TEXT_PLAIN"), //NOI18N
        HTML("text/html", "javax.ws.rs.core.MediaType.TEXT_HTML"), //NOI18N
        TEXT_XML("text/xml", "javax.ws.rs.core.MediaType.TEXT_XML"), //NOI18N
        FORM("application/x-www-form-urlencoded", "javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED"); //NOI18N

        private String mimeType;
        private String mediaType;

        HttpMimeType(String mimeType, String mediaType) {
            this.mimeType = mimeType;
            this.mediaType = mediaType;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getMediaType() {
            return mediaType;
        }
    }
}
