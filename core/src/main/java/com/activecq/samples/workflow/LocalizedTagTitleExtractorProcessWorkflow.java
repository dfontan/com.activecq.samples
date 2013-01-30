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

import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.LocaleUtils;
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
    label="ActiveCQ Samples - Localized Tag Title Extractor Workflow",
    description="Sample Workflow Process implementation",
    metatype=false,
    immediate=false
)
@Properties({
    @Property(
        name=Constants.SERVICE_DESCRIPTION,
        value="Sample Workflow Process implementation - Writes tags titles to index-able property.",
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
        value="Localized Tag Title Extractor",
        description="Writes localized tag Titles to a index-able Page/Asset property (tag-titles)."
    )
})
@Service
public class LocalizedTagTitleExtractorProcessWorkflow implements WorkflowProcess {
    public static final int MIN_TAG_DEPTH = 0;

    public static final String TYPE_CQ_PAGE_CONTENT = "cq:PageContent";
    public static final String TYPE_CQ_PAGE = "cq:Page";
    public static final String TYPE_DAM_ASSET = "dam:Asset";
    public static final String TYPE_DAM_ASSET_METADATA = "metatdata";
    public static final String REL_PATH_DAM_ASSET_METADATA = "jcr:content/metatdata";

    public static final String PATH_DELIMITER = "/";

    public static final String PROPERTY_TAG_TITLES = "tag-titles";
    public static final String PROPERTY_CQ_TAGS = "cq:tags";

    /** OSGi Service References **/

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /** Fields **/

    private static final Logger log = LoggerFactory.getLogger(LocalizedTagTitleExtractorProcessWorkflow.class);

    /** Work flow execute method **/

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
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
        List<String> newTagTitles = new ArrayList<String>();
        Locale locale = null;

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

            if(contentResource == null) {
                log.error("Could not find a valid content resource node for payload: {}", resource.getPath());
                return;
            }

            // Gain access to the content resournce's properties
            final ValueMap properties = contentResource.adaptTo(ValueMap.class);


            // Get the full tag paths (namespace:path/to/tag) from the content resource
            // This only works on the cq:tags property
            final String[] tags = properties.get(PROPERTY_CQ_TAGS, new String[]{});

            // Get any previously applied Localized Tag Titles.
            // This is used to determine if changes if any updates are needed to this node.
            final String[] previousTagTitles = properties.get(PROPERTY_TAG_TITLES, new String[]{});

            if(!ArrayUtils.isEmpty(tags)) {
                // Derive the locale
                if(DamUtil.isAsset(resource)) {
                    // Dam assets use path segments to derive the locale (/content/dam/us/en/...)
                    locale = getLocaleFromPath(resource);
                } else {
                    // Page's use the jcr:language property accessed via the CQ Page API
                    Page page = resource.adaptTo(Page.class);
                    if(page != null) {
                        locale = page.getLanguage(true);
                    }
                }

                // Derive the Localized Tag Titles for all tags in the tag hierarchy from the Tags stored in the cq:tags property
                // This does not remove duplicate titles (different tag trees could repeat titles)
                newTagTitles = tagPathsToLocalizedTagTitles(tags, locale, tagManager);
            }

            try {
                // Get the node in the JCR the payload points to
                final Node node = session.getNode(contentResource.getPath());

                // If the currently applied Tag Titles are the same as the derived Tag titles then skip!
                if(!isSame(newTagTitles.toArray(new String[]{}), previousTagTitles)) {
                    // If changes have been made to the Tag Names, then apply to the tag-titles property
                    // on the content resource.
                    node.setProperty(PROPERTY_TAG_TITLES, newTagTitles.toArray(new String[newTagTitles.size()]));
                } else {
                    log.debug("No change in Tag Titles. Do not update this content resource.");
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
     * @param tagPaths
     * @param locale
     * @param tagManager
     * @return
     */
    private List<String> tagPathsToLocalizedTagTitles(String[] tagPaths, Locale locale, TagManager tagManager) {
        List<String> terms = new ArrayList<String>();

        for(String tagPath : tagPaths){
            boolean isLast = false;

            int count = StringUtils.countMatches(tagPath, PATH_DELIMITER);
            while(count >= MIN_TAG_DEPTH && count >= 0) {

                Tag tag = tagManager.resolve(tagPath);
                if(tag != null) {
                    final String title = tag.getTitle();
                    final String localizeTitle = tag.getTitle(locale);

                    if(StringUtils.isNotBlank(localizeTitle)) {
                        terms.add(localizeTitle);
                    } else if(StringUtils.isNotBlank(title)) {
                        terms.add(title);
                    }
                }

                if(isLast) { break; }

                tagPath = StringUtils.substringBeforeLast(tagPath, PATH_DELIMITER);
                count = StringUtils.countMatches(tagPath, PATH_DELIMITER);

                if(count <= 0) {
                    isLast = true;
                }
            }
        }

        return terms;
    }

    /**
     * Derive the locale from the parent path segments (/content/us/en/..)
     *
     * @param resource
     * @return
     */
    private Locale getLocaleFromPath(final Resource resource) {
        final String[] segments = StringUtils.split(resource.getPath(), PATH_DELIMITER);

        String country = "";
        String language = "";

        for(final String segment : segments) {
            if(ArrayUtils.contains(Locale.getISOCountries(), segment)) {
                country = segment;
            } else if (ArrayUtils.contains(Locale.getISOLanguages(), segment)) {
                language = segment;
            }
        }

        if(StringUtils.isNotBlank(country) && StringUtils.isNotBlank(language)) {
            return LocaleUtils.toLocale(country + "-" + language);
        } else if(StringUtils.isNotBlank(country)) {
            return LocaleUtils.toLocale(country);
        } else if(StringUtils.isNotBlank(language)) {
            return LocaleUtils.toLocale(language);
        }

        return null;
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