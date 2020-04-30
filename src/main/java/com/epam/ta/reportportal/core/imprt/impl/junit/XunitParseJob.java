/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.imprt.impl.junit;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.imprt.impl.ParseResults;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class XunitParseJob implements Callable<ParseResults> {

	@Autowired
	private XunitImportHandler handler;

	private InputStream xmlInputStream;

	@Override
	public ParseResults call() {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser saxParser = spf.newSAXParser();
			XMLReader reader = saxParser.getXMLReader();

			// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
			// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities

			// Using the SAXParserFactory's setFeature
			spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			// Using the XMLReader's setFeature
			reader.setFeature("http://xml.org/sax/features/external-general-entities", false);

			// Xerces 2 only - http://xerces.apache.org/xerces-j/features.html#external-general-entities
			spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

			saxParser.parse(xmlInputStream, handler);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new ReportPortalException(ErrorType.PARSING_XML_ERROR, e.getMessage());
		}
		return new ParseResults(handler.getStartSuiteTime(), handler.getCommonDuration());
	}

	public XunitParseJob withParameters(ReportPortalUser.ProjectDetails projectDetails, String launchId, ReportPortalUser user,
			InputStream xmlInputStream) {
		this.xmlInputStream = xmlInputStream;
		this.handler = handler.withParameters(projectDetails, launchId, user);
		return this;
	}

}
