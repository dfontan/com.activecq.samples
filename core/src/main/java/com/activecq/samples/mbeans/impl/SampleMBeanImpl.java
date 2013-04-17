package com.activecq.samples.mbeans.impl;/*
 * Copyright 2013 david gonzalez.
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

import com.activecq.samples.mbeans.SampleMBean;
import com.adobe.granite.jmx.annotation.AnnotatedStandardMBean;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;

import javax.management.DynamicMBean;
import javax.management.NotCompliantMBeanException;
import java.util.Map;


@Component(
        label = "Samples - MBean",
        description = "Example of exposing data via an MBean",
        immediate = true,
        metatype = false
)
@Properties({
        @Property(
                label = "Vendor",
                name = Constants.SERVICE_VENDOR,
                value = "ActiveCQ",
                propertyPrivate = true
        ),
        @Property(
                label = "MBean Name",
                name = "jmx.objectname",
                value = "com.activecq.samples.mbean:type=HelloWorld",
                propertyPrivate = true
        )
})
@Service(value = DynamicMBean.class)
public class SampleMBeanImpl extends AnnotatedStandardMBean implements SampleMBean {
    private String attributeOne = "initial value";

    // Required cstor for the AnnotatedStandardBean inheritance
    public SampleMBeanImpl() throws NotCompliantMBeanException {
        super(SampleMBean.class);
    }

    @Override
    public String getAttributeOne() {
        return this.attributeOne;
    }

    @Override
    public void setAttributeOne(String attributeOne) {
        this.attributeOne = attributeOne;
    }

    /**
     * Operation Methods *
     */
    @Override
    public String helloWorld() {
        return "Hello World!";
    }

    /**
     * OSGi Component Methods *
     */
    @Activate
    protected void activate(final ComponentContext componentContext) throws Exception {
        final Map<String, String> properties = (Map<String, String>) componentContext.getProperties();

        configure(properties);
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {
    }

    protected void configure(final Map<String, String> properties) {
    }
}