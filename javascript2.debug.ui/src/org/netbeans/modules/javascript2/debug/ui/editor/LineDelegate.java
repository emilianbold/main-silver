/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.debug.ui.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.List;
import org.netbeans.modules.javascript2.debug.EditorLineHandler;
import org.netbeans.modules.javascript2.debug.ui.TextLineHandler;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.WeakListeners;

/**
 *
 * @author Martin
 */
public class LineDelegate implements TextLineHandler {
    
    private Line line;
    private final PropertyChangeSupport pchs = new PropertyChangeSupport(this);
    private final LineChangesListener lineChangeslistener = new LineChangesListener();
    private PropertyChangeListener lineChangesWeak;
    
    public LineDelegate(Line line) {
        this.line = line;
        lineChangesWeak = WeakListeners.propertyChange(lineChangeslistener, line);
        line.addPropertyChangeListener(lineChangesWeak);
    }

    @Override
    public Line getLine() {
        return line;
    }
    
    @Override
    public FileObject getFileObject() {
        if (line instanceof FutureLine) {
            URL url = getURL();
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    LineCookie lineCookie = dobj.getLookup().lookup(LineCookie.class);
                    if (lineCookie == null) {
                        return null;
                    }
                    Line l = lineCookie.getLineSet().getCurrent(getLineNumber() - 1);
                    setLine(l);
                } catch (DataObjectNotFoundException ex) {
                }
            }
            return fo;
        } else {
            return line.getLookup().lookup(FileObject.class);
        }
    }

    @Override
    public URL getURL() {
        if (line instanceof FutureLine) {
            return ((FutureLine) line).getURL();
        }
        return line.getLookup().lookup(FileObject.class).toURL();
    }

    @Override
    public int getLineNumber() {
        return line.getLineNumber() + 1;
    }

    @Override
    public void setLineNumber(int lineNumber) {
        lineNumber--; // Line works with 0-based lines.
        if (line.getLineNumber() == lineNumber) {
            return ;
        }
        LineCookie lineCookie = line.getLookup().lookup(LineCookie.class);
        Line.Set lineSet = lineCookie.getLineSet();
        List<? extends Line> lines = lineSet.getLines();
        if (lines.size() > 0) {
            int lastLineNumber = lines.get(lines.size() - 1).getLineNumber();
            if (lineNumber > lastLineNumber) {
                lineNumber = lastLineNumber;
            }
        }
        Line cline;
        try {
            cline = lineSet.getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException ioobex) {
            cline = lineSet.getCurrent(0);
        }
        setLine(cline);
    }
    
    private void setLine(Line line) {
        dispose();
        int oldLineNumber = getLineNumber();
        this.line = line;
        lineChangesWeak = WeakListeners.propertyChange(lineChangeslistener, line);
        line.addPropertyChangeListener(lineChangesWeak);
        pchs.firePropertyChange(PROP_LINE_NUMBER, oldLineNumber, getLineNumber());
    }

    @Override
    public void dispose() {
        line.removePropertyChangeListener(lineChangesWeak);
        lineChangesWeak = null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pchl) {
        pchs.addPropertyChangeListener(pchl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pchl) {
        pchs.removePropertyChangeListener(pchl);
    }
    
    private class LineChangesListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (Line.PROP_LINE_NUMBER.equals(evt.getPropertyName())) {
                pchs.firePropertyChange(PROP_LINE_NUMBER, evt.getOldValue(), evt.getNewValue());
            }
        }
        
    }
    
}
