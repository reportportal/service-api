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
import com.epam.ta.reportportal.commons.querygen.CompositeFilter;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.*;
import com.epam.ta.reportportal.core.item.history.TestItemsHistoryHandler;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.item.LinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UnlinkExternalIssueRq;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.ApiOperation;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.*;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.ItemAttributeConstant.CRITERIA_ITEM_ATTRIBUTE_KEY;
import static com.epam.ta.reportportal.commons.querygen.constant.ItemAttributeConstant.CRITERIA_ITEM_ATTRIBUTE_VALUE;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver.DEFAULT_FILTER_PREFIX;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * Controller implementation for
 * {@link com.epam.ta.reportportal.entity.item.TestItem} entity
 * <p>
 */
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

	@Autowired
	public TestItemController(StartTestItemHandler startTestItemHandler, DeleteTestItemHandler deleteTestItemHandler,
			FinishTestItemHandler finishTestItemHandler, UpdateTestItemHandler updateTestItemHandler, GetTestItemHandler getTestItemHandler,
			TestItemsHistoryHandler testItemsHistoryHandler) {
		this.startTestItemHandler = startTestItemHandler;
		this.deleteTestItemHandler = deleteTestItemHandler;
		this.finishTestItemHandler = finishTestItemHandler;
		this.updateTestItemHandler = updateTestItemHandler;
		this.getTestItemHandler = getTestItemHandler;
		this.testItemsHistoryHandler = testItemsHistoryHandler;
	}

	/* Report client API */

	@Transactional
	@PostMapping
	@ResponseStatus(CREATED)
	@ApiOperation("Start a root test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedAsyncRS startRootItem(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@RequestBody @Validated StartTestItemRQ startTestItemRQ) {
		return startTestItemHandler.startRootItem(user, extractProjectDetails(user, projectName), startTestItemRQ);
	}

	@Transactional
	@PostMapping("/{parentItem}")
	@ResponseStatus(CREATED)
	@ApiOperation("Start a child test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedAsyncRS startChildItem(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@PathVariable String parentItem, @RequestBody @Validated StartTestItemRQ startTestItemRQ) {
		return startTestItemHandler.startChildItem(user, extractProjectDetails(user, projectName), startTestItemRQ, parentItem);
	}

	@Transactional
	@PutMapping("/{testItemId}")
	@ResponseStatus(OK)
	@ApiOperation("Finish test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public OperationCompletionRS finishTestItem(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@PathVariable String testItemId, @RequestBody @Validated FinishTestItemRQ finishExecutionRQ) {
		return finishTestItemHandler.finishTestItem(user, extractProjectDetails(user, projectName), testItemId, finishExecutionRQ);
	}


	/* Frontend API */

	@Transactional(readOnly = true)
	@GetMapping("/{itemId}")
	@ResponseStatus(OK)
	@ApiOperation("Find test item by ID")
	public TestItemResource getTestItem(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@PathVariable Long itemId) {
		return getTestItemHandler.getTestItem(itemId, extractProjectDetails(user, projectName), user);

	}

	//TODO check pre-defined filter
	@Transactional(readOnly = true)
	@GetMapping
	@ResponseStatus(OK)
	@ApiOperation("Find test items by specified filter")
	public Iterable<TestItemResource> getTestItems(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@RequestParam(value = DEFAULT_FILTER_PREFIX + Condition.EQ + CRITERIA_LAUNCH_ID) Long launchId,
			@FilterFor(TestItem.class) Filter filter, @FilterFor(TestItem.class) Queryable predefinedFilter,
			@SortFor(TestItem.class) Pageable pageable) {
		return getTestItemHandler.getTestItems(
				new CompositeFilter(Operator.AND, filter, predefinedFilter),
				pageable,
				extractProjectDetails(user, projectName),
				user,
				launchId
		);
	}

	@Transactional
	@DeleteMapping("/{itemId}")
	@ResponseStatus(OK)
	@ApiOperation("Delete test item")
	public OperationCompletionRS deleteTestItem(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@PathVariable Long itemId) {
		return deleteTestItemHandler.deleteTestItem(itemId, extractProjectDetails(user, projectName), user);
	}

	@Transactional
	@DeleteMapping
	@ResponseStatus(OK)
	@ApiOperation("Delete test items by specified ids")
	public List<OperationCompletionRS> deleteTestItems(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@RequestParam(value = "ids") Long[] ids) {
		return deleteTestItemHandler.deleteTestItem(ids, extractProjectDetails(user, projectName), user);
	}

	@Transactional
	@PutMapping
	@ResponseStatus(OK)
	@ApiOperation("Update issues of specified test items")
	public List<Issue> defineTestItemIssueType(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@RequestBody @Validated DefineIssueRQ request) {
		return updateTestItemHandler.defineTestItemsIssues(extractProjectDetails(user, projectName), request, user);
	}

	@Transactional(readOnly = true)
	@GetMapping("/history")
	@ResponseStatus(OK)
	@ApiOperation("Load history of test items")
	public List<TestItemHistoryElement> getItemsHistory(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@RequestParam(value = "history_depth", required = false, defaultValue = "5") int historyDepth,
			@RequestParam(value = "ids") Long[] ids,
			@RequestParam(value = "is_full", required = false, defaultValue = "false") boolean showBrokenLaunches) {
		return testItemsHistoryHandler.getItemsHistory(extractProjectDetails(user, projectName), ids, historyDepth, showBrokenLaunches);
	}

	@Transactional(readOnly = true)
	@GetMapping("/ticket/ids")
	@ResponseStatus(OK)
	@ApiOperation("Get all unique attribute keys of specified launch")
	public List<String> getTicketIds(@AuthenticationPrincipal ReportPortalUser user, @PathVariable String projectName,
			@RequestParam(value = "launch") Long id, @RequestParam(value = "term") String term) {
		return getTestItemHandler.getTicketIds(id, normalizeId(term));
	}

	@Transactional(readOnly = true)
	@GetMapping("/attribute/keys")
	@ResponseStatus(OK)
	@ApiOperation("Get all unique attribute keys of specified launch")
	public List<String> getAttributeKeys(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@RequestParam(value = "launch") Long id,
			@RequestParam(value = DEFAULT_FILTER_PREFIX + Condition.CNT + CRITERIA_ITEM_ATTRIBUTE_KEY) String value) {
		return getTestItemHandler.getAttributeKeys(id, value);
	}

	@Transactional(readOnly = true)
	@GetMapping("/attribute/values")
	@ResponseStatus(OK)
	@ApiOperation("Get all unique attribute values of specified launch")
	public List<String> getAttributeValues(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@RequestParam(value = "launch") Long id,
			@RequestParam(value = DEFAULT_FILTER_PREFIX + Condition.EQ + CRITERIA_ITEM_ATTRIBUTE_KEY, required = false) String key,
			@RequestParam(value = DEFAULT_FILTER_PREFIX + Condition.CNT + CRITERIA_ITEM_ATTRIBUTE_VALUE) String value) {
		return getTestItemHandler.getAttributeValues(id, key, value);
	}

	@Transactional
	@PutMapping(value = "/info")
	@PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
	@ResponseStatus(OK)
	@ApiOperation("Bulk update attributes and description")
	public OperationCompletionRS bulkUpdate(@PathVariable String projectName, @RequestBody @Validated BulkInfoUpdateRQ bulkInfoUpdateRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateTestItemHandler.bulkInfoUpdate(bulkInfoUpdateRQ, extractProjectDetails(user, projectName));
	}

	@Transactional
	@PutMapping("/{itemId}/update")
	@ResponseStatus(OK)
	@ApiOperation("Update test item")
	public OperationCompletionRS updateTestItem(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@PathVariable Long itemId, @RequestBody @Validated UpdateTestItemRQ rq) {
		return updateTestItemHandler.updateTestItem(extractProjectDetails(user, projectName), itemId, rq, user);
	}

	@Transactional
	@PutMapping("/issue/link")
	@ResponseStatus(OK)
	@ApiOperation("Attach external issue for specified test items")
	public List<OperationCompletionRS> addExternalIssues(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@RequestBody @Validated LinkExternalIssueRQ rq) {
		return updateTestItemHandler.linkExternalIssues(extractProjectDetails(user, projectName), rq, user);
	}

	@Transactional
	@PutMapping("/issue/unlink")
	@ResponseStatus(OK)
	@ApiOperation("Unlink external issue for specified test items")
	public List<OperationCompletionRS> unlinkExternalIssues(@PathVariable String projectName,
			@AuthenticationPrincipal ReportPortalUser user, @RequestBody @Validated UnlinkExternalIssueRq rq) {
		return updateTestItemHandler.unlinkExternalIssues(extractProjectDetails(user, projectName), rq, user);
	}

	@Transactional(readOnly = true)
	@GetMapping("/items")
	@ResponseStatus(OK)
	@ApiOperation("Get test items by specified ids")
	public List<TestItemResource> getTestItems(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user,
			@RequestParam(value = "ids") Long[] ids) {
		return getTestItemHandler.getTestItems(ids, extractProjectDetails(user, projectName), user);
	}
}
