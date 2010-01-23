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
package org.netbeans.modules.web.beans.navigation.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError;
import org.netbeans.modules.web.beans.api.model.ModelUnit;
import org.netbeans.modules.web.beans.api.model.Result;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModelFactory;
import org.netbeans.modules.web.beans.navigation.InjectablesModel;
import org.netbeans.modules.web.beans.navigation.InjectablesPanel;
import org.netbeans.modules.web.beans.navigation.ResizablePopup;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import com.sun.source.util.TreePath;


/**
 * @author ads
 *
 */
public final class InspectInjectablesAtCaretAction extends BaseAction {

    private static final long serialVersionUID = 1857528107859448216L;
    
    private static final String INSPECT_INJACTABLES_AT_CARET =
        "inspect-injactables-at-caret";                     // NOI18N
    
    private static final String INSPECT_INJACTABLES_AT_CARET_POPUP =
        "inspect-injactables-at-caret-popup";               // NOI18N

    public InspectInjectablesAtCaretAction() {
        super(NbBundle.getMessage(InspectInjectablesAtCaretAction.class, 
                INSPECT_INJACTABLES_AT_CARET), 0);
        
        putValue(ACTION_COMMAND_KEY, INSPECT_INJACTABLES_AT_CARET);
        putValue(SHORT_DESCRIPTION, getValue(NAME));
        putValue(ExtKit.TRIMMED_TEXT,getValue(NAME));
        putValue(POPUP_MENU_TEXT, NbBundle.getMessage(
                InspectInjectablesAtCaretAction.class,
                INSPECT_INJACTABLES_AT_CARET_POPUP));

        putValue("noIconInMenu", Boolean.TRUE); // NOI18N*/
    }


