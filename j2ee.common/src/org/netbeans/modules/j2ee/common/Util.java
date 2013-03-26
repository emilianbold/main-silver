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

package org.netbeans.modules.j2ee.common;

import java.awt.Component;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JLabel;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JComponent;
import java.util.Iterator;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileLock;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.w3c.dom.Element;

public class Util {

    public static final String ENDORSED_LIBRARY_NAME = "javaee-endorsed-api-6.0"; // NOI18N
    public static final String ENDORSED_LIBRARY_CLASSPATH = "${libs."+ENDORSED_LIBRARY_NAME+".classpath}"; // NOI18N

    public static final String DESTINATION_DIRECTORY = "destinationDirectory";
    public static final String DESTINATION_DIRECTORY_ROOT = "100";
    public static final String DESTINATION_DIRECTORY_LIB = "200";
    public static final String DESTINATION_DIRECTORY_DO_NOT_COPY = "300";

    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

    public static void updateDirsAttributeInCPSItem(org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item item,
            Element element) {
        String dirs = item.getAdditionalProperty(Util.DESTINATION_DIRECTORY);
        if (dirs == null) {
            dirs = Util.DESTINATION_DIRECTORY_LIB;
            if (item.getType() == org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item.TYPE_ARTIFACT && !item.isBroken()) {
                if (item.getArtifact() != null && item.getArtifact().getProject() != null &&
                    item.getArtifact().getProject().getLookup().lookup(J2eeModuleProvider.class) != null) {
                    dirs = Util.DESTINATION_DIRECTORY_ROOT;
                }

            }
        }
        element.setAttribute("dirs", dirs); // NOI18N
    }
    
    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static void changeLabelInComponent(JComponent component, String oldLabel, String newLabel) {
        JLabel label = findLabel(component, oldLabel);
        if(label != null) {
            label.setText(newLabel);
        }
    }
    
    /*
     * Hides a JLabel and the component that it is designated to labelFor, if any
     */
    public static void hideLabelAndLabelFor(JComponent component, String lab) {
        JLabel label = findLabel(component, lab);
        if(label != null) {
            label.setVisible(false);
            Component c = label.getLabelFor();
            if(c != null) {
                c.setVisible(false);
            }
        }
    }
    
    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public static void getAllComponents( Component[] components, Collection<Component> allComponents ) {
        for( int i = 0; i < components.length; i++ ) {
            if( components[i] != null ) {
                allComponents.add( components[i] );
                if( ( ( Container )components[i] ).getComponentCount() != 0 ) {
                    getAllComponents( ( ( Container )components[i] ).getComponents(), allComponents );
                }
            }
        }
    }
    
    /*
     *  Recursively finds a JLabel that has labelText in comp
     */
    public static JLabel findLabel(JComponent comp, String labelText) {
        List<Component> allComponents = new ArrayList<Component>();
        getAllComponents(comp.getComponents(), allComponents);
        Iterator<Component> iterator = allComponents.iterator();
        while(iterator.hasNext()) {
            Component c = iterator.next();
            if(c instanceof JLabel) {
                JLabel label = (JLabel)c;
                if(label.getText().equals(labelText)) {
                    return label;
                }
            }
        }
        return null;
    }
    
    
    public static ClassPath getFullClasspath(FileObject fo) {
        if (fo == null) {
            return null;
        }
        return ClassPathSupport.createProxyClassPath(new ClassPath[]{
            ClassPath.getClassPath(fo, ClassPath.SOURCE),
            ClassPath.getClassPath(fo, ClassPath.BOOT),
            ClassPath.getClassPath(fo, ClassPath.COMPILE)
        });
    }
    
