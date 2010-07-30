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

package org.netbeans.modules.javawebstart;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.javawebstart.ui.customizer.JWSProjectProperties;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Zezula
 */
@LookupProvider.Registration(projectType="org-netbeans-modules-java-j2seproject")
public class JWSLookupProvider implements LookupProvider {

    @Override
    public Lookup createAdditionalLookup(Lookup baseContext) {
        if (hasJWS(baseContext)) {
            final Project prj = baseContext.lookup(Project.class);
            final J2SEPropertyEvaluator j2sepe = baseContext.lookup(J2SEPropertyEvaluator.class);
            return Lookups.singleton (new JWSProjectOpenHook(prj, j2sepe.evaluator()));
        }
        return Lookup.EMPTY;        
    }

    private boolean hasJWS(final Lookup lkp) {
        final J2SEPropertyEvaluator j2sePropEval = lkp.lookup(J2SEPropertyEvaluator.class);
        return JWSProjectProperties.isTrue(j2sePropEval.evaluator().getProperty(JWSProjectProperties.JNLP_ENABLED));
    }

    
    private static class JWSProjectOpenHook extends ProjectOpenedHook {
        
        private final Project project;
        private final PropertyEvaluator eval;

        private JWSProjectOpenHook(final Project project, final PropertyEvaluator eval) {
            Parameters.notNull("project", project);     //NOI18N
            Parameters.notNull("eval", eval);           //NOI18N
            this.project = project;
            this.eval = eval;
        }

        @Override
        protected void projectOpened() {
            try {
                JWSProjectProperties.updateWebStartJars(project, eval);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        @Override
        protected void projectClosed() {
        }
    }
}
