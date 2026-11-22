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

package com.epam.reportportal.base.core.user;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;

/**
 * Service for mutating user entity attributes with validation and authorization checks.
 */
public interface UserMutationService {

  /**
   * Updates user email (and login) with validation and duplicate checking. Rejected when the current request is not
   * authenticated by a JWT (for example by an API key) and when a user changes the email of their own non-INTERNAL
   * account.
   *
   * <p>When an administrator changes another user's email, the change is applied at once and all tokens previously
   * issued to the user are revoked. When the owner changes their own email, nothing on the user changes: a pending
   * change is recorded and a confirmation link is mailed to the new address; see {@link #confirmEmailChange}.
   *
   * @param user     the user entity to update
   * @param rawEmail the new email address (will be normalized)
   * @param editor   the user performing the update (used for authorization)
   */
  void updateEmail(User user, String rawEmail, ReportPortalUser editor);

  /**
   * Applies a pending self-service email change identified by the token from the confirmation link: re-checks that the
   * new email is unused, revokes the user's tokens, sets email and login, updates project notification recipients and
   * removes the pending change. Fails with INCORRECT_REQUEST when the token is unknown, used or expired, with
   * USER_ALREADY_EXISTS when the new email was taken meanwhile and with ACCESS_DENIED when the account is no longer
   * active or INTERNAL.
   *
   * @param uuid the confirmation token
   * @return the updated (not yet saved) user
   */
  User confirmEmailChange(String uuid);

  /**
   * Updates user full name with format and length validation.
   *
   * @param user     the user entity to update
   * @param fullName the new full name
   * @param editor   the user performing the update (used for authorization)
   */
  void updateFullName(User user, String fullName, ReportPortalUser editor);

  /**
   * Updates user instance-level role (ADMINISTRATOR or USER).
   *
   * @param user   the user entity to update
   * @param role   the new role as string
   * @param editor the user performing the update (used for audit event)
   */
  void updateInstanceRole(User user, String role, ReportPortalUser editor);

  /**
   * Updates the user's password and revokes all previously issued tokens for the user.
   *
   * @param user        the user entity to update
   * @param rawPassword the new plain-text password (will be encoded)
   */
  void updatePassword(User user, String rawPassword);

  /**
   * Updates user active status.
   *
   * @param user  the user entity to update
   * @param value the active status as boolean
   */
  void updateActive(User user, Object value);

  /**
   * Updates user account type.
   *
   * @param user                 the user entity to update
   * @param accountType          the new account type as string
   * @param restrictAllowedTypes when {@code true}, only INTERNAL or SCIM are allowed
   */
  void updateAccountType(User user, String accountType, boolean restrictAllowedTypes);

  /**
   * Updates user external ID with duplicate checking.
   *
   * @param user       the user entity to update
   * @param externalId the new external ID
   */
  void updateExternalId(User user, String externalId);

  /**
   * Normalizes and validates email format.
   *
   * @param rawEmail the email to normalize
   * @return normalized email
   */
  String normalizeAndValidateEmail(String rawEmail);

  /**
   * Checks if email is unique across all users.
   *
   * @param email the email to check
   */
  void checkEmailUniqueness(String email);
}
