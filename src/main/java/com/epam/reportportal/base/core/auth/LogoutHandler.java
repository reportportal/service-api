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

package com.epam.reportportal.base.core.auth;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Performs the server-side actions required to log a user out of a JWT-authenticated session.
 *
 * <p>Implementations are expected to invalidate the supplied token so that any subsequent request
 * carrying the same {@code jti} is rejected at authentication time.
 */
public interface LogoutHandler {

  /**
   * Invalidates the given JWT for all future requests.
   *
   * @param jwt the JWT that authenticated the current request; must expose a non-null {@code jti} and {@code exp}
   */
  void logout(Jwt jwt);
}