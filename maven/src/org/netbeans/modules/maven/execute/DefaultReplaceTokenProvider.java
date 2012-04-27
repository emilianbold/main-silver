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

package org.netbeans.modules.maven.execute;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.classpath.MavenSourcesImpl;
import org.netbeans.modules.maven.spi.actions.ActionConvertor;
import org.netbeans.modules.maven.spi.actions.ReplaceTokenProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service={ReplaceTokenProvider.class, ActionConvertor.class}, projectType="org-netbeans-modules-maven")
public class DefaultReplaceTokenProvider implements ReplaceTokenProvider, ActionConvertor {
    private static final String ARTIFACTID = "artifactId";//NOI18N
    private static final String CLASSPATHSCOPE = "classPathScope";//NOI18N
    private static final String GROUPID = "groupId";//NOI18N
    private final Project project;
    private static final String CLASSNAME = "className";//NOI18N
    private static final String CLASSNAME_EXT = "classNameWithExtension";//NOI18N
    private static final String PACK_CLASSNAME = "packageClassName";//NOI18N
    public static final String METHOD_NAME = "nb.single.run.methodName"; //NOI18N
    private static final String VARIABLE_PREFIX = "var."; //NOI18N
    // as defined in org.netbeans.modules.project.ant.VariablesModel

    public DefaultReplaceTokenProvider(Project prj) {
        project = prj;
    }
    /**
     * just gets the array of FOs from lookup.
     */
    protected static FileObject[] extractFileObjectsfromLookup(Lookup lookup) {
        List<FileObject> files = new ArrayList<FileObject>();
        for (DataObject d : lookup.lookupAll(DataObject.class)) {
            FileObject f = d.getPrimaryFile();
            files.add(f);
        }
        Collection<? extends SingleMethod> methods = lookup.lookupAll(SingleMethod.class);
        if (methods.size() == 1) {
            SingleMethod method = methods.iterator().next();
            files.add(method.getFile());
        }

        return files.toArray(new FileObject[files.size()]);
    }

    @Override public Map<String, String> createReplacements(String actionName, Lookup lookup) {
        FileObject[] fos = extractFileObjectsfromLookup(lookup);
        Tuple tuple = new Tuple(null, null);
        FileObject fo;
        HashMap<String, String> replaceMap = new HashMap<String, String>();
        //read global variables defined in the IDE
        Map<String, String> vars = readVariables();
        replaceMap.putAll(vars);

        NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
        replaceMap.put(GROUPID, prj.getMavenProject().getGroupId());
        replaceMap.put(ARTIFACTID, prj.getMavenProject().getArtifactId());

        if (fos.length > 0) {
            fo = fos[0];
            Sources srcs = ProjectUtils.getSources(project);
            if ("text/x-java".equals(fo.getMIMEType())) {//NOI18N
                tuple = checkSG(srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA), replaceMap, fo);
            }
            if (tuple.relPath == null) {
                replaceMap.put(CLASSNAME_EXT, "");//NOI18N
                replaceMap.put(CLASSNAME, "");//NOI18N
                replaceMap.put(PACK_CLASSNAME, "");//NOI18N
            }

        }

        Collection<? extends SingleMethod> methods = lookup.lookupAll(SingleMethod.class);
        if (methods.size() == 1) {
            //sort of hack to push the method name through the current apis..
            SingleMethod method = methods.iterator().next();
            replaceMap.put(METHOD_NAME, method.getMethodName());
        }

