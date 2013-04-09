/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute.cmd;

import java.io.File;
import org.apache.maven.execution.ExecutionEvent;
import org.json.simple.JSONObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.execute.ExecutionEventObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
public class ExecMojo extends ExecutionEventObject {
        public final String goal;
        public final GAV plugin;
        public final String phase;
        public final String executionId;

    public ExecMojo(String goal, GAV plugin, String phase, String executionId, ExecutionEvent.Type type) {
        super(type);
        this.goal = goal;
        this.plugin = plugin;
        this.phase = phase;
        this.executionId = executionId;
    }

    
    public static ExecMojo create(JSONObject obj, ExecutionEvent.Type t) {
        JSONObject mojo = (JSONObject) obj.get("mojo");
        String id = (String) mojo.get("id");
        String[] ids = id.split(":");
        GAV mojoGav = new GAV(ids[0], ids[1], ids[2]);
        String goal = (String) mojo.get("goal");
        String execId = (String) mojo.get("execId");
        String phase = (String) mojo.get("phase");
        return new ExecMojo(goal, mojoGav, phase, execId, t);
//        JSONObject exc = (JSONObject) obj.get("exc");
//        if (exc != null) {
//            String message = (String) exc.get("msg");
//            if (message != null) {
//                try {
//                    byte[] bytes = Base64.decodeBase64(message.getBytes("UTF-8"));
//                    excMessage = new String(bytes, "UTF-8");
//                    System.out.println("exc message=" + excMessage);
//                } catch (UnsupportedEncodingException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//        }
    }
    
}
