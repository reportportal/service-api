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

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.fail;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.rules.exception.ErrorType.RESOURCE_ALREADY_EXISTS;
import static com.epam.reportportal.rules.exception.ErrorType.USER_ALREADY_EXISTS;
import static com.epam.reportportal.rules.exception.ErrorType.USER_NOT_FOUND;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.util.email.EmailRulesValidator.NORMALIZE_EMAIL;
import static com.epam.ta.reportportal.ws.converter.converters.UserConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.converter.converters.UserConverter.TO_CREATED_USER;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.auth.authenticator.UserAuthenticator;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.UserCreatedEvent;
import com.epam.ta.reportportal.core.user.CreateUserHandler;
import com.epam.ta.reportportal.dao.RestorePasswordBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.user.RestorePasswordBid;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.model.YesNoRS;
import com.epam.ta.reportportal.model.user.CreateUserRQFull;
import com.epam.ta.reportportal.model.user.CreateUserRS;
import com.epam.ta.reportportal.model.user.ResetPasswordRQ;
import com.epam.ta.reportportal.model.user.RestorePasswordRQ;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.converter.builders.UserBuilder;
import com.epam.ta.reportportal.ws.converter.converters.RestorePasswordBidConverter;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import jakarta.persistence.PersistenceException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  private final UserRepository userRepository;

  private final UserAuthenticator userAuthenticator;

  private final MailServiceFactory emailServiceFactory;

  private final RestorePasswordBidRepository restorePasswordBidRepository;

  private final ThreadPoolTaskExecutor emailExecutorService;

  private final PasswordEncoder passwordEncoder;

  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public CreateUserRS createUserByAdmin(
      CreateUserRQFull request,
      ReportPortalUser creator,
      String basicUrl
  ) {
    request.setEmail(NORMALIZE_EMAIL.apply(request.getEmail()));
    var user = saveUser(request);

    UserCreatedEvent userCreatedEvent = new UserCreatedEvent(
        TO_ACTIVITY_RESOURCE.apply(user, null),
        creator.getUserId(),
        creator.getEmail(),
        false
    );

    eventPublisher.publishEvent(userCreatedEvent);

    emailExecutorService.execute(() -> emailServiceFactory.getDefaultEmailService(true)
        .sendCreateUserConfirmationEmail(request, basicUrl));

    return TO_CREATED_USER.apply(user);
  }

  @Override
  public OperationCompletionRS createRestorePasswordBid(RestorePasswordRQ rq, String baseUrl) {
    var email = NORMALIZE_EMAIL.apply(rq.getEmail());

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, email));

    var bid = restorePasswordBidRepository.findByEmail(rq.getEmail())
        .orElseGet(() -> {
          expect(user.getUserType(), equalTo(UserType.INTERNAL)).verify(
              BAD_REQUEST_ERROR, "Unable to change password for external user");

          RestorePasswordBid newBid = RestorePasswordBidConverter.TO_BID.apply(rq);
          restorePasswordBidRepository.save(newBid);
          return newBid;
        });

    emailServiceFactory.getDefaultEmailService(true)
        .sendRestorePasswordEmail("Password recovery",
            new String[]{email},
            baseUrl + "#login?reset=" + bid.getUuid(),
            user.getLogin()
        );

    return new OperationCompletionRS("Email has been sent");
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

  private User saveUser(CreateUserRQFull request) {
    expect(userRepository.findByEmail(request.getEmail()).isEmpty(), equalTo(true))
        .verify(USER_ALREADY_EXISTS, formattedSupplier("email='{}'", request.getEmail()));

    final User user = convert(request);

    try {
      userRepository.save(user);
      userAuthenticator.authenticate(user);
    } catch (PersistenceException pe) {
      if (pe.getCause() instanceof ConstraintViolationException) {
        fail().withError(RESOURCE_ALREADY_EXISTS,
            ((ConstraintViolationException) pe.getCause()).getConstraintName());
      }
      throw new ReportPortalException("Error while User creating: " + pe.getMessage(), pe);
    } catch (Exception exp) {
      throw new ReportPortalException("Error while User creating: " + exp.getMessage(), exp);
    }
    // TODO: assign organization
    // final Project personalProject = createProjectHandler.createPersonal(user);
    // projectUserHandler.assign(user, personalProject, ProjectRole.EDITOR, creator,
    //     isSystemEvent);

    return user;
  }

  private User convert(CreateUserRQFull request) {
    final UserRole userRole = UserRole.findByName(request.getAccountRole())
        .orElseThrow(() -> new ReportPortalException(
            BAD_REQUEST_ERROR, "Incorrect specified Account Role parameter."));

    User user = new UserBuilder().addCreateUserFullRQ(request)
        .addUserRole(userRole)
        .get();

    ofNullable(request.getPassword())
        .ifPresent(password -> user.setPassword(passwordEncoder.encode(password)));

    return user;
  }

  //  TODO: Return to the code after the project assignment is implemented
  //  private void assignDefaultProject(
  //      User creator,
  //      User user,
  //      String defaultProject,
  //      String role
  //  ) {
  //    var projectToAssign = getProjectHandler.getRaw(normalizeId(defaultProject));
  //    var projectRole = forName(role).orElseThrow(
  //        () -> new ReportPortalException(ROLE_NOT_FOUND, role));
  //    projectUserHandler.assign(user, projectToAssign, projectRole, creator, false);
  //  }
}
