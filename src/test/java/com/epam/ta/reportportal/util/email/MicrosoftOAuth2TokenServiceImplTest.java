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

package com.epam.ta.reportportal.util.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.util.email.MicrosoftOAuth2TokenServiceImpl.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author ReportPortal
 */
class MicrosoftOAuth2TokenServiceImplTest {

  private RestTemplate restTemplate;
  private MicrosoftOAuth2TokenServiceImpl tokenService;

  @BeforeEach
  void setUp() {
    restTemplate = mock(RestTemplate.class);
    tokenService = new MicrosoftOAuth2TokenServiceImpl(restTemplate);
  }

  @Test
  void shouldReturnAccessTokenFromTokenEndpoint() {

    when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(
        new TokenResponse("access-token-1", 3600L));

    String token = tokenService.getAccessToken("tenant", "client", "secret");

    assertEquals("access-token-1", token);
  }

  @Test
  void shouldCacheTokenUntilExpiry() {

    when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(
        new TokenResponse("access-token-1", 3600L));

    String first = tokenService.getAccessToken("tenant", "client", "secret");
    String second = tokenService.getAccessToken("tenant", "client", "secret");

    assertEquals("access-token-1", first);
    assertEquals("access-token-1", second);
    verify(restTemplate, times(1)).postForObject(anyString(), any(), any());
  }

  @Test
  void shouldRequestNewTokenWhenAlreadyExpired() {

    when(restTemplate.postForObject(anyString(), any(), any()))
        .thenReturn(new TokenResponse("access-token-1", 0L))
        .thenReturn(new TokenResponse("access-token-2", 3600L));

    String first = tokenService.getAccessToken("tenant", "client", "secret");
    String second = tokenService.getAccessToken("tenant", "client", "secret");

    assertEquals("access-token-1", first);
    assertEquals("access-token-2", second);
    verify(restTemplate, times(2)).postForObject(anyString(), any(), any());
  }

  @Test
  void shouldRequestSeparateTokensForDifferentCredentials() {

    when(restTemplate.postForObject(anyString(), any(), any()))
        .thenReturn(new TokenResponse("token-for-client-a", 3600L))
        .thenReturn(new TokenResponse("token-for-client-b", 3600L));

    String tokenA = tokenService.getAccessToken("tenant", "client-a", "secret");
    String tokenB = tokenService.getAccessToken("tenant", "client-b", "secret");

    assertEquals("token-for-client-a", tokenA);
    assertEquals("token-for-client-b", tokenB);
  }

  @Test
  void shouldWrapRestClientExceptionAsReportPortalException() {

    when(restTemplate.postForObject(anyString(), any(), any()))
        .thenThrow(new RestClientException("invalid_client"));

    assertThrows(ReportPortalException.class,
        () -> tokenService.getAccessToken("tenant", "client", "wrong-secret"));
  }

  @Test
  void shouldFailWhenTokenResponseHasNoAccessToken() {

    when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(
        new TokenResponse(null, 3600L));

    assertThrows(ReportPortalException.class,
        () -> tokenService.getAccessToken("tenant", "client", "secret"));
  }

}
