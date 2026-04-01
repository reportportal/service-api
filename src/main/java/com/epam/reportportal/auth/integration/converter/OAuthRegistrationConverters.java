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

  public static final Collector<OAuthRegistrationResource, ?,
      Map<String, OAuthRegistrationResource>> RESOURCE_KEY_MAPPER =
      Collectors.toMap(OAuthRegistrationResource::getId, r -> r);

  public static final Function<Integration, OAuthRegistrationResource> TO_RESOURCE = integration -> {
    Preconditions.checkNotNull(integration);
    OAuthRegistrationResource resource = new OAuthRegistrationResource();
    resource.setId(integration.getName());

    var params = integration.getParams().getParams();
    resource.setClientId((String) params.get(CLIENT_ID));
    resource.setClientSecret((String) params.get(CLIENT_SECRET));
    resource.setClientAuthMethod((String) params.get(CLIENT_AUTH_METHOD));
    resource.setAuthGrantType((String) params.get(AUTH_GRANT_TYPE));
    resource.setRedirectUrlTemplate((String) params.get(REDIRECT_URI_TEMPLATE));
    resource.setAuthorizationUri((String) params.get(AUTHORIZATION_URI));
    resource.setTokenUri((String) params.get(TOKEN_URI));
    resource.setUserInfoEndpointUri((String) params.get(USER_INFO_ENDPOINT_URI));
    resource.setUserInfoEndpointNameAttribute((String) params.get(USER_INFO_ENDPOINT_NAME_ATTR));
    resource.setJwkSetUri((String) params.get(JWK_SET_URI));
    resource.setClientName((String) params.get(CLIENT_NAME));

    Object scopesObj = params.get(SCOPES);
    if (scopesObj instanceof Collection) {
      resource.setScopes(new HashSet<>((Collection<String>) scopesObj));
    }

    Object restrictionsObj = params.get(RESTRICTIONS);
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

    return ClientRegistration.withRegistrationId(integration.getName())
        .clientId((String) params.get(CLIENT_ID))
        .clientSecret((String) params.get(CLIENT_SECRET))
        .clientAuthenticationMethod(
            new ClientAuthenticationMethod((String) params.getOrDefault(CLIENT_AUTH_METHOD, "")))
        .authorizationGrantType(new AuthorizationGrantType(
            (String) params.getOrDefault(AUTH_GRANT_TYPE, "")))
        .redirectUri((String) params.getOrDefault(REDIRECT_URI_TEMPLATE, ""))
        .authorizationUri((String) params.getOrDefault(AUTHORIZATION_URI, ""))
        .tokenUri((String) params.getOrDefault(TOKEN_URI, ""))
        .userInfoUri((String) params.getOrDefault(USER_INFO_ENDPOINT_URI, ""))
        .userNameAttributeName((String) params.getOrDefault(USER_INFO_ENDPOINT_NAME_ATTR, ""))
        .jwkSetUri((String) params.getOrDefault(JWK_SET_URI, ""))
        .clientName((String) params.getOrDefault(CLIENT_NAME, integration.getName()))
        .scope(scopes.toArray(String[]::new))
        .build();
  };
}
