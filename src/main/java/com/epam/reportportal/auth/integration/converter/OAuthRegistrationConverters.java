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

package com.epam.reportportal.auth.integration.converter;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.auth.model.settings.OAuthRegistrationResource;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.oauth.OAuthRegistration;
import com.epam.reportportal.base.infrastructure.persistence.entity.oauth.OAuthRegistrationRestriction;
import com.epam.reportportal.base.infrastructure.persistence.entity.oauth.OAuthRegistrationScope;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

/**
 * Converter between resource, database, and Spring representation of OAuthRegistration.
 *
 * @author Anton Machulski
 */
public class OAuthRegistrationConverters {

  private static final String ORGANIZATION_TYPE = "organization";
  private static final String ORGANIZATIONS_KEY = "organizations";

  private OAuthRegistrationConverters() {
    //static only
  }

  public static final Collector<OAuthRegistrationResource, ?,
      Map<String, OAuthRegistrationResource>> RESOURCE_KEY_MAPPER =
      Collectors.toMap(OAuthRegistrationResource::getId, r -> r);

  public static final Function<OAuthRegistration, OAuthRegistrationResource> TO_RESOURCE = db -> {
    Preconditions.checkNotNull(db);
    OAuthRegistrationResource resource = new OAuthRegistrationResource();
    resource.setId(db.getId());
    resource.setClientId(db.getClientId());
    resource.setClientSecret(db.getClientSecret());
    resource.setClientAuthMethod(db.getClientAuthMethod());
    resource.setAuthGrantType(db.getAuthGrantType());
    resource.setRedirectUrlTemplate(db.getRedirectUrlTemplate());
    resource.setAuthorizationUri(db.getAuthorizationUri());
    resource.setTokenUri(db.getTokenUri());
    resource.setUserInfoEndpointUri(db.getUserInfoEndpointUri());
    resource.setUserInfoEndpointNameAttribute(db.getUserInfoEndpointNameAttribute());
    resource.setJwkSetUri(db.getJwkSetUri());
    resource.setClientName(db.getClientName());
    ofNullable(db.getScopes()).ifPresent(scopes -> resource.setScopes(scopes.stream()
        .map(OAuthRegistrationScope::getScope)
        .collect(Collectors.toSet())));
    ofNullable(db.getRestrictions()).ifPresent(
        r -> resource.setRestrictions(OAuthRestrictionConverter.TO_RESOURCE.apply(db)));
    return resource;
  };

  public static final Function<OAuthRegistration, ClientRegistration> TO_SPRING =
      registration -> ClientRegistration.withRegistrationId(registration.getClientName())
          .clientId(registration.getClientId())
          .clientSecret(registration.getClientSecret())
          .clientAuthenticationMethod(
              new ClientAuthenticationMethod(registration.getClientAuthMethod()))
          .authorizationGrantType(new AuthorizationGrantType(registration.getAuthGrantType()))
          .redirectUri(registration.getRedirectUrlTemplate())
          .authorizationUri(registration.getAuthorizationUri())
          .tokenUri(registration.getTokenUri())
          .userInfoUri(registration.getUserInfoEndpointUri())
          .userNameAttributeName(registration.getUserInfoEndpointNameAttribute())
          .jwkSetUri(registration.getJwkSetUri())
          .clientName(registration.getClientName())
          .scope(ofNullable(registration.getScopes()).map(scopes -> scopes.stream()
              .map(OAuthRegistrationScope::getScope)
              .toArray(String[]::new)).orElse(ArrayUtils.EMPTY_STRING_ARRAY))
          .build();

  /**
   * Converts an {@link Integration} (with OAUTH auth flow) to a params {@link Map} by reading the
   * integration's params map and adding the integration name as {@code "id"}.
   */
  public static final Function<Integration, Map<String, Object>> FROM_INTEGRATION = integration -> {
    Map<String, Object> params = new HashMap<>(integration.getParams().getParams());
    params.put("id", integration.getName());
    return params;
  };

  /**
   * Converts an {@link Integration} params directly to a Spring {@link ClientRegistration}.
   */
  @SuppressWarnings("unchecked")
  public static final Function<Integration, ClientRegistration> INTEGRATION_TO_SPRING =
      integration -> {
        Map<String, Object> params = integration.getParams().getParams();
        List<String> scopes = params.get("scopes") instanceof List
            ? (List<String>) params.get("scopes")
            : List.of();

        return ClientRegistration.withRegistrationId(integration.getName())
            .clientId((String) params.get("clientId"))
            .clientSecret((String) params.get("clientSecret"))
            .clientAuthenticationMethod(new ClientAuthenticationMethod(
                (String) params.getOrDefault("clientAuthMethod", "")))
            .authorizationGrantType(new AuthorizationGrantType(
                (String) params.getOrDefault("authGrantType", "")))
            .redirectUri((String) params.getOrDefault("redirectUriTemplate", ""))
            .authorizationUri((String) params.getOrDefault("authorizationUri", ""))
            .tokenUri((String) params.getOrDefault("tokenUri", ""))
            .userInfoUri((String) params.getOrDefault("userInfoEndpointUri", ""))
            .userNameAttributeName((String) params.getOrDefault("userInfoEndpointNameAttr", ""))
            .jwkSetUri((String) params.getOrDefault("jwkSetUri", ""))
            .clientName((String) params.getOrDefault("clientName", integration.getName()))
            .scope(scopes.toArray(String[]::new))
            .build();
      };

  /**
   * Converts integration params map to {@link OAuthRegistrationResource}.
   */
  @SuppressWarnings("unchecked")
  public static OAuthRegistrationResource paramsToResource(String registrationId,
      Map<String, Object> params) {
    OAuthRegistrationResource resource = new OAuthRegistrationResource();
    resource.setId(registrationId);
    resource.setClientId((String) params.get("clientId"));
    resource.setClientSecret((String) params.get("clientSecret"));
    resource.setClientAuthMethod((String) params.get("clientAuthMethod"));
    resource.setAuthGrantType((String) params.get("authGrantType"));
    resource.setRedirectUrlTemplate((String) params.get("redirectUriTemplate"));
    resource.setAuthorizationUri((String) params.get("authorizationUri"));
    resource.setTokenUri((String) params.get("tokenUri"));
    resource.setUserInfoEndpointUri((String) params.get("userInfoEndpointUri"));
    resource.setUserInfoEndpointNameAttribute((String) params.get("userInfoEndpointNameAttr"));
    resource.setJwkSetUri((String) params.get("jwkSetUri"));
    resource.setClientName((String) params.get("clientName"));

    Object scopesObj = params.get("scopes");
    if (scopesObj instanceof Collection) {
      resource.setScopes(new HashSet<>((Collection<String>) scopesObj));
    }

    Object restrictionsObj = params.get("restrictions");
    if (restrictionsObj instanceof List) {
      String organizations = ((List<Map<String, Object>>) restrictionsObj).stream()
          .filter(r -> ORGANIZATION_TYPE.equalsIgnoreCase((String) r.get("type")))
          .map(r -> (String) r.get("value"))
          .collect(Collectors.joining(","));
      Map<String, String> restrictions = new HashMap<>();
      restrictions.put(ORGANIZATIONS_KEY, organizations);
      resource.setRestrictions(restrictions);
    }

    return resource;
  }
}
