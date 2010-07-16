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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class CachingArchiveClassLoader extends ClassLoader {

    private static final int INI_SIZE = 16384;

    private final Archive[] archives;
    private byte[] buffer;

    private CachingArchiveClassLoader(final @NonNull Archive[] archives, final ClassLoader parent) {
        super (parent);
        assert archives != null;
        this.archives = archives;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        final StringBuilder sb = new StringBuilder(FileObjects.convertPackage2Folder(name, '/'));
        sb.append(JavaFileObject.Kind.CLASS.extension);
        final FileObject file = findFileObject(sb.toString());
        if (file != null) {
            try {
                final int len = readJavaFileObject(file);
                return defineClass(name, buffer, 0, len);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return super.findClass(name);
    }

    @Override
    protected URL findResource(String name) {
        final FileObject file = findFileObject(name);
        if (file != null) {
            try {
                return file.toUri().toURL();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return super.findResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        @SuppressWarnings("UseOfObsoleteCollectionType")
        final Vector<URL> v = new Vector<URL>();
        for (Archive archive : archives) {
            final FileObject file = archive.getFile(name);
            if (file != null) {
                v.add(file.toUri().toURL());
            }
        }
        return v.elements();
    }

    private int readJavaFileObject(final FileObject jfo) throws IOException {
        if (buffer == null) {
            buffer = new byte[INI_SIZE];
        }
        int len = 0;
        final InputStream in = jfo.openInputStream();
        try {
            while (true) {
                if (buffer.length == len) {
                    byte[] nb = new byte[2*buffer.length];
                    System.arraycopy(buffer, 0, nb, 0, len);
                    buffer = nb;
                }
                int l = in.read(buffer,len,buffer.length-len);
                if (l<=0) {
                    break;
                }
                len+=l;
            }

        } finally {
            in.close();
        }
        return len;
    }

    private FileObject findFileObject(final String resName) {
        try {
            for (Archive archive : archives) {
                final FileObject file = archive.getFile(resName);
                if (file != null) {
                    return file;
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return null;
    }


    public static ClassLoader forClassPath(final @NonNull ClassPath classPath,
            final @NullAllowed ClassLoader parent) {
        Parameters.notNull("classPath", classPath); //NOI18N
        final List<ClassPath.Entry> entries = classPath.entries();
        final URL[] urls = new URL[entries.size()];
        final Iterator<ClassPath.Entry> eit = entries.iterator();
        for (int i=0; eit.hasNext(); i++) {
            urls[i] = eit.next().getURL();
        }
        return forURLs(urls, parent);
    }

    public static ClassLoader forURLs(final @NonNull URL[] urls,
            final @NullAllowed ClassLoader parent) {
        Parameters.notNull("urls", urls);       //NOI18N
        final List<Archive> archives = new ArrayList<Archive>(urls.length);
        for (URL url : urls) {
            final Archive arch = CachingArchiveProvider.getDefault().getArchive(url, false);
            if (arch != null) {
                archives.add(arch);
            }
        }
        return new CachingArchiveClassLoader(archives.toArray(new Archive[archives.size()]), parent);
    }

}
