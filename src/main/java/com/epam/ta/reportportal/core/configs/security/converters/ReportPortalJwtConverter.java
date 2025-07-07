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

package com.epam.ta.reportportal.core.configs.security.converters;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Converter for JWT tokens to Spring Security authentication tokens.
 * This class extends AbstractJwtConverter to provide a specific implementation
 * for converting JWTs issued by Report Portal.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
public class ReportPortalJwtConverter extends AbstractJwtConverter {

  /**
   * Constructs a ReportPortalJwtConverter with the specified UserDetailsService.
   */
  public ReportPortalJwtConverter(UserDetailsService userDetailsService) {
    super(userDetailsService);
  }

  @Override
  public final AbstractAuthenticationToken convert(Jwt jwt) {
    var username = jwt.getSubject();
    var principal = userDetailsService.loadUserByUsername(username);
    var authorities = extractAuthorities(jwt);

    return new UsernamePasswordAuthenticationToken(principal, null, authorities);
  }
}
