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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import org.netbeans.modules.cnd.debugger.common.breakpoints.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;

/**
* Implementation of breakpoint on method.
*
* @author   Gordon Prieur (copied from Jan Jancura's JPDA implementation)
*/
public class LineBreakpointImpl extends BreakpointImpl<LineBreakpoint> {

    private String lastPath;
    
    public LineBreakpointImpl(LineBreakpoint breakpoint, GdbDebugger debugger) {
        super(breakpoint, debugger);
	lastPath = null;
        set();
    }

    @Override
    protected String getBreakpointCommand() {
        int lineNumber = getBreakpoint().getLineNumber();
	String bppath = getBreakpoint().getPath();
	String path = null;

	if (lastPath == null && bppath.indexOf(' ') == -1) {
	    path = debugger.getPathMap().getRemotePath(bppath,true);
	} else if (lastPath == null) {
	    path = debugger.getBestPath(bppath);
	} else if (lastPath.length() > 0) {
	    if (lastPath.equals(bppath)) {
		path = debugger.getBestPath(bppath);
	    } else {
		int pos = lastPath.lastIndexOf('/');
		if (pos >= 0) {
		    path = lastPath.substring(pos + 1);
		}
	    }
	}
        lastPath = path;
	if (path == null) {
	    return null;
	} else {
            // IZs 169200 & 174479 (send internal path for cygwin)
            if (debugger.isCygwin()) {
                path = WindowsSupport.getInstance().convertToCygwinPath(path);
            }
	    return path + ':' + lineNumber;
	}
    }

    @Override
    protected boolean alternateSourceRootAvailable() {
	return err != null && err.startsWith("No source file named ") && lastPath != null && lastPath.length() > 0; // NOI18N
    }
}
