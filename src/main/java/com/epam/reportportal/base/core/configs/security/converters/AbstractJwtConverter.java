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

import com.epam.reportportal.base.core.auth.TokenBlacklistService;
import com.epam.reportportal.base.core.configs.security.JwtIssuer;
import java.util.Collection;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Abstract class for converting JWT tokens to Spring Security authentication tokens. This class provides common
 * functionality for extracting user details and authorities from JWT tokens, and can be extended to implement specific
 * JWT conversion logic.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
public abstract class AbstractJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  protected final UserDetailsService userDetailsService;
  protected final TokenBlacklistService tokenBlacklistService;

  protected JwtIssuer config;

  protected Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter;

  /**
   * Constructs an AbstractJwtConverter with the specified UserDetailsService and default JwtIssuerConfig.
   *
   * @param userDetailsService    The service to load user details.
   * @param tokenBlacklistService The service to for JWT blacklisting
   */
  protected AbstractJwtConverter(UserDetailsService userDetailsService, TokenBlacklistService tokenBlacklistService) {
    this(userDetailsService, tokenBlacklistService, new JwtIssuer());
  }

  /**
   * Constructs an AbstractJwtConverter with the specified UserDetailsService and JwtIssuerConfig.
   *
   * @param userDetailsService    The service to load user details.
   * @param config                The configuration for JWT issuer settings.
   * @param tokenBlacklistService The service to for JWT blacklisting
   */
  protected AbstractJwtConverter(UserDetailsService userDetailsService, TokenBlacklistService tokenBlacklistService,
      JwtIssuer config) {
    this.userDetailsService = userDetailsService;
    this.tokenBlacklistService = tokenBlacklistService;
    this.config = config;
    var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    authoritiesConverter.setAuthoritiesClaimName(config.getAuthoritiesClaim());
    authoritiesConverter.setAuthorityPrefix("");
    this.jwtGrantedAuthoritiesConverter = authoritiesConverter;
  }

  /**
   * Validates the JWT against the revocation blacklist and, if not revoked, delegates to the subclass-specific
   * {@link #doConvert(Jwt)} to build the authentication token.
   *
   * @param jwt decoded JWT
   * @return authentication token for the resolved user
   * @throws OAuth2AuthenticationException if the JWT's {@code jti} is present in the blacklist
   */
  @Override
  public final AbstractAuthenticationToken convert(Jwt jwt) {
    if (tokenBlacklistService.isRevoked(jwt.getId(), jwt.getSubject(), jwt.getIssuedAt())) {
      throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "Token has been revoked", null));
    }
    return doConvert(jwt);
  }

  /**
   * Subclass hook that builds the {@link AbstractAuthenticationToken} from a JWT that has already passed the revocation
   * check.
   *
   * @param jwt decoded, non-revoked JWT
   * @return authentication token for the resolved user
   */
  protected abstract AbstractAuthenticationToken doConvert(Jwt jwt);

  protected UserDetails findUser(String identifier) {
    try {
      return userDetailsService.loadUserByUsername(identifier);
    } catch (UsernameNotFoundException e) {
      throw new UsernameNotFoundException("User not found: " + identifier, e);
    }
  }

  /**
   * Extracts authorities from the given JWT token.
   *
   * @param jwt The JWT token from which to extract authorities.
   * @return A collection of GrantedAuthority extracted from the JWT.
   */
  protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    return this.jwtGrantedAuthoritiesConverter.convert(jwt);
  }
}
