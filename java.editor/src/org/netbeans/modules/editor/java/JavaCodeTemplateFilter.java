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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class JavaCodeTemplateFilter implements CodeTemplateFilter {
    
    private static final Logger LOG = Logger.getLogger(JavaCodeTemplateFilter.class.getName());
    
    private int startOffset;
    private int endOffset;
    private Tree.Kind ctx = null;
    
    private JavaCodeTemplateFilter(JTextComponent component, int offset) {
        if (Utilities.isJavaContext(component, offset, false)) {
            this.startOffset = offset;
            this.endOffset = component.getSelectionStart() == offset ? component.getSelectionEnd() : -1;
            final Source source = Source.create(component.getDocument());
            if (source != null) {
                final AtomicBoolean cancel = new AtomicBoolean();
                ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ParserManager.parse(Collections.singleton(source), new UserTask() {
                                @Override
                                public void run(ResultIterator resultIterator) throws Exception {
                                    if (cancel.get())
                                        return;
                                    Parser.Result result = resultIterator.getParserResult(startOffset);
                                    CompilationController controller = result != null ? CompilationController.get(result) : null;
                                    if (controller != null) {
                                        controller.toPhase(Phase.PARSED);
                                        Tree tree = controller.getTreeUtilities().pathFor(startOffset).getLeaf();
                                        if (endOffset >= 0 && startOffset != endOffset) {
                                            if (controller.getTreeUtilities().pathFor(endOffset).getLeaf() != tree)
                                                return;
                                        }
                                        ctx = tree.getKind();
                                    }
                                }
                            });
                        } catch (ParseException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }, NbBundle.getMessage(JavaCodeTemplateProcessor.class, "JCT-init"), cancel, false); //NOI18N
            }
        }
    }

    public synchronized boolean accept(CodeTemplate template) {
        if (ctx == null)
            return false;
        EnumSet<Tree.Kind> contexts = getTemplateContexts(template);        
        return contexts.size() == 0 || contexts.contains(ctx);
    }
    
    private EnumSet<Tree.Kind> getTemplateContexts(CodeTemplate template) {
        List<String> contexts = template.getContexts();
        List<Tree.Kind> kinds = new ArrayList<Tree.Kind>();
        
        if (contexts != null) {
            for(String ctx : contexts) {
                Tree.Kind kind = Tree.Kind.valueOf(ctx);
                if (kind != null) {
                    kinds.add(kind);
                } else {
                    LOG.warning("Invalid code template context '" + ctx + "', ignoring."); //NOI18N
                }
            }
        }
        
        if (kinds.size() > 0) {
            return EnumSet.copyOf(kinds);
        } else {
            return EnumSet.noneOf(Tree.Kind.class);
        }
    }

    public static final class Factory implements CodeTemplateFilter.Factory {
        
        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return new JavaCodeTemplateFilter(component, offset);
        }
    }
}
