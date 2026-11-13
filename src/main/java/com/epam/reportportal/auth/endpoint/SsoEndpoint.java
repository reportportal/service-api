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

package com.epam.reportportal.auth.endpoint;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Base SSO controller.
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@RestController
@Transactional
@Tag(name = "sso-endpoint", description = "Sso Endpoint")
public class SsoEndpoint {

  @RequestMapping(value = {"/sso/me", "/sso/user"}, method = {GET, POST})
  @Operation(summary = "Get user details")
  public Map<String, Object> user(Authentication user) {

    ImmutableMap.Builder<String, Object> details = ImmutableMap.<String, Object>builder()
        .put("user", user.getName())
        .put("authorities", user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList());

    if (user.getPrincipal() instanceof ReportPortalUser reportPortalUser) {
      details.put("userId", reportPortalUser.getUserId());
      details.put("organizations", reportPortalUser.getOrganizationDetails());
    }
    return details.build();
  }
}
