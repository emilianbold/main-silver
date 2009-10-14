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

package org.netbeans.modules.php.editor.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessorFactory;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.*;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Andrei Badea
 */
public class PHPCodeTemplateProcessor implements CodeTemplateProcessor {

    private final static String NEW_VAR_NAME = "newVarName"; // NOI18N
    private final static String VARIABLE_FROM_NEXT_ASSIGNMENT_NAME = "variableFromNextAssignmentName"; //NOI18N
    private final static String VARIABLE_FROM_NEXT_ASSIGNMENT_TYPE = "variableFromNextAssignmentType"; //NOI18N
    private static final String VARIABLE_FROM_PREVIOUS_ASSIGNMENT = "variableFromPreviousAssignment"; //NOI18N
    private static final String INSTANCE_OF = "instanceof"; //NOI18N

    private final CodeTemplateInsertRequest request;
    // @GuardedBy("this")
    private ParserResult info;

    public PHPCodeTemplateProcessor(CodeTemplateInsertRequest request) {
        this.request = request;
    }

    public void updateDefaultValues() {
        for (CodeTemplateParameter param : request.getMasterParameters()) {
            String value = getProposedValue(param);
            if (value != null && !value.equals(param.getValue())) {
                param.setValue(value);
            }
        }
    }

    public void parameterValueChanged(CodeTemplateParameter masterParameter, boolean typingChange) {
        // No op.
    }

    public void release() {
        // No op.
    }

    private String getNextVariableType() {
        final int offset = request.getComponent().getCaretPosition();
        Collection<? extends VariableName> declaredVariables = getDeclaredVariables(offset);
        String varName = getNextVariableName();
        if (varName == null || declaredVariables == null) {
            return null;
        }
        if (varName.charAt(0) != '$') {
            varName = "$" + varName; //NOI18N
        }

        List<? extends VariableName> variables = ModelUtils.filter(declaredVariables, varName);
        VariableName first = ModelUtils.getFirst(variables);
        if (first != null) {
            String typeNames = StringUtils.implode(getUniqueTypeNames(first, offset), "|"); // NOI18N
            if (!StringUtils.hasText(typeNames)) {
                return null;
            }
            return typeNames;
        }
        return null;
    }

    private String getProposedValue(CodeTemplateParameter param) {
        String def = null;
        boolean newVarName = false;
        boolean previousVariable = false;
        String type = null;
        for (Entry<String, String> entry : param.getHints().entrySet()) {
            String hintName = entry.getKey();
            // XXX constant anywhere?
            if ("default".equals(hintName)) { // NOI18N
                assert def == null : "default already set to " + def;
                def = param.getValue();
            } else if (NEW_VAR_NAME.equals(hintName)) {
                assert !newVarName : "newVarName already set";
                newVarName = true;
            } else if (VARIABLE_FROM_NEXT_ASSIGNMENT_NAME.equals(hintName)) {
                return getNextVariableName();
            } else if (VARIABLE_FROM_NEXT_ASSIGNMENT_TYPE.equals(hintName)) {
                return getNextVariableType();
            } else if (VARIABLE_FROM_PREVIOUS_ASSIGNMENT.equals(hintName)) {
                assert !previousVariable : "previousVariable already set";
                previousVariable = true;
            } else if (INSTANCE_OF.equals(hintName)) {
                assert type == null : "type already set to " + type;
                type = entry.getValue();
            }
        }

        if (newVarName) {
            return newVarName(def);
        } else if (previousVariable) {
            return getPreviousVariable(type);
        }
        return null;
    }


    private String getNextVariableName() {
        if (!initParsing()) {
            return null;
        }
        final int caretOffset = request.getComponent().getCaretPosition();
        VariableName var = null;
        Collection<? extends VariableName> allVariables = getDeclaredVariables(caretOffset);
        if (allVariables != null) {
            for (VariableName variableName : allVariables) {
                if (var == null) {
                    var = variableName;
                } else {
                    int newDiff = Math.abs(variableName.getNameRange().getStart() - caretOffset);
                    int oldDiff = Math.abs(var.getNameRange().getStart() - caretOffset);
                    if (newDiff < oldDiff) {
                        var = variableName;
                    }
                }
            }
        }

        return var != null ? var.getName().substring(1) : null;
    }

