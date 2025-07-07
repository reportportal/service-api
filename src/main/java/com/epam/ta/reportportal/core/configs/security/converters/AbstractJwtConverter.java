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
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Abstract class for converting JWT tokens to Spring Security authentication tokens.
 * This class provides common functionality for extracting user details and authorities
 * from JWT tokens, and can be extended to implement specific JWT conversion logic.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
public abstract class AbstractJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  protected final UserDetailsService userDetailsService;

  protected JwtIssuerConfig config;

  protected Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter;

  protected AbstractJwtConverter(UserDetailsService userDetailsService) {
    this(userDetailsService, new JwtIssuerConfig());
  }

  protected AbstractJwtConverter(
      UserDetailsService userDetailsService,
      JwtIssuerConfig config
  ) {
    this.userDetailsService = userDetailsService;
    this.config = config;
    var jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(config.getAuthoritiesClaim());
    jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
    this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
  }

  protected UserDetails findUser(String identifier) {
    try {
      return userDetailsService.loadUserByUsername(identifier);
    } catch (UsernameNotFoundException e) {
      throw new UsernameNotFoundException("User not found: " + identifier, e);
    }
  }

  protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    return this.jwtGrantedAuthoritiesConverter.convert(jwt);
  }
}