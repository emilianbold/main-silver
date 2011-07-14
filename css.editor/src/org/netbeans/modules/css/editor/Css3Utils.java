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
package org.netbeans.modules.css.editor;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.ProblemDescription;
import org.openide.filesystems.FileObject;

/**
 * Utility methods to be refactored out later.
 * 
 * 
 * @author marekfukala
 */
public final class Css3Utils {
   
    public static List<Error> getCslErrorForCss3ProblemDescription(FileObject file, List<ProblemDescription> pds) {
        List<Error> errors = new ArrayList<Error>();
        for(ProblemDescription pd : pds) {
            errors.add(getCslErrorForCss3ProblemDescription(file, pd));
        }
        return errors;
    }
    
    private static Error getCslErrorForCss3ProblemDescription(FileObject file, ProblemDescription pd) {
        return new DefaultError(
                pd.getKey(), 
                pd.getDescription(), 
                pd.getDescription(), 
                file, 
                pd.getFrom(), 
                pd.getTo(),
                getCslSeverityForCss3ProblemType(pd.getType()));
    }

    public static Severity getCslSeverityForCss3ProblemType(ProblemDescription.Type problemType) {
        switch(problemType) {
            case ERROR:
                return Severity.ERROR;
            case FATAL:
                    return Severity.FATAL;
            case INFO:
                    return Severity.INFO;
            case WARNING:
                return Severity.WARNING;
        }
        
        return Severity.ERROR;
    }
    
    public static OffsetRange getOffsetRange(Node node) {
        return new OffsetRange(node.from(), node.to());
    }
}
