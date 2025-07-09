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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.widget.CreateWidgetHandler;
import com.epam.ta.reportportal.core.widget.GetWidgetHandler;
import com.epam.ta.reportportal.core.widget.UpdateWidgetHandler;
import com.epam.ta.reportportal.model.EntryCreatedRS;
import com.epam.ta.reportportal.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.model.widget.WidgetRQ;
import com.epam.ta.reportportal.model.widget.WidgetResource;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Pavel Bortnik
 */
@RestController
@RequestMapping("/v1/{projectKey}/widget")
@Tag(name = "Widget", description = "Widgets API collection")
public class WidgetController {

  private final ProjectExtractor projectExtractor;
  private final CreateWidgetHandler createWidgetHandler;
  private final UpdateWidgetHandler updateWidgetHandler;
  private final GetWidgetHandler getWidgetHandler;

  @Autowired
  public WidgetController(ProjectExtractor projectExtractor,
      CreateWidgetHandler createWidgetHandler, UpdateWidgetHandler updateWidgetHandler,
      GetWidgetHandler getWidgetHandler) {
    this.projectExtractor = projectExtractor;
    this.createWidgetHandler = createWidgetHandler;
    this.updateWidgetHandler = updateWidgetHandler;
    this.getWidgetHandler = getWidgetHandler;
  }

  @Transactional
  @PostMapping
  @ResponseStatus(CREATED)
  @Operation(summary = "Create a new widget")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public EntryCreatedRS createWidget(@RequestBody @Validated WidgetRQ createWidget,
      @AuthenticationPrincipal ReportPortalUser user, @PathVariable String projectKey) {
    return createWidgetHandler.createWidget(
        createWidget, projectExtractor.extractMembershipDetails(user, projectKey), user);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/{widgetId}")
  @ResponseStatus(OK)
  @Operation(summary = "Get widget by ID")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public WidgetResource getWidget(@PathVariable String projectKey, @PathVariable Long widgetId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getWidgetHandler.getWidget(
        widgetId, projectExtractor.extractMembershipDetails(user, projectKey), user);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "multilevel/{widgetId}")
  @ResponseStatus(OK)
  @Operation(summary = "Get multilevel widget by ID")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public WidgetResource getWidget(@PathVariable String projectKey, @PathVariable Long widgetId,
      @RequestParam(required = false, name = "attributes") String[] attributes,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getWidgetHandler.getWidget(
        widgetId, ArrayUtils.nullToEmpty(attributes), params,
        projectExtractor.extractMembershipDetails(user, projectKey), user
    );
  }

  @Transactional(readOnly = true)
  @PostMapping(value = "/preview")
  @ResponseStatus(OK)
  @Operation(summary = "Get widget preview")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public Map<String, ?> getWidgetPreview(@PathVariable String projectKey,
      @RequestBody @Validated WidgetPreviewRQ previewRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getWidgetHandler.getWidgetPreview(
        previewRQ, projectExtractor.extractMembershipDetails(user, normalizeId(projectKey)), user);
  }

  @Transactional
  @PutMapping(value = "/{widgetId}")
  @ResponseStatus(OK)
  @Operation(summary = "Update specified widget")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public OperationCompletionRS updateWidget(@PathVariable String projectKey,
      @PathVariable Long widgetId, @RequestBody @Validated WidgetRQ updateRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateWidgetHandler.updateWidget(
        widgetId, updateRQ, projectExtractor.extractMembershipDetails(user, projectKey), user);
  }
}
