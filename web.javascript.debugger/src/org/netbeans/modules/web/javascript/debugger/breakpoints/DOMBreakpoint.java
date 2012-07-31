/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;

/**
 *
 * @author Martin
 */
public class DOMBreakpoint extends AbstractBreakpoint {
    
    public static final String PROP_TYPES = "types";        // NOI18N

    public enum Type {
        
        SUBTREE_MODIFIED(Debugger.DOM_BREAKPOINT_SUBTREE),
        ATTRIBUTE_MODIFIED(Debugger.DOM_BREAKPOINT_ATTRIBUTE),
        NODE_REMOVED(Debugger.DOM_BREAKPOINT_NODE);
        
        private String typeString;
        
        private Type(String typeString) {
            this.typeString = typeString;
        }
        
        public String getTypeString() {
            return typeString;
        }
    }
    
    private boolean onSubtreeModification;
    private boolean onAttributeModification;
    private boolean onNodeRemoval;
    private Set<Type> types;
    private final DOMNode node;
    
    public DOMBreakpoint(DOMNode node) {
        this.node = node;
    }

    public DOMNode getNode() {
        return node;
    }

    public synchronized boolean isOnSubtreeModification() {
        return onSubtreeModification;
    }

    public void setOnSubtreeModification(boolean onSubtreeModification) {
        Set<Type> oldTypes;
        Set<Type> newTypes;
        synchronized (this) {
            oldTypes = types;
            this.onSubtreeModification = onSubtreeModification;
            if (types == null) {
                newTypes = null;
            } else {
                types = createTypes();
                newTypes = types;
            }
        }
        firePropertyChange(PROP_TYPES, oldTypes, newTypes);
    }

    public synchronized boolean isOnAttributeModification() {
        return onAttributeModification;
    }

    public void setOnAttributeModification(boolean onAttributeModification) {
        Set<Type> oldTypes;
        Set<Type> newTypes;
        synchronized (this) {
            oldTypes = types;
            this.onAttributeModification = onAttributeModification;
            if (types == null) {
                newTypes = null;
            } else {
                types = createTypes();
                newTypes = types;
            }
        }
        firePropertyChange(PROP_TYPES, oldTypes, newTypes);
    }

    public synchronized boolean isOnNodeRemoval() {
        return onNodeRemoval;
    }

    public void setOnNodeRemoval(boolean onNodeRemoval) {
        Set<Type> oldTypes;
        Set<Type> newTypes;
        synchronized (this) {
            oldTypes = types;
            this.onNodeRemoval = onNodeRemoval;
            if (types == null) {
                newTypes = null;
            } else {
                types = createTypes();
                newTypes = types;
            }
        }
        firePropertyChange(PROP_TYPES, oldTypes, newTypes);
    }
    
    public synchronized Set<Type> getTypes() {
        if (types == null) {
            types = createTypes();
        }
        return types;
    }
    
    public boolean addType(Type type) {
        boolean added = false;
        if (Type.SUBTREE_MODIFIED.equals(type)) {
            added = !isOnSubtreeModification();
            setOnSubtreeModification(true);
        }
        if (Type.ATTRIBUTE_MODIFIED.equals(type)) {
            added = !isOnAttributeModification();
            setOnAttributeModification(true);
        }
        if (Type.NODE_REMOVED.equals(type)) {
            added = !isOnNodeRemoval();
            setOnNodeRemoval(true);
        }
        return added;
    }
    
    public synchronized boolean removeType(Type type) {
        boolean removed = false;
        if (Type.SUBTREE_MODIFIED.equals(type)) {
            removed = isOnSubtreeModification();
            setOnSubtreeModification(false);
        }
        if (Type.ATTRIBUTE_MODIFIED.equals(type)) {
            removed = isOnAttributeModification();
            setOnAttributeModification(false);
        }
        if (Type.NODE_REMOVED.equals(type)) {
            removed = isOnNodeRemoval();
            setOnNodeRemoval(false);
        }
        return removed;
    }
    
    private Set<Type> createTypes() {
        Set<Type> ts = EnumSet.noneOf(Type.class);
        if (isOnSubtreeModification()) {
            ts.add(Type.SUBTREE_MODIFIED);
        }
        if (isOnAttributeModification()) {
            ts.add(Type.ATTRIBUTE_MODIFIED);
        }
        if (isOnNodeRemoval()) {
            ts.add(Type.NODE_REMOVED);
        }
        return Collections.unmodifiableSet(ts);
    }
    
    void setValidity(DOMNode.PathNotFoundException pnfex) {
        if (pnfex == null) {
            setValidity(VALIDITY.VALID, null);
        } else {
            setValidity(VALIDITY.INVALID, pnfex.getLocalizedMessage());
        }
    }
}
