/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.api.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Miscellaneous file utilities.
 * @author Tomas Mysik
 */
public final class FileUtils {
    /**
     * Constant for PHP MIME type.
     * @since 1.15
     * @see #isPhpFile(FileObject)
     */
    public static final String  PHP_MIME_TYPE = "text/x-php5"; // NOI18N

    private FileUtils() {
    }

    /**
     * Returns <code>true</code> if the file is a PHP file.
     * @param file file to check
     * @return <code>true</code> if the file is a PHP file
     * @since 1.15
     * @see #PHP_MIME_TYPE
     */
    public static boolean isPhpFile(FileObject file) {
        Parameters.notNull("file", file);
        return PHP_MIME_TYPE.equals(FileUtil.getMIMEType(file, PHP_MIME_TYPE));
    }

    /**
     * Find all the files (absolute path) with the given "filename" os user's PATH.
     * <p>
     * This method is suitable for *nix as well as windows.
     * @param filename the name of a file to find.
     * @return list of absolute paths of found files.
     */
    public static List<String> findFileOnUsersPath(String filename) {
        Parameters.notNull("filename", filename);

        String path = System.getenv("PATH"); // NOI18N
        if (path == null) {
            return Collections.<String>emptyList();
        }
        // on linux there are usually duplicities in PATH
        Set<String> dirs = new LinkedHashSet<String>(Arrays.asList(path.split(File.pathSeparator)));
        List<String> found = new ArrayList<String>(dirs.size());
        for (String d : dirs) {
            File file = new File(d, filename);
            if (file.isFile()) {
                String absolutePath = FileUtil.normalizeFile(file).getAbsolutePath();
                // not optimal but should be ok
                if (!found.contains(absolutePath)) {
                    found.add(absolutePath);
                }
            }
        }
        return found;
    }

    /**
     * Get {@link FileObject} for the given {@link Lookup context}.
     * @param context {@link Lookup context} where the {@link FileObject} is searched for
     * @return {@link FileObject} for the given {@link Lookup context} or <code>null</code> if not found
     * @since 1.16
     */
    public static FileObject getFileObject(Lookup context) {
        FileObject fo = context.lookup(FileObject.class);
        if (fo != null) {
            return fo;
        }
        DataObject d = context.lookup(DataObject.class);
        if (d != null) {
            return d.getPrimaryFile();
        }
        return null;
    }

    /**
     * Create {@link org.xml.sax.XMLReader} from {javax.xml.parsers.SAXParser}.
     * @return {@link org.xml.sax.XMLReader} from {javax.xml.parsers.SAXParser}
     * @throws SAXException if the parser cannot be created
     * @since 1.22
     */
    public static XMLReader createXmlReader() throws SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        try {
            return factory.newSAXParser().getXMLReader();
        } catch (ParserConfigurationException ex) {
            throw new SAXException("Cannot create SAX parser", ex);
        }
    }
}
