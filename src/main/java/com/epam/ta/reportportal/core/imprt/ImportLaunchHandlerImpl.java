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
package com.epam.ta.reportportal.core.imprt;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ImportFinishedEvent;
import com.epam.ta.reportportal.core.events.activity.ImportStartedEvent;
import com.epam.ta.reportportal.core.imprt.impl.ImportStrategy;
import com.epam.ta.reportportal.core.imprt.impl.ImportStrategyFactory;
import com.epam.ta.reportportal.core.imprt.impl.ImportType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
	private MessageBus messageBus;

	@Autowired
	public ImportLaunchHandlerImpl(ImportStrategyFactory importStrategyFactory, MessageBus messageBus) {
		this.importStrategyFactory = importStrategyFactory;
		this.messageBus = messageBus;
	}

	@Override
	public OperationCompletionRS importLaunch(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, String format,
			MultipartFile file) {

		validate(file);

		ImportType type = ImportType.fromValue(format)
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Unknown import type - " + format));

		File tempFile = transferToTempFile(file);
		messageBus.publishActivity(new ImportStartedEvent(user.getUserId(),
				user.getUsername(),
				projectDetails.getProjectId(),
				file.getOriginalFilename()
		));
		ImportStrategy strategy = importStrategyFactory.getImportStrategy(type, file.getOriginalFilename());
		Long launchId = strategy.importLaunch(projectDetails, user, tempFile);
		messageBus.publishActivity(new ImportFinishedEvent(user.getUserId(),
				user.getUsername(),
				projectDetails.getProjectId(),
				file.getOriginalFilename()
		));
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
