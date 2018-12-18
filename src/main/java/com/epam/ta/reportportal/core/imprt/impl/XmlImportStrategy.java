/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.imprt.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.imprt.impl.junit.XunitParseJob;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static com.epam.ta.reportportal.core.imprt.FileExtensionConstant.XML_EXTENSION;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class XmlImportStrategy extends AbstractImportStrategy {

	@Autowired
	private Provider<XunitParseJob> xmlParseJobProvider;

	@Override
	public Long importLaunch(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, File file) {
		try {
			return processXmlFile(file, projectDetails, user);
		} finally {
			try {
				ofNullable(file).ifPresent(File::delete);
			} catch (Exception e) {
				LOGGER.error("File '{}' was not successfully deleted.", file.getName(), e);
			}
		}
	}

	private Long processXmlFile(File xml, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		//copy of the launch's id to use it in catch block if something goes wrong
		Long savedLaunchId = null;
		try (InputStream xmlStream = new FileInputStream(xml)) {
			Long launchId = startLaunch(projectDetails, user, xml.getName().substring(0, xml.getName().indexOf("." + XML_EXTENSION)));
			savedLaunchId = launchId;
			XunitParseJob job = xmlParseJobProvider.get().withParameters(projectDetails, launchId, user, xmlStream);
			ParseResults parseResults = job.call();
			finishLaunch(launchId, projectDetails, user, parseResults);
			return launchId;
		} catch (Exception e) {
			updateBrokenLaunch(savedLaunchId);
			throw new ReportPortalException(ErrorType.IMPORT_FILE_ERROR, cleanMessage(e));
		}
	}
}