    /* (non-Javadoc)
     * @see org.netbeans.editor.BaseAction#actionPerformed(java.awt.event.ActionEvent, javax.swing.text.JTextComponent)
     */
    @Override
    public void actionPerformed( ActionEvent event, final JTextComponent component ) {
        if ( component == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        final FileObject fileObject = NbEditorUtilities.getFileObject( 
                component.getDocument());
        if ( fileObject == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        Project project = FileOwnerQuery.getOwner( fileObject );
        if ( project == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        ClassPath boot = getClassPath( project , ClassPath.BOOT);
        ClassPath compile = getClassPath(project, ClassPath.COMPILE );
        ClassPath src = getClassPath(project , ClassPath.SOURCE);
        if ( boot == null || compile == null || src == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        ModelUnit modelUnit = ModelUnit.create( boot, compile , src);
        final MetadataModel<WebBeansModel> metaModel = WebBeansModelFactory.
            getMetaModel( modelUnit );
        
        /*
         *  this list will contain variable element name and TypeElement 
         *  qualified name which contains variable element. 
         */
        final Object[] variableAtCaret = new Object[2];
        if ( !getVariableElementAtDot( component, variableAtCaret )){
            return;
        }
        
        try {
            metaModel.runReadAction( new MetadataModelAction<WebBeansModel, Void>() {

                public Void run( WebBeansModel model ) throws Exception {
                    inspectInjectables(component, fileObject, 
                            model , metaModel, variableAtCaret );
                    return null;
                }
            });
        }
        catch (MetadataModelException e) {
            Logger.getLogger( InspectInjectablesAtCaretAction.class.getName()).
                log( Level.WARNING, e.getMessage(), e);
        }
        catch (IOException e) {
            Logger.getLogger( InspectInjectablesAtCaretAction.class.getName()).
                log( Level.WARNING, e.getMessage(), e);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.AbstractAction#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        if (EditorRegistry.lastFocusedComponent() == null
                || !EditorRegistry.lastFocusedComponent().isShowing())
        {
            return false;
        }
        if ( OpenProjects.getDefault().getOpenProjects().length == 0 ){
            return false;
        }
        final FileObject fileObject = NbEditorUtilities.getFileObject( 
                EditorRegistry.lastFocusedComponent().getDocument());
        if ( fileObject == null ){
            return false;
        }
        WebModule webModule = WebModule.getWebModule(fileObject);
        if ( webModule == null ){
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        return profile.equals(Profile.JAVA_EE_6_FULL) || 
            profile.equals(Profile.JAVA_EE_6_WEB);
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.editor.BaseAction#asynchonous()
     */
    @Override
    protected boolean asynchonous() {
        return true;
    }
    
    private ClassPath getClassPath( Project project, String type ) {
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
        SourceGroup[] webGroup = sources.getSourceGroups(
                WebProjectConstants.TYPE_WEB_INF);
        ClassPath[] paths = new ClassPath[ sourceGroups.length+webGroup.length];
        int i=0;
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[ i ] = provider.findClassPath( rootFolder, type);
            i++;
        }
        for (SourceGroup sourceGroup : webGroup) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[ i ] = provider.findClassPath( rootFolder, type);
            i++;
        }
        return ClassPathSupport.createProxyClassPath( paths );
    }

    /**
     * Variable element is resolved based on containing type element 
     * qualified name and simple name of variable itself.
     * Model methods are used further for injectable resolution.   
     */
    private void inspectInjectables( final JTextComponent component,
            final FileObject fileObject, final WebBeansModel model,
            final MetadataModel<WebBeansModel> metaModel,
            final Object[] variablePath )
    {
        VariableElement var = findVariable(model, variablePath);
        if (var == null) {
            return;
        }
        try {
            if (!model.isInjectionPoint(var)) {
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(GoToInjectableAtCaretAction.class,
                                "LBL_NotInjectionPoint"), // NOI18N
                        StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
                return;
            }
        }
        catch (InjectionPointDefinitionError e) {
            StatusDisplayer.getDefault().setStatusText(e.getMessage(),
                    StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        }
        final Result result = model.getInjectable(var, null);
        if (result == null) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(GoToInjectableAtCaretAction.class,
                            "LBL_InjectableNotFound"), // NOI18N
                    StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
            return;
        }
        if (result instanceof Result.Error) {
            StatusDisplayer.getDefault().setStatusText(
                    ((Result.Error) result).getMessage(),
                    StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        }
        if (result.getKind() == Result.ResultKind.DEFINITION_ERROR) {
            return;
        }
        final CompilationController controller = model
                .getCompilationController();
        final List<AnnotationMirror> bindings = model.getQualifiers(var);
        if (SwingUtilities.isEventDispatchThread()) {
            showDialog(result, bindings, controller, metaModel);
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showDialog(result, bindings, controller, metaModel);
                }
            });
        }
    }

    private VariableElement findVariable( final WebBeansModel model,
            final Object[] variablePath )
    {
        if ( variablePath[0] == null ){
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    InspectInjectablesAtCaretAction.class, 
                    "LBL_VariableNotFound", variablePath[1]));
            return null ;
        }
        Element element = ((ElementHandle<?>)variablePath[0]).resolve(
                model.getCompilationController());
        if ( element == null ){
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    InspectInjectablesAtCaretAction.class, 
                    "LBL_VariableNotFound", variablePath[1]));
            return null ;
        }
        VariableElement var = null;
        ExecutableElement method = null;
        if ( element.getKind() == ElementKind.FIELD){
            var = (VariableElement)element;
        }
        else {
            method = (ExecutableElement)element;
            List<? extends VariableElement> parameters = method.getParameters();
            for (VariableElement variableElement : parameters) {
                if (variableElement.getSimpleName().contentEquals(
                        variablePath[1].toString())) 
                {
                    var = variableElement;
                }
            }
        }
        
        if (var == null) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    InspectInjectablesAtCaretAction.class, 
                    "LBL_VariableNotFound", variablePath[1]));
        }
        return var;
    }

    /**
     * Compilation controller from metamodel could not be used for getting 
     * TreePath via dot because it is not based on one FileObject ( Document ).
     * So this method is required for searching Element at dot.
     * If appropriate element is found it's name is placed into list 
     * along with name of containing type.
     * Resulted element could not be used in metamodel for injectable
     * access. I believe this is because element was gotten via other Compilation
     * controller so it is from other model.
     * As result this trick is used.  
     */
    private boolean getVariableElementAtDot( final JTextComponent component,
            final Object[] variable ) 
    {
        JavaSource javaSource = JavaSource.forDocument(component.getDocument());
        if ( javaSource == null ){
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        try {
            javaSource.runUserActionTask(  new Task<CompilationController>(){
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase( Phase.ELEMENTS_RESOLVED );
                    int dot = component.getCaret().getDot();
                    TreePath tp = controller.getTreeUtilities()
                        .pathFor(dot);
                    Element element = controller.getTrees().getElement(tp );
                    if ( element == null ){
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(
                                InspectInjectablesAtCaretAction.class, 
                                "LBL_ElementNotFound"));
                        return;
                    }
                    if ( !( element instanceof VariableElement) ){
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(
                                InspectInjectablesAtCaretAction.class, 
                                "LBL_NotVariableElement"));
                        return;
                    }
                    else {
                        if ( element.getKind() == ElementKind.FIELD ){
                            ElementHandle<VariableElement> handle = 
                                ElementHandle.create((VariableElement)element);
                            variable[0] = handle;
                            variable[1] = element.getSimpleName().toString();
                        }
                        else {
                            setVariablePath(variable, controller, element);
                        }
                    }
                }
            }, true );
        }
        catch(IOException e ){
            Logger.getLogger( InspectInjectablesAtCaretAction.class.getName()).
                log( Level.WARNING, e.getMessage(), e);
        }
        return variable[1] !=null ;
    }
    
    private void showDialog( Result result , List<AnnotationMirror> bindings , 
            CompilationController controller, MetadataModel<WebBeansModel> model ) 
    {
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                InjectablesModel.class, "LBL_WaitNode"));
        JDialog dialog = ResizablePopup.getDialog();
        String title = NbBundle.getMessage(InspectInjectablesAtCaretAction.class,
                "TITLE_Injectables" , result.getVariable().getSimpleName().toString() );
        dialog.setTitle( title );
        dialog.setContentPane( new InjectablesPanel(result, bindings, 
                controller , model));
        dialog.setVisible( true );
    }


    private void setVariablePath( Object[] variableAtCaret,
            CompilationController controller, Element element )
    {
        Element parent = element.getEnclosingElement();
        if ( parent instanceof ExecutableElement ){
            ElementHandle<ExecutableElement> handle = ElementHandle.create( 
                    (ExecutableElement)parent ) ;
            variableAtCaret[0] = handle;
            variableAtCaret[1] = element.getSimpleName().toString();
        }
    }

}
