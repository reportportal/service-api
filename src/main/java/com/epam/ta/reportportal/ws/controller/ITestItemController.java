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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.Queryable;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.item.AddExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.MergeTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;

public interface ITestItemController {

	/**
	 * Starts root test item (root item for launch)
	 *
	 * @param projectName
	 * @param testItem
	 * @param principal
	 * @return EntryCreatedRS
	 */
	EntryCreatedRS startRootItem(String projectName, StartTestItemRQ testItem, Principal principal);

	/**
	 * Starts child test item
	 *
	 * @param projectName
	 * @param parentItem
	 * @param startLaunch
	 * @param principal
	 * @return EntryCreatedRS
	 */
	EntryCreatedRS startChildItem(String projectName, String parentItem, StartTestItemRQ startLaunch, Principal principal);

	/**
	 * Finishes specified test item
	 *
	 * @param finishExecutionRQ
	 * @param principal
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS finishTestItem(String projectName, String item, FinishTestItemRQ finishExecutionRQ, Principal principal);

	/**
	 * Gets Test Item by ID
	 *
	 * @param projectName
	 * @param item
	 * @param principal
	 * @return TestItemResource
	 */
	TestItemResource getTestItem(String projectName, String item, Principal principal);

	/**
	 * Gets all Test Items of specified launch
	 *
	 * @param projectName
	 * @param filter
	 * @param pageble
	 * @param principal
	 * @return Iterable<TestItemResource>
	 */
	Iterable<TestItemResource> getTestItems(String projectName, Filter filter, Queryable predefinedFilter, Pageable pageble,
			Principal principal);

	/**
	 * Deletes Test Item
	 *
	 * @param projectName
	 * @param item
	 * @param principal
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS deleteTestItem(String projectName, String item, Principal principal);

	List<OperationCompletionRS> deleteTestItems(String projectName, String[] ids, Principal principal);

	/**
	 * Update test item issue block (defects) and updated statistics
	 *
	 * @param projectName
	 * @param request
	 * @return List<Issue>
	 */
	List<Issue> defineTestItemIssueType(String projectName, DefineIssueRQ request, Principal principal);

	/**
	 * Get test item's history. Result map structure:
	 * <ul>
	 * <li>key - launch number.
	 * <li>value - list of testItemResources in current launch.
	 * </ul>
	 *
	 * @param projectName
	 * @param historyDepth
	 * @param principal
	 * @param ids
	 * @param showBrokenLaunches - <b>boolean</b> should in_progress and interrupted launches
	 *                           been included in history:<br>
	 *                           <code>true</code> - if history should contain all launch
	 *                           statuses<br>
	 *                           <code>false</code> - if history should contain only passed and
	 *                           failed launches
	 * @return Map<String, List<TestItemResource>>
	 */
	List<TestItemHistoryElement> getItemsHistory(String projectName, int historyDepth, String[] ids, boolean showBrokenLaunches,
			Principal principal);

	/**
	 * Get specified tags
	 *
	 * @param project
	 * @param launchId
	 * @param value
	 * @param principal
	 * @return List<String>
	 */
	List<String> getAllTags(String project, String launchId, String value, Principal principal);

	/**
	 * Update test items
	 *
	 * @param projectName
	 * @param item
	 * @param rq
	 * @param principal
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS updateTestItem(String projectName, String item, UpdateTestItemRQ rq, Principal principal);

	/**
	 * Attach external issues
	 *
	 * @param projectName
	 * @param rq
	 * @param principal
	 * @return List<OperationCompletionRS>
	 */
	List<OperationCompletionRS> addExternalIssues(String projectName, AddExternalIssueRQ rq, Principal principal);

	List<TestItemResource> getTestItems(String projectName, String[] ids, Principal principal);

	/**
	 * Merge Suites
	 *
	 * @param projectName
	 * @param item
	 * @param rq
	 * @param principal
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS mergeTestItem(String projectName, String item, MergeTestItemRQ rq, Principal principal);
}
