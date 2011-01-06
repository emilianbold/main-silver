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
package org.netbeans.modules.websvc.rest.codegen;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBean;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.support.SpringHelper;

/**
 *
 * @author PeterLiu
 */
public class SpringEntityResourcesGenerator extends EntityResourcesGenerator {

    /** Creates a new instance of EntityRESTServicesCodeGenerator */
    public SpringEntityResourcesGenerator( boolean hasAop) {
        injectEntityManager = true;
	// Fix for BZ#193626 -  RESTful WS from DB ( or entities ) doesn't work with Spring 3
	hasAopAlliance = hasAop;
    }

    @Override
    protected void configurePersistence() {
        new SpringHelper(project, persistenceUnit).configure();
    }

    @Override
    protected List<String> getAdditionalContainerResourceImports(EntityResourceBean bean) {
        List<String> imports = new ArrayList<String>();
        
        imports.add(RestConstants.SINGLETON);
        imports.add(SpringConstants.AUTOWIRE);
        imports.add(SpringConstants.TRANSACTIONAL);
        imports.add(RestConstants.RESOURCE_CONTEXT);

        return imports;
    }

    @Override
    protected List<String> getAdditionalItemResourceImports(EntityResourceBean bean) {
        List<String> imports = new ArrayList<String>();

        imports.add(SpringConstants.AUTOWIRE);
        imports.add(SpringConstants.TRANSACTIONAL);
        imports.add(RestConstants.RESOURCE_CONTEXT);

        return imports;
    }

    @Override
    protected String[] getAdditionalContainerResourceAnnotations() {
        if ( hasAopAlliance ){
        return new String[] {
                RestConstants.SINGLETON_ANNOTATION,
                SpringConstants.AUTOWIRE_ANNOTATION };
	}
	else {
        return new String[] {
                RestConstants.SINGLETON_ANNOTATION,
                SpringConstants.AUTOWIRE_ANNOTATION,
                "Error"                 // NOI18N
        };
	}
    }
    
    @Override
    protected Object[] getAdditionalContainerResourceAnnotationAttrs() {
        if ( hasAopAlliance ){
            return new Object[] {null, null };
	}
	else {
            return new Object[] {null, null, 
		"Please fix your project manually, for instructions see "
		    +"http://wiki.netbeans.org/SpringWithAopalliance"}; //NOI18N
	}
    }
    
    @Override
    protected String[] getAdditionalItemResourceAnnotations() {
        if ( hasAopAlliance ){
            return new String[] {SpringConstants.AUTOWIRE_ANNOTATION };
	}
	else {
            return new String[] {SpringConstants.AUTOWIRE_ANNOTATION,
	         "Error"					// NOI18N
	};
	}
    }
    
    @Override
    protected Object[] getAdditionalItemResourceAnnotationAttrs() {
        if ( hasAopAlliance ){
	    return new Object[] {null};
	}
	else {
	    return new Object[] {null,
		"Please fix your project manually, for instructions see "
		+"http://wiki.netbeans.org/SpringWithAopalliance"}; //NOI18N
	}
    }
    
    private String[] getTransactionalAnnotation() {
        return new String[]{SpringConstants.TRANSACTIONAL_ANNOTATION};
    }

    private Object[] getTransactionalAnnotationAttr() {
        return new Object[]{null};
    }

    @Override
    protected String[] getAdditionalContainerGetMethodAnnotations() {
        return getTransactionalAnnotation();
    }

    @Override
    protected Object[] getAdditionalContainerGetMethodAnnotationAttrs() {
        return getTransactionalAnnotationAttr();
    }

    @Override
    protected String[] getAdditionalContainerPostMethodAnnotations() {
        return getTransactionalAnnotation();
    }

    @Override
    protected Object[] getAdditionalContainerPostMethodAnnotationAttrs() {
        return getTransactionalAnnotationAttr();
    }

    @Override
    protected String[] getAdditionalItemGetMethodAnnotations() {
        return getTransactionalAnnotation();
    }

    @Override
    protected Object[] getAdditionalItemGetMethodAnnotationAttrs() {
        return getTransactionalAnnotationAttr();
    }

    @Override
    protected String[] getAdditionalItemPutMethodAnnotations() {
        return getTransactionalAnnotation();
    }

    @Override
    protected Object[] getAdditionalItemPutMethodAnnotationAttrs() {
        return getTransactionalAnnotationAttr();
    }

    @Override
    protected String[] getAdditionalItemDeleteMethodAnnotations() {
        return getTransactionalAnnotation();
    }

    @Override
    protected Object[] getAdditionalItemDeleteMethodAnnotationAttrs() {
        return getTransactionalAnnotationAttr();
    }
    
    @Override
    protected String[] getAdditionalItemGetResourceMethodAnnotations() {
        return getTransactionalAnnotation();
    }
    
    @Override
    protected Object[] getAdditionalItemGetResourceMethodAnnotationAttrs() {
        return getTransactionalAnnotationAttr();
    }

    private boolean hasAopAlliance;
}
