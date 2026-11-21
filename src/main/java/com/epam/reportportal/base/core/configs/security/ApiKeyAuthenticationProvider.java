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

package com.epam.reportportal.base.core.configs.security;

import com.epam.reportportal.base.auth.ApiKeyUtils;
import com.epam.reportportal.base.core.configs.security.converters.ApiKeyReportPortalUserConverter;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ApiKeyRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.ApiKey;
import jakarta.xml.bind.DatatypeConverter;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Authentication provider for API key authentication. It validates the API key and retrieves the associated user.
 */
@Component
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {

  private final Converter<ReportPortalUser, ? extends AbstractAuthenticationToken> authenticationConverter = new ApiKeyReportPortalUserConverter();

  private final ApiKeyRepository apiKeyRepository;

  private final UserRepository userRepository;

  /**
   * Constructor for ApiKeyAuthenticationProvider.
   */
  @Autowired
  public ApiKeyAuthenticationProvider(ApiKeyRepository apiKeyRepository, UserRepository userRepository) {
    this.apiKeyRepository = apiKeyRepository;
    this.userRepository = userRepository;
  }

  /**
   * Authenticates the provided authentication token.
   *
   * @param authentication The authentication token containing the API key.
   * @return An authenticated user token if the API key is valid.
   * @throws AuthenticationException If the API key is invalid or the user is not found.
   */
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
    String apiToken = (String) bearer.getPrincipal();
    if (!ApiKeyUtils.validateToken(apiToken)) {
      throw new BadCredentialsException("Invalid access token");
    }
    String hashedKey = DatatypeConverter.printHexBinary(DigestUtils.sha3_256(apiToken));
    ApiKey apiKey = apiKeyRepository.findByHash(hashedKey);
    if (apiKey == null) {
      throw new BadCredentialsException("Invalid access token");
    }
    ReportPortalUser user = userRepository.findReportPortalUser(apiKey.getUserId())
        .orElseThrow(() -> new BadCredentialsException("Invalid access token"));
    if (!user.isEnabled()) {
      throw new DisabledException("User account is disabled");
    }
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    if (apiKey.getLastUsedAt() == null || !apiKey.getLastUsedAt().equals(today)) {
      apiKeyRepository.updateLastUsedAt(apiKey.getId(), hashedKey, today);
    }
    AbstractAuthenticationToken authToken = authenticationConverter.convert(user);
    if (authToken.getDetails() == null) {
      authToken.setDetails(bearer.getDetails());
    }
    return authToken;
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
  }

}
