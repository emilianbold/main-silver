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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.api.model.CsmFile.FileType;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.api.model.services.CsmFileLanguageProvider;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import java.util.ListIterator;
import java.util.LinkedList;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmScope;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.openide.util.lookup.Lookups;
import static org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind.*;


/**
 * Misc. (static) utility functions
 * @author Vladimir Kvashin
 */
public class Utils {
    
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.modelimpl"); // NOI18N
    private static final int LOG_LEVEL = Integer.getInteger("org.netbeans.modules.cnd.modelimpl.level", -1).intValue(); // NOI18N
    
    static {
        // command line param has priority for logging
        // do not change it
        if (LOG_LEVEL == -1) {
            // command line param has priority for logging
            if (TraceFlags.DEBUG) {
                LOG.setLevel(Level.ALL);
            } else {
                LOG.setLevel(Level.SEVERE);
            }
        }
    }

    /**
     * Constructor is private to prevent instantiation.
     */
    private Utils() {
    }

    private final static class LangProviders {
        private final static Collection<? extends CsmFileLanguageProvider> langProviders = Lookups.forPath(CsmFileLanguageProvider.REGISTRATION_PATH).lookupAll(CsmFileLanguageProvider.class);
    }
    
    public static String getLanguage(CsmFile.FileType fileType, String path) {
        String lang = null;
        if (!LangProviders.langProviders.isEmpty()) {
            for (org.netbeans.modules.cnd.api.model.services.CsmFileLanguageProvider provider : LangProviders.langProviders) {
                lang = provider.getLanguage(fileType, path);
                if (lang != null) {
                    return lang;
                }
            }
        }
        if (fileType == FileType.SOURCE_CPP_FILE) {
            lang = APTLanguageSupport.GNU_CPP;
        } else if (fileType == FileType.SOURCE_C_FILE) {
            lang = APTLanguageSupport.GNU_C;
        } else if (fileType == FileType.SOURCE_FORTRAN_FILE) {
            lang = APTLanguageSupport.FORTRAN;
        } else {
            lang = APTLanguageSupport.GNU_CPP;
            if (path.length() > 2 && path.endsWith(".c")) { // NOI18N
                lang = APTLanguageSupport.GNU_C;
            }
        }
        return lang;
    }
    
    public static CsmOffsetable createOffsetable(CsmFile file, int startOffset, int endOffset) {
        return OffsetableBase.create(file, startOffset, endOffset);
    }
    
    public static String getQualifiedName(String name, CsmNamespace parent) {
        StringBuilder sb = new StringBuilder(name);
        if (parent != null) {
            if (!parent.isGlobal()) {
                sb.insert(0, "::"); // NOI18N
                sb.insert(0, parent.getQualifiedName());
            }
        }
        return sb.toString();
    }
      
    public static CharSequence[] splitQualifiedName(String qualified) {
        List<CharSequence> v = new ArrayList<CharSequence>();
        for (StringTokenizer t = new StringTokenizer(qualified, ": \t\n\r\f", false); t.hasMoreTokens(); ) {// NOI18N 
            v.add(NameCache.getManager().getString(t.nextToken()));
        }
        return v.toArray(new CharSequence[v.size()]);
    }   
    
    public static void disposeAll(Collection<? extends CsmObject> coll) {
        for (CsmObject elem : coll) {
            if( elem  instanceof Disposable ) {
                Disposable decl = (Disposable) elem;
                if (TraceFlags.TRACE_DISPOSE) {
                    System.err.println("disposing with UID " + ((CsmIdentifiable)elem).getUID()); // NOI18N
                }
                decl.dispose();
            } else {
                if (TraceFlags.TRACE_DISPOSE) {
                    System.err.println("non disposable with UID " + ((CsmIdentifiable)elem).getUID()); // NOI18N
                }
            }            
        }
    }

    public static void setSelfUID(CsmObject decl) {
        if (decl instanceof OffsetableIdentifiableBase) {
            ((OffsetableIdentifiableBase)decl).setSelfUID();
        } else {
            throw new IllegalArgumentException("unexpected object:" + decl);//NOI18N
        }
    }

    public static String getCsmIncludeKindKey() {
        // Returned string should be differed from getCsmDeclarationKindkey()
        return "I"; // NOI18N
    }

    public static String getCsmInheritanceKindKey(CsmInheritance obj) {
        switch (obj.getVisibility()) {
            case PRIVATE:
                return "h"; // NOI18N
            case PROTECTED:
                return "y"; // NOI18N
            case PUBLIC:
                return "H"; // NOI18N
            case NONE:
            default:
                return "Y"; // NOI18N
        }
        // Returned string should be differed from getCsmDeclarationKindkey()
    }

