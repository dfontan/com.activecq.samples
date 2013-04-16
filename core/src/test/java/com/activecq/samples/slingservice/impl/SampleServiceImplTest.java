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

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: david
 */
@RunWith(MockitoJUnitRunner.class)
public class SampleServiceImplTest {
    private final Map<String, String> config = new HashMap<String, String>();

    @MockitoAnnotations.Mock
    private ResourceResolverFactory resourceResolverFactory;

    @InjectMocks
    private SampleServiceImpl sampleService = new SampleServiceImpl();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        config.clear();
    }

    @Test
    public void testHelloWorld_enabled() throws Exception {
        config.put(SampleServiceImpl.PROP_ENABLED, "true");
        sampleService.configure(config);

        assertEquals("Hello World!", sampleService.helloWorld());
    }

    @Test
    public void testHelloWorld_disabled() throws Exception {
        config.put(SampleServiceImpl.PROP_ENABLED, "false");
        sampleService.configure(config);

        assertEquals("Service has been disabled", sampleService.helloWorld());
    }

    @Test
    public void testGetName_nullPath() throws Exception {
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        when(resourceResolverFactory.getAdministrativeResourceResolver(null)).thenReturn(resourceResolver);

        assertEquals(null, sampleService.getName(null));
    }

    @Test
    public void testGetName_validPath() throws Exception {
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        Resource resource = mock(Resource.class);

        when(resourceResolverFactory.getAdministrativeResourceResolver(null)).thenReturn(resourceResolver);
        when(resourceResolver.resolve("/content/samples/foo")).thenReturn(resource);
        when(resource.getName()).thenReturn("foo");

        assertEquals("foo", sampleService.getName("/content/samples/foo"));
    }


    @Test
    public void testGetName_exception() throws Exception {
        when(resourceResolverFactory.getAdministrativeResourceResolver(null)).thenThrow(LoginException.class);

        try {
            sampleService.getName("/content/samples/foo");
            assertTrue(false);
        } catch(LoginException ex) {
            assertTrue(true);
        }
    }
}
