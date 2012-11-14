/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.activecq.samples.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.OptingServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;

/**
 *
 * @author david
 */
@SlingServlet(
        label="ActiveCQ Sample - Sling Safe Methods Servlet",
        description="Sample implementation of a Sling All Methods Servlet.",
        paths={"/services/sample"},
        methods={"GET"}, // Ignored if paths is set - Defaults to GET if not specified
        resourceTypes={}, // Ignored if paths is set
        selectors={"print.a4", "print"}, // Ignored if paths is set
        extensions={"html"} // Ignored if paths is set
)

@Properties({
    @Property(
        label="Vendor",
        name=Constants.SERVICE_VENDOR,
        value="ActiveCQ",
        propertyPrivate=true
    )
})
public class SampleSafeMethodsServlet extends SlingSafeMethodsServlet implements OptingServlet {

    /** Add overrides for other SlingSafeMethodsServlet here (doGeneric, doHead, doOptions, doTrace) **/

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
        // Implement custom handling of GET requests
        boolean responseIsText = true;

        if(responseIsText) {
            // Write a standard text/html response
            response.setContentType("text/html;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("<html><body>Text to write to response</body></html>");
        } else {
            // Write some binary data to the response; Such as sending back an Image or PDF
            InputStream input = new InputStream() {
                @Override
                public int read() throws IOException {
                    return 0;
                }
            };

            response.setContentType(getServletContext().getMimeType(request.getPathInfo()));
            OutputStream output = response.getOutputStream();
            byte[] buffer = new byte[8192];

            for (int length = 0; (length = input.read(buffer)) > 0;) {
                output.write(buffer, 0, length);
                output.flush();
            }
        }
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