/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.refactoring;

import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.css.editor.ui.CssRuleCreateActionDialog;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class CssCodeGenerators {

    public static class Factory implements CodeGenerator.Factory {

	public List<? extends CodeGenerator> create(Lookup context) {
	    JTextComponent component = context.lookup(JTextComponent.class);
	    return Collections.singletonList(new GenerateCssRule());
	}
    }

    public static class GenerateCssRule implements CodeGenerator {

	public String getDisplayName() {
	    return NbBundle.getMessage(CssCodeGenerators.class, "MSG_CreateCssRule"); //NOI18N
	}

	public void invoke() {
	    final JTextComponent target = EditorRegistry.lastFocusedComponent();

	    CssRuleCreateActionDialog cssRuleCreateActionDialog = new CssRuleCreateActionDialog();
	    cssRuleCreateActionDialog.showDialog();
	    final String styleRuleName = cssRuleCreateActionDialog.getStyleRuleName();
	    if ((styleRuleName != null) && !styleRuleName.equals("")) {
		final BaseDocument doc = (BaseDocument) target.getDocument();
		doc.runAtomic(new Runnable() {

		    public void run() {
			int searchPos = target.getCaret().getDot() - 1;
			int insertPos = doc.getLength() + 1;
			try {
			    if (searchPos > 0) {
				String txtBefore = doc.getText(searchPos, 1);
				while (!(txtBefore.equals("{") || txtBefore.equals("}"))) {
				    if (txtBefore.equals("/")) {
					if ((searchPos - 1) >= 0) {
					    if (doc.getText(searchPos - 1, 1).equals("*")) {
						break;
					    } else if ((searchPos + 1) <= doc.getLength()) {
						if (doc.getText(searchPos + 1, 1).equals("*")) {
						    break;
						}
					    }
					}
				    }
				    searchPos--;
				    if (searchPos < 0) {
					break;
				    }
				    txtBefore = doc.getText(searchPos, 1);
				}
				if (searchPos < 0) {
				    insertPos = 0;
				} else if (txtBefore.equals("}")) {
				    insertPos = searchPos + 1;
				} else if (txtBefore.equals("/")) {
				    if ((searchPos - 1) >= 0) {
					if (doc.getText(searchPos - 1, 1).equals("*")) {
					    insertPos = searchPos + 1;
					} else if ((searchPos + 1) <= doc.getLength()) {
					    if (doc.getText(searchPos + 1, 1).equals("*")) {
						insertPos = searchPos - 1;
					    }
					}
				    }
				} else if (txtBefore.equals("{")) {
				    searchPos = target.getCaret().getDot();
				    String txtAfter = doc.getText(searchPos, 1);
				    while (!txtAfter.equals("}")) {
					searchPos++;
					if (searchPos > doc.getLength()) {
					    break;
					}
					txtAfter = doc.getText(searchPos, 1);
				    }
				    if (txtAfter.equals("}")) {
					insertPos = searchPos + 1;
				    }
				}
			    } else {
				insertPos = 0;
			    }
			    doc.insertString(insertPos, "\n" + styleRuleName + " {\n\n}", null);
			    searchPos = insertPos;
			    String txtAfter = doc.getText(searchPos, 1);
			    while (!txtAfter.equals("{")) {
				searchPos++;
				if (searchPos > doc.getLength()) {
				    break;
				}
				txtAfter = doc.getText(searchPos, 1);
			    }
			    searchPos += 2;
			    target.setCaretPosition(searchPos);
			} catch (BadLocationException exc) {
			    exc.printStackTrace();
			}
		    }
		});
	    }
	}
    }
}
