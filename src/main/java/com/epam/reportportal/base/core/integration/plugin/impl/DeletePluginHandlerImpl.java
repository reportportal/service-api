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

package com.epam.reportportal.base.core.integration.plugin.impl;

import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;

import com.epam.reportportal.base.core.events.domain.PluginDeletedEvent;
import com.epam.reportportal.base.core.integration.plugin.DeletePluginHandler;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.ReservedIntegrationTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.activity.PluginActivityResource;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Uninstalls a plugin and cleans related data.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class DeletePluginHandlerImpl implements DeletePluginHandler {

  private final IntegrationTypeRepository integrationTypeRepository;
  private final Pf4jPluginBox pluginBox;

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public DeletePluginHandlerImpl(IntegrationTypeRepository integrationTypeRepository,
      Pf4jPluginBox pluginBox, ApplicationEventPublisher applicationEventPublisher) {
    this.integrationTypeRepository = integrationTypeRepository;
    this.pluginBox = pluginBox;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public OperationCompletionRS deleteById(Long id, ReportPortalUser user) {

    IntegrationType integrationType = integrationTypeRepository.findById(id).orElseThrow(
        () -> new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
            Suppliers.formattedSupplier("Plugin with id = '{}' not found", id).get()
        ));

    expect(
        ReservedIntegrationTypeEnum.fromName(integrationType.getName()), Optional::isEmpty).verify(
        ErrorType.PLUGIN_REMOVE_ERROR,
        Suppliers.formattedSupplier("Unable to remove reserved plugin - '{}'",
            integrationType.getName()
        )
    );

    PluginActivityResource pluginActivityResource = new PluginActivityResource();
    pluginActivityResource.setName(integrationType.getName());
    pluginActivityResource.setId(integrationType.getId());

    applicationEventPublisher.publishEvent(
        new PluginDeletedEvent(pluginActivityResource, user.getUserId(), user.getUsername()));

    if (!pluginBox.deletePlugin(integrationType.getName())) {
      throw new ReportPortalException(
          ErrorType.PLUGIN_REMOVE_ERROR, "Unable to remove from plugin manager.");
    }

    integrationTypeRepository.deleteById(integrationType.getId());

    return new OperationCompletionRS(
        Suppliers.formattedSupplier("Plugin = '{}' has been successfully removed",
            integrationType.getName()
        ).get());

  }
}
