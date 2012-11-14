/*
 * Copyright 2012 david.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.activecq.samples.slingrewriters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Processor;
import org.apache.sling.rewriter.ProcessorConfiguration;
import org.apache.sling.rewriter.ProcessorFactory;
import org.apache.tika.io.IOUtils;
import org.osgi.service.component.ComponentContext;
import org.xml.sax.ContentHandler;

/**
 *
 * @author david
 */
@Component(label="ActiveCQ - Sample Processor Factory",
        description="",
        immediate=true,
        metatype=true)
@Properties ({
    @Property(label = "Vendor",
        name = "service.vendor",
        value = "ActiveCQ",
        propertyPrivate = true),

    @Property(name = "pipeline.type",
        value = "sample-pipeline-type",
        propertyPrivate = true)
})

@Service
public class SampleProcessorFactory implements ProcessorFactory {

    @Override
    public Processor createProcessor() {
        return new SampleProcessor();
    }

    @Activate
    protected void activate(ComponentContext componentContext) {
        Dictionary properties = componentContext.getProperties();
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
    }
}
