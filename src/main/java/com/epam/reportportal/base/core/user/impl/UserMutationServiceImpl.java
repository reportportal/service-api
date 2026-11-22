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
import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.equalTo;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.INCORRECT_REQUEST;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.USER_ALREADY_EXISTS;
import static com.epam.reportportal.base.util.email.EmailRulesValidator.NORMALIZE_EMAIL;

import com.epam.reportportal.base.core.auth.TokenBlacklistService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link UserMutationService} providing validated user field mutations.
 */
@Service
@RequiredArgsConstructor
public class UserMutationServiceImpl implements UserMutationService {

  private static final Pattern FULL_NAME_PATTERN = Pattern.compile(USER_FULL_NAME_REGEXP);
  private static final Set<UserType> ALLOWED_ACCOUNT_TYPES = Set.of(UserType.INTERNAL, UserType.SCIM);
  static final String EMAIL_CHANGE_LINK_INVALID = "Email change link is invalid or expired.";

  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final TokenBlacklistService tokenBlacklistService;
  private final PasswordEncoder passwordEncoder;
  private final EmailChangeConfirmationService emailChangeConfirmationService;

  @Override
  public void updateEmail(User user, String rawEmail, ReportPortalUser editor) {
    var email = normalizeAndValidateEmail(rawEmail);
    if (email.equals(user.getEmail())) {
      return;
    }

    verifyEmailChangeAllowed(user, editor);
    checkEmailUniqueness(email);

    if (isSelfService(user, editor)) {
      // The owner has to prove they control the new address: the change is applied by confirmEmailChange.
      emailChangeConfirmationService.requestChange(user, email);
      return;
    }

    applyEmail(user, email);
  }

  @Override
  public User confirmEmailChange(String uuid) {
    var pending = emailChangeConfirmationService.findPending(uuid)
        .orElseThrow(() -> new ReportPortalException(INCORRECT_REQUEST, EMAIL_CHANGE_LINK_INVALID));

    var user = userRepository.findById(pending.userId())
        .orElseThrow(() -> new ReportPortalException(INCORRECT_REQUEST, EMAIL_CHANGE_LINK_INVALID));

    // The account's email was changed some other way (e.g. by an administrator) after the link was issued.
    expect(pending.currentEmail(), equalTo(user.getEmail()))
        .verify(INCORRECT_REQUEST, EMAIL_CHANGE_LINK_INVALID);

    expect(Boolean.TRUE.equals(user.getActive()) && user.getUserType() == UserType.INTERNAL, Boolean.TRUE::equals)
        .verify(ACCESS_DENIED, "Email of this account cannot be changed.");

    var email = normalizeAndValidateEmail(pending.newEmail());
    checkEmailUniqueness(email);

    applyEmail(user, email);
    emailChangeConfirmationService.completeChange(user.getId());
    return user;
  }

  private void applyEmail(User user, String email) {
    // Revoke while the user still carries the old login: issued JWTs use it as their subject.
    tokenBlacklistService.revokeUserTokens(user);

    List<Project> userProjects = projectRepository.findUserProjects(user.getLogin());
    userProjects.forEach(
        project -> ProjectUtils.updateProjectRecipients(user.getEmail(), email, project));

    // Dashboards, widgets and filters are owned by login string and lock checks compare it with the caller's login,
    // so ownership moves together with the login, in this transaction.
    userRepository.replaceOwnerLogin(user.getLogin(), email);

    user.setEmail(email);
    user.setLogin(email);

    try {
      projectRepository.saveAll(userProjects);
    } catch (Exception exp) {
      throw new ReportPortalException("PROJECT update exception while USER editing.", exp);
    }
  }

  private static boolean isSelfService(User user, ReportPortalUser editor) {
    return editor == null || editor.getUserId() == null || editor.getUserId().equals(user.getId());
  }

  /**
   * Email equals login, so changing it is an account-recovery-sensitive operation. It is allowed only from an
   * interactive (JWT) session, and a user may change their own email only when the account is managed by ReportPortal
   * itself ({@link UserType#INTERNAL}); accounts provisioned by
   * SAML, LDAP, SCIM, UPSA and other identity providers get their email from that provider. Administrators may still
   * change the email of other users of any type.
   */
  private void verifyEmailChangeAllowed(User user, ReportPortalUser editor) {
    expect(isInteractiveSession(), Boolean.TRUE::equals)
        .verify(ACCESS_DENIED, "Email cannot be changed with an API key. Sign in to change it.");

    if (isSelfService(user, editor)) {
      expect(user.getUserType(), UserType.INTERNAL::equals)
          .verify(ACCESS_DENIED, "Email of an externally managed account cannot be changed by its owner.");
    }
  }

  /**
   * API keys authenticate as {@code UsernamePasswordAuthenticationToken} (see
   * {@code ApiKeyReportPortalUserConverter}); both ReportPortal and external IdP access tokens authenticate as
   * {@link JwtAuthenticationToken}. Anything other than a JWT is treated as non-interactive.
   */
  private static boolean isInteractiveSession() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication instanceof JwtAuthenticationToken;
  }

  @Override
  public void updateFullName(User user, String fullName, ReportPortalUser editor) {
    expect(fullName, StringUtils::isNotBlank)
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

    UserRole oldRole = user.getRole();
    if (oldRole == newRole) {
      return;
    }

    eventPublisher.publishEvent(
        new ChangeUserTypeEvent(user.getId(), user.getLogin(), oldRole, newRole,
            editor.getUserId(), editor.getUsername()));

    user.setRole(newRole);
    tokenBlacklistService.revokeUserTokens(user);
  }

  @Override
  public void updatePassword(User user, String rawPassword) {
    user.setPassword(passwordEncoder.encode(rawPassword));
    tokenBlacklistService.revokeUserTokens(user);
  }

  @Override
  public void updateActive(User user, Object value) {
    expect(value != null, Boolean.TRUE::equals)
        .verify(BAD_REQUEST_ERROR, "Active status must not be null.");

    expect(value instanceof Boolean, Boolean.TRUE::equals)
        .verify(BAD_REQUEST_ERROR, "Active status must be a boolean value.");

    boolean active = Boolean.TRUE.equals(value);
    user.setActive(active);

    if (!active) {
      tokenBlacklistService.revokeUserTokens(user);
    }
  }

  @Override
  public void updateAccountType(User user, String accountType, boolean restrictAllowedTypes) {
    expect(StringUtils.isNotBlank(accountType), Boolean.TRUE::equals)
        .verify(BAD_REQUEST_ERROR, "Account type must not be empty.");

    UserType type = UserType.findByName(accountType)
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            "Incorrect specified Account Type parameter."));

    if (restrictAllowedTypes) {
      expect(ALLOWED_ACCOUNT_TYPES.contains(type), Boolean.TRUE::equals)
          .verify(BAD_REQUEST_ERROR, "Account type can only be set to INTERNAL or SCIM.");
    }

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
