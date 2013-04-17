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
package com.activecq.samples.servlets.impl;

import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.OptingServlet;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author david
 */
@SlingServlet(
        label = "Samples - Sling All Methods Servlet",
        description = "Sample implementation of a Sling All Methods Servlet.",
        paths = {"/services/all-sample"},
        methods = {"GET", "POST"}, // Ignored if paths is set - Defaults to GET if not specified
        resourceTypes = {}, // Ignored if paths is set
        selectors = {"print.a4"}, // Ignored if paths is set
        extensions = {"html", "htm"}  // Ignored if paths is set
)
@Properties({
        @Property(
                label = "Vendor",
                name = Constants.SERVICE_VENDOR,
                value = "ActiveCQ",
                propertyPrivate = true
        )
})
public class SampleAllMethodsServlet extends SlingAllMethodsServlet implements OptingServlet {

    /**
     * Add overrides for other SlingAllMethodsServlet here (doHead, doTrace, doPut, doDelete, etc.) *
     */

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        // Implement custom handling of GET requests
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        // Implement custom handling of POST requests
    }

    /**
     * OptingServlet Acceptance Method *
     */

    @Override
    public boolean accepts(SlingHttpServletRequest request) {
        /*
         * Add logic which inspects the request which determines if this servlet
         * should handle the request. This will only be executed if the
         * Service Configuration's paths/resourcesTypes/selectors accept the request.
         */
        return true;
    }
}