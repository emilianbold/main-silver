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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.perfan.stack.impl;

import org.netbeans.modules.dlight.core.stack.api.Function;
import org.netbeans.modules.dlight.core.stack.utils.FunctionNameUtils;

public class FunctionImpl implements Function {

    private final String longName;
    private final long objRef;
    private String sourceFileName;
    private String moduleName;
    private String moduleOffset;
    

    public FunctionImpl(String name, long objRef) {
        if (name == null) {
            throw new NullPointerException("Attempt to create a FunctionImpl object with NULL name"); // NOI18N
        }

        this.longName = name;
        this.objRef = objRef;
    }

    @Override
    public long getContextID() {
        return objRef;
    }

    @Override
    public String getName() {
        return longName;
    }

    @Override
    public String getSignature() {
        return longName;
    }

    public long getRef() {
        return objRef;
    }

    @Override
    public String toString() {
        return "Function: " + longName + " [" + objRef + "]"; // NOI18N
    }

    @Override
    public String getQuilifiedName() {
        if (longName.startsWith("<")) { // NOI18N
            return longName;
        }
        
        return FunctionNameUtils.getFunctionQName(longName);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FunctionImpl)) {
            return false;
        }

        FunctionImpl that = (FunctionImpl) obj;
        return (this.objRef == that.objRef && this.longName.equals(that.longName));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.longName != null ? this.longName.hashCode() : 0);
        hash = 29 * hash + (int) (this.objRef ^ (this.objRef >>> 32));
        return hash;
    }
    
    public synchronized  void setSourcefileName(String fileName){
        this.sourceFileName = fileName;
    }
    
    public synchronized  void setModuleName(String moduleName){
        this.moduleName = moduleName;
    }
    
    public synchronized void setModuleOffset(String moduleOffset){
        this.moduleOffset = moduleOffset;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String getModuleOffset() {
        return moduleOffset;
    }

    @Override
    public String getSourceFile() {
        return sourceFileName;
    }
}
