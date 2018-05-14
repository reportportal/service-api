/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.core.analyzer.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.project.IUpdateProjectHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserPreferenceRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.*;
import com.epam.ta.reportportal.database.entity.Project.UserConfig;
import com.epam.ta.reportportal.database.entity.project.*;
import com.epam.ta.reportportal.database.entity.project.email.EmailSenderCase;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.database.entity.user.UserType;
import com.epam.ta.reportportal.events.EmailConfigUpdatedEvent;
import com.epam.ta.reportportal.events.ProjectIndexEvent;
import com.epam.ta.reportportal.events.ProjectUpdatedEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.EmailConfigConverters;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.AssignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.UnassignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCaseDTO;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.ta.reportportal.commons.Preconditions.*;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.SendCase.findByName;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy.fromString;
import static com.epam.ta.reportportal.database.entity.project.ProjectUtils.*;
import static com.epam.ta.reportportal.database.entity.user.UserUtils.isEmailValid;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.*;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Update project handler
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class UpdateProjectHandler implements IUpdateProjectHandler {

	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final UserPreferenceRepository preferenceRepository;
	private final ApplicationEventPublisher publisher;

	@Autowired
	private ILogIndexer logIndexer;

	@Autowired
	@Qualifier("autoAnalyzeTaskExecutor")
	private TaskExecutor taskExecutor;

	@Autowired
	private AnalyzerStatusCache analyzerStatusCache;

	@Autowired
	public UpdateProjectHandler(ProjectRepository projectRepository, UserRepository userRepository,
			UserPreferenceRepository userPreferenceRepository, ApplicationEventPublisher applicationEventPublisher) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.preferenceRepository = userPreferenceRepository;
		this.publisher = applicationEventPublisher;
	}

	@Override
	public OperationCompletionRS updateProject(String projectName, UpdateProjectRQ updateProjectRQ, String principalName) {
		Project project = projectRepository.findOne(projectName);
		Project before = SerializationUtils.clone(project);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		if (null != updateProjectRQ.getCustomer()) {
			project.setCustomer(updateProjectRQ.getCustomer().trim());
		}
		project.setAddInfo(updateProjectRQ.getAddInfo());

		User principal = userRepository.findOne(principalName);

		if (null != updateProjectRQ.getUserRoles() && !principal.getRole().equals(UserRole.ADMINISTRATOR)) {
			expect(updateProjectRQ.getUserRoles().get(principalName), isNull()).verify(UNABLE_TO_UPDATE_YOURSELF_ROLE, principalName);
		}

		if (null != updateProjectRQ.getUserRoles()) {
			for (Entry<String, String> user : updateProjectRQ.getUserRoles().entrySet()) {

				Optional<ProjectRole> role = ProjectRole.forName(user.getValue());
				/*
				 * Validate role exists
				 */
				expect(role, IS_PRESENT).verify(ROLE_NOT_FOUND, user.getValue());
				ProjectRole projectRole = role.get();
				if (UserRole.ADMINISTRATOR != principal.getRole()) {
					ProjectRole principalRoleLevel = findUserConfigByLogin(project, principalName).getProjectRole();
					ProjectRole userRoleLevel = findUserConfigByLogin(project, user.getKey()).getProjectRole();
					/*
					 * Validate principal role level is high enough
					 */
					if (principalRoleLevel.sameOrHigherThan(userRoleLevel)) {
						expect(projectRole, isLevelEnough(principalRoleLevel)).verify(ACCESS_DENIED);
					} else {
						expect(userRoleLevel, isLevelEnough(principalRoleLevel)).verify(ACCESS_DENIED);
					}
				}
				findUserConfigByLogin(project, user.getKey()).setProjectRole(role.get());
			}
		}

		if (null != updateProjectRQ.getConfiguration()) {
			processConfiguration(updateProjectRQ.getConfiguration(), project.getConfiguration(), projectName, principalName);
		}

		try {
			projectRepository.save(project);
		} catch (Exception e) {
			throw new ReportPortalException("Error during updating Project", e);
		}

		publisher.publishEvent(new ProjectUpdatedEvent(before, project, principalName, updateProjectRQ));
		return new OperationCompletionRS("Project with name = '" + projectName + "' is successfully updated.");
	}

	private void processConfiguration(ProjectConfiguration modelConfig, Project.Configuration dbConfig, String projectName,
			String principalName) {

		ofNullable(modelConfig.getKeepLogs()).ifPresent(keepLogs -> {
			expect(KeepLogsDelay.findByName(keepLogs), notNull()).verify(BAD_REQUEST_ERROR);
			dbConfig.setKeepLogs(keepLogs);
		});

		ofNullable(modelConfig.getInterruptJobTime()).ifPresent(jobTime -> {
			expect(InterruptionJobDelay.findByName(jobTime), notNull()).verify(BAD_REQUEST_ERROR);
			dbConfig.setInterruptJobTime(jobTime);
		});

		ofNullable(modelConfig.getKeepScreenshots()).ifPresent(keepScreens -> {
			expect(KeepScreenshotsDelay.findByName(keepScreens), notNull()).verify(BAD_REQUEST_ERROR);
			dbConfig.setKeepScreenshots(keepScreens);
		});

		ofNullable(modelConfig.getProjectSpecific()).ifPresent(specific -> {
			expect(ProjectSpecific.findByName(specific).isPresent(), equalTo(true)).verify(BAD_REQUEST_ERROR);
			dbConfig.setProjectSpecific(ProjectSpecific.findByName(specific).get());
		});

		ofNullable(modelConfig.getAnalyzerConfig()).ifPresent(analyzerConfig -> {
			expect(analyzerStatusCache.getAnalyzerStatus().asMap().containsValue(projectName), equalTo(false)).verify(
					ErrorType.FORBIDDEN_OPERATION, "Project settings can not be updated until auto-analysis proceeds");
			ProjectAnalyzerConfig dbAnalyzerConfig = ofNullable(dbConfig.getAnalyzerConfig()).orElse(new ProjectAnalyzerConfig());
			ofNullable(analyzerConfig.getAnalyzerMode()).ifPresent(mode -> dbAnalyzerConfig.setAnalyzerMode(AnalyzeMode.fromString(mode)));
			ofNullable(analyzerConfig.getIsAutoAnalyzerEnabled()).ifPresent(dbAnalyzerConfig::setIsAutoAnalyzerEnabled);
			ofNullable(analyzerConfig.getMinDocFreq()).ifPresent(dbAnalyzerConfig::setMinDocFreq);
			ofNullable(analyzerConfig.getMinTermFreq()).ifPresent(dbAnalyzerConfig::setMinTermFreq);
			ofNullable(analyzerConfig.getMinShouldMatch()).ifPresent(dbAnalyzerConfig::setMinShouldMatch);
			ofNullable(analyzerConfig.getNumberOfLogLines()).ifPresent(dbAnalyzerConfig::setNumberOfLogLines);
			dbConfig.setAnalyzerConfig(dbAnalyzerConfig);
		});

		ofNullable(modelConfig.getStatisticCalculationStrategy()).ifPresent(strategy -> dbConfig.setStatisticsCalculationStrategy(
				fromString(strategy).orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
						"Incorrect statistics calculation type: " + strategy
				))));

		ofNullable(modelConfig.getEmailConfig()).ifPresent(
				emailConfig -> updateProjectEmailConfig(projectName, principalName, modelConfig.getEmailConfig()));
	}

	@Override
	public OperationCompletionRS updateProjectEmailConfig(String projectName, String user, ProjectEmailConfigDTO configUpdate) {
		Project project = projectRepository.findOne(projectName);
		Project beforeUpdate = SerializationUtils.clone(project);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		boolean emailEnabled = BooleanUtils.isTrue(configUpdate.getEmailEnabled());
		project.getConfiguration().getEmailConfig().setEmailEnabled(emailEnabled);

		List<EmailSenderCaseDTO> cases = configUpdate.getEmailCases();

		ofNullable(configUpdate.getFrom()).ifPresent(from -> {
			expect(isEmailValid(configUpdate.getFrom()), equalTo(true)).verify(BAD_REQUEST_ERROR,
					formattedSupplier("Provided FROM value '{}' is invalid", configUpdate.getFrom())
			);
			project.getConfiguration().getEmailConfig().setFrom(configUpdate.getFrom());
		});

		expect(cases, Preconditions.NOT_EMPTY_COLLECTION).verify(BAD_REQUEST_ERROR, "At least one rule should be present.");
		cases.forEach(sendCase -> {
			expect(findByName(sendCase.getSendCase()).isPresent(), equalTo(true)).verify(BAD_REQUEST_ERROR, sendCase.getSendCase());
			expect(sendCase.getRecipients(), notNull()).verify(BAD_REQUEST_ERROR, "Recipients list should not be null");
			expect(sendCase.getRecipients().isEmpty(), equalTo(false)).verify(BAD_REQUEST_ERROR,
					formattedSupplier("Empty recipients list for email case '{}' ", sendCase)
			);
			sendCase.setRecipients(sendCase.getRecipients().stream().map(it -> {
				validateRecipient(project, it);
				return it.trim();
			}).distinct().collect(toList()));

			if (null != sendCase.getLaunchNames()) {
				sendCase.setLaunchNames(sendCase.getLaunchNames().stream().map(name -> {
					validateLaunchName(name);
					return name.trim();
				}).distinct().collect(toList()));
			}

			if (null != sendCase.getTags()) {
				sendCase.setTags(sendCase.getTags().stream().map(tag -> {
					expect(isNullOrEmpty(tag), equalTo(false)).verify(BAD_REQUEST_ERROR,
							"Tags values cannot be empty. Please specify it or not include in request."
					);
					return tag.trim();
				}).distinct().collect(toList()));
			}
		});

				/* If project email settings */
		List<EmailSenderCase> withoutDuplicateCases = cases.stream().distinct().map(EmailConfigConverters.TO_CASE_MODEL).collect(toList());
		if (cases.size() != withoutDuplicateCases.size()) {
			fail().withError(BAD_REQUEST_ERROR, "Project email settings contain duplicate cases");
		}

		project.getConfiguration().getEmailConfig().setEmailCases(withoutDuplicateCases);

		try {
			projectRepository.save(project);
		} catch (Exception e) {
			throw new ReportPortalException("Error during updating Project", e);
		}

		publisher.publishEvent(new EmailConfigUpdatedEvent(beforeUpdate, configUpdate, user));
		return new OperationCompletionRS("EMail configuration of project with name = '" + projectName + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS unassignUsers(String projectName, String modifier, UnassignUsersRQ unassignUsersRQ) {

		Project project = projectRepository.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);
		EntryType projectType = project.getConfiguration().getEntryType();

		User principal = userRepository.findOne(modifier);
		if (UserRole.ADMINISTRATOR != principal.getRole()) {
			/* user shouldn't have possibility un-assign himself */
			expect(unassignUsersRQ.getUsernames(), not(contains(equalTo(modifier)))).verify(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT,
					"User should not unassign himself from project."
			);
		}

		List<UserConfig> users = project.getUsers();
		List<String> candidatesForUnassign = new ArrayList<>();
		for (String login : unassignUsersRQ.getUsernames()) {
			/* Verify user existence in database */
			User singleUser = userRepository.findOne(login);
			expect(singleUser, notNull()).verify(USER_NOT_FOUND, login, "User is not found in database.");
			UserType userType = singleUser.getType();
			if (EntryType.PERSONAL.equals(projectType) && projectName.startsWith(singleUser.getId())) {
				fail().withError(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, "Unable to unassign user from his personal project");
			}
			if (projectType.equals(EntryType.UPSA) && userType.equals(UserType.UPSA)) {
				fail().withError(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, "Project and user has UPSA type!");
			}
			if (!ProjectUtils.doesHaveUser(project, singleUser.getId())) {
				fail().withError(USER_NOT_FOUND, singleUser.getId(), String.format("User not found in project %s", projectName));
			}

			if (UserRole.ADMINISTRATOR != principal.getRole()) {
				/* Modifier cannot un-assign users with higher roles */
				expect(findUserConfigByLogin(project, singleUser.getId()).getProjectRole(),
						isLevelEnough(findUserConfigByLogin(project, modifier).getProjectRole())
				).verify(ACCESS_DENIED);
			}
			candidatesForUnassign.add(singleUser.getId());
			/*
			 * placed removing before validation to reduce number of cycles
			 */
			users.removeIf(it -> singleUser.getId().equals(it.getLogin()));

		}

		/* Update un-assigning user's default projects */
		Iterable<User> dbUsers = userRepository.findAll(candidatesForUnassign);
		processCandidateForUnaassign(dbUsers, projectName);
		project = excludeProjectRecipients(dbUsers, project);
		try {
			project.setUsers(users);
			projectRepository.save(project);
			for (String user : unassignUsersRQ.getUsernames()) {
				String normalized = user.toLowerCase();
				preferenceRepository.deleteByUsernameAndProject(normalized, projectName);
			}
		} catch (Exception e) {
			throw new ReportPortalException("Error during updating Project ", e);
		}

		OperationCompletionRS response = new OperationCompletionRS();
		String msg = "User(s) with username(s)='" + unassignUsersRQ.getUsernames() + "' was successfully un-assigned from project='"
				+ projectName + "'";
		response.setResultMessage(msg);
		return response;
	}

	@Override
	public OperationCompletionRS assignUsers(String projectName, String modifier, AssignUsersRQ assignUsersRQ) {
		Project project = projectRepository.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);
		EntryType projectType = project.getConfiguration().getEntryType();
		User principal = userRepository.findOne(modifier);
		if (!principal.getRole().equals(UserRole.ADMINISTRATOR)) {
			expect(assignUsersRQ.getUserNames().keySet(), not(contains(equalTo(modifier)))).verify(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT,
					"User should not assign himself to project."
			);
		}

		for (String username : assignUsersRQ.getUserNames().keySet()) {
			expect(username.toLowerCase(),
					not(in(project.getUsers().stream().map(UserConfig::getLogin).collect(Collectors.toList())))
			).verify(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, formattedSupplier("User '{}' cannot be assigned to project twice.", username));
		}

		UserConfig principalRoles = findUserConfigByLogin(project, modifier);
		List<UserConfig> users = project.getUsers();
		for (String username : assignUsersRQ.getUserNames().keySet()) {
			User user = userRepository.findOne(username.toLowerCase());
			expect(user, notNull()).verify(USER_NOT_FOUND, username);
			UserType userType = user.getType();

			if (projectType.equals(EntryType.UPSA) && userType.equals(UserType.UPSA)) {
				fail().withError(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, "Project and user has UPSA type!");
			}

			UserConfig config = new UserConfig();
			String userToAssign = assignUsersRQ.getUserNames().get(username);
			if (!isNullOrEmpty(userToAssign)) {
				config.setLogin(username.toLowerCase());
				Optional<ProjectRole> proposedRoleOptional = ProjectRole.forName(userToAssign);
				expect(proposedRoleOptional, IS_PRESENT).verify(ROLE_NOT_FOUND, userToAssign);
				ProjectRole proposedRole = proposedRoleOptional.get();

				if (principal.getRole() != UserRole.ADMINISTRATOR) {
					ProjectRole creatorProjectRoleLevel = principalRoles.getProjectRole();
					ProjectRole newUserProjectRoleLevel = proposedRole;
					expect(creatorProjectRoleLevel.sameOrHigherThan(newUserProjectRoleLevel), equalTo(Boolean.TRUE)).verify(ACCESS_DENIED);
					config.setProjectRole(proposedRole);
					config.setProposedRole(proposedRole);
				} else {
					config.setProjectRole(proposedRole);
					config.setProposedRole(proposedRole);
				}
			} else {
				config.setProjectRole(ProjectRole.MEMBER);
				config.setProposedRole(ProjectRole.MEMBER);
			}
			users.add(config);
		}

		try {
			project.setUsers(users);
			projectRepository.save(project);
		} catch (Exception e) {
			throw new ReportPortalException("Error during updating Project", e);
		}

		OperationCompletionRS response = new OperationCompletionRS();
		String msg =
				"User(s) with username='" + assignUsersRQ.getUserNames().keySet() + "' was successfully assigned to project='" + projectName
						+ "'";
		response.setResultMessage(msg);
		return response;
	}

	@Override
	public OperationCompletionRS indexProjectData(String projectName, String username) {
		Project project = projectRepository.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		expect(project.getConfiguration().getAnalyzerConfig().isIndexingRunning(), equalTo(false)).verify(
				ErrorType.FORBIDDEN_OPERATION, "Index can not be removed until index generation proceeds.");

		expect(analyzerStatusCache.getAnalyzerStatus().asMap().containsValue(projectName), equalTo(false)).verify(
				ErrorType.FORBIDDEN_OPERATION, "Index can not be removed until auto-analysis proceeds.");

		User user = userRepository.findOne(username);
		expect(user, notNull()).verify(ErrorType.USER_NOT_FOUND, username);

		projectRepository.enableProjectIndexing(projectName, true);
		logIndexer.deleteIndex(projectName);
		taskExecutor.execute(() -> logIndexer.indexProjectData(project, user));
		publisher.publishEvent(new ProjectIndexEvent(projectName, username, true));
		return new OperationCompletionRS("Log indexing has been started");
	}

	void validateRecipient(Project project, String recipient) {
		expect(recipient, notNull()).verify(BAD_REQUEST_ERROR, formattedSupplier("Provided recipient email '{}' is invalid", recipient));
		if (recipient.contains("@")) {
			expect(isEmailValid(recipient), equalTo(true)).verify(BAD_REQUEST_ERROR,
					formattedSupplier("Provided recipient email '{}' is invalid", recipient)
			);
		} else {
			final String login = recipient.trim();
			expect(MIN_LOGIN_LENGTH <= login.length() && login.length() <= MAX_LOGIN_LENGTH, equalTo(true)).verify(BAD_REQUEST_ERROR,
					"Acceptable login length  [" + MIN_LOGIN_LENGTH + ".." + MAX_LOGIN_LENGTH + "]"
			);
			if (!getOwner().equals(login)) {
				expect(ProjectUtils.doesHaveUser(project, login.toLowerCase()), equalTo(true)).verify(USER_NOT_FOUND, login,
						String.format("User not found in project %s", project.getId())
				);
			}
		}
	}

	void validateLaunchName(String name) {
		expect(isNullOrEmpty(name), equalTo(false)).verify(BAD_REQUEST_ERROR,
				"Launch name values cannot be empty. Please specify it or not include in request."
		);
		expect(name.length() <= MAX_NAME_LENGTH, equalTo(true)).verify(BAD_REQUEST_ERROR,
				formattedSupplier("One of provided launch names '{}' is too long. Acceptable name length is [1..256]", name)
		);
	}

	/**
	 * Validate candidates for unassign from projects, and update default
	 * project if it required
	 *
	 * @param users
	 * @param projectName
	 */
	private void processCandidateForUnaassign(Iterable<User> users, String projectName) {
		List<User> updated = StreamSupport.stream(users.spliterator(), false)
				.filter(it -> it.getDefaultProject().equals(projectName))
				.peek(it -> projectRepository.findPersonalProjectName(it.getId()).ifPresent(it::setDefaultProject))
				.collect(toList());
		userRepository.save(updated);
	}
}
