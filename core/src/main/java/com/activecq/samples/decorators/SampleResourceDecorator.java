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

package com.activecq.samples.decorators;

import com.activecq.samples.wrappers.SampleResourceWrapper;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceDecorator;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * User: david
 */
@Component(
        label = "ActiveCQ - Sample Resource Decorator",
        description = "Sample",
        metatype = true,
        immediate = false,
        configurationFactory = false)
@Properties({
        @Property(
                label="Vendor",
                name= Constants.SERVICE_VENDOR,
                value="ActiveCQ",
                propertyPrivate=true
        )
})
@Service
public class SampleResourceDecorator implements ResourceDecorator {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * OSGi Properties *
     */
    private static final String DEFAULT_RESOURCE_TYPE = "vendors/activecq/samples/components/fake";
    private String resourceType = DEFAULT_RESOURCE_TYPE;
    @Property(label = ".img Resource Type", description = "Resource type to decorate", value = DEFAULT_RESOURCE_TYPE)
    private static final String PROP_RESOURCE_TYPE = "prop.resource-type";

    @Override
    public Resource decorate(Resource resource) {
        return decorate(resource, null);
    }

    @Override
    public Resource decorate(Resource resource, HttpServletRequest request) {
        if(!accepts(resource, request)) {
            return resource;
        }

        final SampleResourceWrapper wrapper = new SampleResourceWrapper(resource);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", "bar");
        map.put("business-logic", "business-data");

        wrapper.addProperties(map);

        return wrapper;
    }

    /**
     * Helper methodss *
     */

    protected boolean accepts(Resource resource, HttpServletRequest request) {
        if(resource == null || request == null) {
            return false;
        }

        return (ResourceUtil.isA(resource, this.resourceType));
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

        this.resourceType = PropertiesUtil.toString(properties.get(PROP_RESOURCE_TYPE), DEFAULT_RESOURCE_TYPE);
    }
}
