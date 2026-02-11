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
package com.epam.reportportal.auth.integration.github;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import java.util.Collections;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class RPOAuth2User extends DefaultOAuth2User {

  private final ReportPortalUser reportPortalUser;
  private final String accessToken;

  public RPOAuth2User(ReportPortalUser reportPortalUser, String accessToken) {
    super(reportPortalUser.getAuthorities(),
        Collections.singletonMap("login", reportPortalUser.getUsername()),
        "login");
    this.reportPortalUser = reportPortalUser;
    this.accessToken = accessToken;
  }

  public ReportPortalUser getReportPortalUser() {
    return reportPortalUser;
  }

  public String getAccessToken() {
    return accessToken;
  }
}
