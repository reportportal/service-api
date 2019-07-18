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
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.user.*;

/**
 * Post request handler
 *
 * @author Andrei_Ramanchuk
 */
public interface CreateUserHandler {

	/**
	 * Create completed user object by administrator
	 *
	 * @param request
	 * @return
	 */
	CreateUserRS createUserByAdmin(CreateUserRQFull request, ReportPortalUser user, String basicUrl);

	/**
	 * Create new User (confirm invitation)
	 *
	 * @param request
	 * @param uuid
	 * @return
	 * @throws ReportPortalException
	 */
	CreateUserRS createUser(CreateUserRQConfirm request, String uuid);

	/**
	 * Create user bid (send invitation)
	 *
	 * @param request
	 * @param username
	 * @return
	 */
	CreateUserBidRS createUserBid(CreateUserRQ request, ReportPortalUser username, String userRegURL);

	/**
	 * Create restore password bid
	 *
	 * @param rq
	 * @param baseUrl
	 * @return
	 */
	OperationCompletionRS createRestorePasswordBid(RestorePasswordRQ rq, String baseUrl);

	/**
	 * Reset password
	 *
	 * @param rq
	 * @return
	 */
	OperationCompletionRS resetPassword(ResetPasswordRQ rq);

	/**
	 * Verify reset password bid exist
	 *
	 * @param uuid
	 * @return
	 */
	YesNoRS isResetPasswordBidExist(String uuid);
}