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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.tool;

import java.awt.EventQueue;
import java.beans.FeatureDescriptor;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.Validateable;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.impl.DLightToolAccessor;
import org.netbeans.modules.dlight.api.impl.DLightToolConfigurationAccessor;
import org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.impl.DataCollectorProvider;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.impl.IDPProvider;
import org.netbeans.modules.dlight.spi.impl.IndicatorAccessor;
import org.netbeans.modules.dlight.spi.impl.IndicatorProvider;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;

/**
 * D-Light Tool is a set of registered collector used to collect data,
 * set of indicators to display in Indicators Panel when tool is running
 * You should implement interface {@link org.netbeans.modules.dlight.core.model.DLightToolConfigurationProvider}
 * which should create new instance of {@link org.netbeans.modules.dlight.core.model.DLightTool.Configuration}
 * object each time create() method is invoked and register it in D-Light filesystem
 */
public final class DLightTool implements Validateable<DLightTarget> {

    private static final Logger log = DLightLogger.getLogger(DLightTool.class);
    private final String id;
    private final String toolName;
    private final String detailedToolName;
    private final String description;
    private final String iconPath;
    private final FeatureDescriptor descriptor;
    private final DLightToolConfiguration configuration;
    private final CopyOnWriteArrayList<DataCollector<?>> dataCollectors;
    private final CopyOnWriteArrayList<IndicatorDataProvider<?>> indicatorDataProviders;
    private final CopyOnWriteArrayList<Indicator<?>> indicators;
    private final CopyOnWriteArrayList<ValidationListener> validationListeners;
    private boolean enabled;
    private boolean visible;
    private boolean collectorsTurnedOn = true;
    private boolean isDetailsEnabled = true;
    private boolean idpsInitialized = false;
    private boolean dcsInitialized = false;
    private ValidationStatus validationStatus = ValidationStatus.initialStatus();
    private ValidationStatus dataCollectorsValidationStatus = ValidationStatus.initialStatus();
    private ValidationStatus indicatorDataProvidersValidationStatus = ValidationStatus.initialStatus();

    static {
        // register accessor which will be used in friend packages of API/SPI
        // to get access to a tool creation, etc.
        DLightToolAccessor.setDefault(new DLightToolAccessorImpl());
    }

    private DLightTool(DLightToolConfiguration configuration) {
        DLightToolConfigurationAccessor toolConfAccessor = DLightToolConfigurationAccessor.getDefault();
        this.id = configuration.getID();
        this.toolName = toolConfAccessor.getToolName(configuration);
        this.detailedToolName = toolConfAccessor.getDetailedToolName(configuration);
        this.iconPath = toolConfAccessor.getIconPath(configuration);
        this.description = toolConfAccessor.getToolDescription(configuration);
        this.configuration = configuration;
        this.visible = configuration.isVisible();
        this.enabled = true;
        this.descriptor = configuration.detailsDescription;
        dataCollectors = new CopyOnWriteArrayList<DataCollector<?>>();
        indicators = new CopyOnWriteArrayList<Indicator<?>>();
        indicatorDataProviders = new CopyOnWriteArrayList<IndicatorDataProvider<?>>();
        validationListeners = new CopyOnWriteArrayList<ValidationListener>();
    }

    static DLightTool newDLightTool(DLightToolConfiguration configuration) {
        return new DLightTool(configuration);
    }

    /**
     * Enable tool
     */
    public final void enable() {
        enabled = true;
    }

    /**
     * Is Visible in Profile Project's Properties Panel?
     * @return
     */
    public final boolean isVisible() {
        return visible;
    }

    /**
     * Disable tool
     */
    public final void disable() {
        enabled = false;
    }

    void turnCollectorsState(String collectorName, boolean turnedOn) {
        collectorsTurnedOn = turnedOn;
    }

    boolean collectorsTurnedOn() {
        return collectorsTurnedOn;
    }

    @Override
    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public final String getName() {
        return toolName;
    }

    public final FeatureDescriptor getFeatureDescriptor() {
        return descriptor;
    }

    public final String getDescription() {
        return description;
    }

