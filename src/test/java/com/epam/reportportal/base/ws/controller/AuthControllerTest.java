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

package com.epam.reportportal.base.ws.controller;

import static com.epam.reportportal.base.util.StandaloneMockMvcSupport.standaloneJsonSetup;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.auth.basic.LoginService;
import com.epam.reportportal.auth.model.AccessTokenRs;
import com.epam.reportportal.base.core.auth.LogoutHandler;
import com.epam.reportportal.base.exception.handler.GlobalExceptionHandler;
import com.epam.reportportal.base.exception.rest.ReportPortalExceptionResolver;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock
  private LoginService loginService;

  @Mock
  private LogoutHandler logoutHandler;

  @InjectMocks
  private AuthController authController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
    ReflectionTestUtils.setField(globalExceptionHandler, "reportPortalExceptionResolver",
        new ReportPortalExceptionResolver());
    mockMvc = standaloneJsonSetup(authController)
        .setControllerAdvice(globalExceptionHandler)
        .build();
  }

  @Test
  void loginWhenValidCredentialsShouldReturnBearerAccessToken() throws Exception {
    // Given
    given(loginService.login("default", "1q2w3e"))
        .willReturn(new AccessTokenRs("jwt-token-value", "Bearer", 86400L));

    // When / Then
    mockMvc.perform(post("/sso/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("grant_type", "password")
            .param("username", "default")
            .param("password", "1q2w3e"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").value("jwt-token-value"))
        .andExpect(jsonPath("$.token_type").value("Bearer"))
        .andExpect(jsonPath("$.expires_in").value(86400));
  }

  @Test
  void loginWhenBadPasswordShouldReturnErrorCode4003() throws Exception {
    // Given
    given(loginService.login("default", "wrong"))
        .willThrow(new BadCredentialsException("Bad credentials"));

    // When / Then
    mockMvc.perform(post("/sso/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("grant_type", "password")
            .param("username", "default")
            .param("password", "wrong"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errorCode").value(4003));
  }

  @Test
  void loginWhenAccountLockedShouldReturnErrorCode4004() throws Exception {
    // Given
    given(loginService.login("default", "1q2w3e"))
        .willThrow(new LockedException("User account is locked"));

    // When / Then
    mockMvc.perform(post("/sso/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("grant_type", "password")
            .param("username", "default")
            .param("password", "1q2w3e"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errorCode").value(4004));
  }

  @Test
  void loginWhenAddressLockedShouldReturnErrorCode4004() throws Exception {
    // Given
    given(loginService.login("default", "1q2w3e"))
        .willThrow(new ReportPortalException(ErrorType.ADDRESS_LOCKED));

    // When / Then
    mockMvc.perform(post("/sso/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("grant_type", "password")
            .param("username", "default")
            .param("password", "1q2w3e"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errorCode").value(4004));
  }

  @Test
  void loginWhenUnsupportedGrantTypeShouldReturnIncorrectRequest() throws Exception {
    // When / Then
    mockMvc.perform(post("/sso/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("grant_type", "authorization_code")
            .param("username", "default")
            .param("password", "1q2w3e"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value(ErrorType.INCORRECT_REQUEST.getCode()));
  }

  @Test
  void loginWhenUsernameMissingShouldReturnBadRequest() throws Exception {
    // When / Then
    mockMvc.perform(post("/sso/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("grant_type", "password")
            .param("password", "1q2w3e"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void logoutWhenJwtAuthenticationShouldInvalidateToken() throws Exception {
    // Given
    Jwt jwt = Jwt.withTokenValue("jwt-token-value")
        .header("alg", "HS256")
        .claim("sub", "default")
        .build();
    var authentication = new JwtAuthenticationToken(jwt);

    // When / Then
    mockMvc.perform(post("/v1/auth/logout").principal(authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Successfully logged out"));

    verify(logoutHandler).logout(any(Jwt.class));
  }

  @Test
  void logoutWhenNonJwtAuthenticationShouldReturnIncorrectRequest() throws Exception {
    // Given
    var authentication = new UsernamePasswordAuthenticationToken("default", "1q2w3e");

    // When / Then
    mockMvc.perform(post("/v1/auth/logout").principal(authentication))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value(ErrorType.INCORRECT_REQUEST.getCode()));
  }
}
