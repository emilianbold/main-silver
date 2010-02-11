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

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Tomas Zezula
 */
public class NullWriteFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    public NullWriteFileManager (@NonNull final JavaFileManager delegate) {
        super (delegate);
    }

    @Override
    public javax.tools.FileObject getFileForOutput(Location l, String pkgName, String relativeName, javax.tools.FileObject sibling)
            throws IOException, UnsupportedOperationException, IllegalArgumentException {
            final FileObject fo = super.getFileForOutput(l, pkgName, relativeName, sibling);
        assert fo != null;
        return new NullFileObject((InferableJavaFileObject) fo);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location l, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling)
            throws IOException, UnsupportedOperationException, IllegalArgumentException {
            final JavaFileObject fo = super.getJavaFileForOutput(l, className, kind, sibling);
        assert fo != null;
        return new NullFileObject((InferableJavaFileObject) fo);    //todo: Covariant JavaFileManager returning InferableJavaFileObject
    }

    private static final class NullFileObject extends ForwardingInferableJavaFileObject {
        private NullFileObject (@NonNull final InferableJavaFileObject delegate) {
            super (delegate);
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return new NullOutputStream();
        }

        @Override
        public Writer openWriter() throws IOException {
            return new OutputStreamWriter(openOutputStream());
        }
    }


    private static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            //pass
        }
    }

}
