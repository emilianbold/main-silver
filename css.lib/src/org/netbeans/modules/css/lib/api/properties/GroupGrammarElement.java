/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.api.properties;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mfukala@netbeans.org
 */
public class GroupGrammarElement extends GrammarElement {

    int index;
    String referenceName = null;

    public enum Type {

        /**
         * One of the group members needs to be resolved.
         */
        SET,
        
        /** 
         * Any of the elements can be present in the value (at least one of them????).
         */
        COLLECTION,
        
        /**
         * All of the group members needs to be resolved in the defined order.
         */
        LIST,
        
        /**
         * All of the group members needs to be present in arbitrary order
         */
        ALL;
    }

    public GroupGrammarElement(GroupGrammarElement parent, int index, String referenceName) {
        this(parent, index);
        this.referenceName = referenceName;
    }

    public GroupGrammarElement(GroupGrammarElement parent, int index) {
        super(parent);
        this.index = index;
        this.type = Type.LIST; //default type
    }
    private List<GrammarElement> elements = new ArrayList<GrammarElement>(5);
    private Type type;

    @Override
    public GrammarElementKind getKind() {
        return GrammarElementKind.GROUP;
    }
    
    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }

    public List<GrammarElement> elements() {
        return elements;
    }

    public void addElement(GrammarElement element) {
        elements.add(element);
    }

    public List<GrammarElement> getAllPossibleValues() {
        List<GrammarElement> all = new ArrayList<GrammarElement>(10);
        if (getType() == Type.LIST) {
            //sequence
            GrammarElement e = elements.get(0); //first element
            if (e instanceof GroupGrammarElement) {
                all.addAll(((GroupGrammarElement) e).getAllPossibleValues());
            } else {
                all.add(e);
            }
        } else {
            //list or set
            for (GrammarElement e : elements()) {
                if (e instanceof GroupGrammarElement) {
                    all.addAll(((GroupGrammarElement) e).getAllPossibleValues());
                } else {
                    all.add(e);
                }
            }
        }
        return all;
    }
    
    public String getName() {
        return referenceName;
    }

    @Override
    public String toString2(int level) {
        StringBuilder sb = new StringBuilder();
        String heading = toString();
        heading = heading.substring(0, heading.length() - 1);
        sb.append(indentString(level)).append(heading); //NOI18N
        if (referenceName != null) {
            sb.append("(").append(referenceName).append(") "); //NOI18N
        }
        
        sb.append('\n');
        for (GrammarElement e : elements()) {
            sb.append(e.toString2(level + 1));
            sb.append('\n');
        }
        sb.append(indentString(level));
        sb.append(']');
        sb.append(super.toString());
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(getType().name().charAt(0));
        sb.append(index);
        if (referenceName != null) {
            sb.append("|").append(referenceName); //NOI18N
        }
        sb.append(']');
        return sb.toString(); //NOI18N
    }
}