    public static CsmVisibility getCsmVisibility(char c) {
        switch (c) {
            case 'h':
                return CsmVisibility.PRIVATE;
            case 'y':
                return CsmVisibility.PROTECTED;
            case 'H':
                return CsmVisibility.PUBLIC;
            case 'Y':
            default:
                return CsmVisibility.NONE;
        }
    }

    public static String getCsmParamListKindKey() {
        // Returned string should be differed from getCsmDeclarationKindkey()
        return "P"; // NOI18N
    }

    public static String getCsmInstantiationKindKey() {
        // Returned string should be differed from getCsmDeclarationKindkey() and getCsmParamListKindKey()
        return "i"; // NOI18N
    }
    
    public static CharSequence[] getAllClassifiersUniqueNames(CharSequence uniqueName) {
        CharSequence namePostfix = uniqueName.subSequence(1, uniqueName.length());
        CharSequence out[] = new CharSequence[]
                                {
                                getCsmDeclarationKindkey(CsmDeclaration.Kind.CLASS) + namePostfix,
                                getCsmDeclarationKindkey(CsmDeclaration.Kind.STRUCT) + namePostfix,
                                getCsmDeclarationKindkey(CsmDeclaration.Kind.UNION) + namePostfix,
                                getCsmDeclarationKindkey(CsmDeclaration.Kind.ENUM) + namePostfix,
                                getCsmDeclarationKindkey(CsmDeclaration.Kind.TYPEDEF) + namePostfix,
                                getCsmDeclarationKindkey(CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION) + namePostfix
                                };
        return out;
    }

    public static String getCsmDeclarationKindkey(CsmDeclaration.Kind kind) {
        // Returned string should be differed from getCsmIncludeKindkey()
        switch (kind) {
            case ASM:
                return "A"; // NOI18N
            case BUILT_IN:
                return "B"; // NOI18N
            case CLASS:
                return "C"; // NOI18N
            case ENUM:
                return "E"; // NOI18N
            case FUNCTION:
                return "F"; // NOI18N
            case MACRO:
                return "M"; // NOI18N
            case NAMESPACE_DEFINITION:
                return "N"; // NOI18N
            case STRUCT:
                return "S"; // NOI18N
            case TEMPLATE_DECLARATION:
                return "T"; // NOI18N
            case UNION:
                return "U"; // NOI18N
            case VARIABLE:
                return "V"; // NOI18N
            case NAMESPACE_ALIAS:
                return "a"; // NOI18N
            case ENUMERATOR:
                return "e"; // NOI18N
            case FUNCTION_DEFINITION:
                return "f"; // NOI18N
            case FUNCTION_INSTANTIATION:
                return "j"; // NOI18N
            case USING_DIRECTIVE:
                return "g"; // NOI18N
            case TEMPLATE_PARAMETER:
                return "p"; // NOI18N
            case CLASS_FRIEND_DECLARATION:
                return "r"; // NOI18N
            case TEMPLATE_SPECIALIZATION:
                return "s"; // NOI18N
            case TYPEDEF:
                return "t"; // NOI18N
            case USING_DECLARATION:
                return "u"; // NOI18N
            case VARIABLE_DEFINITION:
                return "v"; // NOI18N
            case CLASS_FORWARD_DECLARATION:
                return "w"; // NOI18N
            case FUNCTION_FRIEND:
                return "D"; // NOI18N
            case FUNCTION_FRIEND_DEFINITION:
                return "d"; // NOI18N
            default:
                throw new IllegalArgumentException("Unexpected value of CsmDeclaration.Kind:" + kind); //NOI18N
        }
    }

