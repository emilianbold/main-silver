/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.refactoring.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.css.editor.CssProjectSupport;
import org.netbeans.modules.css.indexing.CssFileModel;
import org.netbeans.modules.css.indexing.CssFileModel.Entry;
import org.netbeans.modules.css.indexing.CssIndex;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;

/**
 * @author marekfukala
 */
public class CssRefactoring {

    private CssRefactoring() {
    }

    public static Map<FileObject, Collection<int[]>> findAllOccurances(String elementName, RefactoringElementType type, FileObject baseFile, boolean nonVirtualOnly) {
        CssProjectSupport sup = CssProjectSupport.findFor(baseFile);
        if (sup == null) {
            return null;
        }
        CssIndex index = sup.getIndex();
        Collection<FileObject> queryResult = index.find(type, elementName);
        Map<FileObject, Collection<int[]>> result = new HashMap<FileObject, Collection<int[]>>();

        for (FileObject file : queryResult) {
            try {
                Source source;
                CloneableEditorSupport editor = GsfUtilities.findCloneableEditorSupport(file);
                //prefer using editor
                //XXX this approach doesn't match the dependencies graph
                //which is made strictly upon the index data
                if (editor != null && editor.isModified()) {
                    source = Source.create(editor.getDocument());
                } else {
                    source = Source.create(file);
                }

                CssFileModel model = new CssFileModel(source);
                Collection<Entry> entries = model.get(type);

                Collection<int[]> ranges = result.get(file);
                if (ranges == null) {
                    ranges = new ArrayList<int[]>();
                    result.put(file, ranges);
                }

                for (Entry entry : entries) {
                    if (elementName.equals(entry.getName())) {
                        if (entry.isValidInSourceDocument()) {
                            if (nonVirtualOnly && !entry.isVirtual()) {
                                int[] pair = new int[]{entry.getDocumentRange().getStart(), entry.getLine()};
                                ranges.add(pair);
                            }
                        }
                    }
                }

            } catch (ParseException e) {
                Exceptions.printStackTrace(e);
            }
        }

        return result;

    }
}
