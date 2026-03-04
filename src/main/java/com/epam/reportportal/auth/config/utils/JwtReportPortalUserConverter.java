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

import java.util.Collection;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class JwtReportPortalUserConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private final UserDetailsService userDetailsService;

  private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

  private final static String PRINCIPAL_CLAIM_NAME = "user_name";

  public JwtReportPortalUserConverter(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;

    this.jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    this.jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
    this.jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
  }

  @Override
  public final AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = this.jwtGrantedAuthoritiesConverter.convert(jwt);

    String username = jwt.getClaimAsString(PRINCIPAL_CLAIM_NAME);
    String upstreamToken = jwt.getClaimAsString("upstream_token");
    var principal = userDetailsService.loadUserByUsername(username);

    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
        principal, null, authorities);
    usernamePasswordAuthenticationToken.setDetails(upstreamToken);

    return usernamePasswordAuthenticationToken;
  }
}
