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
package org.netbeans.modules.j2ee.persistence.jpqleditor;

import java.lang.reflect.Method;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;

/**
 * Executes JPQL query.
 */
public class JPQLExecutor {

    public final String ECLIPSELINK_QUERY = "org.eclipse.persistence.jpa.JpaQuery";
       public final String ECLIPSELINK_QUERY_SQL0 = "getDatabaseQuery";
    public final String ECLIPSELINK_QUERY_SQL1 = "getSQLString";
    public final String HIBERNATE_QUERY = "org.hibernate.Query";
    public final String HIBERNATE_QUERY_SQL = "getQueryString";
    public final String OPENJPA_QUERY = "org.apache.openjpa.persistence.QueryImpl";
    public final String OPENJPA_QUERY_SQL = "getQueryString";

    /**
     * Executes given JPQL query and returns the result.
     *
     * @param jpql the query
     * @return JPQLResult containing the execution result (including any
     * errors).
     */
    public JPQLResult execute(String jpql,
            PersistenceUnit pu,
            PersistenceEnvironment pe,
            int maxRowCount,
            ProgressHandle ph) {
        JPQLResult result = new JPQLResult();
        try {
            ph.progress(60);

            Class pClass = Thread.currentThread().getContextClassLoader().loadClass("javax.persistence.Persistence");
            javax.persistence.Persistence p = (javax.persistence.Persistence) pClass.newInstance();
            //p.getClass().getClassLoader().loadClass("org.eclipse.persistence.jpa.PersistenceProvider");
            EntityManagerFactory emf = p.createEntityManagerFactory(pu.getName());

            EntityManager em = emf.createEntityManager();

            Query query = em.createQuery(jpql);
            //
            Provider provider = ProviderUtil.getProvider(pu);
            String queryStr = null;
            if(provider.equals(ProviderUtil.ECLIPSELINK_PROVIDER)){//NOI18N
                Class qClass = Thread.currentThread().getContextClassLoader().loadClass(ECLIPSELINK_QUERY);
                if(qClass !=null) {
                    Method method = qClass.getMethod(ECLIPSELINK_QUERY_SQL0);
                    if(method != null){
                        Object dqOject = method.invoke(query);
                        Method method2 = (dqOject!= null ? dqOject.getClass().getMethod(ECLIPSELINK_QUERY_SQL1) : null);
                        if(method2!=null) {
                            queryStr = (String) method2.invoke(dqOject);
                        }
                    }
                }
            } 
//            else if (provider.equals(ProviderUtil.HIBERNATE_PROVIDER2_0)){//NOI18N
//                Class qClass = Thread.currentThread().getContextClassLoader().loadClass(HIBERNATE_QUERY);
//                if(qClass !=null) {
//                    Method method = qClass.getMethod(HIBERNATE_QUERY_SQL);
//                    if(method != null){
//                        queryStr = (String) method.invoke(query);
//                    }
//                }
//            } else if (provider.getProviderClass().contains("openjpa")){//NOI18N
//                Class qClass = Thread.currentThread().getContextClassLoader().loadClass(OPENJPA_QUERY);
//                if(qClass !=null) {
//                    Method method = qClass.getMethod(OPENJPA_QUERY_SQL);
//                    if(method != null){
//                        queryStr = (String) method.invoke(query);
//                    }
//                }
//            } 
            result.setSqlQuery(queryStr);
            //
            ph.progress(70);

            query.setMaxResults(maxRowCount);

            jpql = jpql.trim();
            jpql = jpql.toUpperCase();

            if (jpql.startsWith("UPDATE") || jpql.startsWith("DELETE")) { //NOI18N
                result.setUpdateOrDeleteResult(query.executeUpdate());
            } else {
                result.setQueryResults(query.getResultList());
            }
        } catch (Exception e) {
            result.getExceptions().add(e);
        }
        return result;
    }
}
