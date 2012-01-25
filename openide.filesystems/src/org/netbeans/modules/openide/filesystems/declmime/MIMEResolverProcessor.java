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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.openide.filesystems.declmime;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.MIMEResolver.ExtensionRegistration;
import org.openide.filesystems.MIMEResolver.Registration;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class MIMEResolverProcessor extends LayerGeneratingProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<String>();
        set.add(MIMEResolver.Registration.class.getCanonicalName());
        set.add(MIMEResolver.ExtensionRegistration.class.getCanonicalName());
        return set;
    }
    
    
    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        for (Element e : roundEnv.getElementsAnnotatedWith(MIMEResolver.Registration.class)) {
            MIMEResolver.Registration r = e.getAnnotation(MIMEResolver.Registration.class);
            registerDefinition(e, r.resource(), r);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(MIMEResolver.ExtensionRegistration.class)) {
            MIMEResolver.ExtensionRegistration r = e.getAnnotation(MIMEResolver.ExtensionRegistration.class);
            registerExt(e, r);
        }
        return true;
    }
    private void registerExt(Element e, ExtensionRegistration r) throws LayerGenerationException {
        final LayerBuilder b = layer(e);
        File f = b.file("Services/MIMEResolver/" + getName(e).replace('.', '-') + "-Extension.instance"); // NOI18N
        f.methodvalue("instanceCreate", MIMEResolver.class.getName(), "create"); // NOI18N
        f.stringvalue("instanceClass", MIMEResolver.class.getName()); // NOI18N
        f.stringvalue("mimeType", r.mimeType()); // NOI18N
        int cnt = 0;
        for (String ext : r.extension()) {
            f.stringvalue("ext." + (cnt++), ext); // NOI18N
        }
        f.position(r.position());
        f.bundlevalue("displayName", r.displayName()); // NOI18N
        f.write();
        
    }

    private void registerDefinition(Element e, String relRes, Registration r) throws LayerGenerationException {
        String absRes = LayerBuilder.absolutizeResource(e, relRes);
        final LayerBuilder b = layer(e);
        FileObject fo = b.validateResource(absRes, e, r, null, false);
        File f = b.file("Services/MIMEResolver/" + getName(e).replace('.', '-') + "-Registration.instance"); // NOI18N
        f.methodvalue("instanceCreate", MIMEResolver.class.getName(), "create"); // NOI18N
        f.stringvalue("instanceClass", MIMEResolver.class.getName()); // NOI18N
        f.serialvalue("bytes", generateInstanceResolver(fo, e)); // NOI18N
        f.position(r.position());
        f.bundlevalue("displayName", r.displayName()); // NOI18N
        f.write();
    }

    private byte[] generateInstanceResolver(FileObject fo, Element e) throws LayerGenerationException {
        try {
            InputStream is = fo.openInputStream();
            org.openide.filesystems.FileObject tmp = FileUtil.createMemoryFileSystem().getRoot().createData("resolver.xml");
            OutputStream os = tmp.getOutputStream();
            for (;;) {
                int ch = is.read();
                if (ch == -1) {
                    break;
                }
                os.write(ch);
            }
            os.close();
            is.close();
            final byte[] almostResult = MIMEResolverImpl.toStream(MIMEResolverImpl.forDescriptor(tmp));
            // XXX: it would be slightly shorter to return the array directly,
            // but the XMLFileSystem insist on deserializing the value, it does
            // not support returning plain byte[]
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(almostResult);
            oos.close();
            return out.toByteArray();
        } catch (IOException ex) {
            final LayerGenerationException le = new LayerGenerationException("Cannot process " + fo, e);
            le.initCause(ex);
            throw le;
        }
    }

    private String getName(Element e) {
        if (e.getKind().isClass() || e.getKind().isInterface()) {
            return processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString();
        } else if (e.getKind() == ElementKind.PACKAGE) {
            return e.getSimpleName().toString();
        } else {
            return getName(e.getEnclosingElement()) + '.' + e.getSimpleName();
        }
    }

    
}
