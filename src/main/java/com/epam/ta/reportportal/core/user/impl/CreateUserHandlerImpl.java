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

package com.epam.ta.reportportal.core.user.impl;

import com.epam.ta.reportportal.auth.authenticator.UserAuthenticator;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.UserCreatedEvent;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.project.CreateProjectHandler;
import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.core.project.ProjectUserHandler;
import com.epam.ta.reportportal.core.user.CreateUserHandler;
import com.epam.ta.reportportal.dao.RestorePasswordBidRepository;
import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.*;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.Predicates;
import com.epam.ta.reportportal.util.UserUtils;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.converter.builders.UserBuilder;
import com.epam.ta.reportportal.ws.converter.converters.RestorePasswordBidConverter;
import com.epam.ta.reportportal.ws.converter.converters.UserCreationBidConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.activity.UserActivityResource;
import com.epam.ta.reportportal.ws.model.user.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.util.Optional;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.project.ProjectRole.forName;
import static com.epam.ta.reportportal.entity.project.ProjectUtils.findUserConfigByLogin;
import static com.epam.ta.reportportal.ws.converter.converters.UserConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Implementation of Create User handler
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class CreateUserHandlerImpl implements CreateUserHandler {

	private final UserRepository userRepository;

	private final UserAuthenticator userAuthenticator;

	private final MailServiceFactory emailServiceFactory;

	private final UserCreationBidRepository userCreationBidRepository;

	private final RestorePasswordBidRepository restorePasswordBidRepository;

	private final MessageBus messageBus;

	private final CreateProjectHandler createProjectHandler;
	private final GetProjectHandler getProjectHandler;

	private final ProjectUserHandler projectUserHandler;

	private final GetIntegrationHandler getIntegrationHandler;

	private final ThreadPoolTaskExecutor emailExecutorService;

	private final PasswordEncoder passwordEncoder;

	@Autowired
	public CreateUserHandlerImpl(PasswordEncoder passwordEncoder, UserRepository userRepository, UserAuthenticator userAuthenticator,
			MailServiceFactory emailServiceFactory, UserCreationBidRepository userCreationBidRepository,
			RestorePasswordBidRepository restorePasswordBidRepository, MessageBus messageBus, CreateProjectHandler createProjectHandler,
			GetProjectHandler getProjectHandler, ProjectUserHandler projectUserHandler, GetIntegrationHandler getIntegrationHandler,
			ThreadPoolTaskExecutor emailExecutorService) {
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
		this.createProjectHandler = createProjectHandler;
		this.projectUserHandler = projectUserHandler;
		this.userAuthenticator = userAuthenticator;
		this.emailServiceFactory = emailServiceFactory;
		this.userCreationBidRepository = userCreationBidRepository;
		this.restorePasswordBidRepository = restorePasswordBidRepository;
		this.messageBus = messageBus;
		this.getProjectHandler = getProjectHandler;
		this.getIntegrationHandler = getIntegrationHandler;
		this.emailExecutorService = emailExecutorService;
	}

	@Override
	@Transactional
	public CreateUserRS createUserByAdmin(CreateUserRQFull request, ReportPortalUser creator, String basicUrl) {
		// creator validation
		User administrator = userRepository.findRawById(creator.getUserId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, creator.getUsername()));
		expect(administrator.getRole(), equalTo(UserRole.ADMINISTRATOR)).verify(ACCESS_DENIED,
				Suppliers.formattedSupplier("Only administrator can create new user. Your role is - {}", administrator.getRole())
		);

		normalize(request);

		Pair<UserActivityResource, CreateUserRS> pair = saveUser(request);
		UserCreatedEvent userCreatedEvent = new UserCreatedEvent(pair.getKey(), creator.getUserId(), creator.getUsername());
		messageBus.publishActivity(userCreatedEvent);

		emailExecutorService.execute(() -> emailServiceFactory.getDefaultEmailService(true)
				.sendCreateUserConfirmationEmail(request, basicUrl));
		return pair.getValue();

	}

	private void normalize(CreateUserRQFull request) {
		final String login = normalizeLogin(request.getLogin());
		final String email = normalizeEmail(request.getEmail());
		request.setLogin(login);
		request.setEmail(email);
	}

	private String normalizeLogin(String login) {
		final String normalizedLogin = getNormalized(login);
		validateLogin(login, normalizedLogin);
		return normalizedLogin;
	}

	private void validateLogin(String original, String normalized) {
		Optional<User> user = userRepository.findByLogin(normalized);
		expect(user.isPresent(), equalTo(Boolean.FALSE)).verify(USER_ALREADY_EXISTS, formattedSupplier("login='{}'", original));
		expect(normalized, Predicates.SPECIAL_CHARS_ONLY.negate()).verify(ErrorType.INCORRECT_REQUEST,
				formattedSupplier("Username '{}' consists only of special characters", original)
		);
	}

	private String normalizeEmail(String email) {
		final String normalizedEmail = getNormalized(email);
		validateEmail(email, normalizedEmail);
		return normalizedEmail;
	}

	private void validateEmail(String original, String normalized) {
		expect(UserUtils.isEmailValid(normalized), equalTo(true)).verify(BAD_REQUEST_ERROR, formattedSupplier("email='{}'", original));
		Optional<User> emailUser = userRepository.findByEmail(normalized);
		expect(emailUser.isPresent(), equalTo(Boolean.FALSE)).verify(USER_ALREADY_EXISTS, formattedSupplier("email='{}'", original));
	}

	private String getNormalized(String original) {
		return normalizeId(original.trim());
	}

	private Pair<UserActivityResource, CreateUserRS> saveUser(CreateUserRQFull request) {

		final Project projectToAssign = getProjectHandler.getRaw(normalizeId(request.getDefaultProject()));
		final ProjectRole projectRole = forName(request.getProjectRole()).orElseThrow(() -> new ReportPortalException(ROLE_NOT_FOUND,
				request.getProjectRole()
		));

		final User user = convert(request);

		try {
			userRepository.save(user);
		} catch (PersistenceException pe) {
			if (pe.getCause() instanceof ConstraintViolationException) {
				fail().withError(RESOURCE_ALREADY_EXISTS, ((ConstraintViolationException) pe.getCause()).getConstraintName());
			}
			throw new ReportPortalException("Error while User creating: " + pe.getMessage(), pe);
		} catch (Exception exp) {
			throw new ReportPortalException("Error while User creating: " + exp.getMessage(), exp);
		}

		userAuthenticator.authenticate(user);

		projectUserHandler.assign(user, projectToAssign, projectRole);
		final Project personalProject = createProjectHandler.createPersonal(user);
		projectUserHandler.assign(user, personalProject, ProjectRole.PROJECT_MANAGER);

		final CreateUserRS response = new CreateUserRS();
		response.setId(user.getId());
		response.setLogin(user.getLogin());

		return Pair.of(TO_ACTIVITY_RESOURCE.apply(user, projectToAssign.getId()), response);
	}

	private User convert(CreateUserRQFull request) {
		final UserRole userRole = UserRole.findByName(request.getAccountRole())
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, "Incorrect specified Account Role parameter."));
		return new UserBuilder().addCreateUserFullRQ(request)
				.addUserRole(userRole)
				.addPassword(passwordEncoder.encode(request.getPassword()))
				.get();
	}

	@Override
	@Transactional
	public CreateUserRS createUser(CreateUserRQConfirm request, String uuid) {
		final UserCreationBid bid = userCreationBidRepository.findById(uuid)
				.orElseThrow(() -> new ReportPortalException(INCORRECT_REQUEST,
						"Impossible to register user. UUID expired or already registered."
				));

		final CreateUserRQFull createUserRQFull = convertToCreateRequest(request, bid);

		normalize(createUserRQFull);
		expect(createUserRQFull.getEmail(), Predicate.isEqual(bid.getEmail())).verify(INCORRECT_REQUEST, "Email from bid not match.");

		final Pair<UserActivityResource, CreateUserRS> pair = saveUser(createUserRQFull);

		userCreationBidRepository.deleteAllByEmail(createUserRQFull.getEmail());

		final UserCreatedEvent userCreatedEvent = new UserCreatedEvent(pair.getKey(), pair.getKey().getId(), createUserRQFull.getLogin());
		messageBus.publishActivity(userCreatedEvent);

		return pair.getValue();
	}

	private CreateUserRQFull convertToCreateRequest(CreateUserRQConfirm request, UserCreationBid bid) {
		CreateUserRQFull createUserRQFull = new CreateUserRQFull();
		createUserRQFull.setLogin(request.getLogin());
		createUserRQFull.setEmail(request.getEmail());
		createUserRQFull.setFullName(request.getFullName());
		createUserRQFull.setPassword(request.getPassword());
		createUserRQFull.setDefaultProject(bid.getDefaultProject().getName());
		createUserRQFull.setAccountRole(UserRole.USER.name());
		createUserRQFull.setProjectRole(bid.getRole());
		return createUserRQFull;
	}

	@Override
	public CreateUserBidRS createUserBid(CreateUserRQ request, ReportPortalUser loggedInUser, String emailURL) {

		final Project defaultProject = getProjectHandler.getProject(normalizeId(request.getDefaultProject()));

		expect(userRepository.existsById(loggedInUser.getUserId()), BooleanUtils::isTrue).verify(USER_NOT_FOUND,
				loggedInUser.getUsername()
		);

		Integration integration = getIntegrationHandler.getEnabledByProjectIdOrGlobalAndIntegrationGroup(defaultProject.getId(),
						IntegrationGroupEnum.NOTIFICATION
				)
				.orElseThrow(() -> new ReportPortalException(EMAIL_CONFIGURATION_IS_INCORRECT,
						"Please configure email server in Report Portal settings."
				));

		final String normalizedEmail = normalizeEmail(request.getEmail());
		request.setEmail(normalizedEmail);

		if (loggedInUser.getUserRole() != UserRole.ADMINISTRATOR) {
			ProjectUser projectUser = findUserConfigByLogin(defaultProject, loggedInUser.getUsername());
			expect(projectUser, not(isNull())).verify(ACCESS_DENIED,
					formattedSupplier("'{}' is not your project", defaultProject.getName())
			);
			expect(projectUser.getProjectRole(), Predicate.isEqual(ProjectRole.PROJECT_MANAGER)).verify(ACCESS_DENIED);
		}

		request.setRole(forName(request.getRole()).orElseThrow(() -> new ReportPortalException(ROLE_NOT_FOUND, request.getRole())).name());

		UserCreationBid bid = UserCreationBidConverter.TO_USER.apply(request, defaultProject);
		try {
			userCreationBidRepository.save(bid);
		} catch (Exception e) {
			throw new ReportPortalException("Error while user creation bid registering.", e);
		}

		StringBuilder emailLink = new StringBuilder(emailURL).append("/ui/#registration?uuid=").append(bid.getUuid());
		emailExecutorService.execute(() -> emailServiceFactory.getEmailService(integration, false)
				.sendCreateUserConfirmationEmail("User registration confirmation", new String[] { bid.getEmail() }, emailLink.toString()));

		CreateUserBidRS response = new CreateUserBidRS();
		String msg = "Bid for user creation with email '" + request.getEmail()
				+ "' is successfully registered. Confirmation info will be send on provided email. Expiration: 1 day.";

		response.setMessage(msg);
		response.setBid(bid.getUuid());
		response.setBackLink(emailLink.toString());
		return response;
	}

	@Override
	public OperationCompletionRS createRestorePasswordBid(RestorePasswordRQ rq, String baseUrl) {
		String email = normalizeId(rq.getEmail());
		expect(UserUtils.isEmailValid(email), equalTo(true)).verify(BAD_REQUEST_ERROR, email);

		User user = userRepository.findByEmail(email).orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, email));
		Optional<RestorePasswordBid> bidOptional = restorePasswordBidRepository.findByEmail(rq.getEmail());

		RestorePasswordBid bid;
		if (bidOptional.isEmpty()) {
			expect(user.getUserType(), equalTo(UserType.INTERNAL)).verify(BAD_REQUEST_ERROR, "Unable to change password for external user");
			bid = RestorePasswordBidConverter.TO_BID.apply(rq);
			restorePasswordBidRepository.save(bid);
		} else {
			bid = bidOptional.get();
		}

		emailServiceFactory.getDefaultEmailService(true)
				.sendRestorePasswordEmail("Password recovery",
						new String[] { email },
						baseUrl + "#login?reset=" + bid.getUuid(),
						user.getLogin()
				);

		return new OperationCompletionRS("Email has been sent");
	}

	@Override
	public OperationCompletionRS resetPassword(ResetPasswordRQ request) {
		RestorePasswordBid bid = restorePasswordBidRepository.findById(request.getUuid())
				.orElseThrow(() -> new ReportPortalException(ACCESS_DENIED, "The password change link is no longer valid."));
		String email = bid.getEmail();
		expect(UserUtils.isEmailValid(email), equalTo(true)).verify(BAD_REQUEST_ERROR, email);
		User byEmail = userRepository.findByEmail(email).orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND));
		expect(byEmail.getUserType(), equalTo(UserType.INTERNAL)).verify(BAD_REQUEST_ERROR, "Unable to change password for external user");
		byEmail.setPassword(passwordEncoder.encode(request.getPassword()));
		userRepository.save(byEmail);
		restorePasswordBidRepository.deleteById(request.getUuid());
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
