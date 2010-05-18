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
package org.netbeans.modules.dlight.threadmap.support.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.threadmap.spi.dataprovider.ThreadMapDataProvider;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.msa.support.MSASQLTables;
import org.netbeans.modules.dlight.spi.dataprovider.DataProviderFactory;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.threadmap.dataprovider.ThreadMapDataProviderImpl;
import org.netbeans.modules.dlight.threadmap.spi.dataprovider.ThreadMapDataScheme;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Alexander Simon
 */
@ServiceProviders({@ServiceProvider(service = DataProviderFactory.class)})
public final class ThreadMapDataProviderFactory implements DataProviderFactory {

    private final DataStorageType supportedStorageType;
    private final List<DataModelScheme> providedSchemas;
    private final List<DataTableMetadata> requiredTables;

    public ThreadMapDataProviderFactory() {
        // This factory creates dataproviders that provide information
        // about ThreadMap (ThreadMapDataScheme) ...
        providedSchemas = Collections.singletonList(ThreadMapDataScheme.getScheme());
        // ... as a source of information use SQL database ...
        supportedStorageType = SQLDataStorage.getStorageType();
        // ... with tables ...
        requiredTables = Collections.singletonList(MSASQLTables.msa.tableMetadata);
    }

    public ThreadMapDataProvider create() {
        return new ThreadMapDataProviderImpl();
    }

    public Collection<DataModelScheme> getProvidedDataModelScheme() {
        return providedSchemas;
    }

    public boolean provides(DataModelScheme dataModel) {
        return providedSchemas.contains(dataModel);
    }

    public boolean validate(DataStorage storage) {
        if (!storage.supportsType(supportedStorageType)) {
            return false;
        }

        for (DataTableMetadata requiredTable : requiredTables) {
            if (!storage.hasData(requiredTable)) {
                return false;
            }
        }

        return true;
    }
}
