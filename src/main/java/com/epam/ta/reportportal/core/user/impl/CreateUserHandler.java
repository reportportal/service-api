/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

package com.epam.ta.reportportal.core.user.impl;

import com.epam.ta.reportportal.commons.Constants;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.user.ICreateUserHandler;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Project.UserConfig;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.user.*;
import com.epam.ta.reportportal.database.entity.user.UserUtils;
import com.epam.ta.reportportal.database.personal.PersonalProjectUtils;
import com.epam.ta.reportportal.events.UserCreatedEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.ws.converter.builders.RestorePasswordBidBuilder;
import com.epam.ta.reportportal.ws.converter.builders.UserBuilder;
import com.epam.ta.reportportal.ws.converter.builders.UserCreationBidBuilder;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.user.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.database.entity.ProjectRole.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Implementation of Create User handler
 * 
 * @author Andrei_Ramanchuk
 */
@Service
public class CreateUserHandler implements ICreateUserHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateUserHandler.class);

	private UserRepository userRepository;
	private ProjectRepository projectRepository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private ServerSettingsRepository settingsRepository;

	@Autowired
	private UserCreationBidRepository userCreationBidRepository;

	@Autowired
	private RestorePasswordBidRepository restorePasswordBidRepository;

	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private Provider<UserBuilder> userBuilder;

	@Autowired
	private Provider<UserCreationBidBuilder> userCreationBidBuilder;

	@Autowired
	private Provider<RestorePasswordBidBuilder> restorePasswordBidBuilder;

	@Autowired
	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Autowired
	public void setProjectRepository(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}


	@Autowired
	public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	@Override
	public CreateUserRS createUserByAdmin(CreateUserRQFull request, String userName, String basicUrl) {
		String newUsername = EntityUtils.normalizeUsername(request.getLogin());

		expect(userRepository.exists(newUsername), equalTo(false))
				.verify(USER_ALREADY_EXISTS, Suppliers.formattedSupplier("login='{}'", newUsername));

		String projectName = EntityUtils.normalizeProjectName(request.getDefaultProject());
		Project defaultProject = projectRepository.findOne(projectName);
		expect(defaultProject, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		String email = EntityUtils.normalizeEmail(request.getEmail());
		expect(UserUtils.isEmailValid(email), equalTo(true)).verify(BAD_REQUEST_ERROR, email);

		CreateUserRQConfirm req = new CreateUserRQConfirm();
		req.setDefaultProject(projectName);
		req.setEmail(email);
		req.setFullName(request.getFullName());
		req.setLogin(request.getLogin());
		req.setPassword(request.getPassword());

		final Optional<UserRole> userRole = UserRole.findByName(request.getAccountRole());
		expect(userRole.isPresent(), equalTo(true)).verify(BAD_REQUEST_ERROR, "Incorrect specified Account Role parameter.");
		User user = userBuilder.get().addCreateUserRQ(req).addUserRole(userRole.get()).build();
		Optional<ProjectRole> projectRole = forName(request.getProjectRole());
		expect(projectRole, Preconditions.IS_PRESENT).verify(ROLE_NOT_FOUND, request.getProjectRole());

		Map<String, UserConfig> projectUsers = defaultProject.getUsers();
		projectUsers.put(user.getId(), UserConfig.newOne().withProjectRole(projectRole.get()).withProposedRole(projectRole.get()));
		defaultProject.setUsers(projectUsers);

		Map<String, UserConfig> demoUsers = new HashMap<>();
		demoUsers.put(user.getId(), UserConfig.newOne().withProjectRole(MEMBER).withProposedRole(MEMBER));

		try {
			userRepository.save(user);
			projectRepository.addUsers(projectName, projectUsers);
			if (!Constants.DEFAULT_PROJECT.toString().equalsIgnoreCase(projectName))
				projectRepository.addUsers(Constants.DEFAULT_PROJECT.toString(), demoUsers);

			/*
			 * Generate personal project for the user
			 */
			Project personalProject = PersonalProjectUtils.generatePersonalProject(user);
			if (!defaultProject.getId().equals(personalProject.getId())){
				projectRepository.save(personalProject);
			}

			emailService.sendConfirmationEmail(request, basicUrl);
		} catch (DuplicateKeyException e) {
			fail().withError(USER_ALREADY_EXISTS, Suppliers.formattedSupplier("email='{}'", request.getEmail()));
		} catch (Exception exp) {
			throw new ReportPortalException("Error while User creating.", exp);
		}

		eventPublisher.publishEvent(new UserCreatedEvent(user, userName));
		CreateUserRS response = new CreateUserRS();
		response.setLogin(user.getLogin());
		return response;
	}

	@Override
	public CreateUserBidRS createUserBid(CreateUserRQ request, Principal principal, String emailURL) {
		// TODO BR for not existing profile
		try {
			emailService.reconfig(settingsRepository.findOne("default").getServerEmailConfig());
			emailService.testConnection();
		} catch (Exception ex) {
			LOGGER.error("Cannot send email to user", ex);
			fail().withError(FORBIDDEN_OPERATION,
					"Email configuration is broken or switched-off. Please config email server in Report Portal settings.");
		}
		User creator = userRepository.findOne(principal.getName());
		expect(creator, notNull()).verify(ACCESS_DENIED);

		String email = EntityUtils.normalizeEmail(request.getEmail());
		expect(UserUtils.isEmailValid(email), equalTo(true)).verify(BAD_REQUEST_ERROR, email);

		Project defaultProject = projectRepository.findOne(EntityUtils.normalizeProjectName(request.getDefaultProject()));
		expect(defaultProject, notNull()).verify(PROJECT_NOT_FOUND, request.getDefaultProject());
		UserConfig userConfig = defaultProject.getUsers().get(principal.getName());
		List<Project> projects = projectRepository.findUserProjects(principal.getName());
		expect(defaultProject, not(in(projects))).verify(ACCESS_DENIED);

		Optional<ProjectRole> role = forName(request.getRole());
		expect(role, Preconditions.IS_PRESENT).verify(ROLE_NOT_FOUND, request.getRole());

		// FIXME move to controller level
		if (creator.getRole() != UserRole.ADMINISTRATOR) {
			int creatorProjectRoleLevel = userConfig.getProjectRole().getRoleLevel();
			int newUserProjectRoleLevel = role.get().getRoleLevel();
			expect(creatorProjectRoleLevel >= newUserProjectRoleLevel, equalTo(Boolean.TRUE)).verify(ACCESS_DENIED);
		}

		UserCreationBid bid = userCreationBidBuilder.get().addUserCreationBid(request).build();
		try {
			userCreationBidRepository.save(bid);
		} catch (Exception e) {
			throw new ReportPortalException("Error while user creation bid registering.", e);
		}

		StringBuilder emailLink = new StringBuilder(emailURL);
		try {
			emailLink.append("/ui/#registration?uuid=");
			emailLink.append(bid.getId());
			// TODO NPE handler! Use default from instead...
			emailService.setAddressFrom(defaultProject.getConfiguration().getEmailConfig().getFrom());
			emailService.sendConfirmationEmail("User registration confirmation", new String[] { bid.getEmail() }, emailLink.toString());
		} catch (Exception e) {
			fail().withError(FORBIDDEN_OPERATION, Suppliers.formattedSupplier("Unable to send email for bid '{}'.", bid.getId()));
		}

		CreateUserBidRS response = new CreateUserBidRS();
		String msg = "Bid for user creation with email '" + email
				+ "' is successfully registered. Confirmation info will be send on provided email. Expiration: 1 day.";

		response.setMessage(msg);
		response.setBid(bid.getId());
		response.setBackLink(emailLink.toString());
		return response;
	}

	@Override
	public CreateUserRS createUser(CreateUserRQConfirm request, String uuid, Principal principal) {
		UserCreationBid bid = userCreationBidRepository.findOne(uuid);
		expect(bid, notNull()).verify(INCORRECT_REQUEST, "Impossible to register user. UUID expired or already registered.");

		User user = userRepository.findOne(request.getLogin());
		expect(user, isNull()).verify(USER_ALREADY_EXISTS, Suppliers.formattedSupplier("login='{}'", request.getLogin()));
		// synchronized (this)
		Project defaultProject = projectRepository.findOne(bid.getDefaultProject());
		expect(defaultProject, notNull()).verify(PROJECT_NOT_FOUND, bid.getDefaultProject());

		// populate field from existing bid record
		request.setDefaultProject(bid.getDefaultProject());

		String email = request.getEmail();
		expect(UserUtils.isEmailValid(email), equalTo(true)).verify(BAD_REQUEST_ERROR, email);

		User email_user = userRepository.findOne(request.getEmail());
		expect(email_user, isNull()).verify(USER_ALREADY_EXISTS, Suppliers.formattedSupplier("email='{}'", request.getEmail()));

		user = userBuilder.get().addCreateUserRQ(request).addUserRole(UserRole.USER).build();
		Optional<ProjectRole> projectRole = forName(bid.getRole());
		expect(projectRole, Preconditions.IS_PRESENT).verify(ROLE_NOT_FOUND, bid.getRole());

		Map<String, UserConfig> projectUsers = defaultProject.getUsers();
		if (projectRole.get().equals(CUSTOMER)) {
			projectUsers.put(user.getId(), UserConfig.newOne().withProjectRole(CUSTOMER).withProposedRole(CUSTOMER));
		} else {
			projectUsers.put(user.getId(), UserConfig.newOne().withProjectRole(projectRole.get()).withProposedRole(projectRole.get()));
		}
		defaultProject.setUsers(projectUsers);

		Map<String, UserConfig> demoUsers = new HashMap<>();
		demoUsers.put(user.getId(), UserConfig.newOne().withProjectRole(MEMBER).withProposedRole(MEMBER));

		try {
			userRepository.save(user);
			projectRepository.addUsers(Constants.DEFAULT_PROJECT.toString(), demoUsers);
			if (!Constants.DEFAULT_PROJECT.toString().equalsIgnoreCase(request.getDefaultProject()))
				projectRepository.addUsers(request.getDefaultProject(), projectUsers);

			/*
			 * Generate personal project for the user
			 */
			Project personalProject = PersonalProjectUtils.generatePersonalProject(user);
			if (defaultProject.getId().equals(personalProject.getId())){
				projectRepository.save(personalProject);
			}

			userCreationBidRepository.delete(uuid);
		} catch (DuplicateKeyException e) {
			fail().withError(USER_ALREADY_EXISTS, Suppliers.formattedSupplier("email='{}'", request.getEmail()));
		} catch (Exception exp) {
			throw new ReportPortalException("Error while User creating.", exp);
		}

		eventPublisher.publishEvent(new UserCreatedEvent(user, user.getLogin()));
		CreateUserRS response = new CreateUserRS();
		response.setLogin(user.getLogin());
		return response;
	}

	@Override
	public OperationCompletionRS createRestorePasswordBid(RestorePasswordRQ rq, String baseUrl) {
		try {
			emailService.reconfig(settingsRepository.findOne("default").getServerEmailConfig());
			emailService.testConnection();
		} catch (Exception ex) {
			fail().withError(FORBIDDEN_OPERATION,
					"Email configuration is broken or switched-off. Please config email server in Report Portal settings.");
		}
		String email = EntityUtils.normalizeEmail(rq.getEmail());
		expect(UserUtils.isEmailValid(email), equalTo(true)).verify(BAD_REQUEST_ERROR, email);
		User user = userRepository.findByEmail(email);
		expect(user, notNull()).verify(USER_NOT_FOUND, email);
		expect(user.getType(), equalTo(UserType.INTERNAL)).verify(BAD_REQUEST_ERROR, "Unable to change password for external user");
		RestorePasswordBid bid = restorePasswordBidBuilder.get().addRestorePasswordBid(rq).build();
		restorePasswordBidRepository.save(bid);
		try {
			// TODO use default 'from' param or project specified?
			emailService.sendRestorePasswordEmail("Password recovery", new String[] { rq.getEmail() },
					baseUrl + "#login?reset=" + bid.getId(), user.getLogin());
		} catch (Exception e) {
			fail().withError(FORBIDDEN_OPERATION, Suppliers.formattedSupplier("Unable to send email for bid '{}'.", bid.getId()));
		}
		return new OperationCompletionRS("Email has been sent");
	}

	@Override
	public OperationCompletionRS resetPassword(ResetPasswordRQ rq) {
		RestorePasswordBid bid = restorePasswordBidRepository.findOne(rq.getUuid());
		expect(bid, notNull()).verify(ACCESS_DENIED, "The password change link is no longer valid.");
		String email = bid.getEmail();
		expect(UserUtils.isEmailValid(email), equalTo(true)).verify(BAD_REQUEST_ERROR, email);
		User byEmail = userRepository.findByEmail(email);
		expect(byEmail, notNull()).verify(USER_NOT_FOUND);
		expect(byEmail.getType(), equalTo(UserType.INTERNAL)).verify(BAD_REQUEST_ERROR,
				"Unable to change password for external user");
		byEmail.setPassword(UserUtils.generateMD5(rq.getPassword()));
		userRepository.save(byEmail);
		restorePasswordBidRepository.delete(rq.getUuid());
		OperationCompletionRS rs = new OperationCompletionRS();
		rs.setResultMessage("Password has been changed");
		return rs;
	}

	@Override
	public YesNoRS isResetPasswordBidExist(String id) {
		RestorePasswordBid bid = restorePasswordBidRepository.findOne(id);
		return new YesNoRS(null != bid);
	}
}
