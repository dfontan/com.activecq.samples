package com.activecq.samples.contentfinder;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.search.Query;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.core.contentfinder.ViewQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * User: david
 */
public class QueryBuilderViewQuery implements ViewQuery {
    private static final Logger log = LoggerFactory.getLogger(QueryBuilderViewQuery.class);

    private final Query query;

    public QueryBuilderViewQuery(final Query query) {
        this.query = query;
    }

    @Override
    public Collection<com.day.cq.wcm.core.contentfinder.Hit> execute() {
        final List<com.day.cq.wcm.core.contentfinder.Hit> hits = new ArrayList<com.day.cq.wcm.core.contentfinder.Hit>();
        if(this.query == null) {
            return hits;
        }

        final SearchResult result = this.query.getResult();

        // iterating over the results
        for (Hit hit : result.getHits()) {
            try {
                hits.add(createHit(hit));
            } catch (RepositoryException e) {
                log.error("Could not return required information for Content Finder result: {}", hit.toString());
            }
        }

        return hits;
    }

    private com.day.cq.wcm.core.contentfinder.Hit createHit(final Hit qbHit) throws RepositoryException {
        com.day.cq.wcm.core.contentfinder.Hit hit = new com.day.cq.wcm.core.contentfinder.Hit();

        final Resource resource = qbHit.getResource();
        final boolean isPage = resource.adaptTo(Page.class) != null;
        final boolean isAsset = DamUtil.isAsset(resource);

        /**
         * Common result properties
         */
        hit.set("name", resource.getName());
        hit.set("path", qbHit.getPath());
        hit.set("excerpt", qbHit.getExcerpt());


        if(isPage) {
            /**
             * Special results for Pages
             */

            final Page page = resource.adaptTo(Page.class);
            String title = resource.getName();

            if (StringUtils.isNotBlank(page.getTitle())) {
                title = page.getTitle();
            } else if (StringUtils.isNotBlank(page.getPageTitle())) {
                title = page.getPageTitle();
            } else if (StringUtils.isNotBlank(page.getNavigationTitle())) {
                title = page.getNavigationTitle();
            }

            hit.set("title", title);
            hit.set("lastModified", getLastModified(page));//System.currentTimeMillis() / 1000 * 1000);
            // TODO Make this result an Array
            hit.set("ddGroups", "page");
            hit.set("type", "Page");

        } else if (isAsset) {
            /**
             * Special results for Assets
             */

            final Asset asset = DamUtil.resolveToAsset(resource);
            String title = resource.getName();

            if(StringUtils.isNotBlank(asset.getMetadataValue(DamConstants.DC_TITLE))) {
                title = asset.getMetadataValue(DamConstants.DC_TITLE);
            }

            hit.set("title", title);
            hit.set("lastModified", getLastModified(asset));
            hit.set("mimeType", asset.getMimeType());
            hit.set("size", getSize(asset));
            hit.set("ck", getCK(asset));

            hit.set("type", "Asset");


        } else {
            hit.set("lastModified", getLastModified(resource));
            hit.set("type", "Data");
        }

        return hit;
    }

    /**
     * Get the last modified date for an Asset
     *
     * @param asset
     * @return
     */
    private long getLastModified(final Asset asset) {
        if(asset.getLastModified() > 0L) {
            return asset.getLastModified();
        } else {
            final Object obj = asset.getMetadata().get(JcrConstants.JCR_LASTMODIFIED);

            if(obj != null && obj instanceof Date) {
                return ((Date) obj).getTime();
            } else {
                return 0L;
            }
        }
    }

    /**
     * Get the last modified date for a Page
     *
     * @param page
     * @return
     */
    private long getLastModified(final Page page) {
        if(page.getLastModified() != null) {
            return page.getLastModified().getTimeInMillis();
        } else {
            final ValueMap properties = page.getProperties();
            Date lastModified = properties.get("cq:lastModified", Date.class);
            if(lastModified != null) {
                return lastModified.getTime();
            } else {
                return 0L;
            }
        }
    }

    /**
     * Get the last modified date for a generic resource
     *
     * @param resource
     * @return
     */
    private long getLastModified(final Resource resource) {
        final ValueMap properties = resource.adaptTo(ValueMap.class);

        final Date cqLastModified = properties.get("cq:lastModified", Date.class);
        if(cqLastModified != null) {
            return cqLastModified.getTime();
        }

        final Date jcrLastModified = properties.get(JcrConstants.JCR_LASTMODIFIED, Date.class);
        if (jcrLastModified != null) {
            return jcrLastModified.getTime();
        }

        return 0L;
    }

    /**
     * Get the size of the Asset (the original rendition)
     *
     * @param asset
     * @return
     */
    private long getSize(final Asset asset) {
        final Rendition original = asset.getOriginal();
        if(original == null) { return 0; }
        return original.getSize();
    }


    /**
     * Get the timestamp for the last change to the thumbnail
     *
     * @param asset
     * @return
     */
     private long getCK(final Asset asset) {
        try {
            Resource resource = asset.getRendition("cq5dam.thumbnail.48.48.png");
            Resource contentResource = resource.getChild("jcr:content");
            ValueMap properties = contentResource.adaptTo(ValueMap.class);

            return properties.get(JcrConstants.JCR_LASTMODIFIED, Long.class) / 1000 * 1000;
        } catch(Exception ex) {
            return 0L;
        }
    }
}
