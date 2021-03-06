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

package org.netbeans.modules.mobility.cldcplatform;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.awt.*;
import org.netbeans.modules.mobility.cldcplatform.editors.FileObjectPropertyEditor;

public class J2MEPlatformBeanInfo extends SimpleBeanInfo {
    
    private static final String PROP_DISPLAY_NAME="displayName";                //NOI18N
    private static final String PROP_SOURCES_FODLER ="sourceFolder";            //NOI18N
    private static final String PROP_JAVADOC_FOLDER = "javadocFolder";          //NOI18N
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            final PropertyDescriptor[] descs =  new PropertyDescriptor[] {
                new PropertyDescriptor(PROP_DISPLAY_NAME, J2MEPlatform.class),
                new PropertyDescriptor(PROP_SOURCES_FODLER, J2MEPlatform.class, "getSourceFolders", null), //NOI18N
                new PropertyDescriptor(PROP_JAVADOC_FOLDER, J2MEPlatform.class),
            };
            descs[0].setDisplayName(NbBundle.getMessage(J2MEPlatformBeanInfo.class,"TXT_Name")); //NOI18N
            descs[1].setDisplayName(NbBundle.getMessage(J2MEPlatformBeanInfo.class,"TXT_SourcesFolder")); //NOI18N
            descs[1].setPropertyEditorClass(FileObjectPropertyEditor.class);
            descs[2].setDisplayName(NbBundle.getMessage(J2MEPlatformBeanInfo.class,"TXT_JavaDocFolder")); //NOI18N
            descs[2].setPropertyEditorClass(FileObjectPropertyEditor.class);
            return descs;
        } catch (IntrospectionException ie) {
            return new PropertyDescriptor[0];
        }
    }
    
    
    public Image getIcon(final int iconKind) {
        if ((iconKind == BeanInfo.ICON_COLOR_16x16) || (iconKind == BeanInfo.ICON_MONO_16x16)) {
            return ImageUtilities.loadImage("org/netbeans/modules/mobility/cldcplatform/resources/platform.gif"); // NOI18N
        } 
        return null;
    }
    
}
