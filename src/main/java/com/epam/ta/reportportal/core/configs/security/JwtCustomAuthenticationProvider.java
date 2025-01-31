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

package com.epam.ta.reportportal.core.configs.security;

import static java.util.Objects.requireNonNull;

import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import java.time.Instant;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.util.Assert;

public class JwtCustomAuthenticationProvider implements AuthenticationProvider {

  private final Log logger = LogFactory.getLog(getClass());

  private final JwtDecoder jwtDecoder;
  private final DatabaseUserDetailsService userDetailsService;

  public JwtCustomAuthenticationProvider(JwtDecoder jwtDecoder,
      DatabaseUserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
    Assert.notNull(jwtDecoder, "jwtDecoder cannot be null");
    this.jwtDecoder = jwtDecoder;
  }


  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
    Jwt jwt = getJwt(bearer);
    String username = jwt.getClaim("user_name");

    BusinessRule.expect(isValid(jwt), BooleanUtils::isTrue)
        .verify(ErrorType.FORBIDDEN_OPERATION, "User token expired");

    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities()
    );
    authToken.setDetails(authentication.getDetails());

    return authToken;

  }

  private Jwt getJwt(BearerTokenAuthenticationToken bearer) {
    try {
      return this.jwtDecoder.decode(bearer.getToken());
    } catch (BadJwtException failed) {
      this.logger.debug("Failed to authenticate since the JWT was invalid");
      throw new InvalidBearerTokenException(failed.getMessage(), failed);
    } catch (JwtException failed) {
      throw new AuthenticationServiceException(failed.getMessage(), failed);
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
  }

  public boolean isValid(Jwt token) {
    return Instant.now()
        .isBefore(requireNonNull(requireNonNull(token).getExpiresAt()));
  }

}
