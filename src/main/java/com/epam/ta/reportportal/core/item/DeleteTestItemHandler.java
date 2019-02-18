/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

import java.util.List;

/**
 * Handler for delete test item operation
 *
 * @author Andrei Varabyeu
 * @author Aliaksei Makayed
 */
public interface DeleteTestItemHandler {

	/**
	 * Delete test item by id.
	 *
	 * @param itemId         Item id
	 * @param projectDetails Project Details
	 * @param user           User
	 * @return {@link OperationCompletionRS}
	 */
	OperationCompletionRS deleteTestItem(Long itemId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

	/**
	 * Delete list of items by ids.
	 *
	 * @param ids            Test item ids
	 * @param projectDetails Project Details
	 * @param user           User
	 * @return {@link OperationCompletionRS}
	 */
	List<OperationCompletionRS> deleteTestItem(Long[] ids, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);
}