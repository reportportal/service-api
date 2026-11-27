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

import lombok.Data;

/**
 * Class for JWT issuer properties. This class holds configuration details for JWT authentication, including URIs,
 * secret keys, algorithms, and claims.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Data
public class JwtIssuer {

  private String issuerUri;
  private String jwkSetUri;
  private String secretKey;
  private String algorithm = "HS256";
  private String usernameClaim = "sub";
  /**
   * Claim holding the user's authorities. When not configured, the token's authorities are ignored and the user's
   * ReportPortal role is used.
   */
  private String authoritiesClaim;
  /**
   * Expected {@code aud} value. When configured, tokens whose audience does not contain it are rejected.
   */
  private String audience;
  private UserResolverType userResolver = UserResolverType.DEFAULT;
}
