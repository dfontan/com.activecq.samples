/*
 * Copyright 2012 david gonzalez.
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
package com.activecq.samples.slingscheduler;

import com.day.cq.jcrclustersupport.ClusterAware;
import java.util.Map;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author david
 */
@Component(
    label="ActiveCQ Samples - Scheduled Service",
    description="",
    immediate=true,
    metatype=true
)

@Properties({
    @Property(
        label="Enabled",
        description="Enable/Disable the Scheduled Service",
        name="service.enabled",
        boolValue=true
    ),
    @Property(
        label="Cron expression defining when this Scheduled Service will run",
        description="[every minute = 0 * * * * ?], [12:01am daily = 0 1 0 ? * *]",
        name="scheduler.expression",
        value="0 1 0 ? * *"
    ),
    @Property(
        label="Allow concurrent executions",
        description="Allow concurrent executions of this Scheduled Service",
        name="scheduler.concurrent",
        boolValue=false
    ),
    @Property(
        label="Vendor",
        name=Constants.SERVICE_VENDOR,
        value="ActiveCQ",
        propertyPrivate=true
    )
})

@Service
public class SampleScheduledService implements Runnable, ClusterAware {

    /** OSGi Service References **/

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /** Fields **/

    private ResourceResolver adminResourceResolver;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private boolean isMaster = false;

    /** Scheduled Service Methods **/

    @Override
    public void run() {
        if(!isMaster) { return; }

        // Scheduled service logic, only run on the Master
    }

    /** ClusterAware Methods **/

    @Override
    public void unbindRepository() {
        this.isMaster = false;
    }

    @Override
    public void bindRepository(String repositoryId, String clusterId, boolean isMaster) {
        this.isMaster = isMaster;
    }

    /** OSGi Component Methods **/

    @Activate
    protected void activate(final ComponentContext componentContext) throws Exception {
        final Map<String, String> properties = (Map<String, String>) componentContext.getProperties();
        this.adminResourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        if(this.adminResourceResolver != null) {
            adminResourceResolver.close();
        }
    }
}
