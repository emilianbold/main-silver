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

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;

/**
 * Provider of information about a specific grid.
 *
 * @author Jan Stola, Petr Somol
 */
public interface GridInfoProvider {

    /**
     * Returns the {@code x} coordinate of the origin of the grid.
     *
     * @return the {@code x} coordinate of the origin of the grid.
     */
    int getX();

    /**
     * Returns the {@code y} coordinate of the origin of the grid.
     *
     * @return the {@code y} coordinate of the origin of the grid.
     */
    int getY();

    /**
     * Returns the width of the grid (in pixels).
     *
     * @return the width of the grid (in pixels).
     */
    int getWidth();

    /**
     * Returns the height of the grid (in pixels).
     *
     * @return the height of the grid (in pixels).
     */
    int getHeight();

    /**
     * Returns the number of columns in the grid.
     *
     * @return the number of columns in the grid.
     */
    int getColumnCount();

    /**
     * Returns the number of rows in the grid.
     *
     * @return the number of rows in the grid.
     */
    int getRowCount();

    /**
     * Return the {@code x} coordinates of the column bounds.
     * If the grid has {@code n} columns than the returned
     * array has length {@code n+1}.
     *
     * @return the {@code x} coordinates of the column bounds.
     */
    int[] getColumnBounds();

    /**
     * Return the {@code y} coordinates of the column bounds.
     * If the grid has {@code n} columns than the returned
     * array has length {@code n+1}.
     *
     * @return the {@code y} coordinates of the column bounds.
     */
    int[] getRowBounds();

    /**
     * Returns grid {@code x} coordinate of the given {@code component}.
     *
     * @param component component in the grid.
     * @return grid {@code x} coordinate of the given {@code component}.
     */
    int getGridX(Component component);

    /**
     * Returns grid {@code y} coordinate of the given {@code component}.
     *
     * @param component component in the grid.
     * @return grid {@code y} coordinate of the given {@code component}.
     */
    int getGridY(Component component);

    /**
     * Returns grid width of the given {@code component}.
     *
     * @param component component in the grid.
     * @return grid width of the given {@code component}.
     */
    int getGridWidth(Component component);

    /**
     * Returns grid height of the given {@code component}.
     *
     * @param component component in the grid.
     * @return grid height of the given {@code component}.
     */
    int getGridHeight(Component component);

    /**
     * Paints additional information about component constraints.
     * The origin of the graphics coordinate system is at the top left
     * corner of the designed container.
     * 
     * @param g graphics to use for painting.
     * @param component component whose constraint information should be painted.
     * @param selected determines whether the component is selected in the designer.
     * @param emphColor color derived from grid background, usable for emhasising.
     */
    void paintConstraints(Graphics g, Component component, boolean selected, Color emphColor);

}
