/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.activecq.samples.slingpostprocessors;

import java.util.List;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.SlingPostProcessor;

/**
 *
 * @author david
 */
public class SamplePostProcessor implements SlingPostProcessor {

    @Override
    public void process(SlingHttpServletRequest request, List<Modification> changes) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
