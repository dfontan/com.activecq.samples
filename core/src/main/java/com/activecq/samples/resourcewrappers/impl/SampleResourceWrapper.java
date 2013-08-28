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

package com.activecq.samples.resourcewrappers.impl;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.jcr.resource.JcrPropertyMap;

import java.util.HashMap;
import java.util.Map;

/**
 * User: david
 */
public class SampleResourceWrapper extends ResourceWrapper {
    private Resource resource;
    private Map<String, Object> overlayProperties;

    /**
     * Creates a new wrapper instance delegating all method calls to the given
     * <code>resource</code>.
     */
    public SampleResourceWrapper(final Resource resource, final Map<String, Object> overlayProperties) {
        super(resource);
        this.resource = resource;
        this.overlayProperties = overlayProperties;
    }


    /**
     * Optionally override this Resource type's AdaptTo. A common use case is modifying how this resource
     * exposes properties via its ValueMap.
     *
     * @param type
     * @param <AdapterType>
     * @return
     */
    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if (type != ValueMap.class) {
            return super.adaptTo(type);
        }

        final JcrPropertyMap map = (JcrPropertyMap) super.adaptTo(type);

        final Map<String, Object> properties = new HashMap<String, Object>();

        for(final String key : map.keySet()) {
            properties.put(key, map.get(key));
        }

        properties.putAll(this.overlayProperties);

        return (AdapterType) new ValueMapDecorator(properties);
    }
}