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

package com.activecq.samples.clientcontext;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import javax.jcr.RepositoryException;

/**
 * User: david
 */
public interface ClientContextBuilder {
    public static final String ANONYMOUS = ClientContextStore.ANONYMOUS;
    public static final String AUTHORIZABLE_ID = "authorizableId";
    public static final String XSS_SUFFIX = "_xss";
    public static final String PATH = "path";

    public enum AuthorizableResolution {
        AUTHENTICATION, // Publish
        IMPERSONATION   // Author
    }

    public String getAuthorizableId(SlingHttpServletRequest request);
    public String getPath(SlingHttpServletRequest request);

    public JSONObject getJSON(SlingHttpServletRequest request, ClientContextStore store) throws JSONException, RepositoryException;
    public JSONObject xssProtect(JSONObject json, String... whiteList) throws JSONException;
    public boolean isSystemProperty(String key);

    public String getGenericInitJS(SlingHttpServletRequest request, ClientContextStore store) throws JSONException, RepositoryException;
    public String getInitJavaScript(JSONObject json, ClientContextStore store);
    public String getInitJavaScript(JSONObject json, String manager);

    public AuthorizableResolution getAuthorizableResolution(SlingHttpServletRequest request);

    public ResourceResolver getResourceResolverFor(final String authorizableId) throws LoginException, RepositoryException;
    public void closeResourceResolverFor(ResourceResolver resourceResolver);
}
