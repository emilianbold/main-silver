/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.verification;

import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.SemanticAnalysis;
import org.netbeans.modules.php.editor.parser.UnusedOffsetRanges;
import org.netbeans.modules.php.editor.verification.PHPHintsProvider.Kind;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@Messages("UnsedUsesHintDisp=Unused Use Statement")
public class UnusedUsesHint extends AbstractRule {

    private static final String HINT_ID = "Unused.Uses.Hint"; //NOI18N

    @Override
    void computeHintsImpl(PHPRuleContext context, List<Hint> hints, Kind kind) throws BadLocationException {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        for (UnusedOffsetRanges unusedOffsetRanges : SemanticAnalysis.computeUnusedUsesOffsetRanges(phpParseResult)) {
            hints.add(new Hint(UnusedUsesHint.this, Bundle.UnsedUsesHintDisp(), fileObject, unusedOffsetRanges.getRangeToVisualise(), createHintFixes(context.doc, unusedOffsetRanges), 500));
        }
    }

    private List<HintFix> createHintFixes(final BaseDocument baseDocument, final UnusedOffsetRanges unusedOffsetRanges) {
        return Collections.<HintFix>singletonList(new RemoveUnusedUseFix(baseDocument, unusedOffsetRanges));
    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("UnusedUsesHintDesc=Checks unused use statements.")
    public String getDescription() {
        return Bundle.UnusedUsesHintDesc();
    }

    @Override
    public String getDisplayName() {
        return Bundle.UnsedUsesHintDisp();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    private class RemoveUnusedUseFix implements HintFix {
        private final BaseDocument baseDocument;
        private final UnusedOffsetRanges unusedOffsetRanges;

        public RemoveUnusedUseFix(final BaseDocument baseDocument, final UnusedOffsetRanges unusedOffsetRanges) {
            this.baseDocument = baseDocument;
            this.unusedOffsetRanges = unusedOffsetRanges;
        }

        @Override
        @Messages("RemoveUnusedUseFixDesc=Remove Unused Use Statement")
        public String getDescription() {
            return Bundle.RemoveUnusedUseFixDesc();
        }

        @Override
        public void implement() throws Exception {
            final EditList editList = new EditList(baseDocument);
            OffsetRange offsetRange = unusedOffsetRanges.getRangeToReplace();
            editList.replace(offsetRange.getStart(), offsetRange.getLength(), "", true, 0); //NOI18N
            editList.apply();
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
