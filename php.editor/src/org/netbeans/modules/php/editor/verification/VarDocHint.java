/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.verification;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
public class VarDocHint extends AbstractRule {
    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public String getId() {
        return "Var.Doc.Hint";//NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(VarDocHint.class, "VarDocHintDesc");//NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(VarDocHint.class, "VarDocHintDispName");//NOI18N
    }

    void computeHintsImpl(PHPRuleContext context, List<Hint> hints, PHPHintsProvider.Kind kind) throws BadLocationException {
        final BaseDocument doc = context.doc;
        final int caretOffset = context.caretOffset;
        int lineBegin = -1;
        int lineEnd = -1;
        lineBegin = caretOffset > 0 ? Utilities.getRowStart(doc, caretOffset) : -1;
        lineEnd = (lineBegin != -1) ? Utilities.getRowEnd(doc, caretOffset) : -1;
        if (lineBegin != -1 && lineEnd != -1 && caretOffset > lineBegin) {
            String identifier = Utilities.getIdentifier(doc, caretOffset);
            if (identifier != null && identifier.startsWith("$")) {
                PHPParseResult parseResult = (PHPParseResult) context.parserResult;
                Model model = parseResult.getModel();
                VariableScope variableScope = model.getVariableScope(caretOffset);
                if (variableScope != null) {
                    int wordStart = Utilities.getWordStart(doc, caretOffset);
                    int wordEnd = Utilities.getWordEnd(doc, caretOffset);
                    VariableName variable = ModelUtils.getFirst(variableScope.getDeclaredVariables(), identifier);
                    if (variable != null && (wordEnd-wordStart) == identifier.length()) {
                        final OffsetRange identifierRange = new OffsetRange(wordStart, wordEnd);
                        int offset = identifierRange.getEnd();
                        if (variable.getTypes(offset).isEmpty()) {
                            Collection<? extends String> typeNames = variable.getTypeNames(offset);
                            for (String type : typeNames) {
                                if (!type.contains("@")) {//NOI18N
                                    return;
                                }
                            }
                            hints.add(new Hint(VarDocHint.this, getDisplayName(),
                                    context.parserResult.getSnapshot().getSource().getFileObject(), identifierRange,
                                    Collections.<HintFix>singletonList(new Fix(context, variable)), 500));
                        }
                    }
                }
            }
        }
    }

    private class Fix implements HintFix {

        private RuleContext context;
        private VariableName vName;

        Fix(RuleContext context, VariableName vName) {
            this.context = context;
            this.vName = vName;
        }

        public String getDescription() {
            return VarDocHint.this.getDescription();
        }

        public void implement() throws Exception {
            BaseDocument doc = context.doc;
            int caretOffset = getOffset(doc);
            final EditList editList = getEditList(doc, caretOffset);
            Position caretPosition = editList.createPosition(caretOffset);
            editList.apply();
            if (caretPosition != null && caretPosition.getOffset() != -1) {
                JTextComponent target = GsfUtilities.getPaneFor(context.parserResult.getSnapshot().getSource().getFileObject());
                if (target != null) {
                    int offset = caretPosition.getOffset();
                    String commentText = getCommentText();
                    int indexOf = commentText.indexOf(getTypeTemplate());
                    if (indexOf != -1 && (offset + indexOf + getTypeTemplate().length() <= doc.getLength())) {
                        String s = doc.getText(offset + indexOf, getTypeTemplate().length());
                        if (getTypeTemplate().equals(s)) {
                            target.select(offset + indexOf, offset + indexOf + getTypeTemplate().length());
                            scheduleShowingCompletion();
                        }

                    }
                }
            }
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }

        EditList getEditList(BaseDocument doc, int caretOffset) throws Exception {
            EditList edits = new EditList(doc);
            edits.replace(caretOffset, 0, getCommentText(), true, 0);
            return edits;
        }

        private String getCommentText() {
            return String.format("\n/* @var %s %s */", vName.getName(), getTypeTemplate());//NOI18N
        }

        private String getTypeTemplate() {
            return "<type>";//NOI18N
        }

        private int getOffset(BaseDocument doc) throws BadLocationException {
            final int caretOffset = Utilities.getRowStart(doc, context.caretOffset);
            return Utilities.getRowEnd(doc, caretOffset-1);
        }

        private void scheduleShowingCompletion() {
            service.schedule(new Runnable() {

                public void run() {
                    Completion.get().showCompletion();
                }
            }, 50, TimeUnit.MILLISECONDS);
        }
    }
}
