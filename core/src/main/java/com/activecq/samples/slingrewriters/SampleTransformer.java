/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
