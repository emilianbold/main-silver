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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.ExtractSuperclassRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.spi.DiffElement;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Plugin that implements the core functionality of Extract Super Class refactoring.
 *
 * @author Martin Matula, Jan Pokorsky
 */
public final class ExtractSuperclassRefactoringPlugin extends JavaRefactoringPlugin {
    /** Reference to the parent refactoring instance */
    private final ExtractSuperclassRefactoring refactoring;
    
    /** source class */
    private ElementHandle<TypeElement> classHandle;
        
    private String pkgName;

    /** Creates a new instance of ExtractSuperClassRefactoringPlugin
     * @param refactoring Parent refactoring instance.
     */
    ExtractSuperclassRefactoringPlugin(ExtractSuperclassRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        return JavaSource.forFileObject(refactoring.getSourceType().getFileObject());
    }

    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        // fire operation start on the registered progress listeners (2 step)
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 2);
        javac.toPhase(JavaSource.Phase.RESOLVED);
        try {
            TreePathHandle sourceType = refactoring.getSourceType();
            
            // check whether the element is valid
            Problem result = isElementAvail(sourceType, javac);
            if (result != null) {
                // fatal error -> don't continue with further checks
                return result;
            }
            
            // check whether the element is an unresolved class
            Element sourceElm = sourceType.resolveElement(javac);
            result = JavaPluginUtils.isSourceElement(sourceElm, javac);
            if (result != null) {
                return result;
            }
            if (sourceElm == null) {
                // fatal error -> return
                return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
            }

            if (sourceElm.getKind() != ElementKind.CLASS) {
                return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ExtractSC_MustBeClass"));
            }
            
            classHandle = ElementHandle.<TypeElement>create((TypeElement) sourceElm);
            
            PackageElement pkgElm = (PackageElement) javac.getElementUtilities().outermostTypeElement(sourceElm).getEnclosingElement();
            pkgName = pkgElm.getQualifiedName().toString();
            
            // increase progress (step 1)
            fireProgressListenerStep();
            
            // all checks passed -> return null
            return null;
        } finally {
            // fire operation end on the registered progress listeners
            fireProgressListenerStop();
        }
    }
    
    @Override
    public Problem fastCheckParameters() {
        Problem result = null;
        
        String newName = refactoring.getSuperClassName();
        
        if (!Utilities.isJavaIdentifier(newName)) {
            result = createProblem(result, true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_InvalidIdentifier", newName)); // NOI18N
            return result;
        }
        
        FileObject primFile = refactoring.getSourceType().getFileObject();
        FileObject folder = primFile.getParent();
        FileObject[] children = folder.getChildren();
        for (FileObject child: children) {
            if (!child.isVirtual() && child.getName().equals(newName) && "java".equals(child.getExt())) { // NOI18N
                result = createProblem(result, true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ClassClash", newName, pkgName)); // NOI18N
                return result;
            }
        }

        return null;
    }

    @Override
    public Problem checkParameters() {
        MemberInfo[] members = refactoring.getMembers();
        if (members.length == 0) {
            return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ExtractSuperClass_MembersNotAvailable")); // NOI18N);
        }
        return super.checkParameters();

    }
    
    @Override
    protected Problem checkParameters(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        
        TypeElement sourceType = (TypeElement) refactoring.getSourceType().resolveElement(javac);
        assert sourceType != null;
        
        Set<? extends Element> members = new HashSet<Element>(sourceType.getEnclosedElements());
        
        fireProgressListenerStart(AbstractRefactoring.PARAMETERS_CHECK, refactoring.getMembers().length);
        try {
            for (MemberInfo info : refactoring.getMembers()) {
                Problem p = null;
                switch(info.getGroup()) {
                case FIELD:
                    @SuppressWarnings("unchecked")
                    ElementHandle<VariableElement> vehandle = (ElementHandle<VariableElement>) info.getElementHandle();
                    VariableElement field = vehandle.resolve(javac);
                    p = checkFieldParameter(javac, field, members);
                    break;
                case METHOD:
                    @SuppressWarnings("unchecked")
                    ElementHandle<ExecutableElement> eehandle = (ElementHandle<ExecutableElement>) info.getElementHandle();
                    ExecutableElement method = eehandle.resolve(javac);
                    p = checkMethodParameter(javac, method, members);
                    break;
                }

                if (p != null) {
                    return p;
                }
                
                fireProgressListenerStep();
            }
        } finally {
            fireProgressListenerStop();
        }

        // XXX check refactoring.getImplements()

        return null;
    }
    
    private Problem checkFieldParameter(CompilationController javac, VariableElement elm, Set<? extends Element> members) throws IOException {
        if (elm == null) {
            return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
        }
        if (javac.getElementUtilities().isSynthetic(elm) || elm.getKind() != ElementKind.FIELD) {
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractSuperClass_UnknownMember", // NOI18N
                    elm.toString()));
        }
        if (!members.contains(elm)) {
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractSuperClass_UnknownMember", // NOI18N
                    elm.toString()));
        }
