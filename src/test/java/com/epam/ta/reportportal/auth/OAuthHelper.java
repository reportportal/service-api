/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.auth;

import static com.epam.ta.reportportal.TestConfig.TEST_SECRET;

import com.epam.ta.reportportal.entity.user.UserRole;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class OAuthHelper {

  private String defaultToken;

  private String superadminToken;

  private String customerToken;

  public String getDefaultToken() {
    return defaultToken == null ? defaultToken = createAccessToken("default", "1q2w3e",
        UserRole.USER) : defaultToken;
  }

  public String getSuperadminToken() {
    return superadminToken == null ?
        superadminToken = createAccessToken("superadmin", "erebus", UserRole.ADMINISTRATOR)
        : superadminToken;
  }

  public String getCustomerToken() {
    return customerToken == null ?
        customerToken = createAccessToken("default_customer", "erebus", UserRole.USER) :
        customerToken;
  }

  private String createAccessToken(String username, String password,
      UserRole... roles) {
    var authorities = Arrays.stream(roles)
        .map(role -> "ROLE_" + role)
        .collect(Collectors.toList());

    return Jwts.builder()
        .subject(username)
        .claim("user_name", username)
        .claim("scope", "ui")
        .claim("authorities", authorities)
        .issuedAt(new Date())
        .expiration(new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli()))
        .signWith(TEST_SECRET)
        .compact();

  }
}
