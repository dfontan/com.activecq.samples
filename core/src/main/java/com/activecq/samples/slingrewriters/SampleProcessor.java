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
