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

import com.epam.ta.reportportal.auth.ApiKeyUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.ApiKeyRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.user.ApiKey;
import jakarta.xml.bind.DatatypeConverter;
import java.time.LocalDate;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {

  private final Log logger = LogFactory.getLog(getClass());
  private final Converter<ReportPortalUser, ? extends AbstractAuthenticationToken> authenticationConverter = new ApiKeyReportPortalUserConverter();

  @Autowired
  private ApiKeyRepository apiKeyRepository;

  @Autowired
  private UserRepository userRepository;


  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
    String apiToken = (String) bearer.getPrincipal();
    String hashedKey = DatatypeConverter.printHexBinary(DigestUtils.sha3_256(apiToken));
    ApiKey apiKey = apiKeyRepository.findByHash(hashedKey);
    if (ApiKeyUtils.validateToken(apiToken) && apiKeyRepository.findByHash(hashedKey) != null) {
      return userRepository.findReportPortalUser(apiKey.getUserId())
          .filter(ReportPortalUser::isEnabled)
          .map(user -> {
            LocalDate today = LocalDate.now();
            if (apiKey.getLastUsedAt() == null || !apiKey.getLastUsedAt().equals(today)) {
              apiKeyRepository.updateLastUsedAt(apiKey.getId(), hashedKey, today);
            }
            return user;
          })
          .map(authenticationConverter::convert)
          .map(authToken -> {
            if (authToken.getDetails() == null) {
              authToken.setDetails(bearer.getDetails());
            }
            return authToken;
          })
          .orElseThrow();
    }

    return authentication;
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
  }

}
