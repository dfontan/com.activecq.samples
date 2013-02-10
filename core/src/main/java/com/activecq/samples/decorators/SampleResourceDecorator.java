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

import com.activecq.samples.decorators.custom.I18nMapDecorator;
import com.activecq.samples.decorators.custom.XSSMapDecorator;
import com.activecq.samples.wrappers.SampleResourceWrapper;
import com.adobe.granite.xss.XSSFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
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
        inherit = true)
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

    @Reference
    protected XSSFilter xssFilter;

    /**
     * OSGi Properties *
     */
    protected static final String[] DEFAULT_RESOURCE_TYPES = new String[] { "vendors/activecq/samples/components/fake" };
    protected String[] resourceTypes = DEFAULT_RESOURCE_TYPES;
    @Property(label = "Resource Types", description = "Resource types to decorate", value = { "vendors/activecq/samples/components/fake" })
    protected static final String PROP_RESOURCE_TYPE = "prop.resource-types";

    @Override
    public Resource decorate(Resource resource) {
        return decorate(resource, null);
    }

    @Override
    public Resource decorate(Resource resource, HttpServletRequest request) {
        if(!accepts(resource, request)) {
            return resource;
        }

        final Map<String, Object> map = new HashMap<String, Object>();
        final SampleResourceWrapper wrapper = new SampleResourceWrapper(resource);

        map.put("jcr:title", "Overwritten title");
        map.put("title", "This is the derived title");

        wrapper.putAll(map);

        wrapper.addValueMapDecorator(new I18nMapDecorator((SlingHttpServletRequest) request));
        wrapper.addValueMapDecorator(new XSSMapDecorator(xssFilter));

        return wrapper;
    }

    /**
     * Helper methods *
     */
    protected boolean accepts(Resource resource, HttpServletRequest request) {
        if(resource == null || request == null || !(request instanceof SlingHttpServletRequest))  {
            return false;
        }

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
     * Abstract Methods *
     */


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

        this.resourceTypes = PropertiesUtil.toStringArray(properties.get(PROP_RESOURCE_TYPE), DEFAULT_RESOURCE_TYPES);
    }
}
