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

package org.netbeans.modules.websvc.wsstack.jaxws.weblogic;

import java.io.File;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Feature;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Tool;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.modules.websvc.wsstack.jaxws.JaxWs;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation;

/**
 *
 * @author mkuchtiak
 */
public class WebLogicJaxWsStack implements WSStackImplementation<JaxWs> {
    
    private File serverHome;
    private String version;
    private JaxWs jaxWs;
    
    public WebLogicJaxWsStack(File serverHome) {
        this.serverHome = serverHome;
        version = "2.1.4";
        jaxWs = new JaxWs(getUriDescriptor());
    }

    public JaxWs get() {
        return jaxWs;
    }

    public WSStackVersion getVersion() {
        return WSStackFactory.createWSStackVersion(version);
    }

    public WSTool getWSTool(Tool toolId) {
            return null;
    }

    public boolean isFeatureSupported(Feature feature) {
        if (feature == JaxWs.Feature.TESTER_PAGE) {
            return true;
        } else if (feature == JaxWs.Feature.JSR109) {
            return true;
        } else {
            return false;
        }
    }
    
    private JaxWs.UriDescriptor getUriDescriptor() {
        return new JaxWs.UriDescriptor() {

            public String getServiceUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                if (isEjb) {
                    return portName+"/"+serviceName;
                } else {
                    return (applicationRoot.length() >0 ? applicationRoot+"/":"")+serviceName;
                }
            }

            public String getDescriptorUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return getServiceUri(applicationRoot, serviceName, portName, isEjb)+"?wsdl"; //NOI18N
            }
            
            public String getTesterPageUri(String host, String port, String applicationRoot, String serviceName, String portName, boolean isEjb) {
                String prefix = "http://"+host+":"+port+"/wls_utc/begin.do?wsdlUrl="; //NOI18N
                return prefix+"http://"+host+":"+port+"/"+getServiceUri(applicationRoot, serviceName, portName, isEjb)+"?wsdl";
            }
            
        };
    }
    
}
