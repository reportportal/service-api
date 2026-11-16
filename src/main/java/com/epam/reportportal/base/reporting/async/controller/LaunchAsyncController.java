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

package com.epam.reportportal.base.reporting.async.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.epam.reportportal.base.core.launch.FinishLaunchHandler;
import com.epam.reportportal.base.core.launch.MergeLaunchHandler;
import com.epam.reportportal.base.core.launch.StartLaunchHandler;
import com.epam.reportportal.base.core.launch.util.LinkGenerator;
import com.epam.reportportal.base.core.logging.HttpLogging;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.model.launch.FinishLaunchRS;
import com.epam.reportportal.base.reporting.FinishExecutionRQ;
import com.epam.reportportal.base.reporting.LaunchResource;
import com.epam.reportportal.base.reporting.LaunchResourceOld;
import com.epam.reportportal.base.reporting.MergeLaunchesRQ;
import com.epam.reportportal.base.reporting.StartLaunchRQ;
import com.epam.reportportal.base.reporting.StartLaunchRS;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.base.ws.converter.converters.LaunchConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller implementation for async reporting client API for {@link Launch} entity
 *
 * @author Pavel Bortnik
 */
@RestController
@RequestMapping("/v2/{projectKey}/launch")
@Tag(name = "Launch Async", description = "Launches Async API collection")
public class LaunchAsyncController {

  private final ProjectExtractor projectExtractor;
  private final StartLaunchHandler startLaunchHandler;
  private final FinishLaunchHandler finishLaunchHandler;
  private final MergeLaunchHandler mergeLaunchesHandler;
  private final LaunchConverter launchConverter;
  private final LinkGenerator linkGenerator;

  @Autowired
  public LaunchAsyncController(ProjectExtractor projectExtractor,
      @Qualifier("launchStartProducer") StartLaunchHandler startLaunchHandler,
      @Qualifier("launchFinishProducer") FinishLaunchHandler finishLaunchHandler,
      MergeLaunchHandler mergeLaunchesHandler, LaunchConverter launchConverter, LinkGenerator linkGenerator) {

    this.projectExtractor = projectExtractor;
    this.startLaunchHandler = startLaunchHandler;
    this.finishLaunchHandler = finishLaunchHandler;
    this.mergeLaunchesHandler = mergeLaunchesHandler;
    this.launchConverter = launchConverter;
    this.linkGenerator = linkGenerator;
  }

  @HttpLogging
  @PostMapping
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @ResponseStatus(CREATED)
  @Operation(summary = "Starts launch for specified project")
  public StartLaunchRS startLaunch(@PathVariable String projectKey,
      @Parameter(description = "Start launch request body", required = true) @RequestBody @Validated StartLaunchRQ startLaunchRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return startLaunchHandler.startLaunch(user,
        projectExtractor.extractMembershipDetails(user, normalizeId(projectKey)), startLaunchRQ);
  }

  @HttpLogging
  @PutMapping(value = "/{launchId}/finish")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @ResponseStatus(OK)
  @Operation(description = "Finish launch for specified project")
  public FinishLaunchRS finishLaunch(@PathVariable String projectKey,
      @PathVariable String launchId,
      @RequestBody @Validated FinishExecutionRQ finishLaunchRQ,
      @AuthenticationPrincipal ReportPortalUser user,
      HttpServletRequest request) {
    return finishLaunchHandler.finishLaunch(
        launchId,
        finishLaunchRQ,
        projectExtractor.extractMembershipDetails(user, normalizeId(projectKey)),
        user,
        linkGenerator.composeBaseUrl(request)
    );
  }

  @HttpLogging
  @Transactional
  @PostMapping(value = "/merge", produces = "application/x.reportportal.launch.v2+json")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @ResponseStatus(OK)
  @Operation(summary = "Merge set of specified launches in common one", description =
      "This operation merges a set of launches into a common one. "
          + "The IDs of the launches to be merged should be provided in the 'launches' "
          + "field of the request body.")
  public LaunchResource mergeLaunches(@PathVariable String projectKey,
      @Parameter(description = "Merge launches request body", required = true) @RequestBody @Validated
      MergeLaunchesRQ mergeLaunchesRQ, @AuthenticationPrincipal ReportPortalUser user) {
    return mergeLaunchesHandler.mergeLaunches(
        projectExtractor.extractMembershipDetails(user, normalizeId(projectKey)), user,
        mergeLaunchesRQ
    );
  }

  @HttpLogging
  @Transactional
  @PostMapping(value = "/merge")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @ResponseStatus(OK)
  @Operation(summary = "Merge set of specified launches in common one", description =
      "This operation merges a set of launches into a common one. "
          + "The IDs of the launches to be merged should be provided in the 'launches' "
          + "field of the request body.")
  @ApiResponse(
      responseCode = "200",
      description = "Successful response with dates in timestamp format. "
          + "Response with dates in ISO-8601 format if the custom header "
          + "'Accept: application/x.reportportal.launch.v2+json' is used.",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = LaunchResourceOld.class))
  )
  public LaunchResourceOld mergeLaunchesOldUuid(@PathVariable String projectKey,
      @Parameter(description = "Merge launches request body", required = true) @RequestBody
      @Validated MergeLaunchesRQ mergeLaunchesRQ, @AuthenticationPrincipal ReportPortalUser user) {
    var launchResource = mergeLaunchesHandler.mergeLaunches(
        projectExtractor.extractMembershipDetails(user, normalizeId(projectKey)), user,
        mergeLaunchesRQ
    );
    return launchConverter.TO_RESOURCE_OLD.apply(launchResource);
  }


}
