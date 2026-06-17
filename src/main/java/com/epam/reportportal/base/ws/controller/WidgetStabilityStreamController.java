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
 * WITHOUT WARRANTIES OR ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.ws.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers.formattedSupplier;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.core.widget.GetWidgetHandler;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetType;
import com.epam.reportportal.base.model.widget.WidgetResource;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.HttpStatus;

/**
 * SSE progress + final table payload for Test stability (flakiness) widget.
 */
@RestController
@PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
@RequestMapping("/v1/{projectName}/widget")
@Tag(name = "Widget", description = "Widgets API collection")
public class WidgetStabilityStreamController {

  private static final long SSE_TIMEOUT_MS = 900_000L;

  private final ProjectExtractor projectExtractor;
  private final GetWidgetHandler getWidgetHandler;
  private final ObjectMapper objectMapper;

  public WidgetStabilityStreamController(ProjectExtractor projectExtractor,
      GetWidgetHandler getWidgetHandler, ObjectMapper objectMapper) {
    this.projectExtractor = projectExtractor;
    this.getWidgetHandler = getWidgetHandler;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/{widgetId}/stability-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Stream test stability (flakiness) widget content (progress + result)")
  public SseEmitter stabilityStream(@PathVariable String projectName, @PathVariable Long widgetId,
      @AuthenticationPrincipal ReportPortalUser user) {
    SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
    var projectDetails = projectExtractor.extractProjectDetails(user, projectName);
    try {
      emitter.send(SseEmitter.event()
          .name("progress")
          .data("{\"percent\":5,\"phase\":\"start\"}"));
      WidgetResource widget = getWidgetHandler.getWidget(widgetId, projectDetails, user);
      if (!WidgetType.TEST_STABILITY_FLAKINESS.getType().equals(widget.getWidgetType())) {
        throw new ReportPortalException(ErrorType.INCORRECT_REQUEST,
            formattedSupplier("Widget {} is not testStabilityFlakiness", widgetId)
        );
      }
      emitter.send(SseEmitter.event()
          .name("progress")
          .data("{\"percent\":50,\"phase\":\"loaded\"}"));
      String body = objectMapper.writeValueAsString(widget.getContent() != null
          ? widget.getContent() : java.util.Collections.emptyMap());
      emitter.send(SseEmitter.event().name("complete").data(body));
      emitter.complete();
    } catch (java.io.IOException e) {
      emitter.completeWithError(e);
    } catch (ReportPortalException e) {
      try {
        emitter.completeWithError(e);
      } catch (Exception ignored) {
        // ignore
      }
    } catch (Exception e) {
      emitter.completeWithError(e);
    }
    return emitter;
  }
}
