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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.callgraph.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.netbeans.modules.cnd.callgraph.api.CallModel;
import org.netbeans.modules.cnd.callgraph.api.Function;

/**
 *
 * @author Alexander Simon
 */
public class CallGraphState {
    private CallModel model;
    private CallGraphScene scene;
    private Action[] actions;
    private Map<Function, Boolean> calleesExpanded = new HashMap<Function, Boolean>();
    private Map<Function, Boolean> callersExpanded = new HashMap<Function, Boolean>();
    
    public CallGraphState(CallModel model, CallGraphScene scene, Action[] actions){
        this.model = model;
        this.scene = scene;
        this.actions = actions;
    }

    public List<Call> getCallers(Function declaration, boolean showOverriding) {
        callersExpanded.put(declaration, true);
        return model.getCallers(declaration, showOverriding);
    }

    public List<Call> getCallees(Function definition, boolean showOverriding) {
        calleesExpanded.put(definition, true);
        return model.getCallees(definition, showOverriding);
    }

    public void doLayout(){
        if (scene != null) {
            scene.doLayout();
        }
    }
    
    public void addCallToScene(Call element){
        if (scene != null) {
            scene.addCallToScene(element);
        }
    }
    
    public void addFunctionToScene(Function element){
        if (scene != null) {
            scene.addFunctionToScene(element);
        }
    }

    public boolean isCalleesExpanded(Function element) {
        Boolean expanded = calleesExpanded.get(element);
        if (expanded == null) {
            return false;
        }
        return expanded;
    }

    public boolean isCallersExpanded(Function element) {
        Boolean expanded = callersExpanded.get(element);
        if (expanded == null) {
            return false;
        }
        return expanded;
    }

    public Action[] getActions() {
        return actions;
    }
}
