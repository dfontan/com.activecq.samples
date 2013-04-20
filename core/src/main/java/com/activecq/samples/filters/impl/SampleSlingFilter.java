/*
 * Copyright 2012 david gonzalez.
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
package com.activecq.samples.filters.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author david
 */

@SlingFilter(
        label = "Samples - Sling Filter",
        description = "Sample implementation of a Sling Filter",
        metatype = true,
        generateComponent = true, // True if you want to leverage activate/deactivate
        generateService = true,
        order = 0, // The smaller the number, the earlier in the Filter chain (can go negative); Defaults to Integer.MAX_VALUE which push it at the end of the chain
        scope = SlingFilterScope.REQUEST) // REQUEST, INCLUDE, FORWARD, ERROR, COMPONENT (REQUEST, INCLUDE, COMPONENT)
@Properties({
        @Property(
                label = "Vendor",
                name = Constants.SERVICE_VENDOR,
                value = "ActiveCQ",
                propertyPrivate = true
        )
})
public class SampleSlingFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(SampleSlingFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Usually, do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof SlingHttpServletRequest) ||
                !(response instanceof SlingHttpServletResponse)) {
            // Not a SlingHttpServletRequest/Response, so ignore.
            chain.doFilter(request, response);
            return;
        }

        final SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) response;
        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
        final Resource resource = slingRequest.getResource();

        if (resource.getPath().startsWith("/content/samples")) {
            // Is the SlingFilterScope is REQUEST, redirects can be issued.
            slingResponse.sendRedirect("/some/redirect.html");
            return;
        }

        response.getWriter().write("<!-- Written from the Sample Sling Filter BEFORE the next include -->");

        // Finally, proceed with the rest of the Filter chain
        chain.doFilter(request, response);

        response.getWriter().write("<!-- Written from the Sample Sling Filter AFTER the next include -->");

    }

    @Override
    public void destroy() {
        // Usually, do Nothing
    }

    /**
     * OSGi Component Methods *
     */

    @Activate
    protected void activate(final ComponentContext componentContext) throws Exception {
        final Map<String, String> properties = (Map<String, String>) componentContext.getProperties();
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {

    }
}