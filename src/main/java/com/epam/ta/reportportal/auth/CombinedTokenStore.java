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

import com.epam.ta.reportportal.auth.util.AuthUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.ApiKeyRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.user.ApiKey;
import com.google.common.collect.Maps;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component(value = "combinedTokenStore")
@Transactional(readOnly = true)
public class CombinedTokenStore extends JwtTokenStore {

  @Autowired
  private ApiKeyRepository apiKeyRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  public CombinedTokenStore(JwtAccessTokenConverter jwtTokenEnhancer) {
    super(jwtTokenEnhancer);
  }

  @Override
  public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
    try {
      return super.readAuthentication(token);
    } catch (InvalidTokenException e) {
      return this.readAuthentication(token.getValue());
    }
  }

  @Transactional
  @Override
  public OAuth2Authentication readAuthentication(String tokenId) {
    try {
      return super.readAuthentication(tokenId);
    } catch (InvalidTokenException e) {
      String hashedKey = DatatypeConverter.printHexBinary(DigestUtils.sha3_256(tokenId));
      ApiKey apiKey = apiKeyRepository.findByHash(hashedKey);
      if (apiKey != null) {
        Optional<ReportPortalUser> user = userRepository.findReportPortalUser(apiKey.getUserId());
        if (user.isPresent()) {
          LocalDate today = LocalDate.now();
          if (apiKey.getLastUsedAt() == null || !apiKey.getLastUsedAt().equals(today)) {
            apiKeyRepository.updateLastUsedAt(apiKey.getId(), hashedKey, today);
          }
          return getAuthentication(getUserWithAuthorities(user.get()));
        }
      }
      return null;
    }
  }

  @Transactional
  @Override
  public OAuth2AccessToken readAccessToken(String tokenValue) {
    try {
      return super.readAccessToken(tokenValue);
    } catch (InvalidTokenException e) {
      if (ApiKeyUtils.validateToken(tokenValue)) {
        DefaultOAuth2AccessToken defaultOAuth2AccessToken =
            new DefaultOAuth2AccessToken(tokenValue);
        defaultOAuth2AccessToken.setExpiration(new Date(System.currentTimeMillis() + 60 * 1000L));
        String hashedKey = DatatypeConverter.printHexBinary(DigestUtils.sha3_256(tokenValue));
        ApiKey apiKey = apiKeyRepository.findByHash(hashedKey);
        if (apiKey != null) {
          LocalDate today = LocalDate.now();
          if (apiKey.getLastUsedAt() == null || !apiKey.getLastUsedAt().equals(today)) {
            apiKeyRepository.updateLastUsedAt(apiKey.getId(), hashedKey, today);
          }
        }
        return defaultOAuth2AccessToken;
      }
      return null; //let spring security handle the invalid token
    }
  }

  private OAuth2Authentication getAuthentication(ReportPortalUser user) {
    HashMap<String, String> requestParameters = new HashMap<>();
    requestParameters.put("username", user.getUsername());
    requestParameters.put("client_id", ReportPortalClient.api.name());

    Set<GrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority(user.getUserRole().getAuthority()));

    Set<String> scopes = Collections.singleton(ReportPortalClient.api.name());

    OAuth2Request authorizationRequest =
        new OAuth2Request(requestParameters, ReportPortalClient.api.name(), authorities, true,
            scopes, Collections.emptySet(), null, Collections.emptySet(), null
        );

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(user, null, authorities);

    OAuth2Authentication authenticationRequest =
        new OAuth2Authentication(authorizationRequest, authenticationToken);
    authenticationRequest.setAuthenticated(true);

    return authenticationRequest;
  }

  private ReportPortalUser getUserWithAuthorities(ReportPortalUser user) {
    return ReportPortalUser.userBuilder().withUserName(user.getUsername())
        .withPassword(user.getPassword())
        .withAuthorities(AuthUtils.AS_AUTHORITIES.apply(user.getUserRole()))
        .withUserId(user.getUserId()).withUserRole(user.getUserRole())
        .withProjectDetails(Maps.newHashMapWithExpectedSize(1)).withEmail(user.getEmail()).build();
  }
}
