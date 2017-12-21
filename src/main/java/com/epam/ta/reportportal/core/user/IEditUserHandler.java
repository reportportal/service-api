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

import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.ws.model.user.EditUserRQ;
import org.springframework.web.multipart.MultipartFile;

/**
 * Edit request handler
 *
 * @author Aliaksandr_Kazantsau
 */
public interface IEditUserHandler {

	/**
	 * Edit User
	 *
	 * @param username
	 * @param editUserRQ
	 * @param role
	 * @return
	 * @throws ReportPortalException
	 */
	OperationCompletionRS editUser(String username, EditUserRQ editUserRQ, UserRole role);

	/**
	 * Upload photo
	 *
	 * @param username
	 * @param file
	 * @return
	 */
	OperationCompletionRS uploadPhoto(String username, MultipartFile file);

	/**
	 * Delete user's photo
	 *
	 * @param username
	 * @return
	 */
	OperationCompletionRS deletePhoto(String username);

	/**
	 * Change password
	 *
	 * @param username
	 * @param changePasswordRQ
	 * @return
	 */
	OperationCompletionRS changePassword(String username, ChangePasswordRQ changePasswordRQ);
}