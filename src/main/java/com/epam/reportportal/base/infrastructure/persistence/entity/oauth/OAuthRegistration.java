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

package com.epam.reportportal.base.infrastructure.persistence.entity.oauth;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;

/**
 * Plain POJO representing an OAuth registration (no longer a JPA entity).
 * Data is sourced from the integration table's params field.
 *
 * @author Andrei Varabyeu
 */
@Getter
public class OAuthRegistration implements Serializable {

  private String id;
  private String clientId;
  private String clientSecret;
  private String clientAuthMethod;
  private String authGrantType;
  private String redirectUrlTemplate;
  private String authorizationUri;
  private String tokenUri;
  private String userInfoEndpointUri;
  private String userInfoEndpointNameAttribute;
  private String jwkSetUri;
  private String clientName;
  private Set<OAuthRegistrationScope> scopes;
  private Set<OAuthRegistrationRestriction> restrictions;

  public void setId(String id) {
    this.id = id;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public void setClientAuthMethod(String clientAuthMethod) {
    this.clientAuthMethod = clientAuthMethod;
  }

  public void setAuthGrantType(String authGrantType) {
    this.authGrantType = authGrantType;
  }

  public void setRedirectUrlTemplate(String redirectUrlTemplate) {
    this.redirectUrlTemplate = redirectUrlTemplate;
  }

  public void setAuthorizationUri(String authorizationUri) {
    this.authorizationUri = authorizationUri;
  }

  public void setTokenUri(String tokenUri) {
    this.tokenUri = tokenUri;
  }

  public void setUserInfoEndpointUri(String userInfoEndpointUri) {
    this.userInfoEndpointUri = userInfoEndpointUri;
  }

  public void setUserInfoEndpointNameAttribute(String userInfoEndpointNameAttribute) {
    this.userInfoEndpointNameAttribute = userInfoEndpointNameAttribute;
  }

  public void setJwkSetUri(String jwkSetUri) {
    this.jwkSetUri = jwkSetUri;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public void setScopes(Set<OAuthRegistrationScope> scopes) {
    this.scopes = scopes;
  }

  public void setRestrictions(Set<OAuthRegistrationRestriction> restrictions) {
    this.restrictions = restrictions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OAuthRegistration that = (OAuthRegistration) o;
    return Objects.equals(id, that.id) && Objects.equals(clientId, that.clientId)
        && Objects.equals(clientSecret, that.clientSecret)
        && Objects.equals(clientAuthMethod, that.clientAuthMethod)
        && Objects.equals(authGrantType, that.authGrantType)
        && Objects.equals(redirectUrlTemplate, that.redirectUrlTemplate)
        && Objects.equals(authorizationUri, that.authorizationUri)
        && Objects.equals(tokenUri, that.tokenUri)
        && Objects.equals(userInfoEndpointUri, that.userInfoEndpointUri)
        && Objects.equals(userInfoEndpointNameAttribute, that.userInfoEndpointNameAttribute)
        && Objects.equals(jwkSetUri, that.jwkSetUri)
        && Objects.equals(clientName, that.clientName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, clientId, clientSecret, clientAuthMethod, authGrantType,
        redirectUrlTemplate, authorizationUri, tokenUri, userInfoEndpointUri,
        userInfoEndpointNameAttribute, jwkSetUri, clientName);
  }
}
