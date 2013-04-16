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

package com.activecq.samples.slingfilters;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import java.io.PrintWriter;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: david
 */

@RunWith(MockitoJUnitRunner.class)
public class SampleSlingFilterTest {

    private SlingHttpServletRequest request = null;
    private SlingHttpServletResponse response = null;
    private FilterChain chain = null;
    private Resource resource = null;

    @Before
    public void setUp() throws Exception {
        request = mock(SlingHttpServletRequest.class);
        response = mock(SlingHttpServletResponse.class);
        chain = mock(FilterChain.class);
        resource = mock(Resource.class);
    }

    @After
    public void tearDown() throws Exception {
        request = null;
        response = null;
        chain = null;
        resource = null;
    }

    @Test
    public void testDoFilter_withRedirect() throws Exception {
        //ServletRequest request = mock(ServletRequest.class, withSettings().extraInterfaces(SlingHttpServletRequest.class));
        //ServletResponse response = mock(ServletResponse.class, withSettings().extraInterfaces(SlingHttpServletResponse.class));

        when(request.getResource()).thenReturn(resource);
        when(resource.getPath()).thenReturn("/content/samples/foo");

        SampleSlingFilter filter = new SampleSlingFilter();
        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        verify(response).sendRedirect("/some/redirect.html");

    }

    @Test
    public void testDoFilter_withoutRedirect() throws Exception {
        //ServletRequest request = mock(ServletRequest.class, withSettings().extraInterfaces(SlingHttpServletRequest.class));
        //ServletResponse response = mock(ServletResponse.class, withSettings().extraInterfaces(SlingHttpServletResponse.class));

        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
        when(request.getResource()).thenReturn(resource);
        when(resource.getPath()).thenReturn("/content/dont/redirect");

        SampleSlingFilter filter = new SampleSlingFilter();
        filter.doFilter(request, response, chain);

        verify(response, never()).sendRedirect("/some/redirect.html");
        verify(chain).doFilter(request, response);

        verify(response.getWriter(), times(2)).write(anyString());
    }
}
