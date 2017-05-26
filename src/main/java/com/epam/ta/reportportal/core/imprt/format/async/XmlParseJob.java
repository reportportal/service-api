package com.epam.ta.reportportal.core.imprt.format.async;

import com.epam.ta.reportportal.exception.ReportPortalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.inject.Provider;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

public class XmlParseJob implements Runnable {

    @Autowired
    private Provider<AsyncXmlImportHandler> asyncImportHandlerProvider;

    private AsyncXmlImportHandler handler;

    private InputStream xmlInputStream;

    @Override
    public void run() {
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(xmlInputStream, handler);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new ReportPortalException("Xml parse job problem.", e);
        }
    }

    XmlParseJob withParameters(String projectId, String launchId, String user, InputStream xmlInputStream) {
        this.xmlInputStream = xmlInputStream;
        this.handler = asyncImportHandlerProvider.get().withParameters(projectId, launchId, user);
        return this;
    }

}
