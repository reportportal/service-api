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

package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.dashboard.CreateDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.DeleteDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.GetDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.UpdateDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.impl.DashboardPreconfiguredService;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.model.EntryCreatedRS;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.model.dashboard.AddWidgetRq;
import com.epam.ta.reportportal.model.dashboard.CreateDashboardRQ;
import com.epam.ta.reportportal.model.dashboard.DashboardConfigResource;
import com.epam.ta.reportportal.model.dashboard.DashboardPreconfiguredRq;
import com.epam.ta.reportportal.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.model.dashboard.UpdateDashboardRQ;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Pavel Bortnik
 */
@RequiredArgsConstructor
@RestController
@PreAuthorize(ASSIGNED_TO_PROJECT)
@RequestMapping("/v1/{projectName}/dashboard")
@Tag(name = "Dashboard", description = "Dashboards API collection")
public class DashboardController {

  private final ProjectExtractor projectExtractor;
  private final CreateDashboardHandler createDashboardHandler;
  private final DashboardPreconfiguredService dashboardPreconfiguredService;
  private final UpdateDashboardHandler updateDashboardHandler;
  private final GetDashboardHandler getDashboardHandler;
  private final DeleteDashboardHandler deleteDashboardHandler;

  @Transactional
  @PostMapping
  @ResponseStatus(CREATED)
  @Operation(summary = "Create dashboard for specified project")
  public EntryCreatedRS createDashboard(@PathVariable String projectName,
      @RequestBody @Validated CreateDashboardRQ createRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return createDashboardHandler.createDashboard(
        projectExtractor.extractProjectDetails(user, projectName), createRQ, user);
  }

  @Transactional(readOnly = true)
  @GetMapping
  @ResponseStatus(OK)
  @Operation(summary = "Get all permitted dashboard resources for specified project")
  public Page<DashboardResource> getAllDashboards(@PathVariable String projectName,
      @SortFor(Dashboard.class) Pageable pageable, @FilterFor(Dashboard.class) Filter filter,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getDashboardHandler.getDashboards(
        projectExtractor.extractProjectDetails(user, projectName), pageable, filter, user);
  }

  @Transactional
  @PutMapping("/{dashboardId}/add")
  @ResponseStatus(OK)
  @Operation(summary = "Add widget to specified dashboard")
  public OperationCompletionRS addWidget(@PathVariable String projectName,
      @PathVariable Long dashboardId, @RequestBody @Validated AddWidgetRq addWidgetRq,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateDashboardHandler.addWidget(
        dashboardId, projectExtractor.extractProjectDetails(user, projectName), addWidgetRq, user);
  }

  @Transactional
  @DeleteMapping("/{dashboardId}/{widgetId}")
  @ResponseStatus(OK)
  @Operation(summary = "Remove widget from specified dashboard")
  public OperationCompletionRS removeWidget(@PathVariable String projectName,
      @PathVariable Long dashboardId, @PathVariable Long widgetId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateDashboardHandler.removeWidget(
        widgetId, dashboardId, projectExtractor.extractProjectDetails(user, projectName), user);
  }

  @Transactional
  @PutMapping(value = "/{dashboardId}")
  @ResponseStatus(OK)
  @Operation(summary = "Update specified dashboard for specified project")
  public OperationCompletionRS updateDashboard(@PathVariable String projectName,
      @PathVariable Long dashboardId, @RequestBody @Validated UpdateDashboardRQ updateRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateDashboardHandler.updateDashboard(
        projectExtractor.extractProjectDetails(user, projectName), updateRQ, dashboardId, user);
  }

  @Transactional
  @DeleteMapping(value = "/{dashboardId}")
  @ResponseStatus(OK)
  @Operation(summary = "Delete specified dashboard by ID for specified project")
  public OperationCompletionRS deleteDashboard(@PathVariable String projectName,
      @PathVariable Long dashboardId, @AuthenticationPrincipal ReportPortalUser user) {
    return deleteDashboardHandler.deleteDashboard(
        dashboardId, projectExtractor.extractProjectDetails(user, projectName), user);
  }

  @Transactional
  @GetMapping(value = "/{dashboardId}")
  @ResponseStatus(OK)
  @Operation(summary = "Get specified dashboard by ID for specified project")
  public DashboardResource getDashboard(@PathVariable String projectName,
      @PathVariable Long dashboardId, @AuthenticationPrincipal ReportPortalUser user) {
    return getDashboardHandler.getDashboard(
        dashboardId, projectExtractor.extractProjectDetails(user, projectName));
  }


  @Transactional
  @GetMapping(value = "/{dashboardId}/config")
  @ResponseStatus(OK)
  @Operation(summary = "Get Dashboard configuration including its widgets and filters if any")
  public DashboardConfigResource getDashboardConfig(@PathVariable String projectName,
      @PathVariable Long dashboardId, @AuthenticationPrincipal ReportPortalUser user) {
    return getDashboardHandler.getDashboardConfig(
        dashboardId, projectExtractor.extractProjectDetails(user, projectName));
  }

  @Transactional
  @PostMapping(value = "/preconfigured")
  @ResponseStatus(OK)
  @Operation(summary = "Create Dashboard with provided configuration including its widgets and filters if any")
  public EntryCreatedRS createPreconfigured(@PathVariable String projectName,
      @RequestBody @Validated DashboardPreconfiguredRq rq,
      @AuthenticationPrincipal ReportPortalUser user) {
    return dashboardPreconfiguredService.createDashboard(
        projectExtractor.extractProjectDetails(user, projectName), rq, user);
  }
}
