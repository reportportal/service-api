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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.integration.CreateIntegrationHandler;
import com.epam.ta.reportportal.core.integration.DeleteIntegrationHandler;
import com.epam.ta.reportportal.core.integration.ExecuteIntegrationHandler;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.model.EntryCreatedRS;
import com.epam.ta.reportportal.model.integration.IntegrationRQ;
import com.epam.ta.reportportal.model.integration.IntegrationResource;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
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
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@RestController
@RequestMapping(value = "/v1/integration")
@Tag(name = "Integration", description = "Integrations API collection")
public class IntegrationController {

  private final ProjectExtractor projectExtractor;
  private final DeleteIntegrationHandler deleteIntegrationHandler;
  private final GetIntegrationHandler getIntegrationHandler;
  private final CreateIntegrationHandler createIntegrationHandler;
  private final ExecuteIntegrationHandler executeIntegrationHandler;

  @Autowired
  public IntegrationController(ProjectExtractor projectExtractor,
      DeleteIntegrationHandler deleteIntegrationHandler,
      GetIntegrationHandler getIntegrationHandler,
      CreateIntegrationHandler createIntegrationHandler,
      ExecuteIntegrationHandler executeIntegrationHandler) {
    this.projectExtractor = projectExtractor;
    this.deleteIntegrationHandler = deleteIntegrationHandler;
    this.getIntegrationHandler = getIntegrationHandler;
    this.createIntegrationHandler = createIntegrationHandler;
    this.executeIntegrationHandler = executeIntegrationHandler;
  }

  @Transactional(readOnly = true)
  @GetMapping({"/global/all", "/global/all/"}) // TODO: fix on UI side and remove the second one
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Get available global integrations")
  public List<IntegrationResource> getGlobalIntegrations(
      @AuthenticationPrincipal ReportPortalUser reportPortalUser) {
    return getIntegrationHandler.getGlobalIntegrations();
  }

  @Transactional(readOnly = true)
  @GetMapping("/global/all/{pluginName}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Get available global integrations for plugin")
  public List<IntegrationResource> getGlobalIntegrations(
      @AuthenticationPrincipal ReportPortalUser reportPortalUser, @PathVariable String pluginName) {
    return getIntegrationHandler.getGlobalIntegrations(pluginName);
  }

  @Transactional(readOnly = true)
  @GetMapping("/project/{projectKey}/all")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Get available project integrations")
  public List<IntegrationResource> getProjectIntegrations(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser reportPortalUser) {
    return getIntegrationHandler.getProjectIntegrations(normalizeId(projectKey));
  }

  @Transactional(readOnly = true)
  @GetMapping("/project/{projectKey}/all/{pluginName}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Get available project integrations for plugin")
  public List<IntegrationResource> getProjectIntegrations(
      @AuthenticationPrincipal ReportPortalUser reportPortalUser, @PathVariable String projectKey,
      @PathVariable String pluginName) {
    return getIntegrationHandler.getProjectIntegrations(pluginName, normalizeId(projectKey));
  }

  @Transactional
  @PostMapping(value = "/{pluginName}")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create global ReportPortal integration instance")
  @PreAuthorize(IS_ADMIN)
  public EntryCreatedRS createGlobalIntegration(@RequestBody @Valid IntegrationRQ createRequest,
      @PathVariable String pluginName, @AuthenticationPrincipal ReportPortalUser user) {
    return createIntegrationHandler.createGlobalIntegration(createRequest, pluginName, user);
  }

  @Transactional
  @PostMapping(value = "/{projectKey}/{pluginName}")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create project ReportPortal integration instance")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public EntryCreatedRS createProjectIntegration(@RequestBody @Valid IntegrationRQ createRequest,
      @PathVariable String pluginName, @PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {
    return createIntegrationHandler.createProjectIntegration(normalizeId(projectKey),
        createRequest, pluginName, user
    );

  }

