/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.activecq.samples.slingrewriters;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.rewriter.TransformerFactory;
import org.osgi.framework.Constants;

@Component(
    label="ActiveCQ Samples - Scheduled Service",
    description="",
    immediate=true,
    metatype=true
)

@Properties({
    @Property(
        label="Pipeline Type",
        description="Unique name for this pipeline type.",
        name="pipeline.type",
        value="sample-transformer"
    ),
    @Property(
        label="Vendor",
        name=Constants.SERVICE_VENDOR,
        value="ActiveCQ",
        propertyPrivate=true
    )
})

@Service
public class SampleTransformerFactory implements TransformerFactory {

    @Override
    public Transformer createTransformer() {
        return new SampleTransformer();
    }
}
