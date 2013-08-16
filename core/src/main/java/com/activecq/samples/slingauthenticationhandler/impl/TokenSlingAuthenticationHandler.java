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

import com.day.crx.security.token.TokenCookie;
import com.day.crx.security.token.TokenUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.auth.core.spi.AuthenticationFeedbackHandler;
import org.apache.sling.auth.core.spi.AuthenticationHandler;
import org.apache.sling.auth.core.spi.AuthenticationInfo;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Component(label = "Samples - Sling Token Authentication Handler",
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
                description = "Service ranking. Higher gives more priority.",
                name = "service.ranking",
                intValue = 20,
                propertyPrivate = false),

        @Property(label = "Vendor",
                name = "service.vendor",
                value = "ActiveCQ",
                propertyPrivate = true)
})

@Service
public class TokenSlingAuthenticationHandler implements AuthenticationHandler, AuthenticationFeedbackHandler {

    @SuppressWarnings("unused")
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private static final String REPO_DESC_ID = "crx.repository.systemid";
    private static final String REPO_DESC_CLUSTER_ID = "crx.cluster.id";

    private String repositoryId;

    /**
     * OSGi Service References *
     */

    @Reference
    private SlingRepository slingRepository;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;


    @Reference
    private SlingSettingsService slingSettings;


    private boolean accepts(final HttpServletRequest request) {
        // Fail quickly and exit from the Auth Handler ASAP

        // This is check thats here to work with the Sample RememberMe Authentication Handler
        final Boolean rememberMe = (Boolean) request.getAttribute(RememberMeSlingAuthenticationHandler.REMEMBER_ME_ROUTINE);
        final String authKey = request.getParameter("auth");

        return "go".equals(authKey) || (rememberMe != null && rememberMe.booleanValue());
    }

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

        if(!accepts(request)) {
            return null;
        }

        final String extractedUserID = request.getParameter("j_sample_username");
        final String extractedPassword = "do not auth";//request.getParameter("j_password");

        // Execute any pre-authentication here such as authenticating cookies
        // or authentication credentials to third-party systems
        String crxUserID = extractedUserID;

        // Example to work w Sample RememeberMe Authentication Handler
        final Boolean rememberMe = (Boolean) request.getAttribute(RememberMeSlingAuthenticationHandler.REMEMBER_ME_ROUTINE);
        if(rememberMe != null && rememberMe.booleanValue()) {
            crxUserID = "david";
        }

        boolean preauthenticated = "david".equalsIgnoreCase(crxUserID); // hased on pre-authentication success

        if (preauthenticated) {
            Session adminSession = null;
            try {
                adminSession = slingRepository.loginAdministrative(null);
                if(!this.userExists(crxUserID, adminSession)) {
                    this.createUser(crxUserID, adminSession);
                }

                return TokenUtil.createCredentials(request, response, slingRepository, crxUserID, true);
            } catch (RepositoryException e) {
                log.error("Repository error authenticating user: {} ~> {}", crxUserID, e);
            } finally {
                if(adminSession != null) {
                    adminSession.logout();
                }
            }
        }

