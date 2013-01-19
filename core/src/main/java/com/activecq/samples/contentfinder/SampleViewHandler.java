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

import com.day.cq.search.QueryBuilder;
import com.day.cq.wcm.core.contentfinder.Hit;
import com.day.cq.wcm.core.contentfinder.ViewHandler;
import com.day.cq.wcm.core.contentfinder.ViewQuery;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Collection;

/**
 * User: david
 */
public class SampleViewHandler extends ViewHandler {

    @Override
    protected ViewQuery createQuery(SlingHttpServletRequest slingRequest, Session session, String queryString) throws Exception {
        final ResourceResolver resolver = slingRequest.getResourceResolver();

        ViewQuery viewQuery = new ViewQuery() {
            @Override
            public Collection<Hit> execute() {
                final Collection<Hit> hits = new ArrayList<Hit>();
                final QueryBuilder qb = resolver.adaptTo(QueryBuilder.class);



                Hit hit = new Hit();
                hit.set("key", "val");

                hits.add(hit);
                return hits;
            }
        };

        return viewQuery;

    }
}
