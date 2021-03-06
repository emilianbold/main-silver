/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.java.module.graph;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.ModuleElement;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class DependencyCalculator {

    private final FileObject moduleInfo;
    private Collection<? extends ModuleNode> nodes;
    private Collection<DependencyEdge> edges;

    public DependencyCalculator(
        @NonNull final FileObject moduleInfo) {
        Parameters.notNull("moduleInfo", moduleInfo);
        this.moduleInfo = moduleInfo;
    }

    @NonNull
    Collection<? extends ModuleNode> getNodes() {
        init();
        assert nodes != null;
        return nodes;
    }

    @NonNull
    Collection<DependencyEdge> getEdges() {
        init();
        assert edges != null;
        return edges;
    }

    private void init() {
        if (nodes == null) {
            assert edges == null;
            nodes = Collections.emptyList();
            edges = Collections.emptyList();
            final JavaSource js = JavaSource.forFileObject(moduleInfo);
            if (js != null) {
                try {
                    js.runUserActionTask((cc)-> {
                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        final List<? extends Tree> decls = cc.getCompilationUnit().getTypeDecls();
                        final ModuleElement me =  !decls.isEmpty() && decls.get(0).getKind() == Tree.Kind.MODULE ?
                                (ModuleElement) cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), decls.get(0))) :
                                null;
                        if (me != null) {
                            final Map<String, ModuleNode> mods = new LinkedHashMap<>();
                            final Collection<DependencyEdge> deps = new HashSet<>();
                            String name = me.getQualifiedName().toString();
                            ClasspathInfo classpathInfo = cc.getClasspathInfo();
                            ModuleNode node = new ModuleNode(name, me.isUnnamed(), isJDK(me, classpathInfo), moduleInfo);
                            mods.put(name, node);
                            collect(node, me, mods, deps, classpathInfo);
                            nodes = mods.values();
                            edges = deps;
                        }
                    }, true);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
    }

    private void collect(
        @NonNull ModuleNode meNode, 
        @NonNull ModuleElement me, 
        @NonNull Map<String, ModuleNode> mods, 
        @NonNull Collection<DependencyEdge> deps,
        ClasspathInfo classpathInfo) {        
        for (Dependency d : collect(me, mods, deps, classpathInfo)) {
            meNode.addChild(d.node);
            d.node.setParent(meNode);
            deps.add(new DependencyEdge(meNode, d.node, d.reqD.isPublic(), false));
        }
        deps.addAll(collectTransitiveDependencies(new HashSet<>(deps)));
    }
    
    private Collection<Dependency> collect(  
        @NonNull final ModuleElement me,
        @NonNull final Map<String, ModuleNode> mods,
        @NonNull final Collection<DependencyEdge> deps,
        ClasspathInfo classpathInfo) {
        List<Dependency> dependencies = new ArrayList<>();
        if (!me.isUnnamed()) {
            for (ModuleElement.Directive d : me.getDirectives()) {
                if (d.getKind() == ModuleElement.DirectiveKind.REQUIRES) {
                    final ModuleElement.RequiresDirective reqD = (ModuleElement.RequiresDirective) d;
                    final ModuleElement reqMod = reqD.getDependency();
                    final String name = reqMod.getQualifiedName().toString();
                    boolean unseen;
                    ModuleNode n = mods.get(name);
                    if(n == null) {                        
                        n = new ModuleNode(name, reqMod.isUnnamed(), isJDK(reqMod, classpathInfo), moduleInfo);
                        mods.put(name, n);
                        unseen = true;
                    } else {
                        unseen = false;
                    }                                        
                    dependencies.add(new Dependency(n, reqD, unseen));
                }
            }
            
            for (Dependency d : dependencies) {
                if(d.unseen) {
                    collect(d.node, d.reqD.getDependency(), mods, deps, classpathInfo);
                }
            }
        }
        return dependencies;
    }   

    private boolean isJDK(final ModuleElement me, ClasspathInfo cpinfo) {
        boolean isJDK = false;
        Symbol.ClassSymbol mi = ((Symbol.ModuleSymbol) me).module_info;
        JavaFileObject cf = mi != null ? mi.classfile : null;
        if(cf != null) {
            URI uri = cf.toUri();
            ClassPath cp = cpinfo.getClassPath(ClasspathInfo.PathKind.BOOT);
            try {
                isJDK = cp.findOwnerRoot(URLMapper.findFileObject(uri.toURL())) != null;
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return isJDK;
    }
    
    Collection<DependencyEdge> collectTransitiveDependencies(Collection<DependencyEdge> deps) {
        Map<ModuleNode, List<ModuleNode>> publicEdges = deps.stream()
                .filter((e) -> e.isPublic())
                .collect(Collectors.groupingBy(DependencyEdge::getSource, 
                                               Collectors.mapping(DependencyEdge::getTarget, Collectors.toList())));
    
        Collection<DependencyEdge> transitiveEdges = new HashSet<>();
        for (DependencyEdge dep : deps) {
            List<ModuleNode> targets = publicEdges.get(dep.getTarget());
            if(targets != null) {
                ModuleNode source = dep.getSource();
                transitiveEdges.addAll(toDependencyEdges(source, targets));
                Collection<ModuleNode> transTargets = new HashSet<>();
                collectTransTargets(targets, publicEdges, transTargets);
                transitiveEdges.addAll(toDependencyEdges(source, transTargets));
            }
        }
        return transitiveEdges;
    }

    private void collectTransTargets(List<ModuleNode> sources, Map<ModuleNode, List<ModuleNode>> publicEdges, Collection<ModuleNode> transTargets) {
        for (ModuleNode source : sources) {
            List<ModuleNode> targets = publicEdges.get(source);
            if(targets != null) {                
                List<ModuleNode> ts = new LinkedList<>();
                for (ModuleNode target : targets) {
                    if(!transTargets.contains(target)) {
                        ts.add(target);
                    }
                }
                transTargets.addAll(ts);
                collectTransTargets(ts, publicEdges, transTargets);
            }
        }       
    }

    private static Collection<DependencyEdge> toDependencyEdges(ModuleNode source, Collection<ModuleNode> targets) {
        return targets.stream().map(target -> new DependencyEdge(source, target, false, true)).collect(Collectors.toList());
    }

    private static class Dependency {
        final ModuleNode node;
        final boolean unseen;
        final ModuleElement.RequiresDirective reqD;

        public Dependency(ModuleNode node, ModuleElement.RequiresDirective reqD, boolean unseen) {
            this.node = node;
            this.unseen = unseen;
            this.reqD = reqD;
        }

        @Override
        public String toString() {
            return String.valueOf(reqD);
        }
    }
    
}
