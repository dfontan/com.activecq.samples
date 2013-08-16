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
package com.activecq.samples.slingauthenticationhandler.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.auth.core.spi.AuthenticationHandler;
import org.apache.sling.auth.core.spi.AuthenticationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component(label = "Samples - Remember Me Sling Token Authentication Handler",
        description = "Sample Sling Authentication Handler that leverages the CRX Token scheme for authentication.",
        metatype = true,
        immediate = false
)
@Properties({

        @Property(label = "Authentication Paths",
                description = "JCR Paths which this Authentication Handler will authenticate",
                name = AuthenticationHandler.PATH_PROPERTY,
                value = {"/content/geometrixx/en"},
                cardinality = Integer.MAX_VALUE),

        @Property(label = "Service Ranking",
                description = "Service ranking. Higher gives more priority. IMPORTANT THIS SITS AT THE TOP OF THE STACK.",
                name = "service.ranking",
                intValue = 10000,
                propertyPrivate = false),

        @Property(label = "Vendor",
                name = "service.vendor",
                value = "ActiveCQ",
                propertyPrivate = true)
})

@Service
public class RememberMeSlingAuthenticationHandler implements AuthenticationHandler {

    @SuppressWarnings("unused")
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public static final String REMEMBER_ME_ROUTINE = "auth.process.remember-me";


    /**
     * Determine if this Authentication Handler should even look at handling equestCredentials request
     * @param request
     * @return
     */
    private boolean accepts(final HttpServletRequest request) {
        final Boolean tmp = (Boolean) request.getAttribute(REMEMBER_ME_ROUTINE);
        final boolean isRememberMeRoutine = (tmp != null) && tmp.booleanValue();

        if(isRememberMeRoutine) {
            // Do not handle requests that already are set to process as part of the Remember Me authentication routine
            // This condition means that Remember Me authentication failed, so do spin off into infinity!
            return false;
        } else if(request.getAttribute("j_reason") == null) {
            // Should have a j_reason from failing the CRX TokenAuthHandler
            // This probably isnt necessary as long as the hasValidRememberMeCookie check works
            return false;
        } else if(!this.hasValidRememberMeCookie(request)) {
            // Verify request have a Valid Remember Me Cookie, else stop processing
            return false;
        }
        return true;

    }

    /** AuthenticationHandler Methods **/

    @Override
    public AuthenticationInfo extractCredentials(HttpServletRequest request,
                                                 HttpServletResponse response) {
        // Handle login for Remember Me cookie
        // In this example the Request will fall through to the TokenSlingAuthenticationHandler
        // If the login logic was properly moved to an OSGi Service, this could handle its own login
        // but for the same of the example it will use TokeSlingAuthenticationHandler's logic
        return null;
    }

    @Override
    public void dropCredentials(HttpServletRequest request,
                                HttpServletResponse response) {
        // Remove the Remember Me  cookie from the request
    }

    @Override
    public boolean requestCredentials(HttpServletRequest request,
                                      HttpServletResponse response) {
        // Catches request coming from a failed auth from OOTB CRX TokenAuthHandler
        if(!this.accepts(request)) {
            return false;
        }

        try {
            request.setAttribute(REMEMBER_ME_ROUTINE, true);
            request.getRequestDispatcher(request.getRequestURI()).forward(request, response);
            return true;
        } catch (ServletException e) {
            log.error("Could not forward request back into authentication process due to ServletException");
        } catch (IOException e) {
            log.error("Could not forward request back into authentication process due to IOException");
        }

        return false;
    }

    private boolean hasValidRememberMeCookie(final HttpServletRequest request) {
        // Make sure there is a Remember Me cookie and it is valid before sending this request back into the
        // Sling authenticator system
        return true;
    }
}