/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.activecq.samples.liveactions;

import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.api.msm.ActionConfig;
import com.day.cq.wcm.api.msm.LiveAction;
import com.day.cq.wcm.api.msm.LiveRelationship;
import java.util.Dictionary;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author david
 */
@Component(
    label="ActiveCQ Samples - LiveAction",
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
        label="Name",
        value="sampleLiveAction",
        description="LiveAction Unique Name; Referenced in Rollout Configurations",
        name="cq.wcm.msm.action.name",
        propertyPrivate=true
    ),
    @Property(
        label="Title",
        value="ActiveCQ Samples - Live Action",
        description="Sample AdobeCQ LiveAction implementation",
        name="cq.wcm.msm.action.title",
        propertyPrivate=true
    ),
    @Property(
        label="Rank",
        intValue=10,
        name="cq.wcm.msm.action.rank",
        description="LiveAction Rank"
    ),
    @Property(
        label="Properties",
        value={"enabled"},
        cardinality=Integer.MAX_VALUE,
        name="cq.wcm.msm.action.properties",
        description="LiveAction Properties"
    )
})
@Service
public class SampleLiveAction implements LiveAction {

    /**
     * default logger
     */
    private final Logger log = LoggerFactory.getLogger(SampleLiveAction.class);
    private int rank;
    private String name;
    private String title;
    private String[] parameterNames;

    @Override
    public void execute(ResourceResolver resolver, LiveRelationship relation, ActionConfig config, boolean autoSave) throws WCMException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void execute(ResourceResolver resolver, LiveRelationship relation, ActionConfig config, boolean autoSave, boolean isResetRollout) throws WCMException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getPropertiesNames() {
        return this.parameterNames;
    }

    @Override
    public String getParameterName() {
        return this.name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getRank() {
        return this.rank;
    }

    /** OSGi Component Methods **/

    @Activate
    protected void activate(ComponentContext context) {
        Dictionary<String, Object> properties = context.getProperties();

        name = OsgiUtil.toString(properties.get("cq.wcm.msm.action.name"), "liveActionNameNotSet");
        title = OsgiUtil.toString(properties.get("cq.wcm.msm.action.title"), "LiveAction Title Not Set");
        rank = OsgiUtil.toInteger(properties.get("cq.wcm.msm.action.rank"), Integer.MAX_VALUE);
        parameterNames = OsgiUtil.toStringArray(properties.get("cq.wcm.msm.action.properties"), new String[0]);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
    }
}