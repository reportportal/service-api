/*
 * Copyright 2026 EPAM Systems
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ApiKeyRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.ApiKey;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;

class ApiKeyAuthenticationProviderTest {

  private static final String VALID_TOKEN = "c229070a-56fe-4f99-ad57-fa945aa9443b";

  private final ApiKeyRepository apiKeyRepository = mock(ApiKeyRepository.class);
  private final UserRepository userRepository = mock(UserRepository.class);
  private final ApiKeyAuthenticationProvider provider =
      new ApiKeyAuthenticationProvider(apiKeyRepository, userRepository);

  @Test
  void malformedTokenIsRejectedWithoutRepositoryLookup() {
    assertThrows(BadCredentialsException.class,
        () -> provider.authenticate(new BearerTokenAuthenticationToken("abc.def!abc.def!abc.def!abc")));

    verifyNoInteractions(apiKeyRepository, userRepository);
  }

  @Test
  void unknownKeyIsBadCredentialsAndLookedUpOnce() {
    when(apiKeyRepository.findByHash(anyString())).thenReturn(null);

    assertThrows(BadCredentialsException.class,
        () -> provider.authenticate(new BearerTokenAuthenticationToken(VALID_TOKEN)));

    verify(apiKeyRepository, times(1)).findByHash(anyString());
  }

  @Test
  void keyOfMissingUserIsBadCredentials() {
    ApiKey apiKey = new ApiKey();
    apiKey.setUserId(1L);
    when(apiKeyRepository.findByHash(anyString())).thenReturn(apiKey);
    when(userRepository.findReportPortalUser(1L)).thenReturn(Optional.empty());

    assertThrows(BadCredentialsException.class,
        () -> provider.authenticate(new BearerTokenAuthenticationToken(VALID_TOKEN)));
  }

  @Test
  void keyOfDisabledUserIsDisabledException() {
    ApiKey apiKey = new ApiKey();
    apiKey.setUserId(1L);
    ReportPortalUser user = mock(ReportPortalUser.class);
    when(user.isEnabled()).thenReturn(false);
    when(apiKeyRepository.findByHash(anyString())).thenReturn(apiKey);
    when(userRepository.findReportPortalUser(1L)).thenReturn(Optional.of(user));

    assertThrows(DisabledException.class,
        () -> provider.authenticate(new BearerTokenAuthenticationToken(VALID_TOKEN)));
  }
}