    public static CsmDeclaration.Kind getCsmDeclarationKind(char kind) {
        switch (kind) {
            case 'A': // NOI18N
                return ASM;
            case 'B': // NOI18N
                return BUILT_IN;
            case 'C': // NOI18N
                return CLASS;
            case 'E': // NOI18N
                return ENUM;
            case 'F': // NOI18N
                return FUNCTION;
            case 'M': // NOI18N
                return MACRO;
            case 'N': // NOI18N
                return NAMESPACE_DEFINITION;
            case 'S': // NOI18N
                return STRUCT;
            case 'T': // NOI18N
                return TEMPLATE_DECLARATION;
            case 'U': // NOI18N
                return UNION;
            case 'V': // NOI18N
                return VARIABLE;
            case 'a': // NOI18N
                return NAMESPACE_ALIAS;
            case 'e': // NOI18N
                return ENUMERATOR;
            case 'f': // NOI18N
                return FUNCTION_DEFINITION;
            case 'j': // NOI18N
                return FUNCTION_INSTANTIATION;
            case 'g': // NOI18N
                return USING_DIRECTIVE;
            case 'p': // NOI18N
                return TEMPLATE_PARAMETER;
            case 'r': // NOI18N
                return CLASS_FRIEND_DECLARATION;
            case 's': // NOI18N
                return TEMPLATE_SPECIALIZATION;
            case 't': // NOI18N
                return TYPEDEF;
            case 'u': // NOI18N
                return USING_DECLARATION;
            case 'v': // NOI18N
                return VARIABLE_DEFINITION;
            case 'w': // NOI18N
                return CLASS_FORWARD_DECLARATION;
            case 'D': // NOI18N
                return FUNCTION_FRIEND;
            case 'd': // NOI18N
                return FUNCTION_FRIEND_DEFINITION;
            default:
                throw new IllegalArgumentException("Unexpected char for CsmDeclaration.Kind: " + kind); //NOI18N
        }
    }

    public static boolean canRegisterDeclaration(CsmDeclaration decl) {
        // WAS: don't put unnamed declarations
        assert decl != null;
        assert decl.getName() != null;
        if (decl.getName().length() == 0) {
            return false;
        }
        CsmScope scope = decl.getScope();
        if (scope instanceof CsmCompoundClassifier) {
            return canRegisterDeclaration((CsmCompoundClassifier) scope);
        }
        return true;
    }

    public static <T> LinkedList<T> reverse(LinkedList<T> original) {
        LinkedList<T> reverse = new LinkedList<T>();
        ListIterator<T> it = original.listIterator(original.size());
        while(it.hasPrevious()){
           reverse.addLast(it.previous());
        }
        return reverse;
    }

    public static NativeFileItem getCompiledFileItem(FileImpl fileImpl) {
        NativeFileItem out = null;
        ProjectBase filePrj = fileImpl.getProjectImpl(true);
        if (filePrj != null) {
            Collection<APTPreprocHandler.State> preprocStates = filePrj.getPreprocStates(fileImpl);
            if (preprocStates.isEmpty()) {
                return null;
            }
            // use start file from one of states (i.e. first)
            APTPreprocHandler.State state = preprocStates.iterator().next();
            FileImpl startFile = getStartFile(state);
            out = startFile != null ? startFile.getNativeFileItem() : null;
        }
        return out;
    }

    public static FileImpl getStartFile(final APTPreprocHandler.State state) {
        StartEntry startEntry = APTHandlersSupport.extractStartEntry(state);
        ProjectBase startProject = getStartProject(startEntry);
        FileImpl csmFile = startProject == null ? null : startProject.getFile(startEntry.getStartFile(), false);
        return csmFile;
    }

    public static ProjectBase getStartProject(final APTPreprocHandler.State state) {
        return getStartProject(APTHandlersSupport.extractStartEntry(state));
    }

    public static ProjectBase getStartProject(StartEntry startEntry) {
        if (startEntry == null) {
            return null;
        }
        Key key = startEntry.getStartFileProject();
        ProjectBase prj = (ProjectBase) RepositoryUtils.get(key);
        return prj;
    }

    public static boolean isCppFile(CsmFile file) {
        return (file instanceof FileImpl) && ((FileImpl) file).isCppFile();
    }

    public static FileImpl.FileType getFileType(NativeFileItem nativeFile) {
        switch (nativeFile.getLanguage()) {
            case C:
                return FileImpl.FileType.SOURCE_C_FILE;
            case CPP:
                return FileImpl.FileType.SOURCE_CPP_FILE;
            case FORTRAN:
                return FileImpl.FileType.SOURCE_FORTRAN_FILE;
            case C_HEADER:
                return FileImpl.FileType.HEADER_FILE;
            default:
                return FileImpl.FileType.UNDEFINED_FILE;
        }
    }

    public static boolean acceptNativeItem(NativeFileItem item) {
        if (item.getFileObject() == null || !item.getFileObject().isValid()) {
            return false;
        }
        NativeFileItem.Language language = item.getLanguage();
        return (language == NativeFileItem.Language.C ||
                language == NativeFileItem.Language.CPP ||
                language == NativeFileItem.Language.FORTRAN ||
                language == NativeFileItem.Language.C_HEADER) &&
                !item.isExcluded();
    }

}
