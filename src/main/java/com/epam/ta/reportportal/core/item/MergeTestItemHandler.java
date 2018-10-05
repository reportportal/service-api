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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.item.MergeTestItemRQ;

public interface MergeTestItemHandler {
	/**
	 * Merge test items specified in rq to item
	 *
	 * @param projectDetails project details
	 * @param itemId         test item ID
	 * @param rq             merge test item request data. Contains list of items we want to merge
	 * @param userName       request principal name
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS mergeTestItem(ReportPortalUser.ProjectDetails projectDetails, Long itemId, MergeTestItemRQ rq, String userName);
}
