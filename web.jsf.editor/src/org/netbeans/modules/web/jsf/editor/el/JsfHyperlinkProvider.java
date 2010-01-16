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

package org.netbeans.modules.web.jsf.editor.el;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.editor.JSFEditorUtilities;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModelFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author Tomasz.Slota@Sun.COM
 * @author Petr Pisl
 */
public class JsfHyperlinkProvider implements HyperlinkProvider {

    /** Creates a new instance of JSFJSPHyperlinkProvider */
    public JsfHyperlinkProvider() {
    }

    /**
     * Should determine whether there should be a hyperlink on the given offset
     * in the given document. May be called any number of times for given parameters.
     * <br>
     * This method is called from event dispatch thread.
     * It should run very fast as it is called very often.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     * @return true if the provided offset should be in a hyperlink
     *         false otherwise
     */
    public boolean isHyperlinkPoint(final Document doc, final int offset) {
	final AtomicBoolean ret = new AtomicBoolean(false);
	doc.render(new Runnable() {

	    public void run() {
		TokenHierarchy<Document> tokenHierarchy = TokenHierarchy.get(doc);
		TokenSequence<?> tokenSequence = tokenHierarchy.tokenSequence();
		tokenSequence.move(offset);
		if (!tokenSequence.moveNext()) {
		    return; //no token
		}

		//check expression language
		TokenSequence<ELTokenId> elTokenSequence = tokenSequence.embedded(ELTokenId.language());
		if(elTokenSequence == null) {
		    return ;
		}
		
 		elTokenSequence.move(offset);
		if (!elTokenSequence.moveNext()) {
		    return; //no token
		}

		ret.set(elTokenSequence.token().id() == ELTokenId.IDENTIFIER);

	    }
	});

	return ret.get();
    }



    /**
     * Should determine the span of hyperlink on given offset. Generally, if
     * isHyperlinkPoint returns true for a given parameters, this class should
     * return a valid span, but it is not strictly required.
     * <br>
     * This method is called from event dispatch thread.
     * This method should run very fast as it is called very often.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     * @return a two member array which contains starting and ending offset of a hyperlink
     *         that should be on a given offset
     */
    public int[] getHyperlinkSpan(Document doc, int offset) {
        if (!(doc instanceof BaseDocument))
            return null;

        BaseDocument bdoc = (BaseDocument) doc;
        JTextComponent target = Utilities.getFocusedComponent();

        if (target == null || target.getDocument() != bdoc)
            return null;

        TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.get(bdoc);
        TokenSequence<?> tokenSequence = tokenHierarchy.tokenSequence();
        if(tokenSequence.move(offset) == Integer.MAX_VALUE) {
            return null; //no token found
        }
        if(!tokenSequence.moveNext()) {
            return null; //no token
        }

        //Token<?> token = tokenSequence.token();

        // is it a bean in EL ?
        TokenSequence<ELTokenId> elTokenSequence = tokenSequence.embedded(
                ELTokenId.language());

        if (elTokenSequence != null){
            FileObject fObject = NbEditorUtilities.getFileObject(doc);
            WebModule wm = WebModule.getWebModule(fObject);
            if (wm != null){
                JsfElExpression exp = new JsfElExpression(wm, doc);
                elTokenSequence.move(offset);
                if(!elTokenSequence.moveNext()) {
                    return null; //no token
                }

                int elEnd = elTokenSequence.offset() + elTokenSequence.token().length();

                int res = exp.parse(elEnd);
                if (res == JsfElExpression.EL_JSF_BEAN || res == JsfElExpression.EL_START
                        || res == JsfElExpression.EL_JSF_BEAN_REFERENCE)
                    return new int[] {elTokenSequence.offset(), elEnd};
            }
        }
        return null;
    }

