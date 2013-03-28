package com.activecq.samples.contentfinder.querybuilder;

import com.activecq.samples.contentfinder.ContentFinderHitBuilder;
import com.day.cq.search.Query;
import com.day.cq.search.result.Hit;
import com.day.cq.search.writer.ResultHitWriter;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;

import javax.jcr.RepositoryException;
import java.util.Map;

/**
 * User: david
 */
@Component(
        label = "Samples - ContentFinder Result Hit Writer",
        description = "",
        factory = "com.day.cq.search.result.ResultHitWriter/cf",
        immediate = false,
        metatype = false
)
@Properties({
        @Property(
                label = "Vendor",
                name = Constants.SERVICE_VENDOR,
                value = "ActiveCQ",
                propertyPrivate = true
        )
})
public class ContentFinderResultHitWriter implements ResultHitWriter {
    /**
     * Result hit writer integration
     *
     * @param hit
     * @param jsonWriter
     * @param query
     * @throws RepositoryException
     * @throws JSONException
     */
    @Override
    public void write(Hit hit, JSONWriter jsonWriter, Query query) throws RepositoryException, JSONException {
        Map<String, Object> map = ContentFinderHitBuilder.buildGenericResult(hit);

        jsonWriter.object();

        for (final String key : map.keySet()) {
            jsonWriter.key(key).value(map.get(key));
        }

        jsonWriter.endObject();
    }

    /**
     * OSGi Component Methods *
     */
    @Activate
    protected void activate(final ComponentContext componentContext) throws Exception {
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
    }
}
