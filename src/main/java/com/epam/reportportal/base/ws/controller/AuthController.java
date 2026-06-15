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

package com.epam.reportportal.base.ws.controller;

import static org.springframework.http.HttpStatus.OK;

import com.epam.reportportal.base.core.auth.LogoutHandler;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing authentication-related operations that are not covered by the OAuth2 authorization service,
 * currently the JWT logout endpoint.
 */
@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Auth", description = "Authentication Controller")
@RequiredArgsConstructor
public class AuthController {

  private final LogoutHandler logoutHandler;

  @PostMapping("/logout")
  @ResponseStatus(OK)
  @Operation(summary = "Invalidate the current JWT token")
  public OperationCompletionRS logout(Authentication authentication) {
    if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST,
          "Logout is supported for JWT-authenticated sessions only.");
    }
    logoutHandler.logout(jwtAuth.getToken());
    return new OperationCompletionRS("Successfully logged out");
  }
}
