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

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.SendCase.findByName;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.database.entity.user.UserUtils.isEmailValid;
import static com.epam.ta.reportportal.database.personal.PersonalProjectUtils.personalProjectName;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.*;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.Constants;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.project.IUpdateProjectHandler;
import com.epam.ta.reportportal.database.dao.FavoriteResourceRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserPreferenceRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Project.UserConfig;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.ProjectSpecific;
import com.epam.ta.reportportal.database.entity.project.*;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.database.entity.user.UserType;
import com.epam.ta.reportportal.events.EmailConfigUpdatedEvent;
import com.epam.ta.reportportal.events.ProjectUpdatedEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.AssignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.UnassignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCase;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfig;
import com.epam.ta.reportportal.ws.model.project.email.UpdateProjectEmailRQ;

/**
 * Update project handler
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class UpdateProjectHandler implements IUpdateProjectHandler {

	private ProjectRepository projectRepository;

	private UserRepository userRepository;

	@Autowired
	private FavoriteResourceRepository favoriteResourceRepository;

	@Autowired
	private UserPreferenceRepository preferenceRepository;

	@Autowired
	private ApplicationEventPublisher publisher;

	@Autowired
	public void setProjectRepository(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Autowired
	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public OperationCompletionRS updateProject(String projectName, UpdateProjectRQ updateProjectRQ, String principalName) {
		Project project = projectRepository.findOne(projectName);
		Project before = SerializationUtils.clone(project);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);
		if (null != updateProjectRQ.getUserRoles()) {
			expect(updateProjectRQ.getUserRoles().get(principalName), isNull()).verify(UNABLE_TO_UPDATE_YOURSELF_ROLE, principalName);
		}

		if (null != updateProjectRQ.getCustomer())
			project.setCustomer(updateProjectRQ.getCustomer().trim());
		project.setAddInfo(updateProjectRQ.getAddInfo());

		User principal = userRepository.findOne(principalName);

		if (null != updateProjectRQ.getUserRoles()) {
			for (Entry<String, String> user : updateProjectRQ.getUserRoles().entrySet()) {
				/*
				 * Validate user exists
				 */
				expect(project.getUsers(), Preconditions.containsKey(user.getKey())).verify(USER_NOT_FOUND, user.getKey(),
						formattedSupplier("User '{}' not found in '{}' project", user.getKey(), projectName));
				Optional<ProjectRole> role = ProjectRole.forName(user.getValue());
				/*
				 * Validate role exists
				 */
				expect(role, Preconditions.IS_PRESENT).verify(ROLE_NOT_FOUND, user.getValue());

				if (UserRole.ADMINISTRATOR != principal.getRole()) {
					int principalRoleLevel = project.getUsers().get(principalName).getProjectRole().getRoleLevel();
					int userRoleLevel = project.getUsers().get(user.getKey()).getProjectRole().getRoleLevel();
					/*
					 * Validate principal role level is high enough
					 */
					if (principalRoleLevel >= userRoleLevel) {
						expect(role.get().getRoleLevel(), Preconditions.isLevelEnough(principalRoleLevel)).verify(ACCESS_DENIED);
					} else {
						expect(userRoleLevel, Preconditions.isLevelEnough(principalRoleLevel)).verify(ACCESS_DENIED);
					}
				}
				project.getUsers().get(user.getKey()).setProjectRole(role.get());
			}
		}

		// TODO Custom fields exceptions handling with readable messages
		if (null != updateProjectRQ.getConfiguration()) {
			ProjectConfiguration modelConfig = updateProjectRQ.getConfiguration();
			if (null != modelConfig.getKeepLogs()) {
				expect(KeepLogsDelay.findByName(modelConfig.getKeepLogs()), notNull()).verify(BAD_REQUEST_ERROR);
				project.getConfiguration().setKeepLogs(modelConfig.getKeepLogs());
			}

			if (null != modelConfig.getInterruptJobTime()) {
				expect(InterruptionJobDelay.findByName(modelConfig.getInterruptJobTime()), notNull()).verify(BAD_REQUEST_ERROR);
				project.getConfiguration().setInterruptJobTime(modelConfig.getInterruptJobTime());
			}

			if (null != modelConfig.getKeepScreenshots()) {
				expect(KeepScreenshotsDelay.findByName(modelConfig.getKeepScreenshots()), notNull()).verify(BAD_REQUEST_ERROR);
				project.getConfiguration().setKeepScreenshots(modelConfig.getKeepScreenshots());
			}

			if (null != modelConfig.getProjectSpecific()) {
				expect(ProjectSpecific.findByName(modelConfig.getProjectSpecific()).isPresent(), equalTo(true)).verify(BAD_REQUEST_ERROR);
				project.getConfiguration().setProjectSpecific(ProjectSpecific.findByName(modelConfig.getProjectSpecific()).get());
			}

			if (null != modelConfig.getIsAAEnabled()) {
				project.getConfiguration().setIsAutoAnalyzerEnabled(modelConfig.getIsAAEnabled());
			}
		}

		try {
			projectRepository.save(project);
		} catch (Exception e) {
			throw new ReportPortalException("Error during updating Project", e);
		}

		publisher.publishEvent(new ProjectUpdatedEvent(before, project, principalName, updateProjectRQ));
		return new OperationCompletionRS("Project with name = '" + projectName + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS updateProjectEmailConfig(String projectName, String user, UpdateProjectEmailRQ updateProjectEmailRQ) {
		Project project = projectRepository.findOne(projectName);
		Project beforeUpdate = SerializationUtils.clone(project);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		if (null != updateProjectEmailRQ.getConfiguration()) {
			ProjectEmailConfig config = updateProjectEmailRQ.getConfiguration();
			if (null != config.getFrom()) {
				expect(isEmailValid(config.getFrom()), equalTo(true)).verify(BAD_REQUEST_ERROR,
						formattedSupplier("Provided FROM value '{}' is invalid", config.getFrom()));
				project.getConfiguration().getEmailConfig().setFrom(config.getFrom());
			}

			List<EmailSenderCase> cases = config.getEmailCases();
			if (null != cases) {
				expect(cases.size() > 0, equalTo(true)).verify(BAD_REQUEST_ERROR, "At least one rule should be present.");
				cases.forEach(sendCase -> {
					expect(findByName(sendCase.getSendCase()).isPresent(), equalTo(true)).verify(BAD_REQUEST_ERROR, sendCase.getSendCase());
					expect(sendCase.getRecipients(), notNull()).verify(BAD_REQUEST_ERROR, "Recipients list should not be null");
					expect(sendCase.getRecipients().size() > 0, equalTo(true)).verify(BAD_REQUEST_ERROR,
							formattedSupplier("Empty recipients list for email case '{}' ", sendCase));
					sendCase.setRecipients(sendCase.getRecipients().stream().map(it -> {
						expect(it, notNull()).verify(BAD_REQUEST_ERROR, formattedSupplier("Provided recipient email '{}' is invalid", it));
						if (it.contains("@")) {
							expect(isEmailValid(it), equalTo(true)).verify(BAD_REQUEST_ERROR,
									formattedSupplier("Provided recipient email '{}' is invalid", it));
						} else {
							final String login = it.trim();
							expect(MIN_LOGIN_LENGTH <= login.length() && login.length() <= MAX_LOGIN_LENGTH, equalTo(true))
									.verify(BAD_REQUEST_ERROR, "Acceptable login length  [4..128]");
							return login;
						}
						return it;
					}).distinct().collect(toList()));

					if ((null != sendCase.getLaunchNames())) {
						sendCase.setLaunchNames(sendCase.getLaunchNames().stream().map(name -> {
							expect(isNullOrEmpty(name), equalTo(false)).verify(BAD_REQUEST_ERROR,
									"Launch name values cannot be empty. Please specify it or not include in request.");
							expect(name.length() <= MAX_NAME_LENGTH, equalTo(true)).verify(BAD_REQUEST_ERROR, formattedSupplier(
									"One of provided launch names '{}' is too long. Acceptable name length is [1..256]", name));
							return name.trim();
						}).distinct().collect(toList()));
					}

					if ((null != sendCase.getTags())) {
						sendCase.setTags(sendCase.getTags().stream().map(tag -> {
							expect(isNullOrEmpty(tag), equalTo(false)).verify(BAD_REQUEST_ERROR,
									"Tags values cannot be empty. Please specify it or not include in request.");
							return tag.trim();
						}).distinct().collect(toList()));
					}
				});

				/* If project email settings */
				List<EmailSenderCase> withoutDuplicateCases = cases.stream().distinct().collect(toList());
				if (cases.size() != withoutDuplicateCases.size())
					fail().withError(BAD_REQUEST_ERROR, "Project email settings contain duplicate cases");

				project.getConfiguration().getEmailConfig().setEmailCases(cases);
			}

			/* If enable parameter is FALSE, previous settings be dropped */
			// TODO NPE?
			if (!config.getEmailEnabled())
				ProjectUtils.setDefaultEmailCofiguration(project);
			else
				project.getConfiguration().getEmailConfig().setEmailEnabled(true);
		} else {
			/* Something wrong with input RQ but we don't care about */
		}

		try {
			projectRepository.save(project);
		} catch (Exception e) {
			throw new ReportPortalException("Error during updating Project", e);
		}

		publisher.publishEvent(new EmailConfigUpdatedEvent(beforeUpdate, updateProjectEmailRQ, user));
		return new OperationCompletionRS("EMail configuration of project with name = '" + projectName + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS unassignUsers(String projectName, String modifier, UnassignUsersRQ unassignUsersRQ) {
		BusinessRule.expect(projectName, Predicates.not(Predicates.equalTo(Constants.DEFAULT_PROJECT.toString())))
				.verify(ErrorType.UNABLE_TO_UPDATE_DEFAULT_PROJECT, "Users cannot be unassigned from default project");

		Project project = projectRepository.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);
		EntryType projectType = project.getConfiguration().getEntryType();

		User principal = userRepository.findOne(modifier);
		if (UserRole.ADMINISTRATOR != principal.getRole()) {
			/* user shouldn't have possibility un-assign himself */
			expect(unassignUsersRQ.getUsernames(), not(Preconditions.contains(equalTo(modifier))))
					.verify(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, "User should not unassign himself from project.");
		}

		Map<String, UserConfig> users = project.getUsers();
		List<String> candidatesForUnassign = new ArrayList<>();
		for (String login : unassignUsersRQ.getUsernames()) {
			/* Verify user existence in database */
			User singleUser = userRepository.findOne(login);
			expect(singleUser, notNull()).verify(USER_NOT_FOUND, login, "User is not found in database.");
			UserType userType = singleUser.getType();
			if (EntryType.PERSONAL.equals(projectType) && projectName.equalsIgnoreCase(personalProjectName(singleUser.getId()))) {
				fail().withError(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, "Unable to unassign user from his personal project");
			}
			if (projectType.equals(EntryType.UPSA) && userType.equals(UserType.UPSA)) {
				fail().withError(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, "Project and user has UPSA type!");
			}
			if (!users.containsKey(singleUser.getId())) {
				fail().withError(USER_NOT_FOUND, singleUser.getId(), String.format("User not found in project %s", projectName));
			}

			if (UserRole.ADMINISTRATOR != principal.getRole()) {
				/* Modifier cannot un-assign users with higher roles */
				expect(users.get(singleUser.getId()).getProjectRole().getRoleLevel(),
						Preconditions.isLevelEnough(users.get(modifier).getProjectRole().getRoleLevel())).verify(ACCESS_DENIED);
			}
			candidatesForUnassign.add(singleUser.getId());
			/*
			 * placed removing before validation to reduce number of cycles
			 */
			users.remove(singleUser.getId());

		}

		/* Update un-assigning user's default projects */
		Iterable<User> dbUsers = userRepository.findAll(candidatesForUnassign);
		processCandidateForUnaassign(dbUsers, projectName);
		project = ProjectUtils.excludeProjectRecipients(dbUsers, project);
		try {
			project.setUsers(users);
			projectRepository.save(project);
			// Clear 'userRef' field in users's launches of specified project
			/*
			 * Commented due new logic of DEBUG section
			 */
			// for(String user : unassignUsersRQ.getUserNames()) {
			// this.processLaunchesOfUnassignedUser(projectName, user);
			// }
			for (String user : unassignUsersRQ.getUsernames()) {
				String normalized = user.toLowerCase();
				favoriteResourceRepository.removeFavoriteResources(normalized, projectName);
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
		if (!principal.getRole().equals(UserRole.ADMINISTRATOR))
			expect(assignUsersRQ.getUserNames().keySet(), not(Preconditions.contains(equalTo(modifier))))
					.verify(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, "User should not assign himself to project.");

		for (String username : assignUsersRQ.getUserNames().keySet()) {
			expect(username.toLowerCase(), not(in(project.getUsers().keySet()))).verify(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT,
					formattedSupplier("User '{}' cannot be assigned to project twice.", username));
		}

		UserConfig principalRoles = project.getUsers().get(modifier);
		Map<String, UserConfig> users = project.getUsers();
		for (String username : assignUsersRQ.getUserNames().keySet()) {
			User user = userRepository.findOne(username.toLowerCase());
			expect(user, notNull()).verify(USER_NOT_FOUND, username);
			UserType userType = user.getType();

			if (projectType.equals(EntryType.UPSA) && userType.equals(UserType.UPSA))
				fail().withError(UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, "Project and user has UPSA type!");

			UserConfig config = new UserConfig();
			String userToAssign = assignUsersRQ.getUserNames().get(username);
			if (!isNullOrEmpty(userToAssign)) {
				Optional<ProjectRole> proposedRole = ProjectRole.forName(userToAssign);
				expect(proposedRole, Preconditions.IS_PRESENT).verify(ROLE_NOT_FOUND, userToAssign);

				if (principal.getRole() != UserRole.ADMINISTRATOR) {
					int creatorProjectRoleLevel = principalRoles.getProjectRole().getRoleLevel();
					int newUserProjectRoleLevel = proposedRole.get().getRoleLevel();
					expect(creatorProjectRoleLevel >= newUserProjectRoleLevel, equalTo(Boolean.TRUE)).verify(ACCESS_DENIED);
					config.setProjectRole(proposedRole.get());
					config.setProposedRole(proposedRole.get());
				} else {
					config.setProjectRole(proposedRole.get());
					config.setProposedRole(proposedRole.get());
				}
			} else {
				config.setProjectRole(ProjectRole.MEMBER);
				config.setProposedRole(ProjectRole.MEMBER);
			}
			users.put(username.toLowerCase(), config);
		}

		try {
			project.setUsers(users);
			projectRepository.save(project);
		} catch (Exception e) {
			throw new ReportPortalException("Error during updating Project", e);
		}

		OperationCompletionRS response = new OperationCompletionRS();
		String msg = "User(s) with username='" + assignUsersRQ.getUserNames().keySet() + "' was successfully assigned to project='"
				+ projectName + "'";
		response.setResultMessage(msg);
		return response;
	}

	/**
	 * Validate candidates for unassign from projects, and update default
	 * project if it required
	 *
	 * @param users
	 * @param projectName
	 */
	private void processCandidateForUnaassign(Iterable<User> users, String projectName) {
		List<User> updated = StreamSupport.stream(users.spliterator(), false).filter(it -> it.getDefaultProject().equals(projectName))
				.map(it -> {
					it.setDefaultProject(Constants.DEFAULT_PROJECT.toString());
					return it;
				}).collect(toList());
		userRepository.save(updated);
	}
}
