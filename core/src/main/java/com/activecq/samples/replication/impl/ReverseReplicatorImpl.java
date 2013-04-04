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

package com.activecq.samples.replication.impl;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentFilter;
import com.day.cq.replication.AgentManager;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.Replicator;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.event.EventUtil;
import org.apache.sling.event.jobs.JobProcessor;
import org.apache.sling.event.jobs.JobUtil;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(label="Samples - Reverse Replicator",
        description="Provides a simple mechanism for reverse replicating nodes. A Reverse replicator agent must be setup on the Server.",
        configurationFactory=true,
        immediate=true,   
        metatype=true)

@Properties ({
    @Property(
        name="service.vendor",
        value="ActiveCQ"        
    ),
    @Property(
        label="Sling Event Topics",
        name="event.topics",        
        description="Sling Events to listen to via this Sling Event Handler." +
                    "Values are limited to: " +
                    "org/apache/sling/api/resource/Resource/ADDED, " +
                    "org/apache/sling/api/resource/Resource/CHANGED, " +
                    "org/apache/sling/api/resource/Resource/REMOVED",
        value={
            org.apache.sling.api.SlingConstants.TOPIC_RESOURCE_ADDED,
            org.apache.sling.api.SlingConstants.TOPIC_RESOURCE_CHANGED,
            org.apache.sling.api.SlingConstants.TOPIC_RESOURCE_REMOVED
        }
    )
})

@Service
public class ReverseReplicatorImpl implements JobProcessor, EventHandler {
    public static final String SLING_TOPIC_ADDED = "org/apache/sling/api/resource/Resource/ADDED";
    public static final String SLING_TOPIC_CHANGED = "org/apache/sling/api/resource/Resource/CHANGED";
    public static final String SLING_TOPIC_REMOVED = "org/apache/sling/api/resource/Resource/REMOVED";
    
    @Reference
    AgentManager agentManager;
    
    @Reference
    private ResourceResolverFactory resourceResolverFactory;
    
    @Reference 
    private Replicator replicator;
    
    @Reference 
    private EventAdmin eventAdmin;


    //private ResourceResolver adminResourceResolver;
    //private Session adminSession;
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private static final boolean DEFAULT_ENABLED = true;
    private boolean enabled = DEFAULT_ENABLED;
    @Property(label="Enable",
            description="Enables this reverse replication configuration.",
            boolValue=DEFAULT_ENABLED)
    private static final String PROP_ENABLED = "prop.enabled";    

    
    private static final boolean DEFAULT_SYNCHRONOUS = false;
    private boolean sychronous = DEFAULT_SYNCHRONOUS;
    @Property(label="Synchronous Replication",
            description="Should the replication be done synchronous or asynchronous? The default is 'false'.",
            boolValue=DEFAULT_SYNCHRONOUS)
    private static final String PROP_SYNCHRONOUS = "prop.synchronous";      
    

    private static final boolean DEFAULT_SUPRESS_STATUS_UPDATE = true;
    private boolean suppressStatusUpdate = DEFAULT_SUPRESS_STATUS_UPDATE;
    @Property(label="Supress Status Update",
            description="If set to true the replication will not update the replication status properties after a replication. Default is 'true'.",
            boolValue=DEFAULT_SUPRESS_STATUS_UPDATE)
    private static final String PROP_SUPRESS_STATUS_UPDATE = "prop.supress-status-update";      
    
    private static final boolean DEFAULT_SUPRESS_VERSIONING = true;
    private boolean supressVersioning = DEFAULT_SUPRESS_VERSIONING;
    @Property(label="Supress Versioning",
            description="If set to true the replication will not trigger implicit versioning. Default is 'true'",
            boolValue=DEFAULT_SUPRESS_VERSIONING)
    private static final String PROP_SUPRESS_VERSIONING = "prop.supress-versioning";         
    
