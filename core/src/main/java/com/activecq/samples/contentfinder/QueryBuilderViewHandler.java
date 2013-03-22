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

package com.activecq.samples.contentfinder;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.wcm.core.contentfinder.ViewHandler;
import com.day.cq.wcm.core.contentfinder.ViewQuery;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: david
 */
@Component(
        label = "Samples - GQL to Querybuilder View Handler",
        description = "Leverage Querybuilder to run ContentFinder queries",
        metatype = false,
        immediate = true
)
@Properties({
        @Property(
                label = "Servlet Paths",
                name = "sling.servlet.paths",
                value = "/bin/wcm/contentfinder/qb/view",
                propertyPrivate = true
        ),
        @Property(
                label = "Vendor",
                name = Constants.SERVICE_VENDOR,
                value = "ActiveCQ",
                propertyPrivate = true
        )
})
@Service
public class QueryBuilderViewHandler extends ViewHandler {
    private static final Logger log = LoggerFactory.getLogger(QueryBuilderViewHandler.class);
    private static final String DELIMITER = ",";

    public static final int GROUP_PATH = 1;
    public static final int GROUP_TYPE = 2;
    public static final int GROUP_MIMETYPE = 3;
    public static final int GROUP_TAGS = 4;

    @Override
    protected ViewQuery createQuery(SlingHttpServletRequest slingRequest, Session session, String queryString) throws Exception {
        final ResourceResolver resolver = slingRequest.getResourceResolver();
        final QueryBuilder qb = resolver.adaptTo(QueryBuilder.class);

        final Map<String, Object> map = getQueryBuilderMap(slingRequest, queryString);

        final Query query = qb.createQuery(PredicateGroup.create(map), session);

        return new QueryBuilderViewQuery(query);
    }

    final private Map<String, Object> getQueryBuilderMap(final SlingHttpServletRequest request, final String queryString) {

        Map<String, Object> map = new LinkedHashMap<String, Object>();

        if(has(request, "querybuilder") && "true".equals(get(request, "querybuilder"))) {
            /** Use raw querybuilder parameters **/

            final String[] blacklist = new String[] { "querybuilder", "_dc", "ck", "mimType", "_charset" };

            for(final String key : (Set<String>)request.getParameterMap().keySet()) {
                if(ArrayUtils.contains(blacklist, key)) { continue; }
                map.put(key, request.getParameter(key));
            }

            if(StringUtils.isNotBlank(queryString)) {
                map.put("fulltext", queryString);
            }

            map.put("p.limit", getLimit(request));
            map.put("p.offset", getOffset(request));

            return map;

        }  else {
            final boolean isPage = isPage(get(request, "type"));
            final boolean isAsset = isAsset(get(request, "type"));

            String prefix = "";
            if(isPage) {
                prefix = "jcr:content/";
            } else if (isAsset) {
                prefix = "jcr:content/metadata/";
            }


            /** Path **/
            if (has(request, "path")) {
                map = put(request, map, "path", GROUP_PATH, true);
            } else {
                map.put("path", request.getRequestPathInfo().getSuffix());
            }

            /** Type **/
            if (has(request, "type")) {
                map = put(request, map, "type", GROUP_TYPE, true);
            }

            /** MimeType **/
            if (isAsset && has(request, "mimeType")) {
                map.put(GROUP_MIMETYPE + "_group.1_property.operation", "like");
                map.put(GROUP_MIMETYPE + "_group.1_property", prefix + "dc:format");
                map.put(GROUP_MIMETYPE + "_group.1_property.value", "%" + get(request, "mimeType") + "%");
            }

            /** Tags **/
            if (has(request, "tags")) {
                map.put(GROUP_TAGS + "_group.p.or", "true");

                if (hasMany(request, "tags")) {
                    final String[] tags = getAll(request, "tags");

                    int i = 1;
                    for (final String tag : tags) {
                        String tagGroup = GROUP_TAGS + "_group." + i + "_tagid";
                        map.put(tagGroup.concat(".property"), (prefix + "cq:tags"));
                        map.put(tagGroup, tag);
                        i++;
                    }
                } else {
                    map.put(GROUP_TAGS + "_group.1_tagid", get(request, "tags"));
                }
            }

            /** Fulltext and Sort Order **/

            if(StringUtils.isNotBlank(queryString)) {
                map.put("fulltext", queryString);

                map.put("1000_orderby", "@jcr:score");
                map.put("1000_orderby.sort", "desc");
            }

            if(isPage) {
                map.put("1001_orderby", "@" + prefix + "cq:lastModified");
            } else if (isAsset) {
                map.put("1001_orderby", "@" + prefix + JcrConstants.JCR_LASTMODIFIED);
            } else {
                map.put("1001_orderby", "@" + JcrConstants.JCR_LASTMODIFIED);
            }

            map.put("1001_orderby.sort", "desc");

            /** Limit and Offset **/
            map.put("p.limit", getLimit(request));
            map.put("p.offset", getOffset(request));

            map.put("p.or", "false");
        }

        log.debug("QueryBuilder Parameter Map: {}", map);

        return map;
    }

