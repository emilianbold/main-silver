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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.editor.ext.html.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.editor.ext.html.parser.api.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.api.ParseException;
import org.netbeans.editor.ext.html.parser.api.HtmlVersion;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.spi.DefaultHtmlParseResult;
import org.netbeans.editor.ext.html.parser.spi.HtmlParseResult;
import org.netbeans.editor.ext.html.parser.spi.HtmlParser;
import org.netbeans.editor.ext.html.parser.api.HtmlSource;
import org.netbeans.editor.ext.html.parser.api.ProblemDescription;
import org.netbeans.editor.ext.html.parser.spi.HtmlTag;
import org.netbeans.editor.ext.html.parser.spi.HtmlTagType;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Can parse HTML4 and older and XHTML1.0
 *
 * @author marekfukala
 */
@ServiceProvider(service = HtmlParser.class, position = 10)
public class DefaultHtmlParser implements HtmlParser {

    private static final Map<HtmlVersion, Collection<HtmlTag>> ALL_TAGS_MAP = new EnumMap<HtmlVersion, Collection<HtmlTag>>(HtmlVersion.class);

    private static synchronized Collection<HtmlTag> getAllTags(HtmlVersion version) {
        Collection<HtmlTag> value = ALL_TAGS_MAP.get(version);
        if (value == null) {
            DTD dtd = version.getDTD();
            assert dtd != null;

            List<Element> all = dtd.getElementList("");
            value = new ArrayList<HtmlTag>();
            for (Element e : all) {
                value.add(DTD2HtmlTag.getTagForElement(e));
            }
            ALL_TAGS_MAP.put(version, value);
        }
        return value;
    }

    @Override
    public boolean canParse(HtmlVersion version) {
        return version == HtmlVersion.HTML32
                || version == HtmlVersion.HTML40_STRICT
                || version == HtmlVersion.HTML40_TRANSATIONAL
                || version == HtmlVersion.HTML40_FRAMESET
                || version == HtmlVersion.HTML41_STRICT
                || version == HtmlVersion.HTML41_TRANSATIONAL
                || version == HtmlVersion.HTML41_FRAMESET
                || version == HtmlVersion.XHTML10_STICT
                || version == HtmlVersion.XHTML10_TRANSATIONAL
                || version == HtmlVersion.XHTML10_FRAMESET
                || version == HtmlVersion.XHTML11;

    }

    @Override
    public HtmlParseResult parse(HtmlSource source, final HtmlVersion version, Lookup lookup) throws ParseException {
        assert version != HtmlVersion.HTML5;

        SyntaxAnalyzerElements elements = lookup.lookup(SyntaxAnalyzerElements.class);
        assert elements != null;

        AstNode root = SyntaxTreeBuilder.makeTree(source, version, elements.items());

        return new DefaultHtmlParseResult(source, root, Collections.<ProblemDescription>emptyList(), version) {

            @Override
            public Collection<HtmlTag> getPossibleTagsInContext(AstNode afterNode, HtmlTagType type) {
                if (type == HtmlTagType.OPEN_TAG) {
                    return DTD2HtmlTag.convert(AstNodeUtils.getPossibleOpenTagElements(afterNode));
                } else {
                    //TODO implement
                    return Collections.emptyList();
                }
            }

            @Override
            public Collection<HtmlTag> getAllTags() {
                return DefaultHtmlParser.getAllTags(version);
            }

        };

    }
}
