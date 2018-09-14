/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.imprt.impl.junit;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.imprt.impl.CallableImportJob;
import com.epam.ta.reportportal.core.imprt.impl.ParseResults;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class XunitParseJob extends CallableImportJob {
	@Autowired
	private XunitImportHandler handler;

	private InputStream xmlInputStream;

	@Override
	public ParseResults call() {
		try {
			SAXParserFactory.newInstance().newSAXParser().parse(xmlInputStream, handler);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new ReportPortalException(ErrorType.PARSING_XML_ERROR, e.getMessage());
		}
		return new ParseResults(handler.getStartSuiteTime(), handler.getCommonDuration());
	}

	@Override
	protected XunitParseJob withParameters(ReportPortalUser.ProjectDetails projectDetails, Long launchId, ReportPortalUser user,
			InputStream xmlInputStream) {
		this.xmlInputStream = xmlInputStream;
		this.handler = handler.withParameters(projectDetails, launchId, user);
		return this;
	}

}
