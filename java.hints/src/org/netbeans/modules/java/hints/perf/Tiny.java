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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.perf;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.MatcherUtilities;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.UseOptions;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.JavaFix.TransformationContext;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class Tiny {

    static final boolean SC_IGNORE_SUBSTRING_DEFAULT = true;
    @BooleanOption(displayName = "#LBL_org.netbeans.modules.java.hints.perf.Tiny.SC_IGNORE_SUBSTRING", tooltip = "#TP_org.netbeans.modules.java.hints.perf.Tiny.SC_IGNORE_SUBSTRING", defaultValue=SC_IGNORE_SUBSTRING_DEFAULT)
    static final String SC_IGNORE_SUBSTRING = "ignore.substring";
    
    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.stringConstructor", description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.stringConstructor", category="performance", suppressWarnings="RedundantStringConstructorCall")
    @UseOptions(SC_IGNORE_SUBSTRING)
    @TriggerPattern(value="new java.lang.String($original)",
                    constraints=@ConstraintVariableType(variable="$original", type="java.lang.String"))
    public static ErrorDescription stringConstructor(HintContext ctx) {
        TreePath original = ctx.getVariables().get("$original");

        if (ctx.getPreferences().getBoolean(SC_IGNORE_SUBSTRING, SC_IGNORE_SUBSTRING_DEFAULT)) {
            if (   MatcherUtilities.matches(ctx, original, "$str1.substring($s)", true)
                || MatcherUtilities.matches(ctx, original, "$str2.substring($s, $e)", true)) {
                TreePath str = ctx.getVariables().get("$str1") != null ? ctx.getVariables().get("$str1") : ctx.getVariables().get("$str2");

                assert str != null;

                TypeMirror type = ctx.getInfo().getTrees().getTypeMirror(str);

                if (type != null && type.getKind() == TypeKind.DECLARED) {
                    TypeElement te = (TypeElement) ((DeclaredType) type).asElement();

                    if (te.getQualifiedName().contentEquals("java.lang.String")) {
                        return null;
                    }
                }
            }
        }

        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_StringConstructor");
        Fix f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$original");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_StringConstructor");
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, f);
    }


    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.stringEqualsEmpty", description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.stringEqualsEmpty", category="performance", enabled=false, suppressWarnings={"StringEqualsEmpty", "", "StringEqualsEmptyString"})
    @TriggerPattern(value="$string.equals(\"\")",
                    constraints=@ConstraintVariableType(variable="$string", type="java.lang.String"))
    public static ErrorDescription stringEqualsEmpty(HintContext ctx) {
        Fix f;
        if (ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_6) >= 0) {
            String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_StringEqualsEmpty16");
            f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$string.isEmpty()");
        } else {
            boolean not = ctx.getPath().getParentPath().getLeaf().getKind() == Kind.LOGICAL_COMPLEMENT;
            String fixDisplayName = NbBundle.getMessage(Tiny.class, not ? "FIX_StringEqualsEmptyNeg" : "FIX_StringEqualsEmpty");
            f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, not ? ctx.getPath().getParentPath() : ctx.getPath(), not ? "$string.length() != 0" : "$string.length() == 0");
        }
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_StringEqualsEmpty");
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, f);
    }


    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.lengthOneStringIndexOf", description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.lengthOneStringIndexOf", category="performance", enabled=false, suppressWarnings="SingleCharacterStringConcatenation")
    @TriggerPatterns({
        @TriggerPattern(value="$string.indexOf($toSearch)",
                        constraints={@ConstraintVariableType(variable="$string", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$toSearch", type="java.lang.String")}),
        @TriggerPattern(value="$string.lastIndexOf($toSearch)",
                        constraints={@ConstraintVariableType(variable="$string", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$toSearch", type="java.lang.String")}),
        @TriggerPattern(value="$string.indexOf($toSearch, $index)",
                        constraints={@ConstraintVariableType(variable="$string", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$toSearch", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$index", type="int")}),
        @TriggerPattern(value="$string.lastIndexOf($toSearch, $index)",
                        constraints={@ConstraintVariableType(variable="$string", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$toSearch", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$index", type="int")})
    })
    public static ErrorDescription lengthOneStringIndexOf(HintContext ctx) {
        TreePath toSearch = ctx.getVariables().get("$toSearch");

        if (toSearch.getLeaf().getKind() != Kind.STRING_LITERAL) {
            return null;
        }

        LiteralTree lt = (LiteralTree) toSearch.getLeaf();
        final String data = (String) lt.getValue();

        if (data.length() != 1) {
            return null;
        }

        int start = (int) ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getInfo().getCompilationUnit(), toSearch.getLeaf());
        int end   = (int) ctx.getInfo().getTrees().getSourcePositions().getEndPosition(ctx.getInfo().getCompilationUnit(), toSearch.getLeaf());
        final String literal = ctx.getInfo().getText().substring(start, end);

        Fix f = new JavaFix(ctx.getInfo(), toSearch) {
@Override protected String getText() {
return NbBundle.getMessage(Tiny.class, "FIX_LengthOneStringIndexOf");
}
@Override protected void performRewrite(TransformationContext ctx) {
WorkingCopy wc = ctx.getWorkingCopy();
TreePath tp = ctx.getPath();
String content;

if ("'".equals(data)) content = "\\'";
else if ("\"".equals(data)) content = "\"";
else {
content = literal;
if (content.length() > 0 && content.charAt(0) == '"') content = content.substring(1);
if (content.length() > 0 && content.charAt(content.length() - 1) == '"') content = content.substring(0, content.length() - 1);
}

wc.rewrite(tp.getLeaf(), wc.getTreeMaker().Identifier("'" + content + "'"));
}
}.toEditorFix();
        
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_LengthOneStringIndexOf", literal);
        
        return ErrorDescriptionFactory.forTree(ctx, toSearch, displayName, f);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.getClassInsteadOfDotClass", description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.getClassInsteadOfDotClass", category="performance", enabled=false, suppressWarnings="InstantiatingObjectToGetClassObject")
    @TriggerPattern(value="new $O($params$).getClass()")
    public static ErrorDescription getClassInsteadOfDotClass(HintContext ctx) {
        TreePath O = ctx.getVariables().get("$O");
        if (O.getLeaf().getKind() == Kind.PARAMETERIZED_TYPE) {
            O = new TreePath(O, ((ParameterizedTypeTree) O.getLeaf()).getType());
        }
        ctx.getVariables().put("$OO", O);//XXX: hack
        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_GetClassInsteadOfDotClass");
        Fix f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$OO.class");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_GetClassInsteadOfDotClass");

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, f);
    }

    private static final Set<Kind> KEEP_PARENTHESIS = EnumSet.of(Kind.MEMBER_SELECT);
    
    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.constantIntern", description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.constantIntern", category="performance", enabled=false, suppressWarnings="ConstantStringIntern")
    @TriggerPattern(value="$str.intern()",
                    constraints=@ConstraintVariableType(variable="$str", type="java.lang.String"))
    public static ErrorDescription constantIntern(HintContext ctx) {
        TreePath str = ctx.getVariables().get("$str");
        TreePath constant;
        if (str.getLeaf().getKind() == Kind.PARENTHESIZED) {
            constant = new TreePath(str, ((ParenthesizedTree) str.getLeaf()).getExpression());
        } else {
            constant = str;
        }
        if (!Utilities.isConstantString(ctx.getInfo(), constant))
            return null;
        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_ConstantIntern");
        String target;
        if (constant != str && KEEP_PARENTHESIS.contains(ctx.getPath().getParentPath().getLeaf().getKind())) {
            target = "$str";
        } else {
            target = "$constant";
            ctx.getVariables().put("$constant", constant);//XXX: hack
        }
        Fix f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), target);
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_ConstantIntern");

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, f);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.enumSet", description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.enumSet", category="performance", suppressWarnings="SetReplaceableByEnumSet", options=Options.QUERY)
    @TriggerPatterns({
        @TriggerPattern("new $coll<$param>($params$)")
    })
    public static ErrorDescription enumSet(HintContext ctx) {
        return enumHint(ctx, "java.util.Set", null, "ERR_Tiny_enumSet");
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.enumMap", description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.enumMap", category="performance", suppressWarnings="MapReplaceableByEnumMap")
    @TriggerPatterns({
        @TriggerPattern("new $coll<$param, $to>($params$)")
    })
    public static ErrorDescription enumMap(HintContext ctx) {
        Fix[] fixes;
        Collection<? extends TreePath> mvars = ctx.getMultiVariables().get("$params$");

        if (mvars != null && mvars.isEmpty()) {
            String displayName = NbBundle.getMessage(Tiny.class, "FIX_Tiny_enumMap");

            fixes = new Fix[] {
                JavaFixUtilities.rewriteFix(ctx, displayName, ctx.getPath(), "new java.util.EnumMap<$param, $to>($param.class)")
            };
        } else {
            fixes = new Fix[0];
        }

        return enumHint(ctx, "java.util.Map", "java.util.EnumMap", "ERR_Tiny_enumMap", fixes);
    }

    private static ErrorDescription enumHint(HintContext ctx, String baseName, String targetTypeName, String key, Fix... fixes) {
        Element type = ctx.getInfo().getTrees().getElement(ctx.getVariables().get("$param"));

        if (type == null || type.getKind() != ElementKind.ENUM) {
            return null;
        }

        Element coll = ctx.getInfo().getTrees().getElement(ctx.getVariables().get("$coll"));

        if (coll == null || coll.getKind() != ElementKind.CLASS) {
            return null;
        }
        
        TypeElement base = ctx.getInfo().getElements().getTypeElement(baseName);
        
        if (base == null) {
            return null;
        }

        Types t = ctx.getInfo().getTypes();

        if (!t.isSubtype(t.erasure(coll.asType()), t.erasure(base.asType()))) {
            return null;
        }

        if (targetTypeName != null) {
            TypeElement target = ctx.getInfo().getElements().getTypeElement(targetTypeName);

            if (target == null) {
                return null;
            }

            if (t.isSubtype(t.erasure(coll.asType()), t.erasure(target.asType()))) {
                return null;
            }
        }

        String displayName = NbBundle.getMessage(Tiny.class, key);

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, fixes);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.collectionsToArray",
          description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.collectionsToArray",
          category="performance",
          enabled=false,
          suppressWarnings="CollectionsToArray")
    @TriggerPattern(value = "$collection.toArray(new $clazz[0])",
                    constraints=@ConstraintVariableType(variable="$collection",
                                                        type="java.util.Collection"))
    public static ErrorDescription collectionsToArray(HintContext ctx) {
        boolean pureMemberSelect = true;
        TreePath tp = ctx.getVariables().get("$collection");
        if (tp == null) return null;
        Tree msTest = tp.getLeaf();
        OUTER: while (true) {
            switch (msTest.getKind()) {
                case IDENTIFIER: break OUTER;
                case MEMBER_SELECT: msTest = ((MemberSelectTree) msTest).getExpression(); break;
                default:
                    pureMemberSelect = false;
                    break OUTER;
            }
        }

        Fix[] fixes;

        if (pureMemberSelect) {
            String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_Tiny_collectionsToArray");

            fixes = new Fix[] {
                JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$collection.toArray(new $clazz[$collection.size()])")
            };
        } else {
            fixes = new Fix[0];
        }

        String displayName = NbBundle.getMessage(Tiny.class, "ERR_Tiny_collectionsToArray");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, fixes);
    }
}
