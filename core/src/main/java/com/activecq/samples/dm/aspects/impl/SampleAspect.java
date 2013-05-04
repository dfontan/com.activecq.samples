/*
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
package com.activecq.samples.dm.aspects.impl;

import com.activecq.samples.slingservice.SampleService;
import org.apache.felix.dm.annotation.api.AspectService;
import org.apache.sling.api.resource.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: david
 */

/**
 * REQUIRES DEPLOYMENT OF THE DEPENDENCY MANAGER RUNTIME BUNDLE INTO FELIX
 *
 * Download Bundle from Maven Central: http://goo.gl/khXcp
 *
 * Build information: http://felix.apache.org/documentation/subprojects/apache-felix-dependency-manager/apache-felix-dependency-manager-using-annotations/apache-felix-dependency-manager-using-annotations-quick-tour/apache-felix-dependency-manager-using-annotations-quick-start.html
 *
 * *** REQUIRES DM ANNOTATION MAVEN PLUG-IN IN POM.XML ***
 *
 * Annotation information: http://felix.apache.org/site/apache-felix-dependency-manager-using-annotations-components.html
 *
 */

@AspectService(ranking = 10, // Order aspects are applied, larger the number, the earlier in the aspect chain
        filter = "(service.vendor=ActiveCQ)", // only apply aspect to Service registrations that match the provided filter
        field = "m_sampleService", // Field to inject the service into; by default any instance vars that are of the Service type will have the service injected
        service = com.activecq.samples.slingservice.SampleService.class // Service to Aspect (also can just use "implements")
        //,factoryMethod="create"
)
public class SampleAspect implements SampleService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private volatile SampleService m_sampleService;

    @Override
    public String helloWorld() {
        return "SampleAspect( " + m_sampleService.helloWorld() +  " )";
    }

    @Override
    public String getName(String path) throws LoginException {
        return "SampleAspect( " + m_sampleService.getName(path) + " )";
    }
}