    private static final String[] DEFAULT_PATHS = { "/path/to/replicate" };   
    private String[] paths = DEFAULT_PATHS;
    @Property(label="Paths to replicate",
            description="JCR paths to listen on.",
            cardinality=Integer.MAX_VALUE,            
            value={"/path/to/replicate"})
    private static final String PROP_PATHS = "prop.paths";    

    
    private static final String[] DEFAULT_PRIMARY_TYPES = { };   
    private String[] primaryTypes = DEFAULT_PRIMARY_TYPES;
    @Property(label="Primary Node Types",
            description="jcr:primaryType's to reverse replciate. Leave blank to disable this filter.",
            cardinality=Integer.MAX_VALUE,            
            value={""})
    private static final String PROP_PRIMARY_TYPES = "prop.primary-types";       

    private static final String[] DEFAULT_PROPERTY_MATCHES = { "cq:distribute=true" };   
    private Map<String, String> propertyMatches = new HashMap<String, String>();
    @Property(label="Primary Node Types to Replicate.",
            description="Format is: <property>=<value>. Leave blank to disable this filter.",
            cardinality=Integer.MAX_VALUE, 
            value={"cq:distribute=true"})
    private static final String PROP_PROPERTY_MATCHES = "prop.property-matches";      

    
    private static final String[] DEFAULT_PATH_BLACKLIST = { };   
    private String[] pathBlacklist = DEFAULT_PATH_BLACKLIST;
    @Property(label="Blacklist Regex",
            description="",
            cardinality=Integer.MAX_VALUE,            
            value={})
    private static final String PROP_PATH_BLACKLIST = "prop.path-blacklist";      

    
    private static final String[] DEFAULT_PATH_WHITELIST = { };   
    private String[] pathWhitelist = DEFAULT_PATH_WHITELIST;
    @Property(label="Whitelist Regex",
            description="",
            cardinality=Integer.MAX_VALUE,
            value={})
    private static final String PROP_PATH_WHITELIST = "prop.path-whitelist";      
    
    /**
     * Sling Event Handling
     *
     * handleEvent and process implement the Sling Eventing which handles
     * syncing changes from the JCR/CRX to the FileSystem via: - VAULT UPDATE
     *
     */
    public void handleEvent(Event event) {
        if (EventUtil.isLocal(event)) {
            JobUtil.processJob(event, this);
        }
    }

    public boolean process(Event event) {
        if(!enabled) { return false; }

        ResourceResolver adminResourceResolver = null;
        try {

            adminResourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);

            final String resourcePath = (String) event.getProperty("path");

            for (final String path : paths) {
                if (StringUtils.startsWithIgnoreCase(resourcePath, path)) {
                    log.debug("Processing Reverse Replication Event for: " + resourcePath);

                    Resource resource = adminResourceResolver.resolve(resourcePath);
                    if(resource == null) { continue; }

                    log.debug("Primary Type: " + hasValidPrimaryType(resource));
                    log.debug("Property: " + hasValidProperty(resource));
                    log.debug("Whitelist: " + isWhitelisted(resource));
                    log.debug("Not Blacklist: " + !isBlacklisted(resource));
                    log.debug("Event: " + event.getTopic());
                    log.debug("is Delete: " + isDeleteEvent(event));

                    if(!isDeleteEvent(event)) {
                        if(!hasValidPrimaryType(resource) || !hasValidProperty(resource)) { continue; }
                    }
                    if(!isWhitelisted(resource) || isBlacklisted(resource)) { continue; }
                    if(!shouldReplicate(resource, event)) { continue; }

                    try {
                        replicate(resource, event);
                        log.debug("*** REPLICATION KICKED OFF ***");
                        break;
                    } catch (ReplicationException ex) {
                        log.debug("*** REPLICATION FAILED ***");
                        log.debug(ex.getMessage());
                    }
                }
            }
        } catch(Exception ex) {
            log.debug("*** REPLICATION FAILED : REPOSITORY EXCEPTION GETTING ADMIN RESOURCE RESOLVER ***");
            log.debug(ex.getMessage());
        }  finally {
            if (adminResourceResolver != null) {
                adminResourceResolver.close();
            }
        }

