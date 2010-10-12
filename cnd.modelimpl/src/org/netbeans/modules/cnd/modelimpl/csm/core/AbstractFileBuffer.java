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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Vladimir Voskresensky
 */
public abstract class AbstractFileBuffer implements FileBuffer {
    private final CharSequence absPath;
    private Charset encoding;
    
    protected AbstractFileBuffer(CharSequence absPath) {
        if (CndUtils.isDebugMode()) {
            CndUtils.assertNormalized(new File(absPath.toString()));
        }
        this.absPath = FilePathCache.getManager().getString(absPath);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
    }

    @Override
    public CharSequence getAbsolutePath() {
        return absPath;
    }

    @Override
    public File getFile() {
        return new File(absPath.toString());
    }

    @Override
    public FileObject getFileObject() {
        return CndFileUtils.toFileObject(absPath); // XXX:FileObject conversion
    }

    @Override
    public final Reader getReader() throws IOException {
        if (encoding == null) {
            File file = getFile();
            // file must be normalized
            FileObject fo = CndFileUtils.toFileObject(file);
            if (fo != null && fo.isValid()) {
                encoding = FileEncodingQuery.getEncoding(fo);
            } else { // paranoia
                encoding = FileEncodingQuery.getDefaultEncoding();
            }
        }
        InputStream is = getInputStream();
        Reader reader = new InputStreamReader(is, encoding);
        return reader;
    }
    
    public abstract InputStream getInputStream() throws IOException;
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    protected void write(DataOutput output) throws IOException {
        assert this.absPath != null;
        PersistentUtils.writeUTF(absPath, output);
    }  
    
    protected AbstractFileBuffer(DataInput input) throws IOException {
        this.absPath = PersistentUtils.readUTF(input, FilePathCache.getManager());
        assert this.absPath != null;
    }

    @Override
    public int getLineByOffset(int offset) throws IOException {
        int[] list = getLineOffsets();
	int low = 0;
	int high = list.length - 1;
	while (low <= high) {
	    int mid = (low + high) >>> 1;
	    int midVal = list[mid];
	    if (midVal < offset) {
                if (low == high) {
                    return low + 1;
                }
                low = mid + 1;
            } else if (midVal > offset) {
                if (low == high) {
                    return low;
                }
                high = mid - 1;
            } else {
                return mid + 1;
            }
	}
	return low;
    }

    @Override
    public int getStartLineOffset(int line) throws IOException {
        line--;
        int[] list = getLineOffsets();
        if (line < list.length) {
            return list[line];
        }
        return list[list.length-1];
    }
    
    private WeakReference<Object> lines = new WeakReference<Object>(null);
    private int[] getLineOffsets() throws IOException {
        WeakReference<Object> aLines = lines;
        int[] res = null;
        if (aLines != null) {
            res = (int[]) aLines.get();
        }
        if (res == null) {
            String text = getText();
            int length = text.length();
            ArrayList<Integer> list = new ArrayList<Integer>(length/10);
            // find line and column
            list.add(Integer.valueOf(0));
            for (int curOffset = 0; curOffset < length; curOffset++) {
                char curChar = text.charAt(curOffset);
                if (curChar == '\n') {
                    list.add(Integer.valueOf(curOffset+1));
                }
            }
            res = new int[list.size()];
            for (int i = 0; i < list.size(); i++){
                res[i] = list.get(i);
            }
            lines = new WeakReference<Object>(res);
        }
        return res;
    }

    protected void clearLineCache() {
        WeakReference<Object> aLines = lines;
        if (aLines != null) {
            aLines.clear();
        }
    }
}
