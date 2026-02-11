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

package com.epam.reportportal.auth.oauth;

import static com.epam.reportportal.auth.integration.converter.OAuthRegistrationConverters.FROM_SPRING_MERGE;

import com.epam.reportportal.auth.model.settings.OAuthRegistrationResource;
import com.epam.reportportal.base.infrastructure.persistence.entity.oauth.OAuthRegistration;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

public class OAuthProviderFactory {

  private static final String CALL_BACK_URL = "{baseUrl}/sso/login/{registrationId}";

  public static OAuthRegistration fillOAuthRegistration(String oauthProviderId,
      OAuthRegistrationResource registrationResource, String pathValue) {

    switch (oauthProviderId) {
      case "github":
        ClientRegistration springRegistration = createGitHubProvider(oauthProviderId,
            registrationResource, pathValue);
        return FROM_SPRING_MERGE.apply(registrationResource, springRegistration);
      default:
        throw new ReportPortalException(ErrorType.AUTH_INTEGRATION_NOT_FOUND, oauthProviderId);
    }

  }

  private static ClientRegistration createGitHubProvider(String oauthProviderId,
      OAuthRegistrationResource registrationResource, String pathValue) {
    return CommonOAuth2Provider.GITHUB.getBuilder(oauthProviderId)
        .clientId(registrationResource.getClientId())
        .clientSecret(registrationResource.getClientSecret())
        .redirectUri(getCallBackUrl(pathValue))
        .scope("read:user", "user:email", "read:org")
        .clientName(oauthProviderId)
        .build();
  }

  private static String getCallBackUrl(String pathValue) {
    return StringUtils.isEmpty(pathValue) || pathValue.equals("/") ? CALL_BACK_URL.replaceFirst("baseUrl}/",
        "baseUrl}/api/") : CALL_BACK_URL;
  }
}
