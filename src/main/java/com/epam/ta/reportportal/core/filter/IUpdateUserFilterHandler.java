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

package com.epam.ta.reportportal.core.filter;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.filter.BulkUpdateFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;

import java.util.List;

/**
 * Update user filter handler
 *
 * @author Aliaksei_Makayed
 */
public interface IUpdateUserFilterHandler {

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