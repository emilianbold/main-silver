/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.mixeddev.java;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.Future;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaMethodInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaTypeInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.QualifiedNamePart;
import org.netbeans.modules.cnd.mixeddev.java.model.QualifiedNamePart.Kind;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public final class JavaContextSupport {
    
    public static <T> T resolveContext(FileObject fObj, ResolveJavaContextTask<T> task) {
        if (fObj != null) {
            JavaSource js = JavaSource.forFileObject(fObj);
            if (js == null) {
                return null;
            }
            try {
                Future<Void> f = js.runWhenScanFinished(task, true);
                if (f.isDone()){
                    return task.getResult();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }       
    
    public static <T> T resolveContext(Document doc, ResolveJavaContextTask<T> task) {
        if (doc != null) {
            JavaSource js = JavaSource.forDocument(doc);
            if (js == null) {
                return null;
            }
            try {
                Future<Void> f = js.runWhenScanFinished(task, true);
                if (f.isDone()){
                    return task.getResult();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }   
    
    public static int[] getIdentifierSpan(final Document doc, final int offset, final Token<JavaTokenId>[] token) {
        if (getFileObject(doc) == null) {
            //do nothing if FO is not attached to the document - the goto would not work anyway:
            return null;
        }
        final int[][] ret = new int[][] {null}; 
        doc.render(new Runnable() {
            @Override
            public void run() {
                TokenHierarchy th = TokenHierarchy.get(doc);
                TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(th, offset);

                if (ts == null)
                    return;

                ts.move(offset);
                if (!ts.moveNext())
                    return;

                Token<JavaTokenId> t = ts.token();

                if (JavaTokenId.JAVADOC_COMMENT == t.id()) {
                    return;
                } else if (!USABLE_TOKEN_IDS.contains(t.id())) {
                    ts.move(offset - 1);
                    if (!ts.moveNext())
                        return;
                    t = ts.token();
                    if (!USABLE_TOKEN_IDS.contains(t.id()))
                        return;
                }

                if (token != null)
                    token[0] = t;

                ret[0] = new int [] {ts.offset(), ts.offset() + t.length()};
            }
        });
        return ret[0];
    }    
    
    public static JavaMethodInfo createMethodInfo(CompilationController controller, TreePath mtdTreePath) {
        assert mtdTreePath.getLeaf().getKind() == Tree.Kind.METHOD;            

        List<JavaTypeInfo> parameters = new ArrayList<JavaTypeInfo>();
        MethodTree mtdTree = (MethodTree) mtdTreePath.getLeaf();
        for (VariableTree param : mtdTree.getParameters()) {
            parameters.add(createTypeInfo(controller, param.getType()));
        }

        List<QualifiedNamePart> qualifiedName = getQualifiedName(mtdTreePath);
        String simpleName = qualifiedName.size() > 0 ? qualifiedName.get(qualifiedName.size() - 1).getText().toString() : "<not_initialized>"; // NOI18N

        return new JavaMethodInfo(
            simpleName, 
            qualifiedName, 
            parameters, 
            createTypeInfo(controller, mtdTree.getReturnType()), 
            isOverloaded(mtdTreePath, simpleName)
        );
    }    

    public static JavaTypeInfo createTypeInfo(CompilationController controller, Tree type) {
        TreePath typePath = controller.getTrees().getPath(controller.getCompilationUnit(), type);
        switch (type.getKind()) {
            case CLASS: {
                TypeElement elem = (TypeElement) controller.getTrees().getElement(typePath);
                return new JavaTypeInfo(elem.getQualifiedName(), elem.getSimpleName(), 0);
            }
                
            case IDENTIFIER: {
                TypeElement elem = (TypeElement) controller.getTrees().getElement(typePath);
                return new JavaTypeInfo(elem.getQualifiedName(), elem.getSimpleName(), 0);
            }
                
            case MEMBER_SELECT: {
                TypeElement elem = (TypeElement) controller.getTrees().getElement(typePath);
                return new JavaTypeInfo(elem.getQualifiedName(), elem.getSimpleName(), 0);
            }

            case PRIMITIVE_TYPE: {
                CharSequence primitiveName = convertKind(((PrimitiveTypeTree) type).getPrimitiveTypeKind());
                return new JavaTypeInfo(primitiveName, primitiveName, 0);
            }

            case ARRAY_TYPE: {
                ArrayTypeTree arrayType = (ArrayTypeTree) type;
                JavaTypeInfo inner = createTypeInfo(controller, arrayType.getType());
                return new JavaTypeInfo(inner.getFullQualifiedName(), inner.getName(), inner.getArrayDepth() + 1);
            }

            default:
                return new JavaTypeInfo("<NOT_SUPPORTED_KIND_" + type.getKind() + ">", "<NOT_SUPPORTED_KIND_" + type.getKind() + ">", 0); // NOI18N
        }        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //  Package section
    ////////////////////////////////////////////////////////////////////////////        
    
    /*package*/ static List<QualifiedNamePart> getQualifiedName(CompilationController controller, Tree tree) {
        TreePath treePath = controller.getTrees().getPath(controller.getCompilationUnit(), tree);
        return getQualifiedName(treePath);
    }    
        
    /*package*/ static List<QualifiedNamePart> getQualifiedName(TreePath treePath) {
        List<QualifiedNamePart> qualifiedName = new ArrayList<QualifiedNamePart>();
        TreePath currentPath = treePath;
        do {
            switch (currentPath.getLeaf().getKind()) {
                case METHOD:
                    qualifiedName.add(0, new QualifiedNamePart(((MethodTree) currentPath.getLeaf()).getName(), Kind.METHOD));
                    break;

                case CLASS: {
                    if (currentPath.getParentPath() != null && currentPath.getParentPath().getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT) {
                        qualifiedName.add(0, new QualifiedNamePart(((ClassTree) currentPath.getLeaf()).getSimpleName(), Kind.NESTED_CLASS));
                    } else {
                        qualifiedName.add(0, new QualifiedNamePart(((ClassTree) currentPath.getLeaf()).getSimpleName(), Kind.CLASS));
                    }
                    break;
                }
                    
                case COMPILATION_UNIT: {
                    List<CharSequence> dotExpression = renderExpression(((CompilationUnitTree) currentPath.getLeaf()).getPackageName());
                    ListIterator<CharSequence> iter = dotExpression.listIterator(dotExpression.size());
                    while (iter.hasPrevious()) {
                        qualifiedName.add(0, new QualifiedNamePart(iter.previous(), Kind.PACKAGE));
                    }
                    break;
                }
            }
        } while ((currentPath = currentPath.getParentPath()) != null);
        return qualifiedName;
    }

    ////////////////////////////////////////////////////////////////////////////
    //  Private section
    ////////////////////////////////////////////////////////////////////////////
    
    private static final Set<JavaTokenId> USABLE_TOKEN_IDS = EnumSet.of(JavaTokenId.IDENTIFIER);
    
    private static FileObject getFileObject(Document doc) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);        
        return od != null ? od.getPrimaryFile() : null;
    }     
    
    private static CharSequence convertKind(TypeKind kind) {
        switch (kind) {
            case BYTE:
                return "byte"; // NOI18N

            case BOOLEAN:
                return "boolean"; // NOI18N

            case CHAR:
                return "char"; // NOI18N

            case FLOAT:       
                return "float"; // NOI18N

            case DOUBLE:
                return "double"; // NOI18N
                
            case SHORT:
                return "short"; // NOI18N

            case INT:
                return "int"; // NOI18N

            case LONG:
                return "long"; // NOI18N
                
            case VOID:
                return "void"; // NOI18N
        }
        throw new UnsupportedOperationException("Unexpected type kind: " + kind); // NOI18N
    }
            
    private static List<CharSequence> renderExpression(ExpressionTree expr) {
        List<CharSequence> exprParts = new ArrayList<CharSequence>();
        
        do {
            switch (expr.getKind()) {
                case MEMBER_SELECT:
                    exprParts.add(0, ((MemberSelectTree) expr).getIdentifier());
                    expr = ((MemberSelectTree) expr).getExpression();
                    break;
                    
                case IDENTIFIER:
                    exprParts.add(0, ((IdentifierTree) expr).getName());
                    expr = null;
                    break;
                    
                default:
                    expr = null;
            }
        } while (expr != null);
        
        return exprParts;
    }    
    
    private static boolean isOverloaded(TreePath mtdTreePath, String mtdName) {
        if (Tree.Kind.METHOD.equals(mtdTreePath.getLeaf().getKind())) {
            boolean searchNative = ((MethodTree) mtdTreePath.getLeaf()).getModifiers().getFlags().contains(Modifier.NATIVE);
            if (Tree.Kind.CLASS.equals(mtdTreePath.getParentPath().getLeaf().getKind())) {
                int counter = 0;
                ClassTree cls = (ClassTree) mtdTreePath.getParentPath().getLeaf();
                for (Tree member : cls.getMembers()) {
                    if (Tree.Kind.METHOD.equals(member.getKind())) {
                        MethodTree method = (MethodTree) member;
                        if (mtdName.equals(method.getName().toString())) {
                            if (searchNative == method.getModifiers().getFlags().contains(Modifier.NATIVE)) {
                                ++counter;
                            }
                        }
                    }
                }

                if (counter > 1) {
                    return true;
                }
            }           
        }
        return false;
    }
    
    private JavaContextSupport() {
        throw new AssertionError("Not instantiable!"); // NOI18N
    }
}
