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

package com.epam.reportportal.core.user.patch;

import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.core.user.UserService;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.util.SecurityContextUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PatchUserHandler {

  private static final String EMAIL_FIELD = "email";
  private static final String FULL_NAME_FIELD = "full_name";
  public static final String ROLE_FIELD = "role";
  public static final String ACTIVE_FIELD = "active";
  public static final String ACCOUNT_TYPE_FIELD = "account_type";
  public static final String EXTERNAL_ID_FIELD = "external_id";
  private static final String UNEXPECTED_PATH_MESSAGE = "Unexpected path: '%s'";

  private final UserService userService;

  public void patchUser(Long userId, List<PatchOperation> patchOperations) {
    final User user = userService.findById(userId);

    patchOperations.forEach(operation -> {
      log.debug("Patch operation: {}", operation);
      validateOperation(user, operation);
      handlePatchOperation(operation, user);
    });
  }

  private void validateOperation(User user, PatchOperation operation) {
    boolean isOwnProfile = SecurityContextUtils.getPrincipal().getUserId().equals(user.getId());
    boolean isAdmin = SecurityContextUtils.isAdminRole();

    // 1. General authorization check: Admins can edit anyone.
    // Regular users can only edit their own profile if it's an INTERNAL account.
    boolean isAuthorized = isAdmin || (user.getUserType() == UserType.INTERNAL && isOwnProfile);
    if (!isAuthorized) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED, "You are not allowed to update this user's profile.");
    }

    String path = operation.getPath();
    Assert.isTrue(StringUtils.isNotEmpty(path), "The 'path' must not be null");

    // 2. Specific field-level restrictions for authorized users.

    // Rule: Email/full_name of UPSA users cannot be changed by anyone.
    if (user.getUserType() == UserType.UPSA && (path.equals(EMAIL_FIELD) || path.equals(FULL_NAME_FIELD))) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED, "Email and full name of UPSA users cannot be updated.");
    }

    // Rule: Profile owners (even if they are admins) can only update their own email and full_name.
    if (isOwnProfile && !path.equals(EMAIL_FIELD) && !path.equals(FULL_NAME_FIELD)) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED,
          "You can only update your own email and full name. Other fields can only be changed by an administrator for you.");
    }
  }


  private void handlePatchOperation(PatchOperation operation, User user) {
    String path = operation.getPath();
    switch (operation.getOp()) {
      case REPLACE -> {
        switch (path) {
          case EMAIL_FIELD -> user.setEmail((String) operation.getValue());
          case FULL_NAME_FIELD -> user.setFullName((String) operation.getValue());
          case ROLE_FIELD -> user.setRole(UserRole.valueOf((String) operation.getValue()));
          case ACTIVE_FIELD -> user.setActive((Boolean) operation.getValue());
          case ACCOUNT_TYPE_FIELD -> user.setUserType(UserType.valueOf((String) operation.getValue()));
          case EXTERNAL_ID_FIELD -> user.setExternalId((String) operation.getValue());
          case null, default ->
              throw new IllegalArgumentException(UNEXPECTED_PATH_MESSAGE.formatted(operation.getPath()));
        }
      }
      case ADD -> {
        switch (path) {
          case EXTERNAL_ID_FIELD -> user.setExternalId((String) operation.getValue());
          case null, default ->
              throw new IllegalArgumentException(UNEXPECTED_PATH_MESSAGE.formatted(operation.getPath()));
        }
      }
      case REMOVE -> {
        switch (path) {
          case EXTERNAL_ID_FIELD -> user.setExternalId(null);
          case null, default ->
              throw new IllegalArgumentException(UNEXPECTED_PATH_MESSAGE.formatted(operation.getPath()));
        }
      }
      case null, default -> throw new IllegalArgumentException("Unexpected operation: " + operation.getOp());
    }
  }
}
