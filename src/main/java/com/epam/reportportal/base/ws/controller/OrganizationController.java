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

import static com.epam.reportportal.base.auth.permissions.Permissions.IS_ADMIN;
import static com.epam.reportportal.base.auth.permissions.Permissions.ORGANIZATION_MANAGER;
import static com.epam.reportportal.base.auth.permissions.Permissions.ORGANIZATION_MEMBER;
import static com.google.common.net.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.data.domain.Sort.Direction.ASC;

import com.epam.reportportal.api.OrganizationsApi;
import com.epam.reportportal.api.model.CreateOrganizationRequest;
import com.epam.reportportal.api.model.OrganizationInfo;
import com.epam.reportportal.api.model.OrganizationPage;
import com.epam.reportportal.api.model.OrganizationSettings;
import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.reportportal.api.model.UpdateOrganizationRequest;
import com.epam.reportportal.base.core.filter.SearchCriteriaService;
import com.epam.reportportal.base.core.jasper.ReportFormat;
import com.epam.reportportal.base.core.jasper.impl.OrganizationJasperReportHandler;
import com.epam.reportportal.base.core.organization.GetOrganizationHandler;
import com.epam.reportportal.base.core.organization.OrganizationExtensionPoint;
import com.epam.reportportal.base.core.organization.patch.PatchOrganizationHandler;
import com.epam.reportportal.base.core.organization.settings.OrganizationSettingsHandler;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationFilter;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.ControllerUtils;
import com.epam.reportportal.base.util.SecurityContextUtils;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Controller for managing organizations in ReportPortal. Provides endpoints for creating, updating, retrieving, and
 * deleting organizations.
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class OrganizationController extends BaseController implements OrganizationsApi {

  private final GetOrganizationHandler getOrganizationHandler;
  private final SearchCriteriaService searchCriteriaService;
  private final Pf4jPluginBox pluginBox;
  private final OrganizationSettingsHandler organizationSettingsHandler;
  private final OrganizationJasperReportHandler organizationReportHandler;
  private final HttpServletResponse httpServletResponse;
  private final PatchOrganizationHandler patchOrganizationHandler;

  @Transactional(readOnly = true)
  @PreAuthorize(ORGANIZATION_MEMBER)
  @Override
  public ResponseEntity<OrganizationInfo> getOrganizationsOrgId(Long orgId) {
    return ResponseEntity.ok().body(getOrganizationHandler.getOrganizationById(orgId));
  }

  @Transactional(readOnly = true)
  @Override
  public ResponseEntity<OrganizationPage> getOrganizations(
      Integer offset,
      Integer limit,
      String order,
      String name,
      String slug,
      String sort
  ) {
    Filter filter = new Filter(OrganizationFilter.class, Lists.newArrayList());

    if (StringUtils.isNotEmpty(name)) {
      filter.withCondition(new FilterCondition(Condition.CONTAINS, false, name, "name"));
    }
    if (StringUtils.isNotEmpty(slug)) {
      filter.withCondition(new FilterCondition(Condition.EQUALS, false, slug, "slug"));
    }

    var pageable = ControllerUtils.getPageable(sort, order, offset, limit);

    return ResponseEntity.ok().body(getOrganizationHandler.getOrganizations(filter, pageable));
  }


  @Transactional(readOnly = true)
  @Override
  public ResponseEntity<OrganizationPage> postOrganizationsSearches(String accept, SearchCriteriaRQ criteriaRq) {
    Queryable filter = searchCriteriaService.createFilterBySearchCriteria(criteriaRq, OrganizationFilter.class);

    var pageable = ControllerUtils.getPageable(
        StringUtils.isNotBlank(criteriaRq.getSort()) ? criteriaRq.getSort() : "name",
        criteriaRq.getOrder() != null ? criteriaRq.getOrder().toString() : ASC.toString(),
        criteriaRq.getOffset(),
        criteriaRq.getLimit());

    if (isExportFormat(accept)) {
      if (!SecurityContextUtils.isAdminRole()) {
        throw new AccessDeniedException("Only administrators allowed to export users");
      }
      ReportFormat format = organizationReportHandler.getReportFormat(accept);
      try (OutputStream outputStream = httpServletResponse.getOutputStream()) {
        httpServletResponse.setContentType("text/csv");
        httpServletResponse.setHeader(CONTENT_DISPOSITION,
            String.format("attachment; filename=\"RP_ORGANIZATIONS_%s_Report.%s\"", ReportFormat.CSV.name(),
                ReportFormat.CSV.getValue()));
        outputStream.write(getOrganizationHandler.exportOrganizations(filter, pageable, format, outputStream));
        return null;
      } catch (IOException e) {
        log.error(e.getMessage(), e);
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Unable to write data to the response.");
      }
    } else {
      return ResponseEntity.ok().body(getOrganizationHandler.getOrganizations(filter, pageable));
    }
  }

  @Override
  @PreAuthorize(IS_ADMIN)
  @Transactional
  public ResponseEntity<OrganizationInfo> postOrganizations(CreateOrganizationRequest request) {
    if (CreateOrganizationRequest.TypeEnum.EXTERNAL == request.getType()) {
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST,
          "Creating organizations with type 'EXTERNAL' is not allowed.");
    }
    var principal = SecurityContextUtils.getPrincipal();
    var org = getOrgExtension().createOrganization(request, principal);
    var location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{orgId}")
        .buildAndExpand(org.getId())
        .toUri();
    return ResponseEntity.created(location).body(org);
  }

  @Override
  @PreAuthorize(ORGANIZATION_MANAGER)
  @Transactional
  public ResponseEntity<SuccessfulUpdate> putOrganizationsOrgId(Long orgId, UpdateOrganizationRequest request) {
    var principal = SecurityContextUtils.getPrincipal();
    getOrgExtension().updateOrganization(orgId, request, principal);
    return ResponseEntity.ok(new SuccessfulUpdate());
  }

  @Override
  @PreAuthorize(IS_ADMIN)
  @Transactional
  public ResponseEntity<Void> deleteOrganizationsOrgId(Long orgId) {
    var principal = SecurityContextUtils.getPrincipal();
    getOrgExtension().deleteOrganization(orgId, principal);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  @PreAuthorize(ORGANIZATION_MEMBER)
  @Transactional(readOnly = true)
  public ResponseEntity<OrganizationSettings> getOrgSettingsByOrgId(Long orgId) {
    return ResponseEntity.ok(organizationSettingsHandler.getOrganizationSettings(orgId));
  }

  @Override
  @PreAuthorize(ORGANIZATION_MANAGER)
  @Transactional
  public ResponseEntity<SuccessfulUpdate> updateOrgSettingsByOrgId(
      Long orgId,
      OrganizationSettings organizationSettings
  ) {
    organizationSettingsHandler.updateOrgSettings(orgId, organizationSettings);
    return ResponseEntity.ok(new SuccessfulUpdate());
  }

  @Override
  @PreAuthorize(ORGANIZATION_MANAGER)
  @Transactional
  public ResponseEntity<SuccessfulUpdate> patchOrganizationOrgId(
      Long orgId,
      List<PatchOperation> patchOperations
  ) {
    patchOrganizationHandler.patchOrganization(patchOperations, orgId);
    return ResponseEntity.ok().body(new SuccessfulUpdate());
  }

  private OrganizationExtensionPoint getOrgExtension() {
    return pluginBox.getInstance(OrganizationExtensionPoint.class)
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.PAID_PLUGIN_REQUIRED,
            "Organization", "Organization management is unavailable."
        ));
  }

}
