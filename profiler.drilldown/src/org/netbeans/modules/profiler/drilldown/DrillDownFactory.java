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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.profiler.drilldown;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.results.cpu.cct.CCTResultsFilter;
import org.netbeans.lib.profiler.results.cpu.cct.CCTResultsFilter.Evaluator;
import org.netbeans.modules.profiler.categorization.api.ProjectCategorization;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Bachorik
 */
@org.openide.util.lookup.ServiceProviders({@org.openide.util.lookup.ServiceProvider(service=org.netbeans.lib.profiler.results.cpu.cct.CCTResultsFilter.EvaluatorProvider.class), @org.openide.util.lookup.ServiceProvider(service=DrillDownFactory.class)})
public class DrillDownFactory implements CCTResultsFilter.EvaluatorProvider {
    private static class WeakEvaluator extends WeakReference<CCTResultsFilter.Evaluator> implements Evaluator {
        public WeakEvaluator(Evaluator referent, ReferenceQueue<? super Evaluator> q) {
            super(referent, q);
        }

        public WeakEvaluator(Evaluator referent) {
            super(referent);
        }

        public boolean evaluate(Mark mark) {
            Evaluator eval = get();
            return eval != null ? eval.evaluate(mark) : true;
        }
    }
    final private Set<WeakEvaluator> drillDownEvaluators = new CopyOnWriteArraySet<WeakEvaluator>();
    
    public DrillDown createDrillDown(Lookup.Provider project, ProfilerClient client) {
        /*
         * #143850 - drilldown fails when profiling a global attach
         */
        if (project == null) {
            return null;
        }
        
        if (ProjectCategorization.isAvailable(project)) {
            DrillDown dd = new DrillDown(new ProjectCategorization(project), client);
            drillDownEvaluators.add(new WeakEvaluator(dd));
            return dd;
        } else {
            return null;
        }
    }

    /**
     * A set of provided evaluators
     * @return Returns a synchronized set - thus each access to the set must be guarded by the instance
     */
    public Set getEvaluators() {
        
        return drillDownEvaluators;
    }
}
