/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.api.Page;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.inspect.webkit.WebKitPageModel;
import org.openide.filesystems.FileObject;

/**
 * Utility methods.
 *
 * @author Jan Stola
 */
class Utilities {
    /** MIME types of files with a content that can be styled. */
    private static final Collection<String> STYLED_MIME_TYPES = new HashSet(
            Arrays.asList(new String[]{"text/html", "text/xhtml", "text/x-jsp", "text/x-php5"})); // NOI18N
    /** MIME types of files for which we have a special support in Navigator. */
    private static final Collection<String> NAVIGATOR_MIME_TYPES = new HashSet(
            Arrays.asList(new String[]{"text/html", "text/xhtml"})); // NOI18N

    /**
     * Determines whether the given MIME type corresponds to a content
     * that can be styled.
     * 
     * @param mimeType MIME type to check.
     * @return {@code true} when the given MIME type corresponds to a content
     * that can be styled, returns {@code false} otherwise.
     */
    static boolean isStyledMimeType(String mimeType) {
        return STYLED_MIME_TYPES.contains(mimeType);
    }

    /**
     * Determines whether the given MIME type has a special support in Navigator.
     * 
     * @param mimeType MIME type to check.
     * @return {@code true} when the given MIME type has a special support
     * in Navigator, returns {@code false} otherwise.
     */
    static boolean isMimeTypeSupportedByNavigator(String mimeType) {
        return NAVIGATOR_MIME_TYPES.contains(mimeType);
    }

    /**
     * Determines whether the given MIME type corresponds to a server-side framework.
     * 
     * @param mimeType MIME type to check.
     * @return {@code true} when the given MIME type corresponds to
     * a server-side framework, returns {@code false} otherwise.
     */
    static boolean isServerSideMimeType(String mimeType) {
        return isStyledMimeType(mimeType) && !NAVIGATOR_MIME_TYPES.contains(mimeType);
    }

    /**
     * Returns the file inspected by the given {@code Page}.
     * 
     * @param page {@code Page} to retrieve the inspected file from.
     * @return {@code FileObject} that corresponds to the file inspected
     * by the given {@code Page} or {@code null} when such {@code FileObject}
     * cannot be found.
     */
    static FileObject inspectedFileObject(Page page) {
        FileObject fob = null;
        // PENDING: resolve the following cast (we are not in the webkit
        // package, we should know nothing about WebKitPageModel)
        Project project = ((WebKitPageModel)page).getProject();
        if (project != null) {
            String documentURL = page.getDocumentURL();
            try {
                URL url = new URL(documentURL);
                fob = ServerURLMapping.fromServer(project, url);
            } catch (MalformedURLException ex) {
            }
        }
        return fob;
    }

}