    public final String getID() {
        return id;
    }

    public final String getDetailedName() {
        return detailedToolName;
    }

    public final boolean hasIcon() {
        return iconPath != null;
    }

    public final String getIconPath() {
        return iconPath;
    }

    /**
     * Returns all collectors
     * @return
     */
    public final List<DataCollector<?>> getCollectors() {
        initCollectors();
        return Collections.unmodifiableList(dataCollectors);
    }

    private synchronized void initCollectors() {
        if (dcsInitialized) {
            return;
        }

        DLightToolConfigurationAccessor toolConfAccessor = DLightToolConfigurationAccessor.getDefault();
        List<DataCollectorConfiguration> configurations = toolConfAccessor.getDataCollectors(configuration);
        List<IndicatorDataProviderConfiguration> idpConfigurations = toolConfAccessor.getIndicatorDataProviders(configuration);

        for (DataCollectorConfiguration conf : configurations) {
            DataCollector<?> collector = DataCollectorProvider.getInstance().createDataCollector(conf);
            if (collector == null) {
                log.log(Level.INFO, "Could not find DataCollector for configuration with id:{0}" + " check if " + //NOI18N
                        "DataColelctorFactory is registered in Global Lookup with the same ID", conf.getID()); //NOI18N
                continue;
            }
            registerCollector(collector);
            //if it is indicator and registered as indicator
            if (collector instanceof IndicatorDataProvider<?> && idpConfigurations.contains((IndicatorDataProviderConfiguration) conf)) {
                registerIndicatorDataProvider((IndicatorDataProvider<?>) collector);
            }
        }

        dcsInitialized = true;
    }

    private synchronized void initIndicatorDataProviders() {
        if (idpsInitialized) {
            return;
        }

        DLightToolConfigurationAccessor toolConfAccessor = DLightToolConfigurationAccessor.getDefault();
        List<DataCollectorConfiguration> configurations = toolConfAccessor.getDataCollectors(configuration);
        List<IndicatorDataProviderConfiguration> idpConfigurations = toolConfAccessor.getIndicatorDataProviders(configuration);
        for (IndicatorDataProviderConfiguration idp : idpConfigurations) {
            //we could create already object
            //if (!configurations.contains(idp)) {
            if ((idp instanceof DataCollectorConfiguration && (!dcsInitialized
                    || !configurations.contains((DataCollectorConfiguration) idp)))
                    || (!(idp instanceof DataCollectorConfiguration))) {
                IndicatorDataProvider<?> indDataProvider = IDPProvider.getInstance().create(idp);
                if (indDataProvider == null) {
                    log.log(Level.INFO, "Could not find IndicatorDataProvider for configuration with id:{0}" + " check if " + //NOI18N
                            "IndicatorDataProviderFactory is registered in Global Lookup with the same ID", idp.getID()); //NOI18N
                    continue;

                }
                registerIndicatorDataProvider(indDataProvider);
            }
        }

        idpsInitialized = true;
    }

//    final List<DataCollector<?>> getCollectorsByName(String name) {
//        initCollectors();
//        return dataCollectors;
//    }
//
    private void registerIndicatorDataProvider(IndicatorDataProvider<?> idp) {
        indicatorDataProviders.addIfAbsent(idp);
    }

    public List<IndicatorDataProvider<?>> getIndicatorDataProviders() {
        initIndicatorDataProviders();
        return Collections.unmodifiableList(indicatorDataProviders);
    }

//    List<IndicatorDataProvider<?>> getIndicatorDataProviders(String name) {
//        initIndicatorDataProviders();
//        return indicatorDataProviders;
//    }
//
    final List<Indicator<?>> getIndicators() {
        synchronized (indicators) {
            if (indicators.isEmpty()) {
                //Add All indicators
                List<IndicatorConfiguration> indConfigurationsList = DLightToolConfigurationAccessor.getDefault().getIndicators(configuration);
                for (IndicatorConfiguration indConfiguration : indConfigurationsList) {
                    Indicator<?> indicator = IndicatorProvider.getInstance().createIndicator(id, indConfiguration, isDetailsEnabled);
                    if (indicator == null) {
                        log.log(Level.INFO, "Could not find Indicator for configuration with id: {0}. Check if " + // NOI18N
                                "IndicatorFactory is registered in Global Lookup with the same ID", indConfiguration.getID()); // NOI18N
                        continue;

                    }
                    IndicatorAccessor.getDefault().setToolDescription(indicator, description);
                    indicators.addIfAbsent(indicator);
                }
            }
            return Collections.unmodifiableList(indicators);
        }
    }