    /**
     * Checks if the provided key has more than 1 values (comma delimited)
     *
     * @param request
     * @param key
     * @return
     */
    private boolean hasMany(SlingHttpServletRequest request, String key) {
        final RequestParameter rp = request.getRequestParameter(key);
        if(rp == null) { return false; }
        return this.getAll(request, key).length > 1;
    }

    /**
     * Checks if the provided key has ANY values (1 or more)
     *
     * @param request
     * @param key
     * @return
     */
    private boolean has(SlingHttpServletRequest request, String key) {
        if(request.getRequestParameters(key) != null) {
            return StringUtils.isNotBlank(request.getRequestParameters(key).toString());
        }

        return false;
    }

    /**
     * Returns a single value for a query parameter key
     *
     * @param request
     * @param key
     * @return
     */
    private String get(SlingHttpServletRequest request, String key) {
        return StringUtils.trim(request.getRequestParameter(key).toString());
    }

    /**
     * Returns a String array from a comma delimited list of values
     *
     * @param request
     * @param key
     * @return
     */
    private String[] getAll(SlingHttpServletRequest request, String key) {
        final RequestParameter rp = request.getRequestParameter(key);
        if(rp == null) { return new String[0]; }
        return StringUtils.split(rp.getString(), DELIMITER);
    }

    /**
     * Convenience wrapper
     *
     * @param request
     * @param map
     * @param key
     * @param group
     * @param or
     * @return
     */
    private Map<String, Object> put(SlingHttpServletRequest request, Map<String, Object> map, String key, int group, boolean or) {
        return putAll(map, key, getAll(request, key), group, or);
    }

    /**
     * Helper method for adding comma delimited values into a Query Builder predicate
     *
     * @param map
     * @param key
     * @param values
     * @param group
     * @param or
     * @return
     */
    private Map<String, Object> putAll(Map<String, Object> map, String key, String[] values, int group, boolean or) {
        final String groupId = String.valueOf(group) + "_group";
        int count = 1;

        for(final String value : values) {
            map.put(groupId + "." + count + "_" + key, StringUtils.trim(value));
            count++;
        }

        map.put(groupId + ".p.or", String.valueOf(or));

        return map;
    }

    /**
     * Checks of the query param node type is that of a CQ Page
     *
     * @param nodeType
     * @return
     */
    private boolean isPage(String nodeType) {
        return StringUtils.equals(nodeType, "cq:Page");
    }

    /**
     * Checks of the query param node type is that of a DAM Asset
     *
     * @param nodeType
     * @return
     */
    private boolean isAsset(String nodeType) {
        return StringUtils.equals(nodeType, "dam:Asset");
    }

    /**
     * Extract the query limit from the ContentFinder Query Parameter notation
     *
     * @param request
     * @return
     */
    private String getLimit(final SlingHttpServletRequest request) {
        int start = 0;
        int end = DEFAULT_LIMIT;

        if (has(request, "limit")) {
            final String[] limits = StringUtils.split(get(request, "limit"), "..");

            try {
                if (limits.length >= 2) {
                    start = Integer.parseInt(limits[0]);
                    end = Integer.parseInt(limits[1]);
                } else if (limits.length == 1) {
                    start = Integer.parseInt(limits[0]);
                }
            } catch (Exception ex) {
                // DO NOTHING
                // Use defaults
            }
        }

        int limit = end - start;
        if(limit < 0) { limit = 0; }

        return String.valueOf(limit);
    }

    /**
     * Extract the query offset from the ContentFinder Query Parameter notation
     *
     * @param request
     * @return
     */
    private String getOffset(final SlingHttpServletRequest request) {
        int start = 0;

        if (has(request, "limit")) {
            final String[] limits = StringUtils.split(get(request, "limit"), "..");

            try {
                if (limits.length >= 1) {
                    start = Integer.parseInt(limits[0]);
                }
            } catch (Exception ex) {
                // DO NOTHING
                // Use defaults
            }
        }

        if (start < 0) { start = 0; }

        return String.valueOf(start);
    }
}

