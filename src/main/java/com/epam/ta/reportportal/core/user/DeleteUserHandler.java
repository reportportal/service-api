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

package com.epam.ta.reportportal.core.user;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.DeleteBulkRQ;
import com.epam.ta.reportportal.ws.model.DeleteBulkRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

/**
 * Delete request handler
 *
 * @author Aliaksandr_Kazantsau
 */
public interface DeleteUserHandler {
	/**
	 * Delete User
	 *
	 * @param userId
	 * @return
	 */
	OperationCompletionRS deleteUser(Long userId, ReportPortalUser currentUser);

	/**
	 * Delete Users
	 *
	 * @param deleteBulkRQ {@link DeleteBulkRQ}
	 * @return
	 */
	DeleteBulkRS deleteUsers(DeleteBulkRQ deleteBulkRQ, ReportPortalUser currentUser);
}