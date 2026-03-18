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
import com.epam.reportportal.base.core.user.UserMutationService;
import com.epam.reportportal.base.core.user.UserService;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.SecurityContextUtils;
import java.util.List;
import java.util.Set;
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
 *   <li>Administrators may update email, full_name, role, active, account_type and external_id of
 *       any non-UPSA user.</li>
 *   <li>Regular users (any type except UPSA) may update only their own email and full name.</li>
 *   <li>UPSA users' mutable fields (email, full_name, role, active, account_type, external_id)
 *       cannot be changed by anyone, including administrators.</li>
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
  private static final Set<String> ALLOWED_SELF_UPDATE_PATHS = Set.of(EMAIL_PATH, FULL_NAME_PATH);

  private final UserService userService;
  private final UserMutationService userMutationService;

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
   * <p>Authorization rules enforced:
   * <ul>
   *   <li>Administrators can update any field for any user</li>
   *   <li>Regular users can only update their own profile</li>
   *   <li>Regular users are restricted to updating only their email and full name</li>
   * </ul>
   *
   * <p>Additionally delegates to {@link UserMutationService#validateUserUpdatable(User)}
   * for user-type-specific restrictions (e.g., UPSA users).
   *
   * @param user      the target user whose profile is being modified
   * @param operation the patch operation containing the field path and new value
   * @throws ReportPortalException    with {@link ErrorType#ACCESS_DENIED} if the operation is not permitted
   * @throws IllegalArgumentException if the operation path is null or empty
   */
  private void validateOperation(User user, PatchOperation operation) {
    String path = operation.getPath();
    Assert.isTrue(StringUtils.isNotEmpty(path), "The 'path' must not be null");

    userMutationService.validateUserUpdatable(user);

    boolean isAdmin = SecurityContextUtils.isAdminRole();
    if (isAdmin) {
      return;
    }

    boolean isOwnProfile = SecurityContextUtils.getPrincipal().getUserId().equals(user.getId());

    if (!isOwnProfile) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED, "You are not allowed to update this user's profile.");
    }

    if (!ALLOWED_SELF_UPDATE_PATHS.contains(path)) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED, "You can only update your own email and full name.");
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
    var principal = SecurityContextUtils.getPrincipal();

    switch (operation.getOp()) {
      case REPLACE -> {
        switch (path) {
          case EMAIL_PATH ->
              userMutationService.updateEmail(user, validateAndGetTypedValue(operation, path, String.class), principal);
          case FULL_NAME_PATH ->
              userMutationService.updateFullName(user, validateAndGetTypedValue(operation, path, String.class),
                  principal);
          case ROLE_PATH ->
              userMutationService.updateInstanceRole(user, validateAndGetTypedValue(operation, path, String.class),
                  principal);
          case ACTIVE_PATH ->
              userMutationService.updateActive(user, validateAndGetTypedValue(operation, path, Boolean.class));
          case ACCOUNT_TYPE_PATH ->
              userMutationService.updateAccountType(user, validateAndGetTypedValue(operation, path, String.class));
          case EXTERNAL_ID_PATH ->
              userMutationService.updateExternalId(user, validateAndGetTypedValue(operation, path, String.class));
          case null, default -> throw new IllegalArgumentException(UNEXPECTED_PATH_MESSAGE.formatted(path));
        }
      }
      case ADD -> {
        switch (path) {
          case EXTERNAL_ID_PATH ->
              userMutationService.updateExternalId(user, validateAndGetTypedValue(operation, path, String.class));
          case null, default -> throw new IllegalArgumentException(UNEXPECTED_PATH_MESSAGE.formatted(path));
        }
      }
      case REMOVE -> {
        switch (path) {
          case EXTERNAL_ID_PATH -> user.setExternalId(null);
          case null, default -> throw new IllegalArgumentException(UNEXPECTED_PATH_MESSAGE.formatted(path));
        }
      }
      case null, default -> throw new IllegalArgumentException("Unexpected operation: " + operation.getOp());
    }
  }

  private <T> T validateAndGetTypedValue(PatchOperation operation, String path, Class<T> expectedType) {
    Object value = operation.getValue();

    if (expectedType.isInstance(value)) {
      return expectedType.cast(value);
    }

    throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
        "Invalid type for path '%s': expected %s".formatted(path, expectedType.getSimpleName())
    );
  }
}
