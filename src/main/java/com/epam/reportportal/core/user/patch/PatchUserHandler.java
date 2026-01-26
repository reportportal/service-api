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

  public static final String OPERATION_IS_NOT_SUPPORTED = "'%s' operation is not supported";

  private final UserService userService;

  public void patchUser(Long userId, List<PatchOperation> patchOperations) {
    final User user = userService.findById(userId);

    patchOperations.forEach(operation -> {
      log.debug("Patch operation: {}", operation);
      validateOperation(user, operation);
      patchUser(operation, user);
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
    if (user.getUserType() == UserType.UPSA && (path.equals("email") || path.equals("full_name"))) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED, "Email and full name of UPSA users cannot be updated.");
    }

    // Rule: Profile owners (even if they are admins) can only update their own email and full_name.
    if (isOwnProfile && !path.equals("email") && !path.equals("full_name")) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED,
          "You can only update your own email and full name. Other fields can only be changed by an administrator for you.");
    }
  }


  private void patchUser(PatchOperation operation, User user) {
    String path = operation.getPath();
    switch (operation.getOp()) {
      case REPLACE -> {
        switch (path) {
          case "email" -> user.setEmail((String) operation.getValue());
          case "full_name" -> user.setFullName((String) operation.getValue());
          case "role" -> user.setRole(UserRole.valueOf((String) operation.getValue()));
          case "active" -> user.setActive((Boolean) operation.getValue());
          case "account_type" -> user.setUserType(UserType.valueOf((String) operation.getValue()));
          case "external_id" -> user.setExternalId((String) operation.getValue());
          case null, default ->
              throw new IllegalArgumentException("Unexpected path: '%s'".formatted(operation.getPath()));
        }
      }
      case ADD -> {
        switch (path) {
          case "external_id" -> user.setExternalId((String) operation.getValue());
          case null, default ->
              throw new IllegalArgumentException("Unexpected path: '%s'".formatted(operation.getPath()));
        }
      }
      case REMOVE -> {
        switch (path) {
          case "external_id" -> user.setExternalId(null);
          case null, default ->
              throw new IllegalArgumentException("Unexpected path: '%s'".formatted(operation.getPath()));
        }
      }
      case null, default -> throw new IllegalArgumentException("Unexpected operation: " + operation.getOp());
    }
  }
}
