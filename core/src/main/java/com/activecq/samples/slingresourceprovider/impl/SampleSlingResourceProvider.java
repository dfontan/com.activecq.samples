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
package com.activecq.samples.slingresourceprovider.impl;

import com.day.cq.commons.jcr.JcrConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author david
 */
@Component(
        label = "Samples - Sling Resource Provider",
        description = "Sample Sling Resource Provider",
        metatype = false,
        immediate = false
)
@Properties({
        @Property(
                label = "Vendor",
                name = Constants.SERVICE_VENDOR,
                value = "ActiveCQ",
                propertyPrivate = true
        ),
        @Property(
                label = "Root paths",
                description = "Root paths this Sling Resource Provider will respond to",
                name = ResourceProvider.ROOTS,
                value = {"/content/mount/thirdparty"})
})
@Service
public class SampleSlingResourceProvider implements ResourceProvider {

    private List<String> roots;

    @Override
    public Resource getResource(ResourceResolver resourceResolver, HttpServletRequest request, String path) {
        // For this example the Request is not taken into consideration when evaluating
        // the Resource request, so we just call getResource(rr, path)

        // Remember, since this is a Synthetic resource there are no ACLs applied to this
        // resource. If you would like to restrict access, it must be done programmatically by checking
        // the ResourceResolver's user.
        return getResource(resourceResolver, path);
    }

    @Override
    public Resource getResource(ResourceResolver resourceResolver, String path) {
        // Make getResource() return as fast as possible!
        // Return null early if getResource() cannot/should not process the resource request

        // Check the user/group issuing the resource resolution request
        if (!accepts(resourceResolver)) {
            return null;
        }

        // Reject any paths that do not match the roots
        if (!accepts(path)) {
            return null;
        }

        // If path is a root, return a Sythentic Folder
        // This could be any "type" of SyntheticResource
        if (isRoot(path)) {
            return new SyntheticResource(resourceResolver, path, JcrConstants.NT_FOLDER);
        }

        // Do other checks on the path to make sure it meets your specific requirements

        // Make a call to some other sytem using the path/request and resolve the data to return
        // as the provided resource
        Map<String, String> thirdPartyData = new HashMap<String, String>();
        // Mocking some data from a third party that represents this resource
        thirdPartyData.put("resourceTypeKey", "samples/components/content/title");
        thirdPartyData.put("sample-data", "This is sample data");

        ResourceMetadata resourceMetaData = new ResourceMetadata();
        // Set the resolution path
        resourceMetaData.setResolutionPath(path);
        // This resourceType can of course be set/derived from anywhere
        // Often it is set in the OSGi Properties if the value is fixed for all
        // Resources this provider returns
        final String resourceType = thirdPartyData.get("resourceTypeKey");

        // Populate "custom" data
        resourceMetaData.put("sample-data", thirdPartyData.get("sample-data"));

        // Create the resource to return
        Resource resource = new SyntheticResource(resourceResolver, resourceMetaData, resourceType);

        return resource;
    }

    @Override
    public Iterator<Resource> listChildren(Resource parent) {
        final String path = parent.getPath();

        // Check the user/group issuing the resource resolution request
        if (!accepts(parent.getResourceResolver())) {
            return null;
        }

        // Reject any paths that do not match the roots
        if (!accepts(path)) {
            return null;
        }

        // If path is not the root, return null
        // This only allows listChildren to be called on a "Root" path
        // This restriction is implementation specific
        if (!isRoot(path)) {
            return null;
        }

        List<Resource> resources = new ArrayList<Resource>();

        // Call third party, get and create a list of resources in a similar fashion as in getResource
        for (int i = 0; i < 10; i++) {
            ResourceMetadata resourceMetaData = new ResourceMetadata();

            // Create the "path" for this resource; this pathing scheme should
            // be compatible with getResource(..)
            resourceMetaData.setResolutionPath(path + "_" + i);
            resourceMetaData.put("index", String.valueOf(i));
            final String resourceType = "samples/components/content/title";

            Resource resource = new SyntheticResource(parent.getResourceResolver(),
                    resourceMetaData, resourceType);

            resources.add(resource);
        }

        return resources.iterator();
    }

    /**
     * Checks if the provided path is a defined Root path
     *
     * @param path
     * @return
     */
    protected boolean isRoot(String path) {
        for (String root : this.roots) {
            if (StringUtils.equals(path, root)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if this Resource Provider is willing to handle the resource path
     *
     * @param path
     * @return
     */
    protected boolean accepts(String path) {
        for (String root : this.roots) {
            if (StringUtils.startsWith(path, root.concat("/"))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if this Resource Provider is willing to handle the resolution request
     *
     * @param resourceResolver
     * @return
     */
    protected boolean accepts(ResourceResolver resourceResolver) {
        if (resourceResolver == null) {
            return false;
        }
        if (StringUtils.equals("anonymous", resourceResolver.getUserID())) {
            // Terrible "anonymous" check, this is just for an example
            return false;
        }

        return true;
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

        // Get Roots from Service properties
        this.roots = new ArrayList<String>();

        String[] rootsArray = PropertiesUtil.toStringArray(properties.get(ResourceProvider.ROOTS), new String[]{});
        for (String root : rootsArray) {
            root = StringUtils.strip(root);
            if (StringUtils.isBlank(root)) {
                continue;
            } else if (StringUtils.equals(root, "/")) {
                // Cowardly refusing to mount the root
                continue;
            }

            this.roots.add(StringUtils.removeEnd(root, "/"));
        }
    }
}
