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
package org.netbeans.modules.php.editor.model.impl;

import java.util.*;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class TypeNameResolver {

    public abstract QualifiedName resolve(final QualifiedName qualifiedName);

    public static TypeNameResolver forNull() {
        return new TypeNameResolver() {

            @Override
            public QualifiedName resolve(final QualifiedName qualifiedName) {
                return qualifiedName;
            }
        };
    }

    public static TypeNameResolver forChainOf(final List<TypeNameResolver> typeNameResolvers) {
        return new TypeNameResolver() {

            @Override
            public QualifiedName resolve(final QualifiedName qualifiedName) {
                QualifiedName result = qualifiedName;
                for (TypeNameResolver nameResolver : typeNameResolvers) {
                    result = nameResolver.resolve(result);
                }
                return result;
            }
        };
    }

    public static TypeNameResolver forFullyQualifiedName(final Scope scope, final int offset) {
        return new TypeNameResolver() {

            @Override
            public QualifiedName resolve(final QualifiedName qualifiedName) {
                return VariousUtils.getFullyQualifiedName(qualifiedName, offset, scope);
            }
        };
    }

    public static TypeNameResolver forQualifiedName(final Scope scope, final int offset) {
        return new CommonQualifiedTypeNameResolver(scope, offset);
    }

    public static TypeNameResolver forUnqualifiedName() {
        return new TypeNameResolver() {

            @Override
            public QualifiedName resolve(final QualifiedName qualifiedName) {
                return qualifiedName.toName();
            }
        };
    }

    public static TypeNameResolver forSmartName(final Scope scope, final int offset) {
        return new SmartQualifiedTypeNameResolver(scope, offset);
    }





    private static abstract class BaseTypeNameResolver extends TypeNameResolver {
        private final Scope scope;
        private final int offset;

        public BaseTypeNameResolver(final Scope scope, final int offset) {
            this.scope = scope;
            this.offset = offset;
        }

        protected abstract QualifiedName processFullyQualifiedName(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope);

        protected abstract QualifiedName processQualifiedName(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope);

        protected abstract QualifiedName processUnQualifiedName(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope);

        @Override
        public QualifiedName resolve(final QualifiedName qualifiedName) {
            QualifiedName result = qualifiedName;
            if (!VariousUtils.isSpecialClassName(qualifiedName.getName()) && !VariousUtils.isPrimitiveType(qualifiedName.toString())) {
                result = processExactType(qualifiedName);
            }
            return result;
        }

        protected int getOffset() {
            return offset;
        }

        private QualifiedName processExactType(final QualifiedName qualifiedName) {
            QualifiedName result = qualifiedName;
            NamespaceScope namespaceScope = retrieveNamespaceScope();
            if (namespaceScope != null) {
                if (qualifiedName.getKind().isFullyQualified()) {
                    result = processFullyQualifiedName(qualifiedName, namespaceScope);
                } else if (qualifiedName.getKind().isQualified()) {
                    result = processQualifiedName(qualifiedName, namespaceScope);
                } else {
                    result = processUnQualifiedName(qualifiedName, namespaceScope);
                }
            }
            return result;
        }

        private NamespaceScope retrieveNamespaceScope() {
            NamespaceScope result = null;
            Scope inScope = scope;
            while (inScope != null && !(inScope instanceof NamespaceScope)) {
                inScope = inScope.getInScope();
            }
            if (inScope != null) {
                result = (NamespaceScope) inScope;
            }
            return result;
        }
    }





    private static interface QualifiedTypeNameResolver {

        public QualifiedName resolveForUseScope(final QualifiedName fullyQualifiedName, final UseScope matchedUseScope);

    }




    private static class CommonQualifiedTypeNameResolver extends BaseTypeNameResolver implements QualifiedTypeNameResolver {

        public CommonQualifiedTypeNameResolver(Scope scope, int offset) {
            super(scope, offset);
        }

        @Override
        protected QualifiedName processFullyQualifiedName(QualifiedName fullyQualifiedName, NamespaceScope namespaceScope) {
            return new FullyQualifiedNameProcessor(this, getOffset()).process(fullyQualifiedName, namespaceScope);
        }

        @Override
        protected QualifiedName processQualifiedName(QualifiedName qualifiedName, NamespaceScope namespaceScope) {
            return resolveNonFullyQualifiedName(qualifiedName, namespaceScope);
        }

        @Override
        protected QualifiedName processUnQualifiedName(QualifiedName unQualifiedName, NamespaceScope namespaceScope) {
            return resolveNonFullyQualifiedName(unQualifiedName, namespaceScope);
        }

        @Override
        public QualifiedName resolveForUseScope(final QualifiedName fullyQualifiedName, final UseScope matchedUseScope) {
            int skipLength = FullyQualifiedNameProcessor.countSkipLength(matchedUseScope);
            return QualifiedName.create(fullyQualifiedName.toString().substring(skipLength));
        }

        private QualifiedName resolveNonFullyQualifiedName(final QualifiedName nonFullyQualifiedName, final NamespaceScope namespaceScope) {
            QualifiedName result = nonFullyQualifiedName;
            UseScope matchedUseScope = getMatchedUseScopeForNonFullyQualifiedName(nonFullyQualifiedName, namespaceScope);
            if (matchedUseScope == null) {
                // passed qualified name is not valid, so construct QN with current NS
                result = namespaceScope.getNamespaceName().append(nonFullyQualifiedName);
            }
            return result;
        }

        private UseScope getMatchedUseScopeForNonFullyQualifiedName(final QualifiedName nonFullyQualifiedName, final NamespaceScope namespaceScope) {
            UseScope result = null;
            String firstSegmentName = nonFullyQualifiedName.getSegments().getFirst();
            int lastOffset = -1;
            for (UseScope useScope : namespaceScope.getDeclaredUses()) {
                if (useScope.getOffset() < getOffset()) {
                    AliasedName aliasName = useScope.getAliasedName();
                    if (aliasName != null) {
                        if (firstSegmentName.equals(aliasName.getAliasName())) {
                            result = useScope;
                            continue;
                        }
                    } else {
                        if (lastOffset < useScope.getOffset() && useScope.getName().endsWith(firstSegmentName)) {
                            result = useScope;
                            lastOffset = useScope.getOffset();
                        }
                    }
                }
            }
            return result;
        }
    }





    @NbBundle.Messages({
        "# {0} - Class name",
        "IllegalArgument=Only fully-qualified names can be resolved by {0}"
    })
    private static class SmartQualifiedTypeNameResolver extends BaseTypeNameResolver implements QualifiedTypeNameResolver {

        public SmartQualifiedTypeNameResolver(final Scope scope, final int offset) {
            super(scope, offset);
        }

        @Override
        public QualifiedName resolveForUseScope(final QualifiedName fullyQualifiedName, final UseScope matchedUseScope) {
            QualifiedName result = fullyQualifiedName;
            if (matchedUseScope != null) {
                int skipLength = FullyQualifiedNameProcessor.countSkipLength(matchedUseScope);
                result = QualifiedName.create(fullyQualifiedName.toString().substring(skipLength));
            }
            return result;
        }

        @Override
        protected QualifiedName processFullyQualifiedName(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope) {
            return new FullyQualifiedNameProcessor(this, getOffset()).process(fullyQualifiedName, namespaceScope);
        }

        @Override
        protected QualifiedName processQualifiedName(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope) {
            throw new IllegalArgumentException(Bundle.IllegalArgument(this.getClass().getName()));
        }

        @Override
        protected QualifiedName processUnQualifiedName(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope) {
            throw new IllegalArgumentException(Bundle.IllegalArgument(this.getClass().getName()));
        }
    }





    private static class FullyQualifiedNameProcessor {
        private final int offset;
        private final QualifiedTypeNameResolver qualifiedTypeNameResolver;

        public static int countSkipLength(final UseScope matchedUseElement) {
            int result = NamespaceDeclarationInfo.NAMESPACE_SEPARATOR.length();
            if (matchedUseElement != null) {
                List<String> segments = createSegments(matchedUseElement);
                if (!segments.isEmpty()) {
                    result += QualifiedName.create(true, segments).toString().length();
                }
            }
            return result;
        }

        private static List<String> createSegments(final UseScope matchedUseElement) {
            List<String> segments = new ArrayList();
            for (StringTokenizer st = new StringTokenizer(matchedUseElement.getName(), NamespaceDeclarationInfo.NAMESPACE_SEPARATOR); st.hasMoreTokens();) {
                String token = st.nextToken();
                if (st.hasMoreTokens()) {
                    segments.add(token);
                }
            }
            return Collections.unmodifiableList(segments);
        }

        private static boolean isFromCurrentNamespace(final QualifiedName fullyQualifiedName, final QualifiedName namespaceName) {
            return fullyQualifiedName.toString().substring(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR.length()).startsWith(namespaceName.toString() + NamespaceDeclarationInfo.NAMESPACE_SEPARATOR);
        }

        public FullyQualifiedNameProcessor(final QualifiedTypeNameResolver qualifiedTypeNameResolver, final int offset) {
            this.qualifiedTypeNameResolver = qualifiedTypeNameResolver;
            this.offset = offset;
        }

        public QualifiedName process(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope) {
            QualifiedName result = fullyQualifiedName;
            if (namespaceScope != null) {
                QualifiedName namespaceName = namespaceScope.getNamespaceName();
                if (isFromCurrentNamespace(fullyQualifiedName, namespaceName)) {
                    result = resolveFromCurrentNamespace(fullyQualifiedName, namespaceName);
                } else {
                    result = resolveFromAnotherNamespace(fullyQualifiedName, namespaceScope.getDeclaredUses());
                }
            }
            return result;
        }

        private QualifiedName resolveFromCurrentNamespace(final QualifiedName fullyQualifiedName, final QualifiedName namespaceName) {
            int namespaceNameSegmentsSize = namespaceName.getSegments().size();
            int qualifiedNameSegmentsSize = fullyQualifiedName.getSegments().size();
            assert namespaceNameSegmentsSize < qualifiedNameSegmentsSize : namespaceName.toString() + ":" + namespaceNameSegmentsSize + " < " + fullyQualifiedName.toString() + ":" + qualifiedNameSegmentsSize; //NOI18N
            return QualifiedName.create(fullyQualifiedName.toString().substring(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR.length() + namespaceName.toString().length() + NamespaceDeclarationInfo.NAMESPACE_SEPARATOR.length()));
        }

        private QualifiedName resolveFromAnotherNamespace(final QualifiedName fullyQualifiedName, final Collection<? extends UseScope> declaredUses) {
            UseScope matchedUseScope = getMatchedUseScopeForFullyQualifiedName(fullyQualifiedName, declaredUses);
            return qualifiedTypeNameResolver.resolveForUseScope(fullyQualifiedName, matchedUseScope);
        }

        private UseScope getMatchedUseScopeForFullyQualifiedName(final QualifiedName fullyQualifiedName, final Collection<? extends UseScope> declaredUses) {
            UseScope result = null;
            String firstSegmentName = fullyQualifiedName.getSegments().getFirst();
            int lastOffset = -1;
            for (UseScope useScope : declaredUses) {
                if (useScope.getOffset() < offset) {
                    AliasedName aliasName = useScope.getAliasedName();
                    if (aliasName != null) {
                        if (firstSegmentName.equals(aliasName.getAliasName())) {
                            result = useScope;
                            continue;
                        }
                    } else {
                        if (lastOffset < useScope.getOffset()) {
                            String useElementName = useScope.getName();
                            String modifiedUseElementName = useElementName.startsWith(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR) ? useElementName : NamespaceDeclarationInfo.NAMESPACE_SEPARATOR + useElementName;
                            if (fullyQualifiedName.toString().startsWith(modifiedUseElementName)) {
                                lastOffset = useScope.getOffset();
                                result = useScope;
                            }
                        }
                    }
                }
            }
            return result;
        }
    }

}