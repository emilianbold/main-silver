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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.imports.ComputeImports;
import org.netbeans.modules.java.editor.imports.JavaFixAllImports;
import org.netbeans.modules.java.hints.errors.ImportClass.ImportCandidatesHolder;
import org.netbeans.modules.java.hints.infrastructure.CreatorBasedLazyFixList;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.infrastructure.Pair;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.preprocessorbridge.spi.ImportProcessor;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Lahoda
 */
public final class ImportClass implements ErrorRule<ImportCandidatesHolder> {
    
    static RequestProcessor WORKER = new RequestProcessor("ImportClassEnabler", 1);
    
    public ImportClass() {
    }
    
    public Set<String> getCodes() {
        return new HashSet<String>(Arrays.asList(
                "compiler.err.cant.resolve",
                "compiler.err.cant.resolve.location",
                "compiler.err.doesnt.exist",
                "compiler.err.not.stmt"));
    }
    
    public List<Fix> run(final CompilationInfo info, String diagnosticKey, final int offset, TreePath treePath, Data<ImportCandidatesHolder> data) {
        resume();

        int errorPosition = offset + 1; //TODO: +1 required to work OK, rethink
        
        if (errorPosition == (-1)) {
            ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.create errorPosition=-1"); //NOI18N
            
            return Collections.<Fix>emptyList();
        }

        TreePath path = info.getTreeUtilities().pathFor(errorPosition);
        
        if (path.getParentPath() != null && path.getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION) {
            //#86313:
            //if the error is in the type parameter, import should be proposed:
            MethodInvocationTree mit = (MethodInvocationTree) path.getParentPath().getLeaf();
            
            if (!mit.getTypeArguments().contains(path.getLeaf())) {
                return Collections.<Fix>emptyList();
            }
        }
        
        Token ident = null;
        
        try {
            ident = ErrorHintsProvider.findUnresolvedElementToken(info, offset);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.create ident={0}", ident); //NOI18N
        
        if (ident == null) {
            return Collections.<Fix>emptyList();
        }
        
        FileObject file = info.getFileObject();
        String simpleName = ident.text().toString();
        Pair<List<String>, List<String>> candidates = getCandidateFQNs(info, file, simpleName, data);

        //workaround for #118714 -- neverending import
        List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
        for (ImportTree it : imports) {
            String toString = it.getQualifiedIdentifier().toString();

            if (candidates != null) {
                List<String> a = candidates.getA();
                if (a != null && a.contains(toString)) {
                    return Collections.<Fix>emptyList();
                }
            }
        }

        if (isCancelled() || candidates == null) {
            ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.cancelled."); //NOI18N
            
            return CreatorBasedLazyFixList.CANCELLED;
        }

        TreePath imp = path;

        while (imp != null && imp.getLeaf().getKind() != Kind.IMPORT) {
            imp = imp.getParentPath();
        }

        List<String> filtered = candidates.getA();
        List<String> unfiltered = candidates.getB();
        List<Fix> fixes = new ArrayList<Fix>();
        
        if (unfiltered != null && filtered != null) {
            ReferencesCount referencesCount = ReferencesCount.get(info.getClasspathInfo());
        
            for (String fqn : unfiltered) {
                StringBuilder sort = new StringBuilder();
                
                sort.append("0001#");
                
                boolean prefered = filtered.contains(fqn);
                
                if (prefered)
                    sort.append("A#");
                else
                    sort.append("Z#");
                
                int order = Utilities.getImportanceLevel(referencesCount, ElementHandle.createTypeElementHandle(ElementKind.CLASS, fqn));
                String orderString = Integer.toHexString(order);
                
                sort.append("00000000".substring(0, 8 - orderString.length()));
                sort.append(orderString);
                sort.append('#');
                sort.append(fqn);
                
                fixes.add(new FixImport(file, fqn, sort.toString(), prefered, info, imp));
            }
        }
        
        ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.create finished."); //NOI18N

        return fixes;
    }
    
    public synchronized void cancel() {
        ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.cancel called."); //NOI18N
        
        cancelled = true;
        
        if (compImports != null) {
            compImports.cancel();
        }
    }
    
    public String getId() {
        return ImportClass.class.getName();
    }
    
    public String getDisplayName() {
        return "Add Import Fix";
    }
    
    public String getDescription() {
        return "Add Import Fix";
    }
    
    private synchronized void resume() {
        ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.resume called."); //NOI18N
        
        cancelled = false;
    }
    
    private synchronized boolean isCancelled() {
        return cancelled;
    }
    
    private boolean cancelled;
    private ComputeImports compImports;
    
    private synchronized void setComputeImports(ComputeImports compImports) {
        this.compImports = compImports;
    }
    
    public Pair<List<String>, List<String>> getCandidateFQNs(CompilationInfo info, FileObject file, String simpleName, Data<ImportCandidatesHolder> data) {
        ImportCandidatesHolder holder = data.getData();
        
        if (holder == null) {
            data.setData(holder = new ImportCandidatesHolder());
        }
        
        Pair<Map<String, List<String>>, Map<String, List<String>>> result = holder.getCandidates();
        
        if (result == null || result.getA() == null || result.getB() == null) {
            //compute imports:
            Map<String, List<String>> candidates = new HashMap<String, List<String>>();
            ComputeImports imp = new ComputeImports();
            
            setComputeImports(imp);
            
            ComputeImports.Pair<Map<String, List<TypeElement>>, Map<String, List<TypeElement>>> rawCandidates = imp.computeCandidates(info);
            
            setComputeImports(null);
            
            if (isCancelled() || rawCandidates == null) {
                ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.getCandidateFQNs cancelled, returning."); //NOI18N
                
                return null;
            }
            
            for (String sn : rawCandidates.a.keySet()) {
                List<String> c = new ArrayList<String>();
                
                for (TypeElement te : rawCandidates.a.get(sn)) {
                    c.add(te.getQualifiedName().toString());
                }
                
                candidates.put(sn, c);
            }
            
            Map<String, List<String>> notFilteredCandidates = new HashMap<String, List<String>>();
            
            for (String sn : rawCandidates.b.keySet()) {
                List<String> c = new ArrayList<String>();
                
                for (TypeElement te : rawCandidates.b.get(sn)) {
                    c.add(te.getQualifiedName().toString());
                }
                
                notFilteredCandidates.put(sn, c);
            }
            
            result = new Pair(candidates, notFilteredCandidates);
            
            holder.setCandidates(result);
        }
        
        List<String> candList = result.getA().get(simpleName);
        List<String> notFilteredCandList = result.getB().get(simpleName);
        
        return new Pair(candList, notFilteredCandList);
    }
    
    public static class ImportCandidatesHolder {
        private Pair<Map<String, List<String>>, Map<String, List<String>>> candidates;
        
        public Pair<Map<String, List<String>>, Map<String, List<String>>> getCandidates() {
            return candidates;
        }
        
        public void setCandidates(Pair<Map<String, List<String>>, Map<String, List<String>>> candidates) {
            this.candidates = candidates;
        }
    }
    
    static final class FixImport implements EnhancedFix {

        private final FileObject file;
        private final String fqn;
        private final String sortText;
        private final boolean isValid;
        private final @NullAllowed TreePathHandle importHandle;
        private final @NullAllowed String suffix;
        private final boolean statik;
        
        public FixImport(FileObject file, String fqn, String sortText, boolean isValid, CompilationInfo info, @NullAllowed TreePath imp) {
            this.file = file;
            this.fqn = fqn;
            this.sortText = sortText;
            this.isValid = isValid;
            if (imp != null) {
                this.importHandle = TreePathHandle.create(imp, info);
                String suffixLoc = ((ImportTree) imp.getLeaf()).getQualifiedIdentifier().toString();
                int dot = suffixLoc.indexOf('.');
                this.suffix = dot > (-1) ? suffixLoc.substring(dot) : suffixLoc;
                this.statik = ((ImportTree) imp.getLeaf()).isStatic();
            } else {
                this.importHandle = null;
                this.suffix = null;
                this.statik = false;
            }
        }

        @Messages("Change_to_import_X=Change to import {1}{0}")
        public String getText() {
            String displayName = importHandle == null ? NbBundle.getMessage(ImportClass.class, "Add_import_for_X", new Object[] {fqn}) : Bundle.Change_to_import_X(fqn + suffix, statik ? "static " : "");
            if (isValid)
                return displayName;
            else
                return JavaFixAllImports.NOT_VALID_IMPORT_HTML + displayName;
        }

        public ChangeInfo implement() throws IOException {
            JavaSource js = JavaSource.forFileObject(file);            
            
            Task task = new Task<WorkingCopy>() {
                    public void run(WorkingCopy copy) throws Exception {
                        if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0)
                           return;

                        if (importHandle != null) {
                            TreePath imp = importHandle.resolve(copy);

                            if (imp == null) {
                                Logger.getAnonymousLogger().warning(String.format("Attempt to change import for FQN: %s, but the import cannot be resolved in the current context", fqn));
                                return;
                            }

                            Tree mst = ((ImportTree) imp.getLeaf()).getQualifiedIdentifier();

                            while (mst != null && mst.getKind() == Kind.MEMBER_SELECT) {
                                mst = ((MemberSelectTree) mst).getExpression();
                            }

                            if (mst == null) {
                                copy.rewrite(imp.getLeaf(), copy.getTreeMaker().Identifier(fqn + suffix));
                                return;
                            }

                            copy.rewrite(mst, copy.getTreeMaker().Identifier(fqn));

                            return;
                        }
                  
                        Element te = copy.getElements().getTypeElement(fqn);
                        
                        if (te == null) {
                            Logger.getAnonymousLogger().warning(String.format("Attempt to fix import for FQN: %s, which does not have a TypeElement in currect context", fqn));
                            return ;
                        }
                        
                        CompilationUnitTree cut = GeneratorUtilities.get(copy).addImports(
                            copy.getCompilationUnit(),
                            Collections.singleton(te)
                        );
                        copy.rewrite(copy.getCompilationUnit(), cut);
                    }
                    
            };

            if (js != null) {
                js.runModificationTask(task).commit();
            } else {
                DataObject od = DataObject.find(file);
                EditorCookie ec = od.getLookup().lookup(EditorCookie.class);
                Document doc = ec != null ? ec.openDocument() : null;
                String topLevelLanguageMIMEType = doc != null ? NbEditorUtilities.getMimeType(doc) : null;
                if (topLevelLanguageMIMEType != null) {
                    Lookup lookup = MimeLookup.getLookup(MimePath.get(topLevelLanguageMIMEType));
                    Collection<? extends ImportProcessor> instances = lookup.lookupAll(ImportProcessor.class);

                    for (ImportProcessor importsProcesor : instances) {
                        importsProcesor.addImport(doc, fqn);
                    }
                }
            }
            return null;
        }
        
        @Override
        public int hashCode() {
            return fqn.hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof FixImport) {
                return fqn.equals(((FixImport) o).fqn);
            }
            
            return false;
        }

        public CharSequence getSortText() {
            return sortText;
        }
    }
}
