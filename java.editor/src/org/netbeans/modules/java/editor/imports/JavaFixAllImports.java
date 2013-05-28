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
package org.netbeans.modules.java.editor.imports;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.java.editor.semantic.SemanticHighlighter;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.editor.imports.ComputeImports.Pair;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Lahoda
 */
public class JavaFixAllImports {
    
    //-J-Dorg.netbeans.modules.java.editor.imports.JavaFixAllImports.invalid_import_html="<html><font color='#808080'>"
    public static final String NOT_VALID_IMPORT_HTML = System.getProperty(JavaFixAllImports.class.getName() + ".invalid_import_html", "");
    
    private static final String PREFS_KEY = JavaFixAllImports.class.getName();
    private static final String KEY_REMOVE_UNUSED_IMPORTS = "removeUnusedImports"; // NOI18N
    private static final JavaFixAllImports INSTANCE = new JavaFixAllImports();
    
    public static JavaFixAllImports getDefault() {
        return INSTANCE;
    }
    
    /** Creates a new instance of JavaFixAllImports */
    private JavaFixAllImports() {
    }
    
    public void fixAllImports(final FileObject fo, final JTextComponent target) {
        final AtomicBoolean cancel = new AtomicBoolean();
        final JavaSource javaSource = JavaSource.forFileObject(fo);
        final AtomicReference<ImportData> id = new AtomicReference<ImportData>();
        final Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy wc) {
                boolean removeUnusedImports;
                try {
                    wc.toPhase(Phase.RESOLVED);
                    if (cancel.get()) {
                        return;
                    }

                    final ImportData data = computeImports(wc);

                    if (cancel.get()) {
                        return;
                    }

                    if (data.shouldShowImportsPanel) {
                        if (!cancel.get()) {
                            id.set(data);
                        }
                    } else {
                        Preferences prefs = NbPreferences.forModule(JavaFixAllImports.class).node(PREFS_KEY);
                        
                        removeUnusedImports = prefs.getBoolean(KEY_REMOVE_UNUSED_IMPORTS, true);
                        performFixImports(wc, data, data.defaults, removeUnusedImports);
                    }
                } catch (IOException ex) {
                    //TODO: ErrorManager
                    ex.printStackTrace();
                }
            }
        };

        if (javaSource == null) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaFixAllImports.class, "MSG_CannotFixImports"));
        } else {
            ProgressUtils.runOffEventDispatchThread(new Runnable() {

                public void run() {
                    try {
                        ModificationResult mr = javaSource.runModificationTask(task);
                        GeneratorUtils.guardedCommit(target, mr);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, "Fix All Imports", cancel, false);

            if (id.get() != null && !cancel.get()) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        showFixImportsDialog(javaSource, target, id.get());
                    }
                });
            }
        }
    }
    
    private static List<TreePathHandle> getImportsFromSamePackage(WorkingCopy wc) {
        ImportVisitor v = new ImportVisitor(wc);
        v.scan(wc.getCompilationUnit(), null);
        return v.getImports();
    }

    private static class ImportVisitor extends TreePathScanner {
        private CompilationInfo info;
        private String currentPackage;
        private List<TreePathHandle> imports;

        private ImportVisitor (CompilationInfo info) {
            this.info = info;
            ExpressionTree pkg = info.getCompilationUnit().getPackageName();
            currentPackage = pkg != null ? pkg.toString() : "";
            imports = new ArrayList<TreePathHandle>();
        }

        @Override
        public Object visitImport(ImportTree node, Object d) {
            if (node.getQualifiedIdentifier().getKind() == Kind.MEMBER_SELECT) {
                ExpressionTree exp = ((MemberSelectTree) node.getQualifiedIdentifier()).getExpression();
                if (exp.toString().equals(currentPackage)) {
                    imports.add(TreePathHandle.create(getCurrentPath(), info));
                }
            }

            super.visitImport(node, null);
            return null;
        }

        List<TreePathHandle> getImports() {
            return imports;
        }
    }

    private static void performFixImports(WorkingCopy wc, ImportData data, CandidateDescription[] selections, boolean removeUnusedImports) throws IOException {
        //do imports:
        Set<Element> toImport = new HashSet<Element>();

        for (CandidateDescription cd : selections) {
            Element el = cd.toImport != null ? cd.toImport.resolve(wc) : null;

            if (el != null) {
                toImport.add(el);
            }
        }

        CompilationUnitTree cut = wc.getCompilationUnit();

        boolean someImportsWereRemoved = false;
        
        if (removeUnusedImports) {
            //compute imports to remove:
            List<TreePathHandle> unusedImports = SemanticHighlighter.computeUnusedImports(wc);
            unusedImports.addAll(getImportsFromSamePackage(wc));
            someImportsWereRemoved = !unusedImports.isEmpty();

            // make the changes to the source
            for (TreePathHandle handle : unusedImports) {
                TreePath path = handle.resolve(wc);

                assert path != null;

                cut = wc.getTreeMaker().removeCompUnitImport(cut, (ImportTree) path.getLeaf());
            }
        }

        if (!toImport.isEmpty()) {
            cut = GeneratorUtilities.get(wc).addImports(cut, toImport);
        }

        wc.rewrite(wc.getCompilationUnit(), cut);

        if( !data.shouldShowImportsPanel ) {
            String statusText;
            if( toImport.isEmpty() && !someImportsWereRemoved ) {
                Toolkit.getDefaultToolkit().beep();
                statusText = NbBundle.getMessage( JavaFixAllImports.class, "MSG_NothingToFix" ); //NOI18N
            } else if( toImport.isEmpty() && someImportsWereRemoved ) {
                statusText = NbBundle.getMessage( JavaFixAllImports.class, "MSG_UnusedImportsRemoved" ); //NOI18N
            } else {
                statusText = NbBundle.getMessage( JavaFixAllImports.class, "MSG_ImportsFixed" ); //NOI18N
            }
            StatusDisplayer.getDefault().setStatusText( statusText );
        }
    }

    private static ImportData computeImports(CompilationInfo info) {
        Pair<Map<String, List<Element>>, Map<String, List<Element>>> candidates = new ComputeImports().computeCandidates(info);

        Map<String, List<Element>> filteredCandidates = candidates.a;
        Map<String, List<Element>> notFilteredCandidates = candidates.b;

        int size = notFilteredCandidates.size();
        ImportData data = new ImportData(size);

        ReferencesCount referencesCount = ReferencesCount.get(info.getClasspathInfo());
        
        int index = 0;

        boolean shouldShowImportsPanel = false;

        for (String key : notFilteredCandidates.keySet()) {
            data.simpleNames[index] = key;

            List<Element> unfilteredVars = notFilteredCandidates.get(key);
            List<Element> filteredVars = filteredCandidates.get(key);


            shouldShowImportsPanel |= unfilteredVars.size() > 1;

            if (!unfilteredVars.isEmpty()) {
                boolean staticImports = true;
                for (Element e : unfilteredVars) {
                    if (e.getKind().isClass() || e.getKind().isInterface()) {
                        staticImports = false;
                    }
                }
                shouldShowImportsPanel |= staticImports;
                
                data.variants[index] = new CandidateDescription[staticImports ? unfilteredVars.size() + 1 : unfilteredVars.size()];

                int i = -1;
                int minImportanceLevel = Integer.MAX_VALUE;

                for (Element e : filteredVars) {
                    String displayName = ComputeImports.displayNameForImport(info, e);
                    Icon icon = ElementIcons.getElementIcon(e.getKind(), e.getModifiers());
                    data.variants[index][++i] = new CandidateDescription(displayName, icon, ElementHandle.create(e));
                    int level = Utilities.getImportanceLevel(info, referencesCount, e);
                    if (level < minImportanceLevel) {
                        data.defaults[index] = data.variants[index][i];
                        minImportanceLevel = level;
                    }
                }
                
                if (data.defaults[index] != null)
                    minImportanceLevel = Integer.MIN_VALUE;

                for (Element e : unfilteredVars) {
                    if (filteredVars.contains(e))
                        continue;

                    String displayName = NOT_VALID_IMPORT_HTML + ComputeImports.displayNameForImport(info, e);
                    Icon icon = ElementIcons.getElementIcon(e.getKind(), e.getModifiers());
                    data.variants[index][++i] = new CandidateDescription(displayName, icon, ElementHandle.create(e));
                    int level = Utilities.getImportanceLevel(info, referencesCount, e);
                    if (level < minImportanceLevel) {
                        data.defaults[index] = data.variants[index][i];
                        minImportanceLevel = level;
                    }
                }

                if (staticImports) {
                    data.variants[index][++i] = new CandidateDescription(NbBundle.getMessage(JavaFixAllImports.class, "FixDupImportStmts_DoNotImport"), //NOI18N
                                                                         ImageUtilities.loadImageIcon("org/netbeans/modules/java/editor/resources/error-glyph.gif", false), //NOI18N
                                                                         null);
                }
            } else {
                data.variants[index] = new CandidateDescription[1];
                data.variants[index][0] = new CandidateDescription(NbBundle.getMessage(JavaFixAllImports.class, "FixDupImportStmts_CannotResolve"), //NOI18N
                                                                   ImageUtilities.loadImageIcon("org/netbeans/modules/java/editor/resources/error-glyph.gif", false), //NOI18N
                                                                   null);
                data.defaults[index] = data.variants[index][0];
            }

            index++;
        }

        data.shouldShowImportsPanel = shouldShowImportsPanel;

        return data;
    }

    static final class ImportData {
        public final String[] simpleNames;
        public final CandidateDescription[][] variants;
        public final CandidateDescription[] defaults;
        public       boolean shouldShowImportsPanel;

        public ImportData(int size) {
            simpleNames = new String[size];
            variants = new CandidateDescription[size][];
            defaults = new CandidateDescription[size];
        }
    }

    private static final RequestProcessor WORKER = new RequestProcessor(JavaFixAllImports.class.getName(), 1);
    
    private static void showFixImportsDialog(final JavaSource js, final JTextComponent target, final ImportData data) {
        final Preferences prefs = NbPreferences.forModule(JavaFixAllImports.class).node(PREFS_KEY);
        final FixDuplicateImportStmts panel = new FixDuplicateImportStmts();

        panel.initPanel(data, prefs.getBoolean(KEY_REMOVE_UNUSED_IMPORTS, true));

        final JButton ok = new JButton("OK");
        final JButton cancel = new JButton("Cancel");
        final AtomicBoolean stop = new AtomicBoolean();
        DialogDescriptor dd = new DialogDescriptor(panel,
                                                   NbBundle.getMessage(JavaFixAllImports.class, "FixDupImportStmts_Title"), //NOI18N
                                                   true,
                                                   new Object[] {ok, cancel},
                                                   ok,
                                                   DialogDescriptor.DEFAULT_ALIGN,
                                                   HelpCtx.DEFAULT_HELP,
                                                   new ActionListener() {
                                                       public void actionPerformed(ActionEvent e) {}
                                                   },
                                                   true
                                                   );

        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok.setEnabled(false);
                final CandidateDescription[] selections = panel.getSelections();
                final boolean removeUnusedImports = panel.getRemoveUnusedImports();
                WORKER.post(new Runnable() {
                    public void run() {
                        try {
                            ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
                                public void run(WorkingCopy wc) throws Exception {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            cancel.setEnabled(false);
                                            ((JDialog)d).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                                        }
                                    });                                    
                                    wc.toPhase(Phase.RESOLVED);
                                    if (stop.get()) return;
                                    performFixImports(wc, data, selections, removeUnusedImports);
                                }
                            });
                            GeneratorUtils.guardedCommit(target, mr);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        prefs.putBoolean(KEY_REMOVE_UNUSED_IMPORTS, removeUnusedImports);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                d.setVisible(false);
                            }
                        });
                    }
                });
            }
        });

        cancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                stop.set(true);
                d.setVisible(false);
            }
        });

        d.setVisible(true);

        d.dispose();
    }

    static final class CandidateDescription {
        public final String displayName;
        public final Icon icon;
        public final ElementHandle<Element> toImport;
        public CandidateDescription(String displayName, Icon icon, ElementHandle<Element> toImport) {
            this.displayName = displayName;
            this.icon = icon;
            this.toImport = toImport;
        }
    }
}
