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

package com.epam.ta.reportportal.core.integration.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.activity.IntegrationDeletedEvent;
import com.epam.ta.reportportal.core.integration.DeleteIntegrationHandler;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.epam.ta.reportportal.ws.converter.converters.IntegrationConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.reporting.ErrorType.INTEGRATION_NOT_FOUND;
import static com.epam.ta.reportportal.ws.reporting.ErrorType.PROJECT_NOT_FOUND;

/**
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Service
public class DeleteIntegrationHandlerImpl implements DeleteIntegrationHandler {

  private final IntegrationRepository integrationRepository;

  private final ProjectRepository projectRepository;

  private final IntegrationTypeRepository integrationTypeRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public DeleteIntegrationHandlerImpl(IntegrationRepository integrationRepository,
      ProjectRepository projectRepository,
      IntegrationTypeRepository integrationTypeRepository,
      ApplicationEventPublisher eventPublisher) {
    this.integrationRepository = integrationRepository;
    this.projectRepository = projectRepository;
    this.integrationTypeRepository = integrationTypeRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public OperationCompletionRS deleteGlobalIntegration(Long integrationId, ReportPortalUser user) {
    Integration integration = integrationRepository.findGlobalById(integrationId)
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));
    publishActivities(List.of(integration), user);

    integrationRepository.deleteById(integration.getId());
    return new OperationCompletionRS(
        Suppliers.formattedSupplier("Global integration with id = {} has been successfully removed",
            integration.getId()
        ).get());
  }

  @Override
  public OperationCompletionRS deleteGlobalIntegrationsByType(String type, ReportPortalUser user) {
    IntegrationType integrationType = integrationTypeRepository.findByName(type)
        .orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, type));

    List<Integration> integrations = integrationRepository.findAllGlobalInIntegrationTypeIds(
        Collections.singletonList(integrationType.getId())
    );
    publishActivities(integrations, user);

    integrationRepository.deleteAllGlobalByIntegrationTypeId(integrationType.getId());
    return new OperationCompletionRS(
        "All global integrations with type ='" + integrationType.getName()
            + "' integrations have been successfully removed.");
  }

  @Override
  public OperationCompletionRS deleteProjectIntegration(Long integrationId, String projectName,
      ReportPortalUser user) {
    Project project = projectRepository.findByName(projectName)
        .orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectName));
    Integration integration = integrationRepository.findByIdAndProjectId(integrationId,
            project.getId())
        .orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, integrationId));
    integration.getProject().getIntegrations()
        .removeIf(it -> it.getId().equals(integration.getId()));
    integrationRepository.deleteById(integration.getId());
    eventPublisher.publishEvent(new IntegrationDeletedEvent(TO_ACTIVITY_RESOURCE.apply(integration),
        user.getUserId(),
        user.getUsername()
    ));
    return new OperationCompletionRS(
        "Integration with ID = '" + integrationId + "' has been successfully deleted.");
  }

  @Override
  public OperationCompletionRS deleteProjectIntegrationsByType(String type, String projectName,
      ReportPortalUser user) {
    Project project = projectRepository.findByName(projectName)
        .orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectName));
    IntegrationType integrationType = integrationTypeRepository.findByName(type)
        .orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, type));
    List<Integration> integrations = integrationRepository.findAllByProjectIdAndInIntegrationTypeIds(
        project.getId(),
        Collections.singletonList(integrationType.getId())
    );
    publishActivities(integrations, user);

    integrationRepository.deleteAllByProjectIdAndIntegrationTypeId(project.getId(),
        integrationType.getId());
    return new OperationCompletionRS(
        "All integrations with type ='" + type + "' for project with name ='" + project.getName()
            + "' have been successfully deleted");
  }

  private void publishActivities(List<Integration> integrations, ReportPortalUser user) {
    integrations.stream()
        .map(TO_ACTIVITY_RESOURCE)
        .forEach(it -> eventPublisher.publishEvent(
            new IntegrationDeletedEvent(it, user.getUserId(), user.getUsername())));
  }
}
