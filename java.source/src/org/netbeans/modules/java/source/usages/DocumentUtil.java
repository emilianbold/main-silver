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

package org.netbeans.modules.java.source.usages;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class DocumentUtil {
    
    private static final String FIELD_RESOURCE_NAME = "resName";        //NOI18N
    private static final String FIELD_BINARY_NAME = "binaryName";         //NOI18N
    private static final String FIELD_PACKAGE_NAME = "packageName";     //NOI18N
    private static final String FIELD_REFERENCES = "references";        //NOI18N
    static final String FIELD_SIMPLE_NAME = "simpleName";       //NOI18N
    static final String FIELD_CASE_INSENSITIVE_NAME = "ciName"; //NOI18N
    private static final String FIELD_SOURCE = "source";                //NOI18N
    private static final String FIELD_IDENTS = "ids";                           //NOI18N
    private static final String FIELD_FEATURE_IDENTS = "fids";                  //NOI18N
    private static final String FIELD_CASE_INSENSItIVE_FEATURE_IDENTS="cifids"; //NOI18N
    
    private static final char NO = '-';                                 //NOI18N
    private static final char YES = '+';                                //NOI18N
    private static final char WILDCARD = '?';                           //NOI18N
    private static final char PKG_SEPARATOR = '.';                      //NOI18N
    
    private static final char EK_CLASS = 'C';                           //NOI18N
    private static final char EK_INTERFACE = 'I';                       //NOI18N
    private static final char EK_ENUM = 'E';                            //NOI18N
    private static final char EK_ANNOTATION = 'A';                      //NOI18N
        
    private static final int SIZE = ClassIndexImpl.UsageType.values().length;    
    private static final char[] MASK_ANY_USAGE = new char[SIZE];        
    
    static {
        Arrays.fill(MASK_ANY_USAGE, WILDCARD);    //NOI18N
    }
    
    private DocumentUtil () {
    }
    
    //Convertor factories
    public static ResultConvertor<Document,FileObject> fileObjectConvertor (final FileObject... roots) {
        assert roots != null;
        return new FileObjectConvertor (roots);
    }
    
    public static ResultConvertor<Document,ElementHandle<TypeElement>> elementHandleConvertor () {
        return new ElementHandleConvertor ();
    }
    
    public static ResultConvertor<Document,String> binaryNameConvertor () {
        return new BinaryNameConvertor ();
    }
    
    static ResultConvertor<Document,String> sourceNameConvertor () {
        return new SourceNameConvertor();
    }
    
    //Document field getters
    static String getBinaryName (final Document doc) {
        return getBinaryName(doc, null);
    }
    
    static String getBinaryName (final Document doc, final ElementKind[] kind) {
        assert doc != null;
        final Field pkgField = doc.getField(FIELD_PACKAGE_NAME);
        final Field snField = doc.getField (FIELD_BINARY_NAME);
        if (snField == null) {
            return null;
        }
        final String tmp = snField.stringValue();
        final String snName = tmp.substring(0,tmp.length()-1);
        if (kind != null) {
            assert kind.length == 1;
            kind[0] = decodeKind (tmp.charAt(tmp.length()-1));
        }
        if (pkgField == null) {
            return snName;
        }        
        String pkg = pkgField.stringValue();
        if (pkg.length() == 0) {
            return snName;
        }
        return  pkg + PKG_SEPARATOR + snName;   //NO I18N
    }
    
    static String getSimpleBinaryName (final Document doc) {
        assert doc != null;
        Field field = doc.getField(FIELD_BINARY_NAME);
        if (field == null) {
            return null;
        }
        else {
            return field.stringValue();
        }
    }
        
    static String getPackageName (final Document doc) {
        assert doc != null;
        Field field = doc.getField(FIELD_PACKAGE_NAME);
        return field == null ? null : field.stringValue();
    }
                
    
    //Term and query factories
    static Query binaryNameQuery (final String resourceName) {
        final BooleanQuery query = new BooleanQuery ();
        int index = resourceName.lastIndexOf(PKG_SEPARATOR);  // NOI18N
        String pkgName, sName;
        if (index < 0) {
            pkgName = "";   // NOI18N
            sName = resourceName;
        }
        else {
            pkgName = resourceName.substring(0,index);
            sName = resourceName.substring(index+1);
        }
        sName = sName + WILDCARD;
        query.add (new TermQuery (new Term (FIELD_PACKAGE_NAME, pkgName)),BooleanClause.Occur.MUST);
        query.add (new WildcardQuery (new Term (FIELD_BINARY_NAME, sName)),BooleanClause.Occur.MUST);
        return query;
    }
    
    static Query binaryNameSourceNamePairQuery (final Pair<String,String> binaryNameSourceNamePair) {
        assert binaryNameSourceNamePair != null;
        final String binaryName = binaryNameSourceNamePair.first;
        final String sourceName = binaryNameSourceNamePair.second;
        final Query query = binaryNameQuery(binaryName);
        if (sourceName != null) {
            assert query instanceof BooleanQuery : "The DocumentUtil.binaryNameQuery was incompatibly changed!";        //NOI18N
            final BooleanQuery bq = (BooleanQuery) query;
            bq.add(new TermQuery(new Term (FIELD_SOURCE,sourceName)), BooleanClause.Occur.MUST);
        }
        return query;
    }
    
    static Query binaryContentNameQuery (final Pair<String,String> binaryNameSourceNamePair) {
        final String resourceName = binaryNameSourceNamePair.first;
        final String sourceName = binaryNameSourceNamePair.second;
        int index = resourceName.lastIndexOf(PKG_SEPARATOR);  // NOI18N
        String pkgName, sName;
        if (index < 0) {
            pkgName = "";   // NOI18N
            sName = resourceName;
        }
        else {
            pkgName = resourceName.substring(0,index);
            sName = resourceName.substring(index+1);
        }
        BooleanQuery query = new BooleanQuery ();
        BooleanQuery subQuery = new BooleanQuery();
        subQuery.add (new WildcardQuery (new Term (FIELD_BINARY_NAME, sName + WILDCARD)),BooleanClause.Occur.SHOULD);
        subQuery.add (new PrefixQuery (new Term (FIELD_BINARY_NAME, sName + '$')),BooleanClause.Occur.SHOULD);
        query.add (new TermQuery (new Term (FIELD_PACKAGE_NAME, pkgName)),BooleanClause.Occur.MUST);
        query.add (subQuery,BooleanClause.Occur.MUST);
        if (sourceName != null) {
            query.add (new TermQuery(new Term (FIELD_SOURCE,sourceName)), BooleanClause.Occur.MUST);
        }
        return query;
    }
    
    static Term identTerm (final String ident) {
        assert ident != null;
        return new Term (FIELD_IDENTS, ident);
    }
    
    static Query identQuery (final String ident) {
        return new TermQuery(identTerm(ident));
    }
    
    static Term featureIdentTerm (final String ident) {
        assert ident != null;
        return new Term (FIELD_FEATURE_IDENTS, ident);
    }
    
    static Query featureIdentQuery (final String ident) {
        return new TermQuery(featureIdentTerm(ident));
    }
    
    static Term caseInsensitiveFeatureIdentTerm (final String ident) {
        assert ident != null;
        return new Term (FIELD_CASE_INSENSItIVE_FEATURE_IDENTS, ident);
    }
            
    static Term simpleBinaryNameTerm (final String resourceFileName) {
        assert resourceFileName != null;
        return new Term (FIELD_BINARY_NAME, resourceFileName);
    }   
    
    static Term packageNameTerm (final String packageName) {
        assert packageName != null;
        return new Term (FIELD_PACKAGE_NAME, packageName);
    }
    
    static Term referencesTerm (String resourceName, final Set<ClassIndexImpl.UsageType> usageType) {
        assert resourceName  != null;
        if (usageType != null) {
            resourceName = encodeUsage (resourceName, usageType, WILDCARD).toString();
        }
        else {
            StringBuilder sb = new StringBuilder (resourceName);
            sb.append(MASK_ANY_USAGE);
            resourceName = sb.toString();
        }
        return new Term (FIELD_REFERENCES, resourceName);
    }
        
    //Factories for lucene document
    static Document createDocument (final String binaryName,
            List<String> references,
            String featureIdents,
            String idents,
            String source) {
        assert binaryName != null;
        assert references != null;
        int index = binaryName.lastIndexOf(PKG_SEPARATOR);  //NOI18N
        String fileName, pkgName, simpleName, caseInsensitiveName;
        if (index<0) {
            fileName = binaryName;
            pkgName = "";                           //NOI18N
        }
        else {
            fileName = binaryName.substring(index+1);
            pkgName = binaryName.substring(0,index);
        }
        index = fileName.lastIndexOf('$');  //NOI18N
        if (index<0) {
            simpleName = fileName.substring(0, fileName.length()-1);
        }
        else {
            simpleName = fileName.substring(index+1,fileName.length()-1);
        }
        caseInsensitiveName = simpleName.toLowerCase();         //XXX: I18N, Locale
        Document doc = new Document ();        
        Field field = new Field (FIELD_BINARY_NAME,fileName,Field.Store.YES, Field.Index.NO_NORMS);
        doc.add (field);
        field = new Field (FIELD_PACKAGE_NAME,pkgName,Field.Store.YES, Field.Index.NO_NORMS);
        doc.add (field);
        field = new Field (FIELD_SIMPLE_NAME,simpleName, Field.Store.YES, Field.Index.NO_NORMS);
        doc.add (field);
        field = new Field (FIELD_CASE_INSENSITIVE_NAME, caseInsensitiveName, Field.Store.YES, Field.Index.NO_NORMS);
        doc.add (field);
        for (String reference : references) {
            field = new Field (FIELD_REFERENCES,reference,Field.Store.NO,Field.Index.NO_NORMS);
            doc.add(field);
        }
        if (featureIdents != null) {
            field = new Field(FIELD_FEATURE_IDENTS, featureIdents, Field.Store.NO, Field.Index.TOKENIZED);
            doc.add(field);
            field = new Field(FIELD_CASE_INSENSItIVE_FEATURE_IDENTS, featureIdents, Field.Store.NO, Field.Index.TOKENIZED);
            doc.add(field);
        }
        if (idents != null) {
            field = new Field(FIELD_IDENTS, idents, Field.Store.NO, Field.Index.TOKENIZED);
            doc.add(field);
        }
        if (source != null) {
            field = new Field (FIELD_SOURCE,source,Field.Store.YES,Field.Index.NO_NORMS);
            doc.add(field);
        }
        return doc;
    }
        
    // Functions for encoding and decoding of UsageType
    static StringBuilder createUsage (final String className) {
        Set<ClassIndexImpl.UsageType> EMPTY = Collections.emptySet();
        return encodeUsage (className, EMPTY,NO);
    }
    
    static void addUsage (final StringBuilder rawUsage, final ClassIndexImpl.UsageType type) {
        assert rawUsage != null;
        assert type != null;
        final int rawUsageLen = rawUsage.length();
        final int startIndex = rawUsageLen - SIZE;
        rawUsage.setCharAt (startIndex + type.getOffset(),YES);
    }
    
    static String encodeUsage (final String className, final Set<ClassIndexImpl.UsageType> usageTypes) {
        return encodeUsage (className, usageTypes, NO).toString();
    }
    
    private static StringBuilder encodeUsage (final String className, final Set<ClassIndexImpl.UsageType> usageTypes, char fill) {
        assert className != null;
        assert usageTypes != null;
        StringBuilder builder = new StringBuilder ();
        builder.append(className);
        char[] map = new char [SIZE];
        Arrays.fill(map,fill);
        for (ClassIndexImpl.UsageType usageType : usageTypes) {
            int offset = usageType.getOffset ();
            assert offset >= 0 && offset < SIZE;
            map[offset] = YES;
        }
        builder.append(map);
        return builder;
    }
    
    static String encodeUsage (final String className, final String usageMap) {
        assert className != null;
        assert usageMap != null;
        StringBuilder sb = new StringBuilder ();
        sb.append(className);
        sb.append(usageMap);
        return sb.toString();
    }
    
    static String decodeUsage (final String rawUsage, final Set<ClassIndexImpl.UsageType> usageTypes) {
        assert rawUsage != null;
        assert usageTypes != null;
        assert usageTypes.isEmpty();
        final int rawUsageLen = rawUsage.length();
        assert rawUsageLen>SIZE;
        final int index = rawUsageLen - SIZE;
        final String className = rawUsage.substring(0,index);
        final String map = rawUsage.substring (index);
        for (ClassIndexImpl.UsageType usageType : ClassIndexImpl.UsageType.values()) {
            if (map.charAt(usageType.getOffset()) == YES) {
                usageTypes.add (usageType);
            }
        }        
        return className;
    }
    
    static ElementKind decodeKind (char kind) {
        switch (kind) {
            case EK_CLASS:
                return ElementKind.CLASS;
            case EK_INTERFACE:
                return ElementKind.INTERFACE;
            case EK_ENUM:
                return ElementKind.ENUM;
            case EK_ANNOTATION:
                return ElementKind.ANNOTATION_TYPE;
            default:
                throw new IllegalArgumentException ();
        }
    }
    
    static char encodeKind (ElementKind kind) {
        switch (kind) {
            case CLASS:
                return EK_CLASS;
            case INTERFACE:
                return EK_INTERFACE;
            case ENUM:
                return EK_ENUM;
            case ANNOTATION_TYPE:
                return EK_ANNOTATION;
            default:
                throw new IllegalArgumentException ();
        }
    }       
    
    
    static FieldSelector declaredTypesFieldSelector () {
        return new FieldSelectorImpl(FIELD_PACKAGE_NAME,FIELD_BINARY_NAME);
    }
    
    static FieldSelector sourceNameFieldSelector () {
        return new FieldSelectorImpl(FIELD_SOURCE);
    }
            
    private static class FieldSelectorImpl implements FieldSelector {
        
        private final Term[] terms;
        
        FieldSelectorImpl(String... fieldNames) {
            terms = new Term[fieldNames.length];
            for (int i=0; i< fieldNames.length; i++) {
                terms[i] = new Term (fieldNames[i],""); //NOI18N
            }
        }
        
        @Override
        public FieldSelectorResult accept(String fieldName) {
            for (Term t : terms) {
                if (fieldName == t.field()) {
                    return FieldSelectorResult.LOAD;
                }
            }
            return FieldSelectorResult.NO_LOAD;
        }
    }
        
    
    private static class LCWhitespaceTokenizer extends WhitespaceTokenizer {
        LCWhitespaceTokenizer (final Reader r) {
            super (r);
        }
        
        protected char normalize(char c) {
            return Character.toLowerCase(c);
        }        
    }
    
    static final class LCWhitespaceAnalyzer extends Analyzer {
        public TokenStream tokenStream(String fieldName, Reader reader) {
            return new LCWhitespaceTokenizer(reader);
        }
    }
    
    private static class FileObjectConvertor implements ResultConvertor<Document,FileObject> {                
        
        private FileObject[] roots;
        
        private FileObjectConvertor (final FileObject... roots) {
            this.roots = roots;
        }
        
        @Override
        public FileObject convert (final Document doc) {
            final String binaryName = getBinaryName(doc, null);
            return binaryName == null ? null : convert(binaryName);
        }
        
        private FileObject convert(String value) {
            for (FileObject root : roots) {
                FileObject result = resolveFile (root, value);
                if (result != null) {
                    return result;
                }
            }
            final ClassIndexManager cim = ClassIndexManager.getDefault();
            for (FileObject root : roots ) {
                try {
                    ClassIndexImpl impl = cim.getUsagesQuery(root.getURL());
                    if (impl != null) {
                        String sourceName = impl.getSourceName(value);
                        if (sourceName != null) {
                            FileObject result = root.getFileObject(sourceName);
                            if (result != null) {
                                return result;
                            }
                        }
                    }
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                } catch (InterruptedException ie) {
                    //Safe to ingnore
                }               
            }
            return null;
        }
        
        private static FileObject resolveFile (final FileObject root, String classBinaryName) {
            assert classBinaryName != null;
            classBinaryName = classBinaryName.replace('.', '/');    //NOI18N
            int index = classBinaryName.lastIndexOf('/');           //NOI18N
            FileObject folder;
            String name;
            if (index<0) {
                folder = root;
                name = classBinaryName;
            }
            else {
                assert index>0 : classBinaryName;
                assert index<classBinaryName.length() - 1 : classBinaryName;
                folder = root.getFileObject(classBinaryName.substring(0,index));
                name = classBinaryName.substring(index+1);
            }
            if (folder == null) {
                return null;
            }
            index = name.indexOf('$');                              //NOI18N
            if (index>0) {
                name = name.substring(0, index);
            }
            for (FileObject child : folder.getChildren()) {
                if (FileObjects.JAVA.equalsIgnoreCase(child.getExt()) && name.equals(child.getName())) {
                    return child;
                }
            }
            return null;
        }
    }
    
    private static class ElementHandleConvertor implements ResultConvertor<Document,ElementHandle<TypeElement>> {
        
        private final ElementKind[] kindHolder = new ElementKind[1];

        @Override
        public ElementHandle<TypeElement> convert (final Document doc) {
            final String binaryName = getBinaryName(doc, kindHolder);
            return binaryName == null ? null : convert(kindHolder[0], binaryName);
        }

        private ElementHandle<TypeElement> convert(ElementKind kind, String value) {
            return ElementHandleAccessor.INSTANCE.create(kind, value);
        }
    }
    
    private static class BinaryNameConvertor implements ResultConvertor<Document,String> {
        
        @Override
        public String convert (final Document doc) {
            return getBinaryName(doc, null);
        }
    }
    
    private static class SourceNameConvertor implements ResultConvertor<Document,String> {

        @Override
        public String convert(Document doc) {
            Field field = doc.getField(FIELD_SOURCE);
            return field == null ? null : field.stringValue();
        }
    }       
}
