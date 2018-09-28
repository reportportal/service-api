/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.item.*;
import com.epam.ta.reportportal.core.item.history.TestItemsHistoryHandler;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.item.LinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.MergeTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.UnlinkExternalIssueRq;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/{projectName}/item")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class TestItemController {

	private final StartTestItemHandler startTestItemHandler;
	private final DeleteTestItemHandler deleteTestItemHandler;
	private final FinishTestItemHandler finishTestItemHandler;
	private final UpdateTestItemHandler updateTestItemHandler;
	private final GetTestItemHandler getTestItemHandler;
	private final TestItemsHistoryHandler testItemsHistoryHandler;
	private final MergeTestItemHandler mergeTestItemHandler;

	@Autowired
	public TestItemController(StartTestItemHandler startTestItemHandler, DeleteTestItemHandler deleteTestItemHandler,
			FinishTestItemHandler finishTestItemHandler, UpdateTestItemHandler updateTestItemHandler, GetTestItemHandler getTestItemHandler,
			TestItemsHistoryHandler testItemsHistoryHandler, MergeTestItemHandler mergeTestItemHandler) {
		this.startTestItemHandler = startTestItemHandler;
		this.deleteTestItemHandler = deleteTestItemHandler;
		this.finishTestItemHandler = finishTestItemHandler;
		this.updateTestItemHandler = updateTestItemHandler;
		this.getTestItemHandler = getTestItemHandler;
		this.testItemsHistoryHandler = testItemsHistoryHandler;
		this.mergeTestItemHandler = mergeTestItemHandler;
	}

	@Transactional
	@PostMapping
	@ResponseStatus(CREATED)
	@ApiOperation("Start a root test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS startRootItem(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestBody @Validated StartTestItemRQ startTestItemRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return startTestItemHandler.startRootItem(user, projectDetails, startTestItemRQ);
	}

	@Transactional(readOnly = true)
	@GetMapping("/{itemId}")
	@ResponseStatus(OK)
	@ApiOperation("Find test item by ID")
	public TestItemResource getTestItem(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @PathVariable Long itemId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getTestItemHandler.getTestItem(itemId, projectDetails, user);

	}

	@Transactional
	@PostMapping("/{parentItem}")
	@ResponseStatus(CREATED)
	@ApiOperation("Start a child test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS startChildItem(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @PathVariable Long parentItem,
			@RequestBody @Validated StartTestItemRQ startTestItemRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return startTestItemHandler.startChildItem(user, projectDetails, startTestItemRQ, parentItem);
	}

	@Transactional
	@PutMapping("/{testItemId}")
	@ResponseStatus(OK)
	@ApiOperation("Finish test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public OperationCompletionRS finishTestItem(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@PathVariable Long testItemId, @RequestBody @Validated FinishTestItemRQ finishExecutionRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return finishTestItemHandler.finishTestItem(user, projectDetails, testItemId, finishExecutionRQ);
	}

	//TODO check pre-defined filter
	@Transactional(readOnly = true)
	@GetMapping
	@ResponseStatus(OK)
	@ApiOperation("Find test items by specified filter")
	public Iterable<TestItemResource> getTestItems(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@FilterFor(TestItem.class) Filter filter, @FilterFor(TestItem.class) Filter predefinedFilter,
			@SortFor(TestItem.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return getTestItemHandler.getTestItems(new Filter(filter, predefinedFilter), pageable, projectDetails, user);
	}

	@Transactional
	@DeleteMapping("/{itemId}")
	@ResponseStatus(OK)
	@ApiOperation("Delete test item")
	public OperationCompletionRS deleteTestItem(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @PathVariable Long itemId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteTestItemHandler.deleteTestItem(itemId, projectDetails, user);
	}

	@Transactional
	@DeleteMapping
	@ResponseStatus(OK)
	@ApiOperation("Delete test items by specified ids")
	public List<OperationCompletionRS> deleteTestItems(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestParam(value = "ids") Long[] ids, @AuthenticationPrincipal ReportPortalUser user) {
		return deleteTestItemHandler.deleteTestItem(ids, projectDetails, user);
	}

	@Transactional
	@PutMapping
	@ResponseStatus(OK)
	@ApiOperation("Update issues of specified test items")
	public List<Issue> defineTestItemIssueType(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestBody @Validated DefineIssueRQ request, @AuthenticationPrincipal ReportPortalUser user) {
		return updateTestItemHandler.defineTestItemsIssues(projectDetails, request, user);
	}

	@Transactional(readOnly = true)
	@GetMapping("/history")
	@ResponseStatus(OK)
	@ApiOperation("Load history of test items")
	public List<TestItemHistoryElement> getItemsHistory(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestParam(value = "history_depth", required = false, defaultValue = "5") int historyDepth,
			@RequestParam(value = "ids") Long[] ids,
			@RequestParam(value = "is_full", required = false, defaultValue = "") boolean showBrokenLaunches,
			@AuthenticationPrincipal ReportPortalUser user) {
		return testItemsHistoryHandler.getItemsHistory(projectDetails, ids, historyDepth, showBrokenLaunches);
	}

	@Transactional(readOnly = true)
	@GetMapping("/tags")
	@ResponseStatus(OK)
	@ApiOperation("Get all unique tags of specified launch")
	public List<String> getAllTags(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @RequestParam(value = "launch") Long id,
			@RequestParam(value = "filter." + "cnt." + "tags") String value, @AuthenticationPrincipal ReportPortalUser user) {
		return getTestItemHandler.getTags(id, value, projectDetails, user);
	}

	@Transactional
	@PutMapping("/{itemId}/update")
	@ResponseStatus(OK)
	@ApiOperation("Update test item")
	public OperationCompletionRS updateTestItem(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @PathVariable Long itemId,
			@RequestBody @Validated UpdateTestItemRQ rq, @AuthenticationPrincipal ReportPortalUser user) {
		return updateTestItemHandler.updateTestItem(projectDetails, itemId, rq, user);
	}

	@Transactional
	@PutMapping("/issue/link")
	@ResponseStatus(OK)
	@ApiOperation("Attach external issue for specified test items")
	public List<OperationCompletionRS> addExternalIssues(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestBody @Validated LinkExternalIssueRQ rq, @AuthenticationPrincipal ReportPortalUser user) {
		return updateTestItemHandler.linkExternalIssues(projectDetails, rq, user);
	}

	@Transactional
	@PutMapping("/issue/unlink")
	@ResponseStatus(OK)
	@ApiOperation("Unlink external issue for specified test items")
	public List<OperationCompletionRS> unlinkExternalIssues(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestBody @Validated UnlinkExternalIssueRq rq, @AuthenticationPrincipal ReportPortalUser user) {
		return updateTestItemHandler.unlinkExternalIssues(projectDetails, rq, user);
	}

	@Transactional(readOnly = true)
	@GetMapping("/items")
	@ResponseStatus(OK)
	@ApiOperation("Get test items by specified ids")
	public List<TestItemResource> getTestItems(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestParam(value = "ids") Long[] ids, @AuthenticationPrincipal ReportPortalUser user) {
		return getTestItemHandler.getTestItems(ids, projectDetails, user);
	}

	@Transactional
	@PutMapping("/{item}/merge")
	@ResponseStatus(OK)
	@ApiOperation("Merge test item")
	public OperationCompletionRS mergeTestItem(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @PathVariable Long item,
			@RequestBody @Validated MergeTestItemRQ rq, @AuthenticationPrincipal ReportPortalUser user) {
		return mergeTestItemHandler.mergeTestItem(projectDetails, item, rq, user.getUsername());
	}

	@ModelAttribute
	private ReportPortalUser.ProjectDetails projectDetails(@PathVariable String projectName,
			@AuthenticationPrincipal ReportPortalUser user) {
		return ProjectUtils.extractProjectDetails(user, normalizeId(projectName));
	}
}
