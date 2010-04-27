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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;

import static org.netbeans.api.java.lexer.JavaTokenId.LINE_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.BLOCK_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.JAVADOC_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.WHITESPACE;

/**
 *
 * @author Jan Becicka
 */
public class RenameTransformer extends RefactoringVisitor {

    private Set<ElementHandle<ExecutableElement>> allMethods;
    private String newName;
    private boolean renameInComments;

    public RenameTransformer(String newName, Set<ElementHandle<ExecutableElement>> am, boolean renameInComments) {
        this.newName = newName;
        this.allMethods = am;
        this.renameInComments = renameInComments;
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        if (!renameInComments) {
            return super.visitCompilationUnit(node, p);
        }

        if (p.getKind() == ElementKind.PARAMETER) {
            renameParameterInMethodComments(p);

        } else {
            String originalName = getOldSimpleName(p);
            if (originalName!=null) {
                TokenSequence<JavaTokenId> ts = workingCopy.getTokenHierarchy().tokenSequence(JavaTokenId.language());

                while (ts.moveNext()) {
                    Token<JavaTokenId> t = ts.token();
                    
                    if (isComment(t)) {
                        rewriteAllInComment(t.text().toString(), ts.offset(), originalName);
                    }
                }
            }
        }
        return super.visitCompilationUnit(node, p);
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        renameUsageIfMatch(getCurrentPath(), node,p);
        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        renameUsageIfMatch(getCurrentPath(), node,p);
        return super.visitMemberSelect(node, p);
    }
    
    private String getOldSimpleName(Element p) {
        if (p!=null) {
            return p.getSimpleName().toString();
        }
        for (ElementHandle<ExecutableElement> mh : allMethods) {
            ExecutableElement baseMethod = mh.resolve(workingCopy);
            if (baseMethod == null) {
                continue;
            }
            return baseMethod.getSimpleName().toString();
        }
        return null;
    }
    
