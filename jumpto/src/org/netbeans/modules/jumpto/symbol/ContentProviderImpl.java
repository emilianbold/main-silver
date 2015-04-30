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
package org.netbeans.modules.jumpto.symbol;

import java.awt.Dialog;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.jumpto.common.AbstractModelFilter;
import org.netbeans.modules.jumpto.common.CurrentSearch;
import org.netbeans.modules.jumpto.common.ItemRenderer;
import org.netbeans.modules.jumpto.common.Models;
import org.netbeans.modules.jumpto.common.Utils;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
final class ContentProviderImpl implements GoToPanel.ContentProvider {

    private static final Logger LOG = Logger.getLogger(ContentProviderImpl.class.getName());
    private static final RequestProcessor rp = new RequestProcessor (ContentProviderImpl.class);

    private final JButton okButton;
    private final AtomicReference<Collection<? extends SymbolProvider>> typeProviders = new AtomicReference<>();
    private final CurrentSearch<SymbolDescriptor> currentSearch = new CurrentSearch<>(
        new Callable<AbstractModelFilter<SymbolDescriptor>>() {
            @NonNull
            @Override
            public AbstractModelFilter<SymbolDescriptor> call() throws Exception {
                return new AbstractModelFilter<SymbolDescriptor>() {
                    @NonNull
                    @Override
                    protected String getItemValue(@NonNull final SymbolDescriptor item) {
                        String name = item.getSimpleName();
                        if (name == null) {
                            //The SymbolDescriptor does not provide simple name
                            //the symbol name contains parameter names, so it's needed to strip them
                            name = item.getSymbolName();
                            final String[] nameParts = name.split("\\s+|\\(");  //NOI18N
                            name = nameParts[0];
                        }
                        return name;
                    }
                    @Override
                    protected void update(@NonNull final SymbolDescriptor item) {
                        String searchText = getSearchText();
                        if (searchText == null) {
                            searchText = "";    //NOI18N
                        }
                        SymbolProviderAccessor.DEFAULT.setHighlightText(item, searchText);
                    }
                };
            }
        });
    //@GuardedBy("this")
    private RequestProcessor.Task task;
    //@GuardedBy("this")
    private Worker running;
    //threading: accessed only in EDT
    private Dialog dialog;


    public ContentProviderImpl(final JButton okButton) {
        this.okButton = okButton;
    }


    void setDialog(final Dialog dialog) {
        this.dialog = dialog;
    }


    @Override
    public ListCellRenderer getListCellRenderer(
            @NonNull final JList list,
            @NonNull final ButtonModel caseSensitive) {
        Parameters.notNull("list", list);   //NOI18N
        Parameters.notNull("caseSensitive", caseSensitive); //NOI18N
        return ItemRenderer.Builder.create(
            list,
            caseSensitive,
            new SymbolDescriptorCovertor()).build();
    }

    @Override
    public boolean setListModel(GoToPanel panel, String text) {
        enableOK(false);
        final Worker workToCancel;
        final RequestProcessor.Task  taskToCancel;
        synchronized (this) {
            workToCancel = running;
            taskToCancel = task;
            running = null;
            task = null;
        }
        if (workToCancel != null) {
                workToCancel.cancel();
        }
        if (taskToCancel != null) {
                taskToCancel.cancel();
        }

        if ( text == null ) {
            currentSearch.resetFilter();
            panel.setModel(new DefaultListModel());
            return false;
        }
        final boolean exact = text.endsWith(" "); // NOI18N
        final boolean isCaseSensitive = panel.isCaseSensitive();
        text = text.trim();
        if ( text.length() == 0 || !Utils.isValidInput(text)) {
            currentSearch.resetFilter();
            panel.setModel(new DefaultListModel());
            return false;
        }
        final SearchType searchType = Utils.getSearchType(text, exact, isCaseSensitive, null, null);
        if (searchType == SearchType.REGEXP || searchType == SearchType.CASE_INSENSITIVE_REGEXP) {
            text = Utils.removeNonNeededWildCards(text);
        }
        final Pair<String,String> nameAndScope = Utils.splitNameAndScope(text.trim());
        final String name = nameAndScope.first();
        final String scope = nameAndScope.second();
        if (name.length() == 0) {
            //Empty name, wait for next char
            currentSearch.resetFilter();
            panel.setModel(new DefaultListModel());
            return false;
        }
        // Compute in other thread
        synchronized(this) {
            if (currentSearch.isNarrowing(searchType, text, scope)) {
                currentSearch.filter(searchType, name);
                enableOK(panel.revalidateModel());
                return false;
            } else {
                running = new Worker(text, searchType, panel);
                task = rp.post( running, 220);
                if ( panel.time != -1 ) {
                    LOG.log(
                       Level.FINE,
                       "Worker posted after {0} ms.",   //NOI18N
                       System.currentTimeMillis() - panel.time);
                }
                return true;
            }
        }
    }

    @Override
    public void closeDialog() {
        if (dialog != null) {
            dialog.setVisible( false );
            DialogFactory.storeDialogDimensions(
                    new Dimension(dialog.getWidth(), dialog.getHeight()));
            dialog.dispose();
            dialog = null;
            cleanUp();
        }
    }

    @Override
    public boolean hasValidContent() {
        return this.okButton.isEnabled();
    }

