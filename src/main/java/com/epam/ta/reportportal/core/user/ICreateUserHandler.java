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

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.user.*;

import java.security.Principal;

/**
 * Post request handler
 *
 * @author Andrei_Ramanchuk
 */
public interface ICreateUserHandler {

	/**
	 * Create completed user object by administrator
	 *
	 * @param request
	 * @param principal
	 * @return
	 */
	CreateUserRS createUserByAdmin(CreateUserRQFull request, String userName, String basicUrl);

	/**
	 * Create new User (confirm invitation)
	 *
	 * @param request
	 * @param uuid
	 * @param principal
	 * @return
	 * @throws ReportPortalException
	 */
	CreateUserRS createUser(CreateUserRQConfirm request, String uuid, Principal principal);

	/**
	 * Create user bid (send invitation)
	 *
	 * @param request
	 * @param principal
	 * @return
	 */
	CreateUserBidRS createUserBid(CreateUserRQ request, Principal principal, String userRegURL);

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
	 * @param id
	 * @return
	 */
	YesNoRS isResetPasswordBidExist(String id);
}