/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.coherence.generators.CodegenUtils;
import org.netbeans.modules.coherence.generators.Constants;
import org.netbeans.modules.coherence.generators.Implements;
import org.netbeans.modules.coherence.generators.NullParameterException;
import org.netbeans.modules.coherence.generators.ReadExternal;
import org.netbeans.modules.coherence.generators.WriteExternal;
import org.netbeans.modules.coherence.project.CoherenceProjectUtils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class POFMethodGenerator implements CodeGenerator {

    private static final Logger LOGGER = Logger.getLogger(POFMethodGenerator.class.getCanonicalName());
    private final JTextComponent textComp;
    private GeneratorFactory factory = null;
    private Implements pofImplements = null;
    private Constants pofConstants = null;
    private ReadExternal pofReadExternal = null;
    private WriteExternal pofWriteExternal = null;

    /**
     * Created new POF method generator.
     *
     * @param textComp {@code JTextComponent) of the related file
     */
    @NbBundle.Messages({
        "name.pom.method.generator.action=Coherence PortableObject Methods",
        "msg.overwrite.portable.object=PortableObject has already been implemented. Overwrite?"
    })
    private POFMethodGenerator(JTextComponent textComp) {
        this.textComp = textComp;
    }

    @Override
    public String getDisplayName() {
        return Bundle.name_pom_method_generator_action();
    }

    @Override
    public void invoke() {
        try {
            factory = GeneratorFactory.newGeneratorFactory();
            pofImplements = factory.newImplements();
            pofConstants = factory.newConstants();
            pofReadExternal = factory.newReadExternal();
            pofWriteExternal = factory.newWriteExternal();

            Document doc = textComp.getDocument();
            JavaSource javaSource = JavaSource.forDocument(doc);

            // Should not be did here but in the create method of the factory!
            if (isOkToModify(javaSource)) {
                modifyJava(javaSource);
            }

        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    protected boolean isOkToModify(JavaSource javaSource) throws IOException {

        final AtomicBoolean insertOverwriteCode = new AtomicBoolean(true);

        // Create a working Task to check the code
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            @Override
            public void cancel() {
            }

            @Override
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        if (pofImplements.isImplementsPresent(workingCopy, clazz)
                                || pofWriteExternal.isWriteExternalPresent(workingCopy, clazz)
                                || pofReadExternal.isReadExternalPresent(workingCopy, clazz)
                                || pofConstants.isConstantsPresent(workingCopy, clazz)) {
                            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(Bundle.msg_overwrite_portable_object());
                            Object ret = DialogDisplayer.getDefault().notify(desc);
                            insertOverwriteCode.set(ret == NotifyDescriptor.YES_OPTION);
                        }
                        break;
                    }
                }
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);

        return insertOverwriteCode.get();
    }

    protected void modifyJava(JavaSource javaSource) throws IOException {

        /*
         * Create Code Removal Task
         * ========================
         *
         * This task will be executed first to remove any existing generated code
         * we will only be executing this code and deleting the existing code if
         * the user has answer "Yes" to the overwrite question.
         *
         * Because this stage is done first the user will have to execute Undo
         * twice to retrieve the previous code.
         */
        CancellableTask removeTask = new CancellableTask<WorkingCopy>() {

            @Override
            public void cancel() {
            }

            @Override
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                /*
                 * Loop through the top level Types although we know that their
                 * will only be one this code is added for safety and once we have
                 * processed the top level class we will exist the loop.
                 */
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        ClassTree modifiedClazz = clazz;

                        /*
                         * Before executing a remove we will check is the corresponding
                         * generated code exists within the java source.
                         */
                        if (pofImplements.isImplementsPresent(workingCopy, clazz)) {
                            modifiedClazz = pofImplements.removeImplements(workingCopy, modifiedClazz, clazz);
                        }
                        if (pofWriteExternal.isWriteExternalPresent(workingCopy, clazz)) {
                            modifiedClazz = pofWriteExternal.removeWriteExternal(workingCopy, modifiedClazz, clazz);
                        }
                        if (pofReadExternal.isReadExternalPresent(workingCopy, clazz)) {
                            modifiedClazz = pofReadExternal.removeReadExternal(workingCopy, modifiedClazz, clazz);
                        }
                        if (pofConstants.isConstantsPresent(workingCopy, clazz)) {
                            modifiedClazz = pofConstants.removeConstants(workingCopy, modifiedClazz, clazz);
                        }
                        // Write Working Copy
                        workingCopy.rewrite(clazz, modifiedClazz);
                        break;
                    }
                }
            }
        };
        // Commit the Code Changes
        ModificationResult removeResult = javaSource.runModificationTask(removeTask);
        removeResult.commit();

        /*
         * Create a working Task to edit the code
         * ======================================
         *
         * This Task is executed to generate and insert the generated code into
         * the java source. At this point we will be using the Java Source Compiler
         * to do all the heavy lifting required to correctly format and declare
         * required imports etc.
         */
        CancellableTask insertTask = new CancellableTask<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                /*
                 * Loop through the top level Types although we know that their
                 * will only be one this code is added for safety and once we have
                 * processed the top level class we will exist the loop.
                 */
                try {
                    for (Tree typeDecl : cut.getTypeDecls()) {
                        if (Tree.Kind.CLASS == typeDecl.getKind()) {
                            ClassTree clazz = (ClassTree) typeDecl;
                            ClassTree modifiedClazz = clazz;

                            // Add Implements Clause
                            modifiedClazz = pofImplements.addImplements(workingCopy, modifiedClazz, clazz);
                            // Add Read External Method
                            modifiedClazz = pofReadExternal.addReadExternal(workingCopy, modifiedClazz, clazz);
                            // Add reader / writer index constants
                            modifiedClazz = pofConstants.addConstants(workingCopy, modifiedClazz, clazz);
                            // Add Write External Method
                            modifiedClazz = pofWriteExternal.addWriteExternal(workingCopy, modifiedClazz, clazz);

                            // Write Working Copy
                            workingCopy.rewrite(clazz, modifiedClazz);
                        }
                    }
                } catch (NullParameterException e) {
                    LOGGER.log(Level.WARNING, null, e);
                }
            }

            @Override
            public void cancel() {
            }
        };
        // Commit the Code Changes
        ModificationResult insertResult = javaSource.runModificationTask(insertTask);
        insertResult.commit();
    }

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            List<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController controller = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);

            path = path != null ? CodegenUtils.getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path) : null;
            if (component == null || controller == null || path == null) {
                return ret;
            }

            // check class
            TypeElement typeElement = (TypeElement) controller.getTrees().getElement(path);
            if (typeElement == null || !typeElement.getKind().isClass()) {
                return ret;
            }

            // check Coherence library availability
            Project owner = FileOwnerQuery.getOwner(controller.getFileObject());
            if (CoherenceProjectUtils.isCoherenceProject(owner)) {
                ret.add(new POFMethodGenerator(component));
            }
            return ret;
        }
    }
}