        // If accepts == true, and conditions have not been met to create a Token AuthInfo object, then authentication has failed
        return AuthenticationInfo.FAIL_AUTH;
    }

    @Override
    public void dropCredentials(HttpServletRequest request,
                                HttpServletResponse response) {
        // Remove the CRX Login Token cookie from the request
        TokenCookie.update(request, response, this.repositoryId, null, null, true);

        // Could remove the token from the user's from CRX but that is a lot of complicated work;
        // Will wait for the token cleanup service to run.
        // Note: if someone else has stolen the login-token cookies, it will be valid until the server-side token cleanup deletes the cookie.
        // Once bug #31617 is fixed, then the supporting token removal service call can be added here
    }

    @Override
    public boolean requestCredentials(HttpServletRequest request,
                                      HttpServletResponse response) {
        // Invoked when an anonymous request is made to a resource this
        // authentication handler handles (based on OSGi paths properties)

        // Also invoked after authenticatedFailed if this auth handler is the best match
        //return true if the handler is able to send an authentication inquiry for the given request. false otherwise.
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

        // Executes directly before requestCredentials()
        log.debug(">>>> Authentication failed");
    }

    @Override
    public boolean authenticationSucceeded(HttpServletRequest request, HttpServletResponse response, AuthenticationInfo authInfo) {
        // Executes if authentication by the LoginModule succeeds

        // Executes after extractCredentials(..) returns a credentials object
        // that CAN be authenticated by the LoginModule
        log.debug(">>>> Authentication succeeded");

        final Map<String, String> data = new HashMap<String, String>();
        data.put("profile/cat", "meow");
        data.put("profile/dog", "woof");
        data.put("profile/bookmarks/item1", "/zip/zap");

        Session adminSession = null;
        try {
            adminSession = slingRepository.loginAdministrative(null);
            final Authorizable authorizable = this.getAuthorizable(authInfo.getUser(), adminSession);

            // Note: By now the CRX TokenCookie has been added so the user is logged in.
            this.manageMembership(authorizable, adminSession, "content-authors", "doesnt-exist");
            this.manageUserProperties(authorizable, adminSession, data);
        } catch (RepositoryException e) {
            // If an error occurs while managing membership or properties, then log the user out as they may be in a invalid state.
            this.dropCredentials(request, response);
            return true;
        } finally {
            if(adminSession != null) {
                adminSession.logout();
            }
        }

        // Return true if the handler sent back a response to the client and request processing should terminate.
        // Return false if the request should proceed as authenticated through the framework. (This is usually the desired behavior)
        return false;
    }

    private Authorizable getAuthorizable(final String userID, final Session session) throws RepositoryException {
        final UserManager userManager = this.getUserManager(session);
        return userManager.getAuthorizable(userID);
    }

    private boolean userExists(final String userId, final Session session) throws RepositoryException {
        return null != getAuthorizable(userId, session);
    }

    private String createUser(final String userId, final Session adminSession) throws RepositoryException {
        final UserManager userManager = this.getUserManager(adminSession);
        final User user = userManager.createUser(userId, UUID.randomUUID().toString());
        return user.getPrincipal().getName();
    }

    private int manageUserProperties(Authorizable user, final Session session, Map<String, String> data) throws RepositoryException {
        int count = 0;
        final ValueFactory valueFactory = session.getValueFactory();

        for(final String key : data.keySet()) {
            user.setProperty(key, valueFactory.createValue(data.get(key)));
            count++;
        }

        return count;
    }

    private boolean manageMembership(Authorizable user, final Session session, String... groupIDs) throws RepositoryException {
        boolean success = true;
        final UserManager userManager = this.getUserManager(session);

        for(final String groupID : groupIDs) {
            final Authorizable authorizable = userManager.getAuthorizable(groupID);
            if(authorizable == null || !authorizable.isGroup()) {
                log.error("Could not find group [ {} ] to add member [ {} ]", groupID, user.getPrincipal().getName());
                success = false;
                continue;
            }

           final Group group = (Group) authorizable;
           group.addMember(user);
        }

        return success;
    }

    private UserManager getUserManager(final Session session) throws RepositoryException {
        if(session instanceof JackrabbitSession) {
            final UserManager userManager = ((JackrabbitSession) session).getUserManager();
            userManager.autoSave(true);
            return userManager;
        } else {
            throw new IllegalArgumentException("Session must be an instanceof JackrabbitSession");
        }
    }

    @Activate
    protected void activate(Map<String, String> config) {
        this.repositoryId = slingRepository.getDescriptor(REPO_DESC_CLUSTER_ID);
        if(StringUtils.isBlank(this.repositoryId)) { this.repositoryId = slingRepository.getDescriptor(REPO_DESC_ID); }
        if(StringUtils.isBlank(this.repositoryId)) { this.repositoryId = slingSettings.getSlingId(); }
        if(StringUtils.isBlank(this.repositoryId)) {
            this.repositoryId = UUID.randomUUID().toString();
            log.error("Unable to get Repository ID; falling back to a random UUID.");
        }
    }
}