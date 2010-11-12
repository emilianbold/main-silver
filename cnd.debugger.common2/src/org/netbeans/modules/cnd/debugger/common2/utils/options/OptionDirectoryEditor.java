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

package org.netbeans.modules.cnd.debugger.common2.utils.options;

import java.beans.PropertyEditorSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.ExPropertyEditor;

class OptionDirectoryEditor extends PropertyEditorSupport
			    implements ExPropertyEditor {

    private OptionPropertySupport ops;
    private final String baseDir;
    private PropertyEnv env;
    private int dirOrFile;

    public OptionDirectoryEditor(OptionPropertySupport ops,
				 String baseDir, int dirOrFile) {
	this.ops = ops;
	this.baseDir = baseDir;
	this.dirOrFile = dirOrFile;
    }

    // interface PropertyEditor
    public void setAsText(String text) {
	setValue(text); // from PropertyEditorSupport
    }

    // interface PropertyEditor
    public String getAsText() {
	return (String) getValue();
    }

    // interface PropertyEditor
    public void setValue(Object v) {
	ops.setValue(v);
    }

    // interface PropertyEditor
    public Object getValue() {
	Object o = ops.getValue();
	return o;
    }

    // interface PropertyEditor
    public boolean supportsCustomEditor() {
	return true;
    }

    // interface PropertyEditor
    public java.awt.Component getCustomEditor() {
	return new DirectoryChooser(this, env, dirOrFile, baseDir, (String) ops.getValue());
    }

    // interface ExPropertyEditor
    public void attachEnv(PropertyEnv env) {
	this.env = env;
    }

    static class DirectoryChooser extends FileChooser
				  implements PropertyChangeListener {

	private final PropertyEditorSupport editor;
	private final String baseDir;


	private static String fullPath(String baseDir, String path) {
	    String seed = path;
	    if (seed.length() == 0)
		seed = ".";	// NOI18N
	    if (!CndPathUtilitities.isPathAbsolute(seed))
		seed = baseDir + File.separatorChar + seed;
	    return seed;
	}

	public DirectoryChooser(PropertyEditorSupport editor,
				PropertyEnv env, int dirOrFile,
				String baseDir, String path) {
	    super("Experiment Directory", // NOI18N
		  "Select", // NOI18N
		  dirOrFile, 
		  // FileChooser.DIRECTORIES_ONLY,
		  null,
		  fullPath(baseDir, path),
		  true);
	    this.editor = editor;
	    this.baseDir = baseDir;

	    setControlButtonsAreShown(false);

	    env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
	    env.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent evt) {
	    if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) &&
		evt.getNewValue() == PropertyEnv.STATE_VALID) {

		File file = getSelectedFile();
		if (file != null) {
		    String path = file.getPath();
		    path = CndPathUtilitities.toAbsolutePath(baseDir, path);
		    editor.setValue(path);
		}
	    }
	}
    }
}
