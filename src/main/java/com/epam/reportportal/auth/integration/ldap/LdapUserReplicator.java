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

package com.epam.reportportal.auth.integration.ldap;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.auth.event.UserEventPublisher;
import com.epam.reportportal.auth.integration.AbstractUserReplicator;
import com.epam.reportportal.auth.integration.parameter.LdapParameter;
import com.epam.reportportal.auth.oauth.UserSynchronizationException;
import com.epam.reportportal.base.infrastructure.commons.ContentTypeResolver;
import com.epam.reportportal.base.infrastructure.persistence.binary.UserBinaryDataService;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.base.infrastructure.persistence.util.PersonalProjectService;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * LDAP replicator.
 *
 * @author Andrei Varabyeu
 */
@Component
public class LdapUserReplicator extends AbstractUserReplicator {

  private static final String USER_ALREADY_EXISTS_MSG = "User with login '%s' already exists";
  private static final String EMAIL_ATTRIBUTE_NOT_PROVIDED_MSG = "Email attribute not provided";
  private final UserEventPublisher userEventPublisher;

  @Autowired
  public LdapUserReplicator(UserRepository userRepository, ProjectRepository projectRepository,
      PersonalProjectService personalProjectService, UserBinaryDataService userBinaryDataService,
      ContentTypeResolver contentTypeResolver, UserEventPublisher userEventPublisher) {
    super(userRepository, projectRepository, personalProjectService, userBinaryDataService,
        contentTypeResolver);
    this.userEventPublisher = userEventPublisher;
  }

  /**
   * Replicates LDAP user to internal database (if does NOT exist). Creates personal project for that user
   *
   * @param ctx       LDAP context
   * @param syncAttrs Synchronization Attributes
   * @return Internal User representation
   */
  @Transactional
  public User replicateUser(DirContextOperations ctx, Map<String, String> syncAttrs) {
    String emailAttribute = ofNullable(
        syncAttrs.get(LdapParameter.EMAIL_ATTRIBUTE.getParameterName()))
        .orElseThrow(() -> new UserSynchronizationException(EMAIL_ATTRIBUTE_NOT_PROVIDED_MSG));

    String emailFromContext = (String) ctx.getObjectAttribute(emailAttribute);
    String email = validateEmail(emailFromContext);

    Optional<User> userOptional = userRepository.findByEmail(email);

    if (userOptional.isEmpty()) {
      return createNewUser(ctx, syncAttrs, email);
    }

    User user = userOptional.get();
    checkUserType(user);
    updateEmailIfNeeded(email, user);

    return user;
  }


  private User createNewUser(
      DirContextOperations ctx,
      Map<String, String> syncAttributes,
      String email
  ) {

    User user = new User();
    user.setLogin(email);
    user.setEmail(email);
    user.setUuid(UUID.randomUUID());
    user.setActive(Boolean.TRUE);

    String fullName = getFullName(ctx, syncAttributes);
    user.setFullName(fullName);
    user.setMetadata(defaultMetaData());
    user.setUserType(UserType.LDAP);
    user.setRole(UserRole.USER);
    user.setExpired(false);

    var saved = userRepository.save(user);
    userEventPublisher.publishOnUserCreated(saved);
    return saved;
  }

  private String getFullName(DirContextOperations ctx, Map<String, String> syncAttributes) {

    Optional<String> fullName = getAttribute(ctx, syncAttributes,
        LdapParameter.FULL_NAME_ATTRIBUTE);
    if (fullName.isPresent()) {
      return fullName.get();
    }

    String res =
        getAttribute(ctx, syncAttributes, LdapParameter.FIRST_NAME_ATTRIBUTE).orElse("") + " "
            + getAttribute(ctx, syncAttributes, LdapParameter.LAST_NAME_ATTRIBUTE).orElse("");

    return res.trim();
  }

  private void checkUserType(User user) {
    if (!UserType.LDAP.equals(user.getUserType())) {
      String login = user.getLogin();
      throw new UserSynchronizationException(String.format(USER_ALREADY_EXISTS_MSG, login));
    }
  }

  private void updateEmailIfNeeded(String email, User user) {
    if (!StringUtils.equals(user.getEmail(), email)) {
      user.setEmail(email);
      userRepository.save(user);
    }
  }

  private Optional<String> getAttribute(DirContextOperations ctx,
      Map<String, String> syncAttributes, LdapParameter parameter) {
    return ofNullable(syncAttributes.get(parameter.getParameterName()))
        .filter(StringUtils::isNotBlank)
        .flatMap(it -> ofNullable(ctx.getStringAttribute(it)));
  }
}
