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
package org.netbeans.modules.dlight.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class DLightMathTest {

    @Test
    public void testMaxInteger() {
        Integer a = new Integer(1);
        Integer b = new Integer(2);
        assertSame(b, DLightMath.max(a, b));
    }

    @Test
    public void testMaxString() {
        String a = "a";
        String b = "b";
        assertSame(b, DLightMath.max(a, b));
    }

    @Test
    public void testMinInteger() {
        Integer a = new Integer(1);
        Integer b = new Integer(2);
        assertSame(a, DLightMath.min(a, b));
    }

    @Test
    public void testMinString() {
        String a = "a";
        String b = "b";
        assertSame(a, DLightMath.min(a, b));
    }

    @Test
    public void testNearestMultipleOf() {
        assertEquals(10, DLightMath.nearestMultipleOf(10, 11));
        assertEquals(40, DLightMath.nearestMultipleOf(10, 39));
        assertEquals(10, DLightMath.nearestMultipleOf(10, 5));
        assertEquals(0, DLightMath.nearestMultipleOf(10, 4));
        assertEquals(444, DLightMath.nearestMultipleOf(222, 445));
    }

    @Test
    public void testPrevMultipleOf() {
        assertEquals(10990, DLightMath.prevMultipleOf(10, 11000));
        assertEquals(230, DLightMath.prevMultipleOf(10, 234));
    }

    @Test
    public void testNextMultipleOf() {
        assertEquals(123, DLightMath.prevMultipleOf(123, 246));
        assertEquals(123, DLightMath.prevMultipleOf(123, 200));
    }
}
