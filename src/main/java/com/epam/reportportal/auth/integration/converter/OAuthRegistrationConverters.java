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

import static com.epam.reportportal.auth.integration.converter.RegistrationParam.AUTHORIZATION_URI;
import static com.epam.reportportal.auth.integration.converter.RegistrationParam.AUTH_GRANT_TYPE;
import static com.epam.reportportal.auth.integration.converter.RegistrationParam.CLIENT_AUTH_METHOD;
import static com.epam.reportportal.auth.integration.converter.RegistrationParam.CLIENT_ID;
import static com.epam.reportportal.auth.integration.converter.RegistrationParam.CLIENT_NAME;
import static com.epam.reportportal.auth.integration.converter.RegistrationParam.CLIENT_SECRET;
import static com.epam.reportportal.auth.integration.converter.RegistrationParam.JWK_SET_URI;
import static com.epam.reportportal.auth.integration.converter.RegistrationParam.ORGANIZATIONS_KEY;
import static com.epam.reportportal.auth.integration.converter.RegistrationParam.ORGANIZATION_TYPE;
import static com.epam.reportportal.auth.integration.converter.RegistrationParam.REDIRECT_URI_TEMPLATE;
import static com.epam.reportportal.auth.integration.converter.RegistrationParam.RESTRICTIONS;
import static com.epam.reportportal.auth.integration.converter.RegistrationParam.SCOPES;
import static com.epam.reportportal.auth.integration.converter.RegistrationParam.TOKEN_URI;
import static com.epam.reportportal.auth.integration.converter.RegistrationParam.USER_INFO_ENDPOINT_NAME_ATTR;
import static com.epam.reportportal.auth.integration.converter.RegistrationParam.USER_INFO_ENDPOINT_URI;

import com.epam.reportportal.auth.model.OAuthRegistrationResource;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

/**
 * Converter between resource, database, and Spring representation of OAuthRegistration.
 *
 * @author Anton Machulski
 */
public class OAuthRegistrationConverters {


  private OAuthRegistrationConverters() {
    //static only
  }

  private static String getStringParam(Map<String, Object> params, String key) {
    Object value = params.get(key);
    return value instanceof String ? (String) value : null;
  }

  private static String getStringParam(Map<String, Object> params, String key, String defaultValue) {
    Object value = params.get(key);
    return value instanceof String ? (String) value : defaultValue;
  }

  public static final Collector<OAuthRegistrationResource, ?,
      Map<String, OAuthRegistrationResource>> RESOURCE_KEY_MAPPER =
      Collectors.toMap(OAuthRegistrationResource::getId, r -> r);

  public static final Function<Integration, OAuthRegistrationResource> TO_RESOURCE = integration -> {
    Preconditions.checkNotNull(integration);
    OAuthRegistrationResource resource = new OAuthRegistrationResource();
    resource.setId(integration.getName());

    var params = integration.getParams().getParams();
    resource.setClientId(getStringParam(params, CLIENT_ID));
    resource.setClientSecret(getStringParam(params, CLIENT_SECRET));
    resource.setClientAuthMethod(getStringParam(params, CLIENT_AUTH_METHOD));
    resource.setAuthGrantType(getStringParam(params, AUTH_GRANT_TYPE));
    resource.setRedirectUrlTemplate(getStringParam(params, REDIRECT_URI_TEMPLATE));
    resource.setAuthorizationUri(getStringParam(params, AUTHORIZATION_URI));
    resource.setTokenUri(getStringParam(params, TOKEN_URI));
    resource.setUserInfoEndpointUri(getStringParam(params, USER_INFO_ENDPOINT_URI));
    resource.setUserInfoEndpointNameAttribute(getStringParam(params, USER_INFO_ENDPOINT_NAME_ATTR));
    resource.setJwkSetUri(getStringParam(params, JWK_SET_URI));
    resource.setClientName(getStringParam(params, CLIENT_NAME));

    Object scopesObj = params.get(SCOPES);
    if (scopesObj instanceof Collection<?> collection) {
      Set<String> scopes = new HashSet<>();
      for (Object item : collection) {
        if (item instanceof String s) {
          scopes.add(s);
        }
      }
      resource.setScopes(scopes);
    }

    Object restrictionsObj = params.get(RESTRICTIONS);
    if (restrictionsObj instanceof List<?> list) {
      String organizations = list.stream()
          .filter(item -> item instanceof Map<?, ?>)
          .map(item -> (Map<?, ?>) item)
          .filter(r -> r.get("type") instanceof String t && ORGANIZATION_TYPE.equalsIgnoreCase(t))
          .map(r -> r.get("value") instanceof String v ? v : null)
          .filter(v -> v != null)
          .collect(Collectors.joining(","));
      Map<String, String> restrictions = new HashMap<>();
      restrictions.put(ORGANIZATIONS_KEY, organizations);
      resource.setRestrictions(restrictions);
    }
    return resource;
  };

  /**
   * Converts an {@link Integration} (with OAUTH auth flow) to a params {@link Map} by reading the integration's params
   * map and adding the integration name as {@code "id"}.
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
  public static final Function<Integration, ClientRegistration> INTEGRATION_TO_OAUTH_REGISTRATION = integration -> {
    Map<String, Object> params = integration.getParams().getParams();
    List<String> scopes = params.get(SCOPES) instanceof List
        ? (List<String>) params.get(SCOPES)
        : List.of();

    String clientAuthMethod = getStringParam(params, CLIENT_AUTH_METHOD, "").trim();
    String authGrantType = getStringParam(params, AUTH_GRANT_TYPE, "").trim();

    return ClientRegistration.withRegistrationId(integration.getName())
        .clientId(getStringParam(params, CLIENT_ID))
        .clientSecret(getStringParam(params, CLIENT_SECRET))
        .clientAuthenticationMethod(clientAuthMethod.isEmpty()
            ? ClientAuthenticationMethod.CLIENT_SECRET_BASIC
            : new ClientAuthenticationMethod(clientAuthMethod))
        .authorizationGrantType(authGrantType.isEmpty()
            ? AuthorizationGrantType.AUTHORIZATION_CODE
            : new AuthorizationGrantType(authGrantType))
        .redirectUri(getStringParam(params, REDIRECT_URI_TEMPLATE, ""))
        .authorizationUri(getStringParam(params, AUTHORIZATION_URI, ""))
        .tokenUri(getStringParam(params, TOKEN_URI, ""))
        .userInfoUri(getStringParam(params, USER_INFO_ENDPOINT_URI, ""))
        .userNameAttributeName(getStringParam(params, USER_INFO_ENDPOINT_NAME_ATTR, ""))
        .jwkSetUri(getStringParam(params, JWK_SET_URI, ""))
        .clientName(getStringParam(params, CLIENT_NAME, integration.getName()))
        .scope(scopes.toArray(String[]::new))
        .build();
  };
}