    private void renameUsageIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(path))
            return;
        Trees trees = workingCopy.getTrees();
        Element el = workingCopy.getTrees().getElement(path);
        if (el == null) {
            path = path.getParentPath();
            if (path != null && path.getLeaf().getKind() == Tree.Kind.IMPORT) {
                ImportTree impTree = (ImportTree)path.getLeaf();
                if (!impTree.isStatic()) {
                    return;
                }
                Tree idTree = impTree.getQualifiedIdentifier();
                if (idTree.getKind() != Tree.Kind.MEMBER_SELECT) {
                    return;
                }
                final Name id = ((MemberSelectTree) idTree).getIdentifier();
                if (id == null || id.contentEquals("*")) { // NOI18N
                    // skip import static java.lang.Math.*
                    return;
                }
                Tree classTree = ((MemberSelectTree) idTree).getExpression();
                path = trees.getPath(workingCopy.getCompilationUnit(), classTree);
                el = trees.getElement(path);
                if (el == null) {
                    return;
                }
                Iterator iter = workingCopy.getElementUtilities().getMembers(el.asType(),new ElementUtilities.ElementAcceptor() {
                    @Override
                    public boolean accept(Element e, TypeMirror type) {
                        return id.equals(e.getSimpleName());
                    }
                }).iterator();
                if (iter.hasNext()) {
                    el = (Element) iter.next();
                }
                if (iter.hasNext()) {
                    return;
                }
            } else {
                return;
            }
        }
        
        if (el.equals(elementToFind) || isMethodMatch(el)) {
            String useThis = null;

            if (elementToFind!=null && elementToFind.getKind().isField()) {
                Scope scope = workingCopy.getTrees().getScope(path);
                for (Element ele : scope.getLocalElements()) {
                    if ((ele.getKind() == ElementKind.LOCAL_VARIABLE || ele.getKind() == ElementKind.PARAMETER) 
                            && ele.getSimpleName().toString().equals(newName)) {
                        if (tree.getKind() == Tree.Kind.MEMBER_SELECT) {
                            String isThis = ((MemberSelectTree) tree).getExpression().toString();
                            if (isThis.equals("this") || isThis.endsWith(".this")) { // NOI18N
                                break;
                            }
                        }
                        if (scope.getEnclosingClass().equals(elementToFind.getEnclosingElement())) 
                            useThis = "this."; // NOI18N
                        else 
                            useThis = elementToFind.getEnclosingElement().getSimpleName() + ".this."; // NOI18N
                        break;
                    }
                } 
            }
            Tree nju;
            if (useThis!=null) {
                nju = make.setLabel(tree, useThis + newName);
            } else {
                nju = make.setLabel(tree, newName);
            }
            rewrite(tree, nju);
        }
    }

    @Override
    public Tree visitMethod(MethodTree tree, Element p) {
        renameDeclIfMatch(getCurrentPath(), tree, p);
        return super.visitMethod(tree, p);
    }

    @Override
    public Tree visitClass(ClassTree tree, Element p) {
        renameDeclIfMatch(getCurrentPath(), tree, p);
        return super.visitClass(tree, p);
    }

    @Override
    public Tree visitVariable(VariableTree tree, Element p) {
        renameDeclIfMatch(getCurrentPath(), tree, p);
        return super.visitVariable(tree, p);
    }

    @Override
    public Tree visitTypeParameter(TypeParameterTree arg0, Element arg1) {
        renameDeclIfMatch(getCurrentPath(), arg0, arg1);
        return super.visitTypeParameter(arg0, arg1);
    }
    
    private void renameDeclIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(path))
            return;
        Element el = workingCopy.getTrees().getElement(path);
        if (el==null) {
            return;
        }
        if (el.equals(elementToFind) || isMethodMatch(el)) {
            Tree nju = make.setLabel(tree, newName);
            rewrite(tree, nju);
            return;
        }
    }
    
    private boolean isMethodMatch(Element method) {
        if (method.getKind() == ElementKind.METHOD && allMethods !=null) {
            for (ElementHandle<ExecutableElement> mh: allMethods) {
                ExecutableElement baseMethod =  mh.resolve(workingCopy);
                if (baseMethod==null) {
                    Logger.getLogger("org.netbeans.modules.refactoring.java").info("RenameTransformer cannot resolve " + mh);
                    continue;
                }
                if (baseMethod.equals(method) || workingCopy.getElements().overrides((ExecutableElement)method, baseMethod, workingCopy.getElementUtilities().enclosingTypeElement(baseMethod))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Renames the method (or constructor) parameter in comments. This method
     * considers comments before and inside the method declaration, and within
     * the method body.
     *
     * @param parameter the method or constructor parameter {@link Element}
     */
    private void renameParameterInMethodComments(final Element parameter) {
        final Tree method = workingCopy.getTrees().getPath(parameter).getParentPath().getLeaf();

        final String originalName = getOldSimpleName(parameter);
        final int methodStart = (int) workingCopy.getTrees().getSourcePositions()
                .getStartPosition(workingCopy.getCompilationUnit(), method);
        final TokenSequence<JavaTokenId> tokenSequence = workingCopy.getTokenHierarchy().tokenSequence(JavaTokenId.language());

        //renaming in comments before the method/constructor
        tokenSequence.move(methodStart);
        while (tokenSequence.movePrevious()) {
            final Token<JavaTokenId> token = tokenSequence.token();
            if (isComment(token)) {
                rewriteAllInComment(token.text().toString(), tokenSequence.offset(), originalName);
            } else if (token.id() != WHITESPACE) {
                break;
            }            
        }

        //renaming in comments within the method/constructor declaration and body
        final int methodEnd = (int) workingCopy.getTrees().getSourcePositions()
                .getEndPosition(workingCopy.getCompilationUnit(), method);

        tokenSequence.move(methodStart);
        while (tokenSequence.moveNext() && tokenSequence.offset() < methodEnd) {
            final Token<JavaTokenId> token = tokenSequence.token();
            if (isComment(token)) {
                rewriteAllInComment(token.text().toString(), tokenSequence.offset(), originalName);
            }
        }
    }

    /**
     * Checks if {@code token} represents a comment.
     * 
     * @param token the {@link Token} to check
     * @return {@code true} if {@code token} represents a line comment, block
     *   comment or javadoc; {@code false} otherwise.
     */
    private boolean isComment(final Token<JavaTokenId> token) {
        switch (token.id()) {
            case LINE_COMMENT:
            case BLOCK_COMMENT:
            case JAVADOC_COMMENT:
                return true;
            default:
                return false;
        }
    }

    /**
     * Changes all occurrences of {@code originalName} to the new name in the comment {@code text}.
     *
     * @param text the text of the comment token
     * @param offset the offset of the comment token
     * @param originalName the old name to change
     */
    private void rewriteAllInComment(final String text, final int offset, final String originalName) {
        for (int index = text.indexOf(originalName); index != -1; index = text.indexOf(originalName, index + 1)) {
            if (index > 0 && Character.isJavaIdentifierPart(text.charAt(index - 1))) {
                continue;
            }
            if ((index + originalName.length() < text.length()) && Character.isJavaIdentifierPart(text.charAt(index + originalName.length()))) {
                continue;
            }
            workingCopy.rewriteInComment(offset + index, originalName.length(), newName);
        }
    }
}
