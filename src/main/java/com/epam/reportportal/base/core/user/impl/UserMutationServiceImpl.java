/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.core.user.impl;

import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.MAX_USER_NAME_LENGTH;
import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.MIN_USER_NAME_LENGTH;
import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.USER_FULL_NAME_REGEXP;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.USER_ALREADY_EXISTS;
import static com.epam.reportportal.base.util.email.EmailRulesValidator.NORMALIZE_EMAIL;

import com.epam.reportportal.base.core.events.domain.ChangeUserTypeEvent;
import com.epam.reportportal.base.core.user.UserMutationService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectUtils;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link UserMutationService} providing validated user field mutations.
 */
@Service
@RequiredArgsConstructor
public class UserMutationServiceImpl implements UserMutationService {

  private static final Pattern FULL_NAME_PATTERN = Pattern.compile(USER_FULL_NAME_REGEXP);
  private static final Set<UserType> ALLOWED_ACCOUNT_TYPES = Set.of(UserType.INTERNAL, UserType.SCIM);

  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public void updateEmail(User user, String rawEmail, ReportPortalUser editor) {
    if (!UserRole.ADMINISTRATOR.equals(editor.getUserRole())) {
      expect(user.getUserType() == UserType.INTERNAL, Boolean.TRUE::equals)
          .verify(ACCESS_DENIED, "Unable to change email for external user");
    }

    var email = normalizeAndValidateEmail(rawEmail);
    if (email.equals(user.getEmail())) {
      return;
    }

    checkEmailUniqueness(email);

    List<Project> userProjects = projectRepository.findUserProjects(user.getLogin());
    userProjects.forEach(
        project -> ProjectUtils.updateProjectRecipients(user.getEmail(), email, project));

    user.setEmail(email);
    user.setLogin(email);

    try {
      projectRepository.saveAll(userProjects);
    } catch (Exception exp) {
      throw new ReportPortalException("PROJECT update exception while USER editing.", exp);
    }
  }

  @Override
  public void updateFullName(User user, String fullName, ReportPortalUser editor) {
    if (!UserRole.ADMINISTRATOR.equals(editor.getUserRole())) {
      expect(user.getUserType() == UserType.INTERNAL, Boolean.TRUE::equals)
          .verify(ACCESS_DENIED, "Unable to change full name for external user");
    }

    expect(StringUtils.isNotBlank(fullName), Boolean.TRUE::equals)
        .verify(BAD_REQUEST_ERROR, "Full name must not be empty.");

    expect(fullName.length() >= MIN_USER_NAME_LENGTH && fullName.length() <= MAX_USER_NAME_LENGTH,
        Boolean.TRUE::equals)
        .verify(BAD_REQUEST_ERROR,
            "Full name length must be between " + MIN_USER_NAME_LENGTH + " and " + MAX_USER_NAME_LENGTH
                + " characters.");

    expect(FULL_NAME_PATTERN.matcher(fullName).matches(), Boolean.TRUE::equals)
        .verify(BAD_REQUEST_ERROR,
            "Full name may only contain letters, digits, spaces, dots, apostrophes, hyphens and underscores.");

    user.setFullName(fullName);
  }

  @Override
  public void updateInstanceRole(User user, String role, ReportPortalUser editor) {
    expect(StringUtils.isNotBlank(role), Boolean.TRUE::equals)
        .verify(BAD_REQUEST_ERROR, "Instance role must not be empty.");

    UserRole newRole = UserRole.findByName(role)
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, "Incorrect specified Account Role parameter."));

    eventPublisher.publishEvent(
        new ChangeUserTypeEvent(user.getId(), user.getLogin(), user.getRole(), newRole,
            editor.getUserId(), editor.getUsername()));

    user.setRole(newRole);
  }

  @Override
  public void updateActive(User user, Object value) {
    expect(value != null, Boolean.TRUE::equals)
        .verify(BAD_REQUEST_ERROR, "Active status must not be null.");

    expect(value instanceof Boolean, Boolean.TRUE::equals)
        .verify(BAD_REQUEST_ERROR, "Active status must be a boolean value.");

    user.setActive((Boolean) value);
  }

  @Override
  public void updateAccountType(User user, String accountType) {
    expect(StringUtils.isNotBlank(accountType), Boolean.TRUE::equals)
        .verify(BAD_REQUEST_ERROR, "Account type must not be empty.");

    UserType type = UserType.findByName(accountType)
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            "Incorrect specified Account Type parameter."));

    expect(ALLOWED_ACCOUNT_TYPES.contains(type), Boolean.TRUE::equals)
        .verify(BAD_REQUEST_ERROR, "Account type can only be set to INTERNAL or SCIM.");

    user.setUserType(type);
  }

  @Override
  public void updateExternalId(User user, String externalId) {
    expect(StringUtils.isNotBlank(externalId), Boolean.TRUE::equals)
        .verify(BAD_REQUEST_ERROR, "External ID must not be empty.");

    Long excludeUserId = user.getId();
    userRepository.findByExternalId(externalId)
        .filter(found -> !found.getId().equals(excludeUserId))
        .ifPresent(existing -> {
          throw new ReportPortalException(USER_ALREADY_EXISTS, externalId);
        });

    user.setExternalId(externalId);
  }

  @Override
  public String normalizeAndValidateEmail(String rawEmail) {
    return NORMALIZE_EMAIL.apply(rawEmail);
  }

  @Override
  public void checkEmailUniqueness(String email) {
    userRepository.findByEmail(email).ifPresent(existing -> {
      throw new ReportPortalException(USER_ALREADY_EXISTS, email);
    });
  }

}
