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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.indicators.graph;

import java.util.List;
import org.netbeans.modules.dlight.indicators.Aggregation;
import org.netbeans.modules.dlight.indicators.DataRowToTimeSeries;
import org.netbeans.modules.dlight.indicators.DetailDescriptor;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import org.netbeans.modules.dlight.indicators.TimeSeriesIndicatorConfiguration;
import org.netbeans.modules.dlight.util.ValueFormatter;

/**
 * @author Alexey Vladykin
 */
public abstract class TimeSeriesIndicatorConfigurationAccessor {

    private static volatile TimeSeriesIndicatorConfigurationAccessor instance;

    public static void setDefault(TimeSeriesIndicatorConfigurationAccessor accessor) {
        if (instance != null) {
            throw new IllegalStateException();
        }
        instance = accessor;
    }

    public static TimeSeriesIndicatorConfigurationAccessor getDefault() {
        TimeSeriesIndicatorConfigurationAccessor a = instance;
        if (a != null) {
            return a;
        }

        try {
            Class.forName(TimeSeriesIndicatorConfiguration.class.getName(), true,
                    TimeSeriesIndicatorConfiguration.class.getClassLoader());
        } catch (Exception e) {
        }
        return instance;
    }

    public TimeSeriesIndicatorConfigurationAccessor() {
    }

    public abstract String getTitle(TimeSeriesIndicatorConfiguration conf);

    public abstract int getGraphScale(TimeSeriesIndicatorConfiguration conf);

    public abstract boolean isLegendVisible(TimeSeriesIndicatorConfiguration conf);

    public abstract List<TimeSeriesDescriptor> getTimeSeriesDescriptors(TimeSeriesIndicatorConfiguration conf);

    public abstract List<DetailDescriptor> getDetailDescriptors(TimeSeriesIndicatorConfiguration conf);

    public abstract DataRowToTimeSeries getDataRowHandler(TimeSeriesIndicatorConfiguration conf);

    public abstract ValueFormatter getLabelFormatter(TimeSeriesIndicatorConfiguration conf);

    public abstract long getGranularity(TimeSeriesIndicatorConfiguration conf);

    public abstract Aggregation getAggregation(TimeSeriesIndicatorConfiguration conf);

    public abstract boolean getLastNonNull(TimeSeriesIndicatorConfiguration conf);

    public abstract String getPersistencePrefix(TimeSeriesIndicatorConfiguration conf);

    public abstract long getExtent(TimeSeriesIndicatorConfiguration conf);
    
    public abstract int getLegendWidth(TimeSeriesIndicatorConfiguration conf);
}
