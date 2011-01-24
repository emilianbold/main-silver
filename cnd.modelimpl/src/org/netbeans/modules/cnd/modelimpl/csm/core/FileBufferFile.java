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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */


package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.*;
import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vladimir Kvashin
 */
public class FileBufferFile extends AbstractFileBuffer {
    
    private volatile SoftReference<String> cachedString;
    private volatile long lastModifiedWhenCachedString;

    public FileBufferFile(FileObject fileObject) {
        super(fileObject);
    }
    
    @Override
    public String getText() throws IOException {
        return asString();
    }
    
    @Override
    public String getText(int start, int end) {
        try {
            String b = asString();
            if( end > b.length() ) {
                new IllegalArgumentException("").printStackTrace(System.err);
                end = b.length();
            }
            return b.substring(start, end);
        } catch( IOException e ) {
            DiagnosticExceptoins.register(e);
            return "";
        }
    }

    private String getEncoding() {
        FileObject fo = CndFileUtils.toFileObject(getAbsolutePath());
        Charset cs = null;
        if (fo != null && fo.isValid()) {
            cs = FileEncodingQuery.getEncoding(fo);
        }
        if (cs == null) {
            cs = FileEncodingQuery.getDefaultEncoding();
        }
        return cs.name();
    }
    
    private synchronized String asString() throws IOException {
        byte[] b;
        long newLastModified = lastModified();
        if( cachedString != null  ) {
            Object o = cachedString.get();
            if( o != null && (lastModifiedWhenCachedString == newLastModified)) {
                return (String) o;
            }
        }
        // either bytes == null or bytes.get() == null
        b = doGetBytes();
        String str = new String(b, getEncoding());
        if (lastModifiedWhenCachedString != newLastModified) {
            clearLineCache();
        }
        lastModifiedWhenCachedString = newLastModified;
        cachedString = new SoftReference<String>(str);
        return str;
    }

    private byte[] doGetBytes() throws IOException {
        FileObject fo = getFileObject();
        long length = fo.getSize();
        if (length > Integer.MAX_VALUE) {
            new IllegalArgumentException("File is too large: " + fo.getPath()).printStackTrace(System.err); // NOI18N
        }
        byte[] readBytes = new byte[(int)length];
        InputStream is = getInputStream();
        try {
            int offset = 0;
            int numRead = 0;
            while (offset < readBytes.length && (numRead = is.read(readBytes, offset, readBytes.length-offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            is.close();
        }
        convertLSToLF(readBytes);
        return readBytes;
    }
    
    private static int convertLSToLF(byte[] bytes) {
        int len = bytes.length;
        int tgtOffset = 0;
        short lsLen = 0;
        int moveStart = 0;
        int moveLen;
        for (int i = 0; i < len; i++) {
            if (bytes[i] == '\r') {
                if (i + 1 < len && bytes[i + 1] == '\n') {
                    lsLen = 2;
                } else  {
                    lsLen = 1;
                }
            }
            if (lsLen > 0) {
                moveLen = i - moveStart;
                if (moveLen > 0) {
                    if (tgtOffset != moveStart) {
                        System.arraycopy(bytes, moveStart, bytes, tgtOffset, moveLen);
                    }
                    tgtOffset += moveLen;
                }
                bytes[tgtOffset++] = '\n';
                moveStart += moveLen + lsLen;
                i += lsLen - 1;
                lsLen = 0;
            }
        }
        moveLen = len - moveStart;
        if (moveLen > 0) {
            if (tgtOffset != moveStart) {
                System.arraycopy(bytes, moveStart, bytes, tgtOffset, moveLen);
            }
            tgtOffset += moveLen;
        }
        return tgtOffset;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        InputStream is;
        FileObject fo = getFileObject();
        if (fo != null && fo.isValid()) {
            is = fo.getInputStream();
        } else {
            throw new FileNotFoundException("Null file object for " + this.getAbsolutePath()); // NOI18N
        }
        return new BufferedInputStream(is, TraceFlags.BUF_SIZE);
    }
    
    @Override
    public int getLength() {
        return (int) getFileObject().getSize();
    }
    
    @Override
    public boolean isFileBased() {
        return true;
    }
    
    @Override
    public long lastModified() {
	return getFileObject().lastModified().getTime();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public FileBufferFile(DataInput input) throws IOException {
        super(input);
    }

    @Override
    public char[] getCharBuffer() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
}
