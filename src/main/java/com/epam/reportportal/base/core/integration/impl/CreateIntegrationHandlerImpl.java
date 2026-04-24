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

package com.epam.reportportal.base.core.integration.impl;

import static com.epam.reportportal.base.ws.converter.converters.IntegrationConverter.TO_ACTIVITY_RESOURCE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.auth.integration.handler.CreateAuthIntegrationHandler;
import com.epam.reportportal.base.core.events.domain.IntegrationCreatedEvent;
import com.epam.reportportal.base.core.events.domain.IntegrationUpdatedEvent;
import com.epam.reportportal.base.core.integration.CreateIntegrationHandler;
import com.epam.reportportal.base.core.integration.util.IntegrationService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.EntryCreatedRS;
import com.epam.reportportal.base.model.activity.IntegrationActivityResource;
import com.epam.reportportal.base.model.integration.IntegrationRQ;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Default implementation of CreateIntegrationHandler that delegates to integration-type-specific strategies.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CreateIntegrationHandlerImpl implements CreateIntegrationHandler {

  private final Map<String, IntegrationService> integrationServiceMapping;

  private final IntegrationRepository integrationRepository;

  private final ProjectRepository projectRepository;

  private final ApplicationEventPublisher eventPublisher;

  private final IntegrationTypeRepository integrationTypeRepository;

  private final IntegrationService basicIntegrationService;

  private final CreateAuthIntegrationHandler createAuthIntegrationHandler;

  @Autowired
  public CreateIntegrationHandlerImpl(@Qualifier("integrationServiceMapping")
      Map<String, IntegrationService> integrationServiceMapping,
      IntegrationRepository integrationRepository, ProjectRepository projectRepository,
      ApplicationEventPublisher eventPublisher, IntegrationTypeRepository integrationTypeRepository,
      @Qualifier("basicIntegrationServiceImpl") IntegrationService integrationService,
      CreateAuthIntegrationHandler createAuthIntegrationHandler) {
    this.integrationServiceMapping = integrationServiceMapping;
    this.integrationRepository = integrationRepository;
    this.projectRepository = projectRepository;
    this.eventPublisher = eventPublisher;
    this.integrationTypeRepository = integrationTypeRepository;
    this.basicIntegrationService = integrationService;
    this.createAuthIntegrationHandler = createAuthIntegrationHandler;
  }

  @Override
  public EntryCreatedRS createGlobalIntegration(IntegrationRQ createRequest, String pluginName, ReportPortalUser user) {
    IntegrationType integrationType = integrationTypeRepository.findByName(pluginName)
        .orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, pluginName));

    if (IntegrationGroupEnum.AUTH == integrationType.getIntegrationGroup()) {
      var newIntegration = createAuthIntegrationHandler.createAuthIntegration(pluginName, createRequest);
      return new EntryCreatedRS(newIntegration.getId());
    }

    IntegrationService integrationService =
        integrationServiceMapping.getOrDefault(integrationType.getName(),
            this.basicIntegrationService
        );
    String integrationName = ofNullable(createRequest.getName())
        .map(name -> {
          validateGlobalIntegrationName(name, integrationType);
          return name;
        })
        .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_INTEGRATION_NAME,
            "Integration name should be not null"
        ));
    createRequest.setName(integrationName);

    Integration integration = integrationService.createIntegration(createRequest, integrationType);
    integration.setCreator(user.getUsername());
    integrationService.checkConnection(integration);
    integrationRepository.save(integration);
    publishCreationActivity(integration, user);

    return new EntryCreatedRS(integration.getId());

  }

  private IntegrationService resolveIntegrationService(IntegrationType integrationType) {
    return integrationServiceMapping
        .getOrDefault(integrationType.getName(), this.basicIntegrationService);
  }

  @Override
  public EntryCreatedRS createProjectIntegration(String projectKey, IntegrationRQ createRequest,
      String pluginName, ReportPortalUser user) {

    Project project = projectRepository.findByKey(projectKey)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectKey));

    IntegrationType integrationType = integrationTypeRepository.findByName(pluginName)
        .orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, pluginName));

    IntegrationService integrationService =
        integrationServiceMapping.getOrDefault(integrationType.getName(),
            this.basicIntegrationService
        );

    String integrationName = ofNullable(createRequest.getName())
        .map(name -> {
          validateProjectIntegrationName(name, integrationType, project);
          return name;
        })
        .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_INTEGRATION_NAME,
            "Integration name should be not null"
        ));
    createRequest.setName(integrationName);

    Integration integration = integrationService.createIntegration(createRequest, integrationType);
    integration.setProject(project);
    integration.setCreator(user.getUsername());
    integrationService.checkConnection(integration);
    integrationRepository.save(integration);

    publishCreationActivity(integration, user);

    return new EntryCreatedRS(integration.getId());
  }

  @Override
  public OperationCompletionRS updateGlobalIntegration(Long id, IntegrationRQ updateRequest,
      ReportPortalUser user) {

    final Integration integration = integrationRepository.findGlobalById(id)
        .orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, id));

    IntegrationActivityResource beforeUpdate = TO_ACTIVITY_RESOURCE.apply(integration);

    ofNullable(updateRequest.getName()).ifPresent(name -> {
      if (!name.equals(integration.getName())) {
        validateGlobalIntegrationName(name, integration.getType());
      }
      updateRequest.setName(name);
    });

    Integration updatedIntegration;
    if (IntegrationGroupEnum.AUTH == integration.getType().getIntegrationGroup()) {
      updatedIntegration = createAuthIntegrationHandler.updateAuthIntegration(integration, updateRequest);
    } else {
      IntegrationService integrationService =
          integrationServiceMapping.getOrDefault(integration.getType().getName(),
              this.basicIntegrationService
          );

      updatedIntegration = integrationService.updateIntegration(integration, updateRequest);
      integrationService.checkConnection(updatedIntegration);
      integrationRepository.save(updatedIntegration);
    }

    publishUpdateActivity(user, beforeUpdate, updatedIntegration, null);

    return new OperationCompletionRS(
        "Integration with id = " + updatedIntegration.getId() + " has been successfully updated.");
  }

  @Override
  public OperationCompletionRS updateProjectIntegration(Long id, String projectKey,
      IntegrationRQ updateRequest, ReportPortalUser user) {

    Project project = projectRepository.findByKey(projectKey)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectKey));

    final Integration integration = integrationRepository.findByIdAndProjectId(id, project.getId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, id));

    IntegrationActivityResource beforeUpdate = TO_ACTIVITY_RESOURCE.apply(integration);

    ofNullable(updateRequest.getName()).ifPresent(name -> {
      if (!name.equals(integration.getName())) {
        validateProjectIntegrationName(name, integration.getType(), project);
      }
      updateRequest.setName(name);
    });

    IntegrationService integrationService =
        integrationServiceMapping.getOrDefault(integration.getType().getName(),
            this.basicIntegrationService
        );
    Integration updatedIntegration =
        integrationService.updateIntegration(integration, updateRequest);
    updatedIntegration.setProject(project);
    integrationService.checkConnection(updatedIntegration);

    integrationRepository.save(updatedIntegration);

    publishUpdateActivity(user, beforeUpdate, updatedIntegration, project.getOrganizationId());

    return new OperationCompletionRS(
        "Integration with id = " + updatedIntegration.getId() + " has been successfully updated.");
  }

  private void validateGlobalIntegrationName(String integrationName,
      IntegrationType type) {
    BusinessRule.expect(integrationName, StringUtils::isNotBlank)
        .verify(ErrorType.INCORRECT_INTEGRATION_NAME, "Integration name should be not empty");
    boolean integrationExists =
        integrationRepository.existsByNameIgnoreCaseAndTypeIdAndProjectIdIsNull(integrationName, type.getId());
    BusinessRule.expect(integrationExists, BooleanUtils::isFalse).
        verify(ErrorType.INTEGRATION_ALREADY_EXISTS,
            Suppliers.formattedSupplier("Global integration of type = '{}' with name = '{}' already exists",
                type.getName(), integrationName)
        );
  }

  private void validateProjectIntegrationName(String integrationName,
      IntegrationType integrationType, Project project) {
    BusinessRule.expect(integrationName, StringUtils::isNotBlank)
        .verify(ErrorType.INCORRECT_INTEGRATION_NAME, "Integration name should be not empty");
    BusinessRule.expect(
        integrationRepository.existsByNameIgnoreCaseAndTypeIdAndProjectId(integrationName,
            integrationType.getId(), project.getId()
        ), BooleanUtils::isFalse).verify(ErrorType.INTEGRATION_ALREADY_EXISTS,
        Suppliers.formattedSupplier(
            "Project integration of type = '{}' with name = '{}' already exists on project = '{}'",
            integrationType.getName(), integrationName, project.getName()
        )
    );
  }

  private void publishUpdateActivity(ReportPortalUser user,
      IntegrationActivityResource beforeUpdate, Integration updatedIntegration, Long orgId) {
    eventPublisher.publishEvent(
        new IntegrationUpdatedEvent(user.getUserId(), user.getUsername(), beforeUpdate,
            TO_ACTIVITY_RESOURCE.apply(updatedIntegration), orgId
        ));
  }

  private void publishCreationActivity(Integration integration, ReportPortalUser user) {
    var orgId =
        integration.getProject() != null ? integration.getProject().getOrganizationId() : null;
    eventPublisher.publishEvent(
        new IntegrationCreatedEvent(TO_ACTIVITY_RESOURCE.apply(integration), user.getUserId(),
            user.getUsername(), orgId
        ));
  }

}
