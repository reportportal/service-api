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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.item.AddExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.MergeTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;

import java.util.List;

public interface ITestItemController {

	/**
	 * Starts root test item (root item for launch)
	 *
	 * @param projectName
	 * @param testItem
	 * @param user
	 * @return EntryCreatedRS
	 */
	EntryCreatedRS startRootItem(String projectName, StartTestItemRQ testItem, ReportPortalUser user);

	/**
	 * Starts child test item
	 *
	 * @param projectName
	 * @param parentItem
	 * @param startLaunch
	 * @param user
	 * @return EntryCreatedRS
	 */
	EntryCreatedRS startChildItem(String projectName, Long parentItem, StartTestItemRQ startLaunch, ReportPortalUser user);

	/**
	 * Finishes specified test item
	 *
	 * @param finishExecutionRQ
	 * @param user
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS finishTestItem(String projectName, Long item, FinishTestItemRQ finishExecutionRQ, ReportPortalUser user);

	/**
	 * Gets Test Item by ID
	 *
	 * @param projectName
	 * @param item
	 * @param user
	 * @return TestItemResource
	 */
	TestItem getTestItem(String projectName, String item, ReportPortalUser user);

	//	/**
	//	 * Gets all Test Items of specified launch
	//	 *
	//	 * @param projectName
	//	 * @param filter
	//	 * @param pageble
	//	 * @param user
	//	 * @return Iterable<TestItemResource>
	//	 */
	//	Iterable<TestItemResource> getTestItems(String projectName, Filter filter, Queryable predefinedFilter, Pageable pageble,
	//			ReportPortalUser user);

	/**
	 * Deletes Test Item
	 *
	 * @param projectName
	 * @param itemId
	 * @param user
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS deleteTestItem(String projectName, Long itemId, ReportPortalUser user);

	/**
	 * Deletes Test Items
	 *
	 * @param projectName
	 * @param ids
	 * @param user
	 * @return
	 */
	List<OperationCompletionRS> deleteTestItems(String projectName, Long[] ids, ReportPortalUser user);

	/**
	 * Update test item issue block (defects) and updated statistics
	 *
	 * @param projectName
	 * @param request
	 * @return List<Issue>
	 */
	List<Issue> defineTestItemIssueType(String projectName, DefineIssueRQ request, ReportPortalUser user);

	/**
	 * Get test item's history. Result map structure:
	 * <ul>
	 * <li>key - launch number.
	 * <li>value - list of testItemResources in current launch.
	 * </ul>
	 *
	 * @param projectName
	 * @param historyDepth
	 * @param user
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
			ReportPortalUser user);

	/**
	 * Get specified tags
	 *
	 * @param project
	 * @param launchId
	 * @param value
	 * @param user
	 * @return List<String>
	 */
	List<String> getAllTags(String project, String launchId, String value, ReportPortalUser user);

	/**
	 * Update test items
	 *
	 * @param projectName
	 * @param itemId
	 * @param rq
	 * @param user
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS updateTestItem(String projectName, Long itemId, UpdateTestItemRQ rq, ReportPortalUser user);

	/**
	 * Attach external issues
	 *
	 * @param projectName
	 * @param rq
	 * @param user
	 * @return List<OperationCompletionRS>
	 */
	List<OperationCompletionRS> addExternalIssues(String projectName, AddExternalIssueRQ rq, ReportPortalUser user);

	List<TestItemResource> getTestItems(String projectName, String[] ids, ReportPortalUser user);

	/**
	 * Merge Suites
	 *
	 * @param projectName
	 * @param item
	 * @param rq
	 * @param user
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS mergeTestItem(String projectName, String item, MergeTestItemRQ rq, ReportPortalUser user);
}