    private String getPreviousVariable(String type) {
        if (!initParsing()) {
            return null;
        }
        final int caretOffset = request.getComponent().getCaretPosition();
        VariableName var = null;
        Collection<? extends VariableName> allVariables = getDeclaredVariables(caretOffset);
        if (allVariables != null) {
            for (VariableName variableName : allVariables) {
                int newDiff = variableName.getNameRange().getStart() - caretOffset;
                if (newDiff < 0) {
                    if (!hasType(variableName, caretOffset, type)) {
                        continue;
                    }
                    // variable is defined before and has correct type
                    if (var == null) {
                        var = variableName;
                        continue;
                    }
                    int oldDiff = var.getNameRange().getStart() - caretOffset;
                    assert oldDiff < 0;
                    if (newDiff > oldDiff) {
                        // variable is closer
                        var = variableName;
                    }
                }
            }
        }

        return var != null ? var.getName() : null;
    }

    private boolean hasType(VariableName variableName, int offset, String type) {
        if (type == null) {
            return true;
        }
        // XXX fix this, radek ;)
        return variableName.getTypeNames(offset).contains(type);
    }

    private List<String> getUniqueTypeNames(VariableName variableName, int offset) {
        List<String> uniqueTypeNames = new ArrayList<String>();
        for (TypeScope type : variableName.getTypes(offset)) {
            if (!uniqueTypeNames.contains(type.getName())) {
                uniqueTypeNames.add(type.getName());
            }
        }
        return uniqueTypeNames;
    }

    private String newVarName(final String proposed) {
        if (!initParsing()) {
            return null;
        }
        final int caretOffset = request.getComponent().getCaretPosition();
        int suffix = 0;
        final String[] nue = { null };
        synchronized (this) {
            for (;;) {
                nue[0] = proposed + (suffix > 0 ? String.valueOf(suffix) : "");
                Set<String> varInScope = ASTNodeUtilities.getVariablesInScope(info, caretOffset, new ASTNodeUtilities.VariableAcceptor() {
                    public boolean acceptVariable(String variableName) {
                        return nue[0].equals(variableName);
                    }
                });
                if (varInScope.isEmpty()) {
                    break;
                }
                ++suffix;
            }
        }
        return nue[0];
    }

    private synchronized boolean initParsing() {
        if (info != null) {
            return true;
        }
        Document doc = request.getComponent().getDocument();
        FileObject file = NavUtils.getFile(doc);
        if (file == null) {
            return false;
        }
        try {
            ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    PHPCodeTemplateProcessor.this.info = (PHPParseResult)resultIterator.getParserResult();;
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
            info = null;
            return false;
        }

        return true;
    }

    private Collection<? extends VariableName> getDeclaredVariables(final int caretOffset) {
        Model model = ((PHPParseResult) info).getModel();
        VariableScope varScope = model.getVariableScope(caretOffset);
        if (varScope != null) {
            return varScope.getDeclaredVariables();
        }
        return null;
    }

    public static final class Factory implements CodeTemplateProcessorFactory {

        public CodeTemplateProcessor createProcessor(CodeTemplateInsertRequest request) {
            return new PHPCodeTemplateProcessor(request);
        }
    }

    private static final class AssignmentLocator extends DefaultVisitor {

        private int offset;
        protected Assignment node = null;

        /**
         * Locates the nearest assignement after the offset
         * @param beginNode
         * @param astOffset
         * @return
         */
        public Assignment locate(ASTNode beginNode, int astOffset) {
            offset = astOffset;
            scan(beginNode);
            return this.node;
        }
        
        @Override
        public void scan(ASTNode current) {
            if (this.node == null && current != null) {
                current.accept(this);
            }
        }

        @Override
        public void visit(Assignment node) { 
            if (node != null) {
                if (node.getStartOffset() > offset) {
                    this.node = node;
                }
            }
        }
    }
}
