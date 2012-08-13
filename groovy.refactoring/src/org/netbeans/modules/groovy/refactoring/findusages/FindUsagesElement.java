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

package org.netbeans.modules.groovy.refactoring.findusages;

import javax.swing.text.Position;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.refactoring.GroovyRefactoringElement;
import org.netbeans.modules.groovy.refactoring.utils.GroovyProjectUtil;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Janicek
 */
public class FindUsagesElement extends SimpleRefactoringElementImplementation implements Comparable<FindUsagesElement> {

    private final GroovyRefactoringElement element;
    private final BaseDocument doc;
    private final Line line;
    private final int lineNumber;


    public FindUsagesElement(GroovyRefactoringElement element, BaseDocument doc) {
        this.element = element;
        this.doc = doc;
        this.line = GroovyProjectUtil.getLine(element.getFileObject(), element.getNode().getLineNumber() - 1);
        this.lineNumber = line.getLineNumber();
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getText() {
        return element.getName() + " -";
    }

    @Override
    public String getDisplayText() {
        final ASTNode node = element.getNode();

        final int columnStart = node.getColumnNumber();
        final int columnEnd = node.getLastColumnNumber();

        String beforeUsagePart = line.createPart(0, columnStart - 1).getText();
        String usagePart = line.createPart(columnStart - 1, columnEnd - columnStart).getText();
        String afterUsagePart = line.createPart(columnEnd - 1, line.getText().length()).getText();

        if (node instanceof ConstructorNode) {
            String constructorName = ((ConstructorNode) node).getDeclaringClass().getNameWithoutPackage();
            int constructorStart = line.getText().indexOf(constructorName);

            beforeUsagePart = line.createPart(0, constructorStart).getText();
            usagePart = line.createPart(constructorStart, constructorName.length()).getText();
            afterUsagePart = line.createPart(constructorStart + constructorName.length(), line.getText().length()).getText();
        }

        return beforeUsagePart + "<b>" + usagePart + "</b>" + afterUsagePart;
    }

    @Override
    public void performChange() {
    }

    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public FileObject getParentFile() {
        return element.getFileObject();
    }

    @Override
    public PositionBounds getPosition() {
        OffsetRange range = AstUtilities.getRange(element.getNode(), doc);
        if (range == OffsetRange.NONE) {
            return null;
        }

        CloneableEditorSupport ces = GroovyProjectUtil.findCloneableEditorSupport(element.getFileObject());
        PositionRef ref1 = ces.createPositionRef(range.getStart(), Position.Bias.Forward);
        PositionRef ref2 = ces.createPositionRef(range.getEnd(), Position.Bias.Forward);
        return new PositionBounds(ref1, ref2);
    }

    @Override
    public int compareTo(FindUsagesElement comparedElement) {
        return this.lineNumber - comparedElement.lineNumber;
    }
}