/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.auth.acl.ShareableObjectsHandler;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.NotificationsConfigUpdatedEvent;
import com.epam.ta.reportportal.core.events.activity.ProjectIndexEvent;
import com.epam.ta.reportportal.core.events.activity.ProjectUpdatedEvent;
import com.epam.ta.reportportal.core.project.UpdateProjectHandler;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.AnalyzeMode;
import com.epam.ta.reportportal.entity.enums.*;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.email.EmailRulesValidator;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.converter.converters.NotificationConfigConverter;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.activity.ProjectAttributesActivityResource;
import com.epam.ta.reportportal.ws.model.project.AssignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.project.UnassignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.config.ProjectConfigurationUpdate;
import com.epam.ta.reportportal.ws.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Preconditions.contains;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.enums.SendCase.findByName;
import static com.epam.ta.reportportal.ws.converter.converters.ProjectActivityConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Pavel Bortnik
 */
@Service
public class UpdateProjectHandlerImpl implements UpdateProjectHandler {

	private final ProjectRepository projectRepository;

	private final UserRepository userRepository;

	private final UserPreferenceRepository preferenceRepository;

	private final ProjectUserRepository projectUserRepository;

	private final MessageBus messageBus;

	private final MailServiceFactory mailServiceFactory;

	private final LaunchRepository launchRepository;

	private final AnalyzerStatusCache analyzerStatusCache;

	private final AnalyzerServiceClient analyzerServiceClient;

	private final LogIndexer logIndexer;

	private final ShareableObjectsHandler aclHandler;

	private final ProjectConverter projectConverter;

