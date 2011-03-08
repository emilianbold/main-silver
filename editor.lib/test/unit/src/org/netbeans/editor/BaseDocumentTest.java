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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * Test functionality of BaseDocument.
 *
 * @author Miloslav Metelka
 */
public class BaseDocumentTest extends NbTestCase {

    public BaseDocumentTest(String testName) {
        super(testName);
    }

    public void testRowUtilities() throws Exception {
        BaseDocument doc = new BaseDocument(false, "text/plain"); // NOI18N
        doc.insertString(0, "a\nbc", null);
        int offset = Utilities.getRowStart(doc, doc.getLength() + 1);
        assertEquals("Invalid offset", 2, offset); // NOI18N
        offset = Utilities.getFirstNonWhiteBwd(doc, 2, doc.getLength() + 1);
        assertEquals("Invalid offset", 2, offset); // NOI18N
        offset = Utilities.getRowLastNonWhite(doc, doc.getLength() + 1);
        assertEquals("Invalid offset", 3, offset); // NOI18N
        offset = Utilities.getRowEnd(doc, doc.getLength() + 1);
        assertEquals("Invalid offset", 4, offset); // NOI18N
        int index = Utilities.getLineOffset(doc, doc.getLength() + 1);
        assertEquals("Invalid index", 1, index); // NOI18N
    }

    public void testGetText() throws Exception {
        BaseDocument doc = new BaseDocument(false, "text/plain");
        CharSequence text = DocumentUtilities.getText(doc);
        assertEquals(1, text.length());
        assertEquals('\n', text.charAt(0));

        text = DocumentUtilities.getText(doc);
        doc.insertString(0, "a\nb", null);
        for (int i = 0; i < doc.getLength() + 1; i++) {
            assertEquals(doc.getText(i, 1).charAt(0), text.charAt(i));
        }
    }

    public void testParagraphUpdates() throws Exception {
        paragraphUpdatesImpl(new PlainDocument());
        BaseDocument doc = new BaseDocument(false, "text/plain");
        paragraphUpdatesImpl(doc);
    }

    public void paragraphUpdatesImpl(Document doc) throws Exception {
        doc.addDocumentListener(new DocumentListener() {
            int version;

            @Override
            public void insertUpdate(DocumentEvent e) {
                switch (version++) {
                    case 0:
                        assertLineElementChange(e, 0, 17, 0, 3, 3, 7, 7, 8, 8, 14, 14, 17);
                        break;
                    default:
                        fail("Invalid insertUpdate version=" + version);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                switch (version++) {
                    case 1:
                        assertLineElementChange(e, 8, 10, 10, 11, 8, 11);
                        break;
                    default:
                        fail("Invalid insertUpdate version=" + version);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
            
            private void assertLineElementChange(DocumentEvent evt, int... startEndOffsets) {
                int offsetsIndex = 0;
                DocumentEvent.ElementChange lineElementChange = evt.getChange(evt.getDocument().getDefaultRootElement());
                if (lineElementChange != null) {
                    Element[] removedLines = lineElementChange.getChildrenRemoved();
                    for (int i = 0; i < removedLines.length; i++) {
                        assertElementBounds(removedLines[i], startEndOffsets, offsetsIndex);
                        offsetsIndex += 2;
                    }
                    Element[] addedLines = lineElementChange.getChildrenAdded();
                    for (int i = 0; i < addedLines.length; i++) {
                        assertElementBounds(addedLines[i], startEndOffsets, offsetsIndex);
                        offsetsIndex += 2;
                    }
                }
            }
            
            private void assertElementBounds(Element line, int[] startEndOffsets, int index) {
                assertTrue("startEndOffsets.length=" + startEndOffsets.length + " < " + (index + 2), index + 2 <= startEndOffsets.length);
                assertEquals("Invalid line[" + (index >> 1) + "] startOffset", startEndOffsets[index], line.getStartOffset());
                assertEquals("Invalid line[" + (index >> 1) + "] endOffset", startEndOffsets[index + 1], line.getEndOffset());

            }
        });
        doc.insertString(0, "ab\ncde\n\nfghij\nkl", null);
        doc.remove(10, 6);
    }

    public void testRecursiveUndoableEdits() throws Exception {
        final BaseDocument doc = new BaseDocument(false, "text/plain");
        class UEL implements UndoableEditListener, Runnable {
            boolean undo;
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                //doc.runAtomic(this);
                doc.render(this);
                undo = e.getEdit().canUndo();
            }

            @Override
            public void run() {
            }
        }
        UEL uel = new UEL();
        doc.addUndoableEditListener(uel);

        class Atom implements Runnable {
            @Override
            public void run() {
                try {
                    doc.insertString(0, "Ahoj", null);
                } catch (BadLocationException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        doc.runAtomicAsUser(new Atom());

        assertTrue("Can undo now", uel.undo);
    }

    public void testBreakAtomicLock() throws Exception {
        final BaseDocument doc = new BaseDocument(false, "text/plain");
        doc.runAtomic(new Runnable() {
            public @Override void run() {
                try {
                    doc.insertString(0, "test1", null);
                    doc.breakAtomicLock();
                } catch (BadLocationException e) {
                    // Expected
                }
            }
        });
        boolean failure = false;
        try {
            doc.runAtomic(new Runnable() {
                public @Override void run() {
                    throw new IllegalStateException("test");
                }
            });
            failure = true;
        } catch (Throwable t) {
            // Expected
        }
        if (failure) {
            throw new IllegalStateException("Unexpected");
        }
        doc.runAtomic(new Runnable() {
            public @Override void run() {
                try {
                    doc.insertString(0, "test1", null);
                    doc.insertString(10, "test2", null);
                } catch (BadLocationException e) {
                    // Expected
                }
            }
        });
    }

    public void testPropertyChangeEvents() {
        final List<PropertyChangeEvent> events = new LinkedList<PropertyChangeEvent>();
        final BaseDocument doc = new BaseDocument(false, "text/plain");
        final PropertyChangeListener l = new PropertyChangeListener() {
            public @Override void propertyChange(PropertyChangeEvent evt) {
                events.add(evt);
            }
        };

        DocumentUtilities.addPropertyChangeListener(doc, l);
        assertEquals("No events expected", 0, events.size());

        doc.putProperty("prop-A", "value-A");
        assertEquals("No event fired", 1, events.size());
        assertEquals("Wrong property name", "prop-A", events.get(0).getPropertyName());
        assertNull("Wrong old property value", events.get(0).getOldValue());
        assertEquals("Wrong new property value", "value-A", events.get(0).getNewValue());

        events.clear();
        DocumentUtilities.removePropertyChangeListener(doc, l);
        assertEquals("No events expected", 0, events.size());

        doc.putProperty("prop-B", "value-B");
        assertEquals("Expecting no events on removed listener", 0, events.size());
    }
}
    