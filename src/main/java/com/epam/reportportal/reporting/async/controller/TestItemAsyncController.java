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

package com.epam.reportportal.reporting.async.controller;


import static com.epam.reportportal.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.epam.reportportal.core.item.FinishTestItemHandler;
import com.epam.reportportal.core.item.StartTestItemHandler;
import com.epam.reportportal.core.logging.HttpLogging;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.reporting.EntryCreatedAsyncRS;
import com.epam.reportportal.reporting.FinishTestItemRQ;
import com.epam.reportportal.reporting.OperationCompletionRS;
import com.epam.reportportal.reporting.StartTestItemRQ;
import com.epam.reportportal.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller implementation for async reporting client API for
 * {@link com.epam.reportportal.infrastructure.persistence.entity.item.TestItem} entity
 * <p>
 *
 * @author Konstantin Antipin
 */
@RestController
@RequestMapping("/v2/{projectKey}/item")
@Tag(name = "Test Item Async", description = "Test Items Async API collection")
public class TestItemAsyncController {

  private final ProjectExtractor projectExtractor;
  private final StartTestItemHandler startTestItemHandler;
  private final FinishTestItemHandler finishTestItemHandler;

  @Autowired
  public TestItemAsyncController(ProjectExtractor projectExtractor,
      @Qualifier("itemStartProducer") StartTestItemHandler startTestItemHandler,
      @Qualifier("itemFinishProducer") FinishTestItemHandler finishTestItemHandler) {
    this.projectExtractor = projectExtractor;
    this.startTestItemHandler = startTestItemHandler;
    this.finishTestItemHandler = finishTestItemHandler;
  }

  @HttpLogging
  @PostMapping(value = {"", "/"})
  @ResponseStatus(CREATED)
  @Operation(description = "Start a root test item")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public EntryCreatedAsyncRS startRootItem(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @RequestBody @Validated StartTestItemRQ startTestItemRQ) {
    return startTestItemHandler.startRootItem(user,
        projectExtractor.extractMembershipDetails(user, projectKey), startTestItemRQ);
  }

  @HttpLogging
  @PostMapping(value = {"/{parentItem}", "/{parentItem}/"})
  @ResponseStatus(CREATED)
  @Operation(description = "Start a child test item")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public EntryCreatedAsyncRS startChildItem(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @PathVariable String parentItem, @RequestBody @Validated StartTestItemRQ startTestItemRQ) {
    return startTestItemHandler.startChildItem(user,
        projectExtractor.extractMembershipDetails(user, projectKey), startTestItemRQ, parentItem);
  }

  @HttpLogging
  @PutMapping(value = {"/{testItemId}", "/{testItemId}/"})
  @ResponseStatus(OK)
  @Operation(description = "Finish test item")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public OperationCompletionRS finishTestItem(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @PathVariable String testItemId, @RequestBody @Validated FinishTestItemRQ finishExecutionRQ) {
    return finishTestItemHandler.finishTestItem(user,
        projectExtractor.extractMembershipDetails(user, projectKey), testItemId, finishExecutionRQ);
  }

}
