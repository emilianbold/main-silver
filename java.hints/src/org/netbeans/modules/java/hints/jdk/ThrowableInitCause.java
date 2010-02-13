/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.java.hints.jdk;

import com.sun.source.util.TreePath;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.modules.java.hints.jackpot.code.spi.Constraint;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPatterns;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.MatcherUtilities;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.java.hints.jackpot.spi.support.OneCheckboxCustomizerProvider;
import org.netbeans.modules.java.hints.jdk.ThrowableInitCause.CustomizerProviderImpl;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(category="general", customizerProvider=CustomizerProviderImpl.class)
public class ThrowableInitCause {

    public static final String STRICT_KEY = "strict";
    public static final boolean STRICT_DEFAULT = false;

    @TriggerPatterns({
        @TriggerPattern(value="throw ($exc) new $exc($str).initCause($del);",
                        constraints={@Constraint(variable="$str", type="java.lang.String"),
                                     @Constraint(variable="$del", type="java.lang.Throwable")}),
        @TriggerPattern(value="$exc $excVar = new $exc($str); $excVar.initCause($del); throw $excVar;",
                        constraints={@Constraint(variable="$str", type="java.lang.String"),
                                     @Constraint(variable="$del", type="java.lang.Throwable")}),
        @TriggerPattern(value="throw ($exc) new $exc().initCause($del);",
                        constraints={@Constraint(variable="$del", type="java.lang.Throwable")}),
        @TriggerPattern(value="$exc $excVar = new $exc(); $excVar.initCause($del); throw $excVar;",
                        constraints={@Constraint(variable="$del", type="java.lang.Throwable")})
    })
    public static ErrorDescription initCause(HintContext ctx) {
        TypeElement throwable = ctx.getInfo().getElements().getTypeElement("java.lang.Throwable");

        if (throwable == null) return null;

        TreePath exc = ctx.getVariables().get("$exc");
        TypeMirror excType = ctx.getInfo().getTrees().getTypeMirror(exc);
        Types t = ctx.getInfo().getTypes();

        if (!t.isSubtype(t.erasure(excType), t.erasure(throwable.asType()))) {
            return null;
        }

        Element el = t.asElement(excType);

        if (el == null || el.getKind() != ElementKind.CLASS) {
            //should not happen
            return null;
        }

        List<TypeMirror> constrParams = new LinkedList<TypeMirror>();
        TreePath str = ctx.getVariables().get("$str");
        String target;

        if (   (str != null && (   MatcherUtilities.matches(ctx, str, "$del.toString()")
                                || (    MatcherUtilities.matches(ctx, str, "$del.getMessage()")
                                    && !ctx.getPreferences().getBoolean(STRICT_KEY, STRICT_DEFAULT))
                                || (    MatcherUtilities.matches(ctx, str, "$del.getLocalizedMessage()")
                                    && !ctx.getPreferences().getBoolean(STRICT_KEY, STRICT_DEFAULT)))
            || (str == null && !ctx.getPreferences().getBoolean(STRICT_KEY, STRICT_DEFAULT)))) {
            target = "throw new $exc($del);";
        } else {
            TypeElement jlString = ctx.getInfo().getElements().getTypeElement("java.lang.String");

            if (jlString == null) return null;

            constrParams.add(jlString.asType());

            if (str != null) {
                target = "throw new $exc($str, $del);";
            } else {
                target = "throw new $exc(null, $del);"; //TODO: might lead to incompilable code (for overloaded constructors)
            }
        }

        TreePath del = ctx.getVariables().get("$del");
        TypeMirror delType = ctx.getInfo().getTrees().getTypeMirror(del);

        constrParams.add(delType);

        if (!findConstructor(el, t, constrParams)) return null;

        String fixDisplayName = NbBundle.getMessage(ThrowableInitCause.class, "FIX_ThrowableInitCause");
        String displayName = NbBundle.getMessage(ThrowableInitCause.class, "ERR_ThrowableInitCause");
        TreePath toUnderline = ctx.getVariables().get("$excVar");

        if (toUnderline == null) {
            toUnderline = ctx.getPath();
        }

        return ErrorDescriptionFactory.forTree(ctx, toUnderline, displayName, JavaFix.rewriteFix(ctx, fixDisplayName, ctx.getPath(), target));
    }

    private static boolean findConstructor(Element el, Types t, List<TypeMirror> paramTypes) {
        boolean found = false;
        OUTER: for (ExecutableElement ee : ElementFilter.constructorsIn(el.getEnclosedElements())) {
            if (ee.isVarArgs() || ee.getParameters().size() != paramTypes.size()) {
                continue;
            }

            Iterator<? extends VariableElement> p = ee.getParameters().iterator();
            Iterator<TypeMirror> expectedType = paramTypes.iterator();

            while (p.hasNext() && expectedType.hasNext()) {
                if (!t.isAssignable(expectedType.next(), p.next().asType())) {
                    continue OUTER;
                }
            }

            found = true;
            break;
        }

        return found;
    }

    public static final class CustomizerProviderImpl extends OneCheckboxCustomizerProvider {
        public CustomizerProviderImpl() {
            super(NbBundle.getMessage(ThrowableInitCause.class, "DN_ThrowableInitCauseStrict"),
                  NbBundle.getMessage(ThrowableInitCause.class, "TP_ThrowableInitCauseStrict"),
                  STRICT_KEY,
                  STRICT_DEFAULT);
        }
    }
}
