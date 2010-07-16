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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.spi.java.project.support;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Miscellaneous helper utils to detect Javadoc root folder, source root folder or
 * package of the given java or class file.
 *
 * @since org.netbeans.modules.java.project/1 1.20
 */
public class JavadocAndSourceRootDetection {

    private static final int HOW_MANY_DIRS_TO_TRAVERSE_DEEP = 5;

    private static final Logger LOG = Logger.getLogger(JavadocAndSourceRootDetection.class.getName());
    
    private JavadocAndSourceRootDetection() {
    }

    /**
     * Finds Javadoc root inside of given folder.
     *
     * @param fo base folder to start search in; routine will traverse 5 folders
     *  deep before giving up; cannot be null; must be folder
     * @return found Javadoc root or null if none found
     */
    public static FileObject findJavadocRoot(FileObject baseFolder) {
        Parameters.notNull("baseFolder", baseFolder);
        if (!baseFolder.isFolder()) {
            throw new IllegalArgumentException("baseFolder must be folder - "+baseFolder); // NOI18N
        }
        return findJavadocRoot(baseFolder, 0);
    }

    /**
     * Finds Java sources root inside of given folder.
     *
     * @param fo base folder to start search in; routine will traverse subfolders
     *  to find a Java file to detect package root; cannot be null; must be folder
     * @return found package root of first Java file found or null if none found
     */
    public static FileObject findSourceRoot(FileObject fo) {
        Parameters.notNull("fo", fo);
        if (!fo.isFolder()) {
            throw new IllegalArgumentException("fo must be folder - "+fo); // NOI18N
        }
        FileObject root = findJavaSourceFile(fo, 0);
        if (root != null) {
            return findPackageRoot(root);
        }
        return null;
    }

    /**
     * Finds Java sources roots inside of given folder.
     *
     * @param folder to start search in; routine will traverse subfolders
     *  to find a Java file to detect package root; cannot be null; must be folder
     * @param canceled if set to true the method immediately returns roots it has already found,
     * may be null
     * @return {@link Collection} of found package roots
     * @since 1.31
     */
    public static Set<? extends FileObject> findSourceRoots(final @NonNull FileObject folder, final @NullAllowed AtomicBoolean canceled) {
        Parameters.notNull("folder", folder);   //NOI18N
        if (!folder.isValid()) {
            throw new IllegalArgumentException("Folder: " + FileUtil.getFileDisplayName(folder)+" is not valid.");  //NOI18N
        }
        if (!folder.isFolder()) {
            throw new IllegalArgumentException("The parameter: " + FileUtil.getFileDisplayName(folder) + " has to be a directory.");    //NOI18N
        }
        final Set<FileObject> result = new HashSet<FileObject>();
        findAllSourceRoots(folder, result, canceled, 0);
        return Collections.unmodifiableSet(result);
    }
    /**
     * Returns package root of the given java or class file.
     *
     * @param fo either .java or .class file; never null
     * @return package root of the given file or null if none found
     */
    public static FileObject findPackageRoot(final FileObject fo) {
        if ("java".equals(fo.getExt())) { // NOI18N
            return findJavaPackage (fo);
        } else if ("class".equals(fo.getExt())) { // NOI18N
            return findClassPackage (fo);
        } else {
            throw new IllegalArgumentException("only java or class files accepted "+fo); // NOI18N
        }
    }

    private static FileObject findAllSourceRoots(final FileObject folder, final Collection<? super FileObject> result,
            final AtomicBoolean canceled, final int depth) {
        if (depth == 50) {
            return null;
        }
        final FileObject[] children = folder.getChildren();
        for (FileObject child : children) {
            if (canceled != null && canceled.get()) {
                return null;
            } else if (child.isData() && "text/x-java".equals(FileUtil.getMIMEType(child, "text/x-java"))) {   //NOI18N
                final FileObject root = findPackageRoot(child);
                if (root != null) {
                    result.add(root);
                }
                return root;
            } else if (child.isFolder()) {
                final FileObject upTo = findAllSourceRoots(child, result, canceled, depth+1);
                if (upTo != null && !upTo.equals(child)) {
                    return upTo;
                }
            }
        }
        return null;
    }

    private static FileObject findJavadocRoot(FileObject fo, int level) {
        FileObject fo1 = fo.getFileObject("package-list", null); // NOI18N
        if (fo1 != null) {
            return fo;
        }
        if (level == HOW_MANY_DIRS_TO_TRAVERSE_DEEP) {
            return null;
        }
        for (FileObject fo2 : fo.getChildren()) {
            if (!fo2.isFolder()) {
                continue;
            }
            fo2 = findJavadocRoot(fo2, level+1);
            if (fo2 != null) {
                return fo2;
            }
        }
        return null;
    }

