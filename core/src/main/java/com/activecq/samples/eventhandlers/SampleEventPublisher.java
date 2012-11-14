/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.activecq.samples.eventhandlers;

import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.event.EventUtil;
import org.apache.sling.event.JobProcessor;
import org.apache.sling.event.jobs.JobUtil;
import org.osgi.framework.Constants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;

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
