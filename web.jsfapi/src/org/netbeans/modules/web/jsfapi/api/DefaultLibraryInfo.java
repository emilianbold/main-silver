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
package org.netbeans.modules.web.jsfapi.api;

import java.util.HashMap;
import java.util.Map;

public enum DefaultLibraryInfo implements LibraryInfo {

    HTML("http://xmlns.jcp.org/jsf/html", "Html Basic", "h"), //NOI18N
    JSF_CORE("http://xmlns.jcp.org/jsf/core", "Jsf Core", "f"), //NOI18N
    JSTL_CORE("http://xmlns.jcp.org/jsp/jstl/core", "Jstl Core", "c"), //NOI18N
    JSTL_CORE_FUNCTIONS("http://xmlns.jcp.org/jsp/jstl/functions", "Jstl Core Functions", "fn"), //NOI18N
    FACELETS("http://xmlns.jcp.org/jsf/facelets", "Facelets", "ui"), //NOI18N
    COMPOSITE("http://xmlns.jcp.org/jsf/composite", "Composite Components", "cc"), //NOI18N
    PRIMEFACES("http://primefaces.org/ui", "PrimeFaces", "p"), //NOI18N
//    FLOW("http://java.sun.com/jsf/flow", "Faces Flow", "j"), //NOI18N
    JSF("http://xmlns.jcp.org/jsf", "Jsf", "jsf"), //NOI18N
    PASSTHROUGH("http://xmlns.jcp.org/jsf/passthrough", "Passthrough", "p"); //NOI18N

    public static final String NS_LOCATION_JCP_ORG = "http://xmlns.jcp.org"; //NOI18N
    public static final String NS_LOCATION_SUN_COM = "http://java.sun.com";  //NOI18N
    public static final Map<String, String> NS_MAPPING = new HashMap<String, String>(8);
    static {
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/html", "http://java.sun.com/jsf/html"); //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/core", "http://java.sun.com/jsf/core"); //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsp/jstl/core", "http://java.sun.com/jsp/jstl/core"); //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsp/jstl/functions", "http://java.sun.com/jsp/jstl/functions"); //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/facelets", "http://java.sun.com/jsf/facelets"); //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/composite", "http://java.sun.com/jsf/composite"); //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf", "http://java.sun.com/jsf"); //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/passthrough", "http://java.sun.com/jsf/passthrough"); //NOI18N
    }

    private String namespace;
    private String displayName;
    private String defaultPrefix;


    private DefaultLibraryInfo(String namespace, String displayName, String defaultPrefix) {
        this.namespace = namespace;
        this.displayName = displayName;
        this.defaultPrefix = defaultPrefix;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    /**
     * Second supported namespace by the library.
     * @return legacy namespace if any or {@code null}
     */
    @Override
    public String getLegacyNamespace() {
        return NS_MAPPING.get(namespace);
    }

    @Override
    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public static LibraryInfo forNamespace(String namespace) {
        for (int i = 0; i < values().length; i++) {
            LibraryInfo li = values()[i];
            if (li.getNamespace().equals(namespace)
                    || (li.getLegacyNamespace() != null && li.getLegacyNamespace().equals(namespace))) {
                return li;
            }
        }
        return null;
    }


}
