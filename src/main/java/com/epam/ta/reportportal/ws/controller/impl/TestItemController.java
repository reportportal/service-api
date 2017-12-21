/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.ws.controller.impl;

import com.epam.ta.reportportal.core.item.*;
import com.epam.ta.reportportal.core.item.history.TestItemsHistoryHandler;
import com.epam.ta.reportportal.core.item.merge.MergeTestItemHandler;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.search.CompositeFilter;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.Queryable;
import com.epam.ta.reportportal.ws.controller.ITestItemController;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.item.AddExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.MergeTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;
import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping("/{projectName}/item")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class TestItemController implements ITestItemController {

	public static final String DEFAULT_HISTORY_DEPTH = "5";
	public static final String DEFAULT_HISTORY_FULL = "true";

	@Autowired
	private StartTestItemHandler startTestItemHandler;

	@Autowired
	private DeleteTestItemHandler deleteTestItemHandler;

	@Autowired
	private FinishTestItemHandler finishTestItemHandler;

	@Autowired
	private GetTestItemHandler getTestItemHandler;

	@Autowired
	private TestItemsHistoryHandler testItemsHistoryHandler;

	@Autowired
	private UpdateTestItemHandler updateTestItemHandler;

	@Autowired
	private MergeTestItemHandler mergeTestItemHandler;

	@Override
	@PostMapping
	@ResponseBody
	@ResponseStatus(CREATED)
	@ApiOperation("Start a root test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS startRootItem(@PathVariable String projectName, @RequestBody @Validated StartTestItemRQ startTestItemRQ,
			Principal principal) {
		return startTestItemHandler.startRootItem(projectName, startTestItemRQ);
	}

	@Override
	@PostMapping("/{parentItem}")
	@ResponseBody
	@ResponseStatus(CREATED)
	@ApiOperation("Start a child test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS startChildItem(@PathVariable String projectName, @PathVariable String parentItem,
			@RequestBody @Validated StartTestItemRQ startTestItemRQ, Principal principal) {
		return startTestItemHandler.startChildItem(projectName, startTestItemRQ, parentItem);
	}

	@Override
	@PutMapping("/{testItemId}")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Finish test item")
	@PreAuthorize(ALLOWED_TO_REPORT)
	public OperationCompletionRS finishTestItem(@PathVariable String projectName, @PathVariable String testItemId,
			@RequestBody @Validated FinishTestItemRQ finishExecutionRQ, Principal principal) {
		return finishTestItemHandler.finishTestItem(testItemId, finishExecutionRQ, principal.getName());
	}

	@Override
	@GetMapping("/{testItemId}")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Find test item by ID")
	public TestItemResource getTestItem(@PathVariable String projectName, @PathVariable String testItemId, Principal principal) {
		return getTestItemHandler.getTestItem(testItemId);
	}

	@Override
	@GetMapping
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Find test items by specified filter")
	public Iterable<TestItemResource> getTestItems(@PathVariable String projectName, @FilterFor(TestItem.class) Filter filter,
			@FilterFor(TestItem.class) Queryable predefinedFilter, @SortFor(TestItem.class) Pageable pageable, Principal principal) {
		return getTestItemHandler.getTestItems(new CompositeFilter(filter, predefinedFilter), pageable);
	}

	@DeleteMapping("/{item}")
	@ResponseBody
	@ResponseStatus(OK)
	@Override
	@ApiOperation("Delete test item")
	public OperationCompletionRS deleteTestItem(@PathVariable String projectName, @PathVariable String item, Principal principal) {
		return deleteTestItemHandler.deleteTestItem(item, normalizeId(projectName), principal.getName(), false);
	}

	@Override
	@ResponseBody
	@ResponseStatus(OK)
	@DeleteMapping
	@ApiOperation("Delete test items by specified ids")
	public List<OperationCompletionRS> deleteTestItems(@PathVariable String projectName, @RequestParam(value = "ids") String[] ids,
			Principal principal) {
		return deleteTestItemHandler.deleteTestItem(ids, normalizeId(projectName), principal.getName());
	}

	@Override
	@PutMapping
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Update issues of specified test items")
	public List<Issue> defineTestItemIssueType(@PathVariable String projectName, @RequestBody @Validated DefineIssueRQ request,
			Principal principal) {
		return updateTestItemHandler.defineTestItemsIssues(normalizeId(projectName), request, principal.getName());
	}

	@Override
	@GetMapping("/history")
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Load history of test items")
	public List<TestItemHistoryElement> getItemsHistory(@PathVariable String projectName,
			@RequestParam(value = "history_depth", required = false, defaultValue = DEFAULT_HISTORY_DEPTH) int historyDepth,
			@RequestParam(value = "ids") String[] ids,
			@RequestParam(value = "is_full", required = false, defaultValue = DEFAULT_HISTORY_FULL) boolean showBrokenLaunches,
			Principal principal) {
		return testItemsHistoryHandler.getItemsHistory(normalizeId(projectName), ids, historyDepth, showBrokenLaunches);
	}

	@Override
	@GetMapping("/tags")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get all unique tags of specified launch")
	public List<String> getAllTags(@PathVariable String projectName, @RequestParam(value = "launch") String id,
			@RequestParam(value = FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.CNT + Launch.TAGS) String value,
			Principal principal) {
		return getTestItemHandler.getTags(id, value);
	}

	@Override
	@PutMapping("/{item}/update")
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation("Update test item")
	public OperationCompletionRS updateTestItem(@PathVariable String projectName, @PathVariable String item,
			@RequestBody @Validated UpdateTestItemRQ rq, Principal principal) {
		return updateTestItemHandler.updateTestItem(normalizeId(projectName), item, rq, principal.getName());
	}

	@Override
	@PutMapping("/issue/add")
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation("Attach external issue for specified test items")
	public List<OperationCompletionRS> addExternalIssues(@PathVariable String projectName, @RequestBody @Validated AddExternalIssueRQ rq,
			Principal principal) {
		return updateTestItemHandler.addExternalIssues(normalizeId(projectName), rq, principal.getName());
	}

	@Override
	@GetMapping("/items")
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation("Get test items by specified ids")
	public List<TestItemResource> getTestItems(@PathVariable String projectName, @RequestParam(value = "ids") String[] ids,
			Principal principal) {
		return getTestItemHandler.getTestItems(ids);
	}

	@Override
	@PutMapping("/{item}/merge")
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	//    @ApiOperation("Merge test item")
	@ApiIgnore
	public OperationCompletionRS mergeTestItem(@PathVariable String projectName, @PathVariable String item,
			@RequestBody @Validated MergeTestItemRQ rq, Principal principal) {
		return mergeTestItemHandler.mergeTestItem(normalizeId(projectName), item, rq, principal.getName());
	}
}
