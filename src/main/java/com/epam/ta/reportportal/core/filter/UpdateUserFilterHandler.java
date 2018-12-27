/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.filter;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.filter.BulkUpdateFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;

import java.util.List;

/**
 * Update user filter handler
 *
 * @author Aliaksei_Makayed
 */
public interface UpdateUserFilterHandler {

	/**
	 * Creates new filter
	 *
	 * @param createFilterRQ
	 * @param projectName
	 * @param user
	 * @return EntryCreatedRS
	 */
	EntryCreatedRS createFilter(UpdateUserFilterRQ createFilterRQ, String projectName, ReportPortalUser user);

	/**
	 * Update user filter with specified id
	 *
	 * @param userFilterId   User filter id
	 * @param updateRQ       Update filter details
	 * @param projectDetails Project details
	 * @param user           User
	 * @return {@link OperationCompletionRS}
	 */
	OperationCompletionRS updateUserFilter(Long userFilterId, UpdateUserFilterRQ updateRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user);

	/**
	 * Update user filter
	 *
	 * @param updateRQ
	 * @param projectDetails
	 * @param user
	 * @return List of {@link OperationCompletionRS}
	 */
	List<OperationCompletionRS> updateUserFilter(CollectionsRQ<BulkUpdateFilterRQ> updateRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user);

}