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
package org.netbeans.modules.maven.j2ee.web;

import java.io.IOException;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openide.util.RequestProcessor;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.maven.spi.actions.ActionConvertor;
import org.netbeans.modules.maven.spi.actions.ReplaceTokenProvider;
import org.netbeans.modules.web.api.webmodule.RequestParametersQuery;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class WebReplaceTokenProvider implements ReplaceTokenProvider, ActionConvertor {

    private static final String WEB_PATH =          "webpagePath";      //NOI18N
    public static final String ATTR_EXECUTION_URI = "execution.uri";    //NOI18N
    public static final String FILE_DD        =     "web.xml";          //NOI18N
    private static final String IS_SERVLET_FILE = 
        "org.netbeans.modules.web.IsServletFile";                       //NOI18N

    private Project project;
    private AtomicBoolean   isScanStarted;
    private AtomicBoolean   isScanFinished;

    public WebReplaceTokenProvider(Project prj) {
        project = prj;
        isScanStarted = new AtomicBoolean( false );
        isScanFinished = new AtomicBoolean(false);
    }
    /**
     * just gets the array of FOs from lookup.
     */
    protected static FileObject[] extractFileObjectsfromLookup(Lookup lookup) {
        List<FileObject> files = new ArrayList<FileObject>();
        Iterator<? extends DataObject> it = lookup.lookup(new Lookup.Template<DataObject>(DataObject.class)).allInstances().iterator();
        while (it.hasNext()) {
            DataObject d = it.next();
            FileObject f = d.getPrimaryFile();
            files.add(f);
        }
        return files.toArray(new FileObject[files.size()]);
    }

    public Map<String, String> createReplacements(String action, Lookup lookup) {
        FileObject[] fos = extractFileObjectsfromLookup(lookup);
        String relPath = null;
        SourceGroup group = null;
        FileObject fo = null;
        HashMap<String, String> replaceMap = new HashMap<String, String>();
        if (fos.length > 0 && action.endsWith(".deploy")) { //NOI18N
            fo = fos[0];
            Sources srcs = project.getLookup().lookup(Sources.class);
            //for jsps
            String requestParams = RequestParametersQuery.getFileAndParameters(fo);
            if (requestParams != null && !"/null".equals(requestParams)) { //IMHO a bug in the RPQI in WebExSupport.java
                relPath =  requestParams;
            }
            if (relPath == null) {
            //for html
                String url = FileUtil.getRelativePath(WebModule.getWebModule(fo).getDocumentBase(), fo); 
                if (url != null) {
                    url = url.replace(" ", "%20"); //NOI18N
                    relPath =  "/" + url; //NOI18N
                }
            }
            if (relPath == null) {
                //TODO we shall check the resources as well, not sure that is covered here..
                // if not, this code is a duplication of the above snippet only..
                SourceGroup[] grp = srcs.getSourceGroups("doc_root"); //NOI18N J2EE
                for (int i = 0; i < grp.length; i++) {
                    relPath = FileUtil.getRelativePath(grp[i].getRootFolder(), fo);
                    if (relPath != null) {
                        break;
                    }
                }
            }

            if (relPath == null) {
                // run servlet
                if ("text/x-java".equals(fo.getMIMEType())) { //NOI18N
                    String executionUri = (String) fo.getAttribute(ATTR_EXECUTION_URI);
                    if (executionUri != null) {
                        relPath = executionUri;
                    } else {
                        WebModule webModule = WebModule.getWebModule(fo);
                        String[] urlPatterns = getServletMappings(webModule, fo);
                        if (urlPatterns != null && urlPatterns.length > 0) {
                            ServletUriPanel uriPanel = new ServletUriPanel(urlPatterns, null, true);
                            DialogDescriptor desc = new DialogDescriptor(uriPanel,
                                    NbBundle.getMessage(WebReplaceTokenProvider.class, "TTL_setServletExecutionUri"));
                            Object res = DialogDisplayer.getDefault().notify(desc);
                            if (res.equals(NotifyDescriptor.YES_OPTION)) {
                                relPath = uriPanel.getServletUri(); //NOI18N
                                try {
                                    fo.setAttribute(ATTR_EXECUTION_URI, uriPanel.getServletUri());
                                } catch (IOException ex) {
                                }
                            }
                        }

                    }

                }

            }
            if (relPath == null) {
                relPath = "";
            }
            replaceMap.put(WEB_PATH, relPath);//NOI18N
        }
        return replaceMap;
    }

    public static String[] getServletMappings(WebModule webModule, FileObject javaClass) {
        if (webModule == null)
            return null;


        ClassPath classPath = ClassPath.getClassPath (javaClass, ClassPath.SOURCE);
        String className = classPath.getResourceName(javaClass,'.',false);
        try {
            List<ServletInfo> servlets =
                    WebAppMetadataHelper.getServlets(webModule.getMetadataModel());
            List<String> mappingList = new ArrayList<String>();
            for (ServletInfo si : servlets) {
                if (className.equals(si.getServletClass())) {
                    mappingList.addAll(si.getUrlPatterns());
                }
            }
            String[] mappings = new String[mappingList.size()];
            mappingList.toArray(mappings);
            return mappings;
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String convert(String action, Lookup lookup) {
        if (ActionProvider.COMMAND_RUN_SINGLE.equals(action) ||
            ActionProvider.COMMAND_DEBUG_SINGLE.equals(action)) {
            FileObject[] fos = extractFileObjectsfromLookup(lookup);
            if (fos.length > 0) {
                FileObject fo = fos[0];
                if ("text/x-java".equals(fo.getMIMEType())) { //NOI18N
                    //TODO sorty of clashes with .main (if both servlet and main are present.
                    // also prohitibs any other conversion method.
                    if ( fo.getAttribute(ATTR_EXECUTION_URI) == null &&
                            servletFilesScanning( fo ) )
                    {
                        return null;
                    }
                    Sources srcs = project.getLookup().lookup(Sources.class);
                    SourceGroup[] grp = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    for (int i = 0; i < grp.length; i++) {
                        if (!"2TestSourceRoot".equals(grp[i].getName())) { //NOI18N hack
                            String relPath = FileUtil.getRelativePath(grp[i].getRootFolder(), fo);
                            if (relPath != null) {
                                if (fo.getAttribute(ATTR_EXECUTION_URI) != null ||
                                        Boolean.TRUE.equals(fo.getAttribute(IS_SERVLET_FILE))) {//NOI18N
                                    return action + ".deploy"; //NOI18N
                                }
                                if (isServletFile(fo,false))  {
                                    try {
                                        fo.setAttribute(IS_SERVLET_FILE, Boolean.TRUE); 
                                    } catch (java.io.IOException ex) {
                                        //we tried
                                    }
                                    return action + ".deploy"; //NOI18N
                                }
                            }
                        }
                    }
                }
                if ("text/x-jsp".equals(fo.getMIMEType())) { //NOI18N
                    return action + ".deploy"; //NOI18N
                }
                if ("text/html".equals(fo.getMIMEType())) { //NOI18N
                    return action + ".deploy"; //NOI18N
                }
            }
        }
        return null;
    }

    private static boolean isServletFile(FileObject javaClass, boolean initialScan) {
        if (javaClass == null) {
            return false;
        }

        ClassPath classPath = ClassPath.getClassPath (javaClass, ClassPath.SOURCE);
        if (classPath == null) {
            return false;
        }
        String className = classPath.getResourceName(javaClass,'.',false);
        if (className == null) {
            return false;
        }

        WebModule webModule = WebModule.getWebModule(javaClass);
        if (webModule == null) {
            //not sure how it can happen, but #176535 proves it can
            return false;
        }
        try {
            MetadataModel<WebAppMetadata> metadataModel = webModule
                    .getMetadataModel();
            boolean result = false;
            if ( initialScan || metadataModel.isReady()) {
                List<ServletInfo> servlets = WebAppMetadataHelper
                        .getServlets(metadataModel);
                List<String> servletClasses = new ArrayList<String>( servlets.size() );
                for (ServletInfo si : servlets) {
                    if (className.equals(si.getServletClass())) {
                        result =  true;
                    }
                    else {
                        servletClasses.add( si.getServletClass() );
                    }
                }
                setServletClasses( servletClasses,  javaClass , initialScan);
            }
            return result;
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * Method check if initial servlet scanning has been started.
     * It's done via setting special mark for project ( actually 
     * for  ProjectWebModule ).
     * 
     * Fix for IZ#172931 - [68cat] AWT thread blocked for 29229 ms.
     */
    private boolean servletFilesScanning( final FileObject fileObject ) 
 {
        if ( isScanFinished.get()){
            return false;
        }
        if ( isScanStarted.get()) {
            return true;
        }
        else {
            Runnable runnable = new Runnable() {

                public void run() {
                    isServletFile(fileObject, true);
                    isScanFinished.set(true);
                }
            };
            if (!isScanStarted.get()) {
                /*
                 * Double check . It's not good but not fatal. In the worst case
                 * we will start several initial scanning.
                 */
                RequestProcessor.getDefault().post(runnable);
                isScanStarted.set(true);
            }
            return true;
        }
    }
    
    /*
     * Created as  fix for IZ#172931 - [68cat] AWT thread blocked for 29229 ms.
     */
    private static void setServletClasses( final List<String> servletClasses, 
            final FileObject orig, boolean initial )
    {
        if ( initial ){
            JavaSource javaSource = JavaSource.forFileObject( orig );
            if ( javaSource == null) {
                return;
            }
            try {
            javaSource.runUserActionTask( new Task<CompilationController>(){
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase( Phase.ELEMENTS_RESOLVED );
                    for( String servletClass : servletClasses){
                        TypeElement typeElem = controller.getElements().
                            getTypeElement( servletClass);
                        if ( typeElem == null ){
                            continue;
                        }
                        ElementHandle<TypeElement> handle = 
                            ElementHandle.create( typeElem );
                        FileObject fileObject = SourceUtils.getFile( handle, 
                                controller.getClasspathInfo());
                        if ( fileObject != null && !Boolean.TRUE.equals(
                                fileObject.getAttribute(IS_SERVLET_FILE)))
                        {
                            fileObject.setAttribute(IS_SERVLET_FILE, Boolean.TRUE); 
                        }
                    }
                }
            }, true);
            }
            catch(IOException e ){
                e.printStackTrace();
            }
        }
        else {
            Runnable runnable = new Runnable() {
                
                public void run() {
                    setServletClasses(servletClasses, orig, true);
                }
            };
            RequestProcessor.getDefault().post(runnable);
        }
    }

}
