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

package org.netbeans.modules.maven.execute;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.execution.ExecutionEvent;
import org.json.simple.JSONObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.execute.cmd.ExecMojo;
import org.netbeans.modules.maven.execute.cmd.ExecProject;
import org.netbeans.modules.maven.execute.cmd.ExecSession;
import org.openide.filesystems.FileUtil;
import org.openide.windows.IOPosition;

/**
 * a stub to be filled with parsed JSON values, vaguely related to ExecutionEventObject in maven codebase.
 * @author mkleint
 */

public class ExecutionEventObject {

    public final ExecutionEvent.Type type;

    public ExecutionEventObject(ExecutionEvent.Type type) {
        this.type = type;
    }

    
    public static class GAV {
        public final String groupId;
        public final String artifactId;
        public final String version;

        public GAV(@NonNull String groupId, @NonNull String artifactId, @NonNull String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
        
        public String getId() {
            return groupId + ":" + artifactId + ":" + version;
        }
        
    }
    
    private static final List<ExecutionEvent.Type> mojo_types = Arrays.asList(new ExecutionEvent.Type[] {
        ExecutionEvent.Type.MojoStarted, ExecutionEvent.Type.MojoFailed, ExecutionEvent.Type.MojoSucceeded, ExecutionEvent.Type.MojoSkipped
    });
    private static final List<ExecutionEvent.Type> project_types = Arrays.asList(new ExecutionEvent.Type[] {
        ExecutionEvent.Type.ProjectStarted, ExecutionEvent.Type.ProjectFailed, ExecutionEvent.Type.ProjectSucceeded, ExecutionEvent.Type.ProjectSkipped
    });
    private static final List<ExecutionEvent.Type> session_types = Arrays.asList(new ExecutionEvent.Type[] {
        ExecutionEvent.Type.SessionStarted, ExecutionEvent.Type.SessionEnded
    });
    
    public static ExecutionEventObject create(JSONObject obj) {
        String s = (String) obj.get("type");
        ExecutionEvent.Type t = ExecutionEvent.Type.valueOf(s);
        if (mojo_types.contains(t)) {
            return ExecMojo.create(obj, t);
        }
        if (project_types.contains(t)) {
            return ExecProject.create(obj, t);
        }
        if (session_types.contains(t)) {
            return ExecSession.create(obj, t);
        }
        return new ExecutionEventObject(t);
    }
    
    //experimental
    public static class Tree {
        public final ExecutionEventObject startEvent;
        public ExecutionEventObject endEvent;
        public final ExecutionEventObject.Tree parentNode;
        public final List<ExecutionEventObject.Tree> childrenNodes = new ArrayList<ExecutionEventObject.Tree>();
        private IOPosition.Position startOffset;
        private IOPosition.Position endOffset;
        

        public Tree(ExecutionEventObject current, ExecutionEventObject.Tree parent) {
            this.startEvent = current;
            this.parentNode = parent;
        }

        public IOPosition.Position getStartOffset() {
            return startOffset;
        }

        public void setStartOffset(IOPosition.Position startOffset) {
            this.startOffset = startOffset;
        }

        public IOPosition.Position getEndOffset() {
            return endOffset;
        }

        public void setEndOffset(IOPosition.Position endOffset) {
            this.endOffset = endOffset;
        }

        public void setEndEvent(ExecutionEventObject endEvent) {
            this.endEvent = endEvent;
            assert endEvent != null && endEvent.getClass().equals(startEvent.getClass());
        }
        
        public ExecutionEventObject.Tree findParentNodeOfType(ExecutionEvent.Type startType) {
            if (parentNode == null) {
                return null;
            }
            ExecutionEventObject event = parentNode.startEvent;
            if (event == null) {
                return null;
            }
            if (startType.equals(event.type)) {
                return parentNode;
            }
            return parentNode.findParentNodeOfType(startType);
        }
        
    }
    
}
