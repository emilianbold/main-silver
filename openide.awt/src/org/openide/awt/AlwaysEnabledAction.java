package org.openide.awt;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.netbeans.modules.openide.util.ActionsBridge;
import org.netbeans.modules.openide.util.ActionsBridge.ActionRunnable;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbPreferences;
import org.openide.util.actions.Presenter;

/** Lazily initialized always enabled action
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class AlwaysEnabledAction extends AbstractAction
implements PropertyChangeListener, ContextAwareAction {

    // -J-Dorg.openide.awt.AlwaysEnabledAction.level=FINE
    private static final Logger LOG = Logger.getLogger(AlwaysEnabledAction.class.getName());

    private static final String PREFERENCES_NODE = "preferencesNode"; // NOI18N

    private static final String PREFERENCES_KEY = "preferencesKey"; // NOI18N

    static AlwaysEnabledAction create(Map m) {
        return (m.containsKey(PREFERENCES_KEY)) ? new CheckBox(m) : new AlwaysEnabledAction(m);
    }

    final Map map;
    ActionListener delegate;
    final Lookup context;
    final Object equals;

    public AlwaysEnabledAction(Map m) {
        super();
        this.map = m;
        this.context = null;
        this.equals = this;
    }

    AlwaysEnabledAction(Map m, ActionListener delegate, Lookup context, Object equals) {
        super();
        this.map = m;
        this.delegate = bindToContext(delegate, context);
        this.context = context;
        this.equals = equals;
    }

    private static ActionListener bindToContext(ActionListener a, Lookup context) {
        if (context != null) {
            if (a instanceof ContextAwareAction) {
                return ((ContextAwareAction)a).createContextAwareInstance(context);
            }
        }
        return a;
    }

    protected ActionListener getDelegate() {
        if (delegate == null) {
            Object listener = map.get("delegate"); // NOI18N
            if (!(listener instanceof ActionListener)) {
                throw new NullPointerException();
            }
            delegate = bindToContext((ActionListener)listener, context);
            if (delegate instanceof Action) {
                Action actionDelegate = (Action) delegate;
                actionDelegate.addPropertyChangeListener(this);
                // Ensure display names and other properties are in sync or propagate them
                syncActionDelegateProperty(Action.NAME, actionDelegate);
            }
        }
        return delegate;
    }

    private void syncActionDelegateProperty(String propertyName, Action actionDelegate) {
        Object value = extractCommonAttribute(map, this, propertyName);
        Object delegateValue = actionDelegate.getValue(propertyName);
        if (value != null) {
            if (delegateValue == null) {
                actionDelegate.putValue(propertyName, value);
            } else {
                if (!delegateValue.equals(value)) { // Values differ
                    LOG.log(Level.FINE, "Value of property \"{0}\" of AlwaysEnabledAction " +
                            "is \"{1}\" but delegate {2} has \"{3}\"",
                            new Object[] {propertyName, value, delegate, delegateValue});
                }
            }
        } // else either both values are null or
        // this has null and delegate has non-null which is probably fine (declarer does not care)
    }

    @Override
    public boolean isEnabled() {
//        assert EventQueue.isDispatchThread();
        if (delegate instanceof Action) {
            return ((Action)delegate).isEnabled();
        }
        return true;
    }

    public void actionPerformed(final ActionEvent e) {
        assert EventQueue.isDispatchThread();
        if (getDelegate() instanceof Action) {
            if (!((Action)getDelegate()).isEnabled()) {
                Toolkit.getDefaultToolkit().beep();
                // Do not fire newValue == null (see #165838)
                firePropertyChange("enabled", null, isEnabled()); // NOI18N
                return;
            }
        }

        boolean async = Boolean.TRUE.equals(map.get("asynchronous")); // NOI18N
        ActionRunnable ar = new ActionRunnable(e, this, async) {
            @Override
            protected void run() {
                getDelegate().actionPerformed(e);
            }
        };
        ActionsBridge.doPerformAction(this, ar);
    }

    @Override
    public Object getValue(String name) {
        if (delegate instanceof Action) {
            Object ret = ((Action)delegate).getValue(name);
            if (ret != null) {
                return ret;
            }
            if (
                "iconBase".equals(name) && // NOI18N
                ((Action)delegate).getValue(Action.SMALL_ICON) != null
            ) {
                return null;
            }
        }
        Object o = extractCommonAttribute(map, this, name);
        // cf. #137709 JG18:
        return o != null ? o : super.getValue(name);
    }

    static final Object extractCommonAttribute(Map fo, Action action, String name) {
        if (Action.NAME.equals(name)) {
            String actionName = (String) fo.get("displayName"); // NOI18N
            // NOI18N
            //return Actions.cutAmpersand(actionName);
            return actionName;
        }
        if (Action.MNEMONIC_KEY.equals(name)) {
            String actionName = (String) fo.get("displayName"); // NOI18N
            // NOI18N
            int position = Mnemonics.findMnemonicAmpersand(actionName);
            if (position == -1) {
                return null;
            } else {
                // #167996: copied from AbstractButton.setMnemonic
                int vk = (int) actionName.charAt(position + 1);
                if(vk >= 'a' && vk <='z') { //NOI18N
                    vk -= ('a' - 'A'); //NOI18N
                }
                return vk;
            }
        }
        if (Action.SMALL_ICON.equals(name)) {
            Object icon = fo == null ? null : fo.get("iconBase"); // NOI18N
            if (icon instanceof Icon) {
                return (Icon) icon;
            }
            if (icon instanceof URL) {
                icon = Toolkit.getDefaultToolkit().getImage((URL)icon);
            }
            if (icon instanceof Image) {
                return ImageUtilities.image2Icon((Image)icon);
            }
            if (icon instanceof String) {
                return ImageUtilities.loadImageIcon((String)icon, false);
            }
        }
        if ("iconBase".equals(name)) { // NOI18N
            return fo == null ? null : fo.get("iconBase"); // NOI18N
        }
        if ("noIconInMenu".equals(name)) { // NOI18N
            return fo == null ? null : fo.get("noIconInMenu"); // NOI18N
        }
        // Delegate query to other properties to "fo" ignoring special properties
        if (!"delegate".equals(name) && !"instanceCreate".equals(name)) {
            return fo == null ? null : fo.get(name);
        }

        return null;
    }


    @Override
    public int hashCode() {
        if (equals == this) {
            return super.hashCode();
        }
        return equals.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AlwaysEnabledAction) {
            final AlwaysEnabledAction other = (AlwaysEnabledAction) obj;
            if (this == this.equals && other == other.equals) {
                return (this == other);
            }

            if (this.equals.equals(other.equals)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "AlwaysEnabledAction[" + getValue(Action.NAME) + "]"; // NOI18N
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == delegate) {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new AlwaysEnabledAction(map, delegate, actionContext, equals);
    }

    static final class CheckBox extends AlwaysEnabledAction
            implements Presenter.Menu, Presenter.Popup, PreferenceChangeListener, LookupListener
    {

        private static final long serialVersionUID = 1L;

        private static final ActionListener EMPTY = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                // Do nothing
            }
        };

        private JCheckBoxMenuItem menuItem;

        private JCheckBoxMenuItem popupItem;

        private Preferences preferencesNode;

        private Lookup.Result<Preferences> preferencesNodeResult;

        private boolean prefsListening;

        CheckBox(Map m) {
            super(m);
        }

        CheckBox(Map m, ActionListener delegate, Lookup context, Object equals) {
            super(m, delegate, context, equals);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Toggle state in preferences
            togglePreferencesSelected();

            super.actionPerformed(e);
        }

        public JMenuItem getMenuPresenter() {
            if (menuItem == null) {
                menuItem = new JCheckBoxMenuItem();
                menuItem.setSelected(isPreferencesSelected());
                Actions.connect(menuItem, this, false);
            }
            return menuItem;
        }

        public JMenuItem getPopupPresenter() {
            if (popupItem == null) {
                popupItem = new JCheckBoxMenuItem();
                popupItem.setSelected(isPreferencesSelected());
                Actions.connect(popupItem, this, true);
            }
            return popupItem;
        }

        public void preferenceChange(PreferenceChangeEvent pce) {
            updateItemsSelected();
        }

        @Override
        protected ActionListener getDelegate() {
            return EMPTY;
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new CheckBox(map, delegate, actionContext, equals);
        }

        private boolean isPreferencesSelected() {
            String key = (String) getValue(PREFERENCES_KEY);
            Preferences prefs = prefs();
            boolean value;
            if (key != null && prefs != null) {
                value = prefs.getBoolean(key, false);
                synchronized (this) {
                    if (!prefsListening) {
                        prefsListening = true;
                        prefs.addPreferenceChangeListener(this);
                    }
                }
            } else {
                value = false;
            }
            return value;
        }

        private void updateItemsSelected() {
            boolean selected = isPreferencesSelected();
            if (menuItem != null) {
                menuItem.setSelected(selected);
            }
            if (popupItem != null) {
                popupItem.setSelected(selected);
            }
        }

        private synchronized Preferences prefs() {
            if (preferencesNode == null) {
                Object prefsNodeOrLookup = getValue(PREFERENCES_NODE);
                if (prefsNodeOrLookup instanceof String) {
                    String nodeName = (String) prefsNodeOrLookup;
                    if (nodeName.startsWith("system:")) {
                        preferencesNode = Preferences.systemRoot();
                        if (preferencesNode != null) {
                            nodeName = nodeName.substring("system:".length());
                            try {
                                preferencesNode = preferencesNode.nodeExists(nodeName) ? preferencesNode.node(nodeName) : null;
                            } catch (BackingStoreException ex) {
                                preferencesNode = null;
                            }
                        }
                    } else if (nodeName.startsWith("user:")) {
                        preferencesNode = Preferences.userRoot();
                        if (preferencesNode != null) {
                            nodeName = nodeName.substring("user:".length());
                            try {
                                preferencesNode = preferencesNode.nodeExists(nodeName) ? preferencesNode.node(nodeName) : null;
                            } catch (BackingStoreException ex) {
                                preferencesNode = null;
                            }
                        }
                    } else {
                        preferencesNode = NbPreferences.root();
                        if (preferencesNode != null) {
                            try {
                                preferencesNode = preferencesNode.nodeExists(nodeName) ? preferencesNode.node(nodeName) : null;
                            } catch (BackingStoreException ex) {
                                preferencesNode = null;
                            }
                        }
                    }

                } else if (prefsNodeOrLookup instanceof Preferences) {
                    preferencesNode = (Preferences) prefsNodeOrLookup;
                } else if (prefsNodeOrLookup instanceof Lookup) {
                    Lookup prefsLookup = (Lookup) prefsNodeOrLookup;
                    preferencesNodeResult = prefsLookup.lookupResult(Preferences.class);
                    Collection<? extends Preferences> instances = preferencesNodeResult.allInstances();
                    if (instances.size() > 0) {
                        preferencesNode = instances.iterator().next();
                        preferencesNodeResult.addLookupListener(this);
                    }
                    return prefsLookup.lookup(Preferences.class);
                } else {
                    preferencesNode = null;
                }
            }
            return preferencesNode;
        }

        public void resultChanged(LookupEvent ev) {
            preferencesNode = null;
            preferencesNodeResult = null;
            updateItemsSelected();
        }

        private void togglePreferencesSelected() {
            String key = (String) getValue(PREFERENCES_KEY);
            Preferences prefs = prefs();
            if (key != null && prefs != null) {
                prefs.putBoolean(key, !prefs.getBoolean(key, false));
            }
        }

    }

}
