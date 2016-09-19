/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.security.Principal;
import java.util.List;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import springfox.documentation.annotations.ApiIgnore;


import com.epam.ta.reportportal.core.item.*;
import com.epam.ta.reportportal.core.item.history.TestItemsHistoryHandler;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.controller.ITestItemController;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.item.AddExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import io.swagger.annotations.ApiOperation;

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

	@Override
	@RequestMapping(method = POST)
	@ResponseBody
	@ResponseStatus(CREATED)
	@ApiOperation("Start a root test item")
	public EntryCreatedRS startRootItem(@PathVariable String projectName, @RequestBody @Validated StartTestItemRQ startTestItemRQ,
			Principal principal) {
		return startTestItemHandler.startRootItem(projectName, startTestItemRQ);
	}

	@Override
	@RequestMapping(value = "/{parentItem}", method = POST)
	@ResponseBody
	@ResponseStatus(CREATED)
	@ApiOperation("Start a child test item")
	public EntryCreatedRS startChildItem(@PathVariable String projectName, @PathVariable String parentItem,
			@RequestBody @Validated StartTestItemRQ startTestItemRQ, Principal principal) {
		return startTestItemHandler.startChildItem(startTestItemRQ, parentItem);
	}

	@Override
	@RequestMapping(value = "/{testItemId}", method = PUT)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Finish test item")
	public OperationCompletionRS finishTestItem(@PathVariable String projectName, @PathVariable String testItemId,
			@RequestBody @Validated FinishTestItemRQ finishExecutionRQ, Principal principal) {
		return finishTestItemHandler.finishTestItem(testItemId, finishExecutionRQ, principal.getName());
	}

	@Override
	@RequestMapping(value = "/{testItemId}", method = GET)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Find test item by ID")
	public TestItemResource getTestItem(@PathVariable String projectName, @PathVariable String testItemId, Principal principal) {
		return getTestItemHandler.getTestItem(testItemId);
	}

	@Override
	@RequestMapping(method = GET)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiIgnore
	@ApiOperation("Find test items by specified filter")
	public Iterable<TestItemResource> getTestItems(@PathVariable String projectName, @FilterFor(TestItem.class) Filter filter,
			@SortFor(TestItem.class) Pageable pageable, Principal principal) {
		return getTestItemHandler.getTestItems(filter, pageable);
	}

	@RequestMapping(value = "/{item}", method = DELETE)
	@ResponseBody
	@ResponseStatus(OK)
	@Override
	@ApiOperation("Delete test item")
	public OperationCompletionRS deleteTestItem(@PathVariable String projectName, @PathVariable String item, Principal principal) {
		return deleteTestItemHandler.deleteTestItem(item, EntityUtils.normalizeProjectName(projectName), principal.getName());
	}

	@Override
	@RequestMapping(method = PUT)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Update issues of specified test items")
	public List<Issue> defineTestItemIssueType(@PathVariable String projectName, @RequestBody @Validated DefineIssueRQ request,
			Principal principal) {
		return updateTestItemHandler.defineTestItemsIssues(EntityUtils.normalizeProjectName(projectName), request, principal.getName());
	}

	@Override
	@RequestMapping(value = "/history", method = GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Load history of test items")
	public List<TestItemHistoryElement> getItemsHistory(@PathVariable String projectName,
			@RequestParam(value = "history_depth", required = false, defaultValue = DEFAULT_HISTORY_DEPTH) int historyDepth,
			@RequestParam(value = "ids") String[] ids,
			@RequestParam(value = "is_full", required = false, defaultValue = DEFAULT_HISTORY_FULL) boolean showBrokenLaunches,
			Principal principal) {
		return testItemsHistoryHandler.getItemsHistory(EntityUtils.normalizeProjectName(projectName), ids, historyDepth,
				showBrokenLaunches);
	}

	@Override
	@RequestMapping(value = "/tags", method = GET)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Get all unique tags of specified launch")
	public List<String> getAllTags(@PathVariable String projectName, @RequestParam(value = "launch") String id,
			@RequestParam(value = FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.CNT + Launch.TAGS) String value,
			Principal principal) {
		return getTestItemHandler.getTags(id, value);
	}

	@Override
	@RequestMapping(value = "/{item}/update", method = PUT)
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation("Update test item")
	public OperationCompletionRS updateTestItem(@PathVariable String projectName, @PathVariable String item,
			@RequestBody @Validated UpdateTestItemRQ rq, Principal principal) {
		return updateTestItemHandler.updateTestItem(EntityUtils.normalizeProjectName(projectName), item, rq, principal.getName());
	}

	@Override
	@RequestMapping(value = "/issue/add", method = PUT)
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation("Attach external issue for specified test items")
	public List<OperationCompletionRS> addExternalIssues(@PathVariable String projectName, @RequestBody @Validated AddExternalIssueRQ rq,
			Principal principal) {
		return updateTestItemHandler.addExternalIssues(EntityUtils.normalizeProjectName(projectName), rq, principal.getName());
	}

	@Override
	@RequestMapping(value = "/items", method = GET)
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	public List<TestItemResource> getTestItems(@PathVariable String projectName, @RequestParam(value = "ids") String[] ids,
			Principal principal) {
		return getTestItemHandler.getTestItems(ids);
	}
}