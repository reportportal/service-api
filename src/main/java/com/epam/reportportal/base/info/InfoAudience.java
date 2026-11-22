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

package com.epam.reportportal.base.info;

import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Tells who is reading {@code /info}. The endpoint is open to everyone, but the security filter chain still
 * authenticates a bearer token (JWT or API key) sent with the request, so a signed-in caller is recognised here.
 */
public final class InfoAudience {

  private static final AuthenticationTrustResolver TRUST_RESOLVER = new AuthenticationTrustResolverImpl();

  private InfoAudience() {
    //static only
  }

  /**
   * @return {@code true} when the current request carries a valid, non-anonymous authentication
   */
  public static boolean isAuthenticated() {
    return TRUST_RESOLVER.isAuthenticated(SecurityContextHolder.getContext().getAuthentication());
  }
}
