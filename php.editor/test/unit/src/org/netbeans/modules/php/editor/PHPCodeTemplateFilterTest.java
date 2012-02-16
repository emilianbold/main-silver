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
package org.netbeans.modules.php.editor;

import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHPCodeTemplateFilterTest extends PHPTestBase {

    public PHPCodeTemplateFilterTest(String testName) {
        super(testName);
    }

    private void checkAllTemplates(String source, boolean expected) throws Exception {
        assertNotNull(source);
        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        String modifiedSource = source.substring(0, sourcePos) + source.substring(sourcePos+1);
        Document document = getDocument(modifiedSource, "text/x-php5");
        assertNotNull(document);
        Collection<? extends CodeTemplate> codeTemplates = getCodeTemplates(document);
        PHPCodeTemplateFilter filter = new PHPCodeTemplateFilter(document, sourcePos);
        assertNotNull(filter);
        for (CodeTemplate codeTemplate : codeTemplates) {
            assertEquals("Code template: " + codeTemplate.toString(), expected, filter.accept(codeTemplate));
        }
    }

    private void checkTemplate(String source, boolean expected, String abbreviation) throws Exception {
        assertNotNull(source);
        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        String modifiedSource = source.substring(0, sourcePos) + source.substring(sourcePos+1);
        Document document = getDocument(modifiedSource, "text/x-php5");
        assertNotNull(document);
        Collection<? extends CodeTemplate> codeTemplates = getCodeTemplates(document);
        PHPCodeTemplateFilter filter = new PHPCodeTemplateFilter(document, sourcePos);
        assertNotNull(filter);
        for (CodeTemplate codeTemplate : codeTemplates) {
            if (codeTemplate.getAbbreviation().equals(abbreviation)) {
                assertEquals("Code template: " + codeTemplate.toString(), expected, filter.accept(codeTemplate));
            }
        }
    }

    private void checkNoContextTemplate(String source, boolean expected) {
        assertNotNull(source);
        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        String modifiedSource = source.substring(0, sourcePos) + source.substring(sourcePos+1);
        Document document = getDocument(modifiedSource, "text/x-php5");
        assertNotNull(document);
        CodeTemplateManager templateManager = CodeTemplateManager.get(document);
        assertNotNull(templateManager);
        CodeTemplate codeTemplate = templateManager.createTemporary("no conext template");
        PHPCodeTemplateFilter filter = new PHPCodeTemplateFilter(document, sourcePos);
        assertEquals("Code template: " + codeTemplate.toString(), expected, filter.accept(codeTemplate));
    }

    private Collection<? extends CodeTemplate> getCodeTemplates(Document document) {
        CodeTemplateManager templateManager = CodeTemplateManager.get(document);
        assertNotNull(templateManager);
        return templateManager.getCodeTemplates();
    }

    public void testAllDefaultTemplatesInClass() throws Exception {
        String source = "<?php\n^ \n?>";
        checkAllTemplates(source, true);
    }

    public void testClsInCls() throws Exception {
        String source = "<?php\nclass Foo {^} \n?>";
        checkTemplate(source, false, "cls");
    }

    public void testNoContextTemplateInClass() throws Exception {
        String source = "<?php\n class Foo {^} \n?>";
        checkNoContextTemplate(source, true);
    }

    public void testFcomInsideClass() throws Exception {
        String source = "<?php\nclass Foo {^} \n?>";
        checkTemplate(source, true, "fcom");
    }

    public void testFcomOutsideClass() throws Exception {
        String source = "<?php\nclass Foo {} \n^ \n?>";
        checkTemplate(source, true, "fcom");
    }

    public void testFcomInsideFunction() throws Exception {
        String source = "<?php\nclass Foo { \n function foo() { ^ } \n} \n?>";
        checkTemplate(source, true, "fcom");
    }

}
