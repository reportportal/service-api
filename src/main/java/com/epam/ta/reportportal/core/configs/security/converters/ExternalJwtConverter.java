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

import com.epam.ta.reportportal.core.configs.security.JwtIssuerConfig;
import java.util.Collection;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Converter for external JWT tokens that retrieves user details and authorities
 * based on a specific claim (e.g., "sub" or "externalId") from the JWT.
 * This class extends {@link AbstractJwtConverter} to provide custom conversion logic.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
public class ExternalJwtConverter extends AbstractJwtConverter {

  /**
   * Constructs an ExternalJwtConverter with the specified
   * UserDetailsService and JWT issuer configuration.
   */
  public ExternalJwtConverter(UserDetailsService userDetailsService, JwtIssuerConfig config) {
    super(userDetailsService, config);
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    var externalId = jwt.getClaimAsString(config.getUsernameClaim());
    if (StringUtils.isBlank(externalId)) {
      throw new IllegalArgumentException("Username claim is missing or null");
    }
    var user = findUser(externalId);
    var authorities = Optional.ofNullable(extractAuthorities(jwt))
        .filter(auths -> !auths.isEmpty())
        .orElseGet(() -> (Collection<GrantedAuthority>) user.getAuthorities());

    return new UsernamePasswordAuthenticationToken(user, null, authorities);
  }
}
