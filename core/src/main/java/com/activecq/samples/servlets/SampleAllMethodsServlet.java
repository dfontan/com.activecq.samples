/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.activecq.samples.servlets;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.OptingServlet;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.SlingPostProcessor;
import org.osgi.framework.Constants;

/**
 *
 * @author david
 */
@SlingServlet(
        label="ActiveCQ Sample - Sling All Methods Servlet",
        description="Sample implementation of a Sling All Methods Servlet.",
        paths={"/services/sample"},
        methods={"GET", "POST"}, // Ignored if paths is set - Defaults to GET if not specified
        resourceTypes={}, // Ignored if paths is set
        selectors={"print.a4"}, // Ignored if paths is set
        extensions={"html", "htm"}  // Ignored if paths is set
)
@Properties({
    @Property(
        label="Vendor",
        name=Constants.SERVICE_VENDOR,
        value="ActiveCQ",
        propertyPrivate=true
    )
})
public class SampleAllMethodsServlet extends SlingAllMethodsServlet implements OptingServlet {

    /** Add overrides for other SlingAllMethodsServlet here (doHead, doTrace, doPut, doDelete, etc.) **/

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
        // Implement custom handling of GET requests
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
        // Implement custom handling of POST requests
    }

    /** OptingServlet Acceptance Method **/

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