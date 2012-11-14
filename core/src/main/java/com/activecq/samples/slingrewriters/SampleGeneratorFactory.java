/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.activecq.samples.slingrewriters;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.rewriter.Generator;
import org.apache.sling.rewriter.GeneratorFactory;

/**
 *
 * @author david
 */
@Component
@Property(name = "pipeline.type", value = "sample-generator")
@Service
public class SampleGeneratorFactory implements GeneratorFactory {

    @Override
    public Generator createGenerator() {
        return new SampleGenerator();
    }
}
