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

package com.epam.reportportal.auth.endpoint;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import com.epam.reportportal.auth.integration.handler.CreateAuthIntegrationHandler;
import com.epam.reportportal.auth.integration.handler.DeleteAuthIntegrationHandler;
import com.epam.reportportal.auth.integration.handler.GetAuthIntegrationHandler;
import com.epam.reportportal.auth.model.settings.OAuthRegistrationResource;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Endpoint for oauth configs.
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Controller
@RequestMapping("/settings/oauth")
@Tag(name = "o-auth-configuration-endpoint", description = "O Auth Configuration Endpoint")
public class OAuthConfigurationEndpoint {

  private final CreateAuthIntegrationHandler createAuthIntegrationHandler;

  private final DeleteAuthIntegrationHandler deleteAuthIntegrationHandler;

  private final GetAuthIntegrationHandler getAuthIntegrationHandler;

  @Autowired
  public OAuthConfigurationEndpoint(CreateAuthIntegrationHandler createAuthIntegrationHandler,
      DeleteAuthIntegrationHandler deleteAuthIntegrationHandler,
      GetAuthIntegrationHandler getAuthIntegrationHandler) {
    this.createAuthIntegrationHandler = createAuthIntegrationHandler;
    this.deleteAuthIntegrationHandler = deleteAuthIntegrationHandler;
    this.getAuthIntegrationHandler = getAuthIntegrationHandler;
  }

  /**
   * Updates oauth integration settings
   *
   * @param clientRegistrationResource OAuth configuration
   * @param oauthProviderId            Oauth settings Profile Id
   * @return All defined OAuth integration settings
   */
  @Transactional
  @RequestMapping(value = "/{authId}", method = {POST, PUT})
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Creates/Updates OAuth Integration Settings")
  public OAuthRegistrationResource updateOAuthSettings(
      @PathVariable("authId") String oauthProviderId,
      @RequestBody @Validated OAuthRegistrationResource clientRegistrationResource) {
    return createAuthIntegrationHandler.createOrUpdateOauthSettings(oauthProviderId,
        clientRegistrationResource);
  }

  /**
   * Deletes oauth integration settings.
   *
   * @param oauthProviderId Oauth settings Profile Id
   * @return All defined OAuth integration settings
   */
  @DeleteMapping(value = "/{authId}")
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Deletes OAuth Integration Settings")
  public OperationCompletionRS deleteOAuthSetting(@PathVariable("authId") String oauthProviderId) {
    return deleteAuthIntegrationHandler.deleteOauthSettingsById(oauthProviderId);
  }

  /**
   * Returns oauth integration settings.
   *
   * @return All defined OAuth integration settings
   */
  @GetMapping
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Returns OAuth Server Settings")
  public Map<String, OAuthRegistrationResource> getOAuthSettings() {
    return getAuthIntegrationHandler.getAllOauthIntegrations();
  }

  /**
   * Returns oauth integration settings.
   *
   * @param oauthProviderId ID of third-party OAuth provider
   * @return All defined OAuth integration settings
   */
  @RequestMapping(value = "/{authId}", method = {GET})
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Returns OAuth Server Settings")
  public ResponseEntity<OAuthRegistrationResource> getOAuthSettings(
      @PathVariable("authId") String oauthProviderId) {
    return getAuthIntegrationHandler.getOauthIntegrationById(oauthProviderId);
  }
}
