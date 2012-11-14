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
package com.activecq.samples.workflow;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import java.util.Arrays;
import java.util.logging.Level;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author david
 */

@Component(
    label="ActiveCQ Samples - CQ Workflow Process",
    description="Sample Workflow Process implementation",
    metatype=false,
    immediate=false
)
@Properties({
    @Property(
        name=Constants.SERVICE_DESCRIPTION,
        value="Sample Workflow Process implementation.",
        propertyPrivate=true
    ),
    @Property(
        label="Vendor",
        name=Constants.SERVICE_VENDOR,
        value="ActiveCQ",
        propertyPrivate=true
    ),
    @Property(
        label="Workflow Label",
        name="process.label",
        value="Sample Workflow Process",
        description="Label which will appear in the Adobe CQ Workflow interface"
    )
})
@Service
public class SampleProcessWorkflow implements WorkflowProcess {

    /** OSGi Service References **/

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    /** Fields **/

    private static final Logger log = LoggerFactory.getLogger(SampleProcessWorkflow.class);

    /** Work flow execute method **/

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        WorkflowData workflowData = workItem.getWorkflowData();
        final String type = workflowData.getPayloadType();

        // Check if the payload is a path in the JCR
        if(!StringUtils.equals(type, "JCR_PATH")) { return; }

        Session session = workflowSession.getSession();
        // Get the path to the JCR resource from the payload
        String path = workflowData.getPayload().toString();

        // Get data from a previous WF Step
        String previouslySetVal = getPersistedData(workItem, "set-in-previous-wf-step", "a default value");

        try {
            // Get the node in the JCR the payload points to
            Node node = session.getNode(path);

            // Do things to the node
        } catch (PathNotFoundException ex) {
            java.util.logging.Logger.getLogger(SampleProcessWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            java.util.logging.Logger.getLogger(SampleProcessWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Standard Arguments metadata
        String argument = args.get("PROCESS_ARGS", "default value");
        // No parse "argument" as needed to extract delimited values

        // Custom WF inputs stored under ./metaData/argSingle and ./metadata/argMulti
        String singleValue = args.get("argSingle", "not set");
        String[] multiValue = args.get("argMulti", new String[]{"not set"});

        log.debug("Single Value: {}", singleValue);
        log.debug("Multi Value: {}", Arrays.toString(multiValue));

        // Save data for use in a subsequent Workflow step
        persistData(workItem, workflowSession, "set-for-next-workflow", "whatever data you want");

        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Helper methods **/

    private <T> boolean persistData(WorkItem workItem, WorkflowSession workflowSession, String key, T val) {
        WorkflowData data = workItem.getWorkflow().getWorkflowData();
        if(data.getMetaDataMap() == null) { return false; }

        data.getMetaDataMap().put(key, val);
        workflowSession.updateWorkflowData(workItem.getWorkflow(), data);

        return true;
    }

    private <T> T getPersistedData(WorkItem workItem, String key, Class<T> type) {
        MetaDataMap map = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
        return map.get(key, type);
    }

    private <T> T getPersistedData(WorkItem workItem, String key, T defaultValue) {
        MetaDataMap map = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
        return map.get(key, defaultValue);
    }
}