    private void enableOK(final boolean enabled) {
        if (okButton != null) {
            okButton.setEnabled (enabled);
        }
    }

    private void cleanUp() {
        for (SymbolProvider provider : getTypeProviders()) {
            provider.cleanup();
        }
    }

    private Collection<? extends SymbolProvider> getTypeProviders() {
        Collection<? extends SymbolProvider> res = typeProviders.get();
        if (res == null) {
            res = Arrays.asList(Lookup.getDefault().lookupAll(SymbolProvider.class).toArray(new SymbolProvider[0]));
            if (!typeProviders.compareAndSet(null, res)) {
                res = typeProviders.get();
            }
        }
        return res;
    }

    private static final class SymbolDescriptorCovertor implements ItemRenderer.Convertor<SymbolDescriptor> {
        @Override
        public String getName(@NonNull final SymbolDescriptor item) {
            return item.getSymbolName();
        }

        @Override
        public String getHighlightText(@NonNull final SymbolDescriptor item) {
            return SymbolProviderAccessor.DEFAULT.getHighlightText(item);
        }

        @Override
        public String getOwnerName(@NonNull final SymbolDescriptor item) {
            return NbBundle.getMessage(GoToSymbolAction.class, "MSG_DeclaredIn", item.getOwnerName());
        }

        @Override
        public String getProjectName(@NonNull final SymbolDescriptor item) {
            return item.getProjectName();
        }

        @Override
        public String getFilePath(@NonNull final SymbolDescriptor item) {
            return item.getFileDisplayPath();
        }

        @Override
        public Icon getItemIcon(@NonNull final SymbolDescriptor item) {
            return item.getIcon();
        }

        @Override
        public Icon getProjectIcon(@NonNull final SymbolDescriptor item) {
            return item.getProjectIcon();
        }

        @Override
        public boolean isFromCurrentProject(@NonNull final SymbolDescriptor item) {
            return false;
        }
    }

    private class Worker implements Runnable {

        private final String text;
        private final SearchType searchType;
        private final long createTime;
        private final GoToPanel panel;

        private volatile boolean isCanceled = false;
        private volatile SymbolProvider current;

        public Worker(
                @NonNull final String text,
                @NonNull final SearchType searchType,
                @NonNull final GoToPanel panel ) {
            this.text = text;
            this.searchType = searchType;
            this.panel = panel;
            this.createTime = System.currentTimeMillis();
            LOG.log(
                Level.FINE,
                "Worker for {0} - created after {1} ms.", //NOI18N
                new Object[]{text, System.currentTimeMillis() - panel.time});
       }

        @Override
        public void run() {
            LOG.log(
                Level.FINE,
                "Worker for {0} - started {1} ms.", //NOI18N
                new Object[]{text, System.currentTimeMillis() - createTime});

            final List<? extends SymbolDescriptor> types = getSymbolNames( text );
            if ( isCanceled ) {
                LOG.log(
                    Level.FINE,
                    "Worker for {0} exited after cancel {1} ms.", //NOI18N
                    new Object[]{text, System.currentTimeMillis() - createTime});
                return;
            }
            final ListModel fmodel = Models.fromList(types, currentSearch.resetFilter());
            if ( isCanceled ) {
                LOG.log(
                    Level.FINE,
                    "Worker for {0} exited after cancel {1} ms.", //NOI18N
                    new Object[]{text, System.currentTimeMillis() - createTime});
                return;
            }

            LOG.log(
                Level.FINE,
                "Worker for text {0} finished after {1} ms.", //NOI18N
                new Object[]{text, System.currentTimeMillis() - createTime});
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    final Pair<String, String> nameAndScope = Utils.splitNameAndScope(text);
                    currentSearch.searchCompleted(searchType, nameAndScope.first(), nameAndScope.second());
                    if (!isCanceled) {
                        enableOK(panel.setModel(fmodel));
                    }
                }
            });
        }

        public void cancel() {
            if ( panel.time != -1 ) {
                LOG.log(
                    Level.FINE,
                    "Worker for text {0} canceled after {1} ms.", //NOI18N
                    new Object[]{text, System.currentTimeMillis() - createTime});
            }
            SymbolProvider _provider;
            synchronized (this) {
                isCanceled = true;
                _provider = current;
            }
            if (_provider != null) {
                _provider.cancel();
            }
        }

        @SuppressWarnings("unchecked")
        private List<? extends SymbolDescriptor> getSymbolNames(final String text) {
            // TODO: Search twice, first for current project, then for all projects
            List<SymbolDescriptor> items;
            // Multiple providers: merge results
            items = new ArrayList<SymbolDescriptor>(128);
            String[] message = new String[1];
            for (SymbolProvider provider : getTypeProviders()) {
                current = provider;
                if (isCanceled) {
                    return null;
                }
                LOG.log(
                    Level.FINE,
                    "Calling SymbolProvider: {0}", //NOI18N
                    provider);
                final SymbolProvider.Context context = SymbolProviderAccessor.DEFAULT.createContext(null, text, searchType);
                final SymbolProvider.Result result = SymbolProviderAccessor.DEFAULT.createResult(items, message, context);
                provider.computeSymbolNames(context, result);
                current = null;
            }
            if ( !isCanceled ) {
                Collections.sort(items, new SymbolComparator());
                panel.setWarning(message[0]);
                return items;
            }
            else {
                return null;
            }
        }
    }
}
