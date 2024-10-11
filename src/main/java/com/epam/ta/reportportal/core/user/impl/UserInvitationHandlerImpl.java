/*
 * Copyright 2024 EPAM Systems
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

import static com.epam.reportportal.api.model.InvitationStatus.PENDING;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.rules.exception.ErrorType.USER_ALREADY_EXISTS;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;

import com.epam.reportportal.api.model.Invitation;
import com.epam.reportportal.api.model.InvitationRequest;
import com.epam.reportportal.api.model.InvitationRequestOrganizationsInner;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.user.UserInvitationHandler;
import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.Metadata;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserCreationBid;
import com.epam.ta.reportportal.util.UserUtils;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.google.common.collect.Maps;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class UserInvitationHandlerImpl implements UserInvitationHandler {

  public static final String BID_TYPE = "type";
  public static final String INTERNAL_BID_TYPE = "internal";

  private final UserCreationBidRepository userCreationBidRepository;
  private final ThreadPoolTaskExecutor emailExecutorService;
  private final MailServiceFactory emailServiceFactory;
  private final UserRepository userRepository;
  private final GetIntegrationHandler getIntegrationHandler;
  private final ApplicationEventPublisher eventPublisher;

  public UserInvitationHandlerImpl(UserCreationBidRepository userCreationBidRepository,
      ThreadPoolTaskExecutor emailExecutorService, MailServiceFactory emailServiceFactory,
      UserRepository userRepository, GetIntegrationHandler getIntegrationHandler,
      ApplicationEventPublisher eventPublisher) {
    this.userCreationBidRepository = userCreationBidRepository;
    this.emailExecutorService = emailExecutorService;
    this.emailServiceFactory = emailServiceFactory;
    this.userRepository = userRepository;
    this.getIntegrationHandler = getIntegrationHandler;
    this.eventPublisher = eventPublisher;
  }

  public Invitation createUserInvitation(InvitationRequest request, ReportPortalUser rpUser,
      String baseUrl) {
    log.debug("User '{}' is trying to create invitation for user '{}'",
        rpUser.getUsername(),
        request.getEmail());

    validateInvitationRequest(request);

    var now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

    /* TODO: waiting for requirements
     Integration integration = getIntegrationHandler
      .getEnabledByProjectIdOrGlobalAndIntegrationGroup(defaultProject.getId(),
          IntegrationGroupEnum.NOTIFICATION)
      .orElseThrow(() -> new ReportPortalException(EMAIL_CONFIGURATION_IS_INCORRECT,
                "Please configure email server in ReportPortal settings."
            ));
    */

    UserCreationBid userBid = new UserCreationBid();
    var user = userRepository.getById(rpUser.getUserId());
    userBid.setUuid(UUID.randomUUID().toString());
    userBid.setEmail(request.getEmail().trim());
    userBid.setInvitingUser(user);
    userBid.setMetadata(getUserCreationBidMetadata(request.getOrganizations()));

    try {
      userCreationBidRepository.save(userBid);
    } catch (Exception e) {
      throw new ReportPortalException("Error while user creation bid registering.", e);
    }

    StringBuilder emailLink = new StringBuilder(baseUrl)
        .append("/ui/#registration?uuid=")
        .append(userBid.getUuid());

    var response = new Invitation();
    response.setCreatedAt(now);
    response.setExpiresAt(now.plus(1, ChronoUnit.DAYS));
    response.setId(UUID.fromString(userBid.getUuid()));
    response.setLink(URI.create(emailLink.toString()));
    response.setStatus(PENDING);

    /*
    emailExecutorService.execute(() -> emailServiceFactory.getEmailService(integration, false)
        .sendCreateUserConfirmationEmail("User registration confirmation",
            new String[]{bid.getEmail()}, emailLink.toString()));
     eventPublisher.publishEvent(
        new CreateInvitationLinkEvent(rpUser.getUserId(), rpUser.getUsername(),
            defaultProject.getId()));
    */

    return response;
  }

  private void validateInvitationRequest(InvitationRequest request) {
    expect(UserUtils.isEmailValid(request.getEmail().trim()), equalTo(true))
        .verify(BAD_REQUEST_ERROR, formattedSupplier("email='{}'", request.getEmail()));

    Optional<User> emailUser = userRepository.findByEmail(request.getEmail().trim());

    expect(emailUser.isPresent(), equalTo(Boolean.FALSE)).verify(USER_ALREADY_EXISTS,
        formattedSupplier("email='{}'", request.getEmail()));
  }

  private Metadata getUserCreationBidMetadata(
      @Valid List<InvitationRequestOrganizationsInner> organizations) {
    final Map<String, Object> meta = Maps.newHashMapWithExpectedSize(1);
    meta.put(BID_TYPE, INTERNAL_BID_TYPE);
    meta.put("organizations", getOrganizationsMetadata(organizations));
    meta.put("projects", getProjectsMetadata(organizations));

    return new Metadata(meta);
  }

  private List<Map<String, Object>> getProjectsMetadata(
      List<InvitationRequestOrganizationsInner> organizations) {
    return organizations.stream()
        .flatMap(org -> org.getProjects().stream())
        .map(project -> {
          Map<String, Object> obj = new HashMap<>();
          obj.put("id", project.getId());
          obj.put("role", project.getProjectRole().name());
          return obj;
        }).toList();
  }


  private List<Map<String, Object>> getOrganizationsMetadata(
      List<InvitationRequestOrganizationsInner> organizations) {
    return organizations.stream()
        .map(org -> {
          Map<String, Object> obj = new HashMap<>();
          obj.put("id", org.getId());
          obj.put("role", org.getOrgRole().name());
          return obj;
        }).toList();
  }
}
