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
package com.epam.reportportal.auth.config.utils;

import com.epam.reportportal.auth.ReportPortalClient;
import com.epam.reportportal.auth.TokenServicesFacade;
import com.epam.reportportal.auth.config.password.ClientToken;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;

/**
 * Converts an OAuth2 authentication request into a ReportPortal OAuth2 access token using
 * {@link com.epam.reportportal.auth.TokenServicesFacade}.
 *
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class ConvertToOauthToken {

  private TokenServicesFacade tokenService;

  public ConvertToOauthToken(TokenServicesFacade tokenService) {
    this.tokenService = tokenService;
  }

  public Authentication convert(ClientToken clientToken, Authentication authentication) throws AuthenticationException {
    OAuth2ClientAuthenticationToken clientPrincipal = (OAuth2ClientAuthenticationToken) clientToken.getClientPrincipal();
    RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
    Set<String> authorizedScopes = Set.of("ui");

    DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
        .registeredClient(registeredClient)
        .principal(authentication)
        .authorizationServerContext(AuthorizationServerContextHolder.getContext())
        .authorizedScopes(authorizedScopes)
        .authorizationGrantType(AuthorizationGrantType.PASSWORD)
        .authorizationGrant(clientToken);

    // ----- Access Token -----
    OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN)
        .principal(authentication).build();
    OAuth2Token generatedAccessToken = tokenService.createToken(ReportPortalClient.ui.name(),
        tokenContext.getPrincipal().getName(),
        tokenContext.getPrincipal().getAuthorities(), Collections.emptyMap());
    if (generatedAccessToken == null) {
      OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
          "The token generator failed to generate the access token.", null);
      throw new OAuth2AuthenticationException(error);
    }
    OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
        generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
        generatedAccessToken.getExpiresAt(), null);

    OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
        .principalName(clientPrincipal.getName())
        .authorizationGrantType(AuthorizationGrantType.PASSWORD);
    authorizationBuilder.token(accessToken,
        (metadata) -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME,
            ((ClaimAccessor) generatedAccessToken).getClaims()));

    // ----- Refresh Token -----
    OAuth2RefreshToken refreshToken = null;
    if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)
        && !clientPrincipal.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE)) {
      tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
      OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
      refreshToken = refreshTokenGenerator.generate(tokenContext);
      authorizationBuilder.refreshToken(refreshToken);
    }

    Map<String, Object> additionalParameters = Collections.emptyMap();

    return new OAuth2AccessTokenAuthenticationToken(registeredClient, authentication, accessToken, refreshToken,
        additionalParameters);
  }
}
