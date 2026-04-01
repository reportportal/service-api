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

import static com.epam.reportportal.auth.integration.converter.OAuthRegistrationConverters.paramsToResource;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import com.epam.reportportal.auth.integration.handler.DeleteAuthIntegrationHandler;
import com.epam.reportportal.auth.integration.handler.GetAuthIntegrationHandler;
import com.epam.reportportal.auth.model.settings.OAuthRegistrationResource;
import com.epam.reportportal.base.core.integration.CreateIntegrationHandler;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.integration.IntegrationRQ;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import com.epam.reportportal.extension.AuthExtension;
import com.epam.reportportal.extension.common.ExtensionPoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

  private final DeleteAuthIntegrationHandler deleteAuthIntegrationHandler;

  private final GetAuthIntegrationHandler getAuthIntegrationHandler;

  private final CreateIntegrationHandler createIntegrationHandler;

  private final IntegrationTypeRepository integrationTypeRepository;

  private final IntegrationRepository integrationRepository;

  private final Pf4jPluginBox pluginBox;

  @Value("${server.servlet.context-path}")
  private String pathValue;

  @Autowired
  public OAuthConfigurationEndpoint(
      DeleteAuthIntegrationHandler deleteAuthIntegrationHandler,
      GetAuthIntegrationHandler getAuthIntegrationHandler,
      CreateIntegrationHandler createIntegrationHandler,
      IntegrationTypeRepository integrationTypeRepository,
      IntegrationRepository integrationRepository,
      Pf4jPluginBox pluginBox) {
    this.deleteAuthIntegrationHandler = deleteAuthIntegrationHandler;
    this.getAuthIntegrationHandler = getAuthIntegrationHandler;
    this.createIntegrationHandler = createIntegrationHandler;
    this.integrationTypeRepository = integrationTypeRepository;
    this.integrationRepository = integrationRepository;
    this.pluginBox = pluginBox;
  }

  /**
   * Creates or updates OAuth integration settings by delegating to the integration framework.
   *
   * @param oauthProviderId            OAuth provider plugin name / integration name
   * @param registrationParams OAuth configuration
   * @param user                       Authenticated admin user
   * @return The stored OAuth integration settings
   */
  @Transactional
  @RequestMapping(value = "/{authId}", method = {POST, PUT})
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Creates/Updates OAuth Integration Settings")
  public OAuthRegistrationResource updateOAuthSettings(
      @PathVariable("authId") String oauthProviderId,
      @RequestBody @Validated Map<String, Object> registrationParams,
      @AuthenticationPrincipal ReportPortalUser user) {

    Map<String, Object> integrationParams = pluginBox.getPlugins().stream()
        .filter(plugin -> ExtensionPoint.AUTH.equals(plugin.getType()))
        .map(plugin -> pluginBox.getInstance(plugin.getId(), AuthExtension.class).orElse(null))
        .filter(Objects::nonNull)
        .map(ext -> ext.fillOAuthRegistration(oauthProviderId, registrationParams, pathValue))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.AUTH_INTEGRATION_NOT_FOUND, oauthProviderId));

    IntegrationRQ integrationRQ = new IntegrationRQ();
    integrationRQ.setName(oauthProviderId);
    integrationRQ.setEnabled(true);
    integrationRQ.setIntegrationParams(integrationParams);

    IntegrationType integrationType = integrationTypeRepository.findByName(oauthProviderId)
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.AUTH_INTEGRATION_NOT_FOUND, oauthProviderId));

    integrationRepository.findByNameAndTypeIdAndProjectIdIsNull(oauthProviderId, integrationType.getId())
        .ifPresentOrElse(existing -> createIntegrationHandler.updateGlobalIntegration(existing.getId(),
                integrationRQ, user),
            () -> createIntegrationHandler.createGlobalIntegration(integrationRQ, oauthProviderId,
                user)
        );

    return paramsToResource(oauthProviderId, integrationParams);
  }

  /**
   * Deletes oauth integration settings.
   *
   * @param oauthProviderId Oauth settings Profile Id
   * @return Completion status
   */
  @DeleteMapping(value = "/{authId}")
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Deletes OAuth Integration Settings")
  public OperationCompletionRS deleteOAuthSetting(@PathVariable("authId") String oauthProviderId) {
    return deleteAuthIntegrationHandler.deleteOauthSettingsById(oauthProviderId);
  }

  /**
   * Returns oauth integration settings by ID.
   *
   * @param oauthProviderId ID of third-party OAuth provider
   * @return OAuth integration settings
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
