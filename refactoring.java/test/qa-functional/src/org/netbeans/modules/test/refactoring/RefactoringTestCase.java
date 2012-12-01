/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 ** Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.test.refactoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.diff.LineDiff;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jiri.Prox@oracle.com, Marian.Mirilovic@oracle.com
 */
public abstract class RefactoringTestCase extends JellyTestCase {

    public static final char TREE_SEPARATOR = '|';
    protected static ProjectRootNode testProjectRootNode = null;
    private static ProjectsTabOperator pto = null;
    /**
     * The distance from the root of preview tree. Nodes located closer to the
     * root then this values will be sorted before dumping to ref file
     */
    public static int sortLevel = 2;
    protected final static String REFACTORING_TEST = "RefactoringTest";

    public RefactoringTestCase(String name) {
        super(name);
    }

    @Override
    public void ref(String text) {
        getRef().print(text);
    }

    public void ref(Object o) {
        getRef().println(o);
    }

    public void ref(File f) {
        getRef().println("==>" + f.getName());
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String s = br.readLine();
            while (s != null) {
                getRef().println(s);
                s = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException ex) {
            fail(ex);
        } catch (IOException ex) {
            fail(ex);
        }
    }

    public void ref(FileObject fo) {
        if (fo.isValid()) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(fo.getInputStream()));
                getRef().println("==>" + fo.getName());
                String s = br.readLine();
                while (s != null) {
                    getRef().println(s);
                    s = br.readLine();
                }
            } catch (IOException ioe) {
                fail(ioe);
            }
        }
    }

    /**
     * Dumps the tree structure into the ref file. The childs are sorted if they
     * are closer to root than {@link #sortLevel}
     *
     * @param model Model of the dumped tree
     * @param parent Current root whose childs are recursively dumped
     * @param level Distance current root - tree root
     */
    protected void browseChildren(TreeModel model, Object parent, int level) {
        Object invoke = getPreviewItemLabel(parent);
        for (int i = 0; i < level; i++) {
            ref("    ");
        }

        String logNode = invoke.toString();

        if (System.getProperty("os.name").contains("Linux")) { // synchronize colors of nodes on Linux 
            logNode = logNode.replaceAll("3c3c3c", "000000");
        }

        ref(logNode + "\n");

        int childs = model.getChildCount(parent);
        ArrayList<Object> al = new ArrayList<Object>(childs);  //storing childs for sorting        

        for (int i = 0; i < childs; i++) {
            Object child = model.getChild(parent, i);
            al.add(child);
        }
        if ((level + 1) <= sortLevel) {
            sortChilds(al);
        }

        while (!al.isEmpty()) {
            Object child = al.remove(0);
            browseChildren(model, child, level + 1);
        }

    }

    protected void browseRoot(JTree tree) {
        TreeModel model = tree.getModel();
        Object root = model.getRoot();
        browseChildren(model, root, 0);
    }

    /**
     * Opens file in editor.
     *
     * @param treeSubPackagePathToFile
     * @param fileName
     */
    protected void openFile(String treeSubPackagePathToFile, String fileName) {
        openProject(REFACTORING_TEST);
        StringTokenizer st = new StringTokenizer(treeSubPackagePathToFile, TREE_SEPARATOR + "");
        if (st.countTokens() > 1) {
            String token = st.nextToken();

            String fullpath = token;
            while (st.hasMoreTokens()) {
                token = st.nextToken();
                waitForChildNode(fullpath, token);
                fullpath += TREE_SEPARATOR + token;
            }
        }
        // last node
        waitForChildNode(treeSubPackagePathToFile, fileName);
        // end of fix of issue #51191

        Node node = new Node(testProjectRootNode, treeSubPackagePathToFile + TREE_SEPARATOR + fileName);
        //node.performPopupAction("Open");
        new OpenAction().performAPI(node);  //should be more stable then performing open action from popup
    }

    /**
     * Gets the file name form the selected path in the preview tree. Supposed
     * is that the name of file in the second element in the path
     *
     * @param tree Preview tree
     * @return File name related to selected node
     */
    public String getFileForSelectedNode(JTreeOperator tree) {
        TreePath selectionPath = tree.getSelectionPath();
        System.err.println("SP " + selectionPath.getPath());
        Object pathComponent = selectionPath.getPathComponent(2);
        System.err.println("PC " + pathComponent);
        return (String) getPreviewItemLabel(pathComponent);
    }

    /**
     * Gets string label of tree item. Suppose that the object has metho
     * {@code getLabel} which is called by reflection.
     *
     * @param parent The tree item
     * @return Test label obtained by method {@code getLabel}
     */
    protected Object getPreviewItemLabel(Object parent) {
        try {
            Method method = parent.getClass().getDeclaredMethod("getLabel");
            method.setAccessible(true);
            Object invoke = method.invoke(parent);
            return invoke;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        }
        fail("Error in reflection");
        return null;
    }

    private void sortChilds(List<Object> al) {
        final HashMap<Object, String> hashMap = new HashMap<Object, String>();
        for (Object object : al) {
            hashMap.put(object, (String) getPreviewItemLabel(object));
        }

        Collections.<Object>sort(al, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return hashMap.get(o1).compareTo(hashMap.get(o2));
            }
        });
    }

    private void waitForChildNode(String parentPath, String childName) {
        openProject(REFACTORING_TEST);
        Node parent = new Node(testProjectRootNode, parentPath);
        final String finalFileName = childName;
        try {
            // wait for max. 3 seconds for the file node to appear
            JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 3000);
            new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object parent) {
                    return ((Node) parent).isChildPresent(finalFileName) ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return ("Waiting for the tree to load.");
                }
            }).waitAction(parent);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }

    protected void openSourceFile(String dir, String srcName) {
        openFile(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir") + TREE_SEPARATOR + dir, srcName);
    }

    @Override
    protected void setUp() throws Exception {
        //jemmyOutput = new PrintStream(new File(getWorkDir(), getName() + ".jemmy"));
        System.setErr(new PrintStream(new File(getWorkDir(), getName() + ".error")));
        System.out.println("Test " + getName() + " started");
        openProject(REFACTORING_TEST);
    }

    protected void openProject(String projectName) {
        if (pto == null) {
            pto = ProjectsTabOperator.invoke();
        }

        if (testProjectRootNode == null) {
            try {
                openDataProjects("projects/" + projectName);
                testProjectRootNode = pto.getProjectRootNode(projectName);
                testProjectRootNode.select();
            } catch (IOException ex) {
                throw new JemmyException("Open project [" + projectName + "] fails !!!", ex);
            }
        } else {
            log("Project is opened!");
        }
    }

    @Override
    protected void tearDown() throws Exception {
        getRef().close();
        assertFile("Golden file differs ", new File(getWorkDir(), getName() + ".ref"), getGoldenFile(), getWorkDir(), new LineDiff());
        System.out.println("Test " + getName() + " finished");
    }
}
