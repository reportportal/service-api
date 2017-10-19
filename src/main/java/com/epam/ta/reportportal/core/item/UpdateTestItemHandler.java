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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.item.AddExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;

import java.util.List;

/**
 * Handler to update test item issue type and issue statistics
 *
 * @author Dzianis Shlychkou
 */
public interface UpdateTestItemHandler {

	/**
	 * Define TestItem issue (or list of issues)
	 *
	 * @param project     project name
	 * @param defineIssue issues request data
	 * @param userName    request principal name
	 * @return list of defined issues for specified test items
	 */
	List<Issue> defineTestItemsIssues(String project, DefineIssueRQ defineIssue, String userName);

	/**
	 * Update specified test item
	 *
	 * @param item     test item ID
	 * @param rq       update test item request data
	 * @param userName request principal name
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS updateTestItem(String projectName, String item, UpdateTestItemRQ rq, String userName);

	/**
	 * Add external system issue link directly to test items
	 *
	 * @param projectName
	 * @param rq
	 * @param userName
	 * @return
	 */
	List<OperationCompletionRS> addExternalIssues(String projectName, AddExternalIssueRQ rq, String userName);
}