    void registerCollector(DataCollector<?> collector) {
        if (collector == null) {
            log.info("Cannot register collector"); //NOI18N
            return;
        }

        dataCollectors.addIfAbsent(collector);

        collector.addValidationListener(new ValidationListener() {

            @Override
            public void validationStateChanged(Validateable source, ValidationStatus oldStatus, ValidationStatus newStatus) {
                notifyStatusChanged(oldStatus, newStatus);
            }
        });
    }

    @Override
    public final ValidationStatus validate(final DLightTarget target) {
        if (validationStatus.isValid()) {
            return validationStatus;
        }

        ValidationStatus oldStatus = validationStatus;
        ValidationStatus newStatus = doValidation(target);

        notifyStatusChanged(oldStatus, newStatus);

        validationStatus = newStatus;

        return newStatus;
    }

    public final ValidationStatus validateIndicatorDataProviders(final DLightConfiguration dlightConfiguration, final DLightTarget target) {
        if (indicatorDataProvidersValidationStatus.isValid()) {
            return indicatorDataProvidersValidationStatus;
        }
        initIndicatorDataProviders();
        initCollectors();
        if (EventQueue.isDispatchThread()) {
            Future<ValidationStatus> task = DLightExecutorService.submit(new Callable<ValidationStatus>() {

                @Override
                public ValidationStatus call() throws Exception {
                    ValidationStatus result = ValidationStatus.initialStatus();

                    for (IndicatorDataProvider<?> idp : dlightConfiguration.getConfigurationOptions(false).getIndicatorDataProviders(DLightTool.this)) {
                        result = result.merge(idp.validate(target));
                    }
                    for (DataCollector<?> dc : dlightConfiguration.getConfigurationOptions(false).getCollectors(DLightTool.this)) {
                        if (!(dc instanceof IndicatorDataProvider)) {
                            continue;
                        }
                        result = result.merge(dc.validate(target));
                    }
                    return result;
                }
            }, "Validate IndicatorDataProviders for tool " + getID());//NO18N // NOI18N
            try {
                indicatorDataProvidersValidationStatus = task.get();
                return indicatorDataProvidersValidationStatus;
            } catch (InterruptedException ex) {
                return ValidationStatus.initialStatus();
            } catch (ExecutionException ex) {
                return ValidationStatus.initialStatus();
            }
        } else {
            ValidationStatus result = ValidationStatus.initialStatus();

            for (IndicatorDataProvider<?> idp : dlightConfiguration.getConfigurationOptions(false).getIndicatorDataProviders(this)) {
                result = result.merge(idp.validate(target));
            }
            for (DataCollector<?> dc : dlightConfiguration.getConfigurationOptions(false).getCollectors(this)) {
                if (!(dc instanceof IndicatorDataProvider<?>)) {
                    continue;
                }
                result = result.merge(dc.validate(target));
            }
            indicatorDataProvidersValidationStatus = result;
            return indicatorDataProvidersValidationStatus;
        }

    }

