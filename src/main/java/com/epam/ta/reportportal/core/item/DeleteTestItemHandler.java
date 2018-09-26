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