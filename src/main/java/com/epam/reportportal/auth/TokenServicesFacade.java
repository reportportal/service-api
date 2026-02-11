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
package com.epam.reportportal.auth;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
@Component
public class TokenServicesFacade {

  private final JwtEncoder jwtEncoder;
  private final String issuer;

  public TokenServicesFacade(JwtEncoder jwtEncoder, @Value("${rp.jwt.issuer}") String issuer) {
    this.jwtEncoder = jwtEncoder;
    this.issuer = issuer;
  }

  public Jwt createToken(
      ReportPortalClient client,
      String username,
      Authentication userAuthentication,
      Map<String, Serializable> extensionParams
  ) {
    return createNonApiToken(client, username, userAuthentication, extensionParams);
  }

  public Jwt createNonApiToken(
      ReportPortalClient client,
      String username,
      Authentication userAuthentication,
      Map<String, Serializable> extensionParams
  ) {
    return createToken(client.name(), username, userAuthentication, extensionParams);
  }

  private Jwt createToken(
      String clientId,
      String username,
      Authentication authentication,
      Map<String, Serializable> extensionParams
  ) {
    Instant now = Instant.now();

    Instant expiry = now.plus(1, ChronoUnit.DAYS);

    JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
        .id(UUID.randomUUID().toString())
        .subject(username)
        .audience(List.of("reportportal"))
        .claim("user_name", username)
        .claim("authorities", authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()))
        .issuedAt(now)
        .notBefore(now)
        .expiresAt(expiry)
        .issuer(issuer)
        .claim(OAuth2ParameterNames.CLIENT_ID, clientId)
        .claim("scopes", List.of("ui"))
        .claim("token_type", "access_token");

    if (extensionParams != null) {
      extensionParams.forEach(claimsBuilder::claim);
    }

    JwtClaimsSet jwtClaims = claimsBuilder.build();

    JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();

    JwtEncoderParameters parameters = JwtEncoderParameters.from(jwsHeader, jwtClaims);
    return jwtEncoder.encode(parameters);
  }

  public Jwt createToken(
      String clientId,
      String username,
      Collection<? extends GrantedAuthority> authorities,
      Map<String, Serializable> extensionParams
  ) {
    Instant now = Instant.now();

    Instant expiry = now.plus(1, ChronoUnit.DAYS);

    JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
        .id(UUID.randomUUID().toString())
        .subject(username)
        .audience(List.of("reportportal"))
        .claim("user_name", username)
        .claim("authorities", authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()))
        .issuedAt(now)
        .notBefore(now)
        .expiresAt(expiry)
        .issuer(issuer)
        .claim(OAuth2ParameterNames.CLIENT_ID, clientId)
        .claim("scopes", List.of("ui"))
        .claim("token_type", "access_token");

    if (extensionParams != null) {
      extensionParams.forEach(claimsBuilder::claim);
    }

    JwtClaimsSet jwtClaims = claimsBuilder.build();

    JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();

    JwtEncoderParameters parameters = JwtEncoderParameters.from(jwsHeader, jwtClaims);
    return jwtEncoder.encode(parameters);
  }
}
