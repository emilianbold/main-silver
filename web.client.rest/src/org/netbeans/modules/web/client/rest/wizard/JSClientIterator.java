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
package org.netbeans.modules.web.client.rest.wizard;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.web.clientproject.api.MissingLibResourceException;
import org.netbeans.modules.web.clientproject.api.WebClientLibraryManager;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.WizardDescriptor.ProgressInstantiatingIterator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;



/**
 * @author ads
 *
 */
public class JSClientIterator implements ProgressInstantiatingIterator<WizardDescriptor>{
    
    private static final Logger LOGGER = Logger.getLogger(JSClientIterator.class.getName());

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener( ChangeListener listener ) {
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#current()
     */
    @Override
    public Panel<WizardDescriptor> current() {
        return myPanels[myIndex];
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        return myIndex<myPanels.length-1;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#hasPrevious()
     */
    @Override
    public boolean hasPrevious() {
        return myIndex >0 ;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#name()
     */
    @Override
    public String name() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#nextPanel()
     */
    @Override
    public void nextPanel() {
        if (! hasNext()) {
            throw new NoSuchElementException();
        }
        myIndex++;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#previousPanel()
     */
    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        myIndex--;        
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#removeChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void removeChangeListener( ChangeListener listener ) {
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#initialize(org.openide.WizardDescriptor)
     */
    @Override
    public void initialize( WizardDescriptor descriptor ) {
        myWizard = descriptor;
        myRestPanel = new RestPanel( descriptor );
        Project project = Templates.getProject( descriptor );
        Sources sources = ProjectUtils.getSources(project);
        
        myPanels = new WizardDescriptor.Panel[]{
                Templates.buildSimpleTargetChooser(project, 
                sources.getSourceGroups(Sources.TYPE_GENERIC)).
                    bottomPanel(myRestPanel).create(), new HtmlPanel( descriptor )
                    };
        setSteps();
    }
    
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#instantiate()
     */
    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        Project project = Templates.getProject(myWizard);
        
        Node restNode = myRestPanel.getRestNode();
        RestServiceDescription description = restNode.getLookup().lookup(
                RestServiceDescription.class);
        Boolean addBackbone = (Boolean)myWizard.getProperty(RestPanel.ADD_BACKBONE);
        FileObject existedBackbone = (FileObject)myWizard.getProperty(
                RestPanel.EXISTED_BACKBONE);
        FileObject existedUnderscore = (FileObject)myWizard.getProperty(
                RestPanel.EXISTED_UNDERSCORE);
        FileObject existedJQuery = (FileObject)myWizard.getProperty(
                RestPanel.EXISTED_JQUERY);
        
        if ( existedBackbone == null ){
            if ( addBackbone!=null && addBackbone ){
                FileObject libs = FileUtil.createFolder(project.
                        getProjectDirectory(),WebClientLibraryManager.LIBS);
                handle.progress(NbBundle.getMessage(JSClientGenerator.class, 
                        "TXT_CreateLibs"));                                 // NOI18N
                existedBackbone = addLibrary( libs , "backbone.js");        // NOI18N
                if ( existedUnderscore == null ){
                    existedUnderscore = addLibrary(libs, "underscore.js");  // NOI18N
                }
                if ( existedJQuery == null ){
                    existedJQuery = addLibrary(libs, "jquery");  // NOI18N
                }
            }
        }
        
        FileObject targetFolder = Templates.getTargetFolder(myWizard);
        String targetName = Templates.getTargetName(myWizard);
        
        FileObject templateFO = FileUtil.getConfigFile("Templates/ClientSide/new.js");  //NOI18N
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);
        DataObject createdFile = templateDO.createFromTemplate(dataFolder, targetName);
        
        FileObject jsFile = createdFile.getPrimaryFile();
        
        handle.progress(NbBundle.getMessage(JSClientGenerator.class, 
                    "TXT_GenerateModel"));                         // NOI18N
        JSClientGenerator generator = JSClientGenerator.create( description );
        generator.generate( jsFile);
        
        File htmlFile = (File)myWizard.getProperty(
                HtmlPanel.HTML_FILE);
        if ( htmlFile != null ){
            handle.progress(NbBundle.getMessage(JSClientGenerator.class, 
                    "TXT_GenerateHtml"));                         // NOI18N
            createHtml( htmlFile , jsFile, existedBackbone , existedUnderscore, 
                    existedJQuery );
        }

        handle.finish();
        return null;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#instantiate()
     */
    @Override
    public Set instantiate() throws IOException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#uninitialize(org.openide.WizardDescriptor)
     */
    @Override
    public void uninitialize( WizardDescriptor descriptor ) {
        myPanels = null;
    }
    
    private void createHtml( File htmlFile, FileObject appFile, 
            FileObject backbone , FileObject underscore, FileObject jQuery) 
        throws IOException 
    {
        File parentFile = htmlFile.getParentFile();
        parentFile.mkdirs();
        FileObject folder = FileUtil.toFileObject(FileUtil.normalizeFile(parentFile));
        FileObject templateFO = FileUtil.getConfigFile("Templates/ClientSide/js.html");  //NOI18N
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(folder);
        String name = htmlFile.getName();
        if ( name.endsWith( HtmlPanelVisual.HTML)){
            name = name.substring(0 , name.length()-HtmlPanelVisual.HTML.length());
        }
        
        Map<String,String> map = new HashMap<String, String>();
        StringBuilder builder = new StringBuilder();
        if ( underscore == null ){
            builder.append("<script src='http://documentcloud.github.com/underscore/underscore-min.js'>"); // NOI18N
            builder.append("</script>\n");  // NOI18N
        }
        else {
            String relativePath = FileUtil.getRelativePath(folder, underscore);
            builder.append("<script src='");    // NOI18N
            builder.append(relativePath);
            builder.append("'></script>\n");    // NOI18N
        }
        if ( backbone == null ){
            builder.append("<script src='http://backbonejs.org/backbone-min.js'></script>\n");// NOI18N
        }
        else {
            String relativePath = FileUtil.getRelativePath(folder, backbone);
            builder.append("<script src='");// NOI18N
            builder.append(relativePath);
            builder.append("'></script>\n");  // NOI18N
        }
        if ( jQuery == null ){
            builder.append("<script src='http://code.jquery.com/jquery-1.7.2.min.js'></script>\n");// NOI18N
        }
        else {
            String relativePath = FileUtil.getRelativePath(folder, jQuery);
            builder.append("<script src='");// NOI18N
            builder.append(relativePath);
            builder.append("'></script>\n");  // NOI18N
        }
        
        String relativePath = FileUtil.getRelativePath(folder, appFile );
        builder.append("<script src='");    // NOI18N
        builder.append(relativePath);
        builder.append("'></script>");      // NOI18N
        map.put("script", builder.toString());  // NOI18N
        
        DataObject createdFile = templateDO.createFromTemplate(dataFolder, 
                name, map);
    }
    
    private void setSteps() {
        Object contentData = myWizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA);  
        if ( contentData instanceof String[] ){
            String steps[] = (String[])contentData;
            String newSteps[] = new String[ steps.length +1];
            System.arraycopy(steps, 0, newSteps, 0, 1);
            newSteps[newSteps.length-2]=NbBundle.getMessage(JSClientIterator.class, 
                "TXT_JsFile");        // NOI18N
            newSteps[newSteps.length-1]=NbBundle.getMessage(JSClientIterator.class, 
                    "TXT_HtmlFile");        // NOI18N
            for( int i=0; i<myPanels.length; i++ ){
                Panel panel = myPanels[i];
                JComponent component = (JComponent)panel.getComponent();
                component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, newSteps);
                component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
            }
        }
    }
    
    private FileObject addLibrary(FileObject libs, String libName ) {
        Library backbone = WebClientLibraryManager.findLibrary(libName, 
                null);    // NOI18N
        if ( backbone == null ){
            return null;
        }
        try {
            List<FileObject> files = WebClientLibraryManager.addLibraries(new Library[]{backbone}, 
                    libs, null);
            if ( !files.isEmpty() ){
                return files.get(0);
            }
            return null;
        }
        catch(IOException e ){
            return null;
        }
        catch(MissingLibResourceException e ){
            List<FileObject> files = e.getResources();
            if ( !files.isEmpty() ){
                return files.get(0);
            }
            return null;
        }
    }
    
    private WizardDescriptor myWizard;
    private RestPanel myRestPanel;
    private WizardDescriptor.Panel[] myPanels;
    private int myIndex;

}
