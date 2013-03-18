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
package com.activecq.samples.workflow;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.*;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;

/**
 *
 * @author david
 */

@Component(
    label="ActiveCQ Samples - Tag Explosion Workflow",
    description="Sample Workflow Process implementation",
    metatype=false,
    immediate=true
)
@Properties({
    @Property(
        name=Constants.SERVICE_DESCRIPTION,
        value="Explodes tags.",
        propertyPrivate=true
    ),
    @Property(
        label="Vendor",
        name=Constants.SERVICE_VENDOR,
        value="ActiveCQ",
        propertyPrivate=true
    ),
    @Property(
        label="Workflow Label",
        name="process.label",
        value="Tag Explosion",
        description="Explodes tags down the tree."
    )
})
@Service
public class TagExplosionProcessWorkflow implements WorkflowProcess {

    public static final String FROM_PROPERTY = "inputTags";
    public static final String TO_PROPERTY = "cq:tags";

    public static final String TYPE_CQ_PAGE_CONTENT = "cq:PageContent";
    public static final String TYPE_CQ_PAGE = "cq:Page";
    public static final String TYPE_DAM_ASSET = "dam:Asset";
    public static final String TYPE_DAM_ASSET_METADATA = "metatdata";
    public static final String REL_PATH_DAM_ASSET_METADATA = "jcr:content/metatdata";

    /** OSGi Service References **/

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /** Fields **/

    private static final Logger log = LoggerFactory.getLogger(TagExplosionProcessWorkflow.class);

    /** Work flow execute method **/

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        log.debug("TAG EXPLODE");
        final WorkflowData workflowData = workItem.getWorkflowData();
        final String type = workflowData.getPayloadType();

        // Check if the payload is a path in the JCR
        if(!StringUtils.equals(type, "JCR_PATH")) { return; }

        Session session = workflowSession.getSession();
        // Get the path to the JCR resource from the payload
        String path = workflowData.getPayload().toString();

        // Get a ResourceResolver using the same permission set as the Workflow's executing Session
        ResourceResolver resolver = null;
        Map<String, Object> authInfo = new HashMap<String, Object>();
        authInfo.put(JcrResourceConstants.AUTHENTICATION_INFO_SESSION, session);

        // Initialize some variables
        final HashSet<String> newExplodedTags = new HashSet<String>();

