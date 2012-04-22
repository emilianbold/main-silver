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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.lib.api.elements;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mfukala@netbeans.org
 */
public class TreePath {

    private Element first,  last;
    
    public TreePath(Element last) {
        this(null, last);
    }
    
    /** @param first may be null; in such case a path from the root is created */
    public TreePath(Element first, Element last) {
        this.first = first;
        this.last = last;
    }

    public Element first() {
        return first;
    }
    
    public Element last() {
        return last;
    }
     
    /** returns a list of nodes from the first node to the last node including the boundaries. */
    public List<Element> path() {
        List<Element> path = new  ArrayList<Element>();
        Element node = last;
        while (node != null) {
            path.add(node);
            if(node == first) {
                break;
            }
            node = node.parent();
        }
        return path;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<Element> p = path();
        for(int i = p.size() - 1; i >= 0; i-- ) {
            Element node = p.get(i);
            Node parent = node.parent();
            int myIndex = parent == null ? 0 : indexInSimilarNodes(node.parent(), node);
            sb.append(node.id());
            if(myIndex > 0) {
                sb.append("(").append(myIndex).append(")");
            }
            sb.append('/');
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof TreePath)) {
            return false;
        }
        TreePath path = (TreePath)o;
        
        List<Element> p1 = path();
        List<Element> p2 = path.path();
        
        if(p1.size() != p2.size()) {
            return false;
        }
        
        for(int i = 0; i < p1.size(); i++) {
            Element n1 = p1.get(i);
            Element n2 = p2.get(i);
            
            Node n1Parent = n1.parent();
            Node n2Parent = n2.parent();
            
            if(n1Parent == null && n2Parent == null) {
                continue;
            }
            
            int n1Index = indexInSimilarNodes(n1Parent, n1);
            int n2Index = indexInSimilarNodes(n2Parent, n2);
            
            if(n1Index != n2Index) {
                return false;
            }
            
            String sig1 = getSignature(n1);
            String sig2 = getSignature(n2);
            if(!sig1.equals(sig2)) {
                return false;
            }
            
        }
        
        return true;
    }

    private String getSignature(Element node) {
        return new StringBuilder().append(node.id()).append("[").append(node.type()).append("]").toString();
    }
    
    private static int indexInSimilarNodes(Node parent, Element node) {
        int index = -1;
        for(Element child : parent.children()) {
            if(node.id().equals(child.id()) && node.type() == child.type()) {
                index++;
            }
            if(child == node) {
                break;
            }
        }
        return index;
    }
    
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.first != null ? this.first.hashCode() : 0);
        hash = 47 * hash + (this.last != null ? this.last.hashCode() : 0);
        return hash;
    }
    
    
    
}
