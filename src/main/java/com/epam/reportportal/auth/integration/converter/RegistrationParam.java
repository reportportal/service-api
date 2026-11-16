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

package com.epam.reportportal.auth.integration.converter;

/**
 * Field name constants for OAuth2 client registration parameters.
 */
public final class RegistrationParam {

  public static final String CLIENT_ID = "clientId";
  public static final String CLIENT_SECRET = "clientSecret";
  public static final String CLIENT_AUTH_METHOD = "clientAuthMethod";
  public static final String CLIENT_NAME = "clientName";
  public static final String AUTH_GRANT_TYPE = "authGrantType";
  public static final String REDIRECT_URL_TEMPLATE = "redirectUrlTemplate";
  public static final String REDIRECT_URI_TEMPLATE = "redirectUriTemplate";
  public static final String SCOPES = "scopes";
  public static final String AUTHORIZATION_URI = "authorizationUri";
  public static final String TOKEN_URI = "tokenUri";
  public static final String USER_INFO_ENDPOINT_URI = "userInfoEndpointUri";
  public static final String USER_INFO_ENDPOINT_NAME_ATTRIBUTE = "userInfoEndpointNameAttribute";
  public static final String USER_INFO_ENDPOINT_NAME_ATTR = "userInfoEndpointNameAttr";
  public static final String JWK_SET_URI = "jwkSetUri";
  public static final String RESTRICTIONS = "restrictions";

  public static final String ORGANIZATION_TYPE = "organization";
  public static final String ORGANIZATIONS_KEY = "organizations";

  private RegistrationParam() {
  }
}
