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

import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.settings.ServerSettingsService;
import com.epam.ta.reportportal.model.settings.AnalyticsResource;
import com.epam.ta.reportportal.model.settings.UpdateSettingsRq;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Andrei_Ramanchuk
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@RestController
@RequestMapping("/v1/settings")
@PreAuthorize(IS_ADMIN)
@Tag(name = "Settings", description = "Settings API collection")
public class SettingsController {

  private final ServerSettingsService serverHandler;

  @Autowired
  public SettingsController(ServerSettingsService serverHandler) {
    this.serverHandler = serverHandler;
  }

  @Transactional
  @RequestMapping(value = "/analytics", method = {RequestMethod.PUT, RequestMethod.POST})
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Update analytics settings")
  public OperationCompletionRS saveAnalyticsSettings(
      @RequestBody @Validated AnalyticsResource request,
      @AuthenticationPrincipal ReportPortalUser user) {
    return serverHandler.saveAnalyticsSettings(request);
  }

  @Transactional
  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Update server settings with specified key")
  public OperationCompletionRS updateServerSettings(
      @RequestBody @Validated UpdateSettingsRq request,
      @AuthenticationPrincipal ReportPortalUser user) {
    return serverHandler.updateServerSettings(request, user);
  }

  @Transactional(readOnly = true)
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Get server settings")
  public Map<String, String> getServerSettings(@AuthenticationPrincipal ReportPortalUser user) {
    return serverHandler.getServerSettings();
  }
}
