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

package org.netbeans.modules.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Common Controller for all options categories composed by subpanels
 *
 * @author Max Sauer, Jiri Skrivanek
 */
public class TabbedController extends OptionsPanelController {

    private static final Logger LOGGER = Logger.getLogger(TabbedController.class.getName());
    private String tabFolder;
    private Lookup.Result<AdvancedOption> options;
    private Map<String, String> id2tabTitle;
    private Map<String, OptionsPanelController> tabTitle2controller;
    private Map<String, AdvancedOption> tabTitle2Option;
    private Lookup masterLookup;
    private final LookupListener lookupListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            readPanels();
            Mutex.EVENT.readAccess(new Runnable() {

                public void run() {
                    initTabbedPane();
                }
            });
        }
    };

    /** pane with sub-panels */
    private JTabbedPane pane;
    /** PropertyChangeSupport and listener to fire changes when switching tabs. */
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final ChangeListener tabbedPaneChangeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            handleTabSwitched();
        }
    };

    /**
     * Creates new instance
     * @param tabFolder layer folder where subpanels (AdvancedOption instances) reside
     */
    public TabbedController(String tabFolder) {
        this.tabFolder = tabFolder;
        readPanels();
        options.addLookupListener(WeakListeners.create(LookupListener.class, lookupListener, options));
    }

    @Override
    public void update() {
        for (OptionsPanelController c : getControllers()) {
            c.update();
        }
    }

    @Override
    public void applyChanges() {
        for (OptionsPanelController c : getControllers()) {
            c.applyChanges();
        }
    }

    @Override
    public void cancel() {
        for (OptionsPanelController c : getControllers()) {
            c.cancel();
        }
    }

    @Override
    public boolean isValid() {
        for (OptionsPanelController c : getControllers()) {
            // if changed (#145569) and not valid
            if (c.isChanged() && !c.isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isChanged() {
        for (OptionsPanelController c : getControllers()) {
            if (c.isChanged()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (pane == null) {
            pane = new JTabbedPane();
            pane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TabbedController.class, "TabbedController.pane.AD"));
            this.masterLookup = masterLookup;
            initTabbedPane();
        }
        return pane;
    }

    private void initTabbedPane() {
        pane.removeChangeListener(tabbedPaneChangeListener);
        pane.removeAll();
        for (String tabTitle : tabTitle2Option.keySet()) {
            pane.addTab(tabTitle, new JLabel(tabTitle));
        }
        pane.addChangeListener(tabbedPaneChangeListener);
        handleTabSwitched();
    }


    /** Replace placeholder with real panel and change help context. */
    private void handleTabSwitched() {
        final int selectedIndex = pane.getSelectedIndex();
        if (selectedIndex != -1) {
            String tabTitle = pane.getTitleAt(selectedIndex);
            OptionsPanelController controller = tabTitle2controller.get(tabTitle);
            if (pane.getSelectedComponent() instanceof JLabel) {
                JComponent comp;
                if (controller == null) {
                    controller = tabTitle2Option.get(tabTitle).create();
                    tabTitle2controller.put(tabTitle, controller);
                    // must be here because many controllers rely on fact that getComponent() is called first than other methods
                    comp = controller.getComponent(masterLookup);
                    // add existing listeners
                    for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
                        controller.addPropertyChangeListener(pcl);
                    }
                } else {
                    comp = controller.getComponent(masterLookup);
                }
                pane.setComponentAt(selectedIndex, new JScrollPane(comp));
                ((JComponent) pane.getSelectedComponent()).setBorder(new EmptyBorder(11, 11, 11, 11));
                controller.update();
            }
            pcs.firePropertyChange(OptionsPanelController.PROP_HELP_CTX, null, null);
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        if (pane != null && pane.getSelectedIndex() != -1) {
            return getHelpCtx(pane.getTitleAt(pane.getSelectedIndex()));
        }
        return null;
    }

    private HelpCtx getHelpCtx(String tabTitle) {
        OptionsPanelController controller = tabTitle2controller.get(tabTitle);
        if (controller != null) {
            return controller.getHelpCtx();
        }
        return new HelpCtx("netbeans.optionsDialog.java");  //NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
        for (OptionsPanelController c : getControllers()) {
            c.addPropertyChangeListener(l);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
        for (OptionsPanelController c : getControllers()) {
            c.removePropertyChangeListener(l);
        }
    }

    @Override
    protected void setCurrentSubcategory(String path) {
        String subcategoryID = path.indexOf('/') == -1 ? path : path.substring(0, path.indexOf('/'));
        final String subcategorySubpath = path.indexOf('/') == -1 ? null : path.substring(path.indexOf('/')+1);
        LOGGER.fine("Set current subcategory: "+path); // NOI18N
        if(!id2tabTitle.containsKey(subcategoryID)) {
            LOGGER.warning("Subcategory "+subcategoryID+" not found.");  //NOI18N
            return;
        }
        // use tab titles because there are still might be placeholders instead of real components
        String newTabTitle = id2tabTitle.get(subcategoryID);
        String currentTabTitle = pane.getSelectedIndex() != -1 ? pane.getTitleAt(pane.getSelectedIndex()) : null;
        if (!newTabTitle.equals(currentTabTitle)) {
            for (int i = 0; i < pane.getTabCount(); i++) {
                if (pane.getTitleAt(i).equals(newTabTitle)) {
                    pane.setSelectedIndex(i);
                    break;
                }
            }
        }
        if(subcategorySubpath != null) {
            OptionsPanelControllerAccessor.getDefault().setCurrentSubcategory(tabTitle2controller.get(newTabTitle), subcategorySubpath);
        }
    }

    @Override
    public Lookup getLookup() {
        List<Lookup> lookups = new ArrayList<Lookup>();
        for (OptionsPanelController controller : getControllers()) {
            Lookup lookup = controller.getLookup();
            if (lookup != Lookup.EMPTY) {
                lookups.add(lookup);
            }
        }
        if (lookups.isEmpty()) {
            return Lookup.EMPTY;
        } else {
            return new ProxyLookup(lookups.toArray(new Lookup[lookups.size()]));
        }
    }

    private Collection<OptionsPanelController> getControllers() {
        return tabTitle2controller.values();
    }

    private void readPanels() {
        Lookup lookup = Lookups.forPath(tabFolder);
        options = lookup.lookup(new Lookup.Template<AdvancedOption>( AdvancedOption.class ));
        tabTitle2controller = new HashMap<String, OptionsPanelController>();
        tabTitle2Option = new LinkedHashMap<String, AdvancedOption>();
        id2tabTitle = new HashMap<String, String>();
        for (Lookup.Item<AdvancedOption> item : options.allItems()) {
            AdvancedOption option = item.getInstance();
            String displayName = option.getDisplayName();
            if (displayName != null) {
                tabTitle2Option.put(displayName, option);
                String id = item.getId().substring(item.getId().lastIndexOf('/') + 1);  //NOI18N
                id2tabTitle.put(id, displayName);
            } else {
                assert false : "Display name not defined: " + item.toString();  //NOI18N
            }
        }
    }
}
