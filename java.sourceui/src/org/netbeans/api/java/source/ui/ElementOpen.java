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
 * Portions Copyrighted 2007-2009 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.ui;

import com.sun.source.tree.*;
import com.sun.source.util.TreePathScanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.java.BinaryElementOpen;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

import javax.lang.model.element.Element;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.ElementKind;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/** Utility class for opening elements in editor.
 *
 * @author Jan Lahoda
 */
public final class ElementOpen {
    private static Logger log = Logger.getLogger(ElementOpen.class.getName());

    private ElementOpen() {
    }
    
    /**
     * Opens {@link Element} corresponding to the given {@link ElementHandle}.
     * 
     * @param cpInfo ClasspathInfo which should be used for the search
     * @param el     declaration to open
     * @return true  if and only if the declaration was correctly opened,
     *                false otherwise
     * @since 1.5
     */
    public static boolean open(final ClasspathInfo cpInfo, final ElementHandle<? extends Element> el) {
        final AtomicBoolean cancel = new AtomicBoolean();
        if (SwingUtilities.isEventDispatchThread() && !JavaSourceAccessor.holdsParserLock()) {
            final boolean[] result = new boolean[1];
            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    public void run() {
                        result[0] = open(cpInfo, el, cancel);
                    }
                },
                NbBundle.getMessage(ElementOpen.class, "TXT_CalculatingDeclPos"),
                cancel,
                false);
            return result[0];
        } else {
            return open(cpInfo, el, cancel);
        }
    }
    
    private static boolean open(final ClasspathInfo cpInfo, final ElementHandle<? extends Element> el, AtomicBoolean cancel) {
        FileObject fo = SourceUtils.getFile(el, cpInfo);
        if (fo != null && fo.isFolder()) {
            fo = fo.getFileObject("package-info.java"); // NOI18N
        }
        Object[] openInfo = fo != null ? getOpenInfo(fo, el, cancel) : null;
        if (cancel.get()) return false;
        if (openInfo != null) {
            assert openInfo[0] instanceof FileObject;
            assert openInfo[1] instanceof Integer;
            return doOpen((FileObject) openInfo[0], (Integer) openInfo[1]);
        }

        BinaryElementOpen beo = Lookup.getDefault().lookup(BinaryElementOpen.class);

        if (beo != null) {
            return beo.open(cpInfo, el, cancel);
        } else {
            return false;
        }        
    }
    
    /**
     * Opens given {@link Element}.
     * 
     * @param cpInfo ClasspathInfo which should be used for the search
     * @param el    declaration to open
     * @return true if and only if the declaration was correctly opened,
     *                false otherwise
     */
    public static boolean open(final ClasspathInfo cpInfo, final Element el) {
        return open(cpInfo, ElementHandle.create(el));
    }
    
    /**
     * Opens given {@link Element}.
     * 
     * @param toSearch fileobject whose {@link ClasspathInfo} will be used
     * @param toOpen   {@link ElementHandle} of the element which should be opened.
     * @return true if and only if the declaration was correctly opened,
     *                false otherwise
     */
    public static boolean open(
            @NonNull final FileObject toSearch,
            @NonNull final ElementHandle<? extends Element> toOpen) {
        final AtomicBoolean cancel = new AtomicBoolean();
        if (SwingUtilities.isEventDispatchThread() && !JavaSourceAccessor.holdsParserLock()) {
            final boolean[] result = new boolean[1];
            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    public void run() {
                        result[0] = open(toSearch, toOpen, cancel);
                    }
                },
                NbBundle.getMessage(ElementOpen.class, "TXT_CalculatingDeclPos"),
                cancel,
                false);
            return result[0];
        } else {
            return open(toSearch, toOpen, cancel);
        }
    }
    
    private static boolean open(
            @NonNull final FileObject toSearch,
            @NonNull final ElementHandle<? extends Element> toOpen,
            @NonNull final AtomicBoolean cancel) {
        Parameters.notNull("toSearch", toSearch);   //NOI18N
        Parameters.notNull("toOpen", toOpen);       //NOI18N

        Object[] openInfo = getOpenInfo (toSearch, toOpen, cancel);
        if (cancel.get()) return false;
        if (openInfo != null) {
            assert openInfo[0] instanceof FileObject;
            assert openInfo[1] instanceof Integer;
            return doOpen((FileObject)openInfo[0],(Integer)openInfo[1]);
        }
        
        BinaryElementOpen beo = Lookup.getDefault().lookup(BinaryElementOpen.class);

        if (beo != null) {
            return beo.open(ClasspathInfo.create(toSearch), toOpen, cancel);
        } else {
            return false;
        }
    }
    
    
    // Private methods ---------------------------------------------------------
        
    private static Object[] getOpenInfo(final FileObject fo, final ElementHandle<? extends Element> handle, AtomicBoolean cancel) {
        assert fo != null;
        
        try {
            int offset = getOffset(fo, handle, cancel);
            return new Object[] {fo, offset};
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }


    @SuppressWarnings("deprecation")
    private static boolean doOpen(FileObject fo, int offset) {
        if (offset == -1) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(ElementOpen.class, "WARN_ElementNotFound"), 
                    StatusDisplayer.IMPORTANCE_ANNOTATION);
        }
        return UiUtils.open(fo, offset);
    }

    private static final int AWT_TIMEOUT = 1000;
    private static final int NON_AWT_TIMEOUT = 2000;

    private static int getOffset(final FileObject fo, final ElementHandle<? extends Element> handle, final AtomicBoolean cancel) throws IOException {
        final int[]  result = new int[] {-1};
        
        final JavaSource js = JavaSource.forFileObject(fo);
        if (js != null) {
            final Task<CompilationController> t = new Task<CompilationController>() {
                public @Override void run(CompilationController info) throws IOException {
                    if (cancel.get()) {
                        return;
                    }
                    try {
                        info.toPhase(JavaSource.Phase.RESOLVED);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                    Element el = handle.resolve(info);
                    if (el == null) {
                        if (!SourceUtils.isScanInProgress()) {
                            log.severe("Cannot resolve " + handle + ". " + info.getClasspathInfo());
                        }
                        return;
                    }
                    
                    if (el.getKind() == ElementKind.PACKAGE) {
                        // FindDeclarationVisitor does not work since there is no visitPackage.
                        // Imprecise but should usually work:
                        Matcher m = Pattern.compile("(?m)^package (.+);$").matcher(fo.asText(/*FileEncodingQuery.getEncoding(fo).name()*/)); // NOI18N
                        if (m.find()) {
                            result[0] = m.start();
                        }
                        return;
                    }

                    FindDeclarationVisitor v = new FindDeclarationVisitor(el, info);

                    CompilationUnitTree cu = info.getCompilationUnit();

                    v.scan(cu, null);
                    Tree elTree = v.declTree;

                    if (elTree != null)
                        result[0] = (int)info.getTrees().getSourcePositions().getStartPosition(cu, elTree);
                    }
            };

            js.runUserActionTask(t, true);
        }
        return result[0];
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private static class FindDeclarationVisitor extends TreePathScanner<Void, Void> {
        
        private Element element;
        private Tree declTree;
        private CompilationInfo info;
        
        public FindDeclarationVisitor(Element element, CompilationInfo info) {
            this.element = element;
            this.info = info;
        }
        
	@Override
        public Void visitClass(ClassTree tree, Void d) {
            handleDeclaration();
            super.visitClass(tree, d);
            return null;
        }
        
	@Override
        public Void visitMethod(MethodTree tree, Void d) {
            handleDeclaration();
            super.visitMethod(tree, d);
            return null;
        }
        
	@Override
        public Void visitVariable(VariableTree tree, Void d) {
            handleDeclaration();
            super.visitVariable(tree, d);
            return null;
        }
    
        public void handleDeclaration() {
            Element found = info.getTrees().getElement(getCurrentPath());
            
            if ( element.equals( found ) ) {
                declTree = getCurrentPath().getLeaf();
            }
        }
    
    }
    
}
