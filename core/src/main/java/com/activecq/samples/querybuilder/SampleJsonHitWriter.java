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

package com.activecq.samples.querybuilder;

import com.day.cq.search.Query;
import com.day.cq.search.result.Hit;
import com.day.cq.search.writer.ResultHitWriter;
import org.apache.felix.scr.annotations.Component;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/*
Interface documentation: http://dev.day.com/docs/en/cq/current/javadoc/com/day/cq/search/writer/ResultHitWriter.html

ResultHitWriter writes a search result Hit into a JSONWriter. This is used in the standard servlet for the query builder to allow different output renderings.

The appropriate ResultHitWriter is selected by passing the desired name in the query using p.hitwriter=NAME as request parameter.

Implementations of this interface must be defined as an OSGi component factory.
The name of the factory must be the fully qualified name of this interface plus "/" and a distinct short name of the renderer (that will be used in request parameters to select it, NAME above).

An example call to this Sample HitWriter might look like:

    http://localhost:4502/bin/querybuilder.json?...&p.hitwriter=activecq-sample

Resulting JSON object would look like:

    [
        ...,
        {
            "path": "/content/path/to/hit",
            "key-to-use-in-json": "Hit Title (pulled from jcr:content node)",
            "complex": "Hello World"
        },
        ...
    ]
*/

@Component(metatype=false, factory="com.day.cq.search.result.ResultHitWriter/activecq-sample")
public class SampleJsonHitWriter implements ResultHitWriter {

    @Override
    public void write(Hit hit, JSONWriter jsonWriter, Query query) throws RepositoryException, JSONException {

        // Get the Node that represents a Query "hit"
        Node node = hit.getNode();

        // Write simple values like the node's path to the JSON result object
        jsonWriter.key("path").value(node.getPath());

        // Write node properties from the hit result node (or relative nodes) to the JSON result object
        final String property1 = "jcr:content/jcr:title";
        if(node.hasProperty(property1)) {
            final Property property = node.getProperty(property1);
            // You have full control over the names/values of the JSON key/value pairs returned.
            // These do not have to match node names
            jsonWriter.key("key-to-use-in-json").value(property.getString() + "(pulled from jcr:content node)");
        }

        // Custom logic can be used to transform and/or retrieve data to be added to the resulting JSON object
        // Note: Keep this logic as light as possible. Complex logic can introduce performance issues that are
        // less visible (Will not appear in Slow Query logs as this logic executes after the actual Query returns).
        String complexValue = sampleComplexLogic(node);
        jsonWriter.key("complex").value(complexValue);
    }

    private String sampleComplexLogic(Node node) {
        // Perform any custom logic you want based on the hit node; This could be "sub-queries", or combining/scrubbing data.
        return "Hello World";
    }
}