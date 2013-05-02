/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
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
package org.netbeans.modules.websvc.rest.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;


/**
 * @author ads
 *
 */
class RestServiceChildFactory extends ChildFactory {
    
    private static final Logger LOG = Logger.getLogger( 
            RestServiceChildFactory.class.getName() );
    
    RestServiceChildFactory( Project project){
        this.project = project;
    }

    /* (non-Javadoc)
     * @see org.openide.nodes.ChildFactory#createKeys(java.util.List)
     */
    @Override
    protected boolean createKeys( final List keys ) {
            
        try {
            RestServicesModel model = getModel();
            assert model != null : "null model"; // NOI18N
            if (model != null) {
                model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>()
                {

                    @Override
                    public Void run( RestServicesMetadata metadata )
                            throws IOException
                    {
                        RestServiceDescription[] restServiceDescription = 
                            metadata.getRoot().getRestServiceDescription();
                        Arrays.sort(restServiceDescription, COMPARATOR);
                        for (RestServiceDescription r : restServiceDescription) {
                            // ignore REST services for which we do not have sources (#216168, #229168):
                            if (r.getFile() != null) {
                                keys.add(r);
                            }
                        }
                        return null;
                    }
                });
            }
        }
        catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
                
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.openide.nodes.ChildFactory#createNodeForKey(java.lang.Object)
     */
    @Override
    protected Node createNodeForKey( Object key ) {
        assert key instanceof RestServiceDescription;
        return new RestServiceNode(project, getModel(), (RestServiceDescription)key);
    }

    private RestServicesModel getModel() {
        RestSupport support = project.getLookup().lookup(RestSupport.class);
        if (support != null) {
            return support.getRestServicesModel();
        }
        return null;
    }
    
    private static class RSDescriptionComparator implements Comparator<RestServiceDescription>{

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare( RestServiceDescription desc1, RestServiceDescription desc2 )
        {
            String key1 = RestServiceNode.getKey(desc1);
            String key2 = RestServiceNode.getKey(desc2);
            return key1.compareTo(key2);
        }
        
    }
    
    private static final RSDescriptionComparator COMPARATOR = new RSDescriptionComparator();
    private Project project;
    
}
