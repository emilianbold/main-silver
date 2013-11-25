/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.js.source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
public final class Source {
    
    public static final String URL_PROTOCOL = "js-scripts"; // NOI18N
    
    private static final String SOURCE_CLASS = "jdk.nashorn.internal.runtime.Source";   // NOI18N
    private static final String SOURCE_FIELD = "source";    // NOI18N
    
    private static final String SOURCE_VAR_NAME = "name";   // NOI18N
    private static final String SOURCE_VAR_CONTENT = "content"; // NOI18N
    private static final String SOURCE_VAR_HASH = "hash";   // NOI18N
    private static final String SOURCE_VAR_URL = "url";     // NOI18N
    
    private static final Map<JPDADebugger, Map<String, Source>> knownSources = new WeakHashMap<>();
    
    private final String name;
    private final URL url;
    private final int hash;
    private final String content;
    
    private Source(String name, URL url, int hash, String content) {
        this.name = name;
        if (url == null || !"file".equalsIgnoreCase(url.getProtocol())) {
            try {
                url = SourceFilesCache.getDefault().getSourceFile(name, hash, content);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        this.url = url;
        this.hash = hash;
        this.content = content;
    }
    
    public static Source getSource(CallStackFrame frame) {
        if (frame == null) {
            return null;
        }
        JPDAClassType classType;
        //classType = currentFrame.getClassType();
        try {
            classType = (JPDAClassType) frame.getClass().getMethod("getClassType").invoke(frame);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        return getSource(classType);
    }
    
    public static Source getSource(JPDAClassType classType) {
        String className = classType.getName();
        JPDADebugger debugger;
        try {
            java.lang.reflect.Field debuggerField = classType.getClass().getDeclaredField("debugger");
            debuggerField.setAccessible(true);
            debugger = (JPDADebugger) debuggerField.get(classType);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        synchronized (knownSources) {
            Map<String, Source> dbgSources = knownSources.get(debugger);
            if (dbgSources != null) {
                Source src = dbgSources.get(className);
                if (src != null) {
                    return src;
                }
            }
        }
        ObjectVariable sourceVar = getSourceVar(debugger, classType);
        if (sourceVar == null) {
            return null;
        }
        Field fieldName = sourceVar.getField(SOURCE_VAR_NAME);
        Field fieldContent = sourceVar.getField(SOURCE_VAR_CONTENT);
        Field fieldHash = sourceVar.getField(SOURCE_VAR_HASH);
        Field fieldURL = sourceVar.getField(SOURCE_VAR_URL);
        if (fieldName == null || fieldContent == null ||
            fieldHash == null || fieldURL == null) {
            
            return null;
        }
        Object urlObj = fieldURL.createMirrorObject();
        URL url;
        if (urlObj == null) {
            url = null;
        } else {
            url = (URL) urlObj;
        }
        Object hashMirror = fieldHash.createMirrorObject();
        if (!(hashMirror instanceof Integer)) {
            return null;
        }
        int hash = ((Integer) hashMirror).intValue();
        Object contentMirror = fieldContent.createMirrorObject();
        if (!(contentMirror instanceof char[])) {
            return null;
        }
        String content = new String((char[]) contentMirror);
        String name = fieldName.getValue();
        if (name.startsWith("\"") && name.endsWith("\"")) {
            name = name.substring(1, name.length() - 1);
        }
        if (!name.endsWith(".js") && !name.endsWith(".JS")) {
            name = name + ".js";
        }
        Source src = new Source(name, url, hash, content);
        synchronized (knownSources) {
            Map<String, Source> dbgSources = knownSources.get(debugger);
            if (dbgSources == null) {
                dbgSources = new HashMap<>();
                knownSources.put(debugger, dbgSources);
            }
            dbgSources.put(className, src);
        }
        return src;
    }
    
    private static ObjectVariable getSourceVar(JPDADebugger debugger, JPDAClassType classType) {
        List<Field> staticFields = classType.staticFields();
        for (Field sf : staticFields) {
            if (sf instanceof ObjectVariable &&
                SOURCE_FIELD.equals(sf.getName()) &&
                SOURCE_CLASS.equals(sf.getType())) {
                
                return (ObjectVariable) sf;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public int getHash() {
        return hash;
    }

    public String getContent() {
        return content;
    }
    
}