    /**
     * Is J2EE version of a given project JavaEE 5 or higher?
     *
     * @param project J2EE project
     * @return true if J2EE version is JavaEE 5 or higher; otherwise false
     */
    public static boolean isJavaEE5orHigher(Project project) {
        if (project == null) {
            return false;
        }
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            J2eeModule j2eeModule = j2eeModuleProvider.getJ2eeModule();
            if (j2eeModule != null) {
                J2eeModule.Type type = j2eeModule.getType();
                String strVersion = j2eeModule.getModuleVersion();
                assert strVersion != null : "Module type " + j2eeModule.getType() + " returned null module version"; // NOI18N
                try {    
                    double version = Double.parseDouble(strVersion);
                    if (J2eeModule.Type.EJB.equals(type) && (version > 2.1)) {
                        return true;
                    }
                    if (J2eeModule.Type.WAR.equals(type) && (version > 2.4)) {
                        return true;
                    }
                    if (J2eeModule.Type.CAR.equals(type) && (version > 1.4)) {
                        return true;
                    }
                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.INFO, "Module version invalid " + strVersion, ex);
                }                
            }
        }
        return false;
    }

    /**
     * Find out if the version of the given profile is at least Java EE 5 or higher.
     *
     * @param profile profile that we want to compare
     * @return true if the version of the given profile is Java EE 5 or higher,
     *         false otherwise
     * @since 1.75
     */
    public static boolean isAtLeastJavaEE5(@NonNull Profile profile) {
        return isVersionEqualOrHigher(profile, Profile.JAVA_EE_5);
    }

    /**
     * Find out if the version of the given profile is at least Java EE 6 Web or
     * higher. Please be aware that Java EE 6 Web is considered as lower than Java
     * EE 6 Full.
     *
     * @param profile profile that we want to compare
     * @return true if the version of the given profile is Java EE 6 Web or
     *         higher, false otherwise
     * @since 1.75
     */
    public static boolean isAtLeastJavaEE6Web(@NonNull Profile profile) {
        return isVersionEqualOrHigher(profile, Profile.JAVA_EE_6_WEB);
    }

    /**
     * Find out if the version of the given profile is at least Java EE 7 Web or
     * higher. Please be aware that Java EE 7 Web is considered as lower than Java
     * EE 7 Full.
     *
     * @param profile profile that we want to compare
     * @return true if the version of the given profile is Java EE 7 Web or
     *         higher, false otherwise
     * @since 1.79
     */
    public static boolean isAtLeastJavaEE7Web(@NonNull Profile profile) {
        return isVersionEqualOrHigher(profile, Profile.JAVA_EE_7_WEB);
    }

    /**
     * Compares if the first given profile has equal or higher Java EE version
     * in comparison to the second profile.
     *
     * Please be aware of the following rules:
     * <br/><br/>
     *
     * 1) Each Java EE X version is considered as lower than Java EE X+1 version
     * (this applies regardless on Web/Full specification and in reality it means
     * that even Java EE 6 Full version is considered as lower than Java EE 7 Web)
     * <br/><br/>
     *
     * 2) Each Java EE X Web version is considered as lower than Java EE X Full
     * <br/>
     *
     * @param profileToCompare profile that we want to compare
     * @param comparingVersion version which we are comparing with
     * @return <code>true</code> if the profile version is equal or higher in
     *         comparison with the second one, <code>false</code> otherwise
     * @since 1.75
     */
    private static boolean isVersionEqualOrHigher(
            @NonNull Profile profileToCompare,
            @NonNull Profile comparingVersion) {

        int comparisonResult = Profile.UI_COMPARATOR.compare(profileToCompare, comparingVersion);
        if (comparisonResult == 0) {
            // The same version for both
            return true;

        } else {
            String profileToCompareVersion = getProfileVersion(profileToCompare);
            String comparingProfileVersion = getProfileVersion(comparingVersion);

            // If the canonicalName is the same value we have to differ between Web and Full profile
            if (profileToCompareVersion.equals(comparingProfileVersion)) {
                return compareWebAndFull(profileToCompare, comparingVersion);
            } else {
                if (comparisonResult > 0) {
                    // profileToCompare has lower version than comparingVersion
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    private static boolean compareWebAndFull(
            @NonNull Profile profileToCompare,
            @NonNull Profile comparingVersion) {

        boolean isThisFullProfile = isFullProfile(profileToCompare);
        boolean isParamFullProfile = isFullProfile(comparingVersion);

        if (isThisFullProfile && isParamFullProfile) {
            // Both profiles are Java EE Full
            return true;
        }
        if (!isThisFullProfile && !isParamFullProfile) {
            // Both profiles are Java EE Web
            return true;
        }
        if (isThisFullProfile && !isParamFullProfile) {
            // profileToCompare is Java EE Full profile and comparingVersion is only Java EEWeb profile
            return true;
        }
        return false;
    }

    private static String getProfileVersion(@NonNull Profile profile) {
        String profileDetails = profile.toPropertiesString();
        int indexOfDash = profileDetails.indexOf("-");
        if (indexOfDash != -1) {
            return profileDetails.substring(0, indexOfDash);
        }
        return profileDetails;
    }

    private static boolean isFullProfile(@NonNull Profile profile) {
        final String profileDetails = profile.toPropertiesString();
        if (profileDetails.indexOf("-") == -1) {
            return true;
        }
        return false;
    }
    
    /**
     * Returns source level of a given project
     *
     * @param project Project
     * @return source level string representation, e.g. "1.6"
     */
    public static String getSourceLevel(Project project) {
        // XXX clients should never request source level for a project, as it may differ
        // from file to file. Source root file object should be used instead (just use
        // org.netbeans.api.java.queries.SourceLevelQuery).
        String srcLevel = null;
        SourceLevelQueryImplementation2 sl2 = project.getLookup().lookup(SourceLevelQueryImplementation2.class);
        if(sl2 != null){
            srcLevel = sl2.getSourceLevel(project.getProjectDirectory()).getSourceLevel();
        } else {
            //backward compartibility
            SourceLevelQueryImplementation sl = project.getLookup().lookup(SourceLevelQueryImplementation.class);
            if(sl != null){
                srcLevel = sl.getSourceLevel(project.getProjectDirectory());
            }
        }
        return srcLevel;
    }
    
    /**
     * Is source level of a given project 1.4 or lower?
     *
     * @param project Project
     * @return true if source level is 1.4 or lower; otherwise false
     */
    public static boolean isSourceLevel14orLower(Project project) {
        String srcLevel = getSourceLevel(project);
        if (srcLevel != null) {
            double sourceLevel = Double.parseDouble(srcLevel);
            return (sourceLevel <= 1.4);
        } else
            return false;
    }
    
    /**
     * Is source level of a given project 1.6 or higher?
     *
     * @param project Project
     * @return true if source level is 1.6 or higher; otherwise false
     */
    public static boolean isSourceLevel16orHigher(Project project) {
        String srcLevel = getSourceLevel(project);
        if (srcLevel != null) {
            double sourceLevel = Double.parseDouble(srcLevel);
            return (sourceLevel >= 1.6);
        } else
            return false;
    }
    
    /**
     * Checks whether the given <code>project</code>'s target server instance
     * is present.
     *
     * @param  project the project to check; can not be null.
     * @return true if the target server instance of the given project
     *          exists, false otherwise.
     *
     * @since 1.8
     */
    public static boolean isValidServerInstance(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null) {
            return false;
        }
        return isValidServerInstance(j2eeModuleProvider);
    }
    
    /**
     * Checks whether the given <code>provider</code>'s target server instance
     * is present.
     *
     * @param  provider the provider to check; can not be null.
     * @return true if the target server instance of the given provider
     *          exists, false otherwise.
     *
     * @since 1.10
     */
    public static boolean isValidServerInstance(J2eeModuleProvider j2eeModuleProvider) {
        String serverInstanceID = j2eeModuleProvider.getServerInstanceID();
        if (serverInstanceID == null) {
            return false;
        }
        return Deployment.getDefault().getServerID(serverInstanceID) != null;
    }
    
    /**
     * Default implementation of ServerStatusProvider.
     */
    public static ServerStatusProvider createServerStatusProvider(final J2eeModuleProvider j2eeModuleProvider) {
        return new ServerStatusProvider() {
            public boolean validServerInstancePresent() {
                return isValidServerInstance(j2eeModuleProvider);
            }
        };
    }

    @NonNull
    public static File[] getJ2eePlatformClasspathEntries(@NullAllowed Project project, @NullAllowed J2eePlatform j2eePlatform) {
        if (project != null) {
            J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            if (j2eeModuleProvider != null) {
                J2eePlatform j2eePlatformLocal = j2eePlatform != null
                        ? j2eePlatform
                        : Deployment.getDefault().getJ2eePlatform(j2eeModuleProvider.getServerInstanceID());
                if (j2eePlatformLocal != null) {
                    try {
                        return j2eePlatformLocal.getClasspathEntries(j2eeModuleProvider.getConfigSupport().getLibraries());
                    } catch (ConfigurationException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                        return j2eePlatformLocal.getClasspathEntries();
                    }
                }
            }
        }
        if (j2eePlatform != null) {
            return j2eePlatform.getClasspathEntries();
        }
        return new File[] {};
    }
    
    /**
     * Returns true if the specified classpath contains a class of the given name,
     * false otherwise.
     * 
     * @param classpath consists of jar urls and folder urls containing classes
     * @param className the name of the class
     * 
     * @return true if the specified classpath contains a class of the given name,
     *         false otherwise.
     * 
     * @throws IOException if an I/O error has occurred
     * 
     * @since 1.15
     */
    public static boolean containsClass(List<URL> classPath, String className) throws IOException {
        Parameters.notNull("classpath", classPath); // NOI18N
        Parameters.notNull("className", className); // NOI18N
        
        List<File> diskFiles = new ArrayList<File>();
        for (URL url : classPath) {
            URL archiveURL = FileUtil.getArchiveFile(url);
            
            if (archiveURL != null) {
                url = archiveURL;
            }
            
            if ("nbinst".equals(url.getProtocol())) { // NOI18N
                // try to get a file: URL for the nbinst: URL
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    URL localURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                    if (localURL != null) {
                        url = localURL;
                    }
                }
            }
            
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                File diskFile = FileUtil.toFile(fo);
                if (diskFile != null) {
                    diskFiles.add(diskFile);
                }
            }
        }
        
        return containsClass(diskFiles, className);
    }
    
    /**
     * Returns true if the specified classpath contains a class of the given name,
     * false otherwise.
     * 
     * @param classpath consists of jar files and folders containing classes
     * @param className the name of the class
     * 
     * @return true if the specified classpath contains a class of the given name,
     *         false otherwise.
     * 
     * @throws IOException if an I/O error has occurred
     * 
     * @since 1.15
     */
    public static boolean containsClass(Collection<File> classpath, String className) throws IOException {
        Parameters.notNull("classpath", classpath); // NOI18N
        Parameters.notNull("driverClassName", className); // NOI18N
        String classFilePath = className.replace('.', '/') + ".class"; // NOI18N
        for (File file : classpath) {
            if (file.isFile()) {
                JarInputStream is = new JarInputStream(new BufferedInputStream(
                        new FileInputStream(file)), false);
                try {
                    JarEntry entry;
                    while ((entry = is.getNextJarEntry()) != null) {
                        if (classFilePath.equals(entry.getName())) {
                            return true;
                        }
                    }
                } finally {
                    is.close();
                }
            } else {
                if (new File(file, classFilePath).exists()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates URL for the file to be used for classpath. This means for
     * jar and zip file method will return jar: URL for other files or
     * directories it will return file: URL.
     *
     * @param file the file to get URL for
     * @return the proper URL
     * @throws MalformedURLException thrown if URL could not be constructed
     * @since 1.70
     */
    public static URL fileToUrl(File file) throws MalformedURLException {
        URL url = file.toURI().toURL();
        if (!file.isDirectory()) {
            if (file.getName().endsWith(".zip") || file.getName().endsWith("jar")) {
            // isArchiveFile reads the bytes from file which is forbidden
            // to be done from UI - check fro extensions should be safe enough
            // see #207440
            //if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
        }
        return url;
    }
    
    /**
     * Returns the label (the name) of the given Java EE specification version.
     * 
     * @param specificationVersion version of the Java EE specification.
     *        Acceptable values are those defined in {@link J2eeModule}:
     * <ul>
     *     <li>{@link J2eeModule.J2EE_13}
     *     <li>{@link J2eeModule.J2EE_14}
     *     <li>{@link J2eeModule.JAVA_EE_5}
     * </ul>
     * 
     * @return true if the specified classpath contains a class of the given name,
     *         false otherwise.
     * 
     * @throws NullPointerException if the specificationVersion is <code>null</code>
     * @throws IllegalArgumentException if the value of the method parameter
     *         is not known specification version constant
     * 
     * @since 1.18
     * @deprecated 
     */    
    public static String getJ2eeSpecificationLabel(String specificationVersion) {
        Parameters.notNull("specificationVersion", specificationVersion); // NOI18N
        
        if (J2eeModule.J2EE_13.equals(specificationVersion)) {
            return NbBundle.getMessage(Util.class, "LBL_J2EESpec_13");
        } else if (J2eeModule.J2EE_14.equals(specificationVersion)) {
            return NbBundle.getMessage(Util.class, "LBL_J2EESpec_14");
        } else if (J2eeModule.JAVA_EE_5.equals(specificationVersion)) {
            return NbBundle.getMessage(Util.class, "LBL_JavaEESpec_5");  
        } else {
            throw new IllegalArgumentException("Unknown specification version: " + specificationVersion); // NOI18N
        }
    }

    public static Set<Profile> getSupportedProfiles(Project project){
        Set<Profile> supportedProfiles = new HashSet<Profile>();
        J2eePlatform j2eePlatform = getPlatform(project);
        if (j2eePlatform != null){
            supportedProfiles = j2eePlatform.getSupportedProfiles();
        }
        return supportedProfiles;
    }

    /**
     * Gets {@link J2eePlatform} for the given {@code Project}.
     *
     * @param project project
     * @return {@code J2eePlatform} for given project if found, {@code null} otherwise
     * @since 1.69
     */
    public static J2eePlatform getPlatform(Project project) {
        try {
            J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
            if (provider != null){
                String instance = provider.getServerInstanceID();
                if (instance != null) {
                    return Deployment.getDefault().getServerInstance(provider.getServerInstanceID()).getJ2eePlatform();
                }
            }
        } catch (InstanceRemovedException ex) {
            // will return null
        }
        return null;
    }

    public static void backupBuildImplFile(UpdateHelper updateHelper) throws IOException {
        //When the project.xml was changed from the customizer and the build-impl.xml was modified
        //move build-impl.xml into the build-impl.xml~ to force regeneration of new build-impl.xml.
        //Never do this if it's not a customizer otherwise user modification of build-impl.xml will be deleted
        //when the project is opened.
        final FileObject projectDir = updateHelper.getAntProjectHelper().getProjectDirectory();
        final FileObject buildImpl = projectDir.getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
        if (buildImpl  != null) {
            final String name = buildImpl.getName();
            final String backupext = String.format("%s~",buildImpl.getExt());   //NOI18N
            final FileObject oldBackup = buildImpl.getParent().getFileObject(name, backupext);
            if (oldBackup != null) {
                oldBackup.delete();
            }
            FileLock lock = buildImpl.lock();
            try {
                buildImpl.rename(lock, name, backupext);
            } finally {
                lock.releaseLock();
            }
        }
    }

    public static void initTwoColumnTableVisualProperties(Component component, JTable table) {
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        // set the color of the table's JViewport
        table.getParent().setBackground(table.getBackground());
        updateColumnWidths(table);
        component.addComponentListener(new TableColumnSizeComponentAdapter(table));
    }

    private static void updateColumnWidths(JTable table) {
        //we'll get the parents width so we can use that to set the column sizes.
        double pw = table.getParent().getSize().getWidth();
        
        //#88174 - Need horizontal scrollbar for library names
        //ugly but I didn't find a better way how to do it
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn column = table.getColumnModel().getColumn(1);
        int w = ((int)pw/2) - 1;
        if (w > column.getMaxWidth()) {
            // second column sometimes might have max width (packace column in Libraries)
            w = column.getMaxWidth();
        }
        column.setWidth(w);
        column.setPreferredWidth(w);
        
        w = (int)pw - w;
        column = table.getColumnModel().getColumn(0);
        column.setWidth( w );
        column.setPreferredWidth( w );
    }

    private static class TableColumnSizeComponentAdapter extends ComponentAdapter {
        private JTable table = null;
        
        public TableColumnSizeComponentAdapter(JTable table){
            this.table = table;
        }
        
        public void componentResized(ComponentEvent evt){
            updateColumnWidths(table);
        }
    }
    
    
}
