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

import com.epam.ta.reportportal.auth.ReportPortalUser;
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
	 * Updates {@link TestItem} instance
	 *
	 * @param testItemId        Test item ID
	 * @param finishExecutionRQ Request with finish Test Item data
	 * @param user              RQ principal
	 * @param projectName       Project
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS finishTestItem(ReportPortalUser user, String projectName, Long testItemId, FinishTestItemRQ finishExecutionRQ);
}
