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
package com.epam.ta.reportportal.core.imprt;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.ImportFinishedEvent;
import com.epam.ta.reportportal.core.events.activity.ImportStartedEvent;
import com.epam.ta.reportportal.core.imprt.impl.ImportStrategy;
import com.epam.ta.reportportal.core.imprt.impl.ImportStrategyFactory;
import com.epam.ta.reportportal.core.imprt.impl.ImportStrategyFactoryImpl;
import com.epam.ta.reportportal.core.imprt.impl.ImportType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.imprt.FileExtensionConstant.XML_EXTENSION;
import static com.epam.ta.reportportal.core.imprt.FileExtensionConstant.ZIP_EXTENSION;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;

@Service
public class ImportLaunchHandlerImpl implements ImportLaunchHandler {
	private ImportStrategyFactory importStrategyFactory;
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	public ImportLaunchHandlerImpl(ImportStrategyFactoryImpl importStrategyFactory, ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
		this.importStrategyFactory = importStrategyFactory;
	}

	@Override
	public OperationCompletionRS importLaunch(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, String format,
			MultipartFile file) {

		validate(file);

		ImportType type = ImportType.fromValue(format)
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Unknown import type - " + format));

		File tempFile = transferToTempFile(file);
		eventPublisher.publishEvent(new ImportStartedEvent(projectDetails.getProjectId(), user.getUserId(), file.getOriginalFilename()));
		ImportStrategy strategy = importStrategyFactory.getImportStrategy(type, file.getOriginalFilename());
		Long launchId = strategy.importLaunch(projectDetails, user, tempFile);
		eventPublisher.publishEvent(new ImportFinishedEvent(projectDetails.getProjectId(), user.getUserId(), file.getOriginalFilename()));
		return new OperationCompletionRS("Launch with id = " + launchId + " is successfully imported.");
	}

	private void validate(MultipartFile file) {
		expect(file.getOriginalFilename(), notNull()).verify(ErrorType.INCORRECT_REQUEST, "File name should be not empty.");

		expect(file.getOriginalFilename(), it -> it.endsWith(ZIP_EXTENSION) || it.endsWith(XML_EXTENSION)).verify(INCORRECT_REQUEST,
				"Should be a zip archive or an xml file " + file.getOriginalFilename()
		);
	}

	private File transferToTempFile(MultipartFile file) {
		try {
			File tmp = File.createTempFile(file.getOriginalFilename(), "." + FilenameUtils.getExtension(file.getOriginalFilename()));
			file.transferTo(tmp);
			return tmp;
		} catch (IOException e) {
			throw new ReportPortalException("Error during transferring multipart file.", e);
		}
	}
}
