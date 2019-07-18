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