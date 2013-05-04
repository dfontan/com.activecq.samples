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

package com.activecq.samples.slingservice.impl;

import com.activecq.samples.slingservice.SampleMutableStateService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: david
 */
@Component(
        label = "Samples - Basic Service with Mutable state",
        description = "Sample implementation of a service with mutable state.",
        metatype = true,
        immediate = false)
@Properties({
        @Property(
                label = "Vendor",
                name = Constants.SERVICE_VENDOR,
                value = "ActiveCQ",
                propertyPrivate = true
        )
})
@Service
public class SampleMutableStateServiceImpl implements SampleMutableStateService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // "Normal" instance variables should be accessed via synchronized blocks
    private final Map<String, String> map = new HashMap<String, String>();

    // Use of a synchronized variable
    // Note: in this case single operations are synchronized, however if we were to iterate
    // over this list, we would need to place it in a sychronized block
    private final List<String> list = Collections.synchronizedList(new ArrayList<String>());

    // Atomic vars (available via the java.util.concurrent library) are thread-safe
    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public void addToMap(final String key, final String val) {
        synchronized (map) {
            // Keep synchronized blocks as short as possible
            map.put(key, val);
        }
    }

    @Override
    public String getFromMap(final String key) {
        synchronized (map) {
            // Keep synchronized blocks as short as possible
            return map.get(key);
        }
    }

    @Override
    public void addToList(final String val) {
        list.add(val);
    }

    @Override
    public int getListLength() {
        return list.size();
    }

    @Override
    public void incremementCount() {
        atomicInteger.incrementAndGet();
    }

    @Override
    public int getCount() {
        return atomicInteger.get();
    }
}