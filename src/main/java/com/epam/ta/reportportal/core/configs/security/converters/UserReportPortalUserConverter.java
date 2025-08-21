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

package com.epam.ta.reportportal.core.configs.security.converters;

import com.epam.ta.reportportal.auth.util.AuthUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.user.UserAuthProjection;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.google.common.collect.Maps;
import java.util.function.Function;

/**
 * Converts {@link UserAuthProjection} to {@link ReportPortalUser}.<br>
 * It ensures that all necessary fields are populated correctly.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
public final class UserReportPortalUserConverter {

  private UserReportPortalUserConverter() {
    // static only
  }

  /**
   * Converts a {@link UserAuthProjection} object to a {@link ReportPortalUser} object.
   */
  public static Function<UserAuthProjection, ReportPortalUser> TO_REPORT_PORTAL_USER = user -> {
    var role = UserRole.valueOf(user.role());
    return ReportPortalUser.userBuilder()
        .withUserId(user.id())
        .withUserName(user.login())
        .withEmail(user.email())
        .withUserRole(role)
        .withPassword(user.password() == null ? "" : user.password())
        .withAuthorities(AuthUtils.AS_AUTHORITIES.apply(role))
        .withProjectDetails(Maps.newHashMapWithExpectedSize(1))
        .withActive(user.active())
        .build();
  };
}