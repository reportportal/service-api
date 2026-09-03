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

package com.epam.ta.reportportal.util.email;

import static com.epam.reportportal.rules.exception.ErrorType.EMAIL_CONFIGURATION_IS_INCORRECT;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * {@link MicrosoftOAuth2TokenService} backed by the Microsoft identity platform's
 * {@code client_credentials} grant against {@code https://login.microsoftonline.com}.
 *
 * <p>Tokens are cached in memory per (tenant, client, secret) triple and refreshed a short margin
 * before they expire, so a burst of outgoing emails does not trigger a token request per message.
 *
 * @author ReportPortal
 */
@Service
public class MicrosoftOAuth2TokenServiceImpl implements MicrosoftOAuth2TokenService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftOAuth2TokenServiceImpl.class);

  private static final String TOKEN_ENDPOINT_FORMAT =
      "https://login.microsoftonline.com/%s/oauth2/v2.0/token";
  private static final String SCOPE = "https://outlook.office365.com/.default";
  /* Refresh this many seconds before the token's real expiry to avoid using an expired token. */
  private static final long EXPIRY_MARGIN_SECONDS = 60L;

  private final RestTemplate restTemplate;
  private final ConcurrentHashMap<String, CachedToken> cache = new ConcurrentHashMap<>();

  public MicrosoftOAuth2TokenServiceImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public String getAccessToken(String tenantId, String clientId, String clientSecret) {
    String cacheKey = cacheKey(tenantId, clientId, clientSecret);
    CachedToken cached = cache.get(cacheKey);
    if (cached != null && cached.isValid()) {
      return cached.accessToken;
    }
    CachedToken fresh = requestToken(tenantId, clientId, clientSecret);
    cache.put(cacheKey, fresh);
    return fresh.accessToken;
  }

  private CachedToken requestToken(String tenantId, String clientId, String clientSecret) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "client_credentials");
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);
    body.add("scope", SCOPE);

    String url = String.format(TOKEN_ENDPOINT_FORMAT, tenantId);
    try {
      TokenResponse response = restTemplate.postForObject(url, new HttpEntity<>(body, headers),
          TokenResponse.class);
      if (response == null || response.accessToken == null) {
        throw new ReportPortalException(EMAIL_CONFIGURATION_IS_INCORRECT,
            "Microsoft identity platform returned an empty token response.");
      }
      long expiresInSeconds = response.expiresIn != null ? response.expiresIn : 0L;
      Instant expiresAt = Instant.now().plusSeconds(Math.max(0, expiresInSeconds - EXPIRY_MARGIN_SECONDS));
      return new CachedToken(response.accessToken, expiresAt);
    } catch (RestClientException e) {
      LOGGER.error("Unable to acquire an OAuth2 access token for the email server integration", e);
      throw new ReportPortalException(EMAIL_CONFIGURATION_IS_INCORRECT,
          "Unable to acquire an OAuth2 access token. Please check the tenant id, client id and "
              + "client secret of the email server integration. " + e.getMessage());
    }
  }

  private static String cacheKey(String tenantId, String clientId, String clientSecret) {
    return tenantId + '|' + clientId + '|' + Objects.hashCode(clientSecret);
  }

  private static final class CachedToken {

    private final String accessToken;
    private final Instant expiresAt;

    private CachedToken(String accessToken, Instant expiresAt) {
      this.accessToken = accessToken;
      this.expiresAt = expiresAt;
    }

    private boolean isValid() {
      return Instant.now().isBefore(expiresAt);
    }
  }

  /* Package-private (not private) so the test class in this same package can build instances directly. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  static final class TokenResponse {

    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("expires_in")
    Long expiresIn;

    TokenResponse() {
    }

    TokenResponse(String accessToken, Long expiresIn) {
      this.accessToken = accessToken;
      this.expiresIn = expiresIn;
    }
  }

}
