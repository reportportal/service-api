/*
 * Copyright (C) 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.core.project.IUpdateProjectHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.AnalyzeMode;
import com.epam.ta.reportportal.entity.enums.InterruptionJobDelay;
import com.epam.ta.reportportal.entity.enums.KeepLogsDelay;
import com.epam.ta.reportportal.entity.enums.KeepScreenshotsDelay;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.AssignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.UnassignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
@Service
public class UpdateProjectHandlerImpl implements IUpdateProjectHandler {

	private final ProjectRepository projectRepository;

	@Autowired
	public UpdateProjectHandlerImpl(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Override
	public OperationCompletionRS updateProject(ReportPortalUser.ProjectDetails projectDetails, UpdateProjectRQ updateProjectRQ,
			ReportPortalUser user) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		updateProjectUserRoles(updateProjectRQ.getUserRoles(), project, projectDetails, user);
		updateProjectConfiguration(updateProjectRQ.getConfiguration(), project, user);
		projectRepository.save(project);
		return new OperationCompletionRS("Project with name = '" + project.getName() + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS updateProjectEmailConfig(String projectName, String user, ProjectEmailConfigDTO updateProjectRQ) {
		return null;
	}

	@Override
	public OperationCompletionRS unassignUsers(String projectName, String modifier, UnassignUsersRQ unassignUsersRQ) {
		return null;
	}

	@Override
	public OperationCompletionRS assignUsers(String projectName, String modifier, AssignUsersRQ assignUsersRQ) {
		return null;
	}

	@Override
	public OperationCompletionRS indexProjectData(String projectName, String user) {
		return null;
	}

	private void updateProjectUserRoles(Map<String, String> userRoles, Project project, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {

		if (null != userRoles && !user.getUserRole().equals(UserRole.ADMINISTRATOR)) {
			expect(userRoles.get(user.getUsername()), isNull()).verify(ErrorType.UNABLE_TO_UPDATE_YOURSELF_ROLE, user.getUsername());
		}

		if (MapUtils.isNotEmpty(userRoles)) {
			userRoles.forEach((key, value) -> {

				Optional<ProjectRole> newProjectRole = ProjectRole.forName(value);
				expect(newProjectRole, isPresent()).verify(ErrorType.ROLE_NOT_FOUND, value);

				Optional<ProjectUser> updatingProjectUser = ofNullable(ProjectUtils.findUserConfigByLogin(project, key));
				expect(updatingProjectUser, notNull()).verify(ErrorType.USER_NOT_FOUND, key);

				if (UserRole.ADMINISTRATOR != user.getUserRole()) {
					ProjectRole principalRole = projectDetails.getProjectRole();
					ProjectRole updatingUserRole = ofNullable(ProjectUtils.findUserConfigByLogin(project,
							key
					)).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, key)).getProjectRole();
					/*
					 * Validate principal role level is high enough
					 */
					if (principalRole.sameOrHigherThan(updatingUserRole)) {
						expect(newProjectRole.get(), Preconditions.isLevelEnough(principalRole)).verify(ErrorType.ACCESS_DENIED);
					} else {
						expect(updatingUserRole, Preconditions.isLevelEnough(principalRole)).verify(ErrorType.ACCESS_DENIED);
					}
				}
				updatingProjectUser.get().setProjectRole(newProjectRole.get());
			});
		}
	}

	private void updateProjectConfiguration(ProjectConfiguration configuration, Project project, ReportPortalUser user) {
		ofNullable(configuration).ifPresent(config -> {
			ofNullable(config.getProjectAttributes()).ifPresent(attributes -> {
				verifyProjectAttributes(attributes);
				attributes.forEach((attribute, value) -> project.getProjectAttributes()
						.stream()
						.filter(it -> it.getAttribute().getName().equalsIgnoreCase(attribute))
						.findFirst()
						.ifPresent(attr -> attr.setValue(value)));
			});
			ofNullable(config.getEmailConfig()).ifPresent(emailConfig -> updateProjectEmailConfig(project.getName(),
					user.getUsername(),
					emailConfig
			));
		});

	}

	private void verifyProjectAttributes(Map<String, String> attributes) {
		ofNullable(attributes.get(ProjectAttributeEnum.KEEP_LOGS.getAttribute())).ifPresent(keepLogs -> expect(keepLogs,
				KeepLogsDelay::isPresent
		).verify(ErrorType.BAD_REQUEST_ERROR, keepLogs));
		ofNullable(attributes.get(ProjectAttributeEnum.INTERRUPT_JOB_TIME.getAttribute())).ifPresent(interruptedJob -> expect(interruptedJob,
				InterruptionJobDelay::isPresent
		).verify(ErrorType.BAD_REQUEST_ERROR, interruptedJob));
		ofNullable(attributes.get(ProjectAttributeEnum.KEEP_SCREENSHOTS.getAttribute())).ifPresent(keepScreenshots -> expect(keepScreenshots,
				KeepScreenshotsDelay::isPresent
		).verify(ErrorType.BAD_REQUEST_ERROR, keepScreenshots));
		ofNullable(attributes.get(ProjectAttributeEnum.AUTO_ANALYZER_MODE.getAttribute())).ifPresent(analyzerMode -> expect(AnalyzeMode.fromString(
				analyzerMode), isPresent()).verify(ErrorType.BAD_REQUEST_ERROR, analyzerMode));
	}
}
