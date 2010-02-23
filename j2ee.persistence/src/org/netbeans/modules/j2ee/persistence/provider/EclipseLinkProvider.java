/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.persistence.provider;

import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.openide.util.NbBundle;

/**
 * This class represents the EclipseLink provider.
 *
 * @author Andrei Badea
 */
class EclipseLinkProvider extends Provider {

    public EclipseLinkProvider(String version){
        super("org.eclipse.persistence.jpa.PersistenceProvider", version); //NOI18N
    }

    public EclipseLinkProvider(){
        this(null); //NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EclipseLinkProvider.class, "LBL_EclipseLink") + (getVersion()!=null ? "(JPA "+getVersion()+")" : ""); //NOI18N
    }

    @Override
    public String getJdbcUrl() {
        return   Persistence.VERSION_1_0.equals(getVersion()) ? "eclipselink.jdbc.url" : super.getJdbcUrl();
    }

    @Override
    public String getJdbcDriver() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "eclipselink.jdbc.driver" : "javax.persistence.jdbc.driver";
    }

    @Override
    public String getJdbcUsername() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "eclipselink.jdbc.user" : "javax.persistence.jdbc.user";
    }

    @Override
    public String getJdbcPassword() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "eclipselink.jdbc.password" : "javax.persistence.jdbc.password";
    }

    @Override
    public String getAnnotationProcessor() {
        return Persistence.VERSION_2_0.equals(getVersion()) ? "org.eclipse.persistence.internal.jpa.modelgen.CanonicalModelProcessor" : super.getAnnotationProcessor();
    }

    @Override
    public String getTableGenerationPropertyName() {
        return "eclipselink.ddl-generation";
    }

    @Override
    public String getTableGenerationDropCreateValue() {
        return "drop-and-create-tables";
    }

    @Override
    public String getTableGenerationCreateValue() {
        return "create-tables";
    }

    @Override
    public Map getUnresolvedVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public Map getDefaultVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }
}
