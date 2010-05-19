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

package org.netbeans.modules.dlight.api.datafilter.support;

import java.util.Arrays;
import java.util.Collection;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.spi.datafilter.DataFilterFactory;
import org.netbeans.modules.dlight.util.Range;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = org.netbeans.modules.dlight.spi.datafilter.DataFilterFactory.class)
public final class TimeIntervalDataFilterFactory implements DataFilterFactory{
    public static final String TIME_INTERVAL_FILTER = "datafilter.timeinterval";//NOI18N

    public DataFilter createFilter(String filterID, String filterSpec) {
        if (!TIME_INTERVAL_FILTER.equals(filterID)){
            return null;
        }
        int index = filterSpec.indexOf(Range.STRING_DELIMITER);
        if ( index== -1){
            return null;
        }
        //get before and after
        Long startTime = Long.valueOf(filterSpec.substring(0, index - 1));
        Long endtime = Long.valueOf(filterSpec.substring(index + Range.STRING_DELIMITER.length(), filterSpec.length()));

        return create(new Range<Long>(startTime, endtime));
    }

    public static TimeIntervalDataFilter create(Range<Long> timeInterval ){
        return new TimeIntervalDataFilter(timeInterval);
    }

    public Collection<String> getSupportedFilterIDs() {
        return Arrays.asList(TIME_INTERVAL_FILTER);
    }

}
