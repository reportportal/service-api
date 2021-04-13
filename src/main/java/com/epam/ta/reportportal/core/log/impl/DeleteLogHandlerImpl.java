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

package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.log.DeleteLogHandler;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;

/**
 * Delete Logs handler. Basic implementation of
 * {@link com.epam.ta.reportportal.core.log.DeleteLogHandler} interface.
 *
 * @author Henadzi_Vrubleuski
 * @author Andrei_Ramanchuk
 */
@Service
public class DeleteLogHandlerImpl implements DeleteLogHandler {

	private final LogRepository logRepository;

	private final AttachmentRepository attachmentRepository;

	private final ProjectRepository projectRepository;

	private final TestItemService testItemService;

	private final LogIndexer logIndexer;

	public DeleteLogHandlerImpl(LogRepository logRepository, ProjectRepository projectRepository, TestItemService testItemService,
			LogIndexer logIndexer, AttachmentRepository attachmentRepository) {
		this.logRepository = logRepository;
		this.projectRepository = projectRepository;
		this.testItemService = testItemService;
		this.logIndexer = logIndexer;
		this.attachmentRepository = attachmentRepository;
	}

	@Override
	public OperationCompletionRS deleteLog(Long logId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		BusinessRule.expect(projectRepository.existsById(projectDetails.getProjectId()), BooleanUtils::isTrue)
				.verify(PROJECT_NOT_FOUND, projectDetails.getProjectId());

		Log log = validate(logId, user, projectDetails);
		try {
			logRepository.delete(log);
			ofNullable(log.getAttachment()).ifPresent(attachment -> attachmentRepository.moveForDeletion(attachment.getId()));
		} catch (Exception exc) {
			throw new ReportPortalException("Error while Log instance deleting.", exc);
		}

		logIndexer.cleanIndex(projectDetails.getProjectId(), Collections.singletonList(logId));
		return new OperationCompletionRS(formattedSupplier("Log with ID = '{}' successfully deleted.", logId).toString());
	}

	/**
	 * Validate specified log against parent objects and project
	 *
	 * @param logId          - validated log ID value
	 * @param projectDetails Project details
	 * @return Log
	 */
	private Log validate(Long logId, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {

		Log log = logRepository.findById(logId).orElseThrow(() -> new ReportPortalException(ErrorType.LOG_NOT_FOUND, logId));

		Optional<TestItem> itemOptional = ofNullable(log.getTestItem());
		Launch launch = ofNullable(log.getTestItem()).map(testItemService::getEffectiveLaunch).orElseGet(log::getLaunch);

		//TODO check if statistics is right in item results
		if (itemOptional.isPresent()) {
			expect(itemOptional.get().getItemResults().getStatistics(), notNull()).verify(TEST_ITEM_IS_NOT_FINISHED, formattedSupplier(
					"Unable to delete log '{}' when test item '{}' in progress state",
					log.getId(),
					itemOptional.get().getItemId()
			));
		} else {
			expect(launch.getStatus(), not(statusIn(StatusEnum.IN_PROGRESS))).verify(LAUNCH_IS_NOT_FINISHED,
					formattedSupplier("Unable to delete log '{}' when launch '{}' in progress state", log.getId(), launch.getId())
			);
		}

		expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
				formattedSupplier("Log '{}' not under specified '{}' project", logId, projectDetails.getProjectId())
		);

		if (user.getUserRole() != UserRole.ADMINISTRATOR && !Objects.equals(user.getUserId(), launch.getUserId())) {
			/*
			 * Only PROJECT_MANAGER roles could delete logs
			 */
			expect(projectDetails.getProjectRole(), equalTo(ProjectRole.PROJECT_MANAGER)).verify(ACCESS_DENIED);
		}

		return log;
	}
}
