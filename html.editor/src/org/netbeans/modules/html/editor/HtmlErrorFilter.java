/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.editor;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.parser.api.SyntaxAnalyzerResult;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.HintsProvider.HintsManager;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ErrorFilter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.hints.HtmlHintsProvider;
import org.netbeans.modules.web.common.api.WebPageMetadata;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
public class HtmlErrorFilter implements ErrorFilter {

    public static final String DISABLE_ERROR_CHECKS_KEY = "disable_error_checking"; //NOI18N
    
    private static final ErrorFilter INSTANCE = new HtmlErrorFilter();
    private HintsProvider htmlHintsProvider;
    private HintsManager htmlHintsManager;

    public HtmlErrorFilter() {
        htmlHintsProvider = new HtmlHintsProvider();
        htmlHintsManager = HintsProvider.HintsManager.getManagerForMimeType(HtmlKit.HTML_MIME_TYPE);
    }
    
    @Override
    public List<? extends Error> filter(ParserResult parserResult) {
        if(!(parserResult instanceof HtmlParserResult)) {
            return null; //not ours
        }
        
        //use hints setting to filter out the errors and set their severity 
        RuleContext context = htmlHintsProvider.createRuleContext();
        context.parserResult = parserResult;
        context.manager = htmlHintsManager;
        context.doc = (BaseDocument)parserResult.getSnapshot().getSource().getDocument(false); //should not load the document if not loaded already
        List<Hint> hints = new ArrayList<Hint>();
        htmlHintsProvider.computeErrors(htmlHintsManager, context, hints, new ArrayList<Error>());
        
        List<Error> filtered = new ArrayList<Error>(hints.size());
        for(Hint h : hints) {
            //TODO fix the severity somehow - now it seems there's no away how to get 
            //the severity set to a particular hint by the hint options.
            
//            //use the severity defined in the hints settings
//            HintSeverity hseverity = HintsSettings.getSeverity((GsfHintsManager)htmlHintsManager, (UserConfigurableRule)h);

            DefaultError e = new DefaultError("error", //NOI18N
                    h.getDescription(), 
                    h.getDescription(), 
                    h.getFile(),
                    h.getRange().getStart(), 
                    h.getRange().getEnd(), 
                    Severity.WARNING);
            
            filtered.add(e);
        }
        
        return filtered;
    }
    
    public static boolean isErrorCheckingEnabled(SyntaxAnalyzerResult result) {
        return !isErrorCheckingDisabledForFile(result) && isErrorCheckingEnabledForMimetype(result);
    }

    public static boolean isErrorCheckingDisabledForFile(SyntaxAnalyzerResult result) {
        FileObject fo = result.getSource().getSourceFileObject();
        return fo != null && fo.getAttribute(DISABLE_ERROR_CHECKS_KEY) != null;
    }

    public static boolean isErrorCheckingEnabledForMimetype(SyntaxAnalyzerResult result) {
        return HtmlPreferences.isHtmlErrorCheckingEnabledForMimetype(getWebPageMimeType(result));
    }
    
    //and now the magic...
    //the method returns an artificial mimetype so the user can enable/disable the error checks
    //for particular content. For example the text/facelets+xhtml mimetype is returned for
    //.xhtml pages with facelets content. This allows to normally verify the plain xhtml file
    //even if their mimetype is text/html
    //sure the correct solution would be to let the mimeresolver to create different mimetype,
    //but since the resolution can be pretty complex it is not done this way
    public static String getWebPageMimeType(SyntaxAnalyzerResult result) {
        InstanceContent ic = new InstanceContent();
        ic.add(result);
        WebPageMetadata wpmeta = WebPageMetadata.getMetadata(new AbstractLookup(ic));

        if (wpmeta != null) {
            //get an artificial mimetype for the web page, this doesn't have to be equal
            //to the fileObjects mimetype.
            String mimeType = (String) wpmeta.value(WebPageMetadata.MIMETYPE);
            if (mimeType != null) {
                return mimeType;
            }
        }

        FileObject fo = result.getSource().getSourceFileObject();
        if(fo != null) {
            return fo.getMIMEType();
        } else {
            //no fileobject?
            return result.getSource().getSnapshot().getMimeType();
        }

    }
    
    @ServiceProvider(service=ErrorFilter.Factory.class)
    public static class Factory implements ErrorFilter.Factory {

        @Override
        public ErrorFilter createErrorFilter(String featureName) {
            return ErrorFilter.FEATURE_TASKLIST.equals(featureName) ? INSTANCE : null;
        }
        
    }
    
}
