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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;

/**
 *
 */
public class DerbyDataStorage extends SQLDataStorage {

    private static final Logger logger = DLightLogger.getLogger(DerbyDataStorage.class);
    private static final String SQL_QUERY_DELIMETER = ""; // NOI18N
    private static final File tmpDir;
    private static final AtomicInteger dbIndex = new AtomicInteger();
//    private static boolean driverLoaded = false;
    private final List<DataStorageType> supportedStorageTypes = new ArrayList<DataStorageType>();
    private String dbURL;
    private final List<DataTableMetadata> tableMetadatas;

    static {
        File tempDir = null;

        try {
            HostInfo hi = HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.getLocal());
            tempDir = hi.getTempDirFile();
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }

        if (tempDir == null) {
            tempDir = new File(System.getProperty("java.io.tmpdir")); // NOI18N
        }

        tmpDir = new File(tempDir, "/derby_dlight"); // NOI18N

        try {
            System.setProperty("derby.system.home", tmpDir.getAbsolutePath()); // NOI18N
            Class driver = Class.forName("org.apache.derby.jdbc.EmbeddedDriver"); // NOI18N
            logger.log(Level.INFO, "Driver for Derby(JavaDB) ({0}) Loaded ", driver.getName()); // NOI18N
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        if (tmpDir.exists()) {
            final File[] files = tmpDir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return dir.isDirectory() && name.startsWith("DerbyDlight"); // NOI18N
                }
            });
            int generalNameLength = "DerbyDlight".length();//NOI18N
            int newValue = 0;
            for (int i = 0; i < files.length; i++) {
                String suffix = files[i].getName().substring(generalNameLength);
                try {
                    newValue = Math.max(newValue, Integer.valueOf(suffix));
                } catch (NumberFormatException e) {
                }
            }

            dbIndex.getAndSet(newValue);
            
            DLightExecutorService.submit(new Runnable() {

                @Override
                public void run() {
                    for (File file : files) {
                        Util.deleteLocalDirectory(file);
                    }
                }
            }, "DerbyDataStorage removing old data bases");//NOI18N
        }
    }

    /**
     * Empty constructor, used by Lookup
     */
    public DerbyDataStorage() throws SQLException {
        this("jdbc:derby:DerbyDlight" + dbIndex.incrementAndGet() + ";create=true;user=dbuser;password=dbuserpswd"); // NOI18N
    }

    private void initStorageTypes() {
        supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType(DerbyDataStorageFactory.DERBY_DATA_STORAGE_TYPE));
        //supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID));
        supportedStorageTypes.addAll(super.getStorageTypes());
    }

    DerbyDataStorage(String url) throws SQLException {
        super(url);
        dbURL = url;
        this.tableMetadatas = new ArrayList<DataTableMetadata>();
        initStorageTypes();
        connection.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }

    String getURL() {
        return dbURL;
    }

    @Override
    public boolean shutdown() {
        //remove folder
        boolean result = super.shutdown();
        String dbName = dbURL.substring(dbURL.lastIndexOf(':') + 1, dbURL.indexOf(';'));
        //and now get number
        result = Util.deleteLocalDirectory(new File(tmpDir, dbName)) && result;
        return result;
    }

    @Override
    protected String classToType(Class clazz) {
        if (clazz == Integer.class) {
            return "integer"; // NOI18N
        }
        return super.classToType(clazz);
    }

    @Override
    protected void connect(String dburl) throws SQLException {
        connection = DriverManager.getConnection(dburl);
    }

    @Override
    public Collection<DataStorageType> getStorageTypes() {
        return supportedStorageTypes;
    }

    @Override
    public void createTables(List<DataTableMetadata> tableMetadatas) {
        for (DataTableMetadata tdmd : tableMetadatas) {
            this.tableMetadatas.add(tdmd);
//            if (tdmd.getName().equals(STACK_METADATA_VIEW_NAME)) {
//                if (!tables.containsKey(STACK_METADATA_VIEW_NAME)) {
//                    tables.put(STACK_METADATA_VIEW_NAME, tdmd);
//                }
//                continue;
//            }
            createTable(tdmd);
        }
    }

//    public int putStack(List<CharSequence> stack, long sampleDuration) {
//        return stackStorage.putStack(stack, sampleDuration);
//    }
//
//    public List<FunctionCall> getCallStack(int stackId) {
//        return stackStorage.getStack(stackId);
//    }
//    public void flush() {
//        try {
//            stackStorage.flush();
//        } catch (InterruptedException ex) {
//            logger.log(Level.SEVERE, null, ex);
//        }
//    }
//    public List<Long> getPeriodicStacks(long startTime, long endTime, long interval) {
//        try {
//            return stackStorage.getPeriodicStacks(startTime, endTime, interval);
//        } catch (SQLException ex) {
//            logger.log(Level.SEVERE, null, ex);
//            return null;
//        }
//    }
//
//    public List<FunctionMetric> getMetricsList() {
//        return stackStorage.getMetricsList();
//    }
//
//    public List<FunctionCallWithMetric> getCallers(FunctionCallWithMetric[] path, boolean aggregate) {
//        try {
//            return stackStorage.getCallers(path, aggregate);
//        } catch (SQLException ex) {
//            logger.log(Level.SEVERE, null, ex);
//            return new ArrayList<FunctionCallWithMetric>();
//        }
//    }
//
//    public List<FunctionCallWithMetric> getCallees(FunctionCallWithMetric[] path, boolean aggregate) {
//        try {
//            return stackStorage.getCallees(path, aggregate);
//        } catch (SQLException ex) {
//            logger.log(Level.SEVERE, null, ex);
//            return new ArrayList<FunctionCallWithMetric>();
//        }
//    }
//
//    public List<FunctionCallWithMetric> getHotSpotFunctions(FunctionMetric metric, int limit) {
//        return stackStorage.getHotSpotFunctions(metric, limit);
//    }
    @Override
    protected String getSQLQueriesDelimeter() {
        return SQL_QUERY_DELIMETER;
    }

//    public List<FunctionCallWithMetric> getFunctionsList(DataTableMetadata metadata, List<Column> metricsColumn, FunctionDatatableDescription functionDescription) {
//        return stackStorage.getFunctionsList(metadata, metricsColumn, functionDescription);
//    }
//
//    public ThreadDump getThreadDump(long timestamp, long threadID, int threadState) {
//        return stackStorage.getThreadDump(timestamp, threadID, threadState);
//    }
    @Override
    public boolean hasData(DataTableMetadata data) {
        return data.isProvidedBy(tableMetadatas);
    }

    @Override
    public boolean supportsType(DataStorageType storageType) {
        return getStorageTypes().contains(storageType);
    }

    @Override
    public void loadSchema() {
    }
}
