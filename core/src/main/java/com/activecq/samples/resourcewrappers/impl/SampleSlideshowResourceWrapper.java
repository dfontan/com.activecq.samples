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

import com.day.cq.commons.ImageResource;
import org.apache.commons.collections.IteratorUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.List;
import java.util.Map;

/**
 * User: david
 */
public class SampleSlideshowResourceWrapper extends ResourceWrapper {
    private Resource resource;

    /**
     * Creates a new wrapper instance delegating all method calls to the given
     * <code>resource</code>.
     */
    public SampleSlideshowResourceWrapper(final Resource resource) {
        super(resource);
        this.resource = resource;
    }

    /**
     * Get the number of child nodes. It is expected all child nodes are Image nodes.
     * <p/>
     * A real implementation can check child nodes for appropriate resourceTypes, etc. qualifying each resource as valid Image resource.
     *
     * @return
     */
    public int getSize() {
        List<Resource> children = IteratorUtils.toList(this.resource.listChildren());
        if (children == null) {
            return 0;
        }
        return children.size();
    }

    /**
     * Return an Image Resource corresponding to each "slide" or image resource defined under thie component.
     *
     * @param index
     * @return
     */
    public ImageResource getSlide(int index) {
        List<Resource> children = IteratorUtils.toList(this.resource.listChildren());
        if (children == null || children.size() < 1) {
            return null;
        } else if (index < 0) {
            index = 0;
        } else if (index >= children.size()) {
            index = children.size() - 1;
        }

        return new ImageResource(children.get(index));
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

        Map<String, Object> map = (Map<String, Object>) super.adaptTo(type);

        /* Do something special with the data exposed in the ValueMap */

        return (AdapterType) new ValueMapDecorator(map);
    }
}