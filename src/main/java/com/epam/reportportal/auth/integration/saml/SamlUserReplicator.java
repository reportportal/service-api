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

package com.epam.reportportal.auth.integration.saml;

import static com.epam.reportportal.auth.util.AuthUtils.NORMALIZE_STRING;

import com.epam.reportportal.auth.event.UserEventPublisher;
import com.epam.reportportal.auth.integration.AbstractUserReplicator;
import com.epam.reportportal.auth.integration.AuthIntegrationType;
import com.epam.reportportal.auth.integration.parameter.SamlParameter;
import com.epam.reportportal.auth.model.saml.SamlResponse;
import com.epam.reportportal.auth.oauth.UserSynchronizationException;
import com.epam.reportportal.base.infrastructure.commons.ContentTypeResolver;
import com.epam.reportportal.base.infrastructure.persistence.binary.UserBinaryDataService;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.base.infrastructure.persistence.util.PersonalProjectService;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Replicates user from SAML response into database if it is not exist.
 *
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Slf4j
@Component
public class SamlUserReplicator extends AbstractUserReplicator {

  private static final String DEFAULT_EMAIL_ATTR = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";
  private final IntegrationTypeRepository integrationTypeRepository;
  private final IntegrationRepository integrationRepository;
  private final UserEventPublisher userEventPublisher;

  /**
   * SAML user replicator constructor.
   *
   * @param userRepository            User repository
   * @param projectRepository         Project repository
   * @param personalProjectService    Personal project service
   * @param userBinaryDataService     User binary data service
   * @param integrationTypeRepository Integration type repository
   * @param integrationRepository     Integration repository
   * @param contentTypeResolver       Content type resolver
   */
  @Autowired
  public SamlUserReplicator(UserRepository userRepository, ProjectRepository projectRepository,
      PersonalProjectService personalProjectService, UserBinaryDataService userBinaryDataService,
      IntegrationTypeRepository integrationTypeRepository,
      IntegrationRepository integrationRepository, ContentTypeResolver contentTypeResolver,
      UserEventPublisher userEventPublisher) {
    super(userRepository, projectRepository, personalProjectService, userBinaryDataService,
        contentTypeResolver
    );
    this.integrationTypeRepository = integrationTypeRepository;
    this.integrationRepository = integrationRepository;
    this.userEventPublisher = userEventPublisher;
  }

  /**
   * Replicates user from SAML response into database if it is not exist.
   *
   * @param samlAuthentication SAML authentication token
   * @return {@link User}
   */
  @Transactional
  public User replicateUser(Saml2AuthenticationToken samlAuthentication) {
    SamlResponse samlResponse;
    try {
      samlResponse = SamlResponseParser.parseSamlResponse(samlAuthentication.getSaml2Response());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    var userEmail = Optional.ofNullable(samlResponse.getNameId().value())
        .filter(StringUtils::isNotBlank)
        .map(NORMALIZE_STRING)
        .orElseThrow(() -> new UserSynchronizationException(
            "SAML response does not contain email")
        );
    Optional<User> userOptional;

    try {
      userOptional = userRepository.findByEmailIgnoreCase(userEmail);
    } catch (IncorrectResultSizeDataAccessException e) {
      log.error("Data integrity violation: Multiple users found with email: {}", userEmail);
      throw new UserSynchronizationException("User with email '"
          + userEmail
          + "' already exists, but multiple records found. Please contact administrator to resolve this issue.");
    }

    return userOptional.orElseGet(() -> createUser(samlResponse.getAttributes(), findProvider(samlResponse)));
  }

  private Integration findProvider(SamlResponse samlResponse) {
    var integrationType = integrationTypeRepository.findByName(AuthIntegrationType.SAML.getName())
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.AUTH_INTEGRATION_NOT_FOUND, AuthIntegrationType.SAML.getName()));

    return integrationRepository.findAllGlobalByType(integrationType)
        .stream()
        .filter(integration -> {
          var alias = SamlParameter.IDP_ALIAS.getParameter(integration);
          return alias.isPresent() && alias.get().equalsIgnoreCase(samlResponse.getIssuer());
        })
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.AUTH_INTEGRATION_NOT_FOUND, samlResponse.getIssuer()));
  }

  private User createUser(Map<String, String> details, Integration integration) {
    var email = resolveEmail(details, integration);

    User user = new User();
    user.setLogin(email);
    user.setEmail(email);
    user.setUuid(UUID.randomUUID());
    user.setFullName(resolveName(details, integration));
    user.setActive(Boolean.TRUE);
    user.setUserType(UserType.SAML);
    user.setRole(resoleRole(details, integration));
    user.setExpired(false);

    user.setMetadata(defaultMetaData());

    var saved = userRepository.save(user);
    userEventPublisher.publishOnUserCreated(saved);

    return saved;
  }

  private String resolveEmail(Map<String, String> details, Integration integration) {
    var attr = SamlParameter.EMAIL_ATTRIBUTE
        .getParameter(integration)
        .orElse(UserAttribute.EMAIL.toString());

    return Optional.ofNullable(details.get(attr))
        .filter(StringUtils::isNotBlank)
        .map(NORMALIZE_STRING)
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.ATTRIBUTE_NOT_FOUND,
            attr
        ));
  }

  private String resolveName(Map<String, String> details, Integration integration) {
    Optional<String> fullNameAttr = SamlParameter.FULL_NAME_ATTRIBUTE.getParameter(integration);

    if (fullNameAttr.isPresent()) {
      return details.get(fullNameAttr.get());
    }

    var firstNameAttr = SamlParameter.FIRST_NAME_ATTRIBUTE.getParameter(integration)
        .orElse(UserAttribute.FIRST_NAME.toString());
    var lastNameAttr = SamlParameter.LAST_NAME_ATTRIBUTE.getParameter(integration)
        .orElse(UserAttribute.LAST_NAME.toString());

    return String.join(" ", details.get(firstNameAttr), details.get(lastNameAttr));
  }

  private UserRole resoleRole(Map<String, String> details, Integration integration) {
    var attr = SamlParameter.ROLES_ATTRIBUTE
        .getParameter(integration)
        .orElse(UserAttribute.ROLES.toString());

    return Optional.ofNullable(details.get(attr))
        .filter(role -> role.toLowerCase().contains("admin"))
        .map(role -> UserRole.ADMINISTRATOR)
        .orElse(UserRole.USER);
  }
}