//        Set<Modifier> mods = elm.getModifiers();
//        if (mods.contains(Modifier.PUBLIC) && mods.contains(Modifier.STATIC) && mods.contains(Modifier.FINAL)) {
//            VariableTree tree = (VariableTree) javac.getTrees().getTree(elm);
//            if (tree.getInitializer() != null) {
//                continue;
//            }
//        }
//        return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_WrongModifiers", elm.getSimpleName().toString())); // NOI18N
        return null;
    }
    
    private Problem checkMethodParameter(CompilationController javac, ExecutableElement elm, Set<? extends Element> members) throws IOException {
        if (elm == null) {
            return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
        }
        if (javac.getElementUtilities().isSynthetic(elm) || elm.getKind() != ElementKind.METHOD) {
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractSuperClass_UnknownMember", // NOI18N
                    elm.toString()));
        }
        if (!members.contains(elm)) {
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractSuperClass_UnknownMember", // NOI18N
                    elm.toString()));
        }
//        Set<Modifier> mods = elm.getModifiers();
//        if (!mods.contains(Modifier.PUBLIC) || mods.contains(Modifier.STATIC)) {
//            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_WrongModifiers", elm.getSimpleName().toString())); // NOI18N
//        }
        return null;
        
    }

    @Override
    public Problem prepare(RefactoringElementsBag bag) {
        FileObject primFile = refactoring.getSourceType().getFileObject();
        try {
            UpdateClassTask.create(bag, primFile, refactoring, classHandle);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }
    
    private static List<TypeMirror> findUsedGenericTypes(CompilationInfo javac, TypeElement javaClass,ExtractSuperclassRefactoring refactoring) {
        List<TypeMirror> typeArgs = JavaRefactoringUtils.elementsToTypes(javaClass.getTypeParameters());
        if (typeArgs.isEmpty()) {
            return typeArgs;
        }
        
        Types typeUtils = javac.getTypes();
        Set<TypeMirror> used = Collections.newSetFromMap(new IdentityHashMap<TypeMirror, Boolean>());
        
        // check super class
        TypeMirror superClass = javaClass.getSuperclass();
        RefactoringUtils.findUsedGenericTypes(typeUtils, typeArgs, used, superClass);
        
        MemberInfo[] members = refactoring.getMembers();
        for (int i = 0; i < members.length && !typeArgs.isEmpty(); i++) {
            if (members[i].getGroup() == MemberInfo.Group.METHOD) {
            // check methods
                @SuppressWarnings("unchecked")
                ElementHandle<ExecutableElement> handle = (ElementHandle<ExecutableElement>) members[i].getElementHandle();
                ExecutableElement elm = handle.resolve(javac);
            
                RefactoringUtils.findUsedGenericTypes(typeUtils, typeArgs, used, elm.getReturnType());

                for (Iterator<? extends VariableElement> paramIter = elm.getParameters().iterator(); paramIter.hasNext() && !typeArgs.isEmpty();) {
                    VariableElement param = paramIter.next();
                    RefactoringUtils.findUsedGenericTypes(typeUtils, typeArgs, used, param.asType());
                }
            } else if (members[i].getGroup() == MemberInfo.Group.FIELD) {
                if (members[i].getModifiers().contains(Modifier.STATIC)) {
                    // do not check since static fields cannot use type parameter of the enclosing class
                    continue;
                }
                @SuppressWarnings("unchecked")
                ElementHandle<VariableElement> handle = (ElementHandle<VariableElement>) members[i].getElementHandle();
                VariableElement elm = handle.resolve(javac);
                TypeMirror asType = elm.asType();
                RefactoringUtils.findUsedGenericTypes(typeUtils, typeArgs, used, asType);
            } else if (members[i].getGroup() == MemberInfo.Group.IMPLEMENTS) {
                // check implements
                TypeMirrorHandle handle = (TypeMirrorHandle) members[i].getElementHandle();
                TypeMirror implemetz = handle.resolve(javac);
                RefactoringUtils.findUsedGenericTypes(typeUtils, typeArgs, used, implemetz);
            }
            // do not check fields since static fields cannot use type parameter of the enclosing class
        }
        
        return RefactoringUtils.filterTypes(typeArgs, used);
    }
    
    private final static class UpdateClassTask implements CancellableTask<WorkingCopy> {
        private final ExtractSuperclassRefactoring refactoring;
        private final ElementHandle<TypeElement> sourceType;
        
        private UpdateClassTask(ExtractSuperclassRefactoring refactoring, ElementHandle<TypeElement> sourceType) {
            this.sourceType = sourceType;
            this.refactoring = refactoring;
        }
        
        public static void create(RefactoringElementsBag bag, FileObject fo,ExtractSuperclassRefactoring refactoring, ElementHandle<TypeElement> sourceType) throws IOException {
            JavaSource js = JavaSource.forFileObject(fo);
            ModificationResult modification = js.runModificationTask(new UpdateClassTask(refactoring, sourceType));
            List<? extends ModificationResult.Difference> diffs = modification.getDifferences(fo);
            for (ModificationResult.Difference diff : diffs) {
                bag.add(refactoring, DiffElement.create(diff, fo, modification));
            }
            bag.registerTransaction(createTransaction(Collections.singletonList(modification)));
        }
        
        @Override
        public void cancel() {
        }

        @Override
        public void run(WorkingCopy wc) throws Exception {
            wc.toPhase(JavaSource.Phase.RESOLVED);
            createCU(wc);
            TypeElement clazz = this.sourceType.resolve(wc);
            assert clazz != null;
            ClassTree classTree = wc.getTrees().getTree(clazz);
            TreeMaker make = wc.getTreeMaker();
            // fake interface since interface file does not exist yet
            Tree superClassTree;
            List<TypeMirror> typeParams = findUsedGenericTypes(wc, clazz, refactoring);
            if (typeParams.isEmpty()) {
                superClassTree = make.Identifier(refactoring.getSuperClassName());
            } else {
                List<ExpressionTree> typeParamTrees = new ArrayList<ExpressionTree>(typeParams.size());
                for (TypeMirror typeParam : typeParams) {
                    Tree t = make.Type(typeParam);
                    typeParamTrees.add((ExpressionTree) t);
                }
                superClassTree = make.ParameterizedType(
                        make.Identifier(refactoring.getSuperClassName()),
                        typeParamTrees
                        );
            }
            
            Set<Tree> members2Remove = new HashSet<Tree>();
            Set<Tree> interfaces2Remove = new HashSet<Tree>();
            
            members2Remove.addAll(getMembers2Remove(wc, refactoring.getMembers()));
            interfaces2Remove.addAll(getImplements2Remove(wc, refactoring.getMembers(), clazz));
            
            // filter out obsolete members
            List<Tree> members2Add = new ArrayList<Tree>();
            for (Tree tree : classTree.getMembers()) {
                if (!members2Remove.contains(tree)) {
                    members2Add.add(tree);
                }
            }
            // filter out obsolete implements trees
            List<Tree> impls2Add = resolveImplements(classTree.getImplementsClause(), interfaces2Remove);

            ClassTree nc;
            nc = make.Class(
                    classTree.getModifiers(),
                    classTree.getSimpleName(),
                    classTree.getTypeParameters(),
                    superClassTree,
                    impls2Add,
                    members2Add);
            
            wc.rewrite(classTree, nc);
        }
        
        private List<Tree> getMembers2Remove(CompilationInfo javac,MemberInfo[] members) {
            if (members == null || members.length == 0) {
                return Collections.<Tree>emptyList();
            }
            List<Tree> result = new ArrayList<Tree>(members.length);
            for (MemberInfo member : members) {
                if (member.getGroup() == MemberInfo.Group.FIELD) {
                    @SuppressWarnings("unchecked")
                    ElementHandle<VariableElement> handle = (ElementHandle<VariableElement>) member.getElementHandle();
                    VariableElement elm = handle.resolve(javac);
                    assert elm != null;
                    Tree t = javac.getTrees().getTree(elm);
                    assert t != null;
                    result.add(t);
                } else if (member.getGroup() == MemberInfo.Group.METHOD && !member.isMakeAbstract()) {
                    @SuppressWarnings("unchecked")
                    ElementHandle<ExecutableElement> handle = (ElementHandle<ExecutableElement>) member.getElementHandle();
                    ExecutableElement elm = handle.resolve(javac);
                    assert elm != null;
                    Tree t = javac.getTrees().getTree(elm);
                    assert t != null;
                    result.add(t);
                }
                
            }

            return result;
        }
        
        private List<Tree> getImplements2Remove(CompilationInfo javac,MemberInfo[] members, TypeElement clazz) {
            if (members == null || members.length == 0) {
                return Collections.<Tree>emptyList();
            }
            
            // resolve members to remove
            List<TypeMirror> memberTypes = new ArrayList<TypeMirror>(members.length);
            for (MemberInfo member : members) {
                if (member.getGroup() == MemberInfo.Group.IMPLEMENTS) {
                    TypeMirrorHandle handle = (TypeMirrorHandle) member.getElementHandle();
                    TypeMirror tm = handle.resolve(javac);
                    memberTypes.add(tm);
                }
            }

            
            ClassTree classTree = javac.getTrees().getTree(clazz);
            List<Tree> result = new ArrayList<Tree>();
            Types types = javac.getTypes();
            
            // map TypeMirror to Tree
            for (Tree tree : classTree.getImplementsClause()) {
                TreePath path = javac.getTrees().getPath(javac.getCompilationUnit(), tree);
                TypeMirror existingTM = javac.getTrees().getTypeMirror(path);
                
                for (TypeMirror tm : memberTypes) {
                    if (types.isSameType(tm, existingTM)) {
                        result.add(tree);
                        break;
                    }
                }
            }

            return result;
        }
        
        private static List<Tree> resolveImplements(List<? extends Tree> allImpls, Set<Tree> impls2Remove) {
            List<Tree> ret;
            if (allImpls == null) {
                ret = new ArrayList<Tree>(1);
            } else {
                ret = new ArrayList<Tree>(allImpls.size() + 1);
                ret.addAll(allImpls);
            }
            
            if (impls2Remove != null && !impls2Remove.isEmpty()) {
                ret.removeAll(impls2Remove);
            }
            return ret;
        }
        
        
        private void createCU(WorkingCopy wc) throws Exception {
            wc.toPhase(JavaSource.Phase.RESOLVED);
            boolean makeAbstract = false;
            TreeMaker make = wc.getTreeMaker();
            GeneratorUtilities genUtils = GeneratorUtilities.get(wc);
            
            // add type parameters
            List<TypeMirror> typeParams = findUsedGenericTypes(wc, sourceType.resolve(wc), refactoring);
            List<TypeParameterTree> newTypeParams = new ArrayList<TypeParameterTree>(typeParams.size());
            // lets retrieve param type trees from origin class since it is
            // almost impossible to create them via TreeMaker
            TypeElement sourceTypeElm = sourceType.resolve(wc);
            for (TypeParameterElement typeParam : sourceTypeElm.getTypeParameters()) {
                TypeMirror origParam = typeParam.asType();
                for (TypeMirror newParam : typeParams) {
                    if (wc.getTypes().isSameType(origParam, newParam)) {
                        Tree t = wc.getTrees().getTree(typeParam);
                        if (t.getKind() == Tree.Kind.TYPE_PARAMETER) {
                            TypeParameterTree typeParamTree = (TypeParameterTree) t;
                            if (!typeParamTree.getBounds().isEmpty()) {
                                typeParamTree = (TypeParameterTree) genUtils.importFQNs(t);
                            }
                            newTypeParams.add(typeParamTree);
                        }
                    }
                }
            }

            // add fields, methods and implements
            List<Tree> members = new ArrayList<Tree>();
            List <Tree> implementsList = new ArrayList<Tree>();
            
            addConstructors(wc, sourceTypeElm, members);
            
            for (MemberInfo member : refactoring.getMembers()) {
                if (member.getGroup() == MemberInfo.Group.FIELD) {
                    @SuppressWarnings("unchecked")
                    ElementHandle<VariableElement> handle = (ElementHandle<VariableElement>) member.getElementHandle();
                    VariableElement elm = handle.resolve(wc);
                    VariableTree tree = (VariableTree) wc.getTrees().getTree(elm);
                    VariableTree copy = genUtils.importComments(tree, wc.getTrees().getPath(elm).getCompilationUnit());
                    copy = genUtils.importFQNs(copy);
                    ModifiersTree modifiers = copy.getModifiers();
                    if(modifiers.getFlags().contains(Modifier.PRIVATE)) {
                        modifiers = make.removeModifiersModifier(modifiers, Modifier.PRIVATE);
                        modifiers = make.addModifiersModifier(modifiers, Modifier.PROTECTED);
                        copy = make.Variable(modifiers, copy.getName(), copy.getType(), copy.getInitializer());
                    }
                    members.add(copy);
                } else if (member.getGroup() == MemberInfo.Group.METHOD) {
                    @SuppressWarnings("unchecked")
                    ElementHandle<ExecutableElement> handle = (ElementHandle<ExecutableElement>) member.getElementHandle();
                    ExecutableElement elm = handle.resolve(wc);
                    MethodTree methodTree = wc.getTrees().getTree(elm);
                    ModifiersTree modifiers = methodTree.getModifiers();
                        if(modifiers.getFlags().contains(Modifier.PRIVATE)) {
                            modifiers = make.removeModifiersModifier(modifiers, Modifier.PRIVATE);
                            modifiers = make.addModifiersModifier(modifiers, Modifier.PROTECTED);
                        }
                    methodTree = genUtils.importComments(methodTree, wc.getTrees().getPath(elm).getCompilationUnit());
                    if (member.isMakeAbstract() && !elm.getModifiers().contains(Modifier.ABSTRACT)) {
                        methodTree = make.Method(
                                RefactoringUtils.makeAbstract(make, modifiers),
                                methodTree.getName(),
                                methodTree.getReturnType(),
                                methodTree.getTypeParameters(),
                                methodTree.getParameters(),
                                methodTree.getThrows(),
                                (BlockTree) null,
                                null);
                    } else {
                        methodTree = make.Method(modifiers,
                                methodTree.getName(),
                                methodTree.getReturnType(),
                                methodTree.getTypeParameters(),
                                methodTree.getParameters(),
                                methodTree.getThrows(),
                                methodTree.getBody(),
                                (ExpressionTree) methodTree.getDefaultValue());
                    }
                    methodTree = genUtils.importFQNs(methodTree);
                    RefactoringUtils.copyJavadoc(elm, methodTree, wc);
                    makeAbstract |= methodTree.getModifiers().getFlags().contains(Modifier.ABSTRACT);
                    members.add(methodTree);
                } else if (member.getGroup() == MemberInfo.Group.IMPLEMENTS) {
                    TypeMirrorHandle handle = (TypeMirrorHandle) member.getElementHandle();
                    // XXX check if interface is not aready there; the templates might be changed by user :-(
                    TypeMirror implMirror = handle.resolve(wc);
                    implementsList.add(make.Type(implMirror));
                    // XXX needs more granular check
                    makeAbstract |= true;
                }
            }

            // create superclass
            Tree superClass = makeSuperclass(make, sourceTypeElm);

            makeAbstract |= ((DeclaredType) sourceTypeElm.getSuperclass()).asElement().getModifiers().contains(Modifier.ABSTRACT);
            
            ModifiersTree classModifiersTree = make.Modifiers(makeAbstract?EnumSet.of(Modifier.PUBLIC, Modifier.ABSTRACT):EnumSet.of(Modifier.PUBLIC));
            
            // create new class
            ClassTree newClassTree = make.Class(
                    classModifiersTree,
                    refactoring.getSuperClassName(),
                    newTypeParams,
                    superClass,
                    implementsList,
                    Collections.<Tree>emptyList());
            
            newClassTree = GeneratorUtilities.get(wc).insertClassMembers(newClassTree, members);
            
            FileObject fileObject = refactoring.getSourceType().getFileObject();
            FileObject sourceRoot = ClassPath.getClassPath(fileObject, ClassPath.SOURCE).findOwnerRoot(fileObject);
            String relativePath = FileUtil.getRelativePath(sourceRoot, fileObject.getParent()) + "/" + refactoring.getSuperClassName() + ".java";
            CompilationUnitTree cu = JavaPluginUtils.createCompilationUnit(sourceRoot, relativePath, newClassTree, wc, make);
            wc.rewrite(null, cu);
            
        }
        
        // --- helper methods ----------------------------------
        
        private static Tree makeSuperclass(TreeMaker make, TypeElement clazz) {
            DeclaredType supType = (DeclaredType) clazz.getSuperclass();
            TypeElement supEl = (TypeElement) supType.asElement();
            return supEl.getSuperclass().getKind() == TypeKind.NONE
                    ? null
                    : make.Type(supType);
        }
        
        /* in case there are constructors delegating to old superclass it is necessery to create delegates in new superclass */
        private static void addConstructors(final WorkingCopy javac, final TypeElement origClass, final List<Tree> members) {
            final TreeMaker make = javac.getTreeMaker();
            final GeneratorUtilities genUtils = GeneratorUtilities.get(javac);
            
            // cache of already resolved constructors
            final Set<Element> added = new HashSet<Element>();
            for (ExecutableElement constr : ElementFilter.constructorsIn(origClass.getEnclosedElements())) {
                if (javac.getElementUtilities().isSynthetic(constr)) {
                    continue;
                }
                
                TreePath path = javac.getTrees().getPath(constr);
                MethodTree mc = (MethodTree) (path != null? path.getLeaf(): null);
                if (mc != null) {
                    for (StatementTree stmt : mc.getBody().getStatements()) {
                        // search super(...); statement
                        if (stmt.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                            ExpressionStatementTree estmt = (ExpressionStatementTree) stmt;
                            boolean isSyntheticSuper = javac.getTreeUtilities().isSynthetic(javac.getTrees().getPath(path.getCompilationUnit(), estmt));
                            ExpressionTree expr = estmt.getExpression();
                            TreePath expath = javac.getTrees().getPath(path.getCompilationUnit(), expr);
                            Element el = javac.getTrees().getElement(expath);
                            if (el != null && el.getKind() == ElementKind.CONSTRUCTOR && added.add(el)) {
                                ExecutableElement superclassConstr = (ExecutableElement) el;
                                MethodInvocationTree invk = (MethodInvocationTree) expr;
                                // create constructor block with super call
                                BlockTree block = isSyntheticSuper
                                        ? make.Block(Collections.<StatementTree>emptyList(), false)
                                        : make.Block(Collections.<StatementTree>singletonList(
                                            make.ExpressionStatement(
                                                make.MethodInvocation(
                                                    Collections.<ExpressionTree>emptyList(),
                                                    invk.getMethodSelect(),
                                                    params2Arguments(make, superclassConstr.getParameters())
                                                ))), false);
                                // create constructor
                                MethodTree newConstr = make.Method(superclassConstr, block);

                                newConstr = removeRuntimeExceptions(javac, superclassConstr, make, newConstr);

                                newConstr = genUtils.importFQNs(newConstr);
                                members.add(newConstr);
                            }
                            
                        }
                        // take just first statement super(...)
                        break;
                    }
                }
            }
        }

        private static MethodTree removeRuntimeExceptions(final WorkingCopy javac, ExecutableElement superclassConstr, final TreeMaker make, MethodTree newConstr) {
            int i = 0;
            TypeMirror rte = javac.getElements().getTypeElement("java.lang.RuntimeException").asType(); //NOI18N
            ArrayList<Integer> rtes = new ArrayList<Integer>();
            for (TypeMirror throwz : superclassConstr.getThrownTypes()) {
                if (javac.getTypes().isSubtype(throwz, rte)) {
                    rtes.add(i);
                }
                i++;
            }
            for (int j = rtes.size()-1; j >= 0; j--) {
                newConstr = make.removeMethodThrows(newConstr, rtes.get(j));
            }
            return newConstr;
        }

        
        private static List<? extends ExpressionTree> params2Arguments(TreeMaker make, List<? extends VariableElement> params) {
            if (params.isEmpty()) {
                return Collections.<ExpressionTree>emptyList();
            }
            List<ExpressionTree> args = new ArrayList<ExpressionTree>(params.size());
            for (VariableElement param : params) {
                args.add(make.Identifier(param.getSimpleName()));
            }
            return args;
        }        
    }
}
