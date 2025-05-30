/*
 * Copyright 2025 EPAM Systems
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

package com.epam.ta.reportportal.util;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for accessing user information from the Spring Security context.
 *
 * @author <a href="mailto:Siarhei_Hrabko@epam.com">Siarhei Hrabko</a>
 */
public class SecurityContextUtils {

  private SecurityContextUtils() {
  }

  /**
   * Fetches the details of the currently authenticated user from the Spring Security context.
   *
   * @return the {@link ReportPortalUser} representing the currently logged-in user.
   * @throws ClassCastException if the principal in the {@link SecurityContextHolder}'s context is not a {@link ReportPortalUser}
   */
  public static ReportPortalUser getPrincipal() {
    return (ReportPortalUser) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }
}
