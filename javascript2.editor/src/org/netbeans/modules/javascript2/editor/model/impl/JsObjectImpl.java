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
package org.netbeans.modules.javascript2.editor.model.impl;

import java.util.*;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.model.*;

/**
 *
 * @author Petr Pisl
 */
public class JsObjectImpl extends JsElementImpl implements JsObject {

    final private HashMap<String, JsObject> properties;
    final private Identifier declarationName;
    final private JsObject parent;
    final private List<Occurrence> occurrences;
    final private Map<Integer, Collection<TypeUsage>> assignments;
    final private boolean hasName;
    
    public JsObjectImpl(JsObject parent, Identifier name, OffsetRange offsetRange) {
        super((parent != null ? parent.getFileObject() : null), name.getName(), false,  offsetRange, EnumSet.of(Modifier.PUBLIC));
        this.properties = new HashMap<String, JsObject>();
        this.declarationName = name;
        this.parent = parent;
        this.occurrences = new ArrayList<Occurrence>();
        this.assignments = new HashMap<Integer, Collection<TypeUsage>>();
        this.hasName = name.getOffsetRange().getStart() != name.getOffsetRange().getEnd();
    }
    
    public JsObjectImpl(JsObject parent, Identifier name, OffsetRange offsetRange, boolean isDeclared, Set<Modifier> modifiers) {
        super((parent != null ? parent.getFileObject() : null), name.getName(), isDeclared,  offsetRange, modifiers);
        this.properties = new HashMap<String, JsObject>();
        this.declarationName = name;
        this.parent = parent;
        this.occurrences = new ArrayList<Occurrence>();
        this.assignments = new HashMap<Integer, Collection<TypeUsage>>();
        this.hasName = name.getOffsetRange().getStart() != name.getOffsetRange().getEnd();
    }
    
    public JsObjectImpl(JsObject parent, Identifier name, OffsetRange offsetRange, boolean isDeclared) {
        this(parent, name, offsetRange, isDeclared, EnumSet.of(Modifier.PUBLIC));
    }
  
    protected JsObjectImpl(JsObject parent, String name, boolean isDeclared, OffsetRange offsetRange, Set<Modifier> modifiers) {
        super((parent != null ? parent.getFileObject() : null), name, isDeclared, offsetRange, modifiers);
        this.properties = new HashMap<String, JsObject>();
        this.declarationName = null;
        this.parent = parent;
        this.occurrences = Collections.EMPTY_LIST;
        this.assignments = Collections.EMPTY_MAP;
        this.hasName = false;
    }
    
    @Override
    public Identifier getDeclarationName() {
        return declarationName;
    }

    @Override
    public Kind getJSKind() {
        if (parent == null) {
            // global object
            return Kind.FILE;
        }
        if (isDeclared()) {
            if ("arguments".equals(getName())) {
                // special variable object of every function
                return Kind.VARIABLE;
            }
            if (!getAssignmentForOffset(getDeclarationName().getOffsetRange().getEnd()).isEmpty()
                && hasOnlyVirtualProperties()) {
                if (getParent().getParent() == null || getModifiers().contains(Modifier.PRIVATE)) {
                    return Kind.VARIABLE;
                } else {
                    return Kind.PROPERTY;
                }
            }
        } else {
            if(!getProperties().isEmpty()) {
                return Kind.OBJECT;
            }
        }
        if (getProperties().isEmpty()) {
            if (getParent().isAnonymous() && (getParent() instanceof AnonymousObject)) {
                return Kind.PROPERTY;
            }
            if (getParent().getParent() == null || getModifiers().contains(Modifier.PRIVATE)) {
                // variable or the global object
                return Kind.VARIABLE;
            }
            if (getParent() instanceof JsFunction) {
                if (isDeclared()) {
                    return getModifiers().contains(Modifier.PRIVATE) ? Kind.VARIABLE : Kind.PROPERTY;
                }
            }
            return Kind.PROPERTY;
        }
        return Kind.OBJECT;
    }
    
    private boolean hasOnlyVirtualProperties() {
        for(JsObject property: getProperties().values()) {
            if (property.isDeclared()) {
                return false;
            }
        }
        return true;
    }
    
    @Override 
    public Map<String, ? extends JsObject> getProperties() {
        return properties;
    }

    @Override
    public void addProperty(String name, JsObject property) {
        properties.put(name, property);
    }

    @Override
    public JsObject getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public JsObject getParent() {
        return parent;
    }
    
    @Override
    public int getOffset() {
        return declarationName.getOffsetRange().getStart();
    }

    @Override
    public List<Occurrence> getOccurrences() {
        return occurrences;
    }
    
    public void addOccurrence(OffsetRange offsetRange) {
        occurrences.add(new OccurrenceImpl(offsetRange, this));
    }
    
    public void addAssignment(Collection<TypeUsage> typeNames, int offset){
        Collection<TypeUsage> types = assignments.get(offset);
        if (types == null) {
            types = new ArrayList<TypeUsage>();
            assignments.put(offset, types);
        }
        types.addAll(typeNames);
    }
    
    public void addAssignment(TypeUsage typeName, int offset){
        Collection<TypeUsage> types = assignments.get(offset);
        if (types == null) {
            types = new ArrayList<TypeUsage>();
            assignments.put(offset, types);
        }
        types.add(new TypeUsageImpl(typeName.getType(), offset, ((TypeUsageImpl)typeName).isResolved()));
    }

    @Override
    public Collection<? extends TypeUsage> getAssignmentForOffset(int offset) {
        Collection<? extends TypeUsage> result = Collections.EMPTY_LIST;
        int closeOffset = -1;
        for(Integer position : assignments.keySet()) {
            if (closeOffset < position && position <= offset) {
                closeOffset = position;
                result = assignments.get(position);
            }
        }
        
        return result;
    }

    @Override
    public Collection<? extends TypeUsage> getAssignments() {
        List<TypeUsage> values;
        values = new ArrayList<TypeUsage>();
        for(Collection<? extends TypeUsage> types : assignments.values()) {
            values.addAll(types);
        }
        return Collections.unmodifiableCollection(values);
    }
    
    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean hasExactName() {
        return hasName;
    }
    
    public void resolveTypes() {
        Collection<TypeUsage> resolved = new ArrayList();
        for(Integer index: assignments.keySet()) {
            resolved.clear();
            Collection<TypeUsage> unresolved = assignments.get(index);
            for (TypeUsage type : unresolved) {
                if(!((TypeUsageImpl)type).isResolved()){
                    resolved.addAll(ModelUtils.resolveTypeFromSemiType(this, type));
                } else {
                    resolved.add(type);
                }
            }
            unresolved.clear();
            unresolved.addAll(resolved);
        }
        
    }
    
}
