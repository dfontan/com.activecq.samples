/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.activecq.samples.slingrewriters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.sling.rewriter.Generator;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author david
 */
class SampleGenerator implements Generator {
    private final StringWriter writer;
    private ContentHandler contentHandler;

    public SampleGenerator() {
        this.writer = new StringWriter();
    }

    @Override
    public void init(ProcessingContext pc, ProcessingComponentConfiguration pcc) throws IOException {
        // Do Nothing
    }

    @Override
    public void setContentHandler(ContentHandler ch) {
            this.contentHandler = ch;
    }

    @Override
    public PrintWriter getWriter() {
        return new PrintWriter(writer);
    }

    @Override
    public void finished() throws IOException, SAXException {
        ByteArrayInputStream bais = new ByteArrayInputStream(this.writer.toString().getBytes("UTF-8"));
    }

    @Override
    public void dispose() {
        // Do Nothing
    }

}
