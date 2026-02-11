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
import com.epam.reportportal.base.infrastructure.persistence.entity.oauth.OAuthRegistration;
import com.epam.reportportal.base.infrastructure.persistence.entity.oauth.OAuthRegistrationRestriction;
import com.epam.reportportal.base.infrastructure.persistence.entity.oauth.OAuthRegistrationScope;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

/**
 * Converter between resource, database, default Spring representation of OAuthRegistration.
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

  public static final Function<String, OAuthRegistrationScope> SCOPE_FROM_RESOURCE = scope -> {
    OAuthRegistrationScope oAuthRegistrationScope = new OAuthRegistrationScope();
    oAuthRegistrationScope.setScope(scope);
    return oAuthRegistrationScope;
  };

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
      registration -> ClientRegistration.withRegistrationId(
              registration.getClientName())
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

  public static final BiFunction<OAuthRegistrationResource, ClientRegistration, OAuthRegistration> FROM_SPRING_MERGE = (registrationResource, clientResource) -> {
    OAuthRegistration registration = new OAuthRegistration();
    registration.setId(clientResource.getRegistrationId());
    registration.setClientId(registrationResource.getClientId());
    registration.setClientSecret(registrationResource.getClientSecret());
    registration.setClientAuthMethod(
        ofNullable(registrationResource.getClientAuthMethod()).orElseGet(
            () -> clientResource.getClientAuthenticationMethod()
                .getValue()));
    registration.setClientName(
        ofNullable(registrationResource.getClientName()).orElseGet(clientResource::getClientName));
    registration.setAuthGrantType(ofNullable(registrationResource.getAuthGrantType()).orElseGet(
        () -> clientResource.getAuthorizationGrantType()
            .getValue()));
    registration.setRedirectUrlTemplate(
        ofNullable(registrationResource.getRedirectUrlTemplate()).orElseGet(
            clientResource::getRedirectUri));
    registration.setScopes(
        ofNullable(registrationResource.getScopes()).map(scopes -> scopes.stream()
                .map(SCOPE_FROM_RESOURCE)
                .peek(registrationScope -> registrationScope.setRegistration(registration))
                .collect(Collectors.toSet()))
            .orElse(clientResource.getScopes()
                .stream()
                .map(SCOPE_FROM_RESOURCE)
                .peek(registrationScope -> registrationScope.setRegistration(registration))
                .collect(Collectors.toSet())));

    List<OAuthRegistrationRestriction> registrationRestrictions =
        OAuthRestrictionConverter.FROM_RESOURCE.apply(registrationResource);

    registration.setRestrictions(registrationRestrictions.stream()
        .peek(restriction -> restriction.setRegistration(registration))
        .collect(Collectors.toSet()));

    ClientRegistration.ProviderDetails details = clientResource.getProviderDetails();
    registration.setAuthorizationUri(
        ofNullable(registrationResource.getAuthorizationUri()).orElseGet(
            details::getAuthorizationUri));
    registration.setTokenUri(
        ofNullable(registrationResource.getTokenUri()).orElseGet(details::getTokenUri));
    registration.setUserInfoEndpointUri(
        ofNullable(registrationResource.getUserInfoEndpointUri()).orElseGet(
            () -> details.getUserInfoEndpoint()
                .getUri()));
    registration.setUserInfoEndpointNameAttribute(
        ofNullable(registrationResource.getUserInfoEndpointNameAttribute()).orElseGet(() -> details
            .getUserInfoEndpoint()
            .getUserNameAttributeName()));
    registration.setJwkSetUri(
        ofNullable(registrationResource.getJwkSetUri()).orElseGet(details::getJwkSetUri));

    return registration;
  };
}
