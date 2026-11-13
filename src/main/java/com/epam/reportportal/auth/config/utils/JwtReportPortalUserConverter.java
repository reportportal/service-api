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
package com.epam.reportportal.auth.config.utils;

import com.epam.reportportal.base.core.auth.TokenBlacklistService;
import java.util.Collection;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Converts a validated JWT into a Spring Security
 * {@link org.springframework.security.authentication.AbstractAuthenticationToken} backed by a
 * {@link org.springframework.security.core.userdetails.UserDetails} loaded from the application.
 *
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class JwtReportPortalUserConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String PRINCIPAL_CLAIM_NAME = "user_name";
  private final UserDetailsService userDetailsService;
  private final TokenBlacklistService tokenBlacklistService;
  private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

  public JwtReportPortalUserConverter(UserDetailsService userDetailsService,
      TokenBlacklistService tokenBlacklistService) {
    this.userDetailsService = userDetailsService;
    this.tokenBlacklistService = tokenBlacklistService;

    this.jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    this.jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
    this.jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
  }

  @Override
  public final AbstractAuthenticationToken convert(Jwt jwt) {
    if (tokenBlacklistService.isRevoked(jwt.getId(), jwt.getSubject(), jwt.getIssuedAt())) {
      throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "Token has been revoked", null));
    }

    Collection<GrantedAuthority> authorities = this.jwtGrantedAuthoritiesConverter.convert(jwt);
    String username = jwt.getClaimAsString(PRINCIPAL_CLAIM_NAME);
    String upstreamToken = jwt.getClaimAsString("upstream_token");
    var principal = userDetailsService.loadUserByUsername(username);

    var token = new UsernamePasswordAuthenticationToken(principal, null, authorities);
    token.setDetails(upstreamToken);
    return token;
  }
}