        try {
            // Get the Workflow Sessions' resource resolver using the authInfo created above
            resolver = resourceResolverFactory.getResourceResolver(authInfo);

            // Get the Resource representing the WF payload
            final Resource resource = resolver.getResource(path);

            // Get the TagManager (using the same permission level as the Workflow's Session)
            final TagManager tagManager = resolver.adaptTo(TagManager.class);

            // Use custom implementation to find the resource to look for cq:tags and write the
            // custom property "tag-titles" to
            final Resource contentResource = getContentResource(resource);


            com.adobe.granite.asset.api.Asset a = contentResource.adaptTo(com.adobe.granite.asset.api.Asset.class);
            a.getIdentifier();

            if(contentResource == null) {
                log.error("Could not find a valid content resource node for payload: {}", resource.getPath());
                return;
            }

            // Gain access to the content resource's properties
            final ValueMap properties = contentResource.adaptTo(ValueMap.class);

            // Get the full tag paths (namespace:path/to/tag) from the content resource
            // This only works on the cq:tags property
            final List<Tag> tags = getStringPropertyToTag(contentResource, FROM_PROPERTY, tagManager);

            // Get any previously applied Localized Tag Titles.
            // This is used to determine if changes if any updates are needed to this node.
            final String[] previousExplodedTags = properties.get(TO_PROPERTY, new String[]{});

            if(!tags.isEmpty()) {
                newExplodedTags.addAll(getExplodedTags(tags, newExplodedTags));
            }


            try {
                // Get the node in the JCR the payload points to
                final Node node = session.getNode(contentResource.getPath());

                // If the currently applied Tag Titles are the same as the derived Tag titles then skip!
                if(!isSame(newExplodedTags.toArray(new String[]{}), previousExplodedTags)) {
                    // If changes have been made to the Tag Names, then apply to the tag-titles property
                    // on the content resource.
                    node.setProperty(TO_PROPERTY, newExplodedTags.toArray(new String[]{}));
                } else {
                    log.debug("No change in Tags. Do not update this content resource.");
                }

            } catch (PathNotFoundException ex) {
                log.error(ex.getMessage());
            } catch (RepositoryException ex) {
                log.error(ex.getMessage());
            }
        } catch(LoginException ex) {
            log.error(ex.getMessage());
        } finally {
            // Clean up after yourself please!!!
            if(resolver != null) {
                resolver.close();
                resolver = null;
            }
        }
    }

    /** Helper methods **/


    /**
     * Checks if two String arrays are the same (same values and same order)
     *
     * @param a
     * @param b
     * @return
     */
    private boolean isSame(String[] a, String[] b) {
        if(a.length != b.length) {
            return false;
        }

        for(int i = 0; i < a.length; i++) {
            if(!StringUtils.equals(a[i], b[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns localized Tag Titles for all the ancestor tags to the tags supplied in "tagPaths"
     *
     * Tags in
     * @param tags
     * @param explodedTags
     * @return
     */
    private HashSet<String> getExplodedTags(final List<Tag> tags, HashSet<String> explodedTags) {
        for(final Tag tag : tags) {
            explodedTags.add(tag.getTagID());

            if(tag.listChildren() != null && tag.listChildren().hasNext()) {
                final List<Tag> children = IteratorUtils.toList(tag.listChildren());
                explodedTags.addAll(this.getExplodedTags(children, explodedTags));
            } else {
                log.debug("Add tag: {}", tag.getTagID() );
                explodedTags.add(tag.getTagID());
            }
        }

        return explodedTags;
    }

    private List<Tag> getStringPropertyToTag(final Resource resource, final String property, final TagManager tagManager) {
        final ValueMap properties = resource.adaptTo(ValueMap.class);
        final String[] tagIds = properties.get(property, new String[]{});
        final List<Tag> tags = new ArrayList<Tag>();

        for(final String tagId : tagIds) {
            final Tag tmp = tagManager.resolve(tagId);

            if(tmp != null) {
                tags.add(tmp);
            }
        }

        return tags;
    }

    /**
     * Finds the proper "content" resource to read cq:tags from and write tag-titles to, based on
     * payload resource type.
     *
     * cq:Page
     * cq:PageContent
     * nt:unstructured acting as cq:PageContent
     *
     * dam:Asset
     * dam:Asset metadata
     *
     * @param payloadResource
     * @return
     */
    private Resource getContentResource(final Resource payloadResource) {

        if(isPrimaryType(payloadResource, TYPE_CQ_PAGE)) {

            /** cq:Page **/

            return payloadResource.getChild(JcrConstants.JCR_CONTENT);
        } else if(StringUtils.equals(payloadResource.getName(), JcrConstants.JCR_CONTENT) &&
            isPrimaryType(payloadResource, TYPE_CQ_PAGE_CONTENT)) {

            /** cq:PageContent **/

            return payloadResource;
        } else if(isPrimaryType(payloadResource, JcrConstants.NT_UNSTRUCTURED)) {

            /** nt:unstructured **/

            final Resource parent = payloadResource.getParent();

            if(parent != null &&
                    isPrimaryType(parent, TYPE_CQ_PAGE) &&
                    StringUtils.equals(payloadResource.getName(), JcrConstants.JCR_CONTENT)) {

                /** cq:Page / jcr:content(nt:unstructured) **/

                return payloadResource;
            } else if(StringUtils.equals(payloadResource.getName(), TYPE_DAM_ASSET_METADATA)) {
                if(parent != null && StringUtils.equals(parent.getName(), JcrConstants.JCR_CONTENT)) {
                    Resource grandParent = null;
                    if(parent != null) {
                        grandParent = parent.getParent();
                    }

                    if(grandParent != null && isPrimaryType(grandParent, TYPE_DAM_ASSET)) {
                        /** dam:Asset / jcr:content / metadata **/
                        return payloadResource;
                    }
                }
            }
        } else if(isPrimaryType(payloadResource, TYPE_DAM_ASSET)) {

            /** dam:Asset **/

            return payloadResource.getChild(REL_PATH_DAM_ASSET_METADATA);
        }

        /** Use the payload resource; Ex. a component resource that uses cq:tags **/

        return payloadResource;
    }

    /**
     * Checks if the jcr:PrimaryType of a resource matches the type param
     * 
     * @param resource
     * @param type
     * @return
     */
    private boolean isPrimaryType(final Resource resource, final String type) {
        ValueMap properties = resource.adaptTo(ValueMap.class);
        String primaryType = properties.get(JcrConstants.JCR_PRIMARYTYPE, "__unknown__");
        return StringUtils.equals(type, primaryType);
    }
}