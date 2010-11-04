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
package org.netbeans.modules.dlight.db.derby;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.modules.dlight.spi.storage.DataStorageFactory;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.PersistentDataStorageFactory;
import org.netbeans.modules.dlight.spi.storage.PersistentDataStorageFactory.Mode;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.spi.support.SQLDataStorageFactory;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author masha
 */
@ServiceProviders({
    @ServiceProvider(service = DataStorageFactory.class,  position = 100),
    @ServiceProvider(service = PersistentDataStorageFactory.class,  position = 100)
})
public final class DerbyDataStorageFactory extends SQLDataStorageFactory<DerbyDataStorage> {

    static final String DERBY_DATA_STORAGE_TYPE = "db:sql:derby"; // NOI18N
    private final Collection<DataStorageType> supportedStorageTypes = new ArrayList<DataStorageType>();

    public DerbyDataStorageFactory() {
        supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType(DERBY_DATA_STORAGE_TYPE));
        supportedStorageTypes.addAll(super.getStorageTypes());
    }

    @Override
    public Collection<DataStorageType> getStorageTypes() {
        return supportedStorageTypes;
    }

    @Override
    public DerbyDataStorage createStorage() {
        try {
            DerbyDataStorage result =  new DerbyDataStorage();
            result.connect();
            return result;
        } catch (SQLException ex) {
            DLightLogger.getLogger(DerbyDataStorageFactory.class).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public DerbyDataStorage openStorage(String uniqueKey) {
        try {
            //the unique key us the url to open storage
            DerbyDataStorage result =  new DerbyDataStorage(uniqueKey);
            result.connect();
            return result;
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public DerbyDataStorage createStorage(String uniqueKey) {
        try {
            DerbyDataStorage result =  new DerbyDataStorage(uniqueKey);
            result.connect();
            return result;
        } catch (SQLException ex) {
            DLightLogger.getLogger(DerbyDataStorageFactory.class).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    

    @Override
    public String getUniqueKey(DerbyDataStorage storage) {
        return storage.getURL();
    }

    @Override
    public DerbyDataStorage openStorage(String uniqueKey, Mode mode) {
        try {
            //the unique key us the url to open storage
            DerbyDataStorage result =  new DerbyDataStorage(uniqueKey);
            result.connect();
            return result;
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