    public final ValidationStatus validateDataCollectors(final DLightConfiguration dlightConfiguration, final DLightTarget target) {
        if (dataCollectorsValidationStatus.isValid()) {
            return dataCollectorsValidationStatus;
        }
        initCollectors();
        initIndicatorDataProviders();
        if (EventQueue.isDispatchThread()) {
            Future<ValidationStatus> task = DLightExecutorService.submit(new Callable<ValidationStatus>() {

                @Override
                public ValidationStatus call() throws Exception {
                    ValidationStatus result = ValidationStatus.initialStatus();

                    for (DataCollector<?> dc : dlightConfiguration.getConfigurationOptions(false).getCollectors(DLightTool.this)) {
                        result = result.merge(dc.validate(target));
                    }
                    return result;
                }
            }, "Validate DataCollectors for tool " + getID());//NO18N // NOI18N
            try {
                dataCollectorsValidationStatus = task.get();
                return dataCollectorsValidationStatus;
            } catch (InterruptedException ex) {
                return ValidationStatus.initialStatus();
            } catch (ExecutionException ex) {
                return ValidationStatus.initialStatus();
            }
        } else {
            ValidationStatus result = ValidationStatus.initialStatus();

            for (DataCollector<?> dc : dlightConfiguration.getConfigurationOptions(false).getCollectors(this)) {
                result = result.merge(dc.validate(target));
            }
            result = dataCollectorsValidationStatus;
            return dataCollectorsValidationStatus;
        }

    }

    @Override
    public final void invalidate() {
        validationStatus = ValidationStatus.initialStatus();
        notifyStatusChanged(null, validationStatus);
    }

    final synchronized ValidationStatus doValidation(final DLightTarget target) {
        initCollectors();
        initIndicatorDataProviders();
        if (EventQueue.isDispatchThread()) {
            Future<ValidationStatus> task = DLightExecutorService.submit(new Callable<ValidationStatus>() {

                @Override
                public ValidationStatus call() throws Exception {
                    ValidationStatus result = ValidationStatus.initialStatus();

                    for (DataCollector<?> dc : dataCollectors) {
                        result = result.merge(dc.validate(target));
                    }
                    for (IndicatorDataProvider<?> idp : indicatorDataProviders) {
                        result = result.merge(idp.validate(target));
                    }
                    return result;
                }
            }, "Validate DataCollectors and IndicatorDataProviders for tool " + getID());//NO18N // NOI18N
            try {
                //NO18N
                return task.get();
            } catch (InterruptedException ex) {
                return ValidationStatus.initialStatus();
            } catch (ExecutionException ex) {
                return ValidationStatus.initialStatus();
            }
        } else {
            ValidationStatus result = ValidationStatus.initialStatus();

            for (DataCollector<?> dc : dataCollectors) {
                result = result.merge(dc.validate(target));
            }
            for (IndicatorDataProvider<?> idp : indicatorDataProviders) {
                result = result.merge(idp.validate(target));
            }
            return result;
        }
    }

    @Override
    public final void addValidationListener(ValidationListener listener) {
        validationListeners.addIfAbsent(listener);
    }

    @Override
    public final void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }

    private void notifyStatusChanged(ValidationStatus oldStatus, ValidationStatus newStatus) {
        if (oldStatus != null && oldStatus.equals(newStatus)) {
            return;
        }

        for (ValidationListener validationListener : validationListeners) {
            validationListener.validationStateChanged(this, oldStatus, newStatus);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    private static final class DLightToolAccessorImpl extends DLightToolAccessor {

        @Override
        public List<IndicatorDataProvider<?>> getIndicatorDataProviders(DLightTool tool) {
            return tool.getIndicatorDataProviders();
        }

        @Override
        public DLightTool newDLightTool(DLightToolConfiguration configuration) {
            return new DLightTool(configuration);
        }

        @Override
        public List<Indicator<?>> getIndicators(DLightTool tool) {
            return tool.getIndicators();
        }

        @Override
        public List<DataCollector<?>> getCollectors(DLightTool tool) {
            return tool.getCollectors();
        }

        @Override
        public boolean collectorsTurnedOn(DLightTool tool) {
            return tool.collectorsTurnedOn();
        }

        @Override
        public void turnCollectorsState(DLightTool tool, boolean turnedOn) {
            tool.turnCollectorsState(null, turnedOn);
        }

        @Override
        public void setDetailsEnabled(DLightTool tool, boolean isEnabled) {
            tool.isDetailsEnabled = isEnabled;
        }

        @Override
        public boolean isDetailsEnabled(DLightTool tool) {
            return tool.isDetailsEnabled;
        }
    }
}
