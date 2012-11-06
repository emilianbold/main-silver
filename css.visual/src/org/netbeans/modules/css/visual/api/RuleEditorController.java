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
package org.netbeans.modules.css.visual.api;

import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import javax.swing.JComponent;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.visual.RuleEditorPanel;
import org.openide.util.Mutex;
import org.openide.util.Parameters;

/**
 * Rule editor panel controller.
 * 
 * Allows to control and listen on the Rule Editor UI.
 * 
 * An example of the usage:
 * <pre>
 * RuleEditorController controller = RuleEditorController.createInstance();
 * JComponent ruleEditor = controller.getRuleEditorController();
 * 
 * yourContainer.add(ruleEditor);
 * 
 * Model cssModel = ...;
 * Rule cssModelRule = ...;
 * 
 * //setup the content
 * controller.setModel(cssModel);
 * controller.setRule(cssModelRule);
 * 
 * //listen on changed
 * controller.addRuleEditorListener(yourListener);
 * 
 * //modify the UI
 * controller.setSortMode(SortMode.NATURAL);
 * controller.setShowCategorie(true);
 * 
 * ...
 * 
 * </pre>
 * 
 * All the {@link RuleEditorController} methods may be called from a non AWT thread.
 * 
 * TODO:
 * 1) consider an ability to get the filters panel and place it to a component outside
 *    of the RuleEditorPanel. Possibly configure the filters themselves.
 * 
 *
 * @author marekfukala
 */
public final class RuleEditorController {
    
    /**
     * Property change support event keys.
     */
    public enum PropertyNames {
        /**
         * Fired when one calls {@link RuleEditorController#setModel(org.netbeans.modules.css.model.api.Model)}
         */
        MODEL_SET,
        /**
         * Fired when one calls {@link RuleEditorController#setRule(org.netbeans.modules.css.model.api.Rule)}
         */
        RULE_SET
        
        //TODO add more
    }
    
    private RuleEditorPanel peer;

    /**
     * Creates a new instance of the controller. 
     * 
     * One controller is paired with one rule editor UI component.
     * 
     * @return non null value
     */
    public static RuleEditorController createInstance() {
        return new RuleEditorController(new RuleEditorPanel());
    }
    
    private RuleEditorController(RuleEditorPanel peer) {
        this.peer = peer;
    }
    
    /**
     * Gets the rule editor UI component.
     * @return non null value
     */
    public JComponent getRuleEditorComponent() {
        return peer;
    }
    
    RuleEditorPanel getRuleEditorPanel() {
        return peer;
    }
    
    /**
     * Sets the css source model to the {@link RuleEditorPanel}.
     * 
     * All subsequent actions refers to this model.
     * 
     * Note: The implementation tries to find a rule corresponding to the previously
     * active rule and set it as active.
     * 
     * @param cssSourceModel an instance of {@link Model}
     */
    public void setModel(final Model cssSourceModel) {
        RuleEditorPanel.LOG.log(Level.FINE, "setModel({0}) called by {1}", new Object[]{cssSourceModel, Thread.currentThread().getStackTrace()[2].toString()});
        Parameters.notNull("cssSourceModel", cssSourceModel);
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                peer.setModel(cssSourceModel);
            }
        });
    }
    
    /**
     * Sets the given css rule as the context.
     * 
     * @param rule a non null instance of {@link Rule). <b>MUST belong to the selected css model instance!</b>
     */
    public void setRule(final Rule rule) {
        RuleEditorPanel.LOG.log(Level.FINE, "setRule({0}) called by {1}", new Object[]{rule, Thread.currentThread().getStackTrace()[2].toString()});
        Parameters.notNull("rule", rule);
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                peer.setRule(rule);
            }
        });
    }
    
    /**
     * Switches the panel to the 'no selected rule mode'. 
     * The panel will show some informational message instead of the css rule properties.
     */
    public void setNoRuleState() {
        RuleEditorPanel.LOG.log(Level.FINE, "setNoRuleState() called by {0}", new Object[]{Thread.currentThread().getStackTrace()[2].toString()});
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                peer.setNoRuleState();
            }
        });
    }
    
    /**
     * Associates an instance of {@link DeclarationInfo} to a {@link Declaration}.
     * 
     * The instance of {@link Declaration} must be a member of {@link Rule} set 
     * previously by {@link #setRule(org.netbeans.modules.css.model.api.Rule).
     * 
     * @param declaration An instance of {@link Declaration}
     * @param declarationInfo  An instance of {@link DeclarationInfo}. May be null 
     * to clear the Declaration-DeclarationInfo association.
     */
    public void setDeclarationInfo(final Declaration declaration, final DeclarationInfo declarationInfo) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                peer.setDeclarationInfo(declaration, declarationInfo);
            }
        });
    }
    
    /**
     * Sets the sort mode of the rule editor. 
     * @see SortMode
     * @param sortMode 
     */
    public void setSortMode(SortMode sortMode) {
        peer.setSortMode(sortMode);
    }
    
    /**
     * Show physical propertis + all existing unused properties.
     * 
     * Shows physical properties of the selected rule first, then all
     * of the existing unused properties. The unused the properties are sorted
     * alphabetically.
     */
    public void setShowAllProperties(boolean enabled) {
        peer.setShowAllProperties(enabled);
    }
    
     /**
     * Show property categories.
     * 
     * Shows categories for the css properties. In each category the physical
     * properties of the selected rule are show first, then the rest of 
     * existing css properties belonging to the category. All unused properties 
     * sorted  alphabetically, categories also sorted alphabetically.
     */
    public void setShowCategories(boolean enabled) {
        peer.setShowCategories(enabled);
    }
    
    /**
     * Registers an instance of {@link PropertyChangeListener} to the component.
     * @param listener
     */
    public void addRuleEditorListener(PropertyChangeListener listener) {
        peer.addRuleEditorListener(listener);
    }
    
    /**
     * Unregisters an instance of {@link PropertyChangeListener} from the component.
     * @param listener
     */
    public void removeRuleEditorListener(PropertyChangeListener listener) {
        peer.removeRuleEditorListener(listener);
    }
    
    
}
