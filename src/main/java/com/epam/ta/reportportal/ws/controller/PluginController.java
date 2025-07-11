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

import static com.epam.reportportal.extension.util.CommandParamUtils.ENTITY_PARAM;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ImportFinishedEvent;
import com.epam.ta.reportportal.core.integration.ExecuteIntegrationHandler;
import com.epam.ta.reportportal.core.integration.plugin.CreatePluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.DeletePluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.GetPluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.UpdatePluginHandler;
import com.epam.ta.reportportal.model.EntryCreatedRS;
import com.epam.ta.reportportal.model.integration.IntegrationTypeResource;
import com.epam.ta.reportportal.model.integration.UpdatePluginStateRQ;
import com.epam.ta.reportportal.model.launch.LaunchImportRQ;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@RestController
@RequestMapping(value = "/v1/plugin")
@RequiredArgsConstructor
@Tag(name = "Plugin", description = "Plugins API collection")
public class PluginController {

  private final CreatePluginHandler createPluginHandler;
  private final UpdatePluginHandler updatePluginHandler;
  private final GetPluginHandler getPluginHandler;
  private final DeletePluginHandler deletePluginHandler;
  private final ExecuteIntegrationHandler executeIntegrationHandler;
  private final ProjectExtractor projectExtractor;
  private final MessageBus messageBus;

  @Transactional
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Upload new ReportPortal plugin")
  @PreAuthorize(IS_ADMIN)
  public EntryCreatedRS uploadPlugin(@NotNull @RequestParam("file") MultipartFile pluginFile,
      @AuthenticationPrincipal ReportPortalUser user) {
    return createPluginHandler.uploadPlugin(pluginFile, user);
  }

  @Transactional
  @PutMapping(value = "/{pluginId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Update ReportPortal plugin state")
  @PreAuthorize(IS_ADMIN)
  public OperationCompletionRS updatePluginState(@PathVariable(value = "pluginId") Long id,
      @RequestBody @Valid UpdatePluginStateRQ updatePluginStateRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updatePluginHandler.updatePluginState(id, updatePluginStateRQ, user);
  }

  @Transactional(readOnly = true)
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Get all available plugins")
  public List<IntegrationTypeResource> getPlugins(@AuthenticationPrincipal ReportPortalUser user) {
    return getPluginHandler.getPlugins();
  }

  @Transactional
  @DeleteMapping(value = "/{pluginId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Delete plugin by id")
  @PreAuthorize(IS_ADMIN)
  public OperationCompletionRS deletePlugin(@PathVariable(value = "pluginId") Long id,
      @AuthenticationPrincipal ReportPortalUser user) {
    return deletePluginHandler.deleteById(id, user);
  }

  @Transactional
  @PutMapping(
      value = "{projectKey}/{pluginName}/common/{command}",
      consumes = {APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Execute command to the plugin instance")
  public Object executePluginCommand(@PathVariable String projectKey,
      @PathVariable("pluginName") String pluginName, @PathVariable("command") String command,
      @RequestBody Map<String, Object> executionParams,
      @AuthenticationPrincipal ReportPortalUser user) {
    return executeIntegrationHandler.executeCommand(
        projectExtractor.extractMembershipDetails(user, projectKey), pluginName, command,
        executionParams
    );
  }

  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @PostMapping(value = "/{projectKey}/{pluginName}/import", consumes = {
      MediaType.MULTIPART_FORM_DATA_VALUE})
  @ResponseStatus(OK)
  @Operation(summary = "Send report to the specified plugin for importing")
  public Object executeImportPluginCommand(@AuthenticationPrincipal ReportPortalUser user,
      @PathVariable String projectKey, @PathVariable String pluginName,
      @RequestParam("file") MultipartFile file,
      @RequestPart(required = false) @Valid LaunchImportRQ launchImportRq) {
    Map<String, Object> executionParams = new HashMap<>();
    executionParams.put("file", file);
    ofNullable(launchImportRq).ifPresentOrElse(
        rq -> executionParams.put(ENTITY_PARAM, launchImportRq),
        () -> executionParams.put(ENTITY_PARAM, new LaunchImportRQ())
    );
    var membershipDetails = projectExtractor.extractMembershipDetails(user, projectKey);
    var importResult = executeIntegrationHandler.executeCommand(
        membershipDetails, pluginName, "import",
        executionParams);
    messageBus.publishActivity(new ImportFinishedEvent(user.getUserId(),
        user.getUsername(),
        membershipDetails.getProjectId(),
        file.getOriginalFilename(),
        membershipDetails.getOrgId()
    ));
    return importResult;
  }
}
