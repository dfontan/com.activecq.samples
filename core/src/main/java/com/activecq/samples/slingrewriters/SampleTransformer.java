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

import java.io.IOException;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Transformer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 *
 * @author david
 */
public class SampleTransformer implements Transformer {
    private ContentHandler contentHandler;
    private ProcessingComponentConfiguration config;
    private Locator documentLocator;
    private ProcessingContext processingContext;

    @Override
    public void init(ProcessingContext pc, ProcessingComponentConfiguration pcc) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setContentHandler(ContentHandler ch) {
        this.contentHandler = ch;
    }

    @Override
    public void dispose() {
        // Do Nothing
    }

    @Override
    public void setDocumentLocator(Locator lctr) {
        this.documentLocator = lctr;
    }

    @Override
    public void startDocument() throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void endDocument() throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startPrefixMapping(String string, String string1) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void endPrefixMapping(String string) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startElement(String string, String string1, String string2, Attributes atrbts) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void endElement(String string, String string1, String string2) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void characters(char[] chars, int i, int i1) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void ignorableWhitespace(char[] chars, int i, int i1) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void processingInstruction(String string, String string1) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void skippedEntity(String string) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
