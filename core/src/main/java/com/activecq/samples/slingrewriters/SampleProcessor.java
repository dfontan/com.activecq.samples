/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.activecq.samples.slingrewriters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.commons.io.IOUtils;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Processor;
import org.apache.sling.rewriter.ProcessorConfiguration;
import org.xml.sax.ContentHandler;

/**
 *
 * @author david
 */
class SampleProcessor implements Processor {

    private ProcessingContext processingContext;
    private ByteArrayOutputStream sampleOutputStream;
    private ByteArrayOutputStream bufferOutputStream;
    private PrintWriter samplePrintWriter;

    public SampleProcessor() {
    }

    @Override
    public void init(ProcessingContext pc, ProcessorConfiguration pc1) throws IOException {
        this.processingContext = pc;

        this.bufferOutputStream = new ByteArrayOutputStream();
        this.sampleOutputStream = new ByteArrayOutputStream();
        this.samplePrintWriter = new PrintWriter(bufferOutputStream);
    }

    @Override
    public PrintWriter getWriter() {
        return this.samplePrintWriter;
    }

    @Override
    public ContentHandler getContentHandler() {
        return null;
    }

    @Override
    public void finished(boolean bln) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(this.bufferOutputStream.toByteArray());
        // Make some changes

        if (this.sampleOutputStream != null && this.sampleOutputStream.toByteArray().length > 0) {
            IOUtils.write(this.sampleOutputStream.toByteArray(), this.processingContext.getOutputStream());
        } else {
            IOUtils.write(this.bufferOutputStream.toByteArray(), this.processingContext.getOutputStream());
        }
    }
}
