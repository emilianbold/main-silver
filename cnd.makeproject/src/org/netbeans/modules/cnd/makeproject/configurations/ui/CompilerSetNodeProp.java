/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public class CompilerSetNodeProp extends Node.Property<String> {

    private CompilerSet2Configuration configuration;
    private final DevelopmentHostConfiguration hostConfiguration;
    private boolean canWrite;
    //private String txt1;
    private String txt2;
    private String txt3;
    private String oldname;

    public CompilerSetNodeProp(CompilerSet2Configuration configuration, DevelopmentHostConfiguration hostConf, boolean canWrite, String txt1, String txt2, String txt3) {
        super(String.class);
        this.configuration = configuration;
        this.hostConfiguration = hostConf;
        this.canWrite = canWrite;
        //this.txt1 = txt1;
        this.txt2 = txt2;
        this.txt3 = txt3;
        oldname = configuration.getOption();
        configuration.setCompilerSetNodeProp(CompilerSetNodeProp.this);
    }

    public String getOldname() {
        return oldname;
    }

    @Override
    public String getName() {
        return txt2;
    }

    @Override
    public String getShortDescription() {
        return txt3;
    }

    @Override
    public String getHtmlDisplayName() {
        if (configuration.getCompilerSetName().getModified()) {
            return configuration.isDevHostSetUp() ? "<b>" + getDisplayName() : getDisplayName(); // NOI18N
        } else {
            return null;
        }
    }

    @Override
    public String getValue() {
        return configuration.getCompilerSetName().getValue();
    }

    @Override
    public void setValue(String v) {
        configuration.setValue(v);
    }

    @Override
    public void restoreDefaultValue() {
        configuration.getCompilerSetName().reset();
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public boolean isDefaultValue() {
        return !configuration.getCompilerSetName().getModified();
    }

    @Override
    public boolean canWrite() {
        return canWrite;
    }

    @Override
    public boolean canRead() {
        return true;
    }

    public void repaint() {
        ((CompilerSetEditor) getPropertyEditor()).repaint();
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new CompilerSetEditor();
    }

    private class CompilerSetEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv env;
        @Override
        public String getJavaInitializationString() {
            return getAsText();
        }

        @Override
        public String getAsText() {
            String displayName = configuration.getDisplayName(true);
            return displayName;
        }

        @Override
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            super.setValue(text);
        }

        @Override
        public String[] getTags() {
            List<String> list = new ArrayList<String>();
            // TODO: this works unpredictable on switching development hosts
            // TODO: should be resolved later on
//            if (configuration.getCompilerSetManager().getCompilerSet(getOldname()) == null) {
//                list.add(getOldname());
//            }
            if (configuration.isDevHostSetUp()) {
                for(CompilerSet cs : configuration.getCompilerSetManager().getCompilerSets()) {
                    list.add(cs.getName());
                }
            }
            return list.toArray(new String[list.size()]);
        }

        public void repaint() {
            firePropertyChange();
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public Component getCustomEditor() {
            return new CompilerSetEditorCustomizer(env);
        }

        @Override
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }

    private final class CompilerSetEditorCustomizer extends JPanel {
        private final VetoableChangeListener delegate;
        public CompilerSetEditorCustomizer(PropertyEnv propertyEnv) {
            this.setLayout(new BorderLayout());
            this.setBorder(new EmptyBorder(6,6,0,6));
            AtomicReference<VetoableChangeListener> okListenerRef = new AtomicReference<VetoableChangeListener>();
            Component tpc = ToolsPanelSupport.getToolsPanelComonent(hostConfiguration.getExecutionEnvironment(), okListenerRef);
            delegate = okListenerRef.get();
            add(tpc, BorderLayout.CENTER);
            this.putClientProperty("title", NbBundle.getMessage(CompilerSetNodeProp.class, "CompilerSetEditorCustomizerTitile", hostConfiguration.getExecutionEnvironment().getDisplayName()));
            propertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            propertyEnv.addVetoableChangeListener(delegate);
        }
    }
}
