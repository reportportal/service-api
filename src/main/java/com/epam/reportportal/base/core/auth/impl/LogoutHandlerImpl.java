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

package com.epam.reportportal.base.core.auth.impl;

import com.epam.reportportal.base.core.auth.LogoutHandler;
import com.epam.reportportal.base.core.auth.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Default {@link LogoutHandler} that invalidates a JWT by adding its {@code jti} to the {@link TokenBlacklistService}.
 */
@Service
@RequiredArgsConstructor
public class LogoutHandlerImpl implements LogoutHandler {

  private final TokenBlacklistService tokenBlacklistService;

  @Override
  public void logout(Jwt jwt) {
    tokenBlacklistService.revoke(jwt.getId(), jwt.getExpiresAt());
  }
}
