/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.core.user.impl;

import static com.epam.reportportal.infrastructure.persistence.commons.Predicates.equalTo;
import static com.epam.reportportal.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.infrastructure.rules.commons.validation.BusinessRule.fail;
import static com.epam.reportportal.infrastructure.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.RESOURCE_ALREADY_EXISTS;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.USER_ALREADY_EXISTS;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.USER_NOT_FOUND;
import static com.epam.reportportal.util.email.EmailRulesValidator.NORMALIZE_EMAIL;
import static com.epam.reportportal.ws.converter.converters.UserConverter.TO_ACTIVITY_RESOURCE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.InstanceUser;
import com.epam.reportportal.api.model.NewUserRequest;
import com.epam.reportportal.core.events.activity.UserCreatedEvent;
import com.epam.reportportal.core.organization.PersonalOrganizationService;
import com.epam.reportportal.core.user.CreateUserHandler;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.dao.RestorePasswordBidRepository;
import com.epam.reportportal.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.infrastructure.persistence.entity.user.RestorePasswordBid;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.model.YesNoRS;
import com.epam.reportportal.model.user.ResetPasswordRQ;
import com.epam.reportportal.model.user.RestorePasswordRQ;
import com.epam.reportportal.reporting.OperationCompletionRS;
import com.epam.reportportal.util.email.MailServiceFactory;
import com.epam.reportportal.ws.converter.builders.UserBuilder;
import com.epam.reportportal.ws.converter.converters.RestorePasswordBidConverter;
import com.epam.reportportal.ws.converter.converters.UserConverter;
import jakarta.persistence.PersistenceException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of Create User handler.
 *
 * @author Andrei_Ramanchuk
 */
@Service
@RequiredArgsConstructor
public class CreateUserHandlerImpl implements CreateUserHandler {

  public static final String BID_TYPE = "type";
  public static final String INTERNAL_BID_TYPE = "internal";
  public static final String EMAIL_HAS_BEEN_SENT = "Your request has been processed. The letter will be sent to the address if it is registered in the system.";

  private final UserRepository userRepository;
  private final MailServiceFactory emailServiceFactory;
  private final RestorePasswordBidRepository restorePasswordBidRepository;
  private final ThreadPoolTaskExecutor emailExecutorService;
  private final PasswordEncoder passwordEncoder;
  private final ApplicationEventPublisher eventPublisher;
  private final PersonalOrganizationService personalOrganizationService;

  @Override
  public InstanceUser createUser(NewUserRequest request, ReportPortalUser creator, String basicUrl) {
    var email = NORMALIZE_EMAIL.apply(request.getEmail());
    request.setEmail(email);

    var savedUser = saveUser(request);

    personalOrganizationService.createPersonalOrganization(savedUser.getId());

    var userCreatedEvent = new UserCreatedEvent(
        TO_ACTIVITY_RESOURCE.apply(savedUser, null),
        creator.getUserId(),
        creator.getEmail(),
        false
    );
    eventPublisher.publishEvent(userCreatedEvent);

    emailExecutorService.execute(() -> emailServiceFactory.getDefaultEmailService(true)
        .sendCreateUserConfirmationEmail(request, basicUrl));

    return UserConverter.TO_INSTANCE_USER.apply(savedUser);
  }

  @Override
  public OperationCompletionRS createRestorePasswordBid(RestorePasswordRQ rq, String baseUrl) {
    var email = NORMALIZE_EMAIL.apply(rq.getEmail());

    Optional<User> user = userRepository.findByEmail(email);
    if (user.isEmpty()) {
      return new OperationCompletionRS(EMAIL_HAS_BEEN_SENT);
    }

    Optional<RestorePasswordBid> bidOptional =
        restorePasswordBidRepository.findByEmail(rq.getEmail());

    RestorePasswordBid bid;
    if (bidOptional.isEmpty()) {
      expect(user.get().getUserType(), equalTo(UserType.INTERNAL)).verify(BAD_REQUEST_ERROR,
          "Unable to change password for external user");
      bid = RestorePasswordBidConverter.TO_BID.apply(rq);
      restorePasswordBidRepository.save(bid);
    } else {
      bid = bidOptional.get();
    }

    emailServiceFactory.getDefaultEmailService(true)
        .sendRestorePasswordEmail("Password recovery",
            new String[]{email},
            baseUrl + "#login?reset=" + bid.getUuid(),
            user.get().getLogin()
        );

    return new OperationCompletionRS(EMAIL_HAS_BEEN_SENT);
  }

  @Override
  public OperationCompletionRS resetPassword(ResetPasswordRQ request) {
    var bid = restorePasswordBidRepository.findById(request.getUuid())
        .orElseThrow(() -> new ReportPortalException(
            ACCESS_DENIED, "The password change link is no longer valid."));

    User user = userRepository.findByEmail(NORMALIZE_EMAIL.apply(bid.getEmail()))
        .orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND));

    expect(user.getUserType(), equalTo(UserType.INTERNAL)).verify(
        BAD_REQUEST_ERROR, "Unable to change password for external user");

    user.setPassword(passwordEncoder.encode(request.getPassword()));
    userRepository.save(user);
    restorePasswordBidRepository.deleteById(request.getUuid());

    return new OperationCompletionRS("Password has been changed");
  }

  @Override
  public YesNoRS isResetPasswordBidExist(String uuid) {
    Optional<RestorePasswordBid> bid = restorePasswordBidRepository.findById(uuid);
    return new YesNoRS(bid.isPresent());
  }

  private User saveUser(NewUserRequest request) {
    expect(userRepository.findByEmail(request.getEmail()).isEmpty(), equalTo(true))
        .verify(USER_ALREADY_EXISTS, formattedSupplier("email='{}'", request.getEmail()));
    var user = convert(request);
    try {
      userRepository.save(user);
    } catch (PersistenceException pe) {
      if (pe.getCause() instanceof ConstraintViolationException) {
        fail().withError(RESOURCE_ALREADY_EXISTS,
            ((ConstraintViolationException) pe.getCause()).getConstraintName());
      }
      throw new ReportPortalException("Error while User creating: " + pe.getMessage(), pe);
    } catch (Exception exp) {
      throw new ReportPortalException("Error while User creating: " + exp.getMessage(), exp);
    }
    return user;
  }

  private User convert(NewUserRequest request) {
    User user = new UserBuilder().fromNewUserRequest(request).get();
    ofNullable(request.getPassword()).ifPresent(
        password -> user.setPassword(passwordEncoder.encode(password)));
    return user;
  }
}
