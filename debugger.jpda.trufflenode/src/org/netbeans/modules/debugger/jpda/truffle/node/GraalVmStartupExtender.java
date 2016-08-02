/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.truffle.node;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.api.NodeJsSupport;
import org.netbeans.spi.extexecution.startup.StartupExtenderImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

@NbBundle.Messages("DESC_DebugGraalNode=Debug GraalVM Node.js")
@StartupExtenderImplementation.Registration(displayName = "#DESC_DebugGraalNode", startMode = StartupExtender.StartMode.DEBUG, position=500)
public class GraalVmStartupExtender implements StartupExtenderImplementation {

    @NbBundle.Messages("CTL_DebugName=GraalVM node Debugger")
    @Override
    public List<String> getArguments(Lookup context, StartupExtender.StartMode mode) {
        Project p = context.lookup(Project.class);
        if (p == null) {
            return Collections.emptyList();
        }
        NodeJsSupport s = NodeJsSupport.getInstance();
        if (!s.isEnabled(p)) {
            return Collections.emptyList();
        }
        if (mode != StartupExtender.StartMode.DEBUG) {
            return Collections.emptyList();
        }
        final String node = s.getNode(p);
        File nodeFile = new File(node);
        nodeFile = FileUtil.normalizeFile(nodeFile);
        FileObject nodeFO = FileUtil.toFileObject(nodeFile);
        if (nodeFO == null) {
            return Collections.emptyList();
        }
        FileObject bin = nodeFO.getParent();
        if (bin == null || !isJavaPlatformBinDir(bin)) {
            return Collections.emptyList();
        }
        final String debugName = Bundle.CTL_DebugName();

        InputOutput io = IOProvider.getDefault().getIO(debugName, false);
        FileObject jdk = bin.getParent();
        if (jdk.getName().equals("jre")) {
            jdk = jdk.getParent();
        }
        JPDAStart start = new JPDAStart(io, debugName, jdk);
        String res = null;
        try {
            res = start.execute(p);
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
        }
        return Arrays.asList("-J-Xrunjdwp:transport=dt_socket,address=" + res + ",server=n,suspend=y");
    }

    private static boolean isJavaPlatformBinDir(FileObject dir) {
        if (!"bin".equals(dir.getNameExt())) {
            return false;
        }
        FileObject file = dir.getFileObject("java", BaseUtilities.isWindows() ? "exe" : null);
        if (file == null) {
            return false;
        }
        file = dir.getFileObject("graalvm", BaseUtilities.isWindows() ? "exe" : null);
        return file != null;
    }

}
