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
package org.netbeans.modules.web.common.api;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Various web utilities
 *
 * @author marekfukala
 */
public class WebUtils {

    /**
     * Resolves the relative or absolute link from the base file
     *
     * @param source The base file
     * @param importedFileName the link
     * @return
     */
    public static FileObject resolve(FileObject source, String importedFileName) {
        try {
            URI u = URI.create(importedFileName);
            File file = null;

            if (u.isAbsolute()) {
                //do refactor only file resources
                if ("file".equals(u.getScheme())) { //NOI18N
                    try {
                        //the IAE is thrown for invalid URIs quite frequently
                        file = new File(u);
                    } catch (IllegalArgumentException iae) {
                        //no-op
                    }
                }
            } else {
                //no schema specified
                file = new File(importedFileName);
            }

            if (file != null) {

                if (!file.isAbsolute()) {
                    //relative to the current file's folder - let's resolve
                    FileObject parent = source.getParent();
                    if(parent != null) {
                        FileObject resolvedFileObject = parent.getFileObject(importedFileName);
                        if (resolvedFileObject != null && resolvedFileObject.isValid()) {
                            return resolvedFileObject;
                        }
                    }
                } else {
                    //absolute web path
                    FileObject webRoot = ProjectWebRootQuery.getWebRoot(source); //find web root
                    if(webRoot != null) {
                        //resolve the link relative to the web root
                        FileObject resolved = webRoot.getFileObject(file.getAbsolutePath());
                        if (resolved != null && resolved.isValid()) {
                            return resolved;
                        }
                    }
                }
            }

        } catch (IllegalArgumentException e) {
            Logger.getAnonymousLogger().log(Level.INFO, "Cannot resolve import '" + importedFileName + "' from file " + source.getPath(), e); //NOI18N
        }
        return null;
    }

    /** finds first ResultIterator of the given mimetype */
    public static ResultIterator getResultIterator(ResultIterator ri, String mimetype) {
        if (ri.getSnapshot().getMimeType().equals(mimetype)) {
            return ri;
        }
        for (Embedding e : ri.getEmbeddings()) {
            ResultIterator eri = ri.getResultIterator(e);
            if (e.getMimeType().equals(mimetype)) {
                return eri;
            } else {
                ResultIterator eeri = getResultIterator(eri, mimetype);
                if (eeri != null) {
                    return eeri;
                }
            }
        }
        return null;
    }

    public static String unquotedValue(CharSequence value) {
        CharSequence unquoted = isValueQuoted(value) ? value.subSequence(1, value.length() - 1) : value;
        return unquoted.toString();
    }

    public static boolean isValueQuoted(CharSequence value) {
        if (value.length() < 2) {
            return false;
        } else {
            return ((value.charAt(0) == '\'' || value.charAt(0) == '"')
                    && (value.charAt(value.length() - 1) == '\'' || value.charAt(value.length() - 1) == '"'));
        }
    }
}
