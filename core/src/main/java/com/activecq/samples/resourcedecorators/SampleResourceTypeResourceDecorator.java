/*
 * Copyright 2012 david gonzalez.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.activecq.samples.resourcedecorators;

import com.activecq.samples.resourcewrappers.SampleSlideshowResourceWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceDecorator;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * User: david
 */
@Component(
        label = "ActiveCQ - Sample Slideshow Resource Decorator",
        description = "Sample",
        metatype = true,
        immediate = false,
        inherit = false)
@Properties({
        @Property(
                label="Vendor",
                name= Constants.SERVICE_VENDOR,
                value="ActiveCQ",
                propertyPrivate=true
        ),
        @Property(
                label = "Resource Types",
                description = "Resource Types to decorate",
                value = { "vendors/activecq/samples/components/fake/slideshow" },
                name = "prop.resource-types"
        )
})
@Service
public class SampleResourceTypeResourceDecorator implements ResourceDecorator {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private String[] resourceTypes = new String[] {};

    @Override
    public Resource decorate(Resource resource) {
        return decorate(resource, null);
    }

    @Override
    public Resource decorate(Resource resource, HttpServletRequest request) {
        /* Usually you will want to check to ensure the resource meets some criteria before
           decorating. It is rare that you want to decorate all resources all the time.

           An "accepts(..)" method is a handy abstraction for managing this.
        */
        if(!accepts(resource, request)) {
            // In this sample, only handle resources with
            // sling:resourceType=vendors/activecq/samples/components/fake/slideshow
            return resource;
        }
        // Remember to return early (as seen above) as this decorator is executed on all
        // Resource resolutions (this happens ALOT), especially if the decorator performs
        // any complex/slow running logic.

        // Common use cases include switching ResourceTypes of resources based on path or
        // adding metadata.

        // You will almost always want to return a customer resource type.

        // The returned resource via the Sling API will be of type "Resource"
        // but will be castable to type "SampleSlideshowResourceWrapper", thus
        // exposing all its custom methods.

        // Any overridden methods (ex. adaptTo) on the wrapper, will be used when invoked on the
        // resultant Resource object (even before casting).
        return new SampleSlideshowResourceWrapper(resource);
    }

    /**
     * Check if the decorator should decorate this resource.
     *
     * @param resource
     * @param request
     * @return
     */
    private boolean accepts(final Resource resource, final HttpServletRequest request) {
        if(resource == null)  {
            return false;
        }

        // Using ResourceUtil.isA(..) will send this into an infinite recursive lookup loop
        for(final String resourceType : this.resourceTypes) {
            final ValueMap properties = resource.adaptTo(ValueMap.class);
            if(properties == null) { return false; }

            final String slingResourceType = properties.get(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, "");

            if(StringUtils.equals(resourceType, slingResourceType)) {
                return true;
            }
        }

        return false;
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
    }

    private void configure(final ComponentContext componentContext) {
        final Map<String, String> properties = (Map<String, String>) componentContext.getProperties();

        this.resourceTypes = PropertiesUtil.toStringArray(properties.get("prop.resource-types"), new String[]{});
    }

}
