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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.AbstractGridAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridActionPerformer;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridBoundsChange;

/**
 * Glass pane of the grid designer.
 *
 * @author Jan Stola
 */
public class GlassPane extends JPanel implements GridActionPerformer {
    /** Color of the grid. */
    private static final Color GRID_COLOR = new Color(192, 192, 192, 128);
    /** Color of the highlighted cell. */
    private static final Color HIGHLIGHT_COLOR = new Color(0,255,0,128);
    /** Gap between the grid and the header. */
    static final int HEADER_GAP = 10;

    private GridDesigner designer;
    /** The container we are glasspane for. */
    private Container innerPane;
    /** The subcontainer of {@code innerPane} that contains the grid. */
    private Container componentPane;
    /** Grid manager. */
    private GridManager gridManager;
    /** Grid information provider. */
    private GridInfoProvider gridInfo;
    /** Grid customizer. */
    private GridCustomizer customizer;

    // SELECTION

    /** Selected component. */
    private Component selection;
    /** Selected columns. */
    private BitSet selectedColumns = new BitSet();
    /** Selected rows. */
    private BitSet selectedRows = new BitSet();
    /** Row of focused cell - used by actions with CELL context. */
    private int focusedCellRow;
    /** Column of focused cell - used by actions with CELL context. */
    private int focusedCellColumn;

    /** The height of the column header. */
    private int headerHeight;
    /** The width of the row header. */
    private int headerWidth;

    // RESIZING and MOVING

    /** Determines if we are in the middle of moving. */
    private boolean moving;
    /** Determines if we are in the middle of resizing. */
    private boolean resizing;
    /** Determines the direction in which we are (or will start) resizing. */
    private int resizingMode;
    /** The initial point of the current resizing/moving. */
    private Point draggingStart;
    /** The current resizing/moving rectangle. */
    private Rectangle draggingRect;

    /** New grid x coordinate of the resized/moved component. */
    private int newGridX;
    /** New grid y coordinate of the resized/moved component. */
    private int newGridY;
    /** New grid width of the resized component. */
    private int newGridWidth;
    /** New grid height of the resized component. */
    private int newGridHeight;

    // ANIMATION

    /** Animation layer. */
    private AnimationLayer animLayer = new AnimationLayer();
    /** Determines whether the animation is in a progress. */
    boolean animation;
    /** The current phase of the animation. */
    float animPhase;
    /** Information about column/row changes. */
    GridBoundsChange animChange;

    /**
     * Creates new {@code GlassPane}.
     */
    public GlassPane(GridDesigner designer) {
        this.designer = designer;
        setLayout(null);
        Listener listener = new Listener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
        initHeaderWidth();
    }

    /**
     * Sets the underlying container of this glass pane ({@code innerPane})
     * and the subcontainer of {@code innerPane} that contains the grid.
     *
     * @param innerPane underlying container of this glass pane.
     * @param componentPane subcontainer of {@code innerPane} that
     * contains the grid.
     */
    public void setPanes(Container innerPane, Container componentPane) {
        this.innerPane = innerPane;
        this.componentPane = componentPane;
    }

    public boolean isUserActionInProgress() {
        return animation;
    }

