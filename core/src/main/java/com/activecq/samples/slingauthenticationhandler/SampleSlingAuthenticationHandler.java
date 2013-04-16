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
package com.activecq.samples.slingauthenticationhandler;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.auth.core.spi.AuthenticationFeedbackHandler;
import org.apache.sling.auth.core.spi.AuthenticationHandler;
import org.apache.sling.auth.core.spi.AuthenticationInfo;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.SimpleCredentials;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Dictionary;


@Component(label = "Samples - Sling Authentication Handler",
        description = "Sample Sling Authentication Handler",
        metatype = true,
        immediate = false
)
@Properties({

        @Property(label = "Authentication Paths",
                description = "JCR Paths which this Authentication Handler will authenticate",
                name = AuthenticationHandler.PATH_PROPERTY,
                value = {"/content/sample-path"},
                cardinality = Integer.MAX_VALUE),

        @Property(label = "Service Ranking",
                description = "Service ranking. Higher gives more priority.",
                name = "service.ranking",
                intValue = 20,
                propertyPrivate = false),

        @Property(
                name = AuthenticationHandler.TYPE_PROPERTY,
                value = HttpServletRequest.FORM_AUTH,
                propertyPrivate = true),

        @Property(label = "Vendor",
                name = "service.vendor",
                value = "ActiveCQ",
                propertyPrivate = true)
})

@Service
public class SampleSlingAuthenticationHandler implements AuthenticationHandler, AuthenticationFeedbackHandler {

    @SuppressWarnings("unused")
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private static final String DEFAULT_TRUST_CREDENTIALS = "TrustedInfo";
    private String trustCredentials = DEFAULT_TRUST_CREDENTIALS;
    @Property(label = "Trust Credentials",
            description = "The Trust Credentials found in repository.xml or ldap.config",
            value = DEFAULT_TRUST_CREDENTIALS)
    private static final String PROP_TRUST_CREDENTIALS = "prop.trust-credentials";

    /**
     * OSGi Service References *
     */

    @Reference
    private ResourceResolverFactory resourceResolverFactory;


    /** AuthenticationHandler Methods **/

    /**
     * Extract the credentials contained inside the request, parameter or cookie
     *
     * @see com .day.cq.auth.impl.AbstractHTTPAuthHandler#authenticate(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public AuthenticationInfo extractCredentials(HttpServletRequest request,
                                                 HttpServletResponse response) {

        final String extractedUserId = request.getParameter("j_username");
        final String extractedPassword = request.getParameter("j_password");

        // Extract UserId and Password from Request and store in SimpleCredentials object
        final SimpleCredentials credentials =
                new SimpleCredentials(extractedUserId, extractedPassword.toCharArray());

        // Execute any pre-authentication here such as authenticating cookies
        // or authentication credentials to third-party systems

        boolean preauthenticated = false; // hased on pre-authentication success

        if (preauthenticated) {
            // If preauthenticated and the trustCredentials are applied, the
            // credentials.getUser() in the credentials object will be logged in
            // regardless of the credentials.getPassword() is valid

            // Set Trusted Credentials Attributes; Must match to what is in
            // repository.xml or ldap.config (if LDAP is used)
            credentials.setAttribute(trustCredentials, "this value is inconsequential");
        }

        // Return a populated AuthenticationInfo object which will be
        // authenticated by the registered LoginModules
        final AuthenticationInfo info = new AuthenticationInfo(
                HttpServletRequest.FORM_AUTH, credentials.getUserID());

        // Add the credentials obj to the AuthenticationInfo obj
        info.put(JcrResourceConstants.AUTHENTICATION_INFO_CREDENTIALS, credentials);

        return info;
    }

    @Override
    public void dropCredentials(HttpServletRequest request,
                                HttpServletResponse response) {
        // Remove credentials from the request/response
        // This generally removed removing/expiring auth Cookies
    }

    @Override
    public boolean requestCredentials(HttpServletRequest request,
                                      HttpServletResponse response) {
        // Invoked when an anonymous request is made to a resource this
        // authetication handler handles (based on OSGi paths properties)
        return false;
    }

    /**
     * AuthenticationFeedbackHandler Methods *
     */

    @Override
    public void authenticationFailed(HttpServletRequest request, HttpServletResponse response, AuthenticationInfo authInfo) {
        // Executes if authentication by the LoginModule fails

        // Executes after extractCredentials(..) returns a credentials object
        // that CANNOT be authenticated by the LoginModule
    }

    @Override
    public boolean authenticationSucceeded(HttpServletRequest request, HttpServletResponse response, AuthenticationInfo authInfo) {
        // Executes if authentication by the LoginModule succeeds

        // Executes after extractCredentials(..) returns a credentials object
        // that CAN be authenticated by the LoginModule


        // Return true if the handler sent back a response to the client and request processing should terminate.
        // Return false if the request should proceed as authenticated through the framework. (This is usually the desired behavior)
        return false;
    }

    /**
     * OSGi Component Methods *
     */

    @Activate
    protected void activate(ComponentContext componentContext) {
        Dictionary properties = componentContext.getProperties();

        this.trustCredentials = PropertiesUtil.toString(
                properties.get(PROP_TRUST_CREDENTIALS), DEFAULT_TRUST_CREDENTIALS);
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
    }
}