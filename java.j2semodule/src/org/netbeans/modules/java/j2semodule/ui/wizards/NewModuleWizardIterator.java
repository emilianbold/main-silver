/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.j2semodule.ui.wizards;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.java.j2semodule.J2SEModularProject;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class NewModuleWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {

    private final transient Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);    
    private transient WizardDescriptor.Panel panel;
    private transient WizardDescriptor wiz;

    @TemplateRegistration(folder = "J2SEModule", position = 1, content = "../resources/module-info.java.template", scriptEngine = "freemarker", displayName = "#moduleWizard", iconBase = JavaTemplates.JAVA_ICON, description = "../resources/module.html")
    @NbBundle.Messages("moduleWizard=Module")
    public static NewModuleWizardIterator moduleWizard() {
        return new NewModuleWizardIterator();
    }

    @Override
    public Set instantiate() throws IOException {
        FileObject dir = Templates.getTargetFolder(wiz);        
        String targetName = Templates.getTargetName(wiz);
        FileObject template = Templates.getTemplate(wiz);
        
        FileObject createdFolder = FileUtil.createFolder(dir, targetName);

        Project p = Templates.getProject(wiz);
        J2SEModularProject project = p != null ? p.getLookup().lookup(J2SEModularProject.class) : null;
        if (project != null) {
            for (String rootProp : project.getSourceRoots().getRootProperties()) {
                String rootPath = project.evaluator().getProperty(rootProp);
                int idx = rootPath.indexOf("/*/");
                if (idx >= 0) {
                    FileObject root = project.getAntProjectHelper().resolveFileObject(rootPath.substring(0, idx));
                    if (root == dir) {
                        String path = rootPath.substring(idx + 3);
                        if (!path.isEmpty()) {
                            createdFolder = createdFolder.createFolder(path);
                        }
                        break;
                    }
                }
            }
        }

        DataFolder df = DataFolder.findFolder(createdFolder);
        DataObject dTemplate = DataObject.find(template);                
        DataObject dobj = dTemplate.createFromTemplate(df, null, Collections.singletonMap("moduleName", targetName)); //NOI18N
        FileObject createdFile = dobj.getPrimaryFile();

        final Set<FileObject> res = new HashSet<>();
        res.add(createdFolder);
        res.add(createdFile);
        return Collections.unmodifiableSet(res);
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wiz = wizard;
        // Ask for Java folders
        Project project = Templates.getProject(wiz);
        if (project == null) {
            throw new NullPointerException ("No project found for: " + wiz);
        }
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_MODULES);
        assert groups != null : "Cannot return null from Sources.getSourceGroups: " + sources;
        if (groups.length == 0) {
            groups = sources.getSourceGroups(Sources.TYPE_GENERIC); 
        }
        panel = new ModuleTargetChooserPanel(project, groups);
        // Make sure list of steps is accurate.
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
        String[] beforeSteps = prop != null && prop instanceof String[] ? (String[])prop : new String[0];
        int diff = 0;
        if (beforeSteps.length > 0) {
            diff = ("...".equals (beforeSteps[beforeSteps.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] steps = new String[ (beforeSteps.length - diff) + 1];
        for (int i = 0; i < steps.length; i++) {
            if (i < (beforeSteps.length - diff)) {
                steps[i] = beforeSteps[i];
            } else {
                steps[i] = panel.getComponent ().getName ();
            }
        }
        Component c = panel.getComponent();
        if (c instanceof JComponent) { // assume Swing components
            JComponent jc = (JComponent)c;
            // Step #.
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(0));
            // Step name (actually the whole list for reference).
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        this.wiz = null;
        panel = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panel;
    }

    @Override
    public String name() {
        return ""; //NOI18N
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public void nextPanel() {
        throw new NoSuchElementException();
    }

    @Override
    public void previousPanel() {
        throw new NoSuchElementException();
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        ChangeListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }
}