    /**
     * The implementor should perform an action
     * corresponding to clicking on the hyperlink on the given offset. The
     * nature of the action is given by the nature of given hyperlink, but
     * generally should open some resource or move cursor
     * to certain place in the current document.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     */
    public void performClickAction(Document doc, int offset) {

        BaseDocument bdoc = (BaseDocument) doc;
        JTextComponent target = Utilities.getFocusedComponent();

        if (target == null || target.getDocument() != bdoc)
            return;

        TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.get(bdoc);
        TokenSequence<?> tokenSequence = tokenHierarchy.tokenSequence();
        if(tokenSequence.move(offset) == Integer.MAX_VALUE) {
            return; //no token found
        }
        if(!tokenSequence.moveNext()) {
            return ; //no token
        }

        //Token<?> token = tokenSequence.token();

        // is it a bean in EL
        TokenSequence<ELTokenId> elTokenSequence = tokenSequence.embedded(
                ELTokenId.language());
        if (elTokenSequence != null){
            FileObject fObject = NbEditorUtilities.getFileObject(doc);
            WebModule wm = WebModule.getWebModule(fObject);
            if (wm != null){
                JsfElExpression exp = new JsfElExpression(wm, bdoc);
                elTokenSequence.move(offset);
                if(!elTokenSequence.moveNext()) {
                    return; //no token
                }

                int res = exp.parse(elTokenSequence.offset() +
                            elTokenSequence.token().length());
                if (res == JsfElExpression.EL_START ){
                    //TODO XXX Add code to point references to beans in JSF file
                    (new OpenConfigFile(fObject, wm,
                            elTokenSequence.token().text().toString())).run();
                    return;
                }
                if (res == JsfElExpression.EL_JSF_BEAN ||
                        res == JsfElExpression.EL_JSF_BEAN_REFERENCE)
                {
                    if (!exp.gotoPropertyDeclaration(exp.getBaseObjectClass())){
                        String msg = NbBundle.getBundle(JsfHyperlinkProvider.class).
                            getString("MSG_source_not_found");
                        StatusDisplayer.getDefault().setStatusText(msg);
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        }
    }

    private static class OpenConfigFile implements Runnable {
        private String beanName;
        private WebModule wm;
        private FileObject mySource;

        OpenConfigFile(FileObject orig, WebModule wm, String beanName){
            this.beanName = beanName;
            this.wm = wm;
            mySource = orig;
        }

        public void run(){
            if (wm == null) return;

            FacesManagedBean bean = ConfigurationUtils.findFacesManagedBean(
                    wm, beanName);
            if ( bean == null ){
                return ;
            }
            FileObject config = null;
            if ( bean instanceof ManagedBean) {
                config = ((ManagedBean)bean).getModel().getModelSource().
                    getLookup().lookup(FileObject.class);
            }
            else {
                String fqn = bean.getManagedBeanClass();
                JavaSource javaSource = JavaSource.create( getClassPathInfo() );
                openElement( javaSource , fqn);
                return;
            }
            if (config != null) {
                try{
                    DataObject dobj = DataObject.find(config);
                    if (dobj != null) {
                        LineCookie lineCookie = dobj.getCookie(LineCookie.class);
                        EditorCookie editorCookie = dobj.getCookie(EditorCookie.class);
                        // EditCookie is needed, because we want to open the source editor.
                        // If we use OpenCookie, then the PageFlow editor will be displayed
                        EditCookie editCookie = dobj.getCookie(EditCookie.class);
                        if (editorCookie != null) {
                            StyledDocument document = editorCookie.openDocument();
                            int[] definition = JSFEditorUtilities.
                                getManagedBeanDefinition((BaseDocument)document,
                                        "managed-bean-name", beanName); //NOI18N
                            // line number in the document
                            int lineNumber = NbDocument.findLineNumber(document,
                                    definition[0]);
                            int lineOffset = NbDocument.findLineOffset(document,
                                    lineNumber);
                            // column at the line
                            int column = lineOffset - definition[0];

                            if (lineNumber != -1) {
                                Line line = lineCookie.getLineSet().getCurrent(
                                        lineNumber);

                                if(line != null) {
                                    // show the line
                                    line.show(ShowOpenType.OPEN,
                                            ShowVisibilityType.FRONT, column);
                                }
                            }

                            if (editCookie != null) {
                                // open the editor with source file
                                editCookie.edit();
                            }
                        }
                    }
                }
                catch (DataObjectNotFoundException exception) {
                    Exceptions.printStackTrace(exception);
                }
                catch (IOException exception) {
                    Exceptions.printStackTrace(exception);
                }
            }
        }

        private void openElement( final JavaSource javaSource, final String fqn){
            try {
                javaSource.runUserActionTask(
                        new Task<CompilationController>() {

                            public void run(
                                    CompilationController controller )
                                    throws Exception
                            {
                                controller.toPhase( Phase.ELEMENTS_RESOLVED );
                                TypeElement typeElement = controller
                                        .getElements().getTypeElement(fqn);
                                if (typeElement == null) {
                                    return;
                                }
                                ElementOpen.open(controller
                                        .getClasspathInfo(), typeElement);
                            }
                        }, true);
            }
            catch( IOException e ){
                Exceptions.printStackTrace(e);
            }
        }

        private ClasspathInfo getClassPathInfo(){
            //ClasspathInfo.create(mySource);
            Project project = FileOwnerQuery.getOwner(mySource);
            return ClasspathInfo.create( getClassPath(project, ClassPath.BOOT ),
                    getClassPath(project, ClassPath.COMPILE ),
                    getClassPath(project, ClassPath.SOURCE ));
        }

        private static ClassPath getClassPath( Project project, String type ) {
            ClassPathProvider provider = project.getLookup().lookup(
                    ClassPathProvider.class);
            if ( provider == null ){
                return null;
            }
            Sources sources = project.getLookup().lookup(Sources.class);
            if ( sources == null ){
                return null;
            }
            SourceGroup[] sourceGroups = sources.getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA );
            ClassPath[] paths = new ClassPath[ sourceGroups.length];
            int i=0;
            for (SourceGroup sourceGroup : sourceGroups) {
                FileObject rootFolder = sourceGroup.getRootFolder();
                paths[ i ] = provider.findClassPath( rootFolder, type);
            }
            return ClassPathSupport.createProxyClassPath( paths );
        }
    }
}