	@Autowired
	public UpdateProjectHandlerImpl(ProjectRepository projectRepository, UserRepository userRepository,
			UserPreferenceRepository preferenceRepository, MessageBus messageBus, ProjectUserRepository projectUserRepository,
			MailServiceFactory mailServiceFactory, LaunchRepository launchRepository, AnalyzerStatusCache analyzerStatusCache,
			AnalyzerServiceClient analyzerServiceClient, LogIndexer logIndexer, ShareableObjectsHandler aclHandler,
			ProjectConverter projectConverter) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.preferenceRepository = preferenceRepository;
		this.messageBus = messageBus;
		this.projectUserRepository = projectUserRepository;
		this.mailServiceFactory = mailServiceFactory;
		this.launchRepository = launchRepository;
		this.analyzerStatusCache = analyzerStatusCache;
		this.analyzerServiceClient = analyzerServiceClient;
		this.logIndexer = logIndexer;
		this.aclHandler = aclHandler;
		this.projectConverter = projectConverter;
	}

	@Override
	public OperationCompletionRS updateProject(ReportPortalUser.ProjectDetails projectDetails, UpdateProjectRQ updateProjectRQ,
			ReportPortalUser user) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectId()));
		ProjectAttributesActivityResource before = TO_ACTIVITY_RESOURCE.apply(project);

		updateProjectUserRoles(updateProjectRQ.getUserRoles(), project, projectDetails, user);
		updateProjectConfiguration(updateProjectRQ.getConfiguration(), project);
		projectRepository.save(project);
		messageBus.publishActivity(new ProjectUpdatedEvent(before, TO_ACTIVITY_RESOURCE.apply(project), user.getUserId()));
		return new OperationCompletionRS("Project with name = '" + project.getName() + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS updateProjectNotificationConfig(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			ProjectNotificationConfigDTO updateProjectNotificationConfigRQ) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectId()));
		ProjectResource before = projectConverter.TO_PROJECT_RESOURCE.apply(project);

		updateSenderCases(project, updateProjectNotificationConfigRQ.getSenderCases());

		messageBus.publishActivity(new NotificationsConfigUpdatedEvent(before, updateProjectNotificationConfigRQ, user.getUserId()));
		return new OperationCompletionRS(
				"EMail configuration of project with id = '" + projectDetails.getProjectId() + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS unassignUsers(ReportPortalUser.ProjectDetails projectDetails, UnassignUsersRQ unassignUsersRQ,
			ReportPortalUser user) {
		expect(unassignUsersRQ.getUsernames(), not(List::isEmpty)).verify(BAD_REQUEST_ERROR,
				"Request should contain at least one username."
		);
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));
		User modifier = userRepository.findById(user.getUserId())
				.orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, user.getUsername()));
		if (!UserRole.ADMINISTRATOR.equals(modifier.getRole())) {
			expect(unassignUsersRQ.getUsernames(), not(contains(equalTo(modifier.getLogin())))).verify(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT,
					"User should not unassign himself from project."
			);
		}
		List<ProjectUser> unassignUsers = new ArrayList<>(unassignUsersRQ.getUsernames().size());
		unassignUsersRQ.getUsernames().forEach(username -> {
			User userForUnassign = userRepository.findByLogin(username)
					.orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, username));
			validateUnassigningUser(modifier, userForUnassign, projectDetails, project);
			ProjectUser projectUser = project.getUsers()
					.stream()
					.filter(it -> it.getUser().getLogin().equalsIgnoreCase(username))
					.findFirst()
					.orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, username));
			project.getUsers().remove(projectUser);
			userForUnassign.getProjects().remove(projectUser);
			unassignUsers.add(projectUser);
			aclHandler.preventSharedObjects(project.getId(), username);
		});

		projectUserRepository.deleteAll(unassignUsers);
		ProjectUtils.excludeProjectRecipients(unassignUsers.stream().map(ProjectUser::getUser).collect(Collectors.toSet()), project);
		unassignUsers.forEach(it -> preferenceRepository.removeByProjectIdAndUserId(projectDetails.getProjectId(), it.getUser().getId()));

		return new OperationCompletionRS(
				"User(s) with username(s)='" + unassignUsersRQ.getUsernames() + "' was successfully un-assigned from project='"
						+ project.getName() + "'");
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
			ProjectRole projectRole = ProjectRole.forName(role).orElseThrow(() -> new ReportPortalException(ROLE_NOT_FOUND, role));
			if (!UserRole.ADMINISTRATOR.equals(user.getUserRole())) {
				ProjectRole modifierRole = projectDetails.getProjectRole();
				expect(modifierRole.sameOrHigherThan(projectRole), BooleanUtils::isTrue).verify(ACCESS_DENIED);
				projectUser.setProjectRole(projectRole);
			} else {
				projectUser.setProjectRole(projectRole);
			}
			projectUser.setUser(modifyingUser);
			projectUser.setProject(project);
			project.getUsers().add(projectUser);
			aclHandler.permitSharedObjects(project.getId(), name);
		});

		return new OperationCompletionRS(
				"User(s) with username='" + assignUsersRQ.getUserNames().keySet() + "' was successfully assigned to project='"
						+ project.getName() + "'");
	}

	@Override
	public OperationCompletionRS indexProjectData(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {

		expect(ofNullable(analyzerStatusCache.getIndexingStatus().getIfPresent(projectDetails.getProjectId())).orElse(false),
				equalTo(false)
		).verify(ErrorType.FORBIDDEN_OPERATION, "Index can not be removed until index generation proceeds.");

		expect(analyzerStatusCache.getAnalyzeStatus().asMap().containsValue(projectDetails.getProjectId()), equalTo(false)).verify(
				ErrorType.FORBIDDEN_OPERATION,
				"Index can not be removed until auto-analysis proceeds."
		);

		expect(analyzerServiceClient.hasClients(), Predicate.isEqual(true)).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"There are no analyzer's."
		);

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		List<Long> launches = launchRepository.findLaunchIdsByProjectId(projectDetails.getProjectId());

		logIndexer.deleteIndex(projectDetails.getProjectId());

		logIndexer.indexLogs(project.getId(), launches, AnalyzerUtils.getAnalyzerConfig(project)).thenAcceptAsync(indexedCount -> {
			mailServiceFactory.getDefaultEmailService(true)
					.sendIndexFinishedEmail("Index generation has been finished", user.getEmail(), indexedCount);
		});

		messageBus.publishActivity(new ProjectIndexEvent(project.getId(), project.getName(), user.getUserId(), true));
		return new OperationCompletionRS("Log indexing has been started");
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

	private void updateProjectConfiguration(ProjectConfigurationUpdate configuration, Project project) {
		ofNullable(configuration).ifPresent(config -> ofNullable(config.getProjectAttributes()).ifPresent(attributes -> {
			verifyProjectAttributes(attributes);
			attributes.forEach((attribute, value) -> project.getProjectAttributes()
					.stream()
					.filter(it -> it.getAttribute().getName().equalsIgnoreCase(attribute))
					.findFirst()
					.ifPresent(attr -> attr.setValue(value)));
		}));
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

	private void updateSenderCases(Project project, List<SenderCaseDTO> cases) {

		expect(cases, Preconditions.NOT_EMPTY_COLLECTION).verify(BAD_REQUEST_ERROR, "At least one rule should be present.");
		cases.forEach(sendCase -> {
			expect(findByName(sendCase.getSendCase()).isPresent(), equalTo(true)).verify(BAD_REQUEST_ERROR, sendCase.getSendCase());
			expect(sendCase.getRecipients(), notNull()).verify(BAD_REQUEST_ERROR, "Recipients list should not be null");
			expect(sendCase.getRecipients().isEmpty(), equalTo(false)).verify(BAD_REQUEST_ERROR,
					formattedSupplier("Empty recipients list for email case '{}' ", sendCase)
			);
			sendCase.setRecipients(sendCase.getRecipients().stream().map(it -> {
				EmailRulesValidator.validateRecipient(project, it);
				return it.trim();
			}).distinct().collect(toList()));

			ofNullable(sendCase.getLaunchNames()).ifPresent(launchNames -> sendCase.setLaunchNames(launchNames.stream().map(name -> {
				EmailRulesValidator.validateLaunchName(name);
				return name.trim();
			}).distinct().collect(toList())));

			ofNullable(sendCase.getAttributes()).ifPresent(attributes -> sendCase.setAttributes(attributes.stream().peek(attribute -> {
				EmailRulesValidator.validateLaunchAttribute(attribute);
				attribute.setValue(attribute.getValue().trim());
			}).collect(Collectors.toSet())));

		});

		/* If project email settings */
		Set<SenderCase> withoutDuplicateCases = cases.stream()
				.distinct()
				.map(NotificationConfigConverter.TO_CASE_MODEL)
				.peek(sc -> sc.setProject(project))
				.collect(toSet());
		if (cases.size() != withoutDuplicateCases.size()) {
			fail().withError(BAD_REQUEST_ERROR, "Project email settings contain duplicate cases");
		}

		project.getSenderCases().clear();
		project.getSenderCases().addAll(withoutDuplicateCases);
	}

}