        if (tuple.group != null &&
                //TODO not nice, how to figure in a better way? by source classpath?
                (MavenSourcesImpl.NAME_TESTSOURCE.equals(tuple.group.getName()))) {
            replaceMap.put(CLASSPATHSCOPE,"test"); //NOI18N
        } else {
            replaceMap.put(CLASSPATHSCOPE,"runtime"); //NOI18N
        }
        if (tuple.group != null && MavenSourcesImpl.NAME_SOURCE.equals(tuple.group.getName()) &&
                (ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) ||
                ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName))) {
            String withExt = replaceMap.get(CLASSNAME_EXT);
            if (withExt != null && withExt.endsWith(".java")) {//NOI18N
                replaceMap.put(CLASSNAME_EXT, withExt.replace(".java", "Test.java"));//NOI18N
                replaceMap.put(CLASSNAME, replaceMap.get(CLASSNAME) + "Test");//NOI18N
                replaceMap.put(PACK_CLASSNAME, replaceMap.get(PACK_CLASSNAME) + "Test");//NOI18N
            }
        }
        return replaceMap;
    }

    private static Tuple checkSG(SourceGroup[] grp, HashMap<String, String> replaceMap, FileObject fo) {
        String relPath = null;
        SourceGroup group = null;
        for (int i = 0; i < grp.length; i++) {
            relPath = FileUtil.getRelativePath(grp[i].getRootFolder(), fo);
            if (relPath != null) {
                group = grp[i];
                replaceMap.put(CLASSNAME_EXT, fo.getNameExt());
                replaceMap.put(CLASSNAME, fo.getName());
                String pack = FileUtil.getRelativePath(grp[i].getRootFolder(), fo.getParent());
                if (pack != null) { //#141175
                    replaceMap.put(PACK_CLASSNAME, (pack + (pack.length() > 0 ? "." : "") + fo.getName()).replace('/', '.')); //NOI18N
                } else {
                    replaceMap.put(PACK_CLASSNAME, fo.getName());//NOI18N
                }
                break;
            }
        }
        return new Tuple(relPath, group);
    }

    private static class Tuple {
        final String relPath;
        final SourceGroup group;
        Tuple(String path, SourceGroup sg) {
            relPath = path;
            group = sg;
        }

    }

    public static Map<String, String> readVariables() {
        Map<String, String> vs = new HashMap<String, String>();
        EditableProperties ep = PropertyUtils.getGlobalProperties();
        for (Map.Entry<String, String> entry : ep.entrySet()) {
            if (entry.getKey().startsWith(VARIABLE_PREFIX)) {
                vs.put(entry.getKey().substring(VARIABLE_PREFIX.length()), FileUtil.normalizeFile(new File(entry.getValue())).getAbsolutePath());
            }
        }
        return vs;
    }


//    /*
//     * copied from ActionUtils and reworked so that it checks for mimeType of files, and DOES NOT include files with suffix 'suffix'
//     */
//    private static FileObject[] findSelectedFilesByMimeType(Lookup context, FileObject dir, String mimeType, String suffix, boolean strict) {
//        if (dir != null && !dir.isFolder()) {
//            throw new IllegalArgumentException("Not a folder: " + dir); // NOI18N
//        }
//
//        List<FileObject> files = new ArrayList<FileObject>();
//        for (DataObject d : context.lookupAll(DataObject.class)) {
//            FileObject f = d.getPrimaryFile();
//            boolean matches = FileUtil.toFile(f) != null;
//            if (dir != null) {
//                matches &= (FileUtil.isParentOf(dir, f) || dir == f);
//            }
//            if (mimeType != null) {
//                matches &= f.getMIMEType().equals(mimeType);
//            }
//            if (suffix != null) {
//                matches &= !f.getNameExt().endsWith(suffix);
//            }
//            // Generally only files from one project will make sense.
//            // Currently the action UI infrastructure (PlaceHolderAction)
//            // checks for that itself. Should there be another check here?
//            if (matches) {
//                files.add(f);
//            } else if (strict) {
//                return null;
//            }
//        }
//        if (files.isEmpty()) {
//            return null;
//        }
//        return files.toArray(
//                new FileObject[files.size()]);
//    }

    @Override public String convert(String action, Lookup lookup) {
        if (SingleMethod.COMMAND_DEBUG_SINGLE_METHOD.equals(action)) {
            return ActionProvider.COMMAND_DEBUG_TEST_SINGLE;
        }
        if (SingleMethod.COMMAND_RUN_SINGLE_METHOD.equals(action)) {
            return ActionProvider.COMMAND_TEST_SINGLE;
        }
        if (ActionProvider.COMMAND_RUN_SINGLE.equals(action) ||
            ActionProvider.COMMAND_DEBUG_SINGLE.equals(action) ||
            ActionProvider.COMMAND_PROFILE_SINGLE.equals(action)) {
            FileObject[] fos = extractFileObjectsfromLookup(lookup);
            if (fos.length > 0) {
                FileObject fo = fos[0];
                if ("text/x-java".equals(fo.getMIMEType())) {//NOI18N
                    Sources srcs = ProjectUtils.getSources(project);
                    SourceGroup[] grp = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    for (int i = 0; i < grp.length; i++) {
                        String relPath = FileUtil.getRelativePath(grp[i].getRootFolder(), fo);
                        if (relPath != null) {
                            if (SourceUtils.isMainClass(relPath.replaceFirst("[.]java$", "").replace('/', '.'), ClasspathInfo.create(fo), true)) {
                                return action + ".main";//NOI18N
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

}
