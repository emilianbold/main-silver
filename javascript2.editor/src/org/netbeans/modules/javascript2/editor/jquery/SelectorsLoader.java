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
package org.netbeans.modules.javascript2.editor.jquery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class loads all selectors from jquery-api.xml file.
 * 
 * @author Petr Pisl
 */
public class SelectorsLoader extends DefaultHandler {
    
    private static final Logger LOGGER = Logger.getLogger(SelectorsLoader.class.getName());
    
    private SelectorsLoader() {
        
    }
    private static List<JQueryCodeCompletion.SelectorItem> result = new ArrayList<JQueryCodeCompletion.SelectorItem>();
    
    public static Collection<JQueryCodeCompletion.SelectorItem> getSelectors(File file) {
        result.clear();
        try {
            long start = System.currentTimeMillis();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            DefaultHandler handler = new SelectorsLoader();
            parser.parse(file, handler);
            long end = System.currentTimeMillis();
            LOGGER.log(Level.FINE, "Loading selectors from API file took {0}ms ",  (end - start));
         
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    private boolean inSelector = false;
    private enum Tag {
        sample, notinterested;
    }
    private String name;
    private String sample;
    
    private Tag inTag = Tag.notinterested;
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (inSelector){
          if (qName.equals(Tag.sample.name())){
              inTag = Tag.sample;
          }  
        } else if(qName.equals("entry")) {
            String type = attributes.getValue("type");
            if (type.equals("selector")) {
                inSelector = true;
                name = attributes.getValue("name");
                System.out.println("Selector: " + name);
            }
        }
        
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(inSelector && qName.equals("entry")) {
            inSelector = false;
            String template = null;
            if(sample.indexOf('(') > -1) {
                template = name + "(${cursor})";
                name = name + "()";
            }
            JQueryCodeCompletion.SelectorItem item = new JQueryCodeCompletion.SelectorItem(name, template);
            result.add(item);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (inTag) {
            case sample:
                sample = new String(ch, start, length);
                inTag = Tag.notinterested;
                break;
        }
    }
    
    
    
}
