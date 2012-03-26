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
package org.netbeans.modules.editor.search.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.search.SearchNbEditorKit;
import org.openide.util.NbBundle;

@EditorActionRegistration(name = SearchNbEditorKit.INCREMENTAL_SEARCH_FORWARD)
public class IncrementalSearchForwardAction extends SearchAction {

    static final long serialVersionUID = -1;
    private static final Map<String, String> searchAttrs;

    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put(Action.NAME, SearchNbEditorKit.INCREMENTAL_SEARCH_FORWARD);
        searchAttrs = Collections.unmodifiableMap(aMap);
    }

    public IncrementalSearchForwardAction(Map<String, ?> attrs) {
        super(attrs);
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(IncrementalSearchForwardAction.class, SearchNbEditorKit.INCREMENTAL_SEARCH_FORWARD));
        putValue(NbEditorKit.SYSTEM_ACTION_CLASS_NAME_PROPERTY, org.openide.actions.FindAction.class.getName());
    }

    public IncrementalSearchForwardAction() {
        this(searchAttrs);
    }
}
