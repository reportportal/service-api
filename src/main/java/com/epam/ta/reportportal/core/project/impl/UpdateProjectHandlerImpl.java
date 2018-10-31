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
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.EmailConfigUpdatedEvent;
import com.epam.ta.reportportal.core.project.UpdateProjectHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.AnalyzeMode;
import com.epam.ta.reportportal.entity.enums.InterruptionJobDelay;
import com.epam.ta.reportportal.entity.enums.KeepLogsDelay;
import com.epam.ta.reportportal.entity.enums.KeepScreenshotsDelay;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.AssignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.UnassignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCaseDTO;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;
import com.google.common.base.Strings;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.epam.ta.reportportal.commons.Preconditions.IS_PRESENT;
import static com.epam.ta.reportportal.commons.Preconditions.contains;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * @author Pavel Bortnik
 */
@Service
public class UpdateProjectHandlerImpl implements UpdateProjectHandler {

	private final ProjectRepository projectRepository;

	private final UserRepository userRepository;

	private final MessageBus messageBus;

	@Autowired
	public UpdateProjectHandlerImpl(ProjectRepository projectRepository, UserRepository userRepository, MessageBus messageBus) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.messageBus = messageBus;
	}

	@Override
	public OperationCompletionRS updateProject(ReportPortalUser.ProjectDetails projectDetails, UpdateProjectRQ updateProjectRQ,
			ReportPortalUser user) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		updateProjectUserRoles(updateProjectRQ.getUserRoles(), project, projectDetails, user);
		updateProjectConfiguration(updateProjectRQ.getConfiguration(), project, projectDetails, user);
		projectRepository.save(project);
		return new OperationCompletionRS("Project with name = '" + project.getName() + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS updateProjectEmailConfig(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			ProjectEmailConfigDTO updateProjectRQ) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectId()));
		Project before = SerializationUtils.clone(project);
		boolean emailEnabled = BooleanUtils.isTrue(updateProjectRQ.getEmailEnabled());
		project.getProjectAttributes()
				.stream()
				.filter(it -> it.getAttribute().getName().equalsIgnoreCase(ProjectAttributeEnum.EMAIL_ENABLED.getAttribute()))
				.findFirst()
				.ifPresent(it -> it.setValue(String.valueOf(emailEnabled)));

		updateEmailAttributes(project.getProjectAttributes(), updateProjectRQ.getEmailEnabled(), updateProjectRQ.getFrom());
		updateEmailCases(project, updateProjectRQ.getEmailCases());

		messageBus.publishActivity(new EmailConfigUpdatedEvent(before, updateProjectRQ, user.getUserId()));
		return new OperationCompletionRS(
				"EMail configuration of project with id = '" + projectDetails.getProjectId() + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS unassignUsers(ReportPortalUser.ProjectDetails projectDetails, UnassignUsersRQ unassignUsersRQ,
			ReportPortalUser user) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));
		User modifier = userRepository.findById(user.getUserId())
				.orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, user.getUsername()));

		if (!UserRole.ADMINISTRATOR.equals(modifier.getRole())) {
			expect(unassignUsersRQ.getUsernames(), not(contains(equalTo(modifier.getLogin())))).verify(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT,
					"User should not unassign himself from project."
			);
		}

		List<User> unassignUsers = new ArrayList<>(unassignUsersRQ.getUsernames().size());
		unassignUsersRQ.getUsernames().forEach(username -> {
			User userForUnassign = userRepository.findByLogin(username)
					.orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, username));
			validateUnassigningUser(modifier, userForUnassign, projectDetails, project);
			project.getUsers().removeIf(it -> it.getUser().getLogin().equalsIgnoreCase(username));
			userForUnassign.getProjects().removeIf(it -> it.getProject().getName().equalsIgnoreCase(project.getName()));
			unassignUsers.add(userForUnassign);
		});
		ProjectUtils.excludeProjectRecipients(unassignUsers, project);

		//		prferenceRepository.removeByUsernamesAndProject(unassignUsersRQ.getUsernames(), project.getId());

		OperationCompletionRS response = new OperationCompletionRS();
		String msg = "User(s) with username(s)='" + unassignUsersRQ.getUsernames() + "' was successfully un-assigned from project='"
				+ project.getName() + "'";
		response.setResultMessage(msg);
		return response;
	}

	@Override
	public OperationCompletionRS assignUsers(ReportPortalUser.ProjectDetails projectDetails, AssignUsersRQ assignUsersRQ,
			ReportPortalUser user) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		if (!UserRole.ADMINISTRATOR.equals(user.getUserRole())) {
			expect(assignUsersRQ.getUserNames().keySet(), not(Preconditions.contains(equalTo(user.getUsername())))).verify(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT,
					"User should not assign himself to project."
			);
		}

		List<String> assignedUsernames = project.getUsers().stream().map(u -> u.getUser().getLogin()).collect(toList());

		assignUsersRQ.getUserNames().forEach((name, role) -> {
			User modifyingUser = userRepository.findByLogin(name).orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, name));
			expect(name, not(in(assignedUsernames))).verify(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT,
					formattedSupplier("User '{}' cannot be assigned to project twice.", name)
			);
			if (ProjectType.UPSA.equals(project.getProjectType()) && UserType.UPSA.equals(modifyingUser.getUserType())) {
				fail().withError(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, "Project and user has UPSA type!");
			}
			ProjectUser projectUser = new ProjectUser();
			if (!Strings.isNullOrEmpty(role)) {
				Optional<ProjectRole> projectRole = ProjectRole.forName(role);
				expect(projectRole, IS_PRESENT).verify(ROLE_NOT_FOUND, role);

				if (!UserRole.ADMINISTRATOR.equals(user.getUserRole())) {
					ProjectRole modifierRole = projectDetails.getProjectRole();
					expect(modifierRole.sameOrHigherThan(projectRole.get()), BooleanUtils::isTrue).verify(ACCESS_DENIED);
					projectUser.setProjectRole(projectRole.get());
				} else {
					projectUser.setProjectRole(projectRole.get());
				}
			} else {
				projectUser.setProjectRole(ProjectRole.MEMBER);
			}
			projectUser.setUser(modifyingUser);
			projectUser.setProject(project);
			project.getUsers().add(projectUser);
		});
		OperationCompletionRS response = new OperationCompletionRS();
		String msg = "User(s) with username='" + assignUsersRQ.getUserNames().keySet() + "' was successfully assigned to project='"
				+ project.getName() + "'";
		response.setResultMessage(msg);
		return response;
	}

	@Override
	public OperationCompletionRS indexProjectData(String projectName, String user) {
		return null;
	}

	private void validateUnassigningUser(User modifier, User userForUnassign, ReportPortalUser.ProjectDetails projectDetails,
			Project project) {
		if (ProjectType.PERSONAL.equals(project.getProjectType()) && project.getName().startsWith(userForUnassign.getLogin())) {
			fail().withError(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, "Unable to unassign user from his personal project");
		}
		if (ProjectType.UPSA.equals(project.getProjectType()) && UserType.UPSA.equals(userForUnassign.getUserType())) {
			fail().withError(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, "Project and user has UPSA type!");
		}
		if (!ProjectUtils.doesHaveUser(project, userForUnassign.getLogin())) {
			fail().withError(USER_NOT_FOUND, userForUnassign.getLogin(), String.format("User not found in project %s", project.getName()));
		}

		ProjectUser projectUser = userForUnassign.getProjects()
				.stream()
				.filter(it -> Objects.equals(it.getProject().getId(), projectDetails.getProjectId()))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND,
						userForUnassign.getLogin(),
						String.format("User not found in project %s", project.getName())
				));

		if (!UserRole.ADMINISTRATOR.equals(modifier.getRole())) {
			expect(projectDetails.getProjectRole().sameOrHigherThan(projectUser.getProjectRole()), BooleanUtils::isTrue).verify(
					ACCESS_DENIED);
		}
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

	private void updateProjectConfiguration(ProjectConfiguration configuration, Project project,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		ofNullable(configuration).ifPresent(config -> {
			ofNullable(config.getProjectAttributes()).ifPresent(attributes -> {
				verifyProjectAttributes(attributes);
				attributes.forEach((attribute, value) -> project.getProjectAttributes()
						.stream()
						.filter(it -> it.getAttribute().getName().equalsIgnoreCase(attribute))
						.findFirst()
						.ifPresent(attr -> attr.setValue(value)));
			});
			ofNullable(config.getEmailConfig()).ifPresent(emailConfig -> updateProjectEmailConfig(projectDetails, user, emailConfig));
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

	private void updateEmailCases(Project project, List<EmailSenderCaseDTO> emailCases) {

	}

	private void updateEmailAttributes(Set<ProjectAttribute> projectAttributes, Boolean emailEnabled, String from) {

	}
}
