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

package org.netbeans.modules.cnd.utils.filters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class ElfExecutableFileFilter extends FileFilterFactory.FileAndFileObjectFilter {

    private static ElfExecutableFileFilter instance = null;

    public ElfExecutableFileFilter() {
	super();
    }

    public static ElfExecutableFileFilter getInstance() {
	if (instance == null) {
            instance = new ElfExecutableFileFilter();
        }
	return instance;
    }
    
    @Override
    public String getDescription() {
	return(getString("FILECHOOSER_ELFEXECUTABLE_FILEFILTER")); // NOI18N
    }
    
    @Override
    public boolean accept(File f) {
	if(f != null) {
	    if(f.isDirectory()) {
		return true;
	    }
            FileObject fo = CndFileSystemProvider.toFileObject(f);
	    return (fo == null) ? checkElfHeader(f) : checkElfHeader(fo);
	}
	return false;
    }

    @Override
    public boolean accept(FileObject f) {
	if(f != null) {
	    if(f.isFolder()) {
		return true;
	    }
	    return checkElfHeader(f);
	}
	return false;
    }


    /** Check if this file's header represents an elf executable */
    private boolean checkElfHeader(Object f) {
        CndUtils.assertTrue((f instanceof File) || (f instanceof FileObject));
        byte b[] = new byte[18];
	int left = 18; // bytes left to read
	int offset = 0; // offset into b array
	InputStream is = null;
	try {
            if (f instanceof File) {
                is = new FileInputStream((File) f);
            } else { // (f instanceof FileObject)
                is = ((FileObject) f).getInputStream();
            }
	    while (left > 0) {
		int n = is.read(b, offset, left);
		if (n <= 0) {
		    // File isn't big enough to be an elf file...
		    return false;
		}
		offset += n;
		left -= n;
	    }
	} catch (Exception e) {
	    return false;
	} finally {
	    if (is != null) {
		try {
		    is.close();
		} catch (IOException e) {
		}
	    }
	}

	short e_type;
	if (b[5] == 1) {  // ELFDATA2LSB=1
	    // byte order on i386
	    e_type = (short) b[17];
	    e_type <<= 8;
	    e_type += (short) b[16];
	} else {
	    e_type = (short) b[16];
	    e_type <<= 8;
	    e_type += (short) b[17];
	}
	if (
	    // Elf header	    
	    (b[0] == 0x7f) && (b[1] == (byte) 'E') &&
	    (b[2] == (byte) 'L') && (b[3] == (byte) 'F') &&
	    // Executable
	    (e_type == 2) // ET_EXEC=2
	    ) {
	    return true;
	}
	return false;
    }

    /** Look up i18n strings here */
    private ResourceBundle bundle;
    private String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(ElfExecutableFileFilter.class);
	}
	return bundle.getString(s);
    }
}
