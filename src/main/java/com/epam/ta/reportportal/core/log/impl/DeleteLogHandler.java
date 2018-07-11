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

package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.service.DataStoreService;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Delete Logs handler. Basic implementation of
 * {@link com.epam.ta.reportportal.core.log.IDeleteLogHandler} interface.
 *
 * @author Henadzi_Vrubleuski
 * @author Andrei_Ramanchuk
 */
@Service
public class DeleteLogHandler {

	private final LogRepository logRepository;

	private final DataStoreService dataStoreService;

	private final ProjectRepository projectRepository;

	public DeleteLogHandler(LogRepository logRepository, DataStoreService dataStoreService, ProjectRepository projectRepository) {
		this.logRepository = logRepository;
		this.dataStoreService = dataStoreService;
		this.projectRepository = projectRepository;
	}

	public OperationCompletionRS deleteLog(String logId, String projectName, ReportPortalUser user) {

		Project project = projectRepository.findByName(projectName).orElse(null);
		expect(project, notNull()).verify(ErrorType.PROJECT_NOT_FOUND, projectName);

		Log log = validate(logId, user, projectName);

		try {

			logRepository.delete(log);
			cleanUpLogData(log);
		} catch (Exception exc) {
			throw new ReportPortalException("Error while Log instance deleting.", exc);
		}

		return new OperationCompletionRS("Log with ID = '" + logId + "' successfully deleted.");
	}

	private void cleanUpLogData(Log log) {

		if (StringUtils.isNotEmpty(log.getFilePath())) {

			dataStoreService.delete(log.getFilePath());
		}
		if (StringUtils.isNotEmpty(log.getThumbnailFilePath())) {

			dataStoreService.delete(log.getThumbnailFilePath());
		}
	}

	/**
	 * Validate specified log against parent objects and project
	 *
	 * @param logId       - validated log ID value
	 * @param projectName - specified project name
	 * @return Log
	 */
	private Log validate(String logId, ReportPortalUser user, String projectName) {

		Log log = logRepository.findById(Long.valueOf(logId)).orElse(null);
		expect(log, notNull()).verify(ErrorType.LOG_NOT_FOUND, logId);

		final TestItem testItem = log.getTestItem();

		expect(testItem.getItemStructure().getItemResults().getExecutionStatistics(), notNull()).verify(
				TEST_ITEM_IS_NOT_FINISHED,
				formattedSupplier("Unable to delete log '{}' when test item '{}' in progress state", log.getId(), testItem.getItemId())
		);

		Launch launch = testItem.getItemStructure().getLaunch();

		ReportPortalUser.ProjectDetails projectDetails = ProjectUtils.extractProjectDetails(user, projectName);
		expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
				formattedSupplier("Log '{}' not under specified '{}' project", logId, projectName)
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