    private static FileObject findJavaSourceFile(FileObject fo, int level) {
        if (level == 999) { // ignore for now
            return null;
        }
        // go through files first:
        for (FileObject fo2 : fo.getChildren()) {
            if (fo2.isData() && "java".equals(fo2.getExt())) { // NOI18N
                return fo2;
            }
        }
        // now check sunfolders:
        for (FileObject fo2 : fo.getChildren()) {
            if (fo2.isFolder()) {
                fo2 = findJavaSourceFile(fo2, level+1);
                if (fo2 != null) {
                    return fo2;
                }
            }
        }
        return null;
    }

    static final Pattern JAVA_FILE, PACKAGE_INFO;
    static {
        String whitespace = "(?:(?://[^\n]*\n)|(?:/\\*.*?\\*/)|\\s)"; //NOI18N
        String javaIdentifier = "(?:\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)"; //NOI18N
        String packageStatement = "package" + whitespace + "+(" + javaIdentifier + "(?:\\." + javaIdentifier + ")*)" + whitespace + "*;"; //NOI18N
        JAVA_FILE = Pattern.compile("(?ms)" + whitespace + "*" + packageStatement + ".*", Pattern.MULTILINE | Pattern.DOTALL); //NOI18N
        // XXX this does not take into account annotations and imports:
        PACKAGE_INFO = Pattern.compile("(?ms)(?:.*" + whitespace + ")?" + packageStatement + whitespace + "*", Pattern.MULTILINE | Pattern.DOTALL); //NOI18N
    }

    @SuppressWarnings({"OS_OPEN_STREAM", "RR_NOT_CHECKED"})
    private static FileObject findJavaPackage(FileObject fo) {
        try {
            InputStream is = fo.getInputStream();
            try {
            // Try default encoding, probably good enough.
            Reader r = new BufferedReader(new InputStreamReader(is));
            r.mark(2);
            char[] cbuf = new char[2];
            r.read(cbuf, 0, 2);
            if (cbuf[0] == 255 && cbuf[1] == 254) { // BOM
                is.close();
                is = fo.getInputStream();
                r = new BufferedReader(new InputStreamReader(is, "Unicode")); //NOI18N
            } else {
                r.reset();
            }
            // TODO: perhaps limit and read just first 100kB and not whole file:
            StringBuilder b = new StringBuilder((int) fo.getSize());
            int read;
            char[] buf = new char[b.length() + 1];
            while ((read = r.read(buf)) != -1) {
                b.append(buf, 0, read);
            }
            Matcher m = (fo.getNameExt().equals("package-info.java") ? PACKAGE_INFO : JAVA_FILE).matcher(b); //NOI18N
            if (m.matches()) {
                String pkg = m.group(1);
                LOG.log(Level.FINE, "Found package declaration {0} in {1}", new Object[] {pkg, fo}); //NOI18N
                return getPackageRoot(fo, pkg);
            } else {
                // XXX probably not a good idea to infer the default package: return f.getParentFile();
                return null;
            }
            } finally {
                is.close();
            }
        } catch (IOException x) {
            Exceptions.printStackTrace(x);
            return null;
        }
    }

    private static FileObject getPackageRoot(FileObject javaOrClassFile, String packageName) {
        String suffix = '/' + packageName.replace('.', '/') + '/' + javaOrClassFile.getNameExt(); //NOI18N
        String fpath = javaOrClassFile.getPath();
        if (fpath.endsWith(suffix)) {
            FileObject fo = javaOrClassFile.getParent();
            String targetPath = fpath.substring(0, fpath.length() - suffix.length());
            while (!fo.getPath().equals(targetPath)) {
                fo = fo.getParent();
            }
            return fo;
        } else {
            return null;
        }
    }


    /**
     * Find java package in side .class file.
     *
     * @return package or null if not found
     */
    private static FileObject findClassPackage(FileObject file) {
        try {
            InputStream in = file.getInputStream();
            try {
                ClassFile cf = new ClassFile(in,false);
                ClassName cn = cf.getName();
                return getPackageRoot(file, cn.getPackage());
            } finally {
                in.close ();
            }
        } catch (FileNotFoundException fnf) {
            //Ignore it
            // The file was removed after checking it for isValid
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }
    
}
