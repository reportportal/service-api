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

package com.epam.reportportal.base.core.organization.impl;

import static com.epam.reportportal.base.util.OffsetUtils.responseWithPageParameters;
import static com.epam.reportportal.base.ws.converter.converters.IntegrationConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.reportportal.base.ws.converter.converters.IntegrationConverter.TO_ORGANIZATION_INTEGRATION;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.OrganizationIntegration;
import com.epam.reportportal.api.model.OrganizationIntegrationPage;
import com.epam.reportportal.base.core.events.domain.IntegrationCreatedEvent;
import com.epam.reportportal.base.core.events.domain.IntegrationDeletedEvent;
import com.epam.reportportal.base.core.events.domain.IntegrationUpdatedEvent;
import com.epam.reportportal.base.core.integration.util.IntegrationService;
import com.epam.reportportal.base.core.organization.OrganizationIntegrationHandler;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.EntryCreatedRS;
import com.epam.reportportal.base.model.activity.IntegrationActivityResource;
import com.epam.reportportal.base.model.integration.IntegrationRQ;
import com.epam.reportportal.base.util.SecurityContextUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
@Service
public class OrganizationIntegrationHandlerImpl implements OrganizationIntegrationHandler {

  private final IntegrationRepository integrationRepository;
  private final IntegrationTypeRepository integrationTypeRepository;
  private final OrganizationRepositoryCustom organizationRepository;
  private final Map<String, IntegrationService> integrationServiceMapping;
  private final IntegrationService basicIntegrationService;
  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public OrganizationIntegrationHandlerImpl(
      IntegrationRepository integrationRepository,
      IntegrationTypeRepository integrationTypeRepository,
      OrganizationRepositoryCustom organizationRepository,
      @Qualifier("integrationServiceMapping") Map<String, IntegrationService> integrationServiceMapping,
      @Qualifier("basicIntegrationServiceImpl") IntegrationService basicIntegrationService,
      ApplicationEventPublisher eventPublisher) {
    this.integrationRepository = integrationRepository;
    this.integrationTypeRepository = integrationTypeRepository;
    this.organizationRepository = organizationRepository;
    this.integrationServiceMapping = integrationServiceMapping;
    this.basicIntegrationService = basicIntegrationService;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public OrganizationIntegrationPage getOrganizationIntegrations(Long orgId, Pageable pageable) {
    validateOrganizationExists(orgId);
    Page<Integration> page = integrationRepository.findAllByOrganizationId(orgId, pageable);
    List<OrganizationIntegration> items = page.getContent().stream()
        .map(TO_ORGANIZATION_INTEGRATION)
        .toList();
    OrganizationIntegrationPage result = new OrganizationIntegrationPage();
    result.setItems(items);
    return responseWithPageParameters(result, pageable, page.getTotalElements());
  }

  @Override
  public OrganizationIntegration getOrganizationIntegrationById(Long orgId, Long integrationId) {
    validateOrganizationExists(orgId);
    Integration integration = integrationRepository.findByIdAndOrganizationId(integrationId, orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));
    return TO_ORGANIZATION_INTEGRATION.apply(integration);
  }

