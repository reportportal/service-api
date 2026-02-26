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

package com.epam.reportportal.base.core.user.patch;

import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.base.core.user.UserService;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.SecurityContextUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Handler responsible for applying JSON-Patch-style operations to User entities.
 *
 * <p>Validates permissions and applies supported patch operations (replace, add, remove) on
 * whitelisted user fields: email, full_name, role, active, account_type and external_id.
 *
 * <p>Authorization rules:
 * <ul>
 *   <li>Administrators may update any user's fields.</li>
 *   <li>Regular INTERNAL users may update only their own email and full name.</li>
 *   <li>UPSA users' email and full_name cannot be changed by anyone.</li>
 * </ul>
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PatchUserHandler {

  private static final String EMAIL_PATH = "/email";
  private static final String FULL_NAME_PATH = "/full_name";
  public static final String ROLE_PATH = "/instance_role";
  public static final String ACTIVE_PATH = "/active";
  public static final String ACCOUNT_TYPE_PATH = "/account_type";
  public static final String EXTERNAL_ID_PATH = "/external_id";
  private static final String UNEXPECTED_PATH_MESSAGE = "Unexpected path: '%s'";

  private final UserService userService;

  /**
   * Applies a list of patch operations to the user identified by {@code userId}.
   *
   * <p>Each operation is validated for authorization and then applied to the entity. Supported
   * operations are REPLACE, ADD and REMOVE for a restricted set of user fields.
   *
   * @param userId          id of the user to patch
   * @param patchOperations list of patch operations to apply
   * @throws ReportPortalException    when the current principal is not authorized to perform an operation
   * @throws IllegalArgumentException when an unexpected operation or path is encountered
   */
  public void patchUser(Long userId, List<PatchOperation> patchOperations) {
    final User user = userService.findById(userId);

    patchOperations.forEach(operation -> {
      log.debug("Patch operation: {}", operation);
      validateOperation(user, operation);
      handlePatchOperation(operation, user);
    });
  }

  /**
   * Validates whether the current principal is allowed to perform the provided {@code operation} on the given
   * {@code user}.
   *
   * <p>This method enforces general authorization rules (admin vs regular user) and field-level
   * restrictions (e.g. UPSA users' email/full name cannot be changed).
   *
   * @param user      target user to validate against
   * @param operation patch operation to validate
   * @throws ReportPortalException when the operation is not permitted
   */
  private void validateOperation(User user, PatchOperation operation) {
    boolean isOwnProfile = SecurityContextUtils.getPrincipal().getUserId().equals(user.getId());
    boolean isAdmin = SecurityContextUtils.isAdminRole();

    String path = operation.getPath();
    Assert.isTrue(StringUtils.isNotEmpty(path), "The 'path' must not be null");

    checkIfAuthorizedToPatchUser(user, isAdmin, isOwnProfile);
    validateUpsaUserModification(user, path);
    validateOwnerFieldAccess(isOwnProfile, path);
  }

  /**
   * Ensures that when the current principal is updating their own profile only the email and full name fields are
   * allowed to be changed.
   *
   * @param isOwnProfile whether the current principal is the owner of the profile
   * @param path         the JSON-Patch path of the field being modified
   * @throws ReportPortalException with ErrorType.ACCESS_DENIED if a non-permitted field is modified
   */
  private static void validateOwnerFieldAccess(boolean isOwnProfile, String path) {
    if (isOwnProfile && !path.equals(EMAIL_PATH) && !path.equals(FULL_NAME_PATH)) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED,
          "You can only update your own email and full name. Other fields can only be changed by an administrator for you.");
    }
  }

  /**
   * Validates that UPSA users' email and full name cannot be modified.
   *
   * <p>If the target {@code user} has type {@link UserType#UPSA} and the provided {@code path}
   * points to either the {@value #EMAIL_PATH} or {@value #FULL_NAME_PATH}, a {@link ReportPortalException} with
   * {@link ErrorType#ACCESS_DENIED} is thrown.
   *
   * @param user target user to validate
   * @param path JSON-Patch path of the field being modified
   * @throws ReportPortalException when attempting to modify email or full name of a UPSA user
   */
  private static void validateUpsaUserModification(User user, String path) {
    if (user.getUserType() == UserType.UPSA && (path.equals(EMAIL_PATH) || path.equals(FULL_NAME_PATH))) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED, "Email and full name of UPSA users cannot be updated.");
    }
  }

  /**
   * Ensures the current principal is authorized to update the specified user.
   *
   * <p>Authorization rules enforced here:
   * - Administrators are allowed to update any user's profile. - INTERNAL users may update only their own profile.
   * <p>
   * Throws a {@link ReportPortalException} with {@link ErrorType#ACCESS_DENIED} when the principal is not authorized.
   *
   * @param user         target user to update
   * @param isAdmin      whether current principal has an admin role
   * @param isOwnProfile whether current principal is the owner of the profile
   */
  private static void checkIfAuthorizedToPatchUser(User user, boolean isAdmin, boolean isOwnProfile) {
    boolean isAuthorized = isAdmin || (user.getUserType() == UserType.INTERNAL && isOwnProfile);
    if (!isAuthorized) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED, "You are not allowed to update this user's profile.");
    }
  }

  /**
   * Applies a single, already validated {@code operation} to the provided {@code user} instance.
   *
   * <p>Only a predefined set of paths is supported; unexpected paths or operations will result
   * in an {@link IllegalArgumentException}.
   *
   * @param operation patch operation to apply
   * @param user      target user to mutate
   * @throws IllegalArgumentException when an unexpected operation or path is encountered
   */
  private void handlePatchOperation(PatchOperation operation, User user) {
    String path = operation.getPath();
    switch (operation.getOp()) {
      case REPLACE -> {
        switch (path) {
          case EMAIL_PATH -> user.setEmail((String) operation.getValue());
          case FULL_NAME_PATH -> user.setFullName((String) operation.getValue());
          case ROLE_PATH -> user.setRole(UserRole.valueOf((String) operation.getValue()));
          case ACTIVE_PATH -> user.setActive((Boolean) operation.getValue());
          case ACCOUNT_TYPE_PATH -> user.setUserType(UserType.valueOf((String) operation.getValue()));
          case EXTERNAL_ID_PATH -> user.setExternalId((String) operation.getValue());
          case null, default ->
              throw new IllegalArgumentException(UNEXPECTED_PATH_MESSAGE.formatted(operation.getPath()));
        }
      }
      case ADD -> {
        switch (path) {
          case EXTERNAL_ID_PATH -> user.setExternalId((String) operation.getValue());
          case null, default ->
              throw new IllegalArgumentException(UNEXPECTED_PATH_MESSAGE.formatted(operation.getPath()));
        }
      }
      case REMOVE -> {
        switch (path) {
          case EXTERNAL_ID_PATH -> user.setExternalId(null);
          case null, default ->
              throw new IllegalArgumentException(UNEXPECTED_PATH_MESSAGE.formatted(operation.getPath()));
        }
      }
      case null, default -> throw new IllegalArgumentException("Unexpected operation: " + operation.getOp());
    }
  }
}