    public void updateLayout() {
        performAction(new AbstractGridAction() {
            @Override
            public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
                GridInfoProvider info = gridManager.getGridInfo();
                int oldColumns = info.getColumnCount();
                int oldRows = info.getRowCount();
                GridUtils.removePaddingComponents(gridManager);
                gridManager.updateLayout();
                GridUtils.revalidateGrid(gridManager);
                int newColumns = info.getColumnCount();
                int newRows = info.getRowCount();
                int columns = Math.max(oldColumns, newColumns);
                int rows = Math.max(oldRows, newRows);
                GridUtils.addPaddingComponents(gridManager, columns, rows);
                GridUtils.revalidateGrid(gridManager);
                return null;
            }
        });
    }

    /**
     * Initializes the {@code headerWidth} (i.e., the width of the row header).
     */
    private void initHeaderWidth() {
        // Simple heuristics for the row header width
        JLabel label = new JLabel("99"); // NOI18N
        label.setBorder(BorderFactory.createRaisedBevelBorder());
        headerWidth = label.getPreferredSize().width+HEADER_GAP;
    }

    /**
     * Sets the grid manager.
     *
     * @param gridManager grid manager.
     */
    public void setGridManager(GridManager gridManager) {
        this.gridManager = gridManager;
        this.gridInfo = gridManager.getGridInfo();
        this.customizer = gridManager.getCustomizer(this);
        GridUtils.revalidateGrid(gridManager);
        GridUtils.addPaddingComponents(gridManager, gridInfo.getColumnCount(), gridInfo.getRowCount());
    }

    /**
     * Glass-pane painting.
     *
     * @param g graphics object.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (animation) {
            Point shift = fromComponentPane(new Point());
            g.translate(shift.x, shift.y);
            animLayer.paint(g);
            g.translate(-shift.x, -shift.y);
            animPhase = animLayer.getPhase();
        }
        paintGrid(g);
        if (resizing) {
            paintResizing(g);
        } else if (moving) {
            paintResizing(g);
        } else if (selection != null) {
            paintSelection(g);
        }
        if (animation && (animPhase == 1f)) {
            // End of animation
            animation = false;
            animPhase = 0;
            repaint(); // Repaint without the animation layer (to show selection, etc.)
        }
    }

    /**
     * Paints the grid.
     *
     * @param g graphics object.
     */
    private void paintGrid(Graphics g) {
        if (gridInfo == null) {
            return;
        }

        Color oldColor = g.getColor();
        g.setColor(GRID_COLOR);
        boolean animReady = animation && (animChange != null);
        
        // Calculate column bounds
        int[] columnBounds;
        if (animReady) {
            int[] animOldColumnBounds = animChange.getOldColumnBounds();
            int[] animNewColumnBounds = animChange.getNewColumnBounds();
            columnBounds = new int[animOldColumnBounds.length];
            for (int i=0; i<animOldColumnBounds.length; i++) {
                columnBounds[i] = Math.round(animOldColumnBounds[i]*(1-animPhase)+animPhase*animNewColumnBounds[i]);
            }
        } else {
            columnBounds = gridInfo.getColumnBounds();
        }
        int columns = columnBounds.length-1;

        // Calculate row bounds
        int[] rowBounds;
        if (animReady) {
            int[] animOldRowBounds = animChange.getOldRowBounds();
            int[] animNewRowBounds = animChange.getNewRowBounds();
            rowBounds = new int[animOldRowBounds.length];
            for (int i=0; i<animOldRowBounds.length; i++) {
                rowBounds[i] = Math.round(animOldRowBounds[i]*(1-animPhase)+animPhase*animNewRowBounds[i]);
            }
        } else {
            rowBounds = gridInfo.getRowBounds();
        }

        // Check if there are any newly created columns/rows
        int rows = rowBounds.length-1;
        if (moving || resizing) {
            columns = Math.max(columns, newGridX+newGridWidth);
            rows = Math.max(rows, newGridY+newGridHeight);
        }
        
        // Paint the grid
        Point shift = new Point();
        shift = fromComponentPane(shift);
        int x = columnBounds[0]+shift.x;
        int y = rowBounds[0]+shift.y;
        int width = extendedBound(columnBounds, columns)-columnBounds[0];
        int height = extendedBound(rowBounds, rows)-rowBounds[0];
        for (int i=0; i<=columns; i++) {
            int bound = extendedBound(columnBounds, i);
            g.drawLine(bound+shift.x, y, bound+shift.x, y+height);
        }
        for (int i=0; i<=rows; i++) {
            int bound = extendedBound(rowBounds, i);
            g.drawLine(x, bound+shift.y, x+width, bound+shift.y);
        }
        g.setColor(oldColor);

        // Paint the columns
        Color c = new Color(GRID_COLOR.getRGB()); // Getting rid of transparency
        JLabel header = new JLabel();
        header.setOpaque(true);
        header.setBackground(c);
        header.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i=0; i<columns; i++) {
            header.setText(Integer.toString(i+1));
            header.setBorder(selectedColumns.get(i) ? BorderFactory.createLoweredBevelBorder() : BorderFactory.createRaisedBevelBorder());
            headerHeight = header.getPreferredSize().height;
            int start = extendedBound(columnBounds, i);
            int end = extendedBound(columnBounds, i+1);
            int w = end-start;
            Graphics gg = g.create(start+shift.x, rowBounds[0]-HEADER_GAP-headerHeight+shift.y, w, headerHeight);
            header.setSize(w, headerHeight);
            header.paint(gg);
            gg.dispose();
        }

        // Paint the rows
        for (int i=0; i<rows; i++) {
            header.setText(Integer.toString(i+1));
            Border border = selectedRows.get(i) ? BorderFactory.createLoweredBevelBorder() : BorderFactory.createRaisedBevelBorder();
            header.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(0,HEADER_GAP/2,0,HEADER_GAP/2)));
            int start = extendedBound(rowBounds, i);
            int end = extendedBound(rowBounds, i+1);
            int h = end-start;
            Graphics gg = g.create(columnBounds[0]-HEADER_GAP-headerWidth+shift.x, start+shift.y, headerWidth, h);
            header.setSize(headerWidth, h);
            header.paint(gg);
            gg.dispose();
        }
    }

    /**
     * Paints the selection.
     *
     * @param g graphics object.
     */
    private void paintSelection(Graphics g) {
        if (animation) return;
        Rectangle rect = fromComponentPane(selectionResizingBounds());
        Rectangle inner = fromComponentPane(selection.getBounds());
        g.setColor(HIGHLIGHT_COLOR);
        g.fillRect(rect.x, rect.y, rect.width, inner.y-rect.y);
        g.fillRect(rect.x, inner.y, inner.x-rect.x, inner.height);
        g.fillRect(inner.x+inner.width, inner.y, rect.width-(inner.x+inner.width-rect.x), inner.height);
        g.fillRect(rect.x, inner.y+inner.height, rect.width, rect.height-(inner.y+inner.height-rect.y));
        g.setColor(GridDesigner.SELECTION_COLOR);
        int x = rect.x-1;
        int y = rect.y-1;
        int w = rect.width/2+1;
        int h = rect.height/2+1;
        g.drawRect(x, y, rect.width+1, rect.height+1);
        Image resizeHandle = GridDesigner.RESIZE_HANDLE;
        int rw = resizeHandle.getWidth(null);
        int rh = resizeHandle.getHeight(null);
        g.drawImage(resizeHandle, x-rw, y-rh, null); // left-top
        x += w;
        g.drawImage(resizeHandle, x-rw/2, y-rh, null); // top
        x += rect.width+2-w;
        g.drawImage(resizeHandle, x, y-rh, null); // right-top
        y += h;
        g.drawImage(resizeHandle, x, y-rh/2, null); // right
        y += rect.height+2-h;
        g.drawImage(resizeHandle, x, y, null); // right-bottom
        x -= rect.width+2-w;
        g.drawImage(resizeHandle, x-rw/2, y, null); // bottom
        x -= w;
        g.drawImage(resizeHandle, x-rw, y, null); // left-bottom
        y -= rect.height+2-h;
        g.drawImage(resizeHandle, x-rw, y-rh/2, null); // left
    }

    /**
     * Paints resizing feedback.
     *
     * @param g graphics object.
     */
    private void paintResizing(Graphics g) {
        g.setColor(GridDesigner.SELECTION_COLOR);
        g.drawRect(draggingRect.x, draggingRect.y, draggingRect.width, draggingRect.height);
        g.setColor(HIGHLIGHT_COLOR);
        int[] columnBounds = gridInfo.getColumnBounds();
        int x = extendedBound(columnBounds, newGridX);
        int width = extendedBound(columnBounds, newGridX+newGridWidth)-x;
        int[] rowBounds = gridInfo.getRowBounds();
        int y = extendedBound(rowBounds, newGridY);
        int height = extendedBound(rowBounds, newGridY+newGridHeight)-y;
        Point p = fromComponentPane(new Point(x, y));
        g.fillRect(p.x, p.y, width, height);
    }
    
    private Rectangle selectionResizingBounds() {
        int[] columnBounds = gridInfo.getColumnBounds();
        int[] rowBounds = gridInfo.getRowBounds();
        int gridX = gridInfo.getGridX(selection);
        int gridY = gridInfo.getGridY(selection);
        int gridWidth = gridInfo.getGridWidth(selection);
        int gridHeight = gridInfo.getGridHeight(selection);
        int x = columnBounds[gridX];
        int width = columnBounds[gridX+gridWidth]-x;
        int y = rowBounds[gridY];
        int height = rowBounds[gridY+gridHeight]-y;
        return new Rectangle(x,y,width,height);
    }

    /**
     * Returns the position of the specified grid line (= column/row edge).
     * It returns either the position of the existing line or the suggested
     * position of a newly created line.
     *
     * @param bounds positions of the existing lines.
     * @param index index of the line we are interested in.
     * @return position of the specified grid line.
     */
    private int extendedBound(int[] bounds, int index) {
        int bound;
        if (index<bounds.length) {
            bound = bounds[index];
        } else {
            bound = bounds[bounds.length-1]+(index-bounds.length+1)*GridUtils.PADDING_SIZE;
        }
        return bound;
    }

    /**
     * Determines if we are moving east side of the component during resizing.
     *
     * @return {@code true} is we are moving east side of the component,
     * returns {@code false} otherwise.
     */
    private boolean isResizingEastward() {
        return (resizingMode == SwingConstants.EAST
                || resizingMode == SwingConstants.SOUTH_EAST
                || resizingMode == SwingConstants.NORTH_EAST);
    }

    /**
     * Determines if we are moving west side of the component during resizing.
     *
     * @return {@code true} is we are moving west side of the component,
     * returns {@code false} otherwise.
     */
    private boolean isResizingWestward() {
        return (resizingMode == SwingConstants.WEST
                || resizingMode == SwingConstants.SOUTH_WEST
                || resizingMode == SwingConstants.NORTH_WEST);
    }

    /**
     * Determines if we are moving south side of the component during resizing.
     *
     * @return {@code true} is we are moving south side of the component,
     * returns {@code false} otherwise.
     */
    private boolean isResizingSouthward() {
        return (resizingMode == SwingConstants.SOUTH
                || resizingMode == SwingConstants.SOUTH_EAST
                || resizingMode == SwingConstants.SOUTH_WEST);
    }

    /**
     * Determines if we are moving north side of the component during resizing.
     *
     * @return {@code true} is we are moving north side of the component,
     * returns {@code false} otherwise.
     */
    private boolean isResizingNorthward() {
        return (resizingMode == SwingConstants.NORTH
                || resizingMode == SwingConstants.NORTH_EAST
                || resizingMode == SwingConstants.NORTH_WEST);
    }

    /**
     * Calculates a resizing rectangle (a part of the resizing feedback).
     *
     * @param resizingEnd potential end of the resizing (the current location
     * of the mouse cursor).
     * @return resizing rectangle that is used as a part of the resizing feedback.
     */
    Rectangle calculateResizingRectangle(Point resizingEnd) {
        Rectangle rect = fromComponentPane(selectionResizingBounds());
        int dx = resizingEnd.x - draggingStart.x;
        int dy = resizingEnd.y - draggingStart.y;
        if (isResizingEastward()) {
            rect.width += dx;
            if (rect.width < 0) {
                rect.width = 0;
            }
        }
        if (isResizingSouthward()) {
            rect.height += dy;
            if (rect.height < 0) {
                rect.height = 0;
            }
        }
        if (isResizingWestward()) {
            rect.width -= dx;
            rect.x += dx;
            if (rect.width < 0) {
                rect.x += rect.width;
                rect.width = 0;
            }
        }
        if (isResizingNorthward()) {
            rect.height -= dy;
            rect.y += dy;
            if (rect.height < 0) {
                rect.y += rect.height;
                rect.height = 0;
            }
        }
        return rect;
    }

    /**
     * Calculates the grid location of the current resizing rectangle
     * ({@code draggingRect}).
     */
    void calculateResizingGridLocation() {
        Rectangle rect = toComponentPane(draggingRect);
        int x = gridInfo.getGridX(selection);
        int y = gridInfo.getGridY(selection);
        int width = gridInfo.getGridWidth(selection);
        int height = gridInfo.getGridHeight(selection);

        if (isResizingEastward()) {
            int currentX = gridXLocation(rect.x+rect.width, false);
            newGridWidth = Math.max(1, currentX-x+1);
        }
        if (isResizingWestward()) {
            int currentX = gridXLocation(rect.x, false);
            newGridX = Math.max(0, Math.min(x+width-1, currentX));
            newGridWidth = x+width-newGridX;
        }
        if (isResizingSouthward()) {
            int currentY = gridYLocation(rect.y+rect.height, false);
            newGridHeight = Math.max(1, currentY-y+1);
        }
        if (isResizingNorthward()) {
            int currentY = gridYLocation(rect.y, false);
            newGridY = Math.max(0, Math.min(y+height-1, currentY));
            newGridHeight = y+height-newGridY;
        }
    }

    /**
     * Calculates a moving rectangle (a part of the moving feedback).
     *
     * @param movingEnd potential end of the moving (the current location
     * of the mouse cursor).
     * @return moving rectangle that is used as a part of the moving feedback.
     */
    Rectangle calculateMovingRectangle(Point movingEnd) {
        Rectangle rect = selection.getBounds();
        rect.x += movingEnd.x-draggingStart.x;
        rect.y += movingEnd.y-draggingStart.y;
        return fromComponentPane(rect);
    }

    /**
     * Calculates the grid location of the moved component.
     *
     * @param cursorLocation current position of the cursor.
     */
    void calculateMovingGridLocation(Point cursorLocation) {
        Point start = toComponentPane(draggingStart);
        Point end = toComponentPane(cursorLocation);
        int startX = gridXLocation(start.x, true);
        int startY = gridYLocation(start.y, true);
        int endX = gridXLocation(end.x, false);
        int endY = gridYLocation(end.y, false);
        int deltaX = endX-startX;
        int deltaY = endY-startY;
        newGridX = Math.max(0,gridInfo.getGridX(selection)+deltaX);
        newGridY = Math.max(0,gridInfo.getGridY(selection)+deltaY);
    }

    /**
     * Updates cursor according to its location. For example, it sets
     * the appropriate resizing cursor when the mouse is over the boundary
     * of the selection component.
     *
     * @param cursorLocation current mouse cursor location.
     */
    void updateCursor(Point cursorLocation) {
        Cursor cursor;
        if (cursorLocation == null) {
            cursor = Cursor.getDefaultCursor();
            resizingMode = 0;
        } else {
            int x = cursorLocation.x;
            int y = cursorLocation.y;
            Image resizeHandle = GridDesigner.RESIZE_HANDLE;
            int rw = resizeHandle.getWidth(null);
            int rh = resizeHandle.getHeight(null);
            Rectangle rect = fromComponentPane(selectionResizingBounds());
            boolean w = (rect.x-rw<=x) && (x<=rect.x+rect.width+rw);
            boolean h = (rect.y-rh<=y) && (y<=rect.y+rect.height+rh);
            boolean top = w && (rect.y-rh<=y) && (y<=rect.y+rh);
            boolean bottom = w && (rect.y+rect.height-rh<=y) && (y<=rect.y+rect.height+rh);
            boolean left = h && (rect.x-rw<=x) && (x<=rect.x+rw);
            boolean right = h && (rect.x+rect.width-rw<=x) && (x<=rect.x+rect.width+rw);
            if (top) {
                if (left) {
                    cursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                    resizingMode = SwingConstants.NORTH_WEST;
                } else if (right) {
                    cursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                    resizingMode = SwingConstants.NORTH_EAST;
                } else {
                    cursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                    resizingMode = SwingConstants.NORTH;
                }
            } else if (bottom) {
                if (left) {
                    cursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                    resizingMode = SwingConstants.SOUTH_WEST;
                } else if (right) {
                    cursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                    resizingMode = SwingConstants.SOUTH_EAST;
                } else {
                    cursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                    resizingMode = SwingConstants.SOUTH;
                }
            } else if (left) {
                cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                resizingMode = SwingConstants.WEST;
            } else if (right) {
                cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                resizingMode = SwingConstants.EAST;
            } else {
                cursor = Cursor.getDefaultCursor();
                resizingMode = 0;
            }
        }
        setCursor(cursor);
    }
    
    // Conversion of coordinates

    /**
     * Converts a {@code point} in {@code innerPane} coordinates to
     * a corresponding point in {@code componentPane} coordinates.
     *
     * @param point point in {@code innerPane} coordinates.
     * @return point in {@code componentPane} coordinates.
     */
    Point toComponentPane(Point point) {
        return SwingUtilities.convertPoint(innerPane, point, componentPane);
    }

    /**
     * Converts a {@code rectangle} in {@code innerPane} coordinates to
     * a corresponding rectangle in {@code componentPane} coordinates.
     *
     * @param rectangle rectangle in {@code innerPane} coordinates.
     * @return rectangle in {@code componentPane} coordinates.
     */
    Rectangle toComponentPane(Rectangle rectangle) {
        return SwingUtilities.convertRectangle(innerPane, rectangle, componentPane);
    }

    /**
     * Converts a {@code point} in {@code componentPane} coordinates to
     * a corresponding point in {@code innerPane} coordinates.
     *
     * @param point point in {@code componentPane} coordinates.
     * @return point in {@code innerPane} coordinates.
     */
    Point fromComponentPane(Point point) {
        return SwingUtilities.convertPoint(componentPane, point, innerPane);
    }

    /**
     * Converts a {@code rectangle} in {@code componentPane} coordinates to
     * a corresponding rectangle in {@code innerPane} coordinates.
     *
     * @param rectangle rectangle in {@code componentPane} coordinates.
     * @return rectangle in {@code innerPane} coordinates.
     */
    Rectangle fromComponentPane(Rectangle rectangle) {
        return SwingUtilities.convertRectangle(componentPane, rectangle, innerPane);
    }

    /**
     * Returns {@code x} grid coordinate that correspons to the given
     * pixel coordinate ({@code xComponentPaneCoordinate}).
     *
     * @param xComponentPaneCoordinate pixel coordinate.
     * @param mustBeInside determines whether the returned coordinate
     * must be forced to lay in the existing grid.
     * @return grid {@code x} coordinate.
     */
    private int gridXLocation(int xComponentPaneCoordinate, boolean mustBeInside) {
        int[] bounds = gridInfo.getColumnBounds();
        int gridX = -1;
        while (gridX+1<bounds.length && bounds[gridX+1]<=xComponentPaneCoordinate) {
            gridX++;
        }
        if (mustBeInside) {
            gridX = Math.max(0,Math.min(gridX, bounds.length-2));
        } else if (gridX == bounds.length-1) {
            gridX = (xComponentPaneCoordinate-bounds[bounds.length-1]+1)/GridUtils.PADDING_SIZE+bounds.length-1;
        }
        return gridX;
    }

    /**
     * Returns {@code y} grid coordinate that correspons to the given
     * pixel coordinate ({@code yComponentPaneCoordinate}).
     *
     * @param yComponentPaneCoordinate pixel coordinate.
     * @param mustBeInside determines whether the returned coordinate
     * must be forced to lay in the existing grid.
     * @return grid {@code y} coordinate.
     */
    private int gridYLocation(int yComponentPaneCoordinate, boolean mustBeInside) {
        int[] bounds = gridInfo.getRowBounds();
        int gridY = -1;
        while (gridY+1<bounds.length && bounds[gridY+1]<=yComponentPaneCoordinate) {
            gridY++;
        }
        if (mustBeInside) {
            gridY = Math.max(0,Math.min(gridY, bounds.length-2));
        } else if (gridY == bounds.length-1) {
            gridY = (yComponentPaneCoordinate-bounds[bounds.length-1]+1)/GridUtils.PADDING_SIZE+bounds.length-1;
        }
        return gridY;
    }

    /**
     * Returns the component at the specified point.
     *
     * @param innerPanePoint point where we are looking for a component.
     * @return component at the specified point or {@code null}.
     */
    Component findComponent(Point innerPanePoint) {
        for (Component comp : componentPane.getComponents()) {
            if (GridUtils.isPaddingComponent(comp)) {
                continue;
            }
            Rectangle rect = fromComponentPane(comp.getBounds());
            if (rect.contains(innerPanePoint)) {
                return comp;
            }
        }
        return null;
    }

    /**
     * Returns the index of the column header at the specified point.
     *
     * @param innerPanePoint point whete we are looking for a column header.
     * @return index of the column header or {@code -1}.
     */
    int findColumnHeader(Point innerPanePoint) {
        Point shift = fromComponentPane(new Point());
        int y = gridInfo.getY()+shift.y-HEADER_GAP;
        if (y-headerHeight <= innerPanePoint.y && innerPanePoint.y <= y) {
            int[] bounds = gridInfo.getColumnBounds();
            for (int i=0; i<bounds.length-1; i++) {
                int x = innerPanePoint.x-shift.x;
                if (bounds[i]<=x && x<bounds[i+1]) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the index of the row header at the specified point.
     *
     * @param innerPanePoint point whete we are looking for a row header.
     * @return index of the row header or {@code -1}.
     */
    int findRowHeader(Point innerPanePoint) {
        Point shift = fromComponentPane(new Point());
        int x = gridInfo.getX()+shift.x-HEADER_GAP;
        if (x-headerWidth <= innerPanePoint.x && innerPanePoint.x <= x) {
            int[] bounds = gridInfo.getRowBounds();
            for (int i=0; i<bounds.length-1; i++) {
                int y = innerPanePoint.y-shift.y;
                if (bounds[i]<=y && y<bounds[i+1]) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns {@ DesignerContext} covering the current state of the designer.
     *
     * @return current designer context.
     */
    DesignerContext currentContext() {
        DesignerContext context = new DesignerContext();
        context.setSelectedColumns((BitSet)selectedColumns.clone());
        context.setSelectedRows((BitSet)selectedRows.clone());
        context.setSelectedComponents((selection==null) ? Collections.EMPTY_SET : Collections.singleton(selection));
        context.setGridInfo(gridInfo);
        context.setFocusedRow(focusedCellRow);
        context.setFocusedColumn(focusedCellColumn);
        return context;
    }

    void updateCurrentContext(DesignerContext context) {
        Set<Component> components = context.getSelectedComponents();
        setSelection(components.isEmpty() ? null : components.iterator().next());
    }

    /**
     * Change the grid location of the selected component according
     * to data collected during moving/resizing.
     */
    void changeLocation() {
        int oldX = gridInfo.getGridX(selection);
        int oldY = gridInfo.getGridY(selection);
        int oldWidth = gridInfo.getGridWidth(selection);
        int oldHeight = gridInfo.getGridHeight(selection);
        boolean xChanged = (oldX != newGridX);
        boolean yChanged = (oldY != newGridY);
        boolean heightChanged = (oldHeight != newGridHeight);
        boolean widthChanged = (oldWidth != newGridWidth);
        if (xChanged || yChanged || widthChanged || heightChanged) {
            performAction(new ChangeLocationAction());
        }
    }

    @Override
    public void performAction(GridAction action) {
        GridActionWrapper wrapper = new GridActionWrapper(action);
        wrapper.setDesignerContext(currentContext());
        wrapper.actionPerformed(null);
    }

    void setSelection(Component selection) {
        if (selection == this.selection) {
            return;
        }
        if (selection == null) {
            updateCursor(null);
        }
        this.selection = selection;
        designer.setSelection(selection);
    }

    /**
     * Listener for glass-pane user gestures.
     */
    class Listener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            Point point = e.getPoint();
            draggingStart = point;
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (resizingMode == 0) {
                    // Component selection
                    setSelection(findComponent(point));
                    // Column selection
                    int column = findColumnHeader(point);
                    if (column != -1) {
                        selectedColumns.flip(column);
                    }
                    // Row selection
                    int row = findRowHeader(point);
                    if (row != -1) {
                        selectedRows.flip(row);
                    }
                } else {
                    // Resizing (start)
                    resizing = true;
                    draggingRect = fromComponentPane(selectionResizingBounds());
                    newGridX = gridInfo.getGridX(selection);
                    newGridY = gridInfo.getGridY(selection);
                    newGridHeight = gridInfo.getGridHeight(selection);
                    newGridWidth = gridInfo.getGridWidth(selection);
                }
            }
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            Point point = e.getPoint();
            if (moving) {
                moving = false;
                changeLocation();
            } else if (resizing) {
                resizing = false;
                changeLocation();
            } else {
                if (SwingUtilities.isRightMouseButton(e)) {
                    List<GridAction> actions = null;
                    // Component actions
                    setSelection(findComponent(point));
                    DesignerContext context = currentContext();
                    if (selection != null) {
                        context.setFocusedComponent(selection);
                        actions = gridManager.designerActions(GridAction.Context.COMPONENT);
                    }

                    // Column actions
                    int column = findColumnHeader(point);
                    context.setFocusedColumn(column);
                    if (column != -1) {
                        actions = gridManager.designerActions(GridAction.Context.COLUMN);
                    }

                    // Row actions
                    int row = findRowHeader(point);
                    context.setFocusedRow(row);
                    if (row != -1) {
                        actions = gridManager.designerActions(GridAction.Context.ROW);
                    }

                    // Grid actions
                    if ((selection == null) && (column == -1) && (row == -1)) {
                        Point shift = fromComponentPane(new Point());
                        int x = gridInfo.getX();
                        int y = gridInfo.getY();
                        int width = gridInfo.getWidth();
                        int height = gridInfo.getHeight();
                        Rectangle rect = new Rectangle(x+shift.x, y+shift.y, width, height);
                        if (rect.contains(point)) {
                            focusedCellColumn = gridXLocation(point.x-shift.x, true);
                            focusedCellRow = gridYLocation(point.y-shift.y, true);
                            context.setFocusedColumn(focusedCellColumn);
                            context.setFocusedRow(focusedCellRow);
                            actions = gridManager.designerActions(GridAction.Context.CELL);
                        }
                    } else {
                        focusedCellColumn = -1;
                        focusedCellRow = -1;
                    }

                    if (actions != null) {
                        JPopupMenu menu = new JPopupMenu();
                        for (GridAction action : actions) {
                            JMenuItem menuItem = action.getPopupPresenter(GlassPane.this);
                            if (menuItem == null) {
                                GridActionWrapper wrapper = new GridActionWrapper(action);
                                wrapper.setDesignerContext(context);
                                menu.add(wrapper);
                            } else {
                                menu.add(menuItem);
                            }
                        }
                        menu.show(GlassPane.this, point.x, point.y);
                    }

                }
            }
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (selection != null) {
                updateCursor(e.getPoint());
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (resizing) {
                draggingRect = calculateResizingRectangle(e.getPoint());
                calculateResizingGridLocation();
            } else if (selection != null) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (!moving) {
                        // Moving start
                        newGridHeight = gridInfo.getGridHeight(selection);
                        newGridWidth = gridInfo.getGridWidth(selection);
                    }
                    moving = true;
                    draggingRect = calculateMovingRectangle(e.getPoint());
                    calculateMovingGridLocation(e.getPoint());
                }
            }
            repaint();
        }

    }

    /**
     * Adapts {@code GridAction} to be a {@code javax.swing.Action}
     * and supports animated changes of the layout and the grid.
     */
    class GridActionWrapper extends AbstractAction {
        /** Wrapped {@code GridAction}. */
        private GridAction delegate;
        /** The designer context given to this action. */
        private DesignerContext currentContext;

        /**
         * Creates a new wrapper around the specified action.
         *
         * @param action action to wrap.
         */
        GridActionWrapper(GridAction action) {
            this.delegate = action;
        }

        /**
         * Gives a designer context to this action.
         *
         * @param currentContext current designer context.
         */
        public void setDesignerContext(DesignerContext currentContext) {
            this.currentContext = currentContext;
            setEnabled(delegate.isEnabled(currentContext));
        }

        /**
         * Invokes the wrapped action and animates its changes.
         *
         * @param e action event.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            animation = true;
            int[] animOldColumnBounds = gridInfo.getColumnBounds();
            int[] animOldRowBounds = gridInfo.getRowBounds();
            animLayer.setContainer(componentPane);
            animLayer.setGlassPane(GlassPane.this);
            animLayer.setVIPComponents((selection == null) ? Collections.EMPTY_LIST : Collections.singletonList(selection));
            animLayer.loadStart();
            GridBoundsChange change = delegate.performAction(gridManager, currentContext);
            updateCurrentContext(currentContext);
            animLayer.loadEnd();
            if (change == null) {
                int[] animNewColumnBounds = gridInfo.getColumnBounds();
                int[] animNewRowBounds = gridInfo.getRowBounds();
                if (animNewColumnBounds.length != animOldColumnBounds.length) {
                    if (animNewColumnBounds.length > animOldColumnBounds.length) {
                        animOldColumnBounds = extendArray(animOldColumnBounds, animNewColumnBounds.length);
                    } else {
                        animNewColumnBounds = extendArray(animNewRowBounds, animOldColumnBounds.length);
                    }
                }
                if (animNewRowBounds.length != animOldRowBounds.length) {
                    if (animNewRowBounds.length > animOldRowBounds.length) {
                        animOldRowBounds = extendArray(animOldRowBounds, animNewRowBounds.length);
                    } else {
                        animNewRowBounds = extendArray(animNewRowBounds, animOldRowBounds.length);
                    }
                }
                change = new GridBoundsChange(animOldColumnBounds, animOldRowBounds, animNewColumnBounds, animNewRowBounds);
            }
            
            // Update customizer
            if (customizer != null) {
                DesignerContext context = currentContext();
                customizer.setContext(context);                
            }
            animChange = change;
            animLayer.animate();
        }

        private int[] extendArray(int[] original, int newLength) {
            int[] result = new int[newLength];
            System.arraycopy(original, 0, result, 0, original.length);
            int last = original[original.length-1];
            for (int i=original.length; i<newLength; i++) {
                result[i] = last;
            }
            return result;
        }

        @Override
        public Object getValue(String key) {
            Object value = delegate.getValue(key);
            if (value == null) {
                value = super.getValue(key);
            }
            return value;
        }

    }

    /**
     * Action that changes location of the selected component.
     */
    class ChangeLocationAction extends AbstractGridAction {

        @Override
        public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
            GridInfoProvider info = gridManager.getGridInfo();
            int[] originalColumnBounds = info.getColumnBounds();
            int[] originalRowBounds = info.getRowBounds();

            int columns = Math.max(info.getColumnCount(), newGridX+newGridWidth);
            int rows = Math.max(info.getRowCount(), newGridY+newGridHeight);
            boolean xChanged = (info.getGridX(selection) != newGridX);
            boolean yChanged = (info.getGridY(selection) != newGridY);
            boolean heightChanged = (info.getGridHeight(selection) != newGridHeight);
            boolean widthChanged = (info.getGridWidth(selection) != newGridWidth);
            GridUtils.removePaddingComponents(gridManager);
            if (xChanged) {
                gridManager.setGridX(selection, newGridX);
            }
            if (yChanged) {
                gridManager.setGridY(selection, newGridY);
            }
            if (widthChanged) {
                gridManager.setGridWidth(selection, newGridWidth);
            }
            if (heightChanged) {
                gridManager.setGridHeight(selection, newGridHeight);
            }
            GridUtils.addPaddingComponents(gridManager, columns, rows);
            GridUtils.revalidateGrid(gridManager);
            int[] newColumnBounds = gridInfo.getColumnBounds();
            int[] newRowBounds = gridInfo.getRowBounds();
            if (newColumnBounds.length>originalColumnBounds.length) {
                // Moving/resizing outside the original grid
                int[] oldBounds = new int[newColumnBounds.length];
                for (int i=0; i<oldBounds.length; i++) {
                    oldBounds[i] = extendedBound(originalColumnBounds, i);
                }
                originalColumnBounds = oldBounds;
            }
            if (newRowBounds.length>originalRowBounds.length) {
                // Moving/resizing outside the original grid
                int[] oldBounds = new int[newRowBounds.length];
                for (int i=0; i<oldBounds.length; i++) {
                    oldBounds[i] = extendedBound(originalRowBounds, i);
                }
                originalRowBounds = oldBounds;
            }

            return new GridBoundsChange(originalColumnBounds, originalRowBounds, newColumnBounds, newRowBounds);
        }

    }


}
