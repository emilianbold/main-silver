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
package org.netbeans.modules.java.navigation.base;

import java.util.Collection;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.navigation.actions.SortActions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Zezula
 */
public abstract class Filters<T> {
    
    private static final String PROP_NATURAL_SORT = "naturalSort";  //NOI18N

    private volatile boolean naturalSort;
    //@NotThreadSafe
    private FiltersManager filtersManager;
    //@NotThreadSafe
    private JToggleButton sortByNameButton;
    //@NotThreadSafe
    private JToggleButton sortByPositionButton;

    protected Filters() {
        naturalSort = NbPreferences.forModule(this.getClass()).getBoolean( PROP_NATURAL_SORT, false );
    }

    public final boolean isNaturalSort() {
        return naturalSort;
    }

    public final void setNaturalSort(final boolean naturalSort) {        
        this.naturalSort = naturalSort;
        NbPreferences.forModule(this.getClass()).putBoolean( PROP_NATURAL_SORT, naturalSort );
        if( null != sortByNameButton ) {
            sortByNameButton.setSelected(!naturalSort);
        }
        if( null != sortByPositionButton ) {
            sortByPositionButton.setSelected(naturalSort);
        }
        sortUpdated();
    }

    public final JComponent getComponent() {
        final FiltersManager fm = getFiltersManager();
        final AbstractButton[] customButtons = createCustomButtons();
        final AbstractButton[] sortButtons = createSortButtons();
        final AbstractButton[] buttons;
        if (customButtons.length == 0) {
            buttons = sortButtons;
        } else {
            buttons = new AbstractButton[customButtons.length + sortButtons.length];
            System.arraycopy(customButtons, 0, buttons, 0, customButtons.length);
            System.arraycopy(sortButtons, 0, buttons, customButtons.length, sortButtons.length);
        }
        return fm.getComponent(buttons);
    }

    public final FiltersManager getFiltersManager() {
        assert SwingUtilities.isEventDispatchThread();
        if (filtersManager == null) {
            filtersManager = createFilters();
        }
        return filtersManager;
    }

    public abstract Collection<T> filter( Collection<? extends T> original);
    
    protected abstract FiltersManager createFilters ();
    
    protected abstract void sortUpdated();

    @NonNull
    protected AbstractButton[] createCustomButtons() {
        return new AbstractButton[0];
    }

    private JToggleButton[] createSortButtons() {
        assert SwingUtilities.isEventDispatchThread();
        JToggleButton[] res = new JToggleButton[2];
        if( null == sortByNameButton ) {
            sortByNameButton = new JToggleButton(SortActions.createSortByNameAction(this));
            sortByNameButton.setToolTipText(sortByNameButton.getText());
            sortByNameButton.setText(null);
            sortByNameButton.setSelected( !isNaturalSort());
            sortByNameButton.setFocusable( false );
        }
        res[0] = sortByNameButton;

        if( null == sortByPositionButton ) {
            sortByPositionButton = new JToggleButton(SortActions.createSortBySourceAction(this));
            sortByPositionButton.setToolTipText(sortByPositionButton.getText());
            sortByPositionButton.setText(null);
            sortByPositionButton.setSelected(isNaturalSort());
            sortByPositionButton.setFocusable( false );
        }
        res[1] = sortByPositionButton;
        return res;
    }
}
