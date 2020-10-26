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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.UserCreatedEvent;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.user.CreateUserHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
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
import com.epam.ta.reportportal.ws.converter.converters.RestorePasswordBidConverter;
import com.epam.ta.reportportal.ws.converter.converters.UserCreationBidConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.activity.UserActivityResource;
import com.epam.ta.reportportal.ws.model.user.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.project.ProjectRole.forName;
import static com.epam.ta.reportportal.entity.project.ProjectUtils.findUserConfigByLogin;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Implementation of Create User handler
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class CreateUserHandlerImpl implements CreateUserHandler {

	private final UserRepository userRepository;

	private final ProjectRepository projectRepository;

	private final MailServiceFactory emailServiceFactory;

	private final UserCreationBidRepository userCreationBidRepository;

	private final RestorePasswordBidRepository restorePasswordBidRepository;

	private final MessageBus messageBus;

	private final SaveDefaultProjectService saveDefaultProjectService;

	private final GetIntegrationHandler getIntegrationHandler;

	private final ThreadPoolTaskExecutor emailExecutorService;

	private final PasswordEncoder passwordEncoder;

	@Autowired
	public CreateUserHandlerImpl(PasswordEncoder passwordEncoder, UserRepository userRepository, ProjectRepository projectRepository,
			MailServiceFactory emailServiceFactory, UserCreationBidRepository userCreationBidRepository,
			RestorePasswordBidRepository restorePasswordBidRepository, MessageBus messageBus,
			SaveDefaultProjectService saveDefaultProjectService, GetIntegrationHandler getIntegrationHandler,
			ThreadPoolTaskExecutor emailExecutorService) {
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
		this.projectRepository = projectRepository;
		this.emailServiceFactory = emailServiceFactory;
		this.userCreationBidRepository = userCreationBidRepository;
		this.restorePasswordBidRepository = restorePasswordBidRepository;
		this.messageBus = messageBus;
		this.saveDefaultProjectService = saveDefaultProjectService;
		this.getIntegrationHandler = getIntegrationHandler;
		this.emailExecutorService = emailExecutorService;
	}

	@Override
	public CreateUserRS createUserByAdmin(CreateUserRQFull request, ReportPortalUser creator, String basicUrl) {
		// creator validation
		User administrator = userRepository.findByLogin(creator.getUsername())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, creator.getUsername()));
		expect(administrator.getRole(), equalTo(UserRole.ADMINISTRATOR)).verify(ACCESS_DENIED,
				Suppliers.formattedSupplier("Only administrator can create new user. Your role is - {}", administrator.getRole())
		);

		request.setLogin(normalizeAndValidateLogin(request.getLogin()));
		request.setEmail(normalizeAndValidateEmail(request.getEmail()));

		Pair<UserActivityResource, CreateUserRS> pair = saveDefaultProjectService.saveDefaultProject(request, basicUrl);
		UserCreatedEvent userCreatedEvent = new UserCreatedEvent(pair.getKey(), creator.getUserId(), creator.getUsername());
		messageBus.publishActivity(userCreatedEvent);
		return pair.getValue();

	}

	@Override
	public CreateUserBidRS createUserBid(CreateUserRQ request, ReportPortalUser loggedInUser, String emailURL) {

		Project defaultProject = projectRepository.findByName(EntityUtils.normalizeId(request.getDefaultProject()))
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, request.getDefaultProject()));

		expect(userRepository.existsById(loggedInUser.getUserId()), BooleanUtils::isTrue).verify(USER_NOT_FOUND,
				loggedInUser.getUsername()
		);

		Integration integration = getIntegrationHandler.getEnabledByProjectIdOrGlobalAndIntegrationGroup(defaultProject.getId(),
				IntegrationGroupEnum.NOTIFICATION
		)
				.orElseThrow(() -> new ReportPortalException(EMAIL_CONFIGURATION_IS_INCORRECT,
						"Please configure email server in Report Portal settings."
				));

		request.setEmail(normalizeAndValidateEmail(request.getEmail()));

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
	public CreateUserRS createUser(CreateUserRQConfirm request, String uuid) {
		UserCreationBid bid = userCreationBidRepository.findById(uuid)
				.orElseThrow(() -> new ReportPortalException(INCORRECT_REQUEST,
						"Impossible to register user. UUID expired or already registered."
				));

		CreateUserRQFull createUserRQFull = new CreateUserRQFull();

		String login = normalizeAndValidateLogin(request.getLogin());
		createUserRQFull.setLogin(login);
		String email = normalizeAndValidateEmail(request.getEmail());
		expect(email, Predicate.isEqual(bid.getEmail())).verify(INCORRECT_REQUEST, "Email from bid not match.");
		createUserRQFull.setEmail(email);
		createUserRQFull.setFullName(request.getFullName());
		createUserRQFull.setPassword(request.getPassword());
		createUserRQFull.setDefaultProject(bid.getDefaultProject().getName());
		createUserRQFull.setAccountRole(UserRole.USER.name());
		createUserRQFull.setProjectRole(bid.getRole());

		Pair<UserActivityResource, CreateUserRS> pair = saveDefaultProjectService.saveDefaultProject(createUserRQFull, null);
		UserCreatedEvent userCreatedEvent = new UserCreatedEvent(pair.getKey(), pair.getKey().getId(), login);

		userCreationBidRepository.deleteAllByEmail(email);

		messageBus.publishActivity(userCreatedEvent);
		return pair.getValue();
	}

	@Override
	public OperationCompletionRS createRestorePasswordBid(RestorePasswordRQ rq, String baseUrl) {
		String email = EntityUtils.normalizeId(rq.getEmail());
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

	private String normalizeAndValidateEmail(String email) {
		String normalizedEmail = EntityUtils.normalizeId(email.trim());
		expect(UserUtils.isEmailValid(normalizedEmail), equalTo(true)).verify(BAD_REQUEST_ERROR, formattedSupplier("email='{}'", email));
		Optional<User> emailUser = userRepository.findByEmail(normalizedEmail);
		expect(emailUser.isPresent(), equalTo(Boolean.FALSE)).verify(USER_ALREADY_EXISTS, formattedSupplier("email='{}'", email));
		return normalizedEmail;
	}

	private String normalizeAndValidateLogin(String login) {
		String normalizedLogin = EntityUtils.normalizeId(login.trim());
		Optional<User> user = userRepository.findByLogin(normalizedLogin);
		expect(user.isPresent(), equalTo(Boolean.FALSE)).verify(USER_ALREADY_EXISTS, formattedSupplier("login='{}'", login));
		expect(normalizedLogin, Predicates.SPECIAL_CHARS_ONLY.negate()).verify(ErrorType.INCORRECT_REQUEST,
				formattedSupplier("Username '{}' consists only of special characters", login)
		);
		return normalizedLogin;
	}
}
