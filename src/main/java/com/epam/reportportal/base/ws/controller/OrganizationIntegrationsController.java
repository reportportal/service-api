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

package com.epam.reportportal.base.ws.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ORGANIZATION_MANAGER;
import static com.epam.reportportal.base.auth.permissions.Permissions.ORGANIZATION_MEMBER;
import static com.epam.reportportal.base.util.ControllerUtils.getPageable;
import static com.epam.reportportal.base.ws.converter.converters.IntegrationConverter.CREATE_REQUEST_TO_RQ;
import static com.epam.reportportal.base.ws.converter.converters.IntegrationConverter.UPDATE_REQUEST_TO_RQ;

import com.epam.reportportal.api.OrganizationIntegrationsApi;
import com.epam.reportportal.api.model.CreateOrgIntegrationRequest;
import com.epam.reportportal.api.model.OrganizationIntegration;
import com.epam.reportportal.api.model.OrganizationIntegrationPage;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.reportportal.api.model.UpdateOrgIntegrationRequest;
import com.epam.reportportal.base.core.organization.OrganizationIntegrationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
@RestController
@RequiredArgsConstructor
public class OrganizationIntegrationsController implements OrganizationIntegrationsApi {

  private static final String DEFAULT_SORT = "name";

  private final OrganizationIntegrationHandler organizationIntegrationHandler;

  @Transactional
  @PreAuthorize(ORGANIZATION_MANAGER)
  @Override
  public ResponseEntity<OrganizationIntegration> createOrgIntegration(Long orgId,
      CreateOrgIntegrationRequest createOrgIntegrationRequest) {
    var created = organizationIntegrationHandler.createOrganizationIntegration(
        orgId, createOrgIntegrationRequest.getPluginId(),
        CREATE_REQUEST_TO_RQ.apply(createOrgIntegrationRequest));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(organizationIntegrationHandler.getOrganizationIntegrationById(orgId, created.getId()));
  }

  @Transactional
  @PreAuthorize(ORGANIZATION_MANAGER)
  @Override
  public ResponseEntity<Void> deleteOrgIntegrationById(Long orgId, Long integrationId) {
    organizationIntegrationHandler.deleteOrganizationIntegration(orgId, integrationId);
    return ResponseEntity.noContent().build();
  }

  @Transactional(readOnly = true)
  @PreAuthorize(ORGANIZATION_MEMBER)
  @Override
  public ResponseEntity<OrganizationIntegration> getOrgIntegrationById(Long orgId, Long integrationId) {
    return ResponseEntity.ok(
        organizationIntegrationHandler.getOrganizationIntegrationById(orgId, integrationId));
  }

  @Transactional(readOnly = true)
  @PreAuthorize(ORGANIZATION_MEMBER)
  @Override
  public ResponseEntity<OrganizationIntegrationPage> getOrgIntegrations(Long orgId, Integer offset, Integer limit,
      String order) {
    return ResponseEntity.ok(organizationIntegrationHandler
        .getOrganizationIntegrations(orgId, getPageable(DEFAULT_SORT, order, offset, limit)));
  }

  @Transactional
  @PreAuthorize(ORGANIZATION_MANAGER)
  @Override
  public ResponseEntity<SuccessfulUpdate> updateOrgIntegrationById(Long orgId, Long integrationId,
      UpdateOrgIntegrationRequest updateOrgIntegrationRequest) {
    organizationIntegrationHandler.updateOrganizationIntegration(orgId, integrationId,
        UPDATE_REQUEST_TO_RQ.apply(updateOrgIntegrationRequest));
    return ResponseEntity.ok(new SuccessfulUpdate());
  }
}
