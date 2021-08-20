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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.logging.HttpLogging;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.EntryCreatedAsyncRS;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * Controller implementation for async reporting client API for
 * {@link com.epam.ta.reportportal.entity.item.TestItem} entity
 * <p>
 *
 * @author Konstantin Antipin
 */
@RestController
@RequestMapping("/v2/{projectName}/item")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class TestItemAsyncController {

	private final ProjectExtractor projectExtractor;
	private final StartTestItemHandler startTestItemHandler;
	private final FinishTestItemHandler finishTestItemHandler;

	@Autowired
	public TestItemAsyncController(ProjectExtractor projectExtractor, @Qualifier("startTestItemHandlerAsync") StartTestItemHandler startTestItemHandler,
			@Qualifier("finishTestItemHandlerAsync") FinishTestItemHandler finishTestItemHandler) {
		this.projectExtractor = projectExtractor;
		this.startTestItemHandler = startTestItemHandler;
		this.finishTestItemHandler = finishTestItemHandler;
	}

	@HttpLogging
	@PostMapping
	@ResponseStatus(CREATED)
	@ApiOperation("Start a root test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedAsyncRS startRootItem(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@RequestBody @Validated StartTestItemRQ startTestItemRQ) {
		return startTestItemHandler.startRootItem(user, projectExtractor.extractProjectDetails(user, projectName), startTestItemRQ);
	}

	@HttpLogging
	@PostMapping("/{parentItem}")
	@ResponseStatus(CREATED)
	@ApiOperation("Start a child test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedAsyncRS startChildItem(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@PathVariable String parentItem, @RequestBody @Validated StartTestItemRQ startTestItemRQ) {
		return startTestItemHandler.startChildItem(user, projectExtractor.extractProjectDetails(user, projectName), startTestItemRQ, parentItem);
	}

	@HttpLogging
	@PutMapping("/{testItemId}")
	@ResponseStatus(OK)
	@ApiOperation("Finish test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public OperationCompletionRS finishTestItem(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@PathVariable String testItemId, @RequestBody @Validated FinishTestItemRQ finishExecutionRQ) {
		return finishTestItemHandler.finishTestItem(user, projectExtractor.extractProjectDetails(user, projectName), testItemId, finishExecutionRQ);
	}

}