  @Override
  public EntryCreatedRS createOrganizationIntegration(Long orgId, String pluginName, IntegrationRQ createRequest) {
    validateOrganizationExists(orgId);

    IntegrationType integrationType = integrationTypeRepository.findByName(pluginName)
        .orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, pluginName));

    String integrationName = ofNullable(createRequest.getName())
        .map(name -> {
          validateOrgIntegrationName(name, integrationType, orgId);
          return name;
        })
        .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_INTEGRATION_NAME,
            "Integration name should be not null"));
    createRequest.setName(integrationName);

    IntegrationService integrationService =
        integrationServiceMapping.getOrDefault(integrationType.getName(), basicIntegrationService);

    var user = SecurityContextUtils.getPrincipal();

    Integration integration = integrationService.createIntegration(createRequest, integrationType);
    integration.setOrganizationId(orgId);
    integration.setCreator(user.getUsername());
    integrationService.checkConnection(integration);
    integrationRepository.save(integration);

    eventPublisher.publishEvent(new IntegrationCreatedEvent(TO_ACTIVITY_RESOURCE.apply(integration),
        user.getUserId(), user.getUsername(), orgId));

    return new EntryCreatedRS(integration.getId());
  }

  @Override
  public void updateOrganizationIntegration(Long orgId, Long integrationId, IntegrationRQ updateRequest) {
    validateOrganizationExists(orgId);
    var user = SecurityContextUtils.getPrincipal();

    Integration integration = integrationRepository.findByIdAndOrganizationId(integrationId, orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));

    IntegrationActivityResource beforeUpdate = TO_ACTIVITY_RESOURCE.apply(integration);

    ofNullable(updateRequest.getName()).ifPresent(name -> {
      if (!name.equals(integration.getName())) {
        validateOrgIntegrationName(name, integration.getType(), orgId);
      }
      updateRequest.setName(name);
    });

    IntegrationService integrationService =
        integrationServiceMapping.getOrDefault(integration.getType().getName(), basicIntegrationService);

    Integration updatedIntegration = integrationService.updateIntegration(integration, updateRequest);
    integrationService.checkConnection(updatedIntegration);
    integrationRepository.save(updatedIntegration);

    eventPublisher.publishEvent(
        new IntegrationUpdatedEvent(user.getUserId(), user.getUsername(), beforeUpdate,
            TO_ACTIVITY_RESOURCE.apply(updatedIntegration), orgId));

  }

  @Override
  public void deleteOrganizationIntegration(Long orgId, Long integrationId) {
    validateOrganizationExists(orgId);
    var user = SecurityContextUtils.getPrincipal();

    Integration integration = integrationRepository.findByIdAndOrganizationId(integrationId, orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));

    integrationRepository.deleteById(integration.getId());

    eventPublisher.publishEvent(
        new IntegrationDeletedEvent(TO_ACTIVITY_RESOURCE.apply(integration),
            user.getUserId(), user.getUsername(), orgId));
  }

  @Override
  public void deleteOrganizationIntegrationsByType(Long orgId, String pluginName) {
    validateOrganizationExists(orgId);
    var user = SecurityContextUtils.getPrincipal();

    IntegrationType integrationType = integrationTypeRepository.findByName(pluginName)
        .orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, pluginName));

    List<Integration> integrations = integrationRepository.findAllByOrganizationId(orgId, Pageable.unpaged())
        .stream()
        .filter(i -> i.getType().getId().equals(integrationType.getId()))
        .toList();

    integrations.stream()
        .map(TO_ACTIVITY_RESOURCE)
        .forEach(it ->
            eventPublisher.publishEvent(new IntegrationDeletedEvent(it, user.getUserId(), user.getUsername(), orgId)));

    integrationRepository.deleteAllByOrganizationIdAndTypeId(orgId, integrationType.getId());

  }

  private void validateOrganizationExists(Long orgId) {
    organizationRepository.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));
  }

  private void validateOrgIntegrationName(String integrationName,
      IntegrationType integrationType, Long orgId) {
    BusinessRule.expect(integrationName, StringUtils::isNotBlank)
        .verify(ErrorType.INCORRECT_INTEGRATION_NAME, "Integration name should be not empty");
    BusinessRule.expect(integrationRepository.existsByNameIgnoreCaseAndTypeIdAndOrganizationId(integrationName,
            integrationType.getId(), orgId), BooleanUtils::isFalse)
        .verify(ErrorType.INTEGRATION_ALREADY_EXISTS, Suppliers.formattedSupplier(
                "Organization integration of type = '{}' with name = '{}' already exists in organization = '{}'",
                integrationType.getName(), integrationName, orgId
            )
        );
  }
}
