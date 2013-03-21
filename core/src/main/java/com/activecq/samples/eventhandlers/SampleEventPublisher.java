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
package com.activecq.samples.eventhandlers;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.event.EventUtil;
import org.apache.sling.event.jobs.JobProcessor;
import org.apache.sling.event.jobs.JobUtil;
import org.osgi.framework.Constants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;

import java.util.Dictionary;
import java.util.Hashtable;

@Component(
    label="ActiveCQ Samples - Event Publisher",
    description="",
    immediate=true,
    metatype=false
)
@Properties({
    @Property(
        label="Vendor",
        name=Constants.SERVICE_VENDOR,
        value="ActiveCQ",
        propertyPrivate=true
    ),
    @Property(
        label="Event Topics",
        value={
            org.apache.sling.api.SlingConstants.TOPIC_RESOURCE_ADDED,
            org.apache.sling.api.SlingConstants.TOPIC_RESOURCE_CHANGED,
            org.apache.sling.api.SlingConstants.TOPIC_RESOURCE_REMOVED
        },
        description="[Required] Sling Event Topics this event handler will to respond to.",
        name="event.topics",
        propertyPrivate=true
    )
})
@Service
public class SampleEventPublisher implements JobProcessor, EventHandler {
    public static final String JOB_TOPIC_POKED = "com/activecq/samples/poked";

    @Reference
	private EventAdmin eventAdmin;

    @Override
    public void handleEvent(Event event) {
        if (EventUtil.isLocal(event)) {
            // If this server created the event
            // then only this server should process the event

            // This will call this's process(..) method, passing in the event obj
            // JobUtil.processJob(..) sends/checks for an ack for this job
            JobUtil.processJob(event, this);
            return;
	    }    }

    @Override
    public boolean process(Event event) {
        final String path = (String) event.getProperty(SlingConstants.PROPERTY_PATH);
        final String resourceType = (String) event.getProperty(SlingConstants.PROPERTY_RESOURCE_TYPE);

        if(!StringUtils.startsWith(path, "/content/samples")) {
            // Only handle events here from resources under /content/samples
            return true;
        }

        try {
            // Only return false if job processing failed and the job should be rescheduled
            return fowardEvent(path);
        } catch (Exception ex) {
            // If this event could not be processes to satisfaction, return false
            // so it can be rescheduled.
            return false;
        }
    }

    private boolean fowardEvent(final String path) throws Exception {
        boolean isDistributableEvent = false;
        boolean sendAsync = true;

        // It is highly recommended to stick w String and Scalar datatypes to avoid
        // issues marshalling data across the wire to other servers in the cluster
        // All value Objects must be serializable
        final Dictionary<String, Object> eventProperties = new Hashtable<String, Object>();
        // The topic must be set in the Event's properties
        eventProperties.put(EventUtil.PROPERTY_JOB_TOPIC, JOB_TOPIC_POKED);

        // Add any other data that Event Handler will need to process this Event
        eventProperties.put("resourcePath", path);

        // Create event object;
        Event pokedEvent;

        if(isDistributableEvent) {
            // Send this event to other servers in the cluster; This is rarely
            // what you want.
            pokedEvent = EventUtil.createDistributableEvent(EventUtil.TOPIC_JOB, eventProperties);
        } else {
            // Send the event out to this server's event queue.
            // This is almost always what you want
            pokedEvent = new Event(EventUtil.TOPIC_JOB, eventProperties);
        }

        // Send the new event out into the world to be handled
        if(sendAsync) {
            // send ASYNCHRONOUSLY
            // This is the usual method
            eventAdmin.postEvent(pokedEvent);
        } else {
            // send SYNCHRONOUSLY
            // Can cause delays in application execution waiting for event to execute
            eventAdmin.sendEvent(pokedEvent);
        }

        return true;
    }
}