  @Transactional(readOnly = true)
  @GetMapping(value = "{projectKey}/{integrationId}/connection/test")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Test connection to the integration through the project config")
  public boolean testIntegrationConnection(@PathVariable Long integrationId,
      @PathVariable String projectKey, @AuthenticationPrincipal ReportPortalUser user) {
    return getIntegrationHandler.testConnection(integrationId, normalizeId(projectKey));
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/{integrationId}/connection/test")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(IS_ADMIN)
  @Operation(summary = "Test connection to the global integration")
  public boolean testIntegrationConnection(@PathVariable Long integrationId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getIntegrationHandler.testConnection(integrationId);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/{integrationId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Get global ReportPortal integration instance")
  @PreAuthorize(IS_ADMIN)
  public IntegrationResource getGlobalIntegration(@PathVariable Long integrationId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getIntegrationHandler.getGlobalIntegrationById(integrationId);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/{projectKey}/{integrationId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Get integration instance")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public IntegrationResource getProjectIntegration(@PathVariable String projectKey,
      @PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {
    return getIntegrationHandler.getProjectIntegrationById(integrationId, normalizeId(projectKey));
  }

  @Transactional
  @PutMapping(value = "/{integrationId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Update global ReportPortal integration instance")
  @PreAuthorize(IS_ADMIN)
  public OperationCompletionRS updateGlobalIntegration(@PathVariable Long integrationId,
      @RequestBody @Valid IntegrationRQ updateRequest,
      @AuthenticationPrincipal ReportPortalUser user) {
    return createIntegrationHandler.updateGlobalIntegration(integrationId, updateRequest, user);

  }

  @Transactional
  @PutMapping(value = "/{projectKey}/{integrationId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Update project integration instance")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public OperationCompletionRS updateProjectIntegration(@PathVariable Long integrationId,
      @RequestBody @Valid IntegrationRQ updateRequest, @PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {
    return createIntegrationHandler.updateProjectIntegration(integrationId,
        normalizeId(projectKey), updateRequest, user
    );

  }

  @Transactional
  @DeleteMapping(value = "/{integrationId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Delete global integration instance")
  @PreAuthorize(IS_ADMIN)
  public OperationCompletionRS deleteGlobalIntegration(@PathVariable Long integrationId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return deleteIntegrationHandler.deleteGlobalIntegration(integrationId, user);
  }

  @Transactional
  @DeleteMapping(value = "/all/{type}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Delete all global integrations by type")
  @PreAuthorize(IS_ADMIN)
  public OperationCompletionRS deleteAllIntegrations(@PathVariable String type,
      @AuthenticationPrincipal ReportPortalUser user) {
    return deleteIntegrationHandler.deleteGlobalIntegrationsByType(type, user);
  }

  @Transactional
  @DeleteMapping(value = "/{projectKey}/{integrationId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Delete project integration instance")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public OperationCompletionRS deleteProjectIntegration(@PathVariable String projectKey,
      @PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {
    return deleteIntegrationHandler.deleteProjectIntegration(integrationId,
        normalizeId(projectKey), user
    );
  }

  @Transactional
  @DeleteMapping(value = "/{projectKey}/all/{type}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Delete all integrations assigned to specified project")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public OperationCompletionRS deleteAllProjectIntegrations(@PathVariable String type,
      @PathVariable String projectKey, @AuthenticationPrincipal ReportPortalUser user) {
    return deleteIntegrationHandler.deleteProjectIntegrationsByType(type, normalizeId(projectKey),
        user
    );
  }

  @Transactional
  @PutMapping(value = "{projectKey}/{integrationId}/{command}", consumes = {
      APPLICATION_JSON_VALUE })
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Operation(summary = "Execute command to the integration instance")
  public Object executeIntegrationCommand(@PathVariable String projectKey,
      @PathVariable("integrationId") Long integrationId, @PathVariable("command") String command,
      @RequestBody Map<String, Object> executionParams,
      @AuthenticationPrincipal ReportPortalUser user) {
    return executeIntegrationHandler.executeCommand(
        projectExtractor.extractMembershipDetails(user, projectKey), integrationId, command,
        executionParams
    );
  }

}
