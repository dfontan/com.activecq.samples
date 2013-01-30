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

package com.activecq.samples.wrappers;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.HashMap;
import java.util.Map;

/**
 * User: david
 */
public class SampleResourceWrapper extends ResourceWrapper {

    private Map<String, Object> data = new HashMap<String, Object>();

    /**
     * Creates a new wrapper instance delegating all method calls to the given
     * <code>resource</code>.
     */
    public SampleResourceWrapper(Resource resource) {
        super(resource);
    }

    public void addProperties(Map<String, Object> map) {
        this.data = map;
    }

    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if (type != ValueMap.class) {
            return super.adaptTo(type);
        }

        ValueMap properties = (ValueMap) super.adaptTo(type);
        if(properties != null) {
            properties.putAll(this.data);
            return (AdapterType) properties;
        } else {
            return (AdapterType) new ValueMapDecorator(this.data);
        }
    }
}
