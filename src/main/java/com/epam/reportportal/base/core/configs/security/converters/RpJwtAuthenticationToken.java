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

package com.epam.reportportal.base.core.configs.security.converters;

import java.util.Collection;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * JWT-based {@link org.springframework.security.core.Authentication} that exposes the resolved {@link UserDetails} as
 * the principal while preserving the underlying {@link Jwt} via {@link #getToken()}.
 */
public class RpJwtAuthenticationToken extends JwtAuthenticationToken {

  private final UserDetails userDetails;

  /**
   * Creates a new authentication token for a successfully validated JWT.
   *
   * @param jwt         decoded and validated JWT bearer token
   * @param userDetails resolved {@link UserDetails} for the JWT subject; exposed as the principal
   * @param authorities granted authorities for the authenticated user
   */
  public RpJwtAuthenticationToken(Jwt jwt, UserDetails userDetails,
      Collection<? extends GrantedAuthority> authorities) {
    super(jwt, authorities, userDetails.getUsername());
    this.userDetails = userDetails;
  }

  @Override
  public Object getPrincipal() {
    return userDetails;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RpJwtAuthenticationToken that)) {
      return false;
    }
    return Objects.equals(getToken().getTokenValue(), that.getToken().getTokenValue())
        && Objects.equals(userDetails, that.userDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getToken().getTokenValue(), userDetails);
  }
}
