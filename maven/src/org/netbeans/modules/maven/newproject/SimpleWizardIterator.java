/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.maven.newproject;

import java.awt.Component;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.validation.adapters.WizardDescriptorAdapter;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * mkleint: Should be more likely called Apisupport/Osgi wizard iterator.
 *@author Dafe Simonek
 */
public class SimpleWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {
    
    private int index;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private WizardDescriptor wiz;
    private final Archetype archetype;
    private boolean isNBM = false;
    private final List<ChangeListener> listeners;

    private SimpleWizardIterator(Archetype archetype) {
        this.archetype = archetype;
        isNBM = archetype.equals(ArchetypeWizardUtils.NB_APP_ARCH) ||
                archetype.equals(ArchetypeWizardUtils.NB_MODULE_ARCH);
        listeners = new ArrayList<ChangeListener>();
    }
    
    public static SimpleWizardIterator createNbModuleIterator() {
        return new SimpleWizardIterator(ArchetypeWizardUtils.NB_MODULE_ARCH);
    }

    public static SimpleWizardIterator createNbAppIterator() {
        return new SimpleWizardIterator(ArchetypeWizardUtils.NB_APP_ARCH);
    }

    public static SimpleWizardIterator createOSGiIterator() {
        return new SimpleWizardIterator(ArchetypeWizardUtils.OSGI_ARCH);
    }
    
    @SuppressWarnings("unchecked")
    private WizardDescriptor.Panel<WizardDescriptor>[] createPanels(ValidationGroup vg) {
        if (isNBM) {
            return new WizardDescriptor.Panel[] {
                new BasicWizardPanel(vg, true),
                new NbmWizardPanel(vg, archetype)
            };
        }
        return new WizardDescriptor.Panel[] {
            new BasicWizardPanel(vg, true)
        };
    }
    
    private String[] createSteps() {
        if (isNBM) {
            return new String[] {
                NbBundle.getMessage(SimpleWizardIterator.class, "LBL_CreateProjectStep2"),
                NbBundle.getMessage(SimpleWizardIterator.class, "LBL_CreateProjectStepNbm")
            };
        }
        return new String[] {
            NbBundle.getMessage(SimpleWizardIterator.class, "LBL_CreateProjectStep2")
        };
    }
    
    @Override
    public Set<FileObject> instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator."; //NOI18N
        return null;
    }
    
    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        return ArchetypeWizardUtils.instantiate(handle, wiz);
    }
    
    @Override
    public void initialize(WizardDescriptor wiz) {
        index = 0;
        ValidationGroup vg = ValidationGroup.create(new WizardDescriptorAdapter(wiz));

        panels = createPanels(vg);
        this.wiz = wiz;
        // set archetype to run
        this.wiz.putProperty(ChooseArchetypePanel.PROP_ARCHETYPE, archetype);

        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }
    }
    
    @Override
    public void uninitialize(WizardDescriptor wiz) {
        panels = null;
    }
    
    @Override
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(SimpleWizardIterator.class, "NameFormat"),
                new Object[] {new Integer(index + 1), new Integer(panels.length)});
    }
    
    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private void fireChange() {
        synchronized (listeners) {
            for (ChangeListener list : listeners) {
                list.stateChanged(new ChangeEvent(this));
            }
        }
    }
    
}
