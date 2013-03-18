/*
 * Copyright 2012 david.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.activecq.samples.slingservice.impl;

import com.activecq.samples.slingservice.SampleService;
import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import java.util.*;

@Component(
    label = "ActiveCQ Samples - Service",
description = "Sample implementation of a service.",
metatype = true,
immediate = false)
@Properties({
    @Property(
        label = "Vendor",
    name = Constants.SERVICE_VENDOR,
    value = "ActiveCQ",
    propertyPrivate = true)
})
@References({
    @Reference(
    name = SampleMultiReferenceServiceImpl.SAMPLE_SERVICE_NAME,
    referenceInterface = SampleService.class,
    cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
    policy = ReferencePolicy.DYNAMIC,
    bind = "bindReferenceServices",
    unbind = "unbindReferenceServices")
})
@Service
public class SampleMultiReferenceServiceImpl implements SampleService {
    public static final String SAMPLE_SERVICE_NAME = "sampleService";

    /**
     * OSGi Properties *
     */
    private static final boolean DEFAULT_ENABLED = false;
    private boolean enabled = DEFAULT_ENABLED;
    @Property(label = "Service Enable/Disable", description = "Enables/Disables the service without nullifying service reference objects. This enable/disabling must be implemented in all public methods of this service.", boolValue = DEFAULT_ENABLED)
    private static final String PROP_ENABLED = "prop.enabled";

    /* OSGi Service References */
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /**
     * Fields *
     */
    // List populated by the OSGi framework via the @Reference annotation above
    private List<ServiceReference> sampleServiceReferences = new ArrayList<ServiceReference>();
    // List to store Service objects derived from the serviceReferenceArray
    private List<SampleService> cachedSampleServices = new ArrayList<SampleService>();
    private ComponentContext componentContext;

    /**
     * Service Methods *
     */
    @Override
    public String helloWorld() {
        if (!this.enabled) {
            return "Service has been disabled";
        }

        return "Hello World!";
    }

    /**
     * OSGi Service Binding Methods
     */
    protected void bindReferenceServices(ServiceReference ref) {
        synchronized (this.sampleServiceReferences) {
            this.sampleServiceReferences.add(ref);
            this.cachedSampleServices = this.registerReferenceServices(
                    this.sampleServiceReferences, this.componentContext,
                    SAMPLE_SERVICE_NAME, SampleService.class);
        }
    }

    /**
     *
     *
     * @param ref
     */
    protected void unbindReferenceServices(ServiceReference ref) {
        synchronized (this.sampleServiceReferences) {
            this.sampleServiceReferences.remove(ref);
            this.cachedSampleServices = this.registerReferenceServices(
                    this.sampleServiceReferences, this.componentContext,
                    SAMPLE_SERVICE_NAME, SampleService.class);
        }
    }

    protected <T> List<T> registerReferenceServices
            (List<ServiceReference> serviceReferences,
            ComponentContext componentContext, String name, Class<T> klass) {
        List<T> serviceObjects = new ArrayList<T>();

        try {
            // Sort ServiceReferences by their ranking; This will allow us to always
            // process Services in the same order.
            Collections.sort(serviceReferences, new ServiceRankingComparator());

            for (final ServiceReference current : serviceReferences) {
                final T service =
                        (T) componentContext.locateService(name, current);
                if (service != null) {
                    serviceObjects.add(service);
                }
            }
        } catch (NullPointerException ex) {
            // Handle issues where OSGi context is unexpectedly removed
            return new ArrayList<T>();
        }

        return serviceObjects;
    }

    protected class ServiceRankingComparator implements Comparator<ServiceReference> {

        @Override
        public int compare(ServiceReference ref1, ServiceReference ref2) {
            if (ref1 == null && ref2 == null) {
                return 0;
            }
            if (ref1 != null && ref2 == null) {
                return 1;
            }
            if (ref1 == null && ref2 != null) {
                return -1;
            }

            final int ranking1 = PropertiesUtil.toInteger(ref1.getProperty(Constants.SERVICE_RANKING), 0);
            final int ranking2 = PropertiesUtil.toInteger(ref2.getProperty(Constants.SERVICE_RANKING), 0);

            if (ranking1 < ranking2) {
                return -1;
            } else if (ranking1 > ranking2) {
                return 1;
            }

            return 0;
        }
    }

    /**
     * OSGi Component Methods *
     */
    @Activate
    protected void activate(final ComponentContext componentContext) throws Exception {
        configure(componentContext);
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        this.componentContext = null;
        this.enabled = false;
    }

    private void configure(final ComponentContext componentContext) {
        final Map<String, String> properties = (Map<String, String>) componentContext.getProperties();

        // Get ComponentContext for accessing services
        this.componentContext = componentContext;

        // Global Service Enabled/Disable Setting
        this.enabled = PropertiesUtil.toBoolean(properties.get(PROP_ENABLED), DEFAULT_ENABLED);
    }
}