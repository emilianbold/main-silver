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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.extras.api.support;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.dlight.extras.api.ViewportModelState;
import org.netbeans.modules.dlight.util.Range;

/**
 *
 * @author Alexey Vladykin
 */
public class DefaultViewportModelTest {

    @Test
    public void testLimits() {
        DefaultViewportModel model = new DefaultViewportModel(new Range<Long>(0L, 0L), new Range<Long>(0L, 0L));
        assertEquals(Long.valueOf(0L), model.getLimits().getStart());
        assertEquals(Long.valueOf(0L), model.getLimits().getEnd());
        model.setLimits(new Range<Long>(null, 20L));
        assertEquals(Long.valueOf(0L), model.getLimits().getStart());
        assertEquals(Long.valueOf(20L), model.getLimits().getEnd());
    }

    @Test
    public void testViewport() {
        DefaultViewportModel model = new DefaultViewportModel(new Range<Long>(0L, 0L), new Range<Long>(0L, 0L));
        assertEquals(Long.valueOf(0L), model.getViewport().getStart());
        assertEquals(Long.valueOf(0L), model.getViewport().getEnd());
        model.setViewport(new Range<Long>(null, 20L));
        assertEquals(Long.valueOf(0L), model.getViewport().getStart());
        assertEquals(Long.valueOf(20L), model.getViewport().getEnd());
    }

    @Test
    public void testViewportState() {
        DefaultViewportModel model = new DefaultViewportModel(new Range<Long>(0L, 0L), new Range<Long>(0L, 0L));
        model.setLimits(new Range<Long>(-10L, 10L));
        model.setViewport(new Range<Long>(-1L, 1L));
        ViewportModelState state = model.getState();
        assertEquals(model.getLimits(), state.getLimits());
        assertEquals(model.getViewport(), state.getViewport());
    }

    @Test
    public void testLimitsNotification() {
        final AtomicBoolean gotNotification = new AtomicBoolean(false);
        DefaultViewportModel model = new DefaultViewportModel(new Range<Long>(0L, 0L), new Range<Long>(0L, 0L));
        model.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                gotNotification.set(true);
            }
        });
        model.setLimits(new Range<Long>(0L, 2L));
        assertTrue(gotNotification.get());
    }

    @Test
    public void testViewportNotification() {
        final AtomicBoolean gotNotification = new AtomicBoolean(false);
        DefaultViewportModel model = new DefaultViewportModel(new Range<Long>(0L, 0L), new Range<Long>(0L, 0L));
        model.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                gotNotification.set(true);
            }
        });
        model.setViewport(new Range<Long>(0L, 2L));
        assertTrue(gotNotification.get());
    }

    @Test
    public void testViewportAutoscroll() {
        DefaultViewportModel model = new DefaultViewportModel(new Range<Long>(0L, 0L), new Range<Long>(0L, 10L));
        model.setLimits(new Range<Long>(0L, 20L));
        assertEquals(Long.valueOf(10L), model.getViewport().getStart());
        assertEquals(Long.valueOf(20L), model.getViewport().getEnd());
    }

    @Test
    public void testMinViewportSize() {
        final AtomicBoolean gotNotification = new AtomicBoolean(false);
        DefaultViewportModel model = new DefaultViewportModel(new Range<Long>(0L, 0L), new Range<Long>(0L, 10L));
        model.setMinViewportSize(10L);
        model.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                gotNotification.set(true);
            }
        });
        model.setViewport(new Range<Long>(5L, 10L));
        assertFalse(gotNotification.get());
        assertEquals(Long.valueOf(0L), model.getViewport().getStart());
        assertEquals(Long.valueOf(10L), model.getViewport().getEnd());
    }
}
