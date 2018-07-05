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
import com.epam.ta.reportportal.core.item.DeleteTestItemHandler;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.item.UpdateTestItemHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.database.dao.TestItemStructureRepository;
import com.epam.ta.reportportal.store.database.entity.item.TestItemStructure;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.item.LinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UnlinkExternalIssueRq;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epam.ta.reportportal.store.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping("/{projectName}/item")
//@PreAuthorize(ASSIGNED_TO_PROJECT)
public class TestItemController {

	public static final String DEFAULT_HISTORY_DEPTH = "5";
	public static final String DEFAULT_HISTORY_FULL = "true";

	@Autowired
	private StartTestItemHandler startTestItemHandler;

	@Autowired
	private DeleteTestItemHandler deleteTestItemHandler;

	@Autowired
	private FinishTestItemHandler finishTestItemHandler;

	@Autowired
	private UpdateTestItemHandler updateTestItemHandler;

	//	@Autowired
	//	private GetTestItemHandler getTestItemHandler;

	//	@Autowired
	//	private TestItemsHistoryHandler testItemsHistoryHandler;

	//	@Autowired
	//	private MergeTestItemHandler mergeTestItemHandler;

	@PostMapping
	@Transactional
	@ResponseBody
	@ResponseStatus(CREATED)
	@ApiOperation("Start a root test item")
	//@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS startRootItem(@PathVariable String projectName, @RequestBody @Validated StartTestItemRQ startTestItemRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return startTestItemHandler.startRootItem(user, projectName, startTestItemRQ);
	}

	@Autowired
	private TestItemStructureRepository itemStructure;

	@GetMapping("/{itemId}")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	public TestItemResource getTestItem(@PathVariable String projectName, @PathVariable Long itemId,
			@AuthenticationPrincipal ReportPortalUser user) {
		TestItemStructure testItem = itemStructure.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
		return TestItemConverter.TO_RESOURCE.apply(testItem);
	}

	@PostMapping("/{parentItem}")
	@Transactional
	@ResponseBody
	@ResponseStatus(CREATED)
	@ApiOperation("Start a child test item")
	//@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS startChildItem(@PathVariable String projectName, @PathVariable Long parentItem,
			@RequestBody @Validated StartTestItemRQ startTestItemRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return startTestItemHandler.startChildItem(user, projectName, startTestItemRQ, parentItem);
	}

	@PutMapping("/{testItemId}")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Finish test item")
	//@PreAuthorize(ALLOWED_TO_REPORT)
	public OperationCompletionRS finishTestItem(@PathVariable String projectName, @PathVariable Long testItemId,
			@RequestBody @Validated FinishTestItemRQ finishExecutionRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return finishTestItemHandler.finishTestItem(user, projectName, testItemId, finishExecutionRQ);
	}

	//	@GetMapping("/{testItemId}")
	//	@ResponseBody
	//	@ResponseStatus(OK)
	//	@ApiOperation("Find test item by ID")
	//	public TestItem getTestItem(@PathVariable String projectName, @PathVariable String testItemId,
	//			@AuthenticationPrincipal ReportPortalUser user) {
	//		//testItemRepository.selectPathNames(11L);
	//		//return getTestItemHandler.getTestItem(testItemId);
	//		return testItemRepository.findById(3l).orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "3"));
	//	}

	//
	//	@GetMapping
	//	@ResponseBody
	//	@ResponseStatus(OK)
	//	@ApiOperation("Find test items by specified filter")
	//	public Iterable<TestItemResource> getTestItems(@PathVariable String projectName, @FilterFor(TestItem.class) Filter filter,
	//			@FilterFor(TestItem.class) Queryable predefinedFilter, @SortFor(TestItem.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
	//		return getTestItemHandler.getTestItems(new CompositeFilter(filter, predefinedFilter), pageable);
	//	}

	@DeleteMapping("/{itemId}")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Delete test item")
	public OperationCompletionRS deleteTestItem(@PathVariable String projectName, @PathVariable Long itemId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteTestItemHandler.deleteTestItem(itemId, projectName, user);
	}

	@ResponseBody
	@Transactional
	@ResponseStatus(OK)
	@DeleteMapping
	@ApiOperation("Delete test items by specified ids")
	public List<OperationCompletionRS> deleteTestItems(@PathVariable String projectName, @RequestParam(value = "ids") Long[] ids,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteTestItemHandler.deleteTestItem(ids, projectName, user);
	}

	@PutMapping
	@Transactional
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Update issues of specified test items")
	public List<Issue> defineTestItemIssueType(@PathVariable String projectName, @RequestBody @Validated DefineIssueRQ request,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateTestItemHandler.defineTestItemsIssues(projectName, request, user);
	}

	//	@GetMapping("/history")
	//	@ResponseStatus(OK)
	//	@ResponseBody
	//	@ApiOperation("Load history of test items")
	//	public List<TestItemHistoryElement> getItemsHistory(@PathVariable String projectName,
	//			@RequestParam(value = "history_depth", required = false, defaultValue = DEFAULT_HISTORY_DEPTH) int historyDepth,
	//			@RequestParam(value = "ids") String[] ids,
	//			@RequestParam(value = "is_full", required = false, defaultValue = DEFAULT_HISTORY_FULL) boolean showBrokenLaunches,
	//			@AuthenticationPrincipal ReportPortalUser user) {
	//		throw new UnsupportedOperationException();
	//		//return testItemsHistoryHandler.getItemsHistory(normalizeId(projectName), ids, historyDepth, showBrokenLaunches);
	//	}
	//
	//	@GetMapping("/tags")
	//	@ResponseBody
	//	@ResponseStatus(OK)
	//	@ApiOperation("Get all unique tags of specified launch")
	//	public List<String> getAllTags(@PathVariable String projectName, @RequestParam(value = "launch") String id,
	//			@RequestParam(value = "filter." + "cnt." + "tags") String value, @AuthenticationPrincipal ReportPortalUser user) {
	//		//return getTestItemHandler.getTags(id, value);
	//		return null;
	//	}

	@PutMapping("/{itemId}/update")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	//@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation("Update test item")
	public OperationCompletionRS updateTestItem(@PathVariable String projectName, @PathVariable Long itemId,
			@RequestBody @Validated UpdateTestItemRQ rq, @AuthenticationPrincipal ReportPortalUser user) {
		return updateTestItemHandler.updateTestItem(projectName, itemId, rq, user);
	}

	@Transactional
	@PutMapping("/issue/link")
	@ResponseBody
	@ResponseStatus(OK)
	//@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation("Attach external issue for specified test items")
	public List<OperationCompletionRS> addExternalIssues(@PathVariable String projectName, @RequestBody @Validated LinkExternalIssueRQ rq,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateTestItemHandler.linkExternalIssues(normalizeId(projectName), rq, user);
	}

	@Transactional
	@PutMapping("/issue/unlink")
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Unlink external issue for specified test items")
	public List<OperationCompletionRS> unlinkExternalIssues(@PathVariable String projectName,
			@RequestBody @Validated UnlinkExternalIssueRq rq, @AuthenticationPrincipal ReportPortalUser user) {
		return updateTestItemHandler.unlinkExternalIssues(normalizeId(projectName), rq, user);
	}

	//
	//	@GetMapping("/items")
	//	@ResponseBody
	//	@ResponseStatus(OK)
	//	//@PreAuthorize(ASSIGNED_TO_PROJECT)
	//	@ApiOperation("Get test items by specified ids")
	//	public List<TestItemResource> getTestItems(@PathVariable String projectName, @RequestParam(value = "ids") String[] ids,
	//			@AuthenticationPrincipal ReportPortalUser user) {
	//		//return getTestItemHandler.getTestItems(ids);
	//		return null;
	//	}
	//
	//	@PutMapping("/{item}/merge")
	//	@ResponseBody
	//	@ResponseStatus(OK)
	//	//@PreAuthorize(ASSIGNED_TO_PROJECT)
	//	//    @ApiOperation("Merge test item")
	//	@ApiIgnore
	//	public OperationCompletionRS mergeTestItem(@PathVariable String projectName, @PathVariable String item,
	//			@RequestBody @Validated MergeTestItemRQ rq, @AuthenticationPrincipal ReportPortalUser user) {
	//		throw new UnsupportedOperationException();
	//		//return mergeTestItemHandler.mergeTestItem(normalizeId(projectName), item, rq, principal.getName());
	//	}
}