        return true;
    }

    private boolean shouldReplicate(final Resource resource, Event event) {
        if(StringUtils.equals(event.getTopic(), SlingConstants.TOPIC_RESOURCE_CHANGED)) {
            ValueMap properties = resource.adaptTo(ValueMap.class);

            Date lastModified = properties.get(JcrConstants.JCR_LASTMODIFIED, Date.class);
            Date lastReplicated = properties.get("cq:lastReplicated", Date.class);
            
            if(lastReplicated == null) { return true; }
            if(lastModified == null) { setLastModified(resource); }
            
            log.debug("LM " + lastModified.getTime()  + " >= LR " + lastReplicated.getTime() + " => " + lastModified.after(lastReplicated));
            
            // Last Modified must be >= Last Replicated
            return lastModified.after(lastReplicated);
        } else {
            return true;
        }
    }
    
    private void setLastModified(final Resource resource) {
        try {
            Calendar now = Calendar.getInstance();

            Node node = resource.adaptTo(Node.class);

            node.setProperty(JcrConstants.JCR_LASTMODIFIED, now);            
            node.setProperty(JcrConstants.JCR_LAST_MODIFIED_BY, resource.getResourceResolver().getUserID());
            
            node.getSession().save();
            
        } catch (ValueFormatException ex) {
            java.util.logging.Logger.getLogger(ReverseReplicatorImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (VersionException ex) {
            java.util.logging.Logger.getLogger(ReverseReplicatorImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LockException ex) {
            java.util.logging.Logger.getLogger(ReverseReplicatorImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConstraintViolationException ex) {
            java.util.logging.Logger.getLogger(ReverseReplicatorImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            java.util.logging.Logger.getLogger(ReverseReplicatorImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    private boolean isWhitelisted(final Resource resource) {
        if(pathWhitelist.length <= 0) { return true; }
        
        for(String regex : pathWhitelist) {
            if(StringUtils.stripToNull(regex) == null) { continue; }

            log.debug("White : " + regex + " vs " + resource.getPath());
            
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(resource.getPath());
            if(m.find()) {
                return true;
            }            
        }
        
        return false;
    }
    
    private boolean isBlacklisted(Resource resource) {
        if(pathBlacklist.length <= 0) { return false; }
        
        for(String regex : pathBlacklist) {
            if(StringUtils.stripToNull(regex) == null) { continue; }
            
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(resource.getPath());
            if(m.find()) {
                return true;
            }            
        }
        
        return false;
    }    
        
    private boolean hasValidPrimaryType(Resource resource) {
        if(primaryTypes.length <= 0) { return true; }
        
        ValueMap properties = resource.adaptTo(ValueMap.class);

        for(final String primaryType : primaryTypes) {
            if(StringUtils.stripToNull(primaryType) == null) { continue; }
            
            String tmp = properties.get(JcrConstants.JCR_PRIMARYTYPE, String.class);
            log.debug("PT : " + tmp + " vs " + primaryType);
            if(StringUtils.equals(primaryType, tmp)) {
                return true;
            }
        }
        
        return false;
    }

    private boolean hasValidProperty(Resource resource) {
        if(propertyMatches.isEmpty()) { return true; }
        // Removal event
        if(resource.adaptTo(Node.class) == null) { return true; }        
        
        ValueMap properties = resource.adaptTo(ValueMap.class);
        
        for(final String property : propertyMatches.keySet()) {
            if(StringUtils.stripToNull(property) == null) { continue; }
            
            final String value  = (String)propertyMatches.get(property);
            
            if(properties.containsKey(property)) {
                String tmp = properties.get(property, String.class);
                if(StringUtils.equals(value, tmp)) {
                    log.debug("Expected(" + property + ": " + value + ") -> Actual(" + property + ": " + tmp + ")");
                    return true;
                }
            }            
        }
        
        return false;
    }    
    
    
    private void replicate(final Resource resource, final Event event) throws ReplicationException {
        ReplicationOptions replicationOptions = new ReplicationOptions();

        if(resource == null) { 
            return;      
        }

        final Session adminSession = resource.getResourceResolver().adaptTo(Session.class);

        final String revision = (String) resource.getResourceMetadata().get("resourceVersion");
        if(revision != null) {
            replicationOptions.setRevision(revision); 
        }
        
        replicationOptions.setFilter(DISTRIBUTE_AGENT_FILTER);
        replicationOptions.setSynchronous(sychronous);
        replicationOptions.setSuppressStatusUpdate(suppressStatusUpdate);
        replicationOptions.setSuppressVersions(supressVersioning);
        
        if(canReplicate(null, resource.getPath())) {//adminResourceResolver.adaptTo(User.class), resource.getPath())) {
            replicator.replicate(adminSession, getReplicationActionType(event), resource.getPath(), replicationOptions);
        } else {
            final String path = resource.getPath();
            log.error((new StringBuilder()).append(adminSession.getUserID()).append(" is not allowed to replicate this page/asset ").append(path).append(". Issuing request for 'replication'").toString());

            Dictionary properties = new Hashtable<String, Object>();
            properties.put("path", path);
            properties.put("replicationType", getReplicationActionType(event));
            Event activationEvent = new Event("com/day/cq/wcm/workflow/req/for/activation", properties);
            eventAdmin.sendEvent(activationEvent);            
        }
    }
    
    protected boolean canReplicate(Object user, String path)  {
        return true;
    //    return user.hasPermissionOn("wcm/core/privileges/replicate", path);
    }
        
    
    protected ReplicationActionType getReplicationActionType(Event event) {
        if(isDeleteEvent(event)) {
            log.debug("IS REP ACTION: DELETE");
            return ReplicationActionType.DELETE;
        } else {
            log.debug("IS REP ACTION: ACTIVATE");
            return ReplicationActionType.ACTIVATE;
        }
    }
    
     protected boolean isDeleteEvent(Event event) {
        return SlingConstants.TOPIC_RESOURCE_REMOVED.toString().equals( event.getTopic()) ||
                SLING_TOPIC_REMOVED.equals(event.getTopic());
    }    
    
    protected void activate(ComponentContext componentContext) {
        Dictionary properties = componentContext.getProperties();

        enabled = PropertiesUtil.toBoolean(properties.get(PROP_ENABLED), DEFAULT_ENABLED);
        log.debug("Enabled: " + enabled);

        sychronous = PropertiesUtil.toBoolean(properties.get(PROP_SYNCHRONOUS), DEFAULT_SYNCHRONOUS);
        suppressStatusUpdate = PropertiesUtil.toBoolean(properties.get(PROP_SUPRESS_STATUS_UPDATE), DEFAULT_SUPRESS_STATUS_UPDATE);
        supressVersioning = PropertiesUtil.toBoolean(properties.get(PROP_SUPRESS_VERSIONING), DEFAULT_SUPRESS_VERSIONING);
        
        paths = PropertiesUtil.toStringArray(properties.get(PROP_PATHS), DEFAULT_PATHS);
        paths = (String[]) ArrayUtils.removeElement(paths, "");

        pathWhitelist = PropertiesUtil.toStringArray(properties.get(PROP_PATH_WHITELIST), DEFAULT_PATH_WHITELIST);
        pathWhitelist = (String[]) ArrayUtils.removeElement(pathWhitelist, "");

        pathBlacklist = PropertiesUtil.toStringArray(properties.get(PROP_PATH_BLACKLIST), DEFAULT_PATH_BLACKLIST);
        pathBlacklist = (String[]) ArrayUtils.removeElement(pathBlacklist, "");
        
        primaryTypes = PropertiesUtil.toStringArray(properties.get(PROP_PRIMARY_TYPES), DEFAULT_PRIMARY_TYPES);
        primaryTypes = (String[]) ArrayUtils.removeElement(primaryTypes, "");
                
        String[] tmp = PropertiesUtil.toStringArray(properties.get(PROP_PROPERTY_MATCHES), DEFAULT_PROPERTY_MATCHES);
        tmp = (String[]) ArrayUtils.removeElement(tmp, "");
        
        for(final String t : tmp) {
            String[] s = StringUtils.split(t, '=');
            if(s == null || s.length != 2) { continue; }
            propertyMatches.put(s[0], s[1]);    
        }
        //adminResourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
        //adminSession = adminResourceResolver.adaptTo(Session.class);

    }

    protected void deactivate(ComponentContext componentContext) {

    }
    
    private static final AgentFilter DISTRIBUTE_AGENT_FILTER = new AgentFilter() {
        public boolean isIncluded(Agent agent) {
            return agent.getConfiguration().isTriggeredOnDistribute();
        }
    };
    
}
