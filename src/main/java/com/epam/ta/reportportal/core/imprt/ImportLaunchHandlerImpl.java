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

import com.epam.ta.reportportal.core.imprt.impl.ImportStrategy;
import com.epam.ta.reportportal.core.imprt.impl.ImportStrategyFactoryImpl;
import com.epam.ta.reportportal.core.imprt.impl.ImportType;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.events.ImportFinishedEvent;
import com.epam.ta.reportportal.events.ImportStartedEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;

@Service
public class ImportLaunchHandlerImpl implements ImportLaunchHandler {

	private static final String ZIP_REGEX = ".*zip";

	@Autowired
	private ImportStrategyFactoryImpl factory;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Override
	public OperationCompletionRS importLaunch(String projectId, String userName, String format, MultipartFile file) {
		Project project = projectRepository.findOne(projectId);

		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectId);
		expect(file.getOriginalFilename(), it -> it.matches(ZIP_REGEX)).verify(
				INCORRECT_REQUEST, "Should be a zip archive" + file.getOriginalFilename());

		ImportType type = ImportType.fromValue(format).orElse(null);
		expect(type, notNull()).verify(ErrorType.BAD_REQUEST_ERROR, format);

		File tempFile = transferToTempFile(file);
		eventPublisher.publishEvent(new ImportStartedEvent(projectId, userName, file.getOriginalFilename()));
		ImportStrategy strategy = factory.getImportLaunch(type);
		String launch = strategy.importLaunch(projectId, userName, tempFile);
		eventPublisher.publishEvent(new ImportFinishedEvent(projectId, userName, file.getOriginalFilename()));
		return new OperationCompletionRS("Launch with id = " + launch + " is successfully imported.");
	}

	private File transferToTempFile(MultipartFile file) {
		try {
			File tmp = File.createTempFile(file.getOriginalFilename(), ".zip");
			file.transferTo(tmp);
			return tmp;
		} catch (IOException e) {
			throw new ReportPortalException("Error during transferring multipart file", e);
		}
	}
}
