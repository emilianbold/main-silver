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
package org.netbeans.modules.html.editor.hints.css;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.html.editor.hints.HtmlRuleContext;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public class MissingCssElement extends Hint {

    public MissingCssElement(Rule rule, String msg, HtmlRuleContext context, OffsetRange range, CssClassesVisitor.HintContext hintContext) {
        super(rule,
                msg,
                context.getFile(),
                range,
                createFixes(context, hintContext),
                10);
    }

    private static List<HintFix> createFixes(HtmlRuleContext context, CssClassesVisitor.HintContext hintContext) {
        List<HintFix> fixes = new ArrayList<HintFix>();
        FileObject sourceFile = context.getFile();

        if (hintContext.getClasses2files().get(hintContext.getClassName()) != null) {
            //1) if the class is found in one of the stylesheets in the project:
            //      * add "Import stylesheet" hintfix
            for (FileObject file : hintContext.getClasses2files().get(hintContext.getClassName())) {
                fixes.add(new AddStylesheetLinkHintFix(sourceFile, file));
            }
        } else {
            //2) if the class is not found in any stylesheet from the project:
            //      * add "create in xxx stylesheet" - for all available stylesheets. 
            //      The fix will add the stylesheet reference and create the rule there
            //* 3) if there's no stylesheet in the project
            //      * create in new stylesheet 
            //      Creates stylesheet 'styles.css' in the same folder, add ref to it and put the rule inside.

            for (FileObject stylesheet : hintContext.getAllStylesheets()) {
                fixes.add(new CreateRuleInStylesheet(
                        sourceFile, 
                        stylesheet, 
                        hintContext.getClassName(),
                        !hintContext.getReferredFiles().contains(stylesheet)));
            }

        }


        return fixes;
    }
}
