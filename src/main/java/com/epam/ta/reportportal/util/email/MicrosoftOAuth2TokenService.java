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

/**
 * Acquires SMTP (XOAUTH2) access tokens for a Microsoft Entra ID app registration using the
 * client-credentials grant (application permissions, e.g. {@code SMTP.SendAsApp}), so RP's Email
 * Server integration can authenticate to Exchange Online without basic auth.
 *
 * @author ReportPortal
 */
public interface MicrosoftOAuth2TokenService {

  /**
   * Returns a valid access token for the given app registration, reusing a cached token until it
   * is close to expiry and transparently refreshing it otherwise.
   *
   * @param tenantId     Entra tenant id
   * @param clientId     Entra app registration (client) id
   * @param clientSecret Entra app registration client secret
   * @return a bearer access token, scoped to {@code https://outlook.office365.com/.default}
   */
  String getAccessToken(String tenantId, String clientId, String clientSecret);

}
