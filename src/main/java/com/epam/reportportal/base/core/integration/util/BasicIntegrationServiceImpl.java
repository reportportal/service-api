/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.integration.util;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.core.plugin.PluginBox;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.integration.IntegrationRQ;
import com.epam.reportportal.base.ws.converter.builders.IntegrationBuilder;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.command.ExtensionCommand;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default integration service for common, non-BTS integration types.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class BasicIntegrationServiceImpl implements IntegrationService {

  private static final String TEST_CONNECTION_COMMAND = "testConnection";
  private static final String RETRIEVE_CREATE_PARAMS = "retrieveCreate";
  private static final String RETRIEVE_UPDATED_PARAMS = "retrieveUpdated";

  protected IntegrationRepository integrationRepository;

  protected PluginBox pluginBox;

  @Autowired
  public BasicIntegrationServiceImpl(IntegrationRepository integrationRepository,
      PluginBox pluginBox) {
    this.integrationRepository = integrationRepository;
    this.pluginBox = pluginBox;
  }

  @Override
  public Integration createIntegration(IntegrationRQ integrationRq, IntegrationType integrationType) {
    return new IntegrationBuilder()
        .withCreationDate(Instant.now())
        .withType(integrationType)
        .withEnabled(integrationRq.getEnabled())
        .withName(integrationRq.getName())
        .withParams(new IntegrationParams(
            retrieveCreateParams(integrationType.getName(), integrationRq.getIntegrationParams())))
        .get();
  }

  @Override
  public Integration updateIntegration(Integration integration, IntegrationRQ integrationRQ) {
    Map<String, Object> validParams = retrieveUpdatedParams(integration.getType().getName(),
        integrationRQ.getIntegrationParams()
    );
    IntegrationParams combinedParams = getCombinedParams(integration, validParams);
    integration.setParams(combinedParams);
    ofNullable(integrationRQ.getEnabled()).ifPresent(integration::setEnabled);
    ofNullable(integrationRQ.getName()).ifPresent(integration::setName);
    return integration;
  }

  @Override
  public Map<String, Object> retrieveCreateParams(String integrationType,
      Map<String, Object> integrationParams) {
    final Optional<ExtensionCommand<?>> pluginCommand = getCommonCommand(integrationType, RETRIEVE_CREATE_PARAMS);
    if (pluginCommand.isPresent()) {
      return (Map<String, Object>) pluginCommand.get()
          .executeCommand(new PluginCommandRQ().context(new PluginCommandContext()).arguments(integrationParams));
    }
    return integrationParams;
  }

  @Override
  public Map<String, Object> retrieveUpdatedParams(String integrationType,
      Map<String, Object> integrationParams) {
    final Optional<ExtensionCommand<?>> pluginCommand =
        getCommonCommand(integrationType, RETRIEVE_UPDATED_PARAMS);
    if (pluginCommand.isPresent()) {
      var pluginCommandRQ = new PluginCommandRQ()
          .context(new PluginCommandContext())
          .arguments(integrationParams);
      return (Map<String, Object>) pluginCommand.get()
          .executeCommand(pluginCommandRQ);
    }
    return integrationParams;
  }

  @Override
  public boolean checkConnection(Integration integration) {
    final Optional<ExtensionCommand<?>> pluginCommand =
        getIntegrationCommand(integration.getType().getName(), TEST_CONNECTION_COMMAND);
    if (pluginCommand.isPresent()) {
      PluginCommandContext ctx = new PluginCommandContext()
          .orgId(integration.getOrganizationId());
      if (integration.getProject() != null) {
        ctx.projectId(integration.getProject().getId());
      }
      var pluginCommandRq = new PluginCommandRQ()
          .context(ctx)
          .arguments(integration.getParams().getParams());
      return (Boolean) pluginCommand.get()
          .executeCommand(integration, pluginCommandRq);
    }
    return true;
  }

  private Optional<ExtensionCommand<?>> getIntegrationCommand(String integration, String commandName) {
    ReportPortalExtensionPoint pluginInstance =
        pluginBox.getInstance(integration, ReportPortalExtensionPoint.class)
            .orElseThrow(
                () -> new ReportPortalException(BAD_REQUEST_ERROR, "Plugin for {} isn't installed", integration));
    return ofNullable(pluginInstance.getIntegrationExtensionCommand(commandName));
  }

  private Optional<ExtensionCommand<?>> getCommonCommand(String integration,
      String commandName) {
    ReportPortalExtensionPoint pluginInstance = pluginBox.getInstance(integration, ReportPortalExtensionPoint.class)
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, "Plugin for {} isn't installed", integration));

    return ofNullable(pluginInstance.getCommonExtensionCommand(commandName));
  }

  private IntegrationParams getCombinedParams(Integration integration,
      Map<String, Object> retrievedParams) {
    if (integration.getParams() != null && integration.getParams().getParams() != null) {
      integration.getParams().getParams().putAll(retrievedParams);
      return integration.getParams();
    }
    return new IntegrationParams(retrievedParams);
  }
}
