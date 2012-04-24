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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.profiler.results.cpu.cct.nodes;

import java.lang.ref.WeakReference;
import org.netbeans.lib.profiler.results.RuntimeCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.CPUCCTNodeFactory;


/**
 *
 * @author Jaroslav Bachorik
 */
public abstract class BaseCPUCCTNode implements RuntimeCPUCCTNode {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class ArrayChildren implements Children {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private RuntimeCPUCCTNode[] children;

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public RuntimeCPUCCTNode getChildAt(int i) {
            return (children != null) ? children[i] : null;
        }

        public void attachNode(RuntimeCPUCCTNode node) {
            addChildEntry();
            children[children.length - 1] = node;
        }

        public int size() {
            return (children != null) ? children.length : 0;
        }

        private void addChildEntry() {
            if (children == null) {
                children = new RuntimeCPUCCTNode[1];
            } else {
                RuntimeCPUCCTNode[] newch = new RuntimeCPUCCTNode[children.length + 1];
                System.arraycopy(children, 0, newch, 0, children.length);
                children = newch;
            }
        }
        
        public RuntimeCPUCCTNode[] toArray() {
            return children == null ? new RuntimeCPUCCTNode[0] : children;
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final ArrayChildren children;
    private final WeakReference factoryRef;
    final private long batchId;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** 
     * Creates a new instance of BaseCPUCCTNode 
     * @param batchId the batch number this node was created in
     * @param parentFactory a {@linkplain CPUCCTNodeFactory} factory instance used to create this node or null
     **/
    public BaseCPUCCTNode(long batchId, CPUCCTNodeFactory parentFactory) {
        if (parentFactory != null) {
            this.factoryRef = new WeakReference(parentFactory);
        } else {
            this.factoryRef = null;
        }

        this.children = new ArrayChildren();
        this.batchId = batchId;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    final public RuntimeCCTNode[] getChildren() {
        return children.toArray();
    }

    final public void attachNodeAsChild(RuntimeCPUCCTNode node) {
        children.attachNode(node);
    }
    
    final protected CPUCCTNodeFactory getFactory() {
        return (factoryRef != null) ? (CPUCCTNodeFactory) factoryRef.get() : null;
    }
    
    final public long getBatchId() {
        return batchId;
    }
}
