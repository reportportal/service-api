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
import com.epam.ta.reportportal.core.item.*;
import com.epam.ta.reportportal.core.item.history.TestItemsHistoryHandler;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.item.LinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.MergeTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.UnlinkExternalIssueRq;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping("/{projectName}/item")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class TestItemController {

	public static final String DEFAULT_HISTORY_DEPTH = "5";
	public static final String DEFAULT_HISTORY_FULL = "true";

	private StartTestItemHandler startTestItemHandler;
	private DeleteTestItemHandler deleteTestItemHandler;
	private FinishTestItemHandler finishTestItemHandler;
	private UpdateTestItemHandler updateTestItemHandler;
	private GetTestItemHandler getTestItemHandler;
	private TestItemsHistoryHandler testItemsHistoryHandler;
	private MergeTestItemHandler mergeTestItemHandler;

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

	@PostMapping
	@ResponseBody
	@ResponseStatus(CREATED)
	@Transactional
	@ApiOperation("Start a root test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS startRootItem(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestBody @Validated StartTestItemRQ startTestItemRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return startTestItemHandler.startRootItem(user, projectDetails, startTestItemRQ);
	}

	@GetMapping("/{itemId}")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Find test item by ID")
	public TestItemResource getTestItem(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @PathVariable Long itemId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getTestItemHandler.getTestItem(itemId);

	}

	@PostMapping("/{parentItem}")
	@ResponseBody
	@ResponseStatus(CREATED)
	@Transactional
	@ApiOperation("Start a child test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS startChildItem(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @PathVariable Long parentItem,
			@RequestBody @Validated StartTestItemRQ startTestItemRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return startTestItemHandler.startChildItem(user, projectDetails, startTestItemRQ, parentItem);
	}

	@PutMapping("/{testItemId}")
	@ResponseBody
	@ResponseStatus(OK)
	@Transactional
	@ApiOperation("Finish test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public OperationCompletionRS finishTestItem(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@PathVariable Long testItemId, @RequestBody @Validated FinishTestItemRQ finishExecutionRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return finishTestItemHandler.finishTestItem(user, projectDetails, testItemId, finishExecutionRQ);
	}

	//	@GetMapping
	//	@ResponseBody
	//	@ResponseStatus(OK)
	//	@ApiOperation("Find test items by specified filter")
	//	public Iterable<TestItemResource> getTestItems(@PathVariable String projectName, @FilterFor(TestItem.class) Filter filter,
	//			@FilterFor(TestItem.class) Queryable predefinedFilter, @SortFor(TestItem.class) Pageable pageable,
	//			@AuthenticationPrincipal ReportPortalUser user) {
	//		return getTestItemHandler.getTestItems(new CompositeFilter(filter, predefinedFilter), pageable);
	//	}

	@DeleteMapping("/{itemId}")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Delete test item")
	public OperationCompletionRS deleteTestItem(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @PathVariable Long itemId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteTestItemHandler.deleteTestItem(itemId, projectDetails, user);
	}

	@DeleteMapping
	@ResponseBody
	@ResponseStatus(OK)
	@Transactional
	@ApiOperation("Delete test items by specified ids")
	public List<OperationCompletionRS> deleteTestItems(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestParam(value = "ids") Long[] ids, @AuthenticationPrincipal ReportPortalUser user) {
		return deleteTestItemHandler.deleteTestItem(ids, projectDetails, user);
	}

	@PutMapping
	@ResponseBody
	@ResponseStatus(OK)
	@Transactional
	@ApiOperation("Update issues of specified test items")
	public List<Issue> defineTestItemIssueType(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestBody @Validated DefineIssueRQ request, @AuthenticationPrincipal ReportPortalUser user) {
		return updateTestItemHandler.defineTestItemsIssues(projectDetails, request, user);
	}

	@GetMapping("/history")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Load history of test items")
	public List<TestItemHistoryElement> getItemsHistory(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestParam(value = "history_depth", required = false, defaultValue = DEFAULT_HISTORY_DEPTH) int historyDepth,
			@RequestParam(value = "ids") Long[] ids,
			@RequestParam(value = "is_full", required = false, defaultValue = DEFAULT_HISTORY_FULL) boolean showBrokenLaunches,
			@AuthenticationPrincipal ReportPortalUser user) {
		return testItemsHistoryHandler.getItemsHistory(projectDetails, ids, historyDepth, showBrokenLaunches);
	}

	@GetMapping("/tags")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get all unique tags of specified launch")
	public List<String> getAllTags(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @RequestParam(value = "launch") Long id,
			@RequestParam(value = "filter." + "cnt." + "tags") String value, @AuthenticationPrincipal ReportPortalUser user) {
		return getTestItemHandler.getTags(id, value);
	}

	@PutMapping("/{itemId}/update")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Update test item")
	public OperationCompletionRS updateTestItem(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails, @PathVariable Long itemId,
			@RequestBody @Validated UpdateTestItemRQ rq, @AuthenticationPrincipal ReportPortalUser user) {
		return updateTestItemHandler.updateTestItem(projectDetails, itemId, rq, user);
	}

	@PutMapping("/issue/link")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Attach external issue for specified test items")
	public List<OperationCompletionRS> addExternalIssues(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestBody @Validated LinkExternalIssueRQ rq, @AuthenticationPrincipal ReportPortalUser user) {
		return updateTestItemHandler.linkExternalIssues(projectDetails, rq, user);
	}

	@PutMapping("/issue/unlink")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Unlink external issue for specified test items")
	public List<OperationCompletionRS> unlinkExternalIssues(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestBody @Validated UnlinkExternalIssueRq rq, @AuthenticationPrincipal ReportPortalUser user) {
		return updateTestItemHandler.unlinkExternalIssues(projectDetails, rq, user);
	}

	@GetMapping("/items")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get test items by specified ids")
	public List<TestItemResource> getTestItems(@ModelAttribute ReportPortalUser.ProjectDetails projectDetails,
			@RequestParam(value = "ids") Long[] ids, @AuthenticationPrincipal ReportPortalUser user) {
		return getTestItemHandler.getTestItems(ids);
	}

	@PutMapping("/{item}/merge")
	@ResponseBody
	@ResponseStatus(OK)
	//    @ApiOperation("Merge test item")
	@ApiIgnore
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
