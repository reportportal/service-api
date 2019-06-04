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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

/**
 * Handler for finish test item operation
 *
 * @author Andrei Varabyeu
 * @author Aliaksei Makayed
 */
public interface FinishTestItemHandler {

	/**
	 * Updates {@link com.epam.ta.reportportal.entity.item.TestItem} instance
	 *
	 * @param user              RQ principal
	 * @param projectDetails    Project Details
	 * @param testItemId        Test item ID
	 * @param finishExecutionRQ Request with finish Test Item data
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS finishTestItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, String testItemId,
			FinishTestItemRQ finishExecutionRQ);
}