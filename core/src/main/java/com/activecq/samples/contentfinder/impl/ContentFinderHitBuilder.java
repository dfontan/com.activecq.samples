package com.activecq.samples.contentfinder.impl;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.search.result.Hit;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.RepositoryException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: david
 */
public class ContentFinderHitBuilder {
    private static final String DAM_THUMBNAIL = "cq5dam.thumbnail.48.48.png";

    public static Map<String, Object> buildGenericResult(final Hit hit) throws RepositoryException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        final Resource resource = hit.getResource();
        final boolean isPage = resource.adaptTo(Page.class) != null;
        final boolean isAsset = DamUtil.isAsset(resource);

        /**
         * Common result properties
         */
        map.put("name", resource.getName());
        map.put("path", hit.getPath());
        map.put("excerpt", hit.getExcerpt());

        if (isPage) {
            map = addPageData(hit, map);
        } else if (isAsset) {
            map = addAssetData(hit, map);
        } else {
            map = addOtherData(hit, map);
        }

        return map;
    }

    /**
     * @param hit
     * @param map
     * @return
     * @throws RepositoryException
     */
    private static Map<String, Object> addPageData(final Hit hit, Map<String, Object> map) throws RepositoryException {
        final Resource resource = hit.getResource();

        final Page page = resource.adaptTo(Page.class);
        String title = resource.getName();

        if (StringUtils.isNotBlank(page.getTitle())) {
            title = page.getTitle();
        } else if (StringUtils.isNotBlank(page.getPageTitle())) {
            title = page.getPageTitle();
        } else if (StringUtils.isNotBlank(page.getNavigationTitle())) {
            title = page.getNavigationTitle();
        }

        map.put("title", title);
        map.put("lastModified", getLastModified(page));//System.currentTimeMillis() / 1000 * 1000);

        // TODO Make this result an Array

        map.put("ddGroups", "page");
        map.put("type", "Page");

        return map;
    }

    /**
     * @param hit
     * @param map
     * @return
     * @throws RepositoryException
     */
    private static Map<String, Object> addAssetData(final Hit hit, Map<String, Object> map) throws RepositoryException {
        final Resource resource = hit.getResource();
        final Asset asset = DamUtil.resolveToAsset(resource);

        String title = resource.getName();

        if (StringUtils.isNotBlank(asset.getMetadataValue(DamConstants.DC_TITLE))) {
            title = asset.getMetadataValue(DamConstants.DC_TITLE);
        }

        map.put("title", title);
        map.put("lastModified", getLastModified(asset));
        map.put("mimeType", asset.getMimeType());
        map.put("size", getSize(asset));
        map.put("ck", getCK(asset));

        map.put("type", "Asset");

        return map;
    }


    /**
     * @param hit
     * @param map
     * @return
     * @throws RepositoryException
     */
    private static Map<String, Object> addOtherData(final Hit hit, Map<String, Object> map) throws RepositoryException {
        final Resource resource = hit.getResource();

        map.put("lastModified", getLastModified(resource));
        map.put("type", "Data");

        return map;
    }

    /**
     * Get the last modified date for an Asset
     *
     * @param asset
     * @return
     */
    private static long getLastModified(final Asset asset) {
        if (asset.getLastModified() > 0L) {
            return asset.getLastModified();
        } else {
            final Object obj = asset.getMetadata().get(JcrConstants.JCR_LASTMODIFIED);

            if (obj != null && obj instanceof Date) {
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
    private static long getLastModified(final Page page) {
        if (page.getLastModified() != null) {
            return page.getLastModified().getTimeInMillis();
        } else {
            final ValueMap properties = page.getProperties();
            Date lastModified = properties.get(NameConstants.PN_PAGE_LAST_MOD, Date.class);
            if (lastModified != null) {
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
    private static long getLastModified(final Resource resource) {
        final ValueMap properties = resource.adaptTo(ValueMap.class);

        final Date cqLastModified = properties.get(NameConstants.PN_PAGE_LAST_MOD, Date.class);
        if (cqLastModified != null) {
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
    private static long getSize(final Asset asset) {
        final Rendition original = asset.getOriginal();
        if (original == null) {
            return 0;
        }
        return original.getSize();
    }


    /**
     * Get the timestamp for the last change to the thumbnail
     *
     * @param asset
     * @return
     */
    private static long getCK(final Asset asset) {
        try {
            Resource resource = asset.getRendition(DAM_THUMBNAIL);
            Resource contentResource = resource.getChild(JcrConstants.JCR_CONTENT);
            ValueMap properties = contentResource.adaptTo(ValueMap.class);

            return properties.get(JcrConstants.JCR_LASTMODIFIED, Long.class) / 1000 * 1000;
        } catch (Exception ex) {
            return 0L;
        }
    }
}
