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
package org.netbeans.modules.j2ee.persistence.wizard;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Container;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JComponent;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JList;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.DefaultProvider;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider;
import org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo;
import org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.wizard.entity.WrapperPanel;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPWizard;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanelDS;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanelJdbc;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

/**
 * Copy of j2ee/utilities Util class
 */
public class Util {

    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static void changeLabelInComponent(JComponent component, String oldLabel, String newLabel) {
        JLabel label = findLabel(component, oldLabel);
        if (label != null) {
            label.setText(newLabel);
        }
    }

    /*
     * Hides a JLabel and the component that it is designated to labelFor, if any
     */
    public static void hideLabelAndLabelFor(JComponent component, String lab) {
        JLabel label = findLabel(component, lab);
        if (label != null) {
            label.setVisible(false);
            Component c = label.getLabelFor();
            if (c != null) {
                c.setVisible(false);
            }
        }
    }

    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public static void getAllComponents(Component[] components, Collection allComponents) {
        for (int i = 0; i < components.length; i++) {
            if (components[i] != null) {
                allComponents.add(components[i]);
                if (((Container) components[i]).getComponentCount() != 0) {
                    getAllComponents(((Container) components[i]).getComponents(), allComponents);
                }
            }
        }
    }

    /*
     *  Recursively finds a JLabel that has labelText in comp
     */
    public static JLabel findLabel(JComponent comp, String labelText) {
        Vector allComponents = new Vector();
        getAllComponents(comp.getComponents(), allComponents);
        Iterator iterator = allComponents.iterator();
        while (iterator.hasNext()) {
            Component c = (Component) iterator.next();
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                if (label.getText().equals(labelText)) {
                    return label;
                }
            }
        }
        return null;
    }

    /**
     * Returns the SourceGroup of the passesd project which contains the
     * fully-qualified class name.
     */
    public static SourceGroup getClassSourceGroup(Project project, String fqClassName) {
        String classFile = fqClassName.replace('.', '/') + ".java"; // NOI18N
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject classFO = sourceGroup.getRootFolder().getFileObject(classFile);
            if (classFO != null) {
                return sourceGroup;
            }
        }
        return null;
    }

    private static List/*<FileObject>*/ getFileObjects(URL[] urls) {
        List result = new ArrayList();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else if (Logger.getLogger("global").isLoggable(Level.FINE)) {
                Logger.getLogger("global").log(Level.FINE, null, new IllegalStateException("No FileObject found for the following URL: " + urls[i]));
            }
        }
        return result;
    }

    public static ClassPath getFullClasspath(FileObject fo) {
        FileObject[] sourceRoots = ClassPath.getClassPath(fo, ClassPath.SOURCE).getRoots();
        FileObject[] bootRoots = ClassPath.getClassPath(fo, ClassPath.BOOT).getRoots();
        FileObject[] compileRoots = ClassPath.getClassPath(fo, ClassPath.COMPILE).getRoots();
        FileObject[] roots = new FileObject[sourceRoots.length + bootRoots.length + compileRoots.length];
        for (int i = 0; i < sourceRoots.length; i++) {
            roots[i] = sourceRoots[i];
        }
        for (int i = 0; i < bootRoots.length; i++) {
            roots[sourceRoots.length + i] = bootRoots[i];
        }
        for (int i = 0; i < compileRoots.length; i++) {
            roots[sourceRoots.length + bootRoots.length + i] = compileRoots[i];
        }
        return ClassPathSupport.createClassPath(roots);
    }

    // from ejbcore utils
    public static boolean isSupportedJavaEEVersion(Project project) {
        JPAModuleInfo moduleInfo = project.getLookup().lookup(JPAModuleInfo.class);
        if (moduleInfo == null) {
            return false;
        }
        if (JPAModuleInfo.ModuleType.EJB == moduleInfo.getType()
                && ("3.1".equals(moduleInfo.getVersion()) || "3.0".equals(moduleInfo.getVersion()))) {
            return true;
        }
        if (JPAModuleInfo.ModuleType.WEB == moduleInfo.getType()
                && ("3.0".equals(moduleInfo.getVersion()) || "2.5".equals(moduleInfo.getVersion()))) {
            return true;
        }
        return false;
    }

    public static boolean isContainerManaged(Project project) {
        PersistenceProviderSupplier providerSupplier = project.getLookup().lookup(PersistenceProviderSupplier.class);
        return Util.isSupportedJavaEEVersion(project) && providerSupplier != null && providerSupplier.supportsDefaultProvider();
    }

    public static Provider getDefaultProvider(Project project) {
        PersistenceProviderSupplier providerSupplier = project.getLookup().lookup(PersistenceProviderSupplier.class);
        if ((providerSupplier != null && providerSupplier.supportsDefaultProvider())) {
            List<Provider> providers = providerSupplier.getSupportedProviders();
            if (providers.size() > 0) {
                return providers.get(0);
            }
            Logger.getLogger(RelatedCMPWizard.class.getName()).log(Level.WARNING, "Default provider support is reported without any supported providers. See: " + providerSupplier);
        }
        return null;
    }

    public static ArrayList<Provider> getProviders(Project project) {
        PersistenceProviderSupplier aProviderSupplier = project.getLookup().lookup(PersistenceProviderSupplier.class);

        if (aProviderSupplier == null) {
            // a java se project
            aProviderSupplier = new DefaultPersistenceProviderSupplier();
        }

        ArrayList<Provider> providers = new ArrayList<Provider>(aProviderSupplier.getSupportedProviders());
        if (providers.size() == 0 && aProviderSupplier.supportsDefaultProvider()) {
            providers.add(ProviderUtil.DEFAULT_PROVIDER);
        }

        addProvidersFromLibraries(providers);
        //
        return providers;
    }

    public static boolean isDefaultProvider(Project project, Provider provider) {
        return provider != null && provider.equals(getDefaultProvider(project));
    }

    public static boolean isEjbModule(Project project) {
        JPAModuleInfo moduleInfo = project.getLookup().lookup(JPAModuleInfo.class);
        if (moduleInfo == null) {
            return false;
        }
        return JPAModuleInfo.ModuleType.EJB == moduleInfo.getType();
    }

    public static boolean isEjb21Module(Project project) {
        JPAModuleInfo moduleInfo = project.getLookup().lookup(JPAModuleInfo.class);
        if (moduleInfo == null) {
            return false;
        }

        return JPAModuleInfo.ModuleType.EJB == moduleInfo.getType()
                && "2.1".equals(moduleInfo.getVersion());
    }

    /**
     * @return true if given this data object's project's enviroment is Java SE, false otherwise.
     */
    public static boolean isJavaSE(Project project) {
        return project.getLookup().lookup(JPAModuleInfo.class) == null;
    }

    /**
     * Builds a persistence unit using wizard. Does not save the created persistence unit
     * nor create the persistence.xml file if it  does not exist.
     * @param project the current project
     * @param preselectedDB the name of the database connection that should be preselected in the wizard.
     * @tableGeneration the table generation strategy that should be preselected in the wizard.
     * @return the created PersistenceUnit or null if nothing was created, for example
     * if wizard was cancelled.
     */
    public static PersistenceUnit buildPersistenceUnitUsingWizard(Project project,
            String preselectedDB, TableGeneration tableGeneration) {

        boolean isContainerManaged = Util.isContainerManaged(project);
        PersistenceUnitWizardPanel panel;
        if (isContainerManaged) {
            panel = new PersistenceUnitWizardPanelDS(project, null, true, tableGeneration);
        } else {
            panel = new PersistenceUnitWizardPanelJdbc(project, null, true, tableGeneration);
        }
        if (preselectedDB != null) {
            panel.setPreselectedDB(preselectedDB);
        }

        final JButton createPUButton = new JButton(NbBundle.getMessage(Util.class, "LBL_CreatePersistenceUnitButton"));
        createPUButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Util.class, "ACSD_CreatePersistenceUnitButton"));
        Object[] buttons = new Object[]{createPUButton, DialogDescriptor.CANCEL_OPTION};

        final DialogDescriptor nd = new DialogDescriptor(
                new WrapperPanel(panel),
                NbBundle.getMessage(Util.class, "LBL_CreatePersistenceUnit"),
                true,
                buttons,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        panel.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(PersistenceUnitWizardPanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        nd.setValid(((Boolean) newvalue).booleanValue());
                        createPUButton.setEnabled(((Boolean) newvalue).booleanValue());
                    }
                }
            }
        });
        if (!panel.isValidPanel()) {
            nd.setValid(false);
            createPUButton.setEnabled(false);
        }
        Object result = DialogDisplayer.getDefault().notify(nd);
        //add necessary libraries before pu creation
        Library lib = null;
        boolean libIsAdded = false;//used to check if lib was added to compile classpath
        if (result == createPUButton) {
            if (isContainerManaged) {
                //TODO: verify if need to add library here
                PersistenceUnitWizardPanelDS puDS = (PersistenceUnitWizardPanelDS) panel;
                lib = PersistenceLibrarySupport.getLibrary(puDS.getSelectedProvider());
            } else {
                PersistenceUnitWizardPanelJdbc puJdbc = (PersistenceUnitWizardPanelJdbc) panel;
                lib = PersistenceLibrarySupport.getLibrary(puJdbc.getSelectedProvider());
                //TODO: verify if don't need to add library here
                if (lib != null) {
                    libIsAdded = true;
                    addLibraryToProject(project, lib);
                }
                JDBCDriver[] driver = JDBCDriverManager.getDefault().getDrivers(puJdbc.getPersistenceConnection().getDriverClass());
                PersistenceLibrarySupport.addDriver(project, driver[0]);
            }
        }
        String version = (lib != null && libIsAdded) ? PersistenceUtils.getJPAVersion(lib) : PersistenceUtils.getJPAVersion(project);//use library if possible it will provide better result, TODO: may be usage of project should be removed and use 1.0 is no library was found
        if (result == createPUButton) {
            PersistenceUnit punit = null;
            if (Persistence.VERSION_2_0.equals(version)) {
                punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
            } else//currently default 1.0
            {
                punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
            }
            if (isContainerManaged) {
                PersistenceUnitWizardPanelDS puPanel = (PersistenceUnitWizardPanelDS) panel;
                if (puPanel.getDatasource() != null && !"".equals(puPanel.getDatasource().trim())) {
                    if (puPanel.isJTA()) {
                        punit.setJtaDataSource(puPanel.getDatasource());
                    } else {
                        if (puPanel.isNonDefaultProviderEnabled()) {
                            punit.setNonJtaDataSource(puPanel.getDatasource());
                        }
                        punit.setTransactionType("RESOURCE_LOCAL");
                    }
                }
                if (puPanel.isNonDefaultProviderEnabled()) {
                    punit.setProvider(puPanel.getNonDefaultProvider());
                }
            } else {
                PersistenceUnitWizardPanelJdbc puJdbc = (PersistenceUnitWizardPanelJdbc) panel;
                punit = ProviderUtil.buildPersistenceUnit(puJdbc.getPersistenceUnitName(), puJdbc.getSelectedProvider(), puJdbc.getPersistenceConnection(), version);
                punit.setTransactionType("RESOURCE_LOCAL"); //NOI18N
                // Explicitly add <exclude-unlisted-classes>false</exclude-unlisted-classes>
                // See issue 142575 - desc 10
                if (!Util.isJavaSE(project)) {
                    punit.setExcludeUnlistedClasses(false);
                }
            }
            punit.setName(panel.getPersistenceUnitName());
            ProviderUtil.setTableGeneration(punit, panel.getTableGeneration(), project);
            return punit;
        }
        return null;

    }

    /**
     * Builds a persistence unit using data passed as parameters. Does not save the created persistence unit
     * nor create the persistence.xml file if it  does not exist.
     * If some parameters are null, try to find default one or best match
     * @param project the current project
     * @param name name for pu, if null default will be used
     * @param preselectedDB the name of the database connection that should be preselected in the wizard.
     * @tableGeneration the table generation strategy that should be preselected in the wizard.
     * @return the created PersistenceUnit or null if nothing was created, for example
     * if wizard was cancelled.
     */
    public static PersistenceUnit buildPersistenceUnitUsingData(Project project, String puName,
            String preselectedDB, TableGeneration tableGeneration, Provider provider) {

        boolean isContainerManaged = Util.isContainerManaged(project);

        if (provider == null) {
            //choose default/first provider
            ArrayList<Provider> providers = getProviders(project);
            //
            provider = providers.get(0);
        }
        //add necessary libraries before pu creation
        Library lib = null;
        boolean libIsAdded = false;//used to check if lib was added to compile classpath
        if (isContainerManaged) {
            //TODO: verify if need to add library here
            lib = PersistenceLibrarySupport.getLibrary(provider);
        } else {
            lib = PersistenceLibrarySupport.getLibrary(provider);
            //TODO: verify if don't need to add library here
            if (lib != null) {
                addLibraryToProject(project, lib);
            }
        }

        String version = (lib != null && libIsAdded) ? PersistenceUtils.getJPAVersion(lib) : PersistenceUtils.getJPAVersion(project);//use library if possible it will provide better result, TODO: may be usage of project should be removed and use 1.0 is no library was found
        if (provider != null && version != null) {
            String provVersion = ProviderUtil.getVersion(provider);
            if (provVersion != null) {
                //even if project support jpa 2.0 etc, but selected provider is reported as jpa1.0 use jpa1.0
                if (Double.parseDouble(version) > Double.parseDouble(provVersion)) {
                    version = provVersion;
                }
            }
        }
        PersistenceUnit punit = null;
        if (Persistence.VERSION_2_0.equals(version)) {
            punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
        } else//currently default 1.0
        {
            punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        }
        if (isContainerManaged) {
            if (preselectedDB == null || preselectedDB.trim().equals("")) {
                //find first with default/sample part in name
                JPADataSourceProvider dsProvider = project.getLookup().lookup(JPADataSourceProvider.class);
                if (dsProvider.getDataSources().size() > 0) {
                    preselectedDB = dsProvider.getDataSources().get(0).getDisplayName();
                }
            }
            if (preselectedDB != null && !"".equals(preselectedDB.trim())) {
                punit.setJtaDataSource(preselectedDB);
            }
            if (!(provider instanceof DefaultProvider)) {
                punit.setProvider(provider.getProviderClass());
            }
        } else {
            DatabaseConnection connection = null;
            if (preselectedDB != null && !preselectedDB.trim().equals("")) {
                connection = ConnectionManager.getDefault().getConnection(preselectedDB);
            }
            if (connection == null) {
                ConnectionManager cm = ConnectionManager.getDefault();
                DatabaseConnection[] connections = cm.getConnections();
                connection = connections != null && connections.length > 0 ? connections[0] : null;
            }
            punit = ProviderUtil.buildPersistenceUnit("tmp", provider, connection, version);
            punit.setTransactionType("RESOURCE_LOCAL"); //NOI18N
            // Explicitly add <exclude-unlisted-classes>false</exclude-unlisted-classes>
            // See issue 142575 - desc 10, and issue 180810
            if (!Util.isJavaSE(project)) {
                punit.setExcludeUnlistedClasses(false);
            }
        }
        if (puName == null) {
            puName = getCandidateName(project);
        }
        punit.setName(puName);
        ProviderUtil.setTableGeneration(punit, tableGeneration != null ? tableGeneration.name() : TableGeneration.NONE.name(), project);
        return punit;
    }

    /**
     *
     * @return true if jpa version is supported by platform (may need to be extended to classpath check and possible library addition check but not in this method
     * it's used to check server support in current realization)
     */
    public static boolean isJPAVersionSupported(Project project, String version){
        JPAModuleInfo info = project.getLookup().lookup(JPAModuleInfo.class);
        if(info!=null){
            return !Boolean.FALSE.equals(info.isJPAVersionSupported(version));//null return considerd valid too and the same as true
        }
        return true;

    }

    /**
     * Creates a persistence unit using the PU wizard and adds the created
     * persistence unit to the given project's <code>PUDataObject</code> and saves it.
     *
     * @param project the project to which the created persistence unit is to be created.
     * @param preselectedDB the name of the db connection that should be preselected, or null if none needs
     * to be preselected.
     * @param tableGeneration the table generation strategy for the persistence unit.
     *
     * @return true if the creation of the persistence unit was successful, false otherwise.
     *
     * @throws InvalidPersistenceXmlException if the persistence.xml file in the given
     * project is not valid.
     *
     */
    public static boolean createPersistenceUnitUsingWizard(Project project,
            String preselectedDB, TableGeneration tableGeneration) throws InvalidPersistenceXmlException {

        PersistenceUnit punit = buildPersistenceUnitUsingWizard(project, preselectedDB, tableGeneration);
        if (punit == null) {
            return false;
        }
        String version = punit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit ? Persistence.VERSION_2_0 : Persistence.VERSION_1_0;
        PUDataObject pud = ProviderUtil.getPUDataObject(project, version);
        if (pud == null) {
            return false;
        }
        pud.addPersistenceUnit(punit);
        pud.save();
        return true;
    }

    /**
     * add pu to the project, add persistence libraries if appropriate and known
     * add db libraries for connection if it's not conteiner managed project
     * @param project
     * @param pu
     */
    public static void addPersistenceUnitToProject(Project project, PersistenceUnit persistenceUnit) {
        String providerClass = persistenceUnit.getProvider();
        boolean libAdded = false;
        Library lib = null;
        if (providerClass != null) {
            Provider selectedProvider = ProviderUtil.getProvider(providerClass, project);
            lib = PersistenceLibrarySupport.getLibrary(selectedProvider);
            if (lib != null) {
                if (!Util.isDefaultProvider(project, selectedProvider)) {
                    Util.addLibraryToProject(project, lib);
                    libAdded = true;
                } else if (selectedProvider.getAnnotationProcessor() != null) {
                    Util.addLibraryToProject(project, lib, JavaClassPathConstants.PROCESSOR_PATH);
                    libAdded = true;
                }
            }
            if (!isContainerManaged(project)) {
                DatabaseConnection connection = ProviderUtil.getConnection(persistenceUnit);
                if (connection != null) {
                    JDBCDriver[] driver = JDBCDriverManager.getDefault().getDrivers(connection.getDriverClass());
                    PersistenceLibrarySupport.addDriver(project, driver[0]);
                } else {
                    Logger.getLogger("global").log(Level.INFO, "Can't find connection for persistence unit"); //NOI18N
                }
            }
        }

        try {
            ProviderUtil.addPersistenceUnit(persistenceUnit, project);
        } catch (InvalidPersistenceXmlException ipx) {
            // just log for debugging purposes, at this point the user has
            // already been warned about an invalid persistence.xml
            Logger.getLogger(RelatedCMPWizard.class.getName()).log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NOI18N
        }
        //need to add ap registration lib if exist
        if (libAdded && lib != null) {
            double version = Math.max(Double.parseDouble(PersistenceUtils.getJPAVersion(lib)), Double.parseDouble(PersistenceUtils.getJPAVersion(project)));
            if (version > 1.0) {
                Library mLib = LibraryManager.getDefault().getLibrary(lib.getName()+"modelgen");
                if(mLib!=null) Util.addLibraryToProject(project, mLib, JavaClassPathConstants.PROCESSOR_PATH);//no real need to add modelgen to compile classpath
            }
        }
    }

    /**
     * Creates a persistence unit with the default table generation strategy using the PU wizard and adds the created
     * persistence unit to the given project's <code>PUDataObject</code> and saves it.
     *
     * @param project the project to which the created persistence unit is to be created.
     * @param preselectedDB the name of the db connection that should be preselected, or null if none needs
     * to be preselected.
     *
     * @return true if the creation of the persistence unit was successful, false otherwise.
     *
     * @throws InvalidPersistenceXmlException if the persistence.xml file in the given
     * project is not valid.
     *
     */
    public static boolean createPersistenceUnitUsingWizard(Project project, String preselectedDB) throws InvalidPersistenceXmlException {
        return createPersistenceUnitUsingWizard(project, preselectedDB, TableGeneration.CREATE);
    }

    /**
     * Ad library to the project to compile classpath(default)
     * @param project
     * @param library
     */
    public static void addLibraryToProject(Project project, Library library) {
        addLibraryToProject(project, library, ClassPath.COMPILE);
    }

    /**
     * add library to the project to specified classpath
     * @param project
     * @param library
     * @param classpathType
     */
    public static void addLibraryToProject(Project project, Library library, String classpathType) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup groups[] = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        SourceGroup firstGroup = groups[0];
        FileObject fo = firstGroup.getRootFolder();
        try {
            ProjectClassPathModifier.addLibraries(new Library[]{library}, fo, classpathType);
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.FINE, "Can't add library to the project", ex);
        } catch (UnsupportedOperationException ex) {
            Logger.getLogger("global").log(Level.FINE, "Can't add library to the project", ex);
        }
    }

    /**
     * Adds persistence providers found from libraries to the given model.
     */
    static private void addProvidersFromLibraries(List<Provider> model) {
        for (Provider each : PersistenceLibrarySupport.getProvidersFromLibraries()) {
            boolean found = false;
            for (int i = 0; i < model.size(); i++) {
                Object elem = model.get(i);
                if (elem instanceof Provider && each.equals(elem)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                model.add(each);
            }
        }
    }

    /**
     *@return an initial name for a persistence unit, i.e. a name that
     * is unique.
     */
    static private final String getCandidateName(Project project) {
        String candidateNameBase = ProjectUtils.getInformation(project).getName() + "PU"; //NOI18N
        try {
            if (!ProviderUtil.persistenceExists(project)) {
                return candidateNameBase;
            }
            PUDataObject pudo = ProviderUtil.getPUDataObject(project);
            Persistence persistence = pudo.getPersistence();

            int suffix = 2;
            PersistenceUnit[] punits = persistence.getPersistenceUnit();
            String candidateName = candidateNameBase;
            while (!isUnique(candidateName, punits)) {
                candidateName = candidateNameBase + suffix++;
            }
            return candidateName;
        } catch (InvalidPersistenceXmlException ipex) {
            // just log, the user is notified about invalid persistence.xml when
            // the panel is validated
            Logger.getLogger("global").log(Level.FINE, "Invalid persistence.xml found", ipex); //NOI18N
        }
        return candidateNameBase;
    }

    /**
     * @return true if the given <code>candidate</code> represents a unique
     * name within the names of the given <code>punits</code>, false otherwise.
     */
    static private boolean isUnique(String candidate, PersistenceUnit[] punits) {
        for (PersistenceUnit punit : punits) {
            if (candidate.equals(punit.getName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * An implementation of the PersistenceProviderSupplier that returns an empty list for supported
     * providers and doesn't support a default provider. Used when an implementation of
     * the PersistenceProviderSupplier can't be found in the project lookup (as is the case
     * for instance for Java SE projects).
     */
    private static class DefaultPersistenceProviderSupplier implements PersistenceProviderSupplier {

        @Override
        public List<Provider> getSupportedProviders() {
            return Collections.<Provider>emptyList();
        }

        @Override
        public boolean supportsDefaultProvider() {
            return false;
        }
    }

    public static String getPersistenceUnitAsString(Project project, String entity) throws IOException {
        String persistenceUnit = null;
        PersistenceScope persistenceScopes[] = PersistenceUtils.getPersistenceScopes(project);
        if (persistenceScopes.length > 0) {
            FileObject persXml = persistenceScopes[0].getPersistenceXml();
            if (persXml != null) {
                Persistence persistence = PersistenceMetadata.getDefault().getRoot(persXml);
                PersistenceUnit units[] = persistence.getPersistenceUnit();
                if (units.length > 0) {
                    persistenceUnit = units[0].getName();
                    if (units.length > 1) {//find best
                        String forAll = null;
                        String forOne = null;
                        for (int i = 0; i < units.length && forOne == null; i++) {
                            PersistenceUnit tmp = units[i];
                            if (forAll == null && !tmp.isExcludeUnlistedClasses()) {
                                forAll = tmp.getName();//first match sutable for all entities in the project
                            }
                            if (tmp.isExcludeUnlistedClasses()) {
                                String[] classes = tmp.getClass2();
                                for (String clas : classes) {
                                    if (entity.equals(clas)) {
                                        forOne = tmp.getName();
                                        break;
                                    }
                                }
                            }
                        }
                        //try again with less restrictions (i.e. for j2se even without exclude-unlisted-classes node, it's by default true)
                        if (forOne == null && forAll != null) {//there is exist pu without exclude-unlisted-classes
                            for (int i = 0; i < units.length && forOne == null; i++) {
                                PersistenceUnit tmp = units[i];
                                if (!tmp.isExcludeUnlistedClasses()) {//verify only pu without exclude-unlisted-classes as all other was examined in previos try
                                    String[] classes = tmp.getClass2();
                                    for (String clas : classes) {
                                        if (entity.equals(clas)) {
                                            forOne = tmp.getName();
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        persistenceUnit = forOne != null ? forOne : (forAll != null ? forAll : persistenceUnit);
                    }
                }
            }
        }
        return persistenceUnit;
    }

    //UI support
    public static Set getSelectedItems(JList list, boolean enabledOnly) {
        Set result = new HashSet();

        int[] selected = list.getSelectedIndices();
        for (int i = 0; i < selected.length; i++) {
            Object item = list.getModel().getElementAt(selected[i]);
            if (enabledOnly) {
                if (!list.getCellRenderer().getListCellRendererComponent(list, item, selected[i], false, false).isEnabled()) {
                    continue;
                }
            }
            result.add(item);
        }

        return result;
    }

    public static Set getEnabledItems(JList list) {
        Set result = new HashSet();
        for (int i = 0; i < list.getModel().getSize(); i++) {
            Object item = list.getModel().getElementAt(i);
            if (!list.getCellRenderer().getListCellRendererComponent(list, item, i, false, false).isEnabled()) {
                continue;
            }
            result.add(item);
        }

        return result;
    }
}
