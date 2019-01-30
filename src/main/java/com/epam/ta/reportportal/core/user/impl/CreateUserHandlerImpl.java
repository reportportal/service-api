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

package com.epam.ta.reportportal.core.user.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.UserCreatedEvent;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.user.CreateUserHandler;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.*;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.PersonalProjectService;
import com.epam.ta.reportportal.util.Predicates;
import com.epam.ta.reportportal.util.UserUtils;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.converter.builders.UserBuilder;
import com.epam.ta.reportportal.ws.converter.converters.RestorePasswordBidConverter;
import com.epam.ta.reportportal.ws.converter.converters.UserCreationBidConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.activity.UserActivityResource;
import com.epam.ta.reportportal.ws.model.user.*;
import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.in;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.project.ProjectRole.forName;
import static com.epam.ta.reportportal.entity.project.ProjectUtils.findUserConfigByLogin;
import static com.epam.ta.reportportal.ws.converter.converters.UserConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;

/**
 * Implementation of Create User handler
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class CreateUserHandlerImpl implements CreateUserHandler {

	static final HashFunction HASH_FUNCTION = Hashing.md5();

	private final UserRepository userRepository;

	private final ProjectRepository projectRepository;

	private final PersonalProjectService personalProjectService;

	private final MailServiceFactory emailServiceFactory;

	private final UserCreationBidRepository userCreationBidRepository;

	private final RestorePasswordBidRepository restorePasswordBidRepository;

	private final MessageBus messageBus;

	private final SaveDefaultProjectService saveDefaultProjectService;

	private final GetIntegrationHandler getIntegrationHandler;

	@Autowired
	public CreateUserHandlerImpl(UserRepository userRepository, ProjectRepository projectRepository,
			PersonalProjectService personalProjectService, MailServiceFactory emailServiceFactory,
			UserCreationBidRepository userCreationBidRepository, RestorePasswordBidRepository restorePasswordBidRepository,
			MessageBus messageBus, SaveDefaultProjectService saveDefaultProjectService, GetIntegrationHandler getIntegrationHandler) {
		this.userRepository = userRepository;
		this.projectRepository = projectRepository;
		this.personalProjectService = personalProjectService;
		this.emailServiceFactory = emailServiceFactory;
		this.userCreationBidRepository = userCreationBidRepository;
		this.restorePasswordBidRepository = restorePasswordBidRepository;
		this.messageBus = messageBus;
		this.saveDefaultProjectService = saveDefaultProjectService;
		this.getIntegrationHandler = getIntegrationHandler;
	}

	@Override
	public CreateUserRS createUserByAdmin(CreateUserRQFull request, ReportPortalUser creator, String basicUrl) {
		User administrator = userRepository.findByLogin(creator.getUsername())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, creator.getUsername()));

		expect(administrator.getRole(), equalTo(UserRole.ADMINISTRATOR)).verify(ACCESS_DENIED,
				Suppliers.formattedSupplier("Only administrator can create new user. Your role is - {}", administrator.getRole())
		);

		String newUsername = EntityUtils.normalizeId(request.getLogin());

		expect(userRepository.findByLogin(newUsername).isPresent(), equalTo(false)).verify(USER_ALREADY_EXISTS,
				formattedSupplier("login='{}'", newUsername)
		);

		expect(newUsername, Predicates.SPECIAL_CHARS_ONLY.negate()).verify(ErrorType.INCORRECT_REQUEST,
				formattedSupplier("Username '{}' consists only of special characters", newUsername)
		);

		String email = EntityUtils.normalizeId(request.getEmail());
		expect(UserUtils.isEmailValid(email), equalTo(true)).verify(BAD_REQUEST_ERROR, formattedSupplier("email = '{}'", email));
		expect(userRepository.findByEmail(email).isPresent(), equalTo(false)).verify(USER_ALREADY_EXISTS,
				formattedSupplier("email = '{}'", email)
		);

		Pair<UserActivityResource, CreateUserRS> pair = saveDefaultProjectService.saveDefaultProject(request, email, basicUrl);
		messageBus.publishActivity(new UserCreatedEvent(pair.getKey(), creator.getUserId()));
		return pair.getValue();

	}

	@Override
	public CreateUserBidRS createUserBid(CreateUserRQ request, ReportPortalUser loggedInUser, String emailURL) {

		Project defaultProject = projectRepository.findByName(EntityUtils.normalizeId(request.getDefaultProject()))
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, request.getDefaultProject()));

		Integration integration = getIntegrationHandler.findEnabledByProjectIdOrGlobalAndIntegrationGroup(defaultProject.getId(),
				IntegrationGroupEnum.NOTIFICATION
		)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						"Neither project nor global enabled email integrations have been found."
				));

		EmailService emailService = emailServiceFactory.getEmailService(integration, true);

		User creator = userRepository.findByLogin(loggedInUser.getUsername()).orElseThrow(() -> new ReportPortalException(ACCESS_DENIED));

		String email = EntityUtils.normalizeId(request.getEmail());
		expect(UserUtils.isEmailValid(email), equalTo(true)).verify(BAD_REQUEST_ERROR, email);

		Optional<User> emailUser = userRepository.findByEmail(request.getEmail());

		expect(emailUser.isPresent(), equalTo(Boolean.FALSE)).verify(USER_ALREADY_EXISTS,
				formattedSupplier("email={}", request.getEmail())
		);

		ProjectUser projectUser = findUserConfigByLogin(defaultProject, loggedInUser.getUsername());
		List<Project> projects = projectRepository.findUserProjects(loggedInUser.getUsername());

		expect(defaultProject, in(projects)).verify(ACCESS_DENIED);

		ProjectRole role = forName(request.getRole()).orElseThrow(() -> new ReportPortalException(ROLE_NOT_FOUND, request.getRole()));

		// FIXME move to controller level
		if (creator.getRole() != UserRole.ADMINISTRATOR) {
			expect(ofNullable(projectUser).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND))
					.getProjectRole()
					.sameOrHigherThan(role), equalTo(Boolean.TRUE)).verify(ACCESS_DENIED);
		}

		UserCreationBid bid = UserCreationBidConverter.TO_USER.apply(request, defaultProject);
		try {
			userCreationBidRepository.save(bid);
		} catch (Exception e) {
			throw new ReportPortalException("Error while user creation bid registering.", e);
		}

		StringBuilder emailLink = new StringBuilder(emailURL);
		try {
			emailLink.append("/ui/#registration?uuid=");
			emailLink.append(bid.getUuid());
			emailService.sendCreateUserConfirmationEmail("User registration confirmation",
					new String[] { bid.getEmail() },
					emailLink.toString()
			);
		} catch (Exception e) {
			fail().withError(EMAIL_CONFIGURATION_IS_INCORRECT,
					formattedSupplier("Unable to send email for bid '{}'." + e.getMessage(), bid.getUuid())
			);
		}

		CreateUserBidRS response = new CreateUserBidRS();
		String msg = "Bid for user creation with email '" + email
				+ "' is successfully registered. Confirmation info will be send on provided email. Expiration: 1 day.";

		response.setMessage(msg);
		response.setBid(bid.getUuid());
		response.setBackLink(emailLink.toString());
		return response;
	}

	@Override
	public CreateUserRS createUser(CreateUserRQConfirm request, String uuid) {
		UserCreationBid bid = userCreationBidRepository.findById(uuid)
				.orElseThrow(() -> new ReportPortalException(INCORRECT_REQUEST,
						"Impossible to register user. UUID expired or already registered."
				));

		Optional<User> user = userRepository.findByLogin(request.getLogin().toLowerCase());
		expect(user.isPresent(), equalTo(Boolean.FALSE)).verify(USER_ALREADY_EXISTS, formattedSupplier("login='{}'", request.getLogin()));

		expect(request.getLogin(), Predicates.SPECIAL_CHARS_ONLY.negate()).verify(ErrorType.INCORRECT_REQUEST,
				formattedSupplier("Username '{}' consists only of special characters", request.getLogin())
		);

		// synchronized (this)
		Project defaultProject = projectRepository.findByName(bid.getDefaultProject().getName())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, bid.getDefaultProject().getName()));

		// populate field from existing bid record
		request.setDefaultProject(bid.getDefaultProject().getName());

		String email = request.getEmail();
		expect(UserUtils.isEmailValid(email), equalTo(true)).verify(BAD_REQUEST_ERROR, email);

		Optional<User> emailUser = userRepository.findByEmail(request.getEmail());
		expect(emailUser.isPresent(), equalTo(Boolean.FALSE)).verify(USER_ALREADY_EXISTS,
				formattedSupplier("email='{}'", request.getEmail())
		);

		User newUser = new UserBuilder().addCreateUserRQ(request).addUserRole(UserRole.USER).get();

		ProjectRole projectRole = forName(bid.getRole()).orElseThrow(() -> new ReportPortalException(ROLE_NOT_FOUND, bid.getRole()));

		Set<ProjectUser> projectUsers = defaultProject.getUsers();

		//@formatter:off
		ProjectUser projectUser = new ProjectUser()
				.withProjectRole(projectRole)
				.withProject(defaultProject)
				.withUser(newUser);

		projectUsers
				.add(projectUser);

		newUser.setProjects(Sets.newHashSet(projectUser));
		//@formatter:on

		try {
			userRepository.save(newUser);
			projectRepository.save(defaultProject);

			/*
			 * Generate personal project for the user
			 */
			Project personalProject = personalProjectService.generatePersonalProject(newUser);
			if (!defaultProject.getId().equals(personalProject.getId())) {
				projectRepository.save(personalProject);
				newUser.setDefaultProject(personalProject);
			}

			userCreationBidRepository.deleteById(uuid);
		} catch (DuplicateKeyException e) {
			fail().withError(USER_ALREADY_EXISTS, formattedSupplier("email='{}'", request.getEmail()));
		} catch (Exception exp) {
			throw new ReportPortalException("Error while User creating.", exp);
		}

		messageBus.publishActivity(new UserCreatedEvent(TO_ACTIVITY_RESOURCE.apply(newUser), newUser.getId()));
		CreateUserRS response = new CreateUserRS();
		response.setLogin(newUser.getLogin());
		return response;
	}

	@Override
	public OperationCompletionRS createRestorePasswordBid(RestorePasswordRQ rq, String baseUrl) {
		EmailService emailService = emailServiceFactory.getDefaultEmailService(true);
		String email = EntityUtils.normalizeId(rq.getEmail());
		expect(UserUtils.isEmailValid(email), equalTo(true)).verify(BAD_REQUEST_ERROR, email);
		User user = userRepository.findByEmail(email).orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, email));
		expect(user.getUserType(), equalTo(UserType.INTERNAL)).verify(BAD_REQUEST_ERROR, "Unable to change password for external user");
		RestorePasswordBid bid = RestorePasswordBidConverter.TO_BID.apply(rq);
		restorePasswordBidRepository.save(bid);
		try {
			// TODO use default 'from' param or project specified?
			emailService.sendRestorePasswordEmail("Password recovery",
					new String[] { email },
					baseUrl + "#login?reset=" + bid.getUuid(),
					user.getLogin()
			);
		} catch (Exception e) {
			fail().withError(FORBIDDEN_OPERATION, formattedSupplier("Unable to send email for bid '{}'.", bid.getUuid()));
		}
		return new OperationCompletionRS("Email has been sent");
	}

	@Override
	public OperationCompletionRS resetPassword(ResetPasswordRQ rq) {
		RestorePasswordBid bid = restorePasswordBidRepository.findById(rq.getUuid())
				.orElseThrow(() -> new ReportPortalException(ACCESS_DENIED, "The password change link is no longer valid."));
		String email = bid.getEmail();
		expect(UserUtils.isEmailValid(email), equalTo(true)).verify(BAD_REQUEST_ERROR, email);
		User byEmail = userRepository.findByEmail(email).orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND));
		expect(byEmail.getUserType(), equalTo(UserType.INTERNAL)).verify(BAD_REQUEST_ERROR, "Unable to change password for external user");
		byEmail.setPassword(HASH_FUNCTION.hashString(rq.getPassword(), Charsets.UTF_8).toString());
		userRepository.save(byEmail);
		restorePasswordBidRepository.deleteById(rq.getUuid());
		OperationCompletionRS rs = new OperationCompletionRS();
		rs.setResultMessage("Password has been changed");
		return rs;
	}

	@Override
	public YesNoRS isResetPasswordBidExist(String uuid) {
		Optional<RestorePasswordBid> bid = restorePasswordBidRepository.findById(uuid);
		return new YesNoRS(bid.isPresent());
	}

}
