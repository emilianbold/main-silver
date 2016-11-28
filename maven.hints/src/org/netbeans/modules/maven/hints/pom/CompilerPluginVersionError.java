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
package org.netbeans.modules.maven.hints.pom;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.Document;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.hints.pom.spi.Configuration;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginManagement;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.text.Line;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class CompilerPluginVersionError implements POMErrorFixProvider {
    private final Configuration configuration;

    private static final String MODULE_INFO = "module-info.java"; // NOI18N
    
    @NbBundle.Messages({
        "TIT_WrongCompilerVersion=Wrong maven-compiler-plugin version.",
        "DESC_ModulesNotSupported=Modules are not supported with maven-compiler-plugin < 3.6."})
    public CompilerPluginVersionError() {
        configuration = new Configuration("CompilerPluginVersionError", //NOI18N
                Bundle.TIT_WrongCompilerVersion(),
                Bundle.DESC_ModulesNotSupported(),
                true, 
                Configuration.HintSeverity.WARNING);
    }

    @Override
    public List<ErrorDescription> getErrorsForDocument(POMModel model, Project prj) {
        assert model != null;
        List<ErrorDescription> toRet = new ArrayList<ErrorDescription>();
        
        if(prj == null) {
            return toRet;
        }
        
        NbMavenProject nbprj = prj.getLookup().lookup(NbMavenProject.class);
        if(nbprj == null) {
            return toRet;
        }
        
        if(!hasModuleInfo(nbprj)) {
            return toRet;
        }
        
        String version = PluginPropertyUtils.getPluginVersion(nbprj.getMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);        
        if(version == null) {
            return toRet;            
        }
                
        if(new ComparableVersion(version).compareTo(new ComparableVersion("3.6-SNAPSHOT")) >= 0) { // NOI18N
            return toRet;
        }
                
        int pos = -1;
        org.netbeans.modules.maven.model.pom.Project p = model.getProject();
        Build bld = p.getBuild();
        if (bld != null) {
            Plugin plg = bld.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
            if (plg != null) {
                pos = plg.findPosition();
            }
        }    
        
        if(pos == -1) {
            pos = p.findPosition();
        }
        
        if(pos == -1) {
            return toRet;
        }        
        
        Document baseDocument = model.getBaseDocument();
        Line line = NbEditorUtilities.getLine(baseDocument, pos, false);
        toRet.add(ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, Bundle.DESC_ModulesNotSupported(), baseDocument, line.getLineNumber() + 1));
        
        return toRet;
    }

    @Override
    public JComponent getCustomizer(Preferences preferences) {
        return null;
    }

    @Override
    public String getSavedValue(JComponent customCustomizer, String key) {
        return null;
    }

    @Override
    public void cancel() { }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private boolean hasModuleInfo(NbMavenProject nbprj) {
        MavenProject mavenProject = nbprj.getMavenProject();        
        return hasModuleInfoInSourceRoot(mavenProject.getCompileSourceRoots()) || 
               hasModuleInfoInSourceRoot(mavenProject.getTestCompileSourceRoots());
    }

    private boolean hasModuleInfoInSourceRoot(List<String> sourceRoots) {        
        for (String sourceRoot : sourceRoots) {
            if (new File(sourceRoot, MODULE_INFO).exists()) {
                return true;
            }
        }
        return false;
    }

}
