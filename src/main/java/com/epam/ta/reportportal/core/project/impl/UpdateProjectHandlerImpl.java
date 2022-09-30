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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.reportportal.extension.event.ProjectEvent;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.analyzer.auto.indexer.IndexerStatusCache;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.NotificationsConfigUpdatedEvent;
import com.epam.ta.reportportal.core.events.activity.ProjectAnalyzerConfigEvent;
import com.epam.ta.reportportal.core.events.activity.ProjectIndexEvent;
import com.epam.ta.reportportal.core.events.activity.ProjectUpdatedEvent;
import com.epam.ta.reportportal.core.project.UpdateProjectHandler;
import com.epam.ta.reportportal.core.project.validator.attribute.ProjectAttributeValidator;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.UserPreferenceRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.ProjectType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ProjectExtractor;
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
import com.google.common.cache.Cache;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.commons.Preconditions.contains;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache.AUTO_ANALYZER_KEY;
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

	private static final String UPDATE_EVENT = "update";

	private final ProjectExtractor projectExtractor;

	private final ProjectAttributeValidator projectAttributeValidator;

	private final ProjectRepository projectRepository;

	private final UserRepository userRepository;

	private final UserPreferenceRepository preferenceRepository;

	private final ProjectUserRepository projectUserRepository;

	private final MessageBus messageBus;

	private final ApplicationEventPublisher applicationEventPublisher;

	private final MailServiceFactory mailServiceFactory;

	private final AnalyzerStatusCache analyzerStatusCache;

	private final IndexerStatusCache indexerStatusCache;

	private final AnalyzerServiceClient analyzerServiceClient;

	private final LogIndexer logIndexer;

	private final ProjectConverter projectConverter;

	@Autowired
	public UpdateProjectHandlerImpl(ProjectExtractor projectExtractor, ProjectAttributeValidator projectAttributeValidator,
			ProjectRepository projectRepository, UserRepository userRepository, UserPreferenceRepository preferenceRepository,
			MessageBus messageBus, ProjectUserRepository projectUserRepository, ApplicationEventPublisher applicationEventPublisher,
			MailServiceFactory mailServiceFactory, AnalyzerStatusCache analyzerStatusCache, IndexerStatusCache indexerStatusCache,
			AnalyzerServiceClient analyzerServiceClient, LogIndexer logIndexer,
			ProjectConverter projectConverter) {
		this.projectExtractor = projectExtractor;
		this.projectAttributeValidator = projectAttributeValidator;
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.preferenceRepository = preferenceRepository;
		this.messageBus = messageBus;
		this.projectUserRepository = projectUserRepository;
		this.applicationEventPublisher = applicationEventPublisher;
		this.mailServiceFactory = mailServiceFactory;
		this.analyzerStatusCache = analyzerStatusCache;
		this.indexerStatusCache = indexerStatusCache;
		this.analyzerServiceClient = analyzerServiceClient;
		this.logIndexer = logIndexer;
		this.projectConverter = projectConverter;
	}

	@Override
	public OperationCompletionRS updateProject(String projectName, UpdateProjectRQ updateProjectRQ, ReportPortalUser user) {
		Project project = projectRepository.findByKey(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));
		ProjectAttributesActivityResource before = TO_ACTIVITY_RESOURCE.apply(project);
		updateProjectConfiguration(updateProjectRQ.getConfiguration(), project);
		ofNullable(updateProjectRQ.getUserRoles()).ifPresent(roles -> updateProjectUserRoles(roles, project, user));
		projectRepository.save(project);
		ProjectAttributesActivityResource after = TO_ACTIVITY_RESOURCE.apply(project);

		applicationEventPublisher.publishEvent(new ProjectEvent(project.getId(), UPDATE_EVENT));
		messageBus.publishActivity(new ProjectUpdatedEvent(before, after, user.getUserId(), user.getUsername()));
		messageBus.publishActivity(new ProjectAnalyzerConfigEvent(before, after, user.getUserId(), user.getUsername()));

		return new OperationCompletionRS("Project with name = '" + project.getName() + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS updateProjectNotificationConfig(String projectKey, ReportPortalUser user,
			ProjectNotificationConfigDTO updateProjectNotificationConfigRQ) {
		Project project = projectRepository.findByKey(projectKey)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectKey));
		ProjectResource before = projectConverter.TO_PROJECT_RESOURCE.apply(project);

		updateSenderCases(project, updateProjectNotificationConfigRQ.getSenderCases());

		project.getProjectAttributes()
				.stream()
				.filter(it -> it.getAttribute().getName().equalsIgnoreCase(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute()))
				.findAny()
				.ifPresent(pa -> pa.setValue(String.valueOf(updateProjectNotificationConfigRQ.isEnabled())));

		messageBus.publishActivity(new NotificationsConfigUpdatedEvent(before,
				updateProjectNotificationConfigRQ,
				user.getUserId(),
				user.getUsername()
		));
		return new OperationCompletionRS("Notification configuration of project - '" + projectKey + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS unassignUsers(String projectKey, UnassignUsersRQ unassignUsersRQ, ReportPortalUser user) {
		expect(unassignUsersRQ.getUsernames(), not(List::isEmpty)).verify(BAD_REQUEST_ERROR,
				"Request should contain at least one username."
		);
		Project project = projectRepository.findByKey(projectKey)
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectKey));
		User modifier = userRepository.findById(user.getUserId())
				.orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, user.getUsername()));
		if (!UserRole.ADMINISTRATOR.equals(modifier.getRole())) {
			expect(unassignUsersRQ.getUsernames(), not(contains(equalTo(modifier.getLogin())))).verify(
					UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT,
					"User should not unassign himself from project."
			);
		}

		List<ProjectUser> unassignedUsers = unassignUsers(unassignUsersRQ.getUsernames(), modifier, project, user);
		projectUserRepository.deleteAll(unassignedUsers);
		ProjectUtils.excludeProjectRecipients(unassignedUsers.stream().map(ProjectUser::getUser).collect(Collectors.toSet()), project);
		unassignedUsers.forEach(it -> preferenceRepository.removeByProjectIdAndUserId(project.getId(), it.getUser().getId()));

		return new OperationCompletionRS(
				"User(s) with username(s)='" + unassignUsersRQ.getUsernames() + "' was successfully un-assigned from project='"
						+ project.getName() + "'");
	}

	@Override
	public OperationCompletionRS assignUsers(String projectName, AssignUsersRQ assignUsersRQ, ReportPortalUser user) {

		if (UserRole.ADMINISTRATOR.equals(user.getUserRole())) {
			Project project = projectRepository.findByKey(normalizeId(projectName))
					.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, normalizeId(projectName)));

			List<String> assignedUsernames = project.getUsers().stream().map(u -> u.getUser().getLogin()).collect(toList());
			assignUsersRQ.getUserNames().forEach((name, role) -> {
				ProjectRole projectRole = ProjectRole.forName(role).orElseThrow(() -> new ReportPortalException(ROLE_NOT_FOUND, role));
				assignUser(name, projectRole, assignedUsernames, project);
			});
		} else {
			expect(assignUsersRQ.getUserNames().keySet(), not(Preconditions.contains(equalTo(user.getUsername())))).verify(
					UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT,
					"User should not assign himself to project."
			);

			ReportPortalUser.ProjectDetails projectDetails = projectExtractor.extractProjectDetails(user, projectName);
			Project project = projectRepository.findById(projectDetails.getProjectId())
					.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, normalizeId(projectName)));

			List<String> assignedUsernames = project.getUsers().stream().map(u -> u.getUser().getLogin()).collect(toList());
			assignUsersRQ.getUserNames().forEach((name, role) -> {

				ProjectRole projectRole = ProjectRole.forName(role).orElseThrow(() -> new ReportPortalException(ROLE_NOT_FOUND, role));
				ProjectRole modifierRole = projectDetails.getProjectRole();
				expect(modifierRole.sameOrHigherThan(projectRole), BooleanUtils::isTrue).verify(ACCESS_DENIED);
				assignUser(name, projectRole, assignedUsernames, project);
			});
		}

		return new OperationCompletionRS(
				"User(s) with username='" + assignUsersRQ.getUserNames().keySet() + "' was successfully assigned to project='"
						+ normalizeId(projectName) + "'");
	}

	@Override
	public OperationCompletionRS indexProjectData(String projectKey, ReportPortalUser user) {
		expect(analyzerServiceClient.hasClients(), Predicate.isEqual(true)).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"There are no analyzer deployed."
		);

		Project project = projectRepository.findByKey(projectKey)
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectKey));

		expect(ofNullable(indexerStatusCache.getIndexingStatus().getIfPresent(project.getId())).orElse(false), equalTo(false)).verify(
				ErrorType.FORBIDDEN_OPERATION,
				"Index can not be removed until index generation proceeds."
		);

		Cache<Long, Long> analyzeStatus = analyzerStatusCache.getAnalyzeStatus(AUTO_ANALYZER_KEY)
				.orElseThrow(() -> new ReportPortalException(ErrorType.ANALYZER_NOT_FOUND, AUTO_ANALYZER_KEY));
		expect(analyzeStatus.asMap().containsValue(project.getId()), equalTo(false)).verify(ErrorType.FORBIDDEN_OPERATION,
				"Index can not be removed until auto-analysis proceeds."
		);

		logIndexer.deleteIndex(project.getId());

		logIndexer.index(project.getId(), AnalyzerUtils.getAnalyzerConfig(project))
				.thenAcceptAsync(indexedCount -> mailServiceFactory.getDefaultEmailService(true)
						.sendIndexFinishedEmail("Index generation has been finished", user.getEmail(), indexedCount));

		messageBus.publishActivity(new ProjectIndexEvent(user.getUserId(), user.getUsername(), project.getId(), project.getName(), true));
		return new OperationCompletionRS("Log indexing has been started");
	}

	private List<ProjectUser> unassignUsers(List<String> usernames, User modifier, Project project, ReportPortalUser user) {
		List<ProjectUser> unassignedUsers = Lists.newArrayListWithExpectedSize(usernames.size());
		if (modifier.getRole() == UserRole.ADMINISTRATOR) {
			usernames.forEach(username -> {
				User userForUnassign = userRepository.findByLogin(username)
						.orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, username));
				validateUnassigningUser(modifier, userForUnassign, project);
				unassignedUsers.add(unassignUser(project, username, userForUnassign));

			});
		} else {
			ReportPortalUser.ProjectDetails projectDetails = projectExtractor.extractProjectDetails(user, project.getKey());

			usernames.forEach(username -> {
				User userForUnassign = userRepository.findByLogin(username)
						.orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, username));
				ProjectUser projectUser = userForUnassign.getProjects()
						.stream()
						.filter(it -> Objects.equals(it.getProject().getId(), project.getId()))
						.findFirst()
						.orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND,
								userForUnassign.getLogin(),
								String.format("User not found in project %s", project.getName())
						));

				expect(projectDetails.getProjectRole().sameOrHigherThan(projectUser.getProjectRole()), BooleanUtils::isTrue).verify(
						ACCESS_DENIED);

				validateUnassigningUser(modifier, userForUnassign, project);
				unassignedUsers.add(unassignUser(project, username, userForUnassign));

			});
		}

		return unassignedUsers;
	}

	private ProjectUser unassignUser(Project project, String username, User userForUnassign) {
		ProjectUser projectUser = project.getUsers()
				.stream()
				.filter(it -> it.getUser().getLogin().equalsIgnoreCase(username))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, username));
		project.getUsers().remove(projectUser);
		userForUnassign.getProjects().remove(projectUser);
		return projectUser;
	}

	private void assignUser(String name, ProjectRole projectRole, List<String> assignedUsernames, Project project) {

		User modifyingUser = userRepository.findByLogin(normalizeId(name))
				.orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, name));
		expect(name, not(in(assignedUsernames))).verify(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT,
				formattedSupplier("User '{}' cannot be assigned to project twice.", name)
		);
		if (ProjectType.UPSA.equals(project.getProjectType()) && UserType.UPSA.equals(modifyingUser.getUserType())) {
			fail().withError(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, "Project and user has UPSA type!");
		}
		ProjectUser projectUser = new ProjectUser();
		projectUser.setProjectRole(projectRole);
		projectUser.setUser(modifyingUser);
		projectUser.setProject(project);
		project.getUsers().add(projectUser);
	}

	private void validateUnassigningUser(User modifier, User userForUnassign, Project project) {
		if (ProjectUtils.isPersonalForUser(project.getProjectType(), project.getName(), userForUnassign.getLogin())) {
			fail().withError(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, "Unable to unassign user from his personal project");
		}
		if (ProjectType.UPSA.equals(project.getProjectType()) && UserType.UPSA.equals(userForUnassign.getUserType())) {
			fail().withError(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, "Project and user has UPSA type!");
		}
		if (!ProjectUtils.doesHaveUser(project, userForUnassign.getLogin())) {
			fail().withError(USER_NOT_FOUND, userForUnassign.getLogin(), String.format("User not found in project %s", project.getName()));
		}
	}

	private void updateProjectUserRoles(Map<String, String> userRoles, Project project, ReportPortalUser updater) {

		if (!updater.getUserRole().equals(UserRole.ADMINISTRATOR)) {
			expect(userRoles.get(updater.getUsername()), isNull()).verify(ErrorType.UNABLE_TO_UPDATE_YOURSELF_ROLE, updater.getUsername());
		}

		if (MapUtils.isNotEmpty(userRoles)) {
			userRoles.forEach((username, role) -> {

				ProjectRole newProjectRole = ProjectRole.forName(role).orElseThrow(() -> new ReportPortalException(ROLE_NOT_FOUND, role));

				ProjectUser updatingProjectUser = ofNullable(ProjectUtils.findUserConfigByLogin(project,
						username
				)).orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, username));

				if (UserRole.ADMINISTRATOR != updater.getUserRole()) {
					ProjectRole principalRole = projectExtractor.extractProjectDetails(updater, project.getName()).getProjectRole();
					ProjectRole updatingUserRole = ofNullable(ProjectUtils.findUserConfigByLogin(project,
							username
					)).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username)).getProjectRole();
					/*
					 * Validate principal role level is high enough
					 */
					if (principalRole.sameOrHigherThan(updatingUserRole)) {
						expect(newProjectRole, Preconditions.isLevelEnough(principalRole)).verify(ErrorType.ACCESS_DENIED);
					} else {
						expect(updatingUserRole, Preconditions.isLevelEnough(principalRole)).verify(ErrorType.ACCESS_DENIED);
					}
				}
				updatingProjectUser.setProjectRole(newProjectRole);
			});
		}
	}

	private void updateProjectConfiguration(ProjectConfigurationUpdate configuration, Project project) {
		ofNullable(configuration).flatMap(config -> ofNullable(config.getProjectAttributes())).ifPresent(attributes -> {
			projectAttributeValidator.verifyProjectAttributes(ProjectUtils.getConfigParameters(project.getProjectAttributes()), attributes);
			attributes.forEach((attribute, value) -> project.getProjectAttributes()
					.stream()
					.filter(it -> it.getAttribute().getName().equalsIgnoreCase(attribute))
					.findFirst()
					.ifPresent(attr -> attr.setValue(value)));
		});
	}

	private void updateSenderCases(Project project, List<SenderCaseDTO> cases) {

		project.getSenderCases().clear();
		if (CollectionUtils.isNotEmpty(cases)) {
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

			project.getSenderCases().addAll(withoutDuplicateCases);
		}

	}

}
