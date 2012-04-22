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
package org.netbeans.modules.html.editor.gsf;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.elements.TreePath;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mfukala@netbeans.org
 */
public class HtmlElementHandle implements ElementHandle {

    private Element node;
    private FileObject fo;

    HtmlElementHandle(Element node, FileObject fo) {
        this.node = node;
        this.fo = fo;
    }

    @Override
    public FileObject getFileObject() {
        return fo;
    }

    @Override
    public String getMimeType() {
        return HtmlKit.HTML_MIME_TYPE;
    }

    @Override
    public String getName() {
        return node.id().toString();
    }

    @Override
    public String getIn() {
        return null;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.TAG;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public boolean signatureEquals(ElementHandle handle) {
        if (!(handle instanceof HtmlElementHandle)) {
            return false;
        }

        Element foreignNode = ((HtmlElementHandle) handle).node();
        if (node == foreignNode) {
            return true;
        }

        TreePath fnPath = new TreePath(foreignNode);
        TreePath path = new TreePath(node);

        return path.equals(fnPath);
    }

    @Override
    public String toString() {
        return new TreePath(node).toString();
    }
    
    

    Element node() {
        return node;
    }

    public int from() {
        return node().from();
    }

    public int to() {
        return node().type() == ElementType.OPEN_TAG ? ((OpenTag)node()).semanticEnd() : node().to();
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        ElementHandle object = HtmlGSFParser.resolveHandle(result, this);
        if (object instanceof HtmlElementHandle) {
            HtmlElementHandle heh = (HtmlElementHandle) object;
            int from = result.getSnapshot().getOriginalOffset(heh.from());
            int to = result.getSnapshot().getOriginalOffset(heh.to());
            return from != -1 && to != -1 ? new OffsetRange(from, to) : OffsetRange.NONE;
        } else {
            throw new IllegalArgumentException("Foreign element: " + object + " of type " +
                    ((object != null) ? object.getClass().getName() : "null")); //NOI18N
        }
    }
}
