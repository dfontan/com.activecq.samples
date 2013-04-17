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

package com.activecq.samples.slingfilters.impl;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.PrintWriter;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * User: david
 */

@RunWith(MockitoJUnitRunner.class)
public class SampleSlingFilterTest {

    // Variables required for all tests. Define one for the test and initialize and clear in setUp and treatDown

    // This shows how to mockout objects using the expected subclass
    // testDoFilter_withoutRedirect show hows to mock with extra interfaces
    private SlingHttpServletRequest request = null;
    private SlingHttpServletResponse response = null;

    private FilterChain chain = null;
    private Resource resource = null;

    @Before
    public void setUp() throws Exception {
        // Create mocks for required variables
        request = mock(SlingHttpServletRequest.class);
        response = mock(SlingHttpServletResponse.class);
        chain = mock(FilterChain.class);
        resource = mock(Resource.class);
    }

    @After
    public void tearDown() throws Exception {
        // Clear variables during teardown
        // Not actually necessary as they get re-initialized in setUp
        request = null;
        response = null;
        chain = null;
        resource = null;
    }

    @Test
    public void testDoFilter_withRedirect() throws Exception {
        // Mock out test-specific behaviors on input parameters

        // Return mock resource when getting the Request's resource
        when(request.getResource()).thenReturn(resource);

        // Return test-data path when requesting mock resource's path
        // This "test" path should meet the conditions to force a redirect in the Filter
        when(resource.getPath()).thenReturn("/content/samples/foo");

        // Instantiate the class; This creation strategy is only used in the context of the test
        // and used to expose the .doFilter(..) method which we will then test
        SampleSlingFilter filter = new SampleSlingFilter();

        // Execute the method with the mocks we want to test
        filter.doFilter(request, response, chain);


        // Perform our test's assertions via mockito verify

        // For the filter we do not test against any returned value, so to test, we want to verify
        // that certain methods are called a certain # of times.

        // For this test case, no more chain.doFilter(..) should be executed and only a redirect should occur

        // Verify no chain.doFilter(..)'s were called
        verify(chain, never()).doFilter(request, response);

        // Verify that response.sendRedirect(..) was called
        verify(response).sendRedirect(anyString());
    }

    @Test
    public void testDoFilter_withoutRedirect() throws Exception {
        // Mocks out the Request and Response inputs as ServletRequest and ServletResponse that can
        // be successfully cast to a SlingHttpServletRequest and SlingHttpServletResponse

        // Typically simply mocking the subclass is preferred
        ServletRequest request = mock(ServletRequest.class, withSettings().extraInterfaces(SlingHttpServletRequest.class));
        ServletResponse response = mock(ServletResponse.class, withSettings().extraInterfaces(SlingHttpServletResponse.class));

        // Mock out the respone's PrintWriter
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        // Cast the request to a SlingHttpServletRequest (possible because of the above
        // mock withSettings.extraInterfaces(..)
        when(((SlingHttpServletRequest)request).getResource()).thenReturn(resource);

        // Mock out the resource's getPath() method response used to determine if the Filter
        // should redirect or not
        when(resource.getPath()).thenReturn("/content/dont/redirect");

        // Instantiate the class; This creation strategy is only used in the context of the test
        // and used to expose the .doFilter(..) method which we will then test
        SampleSlingFilter filter = new SampleSlingFilter();

        // Execute the method with the mocks we want to test
        filter.doFilter(request, response, chain);

        // Cast the request to a SlingHttpServletRequest (possible because of the above
        // mock withSettings.extraInterfaces(..)
        // This allows access to the sendRedirect(..) so we can verify it was not called
        verify((SlingHttpServletResponse)response, never()).sendRedirect(anyString());

        // Verify that chain.doFilter() was called
        verify(chain).doFilter(request, response);

        // Verify that 2 strings were written to the response
        // (This filter writes HTML comments above and below the chain.doFilter(..)
        verify(response.getWriter(), times(2)).write(anyString());
    }
}
