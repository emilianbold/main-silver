/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.editor.hints;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.groovy.editor.imports.ImportHelper;
import org.netbeans.modules.groovy.editor.imports.ImportCandidate;
import org.netbeans.modules.groovy.editor.compiler.error.CompilerErrorID;
import org.netbeans.modules.groovy.editor.compiler.error.GroovyError;
import org.netbeans.modules.groovy.editor.hints.infrastructure.GroovyErrorRule;
import org.netbeans.modules.groovy.editor.hints.infrastructure.GroovyRuleContext;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author schmidtm, Petr Hejl
 */
public class ClassNotFoundRule extends GroovyErrorRule {

    public static final Logger LOG = Logger.getLogger(ClassNotFoundRule.class.getName());
    private final String DESC = NbBundle.getMessage(ClassNotFoundRule.class, "FixImportsHintDescription"); // NOI18N

    
    public ClassNotFoundRule() {
        super();
    }

    @Override
    public Set<CompilerErrorID> getCodes() {
        return EnumSet.of(CompilerErrorID.CLASS_NOT_FOUND);
    }

    @Override
    public void run(GroovyRuleContext context, GroovyError error, List<Hint> result) {
        LOG.log(Level.FINEST, "run()"); // NOI18N

        String desc = error.getDescription();

        if (desc == null) {
            LOG.log(Level.FINEST, "desc == null"); // NOI18N
            return;
        }

        LOG.log(Level.FINEST, "Processing : {0}", desc); // NOI18N

        String missingClassName = ImportHelper.getMissingClassName(desc);

        if (missingClassName == null) {
            return;
        }

        // FIXME parsing API
        FileObject fo = context.parserResult.getSnapshot().getSource().getFileObject();

        List<ImportCandidate> importCandidates = ImportHelper.getImportCandidate(fo, missingClassName);


        if (importCandidates.isEmpty()) {
            return;
        }

        int DEFAULT_PRIORITY = 292;

        // FIXME: for CLASS_NOT_FOUND errors we mark the whole line.
        // This should be replaced with marking the indentifier only.
        // OffsetRange range = new OffsetRange(error.getStartPosition(), error.getEndPosition());
        int lineStart = 0;
        int lineEnd = 0;

        try {

            lineStart = Utilities.getRowStart(context.doc, error.getStartPosition());
            lineEnd = Utilities.getRowEnd(context.doc, error.getEndPosition());

        } catch (BadLocationException ex) {
            LOG.log(Level.FINEST, "Processing : {0}", ex); // NOI18N
            return;
        }

        OffsetRange range = new OffsetRange(lineStart, lineEnd);

        for (ImportCandidate candidate : importCandidates) {
            List<HintFix> fixList = new ArrayList<HintFix>(1);
            String fqn = candidate.getFqnName();
            HintFix fixToApply = new AddImportFix(fo, fqn);
            fixList.add(fixToApply);

            Hint descriptor = new Hint(this, fixToApply.getDescription(), fo, range,
                    fixList, DEFAULT_PRIORITY);

            result.add(descriptor);
        }

        return;
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return DESC;
    }

    @Override
    public boolean showInTasklist() {
        return false;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }

    private class AddImportFix implements HintFix {

        FileObject fo;
        String fqn;

        public AddImportFix(FileObject fo, String fqn) {
            this.fo = fo;
            this.fqn = fqn;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(ClassNotFoundRule.class, "ClassNotFoundRuleHintDescription", fqn); // NOI18N
        }

        @Override
        public void implement() throws Exception {
            ImportHelper.addImportStatement(fo, fqn);
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }
}
